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
 */

import React from 'react';

/**
 * Has values for all the constants related to Business Rules web app
 */
const BusinessRulesConstants = {
    // Rule Template types
    RULE_TEMPLATE_TYPE_TEMPLATE: "template",
    RULE_TEMPLATE_TYPE_INPUT: "input",
    RULE_TEMPLATE_TYPE_OUTPUT: "output",

    // Mode of Business Rule form
    BUSINESS_RULE_FORM_MODE_CREATE: "create",
    BUSINESS_RULE_FORM_MODE_EDIT: "edit",
    BUSINESS_RULE_FORM_MODE_VIEW: "view",

    // Business Rule types
    BUSINESS_RULE_TYPE_TEMPLATE: "template",
    BUSINESS_RULE_TYPE_SCRATCH: "scratch",

    // Business Rule from scratch property types
    BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_INPUT: "inputData",
    BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_OUTPUT: "outputData",
    BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_RULE_COMPONENTS: "ruleComponents",
    BUSINESS_RULE_FROM_SCRATCH_RULE_COMPONENT_PROPERTY_TYPE_FILTER_RULES: "filterRules",
    BUSINESS_RULE_FROM_SCRATCH_RULE_COMPONENT_PROPERTY_TYPE_RULE_LOGIC: "ruleLogic",
    BUSINESS_RULE_FROM_SCRATCH_RULE_PROPERTY_TYPE_OUTPUT_MAPPINGS: "outputMappings",

    // Business Rule deployment statuses
    BUSINESS_RULE_DEPLOYMENT_STATUS_DEPLOYED: "deployed",
    BUSINESS_RULE_DEPLOYMENT_STATUS_NOT_DEPLOYED: "notDeployed",

    // Business Rule Filter Rule operators
    BUSINESS_RULE_FILTER_RULE_OPERATORS: ['<', '<=', '>', '>=', '==', '!='],

    // Business Rule deployment statuses todo: remove this section
    BUSINESS_RULE_STATUS_DEPLOYED: 3,
    BUSINESS_RULE_STATUS_DEPLOYMENT_FAILED: 1, // Tried to save & deploy, but only save was successful
    BUSINESS_RULE_STATUS_NOT_DEPLOYED: 0, // Tried only to save, and was successful todo: check number

    // Business Rule deployment statuses
    BUSINESS_RULE_STATUSES: [ // todo: maintain statuses
        'Deployed', // 0
        'Saved', // 1
        'Partially Deployed', // 2
        'Partially Undeployed', // 3
        'Deployment Failure', // 4
        'Error' // 5
    ],

    BUSINESS_RULE_STATUS_DEPLOYED_STRING: 'Deployed',
    BUSINESS_RULE_STATUS_NOT_DEPLOYED_STRING: 'Not Deployed',
    BUSINESS_RULE_STATUS_DEPLOYMENT_FAILED_STRING: 'Deployment Failed',


    // URL for APIs
    BASE_URL: window.location.origin
}

export default BusinessRulesConstants;
