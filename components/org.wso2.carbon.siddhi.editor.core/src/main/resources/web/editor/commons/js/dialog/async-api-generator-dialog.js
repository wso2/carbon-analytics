/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'jquery', 'log', 'backbone', 'smart_wizard', 'jarsSelectorDialog',
        'templateAppDialog', 'templateConfigDialog', 'fillTemplateValueDialog', 'kubernetesConfigDialog',
        'dockerConfigDialog', 'alerts', 'asyncApiDefinition', 'asyncApiSourceSinkSelect'],
    function (require, $, log, Backbone, smartWizard, JarsSelectorDialog,
              TemplateAppDialog, TemplateConfigDialog, FillTemplateValueDialog, KubernetesConfigDialog,
              DockerConfigDialog, alerts, asyncApiDefinition, asyncApiSourceSinkSelectDialog) {

        var ExportDialog = Backbone.View.extend(
            /** @lends ExportDialog.prototype */
            {
                /**
                 * @augments Backbone.View
                 * @constructs
                 * @class ExportDialog
                 * @param {Object} options exportContainerModal
                 */
                initialize: function (options) {
                    this._options = options;
                    var exportDialog = _.cloneDeep(_.get(options.config, 'generate_async_api'));
                    this._exportContainer = $(_.get(exportDialog, 'selector')).clone();
                    this._baseUrl = options.config.baseUrl;
                },

                show: function () {
                    this._exportContainer.modal('show');
                },

                render: function () {
                    var self = this;
                    var options = this._options;

                    var exportContainer = this._exportContainer;
                    var heading = exportContainer.find('#initialAsyncApiDefHeading');
                    var form = exportContainer.find('#async-api-form');
                    console.log("AsyncAPIGeneratorDialog.render() - form");
                    console.log(form.length);
                    self._siddhiAppSelector = new asyncApiSourceSinkSelectDialog(options, form);
                    self._siddhiAppSelector.render();
                    this._exportContainer = exportContainer;
                },

                clear: function () {
                    if (!_.isNil(this._exportContainer)) {
                        this._exportContainer.remove();
                    }
                }
            });
        return ExportDialog;
    });
