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
//App Components
import StatusDashboardAPIS from "../utils/apis/StatusDashboardAPIs";
import ChartCard from "../common/ChartCard";
import Header from "../common/Header";
import {ComponentType} from '../utils/Constants';
//Material UI
import {Toolbar, ToolbarGroup} from "material-ui/Toolbar";
import HomeButton from "material-ui/svg-icons/action/home";
import {Card, CardHeader, CardMedia, Divider, FlatButton, RaisedButton} from "material-ui";
import DashboardUtils from "../utils/DashboardUtils";

const styles = {button: {margin: 12, backgroundColor: '#f17b31'}};
const toolBar = {width: '50%', marginLeft: '50%', padding: 20, backgroundColor: '#424242'};

const latencyMetadata = {
    names: ['Time', 'Count', 'Max', 'Mean', 'Min', 'Standard Deviation', 'P75', 'P95', 'P99', 'P999',
        'Mean Rate', 'M1 Rate', 'M5 Rate', 'M15 Rate'],
    types: ['time', 'linear', 'linear', 'linear', 'linear', 'linear', 'linear', 'linear', 'linear', 'linear', 'linear',
        'linear', 'linear', 'linear']
};
const latencyLineChartConfig = {
    x: 'Time',
    charts: [{type: 'line', y: 'Count', fill: '#058DC7',  style: {markRadius: 2}},
        {type: 'line', y: 'Max', fill: '#50B432',  style: {markRadius: 2}},
        {type: 'line', y: 'Mean', fill: '#f17b31',  style: {markRadius: 2}},
        {type: 'line', y: 'Min', fill: '#8c51a5',  style: {markRadius: 2}},
        {type: 'line', y: 'Standard Deviation', fill: '#FFEB3B',  style: {markRadius: 2}},
        {type: 'line', y: 'P75', fill: '#70dbed',  style: {markRadius: 2}},
        {type: 'line', y: 'P95', fill: '#ffb873',  style: {markRadius: 2}},
        {type: 'line', y: 'P99', fill: '#95dd87', style: {markRadius: 2}},
        {type: 'line', y: 'P999',fill: '#890f02', style: {markRadius: 2}},
        {type: 'line', y: 'Mean Rate', fill: '#ff918f',style: {markRadius: 2}},
        {type: 'line', y: 'M1 Rate', fill: '#b76969', style: {markRadius: 2}},
        {type: 'line', y: 'M5 Rate', fill: '#aea2e0', style: {markRadius: 2}},
        {type: 'line', y: 'M15 Rate',fill: '#FFEB3B', style: {markRadius: 2}}
    ],
    width: 800,
    height: 250,
    style: {
        tickLabelColor:'#f2f2f2',
        legendTextColor: '#9c9898',
        legendTitleColor: '#9c9898',
        axisLabelColor: '#9c9898'
    },
    tipTimeFormat:"%Y-%m-%d %H:%M:%S %Z",
    legend:true,
    interactiveLegend: true,
    gridColor: '#f2f2f2',
    xAxisTickCount:10
};
const memoryMetadata = {names: ['Time', 'Memory'], types: ['time', 'linear']};
const memoryLineChartConfig = {
    x: 'Time',
    charts: [{type: 'line', y: 'Memory', fill: '#f17b31',  style: {markRadius: 2}}],
    width: 800,
    height: 250,
    style: {
        tickLabelColor:'#f2f2f2',
        legendTextColor: '#9c9898',
        legendTitleColor: '#9c9898',
        axisLabelColor: '#9c9898'
    },
    tipTimeFormat:"%Y-%m-%d %H:%M:%S %Z",
    legend:true,
    interactiveLegend: true,
    gridColor: '#f2f2f2',
    xAxisTickCount:10
};
const tpMetadata = {
    names: ['Time', 'Count', 'Mean Rate', 'M1 Rate', 'M5 Rate', 'M15 Rate'],
    types: ['time', 'linear', 'linear', 'linear', 'linear', 'linear']
};

const tpLineChartConfig = {
    x: 'Time',
    charts: [{type: 'line', y: 'Count', fill: '#058DC7', style: {markRadius: 2}},
        {type: 'line', y: 'Mean Rate', fill: '#50B432', style: {markRadius: 2}},
        {type: 'line', y: 'M1 Rate', fill: '#f17b31', style: {markRadius: 2}},
        {type: 'line', y: 'M5 Rate', fill: '#8c51a5', style: {markRadius: 2}},
        {type: 'line', y: 'M15 Rate', fill: '#FFEB3B', style: {markRadius: 2}}
    ],
    width: 800,
    height: 250,
    style: {
        tickLabelColor:'#f2f2f2',
        legendTextColor: '#9c9898',
        legendTitleColor: '#9c9898',
        axisLabelColor: '#9c9898'
    },
    tipTimeFormat:"%Y-%m-%d %H:%M:%S %Z",
    legend:true,
    interactiveLegend: true,
    gridColor: '#f2f2f2',
    xAxisTickCount:10
};
/**
 * class which manages Siddhi App component history.
 */
