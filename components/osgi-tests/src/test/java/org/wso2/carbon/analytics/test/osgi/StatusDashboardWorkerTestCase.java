/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.analytics.test.osgi;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.test.osgi.util.HTTPResponseMessage;
import org.wso2.carbon.analytics.test.osgi.util.TestUtil;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.container.options.CarbonDistributionOption;
import org.wso2.carbon.datasource.core.api.DataSourceManagementService;
import org.wso2.carbon.datasource.core.beans.DataSourceMetadata;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.kernel.CarbonServerInfo;

import javax.inject.Inject;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyOSGiLibBundle;

/**
 * Test class for test the org.wso2.carbon.status.dashboard.core module.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class StatusDashboardWorkerTestCase {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SiddhiMetricsAPITestcase.class);

    private static final String DATASOURCE_NAME = "WSO2_STATUS_DASHBOARD_DB";
    private static final String DEFAULT_USER_NAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";

    @Inject
    private DataSourceManagementService dataSourceManagementService;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {
        return new Option[] { copyOSGiLibBundle(maven().artifactId("h2").groupId("com.h2database").version("1.4.195")),
                copyDSConfigFile()
        };
    }

    private static Option copyDSConfigFile() {
        return copyFile(Paths.get("src", "test", "resources", "conf", "deployment.yaml"),
                Paths.get("conf", "default", "deployment.yaml"));
    }

    @Test
    public void testDataSourceManagementServiceInject() {
        Assert.assertNotNull(dataSourceManagementService, "DataSourceManagementService not found");
    }

    @Test
    public void testGetDataSource() {
        try {
            List<DataSourceMetadata> list  = dataSourceManagementService.getDataSource();
            Assert.assertEquals(list.size(), 3, "There are three data source registered");
        } catch (DataSourceException e) {
            Assert.fail("Thew DataSourceException when fetching data sources");
        }
    }

    @Test
    public void testDashboardCoreApis() throws Exception {
        URI baseURI = URI.create(String.format("https://%s:%d", "localhost", 9443));
        String path = "/monitoring/apis/workers";
        String contentType = "application/json";
        String method = "POST";
        logger.info("Add a worker");
        HTTPResponseMessage httpResponseMessage = TestUtil
                .sendHRequest("{\"port\":9443\n , \"host\":\"localhost\"}", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        logger.info(httpResponseMessage.getMessage());
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        method = "GET";
        logger.info("Get All workers");
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        path = "/monitoring/apis/workers/config";
        method = "GET";
        logger.info("Get dashboard configs");
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        path = "/monitoring/apis/workers/localhost_9443/system-details";
        method = "GET";
        logger.info("Get worker general details");
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        testValidSiddhiAPPDeployment();
        Thread.sleep(1000);
        path = "/monitoring/apis/workers/localhost_9443/history";
        method = "GET";
        logger.info("Get worker history");
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        path = "/monitoring/apis/workers/localhost_9443/history?more=true";
        method = "GET";
        logger.info("Get worker history");
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp/history";
        method = "GET";
        logger.info("Get siddhi app history");
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp/components/streams/cseEventStream/history";
        method = "GET";
        logger.info("Get siddhi app history");
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/ha-status";
        method = "GET";
        logger.info("Get worker HA status");
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp";
        method = "GET";
        logger.info("Get siddhi app");
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        path = "/monitoring/apis/workers/localhost_9443";
        method = "DELETE";
        logger.info("Delete worker workers");
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

    }


    public void testValidSiddhiAPPDeployment() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/siddhi-apps";
        String contentType = "text/plain";
        String method = "POST";
        String body = "@App:name('CoreTestApp')" +
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

        logger.info("Deploying valid Siddhi App through REST API");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 201);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");

        Thread.sleep(10000);
    }
}
