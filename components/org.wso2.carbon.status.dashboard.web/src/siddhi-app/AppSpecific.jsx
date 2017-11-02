/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React from "react";
import {Link} from "react-router-dom";
import SyntaxHighlighter from "react-syntax-highlighter";
//App Components
import StatusDashboardAPIS from "../utils/apis/StatusDashboardAPIs";
import ComponentTable from "./ComponentTable";
import VizG from "../gadgets/VizG";
//Material UI
import {GridList, GridTile} from "material-ui/GridList";
import HomeButton from "material-ui/svg-icons/action/home";
import {
    Card,
    CardHeader,
    CardText,
    CardTitle,
    Dialog,
    Divider,
    FlatButton,
    FloatingActionButton,
    IconButton,
    Toggle
} from "material-ui";

const styles = {
    root: {display: 'flex', flexWrap: 'wrap', justifyContent: 'space-around'},
    gridList: {width: '90%', height: '50%', overflowY: 'auto', padding: 10, paddingLeft: 40}
};
const memoryMetadata = {names: ['timestamp', 'memory'], types: ['time', 'linear']};
const memoryLineChartConfig = {
    x: 'timestamp',
    charts: [{type: 'line', y: 'memory', fill: '#f17b31'}],
    width: 700,
    height: 300,
    tickLabelColor: '#9c9898',
    axisLabelColor: '#9c9898'
};
const latencyMetadata = {names: ['timestamp', 'latency'], types: ['time', 'linear']};
const latencyLineChartConfig = {
    x: 'timestamp',
    charts: [{type: 'line', y: 'latency', fill: '#f17b31'}],
    width: 700,
    height: 300, tickLabelColor: '#9c9898',
    axisLabelColor: '#9c9898'
};
const tpMetadata = {names: ['timestamp', 'throughput'], types: ['time', 'linear']};
const tpLineChartConfig = {
    x: 'timestamp',
    charts: [{type: 'line', y: 'throughput', fill: '#f17b31'}],
    width: 700,
    height: 300, tickLabelColor: '#9c9898',
    axisLabelColor: '#9c9898'
};
const codeViewStyle = {
    "hljs": {
        "display": "block",
        "overflowX": "auto",
        "padding": "0.5em",
        "background": "#131313",
        "color": "#dad9d9"
    },
    "hljs-comment": {"color": "#777"},
    "hljs-quote": {"color": "#777"},
    "hljs-variable": {"color": "#ab875d"},
    "hljs-template-variable": {"color": "#ab875d"},
    "hljs-tag": {"color": "#ab875d"},
    "hljs-regexp": {"color": "#ab875d"},
    "hljs-meta": {"color": "#ab875d"},
    "hljs-number": {"color": "#ab875d"},
    "hljs-built_in": {"color": "#ab875d"},
    "hljs-builtin-name": {"color": "#ab875d"},
    "hljs-literal": {"color": "#ab875d"},
    "hljs-params": {"color": "#ab875d"},
    "hljs-symbol": {"color": "#ab875d"},
    "hljs-bullet": {"color": "#ab875d"},
    "hljs-link": {"color": "#ab875d"},
    "hljs-deletion": {"color": "#ab875d"},
    "hljs-section": {"color": "#9b869b"},
    "hljs-title": {"color": "#9b869b"},
    "hljs-name": {"color": "#9b869b"},
    "hljs-selector-id": {"color": "#9b869b"},
    "hljs-selector-class": {"color": "#9b869b"},
    "hljs-type": {"color": "#9b869b"},
    "hljs-attribute": {"color": "#9b869b"},
    "hljs-string": {"color": "#f17b31"},
    "hljs-keyword": {"color": "#f17b31"},
    "hljs-selector-tag": {"color": "#f17b31"},
    "hljs-addition": {"color": "#f17b31"},
    "hljs-emphasis": {"fontStyle": "italic"},
    "hljs-strong": {"fontWeight": "bold"}
};
/**
 * class which manages Siddhi App specific details.
 */
