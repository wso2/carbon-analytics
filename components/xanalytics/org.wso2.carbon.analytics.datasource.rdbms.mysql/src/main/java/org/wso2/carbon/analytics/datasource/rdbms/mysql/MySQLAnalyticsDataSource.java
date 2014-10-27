/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.datasource.rdbms.mysql;

import java.util.Iterator;
import java.util.Map;

import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceException;
import org.wso2.carbon.analytics.datasource.core.DataType;
import org.wso2.carbon.analytics.datasource.core.FileSystem;
import org.wso2.carbon.analytics.datasource.core.DataType.Type;
import org.wso2.carbon.analytics.datasource.rdbms.common.RDBMSAnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.rdbms.common.RDBMSAnalyticsDSConstants;

/**
 * MySQL {@link AnalyticsDataSource} implementation.
 */
public class MySQLAnalyticsDataSource extends RDBMSAnalyticsDataSource {

    private String storageEngine;

    @Override
    public void init() {
        this.storageEngine = (String) this.getProperties().get(MySQLAnalyticsDSConstants.STORAGE_ENGINE);
    }

    public String getStorageEngine() {
        return storageEngine;
    }
    
    @Override
    public String generateCreateTableSQL(String tableName, Map<String, DataType> columns) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE " + tableName + "(");
        Iterator<Map.Entry<String, DataType>> itr = columns.entrySet().iterator();
        /* add the id column */
        builder.append(generateSQLType(
                RDBMSAnalyticsDSConstants.ID_COLUMN_NAME, 
                new DataType(Type.STRING, 40)));
        builder.append(",");
        /* add the internal timestamp column */
        builder.append(generateSQLType(
                RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME, 
                new DataType(Type.LONG, -1)));
        while (itr.hasNext()) {
            builder.append(",");
            Map.Entry<String, DataType> entry = itr.next();
            builder.append(generateSQLType(entry.getKey(), entry.getValue()));
        }
        builder.append(",");
        builder.append("PRIMARY KEY(" + RDBMSAnalyticsDSConstants.ID_COLUMN_NAME + ")");
        builder.append(")");
        if (this.storageEngine != null) {
            builder.append(" ENGINE = " + this.storageEngine + ";");
        }
        return builder.toString();
    }
    
    @Override
    public String getSQLType(DataType dataType) {
        switch (dataType.type) {
        case STRING:
            if (dataType.size == -1) {
                return MySQLAnalyticsDSConstants.DATA_TYPE_LONG_TEXT;
            } else {
                return MySQLAnalyticsDSConstants.DATA_TYPE_VARCHAR + " (" + dataType.size + ")";
            }
        case BOOLEAN:
            return MySQLAnalyticsDSConstants.DATA_TYPE_BOOLEAN;
        case DOUBLE:
            return MySQLAnalyticsDSConstants.DATA_TYPE_DOUBLE;
        case INTEGER:
            return MySQLAnalyticsDSConstants.DATA_TYPE_INTEGER;
        case LONG:
            return MySQLAnalyticsDSConstants.DATA_TYPE_LONG;
        default:
            throw new RuntimeException("Unknown data type: " + dataType);
        }
    }
    
    @Override
    public String generateAddIndexSQL(String tableName, String column) {
        return "CREATE INDEX " + "index_" + column + " ON " + tableName + "(" + column + ")";
    }
    
    @Override
    public String generateDropIndexSQL(String tableName, String column) {
        return "DROP INDEX " + "index_" + column + " ON " + tableName;
    }

    @Override
    public FileSystem getFileSystem() throws AnalyticsDataSourceException {
        return new MySQLFileSystem(this.getDataSource());
    }

}
