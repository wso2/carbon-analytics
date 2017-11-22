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

package org.wso2.carbon.analytics.test.osgi;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.container.options.CarbonDistributionOption;
import org.wso2.carbon.kernel.CarbonServerInfo;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.jmx.MetricsMXBean;
import org.wso2.carbon.siddhi.metrics.core.SiddhiMetricsFactory;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.StatisticsConfiguration;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

import javax.inject.Inject;
import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * SiddhiAsAPI Metrics Tests.
 */

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class SiddhiMetricsTestcase {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SiddhiMetricsTestcase.class);
    private static final String MBEAN_NAME = "org.wso2.carbon:type=Metrics";

    @Inject
    protected BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private MetricService metricService;

    @Inject
    private MetricManagementService metricManagementService;

    private Bundle getBundle(String name) {
        Bundle bundle = null;
        for (Bundle b : bundleContext.getBundles()) {
            if (b.getSymbolicName().equals(name)) {
                bundle = b;
                break;
            }
        }
        Assert.assertNotNull(bundle, "Bundle should be available. Name: " + name);
        return bundle;
    }

    @Test
    public void testMetricsCoreBundle() {
        Bundle coreBundle = getBundle("org.wso2.carbon.metrics.core");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE);
    }

    @Configuration
    public Option[] createConfiguration() {
        return new Option[]{
        };
    }

    @Test
    public void testMetrics() throws Exception {
        SiddhiManager siddhiManager = new SiddhiManager();
        StatisticsConfiguration statisticsConfiguration = new StatisticsConfiguration(new SiddhiMetricsFactory());
        siddhiManager.setStatisticsConfiguration(statisticsConfiguration);
        StreamProcessorDataHolder.setSiddhiManager(siddhiManager);
        String siddhiApp = "@App:name('TestApp')" +
                "@app:statistics(reporter = 'jdbc', interval = '2' )" +
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
        siddhiAppRuntime.enableStats(true);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        siddhiAppRuntime.start();
        MetricsMXBean metricsMXBean = null;
        try {
            ObjectName n = new ObjectName(MBEAN_NAME);
            metricsMXBean = JMX.newMXBeanProxy(ManagementFactory.getPlatformMBeanServer(), n, MetricsMXBean.class);
        } catch (MalformedObjectNameException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(metricsMXBean);
        Assert.assertTrue(metricsMXBean.isEnabled());
        // Check whether the reporters are started at the startup
        Assert.assertTrue(metricsMXBean.isReporterRunning("JDBC"));
        Assert.assertTrue(metricsMXBean.getMetricsCount() > 0);
        Assert.assertEquals(metricsMXBean.getRootLevel(), Level.INFO.name());
        Assert.assertEquals(metricsMXBean.getDefaultSource(), "wso2-sp");
        // TODO: 11/22/17 Fix asserts
//        Assert.assertEquals(metricsMXBean.getMetricLevel("org.wso2.siddhi.SiddhiApps.TestApp.Siddhi.Streams." +
//                "cseEventStream.throughput"), Level.INFO.name());
//        Assert.assertEquals(metricsMXBean.getMetricLevel("org.wso2.siddhi.SiddhiApps.TestApp.Siddhi.Streams" +
//                ".cseEventStream2.throughput"), Level.INFO.name());
        siddhiAppRuntime.shutdown();

    }

}
