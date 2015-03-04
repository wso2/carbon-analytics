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
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

public class HBaseUtils {

    public static String normalizeTableName(String tableName) {
        return tableName.toUpperCase();
    }


    public static String generateAnalyticsTablePrefix(int tenantId) {
        if (tenantId < 0) {
            return HBaseAnalyticsDSConstants.ANALYTICS_USER_TABLE_PREFIX + "_X" + Math.abs(tenantId) + "_";
        } else {
            return HBaseAnalyticsDSConstants.ANALYTICS_USER_TABLE_PREFIX + "_" + tenantId + "_";
        }
    }

    public static String generateAnalyticsTableName(int tenantId, String tableName) {
        return generateAnalyticsTablePrefix(tenantId) + normalizeTableName(tableName);
    }

    public static String generateIndexTablePrefix(int tenantId) {
        if (tenantId < 0) {
            return HBaseAnalyticsDSConstants.ANALYTICS_INDEX_TABLE_PREFIX + "_X" + Math.abs(tenantId) + "_";
        } else {
            return HBaseAnalyticsDSConstants.ANALYTICS_INDEX_TABLE_PREFIX + "_" + tenantId + "_";
        }
    }

    public static String generateIndexTableName(int tenantId, String tableName) {
        return generateIndexTablePrefix(tenantId) + normalizeTableName(tableName);
    }

    public static String convertUserToIndexTable(String userTable) {
        return userTable.replaceFirst(HBaseAnalyticsDSConstants.ANALYTICS_USER_TABLE_PREFIX,
                HBaseAnalyticsDSConstants.ANALYTICS_INDEX_TABLE_PREFIX);
    }

    public static Path createPath(String source){
        source = GenericUtils.normalizePath(source);
        return new Path(source);
    }

}
