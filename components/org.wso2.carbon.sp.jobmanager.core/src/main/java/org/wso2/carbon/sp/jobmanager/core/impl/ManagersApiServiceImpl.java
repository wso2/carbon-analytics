/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.sp.jobmanager.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.analytics.permissions.bean.Permission;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.sp.jobmanager.core.api.ApiResponseMessage;
import org.wso2.carbon.sp.jobmanager.core.api.ManagersApiService;
import org.wso2.carbon.sp.jobmanager.core.api.NotFoundException;
import org.wso2.carbon.sp.jobmanager.core.bean.KafkaTransportDetails;
import org.wso2.carbon.sp.jobmanager.core.impl.utils.Constants;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.model.ManagerDetails;
import org.wso2.carbon.sp.jobmanager.core.model.ManagerNode;
import org.wso2.carbon.sp.jobmanager.core.model.SiddhiAppDetails;
import org.wso2.carbon.sp.jobmanager.core.model.SiddhiAppHolder;
import org.wso2.msf4j.Request;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;


/**
 * Distributed Siddhi Service Implementataion Class
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2018-01-29T08:19:07.148Z")
@Component(service = ManagersApiService.class, immediate = true)

public class ManagersApiServiceImpl extends ManagersApiService {
    private static final Log logger = LogFactory.getLog(ManagersApiServiceImpl.class);
    private static final String MANAGE_SIDDHI_APP_PERMISSION_STRING = "siddhiApp.manage";
    private static final String VIEW_SIDDHI_APP_PERMISSION_STRING = "siddhiApp.view";


    private static String getUserName(Request request) {
        Object username = request.getProperty("username");
        return username != null ? username.toString() : null;
    }

    /***
     * This method is to list down all the manager's details that are belongs to the same cluster
     * @return :Manager's host and the port
     */

    public Response getManagerDetails() {
        ClusterCoordinator clusterCoordinator = ServiceDataHolder.getCoordinator();
        ManagerDetails managerDetails = new ManagerDetails();
        if (clusterCoordinator != null) {
            for (NodeDetail nodeDetail : clusterCoordinator.getAllNodeDetails()) {
                if (nodeDetail.getPropertiesMap() != null) {
                    Map<String, Object> propertiesMap = nodeDetail.getPropertiesMap();
                    managerDetails.setGroupId(nodeDetail.getGroupId());
                    if (clusterCoordinator.isLeaderNode()) {
                        managerDetails.setHaStatus(Constants.HA_ACTIVE_STATUS);
                    } else {
                        managerDetails.setHaStatus(Constants.HA_PASIVE_STATUS);
                    }
                }
            }
            return Response.ok().entity(managerDetails).build();
        } else {
            return Response.status(Response.Status.NO_CONTENT).entity(
                    new ApiResponseMessage(ApiResponseMessage.ERROR, "There is no manager nodes found in the "
                            + "cluster")).build();
        }
    }

    /**
     * Returns all the deployed siddhi application details
     *
     * @return
     */

    public Response getSiddhiAppDetails() {
        if(ServiceDataHolder.getResourcePool() != null) {
            Map<String, List<SiddhiAppHolder>> deployedSiddhiAppHolder = ServiceDataHolder.getResourcePool()
                    .getSiddhiAppHoldersMap();
            Map<String, List<SiddhiAppHolder>> waitingToDeploy = ServiceDataHolder.getResourcePool()
                    .getAppsWaitingForDeploy();

            if (deployedSiddhiAppHolder.isEmpty() && waitingToDeploy.isEmpty()) {
                logger.info("There is no siddhi apps");
                return Response.ok().entity("There is no siddhi app  in the manager node").build();
            } else {
                List<SiddhiAppDetails> appList = new ArrayList<>();
                for (Map.Entry<String, List<SiddhiAppHolder>> en : waitingToDeploy.entrySet()) {
                    for (SiddhiAppHolder childApp : en.getValue()) {
                        SiddhiAppDetails appHolder = new SiddhiAppDetails();
                        appHolder.setParentAppName(childApp.getParentAppName());
                        appHolder.setAppName(childApp.getAppName());
                        appHolder.setGroupName(childApp.getGroupName());
                        appHolder.setSiddhiApp(childApp.getSiddhiApp());
                        appList.add(appHolder);
                    }
                }
                for (Map.Entry<String, List<SiddhiAppHolder>> en : deployedSiddhiAppHolder.entrySet()) {
                    for (SiddhiAppHolder childApp : en.getValue()) {
                        SiddhiAppDetails appHolder = new SiddhiAppDetails();
                        appHolder.setParentAppName(childApp.getParentAppName());
                        appHolder.setAppName(childApp.getAppName());
                        appHolder.setGroupName(childApp.getGroupName());
                        appHolder.setSiddhiApp(childApp.getSiddhiApp());

                        if (childApp.getDeployedNode() != null) {
                            appHolder.setId(childApp.getDeployedNode().getId());
                            appHolder.setState(childApp.getDeployedNode().getState());
                            appHolder.setHost(childApp.getDeployedNode().getHttpsInterface().getHost());
                            appHolder.setPort(Integer.toString(childApp.getDeployedNode()
                                    .getHttpsInterface().getPort()));
                            appHolder.setFailedPingAttempts(Integer.toString(childApp.getDeployedNode()
                                    .getFailedPingAttempts()));
                            appHolder.setLastPingTimestamp(Long.toString(childApp.getDeployedNode()
                                    .getLastPingTimestamp()));
                        }
                        appList.add(appHolder);
                    }
                }
                return Response.ok().entity(appList).build();
            }
        } else {
            return Response.status(Response.Status.NO_CONTENT).entity(
                    new ApiResponseMessage(ApiResponseMessage.ERROR, "There is no siddhi apps found in the "
                            + "node")).build();
        }
    }

    /**
     * This method is to display the text view of the siddhi application
     *
     * @param appName
     * @return
     */
    public Response getSiddhiAppTextView(String appName) {
        if (ServiceDataHolder.getUserDefinedSiddhiApp(appName) != null) {
            String definedApp = ServiceDataHolder.getUserDefinedSiddhiApp(appName);
            return Response.ok().entity(definedApp).build();
        } else {
            return Response.status(Response.Status.NO_CONTENT).entity(
                    new ApiResponseMessage(ApiResponseMessage.ERROR, "There is no such siddhi application " +
                            "deployed in the manager node")).build();
        }
    }

    /**
     * This method is to list down the child app details of the given parent siddhi application
     *
     * @param appName
     * @return
     * @throws NotFoundException
     */

    public Response getChildSiddhiApps(String appName) throws NotFoundException {
        Map<String, List<SiddhiAppHolder>> deployedSiddhiAppHolder = ServiceDataHolder.getResourcePool()
                .getSiddhiAppHoldersMap();
        Map<String, List<SiddhiAppHolder>> waitingToDeploy = ServiceDataHolder.getResourcePool()
                .getAppsWaitingForDeploy();
        if (waitingToDeploy.containsKey(appName)) {
            List<SiddhiAppHolder> holder = waitingToDeploy.get(appName);
            List<SiddhiAppDetails> appList = new ArrayList<>();
            holder.forEach((siddhiAppHolder -> {
                SiddhiAppDetails appHolder = new SiddhiAppDetails();
                appHolder.setParentAppName(siddhiAppHolder.getParentAppName());
                appHolder.setAppName(siddhiAppHolder.getAppName());
                String groupName = siddhiAppHolder.getGroupName().replace(
                        siddhiAppHolder.getParentAppName() + "-", " ");
                appHolder.setGroupName(groupName);
                appHolder.setSiddhiApp(siddhiAppHolder.getSiddhiApp());
                appHolder.setAppStatus(Constants.WAITING_STATUS);
                appList.add(appHolder);
            }));
            return Response.ok().entity(appList).build();
        } else {
            List<SiddhiAppHolder> activeHolder = deployedSiddhiAppHolder.get(appName);
            List<SiddhiAppDetails> appList = new ArrayList<>();
            activeHolder.forEach((siddhiAppHolder -> {
                SiddhiAppDetails appHolder = new SiddhiAppDetails();
                appHolder.setParentAppName(siddhiAppHolder.getParentAppName());
                appHolder.setAppName(siddhiAppHolder.getAppName());
                String groupName = siddhiAppHolder.getGroupName().replace(
                        siddhiAppHolder.getParentAppName() + "-", " ");
                appHolder.setGroupName(groupName);
                appHolder.setSiddhiApp(siddhiAppHolder.getSiddhiApp());
                appHolder.setId(siddhiAppHolder.getDeployedNode().getId());
                appHolder.setState(siddhiAppHolder.getDeployedNode().getState());
                appHolder.setHost(siddhiAppHolder.getDeployedNode().getHttpsInterface().getHost());
                appHolder.setPort(Integer.toString(siddhiAppHolder.getDeployedNode().getHttpsInterface().getPort()));
                appHolder.setFailedPingAttempts(
                        Integer.toString(siddhiAppHolder.getDeployedNode().getFailedPingAttempts()));
                appHolder.setLastPingTimestamp(Long.toString(siddhiAppHolder.getDeployedNode().getLastPingTimestamp()));
                appHolder.setAppStatus(Constants.SIDDHI_APP_ACTIVE_STATUS);
                appList.add(appHolder);
            }));
            return Response.ok().entity(appList).build();
        }
    }

    /**
     * This method returns the kafka topic details of each child app.
     *
     * @param appName
     * @return
     */

    public Response getKafkaDetails(String appName) {
        Map<String, List<SiddhiAppHolder>> deployedSiddhiAppHolder = ServiceDataHolder.getResourcePool()
                .getSiddhiAppHoldersMap();
        Map<String, List<SiddhiAppHolder>> waitingToDeploy = ServiceDataHolder.getResourcePool()
                .getAppsWaitingForDeploy();

        if (waitingToDeploy.containsKey(appName)) {
            List<SiddhiAppHolder> holder = waitingToDeploy.get(appName);
            List<KafkaTransportDetails> kafkaDetails = new ArrayList<>();
            holder.forEach((siddhiAppHolder -> {
                KafkaTransportDetails kafkaTransport = new KafkaTransportDetails();
                kafkaTransport.setAppName(siddhiAppHolder.getAppName());
                kafkaTransport.setSiddhiApp(siddhiAppHolder.getSiddhiApp());
                getSourceSinkDetails(siddhiAppHolder, kafkaTransport, kafkaDetails);
            }));
            return Response.ok().entity(kafkaDetails).build();
        } else if (deployedSiddhiAppHolder.containsKey(appName)) {
            List<SiddhiAppHolder> holder = deployedSiddhiAppHolder.get(appName);
            List<KafkaTransportDetails> kafkaDetails = new ArrayList<>();
            holder.forEach((siddhiAppHolder -> {
                KafkaTransportDetails kafkaTransport = new KafkaTransportDetails();
                kafkaTransport.setAppName(siddhiAppHolder.getAppName());
                kafkaTransport.setSiddhiApp(siddhiAppHolder.getSiddhiApp());
                kafkaTransport.setDeployedHost(siddhiAppHolder.getDeployedNode().getHttpsInterface().getHost());
                kafkaTransport.setDeployedPort(Integer.toString(siddhiAppHolder.getDeployedNode()
                        .getHttpsInterface().getPort()));
                getSourceSinkDetails(siddhiAppHolder, kafkaTransport, kafkaDetails);
            }));
            return Response.ok().entity(kafkaDetails).build();
        } else {
            return Response.status(Response.Status.NO_CONTENT).entity(
                    new ApiResponseMessage(ApiResponseMessage.ERROR, "There is no siddhi app  in the manager "
                            + "node")).build();
        }
    }

    /**
     * This method checks whether the Current Manager node is the active node.
     *
     * @return
     */
    public Response isActive() {
        ManagerNode current = ServiceDataHolder.getCurrentNode();
        if (current.equals(ServiceDataHolder.getLeaderNode())) {
            return Response.ok().entity(String.format("Current node: %s is the active manager.", current.getId())).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ApiResponseMessage(ApiResponseMessage.ERROR, "Not the active node")).build();
        }
    }

    /**
     * This method helps to get the kafka sink source details
     *
     * @param siddhiAppHolder
     * @param kafkaTransport
     * @param kafkaDetails
     * @return
     */

    private List getSourceSinkDetails(SiddhiAppHolder siddhiAppHolder, KafkaTransportDetails kafkaTransport,
                                      List<KafkaTransportDetails> kafkaDetails) {
        List<String> sourceList = new ArrayList<>();
        List<String> sinkList = new ArrayList<>();
        SiddhiApp siddhiApp = SiddhiCompiler.parse(siddhiAppHolder.getSiddhiApp());
        for (Map.Entry<String, StreamDefinition> sourceStream : siddhiApp.getStreamDefinitionMap().entrySet()) {
            for (Annotation annotation : sourceStream.getValue().getAnnotations()) {
                if (annotation.getName().equalsIgnoreCase(Constants.KAFKA_SOURCE)) {
                    for (Element sourceElement : annotation.getElements()) {
                        if (sourceElement.getKey().equalsIgnoreCase(Constants.KAFKA_SOURCE_TOPIC_LIST)) {
                            sourceList.add(sourceElement.getValue());
                        }
                    }
                } else if (annotation.getName().equalsIgnoreCase(Constants.KAFKA_SINK)) {
                    for (Element sinkElement : annotation.getElements()) {
                        if (sinkElement.getKey().equalsIgnoreCase(Constants.KAFKA_SINK_TOPIC)) {
                            sinkList.add(sinkElement.getValue());
                        }
                    }
                }
            }
        }
        kafkaTransport.setSourceList(sourceList);
        kafkaTransport.setSinkList(sinkList);
        kafkaDetails.add(kafkaTransport);
        return kafkaDetails;
    }


    /**
     * This method returns all the manager's details in the cluster (manager's host, manager's port, HA
     * details)
     *
     * @return manager object with relevant details
     * @throws NotFoundException
     */

    @Override
    public Response getAllManagers(Request request) throws NotFoundException {
        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new
                Permission(Constants.PERMISSION_APP_NAME, VIEW_SIDDHI_APP_PERMISSION_STRING)) || getPermissionProvider()
                .hasPermission(getUserName(request), new Permission(Constants.PERMISSION_APP_NAME,
                        MANAGE_SIDDHI_APP_PERMISSION_STRING)))) {
            return Response.status(Response.Status.FORBIDDEN).entity(
                    "Insufficient permissions to get the manager's details").build();
        } else {
            return getManagerDetails();
        }

    }


    /**
     * Returns all the deployed siddhi application in the given manager node
     *
     * @return
     * @throws NotFoundException
     */

    @Override
    public Response getSiddhiApps(Request request) throws NotFoundException {
        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new
                Permission(Constants.PERMISSION_APP_NAME, VIEW_SIDDHI_APP_PERMISSION_STRING)) || getPermissionProvider()
                .hasPermission(getUserName(request), new Permission(Constants.PERMISSION_APP_NAME,
                        MANAGE_SIDDHI_APP_PERMISSION_STRING)))) {
            return Response.status(Response.Status.FORBIDDEN).entity("Insufficient permissions to get the "
                    + "details of the Siddhi Apps").build();
        } else {
            return getSiddhiAppDetails();
        }

    }


    /**
     * This method returns the text view of the given siddhi application
     *
     * @param appName
     * @return
     */

    @Override
    public Response getSiddhiAppTextView(String appName, Request request) {
        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new
                Permission(Constants.PERMISSION_APP_NAME, VIEW_SIDDHI_APP_PERMISSION_STRING)) || getPermissionProvider()
                .hasPermission(getUserName(request), new Permission(Constants.PERMISSION_APP_NAME,
                        MANAGE_SIDDHI_APP_PERMISSION_STRING)))) {
            return Response.status(Response.Status.FORBIDDEN).entity("Insufficient permissions to get the "
                    + "execution plan").build();
        } else {
            return getSiddhiAppTextView(appName);
        }
    }

    /**
     * We can get all the details of given parent siddhi application
     * If it is in the waiting mode we can get all the details except deployed worker node details
     *
     * @param appName
     * @return
     * @throws NotFoundException
     */

    @Override
    public Response getChildSiddhiAppDetails(String appName,
                                             Request request) throws NotFoundException {
        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new
                Permission(Constants.PERMISSION_APP_NAME, VIEW_SIDDHI_APP_PERMISSION_STRING)) || getPermissionProvider()
                .hasPermission(getUserName(request), new Permission(Constants.PERMISSION_APP_NAME,
                        MANAGE_SIDDHI_APP_PERMISSION_STRING)))) {
            return Response.status(Response.Status.FORBIDDEN).entity("Insufficient permissions to get child app "
                    + "details").build();
        } else {
            return getChildSiddhiApps(appName);
        }
    }

    @Override
    public Response getRolesByUsername(Request request, String permissionSuffix) {
        boolean isAuthorized = getPermissionProvider().hasPermission(getUserName(request), new
                Permission(Constants.PERMISSION_APP_NAME, Constants.PERMISSION_APP_NAME
                + "." + permissionSuffix));

        if (getUserName(request) != null && isAuthorized) {
            return Response.ok().entity(isAuthorized).build();
        } else {
            return Response.ok().entity(isAuthorized).build();
        }
    }

    @Override
    public Response getKafkaDetails(String appName, Request request) throws NotFoundException {
        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new
                Permission(Constants.PERMISSION_APP_NAME, VIEW_SIDDHI_APP_PERMISSION_STRING)) ||
                getPermissionProvider()
                        .hasPermission(getUserName(request), new Permission(Constants.PERMISSION_APP_NAME,
                                MANAGE_SIDDHI_APP_PERMISSION_STRING)))) {
            return Response.status(Response.Status.FORBIDDEN).entity("Insufficient permissions to get child app "
                    + "details").build();
        } else {
            return getKafkaDetails(appName);
        }
    }

    @Override
    public Response isActive(Request request) throws NotFoundException {
        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new
                Permission(Constants.PERMISSION_APP_NAME, VIEW_SIDDHI_APP_PERMISSION_STRING)) ||
                getPermissionProvider()
                        .hasPermission(getUserName(request), new Permission(Constants.PERMISSION_APP_NAME,
                                MANAGE_SIDDHI_APP_PERMISSION_STRING)))) {
            return Response.status(Response.Status.FORBIDDEN).entity("Insufficient permissions to check cluster "
                    + "leader.").build();
        } else {
            return isActive();
        }
    }

    private PermissionProvider getPermissionProvider() {
        return ServiceDataHolder.getPermissionProvider();
    }

    @Reference(
            name = "carbon.permission.provider",
            service = PermissionProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterPermissionProvider"
    )
    protected void registerPermissionProvider(PermissionProvider permissionProvider) {
        ServiceDataHolder.setPermissionProvider(permissionProvider);
    }

    protected void unregisterPermissionProvider(PermissionProvider permissionProvider) {
        ServiceDataHolder.setPermissionProvider(null);
    }
}
