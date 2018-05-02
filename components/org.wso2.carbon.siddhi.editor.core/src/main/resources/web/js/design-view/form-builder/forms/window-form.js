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

define(['require', 'log', 'jquery', 'lodash', 'attribute', 'window'],
    function (require, log, $, _, Attribute, Window) {

        /**
         * @class WindowForm Creates a forms to collect data from a window
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var WindowForm = function (options) {
            this.configurationData = options.configurationData;
            this.application = options.application;
            this.formUtils = options.formUtils;
            this.consoleListManager = options.application.outputController;
            this.gridContainer = $("#grid-container");
            this.toolPaletteContainer = $("#tool-palette-container");
        };

        /**
         * @function generate form when defining a form
         * @param i id for the element
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        WindowForm.prototype.generateDefineForm = function (i, formConsole, formContainer) {
            var self = this;
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
                show_errors: "always",
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                display_required_only: true,
                no_additional_properties: true
            });

            formContainer.append('<div id="submit"><button type="button" class="btn btn-default">Submit</button></div>');

            // 'Submit' button action
            var submitButtonElement = $('#submit')[0];
            submitButtonElement.addEventListener('click', function () {
                var isWindowNameUsed = self.formUtils.IsDefinitionElementNameUnique(editor.getValue().name);
                if (isWindowNameUsed) {
                    alert("Window name \"" + editor.getValue().name + "\" is already used.");
                    return;
                }
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
                self.configurationData.getSiddhiAppConfig().addWindow(window);

                var textNode = $('#'+i).find('.windowNameNode');
                textNode.html(editor.getValue().name);

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();

                self.gridContainer.removeClass("disabledbutton");
                self.toolPaletteContainer.removeClass("disabledbutton");
            });
            return editor.getValue().name;
        };

        /**
         * @function generate properties form for a window
         * @param element selected element(window)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        WindowForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            // The container and the tool palette are disabled to prevent the user from dropping any elements
            self.gridContainer.addClass("disabledbutton");
            self.toolPaletteContainer.addClass("disabledbutton");

            var id = $(element).parent().attr('id');
            // retrieve the window information from the collection
            var clickedElement = self.configurationData.getSiddhiAppConfig().getWindow(id);
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
                show_errors: "always",
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
                var isWindowNameUsed = self.formUtils.IsDefinitionElementNameUnique(editor.getValue().name,
                    clickedElement.getId());
                if (isWindowNameUsed) {
                    alert("Window name \"" + editor.getValue().name + "\" is already used.");
                    return;
                }
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
                _.forEach(config.attributes, function (attribute) {
                    var attributeObject = new Attribute(attribute);
                    clickedElement.addAttribute(attributeObject);
                });

                var textNode = $(element).parent().find('.windowNameNode');
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

        return WindowForm;
    });
