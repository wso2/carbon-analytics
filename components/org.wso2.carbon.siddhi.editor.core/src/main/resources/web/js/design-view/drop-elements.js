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

define(['require', 'log', 'lodash', 'jquery', 'jsplumb', 'partition', 'stream', 'query', 'formBuilder'],
    function (require, log, _, $, _jsPlumb, Partition, Stream, Query, FormBuilder) {

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
         * @param isGenerateStreamFromQueryOutput whether this element is generated as a output stream in a query
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
         * @param queryModel Query data holding object
         * @param position  position of selected query
         * @param id  id of selected query
         * @param outStream  name for new output stream
         * @param streamAttributes  projections list for output stream
         */
        DropElements.prototype.dropStreamFromQuery = function (queryModel, position , queryId, outStream, streamAttributes) {
            var self = this;
            var isStreamNameUsed = false;
            var elementID;
            _.forEach(self.appData.streamList, function(stream){
                if(stream.getName().toUpperCase() === outStream.toUpperCase()) {
                    isStreamNameUsed = true;
                    elementID = stream.getId();
                }
            });
            if(!isStreamNameUsed) {
                elementID = self.designGrid.getNewAgentId();
                var newAgent = $('<div>').attr('id', elementID).addClass('streamdrop');
                $(self.container).append(newAgent);

                // drop the stream
                self.dropStream(newAgent, elementID, position.top, position.left + 200, false, true, outStream);

                // add the new out stream to the stream array
                var streamOptions = {};
                _.set(streamOptions, 'id', elementID);
                _.set(streamOptions, 'name', outStream);
                _.set(streamOptions, 'isInnerStream', false);
                var stream = new Stream(streamOptions);
                _.forEach(streamAttributes, function (attribute) {
                    stream.addAttribute(attribute);
                });
                self.appData.addStream(stream);

                // increment the variable final element count
                self.appData.setFinalElementCount(self.appData.getFinalElementCount() + 1);
                self.registerElementEventListeners(newAgent);
            }
            // The container and the tool palette are disabled to prevent the user from dropping any elements
            // before initializing a Stream Element
            $("#grid-container").removeClass("disabledbutton");
            $("#tool-palette-container").removeClass("disabledbutton");

            // make the connection
            _jsPlumb.connect({
                source: queryId+'-out',
                target: elementID+'-in'
            });
            // update the query model with output stream
            queryModel.getQueryOutput().setTarget(outStream);
        };

        /**
         * @function drop the table element on the canvas
         * @param newAgent new element
         * @param i id of the element
         * @param top top position of the element
         * @param left left position of the element
         * @param isCodeToDesignMode whether code to design mode is enable or not
         * @param tableName name of the table
         */
        DropElements.prototype.dropTable = function (newAgent, i, top, left, isCodeToDesignMode, tableName) {
            /*
             The node hosts a text node where the Table's name input by the user will be held.
             Rather than simply having a `newAgent.text(tableName)` statement, as the text function tends to
             reposition the other appended elements with the length of the Table name input by the user.
            */

            var self= this;
            var name;
            if(isCodeToDesignMode) {
                name = tableName;
            } else {
               name = self.formBuilder.DefineTable(i);
            }
            var node = $('<div>' + name + '</div>');
            newAgent.append(node);
            node.attr('id', i+"-nodeInitial");
            node.attr('class', "tableNameNode");

            /*
             prop --> When clicked on this icon, a definition and related information of the Table Element will
             be displayed as an alert message
            */
            var settingsIconId = ""+ i + "-dropTableSettingsId";
            var prop = $('<img src="/editor/images/settings.png" id="'+ settingsIconId +'" ' +
                'class="element-prop-icon collapse">');
            newAgent.append(node).append('<img src="/editor/images/cancel.png" ' +
                'class="element-close-icon collapse">').append(prop);

            var settingsIconElement = $('#'+settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForTables(this);
            });

            var finalElement = newAgent;

            /*
             connection --> The connection anchor point is appended to the element
             */
            var connection1 = $('<div class="connectorInTable">').attr('id', i+"-in" ).addClass('connection');
            var connection2 = $('<div class="connectorOutTable">').attr('id', i+"-out" ).addClass('connection');


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
         * @function drop the window element on the canvas
         * @param newAgent new element
         * @param i id of the element
         * @param top top position of the element
         * @param left left position of the element
         * @param isCodeToDesignMode whether code to design mode is enable or not
         * @param windowName name of the table
         */
        DropElements.prototype.dropWindow = function (newAgent, i, top, left, isCodeToDesignMode, windowName) {
            /*
             The node hosts a text node where the Window's name input by the user will be held.
             Rather than simply having a `newAgent.text(windowName)` statement, as the text function tends to
             reposition the other appended elements with the length of the Window name input by the user.
            */

            var self= this;
            var name;
            if(isCodeToDesignMode) {
                name = windowName;
            } else {
                name = self.formBuilder.DefineWindow(i);
            }
            var node = $('<div>' + name + '</div>');
            newAgent.append(node);
            node.attr('id', i+"-nodeInitial");
            node.attr('class', "windowNameNode");

            /*
             prop --> When clicked on this icon, a definition and related information of the Window Element will
             be displayed as an alert message
            */
            var settingsIconId = ""+ i + "-dropWindowSettingsId";
            var prop = $('<img src="/editor/images/settings.png" id="'+ settingsIconId +'" ' +
                'class="element-prop-icon collapse">');
            newAgent.append(node).append('<img src="/editor/images/cancel.png" ' +
                'class="element-close-icon collapse">').append(prop);

            var settingsIconElement = $('#'+settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForWindows(this);
            });

            var finalElement = newAgent;

            /*
             connection --> The connection anchor point is appended to the element
             */
            var connection1 = $('<div class="connectorInWindow">').attr('id', i+"-in" ).addClass('connection');
            var connection2 = $('<div class="connectorOutWindow">').attr('id', i+"-out" ).addClass('connection');


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
         * @function drop the trigger element on the canvas
         * @param newAgent new element
         * @param i id of the element
         * @param top top position of the element
         * @param left left position of the element
         * @param isCodeToDesignMode whether code to design mode is enable or not
         * @param triggerName name of the trigger
         */
        DropElements.prototype.dropTrigger = function (newAgent, i, top, left, isCodeToDesignMode, triggerName) {
            /*
             The node hosts a text node where the Trigger's name input by the user will be held.
             Rather than simply having a `newAgent.text(triggerName)` statement, as the text function tends to
             reposition the other appended elements with the length of the Trigger name input by the user.
            */

            var self= this;
            var name;
            if(isCodeToDesignMode) {
                name = triggerName;
            } else {
                name = self.formBuilder.DefineTrigger(i);
            }
            var node = $('<div>' + name + '</div>');
            newAgent.append(node);
            node.attr('id', i+"-nodeInitial");
            node.attr('class', "triggerNameNode");

            /*
             prop --> When clicked on this icon, a definition and related information of the Trigger Element will
             be displayed as an alert message
            */
            var settingsIconId = ""+ i + "-dropTriggerSettingsId";
            var prop = $('<img src="/editor/images/settings.png" id="'+ settingsIconId +'" ' +
                'class="element-prop-icon collapse">');
            newAgent.append(node).append('<img src="/editor/images/cancel.png" ' +
                'class="element-close-icon collapse">').append(prop);

            var settingsIconElement = $('#'+settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForTriggers(this);
            });

            var finalElement = newAgent;

            /*
             connection --> The connection anchor point is appended to the element
             */
            var connection1 = $('<div class="connectorInTrigger">').attr('id', i+"-in" ).addClass('connection');
            var connection2 = $('<div class="connectorOutTrigger">').attr('id', i+"-out" ).addClass('connection');


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
         * @function drop the aggregation element on the canvas
         * @param newAgent new element
         * @param i id of the element
         * @param top top position of the element
         * @param left left position of the element
         * @param isCodeToDesignMode whether code to design mode is enable or not
         * @param aggregationName name of the aggregation
         */
        DropElements.prototype.dropAggregation = function (newAgent, i, top, left, isCodeToDesignMode, aggregationName) {
            /*
             The node hosts a text node where the Aggregation's name input by the user will be held.
             Rather than simply having a `newAgent.text(aggregationName)` statement, as the text function tends to
             reposition the other appended elements with the length of the Aggregation name input by the user.
            */

            var self= this;
            var name;
            if(isCodeToDesignMode) {
                name = aggregationName;
            } else {
                name = self.formBuilder.DefineAggregation(i);
            }
            var node = $('<div>' + name + '</div>');
            newAgent.append(node);
            node.attr('id', i+"-nodeInitial");
            node.attr('class', "aggregationNameNode");

            /*
             prop --> When clicked on this icon, a definition and related information of the Aggregation Element will
             be displayed as an alert message
            */
            var settingsIconId = ""+ i + "-dropAggregationSettingsId";
            var prop = $('<img src="/editor/images/settings.png" id="'+ settingsIconId +'" ' +
                'class="element-prop-icon collapse">');
            newAgent.append(node).append('<img src="/editor/images/cancel.png" ' +
                'class="element-close-icon collapse">').append(prop);

            var settingsIconElement = $('#'+settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForAggregations(this);
            });

            var finalElement = newAgent;

            /*
             connection --> The connection anchor point is appended to the element
             */
            var connection1 = $('<div class="connectorInAggregation">').attr('id', i+"-in" ).addClass('connection');
            var connection2 = $('<div class="connectorOutAggregation">').attr('id', i+"-out" ).addClass('connection');


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
         * @function drop the query element on the canvas
         * @param newAgent new element
         * @param i id of the element
         * @param dropType type of the query
         * @param top top position of the element
         * @param left left position of the element
         * @param text text to be displayed on the element
         * @param isCodeToDesignMode whether code to design mode is enable or not
         */
        DropElements.prototype.dropWindowFilterProjectionQuery = function (newAgent, i, dropType, top, left, text,
                                                                           isCodeToDesignMode) {
            /*
             A text node division will be appended to the newAgent element so that the element name can be changed in
             the text node and doesn't need to be appended to the newAgent Element every time the user changes it
            */
            var self = this;
            //TODO : check text node division. we might need to add a new div to handle this.
            var node = $('<div>' + text + '</div>');
            newAgent.append(node);
            node.attr('id', i + "-nodeInitial");
            node.attr('class', "queryNameNode");
            if (!isCodeToDesignMode) {
                //add the new join query to the join query array
                var queryOptions = {};
                _.set(queryOptions, 'id', i);
                _.set(queryOptions, 'queryInput', '');
                _.set(queryOptions, 'select', '');
                _.set(queryOptions, 'groupBy', '');
                _.set(queryOptions, 'having', '');
                _.set(queryOptions, 'outputRateLimit', '');
                _.set(queryOptions, 'queryOutput', '');
                var query = new Query(queryOptions);
                self.appData.addWindowFilterProjectionQuery(query);
            }
            var settingsIconId = "" + i + "-dropQuerySettingsId";
            var propertiesIcon = $('<img src="/editor/images/settings.png" id="' + settingsIconId + '" ' +
                'class="element-prop-icon collapse">');
            newAgent.append(node).append('<img src="/editor/images/cancel.png" class="element-close-icon collapse">')
                .append(propertiesIcon);

            var settingsIconElement = $('#' + settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForWindowFilterProjectionQueries(this);
            });

            var finalElement = newAgent;
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
                maxConnections: 1,
                deleteEndpointsOnDetach: true
            });

            _jsPlumb.makeSource(connectionOut, {
                anchor: 'Right',
                uniqueEndpoint: true,
                maxConnections: 1,
                deleteEndpointsOnDetach: true
            });
        };

        /**
         * @function drop the patternQuery element on the canvas
         * @param newAgent new element
         * @param i id of the element
         * @param top top position of the element
         * @param left left position of the element
         * @param isCodeToDesignMode whether code to design mode is enable or not
         * @param patternQueryName name of the patternQuery
         */
        DropElements.prototype.dropPatternQuery = function (newAgent, i, top, left, isCodeToDesignMode, patternQueryName) {

            /*
             A text node division will be appended to the newAgent element so that the element name can be changed in
             the text node and doesn't need to be appended to the newAgent Element every time the user changes it
            */
            var self= this;
            var node = $('<div>' + patternQueryName + '</div>');
            newAgent.append(node);
            node.attr('id', i+"-nodeInitial");
            node.attr('class', "patternQueryNameNode");

            if(!isCodeToDesignMode) {
                //add the new join query to the join query array
                var patternQueryOptions = {};
                _.set(patternQueryOptions, 'id', i);
                _.set(patternQueryOptions, 'queryInput', '');
                _.set(patternQueryOptions, 'select', '');
                _.set(patternQueryOptions, 'groupBy', '');
                _.set(patternQueryOptions, 'having', '');
                _.set(patternQueryOptions, 'outputRateLimit', '');
                _.set(patternQueryOptions, 'queryOutput', '');

                var patternQuery = new Query(patternQueryOptions);
                self.appData.addPatternQuery(patternQuery);
            }

            /*
             prop --> When clicked on this icon, a definition and related information of the PatternQuery Element will
             be displayed as an alert message
            */
            var settingsIconId = ""+ i + "-dropPatternQuerySettingsId";
            var prop = $('<img src="/editor/images/settings.png" id="'+ settingsIconId +'" ' +
                'class="element-prop-icon collapse">');
            newAgent.append(node).append('<img src="/editor/images/cancel.png" ' +
                'class="element-close-icon collapse">').append(prop);

            var settingsIconElement = $('#'+settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForPatternQueries(this);
            });

            var finalElement = newAgent;

            /*
             connection --> The connection anchor point is appended to the element
             */
            var connection1 = $('<div class="connectorInPatternQuery">').attr('id', i+"-in" ).addClass('connection');
            var connection2 = $('<div class="connectorOutPatternQuery">').attr('id', i+"-out" ).addClass('connection');


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
                anchor: 'Left'
            });

            _jsPlumb.makeSource(connection2, {
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
            // var self = this;
            // /*
            //  A text node division will be appended to the newAgent element so that the element name can be changed in
            //  the text node and doesn't need to be appended to the newAgent Element every time the user changes it
            // */
            // var self = this;
            // var node = $('<div>' + text + '</div>');
            // newAgent.append(node);
            // node.attr('id', i + "-nodeInitial");
            // node.attr('class', "queryNameNode");
            //if(!isCodeToDesignMode) {
            //         //add the new join query to the join query array
            //         var joinQueryOptions = {};
            //         _.set(joinQueryOptions, 'id', '');
            //         _.set(joinQueryOptions, 'join', '');
            //         _.set(joinQueryOptions, 'projection', '');
            //         _.set(joinQueryOptions, 'outputType', '');
            //         _.set(joinQueryOptions, 'insertInto', '');
            //         _.set(joinQueryOptions, 'from', '');
            //
            //         var newJoinQuery = new JoinQuery(joinQueryOptions);
            //         newJoinQuery.setId(i);
            //         self.appData.addJoinQuery(newJoinQuery);
            //     }
            //     var settingsIconId = ""+ i + "-dropJoinQuerySettingsId";
            //     var propertiesIcon = $('<img src="/editor/images/settings.png" id="'+ settingsIconId +'" ' +
            //         'class="element-prop-icon collapse">');
            //     newAgent.append(node).append('<img src="/editor/images/cancel.png" class="element-close-icon collapse">')
            //         .append(propertiesIcon);
            //     self.dropCompleteJoinQueryElement(newAgent , i, top, left);
            //
            //     var settingsIconElement = $('#'+settingsIconId)[0];
            //     settingsIconElement.addEventListener('click', function () {
            //         self.formBuilder.GeneratePropertiesFormForJoinQuery(this);
            //     });
            // var finalElement =  newAgent;
            // var connectionIn = $('<div class="connectorIn">').attr('id', i + '-in').addClass('connection');
            // var connectionOut = $('<div class="connectorOut">').attr('id', i + '-out').addClass('connection');
            //
            // finalElement.css({
            //     'top': top,
            //     'left': left
            // });
            //
            // finalElement.append(connectionIn);
            // finalElement.append(connectionOut);
            //
            // $(self.container).append(finalElement);
            //
            // _jsPlumb.draggable(finalElement, {
            //     containment: 'grid-container'
            // });
            //
            // _jsPlumb.makeTarget(connectionIn, {
            //     anchor: 'Left',
            //     maxConnections:2
            // });
            //
            // _jsPlumb.makeSource(connectionOut, {
            //     anchor: 'Right',
            //     uniqueEndpoint: true,
            //     maxConnections: 1
            // });
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
                self.appData.addPartition(newPartition);
            }
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
                    self.appData.removeStream(elementId);

                } else if (newElement.hasClass('tabledrop')) {
                    self.appData.removeTable(elementId);

                } else if (newElement.hasClass('windowdrop')) {
                    self.appData.removeWindow(elementId);

                } else if (newElement.hasClass('triggerdrop')) {
                    self.appData.removeTrigger(elementId);

                } else if (newElement.hasClass('aggregationdrop')) {
                    self.appData.removeAggregation(elementId);

                } else if (newElement.hasClass('projectionQueryDrop')) {
                    self.appData.removeWindowFilterProjectionQuery(elementId);

                } else if (newElement.hasClass('filterQueryDrop')) {
                    self.appData.removeWindowFilterProjectionQuery(elementId);

                } else if (newElement.hasClass('windowQueryDrop')) {
                    self.appData.removeWindowFilterProjectionQuery(elementId);

                } else if (newElement.hasClass('patternQueryDrop')) {
                    self.appData.removePatternQuery(elementId);

                } else if (newElement.hasClass('joquerydrop')) {
                    self.appData.removeJoinQuery(elementId);

                } else if (newElement.hasClass('partitiondrop')) {
                    self.appData.removePartition(elementId);

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
