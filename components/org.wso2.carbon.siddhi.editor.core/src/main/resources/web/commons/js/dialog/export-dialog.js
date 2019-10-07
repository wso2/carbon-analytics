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
        'templateAppDialog', 'templateConfigDialog', 'fillTemplateValueDialog', 'kubernetesConfigDialog'],
    function (require, $, log, Backbone, smartWizard, SiddhiAppSelectorDialog, JarsSelectorDialog,
              TemplateAppDialog, TemplateConfigDialog, FillTemplateValueDialog, KubernetesConfigDialog) {

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
                    this._options = options;
                    var exportDialog = _.cloneDeep(_.get(options.config, 'export_dialog'));
                    this._exportContainer = $(_.get(exportDialog, 'selector')).clone();

                    this._isExportDockerFlow = isExportDockerFlow;
                    this._payload = {
                        templatedSiddhiApps: [],
                        configuration: '',
                        templatedVariables: [],
                        bundles: [],
                        jars: [],
                        kubernetesConfiguration: ''
                    };
                    this._siddhiAppSelector;
                    this._jarsSelectorDialog;
                    this._appTemplatingModel;
                    this._configTemplateModel;
                    this._kubernetesConfigModel;
                    this._fill_template_value_dialog;

                    var type;
                    if (isExportDockerFlow) {
                        type = 'docker';
                    } else {
                        type = 'kubernetes';
                    }
                    var exportUrl = options.config.baseUrl + "/export?type=" + type;
                    this._btnExportForm =  $('' +
                        '<form id="submit-form" method="post" enctype="application/x-www-form-urlencoded" target="export-download" >' +
                        '<button  type="button" class="btn btn-primary hidden" id="export-btn" data-dismiss="modal" >Export</button>' +
                        '</form>').attr('action', exportUrl);

                },

                show: function () {
                    this._exportContainer.modal('show');
                },

                render: function () {
                    var self = this;
                    var isExportDockerFlow = this._isExportDockerFlow;
                    var options = this._options;

                    var exportContainer = this._exportContainer;
                    var heading = exportContainer.find('#initialHeading');
                    var form = exportContainer.find('#export-form');

                    if (isExportDockerFlow) {
                        heading.text('Export Siddhi Apps for Docker image');
                        var formSteps = form.find('#form-steps');
                        formSteps.addClass('docker_export_header_item');
                        formSteps.removeClass('k8_export_header_item');
                    } else {
                        heading.text('Export Siddhi Apps For Kubernetes CRD');
                        var formSteps = form.find('#form-steps');
                        formSteps.removeClass('docker_export_header_item');
                        formSteps.addClass('k8_export_header_item');
                        for (i = 0; i < formSteps.children().length; i++) {
                            formSteps.children()[i].setAttribute("style", "max-width: 16%;")
                        }
                        formSteps.append('<li style="max-width: 14.28%;"><a href="#step-7" ' +
                            'class="link-disabled">Step 7<br/><small>Add Kubernetes Config</small>' +
                            '</a></li>');

                        form.find('#form-containers')
                            .append("\n" +
                                "<div id=\"step-6\" >\n" +
                                "<div class='kubernetes-configuration-step' id='kubernetes-configuration-step-id' style='display: block'>\n" +
                                "<div class=\"step-description\">Configure Kubernetes for Siddhi</div>" +
                                "</div>\n" +
                                "</div>");
                    }

                    // Toolbar extra buttons
                    var btnExportForm = this._btnExportForm;
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

                    self._siddhiAppSelector = new SiddhiAppSelectorDialog(options, form);
                    self._siddhiAppSelector.render();

                    // Initialize the leaveStep event - validate before next
                    form.on("leaveStep", function (e, anchorObject, stepNumber, stepDirection) {
                        if (stepDirection === 'forward') {
                            if (stepNumber === 0) {
                                return self._siddhiAppSelector.validateSiddhiAppSelection();
                            }
                            if (stepNumber === 1) {
                                self._payload.templatedSiddhiApps = self._appTemplatingModel.getTemplatedApps();
                            }
                            if (stepNumber === 2) {
                                self._payload.configuration = self._configTemplateModel.getTemplatedConfig();
                                self._payload.templatedSiddhiApps = self._appTemplatingModel.getTemplatedApps();
                            } else if (stepNumber === 3) {
                                self._payload.templatedVariables = self._fill_template_value_dialog.
                                getTemplatedKeyValues();
                                return self._fill_template_value_dialog.
                                validateTemplatedValues(self._payload.templatedVariables)
                            }
                        }
                    });

                    // Step is passed successfully
                    form.on("showStep", function (e, anchorObject, stepNumber, stepDirection, stepPosition) {
                        // Finish button enable/disable
                        if (stepPosition === 'first') {
                            $(".sw-btn-prev").addClass('disabled');
                            $(".sw-btn-prev").addClass('hidden');
                            $(".sw-btn-prev").parent().removeClass("sw-btn-group-final");
                        } else if (stepPosition === 'final') {
                            $(".sw-btn-next").addClass('hidden disabled');
                            $(".sw-btn-next").parent().addClass("sw-btn-group-final");
                            $("#export-btn").removeClass('hidden');
                        } else {
                            $(".sw-btn-next").removeClass('disabled');
                            $(".sw-btn-next").removeClass('hidden');
                            $(".sw-btn-prev").removeClass('disabled');
                            $(".sw-btn-prev").removeClass('hidden');
                            $(".sw-btn-prev").parent().removeClass("sw-btn-group-final");
                            $("#export-btn").addClass('hidden');
                        }

                        if (stepDirection === 'forward') {
                            if (stepNumber === 1) {
                                var siddhiAppTemplateContainer
                                    = exportContainer.find('#siddhi-app-template-container-id');
                                if (siddhiAppTemplateContainer.children().length > 0) {
                                    siddhiAppTemplateContainer.empty();
                                    siddhiAppTemplateContainer.accordion("destroy");
                                }
                                var siddhiAppsNamesList = self._siddhiAppSelector.getSiddhiApps();
                                var templateOptions = {
                                    app: self._options,
                                    siddhiAppNames: siddhiAppsNamesList,
                                    templateContainer: siddhiAppTemplateContainer
                                };
                                self._appTemplatingModel = new TemplateAppDialog(templateOptions);
                                self._appTemplatingModel.render();
                            } else if (stepNumber === 2) {
                                var templateStep = exportContainer.find('#config-template-container-id');
                                if (templateStep.children().length > 0) {
                                    templateStep.empty();
                                }
                                self._configTemplateModel = new TemplateConfigDialog({
                                    app: self._options,
                                    templateContainer: templateStep
                                });
                                self._configTemplateModel.render();
                            } else if (stepNumber === 4) {
                                self._jarsSelectorDialog = new JarsSelectorDialog(options, form);
                                self._jarsSelectorDialog.render();
                            } else if (stepNumber === 3) {
                                var fillTemplateContainer
                                    = exportContainer.find('#fill-template-container-id');
                                if (fillTemplateContainer.children().length > 0) {
                                    fillTemplateContainer.empty();
                                }
                                var fillTemplateOptions = {
                                    container: fillTemplateContainer,
                                    payload: self._payload
                                };
                                self._fill_template_value_dialog = new FillTemplateValueDialog(fillTemplateOptions);
                                self._fill_template_value_dialog.render();
                            } else if (stepNumber === 5) {
                                self._kubernetesConfigModel = new KubernetesConfigDialog({
                                    app: self._options,
                                    templateHeader: exportContainer.find('#kubernetes-configuration-step-id')
                                });
                                self._kubernetesConfigModel.render();
                            }
                        }
                    });

                    this._exportContainer = exportContainer;
                },

                sendExportRequest: function () {
                    if (!this._isExportDockerFlow) {
                        this._payload.kubernetesConfiguration = this._kubernetesConfigModel.getKubernetesConfigs();
                    }
                    this._payload.bundles = this._jarsSelectorDialog.getSelected('bundles');
                    this._payload.jars = this._jarsSelectorDialog.getSelected('jars');

                    var payloadInputField = $('<input id="payload" name="payload" type="text" style="display: none;"/>')
                        .attr('value', JSON.stringify(this._payload));
                    this._btnExportForm.append(payloadInputField);

                    $(document.body).append(this._btnExportForm);
                    this._btnExportForm.submit();
                },

                clear: function () {
                    if (!_.isNil(this._exportContainer)) {
                        this._exportContainer.remove();
                    }
                    if (!_.isNil(this._btnExportForm)) {
                        this._btnExportForm.remove();
                    }
                }
            });
        return ExportDialog;
    });
