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
import org.wso2.carbon.kernel.CarbonServerInfo;

import java.net.URI;
import javax.inject.Inject;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class SimulatorAPITestcase {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SimulatorAPITestcase.class);

    private static final String DEFAULT_USER_NAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";

    private static final String validSingleEventConfig = "{\n" +
            "  \"streamName\": \"CountStream\",\n" +
            "  \"siddhiAppName\": \"ReceiveAndCount\",\n" +
            "  \"data\": ['Customer1', 26, 'USA', 15 ]\n" +
            "}";

    @Inject
    private CarbonServerInfo carbonServerInfo;

    /*@Configuration
    public Option[] createConfiguration() {
        return new Option[0];
    }*/

    @Configuration
    public Option[] createConfiguration() {
        return new Option[]{CarbonDistributionOption.debug(5005)};
    }

    @Test
    public void siddhiAPPDeployment() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/siddhi-apps";
        String contentType = "text/plain";
        String method = "POST";
        String body = "@App:name('ReceiveAndCount')\n" +
                "@source(type='inMemory', topic='symbol', @map(type='passThrough'))\n" +
                "Define stream CustomerInformationStream(name string, age int, country string);\n" +
                "\n" +
                "@sink(type='log')\n" +
                "define stream CountStream(name string, age int, country string, count long);\n" +
                "\n" +
                "@info(name='query1')\n" +
                "from CustomerInformationStream\n" +
                "select name, age, country, count() as count\n" +
                "insert into CountStream;";

        logger.info("Deploying valid Siddhi App through REST API");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");

        Thread.sleep(10000);
    }

    @Test(dependsOnMethods = {"siddhiAPPDeployment"})
    public void testSingleAPI() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/simulation/single";
        String contentType = "text/plain";
        String method = "POST";

        logger.info("Simulation REST API");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(validSingleEventConfig, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Thread.sleep(10000);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");


    }
}
