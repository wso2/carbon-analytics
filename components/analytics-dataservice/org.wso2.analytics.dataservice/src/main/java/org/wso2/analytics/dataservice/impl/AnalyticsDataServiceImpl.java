/*
 *  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.analytics.dataservice.impl;

import org.wso2.analytics.data.commons.sources.AnalyticsIterator;
import org.wso2.analytics.data.commons.sources.AnalyticsRecordStoreConstants;
import org.wso2.analytics.data.commons.sources.Record;
import org.wso2.analytics.data.commons.sources.RecordGroup;
import org.wso2.analytics.data.commons.utils.AnalyticsCommonUtils;
import org.wso2.analytics.dataservice.AnalyticsDataService;
import org.wso2.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.analytics.dataservice.commons.AnalyticsSchema;
import org.wso2.analytics.dataservice.utils.AnalyticsDataServiceConstants;
import org.wso2.analytics.data.commons.AnalyticsRecordStore;
import org.wso2.analytics.dataservice.commons.AnalyticsDataResponse.Entry;
import org.wso2.analytics.data.commons.exception.AnalyticsException;
import org.wso2.analytics.data.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.analytics.dataservice.config.AnalyticsDataServiceConfigProperty;
import org.wso2.analytics.dataservice.config.AnalyticsDataServiceConfiguration;
import org.wso2.analytics.dataservice.config.AnalyticsRecordStoreConfiguration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsDataServiceImpl implements AnalyticsDataService {

    private static final int DELETE_BATCH_SIZE = 1000;
    private static final String ANALYTICS_META_TABLE = "ANALYTICS_META_TABLE";
    private static final String TABLE_INFO_DATA_COLUMN = "TABLE_INFO_DATA";

    private int recordsBatchSize;
    private String primaryARSName;
    private Map<String, AnalyticsRecordStore> analyticsRecordStores;
    private Map<String, AnalyticsTableInfo> tableInfoMap = new HashMap<>();

    public AnalyticsDataServiceImpl() throws AnalyticsException {
        AnalyticsDataServiceConfiguration config = this.loadAnalyticsDataServiceConfig();
        this.initARS(config);
        //todo: add clustering info here
//        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
//        if (acm.isClusteringEnabled()) {
//            acm.joinGroup(ANALYTICS_DATASERVICE_GROUP, null);
//        }
    }

    private AnalyticsDataServiceConfiguration loadAnalyticsDataServiceConfig() throws AnalyticsException {
        try {
            File confFile = new File(AnalyticsCommonUtils.getAnalyticsConfDirectory() + File.separator +
                    AnalyticsDataServiceConstants.ANALYTICS_DS_CONFIG_DIR + File.separator +
                    AnalyticsDataServiceConstants.ANALYTICS_DS_CONFIG_FILE);
            if (!confFile.exists()) {
                throw new AnalyticsException("Cannot initalize analytics data service, " +
                        "the analytics data service configuration file cannot be found at: " +
                        confFile.getPath());
            }
            JAXBContext ctx = JAXBContext.newInstance(AnalyticsDataServiceConfiguration.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            return (AnalyticsDataServiceConfiguration) unmarshaller.unmarshal(confFile);
        } catch (JAXBException e) {
            throw new AnalyticsException(
                    "Error in processing analytics data service configuration: " + e.getMessage(), e);
        }
    }

    private void initARS(AnalyticsDataServiceConfiguration config) throws AnalyticsException {
        this.primaryARSName = config.getPrimaryRecordStore().trim();
        if (this.primaryARSName.length() == 0) {
            throw new AnalyticsException("Primary record store name cannot be empty!");
        }
        this.analyticsRecordStores = new HashMap<>();
        for (AnalyticsRecordStoreConfiguration arsConfig : config.getAnalyticsRecordStoreConfigurations()) {
            String name = arsConfig.getName().trim();
            String arsClass = arsConfig.getImplementation();
            AnalyticsRecordStore ars;
            try {
                ars = (AnalyticsRecordStore) Class.forName(arsClass).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new AnalyticsException("Error in creating analytics record store with name '" + name +
                        "': " + e.getMessage(), e);
            }
            ars.init(this.convertToMap(arsConfig.getProperties()));
            this.analyticsRecordStores.put(name, ars);
        }
        if (!this.analyticsRecordStores.containsKey(this.primaryARSName)) {
            throw new AnalyticsException("The primary record store with name '" + this.primaryARSName + "' cannot be found.");
        }
        this.recordsBatchSize = config.getRecordsBatchSize();
    }

    private Map<String, String> convertToMap(AnalyticsDataServiceConfigProperty[] props) {
        Map<String, String> result = new HashMap<>();
        for (AnalyticsDataServiceConfigProperty prop : props) {
            result.put(prop.getName(), prop.getValue());
        }
        return result;
    }

    private AnalyticsRecordStore getAnalyticsRecordStore(String name) throws AnalyticsException {
        AnalyticsRecordStore ars = this.analyticsRecordStores.get(name);
        if (ars == null) {
            throw new AnalyticsException("Analytics record store with the name '" + name + "' cannot be found.");
        }
        return ars;
    }

    @Override
    public List<String> listRecordStoreNames() {
        List<String> result = new ArrayList<>(this.analyticsRecordStores.keySet());
        // add the primary record store name as the first one
        result.remove(this.primaryARSName);
        result.add(0, this.primaryARSName);
        return result;
    }

    @Override
    public void createTable(String recordStoreName, String tableName) throws AnalyticsException {
        tableName = AnalyticsCommonUtils.normalizeTableName(tableName);
        recordStoreName = recordStoreName.trim();
        this.getAnalyticsRecordStore(recordStoreName).createTable(tableName);
        AnalyticsTableInfo tableInfo = null;
        try {
            tableInfo = this.lookupTableInfo(tableName);
        } catch (AnalyticsTableNotAvailableException ignore) {
            /* ignore */
        }
        if (tableInfo == null || !tableInfo.getRecordStoreName().equals(recordStoreName)) {
            tableInfo = new AnalyticsTableInfo(tableName, recordStoreName, new AnalyticsSchema());
        }
        this.writeTableInfo(tableName, tableInfo);
        this.invalidateAnalyticsTableInfo(tableName);
    }

    private AnalyticsTableInfo lookupTableInfo(String tableName) throws AnalyticsException {
        AnalyticsTableInfo tableInfo = this.tableInfoMap.get(tableName);
        if (tableInfo == null) {
            tableInfo = this.readTableInfo(tableName);
            this.tableInfoMap.put(tableName, tableInfo);
        }
        return tableInfo;
    }

    private void writeTableInfo(String tableName, AnalyticsTableInfo tableInfo) throws AnalyticsException {
        AnalyticsRecordStore ars = this.getPrimaryAnalyticsRecordStore();
        Map<String, Object> values = new HashMap<>(1);
        values.put(TABLE_INFO_DATA_COLUMN, AnalyticsCommonUtils.serializeObject(tableInfo));
        Record record = new Record(tableName, ANALYTICS_META_TABLE, values);
        List<Record> records = new ArrayList<>(1);
        records.add(record);
        try {
            ars.put(records);
        } catch (AnalyticsTableNotAvailableException e) {
            ars.createTable(ANALYTICS_META_TABLE);
            ars.put(records);
        }
    }

    public void invalidateAnalyticsTableInfo(String tableName) {
        tableName = AnalyticsCommonUtils.normalizeTableName(tableName);
        this.tableInfoMap.remove(tableName);
    }
    private AnalyticsTableInfo readTableInfo(String tableName) throws AnalyticsException {
        AnalyticsRecordStore ars = this.getPrimaryAnalyticsRecordStore();
        List<String> ids = new ArrayList<>();
        ids.add(tableName);
        List<Record> records;
        try {
            records = AnalyticsCommonUtils.listRecords(ars, ars.get(ANALYTICS_META_TABLE, 1, null, ids));
        } catch (AnalyticsTableNotAvailableException e) {
            throw new AnalyticsTableNotAvailableException(tableName);
        }
        if (records.size() == 0) {
            throw new AnalyticsTableNotAvailableException(tableName);
        } else {
            byte[] data = (byte[]) records.get(0).getValue(TABLE_INFO_DATA_COLUMN);
            if (data == null) {
                throw new AnalyticsException("Corrupted table info for table: " + tableName);
            }
            return (AnalyticsTableInfo) AnalyticsCommonUtils.deserializeObject(data);
        }
    }


    @Override
    public void createTableIfNotExists(int tenantId, String recordStoreName, String tableName) throws AnalyticsException {
        if (!this.tableExists(tableName)) {
            this.createTable(recordStoreName, tableName);
        }
    }

    private AnalyticsRecordStore getPrimaryAnalyticsRecordStore() {
        return this.analyticsRecordStores.get(this.primaryARSName);
    }

    @Override
    public void createTable(String tableName) throws AnalyticsException {
        this.createTable(this.primaryARSName, tableName);
    }

    @Override
    public String getRecordStoreNameByTable(String tableName) throws AnalyticsException {
        tableName = AnalyticsCommonUtils.normalizeTableName(tableName);
        return this.lookupTableInfo(tableName).getRecordStoreName();
    }

    @Override
    public void setTableSchema(String tableName, AnalyticsSchema schema) throws AnalyticsException {
        tableName = AnalyticsCommonUtils.normalizeTableName(tableName);
        AnalyticsTableInfo tableInfo = this.lookupTableInfo(tableName);
        tableInfo.setSchema(schema);
        this.writeTableInfo(tableName, tableInfo);
        this.checkAndInvalidateTableInfo(tableName);
    }

    @Override
    public AnalyticsSchema getTableSchema(String tableName) throws AnalyticsException {
        tableName = AnalyticsCommonUtils.normalizeTableName(tableName);
        return this.lookupTableInfo(tableName).getSchema();
    }

    @Override
    public boolean tableExists(String tableName) throws AnalyticsException {
        try {
            tableName = AnalyticsCommonUtils.normalizeTableName(tableName);
            return this.getRecordStoreNameByTable(tableName) != null;
        } catch (AnalyticsTableNotAvailableException e) {
            return false;
        }
    }

    @Override
    public void deleteTable(String tableName) throws AnalyticsException {
        tableName = AnalyticsCommonUtils.normalizeTableName(tableName);
        String arsName;
        try {
            arsName = this.getRecordStoreNameByTable(tableName);
        } catch (AnalyticsTableNotAvailableException e) {
            return;
        }
        if (arsName == null) {
            return;
        }
        this.deleteTableInfo(tableName);
        this.checkAndInvalidateTableInfo(tableName);
        this.getAnalyticsRecordStore(arsName).deleteTable(tableName);
    }

    private void deleteTableInfo(String tableName) throws AnalyticsException {
        List<String> ids = new ArrayList<>(1);
        ids.add(tableName);
        try {
            this.getPrimaryAnalyticsRecordStore().delete(ANALYTICS_META_TABLE, ids);
        } catch (AnalyticsTableNotAvailableException ignore) {
            /* ignore */
        }
    }

    private void checkAndInvalidateTableInfo(String tableName) throws AnalyticsException {
        //todo: add clustered table removal here
        /*AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        if (acm.isClusteringEnabled()) {
            *//* send cluster message to invalidate *//*
            acm.executeAll(ANALYTICS_DATASERVICE_GROUP, new AnalyticsTableInfoChangeMessage(tenantId, tableName));
        } else {
        }*/
        this.invalidateAnalyticsTableInfo(tableName);
    }

    @Override
    public List<String> listTables() throws AnalyticsException {
        try {
            List<Record> records = AnalyticsCommonUtils.listRecords(this.getPrimaryAnalyticsRecordStore(),
                    this.getPrimaryAnalyticsRecordStore().get(ANALYTICS_META_TABLE, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
            return records.stream().map(Record::getId).collect(Collectors.toList());
        } catch (AnalyticsTableNotAvailableException e) {
            return new ArrayList<>(0);
        }
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException {
        Collection<List<Record>> recordBatches = AnalyticsCommonUtils.generateRecordBatches(records, true);
        this.preprocessRecords(recordBatches);
        for (List<Record> recordsBatch : recordBatches) {
            this.putSimilarRecordBatch(recordsBatch);
        }
    }

    private void putSimilarRecordBatch(List<Record> recordsBatch) throws AnalyticsException {
        Record firstRecord = recordsBatch.get(0);
        String tableName = firstRecord.getTableName();
        String arsName = this.getRecordStoreNameByTable(tableName);
        this.getAnalyticsRecordStore(arsName).put(recordsBatch);
    }

    /**
     * This method preprocesses the records before adding to the record store,
     * e.g. update the record ids if its not already set by using the table
     * schema's primary keys.
     *
     * @param recordBatches batch of records
     */
    private void preprocessRecords(Collection<List<Record>> recordBatches) throws AnalyticsException {
        for (List<Record> recordBatch : recordBatches) {
            this.preprocessRecordBatch(recordBatch);
        }
    }

    private void preprocessRecordBatch(List<Record> recordBatch) throws AnalyticsException {
        Record firstRecord = recordBatch.get(0);
        AnalyticsSchema schema = this.lookupTableInfo(firstRecord.getTableName()).getSchema();
        List<String> primaryKeys = schema.getPrimaryKeys();
        if (primaryKeys != null && primaryKeys.size() > 0) {
            this.populateRecordsWithPrimaryKeyAwareIds(recordBatch, primaryKeys);
        } else {
            this.populateWithGenerateIds(recordBatch);
        }
    }

    private void populateWithGenerateIds(List<Record> records) {
        for (Record record : records) {
            if (record.getId() == null) {
                record.setId(AnalyticsCommonUtils.generateRecordID());
            }
        }
    }

    private void populateRecordWithPrimaryKeyAwareId(Record record, List<String> primaryKeys) {
        record.setId(this.generateRecordIdFromPrimaryKeyValues(record.getValues(), primaryKeys));
    }

    private void populateRecordsWithPrimaryKeyAwareIds(List<Record> records, List<String> primaryKeys) {
        /* users have the ability to explicitly provide a record id,
         * in-spite of having primary keys defined to auto generate the id */
        records.stream().filter(record -> record.getId() == null).forEach(record -> this.populateRecordWithPrimaryKeyAwareId(record, primaryKeys));
    }

    @Override
    public AnalyticsDataResponse get(String tableName, int numPartitionsHint, List<String> columns, long timeFrom, long timeTo, int recordsFrom, int recordsCount) throws AnalyticsException {
        tableName = AnalyticsCommonUtils.normalizeTableName(tableName);
        String arsName = this.getRecordStoreNameByTable(tableName);
        if (arsName == null) {
            throw new AnalyticsTableNotAvailableException(tableName);
        }
        RecordGroup[] rgs;
        if (this.isTimestampRangePartitionsCompatible(numPartitionsHint, timeFrom, timeTo, recordsFrom, recordsCount)) {
            List<RecordGroup> rgList = new ArrayList<>(numPartitionsHint);
            List<Long[]> tsRanges = this.splitTimestampRangeForPartitions(timeFrom, timeTo, numPartitionsHint);
            for (Long[] tsRange : tsRanges) {
                rgList.addAll(Arrays.asList(this.getAnalyticsRecordStore(arsName).get(tableName,
                        1, columns, tsRange[0], tsRange[1], recordsFrom, recordsCount)));
            }
            rgs = rgList.toArray(new RecordGroup[0]);
        } else {
            rgs = this.getAnalyticsRecordStore(arsName).get(tableName, numPartitionsHint, columns, timeFrom,
                    timeTo, recordsFrom, recordsCount);
        }
        return new AnalyticsDataResponse(this.createResponseEntriesFromSingleRecordStore(arsName, rgs));
    }

    private List<Long[]> splitTimestampRangeForPartitions(long timeFrom, long timeTo, int numPartitionsHint) {
        List<Long[]> result = new ArrayList<>();
        int delta = (int) Math.ceil((timeTo - timeFrom) / (double) numPartitionsHint);
        long val = timeFrom;
        while (true) {
            if (val + delta >= timeTo) {
                result.add(new Long[]{val, timeTo});
                break;
            } else {
                result.add(new Long[]{val, val + delta});
                val += delta;
            }
        }
        return result;
    }

    private List<Entry> createResponseEntriesFromSingleRecordStore(String arsName, RecordGroup[] rgs) {
        List<Entry> entries = new ArrayList<>(rgs.length);
        for (RecordGroup rg : rgs) {
            entries.add(new Entry(arsName, rg));
        }
        return entries;
    }

    private boolean isTimestampRangePartitionsCompatible(int numPartitionsHint, long timeFrom, long timeTo,
                                                         int recordsFrom, int recordsCount) {
        return numPartitionsHint > 1 && timeFrom != Long.MIN_VALUE && timeTo != Long.MAX_VALUE && recordsFrom == 0 &&
                (recordsCount == -1 || recordsCount == Integer.MAX_VALUE);
    }

    @Override
    public AnalyticsDataResponse get(String tableName, int numPartitionsHint, List<String> columns, List<String> ids) throws AnalyticsException {
        tableName = AnalyticsCommonUtils.normalizeTableName(tableName);
        String arsName = this.getRecordStoreNameByTable(tableName);
        if (arsName == null) {
            throw new AnalyticsTableNotAvailableException(tableName);
        }
        List<List<String>> idsSubLists = this.getChoppedLists(ids, this.recordsBatchSize);
        List<RecordGroup> recordGroups = new ArrayList<>();
        for (List<String> idSubList : idsSubLists) {
            ArrayList<RecordGroup> recordGroupSubList = new ArrayList<>(Arrays.asList(
                    this.getAnalyticsRecordStore(arsName).get(tableName, numPartitionsHint, columns, idSubList)));
            recordGroups.addAll(recordGroupSubList);
        }
        RecordGroup[] rgs = new RecordGroup[recordGroups.size()];
        rgs = recordGroups.toArray(rgs);
        return new AnalyticsDataResponse(this.createResponseEntriesFromSingleRecordStore(arsName, rgs));
    }

    private <T> List<List<T>> getChoppedLists(List<T> list, final int L) {
        List<List<T>> parts = new ArrayList<>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<>(list.subList(i, Math.min(N, i + L))));
        }
        return parts;
    }

    @Override
    public AnalyticsDataResponse getWithKeyValues(String tableName, int numPartitionsHint, List<String> columns, List<Map<String, Object>> valuesBatch) throws AnalyticsException {
        tableName = AnalyticsCommonUtils.normalizeTableName(tableName);
        List<String> ids = new ArrayList<>();
        AnalyticsSchema schema = this.lookupTableInfo(tableName).getSchema();
        List<String> primaryKeys = schema.getPrimaryKeys();
        if (primaryKeys != null && primaryKeys.size() > 0) {
            ids.addAll(valuesBatch.stream().map(values -> this.generateRecordIdFromPrimaryKeyValues(values, primaryKeys)).collect(Collectors.toList()));
        }
        return this.get(tableName, numPartitionsHint, columns, ids);
    }

    /* The users should ensure that the order of the primary key list is independent of the order.
     * check DAS-289.
     */
    private String generateRecordIdFromPrimaryKeyValues(Map<String, Object> values, List<String> primaryKeys) {
        StringBuilder builder = new StringBuilder();
        Object obj;
        for (String key : primaryKeys) {
            obj = values.get(key);
            if (obj != null) {
                builder.append(obj.toString());
            }
        }
        // to make sure, we don't have an empty string
        builder.append("");
        try {
            byte[] data = builder.toString().getBytes(AnalyticsRecordStoreConstants.DEFAULT_CHARSET);
            return UUID.nameUUIDFromBytes(data).toString();
        } catch (UnsupportedEncodingException e) {
            // This wouldn't happen
            throw new RuntimeException(e);
        }
    }

    @Override
    public AnalyticsIterator<Record> readRecords(String recordStoreName, RecordGroup recordGroup) throws AnalyticsException {
        return this.getAnalyticsRecordStore(recordStoreName).readRecords(recordGroup);
    }

    @Override
    public void delete(String tableName, long timeFrom, long timeTo) throws AnalyticsException {
        tableName = AnalyticsCommonUtils.normalizeTableName(tableName);
        while (true) {
            List<Record> recordBatch = AnalyticsCommonUtils.listRecords(this, this.get(tableName, 1, null, timeFrom, timeTo, 0, DELETE_BATCH_SIZE));
            if (recordBatch.size() == 0) {
                break;
            }
            this.delete(tableName, this.getRecordIdsBatch(recordBatch));
        }
    }

    private List<String> getRecordIdsBatch(List<Record> recordsBatch) throws AnalyticsException {
        List<String> result = new ArrayList<>(recordsBatch.size());
        result.addAll(recordsBatch.stream().map(Record::getId).collect(Collectors.toList()));
        return result;
    }

    @Override
    public void delete(String tableName, List<String> ids) throws AnalyticsException {
        tableName = AnalyticsCommonUtils.normalizeTableName(tableName);
        String arsName = this.getRecordStoreNameByTable(tableName);
        if (arsName == null) {
            throw new AnalyticsTableNotAvailableException(tableName);
        }
        /* the below ordering is important, the raw records should be deleted first */
        this.getAnalyticsRecordStore(arsName).delete(tableName, ids);
    }

    @Override
    public void destroy() throws AnalyticsException {
        for (AnalyticsRecordStore ars : this.analyticsRecordStores.values()) {
            ars.destroy();
        }
    }

    /**
     * This class represents meta information about an analytics table.
     */
    public static class AnalyticsTableInfo implements Serializable {

        private static final long serialVersionUID = -9100036429450395707L;
        private String tableName;
        private String recordStoreName;
        private AnalyticsSchema schema;

        public AnalyticsTableInfo() {
        }

        public AnalyticsTableInfo(String tableName, String recordStoreName, AnalyticsSchema schema) {
            this.tableName = tableName;
            this.recordStoreName = recordStoreName;
            this.schema = schema;
        }

        public String getTableName() {
            return tableName;
        }

        public String getRecordStoreName() {
            return recordStoreName;
        }

        public AnalyticsSchema getSchema() {
            return schema;
        }

        public void setSchema(AnalyticsSchema schema) {
            this.schema = schema;
        }

    }

    public static class MultiTableAggregateIterator implements AnalyticsIterator<Record> {

        private AnalyticsIterator<Record> currentItr;
        private List<AnalyticsIterator<Record>> iterators;

        public MultiTableAggregateIterator(List<AnalyticsIterator<Record>> iterators)
                throws AnalyticsException {
            this.iterators = iterators;
        }

        @Override
        public boolean hasNext() {
            if (iterators == null) {
                return false;
            } else {
                if (currentItr == null) {
                    if (!iterators.isEmpty()) {
                        currentItr = iterators.remove(0);
                        return this.hasNext();
                    } else {
                        return false;
                    }
                } else {
                    if (!currentItr.hasNext()) {
                        if (!iterators.isEmpty()) {
                            currentItr = iterators.remove(0);
                            return this.hasNext();
                        } else {
                            return false;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }

        @Override
        public Record next() {
            if (this.hasNext()) {
                return currentItr.next();
            } else {
                return null;
            }
        }

        @Override
        public void remove() {
            /* ignored */
        }

        @Override
        public void close() throws IOException {
            for (AnalyticsIterator<Record> iterator : iterators) {
                iterator.close();
            }
        }
    }
}
