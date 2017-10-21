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
import org.wso2.carbon.analytics.test.osgi.util.SiddhiAppUtil;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.inject.Inject;

import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class FileSystemPersistenceStoreTestIT {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.
            getLogger(FileSystemPersistenceStoreTestIT.class);
    private static final String DEPLOYMENT_FILENAME = "deployment.yaml";
    private static final String PERSISTENCE_FOLDER = "siddhi-app-persistence";
    private static final String SIDDHIAPP_NAME = "SiddhiAppPersistence";

    @Inject
    protected BundleContext bundleContext;

    /**
     * Replace the existing deployment-structure.yaml file with populated deployment-structure.yaml file.
     */
    private Option copyCarbonYAMLOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "conf", "file", "persistence", DEPLOYMENT_FILENAME);
        return copyFile(carbonYmlFilePath, Paths.get("conf", "default", DEPLOYMENT_FILENAME));
    }


    @Configuration
    public Option[] createConfiguration() {
        return new Option[]{
                copyCarbonYAMLOption()
        };
    }

    @Test
    public void testFileSystemPersistence() throws InterruptedException {
        Thread.sleep(5000);
        SiddhiAppRuntime siddhiAppRuntime = SiddhiAppUtil.createSiddhiApp(StreamProcessorDataHolder.getSiddhiManager());

        SiddhiAppUtil.sendDataToStream("WSO2", 500L, siddhiAppRuntime);
        SiddhiAppUtil.sendDataToStream("WSO2", 200L, siddhiAppRuntime);
        SiddhiAppUtil.sendDataToStream("WSO2", 300L, siddhiAppRuntime);
        SiddhiAppUtil.sendDataToStream("WSO2", 250L, siddhiAppRuntime);
        SiddhiAppUtil.sendDataToStream("WSO2", 150L, siddhiAppRuntime);

        File file = new File(PERSISTENCE_FOLDER + File.separator + SIDDHIAPP_NAME);
        Assert.assertEquals(file.exists() && file.isDirectory(), false, "No Folder Created");
        log.info("Waiting for first time interval for state persistence");
        Thread.sleep(60000);
        Assert.assertEquals(file.exists() && file.isDirectory(), true);
        Assert.assertEquals(file.list().length, 1, "There should be one revision persisted");
    }

    @Test(dependsOnMethods = {"testFileSystemPersistence"})
    public void testRestore() throws InterruptedException {
        log.info("Waiting for second time interval for state persistence");
        Thread.sleep(60000);

        SiddhiManager siddhiManager = StreamProcessorDataHolder.getSiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.getSiddhiAppRuntime(SIDDHIAPP_NAME);
        log.info("Shutting Down SiddhiApp");
        siddhiAppRuntime.shutdown();
        log.info("Creating New SiddhiApp with Same Name");
        SiddhiAppRuntime newSiddhiAppRuntime = SiddhiAppUtil.
                createSiddhiApp(StreamProcessorDataHolder.getSiddhiManager());
        newSiddhiAppRuntime.restoreLastRevision();

        SiddhiAppUtil.sendDataToStream("WSO2", 280L, newSiddhiAppRuntime);
        SiddhiAppUtil.sendDataToStream("WSO2", 150L, newSiddhiAppRuntime);
        SiddhiAppUtil.sendDataToStream("WSO2", 200L, newSiddhiAppRuntime);
        SiddhiAppUtil.sendDataToStream("WSO2", 270L, newSiddhiAppRuntime);
        SiddhiAppUtil.sendDataToStream("WSO2", 280L, newSiddhiAppRuntime);
    }

    @Test(dependsOnMethods = {"testRestore"})
    public void testFileSystemPeriodicPersistence() throws InterruptedException {
        Thread.sleep(1000);
        File file = new File(PERSISTENCE_FOLDER + File.separator + SIDDHIAPP_NAME);
        log.info("Waiting for third time interval for state persistence");
        Thread.sleep(62000);

        Assert.assertEquals(file.exists() && file.isDirectory(), true);
        Assert.assertEquals(file.list().length, 2, "There should be two revisions persisted");
    }

}
