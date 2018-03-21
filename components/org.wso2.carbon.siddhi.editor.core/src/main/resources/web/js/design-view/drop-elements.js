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
    'patternQuery', 'query', 'stream', 'windowQuery', 'formBuilder'],
    function (require, log, _, $, _jsPlumb, FilterQuery, JoinQuery, Partition, PassThroughQuery, PatternQuery, Query,
              Stream, WindowQuery, FormBuilder) {

        var constants = {
            STREAM: 'streamdrop',
            PASS_THROUGH : 'squerydrop',
            FILTER : 'filterdrop',
            JOIN : 'joquerydrop',
            WINDOW_QUERY : 'wquerydrop',
            PATTERN : 'stquerydrop',
            WINDOW_STREAM :'', //TODO: implement this
            PARTITION :'partitiondrop'
        };

        /**
         * @class DesignView
         * @constructor
         * @class DesignView  Wraps the Ace editor for design view
         * @param {Object} options Rendering options for the view
         */
        var DropElements = function (options) {
            var errorMessage = 'unable to find design view container';
            if (!_.has(options, 'container')) {
                log.error(errorMessage);
            }
            var container = $(_.get(options, 'container'));
            if (!container.length > 0) {
                log.error(errorMessage);
            }
            this.appData = options.appData;
            this.container = options.container;
            this.application = options.application;
            this.designGrid = options.designGrid;
            this.options = options;

            var formBuilderOptions = {};
            _.set(formBuilderOptions, 'application', this.application);
            _.set(formBuilderOptions, 'appData', this.appData);
            _.set(formBuilderOptions, 'dropElements', this);
            this.formBuilder = new FormBuilder(formBuilderOptions);
        };

        /**
         * @function drop the stream element on the canvas
         * @param newAgent new element
         * @param i id of the element
         * @param top top position of the element
         * @param left left position of the element
         * @param isCodeToDesignMode whether code to design mode is enable or not
         * @param isGenerateStreamFromQueryOutput whether this element is generated as a ourpur stream in a query
         * @param streamName name of the stream
         */
        DropElements.prototype.dropStream = function (newAgent, i, top, left, isCodeToDesignMode,
                                                      isGenerateStreamFromQueryOutput, streamName) {
            /*
             The node hosts a text node where the Stream's name input by the user will be held.
             Rather than simply having a `newAgent.text(streamName)` statement, as the text function tends to
             reposition the other appended elements with the length of the Stream name input by the user.
            */

            var self= this;
            var name;
            if(isCodeToDesignMode) {
                name = streamName;
            } else {
                if(isGenerateStreamFromQueryOutput) {
                    name = streamName;
                } else {
                    name = self.formBuilder.DefineStream(i);
                }
            }
            var node = $('<div>' + name + '</div>');
            newAgent.append(node);
            node.attr('id', i+"-nodeInitial");
            node.attr('class', "streamNameNode");

            /*
             prop --> When clicked on this icon, a definition and related information of the Stream Element will
             be displayed as an alert message
             showIcon --> An icon that elucidates whether the dropped stream element is an Import/Export/Defined
             stream (In this case: an Import arrow icon)
             conIcon --> Clicking this icon is supposed to toggle between showing and hiding the
             "Connection Anchor Points" (Not implemented)//TODO: implement this feature
            */
            var settingsIconId = ""+ i + "-dropStreamSettingsId";
            var prop = $('<img src="/editor/images/settings.png" id="'+ settingsIconId +'" ' +
                'class="element-prop-icon collapse">');
            newAgent.append(node).append('<img src="/editor/images/cancel.png" ' +
                'class="element-close-icon collapse">').append(prop);

            var settingsIconElement = $('#'+settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForStreams(this);
            });

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

            $(self.container).append(finalElement);

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
        };

        /**
         @function drop stream that is defined as the output stream in a query configuration
         * @param position  position of selected query
         * @param id  id of selected query
         * @param outStream  name for new output stream
         * @param streamAttributes  projections list for output stream
         */
        DropElements.prototype.dropStreamFromQuery = function (position , id, outStream, streamAttributes) {
            var self = this;
            var elementID = self.designGrid.getNewAgentId();
            var newAgent = $('<div>').attr('id', elementID).addClass('streamdrop');

            // The container and the tool palette are disabled to prevent the user from dropping any elements
            // before initializing a Stream Element
            $("#grid-container").removeClass("disabledbutton");
            $("#tool-palette-container").removeClass("disabledbutton");
            $(self.container).append(newAgent);

            // drop the stream
            self.dropStream(newAgent, elementID, position.top, position.left + 200, false, true, outStream);

            // add the new out stream to the stream array
            var streamOptions = {};
            _.set(streamOptions, 'id', elementID);
            _.set(streamOptions, 'define', outStream);
            _.set(streamOptions, 'type', 'define-stream');
            _.set(streamOptions, 'attributes', streamAttributes);

            var stream = new Stream(streamOptions);
            self.appData.AddStream(stream);
            // make the connection
            _jsPlumb.connect({
                source: id+'-out',
                target: elementID+'-in'
            });
            // update the query model with output stream
            var query = self.appData.getQuery(id);
            query.setInsertInto(elementID);
            // increment the variable newAgentId and the final element count
            self.appData.setFinalElementCount(self.appData.getFinalElementCount() + 1);
            self.designGrid.generateNextId();
            self.registerElementEventListeners(newAgent);
        };

        /**
         * @function drop the query element on the canvas
         * @param newAgent new element
         * @param i id of the element
         * @param droptype type of the query
         * @param top top position of the element
         * @param left left position of the element
         * @param text text to be displayed on the element
         * @param isCodeToDesignMode whether code to design mode is enable or not
         */
        DropElements.prototype.dropQuery = function (newAgent, i, droptype, top, left, text, isCodeToDesignMode) {
            /*
             A text node division will be appended to the newAgent element so that the element name can be changed in
             the text node and doesn't need to be appended to the newAgent Element everytime theuser changes it
            */
            var self= this;
            //TODO : check text node division. we might need to add a new div to handle this.
            var node = $('<div>' + text + '</div>');
            newAgent.append(node);
            node.attr('id', i+"-nodeInitial");
            node.attr('class', "queryNameNode");

            if(droptype === constants.PASS_THROUGH || droptype === constants.WINDOW_QUERY || droptype === constants.FILTER){
                if(!isCodeToDesignMode) {
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
                    self.appData.AddQuery(newQuery);
                }
                var settingsIconId = ""+ i + "-dropQuerySettingsId";
                var propertiesIcon = $('<img src="/editor/images/settings.png" id="'+ settingsIconId +'" ' +
                    'class="element-prop-icon collapse">');
                newAgent.append(node).append('<img src="/editor/images/cancel.png" class="element-close-icon collapse">')
                    .append(propertiesIcon);
                self.dropSimpleQueryElement(newAgent, i, top, left);
                var settingsIconElement = $('#'+settingsIconId)[0];
                settingsIconElement.addEventListener('click', function () {
                    self.formBuilder.GeneratePropertiesFormForQueries(this);
                });
            }

            else if(droptype === constants.JOIN) {
                if(!isCodeToDesignMode) {
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
                    self.appData.AddJoinQuery(newJoinQuery);
                }
                var settingsIconId = ""+ i + "-dropJoinQuerySettingsId";
                var propertiesIcon = $('<img src="/editor/images/settings.png" id="'+ settingsIconId +'" ' +
                    'class="element-prop-icon collapse">');
                newAgent.append(node).append('<img src="/editor/images/cancel.png" class="element-close-icon collapse">')
                    .append(propertiesIcon);
                self.dropCompleteJoinQueryElement(newAgent , i, top, left);

                var settingsIconElement = $('#'+settingsIconId)[0];
                settingsIconElement.addEventListener('click', function () {
                    self.formBuilder.GeneratePropertiesFormForJoinQuery(this);
                });
            }
            else if(droptype === constants.PATTERN) {
                if(!isCodeToDesignMode) {
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
                    self.appData.AddPatternQuery(newPattern);
                }
                var settingsIconId = ""+ i + "-dropPatternQuerySettingsId";
                var propertiesIcon = $('<img src="/editor/images/settings.png" id="'+ settingsIconId +'" ' +
                    'class="element-prop-icon collapse">');
                newAgent.append(node).append('<img src="/editor/images/cancel.png" class="element-close-icon collapse">')
                    .append(propertiesIcon);

                self.dropPatternQueryElement(newAgent, i, top, left);

                var settingsIconElement = $('#'+settingsIconId)[0];
                settingsIconElement.addEventListener('click', function () {
                    self.formBuilder.GeneratePropertiesFormForPattern(this);
                });
            }
        };

        /**
         * @function draw the simple query element ( passthrough, filter and window)
         * @param newAgent new element
         * @param i id of the element
         * @param top top position of the element
         * @param left left position of the element
         * @description allows single input stream and single output stream
         */
        DropElements.prototype.dropSimpleQueryElement = function (newAgent, i, top, left) {
            var self = this;
            var finalElement =  newAgent;
            var connectionIn = $('<div class="connectorIn">').attr('id', i + '-in').addClass('connection');
            var connectionOut = $('<div class="connectorOut">').attr('id', i + '-out').addClass('connection');

            finalElement.css({
                'top': top,
                'left': left
            });

            finalElement.append(connectionIn);
            finalElement.append(connectionOut);

            $(self.container).append(finalElement);

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
         * @param newAgent new element
         * @param i id of the element
         * @param top top position of the element
         * @param left left position of the element
         * @description allows multiple input streams and single output stream
         *
         */
        DropElements.prototype.dropPatternQueryElement = function (newAgent, i, top, left) {
            var self = this;
            var finalElement =  newAgent;
            var connectionIn = $('<div class="connectorIn">').attr('id', i + '-in').addClass('connection');
            var connectionOut = $('<div class="connectorOut">').attr('id', i + '-out').addClass('connection');

            finalElement.css({
                'top': top,
                'left': left
            });

            finalElement.append(connectionIn);
            finalElement.append(connectionOut);

            $(self.container).append(finalElement);

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
         * @param newAgent new element
         * @param i id of the element
         * @param top top position of the element
         * @param left left position of the element
         */
        DropElements.prototype.dropCompleteJoinQueryElement = function (newAgent, i, top, left) {
            var self = this;
            var finalElement =  newAgent;
            var connectionIn = $('<div class="connectorIn">').attr('id', i + '-in').addClass('connection');
            var connectionOut = $('<div class="connectorOut">').attr('id', i + '-out').addClass('connection');

            finalElement.css({
                'top': top,
                'left': left
            });

            finalElement.append(connectionIn);
            finalElement.append(connectionOut);

            $(self.container).append(finalElement);

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
         * @function draw the partition query on the canvas and add the event listeners for it
         * @param newAgent new element
         * @param i id of the element
         * @param mouseTop top position of the element
         * @param mouseLeft left position of the element
         * @param isCodeToDesignMode whether code to design mode is enable or not
         */
        DropElements.prototype.dropPartition = function (newAgent, i, mouseTop, mouseLeft, isCodeToDesignMode) {
            var self = this;
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
                    self.formBuilder.GeneratePartitionKeyForm(endpoint);
                });
            });

            $(self.container).append(finalElement);
            _jsPlumb.addGroup({
                el: $('#' + i)[0],
                id: i,
                droppable:true,
                constrain:true,
                dropOverride:false,
                draggable:false
            });

            if(!isCodeToDesignMode) {
                //add the new partition to the partition array
                var partitionOptions = {};
                _.set(partitionOptions, 'id', '');
                _.set(partitionOptions, 'partition', {
                    // this will contain json objects { stream : '', property :''}
                    "with" :[]
                });
                _.set(partitionOptions, 'queries', []);
                var newPartition = new Partition(partitionOptions);
                newPartition.setId(i);
                self.appData.AddPartition(newPartition);
            }
        };

        /**
         * @function Drop a window stream on the canvas
         * @param newAgent new element
         * @param i id of the element
         * @param topP top position of the element
         * @param left left position of the element
         * @param asName name of the window stream
         */
        // TODO: not updated (from earlier version)
        DropElements.prototype.dropWindowStream = function (newAgent, i, topP, left, asName) {
            var self= this;
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
//TODO: onclick
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

            $(self.container).append(finalElement);

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
        DropElements.prototype.registerElementEventListeners = function (newElement) {
            var self = this;
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
                var elementId = newElement[0].id;

                if (newElement.hasClass('streamdrop')) {
                    self.appData.streamList.removeElement(elementId);

                } else if (newElement.hasClass('squerydrop')) {
                    self.appData.queryList.removeElement(elementId);
                    self.appData.passThroughList.removeElement(elementId);

                } else if (newElement.hasClass('filterdrop')) {
                    self.appData.queryList.removeElement(elementId);
                    self.appData.filterList.removeElement(elementId);

                } else if (newElement.hasClass('joquerydrop')) {
                    self.appData.queryList.removeElement(elementId);
                    self.appData.joinQueryList.removeElement(elementId);

                } else if (newElement.hasClass('wquerydrop')) {
                    self.appData.queryList.removeElement(elementId);
                    self.appData.windowQueryList.removeElement(elementId);

                } else if (newElement.hasClass('stquerydrop')) {
                    self.appData.queryList.removeElement(elementId);
                    self.appData.patternList.removeElement(elementId);

                } else if (newElement.hasClass('partitiondrop')) {
                    self.appData.partitionList.removeElement(elementId);

                } else if (newElement.hasClass('windowStream')) {
                    //TODO: implement this
                }
                if(_jsPlumb.getGroupFor(newElement)){
                    var queries = self.appData.getPartition(_jsPlumb.getGroupFor(newElement).id).getQueries();
                    var removedQueryIndex = null;
                    $.each( queries , function (index, query) {
                        if(query.getId() === $(newElement).attr('id')){
                            removedQueryIndex = index;
                        }
                    });
                    queries.splice(removedQueryIndex,1);
                    self.appData.getPartition(_jsPlumb.getGroupFor(newElement).id).setQueries(queries);
                    _jsPlumb.remove(newElement);
                    _jsPlumb.removeFromGroup(newElement);
                } else {
                    _jsPlumb.remove(newElement);
                }
                self.appData.setFinalElementCount(self.appData.getFinalElementCount() - 1);
            });
        };

        return DropElements;
    });
