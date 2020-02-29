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
            var generateOptions = $('#generateStreamConfigModal')

            generateOptions.find(".collapse").collapse();

            generateOptions.find('#idFromFile').on('click', function(e) {
                e.stopPropagation();

                generateOptions.find('#generateFromFileContent').collapse("show");
                generateOptions.find('#fileSelector').change(function(e) {
                        handleFileSelect1(e, generateOptions);
                    }
                );

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

            generateOptions.find('button').filter('#loadInlineDbConnection').click(function(e){
                    handleLoadDbConnection(e, generateOptions);
                }
            );
            generateOptions.find('button').filter('#loadDatasourceDbConnection').click(function(e){
                    handleLoadDbConnection(e, generateOptions);
            });

            generateOptions.find("button").filter("#generateButton").click(function () {
                requestBody ={};
                var fileReader = new FileReader();
                config={};
                formData.append('file',files[0]);
                if(generateOptions.find('#idFromFile').is(":checked")){
                    if(generateOptions.find("#fromCsvFile").attr("aria-expanded")=="true"){
                        requestBody["type"] = "csv";
                        config["streamName"] = generateOptions.find('#streamNameCsv')[0].value;
                        config["delimiter"] = generateOptions.find('#delimiterOfCSV')[0].value;
                        config["isHeaderExists"] = generateOptions.find('#isHeaderExists')[0].value;
                        config["fileBody"] = formData;
                        requestBody["config"] = config;
                    } else if(generateOptions.find("#fromJsonFile").attr("aria-expanded")=="true"){
                         requestBody["type"] = "json";
                         config["streamName"] = generateOptions.find('#streamNameJson')[0].value;
                         config["enclosingElement"] = generateOptions.find('#jsonEnclosingElement')[0].value;
                        requestBody["config"] = config;
                    } else if(generateOptions.find("#fromXmlFile").attr("aria-expanded")=="true"){
                          requestBody["type"] = "xml";
                          config["streamName"] = generateOptions.find('#streamNameXml')[0].value;
                          config["nameSpace"] = generateOptions.find('#nameSpaceOfXml')[0].value;
                          config["enclosingElement"] = generateOptions.find('#enclosingElementXML')[0].value;
                          requestBody["config"] = config;
                    }
                } else if(generateOptions.find('#idFromDatabase').is(":checked")){
                    if(generateOptions.find('#idInline').is(":checked")){

                    }else if(generateOptions.find('#idDatasource').is(":checked")){

                    }
                }
                self.retrieveTableNames(requestBody,function(e){
                    console.log(e)
                },function(err){
                    console.log(err)
                });
//                self.callback(constants.streamObj,self.ref);
//                generateOptions.modal('hide');
            });


            var generateStreamConfigModal = generateOptions.filter("#generateStreamConfigModal");
            var generateStreamConfigModalError = generateOptions.find("#generateStreamConfigModalError");
            generateStreamConfigModalError.hide();
            this._generateStreamModal = generateOptions;
            generateStreamConfigModal.modal('hide');


        };
        function handleFileSelect1(evt, generateOptions) {
            files = evt.target.files; // FileList object
                generateOptions.find("#fromJsonFile").collapse("hide");
                generateOptions.find("#fromXmlFile").collapse("hide");
                generateOptions.find("#fromCsvFile").collapse("hide");
            var fileType = files[0].type;

            if (fileType === "application/json"){
                generateOptions.find("#fromJsonFile").collapse("show");
            } else if(fileType === "text/xml"){
                generateOptions.find("#fromXmlFile").collapse("show");
            } else if(fileType === "text/csv"){
                generateOptions.find("#fromCsvFile").collapse("show");

            } else{
                console.error("Huta pata support natho..")
            }
            var output = [];
            for (var i = 0, f; f = files[i]; i++) {
                output.push('<li><strong>', escape(f.name), '</strong> (', f.type || 'n/a', ') - ',
                f.size, ' bytes, last modified: ',
                f.lastModifiedDate ? f.lastModifiedDate.toLocaleDateString() : 'n/a',
                    '</li>');
            }
            document.getElementById('file_list1').innerHTML = '<ul>' + output.join('') + '</ul>';
        }

        function handleLoadDbConnection(evt, generateOptions){
            requestBody={};
            if(generateOptions.find("#inlineContent").attr("aria-expanded")=="true"){
               requestBody['url'] = generateOptions.find('#dataSourceLocation_1')[0].value;
               requestBody['username'] = generateOptions.find('#inlineUsername')[0].value;
                requestBody['password'] = generateOptions.find('#inlinePass')[0].value;
             } else if (generateOptions.find("#datasourceContent").attr("aria-expanded")=="true"){
                requestBody['dataSourceName'] = generateOptions.find('#dataSourceNameId')[0].value;
             }
             console.log(requestBody);
             var response = connectToDatabase(requestBody, function(evt){
                console.log(evt);
             }, function(err){
                console.log(err);
             });
        }

        GenerateStreamDialog.prototype.show = function () {
            this._generateStreamModal.modal('show');
        };

        GenerateStreamDialog.prototype.connectToDatabase = function (connectionDetails) {
            if (connectionDetails !== null && connectionDetails.length > 0) {
                $.ajax({
                    async: false,
                    url: constants.editorUrl + "/connectToDatabase",
                    type: constants.HTTP_POST,
                    contentType: "application/json; charset=utf-8",
                    data: connectionDetails,
                    success: function (data) {
                      return data
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
                    data: connectionDetails,
                    success: function (data) {
                    alert('Success')
                        if (typeof successCallback === 'function')
                            successCallback(data)
                    },
                    error: function (msg) {
                                        alert('error')
                        if (typeof errorCallback === 'function')
                            errorCallback(msg)
                    }
                })
            }
        };

        return GenerateStreamDialog;
    });
