/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.business.rules.core.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.datasource.configreader.ConfigReader;
import org.wso2.carbon.business.rules.core.deployer.SiddhiAppApiHelper;
import org.wso2.carbon.business.rules.core.exceptions.SiddhiAppManagerApiException;
import org.wso2.carbon.business.rules.core.exceptions.SiddhiAppsApiHelperException;
import org.wso2.carbon.business.rules.core.manager.util.SiddhiManagerHelper;
import org.wso2.carbon.business.rules.core.manager.util.UpdateSiddhiAppCountScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Response;

/****
 * This deployer will deploy siddhi app in load balancing manner. So the provided siddhi app will be deployed in the
 * node which has the lowest number of siddhi apps
 */
public class LoadBalancingDeployer implements SiddhiAppDeployer {
    private static final Logger log = LoggerFactory.getLogger(LoadBalancingDeployer.class);
    private Map<String, Long> siddhiAppsCountMap;
    private Map nodes = null;
    private SiddhiAppApiHelper siddhiAppApiHelper;


    @Override
    public void init(ConfigReader configReader, SiddhiAppApiHelper siddhiAppApiHelper) {
        this.siddhiAppApiHelper = siddhiAppApiHelper;
        this.nodes = configReader.getNodes();
        this.siddhiAppsCountMap = genarateSiddhiAppsCountMap();
        Timer timer = new Timer();
        timer.schedule(new UpdateSiddhiAppCountScheduler(), TimeUnit.MINUTES.toMillis(15),
                TimeUnit.MINUTES.toMillis(15));

    }

    @Override
    public List<String> deploySiddhiApp(Object siddhiApp)
            throws SiddhiAppManagerApiException {
        String siddhiAppName;
        List<String> nodeList = new ArrayList<>();

        siddhiAppName = SiddhiManagerHelper.getSiddhiAppName(siddhiApp);
        for (Object node : nodes.keySet()) {
            boolean isSiddhiAppAvailable = false;
            try {
                isSiddhiAppAvailable = siddhiAppApiHelper.getSiddhiAppAvailability(node.toString(),
                        siddhiAppName);
            } catch (SiddhiAppsApiHelperException e) {
                log.error("Error occured while retrieving siddhi app availability from node " + node.toString() +
                        " for siddhi app '" + siddhiAppName + "' :", e);
            }
            if (isSiddhiAppAvailable) {
                log.error("Failed to deploy the siddhi app. Siddhi app already exists in node " +
                        node.toString() + " ");
                throw new SiddhiAppManagerApiException("Failed to deploy the siddhi app. Siddhi app already exists in " +
                        "node " + node.toString() + ". ", Response.Status.CONFLICT);
            }
        }
        Map.Entry<String, Long> deploybleNode = siddhiAppsCountMap.entrySet().stream().
                min(Map.Entry.comparingByValue()).get();
        nodeList.add(deploybleNode.getKey());
        return nodeList;
    }

    @Override
    public List<String> updateSiddhiApp(Object siddhiApp) throws SiddhiAppManagerApiException {
        List<String> nodeList = new ArrayList<>();
        boolean isSiddhiAppAvailable = false;
        String siddhiAppName;

        siddhiAppName = SiddhiManagerHelper.getSiddhiAppName(siddhiApp);
        for (Object node : nodes.keySet()) {
            try {
                isSiddhiAppAvailable = siddhiAppApiHelper.getSiddhiAppAvailability(node.toString(),
                        siddhiAppName);
            } catch (SiddhiAppsApiHelperException e) {
                log.error("Error occured while checking the siddhi app availability from node " + node.toString() + "" +
                        "for siddhi app '" + siddhiAppName + "' :", e);
            }
            if (isSiddhiAppAvailable) {
                nodeList.add(node.toString());
                return nodeList;
            }
        }
        throw new SiddhiAppManagerApiException("Siddhi App '" + siddhiAppName + "' does not exists",
                Response.Status.NOT_FOUND);
    }

    @Override
    public List<String> deleteSiddhiApp(String siddhiAppName)
            throws SiddhiAppManagerApiException {
        List<String> nodeList = new ArrayList<>();
        boolean issiddhiAppAvailable = false;
        for (Object node : nodes.keySet()) {
            try {
                issiddhiAppAvailable = siddhiAppApiHelper.getSiddhiAppAvailability(node.toString(),
                        siddhiAppName);
            } catch (SiddhiAppsApiHelperException e) {
                log.error("Error Occurred while checking the availability of siddhi app '" + siddhiAppName + "'" +
                        " in node " + node.toString(), e);
            }
            if (issiddhiAppAvailable) {
                nodeList.add(node.toString());
                return nodeList;
            }
        }
        throw new SiddhiAppManagerApiException("Siddhi App '" + siddhiAppName + "' does not exists",
                Response.Status.NOT_FOUND);
    }

