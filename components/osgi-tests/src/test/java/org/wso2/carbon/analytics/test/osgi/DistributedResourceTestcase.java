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
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.kernel.CarbonServerInfo;
import org.wso2.carbon.stream.processor.core.distribution.DistributionService;
import org.wso2.carbon.stream.processor.core.util.RuntimeMode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import static org.awaitility.Awaitility.await;
import static org.wso2.carbon.container.options.CarbonDistributionOption.carbonDistribution;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class DistributedResourceTestcase {

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
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "conf", "distributed", "resource",
                CARBON_YAML_FILENAME);
        return copyFile(carbonYmlFilePath, Paths.get("conf", "worker", CARBON_YAML_FILENAME));
    }

    @Configuration
    public Option[] createConfiguration() {
        return new Option[]{
                copyCarbonYAMLOption(),
                carbonDistribution(Paths.get("target", "wso2das-" +
                        System.getProperty("carbon.analytic.version")), "worker")};
    }

    @Test
    public void testResourceNode() throws InterruptedException {
        Thread.sleep(5000);
        await().atMost(1, TimeUnit.MINUTES).until(() -> distributionService.getRuntimeMode() == RuntimeMode.RESOURCE);
    }
}
