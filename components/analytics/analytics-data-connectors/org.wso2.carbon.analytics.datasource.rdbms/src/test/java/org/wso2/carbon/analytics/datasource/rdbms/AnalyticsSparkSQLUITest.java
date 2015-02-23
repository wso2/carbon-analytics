/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.datasource.rdbms;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataServiceImpl;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManagerImpl;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStoreTest;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.rdbms.h2.H2FileDBAnalyticsFileSystemTest;
import org.wso2.carbon.analytics.datasource.rdbms.h2.H2FileDBAnalyticsRecordStoreTest;
import org.wso2.carbon.analytics.spark.core.AnalyticsExecutionContext;
import org.wso2.carbon.analytics.spark.core.AnalyticsSparkServiceHolder;
import org.wso2.carbon.analytics.spark.ui.client.SparkExecutionClient;

import javax.naming.NamingException;

import java.io.IOException;
import java.util.List;

/**
 * This class represents tests related to Spark SQL based analytics.
 */
public class AnalyticsSparkSQLUITest {

    private AnalyticsDataService service;
    
    private H2FileDBAnalyticsRecordStoreTest h2arstest;
    
    private H2FileDBAnalyticsFileSystemTest h2afstest;

    @BeforeClass
    public void setup() throws NamingException, AnalyticsException, IOException {
        this.h2arstest = new H2FileDBAnalyticsRecordStoreTest();
        this.h2afstest = new H2FileDBAnalyticsFileSystemTest();
        this.h2arstest.setup();
        this.h2afstest.setup();
        AnalyticsRecordStore ars = this.h2arstest.getARS();
        AnalyticsFileSystem afs = this.h2afstest.getAFS();
        AnalyticsServiceHolder.setHazelcastInstance(null);
        AnalyticsServiceHolder.setAnalyticsClusterManager(new AnalyticsClusterManagerImpl());
        this.service = new AnalyticsDataServiceImpl(ars, afs, 5);
    }

    @AfterClass
    public void done() throws NamingException, AnalyticsException, IOException {
        this.service.destroy();
        this.h2arstest.destroy();
        this.h2afstest.destroy();
    }

    @Test
    public void testUIJsonStringGeneration() throws AnalyticsException {
        System.out.printf("***** AnalyticsSparkSQLUITest ***** ");
        AnalyticsSparkServiceHolder.setAnalyticsDataService(this.service);
        AnalyticsExecutionContext.init();

        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "Log", 0, 10, -1, -1);
        this.service.deleteTable(1, "Log");
        this.service.createTable(1, "Log");
        this.service.insert(records);

        SparkExecutionClient client = new SparkExecutionClient();
        String result = client.execute(1, "define table Log server_name STRING, "
                                          + "ip STRING, tenant INTEGER, sequence LONG, summary STRING");
        Assert.assertEquals(result.charAt(0), '{');
        Assert.assertEquals(result.charAt(result.length()-1), '}');
        System.out.println(result);

        result = client.execute(1, "SELECT * FROM Log");
        System.out.println(result);
        Assert.assertEquals(result.charAt(0), '{');
        Assert.assertEquals(result.charAt(result.length()-1), '}');

//        example of a failing query...
//        result = client.execute(1, "SELECT * from ABC");
//        System.out.println(result);
//        Assert.assertEquals(result.charAt(0), '{');
//        Assert.assertEquals(result.charAt(result.length()-1), '}');

        this.service.deleteTable(1, "Log");
        AnalyticsExecutionContext.stop();
    }

}