export default class ComponentHistory extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            workerID: this.props.match.params.id.split("_")[0] + ":" + this.props.match.params.id.split("_")[1],
            statsEnable: this.props.match.params.isStatsEnabled,
            componentType: this.props.match.params.componentType,
            componentId: this.props.match.params.componentId,
            period: '5min',
            isApiWaiting: true,
            latency: [],
            memory: [],
            throughput: []
        };
        this.handleChange = this.handleChange.bind(this);
        this.handleApi = this.handleApi.bind(this);
        this.setColor = this.setColor.bind(this);
        this.renderCharts = this.renderCharts.bind(this);
    }

    handleChange(value) {
        this.setState({
            period: value,
            latency: [],
            throughput: [],
            memory: [],
            isApiWaiting: true
        });
        this.handleApi(value);
    }

    handleApi(value) {
        let queryParams = {
            params: {
                period: value
            }
        };
        let that = this;
        StatusDashboardAPIS.getComponentHistoryByID(this.props.match.params.id,
            this.props.match.params.appName, this.props.match.params.componentType,
            this.props.match.params.componentId, queryParams)
            .then(function (response) {
                if (that.props.match.params.componentType === ComponentType.QUERIES) {
                    that.setState({
                        latency: response.data.latency,
                        memory: response.data.memory,
                        isApiWaiting: false
                    });
                } else if (that.props.match.params.componentType === ComponentType.STREAMS) {
                    that.setState({
                        throughput: response.data.throughput,
                        isApiWaiting: false
                    });
                } else if (that.props.match.params.componentType === ComponentType.STORE_QUERIES) {
                    that.setState({
                        latency: response.data.latency,
                        isApiWaiting: false
                    });
                } else if (that.props.match.params.componentType === ComponentType.TRIGGER) {
                    that.setState({
                        throughput: response.data.throughput,
                        isApiWaiting: false
                    });
                } else if (that.props.match.params.componentType === ComponentType.TABLES) {
                    that.setState({
                        latency: response.data.latency,
                        memory: response.data.memory,
                        throughput: response.data.throughput,
                        isApiWaiting: false
                    });
                } else if (that.props.match.params.componentType === ComponentType.SOURCES) {
                    that.setState({
                        throughput: response.data.throughput,
                        isApiWaiting: false
                    });
                } else if (that.props.match.params.componentType === ComponentType.SINKS) {
                    that.setState({
                        throughput: response.data.throughput,
                        isApiWaiting: false
                    });
                } else if (that.props.match.params.componentType === ComponentType.SINK_MAPPERS) {
                    that.setState({
                        latency: response.data.latency,
                        isApiWaiting: false
                    });
                } else if (that.props.match.params.componentType === ComponentType.SOURCE_MAPPERS) {
                    that.setState({
                        latency: response.data.latency,
                        isApiWaiting: false
                    });
                }
            });
    }

    componentWillMount() {
        this.handleApi(this.state.period);
    }

    setColor(period) {
        return (this.state.period === period) ? '#f17b31' : '';
    }

    renderLatencyChart() {
        if (this.state.componentType === ComponentType.STREAMS || this.state.componentType === ComponentType.SOURCES ||
            this.state.componentType === ComponentType.SINKS || this.state.componentType === ComponentType.TRIGGER) {
            return <div/>;
        }
        else if ((this.state.componentType === ComponentType.QUERIES || this.state.componentType
            === ComponentType.STORE_QUERIES ||
            this.state.componentType === ComponentType.TABLES || this.state.componentType
            === ComponentType.SINK_MAPPERS ||
            this.state.componentType === ComponentType.SOURCE_MAPPERS) && this.state.latency.length === 0) {
            return (
                <Card><CardHeader title="Latency"/><Divider/>
                    <CardMedia>
                        <div style={{backgroundColor: '#131313'}}>
                            <h4 style={{marginTop: 0}}>No Data Available</h4>
                        </div>
                    </CardMedia>
                </Card>
            );
        }
        return (
            <ChartCard data={this.state.latency} metadata={latencyMetadata} config={latencyLineChartConfig}
                       title="Latency"/>
        );
    }

    renderMemoryChart() {
        if (this.state.componentType === ComponentType.STREAMS || this.state.componentType === ComponentType.TRIGGER ||
            this.state.componentType === ComponentType.STORE_QUERIES || this.state.componentType
            === ComponentType.SOURCES || this.state.componentType === ComponentType.SINKS
            || this.state.componentType === ComponentType.SOURCES || this.state.componentType
            === ComponentType.SINK_MAPPERS ||
            this.state.componentType === ComponentType.SOURCE_MAPPERS) {
            return <div/>;
        }
        else if ((this.state.componentType === ComponentType.QUERIES || this.state.componentType
            === ComponentType.TABLES) && this.state.memory.length === 0) {
            return (
                <Card><CardHeader title="Memory"/><Divider/>
                    <CardMedia>
                        <div style={{backgroundColor: '#131313'}}>
                            <h4 style={{marginTop: 0}}>No Data Available</h4>
                        </div>
                    </CardMedia>
                </Card>
            );
        }
        console.log(this.state.memory);
        console.log("**************");
        return (
            <ChartCard data={this.state.memory} metadata={memoryMetadata} config={memoryLineChartConfig}
                       title="Memory"/>
        );
    }

    renderThroughputChart() {
        if (this.state.componentType === ComponentType.STORE_QUERIES || this.state.componentType
            === ComponentType.QUERIES
            || this.state.componentType === ComponentType.SOURCE_MAPPERS || this.state.componentType
            === ComponentType.SINK_MAPPERS) {
            return <div/>;
        }
        else if ((this.state.componentType === ComponentType.STREAMS || this.state.componentType
            === ComponentType.TRIGGER
            || this.state.componentType === ComponentType.TABLES || this.state.componentType === ComponentType.SOURCES
            || this.state.componentType === ComponentType.SINKS) && this.state.throughput.length === 0) {
            return (
                <Card><CardHeader title="Throughput"/><Divider/>
                    <CardMedia>
                        <div style={{backgroundColor: '#131313'}}>
                            <h4 style={{marginTop: 0}}>No Data Available</h4>
                        </div>
                    </CardMedia>
                </Card>
            );
        }
        return (
            <ChartCard data={this.state.throughput} metadata={tpMetadata} config={tpLineChartConfig}
                       title="Throughput"/>
        );
    }

    renderCharts() {
        if (this.state.isApiWaiting) {
            return (
                <div style={{backgroundColor: '#222222', width: '100%', height: '100%'}} data-toggle="loading"
                     data-loading-inverse="true">
                    <div id="wrapper" style={{
                        backgroundColor: '#222222',
                        textAlign: 'center',
                        paddingTop: '200px',
                        paddingBottom: '200px'
                    }}>
                        <i className="fw fw-loader5 fw-spin fw-inverse fw-5x"></i>
                    </div>
                </div>
            );
        } else {
            return (
                <div style={{width: '90%', marginLeft: '10px', paddingTop: 60}}>
                    <div style={{padding: 30}}>
                        {this.renderLatencyChart()}
                    </div>
                    <div style={{padding: 30}}>
                        {this.renderMemoryChart()}
                    </div>
                    <div style={{padding: 30}}>
                        {this.renderThroughputChart()}
                    </div>
                </div>
            );
        }
    }

    render() {
        return (
            <div style={{backgroundColor: '#222222'}}>
                <Header/>
                <div className="navigation-bar">
                    <Link to={window.contextPath}><FlatButton label="Overview >"
                                                              icon={<HomeButton color="black"/>}/></Link>
                    <Link to={window.contextPath + '/worker/' + this.props.match.params.id }>
                        <FlatButton label={this.state.workerID + " >"}/></Link>
                    <Link
                        to={window.contextPath + '/worker/' + this.props.match.params.id + '/siddhi-apps/' +
                        this.props.match.params.appName + "/" + this.state.statsEnable}>
                        <FlatButton label={this.props.match.params.appName + " >"}/>
                    </Link>
                    <RaisedButton label={this.props.match.params.componentId} disabled disabledLabelColor='white'
                                  disabledBackgroundColor='#f17b31'/>
                </div>
                <div className="worker-h1">
                    <h2 style={{marginLeft: 40}}> {this.props.match.params.componentId} Metrics </h2>
                </div>
                <Toolbar style={toolBar}>
                    <ToolbarGroup firstChild={true}>
                        <RaisedButton label="Last 5 Minutes" backgroundColor={this.setColor('5min')}
                                      onClick={() => this.handleChange("5min")}
                                      style={styles.button}/>
                        <RaisedButton label="Last 1 Hour" backgroundColor={this.setColor('1hr')}
                                      onClick={() => this.handleChange("1hr")}
                                      style={styles.button}/>
                        <RaisedButton label="Last 6 Hours" backgroundColor={this.setColor('6hr')}
                                      onClick={() => this.handleChange("6hr")}
                                      style={styles.button}/>
                        <RaisedButton label="Last day" backgroundColor={this.setColor('24hr')}
                                      onClick={() => this.handleChange("24hr")}
                                      style={styles.button}/>
                    </ToolbarGroup>
                </Toolbar>
                {this.renderCharts()}
            </div>
        );
    }
}