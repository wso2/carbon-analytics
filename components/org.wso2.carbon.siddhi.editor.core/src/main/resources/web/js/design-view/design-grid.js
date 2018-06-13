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
define(['require', 'log', 'jquery', 'backbone', 'lodash', 'designViewUtils', 'dropElements', 'dagre', 'edge',
        'windowFilterProjectionQueryInput', 'joinQueryInput', 'patternOrSequenceQueryInput', 'queryOutput',
        'partitionWith'],

    function (require, log, $, Backbone, _, DesignViewUtils, DropElements, dagre, Edge,
              WindowFilterProjectionQueryInput, JoinQueryInput, PatternOrSequenceQueryInput, QueryOutput,
              PartitionWith) {

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
            PARTITION :'partitionDrop',
            PARTITION_CONNECTION_POINT: 'partition-connector-in-part'
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
                throw errorMessage;
            }
            var container = $(_.get(options, 'container'));
            if (!container.length > 0) {
                log.error(errorMessage);
                throw errorMessage;
            }
            this.options = options;
            this.configurationData = this.options.configurationData;
            this.container = this.options.container;
            this.application = this.options.application;
            this.jsPlumbInstance = options.jsPlumbInstance;
            this.currentTabId = this.application.tabController.activeTab.cid;
            this.designViewContainer = $('#design-container-' + this.currentTabId);
            this.toggleViewButton = $('#toggle-view-button-' + this.currentTabId);
            this.designGridContainer = $('#design-grid-container-' + this.currentTabId);
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

            // configuring the siddhi app level annotations
            var settingsButtonId = self.currentTabId + '-appSettingsId';
            var settingsButton = $("<div id='"+ settingsButtonId +"' class='btn app-annotations-button tool-container' " +
                "data-placement='bottom' data-toggle='tooltip' title='App Annotations'>" +
                "<i class='fw fw-settings'></i></div>");
            settingsButton.tooltip();
            self.canvas.append(settingsButton);
            var settingsIconElement = $('#' + settingsButtonId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.dropElements.formBuilder.DefineFormForAppAnnotations(this);
            });

            /**
             * @description jsPlumb function opened
             */
            self.jsPlumbInstance.ready(function() {

                self.jsPlumbInstance.importDefaults({
                    PaintStyle: {
                        strokeWidth: 2,
                        stroke: '#424242',
                        outlineStroke: "transparent",
                        outlineWidth: "3"
                        // lineWidth: 2
                    },
                    HoverPaintStyle: {
                        strokeStyle: '#424242',
                        strokeWidth: 2
                    },
                    Overlays: [["Arrow", {location: 1.0, id: "arrow", foldback: 1, width: 8, length: 8}]],
                    DragOptions: {cursor: "crosshair"},
                    Endpoints: [["Dot", {radius: 7}], ["Dot", {radius: 11}]],
                    EndpointStyle: {
                        radius: 3
                    },
                    ConnectionsDetachable: false,
                    Connector: ["Bezier", {curviness: 25}]
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
                    /*
                    * There is no 'in' or 'out' clause(for other connection they will have like 'view74_element_6-out')
                    * section in partition connection point. So once we substr with '-' we don't get any value. So we
                    * explicitly set the targetId.
                    * */
                    if (targetId === '') {
                        targetId = target;
                    }
                    var targetElement = $('#' + targetId);

                    var source = connection.sourceId;
                    var sourceId = source.substr(0, source.indexOf('-'));
                    /*
                    * There is no 'in' or 'out' clause(for other connection they will have like 'view74_element_6-out')
                    * section in partition connection point. So once we substr with '-' we don't get any value. So we
                    * explicitly set the sourceId.
                    * */
                    if (sourceId === '') {
                        sourceId = source;
                    }
                    var sourceElement = $('#' + sourceId);

                    // avoid the expose of inner-streams outside the group
                    if (sourceElement.hasClass(constants.STREAM)
                        && self.jsPlumbInstance.getGroupFor(sourceId) !== undefined) {
                        if (self.jsPlumbInstance.getGroupFor(sourceId) !== self.jsPlumbInstance.getGroupFor(targetId)) {
                            DesignViewUtils.prototype
                                .errorAlert("Invalid Connection: Inner Streams are not exposed to outside");
                            return connectionValidity;
                        } else {
                            return connectionValidity = true;
                        }
                    }
                    else if (targetElement.hasClass(constants.STREAM)
                        && self.jsPlumbInstance.getGroupFor(targetId) !== undefined) {
                        if (self.jsPlumbInstance.getGroupFor(targetId) !== self.jsPlumbInstance.getGroupFor(sourceId)) {
                            DesignViewUtils.prototype
                                .errorAlert("Invalid Connection: Inner Streams are not exposed to outside");
                            return connectionValidity;
                        } else {
                            return connectionValidity = true;
                        }
                    } else if (targetElement.hasClass(constants.PARTITION_CONNECTION_POINT)) {
                        if (!sourceElement.hasClass(constants.STREAM)) {
                            DesignViewUtils.prototype.errorAlert("Invalid Connection: Connect an outer stream");
                            return connectionValidity;
                        } else {
                            var partitionId = targetElement.parent()[0].id;
                            var partition = self.configurationData.getSiddhiAppConfig().getPartition(partitionId);
                            var connectedStreamName
                                = self.configurationData.getSiddhiAppConfig().getStream(sourceId).getName();
                            var isStreamConnected = partition.checkOuterStreamIsAlreadyConnected(connectedStreamName);
                            if (isStreamConnected) {
                                DesignViewUtils.prototype
                                    .errorAlert("Invalid Connection: Stream is already connected to the partition");
                                return connectionValidity;
                            } else {
                                return connectionValidity = true;
                            }
                        }
                    } else if (sourceElement.hasClass(constants.PARTITION_CONNECTION_POINT)) {
                        // check whether the partition connection point has a valid connection with a outer stream.
                        // If not display a error message.
                        var sourceConnections = self.jsPlumbInstance.getConnections({target: sourceId});
                        if (sourceConnections.length === 0) {
                            DesignViewUtils.prototype.errorAlert("Invalid Connection: Connect a outer stream first");
                            return connectionValidity;
                        }
                        var partitionId = sourceElement.parent()[0].id;
                        if (self.jsPlumbInstance.getGroupFor(targetId)
                            !== self.jsPlumbInstance.getGroupFor(partitionId)) {
                            DesignViewUtils.prototype
                                .errorAlert("Invalid Connection: Connect a query inside the partition");
                        } else {
                            if (targetElement.hasClass(constants.PROJECTION)
                                || targetElement.hasClass(constants.FILTER)
                                || targetElement.hasClass(constants.WINDOW_QUERY)
                                || targetElement.hasClass(constants.PATTERN)
                                || targetElement.hasClass(constants.JOIN)
                                || targetElement.hasClass(constants.SEQUENCE)) {
                                return connectionValidity = true;
                            } else {
                                DesignViewUtils.prototype
                                    .errorAlert("Invalid Connection: Connect a query inside the partition");
                                return connectionValidity;
                            }
                        }
                    }
                    else if (sourceElement.hasClass(constants.PARTITION)) {
                        if ($(self.jsPlumbInstance.getGroupFor(targetId)).attr('id') !== sourceId) {
                            DesignViewUtils.prototype.errorAlert("Invalid Connection: Connect to a partition query");
                            return connectionValidity;
                        } else {
                            return connectionValidity = true;
                        }
                    }
                    else if (sourceElement.hasClass(constants.SOURCE)) {
                        if (!targetElement.hasClass(constants.STREAM)) {
                            DesignViewUtils.prototype.errorAlert("Invalid Connection: Connect to a stream");
                            return connectionValidity;
                        } else {
                            return connectionValidity = true;
                        }
                    }
                    else if (targetElement.hasClass(constants.SINK)) {
                        if (!sourceElement.hasClass(constants.STREAM)) {
                            DesignViewUtils.prototype
                                .errorAlert("Invalid Connection: Sink input source should be a stream");
                            return connectionValidity;
                        } else {
                            return connectionValidity = true;
                        }
                    }
                    else if (targetElement.hasClass(constants.AGGREGATION)) {
                        if (!(sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TRIGGER))) {
                            DesignViewUtils.prototype
                                .errorAlert("Invalid Connection: Aggregation input should be a stream or trigger");
                            return connectionValidity;
                        } else {
                            return connectionValidity = true;
                        }
                    }

                    // When connecting streams to a query inside the partition if it is connected to the partition
                    // connection point, it cannot connect to the query directly
                    if ((targetElement.hasClass(constants.PROJECTION) || targetElement.hasClass(constants.FILTER)
                        || targetElement.hasClass(constants.WINDOW_QUERY) || targetElement.hasClass(constants.JOIN)
                        || targetElement.hasClass(constants.PATTERN) || targetElement.hasClass(constants.SEQUENCE))
                        && sourceElement.hasClass(constants.STREAM)) {
                        var querySavedInsideAPartition
                            = self.configurationData.getSiddhiAppConfig().getQueryByIdSavedInsideAPartition(targetId);
                        var isQueryInsideAPartition = querySavedInsideAPartition !== undefined;
                        if (isQueryInsideAPartition) {
                            var partitionId = (self.jsPlumbInstance.getGroupFor(targetId)).id;
                            var partition = self.configurationData.getSiddhiAppConfig().getPartition(partitionId);
                            var connectedStreamName
                                = self.configurationData.getSiddhiAppConfig().getStream(sourceId).getName();
                            var isStreamConnected = partition.checkOuterStreamIsAlreadyConnected(connectedStreamName);
                            if (isStreamConnected) {
                                DesignViewUtils.prototype
                                    .errorAlert("Invalid Connection: Stream is already connected to the partition");
                                return connectionValidity;
                            }
                        }
                    }
                    if (targetElement.hasClass(constants.PATTERN) || targetElement.hasClass(constants.SEQUENCE)) {
                        if(!(sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TRIGGER))) {
                            DesignViewUtils.prototype.errorAlert("Invalid Connection");
                        } else {
                            connectionValidity = true;
                        }
                    }
                    else if (targetElement.hasClass(constants.PROJECTION) || targetElement.hasClass(constants.FILTER)
                        || targetElement.hasClass(constants.WINDOW_QUERY)) {
                        if (!(sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.WINDOW)
                        || sourceElement.hasClass(constants.TRIGGER))) {
                            DesignViewUtils.prototype.errorAlert("Invalid Connection");
                        } else {
                            connectionValidity = true;
                        }
                    }
                    else if (targetElement.hasClass(constants.JOIN)) {
                        if (!(sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TABLE)
                        || sourceElement.hasClass(constants.AGGREGATION) || sourceElement.hasClass(constants.TRIGGER)
                            || sourceElement.hasClass(constants.WINDOW))) {
                            DesignViewUtils.prototype.errorAlert("Invalid Connection");
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
                            if (!queryInput) {
                                connectionValidity = true;
                            } else {
                                var firstConnectedElement = queryInput.getFirstConnectedElement();
                                var secondConnectedElement = queryInput.getSecondConnectedElement();
                                if (!firstConnectedElement && !secondConnectedElement) {
                                    connectionValidity = true;
                                } else if (firstConnectedElement !== undefined
                                    && secondConnectedElement !== undefined) {
                                    connectionValidity = false;
                                    DesignViewUtils.prototype
                                        .errorAlert("Only two input elements are allowed to connect in join query!");
                                } else if (firstConnectedElement !== undefined
                                    && !secondConnectedElement) {
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
                                        DesignViewUtils.prototype
                                            .errorAlert("At least one connected input element in join query should be a stream " +
                                            "or a trigger or a window!");
                                    }
                                } else if (!firstConnectedElement && secondConnectedElement !== undefined) {
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
                                        DesignViewUtils.prototype
                                            .errorAlert("At least one connected input element in join query should be a stream " +
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
                            DesignViewUtils.prototype.errorAlert("Invalid Connection");
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
                    var targetType;
                    /*
                    * There is no 'in' or 'out' clause(for other connection they will have like 'view74_element_6-out')
                    * section in partition connection point. So once we substr with '-' we don't get any value. So we
                    * explicitly set the targetId.  Simply if targetId is '' that means this connection is related to a
                    * partition.
                    * */
                    if (targetId === '') {
                        targetId = target;
                        targetType = 'PARTITION';
                    } else {
                        if (self.configurationData.getSiddhiAppConfig().getDefinitionElementById(targetId, true, true)
                            !== undefined) {
                            targetType
                                = self.configurationData.getSiddhiAppConfig()
                                .getDefinitionElementById(targetId, true, true).type;
                        } else {
                            console.log("Target element not found!");
                        }
                    }
                    var targetElement = $('#' + targetId);

                    var source = connection.sourceId;
                    var sourceId = source.substr(0, source.indexOf('-'));
                    var sourceType;
                    /*
                    * There is no 'in' or 'out' clause(for other connection they will have like 'view74_element_6-out')
                    * section in partition connection point. So once we substr with '-' we don't get any value. So we
                    * explicitly set the sourceId.  Simply if sourceId is '' that means this connection is related to a
                    * partition.
                    * */
                    if (sourceId === '') {
                        sourceId = source;
                        sourceType = 'PARTITION';
                    } else {
                        if (self.configurationData.getSiddhiAppConfig().getDefinitionElementById(sourceId, true, true)
                            !== undefined) {
                            sourceType
                                = self.configurationData.getSiddhiAppConfig()
                                .getDefinitionElementById(sourceId, true, true).type;
                        } else {
                            console.log("Source element not found!");
                        }
                    }
                    var sourceElement = $('#' + sourceId);

                    var isConnectionMadeInsideAPartition = false;
                    if (self.jsPlumbInstance.getGroupFor(sourceId) !== undefined
                        && self.jsPlumbInstance.getGroupFor(targetId) !== undefined) {
                        isConnectionMadeInsideAPartition = true;
                    }
                    // create and add an edge to the edgeList

                    var edgeId = ''+ sourceId + '_' + targetId + '';
                    var edgeInTheEdgeList = self.configurationData.getEdge(edgeId);
                    if(!edgeInTheEdgeList) {
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

                    if (targetElement.hasClass(constants.PARTITION_CONNECTION_POINT)
                        && sourceElement.hasClass(constants.STREAM)){
                        var partitionId = targetElement.parent()[0].id;
                        var partition = self.configurationData.getSiddhiAppConfig().getPartition(partitionId);
                        connectedElementName
                            = self.configurationData.getSiddhiAppConfig().getStream(sourceId).getName();

                        /*
                        * check whether the stream is already connected to partition. This validation is done in the
                        * checkConnectionValidityBeforeElementDrop() function. But that beforedrop event is triggered
                        * only when user adds connection. It doesn't fire when we programmatically create connections
                        * (in this case rendering the design view from code). So we need to do the validation here
                        * again.
                        * */
                        var isStreamConnected = partition.checkOuterStreamIsAlreadyConnected(connectedElementName);
                        if (!isStreamConnected) {
                            var partitionWithOptions = {};
                            _.set(partitionWithOptions, 'streamName', connectedElementName);
                            _.set(partitionWithOptions, 'expression', undefined);
                            var partitionWithObject = new PartitionWith(partitionWithOptions);
                            partition.addPartitionWith(partitionWithObject);
                        }

                        var partitionElement = $('#' + partitionId);
                        var newPartitionConnectorInPartNo = self.generateNextConnectionPointIdForPartition(partitionId);

                        var connectionIn =
                            $('<div class="' + constants.PARTITION_CONNECTION_POINT + '">')
                            .attr('id', newPartitionConnectorInPartNo);
                        partitionElement.append(connectionIn);

                        self.jsPlumbInstance.makeTarget(connectionIn, {
                            anchor: 'Left',
                            maxConnections: 1,
                            deleteEndpointsOnDetach : true
                        });
                        self.jsPlumbInstance.makeSource(connectionIn, {
                            anchor: 'Right',
                            deleteEndpointsOnDetach : true
                        });

                    } else if (sourceElement.hasClass(constants.SOURCE)
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

                    } else if (targetElement.hasClass(constants.AGGREGATION)
                        && (sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TRIGGER))) {
                        if (sourceElement.hasClass(constants.STREAM)) {
                            connectedElementName = self.configurationData.getSiddhiAppConfig().getStream(sourceId)
                                .getName();
                        } else if (sourceElement.hasClass(constants.TRIGGER)) {
                            connectedElementName = self.configurationData.getSiddhiAppConfig().getTrigger(sourceId)
                                .getName();
                        }
                        self.configurationData.getSiddhiAppConfig().getAggregation(targetId)
                            .setFrom(connectedElementName);

                    } else if (sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TABLE)
                        || sourceElement.hasClass(constants.AGGREGATION) || sourceElement.hasClass(constants.WINDOW)
                        || sourceElement.hasClass(constants.TRIGGER)
                        || sourceElement.hasClass(constants.PARTITION_CONNECTION_POINT)) {

                        /*
                        * Partition connection point represents a stream connection point. so it holds a reference for
                        * the stream.So that in here we replaces the source element with the actual stream element if a
                        * connection partition connection pint is found.
                        * */
                        if (sourceElement.hasClass(constants.PARTITION_CONNECTION_POINT)) {
                            var sourceConnection = self.jsPlumbInstance.getConnections({target: sourceId});
                            var sourceConnectionId = sourceConnection[0].sourceId;
                            var connectedStreamId = sourceConnectionId.substr(0, sourceConnectionId.indexOf('-'));
                            connectedElementName = self.configurationData.getSiddhiAppConfig()
                                .getStream(connectedStreamId).getName();
                            sourceElement = $('#' + connectedStreamId);
                            sourceId = connectedStreamId;
                        }
                        else if (sourceElement.hasClass(constants.STREAM)) {
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
                            if (!model.getQueryInput()) {
                                var queryInputOptions = {};
                                _.set(queryInputOptions, 'type', type);
                                _.set(queryInputOptions, 'from', connectedElementName);
                                var queryInputObject = new WindowFilterProjectionQueryInput(queryInputOptions);
                                model.setQueryInput(queryInputObject);
                            } else {
                                model.getQueryInput().setFrom(connectedElementName);
                            }
                        } else if (targetElement.hasClass(constants.JOIN)) {
                            model = self.configurationData.getSiddhiAppConfig().getJoinQuery(targetId);
                            var queryInput = model.getQueryInput();

                            var sourceElementObject =
                                self.configurationData.getSiddhiAppConfig().getDefinitionElementById(sourceId);
                            var connectedElement;
                            if (sourceElementObject !== undefined) {
                                var connectedElementSourceName = (sourceElementObject.element).getName();
                                var connectedElementSourceType = sourceElementObject.type;
                                connectedElement = {
                                    name: connectedElementSourceName,
                                    type: connectedElementSourceType
                                };

                                if (!queryInput) {
                                    var joinQueryInput = new JoinQueryInput();
                                    joinQueryInput.setFirstConnectedElement(connectedElement);
                                    model.setQueryInput(joinQueryInput);
                                } else {
                                    var firstConnectedElement = queryInput.getFirstConnectedElement();
                                    var secondConnectedElement = queryInput.getSecondConnectedElement();
                                    if (!firstConnectedElement) {
                                        queryInput.setFirstConnectedElement(connectedElement);
                                    } else if (!secondConnectedElement) {
                                        queryInput.setSecondConnectedElement(connectedElement);
                                    } else {
                                        console.log("Error: First and second input elements are already filled in " +
                                            "join query!");
                                    }
                                }
                            }
                        } else if (sourceElement.hasClass(constants.STREAM)
                            || sourceElement.hasClass(constants.TRIGGER)) {

                            if (sourceElement.hasClass(constants.STREAM)) {
                                connectedElementName =
                                    self.configurationData.getSiddhiAppConfig().getStream(sourceId).getName();
                            } else {
                                connectedElementName =
                                    self.configurationData.getSiddhiAppConfig().getTrigger(sourceId).getName();
                            }

                            if (targetElement.hasClass(constants.PATTERN)) {
                                model = self.configurationData.getSiddhiAppConfig().getPatternQuery(targetId);
                                if (!model.getQueryInput()) {
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
                                if (!model.getQueryInput()) {
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

                            if (!model.getQueryOutput()) {
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
                                    } else {}
                                }
                            }
                        }
                    ]);

                    //TODO: check the mouse enter and leave events when in a partition
                    if (isConnectionMadeInsideAPartition) {
                        close_icon_overlay.setVisible(false);
                        // show the close icon when mouse is over the connection
                        connectionObject.bind('mouseenter', function () {
                            close_icon_overlay.setVisible(true);
                        });
                        // hide the close icon when the mouse is not on the connection path
                        connectionObject.bind('mouseleave', function () {
                            close_icon_overlay.setVisible(false);
                        });
                    } else {
                        close_icon_overlay.setVisible(false);
                        // show the close icon when mouse is over the connection
                        connectionObject.bind('mouseover', function () {
                            close_icon_overlay.setVisible(true);
                        });
                        // hide the close icon when the mouse is not on the connection path
                        connectionObject.bind('mouseout', function () {
                            close_icon_overlay.setVisible(false);
                        });
                    }
                });
            }

            // Update the model before detaching a connection
            function updateModelOnBeforeConnectionDetach() {
                self.jsPlumbInstance.bind('beforeDetach', function (connection) {
                    var target = connection.targetId;
                    var targetId = target.substr(0, target.indexOf('-'));
                    /*
                    * There is no 'in' or 'out' clause(for other connection they will have like 'view74_element_6-out')
                    * section in partition connection point. So once we substr with '-' we don't get any value. So we
                    * explicitly set the targetId.  Simply if targetId is '' that means this connection is related to a
                    * partition.
                    * */
                    if (targetId === '') {
                        targetId = target;
                    } else if (!self.configurationData.getSiddhiAppConfig()
                        .getDefinitionElementById(targetId, true, true)) {
                        console.log("Target element not found!");
                    }
                    var targetElement = $('#' + targetId);

                    var source = connection.sourceId;
                    var sourceId = source.substr(0, source.indexOf('-'));
                    /*
                    * There is no 'in' or 'out' clause(for other connection they will have like 'view74_element_6-out')
                    * section in partition connection point. So once we substr with '-' we don't get any value. So we
                    * explicitly set the sourceId.  Simply if sourceId is '' that means this connection is related to a
                    * partition.
                    * */
                    if (sourceId === '') {
                        sourceId = source;
                    } else if (!self.configurationData.getSiddhiAppConfig()
                        .getDefinitionElementById(sourceId, true, true)) {
                        console.log("Source element not found!");
                    }
                    var sourceElement = $('#' + sourceId);

                    // removing edge from the edgeList
                    var edgeId = ''+ sourceId + '_' + targetId + '';
                    self.configurationData.removeEdge(edgeId);

                    if (targetElement.hasClass(constants.PARTITION_CONNECTION_POINT)
                        && sourceElement.hasClass(constants.STREAM)) {
                        var partitionId = targetElement.parent()[0].id;
                        var partition = self.configurationData.getSiddhiAppConfig().getPartition(partitionId);
                        var disconnectedElementName
                            = self.configurationData.getSiddhiAppConfig().getStream(sourceId).getName();

                        partition.removePartitionWith(disconnectedElementName);

                        var connections = self.jsPlumbInstance.getConnections({source: targetId});
                        _.forEach(connections, function (connection) {
                            self.jsPlumbInstance.deleteConnection(connection);
                        });
                        targetElement.detach();
                    }
                });
            }
            // Update the model when a connection is detached
            function updateModelOnConnectionDetach() {
                self.jsPlumbInstance.bind('connectionDetached', function (connection) {

                    var target = connection.targetId;
                    var targetId = target.substr(0, target.indexOf('-'));
                    /*
                    * There is no 'in' or 'out' clause(for other connection they will have like 'view74_element_6-out')
                    * section in partition connection point. So once we substr with '-' we don't get any value. So we
                    * explicitly set the targetId.  Simply if targetId is '' that means this connection is related to a
                    * partition.
                    * */
                    if (targetId === '') {
                        targetId = target;
                    } else if (!self.configurationData.getSiddhiAppConfig()
                        .getDefinitionElementById(targetId, true, true)) {
                        console.log("Target element not found!");
                    }
                    var targetElement = $('#' + targetId);

                    var source = connection.sourceId;
                    var sourceId = source.substr(0, source.indexOf('-'));
                    /*
                    * There is no 'in' or 'out' clause(for other connection they will have like 'view74_element_6-out')
                    * section in partition connection point. So once we substr with '-' we don't get any value. So we
                    * explicitly set the sourceId.  Simply if sourceId is '' that means this connection is related to a
                    * partition.
                    * */
                    if (sourceId === '') {
                        sourceId = source;
                    } else if (!self.configurationData.getSiddhiAppConfig()
                        .getDefinitionElementById(sourceId, true, true)) {
                        console.log("Source element not found!");
                    }
                    var sourceElement = $('#' + sourceId);

                    // removing edge from the edgeList
                    var edgeId = ''+ sourceId + '_' + targetId + '';
                    self.configurationData.removeEdge(edgeId);

                    var model;

                    if (sourceElement.hasClass(constants.SOURCE)
                        && (targetElement.hasClass(constants.STREAM) || targetElement.hasClass(constants.TRIGGER))){
                        self.configurationData.getSiddhiAppConfig().getSource(sourceId)
                            .setConnectedElementName(undefined);

                    } else if (targetElement.hasClass(constants.SINK)
                        && (sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TRIGGER))){
                        self.configurationData.getSiddhiAppConfig().getSink(sourceId)
                            .setConnectedElementName(undefined);

                    } else if (targetElement.hasClass(constants.AGGREGATION)
                        && (sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TRIGGER))) {
                        self.configurationData.getSiddhiAppConfig().getAggregation(targetId).setFrom(undefined);

                    } else if (sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TABLE)
                        || sourceElement.hasClass(constants.AGGREGATION) || sourceElement.hasClass(constants.WINDOW)
                        || sourceElement.hasClass(constants.TRIGGER)
                        || sourceElement.hasClass(constants.PARTITION_CONNECTION_POINT)) {

                        // if the sourceElement has the class constants.PARTITION_CONNECTION_POINT then that is
                        // basically a stream because a connection point holds a connection to a stream.
                        // So we replace that sourceElement with the actual stream element.
                        if (sourceElement.hasClass(constants.PARTITION_CONNECTION_POINT)) {
                            var sourceConnection = self.jsPlumbInstance.getConnections({target: sourceId});
                            var sourceConnectionId = sourceConnection[0].sourceId;
                            var connectedStreamId = sourceConnectionId.substr(0, sourceConnectionId.indexOf('-'));
                            sourceElement = $('#' + connectedStreamId);
                            sourceId = connectedStreamId;
                        }

                        if ((sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.WINDOW)
                            || sourceElement.hasClass(constants.TRIGGER))
                            && (targetElement.hasClass(constants.PROJECTION) || targetElement.hasClass(constants.FILTER)
                            || targetElement.hasClass(constants.WINDOW_QUERY))) {
                            model = self.configurationData.getSiddhiAppConfig()
                                .getWindowFilterProjectionQuery(targetId);
                            model.getQueryInput().setFrom(undefined);

                        } else if (targetElement.hasClass(constants.JOIN)) {
                            model = self.configurationData.getSiddhiAppConfig().getJoinQuery(targetId);
                            var queryInput = model.getQueryInput();
                            var sourceElementObject =
                                self.configurationData.getSiddhiAppConfig().getDefinitionElementById(sourceId);
                            if (sourceElementObject !== undefined) {
                                var disconnectedElementSourceName = (sourceElementObject.element).getName();
                                if (!queryInput) {
                                    console.log("Join query output is undefined!");
                                    return;
                                }
                                var firstConnectedElement = queryInput.getFirstConnectedElement();
                                var secondConnectedElement = queryInput.getSecondConnectedElement();
                                if (!firstConnectedElement && !secondConnectedElement) {
                                    console.log("firstConnectedElement and secondConnectedElement are undefined!");
                                } else if (firstConnectedElement !== undefined
                                    && firstConnectedElement.name === disconnectedElementSourceName) {
                                    queryInput.setFirstConnectedElement(undefined);
                                } else if (secondConnectedElement !== undefined
                                    && secondConnectedElement.name === disconnectedElementSourceName) {
                                    queryInput.setSecondConnectedElement(undefined);
                                } else {
                                    console.log("Error: Disconnected source name not found in join query!");
                                    return;
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

                        } else if (sourceElement.hasClass(constants.STREAM)
                            || sourceElement.hasClass(constants.TRIGGER)) {

                            var disconnectedElementName;
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
                            } else if (targetElement.hasClass(constants.SEQUENCE)) {
                                model = self.configurationData.getSiddhiAppConfig().getSequenceQuery(targetId);
                                model.getQueryInput().removeConnectedElementName(disconnectedElementName);
                            }
                        }

                    } else if (targetElement.hasClass(constants.STREAM) || targetElement.hasClass(constants.TABLE)
                        || targetElement.hasClass(constants.WINDOW)) {
                        if (sourceElement.hasClass(constants.PROJECTION) || sourceElement.hasClass(constants.FILTER)
                            || sourceElement.hasClass(constants.WINDOW_QUERY) || sourceElement.hasClass(constants.JOIN)
                            || sourceElement.hasClass(constants.PATTERN)
                            || sourceElement.hasClass(constants.SEQUENCE)) {

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
                            model.getQueryOutput().setTarget(undefined);
                        }
                    }
                });
            }

            function addMemberToPartitionGroup(self) {
                self.jsPlumbInstance.bind('group:addMember', function (event) {

                    var partitionId = $(event.group).attr('id');
                    var partition = self.configurationData.getSiddhiAppConfig().getPartition(partitionId);

                    // check whether member is already added to the group
                    if (partition.isElementInsidePartition(event.el.id)) {
                        return;
                    }

                    var errorMessage = '';
                    var isGroupMemberValid = false;
                    if ($(event.el).hasClass(constants.FILTER) || $(event.el).hasClass(constants.PROJECTION)
                        || $(event.el).hasClass(constants.WINDOW_QUERY) || $(event.el).hasClass(constants.JOIN)
                        || $(event.el).hasClass(constants.SEQUENCE) || $(event.el).hasClass(constants.PATTERN)
                        || $(event.el).hasClass(constants.STREAM)) {

                        var elementId = event.el.id;
                        var sourceConnectionPointId = elementId + '-out';
                        var targetConnectionPointId = elementId + '-in';

                        var noOfSourceConnections
                            = self.jsPlumbInstance.getConnections({source: sourceConnectionPointId});
                        var noOfTargetConnections
                            = self.jsPlumbInstance.getConnections({target: targetConnectionPointId});
                        var totalConnection = noOfSourceConnections.length + noOfTargetConnections.length;
                        if (totalConnection === 0) {
                            isGroupMemberValid = true;

                            if ($(event.el).hasClass(constants.STREAM)) {
                                var streamObject = self.configurationData.getSiddhiAppConfig().getStream(elementId);
                                var streamObjectCopy = _.cloneDeep(streamObject);

                                var streamName = streamObjectCopy.getName() ;
                                var firstCharacterInStreamName = (streamName).charAt(0);
                                if (firstCharacterInStreamName !== '#') {
                                    streamName = '#' + streamName;
                                }
                                // check if there is an inner stream with the same name exists. If yes do not add
                                // the element to the partition
                                var isStreamNameUsed
                                    = self.dropElements.formBuilder.formUtils
                                    .isStreamDefinitionNameUsedInPartition(partitionId, streamName);
                                if (!isStreamNameUsed) {
                                    streamObjectCopy.setName(streamName);
                                    var textNode = $('#' + elementId).parent().find('.streamNameNode');
                                    textNode.html(streamName);
                                    self.configurationData.getSiddhiAppConfig().removeStream(elementId);
                                    partition.addStream(streamObjectCopy);
                                } else {
                                    isGroupMemberValid = false;
                                    errorMessage = ' An inner stream with the same name is already added to the ' +
                                        'partition.';
                                }
                            } else if ($(event.el).hasClass(constants.PROJECTION)) {
                                var projectionQueryObject = self.configurationData.getSiddhiAppConfig()
                                    .getWindowFilterProjectionQuery(elementId);
                                var projectionQueryObjectCopy = _.cloneDeep(projectionQueryObject);
                                self.configurationData.getSiddhiAppConfig().removeWindowFilterProjectionQuery(elementId);
                                partition.addWindowFilterProjectionQuery(projectionQueryObjectCopy);

                            } else if ($(event.el).hasClass(constants.FILTER)) {
                                var filterQueryObject = self.configurationData.getSiddhiAppConfig()
                                    .getWindowFilterProjectionQuery(elementId);
                                var filterQueryObjectCopy = _.cloneDeep(filterQueryObject);
                                self.configurationData.getSiddhiAppConfig().removeWindowFilterProjectionQuery(elementId);
                                partition.addWindowFilterProjectionQuery(filterQueryObjectCopy);

                            } else if ($(event.el).hasClass(constants.WINDOW_QUERY)) {
                                var windowQueryObject = self.configurationData.getSiddhiAppConfig()
                                    .getWindowFilterProjectionQuery(elementId);
                                var windowQueryObjectCopy = _.cloneDeep(windowQueryObject);
                                self.configurationData.getSiddhiAppConfig().removeWindowFilterProjectionQuery(elementId);
                                partition.addWindowFilterProjectionQuery(windowQueryObjectCopy);

                            } else if ($(event.el).hasClass(constants.PATTERN)) {

                                var patternQueryObject = self.configurationData.getSiddhiAppConfig()
                                    .getPatternQuery(elementId);
                                var patternQueryObjectCopy = _.cloneDeep(patternQueryObject);
                                self.configurationData.getSiddhiAppConfig().removePatternQuery(elementId);
                                partition.addPatternQuery(patternQueryObjectCopy);

                            } else if ($(event.el).hasClass(constants.SEQUENCE)) {
                                var sequenceQueryObject = self.configurationData.getSiddhiAppConfig()
                                    .getSequenceQuery(elementId);
                                var sequenceQueryObjectCopy = _.cloneDeep(sequenceQueryObject);
                                self.configurationData.getSiddhiAppConfig().removeSequenceQuery(elementId);
                                partition.addSequenceQuery(sequenceQueryObjectCopy);

                            } else if ($(event.el).hasClass(constants.JOIN)) {
                                var joinQueryObject = self.configurationData.getSiddhiAppConfig()
                                    .getJoinQuery(elementId);
                                var joinQueryObjectCopy = _.cloneDeep(joinQueryObject);
                                self.configurationData.getSiddhiAppConfig().removeJoinQuery(elementId);
                                partition.addJoinQuery(joinQueryObjectCopy);
                            }
                        }
                    }

                    if (!isGroupMemberValid) {
                        DesignViewUtils.prototype.warnAlert('This element cannot be added to partition.' + errorMessage);
                        self.jsPlumbInstance.removeFromGroup(event.group, event.el, false);
                        var elementClientX = $(event.el).attr('data-x');
                        var elementClientY = $(event.el).attr('data-y');
                        var detachedElement = $(event.el).detach();
                        detachedElement.css({
                            left: parseInt(elementClientX) - self.canvas.offset().left,
                            top: parseInt(elementClientY) - self.canvas.offset().top
                        });
                        self.canvas.append(detachedElement);
                        self.jsPlumbInstance.repaintEverything();
                    }
                });
            }

            checkConnectionValidityBeforeElementDrop();

            updateModelOnConnectionAttach();

            updateModelOnBeforeConnectionDetach();

            updateModelOnConnectionDetach();

            addMemberToPartitionGroup(self);

            self.drawGraphFromAppData();
        };

        DesignGrid.prototype.drawGraphFromAppData = function () {
            var self = this;

            _.forEach(self.configurationData.getSiddhiAppConfig().getSourceList(), function(source){
                var sourceId = source.getId();
                var sourceName = "Source";
                var array = sourceId.split("_");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleSourceAnnotation(mouseTop, mouseLeft, true, sourceName, sourceId);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getSinkList(), function(sink){
                var sinkId = sink.getId();
                var sinkName = "Sink";
                var array = sinkId.split("_");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleSinkAnnotation(mouseTop, mouseLeft, true, sinkName, sinkId);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getStreamList(), function(stream){
                var streamId = stream.getId();
                var streamName = stream.getName();
                var array = streamId.split("_");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleStream(mouseTop, mouseLeft, true, streamId, streamName);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getTableList(), function(table){

                var tableId = table.getId();
                var tableName = table.getName();
                var array = tableId.split("_");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleTable(mouseTop, mouseLeft, true, tableId, tableName);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getWindowList(), function(window){

                var windowId = window.getId();
                var windowName = window.getName();
                var array = windowId.split("_");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleWindow(mouseTop, mouseLeft, true, windowId, windowName);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getTriggerList(), function(trigger){

                var triggerId = trigger.getId();
                var triggerName = trigger.getName();
                var array = triggerId.split("_");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleTrigger(mouseTop, mouseLeft, true, triggerId, triggerName);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getAggregationList(), function(aggregation){

                var aggregationId = aggregation.getId();
                var aggregationName = aggregation.getName();
                var array = aggregationId.split("_");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleAggregation(mouseTop, mouseLeft, true, aggregationId, aggregationName);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getFunctionList(), function(functionObject){

                var functionId = functionObject.getId();
                var functionName = functionObject.getName();
                var array = functionId.split("_");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleFunction(mouseTop, mouseLeft, true, functionId, functionName);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getPatternQueryList(), function(patternQuery){

                var patternQueryId = patternQuery.getId();
                var patternQueryName = "Pattern";
                var array = patternQueryId.split("_");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handlePatternQuery(mouseTop, mouseLeft, true, patternQueryName, patternQueryId);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getSequenceQueryList(), function(sequenceQuery){

                var sequenceQueryId = sequenceQuery.getId();
                var sequenceQueryName = "Sequence";
                var array = sequenceQueryId.split("_");
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

                    var array = queryId.split("_");
                    var lastArrayEntry = parseInt(array[array.length - 1]);
                    var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                    var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                    self.handleWindowFilterProjectionQuery(queryType, mouseTop, mouseLeft, true, queryName, queryId);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getJoinQueryList(), function(joinQuery){

                var joinQueryId = joinQuery.getId();
                var joinQueryName = "Join";
                var array = joinQueryId.split("_");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleJoinQuery(mouseTop, mouseLeft, true, joinQueryName, joinQueryId);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getPartitionList(), function(partition){

                var partitionId = partition.getId();
                var array = partitionId.split("_");
                var lastArrayEntry = parseInt(array[array.length -1]);
                var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handlePartition(mouseTop, mouseLeft, true, partitionId);

                var jsPlumbPartitionGroup = self.jsPlumbInstance.getGroup(partitionId);

                _.forEach(partition.getStreamList(), function(stream){
                    var streamId = stream.getId();
                    var streamName = stream.getName();
                    var array = streamId.split("_");
                    var lastArrayEntry = parseInt(array[array.length -1]);
                    var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                    var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                    self.handleStream(mouseTop, mouseLeft, true, streamId, streamName);

                    var streamElement = $('#' + streamId)[0];
                    self.jsPlumbInstance.addToGroup(jsPlumbPartitionGroup, streamElement);
                });

                _.forEach(partition.getPatternQueryList(), function(patternQuery){

                    var patternQueryId = patternQuery.getId();
                    var patternQueryName = "Pattern";
                    var array = patternQueryId.split("_");
                    var lastArrayEntry = parseInt(array[array.length -1]);
                    var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                    var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                    self.handlePatternQuery(mouseTop, mouseLeft, true, patternQueryName, patternQueryId);

                    var patternElement = $('#' + patternQueryId)[0];
                    self.jsPlumbInstance.addToGroup(jsPlumbPartitionGroup, patternElement);
                });

                _.forEach(partition.getSequenceQueryList(), function(sequenceQuery){

                    var sequenceQueryId = sequenceQuery.getId();
                    var sequenceQueryName = "Sequence";
                    var array = sequenceQueryId.split("_");
                    var lastArrayEntry = parseInt(array[array.length -1]);
                    var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                    var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                    self.handleSequenceQuery(mouseTop, mouseLeft, true, sequenceQueryName, sequenceQueryId);

                    var sequenceElement = $('#' + sequenceQueryId)[0];
                    self.jsPlumbInstance.addToGroup(jsPlumbPartitionGroup, sequenceElement);
                });

                _.forEach(partition.getWindowFilterProjectionQueryList(),
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

                        var array = queryId.split("_");
                        var lastArrayEntry = parseInt(array[array.length - 1]);
                        var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                        var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                        self.handleWindowFilterProjectionQuery(queryType, mouseTop, mouseLeft, true, queryName, queryId);

                        var queryElement = $('#' + queryId)[0];
                        self.jsPlumbInstance.addToGroup(jsPlumbPartitionGroup, queryElement);
                    });

                _.forEach(partition.getJoinQueryList(), function(joinQuery){

                    var joinQueryId = joinQuery.getId();
                    var joinQueryName = "Join";
                    var array = joinQueryId.split("_");
                    var lastArrayEntry = parseInt(array[array.length -1]);
                    var mouseTop = lastArrayEntry*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                    var mouseLeft = lastArrayEntry*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                    self.handleJoinQuery(mouseTop, mouseLeft, true, joinQueryName, joinQueryId);

                    var joinElement = $('#' + joinQueryId)[0];
                    self.jsPlumbInstance.addToGroup(jsPlumbPartitionGroup, joinElement);
                });
            });

            _.forEach(self.configurationData.edgeList, function(edge){
                var targetId;
                var sourceId;

                if (edge.getChildType() === 'PARTITION') {
                    targetId = edge.getChildId();
                    sourceId = edge.getParentId() + '-out';
                } else if (edge.getParentType() === 'PARTITION') {
                    targetId = edge.getChildId() + '-in';
                    sourceId = edge.getParentId();
                } else {
                    targetId = edge.getChildId() + '-in';
                    sourceId = edge.getParentId() + '-out';
                }

                self.jsPlumbInstance.connect({
                    source: sourceId,
                    target: targetId
                });
            });

            // re-align the elements
            self.autoAlignElements();
        };

        /**
         * @function Auto align and center the diagram
         */
        DesignGrid.prototype.autoAlignElements = function () {
            var self = this;
            // Create a new graph instance
            var graph = new dagre.graphlib.Graph({compound: true});
            // Sets the graph to grow from left to right, and also to separate the distance between each node
            graph.setGraph({rankdir: 'LR', edgesep: 10, ranksep: 100, nodesep: 50});
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

            // Create an empty JSON to store information of the given graph's nodes, edges and groups.
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
            // Get the edge information and add it too graphJSON.edges[] array
            // Note - This loop is used to exclude the edges between nodes and partition connections
            edges.forEach(function (edge) {
                var source;
                var target;
                var sourceId;
                var targetId;
                // Get the current edge's parent and child Id's
                var parent = edge.sourceId;
                var child = edge.targetId;
                // If the current edge's parent is a partition connection
                if (parent.includes('_pc')) {
                    // Loop through the edges again
                    edges.forEach(function (value) {
                        // Get the inner loops edge (value) targetId as child
                        child = value.targetId;
                        // If child is a partition connection
                        if (child.includes('_pc')) {
                            // If the parent partition connection Id is equal to the child partition connection Id
                            if (parent === child) {
                                // Link inner edge's (value) sourceId with the outer edge's target Id as a single edge
                                source = value.sourceId;
                                target = edge.targetId;
                                sourceId = source.substr(0, source.indexOf('-'));
                                targetId = target.substr(0, target.indexOf('-'));
                            }
                        }
                    });
                } else if (!child.includes('_pc')) {
                    // If the child of the current edge is *not* a partition connection
                    source = edge.sourceId;
                    target = edge.targetId;
                    sourceId = source.substr(0, source.indexOf('-'));
                    targetId = target.substr(0, target.indexOf('-'));
                }
                // Set the sourceId and targetId to graphJSON if they are not undefined
                if (sourceId !== undefined && targetId !== undefined) {
                    graphJSON.edges[i] = {
                        parent: sourceId,
                        child: targetId
                    };
                    i++;
                }

            });
            // Once the needed edge information has been obtained and added to the graphJSON variable
            // then the edges can be set to the dagre graph variable.
            graphJSON.edges.forEach(function (edge) {
                graph.setEdge(edge.parent, edge.child);
            });

            // For every group/partition element
            i = 0;
            var groups = [];
            Array.prototype.push.apply(groups, currentTabElement.getElementsByClassName(constants.PARTITION));
            groups.forEach(function (partition) {
                // Add the group information to the graphJSON object
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

            // Set the default minimum and maximum coordinates to zero
            var minimumCoordinate = {x: 0, y: 0};
            var maximumCoordinate = {x: 0, y: 0};
            // Traverse through every node and find the minimum and maximum x & y coordinates
            // The minimum and maximum x & y coordinates have to be found to obtain the size of the graph
            graph.nodes().forEach(function (nodeId) {
                // Get the instance of the dagre node of 'nodeId'
                var node = graph.node(nodeId);
                // Get the minimum x & y coordinates of the current node
                var minX = node.x - (node.width / 2);
                var minY = node.y - (node.height / 2);
                // Get the maximum x & y coordinates of the current node
                var maxX = node.x + (node.width / 2);
                var maxY = node.y + (node.height / 2);
                // Find the minimum and maximum 'x' coordinates from all nodes
                if (maxX > maximumCoordinate.x || maximumCoordinate.x === 0) {
                    maximumCoordinate.x = maxX;
                }
                if (minX < minimumCoordinate.x || minimumCoordinate.x === 0) {
                    minimumCoordinate.x = minX;
                }
                // Find the minimum and maximum 'y' coordinates from all the nodes
                if (maxY > maximumCoordinate.y || maximumCoordinate.y === 0) {
                    maximumCoordinate.y = maxY;
                }
                if (minY < minimumCoordinate.y || minimumCoordinate.y === 0) {
                    minimumCoordinate.y = minY;
                }
            });
            // Obtain the width and the height of the current design-grid instance
            var gridWidth = this.designGridContainer.width();
            var gridHeight = this.designGridContainer.height();
            // The difference in the largest and smallest 'x' coordinates gives the graph width
            var graphWidth = maximumCoordinate.x - minimumCoordinate.x;
            // The difference in the largest and smallest 'y' coordinates gives the graph height
            var graphHeight = maximumCoordinate.y - minimumCoordinate.y;
            // Set the centerLeft and centerTop coordinates to default 20
            // NOTE - The 'centerLeft' and 'centerTop' variables are the values that have to be added
            // to the final 'left' and 'top' CSS positions of the graph to align the graph to the
            // center of the design-grid
            var centerLeft = 20;
            var centerTop = 20;
            if (gridWidth > graphWidth) {
                centerLeft = (gridWidth - graphWidth) / 2;
            }
            if (gridHeight > graphHeight) {
                centerTop = (gridHeight - graphHeight) / 2;
            }

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
                        if (nodeId === child) {
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
                    var partitionNodeLeft = partitionNode.x - (partitionNode.width / 2) + centerLeft;
                    var partitionNodeTop = partitionNode.y - (partitionNode.height / 2) + centerTop;
                    // Identify the node's left and top position relative to it's partition's top and left position
                    var left = node.x - (node.width / 2) + centerLeft - partitionNodeLeft;
                    var top = node.y - (node.height / 2) + centerTop - partitionNodeTop;
                    // Set the inner node's left and top position
                    $node.css("left", left + "px");
                    $node.css("top", top + "px");
                } else {
                    // If the node is not in a partition then it's left and top positions are obtained relative to
                    // the entire grid
                    var left = node.x - (node.width / 2) + centerLeft;
                    var top = node.y - (node.height / 2) + centerTop;
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

        DesignGrid.prototype.handleTrigger = function (mouseTop, mouseLeft, isCodeToDesignMode, triggerId,
                                                       triggerName) {
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

        DesignGrid.prototype.handleAggregation = function (mouseTop, mouseLeft, isCodeToDesignMode, aggregationId,
                                                           aggregationName) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
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
            self.dropElements.dropAggregation(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode,
                aggregationName);
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

        DesignGrid.prototype.generateNextConnectionPointIdForPartition = function (partitionId) {
            var partitionElement = $('#' + partitionId);
            var partitionConnections = partitionElement.find('.' + constants.PARTITION_CONNECTION_POINT);
            var partitionIds = [];
            _.forEach(partitionConnections, function(connection){
                partitionIds.push(parseInt((connection.id).slice(-1)));
            });

            var maxId = partitionIds.reduce(function(a, b) {
                return Math.max(a, b);
            });
            return partitionId + '_pc' + (maxId + 1);
        };

        return DesignGrid;
    });
