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

define(['require', 'log', 'jquery', 'lodash', 'attribute', 'stream'],
    function (require, log, $, _, Attribute, Stream) {

        /**
         * @class StreamForm Creates a forms to collect data from a stream
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var StreamForm = function (options) {
            if (options !== undefined) {
                this.configurationData = options.configurationData;
                this.application = options.application;
                this.formUtils = options.formUtils;
                this.consoleListManager = options.application.outputController;
            }
            this.gridContainer = $("#grid-container");
            this.toolPaletteContainer = $("#tool-palette-container");
        };

        /**
         * @function generate form when defining a form
         * @param i id for the element
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        StreamForm.prototype.generateDefineForm = function (i, formConsole, formContainer) {
            var self = this;
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
                        //         },
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
                show_errors: "always",
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

                var errors = editor.validate();
                if(errors.length) {
                    return;
                }
                var isStreamNameUsed = self.formUtils.IsDefinitionElementNameUnique(editor.getValue().name);
                if (isStreamNameUsed) {
                    alert("Stream name \"" + editor.getValue().name + "\" is already used.");
                    return;
                }
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
                self.configurationData.getSiddhiAppConfig().addStream(stream);

                var textNode = $('#'+i).find('.streamNameNode');
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
         * @function generate properties form for a stream
         * @param element selected element(stream)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        StreamForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            // The container and the tool palette are disabled to prevent the user from dropping any elements
            self.gridContainer.addClass("disabledbutton");
            self.toolPaletteContainer.addClass("disabledbutton");

            var id = $(element).parent().attr('id');
            // retrieve the stream information from the collection
            var clickedElement = self.configurationData.getSiddhiAppConfig().getStream(id);
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
                show_errors: "always",
                disable_properties: false,
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

                var errors = editor.validate();
                if(errors.length) {
                    return;
                }
                var isStreamNameUsed = self.formUtils.IsDefinitionElementNameUnique(editor.getValue().name,
                    clickedElement.getId());
                if (isStreamNameUsed) {
                    alert("Stream name \"" + editor.getValue().name + "\" is already used.");
                    return;
                }
                // The container and the palette are disabled to prevent the user from dropping any elements
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                var config = editor.getValue();

                // update selected stream model
                clickedElement.setName(config.name);
                // removing all elements from attribute list
                clickedElement.clearAttributeList();
                // adding new attributes to the attribute list
                _.forEach(config.attributes, function (attribute) {
                    var attributeObject = new Attribute(attribute);
                    clickedElement.addAttribute(attributeObject);
                });

                var textNode = $(element).parent().find('.streamNameNode');
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

        return StreamForm;
    });
