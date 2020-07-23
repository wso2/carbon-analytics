/**
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash', 'jquery', 'constants', 'backbone', 'alerts', 'pagination'],
    function (require, _, $, Constants, Backbone, alerts, pagination) {
        var ErrorHandlerDialog = Backbone.View.extend(
            {
                PAYLOAD_STRING: 'PAYLOAD_STRING',

                initialize: function(options) {
                    this.app = options;
                    this.dialog_containers = $(_.get(options.config.dialog, 'container'));
                },

                show: function() {
                    this._errorHandlerModal.modal('show');
                },

                fetchSiddhiApps: function(serverHost, serverPort, username, password) {
                    var self = this;
                    var serviceUrl = self.app.config.services.errorHandler.endpoint;
                    $.ajax({
                        type: "GET",
                        contentType: "application/json; charset=utf-8",
                        headers: {
                            'serverHost': serverHost,
                            'serverPort': serverPort,
                            'username': username,
                            'password': password
                        },
                        url: serviceUrl + '/server/siddhi-apps',
                        async: false,
                        success: function (data) {
                            self.availableSiddhiApps = data;
                        },
                        error: function (e) {
                            alerts.error("Unable to fetch Siddhi apps");
                            throw "Unable to fetch Siddhi apps";
                        }
                    });
                },

                fetchErrorEntryDetails: function(errorEntryId, serverHost, serverPort, username, password) {
                    var self = this;
                    var serviceUrl = self.app.config.services.errorHandler.endpoint;
                    $.ajax({
                        type: "GET",
                        contentType: "application/json; charset=utf-8",
                        headers: {
                            'serverHost': serverHost,
                            'serverPort': serverPort,
                            'username': username,
                            'password': password
                        },
                        url: serviceUrl + '/error-entries/' + errorEntryId,
                        async: false,
                        success: function (data) {
                            self.renderDetailedErrorEntry(data);
                        },
                        error: function (e) {
                            alerts.error("Unable to fetch detailed info of the error");
                            throw "Unable to fetch detailed info of the error";
                        }
                    });
                },

                renderDiscardErrorEntriesConfirmation: function(siddhiAppName) {
                    var self = this;
                    var discardErrorEntriesConfirmationModal = $(
                        '<div class="modal fade" id="discardErrorEntriesConfirmationModal">' +
                        '<div class="modal-dialog">' +
                        '<div class="modal-content">' +
                        '<div class="modal-header">' +
                        "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                        "<i class=\"fw fw-cancel  about-dialog-close\"></i>" +
                        "</button>" +
                        '<h4 class="modal-title file-dialog-title">Confirm Discard All</h4>' +
                        '<hr class="style1">' +
                        '</div>' +
                        '<div id="discardErrorEntriesConfirmationModalBody" class="modal-body">' +
                        `Are you sure you want to discard all the errors of Siddhi app <b>${siddhiAppName}</b>?` +
                        '</div>' +
                        '<div class="modal-footer" style="padding-right: 20px; margin-right: 30px">' +
                        '<button id="discardAll" type="button" class="btn btn-primary">Discard All</button>' +
                        "<div class='divider'></div>" +
                        '<button type="button" class="btn btn-default" data-dismiss="modal">No</button>' +
                        '</div>' +
                        '</div>' +
                        '</div>' +
                        '</div>');
                    discardErrorEntriesConfirmationModal.find("#discardAll").click(function() {
                        self.discardErrorEntries(siddhiAppName, self.serverHost, self.serverPort, self.serverUsername,
                            self.serverPassword);
                        self.discardErrorEntriesConfirmation.modal('hide');
                    });
                    this.discardErrorEntriesConfirmation = discardErrorEntriesConfirmationModal;
                    this.discardErrorEntriesConfirmation.modal('show');
                },

                discardErrorEntries: function(siddhiAppName, serverHost, serverPort, username, password) {
                    var self = this;
                    var serviceUrl = self.app.config.services.errorHandler.endpoint;
                    $.ajax({
                        type: "DELETE",
                        contentType: "application/json; charset=utf-8",
                        headers: {
                            'serverHost': serverHost,
                            'serverPort': serverPort,
                            'username': username,
                            'password': password
                        },
                        url: serviceUrl + '/error-entries?siddhiApp=' + siddhiAppName,
                        async: false,
                        success: function () {
                            self.renderContent();
                        },
                        error: function (e) {
                            alerts.error("Unable to discard errors");
                            throw "Unable to discard errors";
                        }
                    });
                },

                renderDiscardErrorEntryConfirmation: function(errorEntry) {
                    var self = this;
                    var discardErrorEntryConfirmationModal = $(
                        '<div class="modal fade" id="discardErrorEntryConfirmationModal">' +
                        '<div class="modal-dialog">' +
                        '<div class="modal-content">' +
                        '<div class="modal-header">' +
                        "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                        "<i class=\"fw fw-cancel  about-dialog-close\"></i>" +
                        "</button>" +
                        '<h4 class="modal-title file-dialog-title">Confirm Discard</h4>' +
                        '<hr class="style1">' +
                        '</div>' +
                        '<div id="discardErrorEntryConfirmationModalBody" class="modal-body">' +
                        `Are you sure you want to discard the error with ID <b>${errorEntry.id}</b> ?` +
                        '</div>' +
                        '<div class="modal-footer" style="padding-right: 20px; margin-right: 30px">' +
                        '<button id="discard" type="button" class="btn btn-primary">Discard</button>' +
                        "<div class='divider'></div>" +
                        '<button type="button" class="btn btn-default" data-dismiss="modal">No</button>' +
                        '</div>' +
                        '</div>' +
                        '</div>' +
                        '</div>');
                    discardErrorEntryConfirmationModal.find("#discard").click(function() {
                        self.discardErrorEntry(errorEntry.id, self.serverHost, self.serverPort, self.serverUsername,
                            self.serverPassword);
                        self.discardErrorEntryConfirmation.modal('hide');
                    });
                    this.discardErrorEntryConfirmation = discardErrorEntryConfirmationModal;
                    this.discardErrorEntryConfirmation.modal('show');
                },

                discardErrorEntry: function(errorEntryId, serverHost, serverPort, username, password) {
                    var self = this;
                    var serviceUrl = self.app.config.services.errorHandler.endpoint;
                    $.ajax({
                        type: "DELETE",
                        contentType: "application/json; charset=utf-8",
                        headers: {
                            'serverHost': serverHost,
                            'serverPort': serverPort,
                            'username': username,
                            'password': password
                        },
                        url: serviceUrl + '/error-entries/' + errorEntryId,
                        async: false,
                        success: function () {
                            self.renderContent();
                        },
                        error: function (e) {
                            alerts.error("Unable to discard the error");
                            throw "Unable to discard the error";
                        }
                    });
                },

                replayErrorEntries: function(siddhiAppName, serverHost, serverPort, username, password) {
                    var self = this;
                    var serviceUrl = self.app.config.services.errorHandler.endpoint;
                    $.ajax({
                        type: "GET",
                        contentType: "application/json; charset=utf-8",
                        headers: {
                            'serverHost': serverHost,
                            'serverPort': serverPort,
                            'username': username,
                            'password': password
                        },
                        url: serviceUrl + '/error-entries?siddhiApp=' + siddhiAppName,
                        async: false,
                        success: function (data) {
                            // Initiate state for 'Replay all'.
                            self.replayAllState = {
                                errorEntries: data,
                                siddhiAppName: siddhiAppName,
                                currentErrorNumber: 0,
                                totalErrorsCount: data.length,
                            };
                            if (self.replayAllState.errorEntries) {
                                self.replayAllState.errorEntries.forEach(function (errorEntry) {
                                    self.replayAllState.currentErrorNumber++;
                                    self.updateReplayAllProgressDisplay();
                                    self.directReplay(errorEntry.id, serverHost, serverPort, username, password);
                                });
                                self.replayAllState = null;
                            }
                        },
                        error: function (e) {
                            alerts.error("Unable to replay")
                            throw "Unable to replay";
                        }
                    });
                    self.renderContent();
                },

                updateReplayAllProgressDisplay: function() {
                    var container = this._errorHandlerModal.find("#siddhiAppErrorEntriesBlock");
                    var containerHeight = container.height();
                    var containerWidth = container.width();
                    container.empty();
                    container.append(`<div style="height: ${containerHeight}; width: ${containerWidth}; ` +
                        'text-align: center; font-size: 25; padding-top: 150px">' +
                        '<i class="fw fw-wso2-logo fw-pulse fw-2x"></i>'+
                        `<h3>Replaying Error ${this.replayAllState.currentErrorNumber}`+
                        ` of ${this.replayAllState.totalErrorsCount}</h3>` +
                        `<h5 class="description">${this.replayAllState.siddhiAppName}</h5></div>`);
                },

                directReplay: function(errorEntryId, serverHost, serverPort, username, password,
                                       reRenderContentOnSuccess) {
                    var self = this;
                    var serviceUrl = self.app.config.services.errorHandler.endpoint;
                    $.ajax({
                        type: "GET",
                        contentType: "application/json; charset=utf-8",
                        headers: {
                            'serverHost': serverHost,
                            'serverPort': serverPort,
                            'username': username,
                            'password': password
                        },
                        url: serviceUrl + '/error-entries/' + errorEntryId,
                        async: false,
                        success: function (data) {
                            self.replay([data], serverHost, serverPort, username, password, reRenderContentOnSuccess);
                        },
                        error: function (e) {
                            alerts.error("Unable to replay");
                            throw "Unable to replay";
                        }
                    });
                },

                showLoadingDuringDirectReplay: function(container) {
                    var containerHeight = container.height();
                    var containerWidth = container.width();
                    container.empty();
                    container.append(`<div style="height: ${containerHeight}; width: ${containerWidth}; ` +
                        'text-align: center; font-size: 25">' +
                        '<i class="fw fw-wso2-logo fw-pulse fw-2x" style="padding-top: 70px"></i></div>');
                },

                replay: function(payload, serverHost, serverPort, username, password, reRenderContentOnSuccess) {
                    var self = this;
                    var serviceUrl = self.app.config.services.errorHandler.endpoint;
                    $.ajax({
                        type: "POST",
                        dataType: "json",
                        contentType: "application/json; charset=utf-8",
                        headers: {
                            'serverHost': serverHost,
                            'serverPort': serverPort,
                            'username': username,
                            'password': password
                        },
                        data: JSON.stringify(payload),
                        url: serviceUrl,
                        async: false,
                        success: function () {
                            if (reRenderContentOnSuccess) {
                                self.renderContent();
                            }
                        },
                        error: function (e) {
                            alerts.error("Unable to replay");
                            throw "Unable to replay";
                        }
                    });
                },

                renderPurgeSettings: function() {
                    var self = this;
                    var purgeSettingsModal = $('<div class="modal fade" id="purgeSettingsModal">' +
                        '<div class="modal-dialog">' +
                        '<div class="modal-content">' +
                        '<div class="modal-header">' +
                        "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                        "<i class=\"fw fw-cancel  about-dialog-close\"></i>" +
                        "</button>" +
                        '<h3 class="modal-title file-dialog-title">Purge Error Store</h3>' +
                        '<hr class="style1">' +
                        '</div>' +
                        '<div id="purgeSettingsModalModalBody" class="modal-body">' +
                        '</div>' +
                        '<div class="modal-footer" style="padding-right: 20px; margin-right: 30px">' +
                        '<button id="doPurge" type="button" class="btn btn-danger">' +
                        "Purge</button>" +
                        "<div class='divider'></div>" +
                        '<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>' +
                        '</div>' +
                        '</div>' +
                        '</div>' +
                        '</div>');
                    var modalBody = purgeSettingsModal.find("#purgeSettingsModalModalBody");
                    var purgeSettingsBlock =
                        $('<div class="error-handler-dialog-form-block" ' +
                            'style="margin-right: 30px; margin-left: 30px; width: auto; overflow: auto"></div>');
                    var purgeSettings = $('<div class="server-properties-clearfix"></div>');
                    purgeSettings.append('<div class="server-property">' +
                        '<label class="clearfix">Retention Period (Days)</label>' +
                        '<input type="number" id="retentionDays" min="0" placeholder="0"' +
                        ' class="configure-server-input"></div>');
                    purgeSettingsBlock.append(purgeSettings);
                    modalBody.append(purgeSettingsBlock);

                    // Disable the button initially.
                    purgeSettingsModal.find("#doPurge").click(function() {
                        var retentionDays = purgeSettings.find("#retentionDays").val();
                        self.renderPurgeConfirmation(retentionDays);
                        self.purgeSettings.modal('hide');
                    });
                    purgeSettingsModal.find("#doPurge").prop('disabled',true);

                    // Enable the button on input.
                    purgeSettings.find("#retentionDays").on('input', function() {
                        if (typeof this.value !== 'undefined' && this.value !== '') {
                            purgeSettingsModal.find("#doPurge").prop('disabled',false);
                        } else {
                            purgeSettingsModal.find("#doPurge").prop('disabled',true);
                        }
                    });

                    this.purgeSettings = purgeSettingsModal;
                    this.purgeSettings.modal('show');
                },

                renderPurgeConfirmation: function(retentionDays) {
                    var self = this;
                    var purgeConfirmationModal = $(
                        '<div class="modal fade" id="purgeConfirmationModal">' +
                        '<div class="modal-dialog">' +
                        '<div class="modal-content">' +
                        '<div class="modal-header">' +
                        "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                        "<i class=\"fw fw-cancel  about-dialog-close\"></i>" +
                        "</button>" +
                        '<h4 class="modal-title file-dialog-title">Confirm Purge</h4>' +
                        '<hr class="style1">' +
                        '</div>' +
                        '<div id="purgeConfirmationModalBody" class="modal-body">' +
                        'Are you sure you want to purge the error store with a retention period of ' +
                        ` <b>${retentionDays} Days</b>?` +
                        '</div>' +
                        '<div class="modal-footer" style="padding-right: 20px; margin-right: 30px">' +
                        '<button id="purge" type="button" class="btn btn-primary">Purge</button>' +
                        "<div class='divider'></div>" +
                        '<button type="button" class="btn btn-default" data-dismiss="modal">No</button>' +
                        '</div>' +
                        '</div>' +
                        '</div>' +
                        '</div>');
                    purgeConfirmationModal.find("#purge").click(function() {
                        self.purgeErrorStore(retentionDays, self.serverHost, self.serverPort, self.serverUsername,
                            self.serverPassword);
                        self.purgeConfirmation.modal('hide');
                    });
                    this.purgeConfirmation = purgeConfirmationModal;
                    this.purgeConfirmation.modal('show');
                },

                purgeErrorStore: function(retentionDays, serverHost, serverPort, username, password) {
                    var self = this;
                    var serviceUrl = self.app.config.services.errorHandler.endpoint;
                    $.ajax({
                        type: "DELETE",
                        contentType: "application/json; charset=utf-8",
                        headers: {
                            'serverHost': serverHost,
                            'serverPort': serverPort,
                            'username': username,
                            'password': password
                        },
                        url: serviceUrl + '/error-entries?retentionDays=' + retentionDays,
                        async: false,
                        success: function () {
                            self.renderContent();
                        },
                        error: function (e) {
                            alerts.error("Unable to purge the error store");
                            throw "Unable to purge the error store";
                        }
                    });
                },

                renderServerConfigurations: function() {
                    var self = this;
                    var serverConfigurationsModal = $(
                        '<div class="modal fade" id="serverConfigurationsModal">' +
                        '<div class="modal-dialog">' +
                        '<div class="modal-content">' +
                        '<div class="modal-header">' +
                        "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                        "<i class=\"fw fw-cancel  about-dialog-close\"></i>" +
                        "</button>" +
                        '<h3 class="modal-title file-dialog-title">Server Configurations</h3>' +
                        '<hr class="style1">' +
                        '</div>' +
                        '<div id="serverConfigurationsModalBody" class="modal-body">' +
                        '</div>' +
                        '<div class="modal-footer" style="padding-right: 20px; margin-right: 30px">' +
                        '<button id="applyWorkerConfigurations" type="button" class="btn btn-primary">' +
                        "Connect</button>" +
                        "<div class='divider'></div>" +
                        '<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>' +
                        '</div>' +
                        '</div>' +
                        '</div>' +
                        '</div>');
                    var modalBody = serverConfigurationsModal.find("#serverConfigurationsModalBody");
                    var serverPropertiesBlock =
                        $('<div class="error-handler-dialog-form-block" ' +
                            'style="margin-right: 30px; margin-left: 30px; width: auto; overflow: auto"></div>');
                    var serverProperties = $('<div class="server-properties-clearfix"></div>');
                    serverProperties.append('<div class="server-property">' +
                        '<label class="clearfix">Host</label>' +
                        '<input type="text" id="serverHost" placeholder="localhost" class="configure-server-input" ' +
                        'value="' + (this.serverHost || '') + '">' +
                        '</div>');
                    serverProperties.append('<div class="server-property">' +
                        '<label class="clearfix">Port</label>' +
                        '<input type="text" id="serverPort" placeholder="9443" class="configure-server-input" ' +
                        'value="' + (this.serverPort || '') + '">' +
                        '</div>');
                    serverProperties.append('<div class="server-property">' +
                        '<label class="clearfix">Username</label>' +
                        '<input type="text" id="serverUsername" placeholder="admin" class="configure-server-input" ' +
                        'value="' + (this.serverUsername || '') + '">' +
                        '</div>');
                    serverProperties.append('<div class="server-property">' +
                        '<label class="clearfix">Password</label>' +
                        '<input type="password" id="serverPassword" placeholder="admin"' +
                        ' class="configure-server-input" value="' + (this.serverPassword || '') + '">' +
                        '</div>');
                    serverPropertiesBlock.append(serverProperties);
                    modalBody.append(serverPropertiesBlock);

                    serverConfigurationsModal.find("#applyWorkerConfigurations").click(function() {
                        var serverHost = serverProperties.find("#serverHost").val();
                        var serverPort = serverProperties.find("#serverPort").val();
                        var serverUsername = serverProperties.find("#serverUsername").val();
                        var serverPassword = serverProperties.find("#serverPassword").val();

                        var isRequiredPropertyAbsent = false;
                        [serverHost, serverPort, serverUsername, serverPassword].forEach(function (requiredProperty) {
                            if (typeof requiredProperty === 'undefined' || requiredProperty === '') {
                                isRequiredPropertyAbsent = true;
                            }
                        });
                        if (!isRequiredPropertyAbsent) {
                            self.serverHost = serverHost;
                            self.serverPort = serverPort;
                            self.serverUsername = serverUsername;
                            self.serverPassword = serverPassword;
                            self.isServerConfigured = true;
                            self.renderContent();
                            self.serverConfigurations.modal('hide');
                        } else {
                            alerts.error("Please provide values for all the properties of the server");
                        }
                    });

                    this.serverConfigurations = serverConfigurationsModal;
                    this.serverConfigurations.modal('show');
                },

                renderServerDetails: function(errorContainer) {
                    var serverDetails = this.isServerConfigured ? this.generateServerConfiguredDisplay() :
                        this.generateServerNotConfiguredDisplay();
                    errorContainer.append(serverDetails);
                },

                generateServerConfiguredDisplay: function() {
                    var self = this;
                    // Fetch number of total error entries from the error store.
                    var serviceUrl = self.app.config.services.errorHandler.endpoint;
                    var totalErrorEntries = -1;
                    $.ajax({
                        type: "GET",
                        contentType: "application/json; charset=utf-8",
                        headers: {
                            'serverHost': self.serverHost,
                            'serverPort': self.serverPort,
                            'username': self.serverUsername,
                            'password': self.serverPassword
                        },
                        url: serviceUrl + '/error-entries/count',
                        async: false,
                        success: function (data) {
                            totalErrorEntries = data.entriesCount;
                        },
                        error: function (e) {
                            alerts.error("Unable to fetch error count");
                            throw "Unable to fetch error count";
                        }
                    });
                    var serverConfiguredDisplay = $('<div></div>');
                    if (totalErrorEntries > -1) {
                        serverConfiguredDisplay.append(`<h4>${totalErrorEntries} Errors found</h4>`);
                    }
                    serverConfiguredDisplay.append(
                        `<h5 class="description">${self.serverHost + ':' + self.serverPort}&nbsp;&nbsp;`+
                        `<a id="configureServer"><i class="fw fw-settings"></i></a></h5>`);

                    serverConfiguredDisplay.find("#configureServer").click(function() {
                        self.renderServerConfigurations();
                    });
                    return serverConfiguredDisplay;
                },

                generateServerNotConfiguredDisplay: function() {
                    var self = this;
                    var serverNotConfiguredDisplay = $('<div>' +
                        '<h4>SI Server has not been specified</h4>' +
                        '<button id="configureServer" type="button" class="btn btn-primary">' +
                        'Connect to Server</button>' +
                        '</div>');
                    serverNotConfiguredDisplay.find("#configureServer").click(function() {
                        self.renderServerConfigurations();
                    });
                    return serverNotConfiguredDisplay;
                },

                renderSiddhiAppSelection: function(siddhiAppList, errorContainer) {
                    var self = this;
                    var siddhiAppSelection = $('<div></div>');
                    if (siddhiAppList) {
                        siddhiAppSelection.append('<h4>Siddhi app</h4>');
                        var selectInput =
                            $('<select name="siddhiApps" id="siddhiAppSelection" class="form-control"' +
                                'style="margin-bottom:10px"></select>');
                        siddhiAppList.forEach(function (siddhiApp) {
                            if (self.selectedSiddhiApp && self.selectedSiddhiApp === siddhiApp) {
                                selectInput.append('<option value="' + siddhiApp + '" selected>' +
                                    siddhiApp + '</option>');
                            } else {
                                selectInput.append('<option value="' + siddhiApp + '">' + siddhiApp + '</option>');
                            }
                        })
                        siddhiAppSelection.append(selectInput);
                        siddhiAppSelection.append(
                            "<button id='getErrorEntries' type='button' class='btn btn-primary'>Fetch</button>");
                        siddhiAppSelection.append("<div class='divider'></div>");
                        this.discardAllButton =
                            $("<button id='discardAll' type='button' class='btn btn-default'>Discard All</button>");
                        this.replayAllButton =
                            $("<button id='replayAll' type='button' class='btn btn-default'>Replay All</button>");
                        siddhiAppSelection.append(this.discardAllButton);
                        siddhiAppSelection.append("<div class='divider'></div>");
                        siddhiAppSelection.append(this.replayAllButton);
                        // Hide the buttons initially. These will be shown when at least one error entry is available.
                        this.discardAllButton.hide();
                        this.replayAllButton.hide();

                        siddhiAppSelection.find("select").change(function() {
                            self.selectedSiddhiApp = this.value;
                            $(this).parent().find("select").find("option[value='" + this.value + "']")
                                .attr('selected', 'selected')
                        });
                        siddhiAppSelection.find("#getErrorEntries").click(function() {
                            var siddhiAppName = $(this).parent().find("select").get(0).value;
                            self.selectedSiddhiApp = siddhiAppName;
                            self.renderContent();
                        });
                        siddhiAppSelection.find("#discardAll").click(function() {
                            var siddhiAppName = $(this).parent().find("select").get(0).value;
                            self.selectedSiddhiApp = siddhiAppName;
                            self.renderDiscardErrorEntriesConfirmation(siddhiAppName);
                        });
                        siddhiAppSelection.find("#replayAll").click(function() {
                            var siddhiAppName = $(this).parent().find("select").get(0).value;
                            self.selectedSiddhiApp = siddhiAppName;
                            self.replayErrorEntries(siddhiAppName, self.serverHost, self.serverPort,
                                self.serverUsername, self.serverPassword);
                        });
                    } else {
                        siddhiAppSelection.append("<h4 class='description'>No Siddhi apps found</h4>");
                    }
                    errorContainer.append(siddhiAppSelection);
                },

                renderErrorEntries: function(errorContainer) {
                    var self = this;
                    errorDisplay = $('<div id="errorEntriesContainer"></div>');
                    if (this.errorEntries) {
                        this.errorEntries.forEach(function (errorEntry) {
                            errorDisplay.append(self.renderErrorEntry(errorEntry));
                        });
                    }
                    errorContainer.append(errorDisplay);
                },

                renderErrorEntry: function(errorEntry) {
                    var self = this;
                    var errorEntryId = errorEntry.id;
                    var errorEntryElement = $('<div class="error-entry-container">' +
                        `<h4>${errorEntry.streamName}</h4>` +
                        '<span class="error-label" title="Event Type" style="margin-right: 10px;">'+
                        `${errorEntry.eventType}</span>` +
                        `<span class="error-label" title="Error Occurrence">${errorEntry.errorOccurrence}</span>` +
                        this.getRenderableCause(errorEntry, true) +
                        `<p class="description">${this.getReadableTime(errorEntry.timestamp)}` +
                        ` &nbsp; ID: ${errorEntryId}</p><br/>` +
                        `<button id='replay_${errorEntryId}' type='button' class='btn btn-default'>Replay</button>` +
                        "<div class='divider'></div>" +
                        `<button id='detailedInfo_${errorEntryId}' type='button' class='btn btn-default'>` +
                        `Detailed Info</button><div class='divider'></div>` +
                        `<button id='discard_${errorEntryId}' type='button' class='btn btn-default'>Discard</button>` +
                        `</div>`);

                    errorEntryElement.find("#replay_" + errorEntryId).click(function() {
                        self.showLoadingDuringDirectReplay($(this).parent());
                        self.directReplay(errorEntry.id, self.serverHost, self.serverPort, self.serverUsername,
                            self.serverPassword, true);
                    });
                    errorEntryElement.find("#detailedInfo_" + errorEntryId).click(function() {
                        self.fetchErrorEntryDetails(errorEntry.id, self.serverHost, self.serverPort,
                            self.serverUsername, self.serverPassword);
                    });
                    errorEntryElement.find("#discard_" + errorEntryId).click(function() {
                        self.renderDiscardErrorEntryConfirmation(errorEntry);
                    });
                    return errorEntryElement;
                },

                renderDetailedErrorEntry: function(wrappedErrorEntry) {
                    var errorEntry = wrappedErrorEntry.errorEntry;
                    var detailedErrorEntryModal = $(
                        '<div class="modal fade" id="' + errorEntry.id + '">' +
                        '<div class="modal-dialog">' +
                        '<div class="modal-content">' +
                        '<div class="modal-header">' +
                        "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                        "<i class=\"fw fw-cancel  about-dialog-close\"></i>" +
                        "</button>" +
                        '<h3 class="modal-title file-dialog-title" id="heading">Error Entry</h3>' +
                        '<hr class="style1">' +
                        '</div>' +
                        '<div id="detailedErrorModalBody" class="modal-body">' +
                        '</div>' +
                        '<div class="modal-footer">' +
                        '<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>' +
                        '</div>' +
                        '</div>' +
                        '</div>' +
                        '</div>');

                    var modalBody = detailedErrorEntryModal.find("#detailedErrorModalBody");

                    modalBody.append('<div>' +
                        '<h4>' + errorEntry.streamName + '</h4>' +
                        `<span class="error-label" title="Event Type" style="margin-right: 10px;">` +
                        `${errorEntry.eventType}</span>` +
                        `<span class="error-label" title="Error Occurrence">${errorEntry.errorOccurrence}</span>` +
                        this.getRenderableCause(errorEntry) +
                        `<p class="description">${this.getReadableTime(errorEntry.timestamp)}` +
                        ` &nbsp; ID: ${errorEntry.id}</p></div>`);
                    modalBody.append('<br/>');
                    modalBody.append(this.renderReplayButtonInDetailedErrorEntry(wrappedErrorEntry));
                    modalBody.append('<br/>');
                    if (!wrappedErrorEntry.isPayloadModifiable) {
                        // Payload is not modifiable. Show the original payload, in case if the user wants to refer.
                        modalBody.append(this.renderOriginalPayload(errorEntry));
                        modalBody.append('<br/>');
                    }
                    modalBody.append(this.renderStackTrace(errorEntry));

                    this.detailedErrorEntry = detailedErrorEntryModal;
                    this.detailedErrorEntry.modal('show');
                },

                getRenderableCause: function(errorEntry, shouldShorten) {
                    if (errorEntry.cause) {
                        if (shouldShorten && errorEntry.cause.length > 100) {
                            return '<h5 class="error-cause">' + errorEntry.cause.substring(0, 100) + '...</h5>';
                        }
                        return `<h5 class="error-cause">${errorEntry.cause}</h5>`;
                    } else {
                        return '<h5 class="error-cause">Cause Not Available</h5>';
                    }
                },

                getReadableTime: function(timestamp) {
                    return new Date(timestamp).toLocaleString();
                },

                renderReplayButtonInDetailedErrorEntry: function(wrappedErrorEntry) {
                    var self = this;
                    var replay = $('<div></div>');
                    var replayableWrappedErrorEntry = wrappedErrorEntry;
                    if (wrappedErrorEntry.isPayloadModifiable) {
                        // Payload is not modifiable.
                        replay.append('<textarea id="eventPayload" rows="4" cols="40" class="payload-content">' +
                            wrappedErrorEntry.modifiablePayloadString + '</textarea>');
                        replay.append('<br/>');
                    }
                    replay.append("<button id='replay' type='button' class='btn btn-primary'>Replay</button>");

                    replay.find("#replay").click(function() {
                        if (wrappedErrorEntry.isPayloadModifiable) {
                            replayableWrappedErrorEntry.modifiablePayloadString = replay.find("#eventPayload").val();
                        }
                        self.replay([replayableWrappedErrorEntry], self.serverHost, self.serverPort,
                            self.serverUsername, self.serverPassword, true);
                        self.detailedErrorEntry.modal('hide');
                    });
                    return replay;
                },

                renderOriginalPayload: function(errorEntry) {
                    var originalPayload = $('<div><h4>Original Payload</h4></div>');
                    if (errorEntry.originalPayload) {
                        originalPayload.append('<div class="payload-content">' + errorEntry.originalPayload + '</div>');
                    } else {
                        originalPayload.append('<div>Original payload of the event is not available</div>');
                    }
                    return originalPayload;
                },

                renderStackTrace: function(errorEntry) {
                    var stackTrace = $('<div><h4>Stack Trace</h4></div>');
                    if (errorEntry.stackTrace) {
                        stackTrace.append('<div class="stack-trace-content">' + errorEntry.stackTrace + '</div>');
                    } else {
                        stackTrace.append('<div>Stacktrace Not Available</div>');
                    }
                    return stackTrace;
                },

                renderContent: function(errorContainer) {
                    var self = this;
                    var container = errorContainer || this._errorHandlerModal.find("div").filter("#errorContainer");
                    if (this.isServerConfigured) {
                        this.fetchSiddhiApps(
                            this.serverHost, this.serverPort, this.serverUsername, this.serverPassword);
                    }
                    container.empty();
                    var serverDetailsBlock =
                        $('<div id="serverDetailsBlock" class="error-handler-dialog-form-block"></div>');
                    this.renderServerDetails(serverDetailsBlock);
                    var siddhiAppErrorEntriesBlock =
                        $('<div id="siddhiAppErrorEntriesBlock" class="error-handler-dialog-form-block" ' +
                        'style="height: 48%; overflow: auto;"></div>');
                    var siddhiAppSelection = $('<div style="margin-bottom: 20px"></div>');
                    this.renderSiddhiAppSelection(this.availableSiddhiApps, siddhiAppSelection);
                    siddhiAppErrorEntriesBlock.append(siddhiAppSelection);

                    // Cleanup pagination elements during a re-render, or initialize them.
                    if (this.errorEntriesDiv && this.paginator) {
                        this.errorEntriesDiv.empty();
                        this.paginator.empty();
                    } else {
                        this.errorEntriesDiv =
                            $('<div id="paginatedErrorEntries" style="overflow:auto; height:100%"></div>');
                        this.paginator = $('<div id="paginator"></div>');
                    }

                    if (this.isServerConfigured && this.selectedSiddhiApp) {
                        this.paginator.pagination({
                            dataSource: function(done) {
                                var serviceUrl = self.app.config.services.errorHandler.endpoint;
                                $.ajax({
                                    type: 'GET',
                                    contentType: "application/json; charset=utf-8",
                                    headers: {
                                        'serverHost': self.serverHost,
                                        'serverPort': self.serverPort,
                                        'username': self.serverUsername,
                                        'password': self.serverPassword,
                                    },
                                    url: serviceUrl + '/error-entries?siddhiApp=' + self.selectedSiddhiApp,
                                    success: function(response) {
                                        done(response);
                                    }
                                });
                            },
                            alias: {
                                pageNumber: 'offset',
                                pageSize: 'limit'
                            },
                            pageSize: 10,
                            callback: function(data, pagination) {
                                self.errorEntries = data;
                                self.paginationData = pagination;
                                self.errorEntriesDiv.empty();
                                self.renderErrorEntries(self.errorEntriesDiv);
                                if (data.length > 0) {
                                    self.discardAllButton.show();
                                    self.replayAllButton.show();
                                }
                            }
                        });
                    }
                    siddhiAppErrorEntriesBlock.append(this.errorEntriesDiv);
                    siddhiAppErrorEntriesBlock.append(this.paginator);

                    container.append(serverDetailsBlock);
                    container.append(siddhiAppErrorEntriesBlock);
                    if (this.isServerConfigured) {
                        var purgeBlock =
                            $('<div id="purgeBlock" class="error-handler-dialog-form-block" style="margin-bottom: 0">' +
                                '</div>');
                        purgeBlock.append(
                            '<button id="purgeErrorStore" type="button" class="btn btn-danger">Purge</button>');
                        purgeBlock.find("#purgeErrorStore").click(function() {
                            self.renderPurgeSettings();
                        });
                        container.append(purgeBlock);
                    }
                },

                render: function() {
                    if (!_.isNil(this._errorHandlerModal)) {
                        this._errorHandlerModal.remove();
                    }

                    var errorHandlerModalHolder = $(
                        "<div class='modal fade' id='errorHandlerModal' tabindex='-1' role='dialog' " +
                        "aria-hidden='true'>" + "<div class='modal-dialog file-dialog' role='document'>" +
                        "<div class='modal-content' id='sampleDialog'>" +
                        "<div class='modal-header'>" +
                        "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                        "<i class=\"fw fw-cancel  about-dialog-close\"></i>" +
                        "</button>" +
                        "<h4 class='modal-title file-dialog-title'>Error Store Explorer</h4>" +
                        "<hr class='style1'>" +
                        "</div>" +
                        "<div class='modal-body'>" +
                        "<div class='container-fluid'>" +
                        "<form class='form-horizontal' onsubmit='return false'>" +
                        "<div class='form-group'>" +
                        "<div id='errorContainer'></div>" +
                        "</div>" +
                        "<div class='form-group'>" +
                        "<div class='file-dialog-form-btn'>" +
                        "<button type='button' class='btn btn-default' data-dismiss='modal'>cancel</button>" +
                        "</div>" +
                        "</div>" +
                        "</form>" +
                        "<div id='errorHandlerErrorModal' class='alert alert-danger'>" +
                        "<strong>Error!</strong>Something went wrong." +
                        "</div>" +
                        "</div>" +
                        "</div>" +
                        "</div>" +
                        "</div>" +
                        "</div>");
                    var errorHandlerModal = errorHandlerModalHolder.filter("#errorHandlerModal");
                    var errorHandlerErrorModal = errorHandlerModalHolder.find("#errorHandlerErrorModal");
                    var errorContainer = errorHandlerModal.find("div").filter("#errorContainer");
                    this.renderContent(errorContainer);
                    $(this.dialog_containers).append(errorHandlerModal);
                    errorHandlerErrorModal.hide();
                    this._errorHandlerModal = errorHandlerModal;
                },
            });
        return ErrorHandlerDialog;
    });
