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
            this.k8ConfigEditor;

        };

        KubernetesConfigDialog.prototype.constructor = KubernetesConfigDialog;

        KubernetesConfigDialog.prototype.render = function () {
            var self = this;
            var divId = "k8ConfigEditorId";
            var templateEntry = "<div id='".concat(divId).concat("' style='height: 100%;'></div>");
            self.templateContainer.append(templateEntry);

            this._mainEditor = new SiddhiEditor({
                divID: divId,
                realTimeValidation: false,
                autoCompletion: false
            });

            this._editor = ace.edit(divId);
            this._editor.resize(true);
            self.k8ConfigEditor = this._editor;
        };

        KubernetesConfigDialog.prototype.show = function () {
            this._fileOpenModal.modal('show');
        };

        KubernetesConfigDialog.prototype.getKubernetesConfigs = function () {
            var self = this;
            return self.k8ConfigEditor.session.getValue();
        };
        return KubernetesConfigDialog;
    });


