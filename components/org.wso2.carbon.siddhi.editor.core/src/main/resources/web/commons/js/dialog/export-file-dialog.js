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

define(['require', 'lodash', 'jquery', 'log', 'backbone', 'file_browser', 'bootstrap','ace/ace'], function
    (require, _, $, log, Backbone, FileBrowser,ace) {
    var ExportFileDialog = Backbone.View.extend(
        /** @lends ExportFileDialog.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class ExportFileDialog
             * @param {Object} config configuration options for the ExportFileDialog
             */
            initialize: function (options) {
                this.app = options;
                this.dialog_container = $(_.get(options.config.dialog, 'container'));
                this.notification_container = _.get(options.config.tab_controller.tabs.tab.das_editor.notifications, 'container');
            },

            show: function(){
                var self = this;
                this._fileExportModal.modal('show').on('shown.bs.modal', function(){
                    self.trigger('loaded');
                });
                this._fileExportModal.on('hidden.bs.modal', function(){
                    self.trigger('unloaded');
                })
            },

            setSelectedFile: function(path, fileName){
                this._fileBrowser.select(path);
                if(!_.isNil(this._configNameInput)){
                    this._configNameInput.val(fileName);
                }
            },

            render: function () {
                var self = this;
                var fileBrowser;
                var app = this.app;
                var notification_container = this.notification_container;
                var title = app.tabController.getActiveTab().getTitle();

                if(!_.isNil(this._fileExportModal)){
                    this._fileExportModal.remove();
                }

                var fileExport = $(
                    "<div class='modal fade' id='exportConfigModal' tabindex='-1' role='dialog' aria-tydden='true'>" +
                    "<div class='modal-dialog file-dialog' role='document'>" +
                    "<div class='modal-content'>" +
                    "<div class='modal-header'>" +
                    "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                    "<span aria-hidden='true'>&times;</span>" +
                    "</button>" +
                    "<h4 class='modal-title file-dialog-title' id='newConfigModalLabel'>Export</h4>" +
                    "<hr class='style1'>"+
                    "</div>" +
                    "<div class='modal-body'>" +
                    "<div class='container-fluid'>" +
                    "<form class='form-horizontal' onsubmit='return false'>" +
                    "<div class='form-group'>" +
                    "<label for='configName' class='col-sm-2 file-dialog-label'>File Name :</label>" +
                    "<div class='col-sm-9'>" +
                    '<input class="file-dialog-form-control" id="configName" placeholder="'+title+'">' +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label for='location' class='col-sm-2 file-dialog-label'>Location :</label>" +
                    "<div class='col-sm-9'>" +
                    "<input type='text' class='file-dialog-form-control' id='location'" +
                     "placeholder='eg: /home/user/wso2-das-server-tooling/das-configs'>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<div class='file-dialog-form-scrollable-block'>" +
                    "<div id='fileTree'>" +
                    "</div>" +
                    "<div id='file-browser-error' class='alert alert-danger' style='display: none;'>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<div class='file-dialog-form-btn'>" +
                    "<button id='exportButton' type='button' class='btn btn-primary'>export" +
                    "</button>" +
                    "<div class='divider'/>" +
                    "<button type='cancelButton' class='btn btn-default' data-dismiss='modal'>cancel</button>" +
                    "</div>" +
                    "</div>" +
                    "</form>" +
                    "<div id='exportWizardError' class='alert alert-danger'>" +
                    "<strong>Error!</strong> Something went wrong." +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>"
                );

                var successNotification = $(
                    "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-success' id='success-alert'>"+
                    "<span class='notification'>"+
                    "Configuration exported successfully !"+
                    "</span>"+
                    "</div>");

                function getErrorNotification(detailedErrorMsg) {
                    var errorMsg = "Error while exporting configuration";
                    if (!_.isEmpty(detailedErrorMsg)){
                        errorMsg += (" : " + detailedErrorMsg);
                    }
                    return $(
                        "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-danger' id='error-alert'>" +
                        "<span class='notification'>" +
                        errorMsg +
                        "</span>" +
                        "</div>");
                }

                var exportConfigModal = fileExport.filter("#exportConfigModal");
                var exportWizardError = fileExport.find("#exportWizardError");
                var location = fileExport.find("input").filter("#location");
                var configName = fileExport.find("input").filter("#configName");

                var treeContainer  = fileExport.find("div").filter("#fileTree")
                fileBrowser = new FileBrowser({container: treeContainer, application:app, fetchFiles:false});

                fileBrowser.render();
                this._fileBrowser = fileBrowser;
                this._configNameInput = configName;

                //Gets the selected location from tree and sets the value as location
                this.listenTo(fileBrowser, 'selected', function (selectedLocation) {
                    if(selectedLocation){
                        location.val(selectedLocation);
                    }
                });

                fileExport.find("button").filter("#exportButton").click(function() {
                    var _location = location.val();
                    var _configName = configName.val();
                    if (_.isEmpty(_location)) {
                        exportWizardError.text("Please enter a valid file location");
                        exportWizardError.show();
                        return;
                    }
                    if (_.isEmpty(_configName)) {
                        _configName = title;
                    }
                    if(!_configName.endsWith(".siddhi")){
                        _configName = _configName + ".siddhi";
                    }

                    var callback = function(isSaved) {
                        self.trigger('save-completed', isSaved);
                        if (isSaved) {
                            exportConfigModal.modal('hide');
                        }
                    };

                    var client = self.app.workspaceManager.getServiceClient();
                    var path = _location + '/' + _configName;
                    var existsResponse = client.exists(path);

                    if(existsResponse.error == undefined){
                        if(existsResponse.exists) {
                            // File with this name already exists. Need confirmation from user to replace
                            var replaceConfirmCb = function(confirmed) {
                                if(confirmed) {
                                    exportConfiguration({location: _location, configName: _configName}, callback);
                                } else {
                                    callback(false);
                                }
                            };

                            var options = {
                                path: path,
                                handleConfirm: replaceConfirmCb
                            };

                            self.app.commandManager.dispatch('open-replace-file-confirm-dialog', options);
                        } else {
                            exportConfiguration({location: _location, configName: _configName}, callback);
                        }
                    }else {
                        exportWizardError.text("Error in reading the file location "+_location);
                        exportWizardError.show();
                    }

                });

                $(this.dialog_container).append(fileExport);
                exportWizardError.hide();
                this._fileExportModal = fileExport;

                function alertSuccess(){
                    $(notification_container).append(successNotification);
                    successNotification.fadeTo(2000, 200).slideUp(1000, function(){
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

                function isJsonString(str) {
                    try {
                        JSON.parse(str);
                    } catch (e) {
                        return false;
                    }
                    return true;
                }

                function exportConfiguration(options, callback) {
                    var workspaceServiceURL = app.config.services.workspace.endpoint;
                    var exportServiceURL = workspaceServiceURL + "/export";
                    var activeTab = app.tabController.activeTab;
                    var siddhiFileEditor= activeTab.getSiddhiFileEditor();
                    var config = siddhiFileEditor.getContent();
                    var payload = "location=" + btoa(options.location) + "&configName=" + btoa(options.configName)
                        + "&config=" + (btoa(config));

                    $.ajax({
                        url: exportServiceURL,
                        type: "POST",
                        data: payload,
                        contentType: "text/plain; charset=utf-8",
                        async: false,
                        success: function (data, textStatus, xhr) {
                            if (xhr.status == 200) {
                                exportConfigModal.modal('hide');
                                log.debug('file exported successfully');
                                callback(true);
                            } else {
                                exportWizardError.text(data.Error);
                                exportWizardError.show();
                                callback(false);
                            }
                        },
                        error: function(res, errorCode, error){
                            var msg = _.isString(error) ? error : res.statusText;
                            if(isJsonString(res.responseText)){
                                var resObj = JSON.parse(res.responseText);
                                if(_.has(resObj, 'Error')){
                                    msg = _.get(resObj, 'Error');
                                }
                            }
                            exportWizardError.text(msg);
                            exportWizardError.show();
                            callback(false);
                        }
                    });
                }
            }
        });

    return ExportFileDialog;
});
