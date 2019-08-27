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

define(['jquery', 'backbone', 'lodash', 'log', 'file_browser', /** void module - jquery plugin **/ 'js_tree'],
    function ($, Backbone, _, log, FileBrowser) {

    return Backbone.View.extend({

        initialize: function (config) {
            var errMsg;
            if (!_.has(config, 'form')) {
                errMsg = 'unable to find configuration for form';
                log.error(errMsg);
                throw errMsg;
            }
            var form = $(_.get(config, 'form'));
            // check whether form element exists in dom
            if (!form.length > 0) {
                errMsg = 'unable to find form for file browser with selector: ' + _.get(config, 'form');
                log.error(errMsg);
                throw errMsg;
            }
            this._$parent_el = form;

            if (!_.has(config, 'application')) {
                log.error('Cannot init file browser. config: application not found.')
            }

            this.application = _.get(config, 'application');
            this.siddhiAppSelectorStep = form.find("#siddiAppsTree");
            this.openFileWizardError = form.find("#select-siddhi-app-error");
            this.fileBrowser = new FileBrowser({
                container: this.siddhiAppSelectorStep,
                application: this.application,
                fetchFiles: true,
                showWorkspace: true,
                multiSelect: true
            });
            this.pathSeparator =  this.application.getPathSeperator();
        },

        validateSiddhiApps: function() {
            var files = this.fileBrowser.getSelected();
            if (files.length === 0) {
                this.openFileWizardError.text("Select Siddhi Apps To Export");
                this.openFileWizardError.show();
                return false;
            }
            return true;
        },

        render: function() {
            this.openFileWizardError.hide();
            this.siddhiAppSelectorStep.on('ready.jstree', function () {
                this.siddhiAppSelectorStep.jstree("open_all");
            });
            this.fileBrowser.render();
            this.fileBrowser.on("selected", function () {
                this.openFileWizardError.hide();
            });
        },

        getSiddhiApps: function () {
            var siddhiApps = [];
            var files = this.fileBrowser.getSelected();
            for (var i = 0; i < files.length; i++) {
                var fileName = _.last(files[i].id.split(this.pathSeparator));
                if (fileName.lastIndexOf(".siddhi") !== -1) {
                    siddhiApps.push(fileName);
                }
            }
            return siddhiApps;
        }
    });

});
