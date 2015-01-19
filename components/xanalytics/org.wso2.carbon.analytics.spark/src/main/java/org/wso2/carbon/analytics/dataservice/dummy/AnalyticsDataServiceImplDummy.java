/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.dataservice.dummy;

import org.wso2.carbon.analytics.dataservice.indexing.AnalyticsDataIndexer;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.RecordGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by niranda on 1/7/15.
 */
public class AnalyticsDataServiceImplDummy implements Serializable {

    private AnalyticsDataSource analyticsDataSource;

    private AnalyticsDataIndexer indexer;

//    public AnalyticsDataServiceImplDummy(AnalyticsDataSource analyticsDataSource) throws AnalyticsException {
//        this.analyticsDataSource = analyticsDataSource;
//        FileSystem fileSystem;
//        try {
//            fileSystem = this.analyticsDataSource.getFileSystem();
//        } catch (IOException e) {
//            throw new AnalyticsException("Error in creating file system: " + e.getMessage(), e);
//        }
//        this.indexer = new AnalyticsDataIndexer(fileSystem);
//    }
//
//    public AnalyticsDataServiceImplDummy(AnalyticsDataServiceConfiguration config) {
//    }


    public AnalyticsDataIndexer getIndexer() {
        return indexer;
    }

    public AnalyticsDataSource getAnalyticsDataSource() {
        return analyticsDataSource;
    }


    public void createTable(int tenantId, String tableName) throws AnalyticsException {
    }


    public boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        return true;
    }


    public void deleteTable(int tenantId, String tableName) throws AnalyticsException {
    }


    public List<String> listTables(int tenantId) throws AnalyticsException {
        List<String> tableList = new ArrayList<String>();
        tableList.add("table1");
        tableList.add("table2");
        tableList.add("table3");

        return tableList;
    }


    public long getRecordCount(int tenantId, String tableName) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
        return (long) 1001;
    }


    public void insert(List<Record> records)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
    }


    public void update(List<Record> records)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
    }


    public RecordGroup[] get(int tenantId, String tableName, List<String> columns, long timeFrom,
                             long timeTo,
                             int recordsFrom, int recordsCount)
            throws AnalyticsException, AnalyticsTableNotAvailableException {

        return createRecordGroups();
    }


    public RecordGroup[] get()
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        return createRecordGroups();
    }


    public void delete(int tenantId, String tableName, long timeFrom, long timeTo)
            throws AnalyticsException,
            AnalyticsTableNotAvailableException {
//        this.getIndexer().delete(tenantId, tableName, timeFrom, timeTo);
//        this.getAnalyticsDataSource().delete(tenantId, tableName, timeFrom, timeTo);
    }


    public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
//        this.getIndexer().delete(tenantId, tableName, ids);
//        this.getAnalyticsDataSource().delete(tenantId, tableName, ids);
    }


//    public void setIndices(int tenantId, String tableName, Map<String, IndexType> columns)
//            throws AnalyticsIndexException {
////        this.getIndexer().setIndices(tenantId, tableName, columns);
//    }
//
//
//    public List<SearchResultEntry> search(int tenantId, String tableName, String language,
//                                          String query,
//                                          int start, int count) throws AnalyticsIndexException {
//        return this.getIndexer().search(tenantId, tableName, language, query, start, count);
//    }
//
//
//    public Map<String, IndexType> getIndices(int tenantId, String tableName)
//            throws AnalyticsIndexException {
//        return this.getIndexer().lookupIndices(tenantId, tableName);
//    }
//
//
//    public void clearIndices(int tenantId, String tableName) throws AnalyticsIndexException {
////        this.getIndexer().clearIndices(tenantId, tableName);
//    }


    public void destroy() throws AnalyticsException {
//        this.indexer.close();
    }

    private RecordGroup[] createRecordGroups() {
//        ArrayList<RecordGroup> recordGroupsArr = new ArrayList<RecordGroup>();
        RecordGroup[] recordGroups = new RecordGroup[3];

        for (int i = 0; i < 3; i++) {
//            recordGroupsArr.add(new DummyRecordGroup(Integer.toString(i)));
            recordGroups[i] = new DummyRecordGroup(Integer.toString(i));
        }

//        return (RecordGroup[]) recordGroupsArr.toArray();
        return recordGroups;
    }


    public List<Record> createRecordList() {

        Map<String, Object> values1 = new HashMap<String, Object>();
        values1.put("first_name", "jane");
        values1.put("last_name", "patrick");
        values1.put("age", 10);

        Map<String, Object> values2 = new HashMap<String, Object>();
        values2.put("first_name", "ann");
        values2.put("last_name", "bishop");
        values2.put("age", 20);


        Map<String, Object> values3 = new HashMap<String, Object>();
        values3.put("first_name", "sam");
        values3.put("last_name", "winny");
        values3.put("age", 30);


        List<Record> records = new ArrayList<Record>();
        records.add(new Record(1, "table1", values1, (long) 10000));
        records.add(new Record(2, "table1", values2, (long) 20000));
        records.add(new Record(3, "table1", values3, (long) 30000));

        return records;
    }

    private class DummyRecordGroup implements RecordGroup {

        private static final String LOCALHOST = "127.0.0.1";

        private List<Record> records;

        public DummyRecordGroup(String id) {
            this.records = createRecordList();
        }

        @Override
        public String[] getLocations() throws AnalyticsException {
            return new String[]{LOCALHOST};
        }

        @Override
        public List<Record> getRecords() throws AnalyticsException {
            return records;
        }

    }
}
