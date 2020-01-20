/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'elementUtils', 'lodash'],
    function (require, ElementUtils, _) {

        /**
         * @class AppData
         * @constructor
         * @class AppData  Holds the data for given Siddhi app
         * @param options options to instantiate the object
         */
        var AppData = function (options) {
            // initiates the collections
            if (options !== undefined) {
                this.siddhiAppName = options.siddhiAppName;
                this.siddhiAppDescription = options.siddhiAppDescription;

                // Following lists hold references for comments in the code view. These lists are passed to the backend
                // when generating the code from design. Front end does not use them.
                this.elementCodeSegments = options.elementCodeSegments;
                this.commentCodeSegments = options.commentCodeSegments;
            }
            this.appAnnotationList = [];
            this.appAnnotationListObjects = [];
            this.streamList = [];
            this.tableList = [];
            this.windowList = [];
            this.triggerList = [];
            this.aggregationList = [];
            this.functionList = [];
            this.partitionList = [];
            this.sourceList = [];
            this.sinkList = [];
            this.queryLists = {
                WINDOW_FILTER_PROJECTION: [],
                PATTERN: [],
                SEQUENCE: [],
                JOIN: []
            };
            // finalElementCount --> Number of elements that exist on the canvas at the time of saving the model
            this.finalElementCount = 0;

        };

        AppData.prototype.addAppAnnotation = function (annotation) {
            this.appAnnotationList.push(annotation);
        };

        AppData.prototype.addAppAnnotationObject = function (annotation) {
            this.appAnnotationListObjects.push(annotation);
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
            this.queryLists.WINDOW_FILTER_PROJECTION.push(windowFilterProjectionQuery);
        };

        AppData.prototype.addPatternQuery = function (patternQuery) {
            this.queryLists.PATTERN.push(patternQuery);
        };

        AppData.prototype.addSequenceQuery = function (sequenceQuery) {
            this.queryLists.SEQUENCE.push(sequenceQuery);
        };

        AppData.prototype.addJoinQuery = function (joinQuery) {
            this.queryLists.JOIN.push(joinQuery);
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

        AppData.prototype.clearAppAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.appAnnotationList);
        };

        AppData.prototype.clearAppAnnotationListObjects = function () {
            ElementUtils.prototype.removeAllElements(this.appAnnotationListObjects);
        };

        AppData.prototype.removeStream = function (streamId) {
            var isStreamDeleted = ElementUtils.prototype.removeElement(this.streamList, streamId);
            if (!isStreamDeleted) {
                isStreamDeleted = this.removeStreamSavedInsideAPartition(streamId);
            }
            return isStreamDeleted;
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
            var isQueryDeleted = ElementUtils.prototype
                .removeElement(this.queryLists.WINDOW_FILTER_PROJECTION, windowFilterProjectionQueryId);
            if (!isQueryDeleted) {
                isQueryDeleted =
                    this.removeQuerySavedInsideAPartition(windowFilterProjectionQueryId,
                        'WINDOW_FILTER_PROJECTION_QUERY');
            }
            return isQueryDeleted;
        };

        AppData.prototype.removePatternQuery = function (patternQueryId) {
            var isQueryDeleted = ElementUtils.prototype.removeElement(this.queryLists.PATTERN, patternQueryId);
            if (!isQueryDeleted) {
                isQueryDeleted = this.removeQuerySavedInsideAPartition(patternQueryId, 'PATTERN_QUERY');
            }
            return isQueryDeleted;
        };

        AppData.prototype.removeSequenceQuery = function (sequenceQueryId) {
            var isQueryDeleted = ElementUtils.prototype.removeElement(this.queryLists.SEQUENCE, sequenceQueryId);
            if (!isQueryDeleted) {
                isQueryDeleted = this.removeQuerySavedInsideAPartition(sequenceQueryId, 'SEQUENCE_QUERY');
            }
            return isQueryDeleted;
        };

        AppData.prototype.removeJoinQuery = function (joinQueryId) {
            var isQueryDeleted = ElementUtils.prototype.removeElement(this.queryLists.JOIN, joinQueryId);
            if (!isQueryDeleted) {
                isQueryDeleted = this.removeQuerySavedInsideAPartition(joinQueryId, 'JOIN_QUERY');
            }
            return isQueryDeleted;
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

        AppData.prototype.getSiddhiAppName = function () {
            return this.siddhiAppName;
        };

        AppData.prototype.getSiddhiAppDescription = function () {
            return this.siddhiAppDescription;
        };

        AppData.prototype.getStream = function (streamId) {
            var returnedElement = ElementUtils.prototype.getElement(this.streamList, streamId);
            if (!returnedElement) {
                returnedElement = this.getStreamSavedInsideAPartition(streamId);
            }
            return returnedElement;
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
            var returnedElement = ElementUtils.prototype
                .getElement(this.queryLists.WINDOW_FILTER_PROJECTION, windowFilterProjectionQueryId);
            if (!returnedElement) {
                returnedElement =
                    this.getQuerySavedInsideAPartition(windowFilterProjectionQueryId, 'WINDOW_FILTER_PROJECTION_QUERY');
            }
            return returnedElement;
        };

        AppData.prototype.getPatternQuery = function (patternQueryId) {
            var returnedElement = ElementUtils.prototype.getElement(this.queryLists.PATTERN, patternQueryId);
            if (!returnedElement) {
                returnedElement = this.getQuerySavedInsideAPartition(patternQueryId, 'PATTERN_QUERY');
            }
            return returnedElement;
        };

        AppData.prototype.getSequenceQuery = function (sequenceQueryId) {
            var returnedElement = ElementUtils.prototype.getElement(this.queryLists.SEQUENCE, sequenceQueryId);
            if (!returnedElement) {
                returnedElement = this.getQuerySavedInsideAPartition(sequenceQueryId, 'SEQUENCE_QUERY');
            }
            return returnedElement;
        };

        AppData.prototype.getJoinQuery = function (joinQueryId) {
            var returnedElement = ElementUtils.prototype.getElement(this.queryLists.JOIN, joinQueryId);
            if (!returnedElement) {
                returnedElement = this.getQuerySavedInsideAPartition(joinQueryId, 'JOIN_QUERY');
            }
            return returnedElement;
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

        AppData.prototype.getAppAnnotationList = function () {
            return this.appAnnotationList;
        };

        AppData.prototype.getAppAnnotationListObjects = function () {
            return this.appAnnotationListObjects;
        };

        AppData.prototype.getStreamList = function () {
            return this.streamList;
        };

        AppData.prototype.getTableList = function () {
            return this.tableList;
        };

        AppData.prototype.getWindowList = function () {
            return this.windowList;
        };

        AppData.prototype.getTriggerList = function () {
            return this.triggerList;
        };

        AppData.prototype.getAggregationList = function () {
            return this.aggregationList;
        };

        AppData.prototype.getFunctionList = function () {
            return this.functionList;
        };

        AppData.prototype.getWindowFilterProjectionQueryList = function () {
            return this.queryLists.WINDOW_FILTER_PROJECTION;
        };

        AppData.prototype.getPatternQueryList = function () {
            return this.queryLists.PATTERN;
        };

        AppData.prototype.getSequenceQueryList = function () {
            return this.queryLists.SEQUENCE;
        };

        AppData.prototype.getJoinQueryList = function () {
            return this.queryLists.JOIN;
        };

        AppData.prototype.getPartitionList = function () {
            return this.partitionList;
        };

        AppData.prototype.getSourceList = function () {
            return this.sourceList;
        };

        AppData.prototype.getSinkList = function () {
            return this.sinkList;
        };

        AppData.prototype.getFinalElementCount = function () {
            return this.finalElementCount;
        };

        AppData.prototype.setSiddhiAppName = function (siddhiAppName) {
            this.siddhiAppName = siddhiAppName;
        };

        AppData.prototype.setSiddhiAppDescription = function (siddhiAppDescription) {
            this.siddhiAppDescription = siddhiAppDescription;
        };

        AppData.prototype.setAppAnnotationList = function (appAnnotationList) {
            this.appAnnotationList = appAnnotationList;
        };

        AppData.prototype.setAppAnnotationListObjects = function (appAnnotationListObjects) {
            this.appAnnotationListObjects = appAnnotationListObjects;
        };


        /**
         * @function Get the element by providing the element id
         * @param elementId id of the definition element
         * @param includeQueryTypes if true search in the queries as well
         * @param includeSourceAndSink if true search in the sinks and sources as well
         * @param includePartitions if true search in the partition list as well
         * @return requestedElement returns undefined if the requested element is not found
         */
        AppData.prototype.getDefinitionElementById = function (elementId, includeQueryTypes, includeSourceAndSink,
                                                               includePartitions) {
            var self = this;
            var requestedElement;
            var streamList = self.streamList;
            var tableList = self.tableList;
            var windowList = self.windowList;
            var aggregationList = self.aggregationList;
            var functionList = self.functionList;
            var triggerList = self.triggerList;
            var windowFilterProjectionQueryList = self.queryLists.WINDOW_FILTER_PROJECTION;
            var patternQueryList = self.queryLists.PATTERN;
            var sequenceQueryList = self.queryLists.SEQUENCE;
            var joinQueryList = self.queryLists.JOIN;
            var sourceList = self.sourceList;
            var sinkList = self.sinkList;
            var partitionList = self.partitionList;

            var lists = [streamList, tableList, windowList, aggregationList, functionList, triggerList];

            if (includeQueryTypes !== undefined && includeQueryTypes) {
                lists.push(windowFilterProjectionQueryList);
                lists.push(patternQueryList);
                lists.push(sequenceQueryList);
                lists.push(joinQueryList);
            }
            if (includeSourceAndSink !== undefined && includeSourceAndSink) {
                lists.push(sourceList);
                lists.push(sinkList);
            }
            if (includePartitions !== undefined && includePartitions) {
                lists.push(partitionList);
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
                        } else if (list === sourceList) {
                            type = 'SOURCE';
                        } else if (list === sinkList) {
                            type = 'SINK';
                        } else if (list === partitionList) {
                            type = 'PARTITION';
                        }
                        requestedElement = {
                            type: type,
                            element: element
                        };
                    }
                });
            });

            // check the element in queries inside the partitions
            if (includeQueryTypes !== undefined && includeQueryTypes && !requestedElement) {
                requestedElement = self.getQueryByIdSavedInsideAPartition(elementId);
            }
            // search in the inner streams in partitions
            if (!requestedElement) {
                var element = self.getStreamSavedInsideAPartition(elementId);
                if (element !== undefined) {
                    requestedElement = {
                        type: 'STREAM',
                        element: element
                    };
                }
            }

            return requestedElement;
        };

        /**
         * @function Get the element by providing the element name, If partitionId parameter is provided the provided
         * element name will be searched inside the given partition as well.
         * @param elementName name of the definition element
         * @param partitionId partition Id which the element needed to be searched
         * @return requestedElement returns undefined if the requested element is not found
         */
        AppData.prototype.getDefinitionElementByName = function (elementName, partitionId) {
            var self = this;
            var requestedElement;
            var streamList = self.streamList;
            var tableList = self.tableList;
            var windowList = self.windowList;
            var aggregationList = self.aggregationList;
            var functionList = self.functionList;
            var triggerList = self.triggerList;

            var listNames = [streamList, tableList, windowList, aggregationList, functionList, triggerList];

            _.forEach(listNames, function (list) {
                _.forEach(list, function (element) {
                    if (element.getName() === elementName) {
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
                        }
                        requestedElement = {
                            type: type,
                            element: element
                        };
                    }
                });
            });

            // check the element in the given partition
            if (!requestedElement && partitionId !== undefined) {
                var partition = self.getPartition(partitionId);
                // Only stream elements are in the partition which has a name attribute. So we search name only in
                // streamsList.
                var element = partition.getStreamByName(elementName);
                requestedElement = {
                    type: 'STREAM',
                    element: element
                };
            }

            return requestedElement;
        };

        /**
         * @function Checks whether a given query is inside a partition and if yes it returns
         * @param queryId id of the query element
         * @param queryType type of the query
         * @return requestedElement returns undefined if the requested element is not found. Otherwise returns the
         * requestedElement
         */
        AppData.prototype.getQuerySavedInsideAPartition = function (queryId, queryType) {
            var self = this;
            var requestedElement;
            _.forEach(self.partitionList, function (partition) {
                if (!requestedElement) {
                    if (queryType === 'WINDOW_FILTER_PROJECTION_QUERY') {
                        requestedElement = partition.getWindowFilterProjectionQuery(queryId);
                    } else if (queryType === 'PATTERN_QUERY') {
                        requestedElement = partition.getPatternQuery(queryId);
                    } else if (queryType === 'SEQUENCE_QUERY') {
                        requestedElement = partition.getSequenceQuery(queryId);
                    } else if (queryType === 'JOIN_QUERY') {
                        requestedElement = partition.getJoinQuery(queryId);
                    }
                }
            });
            return requestedElement;
        };

        /**
         * @function Checks whether a given stream is inside a partition and if yes it returns
         * @param streamId id of the query element
         * @return requestedElement returns undefined if the requested element is not found. Otherwise returns the
         * requestedElement
         */
        AppData.prototype.getStreamSavedInsideAPartition = function (streamId) {
            var self = this;
            var requestedElement;
            _.forEach(self.partitionList, function (partition) {
                if (!requestedElement) {
                    requestedElement = partition.getStream(streamId);
                }
            });
            return requestedElement;
        };

        /**
         * @function Checks whether a given query is inside a partition and if yes removes it
         * @param queryId id of the query element
         * @param queryType type of the query
         * @return boolean returns false if the element is not found.
         */
        AppData.prototype.removeQuerySavedInsideAPartition = function (queryId, queryType) {
            var self = this;
            var isQueryDeleted = false;
            _.forEach(self.partitionList, function (partition) {
                if (!isQueryDeleted) {
                    if (queryType === 'WINDOW_FILTER_PROJECTION_QUERY') {
                        isQueryDeleted = partition.removeWindowFilterProjectionQuery(queryId);
                    } else if (queryType === 'PATTERN_QUERY') {
                        isQueryDeleted = partition.removePatternQuery(queryId);
                    } else if (queryType === 'SEQUENCE_QUERY') {
                        isQueryDeleted = partition.removeSequenceQuery(queryId);
                    } else if (queryType === 'JOIN_QUERY') {
                        isQueryDeleted = partition.removeJoinQuery(queryId);
                    }
                }
            });
            return isQueryDeleted;
        };

        /**
         * @function Checks whether a given stream is inside a partition and if yes removes it
         * @param streamId id of the stream element
         * @return boolean returns false if the element is not found.
         */
        AppData.prototype.removeStreamSavedInsideAPartition = function (streamId) {
            var self = this;
            var isStreamDeleted = false;
            _.forEach(self.partitionList, function (partition) {
                if (!isStreamDeleted) {
                    isStreamDeleted = partition.removeStream(streamId);
                }
            });
            return isStreamDeleted;
        };

        /**
         * @function Checks whether a given query is inside a partition by id and if yes it returns a object with type
         * and element (ex: {type: type, element: element})
         * @param queryId id of the query element
         * @return requestedElement returns undefined if the requested element is not found. Otherwise returns the
         * requestedElement
         */
        AppData.prototype.getQueryByIdSavedInsideAPartition = function (queryId) {
            var self = this;
            var element;
            var type;
            _.forEach(self.partitionList, function (partition) {
                if (!element) {
                    element = partition.getWindowFilterProjectionQuery(queryId);
                    type = 'WINDOW_FILTER_PROJECTION_QUERY';
                    if (!element) {
                        element = partition.getPatternQuery(queryId);
                        type = 'PATTERN_QUERY';
                    }
                    if (!element) {
                        element = partition.getSequenceQuery(queryId);
                        type = 'SEQUENCE_QUERY';
                    }
                    if (!element) {
                        element = partition.getJoinQuery(queryId);
                        type = 'JOIN_QUERY';
                    }
                }
            });

            var requestedElement;
            if (element !== undefined) {
                requestedElement = {
                    type: type,
                    element: element
                };
            }
            return requestedElement;
        };

        /**
         * @function Checks whether a given query is saved inside a partition and if yes it returns the partition Object
         * @param queryId id of the query element
         * @return requestedElement returns undefined if the requested element is not found. Otherwise returns the
         * requestedElement
         */
        AppData.prototype.getPartitionWhereQueryIsSaved = function (queryId) {
            var self = this;
            var requestedElement;
            _.forEach(self.partitionList, function (partition) {
                if (!requestedElement) {
                    if (partition.getWindowFilterProjectionQuery(queryId) !== undefined) {
                        requestedElement = partition;
                    } else if (partition.getPatternQuery(queryId) !== undefined) {
                        requestedElement = partition;
                    } else if (partition.getSequenceQuery(queryId) !== undefined) {
                        requestedElement = partition;
                    } else if (partition.getJoinQuery(queryId) !== undefined) {
                        requestedElement = partition;
                    }
                }
            });

            return requestedElement;
        };

        /**
         * @function Checks whether a given stream is saved inside a partition and if yes it returns the partition
         * Object
         * @param streamId id of the stream element
         * @return requestedElement returns undefined if the requested element is not found. Otherwise returns the
         * requestedElement
         */
        AppData.prototype.getPartitionWhereStreamIsSaved = function (streamId) {
            var self = this;
            var requestedElement;
            _.forEach(self.partitionList, function (partition) {
                if (!requestedElement && partition.getStream(streamId) !== undefined) {
                    requestedElement = partition;
                }
            });

            return requestedElement;
        };

        return AppData;
    });
