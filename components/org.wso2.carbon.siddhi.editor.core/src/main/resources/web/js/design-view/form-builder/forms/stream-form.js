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

define(['require', 'log', 'jquery', 'lodash', 'attribute', 'stream', 'designViewUtils'],
    function (require, log, $, _, Attribute, Stream, DesignViewUtils) {

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
        StreamForm.prototype.generateDefineForm = function (i, formConsole, formContainer) {
            var self = this;
            var propertyDiv = $('<div id="property-header"><h3>Define Stream </h3></div>' +
                '<div id="define-stream" class="define-stream"></div>');
            formContainer.append(propertyDiv);

            // generate the form to define a stream
            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Stream",
                    properties: {
                        annotations: {
                            propertyOrder: 1,
                            type: "array",
                            format: "table",
                            title: "Annotations",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title : "Annotation",
                                options: {
                                    disable_properties: true
                                },
                                properties: {
                                    annotation: {
                                        title : "Annotation",
                                        type: "string",
                                        minLength: 1
                                    }
                                }
                            }
                        },
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 2
                        },
                        attributes: {
                            required: true,
                            propertyOrder: 3,
                            type: "array",
                            format: "table",
                            title: "Attributes",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title : 'Attribute',
                                options: {
                                    disable_properties: true
                                },
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
                                            "bool"
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
                var isStreamNameUsed = self.formUtils.isDefinitionElementNameUsed(editor.getValue().name);
                if (isStreamNameUsed) {
                    DesignViewUtils.prototype
                        .errorAlert("Stream name \"" + editor.getValue().name + "\" is already used.");
                    return;
                }
                // add the new out stream to the stream array
                var streamOptions = {};
                _.set(streamOptions, 'id', i);
                _.set(streamOptions, 'name', editor.getValue().name);
                var stream = new Stream(streamOptions);
                _.forEach(editor.getValue().attributes, function (attribute) {
                    var attributeObject = new Attribute(attribute);
                    stream.addAttribute(attributeObject);
                });
                _.forEach(editor.getValue().annotations, function (annotation) {
                    stream.addAnnotation(annotation.annotation);
                });
                self.configurationData.getSiddhiAppConfig().addStream(stream);

                var textNode = $('#'+i).find('.streamNameNode');
                textNode.html(editor.getValue().name);

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);

                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

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
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var id = $(element).parent().attr('id');
            // retrieve the stream information from the collection
            var clickedElement = self.configurationData.getSiddhiAppConfig().getStream(id);
            if(!clickedElement) {
                var errorMessage = 'unable to find clicked element';
                log.error(errorMessage);
                throw errorMessage;
            }
            var name = clickedElement.getName();
            var savedAttributes = clickedElement.getAttributeList();
            var attributes = [];
            _.forEach(savedAttributes, function (savedAttribute) {
                var attributeObject = {
                    name: savedAttribute.getName(),
                    type: (savedAttribute.getType()).toLowerCase()
                };
                attributes.push(attributeObject);
            });
            var savedAnnotations = clickedElement.getAnnotationList();
            var annotations = [];
            _.forEach(savedAnnotations, function (savedAnnotation) {
                annotations.push({annotation: savedAnnotation});
            });

            var fillWith = {
                annotations : annotations,
                name : name,
                attributes : attributes
            };
            fillWith = self.formUtils.cleanJSONObject(fillWith);
            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Stream",
                    properties: {
                        annotations: {
                            propertyOrder: 1,
                            type: "array",
                            format: "table",
                            title: "Annotations",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title : "Annotation",
                                options: {
                                    disable_properties: true
                                },
                                properties: {
                                    annotation: {
                                        title : "Annotation",
                                        type: "string",
                                        minLength: 1
                                    }
                                }
                            }
                        },
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 2
                        },
                        attributes: {
                            required: true,
                            propertyOrder: 3,
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
                                            "bool"
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
            formContainer.append(self.formUtils.buildFormButtons(true));

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#btn-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                var errors = editor.validate();
                if(errors.length) {
                    return;
                }

                var config = editor.getValue();
                var streamName;
                var firstCharacterInStreamName;
                var isStreamNameUsed;
                /*
                * check whether the stream is inside a partition and if yes check whether it begins with '#'. If not add
                * '#' to the beginning of the stream name.
                * */
                var isStreamSavedInsideAPartition
                    = self.configurationData.getSiddhiAppConfig().getStreamSavedInsideAPartition(id);
                if (isStreamSavedInsideAPartition === undefined) {
                    firstCharacterInStreamName = (config.name).charAt(0);
                    if (firstCharacterInStreamName === '#') {
                        DesignViewUtils.prototype.errorAlert("'#' is used to define inner streams only.");
                        return;
                    } else {
                        streamName = config.name;
                    }
                    isStreamNameUsed
                        = self.formUtils.isDefinitionElementNameUsed(streamName, id);
                    if (isStreamNameUsed) {
                        DesignViewUtils.prototype.errorAlert("Stream name \"" + streamName + "\" is already defined.");
                        return;
                    }
                } else {
                    firstCharacterInStreamName = (config.name).charAt(0);
                    if (firstCharacterInStreamName !== '#') {
                        streamName = '#' + config.name;
                    } else {
                        streamName = config.name;
                    }
                    isStreamNameUsed = self.formUtils
                        .isStreamDefinitionNameUsedInPartition(streamName, id);
                    if (isStreamNameUsed) {
                        DesignViewUtils.prototype
                            .errorAlert("Stream name \"" + streamName + "\" is already defined in the partition.");
                        return;
                    }
                }

                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

                var previouslySavedName = clickedElement.getName();
                // update connection related to the element if the name is changed
                if (previouslySavedName !== streamName) {
                    // update selected stream model
                    clickedElement.setName(streamName);
                    self.formUtils.updateConnectionsAfterDefinitionElementNameChange(id);
                }
                // removing all elements from attribute list
                clickedElement.clearAttributeList();
                // adding new attributes to the attribute list
                _.forEach(config.attributes, function (attribute) {
                    var attributeObject = new Attribute(attribute);
                    clickedElement.addAttribute(attributeObject);
                });

                clickedElement.clearAnnotationList();
                _.forEach(config.annotations, function (annotation) {
                    clickedElement.addAnnotation(annotation.annotation);
                });

                var textNode = $(element).parent().find('.streamNameNode');
                textNode.html(streamName);

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            });

            // 'Cancel' button action
            var cancelButtonElement = $(formContainer).find('#btn-cancel')[0];
            cancelButtonElement.addEventListener('click', function () {
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            });
        };

        return StreamForm;
    });
