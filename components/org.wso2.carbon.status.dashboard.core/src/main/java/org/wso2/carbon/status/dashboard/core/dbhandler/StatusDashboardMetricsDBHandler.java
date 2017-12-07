/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.status.dashboard.core.dbhandler;

import com.zaxxer.hikari.HikariDataSource;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.status.dashboard.core.bean.table.Attribute;
import org.wso2.carbon.status.dashboard.core.bean.table.ComponentMetrics;
import org.wso2.carbon.status.dashboard.core.bean.table.MetricElement;
import org.wso2.carbon.status.dashboard.core.bean.table.TypeMetrics;
import org.wso2.carbon.status.dashboard.core.dbhandler.exceptions.RDBMSTableException;
import org.wso2.carbon.status.dashboard.core.dbhandler.utils.DBTableUtils;
import org.wso2.carbon.status.dashboard.core.dbhandler.utils.QueryManager;
import org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants;
import org.wso2.carbon.status.dashboard.core.internal.DashboardDataHolder;
import org.wso2.carbon.status.dashboard.core.services.DefaultQueryLoaderService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_AGGREGATION_TIME;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_COLUMNS;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_NAME;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_RESULT;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_TABLE_NAME;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_WORKER_ID;
// TODO: 11/1/17 Constants
/**
 * This class represents key database operations related to metrics data.
 */
public class StatusDashboardMetricsDBHandler {
    private static final Logger logger = LoggerFactory.getLogger(StatusDashboardMetricsDBHandler.class);
    private static final String DATASOURCE_ID = DashboardDataHolder.getMetricsDataSourceName();
    private static final String[] METRICS_TABLE_NAMES = {"METRIC_COUNTER", "METRIC_GAUGE", "METRIC_HISTOGRAM",
            "METRIC_METER", "METRIC_TIMER"};
    private String selectAppMetricsQuery;
    private String selectWorkerMetricsQuery;
    private String selectWorkerAggregatedMetricsQuery;
    private String selectAppComponentList;
    private String selectAppComponentMetrics;
    private String selectWorkerAggregatedThroughputQuery;
    private String selectWorkerThroughputQuery;
    private String selectAppComponentHistory;
    private HikariDataSource dataSource = null;
    private Connection conn;
    private Map<String, Map<String, String>> workerAttributeTypeMap;

