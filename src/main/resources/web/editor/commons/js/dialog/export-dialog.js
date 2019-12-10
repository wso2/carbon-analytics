/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'jquery', 'log', 'backbone', 'smart_wizard', 'siddhiAppSelectorDialog', 'jarsSelectorDialog',
        'templateAppDialog', 'templateConfigDialog', 'fillTemplateValueDialog', 'kubernetesConfigDialog',
        'dockerConfigDialog', 'alerts', 'dockerImageTypeDialog'],
    function (require, $, log, Backbone, smartWizard, SiddhiAppSelectorDialog, JarsSelectorDialog,
              TemplateAppDialog, TemplateConfigDialog, FillTemplateValueDialog, KubernetesConfigDialog,
              DockerConfigDialog, alerts, DockerImageTypeDialog) {

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

                    var exportKubeStep5 = _.cloneDeep(_.get(options.config, 'export_k8s_path_step_5'));
                    var exportKubeStep6 = _.cloneDeep(_.get(options.config, 'export_k8s_path_step_6'));
                    var exportKubeStep8 = _.cloneDeep(_.get(options.config, 'export_k8s_path_step_8'));
                    var exportDockerStep5 = _.cloneDeep(_.get(options.config, 'export_docker_path_step_5'));
                    var exportConfigCommonStep = _.cloneDeep(_.get(options.config, 'export_docker_config_common_step'));
                    this._exportKubeStep5Container = $(_.get(exportKubeStep5, 'selector')).clone();
                    this._exportKubeStep6Container = $(_.get(exportKubeStep6, 'selector')).clone();
                    this._exportKubeStep7Container = $(_.get(exportConfigCommonStep, 'selector')).clone();
                    this._exportKubeStep8Container = $(_.get(exportKubeStep8, 'selector')).clone();
                    this._exportDockerStep5Container = $(_.get(exportDockerStep5, 'selector')).clone();
                    this._exportDockerStep6Container = $(_.get(exportConfigCommonStep, 'selector')).clone();

                    this._isExportDockerFlow = isExportDockerFlow;
                    this._payload = {
                        templatedSiddhiApps: [],
                        configuration: '',
                        templatedVariables: [],
                        bundles: [],
                        jars: [],
                        kubernetesConfiguration: '',
                        dockerConfiguration: ''
                    };
                    this._siddhiAppSelector;
                    this._jarsSelectorDialog;
                    this._appTemplatingModel;
                    this._configTemplateModel;
                    this._kubernetesConfigModel;
                    this._fill_template_value_dialog;
                    this._dockerConfigModel;
                    this._dockerImageTypeModel;
                    this._exportUrl;
                    this._exportType;
                    this._baseUrl;
                    this._isSkippedTemplateAppsStep = false;
                    this._isSkippedDockerConfigSteps = false;

                    if (isExportDockerFlow) {
                        this._exportType = 'docker';
                    } else {
                        this._exportType = 'kubernetes';
                    }
                    this._baseUrl = options.config.baseUrl;
                    this._exportUrl = options.config.baseUrl + "/export?exportType=" + this._exportType;
                    this._btnExportForm =  $('' +
                        '<form id="submit-form" method="post" enctype="application/x-www-form-urlencoded" target="export-download" >' +
                        '<button type="button" class="btn btn-primary hidden" id="export-btn" >Export</button>' +
                        '</form>');

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
                        var dockerStepWidth = 100.0/6;
                        var formSteps = form.find('#form-steps');
                        for (i = 0; i < formSteps.children().length; i++) {
                            formSteps.children()[i].setAttribute("style", "max-width: " + dockerStepWidth.toString() + "%;")
                        }
                        formSteps.append('<li style="max-width: ' + dockerStepWidth.toString()  + '%;"><a href="#docker-path-step-5" ' +
                            'class="link-disabled">Step 5<br/><small>Configure Custom Docker Image</small>' +
                            '</a></li>');
                        formSteps.append('<li style="max-width: ' + dockerStepWidth.toString()  + '%;"><a href="#docker-config-common-step" ' +
                            'class="link-disabled">Step 6<br/><small>Export Custom Docker Image</small>' +
                            '</a></li>');
                        form.find('#form-containers').append(this._exportDockerStep5Container);
                        form.find('#form-containers').append(this._exportDockerStep6Container);
                    } else {
                        heading.text('Export Siddhi Apps For Kubernetes CRD');
                        var k8sStepWidth = 100.0/8;
                        var formSteps = form.find('#form-steps');
                        for (i = 0; i < formSteps.children().length; i++) {
                            formSteps.children()[i].setAttribute("style", "max-width: " + k8sStepWidth.toString() + "%;")
                        }
                        formSteps.append('<li style="max-width: ' + k8sStepWidth.toString()  + '%;"><a href="#k8s-path-step-5" ' +
                            'class="link-disabled">Step 5<br/><small>Select Docker Image</small>' +
                            '</a></li>');
                        formSteps.append('<li style="max-width: ' + k8sStepWidth.toString()  + '%;"><a href="#k8s-path-step-6" ' +
                            'class="link-disabled">Step 6<br/><small>Configure Custom Docker Image</small>' +
                            '</a></li>');
                        formSteps.append('<li style="max-width: ' + k8sStepWidth.toString()  + '%;"><a href="#docker-config-common-step" ' +
                            'class="link-disabled">Step 7<br/><small>Export Custom Docker Image</small>' +
                            '</a></li>');
                        formSteps.append('<li style="max-width: ' + k8sStepWidth.toString()  + '%;"><a href="#k8s-path-step-8" ' +
                            'class="link-disabled">Step 8<br/><small>Add Deployment Configurations</small>' +
                            '</a></li>');
                        form.find('#form-containers').append(this._exportKubeStep5Container);
                        form.find('#form-containers').append(this._exportKubeStep6Container);
                        form.find('#form-containers').append(this._exportKubeStep7Container);
                        form.find('#form-containers').append(this._exportKubeStep8Container);
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

                    self._siddhiAppSelector = new SiddhiAppSelectorDialog(options, form, self._exportType);
                    self._siddhiAppSelector.render();

                    // Initialize the leaveStep event - validate before next
                    form.on("leaveStep", function (e, anchorObject, stepNumber, stepDirection) {
                        if (stepDirection === 'forward') {
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
                            } else if (stepNumber === 4 && self._exportType === 'kubernetes') {
                                return self._dockerImageTypeModel.validateDockerTypeConfig();
                            } else if (stepNumber === 6 && self._exportType === 'kubernetes') {
                                return self._dockerConfigModel.validateDockerConfig();
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
                                if (siddhiAppsNamesList.length == 0) {
                                    e.preventDefault();
                                    self._isSkippedTemplateAppsStep = true;
                                    form.smartWizard("goToStep", 2);
                                } else {
                                    self._isSkippedTemplateAppsStep = false;
                                    self._appTemplatingModel.render();
                                }
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
                                if (self._exportType === 'kubernetes') {
                                    self._dockerImageTypeModel = new DockerImageTypeDialog({
                                        templateHeader: exportContainer.find('#docker-image-type-container-id'),
                                        baseUrl: self._baseUrl,
                                    });
                                    self._dockerImageTypeModel.render();
                                } else {
                                    self._jarsSelectorDialog = new JarsSelectorDialog(options, form);
                                    self._jarsSelectorDialog.render();
                                }
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
                                if (self._exportType === 'kubernetes') {
                                    if (self._dockerImageTypeModel.getDockerTypeConfigs()['isExistingImage']) {
                                        e.preventDefault();
                                        self._isSkippedDockerConfigSteps = true;
                                        form.smartWizard("goToStep", 7);
                                    } else {
                                        self._isSkippedDockerConfigSteps = false;
                                        self._jarsSelectorDialog = new JarsSelectorDialog(options, form);
                                        self._jarsSelectorDialog.render();
                                    }
                                } else {
                                    var dockerImageName = "";
                                    if (self._siddhiAppSelector.getSiddhiProcessName() != "sample-siddhi-process") {
                                        dockerImageName = self._siddhiAppSelector.getSiddhiProcessName();
                                    }
                                    self._dockerConfigModel = new DockerConfigDialog({
                                        app: self._options,
                                        templateHeader: exportContainer.find('#docker-config-container-id'),
                                        exportType: self._exportType,
                                        payload: self._payload,
                                        dockerImageName: dockerImageName
                                    });
                                    self._dockerConfigModel.render();
                                }
                            } else if (stepNumber === 6 && self._exportType === 'kubernetes') {
                                self._dockerConfigModel = new DockerConfigDialog({
                                    app: self._options,
                                    templateHeader: exportContainer.find('#docker-config-container-id'),
                                    exportType:self._exportType,
                                    payload: self._payload,
                                    dockerImageName: self._siddhiAppSelector.getSiddhiProcessName()
                                });
                                self._dockerConfigModel.render();
                            } else if (stepNumber === 7 && self._exportType === 'kubernetes') {
                                self._kubernetesConfigModel = new KubernetesConfigDialog({
                                   app: self._options,
                                   templateHeader: exportContainer.find('#kubernetes-configuration-step-id'),
                                   siddhiProcessName: self._siddhiAppSelector.getSiddhiProcessName()
                                });
                                self._kubernetesConfigModel.render();
                            }
                        }

                        if (stepDirection === 'backward') {
                            if (stepNumber === 1 && self._isSkippedTemplateAppsStep) {
                                e.preventDefault();
                                form.smartWizard("goToStep", 0);
                            }
                            if (stepNumber === 6 && self._isSkippedDockerConfigSteps) {
                                e.preventDefault();
                                form.smartWizard("goToStep", 4);
                            }
                        }
                    });

                    this._exportContainer = exportContainer;
                },

                sendExportRequest: function () {
                    if (!this._isExportDockerFlow) {
                        this._payload.kubernetesConfiguration = this._kubernetesConfigModel.getConfigs()["kubernetesConfig"];
                        this._payload.configuration += this._kubernetesConfigModel.getConfigs()["statePersistenceConfig"];
                    }
                    if (this._dockerImageTypeModel != undefined && !this._dockerImageTypeModel.getDockerTypeConfigs()["pushDocker"]) {
                        this._payload.dockerConfiguration = this._dockerImageTypeModel.getDockerTypeConfigs();
                    } else {
                        this._payload.dockerConfiguration = this._dockerConfigModel.getDockerConfigs();
                    }

                    if (typeof this._jarsSelectorDialog == "undefined") {
                        this._payload.bundles = []
                        this._payload.jars = []
                    } else {
                        this._payload.bundles = this._jarsSelectorDialog.getSelected('bundles');
                        this._payload.jars = this._jarsSelectorDialog.getSelected('jars');
                    }

                    var payloadInputField = $('<input id="payload" name="payload" type="text" style="display: none;"/>')
                        .attr('value', JSON.stringify(this._payload));

                    var exportUrl = this._exportUrl
                    var requestType = "downloadOnly"

                    if (this._exportType == "docker") {
                        if (!this._dockerConfigModel.validateDockerConfig()) {
                           return;
                        }
                        if (this._payload.dockerConfiguration.pushDocker && this._payload.dockerConfiguration.downloadDocker) {
                            requestType = "downloadAndBuild";
                        } else if (this._payload.dockerConfiguration.pushDocker) {
                            this._btnExportForm.append(payloadInputField);
                            $(document.body).append(this._btnExportForm);
                            requestType = "buildOnly";
                            exportUrl = exportUrl + "&requestType=" + requestType;
                            $.ajax({
                                type: "POST",
                                url: exportUrl,
                                headers: {
                                    "Content-Type": "application/x-www-form-urlencoded"
                                 },
                                data: {"payload": JSON.stringify(this._payload)},
                                async: false,
                                success: function (response) {
                                    alerts.info("Docker image push process is in-progress. " +
                                        "Please check editor console for the progress.");
                                },
                                error: function (error) {
                                    alerts.error("Docker image push process failed, " +
                                        "Please check editor console for further information.");
                                }
                            });
                            this._exportContainer.modal('hide');
                            return;
                        } else if (this._payload.dockerConfiguration.downloadDocker) {
                            requestType = "downloadOnly";
                        }
                    } else if (this._exportType == "kubernetes") {
                        if (this._payload.dockerConfiguration.pushDocker) {
                            requestType = "downloadAndBuild";
                        } else {
                            requestType = "downloadOnly";
                        }
                    }
                    this._btnExportForm.append(payloadInputField);
                    $(document.body).append(this._btnExportForm);
                    exportUrl = exportUrl + "&requestType=" + requestType;
                    this._btnExportForm = this._btnExportForm.attr('action', exportUrl)
                    this._btnExportForm.submit();
                    this._exportContainer.modal('hide');

                    if(this._payload.dockerConfiguration.pushDocker) {
                        alerts.info("Please see the editor console for the progress of docker push.");
                    }

                },

                clear: function () {
                    if (!_.isNil(this._exportContainer)) {
                        this._exportContainer.remove();
                    }
                    if (!_.isNil(this._btnExportForm)) {
                        this._btnExportForm.remove();
                    }
                    if (!_.isNil(this._exportKubeStepContainer)) {
                        this._exportKubeStepContainer.remove();
                    }
                }
            });
        return ExportDialog;
    });
