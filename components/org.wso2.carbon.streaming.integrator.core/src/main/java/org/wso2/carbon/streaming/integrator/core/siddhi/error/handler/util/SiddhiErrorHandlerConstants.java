/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.streaming.integrator.core.siddhi.error.handler.util;

/**
 * Contains constants related to Siddhi Error Handler.
 */
public class SiddhiErrorHandlerConstants {

    private SiddhiErrorHandlerConstants(){}

    public static final String PLACEHOLDER_TABLE_NAME = "{{TABLE_NAME}}";
    public static final String ERROR_STORE_NS = "error.store";
    public static final String BUFFER_SIZE = "bufferSize";
    public static final String DROP_WHEN_BUFFER_FULL = "dropWhenBufferFull";
    public static final String ERROR_STORE_CONFIGS = "config";
    public static final String DEFAULT_DB_ERROR_STORE_DATASOURCE = "WSO2_CARBON_DB";
    public static final String DEFAULT_DB_ERROR_STORE_TABLE_NAME = "SIDDHI_ERROR_STORE_TABLE";
    public static final String IS_TABLE_EXISTS = "IS_TABLE_EXISTS";
    public static final String CREATE_TABLE = "CREATE_TABLE";
    public static final String INSERT_INTO_TABLE = "INSERT_INTO_TABLE";
    public static final String SELECT_FROM_TABLE = "SELECT_FROM_TABLE";
    public static final String MINIMAL_SELECT_FROM_TABLE = "MINIMAL_SELECT_FROM_TABLE";
    public static final String SINGLE_SELECT_FROM_TABLE = "SINGLE_SELECT_FROM_TABLE";
    public static final String SELECT_WITH_LIMIT_OFFSET = "SELECT_WITH_LIMIT_OFFSET";
    public static final String MINIMAL_SELECT_WITH_LIMIT_OFFSET = "MINIMAL_SELECT_WITH_LIMIT_OFFSET";
    public static final String SELECT_COUNT_FROM_TABLE = "SELECT_COUNT_FROM_TABLE";
    public static final String SELECT_COUNT_FROM_TABLE_BY_SIDDHI_APP = "SELECT_COUNT_FROM_TABLE_BY_SIDDHI_APP";
    public static final String DELETE_ROW_FROM_TABLE = "DELETE_ROW_FROM_TABLE";
    public static final String DELETE_FROM_TABLE_BY_SIDDHI_APP_NAME = "DELETE_FROM_TABLE_BY_SIDDHI_APP_NAME";
    public static final String PURGE_TABLE = "PURGE_TABLE";

    public static final String ID = "id";
    public static final String TIMESTAMP = "timestamp";
    public static final String SIDDHI_APP_NAME = "siddhiAppName";
    public static final String STREAM_NAME = "streamName";
    public static final String EVENT = "event";
    public static final String CAUSE = "cause";
    public static final String STACK_TRACE = "stackTrace";
    public static final String ORIGINAL_PAYLOAD = "originalPayload";
    public static final String ERROR_OCCURRENCE = "errorOccurrence";
    public static final String EVENT_TYPE = "eventType";
    public static final String ERROR_TYPE = "errorType";
    public static final String ENTRIES_COUNT = "entriesCount";

    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";
    public static final String DESCRIPTIVE = "descriptive";

}
