package org.wso2.carbon.sp.jobmanager.core.allocation;

import org.wso2.carbon.sp.jobmanager.core.model.ResourceNode;

import java.util.LinkedList;

/**
 * A class that represents a Knapsack.
 */
public class Knapsack {

    private double capacity;
    private double startcpuUsage;
    private ResourceNode resourceNode;
    private LinkedList<PartialSiddhiApp> partialSiddhiApps;

    /**
     * Constructor that creates a new knapsack with a capacity, resourceNode and a startcpuUsage latency.
     * @param capacity
     * @param resourceNode
     */
    public Knapsack(double capacity, ResourceNode resourceNode) {
        this.capacity = capacity;
        this.resourceNode = resourceNode;
        this.startcpuUsage = capacity;
        partialSiddhiApps = new LinkedList<>();
    }

    /**
     * Copy constructor which copies a knapsack object and creates a new identical one.
     * @param knapsack
     */
    public Knapsack(Knapsack knapsack) {
        this.capacity = knapsack.getcapacity();
        this.startcpuUsage = knapsack.getStartcpuUsage();
        this.resourceNode = knapsack.getresourceNode();
        this.partialSiddhiApps = new LinkedList<>(knapsack.getpartialSiddhiApps());
    }

    /**
     * Adds an item doubleo the item-list and updates the capacity so it's up to date.
     * @param item
     */
    public void addPartialSiddhiApps(PartialSiddhiApp item) {
        if(item != null) {
            partialSiddhiApps.add(item);
            capacity = capacity - item.getcpuUsage();
            System.out.println();
        }
    }

    /**
     * Stes the capacity to the initial latency of the knapsack.
     */
    public void resetcapacity() {
        capacity = startcpuUsage;
    }

    /**
     * Sets the capacity to the latency provided to the method.
     * @param capacity
     */
    public void setcapacity(double capacity) {
        this.capacity = capacity;
    }

    /**
     * Method that returns the knapsack's startcpuUsage
     * @return
     */
    public double getStartcpuUsage() {
        return startcpuUsage;
    }

    public  void  setStartcpuUsage(double startcpuUsage){
        this.startcpuUsage=startcpuUsage;
    }

    /**
     * Method that returns the knapsack's capacity.
     * @return
     */
    public double getcapacity() {
        return capacity;
    }

    /**
     * Method that returns the knapsack's resourceNode.
     * @return
     */
    public ResourceNode getresourceNode() {
        return resourceNode;
    }

    /**
     * Method that returns the partialSiddhiApps the knapsack is currently holding.
     * @return
     */
    public LinkedList<PartialSiddhiApp> getpartialSiddhiApps() {
        return partialSiddhiApps;
    }
}
