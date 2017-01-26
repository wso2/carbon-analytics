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
package org.wso2.carbon.analytics.datasource.mongo;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.ndatasource.common.DataSourceException;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.UpdateResult;

/**
 * This class represents the Mongo implementation of
 * {@link AnalyticsRecordStore}.
 */
public class MongoAnalyticsRecordStore implements AnalyticsRecordStore {

    private String databaseName;

    private MongoDatabase db;

    private MongoClient mongo;

    private Integer writeConcernTimeout;

    @Override
    public void init(Map<String, String> properties) throws AnalyticsException {
        String dsName = MongoUtils.extractDataSourceName(properties);
        this.databaseName = MongoUtils.extractARSDatabaseName(properties);
        this.writeConcernTimeout = MongoUtils.extractWriteConcernTimeout(properties);
        try {
            MongoClient mongo = (MongoClient) GenericUtils.loadGlobalDataSource(dsName);
            if (mongo == null) {
                throw new AnalyticsException("Error establishing connection to Mongo instance: Invalid datasource configuration");
            }
            db = mongo.getDatabase(databaseName);
        } catch (IllegalArgumentException | DataSourceException e) {
            throw new AnalyticsException("Error establishing connection to Mongo instance:" + e.getMessage(), e);
        }
    }

    private String generateTargetCollectionName(int tenantId, String tableName) {
        return GenericUtils.generateTableUUID(tenantId, tableName);
    }

    @Override
    public synchronized void createTable(int tenantId, String tableName) throws AnalyticsException {
        String collectionName = this.generateTargetCollectionName(tenantId, tableName);
        boolean found = false;
        for (String colName : db.listCollectionNames()) {
            if (colName.equals(collectionName)) {
                found = true;
            }
        }
        if (!found) {
            this.db.withWriteConcern(WriteConcern.MAJORITY.withWTimeout(writeConcernTimeout, TimeUnit.MILLISECONDS))
                    .createCollection(collectionName);
            db.getCollection(collectionName)
                    .withWriteConcern(WriteConcern.MAJORITY.withWTimeout(writeConcernTimeout, TimeUnit.MILLISECONDS))
                    .createIndex(Indexes.ascending("timestamp"));
        }
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        MongoCollection<Document> collection = db.getCollection(this.generateTargetCollectionName(tenantId, tableName));
        collection.deleteMany(and(gte("timestamp", timeFrom), lt("timestamp", timeTo)));
    }