    public StatusDashboardMetricsDBHandler() {
        dataSource = DashboardDataHolder.getInstance().getMetricsDataSource();
        if (dataSource != null) {
            this.conn = DBHandler.getInstance().getConnection(dataSource);
            String dbType = DBTableUtils.getInstance().getDBType(this.conn);
            QueryManager.getInstance().readConfigs(dbType);
            workerAttributeTypeMap = DBTableUtils.getInstance().loadMetricsAttributeTypeMap();

            selectAppMetricsQuery = QueryManager.getInstance().getQuery(SQLConstants.SELECT_APP_METRICS_QUERY);
            selectAppMetricsQuery = loadQuery(selectAppMetricsQuery, SQLConstants.SELECT_APP_METRICS_QUERY, dbType);

            selectWorkerMetricsQuery = QueryManager.getInstance().getQuery(SQLConstants.SELECT_WORKER_METRICS_QUERY);
            selectWorkerMetricsQuery = loadQuery(selectWorkerMetricsQuery, SQLConstants.SELECT_WORKER_METRICS_QUERY,
                    dbType);

            selectWorkerThroughputQuery = QueryManager.getInstance().getQuery(SQLConstants.
                    SELECT_WORKER_THROUGHPUT_QUERY);
            selectWorkerThroughputQuery = loadQuery(selectWorkerThroughputQuery, SQLConstants.
                    SELECT_WORKER_THROUGHPUT_QUERY, dbType);

            selectWorkerAggregatedMetricsQuery = QueryManager.getInstance().getQuery(SQLConstants
                    .SELECT_WORKER_AGGREGATE_METRICS_QUERY);
            selectWorkerAggregatedMetricsQuery = loadQuery(selectWorkerAggregatedMetricsQuery, SQLConstants
                            .SELECT_WORKER_AGGREGATE_METRICS_QUERY,
                    dbType);

            selectWorkerAggregatedThroughputQuery = QueryManager.getInstance().getQuery(SQLConstants.
                    SELECT_WORKER_AGGREGATE_THROUGHPUT_QUERY);
            selectWorkerAggregatedThroughputQuery = loadQuery(selectWorkerAggregatedThroughputQuery, SQLConstants.
                    SELECT_WORKER_AGGREGATE_THROUGHPUT_QUERY, dbType);

            selectAppComponentList = QueryManager.getInstance().getQuery(SQLConstants.
                    SELECT_COMPONENT_LIST);
            selectAppComponentList = loadQuery(selectAppComponentList, SQLConstants.
                    SELECT_COMPONENT_LIST, dbType);

            selectAppComponentMetrics = QueryManager.getInstance().getQuery(SQLConstants.
                    SELECT_COMPONENT_METRICS);
            selectAppComponentMetrics = loadQuery(selectAppComponentMetrics, SQLConstants.
                    SELECT_COMPONENT_METRICS, dbType);

            selectAppComponentHistory = QueryManager.getInstance().getQuery(SQLConstants.
                    SELECT_COMPONENT_METRICS_HISTORY);
            selectAppComponentHistory = loadQuery(selectAppComponentHistory, SQLConstants.
                    SELECT_COMPONENT_METRICS_HISTORY, dbType);
        } else {
            logger.warn(DATASOURCE_ID + " Could not find. Hence cannot initialize the status dashboard.");
        }
    }


    /**
     * This will load the database general queries which is in deployment YAML or default queries.
     *
     * @param query  DB query from YAML.
     * @param key    requested query name.
     * @param dbType Database type
     * @return rdbms query.
     */
    private String loadQuery(String query, String key, String dbType) {
        if (query != null) {
            return query;
        } else {
            return DefaultQueryLoaderService.getInstance()
                    .getDashboardDefaultConfigurations().getQueries().get(dbType).get(key);
        }

    }

    /**
     * This resolve the table name in generic tables.
     *
     * @param query loaded queries.
     * @return table name resolving.
     */
    private String resolveTableName(String query, String tableName) {
        return query.replace(PLACEHOLDER_TABLE_NAME, tableName);
    }

    /**
     * Returns a connection instance.
     *
     * @return a new {@link Connection} instance from the datasource.
     */
    private Connection getConnection() {
        try {
            if ((conn != null) && (!conn.isClosed())) {
            } else {
                try {
                    this.conn = this.dataSource.getConnection();
                    this.conn.setAutoCommit(true);
                } catch (SQLException e) {
                    throw new RDBMSTableException("Error reinitializing connection: " + e.getMessage() + " in "
                            + DATASOURCE_ID, e);
                }
            }
        } catch (SQLException e) {
            throw new RDBMSTableException("Error while getting connection ", e);
        }
        return conn;
    }

    /**
     * Select the component History .
     * @param workerId         ID of the worker
     * @param appName          siddhi application name
     * @param timeInterval     time interval that needed to be taken.
     * @param currentTimeMilli current time in milliseconds expression in db type
     * @param metricsType table name to be fetched
     * @return
     */
    public List<List<Object>> selectAppComponentsHistory(String workerId, String appName, long timeInterval, double
    currentTimeMilli, String metricsType, String componentType, String componentId) {
        Map<String, String> typeTableColumn = DBTableUtils.getInstance().loadMetricsTypeSelection();
        String tableName=typeTableColumn.get(metricsType);
        Map<String, String> tableColumn = DBTableUtils.getInstance().loadMetricsAllValueSelection();
        String componentName = "org.wso2.siddhi.SiddhiApps."+appName+".Siddhi."+componentType+"."+componentId+"" +
                "."+metricsType;
        String resolvedSelectWorkerMetricsHistoryQuery = resolveTableName(selectAppComponentHistory, tableName);
        String resolvedQuery =resolvedSelectWorkerMetricsHistoryQuery.replace(SQLConstants.PLACEHOLDER_TIME_INTERVAL,
                String.valueOf(timeInterval)).replace(PLACEHOLDER_NAME, componentName).replace
                (PLACEHOLDER_WORKER_ID, workerId).replace(SQLConstants.PLACEHOLDER_CURRENT_TIME,
                String.valueOf(currentTimeMilli)).replace(PLACEHOLDER_COLUMNS,tableColumn.get(tableName));
        return select(resolvedQuery,tableColumn.get(tableName),tableName);
    }

