import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.TrainingListener;
//import org.deeplearning4j.rl4j.agent.learning.algorithm.dqn.DQN;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.*;

public class DDQNLSTMAllocation {
    private static final int NUM_HOSTS = 300; // 300 台主机
    private static final int HISTORY_LENGTH = 10; // LSTM 观察过去 10 个时间步
    private static final int ACTIONS = 300; // 可选择 300 个主机
    private static final double GAMMA = 0.99; // 折扣因子
    private static final double EPSILON = 0.1; // 探索率
    private static final int BATCH_SIZE = 64;
    private static final int MEMORY_SIZE = 10000;
    private static final int TARGET_UPDATE = 50;

    private MultiLayerNetwork model;
    private MultiLayerNetwork targetModel;
    private Deque<double[][]> replayBuffer;

    private Random random;

    public DDQNLSTMAllocation() {
        this.model = buildNetwork();
        this.targetModel = buildNetwork();
        this.targetModel.setParams(this.model.params());
        this.replayBuffer = new LinkedList<>();
        this.random = new Random();
    }

    // 构建 LSTM-DDQN 网络
    private MultiLayerNetwork buildNetwork() {
        NeuralNetConfiguration.ListBuilder builder = new NeuralNetConfiguration.Builder()
                .updater(new Adam(1e-3))
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .list();

        // LSTM 层
        builder.layer(new LSTM.Builder()
                .nIn(NUM_HOSTS)
                .nOut(128)
                .activation(Activation.TANH)
                .build());

        // 全连接层
        builder.layer(new DenseLayer.Builder()
                .nIn(128)
                .nOut(128)
                .activation(Activation.RELU)
                .build());

        // 输出层 (Q 值)
        builder.layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .nIn(128)
                .nOut(ACTIONS)
                .activation(Activation.IDENTITY)
                .build());

        MultiLayerNetwork net = new MultiLayerNetwork(builder.build());
        net.init();
        return net;
    }

    public int selectAction(double[][] state) {
        if (random.nextDouble() < EPSILON) {
            return random.nextInt(ACTIONS); // 随机探索
        }

        INDArray input = Nd4j.create(state);

        // 确保输入形状符合 LSTM 预期 [1, 300, 10]
        input = input.reshape(1, NUM_HOSTS, HISTORY_LENGTH);
        System.out.println("Input Shape (before reshape): " + Arrays.toString(input.shape()));

        // 获取模型的 Q 值预测
        INDArray qValues = model.output(input);

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


    // 训练网络
    public void train() {
        if (replayBuffer.size() < BATCH_SIZE) return;

        List<double[][]> batch = new ArrayList<>();
        for (int i = 0; i < BATCH_SIZE; i++) {
            batch.add(replayBuffer.poll());
        }

        double[][] states = new double[BATCH_SIZE][HISTORY_LENGTH * NUM_HOSTS];
        double[][] nextStates = new double[BATCH_SIZE][HISTORY_LENGTH * NUM_HOSTS];
        int[] actions = new int[BATCH_SIZE];
        double[] rewards = new double[BATCH_SIZE];
        boolean[] dones = new boolean[BATCH_SIZE];

        for (int i = 0; i < BATCH_SIZE; i++) {
            double[][] sample = batch.get(i);
            states[i] = sample[0];
            actions[i] = (int) sample[1][0];
            rewards[i] = sample[1][1];
            nextStates[i] = sample[2];
            dones[i] = sample[1][2] == 1.0;
        }

        INDArray stateArray = Nd4j.create(states);
        INDArray nextStateArray = Nd4j.create(nextStates);
        INDArray qValues = model.output(stateArray);
        INDArray nextQValues = targetModel.output(nextStateArray);

        INDArray maxNextQValues = nextQValues.max(1);
        INDArray targetQValues = qValues.dup();

        for (int i = 0; i < BATCH_SIZE; i++) {
            double target = rewards[i] + (dones[i] ? 0 : GAMMA * maxNextQValues.getDouble(i));
            targetQValues.putScalar(new int[]{i, actions[i]}, target);
        }

        model.fit(stateArray, targetQValues);

        // 每 TARGET_UPDATE 轮更新目标网络
        if (random.nextInt(100) < TARGET_UPDATE) {
            targetModel.setParams(model.params());
        }
    }

    // 生成 CPU 负载数据
    private static double[] generateCPUUsage() {
        double[] cpuUsage = new double[NUM_HOSTS];
        for (int i = 0; i < NUM_HOSTS; i++) {
            cpuUsage[i] = 10 + Math.random() * 90; // 10% - 100%
        }
        return cpuUsage;
    }

    // 训练 LSTM-DDQN 进行任务分配
    public void trainAllocation(int episodes) {
        Deque<double[]> history = new LinkedList<>();

        for (int episode = 0; episode < episodes; episode++) {
            double[] state = generateCPUUsage();
            history.add(state);

            if (history.size() < HISTORY_LENGTH) continue;

            double[][] lstmState = history.toArray(new double[0][0]);
            int action = selectAction(lstmState);
            double reward = state[action] < 50 ? 1 : (state[action] > 80 ? -1 : 0);

            double[] nextState = generateCPUUsage();
            history.add(nextState);
            double[][] lstmNextState = history.toArray(new double[0][0]);

            replayBuffer.add(new double[][]{
                    lstmState[0],  // 存储状态
                    new double[]{action, reward, state[action] > 80 ? 1.0 : 0.0}, // 存储动作、奖励、是否终止
            });

            train();

            if (episode % 100 == 0) {
                System.out.println("Episode " + episode + ", Selected Host: " + action + ", CPU Usage: " + state[action] + "%");
            }
        }
    }

    public static void main(String[] args) {
        DDQNLSTMAllocation allocator = new DDQNLSTMAllocation();
        allocator.trainAllocation(1000);
    }
}
