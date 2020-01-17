/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash','jquery', 'log', 'backbone', 'file_browser', 'workspace/file', 'sample_view'],
    function (require, _, $, log, Backbone, FileBrowser, File, SampleView) {
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
                        "<div class='modal-content' id='sampleDialog'>" +
                        "<div class='modal-header'>" +
                        "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                        "<i class=\"fw fw-cancel  about-dialog-close\"></i>" +
                        "</button>" +
                        "<h4 class='modal-title file-dialog-title'>Import Sample</h4>" +
                        "<hr class='style1'>" +
                        "</div>" +
                        "<div class='modal-body'>" +
                        "<div class='container-fluid'>" +
                        "<form class='form-horizontal' onsubmit='return false'>" +
                        "<div class='form-group'>" +
                        "<label for='locationSearch' class='col-sm-2 file-dialog-label'>Search :</label>" +
                        "<input type='text' class='search-file-dialog-form-control' id='locationSearch' autofocus>" +
                        "</div>" +
                        "<div class='form-group'>" +
                        "<div class='file-dialog-form-scrollable-block' style='padding: 10px 4px; margin-left:35px;'>" +
                        "<div id='noResults' style='display:none;'>No results found</div>" +
                        "<div id='sampleTable' class='samples-pane'>" +
                        "</div>" +
                        "<div id='file-browser-error' class='alert alert-danger' style='display: none;'>" +
                        "</div>" +
                        "</div>" +
                        "</div>" +
                        "<div class='form-group'>" +
                        "<div class='file-dialog-form-btn'>" +
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
                    var locationSearch = sampleFileOpen.find("input").filter("#locationSearch");

                    openSampleConfigModal.on('shown.bs.modal', function () {
                        locationSearch.focus();
                    });

                    var treeContainer = sampleFileOpen.find("div").filter("#sampleTable");
                    var bodyUlSampleContent = $('<ul class="recent-files clearfix"></ul>');
                    bodyUlSampleContent.attr('id', "sampleList");
                    var workspaceServiceURL = app.config.services.workspace.endpoint;
                    var getSampleDescServiceURL = workspaceServiceURL + "/listFiles/samples/descriptions";
                    var samplesWithDes = {};

                    $.ajax({
                        type: "GET",
                        contentType: "json",
                        url: getSampleDescServiceURL,
                        async: false,
                        success: function (data) {
                            samplesWithDes = data;
                        },
                        error: function (e) {
                            alertError("Unable to read a sample file.");
                            throw "Unable to read a sample file.";
                        }

                    });

                    samplesWithDes.forEach(function (sample) {
                        var sampleName = sample.path;
                        var config =
                            {
                                "sampleName": ((sampleName.substring(sampleName.lastIndexOf('/') + 1)).split(".siddhi"))[0],
                                "sampleDes": sample.description,
                                "parentContainer": bodyUlSampleContent,
                                "firstItem": true,
                                "clickEventCallback": function (event) {
                                    event.preventDefault();
                                    var payload = sampleName;
                                    openSample(payload);
                                }
                            };
                        samplePreview = new SampleView(config);
                        samplePreview.render();
                        treeContainer.append(bodyUlSampleContent);
                    });

                    locationSearch.keyup(function () {
                        var searchText = locationSearch.val();
                        searchSample(bodyUlSampleContent, searchText);
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

                    function openSample(payload) {
                        var fileRelativeLocation = "artifacts" + self.app.getPathSeperator() + payload;
                        var workspaceServiceURL = app.config.services.workspace.endpoint;
                        var openSampleServiceURL = workspaceServiceURL + "/read/sample";
                        var browserStorage = app.browserStorage;

                        $.ajax({
                            url: openSampleServiceURL,
                            type: "POST",
                            data: fileRelativeLocation,
                            contentType: "text/plain; charset=utf-8",
                            async: false,
                            success: function (data, textStatus, xhr) {
                                if (xhr.status == 200) {
                                    var file = new File({
                                        content: data.content
                                    }, {
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
                                if (isJsonString(res.responseText)) {
                                    var resObj = JSON.parse(res.responseText);
                                    if (_.has(resObj, 'Error')) {
                                        msg = _.get(resObj, 'Error');
                                    }
                                }
                                openFileWizardError.text(msg);
                                openFileWizardError.show();
                            }
                        });
                    };

                    function searchSample(sampleContent, searchText) {
                        var filter = searchText.toUpperCase();
                        var sampleElements = sampleContent[0].getElementsByTagName("li");
                        var noResultsElement = sampleFileOpen.find("div").filter("#noResults");
                        var unmatchedCount = 0;
                        for (var i = 0; i < sampleElements.length; i++) {
                            var sampleElement = sampleElements[i].getElementsByTagName("a")[0];
                            if (sampleElement.innerText.toUpperCase().indexOf(filter) > -1) {
                                sampleElements[i].style.display = "";
                            } else {
                                sampleElements[i].style.display = "none";
                                unmatchedCount += 1;
                            }
                        }
                        var isMatched = (unmatchedCount === sampleElements.length);
                        noResultsElement.toggle(isMatched);
                    }
                },
            });

        return OpenSampleFileDialog;
    });
