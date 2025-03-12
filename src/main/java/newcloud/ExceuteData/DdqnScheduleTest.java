package newcloud.ExceuteData;

import newcloud.*;
import newcloud.datacenter.PowerDatacenterDDQN;
import newcloud.policy.VmAllocationAssignerDDQN;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabHelper;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static newcloud.Constants.*;

public class DdqnScheduleTest {

    private static List<Cloudlet> cloudletList;
    public static List<Vm> vmList;
    private static List<PowerHost> hostList;
    private static DatacenterBroker broker;
    public static int brokerId;
    private static VmAllocationAssignerDDQN vmAllocationAssignerDDQN;
    private static double smallestdata = Double.MAX_VALUE;

    public List<Double> execute() throws Exception {
        for (int i = 0; i < Iteration; i++) {
            double epsilon = 1.0 / (i + 1); // 探索率逐渐降低
            vmAllocationAssignerDDQN = new VmAllocationAssignerDDQN(epsilon,TIME_STEPS); // 使用 DDQN
            CloudSim.init(1, Calendar.getInstance(), false);

            broker = createBroker();
            brokerId = broker.getId();
            System.out.println("Using dataset path: " + inputFolder);
            cloudletList = PlanetLabHelper.createCloudletListPlanetLab(brokerId, inputFolder);
            vmList = newHelper.createVmList(brokerId, cloudletList.size());
            hostList = newHelper.createHostList(Constants.NUMBER_OF_HOSTS);
            VmAllocationPolicy vmAllocationPolicy = new NewPowerAllocatePolicy(hostList);
            PowerDatacenterDDQN datacenter = createDatacenter(
                    "Datacenter",
                    PowerDatacenterDDQN.class,
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

        for (int i = 0; i < PowerDatacenterDDQN.allpower.size(); i++) {
            System.out.println(PowerDatacenterDDQN.allpower.get(i));
            if (PowerDatacenterDDQN.allpower.get(i) < smallestdata) {
                smallestdata = PowerDatacenterDDQN.allpower.get(i);
            }
        }
        System.out.printf("training steps:"+ vmAllocationAssignerDDQN.getTrainingSteps());
        System.out.println("最小能耗：" + smallestdata);
        return PowerDatacenterDDQN.allpower;
    }

    public PowerDatacenterDDQN createDatacenter(
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
        PowerDatacenterDDQN datacenter = new PowerDatacenterDDQN("DDQNLSTM", characteristics, vmAllocationPolicy, new LinkedList<Storage>(), 300, vmAllocationAssignerDDQN);
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