    /**
     * Select the component list of the siddhi app.
     *
     * @param workerId         ID of the worker
     * @param appName          siddhi application name
     * @param timeInterval     time interval that needed to be taken.
     * @param currentTimeMilli current time in milliseconds expression in db type
     * @return component List
     */
    public Map<String, List<String>> selectAppComponentsList(String workerId, String appName, int timeInterval, double
            currentTimeMilli) {
        Map<String, List<String>> allComponentsNames = new LinkedHashMap<>();
        for (String tableName : METRICS_TABLE_NAMES) {
            List<String> subComponentsList = new ArrayList<>();
            String resolvedSelectWorkerMetricsQuery = resolveTableName(selectAppComponentList, tableName);
            String resolvedQuery = resolvedSelectWorkerMetricsQuery.replace(SQLConstants.PLACEHOLDER_TIME_INTERVAL,
                    String.valueOf(timeInterval)).replace(PLACEHOLDER_NAME, appName).replace
                    (PLACEHOLDER_WORKER_ID, workerId).replace(SQLConstants.PLACEHOLDER_CURRENT_TIME,
                    String.valueOf(currentTimeMilli));
            List<List<Object>> list = select(resolvedQuery, "NAME", tableName);
            if (!list.isEmpty()) {
                for (Object app : list) {
                    subComponentsList.add((String) ((ArrayList) app).get(0));
                }
            }
            for (String componentMetricsName:subComponentsList) {
                String componentName = componentMetricsName.substring(0,componentMetricsName.lastIndexOf("."));
               List<String> existingList = allComponentsNames.get(componentName);
               if(existingList == null){
                   List<String> newList = new ArrayList<>();
                   newList.add(tableName);
                   allComponentsNames.put(componentName,newList);
               } else {
                   existingList.add(tableName);
                   allComponentsNames.put(componentName,existingList);
               }
            }
        }
        return allComponentsNames;
    }

