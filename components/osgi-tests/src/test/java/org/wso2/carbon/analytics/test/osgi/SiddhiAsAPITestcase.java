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
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.test.osgi.util.HTTPResponseMessage;
import org.wso2.carbon.analytics.test.osgi.util.TestUtil;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import javax.inject.Inject;
import java.net.URI;


/**
 * SiddhiAsAPI OSGI Tests.
 */

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class SiddhiAsAPITestcase {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SiddhiAsAPITestcase.class);

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {
        return new Option[]{};
    }

    @Test
    public void testSiddhiAPPDeployment() throws Exception {

        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/siddhi-apps";
        String contentType = "text/plain";
        String method = "POST";
        String body = "@Plan:name('SiddhiApp1')\n" +
                "define stream FooStream (symbol string, price float, volume long);\n" +
                "\n" +
                "@source(type='inMemory', topic='symbol', @map(type='passThrough'))Define stream BarStream " +
                "(symbol string, price float, volume long);\n" +
                "\n" +
                "from FooStream\n" +
                "select symbol, price, volume\n" +
                "insert into BarStream;";

        logger.info("Deploying valid Siddhi App trough REST API");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path,
                false, contentType, method);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 201);

        Thread.sleep(10000);

        logger.info("Deploying valid Siddhi App whih is already existing in server trough REST API");
        httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path,
                false, contentType, method);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 409);

        String invalidBody = "@Plan:name('SiddhiApp2')\n" +
                "define stream FooStream (symbol string, price float, volume long);\n" +
                "\n" +
                "@source(type='inMemory', topic='symbol', @map(type='passThrough'))Define stream BarStream " +
                "(symbol string, price float, volume long);\n" +
                "\n" +
                "from FooStream\n" +
                "select symbol, price, volume\n" +
                "";
        logger.info("Deploying invalid Siddhi App trough REST API");
        httpResponseMessage = TestUtil.sendHRequest(invalidBody, baseURI, path,
                false, contentType, method);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 400);
    }

    @Test(dependsOnMethods = {"testSiddhiAPPDeployment"})
    public void testSiddhiAPPRetrieval() throws Exception {

        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/siddhi-apps/SiddhiApp1";
        String method = "GET";
        String contentType = "text/plain";

        logger.info("Retrieving valid Siddhi App trough REST API");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(" ", baseURI, path,
                false, contentType, method);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/siddhi-apps/SiddhiApp2";
        logger.info("Retrieving Siddhi App which does not exists trough REST API");
        httpResponseMessage = TestUtil.sendHRequest(null, baseURI, path,
                false, null, method);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 404);

        path = "/siddhi-apps";
        logger.info("Retrieving all Siddhi App names trough REST API");
        httpResponseMessage = TestUtil.sendHRequest(null, baseURI, path,
                false, null, method);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
    }

    @Test(dependsOnMethods = {"testSiddhiAPPRetrieval"})
    public void testSiddhiAPPSnapshot() throws Exception {

        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/siddhi-apps/SiddhiApp1/backup";
        String method = "POST";
        String contentType = "text/plain";

        logger.info("Taking snapshot of a Siddhi App that exists in server trough REST API");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest("", baseURI, path,
                false, contentType, method);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        Thread.sleep(2000);
        path = "/siddhi-apps/SiddhiApp2/backup";
        logger.info("Taking snapshot of a Siddhi App that does not exist in server trough REST API");
        httpResponseMessage = TestUtil.sendHRequest("", baseURI, path,
                false, null, method);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 404);

    }

    @Test(dependsOnMethods = {"testSiddhiAPPSnapshot"})
    public void testSiddhiAPPRestore() throws Exception {

        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/siddhi-apps/SiddhiApp1/restore";
        String method = "POST";
        String contentType = "text/plain";

        logger.info("Restoring the snapshot (last revision) of a Siddhi App that exists in server trough REST API");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest("", baseURI, path,
                false, contentType, method);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/siddhi-apps/SiddhiApp2/restore";
        logger.info("Restoring the snapshot (last revision) of a Siddhi App that does not exist in " +
                "server trough REST API");
        httpResponseMessage = TestUtil.sendHRequest("", baseURI, path,
                false, null, method);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 404);

        path = "/siddhi-apps/SiddhiApp1/restore?revision=445534";
        logger.info("Restoring the snapshot revison that does not exist of a Siddhi App trough REST API");
        httpResponseMessage = TestUtil.sendHRequest("", baseURI, path,
                false, null, method);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 500);

    }

    @Test(dependsOnMethods = {"testSiddhiAPPRestore"})
    public void testSiddhiAPPDeletion() throws Exception {

        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/siddhi-apps/SiddhiApp2";
        String method = "DELETE";
        String contentType = "text/plain";

        logger.info("Deleting Siddhi App which not exists in server trough REST API");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(null, baseURI, path,
                false, contentType, method);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 404);

        path = "/siddhi-apps/SiddhiApp1";
        logger.info("Deleting valid Siddhi App which exists in server trough REST API");
        httpResponseMessage = TestUtil.sendHRequest(null, baseURI, path,
                false, null, method);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Thread.sleep(6000);

    }

}
