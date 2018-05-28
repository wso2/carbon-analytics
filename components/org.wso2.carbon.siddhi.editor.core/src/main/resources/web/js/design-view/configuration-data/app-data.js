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

define(['require', 'elementUtils', 'lodash'],
    function (require, ElementUtils, _) {

        /**
         * @class AppData
         * @constructor
         * @class AppData  Holds the data for given Siddhi app
         */
        var AppData = function () {
            // initiates the collections
            this.streamList = [];
            this.tableList = [];
            this.windowList = [];
            this.triggerList = [];
            this.aggregationList = [];
            this.functionList = [];
            this.windowFilterProjectionQueryList = [];
            this.patternQueryList = [];
            this.sequenceQueryList = [];
            this.joinQueryList = [];
            this.partitionList = [];
            this.sourceList = [];
            this.sinkList = [];
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

        AppData.prototype.addFunction = function (functionObject) {
            this.functionList.push(functionObject);
        };

        AppData.prototype.addWindowFilterProjectionQuery = function (windowFilterProjectionQuery) {
            this.windowFilterProjectionQueryList.push(windowFilterProjectionQuery);
        };

        AppData.prototype.addPatternQuery = function (patternQuery) {
            this.patternQueryList.push(patternQuery);
        };

        AppData.prototype.addSequenceQuery = function (sequenceQuery) {
            this.sequenceQueryList.push(sequenceQuery);
        };

        AppData.prototype.addJoinQuery = function (joinQuery) {
            this.joinQueryList.push(joinQuery);
        };

        AppData.prototype.addPartition = function (partition) {
            this.partitionList.push(partition);
        };

        AppData.prototype.addSource = function (source) {
            this.sourceList.push(source);
        };

        AppData.prototype.addSink = function (sink) {
            this.sinkList.push(sink);
        };

        AppData.prototype.removeStream = function (streamId) {
            ElementUtils.prototype.removeElement(this.streamList, streamId);
        };

        AppData.prototype.removeTable = function (tableId) {
            ElementUtils.prototype.removeElement(this.tableList, tableId);
        };

        AppData.prototype.removeWindow = function (windowId) {
            ElementUtils.prototype.removeElement(this.windowList, windowId);
        };

        AppData.prototype.removeTrigger = function (triggerId) {
            ElementUtils.prototype.removeElement(this.triggerList, triggerId);
        };

        AppData.prototype.removeAggregation = function (aggregationId) {
            ElementUtils.prototype.removeElement(this.aggregationList, aggregationId);
        };

        AppData.prototype.removeFunction = function (functionId) {
            ElementUtils.prototype.removeElement(this.functionList, functionId);
        };

        AppData.prototype.removeWindowFilterProjectionQuery = function (windowFilterProjectionQueryId) {
            ElementUtils.prototype.removeElement(this.windowFilterProjectionQueryList, windowFilterProjectionQueryId);
        };

        AppData.prototype.removePatternQuery = function (patternQueryId) {
            ElementUtils.prototype.removeElement(this.patternQueryList, patternQueryId);
        };

        AppData.prototype.removeSequenceQuery = function (sequenceQueryId) {
            ElementUtils.prototype.removeElement(this.sequenceQueryList, sequenceQueryId);
        };

        AppData.prototype.removeJoinQuery = function (joinQueryId) {
            ElementUtils.prototype.removeElement(this.joinQueryList, joinQueryId);
        };

        AppData.prototype.removePartition = function (partitionId) {
            ElementUtils.prototype.removeElement(this.partitionList, partitionId);
        };

        AppData.prototype.removeSource = function (sourceId) {
            ElementUtils.prototype.removeElement(this.sourceList, sourceId);
        };

        AppData.prototype.removeSink = function (sinkId) {
            ElementUtils.prototype.removeElement(this.sinkList, sinkId);
        };

        AppData.prototype.setFinalElementCount = function (finalElementCount) {
            this.finalElementCount = finalElementCount;
        };

        AppData.prototype.getStream = function (streamId) {
            return ElementUtils.prototype.getElement(this.streamList, streamId);
        };

        AppData.prototype.getTable = function (tableId) {
            return ElementUtils.prototype.getElement(this.tableList, tableId);
        };

        AppData.prototype.getWindow = function (windowId) {
            return ElementUtils.prototype.getElement(this.windowList, windowId);
        };

        AppData.prototype.getTrigger = function (triggerId) {
            return ElementUtils.prototype.getElement(this.triggerList, triggerId);
        };

        AppData.prototype.getAggregation = function (aggregationId) {
            return ElementUtils.prototype.getElement(this.aggregationList, aggregationId);
        };

        AppData.prototype.getFunction = function (functionId) {
            return ElementUtils.prototype.getElement(this.functionList, functionId);
        };

        AppData.prototype.getWindowFilterProjectionQuery = function (windowFilterProjectionQueryId) {
            return ElementUtils.prototype.getElement(this.windowFilterProjectionQueryList, windowFilterProjectionQueryId);
        };

        AppData.prototype.getPatternQuery = function (patternQueryId) {
            return ElementUtils.prototype.getElement(this.patternQueryList, patternQueryId);
        };

        AppData.prototype.getSequenceQuery = function (sequenceQueryId) {
            return ElementUtils.prototype.getElement(this.sequenceQueryList, sequenceQueryId);
        };

        AppData.prototype.getJoinQuery = function (joinQueryId) {
            return ElementUtils.prototype.getElement(this.joinQueryList, joinQueryId);
        };

        AppData.prototype.getPartition = function (partitionId) {
            return ElementUtils.prototype.getElement(this.partitionList, partitionId);
        };

        AppData.prototype.getSource = function (sourceId) {
            return ElementUtils.prototype.getElement(this.sourceList, sourceId);
        };

        AppData.prototype.getSink = function (sinkId) {
            return ElementUtils.prototype.getElement(this.sinkList, sinkId);
        };

        AppData.prototype.getFinalElementCount = function () {
            return this.finalElementCount;
        };

        /**
         * @function Get the element by providing the element id
         * @param elementId id of the definition element
         * @param includeQueryTypes if true search in the queries as well
         * @return requestedElement returns undefined if the requested element is not found
         */
        AppData.prototype.getDefinitionElementById = function (elementId, includeQueryTypes) {
            var self = this;
            var requestedElement = undefined;
            var streamList = self.streamList;
            var tableList = self.tableList;
            var windowList = self.windowList;
            var aggregationList = self.aggregationList;
            var functionList = self.functionList;
            var triggerList = self.triggerList;
            var windowFilterProjectionQueryList = self.windowFilterProjectionQueryList;
            var patternQueryList = self.patternQueryList;
            var sequenceQueryList = self.sequenceQueryList;
            var joinQueryList = self.joinQueryList;

            var lists;
            if (includeQueryTypes !== undefined && includeQueryTypes) {
                lists = [streamList, tableList, windowList, aggregationList, functionList, triggerList,
                    windowFilterProjectionQueryList, patternQueryList, sequenceQueryList, joinQueryList];
            } else {
                lists = [streamList, tableList, windowList, aggregationList, functionList, triggerList];
            }

            _.forEach(lists, function (list) {
                _.forEach(list, function (element) {
                    if (element.getId() === elementId) {
                        var type = '';
                        if (list === streamList) {
                            type = 'STREAM';
                        } else if (list === tableList) {
                            type = 'TABLE';
                        } else if (list === windowList) {
                            type = 'WINDOW';
                        } else if (list === aggregationList) {
                            type = 'AGGREGATION';
                        } else if (list === functionList) {
                            type = 'FUNCTION';
                        } else if (list === triggerList) {
                            type = 'TRIGGER';
                        } else if (list === windowFilterProjectionQueryList) {
                            type = 'WINDOW_FILTER_PROJECTION_QUERY';
                        } else if (list === patternQueryList) {
                            type = 'PATTERN_QUERY';
                        } else if (list === sequenceQueryList) {
                            type = 'SEQUENCE_QUERY';
                        } else if (list === joinQueryList) {
                            type = 'JOIN_QUERY';
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
         * @param includeQueryTypes if true search in the queries as well
         * @return requestedElement returns undefined if the requested element is not found
         */
        AppData.prototype.getDefinitionElementByName = function (elementName, includeQueryTypes) {
            var self = this;
            var requestedElement = undefined;
            var streamList = self.streamList;
            var tableList = self.tableList;
            var windowList = self.windowList;
            var aggregationList = self.aggregationList;
            var functionList = self.functionList;
            var triggerList = self.triggerList;
            var windowFilterProjectionQueryList = self.windowFilterProjectionQueryList;
            var patternQueryList = self.patternQueryList;
            var sequenceQueryList = self.sequenceQueryList;
            var joinQueryList = self.joinQueryList;

            var listNames;
            if (includeQueryTypes !== undefined && includeQueryTypes) {
                listNames = [streamList, tableList, windowList, aggregationList, functionList, triggerList,
                    windowFilterProjectionQueryList, patternQueryList, sequenceQueryList, joinQueryList];
            } else {
                listNames = [streamList, tableList, windowList, aggregationList, functionList, triggerList];
            }

            _.forEach(listNames, function (list) {
                _.forEach(list, function (element) {
                    if (element.getName().toUpperCase() === elementName.toUpperCase()) {
                        var type = '';
                        if (list === streamList) {
                            type = 'STREAM';
                        } else if (list === tableList) {
                            type = 'TABLE';
                        } else if (list === windowList) {
                            type = 'WINDOW';
                        } else if (list === aggregationList) {
                            type = 'AGGREGATION';
                        } else if (list === functionList) {
                            type = 'FUNCTION';
                        } else if (list === triggerList) {
                            type = 'TRIGGER';
                        } else if (list === windowFilterProjectionQueryList) {
                            type = 'WINDOW_FILTER_PROJECTION_QUERY';
                        } else if (list === patternQueryList) {
                            type = 'PATTERN_QUERY';
                        } else if (list === sequenceQueryList) {
                            type = 'SEQUENCE_QUERY';
                        } else if (list === joinQueryList) {
                            type = 'JOIN_QUERY';
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