    /**
     * Select the last Metrics value of components in a particular table.
     *
     * @param carbonId   carbon id of a worker.
     * @param components component list of the worker.
     * @return
     */
    public List selectComponentsLastMetric(String carbonId, String appName, Map<String, List<String>> components,long timeInterval, double
            currentTimeMilli) {
        Map<String, String> tableMetricsMap = new HashMap<>();
        for(Map.Entry<String, String> entry : DBTableUtils.getInstance().loadMetricsTypeSelection().entrySet()){
            tableMetricsMap.put(entry.getValue(), entry.getKey());
        }
        Map<String, String> tableColumn = DBTableUtils.getInstance().loadMetricsValueSelection();
        List<TypeMetrics> componentsRecentMetrics = new ArrayList<>();
        MetricElement metricElement = new MetricElement();
        ComponentMetrics componentMetrics = new ComponentMetrics();
        TypeMetrics typeMetrics = new TypeMetrics();
        for (Map.Entry componentEntry : components.entrySet()) {
            List<String> componentsTableList =(List<String>) componentEntry.getValue();
            for (String tableEntry : componentsTableList) {
                String columnListString = tableColumn.get(tableEntry);
                String resolvedSelectWorkerMetricsQuery = resolveTableName(selectAppComponentMetrics, tableEntry);
                String resolvedSelectWorkerRecentMetricsQuery = resolveTableName(selectAppComponentHistory,
                        tableEntry);
                String resolvedQuery = resolvedSelectWorkerMetricsQuery.replace(PLACEHOLDER_NAME,
                        (String) componentEntry.getKey()).replace(PLACEHOLDER_WORKER_ID, carbonId).replace
                        (PLACEHOLDER_COLUMNS,
                        columnListString);
                String resolvedRecentQuery = resolvedSelectWorkerRecentMetricsQuery.replace(PLACEHOLDER_NAME,
                        (String) componentEntry.getKey()).replace
                        (PLACEHOLDER_WORKER_ID, carbonId).replace(PLACEHOLDER_COLUMNS,
                        columnListString).replace(SQLConstants.PLACEHOLDER_TIME_INTERVAL,
                        String.valueOf(timeInterval)).replace(PLACEHOLDER_NAME, appName)
                        .replace(SQLConstants.PLACEHOLDER_CURRENT_TIME,
                                String.valueOf(currentTimeMilli));
                String[] columnList = columnListString.split(",");
                List<List<Object>> selectionRecent = select(resolvedRecentQuery, columnListString, tableEntry);
                List<Object> selection = select(resolvedQuery, columnListString, tableEntry).get(0);
                String[] componentElements = ((String)componentEntry.getKey()).replace("org.wso2.siddhi" +
                        ".SiddhiApps" +
                        "." + appName + ".Siddhi.", "").split("\\.", 2);
                String metricType = tableMetricsMap.get(tableEntry).toLowerCase();
                if ((selection != null) && (!selection.isEmpty())) {
                    Attribute attribute = new Attribute(columnList[1], selection.get(1));
                    attribute.setRecentValues(selectionRecent);
                    metricElement.addAttributes(attribute);
                    metricElement.setType(metricType);
                    componentMetrics.addMetrics(metricElement);
                    metricElement = new MetricElement();
                    componentMetrics.setName(componentElements[1]);
                    typeMetrics.setType(componentElements[0]);
                    typeMetrics.setData(componentMetrics);
                }
            }
            boolean isNew = true;
            for (TypeMetrics typeMetric :componentsRecentMetrics) {
                if (typeMetrics.getType().equalsIgnoreCase(typeMetric.getType())){
                    isNew = false;
                    typeMetric.getData().add(typeMetrics.getData().get(0));
                }
            }
            if(isNew) {
                componentsRecentMetrics.add(typeMetrics);
            }
            componentMetrics = new ComponentMetrics();
            typeMetrics = new TypeMetrics();
        }
        return componentsRecentMetrics;
    }


