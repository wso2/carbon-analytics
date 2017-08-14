/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.analytics.test.osgi;

import org.ops4j.pax.exam.ExamFactory;
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

import java.net.URI;
import javax.inject.Inject;

/**
 * Config Reader Test Case.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class ConfigReaderTestCase {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ConfigReaderTestCase.class);

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Test
    public void testConfigReaderFunctionalityTest1() throws Exception {

        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/siddhi-apps";
        String contentType = "text/plain";
        String method = "POST";
        String body = "@App:name('SiddhiApp1')\n" +
                "@Store(ref='store5')" +
                "define table FooTable (symbol string, price float, volume long);";

        logger.info("Store reference support -  Configuring RDBMS extension");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
                true, "admin", "admin");
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 201);
        Thread.sleep(10000);
    }

    @Test
    public void testConfigReaderFunctionalityTest2() throws Exception {

        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/siddhi-apps";
        String contentType = "text/plain";
        String method = "POST";
        String body = "@App:name('SiddhiApp2')\n" +
                "@Store(ref='store6')" +
                "define table FooTable (symbol string, price float, volume long);";

        logger.info("Store reference support -  Configuring solr extension");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
                true, "admin", "admin");
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 201);
        Thread.sleep(10000);
    }

    @Test
    public void testConfigReaderFunctionalityTest3() throws Exception {

        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/siddhi-apps";
        String contentType = "text/plain";
        String method = "POST";
        String body = "@App:name('SiddhiApp3')\n" +
                "@Store(ref='store1')" +
                "define table FooTable (symbol string, price float, volume long);";

        logger.info("Store reference support -  Configuring MongoDB extension");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
                true, "admin", "admin");
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 201);
        Thread.sleep(10000);
    }

    @Test
    public void testConfigReaderFunctionalityTest4() throws Exception {

        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/siddhi-apps";
        String contentType = "text/plain";
        String method = "POST";
        String body = "@App:name('SiddhiApp4')\n" +
                "@Store(ref='store4')" +
                "define table FooTable (symbol string, price float, volume long);";

        logger.info("Store reference support -  Configuring unknown extension");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
                true, "admin", "admin");
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 400);
        Thread.sleep(10000);
    }

    @Test
    public void testConfigReaderFunctionalityTest5() throws Exception {

        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/siddhi-apps";
        String contentType = "text/plain";
        String method = "POST";
        String body = "@App:name('SiddhiApp5')\n" +
                "@Store(ref='store8')" +
                "define table FooTable (symbol string, price float, volume long);";

        logger.info("Store reference support -  Configuring undefined store");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
                true, "admin", "admin");
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 400);
        Thread.sleep(10000);
    }

    @Test(enabled = false)
    public void testConfigReaderFunctionalityTest6() throws Exception {

        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/siddhi-apps";
        String contentType = "text/plain";
        String method = "POST";
        String body = "@App:name('SiddhiApp6')\n" +
                "@Store(ref='store7')" +
                "define table FooTable (symbol string, price float, volume long);";

        logger.info("Store reference support -  Configuring with malformed store");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
                true, "admin", "admin");
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 400);
        Thread.sleep(10000);
    }

    @Test
    public void testConfigReaderFunctionalityTest7() throws Exception {

        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/siddhi-apps";
        String contentType = "text/plain";
        String method = "POST";
        String body = "@App:name('SiddhiApp7')\n" +
                "@Store(ref='store2')" +
                "define table FooTable (symbol string, price float, volume long);";

        logger.info("Store reference support -  Configuring with missing requirements");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
                true, "admin", "admin");
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 400);
        Thread.sleep(10000);
    }

}
