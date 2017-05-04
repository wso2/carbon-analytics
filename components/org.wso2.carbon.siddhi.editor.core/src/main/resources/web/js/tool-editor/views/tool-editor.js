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

define(['require', 'jquery', 'backbone', 'lodash', 'log', './design', "./source", '../constants'],

    function (require, $, Backbone, _, log, DesignView, SourceView, constants) {

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

                    var debugDynamicId = 'debug' + this._$parent_el.attr('id');
                    var debugTemplate = '<div class="container">' +
                        '    <div class="row">' +
                        '        <div class="col-sm-2" style="min-height: 100px; background-color: #333;"">' +
                        '        	<button type="button" id="start-{{id}}">Start</button>' +
                        '        	<button type="button" id="send-{{id}}">Send</button>' +
                        '        	<button type="button" id="next-{{id}}">Next</button>' +
                        '        	<button type="button" id="play-{{id}}">Play</button>' +
                        '        </div>' +
                        '        <div class="col-sm-5" id="event-state-{{id}}" style="min-height: 100px; background-color: #333;"></div>' +
                        '        <div class="col-sm-5" id="query-state-{{id}}" style="min-height: 100px; background-color: #333;"></div>' +
                        '    </div>' +
                        '</div>';

                    var debugHtml = debugTemplate.replaceAll('{{id}}', debugDynamicId);
                    debugContainer.attr("id", debugDynamicId);
                    debugContainer.html(debugHtml);

                    this._debugger.setOnUpdateCallback(function (data) {
                        $('#event-state-' + debugDynamicId).text(JSON.stringify(data['eventState']));
                        $('#query-state-' + debugDynamicId).text(JSON.stringify(data['queryState']));
                    });

                    $('#start-' + debugDynamicId).on('click', function () {
                        self._debugger.start();
                        setTimeout(function () {
                            self._debugger.acquire("query1", "in");
                        }, 500)
                    });

                    $('#send-' + debugDynamicId).on('click', function () {
                        self._debugger.sendEvent("query1", "foo", [136.8, "Sample Data"])
                    });

                    $('#next-' + debugDynamicId).on('click', function () {
                        self._debugger.next();
                    });

                    $('#play-' + debugDynamicId).on('click', function () {
                        self._debugger.play();
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
                        if (typeof breakpoints[row] === typeof undefined) {
                            self._breakpoints[row] = true;
                            e.editor.session.setBreakpoint(row);
                        } else {
                            delete self._breakpoints[row];
                            e.editor.session.clearBreakpoint(row);
                        }
                        console.log(JSON.stringify(self._breakpoints));
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

                        console.log(JSON.stringify(self._breakpoints));
                        console.log(JSON.stringify(e));
                    });
                    /* End Debug Related Stuff */

                    canvasContainer.removeClass('show-div').addClass('hide-div');
                    previewContainer.removeClass('show-div').addClass('hide-div');
                    sourceContainer.removeClass('source-view-disabled').addClass('source-view-enabled');
                    toolPallette.addClass('hide-div');
                    tabContentContainer.removeClass('tab-content-default');

                    this._sourceView.on('modified', function (changeEvent) {
                        //todo do undo stuff here
                        _.set(changeEvent, 'editor', self);
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
                }


            });

        String.prototype.replaceAll = function (search, replacement) {
            var target = this;
            return target.replace(new RegExp(search, 'g'), replacement);
        };

        return ServicePreview;
    });
