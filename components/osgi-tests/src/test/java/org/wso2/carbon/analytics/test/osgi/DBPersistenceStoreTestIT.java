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

import com.zaxxer.hikari.HikariDataSource;
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
import org.wso2.carbon.analytics.test.osgi.util.RDBMSType;
import org.wso2.carbon.analytics.test.osgi.util.RDBMSConfig;
import org.wso2.carbon.analytics.test.osgi.util.SiddhiAppUtil;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.container.options.CarbonDistributionOption;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.sql.DataSource;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class DBPersistenceStoreTestIT {

    @Inject
    protected BundleContext bundleContext;

    @Inject
    private DataSourceService dataSourceService;

    private static final Logger log = org.apache.log4j.Logger.getLogger(DBPersistenceStoreTestIT.class);
    private static final String CARBON_YAML_FILENAME = "deployment.yaml";
    private static final String CARBON_DS_CONFIG_FILENAME = "master-datasources.xml";
    private static final String TABLE_NAME = "PERSISTENCE_TABLE";
    private static final String SIDDHIAPP_NAME = "SiddhiAppPersistence";

    //TODO: This query should support all the databases
    private final String selectLastQuery = "SELECT siddhiAppName FROM " + TABLE_NAME + " WHERE siddhiAppName = ? ORDER BY id DESC";

    /**
     * Replace the existing deployment.yaml file with populated deployment.yaml file.
     */
    private Option copyCarbonYAMLOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "conf", "db", "persistence", CARBON_YAML_FILENAME);
        return copyFile(carbonYmlFilePath, Paths.get("conf", CARBON_YAML_FILENAME));
    }

    /**
     * Replace the existing master-datasources.xml file with populated master-datasources.xml file.
     */
    private Option copyDSOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "conf", "datasources", CARBON_DS_CONFIG_FILENAME);
        return copyFile(carbonYmlFilePath, Paths.get("conf", "datasources", CARBON_DS_CONFIG_FILENAME));
    }

    @Configuration
    public Option[] createConfiguration() {
        RDBMSConfig.createDSFromXML();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
//            e.printStackTrace();
            //todo log
        }
        return new Option[]{
                copyCarbonYAMLOption(),
                copyDSOption(),
                CarbonDistributionOption.copyOSGiLibBundle(maven()
                        .artifactId("postgresql")
                        .groupId("postgresql")
                        .versionAsInProject()),
        };
    }

    @Test
    public void testDBSystemPersistence() throws InterruptedException {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            log.info("Getting Connection from DS");
            DataSource dataSource = null;
            dataSource = (HikariDataSource) dataSourceService.getDataSource("WSO2_ANALYTICS_DB");
            con = dataSource.getConnection();
            Thread.sleep(2000);
            SiddhiAppRuntime siddhiAppRuntime = SiddhiAppUtil.createSiddhiApp(StreamProcessorDataHolder.getSiddhiManager());
            SiddhiAppUtil.sendDataToStream("WSO2", 500L, siddhiAppRuntime);
            SiddhiAppUtil.sendDataToStream("WSO2", 200L, siddhiAppRuntime);
            SiddhiAppUtil.sendDataToStream("WSO2", 300L, siddhiAppRuntime);
            SiddhiAppUtil.sendDataToStream("WSO2", 250L, siddhiAppRuntime);
            SiddhiAppUtil.sendDataToStream("WSO2", 150L, siddhiAppRuntime);
            log.info("Waiting for first time interval for state persistence");
            Thread.sleep(61000);
            stmt = con.prepareStatement(selectLastQuery);
            stmt.setString(1, siddhiAppRuntime.getName());
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                log.info(resultSet.getString("siddhiAppName") + " Revisions Found");
            } else {
                Assert.fail("Database should have a state persisted");
            }
        } catch (DataSourceException e) {
            log.error("Could not return data source with name ", e);
        } catch (SQLException e) {
            log.error("Cannot establish connection to the data source ", e);
        } finally {
            try { //TODO Null check
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Test(dependsOnMethods = {"testDBSystemPersistence"})
    public void testRestore() throws InterruptedException {
        SiddhiManager siddhiManager = StreamProcessorDataHolder.getSiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.getSiddhiAppRuntime(SIDDHIAPP_NAME);
        log.info("Shutting Down SiddhiApp");
        siddhiAppRuntime.shutdown();
        log.info("Creating New SiddhiApp with Same Name");
        SiddhiAppRuntime newSiddhiAppRuntime = SiddhiAppUtil.createSiddhiApp(StreamProcessorDataHolder.getSiddhiManager());
        newSiddhiAppRuntime.restoreLastRevision();

        SiddhiAppUtil.sendDataToStream("WSO2", 280L, newSiddhiAppRuntime);
        SiddhiAppUtil.sendDataToStream("WSO2", 150L, newSiddhiAppRuntime);
        SiddhiAppUtil.sendDataToStream("WSO2", 200L, newSiddhiAppRuntime);
        SiddhiAppUtil.sendDataToStream("WSO2", 270L, newSiddhiAppRuntime);
        SiddhiAppUtil.sendDataToStream("WSO2", 280L, newSiddhiAppRuntime);
    }

    @Test(dependsOnMethods = {"testRestore"})
    public void testPeriodicDBSystemPersistence() throws InterruptedException {

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            DataSource dataSource = null;
            dataSource = (HikariDataSource) dataSourceService.getDataSource("WSO2_ANALYTICS_DB");
            log.info("Waiting for second time interval for state persistence");
            con = dataSource.getConnection();
            Thread.sleep(60000);
            stmt = con.prepareStatement(selectLastQuery);
            SiddhiManager siddhiManager = StreamProcessorDataHolder.getSiddhiManager();
            stmt.setString(1, siddhiManager.getSiddhiAppRuntime(SIDDHIAPP_NAME).getName());
            ResultSet resultSet = stmt.executeQuery();
            int count = 0;
            while (resultSet.next()) {
                count++;
                Assert.assertEquals(resultSet.getString("siddhiAppName"), SIDDHIAPP_NAME);
            }
            Assert.assertEquals(count, 2);
            log.info("Waiting for third time interval for state persistence");
            Thread.sleep(60000);

            resultSet = stmt.executeQuery();
            count = 0;
            while (resultSet.next()) {
                count++;
                Assert.assertEquals(resultSet.getString("siddhiAppName"), SIDDHIAPP_NAME);
            }
            Assert.assertEquals(count, 2);

        } catch (SQLException e) {
            log.error("Cannot establish connection to the data source ", e);
        } catch (DataSourceException e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    //TODO Replace printStackTrace with log
                }
            }
        }
    }
}
