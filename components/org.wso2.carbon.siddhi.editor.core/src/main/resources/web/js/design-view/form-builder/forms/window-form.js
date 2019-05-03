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
                this.jsPlumbInstance = options.jsPlumbInstance;
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
            var windowObject = self.configurationData.getSiddhiAppConfig().getWindow(id);

            var propertyDiv = $('<div class="clearfix"><div class = "window-form-container"> <label> ' +
                '<span class="mandatory-symbol"> * </span> Name </label> <input type="text" id="windowName" class="clearfix name">' +
                '<label class="error-message" id="windowNameErrorMessage"></label> <div id="define-attribute"></div>' +
                self.formUtils.buildFormButtons() + '</div> <div class= "window-form-container"> ' +
                '<div class = "defineFunctionName"> </div> <div class ="defineFunctionParameters"> </div>' +
                '</div> </div> <div class = "window-form-container"> <div class="define-output-events"> </div>' +
                '<div class="define-annotation"></div> </div>');

            formContainer.append(propertyDiv);
            self.formUtils.popUpSelectedElement(id);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            self.formUtils.addEventListenerToRemoveRequiredClass();
            self.formUtils.addEventListenerToShowAndHideInfo();
            self.formUtils.addEventListenerToShowInputContentOnHover();

            //declaration and initialization of variables
            var predefinedWindowFunctionNames = _.orderBy(this.configurationData.rawExtensions["windowFunctionNames"],
                ['name'], ['asc']);
            var functionParameters = [];
            var functionParametersWithValues = [];
            var selectedType;
            var annotations = [];

            self.formUtils.renderFunctions(predefinedWindowFunctionNames, '.window-form-container', Constants.WINDOW);
            var name = windowObject.getName();
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
                var windowType = windowObject.getType().toLowerCase();
                var parameterValues = windowObject.getParameters();

                $('#windowName').val(name.trim());
                selectedType = windowType;
                $('.defineFunctionName').find('#window-type option').filter(function () {
                    return ($(this).val().toLowerCase() == (windowType));
                }).prop('selected', true);
                functionParameters = self.formUtils.getSelectedTypeParameters(windowType, predefinedWindowFunctionNames);
                self.formUtils.callToMapParameters(selectedType, functionParameters, parameterValues,
                    '.window-form-container')
                if (selectedType === Constants.SORT) {
                    self.formUtils.showHideOrderForSort();
                }
                var attributeList = windowObject.getAttributeList();
                self.formUtils.renderAttributeTemplate(attributeList)
                self.formUtils.selectTypesOfSavedAttributes(attributeList);

                var outputEventType = windowObject.getOutputEventType().toLowerCase();
                $('.define-output-events').find('#event-type option').filter(function () {
                    return ($(this).val().toLowerCase() == (outputEventType));
                }).prop('selected', true);

                var annotationListObjects = windowObject.getAnnotationListObjects();
                annotations = annotationListObjects;
            }
            self.formUtils.renderAnnotationTemplate("define-annotation", annotations);
            self.formUtils.addEventListenersForParameterDiv();
            self.formUtils.addEventListenerForSortWindow(selectedType)

            $('#window-type').change(function () {
                functionParameters = self.formUtils.getSelectedTypeParameters(this.value, predefinedWindowFunctionNames);
                selectedType = this.value.toLowerCase();
                if (parameterValues && selectedType == windowType.toLowerCase()) {
                    self.formUtils.callToMapParameters(selectedType, functionParameters, parameterValues,
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

            self.formUtils.initializeNanoScroller();

            // 'Submit' button action
            $(formContainer).on('click', '#btn-submit', function () {

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
                var previouslySavedName = windowObject.getName();
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
                    var outConnections = self.jsPlumbInstance.getConnections({ source: id + '-out' });
                    var inConnections = self.jsPlumbInstance.getConnections({ target: id + '-in' });
                    // delete connections related to the element if the name is changed
                    self.formUtils.deleteConnectionsAfterDefinitionElementNameChange(outConnections, inConnections);
                    // update selected window model
                    windowObject.setName(windowName);
                    // establish connections related to the element if the name is changed
                    self.formUtils.establishConnectionsAfterDefinitionElementNameChange(outConnections, inConnections);
                    var textNode = $(element).parent().find('.windowNameNode');
                    textNode.html(windowName);

                    var parameters = self.formUtils.buildWindowParameters('.window-form-container', windowType,
                        predefinedWindowFunctionNames)
                    windowObject.setType(windowType);
                    windowObject.setParameters(parameters);

                    //clear the previously saved attribute list
                    windowObject.clearAttributeList();
                    //add the attributes to the attribute list
                    $('.attribute .attr-content').each(function () {
                        var nameValue = $(this).find('.attr-name').val().trim();
                        var typeValue = $(this).find('.attr-type').val();
                        if (nameValue != "") {
                            var attributeObject = new Attribute({ name: nameValue, type: typeValue });
                            windowObject.addAttribute(attributeObject)
                        }
                    });

                    var outputEventType = $('.define-output-events #event-type').val().toUpperCase();
                    windowObject.setOutputEventType(outputEventType);

                    windowObject.clearAnnotationList();
                    windowObject.clearAnnotationListObjects();
                    var annotationStringList = [];
                    var annotationObjectList = [];
                    var annotationNodes = $('#annotation-div').jstree(true)._model.data['#'].children;
                    self.formUtils.buildAnnotation(annotationNodes, annotationStringList, annotationObjectList);
                    _.forEach(annotationStringList, function (annotation) {
                        windowObject.addAnnotation(annotation);
                    });
                    _.forEach(annotationObjectList, function (annotation) {
                        windowObject.addAnnotationObject(annotation);
                    });

                    $('#' + id).removeClass('incomplete-element');
                    //Send window element to the backend and generate tooltip
                    var windowToolTip = self.formUtils.getTooltip(windowObject, Constants.WINDOW);
                    $('#' + id).prop('title', windowToolTip);

                    // set the isDesignViewContentChanged to true
                    self.configurationData.setIsDesignViewContentChanged(true);
                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                }
            });

            // 'Cancel' button action
            var cancelButtonElement = $(formContainer).find('#btn-cancel')[0];
            cancelButtonElement.addEventListener('click', function () {
                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            });
        };

        return WindowForm;
    });
