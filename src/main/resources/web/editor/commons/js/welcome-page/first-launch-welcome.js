/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash', 'log', 'jquery', 'backbone', 'command', 'sample_preview', 'workspace/file', 'enjoyhint', 'guide', 'version'],
    function (require, _, log, $, Backbone, CommandManager, SamplePreviewView, File, EnjoyHint, Guide, Version) {

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

            hide: function () {
                //Hiding menu bar
                this._options.application.menuBar.show();
                this.$el.hide();
            },

            show: function () {
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
                var newButton = $('<button id="newButton"></button>');
                var openButton = $('<button></button>');

                var contentPane = $('<div></div>');
                var scrollInner = $('<div class="nano-content"></div>');
                var scrollWrapper = $('<div class="nano"></div>');
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

                $('#query-guide-link-container').append('<a href="https://siddhi.io/en/v'+Version.MINOR_VERSION+
                    '/docs/query-guide/" target="_blank"><i class="fw-info"></i> Siddhi Query Guide</a>');
                $('#step-2 > div.step-description-text').append('Use ${...} format to template siddhi apps');
                $('#step-3 > div.step-description-text').append('Use ${...} format to template the configurations');

                var productNameWrapHeader = $('<img src="/editor/commons/images/siddhi-logo.svg">' +
                    '<h5 class="icontagline">Cloud Native Stream Processor</h5>');
                productNameWrap.append(productNameWrapHeader);


                leftPane.append(buttonWrap);
                leftPane.append(productNameWrap);

                mainWelcomeDiv.append(leftPane);

                var recentFilesHeader = $('<h4>Recently opened</h4>');
                recentFilesPane.append(recentFilesHeader);

                var samplesHeader = $('<h4 class="margin-top-60">Try out samples</h4>');
                samplesPane.append(samplesHeader)
                var bodyUlSampleContent = $('<ul class="recent-files clearfix"></ul>');
                var moreSampleLink = $('<a class="more-samples">' +
                    '<i class="fw fw-application"></i>More Samples</a>');
                bodyUlSampleContent.attr('id', "sampleContent");
                samplesPane.append(bodyUlSampleContent);
                samplesPane.append(moreSampleLink);

                // Show the import file dialog when "More Samples" is clicked.
                $(moreSampleLink).click(function () {
                    command.dispatch("open-sample-file-open-dialog");
                });

                var quickLinkHeader = $('<h4 class="margin-top-60">Quick links</h4>');
                quickLinksPane.append(quickLinkHeader);

                var bodyUlQuickLinkContent = $('<ul class="quick-links col-md-12 col-lg-8">' +
                    '<li class="col-md-4"><a href="https://siddhi.io/en/v'+Version.MINOR_VERSION+'/docs/quick-start/"' +
                    'target="_blank"><i class="fw fw-list"></i>Quick Start Guide</a></li>' +
                    '<li class="col-md-4"><a href="https://siddhi.io/en/v'+Version.MINOR_VERSION+'/docs/"' +
                    'target="_blank"><i class="fw fw-google-docs"></i>Documentation</a></li>' +
                    '<li class="col-md-4"><a href="https://siddhi.io/en/v'+Version.MINOR_VERSION+'/docs/query-guide/"' +
                    'target="_blank"><i class="fw fw-carbon"></i>Siddhi Query Guide</a></li>' +
                    '<li class="col-md-4"><a href="https://siddhi.io/community/"' +
                    'target="_blank"><i class="fw fw-info"></i>Q&A</a></li>' +
                    '<li class="col-md-4"><a href="https://siddhi.io/en/v'+Version.MINOR_VERSION+'/docs/api/'+Version.LATEST_PACK_VERSION+'/"' +
                    'target="_blank"><i class="fw fw-google-docs"></i>API Docs</a></li>'
                );

                quickLinksPane.append(bodyUlQuickLinkContent);
                scrollInner.append(samplesPane);
                scrollInner.append(quickLinksPane);
                scrollWrapper.append(scrollInner);
                contentPane.append(scrollWrapper);

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
                        indexValue: i,
                        data: payload,
                        async: true,
                        success: function (data, textStatus, xhr) {
                            var config =
                                {
                                    "sampleName": this.data.replace(/^.*[\\\/]/, '').match(/[^.]*/i)[0],
                                    "content": data.content,
                                    "parentContainer": "#sampleContent",
                                    "firstItem": this.indexValue === 0,
                                    "clickEventCallback": function (event) {
                                        event.preventDefault();
                                        var file = new File({
                                            content: data.content
                                        }, {
                                            storage: browserStorage
                                        });
                                        self.app.commandManager.dispatch("create-new-tab", {tabOptions: {file: file}});
                                        browserStorage.put("pref:passedFirstLaunch", true);
                                    }
                                };
                            samplePreview = new SamplePreviewView(config);
                            samplePreview.render();
                        },
                        error: function () {
                            alertError("Unable to read a sample file.");
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
                $(openButton).click(function () {
                    command.dispatch("open-file-open-dialog");
                    browserStorage.put("pref:passedFirstLaunch", true);
                });

                // upon welcome tab remove, set flag to indicate first launch pass
                this._tab.on('removed', function () {
                    browserStorage.put("pref:passedFirstLaunch", true);
                });

                function alertError(errorMessage) {
                    var errorNotification = getErrorNotification(errorMessage);
                    $("#notification-container").append(errorNotification);
                    errorNotification.fadeTo(2000, 200).slideUp(1000, function () {
                        errorNotification.slideUp(1000);
                    });
                }

                function getErrorNotification(errorMessage) {
                    return $(
                        "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-danger' id='error-alert'>" +
                        "<span class='notification'>" +
                        errorMessage +
                        "</span>" +
                        "</div>");
                };
            }
        });

        return FirstLaunchWelcomePage;

    });

