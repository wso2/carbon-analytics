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
import org.apache.spark.SparkConf;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.master.AbstractPersistenceEngine;
import scala.collection.JavaConversions;
import scala.collection.Seq;
import scala.reflect.ClassTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by niranda on 6/9/15.
 */
public class AnalyticsPersistenceEngine extends AbstractPersistenceEngine {

    private static final Log log = LogFactory.getLog(AnalyticsPersistenceEngine.class);
    private SparkConf conf;
    private Serialization serialization;
    private AnalyticsDataService ads;

    private static final String SPARK_META_TABLE = "__spark_meta_table";
    private static final String OBJ_COLUMN = "obj_col";
    private static int SPARK_TENANT;

    public AnalyticsPersistenceEngine(SparkConf conf, Serialization serialization) {
        this.serialization = serialization;
        this.conf = conf;
        this.ads = AnalyticsServiceHolder.getAnalyticsDataService();

        SPARK_TENANT = Integer.parseInt(conf.get(AnalyticsConstants.CARBON_TENANT_ID));
    }

    /**
     * Defines how the object is serialized and persisted. Implementation will
     * depend on the store used.
     *
     * @param name
     * @param obj
     */
    @Override
    public void persist(String name, Object obj) {
        System.out.println("################ PERSISTING DATA for : " + name);
        Serializer serializer = serialization.findSerializerFor(obj);
        byte[] serialized = serializer.toBinary(obj);

        try {
            if (!ads.tableExists(SPARK_TENANT, SPARK_META_TABLE)) {
                ads.createTable(SPARK_TENANT, SPARK_META_TABLE);
            }

            Map<String, Object> values = new HashMap<>();
            values.put(OBJ_COLUMN, serialized);

            Record record = new Record(name, SPARK_TENANT, SPARK_META_TABLE, values);
            List<Record> records = new ArrayList<>();
            records.add(record);

            ads.put(records);
        } catch (AnalyticsException e) {
            log.error("Error in writing data to spark meta table ", e);
        }
    }

    /**
     * Defines how the object referred by its name is removed from the store.
     *
     * @param name
     */
    @Override
    public void unpersist(String name) {
        System.out.println("################ UNPERSISTING DATA for : " + name);
        try {
            List<String> recordIds = new ArrayList<>();
            recordIds.add(name);
            ads.delete(SPARK_TENANT, SPARK_META_TABLE, recordIds);
        } catch (AnalyticsException e) {
            log.error("Error in deleting data from spark meta table ", e);
        }
    }

    /**
     * Gives all objects, matching a prefix. This defines how objects are
     * read/deserialized back.
     *
     * @param prefix
     * @param evidence$1
     */
    @Override
    public <T> Seq<T> read(String prefix, ClassTag<T> evidence$1) {
        System.out.println("################ READING DATA for : " + prefix);

        Class<T> clazz = (Class<T>) evidence$1.runtimeClass();
        Serializer serializer = serialization.findSerializerFor(clazz);

        List<T> objects = new ArrayList<>();
        try {
            AnalyticsDataResponse results = ads.get(SPARK_TENANT, SPARK_META_TABLE, 1, null,
                                                    Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
            for (RecordGroup recordGroup : results.getRecordGroups()) {
                Iterator<Record> iterator = ads.readRecords(results.getRecordStoreName(), recordGroup);
                while(iterator.hasNext()){
                    Record record = iterator.next();
                    if (record.getId().startsWith(prefix)){
                        objects.add((T) serializer.fromBinary((byte[]) record.getValue(OBJ_COLUMN),clazz));
                    }
                }
            }

        } catch (AnalyticsException e) {
            log.error("Error in reading data from the spark meta table ", e);
        }

        return JavaConversions.asScalaBuffer(objects).toSeq();
    }
}
