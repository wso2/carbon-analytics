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

define(['require', 'log', 'jquery', 'lodash', 'constants'],
    function (require, log, $, _, Constants) {

        /**
         * @class TriggerForm Creates a forms to collect data from a trigger
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var TriggerForm = function (options) {
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
         * @function to render the drop down for trigger-criteria
         * @param {Object} triggerObject array of trigger criteria
         */
        var renderTriggerCriteria = function (triggerObject) {
            var triggerCriteriaDiv = '<h4> Trigger Criteria </h4> <select id = "trigger-criteria-type">';
            _.forEach(triggerObject, function (triggerCriteria) {
                triggerCriteriaDiv += '<option value = "' + triggerCriteria.name + '">' + triggerCriteria.name + '</option>';
            });
            triggerCriteriaDiv += '</select> <i class = "fw fw-info"> ' +
                '<span style = "display:none" class = "criteria-description"> </span> </i>';
            $('#define-trigger-criteria').html(triggerCriteriaDiv);
        };

        /**
         * @function to render a textbox according to the selected criteria-type
         * @param {String} selectedCriteriaType selected criteria type from the select-box
         */
        var renderTriggerCriteriaContent = function (selectedCriteriaType) {
            if (selectedCriteriaType === Constants.START) {
                //show no text-box
                $('#trigger-criteria-content').html('');
            } else {
                //render a text-box to put the atEvery or cron-expression value
                $('#trigger-criteria-content').html('<input type="text" class="clearfix"> ' +
                    '<label class="error-message" > </label>');
            }
        };

        /**
         * @function to obtain a particular trigger object from the predefined triggers
         * @param {Object} triggerObject predefined trigger object
         * @param {String} selectedCriteria selected trigger criteria
         * @return {Object} triggerCriteriaObject
         */
        var getTriggerCriteria = function (triggerObject, selectedCriteria) {
            var triggerCriteriaObject =
                _.find(triggerObject, function (criteria) {
                    return criteria.name == selectedCriteria
                });
            return triggerCriteriaObject;
        };

        /**
         * @function to determine the trigger-criteria-type
         * @param {String} triggerCriteria criteria value
         * @param {String} triggerCriteriaType at or every
         * @return {String} criteriaType
         */
        var determineCriteriaType = function (triggerCriteria, triggerCriteriaType) {
            if (triggerCriteriaType === Constants.AT) {
                if (triggerCriteria.toLowerCase() === Constants.START) {
                    //criteria-type is start
                    renderTriggerCriteriaContent(Constants.START);
                    return Constants.START;
                } else {
                    //cron expression
                    renderTriggerCriteriaContent(Constants.CRON_EXPRESSION);
                    return Constants.CRON_EXPRESSION;
                }
            } else {
                //atEvery
                renderTriggerCriteriaContent(Constants.EVERY);
                return Constants.EVERY;
            }
        };

        /**
         * @function to show the trigger criteria description
         * @param {Object} triggerCriteria predefined trigger object
         * @param {String} selected trigger criteria
         */
        var showTriggerCriteriaDescription = function (triggerObject, selectedCriteria) {
            var triggerCriteriaObject = getTriggerCriteria(triggerObject, selectedCriteria);
            $('#define-trigger-criteria .criteria-description').text(triggerCriteriaObject.description);
        };

        /**
         * @function generate properties form for a trigger
         * @param element selected element(trigger)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        TriggerForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');
            var triggerObject = self.configurationData.getSiddhiAppConfig().getTrigger(id);

            var propertyDiv = $('<div id="property-header"><h3>Trigger Configuration</h3></div>' +
                '<div class ="trigger-form-container"> <div id="define-trigger-name"> <h4>Name: </h4>' +
                '<input type="text" id="triggerName" class="clearfix name"> <label class="error-message" ' +
                'id = "triggerNameErrorMessage"> </label> </div>' + self.formUtils.buildFormButtons() + '</div>' +
                '<div class = "trigger-form-container"> <div id= "define-trigger-criteria"> </div>' +
                '<div id = "trigger-criteria-content" ></div> </div>');

            formContainer.append(propertyDiv);
            self.formUtils.popUpSelectedElement(id);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var name = triggerObject.getName();
            var triggerCriteriaObject = self.configurationData.application.config.trigger;
            renderTriggerCriteria(triggerCriteriaObject);

            self.formUtils.addEventListenerToRemoveRequiredClass();

            //Event listener to show the criteria description
            $('#define-trigger-criteria').on('mouseover', '.fw-info', function () {
                $(this).find('.criteria-description').show();
            });

            //Event listener to hide the criteria description
            $('#define-trigger-criteria').on('mouseout', '.fw-info', function () {
                $(this).find('.criteria-description').hide();
            });

            if (name) {
                //if the trigger object is already edited
                $('#triggerName').val(name.trim());
                var triggerCriteriaType = triggerObject.getCriteriaType().trim();
                var triggerCriteria = triggerObject.getCriteria().trim();
                if (triggerCriteriaType === Constants.AT) {
                    if (triggerCriteria.indexOf("'") >= 0 || triggerCriteria.indexOf('"') >= 0) {
                        //to remove the string quote from the start and cron expression
                        triggerCriteria = triggerCriteria.slice(1, triggerCriteria.length - 1);
                    }
                } else {
                    //remove every from atEvery's value
                    var replaceEvery = triggerCriteria;
                    triggerCriteria = replaceEvery.replace(Constants.EVERY, '');
                }
                triggerCriteria = triggerCriteria.trim();
                var selectedCriteria = determineCriteriaType(triggerCriteria, triggerCriteriaType);
                $('#define-trigger-criteria').find('#trigger-criteria-type option').filter(function () {
                    return ($(this).val().toLowerCase() == (selectedCriteria.toLowerCase()));
                }).prop('selected', true);

                $('#trigger-criteria-content input[type="text"]').val(triggerCriteria);
                showTriggerCriteriaDescription(triggerCriteriaObject, selectedCriteria)
            }

            //onchange of the triggerCriteria-type selection
            $('#trigger-criteria-type').change(function () {
                renderTriggerCriteriaContent(this.value);
                showTriggerCriteriaDescription(triggerCriteriaObject, this.value)
                if (triggerCriteria && this.value === selectedCriteria) {
                    if (this.value !== Constants.START) {
                        $('#trigger-criteria-content input[type="text"]').val(triggerCriteria);
                    }
                } else {
                    if (this.value !== Constants.START) {
                        var triggerCriteriaObject = getTriggerCriteria(triggerCriteriaObject, this.value);
                        $('#trigger-criteria-content input[type="text"]').val(triggerCriteriaObject.defaultValue);
                    }
                }
            });

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#btn-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                self.formUtils.removeErrorClass();
                var isErrorOccurred = false;

                var triggerName = $('#triggerName').val().trim();
                // to check if trigger name is empty
                if (triggerName == "") {
                    self.formUtils.addErrorClass('#triggerName');
                    $('#triggerNameErrorMessage').text("Trigger name is required.");
                    isErrorOccurred = true;
                    return;
                }
                var previouslySavedName = triggerObject.getName();
                if (!previouslySavedName) {
                    previouslySavedName = "";
                }
                // update connection related to the element if the name is changed
                if (previouslySavedName !== triggerName) {
                    //check if name is already used
                    var isTriggerNameUsed = self.formUtils.isDefinitionElementNameUsed(triggerName,
                        triggerObject.getId());
                    if (isTriggerNameUsed) {
                        self.formUtils.addErrorClass('#triggerName');
                        $('#triggerNameErrorMessage').text("Trigger name is already used.");
                        isErrorOccurred = true;
                        return;
                    }
                    if (self.formUtils.validateAttributeOrElementName("#triggerName", Constants.TRIGGER, triggerName)) {
                        isErrorOccurred = true;
                        return;
                    }
                }

                var selectedCriteriaType = $('#trigger-criteria-type').val();
                var triggerCriteria;
                if (selectedCriteriaType !== Constants.START) {
                    triggerCriteria = $('#trigger-criteria-content input[type="text"]').val().trim();
                    if (triggerCriteria === "") {
                        self.formUtils.addErrorClass($('#trigger-criteria-content input[type="text"]'));
                        $('#trigger-criteria-content').find('.error-message').text("Trigger criteria value is " +
                            "required");
                        isErrorOccurred = true;
                        return;
                    }
                }

                if (!isErrorOccurred) {
                    if (previouslySavedName !== triggerName) {
                        // update selected trigger model
                        triggerObject.setName(triggerName);
                        self.formUtils.updateConnectionsAfterDefinitionElementNameChange(id);

                        var textNode = $(element).parent().find('.triggerNameNode');
                        textNode.html(triggerName);
                    }

                    var triggerCriteriaType;
                    if (selectedCriteriaType === Constants.START) {
                        triggerCriteria = Constants.START
                        triggerCriteriaType = Constants.AT
                    } else if (selectedCriteriaType === Constants.CRON_EXPRESSION) {
                        triggerCriteria = triggerCriteria;
                        triggerCriteriaType = Constants.AT;
                    } else {
                        triggerCriteria = Constants.EVERY + " " + triggerCriteria;
                        triggerCriteriaType = Constants.EVERY;
                    }
                    triggerObject.setCriteria(triggerCriteria);
                    triggerObject.setCriteriaType(triggerCriteriaType);

                    $('#' + id).removeClass('incomplete-element');
                    //Send trigger element to the backend and generate tooltip
                    var triggerToolTip = self.formUtils.getTooltip(triggerObject, Constants.TRIGGER);
                    $('#' + id).prop('title', triggerToolTip);

                    // set the isDesignViewContentChanged to true
                    self.configurationData.setIsDesignViewContentChanged(true);
                    self.designViewContainer.removeClass('disableContainer');
                    self.toggleViewButton.removeClass('disableContainer');
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

        return TriggerForm;
    });

