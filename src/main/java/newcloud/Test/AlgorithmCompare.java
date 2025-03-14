package newcloud.Test;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import FourDrawPlot.*;
import newcloud.ExceuteData.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;
import java.util.*;
import static newcloud.Constants.Iteration;


/**
 * Created by root on 8/25/17.
 */
public class AlgorithmCompare {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        MWNumericArray x = null; // 存放x值的数组
        MWNumericArray y1 = null; // 存放y值的数组
        MWNumericArray y2 = null; // 存放y值的数组
        MWNumericArray y3 = null; // 存放y值的数组
        MWNumericArray y4 = null; // 存放y值的数组
        MWNumericArray y5 = null; // 存放y值的数组


        Plotter thePlot = null; // plotter类的实例（在MatLab编译时，新建的类）
        int n = Iteration; // 作图点数
        int num = 1;
        try {
            // 分配x、y的值
            int[] dims = { 1, n };
            x = MWNumericArray.newInstance(dims, MWClassID.DOUBLE,
                    MWComplexity.REAL);
            y1 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE,
                    MWComplexity.REAL);
            y2 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE,
                    MWComplexity.REAL);
            y3 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE,
                    MWComplexity.REAL);
            y4 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE,
                    MWComplexity.REAL);
            y5 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE,
                    MWComplexity.REAL);

            DdqnlstmScheduleTest ddqnlstmScheduleTest = new DdqnlstmScheduleTest();
            Map<String, List<Double>> ddqnlstmListresults = ddqnlstmScheduleTest.execute();
            List<Double> ddqnlstmListpowerList = ddqnlstmListresults.get("allpower");
            List<Double> ddqnlstmListslavList = ddqnlstmListresults.get("allslav");
            List<Double> ddqnlstmListbalanceList = ddqnlstmListresults.get("allbalance");
            List<Double> ddqnlstmListrewardList = ddqnlstmListresults.get("allreward");
            System.out.println("ddqnlstmlistpoerlist.size(): " + ddqnlstmListpowerList.size());
            System.out.println("ddqnlstmlistslavlist.size(): " + ddqnlstmListslavList.size());
            System.out.println("ddqnlstmlistbalancelist.size(): " + ddqnlstmListbalanceList.size());
            System.out.println("ddqnlstmlistrewardlist.size(): " + ddqnlstmListrewardList.size());

            for (int i = 1; i <= ddqnlstmListrewardList.size(); i++) {
                x.set(i, i);
                y1.set(i, getExponentialSmoothing(ddqnlstmListrewardList).get(i - 1));
            }

            DdqnScheduleTest ddqnScheduleTest = new DdqnScheduleTest();
            Map<String, List<Double>> ddqnListresults = ddqnScheduleTest.execute();
            List<Double> ddqnListpowerList = ddqnListresults.get("allpower");
            List<Double> ddqnListslavList = ddqnListresults.get("allslav");
            List<Double> ddqnListbalanceList = ddqnListresults.get("allbalance");
            List<Double> ddqnListrewardList = ddqnListresults.get("allreward");

            System.out.println("ddqnlistpoerlist.size(): " + ddqnListpowerList.size());
            System.out.println("ddqnlistslavlist.size(): " + ddqnListslavList.size());
            System.out.println("ddqnlistbalancelist.size(): " + ddqnListbalanceList.size());
            System.out.println("ddqnlistrewardlist.size(): " + ddqnListrewardList.size());

            for (int i = 1; i <= ddqnListrewardList.size(); i++) {
                x.set(i, i);
                y2.set(i, getExponentialSmoothing(ddqnListrewardList).get(i - 1));
            }

            // 写入文件
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("allreward.txt"))) {
                for (int i = 0; i < ddqnListrewardList.size(); i++) {
                    writer.write(getExponentialSmoothing(ddqnListrewardList).get(i) + "\t" +  getExponentialSmoothing(ddqnlstmListrewardList).get(i)); // 使用Tab分隔
                    writer.newLine();
                }
            }
            System.out.println("数据已写入文件: allreward.txt");