    @Override
    public Response reShuffle() {
        long totalSiddhiAppCount = 0;
        Map<String, Long> siddhiAppCountsMap = new HashMap<>();
        Map<String, Long> siddhiAppCountToBeRemovedFromEachNode = new HashMap();
        Map<String, String> siddhiAppNamesToBeRemovedFromEachNode = new HashMap<>();
        for (Object node : nodes.keySet()) {
            try {
                long appCount = siddhiAppApiHelper.getSiddhiAppCount(node.toString());
                siddhiAppCountsMap.put(node.toString(), appCount);
            } catch (SiddhiAppsApiHelperException e) {
                log.error("Error occured while retrieving the siddhi app count from node " + node.toString(), e);
                return Response.serverError().entity("Error occured while connecting into node " + node.toString() +
                        ". aborting reshuffle process").build();
            }
        }

        log.info("siddhi app count of each nodes before shuffle, " + siddhiAppCountsMap.toString());
        for (Map.Entry<String, Long> siddhiAppCount : siddhiAppCountsMap.entrySet()) {
            totalSiddhiAppCount = totalSiddhiAppCount + siddhiAppCount.getValue();
        }
        long avgSiddhiAppCountForEachNode = totalSiddhiAppCount / nodes.size();
        log.info("Calculated average siddhi app count for each node " + avgSiddhiAppCountForEachNode);
        for (Map.Entry<String, Long> siddhiAppCountForTheNode : siddhiAppCountsMap.entrySet()) {
            if (siddhiAppCountForTheNode.getValue() - avgSiddhiAppCountForEachNode > 0) {
                siddhiAppCountToBeRemovedFromEachNode.put(siddhiAppCountForTheNode.getKey(),
                        siddhiAppCountForTheNode.getValue() - avgSiddhiAppCountForEachNode);
            }
        }
        log.info("Number of siddhi apps to be removed from each node " +
                siddhiAppCountToBeRemovedFromEachNode.toString());
        for (Map.Entry<String, Long> node : siddhiAppCountToBeRemovedFromEachNode.entrySet()) {
            List<String> siddhiAppList;
            try {
                siddhiAppList = siddhiAppApiHelper.getSiddhiAppList(node.getKey());
            } catch (SiddhiAppsApiHelperException e) {
                log.error("Error occured while retrieving the siddhi app list from node  " + node.getKey(), e);
                return Response.serverError().entity("Error occured while connecting into node " + node.getKey() +
                        ". aborting reshuffle process").build();
            }

            for (int i = 0; i < node.getValue(); i++) {
                siddhiAppNamesToBeRemovedFromEachNode.put(siddhiAppList.get(i), node.getKey());
            }
        }
        log.info("Siddhi apps to be removed from each node " + siddhiAppNamesToBeRemovedFromEachNode.toString());
        for (Map.Entry<String, String> siddhiAppDetails : siddhiAppNamesToBeRemovedFromEachNode.entrySet()) {
            Map.Entry<String, Long> deploybleNode = siddhiAppCountsMap.entrySet().stream().
                    min(Map.Entry.comparingByValue()).get();
            String nodeUrl = siddhiAppDetails.getValue();
            log.info("Siddhi app '" + siddhiAppDetails.getKey() + "' will be removed from " + nodeUrl +
                    " and deployed into " + deploybleNode.getKey() + " node");
            try {
                String siddhiApp = siddhiAppApiHelper.getSiddhiApp(nodeUrl, siddhiAppDetails.getKey());
                log.info("Retrieved the Siddhi App '" + siddhiAppDetails.getKey() + "' from the node " + nodeUrl);
                siddhiAppApiHelper.deploySiddhiApp(deploybleNode.getKey(), siddhiApp);
                log.info("Siddhi App '" + siddhiAppDetails.getKey() + "' successfully deployed on node " +
                        deploybleNode.getKey());
                siddhiAppCountsMap.merge(deploybleNode.getKey(), (long) 1, Long::sum);
                siddhiAppApiHelper.deleteSiddhiApp(nodeUrl, siddhiAppDetails.getKey());
                log.info("Siddhi App '" + siddhiAppDetails.getKey() + "' was deleted from the node " + nodeUrl);
            } catch (SiddhiAppsApiHelperException e) {
                log.error(e.getMessage(), e);
                return Response.status(e.getStatus()).entity(e.getLocalizedMessage()).build();
            }
        }
        log.info("Siddhi app count for each node after reshuffle, " + siddhiAppCountsMap.toString());
        log.info("|----------Reshuffle Process completed successfully----------|");
        setSiddhiAppsCountMap(siddhiAppCountsMap);
        return Response.ok().entity("Reshuffle Process completed successfully").build();
    }


    public void reduceSiddhiAppCout(String node) {
        siddhiAppsCountMap.put(node, siddhiAppsCountMap.get(node) - 1);
        setSiddhiAppsCountMap(siddhiAppsCountMap);
    }

    public void increaseSiddhiAppCout(String node) {
        siddhiAppsCountMap.put(node, siddhiAppsCountMap.get(node) + 1);
        setSiddhiAppsCountMap(siddhiAppsCountMap);
    }

    public Map<String, Long> genarateSiddhiAppsCountMap() {
        Map<String, Long> siddhiAppCounts = new HashMap();
        long appCount;
        for (Object node : nodes.keySet()) {
            try {
                appCount = siddhiAppApiHelper.getSiddhiAppCount(node.toString());
                siddhiAppCounts.put(node.toString(), appCount);
            } catch (SiddhiAppsApiHelperException e) {
                log.error("Error occured while retrieving the siddhi app count from node " + node.toString(), e);
            }
        }
        return siddhiAppCounts;
    }

    public void setSiddhiAppsCountMap(Map<String, Long> siddhiAppsCount) {
        this.siddhiAppsCountMap = siddhiAppsCount;
    }
}
