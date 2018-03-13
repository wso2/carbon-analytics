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

define(['require', 'jquery', 'backbone', 'lodash', 'log', 'design_view', "./source", '../constants',
        'undo_manager','launcher','app/debugger/debugger'],

    function (require, $, Backbone, _, log, DesignView, SourceView, constants,UndoManager,Launcher,
        DebugManager) {

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
                    var sourceContainer = this._$parent_el.find(_.get(this.options, 'source.container'));
                    var debugContainer = this._$parent_el.find(_.get(this.options, 'debug.container'));
                    var tabContentContainer = $(_.get(this.options, 'tabs_container'));

                    if (!canvasContainer.length > 0) {
                        var errMsg = 'cannot find container to render svg';
                        log.error(errMsg);
                        throw errMsg;
                    }

                    //use this line to assign dynamic id for canvas and pass the canvas id to initialize jsplumb
                    canvasContainer.attr('id', 'canvasId1');

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
                        debugger_instance : self._sourceView.getDebugger(),
                        editorInstance : self._sourceView.getEditor(),
                        option : self.options.application.config.debugger_instance

                    };

                    this._debugger = new DebugManager(debugConfOpts);

                    canvasContainer.removeClass('show-div').addClass('hide-div');
                    previewContainer.removeClass('show-div').addClass('hide-div');
                    sourceContainer.removeClass('source-view-disabled').addClass('source-view-enabled');
                    tabContentContainer.removeClass('tab-content-default');

                    this._sourceView.on('modified', function (changeEvent) {
                        if(self.getUndoManager().hasUndo()){
                            // clear undo stack from design view
                            if(!self.getUndoManager().getOperationFactory()
                                    .isSourceModifiedOperation(self.getUndoManager().undoStackTop())){
                                self.getUndoManager().reset();
                            }
                        }

                        if(self.getUndoManager().hasRedo()){
                            // clear redo stack from design view
                            if(!self.getUndoManager().getOperationFactory()
                                    .isSourceModifiedOperation(self.getUndoManager().redoStackTop())){
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

                    var sourceViewBtn = this._$parent_el.find(_.get(this.options, 'toggle_controls.sourceIcon'));
                    var designViewBtn = this._$parent_el.find(_.get(this.options, 'toggle_controls.designIcon'));
                    var applicationData = this.options.application;
                    this.designView = new DesignView(this.options, applicationData);
                    this.designView.render();
                    this.sourceContainer = sourceContainer;
                    this.designViewContainer = this.designView.getDesignViewContainer();

                    sourceViewBtn.click(function () {
                        if (self.getDesignContainer() !== undefined) {
                            if (self.getDesignContainer().is(':visible')) {
                                self.getDesignContainer().hide();
                                self.getSourceContainer().show();
                            } else {
                                self.getSourceContainer().show();
                            }
                        }
                    });

                    designViewBtn.click(function () {
                        if (self.getDesignContainer() !== undefined) {
                            self.getDesignView().showToolPalette();
                            if (self.getSourceContainer().is(':visible')) {
                                self.getSourceContainer().hide();
                                self.getDesignContainer().show();
                            } else {
                                self.getDesignContainer().show();
                            }
                        }
                    });
                },

                getSourceContainer: function () {
                    var self = this;
                    return self.sourceContainer;
                },

                getDesignContainer: function () {
                    var self = this;
                    return self.designView.getDesignViewContainer();
                },
                getDesignView: function () {
                    return this.designView;
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

                isInSourceView: function(){
                    return true;
                }

            });

        String.prototype.replaceAll = function (search, replacement) {
            var target = this;
            return target.replace(new RegExp(search, 'g'), replacement);
        };

        return ServicePreview;
    });
