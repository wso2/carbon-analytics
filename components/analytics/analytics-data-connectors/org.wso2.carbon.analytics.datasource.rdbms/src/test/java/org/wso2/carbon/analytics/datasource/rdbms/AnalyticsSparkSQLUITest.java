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
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStoreTest;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.rs.Record;
import org.wso2.carbon.analytics.datasource.rdbms.h2.H2FileDBAnalyticsFileSystemTest;
import org.wso2.carbon.analytics.datasource.rdbms.h2.H2FileDBAnalyticsRecordStoreTest;
import org.wso2.carbon.analytics.spark.admin.dto.AnalyticsQueryResultDto;
import org.wso2.carbon.analytics.spark.admin.dto.AnalyticsRowResultDto;
import org.wso2.carbon.analytics.spark.admin.internal.AnalyticsResultConverter;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub;
import org.wso2.carbon.analytics.spark.core.AnalyticsProcessorService;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.internal.SparkAnalyticsExecutor;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsQueryResult;
import org.wso2.carbon.analytics.spark.ui.client.AnalyticsExecutionClient;

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
        this.service = new AnalyticsDataServiceImpl(ars, afs, 6);
        ServiceHolder.setAnalyticsDataService(this.service);
        SparkAnalyticsExecutor.init();
    }

    @AfterClass
    public void done() throws NamingException, AnalyticsException, IOException {
        SparkAnalyticsExecutor.stop();
        this.service.destroy();
        this.h2arstest.destroy();
        this.h2afstest.destroy();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testUIJsonStringGeneration() throws Exception {
        System.out.printf("***** AnalyticsSparkSQLUITest ***** \n");

        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "Log", 0, 10, -1, -1);
        this.service.deleteTable(1, "Log");
        this.service.createTable(1, "Log");
        this.service.put(records);

        AnalyticsProcessorService processorService = new AnalyticsProcessorService();
        AnalyticsExecutionClient client = new AnalyticsExecutionClient();

        String query = "define table Log (server_name STRING, "
                + "ip STRING, tenant INTEGER, sequence LONG, summary STRING)";
        AnalyticsQueryResult queryResult = processorService.executeQuery(1, query);
        AnalyticsQueryResultDto queryResultDto = AnalyticsResultConverter.convertResults(queryResult);
        AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto stubDto = getResult(queryResultDto);
        String result = client.toJsonResult(query, stubDto);
        Assert.assertEquals(result.charAt(0), '{');
        Assert.assertEquals(result.charAt(result.length() - 1), '}');
        System.out.println(result);

        query = "SELECT * FROM Log";
        queryResult = processorService.executeQuery(1, query);
        queryResultDto = AnalyticsResultConverter.convertResults(queryResult);
        stubDto = getResult(queryResultDto);
        result = client.toJsonResult(query, stubDto);
        System.out.println(result);
        Assert.assertEquals(result.charAt(0), '{');
        Assert.assertEquals(result.charAt(result.length() - 1), '}');

//        example of a failing query...
        query = "SELECT * from ABC";
        queryResult = processorService.executeQuery(1, query);
        queryResultDto = AnalyticsResultConverter.convertResults(queryResult);
        stubDto = getResult(queryResultDto);
        result = client.toJsonResult(query, stubDto);
        System.out.println(result);
        Assert.assertEquals(result.charAt(0), '{');
        Assert.assertEquals(result.charAt(result.length() - 1), '}');

        this.service.deleteTable(1, "Log");
        SparkAnalyticsExecutor.stop();
    }

    private AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto getResult(AnalyticsQueryResultDto queryResultDto) {
        AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto stubDto = new AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto();
        stubDto.setColumnNames(queryResultDto.getColumnNames());
        stubDto.setQuery(queryResultDto.getQuery());
        stubDto.setRowsResults(getRowStubDtoResult(queryResultDto.getRowsResults()));
        return stubDto;
    }

    private AnalyticsProcessorAdminServiceStub.AnalyticsRowResultDto[] getRowStubDtoResult(AnalyticsRowResultDto[] rowResultDto) {
        if (rowResultDto != null) {
            AnalyticsProcessorAdminServiceStub.AnalyticsRowResultDto[] result = new AnalyticsProcessorAdminServiceStub
                    .AnalyticsRowResultDto[rowResultDto.length];
            int dtoIndex = 0;
            for (AnalyticsRowResultDto dto : rowResultDto) {
                result[dtoIndex] = new AnalyticsProcessorAdminServiceStub.AnalyticsRowResultDto();
                result[dtoIndex].setColumnValues(dto.getColumnValues());
                dtoIndex++;
            }
            return result;
        }
        return null;
    }

}
