/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.sp.jobmanager.core;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.sp.jobmanager.core.appcreator.DeployableSiddhiQueryGroup;
import org.wso2.carbon.sp.jobmanager.core.appcreator.DistributedSiddhiQuery;
import org.wso2.carbon.sp.jobmanager.core.bean.ClusterConfig;
import org.wso2.carbon.sp.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.sp.jobmanager.core.bean.InterfaceConfig;
import org.wso2.carbon.sp.jobmanager.core.bean.StrategyConfig;
import org.wso2.carbon.sp.jobmanager.core.model.ManagerNode;
import org.wso2.carbon.sp.jobmanager.core.model.ManagerNodeConfig;
import org.wso2.carbon.sp.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.sp.jobmanager.core.model.SiddhiAppHolder;
import org.wso2.carbon.sp.jobmanager.core.util.TypeConverter;

import java.util.ArrayList;
import java.util.List;

public class BeanTestCase {

    @Test
    public void testClusterConfigBean() {

        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig.setDatasource("WSO2_DS");
        strategyConfig.setEventPollingInterval(500);
        strategyConfig.setHeartbeatInterval(1000);
        strategyConfig.setHeartbeatMaxRetry(3);

        ClusterConfig clusterConfig = new ClusterConfig();
        clusterConfig.setStrategyConfig(strategyConfig);
        clusterConfig.setCoordinationStrategyClass("distributed");
        clusterConfig.setEnabled(true);
        clusterConfig.setGroupId("group-1");

        Assert.assertEquals(clusterConfig.getCoordinationStrategyClass(), "distributed");
        Assert.assertEquals(clusterConfig.getGroupId(), "group-1");
        Assert.assertEquals(clusterConfig.getStrategyConfig().getDatasource(), "WSO2_DS");
        Assert.assertEquals(clusterConfig.getStrategyConfig().getEventPollingInterval(), 500);
        Assert.assertEquals(clusterConfig.getStrategyConfig().getHeartbeatInterval(), 1000);
        Assert.assertEquals(clusterConfig.getStrategyConfig().getHeartbeatMaxRetry(), 3);
        Assert.assertTrue(clusterConfig.isEnabled());
    }

    @Test
    public void testDeploymentConfig() {

        InterfaceConfig interfaceConfig = new InterfaceConfig();
        interfaceConfig.setHost("localhost");
        interfaceConfig.setPort(9091);

        DeploymentConfig deploymentConfig = new DeploymentConfig();
        deploymentConfig.setBootstrapURLs("localhost:9090");
        deploymentConfig.setDatasource("WSO2_DS");
        deploymentConfig.setHeartbeatInterval(500);
        deploymentConfig.setHeartbeatMaxRetry(3);
        deploymentConfig.setMinResourceCount(2);
        deploymentConfig.setType("distributed");
        deploymentConfig.setHttpsInterface(interfaceConfig);

        Assert.assertEquals(deploymentConfig.getBootstrapURLs(), "localhost:9090");
        Assert.assertEquals(deploymentConfig.getDatasource(), "WSO2_DS");
        Assert.assertEquals(deploymentConfig.getHeartbeatInterval(), 500);
        Assert.assertEquals(deploymentConfig.getHeartbeatMaxRetry(), 3);
        Assert.assertEquals(deploymentConfig.getMinResourceCount(), 2);
        Assert.assertEquals(deploymentConfig.getType(), "distributed");
        Assert.assertEquals(deploymentConfig.getHttpsInterface().getHost(), "localhost");
        Assert.assertEquals(deploymentConfig.getHttpsInterface().getPort(), 9091);
        Assert.assertEquals(deploymentConfig.getHttpsInterface().toString(),
                            "Interface { host: localhost, port: 9091 }");

        InterfaceConfig similarInterfaceConfig = new InterfaceConfig();
        similarInterfaceConfig.setHost("localhost");
        similarInterfaceConfig.setPort(9091);
        InterfaceConfig differentInterfaceConfig = new InterfaceConfig();
        differentInterfaceConfig.setHost("192.168.1.1");
        differentInterfaceConfig.setPort(9091);

        Assert.assertTrue(interfaceConfig.equals(similarInterfaceConfig));
        Assert.assertTrue(interfaceConfig.equals(interfaceConfig));
        Assert.assertFalse(interfaceConfig.equals(null));
        Assert.assertFalse(interfaceConfig.equals(new InterfaceConfig()));
        Assert.assertFalse(interfaceConfig.equals(differentInterfaceConfig));

        org.wso2.carbon.sp.jobmanager.core.model.InterfaceConfig iConfigModel = TypeConverter.convert(interfaceConfig);
        Assert.assertEquals(iConfigModel.getHost(), interfaceConfig.getHost());
        Assert.assertEquals(iConfigModel.getPort(), Integer.valueOf(interfaceConfig.getPort()));
        InterfaceConfig convertedInterfaceConfig = TypeConverter.convert(iConfigModel);
        Assert.assertEquals(convertedInterfaceConfig.getHost(), interfaceConfig.getHost());
        Assert.assertEquals(convertedInterfaceConfig.getPort(), convertedInterfaceConfig.getPort());
    }

