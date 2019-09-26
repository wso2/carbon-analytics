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

define(['require', 'lodash', 'jquery'],
    function (require, _, $) {

        var DockerConfigDialog = function (options) {
            this.container = options.templateHeader;
            this.payload = options.payload;
        };

        DockerConfigDialog.prototype.constructor = DockerConfigDialog;

        DockerConfigDialog.prototype.render = function () {
            var self = this;

            var imageNameInput = '<div class="form-group">\n' +
                '          <label>Docker Image Tag:</label>      <input type="text" class="form-control" id="docker-img-name-input-field" ' +
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

            self.container.append(imageNameInput);
            self.container.append(checkboxs);
            self.container.append(dockerProperties);

            $("#docker-push-checkbox").change(function(){
                if ($(this).prop('checked')==true){
                    $('#properties-id').css("display", "block");;
                } else {
                    $('#properties-id').css("display", "none");
                }
            });
        };

        DockerConfigDialog.prototype.getDockerConfigs = function () {
            var self = this;
                var templateKeyValue = {};
                templateKeyValue["imageName"] = self.container.find("#docker-img-name-input-field").val();
                templateKeyValue["username"] = self.container.find("#username").val();
                templateKeyValue["password"] = self.container.find("#password").val();
                templateKeyValue["email"] = self.container.find("#email").val();
                console.log(templateKeyValue);
            return templateKeyValue;
        };


        DockerConfigDialog.prototype.show = function () {
            this._fileOpenModal.modal('show');
        };

        return DockerConfigDialog;
    });
