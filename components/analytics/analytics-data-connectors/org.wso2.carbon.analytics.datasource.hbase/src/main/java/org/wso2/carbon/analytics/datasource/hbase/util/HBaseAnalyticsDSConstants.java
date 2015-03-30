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

import java.nio.charset.StandardCharsets;

/**
 * Class for holding constants required for the HBase Analytics Datasource
 */
public class HBaseAnalyticsDSConstants {

    public static final String DATASOURCE_NAME = "datasource";

    public static final String ANALYTICS_USER_TABLE_PREFIX = "ANX";
    public static final String ANALYTICS_INDEX_TABLE_PREFIX = "IDX";

    public static final String ANALYTICS_META_TABLE_NAME = "ANALYTICS_META_TABLE";

    public enum TableType {
        DATA,
        INDEX
    }

    public static final byte[] ANALYTICS_DATA_COLUMN_FAMILY_NAME = "carbon-analytics-data".getBytes(StandardCharsets.UTF_8);
    public static final byte[] ANALYTICS_META_COLUMN_FAMILY_NAME = "carbon-analytics-meta".getBytes(StandardCharsets.UTF_8);
    public static final byte[] ANALYTICS_INDEX_COLUMN_FAMILY_NAME = "carbon-analytics-index".getBytes(StandardCharsets.UTF_8);

    public static final byte[] ANALYTICS_ROWDATA_QUALIFIER_NAME = "row-values".getBytes(StandardCharsets.UTF_8);
    public static final byte[] ANALYTICS_SCHEMA_QUALIFIER_NAME = "database-schema".getBytes(StandardCharsets.UTF_8);
    public static final byte[] ANALYTICS_TS_QUALIFIER_NAME = "timestamp".getBytes(StandardCharsets.UTF_8);

    public static final int DEFAULT_QUERY_BATCH_SIZE = 7000;
    public static final String HBASE_ANALYTICS_CONFIG_FILE = "hbase-analytics-config.xml";
    public static final String DELIMITER = "~%~";

}
