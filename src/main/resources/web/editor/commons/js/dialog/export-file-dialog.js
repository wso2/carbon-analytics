/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash', 'jquery', 'log', 'backbone', 'file_browser', 'bootstrap', 'ace/ace'], function
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
                var siddhiFileEditor = activeTab.getSiddhiFileEditor();
                var config = siddhiFileEditor.getContent();
                var payload = new Blob([config], {type: "text/plain; charset=utf-8"});
                var downloadLink = document.createElement("a");
                downloadLink.download = title;
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
