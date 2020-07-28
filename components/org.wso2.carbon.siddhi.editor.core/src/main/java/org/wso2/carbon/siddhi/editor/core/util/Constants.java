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

package org.wso2.carbon.siddhi.editor.core.util;

import io.siddhi.core.executor.function.FunctionExecutor;
import io.siddhi.core.query.processor.stream.StreamProcessor;
import io.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import io.siddhi.core.query.processor.stream.window.WindowProcessor;
import io.siddhi.core.query.selector.attribute.aggregator.AttributeAggregatorExecutor;
import io.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalAttributeAggregator;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.stream.input.source.SourceMapper;
import io.siddhi.core.stream.output.sink.Sink;
import io.siddhi.core.stream.output.sink.SinkMapper;
import io.siddhi.core.table.record.AbstractRecordTable;
import org.wso2.carbon.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Constants related to Editor.
 */
public class Constants {

    public static final String RUNTIME_PATH = Utils.getRuntimePath().normalize().toString();
    public static final String CARBON_HOME = Utils.getCarbonHome().normalize().toString();
    public static final String DIRECTORY_DEPLOYMENT = "deployment";
    public static final String DIRECTORY_WORKSPACE = "workspace";
    public static final String DIRECTORY_SAMPLE = "samples";
    public static final String DIRECTORY_ARTIFACTS = "artifacts";
    public static final String SIDDHI_APP_FILE_EXTENSION = ".siddhi";
    public static final String SIDDHI_APP_NAME = "fileName";
    public static final String DEPLOYMENT_HOST = "host";
    public static final String DEPLOYMENT_PORT = "port";
    public static final String DEPLOYMENT_USERNAME = "username";
    public static final String DEPLOYMENT_PASSWORD = "password";
    public static final String SIDDHI_FILE_LIST = "siddhiFileList";
    public static final String SERVER_LIST = "serverList";
    public static final String TYPE_XML = "xml";
    public static final String TYPE_JSON = "json";
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_CSV = "csv";
    public static final String SAMPLE_EVENTS_PARENT_TAG = "events";
    public static final String SAMPLE_EVENT_PARENT_TAG = "event";
    public static final String ATTR_TYPE_FLOAT = "float";
    public static final String ATTR_TYPE_DOUBLE = "double";
    public static final String ATTR_TYPE_INTEGER = "int";
    public static final String ATTR_TYPE_LONG = "long";
    public static final String ATTR_TYPE_STRING = "string";
    public static final String ATTR_TYPE_BOOL = "bool";
    public static final String EVENT_ATTRIBUTE_VALUE_SEPARATOR = ":";
    public static final String FAULT_STREAM_PREFIX = "!";
    public static final String EXPORT_PROPERTIES_NAMESPACE = "exportConfigs";
    public static final String DOCKER_BASE_IMAGE_PROPERTY = "dockerBaseImage";
    public static final String DEFAULT_SI_DOCKER_BASE_IMAGE_NAME = "wso2/streaming-integrator";
    public static final String DEFAULT_SI_DOCKER_IMAGE_NAME = "wso2/streaming-integrator:1.0.0";
    public static final String DEFAULT_SI_DOCKER_IMAGE_VERSION = "1.0.0";
    public static final String COLLON = ":";
    public static final String JDBC = "jdbc";
    public static final String DB_URL = "url";
    public static final String JDBC_URL = "jdbcUrl";
    public static final String DB_USERNAME = "username";
    public static final String DB_PASSWORD = "password";
    public static final String MYSQL_DATABASE = "mysql";
    public static final String MSSQL_DATABASE = "sqlserver";
    public static final String ORACLE_DATABASE = "oracle";
    public static final String POSTGRESQL = "postgresql";
    public static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
    public static final String MSSQL_DRIVER_CLASS_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static final String POSTGRESQL_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    public static final String ORACLE_DRIVER_CLASS_NAME = "oracle.jdbc.driver.OracleDriver";
    public static final String CONNECTION = "connection";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String COLUMN_NAME = "COLUMN_NAME";
    public static final String TYPE_NAME = "TYPE_NAME";
    public static final String DATASOURCE_NAME = "dataSourceName";
    public static final String ATTRIBUTES = "attributes";
    public static final String TABLES = "tables";
    public static final String NAME = "name";
    public static final String DATA_TYPE = "type";
    public static final String TABLE_NAME = "tableName";
    static final String FUNCTION_EXECUTOR = "FunctionExecutor";
    static final String ATTRIBUTE_AGGREGATOR_EXECUTOR = "AttributeAggregatorExecutor";
    static final String ATTRIBUTE_AGGREGATOR = "AttributeAggregatorExecutor";
    static final String INCREMENTAL_AGGREGATOR = "IncrementalAggregator";
    static final String WINDOW_PROCESSOR = "WindowProcessor";
    static final String STREAM_FUNCTION_PROCESSOR = "StreamFunctionProcessor";
    static final String STREAM_PROCESSOR = "StreamProcessor";
    static final String SOURCE = "Source";
    static final String SINK = "Sink";
    static final String SOURCEMAP = "SourceMap";
    static final String SINKMAP = "SinkMap";
    static final String STORE = "Store";
    public static final String OPERATOR_YAML_NAME = "siddhi-operator.yaml";
    public static final String OPERATOR_PREREQ_YAML_NAME = "k8-prerequisites.yaml";
    public static final String ERROR = "error";
    public static final String WARNING = "warning";
    public static final String REGEX_TO_KEEP_QUOTES = "(?=(?:[^\"|']*(\"|')[^\"|']*(\"|'))*[^\"|']*$)";
    static final Map<String, Class<?>> SUPER_CLASS_MAP;
    static final Map<String, String> PACKAGE_NAME_MAP;

    static {
        // Populating the processor super class map
        SUPER_CLASS_MAP = new HashMap<>();
        SUPER_CLASS_MAP.put(FUNCTION_EXECUTOR, FunctionExecutor.class);
        SUPER_CLASS_MAP.put(INCREMENTAL_AGGREGATOR, IncrementalAttributeAggregator.class);
        SUPER_CLASS_MAP.put(ATTRIBUTE_AGGREGATOR, AttributeAggregatorExecutor.class);
        SUPER_CLASS_MAP.put(WINDOW_PROCESSOR, WindowProcessor.class);
        SUPER_CLASS_MAP.put(STREAM_FUNCTION_PROCESSOR, StreamFunctionProcessor.class);
        SUPER_CLASS_MAP.put(STREAM_PROCESSOR, StreamProcessor.class);
        SUPER_CLASS_MAP.put(SOURCE, Source.class);
        SUPER_CLASS_MAP.put(SINK, Sink.class);
        SUPER_CLASS_MAP.put(SOURCEMAP, SourceMapper.class);
        SUPER_CLASS_MAP.put(SINKMAP, SinkMapper.class);
        SUPER_CLASS_MAP.put(STORE, AbstractRecordTable.class);

        // Populating the package name map
        PACKAGE_NAME_MAP = new HashMap<>();
        PACKAGE_NAME_MAP.put(FUNCTION_EXECUTOR, "io.siddhi.core.executor.function");
        PACKAGE_NAME_MAP.put(ATTRIBUTE_AGGREGATOR,
                "io.siddhi.core.query.selector.attribute.aggregator");
        PACKAGE_NAME_MAP.put(WINDOW_PROCESSOR, "io.siddhi.core.query.processor.stream.window");
    }
}