    @Override
    public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        MongoCollection<Document> collection = db.getCollection(this.generateTargetCollectionName(tenantId, tableName));
        collection.deleteMany(in("_id", ids));
    }

    @Override
    public synchronized void deleteTable(int tenantId, String tableName) throws AnalyticsException {
        String dataTable = this.generateTargetCollectionName(tenantId, tableName);
        db.getCollection(dataTable).drop();
    }

    @Override
    public void destroy() throws AnalyticsException {
        db = null;
        if (this.mongo != null) {
            this.mongo.close();
        }
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, long timeFrom, long timeTo,
            int recordsFrom, int recordsCount) throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (recordsFrom > 0) {
            throw new AnalyticsException("The Mongo connector does not support range queries with an offset: " + recordsFrom);
        }
        if (!this.tableExists(tenantId, tableName)) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        return new RecordGroup[] { new GlobalMongoRecordGroup(tenantId, tableName, columns, timeFrom, timeTo, recordsCount) };

    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, List<String> ids)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (!this.tableExists(tenantId, tableName)) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        return new RecordGroup[] { new GlobalMongoRecordGroup(tenantId, tableName, columns, ids) };
    }

    @Override
    public AnalyticsIterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsException {
        if (recordGroup instanceof GlobalMongoRecordGroup) {
            GlobalMongoRecordGroup crg = (GlobalMongoRecordGroup) recordGroup;
            if (crg.isByIds()) {
                return this.readRecordsByIds(crg);
            } else {
                return this.readRecordsByRange(crg);
            }
        } else {
            throw new AnalyticsException("Unknnown Mongo record group type: " + recordGroup.getClass());
        }
    }

    private AnalyticsIterator<Record> readRecordsByRange(GlobalMongoRecordGroup recordGroup) throws AnalyticsException {
        int tenantId = recordGroup.getTenantId();
        String tableName = recordGroup.getTableName();
        String collection = this.generateTargetCollectionName(tenantId, tableName);
        List<String> columns = recordGroup.getColumns();
        FindIterable<Document> iterableResult;
        int count = recordGroup.getCount();
        if (recordGroup.getTimeFrom() == Long.MIN_VALUE && recordGroup.getTimeTo() == Long.MAX_VALUE) {
            iterableResult = db.getCollection(collection).find().projection(fields(include("_id", "timestamp", "data")));
            if (count != -1) {
                iterableResult = iterableResult.limit(count);
            }
        } else {
            iterableResult = db.getCollection(collection).find()
                    .filter(and(gte("timestamp", recordGroup.getTimeFrom()), lt("timestamp", recordGroup.getTimeTo())));
            if (count != -1) {
                iterableResult = iterableResult.limit(count);
            }
        }
        return new MongoRecordDataIterator(iterableResult.iterator(), columns, tableName, tenantId);
    }

    private AnalyticsIterator<Record> readRecordsByIds(GlobalMongoRecordGroup recordGroup) throws AnalyticsException {
        return this.lookupRecordsByIds(recordGroup.getTenantId(), recordGroup.getTableName(), recordGroup.getIds(),
                recordGroup.getColumns());
    }

    private AnalyticsIterator<Record> lookupRecordsByIds(int tenantId, String tableName, List<String> ids, List<String> columns) {
        String collection = this.generateTargetCollectionName(tenantId, tableName);
        MongoCursor<Document> cursor = db.getCollection(collection).find(in("_id", ids)).iterator();
        return new MongoRecordDataIterator(cursor, columns, tableName, tenantId);
    }

    @Override
    public long getRecordCount(int tenantId, String tableName, long timeFrom, long timeTo)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        String collection = this.generateTargetCollectionName(tenantId, tableName);
        return db.getCollection(collection).count(and(gte("timestamp", timeFrom), lt("timestamp", timeTo)));
    }

    @Override
    public boolean isPaginationSupported() {
        return false;
    }

    @Override
    public boolean isRecordCountSupported() {
        return true;
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {
        Collection<List<Record>> batches = GenericUtils.generateRecordBatches(records);
        for (List<Record> batch : batches) {
            addBatch(batch);
        }
    }

    private void addBatch(List<Record> batch) throws AnalyticsException, AnalyticsTableNotAvailableException {
        Record firstRecord = batch.get(0);
        int tenantId = firstRecord.getTenantId();
        String tableName = firstRecord.getTableName();
        String collection = this.generateTargetCollectionName(tenantId, tableName);
        try {
            Map<String, Document> documentsToInsert = new HashMap<String, Document>();
            for (Record record : batch) {
                UpdateResult result = db.getCollection(collection).replaceOne((eq("_id", record.getId())),
                        AnalyticsRecord.toDocument(record));
                if (result.getModifiedCount() <= 0) {
                    documentsToInsert.put(record.getId(), AnalyticsRecord.toDocument(record));
                }
            }
            if (!documentsToInsert.values().isEmpty()) {
                db.getCollection(collection).insertMany(new ArrayList<Document>(documentsToInsert.values()));
            }
        } catch (Exception e) {
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw new AnalyticsException("Error in adding record batch: " + e.getMessage(), e);
            }
        }
    }

    private boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        String collection = this.generateTargetCollectionName(tenantId, tableName);
        boolean found = false;
        for (String collectionName : db.listCollectionNames()) {
            if (collectionName.equals(collection)) {
                found = true;
            }
        }
        return found;
    }

}
