package newcloud.policy;

import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.dataset.DataSet;

import static newcloud.Constants.NUMBER_OF_HOSTS;
import static newcloud.Constants.*;
import java.util.*;

public class VmAllocationAssignerDDQNLSTM {
    private final double LEARNING_GAMMA = 0.9; // 折扣因子
    private final double LEARNING_ALPHA = 0.0001; // 学习率
    private final int NUM_HOSTS = 300; // 输入维度（可根据主机数调整）
//    private final int TIME_STEP = 1;
    private final int outputSize = 300; // 输出维度（可根据 VM 目标主机数调整）
    private final int hiddenLayerSize = 64; // 隐藏层大小
    private final int timeSteps; // LSTM time_steps
    private final int BATCHSIZE = 32; // 批处理大小
    private double epsilonDecay = 0.995; // 控制探索率下降速度
    private double minEpsilon = 0.1; // 最小探索率
    private int trainingStep = 0;

    private final List<Experience> experienceReplay = new ArrayList<>();
    private  double epsilon;
    private MultiLayerNetwork model;
    private MultiLayerNetwork targetModel;

    public void updateEpsilon() {
        epsilon = Math.max(minEpsilon, epsilon * epsilonDecay);
    }


    public VmAllocationAssignerDDQNLSTM(double epsilon, int timeSteps) {
        this.epsilon = epsilon;
        this.timeSteps = timeSteps;
        this.model = buildNetwork();
        this.targetModel = buildNetwork();
        this.targetModel.setParams(this.model.params());
    }
    public MultiLayerNetwork buildNetwork() {
        MultiLayerNetwork net = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(1e-3)) // Adam 优化器
                .weightInit(WeightInit.XAVIER)
                .list()

                // LSTM 层
                .layer(0, new LSTM.Builder()
                        .nIn(NUM_HOSTS)  // 输入特征数
                        .nOut(128)       // LSTM 隐藏层单元数
                        .activation(Activation.TANH)
                        .build())

                // 全连接层
                .layer(1, new DenseLayer.Builder()
                        .nIn(128)
                        .nOut(128)
                        .activation(Activation.RELU)
                        .build())

                // 输出层（Q 值）
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(128)
                        .nOut(outputSize)
                        .activation(Activation.IDENTITY)
                        .build())

