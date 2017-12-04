package org.wso2.carbon.analytics.test.osgi;

import com.google.gson.Gson;
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
import org.wso2.carbon.analytics.test.osgi.util.ConnectionUtil;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.container.options.CarbonDistributionOption;
import org.wso2.carbon.kernel.CarbonServerInfo;

import java.io.File;
import java.net.URI;
import javax.inject.Inject;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class SimulatorAPITestcase {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SimulatorAPITestcase.class);

    private static final String APP_NAME = "simulator-api-test";
    private static final String SIMULATOR_API_BUNDLE_NAME = "org.wso2.carbon.event.event.simulator.core";
    private static final int HTTP_PORT = 9090;
    private static final String HOSTNAME = "localhost";
    private static final String API_CONTEXT_PATH = "/simulation";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String HTTP_METHOD_POST = "POST";
    private final String DEFAULT_USER_NAME = "admin";
    private final String DEFAULT_PASSWORD = "admin";
    private final Gson gson = new Gson();

    private static final String validSingleEventConfig = "{\n" +
            "  \"streamName\": \"CountStream\",\n" +
            "  \"siddhiAppName\": \"ReceiveAndCount\",\n" +
            "  \"data\": ['Customer1', 26, 'USA', 15 ]\n" +
            "}";

    private static final String validFeedEventConfig = "{\"properties\":{\"simulationName\":\"FeedSimulation\"," +
            "\"startTimestamp\":\"\",\"endTimestamp\":\"\",\"noOfEvents\":\"\",\"description\":\"\"," +
            "\"timeInterval\":\"1000\"},\"sources\":[{\"siddhiAppName\":\"ReceiveAndCount\",\"streamName\":" +
            "\"CountStream\",\"timestampInterval\":\"1000\",\"simulationType\":\"CSV_SIMULATION\",\"fileName\":" +
            "\"sampleCSV.csv\",\"delimiter\":\",\",\"isOrdered\":true,\"indices\":\"0,1,2,3\"}]}";

    //todo:remove
    private static String sampleCSVFile =
            "/home/chiran/Desktop/WSO2/carbon-analytics/components/osgi-tests/src/test/resources/files/sampleCSV.csv";

    @Inject
    private CarbonServerInfo carbonServerInfo;

   /* @Configuration
    public Option[] createConfiguration() {
        return new Option[0];
    }*/
    @Configuration
    public Option[] createConfiguration() {
        return new Option[]{CarbonDistributionOption.debug(5005)};
    }

    @Test
    public void siddhiAPPDeployment() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", HOSTNAME, HTTP_PORT));
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
        HTTPResponseMessage httpResponseMessage = sendHRequest(body, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 201);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");

        Thread.sleep(10000);
    }

    @Test(dependsOnMethods = {"siddhiAPPDeployment"})
    public void testSingleAPI() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", HOSTNAME, HTTP_PORT));
        String path = "/simulation/single";
        String contentType = "text/plain";
        String method = "POST";
        logger.info("Simulation REST API");
        HTTPResponseMessage httpResponseMessage = sendHRequest(validSingleEventConfig, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Thread.sleep(10000);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test(dependsOnMethods = {"testSingleAPI"})
    public void testFilesApi() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", HOSTNAME, HTTP_PORT));
        String fileUploadPath = "/simulation/files";
        String method = "POST";
        String contentType = "multipart/form-data";
        File uploadFile = new File(sampleCSVFile);
        logger.info("Uploading Simulation CSV file");
        ConnectionUtil connectionUtil = new ConnectionUtil(baseURI, fileUploadPath, true, true,
                method, contentType, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        connectionUtil.addFilePart("file", uploadFile);
        HTTPResponseMessage httpResponseMessage = connectionUtil.getResponse();
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 201);
        Thread.sleep(10000);
    }

    @Test(dependsOnMethods = {"testFilesApi"})
    public void testFeedApi() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", HOSTNAME, HTTP_PORT));
        String simlationPath = "/simulation/feed/";
        String simulationContentType = "text/plain";
        String method = "POST";
        logger.info("Uploading Simulation Configuration through API");
        HTTPResponseMessage httpResponseMessage = sendHRequest(validFeedEventConfig, baseURI, simlationPath,
                simulationContentType, method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 201);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Thread.sleep(10000);
    }

    @Test(dependsOnMethods = {"testFeedApi"})
    public void testRunFeedApi() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/simulation/feed/FeedSimulation/?action=run";
        String method = "POST";
        String contentType = null;
        logger.info("Simulation starting");
        HTTPResponseMessage httpResponseMessage = sendHRequest(validSingleEventConfig, baseURI, path, contentType,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Thread.sleep(10000);
    }

    @Test(dependsOnMethods = {"testRunFeedApi"})
    public void testPauseFeedApi() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String method = "POST";
        String path = "/simulation/feed/FeedSimulation/?action=pause";
        logger.info("Pause Simulation Configuration API");
        HTTPResponseMessage httpResponseMessage = sendHRequest(validSingleEventConfig, baseURI, path, null,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
    }

    @Test(dependsOnMethods = {"testPauseFeedApi"})
    public void testStopFeedApi() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String method = "POST";
        String path = "/simulation/feed/FeedSimulation/?action=stop";
        logger.info("Stop Simulation Configuration API");
        HTTPResponseMessage httpResponseMessage = sendHRequest(validSingleEventConfig, baseURI, path, null,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
    }

    @Test(dependsOnMethods = {"testStopFeedApi"})
    public void testGetFilesApi() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", HOSTNAME, HTTP_PORT));
        String path = "/simulation/files";
        String method = "GET";
        logger.info("Get names of all uploaded CSV files");
        HTTPResponseMessage httpResponseMessage = sendHRequest(validSingleEventConfig, baseURI, path, null,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        logger.info("######"+httpResponseMessage.getMessage());
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Thread.sleep(10000);
    }

    @Test(dependsOnMethods = {"testGetFilesApi"})
    public void testDeleteFilesApi() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", HOSTNAME, HTTP_PORT));
        String path = "/simulation/files/sampleCSV.csv";
        String method = "DELETE";
        logger.info("Deleting the CSV file");
        HTTPResponseMessage httpResponseMessage = sendHRequest(validSingleEventConfig, baseURI, path, null,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Thread.sleep(10000);
    }

    private HTTPResponseMessage sendHRequest(String body, URI baseURI, String path, String contentType,
                                             String methodType, Boolean auth, String userName, String password) {
        ConnectionUtil connectionUtil = new ConnectionUtil(baseURI, path, auth, true, methodType,
                contentType, userName, password);
        connectionUtil.addBodyContent(body);
        return connectionUtil.getResponse();
    }
}
