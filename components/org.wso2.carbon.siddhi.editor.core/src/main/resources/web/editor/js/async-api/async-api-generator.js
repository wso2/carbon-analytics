/*
 *
 *  * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */
define(['require', 'jquery', 'lodash', 'log', 'smart_wizard', 'app/source-editor/completion-engine', 'alerts',
        'inputOutputMapper', 'inputOptionConfigurator', 'dataMapper', 'outputConfigurator', 'handlebar',
        'dataMapperUtil', 'ace/ace', 'app/source-editor/editor', 'alerts', 'js/async-api/asyncapi-rest-client', 'yaml',
        'utils', 'asyncAPI'],

    function (require, $, _, log, smartWizard, CompletionEngine, Alerts, InputOutputMapper,
              InputOptionConfigurator, DataMapper, OutputConfigurator, Handlebars, DataMapperUtil,
              ace, AsyncAPIEditor, alerts, AsyncAPIRESTClient, yaml, Utils, AsyncAPI) {

        var AsyncAPIView = function (initOpts) {
            this.__$parent_el_container = $(initOpts.parentEl);
            var self = this;
            this.etlWizardContainer = this.__$parent_el_container.find(_.get(initOpts, 'etl_wizard.container'));
            this.canvasContainer = this.__$parent_el_container.find(_.get(initOpts, 'canvas.container'));
            this.sourceContainer = this.__$parent_el_container.find(_.get(initOpts, 'source.container'));
            this.designContainer = this.__$parent_el_container.find(_.get(initOpts, 'design_view.container'));
            this.previewContainer = this.__$parent_el_container.find(_.get(initOpts, 'preview.container'));
            this.toggleControlsContainer = this.__$parent_el_container.find('.toggle-controls-container');

            this.asyncAPIViewContainer = initOpts.asyncAPIViewContainer;
            this.asyncAPIViewContainer.removeClass("hide-div");
            this.asyncAPISpecContainer = this.asyncAPIViewContainer
                .find(_.get(initOpts, 'async_api_view.specContainer'));
            this.asyncAPIGenContainer = this.asyncAPIViewContainer
                .find(_.get(initOpts, 'async_api_view.generatorContainer'));
            this.asyncAPIYamlContainer = this.asyncAPIViewContainer
                .find(_.get(initOpts, 'async_api_view.yamlContainer'));

            var asyncAPIYAMLViewDynamicId = "async-api-view-yaml-container-id-" + $(this.__$parent_el_container).attr('id');
            $(this.asyncAPIYamlContainer[0]).attr('id', asyncAPIYAMLViewDynamicId);
            self.inGenerator = true;
            self.__options = initOpts;
            self.__app = initOpts.application;
            self.__editorInstance = initOpts.editorInstance;
            self.__tab = initOpts.application.tabController.getActiveTab();
            self.asyncAPIDefYaml = initOpts.asyncAPIDefYaml;
            self.openAsyncAPIGenerateModal = $(
                "<div>" +
                "    <h4 class='modal-title file-dialog-title' " +
                "           id='initialAsyncApiDefHeading'>Generating Async API for Sinks and Sources</h4>" +
                "    <hr class='style1'>" +
                "</div>" +
                "<div>" +
                "    <div id='async-api-form'>" +
                "        <div>" +
                "            <div class='async-api-source-sink-list' id='async-api-source-sink-list' " +
                "               style='display: block'>" +
                "                <div class='form-group'>" +
                "                    <label class='clearfix'> Title </label> " +
                "                    <input class='add-new-server-input' id='asyncAPITitle' " +
                "                       value='SweetProductionApplication'> " +
                "                </div>" +
                "                <div class='form-group'>" +
                "                    <label class='clearfix'> Version </label> " +
                "                    <input class='add-new-server-input' id='asyncAPIVersion' value='1.0.0'> " +
                "                </div>" +
                "                <div class='form-group'>" +
                "                    <label class='clearfix'>Description</label>" +
                "                    <div class='col-sm-12'>" +
                "                         <textarea id='asyncAPIDescription' class='curl-editor form-control'>" +
                "</textarea>" +
                "                    </div>" +
                "                </div>" +
                "                <div class='form-group'>" +
                "                    <label class='clearfix'>Enter server name</label> " +
                "                    <input class='add-new-server-input' id='serverName' value='production'> " +
                "                </div>" +
                "                <div class='form-group'>" +
                "                    <label class='clearfix'>" +
                "                        Select Source or Sink type to Generate Async API" +
                "                    </label>" +
                "                    <select name='siddhi-sink-source-type' id='siddhi-sink-source-type' " +
                "                       class='form-control'>" +
                "                    </select>" +
                "                </div>" +
                "                <div class='form-group' id='source-list-div'>" +
                "                    <label>" +
                "                        Sources" +
                "                    </label>" +
                "                    <div id='source-list' class='col-xs-12' style='margin:10px 0px;'>" +
                "                    </div>" +
                "                </div>" +
                "                <div class='form-group' id='sink-list-div'>" +
                "                    <label>" +
                "                        Sinks" +
                "                    </label>" +
                "                    <div id='sink-list' class='col-xs-12' style='margin:10px 0px;'>" +
                "                    </div>" +
                "                </div>" +
                "            </div>" +
                "        </div>" +
                "    </div>" +
                "    <div class='button-container' id='async-button-container'>" +
                "        <div class='async-dialog-form-btn'>" +
                "            <button id='deployButton' type='button' class='btn btn-primary'>" +
                "               Generate Async API</button>" +
                "            <div class='divider'/>" +
                "            <button type='button' class='btn btn-default' data-dismiss='modal'>Cancel</button>" +
                "        </div>" +
                "    </div>" +
                "</div>"
            );

            $(this.asyncAPIGenContainer[0]).html(self.openAsyncAPIGenerateModal);

            self.sinkSorceTypes = {
                "websocket": {
                    "source": {
                        "security": {
                            "truststore.path": {"truststore.file": {"type": "X509"}}
                        }
                    }
                },
                "sse": {
                    "sink": {
                        "security": {
                            "basic.auth.username": {"http-basic": {"type": "http", "scheme": "BASIC"}},
                            "https.truststore.file": {"truststore.file": {"type": "X509"}},
                            "oauth.username": {"oauth": {"type": "oauth2"}},
                        }
                    }
                },
                "websubhub": {
                    "source": {
                        "security": {
                            "basic.auth.enabled": {"http-basic": {"type": "http", "scheme": "BASIC"}}
                        }
                    }
                },
                "websocket-server": {
                    "sink": {
                        "security": {
                            "keystore.path": {"keystore.file": {"type": "X509"}}
                        }
                    },
                    "source": {
                        "security": {
                            "keystore.path": {"keystore.file": {"type": "X509"}}
                        }
                    }
                }
            };

            self.hideOthers();
            self.hideInternalViews();

            self.compatibleSourceTypes = [];
            self.compatibleSinkTypes = [];

            _.each(self.sinkSorceTypes, function(value, key) {
                _.each(value, function(propertyValue, type) {
                    if (type.toLowerCase() == "source") {
                        self.compatibleSourceTypes.push(key.toLowerCase())
                    }
                });
            });

            _.each(self.sinkSorceTypes, function(value, key) {
                _.each(value, function(propertyValue, type) {
                    if (type.toLowerCase() == "sink") {
                        self.compatibleSinkTypes.push(key.toLowerCase())
                    }
                });
            });

            this.siddhiAppConfig = this.preRenderForSinksSources();

            if (self.siddhiAppConfig !== 'undefined') {
                var initialOptionValue = '<option selected="selected" value = "-1" ' +
                    'disabled>-- Please Sink or Source Type --</option>';
                var sinkSourceSelectorHTML = generateOptions(this.sinkSorceTypes, initialOptionValue);
                this.sinkSourceSelector = self.openAsyncAPIGenerateModal.find("select[name='siddhi-sink-source-type']");
                this.sinkSourceSelector.html(sinkSourceSelectorHTML);
                this.sinkSourceSelector.change(function () {
                    self.renderSinksSources($(this).val());
                });
                var generateBtn = this.openAsyncAPIGenerateModal.find("#deployButton");
                generateBtn.click(function () {
                    self.generateAsyncAPI()
                });
                this.sourceListSelector = this.openAsyncAPIGenerateModal.find("#source-list");
                this.sinkListSelector = this.openAsyncAPIGenerateModal.find("#sink-list");
                this.sourceListDivSelector = this.openAsyncAPIGenerateModal.find("#source-list-div");
                this.sinkListDivSelector = this.openAsyncAPIGenerateModal.find("#sink-list-div");
                this.asyncAPITitleSelector = this.openAsyncAPIGenerateModal.find("#asyncAPITitle");
                this.asyncAPIServerName = this.openAsyncAPIGenerateModal.find("#serverName");
                this.asyncAPIVersionSelector = this.openAsyncAPIGenerateModal.find("#asyncAPIVersion");
                this.asyncAPIDescriptionSelector = this.openAsyncAPIGenerateModal.find("#asyncAPIDescription");
                this.sourceListDivSelector.addClass('hide-div');
                this.sinkListDivSelector.addClass('hide-div');
                self.hideOthers();
                self.hideInternalViews();

                $('.toggle-controls-container #asyncbtn-to-code-view').on('click', function (e) {
                    e.preventDefault();
                    if (self.inGenerator) {
                        self.sourceContainer.show();
                        self.__app.workspaceManager.updateMenuItems();
                        self.asyncAPIViewContainer.hide();
                        $(self.toggleControlsContainer[0]).find('.toggle-view-button').removeClass('hide-div');
                        $(self.toggleControlsContainer[0]).find('.wizard-view-button').removeClass('hide-div');
                        var asyncAPIAddUpdateButton = $(self.toggleControlsContainer[0])
                            .find('.async-api-add-update-button');
                        asyncAPIAddUpdateButton.addClass('hide-div');
                        var codeViewButton = $(self.toggleControlsContainer[0]).find('#asyncbtn-to-code-view');
                        codeViewButton.addClass('hide-div');
                        var AsyncAPIViewButton = $(self.toggleControlsContainer[0]).find('#asyncbtn-asyncapi-view');
                        AsyncAPIViewButton.removeClass('hide-div');
                    }
                });
            }
        };
        //Constructor for the AsyncAPIView
        AsyncAPIView.prototype.constructor = AsyncAPIView;

        AsyncAPIView.prototype.hideInternalViews = function () {
            var self = this;
            self.asyncAPISpecContainer.addClass('hide-div');
            self.asyncAPIYamlContainer.addClass('hide-div');
            self.asyncAPIGenContainer.removeClass('hide-div');
        }

        AsyncAPIView.prototype.hideOthers = function () {
            var self = this;
            self.canvasContainer.addClass('hide-div');
            self.previewContainer.addClass('hide-div');
            self.designContainer.addClass('hide-div');
            self.sourceContainer.addClass('hide-div');
            $(self.toggleControlsContainer[0]).find('.toggle-view-button').addClass('hide-div');
            $(self.toggleControlsContainer[0]).find('.wizard-view-button').addClass('hide-div');
            $(self.toggleControlsContainer[0]).find('.asyncbtn-to-code-view').removeClass('hide-div');
            $(self.toggleControlsContainer[0]).find('.async-api-view-button').addClass('hide-div');
            self.etlWizardContainer.addClass('hide');
            self.asyncAPIViewContainer.addClass('etl-wizard-view-enabled');
        }

        AsyncAPIView.prototype.renderSinksSources = function (type) {
            var self = this;
            var sinks = self.siddhiAppConfig.siddhiAppConfig.sinkList;
            var sources = self.siddhiAppConfig.siddhiAppConfig.sourceList;
            var sinksAvailable = false;
            for (var i = 0; i < sinks.length; i++) {
                if (sinks[i].type.toLowerCase() === type.toLowerCase()) {
                    sinksAvailable = true;
                    break;
                }
            }
            var sourcesAvailable = false;
            for (var i = 0; i < sources.length; i++) {
                if (sources[i].type.toLowerCase() === type.toLowerCase()) {
                    sourcesAvailable = true;
                    break;
                }
            }

            if (sinks.length > 0 && sinksAvailable) {
                self.sinkListDivSelector.removeClass('hide-div');
                self.sinkListSelector.html(generateCheckboxListOptions(sinks, type));
                self.sinkListSelector.find('.asyncapitooltip').popover({
                    container: 'body',
                    trigger: 'hover'
                })
            } else {
                self.sinkListDivSelector.addClass('hide-div');
            }

            if (sources.length > 0 && sourcesAvailable) {
                self.sourceListDivSelector.removeClass('hide-div');
                self.sourceListSelector.html(generateCheckboxListOptions(sources, type));
                self.sourceListSelector.find('.asyncapitooltip').popover({
                    container: 'body',
                    trigger: 'hover'
                })
            } else {
                self.sourceListSelector.addClass('hide-div');
            }
        }

        AsyncAPIView.prototype.hideInternalViews = function () {
            var self = this;
            self.asyncAPISpecContainer.addClass('hide-div');
            self.asyncAPIYamlContainer.addClass('hide-div');
            self.asyncAPIGenContainer.removeClass('hide-div');
        }

        AsyncAPIView.prototype.generateAsyncAPI = function () {
            var self = this;

            var checkedSinkList = self.sinkListSelector.find("input:checked");
            var checkedSourceList = self.sourceListSelector.find("input:checked");

            var selectedIOType = self.sinkSourceSelector.val();
            var initialServer = null;
            var initialPort = -1;
            var server = null, port = -1;

            // validating servers
            var i, serverKeyValue, portKeyValue, urlElements, serverDetails;

            if (checkedSourceList.length === 0 && checkedSinkList.length === 0) {
                alerts.error("Please select a sink or source before generating the Async API");
            }
            var serversDetails = [];
            var sinkList = self.siddhiAppConfig.siddhiAppConfig.sinkList;
            var sourceList = self.siddhiAppConfig.siddhiAppConfig.sourceList;
            var streamList = self.siddhiAppConfig.siddhiAppConfig.streamList;

            for (i = 0; i < checkedSinkList.length; i++) {
                for (j = 0; j < sinkList.length; j++) {
                    if (selectedIOType.toLowerCase() === sinkList[j].type.toLowerCase() &&
                        sinkList[j].connectedElementName === checkedSinkList[i].value) {
                        serverDetails = getServerHostPort(sinkList[j].options, sinkList[j].type.toLowerCase(),
                            self.sinkSorceTypes, "sink");
                        serverDetails.channelType = "subscribe";
                        serverDetails.stream = sinkList[j].connectedElementName;
                        serverDetails.payloadProperties = getPayloadSpec(sinkList[j].connectedElementName,
                            streamList, sinkList[j].type.toLowerCase())
                        serverDetails.payloadSchemaProperties =
                            getPayloadSchemas(sinkList[j].connectedElementName, streamList)
                        serversDetails.push(serverDetails);
                        if (initialPort === -1) {
                            initialServer = serverDetails.host;
                            initialPort = serverDetails.port;
                        } else {
                            if (initialServer !== serverDetails.host || initialPort !== serverDetails.port) {
                                alerts.error("The selected sink " + sinkList[j].connectedElementName + " of " +
                                    sinkList[j].type + " has different server values (host:port)");
                            }
                        }
                    }
                }
            }

            for (i = 0; i < checkedSourceList.length; i++) {
                for (j = 0; j < sourceList.length; j++) {
                    if (selectedIOType.toLowerCase() === sourceList[j].type.toLowerCase() &&
                        sourceList[j].connectedElementName === checkedSourceList[i].value) {
                        serverDetails = getServerHostPort(
                            sourceList[j].options, sourceList[j].type.toLowerCase(), self.sinkSorceTypes, "source");
                        if (serverDetails.protocol === "websub") {
                            serverDetails.channelType = "subscribe";
                        } else {
                            serverDetails.channelType = "publish";
                        }
                        serverDetails.stream = sourceList[j].connectedElementName;
                        serverDetails.payloadProperties = getPayloadSpec(
                            sourceList[j].connectedElementName, streamList, sourceList[j].type.toLowerCase());
                        serverDetails.payloadSchemaProperties =
                            getPayloadSchemas(sourceList[j].connectedElementName, streamList)
                        serversDetails.push(serverDetails);
                        if (initialPort === -1) {
                            initialServer = serverDetails.host;
                            initialPort = serverDetails.port;
                        } else {
                            if (initialServer !== serverDetails.host || initialPort !== serverDetails.port) {
                                alerts.error("The selected source " + sourceList[j].connectedElementName +
                                    " of " + sourceList[j].type + " has different server values (host:port)");
                            }
                        }
                    }
                }
            }
            var title = (self.asyncAPITitleSelector.val().trim() !== "" ? self.asyncAPITitleSelector.val().trim() : 'TestApp');
            var version = (self.asyncAPIVersionSelector.val().trim() !== "" ? self.asyncAPIVersionSelector.val().trim() : '1.0.0');
            var description = (self.asyncAPIDescriptionSelector.val().trim() !== "" ? self.asyncAPIDescriptionSelector.val().trim() : 'This exposes an API from WSO2 SI');
            var asyncAPIJSON = {"asyncapi": "2.0.0"};
            asyncAPIJSON.info = {
                "title": title,
                "version": version,
                "description": description
            };
            asyncAPIJSON.servers = {};
            var serverName = (self.asyncAPIServerName.val().trim() !== "" ? self.asyncAPIServerName.val().trim() : 'production');
            asyncAPIJSON.servers[serverName] = {
                "url": serverDetails.url,
                "protocol": serverDetails.protocol,
                "security": [] //serverDetails.security
            };

            asyncAPIJSON.channels = {};
            for (i = 0; i < serversDetails.length; i++) {
                var channelRef;
                if (serversDetails[i].channelType === "publish") {
                    channelRef = {
                        "publish": {
                            "message": {
                                "$ref": "#/components/messages/" +
                                    serversDetails[i].stream + "Payload"
                            }
                        }
                    };
                } else {
                    channelRef = {
                        "subscribe": {
                            "message": {
                                "$ref": "#/components/messages/" +
                                    serversDetails[i].stream + "Payload"
                            }
                        }
                    };
                }
                var serverDetailChannels = serversDetails[i].channel;
                if (serverDetailChannels !== undefined && serverDetailChannels !== null) {
                    for (j = 0; j < serverDetailChannels.length; j++) {
                        asyncAPIJSON.channels[serverDetailChannels[j]] = channelRef;
                    }
                }

                var securityOptions = serversDetails[i].security;
                if (securityOptions !== undefined) {
                    for (var j = 0; j < securityOptions.length; j++) {
                        Object.keys(securityOptions[i]).forEach(function (key) {
                            var secutiryObject = {}
                            secutiryObject[key] = [];
                            asyncAPIJSON.servers[serverName].security.push(secutiryObject);
                        });
                    }
                }
            }
            asyncAPIJSON.components = {
                "messages": {},
                "schemas": {},
                "securitySchemes": {}
            };
            for (i = 0; i < serversDetails.length; i++) {
                asyncAPIJSON.components.messages[serversDetails[i].stream + "Payload"] =
                    {"payload": serversDetails[i].payloadProperties};
                Object.keys(serversDetails[i].payloadSchemaProperties).forEach(function (key) {
                    asyncAPIJSON.components.schemas[key] = serversDetails[i].payloadSchemaProperties[key];
                })
                var securityOptions = serversDetails[i].security;
                if (securityOptions !== undefined) {
                    for (var j = 0; j < securityOptions.length; j++) {
                        Object.keys(securityOptions[i]).forEach(function (key) {
                            asyncAPIJSON.components.securitySchemes[key] = securityOptions[i][key];
                        });
                    }
                }
            }
            yaml.safeLoad(JSON.stringify(asyncAPIJSON));
            var options = _.cloneDeep(self.__options)
            options.asyncAPIDefYaml = yaml.safeDump(yaml.safeLoad(JSON.stringify(asyncAPIJSON)));
            options.asyncAPIViewContainer = self.asyncAPIViewContainer;
            options.fromGenerator = true;
            options.editorInstance = self.__editorInstance;
            options.parentEl = this.__$parent_el_container;
            self.inGenerator = false;
            this.asyncAPI = new AsyncAPI(options);
        }

        AsyncAPIView.prototype.preRenderForSinksSources = function () {
            var self = this;
            var editorText = self.__tab.getFile().getContent();
            var variableMap = Utils.prototype.retrieveEnvVariables();
            var valid = true;
            if (!typeof self.__tab.getFile === "function") {
                valid = false;
            }
            var file = self.__tab.getFile();
            // file is not saved give an error and avoid running
            if (file.isDirty()) {
                valid = false;
            }
            if (valid) {
                var data = {
                    siddhiApp: editorText,
                    variables: variableMap,
                };
                var response = AsyncAPIRESTClient.submitToParse(data);
                if (response.status === "success") {
                    if (self.__tab.getFile().getRunStatus() !== undefined && !self.__tab.getFile().getRunStatus() &&
                        !self.__tab.getFile().getDebugStatus()) {
                        response = AsyncAPIRESTClient.getSiddhiElements(editorText);
                        if (response.status === "success") {
                            self.JSONObject = JSON.parse(self.__app.utils.b64DecodeUnicode(response.response));
                            if (self.__app.tabController.getActiveTab().getFile().getName().replace(".siddhi", "")
                                .localeCompare(self.JSONObject.siddhiAppConfig.siddhiAppName) === 0) {
                                console.log(self.JSONObject);
                                var sinks = self.JSONObject.siddhiAppConfig.sinkList;
                                var sources = self.JSONObject.siddhiAppConfig.sourceList;
                                var foundCompatibleType = false;
                                
                                for (var i = 0; i < sinks.length; i++) {
                                    if (self.compatibleSinkTypes.includes(sinks[i].type.toLowerCase())) {
                                        foundCompatibleType = true;
                                        break;
                                    }
                                }
                                for (var i = 0; i < sources.length && !foundCompatibleType; i++) {
                                    if (self.compatibleSourceTypes.includes(sources[i].type.toLowerCase())) {
                                        foundCompatibleType = true;
                                        break;
                                    }
                                }
                                if (!foundCompatibleType) {
                                    alerts.error("No compatible sink or source types found in the siddhi app to " +
                                        "generate Async API");
                                    self.sourceContainer.show();
                                    self.asyncAPIViewContainer.hide();
                                    $(self.toggleControlsContainer[0]).find('.toggle-view-button').removeClass('hide-div');
                                    $(self.toggleControlsContainer[0]).find('.wizard-view-button').removeClass('hide-div');
                                    var asyncAPIAddUpdateButton = $(self.toggleControlsContainer[0])
                                        .find('.async-api-add-update-button');
                                    asyncAPIAddUpdateButton.addClass('hide-div');
                                    var codeViewButton = $(self.toggleControlsContainer[0]).find('.asyncbtn-to-code-view');
                                    codeViewButton.addClass('hide-div');
                                    var AsyncAPIViewButton = $(self.toggleControlsContainer[0]).find('.async-api-view-button');
                                    AsyncAPIViewButton.removeClass('hide-div');
                                } else {
                                    return self.JSONObject;
                                }
                            }
                        } else if (response.status === "fail") {
                            alerts.error(response.errorMessage);
                        }
                    }
                } else {
                    alerts.error("Error while parsing Errors in Siddhi app. " +
                        "Please fix Siddhi app before generating AsyncAPI. " + response.message);
                }
            } else {
                alerts.error("Save file before running application");
            }
        };

        var generateOptions = function (sinkSorceTypes, initialOptionValue, componentName) {
            var dataOption =
                '<option value = "{{dataName}}">' +
                '{{dataName}}' +
                '</option>';
            var result = '';
            if (initialOptionValue !== undefined) {
                result += initialOptionValue;
            }
            var dataArray = [];

            Object.keys(sinkSorceTypes).forEach(function (key) {
                dataArray.push(key.toLowerCase());
            })

            if (dataArray) {
                dataArray.sort();
                for (var i = 0; i < dataArray.length; i++) {
                    result += dataOption.replaceAll('{{dataName}}', dataArray[i]);
                }
            }
            return result;
        };

        var getPayloadSpec = function (connectedElementName, streamList, type) {
            let payloadProperties;
            for (var j = 0; j < streamList.length; j++) {
                if (connectedElementName === streamList[j].name) {
                    if (type === "websubpublisher") {
                        return payloadProperties = {
                            "type": "object"
                        }
                    } else {
                        return payloadProperties = {
                            "type": "object",
                            "properties": getPayloadRef(streamList[j].attributeList)
                        }
                    }
                }
            }
        }

        var getPayloadSchemas = function (connectedElementName, streamList) {
            var properties = {};
            for (var j = 0; j < streamList.length; j++) {
                if (connectedElementName === streamList[j].name) {
                    var attributeList = streamList[j].attributeList;
                    for (var i = 0; i < attributeList.length; i++) {
                        properties[attributeList[i].name] = {"type": getAsyncAPIDataType(attributeList[i].type)};
                    }
                }
            }
            return properties;
        }

        var getPayloadRef = function (attributeList) {
            var properties = {};
            for (var i = 0; i < attributeList.length; i++) {
                properties[attributeList[i].name] = {"$ref": "#/components/schemas/" + attributeList[i].name};
            }
            return properties;
        }

        var getAsyncAPIDataType = function (type) {
            switch (type) {
                case "INT":
                case "LONG":
                    return "integer"
                case "STRING":
                case "OBJECT": //todo   Check how objects are sent in each transport
                    return "string";
                case "DOUBLE":
                case "FLOAT":
                    return "number"
                case "BOOL":
                    return "boolean";
            }
        }

        var getServerHostPort = function (options, type, sinkSorceTypes, ioType) {
            //serverPort contains host,port,protocol,channel
            var i, serverKeyValue, portKeyValue, urlElements, channelKeyValue;
            var serverDetails = {};
            serverDetails.security = [];
            serverDetails.channel = [];
            var securityOptions = sinkSorceTypes[type][ioType].security;
            if (type === "websocket-server") {
                for (i = 0; i < options.length; i++) {
                    if (options[i].startsWith("host")) {
                        serverKeyValue = options[i].split("=");
                        serverDetails.hostname = serverKeyValue[1].trim().replaceAll('"', '');
                    }
                    if (options[i].startsWith("port")) {
                        portKeyValue = options[i].split("=");
                        serverDetails.port = portKeyValue[1].trim().replaceAll('"', '');
                    }
                    Object.keys(securityOptions).forEach(function (key) {
                        if (options[i].startsWith(key)) {
                            serverDetails.security.push(securityOptions[key]);
                        }
                    })
                }
                if (serverKeyValue[1].trim().includes("wss")) {
                    serverDetails.protocol = "wss";
                } else {
                    serverDetails.protocol = "ws";
                }
                serverDetails.url = serverDetails.hostname + ":" + serverDetails.port;
                serverDetails.channel.push("/");
            } else if (type === "websocket") {
                for (i = 0; i < options.length; i++) {
                    if (options[i].startsWith("url")) {
                        serverKeyValue = options[i].split("=");
                        urlElements = serverKeyValue[1].trim().replaceAll('"', '')
                            .replaceAll("ws://", '').replaceAll("wss://", '').split("/");
                        var temp = urlElements[0].split(":");
                        serverDetails.hostname = temp[0];
                        serverDetails.port = temp[1];
                        if (serverKeyValue[1].trim().includes("wss")) {
                            serverDetails.protocol = "wss";
                        } else {
                            serverDetails.protocol = "ws";
                        }
                    }
                    Object.keys(securityOptions).forEach(function (key) {
                        if (options[i].startsWith(key)) {
                            serverDetails.security.push(securityOptions[key]);
                        }
                    })
                }
                //adding channel information retrived by the url
                serverDetails.url = serverDetails.hostname + ":" + serverDetails.port;
                serverDetails.channel.push(new URL(serverKeyValue[1].trim().replaceAll('"', '')).pathname);
            } else if (type === "sse") {
                var serverUrl;
                for (i = 0; i < options.length; i++) {
                    if (ioType == "sink") {
                        serverUrl = "event.sink.url";
                    } else {
                        serverUrl = "event.source.url";
                    }
                    if (options[i].startsWith(serverUrl)) {
                        serverKeyValue = options[i].split("=");
                        urlElements = serverKeyValue[1].trim()
                            .replaceAll('"', '').replaceAll("http://", '')
                            .replaceAll("https://", '').split("/");
                        var temp = urlElements[0].split(":");
                        serverDetails.hostname = temp[0];
                        serverDetails.port = temp[1];
                        if (serverKeyValue[1].trim().includes("https")) {
                            serverDetails.protocol = "sse";
                        } else {
                            serverDetails.protocol = "sse";
                        }
                        serverDetails.url = serverDetails.hostname + ":" + serverDetails.port;
                        serverDetails.channel.push(new URL(serverKeyValue[1].trim().replaceAll('"', '')).pathname);
                    }
                    Object.keys(securityOptions).forEach(function (key) {
                        if (options[i].startsWith(key)) {
                            serverDetails.security.push(securityOptions[key]);
                        }
                    })
                }
            } else if (type === "websubhub") {
                for (i = 0; i < options.length; i++) {
                    if (options[i].startsWith("receiver.url")) {
                        serverKeyValue = options[i].split("=");
                        urlElements = serverKeyValue[1].trim().replaceAll('"', '')
                            .replaceAll("http://", '').replaceAll("https://", '').split("/");
                        var temp = urlElements[0].split(":");
                        serverDetails.hostname = temp[0];
                        serverDetails.port = temp[1];
                        if (serverKeyValue[1].trim().includes("https")) {
                            serverDetails.protocol = "websub";
                        } else {
                            serverDetails.protocol = "websub";
                        }
                        serverDetails.url = serverKeyValue[1].trim().replaceAll('"', '')
                            .replaceAll("http://", '').replaceAll("https://", '');
                    }
                    if (options[i].startsWith("topic.list")) {
                        channelKeyValue = options[i].trim().replaceAll("\"", "").split("=");
                        if (channelKeyValue != null && channelKeyValue.length === 2) {
                            var topics = channelKeyValue[1].split(",");
                            for (j = 0; j < topics.length; j++) {
                                serverDetails.channel.push(topics[j].trim());
                            }
                        }

                    }
                    Object.keys(securityOptions).forEach(function (key) {
                        if (options[i].startsWith(key)) {
                            serverDetails.security.push(securityOptions[key]);
                        }
                    })
                }
            }
            return serverDetails;
        };

        var generateCheckboxListOptions = function (dataArray, type) {
            var dataOption =
                '<label for="{{dataName}}"  class="asyncapitooltip" data-toggle="popover" data-content="{{options}}">' +
                '<input id="{{dataName}}" name="stream-name"  type="radio" value="{{dataName}}" ' +
                '/>{{dataName}}' +
                '</label>';

            var result = '';
            if (dataArray) {
                dataArray.sort();
                for (var i = 0; i < dataArray.length; i++) {
                    if (dataArray[i].type.toLowerCase() === type.toLowerCase()) {
                        result += dataOption.replaceAll('{{dataName}}', dataArray[i].connectedElementName)
                            .replaceAll('{{options}}', dataArray[i].options.toString().replaceAll('"', "'"));
                    }
                }
            }
            return result;
        };

        return AsyncAPIView;
    })
;
