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

define(['require', 'lodash', 'jquery', 'log', 'ace/ace', 'app/source-editor/editor'],
    function (require, _, $, log, ace, SiddhiEditor) {

        var KubernetesConfigDialog = function (options) {
            this.app = options.app;
            this.templateContainer = options.templateHeader;
            this.natsConfigsGiven = false;
            this.pvConfigsGiven = false;
            this.editorObjectArrayList = [];
        };

        KubernetesConfigDialog.prototype.constructor = KubernetesConfigDialog;

        KubernetesConfigDialog.prototype.render = function () {
            var self = this;
            let distributionSelectionInput = self.templateContainer.find("#distribution-selection");
            let persistenceSelectionInput = self.templateContainer.find("#persistent-select");
            distributionSelectionInput.find("#non-distributed").prop('checked', true);
            persistenceSelectionInput.find("#stateless").prop('checked', true);

            var messagingEdtdivId = "kubernetes-messaging-editor-id";
            var messagingTemplateEntry = "<div class='kubernetes-config-editor' style='display:none' id='".concat(messagingEdtdivId).concat("'></div>");
            self.templateContainer.find('#distribution-selection').append(messagingTemplateEntry);
            this._mainEditor = new SiddhiEditor({
                divID: messagingEdtdivId,
                realTimeValidation: false,
                autoCompletion: false
            });

            var messagingSampleConfig = 'messagingSystem:\n' +
                '  type: nats\n' +
                '  # config: \n' +
                '  #   bootstrapServers: \n' +
                '  #     - "nats://siddhi-nats:4222"\n' +
                '  #   streamingClusterId: siddhi-stan';
            this._editor1 = ace.edit(messagingEdtdivId);
            this._editor1.getSession().setValue(messagingSampleConfig);
            this._editor1.resize(true);
            var obj1 = {
                name: 'messaging',
                content: this._editor1
            };
            self.editorObjectArrayList.push(obj1);

            self.templateContainer.find('#distributed-with-ext-nats').change(function(){
                self.natsConfigsGiven = true;
                self.templateContainer.find('#kubernetes-messaging-editor-id').show();
            });

            self.templateContainer.find('#non-distributed').change(function(){
                self.natsConfigsGiven = false;
                self.templateContainer.find('#kubernetes-messaging-editor-id').hide();
            });

            self.templateContainer.find('#distributed-with-nats').change(function(){
                self.natsConfigsGiven = false;
                self.templateContainer.find('#kubernetes-messaging-editor-id').hide();
            });

            var divId = "kubernetes-pv-editor-id";
            var templateEntry = "<div class='kubernetes-config-editor' style='display:none' id='".concat(divId).concat("'></div>");
            self.templateContainer.find('#persistent-select').append(templateEntry);
            this._mainEditor = new SiddhiEditor({
                divID: divId,
                realTimeValidation: false,
                autoCompletion: false
            });

            var pvSampleConfig = 'persistentVolumeClaim: \n' +
                '  accessModes: \n' +
                '    - ReadWriteOnce\n' +
                '  resources: \n' +
                '    requests: \n' +
                '      storage: 1Gi\n' +
                '  storageClassName: standard\n' +
                '  volumeMode: Filesystem';
            this._editor2 = ace.edit(divId);
            this._editor2.getSession().setValue(pvSampleConfig);
            this._editor2.resize(true);
            var obj2 = {
                name: 'persistence',
                content: this._editor2
            };
            self.editorObjectArrayList.push(obj2);

            self.templateContainer.find('#stateless').change(function(){
                self.pvConfigsGiven = false;
                self.templateContainer.find('#kubernetes-pv-editor-id').hide();
            });

            self.templateContainer.find('#backed-by-pv').change(function(){
                self.pvConfigsGiven = true;
                self.templateContainer.find('#kubernetes-pv-editor-id').show();
            });
        };

        KubernetesConfigDialog.prototype.getKubernetesConfigs = function () {
            var self = this;
            var messagingConfig ='';
            var pvConfig = '';
            var siddhiProcessName = self.templateContainer.find("#sp-name-input-field").val() || 'sample-siddhi-process';
            var siddhiProcessNameConfig = "siddhiProcessName: ".concat(siddhiProcessName.toString());
            self.editorObjectArrayList.forEach(function(editorObj) {
                if(self.natsConfigsGiven && editorObj.name == 'messaging') {
                    messagingConfig = "\n" + editorObj.content.session.getValue().toString();
                }
                if(self.pvConfigsGiven && editorObj.name == 'persistence') {
                    pvConfig = "\n" + editorObj.content.session.getValue().toString();
                }
            });

            return siddhiProcessNameConfig + messagingConfig + pvConfig;
        };
        return KubernetesConfigDialog;
    });


