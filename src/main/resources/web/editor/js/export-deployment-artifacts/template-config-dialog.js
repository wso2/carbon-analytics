/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash', 'jquery', 'log', 'ace/ace', 'app/source-editor/editor', 'alerts', 'templateConfigBlocks'],
    function (require, _, $, log, ace, SiddhiEditor, alerts, TemplateConfigBlocks) {

        var TemplateConfigDialog = function (options) {
            this.app = options.app;
            this.templateContainer = options.templateContainer;
            this.config = '';
            this.configEditor;

        };

        TemplateConfigDialog.prototype.constructor = TemplateConfigDialog;

        TemplateConfigDialog.prototype.render = function () {
            var self = this;
            var divId = "configurationEditorId";
            var templateEntry = "<div class='config-template-container' id='".concat(divId).concat("'></div>");
            self.templateContainer.append(templateEntry);

            this._mainEditor = new SiddhiEditor({
                divID: divId,
                realTimeValidation: false,
                autoCompletion: false
            });

            this._editor = ace.edit(divId);
            this._editor.getSession().setValue("");
            this._editor.resize(true);
            self.configEditor = this._editor;
            $("#siddhi-config-dropdown-id #data-sources").removeClass('disabled-dropdown-list-element');
            $("#siddhi-config-dropdown-id #data-sources").addClass('dropdown-list-element');
            $("#siddhi-config-dropdown-id #metrics").removeClass('disabled-dropdown-list-element');
            $("#siddhi-config-dropdown-id #metrics").addClass('dropdown-list-element');
            $("#siddhi-config-dropdown-id #extensions").removeClass('disabled-dropdown-list-element');
            $("#siddhi-config-dropdown-id #extensions").addClass('dropdown-list-element');
            $("#siddhi-config-dropdown-id #references").removeClass('disabled-dropdown-list-element');
            $("#siddhi-config-dropdown-id #references").addClass('dropdown-list-element');
            $("#siddhi-config-dropdown-id #transports").removeClass('disabled-dropdown-list-element');
            $("#siddhi-config-dropdown-id #transports").addClass('dropdown-list-element');

            $('#siddhi-config-dropdown-id li a').click(function(){
                var currentConfig = self._editor.getSession().getValue("");
                var needToDisableAttribute = "";
                var defaultConfigBlockContainer = new TemplateConfigBlocks();
                var defaultConfigs = defaultConfigBlockContainer.getTemplatedConfig();
                if ($(this).attr("id") == "data-sources") {
                    currentConfig += defaultConfigs["sampleDatasourceConfig"];
                    needToDisableAttribute = "#siddhi-config-dropdown-id #" + $(this).attr("id");
                } else if ($(this).attr("id") == "metrics") {
                    currentConfig += defaultConfigs["sampleMetricsConfig"];
                    needToDisableAttribute = "#siddhi-config-dropdown-id #" + $(this).attr("id");
                } else if ($(this).attr("id") == "extensions") {
                     currentConfig += defaultConfigs["sampleExtensionsConfig"];
                     needToDisableAttribute = "#siddhi-config-dropdown-id #" + $(this).attr("id");
                } else if ($(this).attr("id") == "references") {
                     currentConfig += defaultConfigs["sampleRefsConfig"];
                     needToDisableAttribute = "#siddhi-config-dropdown-id #" + $(this).attr("id");
                }  else if ($(this).attr("id") == "transports") {
                    currentConfig += defaultConfigs["sampleTransportConfig"];
                    needToDisableAttribute = "#siddhi-config-dropdown-id #" + $(this).attr("id");
                }
                self._editor.getSession().setValue(currentConfig);
                $(needToDisableAttribute).removeClass('dropdown-list-element');
                $(needToDisableAttribute).addClass('disabled-dropdown-list-element');
            });

            $('#config-template-container-id #configurationEditorId').keyup(function(){
                var currentConfig = self._editor.getSession().getValue("");
                var dataSourceRegex = /(^dataSources|\n\s*dataSources)/gi
                var metricsRegex = /(^metrics|\n\s*metrics)/gi
                var extensionsRegex = /(^extensions|\n\s*extensions)/gi
                var referencesRegex = /(^refs|\n\s*refs)/gi
                var transportsRegex = /(^transports|\n\s*transports)/gi
                if (currentConfig.match(dataSourceRegex) == null) {
                    $("#siddhi-config-dropdown-id #data-sources").removeClass('disabled-dropdown-list-element');
                    $("#siddhi-config-dropdown-id #data-sources").addClass('dropdown-list-element');
                }
                if (currentConfig.match(metricsRegex) == null) {
                    $("#siddhi-config-dropdown-id #metrics").removeClass('disabled-dropdown-list-element');
                    $("#siddhi-config-dropdown-id #metrics").addClass('dropdown-list-element');
                }
                if (currentConfig.match(extensionsRegex) == null) {
                    $("#siddhi-config-dropdown-id #extensions").removeClass('disabled-dropdown-list-element');
                    $("#siddhi-config-dropdown-id #extensions").addClass('dropdown-list-element');
                }
                if (currentConfig.match(referencesRegex) == null) {
                    $("#siddhi-config-dropdown-id #references").removeClass('disabled-dropdown-list-element');
                    $("#siddhi-config-dropdown-id #references").addClass('dropdown-list-element');
                }
                if (currentConfig.match(transportsRegex) == null) {
                    $("#siddhi-config-dropdown-id #transports").removeClass('disabled-dropdown-list-element');
                    $("#siddhi-config-dropdown-id #transports").addClass('dropdown-list-element');
                }
            });
        };

        function isJsonString(str) {
            try {
                JSON.parse(str);
            } catch (e) {
                return false;
            }
            return true;
        }

        TemplateConfigDialog.prototype.getTemplatedConfig = function () {
            var self = this;
            if (self.configEditor != undefined) {
                return self.configEditor.session.getValue().trim();
            }
        };
        return TemplateConfigDialog;
    });


