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
import org.wso2.analytics.data.commons.AnalyticsRecordStore;
import org.wso2.analytics.data.commons.Record;
import org.wso2.analytics.data.commons.RecordGroup;
import org.wso2.analytics.data.commons.exception.AnalyticsException;
import org.wso2.analytics.dataservice.AnalyticsDataService;
import org.wso2.analytics.dataservice.commons.AnalyticsDataResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsDataServiceUtils {

    private static final Log log = LogFactory.getLog(AnalyticsDataServiceUtils.class);

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

    public static List<Record> listRecords(AnalyticsRecordStore rs, RecordGroup[] rgs) throws AnalyticsException {
        List<Record> result = new ArrayList<>();
        for (RecordGroup rg : rgs) {
            result.addAll(IteratorUtils.toList(rs.readRecords(rg)));
        }
        return result;
    }

    public static List<Record> listRecords(AnalyticsDataService ads,
                                           AnalyticsDataResponse response) throws AnalyticsException {
        List<Record> result = new ArrayList<>();
        for (AnalyticsDataResponse.Entry entry : response.getEntries()) {
            result.addAll(IteratorUtils.toList(ads.readRecords(entry.getRecordStoreName(), entry.getRecordGroup())));
        }
        return result;
    }

}
