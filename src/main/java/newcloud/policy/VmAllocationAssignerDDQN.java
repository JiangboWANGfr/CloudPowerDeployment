package newcloud.policy;

import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.dataset.DataSet;
import java.util.*;

public class VmAllocationAssignerDDQN {
    private final double LEARNING_GAMMA = 0.9;
    private final double LEARNING_ALPHA = 0.0001;
    private final int NUM_HOSTS = 300;
    private final int outputSize = 300;
    private final int hiddenLayerSize = 128;
    private final int BATCHSIZE = 32;
    private double epsilonDecay = 0.995;
    private double minEpsilon = 0.1;
    private int trainingStep = 0;
    private final List<Experience> experienceReplay = new ArrayList<>();
    private double epsilon;
    private MultiLayerNetwork model;
    private MultiLayerNetwork targetModel;

    public void updateEpsilon() {
        epsilon = Math.max(minEpsilon, epsilon * epsilonDecay);
    }

    public VmAllocationAssignerDDQN(double epsilon) {
        this.epsilon = epsilon;
        this.model = buildNetwork();
        this.targetModel = buildNetwork();
        this.targetModel.setParams(this.model.params());
    }

    public MultiLayerNetwork buildNetwork() {
        MultiLayerNetwork net = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(LEARNING_ALPHA))
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(NUM_HOSTS)
                        .nOut(hiddenLayerSize)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(hiddenLayerSize)
                        .nOut(hiddenLayerSize)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(hiddenLayerSize)
                        .nOut(outputSize)
                        .activation(Activation.IDENTITY)
                        .build())
                .build());

        net.init();
        return net;
    }

    public int selectAction(INDArray state) {
        if (Math.random() < epsilon) {
            return new Random().nextInt(outputSize);
        }
        INDArray qValues = model.output(state, false);
        return qValues.argMax().getInt(0);
    }

    public void storeExperience(INDArray state, int action, double reward, INDArray nextState) {
        experienceReplay.add(new Experience(state, action, reward, nextState));
        if (experienceReplay.size() > 200) {
            experienceReplay.remove(0);
        }
    }

    public void trainModel() {
        trainingStep++;
        if (experienceReplay.size() < BATCHSIZE) return;

        List<INDArray> states = new ArrayList<>();
        List<INDArray> nextStates = new ArrayList<>();
        List<Double> rewards = new ArrayList<>();
        List<Integer> actions = new ArrayList<>();

        for (int i = 0; i < BATCHSIZE; i++) {
            Experience exp = experienceReplay.get(i);
            states.add(exp.state);
            nextStates.add(exp.nextState);
            rewards.add(exp.reward);
            actions.add(exp.action);
        }

        INDArray stateArray = Nd4j.vstack(states);
        INDArray nextStateArray = Nd4j.vstack(nextStates);

        INDArray qValues = model.output(stateArray, false);
        INDArray nextQValues = targetModel.output(nextStateArray, false);

        INDArray bestActions = nextQValues.argMax(1);
        INDArray maxNextQValues = Nd4j.zeros(BATCHSIZE);
        for (int i = 0; i < BATCHSIZE; i++) {
            int bestAction = bestActions.getInt(i);
            maxNextQValues.putScalar(i, nextQValues.getDouble(i, bestAction));
        }

        INDArray targetQValues = qValues.dup();
        for (int i = 0; i < BATCHSIZE; i++) {
            int action = actions.get(i);
            double reward = rewards.get(i);
            double maxNextQ = maxNextQValues.getDouble(i);
            double targetQ = reward + LEARNING_GAMMA * maxNextQ;
            targetQValues.putScalar(i, action, targetQ);
        }

        model.fit(new DataSet(stateArray, targetQValues));

        if (trainingStep % 5 == 0) {
            targetModel.setParams(model.params());
        }
    }

    public static class Experience {
        INDArray state;
        int action;
        double reward;
        INDArray nextState;

        public Experience(INDArray state, int action, double reward, INDArray nextState) {
            this.state = state;
            this.action = action;
            this.reward = reward;
            this.nextState = nextState;
        }
    }
}
