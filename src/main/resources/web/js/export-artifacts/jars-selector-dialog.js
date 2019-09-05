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

define(['jquery', 'lodash', 'log', 'file_browser', /** void module - jquery plugin **/ 'js_tree'],
    function ($, _, log, FileBrowser) {
        var JarsSelectorDialog = function (application, form) {
            var bundlesSelector = form.find("#bundlesTree");
            var bundlesBrowser = new FileBrowser({
                container: bundlesSelector,
                application: application,
                fetchFiles: true,
                showBundles: true,
                multiSelect: true
            });

            this.bundlesSelector = bundlesSelector;
            this.bundlesBrowser = bundlesBrowser;
            this.pathSeparator = application.getPathSeperator();

        };

        JarsSelectorDialog.prototype.constructor = JarsSelectorDialog;

        JarsSelectorDialog.prototype.render = function () {
            this.bundlesBrowser.render();

            var bundlesSelector = this.bundlesSelector;
            bundlesSelector.on('ready.jstree', function () {
                bundlesSelector.jstree("open_all");
            });
        };

        JarsSelectorDialog.prototype.getSelected = function (directoryName) {
            var bundles = [];
            var files = this.bundlesBrowser.getBottomSelected();
            for (var i = 0; i < files.length; i++) {
                if (files[i].id != directoryName && files[i].id.startsWith(directoryName)) {
                    var fileName = _.last(files[i].id.split(this.pathSeparator));
                    bundles.push(fileName);

                }
            }
            return bundles;
        };
        return JarsSelectorDialog;
    });
