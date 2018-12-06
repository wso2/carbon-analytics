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
        './query-store-rest-client', 'datatables', 'datatables_bootstrap', 'datatables_wso2'],
    function (require, _, $, log, Backbone, SimulatorRestClient, QueryStoreRestClient) {
    var QueryStoreDialog = Backbone.View.extend(
        /** @lends QueryStoreDialog.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class queryStore
             * @param {Object} config configuration options for the QueryStoreDialog
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

                if (!_.isNil(this._storeQueryModal)) {
                    this._storeQueryModal.remove();
                }

                var storeQueryModal = $(options.selector).clone();
                this._storeQueryModal = storeQueryModal;
                var errorBox = storeQueryModal.find('#error-box');
                var resultBox = storeQueryModal.find('#simulator_output');

                var showError = function(message) {
                    errorBox.text(message).show();
                };

                var clearError = function() {
                    errorBox.hide();
                };

                SimulatorRestClient.retrieveSiddhiAppNames(
                    function (data) {
                        var template = '<option value="{{dataName}}">{{dataName}}</option>';
                        var options =
                            '<option selected="selected" value = "-1" disabled>-- Please Select a Siddhi App --</option>';
                        if (data) {
                            data.sort();
                            for (var i = 0; i < data.length; i++) {
                                options += template.replaceAll('{{dataName}}', data[i].siddhiAppName);
                            }
                        }
                        storeQueryModal.find("select[name='siddhi-app-name']").html(options);
                    },
                    function (data) {
                        showError("Error when retrieving siddhi apps. Reason: " + data);
                    }
                );

                storeQueryModal.submit(function (event) {
                    QueryStoreRestClient.retrieveStoresQuery(
                        storeQueryModal.find("select[name='siddhi-app-name']").val(),
                        storeQueryModal.find("textarea[id='curlEditor']").val(),
                        function (data) {
                            var rows = [];
                            var headerRow = [];
                            var queryData = storeQueryModal.find("table[id='query_data']");
                            data.records.forEach(function(childArray, index){
                                if (index === 0) {
                                    // add header row
                                    var headerColumns = [];
                                    childArray.forEach(function(value) {
                                        headerColumns.push('<th>' + value + '</th>');
                                    });
                                    headerRow.push('<tr>' + headerColumns + '</tr>');
                                } else {
                                    // add data rows
                                    var columns = [];
                                    childArray.forEach(function(value) {
                                        columns.push('<td>' + value + '</td>');
                                    });
                                    rows.push('<tr>' + columns + '</tr>');
                                }
                            });

                            if ($.fn.DataTable.isDataTable(queryData)) {
                                queryData.DataTable().clear().destroy();
                            }
                            clearError();
                            resultBox.find('thead').html(headerRow);
                            resultBox.find('tbody').html(rows);
                            queryData.DataTable();
                            queryData.removeClass('hidden');
                        },
                        function (data) {
                            var queryData = storeQueryModal.find("table[id='query_data']");
                            if ($.fn.DataTable.isDataTable(queryData)) {
                                queryData.DataTable().clear().destroy();
                            }
                            resultBox.find('thead').html('');
                            resultBox.find('tbody').html('');
                            queryData.addClass('hidden');

                            showError("Error when executing query on Siddhi Store. Reason: " + data.responseText);
                        }
                    );
                    event.preventDefault();
                });
            }
        });
    return QueryStoreDialog;
});
