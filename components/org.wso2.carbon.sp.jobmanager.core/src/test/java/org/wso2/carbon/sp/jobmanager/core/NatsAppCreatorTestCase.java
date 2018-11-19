/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.sp.jobmanager.core;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.sp.jobmanager.core.appcreator.DeployableSiddhiQueryGroup;
import org.wso2.carbon.sp.jobmanager.core.appcreator.NatsSiddhiAppCreator;
import org.wso2.carbon.sp.jobmanager.core.appcreator.SiddhiQuery;
import org.wso2.carbon.sp.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.topology.SiddhiTopology;
import org.wso2.carbon.sp.jobmanager.core.topology.SiddhiTopologyCreatorImpl;
import org.wso2.siddhi.core.SiddhiManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NatsAppCreatorTestCase {

    private static final Logger log = Logger.getLogger(NatsAppCreatorTestCase.class);
    private AtomicInteger count;
    private AtomicInteger errorAssertionCount;
    private static final String CLUSTER_ID = "test-cluster";
    private static final String NATS_SERVER_URL = "nats://localhost:4222";

    @BeforeMethod
    public void setUp()
    {
        DeploymentConfig deploymentConfig = new DeploymentConfig();
        deploymentConfig.setClusterId(CLUSTER_ID);
        deploymentConfig.setNatsServerUrl(NATS_SERVER_URL);
        ServiceDataHolder.setDeploymentConfig(deploymentConfig);
        count = new AtomicInteger(0);
        errorAssertionCount = new AtomicInteger(0);
    }

    /** Test the topology creation for a particular siddhi app includes nats transport.
     */
    @Test
    public void testSiddhiTopologyCreator() {
        String siddhiApp = "@App:name('Energy-Alert-App')\n"
                + "@App:description('Energy consumption and anomaly detection')\n"
                + "@source(type = 'http', topic = 'device-power', @map(type = 'json'))\n"
                + "define stream DevicePowerStream (type string, deviceID string, power int,"
                + " roomID string);\n"
                + "@sink(type = 'email', to = '{{autorityContactEmail}}', username = 'john',"
                + " address = 'john@gmail.com',"
                + " password ='test', subject = 'High power consumption of {{deviceID}}', "
                + "@map(type = 'xml', @payload('Device ID: {{deviceID}} of"
                + "room : {{roomID}} power is consuming {{finalPower}}kW/h. ')))\n"
                + "define stream AlertStream (deviceID string, roomID string, initialPower double, "
                + "finalPower double,autorityContactEmail string);\n"
                + "@info(name = 'monitered-filter')@dist(execGroup='001')\n"
                + "from DevicePowerStream[type == 'monitored']\n"
                + "select deviceID, power, roomID\n"
                + "insert current events into MonitoredDevicesPowerStream;\n"
                + "@info(name = 'power-increase-pattern')@dist(parallel='2', execGroup='002')\n"
                + "partition with (deviceID of MonitoredDevicesPowerStream)\n"
                + "begin\n"
                + "@info(name = 'avg-calculator')\n"
                + "from MonitoredDevicesPowerStream#window.time(2 min)\n"
                + "select deviceID, avg(power) as avgPower, roomID\n"
                + "insert current events into #AvgPowerStream;\n"
                + "@info(name = 'power-increase-detector')\n"
                + "from every e1 = #AvgPowerStream -> e2 = #AvgPowerStream[(e1.avgPower + 5) "
                + "<= avgPower] within 10 min\n"
                + "select e1.deviceID as deviceID, e1.avgPower as initialPower, "
                + "e2.avgPower as finalPower, e1.roomID\n"
                + "insert current events into RisingPowerStream;\n"
                + "end;\n"
                + "@info(name = 'power-range-filter')@dist(parallel='2', execGroup='003')\n"
                + "from RisingPowerStream[finalPower > 100]\n"
                + "select deviceID, roomID, initialPower, finalPower, "
                + "'no-reply@powermanagement.com' as autorityContactEmail\n"
                + "insert current events into AlertStream;\n"
                + "@info(name = 'internal-filter')@dist(execGroup='004')\n"
                + "from DevicePowerStream[type == 'internal']\n"
                + "select deviceID, power\n"
                + "insert current events into InternaltDevicesPowerStream;\n";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new NatsSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);

        Assert.assertEquals(queryGroupList.size(),5);
        for (DeployableSiddhiQueryGroup group : queryGroupList) {
            for (SiddhiQuery query : group.getSiddhiQueries()) {
                SiddhiManager siddhiManager = new SiddhiManager();
                siddhiManager.createSiddhiAppRuntime(query.getApp());
            }
        }
    }


}
