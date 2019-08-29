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

define(['require', 'jquery', 'log', 'backbone', 'smart_wizard', 'siddhiAppSelectorDialog', 'jarsSelectorDialog',
        'templateFileDialog', 'templateConfigDialog', 'fillTemplateValueDialog', 'kubernetesConfigDialog'],
    function (require, $, log, Backbone, smartWizard, SiddhiAppSelectorDialog, JarsSelectorDialog,
              TemplateFileDialog, TemplateConfigDialog, FillTemplateValueDialog, KubernetesConfigDialog) {

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
                    this.exportContainer;
                    this.isDocker = isDocker;
                    this.payload = {
                        templatedSiddhiApps: [],
                        configuration: '',
                        templatedVariables: [],
                        bundles: [],
                        jars: [],
                        kubernetesConfiguration: ''
                    };
                    this.appTemplatingModel;
                    this.configTemplateModel;
                    this.kubernetesConfigModel;
                    this._fill_template_value_dialog;
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
                            .append("\n" +
                                "<div id=\"step-6\" >\n" +
                                "<div class='kubernetes-configuration-step' id='kubernetes-configuration-step-id' style='display: block'>\n" +
                                "Configure Kubernetes for Siddhi\n" +
                                "</div>\n" +
                                "</div>");
                    }

                    // Toolbar extra buttons
                    var btnExportForm = $('' +
                        '<form id="submit-form"  method="post" enctype="application/x-www-form-urlencoded" target="export-download" >' +
                        '<button  type="button" class="btn btn-default hidden" id="export-btn" data-dismiss="modal" >Export</button>' +
                        '</form>');
                    btnExportForm.find('#export-btn').on('click', function () {
                        self.sendExportRequest()
                    });
                    self.btnExportForm = btnExportForm;

                    form.smartWizard({
                        selected: 0,
                        keyNavigation: false,
                        autoAdjustHeight: false,
                        theme: 'default',
                        transitionEffect: 'slideleft',
                        showStepURLhash: false,
                        contentCache: false,
                        toolbarSettings: {
                            toolbarPosition: 'bottom',
                            toolbarExtraButtons: [btnExportForm]
                        }
                    });

                    self.siddhiAppSelector = new SiddhiAppSelectorDialog(app, form);
                    self.siddhiAppSelector.render();

                    // Initialize the leaveStep event - validate before next
                    form.on("leaveStep", function (e, anchorObject, stepNumber, stepDirection) {
                        if (stepDirection === 'forward') {
                            if (stepNumber === 0) {
                                return self.siddhiAppSelector.validateSiddhiApps();
                            }
                            if (stepNumber === 1) {
                                self.payload.templatedSiddhiApps = self.appTemplatingModel.getTemplatedApps();
                            }
                            if (stepNumber === 2) {
                                self.payload.configuration = self.configTemplateModel.getTemplatedConfig();
                                self.payload.templatedSiddhiApps = self.appTemplatingModel.getTemplatedApps();
                            } else if (stepNumber === 3) {
                                self.payload.templatedVariables = self._fill_template_value_dialog.getTemplatedKeyValues();
                            }
                        }
                    });

                    // Step is passed successfully
                    form.on("showStep", function (e, anchorObject, stepNumber, stepDirection, stepPosition) {
                        // Finish button enable/disable
                        if (stepPosition === 'first') {
                            $("#prev-btn").addClass('disabled');
                        } else if (stepPosition === 'final') {
                            $("#next-btn").addClass('hidden disabled');
                            $("#export-btn").removeClass('hidden');
                        } else {
                            $("#prev-btn").removeClass('disabled');
                            $("#next-btn").removeClass('disabled');
                        }

                        if (stepDirection === 'forward') {
                            if (stepNumber === 1) {
                                var siddhiAppTemplateContainer
                                    = exportContainer.find('#siddhi-app-template-container-id');
                                if (siddhiAppTemplateContainer.children().length > 0) {
                                    siddhiAppTemplateContainer.empty();
                                    siddhiAppTemplateContainer.accordion("destroy");
                                }
                                var siddhiAppsNamesList = self.siddhiAppSelector.getSiddhiApps();
                                var templateOptions = {
                                    app: self.app,
                                    siddhiAppNames: siddhiAppsNamesList,
                                    templateHeader: siddhiAppTemplateContainer
                                };
                                self.appTemplatingModel = new TemplateFileDialog(templateOptions);
                                self.appTemplatingModel.render();
                            } else if (stepNumber === 2) {
                                self.configTemplateModel = new TemplateConfigDialog({
                                    app: self.app,
                                    templateHeader: exportContainer.find('#config-template-container-id')
                                });
                                self.configTemplateModel.render();
                            } else if (stepNumber === 4) {
                                self.jarsSelectorDialog = new JarsSelectorDialog(app, form);
                                self.jarsSelectorDialog.render();
                            } else if (stepNumber === 3) {
                                var fillTemplateOptions = {
                                    container: exportContainer.find("#fill-template-container-id"),
                                    payload: self.payload
                                };
                                self._fill_template_value_dialog = new FillTemplateValueDialog(fillTemplateOptions);
                                self._fill_template_value_dialog.render();
                            } else if (stepNumber === 5) {
                                self.kubernetesConfigModel = new KubernetesConfigDialog({
                                    app: self.app,
                                    templateHeader: exportContainer.find('#kubernetes-configuration-step-id')
                                });
                                self.kubernetesConfigModel.render();
                            }
                        }
                    });

                    this.exportContainer = exportContainer;
                },

                sendExportRequest: function () {
                    var self = this;
                    var type;
                    if (this.isDocker) {
                        type = 'docker';
                    } else {
                        type = 'kubernetes';
                        this.payload.kubernetesConfiguration = this.kubernetesConfigModel.getKubernetesConfigs();
                    }
                    this.payload.bundles = this.jarsSelectorDialog.getSelected('bundles');
                    this.payload.jars = this.jarsSelectorDialog.getSelected('jars');

                    var payload = $('<input id="payload" name="payload" type="text" style="display: none;"/>')
                        .attr('value', JSON.stringify(this.payload));

                    var exportUrl = this.app.config.baseUrl + "/export?type=" + type;

                    this.btnExportForm.append(payload);
                    this.btnExportForm.attr('action', exportUrl);

                    $(document.body).append(this.btnExportForm);
                    this.btnExportForm.submit();
                    self.exportContainer.remove();

                }
            });
        return ExportDialog;
    });
