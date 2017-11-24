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
 * Has values for all the string constants related to displaying messages to the user
 */
const BusinessRulesMessages = {
    // Business Rule form (common)
    SELECT_RULE_TEMPLATE: 'Please select a rule template',
    BUSINESS_RULE_NAME_FIELD_NAME: 'Business rule name',
    BUSINESS_RULE_NAME_FIELD_DESCRIPTION: 'Please enter',
    // Errors
    ALL_FIELDS_REQUIRED_ERROR_TITLE: 'Error submitting your form',
    ALL_FIELDS_REQUIRED_ERROR_CONTENT: 'Please fill in all the required values',
    INVALID_BUSINESS_RULE_NAME: 'Business Rule name is invalid',
    ALL_FIELDS_REQUIRED_ERROR_PRIMARY_BUTTON: 'OK',

    // Business Rule from scratch form
    // Filter component
    RULE_LOGIC_HELPER_TEXT: "Enter the Rule Logic, referring filter rule numbers. Eg: (1 OR 2) AND (NOT(3))",
    RULE_LOGIC_WARNING: "Rule logic contains invalid number(s) for filter rules",
    // Output component
    MAPPING_NOT_AVAILABLE: 'Please select both input & output rule templates',

    BUSINESS_RULE_DELETION_CONFIRMATION_TITLE: 'Confirm delete',
    BUSINESS_RUL_DELETION_CONFIRMATION_CONTENT: 'Do you really want to delete this business rule?',

    // Generalized errors with titles
    CONNECTION_FAILURE_ERROR: ['Connection Failed', 'There was an error connecting to the server'],
    API_FAILURE_ERROR: ['Request Failed', 'There was an error processing your request'],
}

export default BusinessRulesMessages;
