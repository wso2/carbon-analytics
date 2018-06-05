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

define(['require', 'log', 'jquery', 'lodash', 'functionDefinition'],
    function (require, log, $, _, FunctionDefinition) {

        /**
         * @class FunctionForm Creates a forms to collect data from a function
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var FunctionForm = function (options) {
            if (options !== undefined) {
                this.configurationData = options.configurationData;
                this.application = options.application;
                this.formUtils = options.formUtils;
                this.consoleListManager = options.application.outputController;
                var currentTabId = this.application.tabController.activeTab.cid;
                this.designViewContainer = $('#design-container-' + currentTabId);
                this.toggleViewButton = $('#toggle-view-button-' + currentTabId);
            }
        };

        /**
         * @function generate form when defining a form
         * @param i id for the element
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        FunctionForm.prototype.generateDefineForm = function (i, formConsole, formContainer) {
            var self = this;
            var propertyDiv = $('<div id="property-header"><h3>Define Function </h3></div>' +
                '<div id="define-function" class="define-function"></div>');
            formContainer.append(propertyDiv);

            // generate the form to define a function
            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Function",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 1
                        },
                        scriptType: {
                            propertyOrder: 2,
                            required: true,
                            type: "string",
                            title: "Script Type",
                            enum: [
                                "Javascript",
                                "R",
                                "Scala"
                            ],
                            default: "Javascript"
                        },
                        returnType: {
                            propertyOrder: 3,
                            required: true,
                            type: "string",
                            title: "Script Type",
                            enum: [
                                "int",
                                "long",
                                "double",
                                "float",
                                "string",
                                "bool",
                                "object"
                            ],
                            default: "int"
                        },
                        body: {
                            propertyOrder: 4,
                            required: true,
                            type: "string",
                            title: "Script Body",
                            format: "textarea",
                            minLength: 1
                        }
                    }
                },
                show_errors: "always",
                disable_properties: true,
                display_required_only: true,
                no_additional_properties: true
            });

            formContainer.append('<div id="submit"><button type="button" class="btn btn-default">Submit</button></div>');

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#submit')[0];
            submitButtonElement.addEventListener('click', function () {

                var errors = editor.validate();
                if(errors.length) {
                    return;
                }
                var isFunctionNameUsed = self.formUtils.isDefinitionElementNameUnique(editor.getValue().name);
                if (isFunctionNameUsed) {
                    alert("Function name \"" + editor.getValue().name + "\" is already used.");
                    return;
                }
                // add the new out function to the function array
                var functionOptions = {};
                _.set(functionOptions, 'id', i);
                _.set(functionOptions, 'name', editor.getValue().name);
                _.set(functionOptions, 'scriptType', (editor.getValue().scriptType).toUpperCase());
                _.set(functionOptions, 'returnType', (editor.getValue().returnType).toUpperCase());
                _.set(functionOptions, 'body', editor.getValue().body);
                var functionObject = new FunctionDefinition(functionOptions);
                self.configurationData.getSiddhiAppConfig().addFunction(functionObject);

                var textNode = $('#'+i).find('.functionNameNode');
                textNode.html(editor.getValue().name);

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);

                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');
            });
            return editor.getValue().name;
        };

        /**
         * @function generate properties form for a function
         * @param element selected element(function)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        FunctionForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var id = $(element).parent().attr('id');
            // retrieve the function information from the collection
            var clickedElement = self.configurationData.getSiddhiAppConfig().getFunction(id);
            if(clickedElement === undefined) {
                var errorMessage = 'unable to find clicked element';
                log.error(errorMessage);
            }
            var name = clickedElement.getName();
            var scriptType = (clickedElement.getScriptType()).toLowerCase();
            if (scriptType === 'javascript') {
                scriptType = 'Javascript';
            } else if (scriptType === "r") {
                scriptType = 'R';
            } else if (scriptType === "scala") {
                scriptType = 'Scala';
            } else {
                console.log("Unknown script type received!")
            }
            var returnType = (clickedElement.getReturnType()).toLowerCase();
            var body = clickedElement.getBody();

            var fillWith = {
                name : name,
                scriptType : scriptType,
                returnType: returnType,
                body: body
            };

            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Function",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 1
                        },
                        scriptType: {
                            propertyOrder: 2,
                            required: true,
                            type: "string",
                            title: "Script Type",
                            enum: [
                                "Javascript",
                                "R",
                                "Scala"
                            ],
                            default: "Javascript"
                        },
                        returnType: {
                            propertyOrder: 3,
                            required: true,
                            type: "string",
                            title: "Script Type",
                            enum: [
                                "int",
                                "long",
                                "double",
                                "float",
                                "string",
                                "bool",
                                "object"
                            ],
                            default: "int"
                        },
                        body: {
                            propertyOrder: 4,
                            required: true,
                            type: "string",
                            title: "Script Body",
                            format: "textarea",
                            minLength: 1
                        }
                    }
                },
                startval: fillWith,
                show_errors: "always",
                disable_properties: true,
                display_required_only: true,
                no_additional_properties: true
            });
            formContainer.append('<div id="form-submit"><button type="button" ' +
                'class="btn btn-default">Submit</button></div>' +
                '<div id="form-cancel"><button type="button" class="btn btn-default">Cancel</button></div>');

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#form-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                var errors = editor.validate();
                if(errors.length) {
                    return;
                }
                var isFunctionNameUsed = self.formUtils.isDefinitionElementNameUnique(editor.getValue().name,
                    clickedElement.getId());
                if (isFunctionNameUsed) {
                    alert("Function name \"" + editor.getValue().name + "\" is already used.");
                    return;
                }
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

                var config = editor.getValue();

                // update selected function model
                clickedElement.setName(config.name);
                clickedElement.setScriptType((config.scriptType).toUpperCase());
                clickedElement.setReturnType((config.returnType).toUpperCase());
                clickedElement.setBody(config.body);

                var textNode = $(element).parent().find('.functionNameNode');
                textNode.html(config.name);

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            });

            // 'Cancel' button action
            var cancelButtonElement = $(formContainer).find('#form-cancel')[0];
            cancelButtonElement.addEventListener('click', function () {
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            });
        };

        return FunctionForm;
    });
