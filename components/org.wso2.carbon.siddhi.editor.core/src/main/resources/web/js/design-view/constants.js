/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Constants to be used by the siddhi server  design view
 */
define(function () {

    "use strict";   // JS strict mode

    /**
     * Constants used by the siddhi server design-view
     */
    var constants = {
        ALPHABETIC_VALIDATOR_REGEX: /^([a-zA-Z])$/,
        INT_LONG_VALIDATOR_REGEX: /^[-+]?\d+$/,
        DOUBLE_FLOAT_VALIDATOR_REGEX: /^[+-]?([0-9]*[.])?[0-9]+$/,
        TIME_VALIDATOR_REGEX: /^[_A-z0-9]*((-|\s)*[_A-z0-9])*$/g,
        START: "start",
        CRON_EXPRESSION: "cron-expression",
        EVERY: "every",
        AT: "at",
        SORT: "sort",
        FREQUENT: "frequent",
        LOSSY_FREQUENT: "lossyfrequent",
        DEFAULT_STORE_TYPE: "in-memory",
        RDBMS_STORE_TYPE: "rdbms",
        DEFAULT_MAPPER_TYPE: "passThrough",
        ATTRIBUTE: "attribute",
        MAP: "map",
        LIST: "list",
        MAPPER: "mapper",
        STORE: "store",
        PRIMARY_KEY: "primarykey",
        INDEX: "index",
        AGGREGATION: "aggregation",
        FUNCTION: "function",
        SINK: "sink",
        SOURCE: "source",
        TABLE: "table",
        STREAM: "stream",
        WINDOW: "window",
        TRIGGER: "trigger",
        PARTITION: "partition",
        JOIN_QUERY: "join-query",
        PATTERN_QUERY: "pattern-query",
        SEQUENCE_QUERY: "sequence-query",
        WINDOW_FILTER_PROJECTION_QUERY: "window-filter-projection-query",
        FILTER_QUERY_DROP: "filterQueryDrop",
        WINDOW_QUERY_DROP: "windowQueryDrop",
        FUNCTION_QUERY_DROP: "functionQueryDrop",
        PROJECTION_QUERY_DROP: "projectionQueryDrop",
        PROJECTION: "projection",
        FILTER: "filter",
        TYPE_USER_DEFINED: "user_defined",
        TYPE_ALL: "all",
        VALUE_ALL: "*",
        INT: "int",
        LONG: "long",
        DOUBLE: "double",
        FLOAT: "float",
        TIME: "time",
        BOOL: "bool",
        TRIGGERED_TIME: "triggered_time",
        INTERVAL: "interval",
        RANGE: "range",
        GROUP_BY: "group-by",
        ORDER_BY: "order-by",
        SIDDHI_TIME: ["seconds", "minutes", "hours", "days", "months", "years"],
        MINUTES: "minutes",
        AS: "as",
        RETENTION_PERIOD: "retentionperiod",
        JOIN: "join",
        LEFT: "left",
        RIGHT: "right",
        QUERY: "query",
        OPERATION: "operation",
        INSERT: "insert",
        UPDATE: "update",
        DELETE: "delete",
        UPDATE_OR_INSERT_INTO: "update_or_insert_into",
        CUSTOM: "custom",
        STREAM_HANDLER: "stream-handler",
        STREAM_FUNCTION: "stream-function",
        TYPE_HTTP_REQUEST: "http-request",
        TYPE_HTTP_RESPONSE: "http-response",
        JOIN_QUERY_TITLE: "Join Query Configuration",
        AGGREGATION_TITLE: "Aggregation Configuration",
        APP_ANNOTATION_TITLE: "Siddhi App Configuration",
        FUNCTION_TITLE: "Function Configuration",
        PARTITION_TITLE: "Partition Configuration",
        PATTERN_QUERY_TITLE: "Pattern Query Configuration",
        SEQUENCE_QUERY_TITLE: "Sequence Query Configuration",
        SINK_TITLE: "Sink Configuration",
        SOURCE_TITLE: "Source Configuration",
        STREAM_TITLE: "Stream Configuration",
        TABLE_TITLE: "Table Configuration",
        TRIGGER_TITLE: "Trigger Configuration",
        QUERY_TITLE: "Query Configuration",
        WINDOW_TITLE: "Window Configuration",
        DATASOURCE: "datasource",
        INLINE_CONFIG: "inline-config",
        JNDI_RESOURCE: "jndi-resource",
        FAULT_STREAM_PREFIX: "!",
        PURGING: "purging",
        PURGE: "purge",
        CURRENT_EVENTS: "current_events",
        AGGREGATE_FUNCTION: "aggregate-function",
        KEYWORD: "keyword",
        OPERATOR: "operator",
        INPUT: "input",
        FOR: "for",
        ASC:"asc",
        DESC: "desc",
        BATCH_WINDOW_PROCESSOR: "batch",
        MULTI_VALUE: "..."
    };

    return constants;
});