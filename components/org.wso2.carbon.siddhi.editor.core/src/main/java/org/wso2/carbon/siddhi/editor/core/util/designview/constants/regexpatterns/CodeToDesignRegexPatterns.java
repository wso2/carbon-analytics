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

package org.wso2.carbon.siddhi.editor.core.util.designview.constants.regexpatterns;

/**
 * Has Regex pattern string constants, related to Code to Design
 */
public class CodeToDesignRegexPatterns {
    public static final String SOURCE_ANNOTATION_CONTENT = "@source\\s*\\(\\s*(.+)\\s*\\)[^.]";
    public static final String SOURCE_SINK_MAP_ANNOTATION_CONTENT = "@map\\s*\\(\\s*(.+)\\s*\\)";
    public static final String MAP_TYPE = "type\\s*=\\'\\s*(.+)\\s*\\'\\s*[,)]";

    /**
     * Avoids Instantiation
     */
    private CodeToDesignRegexPatterns() {
    }
}
