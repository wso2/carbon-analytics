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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.exceptions.SiddhiAppsApiHelperException;
import org.wso2.carbon.business.rules.core.exceptions.SiddhiManagerHelperException;
import org.wso2.carbon.business.rules.core.services.TemplateManagerService;
import org.wso2.carbon.business.rules.core.manager.util.SiddhiManagerHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

public class RoundRobbinDeployer implements SiddhiAppDeployer {
    private static final Logger log = LoggerFactory.getLogger(RoundRobbinDeployer.class);

    @Override
    public Response deploySiddhiApp(TemplateManagerService templateManagerService, Object siddhiApp) {
        Map nodes = templateManagerService.getNodes();
        Map<String, Long> deployedSiddhiAppCountInEachNode;
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        List<Object> responseData = new ArrayList<Object>();

        String siddhiAppName;
        try {
            siddhiAppName = SiddhiManagerHelper.getSiddhiAppName(siddhiApp);
        } catch (SiddhiManagerHelperException e) {
            log.error("Error occured while retrieving the siddhi app name", e);
            return Response.serverError().entity("Error occured while retrieving the siddhi app name " +
                    e.getMessage()).build();
        }
        for (Object node : nodes.keySet()) {
            boolean isSiddhiAppAvailable = false;
            try {
                isSiddhiAppAvailable = templateManagerService.checkSiddhiAppAvailability(node.toString(),
                        siddhiAppName);
            } catch (SiddhiAppsApiHelperException e) {
                log.error("Error occured while retrieving siddhi app availability from node " + node.toString(), e);
            }
            if (isSiddhiAppAvailable) {
                log.error("Failed to deploy the siddhi app. Siddhi app already exists in node" + node.toString());
                responseData.add("Failed to deploy the siddhi app. Siddhi app already exists in node " +
                        node.toString());
                return Response.status(Response.Status.CONFLICT).entity(gson.toJson(responseData)).build();
            }
        }
        deployedSiddhiAppCountInEachNode = templateManagerService.getSiddhiAppsCountMap();
        Map.Entry<String, Long> deploybleNode = deployedSiddhiAppCountInEachNode.entrySet().stream().
                min(Map.Entry.comparingByValue()).get();

        try {
            templateManagerService.deploySiddhiApp(deploybleNode.getKey(), siddhiApp.toString());
            increaseSiddhiAppCout(deploybleNode.getKey(), deployedSiddhiAppCountInEachNode,
                    templateManagerService);
            log.info("Siddhi App " + siddhiAppName + " deployed successfully on " + deploybleNode.getKey());
        } catch (SiddhiAppsApiHelperException e) {
            log.error("Error occured while deploying siddhi app on node " + deploybleNode.getKey(), e);
            return Response.status(e.getStatus()).entity("Error occured while deploying siddhi app on node " +
                    deploybleNode.getKey()).build();
        }
        return Response.accepted().entity("Siddhi App deployed successfully on " + deploybleNode.getKey()).build();
    }

    @Override
    public Response updateSiddhiApp(TemplateManagerService templateManagerService, Object siddhiApp) {
        Map nodes = templateManagerService.getNodes();
        boolean isSiddhiAppAvailable = false;
        String siddhiAppName = null;
        try {
            siddhiAppName = SiddhiManagerHelper.getSiddhiAppName(siddhiApp);
        } catch (SiddhiManagerHelperException e) {
            log.error("Error occured while retrievng the siddhi app name. ", e);
            return Response.serverError().entity("Error occured while retrievng the siddhi app name.").build();
        }

        for (Object node : nodes.keySet()) {
            try {
                isSiddhiAppAvailable = templateManagerService.checkSiddhiAppAvailability(node.toString(),
                        siddhiAppName);
            } catch (SiddhiAppsApiHelperException e) {
                log.error("Error occured while checking the siddhi app availability from node " + node.toString() + "" +
                        "for siddhi app " + siddhiAppName + " :", e);
                return Response.status(e.getStatus()).entity("Error occured while checking the siddhi app " +
                        " availability from node " + node.toString() + " for siddhi app " + siddhiAppName).build();
            }
            if (isSiddhiAppAvailable) {
                try {
                    templateManagerService.updateDeployedSiddhiApp(node.toString(), siddhiApp.toString());
                    log.info("Siddhi App " + siddhiAppName + " updated successfully on node " + node.toString());
                    return Response.status(Response.Status.OK).entity("siddhi App updated successfully").build();
                } catch (SiddhiAppsApiHelperException e) {
                    log.error("Error occurred while deploying the siddhi app " + siddhiAppName + " on node "
                            + node.toString(), e);
                    return Response.status(e.getStatus()).entity("Error occurred while deploying the siddhi app "
                            + siddhiAppName + " on node " + node.toString()).build();
                }
            }
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Siddhi app " + siddhiAppName + "does not " +
                "exists").build();
    }

