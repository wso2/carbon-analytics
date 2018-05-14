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
        'aggregateByTimePeriod', 'windowFilterProjectionQueryInput', 'queryWindow', 'patternQueryInput',
        'patternQueryInputCounting', 'patternQueryInputAndOr', 'patternQueryInputNotFor', 'patternQueryInputNotAnd',
        'edge', 'querySelect', 'queryOrderByValue', 'queryOutput', 'queryOutputInsert', 'queryOutputDelete',
        'queryOutputUpdate', 'queryOutputUpdateOrInsertInto', 'attribute', 'joinQueryInput', 'joinQuerySource',
        'patternOrSequenceQueryInput', 'patternOrSequenceQueryCondition'],
    function (require, log, _, $, ToolPalette, DesignViewGrid, ConfigurationData, AppData, Partition, Query,
              Stream, Table, Window, Trigger, Aggregation, AggregateByTimePeriod, WindowFilterProjectionQueryInput,
              QueryWindow, PatternQueryInput, PatternQueryInputCounting, PatternQueryInputAndOr,
              PatternQueryInputNotFor, PatternQueryInputNotAnd, Edge, QuerySelect, QueryOrderByValue, QueryOutput,
              QueryOutputInsert, QueryOutputDelete, QueryOutputUpdate, QueryOutputUpdateOrInsertInto, Attribute,
              JoinQueryInput, JoinQuerySource, PatternOrSequenceQueryInput, PatternOrSequenceQueryCondition) {

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
                if(queryOutputType === "insert") {
                    output = new QueryOutputInsert(queryOutput.output);
                } else if (queryOutputType === "delete") {
                    output = new QueryOutputDelete(queryOutput.output);
                } else if (queryOutputType === "update") {
                    output = new QueryOutputUpdate(queryOutput.output);
                } else if (queryOutputType === "update_or_insert_into") {
                    output = new QueryOutputUpdateOrInsertInto(queryOutput.output);
                } else {
                    console.log("Invalid query output type received!");
                }
                queryOutputObject.setOutput(output);
                query.setQueryOutput(queryOutputObject);
            }

            _.forEach(configurationData.siddhiAppConfig.streamList, function(stream){
                var streamObject = new Stream(stream);
                //addAnnotationsForElement(stream, streamObject);
                addAttributesForElement(stream, streamObject);
                streamObject.setId(newIdBeginningPhrase + streamObject.getId());
                appData.addStream(streamObject);
            });
            _.forEach(configurationData.siddhiAppConfig.tableList, function(table){
                var tableObject = new Table(table);
                //addAnnotationsForElement(table, tableObject);
                addAttributesForElement(table, tableObject);
                tableObject.setId(newIdBeginningPhrase + tableObject.getId());
                appData.addTable(tableObject);
            });
            _.forEach(configurationData.siddhiAppConfig.windowList, function(window){
                var windowObject = new Window(window);
                //addAnnotationsForElement(window, windowObject);
                addAttributesForElement(window, windowObject);
                windowObject.setId(newIdBeginningPhrase + windowObject.getId());
                appData.addWindow(windowObject);
            });
            _.forEach(configurationData.siddhiAppConfig.triggerList, function(trigger){
                var triggerObject = new Trigger(trigger);
                //addAnnotationsForElement(trigger, triggerObject);
                addAttributesForElement(trigger, triggerObject);
                triggerObject.setId(newIdBeginningPhrase + triggerObject.getId());
                appData.addTrigger(triggerObject);
            });
            _.forEach(configurationData.siddhiAppConfig.aggregationList, function(aggregation){
                var aggregationObject = new Aggregation(aggregation);
                //addAnnotationsForElement(aggregation, aggregationObject);
                setSelectForQuery(aggregationObject, aggregation.select);
                var aggregateByTimePeriodSubElement = new AggregateByTimePeriod(aggregation.aggregateByTimePeriod);
                aggregationObject.setAggregateByTimePeriod(aggregateByTimePeriodSubElement);
                aggregationObject.setId(newIdBeginningPhrase + aggregationObject.getId());
                appData.addAggregation(aggregationObject);
            });
            _.forEach(configurationData.siddhiAppConfig.patternQueryList, function(patternQuery){
                var patternQueryObject = new Query(patternQuery);
                var patternQueryInput = new PatternQueryInput();
                _.forEach(patternQuery.queryInput.eventList, function(event){
                    var eventType = event.type;
                    var patternQueryEventObject;
                    if(eventType === "counting") {
                        patternQueryEventObject = new PatternQueryInputCounting(event);
                    } else if (eventType === "andor") {
                        patternQueryEventObject = new PatternQueryInputAndOr(event);
                    } else if (eventType === "notfor") {
                        patternQueryEventObject = new PatternQueryInputNotFor(event);
                    } else if (eventType === "notand") {
                        patternQueryEventObject = new PatternQueryInputNotAnd(event);
                    } else {
                        console.log("Invalid event type received for pattern query input event");
                    }
                    patternQueryInput.addEvent(patternQueryEventObject);
                });
                patternQueryObject.setQueryInput(patternQueryInput);
                setSelectForQuery(patternQueryObject, patternQuery.select);
                setOrderByForQuery(patternQueryObject, patternQuery.orderBy);
                setQueryOutputForQuery(patternQueryObject, patternQuery.queryOutput);
                patternQueryObject.setId(newIdBeginningPhrase + patternQueryObject.getId());
                appData.addPatternQuery(patternQueryObject);
            });
            _.forEach(configurationData.siddhiAppConfig.sequenceQueryList, function(sequenceQuery){
                var sequenceQueryObject = new Query(sequenceQuery);
                var sequenceQueryInput = new PatternOrSequenceQueryInput(sequenceQuery.queryInput);
                _.forEach(sequenceQuery.queryInput.conditionList, function(condition){
                    var sequenceQueryConditionObject = new PatternOrSequenceQueryCondition(condition);
                    sequenceQueryInput.addCondition(sequenceQueryConditionObject);
                });
                sequenceQueryObject.setQueryInput(sequenceQueryInput);
                setSelectForQuery(sequenceQueryObject, sequenceQuery.select);
                setOrderByForQuery(sequenceQueryObject, sequenceQuery.orderBy);
                setQueryOutputForQuery(sequenceQueryObject, sequenceQuery.queryOutput);
                sequenceQueryObject.setId(newIdBeginningPhrase + sequenceQueryObject.getId());
                appData.addSequenceQuery(sequenceQueryObject);
            });
            _.forEach(configurationData.siddhiAppConfig.windowFilterProjectionQueryList,
                function(windowFilterProjectionQuery){
                var queryObject = new Query(windowFilterProjectionQuery);
                var windowFilterProjectionQueryInput =
                    new WindowFilterProjectionQueryInput(windowFilterProjectionQuery.queryInput);
                if (windowFilterProjectionQuery.queryInput.window !== undefined) {
                    var queryWindowObject = new QueryWindow(windowFilterProjectionQuery.queryInput.window);
                    windowFilterProjectionQueryInput.setWindow(queryWindowObject);
                }
                queryObject.setQueryInput(windowFilterProjectionQueryInput);
                setSelectForQuery(queryObject, windowFilterProjectionQuery.select);
                setOrderByForQuery(queryObject, windowFilterProjectionQuery.orderBy);
                setQueryOutputForQuery(queryObject, windowFilterProjectionQuery.queryOutput);
                queryObject.setId(newIdBeginningPhrase + queryObject.getId());
                appData.addWindowFilterProjectionQuery(queryObject);
            });
            _.forEach(configurationData.siddhiAppConfig.joinQueryList, function(joinQuery){
                var queryObject = new Query(joinQuery);
                var joinQueryInput = new JoinQueryInput(joinQuery.queryInput);
                var leftSource = new JoinQuerySource(joinQuery.queryInput.left);
                if (joinQuery.queryInput.left.window !== undefined) {
                    var leftWindowObject = new QueryWindow(joinQuery.queryInput.left.window);
                    leftSource.setWindow(leftWindowObject);
                }
                var rightSource = new JoinQuerySource(joinQuery.queryInput.right);
                if (joinQuery.queryInput.right.window !== undefined) {
                    var rightWindowObject = new QueryWindow(joinQuery.queryInput.right.window);
                    rightSource.setWindow(rightWindowObject);
                }
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
                appData.addPartition(new Partition(partition));
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
            var toolPaletteContainer = this._$parent_el.find(_.get(this.options, 'design_view.tool_palette.container'))
                .get(0);
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

        return DesignView;
    });
