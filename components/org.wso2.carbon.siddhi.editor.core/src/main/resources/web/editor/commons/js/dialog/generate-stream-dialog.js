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
        var requestBody = {};
        var files;


        constants.HTTP_GET = "GET";
        constants.HTTP_POST = "POST";
        constants.HTTP_PUT = "PUT";
        constants.HTTP_DELETE = "DELETE";
        constants.editorUrl = window.location.protocol + "//" + window.location.host + '/editor';
        constants.streamObj = {
            "name": "testStream",
            "attributes": [
                {
                    "name": "price",
                    "dataType": "int"
                },
                {
                    "name": "name",
                    "dataType": "string"
                },
                {
                    "name": "volume",
                    "dataType": "double"
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
            var generateOptions = $('#generateStreamConfigModal')

            generateOptions.find(".collapse").collapse();

            generateOptions.find('#idFromFile').on('click', function (e) {
                e.stopPropagation();

                generateOptions.find('#generateFromFileContent').collapse("show");
                generateOptions.find('#fileSelector').change(function (e) {
                        self.handleFileSelect(e, generateOptions);
                    }
                );

                if (!generateOptions.find('#idFromDatabase').is(":checked")) {
                    generateOptions.find('#generateFromDBContent').collapse("hide");
                }
            });

            generateOptions.find('#idFromDatabase').on('click', function (e) {
                e.stopPropagation();

                generateOptions.find('#generateFromDBContent').collapse("show");
                if (!generateOptions.find('#idFromFile').is(":checked")) {
                    generateOptions.find('#generateFromFileContent').collapse("hide");
                }
            });

            generateOptions.find('#idInline').on('click', function (e) {
                e.stopPropagation();

                generateOptions.find('#inlineContent').collapse("show");
                if (!generateOptions.find('#idDatasource').is(":checked")) {
                    generateOptions.find('#datasourceContent').collapse("hide");
                }
            });

            generateOptions.find('#idDatasource').on('click', function (e) {
                e.stopPropagation();

                generateOptions.find('#datasourceContent').collapse("show");
                if (!generateOptions.find('#idInline').is(":checked")) {
                    generateOptions.find('#inlineContent').collapse("hide");
                }
            });

            generateOptions.find('button').filter('#loadInlineDbConnection').click(function (e) {
                    self.handleLoadDbConnection(e, generateOptions, self);
                }
            );
            generateOptions.find('button').filter('#loadDatasourceDbConnection').click(function (e) {
                self.handleLoadDbConnection(e, generateOptions, self);
            });

            generateOptions.find("button").filter("#generateButton").click(function () {
                config = {};
                var formData = new FormData();
                if (generateOptions.find('#idFromFile').is(":checked")) {
                    requestBody = {};
                    if (generateOptions.find("#fromCsvFile").attr("aria-expanded") === "true") {
                        requestBody["type"] = "csv";
                        constants.streamObj.name = generateOptions.find('#streamNameCsv')[0].value;
                        config["streamName"] = generateOptions.find('#streamNameCsv')[0].value;
                        config["delimiter"] = generateOptions.find('#delimiterOfCSV')[0].value;
                        config["isHeaderExist"] = generateOptions.find('#isHeaderExists')[0].value;
                        requestBody["config"] = config;
                    } else if (generateOptions.find("#fromJsonFile").attr("aria-expanded") === "true") {
                        requestBody["type"] = "json";
                        constants.streamObj.name = generateOptions.find('#streamNameJson')[0].value;
                        config["streamName"] = generateOptions.find('#streamNameJson')[0].value;
                        config["enclosingElement"] = generateOptions.find('#jsonEnclosingElement')[0].value;
                        requestBody["config"] = config;
                        formData.append("config", JSON.stringify(requestBody));
                    } else if (generateOptions.find("#fromXmlFile").attr("aria-expanded") === "true") {
                        requestBody["type"] = "xml";
                        constants.streamObj.name = generateOptions.find('#streamNameXml')[0].value;
                        config["streamName"] = generateOptions.find('#streamNameXml')[0].value;
                        config["nameSpace"] = generateOptions.find('#nameSpaceOfXml')[0].value;
                        config["enclosingElement"] = generateOptions.find('#enclosingElementXML')[0].value;
                        requestBody["config"] = config;
                    }
                    formData.append("config", JSON.stringify(requestBody));
                    formData.append("file", files[0]);
                    self.retrieveFileDataAttributes(formData, function (data) {
                        constants.streamObj.attributes = JSON.parse(data.attributes);
                        self.callback(constants.streamObj, self.ref);
                        generateOptions.modal('hide');
                    }, function (msg) {
                        self.errorDisplay(msg, generateOptions)

                    });
                } else if (generateOptions.find('#idFromDatabase').is(":checked")) {
                    if (generateOptions.find('#idInline').is(":checked")) {
                        var inlineTableList = generateOptions.find('#inlineTableSelector');
                        requestBody['tableName'] = inlineTableList[0].selectedOptions[0].label;
                        constants.streamObj.name = generateOptions.find('#streamNameInlineDB')[0].value;
                    } else if (generateOptions.find('#idDatasource').is(":checked")) {
                        constants.streamObj.name = generateOptions.find('#streamNameInlineDB')[0].value;
                        var datasourceTableSelector = generateOptions.find('#datasourceTableSelector');
                        requestBody['tableName'] = datasourceTableSelector[0].selectedOptions[0].label;
                    }
                    self.retrieveTableColumnNames(requestBody, function (data) {
                        constants.streamObj.attributes = data.attributes;
                        self.callback(constants.streamObj, self.ref);
                        generateOptions.modal('hide');
                    }, function (err) {
                        self.errorDisplay(err, generateOptions)
                    });
                }
            });


            var generateStreamConfigModal = generateOptions.filter("#generateStreamConfigModal");
            var generateStreamConfigModalError = generateOptions.find("#generateStreamConfigModalError");
            generateStreamConfigModalError.hide();
            this._generateStreamModal = generateOptions;
            generateStreamConfigModal.modal('hide');
        };

        GenerateStreamDialog.prototype.handleFileSelect = function (evt, generateOptions) {
            files = evt.target.files; // FileList object
            generateOptions.find("#fromJsonFile").collapse("hide");
            generateOptions.find("#fromXmlFile").collapse("hide");
            generateOptions.find("#fromCsvFile").collapse("hide");
            var fileType = files[0].type;

            if (fileType === "application/json") {
                generateOptions.find("#fromJsonFile").collapse("show");
            } else if (fileType === "text/xml") {
                generateOptions.find("#fromXmlFile").collapse("show");
            } else if (fileType === "text/csv") {
                generateOptions.find("#fromCsvFile").collapse("show");
            } else {
                self.alertError("Error Occurred while processing the file. File type does not supported")
            }
            var output = [];
            for (var i = 0, f; f = files[i]; i++) {
                output.push('<li><strong>', escape(f.name), '</strong> (', f.type || 'n/a', ') - ',
                    f.size, ' bytes, last modified: ',
                    f.lastModifiedDate ? f.lastModifiedDate.toLocaleDateString() : 'n/a',
                    '</li>');
            }
            document.getElementById('file_list1').innerHTML = '<ul>' + output.join('') + '</ul>';
        };

        GenerateStreamDialog.prototype.handleLoadDbConnection = function (evt, generateOptions, self) {
            requestBody = {};
            if (generateOptions.find("#inlineContent").attr("aria-expanded") === "true") {
                requestBody['url'] = generateOptions.find('#dataSourceLocation_1')[0].value;
                requestBody['username'] = generateOptions.find('#inlineUsername')[0].value;
                requestBody['password'] = generateOptions.find('#inlinePass')[0].value;
            } else if (generateOptions.find("#datasourceContent").attr("aria-expanded") === "true") {
                requestBody['dataSourceName'] = generateOptions.find('#dataSourceNameId')[0].value;
            }
            console.log(requestBody);
            self.connectToDatabase(requestBody, function (evt) {
                self.retrieveTableNames(requestBody, function (evt) {
                    self.populateInlineTableList(evt.tables, generateOptions);
                }, function (err) {
                    self.alertError(err.error);
                });
            }, function (err) {
                self.alertError(err.error);
            });
        };

        GenerateStreamDialog.prototype.show = function () {
            this._generateStreamModal.modal('show');
        };
        GenerateStreamDialog.prototype.populateInlineTableList = function (data, generateOptions) {
            if (data != null && data instanceof Array) {
                  data.forEach( function (value, index) {
                      generateOptions.find('.tableSelector').append('<option value=' + index + '>'
                          + value + '</option>');
                  });
            }
        };

        GenerateStreamDialog.prototype.connectToDatabase = function (connectionDetails, successCallback, errorCallback) {
            if (connectionDetails !== null) {
                $.ajax({
                    async: false,
                    url: constants.editorUrl + "/connectToDatabase",
                    type: constants.HTTP_POST,
                    contentType: "application/json; charset=utf-8",
                    data: JSON.stringify(connectionDetails),
                    success: function (data) {
                        if (typeof errorCallback === 'function')
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
            if (connectionDetails !== null) {
                $.ajax({
                    async: false,
                    url: constants.editorUrl + "/retrieveTableNames",
                    type: constants.HTTP_POST,
                    contentType: "application/json; charset=utf-8",
                    data: JSON.stringify(connectionDetails),
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

        GenerateStreamDialog.prototype.retrieveTableColumnNames = function (connectionDetails, successCallback, errorCallback) {
            if (connectionDetails !== null) {
                $.ajax({
                    async: false,
                    url: constants.editorUrl + "/retrieveTableColumnNames",
                    type: constants.HTTP_POST,
                    contentType: "application/json; charset=utf-8",
                    data: JSON.stringify(connectionDetails),
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

        GenerateStreamDialog.prototype.retrieveFileDataAttributes = function (connectionDetails, successCallback, errorCallback) {
            if (connectionDetails !== null) {
                $.ajax({
                    async: false,
                    url: constants.editorUrl + "/retrieveFileDataAttributes",
                    type: constants.HTTP_POST,
                    contentType: false,
                    processData: false,
                    data: JSON.stringify(connectionDetails),
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

        GenerateStreamDialog.prototype.errorDisplay = function (errorMessage, generateOptions) {
            generateOptions.find("#modal-error").collapse("show");
            var generateStreamConfigModalError = generateOptions.find("#generateStreamConfigModalError");
            generateStreamConfigModalError.show();
        };
        return GenerateStreamDialog;
    });
