/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.stream.processor.core.persistence.util;

import org.apache.log4j.Logger;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.database.query.manager.exception.QueryMappingNotAvailableException;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.persistence.dto.RDBMSQueryConfigurationEntry;
import org.wso2.carbon.stream.processor.core.persistence.exception.DatasourceConfigurationException;
import org.wso2.carbon.stream.processor.core.persistence.query.QueryManager;

import java.io.IOException;

/**
 * Class used to get Database queries according to RDBMS type used
 */
public class RDBMSConfiguration {

    private static RDBMSConfiguration config = new RDBMSConfiguration();

    private static final Logger log = Logger.getLogger(RDBMSConfiguration.class);

    private RDBMSConfiguration() {
    }

    public static RDBMSConfiguration getInstance() {
        return config;
    }

    public RDBMSQueryConfigurationEntry getDatabaseQueryEntries(String databaseType, String databaseVersion,
                                                                String tableName) {
        RDBMSQueryConfigurationEntry databaseQueryEntries = new RDBMSQueryConfigurationEntry();
        try {
            QueryManager queryManager = new QueryManager(databaseType, databaseVersion,
                    StreamProcessorDataHolder.getInstance().getConfigProvider());

            databaseQueryEntries.setCreateTableQuery(queryManager.getQuery(PersistenceConstants.CREATE_TABLE).
                    replace(PersistenceConstants.PLACEHOLDER_TABLE_NAME, tableName));
            databaseQueryEntries.setInsertTableQuery(queryManager.getQuery(PersistenceConstants.INSERT_INTO_TABLE).
                    replace(PersistenceConstants.PLACEHOLDER_TABLE_NAME, tableName));
            databaseQueryEntries.setIsTableExistQuery(queryManager.getQuery(PersistenceConstants.IS_TABLE_EXISTS).
                    replace(PersistenceConstants.PLACEHOLDER_TABLE_NAME, tableName));
            databaseQueryEntries.setSelectTableQuery(queryManager.getQuery(PersistenceConstants.SELECT_SNAPSHOT).
                    replace(PersistenceConstants.PLACEHOLDER_TABLE_NAME, tableName));
            databaseQueryEntries.setSelectLastQuery(queryManager.getQuery(PersistenceConstants.SELECT_LAST_REVISION).
                    replace(PersistenceConstants.PLACEHOLDER_TABLE_NAME, tableName));
            databaseQueryEntries.setSelectRevisionsQuery(queryManager.getQuery(PersistenceConstants.SELECT_REVISIONS).
                    replace(PersistenceConstants.PLACEHOLDER_TABLE_NAME, tableName));
            databaseQueryEntries.setDeleteQuery(queryManager.getQuery(PersistenceConstants.DELETE_ROW_FROM_TABLE).
                    replace(PersistenceConstants.PLACEHOLDER_TABLE_NAME, tableName));
            databaseQueryEntries.setDeleteOldRevisionsQuery(queryManager.getQuery(PersistenceConstants.DELETE_OLD_REVISIONS).
                    replace(PersistenceConstants.PLACEHOLDER_TABLE_NAME, tableName));
            databaseQueryEntries.setCountQuery(queryManager.getQuery(PersistenceConstants.COUNT_NUMBER_REVISIONS).
                    replace(PersistenceConstants.PLACEHOLDER_TABLE_NAME, tableName));

        } catch (QueryMappingNotAvailableException | ConfigurationException | IOException e) {
            throw new DatasourceConfigurationException("Error reading queries for database: " + databaseType + " "
                    + databaseVersion, e);
        }
        return databaseQueryEntries;
    }
}
