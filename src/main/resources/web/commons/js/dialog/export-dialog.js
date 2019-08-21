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
                 * @param {Object} options exportContainerModal
                 * @param {boolean} isDocker  is Docker File Export
                 */
                initialize: function (options, isDocker) {
                    this._options = options;
                    this.isDocker = isDocker;
                },

                show: function () {
                    this._exportContainer.modal('show');
                },

                render: function () {
                    var isDocker = this.isDocker;
                    var options = this._options;

                    if (!_.isNil(this._exportContainer)) {
                        this._exportContainer.remove();
                    }

                    var exportContainer = $(_.get(options, 'selector')).clone();
                    var heading = exportContainer.find('#initialHeading');
                    if (isDocker) {
                        heading.text('Exporting Siddhi Apps For Docker');
                    } else {
                        heading.text('Exporting Siddhi Apps For Kubernetes');
                    }
                    this._exportContainer = exportContainer;
                }
            });
        return ExportDialog;
    });
