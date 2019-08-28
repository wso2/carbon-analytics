/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['require', 'jquery', 'log', 'backbone', 'smart_wizard', 'siddhiAppSelectorDialog', 'jarsSelectorDialog', 'templateFileDialog'],
    function (require, $, log, Backbone, smartWizard, SiddhiAppSelectorDialog, JarsSelectorDialog, TemplateFileDialog) {

        var ExportDialog = Backbone.View.extend(
            /** @lends ExportDialog.prototype */
            {
                /**
                 * @augments Backbone.View
                 * @constructs
                 * @class ExportDialog
                 * @param {Object} options exportContainerModal
                 * @param {boolean} isDocker  is Docker File Export
                 */
                initialize: function (options, isDocker) {
                    this.app = options;
                    this.options = _.cloneDeep(_.get(options.config, 'export_dialog'));
                    this.isDocker = isDocker;
                    this.payload = {
                        siddhiApps: {},
                        configuration: '',
                        bundles: [],
                        jars: [],
                        kubernetesConfiguration: ''
                    };
                },

                show: function () {
                    this.exportContainer.modal('show');
                },

                render: function () {

                    var self = this;
                    if (!_.isNil(this.exportContainer)) {
                        this.exportContainer.remove();
                    }

                    var isDocker = this.isDocker;
                    var options = this.options;
                    var app = this.app;
                    var exportContainer = $(_.get(options, 'selector')).clone();
                    var heading = exportContainer.find('#initialHeading');
                    var form = exportContainer.find('#export-form');

                    if (isDocker) {
                        heading.text('Export Siddhi Apps for Docker image');
                    } else {
                        heading.text('Export Siddhi Apps For Kubernetes CRD');
                        form.find('#form-steps')
                            .append('<li><a href="#step-6">Step 6<br/><small>Add Kubernetes Config</small></a></li>');

                        form.find('#form-containers')
                            .append('<div id="step-6" >' +
                                    '    <label>Kubernetes Config</label>' +
                                    '</div>');
                    }

                    // Toolbar extra buttons
                    var btnExport = $('<button type="button" class="btn btn-default" data-dismiss="modal" id="finish-btn">Export</button>')
                                        .addClass('hidden')
                                        .on('click', function () {self.sendExportRequest()});
                    form.smartWizard({
                        selected: 0,
                        autoAdjustHeight: false,
                        theme: 'none',
                        transitionEffect: 'fade',
                        showStepURLhash: false,
                        contentCache: false,
                        toolbarSettings: {
                            toolbarPosition: 'bottom',
                            toolbarExtraButtons: [btnExport]
                        }
                    });

                    self.siddhiAppSelector = new SiddhiAppSelectorDialog(app, form);
                    self.siddhiAppSelector.render();

                    // Initialize the leaveStep event - validate before next
                    form.on("leaveStep", function(e, anchorObject, stepNumber, stepDirection) {
                        if (stepDirection === 'forward') {
                            if (stepNumber === 0) {
                                return self.siddhiAppSelector.validateSiddhiApps();
                            }
                        }
                    });

                    // Step is passed successfully
                    form.on("showStep", function(e, anchorObject, stepNumber, stepDirection, stepPosition) {
                        // Finish button enable/disable
                        if (stepPosition === 'first') {
                            $("#prev-btn").addClass('disabled');
                        } else if (stepPosition === 'final') {
                            $("#next-btn").addClass('hidden disabled');
                            $("#finish-btn").removeClass('hidden disabled');
                        } else {
                            $("#prev-btn").removeClass('disabled');
                            $("#next-btn").removeClass('disabled');
                        }

                        if (stepDirection === 'forward') {
                            if (stepNumber === 1) {
                                var siddhiAppsNamesList = self.siddhiAppSelector.getSiddhiApps();
                                log.info(siddhiAppsNamesList);
                                self._template_dialog = new TemplateFileDialog(app);
                            } else if (stepNumber === 4) {
                                self.jarsSelectorDialog = new JarsSelectorDialog(app, form);
                                self.jarsSelectorDialog.render();
                            }
                        }
                    });

                    this.exportContainer = exportContainer;
                },

                sendExportRequest: function () {
                    this.payload.bundles = this.jarsSelectorDialog.getSelected('bundles');
                    this.payload.jars = this.jarsSelectorDialog.getSelected('jars');

                    log.info(this.payload);

                }
            });
        return ExportDialog;
    });
