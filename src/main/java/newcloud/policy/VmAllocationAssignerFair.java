package newcloud.policy;

import newcloud.GenExcel;
import org.cloudbus.cloudsim.Host;

import java.util.List;


public class VmAllocationAssignerFair { // 公平分配策略
    private GenExcel genExcel = null;

    public VmAllocationAssignerFair(GenExcel genExcel) {
        this.genExcel = genExcel;
        this.genExcel.init();
    }

    public Host getVmAllcaotionHost(List<Host> hostList) {
        double availableMips = Double.MIN_VALUE;
        Host targetHost = null;
        // print hostlist
        System.out.printf("Host List Size: %d\n", hostList.size());
        System.out.printf("Host list",hostList);
        for (Host host : hostList) {
            double i = host.getAvailableMips();
            if (i >= availableMips) {
                availableMips = i;
                targetHost = host;
            }
        }
        return targetHost;
    }
}
