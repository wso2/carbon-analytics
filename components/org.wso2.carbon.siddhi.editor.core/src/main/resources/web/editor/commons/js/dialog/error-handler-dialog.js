/**
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash', 'jquery', 'constants', 'backbone', 'alerts'],
    function (require, _, $, Constants, Backbone, alerts) {
        var ErrorHandlerDialog = Backbone.View.extend(
            {
                initialize: function(options) {
                    this.dialog_containers = $(_.get(options.config.dialog, 'container'));
                    console.log("Finalize available workers")
                },

                show: function() {
                    this._errorHandlerModal.modal('show');
                },

                // TODO this is hardcoded atm. WOrker should be dynamic
                fetchErrors: function(siddhiAppName) {
                    var self = this;
                    $.ajax({
                        type: "GET",
                        contentType: "json",
                        url: 'http://localhost:9090/error-handler/erroneous-events?siddhiApp=' + siddhiAppName,
                        async: false,
                        success: function (data) {
                            self._application.utils.errorData = new Map(Object.entries(data));
                        },
                        error: function (e) {
                            alerts.error("Unable to read errors." +
                                "Please see the editor console for further information.")
                            throw "Unable to read errors";
                        }
                    });
                },

                renderErrors: function(errorContainer) {
                    this.fetchErrors('MappingErrorTest');

                    var self = this;
                    errorContainer.empty();
                    errorDisplay = $('<h1>Errors</h1>');
                    app.utils.errorData.forEach(function (error) {
                        errorDisplay.append(`<h2>{JSON.stringify(error}</h2>`);
                    });
                    errorContainer.append(errorDisplay);


                    // TODO get the idea of 'iterate and show' from this block
                    // errorDisplay = $('<table class="table table-hover data-table"' + ' id="extensionTableId">' +
                    //     '<tbody></tbody></table>');
                    // app.utils.extensionData.forEach(function (extension, key) {
                    //     var extensionTableBodyData = self.renderExtension(extension, key, self);
                    //     errorDisplay.append(extensionTableBodyData);
                    // });
                    // errorContainer.append(errorDisplay);
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
                        "<div class='file-dialog-form-scrollable-block' style='padding:10px4px; margin-left:35px;'>" +
                        "<div id='noResults' style='display:none;'>No extensions found.</div>" +
                        "<div id='errorContainer' class='samples-pane'>" +
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

                    self.renderErrors(errorContainer);

                    $(this.dialog_containers).append(errorHandlerModal);
                    errorHandlerErrorModal.hide();
                    this._errorHandlerModal = errorHandlerModal;
                },
            });
        return ErrorHandlerDialog;
    });