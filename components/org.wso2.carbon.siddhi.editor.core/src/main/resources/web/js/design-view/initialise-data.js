/*
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

define(['require', 'log', 'lodash', 'jquery', 'configurationData', 'appData', 'partition', 'query', 'stream', 'table',
        'window', 'trigger', 'aggregation', 'aggregateByTimePeriod', 'windowFilterProjectionQueryInput',
        'queryWindowOrFunction', 'edge', 'querySelect', 'queryOrderByValue', 'queryOutput', 'queryOutputInsert',
        'queryOutputDelete', 'queryOutputUpdate', 'queryOutputUpdateOrInsertInto', 'attribute', 'joinQueryInput',
        'joinQuerySource', 'patternOrSequenceQueryInput', 'patternOrSequenceQueryCondition', 'sourceOrSinkAnnotation',
        'mapAnnotation', 'functionDefinition', 'streamHandler', 'storeAnnotation', 'partitionWith', 'designViewUtils'],
    function (require, log, _, $, ConfigurationData, AppData, Partition, Query, Stream, Table, Window, Trigger,
              Aggregation, AggregateByTimePeriod, WindowFilterProjectionQueryInput, QueryWindowOrFunction, Edge,
              QuerySelect, QueryOrderByValue, QueryOutput, QueryOutputInsert, QueryOutputDelete, QueryOutputUpdate,
              QueryOutputUpdateOrInsertInto, Attribute, JoinQueryInput, JoinQuerySource, PatternOrSequenceQueryInput,
              PatternOrSequenceQueryCondition, SourceOrSinkAnnotation, MapAnnotation, FunctionDefinition, StreamHandler,
              StoreAnnotation, PartitionWith, DesignViewUtils) {

        /**
         * @class InitialiseDataStructure
         * @constructor
         * @class InitialiseDataStructure  Initialise the data structure with the json sent from the backend
         * @param parentElement parent element of the view
         */
        var InitialiseDataStructure = function (parentElement) {
            this._$parent_el = parentElement;
        };

        /**
         * @function Initializes the AppData object with th provided configuration
         * @param {Object} configurationJSON configuration data to initialise the data structure
         */
        InitialiseDataStructure.prototype.initialiseSiddhiAppData = function (configurationJSON) {
            var self = this;
            var currentTabId = self._$parent_el.attr('id');
            self.newIdBeginningPhrase = currentTabId + "_element_";
            self.appData = new AppData();
            self.configurationData = new ConfigurationData(self.appData);

            // add definitions to the data storing structure
            addSourceDefinitions(self.appData, configurationJSON.siddhiAppConfig.sourceList);
            addSinkDefinitions(self.appData, configurationJSON.siddhiAppConfig.sinkList);
            addStreamDefinitions(self.appData, configurationJSON.siddhiAppConfig.streamList);
            addTableDefinitions(self.appData, configurationJSON.siddhiAppConfig.tableList);
            addWindowDefinitions(self.appData, configurationJSON.siddhiAppConfig.windowList);
            addTriggerDefinitions(self.appData, configurationJSON.siddhiAppConfig.triggerList);
            addAggregationDefinitions(self.appData, configurationJSON.siddhiAppConfig.aggregationList);
            addFunctionDefinitions(self.appData, configurationJSON.siddhiAppConfig.functionList);

            // add queries to the data storing structure
            addWindowFilterProjectionQueries(self.appData,
                configurationJSON.siddhiAppConfig.queryLists.WINDOW_FILTER_PROJECTION);
            addPatternQueries(self.appData, configurationJSON.siddhiAppConfig.queryLists.PATTERN);
            addSequenceQueries(self.appData, configurationJSON.siddhiAppConfig.queryLists.SEQUENCE);
            addJoinQueries(self.appData, configurationJSON.siddhiAppConfig.queryLists.JOIN);

            // add partitions to the data storing structure
            addPartitions(self.appData, configurationJSON.siddhiAppConfig.partitionList);

            // add edges to the data storing structure
            addEdges(self.configurationData, configurationJSON.edgeList);

            // re-shuffle edgeList to bring forward edges which have 'PARTITION' as child elements
            self.configurationData.getEdgeList().sort(function (a, b) {
                if (a.getChildType() === 'PARTITION' && b.getChildType() !== 'PARTITION') {
                    return 0;
                } else if (a.getChildType() !== 'PARTITION' && b.getChildType() === 'PARTITION') {
                    return 1;
                } else {
                    return 0;
                }
            });

            return self.configurationData;
        };

        function addSourceDefinitions(mainObject, sourceList) {
            var self = this;
            _.forEach(sourceList, function (source) {
                var sourceObject = new SourceOrSinkAnnotation(source);
                sourceObject.setId(self.newIdBeginningPhrase + sourceObject.getId());
                if (_.isEmpty(source.map)) {
                    sourceObject.setMap(undefined);
                } else {
                    var mapperObject = new MapAnnotation(source.map);
                    sourceObject.setMap(mapperObject);
                }
                mainObject.addSource(sourceObject);
            });
        }

        function addSinkDefinitions(mainObject, sinkList) {
            var self = this;
            _.forEach(sinkList, function (sink) {
                var sinkObject = new SourceOrSinkAnnotation(sink);
                sinkObject.setId(self.newIdBeginningPhrase + sinkObject.getId());
                if (_.isEmpty(sink.map)) {
                    sinkObject.setMap(undefined);
                } else {
                    var mapperObject = new MapAnnotation(sink.map);
                    sinkObject.setMap(mapperObject);
                }
                mainObject.addSink(sinkObject);
            });
        }

        function addStreamDefinitions(mainObject, streamList) {
            var self = this;
            _.forEach(streamList, function (stream) {
                var streamObject = new Stream(stream);
                addAnnotationsForElement(stream, streamObject);
                addAttributesForElement(stream, streamObject);
                streamObject.setId(self.newIdBeginningPhrase + streamObject.getId());
                mainObject.addStream(streamObject);
            });
        }

        function addTableDefinitions(mainObject, tableList) {
            var self = this;
            _.forEach(tableList, function (table) {
                var tableObject = new Table(table);
                if (_.isEmpty(table.store)) {
                    tableObject.setStore(undefined);
                } else {
                    var storeAnnotation = new StoreAnnotation(table.store);
                    tableObject.setStore(storeAnnotation);
                }
                addAnnotationsForElement(table, tableObject);
                addAttributesForElement(table, tableObject);
                tableObject.setId(self.newIdBeginningPhrase + tableObject.getId());
                mainObject.addTable(tableObject);
            });
        }

        function addWindowDefinitions(mainObject, windowList) {
            var self = this;
            _.forEach(windowList, function (window) {
                var windowObject = new Window(window);
                addAnnotationsForElement(window, windowObject);
                addAttributesForElement(window, windowObject);
                windowObject.setId(self.newIdBeginningPhrase + windowObject.getId());
                mainObject.addWindow(windowObject);
            });
        }

        function addTriggerDefinitions(mainObject, triggerList) {
            var self = this;
            _.forEach(triggerList, function (trigger) {
                var triggerObject = new Trigger(trigger);
                addAnnotationsForElement(trigger, triggerObject);
                addAttributesForElement(trigger, triggerObject);
                triggerObject.setId(self.newIdBeginningPhrase + triggerObject.getId());
                mainObject.addTrigger(triggerObject);
            });
        }

        function addAggregationDefinitions(mainObject, aggregationList) {
            var self = this;
            _.forEach(aggregationList, function (aggregation) {
                var aggregationObject = new Aggregation(aggregation);
                if (_.isEmpty(aggregation.store)) {
                    aggregationObject.setStore(undefined);
                } else {
                    var storeAnnotation = new StoreAnnotation(aggregation.store);
                    aggregationObject.setStore(storeAnnotation);
                }
                addAnnotationsForElement(aggregation, aggregationObject);
                // select section in the aggregation definition is compulsory. If that is not found there is a error in
                // backend.
                if (aggregation.select === undefined) {
                    var errMsg = 'Cannot find select section for the aggregation definition:' + aggregation;
                    log.error(errMsg);
                    DesignViewUtils.prototype.errorAlert(errMsg);
                    throw errMsg;
                } else {
                    setSelectForQuery(aggregationObject, aggregation.select);
                }
                if (_.isEmpty(aggregation.aggregateByTimePeriod)) {
                    aggregationObject.setAggregateByTimePeriod(undefined);
                } else {
                    var aggregateByTimePeriodSubElement = new AggregateByTimePeriod(aggregation.aggregateByTimePeriod);
                    aggregationObject.setAggregateByTimePeriod(aggregateByTimePeriodSubElement);
                }
                aggregationObject.setId(self.newIdBeginningPhrase + aggregationObject.getId());
                mainObject.addAggregation(aggregationObject);
            });
        }

        function addFunctionDefinitions(mainObject, functionList) {
            var self = this;
            _.forEach(functionList, function (functionJSON) {
                var functionObject = new FunctionDefinition(functionJSON);
                functionObject.setId(self.newIdBeginningPhrase + functionObject.getId());
                mainObject.addFunction(functionObject);
            });
        }

        function addWindowFilterProjectionQueries(mainObject, windowFilterProjectionQueryList) {
            var self = this;
            _.forEach(windowFilterProjectionQueryList, function (windowFilterProjectionQuery) {
                var queryObject = new Query(windowFilterProjectionQuery);
                addAnnotationsForElement(windowFilterProjectionQuery, queryObject);

                // queryInput section in the query is compulsory. If that is not found there is a error in backend.
                if (windowFilterProjectionQuery.queryInput === undefined) {
                    var errMsg
                        = 'Cannot find query input section for the windowFIlterProjection query:'
                        + windowFilterProjectionQuery;
                    log.error(errMsg);
                    DesignViewUtils.prototype.errorAlert(errMsg);
                    throw errMsg;
                }
                var windowFilterProjectionQueryInput =
                    new WindowFilterProjectionQueryInput(windowFilterProjectionQuery.queryInput);
                setStreamHandlerListForQuery(windowFilterProjectionQueryInput,
                    windowFilterProjectionQuery.queryInput.streamHandlerList);
                queryObject.setQueryInput(windowFilterProjectionQueryInput);
                setSelectForQuery(queryObject, windowFilterProjectionQuery.select);
                setOrderByForQuery(queryObject, windowFilterProjectionQuery.orderBy);
                setQueryOutputForQuery(queryObject, windowFilterProjectionQuery.queryOutput);
                queryObject.setId(self.newIdBeginningPhrase + queryObject.getId());
                mainObject.addWindowFilterProjectionQuery(queryObject);
            });
        }

        function addPatternQueries(mainObject, patternQueryList) {
            var self = this;
            _.forEach(patternQueryList, function (patternQuery) {
                var patternQueryObject = new Query(patternQuery);
                addAnnotationsForElement(patternQuery, patternQueryObject);

                // queryInput section in the query is compulsory. If that is not found there is a error in backend.
                if (patternQuery.queryInput === undefined) {
                    var errMsg = 'Cannot find query input section for the pattern query:' + patternQuery;
                    log.error(errMsg);
                    DesignViewUtils.prototype.errorAlert(errMsg);
                    throw errMsg;
                }

                var patternQueryInput = new PatternOrSequenceQueryInput(patternQuery.queryInput);
                _.forEach(patternQuery.queryInput.conditionList, function (condition) {
                    var patternQueryConditionObject = new PatternOrSequenceQueryCondition(condition);
                    setStreamHandlerListForQuery(patternQueryConditionObject, condition.streamHandlerList);
                    patternQueryInput.addCondition(patternQueryConditionObject);
                });
                patternQueryObject.setQueryInput(patternQueryInput);
                setSelectForQuery(patternQueryObject, patternQuery.select);
                setOrderByForQuery(patternQueryObject, patternQuery.orderBy);
                setQueryOutputForQuery(patternQueryObject, patternQuery.queryOutput);
                patternQueryObject.setId(self.newIdBeginningPhrase + patternQueryObject.getId());
                mainObject.addPatternQuery(patternQueryObject);
            });
        }

        function addSequenceQueries(mainObject, sequenceQueryList) {
            var self = this;
            _.forEach(sequenceQueryList, function (sequenceQuery) {
                var sequenceQueryObject = new Query(sequenceQuery);
                addAnnotationsForElement(sequenceQuery, sequenceQueryObject);

                // queryInput section in the query is compulsory. If that is not found there is a error in backend.
                if (sequenceQuery.queryInput === undefined) {
                    var errMsg = 'Cannot find query input section for the sequence query:' + sequenceQuery;
                    log.error(errMsg);
                    DesignViewUtils.prototype.errorAlert(errMsg);
                    throw errMsg;
                }

                var sequenceQueryInput = new PatternOrSequenceQueryInput(sequenceQuery.queryInput);
                _.forEach(sequenceQuery.queryInput.conditionList, function (condition) {
                    var sequenceQueryConditionObject = new PatternOrSequenceQueryCondition(condition);
                    setStreamHandlerListForQuery(sequenceQueryConditionObject, condition.streamHandlerList);
                    sequenceQueryInput.addCondition(sequenceQueryConditionObject);
                });
                sequenceQueryObject.setQueryInput(sequenceQueryInput);
                setSelectForQuery(sequenceQueryObject, sequenceQuery.select);
                setOrderByForQuery(sequenceQueryObject, sequenceQuery.orderBy);
                setQueryOutputForQuery(sequenceQueryObject, sequenceQuery.queryOutput);
                sequenceQueryObject.setId(self.newIdBeginningPhrase + sequenceQueryObject.getId());
                mainObject.addSequenceQuery(sequenceQueryObject);
            });
        }

        function addJoinQueries(mainObject, joinQueryList) {
            var self = this;
            _.forEach(joinQueryList, function (joinQuery) {
                var queryObject = new Query(joinQuery);
                addAnnotationsForElement(joinQuery, queryObject);

                var errMsg;
                // queryInput section in the query is compulsory. If that is not found there is a error in backend.
                if (joinQuery.queryInput === undefined) {
                    errMsg = 'Cannot find query input section for the join query:' + joinQuery;
                    log.error(errMsg);
                    DesignViewUtils.prototype.errorAlert(errMsg);
                    throw errMsg;
                }

                var joinQueryInput = new JoinQueryInput(joinQuery.queryInput);

                // leftStream section in the join query is compulsory. If that is not found there is a error in backend.
                if (joinQuery.queryInput.left === undefined) {
                    errMsg = 'Cannot find left source for join query:' + joinQuery;
                    log.error(errMsg);
                    DesignViewUtils.prototype.errorAlert(errMsg);
                    throw errMsg;
                }

                var leftSource = new JoinQuerySource(joinQuery.queryInput.left);
                setStreamHandlerListForQuery(leftSource, joinQuery.queryInput.left.streamHandlerList);

                // rightStream section in the join query is compulsory. If that is not found there is a error in backend.
                if (joinQuery.queryInput.right === undefined) {
                    errMsg = 'Cannot find right source for join query:' + joinQuery;
                    log.error(errMsg);
                    DesignViewUtils.prototype.errorAlert(errMsg);
                    throw errMsg;
                }

                var rightSource = new JoinQuerySource(joinQuery.queryInput.right);
                setStreamHandlerListForQuery(rightSource, joinQuery.queryInput.right.streamHandlerList);
                joinQueryInput.setLeft(leftSource);
                joinQueryInput.setRight(rightSource);
                queryObject.setQueryInput(joinQueryInput);
                setSelectForQuery(queryObject, joinQuery.select);
                setOrderByForQuery(queryObject, joinQuery.orderBy);
                setQueryOutputForQuery(queryObject, joinQuery.queryOutput);
                queryObject.setId(self.newIdBeginningPhrase + queryObject.getId());
                mainObject.addJoinQuery(queryObject);
            });
        }

        function addPartitions(mainObject, partitionList) {
            _.forEach(partitionList, function (partition) {
                var partitionObject = new Partition(partition);
                partitionObject.setId(self.newIdBeginningPhrase + partitionObject.getId());
                addAnnotationsForElement(partition, partitionObject);

                _.forEach(partition.partitionWith, function (partitionWith) {
                    var partitionWithObject = new PartitionWith(partitionWith);
                    partitionObject.addPartitionWith(partitionWithObject);
                });

                addStreamDefinitions(partitionObject, partition.streamList);

                addWindowFilterProjectionQueries(partitionObject, partition.queryLists.WINDOW_FILTER_PROJECTION);
                addPatternQueries(partitionObject, partition.queryLists.PATTERN);
                addSequenceQueries(partitionObject, partition.queryLists.SEQUENCE);
                addJoinQueries(partitionObject, partition.queryLists.JOIN);

                mainObject.addPartition(partitionObject);
            });
        }

        function addEdges(mainObject, edgeList) {
            _.forEach(edgeList, function (edge) {
                var newParentId = self.newIdBeginningPhrase + edge.parentId;
                var newChildId = self.newIdBeginningPhrase + edge.childId;
                var newEdgeId = newParentId + "_" + newChildId;
                var edgeOptions = {
                    id: newEdgeId,
                    parentId: newParentId,
                    parentType: edge.parentType,
                    childId: newChildId,
                    childType: edge.childType
                };
                mainObject.addEdge(new Edge(edgeOptions));
            });
        }

        // adds annotations from a json object for an element object
        function addAnnotationsForElement(element, newElementObject) {
            _.forEach(element.annotationList, function (annotation) {
                newElementObject.addAnnotation(annotation);
            });
        }

        // adds attributes from a json object for an element object
        function addAttributesForElement(element, newElementObject) {
            _.forEach(element.attributeList, function (attribute) {
                var attributeObject = new Attribute(attribute);
                newElementObject.addAttribute(attributeObject);
            });
        }

        // sets the stream handler list in query input
        function setStreamHandlerListForQuery(queryInput, streamHandlerList) {
            _.forEach(streamHandlerList, function (streamHandler) {
                var streamHandlerOptions = {};
                if (streamHandler.type === "FUNCTION" || streamHandler.type === "WINDOW") {
                    var windowOrFunctionObject = new QueryWindowOrFunction(streamHandler.value);
                    streamHandlerOptions = {};
                    _.set(streamHandlerOptions, 'type', streamHandler.type);
                    _.set(streamHandlerOptions, 'value', windowOrFunctionObject);
                } else if (streamHandler.type === "FILTER") {
                    _.set(streamHandlerOptions, 'type', streamHandler.type);
                    _.set(streamHandlerOptions, 'value', streamHandler.value);
                } else {
                    console.log("Unknown Stream Handler type detected!")
                }
                var streamHandlerObject = new StreamHandler(streamHandlerOptions);
                queryInput.addStreamHandler(streamHandlerObject);
            });
        }

        // sets the query select(and aggregation definition select) part in a query
        function setSelectForQuery(query, querySelect) {
            // select section in the query/aggregation is compulsory. If that is not found there is a error in backend.
            if (querySelect === undefined) {
                var errMsg = 'Cannot find select section for element:' + query;
                log.error(errMsg);
                DesignViewUtils.prototype.errorAlert(errMsg);
                throw errMsg;
            }

            var querySelectObject = new QuerySelect(querySelect);
            query.setSelect(querySelectObject);
        }

        // sets the query orderBy part in a query
        function setOrderByForQuery(query, queryOrderBy) {
            _.forEach(queryOrderBy, function (queryOrderByValue) {
                var queryOrderByValueObject = new QueryOrderByValue(queryOrderByValue);
                query.addOrderByValue(queryOrderByValueObject);
            });
        }

        // sets the query output attribute in a query
        function setQueryOutputForQuery(query, queryOutput) {
            // queryOutput section in the query/aggregation is compulsory. If that is not found there is a error in
            // backend.
            if (queryOutput === undefined) {
                var errMsg = 'Cannot find query output section for query:' + query;
                log.error(errMsg);
                DesignViewUtils.prototype.errorAlert(errMsg);
                throw errMsg;
            }

            var queryOutputObject = new QueryOutput(queryOutput);
            var queryOutputType = queryOutput.type;
            var output;
            if (queryOutputType === "INSERT") {
                output = new QueryOutputInsert(queryOutput.output);
            } else if (queryOutputType === "DELETE") {
                output = new QueryOutputDelete(queryOutput.output);
            } else if (queryOutputType === "UPDATE") {
                output = new QueryOutputUpdate(queryOutput.output);
            } else if (queryOutputType === "UPDATE_OR_INSERT_INTO") {
                output = new QueryOutputUpdateOrInsertInto(queryOutput.output);
            } else {
                console.log("Invalid query output type received!");
            }
            queryOutputObject.setOutput(output);
            query.setQueryOutput(queryOutputObject);
        }

        return InitialiseDataStructure;
    });
