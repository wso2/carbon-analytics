/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.datasource.hbase.util;

import org.apache.hadoop.fs.Path;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceConstants;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.datasource.hbase.HBaseAnalyticsConfigurationEntry;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class HBaseUtils {

    public static String normalizeTableName(String tableName) {
        return tableName.toUpperCase();
    }


    public static String generateTablePrefix(int tenantId, int type) {
        String output = "";
        switch (type) {
            case HBaseAnalyticsDSConstants.DATA:
                if (tenantId < 0) {
                    output = HBaseAnalyticsDSConstants.ANALYTICS_USER_TABLE_PREFIX + "_X" + Math.abs(tenantId) + "_";
                } else {
                    output = HBaseAnalyticsDSConstants.ANALYTICS_USER_TABLE_PREFIX + "_" + tenantId + "_";
                }
                break;
            case HBaseAnalyticsDSConstants.INDEX:
                if (tenantId < 0) {
                    output = HBaseAnalyticsDSConstants.ANALYTICS_INDEX_TABLE_PREFIX + "_X" + Math.abs(tenantId) + "_";
                } else {
                    output = HBaseAnalyticsDSConstants.ANALYTICS_INDEX_TABLE_PREFIX + "_" + tenantId + "_";
                }
                break;
            case HBaseAnalyticsDSConstants.META:
                if (tenantId < 0) {
                    output = HBaseAnalyticsDSConstants.ANALYTICS_META_TABLE_PREFIX + "_X" + Math.abs(tenantId) + "_";
                } else {
                    output = HBaseAnalyticsDSConstants.ANALYTICS_META_TABLE_PREFIX + "_" + tenantId + "_";
                }
                break;
        }
        return output;

    }

    public static String generateTableName(int tenantId, String tableName, int type) {
        return generateTablePrefix(tenantId, type) + normalizeTableName(tableName);
    }

    public static String generateGenericTableName(int tenantId, String tableName) {
        return tenantId + HBaseAnalyticsDSConstants.DELIMITER + tableName;
    }

    public static int inferTenantId(String genericTableName) {
        return Integer.valueOf(genericTableName.substring(0,
                genericTableName.indexOf(HBaseAnalyticsDSConstants.DELIMITER)));
    }

    public static String inferTableName(String genericTableName) {
        int breakpoint = genericTableName.indexOf(HBaseAnalyticsDSConstants.DELIMITER);
        return genericTableName.substring(breakpoint + HBaseAnalyticsDSConstants.DELIMITER.length(),
                genericTableName.length());
    }

    public static Path createPath(String source) {
        source = GenericUtils.normalizePath(source);
        return new Path(source);
    }

    public static byte[] encodeLong(long value) {
        return Long.toString(value).getBytes();
    }

    public static long decodeLong(byte[] arr) {
        return Long.parseLong(new String(arr));
    }

    public static HBaseAnalyticsConfigurationEntry lookupConfiguration() throws AnalyticsException {
        try {
            File confFile = new File(CarbonUtils.getCarbonConfigDirPath() +
                    File.separator + AnalyticsDataSourceConstants.ANALYTICS_CONF_DIR + File.separator +
                    HBaseAnalyticsDSConstants.HBASE_ANALYTICS_CONFIG_FILE);
            if (!confFile.exists()) {
                throw new AnalyticsException("Cannot initialize HBase analytics data source "
                        + "the configuration file cannot be found at: " + confFile.getPath());
            }
            JAXBContext ctx = JAXBContext.newInstance(HBaseAnalyticsConfigurationEntry.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            return (HBaseAnalyticsConfigurationEntry) unmarshaller.unmarshal(confFile);
        } catch (JAXBException e) {
            throw new AnalyticsException("Error in processing HBase analytics data source configuration: " +
                    e.getMessage(), e);
        }
    }

}
