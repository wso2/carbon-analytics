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

        _initTools: function(){

            var definitionTools = [
                {
                    id : "stream",
                    className : "stream",
                    title : "Stream",
                    icon : "/editor/images/stream.png"
                },
                {
                    id : "table",
                    className : "table",
                    title : "Table",
                    icon : "/editor/images/table.png"
                },
                {
                    id : "window",
                    className : "window",
                    title : "Window",
                    icon : "/editor/images/window.png"
                },
                {
                    id : "trigger",
                    className : "trigger",
                    title : "Trigger",
                    icon : "/editor/images/trigger.png"
                },
                {
                    id : "aggregation",
                    className : "aggregation",
                    title : "Aggregation",
                    icon : "/editor/images/aggregation.png"
                },
                {
                    id : "projection-query",
                    className : "projection-query",
                    title : "Projection Query",
                    icon : "/editor/images/projectionQuery.png"
                },
                {
                    id : "filter-query",
                    className : "filter-query",
                    title : "Filter Query",
                    icon : "/editor/images/filterQuery.png"
                },
                {
                    id : "window-query",
                    className : "window-query",
                    title : "Window Query",
                    icon : "/editor/images/windowQuery.png"
                },
                {
                    id : "join-query",
                    className : "join-query",
                    title : "Join Query",
                    icon : "/editor/images/join.png"
                },
                {
                    id : "pattern-query",
                    className : "pattern-query",
                    title : "Pattern Query",
                    icon : "/editor/images/patternQuery.png"
                },
                {
                    id : "sequence-query",
                    className : "sequence-query",
                    title : "Sequence Query",
                    icon : "/editor/images/sequenceQuery.png"
                },
                {
                    id : "partition",
                    className : "partition",
                    title : "Partition",
                    icon : "/editor/images/patternQuery.png"
                }
            ];

            this.tools = [];
            var self = this;
            _.forEach(definitionTools,  function(toolDefinition){
                    var tool = new Tool(toolDefinition);
                    self.tools.push(tool);
                }
            );
        },

        render: function () {
            var self = this;
            var toolPaletteDiv = $('<div></div>');
            this._$parent_el.append(toolPaletteDiv);
            this.$el = toolPaletteDiv;

            var parent = self.$el;
            self.$el.addClass('non-user-selectable');

            var groupDiv = $('<div></div>');
            parent.append(groupDiv);
            groupDiv.attr('id', "tool-group-elements");
            groupDiv.attr('class', "tool-group");

            this.tools.forEach(function (tool) {
                var toolView = new ToolView({model: tool, toolPalette: self.toolPalette});
                toolView.render(groupDiv);
            });
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
