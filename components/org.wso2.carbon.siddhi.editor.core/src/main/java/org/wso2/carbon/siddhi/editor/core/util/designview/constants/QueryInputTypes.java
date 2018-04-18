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
 * Has possible values for a query input type
 */
public class QueryInputTypes {
    public static final String WINDOW_FILTER_PROJECTION = "window-filter-projection";
    // Join
    public static final String JOIN = "join";
    public static final String JOIN_STREAM = "stream";
    public static final String JOIN_TABLE = "table";
    public static final String JOIN_AGGREGATION = "aggregation";
    public static final String JOIN_WINDOW = "window";

    // Pattern
    public static final String PATTERN = "pattern";

    // Event Value in Pattern & Sequence
    public static final String COUNTING_EVENT = "counting";
    public static final String AND_OR_EVENT = "andor";
    public static final String NOT_FOR_EVENT = "notfor";
    public static final String NOT_AND_EVENT = "notand";

    // Sequence
    public static final String SEQUENCE = "sequence";

    // Sequence - countingSequence
    public static final String SEQUENCE_COUNTING_SEQUENCE_MIN_MAX = "minMax";
    public static final String SEQUENCE_COUNTING_SEQUENCE_COUNTING_PATTERN = "countingPattern";
}
