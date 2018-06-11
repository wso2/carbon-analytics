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

        /**
         * @function generate form when defining a form
         * @param i id for the element
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        TriggerForm.prototype.generateDefineForm = function (i, formConsole, formContainer) {
            var self = this;
            var propertyDiv = $('<div id="property-header"><h3>Define Trigger </h3></div>' +
                '<div id="define-trigger" class="define-trigger"></div>');
            formContainer.append(propertyDiv);

            // generate the form to define a trigger
            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Trigger",
                    properties: {
                        annotations: {
                            propertyOrder: 1,
                            type: "array",
                            format: "table",
                            title: "Annotations",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title : "Annotation",
                                options: {
                                    disable_properties: true
                                },
                                properties: {
                                    annotation: {
                                        title : "Annotation",
                                        type: "string",
                                        minLength: 1
                                    }
                                }
                            }
                        },
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 2
                        },
                        at: {
                            type: "string",
                            title: "At",
                            minLength: 1,
                            required: true,
                            propertyOrder: 3
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

            formContainer.append('<div id="submit"><button type="button" class="btn btn-default">Submit</button></div>');

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#submit')[0];
            submitButtonElement.addEventListener('click', function () {

                var errors = editor.validate();
                if(errors.length) {
                    return;
                }
                var isTriggerNameUsed = self.formUtils.isDefinitionElementNameUsed(editor.getValue().name);
                if (isTriggerNameUsed) {
                    DesignViewUtils.prototype
                        .errorAlert("Trigger name \"" + editor.getValue().name + "\" is already used.");
                    return;
                }
                // add the new out trigger to the trigger array
                var triggerOptions = {};
                _.set(triggerOptions, 'id', i);
                _.set(triggerOptions, 'name', editor.getValue().name);
                _.set(triggerOptions, 'at', editor.getValue().at);
                var trigger = new Trigger(triggerOptions);
                _.forEach(editor.getValue().annotations, function (annotation) {
                    trigger.addAnnotation(annotation.annotation);
                });
                self.configurationData.getSiddhiAppConfig().addTrigger(trigger);

                var textNode = $('#'+i).find('.triggerNameNode');
                textNode.html(editor.getValue().name);

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);

                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');
            });
            return editor.getValue().name;
        };

        /**
         * @function generate properties form for a trigger
         * @param element selected element(trigger)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        TriggerForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var id = $(element).parent().attr('id');
            // retrieve the trigger information from the collection
            var clickedElement = self.configurationData.getSiddhiAppConfig().getTrigger(id);
            if(!clickedElement) {
                var errorMessage = 'unable to find clicked element';
                log.error(errorMessage);
                throw errorMessage;
            }
            var name = clickedElement.getName();
            var at = clickedElement.getAt();
            var savedAnnotations = clickedElement.getAnnotationList();
            var annotations = [];
            _.forEach(savedAnnotations, function (savedAnnotation) {
                annotations.push({annotation: savedAnnotation});
            });
            var fillWith = {
                annotations : annotations,
                name : name,
                at : at
            };
            fillWith = self.formUtils.cleanJSONObject(fillWith);
            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Trigger",
                    properties: {
                        annotations: {
                            propertyOrder: 1,
                            type: "array",
                            format: "table",
                            title: "Annotations",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title : "Annotation",
                                options: {
                                    disable_properties: true
                                },
                                properties: {
                                    annotation: {
                                        title : "Annotation",
                                        type: "string",
                                        minLength: 1
                                    }
                                }
                            }
                        },
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 2
                        },
                        at: {
                            type: "string",
                            title: "At",
                            minLength: 1,
                            required: true,
                            propertyOrder: 3
                        }
                    }
                },
                show_errors: "always",
                disable_properties: false,
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                display_required_only: true,
                no_additional_properties: true,
                startval: fillWith
            });
            formContainer.append(self.formUtils.buildFormButtons(true));

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#btn-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                var errors = editor.validate();
                if(errors.length) {
                    return;
                }
                var isTriggerNameUsed = self.formUtils.isDefinitionElementNameUsed(editor.getValue().name,
                    clickedElement.getId());
                if (isTriggerNameUsed) {
                    DesignViewUtils.prototype
                        .errorAlert("Trigger name \"" + editor.getValue().name + "\" is already used.");
                    return;
                }
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

                var config = editor.getValue();

                // update selected trigger model
                clickedElement.setName(config.name);
                clickedElement.setAt(config.at);

                clickedElement.clearAnnotationList();
                _.forEach(config.annotations, function (annotation) {
                    clickedElement.addAnnotation(annotation.annotation);
                });

                var textNode = $(element).parent().find('.triggerNameNode');
                textNode.html(config.name);

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

        return TriggerForm;
    });
