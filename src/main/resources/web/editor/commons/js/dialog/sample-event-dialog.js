/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'jquery', 'log', 'backbone', '../../../js/event-simulator/simulator-rest-client',
        './sample-event-rest-client', '../../js/utils/vkbeautify.0.99.00.beta'],
    function (require, $, log, Backbone, SimulatorRestClient, SampleEventRestClient, vkbeautify) {
        var SampleEventsDialog = Backbone.View.extend(
            /** @lends SettingsDialog.prototype */
            {
                /**
                 * @augments Backbone.View
                 * @constructs
                 * @class SettingsDialog
                 * @param {Object} options configuration options
                 */
                initialize: function (options) {
                    this._options = options;
                    this.application = _.get(options, "application");
                    this._dialogContainer = $(_.get(options, "application.config.dialog.container"));
                },

                show: function () {
                    this._modalContainer.modal('show');
                },

                setSelectedFolder: function (path) {
                    this._fileBrowser.select(path);
                },

                render: function () {
                    var app = this.application, options = this._options;
                    this.notification_container = $("#notification-container");
                    if (!_.isNil(this._modalContainer)) {
                        this._modalContainer.remove();
                    }
                    var sampleEventModal = $(_.get(options, 'selector')).clone();
                    this._modalContainer = sampleEventModal;
                    SimulatorRestClient.retrieveSiddhiAppNames(
                        function (data) {
                            var initialOptionValue = '<option selected="selected" value = "-1" ' +
                                'disabled>-- Please Select a Siddhi App --</option>';
                            var siddhiApps = self.generateOptions(data, initialOptionValue, "siddhiAppName");
                            sampleEventModal.find("select[name='siddhi-app-name']").html(siddhiApps);
                            initialOptionValue = '<option selected="selected" value = "-1" ' +
                                'disabled>-- Please Select a Stream name --</option>';
                            var streamNames = self.generateOptions(null, initialOptionValue);
                            sampleEventModal.find("select[name='stream-name']").html(streamNames);
                        },
                        function (data) {
                            var error = JSON.parse(data.responseText);
                            self.alertError(error.error);
                        }
                    );

                    sampleEventModal.find("select[name='siddhi-app-name']").on('change', function () {
                        SimulatorRestClient.retrieveStreamNames(
                            sampleEventModal.find("select[name='siddhi-app-name'] option:selected").text(),
                            function (data) {
                                var initialOptionValue = '<option selected="selected" ' +
                                    'value = "-1" disabled>-- Please Select a Stream name --</option>';
                                var streamNames = self.generateOptions(data, initialOptionValue);
                                sampleEventModal.find("select[name='stream-name']").html(streamNames);
                            },
                            function (data) {
                                var error = JSON.parse(data.responseText);
                                self.alertError(error.error);
                            });
                    });
                    var $sampleEvent = sampleEventModal.find("textarea[name='generatedSampleEvent']");
                    sampleEventModal.submit(function (event) {
                        var eventType = sampleEventModal.find("select[name='event-format']").val();
                        SampleEventRestClient.retrieveSampleEvent(
                            sampleEventModal.find("select[name='siddhi-app-name']").val(),
                            sampleEventModal.find("select[name='stream-name']").val(),
                            eventType,
                            function (data) {
                                if (eventType === "xml") {
                                    $sampleEvent.val(window.vkbeautify.xml(data));
                                } else if (eventType === "json") {
                                    $sampleEvent.val(window.vkbeautify.json(data));
                                } else {
                                    $sampleEvent.val(data);
                                }
                                $sampleEvent.select();
                            },
                            function (data) {
                                var error = JSON.parse(data.responseText);
                                self.alertError(error.error);
                            }
                        );
                        event.preventDefault();
                    });
                }
            });

        self.generateOptions = function (dataArray, initialOptionValue, componentName) {
            var dataOption =
                '<option value = "{{dataName}}">' +
                '{{dataName}}' +
                '</option>';
            var result = '';
            if (initialOptionValue !== undefined) {
                result += initialOptionValue;
            }
            if (dataArray) {
                dataArray.sort();
                for (var i = 0; i < dataArray.length; i++) {
                    if (componentName) {
                        result += dataOption.replaceAll('{{dataName}}', dataArray[i][componentName]);
                    } else {
                        result += dataOption.replaceAll('{{dataName}}', dataArray[i]);
                    }
                }
            }
            return result;
        };

        self.alertError = function (errorMessage) {
            var errorNotification = getErrorNotification(errorMessage);
            $("#notification-container").append(errorNotification);
            errorNotification.fadeTo(2000, 200).slideUp(1000, function () {
                errorNotification.slideUp(1000);
            });
        };

        function getErrorNotification(errorMessage) {
            return $(
                "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-danger' id='error-alert'>" +
                "<span class='notification'>" +
                errorMessage +
                "</span>" +
                "</div>");
        };

        return SampleEventsDialog;
    });
