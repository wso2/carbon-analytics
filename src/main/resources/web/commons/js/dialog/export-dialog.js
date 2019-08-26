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

define(['require', 'jquery', 'log', 'backbone', 'smart_wizard'],
    function (require, $, log, Backbone, smartWizard) {
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
                    var form = exportContainer.find('#export-form');

                    if (isDocker) {
                        heading.text('Export Siddhi Apps for Docker image');
                    } else {
                        heading.text('Export Siddhi Apps For Kubernetes CRD');
                        form.find('#form-steps')
                            .append('<li><a href="#step-6">Step 6<br/><small>Add Kubernetes Config</small></a></li>');

                        form.find('')
                            .append('<div id="step-6" >' +
                                    '    <label>Kubernetes Config</label>' +
                                    '</div>');
                    }

                    // Toolbar extra buttons
                    var btnFinish = $('<button type="button" class="btn btn-default" hidden data-dismiss="modal" id="finish-btn">Finish</button>')
                                        .on('click', function(){alert('Finish Clicked');});
                    form.smartWizard({
                        selected: 0,
                        autoAdjustHeight: false,
                        theme: 'none',
                        transitionEffect: 'fade',
                        showStepURLhash: false,
                        contentCache: false,
                        toolbarSettings: {
                            toolbarPosition: 'bottom',
                            toolbarExtraButtons: [btnFinish]
                        }

                    });

                    form.on("showStep", function(e, anchorObject, stepNumber, stepDirection, stepPosition) {
                        log.info(stepNumber);
                        log.info(stepDirection);

                        // Finish button enable/disable
                        if (stepPosition === 'first') {
                            $("#prev-btn").addClass('disabled');
                        } else if (stepPosition === 'final') {
                            $("#next-btn").addClass('disabled');
                            $("#finish-btn").removeClass('hidden');
                            $("#finish-btn").removeClass('disabled');
                        } else {
                            $("#prev-btn").removeClass('disabled');
                            $("#next-btn").removeClass('disabled');
                        }
                    });
                    this._exportContainer = exportContainer;
                }
            });
        return ExportDialog;
    });
