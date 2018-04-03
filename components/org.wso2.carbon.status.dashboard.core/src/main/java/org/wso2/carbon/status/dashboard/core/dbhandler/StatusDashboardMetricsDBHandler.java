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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.database.query.manager.exception.QueryMappingNotAvailableException;
import org.wso2.carbon.status.dashboard.core.bean.table.Attribute;
import org.wso2.carbon.status.dashboard.core.bean.table.ComponentMetrics;
import org.wso2.carbon.status.dashboard.core.bean.table.MetricElement;
import org.wso2.carbon.status.dashboard.core.bean.table.TypeMetrics;
import org.wso2.carbon.status.dashboard.core.dbhandler.utils.DBTableUtils;
import org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants;
import org.wso2.carbon.status.dashboard.core.exception.RDBMSTableException;
import org.wso2.carbon.status.dashboard.core.exception.StatusDashboardRuntimeException;
import org.wso2.carbon.status.dashboard.core.internal.MonitoringDataHolder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PERCENTAGE_MARK;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_AGGREGATION_COMPONENT_COLOUM;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_AGGREGATION_TIME;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_COLUMNS;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_NAME;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_RESULT;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_TABLE_NAME;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_WORKER_ID;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.QUESTION_MARK;

/**
 * This class represents key database operations related to metrics data.
 */
public class StatusDashboardMetricsDBHandler {
    private static final Logger logger = LoggerFactory.getLogger(StatusDashboardMetricsDBHandler.class);
    private static final String DATASOURCE_ID = MonitoringDataHolder.getInstance().getStatusDashboardDeploymentConfigs()
            .getMetricsDatasourceName();
    private static final String[] METRICS_TABLE_NAMES = {"METRIC_COUNTER", "METRIC_GAUGE", "METRIC_HISTOGRAM",
            "METRIC_METER", "METRIC_TIMER"};
    private static final String APP_NAME_PREFIX = "org.wso2.siddhi.SiddhiApps.";
    private String selectAppMetricsQuery;
    private String recordSelectAgregatedAppMetricsQuery;
    private String selectWorkerMetricsQuery;
    private String selectWorkerAggregatedMetricsQuery;
    private String selectAppComponentList;
    private String selectAppComponentMetrics;
    private String selectWorkerAggregatedThroughputQuery;
    private String selectWorkerThroughputQuery;
    private String selectAppComponentHistory;
    private String selectAppComponentAggregatedHistory;
    private HikariDataSource dataSource = null;
    private Map<String, Map<String, String>> workerAttributeTypeMap;
    private QueryManager metricsQueryManager;

