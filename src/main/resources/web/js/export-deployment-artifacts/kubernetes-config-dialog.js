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

            var siddhiProcessName = '<div class="form-group">\n' +
                '          <label>Docker Image Tag:</label>      <input type="text" class="form-control" id="sp-name-input-field" ' +
                'placeholder="<DOCKER_REGISTRY_NAME>/<IMAGE_NAME>:<IMAGE_VERSION>">\n' +
                '            </div>';

            var checkboxs = '<div class="form-group">\n' +
                '           <input type="checkbox" name="download-docker-artifacts" value="download"> Download artifacts<br>\n' +
                ' <input type="checkbox" name="push-docker-image" id="docker-push-checkbox" value="push"> Push to docker registry<br>\n' +
                '            </div>';

            var dockerProperties = '<div id="properties-id" style="display:none"><form id="docker-properties-form">\n' +
                '  Docker Username:  <input type="text" name="username" id="username"><br>\n' +
                '  Docker Password:  <input type="text" name="password" id="password"><br>\n' +
                '  Email:            <input type="text" name="email" id="email"><br>\n' +
                '</form></div>';


            // var divId = "kubernetes-config-editor-id";
            // var templateEntry = "<div class= 'config-template-container' id='".concat(divId).concat("'></div>");
            // self.templateContainer.append(templateEntry);
            //
            // this._mainEditor = new SiddhiEditor({
            //     divID: divId,
            //     realTimeValidation: false,
            //     autoCompletion: false
            // });
            //
            // this._editor = ace.edit(divId);
            // this._editor.resize(true);
            self.k8ConfigEditor = this._editor;
        };

        KubernetesConfigDialog.prototype.getKubernetesConfigs = function () {
            var self = this;
            return self.k8ConfigEditor.session.getValue();
        };
        return KubernetesConfigDialog;
    });


