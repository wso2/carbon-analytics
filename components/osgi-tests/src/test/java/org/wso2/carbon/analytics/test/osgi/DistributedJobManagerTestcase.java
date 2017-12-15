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

import org.apache.log4j.Logger;
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
import org.wso2.carbon.analytics.test.osgi.util.KafkaTestUtil;
import org.wso2.carbon.analytics.test.osgi.util.TestUtil;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.kernel.CarbonServerInfo;
import org.wso2.carbon.stream.processor.core.distribution.DistributionService;
import org.wso2.carbon.stream.processor.core.util.RuntimeMode;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import static org.awaitility.Awaitility.await;
import static org.wso2.carbon.container.options.CarbonDistributionOption.carbonDistribution;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;
import static org.wso2.carbon.container.options.CarbonDistributionOption.debug;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class DistributedJobManagerTestcase {

    private static final String DEFAULT_USER_NAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";

    @Inject
    protected BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private DistributionService distributionService;

    private static final Logger log = Logger.getLogger(DistributedJobManagerTestcase.class);
    private static final String CARBON_YAML_FILENAME = "deployment.yaml";

    /**
     * Replace the existing deployment.yaml file with populated deployment.yaml file.
     */
    private Option copyCarbonYAMLOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "conf", "distributed", "manager",
                CARBON_YAML_FILENAME);
        return copyFile(carbonYmlFilePath, Paths.get("conf", "manager", CARBON_YAML_FILENAME));
    }

    /**
     * Copy the Kafka dependency to lib folder of distribution
     */
    private Option copyKafkaJar() {
        Path ojdbc6FilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        ojdbc6FilePath = Paths.get(basedir, "src", "test", "resources", "lib", "kafka_2.10_0.9.0.1_1.0.0.jar");
        return copyFile(ojdbc6FilePath, Paths.get("lib", "kafka_2.10_0.9.0.1_1.0.0.jar"));
    }
    /**
     * Copy the Kafka dependency to lib folder of distribution
     */
    private Option copyKafkaClientsJar() {
        Path ojdbc6FilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        ojdbc6FilePath = Paths.get(basedir, "src", "test", "resources", "lib", "kafka_clients_0.9.0.1_1.0.0.jar");
        return copyFile(ojdbc6FilePath, Paths.get("lib", "kafka_clients_0.9.0.1_1.0.0.jar"));
    }
    /**
     * Copy the Kafka dependency to lib folder of distribution
     */
    private Option copyMetricsCoreJar() {
        Path ojdbc6FilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        ojdbc6FilePath = Paths.get(basedir, "src", "test", "resources", "lib", "metrics_core_2.2.0_1.0.0.jar");
        return copyFile(ojdbc6FilePath, Paths.get("lib", "metrics_core_2.2.0_1.0.0.jar"));
    }
    /**
     * Copy the Kafka dependency to lib folder of distribution
     */
    private Option copyScalaJar() {
        Path ojdbc6FilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        ojdbc6FilePath = Paths.get(basedir, "src", "test", "resources", "lib", "scala_library_2.10.5_1.0.0.jar");
        return copyFile(ojdbc6FilePath, Paths.get("lib", "scala_library_2.10.5_1.0.0.jar"));
    }
    /**
     * Copy the Kafka dependency to lib folder of distribution
     */
    private Option copyZKClientJar() {
        Path ojdbc6FilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        ojdbc6FilePath = Paths.get(basedir, "src", "test", "resources", "lib", "zkclient_0.7_1.0.0.jar");
        return copyFile(ojdbc6FilePath, Paths.get("lib", "zkclient_0.7_1.0.0.jar"));
    }
    /**
     * Copy the Kafka dependency to lib folder of distribution
     */
    private Option copyZookeeperJar() {
        Path ojdbc6FilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        ojdbc6FilePath = Paths.get(basedir, "src", "test", "resources", "lib", "zookeeper_3.4.6_1.0.0.jar");
        return copyFile(ojdbc6FilePath, Paths.get("lib", "zookeeper_3.4.6_1.0.0.jar"));
    }

    @Configuration
    public Option[] createConfiguration() {
        try {
            KafkaTestUtil.cleanLogDir();
            KafkaTestUtil.setupKafkaBroker();
            Thread.sleep(1000);
        } catch (Exception e) {
            log.error("Error starting Kafka/Zookeeper");
        }
        return new Option[]{
                copyCarbonYAMLOption(),
                copyKafkaJar(),
                copyKafkaClientsJar(),
                copyMetricsCoreJar(),
                copyScalaJar(),
                copyZKClientJar(),
                copyZookeeperJar(),
                carbonDistribution(Paths.get("target", "wso2das-" +
                        System.getProperty("carbon.analytic.version")), "manager")
//                debug(5005)
        };
    }

    @Test
    public void testManagerNode() throws InterruptedException {

        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9190));
        String path = "/siddhi-apps";
        String contentType = "text/plain";
        String method = "POST";
        String siddhiApp = "@App:name('TestPlan') "
                + "define stream TempStream(deviceID long, roomNo int, temp double); "
                + "@info(name = 'query1') @dist(parallel ='1', execGroup='group1')\n "
                + "from TempStream\n"
                + "select *\n"
                + "insert into TempInternalStream;"
                + "@info(name = 'query2') @dist(parallel ='2', execGroup='group2')\n "
                + "partition with ( deviceID of TempInternalStream )\n"
                + "begin\n"
                + "    from TempInternalStream#window.length(10)\n"
                + "    select roomNo, deviceID, max(temp) as maxTemp\n"
                + "    insert into DeviceTempStream\n"
                + "end;";

        log.info("Deploying valid Siddhi App through REST API");
        HTTPResponseMessage httpResponseMessage = sendHRequest(siddhiApp, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 201);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        await().atMost(1, TimeUnit.MINUTES).until(() -> distributionService.getRuntimeMode() == RuntimeMode.MANAGER);
        await().atMost(1, TimeUnit.MINUTES).until(() -> distributionService.isLeader());
        await().atMost(1, TimeUnit.MINUTES).until(() -> distributionService.isDistributed("TestPlan"));
        log.info("Undeploying Siddhiapp");
        distributionService.undeploy("TestPlan");
        KafkaTestUtil.stopKafkaBroker();
    }

    private HTTPResponseMessage sendHRequest(String body, URI baseURI, String path, String contentType,
                                             String methodType, Boolean auth, String userName, String password) {
        TestUtil testUtil = new TestUtil(baseURI, path, auth, false, methodType,
                contentType, userName, password);
        testUtil.addBodyContent(body);
        return testUtil.getResponse();
    }
}
