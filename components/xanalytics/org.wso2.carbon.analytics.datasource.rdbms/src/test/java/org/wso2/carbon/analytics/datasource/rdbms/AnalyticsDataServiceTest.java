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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataServiceImpl;
import org.wso2.carbon.analytics.dataservice.IndexType;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceTest;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.base.MultitenantConstants;

/**
 * This class represents the analytics data service tests.
 */
public class AnalyticsDataServiceTest {

    private AnalyticsDataService service;
        
    @BeforeSuite
    public void setup() throws NamingException, AnalyticsException, IOException {
        AnalyticsDataSource ads = H2FileDBAnalyticsDataSourceTest.cleanupAndCreateDS();
        this.service = new AnalyticsDataServiceImpl(ads);
    }
    
    @AfterSuite
    public void done() throws NamingException, AnalyticsException, IOException {
        this.service.destroy();
    }
    
    @Test
    public void testIndexAddRetrieve() throws AnalyticsException {
        this.indexAddRetrieve(MultitenantConstants.SUPER_TENANT_ID);
        this.indexAddRetrieve(1);
        this.indexAddRetrieve(15001);
    }
    
    private void indexAddRetrieve(int tenantId) throws AnalyticsException {
        this.service.clearIndices(tenantId, "T1");
        Map<String, IndexType> columns = new HashMap<String, IndexType>();
        columns.put("C1", IndexType.STRING);
        columns.put("C2", IndexType.TEXT);
        columns.put("C3", IndexType.INTEGER);
        columns.put("C4", IndexType.LONG);
        columns.put("C5", IndexType.DOUBLE);
        columns.put("C6", IndexType.FLOAT);
        service.setIndices(tenantId, "T1", columns);
        Map<String, IndexType> columnsIn = service.getIndices(tenantId, "T1");
        Assert.assertEquals(columnsIn, columns);
        this.service.clearIndices(tenantId, "T1");
    }
    
    private void cleanupTable(int tenantId, String tableName) throws AnalyticsException {
        if (this.service.tableExists(tenantId, tableName)) {
            this.service.deleteTable(tenantId, tableName);
        }
        this.service.clearIndices(tenantId, tableName);
    }
    
    private List<Record> generateIndexRecords(int tenantId, String tableName, int n) {
        Map<String, Object> values = new HashMap<String, Object>();
        Record record;
        List<Record> result = new ArrayList<Record>();
        for (int i = 0; i < n; i++) {
            values = new HashMap<String, Object>();
            values.put("INT1", i);
            values.put("STR1", "STRING" + i);
            values.put("str2", "string" + i);
            values.put("TXT1", "My name is bill" + i);
            values.put("LN1", 1435000L + i);
            values.put("DB1", 54.535 + i);
            values.put("FL1", 3.14 + i);
            values.put("BL1", i % 2 == 0 ? true : false);
            record = new Record(tenantId, tableName, values, System.currentTimeMillis());
            result.add(record);
        }        
        return result;
    }
    
    private void indexDataAddRetrieve(int tenantId, String tableName, int n) throws AnalyticsException {
        this.cleanupTable(tenantId, tableName);
        Map<String, IndexType> columns = new HashMap<String, IndexType>();
        columns.put("INT1", IndexType.INTEGER);
        columns.put("STR1", IndexType.STRING);
        columns.put("str2", IndexType.STRING);
        columns.put("TXT1", IndexType.TEXT);
        columns.put("LN1", IndexType.LONG);
        columns.put("DB1", IndexType.DOUBLE);
        columns.put("FL1", IndexType.FLOAT);
        columns.put("BL1", IndexType.BOOLEAN);
        this.service.createTable(tenantId, tableName);
        this.service.setIndices(tenantId, tableName, columns);
        List<Record> records = this.generateIndexRecords(tenantId, tableName, n);
        this.service.put(records);
        List<String> result = this.service.search(tenantId, tableName, "lucene", "STR1:STRING0", 0, 10);
        Assert.assertEquals(result.size(), 1);
        result = this.service.search(tenantId, tableName, "lucene", "str2:string0", 0, 10);
        Assert.assertEquals(result.size(), 1);
        result = this.service.search(tenantId, tableName, "lucene", "str2:String0", 0, 10);
        Assert.assertEquals(result.size(), 1);
        result = this.service.search(tenantId, tableName, "lucene", "TXT1:name", 0, n + 10);
        Assert.assertEquals(result.size(), n);
        this.cleanupTable(tenantId, tableName);
    }
    
