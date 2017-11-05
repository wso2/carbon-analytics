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
//Material UI
import HomeButton from "material-ui/svg-icons/action/home";
import {
    Card,
    CardHeader,
    CardMedia,
    CardText,
    CardTitle,
    Divider,
    FlatButton,
    FloatingActionButton,
    IconButton,
    Toggle
} from "material-ui";
import {Toolbar, ToolbarGroup} from "material-ui/Toolbar";
import RaisedButton from "material-ui/RaisedButton";

const styles = {
    button: {margin: 12, backgroundColor: '#f17b31'},
    chart: {marginTop: 50, backgroundColor: 'black', textAlign: 'center', height: 300}
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
const toolBar = {width: '50%', marginLeft: '50%', padding: 20, backgroundColor: '#424242'};

/**
 * class which manages Siddhi App history details.
 */
export default class AppSpecific extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            workerID: this.props.match.params.id.split("_")[0] + ":" + this.props.match.params.id.split("_")[1],
            latency: [],
            memory: [],
            throughputAll: [],
            appName: this.props.match.params.appName,
            period: '5min',
            isApiWaiting: true,
            statsEnable: this.props.match.params.isStatsEnabled
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
            throughputAll: [],
            memory: [],
            isApiWaiting: true,
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
        StatusDashboardAPIS.getSiddhiAppHistoryByID(this.props.match.params.id,
            this.props.match.params.appName, queryParams)
            .then(function (response) {
                that.setState({
                    latency: response.data[0].latency.data,
                    throughputAll: response.data[0].throughput.data,
                    memory: response.data[0].memory.data,
                    isApiWaiting: false
                });
            });
    }

    componentWillMount() {
        this.handleApi(this.state.period);
    }

    renderCharts() {
        if (this.state.isApiWaiting) {
            return (
                <div style={{width: '90%', marginLeft: '5%'}}>
                    <Card >
                        <CardHeader title="CPU Usage"/>
                        <Divider/>
                        <CardMedia>
                            <div style={styles.chart}>
                                <i className="fw fw-sync fw-spin fw-inverse fw-5x"></i>
                            </div>
                        </CardMedia>
                    </Card>
                    <Card >
                        <CardHeader title="Memory Usage"/>
                        <Divider/>
                        <CardMedia>
                            <div style={styles.chart}>
                                <i className="fw fw-sync fw-spin fw-inverse fw-5x"></i>
                            </div>
                        </CardMedia>
                    </Card>
                    <Card >
                        <CardHeader title="Load Average"/>
                        <Divider/>
                        <CardMedia>
                            <div style={styles.chart}>
                                <i className="fw fw-sync fw-spin fw-inverse fw-5x"></i>
                            </div>
                        </CardMedia>
                    </Card>
                    <Card >
                        <CardHeader title="Throughput"/>
                        <Divider/>
                        <CardMedia>
                            <div style={styles.chart}>
                                <i className="fw fw-sync fw-spin fw-inverse fw-5x"></i>
                            </div>
                        </CardMedia>
                    </Card>
                </div>
            );
        } else {
            return (
                <div style={{width: '90%', marginLeft: '10px'}}>
                    <div style={{padding: 30}}>
                        <ChartCard data={this.state.latency} metadata={latencyMetadata} config={latencyLineChartConfig}
                                   title="Latency"/>
                    </div>
                    <div style={{padding: 30}}>
                        <ChartCard data={this.state.memory} metadata={memoryMetadata} config={memoryLineChartConfig}
                                   title="Memory Usage"/>
                    </div>
                    <div style={{padding: 30}}>
                        <ChartCard data={this.state.throughputAll} metadata={tpMetadata} config={tpLineChartConfig}
                                   title="Throughput"/>
                    </div>
                </div>
            );
        }
    }

    getYDomain(arr, padding) {
        let values = arr.map(function (elt) {
            return elt[1];
        });
        let max = Math.max.apply(null, values);
        let min = Math.min.apply(null, values);
        return [min - padding, max + padding];
    }

    setColor(period) {
        return (this.state.period === period) ? '#f17b31' : '';
    }

    render() {
        return (
            <div style={{backgroundColor: '#222222'}}>
                <div className="navigation-bar">
                    <Link to="/sp-status-dashboard/overview"><FlatButton label="Overview >"
                                                                         icon={<HomeButton color="black"/>}/></Link>
                    <Link to={"/sp-status-dashboard/worker/" + this.props.match.params.id }>
                        <FlatButton label={this.state.workerID + " >"}/></Link>
                    <Link
                        to={"/sp-status-dashboard/worker/" + this.props.match.params.id + "/siddhi-apps/" +
                        this.props.match.params.appName + "/" + this.state.statsEnable}>
                        <FlatButton label={this.props.match.params.appName + " >"}/></Link>
                    <FlatButton label="Metrics"/>
                </div>
                <div className="worker-h1">
                    <h2 style={{marginLeft: 40}}> {this.state.workerID} : {this.state.appName} Metrics </h2>
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

