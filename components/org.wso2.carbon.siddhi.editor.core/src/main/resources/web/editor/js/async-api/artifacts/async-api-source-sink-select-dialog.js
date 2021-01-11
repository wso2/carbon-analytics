/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['jquery', 'lodash', 'log', 'file_browser', 'js_tree', 'alerts', 'js/async-api/asyncapi-rest-client', 'utils'],
    function ($, _, log, FileBrowser, jsTree, alerts, AsyncAPIRESTClient, Utils) {

        var SourceSinkSelectorDialog = function (application, form) {
            this.application = application;
            this.sourceListSelector = form.find("#source-list");
            this.sinkListSelector = form.find("#sink-list");
            console.log("SourceSinkSelectorDialog.init() - form");
            console.log(form.length);
            console.log("SourceSinkSelectorDialog.init()");
            console.log(this.sinkListSelector.length);
            console.log(this.sinkListSelector.length);
            this.form = form;

            var sinkSorceTypes = ['WebSocket', 'SSE', 'WebHook', 'WebSocket-Server'];
            var initialOptionValue = '<option selected="selected" value = "-1" ' +
                'disabled>-- Please Sink or Source Type --</option>';
            var sinkSourceSelectorHTML = generateOptions(sinkSorceTypes, initialOptionValue);
            var sinkSourceSelector = form.find("select[name='siddhi-sink-source-type']");
            sinkSourceSelector.html(sinkSourceSelectorHTML);
            var self = this;
            sinkSourceSelector.change(function() {
                self.renderSinksSources($(this).val());
            });
        };

        SourceSinkSelectorDialog.prototype.constructor = SourceSinkSelectorDialog;

        SourceSinkSelectorDialog.prototype.renderSinksSources = function (selectedType) {
            var self = this;
            var activeTab = this.application.tabController.getActiveTab();
            var outputController = this.application.outputController;
            var commandManager = this.application.commandManager;
            var editorText = activeTab.getFile().getContent();
            var variableMap = Utils.prototype.retrieveEnvVariables();
            var valid = true;
            if (!typeof activeTab.getFile === "function") {
                valid = false;
            }
            var file = activeTab.getFile();
            // file is not saved give an error and avoid running
            if (file.isDirty()) {
                valid = false;
            }
            if (valid) {
                var data;
                AsyncAPIRESTClient.submitToParse(
                    {
                        siddhiApp: editorText,
                        variables: variableMap,
                    },
                    function (response) {
                        if (response.hasOwnProperty("status") && response.status === "SUCCESS") {
                            var siddhiAppName = "";
                            if (activeTab.getTitle().lastIndexOf(".siddhi") != -1) {
                                siddhiAppName = activeTab.getTitle().substring(0, activeTab.getTitle().lastIndexOf(".siddhi"));
                            } else {
                                siddhiAppName = activeTab.getTitle();
                            }
                            if (activeTab.getFile().getRunStatus() !== undefined && !activeTab.getFile().getRunStatus() &&
                                !activeTab.getFile().getDebugStatus()) {
                                AsyncAPIRESTClient.getSourcesAndSinks(
                                    siddhiAppName,
                                    function (responseData) {
                                        var sinks = responseData.sinks;
                                        var sources = responseData.sources;
                                        var sinkArray = [];
                                        for (var i=0; i<sinks.length; i++) {
                                            if(selectedType.toLowerCase() === sinks[i].type.toLowerCase()) {
                                                sinkArray.push(sinks[i].streamDefinition)
                                            }
                                        }
                                        self.sinkListSelector.html(generateCheckboxListOptions(sinkArray));
                                        var sourceArray = [];
                                        for (var i=0; i<sources.length; i++) {
                                            if(selectedType.toLowerCase() === sources[i].type.toLowerCase()) {
                                                sourceArray.push(sources[i].streamDefinition)
                                            }
                                        }
                                        self.sourceListSelector.html(generateCheckboxListOptions(sourceArray));
                                    },
                                    function (response) {});
                            }
                        } else {
                            alerts.error("Error while parsing Errors in Siddhi app. " +
                                "Please fix Siddhi app before generating AsyncAPI");
                            console.log("Error while parsing Errors in Siddhi app", response.message);
                        }
                    }
                );
            } else {
                alerts.error("Save file before running application");
            }
        }

        SourceSinkSelectorDialog.prototype.render = function () {

        };

        SourceSinkSelectorDialog.prototype.sinkSourceDisplayMethod = function (data) {
        };

        return SourceSinkSelectorDialog;
    });
