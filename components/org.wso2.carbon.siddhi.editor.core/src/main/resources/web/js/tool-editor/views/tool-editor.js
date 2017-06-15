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

define(['require', 'jquery', 'backbone', 'lodash', 'log', 'ace/range', './design', "./source", '../constants',
        'undo_manager','launcher'],

    function (require, $, Backbone, _, log, AceRange, DesignView, SourceView, constants,UndoManager,Launcher) {

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
                    var toolPallete = $(_.get(options, 'toolPalette'));
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
                    var toolPallette = _.get(this.options, 'toolPalette._$parent_el');

                    if (!canvasContainer.length > 0) {
                        var errMsg = 'cannot find container to render svg';
                        log.error(errMsg);
                        throw errMsg;
                    }
                    var designViewOpts = {};
                    _.set(designViewOpts, 'container', canvasContainer.get(0));

                    //use this line to assign dynamic id for canvas and pass the canvas id to initialize jsplumb
                    canvasContainer.attr('id', 'canvasId1');

                    var sourceDynamicId = sourceContainer.attr('id') + this._$parent_el.attr('id');
                    sourceContainer.attr("id", sourceDynamicId);

                    var sourceViewOptions = {
                        sourceContainer: sourceDynamicId,
                        source: constants.INITIAL_SOURCE_INSTRUCTIONS
                    };

                    this._sourceView = new SourceView(sourceViewOptions);

                    /* Start Debug Related Stuff */
                    self._debugger = this._sourceView.getDebugger();
                    self._editor = this._sourceView.getEditor();
                    self._breakpoints = [];
                    self._validBreakpoints = {};
                    self._currentDebugLine = null;
                    self._lineIndex = {};
                    self._debugStarted = false;

                    // todo set Execution Plan name here or at sourceView

                    var debugDynamicId = 'debug' + this._$parent_el.attr('id');
                    var debugTemplate =
                        '<div class="container">' +
                        '   <div class="row">' +
                        '       <div class="col-sm-2" style="min-height: 100px; background-color: #333;"">' +
                        '           <button type="button" id="start-{{id}}">Start</button>' +
                        '           <button type="button" id="debug-{{id}}">Debug</button>' +
                        '        	<button type="button" id="stop-{{id}}">Stop</button>' +
                        '        	<button type="button" id="send-{{id}}">Send</button>' +
                        '        	<button type="button" id="next-{{id}}">Next</button>' +
                        '        	<button type="button" id="play-{{id}}">Play</button>' +
                        '       </div>' +
                        '       <div class="col-sm-5 debug-state-container" >' +
                        '           <div class="panel panel-default panel-state">' +
                        '               <div class="panel-heading panel-state-heading">Event State</div>' +
                        '               <div class="panel-body panel-state-body" id="event-state-{{id}}"></div>' +
                        '           </div>' +
                        '       </div>' +
                        '       <div class="col-sm-5 debug-state-container" >' +
                        '           <div class="panel panel-default panel-state">' +
                        '               <div class="panel-heading panel-state-heading">Query State</div>' +
                        '               <div class="panel-body panel-state-body" id="query-state-{{id}}"></div>' +
                        '           </div>' +
                        '       </div>' +
                        '   </div>' +
                        '</div>';
                    var debugHtml = debugTemplate.replaceAll('{{id}}', debugDynamicId);
                    debugContainer.attr("id", debugDynamicId);
                    debugContainer.html(debugHtml);

                    this._debugger.setOnUpdateCallback(function (data) {
                        var line = self.getLineNumber(data['eventState']['queryIndex'], data['eventState']['queryTerminal']);
                        self.highlightDebugLine(line);
                        renderjson.set_show_to_level(1);
                        $('#event-state-' + debugDynamicId).html(renderjson(data['eventState']));
                        $('#query-state-' + debugDynamicId).html(renderjson(data['queryState']));
                    });

                    this._debugger.setOnBeforeUpdateCallback(function () {
                        self.unHighlightDebugLine();
                        $('#event-state-' + debugDynamicId).html("");
                        $('#query-state-' + debugDynamicId).html("");
                    });

                    this._debugger.setOnChangeLineNumbersCallback(function (validBreakPoints) {
                        self._validBreakpoints = validBreakPoints;

                        // update line indexes
                        self._lineIndex = {};
                        for (var i in self._validBreakpoints) {
                            if (self._validBreakpoints.hasOwnProperty(i)) {
                                // i is the line number
                                var breakpoints = self._validBreakpoints[i];
                                for (var j = 0; j < breakpoints.length; j++) {
                                    var key = breakpoints[j]['queryIndex'] + '_' + breakpoints[j]['terminal'];
                                    key = key.toLowerCase();
                                    self._lineIndex[key] = i;
                                }

                            }
                        }
                    });

                    $('#start-' + debugDynamicId).on('click', function () {
                        // var title = self.options.tabController.getActiveTab().getTitle();

                        self._debugger.start(
                            function (runtimeId, streams, queries) {
                                // debug successfully started
                                // self._debugStarted = true;
                                // for (var i = 0; i < self._breakpoints.length; i++) {
                                //     if (self._breakpoints[i] && i in self._validBreakpoints) {
                                //         self._debugger.acquire(i);
                                //         console.info("Acquire Breakpoint " + JSON.stringify(self._validBreakpoints[i]));
                                //     }
                                // }
                            }, function (e) {
                                // debug not started (possible error)
                                console.error("Could not start execution plan runtime.")
                            }
                        );
                    });

                    $('#debug-' + debugDynamicId).on('click', function () {
                        self._debugger.debug(
                            function (runtimeId, streams, queries) {
                                // debug successfully started
                                self._debugStarted = true;
                                for (var i = 0; i < self._breakpoints.length; i++) {
                                    if (self._breakpoints[i] && i in self._validBreakpoints) {
                                        self._debugger.acquire(i);
                                        console.info("Acquire Breakpoint " + JSON.stringify(self._validBreakpoints[i]));
                                    }
                                }
                            }, function (e) {
                                // debug not started (possible error)
                                console.error("Could not start execution plan in debug mode.")
                            }
                        );
                    });

                    $('#send-' + debugDynamicId).on('click', function () {
                        self._debugger.sendEvent("foo", [136.8, "Sample Data"])
                    });

                    $('#next-' + debugDynamicId).on('click', function () {
                        self._debugger.next();
                    });

                    $('#play-' + debugDynamicId).on('click', function () {
                        self._debugger.play();
                    });

                    $('#stop-' + debugDynamicId).on('click', function () {
                        self.unHighlightDebugLine();
                        self._debugger.stop();
                        self._debugStarted = false;
                    });

                    this._editor.on("guttermousedown", function (e) {
                        var target = e.domEvent.target;
                        if (target.className.indexOf("ace_gutter-cell") == -1)
                            return;
                        if (!self._editor.isFocused())
                            return;
                        if (e.clientX > 25 + target.getBoundingClientRect().left)
                            return;

                        var breakpoints = e.editor.session.getBreakpoints(row, 0);
                        var row = e.getDocumentPosition().row;

                        if (row in self._validBreakpoints) {
                            if (typeof breakpoints[row] === typeof undefined) {
                                if (self._debugStarted) {
                                    self._debugger.acquire(row, function (d) {
                                        self._breakpoints[row] = true;
                                        e.editor.session.setBreakpoint(row);
                                    });
                                } else {
                                    self._breakpoints[row] = true;
                                    e.editor.session.setBreakpoint(row);
                                }
                                console.info("Acquire Breakpoint " +
                                    JSON.stringify(self._validBreakpoints[row]));
                            } else {
                                if (self._debugStarted) {
                                    self._debugger.release(row, function (d) {
                                        delete self._breakpoints[row];
                                        e.editor.session.clearBreakpoint(row);
                                    });
                                } else {
                                    delete self._breakpoints[row];
                                    e.editor.session.clearBreakpoint(row);
                                }
                                console.info("Release Breakpoint " +
                                    JSON.stringify(self._validBreakpoints[row]));
                            }
                        } else {
                            console.warn("Trying to acquire an invalid breakpoint");
                        }
                        e.stop();
                    });

                    this._editor.on("change", function (e) {
                        var len, firstRow;

                        if (e.end.row == e.start.row) {
                            // editing in same line
                            return;
                        } else {
                            // new line or remove line
                            if (e.action == "insert") {
                                len = e.end.row - e.start.row;
                                firstRow = e.start.column == 0 ? e.start.row : e.start.row + 1;
                            } else if (e.action == "remove") {
                                len = e.start.row - e.end.row;
                                firstRow = e.start.row;
                            }

                            if (len > 0) {
                                var args = new Array(len);
                                args.unshift(firstRow, 0);
                                self._breakpoints.splice.apply(self._breakpoints, args);
                            } else if (len < 0) {
                                var rem = self._breakpoints.splice(firstRow + 1, -len);
                                if (!self._breakpoints[firstRow]) {
                                    for (var oldBP in rem) {
                                        if (rem[oldBP]) {
                                            self._breakpoints[firstRow] = rem[oldBP];
                                            break
                                        }
                                    }
                                }
                            }

                            // Redraw the breakpoints
                            for (var r in self._breakpoints) {
                                if (self._breakpoints[r]) {
                                    self._editor.session.setBreakpoint(r);
                                } else {
                                    self._editor.session.clearBreakpoint(r);
                                }
                            }
                        }
                    });
                    /* End Debug Related Stuff */

                    canvasContainer.removeClass('show-div').addClass('hide-div');
                    previewContainer.removeClass('show-div').addClass('hide-div');
                    sourceContainer.removeClass('source-view-disabled').addClass('source-view-enabled');
                    toolPallette.addClass('hide-div');
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
                },

                getContent: function () {
                    var self = this;
                    return self._sourceView.getContent();
                },

                getSourceView: function () {
                    return this._sourceView;
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

                highlightDebugLine: function (lineNo) {
                    var self = this;
                    self.unHighlightDebugLine();
                    self._currentDebugLine = self._editor.session.addMarker(
                        new AceRange.Range(lineNo, 0, lineNo, 256),
                        "debug_line_highlight",
                        "fullLine",
                        true
                    );
                },

                unHighlightDebugLine: function () {
                    var self = this;
                    if (self._currentDebugLine !== null)
                        self._editor.session.removeMarker(self._currentDebugLine);
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
