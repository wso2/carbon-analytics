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

package org.wso2.carbon.business.rules.core.internal.util;

/**
 * Consists of constants related to root.Template Manager
 */
public class TemplateManagerConstants {
    // Directory locations
    public static final String TEMPLATES_DIRECTORY = "/home/senthuran/Desktop/rough-templates/"; // todo: not finalized

    // Pattern of templated elements in Templates
    public static final String TEMPLATED_ELEMENT_REGEX_PATTERN = "(\\$\\{[^}]+\\})"; // ${templatedElement}
    public static final String TEMPLATED_ELEMENT_NAME_REGEX_PATTERN = "\\$\\{(\\S+)\\}"; // to extract element name, from [element with template pattern]

    // Pattern of Template names
    public static final String SIDDHI_APP_NAME_REGEX_PATTERN = "@App:name\\[[\\\",\\\'](\\S+)[\\\",\\\']\\]"; // @App:name("SiddhiAppName")

    // Template types
    public static final String SIDDHI_APP_TEMPLATE_TYPE = "siddhiApp";
    public static final String SOURCE = "source";
    public static final String SINK = "sink";
    public static final String GADGET = "gadget";
    public static final String DASHBOARD = "dashboard";


}
