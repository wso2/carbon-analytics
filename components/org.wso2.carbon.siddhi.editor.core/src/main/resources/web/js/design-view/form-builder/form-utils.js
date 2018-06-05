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
        FormUtils.prototype.isDefinitionElementNameUnique = function (elementName, skipElementID) {
            var self = this;
            var isNameUsed = false;
            var streamList = self.configurationData.getSiddhiAppConfig().streamList;
            var tableList = self.configurationData.getSiddhiAppConfig().tableList;
            var windowList = self.configurationData.getSiddhiAppConfig().windowList;
            var aggregationList = self.configurationData.getSiddhiAppConfig().aggregationList;
            var triggerList = self.configurationData.getSiddhiAppConfig().triggerList;

            var listNames = [streamList, tableList, windowList, aggregationList, triggerList];
            _.forEach(listNames, function (list) {
                _.forEach(list, function (element) {
                    if (element.getName().toUpperCase() === elementName.toUpperCase()) {
                        if (!(skipElementID !== undefined && skipElementID === element.getId())) {
                            isNameUsed = true;
                        }
                    }
                });
            });

            return isNameUsed;
        };

        /**
         * @function This method removes undefined, null, empty arrays, empty object property fields from a JSON object
         * @param objectElement object which is needed to be cleaned
         * @return cleaned element
         */
        FormUtils.prototype.cleanJSONObject = function (objectElement) {
            var self = this;
            for (var propertyName in objectElement) {
                if (objectElement.hasOwnProperty(propertyName)
                    && (objectElement[propertyName] === null
                        || (!_.isNumber(objectElement[propertyName]) && !_.isBoolean(objectElement[propertyName])
                            && _.isEmpty(objectElement[propertyName]))
                        || objectElement[propertyName] === undefined)) {
                    delete objectElement[propertyName];
                } else if (objectElement.hasOwnProperty(propertyName)
                    && objectElement[propertyName] instanceof Object) {
                    self.cleanJSONObject(objectElement[propertyName]);
                    if (objectElement.hasOwnProperty(propertyName) && !_.isBoolean(objectElement[propertyName])
                        && _.isEmpty(objectElement[propertyName])) {
                        delete objectElement[propertyName];
                    }
                }
            }
            return objectElement;
        };

        return FormUtils;
    });
