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

package org.wso2.carbon.siddhi.metrics.core;

import org.apache.log4j.Logger;
import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.Metrics;
import org.wso2.carbon.siddhi.metrics.core.internal.SiddhiMetricsDataHolder;
import org.wso2.carbon.siddhi.metrics.core.internal.SiddhiStatisticsManager;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.StatisticsConfiguration;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.statistics.EventBufferHolder;

import java.util.concurrent.TimeUnit;

/**
 * Test case for carbon metrics inside siddhi.
 */
public class StatisticsTestCase {
    private static final Logger log = Logger.getLogger(StatisticsTestCase.class);
    private int count;
    private boolean eventArrived;
    protected static Metrics metrics;
    
    protected static MetricService metricService;
    
    protected static MetricManagementService metricManagementService;
    
    @BeforeSuite
    protected void initMetrics() throws Exception {
        // Initialize the Metrics
        System.setProperty("metrics.target", "target");
        metrics = new Metrics(TestUtils.getConfigProvider("metrics.yaml"));
        metrics.activate();
        metricService = metrics.getMetricService();
        metricManagementService = metrics.getMetricManagementService();
    }
    
    @BeforeMethod
    public void init() {
        metricManagementService.setRootLevel(Level.ALL);
        metricManagementService.stopReporters();
        count = 0;
        eventArrived = false;
        SiddhiMetricsDataHolder.getInstance().setMetricService(metricService);
        SiddhiMetricsDataHolder.getInstance().setMetricManagementService(metricManagementService);
        metricManagementService.startReporter("Console");
    }
    
    @AfterSuite
    protected static void destroy() throws Exception {
        metrics.deactivate();
    }
    
    @Test
    public void statisticsMetricsFactory() throws InterruptedException {
        StatisticsConfiguration statisticsConfiguration = new StatisticsConfiguration(new SiddhiMetricsFactory());
        SiddhiLatencyMetric latencyTracker = (SiddhiLatencyMetric) statisticsConfiguration
                .getFactory().createLatencyTracker("test.latency", new SiddhiStatisticsManager(
                        "MetricsTest",
                        true));
        AssertJUnit.assertEquals("test.latency", latencyTracker.getName());
        SiddhiThroughputMetric throughputTracker = (SiddhiThroughputMetric) statisticsConfiguration
                .getFactory().createThroughputTracker("test.throughput", new SiddhiStatisticsManager(
                        "MetricsTest",
                        true));
        AssertJUnit.assertEquals("test.throughput", throughputTracker.getName());
        
        SiddhiMemoryUsageMetric memoryUsageTracker = (SiddhiMemoryUsageMetric) statisticsConfiguration
                .getFactory()
                .createMemoryUsageTracker(new SiddhiStatisticsManager("MetricsTest",
                        true));
        mockmoryObject mockmoryObject = new mockmoryObject("test.memory");
        memoryUsageTracker.registerObject(mockmoryObject, "test.memory");
        AssertJUnit.assertEquals("test.memory", memoryUsageTracker.getName(mockmoryObject));
        SiddhiBufferedEventsMetric bufferedEventsTracker = (SiddhiBufferedEventsMetric) statisticsConfiguration
                .getFactory().createBufferSizeTracker(new SiddhiStatisticsManager("MetricsTest",
                        true));
        EventBufferHolder eventBufferHolder = new EventBufferHolder() {
            @Override
            public long getBufferedEvents() {
                return 1;
            }
            
            @Override
            public boolean containsBufferedEvents() {
                return true;
            }
        };
        bufferedEventsTracker.registerEventBufferHolder(eventBufferHolder, "test.size");
        AssertJUnit.assertEquals("test.size", bufferedEventsTracker.getName(eventBufferHolder));
    }
    
