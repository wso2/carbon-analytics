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

package org.wso2.carbon.business.rules.core.util;

import org.wso2.carbon.utils.Utils;

/**
 * Consists of constants related to root.Template Manager
 */
public class TemplateManagerConstants {
    /*
    Pattern of templated elements in Templates
    ${templatedElement}
    */
    public static final String TEMPLATED_ELEMENT_NAME_REGEX_PATTERN = "\\$\\{(\\S+)\\}";
    public static final String TEMPLATED_ELEMENT_PATTERN_PREFIX = "${";
    public static final String TEMPLATED_ELEMENT_PATTERN_SUFFIX = "}";
    /*
    Pattern of Template names
    @App:name("SiddhiAppName")
    */
    public static final String SIDDHI_APP_NAME_REGEX_PATTERN = "@(A|a)pp:name\\([\\\",\\\'](\\S+)[\\\",\\\']\\)";
    // @App:name("SiddhiAppName")
    public static final String SIDDHI_APP_RULE_LOGIC_PATTERN = "(\\d{1,})";
    // Template / Artifact types
    public static final String TEMPLATE_TYPE_SIDDHI_APP = "siddhiApp";
    public static final String TEMPLATE_TYPE_GADGET = "gadget";
    public static final String TEMPLATE_TYPE_DASHBOARD = "dashboard";
    public static final String SIDDHI_APP_TEMPLATE = "@App:name('appName') ${inputTemplate} ${outputTemplate} from " +
            "${inputStreamName} ${logic} select ${mapping} insert into ${outputStreamName}";
    // Rule Template types
    public static final String RULE_TEMPLATE_TYPE_INPUT = "input";
    public static final String RULE_TEMPLATE_TYPE_OUTPUT = "output";
    public static final String RULE_TEMPLATE_TYPE_TEMPLATE = "template";
    //Business Rule parameters
    public static final String BUSINESS_RULE_TYPE = "type";
    public static final String BUSINESS_RULE_UUID = "uuid";
    public static final String BUSINESS_RULE_NAME = "name";
    public static final String BUSINESS_RULE_PROPERTIES = "properties";
    public static final String BUSINESS_RULE_TEMPLATE_GROUP_UUID = "templateGroupUUID";
    public static final String BUSINESS_RULE_RULE_TEMPLATE_UUID = "ruleTemplateUUID";
    public static final String BUSINESS_RULE_INPUT_RULE_TEMPLATE_UUID = "inputRuleTemplateUUID";
    public static final String BUSINESS_RULE_OUTPUT_RULE_TEMPLATE_UUID = "outputRuleTemplateUUID";
    // Business Rule types
    public static final String BUSINESS_RULE_TYPE_TEMPLATE = "template";
    public static final String BUSINESS_RULE_TYPE_SCRATCH = "scratch";

    public static final String INSTANCE_COUNT_ONE = "one";
    public static final String INSTANCE_COUNT_MANY = "many";
    public static final String BUSINESS_RULES = "business.rules";

    public static final int DEPLOYED = 0;
    public static final int SAVED = 1;
    public static final int PARTIALLY_DEPLOYED = 2;
    public static final int PARTIALLY_UNDEPLOYED = 3;
    public static final int DEPLOYMENT_FAILURE = 4;
    public static final int ERROR = 5;

    public static final int SIDDHI_APP_UNREACHABLE = -1;
    public static final int SIDDHI_APP_NOT_DEPLOYED = 0;
    public static final int SIDDHI_APP_DEPLOYED = 1;

    public static final int SUCCESSFULLY_DELETED = 6;
    public static final int SCRIPT_EXECUTION_ERROR = 7;

    // Directory locations
    private static final String CARBON_RUNTIME = Utils.getRuntimePath().toString();
    public static final String TEMPLATES_DIRECTORY = CARBON_RUNTIME + "/resources/businessRules/templates/";

    public static final String ONE = "one";
    public static final String MANY = "many";
}