export default class WorkerSpecific extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            latency: [],
            throughputAll: [],
            totalMem: [],
            workerID: this.props.match.params.id.split("_")[0] + ":" + this.props.match.params.id.split("_")[1],
            appName: this.props.match.params.appName,
            id: this.props.match.params.id,
            appText: '',
            toggled: true,
            open: false
        };
        this.handleToggle = this.handleToggle.bind(this);
    }

    componentWillMount() {
        let that = this;
        StatusDashboardAPIS.getSiddhiAppByName(this.props.match.params.id, this.props.match.params.appName)
            .then((response) => {
                that.setState({
                    appText: response.data.content
                });
                StatusDashboardAPIS.getSiddhiAppHistoryByID(this.props.match.params.id, this.props.match.params.appName, '')
                    .then((response) => {
                        that.setState({
                            latency: response.data[0].latency.data,
                            throughputAll: response.data[0].throughput.data,
                            totalMem: response.data[0].memory.data
                        });
                    });
            });
    }

    renderLatencyChart() {
        if (this.state.latency.length === 0) {
            return (
                <GridTile title="Latency" titlePosition="top" titleBackground='#303030'>
                    <div style={{
                        color: 'rgba(255, 255, 255, 0.2)',
                        marginTop: 50,
                        backgroundColor: '#131313',
                        padding: 30,
                        textAlign: 'center',
                        height: 300
                    }}><h2>No
                           data available</h2></div>
                </GridTile>
            );
        }
        return (
            <GridTile className="container" title="Latency" titlePosition="top" titleBackground='#303030'>
                <div className="overlay"
                     style={{color: 'rgba(255, 255, 255, 0.2)', paddingTop: 20, textAlign: 'right'}}>
                    <h4>Click for more details</h4>
                </div>
                <div style={{marginTop: 50, backgroundColor: '#131313', padding: 20}}>
                    <Link
                        to={"/sp-status-dashboard/worker/" + this.props.match.params.id + "/siddhi-apps/" +
                        this.props.match.params.appName + "/history"}>
                        <VizG data={this.state.latency} metadata={latencyMetadata}
                              config={latencyLineChartConfig}/>
                    </Link>
                </div>
            </GridTile>
        );
    }

    renderThroughputChart() {
        if (this.state.throughputAll.length === 0) {
            return (
                <GridTile title="Overall Throughput" titlePosition="top" titleBackground='#303030'>
                    <div style={{
                        color: 'rgba(255, 255, 255, 0.2)',
                        marginTop: 50,
                        backgroundColor: '#131313',
                        padding: 30,
                        textAlign: 'center',
                        height: 300
                    }}><h2>No
                           data available</h2></div>
                </GridTile>
            );
        }
        return (
            <GridTile className="container" title="Overall Throughput" titlePosition="top"
                      titleBackground='#303030'>
                <div className="overlay"
                     style={{color: 'rgba(255, 255, 255, 0.2)', paddingTop: 20, textAlign: 'right'}}>
                    <h4>Click for more details</h4>
                </div>
                <div style={{marginTop: 50, backgroundColor: '#131313', padding: 20}}>
                    <Link
                        to={"/sp-status-dashboard/worker/" + this.props.match.params.id + "/siddhi-apps/" +
                        this.props.match.params.appName + "/history"}>
                        <VizG data={this.state.throughputAll} metadata={tpMetadata} config={tpLineChartConfig}/>
                    </Link>
                </div>
            </GridTile>
        );
    }

    renderMemoryChart() {
        if (this.state.totalMem.length === 0) {
            return (
                <GridTile title="Memory Used" titlePosition="top" titleBackground='#303030'>
                    <div style={{
                        marginTop: 50,
                        backgroundColor: '#131313',
                        padding: 30,
                        textAlign: 'center',
                        height: 300,
                        color: 'rgba(255, 255, 255, 0.2)'
                    }}><h2>No
                           data available</h2></div>
                </GridTile>
            );
        }
        return (
            <GridTile className="container" title="Memory Used" titlePosition="top"
                      titleBackground='#303030'>
                <div className="overlay"
                     style={{color: 'rgba(255, 255, 255, 0.2)', paddingTop: 20, textAlign: 'right'}}>
                    <h4>Click for more details</h4>
                </div>
                <div style={{marginTop: 50, backgroundColor: '#131313', padding: 20}}>
                    <Link
                        to={"/sp-status-dashboard/worker/" + this.props.match.params.id + "/siddhi-apps/" +
                        this.props.match.params.appName + "/history"}>
                        <VizG data={this.state.totalMem} metadata={memoryMetadata}
                              config={memoryLineChartConfig}/>
                    </Link>
                </div>
            </GridTile>
        );
    }

    handleToggle(state) {
        console.log(state);
        let statEnable = JSON.stringify({
            statsEnable: false
        });
        //todo fix 
        // StatusDashboardAPIS.enableSiddhiAppStats(this.state.id, this.state.appName,statEnable)
        //     .then((response) => {
        //         if (response.status === 200) {
        //             this.setState({toggled: true, open: true})
        //         }
        //     })
    }

    render() {
        let actionsButtons = [
            <FlatButton
                label="Yes"
                backgroundColor='#f17b31'
                onClick={this.handleToggle(true)}
            />,
            <FlatButton
                label="No"
                onClick={() => {
                    this.setState({toggled: false})
                }}
            />,
        ];

        let actionsButton = [
            <FlatButton
                label="OK"
                backgroundColor='#f17b31'
                onClick={() => {
                    this.setState({open: false})
                }}
            />
        ];
        return (
            <div>
                <Dialog
                    actions={actionsButtons}
                    modal
                    open={!this.state.toggled}
                    onRequestClose={() => this.setState({toggled: true})}>
                    Disabling metrics of a SiddhiApp will cause a data loss.
                    Are you sure you want to disable metrics?
                </Dialog>

                <Dialog
                    actions={actionsButton}
                    modal
                    open={this.state.open}
                    onRequestClose={() => this.setState({toggled: true})}>
                    Successfully disabled metrics!
                </Dialog>

                <div>
                    <div className="navigation-bar">
                        <Link to="/sp-status-dashboard/overview"><FlatButton label="Overview >"
                                                                             icon={<HomeButton color="black"/>}/></Link>
                        <Link to={"/sp-status-dashboard/worker/" + this.props.match.params.id }>
                            <FlatButton label={this.state.workerID + " >"}/></Link>
                        <FlatButton label={this.props.match.params.appName}/>
                    </div>
                    <div className="worker-h1">
                        <h2 style={{display: 'inline-block', float: 'left', marginLeft: 20}}> {this.state.workerID}
                            : {this.state.appName} </h2>
                    </div>

                    <div style={{float: 'right', padding: 20, paddingRight: 20}}>
                    <Toggle labelPosition="left"
                    label="Metrics"
                    labelStyle={{color: 'white'}}
                    thumbStyle={{backgroundColor: 'grey'}}
                    thumbSwitchedStyle={{backgroundColor: '#f17b31'}}
                    trackSwitchedStyle={{backgroundColor: '#f17b31'}}
                    toggled={this.state.toggled}
                    onClick={() => this.setState({toggled: !this.state.toggled}) }
                    >
                    </Toggle>
                    </div>

                    <GridList cols={3} padding={20} cellHeight={250} style={styles.gridList}>
                        {this.renderLatencyChart()}
                        {this.renderThroughputChart()}
                        {this.renderMemoryChart()}
                    </GridList>
                </div>

                <div style={{padding: 10, paddingLeft: 40, width: '90%', backgroundColor: "#222222"}}>
                    <Card style={{backgroundColor: "#282828"}}>
                        <CardHeader title="Code View" subtitle={this.props.match.params.appName}
                                    titleStyle={{fontSize: 24, backgroundColor: "#282828"}}
                        />
                        <Divider/>

                        <CardText>
                            <SyntaxHighlighter language='sql'
                                               style={codeViewStyle}>{this.state.appText}</SyntaxHighlighter>
                        </CardText>
                    </Card>
                </div>

                <div style={{padding: 40, width: '90%'}}>
                    <h3 style={{color: 'white', marginLeft: 20}}> Siddhi App Component Statistics</h3>
                    <ComponentTable id={this.props.match.params.id} appName={this.props.match.params.appName}/>
                </div>
            </div>
        );
    }

}


