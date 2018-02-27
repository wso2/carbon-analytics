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
define(['require', 'log', 'jquery', 'jsplumb','backbone', 'lodash', 'dropElements', 'appData', 'dagre', 'formBuilder',
        'jquery_ui'],

    function (require, log, $, _jsPlumb ,Backbone, _, DropElements, AppData, dagre, FormBuilder) {

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
         * @class DesignView
         * @constructor
         * @class DesignView  Wraps the Ace editor for design view
         * @param {Object} options Rendering options for the view
         */
        var DesignView = function (options) {
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
            this.container = this.options.container;
        };

        DesignView.prototype.render = function () {
            var self = this;

            // i --> newAgent ID (Dropped Element ID)
            var i = 1;
            // finalElementCount --> Number of elements that exist on the canvas at the time of saving the model
            var finalElementCount=0;
            var appData = new AppData();
            var formBuilder = new FormBuilder();
            var dropElementsOpts = {};
            _.set(dropElementsOpts, 'container', self.container);
            _.set(dropElementsOpts, 'app', appData);
            _.set(dropElementsOpts, 'formBuilder', formBuilder);
            this.dropElements =new DropElements(dropElementsOpts);

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

                var canvas = $(self.container);

                /**
                 * @function droppable method for the 'stream' & the 'query' objects
                 */
                canvas.droppable
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
                        var mouseTop = e.pageY - canvas.offset().top +canvas.scrollTop()- 40;
                        var mouseLeft = e.pageX - canvas.offset().left +canvas.scrollLeft()- 60;
                        // Clone the element in the toolbox in order to drop the clone on the canvas
                        var droppedElement = ui.helper.clone();
                        // To further manipulate the jsplumb element, remove the jquery UI clone helper as jsPlumb doesn't support it
                        ui.helper.remove();
                        $(droppedElement).draggable({containment: "parent"});
                        // Repaint to reposition all the elements that are on the canvas after the drop/addition of a new element on the canvas
                        _jsPlumb.repaint(ui.helper);

                        // droptype --> Type of query being dropped on the canvas (e.g. droptype = "squerydrop";)
                        var droptype;
                        var newAgent;


                        // If the dropped Element is a Stream then->
                        if ($(droppedElement).hasClass('stream')) {
                            newAgent = $('<div>').attr('id', i).addClass('streamdrop ');

                            // The container and the toolbox are disabled to prevent the user from dropping any elements before initializing a Stream Element
                            //canvas.addClass("disabledbutton");
                            //$("#toolbox").addClass("disabledbutton");

                            canvas.append(newAgent);
                            // generate the stream definition form
                            formBuilder.DefineStream(newAgent,i,mouseTop,mouseLeft);
                            //self.dropElements.dropStream(newAgent, i, mouseTop, mouseLeft, "Stream");
                            finalElementCount = i;
                            i++;    // Increment the Element ID for the next dropped Element

                        }

                        // If the dropped Element is a Window(not window query) then->
                        else if ($(droppedElement).hasClass('window-stream')) {
                            newAgent = $('<div>').attr('id', i).addClass('wstreamdrop');
                            // Drop the element instantly since its projections will be set only when the user requires it
                            self.dropElements.dropWindowStream(newAgent, i, mouseTop, mouseLeft ,"Window");
                            finalElementCount=i;
                            i++;
                        }

                        // If the dropped Element is a Pass through Query then->
                        else if ($(droppedElement).hasClass('pass-through')) {
                            newAgent = $('<div>').attr('id', i).addClass('squerydrop');
                            droptype = "squerydrop";
                            // Drop the element instantly since its projections will be set only when the user requires it
                            self.dropElements.dropQuery(newAgent, i, droptype, mouseTop, mouseLeft, "Empty Query");
                            finalElementCount=i;
                            i++;
                        }

                        // If the dropped Element is a Filter query then->
                        else if ($(droppedElement).hasClass('filter-query')) {
                            newAgent = $('<div>').attr('id', i).addClass('filterdrop ');
                            droptype = "filterdrop";
                            // Drop the element instantly since its projections will be set only when the user requires it
                            self.dropElements.dropQuery(newAgent, i, droptype, mouseTop, mouseLeft, "Empty Query");
                            finalElementCount=i;
                            i++;
                        }

                        // If the dropped Element is a Window Query then->
                        else if ($(droppedElement).hasClass('window-query')) {
                            newAgent = $('<div>').attr('id', i).addClass('wquerydrop ');
                            droptype = "wquerydrop";
                            // Drop the element instantly since its projections will be set only when the user requires it
                            self.dropElements.dropQuery(newAgent, i, droptype, mouseTop, mouseLeft, "Empty Query");
                            finalElementCount=i;
                            i++;
                        }

                        // If the dropped Element is a Join Query then->
                        else if ($(droppedElement).hasClass('join-query')) {
                            newAgent = $('<div>').attr('id', i).addClass('joquerydrop');
                            droptype = "joquerydrop";
                            // Drop the element instantly since its projections will be set only when the user requires it
                            self.dropElements.dropQuery(newAgent, i, droptype, mouseTop, mouseLeft, "Join Query");
                            finalElementCount=i;
                            i++;
                        }

                        // If the dropped Element is a State machine Query(Pattern and Sequence) then->
                        else if($(droppedElement).hasClass('pattern')) {
                            newAgent = $('<div>').attr('id', i).addClass('stquerydrop');
                            droptype = "stquerydrop";
                            // Drop the element instantly since its projections will be set only when the user requires it
                            self.dropElements.dropQuery(newAgent, i, droptype, mouseTop, mouseLeft, "Pattern Query");
                            finalElementCount=i;
                            i++;
                        }

                        // If the dropped Element is a Partition then->
                        else if($(droppedElement).hasClass('partition')) {
                            newAgent = $('<div>').attr('id', i).addClass('partitiondrop');
                            // Drop the element instantly since its projections will be set only when the user requires it
                            self.dropElements.dropPartition(newAgent, i, mouseTop, mouseLeft);
                            finalElementCount=i;

                            i++;
                        }
                        self.dropElements.registerElementEventListeners(newAgent);
                    }
                });

                // auto align the diagram when the button is clicked
                $('#auto-align').click(function(){
                    autoAlign();
                });

            });

            // check the validity of the connections and drop if invalid
            _jsPlumb.bind('beforeDrop', function(connection){
                var connectionValidity= true;
                var target = connection.targetId;
                var targetId= target.substr(0, target.indexOf('-'));
                var targetElement = $('#'+targetId);

                var source = connection.sourceId;
                var sourceId = source.substr(0, source.indexOf('-'));
                var sourceElement = $('#'+sourceId);


                // avoid the expose of inner-streams outside the group
                if( sourceElement.hasClass(constants.STREAM) && _jsPlumb.getGroupFor(sourceId) !== undefined ){
                    if( _jsPlumb.getGroupFor(sourceId) !== _jsPlumb.getGroupFor(targetId)){
                        connectionValidity = false;
                        alert("Invalid Connection: Inner Streams are not exposed to outside");
                    }
                }
                else if( targetElement.hasClass(constants.STREAM) && _jsPlumb.getGroupFor(targetId) !== undefined ){
                    if( _jsPlumb.getGroupFor(targetId) !== _jsPlumb.getGroupFor(sourceId)){
                        connectionValidity = false;
                        alert("Invalid Connection: Inner Streams are not exposed to outside");
                    }
                }
                if($('#'+sourceId).hasClass(constants.PARTITION)){
                    if($(_jsPlumb.getGroupFor(targetId)).attr('id') !== sourceId){
                        connectionValidity = false;
                        alert("Invalid Connection: Connect to a partition query");
                    }
                }
                else if( targetElement.hasClass(constants.PASS_THROUGH) || targetElement.hasClass(constants.FILTER)
                    || targetElement.hasClass(constants.WINDOW_QUERY)
                    || targetElement.hasClass(constants.PATTERN) || targetElement.hasClass(constants.JOIN)) {
                    if (!(sourceElement.hasClass(constants.STREAM)))  {
                        connectionValidity = false;
                        alert("Invalid Connection");
                    }
                }
                else if( sourceElement.hasClass(constants.PASS_THROUGH) || sourceElement.hasClass(constants.FILTER)
                    || sourceElement.hasClass(constants.WINDOW_QUERY)
                    || sourceElement.hasClass(constants.PATTERN) || sourceElement.hasClass(constants.JOIN)) {
                    if (!(targetElement.hasClass(constants.STREAM))) {
                        connectionValidity = false;
                        alert("Invalid Connection");
                    }
                }
                return connectionValidity;
            });

            // Update the model when a connection is established and bind events for the connection
            _jsPlumb.bind('connection' , function(connection){
                var target = connection.targetId;
                var targetId= target.substr(0, target.indexOf('-'));
                var targetElement = $('#'+targetId);

                var source = connection.sourceId;
                var sourceId = source.substr(0, source.indexOf('-'));
                var sourceElement = $('#'+sourceId);

                var model;

                if (sourceElement.hasClass(constants.STREAM)) {
                    if (targetElement.hasClass(constants.PASS_THROUGH) || targetElement.hasClass(constants.FILTER)
                        || targetElement.hasClass(constants.WINDOW_QUERY)) {
                        model = appData.getQuery(targetId);
                        model.setFrom(sourceId);
                    }

                    else if (targetElement.hasClass(constants.PATTERN)){
                        model = appData.getPatternQuery(targetId);
                        var streams = model.getFrom();
                        if (streams === undefined){
                            streams = [sourceId];
                        } else {
                            streams.push(sourceId);
                        }
                        model.setFrom(streams);
                    }
                    else if (targetElement.hasClass(constants.JOIN)) {
                        model = appData.getJoinQuery(targetId);
                        var streams = model.getFrom();
                        if (streams === undefined) {
                            streams = [sourceId];
                        } else {
                            streams.push(sourceId);
                        }
                        model.setFrom(streams);
                    }
                    else if (targetElement.hasClass(constants.PARTITION)){
                        model = appData.getPartition(targetId);
                        var newPartitionKey = { 'stream' : sourceId , 'property' :''};
                        var partitionKeys = (model.getPartition('partition'));
                        partitionKeys['with'].push(newPartitionKey);

                        var connectedQueries = _jsPlumb.getConnections({source : target});
                        $.each(connectedQueries , function (index, connectedQuery) {
                            var query= connectedQuery.targetId;
                            var queryID = query.substr(0, query.indexOf('-'));
                            var queryElement = $('#'+queryID);
                            if( queryElement.hasClass(constants.PASS_THROUGH) || queryElement.hasClass(constants.FILTER)
                                || queryElement.hasClass(constants.WINDOW_QUERY)){
                                model = appData.getQuery(queryID);
                                model.setFrom(sourceId);
                            }
                            else if (queryElement.hasClass(constants.JOIN)){
                                model = appData.getJoinQuery(queryID);
                                var streams = model.getFrom();
                                if (streams === undefined) {
                                    streams = [sourceId];
                                } else {
                                    streams.push(sourceId);
                                }
                                model.setFrom(streams);
                            }
                            else if (queryElement.hasClass(constants.PATTERN)){
                                model = appData.getPatternQuery(queryID);
                                var streams = model.getFrom();
                                if (streams === undefined){
                                    streams = [sourceId];
                                } else {
                                    streams.push(sourceId);
                                }
                                model.setFrom(streams);
                            }
                        });

                    }
                }

                else if (sourceElement.hasClass(constants.PARTITION)){
                    var connectedStreams = _jsPlumb.getConnections({target : source});
                    var streamID = null;
                    $.each(connectedStreams , function (index, connectedStream) {
                        var stream= connectedStream.sourceId;
                        streamID = stream.substr(0, stream.indexOf('-'));
                    });
                    if(streamID != null){
                        if( targetElement.hasClass(constants.PASS_THROUGH) || targetElement.hasClass(constants.FILTER)
                            || targetElement.hasClass(constants.WINDOW_QUERY)){
                            model = appData.getQuery(targetId);
                            model.setFrom(streamID);
                        }
                        else if (targetElement.hasClass(constants.JOIN)){
                            model = appData.getJoinQuery(targetId);
                            var streams = model.getFrom();
                            if (streams === undefined) {
                                streams = [streamID];
                            } else {
                                streams.push(streamID);
                            }
                            model.setFrom(streams);
                        }
                        else if (targetElement.hasClass(constants.PATTERN)){
                            model = appData.getPatternQuery(targetId);
                            var streams = model.getFrom();
                            if (streams === undefined){
                                streams = [streamID];
                            } else {
                                streams.push(streamID);
                            }
                            model.setFrom(streams);
                        }
                    }
                }

                else if (targetElement.hasClass(constants.STREAM)){
                    if( sourceElement.hasClass(constants.PASS_THROUGH) || sourceElement.hasClass(constants.FILTER)
                        || sourceElement.hasClass(constants.WINDOW_QUERY)){
                        model = appData.getQuery(sourceId);
                        model.setInsertInto(targetId);
                    }
                    else if (sourceElement.hasClass(constants.PATTERN)){
                        model = appData.getPatternQuery(sourceId);
                        model.setInsertInto(targetId);
                    }
                    else if (sourceElement.hasClass(constants.JOIN)){
                        model = appData.getJoinQuery(sourceId);
                        model.setInsertInto(targetId);
                    }
                }

                var connectionObject = connection.connection;
                // add a overlay of a close icon for connection. connection can be detached by clicking on it
                var close_icon_overlay = connectionObject.addOverlay([
                    "Custom", {
                        create:function() {
                            return $('<img src="/editor/images/cancel.png" alt="">');
                        },
                        location : 0.60,
                        id:"close",
                        events:{
                            click:function() {
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
                connectionObject.bind('mouseover', function() {
                    close_icon_overlay.setVisible(true);
                });
                // hide the close icon when the mouse is not on the connection path
                connectionObject.bind('mouseout', function() {
                    close_icon_overlay.setVisible(false);
                });
            });

            // Update the model when a connection is detached
            _jsPlumb.bind('connectionDetached', function (connection) {

                var target = connection.targetId;
                var targetId= target.substr(0, target.indexOf('-'));
                var targetElement = $('#'+targetId);

                var source = connection.sourceId;
                var sourceId = source.substr(0, source.indexOf('-'));
                var sourceElement = $('#'+sourceId);

                var model;
                var streams;
                if ( sourceElement.hasClass(constants.STREAM)){
                    if( targetElement.hasClass(constants.PASS_THROUGH) || targetElement.hasClass(constants.FILTER)
                        || targetElement.hasClass(constants.WINDOW_QUERY)){
                        model = appData.getQuery(targetId);
                        if (model !== undefined){
                            model.setFrom('');
                        }
                    }
                    else if (targetElement.hasClass(constants.JOIN)){
                        model = appData.getJoinQuery(targetId);
                        if (model !== undefined){
                            streams = model.getFrom();
                            var removedStream = streams.indexOf(sourceId);
                            streams.splice(removedStream,1);
                            model.setFrom(streams);
                        }
                    }
                    else if ( targetElement.hasClass(constants.PATTERN)){
                        model = appData.getPatternQuery(targetId);
                        if (model !== undefined){
                            streams = model.getFrom();
                            var removedStream = streams.indexOf(sourceId);
                            streams.splice(removedStream,1);
                            model.setFrom(streams);
                        }
                    }
                    else if ( targetElement.hasClass(constants.PARTITION)){
                        model = appData.getPatternQuery(targetId);
                        if (model !== undefined){
                            var removedPartitionKey = null;
                            var partitionKeys = (model.getPartition().with);
                            $.each(partitionKeys, function ( index , key) {
                                if( key.stream === sourceId){
                                    removedPartitionKey = index;
                                }
                            });
                            partitionKeys.splice(removedPartitionKey,1);
                            var partitionKeysObj = { 'with' : partitionKeys};
                            model.setPartition(partitionKeysObj);

                            var connectedQueries = _jsPlumb.getConnections({source : target});
                            $.each(connectedQueries , function (index, connectedQuery) {
                                var query= connectedQuery.targetId;
                                var queryID = query.substr(0, query.indexOf('-'));
                                var queryElement = $('#'+queryID);
                                if( queryElement.hasClass(constants.PASS_THROUGH)
                                    || queryElement.hasClass(constants.FILTER)
                                    || queryElement.hasClass(constants.WINDOW_QUERY)){
                                    model = appData.getQuery(queryID);
                                    if (model !== undefined){
                                        model.setFrom('');
                                    }
                                }
                                else if (queryElement.hasClass(constants.JOIN)){
                                    model = appData.getJoinQuery(queryID);
                                    if (model !== undefined){
                                        streams = model.getFrom();
                                        var removedStream = streams.indexOf(sourceId);
                                        streams.splice(removedStream,1);
                                        model.setFrom(streams);
                                    }
                                }
                                else if (queryElement.hasClass(constants.PATTERN)){
                                    model = appData.getPatternQuery(queryID);
                                    if (model !== undefined){
                                        streams = model.getFrom();
                                        var removedStream = streams.indexOf(sourceId);
                                        streams.splice(removedStream,1);
                                        model.setFrom(streams);
                                    }
                                }
                            });
                        }
                    }
                }

                else if ( sourceElement.hasClass(constants.PARTITION)){

                    var connectedStreams = _jsPlumb.getConnections({target : source});
                    var streamID = null;
                    $.each(connectedStreams , function (index, connectedStream) {
                        var stream= connectedStream.sourceId;
                        streamID = stream.substr(0, stream.indexOf('-'));
                    });
                    if( targetElement.hasClass(constants.PASS_THROUGH) || targetElement.hasClass(constants.FILTER)
                        || targetElement.hasClass(constants.WINDOW_QUERY)){
                        model = appData.getQuery(targetId);
                        if (model !== undefined){
                            model.setFrom('');
                        }
                    }
                    else if (targetElement.hasClass(constants.JOIN)){
                        model = appData.getJoinQuery(targetId);
                        if (model !== undefined){
                            streams = model.getFrom();
                            var removedStream = streams.indexOf(streamID);
                            streams.splice(removedStream,1);
                            model.setFrom(streams);
                        }
                    }
                    else if (targetElement.hasClass(constants.PATTERN)) {
                        model = appData.getPatternQuery(targetId);
                        if (model !== undefined) {
                            streams = model.getFrom();
                            var removedStream = streams.indexOf(streamID);
                            streams.splice(removedStream, 1);
                            model.setFrom(streams);
                        }
                    }
                }
                if(targetElement.hasClass(constants.STREAM)){
                    if( sourceElement.hasClass(constants.PASS_THROUGH) || sourceElement.hasClass(constants.FILTER)
                        || sourceElement.hasClass(constants.WINDOW_QUERY)){
                        model = appData.getQuery(sourceId);
                        if (model !== undefined){
                            model.setInsertInto('');
                        }
                    }
                    else if (sourceElement.hasClass(constants.JOIN)){
                        if(targetElement.hasClass(constants.STREAM)){
                            model = appData.getJoinQuery(sourceId);
                            if (model !== undefined){
                                model.setInsertInto('');
                            }
                        }
                    }
                    else if (sourceElement.hasClass(constants.PATTERN)){
                        if(targetElement.hasClass(constants.STREAM)){
                            model = appData.getPatternQuery(sourceId);
                            if (model !== undefined){
                                model.setInsertInto('');
                            }
                        }
                    }
                }

            });

            _jsPlumb.bind('group:addMember' , function (event){
                var partitionId = $(event.group).attr('id');
                var partition = appData.getPartition(partitionId);
                var queries = partition.getQueries();
                if($(event.el).hasClass(constants.FILTER) || $(event.el).hasClass(constants.PASS_THROUGH)
                    || $(event.el).hasClass(constants.WINDOW_QUERY)){
                    queries.push(appData.getQuery($(event.el).attr('id')));
                    partition.setQueries(queries);
                }
                else if($(event.el).hasClass(constants.JOIN)){
                    queries.push(appData.getJoinQuery($(event.el).attr('id')));
                    partition.setQueries(queries);
                }
            });

            /**
             * @function Auto align the diagram
             */
            function autoAlign() {
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
            }


        };

        return DesignView;
    });
