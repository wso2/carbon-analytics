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

define(['require', 'log', 'jquery', 'lodash', 'sourceOrSinkAnnotation', 'mapAnnotation', 'payloadOrAttribute',
        'jsonValidator'],
    function (require, log, $, _, SourceOrSinkAnnotation, MapAnnotation, PayloadOrAttribute, JSONValidator) {

        var sinkSchema = {
            type: "object",
            title: "Sink",
            properties: {
                annotationType: {
                    required: true,
                    propertyOrder: 1,
                    type: "object",
                    title: "Type",
                    options: {
                        disable_properties: true
                    },
                    properties: {
                        name: {
                            type: "string",
                            title: "Name",
                            required: true,
                            minLength: 1
                        }
                    }
                },
                annotationOptions: {
                    propertyOrder: 2,
                    type: "array",
                    format: "table",
                    title: "Options",
                    uniqueItems: true,
                    minItems: 1,
                    items: {
                        type: "object",
                        title: 'Option',
                        properties: {
                            optionValue: {
                                title: 'Value',
                                type: "string",
                                required: true,
                                minLength: 1
                            }
                        }
                    }
                },
                map: {
                    propertyOrder: 3,
                    title: "Map",
                    type: "object",
                    properties: {
                        annotationType: {
                            required: true,
                            propertyOrder: 1,
                            type: "object",
                            title: "Type",
                            options: {
                                disable_properties: true
                            },
                            properties: {
                                name: {
                                    type: "string",
                                    title: "Name",
                                    required: true,
                                    minLength: 1
                                }
                            }
                        },
                        annotationOptions: {
                            propertyOrder: 2,
                            type: "array",
                            format: "table",
                            title: "Options",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title: 'Option',
                                properties: {
                                    optionValue: {
                                        title: 'Value',
                                        type: "string",
                                        required: true,
                                        minLength: 1
                                    }
                                }
                            }
                        },
                        attributeOrPayloadValues: {
                            propertyOrder: 3,
                            title: "Payload or attribute Mapping",
                            oneOf: [
                                {
                                    $ref: "#/definitions/payloadMapValues",
                                    title: "Enter payload as key/value pairs"
                                },
                                {
                                    $ref: "#/definitions/payloadSingleValue",
                                    title: "Enter a single payload attribute"
                                },
                                {
                                    $ref: "#/definitions/attributeMapValues",
                                    title: "Enter attributes as key/value pairs"
                                },
                                {
                                    $ref: "#/definitions/attributeListValues",
                                    title: "Enter attributes as a list"
                                }
                            ]
                        }
                    }
                }
            },
            definitions: {
                payloadMapValues: {
                    required: true,
                    type: "array",
                    format: "table",
                    title: "Payload Attributes",
                    uniqueItems: true,
                    minItems: 1,
                    items: {
                        type: "object",
                        title: 'Attribute',
                        properties: {
                            payloadMapKey: {
                                title: 'Key',
                                type: "string",
                                required: true,
                                minLength: 1
                            },
                            payloadMapValue: {
                                title: 'Value',
                                type: "string",
                                required: true,
                                minLength: 1
                            }
                        }
                    }
                },
                payloadSingleValue: {
                    required: true,
                    type: "object",
                    title: " Payload Attribute",
                    properties: {
                        singleValue: {
                            title: 'Value',
                            type: "string",
                            required: true,
                            minLength: 1
                        }
                    }
                },
                attributeMapValues: {
                    required: true,
                    type: "array",
                    format: "table",
                    title: "Attributes",
                    uniqueItems: true,
                    minItems: 1,
                    items: {
                        type: "object",
                        title: 'Attribute',
                        properties: {
                            attributeMapKey: {
                                title: 'Key',
                                type: "string",
                                required: true,
                                minLength: 1
                            },
                            attributeMapValue: {
                                title: 'Value',
                                type: "string",
                                required: true,
                                minLength: 1
                            }
                        }
                    }
                },
                attributeListValues: {
                    required: true,
                    type: "array",
                    format: "table",
                    title: "Attributes",
                    uniqueItems: true,
                    minItems: 1,
                    items: {
                        type: "object",
                        title: 'Attribute',
                        properties: {
                            attributeListValue: {
                                title: 'Value',
                                type: "string",
                                required: true,
                                minLength: 1
                            }
                        }
                    }
                }
            }
        };

        /**
         * @class SinkForm Creates a forms to collect data from a sink
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var SinkForm = function (options) {
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
        SinkForm.prototype.generateDefineForm = function (i, formConsole, formContainer) {
            var self = this;
            var propertyDiv = $('<div id="property-header"><h3>Sink Configuration</h3></div>' +
                '<div id="define-sink" class="define-sink"></div>');
            formContainer.append(propertyDiv);

            // generate the form to define a sink
            var editor = new JSONEditor(formContainer.find('.define-sink')[0], {
                schema: sinkSchema,
                show_errors: "always",
                disable_properties: false,
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                display_required_only: true,
                no_additional_properties: true
            });

            formContainer.append(self.formUtils.buildFormButtons());

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#btn-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                var errors = editor.validate();
                if (errors.length) {
                    return;
                }

                // set the isDesignViewContentChanged to true
                self.configurationData.setIsDesignViewContentChanged(true);

                // add the new out sink to the sink array
                var sinkOptions = {};
                _.set(sinkOptions, 'id', i);
                _.set(sinkOptions, 'annotationType', 'SINK');
                _.set(sinkOptions, 'type', editor.getValue().annotationType.name);

                var annotationOptions = [];
                if (editor.getValue().annotationOptions !== undefined) {
                    _.forEach(editor.getValue().annotationOptions, function (option) {
                        annotationOptions.push(option.optionValue);
                    });
                    _.set(sinkOptions, 'options', annotationOptions);
                } else {
                    _.set(sinkOptions, 'options', undefined);
                }

                if (editor.getValue().map !== undefined) {

                    var mapperOptions = {};
                    _.set(mapperOptions, 'type', editor.getValue().map.annotationType.name);

                    var mapperAnnotationOptions = [];
                    if (editor.getValue().map.annotationOptions !== undefined) {
                        _.forEach(editor.getValue().map.annotationOptions, function (option) {
                            mapperAnnotationOptions.push(option.optionValue);
                        });
                        _.set(mapperOptions, 'options', mapperAnnotationOptions);
                    } else {
                        _.set(mapperOptions, 'options', undefined);
                    }

                    if (editor.getValue().map.attributeOrPayloadValues !== undefined) {
                        // if attributeValues[0] or payloadMapValues[0] is defined then mapper annotations values are
                        // saved as a map. If payloadSingleValue is defined then the value is saved as a single value.
                        // Otherwise as a list.
                        var payloadOrAttributeOptions;
                        var payloadOrAttributeObject;
                        if (editor.getValue().map.attributeOrPayloadValues.length !== undefined
                            && editor.getValue().map.attributeOrPayloadValues[0].attributeMapKey !== undefined) {
                            var mapperAttributeValues = {};
                            _.forEach(editor.getValue().map.attributeOrPayloadValues, function (attributeValue) {
                                mapperAttributeValues[attributeValue.attributeMapKey] = attributeValue.attributeMapValue;
                            });

                            payloadOrAttributeOptions = {};
                            _.set(payloadOrAttributeOptions, 'annotationType', 'ATTRIBUTES');
                            _.set(payloadOrAttributeOptions, 'type', 'MAP');
                            _.set(payloadOrAttributeOptions, 'value', mapperAttributeValues);
                            payloadOrAttributeObject = new PayloadOrAttribute(payloadOrAttributeOptions);

                        } else if (editor.getValue().map.attributeOrPayloadValues.length !== undefined
                            && editor.getValue().map.attributeOrPayloadValues[0].attributeListValue !== undefined) {
                            var mapperAttributeValuesArray = [];
                            _.forEach(editor.getValue().map.attributeOrPayloadValues, function (attributeValue) {
                                mapperAttributeValuesArray.push(attributeValue.attributeListValue);
                            });

                            payloadOrAttributeOptions = {};
                            _.set(payloadOrAttributeOptions, 'annotationType', 'ATTRIBUTES');
                            _.set(payloadOrAttributeOptions, 'type', 'LIST');
                            _.set(payloadOrAttributeOptions, 'value', mapperAttributeValuesArray);
                            payloadOrAttributeObject = new PayloadOrAttribute(payloadOrAttributeOptions);

                        } else if (editor.getValue().map.attributeOrPayloadValues.length !== undefined
                            && editor.getValue().map.attributeOrPayloadValues[0].payloadMapKey !== undefined) {
                            var mapperPayloadValues = {};
                            _.forEach(editor.getValue().map.attributeOrPayloadValues, function (payloadMapValue) {
                                mapperPayloadValues[payloadMapValue.payloadMapKey] = payloadMapValue.payloadMapValue;
                            });

                            payloadOrAttributeOptions = {};
                            _.set(payloadOrAttributeOptions, 'annotationType', 'PAYLOAD');
                            _.set(payloadOrAttributeOptions, 'type', 'MAP');
                            _.set(payloadOrAttributeOptions, 'value', mapperPayloadValues);
                            payloadOrAttributeObject = new PayloadOrAttribute(payloadOrAttributeOptions);

                        } else if (editor.getValue().map.attributeOrPayloadValues.singleValue !== undefined) {

                            var mapperPayloadValuesList = [editor.getValue().map.attributeOrPayloadValues.singleValue];
                            payloadOrAttributeOptions = {};
                            _.set(payloadOrAttributeOptions, 'annotationType', 'PAYLOAD');
                            _.set(payloadOrAttributeOptions, 'type', 'LIST');
                            _.set(payloadOrAttributeOptions, 'value', mapperPayloadValuesList);
                            payloadOrAttributeObject = new PayloadOrAttribute(payloadOrAttributeOptions);

                        }
                        _.set(mapperOptions, 'payloadOrAttribute', payloadOrAttributeObject);
                    } else {
                        _.set(mapperOptions, 'payloadOrAttribute', undefined);
                    }

                    var mapperObject = new MapAnnotation(mapperOptions);
                    _.set(sinkOptions, 'map', mapperObject);

                } else {
                    _.set(sinkOptions, 'map', undefined);
                }

                var sink = new SourceOrSinkAnnotation(sinkOptions);
                self.configurationData.getSiddhiAppConfig().addSink(sink);

                var textNode = $('#' + i).find('.sinkNameNode');
                textNode.html(editor.getValue().annotationType.name);

                // perform JSON validation
                JSONValidator.prototype.validateSourceOrSinkAnnotation(sink, 'Sink');

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);

                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

            });

            return editor.getValue().annotationType.name;
        };

        /**
         * @function generate properties form for a sink
         * @param element selected element(sink)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        SinkForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var propertyDiv = $('<div id="property-header"><h3>Sink Configuration</h3></div>' +
                '<div id="define-sink" class="define-sink"></div>');
            formContainer.append(propertyDiv);

            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var id = $(element).parent().attr('id');
            // retrieve the sink information from the collection
            var clickedElement = self.configurationData.getSiddhiAppConfig().getSink(id);
            if (!clickedElement) {
                var errorMessage = 'unable to find clicked element';
                log.error(errorMessage);
                throw errorMessage;
            }
            var type = clickedElement.getType();
            var savedSinkOptions = clickedElement.getOptions();
            var map = clickedElement.getMap();

            var sinkOptionsArray = [];
            if (savedSinkOptions !== undefined) {
                _.forEach(savedSinkOptions, function (option) {
                    sinkOptionsArray.push({optionValue: option});
                });
            }

            var mapperType;
            var mapperOptionsArray = [];
            var payloadOrAttributeObject = {};
            if (map !== undefined) {
                mapperType = map.getType();

                var savedMapperOptions = map.getOptions();
                if (savedMapperOptions !== undefined) {
                    _.forEach(savedMapperOptions, function (option) {
                        mapperOptionsArray.push({optionValue: option});
                    });
                }

                var savedMapperPayloadOrAttribute = map.getPayloadOrAttribute();
                if (savedMapperPayloadOrAttribute !== undefined
                    && savedMapperPayloadOrAttribute.getAnnotationType() !== undefined
                    && savedMapperPayloadOrAttribute.getAnnotationType() === 'ATTRIBUTES') {
                    var attributeValue;
                    var mapperAttributesArray = [];
                    if (savedMapperPayloadOrAttribute.getType() === 'MAP') {
                        attributeValue = savedMapperPayloadOrAttribute.getValue();
                        for (var key in attributeValue) {
                            if (attributeValue.hasOwnProperty(key)) {
                                mapperAttributesArray.push({
                                    attributeMapKey: key,
                                    attributeMapValue: attributeValue[key]
                                });
                            }
                        }

                        payloadOrAttributeObject = mapperAttributesArray;
                    } else if (savedMapperPayloadOrAttribute.getType() === 'LIST') {
                        attributeValue = savedMapperPayloadOrAttribute.getValue();
                        _.forEach(attributeValue, function (attribute) {
                            mapperAttributesArray.push({attributeListValue: attribute})
                        });
                        payloadOrAttributeObject = mapperAttributesArray;
                    } else {
                        console.log("Unknown mapper attribute type detected!");
                    }
                } else if (savedMapperPayloadOrAttribute !== undefined
                    && savedMapperPayloadOrAttribute.getAnnotationType() !== undefined
                    && savedMapperPayloadOrAttribute.getAnnotationType() === 'PAYLOAD') {

                    var payloadValue;
                    if (savedMapperPayloadOrAttribute.getType() === 'MAP') {
                        var mapperPayloadValuesArray = [];
                        payloadValue = savedMapperPayloadOrAttribute.getValue();
                        for (var key in payloadValue) {
                            if (payloadValue.hasOwnProperty(key)) {
                                mapperPayloadValuesArray.push({
                                    payloadMapKey: key,
                                    payloadMapValue: payloadValue[key]
                                });
                            }
                        }
                        payloadOrAttributeObject = mapperPayloadValuesArray;
                    } else if (savedMapperPayloadOrAttribute.getType() === 'LIST') {
                        if (savedMapperPayloadOrAttribute.getValue().length === 1) {
                            payloadValue = savedMapperPayloadOrAttribute.getValue()[0];
                            payloadOrAttributeObject = {singleValue: payloadValue};
                        } else {
                            // Mapper payload type LIST should have a single element
                            console.log("Mapper payload type LIST do not contain a single attribute!");
                        }

                    } else {
                        console.log("Unknown mapper attribute type detected!");
                    }
                }
            }

            var fillWith = {
                annotationType: {
                    name: type
                },
                annotationOptions: sinkOptionsArray,
                map: {
                    annotationType: {
                        name: mapperType
                    },
                    annotationOptions: mapperOptionsArray,
                    attributeOrPayloadValues: payloadOrAttributeObject
                }
            };
            fillWith = self.formUtils.cleanJSONObject(fillWith);

            // generate the form to define a sink
            var editor = new JSONEditor(formContainer.find('.define-sink')[0], {
                schema: sinkSchema,
                startval: fillWith,
                show_errors: "always",
                disable_properties: false,
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                display_required_only: true,
                no_additional_properties: true
            });
            formContainer.append(self.formUtils.buildFormButtons(true));

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#btn-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                var errors = editor.validate();
                if (errors.length) {
                    return;
                }

                // set the isDesignViewContentChanged to true
                self.configurationData.setIsDesignViewContentChanged(true);

                var config = editor.getValue();
                clickedElement.setType(config.annotationType.name);

                var annotationOptions = [];
                if (config.annotationOptions !== undefined) {
                    _.forEach(config.annotationOptions, function (option) {
                        annotationOptions.push(option.optionValue);
                    });
                    clickedElement.setOptions(annotationOptions);
                } else {
                    clickedElement.setOptions(undefined);
                }

                if (config.map !== undefined) {
                    var mapperOptions = {};
                    _.set(mapperOptions, 'type', config.map.annotationType.name);

                    var mapperAnnotationOptions = [];
                    if (config.map.annotationOptions !== undefined) {
                        _.forEach(config.map.annotationOptions, function (option) {
                            mapperAnnotationOptions.push(option.optionValue);
                        });
                        _.set(mapperOptions, 'options', mapperAnnotationOptions);
                    } else {
                        _.set(mapperOptions, 'options', undefined);
                    }

                    if (config.map.attributeOrPayloadValues !== undefined) {
                        // if attributeValues[0] or payloadMapValues[0] is defined then mapper annotations values are
                        // saved as a map. If payloadSingleValue is defined then the value is saved as a single value.
                        // Otherwise as a list.
                        var payloadOrAttributeOptions;
                        var payloadOrAttributeObject;
                        if (config.map.attributeOrPayloadValues.length !== undefined
                            && config.map.attributeOrPayloadValues[0].attributeMapKey !== undefined) {
                            var mapperAttributeValues = {};
                            _.forEach(config.map.attributeOrPayloadValues, function (attributeValue) {
                                mapperAttributeValues[attributeValue.attributeMapKey]
                                    = attributeValue.attributeMapValue;
                            });

                            payloadOrAttributeOptions = {};
                            _.set(payloadOrAttributeOptions, 'annotationType', 'ATTRIBUTES');
                            _.set(payloadOrAttributeOptions, 'type', 'MAP');
                            _.set(payloadOrAttributeOptions, 'value', mapperAttributeValues);
                            payloadOrAttributeObject = new PayloadOrAttribute(payloadOrAttributeOptions);

                        } else if (config.map.attributeOrPayloadValues.length !== undefined
                            && config.map.attributeOrPayloadValues[0].attributeListValue !== undefined) {
                            var mapperAttributeValuesArray = [];
                            _.forEach(config.map.attributeOrPayloadValues, function (attributeValue) {
                                mapperAttributeValuesArray.push(attributeValue.attributeListValue);
                            });

                            payloadOrAttributeOptions = {};
                            _.set(payloadOrAttributeOptions, 'annotationType', 'ATTRIBUTES');
                            _.set(payloadOrAttributeOptions, 'type', 'LIST');
                            _.set(payloadOrAttributeOptions, 'value', mapperAttributeValuesArray);
                            payloadOrAttributeObject = new PayloadOrAttribute(payloadOrAttributeOptions);

                        } else if (config.map.attributeOrPayloadValues.length !== undefined
                            && config.map.attributeOrPayloadValues[0].payloadMapKey !== undefined) {
                            var mapperPayloadValues = {};
                            _.forEach(config.map.attributeOrPayloadValues, function (payloadMapValue) {
                                mapperPayloadValues[payloadMapValue.payloadMapKey] = payloadMapValue.payloadMapValue;
                            });

                            payloadOrAttributeOptions = {};
                            _.set(payloadOrAttributeOptions, 'annotationType', 'PAYLOAD');
                            _.set(payloadOrAttributeOptions, 'type', 'MAP');
                            _.set(payloadOrAttributeOptions, 'value', mapperPayloadValues);
                            payloadOrAttributeObject = new PayloadOrAttribute(payloadOrAttributeOptions);

                        } else if (config.map.attributeOrPayloadValues.length === undefined
                            && config.map.attributeOrPayloadValues.singleValue !== undefined) {
                            var mapperPayloadValuesList = [config.map.attributeOrPayloadValues.singleValue];
                            payloadOrAttributeOptions = {};
                            _.set(payloadOrAttributeOptions, 'annotationType', 'PAYLOAD');
                            _.set(payloadOrAttributeOptions, 'type', 'LIST');
                            _.set(payloadOrAttributeOptions, 'value', mapperPayloadValuesList);
                            payloadOrAttributeObject = new PayloadOrAttribute(payloadOrAttributeOptions);

                        }
                        _.set(mapperOptions, 'payloadOrAttribute', payloadOrAttributeObject);
                    } else {
                        _.set(mapperOptions, 'payloadOrAttribute', undefined);
                    }

                    var mapperObject = new MapAnnotation(mapperOptions);
                    clickedElement.setMap(mapperObject);
                } else {
                    clickedElement.setMap(undefined);
                }

                var textNode = $('#' + id).find('.sinkNameNode');
                textNode.html(config.annotationType.name);

                // perform JSON validation
                JSONValidator.prototype.validateSourceOrSinkAnnotation(clickedElement, 'Sink');

                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

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

        return SinkForm;
    });
