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
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceConstants;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.datasource.hbase.HBaseAnalyticsConfigurationEntry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class containing various utility methods required by classes from the HBase Analytics Datasource implementation
 */
public class HBaseUtils {

    public static String normalizeTableName(String tableName) {
        return tableName.toUpperCase();
    }

    public static String generateTablePrefix(int tenantId, HBaseAnalyticsDSConstants.TableType type) {
        String output = "";
        switch (type) {
            case DATA:
                if (tenantId < 0) {
                    output = HBaseAnalyticsDSConstants.ANALYTICS_USER_TABLE_PREFIX + "_X" + Math.abs(tenantId) + "_";
                } else {
                    output = HBaseAnalyticsDSConstants.ANALYTICS_USER_TABLE_PREFIX + "_" + tenantId + "_";
                }
                break;
            case INDEX:
                if (tenantId < 0) {
                    output = HBaseAnalyticsDSConstants.ANALYTICS_INDEX_TABLE_PREFIX + "_X" + Math.abs(tenantId) + "_";
                } else {
                    output = HBaseAnalyticsDSConstants.ANALYTICS_INDEX_TABLE_PREFIX + "_" + tenantId + "_";
                }
                break;
        }
        return output;

    }

    public static String generateTableName(int tenantId, String tableName, HBaseAnalyticsDSConstants.TableType type) {
        return generateTablePrefix(tenantId, type) + normalizeTableName(tableName);
    }

    public static String generateGenericTableName(int tenantId, String tableName) {
        return tenantId + HBaseAnalyticsDSConstants.DELIMITER + tableName;
    }

    public static int inferTenantId(String genericTableName) {
        return Integer.parseInt(genericTableName.substring(0,
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
        return Bytes.toBytes(value);
    }

    public static long decodeLong(byte[] arr) {
        return Bytes.toLong(arr);
    }

    public static Record constructRecord(Result currentResult, int tenantId, String tableName, Set<String> colSet)
            throws AnalyticsException {
        byte[] rowId = currentResult.getRow();
        Map<String, Object> values;
        if (currentResult.containsColumn(HBaseAnalyticsDSConstants.ANALYTICS_DATA_COLUMN_FAMILY_NAME,
                HBaseAnalyticsDSConstants.ANALYTICS_ROWDATA_QUALIFIER_NAME)) {
            Cell dataCell = currentResult.getColumnLatestCell
                    (HBaseAnalyticsDSConstants.ANALYTICS_DATA_COLUMN_FAMILY_NAME,
                            HBaseAnalyticsDSConstants.ANALYTICS_ROWDATA_QUALIFIER_NAME);
            byte[] data = CellUtil.cloneValue(dataCell);
            if (data.length > 0) {
                values = GenericUtils.decodeRecordValues(data, colSet);
            } else {
                values = new HashMap<>();
            }
            if (currentResult.containsColumn(HBaseAnalyticsDSConstants.ANALYTICS_DATA_COLUMN_FAMILY_NAME,
                    HBaseAnalyticsDSConstants.ANALYTICS_TS_QUALIFIER_NAME)) {
                Cell tsCell = currentResult.getColumnLatestCell(HBaseAnalyticsDSConstants.ANALYTICS_DATA_COLUMN_FAMILY_NAME,
                        HBaseAnalyticsDSConstants.ANALYTICS_TS_QUALIFIER_NAME);
                byte[] timestamp = CellUtil.cloneValue(tsCell);
                if (timestamp.length > 0) {
                    return new Record(Bytes.toString(rowId), tenantId, tableName, values, Bytes.toLong(timestamp));
                }
            }
        }
        return null;
    }

    public static HBaseAnalyticsConfigurationEntry lookupConfiguration() throws AnalyticsException {
        try {
            File confFile = new File(GenericUtils.getAnalyticsConfDirectory() +
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
