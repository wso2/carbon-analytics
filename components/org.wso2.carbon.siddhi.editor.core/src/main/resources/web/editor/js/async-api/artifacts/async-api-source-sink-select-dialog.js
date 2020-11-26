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

            var sinkSorceTypes = ['WebSocket', 'SSE', 'WebHook'];
            var initialOptionValue = '<option selected="selected" value = "-1" ' +
                'disabled>-- Please Sink or Source Type --</option>';
            var sinkSourceSelector = generateOptions(sinkSorceTypes, initialOptionValue);
            form.find("select[name='siddhi-sink-source-type']").html(sinkSourceSelector);

        };

        var generateOptions = function (dataArray, initialOptionValue, componentName) {
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

        var generateCheckboxListOptions = function (dataArray, initialOptionValue, componentName, type) {
            var dataOption =
                '<div className="col-xs-3" style="width:11%;padding-left:0px;">\n' +
                '    <input id="{{dataName}}" type="checkbox" name="{{type}}" value="{{dataName}}">' +
                '    <label htmlFor="{{dataName}}">{{dataName}}</label>\n' +
                '</div>';

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




        SourceSinkSelectorDialog.prototype.constructor = SourceSinkSelectorDialog;

        SourceSinkSelectorDialog.prototype.render = function () {
            var self = this;
            // var openFileWizardError = this.openFileWizardError;
            // var launcher = this.application.tabController.getActiveTab().getSiddhiFileEditor().getAsyncLauncher();
            // launcher.getSinkAndSources(this.application, this.sourceListSelector, this.sinkListSelector);

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
            console.log("SourceSinkSelectorDialog.prototype.render()");
            console.log(this.sinkListSelector.length);
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
                            console.log("SourceSinkSelectorDialog.prototype.render() - " + siddhiAppName);
                            if (activeTab.getFile().getRunStatus() !== undefined && !activeTab.getFile().getRunStatus() &&
                                !activeTab.getFile().getDebugStatus()) {
                                AsyncAPIRESTClient.getSourcesAndSinks(
                                    siddhiAppName,
                                    function (responseData) {
                                        console.log("AsyncAPILaunchManager.prototype.getSinkAndSources.sinks self");
                                        var sinks = responseData.sinks;
                                        console.log(self.sinkListSelector.length);
                                        console.log("AsyncAPILaunchManager.prototype.getSinkAndSources.sources this");
                                        var sources = responseData.sources;
                                        console.log(self.sourceListSelector.length);
                                        var sinkArray = [];
                                        for (var i=0; i<sinks.length; i++) {
                                            sinkArray.push(sinks[i].streamDefinition)
                                        }
                                        self.sinkListSelector.html(generateCheckboxListOptions(sinkArray));
                                        var sourceArray = [];
                                        for (var i=0; i<sinks.length; i++) {
                                            sourceArray.push(sources[i].streamDefinition)
                                        }
                                        self.sourceListSelector.html(generateCheckboxListOptions(sourceArray));
                                        // for (var i=0; i<sources.length; i++) {
                                        //     var name = sources[0].type + ":" + sources[0].streamDefinition;
                                        //     console.log(name);
                                        //     var radioBtn = $('<input type="radio" name="' + name + '" />');
                                        //     radioBtn.appendTo(self.sourceListSelector);
                                        // }
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
        };

        SourceSinkSelectorDialog.prototype.sinkSourceDisplayMethod = function (data) {
        };

        return SourceSinkSelectorDialog;
    });
