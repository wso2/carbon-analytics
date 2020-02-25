/**
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['require', 'lodash', 'jquery', 'log'],
    function (require, _, $, log) {

        var constants = {};
        var streamObj = {};


        constants.HTTP_GET = "GET";
        constants.HTTP_POST = "POST";
        constants.HTTP_PUT = "PUT";
        constants.HTTP_DELETE = "DELETE";
        constants.editorUrl = window.location.protocol + "//" + window.location.host + '/editor';
        constants.streamObj = {
            "name" : "testStream",
            "attributes":[
                {
                    "name":"price",
                    "dataType":"int"
                },
                {
                    "name":"name",
                    "dataType":"string"
                },
                {
                    "name":"volume",
                    "dataType":"double"
                }
            ]
        };

        var GenerateStreamDialog = function (options, ref, callback) {

            this.app = options;
            this.pathSeparator = this.app.getPathSeperator();
            this.ref = ref;
            this.callback = callback
        };

        GenerateStreamDialog.prototype.constructor = GenerateStreamDialog;

        GenerateStreamDialog.prototype.render = function () {
            var self = this;
            var app = this.app;
            if (!_.isNil(this._generateStreamModal)) {
                this._generateStreamModal.remove();
            }
            var generateOptions = $(
                "<div class='modal fade' id='generateStreamConfigModal' tabindex='-1' role='dialog' aria-tydden='true'>" +
                "<div class='modal-dialog file-dialog' role='document'>" +
                "<div class='modal-content'>" +
                "<div class='modal-header'>" +
                "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                "<i class='fw fw-cancel about-dialog-close'>" +
                "</i>" +
                "</button>" +
                "<h4 class='modal-title file-dialog-title'>Generate Stream</h4>" +
                "<hr class='style1'>" +
                "</div>" +
                "<div class='modal-body'>" +
                "<div class='container-fluid'>" +

                "<form class='form-horizontal' onsubmit='return false'>" +

                "<div class='panel-group driving-license-settings' id='accordion'>" +
                "<div class='panel panel-default'>" +

                "<div class='panel-heading'>" +
                "<h4 class='panel-title'>" +
                "<input type='radio' id='idFromFile' name='selection' value='fromFile' style='display: block'> " +
                "From File " +
                "</h4>" +
                "<h4 class='panel-title'>" +
                "<input type='radio' id='idFromDatabase' name='selection' value='fromFile' style='display: block'> " +
                "From Database" +
                "</h4>" +
                "</div>" +

                "<div id='generateFromFileContent' class='panel-collapse collapse in'>" +
                "<div class='panel-body'>" +
                "<div class='driving-license-kind'>" +
                "<div class='checkbox'>" +
                "<input type='checkbox' value=''>A" +
                "</div>" +
                "<div class='checkbox'>" +
                "<input type='checkbox' value=''>B" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +

                "<div id='generateFromDBContent' class='panel-collapse collapse in'>" +
                "<div class='panel-body'>" +

                "<div class='dbsourceConfigForm' data-type='db' style='display: block;'>" +
                "<h4 class='panel-title'>" +
                "<input type='radio' id='idInline' name='dbConfig' value='fromInline' style='display: block'> " +
                "Inline Configuration " +
                "</h4>" +
                "<h4 class='panel-title'>" +
                "<input type='radio' id='idDatasource' name='dbConfig' value='fromDatasource' style='display: block'> " +
                "Provide Datasource " +
                "</h4>" +

                "</div>" +
                "</div>" +

                "<div id='inlineContent' class='panel-collapse collapse in'>" +
                "<div class='panel-body'>" +
                "<div class='form-group'>" +
                "<label>" +
                "    Database URL<span class='required_astrix'> *</span>" +
                "</label>" +
                "<input type='text' class='form-control required-for-db-connection' id='dataSourceLocation_1' " +
                "value='jdbc:mysql://[machine-name/ip]:[port]/[database-name]' name='data-source-location' " +
                "aria-required='true'></div>" +
                "<div class='form-group'>" +
                "<label>" +
                "    Username<span class='required_astrix'> *</span>" +
                "</label>" +
                "<input type='text' class='form-control required-for-db-connection' name='username' value='root' " +
                "aria-required='true'></div>" +
                "<div class='form-group'>" +
                "<label>" +
                "Password<span class='required_astrix'> *</span>" +
                "</label>" +
                "<input type='password' class='form-control required-for-db-connection' name='password' value='root' " +
                "aria-required='true'></div>" +
                "<div class='form-group'>" +
                "<button type='button' class='btn btn-secondary' name='loadDbConnection'>" +
                "    Connect to database" +
                "</button>" +
                "<span class='helper connectionSuccessMsg'></span>" +
                "</div>" +
                "" +
                "<div class='form-group'>" +
                "<label>" +
                "Table name<span class='required_astrix'> *</span>" +
                "</label>" +
                "<select name='table-name' class='form-control' aria-required='true'>" +
                "</select>" +
                "</div>" +

                "</div>" +
                "</div>" +

                "<div id='datasourceContent' class='panel-collapse collapse in'>" +
                "<div class='panel-body'>" +
                "<div class='form-group'>" +
                "<label>" +
                "    Datasource Name<span class='required_astrix'> *</span>" +
                "</label>" +
                "<input type='text' class='form-control required-for-db-connection' id='dataSourceNameId' " +
                " name='dataSourceNameId' " +
                "aria-required='true'></div>" +
                "<div class='form-group'>" +
                "<button type='button' class='btn btn-secondary' name='loadDbConnection'>" +
                "    Connect to database" +
                "</button>" +
                "<span class='helper connectionSuccessMsg'></span>" +
                "</div>" +
                "" +
                "<div class='form-group'>" +
                "<label>" +
                "Table name<span class='required_astrix'> *</span>" +
                "</label>" +
                "<select name='table-name' class='form-control' aria-required='true'>" +
                "</select>" +
                "</div>" +
                "</div>" +
                "</div>" +



                "</div>" +
                "</div>" +

                "<div class='file-dialog-form-divider'>" +
                "</div>" +
                "<div class='button-container' id='button-container'>" +
                "<div class='form-group'>" +
                "<div class='file-dialog-form-btn'>" +
                "<button id='generateButton' type='button' class='btn btn-primary'>Generate" +
                "</button>" +
                "<div class='divider'/>" +
                "<button type='button' class='btn btn-default' data-dismiss='modal'>cancel</button>" +
                "</div>" +
                "</div>" +
                "</div>" +

                "</form>" +

                "<div id='generateStreamConfigModalError-container' class='generateStreamConfigModalError-container'>" +
                "<div id='generateStreamConfigModalError' class='alert alert-danger'>" +
                "<strong>Error!</strong> Something went wrong." +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>"
            );


            generateOptions.find(".collapse").collapse();

            generateOptions.find('#idFromFile').on('click', function(e) {
                e.stopPropagation();

                generateOptions.find('#generateFromFileContent').collapse("show");
                if(!generateOptions.find('#idFromDatabase').is(":checked")){
                    generateOptions.find('#generateFromDBContent').collapse("hide");
                }
            });

            generateOptions.find('#idFromDatabase').on('click', function(e) {
                e.stopPropagation();

                generateOptions.find('#generateFromDBContent').collapse("show");
                if(!generateOptions.find('#idFromFile').is(":checked")){
                    generateOptions.find('#generateFromFileContent').collapse("hide");
                }
            });

            generateOptions.find('#idInline').on('click', function(e) {
                e.stopPropagation();

                generateOptions.find('#inlineContent').collapse("show");
                if(!generateOptions.find('#idDatasource').is(":checked")){
                    generateOptions.find('#datasourceContent').collapse("hide");
                }
            });

            generateOptions.find('#idDatasource').on('click', function(e) {
                e.stopPropagation();

                generateOptions.find('#datasourceContent').collapse("show");
                if(!generateOptions.find('#idInline').is(":checked")){
                    generateOptions.find('#inlineContent').collapse("hide");
                }
            });

            generateOptions.find("button").filter("#generateButton").click(function () {
                self.callback(constants.streamObj,self.ref);
                generateOptions.modal('hide');
            });


            var generateStreamConfigModal = generateOptions.filter("#generateStreamConfigModal");
            var generateStreamConfigModalError = generateOptions.find("#generateStreamConfigModalError");
            generateStreamConfigModalError.hide();
            this._generateStreamModal = generateOptions;
            generateStreamConfigModal.modal('hide');
        };


        GenerateStreamDialog.prototype.show = function () {
            this._generateStreamModal.modal('show');
        };

        GenerateStreamDialog.prototype.connectToDatabase = function (connectionDetails) {
            if (connectionDetails !== null && connectionDetails.length > 0) {
                $.ajax({
                    async: true,
                    url: constants.editorUrl + "/connectToDatabase",
                    type: constants.HTTP_POST,
                    dataType: "json",
                    contentType: "application/json; charset=utf-8",
                    data: connectionDetails,
                    success: function (data) {
                        if (typeof successCallback === 'function')
                            successCallback(data)
                    },
                    error: function (msg) {
                        if (typeof errorCallback === 'function')
                            errorCallback(msg)
                    }
                })
            }
        };

        GenerateStreamDialog.prototype.retrieveTableNames = function (connectionDetails, successCallback, errorCallback) {
            if (connectionDetails !== null && connectionDetails.length > 0) {
                $.ajax({
                    async: false,
                    url: constants.simulatorUrl + "/connectToDatabase/retrieveTableNames",
                    type: constants.HTTP_POST,
                    dataType: "json",
                    contentType: "application/json; charset=utf-8",
                    data: connectionDetails,
                    success: function (data) {
                        if (typeof successCallback === 'function')
                            successCallback(data)
                    },
                    error: function (msg) {
                        if (typeof errorCallback === 'function')
                            errorCallback(msg)
                    }
                })
            }
        };

        return GenerateStreamDialog;
    });