    @Override
    public Response deleteSiddhiApp(TemplateManagerService templateManagerService, String siddhiAppName) {
        Map nodes = templateManagerService.getNodes();
        boolean issiddhiAppAvailable = false;
        for (Object node : nodes.keySet()) {
            try {
                issiddhiAppAvailable = templateManagerService.checkSiddhiAppAvailability(node.toString(),
                        siddhiAppName);
            } catch (SiddhiAppsApiHelperException e) {
                log.error("Error Occurred while checking the availability of siddhi app " + siddhiAppName + " in node " +
                        node.toString(), e);
            }
            if (issiddhiAppAvailable) {
                try {
                    templateManagerService.deleteSiddhiApp(node.toString(), siddhiAppName);
                    reduceSiddhiAppCout(node.toString(), templateManagerService.getSiddhiAppsCountMap(),
                            templateManagerService);
                    return Response.ok().entity("The Siddhi Application is successfully deleted.")
                            .build();
                } catch (SiddhiAppsApiHelperException e) {
                    log.error("Error occurred while deleting siddhi app " + siddhiAppName + " from node " +
                            node.toString(), e);
                    return Response.serverError().entity("Error occurred while deleting siddhi app "
                            + siddhiAppName + " from node " + node.toString()).build();
                }
            }
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Siddhi app" + siddhiAppName + " does not exists")
                .build();
    }

    @Override
    public Response reShuffle(TemplateManagerService templateManagerService) {
        Map nodes = templateManagerService.getNodes();
        long totalSiddhiAppCount = 0;
        Map<String, Long> siddhiAppCountsMap;
        Map<String, Long> siddhiAppCountToBeRemovedFromEachNode = new HashMap();
        Map<String, String> siddhiAppNamesToBeRemovedFromEachNode = new HashMap<>();
        siddhiAppCountsMap = templateManagerService.getSiddhiAppCount();

        for (Map.Entry<String, Long> siddhiAppCount : siddhiAppCountsMap.entrySet()) {
            totalSiddhiAppCount = totalSiddhiAppCount + siddhiAppCount.getValue();
        }

        long avgSiddhiAppCountForEachNode = totalSiddhiAppCount / nodes.size();
        for (Map.Entry<String, Long> siddhiAppCountForTheNode : siddhiAppCountsMap.entrySet()) {
            if (siddhiAppCountForTheNode.getValue() - avgSiddhiAppCountForEachNode > 0) {
                siddhiAppCountToBeRemovedFromEachNode.put(siddhiAppCountForTheNode.getKey(),
                        siddhiAppCountForTheNode.getValue() - avgSiddhiAppCountForEachNode);
            }
        }
        for (Map.Entry<String, Long> node : siddhiAppCountToBeRemovedFromEachNode.entrySet()) {
            List<String> siddhiAppList;
            try {
                siddhiAppList = templateManagerService.getSiddhiAppList(node.getKey());
            } catch (SiddhiAppsApiHelperException e) {
                log.error("Error occured while retrieving the siddhi app list from node  " + node.getKey(), e);
                return Response.serverError().entity("Error occured while connecting into node " + node.getKey() +
                        ". aborting reshuffle process").build();
            }
            for (int i = 0; i < node.getValue(); i++) {
                siddhiAppNamesToBeRemovedFromEachNode.put(siddhiAppList.get(i), node.getKey());
            }
        }
        for (Map.Entry<String, String> siddhiAppDetails : siddhiAppNamesToBeRemovedFromEachNode.entrySet()) {
            Map.Entry<String, Long> deploybleNode = siddhiAppCountsMap.entrySet().stream().
                    min(Map.Entry.comparingByValue()).get();
            String nodeUrl = siddhiAppDetails.getValue();
            try {
                String siddhiApp = templateManagerService.getSiddhiApp(nodeUrl, siddhiAppDetails.getKey());
                log.info("Retrieved the Siddhi App " + siddhiAppDetails.getKey() + " from the node " + nodeUrl);
                templateManagerService.deploySiddhiApp(deploybleNode.getKey(), siddhiApp);
                log.info("Siddhi App " + siddhiAppDetails.getKey() + " successfully deployed on node " +
                        deploybleNode.getKey());
                siddhiAppCountsMap.merge(deploybleNode.getKey(), (long) 1, Long::sum);
                templateManagerService.deleteSiddhiApp(nodeUrl, siddhiAppDetails.getKey());
                log.info("Siddhi App " + siddhiAppDetails.getKey() + " was deleted from the node " + nodeUrl);
            } catch (SiddhiAppsApiHelperException e) {
                log.error(e.getMessage(), e);
                return Response.status(e.getStatus()).entity(e.getLocalizedMessage()).build();
            }
        }
        log.info("|----------Reshuffle Process completed successfully----------|");
        templateManagerService.setSiddhiAppsCountMap(siddhiAppCountsMap);
        return Response.ok().entity("Reshuffle Process completed successfully").build();
    }


    public void reduceSiddhiAppCout(String node, Map<String, Long> siddhiAppCountMap,
                                    TemplateManagerService templateManagerService) {
        siddhiAppCountMap.put(node, siddhiAppCountMap.get(node) - 1);
        templateManagerService.setSiddhiAppsCountMap(siddhiAppCountMap);
    }

    public void increaseSiddhiAppCout(String node, Map<String, Long> siddhiAppCountMap,
                                      TemplateManagerService templateManagerService) {
        siddhiAppCountMap.put(node, siddhiAppCountMap.get(node) + 1);
        templateManagerService.setSiddhiAppsCountMap(siddhiAppCountMap);
    }
}
