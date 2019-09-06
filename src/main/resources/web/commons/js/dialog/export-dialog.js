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

    //todo remove backbone
        var ExportDialog = Backbone.View.extend(
            /** @lends ExportDialog.prototype */
            {
                /**
                 * @augments Backbone.View
                 * @constructs
                 * @class ExportDialog
                 * @param {Object} options exportContainerModal
                 * @param {boolean} isExportDockerFlow  is Docker File Export
                 */
                initialize: function (options, isExportDockerFlow) {
                    this.options = options;
                    var exportDialog = _.cloneDeep(_.get(options.config, 'export_dialog'));
                    this.exportContainer = $(_.get(exportDialog, 'selector')).clone();

                    this.isExportDockerFlow = isExportDockerFlow;
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
                    //todo refactor starts with _

                    var type;
                    if (isExportDockerFlow) {
                        type = 'docker';
                    } else {
                        type = 'kubernetes';
                    }
                    var exportUrl = options.config.baseUrl + "/export?type=" + type;
                    this.btnExportForm =  $('' +
                        '<form id="submit-form" method="post" enctype="application/x-www-form-urlencoded" target="export-download" >' +
                        '<button  type="button" class="btn btn-default hidden" id="export-btn" data-dismiss="modal" >Export</button>' +
                        '</form>').attr('action', exportUrl);

                },

                show: function () {
                    //todo stop form dismiss when clicked away
                    this.exportContainer.modal('show');
                },

                render: function () {
                    var self = this;
                    var isExportDockerFlow = this.isExportDockerFlow;
                    var options = this.options;

                    var exportContainer = this.exportContainer;
                    var heading = exportContainer.find('#initialHeading');
                    var form = exportContainer.find('#export-form');

                    if (isExportDockerFlow) {
                        heading.text('Export Siddhi Apps for Docker image');
                    } else {
                        heading.text('Export Siddhi Apps For Kubernetes CRD');
                        form.find('#form-steps')
                            .append('<li><a href="#step-6">Step 6<br/><small>Add Kubernetes Config</small></a></li>');

                        form.find('#form-containers')
                            .append("\n" +
                                "<div id=\"step-6\" >\n" +
                                "<div class='kubernetes-configuration-step' id='kubernetes-configuration-step-id' style='display: block'>\n" +
                                "<div class=\"step-description\">Configure Kubernetes for Siddhi</div>" +
                                "</div>\n" +
                                "</div>");
                    }

                    // Toolbar extra buttons
                    var btnExportForm = this.btnExportForm;
                    btnExportForm.find('#export-btn').on('click', function () {
                        self.sendExportRequest()
                    });

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

                    self.siddhiAppSelector = new SiddhiAppSelectorDialog(options, form);
                    self.siddhiAppSelector.render();

                    // Initialize the leaveStep event - validate before next
                    form.on("leaveStep", function (e, anchorObject, stepNumber, stepDirection) {
                        if (stepDirection === 'forward') {
                            if (stepNumber === 0) {
                                return self.siddhiAppSelector.validateSiddhiAppSelection();
                            }
                            if (stepNumber === 1) {
                                self.payload.templatedSiddhiApps = self.appTemplatingModel.getTemplatedApps();
                            }
                            if (stepNumber === 2) {
                                self.payload.configuration = self.configTemplateModel.getTemplatedConfig();
                                self.payload.templatedSiddhiApps = self.appTemplatingModel.getTemplatedApps();
                            } else if (stepNumber === 3) {
                                self.payload.templatedVariables = self._fill_template_value_dialog.
                                getTemplatedKeyValues();
                                return self._fill_template_value_dialog.
                                validateTemplatedValues(self.payload.templatedVariables)
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
                                    app: self.options,
                                    siddhiAppNames: siddhiAppsNamesList,
                                    templateContainer: siddhiAppTemplateContainer
                                };
                                self.appTemplatingModel = new TemplateFileDialog(templateOptions);
                                self.appTemplatingModel.render();
                            } else if (stepNumber === 2) {
                                var templateStep = exportContainer.find('#config-template-container-id');
                                if (templateStep.children().length > 0) {
                                    templateStep.empty();
                                }
                                self.configTemplateModel = new TemplateConfigDialog({
                                    app: self.options,
                                    templateContainer: templateStep
                                });
                                self.configTemplateModel.render();
                            } else if (stepNumber === 4) {
                                self.jarsSelectorDialog = new JarsSelectorDialog(options, form);
                                self.jarsSelectorDialog.render();
                            } else if (stepNumber === 3) {
                                var fillTemplateContainer
                                    = exportContainer.find('#fill-template-container-id');
                                if (fillTemplateContainer.children().length > 0) {
                                    fillTemplateContainer.empty();
                                }
                                var fillTemplateOptions = {
                                    container: fillTemplateContainer,
                                    payload: self.payload
                                };
                                self._fill_template_value_dialog = new FillTemplateValueDialog(fillTemplateOptions);
                                self._fill_template_value_dialog.render();
                            } else if (stepNumber === 5) {
                                self.kubernetesConfigModel = new KubernetesConfigDialog({
                                    app: self.options,
                                    templateHeader: exportContainer.find('#kubernetes-configuration-step-id')
                                });
                                self.kubernetesConfigModel.render();
                            }
                        }
                    });

                    this.exportContainer = exportContainer;
                },

                sendExportRequest: function () {
                    if (!this.isExportDockerFlow) {
                        this.payload.kubernetesConfiguration = this.kubernetesConfigModel.getKubernetesConfigs();
                    }
                    this.payload.bundles = this.jarsSelectorDialog.getSelected('bundles');
                    this.payload.jars = this.jarsSelectorDialog.getSelected('jars');

                    var payloadInputField = $('<input id="payload" name="payload" type="text" style="display: none;"/>')
                        .attr('value', JSON.stringify(this.payload));
                    this.btnExportForm.append(payloadInputField);

                    $(document.body).append(this.btnExportForm);
                    this.btnExportForm.submit();

                },

                clear: function () {
                    if (!_.isNil(this.exportContainer)) {
                        this.exportContainer.remove();
                    }
                    if (!_.isNil(this.btnExportForm)) {
                        this.btnExportForm.remove();
                    }
                }
            });
        return ExportDialog;
    });
