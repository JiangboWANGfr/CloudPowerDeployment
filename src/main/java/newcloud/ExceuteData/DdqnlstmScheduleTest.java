package newcloud.ExceuteData;
import java.util.Collections;
import newcloud.*;
import newcloud.datacenter.PowerDatacenterDDQNLSTM;
import newcloud.policy.VmAllocationAssignerDDQNLSTM;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabHelper;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.*;

import static newcloud.Constants.*;

public class DdqnlstmScheduleTest {

    private static List<Cloudlet> cloudletList;
    public static List<Vm> vmList;
    private static List<PowerHost> hostList;
    private static DatacenterBroker broker;
    public static int brokerId;
    private static VmAllocationAssignerDDQNLSTM vmAllocationAssignerDDQNLSTM;
    private static double smallestdata = Double.MAX_VALUE;
    

    public Map<String, List<Double>> execute() throws Exception {

        for (int i = 0; i < Iteration; i++) {
            double epsilon = 1.0 / (i + 1); // 探索率逐渐降低
            CloudSim.init(1, Calendar.getInstance(), false);
            vmAllocationAssignerDDQNLSTM = new VmAllocationAssignerDDQNLSTM(epsilon,TIME_STEPS); // 使用 DDQN

            broker = createBroker();
            brokerId = broker.getId();
            System.out.println("Using dataset path: " + inputFolder);
            cloudletList = PlanetLabHelper.createCloudletListPlanetLab(brokerId, inputFolder);
            vmList = newHelper.createVmList(brokerId, cloudletList.size());
            hostList = newHelper.createHostList(Constants.NUMBER_OF_HOSTS);
            VmAllocationPolicy vmAllocationPolicy = new NewPowerAllocatePolicy(hostList);
            PowerDatacenterDDQNLSTM datacenter = createDatacenter(
                    "Datacenter",
                    PowerDatacenterDDQNLSTM.class,
                    hostList,
                    vmAllocationPolicy);

            datacenter.setDisableMigrations(false);

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);
            CloudSim.terminateSimulation(terminateTime);

            double lastClock = CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();
            System.out.println(i + "----------------------------------");
        }
//        return PowerDatacenterDDQNLSTM.allpower;
//         return PowerDatacenterDDQNLSTM.allslav;
//        return PowerDatacenterDDQNLSTM.allbalance;
//        return PowerDatacenterDDQNLSTM.allepochreward;
        // 构建 Map 返回所有列表
        Map<String, List<Double>> resultMap = new HashMap<>();
        resultMap.put("allpower", PowerDatacenterDDQNLSTM.allpower);
        resultMap.put("allslav", PowerDatacenterDDQNLSTM.highMipsRatioHistory);
        resultMap.put("allbalance", PowerDatacenterDDQNLSTM.allbalance);
        List<Double> reversedList = new ArrayList<>(PowerDatacenterDDQNLSTM.allepochreward);
        Collections.reverse(reversedList);
        resultMap.put("allreward", reversedList);

        return resultMap;

    }

    public PowerDatacenterDDQNLSTM createDatacenter(
            String name,
            Class<? extends Datacenter> datacenterClass,
            List<PowerHost> hostList,
            VmAllocationPolicy vmAllocationPolicy) throws Exception {
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, 
                os, 
                vmm, 
                hostList, 
                time_zone, 
                cost, 
                costPerMem, 
                costPerStorage, 
                costPerBw);
        PowerDatacenterDDQNLSTM datacenter = new PowerDatacenterDDQNLSTM("DDQNLSTM", characteristics, vmAllocationPolicy, new LinkedList<Storage>(), 300, vmAllocationAssignerDDQNLSTM);
        return datacenter;
    }

    public DatacenterBroker createBroker() {
        NewPowerDatacenterBroker broker = null;
        try {
            broker = new NewPowerDatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return broker;
    }
}
