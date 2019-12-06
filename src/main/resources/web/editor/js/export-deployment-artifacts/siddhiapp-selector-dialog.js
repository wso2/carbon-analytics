/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['jquery', 'lodash', 'log', 'file_browser', 'js_tree'],
    function ($,  _, log, FileBrowser, jsTree) {

        var SiddhiAppSelectorDialog = function (application, form, exportType) {
            var siddhiAppSelectorStep = form.find("#siddhi-apps-tree");
            var openFileWizardError = form.find("#select-siddhi-app");
            var fileBrowser = new FileBrowser({
                container: siddhiAppSelectorStep,
                application: application,
                fetchFiles: true,
                showWorkspace: true,
                multiSelect: true
            });

            this.siddhiAppSelectorStep = siddhiAppSelectorStep;
            this.fileBrowser = fileBrowser;
            this.openFileWizardError = openFileWizardError;
            this.pathSeparator = application.getPathSeperator();
            this.exportType = exportType;
            this.form = form;
            this.siddhiProcessName = "sample-siddhi-process";

        };

        SiddhiAppSelectorDialog.prototype.constructor = SiddhiAppSelectorDialog;

        SiddhiAppSelectorDialog.prototype.render = function () {
            var self = this;
            var openFileWizardError = this.openFileWizardError;
            var fileBrowser = this.fileBrowser;
            var siddhiAppSelectorStep = this.siddhiAppSelectorStep;

            if (self.exportType == 'docker') {
                self.form.find("#sp-name-input-row").hide();
            }

            fileBrowser.render();
            siddhiAppSelectorStep.on('ready.jstree', function () {
                siddhiAppSelectorStep.jstree("open_all");
            });
            fileBrowser.on("selected", function (events) {
                openFileWizardError.css('opacity', '0.6');
                openFileWizardError.css('background-color', 'transparent');
                if (events.length > 0) {
                   var i;
                   for(i=0; i < events.length; i++) {
                       if(events[i].endsWith('.siddhi')){
                          if (events[i].endsWith('.siddhi')) {
                             splitEvent = events[i].split("/");
                             if (splitEvent.length > 0) {
                                var siddhiProcessName = splitEvent[splitEvent.length-1]
                                                            .trim().toLowerCase()
                                                            .replace(".siddhi", "")
                                                            .replace(/[^a-z0-9\-]/g, "-");
                                siddhiProcessName = siddhiProcessName + "-1-0-0";
                                self.siddhiProcessName = siddhiProcessName;
                                self.form.find("#sp-name-input-field").val(siddhiProcessName);
                             }
                             break;
                          }
                          break;
                       }
                   }
                }
            });

        };

        SiddhiAppSelectorDialog.prototype.getSiddhiApps = function () {
            var siddhiApps = [];
            var files = this.fileBrowser.getSelected();
            for (var i = 0; i < files.length; i++) {
                var fileName = _.last(files[i].id.split(this.pathSeparator));
                // prevent directory also to be selected
                if (fileName.lastIndexOf(".siddhi") !== -1) {
                    siddhiApps.push(fileName);
                }
            }
            return siddhiApps;
        };

        SiddhiAppSelectorDialog.prototype.getSiddhiProcessName = function () {
            var self = this;
            return self.form.find("#sp-name-input-field").val();
        };

        return SiddhiAppSelectorDialog;
    });
