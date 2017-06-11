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
                    errMsg = 'unable to find container for welcome screen with selector: ' + _.get(options, 'container');
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
                var headingDiv = $('<div></div>');
                var headingTitleSpan = $('<span></span>');
                var headingTitleSpan2 = $('<span></span>');
                var headingGroup1 = $('<div></div>');
                var wrapTitle = $('<div></div>');

                var bodyDiv = $('<div></div>');
                var bodyDivSampleDoc = $('<div></div>');
                bodyDivSampleDoc.attr('id', "documentContent");
                var bodyDivSampleContent = $('<div></div>');
                bodyDivSampleContent.addClass('col-sm-6');
                var bodyUlSampleContent = $('<ul></ul>');
                bodyUlSampleContent.attr('id', "sampleContent");
                var newButton = $('<button></button>');
                var openButton = $('<button></button>');
                var buttonGroup1 = $('<div></div>');                

                var bodyTitleSpan = $('<span></span>');
                //var samplesDiv = $('<div></div>');

                backgroundDiv.addClass(_.get(this._options, 'cssClass.parent'));
                mainWelcomeDiv.addClass(_.get(this._options, 'cssClass.outer'));
                headingDiv.addClass(_.get(this._options, 'cssClass.heading'));
                headingTitleSpan.addClass(_.get(this._options, 'cssClass.headingTitle'));
                headingTitleSpan2.addClass(_.get(this._options, 'cssClass.headingTitle'));
                newButton.addClass(_.get(this._options, 'cssClass.buttonNew'));
                openButton.addClass(_.get(this._options, 'cssClass.buttonOpen'));
                headingGroup1.addClass(_.get(this._options, 'cssClass.headingTop'));
                buttonGroup1.addClass(_.get(this._options, 'cssClass.btnWrap1'));
                

                bodyDiv.addClass(_.get(this._options, 'cssClass.body'));

                bodyDivSampleDoc.addClass('col-sm-6');
                var bodyDivSampleDocHeader = $('<h2>Documentation</h2>');
                var bodyDivSampleDocUl = $('<ul><li><a href="https://docs.wso2.com/display/DAS400/Quick+Start+Guide" target="_blank">'+
                'Quick Start Guide</a></li><li><a href="https://docs.wso2.com/display/DAS400/Key+Concepts" target="_blank">Key Concept</a></li>'+
                                      '<li><a href="https://docs.wso2.com/display/DAS400/Tutorials" target="_blank">Tutorials</a></li>' +
                                       '<li><a href="https://docs.wso2.com/display/CEP420/SiddhiQL+Guide+3.1" target="_blank">Siddhi Grammer</a></li></ul>');
                bodyDivSampleDoc.append(bodyDivSampleDocHeader);
                bodyDivSampleDoc.append(bodyDivSampleDocUl);
                var bodyDivSampleContentHeader = $('<h2>Samples</h2>');
                bodyDivSampleContent.append(bodyDivSampleContentHeader);
                bodyDivSampleContent.append(bodyUlSampleContent);
                bodyTitleSpan.addClass(_.get(this._options, 'cssClass.bodyTitle'));

                newButton.text("New");
                openButton.text("Open");

                headingTitleSpan.text("Welcome to");
                headingTitleSpan2.text("Data Analytics Composer");

                wrapTitle.append(headingTitleSpan);
                wrapTitle.append(headingTitleSpan2);
                headingGroup1.append(wrapTitle);

                buttonGroup1.append(newButton);
                buttonGroup1.append(openButton);

                headingDiv.append(headingGroup1);
                headingDiv.append(buttonGroup1);                

                bodyDiv.append(bodyTitleSpan);
                bodyDiv.append(bodyDivSampleDoc);
                bodyDiv.append(bodyDivSampleContent);

                mainWelcomeDiv.append(headingDiv);
                mainWelcomeDiv.append(bodyDiv);
                backgroundDiv.append(mainWelcomeDiv);

                this._$parent_el.append(backgroundDiv);
                this.$el = backgroundDiv;

                var innerDiv = $('<div></div>');
                innerDiv.attr('id', "innerSamples");

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

