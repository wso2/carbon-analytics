/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'log', 'jquery', 'backbone', 'lodash', 'designViewUtils', 'dropElements', 'dagre', 'edge',
        'windowFilterProjectionQueryInput', 'joinQueryInput', 'patternOrSequenceQueryInput', 'queryOutput',
        'partitionWith', 'jsonValidator', 'constants', 'dragSelect'],

    function (require, log, $, Backbone, _, DesignViewUtils, DropElements, dagre, Edge,
              WindowFilterProjectionQueryInput, JoinQueryInput, PatternOrSequenceQueryInput, QueryOutput,
              PartitionWith, JSONValidator, Constants) {

        const TAB_INDEX = 10;
        const ENTER_KEY = 13;
        const LEFT_ARROW_KEY = 37;
        const RIGHT_ARROW_KEY = 39;
        const ESCAPE_KEY = 27;
        const TAB_KEY = 9;
        var constants = {
            SOURCE: 'sourceDrop',
            SINK: 'sinkDrop',
            STREAM: 'streamDrop',
            TABLE: 'tableDrop',
            WINDOW: 'windowDrop',
            TRIGGER: 'triggerDrop',
            AGGREGATION: 'aggregationDrop',
            FUNCTION: 'functionDrop',
            PROJECTION: 'projectionQueryDrop',
            FILTER: 'filterQueryDrop',
            JOIN: 'joinQueryDrop',
            WINDOW_QUERY: 'windowQueryDrop',
            FUNCTION_QUERY: 'functionQueryDrop',
            PATTERN: 'patternQueryDrop',
            SEQUENCE: 'sequenceQueryDrop',
            PARTITION: 'partitionDrop',
            PARTITION_CONNECTION_POINT: 'partition-connector-in-part',
            SELECTOR: 'selector',
            MULTI_SELECTOR: 'multi-selector',
            TYPE_CALL: "call",
            TYPE_CALL_RESPONSE: "call-response",
            TYPE_HTTP_RESPONSE: "http-response"
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
            this.rawExtensions = options.rawExtensions;
            this.configurationData = this.options.configurationData;
            this.container = this.options.container;
            this.application = this.options.application;
            this.jsPlumbInstance = options.jsPlumbInstance;
            this.currentTabId = this.application.tabController.activeTab.cid;
            this.designViewContainer = $('#design-container-' + this.currentTabId);
            this.toggleViewButton = $('#toggle-view-button-' + this.currentTabId);
            this.designGridContainer = $('#design-grid-container-' + this.currentTabId);
            this.selectedElements = [];
            this.selectedObjects = [];
        };


        DesignGrid.prototype.addSelectedElements = function(element){
            this.selectedElements.push(element);
        };

        DesignGrid.prototype.isSelectedElements = function(element){
            if (this.selectedElements.includes(element)){
                return true;
            } else{
                return false;
            }
        };

        DesignGrid.prototype.getSelectedElement = function(){
            return this.selectedElements;
        };

        DesignGrid.prototype.resetSelectedElement = function(){
            this.selectedElements = [];
        };

        DesignGrid.prototype.removeFromSelectedElements = function(element){
            for (var i = 0; i < this.selectedElements.length; i++) {
                if (this.selectedElements[i] == element) {
                    this.selectedElements.splice(i, 1);
                }
            }
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
            var settingsButton = $("<div id='" + settingsButtonId + "' " +
                "class='btn app-annotations-button' " +
                "data-placement='bottom' data-toggle='tooltip' title='App Annotations'>" +
                "<i class='fw fw-settings'></i></div>");
            settingsButton.tooltip();
            self.canvas.append(settingsButton);
            var settingsIconElement = $('#' + settingsButtonId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.dropElements.formBuilder.DefineFormForAppAnnotations(this);
            });

            // add text fields to display the siddhi app name and description
            var siddhiAppNameNodeId = self.currentTabId + '-siddhiAppNameId';
            var siddhiAppName = self.configurationData.getSiddhiAppConfig().getSiddhiAppName();
            var siddhiAppNameNode = $("<div id='" + siddhiAppNameNodeId + "' " +
                "class='siddhi-app-name-node'>" + siddhiAppName + "</div>");
            var siddhiAppDescription = self.configurationData.getSiddhiAppConfig().getSiddhiAppDescription();
            var siddhiAppDescriptionNode = $("<div id='siddhi-app-desc-node'>" + siddhiAppDescription + "</div>");
            self.canvas.append(siddhiAppNameNode, siddhiAppDescriptionNode);

            /**
             * @description jsPlumb function opened
             */
            self.jsPlumbInstance.ready(function () {

                self.jsPlumbInstance.importDefaults({
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
                    '.function-drag, .function-query-drag',
                    containment: 'grid-container',

                    /**
                     *
                     * @param e --> original event object fired/ normalized by jQuery
                     * @param ui --> object that contains additional info added by jQuery depending on which
                     * interaction was used
                     * @helper clone
                     */

                    drop: function (e, ui) {
                        var mouseTop = e.pageY - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                        var mouseLeft = e.pageX - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
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

                        // If the dropped Element is a Function Query then->
                        else if ($(droppedElement).hasClass('function-query-drag')) {
                            self.handleWindowFilterProjectionQuery(constants.FUNCTION_QUERY, mouseTop, mouseLeft, false,
                                "Query");
                        }

                        // If the dropped Element is a Join Query then->
                        else if ($(droppedElement).hasClass('join-query-drag')) {
                            self.handleJoinQuery(mouseTop, mouseLeft, false, "Join Query");
                        }

                        // If the dropped Element is a Pattern Query then->
                        else if ($(droppedElement).hasClass('pattern-query-drag')) {
                            self.handlePatternQuery(mouseTop, mouseLeft, false, "Pattern Query");
                        }

                        // If the dropped Element is a Sequence Query then->
                        else if ($(droppedElement).hasClass('sequence-query-drag')) {
                            self.handleSequenceQuery(mouseTop, mouseLeft, false, "Sequence Query");
                        }

                        // If the dropped Element is a Partition then->
                        else if ($(droppedElement).hasClass('partition-drag')) {
                            self.handlePartition(mouseTop, mouseLeft, false);
                        }

                        // set the isDesignViewContentChanged to true
                        self.configurationData.setIsDesignViewContentChanged(true);
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

                    } else if (targetElement.hasClass(constants.STREAM)
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

                                var isStreamDirectlyConnectedToAQuery = false;
                                _.forEach(partition.getWindowFilterProjectionQueryList(), function (query) {
                                    if (query.getQueryInput() !== undefined
                                        && query.getQueryInput().getConnectedSource() !== undefined
                                        && query.getQueryInput().getConnectedSource() === connectedStreamName) {
                                        isStreamDirectlyConnectedToAQuery = true;
                                    }
                                });

                                _.forEach(partition.getJoinQueryList(), function (query) {
                                    if (query.getQueryInput() !== undefined) {
                                        if (query.getQueryInput().getFirstConnectedElement() !== undefined
                                            && query.getQueryInput().getFirstConnectedElement().type
                                            === constants.STREAM
                                            && query.getQueryInput().getFirstConnectedElement().name
                                            === connectedStreamName) {
                                            isStreamDirectlyConnectedToAQuery = true;

                                        } else if (query.getQueryInput().getSecondConnectedElement() !== undefined
                                            && query.getQueryInput().getSecondConnectedElement().type
                                            === constants.STREAM
                                            && query.getQueryInput().getSecondConnectedElement().name
                                            === connectedStreamName) {
                                            isStreamDirectlyConnectedToAQuery = true;
                                        }
                                    }
                                });

                                _.forEach(partition.getPatternQueryList(), function (query) {
                                    if (query.getQueryInput() !== undefined) {
                                        var connectedElementNameList
                                            = query.getQueryInput().getConnectedElementNameList();
                                        _.forEach(connectedElementNameList, function (elementName) {
                                            if (connectedStreamName === elementName) {
                                                isStreamDirectlyConnectedToAQuery = true;
                                            }
                                        });
                                    }
                                });

                                _.forEach(partition.getSequenceQueryList(), function (query) {
                                    if (query.getQueryInput() !== undefined) {
                                        var connectedElementNameList
                                            = query.getQueryInput().getConnectedElementNameList();
                                        _.forEach(connectedElementNameList, function (elementName) {
                                            if (connectedStreamName === elementName) {
                                                isStreamDirectlyConnectedToAQuery = true;
                                            }
                                        });
                                    }
                                });

                                if (isStreamDirectlyConnectedToAQuery) {
                                    DesignViewUtils.prototype.errorAlert("Invalid Connection: Connected stream is " +
                                        "already directly connected to a query inside the partition.");
                                    return connectionValidity;
                                } else {
                                    return connectionValidity = true;
                                }
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
                                .errorAlert("Invalid Connection: Connect to a query input inside the partition");
                            return connectionValidity;
                        } else {
                            if (targetElement.hasClass(constants.PROJECTION)
                                || targetElement.hasClass(constants.FILTER)
                                || targetElement.hasClass(constants.WINDOW_QUERY)
                                || targetElement.hasClass(constants.FUNCTION_QUERY)
                                || targetElement.hasClass(constants.PATTERN)
                                || targetElement.hasClass(constants.JOIN)
                                || targetElement.hasClass(constants.SEQUENCE)) {
                                return connectionValidity = true;
                            } else {
                                DesignViewUtils.prototype
                                    .errorAlert("Invalid Connection: Connect to a query input inside the partition");
                                return connectionValidity;
                            }
                        }

                    } else if (sourceElement.hasClass(constants.PARTITION)) {
                        if ($(self.jsPlumbInstance.getGroupFor(targetId)).attr('id') == sourceId) {
                            return connectionValidity = true;
                        }
                        DesignViewUtils.prototype.errorAlert("Invalid Connection: Connect to a partition query");
                        return connectionValidity;

                    } else if (sourceElement.hasClass(constants.SOURCE)) {
                        if (!targetElement.hasClass(constants.STREAM)) {
                            DesignViewUtils.prototype.errorAlert("Invalid Connection: Connect to a stream");
                            return connectionValidity;
                        } else {
                            return connectionValidity = true;
                        }

                    } else if (targetElement.hasClass(constants.SINK)) {
                        if (sourceElement.hasClass(constants.STREAM)) {
                            return connectionValidity = true;
                        }
                        DesignViewUtils.prototype
                            .errorAlert("Invalid Connection: Sink input source should be a stream");
                        return connectionValidity;

                    } else if (targetElement.hasClass(constants.AGGREGATION)) {
                        if (!(sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TRIGGER))) {
                            DesignViewUtils.prototype
                                .errorAlert("Invalid Connection: Aggregation input should be a stream or a trigger");
                            return connectionValidity;
                        } else {
                            return connectionValidity = true;
                        }
                    } //we allowing all sinks to be connected to source here if connections points are available to
                      // connect. This need to be changed if there are use cases to allow specifics to connect
                    else if (targetElement.hasClass(constants.SOURCE)) {
                        if (sourceElement.hasClass(constants.SINK)) {
                            return connectionValidity = true;
                        }

                        var sourceType = targetElement[0].textContent;
                        if (sourceType === constants.TYPE_HTTP_RESPONSE) {
                            DesignViewUtils.prototype
                                .errorAlert("Invalid Connection: Input for the http-response source should be a " +
                                    "http-request sink ");
                        } else if (sourceType.endsWith(constants.TYPE_CALL_RESPONSE)) {
                            DesignViewUtils.prototype
                                .errorAlert("Invalid Connection: Input for the " + sourceType +
                                    " source should be a " + sourceType.slice(0, sourceType.indexOf("-")) +
                                    "call-request sink ");
                        }

                        return connectionValidity;
                    }

                    // When connecting streams to a query inside the partition if it is connected to the partition
                    // connection point, it cannot connect to the query directly
                    if ((targetElement.hasClass(constants.PROJECTION) || targetElement.hasClass(constants.FILTER)
                            || targetElement.hasClass(constants.WINDOW_QUERY) || targetElement.hasClass(constants.JOIN)
                            || targetElement.hasClass(constants.FUNCTION_QUERY) || targetElement.hasClass(constants.PATTERN)
                            || targetElement.hasClass(constants.SEQUENCE))
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
                        if (!(sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TRIGGER))) {
                            DesignViewUtils.prototype.errorAlert("Invalid Connection");
                            return connectionValidity;
                        } else {
                            return connectionValidity = true;
                        }

                    } else if (targetElement.hasClass(constants.PROJECTION) || targetElement.hasClass(constants.FILTER)
                        || targetElement.hasClass(constants.WINDOW_QUERY)
                        || targetElement.hasClass(constants.FUNCTION_QUERY)) {
                        if (!(sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.WINDOW)
                                || sourceElement.hasClass(constants.TRIGGER))) {
                            DesignViewUtils.prototype.errorAlert("Invalid Connection");
                            return connectionValidity;
                        } else {
                            return connectionValidity = true;
                        }

                    } else if (targetElement.hasClass(constants.JOIN)) {
                        if (!(sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TABLE)
                                || sourceElement.hasClass(constants.AGGREGATION)
                                || sourceElement.hasClass(constants.TRIGGER)
                                || sourceElement.hasClass(constants.WINDOW))) {
                            DesignViewUtils.prototype.errorAlert("Invalid Connection");
                            return connectionValidity;
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
                                return connectionValidity = true;
                            } else {
                                var firstConnectedElement = queryInput.getFirstConnectedElement();
                                var secondConnectedElement = queryInput.getSecondConnectedElement();
                                if (!firstConnectedElement && !secondConnectedElement) {
                                    return connectionValidity = true;
                                } else if (firstConnectedElement !== undefined
                                    && secondConnectedElement !== undefined) {
                                    DesignViewUtils.prototype
                                        .errorAlert("Invalid Connection: Only two input elements are allowed to " +
                                            "connect in join query!");
                                    return connectionValidity = false;
                                } else if (firstConnectedElement !== undefined
                                    && !secondConnectedElement) {
                                    var firstElementType = firstConnectedElement.type;
                                    if (firstElementType === 'STREAM' || firstElementType === 'TRIGGER'
                                        || firstElementType === 'WINDOW') {
                                        return connectionValidity = true;
                                    } else if (connectedElementSourceType === 'STREAM'
                                        || connectedElementSourceType === 'TRIGGER'
                                        || connectedElementSourceType === 'WINDOW') {
                                        return connectionValidity = true;
                                    } else {
                                        DesignViewUtils.prototype
                                            .errorAlert("Invalid Connection: At least one connected input element in " +
                                                "join query should be a stream or a trigger or a window!");
                                        return connectionValidity = false;
                                    }
                                } else if (!firstConnectedElement && secondConnectedElement !== undefined) {
                                    var secondElementType = secondConnectedElement.type;
                                    if (secondElementType === 'STREAM' || secondElementType === 'TRIGGER'
                                        || secondElementType === 'WINDOW') {
                                        return connectionValidity = true;
                                    } else if (connectedElementSourceType === 'STREAM'
                                        || connectedElementSourceType === 'TRIGGER'
                                        || connectedElementSourceType === 'WINDOW') {
                                        return connectionValidity = true;
                                    } else {
                                        DesignViewUtils.prototype
                                            .errorAlert("Invalid Connection: At least one connected input element in " +
                                                "join query should be a stream or a trigger or a window!");
                                        return connectionValidity = false;
                                    }
                                }
                            }
                        }

                    } else if (sourceElement.hasClass(constants.PROJECTION) || sourceElement.hasClass(constants.FILTER)
                        || sourceElement.hasClass(constants.WINDOW_QUERY) || sourceElement.hasClass(constants.PATTERN)
                        || sourceElement.hasClass(constants.JOIN) || sourceElement.hasClass(constants.SEQUENCE)
                        || sourceElement.hasClass(constants.FUNCTION_QUERY)) {
                        if (!(targetElement.hasClass(constants.STREAM) || targetElement.hasClass(constants.TABLE)
                                || targetElement.hasClass(constants.WINDOW))) {
                            DesignViewUtils.prototype.errorAlert("Invalid Connection");
                            return connectionValidity;
                        } else {
                            return connectionValidity = true;
                        }
                    }
                    if (!connectionValidity) {
                        DesignViewUtils.prototype.errorAlert("Invalid Connection");
                    }
                    return connectionValidity;
                });
            }

            // Update the model when a connection is established and bind events for the connection
            function updateModelOnConnectionAttach() {
                self.jsPlumbInstance.bind('connection', function (connection) {

                    // set the isDesignViewContentChanged to true
                    self.configurationData.setIsDesignViewContentChanged(true);

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
                    var isFromFaultStream = connection.sourceId.substr(connection.sourceId.indexOf('-')) === '-err-out';
                    if (sourceId === '') {
                        sourceId = source;
                        sourceType = 'PARTITION';
                    } else {
                        if (isFromFaultStream) {
                            // Change the source id to the respective source id of the fault stream
                            var streamMap = {};
                            var eventStreamName = '';
                            self.configurationData.getSiddhiAppConfig().getStreamList().forEach(function(s) {
                                streamMap[s.getName()] = s;
                                if (s.getId() === sourceId) {
                                    eventStreamName = s.getName();
                                }
                            });
                            sourceId = streamMap[Constants.FAULT_STREAM_PREFIX + eventStreamName].getId();
                        }
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

                    var edgeId = '' + sourceId + '_' + targetId + '';
                    var edgeInTheEdgeList = self.configurationData.getEdge(edgeId);
                    if (!edgeInTheEdgeList) {
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
                        && sourceElement.hasClass(constants.STREAM)) {
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
                            deleteEndpointsOnDetach: true
                        });
                        self.jsPlumbInstance.makeSource(connectionIn, {
                            anchor: 'Right',
                            deleteEndpointsOnDetach: true
                        });

                    } else if (sourceElement.hasClass(constants.SOURCE) && targetElement.hasClass(constants.STREAM)) {
                        connectedElementName = self.configurationData.getSiddhiAppConfig().getStream(targetId)
                            .getName();
                        self.configurationData.getSiddhiAppConfig().getSource(sourceId)
                            .setConnectedElementName(connectedElementName);

                    } else if (targetElement.hasClass(constants.SINK) && sourceElement.hasClass(constants.STREAM)) {
                        connectedElementName = self.configurationData.getSiddhiAppConfig().getStream(sourceId)
                            .getName();
                        self.configurationData.getSiddhiAppConfig().getSink(targetId)
                            .setConnectedElementName(connectedElementName);

                    } else if (sourceElement.hasClass(constants.SINK) && targetElement.hasClass(constants.SOURCE)) {
                        connectedElementName = self.configurationData.getSiddhiAppConfig().getSource(targetId)
                            .getType();
                        self.configurationData.getSiddhiAppConfig().getSink(sourceId)
                            .setConnectedRightElementName(connectedElementName);

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
                            .setConnectedSource(connectedElementName);

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
                                || targetElement.hasClass(constants.WINDOW_QUERY)
                                || targetElement.hasClass(constants.FUNCTION_QUERY))) {
                            model = self.configurationData.getSiddhiAppConfig()
                                .getWindowFilterProjectionQuery(targetId);
                            var type;
                            if (targetElement.hasClass(constants.PROJECTION)) {
                                type = 'PROJECTION';
                            }
                            else if (targetElement.hasClass(constants.FILTER)) {
                                type = 'FILTER';
                            }
                            else if (targetElement.hasClass(constants.WINDOW_QUERY)) {
                                type = 'WINDOW';
                            }
                            else if (targetElement.hasClass(constants.FUNCTION_QUERY)) {
                                type = 'FUNCTION';
                            }
                            if (!model.getQueryInput()) {
                                var queryInputOptions = {};
                                _.set(queryInputOptions, 'type', type);
                                _.set(queryInputOptions, 'from', connectedElementName);
                                var queryInputObject = new WindowFilterProjectionQueryInput(queryInputOptions);
                                model.setQueryInput(queryInputObject);
                            } else {
                                model.getQueryInput().setConnectedSource(connectedElementName);
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
                                    if (queryInput.getLeft() && queryInput.getRight()) {
                                        if (queryInput.getLeft().getConnectedSource() === queryInput.getRight()
                                            .getConnectedSource()) {
                                            queryInput.setFirstConnectedElement(connectedElement);
                                            queryInput.setSecondConnectedElement(connectedElement);
                                        }
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
                            || sourceElement.hasClass(constants.FUNCTION_QUERY)
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
                                || sourceElement.hasClass(constants.WINDOW_QUERY)
                                || sourceElement.hasClass(constants.FUNCTION_QUERY)) {
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

                    // do not check for json validity if the design is still generating from the data sent from backend
                    if (!self.configurationData.getIsStillDrawingGraph()) {
                        if (sourceType === 'PARTITION') {
                            sourceId = sourceElement.parent().attr('id');
                        } else if (targetType === 'PARTITION') {
                            targetId = targetElement.parent().attr('id');
                        }
                        // validate source and target elements
                        checkJSONValidityOfElement(self, sourceId, true);
                        checkJSONValidityOfElement(self, targetId, true);
                    }

                    var connectionObject = connection.connection;
                    // add a overlay of a close icon for connection. connection can be detached by clicking on it
                    var close_icon_overlay = connectionObject.addOverlay([
                        "Custom", {
                            create: function () {
                                return $(
                                    '<span><i class="fw fw-delete" id="' + self.currentTabId + connectionObject.id +
                                    '"data-toggle="popover"></i></span>');
                            },
                            location: 0.60,
                            id: "close",
                            events: {
                                click: function () {
                                    popOverForConnector();
                                }
                            }
                        }
                    ]);

                    function popOverForConnector() {
                        $('#' + self.currentTabId + connectionObject.id).popover({
                            trigger: 'focus',
                            title: 'Confirmation',
                            html: true,
                            content: function () {
                                return $('.pop-over').html();

                            }
                        });
                        $('#' + self.currentTabId + connectionObject.id).off();
                        $('#' + self.currentTabId + connectionObject.id).popover("show");
                        $('.btn_no').focus();
                        $(".overlayed-container ").fadeTo(200, 1);
                        // Custom jQuery to hide popover on click of the close button
                        $("#" + self.currentTabId + connectionObject.id).siblings(".popover").on("click", ".popover-footer .btn_yes",
                            function () {
                                if (connectionObject.connector !== null) {
                                    self.jsPlumbInstance.deleteConnection(connectionObject);
                                }
                                $(".overlayed-container ").fadeOut(200);
                                $(this).parents(".popover").popover('hide');
                            });
                        $("#" + self.currentTabId + connectionObject.id).siblings(".popover").on("click", ".popover-footer .btn_no",
                            function () {
                                $(".overlayed-container ").fadeOut(200);
                                $(this).parents(".popover").popover('hide');
                                close_icon_overlay.setVisible(false);
                            });
                        // Dismiss the pop-over by clicking outside
                        $('.overlayed-container ').off('click');
                        $('.overlayed-container ').on('click', function (e) {
                            $('[data-toggle="popover"]').each(function () {
                                if (!$(this).is(e.target) && $(this).has(e.target).length === 0 && $('.popover').has(
                                        e.target).length === 0) {
                                    $(this).popover('hide');
                                    $(".overlayed-container ").fadeOut(200);
                                    close_icon_overlay.setVisible(false);
                                }
                            });
                        });
                        $(".btn_no").on("keyup", function (e) {
                            if (e.which === ESCAPE_KEY && $("#" + self.currentTabId + connectionObject.id).popover()) {
                                $("#" + self.currentTabId + connectionObject.id).popover('hide');
                                $(".overlayed-container ").fadeOut(200);
                                close_icon_overlay.setVisible(false);
                            }
                        });
                        //Navigation using arrow keys
                        $(".btn_no").on('keydown', function (e) {
                            if (e.keyCode == RIGHT_ARROW_KEY) {
                                $('.btn_yes').focus();
                            }
                        });
                        $(".btn_yes").on('keydown', function (e) {
                            if (e.keyCode == LEFT_ARROW_KEY) {
                                $('.btn_no').focus();
                            }
                        });
                        $(".btn_no").on('keydown', function (e) {
                            if (e.keyCode == TAB_KEY) {
                                e.preventDefault();
                            }
                            if (e.keyCode == ENTER_KEY) {
                                e.stopPropagation();
                            }
                        });
                        //Stop tab propagation and enter propagation when popover showed
                        $(".btn_yes").on('keydown', function (e) {
                            if (e.keyCode == TAB_KEY) {
                                e.preventDefault();
                            }
                            if (e.keyCode == ENTER_KEY) {
                                e.stopPropagation();
                            }
                        });
                    }

                    if (isConnectionMadeInsideAPartition) {
                        close_icon_overlay.setVisible(false);
                        // show the close icon when mouse is over the connection
                        connectionObject.bind('mouseenter', function () {
                            close_icon_overlay.setVisible(true);
                        });
                        // hide the close icon when the mouse is not on the connection path
                        connectionObject.bind('mouseleave', function () {
                            if ($("#" + self.currentTabId + connectionObject.id).siblings(".popover").length == 0) {
                                close_icon_overlay.setVisible(false);
                            }
                        });
                    } else {
                        close_icon_overlay.setVisible(false);
                        // show the close icon when mouse is over the connection
                        connectionObject.bind('mouseover', function () {
                            close_icon_overlay.setVisible(true);
                        });
                        // hide the close icon when the mouse is not on the connection path
                        connectionObject.bind('mouseout', function () {
                            if ($("#" + self.currentTabId + connectionObject.id).siblings(".popover").length == 0) {
                                close_icon_overlay.setVisible(false);
                            }
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
                    var edgeId = '' + sourceId + '_' + targetId + '';
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

                        // validate the partition
                        checkJSONValidityOfElement(self, partitionId, true);
                    }
                });
            }

            // Update the model when a connection is detached
            function updateModelOnConnectionDetach() {
                self.jsPlumbInstance.bind('connectionDetached', function (connection) {

                    // set the isDesignViewContentChanged to true
                    self.configurationData.setIsDesignViewContentChanged(true);

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
                    var edgeId = '' + sourceId + '_' + targetId + '';
                    self.configurationData.removeEdge(edgeId);

                    var model;

                    if (sourceElement.hasClass(constants.SOURCE) && targetElement.hasClass(constants.STREAM)) {
                        self.configurationData.getSiddhiAppConfig().getSource(sourceId)
                            .setConnectedElementName(undefined);

                    } else if (targetElement.hasClass(constants.SINK) && sourceElement.hasClass(constants.STREAM)) {
                        self.configurationData.getSiddhiAppConfig().getSink(targetId)
                            .setConnectedElementName(undefined);

                    } else if (targetElement.hasClass(constants.AGGREGATION)
                        && (sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TRIGGER))) {
                        model = self.configurationData.getSiddhiAppConfig().getAggregation(targetId)
                        model.setConnectedSource(undefined);
                        if(sourceElement.hasClass(constants.STREAM)) {
                            model.resetModel(model);
                        }
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
                                || targetElement.hasClass(constants.WINDOW_QUERY)
                                || targetElement.hasClass(constants.FUNCTION_QUERY))) {
                            model = self.configurationData.getSiddhiAppConfig()
                                .getWindowFilterProjectionQuery(targetId);
                            model.resetInputModel(model);

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
                                } else if (firstConnectedElement &&
                                    (firstConnectedElement.name === disconnectedElementSourceName ||
                                        !firstConnectedElement.name)) {
                                    queryInput.setFirstConnectedElement(undefined);
                                } else if (secondConnectedElement
                                    && (secondConnectedElement.name === disconnectedElementSourceName ||
                                        !secondConnectedElement.name)) {
                                    queryInput.setSecondConnectedElement(undefined);
                                } else {
                                    console.log("Error: Disconnected source name not found in join query!");
                                    return;
                                }

                                // if left or sources are created then remove data from those sources
                                if (queryInput.getLeft() !== undefined
                                    && queryInput.getLeft().getConnectedSource() === disconnectedElementSourceName) {
                                    queryInput.setLeft(undefined);
                                } else if (queryInput.getRight() !== undefined
                                    && queryInput.getRight().getConnectedSource() === disconnectedElementSourceName) {
                                    queryInput.setRight(undefined);
                                }
                                model.resetInputModel(model);
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
                                model.resetInputModel(model, disconnectedElementName);
                            } else if (targetElement.hasClass(constants.SEQUENCE)) {
                                model = self.configurationData.getSiddhiAppConfig().getSequenceQuery(targetId);
                                model.resetInputModel(model, disconnectedElementName);
                            }
                        }

                    } else if (targetElement.hasClass(constants.STREAM) || targetElement.hasClass(constants.TABLE)
                        || targetElement.hasClass(constants.WINDOW)) {
                        if (sourceElement.hasClass(constants.PROJECTION) || sourceElement.hasClass(constants.FILTER)
                            || sourceElement.hasClass(constants.WINDOW_QUERY)
                            || sourceElement.hasClass(constants.FUNCTION_QUERY)
                            || sourceElement.hasClass(constants.JOIN)
                            || sourceElement.hasClass(constants.PATTERN)
                            || sourceElement.hasClass(constants.SEQUENCE)) {

                            if (sourceElement.hasClass(constants.PROJECTION) || sourceElement.hasClass(constants.FILTER)
                                || sourceElement.hasClass(constants.WINDOW_QUERY)
                                || sourceElement.hasClass(constants.FUNCTION_QUERY)) {
                                model = self.configurationData.getSiddhiAppConfig()
                                    .getWindowFilterProjectionQuery(sourceId);
                            } else if (sourceElement.hasClass(constants.JOIN)) {
                                model = self.configurationData.getSiddhiAppConfig().getJoinQuery(sourceId);
                            } else if (sourceElement.hasClass(constants.PATTERN)) {
                                model = self.configurationData.getSiddhiAppConfig().getPatternQuery(sourceId);
                            } else if (sourceElement.hasClass(constants.SEQUENCE)) {
                                model = self.configurationData.getSiddhiAppConfig().getSequenceQuery(sourceId);
                            }
                            model.resetOutputModel(model);
                        }
                    }

                    // validate source and target elements
                    checkJSONValidityOfElement(self, sourceId, true);
                    checkJSONValidityOfElement(self, targetId, true);
                });
            }

            function addMemberToPartitionGroup(self) {
                self.jsPlumbInstance.bind('group:addMember', function (event) {

                    // set the isDesignViewContentChanged to true
                    self.configurationData.setIsDesignViewContentChanged(true);

                    var partitionId = $(event.group).attr('id');
                    var partition = self.configurationData.getSiddhiAppConfig().getPartition(partitionId);

                    // check whether member is already added to the group
                    if (partition.isElementInsidePartition(event.el.id)) {
                        return;
                    }

                    var errorMessage = '';
                    var isGroupMemberValid = false;
                    if ($(event.el).hasClass(constants.FILTER) || $(event.el).hasClass(constants.PROJECTION)
                        || $(event.el).hasClass(constants.WINDOW_QUERY)
                        || $(event.el).hasClass(constants.FUNCTION_QUERY)
                        || $(event.el).hasClass(constants.JOIN)
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

                                var streamName = streamObjectCopy.getName();
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

                                    JSONValidator.prototype
                                        .validateInnerStream(streamObjectCopy, self.jsPlumbInstance, true);
                                } else {
                                    isGroupMemberValid = false;
                                    errorMessage = ' An inner stream with the same name is already added to the ' +
                                        'partition.';
                                }
                            } else if ($(event.el).hasClass(constants.PROJECTION)) {
                                var projectionQueryObject = self.configurationData.getSiddhiAppConfig()
                                    .getWindowFilterProjectionQuery(elementId);
                                var projectionQueryObjectCopy = _.cloneDeep(projectionQueryObject);
                                self.configurationData.getSiddhiAppConfig()
                                    .removeWindowFilterProjectionQuery(elementId);
                                partition.addWindowFilterProjectionQuery(projectionQueryObjectCopy);

                            } else if ($(event.el).hasClass(constants.FILTER)) {
                                var filterQueryObject = self.configurationData.getSiddhiAppConfig()
                                    .getWindowFilterProjectionQuery(elementId);
                                var filterQueryObjectCopy = _.cloneDeep(filterQueryObject);
                                self.configurationData.getSiddhiAppConfig()
                                    .removeWindowFilterProjectionQuery(elementId);
                                partition.addWindowFilterProjectionQuery(filterQueryObjectCopy);

                            } else if ($(event.el).hasClass(constants.WINDOW_QUERY)) {
                                var windowQueryObject = self.configurationData.getSiddhiAppConfig()
                                    .getWindowFilterProjectionQuery(elementId);
                                var windowQueryObjectCopy = _.cloneDeep(windowQueryObject);
                                self.configurationData.getSiddhiAppConfig()
                                    .removeWindowFilterProjectionQuery(elementId);
                                partition.addWindowFilterProjectionQuery(windowQueryObjectCopy);

                            } else if ($(event.el).hasClass(constants.FUNCTION_QUERY)) {
                                var functionQueryObject = self.configurationData.getSiddhiAppConfig()
                                    .getWindowFilterProjectionQuery(elementId);
                                var functionQueryObjectCopy = _.cloneDeep(functionQueryObject);
                                self.configurationData.getSiddhiAppConfig()
                                    .removeWindowFilterProjectionQuery(elementId);
                                partition.addWindowFilterProjectionQuery(functionQueryObjectCopy);

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
                        DesignViewUtils.prototype
                            .warnAlert('This element cannot be added to partition.' + errorMessage);
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

            function checkJSONValidityOfElement(self, elementId, doNotShowErrorMessages) {
                var element = self.configurationData.getSiddhiAppConfig()
                    .getDefinitionElementById(elementId, true, true, true);
                if (element !== undefined) {
                    var type = element.type;
                    var elementObject = element.element;
                    if (type === 'STREAM') {
                        // If this is an inner stream perform validation
                        var streamSavedInsideAPartition
                            = self.configurationData.getSiddhiAppConfig()
                            .getStreamSavedInsideAPartition(elementObject.getId());
                        // if streamSavedInsideAPartition is undefined then the stream is not inside a partition
                        if (streamSavedInsideAPartition !== undefined) {
                            JSONValidator.prototype.validateInnerStream(elementObject, self.jsPlumbInstance, true);
                        }
                    } else if (type === 'WINDOW_FILTER_PROJECTION_QUERY') {
                        JSONValidator.prototype.validateWindowFilterProjectionQuery(elementObject,
                            doNotShowErrorMessages);
                    } else if (type === 'PATTERN_QUERY') {
                        JSONValidator.prototype.validatePatternOrSequenceQuery(elementObject, 'Pattern Query',
                            doNotShowErrorMessages);
                    } else if (type === 'SEQUENCE_QUERY') {
                        JSONValidator.prototype.validatePatternOrSequenceQuery(elementObject, 'Sequence Query',
                            doNotShowErrorMessages);
                    } else if (type === 'JOIN_QUERY') {
                        JSONValidator.prototype.validateJoinQuery(elementObject, doNotShowErrorMessages);
                    } else if (type === 'SOURCE') {
                        JSONValidator.prototype.validateSourceOrSinkAnnotation(elementObject, 'Source',
                            doNotShowErrorMessages);
                    } else if (type === 'SINK') {
                        JSONValidator.prototype.validateSourceOrSinkAnnotation(elementObject, 'Sink',
                            doNotShowErrorMessages);
                    } else if (type === 'AGGREGATION') {
                        JSONValidator.prototype.validateAggregation(elementObject, doNotShowErrorMessages);
                    } else if (type === 'PARTITION') {
                        JSONValidator.prototype.validatePartition(elementObject, self.jsPlumbInstance,
                            doNotShowErrorMessages);
                    }
                }
            }

            checkConnectionValidityBeforeElementDrop();

            updateModelOnConnectionAttach();

            updateModelOnBeforeConnectionDetach();

            updateModelOnConnectionDetach();

            addMemberToPartitionGroup(self);

            self.drawGraphFromAppData();

            self.enableMultipleSelection();
        };

        DesignGrid.prototype.drawGraphFromAppData = function () {
            var self = this;

            //Send SiddhiApp Config to the backend and get tooltips
            var sendingString = JSON.stringify(self.configurationData.siddhiAppConfig);
            var response = this.getTooltips(sendingString);
            var tooltipList = [];
            if (response.status === "success") {
                tooltipList = response.tooltipList;
            } else {
                log.error(response.errorMessage);
            }

            // set isStillDrawingGraph to true since the graph drawing has begun
            self.configurationData.setIsStillDrawingGraph(true);

            _.forEach(self.configurationData.getSiddhiAppConfig().getSourceList(), function (source) {
                var sourceId = source.getId();
                var sourceName = source.getType();
                var array = sourceId.split("_");
                var lastArrayEntry = parseInt(array[array.length - 1]);
                var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                var sourceToolTip = '';
                var toolTipObject = _.find(tooltipList, function (toolTip) {
                    return toolTip.id === sourceId;
                });
                if (toolTipObject !== undefined) {
                    sourceToolTip = toolTipObject.text;
                }
                self.handleSourceAnnotation(mouseTop, mouseLeft, true, sourceName, sourceId, sourceToolTip);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getSinkList(), function (sink) {
                var sinkId = sink.getId();
                var sinkName = sink.getType();
                var array = sinkId.split("_");
                var lastArrayEntry = parseInt(array[array.length - 1]);
                var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                var sinkToolTip = '';
                var toolTipObject = _.find(tooltipList, function (toolTip) {
                    return toolTip.id === sinkId;
                });
                if (toolTipObject !== undefined) {
                    sinkToolTip = toolTipObject.text;
                }
                self.handleSinkAnnotation(mouseTop, mouseLeft, true, sinkName, sinkId, sinkToolTip);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getStreamList(), function (stream) {
                var streamId = stream.getId();
                var streamName = stream.getName();
                var array = streamId.split("_");
                var lastArrayEntry = parseInt(array[array.length - 1]);
                var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                var streamToolTip = '';
                var toolTipObject = _.find(tooltipList, function (toolTip) {
                    return toolTip.id === streamId;
                });
                if (toolTipObject !== undefined) {
                    streamToolTip = toolTipObject.text;
                }
                self.handleStream(mouseTop, mouseLeft, true, streamId, streamName, streamToolTip, stream);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getTableList(), function (table) {

                var tableId = table.getId();
                var tableName = table.getName();
                var array = tableId.split("_");
                var lastArrayEntry = parseInt(array[array.length - 1]);
                var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                var tableToolTip = '';
                var toolTipObject = _.find(tooltipList, function (toolTip) {
                    return toolTip.id === tableId;
                });
                if (toolTipObject !== undefined) {
                    tableToolTip = toolTipObject.text;
                }
                self.handleTable(mouseTop, mouseLeft, true, tableId, tableName, tableToolTip);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getWindowList(), function (window) {

                var windowId = window.getId();
                var windowName = window.getName();
                var array = windowId.split("_");
                var lastArrayEntry = parseInt(array[array.length - 1]);
                var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                var windowToolTip = '';
                var toolTipObject = _.find(tooltipList, function (toolTip) {
                    return toolTip.id === windowId;
                });
                if (toolTipObject !== undefined) {
                    windowToolTip = toolTipObject.text;
                }
                self.handleWindow(mouseTop, mouseLeft, true, windowId, windowName, windowToolTip);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getTriggerList(), function (trigger) {

                var triggerId = trigger.getId();
                var triggerName = trigger.getName();
                var array = triggerId.split("_");
                var lastArrayEntry = parseInt(array[array.length - 1]);
                var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                var triggerToolTip = '';
                var toolTipObject = _.find(tooltipList, function (toolTip) {
                    return toolTip.id === triggerId;
                });
                if (toolTipObject !== undefined) {
                    triggerToolTip = toolTipObject.text;
                }
                self.handleTrigger(mouseTop, mouseLeft, true, triggerId, triggerName, triggerToolTip);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getAggregationList(), function (aggregation) {

                var aggregationId = aggregation.getId();
                var aggregationName = aggregation.getName();
                var array = aggregationId.split("_");
                var lastArrayEntry = parseInt(array[array.length - 1]);
                var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                var aggregationToolTip = '';
                var toolTipObject = _.find(tooltipList, function (toolTip) {
                    return toolTip.id === aggregationId;
                });
                if (toolTipObject !== undefined) {
                    aggregationToolTip = toolTipObject.text;
                }
                self.handleAggregation(mouseTop, mouseLeft, true, aggregationId, aggregationName, aggregationToolTip);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getFunctionList(), function (functionObject) {

                var functionId = functionObject.getId();
                var functionName = functionObject.getName();
                var array = functionId.split("_");
                var lastArrayEntry = parseInt(array[array.length - 1]);
                var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                var functionToolTip = '';
                var toolTipObject = _.find(tooltipList, function (toolTip) {
                    return toolTip.id === functionId;
                });
                if (toolTipObject !== undefined) {
                    functionToolTip = toolTipObject.text;
                }
                self.handleFunction(mouseTop, mouseLeft, true, functionId, functionName, functionToolTip);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getPatternQueryList(), function (patternQuery) {

                var patternQueryId = patternQuery.getId();
                var patternQueryName = patternQuery.getQueryName();
                var array = patternQueryId.split("_");
                var lastArrayEntry = parseInt(array[array.length - 1]);
                var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                var patternQueryToolTip = '';
                var toolTipObject = _.find(tooltipList, function (toolTip) {
                    return toolTip.id === patternQueryId;
                });
                if (toolTipObject !== undefined) {
                    patternQueryToolTip = toolTipObject.text;
                }
                self.handlePatternQuery(mouseTop, mouseLeft, true, patternQueryName, patternQueryId,
                    patternQueryToolTip);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getSequenceQueryList(), function (sequenceQuery) {

                var sequenceQueryId = sequenceQuery.getId();
                var sequenceQueryName = sequenceQuery.getQueryName();
                var array = sequenceQueryId.split("_");
                var lastArrayEntry = parseInt(array[array.length - 1]);
                var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                var sequenceQueryToolTip = '';
                var toolTipObject = _.find(tooltipList, function (toolTip) {
                    return toolTip.id === sequenceQueryId;
                });
                if (toolTipObject !== undefined) {
                    sequenceQueryToolTip = toolTipObject.text;
                }
                self.handleSequenceQuery(mouseTop, mouseLeft, true, sequenceQueryName, sequenceQueryId,
                    sequenceQueryToolTip);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getWindowFilterProjectionQueryList(),
                function (windowFilterProjectionQuery) {
                    var queryId = windowFilterProjectionQuery.getId();
                    var queryName = windowFilterProjectionQuery.getQueryName();
                    var querySubType = windowFilterProjectionQuery.getQueryInput().getType();

                    var queryType;
                    if (querySubType === 'PROJECTION') {
                        queryType = constants.PROJECTION;
                    } else if (querySubType === 'FILTER') {
                        queryType = constants.FILTER;
                    } else if (querySubType === 'WINDOW') {
                        queryType = constants.WINDOW_QUERY;
                    } else if (querySubType === 'FUNCTION') {
                        queryType = constants.FUNCTION_QUERY;
                    }

                    var array = queryId.split("_");
                    var lastArrayEntry = parseInt(array[array.length - 1]);
                    var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                    var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                    var queryToolTip = '';
                    var toolTipObject = _.find(tooltipList, function (toolTip) {
                        return toolTip.id === queryId;
                    });
                    if (toolTipObject !== undefined) {
                        queryToolTip = toolTipObject.text;
                    }
                    self.handleWindowFilterProjectionQuery(queryType, mouseTop, mouseLeft, true, queryName, queryId,
                        queryToolTip);
                });

            _.forEach(self.configurationData.getSiddhiAppConfig().getJoinQueryList(), function (joinQuery) {

                var joinQueryId = joinQuery.getId();
                var joinQueryName = joinQuery.getQueryName();
                var array = joinQueryId.split("_");
                var lastArrayEntry = parseInt(array[array.length - 1]);
                var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                var joinQueryToolTip = '';
                var toolTipObject = _.find(tooltipList, function (toolTip) {
                    return toolTip.id === joinQueryId;
                });
                if (toolTipObject !== undefined) {
                    joinQueryToolTip = toolTipObject.text;
                }
                self.handleJoinQuery(mouseTop, mouseLeft, true, joinQueryName, joinQueryId, joinQueryToolTip);
            });

            _.forEach(self.configurationData.getSiddhiAppConfig().getPartitionList(), function (partition) {

                var partitionId = partition.getId();
                var array = partitionId.split("_");
                var lastArrayEntry = parseInt(array[array.length - 1]);
                var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                var partitionToolTip = '';
                var toolTipObject = _.find(tooltipList, function (toolTip) {
                    return toolTip.id === partitionId;
                });
                if (toolTipObject !== undefined) {
                    partitionToolTip = toolTipObject.text;
                }
                self.handlePartition(mouseTop, mouseLeft, true, partitionId, partitionToolTip);

                var jsPlumbPartitionGroup = self.jsPlumbInstance.getGroup(partitionId);

                _.forEach(partition.getStreamList(), function (stream) {
                    var streamId = stream.getId();
                    var streamName = stream.getName();
                    var array = streamId.split("_");
                    var lastArrayEntry = parseInt(array[array.length - 1]);
                    var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                    var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                    var streamToolTip = '';
                    var toolTipObject = _.find(tooltipList, function (toolTip) {
                        return toolTip.id === streamId;
                    });
                    if (toolTipObject !== undefined) {
                        streamToolTip = toolTipObject.text;
                    }
                    self.handleStream(mouseTop, mouseLeft, true, streamId, streamName, streamToolTip, stream);

                    var streamElement = $('#' + streamId)[0];
                    self.jsPlumbInstance.addToGroup(jsPlumbPartitionGroup, streamElement);
                });

                _.forEach(partition.getPatternQueryList(), function (patternQuery) {

                    var patternQueryId = patternQuery.getId();
                    var patternQueryName = patternQuery.getQueryName();
                    var array = patternQueryId.split("_");
                    var lastArrayEntry = parseInt(array[array.length - 1]);
                    var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                    var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                    var patternQueryToolTip = '';
                    var toolTipObject = _.find(tooltipList, function (toolTip) {
                        return toolTip.id === patternQueryId;
                    });
                    if (toolTipObject !== undefined) {
                        patternQueryToolTip = toolTipObject.text;
                    }
                    self.handlePatternQuery(mouseTop, mouseLeft, true, patternQueryName, patternQueryId,
                        patternQueryToolTip);

                    var patternElement = $('#' + patternQueryId)[0];
                    self.jsPlumbInstance.addToGroup(jsPlumbPartitionGroup, patternElement);
                });

                _.forEach(partition.getSequenceQueryList(), function (sequenceQuery) {

                    var sequenceQueryId = sequenceQuery.getId();
                    var sequenceQueryName = sequenceQuery.getQueryName();
                    var array = sequenceQueryId.split("_");
                    var lastArrayEntry = parseInt(array[array.length - 1]);
                    var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                    var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                    var sequenceQueryToolTip = '';
                    var toolTipObject = _.find(tooltipList, function (toolTip) {
                        return toolTip.id === sequenceQueryId;
                    });
                    if (toolTipObject !== undefined) {
                        sequenceQueryToolTip = toolTipObject.text;
                    }
                    self.handleSequenceQuery(mouseTop, mouseLeft, true, sequenceQueryName, sequenceQueryId,
                        sequenceQueryToolTip);

                    var sequenceElement = $('#' + sequenceQueryId)[0];
                    self.jsPlumbInstance.addToGroup(jsPlumbPartitionGroup, sequenceElement);
                });

                _.forEach(partition.getWindowFilterProjectionQueryList(),
                    function (windowFilterProjectionQuery) {
                        var queryId = windowFilterProjectionQuery.getId();
                        var queryName = windowFilterProjectionQuery.getQueryName();
                        var querySubType = windowFilterProjectionQuery.getQueryInput().getType();

                        var queryType;
                        if (querySubType === 'PROJECTION') {
                            queryType = constants.PROJECTION;
                        } else if (querySubType === 'FILTER') {
                            queryType = constants.FILTER;
                        } else if (querySubType === 'WINDOW') {
                            queryType = constants.WINDOW_QUERY;
                        } else if (querySubType === 'FUNCTION') {
                            queryType = constants.FUNCTION_QUERY;
                        }

                        var array = queryId.split("_");
                        var lastArrayEntry = parseInt(array[array.length - 1]);
                        var mouseTop
                            = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                        var mouseLeft
                            = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                        var queryToolTip = '';
                        var toolTipObject = _.find(tooltipList, function (toolTip) {
                            return toolTip.id === queryId;
                        });
                        if (toolTipObject !== undefined) {
                            queryToolTip = toolTipObject.text;
                        }
                        self.handleWindowFilterProjectionQuery(
                            queryType, mouseTop, mouseLeft, true, queryName, queryId, queryToolTip);

                        var queryElement = $('#' + queryId)[0];
                        self.jsPlumbInstance.addToGroup(jsPlumbPartitionGroup, queryElement);
                    });

                _.forEach(partition.getJoinQueryList(), function (joinQuery) {

                    var joinQueryId = joinQuery.getId();
                    var joinQueryName = joinQuery.getQueryName();
                    var array = joinQueryId.split("_");
                    var lastArrayEntry = parseInt(array[array.length - 1]);
                    var mouseTop = lastArrayEntry * 100 - self.canvas.offset().top + self.canvas.scrollTop() - 40;
                    var mouseLeft = lastArrayEntry * 200 - self.canvas.offset().left + self.canvas.scrollLeft() - 60;
                    var joinQueryToolTip = '';
                    var toolTipObject = _.find(tooltipList, function (toolTip) {
                        return toolTip.id === joinQueryId;
                    });
                    if (toolTipObject !== undefined) {
                        joinQueryToolTip = toolTipObject.text;
                    }
                    self.handleJoinQuery(mouseTop, mouseLeft, true, joinQueryName, joinQueryId, joinQueryToolTip);

                    var joinElement = $('#' + joinQueryId)[0];
                    self.jsPlumbInstance.addToGroup(jsPlumbPartitionGroup, joinElement);
                });
            });

            _.forEach(self.configurationData.edgeList, function (edge) {
                var targetId;
                var sourceId;
                var paintStyle = {
                    strokeWidth: 2,
                    stroke: '#424242',
                    outlineStroke: "transparent",
                    outlineWidth: "3"
                };

                if (edge.getChildType() === 'PARTITION') {
                    targetId = edge.getChildId();
                    sourceId = edge.getParentId() + '-out';
                } else if (edge.getParentType() === 'PARTITION') {
                    targetId = edge.getChildId() + '-in';
                    sourceId = edge.getParentId();
                } else if (edge.getParentType() === 'SINK' && edge.getChildType() === 'SOURCE') {
                    targetId = edge.getChildId() + '-in';
                    sourceId = edge.getParentId() + '-out';
                    paintStyle = {
                        strokeWidth: 2, stroke: "#424242", dashstyle: "2 3", outlineStroke: "transparent",
                        outlineWidth: "3"
                    }
                } else {
                    // check if the edge is originating from a fault stream. if so get the corresponding event stream
                    // and draw the edge from the -err-out connector.
                    if (edge.isFromFaultStream()) {
                        sourceId = edge.getParentId() + '-err-out';
                        paintStyle = {
                            strokeWidth: 2, stroke: "#FF0000", dashstyle: "2 3", outlineStroke: "transparent",
                            outlineWidth: "3"
                        };
                    } else {
                        sourceId = edge.getParentId() + '-out';
                    }
                    targetId = edge.getChildId() + '-in';
                }

                self.jsPlumbInstance.connect({
                    source: sourceId,
                    target: targetId,
                    paintStyle: paintStyle
                });
            });

            // re-align the elements
            self.autoAlignElements();

            /*
            * In here we set a timeout because when drawing the graph jsplumb triggers a 'addMember' event (when adding
            * an element to the partition) an it takes some time to execute. So that we add a timeout and set the
            * isDesignViewContentChange to false.
            * */
            setTimeout(function () {
                // set the isDesignViewContentChanged to false
                self.configurationData.setIsDesignViewContentChanged(false);

                // set isStillDrawingGraph to false since the graph drawing is done
                self.configurationData.setIsStillDrawingGraph(false);
            }, 100);
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
            Array.prototype.push.apply(nodes, currentTabElement.getElementsByClassName(constants.FUNCTION_QUERY));
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
                        className.includes(constants.FUNCTION_QUERY) || className.includes(constants.JOIN) ||
                        className.includes(constants.PATTERN) || className.includes(constants.SEQUENCE)) {
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
                                                                sourceId, sourceToolTip) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if (sourceId !== undefined) {
                    elementId = sourceId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("sourceId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr({
                'id': elementId,
                'tabindex': TAB_INDEX
            }).addClass(constants.SOURCE);
            if (isCodeToDesignMode) {
                newAgent.attr('title', sourceToolTip);
            }
            self.canvas.append(newAgent);
            // Drop the source element. Inside this a it generates the source definition form.
            self.dropElements.dropSource(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, sourceName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
            self.enableMultipleSelection();
        };

        DesignGrid.prototype.handleSinkAnnotation = function (mouseTop, mouseLeft, isCodeToDesignMode, sinkName,
                                                              sinkId, sinkToolTip) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if (sinkId !== undefined) {
                    elementId = sinkId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("sinkId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr({
                'id': elementId,
                'tabindex': TAB_INDEX
            }).addClass(constants.SINK);
            if (isCodeToDesignMode) {
                newAgent.attr('title', sinkToolTip);
            }
            self.canvas.append(newAgent);
            // Drop the sink element. Inside this a it generates the sink definition form.
            self.dropElements.dropSink(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, sinkName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
            self.enableMultipleSelection();
        };

        DesignGrid.prototype.handleStream = function (mouseTop, mouseLeft, isCodeToDesignMode, streamId, streamName,
                                                      streamToolTip, stream) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = stream && stream.getId() ? stream.getId() : self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if (streamId !== undefined) {
                    elementId = streamId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("streamId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }

            var newAgent = $('<div>').attr({
                'id': elementId,
                'tabindex': TAB_INDEX
            }).addClass(constants.STREAM);

            var inFaultStreamCreationPath = false;

            // If this is a fault stream, hide it
            if (stream && stream.isFaultStream()) {
                newAgent.hide();
                inFaultStreamCreationPath = true;
            }

            if (isCodeToDesignMode) {
                newAgent.attr('title', streamToolTip);
            }
            self.canvas.append(newAgent);
            // Drop the stream element. Inside this a it generates the stream definition form.
            self.dropElements.dropStream(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode,
                false, streamName, stream && stream.hasFaultStream(), inFaultStreamCreationPath);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
            self.enableMultipleSelection();
        };

        DesignGrid.prototype.handleTable = function (mouseTop, mouseLeft, isCodeToDesignMode, tableId, tableName,
                                                     tableToolTip) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if (tableId !== undefined) {
                    elementId = tableId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("tableId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr({
                'id': elementId,
                'tabindex': TAB_INDEX
            }).addClass(constants.TABLE);
            if (isCodeToDesignMode) {
                newAgent.attr('title', tableToolTip);
            }
            self.canvas.append(newAgent);
            // Drop the Table element. Inside this a it generates the table definition form.
            self.dropElements.dropTable(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, tableName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
            self.enableMultipleSelection();
        };

        DesignGrid.prototype.handleWindow = function (mouseTop, mouseLeft, isCodeToDesignMode, windowId, windowName,
                                                      windowToolTip) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if (windowId !== undefined) {
                    elementId = windowId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("windowId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr({
                'id': elementId,
                'tabindex': TAB_INDEX
            }).addClass(constants.WINDOW);
            if (isCodeToDesignMode) {
                newAgent.attr('title', windowToolTip);
            }
            self.canvas.append(newAgent);
            // Drop the Table element. Inside this a it generates the table definition form.
            self.dropElements.dropWindow(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, windowName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
            self.enableMultipleSelection();
        };

        DesignGrid.prototype.handleTrigger = function (mouseTop, mouseLeft, isCodeToDesignMode, triggerId,
                                                       triggerName, triggerToolTip) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if (triggerId !== undefined) {
                    elementId = triggerId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("triggerId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr({
                'id': elementId,
                'tabindex': TAB_INDEX
            }).addClass(constants.TRIGGER);
            if (isCodeToDesignMode) {
                newAgent.attr('title', triggerToolTip);
            }
            self.canvas.append(newAgent);
            // Drop the Trigger element. Inside this a it generates the trigger definition form.
            self.dropElements.dropTrigger(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, triggerName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
            self.enableMultipleSelection();
        };

        DesignGrid.prototype.handleAggregation = function (mouseTop, mouseLeft, isCodeToDesignMode, aggregationId,
                                                           aggregationName, aggregationToolTip) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if (aggregationId !== undefined) {
                    elementId = aggregationId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("aggregationId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr({
                'id': elementId,
                'tabindex': TAB_INDEX
            }).addClass(constants.AGGREGATION);
            if (isCodeToDesignMode) {
                newAgent.attr('title', aggregationToolTip);
            }
            self.canvas.append(newAgent);
            // Drop the Aggregation element. Inside this a it generates the aggregation definition form.
            self.dropElements.dropAggregation(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode,
                aggregationName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
            self.enableMultipleSelection();
        };

        DesignGrid.prototype.handleFunction = function (mouseTop, mouseLeft, isCodeToDesignMode, functionId,
                                                        functionName, functionToolTip) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if (functionId !== undefined) {
                    elementId = functionId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("functionId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr({
                'id': elementId,
                'tabindex': TAB_INDEX
            }).addClass(constants.FUNCTION);
            if (isCodeToDesignMode) {
                newAgent.attr('title', functionToolTip);
            }
            self.canvas.append(newAgent);
            // Drop the Function element. Inside this a it generates the function definition form.
            self.dropElements.dropFunction(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, functionName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
            self.enableMultipleSelection();
        };


        DesignGrid.prototype.handleWindowFilterProjectionQuery = function (type, mouseTop, mouseLeft,
                                                                           isCodeToDesignMode, queryName, queryId,
                                                                           queryToolTip) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if (queryId !== undefined) {
                    elementId = queryId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("queryId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr({
                'id': elementId,
                'tabindex': TAB_INDEX
            }).addClass(type);
            if (isCodeToDesignMode) {
                newAgent.attr('title', queryToolTip);
            }
            self.canvas.append(newAgent);
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropWindowFilterProjectionQuery(newAgent, elementId, type, mouseTop, mouseLeft, queryName,
                isCodeToDesignMode);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
            self.enableMultipleSelection();
        };

        DesignGrid.prototype.handleJoinQuery = function (mouseTop, mouseLeft, isCodeToDesignMode, joinQueryName,
                                                         joinQueryId, joinQueryToolTip) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if (joinQueryId !== undefined) {
                    elementId = joinQueryId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("queryId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr({
                'id': elementId,
                'tabindex': TAB_INDEX
            }).addClass(constants.JOIN);
            if (isCodeToDesignMode) {
                newAgent.attr('title', joinQueryToolTip);
            }
            self.canvas.append(newAgent);
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropJoinQuery(newAgent, elementId, mouseTop, mouseLeft, joinQueryName,
                isCodeToDesignMode);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
            self.enableMultipleSelection();
        };

        DesignGrid.prototype.handlePatternQuery = function (mouseTop, mouseLeft, isCodeToDesignMode, patternQueryName,
                                                            patternQueryId, patternQueryToolTip) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if (patternQueryId !== undefined) {
                    elementId = patternQueryId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("patternQueryId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr({
                'id': elementId,
                'tabindex': TAB_INDEX
            }).addClass(constants.PATTERN);
            if (isCodeToDesignMode) {
                newAgent.attr('title', patternQueryToolTip);
            }
            self.canvas.append(newAgent);
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropPatternQuery(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode,
                patternQueryName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
            self.enableMultipleSelection();
        };

        DesignGrid.prototype.handleSequenceQuery = function (mouseTop, mouseLeft, isCodeToDesignMode, sequenceQueryName,
                                                             sequenceQueryId, sequenceQueryToolTip) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if (sequenceQueryId !== undefined) {
                    elementId = sequenceQueryId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("sequenceQueryId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr({
                'id': elementId,
                'tabindex': TAB_INDEX
            }).addClass(constants.SEQUENCE);
            if (isCodeToDesignMode) {
                newAgent.attr('title', sequenceQueryToolTip);
            }
            self.canvas.append(newAgent);
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropSequenceQuery(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode,
                sequenceQueryName);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
            self.enableMultipleSelection();
        };

        DesignGrid.prototype.handlePartition = function (mouseTop, mouseLeft, isCodeToDesignMode, partitionId,
                                                         partitionToolTip) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.getNewAgentId();
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if (partitionId !== undefined) {
                    elementId = partitionId;
                    self.generateNextNewAgentId();
                } else {
                    console.log("partitionId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            var newAgent = $('<div>').attr({
                'id': elementId,
                'tabindex': TAB_INDEX
            }).addClass(constants.PARTITION);
            if (isCodeToDesignMode) {
                newAgent.attr('title', partitionToolTip);
            }
            self.canvas.append(newAgent);
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropPartition(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode);
            self.configurationData.getSiddhiAppConfig()
                .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
            self.enableMultipleSelection();
        };

        DesignGrid.prototype.generateNextNewAgentId = function () {
            var newId = parseInt(this.newAgentId) + 1;
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
            _.forEach(partitionConnections, function (connection) {
                partitionIds.push(parseInt((connection.id).slice(-1)));
            });

            var maxId = partitionIds.reduce(function (a, b) {
                return Math.max(a, b);
            });
            return partitionId + '_pc' + (maxId + 1);
        };

        DesignGrid.prototype.enableMultipleSelection = function () {
            var self = this;
            var selector = $('<div>').attr('id', constants.MULTI_SELECTOR + self.currentTabId).addClass(constants.SELECTOR);
            self.canvas.append(selector);
            new DragSelect({
                selectables: document.querySelectorAll('.jtk-draggable'),
                selector: document.getElementById(constants.MULTI_SELECTOR + self.currentTabId),
                area: document.getElementById('design-grid-container-' + self.currentTabId),
                multiSelectKeys: ['ctrlKey', 'shiftKey'],
                onElementSelect: function (element) {
                    if (!self.selectedObjects.includes(element, 0)) {
                        self.jsPlumbInstance.addToDragSelection(element);
                        self.addSelectedElements(element);
                        $(element).focus();
                        $(element).addClass('selected-container');
                        self.selectedObjects.push(element);
                    }
                },
                onElementUnselect: function (element) {
                    self.jsPlumbInstance.removeFromDragSelection(element);
                    self.removeFromSelectedElements(element);
                    for (var i = 0; i < self.selectedObjects.length; i++) {
                        if (self.selectedObjects[i] == element) {
                            self.selectedObjects.splice(i, 1);
                        }
                    }
                    $(element).removeClass('selected-container focused-container');
                    self.selectedObjects = [];
                }
            });
        };

        /**
         * Generate tooltips for all the elements
         *
         * @param designViewJSON siddhiAppConfig
         */
        DesignGrid.prototype.getTooltips = function (designViewJSON) {
            var self = this;
            var result = {};
            self.tooltipsURL = window.location.protocol + "//" + window.location.host + "/editor/tooltips";
            $.ajax({
                type: "POST",
                url: self.tooltipsURL,
                data: self.options.application.utils.base64EncodeUnicode(designViewJSON),
                async: false,
                success: function (response) {
                    result = {status: "success", tooltipList: response};
                },
                error: function (error) {
                    if (error.responseText) {
                        result = {status: "fail", errorMessage: error.responseText};
                    } else {
                        result = {status: "fail", errorMessage: "Error Occurred while processing your request"};
                    }
                }
            });
            return result;
        };

        return DesignGrid;
    });
