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

define(['require', 'log', 'jquery', 'lodash', 'jsplumb', 'stream', 'leftStream', 'rightStream', 'join'],
    function (require, log, $, _, jsPlumb, Stream, LeftStream, RightStream, Join) {

        // common properties for the JSON editor
        JSONEditor.defaults.options.theme = 'bootstrap3';
        //JSONEditor.defaults.options.iconlib = 'bootstrap3';
        JSONEditor.defaults.options.disable_edit_json = true;
        JSONEditor.plugins.sceditor.emoticonsEnabled = true;
        JSONEditor.defaults.options.disable_collapse = true;
        JSONEditor.plugins.selectize.enable = true;

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
         * @class AppData
         * @constructor
         * @class DesignView  Wraps the Ace editor for design view
         * @param {Object} options Rendering options for the view
         */
        var FormBuilder = function (options) {
            this.appData = options.appData;
            this.dropElements = options.dropElements;
            this.application = options.application;
            this.consoleListManager = options.application.outputController;
            this.gridContainer = $("#grid-container");
            this.toolPaletteContainer = $("#tool-palette-container");
        };

        //TODO: input validation in forms

        /**
         * @function generate a tab in the output console to view the form
         * @returns newly created formConsole
         */
        FormBuilder.prototype.createTabForForm = function () {
            var activeTab = this.application.tabController.getActiveTab();
            var siddhiAppName = "";

            if(activeTab.getTitle().lastIndexOf(".siddhi") !== -1){
                siddhiAppName = activeTab.getTitle().substring(0, activeTab.getTitle().lastIndexOf(".siddhi"));
            } else{
                siddhiAppName = activeTab.getTitle();
            }

            var uniqueTabId = 'form-' + activeTab.cid;
            var consoleOptions = {};
            var options = {};
            _.set(options, '_type', "FORM");
            _.set(options, 'title', "Form");
            _.set(options, 'uniqueTabId', uniqueTabId);
            _.set(options, 'appName', siddhiAppName);

            var console = this.consoleListManager.getGlobalConsole();

            if(console === undefined){
                var globalConsoleOptions = {};
                var opts = {};
                _.set(opts, '_type', "CONSOLE");
                _.set(opts, 'title', "Console");
                _.set(opts, 'currentFocusedFile', siddhiAppName);
                _.set(opts, 'statusForCurrentFocusedFile', "SUCCESS");
                _.set(opts, 'message', "");
                _.set(globalConsoleOptions, 'consoleOptions', opts);
                console = this.consoleListManager.newConsole(globalConsoleOptions);
            }

            _.set(options, 'consoleObj', console);
            _.set(consoleOptions, 'consoleOptions', options);
            return this.consoleListManager.newFormConsole(consoleOptions);
        };

        /**
         * @function generate the form to define the stream once it is dropped on the canvas
         * @param i id for the element
         * @returns user given stream name
         */
        FormBuilder.prototype.DefineStream = function (i) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();
            var propertyDiv = $('<div id="property-header"><h3>Define Stream </h3></div>' +
                '<div id="define-stream" class="define-stream"></div>');
            formContainer.append(propertyDiv);
            var streamElement = $("#define-stream")[0];

            // generate the form to define a stream
            var editor = new JSONEditor(streamElement, {
                schema: {
                    type: "object",
                    title: "Stream",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name"
                        },
                        attributes: {
                            type: "array",
                            format: "table",
                            title: "Attributes",
                            uniqueItems: true,
                            items: {
                                type: "object",
                                title : 'Attribute',
                                properties: {
                                    attribute: {
                                        type: "string"
                                    },
                                    type: {
                                        type: "string",
                                        enum: [
                                            "string",
                                            "int",
                                            "long",
                                            "float",
                                            "double",
                                            "boolean"
                                        ],
                                        default: "string"
                                    }
                                }
                            }
                        }
                    }
                },
                disable_properties: true,
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true
            });

            formContainer.append('<div id="submit"><button type="button" class="btn btn-default">Submit</button></div>');

            // 'Submit' button action
            var submitButtonElement = $('#submit')[0];
            submitButtonElement.addEventListener('click', function () {
                // add the new out stream to the stream array
                var streamOptions = {};
                _.set(streamOptions, 'id', i);
                _.set(streamOptions, 'define', editor.getValue().name);
                _.set(streamOptions, 'type', 'define-stream');
                _.set(streamOptions, 'attributes', editor.getValue().attributes);
                var stream = new Stream(streamOptions);
                self.appData.AddStream(stream);

                // close the form window
                self.consoleListManager.removeConsole(formConsole);

                self.gridContainer.removeClass("disabledbutton");
                self.toolPaletteContainer.removeClass("disabledbutton");
            });
            return editor.getValue().name;
        };

        /**
         * @function generate the property window for an existing stream
         * @param element selected element(stream)
         */
        FormBuilder.prototype.GeneratePropertiesFormForStreams = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            // The container and the tool palette are disabled to prevent the user from dropping any elements
            self.gridContainer.addClass("disabledbutton");
            self.toolPaletteContainer.addClass("disabledbutton");

            var id = parseInt($(element).parent().attr('id'));
            // retrieve the stream information from the collection
            var clickedElement = self.appData.getStream(id);
            if(clickedElement === undefined) {
                var errorMessage = 'unable to find clicked element';
                log.error(errorMessage);
            }
            var name = clickedElement.getDefine();
            var attributes = clickedElement.getAttributes();
            var fillWith = {
                name : name,
                attributes : attributes
            };
            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Stream",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name"
                        },
                        attributes: {
                            type: "array",
                            format: "table",
                            title: "Attributes",
                            uniqueItems: true,
                            items: {
                                type: "object",
                                properties: {
                                    attribute: {
                                        type: "string"
                                    },
                                    type: {
                                        type: "string",
                                        enum: [
                                            "string",
                                            "int",
                                            "long",
                                            "float",
                                            "double",
                                            "boolean"
                                        ],
                                        default: "string"
                                    }
                                }
                            }
                        }
                    }
                },
                disable_properties: true,
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                startval: fillWith
            });
            $(formContainer).append('<div id="form-submit"><button type="button" ' +
                'class="btn btn-default">Submit</button></div>' +
                '<div id="form-cancel"><button type="button" class="btn btn-default">Cancel</button></div>');

            // 'Submit' button action
            var submitButtonElement = $('#form-submit')[0];
            submitButtonElement.addEventListener('click', function () {
                // The container and the palette are disabled to prevent the user from dropping any elements
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                var config = editor.getValue();

                // update selected stream model
                clickedElement.setDefine(config.name);
                clickedElement.setAttributes(config.attributes);

                var textNode = $(element).parent().find('.streamnamenode');
                textNode.html(config.name);

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
            });

            // 'Cancel' button action
            var cancelButtonElement = $('#form-cancel')[0];
            cancelButtonElement.addEventListener('click', function () {
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
            });
        };

        /**
         * @function generate the property window for the simple queries ( passthrough, filter and window)
         * @param element selected element(query)
         */
        FormBuilder.prototype.GeneratePropertiesFormForQueries = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            // The container and the tool palette are disabled to prevent the user from dropping any elements
            self.gridContainer.addClass('disabledbutton');
            self.toolPaletteContainer.addClass('disabledbutton');

            var id = parseInt($(element).parent().attr('id'));
            var clickedElement = self.appData.getQuery(id);
            if(clickedElement === undefined) {
                var errorMessage = 'unable to find clicked element';
                log.error(errorMessage);
            }
            var type= $(element).parent();
            if (!(clickedElement.getFrom())) {
                alert('Connect an input stream');
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
            }
            else if (!(clickedElement.getInsertInto())) {
                // retrieve the query information from the collection
                var name = clickedElement.getName();
                var inStream = (self.appData.getStream(clickedElement.getFrom())).getDefine();
                var filter1 = clickedElement.getFilter();
                var window = clickedElement.getWindow();
                var filter2 = clickedElement.getPostWindowFilter();
                var fillWith;
                if (type.hasClass(constants.PASS_THROUGH)) {
                    fillWith = {
                        name: name,
                        from: inStream,
                        projection: ''
                    };
                } else if (type.hasClass(constants.FILTER)) {
                    fillWith = {
                        name: name,
                        from: inStream,
                        filter: filter1,
                        projection: ''
                    };
                } else if (type.hasClass(constants.WINDOW_QUERY)) {
                    fillWith = {
                        name: name,
                        from: inStream,
                        filter: filter1,
                        window: window,
                        postWindowFilter: filter2,
                        projection: ''
                    };
                }
                // generate the form for the query an output stream is not connected
                var editor = new JSONEditor(formContainer[0], {
                    schema: {
                        type: 'object',
                        title: 'Query',
                        properties: {
                            name: {
                                type: 'string',
                                title: 'Name',
                                required: true,
                                propertyOrder: 1
                            },
                            from: {
                                type: 'string',
                                title: 'From',
                                required: true,
                                propertyOrder: 2
                            },
                            filter: {
                                type: 'string',
                                title: 'Filter',
                                propertyOrder: 3
                            },
                            window: {
                                type: 'string',
                                title: 'Window',
                                propertyOrder: 4
                            },
                            postWindowFilter: {
                                type: 'string',
                                title: 'Post Window Filter',
                                propertyOrder: 5
                            },
                            outputType: {
                                type: 'string',
                                title: 'Output Type',
                                enum : ['all events' , 'current events' , 'expired events'],
                                default: 'all events',
                                propertyOrder: 6,
                                required: true
                            },
                            insertInto: {
                                type: 'string',
                                title: 'Output Stream',
                                propertyOrder: 7,
                                required: true
                            },
                            projection: {
                                type: 'array',
                                title: 'Attributes',
                                format: 'table',
                                required: true,
                                propertyOrder: 8,
                                uniqueItems: true,
                                items: {
                                    type: 'object',
                                    title : 'Attribute',
                                    properties: {
                                        select: {
                                            type: 'string',
                                            title: 'select'
                                        },
                                        newName: {
                                            type: 'string',
                                            title: 'as'
                                        },
                                        type: {
                                            type: "string",
                                            enum: [
                                                "string",
                                                "int",
                                                "long",
                                                "float",
                                                "double",
                                                "boolean"
                                            ],
                                            default: "string"
                                        }
                                    }
                                }
                            }
                        }
                    },
                    startval: fillWith,
                    disable_array_add: false,
                    disable_array_delete: false,
                    disable_array_reorder: true,
                    display_required_only: true,
                    no_additional_properties: true
                });
                // disable the uneditable fields
                editor.getEditor('root.from').disable();

                $(formContainer).append('<div id="form-submit"><button type="button" ' +
                    'class="btn btn-default">Submit</button></div>' +
                    '<div id="form-cancel"><button type="button" class="btn btn-default">Cancel</button></div>');

                // 'Submit' button action
                var submitButtonElement = $('#form-submit')[0];
                submitButtonElement.addEventListener('click', function () {
                    self.gridContainer.removeClass('disabledbutton');
                    self.toolPaletteContainer.removeClass('disabledbutton');

                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);

                    var config = editor.getValue();

                    // change the query icon depending on the fields filled
                    if (config.window) {
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.WINDOW_QUERY + ' jtk-draggable');
                    } else if (config.filter || config.postWindowFilter) {
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.FILTER + ' jtk-draggable');
                    } else if (!(config.filter || config.postWindowFilter || config.window )) {
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.PASS_THROUGH+ ' jtk-draggable');
                    }

                    // obtain values from the form and update the query model
                    var config = editor.getValue();
                    clickedElement.setName(config.name);
                    clickedElement.setFilter(config.filter);
                    clickedElement.setWindow(config.window);
                    clickedElement.setPostWindowFilter(config.postWindowFilter);
                    clickedElement.setOutputType(config.outputType);
                    var streamAttributes = [];
                    var queryAttributes = [];
                    $.each( config.projection, function(key, value ) {
                        streamAttributes.push({ attribute : value.newName , type : value.type});
                        queryAttributes.push(value.select);
                    });
                    clickedElement.setProjection(queryAttributes);
                    var textNode = $(element).parent().find('.queryNameNode');
                    textNode.html(config.name);
                    // generate the stream defined as the output stream
                    var position = $(element).parent().position();
                    //TODO: check whether it is needed to add a stream since there might be a stream with that name alredy existed
                    self.dropElements.dropStreamFromQuery(position, id, config.insertInto, streamAttributes);
                });

                // 'Cancel' button action
                var cancelButtonElement = $('#form-cancel')[0];
                cancelButtonElement.addEventListener('click', function () {
                    self.gridContainer.removeClass('disabledbutton');
                    self.toolPaletteContainer.removeClass('disabledbutton');
                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);
                });
            } else {
                // retrieve the query information from the collection
                var name = clickedElement.getName();
                var inStream = (self.appData.getStream(clickedElement.getFrom())).getDefine();
                var outStream = (self.appData.getStream(clickedElement.getInsertInto())).getDefine();
                var filter1 = clickedElement.getFilter();
                var window = clickedElement.getWindow();
                var filter2 = clickedElement.getPostWindowFilter();
                var outputType = clickedElement.getOutputType();
                var attrString = [];
                var outStreamAttributes = (self.appData.getStream(clickedElement.getInsertInto())).getAttributes();
                if (clickedElement.getProjection() === '') {
                    for (var i = 0; i < outStreamAttributes.length; i++) {
                        var attr = {select: '', newName: outStreamAttributes[i].attribute};
                        attrString.push(attr);
                    }
                } else {
                    var selectedAttributes = clickedElement.getProjection();
                    for (var i = 0; i < outStreamAttributes.length; i++) {
                        var attr = {select: selectedAttributes[i], newName: outStreamAttributes[i].attribute};
                        attrString.push(attr);
                    }
                }

                var fillWith;
                if (type.hasClass(constants.PASS_THROUGH)) {
                    fillWith = {
                        name: name,
                        from: inStream,
                        projection: attrString,
                        outputType :outputType,
                        insertInto: outStream
                    };
                } else if (type.hasClass(constants.FILTER)) {
                    fillWith = {
                        name: name,
                        from: inStream,
                        filter: filter1,
                        projection: attrString,
                        outputType :outputType,
                        insertInto: outStream
                    };
                } else if (type.hasClass(constants.WINDOW_QUERY)) {
                    fillWith = {
                        name: name,
                        from: inStream,
                        filter : filter1,
                        window: window,
                        pstWindowFilter : filter2,
                        projection: attrString,
                        outputType :outputType,
                        insertInto: outStream
                    };
                }
                // generate form for the queries where both input and output streams are defined
                var editor = new JSONEditor(formContainer[0], {
                    schema: {
                        type: 'object',
                        title: 'Query',
                        properties: {
                            name: {
                                type: 'string',
                                title: 'Name',
                                required: true,
                                propertyOrder: 1
                            },
                            from: {
                                type: 'string',
                                title: 'From',
                                template: inStream,
                                required: true,
                                propertyOrder: 2
                            },
                            filter: {
                                type: 'string',
                                title: 'Filter',
                                propertyOrder: 3
                            },
                            window: {
                                type: 'string',
                                title: 'Window',
                                propertyOrder: 4
                            },
                            postWindowQuery: {
                                type: 'string',
                                title: 'Post Window Filter',
                                propertyOrder: 5
                            },
                            projection: {
                                type: 'array',
                                title: 'Attributes',
                                format: 'table',
                                required: true,
                                propertyOrder: 6,
                                items: {
                                    type: 'object',
                                    title : 'Attribute',
                                    properties: {
                                        select: {
                                            type: 'string',
                                            title: 'select'
                                        },
                                        newName: {
                                            type: 'string',
                                            title: 'as'
                                        }
                                    }
                                }
                            },
                            outputType: {
                                type: 'string',
                                title: 'Output Type',
                                enum : ['all events' , 'current events' , 'expired events'],
                                default: 'all events',
                                required : true,
                                propertyOrder: 7
                            },
                            insertInto: {
                                type: 'string',
                                template: outStream,
                                required: true,
                                propertyOrder: 8
                            }
                        }
                    },
                    startval: fillWith,
                    disable_array_add: true,
                    disable_array_delete: true,
                    disable_array_reorder: true,
                    display_required_only: true,
                    no_additional_properties: true
                });

                // disable fields that can not be changed
                editor.getEditor('root.from').disable();
                editor.getEditor('root.insertInto').disable();
                for (var i = 0; i < outStreamAttributes.length; i++) {
                    editor.getEditor('root.projection.' + i + '.newName').disable();
                }

                $(formContainer).append('<div id="form-submit"><button type="button" ' +
                    'class="btn btn-default">Submit</button></div>' +
                    '<div id="form-cancel"><button type="button" class="btn btn-default">Cancel</button></div>');

                // 'Submit' button action
                var submitButtonElement = $('#form-submit')[0];
                submitButtonElement.addEventListener('click', function () {
                    self.gridContainer.removeClass('disabledbutton');
                    self.toolPaletteContainer.removeClass('disabledbutton');

                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);
                    var config = editor.getValue();

                    // change the query icon depending on the fields(filter, window) filled
                    if (config.window) {
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.WINDOW_QUERY + ' jtk-draggable');
                    } else if (config.filter || config.postWindowFilter) {
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.FILTER + ' jtk-draggable');
                    } else if (!(config.filter || config.postWindowFilter || config.window )) {
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.PASS_THROUGH + ' jtk-draggable');
                    }

                    // update selected query model
                    clickedElement.setName(config.name);
                    clickedElement.setFilter(config.filter);
                    clickedElement.setWindow(config.window);
                    clickedElement.setPostWindowFilter(config.postWindowFilter);
                    var projections = [];
                    $.each(config.projection, function (index, attribute) {
                        projections.push(attribute.select);
                    });
                    clickedElement.setProjection(projections);
                    clickedElement.setOutputType(config.outputType);
                    var textNode = $(element).parent().find('.queryNameNode');
                    textNode.html(config.name);
                });

                // 'Cancel' button action
                var cancelButtonElement = $('#form-cancel')[0];
                cancelButtonElement.addEventListener('click', function () {
                    self.gridContainer.removeClass('disabledbutton');
                    self.toolPaletteContainer.removeClass('disabledbutton');
                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);
                });
            }
        };

        /**
         * @function generate property window for state machine
         * @param element selected element(query)
         */
        FormBuilder.prototype.GeneratePropertiesFormForPattern = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            // The container and the tool palette are disabled to prevent the user from dropping any elements
            self.gridContainer.addClass('disabledbutton');
            self.toolPaletteContainer.addClass('disabledbutton');

            var id = parseInt($(element).parent().attr('id'));
            var clickedElement = self.appData.getPatternQuery(id);
            if (clickedElement.getFrom().length === 0) {
                alert('Connect input streams');
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
            } else if (clickedElement.getInsertInto() === '') {
                alert('Connect an output stream');
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
            } else {
                // retrieve the pattern information from the collection
                var streams = [];
                var projections = [];
                $.each(clickedElement.getFrom(), function (index, streamID) {
                    streams.push((self.appData.getStream(streamID)).getDefine());
                });

                var insertInto = self.appData.getStream(clickedElement.getInsertInto()).getDefine();
                var outStreamAttributes = (self.appData.getStream(clickedElement.getInsertInto())).getAttributes();
                if (clickedElement.getProjection() === '') {
                    for (var i = 0; i < outStreamAttributes.length; i++) {
                        var attr = {select: '', newName: outStreamAttributes[i].attribute};
                        projections.push(attr);
                    }
                }
                else {
                    var selectedAttributes = clickedElement.getProjection();
                    for (var i = 0; i < outStreamAttributes.length; i++) {
                        var attr = {select: selectedAttributes[i], newName: outStreamAttributes[i].attribute};
                        projections.push(attr);
                    }
                }
                var states = [];
                $.each(clickedElement.getStates(), function (index, state) {
                    for (var event in state) {
                        if (state.hasOwnProperty(event)) {
                            var restoredState = { stateID: event, stream: state[event].stream,
                                filter: state[event].filter};
                            states.push(restoredState);
                        }
                    }
                });

                var fillWith = {
                    name: clickedElement.getName(),
                    states: states,
                    logic: clickedElement.getLogic(),
                    outputType: clickedElement.getOutputType(),
                    projection: projections
                };
                if (clickedElement.getFilter()) {
                    fillWith.filter = clickedElement.getFilter();
                }
                if (clickedElement.getWindow()) {
                    fillWith.window = clickedElement.getWindow();
                }
                if (clickedElement.getPostWindowFilter()) {
                    fillWith.postWindowFilter = clickedElement.getPostWindowFilter();
                }
                if (clickedElement.getHaving()) {
                    fillWith.having = clickedElement.getHaving();
                }
                if (clickedElement.getGroupBy()) {
                    fillWith.groupBy = clickedElement.getGroupBy();
                }
                var editor = new JSONEditor(formContainer[0], {
                    ajax: true,
                    schema: {
                        type: 'object',
                        title: 'Query',
                        properties: {
                            name: {
                                type: 'string',
                                title: 'Name',
                                required: true,
                                propertyOrder: 1
                            },
                            states: {
                                type: 'array',
                                title: 'State',
                                format: 'tabs',
                                uniqueItems: true,
                                required: true,
                                propertyOrder: 2,
                                items: {
                                    type: 'object',
                                    title: 'state',
                                    headerTemplate: "State" + "{{i1}}",
                                    options: {
                                        disable_properties: true
                                    },
                                    properties: {
                                        stateID: {
                                            type: 'string',
                                            title: 'State ID',
                                            required: true,
                                            propertyOrder: 1
                                        },
                                        stream: {
                                            type: 'string',
                                            title: 'Stream',
                                            enum: streams,
                                            required: true,
                                            propertyOrder: 2
                                        },
                                        filter: {
                                            type: 'string',
                                            title: 'Filter',
                                            required: true,
                                            propertyOrder: 3
                                        }
                                    }
                                }
                            },
                            logic: {
                                type: 'string',
                                title: 'Logic',
                                required: true,
                                propertyOrder: 3
                            },
                            filter: {
                                type: 'string',
                                title: 'Filter',
                                propertyOrder: 4
                            },
                            window: {
                                type: 'string',
                                title: 'Window',
                                propertyOrder: 5
                            },
                            postWindowFilter: {
                                type: 'string',
                                title: 'Post Window Filter',
                                propertyOrder: 6
                            },
                            having: {
                                type: 'string',
                                title: 'Having',
                                propertyOrder: 7
                            },
                            groupBy: {
                                type: 'string',
                                title: 'Group By',
                                propertyOrder: 8
                            },
                            projection: {
                                type: 'array',
                                title: 'Projection',
                                format: 'table',
                                required: true,
                                propertyOrder: 9,
                                options: {
                                    disable_array_add: true,
                                    disable_array_delete: true
                                },
                                items: {
                                    type: 'object',
                                    title: 'Attribute',
                                    properties: {
                                        select: {
                                            type: 'string',
                                            title: 'select'
                                        },
                                        newName: {
                                            type: 'string',
                                            title: 'as'
                                        }
                                    }
                                }
                            },
                            outputType: {
                                type: 'string',
                                title: 'Output Type',
                                enum: ['all events', 'current events', 'expired events'],
                                default: 'all events',
                                required: true,
                                propertyOrder: 10
                            },
                            insertInto: {
                                type: 'string',
                                title: 'Insert Into',
                                template: insertInto,
                                required: true,
                                propertyOrder: 11
                            }
                        }
                    },
                    startval: fillWith,
                    disable_properties: false,
                    display_required_only: true,
                    no_additional_properties: true,
                    disable_array_delete_all_rows: true,
                    disable_array_delete_last_row: true,
                    disable_array_reorder: true
                });
                // disable fields that can not be changed
                editor.getEditor('root.insertInto').disable();

                for (var i = 0; i < outStreamAttributes.length; i++) {
                    editor.getEditor('root.projection.' + i + '.newName').disable();
                }
                $(formContainer).append('<div id="form-submit"><button type="button" ' +
                    'class="btn btn-default">Submit</button></div>' +
                    '<div id="form-cancel"><button type="button" class="btn btn-default">Cancel</button></div>');

                // 'Submit' button action
                var submitButtonElement = $('#form-submit')[0];
                submitButtonElement.addEventListener('click', function () {
                    self.gridContainer.removeClass('disabledbutton');
                    self.toolPaletteContainer.removeClass('disabledbutton');

                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);

                    var config = editor.getValue();

                    // update selected query model
                    clickedElement.setName(config.name);
                    clickedElement.setLogic(config.logic);
                    clickedElement.setFilter(config.filter);
                    clickedElement.setWindow(config.window);
                    clickedElement.setPostWindowFilter(config.postWindowFilter);
                    clickedElement.setHaving(config.having);
                    clickedElement.setGroupBy(config.groupBy);
                    var states = [];
                    $.each(config.states, function (index, state) {
                        var stateID = state.stateID;
                        var stream = state.stream;
                        var filter = state.filter;
                        var stateObject = {};
                        stateObject[stateID] = {stream: stream, filter: filter};
                        states.push(stateObject);
                    });
                    clickedElement.setStates(states);
                    var projections = [];
                    $.each(config.projection, function (index, attribute) {
                        projections.push(attribute.select);
                    });
                    clickedElement.setProjection(projections);
                    clickedElement.setOutputType(config.outputType);
                    var textNode = $(element).parent().find('.queryNameNode');
                    textNode.html(config.name);
                });

                // 'Cancel' button action
                var cancelButtonElement = $('#form-cancel')[0];
                cancelButtonElement.addEventListener('click', function () {
                    self.gridContainer.removeClass('disabledbutton');
                    self.toolPaletteContainer.removeClass('disabledbutton');
                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);
                });
            }
        };

        /**
         * @function generate property window for Join Query
         * @param element selected element(query)
         */
        FormBuilder.prototype.GeneratePropertiesFormForJoinQuery = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            // The container and the tool palette are disabled to prevent the user from dropping any elements
            self.gridContainer.addClass('disabledbutton');
            self.toolPaletteContainer.addClass('disabledbutton');

            var id = parseInt($(element).parent().attr('id'));
            var clickedElement = self.appData.getJoinQuery(id);
            if (!(clickedElement.getFrom()) || clickedElement.getFrom().length !== 2) {
                alert('Connect TWO input streams');
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
            }
            else if (!(clickedElement.getInsertInto())) {
                alert('Connect an output stream');
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
            }

            else {
                var streams = [];
                $.each(clickedElement.getFrom(), function (index, streamID) {
                    streams.push((self.appData.getStream(streamID)).getDefine());
                });
                var projections = [];
                var insertInto = self.appData.getStream(clickedElement.getInsertInto()).getDefine();
                var outStreamAttributes = (self.appData.getStream(clickedElement.getInsertInto())).getAttributes();
                if (!(clickedElement.getProjection())) {
                    for (var i = 0; i < outStreamAttributes.length; i++) {
                        var attr = {select: '', newName: outStreamAttributes[i].attribute};
                        projections.push(attr);
                    }
                }
                else {
                    var selectedAttributes = clickedElement.getProjection();
                    for (var i = 0; i < outStreamAttributes.length; i++) {
                        var attr = {select: selectedAttributes[i], newName: outStreamAttributes[i].attribute};
                        projections.push(attr);
                    }
                }
                var fillWith = {
                    projection: projections,
                    outputType: clickedElement.getOutputType(),
                    insertInto: insertInto
                };

                if (clickedElement.getJoin()) {
                    fillWith.type = clickedElement.getJoin().getType();
                    fillWith.leftStream = clickedElement.getJoin().getLeftStream();
                    fillWith.rightStream = clickedElement.getJoin().getRightStream();
                    fillWith.on = clickedElement.getJoin().getOn();
                }
                var editor = new JSONEditor(formContainer[0], {
                    ajax: true,
                    schema: {
                        type: 'object',
                        title: 'Query',
                        properties: {
                            type: {
                                type: 'string',
                                title: 'Type',
                                required: true,
                                propertyOrder: 1
                            },
                            leftStream: {
                                type: 'object',
                                title: 'Left Stream',
                                required: true,
                                propertyOrder: 2,
                                options: {
                                    disable_collapse: false,
                                    disable_properties: true,
                                    collapsed: true
                                },
                                properties: {
                                    from: {
                                        type: 'string',
                                        title: 'From',
                                        enum: streams,
                                        required: true
                                    },
                                    filter: {
                                        type: 'string',
                                        title: 'Filter',
                                        required: true
                                    },
                                    window: {
                                        type: 'string',
                                        title: 'Window',
                                        required: true
                                    },
                                    postWindowFilter: {
                                        type: 'string',
                                        title: 'Post Window Filter',
                                        required: true
                                    },
                                    as: {
                                        type: 'string',
                                        title: 'As',
                                        required: true
                                    }
                                }
                            },
                            rightStream: {
                                type: 'object',
                                title: 'Right Stream',
                                required: true,
                                propertyOrder: 3,
                                options: {
                                    disable_collapse: false,
                                    disable_properties: true,
                                    collapsed: true
                                },
                                properties: {
                                    from: {
                                        type: 'string',
                                        title: 'From',
                                        enum: streams,
                                        required: true
                                    },
                                    filter: {
                                        type: 'string',
                                        title: 'Filter',
                                        required: true
                                    },
                                    window: {
                                        type: 'string',
                                        title: 'Window',
                                        required: true
                                    },
                                    postWindowFilter: {
                                        type: 'string',
                                        title: 'Post Window Filter',
                                        required: true
                                    },
                                    as: {
                                        type: 'string',
                                        title: 'As',
                                        required: true
                                    }
                                }
                            },
                            on: {
                                type: 'string',
                                title: 'On',
                                required: true,
                                propertyOrder: 4
                            },
                            projection: {
                                type: 'array',
                                title: 'Projection',
                                format: 'table',
                                required: true,
                                propertyOrder: 5,
                                options: {
                                    disable_array_add: true,
                                    disable_array_delete: true,
                                    disable_array_reorder: true
                                },
                                items: {
                                    type: 'object',
                                    title: 'Attribute',
                                    properties: {
                                        select: {
                                            type: 'string',
                                            title: 'select'
                                        },
                                        newName: {
                                            type: 'string',
                                            title: 'as'
                                        }
                                    }
                                }
                            },
                            outputType: {
                                type: 'string',
                                title: 'Output Type',
                                enum: ['all events', 'current events', 'expired events'],
                                default: 'all events',
                                required: true,
                                propertyOrder: 6
                            },
                            insertInto: {
                                type: 'string',
                                title: 'Insert Into',
                                template: insertInto,
                                required: true,
                                propertyOrder: 7
                            }
                        }
                    },
                    startval: fillWith,
                    no_additional_properties: true,
                    disable_properties: true
                });
                for (var i = 0; i < outStreamAttributes.length; i++) {
                    editor.getEditor('root.projection.' + i + '.newName').disable();
                }
                editor.getEditor('root.insertInto').disable();

                $(formContainer).append('<div id="form-submit"><button type="button" ' +
                    'class="btn btn-default">Submit</button></div>' +
                    '<div id="form-cancel"><button type="button" class="btn btn-default">Cancel</button></div>');

                // 'Submit' button action
                var submitButtonElement = $('#form-submit')[0];
                submitButtonElement.addEventListener('click', function () {
                    self.gridContainer.removeClass('disabledbutton');
                    self.toolPaletteContainer.removeClass('disabledbutton');

                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);

                    var config = editor.getValue();
                    // update selected query object
                    var leftStreamOptions = {};
                    _.set(leftStreamOptions, 'from', config.leftStream.from);
                    _.set(leftStreamOptions, 'filter', config.leftStream.filter);
                    _.set(leftStreamOptions, 'window', config.leftStream.window);
                    _.set(leftStreamOptions, 'postWindowFilter', config.leftStream.postWindowFilter);
                    _.set(leftStreamOptions, 'as', config.leftStream.as);
                    var leftStream = new LeftStream(leftStreamOptions);

                    var rightStreamOptions = {};
                    _.set(leftStreamOptions, 'from', config.rightStream.from);
                    _.set(leftStreamOptions, 'filter', config.rightStream.filter);
                    _.set(leftStreamOptions, 'window', config.rightStream.window);
                    _.set(leftStreamOptions, 'postWindowFilter', config.rightStream.postWindowFilter);
                    _.set(leftStreamOptions, 'as', config.rightStream.as);
                    var rightStream = new RightStream(rightStreamOptions);

                    var joinOptions = {};
                    _.set(joinOptions, 'type', config.type);
                    _.set(joinOptions, 'left-stream', leftStream);
                    _.set(joinOptions, 'right-stream', rightStream);
                    _.set(joinOptions, 'on', config.on);
                    var join =new Join(joinOptions);

                    clickedElement.setJoin(join);
                    var projections = [];
                    $.each(config.projection, function (index, attribute) {
                        projections.push(attribute.select);
                    });
                    clickedElement.setProjection(projections);
                    clickedElement.setOutputType(config.outputType);
                });

                // 'Cancel' button action
                var cancelButtonElement = $('#form-cancel')[0];
                cancelButtonElement.addEventListener('click', function () {
                    self.gridContainer.removeClass('disabledbutton');
                    self.toolPaletteContainer.removeClass('disabledbutton');

                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);
                });
            }
        };

        /**
         * @function generate property window for Partition
         * @param element selected element(query)
         */
        FormBuilder.prototype.GeneratePartitionKeyForm = function (element) {
            var self = this;
            var id =parseInt($(element.target).parent().attr('id'));
            var partition = self.appData.getPartition(id);
            var connections = jsPlumb.getConnections(element);
            var connected= false;
            var connectedStream = null;
            $.each(connections, function (index, connection) {
                var target = connection.targetId;
                if(target.substr(0, target.indexOf('-')) == id){
                    connected = true;
                    var source = connection.sourceId;
                    connectedStream = source.substr(0, source.indexOf('-'));
                }
            });
            if(!(connected)){
                alert('Connect a stream for partitioning');
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');
            }
            else{
                var fillWith= {};
                var partitionKeys = partition.getPartition().with;
                $.each(partitionKeys, function ( index , key) {
                    if( key.stream == connectedStream){
                        fillWith ={
                            stream : (self.appData.getStream(connectedStream)).getDefine(),
                            property : key.property
                        }
                    }
                });

                var formConsole = this.createTabForForm();
                var formContainer = formConsole.getContentContainer();
                self.gridContainer.addClass('disabledbutton');
                self.toolPaletteContainer.addClass('disabledbutton');

                var editor = new JSONEditor(formContainer[0], {
                    ajax: true,
                    schema: {
                        type: 'object',
                        title: 'Partition Key',
                        properties: {
                            stream: {
                                type: 'string',
                                title: 'Stream',
                                required: true,
                                propertyOrder: 1,
                                template: (self.appData.getStream(connectedStream)).getDefine()
                            },
                            property: {
                                type: 'string',
                                title: 'Property',
                                required: true,
                                propertyOrder: 2
                            }
                        }
                    },
                    startval: fillWith,
                    disable_properties: true
                });
                $(formContainer).append('<div id="form-submit"><button type="button" ' +
                    'class="btn btn-default">Submit</button></div>' +
                    '<div id="form-cancel"><button type="button" class="btn btn-default">Cancel</button></div>');

                // 'Submit' button action
                var submitButtonElement = $('#form-submit')[0];
                submitButtonElement.addEventListener('click', function () {
                    self.gridContainer.removeClass('disabledbutton');
                    self.toolPaletteContainer.removeClass('disabledbutton');

                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);

                    var config = editor.getValue();
                    $.each(partitionKeys, function ( index , key) {
                        if( key.stream == connectedStream){
                            key.property = config.property
                        }
                        else {
                            var key = { stream : connectedStream , property : config.property};
                            partitionKeys['with'].push(key);
                        }
                    });
                });
                // 'Cancel' button action
                var cancelButtonElement = $('#form-cancel')[0];
                cancelButtonElement.addEventListener('click', function () {
                    self.gridContainer.removeClass('disabledbutton');
                    self.toolPaletteContainer.removeClass('disabledbutton');

                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);
                });
            }
        };

        return FormBuilder;
    });