    @Test
    public void testDistributedSiddhiQuery() {
        DeployableSiddhiQueryGroup deployableSiddhiQueryGroup = new DeployableSiddhiQueryGroup("group-1", false,
                                                                                               1);
        DeployableSiddhiQueryGroup deployableSiddhiQueryGroup2 = new DeployableSiddhiQueryGroup("group-2", false,
                                                                                                1);
        Assert.assertTrue(deployableSiddhiQueryGroup.equals(deployableSiddhiQueryGroup));
        Assert.assertFalse(deployableSiddhiQueryGroup.equals(null));
        Assert.assertFalse(deployableSiddhiQueryGroup.equals(deployableSiddhiQueryGroup2));

        List<DeployableSiddhiQueryGroup> queryGroups = new ArrayList<>();
        queryGroups.add(deployableSiddhiQueryGroup);
        queryGroups.add(deployableSiddhiQueryGroup2);

        DistributedSiddhiQuery distributedSiddhiQuery = new DistributedSiddhiQuery("app-1", queryGroups);
        Assert.assertEquals(distributedSiddhiQuery.getAppName(), "app-1");
        distributedSiddhiQuery.setAppName("app-2");
        Assert.assertEquals(distributedSiddhiQuery.getAppName(), "app-2");
        Assert.assertTrue(distributedSiddhiQuery.equals(distributedSiddhiQuery));
        Assert.assertFalse(distributedSiddhiQuery.equals(null));
        Assert.assertFalse(distributedSiddhiQuery.equals(new DistributedSiddhiQuery("app-2", null)));
        Assert.assertTrue(distributedSiddhiQuery.getQueryGroups().equals(queryGroups));

    }

    @Test
    public void testManagerNodeModel() {
        InterfaceConfig interfaceConfig = new InterfaceConfig();
        interfaceConfig.setHost("localhost");
        interfaceConfig.setPort(9091);

        ManagerNode managerNode = new ManagerNode();
        managerNode.setId("manager-1").setHeartbeatInterval(1000).setHeartbeatMaxRetry(3).
                setHttpsInterface(interfaceConfig);
        Assert.assertEquals(managerNode.getId(), "manager-1");
        Assert.assertEquals(managerNode.getHeartbeatInterval(), 1000);
        Assert.assertEquals(managerNode.getHeartbeatMaxRetry(), 3);
        Assert.assertEquals(managerNode.getHttpsInterface(), interfaceConfig);
        Assert.assertEquals(managerNode.toString(), "ManagerNode { id: manager-1, host: localhost, port: 9091 }");

        ManagerNodeConfig managerNodeConfig = TypeConverter.convert(managerNode);
        Assert.assertEquals(managerNodeConfig.getHeartbeatInterval(), Integer.valueOf(1000));
        Assert.assertEquals(managerNodeConfig.getHeartbeatMaxRetry(), Integer.valueOf(3));
        Assert.assertEquals(managerNodeConfig.getId(), "manager-1");
        Assert.assertEquals(managerNodeConfig.getHttpsInterface().getHost(), interfaceConfig.getHost());
        Assert.assertEquals(managerNodeConfig.getHttpsInterface().getPort(), Integer.valueOf(interfaceConfig.getPort()));

        ManagerNode convertedManagerNode = TypeConverter.convert(managerNodeConfig);
        Assert.assertEquals(convertedManagerNode.getId(), managerNode.getId());
    }

