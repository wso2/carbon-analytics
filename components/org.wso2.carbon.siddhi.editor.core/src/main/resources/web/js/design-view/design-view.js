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

define(['require', 'log', 'lodash', 'jquery', 'tool_palette/tool-palette', 'designViewGrid',
        'configurationData', 'appData', 'partition', 'query', 'stream', 'table', 'window', 'trigger', 'aggregation',
        'aggregateByTimePeriod', 'windowFilterProjectionQueryInput', 'queryWindowOrFunction', 'edge', 'querySelect',
        'queryOrderByValue', 'queryOutput', 'queryOutputInsert', 'queryOutputDelete', 'queryOutputUpdate',
        'queryOutputUpdateOrInsertInto', 'attribute', 'joinQueryInput', 'joinQuerySource',
        'patternOrSequenceQueryInput', 'patternOrSequenceQueryCondition', 'sourceOrSinkAnnotation', 'mapAnnotation',
        'functionDefinition', 'streamHandler', 'storeAnnotation'],
    function (require, log, _, $, ToolPalette, DesignViewGrid, ConfigurationData, AppData, Partition, Query,
              Stream, Table, Window, Trigger, Aggregation, AggregateByTimePeriod, WindowFilterProjectionQueryInput,
              QueryWindowOrFunction, Edge, QuerySelect, QueryOrderByValue, QueryOutput, QueryOutputInsert,
              QueryOutputDelete, QueryOutputUpdate, QueryOutputUpdateOrInsertInto, Attribute, JoinQueryInput,
              JoinQuerySource, PatternOrSequenceQueryInput, PatternOrSequenceQueryCondition, SourceOrSinkAnnotation,
              MapAnnotation, FunctionDefinition, StreamHandler, StoreAnnotation) {

        /**
         * @class DesignView
         * @constructor
         * @class DesignView  Wraps the Ace editor for design view
         * @param {Object} options Rendering options for the view
         * @param application Application data
         * @param jsPlumbInstance js plumb instance for the design grid
         */
        var DesignView = function (options, application ,jsPlumbInstance) {
            var errorMessage1 = 'unable to find design view container in design-view.js';
            var errorMessage2 = 'unable to find application in design-view.js';
            if (!_.has(options, 'container')) {
                log.error(errorMessage1);
            }
            var container = $(_.get(options, 'container'));
            if (!container.length > 0) {
                log.error(errorMessage1);
            }
            if (!_.has(options, 'application')) {
                log.error(errorMessage2);
            }
            this._$parent_el = container;
            this.options = options;
            this.application = application;
            this.jsPlumbInstance =jsPlumbInstance;
        };

        /**
         * @function Initializes the AppData object with th provided configuration
         * @param configurationData siddhi application details as a json
         */
        DesignView.prototype.initialiseSiddhiAppData = function (configurationData) {
            var self = this;
            var currentTabId = self._$parent_el.attr('id');
            var newIdBeginningPhrase = currentTabId + "_element_";
            var appData = new AppData();
            self.configurationData = new ConfigurationData(appData);

            // adds annotations from a json object for an element object
            function addAnnotationsForElement(element, newElementObject) {
                _.forEach(element.annotationList, function(annotation){
                    newElementObject.addAnnotation(annotation);
                });
            }
            // adds attributes from a json object for an element object
            function addAttributesForElement(element, newElementObject) {
                _.forEach(element.attributeList, function(attribute){
                    var attributeObject = new Attribute(attribute);
                    newElementObject.addAttribute(attributeObject);
                });
            }

            // sets the stream handler list in query input
            function setStreamHandlerListForQuery(queryInput, streamHandlerList) {
                _.forEach(streamHandlerList, function(streamHandler){
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
                var querySelectObject = new QuerySelect(querySelect);
                query.setSelect(querySelectObject);
            }

            // sets the query orderBy part in a query
            function setOrderByForQuery(query, queryOrderBy) {
                _.forEach(queryOrderBy, function(queryOrderByValue){
                    var queryOrderByValueObject = new QueryOrderByValue(queryOrderByValue);
                    query.addOrderByValue(queryOrderByValueObject);
                });
            }

            // sets the query output attribute in a query
            function setQueryOutputForQuery(query, queryOutput) {
                var queryOutputObject = new QueryOutput(queryOutput);
                var queryOutputType = queryOutput.type;
                var output;
                if(queryOutputType === "INSERT") {
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

            _.forEach(configurationData.siddhiAppConfig.sourceList, function(source){
                var sourceObject = new SourceOrSinkAnnotation(source);
                sourceObject.setId(newIdBeginningPhrase + sourceObject.getId());
                var mapperObject = new MapAnnotation(source.map);
                sourceObject.setMap(mapperObject);
                appData.addSource(sourceObject);
            });
            _.forEach(configurationData.siddhiAppConfig.sinkList, function(sink){
                var sinkObject = new SourceOrSinkAnnotation(sink);
                sinkObject.setId(newIdBeginningPhrase + sinkObject.getId());
                var mapperObject = new MapAnnotation(sink.map);
                sinkObject.setMap(mapperObject);
                appData.addSink(sinkObject);
            });
            _.forEach(configurationData.siddhiAppConfig.streamList, function(stream){
                var streamObject = new Stream(stream);
                addAnnotationsForElement(stream, streamObject);
                addAttributesForElement(stream, streamObject);
                streamObject.setId(newIdBeginningPhrase + streamObject.getId());
                appData.addStream(streamObject);
            });
            _.forEach(configurationData.siddhiAppConfig.tableList, function(table){
                var tableObject = new Table(table);
                var storeAnnotation = new StoreAnnotation(table.store);
                tableObject.setStore(storeAnnotation);
                addAnnotationsForElement(table, tableObject);
                addAttributesForElement(table, tableObject);
                tableObject.setId(newIdBeginningPhrase + tableObject.getId());
                appData.addTable(tableObject);
            });
            _.forEach(configurationData.siddhiAppConfig.windowList, function(window){
                var windowObject = new Window(window);
                addAnnotationsForElement(window, windowObject);
                addAttributesForElement(window, windowObject);
                windowObject.setId(newIdBeginningPhrase + windowObject.getId());
                appData.addWindow(windowObject);
            });
            _.forEach(configurationData.siddhiAppConfig.triggerList, function(trigger){
                var triggerObject = new Trigger(trigger);
                addAnnotationsForElement(trigger, triggerObject);
                addAttributesForElement(trigger, triggerObject);
                triggerObject.setId(newIdBeginningPhrase + triggerObject.getId());
                appData.addTrigger(triggerObject);
            });
            _.forEach(configurationData.siddhiAppConfig.aggregationList, function(aggregation){
                var aggregationObject = new Aggregation(aggregation);
                var storeAnnotation = new StoreAnnotation(aggregation.store);
                aggregationObject.setStore(storeAnnotation);
                addAnnotationsForElement(aggregation, aggregationObject);
                setSelectForQuery(aggregationObject, aggregation.select);
                var aggregateByTimePeriodSubElement = new AggregateByTimePeriod(aggregation.aggregateByTimePeriod);
                aggregationObject.setAggregateByTimePeriod(aggregateByTimePeriodSubElement);
                aggregationObject.setId(newIdBeginningPhrase + aggregationObject.getId());
                appData.addAggregation(aggregationObject);
            });
            _.forEach(configurationData.siddhiAppConfig.functionList, function(functionJSON){
                var functionObject = new FunctionDefinition(functionJSON);
                functionObject.setId(newIdBeginningPhrase + functionObject.getId());
                appData.addFunction(functionObject);
            });
            _.forEach(configurationData.siddhiAppConfig.queryLists.PATTERN, function(patternQuery){
                var patternQueryObject = new Query(patternQuery);
                addAnnotationsForElement(patternQuery, patternQueryObject);
                var patternQueryInput = new PatternOrSequenceQueryInput(patternQuery.queryInput);
                _.forEach(patternQuery.queryInput.conditionList, function(condition){
                    var patternQueryConditionObject = new PatternOrSequenceQueryCondition(condition);
                    setStreamHandlerListForQuery(patternQueryConditionObject, condition.streamHandlerList);
                    patternQueryInput.addCondition(patternQueryConditionObject);
                });
                patternQueryObject.setQueryInput(patternQueryInput);
                setSelectForQuery(patternQueryObject, patternQuery.select);
                setOrderByForQuery(patternQueryObject, patternQuery.orderBy);
                setQueryOutputForQuery(patternQueryObject, patternQuery.queryOutput);
                patternQueryObject.setId(newIdBeginningPhrase + patternQueryObject.getId());
                appData.addPatternQuery(patternQueryObject);
            });
            _.forEach(configurationData.siddhiAppConfig.queryLists.SEQUENCE, function(sequenceQuery){
                var sequenceQueryObject = new Query(sequenceQuery);
                addAnnotationsForElement(sequenceQuery, sequenceQueryObject);
                var sequenceQueryInput = new PatternOrSequenceQueryInput(sequenceQuery.queryInput);
                _.forEach(sequenceQuery.queryInput.conditionList, function(condition){
                    var sequenceQueryConditionObject = new PatternOrSequenceQueryCondition(condition);
                    setStreamHandlerListForQuery(sequenceQueryConditionObject, condition.streamHandlerList);
                    sequenceQueryInput.addCondition(sequenceQueryConditionObject);
                });
                sequenceQueryObject.setQueryInput(sequenceQueryInput);
                setSelectForQuery(sequenceQueryObject, sequenceQuery.select);
                setOrderByForQuery(sequenceQueryObject, sequenceQuery.orderBy);
                setQueryOutputForQuery(sequenceQueryObject, sequenceQuery.queryOutput);
                sequenceQueryObject.setId(newIdBeginningPhrase + sequenceQueryObject.getId());
                appData.addSequenceQuery(sequenceQueryObject);
            });
            _.forEach(configurationData.siddhiAppConfig.queryLists.WINDOW_FILTER_PROJECTION,
                function(windowFilterProjectionQuery){
                var queryObject = new Query(windowFilterProjectionQuery);
                addAnnotationsForElement(windowFilterProjectionQuery, queryObject);
                var windowFilterProjectionQueryInput =
                    new WindowFilterProjectionQueryInput(windowFilterProjectionQuery.queryInput);
                setStreamHandlerListForQuery(windowFilterProjectionQueryInput,
                    windowFilterProjectionQuery.queryInput.streamHandlerList);
                queryObject.setQueryInput(windowFilterProjectionQueryInput);
                setSelectForQuery(queryObject, windowFilterProjectionQuery.select);
                setOrderByForQuery(queryObject, windowFilterProjectionQuery.orderBy);
                setQueryOutputForQuery(queryObject, windowFilterProjectionQuery.queryOutput);
                queryObject.setId(newIdBeginningPhrase + queryObject.getId());
                appData.addWindowFilterProjectionQuery(queryObject);
            });
            _.forEach(configurationData.siddhiAppConfig.queryLists.JOIN, function(joinQuery){
                var queryObject = new Query(joinQuery);
                addAnnotationsForElement(joinQuery, queryObject);
                var joinQueryInput = new JoinQueryInput(joinQuery.queryInput);
                var leftSource = new JoinQuerySource(joinQuery.queryInput.left);
                setStreamHandlerListForQuery(leftSource, joinQuery.queryInput.left.streamHandlerList);
                var rightSource = new JoinQuerySource(joinQuery.queryInput.right);
                setStreamHandlerListForQuery(rightSource, joinQuery.queryInput.right.streamHandlerList);
                joinQueryInput.setLeft(leftSource);
                joinQueryInput.setRight(rightSource);
                queryObject.setQueryInput(joinQueryInput);
                setSelectForQuery(queryObject, joinQuery.select);
                setOrderByForQuery(queryObject, joinQuery.orderBy);
                setQueryOutputForQuery(queryObject, joinQuery.queryOutput);
                queryObject.setId(newIdBeginningPhrase + queryObject.getId());
                appData.addJoinQuery(queryObject);
            });
            _.forEach(configurationData.siddhiAppConfig.partitionList, function(partition){
                //partitionObject.setId(newIdBeginningPhrase + partitionObject.getId());
                var partitionObject = new Partition(partition);
                addAnnotationsForElement(partition, partitionObject);
                appData.addPartition(partitionObject);
            });
            _.forEach(configurationData.edgeList, function(edge){
                var newParentId = newIdBeginningPhrase + edge.parentId;
                var newChildId = newIdBeginningPhrase + edge.childId;
                var newEdgeId = newParentId + "_" + newChildId;
                var edgeOptions = {
                    id: newEdgeId,
                    parentId: newParentId,
                    parentType: edge.parentType,
                    childId: newChildId,
                    childType: edge.childType
                };
                self.configurationData.addEdge(new Edge(edgeOptions));
            });
        };

        /**
         * @function Renders tool palette in the design container
         */
        DesignView.prototype.renderToolPalette = function () {
            var errMsg = '';
            var toolPaletteContainer =
                this._$parent_el.find(_.get(this.options, 'design_view.tool_palette.container')).get(0);
            if (toolPaletteContainer === undefined) {
                errMsg = 'unable to find tool palette container with selector: '
                    + _.get(this.options, 'design_view.tool_palette.container');
                log.error(errMsg);
            }
            var toolPaletteOpts = _.clone(_.get(this.options, 'design_view.tool_palette'));
            if (toolPaletteOpts === undefined) {
                errMsg = 'unable to find tool palette with selector: '
                    + _.get(this.options, 'design_view.tool_palette');
                log.error(errMsg);
            }
            toolPaletteOpts.container = toolPaletteContainer;
            this.toolPalette = new ToolPalette(toolPaletteOpts);
            this.toolPalette.render();
        };

        /**
         * @function Renders design view in the design container
         * @param configurationData Siddhi application content
         */
        DesignView.prototype.renderDesignGrid = function (configurationData) {
            this.initialiseSiddhiAppData(configurationData);
            var designViewGridOpts = {};
            _.set(designViewGridOpts, 'container', this.designViewGridContainer);
            _.set(designViewGridOpts, 'configurationData', this.configurationData);
            _.set(designViewGridOpts, 'application', this.application);
            _.set(designViewGridOpts, 'jsPlumbInstance', this.jsPlumbInstance);
            this.designViewGrid = new DesignViewGrid(designViewGridOpts);
            this.designViewGrid.render();
        };

        DesignView.prototype.autoAlign = function () {
            if (!_.isUndefined(this.designViewGrid)) {
                this.designViewGrid.autoAlignElements();
            }
        };

        DesignView.prototype.getConfigurationData = function () {
            return this.configurationData;
        };

        DesignView.prototype.emptyDesignViewGridContainer = function () {
            var errMsg = '';
            this.designViewGridContainer = this._$parent_el.find(_.get(this.options, 'design_view.grid_container'));
            if (!this.designViewGridContainer.length > 0) {
                errMsg = 'unable to find design view grid container with selector: '
                    + _.get(this.options, 'design_view.grid_container');
                log.error(errMsg);
            }
            // remove any child nodes from designViewGridContainer if exists
            this.designViewGridContainer.empty();
            // reset the jsPlumb common instance
            if (this.jsPlumbInstance !== undefined) {
                this.jsPlumbInstance.reset();
            }
        };

        DesignView.prototype.showToolPalette = function () {
            if (this.toolPalette !== undefined) {
                this.toolPalette.showToolPalette();
            }
        };

        DesignView.prototype.hideToolPalette = function () {
            if (this.toolPalette !== undefined) {
                this.toolPalette.hideToolPalette();
            }
        };

        DesignView.prototype.getDesign = function (code) {
            var self = this;
            var result = {};
            self.codeToDesignURL = window.location.protocol + "//" + window.location.host + "/editor/design-view";
            $.ajax({
                type: "POST",
                url: self.codeToDesignURL,
                data: window.btoa(code),
                async: false,
                success: function (response) {
                    result = {status: "success", responseJSON: response};
                },
                error: function (error) {
                    console.log(error);
                    if (error.status === 400) {
                        result = {status: "fail", errorMessage: "Siddhi App Contains Errors"};
                    } else {
                        result = {status: "fail", errorMessage: "Internal Server Error Occurred"};
                    }
                }
            });
            return result;
        };

        DesignView.prototype.getCode = function (designViewJSON) {
            var self = this;
            var result = {};
            self.designToCodeURL = window.location.protocol + "//" + window.location.host + "/editor/code-view";
            $.ajax({
                type: "POST",
                url: self.designToCodeURL,
                headers: { 'Content-Type':'application/json' },
                data: designViewJSON,
                async: false,
                success: function (response) {
                    result = {status: "success", responseJSON: window.atob(response)};
                },
                error: function (error) {
                    console.log(error);
                    if (error.status === 400) {
                        result = {status: "fail", errorMessage: "Siddhi App Contains Errors"};
                    } else {
                        result = {status: "fail", errorMessage: "Internal Server Error Occurred"};
                    }
                }
            });
            return result;
        };

        return DesignView;
    });
