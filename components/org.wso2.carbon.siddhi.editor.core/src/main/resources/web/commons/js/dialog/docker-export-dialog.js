/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['require', 'jquery', 'log', 'backbone', 'file_browser'],
    function (require, $, log, Backbone, FileBrowser) {
        var DockerExportDialog = Backbone.View.extend(
            /** @lends DockerExportDialog.prototype */
            {
                /**
                 * @augments Backbone.View
                 * @constructs
                 * @class DockerExportDialog
                 * @param {Object} options configuration options
                 */
                initialize: function (options) {
                    this.app = options;
                    this.pathSeparator = this.app.getPathSeperator();
                    this.dialogContainer = $(_.get(this.app, 'config.dialog.container'));
                },

                show: function () {
                    this.modalContainer.modal('show');
                },

                render: function () {
                    var self = this,
                        options = _.get(self.app, 'config.docker_export_dialog');

                    if(!_.isNil(this.modalContainer)){
                        this.modalContainer.remove();
                    }

                    var exportModal = $(options.selector).clone();
                    var treeContainer = exportModal.find('#file-tree');
                    var exportButton = exportModal.find('#exportButton');

                    var fileBrowser = new FileBrowser({container: treeContainer, application: self.app, fetchFiles: true,
                        showWorkspace: true, multiSelect: true});
                    $(treeContainer).on('ready.jstree', function () {
                        $(treeContainer).jstree("open_all");
                    });
                    fileBrowser.render();
                    this.fileBrowser = fileBrowser;

                    this.listenTo(fileBrowser, 'selected', function (files) {
                        if (files.length > 0) {
                            exportButton.removeAttr('disabled');
                        } else {
                            exportButton.attr('disabled', 'disabled');
                        }
                    });

                    exportButton.on('click', function() {
                        var payload = {
                            profile: exportModal.find('#dockerProfile').val(),
                            files: []
                        };

                        var files = self.fileBrowser.getSelected();
                        for (var i = 0; i < files.length; i++) {
                            var fileName = _.last(files[i].id.split(self.pathSeparator));
                            if (fileName.lastIndexOf(".siddhi") !== -1) {
                                payload.files.push(fileName);
                            }
                        }
                        var downloadURL = window.location.protocol + "//" + window.location.host +
                            "/editor/docker/download?q=" + encodeURIComponent(JSON.stringify(payload));
                        $('#frm-download').attr('src', downloadURL);
                    });

                    this.dialogContainer.append(exportModal);
                    this.modalContainer = exportModal;
                }
            });
        return DockerExportDialog;
    });
