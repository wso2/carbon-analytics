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
import org.wso2.carbon.sp.jobmanager.core.util.NatsClient;
import org.wso2.carbon.sp.jobmanager.core.util.SiddhiTopologyCreatorConstants;
import org.wso2.carbon.sp.jobmanager.core.util.TransportStrategy;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiTestHelper;
import org.wso2.siddhi.core.util.transport.InMemoryBroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Create the distributed partial siddhi applications which communicates using nats broker.
 */
public class NatsAppCreatorTestCase {

    private static final Logger log = Logger.getLogger(NatsAppCreatorTestCase.class);
    private AtomicInteger count;
    private AtomicInteger errorAssertionCount;
    private static final String CLUSTER_ID = "test-cluster";
    private static final String NATS_SERVER_URL = "nats://localhost:4222";
    private NatsClient natsClient;

    @BeforeMethod
    public void setUp() {
        DeploymentConfig deploymentConfig = new DeploymentConfig();
        deploymentConfig.setClusterId(CLUSTER_ID);
        deploymentConfig.setNatsServerUrl(NATS_SERVER_URL);
        ServiceDataHolder.setDeploymentConfig(deploymentConfig);
        count = new AtomicInteger(0);
        errorAssertionCount = new AtomicInteger(0);
        natsClient = new NatsClient(CLUSTER_ID, NATS_SERVER_URL);
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

        Assert.assertEquals(queryGroupList.size(), 5);
        for (DeployableSiddhiQueryGroup group : queryGroupList) {
            for (SiddhiQuery query : group.getSiddhiQueries()) {
                SiddhiManager siddhiManager = new SiddhiManager();
                siddhiManager.createSiddhiAppRuntime(query.getApp());
            }
        }
    }

    /**
     * Filter query can reside in an execGroup with parallel > 1 and the corresponding stream
     * will have {@link TransportStrategy#ROUND_ROBIN}.
     */
    @Test(dependsOnMethods = "testSiddhiTopologyCreator")
    public void testFilterQuery() {
        String siddhiApp = "@App:name('TestPlan2')"
                + "@source(type = 'http', topic = 'device-power', @map(type = 'json'))\n"
                + "define stream TempStream(deviceID long, roomNo int, temp double);\n"
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
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams()
                .get("TempInternalStream").getSubscriptionStrategy().getStrategy(), TransportStrategy.ROUND_ROBIN);
        SiddhiAppCreator appCreator = new NatsSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap = createSiddhiAppRuntimes(siddhiManager,
                    queryGroupList);
            InputHandler tempStreamHandler = siddhiAppRuntimeMap.get("TestPlan2-001").get(0)
                            .getInputHandler("TempStream");
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

