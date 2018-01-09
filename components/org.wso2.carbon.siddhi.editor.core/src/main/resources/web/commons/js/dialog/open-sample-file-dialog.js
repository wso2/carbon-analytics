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

define(['require', 'lodash','jquery', 'log', 'backbone', 'file_browser', 'workspace/file'],
    function (require, _, $, log, Backbone, FileBrowser, File) {
    var OpenSampleFileDialog = Backbone.View.extend(
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

            show: function(){
                this._sampleFileOpenModal.modal('show');
            },

            select: function(path){
                this._fileBrowser.select('path');
            },

            render: function () {
                var self = this;
                var fileBrowser;
                var fileContent;
                var app = this.app;
                var notification_container = this.notification_container;

                if(!_.isNil(this._sampleFileOpenModal)){
                    this._sampleFileOpenModal.remove();
                }

                var sampleFileOpen = $(
                    "<div class='modal fade' id='openSampleConfigModal' tabindex='-1' role='dialog' " +
                    "aria-tydden='true'>" + "<div class='modal-dialog file-dialog' role='document'>" +
                    "<div class='modal-content'>" +
                    "<div class='modal-header'>" +
                    "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                    "<span aria-hidden='true'>&times;</span>" +
                    "</button>" +
                    "<h4 class='modal-title file-dialog-title'>Import Sample</h4>" +
                    "<hr class='style1'>"+
                    "</div>" +
                    "<div class='modal-body'>" +
                    "<div class='container-fluid'>" +
                    "<form class='form-horizontal' onsubmit='return false'>" +
                    "<div class='form-group'>" +
                    "<label for='location' class='col-sm-2 file-dialog-label'>Sample Name :</label>" +
                    "<div class='col-sm-9'>" +
                    "<input type='text' class='file-dialog-form-control' id='location' readonly>" +
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
                    "<button id='openButton' type='button' class='btn btn-primary'>open" +
                    "</button>" +
                    "<div class='divider'/>" +
                    "<button type='button' class='btn btn-default' data-dismiss='modal'>cancel</button>" +
                    "</div>" +
                    "</div>" +
                    "</form>" +
                    "<div id='openFileWizardError' class='alert alert-danger'>" +
                    "<strong>Error!</strong> Something went wrong." +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>"
                );

                var successNotification = $(
                    "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-success' " +
                    "id='success-alert'>" +
                    "<span class='notification'>" +
                    "Configuration opened successfully !" +
                    "</span>" +
                    "</div>");

                function getErrorNotification(detailedErrorMsg) {
                    var errorMsg = "Error while opening configuration";
                    if (!_.isEmpty(detailedErrorMsg)){
                        errorMsg += (" : " + detailedErrorMsg);
                    }
                    return $(
                        "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-danger' " +
                        "id='error-alert'>" +
                        "<span class='notification'>" +
                        errorMsg +
                        "</span>" +
                        "</div>");
                }

                var openSampleConfigModal = sampleFileOpen.filter("#openSampleConfigModal");
                var openFileWizardError = sampleFileOpen.find("#openFileWizardError");
                var location = sampleFileOpen.find("input").filter("#location");

                var treeContainer  = sampleFileOpen.find("div").filter("#fileTree");
                fileBrowser = new FileBrowser({container: treeContainer, application:app, fetchFiles:true,
                showWorkspace:false,showSamples:true});

                fileBrowser.render();
                this._fileBrowser = fileBrowser;

                //Gets the selected location from tree and sets the value as location
                this.listenTo(fileBrowser, 'selected', function (selectedLocation) {
                    var pathAttributes = selectedLocation.split(self.pathSeparator);
                    var fileName = _.last(pathAttributes);
                    self._artifactName = pathAttributes[pathAttributes.length - 2];

                    if(selectedLocation && fileName.lastIndexOf(".siddhi") != -1){
                        location.val(fileName);
                    }
                });

                sampleFileOpen.find("button").filter("#openButton").click(function () {

                    var _location = location.val();
                    if (_.isEmpty(_location)) {
                        openFileWizardError.text("Invalid Value for Location.");
                        openFileWizardError.show();
                        return;
                    }

                    var pathAttributes = _location.split(self.pathSeparator);
                    var fileName = _.last(pathAttributes);

                    var tabList = self.app.tabController.getTabList();
                    var fileAlreadyOpened = false;
                    var openedTab;

                    _.each(tabList, function(tab) {
                        if(tab.getTitle() == fileName){
                            fileAlreadyOpened = true;
                            openedTab = tab;
                        }
                    })

                    if(fileAlreadyOpened){
                        self.app.tabController.setActiveTab(openedTab);
                        openSampleConfigModal.modal('hide');
                    } else {
                        openSampleConfiguration({location: location});
                    }

                });


                $(this.dialog_container).append(sampleFileOpen);
                openFileWizardError.hide();
                this._sampleFileOpenModal = sampleFileOpen;

                function alertSuccess() {
                    $(notification_container).append(successNotification);
                    successNotification.fadeTo(2000, 200).slideUp(1000, function () {
                        successNotification.slideUp(1000);
                    });
                };

                function alertError(errorMessage) {
                    var errorNotification = getErrorNotification(errorMessage);
                    $(notification_container).append(errorNotification);
                    errorNotification.fadeTo(2000, 200).slideUp(1000, function () {
                        errorNotification.slideUp(1000);
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

                function openSampleConfiguration() {
                    var pathArray = _.split(location.val(), self.app.getPathSeperator());
                    var fileName = _.last(pathArray);
                    var fileRelativeLocation = "artifacts" + self.app.getPathSeperator() +
                        self._artifactName + self.app.getPathSeperator() + fileName;
                    var defaultView = {configLocation: fileRelativeLocation};
                    var workspaceServiceURL = app.config.services.workspace.endpoint;
                    var openSampleServiceURL = workspaceServiceURL + "/read/sample";
                    var browserStorage = app.browserStorage;

                    var path = defaultView.configLocation;
                    $.ajax({
                        url: openSampleServiceURL,
                        type: "POST",
                        data: path,
                        contentType: "text/plain; charset=utf-8",
                        async: false,
                        success: function (data, textStatus, xhr) {
                            if (xhr.status == 200) {
                                var file = new File({
                                    content: data.content
                                },{
                                    storage: browserStorage
                                });
                                openSampleConfigModal.modal('hide');
                                app.commandManager.dispatch("create-new-tab", {tabOptions: {file: file}});
                            } else {
                                openFileWizardError.text(data.Error);
                                openFileWizardError.show();
                            }
                        },
                        error: function (res, errorCode, error) {
                            var msg = _.isString(error) ? error : res.statusText;
                            if(isJsonString(res.responseText)){
                                var resObj = JSON.parse(res.responseText);
                                if(_.has(resObj, 'Error')){
                                    msg = _.get(resObj, 'Error');
                                }
                            }
                            openFileWizardError.text(msg);
                            openFileWizardError.show();
                        }
                    });
                };
            },
        });

    return OpenSampleFileDialog;
});
