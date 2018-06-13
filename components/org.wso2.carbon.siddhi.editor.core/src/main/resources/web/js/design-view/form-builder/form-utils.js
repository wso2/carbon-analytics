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
         * @param {Object} jsPlumbInstance JsPlumb instance of the current tab
         */
        var FormUtils = function (configurationData, jsPlumbInstance) {
            this.configurationData = configurationData;
            this.jsPlumbInstance = jsPlumbInstance;
        };

        /**
         * @function check whether given name to the definition element is used(This will only consider definitions
         * which creates internal streams in Siddhi for each of them. Function definitions are not considered.)
         * @param elementName given name to the definition element
         * @param skipElementID this element name will be ignored when checking the unique name. This is used when
         *          saving the same name after editing a particular element
         * @return {boolean}
         */
        FormUtils.prototype.isDefinitionElementNameUsed = function (elementName, skipElementID) {
            var self = this;
            var isNameUsed = false;
            var streamList = self.configurationData.getSiddhiAppConfig().getStreamList();
            var tableList = self.configurationData.getSiddhiAppConfig().getTableList();
            var windowList = self.configurationData.getSiddhiAppConfig().getWindowList();
            var aggregationList = self.configurationData.getSiddhiAppConfig().getAggregationList();
            var triggerList = self.configurationData.getSiddhiAppConfig().getTriggerList();

            var listNames = [streamList, tableList, windowList, aggregationList, triggerList];
            _.forEach(listNames, function (list) {
                _.forEach(list, function (element) {
                    if (element.getName() === elementName) {
                        if (!(skipElementID !== undefined && skipElementID === element.getId())) {
                            isNameUsed = true;
                        }
                    }
                });
            });

            return isNameUsed;
        };

        /**
         * @function check whether given name to the function definition element is used.
         * @param elementName given name to the definition element
         * @param skipElementID this element name will be ignored when checking the unique name. This is used when
         *          saving the same name after editing a particular element
         * @return {boolean}
         */
        FormUtils.prototype.isFunctionDefinitionElementNameUsed = function (elementName, skipElementID) {
            var self = this;
            var isNameUsed = false;
            var functionList = self.configurationData.getSiddhiAppConfig().getFunctionList();

            _.forEach(functionList, function (element) {
                if (element.getName() === elementName) {
                    if (!(skipElementID !== undefined && skipElementID === element.getId())) {
                        isNameUsed = true;
                    }
                }
            });

            return isNameUsed;
        };

        /**
         * @function check whether given name to the inner stream definition is used in the given partition.
         * @param partitionId id of the partition element
         * @param elementName given name to the definition element
         * @param skipElementID this element name will be ignored when checking the unique name. This is used when
         *          saving the same name after editing a particular element
         * @return {boolean}
         */
        FormUtils.prototype.isStreamDefinitionNameUsedInPartition = function (partitionId, elementName,
                                                                              skipElementID) {
            var self = this;
            var isNameUsed = false;
            var partition = self.configurationData.getSiddhiAppConfig().getPartition(partitionId);
            var streamList = partition.getStreamList();

            _.forEach(streamList, function (element) {
                if (element.getName() === elementName) {
                    if (!(skipElementID !== undefined && skipElementID === element.getId())) {
                        isNameUsed = true;
                    }
                }
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
                        || !objectElement[propertyName])) {
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

        /**
         * @function Builds HTML for form buttons.
         * @param includeCancel boolean Show cancel button
         * @returns {string} HTML string
         */
        FormUtils.prototype.buildFormButtons = function (includeCancel) {
            var html = '<div class="query-form-actions">' +
                '<button type="button" id="btn-submit" class="btn btn-primary">Save</button>';
            if (includeCancel) {
                html += '<button type="button" id="btn-cancel" class="btn btn-default">Cancel</button>';
            }
            html += '</div>';
            return html;
        };

        /**
         * @function Updates connections of a definition element after the element name is changed.
         * @param elementId id of the element
         */
        FormUtils.prototype.updateConnectionsAfterDefinitionElementNameChange = function (elementId) {
            var self = this;

            var outConnections = self.jsPlumbInstance.getConnections({source: elementId + '-out'});
            var inConnections = self.jsPlumbInstance.getConnections({target: elementId + '-in'});

            _.forEach(outConnections, function (connection) {
                self.jsPlumbInstance.deleteConnection(connection);
            });
            _.forEach(inConnections, function (connection) {
                self.jsPlumbInstance.deleteConnection(connection);
            });

            _.forEach(inConnections, function (inConnection) {
                self.jsPlumbInstance.connect({
                    source: inConnection.sourceId,
                    target: inConnection.targetId
                });
            });

            _.forEach(outConnections, function (outConnection) {
                self.jsPlumbInstance.connect({
                    source: outConnection.sourceId,
                    target: outConnection.targetId
                });
            });
        };

        return FormUtils;
    });