//            LearningScheduleTest learningScheduleTest = new LearningScheduleTest();
//            Map<String, List<Double>> learningListresults = learningScheduleTest.execute();
//            List<Double> learningPowerList = learningListresults.get("allpower");
//            List<Double> learningSlavList = learningListresults.get("allslav");
//            List<Double> learningBalanceList = learningListresults.get("allbalance");
//            List<Double> learningRewardList = learningListresults.get("allreward");
//
//            System.out.println("learningpowerList.size(): " + learningPowerList.size());
//            System.out.println("learningslavList.size(): " + learningSlavList.size());
//            System.out.println("learningbalancelist.size(): " + learningBalanceList.size());
//            System.out.println("learningrewardlist.size(): " + learningRewardList.size());
//
//            for (int i = 1; i <= learningRewardList.size(); i++) {
//                x.set(i, i);
//                y3.set(i, (learningRewardList).get(i - 1));
//            }
//            System.out.println("ddqnlstmlistrewardList = " + ddqnlstmListrewardList);
//            System.out.println("ddqnlistrewardList = " + ddqnListrewardList);
//            System.out.println("learningRewardList = " + learningRewardList);
            //            GreedyScheduleTest greedyScheduleTest = new GreedyScheduleTest();
            //             Map<String, List<Double>> greedyListresults = greedyScheduleTest.execute();
            //                List<Double> greedyPowerList = greedyListresults.get("allpower");
            //                List<Double> greedySlavList = greedyListresults.get("allslav");
            //                List<Double> greedyBalanceList = greedyListresults.get("allbalance");
            //
            //            for (int i = 1; i <= greedyPowerList.size(); i++) {
            //                x.set(i, i);
            //                y4.set(i, greedyBalanceList.get(i - 1));
            //            }
            ////
            //            LearningAndInitScheduleTest fairScheduleTest = new LearningAndInitScheduleTest();
            //            Map<String, List<Double>> fairListresults = fairScheduleTest.execute();
            //            List<Double> fairPowerList = fairListresults.get("allpower");
            //            List<Double> fairSlavList = fairListresults.get("allslav");
            //            List<Double> fairBalanceList = fairListresults.get("allbalance");
            //            System.out.println("fairpowerList.size(): " + fairPowerList.size());
            //            System.out.println("fairslavList.size(): " + fairSlavList.size());
            //            System.out.println("fairbalancelist.size(): " + fairBalanceList.size());
            //
            //            for (int i = 1; i <= fairPowerList.size(); i++) {
            //                x.set(i, i);
            //                y5.set(i, fairBalanceList.get(i - 1));
            //            }

            // test using fack data
            //            List<Double> learningPowerList = new ArrayList<>();
            //            List<Double> lamdaPowerList = new ArrayList<>();
            //            List<Double> greedyPowerList = new ArrayList<>();
            //            List<Double> fairPowerList = new ArrayList<>();
            //            List<Double> ddqnlstmPowerList = new ArrayList<>();

            for (int i = 1; i <= Iteration; i++) {
                //                lamdaPowerList.add(1000.0 / i + 10);
                //                greedyPowerList.add(1000.0 / i + 20);
                //                fairPowerList.add(1000.0 / i + 30);
                //                learningPowerList.add(1000.0 / i);
                //                ddqnlstmPowerList.add(1000.0 / i + 40);
            }

            //            System.out.println(getAverage(learningPowerList));
            //            System.out.println(getAverage(ddqnlstmPowerListpowerList));
            //            System.out.println(getAverage(greedyPowerList));
            //            System.out.println(getAverage(fairPowerList));
            // 初始化plotter的对象
            thePlot = new Plotter();
            //
            //             System.out.println("greedypowerList.size(): " + greedyPowerList.size());
            //            System.out.println("greedyslavList.size(): " + greedySlavList.size());
            //            System.out.println("greedybalancelist.size(): " + greedyBalanceList.size());
            //
            //            System.out.println("ddqnlistpoerlist.size(): " + ddqnListpowerList.size());
            //            System.out.println("ddqnlistslavlist.size(): " + ddqnListslavList.size());
            //            System.out.println("ddqnlistbalancelist.size(): " + ddqnListbalanceList.size());
            //
            //            System.out.println("learningpowerList.size(): " + learningPowerList.size());
            //            System.out.println("learningslavList.size(): " + learningSlavList.size());
            //            System.out.println("learningbalancelist.size(): " + learningBalanceList.size());
            //
            //
            //            System.out.println("ddqnlstmListpowerList = " + ddqnlstmListpowerList);
            //            System.out.println("ddqnlistpowerList = " + ddqnListpowerList);
            //            System.out.println("learningPowerList = " + learningPowerList);
            //            System.out.println("greedyPowerList = " + greedyPowerList);
            //            System.out.println("fairPowerList = " + fairPowerList);
            //            System.out.println();
            //
            //            System.out.println("ddqnlstmListslavList = " + ddqnlstmListslavList);
            //            System.out.println("ddqnlistslavList = " + ddqnListslavList);
            //            System.out.println("learningSlavList = " + learningSlavList);
            //            System.out.println("greedySlavList = " + greedySlavList);
            //            System.out.println("fairSlavList = " + fairSlavList);
            //            System.out.println();
            //
            //
            //            System.out.println("ddqnlstmListBalanceList = " + ddqnlstmListbalanceList);
            //            System.out.println("ddqnlistBalanceList = " + ddqnListbalanceList);
            //            System.out.println("learningbalanceList = " + learningBalanceList);
            //            System.out.println("greedyBalanceList = " + greedyBalanceList);
            //            System.out.println("fairBalanceList = " + fairBalanceList);

            // 作图
            thePlot.drawplot(x, y1, "DDQN-LSTM", y2, "DDQN", y3, "Q-Learning", y4, "Greedy", "迭代次数", "Reward",
                    "各类算法奖励随迭代次数的变化");
            //            thePlot.drawplot(x, y4, "Q-Learning", y5, "DDQNLSTM", "迭代次数", "能耗", "各类算法随迭代次数的能耗变化");
            thePlot.waitForFigures();
        }
        catch (Exception e) {
            System.out.println("Exception: " + e.toString());
            } finally {
                // 释放本地资源
                MWArray.disposeArray(x);
                MWArray.disposeArray(y1);
                MWArray.disposeArray(y2);
                MWArray.disposeArray(y3);
                MWArray.disposeArray(y4);
//                MWArray.disposeArray(y5);
                if (thePlot != null)
                    thePlot.dispose();
            }
        }


    public static List<Double> getAverageResult(List<Double> datas, int step) {
        List<Double> smoothedData = new ArrayList<>();
        for (int i = 0; i <= datas.size() - step; i++) {
            double total = 0;
            for (int j = 0; j < step; j++) {
                total += datas.get(i + j);
            }
            smoothedData.add(total / step);
        }
        System.out.printf("smoothedData:%s\n", smoothedData);
        return smoothedData;
    }


    public static List<Double> getExponentialSmoothing(List<Double> datas) {
        List<Double> smoothedData = new ArrayList<>();
        double alpha = 0.4;
        if (datas.isEmpty()) return smoothedData;

        double prev = datas.get(0); // 初始化为第一个数据点
        smoothedData.add(prev);

        for (int i = 1; i < datas.size(); i++) {
            double smoothedValue = alpha * datas.get(i) + (1 - alpha) * prev;
            smoothedData.add(smoothedValue);
            prev = smoothedValue;
        }
//        System.out.printf("smoothedData:%s\n", smoothedData);
        return smoothedData;
    }

    public static double getAverage(List<Double> datas) {
        if (datas == null || datas.isEmpty()) {
            return 0.0; // 避免空列表异常
        }
        double sum = 0;
        for (double value : datas) {
            sum += value;
        }
        return sum / datas.size();
    }


}
