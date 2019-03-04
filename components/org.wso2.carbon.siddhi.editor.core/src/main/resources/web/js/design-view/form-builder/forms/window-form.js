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

define(['require', 'log', 'jquery', 'lodash', 'attribute', 'constants'],
    function (require, log, $, _, Attribute, Constants) {

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
         * @function generate properties form for a window
         * @param element selected element(window)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        WindowForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');
            var clickedElement = self.configurationData.getSiddhiAppConfig().getWindow(id);

            var propertyDiv = $('<div id="property-header"><h3>Window Configuration</h3></div> <div class = ' +
                '"window-form-container"> <h4>Name: </h4> <input type="text" id="windowName" class="clearfix name">' +
                '<label class="error-message" id="windowNameErrorMessage"></label> <div id="define-attribute"></div>' +
                self.formUtils.buildFormButtons() + '</div> <div class= "window-form-container"> ' +
                '<div class = "defineFunctionName"> </div> <div class ="defineFunctionParameters"> </div>' +
                '</div> <div class = "window-form-container"> <div class="define-output-events"> </div> </div>' +
                '<div class = "window-form-container"> <div class="define-annotation"> </div> </div>');

            formContainer.append(propertyDiv);
            self.formUtils.popUpSelectedElement(id);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            self.formUtils.addEventListenerToRemoveRequiredClass();

            //declaration and initialization of variables
            var predefinedWindowFunctionNames = _.orderBy(this.configurationData.rawExtensions["windowFunctionNames"],
                ['name'], ['asc']);
            var functionParameters = [];
            var functionParametersWithValues = [];
            var selectedWindowType;
            var annotations = [];

			self.formUtils.renderFunctions(predefinedWindowFunctionNames, '.window-form-container', Constants.WINDOW);
            var name = clickedElement.getName();
            self.formUtils.renderOutputEventTypes();
            if (!name) {
                //if window form is freshly opened[unedited window object]
                var attributes = [{ name: "" }];
                self.formUtils.renderAttributeTemplate(attributes)
                selectedType = $('.defineFunctionName #window-type').val();
                functionParameters = self.formUtils.getSelectedTypeParameters(selectedType,
                    predefinedWindowFunctionNames);
                functionParametersWithValues = self.formUtils.createObjectWithValues(functionParameters);
                self.formUtils.renderParameters(functionParametersWithValues, selectedType, Constants.WINDOW)
            } else {
                //if window object is already edited
                var windowType = clickedElement.getType().toLowerCase();
                var savedParameterValues = clickedElement.getParameters();

                $('#windowName').val(name.trim());
                selectedType = windowType;
                $('.defineFunctionName').find('#window-type option').filter(function () {
                    return ($(this).val().toLowerCase() == (windowType));
                }).prop('selected', true);
                functionParameters = self.formUtils.getSelectedTypeParameters(windowType, predefinedWindowFunctionNames);
                self.formUtils.callToMapParameters(selectedType, functionParameters, savedParameterValues,
                    '.window-form-container')
                if (selectedType === Constants.SORT) {
                    self.formUtils.showHideOrderForSort();
                }
                var savedAttributes = clickedElement.getAttributeList();
                self.formUtils.renderAttributeTemplate(savedAttributes)
                self.formUtils.selectTypesOfSavedAttributes(savedAttributes);

                var savedOutputEventType = clickedElement.getOutputEventType().toLowerCase();
                $('.define-output-events').find('#event-type option').filter(function () {
                    return ($(this).val().toLowerCase() == (savedOutputEventType));
                }).prop('selected', true);

                var savedAnnotationObjects = clickedElement.getAnnotationListObjects();
                annotations = savedAnnotationObjects;
            }
            self.formUtils.renderAnnotationTemplate("define-annotation", annotations);
            self.formUtils.addEventListenersForParameterDiv();
            self.formUtils.addEventListenerForSortWindow(selectedType)

            $('#window-type').change(function () {
                functionParameters = self.formUtils.getSelectedTypeParameters(this.value, predefinedWindowFunctionNames);
                selectedType = this.value.toLowerCase();
                if (savedParameterValues && selectedType == windowType.toLowerCase()) {
                    self.formUtils.callToMapParameters(selectedType, functionParameters, savedParameterValues,
                        '.window-form-container')
                } else {
                    functionParametersWithValues = self.formUtils.createObjectWithValues(functionParameters);
                    self.formUtils.renderParameters(functionParametersWithValues, Constants.WINDOW,
                    '.window-form-container');
                }
                if (selectedType === Constants.SORT) {
					self.formUtils.showHideOrderForSort();
					self.formUtils.addEventListenerForSortWindow(selectedType)
				}
            });

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#btn-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                self.formUtils.removeErrorClass();
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

                var windowType = $('.defineFunctionName #window-type').val();
                if (self.formUtils.validateParameters('.window-form-container', functionParameters)) {
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

					var parameters = self. formUtils.buildWindowParameters('.window-form-container', windowType,
					predefinedWindowFunctionNames)
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

                    var outputEventType = $('.define-output-events #event-type').val().toUpperCase();
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
