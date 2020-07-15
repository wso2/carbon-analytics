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

                // TODO this is hardcoded atm. WOrker should be dynamic
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
                            // self._application.utils.errorData = new Map(Object.entries(data));
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

                // TODO rename this to 'minimal'
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
                            // self._application.utils.errorData = new Map(Object.entries(data));
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

                byteArrayToString: function(byteArray) {
                    // const extraByteMap = [ 1, 1, 1, 1, 2, 2, 3, 0 ];
                    // var count = byteArray.length;
                    // var str = '';
                    //
                    // for (var index = 0;index < count;) {
                    //     var ch = byteArray[index++];
                    //     if (ch & 0x80) {
                    //         var extra = extraByteMap[(ch >> 3) & 0x07];
                    //         if (!(ch & 0x40) || !extra || ((index + extra) > count)) {
                    //             return null;
                    //         }
                    //
                    //         ch = ch & (0x3F >> extra);
                    //         for (;extra > 0;extra -= 1) {
                    //             var chx = byteArray[index++];
                    //             if ((chx & 0xC0) != 0x80) {
                    //                 return null;
                    //             }
                    //             ch = (ch << 6) | (chx & 0x3F);
                    //         }
                    //     }
                    //
                    //     str += String.fromCharCode(ch);
                    // }
                    //
                    // return str;

                },

                stringToByteArray: function(string) {
                    var result = [];
                    for (var i = 0; i < string.length; i++) {
                        result.push(string.charCodeAt(i).toString(2));
                    }
                    return result;
                },

                test: function Decodeuint8arr(uint8array){
            return new TextDecoder("utf-8").decode(uint8array);
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
                            // self._application.utils.errorData = new Map(Object.entries(data));
                            console.log("Detailed Error Entry received", data)
                            self.renderDetailedErrorEntry(data);
                        },
                        error: function (e) {
                            alerts.error("Unable to fetch Detailed error entry"); // TODO improve
                            throw "Unable to read errors";
                        }
                    });
                },

                replay: function(payload, serverHost, serverPort, username, password) {
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
                            console.log("Replay Response received")
                            // Re-fetch after discarding error
                            self.fetchErrorEntries(self.selectedSiddhiApp, serverHost, serverPort, username, password);
                        },
                        error: function (e) {
                            alerts.error("Unable to fetch Erroneous Events." +
                                "Please see the editor console for further information.")
                            throw "Unable to read errors";
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
                        '<h2 class="modal-title file-dialog-title">' +
                        'Server Configurations' +
                        '</h2>' +
                        '<hr class="style1">' +
                        '</div>' +
                        '<div id="serverConfigurationsModalBody" class="modal-body">' +
                        '</div>' +
                        '<div class="modal-footer">' +
                        '<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>' +
                        '</div>' +
                        '</div>' +
                        '</div>' +
                        '</div>');

                    var modalBody = serverConfigurationsModal.find("#serverConfigurationsModalBody");


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
                    modalBody.append(serverProperties);
                    modalBody.append('<br/>');
                    modalBody.append(
                        "<button id='applyWorkerConfigurations' type='button' class='btn btn-primary'>Done</button>");

                    modalBody.find("#applyWorkerConfigurations").click(function() { // TODO deactivate button & validate
                        self.serverHost = $(this).parent().find("#serverHost").val();
                        self.serverPort = $(this).parent().find("#serverPort").val();
                        self.serverUsername = $(this).parent().find("#serverUsername").val();
                        self.serverPassword = $(this).parent().find("#serverPassword").val();
                        self.isServerConfigured = true;
                        self.renderContent();
                    });

                    this.serverConfigurations = serverConfigurationsModal;
                    this.serverConfigurations.modal('show');
                },

                renderServerDetails: function(errorContainer) { // TODO this is about to go away
                    var self = this;
                    // TODO beautify
                    var serverDetails =
                        $('<div>' +
                            `<h4>${self.isServerConfigured ? (self.serverHost + ':' + self.serverPort) :
                                'Not Configured'}</h4>` +
                            '<button id="configureServer" type="button" class="btn btn-primary">Configure</button>' +
                            '</div>');

                    serverDetails.find("#configureServer").click(function() { // TODO deactivate button & validate
                        self.renderServerConfigurations();
                    });

                    errorContainer.append(serverDetails);

                    // TODO remove when confirmed
                    // var self = this;
                    // var workerConfigurations = $('<div></div>');
                    // workerConfigurations.append(
                    //     '<input type="text" id="serverHost" placeholder="Host" value="' +
                    //     (this.serverHost || '') + '">');
                    // workerConfigurations.append(
                    //     '<input type="text" id="serverPort" placeholder="Host" value="' +
                    //     (this.serverPort || '') + '">');
                    // workerConfigurations.append(
                    //     '<input type="text" id="serverUsername" placeholder="Host" value="' +
                    //     (this.serverUsername || '') + '">');
                    // workerConfigurations.append(
                    //     '<input type="password" id="serverPassword" placeholder="Host" value="' +
                    //     (this.serverPassword || '') + '">');
                    // workerConfigurations.append(
                    //     "<button id='configureWorker' type='button' class='btn btn-primary'>Done</button>");
                    //
                    // workerConfigurations.find("button").click(function() { // TODO deactivate button & validate
                    //     self.serverHost = $(this).parent().find("#serverHost").get(0).value;
                    //     self.serverPort = $(this).parent().find("#serverPort").get(0).value;
                    //     self.serverUsername = $(this).parent().find("#serverUsername").get(0).value;
                    //     self.serverPassword = $(this).parent().find("#serverPassword").get(0).value;
                    //     self.isServerConfigured = true;
                    //     self.renderContent();
                    // });
                    //
                    // errorContainer.append(workerConfigurations);
                },

                renderSiddhiAppSelection: function(siddhiAppList, errorContainer) {
                    var self = this;

                    var siddhiAppSelection = $('<div></div>');
                    if (siddhiAppList) {
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
                            "<button id='getErrorEntries' type='button' class='btn btn-primary'>Search</button>");

                        siddhiAppSelection.find("select").change(function() {
                            self.selectedSiddhiApp = this.value;
                            $(this).parent().find("select").find("option[value='" + this.value + "']")
                                .attr('selected', 'selected')
                        });

                        siddhiAppSelection.find("button").click(function() {
                            var siddhiAppName = $(this).parent().find("select").get(0).value;
                            self.selectedSiddhiApp = siddhiAppName;
                            self.fetchErrorEntries(siddhiAppName, 'localhost', '9444', 'admin', 'admin');
                        });
                    } else {
                        siddhiAppSelection.append("<h3>No Siddhi apps found</h3>"); // TODO confirm this
                    }
                    errorContainer.append(siddhiAppSelection);
                },

                renderErrorEntries: function(errorContainer) {
                    var self = this;
                    errorDisplay = $('<div></div>');

                    if (this.errorEntries) {
                        this.errorEntries.forEach(function (errorEntry) {
                            errorDisplay.append(self.renderErrorEntry(errorEntry));
                        });
                    }
                    errorContainer.append(errorDisplay);
                },

                renderOriginalPayload: function(errorEntry) { // TODO uneditable original payload for occurrences other than before source mapping
                    var originalPayload = $('<div><h4>Original Payload</h4></div>');
                    if (errorEntry.originalPayloadAsBytes) { // TODO bytes to string
                        originalPayload.append('<div class="payload-content">' +
                            this.byteArrayToString(errorEntry.originalPayloadAsBytes)+ '</div>');
                    } else {
                        originalPayload.append('<div>Original payload of the event is not available</div>');
                    }
                    return originalPayload;
                },

                renderReplayButtonInDetailedErrorEntry: function(errorEntry) {
                    var self = this;
                    var replay = $('<div></div>');
                    var replayableErrorEntry = errorEntry;
                    var isReplayPayloadEditable = (errorEntry.eventType === this.PAYLOAD_STRING);
                    replay.append("<button id='replay' type='button' class='btn btn-primary'>Replay</button>");
                    if (isReplayPayloadEditable) {
                        // Editable event payload
                        replay.append('<br/>');
                        replay.append('<textarea id="eventPayload" rows="4" cols="40" class="payload-content">' +
                            self.byteArrayToString(replayableErrorEntry.eventAsBytes) + '</textarea>');
                    }

                    replay.find("#replay").click(function() { // TODO disable if server not configured
                        if (isReplayPayloadEditable) {
                            // TODO make sure that no worries about mutating the original object
                            var eventAsString = replay.find("#eventPayload").val();
                            replayableErrorEntry.eventAsBytes = self.stringToByteArray(eventAsString);
                        }
                        self.replay([replayableErrorEntry], self.serverHost, self.serverPort, self.serverUsername,
                            self.serverPassword);
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

                renderDetailedErrorEntry: function(errorEntry) {
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
                        `<p class="description">Timestamp: ${errorEntry.timestamp} &nbsp; ID: ${errorEntry.id}</p>` +
                        '</div>');
                    modalBody.append('<br/>');
                    modalBody.append(this.renderReplayButtonInDetailedErrorEntry(errorEntry));
                    modalBody.append('<br/>');
                    if (errorEntry.eventType !== this.PAYLOAD_STRING) {
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
                        `<p class="description">Timestamp: ${errorEntry.timestamp} &nbsp; ID: ${errorEntry.id}</p>` +
                        '<br/>' +
                        "<button id='replay' type='button' class='btn btn-primary'>Replay</button>" +
                        "<button id='detailedInfo' type='button' class='btn btn-default'>Detailed Info</button>" +
                        '</div>');

                    errorEntryElement.find("#replay").click(function() { // TODO disable if server not configured
                        self.replay([errorEntry], self.serverHost, self.serverPort, self.serverUsername,
                            self.serverPassword);
                    });

                    errorEntryElement.find("#detailedInfo").click(function() {
                        self.fetchErrorEntryDetails(errorEntry.id, self.serverHost, self.serverPort,
                            self.serverUsername, self.serverPassword);
                    });

                    return errorEntryElement;
                },

                renderContent: function(errorContainer) {
                    var container = errorContainer || this._errorHandlerModal.find("div").filter("#errorContainer");
                    if (this.isServerConfigured) {
                        this.fetchSiddhiApps(
                            this.serverHost, this.serverPort, this.serverUsername, this.serverPassword);
                    }
                    container.empty();
                    var serverConfigBlock = $('<div class="error-handler-dialog-form-block"></div>');
                    this.renderServerDetails(serverConfigBlock);
                    var errorEntriesBlock = $('<div class="error-handler-dialog-form-block" ' +
                        'style="height: 50%; margin-bottom: 0; overflow: auto;"></div>');
                    var siddhiAppSelection = $('<div style="margin-bottom: 20px"></div>');
                    this.renderSiddhiAppSelection(this.availableSiddhiApps, siddhiAppSelection);
                    errorEntriesBlock.append(siddhiAppSelection);
                    var errorEntries = $('<div style="overflow:auto; height:100%"></div>');
                    this.renderErrorEntries(errorEntries);

                    errorEntriesBlock.append(errorEntries);
                    container.append(serverConfigBlock);
                    container.append(errorEntriesBlock);
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