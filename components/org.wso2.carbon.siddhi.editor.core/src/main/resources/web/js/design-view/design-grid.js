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
define(['require', 'log', 'jquery', 'backbone', 'lodash', 'dropElements', 'dagre', 'edge',
        'windowFilterProjectionQueryInput', 'joinQueryInput', 'patternOrSequenceQueryInput', 'queryOutput'],

    function (require, log, $, Backbone, _, DropElements, dagre, Edge, WindowFilterProjectionQueryInput,
              JoinQueryInput, PatternOrSequenceQueryInput, QueryOutput) {

        var constants = {
            SOURCE: 'sourceDrop',
            SINK: 'sinkDrop',
            STREAM : 'streamDrop',
            TABLE : 'tableDrop',
            WINDOW :'windowDrop',
            TRIGGER :'triggerDrop',
            AGGREGATION : 'aggregationDrop',
            FUNCTION : 'functionDrop',
            PROJECTION : 'projectionQueryDrop',
            FILTER : 'filterQueryDrop',
            JOIN : 'joinQueryDrop',
            WINDOW_QUERY : 'windowQueryDrop',
            PATTERN : 'patternQueryDrop',
            SEQUENCE : 'sequenceQueryDrop',
            PARTITION :'partitionDrop'
        };

        /**
         * @class DesignGrid
         * @constructor
         * @class DesignGrid  Wraps the Ace editor for design view
         * @param {Object} options Rendering options for the view
         */
        var DesignGrid = function (options) {
            var errorMessage = 'unable to find design view grid container';
            if (!_.has(options, 'container')) {
                log.error(errorMessage);
            }
            var container = $(_.get(options, 'container'));
            if (!container.length > 0) {
                log.error(errorMessage);
            }
            this.options = options;
            this.configurationData = this.options.configurationData;
            this.container = this.options.container;
            this.application = this.options.application;
            this.jsPlumbInstance = options.jsPlumbInstance;
            this.currentTabId = this.application.tabController.activeTab.cid;
            this.designViewContainer = $('#design-container-' + this.currentTabId);
            this.toggleViewButton = $('#toggle-view-button-' + this.currentTabId);
        };

        DesignGrid.prototype.render = function () {
            var self = this;

            // newAgentId --> newAgent ID (Dropped Element ID)
            this.newAgentId = "0";
            var dropElementsOpts = {};
            _.set(dropElementsOpts, 'container', self.container);
            _.set(dropElementsOpts, 'configurationData', self.configurationData);
            _.set(dropElementsOpts, 'application', self.application);
            _.set(dropElementsOpts, 'jsPlumbInstance', self.jsPlumbInstance);
            _.set(dropElementsOpts, 'designGrid', self);
            this.dropElements = new DropElements(dropElementsOpts);
            this.canvas = $(self.container);

            /**
             * @description jsPlumb function opened
             */
            self.jsPlumbInstance.ready(function() {

                self.jsPlumbInstance.importDefaults({
                    PaintStyle: {
                        strokeWidth: 2,
                        stroke: 'darkblue',
                        outlineStroke: "transparent",
                        outlineWidth: "5"
                        // lineWidth: 2
                    },
                    HoverPaintStyle: {
                        strokeStyle: 'darkblue',
                        strokeWidth: 3
                    },
                    Overlays: [["Arrow", {location: 1.0, id: "arrow"}]],
                    DragOptions: {cursor: "crosshair"},
                    Endpoints: [["Dot", {radius: 7}], ["Dot", {radius: 11}]],
                    EndpointStyle: {
                        radius: 3
                    },
                    ConnectionsDetachable: false,
                    Connector: ["Bezier", {curviness: 50}]
                });

                /**
                 * @function droppable method for the 'stream' & the 'query' objects
                 */
                self.canvas.droppable
                ({
                    accept: '.stream-drag, .table-drag, .window-drag, .trigger-drag, .aggregation-drag,' +
                    '.projection-query-drag, .filter-query-drag, .join-query-drag, .window-query-drag,' +
                    '.pattern-query-drag, .sequence-query-drag, .partition-drag, .source-drag, .sink-drag, ' +
                    '.function-drag',
                    containment: 'grid-container',

                    /**
                     *
                     * @param e --> original event object fired/ normalized by jQuery
                     * @param ui --> object that contains additional info added by jQuery depending on which
                     * interaction was used
                     * @helper clone
                     */

                    drop: function (e, ui) {
                        var mouseTop = e.pageY - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                        var mouseLeft = e.pageX - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                        // Clone the element in the toolbox in order to drop the clone on the canvas
                        var droppedElement = ui.helper.clone();
                        // To further manipulate the jsplumb element, remove the jquery UI clone helper as jsPlumb
                        // doesn't support it
                        ui.helper.remove();
                        $(droppedElement).draggable({containment: "parent"});
                        // Repaint to reposition all the elements that are on the canvas after the drop/addition of a
                        // new element on the canvas
                        self.jsPlumbInstance.repaint(ui.helper);

                        // If the dropped Element is a Source annotation then->
                        if ($(droppedElement).hasClass('source-drag')) {
                            self.handleSourceAnnotation(mouseTop, mouseLeft, false, "Source");
                        }

                        // If the dropped Element is a Sink annotation then->
                        if ($(droppedElement).hasClass('sink-drag')) {
                            self.handleSinkAnnotation(mouseTop, mouseLeft, false, "Sink");
                        }

                        // If the dropped Element is a Stream then->
                        if ($(droppedElement).hasClass('stream-drag')) {
                            self.handleStream(mouseTop, mouseLeft, false);
                        }

                        // If the dropped Element is a Table then->
                        if ($(droppedElement).hasClass('table-drag')) {
                            self.handleTable(mouseTop, mouseLeft, false);
                        }

                        // If the dropped Element is a Window(not window query) then->
                        else if ($(droppedElement).hasClass('window-drag')) {
                            self.handleWindow(mouseTop, mouseLeft, false);
                        }

                        // If the dropped Element is a Trigger then->
                        else if ($(droppedElement).hasClass('trigger-drag')) {
                            self.handleTrigger(mouseTop, mouseLeft, false);
                        }

                        // If the dropped Element is a Aggregation then->
                        else if ($(droppedElement).hasClass('aggregation-drag')) {
                            self.handleAggregation(mouseTop, mouseLeft, false);
                        }

                        // If the dropped Element is a Aggregation then->
                        else if ($(droppedElement).hasClass('function-drag')) {
                            self.handleFunction(mouseTop, mouseLeft, false);
                        }

                        // If the dropped Element is a Projection Query then->
                        else if ($(droppedElement).hasClass('projection-query-drag')) {
                            self.handleWindowFilterProjectionQuery(constants.PROJECTION, mouseTop, mouseLeft, false,
                                "Query");
                        }

                        // If the dropped Element is a Filter query then->
                        else if ($(droppedElement).hasClass('filter-query-drag')) {
                            self.handleWindowFilterProjectionQuery(constants.FILTER, mouseTop, mouseLeft, false,
                                "Query");
                        }

                        // If the dropped Element is a Window Query then->
                        else if ($(droppedElement).hasClass('window-query-drag')) {
                            self.handleWindowFilterProjectionQuery(constants.WINDOW_QUERY, mouseTop, mouseLeft, false,
                                "Query");
                        }

                        // If the dropped Element is a Join Query then->
                        else if ($(droppedElement).hasClass('join-query-drag')) {
                            self.handleJoinQuery(mouseTop, mouseLeft, false, "Join");
                        }

                        // If the dropped Element is a Pattern Query then->
                        else if($(droppedElement).hasClass('pattern-query-drag')) {
                            self.handlePatternQuery(mouseTop, mouseLeft, false, "Pattern");
                        }

                        // If the dropped Element is a Sequence Query then->
                        else if($(droppedElement).hasClass('sequence-query-drag')) {
                            self.handleSequenceQuery(mouseTop, mouseLeft, false, "Sequence");
                        }

                        // If the dropped Element is a Partition then->
                        else if($(droppedElement).hasClass('partition-drag')) {
                            self.handlePartition(mouseTop, mouseLeft, false);
                        }
                    }
                });
            });

            // check the validity of the connections and drop if invalid
            function checkConnectionValidityBeforeElementDrop() {
                self.jsPlumbInstance.bind('beforeDrop', function (connection) {
                    var connectionValidity = false;
                    var target = connection.targetId;
                    var targetId = target.substr(0, target.indexOf('-'));
                    var targetElement = $('#' + targetId);

                    var source = connection.sourceId;
                    var sourceId = source.substr(0, source.indexOf('-'));
                    var sourceElement = $('#' + sourceId);

                    // avoid the expose of inner-streams outside the group
                    if (sourceElement.hasClass(constants.STREAM) && self.jsPlumbInstance.getGroupFor(sourceId) !== undefined) {
                        if (self.jsPlumbInstance.getGroupFor(sourceId) !== self.jsPlumbInstance.getGroupFor(targetId)) {
                            alert("Invalid Connection: Inner Streams are not exposed to outside");
                        } else {
                            connectionValidity = true;
                        }
                    }
                    else if (targetElement.hasClass(constants.STREAM) && self.jsPlumbInstance.getGroupFor(targetId) !== undefined) {
                        if (self.jsPlumbInstance.getGroupFor(targetId) !== self.jsPlumbInstance.getGroupFor(sourceId)) {
                            alert("Invalid Connection: Inner Streams are not exposed to outside");
                        } else {
                            connectionValidity = true;
                        }
                    }
                    if (sourceElement.hasClass(constants.PARTITION)) {
                        if ($(self.jsPlumbInstance.getGroupFor(targetId)).attr('id') !== sourceId) {
                            alert("Invalid Connection: Connect to a partition query");
                        } else {
                            connectionValidity = true;
                        }
                    }
                    else if (sourceElement.hasClass(constants.SOURCE)) {
                        if (!targetElement.hasClass(constants.STREAM)) {
                            alert("Invalid Connection: Connect to a stream");
                        } else {
                            connectionValidity = true;
                        }
                    }
                    else if (targetElement.hasClass(constants.SINK)) {
                        if (!sourceElement.hasClass(constants.STREAM)) {
                            alert("Invalid Connection: Sink input source should be a stream");
                        } else {
                            connectionValidity = true;
                        }
                    }
                    else if (targetElement.hasClass(constants.PATTERN) || targetElement.hasClass(constants.SEQUENCE)) {
                        if(!(sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TRIGGER))) {
                            alert("Invalid Connection");
                        } else {
                            connectionValidity = true;
                        }
                    }
                    else if (targetElement.hasClass(constants.PROJECTION) || targetElement.hasClass(constants.FILTER)
                        || targetElement.hasClass(constants.WINDOW_QUERY)) {
                        if (!(sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.WINDOW)
                        || sourceElement.hasClass(constants.TRIGGER))) {
                            alert("Invalid Connection");
                        } else {
                            connectionValidity = true;
                        }
                    }
                    else if (targetElement.hasClass(constants.JOIN)) {
                        if (!(sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TABLE)
                        || sourceElement.hasClass(constants.AGGREGATION) || sourceElement.hasClass(constants.TRIGGER)
                            || sourceElement.hasClass(constants.WINDOW))) {
                            alert("Invalid Connection");
                        } else {
                            var sourceElementObject =
                                self.configurationData.getSiddhiAppConfig().getDefinitionElementById(sourceId);
                            if (sourceElementObject !== undefined) {
                                var connectedElementSourceType = sourceElementObject.type;
                            } else {
                                console.log("Cannot find the source element connected to join query");
                            }
                            var joinQuery = self.configurationData.getSiddhiAppConfig().getJoinQuery(targetId);
                            var queryInput = joinQuery.getQueryInput();
                            if (queryInput === undefined) {
                                connectionValidity = true;
                            } else {
                                var firstConnectedElement = queryInput.getFirstConnectedElement();
                                var secondConnectedElement = queryInput.getSecondConnectedElement();
                                if (firstConnectedElement === undefined && secondConnectedElement === undefined) {
                                    connectionValidity = true;
                                } else if (firstConnectedElement !== undefined
                                    && secondConnectedElement !== undefined) {
                                    connectionValidity = false;
                                    alert("Only two input elements are allowed to connect in join query!");
                                } else if (firstConnectedElement !== undefined
                                    && secondConnectedElement === undefined) {
                                    var firstElementType = firstConnectedElement.type;
                                    if (firstElementType === 'STREAM' || firstElementType === 'TRIGGER'
                                        || firstElementType === 'WINDOW') {
                                        connectionValidity = true;
                                    } else if (connectedElementSourceType === 'STREAM'
                                        || connectedElementSourceType === 'TRIGGER'
                                        || connectedElementSourceType === 'WINDOW') {
                                        connectionValidity = true;
                                    } else {
                                        connectionValidity = false;
                                        alert("At least one connected input element in join query should be a stream " +
                                            "or a trigger or a window!");
                                    }
                                } else if (firstConnectedElement === undefined
                                    && secondConnectedElement !== undefined) {
                                    var secondElementType = secondConnectedElement.type;
                                    if (secondElementType === 'STREAM' || secondElementType === 'TRIGGER'
                                        || secondElementType === 'WINDOW') {
                                        connectionValidity = true;
                                    } else if (connectedElementSourceType === 'STREAM'
                                        || connectedElementSourceType === 'TRIGGER'
                                        || connectedElementSourceType === 'WINDOW') {
                                        connectionValidity = true;
                                    } else {
                                        connectionValidity = false;
                                        alert("At least one connected input element in join query should be a stream " +
                                            "or a trigger or a window!");
                                    }
                                }
                            }
                        }
                    }
                    else if (sourceElement.hasClass(constants.PROJECTION) || sourceElement.hasClass(constants.FILTER)
                        || sourceElement.hasClass(constants.WINDOW_QUERY) || sourceElement.hasClass(constants.PATTERN)
                        || sourceElement.hasClass(constants.JOIN) || sourceElement.hasClass(constants.SEQUENCE)) {
                        if (!(targetElement.hasClass(constants.STREAM) || targetElement.hasClass(constants.TABLE)
                            || targetElement.hasClass(constants.WINDOW))) {
                            alert("Invalid Connection");
                        } else {
                            connectionValidity = true;
                        }
                    }
                    return connectionValidity;
                });
            }

            // Update the model when a connection is established and bind events for the connection
            function updateModelOnConnectionAttach() {
                self.jsPlumbInstance.bind('connection', function (connection) {
                    var target = connection.targetId;
                    var targetId = target.substr(0, target.indexOf('-'));
                    var targetElement = $('#' + targetId);
                    var targetType;
                    if (self.configurationData.getSiddhiAppConfig().getDefinitionElementById(targetId, true, true)
                        !== undefined) {
                        targetType
                            = self.configurationData.getSiddhiAppConfig()
                            .getDefinitionElementById(targetId, true, true).type;
                    } else {
                        console.log("Target element not found!");
                    }

                    var source = connection.sourceId;
                    var sourceId = source.substr(0, source.indexOf('-'));
                    var sourceElement = $('#' + sourceId);
                    var sourceType;
                    if (self.configurationData.getSiddhiAppConfig().getDefinitionElementById(sourceId, true, true)
                        !== undefined) {
                        sourceType
                            = self.configurationData.getSiddhiAppConfig()
                            .getDefinitionElementById(sourceId, true, true).type;
                    } else {
                        console.log("Source element not found!");
                    }

                    // create and add an edge to the edgeList

                    var edgeId = ''+ sourceId + '_' + targetId + '';
                    var edgeInTheEdgeList = self.configurationData.getEdge(edgeId);
                    if(edgeInTheEdgeList === undefined) {
                        var edgeOptions = {};
                        _.set(edgeOptions, 'id', edgeId);
                        _.set(edgeOptions, 'childId', targetId);
                        _.set(edgeOptions, 'childType', targetType);
                        _.set(edgeOptions, 'parentId', sourceId);
                        _.set(edgeOptions, 'parentType', sourceType);
                        var edge = new Edge(edgeOptions);
                        self.configurationData.addEdge(edge);
                    }

                    var model;
                    var connectedElementName;

                    if (sourceElement.hasClass(constants.SOURCE)
                        && (targetElement.hasClass(constants.STREAM) || targetElement.hasClass(constants.TRIGGER))){
                        if(targetElement.hasClass(constants.STREAM)) {
                            connectedElementName = self.configurationData.getSiddhiAppConfig().getStream(targetId)
                                .getName();
                        } else if (targetElement.hasClass(constants.TRIGGER)) {
                            connectedElementName = self.configurationData.getSiddhiAppConfig().getTrigger(targetId)
                                .getName();
                        }
                        self.configurationData.getSiddhiAppConfig().getSource(sourceId)
                            .setConnectedElementName(connectedElementName);
                    } else if (targetElement.hasClass(constants.SINK)
                        && (sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TRIGGER))){
                        if(sourceElement.hasClass(constants.STREAM)) {
                            connectedElementName = self.configurationData.getSiddhiAppConfig().getStream(sourceId)
                                .getName();
                        } else if (sourceElement.hasClass(constants.TRIGGER)) {
                            connectedElementName = self.configurationData.getSiddhiAppConfig().getTrigger(sourceId)
                                .getName();
                        }
                        self.configurationData.getSiddhiAppConfig().getSink(targetId)
                            .setConnectedElementName(connectedElementName);
                    } else if (sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TABLE)
                        || sourceElement.hasClass(constants.AGGREGATION) || sourceElement.hasClass(constants.WINDOW)
                        || sourceElement.hasClass(constants.TRIGGER)) {

                        if (sourceElement.hasClass(constants.STREAM)) {
                            connectedElementName = self.configurationData.getSiddhiAppConfig().getStream(sourceId)
                                .getName();
                        }
                        else if (sourceElement.hasClass(constants.TABLE)) {
                            connectedElementName = self.configurationData.getSiddhiAppConfig().getTable(sourceId)
                                .getName();
                        }
                        else if (sourceElement.hasClass(constants.WINDOW)) {
                            connectedElementName = self.configurationData.getSiddhiAppConfig().getWindow(sourceId)
                                .getName();
                        }
                        else if (sourceElement.hasClass(constants.AGGREGATION)) {
                            connectedElementName = self.configurationData.getSiddhiAppConfig().getAggregation(sourceId)
                                .getName();
                        }
                        else if (sourceElement.hasClass(constants.TRIGGER)) {
                            connectedElementName = self.configurationData.getSiddhiAppConfig().getTrigger(sourceId)
                                .getName();
                        }

                        if ((sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.WINDOW)
                            || sourceElement.hasClass(constants.TRIGGER))
                            && (targetElement.hasClass(constants.PROJECTION) || targetElement.hasClass(constants.FILTER)
                            || targetElement.hasClass(constants.WINDOW_QUERY))) {
                            model = self.configurationData.getSiddhiAppConfig()
                                .getWindowFilterProjectionQuery(targetId);
                            var type;
                            if (targetElement.hasClass(constants.PROJECTION)) {
                                type = 'PROJECTION';
                            }
                            else if (targetElement.hasClass(constants.FILTER)) {
                                type = 'FILTER';
                            }
                            if (targetElement.hasClass(constants.WINDOW_QUERY)) {
                                type = 'WINDOW';
                            }
                            if (model.getQueryInput() === undefined) {
                                var queryInputOptions = {};
                                _.set(queryInputOptions, 'type', type);
                                _.set(queryInputOptions, 'from', connectedElementName);
                                var queryInputObject = new WindowFilterProjectionQueryInput(queryInputOptions);
                                model.setQueryInput(queryInputObject);
                            } else {
                                model.getQueryInput().setFrom(connectedElementName);
                            }
                        }

                        if (targetElement.hasClass(constants.JOIN)) {
                            model = self.configurationData.getSiddhiAppConfig().getJoinQuery(targetId);
                            var queryInput = model.getQueryInput();

                            var sourceElementObject =
                                self.configurationData.getSiddhiAppConfig().getDefinitionElementById(sourceId);
                            var connectedElement = undefined;
                            if (sourceElementObject !== undefined) {
                                var connectedElementSourceName = (sourceElementObject.element).getName();
                                var connectedElementSourceType = sourceElementObject.type;
                                connectedElement = {
                                    name: connectedElementSourceName,
                                    type: connectedElementSourceType
                                };

                                if (queryInput === undefined) {
                                    var joinQueryInput = new JoinQueryInput();
                                    joinQueryInput.setFirstConnectedElement(connectedElement);
                                    model.setQueryInput(joinQueryInput);
                                } else {
                                    var firstConnectedElement = queryInput.getFirstConnectedElement();
                                    var secondConnectedElement = queryInput.getSecondConnectedElement();
                                    if (firstConnectedElement === undefined) {
                                        queryInput.setFirstConnectedElement(connectedElement);
                                    } else if (secondConnectedElement === undefined) {
                                        queryInput.setSecondConnectedElement(connectedElement);
                                    } else {
                                        console.log("Error: First and second input elements are already filled in " +
                                            "join query!");
                                    }
                                }
                            }
                        }
                    } else if (sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TRIGGER) ) {
                        if (sourceElement.hasClass(constants.STREAM)) {
                            connectedElementName =
                                self.configurationData.getSiddhiAppConfig().getStream(sourceId).getName();
                        } else {
                            connectedElementName =
                                self.configurationData.getSiddhiAppConfig().getTrigger(sourceId).getName();
                        }
                        if (targetElement.hasClass(constants.PATTERN)) {
                            model = self.configurationData.getSiddhiAppConfig().getPatternQuery(targetId);
                            if (model.getQueryInput() === undefined) {
                                var patternQueryInputOptions = {};
                                _.set(patternQueryInputOptions, 'type', 'PATTERN');
                                var patternQueryInputObject =
                                    new PatternOrSequenceQueryInput(patternQueryInputOptions);
                                patternQueryInputObject.addConnectedElementName(connectedElementName);
                                model.setQueryInput(patternQueryInputObject);
                            } else {
                                model.getQueryInput().addConnectedElementName(connectedElementName);
                            }
                        } else if (targetElement.hasClass(constants.SEQUENCE)) {
                            model = self.configurationData.getSiddhiAppConfig().getSequenceQuery(targetId);
                            if (model.getQueryInput() === undefined) {
                                var sequenceQueryInputOptions = {};
                                _.set(sequenceQueryInputOptions, 'type', 'SEQUENCE');
                                var sequenceQueryInputObject =
                                    new PatternOrSequenceQueryInput(sequenceQueryInputOptions);
                                sequenceQueryInputObject.addConnectedElementName(connectedElementName);
                                model.setQueryInput(sequenceQueryInputObject);
                            } else {
                                model.getQueryInput().addConnectedElementName(connectedElementName);
                            }
                        }
                    }
                    else if (sourceElement.hasClass(constants.STREAM) && targetElement.hasClass(constants.PARTITION)) {
                        model = self.configurationData.getSiddhiAppConfig().getPartition(targetId);
                        var newPartitionKey = {'stream': sourceId, 'property': undefined};
                        var partitionKeys = (model.getPartition('partition'));
                        partitionKeys['with'].push(newPartitionKey);

                        var connectedQueries = self.jsPlumbInstance.getConnections({source: target});
                        $.each(connectedQueries, function (index, connectedQuery) {
                            var query = connectedQuery.targetId;
                            var queryID = query.substr(0, query.indexOf('-'));
                            var queryElement = $('#' + queryID);
                            if (queryElement.hasClass(constants.PROJECTION)
                                || queryElement.hasClass(constants.FILTER)
                                || queryElement.hasClass(constants.WINDOW_QUERY)) {
                                model = self.configurationData.getSiddhiAppConfig().getQuery(queryID);
                                model.setFrom(sourceId);
                            }
                            else if (queryElement.hasClass(constants.JOIN)) {
                                model = self.configurationData.getSiddhiAppConfig().getJoinQuery(queryID);
                                var streams = model.getFrom();
                                if (streams === undefined) {
                                    streams = [sourceId];
                                } else {
                                    streams.push(sourceId);
                                }
                                model.setFrom(streams);
                            }
                            else if (queryElement.hasClass(constants.PATTERN)) {
                                model = self.configurationData.getSiddhiAppConfig().getPatternQuery(queryID);
                                var streams = model.getFrom();
                                if (streams === undefined) {
                                    streams = [sourceId];
                                } else {
                                    streams.push(sourceId);
                                }
                                model.setFrom(streams);
                            }
                        });


                    }

                    else if (sourceElement.hasClass(constants.PARTITION)) {
                        var connectedStreams = self.jsPlumbInstance.getConnections({target: source});
                        var streamID = null;
                        $.each(connectedStreams, function (index, connectedStream) {
                            var stream = connectedStream.sourceId;
                            streamID = stream.substr(0, stream.indexOf('-'));
                        });
                        if (streamID != null) {
                            if (targetElement.hasClass(constants.PROJECTION) || targetElement.hasClass(constants.FILTER)
                                || targetElement.hasClass(constants.WINDOW_QUERY)) {
                                model = self.configurationData.getSiddhiAppConfig().getQuery(targetId);
                                model.setFrom(streamID);
                            }
                            else if (targetElement.hasClass(constants.JOIN)) {
                                model = self.configurationData.getSiddhiAppConfig().getJoinQuery(targetId);
                                var streams = model.getFrom();
                                if (streams === undefined) {
                                    streams = [streamID];
                                } else {
                                    streams.push(streamID);
                                }
                                model.setFrom(streams);
                            }
                            else if (targetElement.hasClass(constants.PATTERN)) {
                                model = self.configurationData.getSiddhiAppConfig().getPatternQuery(targetId);
                                var streams = model.getFrom();
                                if (streams === undefined) {
                                    streams = [streamID];
                                } else {
                                    streams.push(streamID);
                                }
                                model.setFrom(streams);
                            }
                        }
                    }

                    else if (targetElement.hasClass(constants.STREAM) || targetElement.hasClass(constants.TABLE)
                        || targetElement.hasClass(constants.WINDOW)) {
                        if (sourceElement.hasClass(constants.PROJECTION) || sourceElement.hasClass(constants.FILTER)
                            || sourceElement.hasClass(constants.WINDOW_QUERY) || sourceElement.hasClass(constants.JOIN)
                            || sourceElement.hasClass(constants.PATTERN)
                            || sourceElement.hasClass(constants.SEQUENCE)) {

                            if (targetElement.hasClass(constants.STREAM)) {
                                connectedElementName = self.configurationData.getSiddhiAppConfig().getStream(targetId)
                                    .getName();
                            }
                            else if (targetElement.hasClass(constants.TABLE)) {
                                connectedElementName = self.configurationData.getSiddhiAppConfig().getTable(targetId)
                                    .getName();
                            }
                            else if (targetElement.hasClass(constants.WINDOW)) {
                                connectedElementName = self.configurationData.getSiddhiAppConfig().getWindow(targetId)
                                    .getName();
                            }

                            if (sourceElement.hasClass(constants.PROJECTION) || sourceElement.hasClass(constants.FILTER)
                                || sourceElement.hasClass(constants.WINDOW_QUERY)) {
                                model = self.configurationData.getSiddhiAppConfig()
                                    .getWindowFilterProjectionQuery(sourceId);
                            } else if (sourceElement.hasClass(constants.JOIN)) {
                                model = self.configurationData.getSiddhiAppConfig().getJoinQuery(sourceId);
                            } else if (sourceElement.hasClass(constants.PATTERN)) {
                                model = self.configurationData.getSiddhiAppConfig().getPatternQuery(sourceId);
                            } else if (sourceElement.hasClass(constants.SEQUENCE)) {
                                model = self.configurationData.getSiddhiAppConfig().getSequenceQuery(sourceId);
                            }

                            if (model.getQueryOutput() === undefined) {
                                var queryOutputOptions = {};
                                _.set(queryOutputOptions, 'target', connectedElementName);
                                var patternQueryOutputObject = new QueryOutput(queryOutputOptions);
                                model.setQueryOutput(patternQueryOutputObject);
                            } else {
                                model.getQueryOutput().setTarget(connectedElementName);
                            }
                        }
                    }

                    var connectionObject = connection.connection;
                    // add a overlay of a close icon for connection. connection can be detached by clicking on it
                    var close_icon_overlay = connectionObject.addOverlay([
                        "Custom", {
                            create: function () {
                                return $('<img src="/editor/images/cancel.png" alt="">');
                            },
                            location: 0.60,
                            id: "close",
                            events: {
                                click: function () {
                                    if (confirm('Are you sure you want to remove the connection?')) {
                                        self.jsPlumbInstance.deleteConnection(connectionObject);
                                    } else {
                                    }
                                }
                            }
                        }
                    ]);
                    close_icon_overlay.setVisible(false);
                    // show the close icon when mouse is over the connection
                    connectionObject.bind('mouseover', function () {
                        close_icon_overlay.setVisible(true);
                    });
                    // hide the close icon when the mouse is not on the connection path
                    connectionObject.bind('mouseout', function () {
                        close_icon_overlay.setVisible(false);
                    });
                });
            }

            // Update the model when a connection is detached
            function updateModelOnConnectionDetach() {
                self.jsPlumbInstance.bind('connectionDetached', function (connection) {

                    var target = connection.targetId;
                    var targetId = target.substr(0, target.indexOf('-'));
                    var targetElement = $('#' + targetId);

                    var source = connection.sourceId;
                    var sourceId = source.substr(0, source.indexOf('-'));
                    var sourceElement = $('#' + sourceId);

                    // removing edge from the edgeList
                    var edgeId = ''+ sourceId + '_' + targetId + '';
                    self.configurationData.removeEdge(edgeId);

                    var model;
                    var streams;

                    if (sourceElement.hasClass(constants.SOURCE)
                        && (targetElement.hasClass(constants.STREAM) || targetElement.hasClass(constants.TRIGGER))){
                        self.configurationData.getSiddhiAppConfig().getSource(sourceId)
                            .setConnectedElementName(undefined);
                    } else if (targetElement.hasClass(constants.SINK)
                        && (sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TRIGGER))){
                        self.configurationData.getSiddhiAppConfig().getSink(sourceId)
                            .setConnectedElementName(undefined);
                    }

                    if (sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TABLE)
                        || sourceElement.hasClass(constants.AGGREGATION) || sourceElement.hasClass(constants.WINDOW)
                        || sourceElement.hasClass(constants.TRIGGER)) {

                        if ((sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.WINDOW)
                            || sourceElement.hasClass(constants.TRIGGER))
                            && (targetElement.hasClass(constants.PROJECTION) || targetElement.hasClass(constants.FILTER)
                            || targetElement.hasClass(constants.WINDOW_QUERY))) {
                            model = self.configurationData.getSiddhiAppConfig()
                                .getWindowFilterProjectionQuery(targetId);
                            model.getQueryInput().setFrom(undefined);
                            return;
                        }
                        if (targetElement.hasClass(constants.JOIN)) {
                            model = self.configurationData.getSiddhiAppConfig().getJoinQuery(targetId);
                            var queryInput = model.getQueryInput();
                            var sourceElementObject =
                                self.configurationData.getSiddhiAppConfig().getDefinitionElementById(sourceId);
                            if (sourceElementObject !== undefined) {
                                var disconnectedElementSourceName = (sourceElementObject.element).getName();
                                if (queryInput === undefined) {
                                    console.log("Join query output is undefined!");
                                    return;
                                }
                                var firstConnectedElement = queryInput.getFirstConnectedElement();
                                var secondConnectedElement = queryInput.getSecondConnectedElement();
                                if (firstConnectedElement === undefined && secondConnectedElement === undefined) {
                                    console.log("firstConnectedElement and secondConnectedElement are undefined!");
                                } else if (firstConnectedElement !== undefined
                                    && firstConnectedElement.name === disconnectedElementSourceName) {
                                    queryInput.setFirstConnectedElement(undefined);
                                } else if (secondConnectedElement !== undefined
                                    && secondConnectedElement.name === disconnectedElementSourceName) {
                                    queryInput.setSecondConnectedElement(undefined);
                                }else {
                                    console.log("Error: Disconnected source name not found in join query!");
                                }

                                // if left or sources are created then remove data from those sources
                                if (queryInput.getLeft() !== undefined
                                    && queryInput.getLeft().getFrom() === disconnectedElementSourceName) {
                                    queryInput.setLeft(undefined);
                                } else if (queryInput.getRight() !== undefined
                                    && queryInput.getRight().getFrom() === disconnectedElementSourceName) {
                                    queryInput.setRight(undefined);
                                }
                            }
                            return;
                        }
                    }

                    var disconnectedElementName;
                    if (sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TRIGGER)) {
                        if (sourceElement.hasClass(constants.STREAM)) {
                            disconnectedElementName =
                                self.configurationData.getSiddhiAppConfig().getStream(sourceId).getName();
                        } else {
                            disconnectedElementName =
                                self.configurationData.getSiddhiAppConfig().getTrigger(sourceId).getName();
                        }
                        if (targetElement.hasClass(constants.PATTERN)) {
                            model = self.configurationData.getSiddhiAppConfig().getPatternQuery(targetId);
                            model.getQueryInput().removeConnectedElementName(disconnectedElementName);
                            return;
                        } else if (targetElement.hasClass(constants.SEQUENCE)) {
                            model = self.configurationData.getSiddhiAppConfig().getSequenceQuery(targetId);
                            model.getQueryInput().removeConnectedElementName(disconnectedElementName);
                            return;
                        }
                    }
                    if (sourceElement.hasClass(constants.STREAM) && targetElement.hasClass(constants.PARTITION)) {
                        model = self.configurationData.getSiddhiAppConfig().getPartition(targetId);
                        if (model !== undefined) {
                            var removedPartitionKey = null;
                            var partitionKeys = (model.getPartition().with);
                            $.each(partitionKeys, function (index, key) {
                                if (key.stream === sourceId) {
                                    removedPartitionKey = index;
                                }
                            });
                            partitionKeys.splice(removedPartitionKey, 1);
                            var partitionKeysObj = {'with': partitionKeys};
                            model.setPartition(partitionKeysObj);

                            var connectedQueries = self.jsPlumbInstance.getConnections({source: target});
                            $.each(connectedQueries, function (index, connectedQuery) {
                                var query = connectedQuery.targetId;
                                var queryID = query.substr(0, query.indexOf('-'));
                                var queryElement = $('#' + queryID);
                                if (queryElement.hasClass(constants.PROJECTION)
                                    || queryElement.hasClass(constants.FILTER)
                                    || queryElement.hasClass(constants.WINDOW_QUERY)) {
                                    model = self.configurationData.getSiddhiAppConfig().getQuery(queryID);
                                    if (model !== undefined) {
                                        model.setFrom(undefined);
                                    }
                                }
                                else if (queryElement.hasClass(constants.JOIN)) {
                                    model = self.configurationData.getSiddhiAppConfig().getJoinQuery(queryID);
                                    if (model !== undefined) {
                                        streams = model.getFrom();
                                        var removedStream = streams.indexOf(sourceId);
                                        streams.splice(removedStream, 1);
                                        model.setFrom(streams);
                                    }
                                }
                                else if (queryElement.hasClass(constants.PATTERN)) {
                                    model = self.configurationData.getSiddhiAppConfig().getPatternQuery(queryID);
                                    if (model !== undefined) {
                                        streams = model.getFrom();
                                        var removedStream = streams.indexOf(sourceId);
                                        streams.splice(removedStream, 1);
                                        model.setFrom(streams);
                                    }
                                }
                            });
                        }

                    }

                    else if (sourceElement.hasClass(constants.PARTITION)) {

                        var connectedStreams = self.jsPlumbInstance.getConnections({target: source});
                        var streamID = null;
                        $.each(connectedStreams, function (index, connectedStream) {
                            var stream = connectedStream.sourceId;
                            streamID = stream.substr(0, stream.indexOf('-'));
                        });
                        if (targetElement.hasClass(constants.PROJECTION) || targetElement.hasClass(constants.FILTER)
                            || targetElement.hasClass(constants.WINDOW_QUERY)) {
                            model = self.configurationData.getSiddhiAppConfig().getQuery(targetId);
                            if (model !== undefined) {
                                model.setFrom(undefined);
                            }
                        }
                        else if (targetElement.hasClass(constants.JOIN)) {
                            model = self.configurationData.getSiddhiAppConfig().getJoinQuery(targetId);
                            if (model !== undefined) {
                                streams = model.getFrom();
                                var removedStream = streams.indexOf(streamID);
                                streams.splice(removedStream, 1);
                                model.setFrom(streams);
                            }
                        }
                        else if (targetElement.hasClass(constants.PATTERN)) {
                            model = self.configurationData.getSiddhiAppConfig().getPatternQuery(targetId);
                            if (model !== undefined) {
                                streams = model.getFrom();
                                var removedStream = streams.indexOf(streamID);
                                streams.splice(removedStream, 1);
                                model.setFrom(streams);
                            }
                        }
                    }

                    if (targetElement.hasClass(constants.STREAM) || targetElement.hasClass(constants.TABLE)
                        || targetElement.hasClass(constants.WINDOW)) {
                        if (sourceElement.hasClass(constants.PROJECTION) || sourceElement.hasClass(constants.FILTER)
                            || sourceElement.hasClass(constants.WINDOW_QUERY) || sourceElement.hasClass(constants.JOIN)
                            || sourceElement.hasClass(constants.PATTERN)
                            || sourceElement.hasClass(constants.SEQUENCE)) {

                            if (sourceElement.hasClass(constants.PROJECTION) || sourceElement.hasClass(constants.FILTER)
                                || sourceElement.hasClass(constants.WINDOW)) {
                                model = self.configurationData.getSiddhiAppConfig()
                                    .getWindowFilterProjectionQuery(sourceId);
                            } else if (sourceElement.hasClass(constants.JOIN)) {
                                model = self.configurationData.getSiddhiAppConfig().getJoinQuery(sourceId);
                            } else if (sourceElement.hasClass(constants.PATTERN)) {
                                model = self.configurationData.getSiddhiAppConfig().getPatternQuery(sourceId);
                            } else if (sourceElement.hasClass(constants.SEQUENCE)) {
                                model = self.configurationData.getSiddhiAppConfig().getSequenceQuery(sourceId);
                            }
                            model.getQueryOutput().setTarget(undefined);
                        }
                    }
                });
            }

            function addMemberToPartitionGroup(self) {
                //TODO: check for same connection point connecting to itself scenario. It should be invalid connection.
                self.jsPlumbInstance.bind('group:addMember', function (event) {
                    // if($(event.el).hasClass(constants.FILTER) || $(event.el).hasClass(constants.PROJECTION)
                    //     || $(event.el).hasClass(constants.WINDOW_QUERY) || $(event.el).hasClass(constants.JOIN)
                    //     || $(event.el).hasClass(constants.STREAM)) {
                    //
                    //     var connections = self.jsPlumbInstance.getConnections(event.el);
                    //     //TODO: insert into can be connected to a outside(not inner) stream as well
                    //     if($(event.el).hasClass(constants.STREAM)) {
                    //
                    //     }
                    //     var detachedElement = $(event.el).detach();
                    //     $(detachedElement).insertBefore($(event.group)[0].getEl());
                    //     self.autoAlignElements();
                    //     alert("bc");//TODO: add a proper error message and add align
                    //     // TODO: stand alone inner stream form should not be displayed
                    //     var partitionId = $(event.group).attr('id');
                    //     var partition = self.configurationData.getSiddhiAppConfig().getPartition(partitionId);
                    //     var queries = partition.getQueries();
                    //     if ($(event.el).hasClass(constants.FILTER) || $(event.el).hasClass(constants.PROJECTION)
                    //         || $(event.el).hasClass(constants.WINDOW_QUERY)) {
                    //         queries.push(self.configurationData.getSiddhiAppConfig().getQuery($(event.el).attr('id')));
                    //         //TODO: set isInner flag true
                    //         partition.setQueries(queries);
                    //     }
                    //     else if ($(event.el).hasClass(constants.JOIN)) {
                    //         queries.push(self.configurationData.getSiddhiAppConfig().getJoinQuery($(event.el)
                    //             .attr('id')));
                    //         partition.setQueries(queries);
                    //     }
                    // } else {
                    //     alert("Invalid element type dropped into partition!");
                    //     var detachedElement = $(event.el).detach();
                    //     $(detachedElement).insertBefore($(event.group)[0].getEl());
                    //     self.autoAlignElements();
                    // }
                });
            }

            checkConnectionValidityBeforeElementDrop();

            updateModelOnConnectionAttach();

            updateModelOnConnectionDetach();

            addMemberToPartitionGroup(self);

            self.drawGraphFromAppData();
        };

        DesignGrid.prototype.drawGraphFromAppData = function () {
            var self = this;

            _.forEach(self.configurationData.getSiddhiAppConfig().getSourceList(), function(source){
                var sourceId = source.getId();
                var sourceName = "Source";
                var array = sourceId.split("-");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleSourceAnnotation(mouseTop, mouseLeft, true, sourceName, sourceId);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getSinkList(), function(sink){
                var sinkId = sink.getId();
                var sinkName = "Sink";
                var array = sinkId.split("-");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleSinkAnnotation(mouseTop, mouseLeft, true, sinkName, sinkId);
            });
            
            _.forEach(self.configurationData.getSiddhiAppConfig().getStreamList(), function(stream){
                var streamId = stream.getId();
                var streamName = stream.getName();
                var array = streamId.split("-");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleStream(mouseTop, mouseLeft, true, streamId, streamName);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getTableList(), function(table){

                var tableId = table.getId();
                var tableName = table.getName();
                var array = tableId.split("-");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleTable(mouseTop, mouseLeft, true, tableId, tableName);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getWindowList(), function(window){

                var windowId = window.getId();
                var windowName = window.getName();
                var array = windowId.split("-");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleWindow(mouseTop, mouseLeft, true, windowId, windowName);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getTriggerList(), function(trigger){

                var triggerId = trigger.getId();
                var triggerName = trigger.getName();
                var array = triggerId.split("-");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleTrigger(mouseTop, mouseLeft, true, triggerId, triggerName);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getAggregationList(), function(aggregation){

                var aggregationId = aggregation.getId();
                var aggregationName = aggregation.getName();
                var array = aggregationId.split("-");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleAggregation(mouseTop, mouseLeft, true, aggregationId, aggregationName);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getFunctionList(), function(functionObject){

                var functionId = functionObject.getId();
                var functionName = functionObject.getName();
                var array = functionId.split("-");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleFunction(mouseTop, mouseLeft, true, functionId, functionName);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getPatternQueryList(), function(patternQuery){

                var patternQueryId = patternQuery.getId();
                var patternQueryName = "Pattern";
                var array = patternQueryId.split("-");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handlePatternQuery(mouseTop, mouseLeft, true, patternQueryName, patternQueryId);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getSequenceQueryList(), function(sequenceQuery){

                var sequenceQueryId = sequenceQuery.getId();
                var sequenceQueryName = "Sequence";
                var array = sequenceQueryId.split("-");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleSequenceQuery(mouseTop, mouseLeft, true, sequenceQueryName, sequenceQueryId);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getWindowFilterProjectionQueryList(),
                function (windowFilterProjectionQuery) {
                    var queryId = windowFilterProjectionQuery.getId();
                    var queryName = "Query";
                    var querySubType = windowFilterProjectionQuery.getQueryInput().getType();

                    var queryType;
                    if (querySubType === 'PROJECTION') {
                        queryType = constants.PROJECTION;
                    } else if (querySubType === 'FILTER') {
                        queryType = constants.FILTER;
                    } else if (querySubType === 'WINDOW') {
                        queryType = constants.WINDOW_QUERY;
                    }

                    var array = queryId.split("-");
                    var lastArrayEntry = parseInt(array[array.length - 1]);
                    var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                    var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                    self.handleWindowFilterProjectionQuery(queryType, mouseTop, mouseLeft, true, queryName, queryId);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getJoinQueryList(), function(joinQuery){

                var joinQueryId = joinQuery.getId();
                var joinQueryName = "Join";
                var array = joinQueryId.split("-");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleJoinQuery(mouseTop, mouseLeft, true, joinQueryName, joinQueryId);
            });

            _.forEach(self.configurationData.edgeList, function(edge){

                var targetId = edge.getChildId();
                var sourceId = edge.getParentId();

                self.jsPlumbInstance.connect({
                    source: sourceId+'-out',
                    target: targetId+'-in'
                });
            });

            // re-align the elements
            self.autoAlignElements();
        };

        /**
         * @function Auto align the diagram
         */
        DesignGrid.prototype.autoAlignElements = function () {
            var self = this;
            // Create a new graph instance
            var graph = new dagre.graphlib.Graph({compound: true});
            // Sets the graph to grow from left to right
            graph.setGraph({rankdir: "LR"});
            // This sets the default edge label to `null` as edges/arrows in the design view will
            // never have any labels/names to display on the screen
            graph.setDefaultEdgeLabel(function () {
                return {};
            });

            // Obtain all the draggable UI elements and add them into the nodes[] array
            var currentTabElement = document.getElementById(self.currentTabId);
            var nodes = [];
            Array.prototype.push.apply(nodes, currentTabElement.getElementsByClassName(constants.SOURCE));
            Array.prototype.push.apply(nodes, currentTabElement.getElementsByClassName(constants.SINK));
            Array.prototype.push.apply(nodes, currentTabElement.getElementsByClassName(constants.STREAM));
            Array.prototype.push.apply(nodes, currentTabElement.getElementsByClassName(constants.TABLE));
            Array.prototype.push.apply(nodes, currentTabElement.getElementsByClassName(constants.WINDOW));
            Array.prototype.push.apply(nodes, currentTabElement.getElementsByClassName(constants.TRIGGER));
            Array.prototype.push.apply(nodes, currentTabElement.getElementsByClassName(constants.AGGREGATION));
            Array.prototype.push.apply(nodes, currentTabElement.getElementsByClassName(constants.FUNCTION));
            Array.prototype.push.apply(nodes, currentTabElement.getElementsByClassName(constants.PROJECTION));
            Array.prototype.push.apply(nodes, currentTabElement.getElementsByClassName(constants.FILTER));
            Array.prototype.push.apply(nodes, currentTabElement.getElementsByClassName(constants.WINDOW_QUERY));
            Array.prototype.push.apply(nodes, currentTabElement.getElementsByClassName(constants.JOIN));
            Array.prototype.push.apply(nodes, currentTabElement.getElementsByClassName(constants.PATTERN));
            Array.prototype.push.apply(nodes, currentTabElement.getElementsByClassName(constants.SEQUENCE));
            Array.prototype.push.apply(nodes, currentTabElement.getElementsByClassName(constants.PARTITION));

            // Create an empty JSON to store information of the given graph's nodes, egdes and groups.
            var graphJSON = [];
            graphJSON.nodes = [];
            graphJSON.edges = [];
            graphJSON.groups = [];

            // For every node object in the nodes[] array
            var i = 0;
            nodes.forEach(function (node) {
                // Add each node to the dagre graph object
                graph.setNode(node.id, {width: node.offsetWidth, height: node.offsetHeight});
                // Add each node information to the graphJSON object
                graphJSON.nodes[i] = {
                    id: node.id,
                    width: node.offsetWidth,
                    height: node.offsetHeight
                };
                i++;
            });

            // For every edge in the jsplumb instance
            i = 0;
            var edges = self.jsPlumbInstance.getAllConnections();
            edges.forEach(function (edge) {
                // Get the source and target ID from each edge
                var target = edge.targetId;
                var source = edge.sourceId;
                var targetId = target.substr(0, target.indexOf('-'));
                var sourceId = source.substr(0, source.indexOf('-'));
                // Set the edge to the dagre graph object
                graph.setEdge(sourceId, targetId);
                // Set the edge information to the graphJSON object
                graphJSON.edges[i] = {
                    parent: sourceId,
                    child: targetId
                };
                i++;
            });

            // For every group/partition element
            i = 0;
            var groups = [];
            Array.prototype.push.apply(groups, currentTabElement.getElementsByClassName(constants.PARTITION));
            groups.forEach(function (partition) {
                // Add the group information to the graphJSON obect
                graphJSON.groups[i] = {
                    id: null,
                    children: []
                };
                graphJSON.groups[i].id = partition.id;

                // Identify the children in each group/partition element
                var c = 0;
                var children = partition.childNodes;
                children.forEach(function (child) {
                    // If the children is of the following types, then only can they be considered as a child
                    // of the group
                    var className = child.className;
                    if (className.includes(constants.STREAM) || className.includes(constants.PROJECTION) ||
                        className.includes(constants.FILTER) || className.includes(constants.WINDOW_QUERY) ||
                        className.includes(constants.JOIN) || className.includes(constants.PATTERN) ||
                        className.includes(constants.SEQUENCE)) {
                        // Set the child to it's respective group in the dagre graph object
                        graph.setParent(child.id, partition.id);
                        // Add the child information of each group to the graphJSON object
                        graphJSON.groups[i].children[c] = child.id;
                        c++;
                    }
                });

                i++;
            });

            // This command tells dagre to calculate and finalize the final layout of how the
            // nodes in the graph should be placed
            dagre.layout(graph);

            // Re-align the elements in the grid based on the graph layout given by dagre
            graph.nodes().forEach(function (nodeId) {
                // Get a dagre instance of the node of `nodeId`
                var node = graph.node(nodeId);
                // Get a JQuery instance of the node of `nodeId`
                var $node = $("#" + nodeId);
                // Identify if the node is in a partiton or not using the information
                // in the graphJSON object
                var isInPartition = false;
                var partitionId = -1;
                graphJSON.groups.forEach(function (group) {
                    group.children.forEach(function (child) {
                        if (nodeId == child) {
                            isInPartition = true;
                            partitionId = group.id;
                        }
                    });
                });

                // Note that dagre defines the left(x) & top(y) positions from the center of the element
                // This has to be converted to the actual left and top position of a JQuery element
                if (isInPartition) {
                    // If the current node is in a partition, then that node must be added relative to the position
                    // of it's parent partition
                    var partitionNode = graph.node(partitionId);

                    // Identify the left and top value
                    var partitionNodeLeft = partitionNode.x - (partitionNode.width / 2) + 20;
                    var partitionNodeTop = partitionNode.y - (partitionNode.height / 2) + 20;

                    // Identify the node's left and top position relative to it's partition's top and left position
                    var left = node.x - (node.width / 2) + 20 - partitionNodeLeft;
                    var top = node.y - (node.height / 2) + 20 - partitionNodeTop;

                    // Set the inner node's left and top position
                    $node.css("left", left + "px");
                    $node.css("top", top + "px");
                } else {
                    // If the node is not in a partition then it's left and top positions are obtained relative to
                    // the entire grid
                    var left = node.x - (node.width / 2) + 20;
                    var top = node.y - (node.height / 2) + 20;
                    // Set the node's left and top positions
                    $node.css("left", left + "px");
                    $node.css("top", top + "px");
                }
                // Resize the node with the new width and height defined by dagre
                // The node size can only change for partition nodes
                $node.css("width", node.width + "px");
                $node.css("height", node.height + "px");
            });

            // Redraw the edges in jsplumb
            self.jsPlumbInstance.repaintEverything();
        };

        DesignGrid.prototype.handleSourceAnnotation = function (mouseTop, mouseLeft, isCodeToDesignMode, sourceName,
                                                                sourceId) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
                // design view container is disabled to prevent the user from dropping any elements before initializing
                // a source element
                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(sourceId !== undefined) {
                    elementId = sourceId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("sourceId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr('id', elementId).addClass(constants.SOURCE);
            self.canvas.append(newAgent);
            // Drop the source element. Inside this a it generates the source definition form.
            self.dropElements.dropSource(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, sourceName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handleSinkAnnotation = function (mouseTop, mouseLeft, isCodeToDesignMode, sinkName,
                                                              sinkId) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
                // design view container is disabled to prevent the user from dropping any elements before initializing
                // a sink element
                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(sinkId !== undefined) {
                    elementId = sinkId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("sinkId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr('id', elementId).addClass(constants.SINK);
            self.canvas.append(newAgent);
            // Drop the sink element. Inside this a it generates the sink definition form.
            self.dropElements.dropSink(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, sinkName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };
        
        DesignGrid.prototype.handleStream = function (mouseTop, mouseLeft, isCodeToDesignMode, streamId, streamName) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
                // design view container is disabled to prevent the user from dropping any elements before initializing
                // a stream element
                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(streamId !== undefined) {
                    elementId = streamId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("streamId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr('id', elementId).addClass(constants.STREAM);
            self.canvas.append(newAgent);
            // Drop the stream element. Inside this a it generates the stream definition form.
            self.dropElements.dropStream(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode,
                false, streamName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handleTable = function (mouseTop, mouseLeft, isCodeToDesignMode, tableId, tableName) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
                // design view container is disabled to prevent the user from dropping any elements before initializing
                // a stream element
                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(tableId !== undefined) {
                    elementId = tableId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("tableId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr('id', elementId).addClass(constants.TABLE);
            self.canvas.append(newAgent);
            // Drop the Table element. Inside this a it generates the table definition form.
            self.dropElements.dropTable(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, tableName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handleWindow = function (mouseTop, mouseLeft, isCodeToDesignMode, windowId, windowName) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
                // design view container is disabled to prevent the user from dropping any elements before initializing
                // a stream element
                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(windowId !== undefined) {
                    elementId = windowId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("windowId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr('id', elementId).addClass(constants.WINDOW);
            self.canvas.append(newAgent);
            // Drop the Table element. Inside this a it generates the table definition form.
            self.dropElements.dropWindow(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, windowName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handleTrigger = function (mouseTop, mouseLeft, isCodeToDesignMode, triggerId, triggerName) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
                // design view container is disabled to prevent the user from dropping any elements before initializing
                // a stream element
                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(triggerId !== undefined) {
                    elementId = triggerId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("triggerId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr('id', elementId).addClass(constants.TRIGGER);
            self.canvas.append(newAgent);
            // Drop the Trigger element. Inside this a it generates the trigger definition form.
            self.dropElements.dropTrigger(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, triggerName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        //TODO: Reduce code duplication. Handle definitions in a one method.
        DesignGrid.prototype.handleAggregation = function (mouseTop, mouseLeft, isCodeToDesignMode, aggregationId, aggregationName) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
                // design view container is disabled to prevent the user from dropping any elements before initializing
                // a stream element
                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(aggregationId !== undefined) {
                    elementId = aggregationId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("aggregationId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr('id', elementId).addClass(constants.AGGREGATION);
            self.canvas.append(newAgent);
            // Drop the Aggregation element. Inside this a it generates the aggregation definition form.
            self.dropElements.dropAggregation(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, aggregationName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handleFunction = function (mouseTop, mouseLeft, isCodeToDesignMode, functionId,
                                                        functionName) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
                // design view container is disabled to prevent the user from dropping any elements before initializing
                // a stream element
                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(functionId !== undefined) {
                    elementId = functionId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("functionId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr('id', elementId).addClass(constants.FUNCTION);
            self.canvas.append(newAgent);
            // Drop the Function element. Inside this a it generates the function definition form.
            self.dropElements.dropFunction(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, functionName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };


        DesignGrid.prototype.handleWindowFilterProjectionQuery = function (type, mouseTop, mouseLeft,
                                                                           isCodeToDesignMode, queryName, queryId) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(queryId !== undefined) {
                    elementId = queryId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("queryId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr('id', elementId).addClass(type);
            self.canvas.append(newAgent);
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropWindowFilterProjectionQuery(newAgent, elementId, type, mouseTop, mouseLeft, queryName,
                isCodeToDesignMode);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handleJoinQuery = function (mouseTop, mouseLeft, isCodeToDesignMode, joinQueryName,
                                                         joinQueryId) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(joinQueryId !== undefined) {
                    elementId = joinQueryId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("queryId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr('id', elementId).addClass(constants.JOIN);
            self.canvas.append(newAgent);
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropJoinQuery(newAgent, elementId, mouseTop, mouseLeft, joinQueryName,
                isCodeToDesignMode);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handlePatternQuery = function (mouseTop, mouseLeft, isCodeToDesignMode, patternQueryName,
                                                            patternQueryId) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(patternQueryId !== undefined) {
                    elementId = patternQueryId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("patternQueryId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr('id', elementId).addClass(constants.PATTERN);
            self.canvas.append(newAgent);
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropPatternQuery(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode,
                patternQueryName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handleSequenceQuery = function (mouseTop, mouseLeft, isCodeToDesignMode, sequenceQueryName,
                                                            sequenceQueryId) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(sequenceQueryId !== undefined) {
                    elementId = sequenceQueryId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("sequenceQueryId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr('id', elementId).addClass(constants.SEQUENCE);
            self.canvas.append(newAgent);
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropSequenceQuery(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode,
                sequenceQueryName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handlePartition = function (mouseTop, mouseLeft, isCodeToDesignMode, partitionId) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(partitionId !== undefined) {
                    elementId = partitionId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("partitionId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr('id', elementId).addClass(constants.PARTITION);
            self.canvas.append(newAgent);
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropPartition(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
            //TODO: connection points should be able to remove( close icon). Then update on connection detach.
        };

        DesignGrid.prototype.generateNextNewAgentId = function () {
            var newId = parseInt(this.newAgentId) +1;
            this.newAgentId = "" + newId + "";
            return this.currentTabId + "_element_" + this.newAgentId;
        };

        DesignGrid.prototype.getNewAgentId = function () {
            var self = this;
            return self.generateNextNewAgentId();
        };

        return DesignGrid;
    });
