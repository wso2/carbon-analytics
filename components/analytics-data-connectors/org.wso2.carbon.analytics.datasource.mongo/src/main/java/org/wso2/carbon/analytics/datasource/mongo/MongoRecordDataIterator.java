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

import java.io.IOException;
import java.util.List;

import org.bson.Document;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.Record;

import com.mongodb.client.MongoCursor;

/**
 * Iterator for a record.
 *
 */
public class MongoRecordDataIterator implements AnalyticsIterator<Record> {

    private MongoCursor<Document> iterable;

    private List<String> columns;

    private String tableName;

    private Integer tenantId;

    public MongoRecordDataIterator(MongoCursor<Document> mongoIterable, List<String> columns, String tableName, Integer tenantId) {
        this.iterable = mongoIterable;
        this.columns = columns;
        this.tableName = tableName;
        this.tenantId = tenantId;
    }

    @Override
    public boolean hasNext() {
        return iterable.hasNext();
    }

    @Override
    public Record next() {
        Document document = iterable.next();
        return AnalyticsRecord.toRecord(AnalyticsRecord.fromDocument(document, tableName, tenantId), columns);
    }

    @Override
    public void remove() {
        /* this is a read-only iterator, nothing will be removed */
    }

    @Override
    public void close() throws IOException {
        iterable.close();
    }

}
