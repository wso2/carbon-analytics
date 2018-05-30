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
import * as d3 from 'd3';
import * as dagreD3 from 'dagre-d3';

import StatusDashboardAPIS from '../utils/apis/StatusDashboardAPIs';

export default class AppEventFlow extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            appData: {},
            workerId: this.props.id,
            appName: this.props.appName,
        }
    }

    componentDidMount() {
        StatusDashboardAPIS.getSiddhiAppElementsByName(this.state.workerId, this.state.appName)
            .then((response) => {
                if (response.status === 200) {
                    this.setState({
                        appData: response.data
                    });
                }
                let g = new dagreD3.graphlib.Graph({compound: true}).setGraph({});

                g.graph().rankDir = "LR";

                this.state.appData.map((entry) => {
                    let inputContainer;
                    let outputContainer;
                    let siddhiAppContainer;
                    let partitionType;
                    let partitionContainer;
                    let functionContainer;
                    let inputStreamSiddhiApp = entry.inputStreamSiddhiApp.replace(/\'|\"/g, '').replace(/>/g, '\>');
                    let outputStreamSiddhiApp = entry.outputStreamSiddhiApp.replace(/\'|\"/g, '').replace(/>/g, '\>');

                    const inputCssClassType = entry.inputStreamType.toLowerCase();
                    inputContainer =
                        `<div id="container" class="node-content" title="${inputStreamSiddhiApp}">
                            <span class="${inputCssClassType}-indicator"></span>
                            <span id="myTextInput" class="nodeLabel" style="margin-top: 10px">${entry.inputStreamId}</span>
                         </div>`;
                    g.setNode(entry.inputStreamId, {
                        labelType: "html",
                        label: inputContainer,
                        paddingBottom: 0,
                        paddingTop: 0,
                        paddingLeft: 0,
                        paddingRight: 150,
                        rx: 0,
                        ry: 0
                    }, {labelStyle: "width:300"});

                    const outputCssClassNamePrefix = entry.outputStreamType.toLowerCase();
                    outputContainer =
                        `<div id="container" class="node-content" title="${outputStreamSiddhiApp}">
                            <span class="${outputCssClassNamePrefix}-indicator"></span>
                            <span id="myTextInput" class="nodeLabel" style="margin-top: 10px">${entry.outputStreamId}</span>
                         </div>`;
                    g.setNode(entry.outputStreamId, {
                        labelType: "html",
                        label: outputContainer,
                        paddingBottom: 0,
                        paddingTop: 0,
                        paddingLeft: 0,
                        paddingRight: 150,
                        rx: 0,
                        ry: 0
                    }, {labelStyle: "width:300"});


                    if (entry.query !== undefined && entry.partitionType === 'Value Partition' || entry.partitionType === 'Range Partition') {
                        siddhiAppContainer =
                            `<div id="container" class="node-content" title='${entry.partitionQuery}'>
                                <span class="query-indicator"></span>
                                <span id="myTextInput" class="nodeLabel" style="margin-top: 10px">query</span>
                             </div>`;
                        g.setNode(entry.query, {
                            labelType: "html",
                            label: siddhiAppContainer,
                            paddingBottom: 0,
                            paddingTop: 0,
                            paddingLeft: 0,
                            paddingRight: 150,
                            rx: 0,
                            ry: 0
                        }, {labelStyle: "width:300"});
                    } else if (entry.query !== undefined) {
                        siddhiAppContainer =
                            `<div id="container" class="node-content" title='${entry.query}'>
                                <span class="query-indicator"></span>
                                <span id="myTextInput" class="nodeLabel" style="margin-top: 10px">${entry.queryName}</span>
                             </div>`;
                        g.setNode(entry.query, {
                            labelType: "html",
                            label: siddhiAppContainer,
                            paddingBottom: 0,
                            paddingTop: 0,
                            paddingLeft: 0,
                            paddingRight: 150,
                            rx: 0,
                            ry: 0
                        }, {labelStyle: "width:300"});
                    }

                    if (entry.function !== undefined) {
                        functionContainer =
                            `<div id="container" class="node-content" title="${entry.functionQuery}">
                                <span class="function-indicator"></span>
                                <span id="myTextInput" class="nodeLabel" style="margin-top: 10px">${entry.function}</span>
                             </div>`;
                        g.setNode(entry.function, {
                            labelType: "html",
                            label: functionContainer,
                            paddingBottom: 0,
                            paddingTop: 0,
                            paddingLeft: 0,
                            paddingRight: 150,
                            rx: 0,
                            ry: 0
                        }, {labelStyle: "width:300"});
                    }

                    if (entry.partitionType === 'Value Partition' || entry.partitionType === 'Range Partition') {
                        partitionType =
                            `<div id="container" class="node-content" title="${entry.partitionTypeQuery}">
                                <span class="partition-type-colour"></span>
                                <span id="myTextInput" class="nodeLabel" style="margin-top: 10px">${entry.partitionType}</span>
                             </div>`;
                        g.setNode(entry.partitionType, {
                            labelType: "html",
                            label: partitionType,
                            paddingBottom: 0,
                            paddingTop: 0,
                            paddingLeft: 0,
                            paddingRight: 150,
                            rx: 0,
                            ry: 0
                        }, {labelStyle: "width:300"});

                        partitionContainer =
                            `<div id="container" class="partition" title='${entry.query}'>${entry.queryName}</div>`;
                        g.setNode("partition", {
                            labelType: "html",
                            label: partitionContainer,
                            paddingBottom: 0,
                            paddingTop: 0,
                            paddingLeft: 0,
                            paddingRight: 150,
                            rx: 0,
                            ry: 0
                        }, {fill: 'red'});
                        g.setParent(entry.partitionType, "partition")
                        g.setParent(entry.query, "partition")

                    }
                    if (entry.partitionType === 'Value Partition' || entry.partitionType === 'Range Partition') {
                        g.setEdge(entry.inputStreamId, entry.partitionType, {
                            arrowheadStyle: "fill: #bbb",
                            lineInterpolate: "basis"
                        });
                        if (entry.query !== undefined) {
                            g.setEdge(entry.partitionType, entry.query, {
                                arrowheadStyle: "fill: #bbb",
                                lineInterpolate: "basis"
                            });
                            g.setEdge(entry.query, entry.outputStreamId, {
                                arrowheadStyle: "fill: #bbb",
                                lineInterpolate: "basis"
                            });
                        } else {
                            g.setEdge(entry.partitionType, entry.outputStreamId, {
                                arrowheadStyle: "fill: #bbb",
                                lineInterpolate: "basis"
                            });
                        }

                    } else {
                        if (entry.query !== undefined) {
                            g.setEdge(entry.inputStreamId, entry.query, {
                                arrowheadStyle: "fill: #bbb",
                                lineInterpolate: "basis"
                            });


                            g.setEdge(entry.query, entry.outputStreamId, {
                                arrowheadStyle: "fill: #bbb",
                                lineInterpolate: "basis"
                            });
                        } else {
                            g.setEdge(entry.inputStreamId, entry.outputStreamId, {
                                arrowheadStyle: "fill: #bbb",
                                lineInterpolate: "basis"
                            });
                        }

                        if (entry.function !== undefined) {
                            g.setEdge(entry.function, entry.query, {
                                arrowheadStyle: "fill: #bbb",
                                lineInterpolate: "basis"
                            });
                        }
                    }

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
                    let initialScale = 2.25;
                    svg.call(zoom.transform, d3.zoomIdentity.translate((svg.attr("width") - g.graph().width * initialScale) / 2, 10).scale(initialScale));
                    svg.classed("nodeTree", true);
                    render(inner, g);
                });

            })
            .catch((error) => {
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
                    display: 'flex',
                    position: 'relative',
                    width: '100%',
                    height: '100%',
                    verticalAlign: 'middle',
                    overflow: 'scroll'
                }}

                preserveAspectRatio="xMidYMin meet" viewBox="-900 -70 5000 500"
                height="500"
                width="300"
            >
                <g ref={(r) => {
                    this.nodeTreeGroup = r;
                }}
                   style={{
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
