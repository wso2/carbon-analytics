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
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

public class HBaseUtils {

    private static final byte BOOLEAN_TRUE = 1;

    private static final byte BOOLEAN_FALSE = 0;

    private static final byte DATA_TYPE_NULL = 0x00;

    private static final byte DATA_TYPE_STRING = 0x01;

    private static final byte DATA_TYPE_INTEGER = 0x02;

    private static final byte DATA_TYPE_LONG = 0x03;

    private static final byte DATA_TYPE_FLOAT = 0x04;

    private static final byte DATA_TYPE_DOUBLE = 0x05;

    private static final byte DATA_TYPE_BOOLEAN = 0x06;

    private static final byte DATA_TYPE_BINARY = 0x07;

    private static final String DEFAULT_CHARSET = "UTF8";

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

    public static Map<String,Object> decodeElementValue(Cell[] cells) throws AnalyticsException {
        /* using LinkedHashMap to retain the column order */
        Map<String, Object> values = new LinkedHashMap<>();
        ByteBuffer buffer;
        int type, size;
        String colName;
        Object value;
        byte[] buff;
        byte boolVal;
        byte[] binData;

        for(Cell cell : cells){
            try {
                buffer = ByteBuffer.wrap(CellUtil.cloneValue(cell));
                size = buffer.getInt();
                buff = new byte[size];
                buffer.get(buff, 0, size);
                colName = new String(buff, DEFAULT_CHARSET);
                type = buffer.get();
                switch (type) {
                    case DATA_TYPE_STRING:
                        size = buffer.getInt();
                        buff = new byte[size];
                        buffer.get(buff, 0, size);
                        value = new String(buff, DEFAULT_CHARSET);
                        break;
                    case DATA_TYPE_LONG:
                        value = buffer.getLong();
                        break;
                    case DATA_TYPE_DOUBLE:
                        value = buffer.getDouble();
                        break;
                    case DATA_TYPE_BOOLEAN:
                        boolVal = buffer.get();
                        if (boolVal == BOOLEAN_TRUE) {
                            value = true;
                        } else if (boolVal == BOOLEAN_FALSE) {
                            value = false;
                        } else {
                            throw new AnalyticsException("Invalid encoded boolean value " + boolVal+ " for column "+colName);
                        }
                        break;
                    case DATA_TYPE_INTEGER:
                        value = buffer.getInt();
                        break;
                    case DATA_TYPE_FLOAT:
                        value = buffer.getFloat();
                        break;
                    case DATA_TYPE_BINARY:
                        size = buffer.getInt();
                        binData = new byte[size];
                        buffer.get(binData);
                        value = binData;
                        break;
                    case DATA_TYPE_NULL:
                        value = null;
                        break;
                    default:
                        throw new AnalyticsException("Unknown encoded data source type: " + type+ " for column "+colName);
                }
                values.put(colName, value);

            } catch (Exception e) {
                throw new AnalyticsException("Error in decoding cell values: " + e.getMessage());
            }
        }
        return values;
    }

}
