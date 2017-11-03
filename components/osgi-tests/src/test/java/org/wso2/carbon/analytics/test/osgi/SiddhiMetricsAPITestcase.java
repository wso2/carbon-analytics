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
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.test.osgi.util.HTTPResponseMessage;
import org.wso2.carbon.analytics.test.osgi.util.TestUtil;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.container.options.CarbonDistributionOption;
import org.wso2.carbon.kernel.CarbonServerInfo;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;

import javax.inject.Inject;
import java.net.URI;

/**
 * SiddhiAsAPI OSGI Tests.
 */

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class SiddhiMetricsAPITestcase {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SiddhiMetricsAPITestcase.class);

    private static final String DEFAULT_USER_NAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";

    @Inject
    protected BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private MetricService metricService;

    @Inject
    private MetricManagementService metricManagementService;

    @Configuration
    public Option[] createConfiguration() {
        return new Option[]{ };
        }

    // TODO: 11/3/17 rewrite API chaging query param to body param
    @Test
    public void testSystemDetails() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/system-details";
        String method = "GET";
        String contentType = "application/json";
        logger.info("Retrieving active Siddhi App through REST API");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(" ", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
    }


    @Test
    public void testGetRealTimeStatistics() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/statistics";
        String method = "GET";
        String contentType = "application/json";
        logger.info("Retrieving active Siddhi App through REST API");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(" ", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
    }


}
