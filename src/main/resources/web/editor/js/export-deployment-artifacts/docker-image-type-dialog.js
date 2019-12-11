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

define(['require', 'lodash', 'jquery', 'version'],
    function (require, _, $, Version) {

        var DockerImageTypeDialog = function (options) {
            this.dockerNameMissingErrorMessage = options.templateHeader.find("#k8s-path-missing-docker-build-type-error-message");
            this.dockerNameInUpperError = options.templateHeader.find("#k8s-path-missing-docker-build-type-upper-error");
            this.templateContainer = options.templateHeader;
            this.dockerImageNameForm = options.templateHeader.find('#k8s-path-docker-image-name-form');
        };

        DockerImageTypeDialog.prototype.constructor = DockerImageTypeDialog;

        DockerImageTypeDialog.prototype.render = function () {
            var self = this;
            this.templateContainer.show();
            self.templateContainer.find("#docker-image-type-tobuild").prop("checked", true);
            self.templateContainer.click(function(event){
                var preBuiltImage = self.templateContainer.find("#docker-image-type-existing").is(":checked");
                if (preBuiltImage) {
                    self.dockerImageNameForm.show();
                    self.templateContainer.find("#docker-image-type-tobuild").prop("checked", false);
                    if (self.dockerImageNameForm.find("#built-docker-image-name-input-field").val() == "") {
                        self.dockerImageNameForm.find("#built-docker-image-name-input-field").val("siddhiio/siddhi-runner-alpine:"+Version.DOCKER_VERSION);
                    }
                } else {
                    self.dockerImageNameForm.hide();
                    self.dockerNameMissingErrorMessage.hide();
                    self.dockerNameInUpperError.hide();
                    self.templateContainer.find("#docker-image-type-tobuild").prop("checked", true);
                }
            });

            self.dockerImageNameForm.find("#built-docker-image-name-input-field").on('input', function() {
                var imageName = self.dockerImageNameForm.find("#built-docker-image-name-input-field").val();
                var isExistingImage = self.templateContainer.find("#docker-image-type-existing").is(":checked");
                if (isExistingImage) {
                    var upperCase = new RegExp('[A-Z]');
                    if (!imageName.match(upperCase)) {
                        self.dockerNameInUpperError.hide();
                    } else {
                        self.dockerNameInUpperError.show();
                    }
                    if (imageName !== "") {
                        self.dockerNameMissingErrorMessage.hide();
                    }
                }
            });
        };

        DockerImageTypeDialog.prototype.getDockerTypeConfigs = function () {
            var self = this;
            var templateKeyValue = {};
            templateKeyValue["isExistingImage"] = self.templateContainer.find("#docker-image-type-existing").is(":checked");
            templateKeyValue["imageName"] = self.dockerImageNameForm.find("#built-docker-image-name-input-field").val();
            templateKeyValue["userName"] = "";
            templateKeyValue["password"] = "";
            templateKeyValue["email"] = "";
            templateKeyValue["downloadDocker"] = self.templateContainer.find("#docker-image-type-existing").is(":checked");
            templateKeyValue["pushDocker"] = self.templateContainer.find("#docker-image-type-tobuild").is(":checked");
            return templateKeyValue;
        };

        DockerImageTypeDialog.prototype.validateDockerTypeConfig = function () {
            var self = this;
            var isExistingImage = self.templateContainer.find("#docker-image-type-existing").is(":checked");
            var imageName = self.dockerImageNameForm.find("#built-docker-image-name-input-field").val();
            if (isExistingImage) {
                var upperCase = new RegExp('[A-Z]');
                if (imageName === null || imageName == "") {
                    self.dockerNameMissingErrorMessage.show();
                    return false;
                } else if (imageName.match(upperCase)) {
                    self.dockerNameInUpperError.show();
                    return false;
                }
            }
            return true;
        };

        return DockerImageTypeDialog;
    });