    @Test
    public void testIndexedDataAddRetrieve() throws AnalyticsException {
        this.indexDataAddRetrieve(5, "TX", 1);
        this.indexDataAddRetrieve(5, "TX", 10);
        this.indexDataAddRetrieve(6, "TX", 150);
        this.indexDataAddRetrieve(7, "TX", 2500);
    }
    
    @Test
    public void testDataRecordAddReadPerformanceNonIndex() throws AnalyticsException {
        this.cleanupTable(50, "TableX");
        System.out.println("\n************** START ANALYTICS DS (WITHOUT INDEXING, H2-FILE) PERF TEST **************");
        int n = 100, batch = 200;
        List<Record> records;
        
        /* warm-up */
        this.service.createTable(50, "TableX");      
        for (int i = 0; i < 10; i++) {
            records = AnalyticsDataSourceTest.generateRecords(50, "TableX", i, batch, -1, -1);
            this.service.put(records);
        }
        this.cleanupTable(50, "TableX");
        
        this.service.createTable(50, "TableX");
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            records = AnalyticsDataSourceTest.generateRecords(50, "TableX", i, batch, -1, -1);
            this.service.put(records);
        }
        long end = System.currentTimeMillis();
        System.out.println("* Records: " + (n * batch));
        System.out.println("* Write Time: " + (end - start) + " ms.");
        System.out.println("* Write Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        Set<Record> recordsIn = AnalyticsDataSourceTest.recordGroupsToSet(this.service.get(50, "TableX", null, -1, -1, 0, -1));
        Assert.assertEquals(recordsIn.size(), (n * batch));
        end = System.currentTimeMillis();
        System.out.println("* Read Time: " + (end - start) + " ms.");
        System.out.println("* Read Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        this.cleanupTable(50, "TableX");
        System.out.println("\n************** END ANALYTICS DS (WITHOUT INDEXING, H2-FILE) PERF TEST **************");
    }
    
    @Test
    public void testDataRecordAddReadPerformanceIndex() throws AnalyticsException {
        this.cleanupTable(50, "TableX");
        
        System.out.println("\n************** START ANALYTICS DS (WITH INDEXING, H2-FILE) PERF TEST **************");
        int n = 100, batch = 200;
        List<Record> records;
        Map<String, IndexType> columns = new HashMap<String, IndexType>();
        columns.put("tenant", IndexType.INTEGER);
        columns.put("ip", IndexType.STRING);
        columns.put("log", IndexType.TEXT);
        
        /* warm-up */
        this.service.createTable(50, "TableX");
        this.service.setIndices(50, "TableX", columns);
        for (int i = 0; i < 10; i++) {
            records = AnalyticsDataSourceTest.generateRecords(50, "TableX", i, batch, -1, -1);
            this.service.put(records);
        }
        this.cleanupTable(50, "TableX");
        
        this.service.createTable(50, "TableX");
        this.service.setIndices(50, "TableX", columns);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            records = AnalyticsDataSourceTest.generateRecords(50, "TableX", i, batch, -1, -1);
            this.service.put(records);
        }
        long end = System.currentTimeMillis();
        System.out.println("* Records: " + (n * batch));
        System.out.println("* Write Time: " + (end - start) + " ms.");
        System.out.println("* Write Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        Set<Record> recordsIn = AnalyticsDataSourceTest.recordGroupsToSet(this.service.get(50, "TableX", null, -1, -1, 0, -1));
        Assert.assertEquals(recordsIn.size(), (n * batch));
        end = System.currentTimeMillis();
        System.out.println("* Read Time: " + (end - start) + " ms.");
        System.out.println("* Read Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        
        List<String> ids = this.service.search(50, "TableX", "lucene", "log: exception", 0, 1);
        Assert.assertEquals(ids.size(), 1);
        System.out.println("* Search Result Count: " + ids.size());
        
        this.cleanupTable(50, "TableX");
        System.out.println("\n************** END ANALYTICS DS (WITH INDEXING, H2-FILE) PERF TEST **************");
    }

}
