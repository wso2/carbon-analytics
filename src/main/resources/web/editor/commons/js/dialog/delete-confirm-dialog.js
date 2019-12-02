/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash', 'jquery', 'log', 'backbone', 'file_browser',
        '../../../js/event-simulator/simulator-rest-client', 'bootstrap', 'ace/ace'],
    function (require, _, $, log, Backbone, FileBrowser, SimulatorClient, ace) {
        var DeleteConfirmDialog = Backbone.View.extend(
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
                    this.application = _.get(options.config, 'application');
                    this.dialog_container = $(_.get(options.config.dialog, 'container'));
                    this.notification_container = _.get(options.config.tab_controller.tabs.tab.das_editor.notifications,
                        'container');
                },

                show: function () {
                    var self = this;
                    this._fileDeleteModal.modal('show').on('shown.bs.modal', function () {
                        self.trigger('loaded');
                    });
                    this._fileDeleteModal.on('hidden.bs.modal', function () {
                        self.trigger('unloaded');
                    })
                },

                setSelectedFile: function (path, fileName) {
                    this._fileBrowser.select(path);
                    if (!_.isNil(this._configNameInput)) {
                        this._configNameInput.val(fileName);
                    }
                },

                render: function () {
                    var self = this;
                    var fileBrowser;
                    var app = this.app;
                    var notification_container = this.notification_container;
                    var workspaceServiceURL = app.config.services.workspace.endpoint;
                    var activeTab = app.tabController.activeTab;
                    var siddhiFileEditor = activeTab.getSiddhiFileEditor();
                    var content = siddhiFileEditor.getContent();
                    var providedFileName = activeTab.getTitle();
                    var trimmedSiddhiAppName = providedFileName;
                    if (checkEndsWithSiddhi(trimmedSiddhiAppName)) {
                        trimmedSiddhiAppName = trimmedSiddhiAppName.slice(0, -7);
                    }

                    if (!_.isNil(this._fileDeleteModal)) {
                        this._fileDeleteModal.remove();
                    }

                    var fileDelete = $(
                        "<div class='modal fade' id='deleteAppModal' tabindex='-1' role='dialog' aria-tydden='true'>" +
                        "<div class='modal-dialog file-dialog' role='document'>" +
                        "<div class='modal-content'>" +
                        "<div class='modal-header'>" +
                        "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                        "<i class='fw fw-cancel about-dialog-close'> </i> " +
                        "</button>" +
                        "<h4 class='modal-title file-dialog-title' id='newConfigModalLabel'>Delete from Workspace<" +
                        "/h4>" +
                        "<hr class='style1'>" +
                        "</div>" +
                        "<div class='modal-body'>" +
                        "<div class='container-fluid'>" +
                        "<form class='form-horizontal' onsubmit='return false'>" +
                        "<div class='form-group'>" +
                        "<label for='configName' class='col-sm-9 file-dialog-label'>" +
                        "Are you sure to delete Siddhi App: " + providedFileName + "" +
                        "</label>" +
                        "</div>" +
                        "<div class='form-group'>" +
                        "<div class='file-dialog-form-btn'>" +
                        "<button id='deleteButton' type='button' class='btn btn-primary'>delete" +
                        "</button>" +
                        "<div class='divider'/>" +
                        "<button type='cancelButton' class='btn btn-default' data-dismiss='modal'>cancel</button>" +
                        "</div>" +
                        "</form>" +
                        "<div id='deleteWizardError' class='alert alert-danger'>" +
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
                        "Siddhi app deleted successfully !" +
                        "</span>" +
                        "</div>");

                    function getErrorNotification(detailedErrorMsg) {
                        var errorMsg = "Error while deleting Siddhi app";
                        if (!_.isEmpty(detailedErrorMsg)) {
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

                    var deleteAppModal = fileDelete.filter("#deleteAppModal");
                    var deleteWizardError = fileDelete.find("#deleteWizardError");
                    var location = fileDelete.find("input").filter("#location");
                    var configName = fileDelete.find("input").filter("#configName");
                    this._configNameInput = configName;

                    //Gets the selected location from tree and sets the value as location
                    this.listenTo(fileBrowser, 'selected', function (selectedLocation) {
                        if (selectedLocation) {
                            location.val(selectedLocation);
                        }
                    });

                    fileDelete.find("button").filter("#deleteButton").click(function () {
                        var existsResponse = existFileInPath({configName: providedFileName});
                        var callback = function (isDeleted) {
                            deleteAppModal.modal('hide');
                        };
                        if (existsResponse.exists) {
                            fileDelete.find("label").text("Please wait while checking on simulation dependencies.");
                            fileDelete.find("button[id='deleteButton']").prop("disabled", true);
                            SimulatorClient.getFeedSimulations(
                                function (data) {
                                    var simulations = JSON.parse(data.message).activeSimulations;
                                    if ( 1 > simulations.length) {
                                        deleteSiddhiAppAndCloseSingleSimulations(callback, this.application);
                                    } else {
                                        var simulationsExists = false;
                                        for (var i = 0; i < simulations.length; i++) {
                                            for (var j = 0; j < simulations[i].sources.length; j++) {
                                                if (simulations[i].sources[j].siddhiAppName == trimmedSiddhiAppName) {
                                                    SimulatorClient.getFeedSimulationStatus(
                                                        simulations[i].properties.simulationName,
                                                        function (data) {
                                                            if (data.message == "RUN") {
                                                                deleteWizardError.text("Cannot delete Siddhi app. " +
                                                                    "There are running simulations.");
                                                                deleteWizardError.show();
                                                                simulationsExists = true;
                                                            }
                                                        },
                                                        function (data) {
                                                            var message = {
                                                                "type" : "ERROR",
                                                                "message": "Cannot Simulate Siddhi App \"" + appName + "\" as its in Faulty state."
                                                            };

                                                            log.info(data);
                                                        },
                                                        false
                                                    );
                                                }
                                            }
                                        }
                                        if (!simulationsExists) {
                                            deleteSiddhiAppAndCloseSingleSimulations(callback, this.application);
                                        }
                                    }
                                },
                                function (data) {
                                    log.info(data);
                                }
                            );
                        } else {
                            deleteWizardError.text("File doesn't exists in workspace");
                            deleteWizardError.show();
                        }

                    });

                    $(this.dialog_container).append(fileDelete);
                    deleteWizardError.hide();
                    this._fileDeleteModal = fileDelete;

                    function deleteSiddhiAppAndCloseSingleSimulations(callback, application) {
                        var $singleEventConfigList = $("#single-event-configs")
                            .find("div[id^='event-content-parent-']");
                        var $singleEventConfigTabs = $("#single-event-config-tab");
                        var activetingUuid = -1;
                        $singleEventConfigList.each(function () {
                            var $singleEventConfig = $(this);
                            var siddhiAppName =
                                $singleEventConfig.find("select[name='single-event-siddhi-app-name']").val();
                            var simulationConfigId = $singleEventConfig.attr("id").replace("event-content-parent-", "");
                            var eventConfigTab = $singleEventConfigTabs
                                .find("li[data-uuid=\"" + simulationConfigId + "\"]");
                            if (trimmedSiddhiAppName == siddhiAppName) {
                                eventConfigTab.remove();
                                $(this).remove();
                            } else {
                                if (!eventConfigTab.hasClass("active")) {
                                    activetingUuid = eventConfigTab.attr("data-uuid");
                                }

                            }
                        });
                        renameSingleEventConfigTabs();
                        if (activetingUuid != -1) {
                            var eventConfigTab = $singleEventConfigTabs.find("li[data-uuid=\"" + activetingUuid + "\"]");
                            eventConfigTab.addClass("active");
                            var $singleEventConfig = $("#single-event-config-tab-content")
                                .find("div[id='event-content-parent-" + activetingUuid + "']");
                            $singleEventConfig.addClass("active");
                        }

                        var $singleEventConfigListTemp = $("#single-event-configs")
                            .find("div[id^='event-content-parent-']");
                        if ($singleEventConfigListTemp.size() == 0) {
                            self.app.commandManager.dispatch("add-single-simulator");
                        }

                        deleteSiddhiApp({
                            oldAppName: providedFileName,
                            application: application
                        }, callback);
                    }

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

                    function isJsonString(str) {
                        try {
                            JSON.parse(str);
                        } catch (e) {
                            return false;
                        }
                        return true;
                    }

                    function existFileInPath(options) {
                        var client = self.app.workspaceManager.getServiceClient();
                        var data = {};
                        var workspaceServiceURL = app.config.services.workspace.endpoint;
                        var saveServiceURL = workspaceServiceURL + "/exists/workspace";
                        var payload = "configName=" + self.app.utils.base64EncodeUnicode(options.configName);

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

                    function deleteSiddhiApp(options, callback) {
                        var activeTab = app.tabController.activeTab;
                        var relativePath = options.oldAppName;
                        var workspaceServiceURL = app.config.services.workspace.endpoint;
                        $.ajax({
                            url: workspaceServiceURL + "/delete?siddhiAppName=" + options.oldAppName ,
                            type: "DELETE",
                            contentType: "text/plain; charset=utf-8",
                            async: false,
                            success: function (data, textStatus, xhr) {
                                if (xhr.status == 200) {
                                    app.tabController.removeTab(activeTab, undefined, true);
                                    deleteAppModal.modal('hide');
                                    log.debug('file deleted successfully');
                                    callback(true);
                                    app.commandManager.dispatch("open-folder", data.path);
                                    app.eventSimulator.getFeedSimulator().updateFeedCreationButtonAndNotification();
                                    app.commandManager.dispatch("remove-siddhi-apps-on-delete", trimmedSiddhiAppName);
                                    alertSuccess();
                                } else {
                                    callback(false);
                                    console.log(data);
                                    alertError(data.Error)
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
                                callback(false);
                            }
                        });
                    }

                    function renameSingleEventConfigTabs() {
                        var nextNum = 1;
                        $('ul#single-event-config-tab li').each(function () {
                            var $element = $(this);
                            var uuid = $element.data('uuid');
                            if (uuid !== undefined) {
                                $element
                                    .find('a')
                                    .html(createSingleListItemText(nextNum, uuid));
                                nextNum++;
                            }
                        })
                    };
                    function createSingleListItemText(nextNum) {
                        var listItemText =
                            'S {{nextNum}}' +
                            '<button type="button" class="close" name="delete" data-form-type="single"' +
                            '       aria-label="Close">' +
                            '    <i class="fw fw-cancel"></i>' +
                            '</button>';
                        return listItemText.replaceAll('{{nextNum}}', nextNum);
                    };

                    function checkEndsWithSiddhi(string) {
                        return string.endsWith(".siddhi");
                    }
                }
            });

        return DeleteConfirmDialog;
    });
