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
define(['require', 'log', 'jquery', 'jsplumb','backbone', 'lodash', 'dropElements', 'dagre', 'edge'],

    function (require, log, $, _jsPlumb ,Backbone, _, DropElements, dagre, Edge) {

        var constants = {
            STREAM: 'streamdrop',
            PASS_THROUGH : 'squerydrop',
            FILTER : 'filterdrop',
            JOIN : 'joquerydrop',
            WINDOW_QUERY : 'wquerydrop',
            PATTERN : 'stquerydrop',
            WINDOW_STREAM :'',
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
            //TODO: if close button in output console is clicked disabled containers should active
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
                    accept: '.stream, .window-stream, .pass-through, .filter-query, .join-query, .window-query, ' +
                    '.pattern, .partition',
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

                        // If the dropped Element is a Window(not window query) then->
                        else if ($(droppedElement).hasClass('window-stream')) {
                            self.handleWindowStream(mouseTop, mouseLeft, false);
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

                        // If the dropped Element is a State machine Query(Pattern and Sequence) then->
                        else if($(droppedElement).hasClass('pattern')) {
                            self.handlePatternQuery(mouseTop, mouseLeft, false);
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
                    else if (targetElement.hasClass(constants.PASS_THROUGH) || targetElement.hasClass(constants.FILTER)
                        || targetElement.hasClass(constants.WINDOW_QUERY)
                        || targetElement.hasClass(constants.PATTERN) || targetElement.hasClass(constants.JOIN)) {
                        if (!(sourceElement.hasClass(constants.STREAM))) {
                            connectionValidity = false;
                            alert("Invalid Connection");
                        }
                    }
                    else if (sourceElement.hasClass(constants.PASS_THROUGH) || sourceElement.hasClass(constants.FILTER)
                        || sourceElement.hasClass(constants.WINDOW_QUERY)
                        || sourceElement.hasClass(constants.PATTERN) || sourceElement.hasClass(constants.JOIN)) {
                        if (!(targetElement.hasClass(constants.STREAM))) {
                            connectionValidity = false;
                            alert("Invalid Connection");
                        }
                    }
                    return connectionValidity;
                });
            }

            // Update the model when a connection is established and bind events for the connection
            function updateModelOnConnectionAttach() {
                _jsPlumb.bind('connection', function (connection) {
                    var target = connection.targetId;
                    var targetId = target.substr(0, target.indexOf('-'));
                    var targetElement = $('#' + targetId);

                    var source = connection.sourceId;
                    var sourceId = source.substr(0, source.indexOf('-'));
                    var sourceElement = $('#' + sourceId);

                    // create and add an ege to the edgeList
                    var edgeOptions = {};
                    var edgeId = ''+ targetId + '_' + sourceId + '';
                    _.set(edgeOptions, 'id', edgeId);
                    _.set(edgeOptions, 'parentId', targetId);
                    _.set(edgeOptions, 'parentType', 'query');//TODO: correct this
                    _.set(edgeOptions, 'childId', sourceId);
                    _.set(edgeOptions, 'childType', 'stream');
                    var edge = new Edge(edgeOptions);
                    self.appData.addEdge(edge);

                    var model;

                    if (sourceElement.hasClass(constants.STREAM)) {
                        if (targetElement.hasClass(constants.PASS_THROUGH) || targetElement.hasClass(constants.FILTER)
                            || targetElement.hasClass(constants.WINDOW_QUERY)) {
                            model = self.appData.getQuery(targetId);
                            model.setFrom(sourceId);
                        }

                        else if (targetElement.hasClass(constants.PATTERN)) {
                            model = self.appData.getPatternQuery(targetId);
                            var streams = model.getFrom();
                            if (streams === undefined || streams === "") {
                                streams = [sourceId];
                            } else {
                                streams.push(sourceId);
                            }
                            model.setFrom(streams);
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
                            model.setInsertInto(targetId);
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
                            if (model !== undefined) {
                                streams = model.getFrom();
                                var removedStream = streams.indexOf(sourceId);
                                streams.splice(removedStream, 1);
                                model.setFrom(streams);
                            }
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
                            if (targetElement.hasClass(constants.STREAM)) {
                                model = self.appData.getPatternQuery(sourceId);
                                if (model !== undefined) {
                                    model.setInsertInto('');
                                }
                            }
                        }
                    }

                });
            }

            function addMemberToPartitionGroup() {
                _jsPlumb.bind('group:addMember', function (event) {
                    var partitionId = $(event.group).attr('id');
                    var partition = self.appData.getPartition(partitionId);
                    var queries = partition.getQueries();
                    if ($(event.el).hasClass(constants.FILTER) || $(event.el).hasClass(constants.PASS_THROUGH)
                        || $(event.el).hasClass(constants.WINDOW_QUERY)) {
                        queries.push(self.appData.getQuery($(event.el).attr('id')));
                        partition.setQueries(queries);
                    }
                    else if ($(event.el).hasClass(constants.JOIN)) {
                        queries.push(self.appData.getJoinQuery($(event.el).attr('id')));
                        partition.setQueries(queries);
                    }
                });
            }

            checkConnectionValidityBeforeElementDrop();

            updateModelOnConnectionAttach();

            updateModelOnConnectionDetach();

            addMemberToPartitionGroup();

            self.drawGraphFromAppData();
        };

        DesignGrid.prototype.drawGraphFromAppData = function () {
            var self = this;

            _.forEach(self.appData.streamList, function(stream){
                var mouseTop = 100 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = 200 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                var streamId = stream.getId();
                var streamName = stream.getDefine();
                self.handleStream(mouseTop, mouseLeft, true, streamId, streamName);
            });

            _.forEach(self.appData.queryList, function(query){
                var mouseTop = 300 - self.canvas.offset().top + self.canvas.scrollTop()- 40;
                var mouseLeft = 400 - self.canvas.offset().left + self.canvas.scrollLeft()- 60;
                var queryId = query.getId();
                var queryName = query.getName();
                self.handlePassThroughQuery(mouseTop, mouseLeft, true, queryId, queryName);
                //TODO: correct query types saving style. ex: passthrough should be saved in passthroughList
            });
            _.forEach(self.appData.edgeList, function(edge){

                var targetId = edge.getParentId();
                var targetType = edge.getParentType();
                var targetElement = $('#' + targetId);

                var sourceId = edge.getChildId();
                var sourceElement = $('#' + sourceId);
                var sourceType = edge.getChildType();

                _jsPlumb.connect({
                    source: sourceId + "-out",
                    target: targetId + "-in"
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
                self.generateNextId();    // Increment the Element ID for the next dropped Element
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

            var newAgent = $('<div>').attr('id', elementId).addClass('streamdrop');
            self.canvas.append(newAgent);
            // Drop the stream element. Inside this a it generates the stream definition form.
            self.dropElements.dropStream(newAgent, elementId, mouseTop, mouseLeft, isCodeToDesignMode,
                false, streamName);
            self.appData.setFinalElementCount(self.appData.getFinalElementCount() + 1);
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handleWindowStream = function (mouseTop, mouseLeft, isCodeToDesignMode) {
            var self = this;
            var newAgent = $('<div>').attr('id', self.newAgentId).addClass('wstreamdrop');
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropWindowStream(newAgent, self.newAgentId, mouseTop, mouseLeft ,"Window",
                isCodeToDesignMode);
            self.appData.setFinalElementCount(self.appData.getFinalElementCount() + 1);
            self.generateNextId();
            self.dropElements.registerElementEventListeners(newAgent);
        };

        DesignGrid.prototype.handlePassThroughQuery = function (mouseTop, mouseLeft, isCodeToDesignMode, queryId,
                                                                queryName) {
            var self = this;
            var elementId;
            var passThroughQueryName;
            if (isCodeToDesignMode !== undefined && !isCodeToDesignMode) {
                elementId = self.newAgentId;
                passThroughQueryName = "Empty Query";
                self.generateNextId();    // Increment the Element ID for the next dropped Element
            } else if (isCodeToDesignMode !== undefined && isCodeToDesignMode) {
                if(queryId !== undefined) {
                    elementId = queryId;
                } else {
                    console.log("queryId parameter is undefined");
                }
                if(queryName !== undefined) {
                    passThroughQueryName = queryName;
                } else {
                    console.log("queryName parameter is undefined");
                }
            } else {
                console.log("isCodeToDesignMode parameter is undefined");
            }
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

        DesignGrid.prototype.handlePatternQuery = function (mouseTop, mouseLeft, isCodeToDesignMode) {
            var self = this;
            var newAgent = $('<div>').attr('id', self.newAgentId).addClass('stquerydrop');
            var droptype = "stquerydrop";
            // Drop the element instantly since its projections will be set only when the user requires it
            self.dropElements.dropQuery(newAgent, self.newAgentId, droptype, mouseTop, mouseLeft, "Pattern Query",
                isCodeToDesignMode);
            self.appData.setFinalElementCount(self.appData.getFinalElementCount() + 1);
            self.generateNextId();
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
        };

        DesignGrid.prototype.getNewAgentId = function () {
            return this.newAgentId;
        };

        return DesignGrid;
    });
