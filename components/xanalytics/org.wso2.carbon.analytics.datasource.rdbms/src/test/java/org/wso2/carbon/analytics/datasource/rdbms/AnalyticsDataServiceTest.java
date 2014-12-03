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
import java.util.HashSet;
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
        Set<String> columns = new HashSet<String>();
        columns.add("C1");
        columns.add("C2");
        columns.add("C3");
        service.setIndices(tenantId, "T1", columns);
        Set<String> columnsIn = service.getIndices(tenantId, "T1");
        Assert.assertEquals(columnsIn, columns);
        this.service.clearIndices(tenantId, "T1");
    }
    
    private void cleanupTable(int tenantId, String tableName) throws AnalyticsException {
        if (this.service.tableExists(tenantId, tableName)) {
            this.service.deleteTable(tenantId, tableName);
        }
        this.service.clearIndices(tenantId, tableName);
    }
    
    @Test
    public void testIndexedDataAddRetrieve() throws AnalyticsException {
        this.service.clearIndices(1, "TX");
        this.service.deleteTable(1, "T1");
        this.service.createTable(1, "TX");
        Set<String> columns = new HashSet<String>();
        columns.add("C1");
        columns.add("T1");
        this.service.setIndices(1, "TX", columns);
        List<Record> records = new ArrayList<Record>();
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("C1", "My name is jack");
        values.put("T1", 28);
        Record record = new Record(1, "TX", values, System.currentTimeMillis());
        records.add(record);
        this.service.put(records);
        this.service.clearIndices(1, "TX");
        this.service.deleteTable(1, "T1");
    }
    
    @Test
    public void testDataRecordAddReadPerformanceNonIndex() throws AnalyticsException {
        this.cleanupTable(50, "TableX");
        System.out.println("\n************** START ANALYTICS DS (WITHOUT INDEXING, H2-FILE) PERF TEST **************");
        int n = 55, batch = 1000;
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
        int n = 50, batch = 1000;
        List<Record> records;
        Set<String> columns = new HashSet<String>();
        columns.add("tenant");
        columns.add("ip");
        columns.add("log");
        
        /* warm-up */
        this.service.createTable(50, "TableX");
        this.service.setIndices(50, "TableX", columns);
        for (int i = 0; i < 1; i++) {
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
        
        List<String> ids = this.service.search(50, "TableX", "lucene", "log: exception", 0, 30);
        Assert.assertEquals(ids.size(), 30);
        System.out.println("* Search Result Count: " + ids.size());
        
        this.cleanupTable(50, "TableX");
        System.out.println("\n************** END ANALYTICS DS (WITH INDEXING, H2-FILE) PERF TEST **************");
    }

}
