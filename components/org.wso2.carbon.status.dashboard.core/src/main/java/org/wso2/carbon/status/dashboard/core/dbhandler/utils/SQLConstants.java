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

package org.wso2.carbon.status.dashboard.core.dbhandler.utils;

/**
 * Class to define SQL queries and constants.
 */
public class SQLConstants {
    private SQLConstants() {
        //preventing initialization
    }
    
    public static final String SELECT_QUERY = "recordSelectQuery";
    public static final String SELECT_APP_METRICS_QUERY = "recordSelectAppMetricsQuery";
    public static final String SELECT_WORKER_METRICS_QUERY = "recordSelectWorkerMetricsQuery";
    public static final String SELECT_WORKER_THROUGHPUT_QUERY = "recordSelectWorkerThroughputQuery";
    public static final String SELECT_WORKER_AGGREGATE_METRICS_QUERY = "recordSelectWorkerAggregateMetricsQuery";
    public static final String SELECT_WORKER_AGGREGATE_THROUGHPUT_QUERY = "recordSelectWorkerAggregateThroughputQuery";
    public static final String SELECT_COMPONENT_LIST = "selectAppComponentList";
    public static final String SELECT_COMPONENT_METRICS_HISTORY = "selectAppComponentHistory";
    public static final String SELECT_APP_AGG_METRICS_HISTORY = "recordSelectAgregatedAppMetricsQuery";
    public static final String SELECT_COMPONENT_AGG_METRICS_HISTORY = "selectAppComponentAggregatedHistory";
    public static final String SELECT_COMPONENT_METRICS = "selectAppComponentMetrics";
    public static final String CREATE_TABLE = "tableCreateQuery";
    public static final String ISTABLE_EXISTS_QUERY = "tableCheckQuery";
    public static final String DELETE_QUERY = "recordDeleteQuery";
    public static final String INSERT_QUERY = "recordInsertQuery";
    
    //Placeholder strings needed for processing the query configuration file
    public static final String DASHBOARD_CONFIG_FILE = "dashboard-configs.yaml";
    public static final String QUERY_CONFIG_FILE = "queries.yaml";
    public static final String PLACEHOLDER_COLUMNS = "{{COLUMNS}}";
    public static final String PLACEHOLDER_CONDITION = "{{CONDITION}}";
    public static final String PLACEHOLDER_TABLE_NAME = "{{TABLE_NAME}}";
    public static final String PLACEHOLDER_NAME = "{{NAME}}";
    public static final String PLACEHOLDER_AGGREGATION_COMPONENT_COLOUM = "{{ALL_COLUMS_EXPRESSION}}";
    public static final String PLACEHOLDER_AGGREGATION_TIME = "{{TIME_AGGREGATION_IN_MINUTES}}";
    public static final String PLACEHOLDER_WORKER_ID = "{{WORKER_ID}}";
    public static final String PLACEHOLDER_TIME_INTERVAL = "{{TIME_INTERVAL}}";
    public static final String PLACEHOLDER_CURRENT_TIME = "{{CURRENT_TIME_MILLISECONDS}}";
    public static final String PLACEHOLDER_BEGIN_TIME = "{{BEGIN_TIME_MILLISECONDS}}";
    public static final String PLACEHOLDER_RESULT = "{{RESULT}}";
    public static final String PLACEHOLDER_Q = "{{Q}}";
    public static final String PLACEHOLDER_COLUMNS_PRIMARYKEY = "{{COLUMNS, PRIMARY_KEYS}}";
    public static final String STRING_TEMPLATE = "%s";
    public static final String INTEGER_TEMPLATE = "%d";
    public static final String SQL_WHERE = "WHERE";
    public static final String WHITESPACE = " ";
    public static final String SEPARATOR = ",";
    public static final String SEPARATOR_REGEX = ",";
    public static final String TUPLE_SEPARATOR = ",%n";
    public static final String QUESTION_MARK = "?";
    public static final String PERCENTAGE_MARK = "%";
    public static final String PACKAGE_NAME_SEPARATOR = ".";
    public static final String COLUMN_COUNT = "TIMESTAMP,COUNT";
    public static final String AGG_COLUMN_COUNT = "AGG_TIMESTAMP,COUNT";
    public static final String AGG_AVG_COLUMN_COUNT ="AVG(COUNT) as COUNT";
    //metrics types
    public static final String METRICS_TYPE_LATENCY = "latency";
    public static final String METRICS_TYPE_LATENCY_COUNT = "latency_count";
    public static final String METRICS_TYPE_MEMORY = "memory";
    public static final String METRICS_TYPE_THROUGHPUT = "throughput";
    public static final String METRICS_TYPE_THROUGHPUT_COUNT = "throughput_count";
    //metrics table names
    public static final String METRICS_TABLE_METRIC_COUNTER = "METRIC_COUNTER";
    public static final String METRICS_TABLE_METRIC_GAUGE = "METRIC_GAUGE";
    public static final String METRICS_TABLE_METRIC_HISTOGRAM = "METRIC_HISTOGRAM";
    public static final String METRICS_TABLE_METRIC_METER = "METRIC_METER";
    public static final String METRICS_TABLE_METRIC_TIMER = "METRIC_TIMER";
    
    //metrics column names
    public static final String COLUMN_TIMESTAMP = "TIMESTAMP";
    public static final String COLUMN_AGG_TIMESTAMP = "AGG_TIMESTAMP";
    public static final String COLUMN_VALUE = "VALUE";
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_M1_RATE = "M1_RATE";
    
    //sql expressions
    public static final String EXPR_SUM_FROM_STRING = "SUM(CAST(result.VALUE as DECIMAL(22,2)))";
    public static final String EXPR_SUM_FROM_M1_RATE = "SUM(result.M1_RATE)";
}
