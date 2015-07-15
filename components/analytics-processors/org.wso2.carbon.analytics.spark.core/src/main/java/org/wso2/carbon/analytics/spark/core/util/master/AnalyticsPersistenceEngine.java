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

package org.wso2.carbon.analytics.spark.core.util.master;

import akka.serialization.Serialization;
import akka.serialization.Serializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.deploy.master.PersistenceEngine;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import scala.collection.JavaConversions;
import scala.collection.Seq;
import scala.reflect.ClassTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by niranda on 6/9/15. //todo: change this
 */
public class AnalyticsPersistenceEngine extends PersistenceEngine {

    private static final Log log = LogFactory.getLog(AnalyticsPersistenceEngine.class);
    private Serialization serialization;
    private AnalyticsDataService ads;

    private static final String SPARK_META_TABLE = "__spark_meta_table";
    private static final String OBJ_COLUMN = "obj_col";
    private int SPARK_TENANT = AnalyticsConstants.SPARK_PERSISTENCE_TENANT_ID; // dont use 2 variables

    public AnalyticsPersistenceEngine(Serialization serialization) {
        this.serialization = serialization;
        this.ads = AnalyticsServiceHolder.getAnalyticsDataService();
    }

    @Override
    public void persist(String name, Object obj) {
        Serializer serializer = serialization.findSerializerFor(obj);
        byte[] serialized = serializer.toBinary(obj);

        try {
            if (!ads.tableExists(SPARK_TENANT, SPARK_META_TABLE)) {
                ads.createTable(SPARK_TENANT, SPARK_META_TABLE);
            }

            Map<String, Object> values = new HashMap<>(1);
            values.put(OBJ_COLUMN, serialized);

            Record record = new Record(name, SPARK_TENANT, SPARK_META_TABLE, values);
            List<Record> records = new ArrayList<>(1);
            records.add(record);

            ads.put(records);
        } catch (AnalyticsException e) {
            String msg = "Error in writing data to spark meta table: " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public void unpersist(String name) {
        try {
            List<String> recordIds = new ArrayList<>(1);
            recordIds.add(name);
            ads.delete(SPARK_TENANT, SPARK_META_TABLE, recordIds);
        } catch (AnalyticsException e) {
            String msg = "Error in deleting data from spark meta table: " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public <T> Seq<T> read(String prefix, ClassTag<T> evidence$1) {
        Class<T> clazz = (Class<T>) evidence$1.runtimeClass();
        Serializer serializer = serialization.findSerializerFor(clazz);

        List<T> objects = new ArrayList<>();
        try {
            if (ads.tableExists(SPARK_TENANT, SPARK_META_TABLE)) { //todo: use AnalyticsDataServiceUtils.listRecords method
                AnalyticsDataResponse results = ads.get(SPARK_TENANT, SPARK_META_TABLE, 1, null,
                                                        Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
                for (RecordGroup recordGroup : results.getRecordGroups()) {
                    Iterator<Record> iterator = ads.readRecords(results.getRecordStoreName(), recordGroup);
                    //todo: (later) separate the prefixes in different tables
                    while (iterator.hasNext()) {
                        Record record = iterator.next();
                        if (record.getId().startsWith(prefix)) {
                            objects.add((T) serializer.fromBinary((byte[]) record.getValue(OBJ_COLUMN), clazz));
                        }
                    }
                }
            }
        } catch (AnalyticsException e) {
            String msg = "Error in reading data from spark meta table: " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        return JavaConversions.asScalaBuffer(objects).toSeq();
    }
}
