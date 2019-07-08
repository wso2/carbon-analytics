/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['require', 'log', 'jquery', 'backbone', 'tool_palette/tool', 'tool_palette/tool-view'],
    function (require, log, $, Backbone, Tool, ToolView) {

        var ToolPalette = Backbone.View.extend({
            initialize: function (options) {
                var errMsg;
                if (!_.has(options, 'container')) {
                    errMsg = 'unable to find configuration for container';
                    log.error(errMsg);
                    throw errMsg;
                }
                var container = $(_.get(options, 'container'));
                // check whether container element exists in dom
                if (!container.length > 0) {
                    errMsg = 'unable to find container for tab list with selector: ' + _.get(options, 'container');
                    log.error(errMsg);
                    throw errMsg;
                }
                this._$parent_el = container;
                this._options = options;
                this._initTools();
            },

            _initTools: function () {

                var definitionTools = [
                    {
                        id: "stream",
                        className: "stream-drag",
                        title: "Stream",
                        icon: "/server/images/streams.svg",
                        toolGroupName: "Flow Constructs"
                    },
                    {
                        id: "source",
                        className: "source-drag",
                        title: "Source",
                        icon: "/server/images/source.svg",
                        toolGroupName: "I/O"
                    },
                    {
                        id: "sink",
                        className: "sink-drag",
                        title: "Sink",
                        icon: "/server/images/sink.svg",
                        toolGroupName: "I/O"
                    },
                    {
                        id: "table",
                        className: "table-drag",
                        title: "Table",
                        icon: "/server/images/table.svg",
                        toolGroupName: "Collections"
                    },
                    {
                        id: "window",
                        className: "window-drag",
                        title: "Window",
                        icon: "/server/images/window.svg",
                        toolGroupName: "Collections"
                    },
                    {
                        id: "trigger",
                        className: "trigger-drag",
                        title: "Trigger",
                        icon: "/server/images/trigger.svg",
                        toolGroupName: "Flow Constructs"
                    },
                    {
                        id: "aggregation",
                        className: "aggregation-drag",
                        title: "Aggregation",
                        icon: "/server/images/aggregate.svg",
                        toolGroupName: "Collections"
                    },
                    {
                        id: "projection-query",
                        className: "projection-query-drag",
                        title: "Projection",
                        icon: "/server/images/query.svg",
                        toolGroupName: "Queries"
                    },
                    {
                        id: "filter-query",
                        className: "filter-query-drag",
                        title: "Filter",
                        icon: "/server/images/filter-query.svg",
                        toolGroupName: "Queries"
                    },
                    {
                        id: "window-query",
                        className: "window-query-drag",
                        title: "Window",
                        icon: "/server/images/window-query.svg",
                        toolGroupName: "Queries"
                    },
                    {
                        id: "function-query",
                        className: "function-query-drag",
                        title: "Function",
                        icon: "/server/images/function-query.svg",
                        toolGroupName: "Queries"
                    },
                    {
                        id: "join-query",
                        className: "join-query-drag",
                        title: "Join",
                        icon: "/server/images/join-query.svg",
                        toolGroupName: "Queries"
                    },
                    {
                        id: "pattern-query",
                        className: "pattern-query-drag",
                        title: "Pattern",
                        icon: "/server/images/pattern-query.svg",
                        toolGroupName: "Queries"
                    },
                    {
                        id : "sequence-query",
                        className : "sequence-query-drag",
                        title : "Sequence",
                        icon : "/server/images/sequence-query.svg",
                        toolGroupName: "Queries"
                    },
                    {
                        id: "partition",
                        className: "partition-drag",
                        title: "Partition",
                        icon: "/server/images/partition.svg",
                        toolGroupName: "Flow Constructs"
                    },
                    {
                        id: "function",
                        className: "function-drag",
                        title: "Function",
                        icon: "/server/images/function.svg",
                        toolGroupName: "Functions"
                    }
                ];

                this.toolGroups = {};
                var self = this;

                _.forEach(definitionTools, function(obj){
                    if(!self.toolGroups[obj.toolGroupName]){
                        self.toolGroups[obj.toolGroupName] = [];
                    }
                    var tool = new Tool(obj);
                    self.toolGroups[obj.toolGroupName].push(tool);
                });
            },

            render: function () {
                var self = this;
                self._$parent_el.addClass('non-user-selectable');

                var toolGroupElements = [];

                Object.keys(self.toolGroups).forEach(function(key) {
                    var groupDiv = $('<div id="tool-group-'+ key +'" class="tool-group"></div>');
                    var groupDivBody = $('<div class="tool-group-body"></div>');
                    var groupDivHeader = $('<div class="tool-group-header">' +
                        '<a class="tool-group-header-title">'+key+'</a>' +
                        '<span class="collapse-icon fw fw-up"></span>' +
                        '</div>');

                    groupDivHeader.click(function(e) {
                        var clickedEl = e.target;

                        if(clickedEl.classList.contains('collapse-icon') ||
                            clickedEl.classList.contains('tool-group-header-title')) {
                            clickedEl = e.target.parentElement;
                        }

                        if(clickedEl.classList.contains('tool-group-header')) {
                            $(clickedEl.parentElement.lastElementChild).slideToggle(500, function() {
                                $(clickedEl.lastElementChild).toggleClass("fw-down fw-up");
                            });
                        }
                    });

                    groupDiv.append(groupDivHeader);

                    _.forEach(self.toolGroups[key], function(tool) {
                        var toolView = new ToolView({model: tool, toolPalette: self.toolPalette});
                        toolView.render(groupDivBody);
                    });

                    groupDiv.append(groupDivBody);
                    toolGroupElements.push(groupDiv);
                });
                self._$parent_el.append(toolGroupElements);
            },

            hideToolPalette: function () {
                this._$parent_el.hide();
            },

            showToolPalette: function () {
                this._$parent_el.show();
            }
        });

        return ToolPalette;
    });
