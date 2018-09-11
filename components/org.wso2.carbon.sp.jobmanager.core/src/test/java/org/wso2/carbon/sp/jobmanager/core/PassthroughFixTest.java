/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.sp.jobmanager.core.appcreator.DeployableSiddhiQueryGroup;
import org.wso2.carbon.sp.jobmanager.core.appcreator.SPSiddhiAppCreator;
import org.wso2.carbon.sp.jobmanager.core.appcreator.SiddhiQuery;
import org.wso2.carbon.sp.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.sp.jobmanager.core.bean.ZooKeeperConfig;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.topology.SiddhiTopology;
import org.wso2.carbon.sp.jobmanager.core.topology.SiddhiTopologyCreatorImpl;
import org.wso2.carbon.sp.jobmanager.core.util.KafkaTestUtil;
import org.wso2.carbon.sp.jobmanager.core.util.SiddhiTopologyCreatorConstants;
import org.wso2.carbon.sp.jobmanager.core.util.TransportStrategy;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiTestHelper;
import org.wso2.siddhi.core.util.transport.InMemoryBroker;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PassthroughFixTest {
    private static final Logger log = Logger.getLogger(SiddhiTopologyCreatorTestCase.class);
    private AtomicInteger count;
    //Assertions within callbacks does not fail the testcase. Hence using below var to track such failures.
    private AtomicInteger errorAssertionCount;
    private String bootstrapSeverURL = "localhost:9092";

    @BeforeClass
    public static void init() throws Exception {
        try {
            KafkaTestUtil.cleanLogDir();
            KafkaTestUtil.setupKafkaBroker();
            Thread.sleep(2000);
        } catch (Exception e) {
            throw new RemoteException("Exception caught when starting server", e);
        }
    }

    @AfterClass
    public static void stopKafkaBroker() {
        KafkaTestUtil.stopKafkaBroker();
    }

    @BeforeMethod
    public void setUp() {
        String zooKeeperURL = "localhost:2181";
        DeploymentConfig deploymentConfig = new DeploymentConfig();
        deploymentConfig.setBootstrapURLs(bootstrapSeverURL);
        ZooKeeperConfig zooKeeperConfig = new ZooKeeperConfig();
        zooKeeperConfig.setZooKeeperURLs(zooKeeperURL);
        deploymentConfig.setZooKeeperConfig(zooKeeperConfig);
        ServiceDataHolder.setDeploymentConfig(deploymentConfig);
        count = new AtomicInteger(0);
        errorAssertionCount = new AtomicInteger(0);
    }



    @Test
    public void testPassthrougInvocForPartitionGroup(){

        String siddhiApp = "@App:name('MB-Testcase-ptTest')\n" +
                "@App:description('Testing the MB implementation with passthrough fix.')\n"+
                "@source(type = 'http', receiver.url='http://localhost:8080/SweetProductionEP', @map(type = 'json'))\n"+
                "define stream Test1Stream (name string, amount double);\n"+
                "@sink(type='log')\n"+
                "define stream Test2Stream (name string, amount double);\n"+
                "@info(name = 'query1')@dist(parallel='3', execGroup='001')\n"+
                "Partition with (name of Test1Stream)\n"+
                "Begin\n"+
                "from Test1Stream\n"+
                "select name,amount\n"+
                "insert into Test2Stream;\n"+
                "end";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        for (DeployableSiddhiQueryGroup group : queryGroupList) {
            for (SiddhiQuery query : group.getSiddhiQueries()) {
                SiddhiManager siddhiManager = new SiddhiManager();
                siddhiManager.createSiddhiAppRuntime(query.getApp());
            }
        }
    }

    @Test
    public void testPassthroughInvocForWebinarApp(){
        String siddhiApp = "@App:name('sampleapp')\n"+
                "@App:description('Energy consumption and anomaly detection')\n"+
                "@source(type = 'http', topic = 'device-power', @map(type = 'json'))\n"+
                "define stream DevicePowerStream (type string, deviceID string, power int, roomID string);\n"+
                "@sink(type = 'log')\n"+
                "define stream AlertStream (deviceID string, roomID string, initialPower double, finalPower double,\n"+
                "autorityContactEmail string);\n"+
                "@info(name = 'monitered-filter')@dist(execGroup='001')\n"+
                "from DevicePowerStream[type == 'monitored']\n"+
                "select deviceID, power, roomID\n"+
                "insert current events into MonitoredDevicesPowerStream;\n"+
                "@info(name = 'power-increase-pattern')@dist(parallel='2', execGroup='002')\n"+
                "partition with (deviceID of MonitoredDevicesPowerStream)\n"+
                "begin\n"+
                "@info(name = 'avg-calculator')\n"+
                "from MonitoredDevicesPowerStream#window.time(2 min)\n"+
                "select deviceID, avg(power) as avgPower, roomID\n"+
                "insert current events into #AvgPowerStream;\n"+
                "@info(name = 'power-increase-detector')\n"+
                "from every e1 = #AvgPowerStream -> e2 = #AvgPowerStream[(e1.avgPower + 5) <= avgPower] within 10 min\n"+
                "select e1.deviceID as deviceID, e1.avgPower as initialPower, e2.avgPower as finalPower, e1.roomID\n"+
                "insert current events into RisingPowerStream;\n"+
                "end;\n"+
                "@info(name = 'power-range-filter')@dist(parallel='2', execGroup='003')\n"+
                "from RisingPowerStream[finalPower > 100]\n"+
                "select deviceID, roomID, initialPower, finalPower, 'no-reply@powermanagement.com' as autorityContactEmail\n"+
                "insert current events into AlertStream;\n"+
                "@info(name = 'internal-filter')@dist(execGroup='004')\n"+
                "from DevicePowerStream[type == 'internal']\n"+
                "select deviceID, power\n"+
                "insert current events into InternaltDevicesPowerStream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        for (DeployableSiddhiQueryGroup group : queryGroupList) {
            for (SiddhiQuery query : group.getSiddhiQueries()) {
                SiddhiManager siddhiManager = new SiddhiManager();
                siddhiManager.createSiddhiAppRuntime(query.getApp());
            }
        }

    }

    @Test
    public void testPassThroughFix4(){
        String siddhiApp = "@App:name('MB_Testcase_5')\n" +
                "@App:description('Testing the MB implementation with multiple FGs strategies.')\n" +
                "@source(type = 'http', receiver.url='https://0.0.0.0:9550/SweetProductionEP', @map(type = 'json'))\n"+
                "define stream Test1Stream (name string, amount double,value double);\n"+
                "define stream Test2Stream (name string, amount double, value double);\n"+

                "@Sink(type='log')\n"+
                "define stream Test3Stream (name string, amount double, value double);\n"+
                "define stream Test4Stream (name string, amount double, value double);\n"+
                "define stream Test5Stream (name string, amount double, value double);\n"+

                "@info(name = 'query1')@dist(parallel='1', execGroup='001')\n"+
                "from Test1Stream\n"+
                "select name,amount,value\n"+
                "insert into Test2Stream;\n"+


                "@info(name = 'partition1')@dist(parallel='3',execGroup='002')\n"+
                "partition with (name of Test2Stream)\n"+
                "begin\n"+
                "@info(name = 'avg-calculator1')\n"+
                "from Test2Stream\n"+
                "select name,amount,value\n"+
                "insert into Test3Stream;\n"+
                "end;\n"+


                "@info(name = 'partition2')@dist(parallel='4',execGroup='003')\n"+
                "partition with (amount of Test2Stream)\n"+
                "begin\n"+
                "@info(name = 'avg-calculator2')\n"+
                "from Test2Stream\n"+
                "select name,amount,value\n"+
                "insert into Test4Stream;\n"+
                "end;\n"+


                "@info(name = 'query2')@dist(execGroup='004')\n"+
                "from Test2Stream\n"+
                "select name,amount,value\n"+
                "insert into Test5Stream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        for (DeployableSiddhiQueryGroup group : queryGroupList) {
            for (SiddhiQuery query : group.getSiddhiQueries()) {
                SiddhiManager siddhiManager = new SiddhiManager();
                siddhiManager.createSiddhiAppRuntime(query.getApp());
            }
        }

    }

    @Test
    public void testPassThroughFix5(){
        String siddhiApp = "@App:name('MB_Testcase_5')\n" +
                "@App:description('Testing the MB implementation with multiple FGs strategies.')\n" +
                "@source(type = 'http', receiver.url='https://0.0.0.0:9550/SweetProductionEP', @map(type = 'json'))\n"+
                "define stream Test1Stream (name string, amount double,value double);\n"+
                "define stream Test2Stream (name string, amount double, value double);\n"+

                "@Sink(type='log')\n"+
                "define stream Test3Stream (name string, amount double, value double);\n"+
                "define stream Test4Stream (name string, amount double, value double);\n"+
                "define stream Test5Stream (name string, amount double, value double);\n"+

                "@info(name = 'query1')@dist(parallel='2', execGroup='001')\n"+
                "from Test1Stream\n"+
                "select name,amount,value\n"+
                "insert into Test2Stream;\n"+

                "@info(name = 'partition1')@dist(parallel='3',execGroup='002')\n"+
                "partition with (name of Test2Stream)\n"+
                "begin\n"+
                "@info(name = 'avg-calculator1')\n"+
                "from Test2Stream\n"+
                "select name,amount,value\n"+
                "insert into Test3Stream;\n"+
                "end;\n"+

                "@info(name = 'partition2')@dist(parallel='4',execGroup='003')\n"+
                "partition with (amount of Test2Stream)\n"+
                "begin\n"+
                "@info(name = 'avg-calculator2')\n"+
                "from Test2Stream\n"+
                "select name,amount,value\n"+
                "insert into Test4Stream;\n"+
                "end;\n"+


                "@info(name = 'query2')@dist(execGroup='004')\n"+
                "from Test2Stream\n"+
                "select name,amount,value\n"+
                "insert into Test5Stream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        for (DeployableSiddhiQueryGroup group : queryGroupList) {
            for (SiddhiQuery query : group.getSiddhiQueries()) {
                SiddhiManager siddhiManager = new SiddhiManager();
                siddhiManager.createSiddhiAppRuntime(query.getApp());
            }
        }

    }

    @Test
    public void testPassThroughFix6(){
        String siddhiApp = "@App:name('MB_Testcase_5')\n" +
                "@App:description('Testing the MB implementation with multiple FGs strategies.')\n" +
                "@source(type = 'http', receiver.url='https://0.0.0.0:9550/SweetProductionEP', @map(type = 'json'))\n"+
                "define stream Test1Stream (name string, amount double,value double);\n"+
                "define stream Test2Stream (name string, amount double, value double);\n"+

                "@Sink(type='log')\n"+
                "define stream Test3Stream (name string, amount double, value double);\n"+
                "define stream Test4Stream (name string, amount double, value double);\n"+
                "define stream Test5Stream (name string, amount double, value double);\n"+

                "@info(name = 'query1')@dist(parallel='2', execGroup='001')\n"+
                "from Test1Stream\n"+
                "select name,amount,value\n"+
                "insert into Test2Stream;\n"+


                "@info(name = 'partition1')@dist(parallel='3',execGroup='002')\n"+
                "partition with (name of Test1Stream)\n"+
                "begin\n"+
                "@info(name = 'avg-calculator1')\n"+
                "from Test1Stream\n"+
                "select name,amount,value\n"+
                "insert into Test3Stream;\n"+
                "end;\n"+


                "@info(name = 'partition2')@dist(parallel='4',execGroup='003')\n"+
                "partition with (amount of Test2Stream)\n"+
                "begin\n"+
                "@info(name = 'avg-calculator2')\n"+
                "from Test2Stream\n"+
                "select name,amount,value\n"+
                "insert into Test4Stream;\n"+
                "end;\n"+


                "@info(name = 'query2')@dist(execGroup='004')\n"+
                "from Test2Stream\n"+
                "select name,amount,value\n"+
                "insert into Test5Stream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);


        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains(SiddhiTopologyCreatorConstants.PASSTHROUGH),
                "Two passthrough queries should be present in separate groups");

        Assert.assertTrue(queryGroupList.get(0).getSiddhiQueries().get(0).getApp().contains("@source(type = 'http', receiver.url='https://0.0.0.0:9550/SweetProductionEP', @map(type = 'json'))\n" +
                "define stream passthroughTest1Stream (name string, amount double,value double);\n" +
                "@sink(type='kafka', topic='MB_Testcase_5.Test1Stream' , bootstrap.servers='localhost:9092', @map(type='xml'))\n" +
                "@sink(type='kafka', topic='MB_Testcase_5.Test1Stream.name' , bootstrap.servers='localhost:9092', @map(type='xml'), @distribution(strategy='partitioned', partitionKey='name', @destination(partition.no = '0'),@destination(partition.no = '1'),@destination(partition.no = '2') )) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "from passthroughTest1Stream select * insert into Test1Stream;"),"Incorrect query created");

        Assert.assertTrue(queryGroupList.get(1).getSiddhiQueries().get(0).getApp().contains("@App:name('MB_Testcase_5-001-1') \n" +
                "@source(type='kafka', topic.list='MB_Testcase_5.Test1Stream', group.id='MB_Testcase_5-001', threading.option='single.thread', bootstrap.servers='localhost:9092', @map(type='xml')) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "@sink(type='kafka', topic='MB_Testcase_5.Test2Stream' , bootstrap.servers='localhost:9092', @map(type='xml'))\n" +
                "@sink(type='kafka', topic='MB_Testcase_5.Test2Stream.amount' , bootstrap.servers='localhost:9092', @map(type='xml'), @distribution(strategy='partitioned', partitionKey='amount', @destination(partition.no = '0'),@destination(partition.no = '1'),@destination(partition.no = '2'),@destination(partition.no = '3') ))define stream Test2Stream (name string, amount double, value double);\n" +
                "@info(name = 'query1')\n" +
                "from Test1Stream\n" +
                "select name,amount,value\n" +
                "insert into Test2Stream;"),"Incorrect query generated");



        for (DeployableSiddhiQueryGroup group : queryGroupList) {
            for (SiddhiQuery query : group.getSiddhiQueries()) {
                SiddhiManager siddhiManager = new SiddhiManager();
                siddhiManager.createSiddhiAppRuntime(query.getApp());
            }
        }

    }

    @Test
    public void testPassThroughFix7(){
        String siddhiApp = "@App:name('MB_Testcase_7')\n" +
                "@App:description('Testing the MB implementation with multiple FGs strategies.')\n" +

                "@source(type = 'http', receiver.url='https://0.0.0.0:9550/SweetProductionEP', @map(type = 'json'))\n"+
                "define stream Test1Stream (name string, amount double,value double);\n"+
                "define stream Test2Stream (name string, amount double, value double);\n"+

                "@Sink(type='log')\n"+
                "define stream Test3Stream (name string, amount double, value double);\n"+
                "define stream Test4Stream (name string, amount double, value double);\n"+
                "define stream Test5Stream (name string, amount double, value double);\n"+

                "@info(name = 'query1')@dist(parallel='2', execGroup='001')\n"+
                "from Test1Stream\n"+
                "select name,amount,value\n"+
                "insert into Test2Stream;\n"+

                "@info(name = 'query3')@dist(parallel='3',execGroup='002')\n"+
                "from Test1Stream\n"+
                "select name,amount,value\n"+
                "insert into Test3Stream;\n"+


                "@info(name = 'partition2')@dist(parallel='4',execGroup='003')\n"+
                "partition with (amount of Test2Stream)\n"+
                "begin\n"+
                "@info(name = 'avg-calculator2')\n"+
                "from Test2Stream\n"+
                "select name,amount,value\n"+
                "insert into Test4Stream;\n"+
                "end;\n"+


                "@info(name = 'query2')@dist(execGroup='004')\n"+
                "from Test2Stream\n"+
                "select name,amount,value\n"+
                "insert into Test5Stream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);

        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains(SiddhiTopologyCreatorConstants.PASSTHROUGH),
                "Two passthrough queries should be present in separate groups");

        Assert.assertTrue(queryGroupList.get(0).getSiddhiQueries().get(0).getApp().contains("@source(type = 'http', receiver.url='https://0.0.0.0:9550/SweetProductionEP', @map(type = 'json'))\n" +
                "define stream passthroughTest1Stream (name string, amount double,value double);\n" +
                "@sink(type='kafka', topic='MB_Testcase_7.Test1Stream' , bootstrap.servers='localhost:9092', @map(type='xml')) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "from passthroughTest1Stream select * insert into Test1Stream;"),"Incorrect query generated");

        Assert.assertTrue(queryGroupList.get(1).getSiddhiQueries().get(0).getApp().contains("@App:name('MB_Testcase_7-001-1') \n" +
                "@source(type='kafka', topic.list='MB_Testcase_7.Test1Stream', group.id='MB_Testcase_7-001', threading.option='single.thread', bootstrap.servers='localhost:9092', @map(type='xml')) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "@sink(type='kafka', topic='MB_Testcase_7.Test2Stream' , bootstrap.servers='localhost:9092', @map(type='xml'))\n" +
                "@sink(type='kafka', topic='MB_Testcase_7.Test2Stream.amount' , bootstrap.servers='localhost:9092', @map(type='xml'), @distribution(strategy='partitioned', partitionKey='amount', @destination(partition.no = '0'),@destination(partition.no = '1'),@destination(partition.no = '2'),@destination(partition.no = '3') ))define stream Test2Stream (name string, amount double, value double);\n" +
                "@info(name = 'query1')\n" +
                "from Test1Stream\n" +
                "select name,amount,value\n" +
                "insert into Test2Stream;"),"Incorrect query generated");

    }

    @Test
    public void testPassThroughFix8(){
        String siddhiApp = "@App:name('MB_Testcase_7')\n" +
                "@App:description('Testing the MB implementation with multiple FGs strategies.')\n" +
                "@source(type = 'http', receiver.url='https://0.0.0.0:9550/SweetProductionEP', @map(type = 'json'))\n"+
                "define stream Test1Stream (name string, amount double,value double);\n"+
                "define stream Test2Stream (name string, amount double, value double);\n"+
                "@Sink(type='log')\n"+
                "define stream Test3Stream (name string, amount double, value double);\n"+
                "define stream Test4Stream (name string, amount double, value double);\n"+
                "define stream Test5Stream (name string, amount double, value double);\n"+

                "@info(name = 'partition1')@dist(parallel='3',execGroup='002')\n"+
                "partition with (amount of Test1Stream)\n"+
                "begin\n"+
                "@info(name = 'avg-calculator1')\n"+
                "from Test1Stream\n"+
                "select name,amount,value\n"+
                "insert into Test3Stream;\n"+
                "end;\n"+


                "@info(name = 'partition2')@dist(parallel='4',execGroup='003')\n"+
                "partition with (amount of Test1Stream)\n"+
                "begin\n"+
                "@info(name = 'avg-calculator2')\n"+
                "from Test1Stream\n"+
                "select name,amount,value\n"+
                "insert into Test4Stream;\n"+
                "end;\n"+


                "@info(name = 'query2')@dist(execGroup='004')\n"+
                "from Test2Stream\n"+
                "select name,amount,value\n"+
                "insert into Test5Stream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);


        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains(SiddhiTopologyCreatorConstants.PASSTHROUGH),
                "Two passthrough queries should be present in separate groups");

        Assert.assertTrue(queryGroupList.get(0).getSiddhiQueries().get(0).getApp().contains("@source(type = 'http', receiver.url='https://0.0.0.0:9550/SweetProductionEP', @map(type = 'json'))\n" +
                "define stream passthroughTest1Stream (name string, amount double,value double);\n" +
                "@sink(type='kafka', topic='MB_Testcase_7.Test1Stream.amount' , bootstrap.servers='localhost:9092', @map(type='xml'), @distribution(strategy='partitioned', partitionKey='amount', @destination(partition.no = '0'),@destination(partition.no = '1'),@destination(partition.no = '2'),@destination(partition.no = '3') )) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "from passthroughTest1Stream select * insert into Test1Stream;"),"Incorrect query generated");

        Assert.assertTrue(queryGroupList.get(1).getSiddhiQueries().get(0).getApp().contains("@App:name('MB_Testcase_7-002-1') \n" +
                "@source(type='kafka', topic.list='MB_Testcase_7.Test1Stream.amount', group.id='MB_Testcase_7-002', threading.option='partition.wise', bootstrap.servers='localhost:9092', partition.no.list='0',@map(type='xml')) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "@Sink(type='log')\n" +
                "define stream Test3Stream (name string, amount double, value double);\n" +
                "@info(name = 'partition1')\n" +
                "partition with (amount of Test1Stream)\n" +
                "begin\n" +
                "@info(name = 'avg-calculator1')\n" +
                "from Test1Stream\n" +
                "select name,amount,value\n" +
                "insert into Test3Stream;\n" +
                "end;"),"Incorrect query generated");

        Assert.assertTrue(queryGroupList.get(2).getSiddhiQueries().get(3).getApp().contains("@App:name('MB_Testcase_7-003-4') \n" +
                "@source(type='kafka', topic.list='MB_Testcase_7.Test1Stream.amount', group.id='MB_Testcase_7-003', threading.option='partition.wise', bootstrap.servers='localhost:9092', partition.no.list='3',@map(type='xml')) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "define stream Test4Stream (name string, amount double, value double);\n" +
                "@info(name = 'partition2')\n" +
                "partition with (amount of Test1Stream)\n" +
                "begin\n" +
                "@info(name = 'avg-calculator2')\n" +
                "from Test1Stream\n" +
                "select name,amount,value\n" +
                "insert into Test4Stream;\n" +
                "end;"),"Incorrect query generated");



    }

    @Test
    public void testPassThroughFix9() {
        String siddhiApp = "@App:name('MB_Testcase_7')\n" +
                "@App:description('Testing the MB implementation with multiple FGs strategies.')\n" +
                "@source(type = 'http', receiver.url='https://0.0.0.0:9550/SweetProductionEP', @map(type = 'json'))\n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "define stream Test2Stream (name string, amount double, value double);\n" +
                "@Sink(type='log')\n" +
                "define stream Test3Stream (name string, amount double, value double);\n" +
                "define stream Test4Stream (name string, amount double, value double);\n" +
                "define stream Test5Stream (name string, amount double, value double);\n" +


                "@info(name = 'partition1')@dist(parallel='3',execGroup='002')\n" +
                "partition with (amount of Test1Stream)\n" +
                "begin\n" +
                "@info(name = 'avg-calculator1')\n" +
                "from Test1Stream\n" +
                "select name,amount,value\n" +
                "insert into Test3Stream;\n" +
                "end;\n" +


                "@info(name = 'partition2')@dist(parallel='4',execGroup='003')\n" +
                "partition with (name of Test1Stream)\n" +
                "begin\n" +
                "@info(name = 'avg-calculator2')\n" +
                "from Test1Stream\n" +
                "select name,amount,value\n" +
                "insert into Test4Stream;\n" +
                "end;\n" +


                "@info(name = 'query2')@dist(execGroup='004')\n" +
                "from Test2Stream\n" +
                "select name,amount,value\n" +
                "insert into Test5Stream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);




    }

    @Test
    public void testPassThroughFix10(){
        String siddhiApp = "@App:name('MB_Testcase_7')\n" +
                "@App:description('Testing the MB implementation with multiple FGs strategies.')\n" +
                "@source(type = 'http', receiver.url='https://0.0.0.0:9550/SweetProductionEP', @map(type = 'json'))\n"+
                "define stream Test1Stream (name string, amount double,value double);\n"+
                "define stream Test2Stream (name string, amount double, value double);\n"+
                "@Sink(type='log')\n"+
                "define stream Test3Stream (name string, amount double, value double);\n"+
                "define stream Test4Stream (name string, amount double, value double);\n"+
                "define stream Test5Stream (name string, amount double, value double);\n"+


                "@info(name = 'partition1')@dist(parallel='3',execGroup='002')\n"+
                "partition with (amount of Test1Stream)\n"+
                "begin\n"+
                "@info(name = 'avg-calculator1')\n"+
                "from Test1Stream\n"+
                "select name,amount,value\n"+
                "insert into Test3Stream;\n"+
                "end;\n"+


                "@info(name = 'partition2')@dist(parallel='4',execGroup='003')\n"+
                "partition with (name of Test1Stream)\n"+
                "begin\n"+
                "@info(name = 'avg-calculator2')\n"+
                "from Test1Stream\n"+
                "select name,amount,value\n"+
                "insert into Test4Stream;\n"+
                "end;\n"+


                "@info(name = 'query2')@dist(execGroup='004')\n"+
                "from Test1Stream\n"+
                "select name,amount,value\n"+
                "insert into Test5Stream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains(SiddhiTopologyCreatorConstants.PASSTHROUGH),
                "Two passthrough queries should be present in separate groups");

        Assert.assertTrue(queryGroupList.get(0).getSiddhiQueries().get(0).getApp().contains("@source(type = 'http', receiver.url='https://0.0.0.0:9550/SweetProductionEP', @map(type = 'json'))\n" +
                "define stream passthroughTest1Stream (name string, amount double,value double);\n" +
                "@sink(type='kafka', topic='MB_Testcase_7.Test1Stream.name' , bootstrap.servers='localhost:9092', @map(type='xml'), @distribution(strategy='partitioned', partitionKey='name', @destination(partition.no = '0'),@destination(partition.no = '1'),@destination(partition.no = '2'),@destination(partition.no = '3') ))\n" +
                "@sink(type='kafka', topic='MB_Testcase_7.Test1Stream.amount' , bootstrap.servers='localhost:9092', @map(type='xml'), @distribution(strategy='partitioned', partitionKey='amount', @destination(partition.no = '0'),@destination(partition.no = '1'),@destination(partition.no = '2') )) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "from passthroughTest1Stream select * insert into Test1Stream;"),"Incorrect query generated");

        Assert.assertTrue(queryGroupList.get(1).getSiddhiQueries().get(0).getApp().contains("@App:name('MB_Testcase_7-002-1') \n" +
                "@source(type='kafka', topic.list='MB_Testcase_7.Test1Stream.amount', group.id='MB_Testcase_7-002', threading.option='partition.wise', bootstrap.servers='localhost:9092', partition.no.list='0',@map(type='xml')) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "@Sink(type='log')\n" +
                "define stream Test3Stream (name string, amount double, value double);\n" +
                "@info(name = 'partition1')\n" +
                "partition with (amount of Test1Stream)\n" +
                "begin\n" +
                "@info(name = 'avg-calculator1')\n" +
                "from Test1Stream\n" +
                "select name,amount,value\n" +
                "insert into Test3Stream;\n" +
                "end;\n"),"Incorrect query generated");

        Assert.assertTrue(queryGroupList.get(1).getSiddhiQueries().get(2).getApp().contains("@App:name('MB_Testcase_7-002-3') \n" +
                "@source(type='kafka', topic.list='MB_Testcase_7.Test1Stream.amount', group.id='MB_Testcase_7-002', threading.option='partition.wise', bootstrap.servers='localhost:9092', partition.no.list='2',@map(type='xml')) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "@Sink(type='log')\n" +
                "define stream Test3Stream (name string, amount double, value double);\n" +
                "@info(name = 'partition1')\n" +
                "partition with (amount of Test1Stream)\n" +
                "begin\n" +
                "@info(name = 'avg-calculator1')\n" +
                "from Test1Stream\n" +
                "select name,amount,value\n" +
                "insert into Test3Stream;\n" +
                "end;\n"),"Incorrect query generated");


        Assert.assertTrue(queryGroupList.get(2).getSiddhiQueries().get(0).getApp().contains("@App:name('MB_Testcase_7-003-1') \n" +
                "@source(type='kafka', topic.list='MB_Testcase_7.Test1Stream.name', group.id='MB_Testcase_7-003', threading.option='partition.wise', bootstrap.servers='localhost:9092', partition.no.list='0',@map(type='xml')) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "define stream Test4Stream (name string, amount double, value double);\n" +
                "@info(name = 'partition2')\n" +
                "partition with (name of Test1Stream)\n" +
                "begin\n" +
                "@info(name = 'avg-calculator2')\n" +
                "from Test1Stream\n" +
                "select name,amount,value\n" +
                "insert into Test4Stream;\n" +
                "end;\n"),"Incorrect query generated");


        Assert.assertTrue(queryGroupList.get(3).getSiddhiQueries().get(0).getApp().contains("@App:name('MB_Testcase_7-004-1') \n" +
                "@source(type='kafka', topic.list='MB_Testcase_7.Test1Stream.name', group.id='MB_Testcase_7-004-0', threading.option='single.thread', bootstrap.servers='localhost:9092', @map(type='xml')) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "define stream Test5Stream (name string, amount double, value double);\n" +
                "@info(name = 'query2')\n" +
                "from Test1Stream\n" +
                "select name,amount,value\n" +
                "insert into Test5Stream;"),"Incorrect query generated");
    }

    @Test
    public void testPassThroughFix11(){
        String siddhiApp = "@App:name('MB_Testcase_7')\n" +
                "@App:description('Testing the MB implementation with multiple FGs strategies.')\n" +
                "@source(type = 'http', receiver.url='https://0.0.0.0:9550/SweetProductionEP', @map(type = 'json'))\n"+
                "define stream Test1Stream (name string, amount double,value double);\n"+
                "define stream Test2Stream (name string, amount double, value double);\n"+
                "@Sink(type='log')\n"+
                "define stream Test3Stream (name string, amount double, value double);\n"+
                "define stream Test4Stream (name string, amount double, value double);\n"+
                "define stream Test5Stream (name string, amount double, value double);\n"+



                "@info(name = 'query2')@dist(execGroup='004')\n"+
                "from Test1Stream\n"+
                "select name,amount,value\n"+
                "insert into Test5Stream;"+




                "@info(name = 'partition1')@dist(parallel='3',execGroup='002')\n"+
                "partition with (amount of Test1Stream)\n"+
                "begin\n"+
                "@info(name = 'avg-calculator1')\n"+
                "from Test1Stream\n"+
                "select name,amount,value\n"+
                "insert into Test3Stream;\n"+
                "end;\n"+


                "@info(name = 'partition2')@dist(parallel='4',execGroup='003')\n"+
                "partition with (name of Test1Stream)\n"+
                "begin\n"+
                "@info(name = 'avg-calculator2')\n"+
                "from Test1Stream\n"+
                "select name,amount,value\n"+
                "insert into Test4Stream;\n"+
                "end;\n";




        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains(SiddhiTopologyCreatorConstants.PASSTHROUGH),
                "One passthrough query should be present");

        Assert.assertTrue(queryGroupList.get(0).getSiddhiQueries().get(0).getApp().contains("@source(type = 'http', receiver.url='https://0.0.0.0:9550/SweetProductionEP', @map(type = 'json'))\n" +
                "define stream passthroughTest1Stream (name string, amount double,value double);\n" +
                "@sink(type='kafka', topic='MB_Testcase_7.Test1Stream.name' , bootstrap.servers='localhost:9092', @map(type='xml'), @distribution(strategy='partitioned', partitionKey='name', @destination(partition.no = '0'),@destination(partition.no = '1'),@destination(partition.no = '2'),@destination(partition.no = '3') ))\n" +
                "@sink(type='kafka', topic='MB_Testcase_7.Test1Stream.amount' , bootstrap.servers='localhost:9092', @map(type='xml'), @distribution(strategy='partitioned', partitionKey='amount', @destination(partition.no = '0'),@destination(partition.no = '1'),@destination(partition.no = '2') )) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "from passthroughTest1Stream select * insert into Test1Stream;"),"Incorrect query generated");

        Assert.assertTrue(queryGroupList.get(2).getSiddhiQueries().get(0).getApp().contains("@App:name('MB_Testcase_7-002-1') \n" +
                "@source(type='kafka', topic.list='MB_Testcase_7.Test1Stream.amount', group.id='MB_Testcase_7-002', threading.option='partition.wise', bootstrap.servers='localhost:9092', partition.no.list='0',@map(type='xml')) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "@Sink(type='log')\n" +
                "define stream Test3Stream (name string, amount double, value double);\n" +
                "@info(name = 'partition1')\n" +
                "partition with (amount of Test1Stream)\n" +
                "begin\n" +
                "@info(name = 'avg-calculator1')\n" +
                "from Test1Stream\n" +
                "select name,amount,value\n" +
                "insert into Test3Stream;\n" +
                "end;\n"),"Incorrect query generated");

        Assert.assertTrue(queryGroupList.get(2).getSiddhiQueries().get(2).getApp().contains("@App:name('MB_Testcase_7-002-3') \n" +
                "@source(type='kafka', topic.list='MB_Testcase_7.Test1Stream.amount', group.id='MB_Testcase_7-002', threading.option='partition.wise', bootstrap.servers='localhost:9092', partition.no.list='2',@map(type='xml')) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "@Sink(type='log')\n" +
                "define stream Test3Stream (name string, amount double, value double);\n" +
                "@info(name = 'partition1')\n" +
                "partition with (amount of Test1Stream)\n" +
                "begin\n" +
                "@info(name = 'avg-calculator1')\n" +
                "from Test1Stream\n" +
                "select name,amount,value\n" +
                "insert into Test3Stream;\n" +
                "end;\n"),"Incorrect query generated");


        Assert.assertTrue(queryGroupList.get(3).getSiddhiQueries().get(0).getApp().contains("@App:name('MB_Testcase_7-003-1') \n" +
                "@source(type='kafka', topic.list='MB_Testcase_7.Test1Stream.name', group.id='MB_Testcase_7-003', threading.option='partition.wise', bootstrap.servers='localhost:9092', partition.no.list='0',@map(type='xml')) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "define stream Test4Stream (name string, amount double, value double);\n" +
                "@info(name = 'partition2')\n" +
                "partition with (name of Test1Stream)\n" +
                "begin\n" +
                "@info(name = 'avg-calculator2')\n" +
                "from Test1Stream\n" +
                "select name,amount,value\n" +
                "insert into Test4Stream;\n" +
                "end;\n"),"Incorrect query generated");


        Assert.assertTrue(queryGroupList.get(1).getSiddhiQueries().get(0).getApp().contains("@App:name('MB_Testcase_7-004-1') \n" +
                "@source(type='kafka', topic.list='MB_Testcase_7.Test1Stream', group.id='MB_Testcase_7-004-0', threading.option='single.thread', bootstrap.servers='localhost:9092', @map(type='xml')) \n" +
                "define stream Test1Stream (name string, amount double,value double);\n" +
                "define stream Test5Stream (name string, amount double, value double);\n" +
                "@info(name = 'query2')\n" +
                "from Test1Stream\n" +
                "select name,amount,value\n" +
                "insert into Test5Stream;"),"Incorrect query generated");
    }



    private Map<String, List<SiddhiAppRuntime>> createSiddhiAppRuntimes(
            SiddhiManager siddhiManager, List<DeployableSiddhiQueryGroup> queryGroupList) {
        Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap = new HashMap<>(queryGroupList.size());
        for (DeployableSiddhiQueryGroup group : queryGroupList) {
            List<SiddhiAppRuntime> runtimeList = new ArrayList<>(group.getSiddhiQueries().size());
            for (SiddhiQuery siddhiQuery : group.getSiddhiQueries()) {
                SiddhiAppRuntime runtime = siddhiManager.createSiddhiAppRuntime(siddhiQuery.getApp());
                runtime.start();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                runtimeList.add(runtime);
            }
            siddhiAppRuntimeMap.put(group.getGroupName(), runtimeList);
        }
        return siddhiAppRuntimeMap;
    }
}