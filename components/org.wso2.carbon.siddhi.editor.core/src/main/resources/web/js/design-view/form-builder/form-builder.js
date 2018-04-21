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

define(['require', 'log', 'jquery', 'lodash', 'jsplumb', 'attribute', 'stream', 'table', 'window', 'trigger',
        'aggregation', 'aggregateByTimePeriod', 'querySelect', 'patternQueryInputCounting', 'patternQueryInputAndOr',
        'patternQueryInputNotFor', 'patternQueryInputNotAnd', 'queryOutputInsert', 'queryOutputDelete',
        'queryOutputUpdate', 'queryOutputUpdateOrInsertInto', 'queryWindow'],
    function (require, log, $, _, jsPlumb, Attribute, Stream, Table, Window, Trigger, Aggregation, AggregateByTimePeriod,
              QuerySelect, PatternQueryInputCounting, PatternQueryInputAndOr, PatternQueryInputNotFor,
              PatternQueryInputNotAnd, QueryOutputInsert, QueryOutputDelete, QueryOutputUpdate,
              QueryOutputUpdateOrInsertInto, QueryWindow) {

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
            PROJECTION : 'projectionQueryDrop',
            FILTER : 'filterQueryDrop',
            JOIN : 'joquerydrop',
            WINDOW_QUERY : 'windowQueryDrop',
            PATTERN : 'patternQueryDrop',
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
                }
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
                                    name: {
                                        title : 'Name',
                                        type: "string",
                                        minLength: 1
                                    },
                                    type: {
                                        title : 'Type',
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
                        var attributeObject = new Attribute(attribute);
                        stream.addAttribute(attributeObject);
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
            var attributesList = clickedElement.getAttributeList();
            var attributes = [];
            _.forEach(attributesList, function (attribute) {
                var attributeObject = {
                    name: attribute.getName(),
                    type: attribute.getType()
                };
                attributes.push(attributeObject);
            });

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
                                    name: {
                                        title: "Name",
                                        type: "string",
                                        minLength: 1
                                    },
                                    type: {
                                        title: "Type",
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
                _.forEach(config.attributes, function (attribute) {
                    var attributeObject = new Attribute(attribute);
                    clickedElement.addAttribute(attributeObject);
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
                                    name: {
                                        title: "Name",
                                        type: "string",
                                        minLength: 1
                                    },
                                    type: {
                                        title: "Type",
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
                        var attributeObject = new Attribute(attribute);
                        table.addAttribute(attributeObject);
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
            var attributesList = clickedElement.getAttributeList();
            var attributes = [];
            _.forEach(attributesList, function (attribute) {
                var attributeObject = {
                    name: attribute.getName(),
                    type: attribute.getType()
                };
                attributes.push(attributeObject);
            });
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
                                    name: {
                                        title: "Name",
                                        type: "string",
                                        minLength: 1
                                    },
                                    type: {
                                        title: "Type",
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
                _.forEach(config.attributes, function (attribute) {
                    var attributeObject = new Attribute(attribute);
                    clickedElement.addAttribute(attributeObject);
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
                                    name: {
                                        title: "Name",
                                        type: "string",
                                        minLength: 1
                                    },
                                    type: {
                                        title: "Type",
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
                        var attributeObject = new Attribute(attribute);
                        window.addAttribute(attributeObject);
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
         * @function generate the form for an existing window
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
            var attributesList = clickedElement.getAttributeList();
            var attributes = [];
            _.forEach(attributesList, function (attribute) {
                var attributeObject = {
                    name: attribute.getName(),
                    type: attribute.getType()
                };
                attributes.push(attributeObject);
            });
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
                                    name: {
                                        title: "Name",
                                        type: "string",
                                        minLength: 1
                                    },
                                    type: {
                                        title: "Type",
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
         * @function generate the form window for an existing trigger
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
                        querySelect: {
                            propertyOrder: 3,
                            required: true,
                            type: "object",
                            title: "Query Select",
                            properties: {
                                select: {
                                    title: "Query Select Type",
                                    required: true,
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
                                }
                            }
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
                            template: '*'
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
                if (editor.getValue().querySelect.select instanceof Array) {
                    _.set(selectAttributeOptions, 'type', 'user_defined');
                    _.set(selectAttributeOptions, 'value', editor.getValue().querySelect.select);
                } else if (editor.getValue().querySelect.select === "*") {
                    _.set(selectAttributeOptions, 'type', 'all');
                    _.set(selectAttributeOptions, 'value', editor.getValue().querySelect.select);
                } else {
                    console.log("Value other than \"user_defined\" and \"all\" received!");
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
         * @function generate the form for an existing aggregation
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
                querySelect : {
                    select: select
                },
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
                        querySelect: {
                            propertyOrder: 3,
                            required: true,
                            type: "object",
                            title: "Query Select",
                            properties: {
                                select: {
                                    title: "Query Select Type",
                                    required: true,
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
                                }
                            }
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
                            template: '*'
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
                if (config.querySelect.select instanceof Array) {
                    _.set(selectAttributeOptions, 'type', 'user_defined');
                    _.set(selectAttributeOptions, 'value', editor.getValue().querySelect.select);
                } else if (config.querySelect.select === "*") {
                    _.set(selectAttributeOptions, 'type', 'all');
                    _.set(selectAttributeOptions, 'value', editor.getValue().querySelect.select);
                } else {
                    console.log("Value other than \"user_defined\" and \"all\" received!");
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
         * @function generate the form for the simple queries ( projection, filter and window)
         * @param element selected element(query)
         */
        FormBuilder.prototype.GeneratePropertiesFormForWindowFilterProjectionQueries = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            // The container and the tool palette are disabled to prevent the user from dropping any elements
            self.gridContainer.addClass('disabledbutton');
            self.toolPaletteContainer.addClass('disabledbutton');

            var id = $(element).parent().attr('id');
            var clickedElement = self.appData.getWindowFilterProjectionQuery(id);
            if (clickedElement.getQueryInput() === ''
                || clickedElement.getQueryInput().getFrom() === '') {
                alert('Connect an input element');
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            } else if (clickedElement.getQueryOutput() === '' || clickedElement.getQueryOutput().getTarget() === '') {
                alert('Connect an output stream');
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            } else {
                var savedQueryInput = {};
                if (clickedElement.getQueryInput().getWindow() === '') {
                    savedQueryInput = {
                        from : clickedElement.getQueryInput().getFrom(),
                        filter : clickedElement.getQueryInput().getFilter()
                    };
                } else {
                    savedQueryInput = {
                        from : clickedElement.getQueryInput().getFrom(),
                        filter : clickedElement.getQueryInput().getFilter(),
                        window : {
                            functionName: clickedElement.getQueryInput().getWindow().getFunction(),
                            filter: clickedElement.getQueryInput().getWindow().getFilter(),
                            parameters: clickedElement.getQueryInput().getWindow().getParameters()
                        }
                    };
                }

                var inputElementName = clickedElement.getQueryInput().getFrom();
                var select = clickedElement.getSelect().value;
                var savedGroupByAttributes = clickedElement.getGroupBy();
                var having = clickedElement.getHaving();
                var outputRateLimit = clickedElement.getOutputRateLimit();

                var groupBy = [];
                _.forEach(savedGroupByAttributes, function (savedGroupByAttribute) {
                    var groupByAttributeObject = {
                        attribute: savedGroupByAttribute
                    };
                    groupBy.push(groupByAttributeObject);
                });

                var possibleGroupByAttributes = [];
                var isInputElementNameFound = false;
                _.forEach(self.appData.streamList, function (stream) {
                    if (stream.getName() === inputElementName) {
                        isInputElementNameFound = true;
                        _.forEach(stream.getAttributeList(), function (attribute) {
                            possibleGroupByAttributes.push(attribute.getName());
                        });
                    }
                });
                if (!isInputElementNameFound) {
                    _.forEach(self.appData.windowList, function (window) {
                        if (window.getName() === inputElementName) {
                            isInputElementNameFound = true;
                            _.forEach(window.getAttributeList(), function (attribute) {
                                possibleGroupByAttributes.push(attribute.getName());
                            });
                        }
                    });
                }
                if (isInputElementNameFound) {
                    _.forEach(self.appData.tableList, function (table) {
                        if (table.getName() === inputElementName) {
                            isInputElementNameFound = true;
                            _.forEach(table.getAttributeList(), function (attribute) {
                                possibleGroupByAttributes.push(attribute.getName());
                            });
                        }
                    });
                }

                var savedQueryOutput = clickedElement.getQueryOutput();
                if (savedQueryOutput !== undefined && savedQueryOutput !== "") {
                    var savedQueryOutputTarget = savedQueryOutput.getTarget();
                    var savedQueryOutputType = savedQueryOutput.getType();
                    var output = savedQueryOutput.getOutput();
                    var queryOutput;
                    if (savedQueryOutputTarget !== undefined && savedQueryOutputType !== undefined && output !== undefined) {
                        if (savedQueryOutputType === "insert") {
                            queryOutput = {
                                output: {
                                    outputType: "Insert",
                                    insertTarget: savedQueryOutputTarget,
                                    eventType: (output.getEventType() === '') ? 'all' : output.getEventType()
                                }
                            };
                        } else if (savedQueryOutputType === "delete") {
                            queryOutput = {
                                output: {
                                    outputType: "Delete",
                                    deleteTarget: savedQueryOutputTarget,
                                    forEventType: (output.getForEventType() === '') ? 'all' : output.getForEventType(),
                                    on: output.getOn()
                                }
                            };

                        } else if (savedQueryOutputType === "update") {
                            queryOutput = {
                                output: {
                                    outputType: "Update",
                                    updateTarget: savedQueryOutputTarget,
                                    forEventType: (output.getForEventType() === '') ? 'all' : output.getForEventType(),
                                    set: output.getSet(),
                                    on: output.getOn()
                                }
                            };
                        } else if (savedQueryOutputType === "update_or_insert_into") {
                            queryOutput = {
                                output: {
                                    outputType: "Update or Insert Into",
                                    updateOrInsertIntoTarget: savedQueryOutputTarget,
                                    forEventType: (output.getForEventType() === '') ? 'all' : output.getForEventType(),
                                    set: output.getSet(),
                                    on: output.getOn()
                                }
                            };
                        }
                    }
                }

                var fillWith = {
                    queryInput : savedQueryInput,
                    querySelect : {
                        select : select
                    },
                    groupBy : groupBy,
                    having : having,
                    outputRateLimit : outputRateLimit,
                    queryOutput: queryOutput
                };

                var editor = new JSONEditor(formContainer[0], {
                    schema: {
                        type: "object",
                        title: "Pattern Query",
                        properties: {
                            queryInput: {
                                propertyOrder: 1,
                                type: "object",
                                title: "Query Input",
                                required: true,
                                properties: {
                                    from: {
                                        required: true,
                                        title: "From",
                                        type: "string",
                                        template: inputElementName,
                                        minLength: 1
                                    },
                                    filter: {
                                        title: "Filter",
                                        type: "string",
                                        minLength: 1
                                    },
                                    window: {
                                        title: "Window",
                                        type: "object",
                                        properties: {
                                            functionName: {
                                                required: true,
                                                title: "Function",
                                                type: "string",
                                                minLength: 1
                                            },
                                            parameters: {
                                                required: true,
                                                type: "array",
                                                format: "table",
                                                title: "Parameters",
                                                uniqueItems: true,
                                                items: {
                                                    type: "object",
                                                    title : 'Attribute',
                                                    properties: {
                                                        parameter: {
                                                            type: 'string',
                                                            title: 'Parameter Name',
                                                            minLength: 1
                                                        }
                                                    }
                                                }
                                            },
                                            filter: {
                                                title: "Filter",
                                                type: "string",
                                                minLength: 1
                                            }
                                        }
                                    }
                                }
                            },
                            querySelect: {
                                propertyOrder: 3,
                                required: true,
                                type: "object",
                                title: "Query Select",
                                properties: {
                                    select: {
                                        title: "Query Select Type",
                                        required: true,
                                        oneOf: [
                                            {
                                                $ref: "#/definitions/querySelectUserDefined",
                                                title: "User-Defined"
                                            },
                                            {
                                                $ref: "#/definitions/querySelectAll",
                                                title: "All"
                                            }
                                        ]
                                    }
                                }
                            },
                            groupBy: {
                                propertyOrder: 4,
                                type: "array",
                                format: "table",
                                title: "Group By Attributes",
                                uniqueItems: true,
                                items: {
                                    type: "object",
                                    title : 'Attribute',
                                    properties: {
                                        attribute: {
                                            type: 'string',
                                            title: 'Attribute Name',
                                            enum: possibleGroupByAttributes,
                                            default: ""
                                        }
                                    }
                                }
                            },
                            having: {
                                propertyOrder: 5,
                                title: "Having",
                                type: "string",
                                minLength: 1
                            },
                            outputRateLimit: {
                                propertyOrder: 6,
                                title: "Output Rate Limit",
                                type: "string",
                                minLength: 1
                            },
                            queryOutput: {
                                propertyOrder: 7,
                                required: true,
                                type: "object",
                                title: "Query Output Type",
                                properties: {
                                    output: {
                                        title: "Query Output Type",
                                        required: true,
                                        oneOf: [
                                            {
                                                $ref: "#/definitions/queryOutputInsertType",
                                                title: "Insert Type"
                                            },
                                            {
                                                $ref: "#/definitions/queryOutputDeleteType",
                                                title: "Delete Type"
                                            },
                                            {
                                                $ref: "#/definitions/queryOutputUpdateType",
                                                title: "Update Type"
                                            },
                                            {
                                                $ref: "#/definitions/queryOutputUpdateOrInsertIntoType",
                                                title: "Update Or Insert Into Type"
                                            }
                                        ]
                                    }
                                }
                            }
                        },
                        definitions: {
                            querySelectUserDefined: {
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
                            querySelectAll: {
                                type: "string",
                                title: "All",
                                template: '*'
                            },
                            queryOutputInsertType: {
                                required: true,
                                title: "Insert",
                                type: "object",
                                properties: {
                                    outputType: {
                                        required: true,
                                        title: "Output Type",
                                        type: "string",
                                        template: "Insert"
                                    },
                                    eventType: {
                                        required: true,
                                        title: "For Every",
                                        type: "string",
                                        enum: ['current', 'expired', 'all'],
                                        default: 'all'
                                    },
                                    insertTarget: {
                                        type: 'string',
                                        title: 'Insert Into',
                                        template: savedQueryOutputTarget,
                                        required: true
                                    }
                                }
                            },
                            queryOutputDeleteType: {
                                required: true,
                                title: "Delete",
                                type: "object",
                                properties: {
                                    outputType: {
                                        required: true,
                                        title: "Output Type",
                                        type: "string",
                                        template: "Delete"
                                    },
                                    deleteTarget: {
                                        type: 'string',
                                        title: 'Delete From',
                                        template: savedQueryOutputTarget,
                                        required: true
                                    },
                                    forEventType: {
                                        title: "For Every",
                                        type: "string",
                                        enum: ['current', 'expired', 'all'],
                                        default: 'all'
                                    },
                                    on: {
                                        type: 'string',
                                        title: 'On',
                                        minLength: 1,
                                        required: true
                                    }
                                }
                            },
                            queryOutputUpdateType: {
                                required: true,
                                title: "Update",
                                type: "object",
                                properties: {
                                    outputType: {
                                        required: true,
                                        title: "Output Type",
                                        type: "string",
                                        template: "Update"
                                    },
                                    updateTarget: {
                                        type: 'string',
                                        title: 'Update From',
                                        template: savedQueryOutputTarget,
                                        required: true
                                    },
                                    forEventType: {
                                        title: "For Every",
                                        type: "string",
                                        enum: ['current', 'expired', 'all'],
                                        default: 'all'
                                    },
                                    set: {
                                        required: true,
                                        type: "array",
                                        format: "table",
                                        title: "Set",
                                        uniqueItems: true,
                                        items: {
                                            type: "object",
                                            title: 'Set Condition',
                                            properties: {
                                                attribute: {
                                                    type: "string",
                                                    title: 'Attribute',
                                                    minLength: 1
                                                },
                                                value: {
                                                    type: "string",
                                                    title: 'Value',
                                                    minLength: 1
                                                }
                                            }
                                        }
                                    },
                                    on: {
                                        type: 'string',
                                        title: 'On',
                                        minLength: 1,
                                        required: true
                                    }
                                }
                            },
                            queryOutputUpdateOrInsertIntoType: {
                                required: true,
                                title: "Update or Insert Into",
                                type: "object",
                                properties: {
                                    outputType: {
                                        required: true,
                                        title: "Output Type",
                                        type: "string",
                                        template: "Update or Insert Into"
                                    },
                                    updateOrInsertIntoTarget: {
                                        type: 'string',
                                        title: 'Update or Insert Into',
                                        template: savedQueryOutputTarget,
                                        required: true
                                    },
                                    forEventType: {
                                        title: "For Every",
                                        type: "string",
                                        enum: ['current', 'expired', 'all'],
                                        default: 'all'
                                    },
                                    set: {
                                        required: true,
                                        type: "array",
                                        format: "table",
                                        title: "Set",
                                        uniqueItems: true,
                                        items: {
                                            type: "object",
                                            title: 'Set Condition',
                                            properties: {
                                                attribute: {
                                                    type: "string",
                                                    title: 'Attribute',
                                                    minLength: 1
                                                },
                                                value: {
                                                    type: "string",
                                                    title: 'Value',
                                                    minLength: 1
                                                }
                                            }
                                        }
                                    },
                                    on: {
                                        type: 'string',
                                        title: 'On',
                                        minLength: 1,
                                        required: true
                                    }
                                }

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

                    var subType;
                    // change the query icon depending on the fields filled
                    if (config.queryInput.window) {
                        subType = "window";
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.WINDOW_QUERY + ' jtk-draggable');
                    } else if (config.queryInput.filter ||
                        (config.queryInput.window && config.queryInput.window.filter)) {
                        subType = "filter";
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.FILTER + ' jtk-draggable');
                    } else if (!(config.queryInput.filter || config.queryInput.window)
                        || (config.queryInput.window && config.queryInput.window.filter)) {
                        subType = "projection";
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.PROJECTION + ' jtk-draggable');
                    }

                    var queryInput = clickedElement.getQueryInput();
                    queryInput.setSubType(subType);
                    if (config.queryInput.filter !== undefined) {
                        queryInput.setFilter(config.queryInput.filter);
                    } else {
                        queryInput.setFilter('');
                    }
                    if (config.queryInput.window !== undefined) {
                        var windowOptions = {};
                        _.set(windowOptions, 'function', config.queryInput.window.functionName);
                        _.set(windowOptions, 'parameters', config.queryInput.window.parameters);
                        if (config.queryInput.window.filter !== undefined) {
                            _.set(windowOptions, 'filter', config.queryInput.window.filter);
                        } else {
                            _.set(windowOptions, 'filter', '');
                        }
                        var queryWindow = new QueryWindow(windowOptions);
                        queryInput.setWindow(queryWindow);
                    } else {
                        queryInput.setWindow('');
                    }

                    var selectAttributeOptions = {};
                    if (config.querySelect.select instanceof Array) {
                        _.set(selectAttributeOptions, 'type', 'user_defined');
                        _.set(selectAttributeOptions, 'value', editor.getValue().querySelect.select);
                    } else if (config.querySelect.select === "*") {
                        _.set(selectAttributeOptions, 'type', 'all');
                        _.set(selectAttributeOptions, 'value', editor.getValue().querySelect.select);
                    } else {
                        console.log("Value other than \"user_defined\" and \"all\" received!");
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

                    if (config.having !== undefined) {
                        clickedElement.setHaving(config.having);
                    } else {
                        clickedElement.setHaving('');
                    }

                    if (config.outputRateLimit !== undefined) {
                        clickedElement.setOutputRateLimit(config.outputRateLimit);
                    } else {
                        clickedElement.setOutputRateLimit('');
                    }

                    var queryOutput = clickedElement.getQueryOutput();
                    var outputObject;
                    var outputType;
                    var outputTarget;
                    if (config.queryOutput.output !== undefined) {
                        if (config.queryOutput.output.outputType === "Insert") {
                            outputType = "insert";
                            outputTarget = config.queryOutput.output.insertTarget;
                            outputObject = new QueryOutputInsert(config.queryOutput.output);
                            if (config.queryOutput.output.eventType === undefined) {
                                outputObject.setEventType('');
                            }
                        } else if (config.queryOutput.output.outputType === "Delete") {
                            outputType = "delete";
                            outputTarget = config.queryOutput.output.deleteTarget;
                            outputObject = new QueryOutputDelete(config.queryOutput.output);
                            if (config.queryOutput.output.forEventType === undefined) {
                                outputObject.setForEventType('');
                            }
                        } else if (config.queryOutput.output.outputType === "Update") {
                            outputType = "update";
                            outputTarget = config.queryOutput.output.updateTarget;
                            outputObject = new QueryOutputUpdate(config.queryOutput.output);
                            if (config.queryOutput.output.forEventType === undefined) {
                                outputObject.setForEventType('');
                            }
                        } else if (config.queryOutput.output.outputType === "Update or Insert Into") {
                            outputType = "update_or_insert_into";
                            outputTarget = config.queryOutput.output.updateOrInsertIntoTarget;
                            outputObject = new QueryOutputUpdateOrInsertInto(config.queryOutput.output);
                            if (config.queryOutput.output.forEventType === undefined) {
                                outputObject.setForEventType('');
                            }
                        } else {
                            console.log("Invalid output type for query received!")
                        }
                        queryOutput.setTarget(outputTarget);
                        queryOutput.setOutput(outputObject);
                        queryOutput.setType(outputType);
                    }
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
         * @function generate form for state machine
         * @param element selected element(query)
         */
        FormBuilder.prototype.GeneratePropertiesFormForPatternQueries = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            // The container and the tool palette are disabled to prevent the user from dropping any elements
            self.gridContainer.addClass('disabledbutton');
            self.toolPaletteContainer.addClass('disabledbutton');

            var id = $(element).parent().attr('id');
            var clickedElement = self.appData.getPatternQuery(id);
            if (clickedElement.getQueryInput() === ''
                || clickedElement.getQueryInput().getConnectedElementNameList().length === 0) {
                alert('Connect input streams');
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            } else if (clickedElement.getQueryOutput() === '' || clickedElement.getQueryOutput().getTarget() === '') {
                alert('Connect an output stream');
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            } else {

                var savedQueryInputEventList = clickedElement.getQueryInput().getEventList();
                var inputStreams = clickedElement.getQueryInput().getConnectedElementNameList();
                var queryInput1 = [];
                var queryInput2 = {};
                var inputEvent;
                _.forEach(savedQueryInputEventList, function (queryInputEvent) {
                    var inputEventType = queryInputEvent.getType();
                    if (inputEventType === "counting") {
                        inputEvent = {
                            inputType:"Counting Pattern Event",
                            forEvery: queryInputEvent.getForEvery(),
                            eventReference: queryInputEvent.getEventReference(),
                            streamName: queryInputEvent.getStreamName(),
                            filter: queryInputEvent.getFilter(),
                            minCount: queryInputEvent.getMinCount(),
                            maxCount: queryInputEvent.getMaxCount(),
                            within: queryInputEvent.getWithin()
                        };
                        queryInput1.push(inputEvent);
                    } else if (inputEventType === "andor") {
                        inputEvent = {
                            inputType:"And Or Event",
                            forEvery: queryInputEvent.getForEvery(),
                            leftEventReference:queryInputEvent.getLeftEventReference(),
                            leftStreamName: queryInputEvent.getLeftStreamName(),
                            leftFilter: queryInputEvent.getLeftFilter(),
                            connectedWith: (queryInputEvent.getConnectedWith() === '')?
                                'and' : queryInputEvent.getConnectedWith(),
                            rightEventReference: queryInputEvent.getRightEventReference(),
                            rightStreamName: queryInputEvent.getRightStreamName(),
                            rightFilter: queryInputEvent.getRightFilter(),
                            within: queryInputEvent.getWithin()
                        };
                        queryInput1.push(inputEvent);
                    } else if (inputEventType === "notfor") {
                        inputEvent = {
                            inputType:"Not For Event",
                            forEvery: queryInputEvent.getForEvery(),
                            streamName: queryInputEvent.getStreamName(),
                            filter: queryInputEvent.getFilter(),
                            forDuration: queryInputEvent.getForDuration()
                        };
                        queryInput2 = inputEvent;
                    } else if (inputEventType === "notand") {
                        inputEvent = {
                            inputType:"Not And Event",
                            forEvery: queryInputEvent.getForEvery(),
                            leftStreamName: queryInputEvent.getLeftStreamName(),
                            leftFilter: queryInputEvent.getLeftFilter(),
                            rightEventReference: queryInputEvent.getRightEventReference(),
                            rightStreamName: queryInputEvent.getRightStreamName(),
                            rightFilter: queryInputEvent.getRightFilter(),
                            within: queryInputEvent.getWithin()
                        };
                        queryInput2 = inputEvent;
                    }
                });

                var select = clickedElement.getSelect().value;
                var savedGroupByAttributes = clickedElement.getGroupBy();
                var having = clickedElement.getHaving();
                var outputRateLimit = clickedElement.getOutputRateLimit();

                var groupBy = [];
                _.forEach(savedGroupByAttributes, function (savedGroupByAttribute) {
                    var groupByAttributeObject = {
                        attribute: savedGroupByAttribute
                    };
                    groupBy.push(groupByAttributeObject);
                });

                var possibleGroupByAttributes = [];
                _.forEach(inputStreams, function (inputStreamName) {
                    _.forEach(self.appData.streamList, function (stream) {
                        if (stream.getName() === inputStreamName) {
                            _.forEach(stream.getAttributeList(), function (attribute) {
                                possibleGroupByAttributes.push(attribute.attribute);
                            });
                        }
                    });
                });

                var savedQueryOutput = clickedElement.getQueryOutput();
                var savedQueryOutputTarget = savedQueryOutput.getTarget();
                var savedQueryOutputType = savedQueryOutput.getType();
                var output = savedQueryOutput.getOutput();
                var queryOutput;
                if (savedQueryOutputTarget !== undefined && savedQueryOutputType!== undefined && output!== undefined) {
                    if (savedQueryOutputType === "insert") {
                        queryOutput = {
                            output : {
                                outputType: "Insert",
                                insertTarget: savedQueryOutputTarget,
                                eventType: (output.getEventType() === '')?'all':output.getEventType()
                            }
                        };
                    } else if (savedQueryOutputType === "delete") {
                        queryOutput = {
                            output : {
                                outputType : "Delete",
                                deleteTarget: savedQueryOutputTarget,
                                forEventType : (output.getForEventType() === '')?'all':output.getForEventType(),
                                on : output.getOn()
                            }
                        };

                    } else if (savedQueryOutputType === "update") {
                        queryOutput = {
                            output : {
                                outputType : "Update",
                                updateTarget: savedQueryOutputTarget,
                                forEventType : (output.getForEventType() === '')?'all':output.getForEventType(),
                                set: output.getSet(),
                                on : output.getOn()
                            }
                        };
                    } else if (savedQueryOutputType === "update_or_insert_into") {
                        queryOutput = {
                            output: {
                                outputType: "Update or Insert Into",
                                updateOrInsertIntoTarget: savedQueryOutputTarget,
                                forEventType: (output.getForEventType() === '')?'all':output.getForEventType(),
                                set: output.getSet(),
                                on: output.getOn()
                            }
                        };
                    }
                }

                var fillWith = {
                    queryInput : {
                        queryInput1: queryInput1,
                        queryInput2: queryInput2
                    },
                    querySelect : {
                        select : select
                    },
                    groupBy : groupBy,
                    having : having,
                    outputRateLimit : outputRateLimit,
                    queryOutput: queryOutput
                };

                //TODO: check whether the left and right stream both are same in some occasions
                var editor = new JSONEditor(formContainer[0], {
                    schema: {
                        type: "object",
                        title: "Pattern Query",
                        properties: {
                            queryInput: {
                                propertyOrder: 1,
                                type: "object",
                                title: "Query Input",
                                required: true,
                                properties: {
                                    queryInput1: {
                                        title: "Query Input Type - Normal Types",
                                        type: "array",
                                        format: "array",
                                        items: {
                                            title: "Normal Query Input Type",
                                            oneOf: [
                                                {
                                                    $ref: "#/definitions/queryInputCountingType",
                                                    title: "Counting Pattern Type"
                                                },
                                                {
                                                    $ref: "#/definitions/queryInputAndOrType",
                                                    title: "Logical Pattern(And Or) Type"
                                                }
                                            ]
                                        }
                                    },
                                    queryInput2: {
                                        type: "object",
                                        title: "Query Input Type - Negation Events",
                                        oneOf: [
                                            {
                                                $ref: "#/definitions/queryInputNotAndType",
                                                title: "Logical Pattern(Not And) Type"
                                            },
                                            {
                                                $ref: "#/definitions/queryInputNotForType",
                                                title: "Logical Pattern(Not For) Type"
                                            }
                                        ]
                                    }
                                }
                            },
                            querySelect: {
                                propertyOrder: 3,
                                required: true,
                                type: "object",
                                title: "Query Select",
                                properties: {
                                    select: {
                                        title: "Query Select Type",
                                        required: true,
                                        oneOf: [
                                            {
                                                $ref: "#/definitions/querySelectUserDefined",
                                                title: "User-Defined"
                                            },
                                            {
                                                $ref: "#/definitions/querySelectAll",
                                                title: "All"
                                            }
                                        ]
                                    }
                                }
                            },
                            groupBy: {
                                propertyOrder: 4,
                                type: "array",
                                format: "table",
                                title: "Group By Attributes",
                                uniqueItems: true,
                                items: {
                                    type: "object",
                                    title : 'Attribute',
                                    properties: {
                                        attribute: {
                                            type: 'string',
                                            title: 'Attribute Name',
                                            enum: possibleGroupByAttributes,
                                            default: ""
                                        }
                                    }
                                }
                            },
                            having: {
                                propertyOrder: 5,
                                title: "Having",
                                type: "string",
                                minLength: 1
                            },
                            outputRateLimit: {
                                propertyOrder: 6,
                                title: "Output Rate Limit",
                                type: "string",
                                minLength: 1
                            },
                            queryOutput: {
                                propertyOrder: 7,
                                required: true,
                                type: "object",
                                title: "Query Output Type",
                                properties: {
                                    output: {
                                        title: "Query Output Type",
                                        required: true,
                                        oneOf: [
                                            {
                                                $ref: "#/definitions/queryOutputInsertType",
                                                title: "Insert Type"
                                            },
                                            {
                                                $ref: "#/definitions/queryOutputDeleteType",
                                                title: "Delete Type"
                                            },
                                            {
                                                $ref: "#/definitions/queryOutputUpdateType",
                                                title: "Update Type"
                                            },
                                            {
                                                $ref: "#/definitions/queryOutputUpdateOrInsertIntoType",
                                                title: "Update Or Insert Into Type"
                                            }
                                        ]
                                    }
                                }
                            }
                        },
                        definitions: {
                            queryInputCountingType: {
                                title: "Counting Pattern Event",
                                type: "object",
                                properties: {
                                    inputType: {
                                        required: true,
                                        title: "Input Type",
                                        type: "string",
                                        template: "Counting Pattern Event"
                                    },
                                    forEvery: {
                                        required: true,
                                        title: "For Every",
                                        type: "boolean",
                                        enum: [true, false],
                                        default: false
                                    },
                                    eventReference: {
                                        title: "Event Reference",
                                        type: "string",
                                        minLength: 1
                                    },
                                    streamName: {
                                        type: 'string',
                                        title: 'Stream Name',
                                        enum: inputStreams,
                                        required: true
                                    },
                                    filter: {
                                        title: "Filter",
                                        type: "string",
                                        minLength: 1
                                    },
                                    minCount: {
                                        title: "Min Count",
                                        type: "string",
                                        minLength: 1
                                    },
                                    maxCount: {
                                        title: "Max Count",
                                        type: "string",
                                        minLength: 1
                                    },
                                    within: {
                                        title: "Within",
                                        type: "string",
                                        minLength: 1
                                    }
                                }

                            },
                            queryInputAndOrType: {
                                title: "And Or Event",
                                type: "object",
                                properties: {
                                    inputType: {
                                        required: true,
                                        title: "Input Type",
                                        type: "string",
                                        template: "Logical And Or Event"
                                    },
                                    forEvery: {
                                        required: true,
                                        title: "For Every",
                                        type: "boolean",
                                        enum: [true, false],
                                        default: false
                                    },
                                    leftEventReference: {
                                        title: "Left Stream Event Reference",
                                        type: "string",
                                        minLength: 1
                                    },
                                    leftStreamName: {
                                        type: 'string',
                                        title: 'Left Stream Name',
                                        enum: inputStreams,
                                        required: true
                                    },
                                    leftFilter: {
                                        title: "Left Stream Filter",
                                        type: "string",
                                        minLength: 1
                                    },
                                    connectedWith: {
                                        title: "Connected With",
                                        type: "string",
                                        enum: ['and', 'or'],
                                        default: 'and'
                                    },
                                    rightEventReference: {
                                        title: "Right Stream Event Reference",
                                        type: "string",
                                        minLength: 1
                                    },
                                    rightStreamName: {
                                        type: 'string',
                                        title: 'Right Stream Name',
                                        enum: inputStreams,
                                        required: true
                                    },
                                    rightFilter: {
                                        title: "Right Stream Filter",
                                        type: "string",
                                        minLength: 1
                                    },
                                    within: {
                                        title: "Within",
                                        type: "string",
                                        minLength: 1
                                    }
                                }
                            },
                            queryInputNotForType: {
                                title: "Not For Event",
                                type: "object",
                                properties: {
                                    inputType: {
                                        required: true,
                                        title: "Input Type",
                                        type: "string",
                                        template: "Logical Not For Event"
                                    },
                                    forEvery: {
                                        required: true,
                                        title: "For Every",
                                        type: "boolean",
                                        enum: [true, false],
                                        default: false
                                    },
                                    streamName: {
                                        type: 'string',
                                        title: 'Stream Name',
                                        enum: inputStreams,
                                        required: true
                                    },
                                    filter: {
                                        title: "Filter",
                                        type: "string",
                                        minLength: 1
                                    },
                                    forDuration: {
                                        required: true,
                                        title: "Duration",
                                        type: "string",
                                        minLength: 1
                                    }
                                }
                            },
                            queryInputNotAndType: {
                                title: "Not And Event",
                                type: "object",
                                properties: {
                                    inputType: {
                                        required: true,
                                        title: "Input Type",
                                        type: "string",
                                        template: "Logical Not And Event"
                                    },
                                    forEvery: {
                                        required: true,
                                        title: "For Every",
                                        type: "boolean",
                                        enum: [true, false],
                                        default: false
                                    },
                                    leftStreamName: {
                                        type: 'string',
                                        title: 'Left Stream Name',
                                        enum: inputStreams,
                                        required: true
                                    },
                                    leftFilter: {
                                        title: "Left Stream Filter",
                                        type: "string",
                                        minLength: 1
                                    },
                                    rightEventReference: {
                                        title: "Right Stream Event Reference",
                                        type: "string",
                                        minLength: 1
                                    },
                                    rightStreamName: {
                                        type: 'string',
                                        title: 'Right Stream Name',
                                        enum: inputStreams,
                                        required: true
                                    },
                                    rightFilter: {
                                        title: "Right Stream Filter",
                                        type: "string",
                                        minLength: 1
                                    },
                                    within: {
                                        title: "Within",
                                        type: "string",
                                        minLength: 1
                                    }
                                }
                            },
                            querySelectUserDefined: {
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
                            querySelectAll: {
                                type: "string",
                                title: "All",
                                template: '*'
                            },
                            queryOutputInsertType: {
                                required: true,
                                title: "Insert",
                                type: "object",
                                properties: {
                                    outputType: {
                                        required: true,
                                        title: "Output Type",
                                        type: "string",
                                        template: "Insert"
                                    },
                                    eventType: {
                                        required: true,
                                        title: "For Every",
                                        type: "string",
                                        enum: ['current', 'expired', 'all'],
                                        default: 'all'
                                    },
                                    insertTarget: {
                                        type: 'string',
                                        title: 'Insert Into',
                                        template: savedQueryOutputTarget,
                                        required: true
                                    }
                                }
                            },
                            queryOutputDeleteType: {
                                required: true,
                                title: "Delete",
                                type: "object",
                                properties: {
                                    outputType: {
                                        required: true,
                                        title: "Output Type",
                                        type: "string",
                                        template: "Delete"
                                    },
                                    deleteTarget: {
                                        type: 'string',
                                        title: 'Delete From',
                                        template: savedQueryOutputTarget,
                                        required: true
                                    },
                                    forEventType: {
                                        title: "For Every",
                                        type: "string",
                                        enum: ['current', 'expired', 'all'],
                                        default: 'all'
                                    },
                                    on: {
                                        type: 'string',
                                        title: 'On',
                                        minLength: 1,
                                        required: true
                                    }
                                }
                            },
                            queryOutputUpdateType: {
                                required: true,
                                title: "Update",
                                type: "object",
                                properties: {
                                    outputType: {
                                        required: true,
                                        title: "Output Type",
                                        type: "string",
                                        template: "Update"
                                    },
                                    updateTarget: {
                                        type: 'string',
                                        title: 'Update From',
                                        template: savedQueryOutputTarget,
                                        required: true
                                    },
                                    forEventType: {
                                        title: "For Every",
                                        type: "string",
                                        enum: ['current', 'expired', 'all'],
                                        default: 'all'
                                    },
                                    set: {
                                        required: true,
                                        type: "array",
                                        format: "table",
                                        title: "Set",
                                        uniqueItems: true,
                                        items: {
                                            type: "object",
                                            title: 'Set Condition',
                                            properties: {
                                                attribute: {
                                                    type: "string",
                                                    title: 'Attribute',
                                                    minLength: 1
                                                },
                                                value: {
                                                    type: "string",
                                                    title: 'Value',
                                                    minLength: 1
                                                }
                                            }
                                        }
                                    },
                                    on: {
                                        type: 'string',
                                        title: 'On',
                                        minLength: 1,
                                        required: true
                                    }
                                }
                            },
                            queryOutputUpdateOrInsertIntoType: {
                                required: true,
                                title: "Update or Insert Into",
                                type: "object",
                                properties: {
                                    outputType: {
                                        required: true,
                                        title: "Output Type",
                                        type: "string",
                                        template: "Update or Insert Into"
                                    },
                                    updateOrInsertIntoTarget: {
                                        type: 'string',
                                        title: 'Update or Insert Into',
                                        template: savedQueryOutputTarget,
                                        required: true
                                    },
                                    forEventType: {
                                        title: "For Every",
                                        type: "string",
                                        enum: ['current', 'expired', 'all'],
                                        default: 'all'
                                    },
                                    set: {
                                        required: true,
                                        type: "array",
                                        format: "table",
                                        title: "Set",
                                        uniqueItems: true,
                                        items: {
                                            type: "object",
                                            title: 'Set Condition',
                                            properties: {
                                                attribute: {
                                                    type: "string",
                                                    title: 'Attribute',
                                                    minLength: 1
                                                },
                                                value: {
                                                    type: "string",
                                                    title: 'Value',
                                                    minLength: 1
                                                }
                                            }
                                        }
                                    },
                                    on: {
                                        type: 'string',
                                        title: 'On',
                                        minLength: 1,
                                        required: true
                                    }
                                }

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
                    var patternQueryInput = clickedElement.getQueryInput();
                    var newEventObject;
                    patternQueryInput.getEventList().removeAllElements();
                    if (config.queryInput.queryInput1 !== undefined) {
                        _.forEach(config.queryInput.queryInput1, function (queryInputEvent) {
                            if (queryInputEvent.inputType === "Counting Pattern Event") {
                                newEventObject = new PatternQueryInputCounting(queryInputEvent);
                                if (queryInputEvent.eventReference === undefined) {
                                    newEventObject.setEventReference('');
                                }
                                if (queryInputEvent.filter === undefined) {
                                    newEventObject.setFilter('');
                                }
                                if (queryInputEvent.minCount === undefined) {
                                    newEventObject.setMinCount('');
                                }
                                if (queryInputEvent.maxCount === undefined) {
                                    newEventObject.setMaxCount('');
                                }
                                if (queryInputEvent.within === undefined) {
                                    newEventObject.setWithin('');
                                }
                            } else if (queryInputEvent.inputType === "Logical And Or Event") {
                                newEventObject = new PatternQueryInputAndOr(queryInputEvent);
                                if (queryInputEvent.leftEventReference === undefined) {
                                    newEventObject.setLeftEventReference('');
                                }
                                if (queryInputEvent.leftFilter === undefined) {
                                    newEventObject.setLeftFilter('');
                                }
                                if (queryInputEvent.rightEventReference === undefined) {
                                    newEventObject.setRightEventReference('');
                                }
                                if (queryInputEvent.rightFilter === undefined) {
                                    newEventObject.setRightFilter('');
                                }
                                if (queryInputEvent.within === undefined) {
                                    newEventObject.setWithin('');
                                }
                            } else {
                                console.log("Invalid input event type for pattern query received!")
                            }
                            patternQueryInput.addEvent(newEventObject);
                        });
                    }

                    if (config.queryInput.queryInput2 !== undefined
                        && config.queryInput.queryInput2.inputType !== undefined) {
                        if (config.queryInput.queryInput2.inputType === "Logical Not For Event") {
                            newEventObject = new PatternQueryInputNotFor(config.queryInput.queryInput2);
                            if (config.queryInput.queryInput2.filter === undefined) {
                                newEventObject.setFilter('');
                            }
                        } else if (config.queryInput.queryInput2.inputType === "Logical Not And Event") {
                            newEventObject = new PatternQueryInputNotAnd(config.queryInput.queryInput2);
                            if (config.queryInput.queryInput2.leftFilter === undefined) {
                                newEventObject.setLeftFilter('');
                            }
                            if (config.queryInput.queryInput2.rightEventReference === undefined) {
                                newEventObject.setRightEventReference('');
                            }
                            if (config.queryInput.queryInput2.rightFilter === undefined) {
                                newEventObject.setRightFilter('');
                            }
                            if (config.queryInput.queryInput2.within === undefined) {
                                newEventObject.setWithin('');
                            }
                        } else {
                            console.log("Invalid input event type for pattern query received!")
                        }
                        patternQueryInput.addEvent(newEventObject);
                    }

                    var selectAttributeOptions = {};
                    if (config.querySelect.select instanceof Array) {
                        _.set(selectAttributeOptions, 'type', 'user_defined');
                        _.set(selectAttributeOptions, 'value', editor.getValue().querySelect.select);
                    } else if (config.querySelect.select === "*") {
                        _.set(selectAttributeOptions, 'type', 'all');
                        _.set(selectAttributeOptions, 'value', editor.getValue().querySelect.select);
                    } else {
                        console.log("Value other than \"user_defined\" and \"all\" received!");
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

                    if (config.having !== undefined) {
                        clickedElement.setHaving(config.having);
                    } else {
                        clickedElement.setHaving('');
                    }

                    if (config.outputRateLimit !== undefined) {
                        clickedElement.setOutputRateLimit(config.outputRateLimit);
                    } else {
                        clickedElement.setOutputRateLimit('');
                    }

                    var patternQueryOutput = clickedElement.getQueryOutput();
                    var outputObject;
                    var outputType;
                    if (config.queryOutput.output !== undefined) {
                        if (config.queryOutput.output.outputType === "Insert") {
                            outputType = "insert";
                            outputObject = new QueryOutputInsert(config.queryOutput.output);
                            if (config.queryOutput.output.eventType === undefined) {
                                outputObject.setEventType('');
                            }
                        } else if (config.queryOutput.output.outputType === "Delete") {
                            outputType = "delete";
                            outputObject = new QueryOutputDelete(config.queryOutput.output);
                            if (config.queryOutput.output.forEventType === undefined) {
                                outputObject.setForEventType('');
                            }
                        } else if (config.queryOutput.output.outputType === "Update") {
                            outputType = "update";
                            outputObject = new QueryOutputUpdate(config.queryOutput.output);
                            if (config.queryOutput.output.forEventType === undefined) {
                                outputObject.setForEventType('');
                            }
                        } else if (config.queryOutput.output.outputType === "Update or Insert Into") {
                            outputType = "update_or_insert_into";
                            outputObject = new QueryOutputUpdateOrInsertInto(config.queryOutput.output);
                            if (config.queryOutput.output.forEventType === undefined) {
                                outputObject.setForEventType('');
                            }
                        } else {
                            console.log("Invalid output type for pattern query received!")
                        }
                        patternQueryOutput.setOutput(outputObject);
                        patternQueryOutput.setType(outputType);
                    }
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
         * @function generate form for Join Query
         * @param element selected element(query)
         */
        FormBuilder.prototype.GeneratePropertiesFormForJoinQuery = function (element) {
        //     var self = this;
        //     var formConsole = this.createTabForForm();
        //     var formContainer = formConsole.getContentContainer();
        //
        //     // The container and the tool palette are disabled to prevent the user from dropping any elements
        //     self.gridContainer.addClass('disabledbutton');
        //     self.toolPaletteContainer.addClass('disabledbutton');
        //
        //     var id = $(element).parent().attr('id');
        //     var clickedElement = self.appData.getJoinQuery(id);
        //     if (!(clickedElement.getFrom()) || clickedElement.getFrom().length !== 2) {
        //         alert('Connect TWO input streams');
        //         self.gridContainer.removeClass('disabledbutton');
        //         self.toolPaletteContainer.removeClass('disabledbutton');
        //
        //         // close the form window
        //         self.consoleListManager.removeConsole(formConsole);
        //         self.consoleListManager.hideAllConsoles();
        //     }
        //     else if (!(clickedElement.getInsertInto())) {
        //         alert('Connect an output stream');
        //         self.gridContainer.removeClass('disabledbutton');
        //         self.toolPaletteContainer.removeClass('disabledbutton');
        //
        //         // close the form window
        //         self.consoleListManager.removeConsole(formConsole);
        //         self.consoleListManager.hideAllConsoles();
        //     }
        //
        //     else {
        //         var streams = [];
        //         $.each(clickedElement.getFrom(), function (index, streamID) {
        //             streams.push((self.appData.getStream(streamID)).getName());
        //         });
        //         var projections = [];
        //         var insertInto = self.appData.getStream(clickedElement.getInsertInto()).getName();
        //         var outStreamAttributes = (self.appData.getStream(clickedElement.getInsertInto())).getAttributeList();
        //         if (!(clickedElement.getProjection())) {
        //             for (var i = 0; i < outStreamAttributes.length; i++) {
        //                 var attr = {select: '', newName: outStreamAttributes[i].attribute};
        //                 projections.push(attr);
        //             }
        //         }
        //         else {
        //             var selectedAttributes = clickedElement.getProjection();
        //             for (var i = 0; i < outStreamAttributes.length; i++) {
        //                 var attr = {select: selectedAttributes[i], newName: outStreamAttributes[i].attribute};
        //                 projections.push(attr);
        //             }
        //         }
        //         var fillWith = {
        //             projection: projections,
        //             outputType: clickedElement.getOutputType(),
        //             insertInto: insertInto
        //         };
        //
        //         if (clickedElement.getJoin()) {
        //             fillWith.type = clickedElement.getJoin().getType();
        //             fillWith.leftStream = clickedElement.getJoin().getLeftStream();
        //             fillWith.rightStream = clickedElement.getJoin().getRightStream();
        //             fillWith.on = clickedElement.getJoin().getOn();
        //         }
        //         var editor = new JSONEditor(formContainer[0], {
        //             ajax: true,
        //             schema: {
        //                 type: 'object',
        //                 title: 'Query',
        //                 properties: {
        //                     type: {
        //                         type: 'string',
        //                         title: 'Type',
        //                         required: true,
        //                         propertyOrder: 1
        //                     },
        //                     leftStream: {
        //                         type: 'object',
        //                         title: 'Left Stream',
        //                         required: true,
        //                         propertyOrder: 2,
        //                         options: {
        //                             disable_collapse: false,
        //                             disable_properties: true,
        //                             collapsed: true
        //                         },
        //                         properties: {
        //                             from: {
        //                                 type: 'string',
        //                                 title: 'From',
        //                                 enum: streams,
        //                                 required: true
        //                             },
        //                             filter: {
        //                                 type: 'string',
        //                                 title: 'Filter',
        //                                 required: true
        //                             },
        //                             window: {
        //                                 type: 'string',
        //                                 title: 'Window',
        //                                 required: true
        //                             },
        //                             postWindowFilter: {
        //                                 type: 'string',
        //                                 title: 'Post Window Filter',
        //                                 required: true
        //                             },
        //                             as: {
        //                                 type: 'string',
        //                                 title: 'As',
        //                                 required: true
        //                             }
        //                         }
        //                     },
        //                     rightStream: {
        //                         type: 'object',
        //                         title: 'Right Stream',
        //                         required: true,
        //                         propertyOrder: 3,
        //                         options: {
        //                             disable_collapse: false,
        //                             disable_properties: true,
        //                             collapsed: true
        //                         },
        //                         properties: {
        //                             from: {
        //                                 type: 'string',
        //                                 title: 'From',
        //                                 enum: streams,
        //                                 required: true
        //                             },
        //                             filter: {
        //                                 type: 'string',
        //                                 title: 'Filter',
        //                                 required: true
        //                             },
        //                             window: {
        //                                 type: 'string',
        //                                 title: 'Window',
        //                                 required: true
        //                             },
        //                             postWindowFilter: {
        //                                 type: 'string',
        //                                 title: 'Post Window Filter',
        //                                 required: true
        //                             },
        //                             as: {
        //                                 type: 'string',
        //                                 title: 'As',
        //                                 required: true
        //                             }
        //                         }
        //                     },
        //                     on: {
        //                         type: 'string',
        //                         title: 'On',
        //                         required: true,
        //                         propertyOrder: 4
        //                     },
        //                     projection: {
        //                         type: 'array',
        //                         title: 'Projection',
        //                         format: 'table',
        //                         required: true,
        //                         propertyOrder: 5,
        //                         options: {
        //                             disable_array_add: true,
        //                             disable_array_delete: true,
        //                             disable_array_reorder: true
        //                         },
        //                         items: {
        //                             type: 'object',
        //                             title: 'Attribute',
        //                             properties: {
        //                                 select: {
        //                                     type: 'string',
        //                                     title: 'select'
        //                                 },
        //                                 newName: {
        //                                     type: 'string',
        //                                     title: 'as'
        //                                 }
        //                             }
        //                         }
        //                     },
        //                     outputType: {
        //                         type: 'string',
        //                         title: 'Output Type',
        //                         enum: ['all events', 'current events', 'expired events'],
        //                         default: 'all events',
        //                         required: true,
        //                         propertyOrder: 6
        //                     },
        //                     insertInto: {
        //                         type: 'string',
        //                         title: 'Insert Into',
        //                         template: insertInto,
        //                         required: true,
        //                         propertyOrder: 7
        //                     }
        //                 }
        //             },
        //             startval: fillWith,
        //             no_additional_properties: true,
        //             disable_properties: true
        //         });
        //         for (var i = 0; i < outStreamAttributes.length; i++) {
        //             editor.getEditor('root.projection.' + i + '.newName').disable();
        //         }
        //         editor.getEditor('root.insertInto').disable();
        //
        //         $(formContainer).append('<div id="form-submit"><button type="button" ' +
        //             'class="btn btn-default">Submit</button></div>' +
        //             '<div id="form-cancel"><button type="button" class="btn btn-default">Cancel</button></div>');
        //
        //         // 'Submit' button action
        //         var submitButtonElement = $('#form-submit')[0];
        //         submitButtonElement.addEventListener('click', function () {
        //             self.gridContainer.removeClass('disabledbutton');
        //             self.toolPaletteContainer.removeClass('disabledbutton');
        //
        //             // close the form window
        //             self.consoleListManager.removeConsole(formConsole);
        //             self.consoleListManager.hideAllConsoles();
        //
        //             var config = editor.getValue();
        //             // update selected query object
        //             var leftStreamOptions = {};
        //             _.set(leftStreamOptions, 'from', config.leftStream.from);
        //             _.set(leftStreamOptions, 'filter', config.leftStream.filter);
        //             _.set(leftStreamOptions, 'window', config.leftStream.window);
        //             _.set(leftStreamOptions, 'postWindowFilter', config.leftStream.postWindowFilter);
        //             _.set(leftStreamOptions, 'as', config.leftStream.as);
        //             var leftStream = new LeftStream(leftStreamOptions);
        //
        //             var rightStreamOptions = {};
        //             _.set(leftStreamOptions, 'from', config.rightStream.from);
        //             _.set(leftStreamOptions, 'filter', config.rightStream.filter);
        //             _.set(leftStreamOptions, 'window', config.rightStream.window);
        //             _.set(leftStreamOptions, 'postWindowFilter', config.rightStream.postWindowFilter);
        //             _.set(leftStreamOptions, 'as', config.rightStream.as);
        //             var rightStream = new RightStream(rightStreamOptions);
        //
        //             var joinOptions = {};
        //             _.set(joinOptions, 'type', config.type);
        //             _.set(joinOptions, 'left-stream', leftStream);
        //             _.set(joinOptions, 'right-stream', rightStream);
        //             _.set(joinOptions, 'on', config.on);
        //             var join =new Join(joinOptions);
        //
        //             clickedElement.setJoin(join);
        //             var projections = [];
        //             $.each(config.projection, function (index, attribute) {
        //                 projections.push(attribute.select);
        //             });
        //             clickedElement.setProjection(projections);
        //             clickedElement.setOutputType(config.outputType);
        //         });
        //
        //         // 'Cancel' button action
        //         var cancelButtonElement = $('#form-cancel')[0];
        //         cancelButtonElement.addEventListener('click', function () {
        //             self.gridContainer.removeClass('disabledbutton');
        //             self.toolPaletteContainer.removeClass('disabledbutton');
        //
        //             // close the form window
        //             self.consoleListManager.removeConsole(formConsole);
        //             self.consoleListManager.hideAllConsoles();
        //         });
        //     }
        };

        /**
         * @function generate form for Partition
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
