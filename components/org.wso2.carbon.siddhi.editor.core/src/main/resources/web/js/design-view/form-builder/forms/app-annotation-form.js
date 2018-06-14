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

define(['require', 'log', 'jquery', 'lodash'],
    function (require, log, $, _) {

        /**
         * @class AppAnnotationForm Creates a forms to collect app level annotations and siddhi app name
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var AppAnnotationForm = function (options) {
            if (options !== undefined) {
                this.configurationData = options.configurationData;
                this.application = options.application;
                this.consoleListManager = options.application.outputController;
                this.formUtils = options.formUtils;
                var currentTabId = this.application.tabController.activeTab.cid;
                this.designViewContainer = $('#design-container-' + currentTabId);
                this.toggleViewButton = $('#toggle-view-button-' + currentTabId);
            }
        };

        /**
         * @function generate form for Partition
         * @param element selected element(partition)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        AppAnnotationForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            // design view container and toggle view button are enabled
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var siddhiAppConfig = self.configurationData.getSiddhiAppConfig();
            var siddhiAppName = siddhiAppConfig.getSiddhiAppName();
            var savedAppAnnotations = siddhiAppConfig.getAppAnnotationList();
            var annotations = [];
            _.forEach(savedAppAnnotations, function (savedAnnotation) {
                annotations.push({annotation: savedAnnotation});
            });

            var fillWith = {
                siddhiApp: {
                    name: siddhiAppName
                },
                annotations: annotations
            };
            fillWith = self.formUtils.cleanJSONObject(fillWith);
            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Siddhi App Configurations",
                    options: {
                        disable_properties: false
                    },
                    properties: {
                        siddhiApp: {
                            propertyOrder: 1,
                            type: "object",
                            title: "Siddhi App",
                            required: true,
                            properties: {
                                name: {
                                    required: true,
                                    title: "Name",
                                    type: "string",
                                    minLength: 1
                                }
                            }
                        },
                        annotations: {
                            propertyOrder: 2,
                            type: "array",
                            format: "table",
                            title: "App Annotations",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title: "App Annotation",
                                properties: {
                                    annotation: {
                                        title: "App Annotation",
                                        type: "string",
                                        minLength: 1
                                    }
                                }
                            }
                        }
                    }
                },
                startval: fillWith,
                show_errors: "always",
                disable_properties: true,
                display_required_only: true,
                no_additional_properties: true,
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                disable_array_reorder: true
            });

            formContainer.append(self.formUtils.buildFormButtons(true));

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#btn-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                var config = editor.getValue();

                siddhiAppConfig.setSiddhiAppName(config.siddhiApp.name);
                siddhiAppConfig.clearAppAnnotationList();
                _.forEach(config.annotations, function (annotation) {
                    siddhiAppConfig.addAppAnnotation(annotation.annotation);
                });

                // design view container and toggle view button are enabled
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

        return AppAnnotationForm;
    });
