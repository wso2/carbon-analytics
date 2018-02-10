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
package org.wso2.carbon.sp.jobmanager.core.dbhandler.utils;

/**
 * Class to define SQL queries and constants.
 */
public class SQLConstants {
    public static final String SELECT_QUERY = "recordSelectQuery";
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
    public static final String PLACEHOLDER_Q = "{{Q}}";
    public static final String PLACEHOLDER_COLUMNS_PRIMARYKEY = "{{COLUMNS, PRIMARY_KEYS}}";
    public static final String String_TEMPLATE = "%s";
    public static final String SQL_WHERE = "WHERE";
    public static final String WHITESPACE = " ";
    public static final String SEPARATOR = ", ";
    public static final String TUPLES_SEPARATOR = ",%n";
    public static final String QUESTION_MARK = "?";


    private SQLConstants() {
        //preventing initialization
    }

}
