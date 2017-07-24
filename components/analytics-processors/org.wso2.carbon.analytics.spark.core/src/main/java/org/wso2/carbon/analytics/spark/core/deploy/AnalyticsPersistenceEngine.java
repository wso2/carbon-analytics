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
package org.wso2.carbon.analytics.spark.core.deploy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.deploy.master.PersistenceEngine;
import org.apache.spark.serializer.Serializer;
import org.apache.spark.serializer.SerializerInstance;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse.Entry;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import scala.collection.JavaConversions;
import scala.collection.Seq;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class represents the analytics persistence engine which is responsible for persist,
 * unpersist and read spark failover recovery related data
 */
public class AnalyticsPersistenceEngine extends PersistenceEngine {

    private static final Log log = LogFactory.getLog(AnalyticsPersistenceEngine.class);
    private Serializer serializer;
    private AnalyticsDataService ads;

    private static final String SPARK_META_TABLE = "__spark_meta_table";
    private static final String OBJ_COLUMN = "obj_col";

    public AnalyticsPersistenceEngine(Serializer serializer) {
        this.serializer = serializer;
        this.ads = AnalyticsServiceHolder.getAnalyticsDataService();
    }

    @Override
    public void persist(String name, Object obj) {
        SerializerInstance serializer = this.serializer.newInstance();
        byte[] serialized = serializer.serialize(obj, ClassTag$.MODULE$.Object()).array();

        try {
            if (!ads.tableExists(AnalyticsConstants.SPARK_PERSISTENCE_TENANT_ID, SPARK_META_TABLE)) {
                ads.createTable(AnalyticsConstants.SPARK_PERSISTENCE_TENANT_ID, SPARK_META_TABLE);
            }

            Map<String, Object> values = new HashMap<>(1);
            values.put(OBJ_COLUMN, serialized);

            Record record = new Record(name, AnalyticsConstants.SPARK_PERSISTENCE_TENANT_ID, SPARK_META_TABLE, values);
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
            ads.delete(AnalyticsConstants.SPARK_PERSISTENCE_TENANT_ID, SPARK_META_TABLE, recordIds);
        } catch (AnalyticsException e) {
            String msg = "Error in deleting data from spark meta table: " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Seq<T> read(String prefix, ClassTag<T> evidence$1) {
        SerializerInstance serializer = this.serializer.newInstance();

        List<T> objects = new ArrayList<>();
        try {
            if (ads.tableExists(AnalyticsConstants.SPARK_PERSISTENCE_TENANT_ID, SPARK_META_TABLE)) { //todo: use AnalyticsDataServiceUtils.listRecords method
                AnalyticsDataResponse results = ads.get(AnalyticsConstants.SPARK_PERSISTENCE_TENANT_ID, SPARK_META_TABLE, 1, null,
                                                        Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
                for (Entry entry : results.getEntries()) {
                    Iterator<Record> iterator = ads.readRecords(entry.getRecordStoreName(), entry.getRecordGroup());
                    //todo: (later) separate the prefixes in different tables
                    while (iterator.hasNext()) {
                        Record record = iterator.next();
                        if (record.getId().startsWith(prefix)) {
                            objects.add(serializer.deserialize(ByteBuffer.wrap((byte[]) record.getValue(OBJ_COLUMN)), evidence$1));
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

    public static void cleanupSparkMetaTable() throws AnalyticsException {
        if (AnalyticsServiceHolder.getAnalyticsDataService().tableExists(AnalyticsConstants.SPARK_PERSISTENCE_TENANT_ID,
                                                                         SPARK_META_TABLE)) {
            AnalyticsServiceHolder.getAnalyticsDataService().delete(AnalyticsConstants.SPARK_PERSISTENCE_TENANT_ID,
                                                                    SPARK_META_TABLE, Long.MIN_VALUE, Long.MAX_VALUE);
        }
    }
}
