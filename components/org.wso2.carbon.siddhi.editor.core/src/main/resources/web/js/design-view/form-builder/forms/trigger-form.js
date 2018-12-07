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

define(['require', 'log', 'jquery', 'lodash', 'trigger', 'designViewUtils'],
    function (require, log, $, _, Trigger, DesignViewUtils) {

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

        const alphabeticValidatorRegex = /^([a-zA-Z])$/;
        const start = "start";
        const cronExpression = "cronExpression";
        const every = "every";

        /**
         * Function to render the drop down for trigger-at
         */
        var renderAt = function () {
            var atPropertyDiv = '<div class = "clearfix"> <h4> At </h4> </div>' +
                '<select id = "at-type">' +
                '<option value = "' + start + '"> start </option>' +
                '<option value = "' + cronExpression + '"> cron expression </option>' +
                '<option value = "' + every + '"> every </option>' +
                '</select>';
            $('#define-trigger-at').html(atPropertyDiv);
        };

        /**
         * Function to render a textbox according to the selected at-type
         * @param {String} selectedAtType selected at type from the select-box
         */
        var renderAtContent = function (selectedAtType) {
            if (selectedAtType === start) {
                //show no text-box
                $('#trigger-at-content').html("");
            } else {
                //render a text-box to put the atEvery or cron-expression value
                $('#trigger-at-content').html('<input type="text" class="clearfix"> ' +
                    '<label class="error-message" > </label>');
            }
        };

        /**
         * Function to determine the at-type
         * @param {String} at at value
         * @param {String} atOrAtEvery at or atEvery
         * @return {String} selectedAtType
         */
        var determineAt = function (at, atOrAtEvery) {
            var selectedAtType;
            if (atOrAtEvery === "at") {
                //check if at-type is start
                if (at.toLowerCase() === start) {
                    selectedAtType = start;
                    renderAtContent(selectedAtType);
                    return selectedAtType;
                } else {
                    //cron expression
                    selectedAtType = cronExpression;
                    renderAtContent(selectedAtType);
                    return selectedAtType;
                }
            } else {
                //atEvery
                selectedAtType = every;
                renderAtContent(selectedAtType);
                return selectedAtType;
            }
        };

        /**
         * @function generate properties form for a trigger
         * @param element selected element(trigger)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        TriggerForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var propertyDiv = $('<div id="property-header"><h3>Trigger Configuration</h3></div>' +
                '<div class ="trigger-form-container"> <div id="define-trigger-name"> <h4>Name: </h4>' +
                '<input type="text" id="triggerName" class="clearfix"> <label class="error-message" > </label> </div>' +
                '<button id="btn-submit" type="button" class="btn toggle-view-button"> Submit </button> </div>' +
                '<div class = "trigger-form-container"> <div id= "define-trigger-at"> </div>' +
                '<div id = "trigger-at-content" ></div> </div>');
            formContainer.append(propertyDiv);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var id = $(element).parent().attr('id');
            $('#' + id).addClass('selected-element');
            $(".overlayed-container").fadeTo(200, 1);

            // retrieve the trigger information from the collection
            var clickedElement = self.configurationData.getSiddhiAppConfig().getTrigger(id);
            var name = clickedElement.getName();
            renderAt();

            //if name is defined
            if (name) {
                $('#triggerName').val(name);

                var atOrAtEvery = clickedElement.getAtOrAtEvery().trim();
                var at = clickedElement.getAt().trim();
                if (atOrAtEvery === "at") {
                    if (at.indexOf("'") >= 0 || at.indexOf('"') >= 0) {
                        //to remove the string quote from the start and cron expression
                        at = at.slice(1, at.length - 1);
                    }
                } else {
                    //remove every from atEvery's value
                    var replaceEvery = at;
                    at = replaceEvery.replace("every", '');
                }
                at = at.trim();
                var selectedAtType = determineAt(at, atOrAtEvery);
                $('#define-trigger-at').find('#at-type option').filter(function () {
                    return ($(this).val().toLowerCase() == (selectedAtType.toLowerCase()));
                }).prop('selected', true);

                $('#trigger-at-content input[type="text"]').val(at);
            }

            //onchange of the at-type selection
            $('#at-type').change(function () {
                renderAtContent(this.value);
                if (at && this.value.toLowerCase() === selectedAtType.toLowerCase()) {
                    if (this.value !== "start") {
                        $('#trigger-at-content input[type="text"]').val(at);
                    }
                }
            });

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#btn-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                //clear the error classes
                $('.error-message').text("");
                $('.required-input-field').removeClass('required-input-field');

                var triggerName = $('#triggerName').val().trim();

                // to check if trigger name is empty
                if (triggerName == "") {
                    $('#triggerName').addClass('required-input-field');
                    $('#triggerName')[0].scrollIntoView();
                    $('#define-trigger-name').find('.error-message').text("Trigger name is required.");
                    return;
                }
                var previouslySavedName = clickedElement.getName();
                if (!previouslySavedName) {
                    previouslySavedName = "";
                }
                // update connection related to the element if the name is changed
                if (previouslySavedName !== triggerName) {

                    //check if name is already used
                    var isTriggerNameUsed = self.formUtils.isDefinitionElementNameUsed(triggerName,
                        clickedElement.getId());
                    if (isTriggerNameUsed) {
                        $('#triggerName').addClass('required-input-field');
                        $('#triggerName')[0].scrollIntoView();
                        $('#define-trigger-name').find('.error-message').text("Trigger name is already used.");
                        return;
                    }
                    //to check if trigger name contains white spaces
                    if (triggerName.indexOf(' ') >= 0) {
                        $('#triggerName').addClass('required-input-field');
                        $('#triggerName')[0].scrollIntoView();
                        $('#define-trigger-name').find('.error-message').text("Trigger name cannot have white space.");
                        return;
                    }
                    //to check if trigger name starts with an alphabetic character
                    if (!(alphabeticValidatorRegex).test(triggerName.charAt(0))) {
                        $('#triggerName').addClass('required-input-field');
                        $('#triggerName')[0].scrollIntoView();
                        $('#define-trigger-name').find('.error-message').text("Trigger name must start with an " +
                            "alphabetic character.");
                        return;
                    }
                    // update selected trigger model
                    clickedElement.setName(triggerName);
                    self.formUtils.updateConnectionsAfterDefinitionElementNameChange(id);
                }

                var selectedAtType = $('#at-type').val();
                var at;
                var atOrAtEvery;
                if (selectedAtType !== start) {
                    at = $('#trigger-at-content input[type="text"]').val().trim();
                    if (at === "") {
                        $('#trigger-at-content').find('.error-message').text("Value is required");
                        return;
                    }
                }
                if (selectedAtType === start) {
                    at = start
                    atOrAtEvery = "at"
                } else if (selectedAtType === cronExpression) {
                    at = at;
                    atOrAtEvery = "at";
                } else {
                    at = "every " + at;
                    atOrAtEvery = "atEvery";
                }

                clickedElement.setAt(at);
                clickedElement.setAtOrAtEvery(atOrAtEvery);

                var textNode = $(element).parent().find('.triggerNameNode');
                textNode.html(triggerName);

                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');
                $('#' + id).removeClass('incomplete-element');
                $('#' + id).prop('title', '');

                // set the isDesignViewContentChanged to true
                self.configurationData.setIsDesignViewContentChanged(true);

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            });
        };

        return TriggerForm;
    });
