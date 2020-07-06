/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.streaming.integrator.core.siddhi.error.handler.util;

import org.apache.log4j.Logger;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.database.query.manager.exception.QueryMappingNotAvailableException;
import org.wso2.carbon.streaming.integrator.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.streaming.integrator.core.siddhi.error.handler.dto.RDBMSQueryConfigurationEntry;
import org.wso2.carbon.streaming.integrator.core.siddhi.error.handler.query.QueryManager;
import org.wso2.carbon.streaming.integrator.core.siddhi.error.handler.exception.DatasourceConfigurationException;


import java.io.IOException;
import java.util.Locale;

/**
 * Class used to get Database queries according to RDBMS type used.
 */
public class RDBMSConfiguration {

    private static RDBMSConfiguration config = new RDBMSConfiguration();

    private static final Logger log = Logger.getLogger(RDBMSConfiguration.class);
    private static final String DB2_DB_TYPE = "db2";

    private RDBMSConfiguration() {
    }

    public static RDBMSConfiguration getInstance() {
        return config;
    }

    public RDBMSQueryConfigurationEntry getDatabaseQueryEntries(String databaseType, String databaseVersion,
                                                                String tableName) {
        RDBMSQueryConfigurationEntry databaseQueryEntries = new RDBMSQueryConfigurationEntry();
        // DB2 product name changes with the specific versions(For an example DB2/LINUXX8664, DB2/NT). Hence, checks
        // whether the product name contains "DB2".
        if (databaseType.toLowerCase(Locale.ENGLISH).contains(DB2_DB_TYPE)) {
            databaseType = DB2_DB_TYPE;
        }
        try {
            QueryManager queryManager = new QueryManager(databaseType, databaseVersion,
                    StreamProcessorDataHolder.getInstance().getConfigProvider());

            databaseQueryEntries.setIsTableExistQuery(
                queryManager.getQuery(SiddhiErrorHandlerConstants.IS_TABLE_EXISTS).
                    replace(SiddhiErrorHandlerConstants.PLACEHOLDER_TABLE_NAME, tableName));
            databaseQueryEntries.setCreateTableQuery(queryManager.getQuery(
                SiddhiErrorHandlerConstants.CREATE_TABLE).
                replace(SiddhiErrorHandlerConstants.PLACEHOLDER_TABLE_NAME, tableName));
            databaseQueryEntries.setInsertTableQuery(queryManager.getQuery(
                SiddhiErrorHandlerConstants.INSERT_INTO_TABLE).
                replace(SiddhiErrorHandlerConstants.PLACEHOLDER_TABLE_NAME, tableName));
            databaseQueryEntries.setSelectTableQuery(queryManager.getQuery(
                SiddhiErrorHandlerConstants.SELECT_FROM_TABLE).
                replace(SiddhiErrorHandlerConstants.PLACEHOLDER_TABLE_NAME, tableName));
            databaseQueryEntries.setSelectWithLimitOffsetQuery(
                queryManager.getQuery(SiddhiErrorHandlerConstants.SELECT_WITH_LIMIT_OFFSET).
                    replace(SiddhiErrorHandlerConstants.PLACEHOLDER_TABLE_NAME, tableName));
            databaseQueryEntries.setSelectCountQuery(
                queryManager.getQuery(SiddhiErrorHandlerConstants.SELECT_COUNT_FROM_TABLE).
                    replace(SiddhiErrorHandlerConstants.PLACEHOLDER_TABLE_NAME, tableName));
            databaseQueryEntries.setDeleteQuery(
                queryManager.getQuery(SiddhiErrorHandlerConstants.DELETE_ROW_FROM_TABLE).
                replace(SiddhiErrorHandlerConstants.PLACEHOLDER_TABLE_NAME, tableName));

        } catch (QueryMappingNotAvailableException | ConfigurationException | IOException e) {
            throw new DatasourceConfigurationException("Error reading queries for database: " + databaseType + " "
                    + databaseVersion, e);
        }
        return databaseQueryEntries;
    }
}
