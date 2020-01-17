/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'jquery', 'backbone', 'lodash', 'log', 'design_view', "./source", '../constants',
        'undo_manager', 'launcher', 'app/debugger/debugger', 'designViewUtils'],

    function (require, $, Backbone, _, log, DesignView, SourceView, constants, UndoManager, Launcher,
              DebugManager, DesignViewUtils) {

        const ENTER_KEY = 13;

        var ServicePreview = Backbone.View.extend(
            /** @lends ServicePreview.prototype */
            {
                /**
                 * @augments Backbone.View
                 * @constructs
                 * @class ServicePreview Represents the view for siddhi samples
                 * @param {Object} options Rendering options for the view
                 */
                initialize: function (options) {
                    if (!_.has(options, 'container')) {
                        throw "container is not defined."
                    }
                    var container = $(_.get(options, 'container'));
                    if (!container.length > 0) {
                        throw "container not found."
                    }
                    this._$parent_el = container;
                    this.options = options;
                    this._file = _.get(options, 'file');
                    // init undo manager
                    this._undoManager = new UndoManager();
                    this._launcher = new Launcher(options);

                },

                render: function () {
                    var self = this;
                    var canvasContainer = this._$parent_el.find(_.get(this.options, 'canvas.container'));
                    var previewContainer = this._$parent_el.find(_.get(this.options, 'preview.container'));
                    var loadingScreen = this._$parent_el.find(_.get(this.options, 'loading_screen.container'));
                    var sourceContainer = this._$parent_el.find(_.get(this.options, 'source.container'));
                    var designContainer = this._$parent_el.find(_.get(this.options, 'design_view.container'));
                    var debugContainer = this._$parent_el.find(_.get(this.options, 'debug.container'));
                    var tabContentContainer = $(_.get(this.options, 'tabs_container'));

                    if (!canvasContainer.length > 0) {
                        var errMsg = 'cannot find container to render svg';
                        log.error(errMsg);
                        throw errMsg;
                    }

                    // check whether design container element exists in dom
                    if (!designContainer.length > 0) {
                        errMsg = 'unable to find container for file composer with selector: '
                            + _.get(this.options, 'design_view.container');
                        log.error(errMsg);
                    }

                    var designViewDynamicId = "design-container-" + this._$parent_el.attr('id');
                    designContainer.attr('id', designViewDynamicId);

                    /*
                    * Use the below line to assign dynamic id for design grid container and pass the id to initialize
                    * jsPlumb.
                    *
                    * NOTE: jsPlumb is loaded via the index.html as a common script for the entire program. When a new
                    * tab is created, that tab is initialised with a dedicated jsPlumb instance.
                    * */
                    var designGridDynamicId = "design-grid-container-" + this._$parent_el.attr('id');
                    var designViewGridContainer =
                        this._$parent_el.find(_.get(this.options, 'design_view.grid_container'));
                    designViewGridContainer.attr('id', designGridDynamicId);

                    // initialise jsPlumb instance for design grid
                    this.jsPlumbInstance = jsPlumb.getInstance({
                        Container: designGridDynamicId
                    });

                    var sourceDynamicId = sourceContainer.attr('id') + this._$parent_el.attr('id');
                    sourceContainer.attr("id", sourceDynamicId);

                    var sourceViewOptions = {
                        sourceContainer: sourceDynamicId,
                        source: constants.INITIAL_SOURCE_INSTRUCTIONS,
                        app: self.options.application,
                        theme: "ace/theme/twilight",
                        font_size: "12px"
                    };

                    this._sourceView = new SourceView(sourceViewOptions);


                    /* Start Debug Related Stuff */
                    var debugConfOpts = {
                        debugger_instance: self._sourceView.getDebugger(),
                        editorInstance: self._sourceView.getEditor(),
                        option: self.options.application.config.debugger_instance

                    };

                    this._debugger = new DebugManager(debugConfOpts);

                    canvasContainer.removeClass('show-div').addClass('hide-div');
                    previewContainer.removeClass('show-div').addClass('hide-div');
                    designContainer.removeClass('show-div').addClass('hide-div');
                    sourceContainer.removeClass('source-view-disabled').addClass('source-view-enabled');
                    tabContentContainer.removeClass('tab-content-default');

                    this._sourceView.on('modified', function (changeEvent) {
                        if (self.getUndoManager().hasUndo()) {
                            // clear undo stack from design view
                            if (!self.getUndoManager().getOperationFactory()
                                    .isSourceModifiedOperation(self.getUndoManager().undoStackTop())) {
                                self.getUndoManager().reset();
                            }
                        }

                        if (self.getUndoManager().hasRedo()) {
                            // clear redo stack from design view
                            if (!self.getUndoManager().getOperationFactory()
                                    .isSourceModifiedOperation(self.getUndoManager().redoStackTop())) {
                                self.getUndoManager().reset();
                            }
                        }

                        _.set(changeEvent, 'editor', self);
                        self.getUndoManager().onUndoableOperation(changeEvent);
                        self.trigger('content-modified');
                    });

                    this._sourceView.on('dispatch-command', function (id) {
                        self.trigger('dispatch-command', id);
                    });

                    this._sourceView.render(sourceViewOptions);
                    if (self._file.getContent() !== undefined) {
                        self._sourceView.setContent(self._file.getContent());
                    }
                    this._sourceView.editorResize();

                    var application = self.options.application;
                    var designView = new DesignView(self.options, application, this.jsPlumbInstance);
                    this._designView = designView;
                    designView.setRawExtensions(this._sourceView.getRawExtensions())
                    designView.renderToolPalette();

                    var toggleViewButton = this._$parent_el.find(_.get(this.options, 'toggle_controls.toggle_view'));
                    var toggleViewButtonDynamicId = "toggle-view-button-" + this._$parent_el.attr('id');
                    toggleViewButton.attr('id', toggleViewButtonDynamicId);
                    toggleViewButton.click(function () {
                        if (sourceContainer.is(':visible')) {
                            if (application.tabController.getActiveTab().getFile().isDirty()) {
                                DesignViewUtils.prototype.warnAlert("Please save the file before switching to the Design View");
                                return;
                            }

                            loadingScreen.show();
                            setTimeout(function(){
                                var response = self._designView.getDesign(self.getContent());
                                if (response.status === "success") {
                                    self.JSONObject = JSON.parse(response.responseString);
                                    sourceContainer.hide();
                                    // The following code has been added to the setTimeout() method because
                                    // the code needs to run asynchronously for the loading screen
                                    setTimeout(function () {
                                        var fileHashCode = application.tabController.getActiveTab().getFile().attributes.hashCode;
                                        var renderedAppContentHashCode = designView.getHashCode();
                                        if (fileHashCode != renderedAppContentHashCode || fileHashCode == undefined &&
                                            renderedAppContentHashCode == undefined) {
                                            designView.setHashCode(fileHashCode);
                                            designView.emptyDesignViewGridContainer();
                                            designContainer.show();
                                            designView.renderDesignGrid(self.JSONObject);
                                            loadingScreen.hide();
                                        } else {
                                            designContainer.show();
                                            loadingScreen.hide();
                                        }
                                        // NOTE - This trigger should be always handled at the end of setTimeout()
                                        self.trigger("view-switch", { view: 'design' });
                                    }, 100);
                                    toggleViewButton.html("<i class=\"fw fw-code\"></i>" +
                                        "<span class=\"toggle-button-text\">Source View</span>");
                                } else if (response.status === "fail") {
                                    loadingScreen.hide();
                                    DesignViewUtils.prototype.errorAlert(response.errorMessage);
                                }
                            }, 100);
                        } else if (designContainer.is(':visible')) {

                            /**
                             * This method removes unnecessary attributes from the json which is sent to backend.
                             * Removed attributes are used only for front end use only.
                             * */
                            function removeUnnecessaryFieldsFromJSON(object) {
                                if (object.hasOwnProperty('rawExtensions')) {
                                    delete object['rawExtensions'];
                                }
                                if (object.hasOwnProperty('application')) {
                                    delete object['application'];
                                }
                                if (object.hasOwnProperty('isStillDrawingGraph')) {
                                    delete object['isStillDrawingGraph'];
                                }
                                if (object.hasOwnProperty('isDesignViewContentChanged')) {
                                    delete object['isDesignViewContentChanged'];
                                }
                                _.forEach(object.siddhiAppConfig.queryLists.PATTERN, function (patternQuery) {
                                    if (patternQuery.queryInput !== undefined) {
                                        if (patternQuery.queryInput.hasOwnProperty('connectedElementNameList')) {
                                            delete patternQuery.queryInput['connectedElementNameList'];
                                        }
                                    }
                                });
                                _.forEach(object.siddhiAppConfig.queryLists.SEQUENCE, function (sequenceQuery) {
                                    if (sequenceQuery.queryInput !== undefined) {
                                        if (sequenceQuery.queryInput.hasOwnProperty('connectedElementNameList')) {
                                            delete sequenceQuery.queryInput['connectedElementNameList'];
                                        }
                                    }
                                });
                                _.forEach(object.siddhiAppConfig.queryLists.JOIN, function (joinQuery) {
                                    if (joinQuery.queryInput !== undefined) {
                                        if (joinQuery.queryInput.hasOwnProperty('firstConnectedElement')) {
                                            delete joinQuery.queryInput['firstConnectedElement'];
                                        }
                                        if (joinQuery.queryInput.hasOwnProperty('secondConnectedElement')) {
                                            delete joinQuery.queryInput['secondConnectedElement'];
                                        }
                                    }
                                });
                            }

                            var isDesignViewContentChanged
                                = designView.getConfigurationData().getIsDesignViewContentChanged();

                            if (!isDesignViewContentChanged) {
                                designContainer.hide();
                                sourceContainer.show();
                                self.trigger("view-switch", { view: 'source' });
                                toggleViewButton.html("<i class=\"fw fw-design-view fw-rotate-90\"></i>" +
                                    "<span class=\"toggle-button-text\">Design View</span>");
                                return;
                            }

                            var configurationCopy = _.cloneDeep(designView.getConfigurationData());

                            // validate json before sending to backend to get the code view
                            if (!designView.validateJSONBeforeSendingToBackend(configurationCopy.getSiddhiAppConfig())) {
                                return;
                            }

                            removeUnnecessaryFieldsFromJSON(configurationCopy);
                            var sendingString = JSON.stringify(configurationCopy);

                            var response = self._designView.getCode(sendingString);
                            if (response.status === "success") {
                                designContainer.hide();
                                loadingScreen.show();
                                // The following code has been added to the setTimeout() method because
                                // the code needs to run asynchronously for the loading screen
                                setTimeout(function () {
                                    self.setContent(response.responseString);
                                    self.trigger('content-modified');
                                    designView.emptyDesignViewGridContainer();
                                    sourceContainer.show();
                                    self._sourceView.editorResize();
                                    self._sourceView.format();
                                    loadingScreen.hide();
                                    // NOTE - This trigger should be always handled at the end of setTimeout()
                                    self.trigger("view-switch", { view: 'source' });
                                }, 100);
                                toggleViewButton.html("<i class=\"fw fw-design-view fw-rotate-90\"></i>" +
                                    "<span class=\"toggle-button-text\">Design View</span>");
                            } else if (response.status === "fail") {
                                DesignViewUtils.prototype.errorAlert(response.errorMessage);
                            }
                        }
                    });
                    toggleViewButton.keydown(function (key) {
                        if (key.keyCode == ENTER_KEY) {
                            toggleViewButton.click();
                        }
                    });
                    toggleViewButton.focus(function () {
                        toggleViewButton.addClass("selected-button");
                    });
                    toggleViewButton.focusout(function () {
                        toggleViewButton.removeClass("selected-button");
                    });
                },

                getContent: function () {
                    var self = this;
                    return self._sourceView.getContent();
                },

                setContent: function (content) {
                    var self = this;
                    return self._sourceView.setContent(content);
                },

                getSourceView: function () {
                    return this._sourceView;
                },

                getDesignView: function () {
                    return this._designView;
                },

                getDebuggerWrapper: function () {
                    return this._debugger;
                },

                getLineNumber: function (queryIndex, queryTerminal) {
                    var self = this;
                    var key = queryIndex + '_' + queryTerminal;
                    key = key.toLowerCase();
                    if (self._lineIndex.hasOwnProperty(key)) {
                        return self._lineIndex[key];
                    } else {
                        return null;
                    }
                },

                getUndoManager: function () {
                    var self = this;
                    return self._undoManager;
                },

                getLauncher: function () {
                    var self = this;
                    return self._launcher;
                },

                isInSourceView: function () {
                    return this._sourceView.isVisible();
                }

            });

        String.prototype.replaceAll = function (search, replacement) {
            var target = this;
            return target.replace(new RegExp(search, 'g'), replacement);
        };

        return ServicePreview;
    });
