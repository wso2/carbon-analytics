/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.data.provider.rdbms;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.data.provider.DataProvider;
import org.wso2.carbon.data.provider.bean.DataSetMetadata;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.wso2.carbon.data.provider.rdbms.utils.RDBMSProviderConstants.LAST_RECORD_VALUE_PLACEHOLDER;

/**
 * RDBMS streaming data provider instance.
 */
@Component(
        service = DataProvider.class,
        immediate = true
)
public class RDBMSStreamingDataProvider extends AbstractRDBMSDataProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(RDBMSStreamingDataProvider.class);
    private double lastRecordValue = 0;

    @Override
    public void publish(String topic, String sessionId) {
        String customQuery = getRecordLimitQuery();
        DataSetMetadata metadata = getMetadata();
        int columnCount = getColumnCount();
        if (customQuery != null) {
            Connection connection;
            try {
                connection = getConnection(getRdbmsProviderConfig().getDatasourceName());
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                try {
                    if (lastRecordValue > 0) {
                        String greaterThanWhereQuery = getGreaterThanWhereSQLQuery().replace
                                (LAST_RECORD_VALUE_PLACEHOLDER,
                                        Double.toString(lastRecordValue));
                        statement = connection.prepareStatement(greaterThanWhereQuery);
                    } else {
                        statement = connection.prepareStatement(customQuery);
                    }
                    resultSet = statement.executeQuery();
                    ArrayList<Object[]> data = new ArrayList<>();
                    while (resultSet.next()) {
                        Object[] rowData = new Object[columnCount];
                        for (int i = 0; i < columnCount; i++) {
                            if (metadata.getTypes()[i].equals(DataSetMetadata.Types.LINEAR)) {
                                rowData[i] = resultSet.getDouble(i + 1);
                            } else if (metadata.getTypes()[i].equals(DataSetMetadata.Types.ORDINAL)) {
                                rowData[i] = resultSet.getString(i + 1);
                            } else if (metadata.getTypes()[i].equals(DataSetMetadata.Types.TIME)) {
                                rowData[i] = resultSet.getTimestamp(i + 1);
                            } else {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("Meta Data type not defined, added value of the given column as a " +
                                            "java object.");
                                }
                                rowData[i] = resultSet.getObject(i + 1);
                            }
                            if (metadata.getNames()[i].equalsIgnoreCase(getRdbmsProviderConfig()
                                    .getIncrementalColumn()) &&
                                    !metadata.getTypes()[i].equals(DataSetMetadata.Types.TIME)) {
                                if (lastRecordValue < (double) rowData[i]) {
                                    lastRecordValue = (double) rowData[i];
                                }
                            }
                        }
                        data.add(rowData);
                    }
                    if (!data.isEmpty() || lastRecordValue == 0) {
                        publishToEndPoint(data, sessionId, topic);
                    }
                } catch (SQLException e) {
                    LOGGER.error("SQL exception occurred " + e.getMessage(), e);
                } finally {
                    cleanupConnection(resultSet, statement, connection);
                }
            } catch (SQLException | DataSourceException e) {
                LOGGER.error("Failed to create a connection to the database " + e.getMessage(), e);
            }
        }
    }
}
