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
define(['require', 'log', 'jquery', 'jsplumb','backbone', 'lodash', 'dropElements', 'dagre', 'edge', 'patternQueryInput'
        , 'queryOutput'],

    function (require, log, $, _jsPlumb ,Backbone, _, DropElements, dagre, Edge, PatternQueryInput, QueryOutput) {

        var constants = {
            STREAM : 'streamdrop',
            TABLE : 'tabledrop',
            WINDOW :'windowdrop',
            TRIGGER :'triggerdrop',
            AGGREGATION : 'aggregationdrop',
            PASS_THROUGH : 'squerydrop',
            FILTER : 'filterdrop',
            JOIN : 'joquerydrop',
            WINDOW_QUERY : 'wquerydrop',
            PATTERN : 'patternQueryDrop',
            PARTITION :'partitiondrop'
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
            this.appData = this.options.appData;
            this.container = this.options.container;
            this.application = this.options.application;
        };

        DesignGrid.prototype.render = function () {
            var self = this;

            // newAgentId --> newAgent ID (Dropped Element ID)
            this.newAgentId = "1";
            var dropElementsOpts = {};
            _.set(dropElementsOpts, 'container', self.container);
            _.set(dropElementsOpts, 'appData', self.appData);
            _.set(dropElementsOpts, 'application', self.application);
            _.set(dropElementsOpts, 'designGrid', self);
            this.dropElements = new DropElements(dropElementsOpts);
            this.canvas = $(self.container);
            //TODO: once an element is dropped source view/ design view buttons should be disabled

            /**
             * @description jsPlumb function opened
             */
            _jsPlumb.ready(function() {

                _jsPlumb.importDefaults({
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
                _jsPlumb.setContainer($(self.container));

                /**
                 * @function droppable method for the 'stream' & the 'query' objects
                 */
                self.canvas.droppable
                ({
                    accept: '.stream, .table, .window, .trigger, .aggregation, .pass-through, .filter-query, ' +
                    '.join-query, .window-query, .pattern-query, .partition',
                    containment: 'grid-container',

                    /**
                     *
                     * @param e --> original event object fired/ normalized by jQuery
                     * @param ui --> object that contains additional info added by jQuery depending on which interaction was used
                     * @helper clone
                     */

                    drop: function (e, ui) {
                        var mouseTop = e.pageY - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                        var mouseLeft = e.pageX - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                        // Clone the element in the toolbox in order to drop the clone on the canvas
                        var droppedElement = ui.helper.clone();
                        // To further manipulate the jsplumb element, remove the jquery UI clone helper as jsPlumb doesn't support it
                        ui.helper.remove();
                        $(droppedElement).draggable({containment: "parent"});
                        // Repaint to reposition all the elements that are on the canvas after the drop/addition of a new element on the canvas
                        _jsPlumb.repaint(ui.helper);//TODO: check this

                        // If the dropped Element is a Stream then->
                        if ($(droppedElement).hasClass('stream')) {
                            self.handleStream(mouseTop, mouseLeft, false);
                        }

                        // If the dropped Element is a Table then->
                        if ($(droppedElement).hasClass('table')) {
                            self.handleTable(mouseTop, mouseLeft, false);
                        }

                        // If the dropped Element is a Window(not window query) then->
                        else if ($(droppedElement).hasClass('window')) {
                            self.handleWindow(mouseTop, mouseLeft, false);
                        }

                        // If the dropped Element is a Trigger then->
                        else if ($(droppedElement).hasClass('trigger')) {
                            self.handleTrigger(mouseTop, mouseLeft, false);
                        }

                        // If the dropped Element is a Aggregation then->
                        else if ($(droppedElement).hasClass('aggregation')) {
                            self.handleAggregation(mouseTop, mouseLeft, false);
                        }

                        // If the dropped Element is a Pass through Query then->
                        else if ($(droppedElement).hasClass('pass-through')) {
                            self.handlePassThroughQuery(mouseTop, mouseLeft, false);
                        }

                        // If the dropped Element is a Filter query then->
                        else if ($(droppedElement).hasClass('filter-query')) {
                            self.handleFilterQuery(mouseTop, mouseLeft, false);
                        }

                        // If the dropped Element is a Window Query then->
                        else if ($(droppedElement).hasClass('window-query')) {
                            self.handleWindowQuery(mouseTop, mouseLeft, false);
                        }

                        // If the dropped Element is a Join Query then->
                        else if ($(droppedElement).hasClass('join-query')) {
                            self.handleJoinQuery(mouseTop, mouseLeft, false);
                        }

                        // If the dropped Element is a Pattern Query then->
                        else if($(droppedElement).hasClass('pattern-query')) {
                            self.handlePatternQuery(mouseTop, mouseLeft, false, undefined, "Pattern Query");
                        }

                        // If the dropped Element is a Partition then->
                        else if($(droppedElement).hasClass('partition')) {
                            self.handlePartition(mouseTop, mouseLeft, false);
                        }
                    }
                });

                // auto align the diagram when the button is clicked
                $('#auto-align').click(function(){ //TODO: add auto align function
                    self.autoAlignElements();
                });
            });

            // check the validity of the connections and drop if invalid
            function checkConnectionValidityBeforeElementDrop() { //TODO: streams can be connected to streams?
                _jsPlumb.bind('beforeDrop', function (connection) {
                    var connectionValidity = true;
                    var target = connection.targetId;
                    var targetId = target.substr(0, target.indexOf('-'));
                    var targetElement = $('#' + targetId);

                    var source = connection.sourceId;
                    var sourceId = source.substr(0, source.indexOf('-'));
                    var sourceElement = $('#' + sourceId);

                    // avoid the expose of inner-streams outside the group
                    if (sourceElement.hasClass(constants.STREAM) && _jsPlumb.getGroupFor(sourceId) !== undefined) {
                        if (_jsPlumb.getGroupFor(sourceId) !== _jsPlumb.getGroupFor(targetId)) {
                            connectionValidity = false;
                            alert("Invalid Connection: Inner Streams are not exposed to outside");
                        }
                    }
                    else if (targetElement.hasClass(constants.STREAM) && _jsPlumb.getGroupFor(targetId) !== undefined) {
                        if (_jsPlumb.getGroupFor(targetId) !== _jsPlumb.getGroupFor(sourceId)) {
                            connectionValidity = false;
                            alert("Invalid Connection: Inner Streams are not exposed to outside");
                        }
                    }
                    if (sourceElement.hasClass(constants.PARTITION)) {
                        if ($(_jsPlumb.getGroupFor(targetId)).attr('id') !== sourceId) {
                            connectionValidity = false;
                            alert("Invalid Connection: Connect to a partition query");
                        }
                    }
                    else if (targetElement.hasClass(constants.PATTERN)) {
                        if(!(sourceElement.hasClass(constants.STREAM))) {
                            connectionValidity = false;
                            alert("Invalid Connection");
                        }
                    }
                    else if (targetElement.hasClass(constants.PASS_THROUGH) || targetElement.hasClass(constants.FILTER)
                        || targetElement.hasClass(constants.WINDOW_QUERY) || targetElement.hasClass(constants.JOIN)) {
                        if (!(sourceElement.hasClass(constants.STREAM) || sourceElement.hasClass(constants.TABLE))) {
                            connectionValidity = false;
                            alert("Invalid Connection");
                        }
                    }
                    else if (sourceElement.hasClass(constants.PASS_THROUGH) || sourceElement.hasClass(constants.FILTER)
                        || sourceElement.hasClass(constants.WINDOW_QUERY)
                        || sourceElement.hasClass(constants.PATTERN) || sourceElement.hasClass(constants.JOIN)) {
                        if (!(targetElement.hasClass(constants.STREAM))) { //TODO: can a output of a query connected to a table
                            connectionValidity = false;
                            alert("Invalid Connection");
                        }
                    }
                    return connectionValidity;
                });
            }

            // Update the model when a connection is established and bind events for the connection
            function updateModelOnConnectionAttach() {
                //TODO: whenever a annotation is changed, delete the entire annotation and save on it
                _jsPlumb.bind('connection', function (connection) {
                    var target = connection.targetId;
                    var targetId = target.substr(0, target.indexOf('-'));
                    var targetElement = $('#' + targetId);

                    var source = connection.sourceId;
                    var sourceId = source.substr(0, source.indexOf('-'));
                    var sourceElement = $('#' + sourceId);

                    // create and add an ege to the edgeList

                    var edgeId = ''+ targetId + '_' + sourceId + '';
                    var edgeInTheEdgeList = self.appData.getEdge(edgeId);
                    if(edgeInTheEdgeList !== undefined) {
                        //TODO update the model. why?: query type can be changed even the connection  is same
                        //edgeInTheEdgeList.setParentType('');
                        //edgeInTheEdgeList.setChildType('');
                    } else {
                        var edgeOptions = {};
                        _.set(edgeOptions, 'id', edgeId);
                        _.set(edgeOptions, 'parentId', targetId);
                        _.set(edgeOptions, 'parentType', 'query');//TODO: correct this
                        _.set(edgeOptions, 'childId', sourceId);
                        _.set(edgeOptions, 'childType', 'stream');
                        var edge = new Edge(edgeOptions);
                        self.appData.addEdge(edge);
                    }

                    var model;

                    if (sourceElement.hasClass(constants.STREAM)) {
                        if (targetElement.hasClass(constants.PASS_THROUGH) || targetElement.hasClass(constants.FILTER)
                            || targetElement.hasClass(constants.WINDOW_QUERY)) {
                            model = self.appData.getQuery(targetId);
                            model.setFrom(sourceId);
                        }
                        else if (targetElement.hasClass(constants.PATTERN)) {
                            model = self.appData.getPatternQuery(targetId);
                            var connectedStreamName = self.appData.getStream(sourceId).getName();
                            if (model.getQueryInput() === '') {
                                var patternQueryInputObject = new PatternQueryInput();
                                patternQueryInputObject.addConnectedElementName(connectedStreamName);
                                model.setQueryInput(patternQueryInputObject);
                            } else {
                                model.getQueryInput().addConnectedElementName(connectedStreamName);
                            }
                        }
                        else if (targetElement.hasClass(constants.JOIN)) {
                            model = self.appData.getJoinQuery(targetId);
                            var streams = model.getFrom();
                            if (streams === undefined || streams === "") {
                                streams = [sourceId];
                            } else {
                                streams.push(sourceId);
                            }
                            model.setFrom(streams);
                        }
                        else if (targetElement.hasClass(constants.PARTITION)) {
                            model = self.appData.getPartition(targetId);
                            var newPartitionKey = {'stream': sourceId, 'property': ''};
                            var partitionKeys = (model.getPartition('partition'));
                            partitionKeys['with'].push(newPartitionKey);

                            var connectedQueries = _jsPlumb.getConnections({source: target});
                            $.each(connectedQueries, function (index, connectedQuery) {
                                var query = connectedQuery.targetId;
                                var queryID = query.substr(0, query.indexOf('-'));
                                var queryElement = $('#' + queryID);
                                if (queryElement.hasClass(constants.PASS_THROUGH) || queryElement.hasClass(constants.FILTER)
                                    || queryElement.hasClass(constants.WINDOW_QUERY)) {
                                    model = self.appData.getQuery(queryID);
                                    model.setFrom(sourceId);
                                }
                                else if (queryElement.hasClass(constants.JOIN)) {
                                    model = self.appData.getJoinQuery(queryID);
                                    var streams = model.getFrom();
                                    if (streams === undefined || streams === "") {
                                        streams = [sourceId];
                                    } else {
                                        streams.push(sourceId);
                                    }
                                    model.setFrom(streams);
                                }
                                else if (queryElement.hasClass(constants.PATTERN)) {
                                    model = self.appData.getPatternQuery(queryID);
                                    var streams = model.getFrom();
                                    if (streams === undefined || streams === "") {
                                        streams = [sourceId];
                                    } else {
                                        streams.push(sourceId);
                                    }
                                    model.setFrom(streams);
                                }
                            });

                        }
                    }

                    else if (sourceElement.hasClass(constants.PARTITION)) {
                        var connectedStreams = _jsPlumb.getConnections({target: source});
                        var streamID = null;
                        $.each(connectedStreams, function (index, connectedStream) {
                            var stream = connectedStream.sourceId;
                            streamID = stream.substr(0, stream.indexOf('-'));
                        });
                        if (streamID != null) {
                            if (targetElement.hasClass(constants.PASS_THROUGH) || targetElement.hasClass(constants.FILTER)
                                || targetElement.hasClass(constants.WINDOW_QUERY)) {
                                model = self.appData.getQuery(targetId);
                                model.setFrom(streamID);
                            }
                            else if (targetElement.hasClass(constants.JOIN)) {
                                model = self.appData.getJoinQuery(targetId);
                                var streams = model.getFrom();
                                if (streams === undefined || streams === "") {
                                    streams = [streamID];
                                } else {
                                    streams.push(streamID);
                                }
                                model.setFrom(streams);
                            }
                            else if (targetElement.hasClass(constants.PATTERN)) {
                                model = self.appData.getPatternQuery(targetId);
                                var streams = model.getFrom();
                                if (streams === undefined || streams === "") {
                                    streams = [streamID];
                                } else {
                                    streams.push(streamID);
                                }
                                model.setFrom(streams);
                            }
                        }
                    }

                    else if (targetElement.hasClass(constants.STREAM)) {
                        if (sourceElement.hasClass(constants.PASS_THROUGH) || sourceElement.hasClass(constants.FILTER)
                            || sourceElement.hasClass(constants.WINDOW_QUERY)) {
                            model = self.appData.getQuery(sourceId);
                            model.setInsertInto(targetId);
                        }
                        else if (sourceElement.hasClass(constants.PATTERN)) {
                            model = self.appData.getPatternQuery(sourceId);
                            var connectedStreamName = self.appData.getStream(targetId).getName();
                            if (model.getQueryOutput() === '') {
                                var queryOutputOptions = {};
                                _.set(queryOutputOptions, 'type', '');
                                _.set(queryOutputOptions, 'output', '');
                                _.set(queryOutputOptions, 'target', connectedStreamName);
                                var patternQueryOutputObject = new QueryOutput(queryOutputOptions);
                                model.setQueryOutput(patternQueryOutputObject);
                            } else {
                                model.getQueryOutput().setTarget(connectedStreamName);
                            }
                        }
                        else if (sourceElement.hasClass(constants.JOIN)) {
                            model = self.appData.getJoinQuery(sourceId);
                            model.setInsertInto(targetId);
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
                                        _jsPlumb.detach(connectionObject);
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
                _jsPlumb.bind('connectionDetached', function (connection) {

                    var target = connection.targetId;
                    var targetId = target.substr(0, target.indexOf('-'));
                    var targetElement = $('#' + targetId);

                    var source = connection.sourceId;
                    var sourceId = source.substr(0, source.indexOf('-'));
                    var sourceElement = $('#' + sourceId);

                    // removing edge from the edgeList
                    var edgeId = ''+ targetId + '_' + sourceId + '';
                    self.appData.removeEdge(edgeId);

                    var model;
                    var streams;
                    if (sourceElement.hasClass(constants.STREAM)) {
                        if (targetElement.hasClass(constants.PASS_THROUGH) || targetElement.hasClass(constants.FILTER)
                            || targetElement.hasClass(constants.WINDOW_QUERY)) {
                            model = self.appData.getQuery(targetId);
                            if (model !== undefined) {
                                model.setFrom('');
                            }
                        }
                        else if (targetElement.hasClass(constants.JOIN)) {
                            model = self.appData.getJoinQuery(targetId);
                            if (model !== undefined) {
                                streams = model.getFrom();
                                var removedStream = streams.indexOf(sourceId);
                                streams.splice(removedStream, 1);
                                model.setFrom(streams);
                            }
                        }
                        else if (targetElement.hasClass(constants.PATTERN)) {
                            model = self.appData.getPatternQuery(targetId);
                            var disconnectedStreamName = self.appData.getStream(sourceId).getName();
                            model.getQueryInput().removeConnectedElementName(disconnectedStreamName);
                        }
                        else if (targetElement.hasClass(constants.PARTITION)) {
                            model = self.appData.getPartition(targetId);
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

                                var connectedQueries = _jsPlumb.getConnections({source: target});
                                $.each(connectedQueries, function (index, connectedQuery) {
                                    var query = connectedQuery.targetId;
                                    var queryID = query.substr(0, query.indexOf('-'));
                                    var queryElement = $('#' + queryID);
                                    if (queryElement.hasClass(constants.PASS_THROUGH)
                                        || queryElement.hasClass(constants.FILTER)
                                        || queryElement.hasClass(constants.WINDOW_QUERY)) {
                                        model = self.appData.getQuery(queryID);
                                        if (model !== undefined) {
                                            model.setFrom('');
                                        }
                                    }
                                    else if (queryElement.hasClass(constants.JOIN)) {
                                        model = self.appData.getJoinQuery(queryID);
                                        if (model !== undefined) {
                                            streams = model.getFrom();
                                            var removedStream = streams.indexOf(sourceId);
                                            streams.splice(removedStream, 1);
                                            model.setFrom(streams);
                                        }
                                    }
                                    else if (queryElement.hasClass(constants.PATTERN)) {
                                        model = self.appData.getPatternQuery(queryID);
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
                    }

                    else if (sourceElement.hasClass(constants.PARTITION)) {

                        var connectedStreams = _jsPlumb.getConnections({target: source});
                        var streamID = null;
                        $.each(connectedStreams, function (index, connectedStream) {
                            var stream = connectedStream.sourceId;
                            streamID = stream.substr(0, stream.indexOf('-'));
                        });
                        if (targetElement.hasClass(constants.PASS_THROUGH) || targetElement.hasClass(constants.FILTER)
                            || targetElement.hasClass(constants.WINDOW_QUERY)) {
                            model = self.appData.getQuery(targetId);
                            if (model !== undefined) {
                                model.setFrom('');
                            }
                        }
                        else if (targetElement.hasClass(constants.JOIN)) {
                            model = self.appData.getJoinQuery(targetId);
                            if (model !== undefined) {
                                streams = model.getFrom();
                                var removedStream = streams.indexOf(streamID);
                                streams.splice(removedStream, 1);
                                model.setFrom(streams);
                            }
                        }
                        else if (targetElement.hasClass(constants.PATTERN)) {
                            model = self.appData.getPatternQuery(targetId);
                            if (model !== undefined) {
                                streams = model.getFrom();
                                var removedStream = streams.indexOf(streamID);
                                streams.splice(removedStream, 1);
                                model.setFrom(streams);
                            }
                        }
                    }
                    if (targetElement.hasClass(constants.STREAM)) {
                        if (sourceElement.hasClass(constants.PASS_THROUGH) || sourceElement.hasClass(constants.FILTER)
                            || sourceElement.hasClass(constants.WINDOW_QUERY)) {
                            model = self.appData.getQuery(sourceId);
                            if (model !== undefined) {
                                model.setInsertInto('');
                            }
                        }
                        else if (sourceElement.hasClass(constants.JOIN)) {
                            if (targetElement.hasClass(constants.STREAM)) {
                                model = self.appData.getJoinQuery(sourceId);
                                if (model !== undefined) {
                                    model.setInsertInto('');
                                }
                            }
                        }
                        else if (sourceElement.hasClass(constants.PATTERN)) {
                            model = self.appData.getPatternQuery(sourceId);
                            model.getQueryOutput().setTarget('');
                        }
                    }
                });
            }

            function addMemberToPartitionGroup(self) {
                _jsPlumb.bind('group:addMember', function (event) {
                    if($(event.el).hasClass(constants.FILTER) || $(event.el).hasClass(constants.PASS_THROUGH)
                        || $(event.el).hasClass(constants.WINDOW_QUERY) || $(event.el).hasClass(constants.JOIN)
                        || $(event.el).hasClass(constants.STREAM)) {

                        var connections = _jsPlumb.getConnections(event.el);
                        //TODO: insert into can be connected to a outside(not inner) stream as well
                        if($(event.el).hasClass(constants.STREAM)) {

                        }
                        var detachedElement = $(event.el).detach();
                        $(detachedElement).insertBefore($(event.group)[0].getEl());
                        self.autoAlignElements();
                        alert("bc");//TODO: add a proper error message and add align
                        // TODO: stand alone inner stream form should not be displayed
                        var partitionId = $(event.group).attr('id');
                        var partition = self.appData.getPartition(partitionId);
                        var queries = partition.getQueries();
                        if ($(event.el).hasClass(constants.FILTER) || $(event.el).hasClass(constants.PASS_THROUGH)
                            || $(event.el).hasClass(constants.WINDOW_QUERY)) {
                            queries.push(self.appData.getQuery($(event.el).attr('id')));
                            //TODO: set isInner flag true
                            partition.setQueries(queries);
                        }
                        else if ($(event.el).hasClass(constants.JOIN)) {
                            queries.push(self.appData.getJoinQuery($(event.el).attr('id')));
                            partition.setQueries(queries);
                        }
                    } else {
                        alert("Invalid element type dropped into partition!");
                        var detachedElement = $(event.el).detach();
                        $(detachedElement).insertBefore($(event.group)[0].getEl());
                        self.autoAlignElements();
                    }
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

            _.forEach(self.appData.streamList, function(stream){

                var streamId = stream.getId();
                var streamName = stream.getName();
                var mouseTop = parseInt(streamId)*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = parseInt(streamId)*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleStream(mouseTop, mouseLeft, true, streamId, streamName);
            });

            _.forEach(self.appData.tableList, function(table){

                var tableId = table.getId();
                var tableName = table.getName();
                var mouseTop = parseInt(tableId)*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = parseInt(tableId)*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleTable(mouseTop, mouseLeft, true, tableId, tableName);
            });

            _.forEach(self.appData.windowList, function(window){

                var windowId = window.getId();
                var windowName = window.getName();
                var mouseTop = parseInt(windowId)*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = parseInt(windowId)*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleWindow(mouseTop, mouseLeft, true, windowId, windowName);
            });

            _.forEach(self.appData.triggerList, function(trigger){

                var triggerId = trigger.getId();
                var triggerName = trigger.getName();
                var mouseTop = parseInt(triggerId)*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = parseInt(triggerId)*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleTrigger(mouseTop, mouseLeft, true, triggerId, triggerName);
            });

            _.forEach(self.appData.aggregationList, function(aggregation){

                var aggregationId = aggregation.getId();
                var aggregationName = aggregation.getName();
                var mouseTop = parseInt(aggregationId)*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = parseInt(aggregationId)*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handleAggregation(mouseTop, mouseLeft, true, aggregationId, aggregationName);
            });

            _.forEach(self.appData.patternQueryList, function(patternQuery){

                var patternQueryId = patternQuery.getId();
                var patternQueryName = "Pattern Query";
                var mouseTop = parseInt(patternQueryId)*100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = parseInt(patternQueryId)*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handlePatternQuery(mouseTop, mouseLeft, true, patternQueryId, patternQueryName);
            });

            _.forEach(self.appData.queryList, function(query){
                var queryId = query.getId();
                var mouseTop = parseInt(queryId)*100  - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = parseInt(queryId)*200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                self.handlePassThroughQuery(mouseTop, mouseLeft, true, queryId);
                //TODO: correct query types saving style. ex: passthrough should be saved in passthroughList
            });
            _.forEach(self.appData.edgeList, function(edge){

                var targetId = edge.getParentId();
                var sourceId = edge.getChildId();

                _jsPlumb.connect({
                    source: sourceId+'-out',
                    target: targetId+'-in'
                });
            });
        };

        DesignGrid.prototype.generateNextId = function () {
            // TODO: Not finalized
            var currentId = parseInt(this.newAgentId) +1;
            this.newAgentId = "" + currentId + "";
        };

        /**
         * @function Auto align the diagram
         */
        DesignGrid.prototype.autoAlignElements = function () {
            //TODO auto aligning does not support 'Partition'
            var g = new dagre.graphlib.Graph();
            g.setGraph({
                rankdir: 'LR'
            });
            g.setDefaultEdgeLabel(function () {
                return {};
            });
            var nodes =[];
            Array.prototype.push.apply(nodes,document.getElementsByClassName(constants.STREAM));
            Array.prototype.push.apply(nodes,document.getElementsByClassName(constants.PASS_THROUGH));
            Array.prototype.push.apply(nodes,document.getElementsByClassName(constants.FILTER));
            Array.prototype.push.apply(nodes,document.getElementsByClassName(constants.WINDOW_QUERY));
            Array.prototype.push.apply(nodes,document.getElementsByClassName(constants.JOIN));
            Array.prototype.push.apply(nodes,document.getElementsByClassName(constants.PATTERN));
            Array.prototype.push.apply(nodes,document.getElementsByClassName(constants.WINDOW_STREAM));
            Array.prototype.push.apply(nodes,document.getElementsByClassName(constants.PARTITION));

            // var nodes = $(".ui-draggable");
            for (var i = 0; i < nodes.length; i++) {
                var n = nodes[i];
                var nodeID = n.id ;
                g.setNode(nodeID, {width: 120, height: 80});
            }
            var edges = _jsPlumb.getAllConnections();
            for (var i = 0; i < edges.length; i++) {
                var connection = edges[i];
                var target = connection.targetId;
                var source = connection.sourceId;
                var targetId= target.substr(0, target.indexOf('-'));
                var sourceId= source.substr(0, source.indexOf('-'));
                g.setEdge(sourceId, targetId);
            }
            // calculate the layout (i.e. node positions)
            dagre.layout(g);
            // Applying the calculated layout
            g.nodes().forEach(function (v) {
                $("#" + v).css("left", g.node(v).x + "px");
                $("#" + v).css("top", g.node(v).y + "px");
            });

            _jsPlumb.repaintEverything();
            // edges = edges.slice(0);
            // for (var j = 0; j<edges.length ; j++){
            //     var source = edges[j].sourceId;
            //     var target = edges[j].targetId;
            //     _jsPlumb.detach(edges[j]);
            //     _jsPlumb.connect({
            //         source: source,
            //         target: target
            //     });
            //
            // }
        };

        DesignGrid.prototype.handleStream = function (mouseTop, mouseLeft, isCodeToDesignMode, streamId, streamName) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.newAgentId;
                // The container and the toolbox are disabled to prevent the user from dropping any elements before
                // initializing a Stream Element
                self.canvas.addClass("disabledbutton");
                $("#tool-palette-container").addClass("disabledbutton");
                $("#output-console-activate-button").addClass("disabledbutton");
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(streamId !== undefined) {
                    elementId = streamId;
                } else {
                    console.log("streamId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            // increment the Element ID for the next dropped Element
            self.generateNextId();
            var newAgent = $('<div>').attr('id', elementId).addClass('streamdrop');
            self.canvas.append(newAgent);
            // Drop the stream element. Inside this a it generates the stream definition form.
            self.dropElements.dropStream(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode,
                false, streamName);
            self.appData.setFinalElementCount(self.appData.getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handleTable = function (mouseTop, mouseLeft, isCodeToDesignMode, tableId, tableName) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.newAgentId;
                // The container and the toolbox are disabled to prevent the user from dropping any elements before
                // initializing a Table Element
                self.canvas.addClass("disabledbutton");
                $("#tool-palette-container").addClass("disabledbutton");
                $("#output-console-activate-button").addClass("disabledbutton");
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(tableId !== undefined) {
                    elementId = tableId;
                } else {
                    console.log("tableId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            // increment the Element ID for the next dropped Element
            self.generateNextId();
            var newAgent = $('<div>').attr('id', elementId).addClass('tabledrop');
            self.canvas.append(newAgent);
            // Drop the Table element. Inside this a it generates the table definition form.
            self.dropElements.dropTable(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, tableName);
            self.appData.setFinalElementCount(self.appData.getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handleWindow = function (mouseTop, mouseLeft, isCodeToDesignMode, windowId, windowName) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.newAgentId;
                // The container and the toolbox are disabled to prevent the user from dropping any elements before
                // initializing a window Element
                self.canvas.addClass("disabledbutton");
                $("#tool-palette-container").addClass("disabledbutton");
                $("#output-console-activate-button").addClass("disabledbutton");
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(windowId !== undefined) {
                    elementId = windowId;
                } else {
                    console.log("windowId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            // increment the Element ID for the next dropped Element
            self.generateNextId();
            var newAgent = $('<div>').attr('id', elementId).addClass('windowdrop');
            self.canvas.append(newAgent);
            // Drop the Table element. Inside this a it generates the table definition form.
            self.dropElements.dropWindow(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, windowName);
            self.appData.setFinalElementCount(self.appData.getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handleTrigger = function (mouseTop, mouseLeft, isCodeToDesignMode, triggerId, triggerName) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.newAgentId;
                // The container and the toolbox are disabled to prevent the user from dropping any elements before
                // initializing a Trigger Element
                self.canvas.addClass("disabledbutton");
                $("#tool-palette-container").addClass("disabledbutton");
                $("#output-console-activate-button").addClass("disabledbutton");
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(triggerId !== undefined) {
                    elementId = triggerId;
                } else {
                    console.log("triggerId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            // increment the Element ID for the next dropped Element
            self.generateNextId();
            var newAgent = $('<div>').attr('id', elementId).addClass('triggerdrop');
            self.canvas.append(newAgent);
            // Drop the Trigger element. Inside this a it generates the trigger definition form.
            self.dropElements.dropTrigger(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, triggerName);
            self.appData.setFinalElementCount(self.appData.getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        //TODO: Reduce code duplication. Handle definitions in a one method.
        DesignGrid.prototype.handleAggregation = function (mouseTop, mouseLeft, isCodeToDesignMode, aggregationId, aggregationName) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.newAgentId;
                // The container and the toolbox are disabled to prevent the user from dropping any elements before
                // initializing a Aggregation Element
                self.canvas.addClass("disabledbutton");
                $("#tool-palette-container").addClass("disabledbutton");
                $("#output-console-activate-button").addClass("disabledbutton");
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(aggregationId !== undefined) {
                    elementId = aggregationId;
                } else {
                    console.log("aggregationId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            // increment the Element ID for the next dropped Element
            self.generateNextId();
            var newAgent = $('<div>').attr('id', elementId).addClass('aggregationdrop');
            self.canvas.append(newAgent);
            // Drop the Aggregation element. Inside this a it generates the aggregation definition form.
            self.dropElements.dropAggregation(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, aggregationName);
            self.appData.setFinalElementCount(self.appData.getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };


        DesignGrid.prototype.handlePassThroughQuery = function (mouseTop, mouseLeft, isCodeToDesignMode, queryId) {
            var self = this;
            var elementId;
            var passThroughQueryName;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.newAgentId;
                passThroughQueryName = "Empty Query";
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(queryId !== undefined) {
                    elementId = queryId;
                } else {
                    console.log("queryId parameter is undefined");
                }
                passThroughQueryName = "Empty Query";
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            // increment the Element ID for the next dropped Element
            self.generateNextId();
            var newAgent = $('<div>').attr('id', elementId).addClass('squerydrop');
            var dropType = "squerydrop";
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropQuery(newAgent, elementId, dropType, mouseTop, mouseLeft, passThroughQueryName,
                isCodeToDesignMode);
            self.appData.setFinalElementCount(self.appData.getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handleFilterQuery = function (mouseTop, mouseLeft, isCodeToDesignMode) {
            var self =this;
            var newAgent = $('<div>').attr('id', self.newAgentId).addClass('filterdrop ');
            var droptype = "filterdrop";
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropQuery(newAgent, self.newAgentId, droptype, mouseTop, mouseLeft, "Empty Query",
                isCodeToDesignMode);
            self.appData.setFinalElementCount(self.appData.getFinalElementCount() + 1);
            self.generateNextId();
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handleWindowQuery = function (mouseTop, mouseLeft, isCodeToDesignMode) {
            var self = this;
            var newAgent = $('<div>').attr('id', self.newAgentId).addClass('wquerydrop ');
            var droptype = "wquerydrop";
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropQuery(newAgent, self.newAgentId, droptype, mouseTop, mouseLeft, "Empty Query",
                isCodeToDesignMode);
            self.appData.setFinalElementCount(self.appData.getFinalElementCount() + 1);
            self.generateNextId();
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handleJoinQuery = function (mouseTop, mouseLeft, isCodeToDesignMode) {
            var self = this;
            var newAgent = $('<div>').attr('id', self.newAgentId).addClass('joquerydrop');
            var droptype = "joquerydrop";
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropQuery(newAgent, self.newAgentId, droptype, mouseTop, mouseLeft, "Join Query",
                isCodeToDesignMode);
            self.appData.setFinalElementCount(self.appData.getFinalElementCount() + 1);
            self.generateNextId();
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handlePatternQuery = function (mouseTop, mouseLeft, isCodeToDesignMode, patternQueryId,
                                                            patternQueryName) {
            var self = this;
            var elementId;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.newAgentId;
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(patternQueryId !== undefined) {
                    elementId = patternQueryId;
                } else {
                    console.log("patternQueryId parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
            // increment the Element ID for the next dropped Element
            self.generateNextId();
            var newAgent = $('<div>').attr('id', elementId).addClass('patternQueryDrop');
            self.canvas.append(newAgent);
            // Drop the Table element. Inside this a it generates the table definition form.
            self.dropElements.dropPatternQuery(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode, patternQueryName);
            self.appData.setFinalElementCount(self.appData.getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handlePartition = function (mouseTop, mouseLeft, isCodeToDesignMode) {
            var self = this;
            var newAgent = $('<div>').attr('id', self.newAgentId).addClass('partitiondrop');
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropPartition(newAgent, self.newAgentId, mouseTop, mouseLeft, isCodeToDesignMode);
            self.appData.setFinalElementCount(self.appData.getFinalElementCount() + 1);
            self.generateNextId();
            self.dropElements.registerElementEventListeners(newAgent);
            //TODO: connection points should be able to remove( close icon). Then update on connection detach.
        };

        DesignGrid.prototype.getNewAgentId = function () {
            return this.newAgentId;
        };

        return DesignGrid;
    });
