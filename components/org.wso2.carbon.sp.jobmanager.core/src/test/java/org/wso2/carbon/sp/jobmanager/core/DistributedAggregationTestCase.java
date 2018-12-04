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
import org.wso2.carbon.sp.jobmanager.core.appcreator.JMSSiddhiAppCreator;
import org.wso2.carbon.sp.jobmanager.core.appcreator.SiddhiQuery;
import org.wso2.carbon.sp.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.topology.SiddhiQueryGroup;
import org.wso2.carbon.sp.jobmanager.core.topology.SiddhiTopology;
import org.wso2.carbon.sp.jobmanager.core.topology.SiddhiTopologyCreatorImpl;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiTestHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contains the test cases for distributed incremental aggregations and aggregations joins.
 */
public class DistributedAggregationTestCase {
    private static final Logger log = Logger.getLogger(DistributedAggregationTestCase.class);
    private AtomicInteger count;
    private AtomicInteger errorAssertionCount;
    private static final String PROVIDER_URL = "vm://localhost";
    private static final String INITIAL_FACTORY = "org.apache.activemq.jndi.ActiveMQInitialContextFactory";
    private static final String JDBC_URL = "jdbc:h2:./target/testdb";
    private static final String JDBC_DRIVER_NAME = "org.h2.Driver";

    @BeforeMethod
    public void setUp() {
        DeploymentConfig deploymentConfig = new DeploymentConfig();
        deploymentConfig.setFactoryInitial(INITIAL_FACTORY);
        deploymentConfig.setProviderUrl(PROVIDER_URL);
        ServiceDataHolder.setDeploymentConfig(deploymentConfig);
        count = new AtomicInteger(0);
        errorAssertionCount = new AtomicInteger(0);
    }

