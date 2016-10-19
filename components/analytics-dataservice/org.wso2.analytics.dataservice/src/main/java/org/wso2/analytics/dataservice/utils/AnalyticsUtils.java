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
package org.wso2.analytics.dataservice.utils;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.analytics.dataservice.AnalyticsDataService;
import org.wso2.analytics.dataservice.AnalyticsRecordStore;
import org.wso2.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.analytics.dataservice.commons.Record;
import org.wso2.analytics.dataservice.commons.RecordGroup;
import org.wso2.analytics.dataservice.commons.exception.AnalyticsException;

import java.io.File;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.*;

public class AnalyticsUtils {
    private static final Log log = LogFactory.getLog(AnalyticsUtils.class);

    public static String getAnalyticsConfDirectory() throws AnalyticsException {
        File confDir = null;
        try {
            confDir = new File(getConfDirectoryPath());
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error in getting the config path: " + e.getMessage(), e);
            }
        }
        if (confDir == null || !confDir.exists()) {
            return getConfDirectoryPath();
        } else {
            return confDir.getAbsolutePath();
        }
    }

    public static String getConfDirectoryPath() {
        String carbonConfigDirPath = System.getProperty("carbon.config.dir.path");
        if (carbonConfigDirPath == null) {
            carbonConfigDirPath = System.getenv("CARBON_CONFIG_DIR_PATH");
            if (carbonConfigDirPath == null) {
                return getBaseDirectoryPath() + File.separator + "conf";
            }
        }
        return carbonConfigDirPath;
    }

    public static String getBaseDirectoryPath() {
        String baseDir = System.getProperty("analytics.home");
        if (baseDir == null) {
            baseDir = System.getenv("ANALYTICS_HOME");
            System.setProperty("analytics.home", baseDir);
        }
        return baseDir;
    }

    public static Collection<List<Record>> generateRecordBatches(List<Record> records) {
        return generateRecordBatches(records, false);
    }

    public static Collection<List<Record>> generateRecordBatches(List<Record> records, boolean normalizeTableName) {
        /* if the records have identities (unique table category and name) as the following
         * "ABABABCCAACBDABCABCDBAC", the job of this method is to make it like the following,
         * {"AAAAAAAA", "BBBBBBB", "CCCCCC", "DD" } */
        Map<String, List<Record>> recordBatches = new HashMap<>();
        List<Record> recordBatch;
        for (Record record : records) {
            if (normalizeTableName) {
                record.setTableName(normalizeTableName(record.getTableName()));
            }
            recordBatch = recordBatches.get(calculateRecordIdentity(record));
            if (recordBatch == null) {
                recordBatch = new ArrayList<>();
                recordBatches.put(calculateRecordIdentity(record), recordBatch);
            }
            recordBatch.add(record);
        }
        return recordBatches.values();
    }

    public static String calculateRecordIdentity(Record record) {
        return normalizeTableName(record.getTableName());
    }

    public static String normalizeTableName(String tableName) {
        return tableName.toUpperCase();
    }

    public static List<Record> listRecords(AnalyticsRecordStore rs, RecordGroup[] rgs) throws AnalyticsException {
        List<Record> result = new ArrayList<>();
        for (RecordGroup rg : rgs) {
            result.addAll(IteratorUtils.toList(rs.readRecords(rg)));
        }
        return result;
    }

    public static List<Record> listRecords(AnalyticsDataService ads,
                                           AnalyticsDataResponse response) throws AnalyticsException {
        List<Record> result = new ArrayList<Record>();
        for (AnalyticsDataResponse.Entry entry : response.getEntries()) {
            result.addAll(IteratorUtils.toList(ads.readRecords(entry.getRecordStoreName(), entry.getRecordGroup())));
        }
        return result;
    }

    public static String generateRecordID() {
        byte[] data = new byte[16];
        secureRandom.get().nextBytes(data);
        ByteBuffer buff = ByteBuffer.wrap(data);
        return new UUID(buff.getLong(), buff.getLong()).toString();
    }

    private static ThreadLocal<SecureRandom> secureRandom = new ThreadLocal<SecureRandom>() {
        protected SecureRandom initialValue() {
            return new SecureRandom();
        }
    };

}
