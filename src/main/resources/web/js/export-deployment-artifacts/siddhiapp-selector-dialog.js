/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['jquery', 'lodash', 'log', 'file_browser', 'js_tree'],
    function ($,  _, log, FileBrowser, jsTree) {

        var SiddhiAppSelectorDialog = function (application, form) {
            var siddhiAppSelectorStep = form.find("#siddiAppsTree");
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

        };

        SiddhiAppSelectorDialog.prototype.constructor = SiddhiAppSelectorDialog;

        SiddhiAppSelectorDialog.prototype.render = function () {
            var openFileWizardError = this.openFileWizardError;
            var fileBrowser = this.fileBrowser;
            var siddhiAppSelectorStep = this.siddhiAppSelectorStep;

            fileBrowser.render();
            siddhiAppSelectorStep.on('ready.jstree', function () {
                siddhiAppSelectorStep.jstree("open_all");
            });
            fileBrowser.on("selected", function () {
                openFileWizardError.addClass('step-description');
                openFileWizardError.css('background-color', 'transparent');
            });

        };
        
        SiddhiAppSelectorDialog.prototype.validateSiddhiAppSelection = function () {
            var files = this.fileBrowser.getSelected();
            if (files.length === 0) {
                this.openFileWizardError.removeClass('step-description');
                this.openFileWizardError.css('background-color', '#d9534f !important');
                return false;
            }
            return true;
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

        return SiddhiAppSelectorDialog;
    });
