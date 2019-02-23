package org.wso2.carbon.sp.jobmanager.core.allocation;

import org.apache.log4j.Logger;
import org.wso2.carbon.sp.jobmanager.core.model.ResourceNode;

import java.util.*;

public class MultipleKnapsack {

    private static final Logger log = Logger.getLogger(MetricsBasedAllocationAlgorithm.class);
    private LinkedList<Knapsack> knapsacks;
    private LinkedList<PartialSiddhiApp> partialSiddhiApps;
    private double latency;
    public  Map<ResourceNode, List<PartialSiddhiApp>>output_map = new HashMap<>();

    /**
     * Method that gets all neighbors a current solution to the Multiple Knapsack Problem has.
     * @param knapsacks
     * @param partialSiddhiApps
     * @return
     */
    public LinkedList<MultipleKnapsack> getNeighbors(LinkedList<Knapsack> knapsacks, LinkedList<PartialSiddhiApp> partialSiddhiApps) {

        LinkedList<MultipleKnapsack> knapsackNeighbors = new LinkedList<>();

        for (int gKnapsack = 0; gKnapsack < knapsacks.size(); gKnapsack++) {
            for (int gItem = 0; gItem < knapsacks.get(gKnapsack).getpartialSiddhiApps().size(); gItem++) {
                for (int lKnapsack = 0; lKnapsack < knapsacks.size(); lKnapsack++) {
                    for (int lItem = 0; lItem < knapsacks.get(lKnapsack).getpartialSiddhiApps().size(); lItem++) {

                        Knapsack globalKnapsack = knapsacks.get(gKnapsack);
                        Knapsack localKnapsack = knapsacks.get(lKnapsack);

                        if(!globalKnapsack.equals(localKnapsack)) {
                            LinkedList<PartialSiddhiApp> globalpartialSiddhiApps = globalKnapsack.getpartialSiddhiApps();
                            LinkedList<PartialSiddhiApp> localpartialSiddhiApps = localKnapsack.getpartialSiddhiApps();
                            if(globalpartialSiddhiApps.get(gItem).getcpuUsage() <= localpartialSiddhiApps.get(lItem).getcpuUsage() + localKnapsack.getcapacity()) {

                                MultipleKnapsack neighbor = new MultipleKnapsack();
                                LinkedList<Knapsack> currentKnapsack = new LinkedList<>();
                                LinkedList<PartialSiddhiApp> currentpartialSiddhiApps = new LinkedList<>(partialSiddhiApps);
                                for(Knapsack knapsack : knapsacks) {
                                    if(knapsack.equals(localKnapsack)) {
                                        Knapsack local = new Knapsack(knapsack);
                                        local.setcapacity(localKnapsack.getcapacity() + localpartialSiddhiApps.get(lItem).getcpuUsage() - globalpartialSiddhiApps.get(gItem).getcpuUsage());
                                        local.getpartialSiddhiApps().set(lItem, globalpartialSiddhiApps.get(gItem));
                                        currentKnapsack.add(local);
                                    } else if(knapsack.equals(globalKnapsack)) {
                                        Knapsack global = new Knapsack(knapsack);
                                        global.setcapacity(global.getcapacity() + global.getpartialSiddhiApps().get(gItem).getcpuUsage());
                                        global.getpartialSiddhiApps().remove(gItem);
                                        currentKnapsack.add(global);
                                    } else {
                                        currentKnapsack.add(new Knapsack(knapsack));
                                    }
                                }

                                neighbor.setKnapsacks(currentKnapsack);
                                neighbor.setpartialSiddhiApps(currentpartialSiddhiApps);
                                neighbor.shufflepartialSiddhiAppsInKnapsacks();
                                neighbor.greedyMultipleKnapsack(currentpartialSiddhiApps);
                                neighbor.calculatelatency();
                                knapsackNeighbors.add(neighbor);
                            }
                        }
                    }
                }
            }
        }

        return knapsackNeighbors;
    }

    /**
     * Method that tries to find neighbors for a solution that have a better outcome than the solution itself.
     * @param knapsacks
     * @return
     */
    public MultipleKnapsack neighborSearch(MultipleKnapsack knapsacks) {
        LinkedList<MultipleKnapsack> neighbors = getNeighbors(knapsacks.getKnapsacks(), knapsacks.getpartialSiddhiApps());
        for (MultipleKnapsack neighbor : neighbors) {
            if(neighbor.getlatency() > knapsacks.getlatency()) {
                knapsacks = neighborSearch(neighbor);
            }
        }

        return knapsacks;
    }

