/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'log', 'jquery', 'lodash', 'constants'],
    function (require, log, $, _, Constants) {

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
                this.jsPlumbInstance = options.jsPlumbInstance;
                this.consoleListManager = options.application.outputController;
                var currentTabId = this.application.tabController.activeTab.cid;
                this.designViewContainer = $('#design-container-' + currentTabId);
                this.toggleViewButton = $('#toggle-view-button-' + currentTabId);
            }
        };

        /**
         * @function renders the script types
         */
        var renderScriptType = function () {
            var scriptDiv = '<h4> Script Type: </h4> <select id = "script-type">' +
                '<option value = "Javascript"> Javascript </option>' +
                '<option value = "Scala"> Scala </option>' +
                '<option value = "R"> R </option>' +
                '</select>';
            $('#function-script-type').html(scriptDiv);
        };

        /**
         * @function renders the return type of the script
         */
        var renderReturnType = function () {
            var returnDiv = '<h4> Return Type: </h4> <select id = "return-type">' +
                '<option value = "int"> int </option>' +
                '<option value = "long"> long </option>' +
                '<option value = "double"> double </option>' +
                '<option value = "float"> float </option>' +
                '<option value = "string"> string </option>' +
                '<option value = "bool"> bool </option>' +
                '<option value = "object"> object </option>' +
                '</select>';
            $('#function-return-type').html(returnDiv);
        };

        /**
         * @function generate properties form for a function
         * @param element selected element(function)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        FunctionForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');
            var functionObject = self.configurationData.getSiddhiAppConfig().getFunction(id);
            var previouseFunctionObject = _.cloneDeep(functionObject);
            var propertyDiv = $('<div class="clearfix form-min-width"> <div class = "function-form-container"> ' +
                '<div id = "define-function-name"> <label>' +
                ' <span class="mandatory-symbol"> * </span> Name </label> ' +
                '<input type="text" id="functionName" class="clearfix name"><label class = "error-message" ' +
                'id = "functionNameErrorMessage"> </label></div> <div id = "function-script-type"> ' +
                '</div> <div id= "function-return-type"> </div> </div> <div class = "function-form-container"> ' +
                '<div id="define-script-body"> <label> <span class="mandatory-symbol"> * </span> Script Body </label> ' +
                '<textarea id= "script-body-content" class="clearfix" rows="5" cols="50"> </textarea> ' +
                '<label class = "error-message"></label> </div> </div> </div>');

            formContainer.html(propertyDiv);
            self.formUtils.buildFormButtons(formConsole.cid);
            self.formUtils.popUpSelectedElement(id);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            self.formUtils.addEventListenerToRemoveRequiredClass();
            self.formUtils.addEventListenerToShowInputContentOnHover();

            var name = functionObject.getName();
            renderScriptType();
            renderReturnType();
            if (name) {
                //if the function object is already edited
                var scriptType = (functionObject.getScriptType()).toLowerCase();
                var returnType = (functionObject.getReturnType()).toLowerCase();
                var body = functionObject.getBody().trim();

                //populate the saved values
                $('#functionName').val(name.trim());
                $('#function-script-type').find('#script-type option').filter(function () {
                    return ($(this).val().toLowerCase() == (scriptType.toLowerCase()));
                }).prop('selected', true);
                $('#function-return-type').find('#return-type option').filter(function () {
                    return ($(this).val().toLowerCase() == (returnType.toLowerCase()));
                }).prop('selected', true);
                $('#script-body-content').val(body);

            }

            self.formUtils.initPerfectScroller(formConsole.cid);

            // 'Submit' button action
            $('#' + formConsole.cid).on('click', '#btn-submit', function () {

                self.formUtils.removeErrorClass();
                var isErrorOccurred = false;

                var functionName = $('#functionName').val().trim();
                var previouslySavedName = functionObject.getName();
                if (functionName === "") {
                    self.formUtils.addErrorClass($('#functionName'));
                    $('#functionNameErrorMessage').text("Function name is required.");
                    isErrorOccurred = true;
                    return;
                }
                if (!previouslySavedName) {
                    previouslySavedName = "";
                }
                if (previouslySavedName !== functionName) {
                    var isFunctionNameUsed = self.formUtils.isFunctionDefinitionElementNameUsed(functionName, id);
                    if (isFunctionNameUsed) {
                        self.formUtils.addErrorClass('#functionName');
                        $('#functionNameErrorMessage').text("Function name is already used.");
                        isErrorOccurred = true;
                        return;
                    }
                    if (self.formUtils.validateAttributeOrElementName("#functionName", Constants.FUNCTION, functionName)) {
                        isErrorOccurred = true;
                        return;
                    }
                }

                var scriptBody = $('#script-body-content').val().trim();
                if (scriptBody === "") {
                    self.formUtils.addErrorClass('#script-body-content');
                    $('#define-script-body').find('.error-message').text("Script body is required.");
                    isErrorOccurred = true;
                    return;
                }

                if (!isErrorOccurred) {
                    functionObject.setName(functionName);
                    var textNode = $(element).parent().find('.functionNameNode');
                    textNode.html(functionName);

                    var scriptType = $('#script-type').val();
                    var returnType = $('#return-type').val();
                    functionObject.setScriptType(scriptType.toUpperCase());
                    functionObject.setReturnType(returnType.toUpperCase());
                    functionObject.setBody(scriptBody);

                    if (self.formUtils.isUpdatingOtherElementsRequired(previouseFunctionObject, functionObject,
                        Constants.FUNCTION)) {
                        var outConnections = self.jsPlumbInstance.getConnections({source: id + '-out'});
                        var inConnections = self.jsPlumbInstance.getConnections({target: id + '-in'});

                        //to delete the connection, it requires the previous object name
                        functionObject.setName(previouseFunctionObject.getName())
                        // delete connections related to the element if the name is changed
                        self.formUtils.deleteConnectionsAfterDefinitionElementNameChange(outConnections, inConnections);
                        //reset the name to new name
                        functionObject.setName(functionName);

                        // establish connections related to the element if the name is changed
                        self.formUtils.establishConnectionsAfterDefinitionElementNameChange(outConnections, inConnections);
                    }

                    $('#' + id).removeClass('incomplete-element');
                    //Send function element to the backend and generate tooltip
                    var functionToolTip = self.formUtils.getTooltip(functionObject, Constants.FUNCTION);
                    $('#' + id).prop('title', functionToolTip);

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

        return FunctionForm;
    });


