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

define(['require', 'log', 'lodash', 'jquery', 'alerts', 'd3', 'dagre_d3'],
    function (require, log, _, $, alerts, d3, dagreD3) {

        /**
         * Create an instance of the EventFlow class in JavaScript.
         * This class is what is used to connect to the back-end and generate a graph in the front end design view.
         *
         * @param designView
         * @constructor Sets the default variables
         */
        var EventFlow = function (designView) {
            this.$designView = designView;
            this.$siddhiAppName = this.$designView.find('.siddhi-app-name');
            this.$siddhiAppDescription = this.$designView.find('.siddhi-app-description');
            this.$graphView = this.$designView.find('.graph-container');
            this.$siddhiGraph = this.$designView.find('.siddhi-graph');
            this.url = window.location.protocol + "//" + window.location.host + "/editor/event-flow";
            this.defaultCode = "@App:name(\"SiddhiApp\")\n" +
                "@App:description(\"Description of the plan\")\n" +
                "\n" +
                "-- Please refer to https://docs.wso2.com/display/SP400/Quick+Start+Guide" +
                " on getting started with SP editor. \n" +
                "\n";

            var defaultNodeStyle = {
                labelType: "html",
                rx: 7,
                ry: 7,
                padding: 0
            };
            this.renderOptions = {
                node: {
                    stream: {
                        name: "stream",
                        nodeStyle: defaultNodeStyle,
                        cssClass: "indicator stream-colour"
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
                            style: "fill: #e0e0d1"
                        },
                        cssClass: "partition"
                    },
                    partition_type: {
                        name: "partition-type",
                        nodeStyle: defaultNodeStyle,
                        cssClass: "indicator partition-type-colour"
                    }
                },
                edge: {
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
        };

        /**
         * Does an AJAX call to the backend MSF4J service that converts the 'siddhiCode'
         * to a specific JSON format to be displayed in the UI.
         *
         * @param siddhiCode The Siddhi App as a string
         * @returns A JSON with a response status, and the JSON returned by the back-end
         */
        EventFlow.prototype.fetchJSON = function (siddhiCode) {
            var self = this;
            var result = {};

            if (siddhiCode === self.defaultCode) {
                result = {status: "success", responseJSON: null};
            } else if (siddhiCode === null || siddhiCode === undefined || siddhiCode === "") {
                result = {status: "fail", errorMessage: "The Siddhi App Cannot Be Empty"};
            } else {
                fetch(siddhiCode);
            }

            function fetch(code) {
                $.ajax({
                    type: "POST",
                    url: self.url,
                    data: window.btoa(code),
                    // todo make async and handle loading issues
                    async: false,
                    success: function (response) {
                        result = {status: "success", responseJSON: response};
                        // todo remove this log once done
                        log.info(response);
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
                showDefaultGraph();
            } else {
                createGraph();
            }

            function createGraph() {

                var nodeOptions = self.renderOptions.node;
                var edgeOptions = self.renderOptions.edge;

                // todo set any fixed values to a seperate JSON and obtain it from there.
                // Create an instance of the dagreD3 graph.
                var graph = new dagreD3.graphlib.Graph({compound: true}).setGraph({});
                // This makes sure the graph grows from left-to-right.
                graph.graph().rankDir = "LR";

                // Set the nodes of the graph
                data.nodes.forEach(function (node) {
                    var html;
                    var nodeStyle;

                    if (node.type === nodeOptions.partition.name) {
                        html = "<div class='" + nodeOptions.partition.cssClass + "' title='"
                            + node.description + "'>" + node.name + "</div>";
                        nodeStyle = _.clone(nodeOptions.partition.nodeStyle);
                        nodeStyle.label = html;
                    } else {
                        html = "<div title = '" + node.description + "'>";
                        switch (node.type) {
                            case nodeOptions.stream.name:
                                html = html + "<span class='indicator stream-colour'></span>";
                                nodeStyle = _.clone(nodeOptions.stream.nodeStyle);
                                break;
                            case nodeOptions.table.name:
                                html = html + "<span class='indicator table-colour'></span>";
                                nodeStyle = _.clone(nodeOptions.table.nodeStyle);
                                break;
                            case nodeOptions.window.name:
                                html = html + "<span class='indicator window-colour'></span>";
                                nodeStyle = _.clone(nodeOptions.window.nodeStyle);
                                break;
                            case nodeOptions.trigger.name:
                                html = html + "<span class='indicator trigger-colour'></span>";
                                nodeStyle = _.clone(nodeOptions.trigger.nodeStyle);
                                break;
                            case nodeOptions.aggregation.name:
                                html = html + "<span class='indicator aggregation-colour'></span>";
                                nodeStyle = _.clone(nodeOptions.aggregation.nodeStyle);
                                break;
                            case nodeOptions.function.name:
                                html = html + "<span class='indicator function-colour'></span>";
                                nodeStyle = _.clone(nodeOptions.function.nodeStyle);
                                break;
                            case nodeOptions.query.name:
                                html = html + "<span class='indicator query-colour'></span>";
                                nodeStyle = _.clone(nodeOptions.query.nodeStyle);
                                break;
                            case nodeOptions.partition_type.name:
                                html = html + "<span class='indicator partition-type-colour'></span>";
                                nodeStyle = _.clone(nodeOptions.partition_type.nodeStyle);
                                break;
                        }
                        html = html + "<span class='nodeLabel'>" + node.name + "</span>" + "</div>";
                        nodeStyle.label = html;
                    }
                    // Set the node
                    graph.setNode(node.id, nodeStyle);

                });
                console.log(self.renderOptions);

                // Set the edges of the graph
                data.edges.forEach(function (edge) {
                    var edgeStyle = {arrowheadStyle: "fill: #bbb"};

                    if (edge.type === "default") {
                        // This makes the lines curve
                        edgeStyle.lineInterpolate = "basis";
                    } else if (edge.type === "dotted-line") {
                        // This makes the lines dotted
                        edgeStyle.style = "stroke-dasharray: 5, 5;";
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

                var render = new dagreD3.render();
                var graphId = "#" + self.$siddhiGraph.attr("id");
                render(d3.select(graphId + " g"), graph);

                centerGraphPosition(self.$siddhiGraph);

                function centerGraphPosition(svg) {
                    var inner = svg.find('g');

                    svg.attr("width", inner.get(0).getBoundingClientRect().width + 60);
                    svg.attr("height", inner.get(0).getBoundingClientRect().height + 60);

                    var graphWidth = parseInt(svg.attr("width"));
                    var graphHeight = parseInt(svg.attr("height"));
                    var width = self.$graphView.width();
                    var height = self.$graphView.height();

                    var left = diff(width, graphWidth) / 2;
                    var top = diff(height, graphHeight) / 2;
                    svg.attr("transform", "translate(" + left + "," + top + ")");
                }

                function diff(divValue, graphValue) {
                    if (divValue > graphValue) {
                        return (divValue - graphValue);
                    } else {
                        return 0;
                    }
                }
            }

            function showDefaultGraph() {
                self.$siddhiAppName.html("SiddhiApp");
                self.$siddhiAppDescription.html("Description of the plan");
            }
        };

        /**
         * Clears the design view of all it's contents when called.
         */
        EventFlow.prototype.clear = function () {
            // todo if the app has not been changed, then no need to re-render
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
