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

define(['require', 'log', 'jquery', 'lodash', 'attribute', 'handlebar', 'constants'],
    function (require, log, $, _, Attribute, Handlebars, Constants) {

        /**
         * @class WindowForm Creates a forms to collect data from a window
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var WindowForm = function (options) {
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
         * @function to render the parameter for the selected window function using handlebars
         * @param {Object} parameterArray Saved parameters
         * @param {Object} windowType selected window processor type
         * @param {String} id Id for the div to embed the parameters
         */
        var renderParameters = function (parameterArray, windowType, id) {
            parameterArray.sort(function (val1, val2) {
                if (val1.optional && !val2.optional) return 1;
                else if (!val1.optional && val2.optional) return -1;
                else return 0;
            });
            var parameterTemplate = Handlebars.compile($('#window-function-parameters-template').html());
            var wrappedHtml = parameterTemplate({
                id: id,
                windowFunctionName: windowType,
                parameters: parameterArray
            });
            $('#defineFunctionParameters').html(wrappedHtml);
        };

        /**
         * @function to map the saved parameter values to the parameter object
         * @param {Object} predefinedParameters Predefined parameters of a particular window type
         * @param {Object} savedParameterValues Saved parameter values
         * @return {Object} parameters
         */
        var mapUserParameterValues = function (predefinedParameters, savedParameterValues) {
            var parameters = [];
            for (var i = 0; i < predefinedParameters.length; i++) {
                var timeStamp = "";
                if (i < savedParameterValues.length) {
                    var parameterValue = savedParameterValues[i];
                    if (predefinedParameters[i].type.includes("STRING")) {
                        parameterValue = parameterValue.slice(1, parameterValue.length - 1)
                    }
                    parameters.push({
                        name: predefinedParameters[i].name, value: parameterValue, description:
                            predefinedParameters[i].description, optional: predefinedParameters[i].optional,
                        defaultValue: predefinedParameters[i].defaultValue, timeStamp: timeStamp
                    });
                } else {
                    parameters.push({
                        name: predefinedParameters[i].name, value: "", description: predefinedParameters[i]
                            .description, optional: predefinedParameters[i].optional,
                        defaultValue: predefinedParameters[i].defaultValue, timeStamp: timeStamp
                    });
                }
            }
            return parameters;
        };

        /**
         * Function to select the parameter mapping method
         * @param {String} selectedType selected window type
         * @param {Object} functionParameters parameters of the selected window type
         * @param {Object} savedParameterValues saved parameter values
         * @param {Object} functionParametersWithValues array to hold the parameter of the mapped value
         */
        var callToMapParameters = function (selectedType, functionParameters, savedParameterValues,
            functionParametersWithValues) {
            if (selectedType === Constants.SORT) {
                functionParametersWithValues = mapParameterValuesSort(functionParameters, savedParameterValues);
            } else if (selectedType === Constants.FREQUENT) {
                functionParametersWithValues = mapParameterValuesFrequent(functionParameters,
                    savedParameterValues);
            } else if (selectedType === Constants.LOSSY_FREQUENT) {
                functionParametersWithValues = mapParameterValuesLossyFrequent(functionParameters,
                    savedParameterValues);
            } else {
                functionParametersWithValues = mapUserParameterValues(functionParameters, savedParameterValues);
            }
            renderParameters(functionParametersWithValues, selectedType, Constants.WINDOW);
        };

        /**
         * @function to render the output event types
         */
        var renderOutputEventTypes = function () {
            var outputEventDiv = '<div class = "clearfix"> <label>Event Type </label></div>' +
                '<div class = "clearfix" ><select id="event-type">' +
                '<option value = "current_events"> current events </option>' +
                '<option value = "all_events"> all events </option>' +
                '<option value = "expired_events"> expired events </option>' +
                '</select> </div>'
            $('#defineOutputEvents').html(outputEventDiv);
        };

        /**
         * @function to build the parameter values
         * @param {Object} parameterValues array to add the parameters
         * @param {Object} predefinedParameters predefined parameters
         * @return {boolean} isError
         */
        var buildParameterValues = function (self, parameterValues, predefinedParameters) {
            $('.parameter').each(function () {
                if ($(this).find('.parameter-name').hasClass('mandatory-parameter') || ($(this).find('.parameter-name')
                    .hasClass('optional-parameter') && $(this).find('.parameter-checkbox').is(":checked"))) {
                    var parameterValue = $(this).find('.parameter-value').val().trim();
                    var parameterName = $(this).find('.parameter-name').text().trim();;
                    var predefinedParameter = self.formUtils.getObject(parameterName, predefinedParameters);
                    if (predefinedParameter.type.includes("STRING")) {
                        parameterValue = "'" + parameterValue + "'";
                    }
                    parameterValues.push(parameterValue)
                }
            });
        };

        /**
         * @function to construct parameter 'attributes'
         * @param {String} parameterValue the attribute value
         * @param {Object} parameterValues array to add the parameters
         */
        var buildAttributes = function (parameterValue, parameterValues) {
            var attributeArray = parameterValue.split(',');
            _.forEach(attributeArray, function (attribute) {
                attribute = attribute.trim();
                if (attribute !== "") {
                    parameterValues.push(attribute);
                }
            });
        };

        /**
         * @function to build parameters for frequent and lossyFrequent type
         * @param {Object} parameterValues array to add the parameters
         * @param {Object} predefinedParameters predefined parameters
         */
        var buildParameterValuesFrequentOrLossyFrequent = function (self, parameterValues, predefinedParameters) {
            $('.parameter').each(function () {
                if ($(this).find('.parameter-name').hasClass('mandatory-parameter') || ($(this).find('.parameter-name')
                    .hasClass('optional-parameter') && $(this).find('.parameter-checkbox').is(":checked"))) {
                    var parameterValue = $(this).find('.parameter-value').val().trim();
                    var parameterName = $(this).find('.parameter-name').text().trim();
                    var predefinedParameter = self.formUtils.getObject(parameterName, predefinedParameters);
                    if (parameterName === "attribute") {
                        buildAttributes(parameterValue, parameterValues);
                    } else {
                        if (predefinedParameter.type.includes("STRING")) {
                            parameterValue = "'" + parameterValue + "'";
                        }
                        parameterValues.push(parameterValue)
                    }
                }
            });
        };

        /**
         * @function to build parameters for sort type
         * @param {Object} parameterValues array to add the parameters
         * @param {Object} predefinedParameters predefined parameters
         */
        var buildParameterValuesSort = function (self, parameterValues, predefinedParameters) {
            $('.parameter').each(function () {
                var parameterValue = $(this).find('.parameter-value').val().trim();
                var parameterName = $(this).find('.parameter-name').text().trim();;
                var predefinedParameter = self.formUtils.getObject(parameterName, predefinedParameters);
                if (parameterName === "window.length") {
                    parameterValues.push(parameterValue)
                } else if (parameterName === "attribute") {
                    if ($('#attribute-parameter').find('.parameter-checkbox').is(":checked")) {
                        buildAttributes(parameterValue, parameterValues);
                    }
                } else {
                    if (($('#attribute-parameter').find('.parameter-checkbox').is(":checked")) && ($
                        ('#order-parameter').find('.parameter-checkbox').is(":checked"))) {
                        parameterValue = "'" + parameterValue + "'";
                        parameterValues.push(parameterValue)
                    }
                }
            });
        };

        /**
         * @function for generic validation of parameter values
         * @param {Object} predefinedParameters predefined parameters of the selected window type
         * @return {boolean} isError
         */
        var validateParameters = function (self, predefinedParameters) {
            var isError = false;
            $('.parameter').each(function () {
                var parameterValue = $(this).find('.parameter-value').val().trim();
                var parameterName = $(this).find('.parameter-name').text().trim();;
                var predefinedParameter = self.formUtils.getObject(parameterName, predefinedParameters);
                if (!predefinedParameter.optional) {
                    if (!checkParameterValue(self, parameterValue, predefinedParameter, this)) {
                        isError = true;
                        return false;
                    }
                } else {
                    if ($(this).find('.parameter-checkbox').is(":checked")) {
                        if (!checkParameterValue(self, parameterValue, predefinedParameter, this)) {
                            isError = true;
                            return false;
                        }
                    }
                }
            });
            return isError;
        };

        /**
         * @function to check the given parameter value
         * @param {String} parameterValue value which needs to be validated
         * @param {Object} predefinedParameter predefined parameter object
         * @param {Object} parent div of the html to locate the parameter
         * @return {boolean}
         */
        var checkParameterValue = function (self, parameterValue, predefinedParameter, parent) {
            if (parameterValue === "") {
                $(parent).find('.error-message').text('Parameter Value is required.');
                self.formUtils.addErrorClass($(parent).find('.parameter-value'));
                return false;
            } else {
                var dataType = predefinedParameter.type;
                if (self.formUtils.validateDataType(dataType, parameterValue)) {
                    var errorMessage = "Invalid data-type. ";
                    _.forEach(dataType, function (type) {
                        errorMessage += type + " or ";
                    });
                    errorMessage = errorMessage.substring(0, errorMessage.length - 4);
                    errorMessage += " is required";
                    $(parent).find('.error-message').text(errorMessage);
                    self.formUtils.addErrorClass($(parent).find('.parameter-value'));
                    return false;
                }
            }
            return true;
        };

        /**
         * @function to map the user saved parameters of lossyFrequent
         * @param {Object} predefinedParameters predefined parameters
         * @param {Object} savedParameterValues user saved parameters
         * @return {Object} parameters
         */
        var mapParameterValuesLossyFrequent = function (predefinedParameters, savedParameterValues) {
            var parameters = [];
            var attributes = "";
            //add the two mandatory params of the saved values to the predefined param objects
            for (var i = 0; i <= 1; i++) {
                parameters.push({
                    name: predefinedParameters[i].name, value: savedParameterValues[i], description:
                        predefinedParameters[i].description, optional: predefinedParameters[i].optional,
                    defaultValue: predefinedParameters[i].defaultValue
                });
            }
            // add the attributes
            for (var i = 2; i < savedParameterValues.length; i++) {
                attributes += savedParameterValues[i] + ", "
            }
            //cutting off the last white space and the comma
            attributes = attributes.substring(0, attributes.length - 2);
            //add the attributes to the third obj of the predefined parameter
            parameters.push({
                name: predefinedParameters[2].name, value: attributes, description:
                    predefinedParameters[2].description, optional: predefinedParameters[2].optional,
                defaultValue: predefinedParameters[2].defaultValue
            });
            return parameters;
        };

        /**
         * @function to map the user saved parameters of frequent
         * @param {Object} predefinedParameters predefined parameters
         * @param {Object} savedParameterValues user saved parameters
         * @return {Object} parameters
         */
        var mapParameterValuesFrequent = function (predefinedParameters, savedParameterValues) {
            var parameters = [];
            var attributes = "";
            //add the first saved param to predefined param's first index (event.count)
            parameters.push({
                name: predefinedParameters[0].name, value: savedParameterValues[0], description:
                    predefinedParameters[0].description, optional: predefinedParameters[0].optional,
                defaultValue: predefinedParameters[0].defaultValue
            });
            // add the attributes
            for (var i = 1; i < savedParameterValues.length; i++) {
                attributes += savedParameterValues[i] + ", "
            }
            //cutting off the last white space and the comma
            attributes = attributes.substring(0, attributes.length - 2);
            //add the attributes to second obj of the predefined parameter
            parameters.push({
                name: predefinedParameters[1].name, value: attributes, description:
                    predefinedParameters[1].description, optional: predefinedParameters[1].optional,
                defaultValue: predefinedParameters[1].defaultValue
            });
            return parameters;
        };

        /**
         * @function to map the user saved parameters of sort
         * @param {Object} predefinedParameters predefined parameters
         * @param {Object} savedParameterValues user saved parameters
         * @return {Object} parameters
         */
        var mapParameterValuesSort = function (predefinedParameters, savedParameterValues) {
            var parameters = [];
            var attributes = "";
            var order = "";
            var length = "";
            if (savedParameterValues.length != 0) {
                length = savedParameterValues[0];
            }
            //add the first saved param to predefined param's first index (window.length)
            parameters.push({
                name: predefinedParameters[0].name, value: length, description:
                    predefinedParameters[0].description, optional: predefinedParameters[0].optional,
                defaultValue: predefinedParameters[0].defaultValue
            });
            // to determine the attributes and order
            if (savedParameterValues.length > 1) {
                for (var i = 1; i < savedParameterValues.length; i++) {
                    if (savedParameterValues[i].indexOf("'") >= 0 || savedParameterValues[i].indexOf('"') >= 0) {
                        order = savedParameterValues[i];
                        order = order.slice(1, order.length - 1)
                    } else {
                        //attributes
                        attributes += savedParameterValues[i] + ", ";

                    }
                }
                //cutting off the last white space and the comma
                attributes = attributes.substring(0, attributes.length - 2);
            }
            //add the attributes to second obj of the predefined parameter
            parameters.push({
                name: predefinedParameters[1].name, value: attributes, description:
                    predefinedParameters[1].description, optional: predefinedParameters[1].optional,
                defaultValue: predefinedParameters[1].defaultValue
            });
            //add the order to the third obj of the predefined parameter
            parameters.push({
                name: predefinedParameters[2].name, value: order, description:
                    predefinedParameters[2].description, optional: predefinedParameters[2].optional,
                defaultValue: predefinedParameters[2].defaultValue
            });
            return parameters;
        };

        /**
         * @function to show and hide the order parameter of sort type
         */
        var showHideOrderForSort = function () {
            if ($('#window-parameters #attribute-parameter').find('.parameter-checkbox').is(":checked")) {
                $('#window-parameters #order-parameter').show();
            } else {
                $('#window-parameters #order-parameter').hide();
            }
        };

        /**
         * @function generate properties form for a window
         * @param element selected element(window)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        WindowForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');
            var clickedElement = self.configurationData.getSiddhiAppConfig().getWindow(id);

            var propertyDiv = $('<div id="property-header"><h3>Window Configuration</h3></div> <div class = '+
            	'"window-form-container"> <h4>Name: </h4> <input type="text" id="windowName" class="clearfix name">' +
                '<label class="error-message" id="windowNameErrorMessage"></label> <div id="define-attribute"></div>' +
                self.formUtils.buildFormButtons() + '</div> <div class= "window-form-container"> ' +
                '<div id = "defineFunctionName"> </div> <div id="defineFunctionParameters"> </div>' +
                '</div> <div class = "window-form-container"> <div id="defineOutputEvents"> </div> </div>' +
                '<div class = "window-form-container"> <div id="define-annotation"> </div> </div>');

            formContainer.append(propertyDiv);
            self.formUtils.popUpSelectedElement(id);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            //declaration and initialization of variables
            var predefinedWindowFunctionNames = _.orderBy(this.configurationData.rawExtensions["windowFunctionNames"],
                ['name'], ['asc']);
            var functionParameters = [];
            var functionParametersWithValues = [];
            var selectedWindowType;
            var annotations = [];

            //event listener to show parameter description
            $('#defineFunctionParameters').on('mouseover', '.parameter-desc', function () {
                $(this).find('.parameter-desc-content').show();
            });

            //event listener to hide parameter description
            $('#defineFunctionParameters').on('mouseout', '.parameter-desc', function () {
                $(this).find('.parameter-desc-content').hide();
            });

            //event listener when the parameter checkbox is changed
            $('#defineFunctionParameters').on('change', '.parameter-checkbox', function () {
                var parameterParent = $(this).parents(".parameter");
                if ($(this).is(':checked')) {
                    parameterParent.find(".optional-param-content").show();
                } else {
                    parameterParent.find(".optional-param-content").hide();
                    parameterParent.find(".parameter-value").removeClass("required-input-field");
                    parameterParent.find(".error-message").text("");
                }
                //check for sort type's parameter (order & attribute params)
                if (selectedType === Constants.SORT) {
                    showHideOrderForSort();
                }
            });

            var windowFunctionNameTemplate = Handlebars.compile($('#type-selection-form-template').html());
            var wrappedHtml = windowFunctionNameTemplate({ id: Constants.WINDOW, types: predefinedWindowFunctionNames });
            $('#defineFunctionName').html(wrappedHtml);

            var name = clickedElement.getName();
            renderOutputEventTypes();
            if (!name) {
                //if window form is freshly opened[unedited window object]
                var attributes = [{ name: "" }];
                self.formUtils.renderAttributeTemplate(attributes)
                selectedWindowType = $('#defineFunctionName #window-type').val();
                functionParameters = self.formUtils.getSelectedTypeParameters(selectedWindowType,
                    predefinedWindowFunctionNames);
                functionParametersWithValues = self.formUtils.createObjectWithValues(functionParameters);
                renderParameters(functionParametersWithValues, selectedWindowType, Constants.WINDOW)
            } else {
                //if window object is already edited
                var windowType = clickedElement.getType().toLowerCase();
                var savedParameterValues = clickedElement.getParameters();

                $('#windowName').val(name.trim());
                selectedType = windowType;
                $('#defineFunctionName').find('#window-type option').filter(function () {
                    return ($(this).val().toLowerCase() == (windowType));
                }).prop('selected', true);
                functionParameters = self.formUtils.getSelectedTypeParameters(windowType, predefinedWindowFunctionNames);
                callToMapParameters(selectedType, functionParameters, savedParameterValues, functionParametersWithValues)
                if (selectedType === Constants.SORT) {
                    showHideOrderForSort();
                }
                var savedAttributes = clickedElement.getAttributeList();
                self.formUtils.renderAttributeTemplate(savedAttributes)
                self.formUtils.selectTypesOfSavedAttributes(savedAttributes);

                var savedOutputEventType = clickedElement.getOutputEventType().toLowerCase();
                $('#defineOutputEvents').find('#event-type option').filter(function () {
                    return ($(this).val().toLowerCase() == (savedOutputEventType));
                }).prop('selected', true);

                var savedAnnotationObjects = clickedElement.getAnnotationListObjects();
                annotations = savedAnnotationObjects;
            }
            self.formUtils.renderAnnotationTemplate("define-annotation", annotations);

            $('#window-type').change(function () {
                functionParameters = self.formUtils.getSelectedTypeParameters(this.value, predefinedWindowFunctionNames);
                selectedType = this.value.toLowerCase();
                if (savedParameterValues && selectedType == windowType.toLowerCase()) {
                    callToMapParameters(selectedType, functionParameters, savedParameterValues, functionParametersWithValues)
                } else {
                    functionParametersWithValues = self.formUtils.createObjectWithValues(functionParameters);
                    renderParameters(functionParametersWithValues, selectedType, Constants.WINDOW);
                }
                if (selectedType === Constants.SORT) {
                    showHideOrderForSort();
                }
            });

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#btn-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                //clear the error messages
                $('.error-message').text("")
                $('.required-input-field').removeClass('required-input-field');
                var isErrorOccurred = false;

                var windowName = $('#windowName').val().trim();
                //check if window name is empty
                if (windowName == "") {
                    self.formUtils.addErrorClass("#windowName");
                    $('#windowNameErrorMessage').text("Window name is required.")
                    isErrorOccurred = true;
                    return;
                }
                var previouslySavedName = clickedElement.getName();
                if (previouslySavedName === undefined) {
                    previouslySavedName = "";
                }
                if (previouslySavedName !== windowName) {
                    var isWindowNameUsed = self.formUtils.isDefinitionElementNameUsed(windowName, id);
                    if (isWindowNameUsed) {
                        self.formUtils.addErrorClass("#windowName");
                        $('#windowNameErrorMessage').text("Window name is already used.")
                        isErrorOccurred = true;
                        return;
                    }
                    if (self.formUtils.validateAttributeOrElementName("#windowName", Constants.WINDOW, windowName)) {
                        isErrorOccurred = true;
                        return;
                    }
                }
                var attributeNameList = [];
                if (self.formUtils.validateAttributes(attributeNameList)) {
                    isErrorOccurred = true;
                    return;
                }

                if (attributeNameList.length == 0) {
                    self.formUtils.addErrorClass($('.attribute:eq(0)').find('.attr-name'))
                    $('.attribute:eq(0)').find('.error-message').text("Minimum one attribute is required.");
                    isErrorOccurred = true;
                    return;
                }

                var windowType = $('#defineFunctionName #window-type').val();
                var parameters = [];
                if (validateParameters(self, functionParameters)) {
                    isErrorOccurred = true;
                    return;
                }

                if (!isErrorOccurred) {
                    if (previouslySavedName !== windowName) {
                        // update connection related to the element if the name is changed
                        clickedElement.setName(windowName);
                        self.formUtils.updateConnectionsAfterDefinitionElementNameChange(id);

                        var textNode = $(element).parent().find('.windowNameNode');
                        textNode.html(windowName);
                    }

                    if (windowType.toLowerCase() === Constants.SORT) {
                        buildParameterValuesSort(self, parameters, functionParameters)
                    } else if (windowType.toLowerCase() === Constants.FREQUENT ||
                        windowType.toLowerCase() === Constants.LOSSY_FREQUENT) {
                        buildParameterValuesFrequentOrLossyFrequent(self, parameters, functionParameters);
                    } else {
                        buildParameterValues(self, parameters, functionParameters)
                    }
                    clickedElement.setType(windowType);
                    clickedElement.setParameters(parameters);

                    //clear the previously saved attribute list
                    clickedElement.clearAttributeList();
                    //add the attributes to the attribute list
                    $('.attribute .attr-content').each(function () {
                        var nameValue = $(this).find('.attr-name').val().trim();
                        var typeValue = $(this).find('.attr-type').val();
                        if (nameValue != "") {
                            var attributeObject = new Attribute({ name: nameValue, type: typeValue });
                            clickedElement.addAttribute(attributeObject)
                        }
                    });

                    var outputEventType = $('#defineOutputEvents #event-type').val().toUpperCase();
                    clickedElement.setOutputEventType(outputEventType);

                    clickedElement.clearAnnotationList();
                    clickedElement.clearAnnotationListObjects();
                    var annotationStringList = [];
                    var annotationObjectList = [];
                    var annotationNodes = $('#annotation-div').jstree(true)._model.data['#'].children;
                    self.formUtils.buildAnnotation(annotationNodes, annotationStringList, annotationObjectList);
                    _.forEach(annotationStringList, function (annotation) {
                        clickedElement.addAnnotation(annotation);
                    });
                    _.forEach(annotationObjectList, function (annotation) {
                        clickedElement.addAnnotationObject(annotation);
                    });

                    $('#' + id).removeClass('incomplete-element');
                    //Send window element to the backend and generate tooltip
                    var windowToolTip = self.formUtils.getTooltip(clickedElement, Constants.WINDOW);
                    $('#' + id).prop('title', windowToolTip);

                    self.designViewContainer.removeClass('disableContainer');
                    self.toggleViewButton.removeClass('disableContainer');
                    // set the isDesignViewContentChanged to true
                    self.configurationData.setIsDesignViewContentChanged(true);
                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                }
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

        return WindowForm;
    });


