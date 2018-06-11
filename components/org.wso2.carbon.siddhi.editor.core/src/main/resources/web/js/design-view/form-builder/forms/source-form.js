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

define(['require', 'log', 'jquery', 'lodash', 'sourceOrSinkAnnotation', 'mapAnnotation'],
    function (require, log, $, _, SourceOrSinkAnnotation, MapAnnotation) {

        /**
         * @class SourceForm Creates a forms to collect data from a source
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var SourceForm = function (options) {
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
        SourceForm.prototype.generateDefineForm = function (i, formConsole, formContainer) {
            var self = this;
            var propertyDiv = $('<div id="property-header"><h3>Define Source </h3></div>' +
                '<div id="define-source" class="define-source"></div>');
            formContainer.append(propertyDiv);

            // generate the form to define a source
            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Source",
                    properties: {
                        annotationType: {
                            required: true,
                            propertyOrder: 1,
                            type: "object",
                            title: "Annotation Type",
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
                            title: "Source Options",
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
                                    title: "Annotation Type",
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
                                    title: "Mapper Options",
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
                                attributeValues: {
                                    propertyOrder: 3,
                                    title: "Attributes Type",
                                    oneOf: [
                                        {
                                            $ref: "#/definitions/mapValues",
                                            title: "Enter Attributes as key/value pairs"
                                        },
                                        {
                                            $ref: "#/definitions/listValues",
                                            title: "Enter Attributes as a list"
                                        }
                                    ]
                                }
                            }
                        }
                    },
                    definitions: {
                        mapValues: {
                            required: true,
                            type: "array",
                            format: "table",
                            title: "Map Options",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title: 'Attribute',
                                properties: {
                                    key: {
                                        title: 'Key',
                                        type: "string",
                                        required: true,
                                        minLength: 1
                                    },
                                    value: {
                                        title: 'Value',
                                        type: "string",
                                        required: true,
                                        minLength: 1
                                    }
                                }
                            }
                        },
                        listValues: {
                            required: true,
                            type: "array",
                            format: "table",
                            title: "Map Options",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title: 'Attribute',
                                properties: {
                                    value: {
                                        title: 'Value',
                                        type: "string",
                                        required: true,
                                        minLength: 1
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

            formContainer.append(self.formUtils.buildFormButtons());

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#btn-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                var errors = editor.validate();
                if(errors.length) {
                    return;
                }
                // add the new out source to the source array
                var sourceOptions = {};
                _.set(sourceOptions, 'id', i);
                _.set(sourceOptions, 'annotationType', 'SOURCE');
                _.set(sourceOptions, 'type', editor.getValue().annotationType.name);

                var annotationOptions = [];
                if(editor.getValue().annotationOptions !== undefined) {
                    _.forEach(editor.getValue().annotationOptions, function (option) {
                        annotationOptions.push(option.optionValue);
                    });
                    _.set(sourceOptions, 'options', annotationOptions);
                } else {
                    _.set(sourceOptions, 'options', undefined);
                }

                if (editor.getValue().map !== undefined) {

                    var mapperOptions = {};
                    _.set(mapperOptions, 'type', editor.getValue().map.annotationType.name);

                    var mapperAnnotationOptions = [];
                    if(editor.getValue().map.annotationOptions !== undefined) {
                        _.forEach(editor.getValue().map.annotationOptions, function (option) {
                            mapperAnnotationOptions.push(option.optionValue);
                        });
                        _.set(mapperOptions, 'options', mapperAnnotationOptions);
                    } else {
                        _.set(mapperOptions, 'options', undefined);
                    }

                    var mapperAttributeValues = {};
                    if (editor.getValue().map.attributeValues !== undefined) {
                        // if key is defined then mapper annotations values are saved as a map. Otherwise as a list.
                        if (editor.getValue().map.attributeValues[0].key !== undefined) {
                            _.forEach(editor.getValue().map.attributeValues, function (attributeValue) {
                                mapperAttributeValues[attributeValue.key] = attributeValue.value;
                            });
                            var attributes = {
                                type: "MAP",
                                value: mapperAttributeValues
                            };
                            _.set(mapperOptions, 'attributes', attributes);
                        } else {
                            var mapperAttributeValuesArray = [];
                            _.forEach(editor.getValue().map.attributeValues, function (attributeValue) {
                                mapperAttributeValuesArray.push(attributeValue.value);
                            });
                            var attributes = {
                                type: "LIST",
                                value: mapperAttributeValuesArray
                            };
                            _.set(mapperOptions, 'attributes', attributes);
                        }
                        var mapperObject = new MapAnnotation(mapperOptions);
                        _.set(sourceOptions, 'map', mapperObject);
                    } else {
                        _.set(mapperOptions, 'attributes', undefined);
                    }
                } else {
                    _.set(sourceOptions, 'map', undefined);
                }

                var source = new SourceOrSinkAnnotation(sourceOptions);
                self.configurationData.getSiddhiAppConfig().addSource(source);

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);

                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

            });
        };

        /**
         * @function generate properties form for a source
         * @param element selected element(source)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        SourceForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var id = $(element).parent().attr('id');
            // retrieve the source information from the collection
            var clickedElement = self.configurationData.getSiddhiAppConfig().getSource(id);
            if(!clickedElement) {
                var errorMessage = 'unable to find clicked element';
                log.error(errorMessage);
                throw errorMessage;
            }
            var type = clickedElement.getType();
            var savedSourceOptions = clickedElement.getOptions();
            var map = clickedElement.getMap();

            var sourceOptionsArray = [];
            if (savedSourceOptions !== undefined) {
                _.forEach(savedSourceOptions, function (option) {
                    sourceOptionsArray.push({optionValue: option})
                });
            }

            var mapperType;
            var mapperOptionsArray = [];
            var mapperAttributesArray = [];
            if (map !== undefined) {
                mapperType = map.getType();

                var savedMapperOptions = map.getOptions();
                if (savedMapperOptions !== undefined) {
                    _.forEach(savedMapperOptions, function (option) {
                        mapperOptionsArray.push({optionValue: option})
                    });
                }

                var savedMapperAttributes = map.getAttributes();
                if (savedMapperAttributes !== undefined) {
                    if (savedMapperAttributes.type === 'MAP') {
                        for (var key in savedMapperAttributes.value) {
                            if (savedMapperAttributes.value.hasOwnProperty(key)) {
                                mapperAttributesArray.push({
                                    key: key,
                                    value: savedMapperAttributes.value[key]
                                });
                            }
                        }
                    } else if (savedMapperAttributes.type === 'LIST') {
                        _.forEach(savedMapperAttributes.value, function (attribute) {
                            mapperAttributesArray.push({value: attribute})
                        });
                    } else {
                        console.log("Unknown mapper attribute type detected!")
                    }
                }
            }

            var fillWith = {
                annotationType: {
                    name: type
                },
                annotationOptions : sourceOptionsArray,
                map: {
                    annotationType: {
                        name: mapperType
                    },
                    annotationOptions : mapperOptionsArray,
                    attributeValues: mapperAttributesArray
                }
            };
            fillWith = self.formUtils.cleanJSONObject(fillWith);

            // generate the form to define a source
            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Source",
                    properties: {
                        annotationType: {
                            required: true,
                            propertyOrder: 1,
                            type: "object",
                            title: "Annotation Type",
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
                            title: "Source Options",
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
                                    title: "Annotation Type",
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
                                    title: "Mapper Options",
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
                                attributeValues: {
                                    propertyOrder: 3,
                                    title: "Attributes Type",
                                    oneOf: [
                                        {
                                            $ref: "#/definitions/mapValues",
                                            title: "Enter Attributes as key/value pairs"
                                        },
                                        {
                                            $ref: "#/definitions/listValues",
                                            title: "Enter Attributes as a list"
                                        }
                                    ]
                                }
                            }
                        }
                    },
                    definitions: {
                        mapValues: {
                            required: true,
                            type: "array",
                            format: "table",
                            title: "Map Options",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title: 'Attribute',
                                properties: {
                                    key: {
                                        title: 'Key',
                                        type: "string",
                                        required: true,
                                        minLength: 1
                                    },
                                    value: {
                                        title: 'Value',
                                        type: "string",
                                        required: true,
                                        minLength: 1
                                    }
                                }
                            }
                        },
                        listValues: {
                            required: true,
                            type: "array",
                            format: "table",
                            title: "Map Options",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title: 'Attribute',
                                properties: {
                                    value: {
                                        title: 'Value',
                                        type: "string",
                                        required: true,
                                        minLength: 1
                                    }
                                }
                            }
                        }
                    }
                },
                startval: fillWith,
                show_errors: "always",
                disable_properties: false,
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                display_required_only: true,
                no_additional_properties: true
            });
            formContainer.append(self.formUtils.buildFormButtons(true))

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#btn-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                var errors = editor.validate();
                if(errors.length) {
                    return;
                }
                
                var config = editor.getValue();
                clickedElement.setType(config.annotationType.name);

                var annotationOptions = [];
                if(config.annotationOptions !== undefined) {
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
                    if(config.map.annotationOptions !== undefined) {
                        _.forEach(config.map.annotationOptions, function (option) {
                            mapperAnnotationOptions.push(option.optionValue);
                        });
                        _.set(mapperOptions, 'options', mapperAnnotationOptions);
                    } else {
                        _.set(mapperOptions, 'options', undefined);
                    }
                    if (config.map.attributeValues !== undefined) {
                        // if key is defined then mapper annotations values are saved as a map. Otherwise as a list.
                        if (config.map.attributeValues[0].key !== undefined) {
                            var mapperAttributesValues = {};
                            _.forEach(config.map.attributeValues, function (attributeValue) {
                                mapperAttributesValues[attributeValue.key] = attributeValue.value;
                            });
                            var attributes = {
                                type: "MAP",
                                value: mapperAttributesValues
                            };
                            _.set(mapperOptions, 'attributes', attributes);
                        } else {
                            var mapperAttributesValueArray = [];
                            _.forEach(config.map.attributeValues, function (annotationValue) {
                                mapperAttributesValueArray.push(annotationValue.value);
                            });
                            var attributes = {
                                type: "LIST",
                                value: mapperAttributesValueArray
                            };
                            _.set(mapperOptions, 'attributes', attributes);
                        }
                    } else {
                        _.set(mapperOptions, 'attributes', undefined);
                    }

                    var mapperObject = new MapAnnotation(mapperOptions);
                    clickedElement.setMap(mapperObject);
                } else {
                    clickedElement.setMap(undefined);
                }

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

        return SourceForm;
    });
