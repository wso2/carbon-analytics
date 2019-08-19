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

define(['require', 'jquery', 'log', 'backbone'],
    function (require, $, log, Backbone) {
        var ExportDialog = Backbone.View.extend(
            /** @lends ExportDialog.prototype */
            {
                /**
                 * @augments Backbone.View
                 * @constructs
                 * @class ExportDialog
                 * @param {Object} options configuration options
                 * @param {boolean} isDocker  is Docker File Export
                 */
                initialize: function (options, isDocker) {
                    this.app = options;
                    this.isDocker = isDocker;
                },

                show: function () {
                    this.modalContainer.modal('show');
                },

                render: function () {
                    log.info("isDocker " + this.isDocker);
                    this.modalContainer = $(
                        "<div class='modal fade' id='openConfigModal' tabindex='-1' role='dialog' >" +
                        "<div class='modal-dialog file-dialog' role='document'>" +
                        "<div class='modal-content'>" +
                        "<div class='modal-header'>" +
                        "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                        "<i class='fw fw-cancel about-dialog-close'>" +
                        "</i>" +
                        "</button>" +
                        "<h4 class='modal-title file-dialog-title'>Exporting Siddhi Apps For Docker</h4>" +
                        "<hr class='style1'>" +
                        "</div>" +
                        "</div>" +
                        "</div>" +
                        "</div>"
                    );
                }
            });
        return ExportDialog;
    });
