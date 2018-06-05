/*
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

package org.wso2.carbon.siddhi.editor.core.util.designview.constants;

/**
 * Contains all the generic String/char values that are needed by
 * the CodeGenerator class to build the entire Siddhi app string
 */
public class SiddhiStringBuilderConstants {
    // General chars
    public static final char ALL = '*';
    public static final char FULL_STOP = '.';
    public static final char COMMA = ',';
    public static final char EQUAL = '=';
    public static final char HASH = '#';
    public static final char NEW_LINE = '\n';
    public static final char SEMI_COLON = ';';
    public static final char SINGLE_QUOTE = '\'';
    public static final char SPACE = ' ';
    // Brackets
    public static final char OPEN_BRACKET = '(';
    public static final char CLOSE_BRACKET = ')';
    public static final char OPEN_SQUARE_BRACKET = '[';
    public static final char CLOSE_SQUARE_BRACKET = ']';
    public static final char OPEN_CURLY_BRACKET = '{';
    public static final char CLOSE_CURLY_BRACKET = '}';
    // General Strings
    public static final String EMPTY_STRING = "";
    public static final String TAB_SPACE = "    ";
    public static final String THREE_DOTS = "...";
    // Definition Strings
    public static final String DEFINE_STREAM = "define stream";
    public static final String DEFINE_TABLE = "define table";
    public static final String DEFINE_WINDOW = "define window";
    public static final String DEFINE_TRIGGER = "define trigger";
    public static final String DEFINE_AGGREGATION = "define aggregation";
    public static final String DEFINE_FUNCTION = "define function";
    // Event Strings
    public static final String CURRENT_EVENTS = "current events";
    public static final String EXPIRED_EVENTS = "expired events";
    public static final String ALL_EVENTS = "all events";
    // Annotation Strings
    public static final String APP_NAME_ANNOTATION = "@App:name('";
    public static final String APP_DESCRIPTION_ANNOTATION = "@App:description('";
    public static final String SOURCE_ANNOTATION = "@source(type='";
    public static final String SINK_ANNOTATION = "@sink(type='";
    public static final String STORE_ANNOTATION = "@store(type='";
    public static final String MAP_ANNOTATION = "@map(type='";
    public static final String ATTRIBUTES_ANNOTATION = "@attributes(";
    public static final String PAYLOAD_ANNOTATION = "@payload(";
    public static final String DEFAULT_APP_NAME_ANNOTATION = APP_NAME_ANNOTATION + "SiddhiApp" +
            SINGLE_QUOTE + CLOSE_BRACKET;
    public static final String DEFAULT_APP_DESCRIPTION_ANNOTATION = APP_DESCRIPTION_ANNOTATION +
            "Description of the plan" + SINGLE_QUOTE + CLOSE_BRACKET;
    // Join Strings
    public static final String JOIN = "join";
    public static final String LEFT_OUTER_JOIN = "left outer join";
    public static final String RIGHT_OUTER_JOIN = "right outer join";
    public static final String FULL_OUTER_JOIN = "full outer join";
    // Query Output Strings
    public static final String INSERT = "insert";
    public static final String DELETE = "delete";
    public static final String UPDATE = "update";
    public static final String UPDATE_OR_INSERT_INTO = "update or insert into";
    // Siddhi Comments
    public static final String STREAMS_COMMENT = "-- Streams";
    public static final String TABLES_COMMENT = "-- Tables";
    public static final String WINDOWS_COMMENT = "-- Windows";
    public static final String TRIGGERS_COMMENT = "-- Triggers";
    public static final String AGGREGATIONS_COMMENT = "-- Aggregations";
    public static final String FUNCTIONS_COMMENT = "-- Functions";
    public static final String QUERIES_COMMENT = "-- Queries";
    public static final String PARTITIONS_COMMENT = "-- Partitions";
    // Other Strings
    public static final String FROM = "from";
    public static final String SELECT = "select";
    public static final String GROUP = "group";
    public static final String ORDER = "order";
    public static final String BY = "by";
    public static final String AS = "as";
    public static final String AT = "at";
    public static final String OF = "of";
    public static final String EVERY = "every";
    public static final String RETURN = "return";
    public static final String LIMIT = "limit";
    public static final String WINDOW = "window";
    public static final String HAVING = "having";
    public static final String OUTPUT = "output";
    public static final String ON = "on";
    public static final String INTO = "into";
    public static final String FOR = "for";
    public static final String SET = "set";
    public static final String UNIDIRECTIONAL = "unidirectional";
    public static final String WITHIN = "within";
    public static final String PER = "per";
    public static final String AGGREGATE = "aggregate";
    public static final String PARTITION_WITH = "partition with";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    private SiddhiStringBuilderConstants() {
    }

}
