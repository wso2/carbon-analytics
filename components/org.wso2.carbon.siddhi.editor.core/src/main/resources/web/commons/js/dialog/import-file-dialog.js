/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['require', 'lodash', 'jquery', 'log', 'backbone', 'file_browser', 'workspace/file'],
    function (require, _, $, log, Backbone, FileBrowser, File) {
        var ImportFileDialog = Backbone.View.extend(
            /** @lends SaveToFileDialog.prototype */
            {
                /**
                 * @augments Backbone.View
                 * @constructs
                 * @class SaveToFileDialog
                 * @param {Object} config configuration options for the SaveToFileDialog
                 */
                initialize: function (options) {
                    this.app = options;
                    this.pathSeparator = this.app.getPathSeperator();
                    this.dialog_container = $(_.get(options.config.dialog, 'container'));
                    this.notification_container = _.get(options.config.tab_controller.tabs.tab.das_editor.notifications,
                        'container');
                    this.source_view_container = _.get(options.config.tab_controller.tabs.tab.das_editor,
                        'source_view.container');
                },

                render: function () {

                    var self = this;
                    var app = this.app;
                    var fileContent;
                    var fileName;
                    var notification_container = this.notification_container;

                    var ImportLink = document.createElement("input");
                    ImportLink.type = "file";
                    ImportLink.name = "File";
                    ImportLink.accept = ".siddhi";
                    ImportLink.onchange = handleFiles;
                    ImportLink.onclick = destroyClickedElement;
                    ImportLink.style.display = "none";
                    document.body.appendChild(ImportLink);
                    ImportLink.click();

                    function destroyClickedElement(event) {
                        document.body.removeChild(event.target);
                    }

                    function handleFiles(event) {
                        var files = event.target.files;
                        var file = files[0];
                        var reader = new FileReader();

                        fileName = file.name;

                        var existsResponse = existFileInPath({
                            configName: fileName
                        });

                        if (existsResponse.error === undefined) {
                            if (existsResponse.exists) {
                                alertError("A file already exist in workspace with selected name.");
                                return;
                            } else {
                                reader.readAsText(file);
                                importConfiguration();
                            }
                        } else {
                            alertError("Error in reading the file.");
                        }

                        reader.onload = (function (reader) {
                            return function () {
                                fileContent = reader.result;
                            }
                        })(reader);
                    }

                    function existFileInPath(options) {
                        var client = self.app.workspaceManager.getServiceClient();
                        var data = {};
                        var workspaceServiceURL = app.config.services.workspace.endpoint;
                        var saveServiceURL = workspaceServiceURL + "/exists/workspace";
                        var payload = "configName=" + btoa("workspace" + app
                            .getPathSeperator() + options.configName);

                        $.ajax({
                            type: "POST",
                            contentType: "text/plain; charset=utf-8",
                            url: saveServiceURL,
                            data: payload,
                            async: false,
                            success: function (response) {
                                data = response;
                            },
                            error: function (xhr, textStatus, errorThrown) {
                                data = client.getErrorFromResponse(xhr, textStatus, errorThrown);
                                log.error(data.message);
                            }
                        });
                        return data;
                    }

                    function isJsonString(str) {
                        try {
                            JSON.parse(str);
                        } catch (e) {
                            return false;
                        }
                        return true;
                    }

                    function importConfiguration() {

                        var workspaceServiceURL = app.config.services.workspace.endpoint;
                        var importServiceURL = workspaceServiceURL + "/write";
                        var config = fileContent;
                        var configName = fileName;

                        var payload = "configName=" + btoa("workspace" + self.app
                            .getPathSeperator() + configName) + "&config=" + (btoa(config));

                        $.ajax({
                            url: importServiceURL,
                            type: "POST",
                            data: payload,
                            contentType: "text/plain; charset=utf-8",
                            async: false,
                            success: function (data, textStatus, xhr) {
                                if (xhr.status == 200) {

                                    var file = new File({
                                        name: configName,
                                        content: config,
                                        isPersisted: true,
                                        isDirty: false
                                    });
                                    app.commandManager.dispatch("create-new-tab", {
                                        tabOptions: {
                                            file: file
                                        }
                                    });
                                    alertSuccess();
                                } else {
                                    alertError(data.Error);
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
                                alertError(msg);
                            }
                        });
                    }

                    var successNotification = $(
                        "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-success' " +
                        "id='success-alert'><span class='notification'>" +
                        "Siddhi file is successfully imported to workspace." +
                        "</span>" +
                        "</div>");

                    function alertSuccess() {
                        $(notification_container).append(successNotification);
                        successNotification.fadeTo(2000, 200).slideUp(1000, function () {
                            successNotification.slideUp(1000);
                        });
                    }

                    function alertError(errorMessage) {
                        var errorNotification = getErrorNotification(errorMessage);
                        $(notification_container).append(errorNotification);
                        errorNotification.fadeTo(2000, 200).slideUp(1000, function () {
                            errorNotification.slideUp(1000);
                        });
                    }

                    function getErrorNotification(detailedErrorMsg) {
                        var errorMsg = "Error while importing file";
                        if (!_.isEmpty(detailedErrorMsg)) {
                            errorMsg = (detailedErrorMsg);
                        }
                        return $(
                            "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-danger'" +
                            " id='error-alert'>" + "<span class='notification'>" +
                            errorMsg +
                            "</span>" +
                            "</div>");
                    }

                },
            });

        return ImportFileDialog;
    });