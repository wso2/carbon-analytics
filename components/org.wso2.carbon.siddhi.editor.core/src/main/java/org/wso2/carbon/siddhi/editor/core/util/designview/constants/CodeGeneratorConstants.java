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
 * Contains generic String constants that are used by the
 * CodeGenerator & CodeGeneratorHelper class
 */
public class CodeGeneratorConstants {

    public static final String SOURCE = "SOURCE";
    public static final String SINK = "SINK";

    public static final String WINDOW = "WINDOW";
    public static final String FILTER = "FILTER";
    public static final String FUNCTION = "FUNCTION";
    public static final String PROJECTION = "PROJECTION";
    public static final String PATTERN = "PATTERN";
    public static final String SEQUENCE = "SEQUENCE";
    public static final String AGGREGATION = "AGGREGATION";

    public static final String JOIN = "JOIN";
    public static final String LEFT_OUTER = "LEFT_OUTER";
    public static final String RIGHT_OUTER = "RIGHT_OUTER";
    public static final String FULL_OUTER = "FULL_OUTER";

    public static final String INSERT = "INSERT";
    public static final String DELETE = "DELETE";
    public static final String UPDATE = "UPDATE";
    public static final String UPDATE_OR_INSERT_INTO = "UPDATE_OR_INSERT_INTO";

    public static final String CURRENT_EVENTS = "CURRENT_EVENTS";
    public static final String EXPIRED_EVENTS = "EXPIRED_EVENTS";
    public static final String ALL_EVENTS = "ALL_EVENTS";

    public static final String PRIMARY_KEY_ANNOTATION = "@primarykey";

    private CodeGeneratorConstants() {
    }

}
