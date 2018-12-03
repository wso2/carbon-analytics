/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['require', 'lodash','jquery', 'log', 'backbone', '../../../js/event-simulator/simulator-rest-client',
        './query-store-rest-client'],
    function (require, _, $, log, Backbone, SimulatorRestClient, QueryStoreRestClient) {
    var QueryStoreApi = Backbone.View.extend(
        /** @lends SaveToFileDialog.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class queryStore
             * @param {Object} config configuration options for the SaveToFileDialog
             */
            initialize: function (options) {
                this._options = options;
                this.application = _.get(options, "application");
                this._dialogContainer = $(_.get(options, "application.config.dialog.container"));
            },

            show: function(){
                this._storeQueryModal.modal('show');
            },

            render: function () {

                var app = this.application,
                    options = this._options;
                this.notification_container = $("#notification-container");
                if (!_.isNil(this._storeQueryModal)) {
                    this._storeQueryModal.remove();
                }
                var storeQueryModal = $(_.get(options, 'selector')).clone();
                this._storeQueryModal = storeQueryModal;
                SimulatorRestClient.retrieveSiddhiAppNames(
                    function (data) {
                        var initialOptionValue = '<option selected="selected" value = "-1" ' +
                            'disabled>-- Please Select a Siddhi App --</option>';
                        var siddhiApps = self.generateOptions(data, initialOptionValue, "siddhiAppName");
                        storeQueryModal.find("select[name='siddhi-app-name']").html(siddhiApps);
                    },
                    function (data) {
                        self.alertError("Error when retrieving siddhi apps. Reason: " + data);
                    }
                );

                storeQueryModal.submit(function (event) {
                    QueryStoreRestClient.retrieveStoresQuery(
                        storeQueryModal.find("select[name='siddhi-app-name']").val(),
                        storeQueryModal.find("textarea[id='curlEditor']").val(),
                        function (data) {
                            var rows = [];
                            data.records.forEach(function(childArray){
                                var columns = [];

                                childArray.forEach(function(value) {
                                    columns.push('<td>' + value + '</td>');
                                });

                                rows.push('<tr>' + columns + '</tr>');
                            });

                            storeQueryModal.find("div[id='simulator_output'] tbody").html(rows);
                        },
                        function (data) {
                            self.alertError("Error when executing query on Siddhi Store. Reason: " + data.responseText);
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
        }

    return QueryStoreApi;
});