    /**
     * This method resold the MetricElement query by replacing the values.
     *
     * @param workerId     workerID
     * @param timeInterval timeInterval
     * @param appName      siddhi app name
     * @param currentTime  current time expression.
     * @return selected list of metrics.
     */
    public List selectAppOverallMetrics(String metricsType, String workerId, long
            timeInterval, String appName, long currentTime) {
        switch (metricsType) {
            case "memory": {
                String tableName = "METRIC_GAUGE";
                String columnsOrSelectExpressions = "SUM(CAST(result.VALUE as DECIMAL(22,2)))";
                String resultLabel = "VALUE";
                String resolvedQueryTable = selectAppMetricsQuery.replace(SQLConstants.PLACEHOLDER_COLUMNS,
                        columnsOrSelectExpressions).replace(SQLConstants.PLACEHOLDER_TIME_INTERVAL, String
                        .valueOf(timeInterval)).replace(PLACEHOLDER_NAME, appName).replace
                        (PLACEHOLDER_WORKER_ID, workerId).replace(SQLConstants.PLACEHOLDER_CURRENT_TIME, String.valueOf
                        (currentTime)).replace(PLACEHOLDER_RESULT, resultLabel).replace(SQLConstants.
                                PLACEHOLDER_COLUMNS, columnsOrSelectExpressions)
                        .replace(PLACEHOLDER_TABLE_NAME, tableName);
                return selectAppMemory(resolvedQueryTable, tableName);
            }
            case "throughput": {
                String tableName = "METRIC_METER";
                String columnsLabels = "TIMESTAMP,COUNT";
                String columnsOrSelectExpressions = "SUM(result.COUNT)";
                String resultLabel = "COUNT";
                String resolvedQueryTable = selectAppMetricsQuery.replace(SQLConstants.PLACEHOLDER_COLUMNS,
                        columnsOrSelectExpressions).replace(SQLConstants.PLACEHOLDER_TIME_INTERVAL, String
                        .valueOf(timeInterval)).replace(PLACEHOLDER_NAME, appName).replace
                        (PLACEHOLDER_WORKER_ID, workerId).replace(SQLConstants.PLACEHOLDER_CURRENT_TIME,
                        String.valueOf(currentTime)).replace(PLACEHOLDER_RESULT, resultLabel)
                        .replace(SQLConstants.PLACEHOLDER_COLUMNS, columnsOrSelectExpressions)
                        .replace(PLACEHOLDER_TABLE_NAME, tableName);
                return select(resolvedQueryTable, columnsLabels, tableName);
            }
            case "latency": {
                String tableName = "METRIC_TIMER";
                String columnsLabels = "TIMESTAMP,COUNT";
                String columnsOrSelectExpressions = "SUM(result.COUNT)";
                String resultLabel = "COUNT";
                String resolvedQueryTable = selectAppMetricsQuery.replace(SQLConstants.PLACEHOLDER_COLUMNS,
                        columnsOrSelectExpressions).replace(SQLConstants.PLACEHOLDER_TIME_INTERVAL, String
                        .valueOf(timeInterval)).replace(PLACEHOLDER_NAME, appName).replace
                        (PLACEHOLDER_WORKER_ID, workerId).replace(SQLConstants.PLACEHOLDER_CURRENT_TIME,
                        String.valueOf(currentTime)).replace(PLACEHOLDER_RESULT, resultLabel)
                        .replace(SQLConstants.PLACEHOLDER_COLUMNS,
                        columnsOrSelectExpressions).replace(PLACEHOLDER_TABLE_NAME, tableName);
                return select(resolvedQueryTable, columnsLabels, tableName);
            }
            default: {
                logger.error(getEncodedString("Invalid parameters type: " + workerId + ":" + appName));
                return null;
            }
        }
    }


    /**
     * Used to get the metrics gauges of jvm metrics.
     *
     * @param workerId       source ID of the metrics.
     * @param timeInterval   time interval that needed to be taken.
     * @param metricTypeName metrics type name ex: memory,cpu
     * @param currentTime    current time in milliseconds.
     * @return List<List<Object>> of metrics data because charts needed in that format
     */
    public List selectWorkerMetrics(String workerId, long timeInterval, String metricTypeName, long
            currentTime) {
        String resolvedSelectWorkerMetricsQuery = resolveTableName(selectWorkerMetricsQuery, "METRIC_GAUGE");
        String resolvedQuery = resolvedSelectWorkerMetricsQuery.replace(SQLConstants.PLACEHOLDER_TIME_INTERVAL, String
                .valueOf(timeInterval)).replace(PLACEHOLDER_NAME, metricTypeName).replace
                (PLACEHOLDER_WORKER_ID, workerId).replace(SQLConstants.PLACEHOLDER_CURRENT_TIME, String.valueOf
                (currentTime));
        return selectGauge(resolvedQuery,false);
    }

