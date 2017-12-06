/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.stream.processor.core;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.datasource.core.impl.DataSourceServiceImpl;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.persistence.DBPersistenceStore;
import org.wso2.carbon.stream.processor.core.persistence.exception.DatasourceConfigurationException;
import org.wso2.carbon.stream.processor.core.persistence.util.PersistenceConstants;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(StreamProcessorDataHolder.class)
public class DBPersistenceStoreTest extends PowerMockTestCase {

    @BeforeTest
    public void setDebugLogLevel() {
        Logger.getLogger(DBPersistenceStore.class.getName()).setLevel(Level.DEBUG);
    }

    @Test(expectedExceptions = DatasourceConfigurationException.class)
    public void testDataSourceConfigurationException() {

        mockStatic(StreamProcessorDataHolder.class);
        when(StreamProcessorDataHolder.getDataSourceService()).thenReturn(new DataSourceServiceImpl());

        DBPersistenceStore dbPersistenceStore = new DBPersistenceStore();
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> config = new HashMap<>();
        config.put("datasource", "MYSQL");
        config.put("table", "PERSISTENCE_TABLE");
        properties.put(PersistenceConstants.STATE_PERSISTENCE_REVISIONS_TO_KEEP, 2);
        properties.put(PersistenceConstants.STATE_PERSISTENCE_CONFIGS, config);

        dbPersistenceStore.setProperties(properties);
    }

    @Test(expectedExceptions = DatasourceConfigurationException.class)
    public void testConfigDefaultValues() throws DataSourceException, SQLException {

        mockStatic(StreamProcessorDataHolder.class);
        DataSourceServiceImpl dataSourceService = mock(DataSourceServiceImpl.class);
        HikariDataSource hikariDataSource = mock(HikariDataSource.class);
        when(StreamProcessorDataHolder.getDataSourceService()).thenReturn(dataSourceService);
        when(dataSourceService.getDataSource(any())).thenReturn(hikariDataSource);
        when(hikariDataSource.getConnection()).thenThrow(SQLException.class);

        DBPersistenceStore dbPersistenceStore = new DBPersistenceStore();
        Map<String, Object> properties = new HashMap<>();
        dbPersistenceStore.setProperties(properties);
    }

    @Test(expectedExceptions = DatasourceConfigurationException.class)
    public void testDataSourceTableDefaultValues() throws DataSourceException, SQLException {

        mockStatic(StreamProcessorDataHolder.class);
        DataSourceServiceImpl dataSourceService = mock(DataSourceServiceImpl.class);
        HikariDataSource hikariDataSource = mock(HikariDataSource.class);
        when(StreamProcessorDataHolder.getDataSourceService()).thenReturn(dataSourceService);
        when(dataSourceService.getDataSource(any())).thenReturn(hikariDataSource);
        when(hikariDataSource.getConnection()).thenThrow(SQLException.class);

        DBPersistenceStore dbPersistenceStore = new DBPersistenceStore();
        Map<String, Object> properties = new HashMap<>();
        properties.put(PersistenceConstants.STATE_PERSISTENCE_REVISIONS_TO_KEEP, 2);
        properties.put(PersistenceConstants.STATE_PERSISTENCE_CONFIGS, new HashMap<>());
        dbPersistenceStore.setProperties(properties);
    }
}
