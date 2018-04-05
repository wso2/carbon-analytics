/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['require', 'lodash', 'jquery', 'log', 'backbone', 'file_browser', 'workspace/file'],
    function (require, _, $, log, Backbone, FileBrowser, File) {
        var ImportFileDialog = Backbone.View.extend(
            /** @lends SaveToFileDialog.prototype */
            {
                /**
                 * @augments Backbone.View
                 * @constructs
                 * @class SaveToFileDialog
                 * @param {Object} config configuration options for the SaveToFileDialog
                 */
                initialize: function (options) {
                    this.app = options;
                    this.pathSeparator = this.app.getPathSeperator();
                    this.dialog_container = $(_.get(options.config.dialog, 'container'));
                    this.notification_container = _.get(options.config.tab_controller.tabs.tab.das_editor.notifications,
                        'container');
                    this.source_view_container = _.get(options.config.tab_controller.tabs.tab.das_editor,
                        'source_view.container');
                },

                render: function () {

                    var self = this;
                    var app = this.app;
                    var fileContent;
                    var fileName;
                    var notification_container = this.notification_container;


                    var ImportLink = document.createElement("input");
                    ImportLink.type = "file";
                    ImportLink.name = "File";
                    ImportLink.accept = ".siddhi";
                    ImportLink.onchange = handleFiles;
                    ImportLink.onclick = destroyClickedElement;
                    ImportLink.style.display = "none";
                    document.body.appendChild(ImportLink);
                    ImportLink.click();

                    function destroyClickedElement(event) {
                        document.body.removeChild(event.target);
                    }

                    function handleFiles(event) {
                        var files = event.target.files;
                        var file = files[0];
                        var reader = new FileReader();

                        fileName = file.name;
                        reader.onload = (function (reader) {
                            return function () {
                                fileContent = reader.result;
                            }
                        })(reader);

                        reader.readAsText(file);
                    }
                },
            });

        return ImportFileDialog;
    });
