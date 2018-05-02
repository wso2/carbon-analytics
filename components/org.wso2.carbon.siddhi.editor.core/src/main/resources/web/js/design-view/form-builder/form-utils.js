/**
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

define(['require', 'lodash'],
    function (require, _) {

        /**
         * @class FormUtils Contains utility methods for forms
         * @constructor
         * @param {Object} configurationData Siddhi app data
         */
        var FormUtils = function (configurationData) {
            this.configurationData = configurationData;
        };

        /**
         * @function check whether given name to the definition element is unique
         * @param elementName given name to the definition element
         * @param skipElementID this element name will be ignored when checking the unique name. This is used when
         *          saving the same name after editing a particular element
         * @return {boolean}
         */
        FormUtils.prototype.IsDefinitionElementNameUnique = function (elementName, skipElementID) {
            var self = this;
            var isNameUsed = false;
            _.forEach(self.configurationData.getSiddhiAppConfig().streamList, function (stream) {
                if (stream.getName().toUpperCase() === elementName.toUpperCase()) {
                    if (!(skipElementID !== undefined && skipElementID === stream.getId())) {
                        isNameUsed = true;
                    }
                }
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().tableList, function (table) {
                if (table.getName().toUpperCase() === elementName.toUpperCase()) {
                    if (!(skipElementID !== undefined && skipElementID === table.getId())) {
                        isNameUsed = true;
                    }
                }
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().windowList, function (window) {
                if (window.getName().toUpperCase() === elementName.toUpperCase()) {
                    if (!(skipElementID !== undefined && skipElementID === window.getId())) {
                        isNameUsed = true;
                    }
                }
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().aggregationList, function (aggregation) {
                if (aggregation.getName().toUpperCase() === elementName.toUpperCase()) {
                    if (!(skipElementID !== undefined && skipElementID === aggregation.getId())) {
                        isNameUsed = true;
                    }
                }
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().triggerList, function (trigger) {
                if (trigger.getName().toUpperCase() === elementName.toUpperCase()) {
                    if (!(skipElementID !== undefined && skipElementID === trigger.getId())) {
                        isNameUsed = true;
                    }
                }
            });
            return isNameUsed;
        };

        return FormUtils;
    });
