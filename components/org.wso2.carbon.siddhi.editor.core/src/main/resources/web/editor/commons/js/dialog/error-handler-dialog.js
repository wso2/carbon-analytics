/**
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash', 'jquery', 'constants', 'backbone', 'alerts'],
    function (require, _, $, Constants, Backbone, alerts) {
        var ErrorHandlerDialog = Backbone.View.extend(
            {
                PAYLOAD_STRING: 'PAYLOAD_STRING',

                initialize: function(options) {
                    this.app = options;
                    this.dialog_containers = $(_.get(options.config.dialog, 'container'));
                    console.log("Finalize available workers")
                },

                show: function() {
                    this._errorHandlerModal.modal('show');
                },

                fetchSiddhiApps: function(serverHost, serverPort, username, password) {
                    // TODO test: server down, error handler is requested in menu
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
                            console.log("Siddhi apps Response received", data)
                            self.availableSiddhiApps = data;
                        },
                        error: function (e) {
                            alerts.error("Unable to fetch Siddhi apps." +
                                "Please see the editor console for further information.")
                            throw "Unable to read errors";
                        }
                    });
                },

                // TODO this will be replaced with pagination. So don't worry about duplicate code with replay all
                fetchErrorEntries: function(siddhiAppName, serverHost, serverPort, username, password) {
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
                        // TODO for now no limit and offset. Need to have it
                        url: serviceUrl + '/error-entries?siddhiApp=' + siddhiAppName,
                        async: false,
                        success: function (data) {
                            console.log("Erroneous Events Response received", data)
                            self.errorEntries = data;
                            self.renderContent();
                        },
                        error: function (e) {
                            alerts.error("Unable to fetch Erroneous Events." +
                                "Please see the editor console for further information.")
                            throw "Unable to read errors";
                        }
                    });
                },

                // TODO improve name?
                // TODO rename this should fetch after param
                directReplay: function(errorEntryId, serverHost, serverPort, username, password,
                                       shouldFetchAfterReplay) {
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
                            console.log("Detailed Error Entry received. Gonna replay", data)
                            self.replay([data], serverHost, serverPort, username, password,
                                shouldFetchAfterReplay);
                        },
                        error: function (e) {
                            alerts.error("Unable to fetch Detailed error entry"); // TODO improve
                            throw "Unable to read errors";
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
                            console.log("Detailed Error Entry received", data)
                            self.renderDetailedErrorEntry(data);
                        },
                        error: function (e) {
                            alerts.error("Unable to fetch Detailed error entry"); // TODO improve
                            throw "Unable to read errors";
                        }
                    });
                },

                replay: function(payload, serverHost, serverPort, username, password, shouldFetchAfterReplay) {
                    var self = this; // TODO rename this should fetch after param
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
                            console.log("Replay Response received")
                            // Re-fetch after discarding error
                            if (shouldFetchAfterReplay) {
                                self.fetchErrorEntries(self.selectedSiddhiApp, serverHost, serverPort, username,
                                    password);
                            }
                        },
                        error: function (e) {
                            alerts.error("Unable to fetch Erroneous Events." +
                                "Please see the editor console for further information.")
                            throw "Unable to read errors";
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
                        // TODO for now no limit and offset. Need to have it
                        url: serviceUrl + '/error-entries?siddhiApp=' + siddhiAppName,
                        async: false,
                        success: function (data) {
                            console.log("Erroneous Events Response received", data)
                            // Initiate state for 'Replay all'
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
                            alerts.error("Unable to fetch Erroneous Events." +
                                "Please see the editor console for further information.")
                            throw "Unable to read errors";
                        }
                    });
                    self.fetchErrorEntries(self.selectedSiddhiApp, serverHost, serverPort, username, password);
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
                        success: function (data) {
                            console.log("Discarded entry with id: " + errorEntryId, data)
                            self.fetchErrorEntries(self.selectedSiddhiApp, serverHost, serverPort, username, password);
                        },
                        error: function (e) {
                            alerts.error("Unable to fetch Detailed error entry"); // TODO improve
                            throw "Unable to read errors";
                        }
                    });
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
                        success: function (data) {
                            console.log("Discarded entries for Siddhi app: " + siddhiAppName, data)
                            // TODO is this behaviour correct? or should I optimize it
                            self.fetchErrorEntries(self.selectedSiddhiApp, serverHost, serverPort, username, password);
                        },
                        error: function (e) {
                            alerts.error("Unable to fetch Detailed error entry"); // TODO improve
                            throw "Unable to read errors";
                        }
                    });
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
                        success: function (data) {
                            console.log("Purged error store with retentionDays: " + retentionDays, data)
                            self.fetchErrorEntries(self.selectedSiddhiApp, serverHost, serverPort, username, password);
                        },
                        error: function (e) {
                            alerts.error("Unable to purge the error store."); // TODO improve
                            throw "Unable to purge the error store";
                        }
                    });
                },

                renderPurgeSettings: function() {
                    var self = this;
                    var purgeSettingsModal = $(
                        '<div class="modal fade" id="purgeSettingsModal">' +
                        '<div class="modal-dialog">' +
                        '<div class="modal-content">' +
                        '<div class="modal-header">' +
                        "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                        "<i class=\"fw fw-cancel  about-dialog-close\"></i>" +
                        "</button>" +
                        '<h2 class="modal-title file-dialog-title">' +
                        'Purge Error Store' +
                        '</h2>' +
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

                    // TODO deactivate button & validate
                    purgeSettingsModal.find("#doPurge").click(function() {
                        var retentionDays = purgeSettings.find("#retentionDays").val();
                        self.purgeErrorStore(retentionDays, self.serverHost, self.serverPort, self.serverUsername,
                            self.serverPassword);
                    });

                    this.purgeSettings = purgeSettingsModal;
                    this.purgeSettings.modal('show');
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
                        '<h2 class="modal-title file-dialog-title">' +
                        'Server Configurations' +
                        '</h2>' +
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

                    // TODO when giving a wrong host, gets stuck. look into
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

                    // TODO deactivate button & validate
                    serverConfigurationsModal.find("#applyWorkerConfigurations").click(function() {
                        self.serverHost = serverProperties.find("#serverHost").val();
                        self.serverPort = serverProperties.find("#serverPort").val();
                        self.serverUsername = serverProperties.find("#serverUsername").val();
                        self.serverPassword = serverProperties.find("#serverPassword").val();
                        self.isServerConfigured = true;
                        self.renderContent();
                    });

                    this.serverConfigurations = serverConfigurationsModal;
                    this.serverConfigurations.modal('show');
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
                            console.log("Count received", data)
                            totalErrorEntries = data.entriesCount;
                        },
                        error: function (e) {
                            alerts.error("Unable to fetch Detailed error entry"); // TODO improve
                            throw "Unable to read errors";
                        }
                    });
                    var serverConfiguredDisplay = $('<div></div>');
                    if (totalErrorEntries > -1) {
                        serverConfiguredDisplay.append(`<h4>${totalErrorEntries} Errors found</h4>`);
                    }
                    serverConfiguredDisplay.append(
                        `<h5 class="description">${self.serverHost + ':' + self.serverPort}&nbsp;&nbsp;`+
                        `<a id="configureServer"><i class="fw fw-settings"></i></a></h5>`);
                    serverConfiguredDisplay.append(
                        '<button id="purgeErrorStore" type="button" class="btn btn-danger">Purge</button>');

                    serverConfiguredDisplay.find("#configureServer").click(function() { // TODO deactivate button & validate
                        self.renderServerConfigurations();
                    });
                    serverConfiguredDisplay.find("#purgeErrorStore").click(function() { // TODO deactivate button & validate?
                        self.renderPurgeSettings();
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
                    serverNotConfiguredDisplay.find("#configureServer").click(function() { // TODO deactivate button & validate
                        self.renderServerConfigurations();
                    });
                    return serverNotConfiguredDisplay;
                },

                renderServerDetails: function(errorContainer) { // TODO this is about to go away
                    var self = this;
                    var serverDetails = self.isServerConfigured ? this.generateServerConfiguredDisplay() :
                        this.generateServerNotConfiguredDisplay();
                    errorContainer.append(serverDetails);
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
                        if (this.errorEntries && this.errorEntries.length > 0) {
                            siddhiAppSelection.append("<div class='divider'></div>");
                            siddhiAppSelection.append(
                                "<button id='discardAll' type='button' class='btn btn-default'>Discard All</button>");
                            siddhiAppSelection.append("<div class='divider'></div>");
                            siddhiAppSelection.append(
                                "<button id='replayAll' type='button' class='btn btn-default'>Replay All</button>");
                        }

                        siddhiAppSelection.find("select").change(function() {
                            self.selectedSiddhiApp = this.value;
                            $(this).parent().find("select").find("option[value='" + this.value + "']")
                                .attr('selected', 'selected')
                        });

                        siddhiAppSelection.find("#getErrorEntries").click(function() {
                            var siddhiAppName = $(this).parent().find("select").get(0).value;
                            self.selectedSiddhiApp = siddhiAppName;
                            self.fetchErrorEntries(siddhiAppName, self.serverHost, self.serverPort, self.serverUsername,
                                self.serverPassword);
                        });

                        siddhiAppSelection.find("#discardAll").click(function() {
                            var siddhiAppName = $(this).parent().find("select").get(0).value;
                            self.selectedSiddhiApp = siddhiAppName;
                            self.discardErrorEntries(siddhiAppName, self.serverHost, self.serverPort,
                                self.serverUsername, self.serverPassword);
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

                renderOriginalPayload: function(errorEntry) { // TODO uneditable original payload for occurrences other than before source mapping
                    var originalPayload = $('<div><h4>Original Payload</h4></div>');
                    if (errorEntry.originalPayload) {
                        originalPayload.append('<div class="payload-content">' + errorEntry.originalPayload + '</div>');
                    } else {
                        originalPayload.append('<div>Original payload of the event is not available</div>');
                    }
                    return originalPayload;
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

                    replay.find("#replay").click(function() { // TODO disable if server not configured
                        if (wrappedErrorEntry.isPayloadModifiable) {
                            // TODO make sure that no worries about mutating the original object
                            replayableWrappedErrorEntry.modifiablePayloadString = replay.find("#eventPayload").val();
                        }
                        self.replay([replayableWrappedErrorEntry], self.serverHost, self.serverPort,
                            self.serverUsername, self.serverPassword, true);
                    });
                    return replay;
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
                        '<h2 class="modal-title file-dialog-title" id="heading">' + // TODO finalize this tag
                        'Error Entry' +
                        '</h2>' +
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

                    // TODO add all the details
                    modalBody.append('<div>' +
                        '<h4>' + errorEntry.streamName + '</h4>' +
                        `<span class="error-label" style="margin-right: 10px;">${errorEntry.eventType}</span>` +
                        `<span class="error-label">${errorEntry.errorOccurrence}</span>` +
                        this.getRenderableCause(errorEntry) +
                        `<p class="description">${this.getReadableTime(errorEntry.timestamp)}` +
                        ` &nbsp; ID: ${errorEntry.id}</p>` +
                        '</div>');
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

                renderErrorEntry: function(errorEntry) {
                    var self = this;

                    // TODO styling is hardcoded. This should use standard classes
                    var errorEntryElement = $('<div class="error-entry-container">' +
                        `<h4>${errorEntry.streamName}</h4>` +
                        `<span class="error-label" style="margin-right: 10px;">${errorEntry.eventType}</span>` +
                        `<span class="error-label">${errorEntry.errorOccurrence}</span>` +
                        this.getRenderableCause(errorEntry, true) +
                        `<p class="description">${this.getReadableTime(errorEntry.timestamp)}` +
                        ` &nbsp; ID: ${errorEntry.id}</p>` +
                        '<br/>' +
                        "<button id='replay' type='button' class='btn btn-default'>Replay</button>" +
                        "<div class='divider'></div>" +
                        "<button id='detailedInfo' type='button' class='btn btn-default'>Detailed Info</button>" +
                        "<div class='divider'></div>" +
                        "<button id='discard' type='button' class='btn btn-default'>Discard</button>" +
                        '</div>');

                    errorEntryElement.find("#replay").click(function() { // TODO disable if server not configured
                        self.showLoadingDuringDirectReplay($(this).parent());
                        self.directReplay(errorEntry.id, self.serverHost, self.serverPort, self.serverUsername,
                            self.serverPassword, true);
                    });

                    errorEntryElement.find("#detailedInfo").click(function() {
                        self.fetchErrorEntryDetails(errorEntry.id, self.serverHost, self.serverPort,
                            self.serverUsername, self.serverPassword);
                    });

                    errorEntryElement.find("#discard").click(function() { // TODO testing now
                        self.discardErrorEntry(errorEntry.id, self.serverHost, self.serverPort, self.serverUsername,
                            self.serverPassword);
                    });

                    return errorEntryElement;
                },

                showLoadingDuringDirectReplay: function(container) {
                    var containerHeight = container.height();
                    var containerWidth = container.width();
                    container.empty();
                    container.append(`<div style="height: ${containerHeight}; width: ${containerWidth}; ` +
                        'text-align: center; font-size: 25">' +
                        '<i class="fw fw-wso2-logo fw-pulse fw-2x" style="padding-top: 70px"></i></div>');
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
                        `<h5 class="description">${this.replayAllState.siddhiAppName}</h5>` +
                        '</div>');
                },

                renderContent: function(errorContainer) {
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
                        'style="height: 50%; margin-bottom: 0; overflow: auto;"></div>');
                    var siddhiAppSelection = $('<div style="margin-bottom: 20px"></div>');
                    this.renderSiddhiAppSelection(this.availableSiddhiApps, siddhiAppSelection);
                    siddhiAppErrorEntriesBlock.append(siddhiAppSelection);
                    var errorEntries = $('<div style="overflow:auto; height:100%"></div>');
                    this.renderErrorEntries(errorEntries);

                    siddhiAppErrorEntriesBlock.append(errorEntries);
                    container.append(serverDetailsBlock);
                    container.append(siddhiAppErrorEntriesBlock);
                },

                render: function() {
                    console.log("render")
                    var self = this;
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
                        "<h4 class='modal-title file-dialog-title'>Error Handler</h4>" +
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