    @Test
    public void testResourceNodeModel() {
        InterfaceConfig interfaceConfig = new InterfaceConfig();
        interfaceConfig.setHost("localhost");
        interfaceConfig.setPort(9091);

        ResourceNode resourceNode = new ResourceNode("resource-1");
        resourceNode.setHttpsInterface(interfaceConfig);
        resourceNode.setState("NEW");
        Assert.assertEquals(resourceNode.getId(), "resource-1");
        resourceNode.setId("resource-2");
        Assert.assertEquals(resourceNode.getId(), "resource-2");
        Assert.assertEquals(resourceNode.getState(), "NEW");
        Assert.assertEquals(resourceNode.getHttpsInterface(), interfaceConfig);
        Assert.assertEquals(resourceNode.getFailedPingAttempts(), 0);
        resourceNode.incrementFailedPingAttempts();
        Assert.assertEquals(resourceNode.getFailedPingAttempts(), 1);
        resourceNode.resetFailedPingAttempts();
        Assert.assertEquals(resourceNode.getFailedPingAttempts(), 0);
        long timestamp = System.currentTimeMillis();
        Assert.assertTrue(resourceNode.getLastPingTimestamp() <= timestamp);
        resourceNode.updateLastPingTimestamp();
        Assert.assertTrue(resourceNode.getLastPingTimestamp() >= timestamp);
        Assert.assertEquals(resourceNode.toString(), "ResourceNode { id: resource-2, host: localhost, port: 9091 }");
        Assert.assertTrue(resourceNode.equals(resourceNode));
        Assert.assertFalse(resourceNode.equals(null));
        Assert.assertFalse(resourceNode.equals(new ResourceNode("resource-3")));

    }

    @Test
    public void testSiddhiAppHolderModel() {
        InterfaceConfig interfaceConfig = new InterfaceConfig();
        interfaceConfig.setHost("localhost");
        interfaceConfig.setPort(9091);
        ResourceNode resourceNode = new ResourceNode("resource-1");
        SiddhiAppHolder siddhiAppHolder = new SiddhiAppHolder(
                "parentAppName", "group-1", "app-1",
                "@App:name('app-1')", resourceNode, false, 0);
        Assert.assertEquals(siddhiAppHolder.getParentAppName(), "parentAppName");
        Assert.assertEquals(siddhiAppHolder.getGroupName(), "group-1");
        Assert.assertEquals(siddhiAppHolder.getAppName(), "app-1");
        Assert.assertEquals(siddhiAppHolder.getSiddhiApp(), "@App:name('app-1')");
        Assert.assertEquals(siddhiAppHolder.getDeployedNode(), resourceNode);

        siddhiAppHolder.setParentAppName("parent2AppName");
        siddhiAppHolder.setGroupName("group-2");
        siddhiAppHolder.setAppName("app-2");
        siddhiAppHolder.setSiddhiApp("@App:name('app-2')");
        siddhiAppHolder.setDeployedNode(new ResourceNode("resource-2"));

        Assert.assertEquals(siddhiAppHolder.getParentAppName(), "parent2AppName");
        Assert.assertEquals(siddhiAppHolder.getGroupName(), "group-2");
        Assert.assertEquals(siddhiAppHolder.getAppName(), "app-2");
        Assert.assertEquals(siddhiAppHolder.getSiddhiApp(), "@App:name('app-2')");
        Assert.assertFalse(siddhiAppHolder.getDeployedNode().equals(resourceNode));

        Assert.assertFalse(siddhiAppHolder.equals(null));
        Assert.assertTrue(siddhiAppHolder.equals(siddhiAppHolder));
        Assert.assertFalse(siddhiAppHolder.equals(new SiddhiAppHolder(
                "parent2AppName", null, null, null, null,
                false, 0)));
        Assert.assertFalse(siddhiAppHolder.equals(
                new SiddhiAppHolder("parent2AppName", "group-2", null, null,
                                    null, false, 0)));
        Assert.assertFalse(siddhiAppHolder.equals(new SiddhiAppHolder(
                "parent2AppName", "group-2", "app-2", null,
                null, false, 0)));
        Assert.assertTrue(siddhiAppHolder.equals(new SiddhiAppHolder(
                "parent2AppName", "group-2", "app-2",
                "@App:name('app-2')", null, false, 0)));
    }
}
