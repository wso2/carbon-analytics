/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

import React from 'react';
import * as d3 from "d3";
import * as dagreD3 from 'dagre-d3';

import StatusDashboardAPIS from "../utils/apis/StatusDashboardAPIs";
import {HttpStatus} from "../utils/Constants";

export default class KafkaFlow extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            kafkaData: {},
            workerId: this.props.id,
            appName: this.props.appName,
        }
    }

    componentDidMount() {
        StatusDashboardAPIS.getKafkaDetails(this.state.workerId, this.state.appName)
            .then((response) => {
                if (response.status === HttpStatus.OK) {
                    this.setState({
                        kafkaData: response.data
                    });

                    let g = new dagreD3.graphlib.Graph({compound: true}).setGraph({});

                    g.graph().rankDir = "LR";

                    this.state.kafkaData.map((entry) => {
                        let html;
                        let sourceHtml;
                        let sinkHtml;

                        let app = entry.siddhiApp.replace(/\'|\"/g,'').replace(/>/g,'\>');

                        let wordLength;
                        let isInactive = (entry.deployedHost === undefined && entry.deployedPort === undefined);
                        {isInactive ? (

                            html=  "<div id='container' class='node-content' title='"+ app +"'>"
                                +"<span class='indicator '></span>"
                                + "<span id='myTextInput' class='nodeLabel'>" + entry.appName  + "</span>"

                                +"<span id='hostPort' class='hostPort' title='Deployed Node'>"+"Un-Deployed App"+"</span> "

                                + "</div>"

                        ) : (

                            html=  "<div id='container' class='node-content' title='"+ app +"'>"
                                +"<span class='indicator '></span>"
                                + "<span id='myTextInput' class='nodeLabel'>" + entry.appName  + "</span>"

                                +"<span id='hostPort' class='hostPort' title='Deployed Node'>"+ entry.deployedHost+" : "+entry.deployedPort+"</span> "

                                + "</div>"

                        )}

                        g.setNode(entry.appName, {labelType: "html", label: html,paddingBottom:0,paddingTop:0,paddingLeft:0,paddingRight:130,rx: 0, ry: 0});
                        wordLength = entry.appName.length;

                        entry.sourceList.map((source) => {
                            sourceHtml =
                                "<div id='container' class='node-content' title='kafka_topic'>"
                                +"<span class='topic-indicator '></span>"
                                + "<span id='myTextInput' class='nodeLabel'style='margin-top: 10px'>" + source + "</span>"
                                + "</div>";
                            g.setNode(source, {labelType: "html", label: sourceHtml,paddingBottom:0,paddingTop:0,paddingLeft:0,paddingRight:150,rx: 0, ry: 0},{labelStyle:"width:300"});
                        });

                        entry.sinkList.map((sink) => {

                            sinkHtml =
                                "<div id='container' class='node-content' title='kafka_topic'>"
                                +"<span class='topic-indicator '></span>"
                                + "<span id='myTextInput' class='nodeLabel' itemref='myTextInput'>"  + sink + "</span>"
                                + "</div>";
                            g.setNode(sink, {labelType: "html", label: sinkHtml,paddingBottom:0,paddingTop:0,paddingLeft:0,paddingRight:130,rx: 0, ry: 0});

                            wordLength = sink.length;

                        });

                        g.nodes().forEach(function(v) {
                            let node = g.node(v);
                            // Round the corners of the nodes
                            node.rx = node.ry = 5;
                        });
                        entry.sourceList.map((edgeSource) => {
                            g.setEdge(edgeSource, entry.appName, {
                                arrowheadStyle: "fill: #bbb",
                                lineInterpolate: "basis"
                            });
                        });

                        entry.sinkList.map((edgeSink) => {
                            g.setEdge(entry.appName, edgeSink, {
                                arrowheadStyle: "fill: #bbb",
                                lineInterpolate: "basis"
                            });
                        });

                        // Create the renderer
                        let render = new dagreD3.render();

                        let svg = d3.select(this.nodeTree);
                        let inner = svg.select("g");
                        // Set up zoom support
                        let zoom = d3.zoom().on("zoom", function () {
                            inner.attr("transform", d3.event.transform);
                        });
                        svg.call(zoom);

                        svg.attr("height", g.graph().height + 40).attr('width', g.graph().width + 40);
                        // Center the graph
                        let initialScale = 0.95;
                        svg.call(zoom.transform, d3.zoomIdentity.translate((svg.attr("width") - g.graph().width * initialScale) / 2, 10).scale(initialScale));
                        svg.classed("nodeTree", true);
                        render(inner, g);
                    });
                }
            }).catch((error) => {
            if (error.response != null) {
                if (error.response.status === 401) {
                    this.setState({
                        isApiCalled: true,
                        sessionInvalid: true,
                        statusMessage: "Authentication fail. Please login again."
                    })
                } else if (error.response.status === 403) {
                    this.setState({
                        isApiCalled: true,
                        statusMessage: "User Have No Permission to view this page."
                    });
                } else {
                    this.setState({
                        isError: true,
                        isApiCalled: true,
                        statusMessage: "Unknown error occurred! : " + JSON.stringify(error.response.data)
                    });
                }
            }
        });
    }

    render() {
        return (
            <svg
                id="nodeTree"
                ref={(ref) => {
                    this.nodeTree = ref;
                }}

                style={{
                    display: 'inline-block',
                    position: 'relative',
                    width: '100%',
                    verticalAlign: 'middle',
                    overflow: 'scroll'
                }}

                preserveAspectRatio="xMidYMin meet" viewBox="-100 -100 2000 500"
                height="500"
                width="300"
            >
                <g ref={(r) => {
                    this.nodeTreeGroup = r;
                }}
                   style={{
                       // display: 'inline-block',
                       position: 'relative',
                       top: '10',
                       left: '0'
                   }}
                   viewBox="0 0 100 100"
                />
            </svg>
        );
    }
}
