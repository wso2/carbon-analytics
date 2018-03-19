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

define(['require', 'lodash', 'jquery', 'log', 'backbone', 'file_browser', 'bootstrap','ace/ace'], function
    (require, _, $, log, Backbone) {
    var ExportFileDialog = Backbone.View.extend(
        /** @lends ExportFileDialog.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class ExportFileDialog
             * @param {Object} config configuration options for the ExportFileDialog
             */
            initialize: function (options) {
                this.app = options;
            },

            render: function () {
                var app = this.app;
                var title = app.tabController.getActiveTab().getTitle();
                var activeTab = app.tabController.activeTab;
                var siddhiFileEditor= activeTab.getSiddhiFileEditor();
                var config = siddhiFileEditor.getContent();
                var payload = new Blob([config], {type: "text/plain; charset=utf-8"});
                var downloadLink = document.createElement("a");
                    downloadLink.download = title;
                    downloadLink.innerHTML = "export File";
                    downloadLink.href = window.URL.createObjectURL(payload);
                    downloadLink.onclick = destroyClickedElement;
                    downloadLink.style.display = "none";
                    document.body.appendChild(downloadLink);
                    window.URL.revokeObjectURL(payload);
                    downloadLink.click();

                function destroyClickedElement(event) {
                    document.body.removeChild(event.target);
                }
            }
        });

    return ExportFileDialog;
});
