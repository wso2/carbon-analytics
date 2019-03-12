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

define(['require', 'lodash', 'appData', 'log', 'constants', 'handlebar', 'annotationObject', 'annotationElement',
        'designViewUtils'],
    function (require, _, AppData, log, Constants, Handlebars, AnnotationObject, AnnotationElement, DesignViewUtils) {


        /**
         * @class FormUtils Contains utility methods for forms
         * @constructor
         * @param {Object} configurationData Siddhi app data
         * @param {Object} jsPlumbInstance JsPlumb instance of the current tab
         */
        var FormUtils = function (configurationData, jsPlumbInstance) {
            this.configurationData = configurationData;
            this.jsPlumbInstance = jsPlumbInstance;
        };

        /**
         * @function check whether given name to the definition element is used(This will only consider definitions
         * which creates internal streams in Siddhi for each of them. Function definitions are not considered.)
         * @param elementName given name to the definition element
         * @param skipElementID this element name will be ignored when checking the unique name. This is used when
         *          saving the same name after editing a particular element
         * @return {boolean}
         */
        FormUtils.prototype.isDefinitionElementNameUsed = function (elementName, skipElementID) {
            var self = this;
            var isNameUsed = false;
            var streamList = self.configurationData.getSiddhiAppConfig().getStreamList();
            var tableList = self.configurationData.getSiddhiAppConfig().getTableList();
            var windowList = self.configurationData.getSiddhiAppConfig().getWindowList();
            var aggregationList = self.configurationData.getSiddhiAppConfig().getAggregationList();
            var triggerList = self.configurationData.getSiddhiAppConfig().getTriggerList();
            var listNames = [streamList, tableList, windowList, aggregationList, triggerList];
            _.forEach(listNames, function (list) {
                _.forEach(list, function (element) {
                    if (element.getName() === elementName) {
                        if (!(skipElementID !== undefined && skipElementID === element.getId())) {
                            isNameUsed = true;
                        }
                    }
                });
            });

            return isNameUsed;
        };

        /**
         * @function check whether given name to the function definition element is used.
         * @param elementName given name to the definition element
         * @param skipElementID this element name will be ignored when checking the unique name. This is used when
         *          saving the same name after editing a particular element
         * @return {boolean}
         */
        FormUtils.prototype.isFunctionDefinitionElementNameUsed = function (elementName, skipElementID) {
            var self = this;
            var isNameUsed = false;
            var functionList = self.configurationData.getSiddhiAppConfig().getFunctionList();
            _.forEach(functionList, function (element) {
                if (element.getName() === elementName) {
                    if (!(skipElementID !== undefined && skipElementID === element.getId())) {
                        isNameUsed = true;
                    }
                }
            });

            return isNameUsed;
        };

        /**
         * @function check whether given name to the inner stream definition is used in the given partition.
         * @param partitionId id of the partition element
         * @param elementName given name to the definition element
         * @param skipElementID this element name will be ignored when checking the unique name. This is used when
         *          saving the same name after editing a particular element
         * @return {boolean}
         */
        FormUtils.prototype.isStreamDefinitionNameUsedInPartition = function (partitionId, elementName,
                                                                              skipElementID) {
            var self = this;
            var isNameUsed = false;
            var partition = self.configurationData.getSiddhiAppConfig().getPartition(partitionId);
            var streamList = partition.getStreamList();
            _.forEach(streamList, function (element) {
                if (element.getName() === elementName) {
                    if (!(skipElementID !== undefined && skipElementID === element.getId())) {
                        isNameUsed = true;
                    }
                }
            });

            return isNameUsed;
        };

        /**
         * @function check whether given query name is used in the query list
         * @param elementName given name to the definition element
         * @param skipElementID this element name will be ignored when checking the unique name. This is used when
         *          saving the same name after editing a particular element
         * @return {boolean}
         */
        FormUtils.prototype.isQueryDefinitionNameUsed = function (elementName, skipElementID) {
            var self = this;
            var isNameUsed = false;
            var joinQueryList = self.configurationData.getSiddhiAppConfig().getJoinQueryList();
            var sequenceQueryList = self.configurationData.getSiddhiAppConfig().getSequenceQueryList();
            var patternQueryList = self.configurationData.getSiddhiAppConfig().getPatternQueryList();
            var WindowFilterProjectionQueryList = self.configurationData.getSiddhiAppConfig()
                .getWindowFilterProjectionQueryList();
            var listNames = [joinQueryList, sequenceQueryList, patternQueryList, WindowFilterProjectionQueryList];
            _.forEach(listNames, function (list) {
                _.forEach(list, function (element) {
                    if (element.getQueryName() === elementName) {
                        if (!(skipElementID !== undefined && skipElementID === element.getId())) {
                            isNameUsed = true;
                        }
                    }
                });
            });

            return isNameUsed;
        };

        /**
         * @function This method removes undefined, null, empty arrays, empty object property fields from a JSON object
         * @param objectElement object which is needed to be cleaned
         * @return cleaned element
         */
        FormUtils.prototype.cleanJSONObject = function (objectElement) {
            var self = this;
            for (var propertyName in objectElement) {
                if (objectElement.hasOwnProperty(propertyName)
                    && (objectElement[propertyName] === null
                    || (!_.isNumber(objectElement[propertyName]) && !_.isBoolean(objectElement[propertyName])
                    && _.isEmpty(objectElement[propertyName]))
                    || !objectElement[propertyName])) {
                    delete objectElement[propertyName];
                } else if (objectElement.hasOwnProperty(propertyName)
                    && objectElement[propertyName] instanceof Object) {
                    self.cleanJSONObject(objectElement[propertyName]);
                    if (objectElement.hasOwnProperty(propertyName) && !_.isBoolean(objectElement[propertyName])
                        && _.isEmpty(objectElement[propertyName])) {
                        delete objectElement[propertyName];
                    }
                }
            }
            return objectElement;
        };

        /**
         * @function Updates connections of a definition element after the element name is changed.
         * @param elementId id of the element
         */
        FormUtils.prototype.updateConnectionsAfterDefinitionElementNameChange = function (elementId) {
            var self = this;

            var outConnections = self.jsPlumbInstance.getConnections({source: elementId + '-out'});
            var inConnections = self.jsPlumbInstance.getConnections({target: elementId + '-in'});

            _.forEach(outConnections, function (connection) {
                self.jsPlumbInstance.deleteConnection(connection);
            });
            _.forEach(inConnections, function (connection) {
                self.jsPlumbInstance.deleteConnection(connection);
            });

            _.forEach(inConnections, function (inConnection) {
                self.jsPlumbInstance.connect({
                    source: inConnection.sourceId,
                    target: inConnection.targetId
                });
            });

            _.forEach(outConnections, function (outConnection) {
                self.jsPlumbInstance.connect({
                    source: outConnection.sourceId,
                    target: outConnection.targetId
                });
            });
        };

        /**
         * @function Builds HTML for form buttons.
         * @param includeCancel boolean Show cancel button
         * @returns {string} HTML string
         */
        FormUtils.prototype.buildFormButtons = function () {
            var html = '<div class="query-form-actions">' +
                '<button type="button" id="btn-submit" class="btn btn-primary">Submit</button>' +
                '<button type="button" id="btn-cancel" class="btn btn-default">Cancel</button> </div>';
            return html;
        };

        /**
         * @function Builds HTML for atribute
         * @return {String} attributeHtml
         */
        FormUtils.prototype.addAttribute = function () {
            var attributeHtml = '<li class="attribute clearfix"><div class="clearfix"> ' +
                '<div class="attr-content">' +
                '<input type="text" value="" class="attr-name"/> ' +
                '<select class="attr-type">' +
                '<option value="string">string</option>' +
                '<option value="int">int</option>' +
                '<option value="long">long</option>' +
                '<option value="float">float</option>' +
                '<option value="double">double</option>' +
                '<option value="bool">bool</option>' +
                '<option value="object">object</option>' +
                '</select>' +
                '</div> <div class="attr-nav"> </div></div>' +
                '<label class="error-message"></label></li>';
            return attributeHtml;
        };

        /**
         * @function render the select box template
         * @param {String} id div id to embed the select box
         * @param {Object} predefinedTypes
         */
        FormUtils.prototype.renderTypeSelectionTemplate = function (id, predefinedTypes) {
            var selectionFormTemplate = Handlebars.compile($('#type-selection-form-template').html());
            var wrappedHtml = selectionFormTemplate({id: id, types: predefinedTypes});
            $('#define-' + id).html(wrappedHtml);
        };

        /**
         * @function render the attribute form template
         * @param {Object} attributes
         */
        FormUtils.prototype.renderAttributeTemplate = function (attributes) {
            var self = this;
            var attributeFormTemplate = Handlebars.compile($('#attribute-form-template').html());
            var wrappedHtml = attributeFormTemplate(attributes);
            $('#define-attribute').html(wrappedHtml);
            self.changeAttributeNavigation();
        };

        /**
         * @function renders the annotation form template
         * @param {String} id div to embed the template
         * @param {Object} annotations
         */
        FormUtils.prototype.renderAnnotationTemplate = function (id, annotations) {
            var self = this;
            var raw_partial = document.getElementById('recursiveAnnotationPartial').innerHTML;
            Handlebars.registerPartial('recursiveAnnotation', raw_partial);
            var annotationFormTemplate = Handlebars.compile($('#annotation-form-template').html());
            var wrappedHtml = annotationFormTemplate(annotations);
            $('#' + id).html(wrappedHtml);
            self.loadAnnotation();
        };

        /**
         * @function render the select box for predefined maps
         * @param {Object} predefinedMaps Predefined map annotations
         */
        FormUtils.prototype.renderMap = function (predefinedMaps) {
            if (!$.trim($('#define-map').html()).length) {
                var mapFormTemplate = Handlebars.compile($('#type-selection-form-template').html());
                var wrappedHtml = mapFormTemplate({id: "map", types: predefinedMaps});
                $('#define-map').html(wrappedHtml);
                $('#define-map #map-type').val('passThrough');
                $('#define-map #map-type option:contains("' + Constants.DEFAULT_MAPPER_TYPE + '")').text('passThrough (default)');
            }
        };

        /**
         * @functionrender the options for the selected type
         * @param {Object} optionsArray Saved options
         * @param {Object} customizedMapperOptions Options typed by the user which aren't one of the predefined option
         * @param {String} id div to embed the options
         */
        FormUtils.prototype.renderOptions = function (optionsArray, customizedOptions, id) {
            var self = this;
            optionsArray.sort(function (val1, val2) {
                if (val1.optional && !val2.optional) return 1;
                else if (!val1.optional && val2.optional) return -1;
                else return 0;
            });
            var optionsTemplate = Handlebars.compile($('#source-sink-store-options-template').html());
            var wrappedHtml = optionsTemplate({
                id: id,
                options: optionsArray,
                customizedOptions: customizedOptions
            });
            $('#' + id + '-options-div').html(wrappedHtml);
            self.changeCustomizedOptDiv(id);
            ;
        };

        /**
         * @function to select the attribute type from the select box
         * @param {Object} savedAttributes
         */
        FormUtils.prototype.selectTypesOfSavedAttributes = function (savedAttributes) {
            //to select the options(type) of the saved attributes
            var i = 0;
            $('.attribute .attr-content').each(function () {
                $(this).find('.attr-type option').filter(function () {
                    return ($(this).val() == (savedAttributes[i].getType()).toLowerCase());
                }).prop('selected', true);
                i++;
            });
        };

        /**
         * @function to get the parameters of a selected type
         * @param {String} selectedType
         * @param {object} predefinedTypes Predefined types
         * @return {object} parameters
         */
        FormUtils.prototype.getSelectedTypeParameters = function (selectedType, predefinedTypes) {
            var parameters = [];
            for (type of predefinedTypes) {
                if (type.name.toLowerCase() == selectedType.toLowerCase()) {
                    if (type.parameters) {
                        parameters = type.parameters;
                    }
                    break;
                }
            }
            return parameters;
        };

        /**
         * @function to obtain a particular option from predefined options
         * @param {String} optionName option which needs to be found
         * @param {Object} predefinedOptions set of predefined option
         * @return {Object} option
         */
        FormUtils.prototype.getObject = function (optionName, predefinedOptions) {
            var option;
            for (var predefinedOption of predefinedOptions) {
                if (predefinedOption.name.toLowerCase() == optionName.toLowerCase()) {
                    option = predefinedOption;
                    break;
                }
            }
            return option;
        };

        /**
         * @function to obtain customized options from the saved options
         * @param {Object} predefinedOptions Predefined options of a particular annotation type
         * @param {Object} savedOptions saved options
         * @return {Object} customizedOptions
         */
        FormUtils.prototype.getCustomizedOptions = function (predefinedOptions, savedOptions) {
            var customizedOptions = [];
            _.forEach(savedOptions, function (savedOption) {
                var foundSavedOption = false;
                var optionKey = savedOption.split('=')[0];
                var optionValue = savedOption.split('=')[1].trim();
                optionValue = optionValue.substring(1, optionValue.length - 1);
                for (var predefinedOption of predefinedOptions) {
                    if (predefinedOption.name.toLowerCase() == optionKey.toLowerCase().trim()) {
                        foundSavedOption = true;
                        break;
                    }
                }
                if (!foundSavedOption) {
                    customizedOptions.push({key: optionKey, value: optionValue});
                }
            });
            return customizedOptions;
        };

        /**
         * @function to obtain the connected stream's attributes
         * @param {Object} streamList List of all stream objects
         * @param {String} connectedElement connected element's name
         * @return {Object} streamAttributes
         */
        FormUtils.prototype.getConnectedStreamAttributes = function (streamList, connectedElement) {
            var streamAttributes = [];
            for (var stream of streamList) {
                if (stream.name == connectedElement) {
                    var attributeList = stream.getAttributeList();
                    _.forEach(attributeList, function (attribute) {
                        streamAttributes.push({key: attribute.getName(), value: ""});
                    })
                    break;
                }
            }
            return streamAttributes;
        };

        /**
         * @function to validate the predefined options
         * @param {Object} predefinedOptions
         * @param {String} id to identify the div in the html to traverse
         * @return {boolean} isError
         */
        FormUtils.prototype.validateOptions = function (predefinedOptions, id) {
            var self = this;
            var isError = false;
            $('#' + id + '-options .option').each(function () {
                var optionName = $(this).find('.option-name').text().trim();
                var optionValue = $(this).find('.option-value').val().trim();
                var predefinedOptionObject = self.getObject(optionName, predefinedOptions);
                if ($(this).find('.option-name').hasClass('mandatory-option')) {
                    if (!self.checkOptionValue(optionValue, predefinedOptionObject, this)) {
                        isError = true;
                        return false;
                    }
                } else {
                    if ($(this).find('.option-checkbox').is(":checked")) {
                        if (!self.checkOptionValue(optionValue, predefinedOptionObject, this)) {
                            isError = true;
                            return false;
                        }
                    }
                }
            });
            return isError;
        };

        /**
         * @function to validate the data type of a given value
         * @param {Objects} dataType possible data-types
         * @param {String} optionValue value which needs to be checked for
         * @return {boolean} invalidDataType
         */
        FormUtils.prototype.validateDataType = function (dataTypes, value) {
            var invalidDataType = false;
            var intLongRegexMatch = /^[-+]?\d+$/;
            var doubleFloatRegexMatch = /^[+-]?([0-9]*[.])?[0-9]+$/;
            var timeRegexMatch = /^[_A-z0-9]*((-|\s)*[_A-z0-9])*$/g;
            for (var dataType of dataTypes) {
                if (dataType === "INT" || dataType === "LONG") {
                    if (!value.match(intLongRegexMatch)) {
                        invalidDataType = true;
                    } else {
                        invalidDataType = false;
                        break;
                    }
                } else if (dataType === "DOUBLE" || dataType === "FLOAT") {
                    if (!value.match(doubleFloatRegexMatch)) {
                        invalidDataType = true;
                    } else {
                        invalidDataType = false;
                        break;
                    }
                } else if (dataType === "BOOL") {
                    if (!(value.toLowerCase() === "false" || value.toLowerCase() === "true")) {
                        invalidDataType = true;
                    } else {
                        invalidDataType = false;
                        break;
                    }
                } else if (dataType === "TIME") {
                    if (!value.match(timeRegexMatch)) {
                        invalidDataType = true;
                    } else {
                        invalidDataType = false;
                        break;
                    }
                }
            }
            return invalidDataType;
        };

        /**
         * @function to validate the customized options
         * @param {String} id to identify the div in the html to traverse
         * @return {boolean} isError
         */
        FormUtils.prototype.validateCustomizedOptions = function (id) {
            var self = this;
            var isError = false;
            if ($('#customized-' + id + '-options ul').has('li').length != 0) {
                $('#customized-' + id + '-options .option').each(function () {
                    var custOptName = $(this).find('.cust-option-key').val().trim();
                    var custOptValue = $(this).find('.cust-option-value').val().trim();
                    if ((custOptName != "") || (custOptValue != "")) {
                        if (custOptName == "") {
                            self.addErrorClass($(this).find('.cust-option-key'))
                            $(this).find('.error-message').text('Option key is required.');
                            isError = true;
                            return false;
                        } else if (custOptValue == "") {
                            $(this).find('.error-message').text('Option value is required.');
                            self.addErrorClass($(this).find('.cust-option-value'));
                            isError = true;
                            return false;
                        }
                    }
                });
            }
            return isError;
        };

        /**
         * @function to check if a particular value is valid
         * @param {String} optionValue value which needs to be validated
         * @param {Object} predefinedOptionObject predefined object of the option
         * @param {Object} parent div of the particular option
         */
        FormUtils.prototype.checkOptionValue = function (optionValue, predefinedOptionObject, parent) {
            var self = this;
            if (optionValue == "") {
                $(parent).find('.error-message').text('Option value is required.');
                self.addErrorClass($(parent).find('.option-value'));
                return false;
            } else {
                if (self.validateDataType(predefinedOptionObject.type, optionValue)) {
                    $(parent).find('.error-message').text('Invalid data-type. ' + predefinedOptionObject.type[0] +
                        ' required.');
                    self.addErrorClass($(parent).find('.option-value'));
                    return false;
                }
            }
            return true;
        };

        /**
         * @function validates the names
         * @param {Object} id to find the error-message label
         * @param {String} type Attribue or any element
         * @param {String} name the name to be validated
         * @return {boolean}
         */
        FormUtils.prototype.validateAttributeOrElementName = function (id, type, name) {
            var self = this;
            var errorMessageLabel;
            if (type === Constants.ATTRIBUTE) {
                errorMessageLabel = $(id).parents(".attribute").find(".error-message");
            } else {
                errorMessageLabel = $(id + 'ErrorMessage');
            }

            if (name.indexOf(' ') >= 0) {
                errorMessageLabel.text(self.capitalizeFirstLetter(type) + " name can not have white space.")
                self.addErrorClass(id);
                return true;
            }
            if (!Constants.ALPHABETIC_VALIDATOR_REGEX.test(name.charAt(0))) {
                errorMessageLabel.text
                (self.capitalizeFirstLetter(type) + " name must start with an alphabetical character.");
                self.addErrorClass(id);
                return true;
            }
            return false;
        };

        /**
         * @function validate the attributes
         * @param {Object} attributeNameList to add the valid attributes
         * @return {boolean} isErrorOccurred
         */
        FormUtils.prototype.validateAttributes = function (attributeNameList) {
            var self = this;
            var isErrorOccurred = false;
            $('.attr-name').each(function () {
                var attributeName = $(this).val().trim();
                if (attributeName != "") {
                    var isError = self.validateAttributeOrElementName(this, Constants.ATTRIBUTE, attributeName);
                    if (!isError) {
                        attributeNameList.push(attributeName)
                    } else {
                        isErrorOccurred = true;
                    }
                }
            });
            return isErrorOccurred;
        };

        /**
         * @function to build the options
         * @param {Object} predefinedOptions predefined options
         * @param {Object} selectedOptions array to add the built options
         * @param {String} id to identify the div in the html to traverse
         */
        FormUtils.prototype.buildOptions = function (selectedOptions, id) {
            var option;
            $('#' + id + '-options .option').each(function () {
                var optionName = $(this).find('.option-name').text().trim();
                var optionValue = $(this).find('.option-value').val().trim();
                if ($(this).find('.option-name').hasClass('mandatory-option')) {
                    option = optionName + " = \"" + optionValue + "\"";
                    selectedOptions.push(option);
                } else {
                    if ($(this).find('.option-checkbox').is(":checked")) {
                        option = optionName + " = \"" + optionValue + "\"";
                        selectedOptions.push(option);
                    }
                }
            });
        };

        /**
         * @function to build the customized options
         * @param {Object} selectedOptions array to add the built option
         * @param {String} id to identify the div in the html to traverse
         */
        FormUtils.prototype.buildCustomizedOption = function (selectedOptions, id) {
            var option = "";
            if ($('#customized-' + id + '-options ul').has('li').length != 0) {
                $('#customized-' + id + '-options .option').each(function () {
                    var custOptName = $(this).find('.cust-option-key').val().trim();
                    var custOptValue = $(this).find('.cust-option-value').val().trim();
                    if ((custOptName != "") && (custOptValue != "")) {
                        option = custOptName + " = \"" + custOptValue + "\"";
                        selectedOptions.push(option);
                    }
                });
            }
        };

        /**
         * @function to create the map section of the source/sink annotation
         * @param {Object} predefinedMaps predefined mappers
         */
        FormUtils.prototype.buildMapSection = function (predefinedMaps, mapperOptions) {
            var self = this;
            var customizedMapperOptions = [];
            self.renderMap(predefinedMaps);
            mapperOptions = self.getSelectedTypeParameters(Constants.DEFAULT_MAPPER_TYPE, predefinedMaps);
            mapperOptionsWithValues = self.createObjectWithValues(mapperOptions);
            self.renderOptions(mapperOptionsWithValues, customizedMapperOptions, Constants.MAPPER)
        };

        /**
         * @function to create option object with an additional empty value
         * @param {Object} objectArray Predefined objects without the 'value' attribute
         * @return {Object} objects
         */
        FormUtils.prototype.createObjectWithValues = function (objectArray) {
            var objects = [];
            _.forEach(objectArray, function (object) {
                objects.push({
                    key: object.name, value: "", description: object.description, optional: object.optional,
                    defaultValue: object.defaultValue
                });
            });
            return objects;
        };

        /**
         * @function to map the saved option values to the option object
         * @param {Object} predefinedOptions Predefined options of a particular annotation type
         * @param {Object} savedOptions Saved options
         * @return {Object} options
         */
        FormUtils.prototype.mapUserOptionValues = function (predefinedOptions, savedOptions) {
            var options = [];
            _.forEach(predefinedOptions, function (predefinedOption) {
                var foundPredefinedOption = false;
                for (var savedOption of savedOptions) {
                    var optionKey = savedOption.split('=')[0].trim();
                    var optionValue = savedOption.split('=')[1].trim();
                    optionValue = optionValue.substring(1, optionValue.length - 1);
                    if (optionKey.toLowerCase() == predefinedOption.name.toLowerCase()) {
                        foundPredefinedOption = true;
                        options.push({
                            key: predefinedOption.name, value: optionValue, description: predefinedOption
                                .description, optional: predefinedOption.optional,
                            defaultValue: predefinedOption.defaultValue
                        });
                        break;
                    }
                }
                if (!foundPredefinedOption) {
                    options.push({
                        key: predefinedOption.name,
                        value: "",
                        description: predefinedOption
                            .description,
                        optional: predefinedOption.optional,
                        defaultValue: predefinedOption.defaultValue
                    });
                }
            });
            return options;
        };

        /**
         * @function checks if an annotation is predefined using the annotation name
         * @param {Object} predefinedAnnotationList list of predefined annotations
         * @param {String} annotationName the name which needs to be checked
         * @return {Object} predefinedObject
         */
        FormUtils.prototype.isPredefinedAnnotation = function (predefinedAnnotationList, annotationName) {
            var predefinedObject;
            _.forEach(predefinedAnnotationList, function (predefinedAnnotation) {
                if (predefinedAnnotation.name.toLowerCase() == annotationName.toLowerCase()) {
                    predefinedObject = predefinedAnnotation;
                    return;
                }
            });
            return predefinedObject;
        };

        /**
         * @function validate the annotations
         * @param {Object} predefinedAnnotationList List of predefined annotations
         * @param {Object} annotationNodes array to add the nodes which needs to be built
         * @return {boolean} isErrorOccurred
         */
        FormUtils.prototype.validateAnnotations = function (predefinedAnnotationList, annotationNodes) {
            var self = this;
            //gets all the parent nodes
            var jsTreeAnnotationList = $('#annotation-div').jstree(true)._model.data['#'].children;
            var isErrorOccurred = false;
            for (var jsTreeAnnotation of jsTreeAnnotationList) {
                var node_info = $('#annotation-div').jstree("get_node", jsTreeAnnotation);
                var predefinedObject = self.isPredefinedAnnotation(predefinedAnnotationList, node_info.text.trim())
                if (predefinedObject) {
                    if ((predefinedObject.isMandatory) || (!predefinedObject.isMandatory && node_info.state.checked)) {
                        if (self.validatePredefinedAnnotation(node_info, predefinedObject)) {
                            isErrorOccurred = true;
                            break;
                        } else {
                            annotationNodes.push(jsTreeAnnotation)
                        }
                    }
                } else {
                    annotationNodes.push(jsTreeAnnotation)
                }
            }
            return isErrorOccurred;
        };

        /**
         * @function validates elements of jstree predefined Annotations
         * @param {Object} node_info jstree node info
         * @param {Object} predefinedAnnotationObject predefined annotation
         * @return {boolean} isErrorOccurred
         */
        FormUtils.prototype.validatePredefinedAnnotation = function (node_info, predefinedAnnotationObject) {
            var self = this;
            var isErrorOccurred = false;
            var childrenOFPredefinedAnnotationNode = node_info.children;
            for (var jsTreePredefinedAnnotationElement of childrenOFPredefinedAnnotationNode) {
                var annotation_key_info = $('#annotation-div').jstree("get_node",
                    jsTreePredefinedAnnotationElement);
                var annotation_value_info = $('#annotation-div').jstree("get_node", annotation_key_info
                    .children[0])
                //validate for checked(optional)properties which has empty values
                if (annotation_key_info.state.checked && annotation_value_info.text.trim() == "") {
                    DesignViewUtils.prototype.errorAlert("Property '" + annotation_key_info.text.trim() +
                        "' is empty");
                    isErrorOccurred = true;
                    break;
                }
                if (self.validateMandatoryElementsOfPredefinedObjects(annotation_key_info,
                        annotation_value_info, predefinedAnnotationObject)) {
                    isErrorOccurred = true;
                    break;
                }
            }
            return isErrorOccurred;
        };

        /**
         * @function validate the elements of the annotation
         * @param {Object} annotationKey jstree annotation key
         * @param {Object} annotationValue jstree annotation key
         * @param {Object} predefinedAnnotationObject predefined annotation object
         * @return {Boolean} isErrorOccurred
         */
        FormUtils.prototype.validateMandatoryElementsOfPredefinedObjects = function (annotationKey, annotationValue,
                                                                                     predefinedAnnotationObject) {
            var isErrorOccurred = false;
            for (var predefinedObjectElement of predefinedAnnotationObject.elements) {
                if (annotationKey.text.trim().toLowerCase() == predefinedObjectElement.key
                        .toLowerCase()) {
                    if (predefinedObjectElement.isMandatory) {
                        if (annotationValue.text.trim() == "") {
                            DesignViewUtils.prototype.errorAlert("Property '" + predefinedObjectElement
                                    .key + "' is mandatory");
                            isErrorOccurred = true;
                            break;
                        }
                    }
                }
            }
            return isErrorOccurred
        };

        /**
         * @function makes the predefined annotation's checkbox checked
         * @param {Object} checkedBoxes array of saved annotation names
         */
        FormUtils.prototype.checkPredefinedAnnotations = function (checkedBoxes) {
            var jsTreeNodes = $('#annotation-div').jstree(true).get_json('#', {'flat': true});
            for (var checkedBoxName of checkedBoxes) {
                for (var node of jsTreeNodes) {
                    if (node.text.trim().toLowerCase() == checkedBoxName.toLowerCase()) {
                        $("#annotation-div").jstree(true).check_node(node.id)
                        break;
                    }
                }
            }
        };

        /**
         * @function to build the annotations
         * @param {Object} annotationNodes array of nodes which needs to be constructed
         * @param {Object} annotationStringList array to add the built annotation strings
         * @param {Object} annotationObjectList array to add the created annotation objects
         */
        var annotation = "";
        FormUtils.prototype.buildAnnotation = function (annotationNodes, annotationStringList, annotationObjectList) {
            var self = this;
            _.forEach(annotationNodes, function (node) {
                var node_info = $('#annotation-div').jstree("get_node", node);
                var childArray = node_info.children
                if (childArray.length != 0) {
                    annotation += "@" + node_info.text.trim() + "( "
                    //create annotation object
                    var annotationObject = new AnnotationObject();
                    annotationObject.setName(node_info.text.trim())
                    self.traverseChildAnnotations(childArray, annotationObject)
                    annotation = annotation.substring(0, annotation.length - 1);
                    annotation += ")"
                    annotationObjectList.push(annotationObject)
                    annotationStringList.push(annotation);
                    annotation = "";
                }
            });
        };

        /**
         * @function to traverse the children of the parent annotaions
         * @param {Object} children the children of a parent annotation node
         * @param {Object} annotationObject the parent's annotation object
         */
        FormUtils.prototype.traverseChildAnnotations = function (children, annotationObject) {
            var self = this;
            children.forEach(function (node) {
                node_info = $('#annotation-div').jstree("get_node", node);
                //if the child is a sub annotation
                if (self.isChildSubAnnotation(node_info)) {
                    if (node_info.children.length != 0) {
                        annotation += "@" + node_info.text.trim() + "( "
                        var childAnnotation = new AnnotationObject();
                        childAnnotation.setName(node_info.text.trim())
                        self.traverseChildAnnotations(node_info.children, childAnnotation)
                        annotationObject.addAnnotation(childAnnotation)
                        annotation = annotation.substring(0, annotation.length - 1);
                        annotation += "),"
                    }
                } else {
                    self.addAnnotationElement(node_info, annotationObject);
                }
            });
        };

        /**
         * @function to add the annotation's element
         * @param {Object} node_info jstree node information
         * @param {AnnotationObject} annotationObject
         */
        FormUtils.prototype.addAnnotationElement = function (node_info, annotationObject) {
            if (node_info.li_attr.class != undefined && (node_info.li_attr.class == "optional-key")
                && node_info.state.checked == false) {
                //not to add the child property if it hasn't been checked(predefined optional-key only)
            } else {
                annotation += node_info.text.trim() + "="
                var node_value = $('#annotation-div').jstree("get_node", node_info.children[0]).text.trim();
                annotation += "'" + node_value + "' ,";
                var element = new AnnotationElement(node_info.text.trim(), node_value)
                annotationObject.addElement(element);
            }
        };

        /**
         * @function to determine if annotation's child is a sub-annotation
         * @param {Object} node_info jstree node information
         * @return {Boolean}
         */
        FormUtils.prototype.isChildSubAnnotation = function (node_info) {
            if ((node_info.original != undefined && node_info.original.class == "annotation") ||
                (node_info.li_attr != undefined && (node_info.li_attr.class == "annotation" ||
                node_info.li_attr.class == "optional-annotation" || node_info.li_attr.class ==
                "mandatory-annotation"))) {
                return true;
            } else {
                return false;
            }
        };

        /**
         * @function to initialize the jstree for annotations
         */
        FormUtils.prototype.loadAnnotation = function () {
            var self = this;
            //initialise jstree
            $("#annotation-div").jstree({
                "core": {
                    "check_callback": true
                },
                "themes": {
                    "theme": "default",
                    "url": "editor/commons/lib/js-tree-v3.3.2/themes/default/style.css"
                },
                "checkbox": {
                    "three_state": false,
                    "whole_node": false,
                    "tie_selection": false
                },
                "plugins": ["themes", "checkbox"]
            });

            var tree = $('#annotation-div').jstree(true);
            self.addEventListenersForJstree(tree);
        };

        /**
         * @function to add event listeners for jstree annotations
         */
        FormUtils.prototype.addEventListenersForJstree = function (tree) {
            //to add key-value for annotation node
            $("#btn-add-key-val").on("click", function () {
                var selectedNode = $("#annotation-div").jstree("get_selected");
                tree.create_node(selectedNode,
                    {
                        text: "property", class: "annotation-key", state: {"opened": true},
                        "a_attr": {"class": "annotation-key"}, icon: "/editor/commons/images/properties.png",
                        children: [{
                            text: "value", class: "annotation-value", "a_attr": {"class": "annotation-value"},
                            icon: "/editor/commons/images/value.png"
                        }]
                    }
                );
                tree.open_node(selectedNode);
                tree.deselect_all();
            });

            //to add annotation node
            $("#btn-add-annotation").on("click", function () {
                var selectedNode = $("#annotation-div").jstree("get_selected");
                if (selectedNode == "") {
                    selectedNode = "#"
                }
                tree.create_node(selectedNode, {
                    text: "Annotation", class: "annotation", state: {"opened": true},
                    "a_attr": {"class": "annotation"}, icon: "/editor/commons/images/annotation.png",
                    children: [{
                        text: "property", class: "annotation-key", icon: "/editor/commons/images/properties.png",
                        "a_attr": {"class": "annotation-key"},
                        children: [{
                            text: "value", class: "annotation-value", "a_attr": {"class": "annotation-value"},
                            icon: "/editor/commons/images/value.png"
                        }]
                    }]

                });
                tree.open_node(selectedNode);
                tree.deselect_all();
            });

            //to delete an annotation or a key-value node
            $("#btn-del-annotation").on("click", function () {
                var selectedNode = $("#annotation-div").jstree("get_selected");
                tree.delete_node([selectedNode]);
                tree.deselect_all();
            })

            //to edit the selected node
            //to hide/show the buttons corresponding to the node selected
            $('#annotation-div').on("select_node.jstree", function (e, data) {
                var node_info = $('#annotation-div').jstree("get_node", data.node)
                if ((node_info.original != undefined && (node_info.original.class == "annotation")) ||
                    (node_info.li_attr != undefined && (node_info.li_attr.class == "annotation"))) {
                    tree.edit(data.node)
                    $("#btn-del-annotation").show();
                    $("#btn-add-annotation").show();
                    $("#btn-add-key-val").show();

                } else if ((node_info.original != undefined && (node_info.original.class == "annotation-key")) ||
                    (node_info.li_attr != undefined && (node_info.li_attr.class == "annotation-key"))) {
                    tree.edit(data.node);
                    $("#btn-del-annotation").show();
                    $("#btn-add-annotation").hide();
                    $("#btn-add-key-val").hide();

                } else if ((node_info.original != undefined && (node_info.original.class == "annotation-value")) ||
                    (node_info.li_attr != undefined && (node_info.li_attr.class == "annotation-value"))) {
                    $("#btn-del-annotation").hide();
                    $("#btn-add-annotation").hide();
                    $("#btn-add-key-val").hide();
                    tree.edit(data.node);
                }
            });

            //to unselect the nodes when user clicks other than the nodes in jstree
            $(document).on('click', function (e) {
                if ($(e.target).closest('.jstree').length) {
                    $("#btn-del-annotation").hide();
                    $("#btn-add-annotation").show();
                    $("#btn-add-key-val").hide();
                    tree.deselect_all();
                }
            });
        };

        /**
         * @function to add event listeners for attribute section
         */
        FormUtils.prototype.addEventListenersForAttributeDiv = function () {
            var self = this;
            //To add attribute
            $("#define-attribute").on('click', '#btn-add-attribute', function () {
                $("#attribute-div").append(self.addAttribute());
                self.changeAttributeNavigation();
            });

            //To delete attribute
            $("#define-attribute").on('click', '#attribute-div .btn-del-attr', function () {
                $(this).closest('li').remove();
                self.changeAttributeNavigation();
            });

            //To reorder up the attribute
            $("#define-attribute").on('click', ' #attribute-div .reorder-up', function () {
                var $current = $(this).closest('li');
                var $previous = $current.prev('li');
                if ($previous.length !== 0) {
                    $current.insertBefore($previous);
                }
                self.changeAttributeNavigation();

            });

            //To reorder down the attribute
            $("#define-attribute").on('click', ' #attribute-div .reorder-down', function () {
                var $current = $(this).closest('li');
                var $next = $current.next('li');
                if ($next.length !== 0) {
                    $current.insertAfter($next);
                }
                self.changeAttributeNavigation();
            });
        };

        /**
         * @function to add event listeners of the options of source/sink/store
         */
        FormUtils.prototype.addEventListenersForOptionsDiv = function (id) {
            var self = this;
            //To show option description
            $('#' + id + '-options-div').on('mouseover', '.option-desc', function () {
                $(this).find('.option-desc-content').show();
            });

            //To hide option description
            $('#' + id + '-options-div').on('mouseout', '.option-desc', function () {
                $(this).find('.option-desc-content').hide();
            });

            //To hide and show the option content of the optional options
            $('#' + id + '-options-div').on('change', '.option-checkbox', function () {
                var optionParent = $(this).parents(".option");
                if ($(this).is(':checked')) {
                    optionParent.find(".option-value").show();
                } else {
                    optionParent.find(".option-value").hide();
                    optionParent.find(".option-value").removeClass("required-input-field");
                    optionParent.find(".error-message").text("");
                }
            });

            //To add customized option
            $('#' + id + '-options-div').on('click', '#btn-add-' + id + '-options', function () {
                var custOptDiv = '<li class="option">' +
                    '<div class = "clearfix"> <label>option.key</label> <input type="text" class="cust-option-key"' +
                    'value=""> </div> <div class="clearfix"> <label>option.value</label> ' +
                    '<input type="text" class="cust-option-value" value="">' +
                    '<a class = "btn-del btn-del-option"><i class="fw fw-delete"></i></a></div>' +
                    '<label class = "error-message"></label></li>';
                $('#customized-' + id + '-options .cust-options').append(custOptDiv);
                self.changeCustomizedOptDiv(id);
            });

            //To delete customized option
            $('#' + id + '-options-div').on('click', '.btn-del-option', function () {
                $(this).closest('li').remove();
                self.changeCustomizedOptDiv(id);
            });
        };

        /**
         * @function to change the heading and the button text of the customized options div
         */
        FormUtils.prototype.changeCustomizedOptDiv = function (id) {
            var customizedOptionList = $('#customized-' + id + '-options').find('.cust-options li');
            var parent = $('#customized-' + id + '-options');
            if (customizedOptionList.length > 0) {
                parent.find('h3').show();
                parent.find('.btn-add-options').html('Add more');
            } else {
                parent.find('h3').hide();
                parent.find('.btn-add-options').html('Add customized option');
            }
        };

        /**
         * @function manages the attribute navigations
         */
        FormUtils.prototype.changeAttributeNavigation = function () {
            $('.attr-nav').empty();
            var attrLength = $('#attribute-div li').length;
            if (attrLength == 1) {
                $('.attribute:eq(0)').find('.attr-nav').empty();
            }
            if (attrLength == 2) {
                $('.attribute:eq(0)').find('.attr-nav').append('<a class = "reorder-down"><i class="fw fw-sort-down">' +
                    '</i></a><a class = "btn-del-attr"><i class="fw fw-delete"></i></a>');
                $('.attribute:eq(1)').find('.attr-nav').append('<a class="reorder-up"> <i class="fw fw-sort-up "></i>' +
                    '</a><a class = "btn-del-attr"><i class="fw fw-delete"></i></a>');
            }
            if (attrLength > 2) {
                var lastIndex = attrLength - 1;
                for (var i = 0; i < attrLength; i++) {
                    $('.attribute:eq(' + i + ')').find('.attr-nav').append('<a class="reorder-up"> ' +
                        '<i class="fw fw-sort-up"></i></a>' +
                        '<a class = "reorder-down"><i class="fw fw-sort-down"> </i></a>' +
                        '<a class = "btn-del-attr"><i class="fw fw-delete"></i></a>');
                }
                $('.attribute:eq(0)').find('.attr-nav a:eq(0)').remove();
                $('.attribute:eq(' + lastIndex + ')').find('.attr-nav a:eq(1)').remove();
            }
        };

        /**
         * @function to pop up the element which is being currently edited
         */
        FormUtils.prototype.popUpSelectedElement = function (id) {
            $('#' + id).addClass('selected-element');
            $(".overlayed-container").fadeTo(200, 1);
        };

        /**
         * @function to add the error class
         * @param {Object} id object where the error needs to be added
         */
        FormUtils.prototype.addErrorClass = function (id) {
            $(id)[0].scrollIntoView();
            $(id).addClass('required-input-field')
        };

        /**
         * @function to capitalize the first letter
         */
        FormUtils.prototype.capitalizeFirstLetter = function (text) {
            return text[0].toUpperCase() + text.slice(1);
        };

        /**
         * @function generate tooltip for siddhi app elements
         * @param element JSON object of the element
         * @param type type of the element
         * @returns {string} tooltip
         */
        FormUtils.prototype.getTooltip = function (element, type) {
            var appData = new AppData();

            switch (type) {
                case Constants.AGGREGATION:
                    appData.addAggregation(element);
                    break;

                case Constants.FUNCTION:
                    appData.addFunction(element);
                    break;

                case Constants.JOIN_QUERY:
                    appData.addJoinQuery(element);
                    break;

                case Constants.PARTITION:
                    appData.addPartition(element);
                    break;

                case Constants.PATTERN_QUERY:
                    appData.addPatternQuery(element);
                    break;

                case Constants.SEQUENCE_QUERY:
                    appData.addSequenceQuery(element);
                    break;

                case Constants.SINK:
                    appData.addSink(element);
                    break;

                case Constants.SOURCE:
                    appData.addSource(element);
                    break;

                case Constants.STREAM:
                    appData.addStream(element);
                    break;

                case Constants.TABLE:
                    appData.addTable(element);
                    break;

                case Constants.TRIGGER:
                    appData.addTrigger(element);
                    break;

                case Constants.WINDOW:
                    appData.addWindow(element);
                    break;

                case Constants.WINDOW_FILTER_PROJECTION_QUERY:
                    appData.addWindowFilterProjectionQuery(element);
                    break;
            }
            ;

            var self = this;
            var result = '';
            self.tooltipsURL = window.location.protocol + "//" + window.location.host + "/editor/tooltips";
            $.ajax({
                type: "POST",
                url: self.tooltipsURL,
                data: window.btoa(JSON.stringify(appData)),
                async: false,
                success: function (response) {
                    var toolTipObject = _.find(response, function (toolTip) {
                        return toolTip.id === element.getId();
                    });
                    if (toolTipObject !== undefined) {
                        result = toolTipObject.text;
                    }
                },
                error: function (error) {
                    if (error.responseText) {
                        log.error(error.responseText);
                    } else {
                        log.error("Error occurred while processing the request");
                    }
                }
            });
            return result;
        };

        /** Register classes for Handlebars */

        /** Generates the current index of the option being rendered */
        Handlebars.registerHelper('sum', function () {
            return Array.prototype.slice.call(arguments, 0, -1).reduce((acc, num) = > acc += num
            )
            ;
        });

        /** Handlebar helper to check if the index is equivalent to half the length of the option's array */
        Handlebars.registerHelper('isDivisor', function (index, options) {
            var divLength = Math.ceil(options.length / 2);
            return index === divLength;
        });

        /** Handlebar helper to render heading for the form */
        Handlebars.registerHelper('addTitle', function (id) {
            return id.charAt(0).toUpperCase() + id.slice(1);
        });

        /** Handlebar helper to compare if the id is "source" or "sink" */
        Handlebars.registerHelper('ifSourceOrSink', function (id, div) {
            if (id === "source" || id === "sink") {
                return div.fn(this);
            }
            return div.inverse(this);
        });

        /** Handlebar helper to compare if the id is "source" or "sink" or "store" */
        Handlebars.registerHelper('ifSourceOrSinkOrStore', function (id, div) {
            if (id === "source" || id === "sink" || id === "store") {
                return div.fn(this);
            }
            return div.inverse(this);
        });

        /** Handlebar helper to check id is equivalent to a given string */
        Handlebars.registerHelper('ifId', function (id, name, div) {
            if (id === name) {
                return div.fn(this);
            }
            return div.inverse(this);
        });
        /** End of register classes */

        return FormUtils;
    });


