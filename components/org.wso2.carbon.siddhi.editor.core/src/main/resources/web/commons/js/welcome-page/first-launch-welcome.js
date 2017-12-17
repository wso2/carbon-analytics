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
define(['require', 'lodash', 'log', 'jquery', 'backbone', 'command', 'sample_preview', 'workspace/file'],
    function (require, _, log, $, Backbone, CommandManager, SamplePreviewView, File) {

        var FirstLaunchWelcomePage = Backbone.View.extend({
            initialize: function (options) {
                var errMsg;
                if (!_.has(options, 'tab')) {
                    errMsg = 'unable to find a reference for editor tab';
                    log.error(errMsg);
                    throw errMsg;
                }
                    this._tab = _.get(options, 'tab');
                var container = $(this._tab.getContentContainer());
                // make sure default tab content are cleared
                container.empty();
                // check whether container element exists in dom
                if (!container.length > 0) {
                    errMsg = 'unable to find container for welcome screen with selector: ' +
                        _.get(options, 'container');
                    log.error(errMsg);
                    throw errMsg;
                }
                this.app = options.application;
                this._$parent_el = container;
                this._options = options;
            },

            hide: function(){
                //Hiding menu bar
                this._options.application.menuBar.show();
                this.$el.hide();
            },

            show: function(){
                //Hiding menu bar
                this._options.application.menuBar.hide();
                this.$el.show();
            },

            render: function () {
                var self = this;
                var backgroundDiv = $('<div></div>');
                var mainWelcomeDiv = $('<div></div>');
                var leftPane = $('<div></div>');
                var buttonWrap = $('<div></div>');
                var productNameWrap = $('<div></div>');
                var newButton = $('<button></button>');
                var openButton = $('<button></button>');

                var contentPane = $('<div></div>');
                var recentFilesPane = $('<div></div>');
                var samplesPane = $('<div></div>');
                var quickLinksPane = $('<div></div>');

                backgroundDiv.addClass(_.get(this._options, 'cssClass.parent'));
                mainWelcomeDiv.addClass(_.get(this._options, 'cssClass.outer'));
                leftPane.addClass(_.get(this._options, 'cssClass.leftPane'));
                buttonWrap.addClass(_.get(this._options, 'cssClass.buttonWrap'));
                productNameWrap.addClass(_.get(this._options, 'cssClass.productNameWrap'));
                newButton.addClass(_.get(this._options, 'cssClass.buttonNew'));
                openButton.addClass(_.get(this._options, 'cssClass.buttonOpen'));
                contentPane.addClass(_.get(this._options, 'cssClass.contentPane'));
                recentFilesPane.addClass(_.get(this._options, 'cssClass.recentFilesPane'));
                samplesPane.addClass(_.get(this._options, 'cssClass.samplesPane'));
                quickLinksPane.addClass(_.get(this._options, 'cssClass.quickLinksPane'));

                newButton.text("New");
                openButton.text("Open");
                buttonWrap.append(newButton);
                buttonWrap.append(openButton);

                var productNameWrapHeader = $('<h2><img src="/editor/commons/images/wso2-logo.svg">' +
                    '<h1>Stream Processor Studio</h1></h2>');
                productNameWrap.append(productNameWrapHeader);


                leftPane.append(buttonWrap);
                leftPane.append(productNameWrap);

                mainWelcomeDiv.append(leftPane);

                var recentFilesHeader = $('<h4>Recently opened</h4>');
                recentFilesPane.append(recentFilesHeader);

                var samplesHeader = $('<h4 class="margin-top-60">Try out samples</h4>');
                samplesPane.append(samplesHeader);
                var bodyUlSampleContent = $('<ul class="recent-files clearfix"></ul>');
                bodyUlSampleContent.attr('id', "sampleContent");
                samplesPane.append(bodyUlSampleContent);

                var quickLinkHeader = $('<h4 class="margin-top-60">Quick links</h4>');
                quickLinksPane.append(quickLinkHeader);

                var bodyUlQuickLinkContent = $('<ul class="quick-links col-md-12 col-lg-8">' +
                    '<li class="col-md-4"><a href="https://docs.wso2.com/display/SP400/Quick+Start+Guide"' +
                    'target="_blank"><i class="fw fw-list"></i>Quick Start Guide</a></li>' +
                    '<li class="col-md-4"><a href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/"' +
                    'target="_blank"><i class="fw fw-carbon"></i>Siddhi Grammar</a></li>' +
                    '<li class="col-md-4"><a href="https://stackoverflow.com/questions/tagged/wso2-sp"' +
                    'target="_blank"><i class="fw fw-info"></i>Q&A</a></li>' +
                    '<li class="col-md-4"><a href="https://docs.wso2.com/display/SP400/Tutorials"' +
                    'target="_blank"><i class="fw fw-text"></i>Tutorials</a></li>' +
                    '<li class="col-md-4"><a href="https://docs.wso2.com/display/SP400/Samples"' +
                    'target="_blank"><i class="fw fw-application"></i>Samples</a></li>' +
                    '<li class="col-md-4"><a href="http://wso2.com/support/"' +
                    'target="_blank"><i class="fw fw-ringing"></i>Support</a></li></ul>');

                quickLinksPane.append(bodyUlQuickLinkContent);
                contentPane.append(samplesPane);
                contentPane.append(quickLinksPane);

                mainWelcomeDiv.append(contentPane);
                backgroundDiv.append(mainWelcomeDiv);

                this._$parent_el.append(backgroundDiv);
                this.$el = backgroundDiv;

                var command = this._options.application.commandManager;
                var browserStorage = this._options.application.browserStorage;

                var samples = _.get(this._options, "samples", []);
                
                var config;
                var samplePreview;

                var self = this;
                var workspaceServiceURL = self.app.config.services.workspace.endpoint;
                var sampleServiceURL = workspaceServiceURL + "/read/sample";

                for (var i = 0; i < samples.length; i++) {

                    var payload = samples[i];

                    $.ajax({
                        type: "POST",
                        contentType: "text/plain; charset=utf-8",
                        url: sampleServiceURL,
                        data: payload,
                        async: false,
                        success: function(data, textStatus, xhr) {
                            var content = {"content": data.content};
                            var config =
                                {
                                    "sampleName": samples[i].replace(/^.*[\\\/]/, '').match(/[^.]*/i)[0],
                                    "parentContainer": "#sampleContent",
                                    "firstItem": i === 0,
                                    "clickEventCallback": function (event) {
                                        event.preventDefault();
                                        var file = new File({
                                            content: data.content
                                            },{
                                            storage: browserStorage
                                        });
                                        self.app.commandManager.dispatch("create-new-tab", {tabOptions: {file: file}});
                                        browserStorage.put("pref:passedFirstLaunch", true);
                                    }
                                };
                            samplePreview = new SamplePreviewView(config);
                            samplePreview.render();
                        },
                        error: function() {
                            alerts.error("Unable to read a sample file.");
                            throw "Unable to read a sample file.";
                        }
                    });
                }

                var workExPath = 'workspace';
                command.dispatch("open-folder", workExPath);

                var command = this._options.application.commandManager;
                var browserStorage = this._options.application.browserStorage;

                // When "new" is clicked, open up an empty workspace.
                $(newButton).on('click', function () {
                    command.dispatch("create-new-tab");
                    browserStorage.put("pref:passedFirstLaunch", true);
                });

                // Show the open file dialog when "open" is clicked.
                $(openButton).click(function(){
                    command.dispatch("open-file-open-dialog");
                    browserStorage.put("pref:passedFirstLaunch", true);
                });

                // upon welcome tab remove, set flag to indicate first launch pass
                this._tab.on('removed', function(){
                    browserStorage.put("pref:passedFirstLaunch", true);
                });
            }
        });

        return FirstLaunchWelcomePage;

    });