    public StatusDashboardMetricsDBHandler() {
        Connection conn = null;
        dataSource = MonitoringDataHolder.getInstance().getMetricsDataSource();
        if (dataSource != null) {
            try {
                conn = MonitoringDataHolder.getInstance().getMetricsDataSource().getConnection();
                DatabaseMetaData databaseMetaData = conn.getMetaData();
                metricsQueryManager = new QueryManager(databaseMetaData.getDatabaseProductName(),
                        databaseMetaData.getDatabaseProductVersion());
                workerAttributeTypeMap = DBTableUtils.getInstance().loadMetricsAttributeTypeMap(metricsQueryManager);
                selectAppMetricsQuery = metricsQueryManager.getQuery(SQLConstants.SELECT_APP_METRICS_QUERY);
                selectWorkerMetricsQuery = metricsQueryManager.getQuery(SQLConstants.SELECT_WORKER_METRICS_QUERY);
                selectWorkerThroughputQuery = metricsQueryManager.getQuery(SQLConstants.
                        SELECT_WORKER_THROUGHPUT_QUERY);
                selectWorkerAggregatedMetricsQuery = metricsQueryManager.getQuery(SQLConstants
                        .SELECT_WORKER_AGGREGATE_METRICS_QUERY);
                selectWorkerAggregatedThroughputQuery = metricsQueryManager.getQuery(SQLConstants.
                        SELECT_WORKER_AGGREGATE_THROUGHPUT_QUERY);
                selectAppComponentList = metricsQueryManager.getQuery(SQLConstants.
                        SELECT_COMPONENT_LIST);
                selectAppComponentMetrics = metricsQueryManager.getQuery(SQLConstants.
                        SELECT_COMPONENT_METRICS);
                selectAppComponentHistory = metricsQueryManager.getQuery(SQLConstants.
                        SELECT_COMPONENT_METRICS_HISTORY);
                recordSelectAgregatedAppMetricsQuery = metricsQueryManager.getQuery(SQLConstants.
                        SELECT_APP_AGG_METRICS_HISTORY);
                selectAppComponentAggregatedHistory = metricsQueryManager.getQuery(SQLConstants.
                        SELECT_COMPONENT_AGG_METRICS_HISTORY);
            } catch (SQLException | ConfigurationException | IOException | QueryMappingNotAvailableException e) {
                throw new StatusDashboardRuntimeException("Error initializing connection. ", e);
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        logger.warn("Database error. Could not close database connection", e);
                    }
                }
            }
        } else {
            logger.warn(DATASOURCE_ID + " Could not find. Hence cannot initialize the status dashboard.");
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
        return DBHandler.getInstance().getConnection(dataSource);
    }

    /**
     * Method which can be used to clear up and ephemeral SQL connectivity artifacts.
     *
     * @param conn {@link Connection} instance (can be null)
     */
    public static void cleanupConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                if (logger.isDebugEnabled()) {
                    logger.debug("Closed Connection  in Metrics DB");
                }
            } catch (SQLException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Error closing Connection in metrics DB : " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Select the component History .
     *
     * @param workerId         ID of the worker
     * @param appName          siddhi application name
     * @param timeInterval     time interval that needed to be taken.
     * @param currentTimeMilli current time in milliseconds expression in db type
     * @param metricsType      table name to be fetched
     * @return
     */
    public List<List<Object>> selectAppComponentsHistory(String workerId, String appName, long timeInterval, long
            currentTimeMilli, String metricsType, String componentType, String componentId) {
        Map<String, String> typeTableColumn = DBTableUtils.getInstance().loadMetricsTypeSelection();
        String tableName = typeTableColumn.get(metricsType);
        Map<String, String> tableColumn = DBTableUtils.getInstance().loadMetricsAllValueSelection();
        String componentName = APP_NAME_PREFIX + appName + ".Siddhi." + componentType + "." +
                componentId + "" + "." + metricsType;
        String resolvedSelectWorkerMetricsHistoryQuery = resolveTableName(selectAppComponentHistory, tableName);
        String resolvedQuery = resolvedSelectWorkerMetricsHistoryQuery.replace(SQLConstants.PLACEHOLDER_BEGIN_TIME,
                QUESTION_MARK).replace(PLACEHOLDER_NAME, QUESTION_MARK).replace
                (PLACEHOLDER_WORKER_ID, QUESTION_MARK).replace(SQLConstants.PLACEHOLDER_CURRENT_TIME,
                QUESTION_MARK).replace(PLACEHOLDER_COLUMNS, tableColumn.get(tableName));
        Object[] parameters = new Object[]{workerId, componentName + PERCENTAGE_MARK,
                currentTimeMilli - timeInterval, currentTimeMilli};
        return select(resolvedQuery, tableColumn.get(tableName), tableName, parameters);
    }

    /**
     * Select the component History .
     *
     * @param workerId         ID of the worker
     * @param appName          siddhi application name
     * @param timeInterval     time interval that needed to be taken.
     * @param currentTimeMilli current time in milliseconds expression in db type
     * @param metricsType      table name to be fetched
     * @return
     */
    public List<List<Object>> selectAppComponentsAggHistory(String workerId, String appName, long timeInterval, long
            currentTimeMilli, String metricsType, String componentType, String componentId) {
        long aggregationTime = DBTableUtils.getAggregation(timeInterval);
        Map<String, String> typeTableColumn = DBTableUtils.getInstance().loadMetricsTypeSelection();
        String tableName = typeTableColumn.get(metricsType);
        Map<String, String> tableAggColumn = DBTableUtils.getInstance().loadAggMetricsAllValueSelection();
        Map<String, String> tableColumn = DBTableUtils.getInstance().loadAggRowMetricsAllValueSelection();
        String componentName = "org.wso2.siddhi.SiddhiApps." + appName + ".Siddhi." + componentType + "." + componentId + "" +
                "." + metricsType;
        String resolvedSelectWorkerMetricsHistoryQuery = resolveTableName(selectAppComponentAggregatedHistory, tableName);
        String resolvedQuery = resolvedSelectWorkerMetricsHistoryQuery.replace(SQLConstants.PLACEHOLDER_BEGIN_TIME,
                QUESTION_MARK).replace(PLACEHOLDER_NAME, QUESTION_MARK).replace
                (PLACEHOLDER_WORKER_ID, QUESTION_MARK).replace(SQLConstants.PLACEHOLDER_CURRENT_TIME,
                QUESTION_MARK)
                .replace(PLACEHOLDER_AGGREGATION_TIME, Long.toString(aggregationTime))
                .replace(PLACEHOLDER_AGGREGATION_COMPONENT_COLOUM, tableAggColumn.get(tableName));
        Object[] parameters = new Object[]{workerId, componentName + PERCENTAGE_MARK,
                currentTimeMilli - timeInterval, currentTimeMilli};
        return select(resolvedQuery, tableColumn.get(tableName), tableName, parameters);
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
    public Map<String, List<String>> selectAppComponentsList(String workerId, String appName, int timeInterval, long
            currentTimeMilli) {
        Map<String, List<String>> allComponentsNames = new LinkedHashMap<>();
        for (String tableName : METRICS_TABLE_NAMES) {
            List<String> subComponentsList = new ArrayList<>();
            String resolvedSelectWorkerMetricsQuery = resolveTableName(selectAppComponentList, tableName);
            String resolvedQuery = resolvedSelectWorkerMetricsQuery.replace(SQLConstants.PLACEHOLDER_BEGIN_TIME,
                    QUESTION_MARK).replace(PLACEHOLDER_NAME, QUESTION_MARK).replace
                    (PLACEHOLDER_WORKER_ID, QUESTION_MARK).replace(SQLConstants.PLACEHOLDER_CURRENT_TIME,
                    QUESTION_MARK);
            Object[] parameters = new Object[]{workerId, APP_NAME_PREFIX + appName + "." + PERCENTAGE_MARK,
                    currentTimeMilli - timeInterval, currentTimeMilli};
            List<List<Object>> list = select(resolvedQuery, "NAME", tableName, parameters);
            if (!list.isEmpty()) {
                for (Object app : list) {
                    subComponentsList.add((String) ((ArrayList) app).get(0));
                }
            }
            for (String componentMetricsName : subComponentsList) {
                String componentName = componentMetricsName.substring(0, componentMetricsName.lastIndexOf("."));
                List<String> existingList = allComponentsNames.get(componentName);
                if (existingList == null) {
                    List<String> newList = new ArrayList<>();
                    newList.add(tableName);
                    allComponentsNames.put(componentName, newList);
                } else {
                    existingList.add(tableName);
                    allComponentsNames.put(componentName, existingList);
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
    public List selectComponentsLastMetric(String carbonId, String appName, Map<String, List<String>> components,
                                           long timeInterval, long currentTimeMilli) {
        Map<String, String> tableMetricsMap = new HashMap<>();
        for (Map.Entry<String, String> entry : DBTableUtils.getInstance().loadMetricsTypeSelection().entrySet()) {
            tableMetricsMap.put(entry.getValue(), entry.getKey());
        }
        Map<String, String> tableMetricsUnitsMap = DBTableUtils.getInstance().loadMetricsUnitsSelection();
        Map<String, String> tableColumn = DBTableUtils.getInstance().loadMetricsValueSelection();
        List<TypeMetrics> componentsRecentMetrics = new ArrayList<>();
        MetricElement metricElement = new MetricElement();
        ComponentMetrics componentMetrics = new ComponentMetrics();
        TypeMetrics typeMetrics = new TypeMetrics();
        for (Map.Entry componentEntry : components.entrySet()) {
            List<String> componentsTableList = (List<String>) componentEntry.getValue();
            for (String tableEntry : componentsTableList) {
                String columnListString = tableColumn.get(tableEntry);
                String resolvedSelectWorkerMetricsQuery = resolveTableName(selectAppComponentMetrics, tableEntry);
                String resolvedSelectWorkerRecentMetricsQuery = resolveTableName(selectAppComponentHistory,
                        tableEntry);
                String resolvedQuery = resolvedSelectWorkerMetricsQuery.replace(PLACEHOLDER_NAME,
                        QUESTION_MARK).replace(PLACEHOLDER_WORKER_ID, QUESTION_MARK).replace
                        (PLACEHOLDER_COLUMNS, columnListString);
                String resolvedRecentQuery = resolvedSelectWorkerRecentMetricsQuery
                        .replace(PLACEHOLDER_NAME, QUESTION_MARK).replace(PLACEHOLDER_WORKER_ID, QUESTION_MARK)
                        .replace(PLACEHOLDER_COLUMNS, columnListString)
                        .replace(SQLConstants.PLACEHOLDER_BEGIN_TIME, QUESTION_MARK)
                        .replace(SQLConstants.PLACEHOLDER_CURRENT_TIME, QUESTION_MARK);
                Object[] recentQueryParameters = new Object[]{carbonId,
                        String.format("%s%s%s", (String) componentEntry.getKey(), ".", PERCENTAGE_MARK),
                        currentTimeMilli - timeInterval, currentTimeMilli};
                //String[] resolvedQueryParameters = new String[] {carbonId, (String) componentEntry.getKey()};
                String[] columnList = columnListString.split(",");
                List<List<Object>> selectionRecent =
                        select(resolvedRecentQuery, columnListString, tableEntry, recentQueryParameters);
                List<Object> selection = new ArrayList<>();
                if (selectionRecent.size() > 0) {
                    selection = selectionRecent.get(selectionRecent.size() - 1);
                }
                String[] componentElements = ((String) componentEntry.getKey()).replace("org.wso2.siddhi" +
                        ".SiddhiApps" +
                        "." + appName + ".Siddhi.", "").split("\\.", 2);
                String metricType = tableMetricsMap.get(tableEntry).toLowerCase();
                if ((selection != null) && (!selection.isEmpty())) {
                    Attribute attribute;
                    if ((!("Streams".equalsIgnoreCase(componentElements[0]))) && ("memory".equalsIgnoreCase(metricType))) {
                        attribute = new Attribute(columnList[1], humanReadableByteCount((double) selection.get(1),
                                true));
                    } else {
                        attribute = new Attribute(columnList[1], NumberFormat.getIntegerInstance().format(selection.get(1)));
                    }
                    attribute.setRecentValues(selectionRecent);
                    metricElement.addAttributes(attribute);
                    if (("Streams".equalsIgnoreCase(componentElements[0])) && ("memory".equalsIgnoreCase(metricType))) {
                        metricElement.setType("size (events)");
                    } else {
                        metricElement.setType(metricType + " " + tableMetricsUnitsMap.get(metricType));
                    }
                    componentMetrics.addMetrics(metricElement);
                    metricElement = new MetricElement();
                    componentMetrics.setName(componentElements[1]);
                    typeMetrics.setType(componentElements[0]);
                    typeMetrics.setData(componentMetrics);
                }
            }
            boolean isNew = true;
            for (TypeMetrics typeMetric : componentsRecentMetrics) {
                if (typeMetrics.getType().equalsIgnoreCase(typeMetric.getType())) {
                    isNew = false;
                    typeMetric.getData().add(typeMetrics.getData().get(0));
                }
            }
            if (isNew) {
                componentsRecentMetrics.add(typeMetrics);
            }
            componentMetrics = new ComponentMetrics();
            typeMetrics = new TypeMetrics();
        }
        return componentsRecentMetrics;
    }

    /**
     * Convert memory bytes into human readable format.
     * unit bytes value is decided by based on SI format or not.
     * Ref:
     * http://programming.guide/java/formatting-byte-size-to-human-readable-format.html
     *
     * @param bytes memory bytes
     * @param si    is is format needed.
     * @return is format.
     */
    private static String humanReadableByteCount(double bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
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
                        columnsOrSelectExpressions).replace(SQLConstants.PLACEHOLDER_BEGIN_TIME, QUESTION_MARK)
                        .replace(PLACEHOLDER_NAME, QUESTION_MARK)
                        .replace(PLACEHOLDER_WORKER_ID, QUESTION_MARK)
                        .replace(SQLConstants.PLACEHOLDER_CURRENT_TIME, QUESTION_MARK)
                        .replace(PLACEHOLDER_RESULT, resultLabel)
                        .replace(PLACEHOLDER_TABLE_NAME, tableName);
                Object[] parameters = new Object[]{workerId, APP_NAME_PREFIX + appName + "." + PERCENTAGE_MARK,
                        currentTime - timeInterval, currentTime};
                return selectAppMemory(resolvedQueryTable, tableName, parameters, "TIMESTAMP");
            }
            case "throughput": {
                String tableName = "METRIC_METER";
                String columnsLabels = "TIMESTAMP,M1_RATE";
                String columnsOrSelectExpressions = "SUM(result.M1_RATE)";
                String resultLabel = "M1_RATE";
                String resolvedQueryTable = selectAppMetricsQuery.replace(SQLConstants.PLACEHOLDER_COLUMNS,
                        columnsOrSelectExpressions).replace(SQLConstants.PLACEHOLDER_BEGIN_TIME, QUESTION_MARK)
                        .replace(PLACEHOLDER_NAME, QUESTION_MARK).replace(PLACEHOLDER_WORKER_ID, QUESTION_MARK)
                        .replace(SQLConstants.PLACEHOLDER_CURRENT_TIME, QUESTION_MARK)
                        .replace(PLACEHOLDER_RESULT, resultLabel)
                        .replace(PLACEHOLDER_TABLE_NAME, tableName);
                Object[] parameters = new Object[]{workerId, APP_NAME_PREFIX + appName + "." + PERCENTAGE_MARK,
                        currentTime - timeInterval, currentTime};
                return select(resolvedQueryTable, columnsLabels, tableName, parameters);
            }
            case "latency": {
                String tableName = "METRIC_TIMER";
                String columnsLabels = "TIMESTAMP,M1_RATE";
                String columnsOrSelectExpressions = "SUM(result.M1_RATE)";
                String resultLabel = "M1_RATE";
                String resolvedQueryTable = selectAppMetricsQuery.replace(SQLConstants.PLACEHOLDER_COLUMNS,
                        columnsOrSelectExpressions).replace(SQLConstants.PLACEHOLDER_BEGIN_TIME, QUESTION_MARK)
                        .replace(PLACEHOLDER_NAME, QUESTION_MARK).replace(PLACEHOLDER_WORKER_ID, QUESTION_MARK)
                        .replace(SQLConstants.PLACEHOLDER_CURRENT_TIME, QUESTION_MARK)
                        .replace(PLACEHOLDER_RESULT, resultLabel)
                        .replace(PLACEHOLDER_TABLE_NAME, tableName);
                Object[] parameters = new Object[]{workerId, APP_NAME_PREFIX + appName + "." + PERCENTAGE_MARK,
                        currentTime - timeInterval, currentTime};
                return select(resolvedQueryTable, columnsLabels, tableName, parameters);
            }
            default: {
                logger.error("Invalid parameters type: " + removeCRLFCharacters(workerId) + ":"
                        + removeCRLFCharacters(appName));
                return null;
            }
        }
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
    public List selectAppAggOverallMetrics(String metricsType, String workerId, long
            timeInterval, String appName, long currentTime) {
        long aggregationTime = DBTableUtils.getAggregation(timeInterval);
        switch (metricsType) {
            case "memory": {
                String tableName = "METRIC_GAUGE";
                String columnsOrSelectExpressions = "SUM(CAST(result.VALUE as DECIMAL(22,2)))";
                String resultLabel = "VALUE";
                String resolvedQueryTable = recordSelectAgregatedAppMetricsQuery.replace(SQLConstants.PLACEHOLDER_COLUMNS,
                        columnsOrSelectExpressions).replace(SQLConstants.PLACEHOLDER_BEGIN_TIME, QUESTION_MARK)
                        .replace(PLACEHOLDER_NAME, QUESTION_MARK)
                        .replace(PLACEHOLDER_WORKER_ID, QUESTION_MARK)
                        .replace(SQLConstants.PLACEHOLDER_CURRENT_TIME, QUESTION_MARK)
                        .replace(PLACEHOLDER_RESULT, resultLabel)
                        .replace(PLACEHOLDER_TABLE_NAME, tableName)
                        .replace(PLACEHOLDER_AGGREGATION_TIME, Long.toString(aggregationTime));
                Object[] parameters = new Object[]{workerId, APP_NAME_PREFIX + appName + "." + PERCENTAGE_MARK,
                        currentTime - timeInterval, currentTime};
                return selectAppMemory(resolvedQueryTable, tableName, parameters, "AGG_TIMESTAMP");
            }
            case "throughput": {
                String tableName = "METRIC_METER";
                String columnsLabels = "AGG_TIMESTAMP,M1_RATE";
                String columnsOrSelectExpressions = "SUM(result.M1_RATE)";
                String resultLabel = "M1_RATE";
                String resolvedQueryTable = recordSelectAgregatedAppMetricsQuery.replace(SQLConstants.PLACEHOLDER_COLUMNS,
                        columnsOrSelectExpressions).replace(SQLConstants.PLACEHOLDER_BEGIN_TIME, QUESTION_MARK)
                        .replace(PLACEHOLDER_NAME, QUESTION_MARK).replace
                                (PLACEHOLDER_WORKER_ID, QUESTION_MARK).replace(SQLConstants.PLACEHOLDER_CURRENT_TIME,
                                QUESTION_MARK).replace(PLACEHOLDER_RESULT, resultLabel)
                        .replace(PLACEHOLDER_TABLE_NAME, tableName)
                        .replace(PLACEHOLDER_AGGREGATION_TIME, Long.toString(aggregationTime));
                Object[] parameters = new Object[]{workerId, APP_NAME_PREFIX + appName + "." + PERCENTAGE_MARK,
                        currentTime - timeInterval, currentTime};
                return select(resolvedQueryTable, columnsLabels, tableName, parameters);
            }
            case "latency": {
                String tableName = "METRIC_TIMER";
                String columnsLabels = "AGG_TIMESTAMP,M1_RATE";
                String columnsOrSelectExpressions = "SUM(result.M1_RATE)";
                String resultLabel = "M1_RATE";
                String resolvedQueryTable = recordSelectAgregatedAppMetricsQuery.replace(SQLConstants.PLACEHOLDER_COLUMNS,
                        columnsOrSelectExpressions).replace(SQLConstants.PLACEHOLDER_BEGIN_TIME, QUESTION_MARK)
                        .replace(PLACEHOLDER_NAME, QUESTION_MARK).replace
                                (PLACEHOLDER_WORKER_ID, QUESTION_MARK).replace(SQLConstants.PLACEHOLDER_CURRENT_TIME,
                                QUESTION_MARK).replace(PLACEHOLDER_RESULT, resultLabel)
                        .replace(PLACEHOLDER_TABLE_NAME, tableName)
                        .replace(PLACEHOLDER_AGGREGATION_TIME, Long.toString(aggregationTime));
                Object[] parameters = new Object[]{workerId, APP_NAME_PREFIX + appName + "." + PERCENTAGE_MARK,
                        currentTime - timeInterval, currentTime};
                return select(resolvedQueryTable, columnsLabels, tableName, parameters);
            }
            default: {
                logger.error("Invalid parameters type: " + removeCRLFCharacters(workerId) + ":"
                        + removeCRLFCharacters(appName));
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
        String resolvedQuery = resolvedSelectWorkerMetricsQuery.replace(SQLConstants.PLACEHOLDER_BEGIN_TIME,
                QUESTION_MARK).replace(PLACEHOLDER_NAME, QUESTION_MARK)
                .replace(PLACEHOLDER_WORKER_ID, QUESTION_MARK)
                .replace(SQLConstants.PLACEHOLDER_CURRENT_TIME, QUESTION_MARK);
        Object[] parameters = new Object[]{workerId, metricTypeName, currentTime - timeInterval, currentTime};
        return selectGauge(resolvedQuery, false, parameters);
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
        String resolvedQuery = resolvedSelectWorkerMetricsQuery.replace(SQLConstants.PLACEHOLDER_BEGIN_TIME,
                QUESTION_MARK).replace(PLACEHOLDER_NAME, QUESTION_MARK)
                .replace(PLACEHOLDER_WORKER_ID, QUESTION_MARK)
                .replace(SQLConstants.PLACEHOLDER_CURRENT_TIME, QUESTION_MARK)
                .replace(PLACEHOLDER_AGGREGATION_TIME, Long.toString(aggregationTime));
        Object[] parameters = new Object[]{workerId, metricTypeName, currentTime - timeInterval, currentTime};
        return selectGauge(resolvedQuery, true, parameters);
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
                "SUM(result.M1_RATE)").replace
                (SQLConstants.PLACEHOLDER_BEGIN_TIME, QUESTION_MARK).replace
                (PLACEHOLDER_WORKER_ID, QUESTION_MARK).replace(SQLConstants.PLACEHOLDER_CURRENT_TIME,
                QUESTION_MARK).replace(PLACEHOLDER_RESULT, "M1_RATE");
        Object[] parameters = new Object[]{workerId, currentTime - timeInterval, currentTime};
        return select(resolvedQuery, "TIMESTAMP,M1_RATE", "METRIC_METER", parameters);
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
                "SUM(result.M1_RATE)").replace
                (SQLConstants.PLACEHOLDER_BEGIN_TIME, QUESTION_MARK).replace
                (PLACEHOLDER_WORKER_ID, QUESTION_MARK).replace(SQLConstants.PLACEHOLDER_CURRENT_TIME,
                QUESTION_MARK).replace(PLACEHOLDER_RESULT, "M1_RATE").
                replace(PLACEHOLDER_AGGREGATION_TIME, Long.toString(aggregationTime));
        Object[] parameters = new Object[]{workerId, currentTime - timeInterval, currentTime};
        return select(resolvedQuery, "AGG_TIMESTAMP,M1_RATE", "METRIC_METER", parameters);
    }

    /**
     * Select the metrics of the siddhi app.
     *
     * @param query selection query.
     * @return the selected object.
     */
    private List<List<Object>> selectAppMemory(String query, String tableName, Object[] parameters, String timesStampLable) {
        Map<String, String> attributesTypeMap = workerAttributeTypeMap.get(tableName);
        Connection conn = this.getConnection();
        ResultSet rs = null;
        List<List<Object>> tuple = new ArrayList<>();
        List<Object> row;
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            setDynamicValuesToStatement(stmt,parameters);
            rs = DBHandler.getInstance().select(stmt);
            while (rs.next()) {
                row = new ArrayList<>();
                row.add(DBTableUtils.getInstance().fetchData(rs, timesStampLable, attributesTypeMap.get
                        (timesStampLable), metricsQueryManager));
                row.add(Double.valueOf((String) DBTableUtils.getInstance().fetchData(rs, "VALUE",
                        attributesTypeMap.get("VALUE"), metricsQueryManager)));
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
            } catch (SQLException e) {
                //ignore
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                //ignore
            }
            cleanupConnection(conn);
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
    private List<List<Object>> select(String query, String columns, String tableName, Object[] parameters) {
        Map<String, String> attributesTypeMap = workerAttributeTypeMap.get(tableName);
        Connection conn = this.getConnection();
        ResultSet rs = null;
        List<List<Object>> tuple = new ArrayList<>();
        List<Object> row;
        PreparedStatement stmt = null;
        String[] columnLabels = columns.split(",");
        try {
            stmt = conn.prepareStatement(query);
            setDynamicValuesToStatement(stmt,parameters);
            rs = DBHandler.getInstance().select(stmt);
            while (rs.next()) {
                row = new ArrayList<>();
                for (String columnLabel : columnLabels) {
                    if (columnLabel.equalsIgnoreCase("VALUE")) {
                        row.add(Double.valueOf((String) DBTableUtils.getInstance().fetchData(rs, columnLabel, attributesTypeMap.get
                                (columnLabel), metricsQueryManager)));
                    } else {
                        row.add(DBTableUtils.getInstance().fetchData(rs, columnLabel, attributesTypeMap.get
                                (columnLabel), metricsQueryManager));
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
            } catch (SQLException e) {
                //ignore
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
                //ignore
            }
            cleanupConnection(conn);
        }
        return tuple;
    }

    /**
     * Select the metrics.
     *
     * @param query selection query.
     * @return the selected object.
     */
    private List<List<Object>> selectGauge(String query, boolean isAggregated, Object[] parameters) {
        Map<String, String> attributesTypeMap = workerAttributeTypeMap.get("METRIC_GAUGE");
        Connection conn = this.getConnection();
        ResultSet rs = null;
        List<List<Object>> tuple = new ArrayList<>();
        PreparedStatement stmt = null;
        List<Object> row;
        try {
            stmt = conn.prepareStatement(query);
            setDynamicValuesToStatement(stmt,parameters);
            rs = DBHandler.getInstance().select(stmt);
            String timestampCol = "TIMESTAMP";
            if (isAggregated) {
                timestampCol = "AGG_TIMESTAMP";
            }
            while (rs.next()) {
                row = new ArrayList<>();
                row.add(DBTableUtils.getInstance().fetchData(rs, timestampCol, attributesTypeMap.get
                        ("TIMESTAMP"), metricsQueryManager));
                row.add(Double.valueOf((String) DBTableUtils.getInstance().fetchData(rs, "VALUE",
                        attributesTypeMap.get
                                ("VALUE"), metricsQueryManager)));
                tuple.add(row);
            }
        } catch (SQLException e) {
            throw new RDBMSTableException("Error retrieving records from table '" + "METRIC_GAUGE" + "': "
                    + e.getMessage() + " in " + DATASOURCE_ID, e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    //ignore
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    //ignore
                }
            }
            cleanupConnection(conn);
        }
        return tuple;
    }

    private static String removeCRLFCharacters(String str) {
        if (str != null) {
            str = str.replace('\n', '_').replace('\r', '_');
        }
        return str;
    }

    private void setDynamicValuesToStatement(PreparedStatement statement,Object[] parameters) throws SQLException {
        int counter = 1;
        for (Object parameter : parameters){
            if(parameter instanceof String){
                statement.setString(counter, (String) parameter);
            } else if(parameter instanceof Long){
                statement.setLong(counter, (Long) parameter);
            } else if(parameter instanceof Double){
                statement.setDouble(counter, (Double) parameter);
            } else if(parameter instanceof Integer){
                statement.setInt(counter, (Integer) parameter);
            } else if(parameter instanceof Float){
                statement.setFloat(counter, (Float) parameter);
            } else if(parameter instanceof Boolean){
                statement.setBoolean(counter, (Boolean) parameter);
            } else {
                logger.error("Invalid Type of Object.Found " + parameters.getClass());
            }
            counter ++;
        }
    }
}
