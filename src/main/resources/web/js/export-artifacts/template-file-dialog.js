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

        var TemplateFileDialog = function (options) {
            this.app = options.app;
            this.appNames = options.siddhiAppNames;
            this.templateContainer = options.templateHeader;
            this.appArr = this.readSiddhiApps(this.appNames);
            this.editorObjArr = [];
        };

        TemplateFileDialog.prototype.constructor = TemplateFileDialog;

        TemplateFileDialog.prototype.render = function () {
            var self = this;
            var i;
            for (i = 0; i < self.appArr.length; i++) {
                var entry = self.appArr[i];
                var divId = "siddhi-app-content-id".concat(i);

                var heading = $('<h3 class="siddhi-app-template-header"></h3>').text(entry.name);
                var div = $('<div class="siddhi-app-template-container"></div>').attr("id", divId);

                self.templateContainer.append(heading);
                self.templateContainer.append(div);

                this._mainEditor = new SiddhiEditor({
                    divID: divId,
                    realTimeValidation: true,
                    autoCompletion: true
                });

                this._editor = ace.edit(divId);
                this._editor.getSession().setValue(entry.content);
                this._editor.resize(true);
                var obj = {
                    name: entry.name,
                    content: this._editor
                };
                self.editorObjArr.push(obj);
            }
            self.templateContainer.accordion();
        };

        TemplateFileDialog.prototype.readSiddhiApps = function (appNames) {
            var self = this;
            var apps = [];
            var i;
            for (i = 0; i < appNames.length; i++) {
                var fileName = appNames[i];
                var fileRelativeLocation = "workspace" + self.app.getPathSeperator() +
                    fileName;
                var defaultView = {configLocation: fileRelativeLocation};
                var workspaceServiceURL = self.app.config.services.workspace.endpoint;
                var openServiceURL = workspaceServiceURL + "/read";

                var path = defaultView.configLocation;
                $.ajax({
                    url: openServiceURL,
                    type: "POST",
                    data: path,
                    contentType: "text/plain; charset=utf-8",
                    async: false,
                    success: function (data, textStatus, xhr) {
                        if (xhr.status == 200) {
                            var pathArray = _.split(path, self.app.getPathSeperator()),
                                fileName = _.last(pathArray),
                                folderPath = _.join(_.take(pathArray, pathArray.length - 1), self.app
                                    .getPathSeperator());
                            var siddhiApp = {
                                name: fileName,
                                content: data.content
                            };
                            apps.push(siddhiApp);
                        } else {
                            console.error("Failed to read Siddhi Application" + data.error);
                        }
                    },
                    error: function (res, errorCode, error) {
                        var msg = _.isString(error) ? error : res.statusText;
                        if (isJsonString(res.responseText)) {
                            var resObj = JSON.parse(res.responseText);
                            if (_.has(resObj, 'Error')) {
                                msg = _.get(resObj, 'Error');
                            }
                        }
                        console.error(msg);
                    }
                });
            }
            return apps;
        };

        TemplateFileDialog.prototype.show = function () {
            this._fileOpenModal.modal('show');
        };

        TemplateFileDialog.prototype.getTemplatedApps = function () {
            var self = this;
            var templatedApps = [];
            var i;
            for (i = 0; i < self.editorObjArr.length; i++) {
                var appEntry = {
                    appName: self.editorObjArr[i].name,
                    appContent: self.editorObjArr[i].content.session.getValue()
                }
                templatedApps.push(appEntry);
            }
            return templatedApps;
        };
        return TemplateFileDialog;
    });


