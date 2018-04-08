import React from 'react';
import * as d3 from "d3";
import * as dagreD3 from 'dagre-d3';
import {Link} from "react-router-dom";

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
                        var html;
                        var sourceHtml;
                        var sinkHtml;

                        var app = entry.siddhiApp.replace(/\'|\"/g,'').replace(/>/g,'\>');

                        var wordLength;

                          html=  "<div id='container' class='node-content' title='"+ app +"'>"
                            +"<span class='indicator '></span>"
                            + "<span id='myTextInput' class='nodeLabel'>" + entry.appName + "</span>"
                            + "</div>";

                        g.setNode(entry.appName, {labelType: "html", label: html,paddingBottom:0,paddingTop:0,paddingLeft:0,paddingRight:130,rx: 0, ry: 0});
                        wordLength = entry.appName.length;


                        entry.sourceList.map((source) => {
                            sourceHtml =
                                "<div id='container' class='node-content'>"
                                +"<span class='topic-indicator '></span>"
                                + "<span id='myTextInput' class='nodeLabel'>" + source + "</span>"
                                + "</div>";

                            console.log("sources" + source);
                             g.setNode(source, {labelType: "html", label: sourceHtml,paddingBottom:0,paddingTop:0,paddingLeft:0,paddingRight:150,rx: 0, ry: 0},{labelStyle:"width:300"});

                        });

                        entry.sinkList.map((sink) => {

                            sinkHtml =
                                "<div id='container' class='node-content'>"
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
                        let initialScale = 0.85;
                        svg.call(zoom.transform, d3.zoomIdentity.translate((svg.attr("width") - g.graph().width * initialScale) / 2, 10).scale(initialScale));
                        svg.classed("nodeTree", true);
                        render(inner, g);

                        // // Center the graph
                        // let initialScale = 0.95;
                        // svg.call(zoom.transform, d3.zoomIdentity.translate((svg.attr("width") - g.graph().width * initialScale) / 2, 10).scale(initialScale));
                        // svg.classed("nodeTree", true);
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
                    // paddingRight: '10%',
                    verticalAlign: 'middle',
                    overflow: 'scroll'
                }}

                preserveAspectRatio="xMidYMin meet" viewBox="-100 -100 2000 500"
                height="500"
                width="300"
            >
                {/*{alert("Hi am here")}*/}
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
