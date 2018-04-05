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

define(['require', 'log', 'jquery', 'lodash', 'jsplumb', 'stream', 'table', 'window', 'trigger', 'aggregation',
        'aggregateByTimePeriod', 'querySelect', 'leftStream', 'rightStream', 'join'],
    function (require, log, $, _, jsPlumb, Stream, Table, Window, Trigger, Aggregation, AggregateByTimePeriod,
              QuerySelect, LeftStream, RightStream, Join) {

        // common properties for the JSON editor
        JSONEditor.defaults.options.theme = 'bootstrap3';
        //JSONEditor.defaults.options.iconlib = 'bootstrap3';
        JSONEditor.defaults.options.disable_edit_json = true;
        JSONEditor.plugins.sceditor.emoticonsEnabled = true;
        JSONEditor.defaults.options.disable_collapse = true;
        JSONEditor.plugins.selectize.enable = true;

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
            PATTERN : 'stquerydrop',
            PARTITION :'partitiondrop'
        };

        /**
         * @class FormBuilder Creates forms to collect data when a element is dropped on the canvas
         * @constructor
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
         * @param elementId id of the element which form is created for
         * @param elementType type of the element
         * @returns newly created formConsole
         */
        FormBuilder.prototype.createTabForForm = function (elementId, elementType) {
            var self = this;
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
            var formConsole = this.consoleListManager.newFormConsole(consoleOptions);
            $(formConsole).on( "close-button-in-form-clicked", function() {
                if(elementType === constants.STREAM) {
                    if(self.appData.getStream(elementId) === undefined) {
                        $("#"+elementId).remove();
                    }
                }
                if(elementType === constants.TABLE) {
                    if(self.appData.getTable(elementId) === undefined) {
                        $("#"+elementId).remove();
                    }
                }
                if(elementType === constants.WINDOW) {
                    if(self.appData.getWindow(elementId) === undefined) {
                        $("#"+elementId).remove();
                    }
                }
                if(elementType === constants.TRIGGER) {
                    if(self.appData.getTrigger(elementId) === undefined) {
                        $("#"+elementId).remove();
                    }
                }
                if(elementType === constants.AGGREGATION) {
                    if(self.appData.getAggregation(elementId) === undefined) {
                        $("#"+elementId).remove();
                    }
                } //TODO: Add other types like pattern, filter
                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
                $("#grid-container").removeClass("disabledbutton");
                $("#tool-palette-container").removeClass("disabledbutton");
            });
            return formConsole;
        };

        /**
         * @function generate the form to define the stream once it is dropped on the canvas
         * @param i id for the element
         * @returns user given stream name
         */
        FormBuilder.prototype.DefineStream = function (i) {
            var self = this;
            var formConsole = this.createTabForForm(i, constants.STREAM);
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
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 1
                        },
                        attributes: {
                            required: true,
                            propertyOrder: 2,
                            type: "array",
                            format: "table",
                            title: "Attributes",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title : 'Attribute',
                                properties: {
                                    attribute: {
                                        type: "string",
                                        minLength: 1
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
                        }//, TODO: Since annotations structure is not confirmed, commenting it.
                        // annotations: {
                        //     required: true,
                        //     propertyOrder: 3,
                        //     type: 'object',
                        //     title: 'Annotations',          //@Async(buffer.size='64')
                        //     properties: {                 //@info(name='HelloWorldQuery')
                        //         Async: {
                        //             type: 'object',
                        //             title: 'Async',
                        //             annotationType: 'map',
                        //             properties: {
                        //                 prop1: {
                        //                     title: 'buffer.size',
                        //                     type: "integer",
                        //                     required: true
                        //                 }
                        //             },
                        //             propertyOrder: 1
                        //         }, //TODO: start here
                        //         Info: {
                        //             type: 'object',
                        //             title: 'Info',
                        //             annotationType: 'map',
                        //             properties: {
                        //                 prop1: {
                        //                     title: 'name',
                        //                     type: "string",
                        //                     required: true
                        //                 }
                        //             },
                        //             propertyOrder: 2
                        //         }
                        //     }
                        // }
                    }
                },
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                display_required_only: true,
                no_additional_properties: true
            });

            formContainer.append('<div id="submit"><button type="button" class="btn btn-default">Submit</button></div>');

            // 'Submit' button action
            var submitButtonElement = $('#submit')[0];
            submitButtonElement.addEventListener('click', function () {
                var isStreamNameUsed = false;
                _.forEach(self.appData.streamList, function(stream){
                    if(stream.getName().toUpperCase() === editor.getValue().name.toUpperCase()) {
                        isStreamNameUsed = true;
                    }
                });
                if(isStreamNameUsed) {
                    alert("Stream name \"" + editor.getValue().name + "\" is already used.");
                } else {
                    // add the new out stream to the stream array
                    var streamOptions = {};
                    _.set(streamOptions, 'id', i);
                    _.set(streamOptions, 'name', editor.getValue().name);
                    _.set(streamOptions, 'isInnerStream', false);
                    var stream = new Stream(streamOptions);
                    _.forEach(editor.getValue().attributes, function (attribute) {
                        stream.addAttribute(attribute);
                    });
                    self.appData.addStream(stream);

                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);
                    self.consoleListManager.hideAllConsoles();

                    self.gridContainer.removeClass("disabledbutton");
                    self.toolPaletteContainer.removeClass("disabledbutton");
                }
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

            var id = $(element).parent().attr('id');
            // retrieve the stream information from the collection
            var clickedElement = self.appData.getStream(id);
            if(clickedElement === undefined) {
                var errorMessage = 'unable to find clicked element';
                log.error(errorMessage);
            }
            var name = clickedElement.getName();
            var attributes = clickedElement.getAttributeList();
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
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 1
                        },
                        attributes: {
                            required: true,
                            propertyOrder: 2,
                            type: "array",
                            format: "table",
                            title: "Attributes",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title : 'Attribute',
                                properties: {
                                    attribute: {
                                        type: "string",
                                        minLength: 1
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
                clickedElement.setName(config.name);
                // removing all elements from attribute list
                clickedElement.getAttributeList().removeAllElements();
                // adding new attributes to the attribute list
                _.forEach(config.attributes, function(attribute){
                    clickedElement.addAttribute(attribute);
                });

                var textNode = $(element).parent().find('.streamnamenode');
                textNode.html(config.name);

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            });

            // 'Cancel' button action
            var cancelButtonElement = $('#form-cancel')[0];
            cancelButtonElement.addEventListener('click', function () {
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            });
        };

        /**
         * @function generate the form to define the table once it is dropped on the canvas
         * @param i id for the element
         * @returns user given table name
         */
        FormBuilder.prototype.DefineTable = function (i) {
            var self = this;
            var formConsole = this.createTabForForm(i, constants.TABLE);
            var formContainer = formConsole.getContentContainer();
            var propertyDiv = $('<div id="property-header"><h3>Define Table </h3></div>' +
                '<div id="define-table" class="define-table"></div>');
            formContainer.append(propertyDiv);
            var tableElement = $("#define-table")[0];

            // generate the form to define a table
            var editor = new JSONEditor(tableElement, {
                schema: {
                    type: "object",
                    title: "Table",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 1
                        },
                        attributes: {
                            required: true,
                            propertyOrder: 2,
                            type: "array",
                            format: "table",
                            title: "Attributes",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title : 'Attribute',
                                properties: {
                                    attribute: {
                                        type: "string",
                                        minLength: 1
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
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                display_required_only: true,
                no_additional_properties: true
            });

            formContainer.append('<div id="submit"><button type="button" class="btn btn-default">Submit</button></div>');

            // 'Submit' button action
            var submitButtonElement = $('#submit')[0];
            submitButtonElement.addEventListener('click', function () {
                var isTableNameUsed = false;
                _.forEach(self.appData.tableList, function(table){
                    if(table.getName().toUpperCase() === editor.getValue().name.toUpperCase()) {
                        isTableNameUsed = true;
                    }
                });
                if(isTableNameUsed) {
                    alert("Table name \"" + editor.getValue().name + "\" is already used.");
                } else {
                    // add the new out table to the table array
                    var tableOptions = {};
                    _.set(tableOptions, 'id', i);
                    _.set(tableOptions, 'name', editor.getValue().name);
                    _.set(tableOptions, 'store', '');
                    var table = new Table(tableOptions);
                    _.forEach(editor.getValue().attributes, function (attribute) {
                        table.addAttribute(attribute);
                    });
                    self.appData.addTable(table);

                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);
                    self.consoleListManager.hideAllConsoles();

                    self.gridContainer.removeClass("disabledbutton");
                    self.toolPaletteContainer.removeClass("disabledbutton");
                }
            });
            return editor.getValue().name;
        };

        /**
         * @function generate the property window for an existing table
         * @param element selected element(table)
         */
        FormBuilder.prototype.GeneratePropertiesFormForTables = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            // The container and the tool palette are disabled to prevent the user from dropping any elements
            self.gridContainer.addClass("disabledbutton");
            self.toolPaletteContainer.addClass("disabledbutton");

            var id = $(element).parent().attr('id');
            // retrieve the table information from the collection
            var clickedElement = self.appData.getTable(id);
            if(clickedElement === undefined) {
                var errorMessage = 'unable to find clicked element';
                log.error(errorMessage);
            }
            var name = clickedElement.getName();
            var attributes = clickedElement.getAttributeList();
            var fillWith = {
                name : name,
                attributes : attributes
            };
            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Table",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 1
                        },
                        attributes: {
                            required: true,
                            propertyOrder: 2,
                            type: "array",
                            format: "table",
                            title: "Attributes",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title : 'Attribute',
                                properties: {
                                    attribute: {
                                        type: "string",
                                        minLength: 1
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

                // update selected table model
                clickedElement.setName(config.name);
                // removing all elements from attribute list
                clickedElement.getAttributeList().removeAllElements();
                // adding new attributes to the attribute list
                _.forEach(config.attributes, function(attribute){
                    clickedElement.addAttribute(attribute);
                });

                var textNode = $(element).parent().find('.tablenamenode');
                textNode.html(config.name);

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            });

            // 'Cancel' button action
            var cancelButtonElement = $('#form-cancel')[0];
            cancelButtonElement.addEventListener('click', function () {
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            });
        };

        /**
         * @function generate the form to define the window once it is dropped on the canvas
         * @param i id for the element
         * @returns user given window name
         */
        FormBuilder.prototype.DefineWindow = function (i) {
            var self = this;
            var formConsole = this.createTabForForm(i, constants.WINDOW);
            var formContainer = formConsole.getContentContainer();
            var propertyDiv = $('<div id="property-header"><h3>Define Window </h3></div>' +
                '<div id="define-window" class="define-window"></div>');
            formContainer.append(propertyDiv);
            var windowElement = $("#define-window")[0];

            // generate the form to define a window
            var editor = new JSONEditor(windowElement, {
                schema: {
                    type: "object",
                    title: "Window",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 1
                        },
                        attributes: {
                            required: true,
                            propertyOrder: 2,
                            type: "array",
                            format: "table",
                            title: "Attributes",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title : 'Attribute',
                                properties: {
                                    attribute: {
                                        type: "string",
                                        minLength: 1
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
                        },
                        functionName: {
                            type: "string",
                            title: "Function Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 3
                        },
                        parameters: {
                            required: true,
                            propertyOrder: 4,
                            type: "array",
                            format: "table",
                            title: "Parameters",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title : 'Parameter',
                                properties: {
                                    parameterValue: {
                                        type: "string",
                                        minLength: 1
                                    }
                                }
                            }
                        },
                        outputEventType: {
                            type: "string",
                            title: "Output Event Type",
                            propertyOrder: 5,
                            enum: [
                                "current",
                                "expired",
                                "all"
                            ],
                            default: "current"
                        }
                    }
                },
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                display_required_only: true,
                no_additional_properties: true
            });

            formContainer.append('<div id="submit"><button type="button" class="btn btn-default">Submit</button></div>');

            // 'Submit' button action
            var submitButtonElement = $('#submit')[0];
            submitButtonElement.addEventListener('click', function () {
                var isWindowNameUsed = false;
                _.forEach(self.appData.windowList, function(window){
                    if(window.getName().toUpperCase() === editor.getValue().name.toUpperCase()) {
                        isWindowNameUsed = true;
                    }
                });
                if(isWindowNameUsed) {
                    alert("Window name \"" + editor.getValue().name + "\" is already used.");
                } else {
                    // add the new out window to the window array
                    var windowOptions = {};
                    _.set(windowOptions, 'id', i);
                    _.set(windowOptions, 'name', editor.getValue().name);
                    _.set(windowOptions, 'function', editor.getValue().functionName);
                    var parameters = [];
                    _.forEach(editor.getValue().parameters, function (parameter) {
                        parameters.push(parameter.parameterValue);
                    });
                    _.set(windowOptions, 'parameters', parameters);
                    if (editor.getValue().outputEventType !== undefined) {
                        _.set(windowOptions, 'outputEventType', editor.getValue().outputEventType);
                    } else {
                        _.set(windowOptions, 'outputEventType', '');
                    }
                    var window = new Window(windowOptions);
                    _.forEach(editor.getValue().attributes, function (attribute) {
                        window.addAttribute(attribute);
                    });
                    self.appData.addWindow(window);

                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);
                    self.consoleListManager.hideAllConsoles();

                    self.gridContainer.removeClass("disabledbutton");
                    self.toolPaletteContainer.removeClass("disabledbutton");
                }
            });
            return editor.getValue().name;
        };

        /**
         * @function generate the property window for an existing window
         * @param element selected element(window)
         */
        FormBuilder.prototype.GeneratePropertiesFormForWindows = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            // The container and the tool palette are disabled to prevent the user from dropping any elements
            self.gridContainer.addClass("disabledbutton");
            self.toolPaletteContainer.addClass("disabledbutton");

            var id = $(element).parent().attr('id');
            // retrieve the window information from the collection
            var clickedElement = self.appData.getWindow(id);
            if(clickedElement === undefined) {
                var errorMessage = 'unable to find clicked element';
                log.error(errorMessage);
            }
            var name = clickedElement.getName();
            var attributes = clickedElement.getAttributeList();
            var functionName = clickedElement.getFunction();
            var savedParameterValues = clickedElement.getParameters();

            var parameters = [];
            _.forEach(savedParameterValues, function (savedParameterValue) {
                var parameterObject = {
                    parameterValue: savedParameterValue
                };
                parameters.push(parameterObject);
            });

            var outputEventType = clickedElement.getOutputEventType();
            var fillWith = {
                name : name,
                attributes : attributes,
                functionName : functionName,
                parameters : parameters,
                outputEventType : outputEventType
            };
            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Window",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 1
                        },
                        attributes: {
                            required: true,
                            propertyOrder: 2,
                            type: "array",
                            format: "table",
                            title: "Attributes",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title : 'Attribute',
                                properties: {
                                    attribute: {
                                        type: "string",
                                        minLength: 1
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
                        },
                        functionName: {
                            type: "string",
                            title: "Function Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 3
                        },
                        parameters: {
                            required: true,
                            propertyOrder: 4,
                            type: "array",
                            format: "table",
                            title: "Parameters",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title : 'Parameter',
                                properties: {
                                    parameterValue: {
                                        type: "string",
                                        minLength: 1
                                    }
                                }
                            }
                        },
                        outputEventType: {
                            type: "string",
                            title: "Output Event Type",
                            propertyOrder: 5,
                            enum: [
                                "current",
                                "expired",
                                "all"
                            ],
                            default: "current"
                        }
                    }
                },
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                display_required_only: true,
                no_additional_properties: true,
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

                // update selected window model
                clickedElement.setName(config.name);
                clickedElement.setFunction(config.function);
                var parameters = [];
                _.forEach(config.parameters, function (parameter) {
                    parameters.push(parameter.parameterValue);
                });
                clickedElement.setParameters(parameters);
                clickedElement.setOutputEventType(config.outputEventType);
                if (config.outputEventType !== undefined) {
                    clickedElement.setOutputEventType(config.outputEventType);
                } else {
                    clickedElement.setOutputEventType('');
                }
                // removing all elements from attribute list
                clickedElement.getAttributeList().removeAllElements();
                // adding new attributes to the attribute list
                _.forEach(config.attributes, function(attribute){
                    clickedElement.addAttribute(attribute);
                });

                var textNode = $(element).parent().find('.windownamenode');
                textNode.html(config.name);

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            });

            // 'Cancel' button action
            var cancelButtonElement = $('#form-cancel')[0];
            cancelButtonElement.addEventListener('click', function () {
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            });
        };

        /**
         * @function generate the form to define the trigger once it is dropped on the canvas
         * @param i id for the element
         * @returns user given trigger name
         */
        FormBuilder.prototype.DefineTrigger = function (i) {
            var self = this;
            var formConsole = this.createTabForForm(i, constants.TRIGGER);
            var formContainer = formConsole.getContentContainer();
            var propertyDiv = $('<div id="property-header"><h3>Define Trigger </h3></div>' +
                '<div id="define-trigger" class="define-trigger"></div>');
            formContainer.append(propertyDiv);
            var triggerElement = $("#define-trigger")[0];

            // generate the form to define a trigger
            var editor = new JSONEditor(triggerElement, {
                schema: {
                    type: "object",
                    title: "Trigger",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 1
                        },
                        at: {
                            type: "string",
                            title: "At",
                            minLength: 1,
                            required: true,
                            propertyOrder: 2
                        }
                    }
                },
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                display_required_only: true,
                no_additional_properties: true
            });

            formContainer.append('<div id="submit"><button type="button" class="btn btn-default">Submit</button></div>');

            // 'Submit' button action
            var submitButtonElement = $('#submit')[0];
            submitButtonElement.addEventListener('click', function () {
                var isTriggerNameUsed = false;
                _.forEach(self.appData.triggerList, function(trigger){
                    if(trigger.getName().toUpperCase() === editor.getValue().name.toUpperCase()) {
                        isTriggerNameUsed = true;
                    }
                });
                if(isTriggerNameUsed) {
                    alert("Trigger name \"" + editor.getValue().name + "\" is already used.");
                } else {
                    // add the new out trigger to the trigger array
                    var triggerOptions = {};
                    _.set(triggerOptions, 'id', i);
                    _.set(triggerOptions, 'name', editor.getValue().name);
                    _.set(triggerOptions, 'at', editor.getValue().at);
                    var trigger = new Trigger(triggerOptions);
                    self.appData.addTrigger(trigger);

                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);
                    self.consoleListManager.hideAllConsoles();

                    self.gridContainer.removeClass("disabledbutton");
                    self.toolPaletteContainer.removeClass("disabledbutton");
                }
            });
            return editor.getValue().name;
        };

        /**
         * @function generate the property window for an existing trigger
         * @param element selected element(trigger)
         */
        FormBuilder.prototype.GeneratePropertiesFormForTriggers = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            // The container and the tool palette are disabled to prevent the user from dropping any elements
            self.gridContainer.addClass("disabledbutton");
            self.toolPaletteContainer.addClass("disabledbutton");

            var id = $(element).parent().attr('id');
            // retrieve the trigger information from the collection
            var clickedElement = self.appData.getTrigger(id);
            if(clickedElement === undefined) {
                var errorMessage = 'unable to find clicked element';
                log.error(errorMessage);
            }
            var name = clickedElement.getName();
            var at = clickedElement.getAt();
            var fillWith = {
                name : name,
                at : at
            };
            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Trigger",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 1
                        },
                        at: {
                            type: "string",
                            title: "At",
                            minLength: 1,
                            required: true,
                            propertyOrder: 2
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

                // update selected trigger model
                clickedElement.setName(config.name);
                clickedElement.setAt(config.at);

                var textNode = $(element).parent().find('.triggernamenode');
                textNode.html(config.name);

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            });

            // 'Cancel' button action
            var cancelButtonElement = $('#form-cancel')[0];
            cancelButtonElement.addEventListener('click', function () {
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            });
        };

        /**
         * @function generate the form to define the aggregation once it is dropped on the canvas
         * @param i id for the element
         * @returns user given aggregation name
         */
        FormBuilder.prototype.DefineAggregation = function (i) {
            var self = this;
            var formConsole = this.createTabForForm(i, constants.AGGREGATION);
            var formContainer = formConsole.getContentContainer();
            var propertyDiv = $('<div id="property-header"><h3>Define Aggregation </h3></div>' +
                '<div id="define-aggregation" class="define-aggregation"></div>');
            formContainer.append(propertyDiv);
            var aggregationElement = $("#define-aggregation")[0];

            // generate the form to define a aggregation
            var editor = new JSONEditor(aggregationElement, {
                schema: {
                    type: "object",
                    title: "Aggregation",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 1
                        },
                        from: {
                            type: "string",
                            title: "From",
                            minLength: 1,
                            required: true,
                            propertyOrder: 2
                        },
                        select: {
                            title: "Select",
                            required: true,
                            propertyOrder: 4,
                            oneOf: [
                                {
                                    $ref: "#/definitions/userDefined",
                                    title: "User-Defined"
                                },
                                {
                                    $ref: "#/definitions/all",
                                    title: "All"
                                }
                                ]
                        },
                        groupBy: {
                            required: true,
                            propertyOrder: 5,
                            type: "array",
                            format: "table",
                            title: "Group By Attributes",
                            uniqueItems: true,
                            items: {
                                type: "object",
                                title : 'Attribute',
                                properties: {
                                    attribute: {
                                        type: "string"
                                    }
                                }
                            }
                        },
                        aggregateByAttribute: {
                            required: true,
                            type: "string",
                            title: "Aggregate by Attribute",
                            minLength: 1,
                            propertyOrder: 6
                        },
                        aggregateByTimePeriod: {
                            type: "object",
                            title: "Aggregate by Time Period",
                            required: true,
                            propertyOrder: 7,
                            properties: {
                                minValue: {
                                    type: "string",
                                    title: "Starting Time Value",
                                    required: true,
                                    enum: [
                                        "seconds",
                                        "minutes",
                                        "hours",
                                        "days",
                                        "weeks",
                                        "months",
                                        "years"
                                    ],
                                    default: "seconds"
                                },
                                maxValue: {
                                    type: "string",
                                    title: "Ending Time Value",
                                    required: true,
                                    enum: [
                                        "",
                                        "seconds",
                                        "minutes",
                                        "hours",
                                        "days",
                                        "weeks",
                                        "months",
                                        "years"
                                    ],
                                    default: ""
                                }
                            }

                        }
                    },
                    definitions: {
                        userDefined: {
                            required: true,
                            type: "array",
                            format: "table",
                            title: "User Defined",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                title: "Value Set",
                                type: "object",
                                properties: {
                                    expression: {
                                        title: "Expression",
                                        type: "string",
                                        minLength: 1
                                    },
                                    as: {
                                        title: "As",
                                        type: "string"
                                    }
                                }
                            }
                        },
                        all: {
                            type: "string",
                            title: "All",
                            enum: [
                                "*"
                            ],
                            default: "*"
                        }
                    }
                },
                disable_properties: false,
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                display_required_only: true,
                no_additional_properties: true
            });

            formContainer.append('<div id="submit"><button type="button" class="btn btn-default">Submit</button></div>');

            // 'Submit' button action
            var submitButtonElement = $('#submit')[0];
            submitButtonElement.addEventListener('click', function () {
                var isAggregationNameUsed = false;
                _.forEach(self.appData.aggregationList, function(aggregation){
                    if(aggregation.getName().toUpperCase() === editor.getValue().name.toUpperCase()) {
                        isAggregationNameUsed = true;
                    }
                });
                if(isAggregationNameUsed) {
                    alert("Aggregation name \"" + editor.getValue().name + "\" is already used.");
                    return;
                }
                // add the new aggregation to the aggregation array
                var aggregationOptions = {};
                _.set(aggregationOptions, 'id', i);
                _.set(aggregationOptions, 'name', editor.getValue().name);
                _.set(aggregationOptions, 'from', editor.getValue().from);
                _.set(aggregationOptions, 'aggregateByAttribute', editor.getValue().aggregateByAttribute);

                var selectAttributeOptions = {};
                if (editor.getValue().select instanceof Array) {
                    _.set(selectAttributeOptions, 'type', 'user-defined');
                    _.set(selectAttributeOptions, 'value', editor.getValue().select);
                } else if (editor.getValue().select === "*") {
                    _.set(selectAttributeOptions, 'type', 'all');
                    _.set(selectAttributeOptions, 'value', editor.getValue().select);
                } else {
                    console.log("Value other than \"user-defined\" and \"all\" received!");
                }
                var selectObject = new QuerySelect(selectAttributeOptions);
                _.set(aggregationOptions, 'select', selectObject);

                if (editor.getValue().groupBy !== undefined) {
                    var groupByAttributes = [];
                    _.forEach(editor.getValue().groupBy, function (groupByAttribute) {
                        groupByAttributes.push(groupByAttribute.attribute);
                    });
                    _.set(aggregationOptions, 'groupBy', groupByAttributes);
                } else {
                    _.set(aggregationOptions, 'groupBy', '');
                }

                var aggregateByTimePeriod = new AggregateByTimePeriod(editor.getValue().aggregateByTimePeriod);
                if (editor.getValue().aggregateByTimePeriod === undefined) {
                    aggregateByTimePeriod.setMaxValue('');
                }
                _.set(aggregationOptions, 'aggregateByTimePeriod', aggregateByTimePeriod);

                var aggregation = new Aggregation(aggregationOptions);
                self.appData.addAggregation(aggregation);

                // close the form aggregation
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();

                self.gridContainer.removeClass("disabledbutton");
                self.toolPaletteContainer.removeClass("disabledbutton");

            });
            return editor.getValue().name;
        };

        /**
         * @function generate the property aggregation for an existing aggregation
         * @param element selected element(aggregation)
         */
        FormBuilder.prototype.GeneratePropertiesFormForAggregations = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            // The container and the tool palette are disabled to prevent the user from dropping any elements
            self.gridContainer.addClass("disabledbutton");
            self.toolPaletteContainer.addClass("disabledbutton");

            var id = $(element).parent().attr('id');
            // retrieve the aggregation information from the collection
            var clickedElement = self.appData.getAggregation(id);
            if(clickedElement === undefined) {
                var errorMessage = 'unable to find clicked element';
                log.error(errorMessage);
            }
            var name = clickedElement.getName();
            var from = clickedElement.getFrom();
            var select = clickedElement.getSelect().value;
            var savedGroupByAttributes = clickedElement.getGroupBy();
            var aggregateByAttribute = clickedElement.getAggregateByAttribute();
            var aggregateByTimePeriod = clickedElement.getAggregateByTimePeriod();

            var groupBy = [];
            _.forEach(savedGroupByAttributes, function (savedGroupByAttribute) {
                var groupByAttributeObject = {
                    attribute: savedGroupByAttribute
                };
                groupBy.push(groupByAttributeObject);
            });

            var fillWith = {
                name : name,
                from : from,
                select : select,
                groupBy : groupBy,
                aggregateByAttribute : aggregateByAttribute,
                aggregateByTimePeriod : aggregateByTimePeriod
            };
            // generate the form to define a aggregation
            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Aggregation",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 1
                        },
                        from: {
                            type: "string",
                            title: "From",
                            minLength: 1,
                            required: true,
                            propertyOrder: 2
                        },
                        select: {
                            title: "Select",
                            required: true,
                            propertyOrder: 4,
                            oneOf: [
                                {
                                    $ref: "#/definitions/userDefined",
                                    title: "User-Defined"
                                },
                                {
                                    $ref: "#/definitions/all",
                                    title: "All"
                                }
                            ]
                        },
                        groupBy: {
                            required: true,
                            propertyOrder: 5,
                            type: "array",
                            format: "table",
                            title: "Group By Attributes",
                            uniqueItems: true,
                            items: {
                                type: "object",
                                title : 'Attribute',
                                properties: {
                                    attribute: {
                                        type: "string"
                                    }
                                }
                            }
                        },
                        aggregateByAttribute: {
                            required: true,
                            type: "string",
                            title: "Aggregate by Attribute",
                            minLength: 1,
                            propertyOrder: 6
                        },
                        aggregateByTimePeriod: {
                            type: "object",
                            title: "Aggregate by Time Period",
                            required: true,
                            propertyOrder: 7,
                            properties: {
                                minValue: {
                                    type: "string",
                                    title: "Starting Time Value",
                                    required: true,
                                    enum: [
                                        "seconds",
                                        "minutes",
                                        "hours",
                                        "days",
                                        "weeks",
                                        "months",
                                        "years"
                                    ],
                                    default: "seconds"
                                },
                                maxValue: {
                                    type: "string",
                                    title: "Ending Time Value",
                                    required: true,
                                    enum: [
                                        "",
                                        "seconds",
                                        "minutes",
                                        "hours",
                                        "days",
                                        "weeks",
                                        "months",
                                        "years"
                                    ],
                                    default: ""
                                }
                            }

                        }
                    },
                    definitions: {
                        userDefined: {
                            required: true,
                            type: "array",
                            format: "table",
                            title: "User Defined",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                title: "Value Set",
                                type: "object",
                                properties: {
                                    expression: {
                                        title: "Expression",
                                        type: "string",
                                        minLength: 1
                                    },
                                    as: {
                                        title: "As",
                                        type: "string"
                                    }
                                }
                            }
                        },
                        all: {
                            type: "string",
                            title: "All",
                            enum: [
                                "*"
                            ],
                            default: "*"
                        }
                    }
                },
                startval: fillWith,
                disable_properties: false,
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                display_required_only: true,
                no_additional_properties: true
            });
            $(formContainer).append('<div id="form-submit"><button type="button" ' +
                'class="btn btn-default">Submit</button></div>' +
                '<div id="form-cancel"><button type="button" class="btn btn-default">Cancel</button></div>');

            // 'Submit' button action
            var submitButtonElement = $('#form-submit')[0];
            submitButtonElement.addEventListener('click', function () {
                //TODO: when the name is saved check whether this name is same as the previously saved name. If not, then only check whether it is unique.
                // var isAggregationNameUsed = false;
                // _.forEach(self.appData.aggregationList, function(aggregation){
                //     if(aggregation.getName().toUpperCase() === editor.getValue().name.toUpperCase()) {
                //         isAggregationNameUsed = true;
                //     }
                // });
                // if(isAggregationNameUsed) {
                //     alert("Aggregation name \"" + editor.getValue().name + "\" is already used.");
                //     return;
                // }
                // The container and the palette are disabled to prevent the user from dropping any elements
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                var config = editor.getValue();

                // update selected aggregation model
                clickedElement.setName(config.name);
                clickedElement.setFrom(config.from);
                clickedElement.setAggregateByAttribute(config.aggregateByAttribute);

                var selectAttributeOptions = {};
                if (config.select instanceof Array) {
                    _.set(selectAttributeOptions, 'type', 'user-defined');
                    _.set(selectAttributeOptions, 'value', editor.getValue().select);
                } else if (config.select === "*") {
                    _.set(selectAttributeOptions, 'type', 'all');
                    _.set(selectAttributeOptions, 'value', editor.getValue().select);
                } else {
                    console.log("Value other than \"user-defined\" and \"all\" received!");
                }
                var selectObject = new QuerySelect(selectAttributeOptions);
                clickedElement.setSelect(selectObject);

                if (config.groupBy !== undefined) {
                    var groupByAttributes = [];
                    _.forEach(config.groupBy, function (groupByAttribute) {
                        groupByAttributes.push(groupByAttribute.attribute);
                    });
                    clickedElement.setGroupBy(groupByAttributes);
                } else {
                    clickedElement.setGroupBy('');
                }

                var aggregateByTimePeriod = new AggregateByTimePeriod(config.aggregateByTimePeriod);
                if (config.aggregateByTimePeriod === undefined) {
                    aggregateByTimePeriod.setMaxValue('');
                }
                clickedElement.setAggregateByTimePeriod(aggregateByTimePeriod);

                var textNode = $(element).parent().find('.aggregationnamenode');
                textNode.html(config.name);

                // close the form aggregation
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            });

            // 'Cancel' button action
            var cancelButtonElement = $('#form-cancel')[0];
            cancelButtonElement.addEventListener('click', function () {
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form aggregation
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
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

            var id = $(element).parent().attr('id');
            var clickedElement = self.appData.getQuery(id);
            if(clickedElement === undefined) {
                var errorMessage = 'unable to find clicked element';
                log.error(errorMessage);
            }
            var type= $(element).parent();
            if (!(clickedElement.getFrom())) {
                alert('Connect an input stream'); //TODO: check here table also? Restructure all the forms according to this.
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            }
            else if (!(clickedElement.getInsertInto())) {
                // retrieve the query information from the collection
                var name = clickedElement.getName();
                var inStream = (self.appData.getStream(clickedElement.getFrom())).getName();
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
                    self.consoleListManager.hideAllConsoles();

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
                    self.dropElements.dropStreamFromQuery(position, id, config.insertInto, streamAttributes);
                });

                // 'Cancel' button action
                var cancelButtonElement = $('#form-cancel')[0];
                cancelButtonElement.addEventListener('click', function () {
                    self.gridContainer.removeClass('disabledbutton');
                    self.toolPaletteContainer.removeClass('disabledbutton');
                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);
                    self.consoleListManager.hideAllConsoles();
                });
            } else {
                // retrieve the query information from the collection
                var name = clickedElement.getName();
                var inStream = (self.appData.getStream(clickedElement.getFrom())).getName();
                var outStream = (self.appData.getStream(clickedElement.getInsertInto())).getName();
                var filter1 = clickedElement.getFilter();
                var window = clickedElement.getWindow();
                var filter2 = clickedElement.getPostWindowFilter();
                var outputType = clickedElement.getOutputType();
                var attrString = [];
                var outStreamAttributes = (self.appData.getStream(clickedElement.getInsertInto())).getAttributeList();
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
                    self.consoleListManager.hideAllConsoles();
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
                    self.consoleListManager.hideAllConsoles();
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

            var id = $(element).parent().attr('id');
            var clickedElement = self.appData.getPatternQuery(id);
            if (clickedElement.getFrom().length === 0) {
                alert('Connect input streams');
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            } else if (clickedElement.getInsertInto() === '') {
                alert('Connect an output stream');
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            } else {
                // retrieve the pattern information from the collection
                var streams = [];
                var projections = [];
                $.each(clickedElement.getFrom(), function (index, streamID) {
                    streams.push((self.appData.getStream(streamID)).getName());
                });

                var insertInto = self.appData.getStream(clickedElement.getInsertInto()).getName();
                var outStreamAttributes = (self.appData.getStream(clickedElement.getInsertInto())).getAttributeList();
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
                    self.consoleListManager.hideAllConsoles();

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
                    self.consoleListManager.hideAllConsoles();
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

            var id = $(element).parent().attr('id');
            var clickedElement = self.appData.getJoinQuery(id);
            if (!(clickedElement.getFrom()) || clickedElement.getFrom().length !== 2) {
                alert('Connect TWO input streams');
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            }
            else if (!(clickedElement.getInsertInto())) {
                alert('Connect an output stream');
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            }

            else {
                var streams = [];
                $.each(clickedElement.getFrom(), function (index, streamID) {
                    streams.push((self.appData.getStream(streamID)).getName());
                });
                var projections = [];
                var insertInto = self.appData.getStream(clickedElement.getInsertInto()).getName();
                var outStreamAttributes = (self.appData.getStream(clickedElement.getInsertInto())).getAttributeList();
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
                    self.consoleListManager.hideAllConsoles();

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
                    self.consoleListManager.hideAllConsoles();
                });
            }
        };

        /**
         * @function generate property window for Partition
         * @param element selected element(query)
         */
        FormBuilder.prototype.GeneratePartitionKeyForm = function (element) {
            var self = this;
            var id = $(element.target).parent().attr('id');
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
                            stream : (self.appData.getStream(connectedStream)).getName(),
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
                                template: (self.appData.getStream(connectedStream)).getName()
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
                    self.consoleListManager.hideAllConsoles();

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
                    self.consoleListManager.hideAllConsoles();
                });
            }
        };

        return FormBuilder;
    });
