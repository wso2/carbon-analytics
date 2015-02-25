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
import org.apache.hadoop.hbase.TableName;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

public class HBaseUtils {

    private static String generateTablePrefix(int tenantId) {
        if (tenantId < 0) {
            return HBaseAnalyticsDSConstants.ANALYTICS_USER_TABLE_PREFIX + "_X" + Math.abs(tenantId) + "_";
        } else {
            return HBaseAnalyticsDSConstants.ANALYTICS_USER_TABLE_PREFIX + "_" + tenantId + "_";
        }
    }

    private static String normalizeTableName(String tableName) {
        return tableName.toUpperCase();
    }

    public static TableName generateTableName(int tenantId, String tableName) {
        return TableName.valueOf(generateTablePrefix(tenantId) + normalizeTableName(tableName));
    }

    public static Path createPath(String source){
        source = GenericUtils.normalizePath(source);
        return new Path(source);
    }

}
