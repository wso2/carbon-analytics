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
import org.wso2.carbon.kernel.CarbonServerInfo;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;

import javax.inject.Inject;
import java.net.URI;

/**
 * Test class for test the org.wso2.carbon.status.dashboard.core module.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class SiddhiSatusDashboardCoreTestcase {
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
        //CarbonDistributionOption.debug(5005);
        return new Option[]{ };
    }

    @Test
    public void testCase1() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/monitoring/apis/workers";
        String contentType = "application/json";
        String method = "GET";
        logger.info("Get all real-time all worker details by reaching each worker nodes.");
        HTTPResponseMessage httpResponseMessage = TestUtil
                .sendHRequest("{\"statsEnable\":true}", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Thread.sleep(10000);
    }
//
//    @Test
//    public void testCase2() throws Exception {
//        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
//        String path = "/monitoring/apis/workers/config";
//        String contentType = "application/json";
//        String method = "GET";
//        logger.info("Reading the dashboard configuration details from the deploment YML of dashboard running server.");
//        HTTPResponseMessage httpResponseMessage = TestUtil
//                .sendHRequest("{\"statsEnable\":true}", baseURI, path, contentType, method,
//                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
//        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
//        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
//        Thread.sleep(10000);
//    }
//
//
//    @Test
//    public void testCase3() throws Exception {
//        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
//        String path = "/monitoring/apis/workers/localhost_9090/ha-status";
//        String contentType = "application/json";
//        String method = "GET";
//        logger.info("Get all HA Status.");
//        HTTPResponseMessage httpResponseMessage = TestUtil
//                .sendHRequest("{\"statsEnable\":true}", baseURI, path, contentType, method,
//                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
//        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
//        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
//        Thread.sleep(10000);
//    }
//
//    @Test
//    public void testCase4() throws Exception {
//        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
//        String path = "/monitoring/apis/workers/localhost_9090/siddhi-apps/appName";
//        String contentType = "application/json";
//        String method = "GET";
//        logger.info("Get text view of the siddhi app.");
//        HTTPResponseMessage httpResponseMessage = TestUtil
//                .sendHRequest("{\"statsEnable\":true}", baseURI, path, contentType, method,
//                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
//        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
//        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
//        Thread.sleep(10000);
//    }

}