    @Test
    public void statisticsTest1() throws InterruptedException {
        log.info("statistics test 1");
        SiddhiManager siddhiManager = new SiddhiManager();
        StatisticsConfiguration statisticsConfiguration = new StatisticsConfiguration(new SiddhiMetricsFactory());
        siddhiManager.setStatisticsConfiguration(statisticsConfiguration);
        String siddhiApp = "@app:name('MetricsTest')" +
                "@app:statistics(reporter = 'console', interval = '1' )" +
                " " +
                "define stream cseEventStream (symbol string, price float, volume int);" +
                "define stream cseEventStream2 (symbol string, price float, volume int);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream[70 > price] " +
                "select * " +
                "insert into outputStream ;" +
                "" +
                "@info(name = 'query2') " +
                "from cseEventStream[volume > 90] " +
                "select * " +
                "insert into outputStream ;";
        
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                for (Event event : events) {
                    count++;
                    AssertJUnit.assertTrue("IBM".equals(event.getData(0)) ||
                            "WSO2".equals(event.getData(0)));
                }
            }
        });
        
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        for (int i = 0; i < 1; i++) {
            inputHandler.send(new Object[] {"WSO2", 55.6f, 100});
            inputHandler.send(new Object[] {"IBM", 75.6f, 100});
        }
        
        String name1 = MetricService.name("org.wso2.siddhi.SiddhiApps.MetricsTest",
                "Siddhi.Queries.query1.memory");
        String name2 = MetricService.name("org.wso2.siddhi.SiddhiApps.MetricsTest",
                "Siddhi.Queries.query2.memory");
        String name3 = MetricService.name("org.wso2.siddhi.SiddhiApps.MetricsTest",
                "Siddhi.Streams.cseEventStream.throughput");
        String name4 = MetricService.name("org.wso2.siddhi.SiddhiApps.MetricsTest",
                "Siddhi.Streams.cseEventStream2.throughput");
        String name5 = MetricService.name("org.wso2.siddhi.SiddhiApps.MetricsTest",
                "Siddhi.Streams.outputStream.throughput");
        String name6 = MetricService.name("org.wso2.siddhi.SiddhiApps.MetricsTest",
                "Siddhi.Queries.query1.latency");
        String name7 = MetricService.name("org.wso2.siddhi.SiddhiApps.MetricsTest",
                "Siddhi.Queries.query2.latency");
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> {
            boolean condition = metricManagementService.getMetricLevel(name1).name() != null &&
                    metricManagementService.getMetricLevel(name2).name() != null &&
                    metricManagementService.getMetricLevel(name3).name() != null &&
                    metricManagementService.getMetricLevel(name4).name() != null &&
                    metricManagementService.getMetricLevel(name5).name() != null &&
                    metricManagementService.getMetricLevel(name6).name() != null &&
                    metricManagementService.getMetricLevel(name7).name() != null;
            return condition;
        });
        Assert.assertTrue(metricManagementService.isReporterRunning("Console"));
        AssertJUnit.assertEquals("INFO", metricManagementService.getMetricLevel(name1).name());
        AssertJUnit.assertEquals("INFO", metricManagementService.getMetricLevel(name2).name());
        AssertJUnit.assertEquals("INFO", metricManagementService.getMetricLevel(name3).name());
        AssertJUnit.assertEquals("INFO", metricManagementService.getMetricLevel(name4).name());
        AssertJUnit.assertEquals("INFO", metricManagementService.getMetricLevel(name5).name());
        AssertJUnit.assertEquals("INFO", metricManagementService.getMetricLevel(name6).name());
        AssertJUnit.assertEquals("INFO", metricManagementService.getMetricLevel(name7).name());
        
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(3, count);
        metricManagementService.stopReporter("Console");
        siddhiAppRuntime.shutdown();
    }
    
    @Test
    public void asyncTest5() throws InterruptedException {
        log.info("async test 5");
        SiddhiManager siddhiManager = new SiddhiManager();
        StatisticsConfiguration statisticsConfiguration = new StatisticsConfiguration(new SiddhiMetricsFactory());
        siddhiManager.setStatisticsConfiguration(statisticsConfiguration);
        String siddhiApp = "" +
                "@app:name('MetricsTest2')" +
                "@app:statistics(reporter = 'console', interval = '1' )" +
                "@async(buffer.size='1')" +
                "define stream cseEventStream (symbol string, price float, volume int);" +
                "" +
                "define stream cseEventStream2 (symbol string, price float, volume int);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream[70 > price] " +
                "select * " +
                "insert into innerStream ;" +
                "" +
                "@info(name = 'query2') " +
                "from innerStream[volume > 90] " +
                "select * " +
                "insert into outputStream ;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
                eventArrived = true;
                for (Event event : events) {
                    count++;
                }
            }
            
        });
        
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[] {"WSO2", 55.6f, 100});
        inputHandler.send(new Object[] {"IBM", 9.6f, 100});
        inputHandler.send(new Object[] {"FB", 7.6f, 100});
        inputHandler.send(new Object[] {"GOOG", 5.6f, 100});
        inputHandler.send(new Object[] {"WSO2", 15.6f, 100});
        String name1 = MetricService.name("org.wso2.siddhi.SiddhiApps.MetricsTest2.Siddhi.Streams" +
                ".cseEventStream.size");
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> {
            return metricManagementService.getMetricLevel(name1).name() != null;
        });
        Assert.assertTrue(metricManagementService.isReporterRunning("Console"));
        AssertJUnit.assertEquals("INFO", metricManagementService.getMetricLevel(name1).name());
        siddhiAppRuntime.shutdown();
        AssertJUnit.assertTrue(eventArrived);
        metricManagementService.stopReporter("Console");
    }
    
    private class mockmoryObject {
        String name;
        
        public mockmoryObject(String name) {
            this.name = name;
        }
        
        String getName() {
            return name;
        }
    }
}