    /**
     * Used to get the metrics gauges of jvm metrics.
     *
     * @param workerId       source ID of the metrics.
     * @param timeInterval   time interval that needed to be taken.
     * @param metricTypeName metrics type name ex: memory,cpu
     * @param currentTime    current time in milliseconds.
     * @return List<List<Object>> of metrics data because charts needed in that format
     */
    public List selectWorkerAggregatedMetrics(String workerId, long timeInterval, String metricTypeName, long
            currentTime) {
        long aggregationTime = DBTableUtils.getAggregation(timeInterval);
        String resolvedSelectWorkerMetricsQuery = resolveTableName(selectWorkerAggregatedMetricsQuery, "METRIC_GAUGE");
        String resolvedQuery = resolvedSelectWorkerMetricsQuery.replace(SQLConstants.PLACEHOLDER_TIME_INTERVAL, String
                .valueOf(timeInterval)).replace(PLACEHOLDER_NAME, metricTypeName).replace
                (PLACEHOLDER_WORKER_ID, workerId).replace(SQLConstants.PLACEHOLDER_CURRENT_TIME, String.valueOf
                (currentTime)).replace(PLACEHOLDER_AGGREGATION_TIME,String.valueOf(aggregationTime));
        return selectGauge(resolvedQuery,true);
    }

    /**
     * Used to ge the overall throughput of the worker.
     *
     * @param workerId     source id of the metrics
     * @param timeInterval time interval that metrics needed to be taken.
     * @param currentTime  current time
     * @return List<List<Object>> of metrics data because charts needed in that format
     */
    public List selectWorkerThroughput(String workerId, long timeInterval, long currentTime) {
        String resolvedSelectWorkerThroughputQuery = resolveTableName(selectWorkerThroughputQuery,
                "METRIC_METER");
        String resolvedQuery = resolvedSelectWorkerThroughputQuery.replace(SQLConstants.PLACEHOLDER_COLUMNS,
                "SUM(result.COUNT)").replace
                (SQLConstants.PLACEHOLDER_TIME_INTERVAL, String.valueOf(timeInterval)).replace
                (PLACEHOLDER_WORKER_ID, workerId).replace(SQLConstants.PLACEHOLDER_CURRENT_TIME,
                String.valueOf(currentTime)).replace(PLACEHOLDER_RESULT, "COUNT");
        return select(resolvedQuery, "TIMESTAMP,COUNT", "METRIC_METER");
    }

    /**
     * Used to ge the overall throughput of the worker.
     *
     * @param workerId     source id of the metrics
     * @param timeInterval time interval that metrics needed to be taken.
     * @param currentTime  current time
     * @return List<List<Object>> of metrics data because charts needed in that format
     */
    public List selectWorkerAggregatedThroughput(String workerId, long timeInterval, long currentTime) {
        long aggregationTime = DBTableUtils.getAggregation(timeInterval);
        String resolvedSelectWorkerThroughputQuery = resolveTableName(selectWorkerAggregatedThroughputQuery,
                "METRIC_METER");
        String resolvedQuery = resolvedSelectWorkerThroughputQuery.replace(SQLConstants.PLACEHOLDER_COLUMNS,
                "SUM(result.COUNT)").replace
                (SQLConstants.PLACEHOLDER_TIME_INTERVAL, String.valueOf(timeInterval)).replace
                (PLACEHOLDER_WORKER_ID, workerId).replace(SQLConstants.PLACEHOLDER_CURRENT_TIME,
                String.valueOf(currentTime)).replace(PLACEHOLDER_RESULT, "COUNT").
                replace(PLACEHOLDER_AGGREGATION_TIME,String.valueOf(aggregationTime));;
        return select(resolvedQuery, "AGG_TIMESTAMP,COUNT", "METRIC_METER");
    }

