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

import BusinessRulesConstants from '../constants/BusinessRulesConstants';

/**
 * Utility functions for the Business Rules Manager app
 */
export default class BusinessRulesUtilityFunctions {
    /**
     * Generates UUID for the given name of a business rule
     * @param {String} businessRuleName     Name of the business rule
     * @returns {String}                    UUID for the business rule
     */
    static generateBusinessRuleUUID(businessRuleName) {
        return businessRuleName.toLowerCase().split(' ').join('-');
    }

    /**
     * Checks whether a given object is empty or not
     * @param {Object} object       Object, which is to be checked whether empty or not
     * @returns {boolean}           Whether the object is empty or not
     */
    static isEmpty(object) {
        for (const key in object) {
            if (object.hasOwnProperty(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the error code for displaying Error
     * @param {Object} error        Erroneous response
     * @returns {number}            Error number
     */
    static getErrorDisplayCode(error) {
        let errorCode;
        if (error.response != null) {
            switch (error.response.status) {
                case 401:
                case 403:
                case 500:
                    errorCode = error.response.status;
                    break;
                default:
                    errorCode = BusinessRulesConstants.ERROR_CODES.UNKNOWN;
            }
        } else {
            errorCode = BusinessRulesConstants.ERROR_CODES.UNKNOWN;
        }
        return errorCode;
    }

    /**
     * Generates GUID for authentication purposes
     * @returns {String}        GUID
     */
    static generateGUID() {
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }

        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    }
}
