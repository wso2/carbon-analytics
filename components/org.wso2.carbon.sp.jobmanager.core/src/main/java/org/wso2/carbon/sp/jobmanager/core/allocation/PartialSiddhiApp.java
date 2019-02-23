package org.wso2.carbon.sp.jobmanager.core.allocation;

public class PartialSiddhiApp {

    private double cpuUsage;
    private double latency;
    private double latencyBycpuUsage;
    private String name;

    /**
     * Constructor that instantiates cpuUsage, latency and name for an item.
     * @param cpuUsage
     * @param latency
     * @param name
     */
    public PartialSiddhiApp (double cpuUsage, double latency, String name) {
        this.cpuUsage = cpuUsage;
        this.latency = latency;
        latencyBycpuUsage = (double) latency / (double) cpuUsage;
        this.name = name;
    }

    /**
     * Method that gets the latency / cpuUsage latency from an item.
     * @return
     */
    public double getlatencyBycpuUsage() {
        return latencyBycpuUsage;
    }

    /**
     * Method that returns the cpuUsage an item has.
     * @return
     */
    public double getcpuUsage() {
        return cpuUsage;
    }

    /**
     * Method that gets the latency an item has.
     * @return
     */
    public double getlatency() {
        return latency;
    }

    /**
     * Method that sets the name of an item.
     * @return
     */
    public String getName() {
        return name;
    }
}
