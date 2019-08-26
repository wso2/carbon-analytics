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

define(['require', 'jquery', 'log', 'backbone', 'smart_wizard', 'file_browser'],
    function (require, $, log, Backbone, smartWizard, FileBrowser) {
        var siddhiApps = [];

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
                    this.app = options;
                    this.options = _.cloneDeep(_.get(options.config, 'export_dialog'));
                    this.isDocker = isDocker;
                    this.pathSeparator = this.app.getPathSeperator();
                },

                show: function () {
                    this.exportContainer.modal('show');
                },

                render: function () {
                    var isDocker = this.isDocker;
                    var options = this.options;
                    var app = this.app;
                    var pathSeparator = this.pathSeparator;

                    if (!_.isNil(this.exportContainer)) {
                        this.exportContainer.remove();
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

                        form.find('#form-containers')
                            .append('<div id="step-6" >' +
                                    '    <label>Kubernetes Config</label>' +
                                    '</div>');
                    }

                    // Toolbar extra buttons
                    var btnFinish = $('<button type="button" class="btn btn-default" data-dismiss="modal" id="finish-btn">Finish</button>')
                                        .addClass('hidden')
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

                    var treeContainer = form.find("#siddiAppsTree");
                    var openFileWizardError = form.find("#select-siddhi-app-error");
                    openFileWizardError.hide();
                    var fileBrowser = new FileBrowser({
                        container: treeContainer,
                        application: app,
                        fetchFiles: true,
                        showWorkspace: true,
                        multiSelect: true
                    });
                    $(treeContainer).on('ready.jstree', function () {
                        $(treeContainer).jstree("open_all");
                    });
                    fileBrowser.render();

                    fileBrowser.on("selected", function () {openFileWizardError.hide();});

                    // Initialize the leaveStep event - validate before next
                    form.on("leaveStep", function(e, anchorObject, stepNumber, stepDirection) {
                        if (stepDirection === 'forward') {
                            if (stepNumber === 0) {
                                return validateSiddhiApps();
                            }
                        }
                    });

                    // Step is passed successfully
                    form.on("showStep", function(e, anchorObject, stepNumber, stepDirection, stepPosition) {
                        // Finish button enable/disable
                        if (stepPosition === 'first') {
                            $("#prev-btn").addClass('disabled');
                        } else if (stepPosition === 'final') {
                            $("#next-btn").addClass('disabled');
                            $("#finish-btn").removeClass('hidden disabled');
                        } else {
                            $("#prev-btn").removeClass('disabled');
                            $("#next-btn").removeClass('disabled');
                        }

                        if (stepDirection === 'forward') {
                            if (stepNumber === 1) {
                                getSiddhiApps();
                            }
                        }
                    });

                    function validateSiddhiApps() {
                        var files = fileBrowser.getSelected();
                        if (files.length === 0) {
                            openFileWizardError.text("Select Siddhi Apps To Export");
                            openFileWizardError.show();
                            return false;
                        }
                        return true;
                    }

                    function getSiddhiApps() {
                        siddhiApps = [];
                        var files = fileBrowser.getSelected();
                        for (var i = 0; i < files.length; i++) {
                            var fileName = _.last(files[i].id.split(pathSeparator));
                            if (fileName.lastIndexOf(".siddhi") !== -1) {
                                siddhiApps.push(fileName);
                            }
                        }
                    }

                    this.exportContainer = exportContainer;
                }
            });
        return ExportDialog;
    });
