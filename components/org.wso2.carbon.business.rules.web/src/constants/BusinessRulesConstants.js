/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

/**
 * Constants related to the Business Rules Manager web app
 */
const BusinessRulesConstants = {
    // Rule Template types
    RULE_TEMPLATE_TYPE_TEMPLATE: 'template',
    RULE_TEMPLATE_TYPE_INPUT: 'input',
    RULE_TEMPLATE_TYPE_OUTPUT: 'output',

    // Mode of Business Rule form
    BUSINESS_RULE_FORM_MODE_CREATE: 'create',
    BUSINESS_RULE_FORM_MODE_EDIT: 'edit',
    BUSINESS_RULE_FORM_MODE_VIEW: 'view',

    // Business Rule types
    BUSINESS_RULE_TYPE_TEMPLATE: 'template',
    BUSINESS_RULE_TYPE_SCRATCH: 'scratch',

    // Business Rule from scratch property types
    INPUT_DATA_KEY: 'inputData',
    OUTPUT_DATA_KEY: 'outputData',
    RULE_COMPONENTS_KEY: 'ruleComponents',
    FILTER_RULES_KEY: 'filterRules',
    RULE_LOGIC_KEY: 'ruleLogic',
    OUTPUT_MAPPINGS_KEY: 'outputMappings',

    BUSINESS_RULE_NAME_REGEX: /[a-zA-Z][a-zA-Z0-9\s_-]*/g,

    // Business Rule deployment statuses
    BUSINESS_RULE_DEPLOYMENT_STATUS_DEPLOYED: 'deployed',
    BUSINESS_RULE_DEPLOYMENT_STATUS_NOT_DEPLOYED: 'notDeployed',

    // Business Rule Filter Rule operators
    BUSINESS_RULE_FILTER_RULE_OPERATORS: ['<', '<=', '>', '>=', '==', '!='],

    SCRIPT_EXECUTION_ERROR: 7, // Script execution has been failed in the backend, due to the provided value(s)

    // User Permissions
    USER_PERMISSIONS: {
        MANAGER: 0,
        VIEWER: 1,
        UNSET: -1,
        NONE: -2,
    },

    // Business Rule deployment statuses
    BUSINESS_RULE_STATUSES: [
        'Deployed', // 0
        'Saved', // 1
        'Partially Deployed', // 2
        'Partially Undeployed', // 3
        'Deployment Failure', // 4
        'Error', // 5
    ],

    // Siddhi App deployment statuses
    SIDDHI_APP_DEPLOYMENT_STATUSES: {
        DEPLOYED: 1,
        NOT_DEPLOYED: 0,
        UNREACHABLE: -1,
    },

    SIDDHI_APP_DEPLOYMENT_STATUS_TEXTS: [
        'Unreachable', // -1
        'Not Deployed', // 0
        'Deployed', // 1
    ],

    ERROR_CODES: {
        UNKNOWN: 0,
        NONE: -1,
    },

    // URL for APIs
    BASE_URL: window.location.origin,
};

export default BusinessRulesConstants;