    /**
     * If a siddhi application contains an aggregation then a {@link SiddhiQueryGroup} should be created.
     */
    @Test
    public void testAggregationQueryGroupCreation() {
        String siddhiApp = "@App:name('TestPlan1')\n"
                + "@App:description('Queries and partitions joins with aggregation')\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/SweetProductionEP0', @map(type = 'json')"
                + ")\n"
                + "define stream TradeStream (symbol string, price double, volume long, timestamp long);\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/Stocks', @map(type = 'json'))\n"
                + "define stream StockStream (symbol string, value int, timestamp long);\n"
                + "@dist(parallel='1')\n"
                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= 'root', jdbc"
                + ".driver.name= '" + JDBC_DRIVER_NAME + "')"
                + "define aggregation StockAggregation\n"
                + "from StockStream\n"
                + "select symbol, avg(value) as avgValue, sum(value) as total\n"
                + "group by symbol\n"
                + "aggregate by timestamp every sec ... year;\n"
                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= 'root', jdbc"
                + ".driver.name= '" + JDBC_DRIVER_NAME + "')"
                + "define aggregation TradeAggregation\n"
                + "from TradeStream\n"
                + "select symbol, avg(price) as avgPrice, sum(price) as total\n"
                + "group by symbol\n"
                + "aggregate by timestamp every sec ... year;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new JMSSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);

        Assert.assertEquals(queryGroupList.size(), 2);
        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains("aggregation"));
        Assert.assertTrue(queryGroupList.get(1).getGroupName().contains("aggregation"));
        Assert.assertTrue(topology.getQueryGroupList().get(0).getInputStreams().keySet().contains("StockStream"));
        Assert.assertTrue(topology.getQueryGroupList().get(1).getInputStreams().keySet().contains("TradeStream"));

    }

    /**
     * If a siddhi query uses persistent aggregation, then that aggregation definition
     * should be added to the {@link SiddhiQueryGroup} where that query resides and the source stream of the
     * aggregation should be renamed. (To avoid the aggregation get fed) Also the new stream should be added to the
     * {@link SiddhiQueryGroup} to avoid {@link org.wso2.siddhi.query.compiler.exception.SiddhiParserException}
     */
    @Test(dependsOnMethods = "testAggregationQueryGroupCreation")
    public void testQueryJoinAggregation() {
        String siddhiApp = "@App:name('TestPlan2')\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/SweetProduction', @map(type"
                + " = 'json'))\n"
                + "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , "
                + "quantity int, timestamp string);"
                + "@dist(parallel='2')\n"
                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= 'root', jdbc"
                + ".driver.name= '" + JDBC_DRIVER_NAME + "')"
                + "define aggregation stockAggregation "
                + "from stockStream "
                + "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) "
                + "as lastTradeValue  "
                + "group by symbol "
                + "aggregate by timestamp every sec...year; "
                + "@source(type = 'http', receiver.url='http://localhost:8080/inputs', @map(type"
                + " = 'json'))\n"
                + "define stream TestStream (symbol string, value int, startTime string, "
                + "endTime string, perValue string); "
                + "@sink(type='log')\n"
                + "define stream outputStream (symbol string, avgPrice double, sumPrice double, lastTradeValue float);"
                + "\n"
                + "@info(name = 'query-join1')@dist(execGroup='001', parallel = '2')\n"
                + "from TestStream as i join stockAggregation as s "
                + "on i.symbol == s.symbol "
                + "within \"2017-06-01 09:35:00 +05:30\", \"2017-06-01 10:37:57 +05:30\" "
                + "per i.perValue "
                + "select s.symbol, avgPrice, totalPrice as sumPrice, lastTradeValue  "
                + "insert into outputStream; ";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new JMSSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);

        Assert.assertEquals(queryGroupList.size(), 3);
        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains("passthrough"));
        Assert.assertTrue(queryGroupList.get(1).getGroupName().contains("TestPlan2-001"));
        Assert.assertTrue(queryGroupList.get(2).getGroupName().contains("aggregation"));

        SiddhiManager siddhiManager = new SiddhiManager();
        Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap =
                createSiddhiAppRuntimes(siddhiManager, queryGroupList);

        for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get(queryGroupList.get(1).getGroupName())) {
            runtime.addCallback("outputStream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    EventPrinter.print(events);
                    count.addAndGet(events.length);
                    for (Event event : events) {
                        errorAssertionCount.incrementAndGet();
                        errorAssertionCount.decrementAndGet();
                    }
                }
            });
        }

        InputHandler stockStreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(2).getGroupName()).get(0)
                .getInputHandler("stockStream");
        InputHandler testStreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(0).getGroupName()).get(0)
                .getInputHandler("TestStream");

        try {
            stockStreamInputHandler.send(new Object[]{"IBM", 50f, 60f, 90L, 6, "2017-06-01 04:05:51"});
            Thread.sleep(1000);
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, 50f, 200L, 26, "2017-06-01 04:06:54"});
            Thread.sleep(1000);
            stockStreamInputHandler.send(new Object[]{"IBM", 50f, 60f, 90L, 8, "2017-06-01 04:07:50"});
            Thread.sleep(1000);
            stockStreamInputHandler.send(new Object[]{"IBM", 900f, 25f, 200L, 60, "2017-06-01 04:08:56"});

            Thread.sleep(2000);
            testStreamInputHandler.send(new Object[]{"IBM", 1, "2017-06-01 09:35:51 +05:30",
                    "2017-06-01 09:35:52 +05:30", "seconds"});
            Thread.sleep(2000);

            SiddhiTestHelper.waitForEvents(2000, 4, count, 60000);
            Assert.assertEquals(count.get(), 4);

        } catch (InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            siddhiManager.shutdown();
        }
    }

    /**
     * If a siddhi partition uses persistent aggregation, then that aggregation definition
     * should be added to the {@link SiddhiQueryGroup} where that partition resides and the source stream of the
     * aggregation should be renamed. (To avoid the aggregation get fed) Also the new stream should be added to the
     * {@link SiddhiQueryGroup} to avoid {@link org.wso2.siddhi.query.compiler.exception.SiddhiParserException}
     */
    @Test(dependsOnMethods = "testQueryJoinAggregation")
    public void testPartitionJoinAggregation() {
        String siddhiApp = "@App:name('TestPlan3')\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/SweetProductionEP1', @map(type"
                + " = 'json'))\n"
                + "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , "
                + "quantity int, timestamp string);"
                + "@dist(parallel='2')\n"
                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= 'root', jdbc"
                + ".driver.name= '" + JDBC_DRIVER_NAME + "')"
                + "define aggregation stockAggregation "
                + "from stockStream "
                + "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) "
                + "as lastTradeValue  "
                + "group by symbol "
                + "aggregate by timestamp every sec...year; "
                + "@source(type = 'http', receiver.url='http://localhost:8080/inputs', @map(type"
                + " = 'json'))\n"
                + "define stream TestStream (symbol string, value int, startTime string, "
                + "endTime string, perValue string); "
                + "@sink(type='log')\n"
                + "define stream outputStream (symbol string, avgPrice double, sumPrice double, lastTradeValue float);"
                + "\n"
                + "@info(name = 'partition-join1')@dist(execGroup='001', parallel = '2')\n"
                + "Partition with (symbol of TestStream)\n"
                + "Begin\n"
                + "from TestStream as i join stockAggregation as s "
                + "on i.symbol == s.symbol "
                + "within \"2017-06-01 09:35:00 +05:30\", \"2017-06-01 10:37:57 +05:30\" "
                + "per i.perValue "
                + "select s.symbol, avgPrice, totalPrice as sumPrice, lastTradeValue  "
                + "insert into outputStream; "
                + "end";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new JMSSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);

        Assert.assertEquals(queryGroupList.size(), 3);
        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains("passthrough"));
        Assert.assertTrue(queryGroupList.get(1).getGroupName().contains("TestPlan3-001"));
        Assert.assertTrue(queryGroupList.get(2).getGroupName().contains("aggregation"));

        SiddhiManager siddhiManager = new SiddhiManager();
        Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap =
                createSiddhiAppRuntimes(siddhiManager, queryGroupList);

        for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get(queryGroupList.get(1).getGroupName())) {
            runtime.addCallback("outputStream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    EventPrinter.print(events);
                    count.addAndGet(events.length);
                    for (Event event : events) {
                        errorAssertionCount.incrementAndGet();
                        errorAssertionCount.decrementAndGet();
                    }
                }
            });
        }

        InputHandler stockStreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(2).getGroupName()).get(0)
                .getInputHandler("stockStream");
        InputHandler testStreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(0).getGroupName()).get(0)
                .getInputHandler("TestStream");

        try {
            stockStreamInputHandler.send(new Object[]{"IBM", 50f, 60f, 90L, 6, "2017-06-01 04:05:51"});
            Thread.sleep(1000);
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, 50f, 200L, 26, "2017-06-01 04:06:54"});
            Thread.sleep(1000);
            stockStreamInputHandler.send(new Object[]{"IBM", 50f, 60f, 90L, 8, "2017-06-01 04:07:50"});
            Thread.sleep(1000);
            stockStreamInputHandler.send(new Object[]{"IBM", 900f, 25f, 200L, 60, "2017-06-01 04:08:56"});

            Thread.sleep(100);
            testStreamInputHandler.send(new Object[]{"IBM", 1, "2017-06-01 09:35:51 +05:30",
                    "2017-06-01 09:35:52 +05:30", "seconds"});
            Thread.sleep(100);

            SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
            Assert.assertEquals(count.get(), 4);

        } catch (InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            siddhiManager.shutdown();
        }
    }

    /**
     * If multiple query/partition joins with a persistent aggregation, then all the {@link SiddhiQueryGroup}
     * where the query/partition resides should include the aggregation definition and the renamed source stream.
     */
    @Test(dependsOnMethods = "testPartitionJoinAggregation")
    public void testMultipleAggregations() {
        String siddhiApp = "@App:name('TestPlan4')\n"
                + "@App:description('Get knowledge on aggregated topology')\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/SweetProductionEP', @map(type = 'json')"
                + ")\n"
                + "define stream Test1Stream (symbol string, price double, volume long, timestamp long);\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/Stocks', @map(type = 'json'))\n"
                + "define stream Test2Stream (symbol string, price double, volume long, timestamp long);\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/values', @map(type = 'json'))\n"
                + "define stream Test3Stream (symbol string, price double, volume long, timestamp long);\n"
                + "@sink(type='log')\n"
                + "define stream Test4Stream (symbol string, price double, volume long, timestamp long);\n"
                + "@sink(type='log')\n"
                + "define stream Test5Stream (symbol string, avgPrice double, totalVolume long);\n"
                + "@sink(type='log')\n"
                + "define stream Test6Stream (symbol string, avgPrice double);\n"
                + "@dist(parallel='2')\n"
                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= 'root', jdbc"
                + ".driver.name= '" + JDBC_DRIVER_NAME + "')"
                + "define aggregation AggregationTest1\n"
                + "from Test2Stream\n"
                + "select symbol, avg(price) as avgPrice, sum(volume) as totalVolume\n"
                + "group by symbol\n"
                + "aggregate by timestamp every sec ... year;\n"
                + "@dist(parallel='3')\n"
                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= 'root', jdbc"
                + ".driver.name= '" + JDBC_DRIVER_NAME + "')"
                + "define aggregation AggregationTest2\n"
                + "from Test4Stream\n"
                + "select symbol, avg(price) as avgPrice, sum(volume) as totalVolume\n"
                + "group by symbol\n"
                + "aggregate by timestamp every sec ... year;\n"
                + "@dist(parallel='2')\n"
                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= 'root', jdbc"
                + ".driver.name= '" + JDBC_DRIVER_NAME + "')"
                + "define aggregation AggregetionTest3\n"
                + "from Test3Stream\n"
                + "select symbol, avg(price) as avgPrice, sum(volume) as totalVolume\n"
                + "group by symbol\n"
                + "aggregate by timestamp every sec ... year;\n"
                + "@info(name = 'test-filter')@dist(execGroup='001', parallel = '1')\n"
                + "from Test1Stream[price >100]\n"
                + "select *\n"
                + "insert into\n"
                + "Test4Stream;\n"
                + "@info(name = 'query-join1')@dist(execGroup='002', parallel = '2')\n"
                + "from Test1Stream as P join AggregationTest1 as Q\n"
                + "on P.symbol == Q.symbol\n"
                + "within \"2014-02-15 00:00:00 +05:30\", \"2018-12-16 00:00:00 +05:30\"\n"
                + "per \"days\"\n"
                + "select P.symbol, Q.totalVolume\n"
                + "Insert into Test7Stream;\n"
                + "@info(name='partition-join')@dist(parallel='2', execGroup='003')\n"
                + "Partition with (symbol of Test4Stream)\n"
                + "begin\n"
                + "From Test4Stream as P join AggregationTest1 as Q\n"
                + "on P.symbol == Q.symbol\n"
                + "within \"2014-02-15 00:00:00 +05:30\", \"2018-12-16 00:00:00 +05:30\"\n"
                + "per \"days\"\n"
                + "select P.symbol, Q.avgPrice, Q.totalVolume\n"
                + "Insert into Test5Stream;\n"
                + "End;\n"
                + "@info(name = 'query-join2')@dist(parallel='3', execGroup='004')\n"
                + "from Test3Stream as P join AggregationTest2 as Q\n"
                + "on P.symbol == Q.symbol\n"
                + "within \"2014-02-15 00:00:00 +05:30\", \"2018-12-16 00:00:00 +05:30\"\n"
                + "per \"days\"\n"
                + "select P.symbol, Q.avgPrice\n"
                + "insert into Test6Stream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new JMSSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);

        Assert.assertEquals(queryGroupList.size(), 9);
        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains("passthrough"));
        Assert.assertTrue(queryGroupList.get(1).getGroupName().contains("passthrough"));
        Assert.assertTrue(queryGroupList.get(2).getGroupName().contains("001"));
        Assert.assertTrue(queryGroupList.get(3).getGroupName().contains("002"));
        Assert.assertTrue(queryGroupList.get(4).getGroupName().contains("003"));
        Assert.assertTrue(queryGroupList.get(5).getGroupName().contains("004"));
        Assert.assertTrue(queryGroupList.get(6).getGroupName().contains("aggregation"));
        Assert.assertTrue(queryGroupList.get(7).getGroupName().contains("aggregation"));
        Assert.assertTrue(queryGroupList.get(8).getGroupName().contains("aggregation"));

        Assert.assertTrue(queryGroupList.get(0).getSiddhiQueries().get(0).getApp()
                        .contains("@source(type = 'http', receiver.url='http://localhost:8080/SweetProductionEP',"
                                + " @map(type = 'json'))\n"
                                + "define stream passthroughTest1Stream (symbol string, price double, volume long,"
                                + " timestamp long);\n"
                                + "@sink(type='jms',factory.initial="
                                + "'org.apache.activemq.jndi.ActiveMQInitialContextFactory',provider.url="
                                + "'vm://localhost',connection.factory.type='topic',destination = "
                                + "'TestPlan4_Test1Stream', connection.factory.jndi.name='TopicConnectionFactory'"
                                + ",@map(type='xml'))\n"
                                + "@sink(type='jms',factory.initial="
                                + "'org.apache.activemq.jndi.ActiveMQInitialContextFactory',provider.url="
                                + "'vm://localhost',connection.factory.type='queue',destination = "
                                + "'TestPlan4_Test1Stream_0', connection.factory.jndi.name='QueueConnectionFactory'"
                                + ",@map(type='xml')) \n"
                                + "define stream Test1Stream (symbol string, price double, volume long,"
                                + " timestamp long);\n"
                                + "from passthroughTest1Stream select * insert into Test1Stream;"),
                "Incorrect Partial Siddhi application created");

        Assert.assertTrue(queryGroupList.get(1).getSiddhiQueries().get(0).getApp()
                        .contains("@source(type = 'http', receiver.url='http://localhost:8080/values', "
                                + "@map(type = 'json'))\n"
                                + "define stream passthroughTest3Stream (symbol string, price double, volume long,"
                                + " timestamp long);\n"
                                + "@sink(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',connection.factory"
                                + ".type='queue',destination = 'TestPlan4_Test3Stream_0', connection.factory.jndi"
                                + ".name='QueueConnectionFactory',@map(type='xml'))\n"
                                + "@sink(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',"
                                + "connection.factory.type='topic',destination = 'TestPlan4_Test3Stream',"
                                + " connection.factory.jndi.name='TopicConnectionFactory',@map(type='xml')) \n"
                                + "define stream Test3Stream (symbol string, price double, volume long,"
                                + " timestamp long);\n"
                                + "from passthroughTest3Stream select * insert into Test3Stream;"),
                "Incorrect Partial Siddhi application created");

        Assert.assertTrue(queryGroupList.get(2).getSiddhiQueries().get(0).getApp()
                        .contains("@App:name('TestPlan4-001-1') \n"
                                + "@source(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',connection.factory"
                                + ".type='topic',destination ='TestPlan4_Test1Stream' , connection.factory.jndi"
                                + ".name='TopicConnectionFactory',@map(type='xml')) \n"
                                + "define stream Test1Stream (symbol string, price double, volume long,"
                                + " timestamp long);\n"
                                + "@sink(type='log')\n"
                                + "@sink(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',connection.factory"
                                + ".type='topic',destination = 'TestPlan4_Test4Stream', connection.factory.jndi"
                                + ".name='TopicConnectionFactory',@map(type='xml'))\n"
                                + "@sink(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',"
                                + "@distribution(strategy='partitioned', partitionKey='symbol',"
                                + "@destination(destination = 'TestPlan4_Test4Stream_symbol_0'),"
                                + "@destination(destination = 'TestPlan4_Test4Stream_symbol_1')),connection.factory"
                                + ".type='topic',connection.factory.jndi.name='TopicConnectionFactory',"
                                + "@map(type='xml')) \n"
                                + "define stream Test4Stream (symbol string, price double, volume long,"
                                + " timestamp long);\n"
                                + "@info(name = 'test-filter')\n"
                                + "from Test1Stream[price >100]\n"
                                + "select *\n"
                                + "insert into\n"
                                + "Test4Stream;"),
                "Incorrect Partial Siddhi application created");

        Assert.assertTrue(queryGroupList.get(3).getSiddhiQueries().get(0).getApp()
                        .contains("@App:name('TestPlan4-002-1') \n"
                                + "\n"
                                + "define stream Test2Stream_TestPlan4_002 (symbol string, price double, volume long, "
                                + "timestamp long);\n"
                                + "@source(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',connection.factory"
                                + ".type='queue',destination ='TestPlan4_Test1Stream_0',connection.factory.jndi"
                                + ".name='QueueConnectionFactory',@map(type ='xml')) \n"
                                + "define stream Test1Stream (symbol string, price double, volume long,"
                                + " timestamp long);\n"
                                + "\n"
                                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= "
                                + "'root', jdbc"
                                + ".driver.name= '" + JDBC_DRIVER_NAME + "')"
                                + "define aggregation AggregationTest1\n"
                                + "from Test2Stream_TestPlan4_002\n"
                                + "select symbol, avg(price) as avgPrice, sum(volume) as totalVolume\n"
                                + "group by symbol\n"
                                + "aggregate by timestamp every sec ... year;\n"
                                + "@info(name = 'query-join1')\n"
                                + "from Test1Stream as P join AggregationTest1 as Q\n"
                                + "on P.symbol == Q.symbol\n"
                                + "within \"2014-02-15 00:00:00 +05:30\", \"2018-12-16 00:00:00 +05:30\"\n"
                                + "per \"days\"\n"
                                + "select P.symbol, Q.totalVolume\n"
                                + "Insert into Test7Stream;\n"),
                "Incorrect Partial Siddhi application created");

        Assert.assertTrue(queryGroupList.get(4).getSiddhiQueries().get(0).getApp()
                        .contains("@App:name('TestPlan4-003-1') \n"
                                + "@source(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',connection.factory"
                                + ".type='topic',destination ='TestPlan4_Test4Stream_symbol_0' , connection.factory"
                                + ".jndi.name='TopicConnectionFactory',@map(type='xml')) \n"
                                + "define stream Test4Stream (symbol string, price double, volume long,"
                                + " timestamp long);\n"
                                + "\n"
                                + "define stream Test2Stream_TestPlan4_003 (symbol string, price double, volume long,"
                                + " timestamp long);\n"
                                + "@sink(type='log')\n"
                                + "define stream Test5Stream (symbol string, avgPrice double, totalVolume long);\n"
                                + "\n"
                                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= "
                                + "'root', jdbc"
                                + ".driver.name= '" + JDBC_DRIVER_NAME + "')"
                                + "define aggregation AggregationTest1\n"
                                + "from Test2Stream_TestPlan4_003\n"
                                + "select symbol, avg(price) as avgPrice, sum(volume) as totalVolume\n"
                                + "group by symbol\n"
                                + "aggregate by timestamp every sec ... year;\n"
                                + "@info(name='partition-join')\n"
                                + "Partition with (symbol of Test4Stream)\n"
                                + "begin\n"
                                + "From Test4Stream as P join AggregationTest1 as Q\n"
                                + "on P.symbol == Q.symbol\n"
                                + "within \"2014-02-15 00:00:00 +05:30\", \"2018-12-16 00:00:00 +05:30\"\n"
                                + "per \"days\"\n"
                                + "select P.symbol, Q.avgPrice, Q.totalVolume\n"
                                + "Insert into Test5Stream;\n"
                                + "End;"),
                "Incorrect Partial Siddhi application created");

        Assert.assertTrue(queryGroupList.get(5).getSiddhiQueries().get(0).getApp()
                        .contains("@App:name('TestPlan4-004-1') \n"
                                + "\n"
                                + "define stream Test4Stream_TestPlan4_004 (symbol string, price double, volume long,"
                                + " timestamp long);\n"
                                + "@source(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',connection.factory"
                                + ".type='queue',destination ='TestPlan4_Test3Stream_0',connection.factory.jndi"
                                + ".name='QueueConnectionFactory',@map(type ='xml')) \n"
                                + "define stream Test3Stream (symbol string, price double, volume long,"
                                + " timestamp long);\n"
                                + "@sink(type='log')\n"
                                + "define stream Test6Stream (symbol string, avgPrice double);\n"
                                + "\n"
                                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= "
                                + "'root',"
                                + " jdbc.driver.name= '" + JDBC_DRIVER_NAME + "')"
                                + "define aggregation AggregationTest2\n"
                                + "from Test4Stream_TestPlan4_004\n"
                                + "select symbol, avg(price) as avgPrice, sum(volume) as totalVolume\n"
                                + "group by symbol\n"
                                + "aggregate by timestamp every sec ... year;\n"
                                + "@info(name = 'query-join2')\n"
                                + "from Test3Stream as P join AggregationTest2 as Q\n"
                                + "on P.symbol == Q.symbol\n"
                                + "within \"2014-02-15 00:00:00 +05:30\", \"2018-12-16 00:00:00 +05:30\"\n"
                                + "per \"days\"\n"
                                + "select P.symbol, Q.avgPrice\n"
                                + "insert into Test6Stream;"),
                "Incorrect Partial Siddhi application created");

        Assert.assertTrue(queryGroupList.get(5).getSiddhiQueries().get(0).getApp()
                        .contains("@App:name('TestPlan4-004-1') \n"
                                + "\n"
                                + "define stream Test4Stream_TestPlan4_004 (symbol string, price double, volume long,"
                                + " timestamp long);\n"
                                + "@source(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',connection.factory"
                                + ".type='queue',destination ='TestPlan4_Test3Stream_0',connection.factory.jndi"
                                + ".name='QueueConnectionFactory',@map(type ='xml')) \n"
                                + "define stream Test3Stream (symbol string, price double, volume long,"
                                + " timestamp long);\n"
                                + "@sink(type='log')\n"
                                + "define stream Test6Stream (symbol string, avgPrice double);\n"
                                + "\n"
                                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= "
                                + "'root', jdbc"
                                + ".driver.name= '" + JDBC_DRIVER_NAME + "')"
                                + "define aggregation AggregationTest2\n"
                                + "from Test4Stream_TestPlan4_004\n"
                                + "select symbol, avg(price) as avgPrice, sum(volume) as totalVolume\n"
                                + "group by symbol\n"
                                + "aggregate by timestamp every sec ... year;\n"
                                + "@info(name = 'query-join2')\n"
                                + "from Test3Stream as P join AggregationTest2 as Q\n"
                                + "on P.symbol == Q.symbol\n"
                                + "within \"2014-02-15 00:00:00 +05:30\", \"2018-12-16 00:00:00 +05:30\"\n"
                                + "per \"days\"\n"
                                + "select P.symbol, Q.avgPrice\n"
                                + "insert into Test6Stream;"),
                "Incorrect Partial Siddhi application created");

        Assert.assertTrue(queryGroupList.get(6).getSiddhiQueries().get(0).getApp()
                        .contains("@source(type = 'http', receiver.url='http://localhost:8080/Stocks', "
                                + "@map(type = 'json'))\n"
                                + "define stream Test2Stream (symbol string, price double, volume long, "
                                + "timestamp long);\n"
                                + "\n"
                                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= "
                                + "'root', jdbc.driver.name= '" + JDBC_DRIVER_NAME + "')"
                                + "define aggregation AggregationTest1\n"
                                + "from Test2Stream\n"
                                + "select symbol, avg(price) as avgPrice, sum(volume) as totalVolume\n"
                                + "group by symbol\n"
                                + "aggregate by timestamp every sec ... year;"),
                "Incorrect Partial Siddhi application created");

        Assert.assertTrue(queryGroupList.get(7).getSiddhiQueries().get(0).getApp()
                        .contains("@source(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',connection.factory"
                                + ".type='topic',destination ='TestPlan4_Test3Stream' , connection.factory.jndi.name="
                                + "'TopicConnectionFactory',@map(type='xml')) \n"
                                + "define stream Test3Stream (symbol string, price double, volume long,"
                                + " timestamp long);\n"
                                + "\n"
                                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= "
                                + "'root', jdbc"
                                + ".driver.name= '" + JDBC_DRIVER_NAME + "')"
                                + "define aggregation AggregetionTest3\n"
                                + "from Test3Stream\n"
                                + "select symbol, avg(price) as avgPrice, sum(volume) as totalVolume\n"
                                + "group by symbol\n"
                                + "aggregate by timestamp every sec ... year;"),
                "Incorrect Partial Siddhi application created");
        Assert.assertTrue(queryGroupList.get(8).getSiddhiQueries().get(0).getApp()
                    .contains("@source(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',connection.factory"
                                + ".type='topic',destination ='TestPlan4_Test4Stream' , connection.factory.jndi.name="
                                + "'TopicConnectionFactory',@map(type='xml')) \n"
                                + "define stream Test4Stream (symbol string, price double, volume long,"
                                + " timestamp long);\n"
                                + "\n"
                                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= "
                                + "'root', jdbc.driver.name= '" + JDBC_DRIVER_NAME + "')"
                                + "define aggregation AggregationTest2\n"
                                + "from Test4Stream\n"
                                + "select symbol, avg(price) as avgPrice, sum(volume) as totalVolume\n"
                                + "group by symbol\n"
                                + "aggregate by timestamp every sec ... year;"),
                "Incorrect Partial Siddhi application created");

        SiddhiManager siddhiManager = new SiddhiManager();

        Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap =
                createSiddhiAppRuntimes(siddhiManager, queryGroupList);
        InputHandler test1StreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(0).getGroupName()).get(0)
                .getInputHandler("Test1Stream");
        InputHandler test3StreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(1).getGroupName()).get(0)
                .getInputHandler("passthroughTest3Stream");
        InputHandler test2StreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(6).getGroupName()).get(0)
                .getInputHandler("Test2Stream");

        for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get(queryGroupList.get(5).getGroupName())) {
            runtime.addCallback("Test6Stream", new StreamCallback() {
                @Override public void receive(Event[] events) {
                    EventPrinter.print(events);
                    count.addAndGet(events.length);
                    for (Event event : events) {
                        errorAssertionCount.incrementAndGet();
                        errorAssertionCount.decrementAndGet();
                    }
                }
            });
        }

        for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get(queryGroupList.get(4).getGroupName())) {
            runtime.addCallback("Test5Stream", new StreamCallback() {
                @Override public void receive(Event[] events) {
                    EventPrinter.print(events);
                    count.addAndGet(events.length);
                    for (Event event : events) {
                        errorAssertionCount.incrementAndGet();
                        errorAssertionCount.decrementAndGet();
                    }
                }
            });
        }

        try {

            test2StreamInputHandler.send(new Object[]{"WSO2", 55.25, 560L, 1496289950000L});
            Thread.sleep(1000);
            test1StreamInputHandler.send(new Object[]{"WSO2", 500.25, 90L, 1496289950005L});
            Thread.sleep(1000);
            test3StreamInputHandler.send(new Object[]{"WSO2", 100.25, 200L, 1496289954000L});

            SiddhiTestHelper.waitForEvents(1500, 2, count, 60000);
            Assert.assertEquals(count.get(), 2);
            Assert.assertEquals(errorAssertionCount.get(), 0);
        } catch (InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            siddhiManager.shutdown();
        }
    }

    /**
     * If an  inmemory aggregation joins with a query then that query should have parallelism = 1.
     * Otherwise SiddhiAppValidation Exception will be thrown.
     */
    @Test(dependsOnMethods = "testMultipleAggregations")
    public void testInmemoryJoinQueueParallelism() {
        String siddhiApp = "@App:name('TestPlan5')\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/SweetProduction', @map(type"
                + " = 'json'))\n"
                + "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , "
                + "quantity int, timestamp string);"
                + "define aggregation stockAggregation "
                + "from stockStream "
                + "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) "
                + "as lastTradeValue  "
                + "group by symbol "
                + "aggregate by timestamp every sec...year; "
                + "@source(type = 'http', receiver.url='http://localhost:8080/inputs', @map(type"
                + " = 'json'))\n"
                + "define stream TestStream (symbol string, value int, startTime string, "
                + "endTime string, perValue string); "
                + "@sink(type='log')\n"
                + "define stream outputStream (symbol string, avgPrice double, sumPrice double, lastTradeValue float);"
                + "\n"
                + "@info(name = 'query-join1')@dist(execGroup='001', parallel = '2')\n"
                + "from TestStream as i join stockAggregation as s "
                + "on i.symbol == s.symbol "
                + "within \"2017-06-01 09:35:00 +05:30\", \"2017-06-01 10:37:57 +05:30\" "
                + "per i.perValue "
                + "select s.symbol, avgPrice, totalPrice as sumPrice, lastTradeValue  "
                + "insert into outputStream; ";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        try {
            SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("has parallelism > 1 while joining with an in-memory "
                    + "aggregation"));
        }
    }

    /**
     * If an  inmemory aggregation joins with a partition then that query should have parallelism = 1.
     * Otherwise SiddhiAppValidation Exception will be thrown.
     */
    @Test(dependsOnMethods = "testInmemoryJoinQueueParallelism")
    public void testInmemoryJoinPartitionParallelism() {
        String siddhiApp = "@App:name('TestPlan6')\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/SweetProductionEP1', @map(type"
                + " = 'json'))\n"
                + "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , "
                + "quantity int, timestamp string);"
                + "define aggregation stockAggregation "
                + "from stockStream "
                + "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) "
                + "as lastTradeValue  "
                + "group by symbol "
                + "aggregate by timestamp every sec...year; "
                + "@source(type = 'http', receiver.url='http://localhost:8080/inputs', @map(type"
                + " = 'json'))\n"
                + "define stream TestStream (symbol string, value int, startTime string, "
                + "endTime string, perValue string); "
                + "@sink(type='log')\n"
                + "define stream outputStream (symbol string, avgPrice double, sumPrice double, lastTradeValue float);"
                + "\n"
                + "@info(name = 'partition-join1')@dist(execGroup='001', parallel = '2')\n"
                + "Partition with (symbol of TestStream)\n"
                + "Begin\n"
                + "from TestStream as i join stockAggregation as s "
                + "on i.symbol == s.symbol "
                + "within \"2017-06-01 09:35:00 +05:30\", \"2017-06-01 10:37:57 +05:30\" "
                + "per i.perValue "
                + "select s.symbol, avgPrice, totalPrice as sumPrice, lastTradeValue  "
                + "insert into outputStream; "
                + "end";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        try {
            SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("has parallelism > 1 while joining with an in-memory "
                    + "aggregation"));
        }
    }

    /**
     * If a siddhi query joins with an in-memory aggregation then that aggregation should be in the same
     * {@link SiddhiQueryGroup} where the query resides.
     */
    @Test(dependsOnMethods = "testInmemoryJoinPartitionParallelism")
    public void testQueryJoinInmemoryAggregation() {
        String siddhiApp = "@App:name('TestPlan7')\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/SweetProduction', @map(type"
                + " = 'json'))\n"
                + "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , "
                + "quantity int, timestamp string);"
                + "define aggregation stockAggregation "
                + "from stockStream "
                + "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) "
                + "as lastTradeValue  "
                + "group by symbol "
                + "aggregate by timestamp every sec...year; "
                + "@source(type = 'http', receiver.url='http://localhost:8080/inputs', @map(type"
                + " = 'json'))\n"
                + "define stream TestStream (symbol string, value int, startTime string, "
                + "endTime string, perValue string); "
                + "@sink(type='log')\n"
                + "define stream outputStream (symbol string, avgPrice double, sumPrice double, lastTradeValue float);"
                + "\n"
                + "@info(name = 'query-join1')@dist(execGroup='001', parallel = '1')\n"
                + "from TestStream as i join stockAggregation as s "
                + "on i.symbol == s.symbol "
                + "within \"2017-06-01 09:35:00 +05:30\", \"2017-06-01 10:37:57 +05:30\" "
                + "per i.perValue "
                + "select s.symbol, avgPrice, totalPrice as sumPrice, lastTradeValue  "
                + "insert into outputStream; ";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new JMSSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);

        Assert.assertTrue(queryGroupList.get(0).getSiddhiQueries().get(0).getApp()
                        .contains("@source(type = 'http', receiver.url='http://localhost:8080/SweetProduction', "
                                + "@map(type = 'json'))\n"
                                + "define stream stockStream (symbol string, price float, lastClosingPrice float, "
                                + "volume long , quantity int, timestamp string);\n"
                                + "@source(type = 'http', receiver.url='http://localhost:8080/inputs', "
                                + "@map(type = 'json'))\n"
                                + "define stream TestStream (symbol string, value int, startTime string, "
                                + "endTime string, perValue string);\n"
                                + "@sink(type='log')\n"
                                + "define stream outputStream (symbol string, avgPrice double, sumPrice double, "
                                + "lastTradeValue float);\n"
                                + "define aggregation stockAggregation from stockStream select symbol, avg(price) "
                                + "as avgPrice, sum(price) as totalPrice, (price * quantity) as lastTradeValue  group "
                                + "by symbol aggregate by timestamp every sec...year;\n"
                                + "@info(name = 'query-join1')\n"
                                + "from TestStream as i join stockAggregation as s on i.symbol == s.symbol "
                                + "within \"2017-06-01 09:35:00 +05:30\", \"2017-06-01 10:37:57 +05:30\" "
                                + "per i.perValue "
                                + "select s.symbol, avgPrice, totalPrice as sumPrice, lastTradeValue  "
                                + "insert into outputStream;"),
                "Incorrect Partial Siddhi application created");

        Assert.assertEquals(queryGroupList.size(), 1);
        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains("aggregation"));

        SiddhiManager siddhiManager = new SiddhiManager();
        Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap =
                createSiddhiAppRuntimes(siddhiManager, queryGroupList);

        for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get(queryGroupList.get(0).getGroupName())) {
            runtime.addCallback("outputStream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    EventPrinter.print(events);
                    count.addAndGet(events.length);
                    for (Event event : events) {
                        errorAssertionCount.incrementAndGet();
                        errorAssertionCount.decrementAndGet();
                    }
                }
            });
        }

        InputHandler stockStreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(0).getGroupName()).get(0)
                .getInputHandler("stockStream");
        InputHandler testStreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(0).getGroupName()).get(0)
                .getInputHandler("TestStream");

        try {
            stockStreamInputHandler.send(new Object[]{"IBM", 50f, 60f, 90L, 6, "2017-06-01 04:05:51"});
            Thread.sleep(1000);
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, 50f, 200L, 26, "2017-06-01 04:06:54"});
            Thread.sleep(1000);
            stockStreamInputHandler.send(new Object[]{"IBM", 50f, 60f, 90L, 8, "2017-06-01 04:07:50"});
            Thread.sleep(1000);
            stockStreamInputHandler.send(new Object[]{"IBM", 900f, 25f, 200L, 60, "2017-06-01 04:08:56"});

            Thread.sleep(2000);
            testStreamInputHandler.send(new Object[]{"IBM", 1, "2017-06-01 09:35:51 +05:30",
                    "2017-06-01 09:35:52 +05:30", "seconds"});
            Thread.sleep(2000);

            SiddhiTestHelper.waitForEvents(2000, 4, count, 60000);
            Assert.assertEquals(count.get(), 4);

        } catch (InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            siddhiManager.shutdown();
        }
    }

    /**
     * If a siddhi partition joins with an in-memory aggregation then that aggregation should be in the same
     * {@link SiddhiQueryGroup} where the partition resides.
     */
    @Test(dependsOnMethods = "testQueryJoinInmemoryAggregation")
    public void testPartitionJoinInmemoryAggregation() {
        String siddhiApp = "@App:name('TestPlan8')\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/SweetProductionEP1', @map(type"
                + " = 'json'))\n"
                + "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , "
                + "quantity int, timestamp string);"
                + "define aggregation stockAggregation "
                + "from stockStream "
                + "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) "
                + "as lastTradeValue  "
                + "group by symbol "
                + "aggregate by timestamp every sec...year; "
                + "@source(type = 'http', receiver.url='http://localhost:8080/inputs', @map(type"
                + " = 'json'))\n"
                + "define stream TestStream (symbol string, value int, startTime string, "
                + "endTime string, perValue string); "
                + "@sink(type='log')\n"
                + "define stream outputStream (symbol string, avgPrice double, sumPrice double, lastTradeValue float);"
                + "\n"
                + "@info(name = 'partition-join1')@dist(execGroup='001', parallel = '1')\n"
                + "Partition with (symbol of TestStream)\n"
                + "Begin\n"
                + "from TestStream as i join stockAggregation as s "
                + "on i.symbol == s.symbol "
                + "within \"2017-06-01 09:35:00 +05:30\", \"2017-06-01 10:37:57 +05:30\" "
                + "per i.perValue "
                + "select s.symbol, avgPrice, totalPrice as sumPrice, lastTradeValue  "
                + "insert into outputStream; "
                + "end";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new JMSSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);

        Assert.assertTrue(queryGroupList.get(0).getSiddhiQueries().get(0).getApp()
                        .contains("@source(type = 'http', receiver.url='http://localhost:8080/SweetProductionEP1',"
                                + " @map(type = 'json'))\n"
                                + "define stream stockStream (symbol string, price float, lastClosingPrice float,"
                                + " volume long , quantity int, timestamp string);\n"
                                + "@source(type = 'http', receiver.url='http://localhost:8080/inputs',"
                                + " @map(type = 'json'))\n"
                                + "define stream TestStream (symbol string, value int, startTime string,"
                                + " endTime string, perValue string);\n"
                                + "@sink(type='log')\n"
                                + "define stream outputStream (symbol string, avgPrice double, sumPrice double,"
                                + " lastTradeValue float);\n"
                                + "define aggregation stockAggregation from stockStream select symbol, avg(price)"
                                + " as avgPrice, sum(price) as totalPrice, (price * quantity) as lastTradeValue"
                                + "  group by symbol aggregate by timestamp every sec...year;\n"
                                + "@info(name = 'partition-join1')\n"
                                + "Partition with (symbol of TestStream)\n"
                                + "Begin\n"
                                + "from TestStream as i join stockAggregation as s on i.symbol == s.symbol within"
                                + " \"2017-06-01 09:35:00 +05:30\", \"2017-06-01 10:37:57 +05:30\" per i.perValue"
                                + " select s.symbol, avgPrice, totalPrice as sumPrice, lastTradeValue"
                                + "  insert into outputStream; end;"),
                "Incorrect Partial Siddhi application created");

        Assert.assertEquals(queryGroupList.size(), 1);
        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains("aggregation"));

        SiddhiManager siddhiManager = new SiddhiManager();
        Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap =
                createSiddhiAppRuntimes(siddhiManager, queryGroupList);

        for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get(queryGroupList.get(0).getGroupName())) {
            runtime.addCallback("outputStream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    EventPrinter.print(events);
                    count.addAndGet(events.length);
                    for (Event event : events) {
                        errorAssertionCount.incrementAndGet();
                        errorAssertionCount.decrementAndGet();
                    }
                }
            });
        }

        InputHandler stockStreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(0).getGroupName()).get(0)
                .getInputHandler("stockStream");
        InputHandler testStreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(0).getGroupName()).get(0)
                .getInputHandler("TestStream");

        try {
            stockStreamInputHandler.send(new Object[]{"IBM", 50f, 60f, 90L, 6, "2017-06-01 04:05:51"});
            Thread.sleep(1000);
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, 50f, 200L, 26, "2017-06-01 04:06:54"});
            Thread.sleep(1000);
            stockStreamInputHandler.send(new Object[]{"IBM", 50f, 60f, 90L, 8, "2017-06-01 04:07:50"});
            Thread.sleep(1000);
            stockStreamInputHandler.send(new Object[]{"IBM", 900f, 25f, 200L, 60, "2017-06-01 04:08:56"});

            Thread.sleep(100);
            testStreamInputHandler.send(new Object[]{"IBM", 1, "2017-06-01 09:35:51 +05:30",
                    "2017-06-01 09:35:52 +05:30", "seconds"});
            Thread.sleep(100);

            SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
            Assert.assertEquals(count.get(), 4);

        } catch (InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            siddhiManager.shutdown();
        }
    }

    /**
     * If a single query/partition element, joins with multiple in-memory aggregations then the element and joining
     * aggregations should be placed in a single siddhiQueryGroup.
     */
    @Test(dependsOnMethods = "testPartitionJoinInmemoryAggregation")
    public void testMultipleInmemoryJoinsSingleGroup() {

        String siddhiApp = "@App:name('TestPlan9')\n"
                + "@App:description('Get knowledge on aggregated topology')\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/SweetProductionEP', @map(type = 'json')"
                + ")\n"
                + "define stream Test1Stream (symbol string, price double, volume long, timestamp long);\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/Stocks', @map(type = 'json'))\n"
                + "define stream Test2Stream (symbol string, price double, volume long, timestamp long);\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/values', @map(type = 'json'))\n"
                + "define stream Test3Stream (symbol string, price double, volume long, timestamp long);\n"
                + "@sink(type='log')\n"
                + "define stream Test4Stream (symbol string, price double, volume long, timestamp long);\n"
                + "@sink(type='log')\n"
                + "define stream Test5Stream (symbol string, price double, volume long);\n"
                + "@sink(type='log')\n"
                + "define stream Test6Stream (symbol string, price double);\n"
                + "define aggregation AggregationTest1\n"
                + "from Test2Stream\n"
                + "select symbol, avg(price) as avgPrice, sum(volume) as totalVolume\n"
                + "group by symbol\n"
                + "aggregate by timestamp every sec ... year;\n"
                + "define aggregation AggregationTest2\n"
                + "from Test4Stream\n"
                + "select symbol, avg(price) as avgPrice, sum(volume) as totalVolume\n"
                + "group by symbol\n"
                + "aggregate by timestamp every sec ... year;\n"
                + "define aggregation AggregationTest3\n"
                + "from Test3Stream\n"
                + "select symbol, avg(price) as avgPrice, sum(volume) as totalVolume\n"
                + "group by symbol\n"
                + "aggregate by timestamp every sec ... year;\n"
                + "@info(name = 'test-filter')@dist(execGroup='001', parallel = '1')\n"
                + "from Test1Stream[price >100]\n"
                + "select *\n"
                + "insert into\n"
                + "Test4Stream;\n"
                + "@info(name = 'query-join1')@dist(execGroup='002', parallel = '1')\n"
                + "from Test1Stream as P join AggregationTest1 as Q\n"
                + "on P.symbol == Q.symbol\n"
                + "within \"2014-02-15 00:00:00 +05:30\", \"2014-03-16 00:00:00 +05:30\"\n"
                + "per \"days\"\n"
                + "select P.symbol, Q.avgPrice, Q.totalVolume\n"
                + "Insert into Test5stream;\n"
                + "@info(name='partition-join')@dist(parallel='1', execGroup='003')\n"
                + "Partition with (symbol of Test4Stream)\n"
                + "begin\n"
                + "From Test4Stream as P join AggregationTest1 as Q\n"
                + "on P.symbol == Q.symbol\n"
                + "within \"2014-02-15 00:00:00 +05:30\", \"2014-03-16 00:00:00 +05:30\"\n"
                + "per \"days\"\n"
                + "select P.symbol, Q.totalVolume\n"
                + "Insert into Test7Stream;\n"
                + "From Test4Stream as M join AggregationTest3 as S\n"
                + "on M.symbol == S.symbol\n"
                + "within \"2014-02-15 00:00:00 +05:30\", \"2014-03-16 00:00:00 +05:30\" \n"
                + "per \"days\"\n"
                + "select M.symbol, S.totalVolume\n"
                + "Insert into Test7Stream;\n"
                + "End;\n"
                + "@info(name = 'query-join2')@dist(parallel='1', execGroup='004')\n"
                + "from Test3Stream as P join AggregationTest2 as Q\n"
                + "on P.symbol == Q.symbol\n"
                + "within \"2014-02-15 00:00:00 +05:30\", \"2014-03-16 00:00:00 +05:30\"\n"
                + "per \"days\"\n"
                + "select P.symbol, Q.avgPrice\n"
                + "Insert into Test6stream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new JMSSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);

        Assert.assertEquals(queryGroupList.size(), 5);
        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains("passthrough"));
        Assert.assertTrue(queryGroupList.get(1).getGroupName().contains("passthrough"));
        Assert.assertTrue(queryGroupList.get(2).getGroupName().contains("001"));
        Assert.assertTrue(queryGroupList.get(3).getGroupName().contains("aggregation"));
        Assert.assertTrue(queryGroupList.get(4).getGroupName().contains("aggregation"));

        Assert.assertTrue(queryGroupList.get(0).getSiddhiQueries().get(0).getApp()
                        .contains("@source(type = 'http', receiver.url='http://localhost:8080/SweetProductionEP', "
                                + "@map(type = 'json'))\n"
                                + "define stream passthroughTest1Stream (symbol string, price double, volume long, "
                                + "timestamp long);\n"
                                + "@sink(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',connection.factory"
                                + ".type='topic',destination = 'TestPlan9_Test1Stream', connection.factory.jndi"
                                + ".name='TopicConnectionFactory',@map(type='xml')) \n"
                                + "define stream Test1Stream (symbol string, price double, volume long,"
                                + " timestamp long);\n"
                                + "from passthroughTest1Stream select * insert into Test1Stream;"),
                "Incorrect Partial Siddhi application created");

        Assert.assertTrue(queryGroupList.get(1).getSiddhiQueries().get(0).getApp()
                        .contains("@source(type = 'http', receiver.url='http://localhost:8080/values', "
                                + "@map(type = 'json'))\n"
                                + "define stream passthroughTest3Stream (symbol string, price double, volume long, "
                                + "timestamp long);\n"
                                + "@sink(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',connection.factory"
                                + ".type='topic',destination = 'TestPlan9_Test3Stream', connection.factory.jndi"
                                + ".name='TopicConnectionFactory',@map(type='xml')) \n"
                                + "define stream Test3Stream (symbol string, price double, volume long, "
                                + "timestamp long);\n"
                                + "from passthroughTest3Stream select * insert into Test3Stream;"),
                "Incorrect Partial Siddhi application created");

        Assert.assertTrue(queryGroupList.get(2).getSiddhiQueries().get(0).getApp()
                        .contains("@source(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',connection"
                                + ".factory.type='topic',destination ='TestPlan9_Test1Stream' , connection"
                                + ".factory.jndi.name='TopicConnectionFactory',@map(type='xml')) \n"
                                + "define stream Test1Stream (symbol string, price double, volume long, timestamp"
                                + " long);\n"
                                + "@sink(type='log')\n"
                                + "@sink(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',connection.factory"
                                + ".type='topic',destination = 'TestPlan9_Test4Stream', connection.factory.jndi"
                                + ".name='TopicConnectionFactory',@map(type='xml')) \n"
                                + "define stream Test4Stream (symbol string, price double, volume long, timestamp"
                                + " long);\n"
                                + "@info(name = 'test-filter')\n"
                                + "from Test1Stream[price >100]\n"
                                + "select *\n"
                                + "insert into\n"
                                + "Test4Stream;"),
                "Incorrect Partial Siddhi application created");

        Assert.assertTrue(queryGroupList.get(4).getSiddhiQueries().get(0).getApp()
                        .contains("@source(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',"
                                + "connection.factory.type='topic',destination ='TestPlan9_Test3Stream' , "
                                + "connection.factory.jndi.name='TopicConnectionFactory',@map(type='xml')) \n"
                                + "define stream Test3Stream (symbol string, price double, volume long, "
                                + "timestamp long);\n"
                                + "@source(type='jms',factory.initial='org.apache.activemq.jndi"
                                + ".ActiveMQInitialContextFactory',provider.url='vm://localhost',"
                                + "connection.factory.type='topic',destination ='TestPlan9_Test4Stream' , "
                                + "connection.factory.jndi.name='TopicConnectionFactory',@map(type='xml')) \n"
                                + "define stream Test4Stream (symbol string, price double, volume long, "
                                + "timestamp long);\n"
                                + "define aggregation AggregationTest2\n"
                                + "from Test4Stream\n"
                                + "select symbol, avg(price) as avgPrice, sum(volume) as totalVolume\n"
                                + "group by symbol\n"
                                + "aggregate by timestamp every sec ... year;\n"
                                + "@info(name = 'query-join2')\n"
                                + "from Test3Stream as P join AggregationTest2 as Q\n"
                                + "on P.symbol == Q.symbol\n"
                                + "within \"2014-02-15 00:00:00 +05:30\", \"2014-03-16 00:00:00 +05:30\"\n"
                                + "per \"days\"\n"
                                + "select P.symbol, Q.avgPrice\n"
                                + "Insert into Test6stream;"),
                "Incorrect Partial Siddhi application created");
    }

    /**
     * Test the behavior when multiple queries joins with single aggregation.
     */
    @Test(dependsOnMethods = "testMultipleInmemoryJoinsSingleGroup")
    public void testSingleAggregatioinMultipleQueryJoins() throws InterruptedException {
        String siddhiApp = "@App:name('TestPlan10')"
                + "@App:description('Incremental aggregation distributed test cases.')\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/SweetProductionEP', "
                + "@map(type = 'json'))\n"
                + "define stream Test1Stream (symbol string, price float, lastClosingPrice float, volume long , "
                + "quantity int, timestamp long);\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/Stocks', @map(type = 'json'))\n"
                + "define stream Test2Stream (symbol string, price double, volume long, timestamp long);\n"
                + "@sink(type='log')\n"
                + "define stream Test4Stream (symbol string, avgPrice double, totalPrice double, lastTradeValue "
                + "float);\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/product', @map(type = 'json'))\n"
                + "define stream Test3Stream (symbol string, price double, volume long, timestamp long);\n"
                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= 'root', jdbc.driver"
                + ".name= '" + JDBC_DRIVER_NAME + "')\n"
                + "define aggregation queryAggregationTest1\n"
                + "from Test1Stream \n"
                + "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity)"
                + "as lastTradeValue\n"
                + "group by symbol\n"
                + "aggregate by timestamp every sec...hour ;\n"
                + "@info(name = 'query-join1')@dist(execGroup='001', parallel = '2')\n"
                + "from Test3Stream as P join queryAggregationTest1 as Q\n"
                + "on P.symbol == Q.symbol\n"
                + "within '2017-06-** **:**:**'\n"
                + "per \"seconds\"\n"
                + "select P.symbol, Q.avgPrice, Q.totalPrice, Q.lastTradeValue\n"
                + "Insert into Test4Stream;\n"
                + "@info(name = 'query-join2')@dist(execGroup='002', parallel = '2')\n"
                + "from Test2Stream as P join queryAggregationTest1 as Q \n"
                + "on P.symbol == Q.symbol\n"
                + "within \"2014-02-15 00:00:00 +05:30\", \"2018-12-16 00:00:00 +05:30\"\n"
                + "per \"seconds\"\n"
                + "select P.symbol, Q.avgPrice, Q.totalPrice, Q.lastTradeValue\n"
                + "Insert into Test4Stream;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new JMSSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();

        Assert.assertEquals(queryGroupList.size(), 5);
        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains("passthrough"));
        Assert.assertTrue(queryGroupList.get(1).getGroupName().contains("passthrough"));
        Assert.assertTrue(queryGroupList.get(2).getGroupName().contains("001"));
        Assert.assertTrue(queryGroupList.get(3).getGroupName().contains("002"));
        Assert.assertTrue(queryGroupList.get(4).getGroupName().contains("aggregation"));

        Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap =
                createSiddhiAppRuntimes(siddhiManager, queryGroupList);
        InputHandler test1StreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(4).getGroupName()).get(0)
                .getInputHandler("Test1Stream");
        InputHandler test3StreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(2).getGroupName()).get(0)
                .getInputHandler("Test3Stream");
        InputHandler test2StreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(3).getGroupName()).get(0)
                .getInputHandler("Test2Stream");


        test1StreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
        test1StreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

        test1StreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289952000L});
        test1StreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289952000L});

        test1StreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289954000L});
        test1StreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289954000L});
        Thread.sleep(100);

        ArrayList<Event> query1EventsList = new ArrayList<>();
        for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get(queryGroupList.get(2).getGroupName())) {
            runtime.addCallback("Test4Stream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    EventPrinter.print(events);
                    count.addAndGet(events.length);
                    for (Event event : events) {
                        query1EventsList.add(event);
                        errorAssertionCount.incrementAndGet();
                        errorAssertionCount.decrementAndGet();
                    }
                }
            });
        }

        ArrayList<Event> query2EventsList = new ArrayList<>();
        for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get(queryGroupList.get(3).getGroupName())) {
            runtime.addCallback("Test4Stream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    EventPrinter.print(events);
                    count.addAndGet(events.length);
                    for (Event event : events) {
                        query2EventsList.add(event);
                        errorAssertionCount.incrementAndGet();
                        errorAssertionCount.decrementAndGet();
                    }
                }
            });
        }

        Thread.sleep(1000);
        test3StreamInputHandler.send(new Object[]{"IBM", 100.25, 200L, 1496289954000L});
        test3StreamInputHandler.send(new Object[]{"WSO2", 100.25, 200L, 1496289954000L});

        Assert.assertEquals(query1EventsList.size(), 3);
        List<Object[]> query1EventsOutputList = new ArrayList<>();
        for (Event event : query1EventsList) {
            query1EventsOutputList.add(event.getData());
        }
        List<Object[]> expected = Arrays.asList(
                new Object[]{"WSO2", 80.0, 160.0, 1600f},
                new Object[]{"WSO2", 60.0, 120.0, 700f},
                new Object[]{"IBM", 100.0, 200.0, 9600f}
        );
        Assert.assertTrue(SiddhiTestHelper.isUnsortedEventsMatch(query1EventsOutputList, expected));

        test2StreamInputHandler.send(new Object[]{"IBM", 100.25, 200L, 1496289954000L});
        test2StreamInputHandler.send(new Object[]{"WSO2", 100.25, 200L, 1496289954000L});

        Assert.assertEquals(query2EventsList.size(), 3);
        List<Object[]> query2EventsOutputList = new ArrayList<>();
        for (Event event : query2EventsList) {
            query2EventsOutputList.add(event.getData());
        }
        Assert.assertTrue(SiddhiTestHelper.isUnsortedEventsMatch(query2EventsOutputList, expected));
        siddhiManager.shutdown();
    }

    /**
     * Test the aggregation behaviour when multiple partitions joins with single aggregation.
     */
    @Test(dependsOnMethods = "testSingleAggregatioinMultipleQueryJoins")
    public void testSingleAggregationMultiplePartitionJoins() throws InterruptedException {
        String siddhiApp = "@App:name('TestPlan11')\n"
                + "@App:description('Incremental aggregation distributed test cases.')\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/SweetProductionEP', "
                + "@map(type = 'json'))\n"
                + "define stream Test1Stream (symbol string, price float, lastClosingPrice float, volume long ,"
                + " quantity int, timestamp long);\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/Stocks', @map(type = 'json'))\n"
                + "define stream Test2Stream (symbol string, price double, volume long, timestamp long);\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/product', @map(type = 'json'))\n"
                + "define stream Test3Stream (symbol string, price double, volume long, timestamp long);\n"
                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= 'root', "
                + "jdbc.driver.name= '" + JDBC_DRIVER_NAME + "')\n"
                + "define aggregation partitionAggregationTest1\n"
                + "from Test1Stream\n"
                + "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity)\n"
                + "as lastTradeValue\n"
                + "group by symbol\n"
                + "aggregate by timestamp every sec...hour ;\n"
                + "@info(name='partition-join1')@dist(parallel='3', execGroup='001')\n"
                + "Partition with (symbol of Test3Stream)\n"
                + "begin\n"
                + "From Test3Stream as P join partitionAggregationTest1 as Q\n"
                + "on P.symbol == Q.symbol\n"
                + "within '2017-06-** **:**:**'\n"
                + "per \"seconds\"\n"
                + "select P.symbol, Q.avgPrice, Q.totalPrice, Q.lastTradeValue\n"
                + "Insert into Test4Stream;\n"
                + "End;\n"
                + "@info(name='partition-join2')@dist(parallel='2', execGroup='002')\n"
                + "Partition with (symbol of Test2Stream)\n"
                + "begin\n"
                + "From Test2Stream as P join partitionAggregationTest1 as Q\n"
                + "on P.symbol == Q.symbol\n"
                + "within '2017-06-** **:**:**'\n"
                + "per \"seconds\"\n"
                + "select P.symbol, Q.avgPrice, Q.totalPrice, Q.lastTradeValue\n"
                + "Insert into Test4Stream;\n"
                + "End;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new JMSSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();

        Assert.assertEquals(queryGroupList.size(), 5);
        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains("passthrough"));
        Assert.assertTrue(queryGroupList.get(1).getGroupName().contains("passthrough"));
        Assert.assertTrue(queryGroupList.get(2).getGroupName().contains("001"));
        Assert.assertTrue(queryGroupList.get(3).getGroupName().contains("002"));
        Assert.assertTrue(queryGroupList.get(4).getGroupName().contains("aggregation"));

        Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap =
                createSiddhiAppRuntimes(siddhiManager, queryGroupList);
        InputHandler test1StreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(4).getGroupName()).get(0)
                .getInputHandler("Test1Stream");
        InputHandler test3StreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(2).getGroupName()).get(0)
                .getInputHandler("Test3Stream");
        InputHandler test2StreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(3).getGroupName()).get(0)
                .getInputHandler("Test2Stream");


        test1StreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
        test1StreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

        test1StreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289952000L});
        test1StreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289952000L});

        test1StreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289954000L});
        test1StreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289954000L});
        Thread.sleep(100);

        ArrayList<Event> partition1EventsList = new ArrayList<>();
        for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get(queryGroupList.get(2).getGroupName())) {
            runtime.addCallback("Test4Stream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    EventPrinter.print(events);
                    count.addAndGet(events.length);
                    for (Event event : events) {
                        partition1EventsList.add(event);
                        errorAssertionCount.incrementAndGet();
                        errorAssertionCount.decrementAndGet();
                    }
                }
            });
        }

        ArrayList<Event> partition2EventsList = new ArrayList<>();
        for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get(queryGroupList.get(3).getGroupName())) {
            runtime.addCallback("Test4Stream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    EventPrinter.print(events);
                    count.addAndGet(events.length);
                    for (Event event : events) {
                        partition2EventsList.add(event);
                        errorAssertionCount.incrementAndGet();
                        errorAssertionCount.decrementAndGet();
                    }
                }
            });
        }

        Thread.sleep(1000);
        test3StreamInputHandler.send(new Object[]{"IBM", 100.25, 200L, 1496289954000L});
        test3StreamInputHandler.send(new Object[]{"WSO2", 100.25, 200L, 1496289954000L});

        Assert.assertEquals(partition1EventsList.size(), 3);
        List<Object[]> query1EventsOutputList = new ArrayList<>();
        for (Event event : partition1EventsList) {
            query1EventsOutputList.add(event.getData());
        }
        List<Object[]> expected = Arrays.asList(
                new Object[]{"WSO2", 80.0, 160.0, 1600f},
                new Object[]{"WSO2", 60.0, 120.0, 700f},
                new Object[]{"IBM", 100.0, 200.0, 9600f}
        );
        Assert.assertTrue(SiddhiTestHelper.isUnsortedEventsMatch(query1EventsOutputList, expected));

        test2StreamInputHandler.send(new Object[]{"IBM", 100.25, 200L, 1496289954000L});
        test2StreamInputHandler.send(new Object[]{"WSO2", 100.25, 200L, 1496289954000L});

        Assert.assertEquals(partition2EventsList.size(), 3);
        List<Object[]> query2EventsOutputList = new ArrayList<>();
        for (Event event : partition2EventsList) {
            query2EventsOutputList.add(event.getData());
        }
        Assert.assertTrue(SiddhiTestHelper.isUnsortedEventsMatch(query2EventsOutputList, expected));
        siddhiManager.shutdown();
    }

    /**
     * Test the behavior when multiple queries resides in a single partition join with distinct aggregations.
     */
    @Test(dependsOnMethods = "testSingleAggregationMultiplePartitionJoins")
    public void testMultipleAggregationsSinglePartitionJoin() throws InterruptedException {
        String siddhiApp = "@App:name('TestPlan12')\n"
                + "@App:description('Incremental aggregation distribution test cases.')\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/SweetProductionEP', "
                + "@map(type = 'json'))\n"
                + "define stream Test1Stream (symbol string, price float, lastClosingPrice float, volume long , "
                + "quantity int, timestamp long);\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/Stocks', @map(type = 'json'))\n"
                + "define stream Test2Stream (symbol string, price float, count int, timestamp long);\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/product', @map(type = 'json'))\n"
                + "define stream Test3Stream (symbol string, price double, volume long, timestamp long);\n"
                + "@source(type = 'http', receiver.url='http://localhost:8080/shortsum', @map(type = 'json'))\n"
                + "define stream Test4Stream (symbol string, price double, volume long, timestamp long);\n"
                + "@sink(type='log')\n"
                + "define stream Test5Stream (symbol string, avgPrice double, totalPrice double, lastTradeValue "
                + "float);\n"
                + "@sink(type='log')\n"
                + "define stream Test6Stream (symbol string, avgPrice double, totalPrice double, totalCount long);\n"
                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= 'root', jdbc.driver"
                + ".name= '" + JDBC_DRIVER_NAME + "')\n"
                + "define aggregation partitionAggregationTest01\n"
                + "from Test1Stream\n"
                + "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity)\n"
                + "as lastTradeValue\n"
                + "group by symbol\n"
                + "aggregate by timestamp every sec...hour ;\n"
                + "@store(type='rdbms', jdbc.url= '" + JDBC_URL + "', username='root', password= 'root', "
                + "jdbc.driver.name= '" + JDBC_DRIVER_NAME + "')\n"
                + "define aggregation partitionAggregationTest02\n"
                + "from Test2Stream\n"
                + "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, sum(count) as totalCount\n"
                + "group by symbol\n"
                + "aggregate by timestamp every sec...hour ;\n"
                + "@info(name='partition-join')@dist(parallel='3', execGroup='001')\n"
                + "Partition with (symbol of Test4Stream)\n"
                + "begin\n"
                + "From Test4Stream as M join partitionAggregationTest02 as S\n"
                + "on M.symbol == S.symbol\n"
                + "within '2017-06-** **:**:**'\n"
                + "per \"seconds\"\n"
                + "select M.symbol, S.avgPrice, S.totalPrice, S.totalCount\n"
                + "Insert into Test6Stream;\n"
                + "From Test3Stream as P join partitionAggregationTest01 as Q\n"
                + "on P.symbol == Q.symbol\n"
                + "within '2017-06-** **:**:**'\n"
                + "per \"seconds\"\n"
                + "select P.symbol, Q.avgPrice, Q.totalPrice, Q.lastTradeValue\n"
                + "Insert into Test5Stream;\n"
                + "End;";

        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new JMSSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology);
        SiddhiManager siddhiManager = new SiddhiManager();

        Assert.assertEquals(queryGroupList.size(), 5);
        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains("passthrough"));
        Assert.assertTrue(queryGroupList.get(0).getGroupName().contains("passthrough"));
        Assert.assertTrue(queryGroupList.get(2).getGroupName().contains("001"));
        Assert.assertTrue(queryGroupList.get(3).getGroupName().contains("aggregation"));
        Assert.assertTrue(queryGroupList.get(4).getGroupName().contains("aggregation"));

        Map<String, List<SiddhiAppRuntime>> siddhiAppRuntimeMap =
                createSiddhiAppRuntimes(siddhiManager, queryGroupList);
        InputHandler test1StreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(3).getGroupName()).get(0)
                .getInputHandler("Test1Stream");
        InputHandler test2StreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(4).getGroupName()).get(0)
                .getInputHandler("Test2Stream");
        InputHandler test3StreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(2).getGroupName()).get(0)
                .getInputHandler("Test3Stream");
        InputHandler test4StreamInputHandler = siddhiAppRuntimeMap.get(queryGroupList.get(2).getGroupName()).get(0)
                .getInputHandler("Test4Stream");


        test1StreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
        test1StreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

        test1StreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289952000L});
        test1StreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289952000L});

        test1StreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289954000L});
        test1StreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289954000L});

        test2StreamInputHandler.send(new Object[]{"WSO2", 50f, 5, 1496289950000L});
        test2StreamInputHandler.send(new Object[]{"WSO2", 70f, 7, 1496289950000L});

        test2StreamInputHandler.send(new Object[]{"WSO2", 60f, 8, 1496289952000L});
        test2StreamInputHandler.send(new Object[]{"WSO2", 100f, 13, 1496289952000L});

        test2StreamInputHandler.send(new Object[]{"IBM", 100f, 9, 1496289954000L});
        test2StreamInputHandler.send(new Object[]{"IBM", 100f, 6, 1496289954000L});

        Thread.sleep(1000);

        ArrayList<Event> partitionQuery1EventsList = new ArrayList<>();
        for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get(queryGroupList.get(2).getGroupName())) {
            runtime.addCallback("Test5Stream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    EventPrinter.print(events);
                    count.addAndGet(events.length);
                    for (Event event : events) {
                        partitionQuery1EventsList.add(event);
                        errorAssertionCount.incrementAndGet();
                        errorAssertionCount.decrementAndGet();
                    }
                }
            });
        }

        ArrayList<Event> partitionQuery2EventsList = new ArrayList<>();
        for (SiddhiAppRuntime runtime : siddhiAppRuntimeMap.get(queryGroupList.get(2).getGroupName())) {
            runtime.addCallback("Test6Stream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    EventPrinter.print(events);
                    count.addAndGet(events.length);
                    for (Event event : events) {
                        partitionQuery2EventsList.add(event);
                        errorAssertionCount.incrementAndGet();
                        errorAssertionCount.decrementAndGet();
                    }
                }
            });
        }

        test4StreamInputHandler.send(new Object[]{"IBM", 100.25, 200L, 1496289954000L});
        test3StreamInputHandler.send(new Object[]{"IBM", 100.25, 200L, 1496289954000L});
        test3StreamInputHandler.send(new Object[]{"WSO2", 100.25, 200L, 1496289954000L});
        test4StreamInputHandler.send(new Object[]{"WSO2", 100.25, 200L, 1496289954000L});
        Thread.sleep(1000);

        List<Object[]> query1EventsOutputList = new ArrayList<>();
        for (Event event : partitionQuery1EventsList) {
            query1EventsOutputList.add(event.getData());
        }
        List<Object[]> partitionQuery1Expected = Arrays.asList(
                new Object[]{"WSO2", 80.0, 160.0, 1600f},
                new Object[]{"WSO2", 60.0, 120.0, 700f},
                new Object[]{"IBM", 100.0, 200.0, 9600f}
        );

        List<Object[]> partitionQuery2Expected = Arrays.asList(
                new Object[]{"WSO2", 80.0, 160.0, 21L},
                new Object[]{"WSO2", 60.0, 120.0, 12L},
                new Object[]{"IBM", 100.0, 200.0, 15L}
        );


        List<Object[]> query2EventsOutputList = new ArrayList<>();
        for (Event event : partitionQuery2EventsList) {
            query2EventsOutputList.add(event.getData());
        }

        Assert.assertEquals(partitionQuery1EventsList.size(), 3);
        Assert.assertEquals(partitionQuery2EventsList.size(), 3);
        Assert.assertTrue(SiddhiTestHelper.isUnsortedEventsMatch(query1EventsOutputList, partitionQuery1Expected));
        Assert.assertTrue(SiddhiTestHelper.isUnsortedEventsMatch(query2EventsOutputList, partitionQuery2Expected));
        siddhiManager.shutdown();
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