    /**
     * Select the metrics of the siddhi app.
     *
     * @param query   selection query.
     * @return the selected object.
     */
    private List<List<Object>> selectAppMemory(String query, String tableName) {
        Map<String, String> attributesTypeMap = workerAttributeTypeMap.get(tableName);
        Connection conn = this.getConnection();
        ResultSet rs = null;
        List<List<Object>> tuple = new ArrayList<>();
        List<Object> row;
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            rs = DBHandler.getInstance().select(stmt);
            while (rs.next()) {
                row = new ArrayList<>();
                row.add(DBTableUtils.getInstance().fetchData(rs, "TIMESTAMP", attributesTypeMap.get
                        ("TIMESTAMP")));
                row.add(Double.valueOf((String) DBTableUtils.getInstance().fetchData(rs, "VALUE",
                        attributesTypeMap.get
                        ("VALUE"))));
                tuple.add(row);
            }
        } catch (SQLException e) {
            throw new RDBMSTableException("Error retrieving records from table '" + tableName + "': "
                    + e.getMessage() + " in " + DATASOURCE_ID, e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                //ignore
            }
        }
        return tuple;
    }
    /**
     * Select the metrics.
     *
     * @param query   selection query.
     * @param columns column labeles that needed to select.
     * @return the selected object.
     */
    private List<List<Object>> select(String query, String columns, String tableName) {
        Map<String, String> attributesTypeMap = workerAttributeTypeMap.get(tableName);
        Connection conn = this.getConnection();
        ResultSet rs = null;
        List<List<Object>> tuple = new ArrayList<>();
        List<Object> row;
        PreparedStatement stmt = null;
        String[] columnLabels = columns.split(",");
        try {
            stmt = conn.prepareStatement(query);
            rs = DBHandler.getInstance().select(stmt);
            while (rs.next()) {
                row = new ArrayList<>();
                for (String columnLabel : columnLabels) {
                    if(columnLabel.equalsIgnoreCase("VALUE")) {
                        row.add(Double.valueOf((String) DBTableUtils.getInstance().fetchData(rs, columnLabel, attributesTypeMap.get
                                (columnLabel))));
                    } else {
                        row.add(DBTableUtils.getInstance().fetchData(rs, columnLabel, attributesTypeMap.get
                                (columnLabel)));
                    }
                }
                tuple.add(row);
            }
        } catch (SQLException e) {
            throw new RDBMSTableException("Error retrieving records from table '" + tableName + "': "
                    + e.getMessage() + " in " + DATASOURCE_ID, e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                //ignore
            }
        }
        return tuple;
    }

    /**
     * Select the metrics.
     *
     * @param query selection query.
     * @return the selected object.
     */
    private List<List<Object>> selectGauge(String query,boolean isAggregated) {
        Map<String, String> attributesTypeMap = workerAttributeTypeMap.get("METRIC_GAUGE");
        Connection conn = this.getConnection();
        ResultSet rs = null;
        List<List<Object>> tuple = new ArrayList<>();
        PreparedStatement stmt = null;
        List<Object> row;
        try {
             stmt = conn.prepareStatement(query);
            rs = DBHandler.getInstance().select(stmt);
            String timestampCol="TIMESTAMP";
            if(isAggregated){
               timestampCol = "AGG_TIMESTAMP";
            }
            while (rs.next()) {
                row = new ArrayList<>();
                row.add(DBTableUtils.getInstance().fetchData(rs, timestampCol, attributesTypeMap.get
                        ("TIMESTAMP")));
                row.add(Double.valueOf((String) DBTableUtils.getInstance().fetchData(rs, "VALUE",
                        attributesTypeMap.get
                        ("VALUE"))));
                tuple.add(row);
            }

        } catch (SQLException e) {
            throw new RDBMSTableException("Error retrieving records from table '" + "METRIC_GAUGE" + "': "
                    + e.getMessage() + " in " + DATASOURCE_ID, e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                //ignore
            }
        }
        return tuple;
    }

    /**
     * clean up the database connection.
     */
    public void cleanupConnection() {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignore) { /* ignore */ }
        }
    }

    private static String getEncodedString(String str) {
        String cleanedString = str.replace('\n', '_').replace('\r', '_');
        cleanedString = Encode.forHtml(cleanedString);
        if (!cleanedString.equals(str)) {
            cleanedString += " (Encoded)";
        }
        return cleanedString;
    }
}