            SiddhiTestHelper.waitForEvents(2000, 3, count, 2000);
            Assert.assertEquals(count.intValue(), 3);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }
    }

    /**
     *Window can can reside in an execGroup with parallel > 1 if the used stream is a
     * (Partitioned/Inner) Stream.
     */
    @Test(dependsOnMethods = "testFilterQuery")
    public void testPartitionWithWindow() {
        String siddhiApp = "@App:name('TestPlan3')"
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
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams()
                .get("TempInternalStream").getSubscriptionStrategy()
                .getStrategy(), TransportStrategy.FIELD_GROUPING);
        SiddhiAppCreator appCreator = new NatsSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap = createSiddhiAppRuntimes(siddhiManager,
                    queryGroupList);
            InputHandler tempStreamHandler = siddhiAppRuntimeMap.get("TestPlan3-group1").get(0)
                    .getInputHandler("TempStream");
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan3-group2")) {
                runtime.addCallback("DeviceTempStream", new StreamCallback() {
                    @Override
                    public void receive(Event[] events) {
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

            SiddhiTestHelper.waitForEvents(2000, 2, count, 3000);
            Assert.assertEquals(count.intValue(), 2);
            Assert.assertEquals(errorAssertionCount.intValue(), 0,
                    "No assertion errors should occur inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }
    }

    /**
     * Sequence can can reside in an execGroup with parallel > 1 if the used stream is a
     * (Partitioned/Inner) Stream.
     */
    @Test(dependsOnMethods = "testPartitionWithWindow")
    public void testPartitionWithSequence() {
        String siddhiApp = "@App:name('TestPlan4')\n"
                + "@source(type = 'http', topic = 'device-power', @map(type = 'json'))\n"
                + "define stream TempStream(deviceID long, roomNo int, temp double);\n"
                + "@info(name = 'query1') @dist(parallel ='1', execGroup='group1')\n "
                + "from TempStream\n"
                + "select *\n"
                + "insert into TempInternalStream;\n"
                + "@info(name ='query2') @dist(execGroup='group2', parallel='2')\n"
                + "partition with ( deviceID of TempInternalStream )\n"
                + "begin\n"
                + "from every e1=TempInternalStream, e2=TempInternalStream[e1.temp <= temp],"
                + "e3=TempInternalStream[e2[last].temp > temp]\n"
                + "select e1.deviceID, e1.temp as initialTemp, e2[last].temp as peakTemp\n"
                + "insert into PeakTempStream;\n"
                + "end;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new NatsSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap =
                    createSiddhiAppRuntimes(siddhiManager, queryGroupList);
            InputHandler tempStreamHandler = siddhiAppRuntimeMap.get("TestPlan4-group1").get(0)
                    .getInputHandler("TempStream");
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

            SiddhiTestHelper.waitForEvents(2500, 2, count, 3000);
            Assert.assertEquals(count.intValue(), 2);
            Assert.assertEquals(errorAssertionCount.intValue(), 0,
                    "No assertion errors should occur inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }
    }

    /**
     * A join can exist with Parallel > 1 if the joined stream consists at least one
     * Partitioned Stream. The partitioned streams in the join will subscribe with
     * {@link TransportStrategy#FIELD_GROUPING}. The un-partitioned streams in the join will
     * subscribe with {@link TransportStrategy#ALL}
     */
    @Test//(dependsOnMethods = "testPartitionWithSequence")
    public void testJoinWithPartition() {
        String siddhiApp = "@App:name('TestPlan5') "
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

        Assert.assertEquals(topology.getQueryGroupList().get(0).getInputStreams().get("TempStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.ALL);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams()
                .get("TempInternalStream").getSubscriptionStrategy()
                .getStrategy(), TransportStrategy.FIELD_GROUPING);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams()
                .get("RegulatorStream").getSubscriptionStrategy()
                .getStrategy(), TransportStrategy.ALL);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams()
                .get("TempInternalStream").getSubscriptionStrategy()
                .getOfferedParallelism(), 2);

        SiddhiAppCreator appCreator = new NatsSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap =
                    createSiddhiAppRuntimes(siddhiManager, queryGroupList);
            InputHandler tempStreamHandler =
                    siddhiAppRuntimeMap.get("TestPlan5-group1").get(0)
                            .getInputHandler("TempStream");
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan5-group2")) {
                runtime.addCallback("RegulatorActionStream", new StreamCallback() {
                    @Override public void receive(Event[] events) {
                        EventPrinter.print(events);
                        count.addAndGet(events.length);
                        for (Event event : events) {
                            if ((long) event.getData()[1] == 1) {
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

            natsClient.connect();
            Thread.sleep(1000);

            natsClient.publish("TestPlan5_RegulatorStream", "<events>"
                    + "   <event>"
                    + "       <deviceID>1</deviceID>"
                    + "       <roomNo>100</roomNo>"
                    + "       <isOn>false</isOn>"
                    + "   </event>"
                    + "</events>");

            natsClient.publish("TestPlan5_RegulatorStream", "<events>"
                    + "   <event>"
                    + "       <deviceID>2</deviceID>"
                    + "       <roomNo>101</roomNo>"
                    + "       <isOn>false</isOn>"
                    + "   </event>"
                    + "</events>");

            SiddhiTestHelper.waitForEvents(2000, 2, count, 3000);
            Assert.assertEquals(count.intValue(), 2);
            Assert.assertEquals(errorAssertionCount.intValue(), 0,
                    "No assertion errors should occur inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }
    }

    /**
     * A partitioned stream used outside the Partition but inside the same execGroup will
     * have the Subscription strategy of {@link TransportStrategy#FIELD_GROUPING}.
     */
    @Test(dependsOnMethods = "testJoinWithPartition")
    public void testPartitionStrategy() {
        AtomicInteger highTempStreamCount = new AtomicInteger(0);
        String siddhiApp = "@App:name('TestPlan6') "
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

        Assert.assertEquals(topology.getQueryGroupList().get(0).getInputStreams().get("TempStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.ALL);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams()
                .get("TempInternalStream").getSubscriptionStrategy()
                .getStrategy(), TransportStrategy.FIELD_GROUPING);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams()
                .get("RegulatorStream").getSubscriptionStrategy()
                .getStrategy(), TransportStrategy.ALL);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams()
                .get("TempInternalStream").getSubscriptionStrategy()
                .getOfferedParallelism(), 2);

        SiddhiAppCreator appCreator = new NatsSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap =
                    createSiddhiAppRuntimes(siddhiManager, queryGroupList);
            InputHandler tempStreamHandler =
                    siddhiAppRuntimeMap.get("TestPlan6-group1").get(0)
                            .getInputHandler("TempStream");
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
            natsClient.connect();
            Thread.sleep(1000);

            natsClient.publish("TestPlan6_RegulatorStream", "<events>"
                    + "   <event><deviceID>1</deviceID><roomNo>100</roomNo>\n"
                    + "                        <isOn>false</isOn></event>"
                    + "</events>");
            natsClient.publish("TestPlan6_RegulatorStream", "<events>"
                    + "   <event><deviceID>2</deviceID><roomNo>101</roomNo>\n"
                    + "                        <isOn>false</isOn></event>"
                    + "</events>");

            SiddhiTestHelper.waitForEvents(2000, 2, count, 3000);
            Assert.assertEquals(count.intValue(), 2);
            Assert.assertEquals(errorAssertionCount.intValue(), 0,
                    "No assertion errors should occur inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }
    }

    /**
     * A stream used by multiple partitions residing in different executionGroups and under
     * same Partition key gets assigned with the respective parallelism and as distinct
     * publishing strategies.
     */
    @Test(dependsOnMethods = "testPartitionStrategy")
    public void testPartitionMultiSubscription() {
        AtomicInteger dumbStreamCount = new AtomicInteger(0);
        String siddhiApp = "@App:name('TestPlan7') \n"
                + "Define stream stockStream(symbol string, price float, quantity int,"
                + " tier string);\n"
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
                + "From #avgPriceStream#window.time(5 min) as a right outer join "
                + "companyTriggerInternalStream#window"
                + ".length(1)\n"
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

        Assert.assertEquals(topology.getQueryGroupList().get(0).getInputStreams()
                .get("stockStream").getSubscriptionStrategy()
                .getStrategy(), TransportStrategy.ALL);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams()
                .get("filteredStockStream").getSubscriptionStrategy()
                .getStrategy(), TransportStrategy.FIELD_GROUPING);
        Assert.assertEquals(topology.getQueryGroupList().get(2).getInputStreams()
                .get("filteredStockStream").getSubscriptionStrategy()
                .getStrategy(), TransportStrategy.FIELD_GROUPING);
        Assert.assertEquals(topology.getQueryGroupList().get(0).getOutputStreams()
                .get("filteredStockStream").getPublishingStrategyList().get(0)
                .getParallelism(), 3);
        Assert.assertEquals(topology.getQueryGroupList().get(0).getOutputStreams()
                .get("filteredStockStream").getPublishingStrategyList().get(0)
                .getGroupingField(), "symbol");

        Assert.assertEquals(topology.getQueryGroupList().get(0).getSiddhiApp(),
                "@App:name('${appName}') \n"
                        + "${companyTriggerStream}"
                        + "Define stream companyTriggerStream(symbol string);\n"
                        + "${stockStream}"
                        + "Define stream stockStream(symbol string, price float, quantity int,"
                        + " tier string);\n"
                        + "${filteredStockStream}define stream filteredStockStream (symbol string, "
                        + "price float, quantity int, tier string);\n"
                        + "${companyTriggerInternalStream}define stream "
                        + "companyTriggerInternalStream (symbol string);\n"
                        + "@info(name = 'query1')\n"
                        + "From stockStream[price > 100]\n"
                        + "Select *\n"
                        + "Insert into filteredStockStream;\n"
                        + "@info(name = 'query2')\n"
                        + "from companyTriggerStream\n"
                        + "select *\n"
                        + "insert into\n"
                        + "companyTriggerInternalStream;\n");

        Assert.assertEquals(topology.getQueryGroupList().get(1).getSiddhiApp(),
                "@App:name('${appName}') \n"
                        + "${companyTriggerInternalStream}define stream "
                        + "companyTriggerInternalStream (symbol string);\n"
                        + "${filteredStockStream}define stream filteredStockStream (symbol string,"
                        + " price float, quantity int,"
                        + " tier string);\n"
                        + "@info(name='query3')\n"
                        + "Partition with (symbol of filteredStockStream)\n"
                        + "begin\n"
                        + "From filteredStockStream#window.lengthBatch(2)\n"
                        + "Select symbol, avg(price) as avgPrice, quantity\n"
                        + "Insert into #avgPriceStream;\n"
                        + "From #avgPriceStream#window.time(5 min) as a right outer join "
                        + "companyTriggerInternalStream#window"
                        + ".length(1)\n"
                        + "On (companyTriggerInternalStream.symbol == a.symbol)\n"
                        + "Select a.symbol, a.avgPrice, a.quantity\n"
                        + "Insert into triggeredAvgStream;\n"
                        + "End;\n");

        SiddhiAppCreator appCreator = new NatsSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);

        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap =
                    createSiddhiAppRuntimes(siddhiManager, queryGroupList);
            InputHandler stockStreamHandler = siddhiAppRuntimeMap.get("TestPlan7-000").get(0)
                    .getInputHandler("stockStream");
            InputHandler triggerStreamHandler = siddhiAppRuntimeMap.get("TestPlan7-000").get(0)
                    .getInputHandler("companyTriggerStream");
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan7-001")) {
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
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan7-002")) {
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

            SiddhiTestHelper.waitForEvents(1000, 1, count, 2000);
            SiddhiTestHelper.waitForEvents(1000, 2, dumbStreamCount, 2000);
            Assert.assertEquals(count.intValue(), 1);
            Assert.assertEquals(dumbStreamCount.intValue(), 2);
            Assert.assertEquals(errorAssertionCount.intValue(), 0,
                    "No assertion errors should occur inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }
    }

    /**
     * A stream used by multiple partitions residing in different executionGroups and
     * different Partition key gets assigned with the {@link TransportStrategy#FIELD_GROUPING}
     * and corresponding parallelism.
     */
    @Test(dependsOnMethods = "testPartitionMultiSubscription")
    public void testPartitionWithMultiKey() {
        AtomicInteger dumbStreamCount = new AtomicInteger(0);
        String siddhiApp = "@App:name('TestPlan8') \n"
                + "Define stream stockStream(symbol string, price float, quantity int,"
                + " tier string);\n"
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
                + "From #avgPriceStream#window.time(5 min) as a "
                + "right outer join companyTriggerInternalStream#window.length(1)\n"
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

        Assert.assertEquals(topology.getQueryGroupList().get(0).getInputStreams()
                .get("stockStream").getSubscriptionStrategy().getStrategy(), TransportStrategy.ALL);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams()
                .get("filteredStockStream").getSubscriptionStrategy()
                .getStrategy(), TransportStrategy.FIELD_GROUPING);
        Assert.assertEquals(topology.getQueryGroupList().get(0).getOutputStreams()
                .get("filteredStockStream").getPublishingStrategyList().get(0)
                .getGroupingField(), "symbol");
        Assert.assertEquals(topology.getQueryGroupList().get(0).getOutputStreams()
                .get("filteredStockStream").getPublishingStrategyList().get(0)
                .getParallelism(), 2);
        Assert.assertEquals(topology.getQueryGroupList().get(0).getOutputStreams()
                .get("filteredStockStream").getPublishingStrategyList().get(1)
                .getGroupingField(), "tier");
        Assert.assertEquals(topology.getQueryGroupList().get(0).getOutputStreams()
                .get("filteredStockStream").getPublishingStrategyList().get(1)
                .getParallelism(), 3);

        SiddhiAppCreator appCreator = new NatsSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap =
                    createSiddhiAppRuntimes(siddhiManager, queryGroupList);
            InputHandler stockStreamHandler = siddhiAppRuntimeMap.get("TestPlan8-001").get(0)
                    .getInputHandler("stockStream");
            InputHandler triggerStreamHandler = siddhiAppRuntimeMap.get("TestPlan8-001").get(0)
                    .getInputHandler("companyTriggerStream");
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan8-002")) {
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
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("TestPlan8-003")) {
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

            SiddhiTestHelper.waitForEvents(2000, 1, count, 3000);
            SiddhiTestHelper.waitForEvents(2000, 2, dumbStreamCount,
                    3000);
            Assert.assertEquals(count.intValue(), 1);
            Assert.assertEquals(dumbStreamCount.intValue(), 2);
            Assert.assertEquals(errorAssertionCount.intValue(), 0,
                    "No assertion errors should occur inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }
    }

    /**
     * user given Sink used in (parallel/multiple execGroups) will get assigned to all the
     * execGroups after Topology creation.
     */
    @Test(dependsOnMethods = "testPartitionWithMultiKey")
    public void testUserDefinedSink() {
        String siddhiApp = "@App:name('TestPlan9') \n"
                + "Define stream stockStream(symbol string, price float, quantity int, "
                + "tier string);\n"
                + "@Sink(type='inMemory', topic='takingOverTopic', @map(type='passThrough'))\n"
                + "Define stream takingOverStream(symbol string, overtakingSymbol string, "
                + "avgPrice double);\n"
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

        Assert.assertEquals(topology.getQueryGroupList().get(0).getOutputStreams()
                .get("filteredStockStream").getPublishingStrategyList()
                .get(0).getStrategy(), TransportStrategy.ROUND_ROBIN);
        Assert.assertEquals(topology.getQueryGroupList().get(0).getOutputStreams()
                .get("filteredStockStream").getPublishingStrategyList()
                .get(1).getStrategy(), TransportStrategy.FIELD_GROUPING);
        Assert.assertTrue(topology.getQueryGroupList().get(1).getOutputStreams()
                .containsKey("takingOverStream"));
        Assert.assertTrue(topology.getQueryGroupList().get(2).getOutputStreams()
                .containsKey("takingOverStream"));

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
        SiddhiAppCreator appCreator = new NatsSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap =
                    createSiddhiAppRuntimes(siddhiManager, queryGroupList);
            InputHandler stockStreamHandler = siddhiAppRuntimeMap.get("TestPlan9-001").get(0)
                    .getInputHandler("stockStream");

            stockStreamHandler.send(new Object[]{"WSO2", 150F, 2, "middleware"});
            stockStreamHandler.send(new Object[]{"WSO2", 200F, 2, "middleware"});
            stockStreamHandler.send(new Object[]{"ABC", 300F, 2, "language"});
            stockStreamHandler.send(new Object[]{"ABC", 200F, 2, "language"});

            SiddhiTestHelper.waitForEvents(1500, 3, count, 2000);
            Assert.assertEquals(count.intValue(), 3);
            Assert.assertEquals(errorAssertionCount.intValue(), 0,
                    "No assertion errors should occur inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
            InMemoryBroker.unsubscribe(subscriptionTakingOver);
        }
    }

    /**
     * when a user defined sink stream is used as in an internal source stream, a placeholder
     * corresponding to the streamID will be added to the respective sink so that the placeholder
     * will bridge the stream to the required source.
     */
    @Test(dependsOnMethods = "testUserDefinedSink")
    public void testSinkStreamForSource() {
        String siddhiApp = "@App:name('TestPlan10')\n"
                + "Define stream stockStream(symbol string, price float, quantity int,"
                + " tier string);\n"
                + "Define stream companyTriggerStream(symbol string);\n"
                + "@Sink(type='inMemory', topic='takingOverTopic', @map(type='passThrough'))\n"
                + "Define stream takingOverStream(symbol string, overtakingSymbol string, "
                + "avgPrice double);\n"
                + "@Sink(type='inMemory', topic='takingOverTableTopic', @map(type='passThrough'))\n"
                + "Define stream takingOverTableStream(symbol string, overtakingSymbol string, "
                + "avgPrice double);\n"
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

        Assert.assertEquals(topology.getQueryGroupList().get(0).getInputStreams().get("stockStream")
                .getSubscriptionStrategy().getStrategy(), TransportStrategy.ALL);
        Assert.assertEquals(topology.getQueryGroupList().get(1).getInputStreams()
                .get("filteredStockStream").getSubscriptionStrategy()
                .getStrategy(), TransportStrategy.FIELD_GROUPING);
        Assert.assertEquals(topology.getQueryGroupList().get(2).getInputStreams()
                .get("avgPriceStream").getSubscriptionStrategy()
                .getStrategy(), TransportStrategy.ALL);
        Assert.assertEquals(topology.getQueryGroupList().get(3).getInputStreams()
                .get("takingOverStream").getSubscriptionStrategy()
                .getStrategy(), TransportStrategy.ALL);

        Assert.assertEquals(topology.getQueryGroupList().get(2).getOutputStreams()
                .get("takingOverStream").getStreamDefinition()
                + ";", "@Sink(type='inMemory', topic='takingOverTopic', "
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

        SiddhiAppCreator appCreator = new NatsSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap =
                    createSiddhiAppRuntimes(siddhiManager, queryGroupList);
            InputHandler stockStreamHandler = siddhiAppRuntimeMap.get("TestPlan10-001").get(0)
                    .getInputHandler("stockStream");

            stockStreamHandler.send(new Object[]{"WSO2", 150F, 2, "middleware"});
            stockStreamHandler.send(new Object[]{"WSO2", 200F, 2, "middleware"});
            stockStreamHandler.send(new Object[]{"ABC", 300F, 2, "language"});
            stockStreamHandler.send(new Object[]{"ABC", 200F, 2, "language"});

            SiddhiTestHelper.waitForEvents(5000, 3, count, 8000);
            Assert.assertEquals(count.intValue(), 4);
            Assert.assertEquals(errorAssertionCount.intValue(), 0,
                    "No assertion errors should "
                            + "occur inside callbacks");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
            InMemoryBroker.unsubscribe(subscriptionTakingOver);
            InMemoryBroker.unsubscribe(subscriptionTakingOverTable);
        }
    }

    /**
     * when user given sources are located in more than 1 execGroup then a passthrough query
     * will be added in a new execGroup.Newly created execGroup will be moved to as the first
     * element of already created passthrough queries.
     */
    @Test(dependsOnMethods = "testSinkStreamForSource")
    public void testUsergivenSourceNoGroup() {
        String siddhiApp = "@App:name('testplan11') \n"
                + "@source(type='inMemory', topic='stock', @map(type='json'))\n"
                + "Define stream stockstream(symbol string, price float, quantity int,"
                + " tier string);\n"
                + "@source(type='inMemory', topic='companyTrigger', @map(type='json'))\n"
                + "Define stream companyTriggerStream(symbol string);\n"
                + "@info(name = 'query1')@dist(parallel='3', execGroup='001')\n"
                + "From stockstream[price > 100]\n"
                + "Select *\n"
                + "Insert into filteredStockStream;\n"
                + "@info(name = 'query2')@dist(parallel='3', execGroup='001')\n"
                + "From companyTriggerStream \n"
                + "Select *\n"
                + "Insert into SymbolStream;\n"
                + "@info(name = 'query3')@dist(parallel='3', execGroup='002')\n"
                + "From stockstream\n"
                + "Select *\n"
                + "Insert into LowStockStream;\n"
                + "@info(name='query4')@dist(parallel='3', execGroup='002')\n"
                + "Partition with (symbol of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#window.lengthBatch(2)\n"
                + "Select symbol, avg(price) as avgPrice, quantity\n"
                + "Insert into #avgPriceStream;\n"
                + "From #avgPriceStream#window.time(5 min) as a right outer join "
                + "companyTriggerStream#window.length(1) \n"
                + "On (companyTriggerStream.symbol == a.symbol)\n"
                + "Select a.symbol, a.avgPrice, a.quantity, companyTriggerStream.symbol as sss\n"
                + "Insert into triggeredAvgStream;\n"
                + "End;\n";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new NatsSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        Assert.assertTrue(queryGroupList.size() == 4,
                "Four query groups should be created");
        Assert.assertTrue(queryGroupList.get(0).getGroupName()
                        .contains(SiddhiTopologyCreatorConstants.PASSTHROUGH),
                "Two passthrough queries should be present in separate groups");
        Assert.assertTrue(queryGroupList.get(0).isReceiverQueryGroup(),
                "Receiver type should be set");
        Assert.assertTrue(queryGroupList.get(1).getGroupName()
                        .contains(SiddhiTopologyCreatorConstants.PASSTHROUGH),
                "Two passthrough queries should be present in separate groups");
        Assert.assertTrue(queryGroupList.get(1).isReceiverQueryGroup(),
                "Receiver type should be set");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap =
                    createSiddhiAppRuntimes(siddhiManager, queryGroupList);
            for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get("testplan11-001")) {
                runtime.addCallback("stockstream", new StreamCallback() {
                    @Override
                    public void receive(Event[] events) {
                        EventPrinter.print(events);
                        count.addAndGet(events.length);
                        for (Event event : events) {
                            if (event.getData()[0].equals("WSO2")) {
                                errorAssertionCount.incrementAndGet();
                                Assert.assertEquals(event.getData()[0], "WSO2");
                                errorAssertionCount.decrementAndGet();
                            }
                        }
                    }
                });
            }
            InMemoryBroker.publish("stock", "{\"event\":{\"symbol\":\"WSO2\","
                    + " \"price\":225.0, \"quantity\":20,\"tier\":\"middleware\"}}");

            Thread.sleep(5000);
            SiddhiTestHelper.waitForEvents(8000, 1, count, 10000);
            Assert.assertEquals(count.intValue(), 1);
            Assert.assertEquals(errorAssertionCount.intValue(), 0,
                    "No assertion errors should occur inside callbacks");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            siddhiManager.shutdown();
        }
    }

    /**
     * Test pass-through creation when multiple subscriptions R/R and Field grouping exist for user
     * given stream.
     */
    @Test(dependsOnMethods = "testUsergivenSourceNoGroup")
    public void testPassthoughWithMultipleSubscription() {
        String siddhiApp = "@App:name('TestPlan12')\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/SweetProductionEP',"
                + " @map(type = 'json'))\n"
                + "define stream Test1Stream (name string, amount double);\n"
                + "@sink(type='log')\n"
                + "define stream Test2Stream (name string, amount double);\n"
                + "@info(name = 'query2')@dist(parallel='1', execGroup='001')\n"
                + " from Test1Stream\n"
                + "select *\n"
                + "insert into Test3Stream;\n"
                + "@info(name = 'query1')@dist(parallel='3', execGroup='002')\n"
                + "Partition with (name of Test1Stream)\n"
                + "Begin\n"
                + "from Test1Stream\n"
                + "select name,amount\n"
                + "insert into Test2Stream;\n"
                + "end;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new NatsSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);

        Assert.assertTrue(queryGroupList.size() == 3, "Three query groups should be created");
        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains(SiddhiTopologyCreatorConstants.PASSTHROUGH),
                "Passthrough query should be present in a separate group");
        Assert.assertTrue(queryGroupList.get(0).isReceiverQueryGroup(), "Receiver type should be set");


        Assert.assertTrue(queryGroupList.get(0).getSiddhiQueries().get(0).getApp()
                        .contains("@source(type = 'http', receiver.url='http://localhost:8080/SweetProductionEP', "
                                + "@map(type = 'json'))\n"
                                + "define stream passthroughTest1Stream (name string, amount double);\n"
                                + "@sink(type='nats',cluster.id='test-cluster',destination = 'TestPlan12_Test1Stream', "
                                + "bootstrap.servers='nats://localhost:4222',@map(type='xml'))\n"
                                + "@sink(type='nats',cluster.id='test-cluster',@distribution(strategy='partitioned', "
                                + "partitionKey='name',@destination(destination = 'TestPlan12_Test1Stream_name_0'),"
                                + "@destination(destination = 'TestPlan12_Test1Stream_name_1'),"
                                + "@destination(destination = 'TestPlan12_Test1Stream_name_2')), "
                                + "bootstrap.servers='nats://localhost:4222',@map(type='xml')) \n"
                                + "define stream Test1Stream (name string, amount double);\n"
                                + "from passthroughTest1Stream select * insert into Test1Stream;"),
                "Incorrect partial Siddhi application created");

        Assert.assertTrue(queryGroupList.get(1).getSiddhiQueries().get(0).getApp()
                        .contains("@App:name('TestPlan12-001-1') \n"
                                + "@source(type='nats',cluster.id='test-cluster',destination = "
                                + "'TestPlan12_Test1Stream', "
                                + "bootstrap.servers='nats://localhost:4222',@map(type='xml')) \n"
                                + "define stream Test1Stream (name string, amount double);\n"
                                + "@info(name = 'query2')\n"
                                + " from Test1Stream\n"
                                + "select *\n"
                                + "insert into Test3Stream;"),
                "Incorrect partial Siddhi application Created");

        Assert.assertTrue(queryGroupList.get(2).getSiddhiQueries().get(0).getApp()
                .contains("@App:name('TestPlan12-002-1') \n"
                                + "@source(type='nats',cluster.id='test-cluster',destination = "
                                + "'TestPlan12_Test1Stream_name_0', "
                                + "bootstrap.servers='nats://localhost:4222',@map(type='xml')) \n"
                                + "define stream Test1Stream (name string, amount double);\n"
                                + "@sink(type='log')\n"
                                + "define stream Test2Stream (name string, amount double);\n"
                                + "@info(name = 'query1')\n"
                                + "Partition with (name of Test1Stream)\n"
                                + "Begin\n"
                                + "from Test1Stream\n"
                                + "select name,amount\n"
                                + "insert into Test2Stream;\n"
                                + "end;"), "Incorrect partial Siddhi application Created");

    }


    private Map<String, List<SiddhiAppRuntime>> createSiddhiAppRuntimes(
            SiddhiManager siddhiManager, List<DeployableSiddhiQueryGroup> queryGroupList) {
        Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap = new HashMap<>(queryGroupList
                .size());
        for (DeployableSiddhiQueryGroup group : queryGroupList) {
            List<SiddhiAppRuntime> runtimeList = new ArrayList<>(group.getSiddhiQueries().size());
            for (SiddhiQuery siddhiQuery : group.getSiddhiQueries()) {
                SiddhiAppRuntime runtime = siddhiManager.createSiddhiAppRuntime(siddhiQuery
                        .getApp());
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
