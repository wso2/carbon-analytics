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
import org.wso2.carbon.stream.processor.core.persistence.dto.RDBMSQueryConfiguration;
import org.wso2.carbon.stream.processor.core.persistence.dto.RDBMSQueryConfigurationEntry;
import org.wso2.siddhi.core.exception.CannotLoadConfigurationException;

import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import static org.wso2.carbon.stream.processor.core.persistence.util.PersistenceConstants.RDBMS_QUERY_CONFIG_FILE;

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

    public RDBMSQueryConfigurationEntry getDatabaseQueryEntries(String databaseType, String tableName) {
        return resolveTableName(loadDatabaseQueryEntries(databaseType), tableName);
    }

    /**
     * Method that inserts the Table Names in the DB Queries
     * @param databaseQueryEntries contains the list of DB Queries
     * @param tableName is the table name to be used
     * @return the list of DB Queries with the table name inserted
     */
    private RDBMSQueryConfigurationEntry resolveTableName
            (RDBMSQueryConfigurationEntry databaseQueryEntries, String tableName) {
        if (databaseQueryEntries == null) {
            return null;
        }
        databaseQueryEntries.setCreateTableQuery(databaseQueryEntries.getCreateTableQuery().
                replace(PersistenceConstants.PLACEHOLDER_TABLE_NAME, tableName));
        databaseQueryEntries.setInsertTableQuery(databaseQueryEntries.getInsertTableQuery().
                replace(PersistenceConstants.PLACEHOLDER_TABLE_NAME, tableName));
        databaseQueryEntries.setIsTableExistQuery(databaseQueryEntries.getIsTableExistQuery().
                replace(PersistenceConstants.PLACEHOLDER_TABLE_NAME, tableName));
        databaseQueryEntries.setSelectTableQuery(databaseQueryEntries.getSelectTableQuery().
                replace(PersistenceConstants.PLACEHOLDER_TABLE_NAME, tableName));
        databaseQueryEntries.setSelectLastQuery(databaseQueryEntries.getSelectLastQuery().
                replace(PersistenceConstants.PLACEHOLDER_TABLE_NAME, tableName));
        databaseQueryEntries.setDeleteQuery(databaseQueryEntries.getDeleteQuery().
                replace(PersistenceConstants.PLACEHOLDER_TABLE_NAME, tableName));
        databaseQueryEntries.setCountQuery(databaseQueryEntries.getCountQuery().
                replace(PersistenceConstants.PLACEHOLDER_TABLE_NAME, tableName));

        return databaseQueryEntries;
    }

    /**
     * Method that return the Database Queries from the config XML according the correct Database Type
     * @param databaseType is the type of RDBMS used
     * @return the list of DB Queries
     */
    private RDBMSQueryConfigurationEntry loadDatabaseQueryEntries(String databaseType) {
        try {
            RDBMSQueryConfiguration rdbmsQueryConfiguration = readTableConfigXML();
            for (RDBMSQueryConfigurationEntry databaseQueryEntries : rdbmsQueryConfiguration
                    .getDatabaseQueryEntries()) {
                if (databaseType.equals(databaseQueryEntries.getDatabaseName())) {
                    return databaseQueryEntries;
                }
            }

        } catch (CannotLoadConfigurationException e) {
            log.error("Error loading configuration file " + RDBMS_QUERY_CONFIG_FILE, e);
        }
        return null;
    }

    /**
     * Method that reads the RDBMS Query config file
     * @return All defined Database Queries for all Database Types
     * @throws CannotLoadConfigurationException
     */
    private RDBMSQueryConfiguration readTableConfigXML() throws CannotLoadConfigurationException {
        try {
            JAXBContext ctx = JAXBContext.newInstance(RDBMSQueryConfiguration.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(RDBMS_QUERY_CONFIG_FILE);
            if (inputStream == null) {
                throw new CannotLoadConfigurationException(RDBMS_QUERY_CONFIG_FILE
                        + " is not found in the classpath");
            }
            return (RDBMSQueryConfiguration) unmarshaller.unmarshal(inputStream);
        } catch (JAXBException e) {
            throw new CannotLoadConfigurationException(
                    "Error in processing RDBMS query configuration: " + e.getMessage(), e);
        }
    }
}
