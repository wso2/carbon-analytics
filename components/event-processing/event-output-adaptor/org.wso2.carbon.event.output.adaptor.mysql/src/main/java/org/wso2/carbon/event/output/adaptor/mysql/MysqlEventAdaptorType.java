/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.output.adaptor.mysql;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.mysql.internal.TableInfo;
import org.wso2.carbon.event.output.adaptor.mysql.internal.ds.EventAdaptorValueHolder;
import org.wso2.carbon.event.output.adaptor.mysql.internal.util.MysqlEventAdaptorConstants;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public final class MysqlEventAdaptorType extends AbstractOutputEventAdaptor {

    private static final Log log = LogFactory.getLog(MysqlEventAdaptorType.class);

    private static MysqlEventAdaptorType mysqlEventAdaptor = new MysqlEventAdaptorType();
    private ResourceBundle resourceBundle;

    private ConcurrentHashMap<OutputEventAdaptorConfiguration, ConcurrentHashMap<String, TableInfo>> tables;
    private ConcurrentHashMap<OutputEventAdaptorConfiguration, DataSource> pooledDataSources;

    private MysqlEventAdaptorType() {
        this.tables = new ConcurrentHashMap<OutputEventAdaptorConfiguration, ConcurrentHashMap<String, TableInfo>>(32);
        this.pooledDataSources = new ConcurrentHashMap<OutputEventAdaptorConfiguration, DataSource>(32);
    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.MAP);
        return supportOutputMessageTypes;
    }

    /**
     * @return mysql event adaptor instance
     */
    public static MysqlEventAdaptorType getInstance() {
        return mysqlEventAdaptor;
    }

    /**
     * @return name of the mysql event adaptor
     */
    @Override
    protected String getName() {
        return MysqlEventAdaptorConstants.ADAPTOR_TYPE_MYSQL;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adaptor.mysql.i18n.Resources", Locale.getDefault());
    }


    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {
        List<Property> propertyList = new ArrayList<Property>();
        Property datasourceName = new Property(MysqlEventAdaptorConstants.ADAPTOR_MYSQL_DATASOURCE_NAME);
        datasourceName.setDisplayName(resourceBundle.getString(MysqlEventAdaptorConstants.ADAPTOR_MYSQL_DATASOURCE_NAME));
        datasourceName.setRequired(true);
        propertyList.add(datasourceName);
        return propertyList;
    }

    /**
     * @return output message configuration property list
     */
    @Override
    public List<Property> getOutputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        Property databaseName = new Property(MysqlEventAdaptorConstants.ADAPTOR_MYSQL_DATABASE_NAME);
        databaseName.setDisplayName(resourceBundle.getString(MysqlEventAdaptorConstants.ADAPTOR_MYSQL_DATABASE_NAME));
        databaseName.setRequired(true);
        databaseName.setHint(resourceBundle.getString(MysqlEventAdaptorConstants.ADAPTOR_MYSQL_DATABASE_NAME_HINT));
        propertyList.add(databaseName);

        Property tableName = new Property(MysqlEventAdaptorConstants.ADAPTOR_MYSQL_TABLE_NAME);
        tableName.setDisplayName(resourceBundle.getString(MysqlEventAdaptorConstants.ADAPTOR_MYSQL_TABLE_NAME));
        tableName.setRequired(true);
        propertyList.add(tableName);

        return propertyList;
    }

    /**
     * @param outputEventMessageConfiguration
     *                - topic name to publish messages
     * @param message - is and Object[]{Event, EventDefinition}
     * @param outputEventAdaptorConfiguration
     * @param tenantId
     */
    public void publish(
            OutputEventAdaptorMessageConfiguration outputEventMessageConfiguration,
            Object message,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            if (message instanceof Map) {
                String databaseName = outputEventMessageConfiguration.getOutputMessageProperties().get(MysqlEventAdaptorConstants.ADAPTOR_MYSQL_DATABASE_NAME);
                String tableName = outputEventMessageConfiguration.getOutputMessageProperties().get(MysqlEventAdaptorConstants.ADAPTOR_MYSQL_TABLE_NAME);

                String fullTableName = databaseName + "." + tableName;
                ConcurrentHashMap<String, TableInfo> tableInfoMap = tables.get(outputEventMessageConfiguration);

                TableInfo tableInfo;
                if (tableInfoMap == null || tableInfoMap.get(fullTableName) == null) {
                    tableInfo = initializeDatabaseTableInfo(databaseName, tableName, message, outputEventAdaptorConfiguration);
                    if (tableInfoMap == null) {
                        tableInfoMap = new ConcurrentHashMap<String, TableInfo>();
                        tables.put(outputEventAdaptorConfiguration, tableInfoMap);
                    }
                    if (tableInfo != null) {
                        tableInfoMap.put(fullTableName, tableInfo);
                    } else {
                        throw new OutputEventAdaptorEventProcessingException("Unable to initialize the table.");
                    }
                } else {
                    tableInfo = tableInfoMap.get(fullTableName);
                }


                con = pooledDataSources.get(outputEventAdaptorConfiguration).getConnection();
                stmt = con.prepareStatement(tableInfo.getPreparedInsertStatement());

                Map<String, Object> map = (Map<String, Object>) message;
                Attribute attribute;
                for (int i = 0; i < tableInfo.getColumnOrder().size(); i++) {
                    attribute = tableInfo.getColumnOrder().get(i);
                    Object value = map.get(attribute.getName());
                    if (value != null) {
                        switch (attribute.getType()) {
                            case INT:
                                stmt.setInt(i+1, (Integer) value);
                                break;
                            case LONG:
                                stmt.setLong(i+1, (Long) value);
                                break;
                            case FLOAT:
                                stmt.setFloat(i+1, (Float) value);
                                break;
                            case DOUBLE:
                                stmt.setDouble(i + 1, (Double) value);
                                break;
                            case STRING:
                                stmt.setString(i+1, (String) value);
                                break;
                            case BOOL:
                                stmt.setBoolean(i+1, (Boolean) value);
                                break;
                        }
                    }
                }
                stmt.execute();
            }
        } catch (SQLException e) {
            log.error(e);
        } finally {
            cleanupConnections(stmt, con);
        }
    }


    private void cleanupConnections(Statement stmt, Connection con) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.error("unable to close statement", e);
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error("unable to close connection", e);
            }
        }
    }


    private TableInfo initializeDatabaseTableInfo(String databaseName, String tableName, Object message,
                                                  OutputEventAdaptorConfiguration adaptorConfig) throws SQLException {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName(tableName);

        Connection con = null;
        Statement stmt = null;

        try {
            // create the table.
            StringBuilder statementBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
            statementBuilder.append(databaseName + "." + tableName);
            statementBuilder.append(" (");
            boolean appendComma = false;
            for (Map.Entry<String, Object> entry : (((Map<String, Object>) message).entrySet())) {
                if (appendComma) {
                    statementBuilder.append(",");
                } else {
                    appendComma = true;
                }
                statementBuilder.append(entry.getKey()).append("  ");
                if (entry.getValue() instanceof Integer) {
                    statementBuilder.append("INT");
                } else if (entry.getValue() instanceof Long) {
                    statementBuilder.append("BIGINT");
                } else if (entry.getValue() instanceof Float) {
                    statementBuilder.append("FLOAT");
                } else if (entry.getValue() instanceof Double) {
                    statementBuilder.append("DOUBLE");
                } else if (entry.getValue() instanceof String) {
                    statementBuilder.append("VARCHAR(255)");
                } else if (entry.getValue() instanceof Boolean) {
                    statementBuilder.append("BOOL");
                }
            }
            statementBuilder.append(")");

            DataSource dataSource = pooledDataSources.get(adaptorConfig.getOutputProperties().get(MysqlEventAdaptorConstants.ADAPTOR_MYSQL_DATASOURCE_NAME));
            if (dataSource == null) {
                CarbonDataSource carbonDataSource = EventAdaptorValueHolder.getDataSourceService().getDataSource(adaptorConfig.getOutputProperties().get(MysqlEventAdaptorConstants.ADAPTOR_MYSQL_DATASOURCE_NAME));
                if(carbonDataSource == null){
                    throw new OutputEventAdaptorEventProcessingException("There is no any data-source found called : "+adaptorConfig.getOutputProperties().get(MysqlEventAdaptorConstants.ADAPTOR_MYSQL_DATASOURCE_NAME));
                }else{
                    dataSource = (DataSource) carbonDataSource.getDSObject();
                    pooledDataSources.put(adaptorConfig, dataSource);
                }
            }

            con = dataSource.getConnection();
            stmt = con.createStatement();
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + databaseName);
            stmt.executeUpdate(statementBuilder.toString());

            ArrayList<Attribute> tableColumnList = new ArrayList<Attribute>();
            statementBuilder = new StringBuilder("INSERT INTO ");
            statementBuilder.append(databaseName + "." + tableName);
            statementBuilder.append(" ( ");

            StringBuilder valuePositionsBuilder = new StringBuilder("");

            appendComma = false;
            DatabaseMetaData databaseMetaData = con.getMetaData();
            ResultSet rs = databaseMetaData.getColumns(databaseName, null, tableName, null);
            while (rs.next()) {
                AttributeType type = null;
                int colType = rs.getInt("DATA_TYPE");
                switch (colType) {
                    case Types.VARCHAR:
                        type = AttributeType.STRING;
                        break;
                    case Types.INTEGER:
                        type = AttributeType.INT;
                        break;
                    case Types.BIGINT:
                        type = AttributeType.LONG;
                        break;
                    case Types.DOUBLE:
                        type = AttributeType.DOUBLE;
                        break;
                    case Types.FLOAT:
                        type = AttributeType.FLOAT;
                        break;
                    case Types.BOOLEAN:
                        type = AttributeType.BOOL;
                        break;

                }
                Attribute attribute = new Attribute(rs.getString("COLUMN_NAME"), type);
                tableColumnList.add(attribute);

                if (appendComma) {
                    statementBuilder.append(",");
                    valuePositionsBuilder.append(",");
                } else {
                    appendComma = true;
                }
                statementBuilder.append(attribute.getName());
                valuePositionsBuilder.append("?");
            }
            statementBuilder.append(") VALUES (");
            statementBuilder.append(valuePositionsBuilder.toString());
            statementBuilder.append(")");
            tableInfo.setPreparedInsertStatement(statementBuilder.toString());
            tableInfo.setColumnOrder(tableColumnList);
            return tableInfo;
        } catch (SQLException e) {
            pooledDataSources.remove(adaptorConfig.getOutputProperties().get(MysqlEventAdaptorConstants.ADAPTOR_MYSQL_DATASOURCE_NAME));
            log.error("error while initializing the table", e);
        } catch (DataSourceException e) {
            pooledDataSources.remove(adaptorConfig.getOutputProperties().get(MysqlEventAdaptorConstants.ADAPTOR_MYSQL_DATASOURCE_NAME));
            log.error("error while accessing the datasource:", e);
        } finally {
            cleanupConnections(stmt, con);
        }
        return null;
    }

    @Override
    public void testConnection(OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        try {
            DataSource dataSource = null;
            CarbonDataSource carbonDataSource = EventAdaptorValueHolder.getDataSourceService().getDataSource(outputEventAdaptorConfiguration.getOutputProperties().get(MysqlEventAdaptorConstants.ADAPTOR_MYSQL_DATASOURCE_NAME));
            if(carbonDataSource != null){
                dataSource = (DataSource) carbonDataSource.getDSObject();
                Connection conn = dataSource.getConnection();
                conn.close();
            } else{
                throw new OutputEventAdaptorEventProcessingException("There is no any datsource found to connect.");
            }
        } catch (Exception e) {
            throw new OutputEventAdaptorEventProcessingException(e);
        }

    }


}

