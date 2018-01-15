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

public class PersistenceConstants {

    private PersistenceConstants(){}

    public static final String PLACEHOLDER_TABLE_NAME = "{{TABLE_NAME}}";
    public static final String STATE_PERSISTENCE_NS = "state.persistence";
    public static final String STATE_PERSISTENCE_REVISIONS_TO_KEEP = "revisionsToKeep";
    public static final String STATE_PERSISTENCE_CONFIGS = "config";
    public static final String DEFAULT_FILE_PERSISTENCE_FOLDER = "siddhi-app-persistence";
    public static final String DEFAULT_DB_PERSISTENCE_DATASOURCE = "WSO2_CARBON_DB";
    public static final String DEFAULT_DB_PERSISTENCE_TABLE_NAME = "PERSISTENCE_TABLE";

    public static final String CREATE_TABLE = "CREATE_TABLE";
    public static final String INSERT_INTO_TABLE = "INSERT_INTO_TABLE";
    public static final String IS_TABLE_EXISTS = "IS_TABLE_EXISTS";
    public static final String SELECT_SNAPSHOT = "SELECT_SNAPSHOT";
    public static final String SELECT_LAST_REVISION = "SELECT_LAST_REVISION";
    public static final String DELETE_ROW_FROM_TABLE = "DELETE_ROW_FROM_TABLE";
    public static final String COUNT_NUMBER_REVISIONS = "COUNT_NUMBER_REVISIONS";

}
