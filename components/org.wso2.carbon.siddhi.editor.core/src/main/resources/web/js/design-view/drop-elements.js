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

define(['require', 'log', 'lodash', 'jquery', 'jsplumb', 'filterQuery', 'joinQuery', 'partition', 'passThroughQuery',
    'patternQuery', 'query', 'stream', 'windowQuery'],
    function (require, log, _, $, _jsPlumb, FilterQuery, JoinQuery, Partition, PassThroughQuery, PatternQuery, Query,
              Stream, WindowQuery) {

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
        var dropElements = function (options) {
            var errorMessage = 'unable to find design view container';
            if (!_.has(options, 'container')) {
                log.error(errorMessage);
                throw errorMessage;
                //TODO: handle all the log. Throw detailed messages.
            }
            var container = $(_.get(options, 'container'));
            if (!container.length > 0) {
                log.error(errorMessage);
                throw errorMessage;
            }
            this.app = options.app;
            this.container = options.container;
            this.formBuilder = options.formBuilder;
            this.options = options;
        };

        dropElements.prototype.dropStream = function (newAgent,i,top,left, name) {
            /*
             The node hosts a text node where the Stream's name input by the user will be held.
             Rather than simply having a `newAgent.text(streamName)` statement, as the text function tends to
             reposition the other appended elements with the length of the Stream name input by the user.
            */

            var self= this;
            var node = $('<div>' + name + '</div>');
            newAgent.append(node);
            node.attr('id', i+"-nodeInitial");
            node.attr('class', "streamNameNode");

            /*
             prop --> When clicked on this icon, a definition and related information of the Stream Element will be displayed as an alert message
             showIcon --> An icon that elucidates whether the dropped stream element is an Import/Export/Defined stream (In this case: an Import arrow icon)
             conIcon --> Clicking this icon is supposed to toggle between showing and hiding the "Connection Anchor Points" (Not implemented)
            */
            // TODO: not implemented (from earlier version)
            var prop = $('<img src="/editor/images/settings.png" class="element-prop-icon collapse" ' +
                'onclick ="self.formBuilder.GeneratePropertiesFormForStreams(this)">');
            newAgent.append(node).append('<img src="/editor/images/cancel.png" class="element-close-icon collapse">').append(prop);

            var finalElement = newAgent;

            /*
             connection --> The connection anchor point is appended to the element
             */
            var connection1 = $('<div class="connectorInStream">').attr('id', i+"-in" ).addClass('connection');
            var connection2 = $('<div class="connectorOutStream">').attr('id', i+"-out" ).addClass('connection');


            finalElement.append(connection1);
            finalElement.append(connection2);

            finalElement.css({
                'top': top,
                'left': left
            });

            $(this.container).append(finalElement);

            _jsPlumb.draggable(finalElement, {
                containment: 'grid-container'
            });
            _jsPlumb.makeTarget(connection1, {
                deleteEndpointsOnDetach:true,
                anchor: 'Left'
            });

            _jsPlumb.makeSource(connection2, {
                deleteEndpointsOnDetach : true,
                anchor : 'Right'
            });

            // $("#grid-container").removeClass("disabledbutton");
            // $("#tool-palette-container").removeClass("disabledbutton");
        };

        /**
         @function drop stream that is defined as the output stream in a query configuration
         * @param position   position of selected query
         * @param id    id of selected query
         * @param outStream     name for new output stream
         * @param streamAttributes      projections list for output stream
         */
        dropElements.prototype.dropStreamFromQuery = function (position , id, outStream, streamAttributes) {
            var elementID = i;
            var newAgent = $('<div>').attr('id', elementID).addClass('streamdrop');

            // The container and the toolbox are disabled to prevent the user from dropping any elements before initializing a Stream Element
            // $("#grid-container").removeClass("disabledbutton");
            // $("#tool-palette-container").removeClass("disabledbutton");
            $(this.container).append(newAgent);

            // drop the stream
            this.dropStream(newAgent, i, position.top, position.left + 200, outStream);

            // add the new out stream to the stream array
            var streamOptions = {};
            _.set(streamOptions, 'id', elementID);
            _.set(streamOptions, 'define', outStream);
            _.set(streamOptions, 'type', 'define-stream');
            _.set(streamOptions, 'attributes', streamAttributes);
            var stream = new Stream(streamOptions);
            this.app.AddStream(stream);
            // make the connection
            _jsPlumb.connect({
                source: id+'-out',
                target: elementID+'-in'
            });
            // update the query model with output stream
            var query = this.app.getQuery(id);
            query.setInsertInto(elementID);
            // increment the global variable i and the final element count
            finalElementCount = i; //tODO:check this variable
            i++;
            this.registerElementEventListeners(newAgent);
        };

        /**
         * @function drop the query element on the canvas
         * @param newAgent
         * @param i
         * @param droptype
         * @param top
         * @param left
         * @param text
         */
        dropElements.prototype.dropQuery = function (newAgent, i, droptype, top, left, text) {
            /*
             A text node division will be appended to the newAgent element so that the element name can be changed in
             the text node and doesn't need to be appended to the newAgent Element everytime theuser changes it
            */
            //TODO : check text node division. we might need to add a new div to handle this.
            var node = $('<div>' + text + '</div>');
            newAgent.append(node);
            node.attr('id', i+"-nodeInitial");
            node.attr('class', "queryNameNode");

            if( droptype === constants.PASS_THROUGH || droptype === constants.WINDOW_QUERY || droptype === constants.FILTER){
                //add the new query to the query array
                var queryOptions = {};
                _.set(queryOptions, 'id', '');
                _.set(queryOptions, 'name', '');
                _.set(queryOptions, 'from', '');
                _.set(queryOptions, 'insertInto', '');
                _.set(queryOptions, 'filter', '');
                _.set(queryOptions, 'postWindowFilter', '');
                _.set(queryOptions, 'window', '');
                _.set(queryOptions, 'outputType', '');
                _.set(queryOptions, 'projection', '');
                var newQuery = new Query(queryOptions);
                newQuery.setId(i);
                this.app.AddQuery(newQuery);
                var propertiesIcon = $('<img src="/editor/images/settings.png" class="element-prop-icon collapse" ' +
                    'onclick="this.formBuilder.GeneratePropertiesFormForQueries(this)">');
                newAgent.append(node).append('<img src="/editor/images/cancel.png" class="element-close-icon collapse">')
                    .append(propertiesIcon);
                this.dropSimpleQueryElement(newAgent, i, top, left);
            }

            else if(droptype === constants.JOIN)
            {
                //add the new join query to the join query array
                var joinQueryOptions = {};
                _.set(joinQueryOptions, 'id', '');
                _.set(joinQueryOptions, 'join', '');
                _.set(joinQueryOptions, 'projection', '');
                _.set(joinQueryOptions, 'outputType', '');
                _.set(joinQueryOptions, 'insertInto', '');
                _.set(joinQueryOptions, 'from', '');
                var newJoinQuery = new JoinQuery(joinQueryOptions);
                newJoinQuery.setId(i);
                this.app.AddJoinQuery(newJoinQuery);
                var propertiesIcon = $('<img src="/editor/images/settings.png" class="element-prop-icon collapse" ' +
                    'onclick="this.formBuilder.GeneratePropertiesFormForJoinQuery(this)">');
                newAgent.append(node).append('<img src="/editor/images/cancel.png" class="element-close-icon collapse">')
                    .append(propertiesIcon);
                this.dropCompleteJoinQueryElement(newAgent , i, top, left);
            }
            else if(droptype === constants.PATTERN)
            {
                //add the new join query to the join query array
                var patternQueryOptions = {};
                _.set(patternQueryOptions, 'id', '');
                _.set(patternQueryOptions, 'name', '');
                _.set(patternQueryOptions, 'states', '');
                _.set(patternQueryOptions, 'logic', '');
                _.set(patternQueryOptions, 'projection', '');
                _.set(patternQueryOptions, 'filter', '');
                _.set(patternQueryOptions, 'postWindowFilter', '');
                _.set(patternQueryOptions, 'window', '');
                _.set(patternQueryOptions, 'having', '');
                _.set(patternQueryOptions, 'groupBy', '');
                _.set(patternQueryOptions, 'outputType', '');
                _.set(patternQueryOptions, 'insertInto', '');
                _.set(patternQueryOptions, 'from', '');

                var newPattern = new PatternQuery(patternQueryOptions);
                newPattern.setId(i);
                this.app.AddPatternQuery(newPattern);
                var propertiesIcon = $('<img src="/editor/images/settings.png" class="element-prop-icon collapse" ' +
                    'onclick="this.formBuilder.GeneratePropertiesFormForPattern(this)">');
                newAgent.append(node).append('<img src="/editor/images/cancel.png" class="element-close-icon collapse">')
                    .append(propertiesIcon);
                this.dropPatternQueryElement(newAgent, i, top, left);
            }
        };

        /**
         * @function draw the simple query element ( passthrough, filter and window)
         * @param newAgent
         * @param i
         * @param top
         * @param left
         * @description allows single input stream and single output stream
         */
        dropElements.prototype.dropSimpleQueryElement = function (newAgent, i, top, left) {
            var finalElement =  newAgent;
            var connectionIn = $('<div class="connectorIn">').attr('id', i + '-in').addClass('connection');
            var connectionOut = $('<div class="connectorOut">').attr('id', i + '-out').addClass('connection');

            finalElement.css({
                'top': top,
                'left': left
            });

            finalElement.append(connectionIn);
            finalElement.append(connectionOut);

            $(this.container).append(finalElement);

            _jsPlumb.draggable(finalElement, {
                containment: 'grid-container'
            });

            _jsPlumb.makeTarget(connectionIn, {
                anchor: 'Left',
                maxConnections : 1,
                deleteEndpointsOnDetach:true
            });

            _jsPlumb.makeSource(connectionOut, {
                anchor: 'Right',
                uniqueEndpoint: true,
                maxConnections : 1,
                deleteEndpointsOnDetach:true
            });
        };

        /**
         * @function draw the pattern query element ( passthrough, filter and window)
         * @param newAgent
         * @param i
         * @param top
         * @param left
         * @description allows mulitple input streams and single output stream
         *
         */
        dropElements.prototype.dropPatternQueryElement = function (newAgent, i, top, left) {
            var finalElement =  newAgent;
            var connectionIn = $('<div class="connectorIn">').attr('id', i + '-in').addClass('connection');
            var connectionOut = $('<div class="connectorOut">').attr('id', i + '-out').addClass('connection');

            finalElement.css({
                'top': top,
                'left': left
            });

            finalElement.append(connectionIn);
            finalElement.append(connectionOut);

            $(this.container).append(finalElement);

            _jsPlumb.draggable(finalElement, {
                containment: 'grid-container'
            });

            _jsPlumb.makeTarget(connectionIn, {
                anchor: 'Left'
            });

            _jsPlumb.makeSource(connectionOut, {
                anchor: 'Right',
                maxConnections:1
            });
        };

        /**
         * @function draw the join query element on the canvas
         * @param newAgent
         * @param i
         * @param top
         * @param left
         */
        dropElements.prototype.dropCompleteJoinQueryElement = function (newAgent, i, top, left) {
            var finalElement =  newAgent;
            var connectionIn = $('<div class="connectorIn">').attr('id', i + '-in').addClass('connection');
            var connectionOut = $('<div class="connectorOut">').attr('id', i + '-out').addClass('connection');

            finalElement.css({
                'top': top,
                'left': left
            });

            finalElement.append(connectionIn);
            finalElement.append(connectionOut);

            $(this.container).append(finalElement);

            _jsPlumb.draggable(finalElement, {
                containment: 'grid-container'
            });

            _jsPlumb.makeTarget(connectionIn, {
                anchor: 'Left',
                maxConnections:2
            });

            _jsPlumb.makeSource(connectionOut, {
                anchor: 'Right',
                uniqueEndpoint: true,
                maxConnections: 1
            });
        };

        /**
         * @description draw the partition query on the canvas and add the event listeners for it
         * @param newAgent
         * @param i
         * @param mouseTop
         * @param mouseLeft
         */
        dropElements.prototype.dropPartition = function (newAgent, i, mouseTop, mouseLeft) {
            var finalElement =  newAgent;

            $(finalElement).draggable({
                containment: "grid-container",
                drag:function(){
                    _jsPlumb.repaintEverything();
                    // var connections = jsPlumb.getConnections(this);
                    // $.each( connections, function(index,connection){
                    //     jsPlumb.repaint(connection);
                    // });
                }
            });
            var x =1;
            $(finalElement).resizable();

            finalElement.css({
                'top': mouseTop,
                'left': mouseLeft
            });
            var connectionIn;
            $(finalElement).on('dblclick',function () {
                connectionIn = $('<div class="connectorInPart" >').attr('id', i + '-pc'+ x);
                finalElement.append(connectionIn);
                //
                _jsPlumb.makeTarget(connectionIn, {
                    anchor: 'Left',
                    maxConnections : 1
                });
                _jsPlumb.makeSource(connectionIn, {
                    anchor: 'Right'
                });

                x++;
                $(connectionIn).on('click', function(endpoint){
                    this.formBuilder.GeneratePartitionKeyForm(endpoint);
                });
            });

            $(this.container).append(finalElement);
            _jsPlumb.addGroup({
                el: $('#' + i),
                id: i,
                droppable:true,
                constrain:true,
                dropOverride:false,
                draggable:false
            });

            //add the new partition to the partition array
            var partitionOptions = {};
            _.set(partitionOptions, 'id', '');
            _.set(partitionOptions, 'partition', '');
            _.set(partitionOptions, 'queries', '');
            var newPartition = new Partition(partitionOptions);
            newPartition.setId(i);
            this.app.AddPartition(newPartition);
        };

        /**
         * @function Drop a window stream on the canvas
         * @param newAgent
         * @param i
         * @param topP
         * @param left
         * @param asName
         */
        // TODO: not updated (from earlier version)
        dropElements.prototype.dropWindowStream = function (newAgent, i, topP, left, asName) {
            /*
             The node hosts a text node where the Window's name, input by the user will be held.
             Rather than simply having a `newAgent.text(windowName)` statement, as the text function tends to
             reposition the other appended elements with the length of the Stream name input by the user.
            */

            //Initially the asName will be "Window" as the has not yet initialized the window

            var windowNode = $('<div>' + asName + '</div>');
            newAgent.append(windowNode);
            windowNode.attr('id', i+"-windowNode");
            windowNode.attr('class', "windowNameNode");

            var prop = $('<img src="/editor/images/settings.png" class="element-prop-icon collapse" onclick="">')
                .attr('id', (i+('-prop')));
            newAgent.append(windowNode).append('<img src="/editor/images/cancel.png" ' +
                'class="element-close-icon collapse">').append(prop);
            var finalElement =  newAgent;

            var connectionIn = $('<div class="connectorInWindow">').attr('id', i + '-in').addClass('connection');
            var connectionOut = $('<div class="connectorOutWindow">').attr('id', i + '-out').addClass('connection');

            finalElement.css({
                'top': topP,
                'left': left
            });

            finalElement.append(connectionIn);
            finalElement.append(connectionOut);

            $(this.container).append(finalElement);

            _jsPlumb.draggable(finalElement, {
                containment: 'parent'
            });

            _jsPlumb.makeTarget(connectionIn, {
                anchor: 'Continuous',
                maxConnections:1
            });

            _jsPlumb.makeSource(connectionOut, {
                anchor: 'Continuous'
            });
        };

        /**
         * @function Bind event listeners for the elements that are dropped.
         * @param newElement dropped element
         */
        dropElements.prototype.registerElementEventListeners = function (newElement) {
            //register event listener to show configuration icons when mouse is over the element
            newElement.on( "mouseenter", function() {
                var element = $(this);
                element.find('.element-prop-icon').show();
                element.find('.element-conn-icon').show();
                element.find('.element-close-icon').show();
            });

            //register event listener to hide configuration icons when mouse is out from the element
            newElement.on( "mouseleave", function() {
                var element = $(this);
                element.find('.element-prop-icon').hide();
                element.find('.element-conn-icon').hide();
                element.find('.element-close-icon').hide();
            });

            //register event listener to remove the element when the close icon is clicked
            newElement.on('click', '.element-close-icon', function () {
                if(_jsPlumb.getGroupFor(newElement)){
                    var queries = this.app.getPartition(_jsPlumb.getGroupFor(newElement)).getQueries();
                    var removedQueryIndex = null;
                    $.each( queries , function (index, query) {
                        if(query.getId() === $(newElement).attr('id')){
                            removedQueryIndex = index;
                        }
                    });
                    queries.splice(removedQueryIndex,1);
                    this.app.getPartition(_jsPlumb.getGroupFor(newElement)).setQueries(queries);
                    _jsPlumb.remove(newElement);
                    _jsPlumb.removeFromGroup(newElement);
                }
                else{
                    _jsPlumb.remove(newElement);
                }
            });
        };


        return dropElements;
    });
