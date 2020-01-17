/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
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

        //function to get the attributes
        var getAttributes = function () {
            var attributes = [];
            $('.attr-name').each(function () {
                var value = $(this).val().trim();
                if (value !== "") {
                    attributes.push(value)
                }
            });
            return attributes;
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
            var previousWindowObject = _.cloneDeep(windowObject);

            var propertyDiv = $('<div class="clearfix form-min-width"><div class = "window-form-container"> <label> ' +
                '<span class="mandatory-symbol"> * </span>Name </label> <input type="text" id="windowName" class="clearfix name">' +
                '<label class="error-message" id="windowNameErrorMessage"></label> <div id="define-attribute"></div>' +
                '</div> <div class= "window-form-container"> <div class = "defineFunctionName"> </div> ' +
                '<div class ="defineFunctionParameters"> </div> </div> <div class = "window-form-container"> ' +
                '<div class="define-output-events"> </div><div class="define-annotation"></div> </div> </div>');

            formContainer.html(propertyDiv);
            self.formUtils.buildFormButtons(formConsole.cid);
            self.formUtils.popUpSelectedElement(id);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            self.formUtils.addEventListenerToRemoveRequiredClass();
            self.formUtils.addEventListenerToShowAndHideInfo();
            self.formUtils.addEventListenerToShowInputContentOnHover();

            //declaration and initialization of variables
            var predefinedWindowFunctionNames = _.orderBy(this.configurationData.rawExtensions["windowFunctionNames"],
                ['name'], ['asc']);
            var windowFormContainer = '.window-form-container';
            var savedParameters = [];
            var selectedType;
            var annotations = [];

            self.formUtils.renderFunctions(predefinedWindowFunctionNames, windowFormContainer, Constants.WINDOW);
            self.formUtils.showDropDown();
            var name = windowObject.getName();
            self.formUtils.renderOutputEventTypes();
            if (!name) {
                //if window form is freshly opened[unedited window object]
                var attributes = [{name: ""}];
                self.formUtils.renderAttributeTemplate(attributes)
                selectedType = Constants.BATCH_WINDOW_PROCESSOR;
            } else {
                //if window object is already edited
                var windowType = windowObject.getType().toLowerCase();
                savedParameters = windowObject.getParameters();

                $('#windowName').val(name.trim());
                selectedType = windowType;

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
            var windowParameters = {
                type: Constants.WINDOW,
                value: {
                    function: selectedType,
                    parameters: savedParameters
                }
            };
            self.formUtils.mapParameterValues(windowParameters, windowFormContainer, false);
            self.formUtils.renderAnnotationTemplate("define-annotation", annotations);

            $(windowFormContainer).on("autocompleteselect", '.custom-combobox-input', function (event, ui) {
                var savedParameterValues = [];
                var selectedType = ui.item.value.toLowerCase();
                if (name && self.formUtils.getFunctionNameWithoutParameterOverload(selectedType) === windowType) {
                    savedParameterValues = savedParameters;
                }
                var currentlySelectedWindow = {
                    type: Constants.WINDOW,
                    value: {
                        function: selectedType,
                        parameters: savedParameterValues
                    }
                };
                self.formUtils.mapParameterValues(currentlySelectedWindow, windowFormContainer, true);
                self.formUtils.addAutoCompleteForStreamWindowFunctionAttributes(getAttributes());
            });

            $(windowFormContainer).on("blur", ".attr-name", function () {
                self.formUtils.addAutoCompleteForStreamWindowFunctionAttributes(getAttributes());
            });

            self.formUtils.initPerfectScroller(formConsole.cid);
            self.formUtils.addAutoCompleteForStreamWindowFunctionAttributes(getAttributes());

            // 'Submit' button action
            $('#' + formConsole.cid).on('click', '#btn-submit', function () {

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

                if ($(windowFormContainer).find('.display-predefined-parameters').length === 0) {
                    var predefinedParameters = self.formUtils.getPredefinedParameters(windowFormContainer,
                        predefinedWindowFunctionNames);
                    if (self.formUtils.validateParameters(windowFormContainer, predefinedParameters)) {
                        isErrorOccurred = true;
                        return;
                    }
                } else {
                    if (self.formUtils.validateUnknownParameters(windowFormContainer)) {
                        isErrorOccurred = true;
                        return;
                    }
                }

                if (!isErrorOccurred) {
                    windowObject.setName(windowName);
                    var textNode = $(element).parent().find('.windowNameNode');
                    textNode.html(windowName);

                    var parameters = self.formUtils.buildParameterValues(windowFormContainer);
                    windowObject.setType(self.formUtils.getFunctionNameWithoutParameterOverload($('.custom-combobox-input').val()));
                    windowObject.setParameters(parameters);

                    //clear the previously saved attribute list
                    windowObject.clearAttributeList();
                    //add the attributes to the attribute list
                    $('.attribute .attr-content').each(function () {
                        var nameValue = $(this).find('.attr-name').val().trim();
                        var typeValue = $(this).find('.attr-type').val();
                        if (nameValue != "") {
                            var attributeObject = new Attribute({name: nameValue, type: typeValue});
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

                    if (self.formUtils.isUpdatingOtherElementsRequired(previousWindowObject, windowObject,
                        Constants.WINDOW)) {
                        var outConnections = self.jsPlumbInstance.getConnections({source: id + '-out'});
                        var inConnections = self.jsPlumbInstance.getConnections({target: id + '-in'});

                        //to delete the connection, it requires the previous object name
                        windowObject.setName(previousWindowObject.getName())
                        // delete connections related to the element if the name is changed
                        self.formUtils.deleteConnectionsAfterDefinitionElementNameChange(outConnections, inConnections);
                        //reset the name to new name
                        windowObject.setName(windowName);

                        // establish connections related to the element if the name is changed
                        self.formUtils.establishConnectionsAfterDefinitionElementNameChange(outConnections, inConnections);
                    }

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
            $('#' + formConsole.cid).on('click', '#btn-cancel', function () {
                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            });
        };

        return WindowForm;
    });
