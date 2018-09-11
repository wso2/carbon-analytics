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

public class SiddhiTopologyCreatorTestCase {
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
    public void testSiddhiTopologyCreator() {

        String siddhiApp = "@App:name('TestPlan1') \n"
                + "@source(type='kafka', topic.list='custom_topic', group.id='1', threading.option='single.thread', "
                + "bootstrap.servers='localhost:9092', @map(type='xml')) "
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "@Sink(type='email', @map(type='json'), username='wso2', address='test@wso2.com',password='****',"
                + "host='smtp.gmail.com',subject='Event from SP',to='towso2@gmail.com')\n"
                + "Define stream takingOverStream(symbol string, overtakingSymbol string, avgPrice double);\n"
                + "Define stream companyTriggerStream(symbol string);\n"
                + "@Store(type='rdbms', jdbc.url='jdbc:mysql://localhost:3306/cepDB',jdbc.driver.name='', "
                + "username='root', password='****',field.length='symbol:254')\n"
                + "Define table filteredTable (symbol string, price float, quantity int, tier string);\n"
                + "@Store(type='rdbms', jdbc.url='jdbc:mysql://localhost:3306/spDB',jdbc.driver.name='', "
                + "username='root', password='****',field.length='symbol:254')\n"
                + "Define table takingOverTable(symbol string, overtakingSymbol string, avgPrice double);\n"
                + "@info(name = 'query1')@dist(parallel='2', execGroup='001')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into filteredStockStream;\n"
                + "@info(name='query3')@dist(parallel='2', execGroup='002')\n"
                + "Partition with (symbol of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#window.time(5 min)\n"
                + "Select symbol, avg(price) as avgPrice, quantity\n"
                + "Insert into #avgPriceStream;\n"
                + "From #avgPriceStream#window.time(5 min) as a right outer join companyTriggerStream#window.length"
                + "(1)\n"
                + "On (companyTriggerStream.symbol == a.symbol)\n"
                + "Select a.symbol, a.avgPrice, a.quantity\n"
                + "Insert into triggeredAvgStream;\n"
                + "End;\n"
                + "@info(name='query4')@dist(parallel='1', execGroup='003')\n"
                + "From  a1=triggeredAvgStream,  a2=triggeredAvgStream[a1.avgPrice<a2.avgPrice]\n"
                + "Select a1.symbol, a2.symbol as overtakingSymbol, a2.avgPrice \n"
                + "Insert into takingOverStream;\n"
                + "@info(name='query5')@dist(parallel='4', execGroup='004')\n"
                + "From filteredStockStream\n"
                + "Select *\n"
                + "Insert into filteredTable;\n"
                + "@info(name='query6')@dist(parallel='4', execGroup='004')\n"
                + "From takingOverStream\n"
                + "Select *\n"
                + "Insert into takingOverTable;\n"
                + "@info(name='query7')@dist(parallel='3', execGroup='005')\n"
                + "Partition with (tier of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#log(tier)\n"
                + "Select *\n"
                + "Insert into dumbstream;\n"
                + "End;\n";

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

    /**
     * Filter query can reside in an execGroup with parallel > 1 and the corresponding stream will have
     * {@link TransportStrategy#ROUND_ROBIN}. Also transportChannelCreationEnabled is set to false and topics are
     * created manually. Apps should be able to connect to existing topics.
     */
    @Test(dependsOnMethods = "testSiddhiTopologyCreator")
    public void testFilterQuery() {
        String siddhiApp = "@App:name('TestPlan2')  @App:transportChannelCreationEnabled('false')"
                + "@source(type='kafka', topic.list='custom_topic', group.id='1', threading.option='single.thread', "
                + "bootstrap.servers='localhost:9092', @map(type='xml')) "
                + "define stream TempStream(deviceID long, roomNo int, temp double); "
                + "@info(name = 'query1') @dist(parallel ='1', execGroup='001')\n "
                + "from TempStream\n"
                + "select *\n"
                + "insert into TempInternalStream;"
                + "@info(name='query2')@dist(parallel='3',execGroup='002')\n"
                + "from TempInternalStream[(roomNo >= 100 and roomNo < 210) and temp > 40]\n"
                + "select roomNo, temp\n"
                + "insert into HighTempStream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams().get("TempInternalStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.ROUND_ROBIN);
        String topics[] = new String[]{"custom_topic", "TestPlan2.TempInternalStream"};
        KafkaTestUtil.createTopic(topics, 1);

        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);


        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap = createSiddhiAppRuntimes(siddhiManager,
                    queryGroupList);
            InputHandler tempStreamHandler =
                    siddhiAppRuntimeMap.get("TestPlan2-001").get(0).getInputHandler("TempStream");
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan2-002")) {
                runtime.addCallback("HighTempStream", new StreamCallback() {
                    @Override public void receive(Event[] events) {
                        EventPrinter.print(events);
                        count.addAndGet(events.length);
                    }
                });
            }
            tempStreamHandler.send(new Object[]{1, 110, 80});
            tempStreamHandler.send(new Object[]{1, 120, 60});
            tempStreamHandler.send(new Object[]{1, 140, 70});
            tempStreamHandler.send(new Object[]{1, 140, 30});

            SiddhiTestHelper.waitForEvents(100, 3, count, 2000);
            Assert.assertEquals(count.intValue(), 3);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }
    }

    /**
     *Window can can reside in an execGroup with parallel > 1 if the used stream is a (Partitioned/Inner) Stream.
     * This test will also validate ability to create topics and partitions on demand. Also
     * transportChannelCreationEnabled is set to false and topics are created manually. Apps should be able to
     * connect to existing topics.
     */
    @Test(dependsOnMethods = "testFilterQuery")
    public void testPartitionWithWindow() {
        String siddhiApp = "@App:name('TestPlan3')  @App:transportChannelCreationEnabled('false')"
                + "define stream TempStream(deviceID long, roomNo int, temp double); "
                + "@info(name = 'query1') @dist(parallel ='1', execGroup='group1')\n "
                + "from TempStream\n"
                + "select *\n"
                + "insert into TempInternalStream;"
                + "@info(name = 'query2') @dist(parallel ='2', execGroup='group2')\n "
                + "partition with ( deviceID of TempInternalStream )\n"
                + "begin\n"
                + "    from TempInternalStream#window.lengthBatch(2)\n"
                + "    select deviceID, roomNo, max(temp) as maxTemp\n"
                + "    insert into DeviceTempStream\n"
                + "end;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams().get("TempInternalStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.FIELD_GROUPING);
        String topics[] = new String[]{"TestPlan3.TempInternalStream.deviceID"};
        KafkaTestUtil.createTopic(topics, 2);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap = createSiddhiAppRuntimes(siddhiManager,
                    queryGroupList);
            InputHandler tempStreamHandler =
                    siddhiAppRuntimeMap.get("TestPlan3-group1").get(0).getInputHandler("TempStream");
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan3-group2")) {
                runtime.addCallback("DeviceTempStream", new StreamCallback() {
                    @Override public void receive(Event[] events) {
                        EventPrinter.print(events);
                        count.addAndGet(events.length);
                        for (Event event : events) {
                            if ((long) event.getData()[0] == 1L) {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[2], 80.0);
                                errorAssertionCount.decrementAndGet();
                            } else {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[2], 60.0);
                                errorAssertionCount.decrementAndGet();
                            }
                        }
                    }
                });
            }
            tempStreamHandler.send(new Object[]{1, 110, 70});
            tempStreamHandler.send(new Object[]{2, 120, 60});
            tempStreamHandler.send(new Object[]{1, 140, 80});
            tempStreamHandler.send(new Object[]{2, 140, 30});

            SiddhiTestHelper.waitForEvents(100, 2, count, 2000);
            Assert.assertEquals(count.intValue(), 2);
            Assert.assertEquals(errorAssertionCount.intValue(), 0, "No assertion errors should occur " +
                    "inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }
    }


    /**
     * Sequence can can reside in an execGroup with parallel > 1 if the used stream is a (Partitioned/Inner) Stream
     */
    @Test(dependsOnMethods = "testPartitionWithWindow")
    public void testPartitionWithSequence() {
        String siddhiApp = "@App:name('TestPlan4') "
                + "@source(type='kafka', topic.list='custom_topic', group.id='1', threading.option='single.thread', "
                + "bootstrap.servers='localhost:9092', @map(type='xml')) "
                + "define stream TempStream(deviceID long, roomNo int, temp double); "
                + "@info(name = 'query1') @dist(parallel ='1', execGroup='group1')\n "
                + "from TempStream\n"
                + "select *\n"
                + "insert into TempInternalStream;"
                + "@info(name ='query2') @dist(execGroup='group2', parallel='2')\n"
                + "partition with ( deviceID of TempInternalStream )\n"
                + "begin\n"
                + "from every e1=TempInternalStream, e2=TempInternalStream[e1.temp <= temp]+, "
                + "e3=TempInternalStream[e2[last].temp > temp]\n"
                + "select e1.deviceID, e1.temp as initialTemp, e2[last].temp as peakTemp\n"
                + "insert into PeakTempStream;"
                + "end;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        String topics[] = new String[]{"TestPlan4.TempInternalStream.deviceID"};
        KafkaTestUtil.createTopic(topics, 2);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap = createSiddhiAppRuntimes(siddhiManager,
                    queryGroupList);
            InputHandler tempStreamHandler =
                    siddhiAppRuntimeMap.get("TestPlan4-group1").get(0).getInputHandler("TempStream");
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan4-group2")) {
                runtime.addCallback("PeakTempStream", new StreamCallback() {
                    @Override public void receive(Event[] events) {
                        EventPrinter.print(events);
                        count.addAndGet(events.length);
                        for (Event event : events) {
                            if ((long) event.getData()[0] == 1L) {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[2], 80.0);
                                errorAssertionCount.decrementAndGet();
                            } else {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[2], 100.0);
                                errorAssertionCount.decrementAndGet();
                            }
                        }
                    }
                });
            }
            tempStreamHandler.send(new Object[]{1, 110, 40});
            tempStreamHandler.send(new Object[]{1, 120, 60});
            tempStreamHandler.send(new Object[]{1, 140, 80});
            tempStreamHandler.send(new Object[]{1, 140, 30});

            tempStreamHandler.send(new Object[]{2, 110, 40});
            tempStreamHandler.send(new Object[]{2, 120, 60});
            tempStreamHandler.send(new Object[]{2, 140, 100});
            tempStreamHandler.send(new Object[]{2, 140, 30});

            SiddhiTestHelper.waitForEvents(100, 2, count, 2000);
            Assert.assertEquals(count.intValue(), 2);
            Assert.assertEquals(errorAssertionCount.intValue(), 0, "No assertion errors should occur " +
                    "inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }
    }

    /**
     * Pattern can reside in an execGroup with parallel > 1 if the used stream is a (Partitioned/Inner) Stream
     */
    @Test(dependsOnMethods = "testPartitionWithSequence")
    public void testPartitionWithPattern() {
        String siddhiApp = "@App:name('TestPlan5') "
                + "define stream TempStream(deviceID long, roomNo int, temp double); "
                + "define stream RegulatorStream (deviceID long, roomNo int, tempSet double, isOn bool);\n"
                + "@info(name = 'query1') @dist(execGroup='group1', parallel='1')\n "
                + "from TempStream\n"
                + "select *\n"
                + "insert into TempInternalStream;"
                + "@info(name ='query2') @dist(execGroup='group2', parallel='2')\n"
                + "partition with ( deviceID of TempInternalStream )\n"
                + "begin\n"
                + "from every( e1=RegulatorStream) -> e2=TempInternalStream[e1.roomNo==roomNo]<1:> -> "
                + "e3=RegulatorStream[e1.roomNo==roomNo]\n"
                + "select e1.deviceID, e1.roomNo, e2[0].temp - e2[last].temp as tempDiff\n"
                + "insert into TempDiffStream;"
                + "end;";
        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        String topics[] = new String[]{"TestPlan5.RegulatorStream"};
        KafkaTestUtil.createTopic(topics, 1);
        Assert.assertTrue(topology.getQueryGroupList().get(0).getInputStreams().containsKey("TempStream"));
        Assert.assertTrue(topology.getQueryGroupList().get(1).getInputStreams().containsKey("RegulatorStream"));
        Assert.assertTrue(topology.getQueryGroupList().get(1).getOutputStreams().containsKey("TempDiffStream"));

        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap = createSiddhiAppRuntimes(siddhiManager,
                    queryGroupList);
            InputHandler tempStreamHandler =
                    siddhiAppRuntimeMap.get("TestPlan5-group1").get(0).getInputHandler("TempStream");
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan5-group2")) {
                runtime.addCallback("TempDiffStream", new StreamCallback() {
                    @Override public void receive(Event[] events) {
                        EventPrinter.print(events);
                        count.addAndGet(events.length);
                        for (Event event : events) {
                            if ((long) event.getData()[0] == 1L) {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[2], 25.0);
                                errorAssertionCount.decrementAndGet();
                            } else {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[2], 3.0);
                                errorAssertionCount.decrementAndGet();
                            }
                        }
                    }
                });
            }

            tempStreamHandler.send(new Object[]{1, 100, 30});
            tempStreamHandler.send(new Object[]{2, 100, 30});
            Thread.sleep(200);
            KafkaTestUtil.publish("TestPlan5.RegulatorStream",
                    Arrays.asList("<events><event><deviceID>1</deviceID><roomNo>100</roomNo><tempSet>30</tempSet>" +
                            "<isOn>true</isOn></event></events>"), 1, bootstrapSeverURL);
            Thread.sleep(100);
            tempStreamHandler.send(new Object[]{1, 100, 60});
            tempStreamHandler.send(new Object[]{1, 100, 50});
            tempStreamHandler.send(new Object[]{1, 100, 40});
            tempStreamHandler.send(new Object[]{1, 100, 35});
            KafkaTestUtil.publish("TestPlan5.RegulatorStream",
                    Arrays.asList("<events><event><deviceID>1</deviceID><roomNo>100</roomNo><tempSet>30</tempSet>" +
                            "<isOn>true</isOn></event></events>"), 1, bootstrapSeverURL);
            KafkaTestUtil.publish("TestPlan5.RegulatorStream",
                    Arrays.asList("<events><event><deviceID>2</deviceID><roomNo>101</roomNo><tempSet>35</tempSet>" +
                            "<isOn>true</isOn></event></events>"), 1, bootstrapSeverURL);
            tempStreamHandler.send(new Object[]{2, 101, 40});
            tempStreamHandler.send(new Object[]{2, 101, 39});
            tempStreamHandler.send(new Object[]{2, 101, 38});
            tempStreamHandler.send(new Object[]{2, 101, 37});
            KafkaTestUtil.publish("TestPlan5.RegulatorStream",
                    Arrays.asList("<events><event><deviceID>2</deviceID><roomNo>101</roomNo><tempSet>35</tempSet>" +
                            "<isOn>true</isOn></event></events>"), 1, bootstrapSeverURL);

            SiddhiTestHelper.waitForEvents(100, 2, count, 2000);
            Assert.assertEquals(count.intValue(), 2);
            Assert.assertEquals(errorAssertionCount.intValue(), 0, "No assertion errors should occur " +
                    "inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }
    }


    /**
     * A join can exist with Parallel >1 if the joined stream consists at least one Partitioned Stream
     * The partitioned streams in the join will subscribe with {@link TransportStrategy#FIELD_GROUPING}
     * The unpartitioned streams in the join will subscribe with {@link TransportStrategy#ALL}
     */
    @Test(dependsOnMethods = "testPartitionWithPattern")
    public void testJoinWithPartition() {
        String siddhiApp = "@App:name('TestPlan6') "
                + "define stream TempStream(deviceID long, roomNo int, temp double); "
                + "define stream RegulatorStream(deviceID long, roomNo int, isOn bool);\n"
                + "@info(name = 'query1') @dist(execGroup='group1', parallel='1')\n "
                + "from TempStream\n"
                + "select *\n"
                + "insert into TempInternalStream;"
                + "@info(name ='query2') @dist(execGroup='group2', parallel='2')\n"
                + "partition with ( deviceID of TempInternalStream )\n"
                + "begin\n"
                + "from TempInternalStream[temp > 30.0]#window.time(1 min) as T\n"
                + "  join RegulatorStream[isOn == false]#window.length(1) as R\n"
                + "  on T.roomNo == R.roomNo\n"
                + "select T.roomNo, R.deviceID, 'start' as action\n"
                + "insert into RegulatorActionStream;"
                + "end;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        String topics[] = new String[]{"TestPlan6.RegulatorStream"};
        KafkaTestUtil.createTopic(topics, 1);

        Assert.assertEquals(topology.getQueryGroupList().get(0).getInputStreams().get("TempStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.ALL);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams().get("TempInternalStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.FIELD_GROUPING);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams().get("RegulatorStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.ALL);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams().get("TempInternalStream")
                .getSubscriptionStrategy().getOfferedParallelism(), 2);

        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap = createSiddhiAppRuntimes(siddhiManager,
                    queryGroupList);
            InputHandler tempStreamHandler =
                    siddhiAppRuntimeMap.get("TestPlan6-group1").get(0).getInputHandler("TempStream");
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan6-group2")) {
                runtime.addCallback("RegulatorActionStream", new StreamCallback() {
                    @Override public void receive(Event[] events) {
                        EventPrinter.print(events);
                        count.addAndGet(events.length);
                        for (Event event : events) {
                            if ((long) event.getData()[1] == 1) {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[2], "start");
                                errorAssertionCount.decrementAndGet();
                            } else {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[2], "start");
                                errorAssertionCount.decrementAndGet();
                            }
                        }
                    }
                });
            }

            tempStreamHandler.send(new Object[]{1, 100, 40});
            tempStreamHandler.send(new Object[]{2, 101, 45});
            Thread.sleep(200);
            KafkaTestUtil.publish("TestPlan6.RegulatorStream",
                    Arrays.asList("<events><event><deviceID>1</deviceID><roomNo>100</roomNo>" +
                            "<isOn>false</isOn></event></events>"), 1, bootstrapSeverURL);
            KafkaTestUtil.publish("TestPlan6.RegulatorStream",
                    Arrays.asList("<events><event><deviceID>2</deviceID><roomNo>101</roomNo>" +
                            "<isOn>false</isOn></event></events>"), 1, bootstrapSeverURL);

            SiddhiTestHelper.waitForEvents(100, 2, count, 2000);
            Assert.assertEquals(count.intValue(), 2);
            Assert.assertEquals(errorAssertionCount.intValue(), 0, "No assertion errors should occur " +
                    "inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }


    }


    /**
     * A partitioned stream used outside the Partition but inside the same execGroup will have the Subscription
     * strategy of {@link TransportStrategy#FIELD_GROUPING}
     */
    @Test(dependsOnMethods = "testJoinWithPartition")
    public void testPartitionStrategy() {
        AtomicInteger highTempStreamCount = new AtomicInteger(0);
        String siddhiApp = "@App:name('TestPlan7') "
                + "define stream TempStream(deviceID long, roomNo int, temp double); "
                + "define stream RegulatorStream(deviceID long, roomNo int, isOn bool);\n"
                + "@info(name = 'query1') @dist(execGroup='group1', parallel='1')\n "
                + "from TempStream\n"
                + "select *\n"
                + "insert into TempInternalStream;"
                + "@info(name='query3') @dist(execGroup='group2' ,parallel='2')\n"
                + "from TempInternalStream[(roomNo >= 100 and roomNo < 210) and temp > 40]\n"
                + "select roomNo, temp\n"
                + "insert into HighTempStream;"
                + "@info(name ='query2') @dist(execGroup='group2', parallel='2')\n"
                + "partition with ( deviceID of TempInternalStream )\n"
                + "begin\n"
                + "from TempInternalStream[temp > 30.0]#window.time(1 min) as T\n"
                + "  join RegulatorStream[isOn == false]#window.length(1) as R\n"
                + "  on T.roomNo == R.roomNo\n"
                + "select T.roomNo, R.deviceID, 'start' as action\n"
                + "insert into RegulatorActionStream;"
                + "end;";


        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        String topics[] = new String[]{"TestPlan7.RegulatorStream"};
        KafkaTestUtil.createTopic(topics, 1);

        Assert.assertEquals(topology.getQueryGroupList().get(0).getInputStreams().get("TempStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.ALL);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams().get("TempInternalStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.FIELD_GROUPING);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams().get("RegulatorStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.ALL);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams().get("TempInternalStream")
                .getSubscriptionStrategy().getOfferedParallelism(), 2);

        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap = createSiddhiAppRuntimes(siddhiManager,
                    queryGroupList);
            InputHandler tempStreamHandler =
                    siddhiAppRuntimeMap.get("TestPlan7-group1").get(0).getInputHandler("TempStream");
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan7-group2")) {
                runtime.addCallback("RegulatorActionStream", new StreamCallback() {
                    @Override public void receive(Event[] events) {
                        EventPrinter.print(events);
                        count.addAndGet(events.length);
                        for (Event event : events) {
                            if ((long) event.getData()[1] == 1) {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[2], "start");
                                errorAssertionCount.decrementAndGet();
                            } else {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[2], "start");
                                errorAssertionCount.decrementAndGet();
                            }
                        }
                    }
                });
                runtime.addCallback("HighTempStream", new StreamCallback() {
                    @Override public void receive(Event[] events) {
                        EventPrinter.print(events);
                        highTempStreamCount.addAndGet(events.length);
                        for (Event event : events) {
                            if ((int) event.getData()[0] == 100) {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[1], 40.0);
                                errorAssertionCount.decrementAndGet();
                            } else {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[1], 45.0);
                                Assert.assertEquals(event.getData()[0], 101);
                                errorAssertionCount.decrementAndGet();
                            }
                        }
                    }
                });
            }

            tempStreamHandler.send(new Object[]{1, 100, 40});
            tempStreamHandler.send(new Object[]{2, 101, 45});
            Thread.sleep(200);
            KafkaTestUtil.publish("TestPlan7.RegulatorStream",
                    Arrays.asList("<events><event><deviceID>1</deviceID><roomNo>100</roomNo>" +
                            "<isOn>false</isOn></event></events>"), 1, bootstrapSeverURL);
            KafkaTestUtil.publish("TestPlan7.RegulatorStream",
                    Arrays.asList("<events><event><deviceID>2</deviceID><roomNo>101</roomNo>" +
                            "<isOn>false</isOn></event></events>"), 1, bootstrapSeverURL);

            SiddhiTestHelper.waitForEvents(100, 2, count, 2000);
            Assert.assertEquals(count.intValue(), 2);
            Assert.assertEquals(errorAssertionCount.intValue(), 0, "No assertion errors should occur " +
                    "inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }
    }


    /**
     * A stream used by multiple partitions residing in different executionGroups and under same Partition key gets
     * assigned with the respective parallelism as as distinct publishing strategies.
     */
    @Test(dependsOnMethods = "testPartitionStrategy")
    public void testPartitionMultiSubscription() {
        AtomicInteger dumbStreamCount = new AtomicInteger(0);
        String siddhiApp = "@App:name('TestPlan8') \n"
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "Define stream companyTriggerStream(symbol string);\n"
                + "@info(name = 'query1')@dist(parallel='1', execGroup='000')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into filteredStockStream;\n"
                + "@info(name = 'query2')@dist(parallel='1', execGroup='000')\n"
                + "from companyTriggerStream\n"
                + "select *\n"
                + "insert into\n"
                + "companyTriggerInternalStream;\n"
                + "@info(name='query3')@dist(parallel='2',execGroup='001')\n"
                + "Partition with (symbol of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#window.lengthBatch(2)\n"
                + "Select symbol, avg(price) as avgPrice, quantity\n"
                + "Insert into #avgPriceStream;\n"
                + "From #avgPriceStream#window.time(5 min) as a right outer join companyTriggerInternalStream#window"
                + ".length"
                + "(1)\n"
                + "On (companyTriggerInternalStream.symbol == a.symbol)\n"
                + "Select a.symbol, a.avgPrice, a.quantity\n"
                + "Insert into triggeredAvgStream;\n"
                + "End;\n"
                + "@info(name='query4')@dist(parallel='3', execGroup='002')\n"
                + "Partition with (symbol of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#log(symbol)\n"
                + "Select *\n"
                + "Insert into dumbStream;\n"
                + "End;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);

        Assert.assertEquals(topology.getQueryGroupList().get(0).getInputStreams().get("stockStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.ALL);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams().get("filteredStockStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.FIELD_GROUPING);
        Assert.assertEquals(topology.getQueryGroupList().get(2).getInputStreams().get("filteredStockStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.FIELD_GROUPING);
        Assert.assertEquals(topology.getQueryGroupList().get(0).getOutputStreams().get("filteredStockStream")
                .getPublishingStrategyList().get(0).getParallelism(), 3);
        Assert.assertEquals(topology.getQueryGroupList().get(0).getOutputStreams().get("filteredStockStream")
                .getPublishingStrategyList().get(0).getGroupingField(), "symbol");



        Assert.assertEquals(topology.getQueryGroupList().get(0).getSiddhiApp(), "@App:name('${appName}') \n"
                + "${companyTriggerStream}"
                + "Define stream companyTriggerStream(symbol string);\n"
                + "${stockStream}"
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "${filteredStockStream}define stream filteredStockStream (symbol string, price float, quantity int,"
                + " tier string);\n"
                + "${companyTriggerInternalStream}define stream companyTriggerInternalStream (symbol string);\n"
                + "@info(name = 'query1')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into filteredStockStream;\n"
                + "@info(name = 'query2')\n"
                + "from companyTriggerStream\n"
                + "select *\n"
                + "insert into\n"
                + "companyTriggerInternalStream;\n");

        Assert.assertEquals(topology.getQueryGroupList().get(1).getSiddhiApp(), "@App:name('${appName}') \n"
                + "${companyTriggerInternalStream}define stream companyTriggerInternalStream (symbol string);\n"
                + "${filteredStockStream}define stream filteredStockStream (symbol string, price float, quantity int,"
                + " tier string);\n"
                + "${triggeredAvgStream}define stream triggeredAvgStream (symbol string, avgPrice double, quantity "
                + "int);\n"
                + "@info(name='query3')\n"
                + "Partition with (symbol of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#window.lengthBatch(2)\n"
                + "Select symbol, avg(price) as avgPrice, quantity\n"
                + "Insert into #avgPriceStream;\n"
                + "From #avgPriceStream#window.time(5 min) as a right outer join companyTriggerInternalStream#window"
                + ".length(1)\n"
                + "On (companyTriggerInternalStream.symbol == a.symbol)\n"
                + "Select a.symbol, a.avgPrice, a.quantity\n"
                + "Insert into triggeredAvgStream;\n"
                + "End;\n");

        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);

        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap = createSiddhiAppRuntimes(siddhiManager,
                    queryGroupList);
            InputHandler stockStreamHandler =
                    siddhiAppRuntimeMap.get("TestPlan8-000").get(0).getInputHandler("stockStream");
            InputHandler triggerStreamHandler =
                    siddhiAppRuntimeMap.get("TestPlan8-000").get(0).getInputHandler("companyTriggerStream");
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan8-001")) {
                runtime.addCallback("triggeredAvgStream", new StreamCallback() {
                    @Override public void receive(Event[] events) {
                        EventPrinter.print(events);
                        count.addAndGet(events.length);
                        for (Event event : events) {
                            if (event.getData()[0].equals("WSO2")) {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[1], 175.0);
                                errorAssertionCount.decrementAndGet();
                            }
                        }
                    }
                });
            }
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan8-002")) {
                runtime.addCallback("dumbStream", new StreamCallback() {
                    @Override public void receive(Event[] events) {
                        EventPrinter.print(events);
                        dumbStreamCount.addAndGet(events.length);
                        for (Event event : events) {
                            errorAssertionCount.incrementAndGet();
                            Assert.assertEquals(event.getData()[0], "WSO2");
                            errorAssertionCount.decrementAndGet();
                        }
                    }
                });
            }

            stockStreamHandler.send(new Object[]{"WSO2", 150F, 2, "technology"});
            stockStreamHandler.send(new Object[]{"WSO2", 200F, 2, "technology"});
            Thread.sleep(1000);
            triggerStreamHandler.send(new Object[]{"WSO2"});

            SiddhiTestHelper.waitForEvents(100, 1, count, 2000);
            SiddhiTestHelper.waitForEvents(100, 2, dumbStreamCount, 2000);
            Assert.assertEquals(count.intValue(), 1);
            Assert.assertEquals(dumbStreamCount.intValue(), 2);
            Assert.assertEquals(errorAssertionCount.intValue(), 0, "No assertion errors should occur " +
                    "inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }
    }


    /**
     * A stream used by multiple partitions residing in different executionGroups and different Partition key gets
     * assigned with the {@link TransportStrategy#FIELD_GROUPING} and corresponding parallelism.
     */
    @Test(dependsOnMethods = "testPartitionMultiSubscription")
    public void testPartitionWithMultiKey() {
        AtomicInteger dumbStreamCount = new AtomicInteger(0);
        String siddhiApp = "@App:name('TestPlan9') \n"
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "Define stream companyTriggerStream(symbol string);\n"
                + "@info(name = 'query1')@dist(parallel='1', execGroup='001')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into filteredStockStream;\n"
                + "@info(name = 'query2')@dist(parallel='1', execGroup='001')\n"
                + "from companyTriggerStream\n"
                + "select *\n"
                + "insert into\n"
                + "companyTriggerInternalStream;\n"
                + "@info(name='query3')@dist(parallel='2',execGroup='002')\n"
                + "Partition with (symbol of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#window.lengthBatch(4)\n"
                + "Select symbol, avg(price) as avgPrice, quantity\n"
                + "Insert into #avgPriceStream;\n"
                + "From #avgPriceStream#window.time(5 min) as a " +
                "right outer join companyTriggerInternalStream#window.length(1)\n"
                + "On (companyTriggerInternalStream.symbol == a.symbol)\n"
                + "Select a.symbol, a.avgPrice, a.quantity\n"
                + "Insert into triggeredAvgStream;\n"
                + "End;\n"
                + "@info(name='query4')@dist(parallel='3', execGroup='003')\n"
                + "Partition with (tier of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#window.lengthBatch(2)\n"
                + "Select tier, count() as eventCount\n"
                + "Insert into dumbStream;\n"
                + "End;\n";
        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);

        Assert.assertEquals(topology.getQueryGroupList().get(0).getInputStreams().get("stockStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.ALL);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams().get("filteredStockStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.FIELD_GROUPING);
        Assert.assertEquals(topology.getQueryGroupList().get(0).getOutputStreams().get("filteredStockStream")
                .getPublishingStrategyList().get(0).getGroupingField(), "symbol");
        Assert.assertEquals(topology.getQueryGroupList().get(0).getOutputStreams().get("filteredStockStream")
                .getPublishingStrategyList().get(1).getGroupingField(), "tier");

        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap = createSiddhiAppRuntimes(siddhiManager,
                    queryGroupList);
            InputHandler stockStreamHandler =
                    siddhiAppRuntimeMap.get("TestPlan9-001").get(0).getInputHandler("stockStream");
            InputHandler triggerStreamHandler =
                    siddhiAppRuntimeMap.get("TestPlan9-001").get(0).getInputHandler("companyTriggerStream");
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan9-002")) {
                runtime.addCallback("triggeredAvgStream", new StreamCallback() {
                    @Override public void receive(Event[] events) {
                        EventPrinter.print(events);
                        count.addAndGet(events.length);
                        for (Event event : events) {
                            if (event.getData()[0].equals("WSO2")) {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[1], 175.0);
                                errorAssertionCount.decrementAndGet();
                            }
                        }
                    }
                });
            }
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan9-003")) {
                runtime.addCallback("dumbStream", new StreamCallback() {
                    @Override public void receive(Event[] events) {
                        EventPrinter.print(events);
                        dumbStreamCount.addAndGet(events.length);
                        for (Event event : events) {
                            if (event.getData()[0].equals("middleware")) {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[1], 2L);
                                errorAssertionCount.decrementAndGet();
                            } else if (event.getData()[0].equals("language")) {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[1], 2L);
                                errorAssertionCount.decrementAndGet();
                            }
                        }
                    }
                });
            }

            stockStreamHandler.send(new Object[]{"WSO2", 150F, 2, "middleware"});
            stockStreamHandler.send(new Object[]{"WSO2", 200F, 2, "middleware"});
            stockStreamHandler.send(new Object[]{"WSO2", 150F, 2, "language"});
            stockStreamHandler.send(new Object[]{"WSO2", 200F, 2, "language"});
            Thread.sleep(1000);
            triggerStreamHandler.send(new Object[]{"WSO2"});

            SiddhiTestHelper.waitForEvents(100, 1, count, 2000);
            SiddhiTestHelper.waitForEvents(100, 2, dumbStreamCount, 2000);
            Assert.assertEquals(count.intValue(), 1);
            Assert.assertEquals(dumbStreamCount.intValue(), 2);
            Assert.assertEquals(errorAssertionCount.intValue(), 0, "No assertion errors should occur " +
                    "inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }
    }


    /**
     * user given Sink used in (parallel/multiple execGroups) will get assigned to a all the execGroups after Topology
     * creation
     */
    @Test(dependsOnMethods = "testPartitionWithMultiKey")
    public void testUserDefinedSink() {
        String siddhiApp = "@App:name('TestPlan10') \n"
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "@Sink(type='inMemory', topic='takingOverTopic', @map(type='passThrough'))\n"
                + "Define stream takingOverStream(symbol string, overtakingSymbol string, avgPrice double);\n"
                + "@info(name = 'query1')@dist(parallel='1', execGroup='001')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into filteredStockStream;\n"
                + "@info(name = 'query2')@dist(parallel='2', execGroup='002')"
                + "Partition with (symbol of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#window.lengthBatch(2)\n"
                + "Select symbol, tier as overtakingSymbol,avg(price) as avgPrice  \n"
                + "Insert into takingOverStream;\n"
                + "end;\n"
                + "@info(name ='query3')@dist(parallel='2', execGroup='003')\n"
                + "from filteredStockStream [price >250 and price <350]\n"
                + "Select \"XYZ\" as symbol, tier as overtakingSymbol ,avg(price) as avgPrice  \n"
                + "Insert into takingOverStream;\n";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);

        Assert.assertTrue(topology.getQueryGroupList().get(1).getOutputStreams().containsKey("takingOverStream"));
        Assert.assertTrue(topology.getQueryGroupList().get(2).getOutputStreams().containsKey("takingOverStream"));

        InMemoryBroker.Subscriber subscriptionTakingOver = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                count.incrementAndGet();
                Event event = (Event) msg;
                EventPrinter.print(new Event[]{event});
                if (event.getData()[0].equals("WSO2")) {
                    errorAssertionCount.incrementAndGet();
                    Assert.assertEquals(event.getData()[2], 175.0);
                    errorAssertionCount.decrementAndGet();
                } else if (event.getData()[0].equals("ABC")) {
                    errorAssertionCount.incrementAndGet();
                    Assert.assertEquals(event.getData()[2], 250.0);
                    errorAssertionCount.decrementAndGet();
                } else if (event.getData()[0].equals("XYZ")) {
                    errorAssertionCount.incrementAndGet();
                    Assert.assertEquals(event.getData()[2], 300.0);
                    errorAssertionCount.decrementAndGet();
                }
            }

            @Override
            public String getTopic() {
                return "takingOverTopic";
            }
        };
        InMemoryBroker.subscribe(subscriptionTakingOver);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap = createSiddhiAppRuntimes(siddhiManager,
                    queryGroupList);
            InputHandler stockStreamHandler =
                    siddhiAppRuntimeMap.get("TestPlan10-001").get(0).getInputHandler("stockStream");

            stockStreamHandler.send(new Object[]{"WSO2", 150F, 2, "middleware"});
            stockStreamHandler.send(new Object[]{"WSO2", 200F, 2, "middleware"});
            stockStreamHandler.send(new Object[]{"ABC", 300F, 2, "language"});
            stockStreamHandler.send(new Object[]{"ABC", 200F, 2, "language"});

            SiddhiTestHelper.waitForEvents(100, 3, count, 2000);
            Assert.assertEquals(count.intValue(), 3);
            Assert.assertEquals(errorAssertionCount.intValue(), 0, "No assertion errors should occur " +
                    "inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
            InMemoryBroker.unsubscribe(subscriptionTakingOver);
        }
    }


    /**
     * when a user defined sink stream is used as in an internal source stream, a placeholder corresponding to the
     * streamID will be added to the respective sink so that the placeholder will bridge the stream to the required
     * source.
     */
    @Test(dependsOnMethods = "testUserDefinedSink")
    public void testSinkStreamForSource() {

        String siddhiApp = "@App:name('TestPlan11')\n"
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "Define stream companyTriggerStream(symbol string);\n"
                + "@Sink(type='inMemory', topic='takingOverTopic', @map(type='passThrough'))\n"
                + "Define stream takingOverStream(symbol string, overtakingSymbol string, avgPrice double);\n"
                + "@Sink(type='inMemory', topic='takingOverTableTopic', @map(type='passThrough'))\n"
                + "Define stream takingOverTableStream(symbol string, overtakingSymbol string, avgPrice double);\n"
                + "@info(name = 'query1')@dist(parallel='1', execGroup='001')\n"
                + "From stockStream[price > 100]\n"
                + "Select * Insert into filteredStockStream;\n"
                + "@info(name='query3') @dist(parallel='3',execGroup='002')\n"
                + "Partition with (symbol of filteredStockStream)\n"
                + "Begin\n"
                + "From filteredStockStream#window.lengthBatch(2)\n"
                + "Select symbol, avg(price) as avgPrice, quantity Insert into avgPriceStream;\n"
                + "end;\n"
                + "@info(name='query4')@dist(parallel='1', execGroup='003')\n"
                + "From  avgPriceStream[avgPrice > 150]\n"
                + "Select symbol, symbol as overtakingSymbol, avgPrice Insert into "
                + "takingOverStream;\n"
                + "@info(name='query5')@dist(parallel='1', execGroup='004')\n"
                + "From takingOverStream Select * Insert into takingOverTableStream;\n";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);

        //checking assigned strategies
        Assert.assertEquals(topology.getQueryGroupList().get(0).getInputStreams().get("stockStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.ALL);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams().get("filteredStockStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.FIELD_GROUPING);
        Assert.assertEquals(topology.getQueryGroupList().get(2).getInputStreams().get("avgPriceStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.ALL);
        Assert.assertEquals(topology.getQueryGroupList().get(3).getInputStreams().get("takingOverStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.ALL);

        Assert.assertEquals(topology.getQueryGroupList().get(2).getOutputStreams().get("takingOverStream")
                .getStreamDefinition() + ";", "@Sink(type='inMemory', topic='takingOverTopic', "
                + "@map(type='passThrough'))\n"
                + "${takingOverStream} \n"
                + "Define stream takingOverStream(symbol string, overtakingSymbol string, "
                + "avgPrice double);");

        InMemoryBroker.Subscriber subscriptionTakingOver = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                count.incrementAndGet();
                Event event = (Event) msg;
                EventPrinter.print(new Event[]{event});
                if (event.getData()[0].equals("WSO2")) {
                    errorAssertionCount.incrementAndGet();
                    Assert.assertEquals(event.getData()[2], 175.0);
                    errorAssertionCount.decrementAndGet();
                } else if (event.getData()[0].equals("ABC")) {
                    errorAssertionCount.incrementAndGet();
                    Assert.assertEquals(event.getData()[2], 250.0);
                    errorAssertionCount.decrementAndGet();
                }
            }

            @Override
            public String getTopic() {
                return "takingOverTopic";
            }
        };
        InMemoryBroker.Subscriber subscriptionTakingOverTable = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                count.incrementAndGet();
                Event event = (Event) msg;
                EventPrinter.print(new Event[]{event});
                if (event.getData()[0].equals("WSO2")) {
                    errorAssertionCount.incrementAndGet();
                    Assert.assertEquals(event.getData()[2], 175.0);
                    errorAssertionCount.decrementAndGet();
                } else if (event.getData()[0].equals("ABC")) {
                    errorAssertionCount.incrementAndGet();
                    Assert.assertEquals(event.getData()[2], 250.0);
                    errorAssertionCount.decrementAndGet();
                }
            }

            @Override
            public String getTopic() {
                return "takingOverTableTopic";
            }
        };
        InMemoryBroker.subscribe(subscriptionTakingOver);
        InMemoryBroker.subscribe(subscriptionTakingOverTable);

        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap = createSiddhiAppRuntimes(siddhiManager,
                    queryGroupList);
            InputHandler stockStreamHandler =
                    siddhiAppRuntimeMap.get("TestPlan11-001").get(0).getInputHandler("stockStream");

            stockStreamHandler.send(new Object[]{"WSO2", 150F, 2, "middleware"});
            stockStreamHandler.send(new Object[]{"WSO2", 200F, 2, "middleware"});
            stockStreamHandler.send(new Object[]{"ABC", 300F, 2, "language"});
            stockStreamHandler.send(new Object[]{"ABC", 200F, 2, "language"});

            SiddhiTestHelper.waitForEvents(100, 3, count, 2000);
            Assert.assertEquals(count.intValue(), 4);
            Assert.assertEquals(errorAssertionCount.intValue(), 0, "No assertion errors should occur " +
                    "inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
            InMemoryBroker.unsubscribe(subscriptionTakingOver);
            InMemoryBroker.unsubscribe(subscriptionTakingOverTable);
        }

    }


    /**
     * when user given sources are located in more than 1 execGroup then a passthrough query will be added in a new
     * execGroup.Newly created execGroup will be moved to as the first element of already created passthrough queries
     */
    @Test(dependsOnMethods = "testSinkStreamForSource")
    public void testUsergivenSourceNoGroup() {
        String siddhiApp = "@App:name('TestPlan12') \n"
                + "@source(type='inMemory', topic='stock', @map(type='json'))  "
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "@source(type='inMemory', topic='companyTrigger', @map(type='json'))"
                + "Define stream companyTriggerStream(symbol string);\n"
                + "@info(name = 'query1')@dist(parallel='3', execGroup='001')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into filteredStockStream;\n"
                + "@info(name = 'query2')@dist(parallel='3', execGroup='001')\n"
                + "From companyTriggerStream \n"
                + "Select *\n"
                + "Insert into SymbolStream;\n"
                + "@info(name = 'query3')@dist(parallel='3', execGroup='002')\n"
                + "From stockStream\n"
                + "Select *\n"
                + "Insert into LowStockStream;\n"
                + "@info(name='query4')@dist(parallel='3', execGroup='002')\n"
                + "Partition with (symbol of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#window.lengthBatch(2)\n"
                + "Select symbol, avg(price) as avgPrice, quantity\n"
                + "Insert into #avgPriceStream;\n"
                + "From #avgPriceStream#window.time(5 min) as a right outer join companyTriggerStream#window.length"
                + "(1) \n"
                + "On (companyTriggerStream.symbol == a.symbol)\n"
                + "Select a.symbol, a.avgPrice, a.quantity, companyTriggerStream.symbol as sss\n"
                + "Insert into triggeredAvgStream;\n"
                + "End;\n";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        Assert.assertTrue(queryGroupList.size() == 4, "Four query groups should be created");
        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains(SiddhiTopologyCreatorConstants.PASSTHROUGH),
                "Two passthrough queries should be present in separate groups");
        Assert.assertTrue(queryGroupList.get(0).isReceiverQueryGroup(), "Receiver type should be set");
        Assert.assertTrue(queryGroupList.get(1).getGroupName().contains(SiddhiTopologyCreatorConstants.PASSTHROUGH),
                "Two passthrough queries should be present in separate groups");
        Assert.assertTrue(queryGroupList.get(1).isReceiverQueryGroup(), "Receiver type should be set");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap = createSiddhiAppRuntimes(siddhiManager,
                    queryGroupList);

            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan12-002")) {
                runtime.addCallback("triggeredAvgStream", new StreamCallback() {
                    @Override public void receive(Event[] events) {
                        EventPrinter.print(events);
                        count.addAndGet(events.length);
                        for (Event event : events) {
                            if (event.getData()[0].equals("WSO2")) {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[1], 225.0);
                                errorAssertionCount.decrementAndGet();
                            }
                        }
                    }
                });
            }


            InMemoryBroker.publish("stock", "{\"event\":{\"symbol\":\"WSO2\", \"price\":200, \"quantity\":20," +
                    " \"tier\":\"middleware\"}}");
            InMemoryBroker.publish("stock", "{\"event\":{\"symbol\":\"WSO2\", \"price\":250, \"quantity\":20," +
                    " \"tier\":\"middleware\"}}");
            Thread.sleep(1000);
            InMemoryBroker.publish("companyTrigger", "{\"event\":{\"symbol\":\"WSO2\"}}");

            SiddhiTestHelper.waitForEvents(100, 1, count, 2000);
            Assert.assertEquals(count.intValue(), 1);
            Assert.assertEquals(errorAssertionCount.intValue(), 0, "No assertion errors should occur " +
                    "inside callbacks");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }

    }

    /**
     * When user has disabled the topic creation and topics are not available app creation should fail.
     */
    @Test(dependsOnMethods = "testUsergivenSourceNoGroup",
            expectedExceptions = SiddhiAppCreationException.class)
    public void testPartitionTopicCreationDisabledWithNoTopics() throws InterruptedException {
        String siddhiApp = "@App:name('TestPlan13')  @App:transportChannelCreationEnabled('false')\n"
                + "@source(type='kafka', topic.list='TestPlan12.stockStream', group.id='1', threading.option='single"
                + ".thread', bootstrap.servers='localhost:9092', @map(type='xml'))  "
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "@source(type='kafka', topic.list='TestPlan12.companyTriggerStream', group.id='1', threading"
                + ".option='single"
                + ".thread', bootstrap.servers='localhost:9092', @map(type='xml'))"
                + "Define stream companyTriggerStream(symbol string);\n"
                + "@info(name = 'query1')@dist(parallel='2', execGroup='001')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into filteredStockStream;\n"
                + "@info(name = 'query2')@dist(parallel='2', execGroup='001')\n"
                + "From companyTriggerStream \n"
                + "Select *\n"
                + "Insert into SymbolStream;\n"
                + "@info(name = 'query3')@dist(parallel='3', execGroup='002')\n"
                + "From stockStream[price < 100]\n"
                + "Select *\n"
                + "Insert into LowStockStream;\n"
                + "@info(name='query4')@dist(parallel='3', execGroup='002')\n"
                + "Partition with (symbol of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#window.time(5 min)\n"
                + "Select symbol, avg(price) as avgPrice, quantity\n"
                + "Insert into #avgPriceStream;\n"
                + "From #avgPriceStream#window.time(5 min) as a right outer join companyTriggerStream#window.length"
                + "(1)\n"
                + "On (companyTriggerStream.symbol == a.symbol)\n"
                + "Select a.symbol, a.avgPrice, a.quantity\n"
                + "Insert into triggeredAvgStream;\n"
                + "End;\n";

        String topics[] = new String[]{"TestPlan12.stockStream", "TestPlan12.companyTriggerStream"};
        KafkaTestUtil.createTopic(topics, 1);
        Thread.sleep(1000);
        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        try {
            appCreator.createApps(topology);
            Assert.fail();
        } catch (SiddhiAppCreationException e) {
            Assert.assertTrue(e.getMessage().contains("User has disabled topic creation by setting " +
                    "transportChannelCreationEnabled property to false. Hence Siddhi App deployment will be aborted"));
            throw e;
        }

    }

    @Test(dependsOnMethods = "testPartitionTopicCreationDisabledWithNoTopics",
            expectedExceptions = SiddhiAppCreationException.class)
    public void testTopicCreationDisabledWithNoTopics() {
        String siddhiApp = "@App:name('TestPlan14')  @App:transportChannelCreationEnabled('false')"
                + "@source(type='kafka', topic.list='custom_topic14', group.id='1', threading.option='single.thread', "
                + "bootstrap.servers='localhost:9092', @map(type='xml')) "
                + "define stream TempStream(deviceID long, roomNo int, temp double); "
                + "@info(name = 'query1') @dist(parallel ='1', execGroup='001')\n "
                + "from TempStream\n"
                + "select *\n"
                + "insert into TempInternalStream;"
                + "@info(name='query2')@dist(parallel='3',execGroup='002')\n"
                + "from TempInternalStream[(roomNo >= 100 and roomNo < 210) and temp > 40]\n"
                + "select roomNo, temp\n"
                + "insert into HighTempStream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        try {
            siddhiTopologyCreator.createTopology(siddhiApp);
            Assert.fail();
        } catch (SiddhiAppCreationException e) {
            Assert.assertTrue(e.getMessage().contains("Topic(s) custom_topic14 creation failed. User has disabled topic" +
                    " creation by setting transportChannelCreationEnabled property to false"));
            throw e;
        }
    }

    @Test(dependsOnMethods = "testSinkStreamForSource")
    public void testUsergivenParallelSources() {
        String siddhiApp = "@App:name('TestPlan12') \n"
                + "@source(type='inMemory', topic='stock', @map(type='json'), @dist(parallel='2')) " +
                "@source(type='inMemory', topic='stock123', @map(type='json'), @dist(parallel='3'))  "
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "@source(type='inMemory', topic='companyTrigger', @map(type='json'))"
                + "Define stream companyTriggerStream(symbol string);\n"
                + "@info(name = 'query1')@dist(parallel='3', execGroup='001')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into filteredStockStream;\n"
                + "@info(name = 'query2')@dist(parallel='3', execGroup='001')\n"
                + "From companyTriggerStream \n"
                + "Select *\n"
                + "Insert into SymbolStream;\n"
                + "@info(name = 'query3')@dist(parallel='3', execGroup='002')\n"
                + "From stockStream\n"
                + "Select *\n"
                + "Insert into LowStockStream;\n"
                + "@info(name='query4')@dist(parallel='3', execGroup='002')\n"
                + "Partition with (symbol of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#window.lengthBatch(2)\n"
                + "Select symbol, avg(price) as avgPrice, quantity\n"
                + "Insert into #avgPriceStream;\n"
                + "From #avgPriceStream#window.time(5 min) as a right outer join companyTriggerStream#window.length"
                + "(1) \n"
                + "On (companyTriggerStream.symbol == a.symbol)\n"
                + "Select a.symbol, a.avgPrice, a.quantity, companyTriggerStream.symbol as sss\n"
                + "Insert into triggeredAvgStream;\n"
                + "End;\n";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new SPSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        Assert.assertTrue(queryGroupList.size() == 5, "Five query groups should be created");
        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains(SiddhiTopologyCreatorConstants.PASSTHROUGH),
                "Two passthrough queries should be present in separate groups");
        Assert.assertTrue(queryGroupList.get(0).isReceiverQueryGroup(), "Receiver type should be set");
        Assert.assertTrue(queryGroupList.get(1).getGroupName().contains(SiddhiTopologyCreatorConstants.PASSTHROUGH),
                "Two passthrough queries should be present in separate groups");
        Assert.assertTrue(queryGroupList.get(1).isReceiverQueryGroup(), "Receiver type should be set");
        Assert.assertTrue(queryGroupList.get(2).getGroupName().contains(SiddhiTopologyCreatorConstants.PASSTHROUGH),
                "Two passthrough queries should be present in separate groups");
        Assert.assertTrue(queryGroupList.get(2).isReceiverQueryGroup(), "Receiver type should be set");

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