    /**
     * Method that shuffles or packs the partialSiddhiApps so that there's space for other partialSiddhiApps to be added.
     */
    public void shufflepartialSiddhiAppsInKnapsacks() {
        LinkedList<PartialSiddhiApp> partialSiddhiAppsInKnapsacks = new LinkedList<>();
        for (Knapsack knapsack : knapsacks) {
            partialSiddhiAppsInKnapsacks.addAll(knapsack.getpartialSiddhiApps());
        }

        Collections.sort(partialSiddhiAppsInKnapsacks, new Comparator<PartialSiddhiApp>() {
            @Override
            public int compare(PartialSiddhiApp i1, PartialSiddhiApp i2) {
                if(i1.getcpuUsage() > i2.getcpuUsage()) {
                    return -1;
                } else if (i2.getcpuUsage() > i1.getcpuUsage()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        for(Knapsack knapsack : knapsacks) {
            knapsack.getpartialSiddhiApps().clear();
            knapsack.resetcapacity();
            for(Iterator<PartialSiddhiApp> it = partialSiddhiAppsInKnapsacks.iterator(); it.hasNext(); ) {
                PartialSiddhiApp item = it.next();
                if(item.getcpuUsage() <= knapsack.getcapacity()) {
                    knapsack.addPartialSiddhiApps(item);
                    it.remove();
                }
            }
        }
    }

    /**
     * Method that solves the Multiple Knapsack Problem by a greedy approach.
     * @param partialSiddhiApps
     */
    public void greedyMultipleKnapsack(LinkedList<PartialSiddhiApp> partialSiddhiApps) {

        Collections.sort(partialSiddhiApps, new Comparator<PartialSiddhiApp>() {
            @Override
            public int compare(PartialSiddhiApp i1, PartialSiddhiApp i2) {
                if(i1.getlatencyBycpuUsage() > i2.getlatencyBycpuUsage()) {
                    return -1;
                } else if (i2.getlatencyBycpuUsage() > i2.getlatencyBycpuUsage()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        Knapsack bestKnapsack;
        double bestcpuUsageDifference;
        double currentcpuUsageDifference;

        for (int i = 0; i < partialSiddhiApps.size(); i++) {
            if(!this.partialSiddhiApps.contains(partialSiddhiApps.get(i))) {
                this.partialSiddhiApps.add(partialSiddhiApps.get(i));
            }
            bestcpuUsageDifference = Integer.MAX_VALUE;
            bestKnapsack = null;
            for (int j = 0; j < knapsacks.size(); j++) {
                if(knapsacks.get(j).getcapacity() >= partialSiddhiApps.get(i).getcpuUsage()) {
                    currentcpuUsageDifference = knapsacks.get(j).getcapacity() - partialSiddhiApps.get(i).getcpuUsage();
                    if(currentcpuUsageDifference < bestcpuUsageDifference && currentcpuUsageDifference > 0) {
                        bestcpuUsageDifference = currentcpuUsageDifference;
                        bestKnapsack = knapsacks.get(j);
                    }
                }
            }
            if(bestKnapsack != null) {
                bestKnapsack.addPartialSiddhiApps(partialSiddhiApps.get(i));
                this.partialSiddhiApps.remove(partialSiddhiApps.get(i));
            }
        }
    }

    /**
     * Method that calculates a MultipleKnapsack's total latency.
     */
    public void calculatelatency() {
        double latency = 0;

        for(Knapsack knapsack : knapsacks) {
            for (PartialSiddhiApp item : knapsack.getpartialSiddhiApps()) {
                latency += item.getlatency();
            }
        }

        this.latency = latency;
    }

    /**
     * Method that prints out the result of a MultipleKnapsack.
     */
    public LinkedList<PartialSiddhiApp>  printResult(boolean flag) {
        for (Knapsack knapsack : knapsacks) {
                  ;
            log.info("\nResourceNode\n" + "resourceNode: " + knapsack.getresourceNode()
                    + "\nTotal Usable CPU : " + knapsack.getStartcpuUsage() +
                    "\nUsed CPU in this iteration: " + (knapsack.getStartcpuUsage() - knapsack.getcapacity()) +
                    "\nRemaining CPU : " + knapsack.getcapacity() + "\n");

            knapsack.setStartcpuUsage(knapsack.getcapacity());

            log.info("Initial CPU Usage of " + knapsack.getresourceNode() + " is set to " + knapsack.getStartcpuUsage()+ "\n");
            try {
                for (PartialSiddhiApp item : knapsack.getpartialSiddhiApps()) {
                    log.info("\n\nPartial siddhi app\n" + "Name: " + item.getName()
                            + "\nLatency : " + item.getlatency() + "\nCPU Usage : " + item.getcpuUsage());
                    partialSiddhiApps.remove(item);
                    log.info("removing " + item.getName() + " from partialsiddhiapps list");
                    log.info("\n");
                }
//                if (output_map.containsKey(knapsack.getresourceNode())){
//                    for (PartialSiddhiApp partialSiddhiApps : temp ){
//                        output_map.get(knapsack.getresourceNode()).add(partialSiddhiApps);
//                        log.info("Updating " + knapsack.getresourceNode() + " with " +
//                                partialSiddhiApps.getName());
//                        log.info("Size of outmap " + output_map.size());
//                    }
//                } else {
//                    output_map.put(knapsack.getresourceNode() , temp);
//                    log.info("Adding to" + knapsack.getresourceNode());
//                    log.info("Size of outmap " + output_map.size());
//                }

            } catch (ConcurrentModificationException e) {
                e.printStackTrace();
            }
            log.info("---------------------------\n");
        }

        log.info("Total latency: " + latency);
        return partialSiddhiApps;
    }


    public Map<ResourceNode, List<PartialSiddhiApp>> updatemap(Map<ResourceNode, List<PartialSiddhiApp>> map){
        for (Knapsack knapsack : knapsacks) {
            List<PartialSiddhiApp>temp = new LinkedList<>();
            for (PartialSiddhiApp item : knapsack.getpartialSiddhiApps()) {
                    temp.add(item);
                    //log.info("Adding " + item.getName() + " to " + knapsack.getresourceNode());
                }
                if (map.containsKey(knapsack.getresourceNode())){
                    for (PartialSiddhiApp partialSiddhiApps : temp ){
                        map.get(knapsack.getresourceNode()).add(partialSiddhiApps);
                        log.info("Updating " + knapsack.getresourceNode().getId() + " with " +
                                partialSiddhiApps.getName());
                    }
                } else {
                    map.put(knapsack.getresourceNode() , temp);
                      log.info("Adding to" + knapsack.getresourceNode().getId());
                }
        }

        return  map;
    }


    public ResourceNode getResourceNode(String partialSiddhiAppName){
        for (Knapsack knapsack : knapsacks) {
            log.info("checking" + knapsack.getresourceNode().toString());
            for (PartialSiddhiApp item : knapsack.getpartialSiddhiApps()) {
                if (item.getName() == partialSiddhiAppName){
                    log.info("deploying node is " + knapsack.getresourceNode().toString());
                    return knapsack.getresourceNode();

                }
            }
        }
        return null;
    }

    /**
     * Method that sets the partialSiddhiApps that are not in a knapsack already.
     * @param partialSiddhiApps
     */
    public void setpartialSiddhiApps(LinkedList<PartialSiddhiApp> partialSiddhiApps) {
        this.partialSiddhiApps = partialSiddhiApps;
    }

    /**
     * Method that sets all of the knapsacks.
     * @param knapsacks
     */
    public void setKnapsacks(LinkedList<Knapsack> knapsacks) {
        this.knapsacks = knapsacks;
    }

    /**
     * Method that gets the total latency of a MultipleKnapsack.
     * @return
     */
    public double getlatency() {
        return latency;
    }

    /**
     * Constructor that instantiates necessary objects.
     */
    public MultipleKnapsack() {
        knapsacks = new LinkedList<>();
        partialSiddhiApps = new LinkedList<>();
    }

    /**
     * Method that gets all of the knapsacks in the MultipleKnapsack.
     * @return
     */
    public LinkedList<Knapsack> getKnapsacks() {
        return knapsacks;
    }

    /**
     * Method that gets all of the partialSiddhiApps that are not in a knapsack already.
     * @return
     */
    public LinkedList<PartialSiddhiApp> getpartialSiddhiApps() {
        return partialSiddhiApps;
    }

    /**
     * Method that adds a knapsack into the MultipleKnapsack.
     * @param knapsack
     */
    public void addKnapsack(Knapsack knapsack) {
        knapsacks.add(knapsack);
    }

    public  void modifyKnapsack(int index , double capacityacity , String resourceNode){
        //knapsacks.set(index,new Knapsack(capacityacity, resourceNode));
    }

    public Map<ResourceNode, List<PartialSiddhiApp>> getMap(){
        return output_map;
    }


}
