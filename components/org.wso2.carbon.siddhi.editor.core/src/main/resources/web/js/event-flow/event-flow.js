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
                    async: false,
                    success: function (response) {
                        result = {status: "success", responseJSON: response};
                    },
                    error: function (error) {
                        if (error.status === 400) {
                            result = {status: "fail", errorMessage: "Siddhi App Contains Errors"};
                            log.info(error.responseText);
                        } else {
                            result = {status: "fail", errorMessage: "Internal Error Occurred"};
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
                var graph = new dagreD3.graphlib.Graph({compound: true}).setGraph({});
                graph.graph().rankDir = "LR";

                // Set the nodes of the graph
                data.nodes.forEach(function (value) {
                    var html;
                    var node;

                    if (value.type === "partition") {
                        html = "<div class='partition' title='" + value.description + "'>" + value.name + "</div>";
                        node = {
                            label: html,
                            labelType: "html",
                            clusterLabelPos: 'top',
                            style: 'fill: #e0e0d1'
                        };
                    } else {
                        html = "<div title = '" + value.description + "'>";
                        switch (value.type) {
                            case "stream":
                                html = html + "<span class='indicator stream-colour'></span>";
                                break;
                            case "table":
                                html = html + "<span class='indicator table-colour'></span>";
                                break;
                            case "window":
                                html = html + "<span class='indicator window-colour'></span>";
                                break;
                            case "trigger":
                                html = html + "<span class='indicator trigger-colour'></span>";
                                break;
                            case "aggregation":
                                html = html + "<span class='indicator aggregation-colour'></span>";
                                break;
                            case "function":
                                html = html + "<span class='indicator function-colour'></span>";
                                break;
                            case "query":
                                html = html + "<span class='indicator query-colour'></span>";
                                break;
                            case "partitionType":
                                html = html + "<span class='indicator partitionType-colour'></span>";
                                break;
                        }
                        html = html + "<span class='nodeLabel'>" + value.name + "</span>" + "</div>";
                        node = {
                            label: html,
                            labelType: "html",
                            rx: 7,
                            ry: 7,
                            padding: 0
                        };
                    }
                    // Set the node
                    graph.setNode(value.id, node);
                });

                // Set the edges of the graph
                data.edges.forEach(function (value) {
                    var edge = {arrowheadStyle: "fill: #bbb"};

                    if (value.type === "arrow") {
                        // This makes the lines curve
                        edge.lineInterpolate = "basis";
                    } else if (value.type === "dotted-line") {
                        // This makes the lines dotted
                        edge.style = "stroke-dasharray: 5, 5;";
                    }

                    // Set the edge
                    graph.setEdge(value.parent, value.child, edge);
                });

                // Set the groups of the graph
                data.groups.forEach(function (value) {
                    value.children.forEach(function (child) {
                        graph.setParent(child, value.id);
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

                var svg = self.$siddhiGraph;
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
