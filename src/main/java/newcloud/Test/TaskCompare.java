package newcloud.Test;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import FourDrawPlot.Plotter;
import newcloud.ExceuteData.GreedyScheduleTest;
import newcloud.ExceuteData.LearningAndInitScheduleTest;
import newcloud.ExceuteData.LearningLamdaScheduleTest;
import newcloud.ExceuteData.LearningScheduleTest;
import newcloud.ExceuteData.DdqnlstmScheduleTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static newcloud.Constants.Iteration;
import static newcloud.Constants.inputFolder;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TaskCompare {
    public static void main(String[] args) {
        String ss = "C:\\Users\\admin\\Desktop\\parttimeJOB\\RL_cloudsim\\CloudPowerDeployment-master\\CloudPowerDeployment-master\\src\\main\\resources\\datas\\";
        String[] folders = new String[]{"50", "100", "150", "200", "250", "300"};
        List<Double> a1 = new ArrayList<>();
        List<Double> a2 = new ArrayList<>();
        List<Double> a3 = new ArrayList<>();
        List<Double> a4 = new ArrayList<>();

        List<Double> slav1 = new ArrayList<>();
        List<Double> slav2 = new ArrayList<>();
        List<Double> slav3 = new ArrayList<>();
        List<Double> slav4 = new ArrayList<>();

        List<Double> balance1 = new ArrayList<>();
        List<Double> balance2 = new ArrayList<>();
        List<Double> balance3 = new ArrayList<>();
        List<Double> balance4 = new ArrayList<>();

        try {
            FileWriter powerWriter = new FileWriter("allpower.txt");
            FileWriter slavWriter = new FileWriter("allslav.txt");
            FileWriter balanceWriter = new FileWriter("allbalance.txt");

            for (String file : folders) {
                inputFolder = ss + file;

                LearningScheduleTest learningScheduleTest = new LearningScheduleTest();
                Map<String, List<Double>> learningResults = learningScheduleTest.execute();

                DdqnlstmScheduleTest ddqnlstmScheduleTest = new DdqnlstmScheduleTest();
                Map<String, List<Double>> ddqnResults = ddqnlstmScheduleTest.execute();

                GreedyScheduleTest greedyScheduleTest = new GreedyScheduleTest();
                Map<String, List<Double>> greedyResults = greedyScheduleTest.execute();

                LearningAndInitScheduleTest psoScheduleTest = new LearningAndInitScheduleTest();
                Map<String, List<Double>> psoResults = psoScheduleTest.execute();

                // 计算 allpower、allslav、allbalance 的平均值
                double avgLearningPower = getAverage(learningResults.get("allpower"));
                double avgDdqnPower = getAverage(ddqnResults.get("allpower"));
                double avgGreedyPower = getAverage(greedyResults.get("allpower"));
                double avgPsoPower = getAverage(psoResults.get("allpower"));

                double avgLearningSlav = getAverage(learningResults.get("allslav"));
                double avgDdqnSlav = getAverage(ddqnResults.get("allslav"));
                double avgGreedySlav = getAverage(greedyResults.get("allslav"));
                double avgPsoSlav = getAverage(psoResults.get("allslav"));

                double avgLearningBalance = getAverage(learningResults.get("allbalance"));
                double avgDdqnBalance = getAverage(ddqnResults.get("allbalance"));
                double avgGreedyBalance = getAverage(greedyResults.get("allbalance"));
                double avgPsoBalance = getAverage(psoResults.get("allbalance"));

                // 保存到 List
                a1.add(avgLearningPower);
                a2.add(avgDdqnPower);
                a3.add(avgGreedyPower);
                a4.add(avgPsoPower);

                slav1.add(avgLearningSlav);
                slav2.add(avgDdqnSlav);
                slav3.add(avgGreedySlav);
                slav4.add(avgPsoSlav);

                balance1.add(avgLearningBalance);
                balance2.add(avgDdqnBalance);
                balance3.add(avgGreedyBalance);
                balance4.add(avgPsoBalance);

                // 写入文件
                powerWriter.write(String.format("%s %.4f %.4f %.4f %.4f%n", file, avgLearningPower, avgDdqnPower, avgGreedyPower, avgPsoPower));
                slavWriter.write(String.format("%s %.4f %.4f %.4f %.4f%n", file, avgLearningSlav, avgDdqnSlav, avgGreedySlav, avgPsoSlav));
                balanceWriter.write(String.format("%s %.4f %.4f %.4f %.4f%n", file, avgLearningBalance, avgDdqnBalance, avgGreedyBalance, avgPsoBalance));
            }

            // 关闭文件
            powerWriter.close();
            slavWriter.close();
            balanceWriter.close();

            System.out.println("数据已保存至 allpower.txt, allslav.txt, allbalance.txt");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static double getAverage(List<Double> datas) {
        if (datas == null || datas.isEmpty()) {
            return 0.0; // 避免空列表异常
        }
        return datas.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}
