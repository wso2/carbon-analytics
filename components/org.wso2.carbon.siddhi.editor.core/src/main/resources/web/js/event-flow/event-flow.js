/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['require', 'log', 'lodash', 'jquery', 'alerts', 'd3', 'dagre_d3', 'overlay_scroller'],
    function (require, log, _, $, alerts, d3, dagreD3, overlayScroller) {

        /**
         * Create an instance of the EventFlow class in JavaScript.
         * This class is what is used to connect to the back-end and generate a graph in the front end design view.
         *
         * @param designView
         * @constructor Sets the default variables
         */
        var EventFlow = function (designView) {
            this.$designView = designView;
            this.$designViewData = this.$designView.find('.design-container-data');
            this.$siddhiAppName = this.$designView.find('.siddhi-app-name');
            this.$siddhiAppDescription = this.$designView.find('.siddhi-app-description');
            this.$graphView = this.$designView.find('.graph-container');
            this.$loadingScreen = this.$designView.find('.loading-screen');
            this.$siddhiGraph = this.$designView.find('.siddhi-graph');

            this.url = window.location.protocol + "//" + window.location.host + "/editor/event-flow";
            // The render options contains any settings/options for rendering the graph,
            // this has been added here separately for easier access if the style of the
            // graph generated needs to be changed.
            var defaultNodeStyle = {
                labelType: "html",
                rx: 0,
                ry: 0,
                padding: 0,
            };
            this.renderOptions = {
                nodeOptions: {
                    stream: {
                        name: "stream",
                        nodeStyle: defaultNodeStyle,
                        cssClass: "indicator stream-colour"
                    },
                    source: {
                      name: "source",
                      nodeStyle: defaultNodeStyle,
                      cssClass: "indicator source-colour"
                    },
                    sink: {
                        name: "sink",
                        nodeStyle: defaultNodeStyle,
                        cssClass: "indicator sink-colour"
                    },
                    table: {
                        name: "table",
                        nodeStyle: defaultNodeStyle,
                        cssClass: "indicator table-colour"
                    },
                    window: {
                        name: "window",
                        nodeStyle: defaultNodeStyle,
                        cssClass: "indicator window-colour"
                    },
                    trigger: {
                        name: "trigger",
                        nodeStyle: defaultNodeStyle,
                        cssClass: "indicator trigger-colour"
                    },
                    aggregation: {
                        name: "aggregation",
                        nodeStyle: defaultNodeStyle,
                        cssClass: "indicator aggregation-colour"
                    },
                    function: {
                        name: "function",
                        nodeStyle: defaultNodeStyle,
                        cssClass: "indicator function-colour"
                    },
                    query: {
                        name: "query",
                        nodeStyle: defaultNodeStyle,
                        cssClass: "indicator query-colour"
                    },
                    partition: {
                        name: "partition",
                        nodeStyle: {
                            labelType: "html",
                            clusterLabelPos: "top",
                            style: "fill: #434343"
                        },
                        cssClass: "partition"
                    },
                    partition_type: {
                        name: "partition-type",
                        nodeStyle: defaultNodeStyle,
                        cssClass: "indicator partition-type-colour"
                    }
                },
                edgeOptions: {
                    default: {
                        name: "default",
                        edgeStyle: {
                            arrowheadStyle: "fill: #bbb",
                            lineInterpolate: "basis"
                        }
                    },
                    dotted_line: {
                        name: "dotted-line",
                        edgeStyle: {
                            arrowheadStyle: "fill: #bbb",
                            style: "stroke-dasharray: 5, 5;"
                        }
                    }
                }
            };

            var self = this;
            $(window).on('resize', function () {
                if (self.isVisible()) {
                    self.graphResize();
                }
            });

            this.$graphView.overlayScrollbars({className: "os-theme-light"});
        };

        /**
         * Does an AJAX call to the backend MSF4J service that converts the 'siddhiCode'
         * to a specific JSON format to be displayed in the UI.
         *
         * @param siddhiCode The Siddhi App as a string
         * @returns A JSON with a response status, and the JSON value returned by the back-end
         */
        EventFlow.prototype.fetchJSON = function (siddhiCode) {
            var self = this;
            var result = {};

            if (siddhiCode === null || siddhiCode === undefined || siddhiCode === "") {
                result = {status: "fail", errorMessage: "The Siddhi App Cannot Be Empty"};
            } else {
                // Remove Single Line Comments
                var regexStr = siddhiCode.replace(/--.*/g, '');
                // Remove Multi-line Comments
                var regexStr = regexStr.replace(/\/\*(.|\s)*?\*\//g, '');
                var regex = /^\s*@\s*app\s*:\s*name\s*\(\s*["|'](.*)["|']\s*\)\s*@\s*app\s*:\s*description\s*\(\s*["|'](.*)["|']\s*\)\s*$/gi;
                var match = regex.exec(regexStr);

                if (match !== null) {
                    result = {
                        status: "success",
                        responseJSON: {
                            appName: match[1],
                            appDescription: match[2],
                            nodes: [],
                            edges: [],
                            groups: []
                        }
                    };
                } else {
                    fetch(siddhiCode);
                }

            }

            function fetch(code) {
                $.ajax({
                    type: "POST",
                    url: self.url,
                    data: window.btoa(code),
                    async: false,
                    success: function (response) {
                        result = {status: "success", responseJSON: response};
                    },
                    error: function (error) {
                        if (error.status === 400) {
                            result = {status: "fail", errorMessage: "Siddhi App Contains Errors"};
                        } else {
                            result = {status: "fail", errorMessage: "Internal Server Error Occurred"};
                        }
                    }
                });
            }

            return result;
        };

        /**
         * Renders a graph in the design view based on the information of the structure
         * of the graph in the 'data' argument.
         *
         * @param data A JSON object that defines the graph that is to be rendered in the UI
         */
        EventFlow.prototype.render = function (data) {
            var self = this;

            if (data === null || data === undefined || data === {}) {
                log.error("The argument sent to the render method is null/undefined");
            } else {
                createGraph();
                self.$loadingScreen.hide();
            }

            function createGraph() {
                if (data.appName === null || data.appName === undefined || data.appName === "") {
                    self.$siddhiAppName.html("SiddhiApp");
                } else {
                    self.$siddhiAppName.html(data.appName);
                }

                if (data.appDescription === null || data.appDescription === undefined || data.appDescription === "") {
                    self.$siddhiAppDescription.html("Description of the plan");
                } else {
                    self.$siddhiAppDescription.html(data.appDescription);
                }

                var nodeOptions = self.renderOptions.nodeOptions;
                var edgeOptions = self.renderOptions.edgeOptions;

                // Create an instance of the dagreD3 graph.
                var graph = new dagreD3.graphlib.Graph({compound: true}).setGraph({});
                // This makes sure the graph grows from left-to-right.
                graph.graph().rankDir = "LR";

                // Set the nodes of the graph
                data.nodes.forEach(function (node) {
                    var html;
                    var nodeStyle;

                    switch (node.type) {
                        case nodeOptions.stream.name:
                            html = "<div class='node-content' title='" + node.description + "'>"
                                + "<span class='" + nodeOptions.stream.cssClass + "'></span>"
                                + "<span class='nodeLabel'>" + node.name + "</span>"
                                + "</div>";
                            // The _.clone() method must be used to avoid passing by reference.
                            nodeStyle = _.clone(nodeOptions.stream.nodeStyle);
                            nodeStyle.label = html;
                            break;
                        case nodeOptions.source.name:
                            html = "<div class='node-content' title='" + node.description + "'>"
                                + "<span class='" + nodeOptions.source.cssClass + "'></span>"
                                + "<span class='nodeLabel'>" + node.name + "</span>"
                                + "</div>";
                            nodeStyle = _.clone(nodeOptions.source.nodeStyle);
                            nodeStyle.label = html;
                            break;
                        case nodeOptions.sink.name:
                            html = "<div class='node-content' title='" + node.description + "'>"
                                + "<span class='" + nodeOptions.sink.cssClass + "'></span>"
                                + "<span class='nodeLabel'>" + node.name + "</span>"
                                + "</div>";
                            nodeStyle = _.clone(nodeOptions.sink.nodeStyle);
                            nodeStyle.label = html;
                            break;
                        case nodeOptions.table.name:
                            html = "<div class='node-content' title='" + node.description + "'>"
                                + "<span class='" + nodeOptions.table.cssClass + "'></span>"
                                + "<span class='nodeLabel'>" + node.name + "</span>"
                                + "</div>";
                            nodeStyle = _.clone(nodeOptions.table.nodeStyle);
                            nodeStyle.label = html;
                            break;
                        case nodeOptions.window.name:
                            html = "<div class='node-content' title='" + node.description + "'>"
                                + "<span class='" + nodeOptions.window.cssClass + "'></span>"
                                + "<span class='nodeLabel'>" + node.name + "</span>"
                                + "</div>";
                            nodeStyle = _.clone(nodeOptions.window.nodeStyle);
                            nodeStyle.label = html;
                            break;
                        case nodeOptions.trigger.name:
                            html = "<div class='node-content' title='" + node.description + "'>"
                                + "<span class='" + nodeOptions.trigger.cssClass + "'></span>"
                                + "<span class='nodeLabel'>" + node.name + "</span>"
                                + "</div>";
                            nodeStyle = _.clone(nodeOptions.trigger.nodeStyle);
                            nodeStyle.label = html;
                            break;
                        case nodeOptions.aggregation.name:
                            html = "<div class='node-content' title='" + node.description + "'>"
                                + "<span class='" + nodeOptions.aggregation.cssClass + "'></span>"
                                + "<span class='nodeLabel'>" + node.name + "</span>"
                                + "</div>";
                            nodeStyle = _.clone(nodeOptions.aggregation.nodeStyle);
                            nodeStyle.label = html;
                            break;
                        case nodeOptions.function.name:
                            html = "<div class='node-content' title='" + node.description + "'>"
                                + "<span class='" + nodeOptions.function.cssClass + "'></span>"
                                + "<span class='nodeLabel'>" + node.name + "</span>"
                                + "</div>";
                            nodeStyle = _.clone(nodeOptions.function.nodeStyle);
                            nodeStyle.label = html;
                            break;
                        case nodeOptions.query.name:
                            html = "<div class='node-content' title='" + node.description + "'>"
                                + "<span class='" + nodeOptions.query.cssClass + "'></span>"
                                + "<span class='nodeLabel'>" + node.name + "</span>"
                                + "</div>";
                            nodeStyle = _.clone(nodeOptions.query.nodeStyle);
                            nodeStyle.label = html;
                            break;
                        case nodeOptions.partition.name:
                            html = "<div class='" + nodeOptions.partition.cssClass
                                + "' title='" + node.description + "'>"
                                + node.name
                                + "</div>";
                            nodeStyle = _.clone(nodeOptions.partition.nodeStyle);
                            nodeStyle.label = html;
                            break;
                        case nodeOptions.partition_type.name:
                            html = "<div class='node-content' title='" + node.description + "'>"
                                + "<span class='" + nodeOptions.partition_type.cssClass + "'></span>"
                                + "<span class='nodeLabel'>" + node.name + "</span>" + "</div>";
                            nodeStyle = _.clone(nodeOptions.partition_type.nodeStyle);
                            nodeStyle.label = html;
                            break;
                    }

                    // Set the node
                    graph.setNode(node.id, nodeStyle);

                });

                // Set the edges of the graph
                data.edges.forEach(function (edge) {
                    var edgeStyle;
                    if (edge.type === "dotted-line") {
                        // This makes the lines dotted
                        edgeStyle = _.clone(edgeOptions.dotted_line.edgeStyle);
                    } else {
                        edgeStyle = _.clone(edgeOptions.default.edgeStyle);
                    }

                    // Set the edge
                    graph.setEdge(edge.parent, edge.child, edgeStyle);
                });

                // Set the groups of the graph
                // NOTE - The nodes and edges should be created first before the groups can be created
                data.groups.forEach(function (group) {
                    group.children.forEach(function (child) {
                        graph.setParent(child, group.id);
                    });
                });

                var render = new dagreD3.render();
                var graphId = "#" + self.$siddhiGraph.attr("id");
                render(d3.select(graphId + " g"), graph);

                resizeGraph(self.$siddhiGraph);

                function resizeGraph(svg) {
                    var inner = svg.find('g');
                    svg.attr("width", inner.get(0).getBoundingClientRect().width + 60);
                    svg.attr("height", inner.get(0).getBoundingClientRect().height + 60);
                }
            }
        };

        EventFlow.prototype.graphResize = function () {
            var self = this;

            resizeGraph();
            centerSVG();

            function resizeGraph() {
                var height = self.$designView.height() - self.$designViewData.height() - 20;
                self.$graphView.height(height);
            }

            function centerSVG() {
                var graphWidth = parseInt(self.$siddhiGraph.attr("width"));
                var graphHeight = parseInt(self.$siddhiGraph.attr("height"));
                var width = self.$graphView.width();
                var height = self.$graphView.height();

                var left = diff(width, graphWidth) / 2;
                var top = diff(height, graphHeight) / 2;
                self.$siddhiGraph.attr("transform", "translate(" + left + "," + top + ")");

                function diff(divValue, graphValue) {
                    if (divValue > graphValue) {
                        return (divValue - graphValue);
                    } else {
                        return 0;
                    }
                }
            }
        };

        EventFlow.prototype.isVisible = function () {
            return this.$designView.is(':visible');
        };

        /**
         * Clears the design view of all it's contents when called.
         */
        EventFlow.prototype.clearContent = function () {
            this.$siddhiAppName.empty();
            this.$siddhiAppDescription.empty();
            this.$loadingScreen.show();
            this.$siddhiGraph.empty();
            this.$siddhiGraph.html('<g></g>');
        };

        /**
         * Display's a warning using the AlertsManager.
         *
         * @param message The content to be displayed in the alert
         */
        EventFlow.prototype.alert = function (message) {
            alerts.warn(message);
        };

        return EventFlow;

    });