//                .setInputType(InputType.recurrent(NUM_HOSTS,TIME_STEP)) // 关键：确保 LSTM 处理序列输入
                .build()
        );

        net.init();
        return net;
    }

    /**
     * 返回训练步数
     * @return 训练步数
     */
    public int getTrainingSteps() {
        return trainingStep;
    }

    /**
     * 选择一个主机进行 VM 分配
     * @param stateTensor 当前状态 (batch_size=1, time_steps, features)
     * @param timeStep LSTM 需要的时间步长
     * @return 选中的 host ID
     */
    public int selectAction(INDArray stateTensor, int timeStep) {
        stateTensor = stateTensor.permute(0, 2, 1);
        System.out.println("stateTensor: " + Arrays.toString(stateTensor.shape()));
        if (Math.random() < epsilon) {
            return new Random().nextInt(outputSize);
        }

            // 获取模型的 Q 值预测
            INDArray qValues = model.output(stateTensor,false);

            // 打印输出形状
            System.out.println("Model output shape: " + Arrays.toString(qValues.shape()));
            System.out.println("Model output: " + qValues);

            // 只取最后一个时间步的 Q 值
            INDArray finalQValues = qValues.getRow(qValues.rows() - 1);
            System.out.println("Final Q-values (last timestep): " + finalQValues);

            // 选择 Q 值最高的动作（最佳主机）
            int selectedHost = finalQValues.argMax().getInt(0);
            System.out.println("Selected Host: " + selectedHost);

            return selectedHost;
    }

    /**
     * 存储经验 (state, action, reward, nextState) 以供训练
     * @param state 当前状态 (batch_size=1, time_steps, features)
     * @param action 选中的主机 ID
     * @param reward 奖励值
     * @param nextState 下一个状态 (batch_size=1, time_steps, features)
     */
    public void storeExperience(INDArray state, int action, double reward, INDArray nextState) {
        experienceReplay.add(new Experience(state, action, reward, nextState));
        if (experienceReplay.size() > 200) { // 限制经验池大小
            experienceReplay.remove(0);
        }
    }

    /**
     * 训练 DDQN 模型
     */
    public void trainModel() {
        trainingStep++;
        int batchSize = BATCHSIZE;
        if (experienceReplay.size() < batchSize) return;

        List<INDArray> states = new ArrayList<>();
        List<INDArray> nextStates = new ArrayList<>();
        List<Double> rewards = new ArrayList<>();
        List<Integer> actions = new ArrayList<>();

        for (int i = 0; i < batchSize; i++) {
            Experience exp = experienceReplay.get(i);
            states.add(exp.state);
            nextStates.add(exp.nextState);
            rewards.add(exp.reward);
            actions.add(exp.action);
        }

        INDArray stateArray = Nd4j.concat(0, states.toArray(new INDArray[0])).permute(0, 2, 1);
        INDArray nextStateArray = Nd4j.concat(0, nextStates.toArray(new INDArray[0])).permute(0, 2, 1);
        System.out.println("stateArray: " + Arrays.toString(stateArray.shape()));
        System.out.println("nextStateArray: " + Arrays.toString(nextStateArray.shape()));

        //  计算 Q(s, a) 和 Q(s', a')
        INDArray qValues = model.output(stateArray, false);
        INDArray nextQValues = targetModel.output(nextStateArray, false);
        System.out.println("qValues: " + Arrays.toString(qValues.shape()));
        System.out.println("nextQValues: " + Arrays.toString(nextQValues.shape()));

        // 修正 qValues 维度问题
        INDArray reshapedQValues = qValues.reshape(batchSize, timeSteps, outputSize);
        INDArray reshapedNextQValues = nextQValues.reshape(batchSize, timeSteps, outputSize);

        // 取最后一个时间步
        INDArray lastQValues = reshapedQValues.get(NDArrayIndex.all(), NDArrayIndex.point(timeSteps - 1), NDArrayIndex.all());
        INDArray lastNextQValues = reshapedNextQValues.get(NDArrayIndex.all(), NDArrayIndex.point(timeSteps - 1), NDArrayIndex.all());

        // 取出最后一个时间步的 Q 值，得到 2D 形状 (batch_size, action_space)
        System.out.println("lastQValues: " + Arrays.toString(lastQValues.shape()));
        System.out.println("lastNextQValues: " + Arrays.toString(lastNextQValues.shape()));

        INDArray bestActions = lastNextQValues.argMax(1);
        System.out.println("bestActions: " + Arrays.toString(bestActions.shape()));
        INDArray maxNextQValues = Nd4j.zeros(batchSize);
        for (int i = 0; i < batchSize; i++) {
            int bestAction = bestActions.getInt(i);
            maxNextQValues.putScalar(i, lastNextQValues.getDouble(i, bestAction));
        }

        INDArray targetQValues = lastQValues.dup();
        for (int i = 0; i < batchSize; i++) {
            int action = actions.get(i);
            double reward = rewards.get(i);
            double maxNextQ = maxNextQValues.getDouble(i);
            double targetQ = reward + LEARNING_GAMMA * maxNextQ;
            targetQValues.putScalar(i, action, targetQ);
        }

        System.out.println("stateArray: " + Arrays.toString(stateArray.shape()));
        System.out.println("targetQValues: " + Arrays.toString(targetQValues.shape()));
        INDArray lastStateArray = stateArray.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(TIME_STEPS - 1));
        lastStateArray = lastStateArray.reshape(batchSize, NUM_HOSTS, 1); // 确保 3D 输入

        model.fit(new DataSet(lastStateArray, targetQValues));
//        updateEpsilon();

        if(trainingStep % 5 == 0) {
            System.out.println("Target Q-values: " + targetQValues);
            targetModel.setParams(model.params());
        }
    }

    /**
     * 经验回放存储类
     */
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
