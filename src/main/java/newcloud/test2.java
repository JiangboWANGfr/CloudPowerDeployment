package newcloud;

import newcloud.ExceuteData.DdqnScheduleTest;

import java.util.List;
import java.util.Map;

import static newcloud.Constants.Iteration;

public class test2 {
    public static void main(String[] args) throws Exception {
        double total = 0;
//        RandomScheduleTest randomScheduleTest = new RandomScheduleTest();
//        List<Double> learningPowerList = randomScheduleTest.execute();
//        FairScheduleTest fairScheduleTest = new FairScheduleTest();
//        List<Double> learningPeriodList = fairScheduleTest.execute();
//        LearningScheduleTest learningScheduleTest = new LearningScheduleTest();
//        List<Double> learningPowerList = learningScheduleTest.execute();
//        DDQNLSTMScheduleTest1 ddqnlstmscheduletest = new DDQNLSTMScheduleTest1();
//         List<Double> learningPowerList = ddqnlstmscheduletest.execute();
        DdqnScheduleTest test = new DdqnScheduleTest();
//        List<Double>  learningPowerlist = test.execute();
//        for (int i = 0; i < learningPowerList.size(); i++) {
//            total += learningPowerList.get(i);
//        }
        System.out.println(total / Iteration);
    }
}
