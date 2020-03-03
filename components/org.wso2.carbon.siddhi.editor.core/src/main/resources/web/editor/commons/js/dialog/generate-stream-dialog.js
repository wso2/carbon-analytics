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

        var requestBody = {};
        var files;
        var config = {};

        var constants = {
            HTTP_GET: 'GET',
            HTTP_POST: 'POST',
            HTTP_PUT: 'PUT',
            HTTP_DELETE: 'DELETE',
            editorUrl: window.location.protocol + '//' + window.location.host + '/editor',
            streamObj: {
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
            }
        };

        var GenerateStreamDialog = function (options, ref, callback) {
            this.app = options;
            this.pathSeparator = this.app.getPathSeperator();
            this.ref = ref;
            this.callback = callback;
        };

        GenerateStreamDialog.prototype.constructor = GenerateStreamDialog;

        GenerateStreamDialog.prototype.render = function () {
            var self = this;
            if (!_.isNil(this._generateStreamModal)) {
                this._generateStreamModal.remove();
            }

            // rename the variable
            var generateStreamModal = $('#generateStreamConfigModal');

            generateStreamModal.find(".collapse").collapse();

            generateStreamModal.find('#idFromFile').on('click', function (e) {
                e.stopPropagation();

                generateStreamModal.find('#generateFromFileContent').collapse("show");
                generateStreamModal.find('#fileSelector').change(function (e) {
                    self.handleFileSelect(e, generateStreamModal);
                });

                if (!generateStreamModal.find('#idFromDatabase').is(":checked")) {
                    generateStreamModal.find('#generateFromDBContent').collapse("hide");
                }
            });

            generateStreamModal.find('#idFromDatabase').on('click', function (e) {
                e.stopPropagation();

                generateStreamModal.find('#generateFromDBContent').collapse("show");
                if (!generateStreamModal.find('#idFromFile').is(":checked")) {
                    generateStreamModal.find('#generateFromFileContent').collapse("hide");
                }
            });

            generateStreamModal.find('#idInline').on('click', function (e) {
                e.stopPropagation();

                generateStreamModal.find('#inlineContent').collapse("show");
                if (!generateStreamModal.find('#idDatasource').is(":checked")) {
                    generateStreamModal.find('#datasourceContent').collapse("hide");
                }
            });

            generateStreamModal.find('#idDatasource').on('click', function (e) {
                e.stopPropagation();

                generateStreamModal.find('#datasourceContent').collapse("show");
                if (!generateStreamModal.find('#idInline').is(":checked")) {
                    generateStreamModal.find('#inlineContent').collapse("hide");
                }
            });

            // todo
            generateStreamModal.find('#loadInlineDbConnection').click(function (e) {
                self.handleLoadDbConnection(e, generateStreamModal, self);
            });
            // todo
            generateStreamModal.find('#loadDatasourceDbConnection').click(function (e) {
                self.handleLoadDbConnection(e, generateStreamModal, self);
            });

            generateStreamModal.find("#generateButton").click(function () {
                config = {};
                var formData = new FormData();
                if (generateStreamModal.find('#idFromFile').is(":checked")) {
                    requestBody = {};
                    if (generateStreamModal.find("#fromCsvFile").attr("aria-expanded") === "true") {
                        constants.streamObj.name = generateStreamModal.find('#streamNameCsv')[0].value;
                        config = {
                            streamName: generateStreamModal.find('#streamNameCsv')[0].value,
                            delemiter: generateStreamModal.find('#delimiterOfCSV')[0].value,
                            isHeaderExist: generateStreamModal.find('#isHeaderExists')[0].value
                        };
                        requestBody.type = 'csv';
                        requestBody["config"] = config;
                    } else if (generateStreamModal.find("#fromJsonFile").attr("aria-expanded") === "true") {
                        constants.streamObj.name = generateStreamModal.find('#streamNameJson')[0].value;
                        config = {
                            streamName: generateStreamModal.find('#streamNameJson')[0].value,
                            enclosingElement:generateStreamModal.find('#jsonEnclosingElement')[0].value
                        };
                        requestBody.type = 'json';
                        requestBody.config = config;
                    } else if (generateStreamModal.find("#fromXmlFile").attr("aria-expanded") === "true") {
                        requestBody["type"] = "xml";
                        constants.streamObj.name = generateStreamModal.find('#streamNameXml')[0].value;
                        config = {
                            streamName: generateStreamModal.find('#streamNameXml')[0].value,
                            nameSpace: generateStreamModal.find('#nameSpaceOfXml')[0].value,
                            enclosingElement: generateStreamModal.find('#enclosingElementXML')[0].value
                        };

                        requestBody.type = 'xml'
                        requestBody.config = config;
                    }
                    formData.append("config", JSON.stringify(requestBody));
                    formData.append("file", files[0]);
                    self.retrieveFileDataAttributes(formData, function (data) {
                        constants.streamObj.attributes = JSON.parse(data.attributes);
                        self.callback(constants.streamObj, self.ref);
                        generateStreamModal.modal('hide');
                    }, function (msg) {
                        self.errorDisplay(msg, generateStreamModal)

                    });
                } else if (generateStreamModal.find('#idFromDatabase').is(":checked")) {
                    if (generateStreamModal.find('#idInline').is(":checked")) {
                        var inlineTableList = generateStreamModal.find('#inlineTableSelector');
                        requestBody.tableName = inlineTableList[0].selectedOptions[0].label;
                        constants.streamObj.name = generateStreamModal.find('#streamNameInlineDB')[0].value;
                    } else if (generateStreamModal.find('#idDatasource').is(":checked")) {
                        constants.streamObj.name = generateStreamModal.find('#streamNameInlineDB')[0].value;
                        var datasourceTableSelector = generateStreamModal.find('#datasourceTableSelector');
                        requestBody.tableName = datasourceTableSelector[0].selectedOptions[0].label;
                    }
                    self.retrieveTableColumnNames(requestBody, function (data) {
                        constants.streamObj.attributes = data.attributes;
                        self.callback(constants.streamObj, self.ref);
                        generateStreamModal.modal('hide');
                    }, function (err) {
                        self.errorDisplay(err, generateStreamModal)
                    });
                }
            });

            // todo
            var generateStreamConfigModal = generateStreamModal.filter("#generateStreamConfigModal");
            var generateStreamConfigModalError = generateStreamModal.find("#generateStreamConfigModalError");
            generateStreamConfigModalError.hide();
            this._generateStreamModal = generateStreamModal;
            generateStreamConfigModal.modal('hide');
        };

        GenerateStreamDialog.prototype.handleFileSelect = function (evt, generateOptions) {
            files = evt.target.files; // FileList object
            generateOptions.find('.from-file-section').collapse('hide');
            var fileType = files[0].type;

            var section = generateOptions.find('.from-file-section[data-file-type=' + fileType + ']');
            if (section) {
                section.collapse('show');
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
                requestBody = {
                    url: generateOptions.find('#dataSourceLocation_1')[0].value,
                    username: generateOptions.find('#inlineUsername')[0].value,
                    password: generateOptions.find('#inlinePass')[0].value
                };
            } else if (generateOptions.find("#datasourceContent").attr("aria-expanded") === "true") {
                requestBody.dataSourceName = generateOptions.find('#dataSourceNameId')[0].value;
            }
            console.log(requestBody);
            self.connectToDatabase(requestBody, function (evt) {
                self.retrieveTableNames(requestBody, function (evt) {
                    self.populateInlineTableList(evt.tables, generateOptions);
                }, function (err) {
                    alertError(err.error);
                });
            }, function (err) {
                alertError(err.error);
            });
        };

        GenerateStreamDialog.prototype.show = function () {
            this._generateStreamModal.modal('show');
        };
        GenerateStreamDialog.prototype.populateInlineTableList = function (data, generateOptions) {
            if (data != null && data instanceof Array) {
                data.forEach(function (value, index) {
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
                    contentType: "application/json; charset=utf-8", // todo constant
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
