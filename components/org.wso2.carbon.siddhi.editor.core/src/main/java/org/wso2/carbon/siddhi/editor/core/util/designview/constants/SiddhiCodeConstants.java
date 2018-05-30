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
 * Contains all the generic string/char values that are needed by
 * the CodeGenerator class to build the entire Siddhi app string
 */
public class SiddhiCodeConstants {
    // TODO: 5/25/18 Have better names for the constants
    // TODO: 5/3/18 Change the name of the constants class to something else
    public static final char ALL = '*';
    public static final char CLOSE_BRACKET = ')';
    public static final char CLOSE_SQUARE_BRACKET = ']';
    public static final char COMMA = ',';
    public static final char EQUALS = '=';
    public static final char FULL_STOP = '.';
    public static final char HASH = '#';
    public static final char NEW_LINE = '\n';
    public static final char OPEN_BRACKET = '(';
    public static final char OPEN_SQUARE_BRACKET = '[';
    public static final char OPEN_CURLY_BRACKET = '{';
    public static final char CLOSE_CURLY_BRACKET = '}';
    public static final char SEMI_COLON = ';';
    public static final char SINGLE_QUOTE = '\'';
    public static final char SPACE = ' ';

    public static final String TAB_SPACE = "    ";
    public static final String THRIPPLE_DOTS = "...";

    public static final String DEFINE_STREAM = "define stream";
    public static final String DEFINE_TABLE = "define table";
    public static final String DEFINE_WINDOW = "define window";
    public static final String DEFINE_TRIGGER = "define trigger";
    public static final String DEFINE_AGGREGATION = "define aggregation";
    public static final String DEFINE_FUNCTION = "define function";

    // TODO: 5/2/18 combine 'OUTPUT_<VALUE>_EVENTS' with 'OUTPUT + SPACE + <VALUE>_EVENTS'
    public static final String OUTPUT_CURRENT_EVENTS = "output current events";
    public static final String OUTPUT_EXPIRED_EVENTS = "output expired events";
    public static final String OUTPUT_ALL_EVENTS = "output all events";

    public static final String CURRENT_EVENTS = "current events";
    public static final String EXPIRED_EVENTS = "expired events";
    public static final String ALL_EVENTS = "all events";

    public static final String FROM = "from";
    public static final String SELECT = "select";
    public static final String AT = "at";
    public static final String AS = "as";

    // TODO: 5/22/18 Combine GROUP_BY & ORDER_BY with GROUP + BY & ORDER + BY
    public static final String GROUP_BY = "group by";
    public static final String GROUP = "group";
    public static final String ORDER_BY = "order by";
    public static final String ORDER = "order";

    public static final String EVERY = "every";
    public static final String RETURN = "return";
    public static final String LIMIT = "limit";
    public static final String WINDOW = "window";
    public static final String SOURCE = "@source(type='";
    public static final String SINK = "@sink(type='";
    public static final String STORE = "@store(type='";
    public static final String MAP = "@map(type='";
    public static final String ATTRIBUTES = "@attributes(";
    public static final String PAYLOAD = "@payload(";
    public static final String HAVING = "having";
    public static final String OUTPUT = "output";
    public static final String ON = "on";
    public static final String INSERT = "insert";
    public static final String INTO = "into";
    public static final String DELETE = "delete";
    public static final String FOR = "for";
    public static final String UPDATE = "update";
    public static final String UPDATE_OR_INSERT_INTO = "update or insert into";
    public static final String SET = "set";
    public static final String UNIDIRECTIONAL = "unidirectional";
    public static final String JOIN = "join";
    public static final String LEFT_OUTER_JOIN = "left outer join";
    public static final String RIGHT_OUTER_JOIN = "right outer join";
    public static final String FULL_OUTER_JOIN = "full outer join";
    public static final String WITHIN = "within";
    public static final String PER = "per";
    public static final String APP_NAME = "@App:name('";
    public static final String APP_DESCRIPTION = "@App:description('";
    public static final String AGGREGATE = "aggregate";
    public static final String BY = "by";
    public static final String PARTITION_WITH = "partition with";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    public static final String DEFAULT_APP_NAME = APP_NAME + "SiddhiApp" + SINGLE_QUOTE + CLOSE_BRACKET;
    public static final String DEFAULT_APP_DESCRIPTION = APP_DESCRIPTION + "Description of the plan"
            + SINGLE_QUOTE + CLOSE_BRACKET;

    public static final String EMPTY_STRING = "";

    private SiddhiCodeConstants() {
    }

    // TODO: 5/25/18 Change the name of this class to SiddhiSyntax and create a new class for SiddhiCodeConstants

    // TODO: 5/4/18 Add another constants class for values that are not used to create the siddhi app
    // use these values for things like switch cases

}
