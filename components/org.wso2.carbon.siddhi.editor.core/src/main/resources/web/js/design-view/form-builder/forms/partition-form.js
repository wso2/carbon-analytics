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
         * @class PartitionForm Creates a forms to collect data from a partition
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var PartitionForm = function (options) {
            if (options !== undefined) {
                this.configurationData = options.configurationData;
                this.application = options.application;
                this.consoleListManager = options.application.outputController;
                this.jsPlumbInstance = options.self.jsPlumbInstance;
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
        PartitionForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            // design view container and toggle view button are enabled
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var id = $(element.target).parent().attr('id');
            var partition = self.configurationData.getSiddhiAppConfig().getPartition(id);
            var connected= true;

            if(!(connected)){
                alert('Connect a stream for partitioning');
                // design view container and toggle view button are enabled
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');
            } else {
                var fillWith= {};
                var editor = new JSONEditor(formContainer[0], {
                    schema: {
                        type: "object",
                        title: "Stream",
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
                            partitionKeys: {
                                required: true,
                                propertyOrder: 2,
                                type: "array",
                                format: "table",
                                title: "Partition Keys",
                                uniqueItems: true,
                                minItems: 1,
                                items: {
                                    type: "object",
                                    title : 'Partition Key',
                                    options: {
                                        disable_properties: true
                                    },
                                    properties: {
                                        expression: {
                                            title : 'Expression',
                                            type: "string",
                                            minLength: 1,
                                            required: true
                                        },
                                        streamName: {
                                            title : 'Stream Name',
                                            type: "string",
                                            minLength: 1,
                                            required: true
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
                formContainer.append('<div id="form-submit"><button type="button" ' +
                    'class="btn btn-default">Submit</button></div>' +
                    '<div id="form-cancel"><button type="button" class="btn btn-default">Cancel</button></div>');

                // 'Submit' button action
                var submitButtonElement = $(formContainer).find('#form-submit')[0];
                submitButtonElement.addEventListener('click', function () {

                    var config = editor.getValue();

                    // design view container and toggle view button are enabled
                    self.designViewContainer.removeClass('disableContainer');
                    self.toggleViewButton.removeClass('disableContainer');

                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                });
                // 'Cancel' button action
                var cancelButtonElement = $(formContainer).find('#form-cancel')[0];
                cancelButtonElement.addEventListener('click', function () {
                    self.designViewContainer.removeClass('disableContainer');
                    self.toggleViewButton.removeClass('disableContainer');

                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                });
            }
        };

        return PartitionForm;
    });
