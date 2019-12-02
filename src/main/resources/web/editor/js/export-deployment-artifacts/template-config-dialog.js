/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash', 'jquery', 'log', 'ace/ace', 'app/source-editor/editor', 'alerts'],
    function (require, _, $, log, ace, SiddhiEditor, alerts) {

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
            this.sampleDatasourceConfig =  'dataSources: \n' +
               '  - name: SIDDHI_TEST_DB \n' +
               '    description: The datasource used for test database \n' +
               '    jndiConfig: \n' +
               '      name: jdbc/SIDDHI_TEST_DB \n' +
               '    definition: \n' +
               '      type: RDBMS \n' +
               '      configuration: \n' +
               '        jdbcUrl: jdbc:mysql://hostname:port/testdb \n' +
               '        username: root \n' +
               '        password: root \n' +
               '        driverClassName: com.mysql.jdbc.Driver \n' +
               '        maxPoolSize: 10 \n' +
               '        idleTimeout: 60000 \n' +
               '        connectionTestQuery: SELECT 1 \n' +
               '        validationTimeout: 30000 \n' +
               '        isAutoCommit: false \n';
            this.sampleMetricsConfig =  'metrics: \n' +
               '  levels: \n' +
               '    # The root level configured for Metrics \n' +
               '    rootLevel: INFO \n' +
               '    levels: \n' +
               '      jvm.buffers: \'OFF\' \n' +
               '      jvm.class-loading: INFO \n' +
               '      jvm.gc: DEBUG \n' +
               '      jvm.memory: INFO \n';
            this.sampleExtensionsConfig =  'extensions: \n' +
               '  - \n' +
               '    extension: \n' +
               '      name: extension_name \n' +
               '      namespace: extension_namespace \n' +
               '      properties: \n' +
               '        key: value \n';
            this.sampleRefsConfig = 'refs: \n' +
               '  - \n' +
               '    ref: \n' +
               '      name: \'name\' \n' +
               '      type: \'type\' \n' +
               '      properties: \n' +
               '        property1: value1 \n' +
               '        property2: value2 \n';
            this.sampleTransportConfig =  'wso2.carbon: \n' +
               '  id: siddhi-runner \n' +
               '  name: Siddhi Runner Distribution \n' +
               '  hostnameVerificationEnabled: false \n';

            $('#siddhi-config-dropdown-id li a').click(function(){
                var currentConfig = self._editor.getSession().getValue("");
                var needToDisableAttribute = "";

                if ($(this).attr("id") == "data-sources") {
                    currentConfig += self.sampleDatasourceConfig;
                    needToDisableAttribute = "#siddhi-config-dropdown-id #" + $(this).attr("id");
                } else if ($(this).attr("id") == "metrics") {
                    currentConfig += self.sampleMetricsConfig;
                    needToDisableAttribute = "#siddhi-config-dropdown-id #" + $(this).attr("id");
                } else if ($(this).attr("id") == "extensions") {
                     currentConfig += self.sampleExtensionsConfig;
                     needToDisableAttribute = "#siddhi-config-dropdown-id #" + $(this).attr("id");
                } else if ($(this).attr("id") == "references") {
                     currentConfig += self.sampleRefsConfig;
                     needToDisableAttribute = "#siddhi-config-dropdown-id #" + $(this).attr("id");
                }  else if ($(this).attr("id") == "transports") {
                    currentConfig += self.sampleTransportConfig;
                    needToDisableAttribute = "#siddhi-config-dropdown-id #" + $(this).attr("id");
                }
                self._editor.getSession().setValue(currentConfig);
                $(needToDisableAttribute).removeClass('dropdown-list-element');
                $(needToDisableAttribute).addClass('disabled-dropdown-list-element');
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
                return self.configEditor.session.getValue();
            }
        };
        return TemplateConfigDialog;
    });


