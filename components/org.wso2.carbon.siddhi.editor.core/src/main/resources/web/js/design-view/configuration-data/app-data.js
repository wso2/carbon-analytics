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

define(['require', 'elementArray', 'lodash'],
    function (require, ElementArray, _) {

        /**
         * @class AppData
         * @constructor
         * @class AppData  Holds the data for given Siddhi app
         */
        var AppData = function () {
            // initiates the collections
            this.streamList = new ElementArray();
            this.tableList = new ElementArray();
            this.windowList = new ElementArray();
            this.triggerList = new ElementArray();
            this.aggregationList = new ElementArray();
            this.windowFilterProjectionQueryList = new ElementArray();
            this.patternQueryList = new ElementArray();
            this.joinQueryList = new ElementArray();
            this.partitionList = new ElementArray();
            // finalElementCount --> Number of elements that exist on the canvas at the time of saving the model
            this.finalElementCount = 0;

        };

        AppData.prototype.addStream = function (stream) {
            this.streamList.push(stream);
        };

        AppData.prototype.addTable = function (table) {
            this.tableList.push(table);
        };

        AppData.prototype.addWindow = function (window) {
            this.windowList.push(window);
        };

        AppData.prototype.addTrigger = function (trigger) {
            this.triggerList.push(trigger);
        };

        AppData.prototype.addAggregation = function (aggregation) {
            this.aggregationList.push(aggregation);
        };

        AppData.prototype.addWindowFilterProjectionQuery = function (windowFilterProjectionQuery) {
            this.windowFilterProjectionQueryList.push(windowFilterProjectionQuery);
        };

        AppData.prototype.addPatternQuery = function (patternQuery) {
            this.patternQueryList.push(patternQuery);
        };

        AppData.prototype.addJoinQuery = function (joinQuery) {
            this.joinQueryList.push(joinQuery);
        };

        AppData.prototype.addPartition = function (partition) {
            this.partitionList.push(partition);
        };

        AppData.prototype.removeStream = function (streamId) {
            this.streamList.removeElement(streamId);
        };

        AppData.prototype.removeTable = function (tableId) {
            this.tableList.removeElement(tableId);
        };

        AppData.prototype.removeWindow = function (windowId) {
            this.windowList.removeElement(windowId);
        };

        AppData.prototype.removeTrigger = function (triggerId) {
            this.triggerList.removeElement(triggerId);
        };

        AppData.prototype.removeAggregation = function (aggregationId) {
            this.aggregationList.removeElement(aggregationId);
        };

        AppData.prototype.removeWindowFilterProjectionQuery = function (windowFilterProjectionQueryId) {
            this.windowFilterProjectionQueryList.removeElement(windowFilterProjectionQueryId);
        };

        AppData.prototype.removePatternQuery = function (patternQueryId) {
            this.patternQueryList.removeElement(patternQueryId);
        };

        AppData.prototype.removeJoinQuery = function (joinQueryId) {
            this.joinQueryList.removeElement(joinQueryId);
        };

        AppData.prototype.removePartition = function (partitionId) {
            this.partitionList.removeElement(partitionId);
        };

        AppData.prototype.setFinalElementCount = function (finalElementCount) {
            this.finalElementCount = finalElementCount;
        };

        AppData.prototype.getStream = function (streamId) {
            return this.streamList.getElement(streamId);
        };

        AppData.prototype.getTable = function (tableId) {
            return this.tableList.getElement(tableId);
        };

        AppData.prototype.getWindow = function (windowId) {
            return this.windowList.getElement(windowId);
        };

        AppData.prototype.getTrigger = function (triggerId) {
            return this.triggerList.getElement(triggerId);
        };

        AppData.prototype.getAggregation = function (aggregationId) {
            return this.aggregationList.getElement(aggregationId);
        };

        AppData.prototype.getWindowFilterProjectionQuery = function (windowFilterProjectionQueryId) {
            return this.windowFilterProjectionQueryList.getElement(windowFilterProjectionQueryId);
        };

        AppData.prototype.getPatternQuery = function (patternQueryId) {
            return this.patternQueryList.getElement(patternQueryId);
        };

        AppData.prototype.getJoinQuery = function (joinQueryId) {
            return this.joinQueryList.getElement(joinQueryId);
        };

        AppData.prototype.getPartition = function (partitionId) {
            return this.partitionList.getElement(partitionId);
        };

        AppData.prototype.getFinalElementCount = function () {
            return this.finalElementCount;
        };

        /**
         * @function Get the element by providing the element id
         * @param elementId id of the definition element
         * @return requestedElement returns undefined if the requested element is not found
         */
        AppData.prototype.getDefinitionElementById = function (elementId) {
            var self = this;
            var requestedElement = undefined;
            var streamList = self.streamList;
            var tableList = self.tableList;
            var windowList = self.windowList;
            var aggregationList = self.aggregationList;
            var triggerList = self.triggerList;
            var lists = [streamList, tableList, windowList, aggregationList, triggerList];

            _.forEach(lists, function (list) {
                _.forEach(list, function (element) {
                    if (element.getId() === elementId) {
                        var type = '';
                        if (list === streamList) {
                            type = 'stream';
                        } else if (list === tableList) {
                            type = 'table';
                        } else if (list === windowList) {
                            type = 'window';
                        } else if (list === aggregationList) {
                            type = 'aggregation';
                        } else if (list === triggerList) {
                            type = 'trigger';
                        }
                        requestedElement = {
                            type: type,
                            element: element
                        };
                    }
                });
            });

            return requestedElement;
        };

        /**
         * @function Get the element by providing the element name
         * @param elementName name of the definition element
         * @return requestedElement returns undefined if the requested element is not found
         */
        AppData.prototype.getDefinitionElementByName = function (elementName) {
            var self = this;
            var requestedElement = undefined;
            var streamList = self.streamList;
            var tableList = self.tableList;
            var windowList = self.windowList;
            var aggregationList = self.aggregationList;
            var triggerList = self.triggerList;
            var listNames = [streamList, tableList, windowList, aggregationList, triggerList];

            _.forEach(listNames, function (list) {
                _.forEach(list, function (element) {
                    if (element.getName().toUpperCase() === elementName.toUpperCase()) {
                        var type = '';
                        if (list === streamList) {
                            type = 'stream';
                        } else if (list === tableList) {
                            type = 'table';
                        } else if (list === windowList) {
                            type = 'window';
                        } else if (list === aggregationList) {
                            type = 'aggregation';
                        } else if (list === triggerList) {
                            type = 'trigger';
                        }
                        requestedElement = {
                            type: type,
                            element: element
                        };
                    }
                });
            });

            return requestedElement;
        };

        return AppData;
    });
