/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'log', 'lodash', 'jquery', 'partition', 'stream', 'query', 'formBuilder', 'aggregation',
        'jsonValidator', 'sourceOrSinkAnnotation', 'stream', 'table', 'window', 'trigger', 'functionDefinition',
        'constants', 'attribute'],
    function (require, log, _, $, Partition, Stream, Query, FormBuilder, Aggregation, JSONValidator,
              SourceOrSinkAnnotation, Stream, Table, Window, Trigger, FunctionDefinition,Constants,Attribute) {

        /**
         * @class DesignView
         * @constructor
         * @class DesignView  Wraps the Ace editor for design view
         * @param {Object} options Rendering options for the view
         */

        const ENTER_KEY = 13;
        const DELETE_KEY = 46;
        const MAC_COMMAND_KEY = 91;
        const MAC_DELETE_KEY = 8;
        const LEFT_ARROW_KEY = 37;
        const RIGHT_ARROW_KEY = 39;
        const ESCAPE_KEY = 27;
        const TAB_KEY = 9;

        var DropElements = function (options) {
            var errorMessage = 'unable to find design view container';
            if (!_.has(options, 'container')) {
                log.error(errorMessage);
                throw errorMessage;
            }
            var container = $(_.get(options, 'container'));
            if (!container.length > 0) {
                log.error(errorMessage);
                throw errorMessage;
            }
            this.configurationData = options.configurationData;
            this.container = options.container;
            this.application = options.application;
            this.designGrid = options.designGrid;
            this.jsPlumbInstance = options.jsPlumbInstance;
            var currentTabId = this.application.tabController.activeTab.cid;
            this.designViewContainer = $('#design-container-' + currentTabId);
            this.toggleViewButton = $('#toggle-view-button-' + currentTabId);

            var formBuilderOptions = {};
            _.set(formBuilderOptions, 'application', this.application);
            _.set(formBuilderOptions, 'configurationData', this.configurationData);
            _.set(formBuilderOptions, 'jsPlumbInstance', this.jsPlumbInstance);
            _.set(formBuilderOptions, 'dropElementInstance', this);
            _.set(formBuilderOptions, 'designGrid', this.designGrid);
            this.formBuilder = new FormBuilder(formBuilderOptions);
        };

        /**
         * @function drop the source element on the canvas
         * @param newAgent new element
         * @param i id of the element
         * @param top top position of the element
         * @param left left position of the element
         * @param isCodeToDesignMode whether code to design mode is enable or not
         * @param sourceName name of the source
         */
        DropElements.prototype.dropSource = function (newAgent, i, top, left, isCodeToDesignMode, sourceName) {

            var self = this;
            var name;
            if (isCodeToDesignMode) {
                name = sourceName;
            } else {
                var sourceOptions = {};
                _.set(sourceOptions, 'id', i);
                _.set(sourceOptions, 'annotationType', 'SOURCE');
                _.set(sourceOptions, 'type', undefined);
                var source = new SourceOrSinkAnnotation(sourceOptions);
                self.configurationData.getSiddhiAppConfig().addSource(source);

                JSONValidator.prototype.validateSourceOrSinkAnnotation(source, 'Source', true)
            }
            var node = $('<div> ' + name + ' </div>');
            newAgent.append(node);
            node.attr('id', i + "-nodeInitial");
            node.attr('class', "sourceNameNode");

            var nodeId = $('#' + i)[0];
            nodeId.addEventListener('dblclick', function () {
                self.formBuilder.GeneratePropertiesFormForSources(this.children)
            });

            $('#' + i).on('keydown', function (key) {
                if (key.keyCode == ENTER_KEY) {
                    self.formBuilder.GeneratePropertiesFormForSources(this.children)
                }
            });
            /*
             prop --> When clicked on this icon, a definition and related information of the Source Element will
             be displayed in a form
            */
            var settingsIconId = "" + i + "-dropSourceSettingsId";
            var prop = $('<i id="' + settingsIconId + '" ' +
                'class="fw fw-settings element-prop-icon collapse"></i>');
            newAgent.append(node).append(
                '<i class="fw fw-delete element-close-icon collapse" data-toggle="popover"></i>').append(prop);

            var settingsIconElement = $('#' + settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForSources(this);
            });

            var finalElement = newAgent;

            /*
             connection --> The connection anchor point is appended to the element
             */
            self.generateSpecificSourceConnectionElements(name,self.jsPlumbInstance, i, finalElement);
            var connection = $('<div class="connectorOutSource">').attr('id', i + "-out").addClass('connection');

            finalElement.append(connection);

            finalElement.css({
                'top': top,
                'left': left
            });

            $(self.container).append(finalElement);

            self.jsPlumbInstance.draggable(finalElement, {
                containment: true,
                start: function (e) {
                    finalElement.attr('data-x', e.e.clientX);
                    finalElement.attr('data-y', e.e.clientY);
                }
            });

            self.jsPlumbInstance.makeSource(connection, {
                deleteEndpointsOnDetach: true,
                anchor: 'Right',
                maxConnections: 1
            });
        };

        /**
         * @function drop the sink element on the canvas
         * @param newAgent new element
         * @param i id of the element
         * @param top top position of the element
         * @param left left position of the element
         * @param isCodeToDesignMode whether code to design mode is enable or not
         * @param sinkName name of the sink
         */
        DropElements.prototype.dropSink = function (newAgent, i, top, left, isCodeToDesignMode, sinkName) {

            var self = this;
            var name;
            if (isCodeToDesignMode) {
                name = sinkName;
            } else {
                var sinkOptions = {};
                _.set(sinkOptions, 'id', i);
                _.set(sinkOptions, 'annotationType', 'SINK');
                _.set(sinkOptions, 'type', undefined);
                var sink = new SourceOrSinkAnnotation(sinkOptions);
                self.configurationData.getSiddhiAppConfig().addSink(sink);

                JSONValidator.prototype.validateSourceOrSinkAnnotation(sink, 'Sink', true)
            }
            var node = $('<div>' + name + '</div>');
            newAgent.append(node);
            node.attr('id', i + "-nodeInitial");
            node.attr('class', "sinkNameNode");

            var nodeId = $('#' + i)[0];
            nodeId.addEventListener('dblclick', function () {
                self.formBuilder.GeneratePropertiesFormForSinks(this.children)
            });

            $('#' + i).on('keydown', function (key) {
                if (key.keyCode == ENTER_KEY) {
                    self.formBuilder.GeneratePropertiesFormForSinks(this.children)
                }
            });
            /*
             prop --> When clicked on this icon, a definition and related information of the Sink Element will
             be displayed in a form
            */
            var settingsIconId = "" + i + "-dropSinkSettingsId";
            var prop = $('<i id="' + settingsIconId + '" ' +
                'class="fw fw-settings element-prop-icon collapse"></i>');
            newAgent.append(node).append(
                '<i class="fw fw-delete element-close-icon collapse" data-toggle="popover"></i>').append(prop);

            var settingsIconElement = $('#' + settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForSinks(this);
            });

            var finalElement = newAgent;

            /*
             connection --> The connection anchor point is appended to the element
             */
            self.generateSpecificSinkConnectionElements(name,self.jsPlumbInstance, i, finalElement);
            var connection = $('<div class="connectorInSink">').attr('id', i + "-in").addClass('connection');

            finalElement.append(connection);

            finalElement.css({
                'top': top,
                'left': left
            });

            $(self.container).append(finalElement);

            self.jsPlumbInstance.draggable(finalElement, {
                containment: true,
                start: function (e) {
                    finalElement.attr('data-x', e.e.clientX);
                    finalElement.attr('data-y', e.e.clientY);
                }
            });

            self.jsPlumbInstance.makeTarget(connection, {
                deleteEndpointsOnDetach: true,
                anchor: 'Left',
                maxConnections: 1
            });
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
                                                      isGenerateStreamFromQueryOutput, streamName, hasFaultStream, inFaultStreamCreationPath) {
            /*
             The node hosts a text node where the Stream's name input by the user will be held.
             Rather than simply having a `newAgent.text(streamName)` statement, as the text function tends to
             reposition the other appended elements with the length of the Stream name input by the user.
            */
            var self = this;
            var name;
            if (isCodeToDesignMode || inFaultStreamCreationPath) {
                name = streamName;
            } else {
                if (isGenerateStreamFromQueryOutput) {
                    name = streamName;
                } else if (!hasFaultStream) {
                    var streamOptions = {};
                    _.set(streamOptions, 'id', i);
                    _.set(streamOptions, 'name', undefined);
                    var stream = new Stream(streamOptions);
                    self.configurationData.getSiddhiAppConfig().addStream(stream);

                    JSONValidator.prototype.validateForElementName(stream, "Stream", true)
                }
            }
            var node = $('<div>' + name + '</div>');
            newAgent.append(node);
            node.attr('id', i + "-nodeInitial");
            node.attr('class', "streamNameNode");

            var nodeId = $('#' + i)[0];
            nodeId.addEventListener('dblclick', function () {
                self.formBuilder.GeneratePropertiesFormForStreams(this.children)
            });

            $('#' + i).on('keydown', function (key) {
                if (key.keyCode == ENTER_KEY) {
                    self.formBuilder.GeneratePropertiesFormForStreams(this.children)
                }
            });

            /*
             prop --> When clicked on this icon, a definition and related information of the Stream Element will
             be displayed in a form
             showIcon --> An icon that elucidates whether the dropped stream element is an Import/Export/Defined
             stream (In this case: an Import arrow icon)
            */
            var settingsIconId = "" + i + "-dropStreamSettingsId";
            var prop = $('<i id="' + settingsIconId + '" ' +
                'class="fw fw-settings element-prop-icon collapse"></i>');
            newAgent.append(node).append(
                '<i class="fw fw-delete element-close-icon collapse"data-toggle="popover"></i>').append(prop);

            var settingsIconElement = $('#' + settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForStreams(this);
            });

            var finalElement = newAgent;

            /*
             connection --> The connection anchor point is appended to the element
             */

            var connection1 = $('<div class="connectorInStream">').attr('id', i + "-in").addClass('connection');
            var connection2 = $('<div class="connectorOutStream">').attr('id', i + "-out").addClass('connection');

            var connectionErrOut;
            if (hasFaultStream) {
                connectionErrOut = $('<div class="error-connection connectorErrOutStream">').attr('id', i + "-err-out");
                finalElement.append(connectionErrOut);
                $("#"+finalElement[0].id).addClass('errorStreamDrop');

                connection1 = $('<div class="connectorInStreamForErrorStream">').attr('id', i + "-in").
                addClass('connection');
                connection2 = $('<div class="connectorOutForErrorStream">').attr('id', i + "-out").
                addClass('connection');
            }

            finalElement.append(connection1);
            finalElement.append(connection2);

            finalElement.css({
                'top': top,
                'left': left
            });

            $(self.container).append(finalElement);

            self.jsPlumbInstance.draggable(finalElement, {
                containment: true,
                start: function (e) {
                    finalElement.attr('data-x', e.e.clientX);
                    finalElement.attr('data-y', e.e.clientY);
                }
            });

            self.jsPlumbInstance.makeTarget(connection1, {
                deleteEndpointsOnDetach: true,
                anchor: 'Left'
            });

            self.jsPlumbInstance.makeSource(connection2, {
                deleteEndpointsOnDetach: true,
                anchor: 'Right'
            });

            if (hasFaultStream) {
                self.jsPlumbInstance.makeSource(connectionErrOut, {
                    deleteEndpointsOnDetach: true,
                    anchor: 'Right',
                    connectorStyle: { strokeWidth: 2, stroke: '#FF0000', outlineStroke: 'transparent',
                        outlineWidth: '3' }
                });
            }
        };

        /**
         @function drop stream that is defined as the output stream in a query configuration
         * @param queryModel Query data holding object
         * @param position  position of selected query
         * @param queryId  id of selected query
         * @param outStream  name for new output stream
         * @param streamAttributes  projections list for output stream
         */
        DropElements.prototype.dropStreamFromQuery = function (queryModel, position, queryId, outStream,
                                                               streamAttributes) {
            var self = this;
            var isStreamNameUsed = false;
            var elementID;
            _.forEach(self.configurationData.getSiddhiAppConfig().streamList, function (stream) {
                if (stream.getName().toUpperCase() === outStream.toUpperCase()) {
                    isStreamNameUsed = true;
                    elementID = stream.getId();
                }
            });
            if (!isStreamNameUsed) {
                elementID = self.designGrid.getNewAgentId();
                var newAgent = $('<div>').attr('id', elementID).addClass('streamDrop');
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
                self.configurationData.getSiddhiAppConfig().addStream(stream);

                // increment the variable final element count
                self.configurationData.getSiddhiAppConfig()
                    .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() + 1);
                self.registerElementEventListeners(newAgent);
            }
            // design view container and toggle view button are enabled
            this.designViewContainer.removeClass('disableContainer');
            this.toggleViewButton.removeClass('disableContainer');

            // make the connection
            self.jsPlumbInstance.connect({
                source: queryId + '-out',
                target: elementID + '-in'
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

            var self = this;
            var name;
            if (isCodeToDesignMode) {
                name = tableName;
            } else {
                var tableOptions = {};
                _.set(tableOptions, 'id', i);
                _.set(tableOptions, 'name', undefined);
                var table = new Table(tableOptions);
                self.configurationData.getSiddhiAppConfig().addTable(table);

                JSONValidator.prototype.validateForElementName(table, "Table", true)
            }
            var node = $('<div>' + name + '</div>');
            newAgent.append(node);
            node.attr('id', i + "-nodeInitial");
            node.attr('class', "tableNameNode");

            var nodeId = $('#' + i)[0];
            nodeId.addEventListener('dblclick', function () {
                self.formBuilder.GeneratePropertiesFormForTables(this.children)
            });

            $('#' + i).on('keydown', function (key) {
                if (key.keyCode == ENTER_KEY) {
                    self.formBuilder.GeneratePropertiesFormForTables(this.children)
                }
            });
            /*
             prop --> When clicked on this icon, a definition and related information of the Table Element will
             be displayed in a form
            */
            var settingsIconId = "" + i + "-dropTableSettingsId";
            var prop = $('<i id="' + settingsIconId + '" ' +
                'class="fw fw-settings element-prop-icon collapse"></i>');
            newAgent.append(node).append(
                '<i class="fw fw-delete element-close-icon collapse" data-toggle="popover"></i>').append(prop);

            var settingsIconElement = $('#' + settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForTables(this);
            });

            var finalElement = newAgent;

            /*
             connection --> The connection anchor point is appended to the element
             */
            var connection1 = $('<div class="connectorInTable">').attr('id', i + "-in").addClass('connection');
            var connection2 = $('<div class="connectorOutTable">').attr('id', i + "-out").addClass('connection');


            finalElement.append(connection1);
            finalElement.append(connection2);

            finalElement.css({
                'top': top,
                'left': left
            });

            $(self.container).append(finalElement);

            self.jsPlumbInstance.draggable(finalElement, {
                containment: true,
                start: function (e) {
                    finalElement.attr('data-x', e.e.clientX);
                    finalElement.attr('data-y', e.e.clientY);
                }
            });

            self.jsPlumbInstance.makeTarget(connection1, {
                deleteEndpointsOnDetach: true,
                anchor: 'Left'
            });

            self.jsPlumbInstance.makeSource(connection2, {
                deleteEndpointsOnDetach: true,
                anchor: 'Right'
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

            var self = this;
            var name;
            if (isCodeToDesignMode) {
                name = windowName;
            } else {
                var windowOptions = {};
                _.set(windowOptions, 'id', i);
                _.set(windowOptions, 'name', undefined);
                var window = new Window(windowOptions);
                self.configurationData.getSiddhiAppConfig().addWindow(window);

                JSONValidator.prototype.validateForElementName(window, "Window", true)
            }
            var node = $('<div>' + name + '</div>');
            newAgent.append(node);
            node.attr('id', i + "-nodeInitial");
            node.attr('class', "windowNameNode");

            var nodeId = $('#' + i)[0];
            nodeId.addEventListener('dblclick', function () {
                self.formBuilder.GeneratePropertiesFormForWindows(this.children)
            });

            $('#' + i).on('keydown', function (key) {
                if (key.keyCode == ENTER_KEY) {
                    self.formBuilder.GeneratePropertiesFormForWindows(this.children)
                }
            });
            /*
             prop --> When clicked on this icon, a definition and related information of the Window Element will
             be displayed as an DesignViewUtils.prototype.warnAlert message
            */
            var settingsIconId = "" + i + "-dropWindowSettingsId";
            var prop = $('<i id="' + settingsIconId + '" ' +
                'class="fw fw-settings element-prop-icon collapse"></i>');
            newAgent.append(node).append(
                '<i class="fw fw-delete element-close-icon collapse" data-toggle="popover"></i>').append(prop);

            var settingsIconElement = $('#' + settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForWindows(this);
            });

            var finalElement = newAgent;

            /*
             connection --> The connection anchor point is appended to the element
             */
            var connection1 = $('<div class="connectorInWindow">').attr('id', i + "-in").addClass('connection');
            var connection2 = $('<div class="connectorOutWindow">').attr('id', i + "-out").addClass('connection');


            finalElement.append(connection1);
            finalElement.append(connection2);

            finalElement.css({
                'top': top,
                'left': left
            });

            $(self.container).append(finalElement);

            self.jsPlumbInstance.draggable(finalElement, {
                containment: true,
                start: function (e) {
                    finalElement.attr('data-x', e.e.clientX);
                    finalElement.attr('data-y', e.e.clientY);
                }
            });

            self.jsPlumbInstance.makeTarget(connection1, {
                deleteEndpointsOnDetach: true,
                anchor: 'Left'
            });

            self.jsPlumbInstance.makeSource(connection2, {
                deleteEndpointsOnDetach: true,
                anchor: 'Right'
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

            var self = this;
            var name;
            if (isCodeToDesignMode) {
                name = triggerName;
            } else {
                var triggerOptions = {};
                _.set(triggerOptions, 'id', i);
                _.set(triggerOptions, 'name', undefined);
                var trigger = new Trigger(triggerOptions);
                self.configurationData.getSiddhiAppConfig().addTrigger(trigger);

                JSONValidator.prototype.validateForElementName(trigger, "Trigger", true)
            }
            var node = $('<div>' + name + '</div>');
            newAgent.append(node);
            node.attr('id', i + "-nodeInitial");
            node.attr('class', "triggerNameNode");

            var nodeId = $('#' + i)[0];
            nodeId.addEventListener('dblclick', function () {
                self.formBuilder.GeneratePropertiesFormForTriggers(this.children)
            });

            $('#' + i).on('keydown', function (key) {
                if (key.keyCode == ENTER_KEY) {
                    self.formBuilder.GeneratePropertiesFormForTriggers(this.children)
                }
            });
            /*
             prop --> When clicked on this icon, a definition and related information of the Trigger Element will
             be displayed as an DesignViewUtils.prototype.warnAlert message
            */
            var settingsIconId = "" + i + "-dropTriggerSettingsId";
            var prop = $('<i id="' + settingsIconId + '" ' +
                'class="fw fw-settings element-prop-icon collapse"></i>');
            newAgent.append(node).append(
                '<i class="fw fw-delete element-close-icon collapse" data-toggle="popover"></i>').append(prop);

            var settingsIconElement = $('#' + settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForTriggers(this);
            });

            var finalElement = newAgent;

            /*
             connection --> The connection anchor point is appended to the element
             */
            var connection1 = $('<div class="connectorInTrigger">').attr('id', i + "-in").addClass('connection');
            var connection2 = $('<div class="connectorOutTrigger">').attr('id', i + "-out").addClass('connection');


            finalElement.append(connection1);
            finalElement.append(connection2);

            finalElement.css({
                'top': top,
                'left': left
            });

            $(self.container).append(finalElement);

            self.jsPlumbInstance.draggable(finalElement, {
                containment: true,
                start: function (e) {
                    finalElement.attr('data-x', e.e.clientX);
                    finalElement.attr('data-y', e.e.clientY);
                }
            });

            self.jsPlumbInstance.makeTarget(connection1, {
                deleteEndpointsOnDetach: true,
                anchor: 'Left'
            });

            self.jsPlumbInstance.makeSource(connection2, {
                deleteEndpointsOnDetach: true,
                anchor: 'Right'
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
        DropElements.prototype.dropAggregation = function (newAgent, i, top, left, isCodeToDesignMode,
                                                           aggregationName) {
            /*
             The node hosts a text node where the Aggregation's name input by the user will be held.
             Rather than simply having a `newAgent.text(aggregationName)` statement, as the text function tends to
             reposition the other appended elements with the length of the Aggregation name input by the user.
            */

            var self = this;
            var name;
            if (isCodeToDesignMode) {
                name = aggregationName;
            } else {
                //add the new aggregation element to aggregation list
                var aggregationOptions = {};
                _.set(aggregationOptions, 'id', i);
                _.set(aggregationOptions, 'name', undefined);
                var aggregation = new Aggregation(aggregationOptions);
                self.configurationData.getSiddhiAppConfig().addAggregation(aggregation);

                // perform JSON validation
                JSONValidator.prototype.validateAggregation(aggregation, true);
            }

            var node = $('<div>' + name + '</div>');
            newAgent.append(node);
            node.attr('id', i + "-nodeInitial");
            node.attr('class', "aggregationNameNode");

            var nodeId = $('#' + i)[0];
            nodeId.addEventListener('dblclick', function () {
                self.formBuilder.GeneratePropertiesFormForAggregations(this.children)
            });

            $('#' + i).on('keydown', function (key) {
                if (key.keyCode == ENTER_KEY) {
                    self.formBuilder.GeneratePropertiesFormForAggregations(this.children)
                }
            });
            /*
             prop --> When clicked on this icon, a definition and related information of the Aggregation Element will
             be displayed as an DesignViewUtils.prototype.warnAlert message
            */
            var settingsIconId = "" + i + "-dropAggregationSettingsId";
            var prop = $('<i id="' + settingsIconId + '" ' +
                'class="fw fw-settings element-prop-icon collapse"></i>');
            newAgent.append(node).append(
                '<i class="fw fw-delete element-close-icon collapse"data-toggle="popover"></i>').append(prop);

            var settingsIconElement = $('#' + settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForAggregations(this);
            });

            var finalElement = newAgent;

            /*
             connection --> The connection anchor point is appended to the element
             */
            var connection1 = $('<div class="connectorInAggregation">').attr('id', i + "-in").addClass('connection');
            var connection2 = $('<div class="connectorOutAggregation">').attr('id', i + "-out").addClass('connection');


            finalElement.append(connection1);
            finalElement.append(connection2);

            finalElement.css({
                'top': top,
                'left': left
            });

            $(self.container).append(finalElement);

            self.jsPlumbInstance.draggable(finalElement, {
                containment: true,
                start: function (e) {
                    finalElement.attr('data-x', e.e.clientX);
                    finalElement.attr('data-y', e.e.clientY);
                }
            });

            self.jsPlumbInstance.makeTarget(connection1, {
                deleteEndpointsOnDetach: true,
                maxConnections: 1,
                anchor: 'Left'
            });

            self.jsPlumbInstance.makeSource(connection2, {
                deleteEndpointsOnDetach: true,
                anchor: 'Right'
            });
        };

        /**
         * @function drop the function element on the canvas
         * @param newAgent new element
         * @param i id of the element
         * @param top top position of the element
         * @param left left position of the element
         * @param isCodeToDesignMode whether code to design mode is enable or not
         * @param functionName name of the function
         */
        DropElements.prototype.dropFunction = function (newAgent, i, top, left, isCodeToDesignMode, functionName) {
            /*
             The node hosts a text node where the Function's name input by the user will be held.
             Rather than simply having a `newAgent.text(functionName)` statement, as the text function tends to
             reposition the other appended elements with the length of the Function name input by the user.
            */

            var self = this;
            var name;
            if (isCodeToDesignMode) {
                name = functionName;
            } else {
                //add the new function element to function list
                var functionOptions = {};
                _.set(functionOptions, 'id', i);
                _.set(functionOptions, 'name', undefined);
                var functionObject = new FunctionDefinition(functionOptions);
                self.configurationData.getSiddhiAppConfig().addFunction(functionObject);

                //perform json validation
                JSONValidator.prototype.validateForElementName(functionObject, "Function", true)

            }
            var node = $('<div>' + name + '</div>');
            newAgent.append(node);
            node.attr('id', i + "-nodeInitial");
            node.attr('class', "functionNameNode");

            var nodeId = $('#' + i)[0];
            nodeId.addEventListener('dblclick', function () {
                self.formBuilder.GeneratePropertiesFormForFunctions(this.children)
            });

            $('#' + i).on('keydown', function (key) {
                if (key.keyCode == ENTER_KEY) {
                    self.formBuilder.GeneratePropertiesFormForFunctions(this.children)
                }
            });
            /*
             prop --> When clicked on this icon, a definition and related information of the Function Element will
             be displayed as an DesignViewUtils.prototype.warnAlert message
            */
            var settingsIconId = "" + i + "-dropFunctionSettingsId";
            var prop = $('<i id="' + settingsIconId + '" ' +
                'class="fw fw-settings element-prop-icon collapse"></i>');
            newAgent.append(node).append(
                '<i class="fw fw-delete element-close-icon collapse"data-toggle="popover"></i>').append(prop);

            var settingsIconElement = $('#' + settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForFunctions(this);
            });

            var finalElement = newAgent;

            finalElement.css({
                'top': top,
                'left': left
            });

            $(self.container).append(finalElement);

            self.jsPlumbInstance.draggable(finalElement, {
                containment: true,
                start: function (e) {
                    finalElement.attr('data-x', e.e.clientX);
                    finalElement.attr('data-y', e.e.clientY);
                }
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
            var node = $('<div>' + text + '</div>');
            newAgent.append(node);
            node.attr('id', i + "-nodeInitial");
            node.attr('class', "queryNameNode");

            var nodeId = $('#' + i)[0];
            nodeId.addEventListener('dblclick', function () {
                self.formBuilder.GeneratePropertiesFormForWindowFilterProjectionQueries(this.children)
            });
            $('#' + i).on('keydown', function (key) {
                if (key.keyCode == ENTER_KEY) {
                    self.formBuilder.GeneratePropertiesFormForWindowFilterProjectionQueries(this.children)
                }
            });
            if (!isCodeToDesignMode) {
                //add the new join query to the join query array
                var queryOptions = {};
                _.set(queryOptions, 'id', i);
                var query = new Query(queryOptions);
                self.configurationData.getSiddhiAppConfig().addWindowFilterProjectionQuery(query);

                // perform JSON validation
                JSONValidator.prototype.validateWindowFilterProjectionQuery(query, true);
            }
            var settingsIconId = "" + i + "-dropQuerySettingsId";
            var propertiesIcon = $('<i id="' + settingsIconId + '" ' +
                'class="fw fw-settings element-prop-icon collapse"></i>');
            newAgent.append(node).append(
                '<i class="fw fw-delete element-close-icon collapse" data-toggle="popover"></i>')
                .append(propertiesIcon);

            var settingsIconElement = $('#' + settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForWindowFilterProjectionQueries(this);
            });

            var finalElement = newAgent;
            var connectionIn = $('<div class="connectorInQuery">').attr('id', i + '-in').addClass('connection');
            var connectionOut = $('<div class="connectorOutQuery">').attr('id', i + '-out').addClass('connection');

            finalElement.css({
                'top': top,
                'left': left
            });

            finalElement.append(connectionIn);
            finalElement.append(connectionOut);

            $(self.container).append(finalElement);

            self.jsPlumbInstance.draggable(finalElement, {
                containment: true,
                start: function (e) {
                    finalElement.attr('data-x', e.e.clientX);
                    finalElement.attr('data-y', e.e.clientY);
                }
            });

            self.jsPlumbInstance.makeTarget(connectionIn, {
                anchor: 'Left',
                maxConnections: 1,
                deleteEndpointsOnDetach: true
            });

            self.jsPlumbInstance.makeSource(connectionOut, {
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
        DropElements.prototype.dropPatternQuery = function (newAgent, i, top, left, isCodeToDesignMode,
                                                            patternQueryName) {

            /*
             A text node division will be appended to the newAgent element so that the element name can be changed in
             the text node and doesn't need to be appended to the newAgent Element every time the user changes it
            */
            var self = this;
            var node = $('<div>' + patternQueryName + '</div>');
            newAgent.append(node);
            node.attr('id', i + "-nodeInitial");
            node.attr('class', "patternQueryNameNode");

            var nodeId = $('#' + i)[0];
            nodeId.addEventListener('dblclick', function () {
                self.formBuilder.GeneratePropertiesFormForPatternQueries(this.children)
            });

            $('#' + i).on('keydown', function (key) {
                if (key.keyCode == ENTER_KEY) {
                    self.formBuilder.GeneratePropertiesFormForPatternQueries(this.children)
                }
            });

            if (!isCodeToDesignMode) {
                //add the new join query to the join query array
                var patternQueryOptions = {};
                _.set(patternQueryOptions, 'id', i);
                var patternQuery = new Query(patternQueryOptions);
                self.configurationData.getSiddhiAppConfig().addPatternQuery(patternQuery);

                // perform JSON validation
                JSONValidator.prototype.validatePatternOrSequenceQuery(patternQuery, 'Pattern Query', true);
            }

            /*
             prop --> When clicked on this icon, a definition and related information of the PatternQuery Element will
             be displayed as an DesignViewUtils.prototype.warnAlert message
            */
            var settingsIconId = "" + i + "-dropPatternQuerySettingsId";
            var prop = $('<i id="' + settingsIconId + '" ' +
                'class="fw fw-settings element-prop-icon collapse"></i>');
            newAgent.append(node).append(
                '<i class="fw fw-delete element-close-icon collapse"data-toggle="popover"></i>').append(prop);

            var settingsIconElement = $('#' + settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForPatternQueries(this);
            });

            var finalElement = newAgent;

            /*
             connection --> The connection anchor point is appended to the element
             */
            var connection1 = $('<div class="connectorInPatternQuery">').attr('id', i + "-in").addClass('connection');
            var connection2 = $('<div class="connectorOutPatternQuery">').attr('id', i + "-out").addClass('connection');


            finalElement.append(connection1);
            finalElement.append(connection2);

            finalElement.css({
                'top': top,
                'left': left
            });

            $(self.container).append(finalElement);

            self.jsPlumbInstance.draggable(finalElement, {
                containment: true,
                start: function (e) {
                    finalElement.attr('data-x', e.e.clientX);
                    finalElement.attr('data-y', e.e.clientY);
                }
            });

            self.jsPlumbInstance.makeTarget(connection1, {
                anchor: 'Left',
                deleteEndpointsOnDetach: true
            });

            self.jsPlumbInstance.makeSource(connection2, {
                anchor: 'Right',
                maxConnections: 1,
                deleteEndpointsOnDetach: true
            });
        };

        /**
         * @function drop the sequenceQuery element on the canvas
         * @param newAgent new element
         * @param i id of the element
         * @param top top position of the element
         * @param left left position of the element
         * @param isCodeToDesignMode whether code to design mode is enable or not
         * @param sequenceQueryName name of the sequenceQuery
         */
        DropElements.prototype.dropSequenceQuery = function (newAgent, i, top, left, isCodeToDesignMode,
                                                             sequenceQueryName) {

            /*
             A text node division will be appended to the newAgent element so that the element name can be changed in
             the text node and doesn't need to be appended to the newAgent Element every time the user changes it
            */
            var self = this;
            var node = $('<div>' + sequenceQueryName + '</div>');
            newAgent.append(node);
            node.attr('id', i + "-nodeInitial");
            node.attr('class', "sequenceQueryNameNode");

            var nodeId = $('#' + i)[0];
            nodeId.addEventListener('dblclick', function () {
                self.formBuilder.GeneratePropertiesFormForSequenceQueries(this.children)
            });

            $('#' + i).on('keydown', function (key) {
                if (key.keyCode == ENTER_KEY) {
                    self.formBuilder.GeneratePropertiesFormForSequenceQueries(this.children)
                }
            });

            if (!isCodeToDesignMode) {
                //add the new join query to the join query array
                var sequenceQueryOptions = {};
                _.set(sequenceQueryOptions, 'id', i);
                var sequenceQuery = new Query(sequenceQueryOptions);
                self.configurationData.getSiddhiAppConfig().addSequenceQuery(sequenceQuery);

                // perform JSON validation
                JSONValidator.prototype
                    .validatePatternOrSequenceQuery(sequenceQuery, 'Sequence Query', true);
            }

            /*
             prop --> When clicked on this icon, a definition and related information of the SequenceQuery Element will
             be displayed in a form
            */
            var settingsIconId = "" + i + "-dropSequenceQuerySettingsId";
            var prop = $('<i id="' + settingsIconId + '" ' +
                'class="fw fw-settings element-prop-icon collapse"></i>');
            newAgent.append(node).append(
                '<i class="fw fw-delete element-close-icon collapse" data-toggle="popover"></i>').append(prop);

            var settingsIconElement = $('#' + settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForSequenceQueries(this);
            });

            var finalElement = newAgent;

            /*
             connection --> The connection anchor point is appended to the element
             */
            var connection1 = $('<div class="connectorInSequenceQuery">').attr('id', i + "-in")
                .addClass('connection');
            var connection2 = $('<div class="connectorOutSequenceQuery">').attr('id', i + "-out")
                .addClass('connection');


            finalElement.append(connection1);
            finalElement.append(connection2);

            finalElement.css({
                'top': top,
                'left': left
            });

            $(self.container).append(finalElement);

            self.jsPlumbInstance.draggable(finalElement, {
                containment: true,
                start: function (e) {
                    finalElement.attr('data-x', e.e.clientX);
                    finalElement.attr('data-y', e.e.clientY);
                }
            });

            self.jsPlumbInstance.makeTarget(connection1, {
                anchor: 'Left',
                deleteEndpointsOnDetach: true
            });

            self.jsPlumbInstance.makeSource(connection2, {
                anchor: 'Right',
                maxConnections: 1,
                deleteEndpointsOnDetach: true
            });
        };

        /**
         * @function draw the join query element on the canvas
         * @param newAgent new element
         * @param i id of the element
         * @param top top position of the element
         * @param left left position of the element
         * @param text text to be displayed on the element
         * @param isCodeToDesignMode whether code to design mode is enable or not
         */
        DropElements.prototype.dropJoinQuery = function (newAgent, i, top, left, text, isCodeToDesignMode) {
            /*
             A text node division will be appended to the newAgent element so that the element name can be changed in
             the text node and doesn't need to be appended to the newAgent Element every time the user changes it
            */
            var self = this;
            var node = $('<div>' + text + '</div>');
            newAgent.append(node);
            node.attr('id', i + "-nodeInitial");
            node.attr('class', "joinQueryNameNode");

            var nodeId = $('#' + i)[0];
            nodeId.addEventListener('dblclick', function () {
                self.formBuilder.GeneratePropertiesFormForJoinQuery(this.children)
            });

            $('#' + i).on('keydown', function (key) {
                if (key.keyCode == ENTER_KEY) {
                    self.formBuilder.GeneratePropertiesFormForJoinQuery(this.children)
                }
            });

            if (!isCodeToDesignMode) {
                //add the new join query to the join query array
                var queryOptions = {};
                _.set(queryOptions, 'id', i);
                var query = new Query(queryOptions);
                self.configurationData.getSiddhiAppConfig().addJoinQuery(query);

                // perform JSON validation
                JSONValidator.prototype.validateJoinQuery(query, true);
            }
            var settingsIconId = "" + i + "-dropJoinQuerySettingsId";
            var propertiesIcon = $('<i id="' + settingsIconId + '" ' +
                'class="fw fw-settings element-prop-icon collapse"></i>');
            newAgent.append(node).append(
                '<i class="fw fw-delete element-close-icon collapse"data-toggle="popover" ></i>')
                .append(propertiesIcon);

            var settingsIconElement = $('#' + settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePropertiesFormForJoinQuery(this);
            });

            var finalElement = newAgent;
            var connectionIn = $('<div class="connectorInJoinQuery">').attr('id', i + '-in').addClass('connection');
            var connectionOut = $('<div class="connectorOutJoinQuery">').attr('id', i + '-out').addClass('connection');

            finalElement.css({
                'top': top,
                'left': left
            });

            finalElement.append(connectionIn);
            finalElement.append(connectionOut);

            $(self.container).append(finalElement);

            self.jsPlumbInstance.draggable(finalElement, {
                containment: true,
                start: function (e) {
                    finalElement.attr('data-x', e.e.clientX);
                    finalElement.attr('data-y', e.e.clientY);
                }
            });

            self.jsPlumbInstance.makeTarget(connectionIn, {
                anchor: 'Left',
                maxConnections: 2,
                deleteEndpointsOnDetach: true
            });

            self.jsPlumbInstance.makeSource(connectionOut, {
                anchor: 'Right',
                uniqueEndpoint: true,
                maxConnections: 1,
                deleteEndpointsOnDetach: true
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

            var node = $('<div></div>');
            node.attr('class', "partitionNameNode");

            var nodeId = $('#' + i)[0];
            nodeId.addEventListener('dblclick', function () {
                self.formBuilder.GeneratePartitionKeyForm(this.children)
            });

            $('#' + i).on('keydown', function (key) {
                if (key.keyCode == ENTER_KEY) {
                    self.formBuilder.GeneratePartitionKeyForm(this.children)
                }
            });

            newAgent.append(node);
            var settingsIconId = "" + i + "-dropPartitionSettingsId";
            var propertiesIcon = $('<i id="' + settingsIconId + '" ' +
                'class="fw fw-settings partition-element-prop-icon collapse"></i>');
            newAgent.append(node).append(
                '<i class="fw fw-delete element-close-icon partition collapse" data-type="partition" ' +
                'data-toggle="popover"></i>')
                .append(propertiesIcon);

            var settingsIconElement = $('#' + settingsIconId)[0];
            settingsIconElement.addEventListener('click', function () {
                self.formBuilder.GeneratePartitionKeyForm(this);
            });
            var finalElement = newAgent;

            $(finalElement).draggable({
                containment: "grid-container",
                drag: function () {
                    self.jsPlumbInstance.repaintEverything();
                }
            });

            $(finalElement).resizable();

            finalElement.css({
                'top': mouseTop,
                'left': mouseLeft
            });

            if (!isCodeToDesignMode) {
                //add the new partition to the partition array
                var partitionOptions = {};
                _.set(partitionOptions, 'id', i);
                var newPartition = new Partition(partitionOptions);
                newPartition.setId(i);
                self.configurationData.getSiddhiAppConfig().addPartition(newPartition);

                // perform JSON validation
                JSONValidator.prototype.validatePartition(newPartition, self.jsPlumbInstance, true);
            }

            // There will be always added a connection point by default
            var connectionIn = $('<div class="partition-connector-in-part" >').attr('id', i + '_pc' + 1);
            finalElement.append(connectionIn);

            self.jsPlumbInstance.makeTarget(connectionIn, {
                anchor: 'Left',
                maxConnections: 1,
                deleteEndpointsOnDetach: true
            });
            self.jsPlumbInstance.makeSource(connectionIn, {
                anchor: 'Right',
                deleteEndpointsOnDetach: true
            });

            $(self.container).append(finalElement);
            self.jsPlumbInstance.addGroup({
                el: $('#' + i)[0],
                id: i,
                droppable: true,
                constrain: true,
                dropOverride: false,
                draggable: false
            });
        };

        DropElements.prototype.generateSpecificSourceConnectionElements = function (type, jsPlumbInstance, i, element) {
            if (type === Constants.TYPE_HTTP_RESPONSE ||
                (type !== undefined && type.endsWith(Constants.TYPE_CALL_RESPONSE))) {
                var connectionIn = $('<div class="connectorInSource">').attr('id', i + "-in").addClass('connection');
                element.append(connectionIn);
                jsPlumbInstance.makeTarget(connectionIn, {
                    deleteEndpointsOnDetach: true,
                    anchor: 'Left',
                    maxConnections: 1
                });
            } else {
                if ($( "#" + i + "-in" ).length) {
                    $( "#" + i + "-in" ).remove();
                }
            }

        };

        DropElements.prototype.generateSpecificSinkConnectionElements = function (type, jsPlumbInstance, i, element) {
            if (type === Constants.TYPE_HTTP_REQUEST ||
                (type !== undefined && type.endsWith(Constants.TYPE_CALL))) {
                var connectionOut = $('<div class="connectorOutSink">').attr('id', i + "-out").addClass('connection');
                element.append(connectionOut);
                jsPlumbInstance.makeSource(connectionOut, {
                    deleteEndpointsOnDetach: true,
                    anchor: 'Right',
                    connectorStyle: { strokeWidth: 2, stroke: "#424242", dashstyle: "2 3", outlineStroke: "transparent",
                        outlineWidth: "3" }
                });
            } else {
                if ($( "#" + i + "-out" ).length) {
                    $( "#" + i + "-out" ).remove();
                }
            }

        };

        DropElements.prototype.toggleFaultStreamConnector = function(stream, jsPlumbInstance,
                                                                     previouslySavedStreamName) {
            var self = this;
            var oldFaultStreamName = Constants.FAULT_STREAM_PREFIX + previouslySavedStreamName;
            var oldFaultStreamElementId = "";
            self.configurationData.getSiddhiAppConfig().getStreamList().forEach(function(s) {
                if (oldFaultStreamName === s.getName()) {
                    oldFaultStreamElementId = s.getId();
                }
            });

            if (oldFaultStreamElementId != "") {
                self.configurationData.getSiddhiAppConfig().removeStream(oldFaultStreamElementId);
                $( "#" + oldFaultStreamElementId ).remove();
            }
            if (stream.hasFaultStream()) {
                self.removeAndRecreateConnection(stream.getId());

                var faultStreamName = Constants.FAULT_STREAM_PREFIX + stream.getName();
                var connectionErrOut = $('<div class="error-connection connectorErrOutStream">')
                    .attr('id', stream.getId() + "-err-out");
                $('#' + stream.getId()).append(connectionErrOut);
                jsPlumbInstance.makeSource(connectionErrOut, {
                    deleteEndpointsOnDetach: true,
                    anchor: 'Right',
                    connectorStyle: {
                        strokeWidth: 2, stroke: '#FF0000',
                        outlineStroke: 'transparent',
                        outlineWidth: '3'
                    }
                });

                $('#' + stream.getId()).addClass('errorStreamDrop');

                //create error object for faults stream
                var id = self.designGrid.generateNextNewAgentId();

                //add error object
                var attributeObject = new Attribute({ name: "_error", type: "OBJECT" });
                var attributeList = stream.getAttributeList();
                // Add the fault stream to the stream list
                var faultStream = new Stream({
                    id: id,
                    name: faultStreamName
                });
                for(var i = 0; i < attributeList.length; i++) {
                    var obj = attributeList[i];
                    faultStream.addAttribute(obj);
                }
                faultStream.addAttribute(attributeObject);
                this.configurationData.getSiddhiAppConfig().addStream(faultStream);

                self.designGrid.handleStream(0, 0, false, id, faultStreamName, undefined, faultStream);
            } else {
                var connections = self.jsPlumbInstance.getConnections({source: stream.getId() + '-err-out'});
                _.forEach(connections, function (connection) {
                    self.jsPlumbInstance.deleteConnection(connection);
                });

                $('#' + stream.getId() + '-err-out').remove();
                $('#' + stream.getId()).removeClass('errorStreamDrop');

                self.removeAndRecreateConnection(stream.getId());
            }
        };

        DropElements.prototype.removeAndRecreateConnection = function (elementId) {
            var self = this;

            var inConnection = self.jsPlumbInstance.getConnections({target: elementId + '-in'})[0];
            var outConnection = self.jsPlumbInstance.getConnections({source: elementId + '-out'})[0];

            if (inConnection) {
                self.jsPlumbInstance.deleteConnection(inConnection, {
                    fireEvent: false, //prevents firing a connection detached event
                    forceDetach: false //override any beforeDetach listeners
                });
                self.jsPlumbInstance.connect({
                    source: inConnection.sourceId,
                    target: inConnection.targetId,
                });
                self.jsPlumbInstance.makeTarget(inConnection.targetId, {
                    deleteEndpointsOnDetach: true,
                    anchor: 'Left'
                });
            }
            if (outConnection) {
                self.jsPlumbInstance.deleteConnection(outConnection, {
                    fireEvent: false, //prevents firing a connection detached event
                    forceDetach: false //override any beforeDetach listeners
                });
                self.jsPlumbInstance.connect({
                    source: outConnection.sourceId,
                    target: outConnection.targetId
                });
                self.jsPlumbInstance.makeSource(outConnection.sourceId, {
                    deleteEndpointsOnDetach: true,
                    anchor: 'Right'
                });
            }
        };

        /**
         * @function Bind event listeners for the elements that are dropped.
         * @param newElement dropped element
         */
        DropElements.prototype.registerElementEventListeners = function (newElement) {
            var self = this;
            //register event listener to show configuration icons when mouse is over the element
            newElement.on("mouseenter", function () {
                $('.element-prop-icon').hide();
                $('.partition-element-prop-icon').hide();
                $('.element-close-icon').hide();
                var element = $(this);
                element.find('.element-prop-icon').show();
                element.find('.element-close-icon').show();
                element.find('.partition-element-prop-icon').show();
            });

            //register event listener to hide configuration icons when mouse is out from the element
            newElement.on("mouseleave", function () {
                var element = $(this);
                element.find('.element-prop-icon').hide();
                element.find('.element-close-icon').hide();
                element.find('.partition-element-prop-icon').hide();
            });

            // Pop-over element for displaying the popover when delete button clicked
            function showPopOver(dataObj, element) {
                $(dataObj).popover({
                    trigger: 'focus',
                    placement: 'auto',
                    title: 'Confirmation',
                    html: true,
                    content: function () {
                        return $('.pop-over').html();
                    }
                });
                $(dataObj).popover("show");
                $('.grid-container').addClass('removeOverflow');
                $('.btn_no').focus();
                $(".overlayed-container ").fadeTo(200, 1);
                element.on("click", ".popover-footer .btn_no", function () {
                    $(".overlayed-container ").fadeOut(200);
                    $(this).parents(".popover").popover('hide');
                    $('#' + newElement[0].id).removeClass("selected-element");
                    $('.grid-container').removeClass('removeOverflow');
                });

                element.off('click', '.popover-footer .btn_yes');
                element.on("click", ".popover-footer .btn_yes", function () {
                    if ('partition' === $(dataObj).data('type')) {
                        deletePartition();
                    } else {
                        deleteElement();
                    }
                    $(this).parents(".popover").popover('hide');
                    $(".overlayed-container ").fadeOut(200);
                });
                // Dismiss the pop-over by clicking outside
                $('.overlayed-container ').on('click', function (e) {
                    $('[data-toggle="popover"]').each(function () {
                        if (!$(this).is(e.target) && $(this).has(e.target).length === 0 && $('.popover').has(
                                e.target).length === 0) {
                            $(this).popover('hide');
                            $(".overlayed-container ").fadeOut(200);
                            $('#' + newElement[0].id).removeClass("selected-element");
                            $('#' + newElement[0].id).children().removeClass("selected-element");
                            $('.grid-container').removeClass('removeOverflow');
                        }
                    });
                });
                //Dismiss the pop-over on Esc button
                $(".btn_no").keyup(function (e) {
                    if (e.which === ESCAPE_KEY) {
                        $(dataObj).popover('hide');
                        $(".overlayed-container ").fadeOut(200);
                        $('#' + newElement[0].id).removeClass("selected-element");
                        $('#' + newElement[0].id).children().removeClass("selected-element")
                        $('.grid-container').removeClass('removeOverflow');
                    }
                });
                //Popver navigation using arrow keys
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

            //register event listener to remove the element when the close icon is clicked
            newElement.on('click', '.element-close-icon', function () {
                $('#' + newElement[0].id).addClass("selected-element");
                showPopOver(this, newElement);
            });

            newElement.on('focus', function () {
                var element = $(this);
                element.addClass("focused-container");
                element.find('.element-prop-icon').show();
                element.find('.element-close-icon').show();
                element.find('.partition-element-prop-icon').show();
            });

            newElement.on('focusout', function () {
                var element = $(this);
                element.removeClass("focused-container");
                $('.element-prop-icon').hide();
                $('.partition-element-prop-icon').hide();
                $('.element-close-icon').hide();

            });

            newElement.on('keydown', function (key) {
                if ((key.keyCode == DELETE_KEY) || (key.keyCode == MAC_COMMAND_KEY && key.keyCode == MAC_DELETE_KEY)) {
                    $('#' + newElement[0].id).addClass('selected-element');
                    showPopOver(this.children.item(1), newElement);
                }
            });

            // Check whether clicked element is there in the selected element list and if not clear the selected
            // element list and cleanDragSelection object
            newElement.on('mousedown', function () {
                if (!self.designGrid.isSelectedElements(newElement[0])){
                    // if (!window.selectedElements.includes(newElement[0], 0)) {
                    self.jsPlumbInstance.clearDragSelection();
                    var selectedElements = self.designGrid.getSelectedElement();
                    for (var i = 0; i < selectedElements.length; i++) {
                        $('#' + selectedElements[i].id).removeClass('selected-container focused-container');
                    }
                    self.designGrid.resetSelectedElement();
                }
            });

            // Deleting an element
            function deleteElement() {
                // set the isDesignViewContentChanged to true
                self.configurationData.setIsDesignViewContentChanged(true);
                var elementId = newElement[0].id;

                self.jsPlumbInstance.remove(newElement, true);
                if (self.jsPlumbInstance.getGroupFor(newElement)) {
                    self.jsPlumbInstance.removeFromGroup(newElement);
                }
                if (newElement.hasClass('streamDrop')) {
                    self.configurationData.getSiddhiAppConfig().removeStream(elementId);
                } else if (newElement.hasClass('tableDrop')) {
                    self.configurationData.getSiddhiAppConfig().removeTable(elementId);
                } else if (newElement.hasClass('windowDrop')) {
                    self.configurationData.getSiddhiAppConfig().removeWindow(elementId);
                } else if (newElement.hasClass('triggerDrop')) {
                    self.configurationData.getSiddhiAppConfig().removeTrigger(elementId);
                } else if (newElement.hasClass('aggregationDrop')) {
                    self.configurationData.getSiddhiAppConfig().removeAggregation(elementId);
                } else if (newElement.hasClass('functionDrop')) {
                    self.configurationData.getSiddhiAppConfig().removeFunction(elementId);
                } else if (newElement.hasClass('projectionQueryDrop')) {
                    self.configurationData.getSiddhiAppConfig().removeWindowFilterProjectionQuery(elementId);
                } else if (newElement.hasClass('filterQueryDrop')) {
                    self.configurationData.getSiddhiAppConfig().removeWindowFilterProjectionQuery(elementId);
                } else if (newElement.hasClass('windowQueryDrop')) {
                    self.configurationData.getSiddhiAppConfig().removeWindowFilterProjectionQuery(elementId);
                } else if (newElement.hasClass('functionQueryDrop')) {
                    self.configurationData.getSiddhiAppConfig().removeWindowFilterProjectionQuery(elementId);
                } else if (newElement.hasClass('patternQueryDrop')) {
                    self.configurationData.getSiddhiAppConfig().removePatternQuery(elementId);
                } else if (newElement.hasClass('sequenceQueryDrop')) {
                    self.configurationData.getSiddhiAppConfig().removeSequenceQuery(elementId);
                } else if (newElement.hasClass('joinQueryDrop')) {
                    self.configurationData.getSiddhiAppConfig().removeJoinQuery(elementId);
                } else if (newElement.hasClass('sourceDrop')) {
                    self.configurationData.getSiddhiAppConfig().removeSource(elementId);
                } else if (newElement.hasClass('sinkDrop')) {
                    self.configurationData.getSiddhiAppConfig().removeSink(elementId);
                }

                self.configurationData.getSiddhiAppConfig()
                    .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount() - 1);
                $(".overlayed-container ").fadeOut(200);
                $(this).parents(".popover").popover('hide');
            }

            function deletePartition() {
                // set the isDesignViewContentChanged to true
                self.configurationData.setIsDesignViewContentChanged(true);

                var elementId = newElement[0].id;
                var partition = self.configurationData.getSiddhiAppConfig().getPartition(elementId);
                var noOfElementsInsidePartition = partition.getNoOfElementsInPartition();
                var partitionConnectionPoints = newElement.find('.partition-connector-in-part');

                _.forEach(partitionConnectionPoints, function (partitionConnectionPoint) {
                    var outConnections = self.jsPlumbInstance.getConnections({source: partitionConnectionPoint});
                    var inConnections = self.jsPlumbInstance.getConnections({target: partitionConnectionPoint});

                    _.forEach(outConnections, function (connection) {
                        self.jsPlumbInstance.deleteConnection(connection);
                    });
                    _.forEach(inConnections, function (connection) {
                        self.jsPlumbInstance.deleteConnection(connection);
                    });
                });
                /*
                * before deleting the element data from the data store structure, it is mandatory to delete the element
                * from jsPlumb.
                * */
                self.jsPlumbInstance.remove(newElement);
                if (self.jsPlumbInstance.getGroupFor(newElement)) {
                    self.jsPlumbInstance.removeFromGroup(newElement);
                }
                if (newElement.hasClass('partitionDrop')) {
                    self.configurationData.getSiddhiAppConfig().removePartition(elementId);
                }
                self.configurationData.getSiddhiAppConfig()
                    .setFinalElementCount(self.configurationData.getSiddhiAppConfig().getFinalElementCount()
                        - noOfElementsInsidePartition);
            }
        };

        return DropElements;
    });
