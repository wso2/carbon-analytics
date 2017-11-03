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

const styles = {button: {margin: 12, backgroundColor: '#f17b31',}};

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
            isApiWaiting: true
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
        StatusDashboardAPIS.getSiddhiAppHistoryByID(this.props.match.params.id, this.props.match.params.appName, queryParams)
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
        let metadata = {
            names: ['timestamp', 'value'],
            types: ['time', 'linear']
        };

        let lineChartConfig = {
            x: 'timestamp',
            charts: [{type: 'line', y: 'value', fill: '#f17b31'}],
            width: 800,
            height: 250,
            tickLabelColor: 'white',
            axisLabelColor: 'white'
        };
        if (this.state.isApiWaiting) {
            return (
                <div style={{width: '90%', marginLeft: '5%'}}>
                    <Card >
                        <CardHeader title="CPU Usage"/>
                        <Divider/>
                        <CardMedia>
                            <div style={{
                                marginTop: 50,
                                backgroundColor: 'black',
                                textAlign: 'center',
                                height: 300
                            }}>
                                <i className="fw fw-sync fw-spin fw-inverse fw-5x"></i>
                            </div>
                        </CardMedia>
                    </Card>
                    <Card >
                        <CardHeader title="Memory Usage"/>
                        <Divider/>
                        <CardMedia>
                            <div style={{
                                marginTop: 50,
                                backgroundColor: 'black',
                                textAlign: 'center',
                                height: 300
                            }}>
                                <i className="fw fw-sync fw-spin fw-inverse fw-5x"></i>
                            </div>
                        </CardMedia>
                    </Card>
                    <Card >
                        <CardHeader title="Load Average"/>
                        <Divider/>
                        <CardMedia>
                            <div style={{
                                marginTop: 50,
                                backgroundColor: 'black',
                                textAlign: 'center',
                                height: 300
                            }}>
                                <i className="fw fw-sync fw-spin fw-inverse fw-5x"></i>
                            </div>
                        </CardMedia>
                    </Card>
                    <Card >
                        <CardHeader title="Throughput"/>
                        <Divider/>
                        <CardMedia>
                            <div style={{
                                marginTop: 50,
                                backgroundColor: 'black',
                                textAlign: 'center',
                                height: 300
                            }}>
                                <i className="fw fw-sync fw-spin fw-inverse fw-5x"></i>
                            </div>
                        </CardMedia>
                    </Card>
                </div>
            );
        } else {
            return (
                <div style={{width: '90%', marginLeft: '5%'}}>
                    <div style={{padding: 30}}>
                        <ChartCard data={this.state.latency} metadata={metadata} config={lineChartConfig}
                                   title="Latency"/>
                    </div>
                    <div style={{padding: 30}}>
                        <ChartCard data={this.state.memory} metadata={metadata} config={lineChartConfig}
                                   title="Memory Usage"/>
                    </div>
                    <div style={{padding: 30}}>
                        <ChartCard data={this.state.throughputAll} metadata={metadata} config={lineChartConfig}
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
                        to={"/sp-status-dashboard/worker/" + this.props.match.params.id + "/siddhi-apps/" + this.props.match.params.appName }>
                        <FlatButton label={this.props.match.params.appName + " >"}/></Link>
                    <FlatButton label="Metrics"/>
                </div>
                <div className="worker-h1">
                    <h2 style={{marginLeft: 40}}> {this.state.workerID} : {this.state.appName} Metrics </h2>
                </div>
                <Toolbar style={{width: '50%', marginLeft: '50%', padding: 20, backgroundColor: 'grey'}}>
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

