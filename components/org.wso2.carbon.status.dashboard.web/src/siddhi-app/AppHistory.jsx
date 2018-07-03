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

import React from 'react';
import {Link, Redirect} from 'react-router-dom';
//App Components
import StatusDashboardAPIS from '../utils/apis/StatusDashboardAPIs';
import ChartCard from '../common/ChartCard';
import Header from '../common/Header';
//Material UI
import HomeButton from 'material-ui/svg-icons/action/home';
import {Card, CardHeader, CardMedia, Divider} from 'material-ui';
import {Button, Typography} from 'material-ui-next';
import {Toolbar, ToolbarGroup} from 'material-ui/Toolbar';
import RaisedButton from 'material-ui/RaisedButton';
import AuthenticationAPI from '../utils/apis/AuthenticationAPI';
import AuthManager from '../auth/utils/AuthManager';
import Error403 from '../error-pages/Error403';

const styles = {
    navBar: {padding: '0 15px'},
    navBtn: {color: '#BDBDBD', padding: '0 10px', verticalAlign: 'middle', textTransform: 'capitalize'},
    navBtnActive: {color: '#f17b31', display: 'inline-block', verticalAlign: 'middle', textTransform: 'capitalize',
        padding: '0 10px'},
    titleStyle: {fontSize: '1.6rem', margin: '20px 0 0 24px', color: '#dedede'},
    button: {margin: 0, fontSize: 10, borderLeft: '1px solid #4c4c4c', borderRadius: 0}
};

const memoryMetadata = {names: ['Time', 'Memory(bytes)'], types: ['time', 'linear']};

const latencyMetadata = {names: ['Time', 'Latency(milliseconds)'], types: ['time', 'linear']};

const tpMetadata = {names: ['Time', 'Throughput(events/second)'], types: ['time', 'linear']};

const toolBar = {position: 'absolute', top: 85, right: 15, padding: 0, backgroundColor: 'transparent'};

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
            statsEnable: this.props.match.params.isStatsEnabled,
            sessionInvalid: false,
            hasViewerPermission: true,
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
            tickCountTp: 10,
            tickCountLt: 10,
            tickCountMem: 10
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
                    isApiWaiting: false,
                    tickCountTp: (response.data[0].throughput.data.length > 20) ? 10 :
                        response.data[0].throughput.data.length,
                    tickCountLt: (response.data[0].latency.data.length > 20) ? 10 :
                        response.data[0].latency.data.length,
                    tickCountMem: (response.data[0].memory.data.length > 20) ? 10 :
                        response.data[0].memory.data.length,
                });
            }).catch((error) => {
            let message;
            if (error.response != null) {
                if (error.response.status === 401) {
                    message = "Authentication fail. Please login again.";
                    this.setState({
                        sessionInvalid: true
                    })
                } else if (error.response.status === 403) {
                    message = "User Have No Viewer Permission to view this page.";
                    this.setState({
                        hasViewerPermission: false
                    })
                } else {
                    message = "Unknown error occurred! : " + error.response.data;
                }
            }
        });
    }

    componentWillMount() {
        AuthenticationAPI.isUserAuthorized('viewer', AuthManager.getUser().SDID)
            .then((response) => {
                that.setState({
                    hasViewerPermission: response.data
                });
            }).catch((error) => {
            let message;
            if (error.response != null) {
                if (error.response.status === 401) {
                    message = "Authentication fail. Please login again.";
                    this.setState({
                        sessionInvalid: true
                    })
                } else if (error.response.status === 403) {
                    message = "User Have No Viewer Permission to view this page.";
                    this.setState({
                        hasViewerPermission: false
                    })
                } else {
                    message = "Unknown error occurred! : " + error.response.data;
                }
            }
        });
        this.handleApi(this.state.period);
    }

    renderLatencyChart() {

        const latencyLineChartConfig = {
            x: 'Time',
            charts: [{type: 'area', y: 'Latency(milliseconds)', fill: '#f17b31', style: {markRadius: 2}}],
            width: 800,
            height: 250,
            legend: true,
            interactiveLegend: true,
            gridColor: 'white',
            xAxistickCountTp: this.state.tickCountLt,
            tipTimeFormat: "%Y-%m-%d %H:%M:%S %Z",
            style: {
                tickLabelColor: 'white',
                legendTextColor: '#9c9898',
                legendTitleColor: '#9c9898',
                axisLabelColor: '#9c9898',
                legendTextSize: 10,
                legendTitleSize: 12
            }
        };
        if (this.state.latency.length === 0) {
            return (
                <Card><CardHeader title="Latency"/><Divider/>
                    <CardMedia>
                        <div style={{backgroundColor: '#131313'}}>
                            <h2>No Data Available</h2>
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
        const memoryLineChartConfig = {
            x: 'Time',
            charts: [{type: 'area', y: 'Memory(bytes)', fill: '#f17b31', style: {markRadius: 2}}],
            width: 800,
            height: 250,
            legend: true,
            interactiveLegend: true,
            gridColor: 'white',
            xAxistickCountTp: this.state.tickCountMem,
            tipTimeFormat: "%Y-%m-%d %H:%M:%S %Z",
            style: {
                tickLabelColor: 'white',
                legendTextColor: '#9c9898',
                legendTitleColor: '#9c9898',
                axisLabelColor: '#9c9898',
                legendTextSize: 10,
                legendTitleSize: 12
            }
        };
        if (this.state.memory.length === 0) {
            return (
                <Card><CardHeader title="Memory Usage"/><Divider/>
                    <CardMedia>
                        <div style={{backgroundColor: '#131313'}}>
                            <h2>No Data Available</h2>
                        </div>
                    </CardMedia>
                </Card>
            );
        }
        return (
            <ChartCard data={this.state.memory} metadata={memoryMetadata} config={memoryLineChartConfig}
                       title="Memory Usage"/>
        );
    }

    renderThroughputChart() {

        const tpLineChartConfig = {
            x: 'Time',
            charts: [{type: 'area', y: 'Throughput(events/second)', fill: '#f17b31', style: {markRadius: 2}}],
            width: 800,
            height: 250,
            legend: true,
            interactiveLegend: true,
            gridColor: 'white',
            xAxistickCountTp: this.state.tickCountTp,
            tipTimeFormat: "%Y-%m-%d %H:%M:%S %Z",
            style: {
                tickLabelColor: 'white',
                legendTextColor: '#9c9898',
                legendTitleColor: '#9c9898',
                axisLabelColor: '#9c9898',
                legendTextSize: 10,
                legendTitleSize: 12
            }
        };
        if (this.state.throughputAll.length === 0) {
            return (
                <Card><CardHeader title="Throughput"/><Divider/>
                    <CardMedia>
                        <div style={{backgroundColor: '#131313'}}>
                            <h2>No Data Available</h2>
                        </div>
                    </CardMedia>
                </Card>
            );
        }
        return (
            <ChartCard data={this.state.throughputAll} metadata={tpMetadata} config={tpLineChartConfig}
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
                <div style={{padding: '30px 24px'}}>
                    {this.renderLatencyChart()}
                    {this.renderMemoryChart()}
                    {this.renderThroughputChart()}
                </div>
            );
        }
    }

    setColor(period) {
        return (this.state.period === period) ? '#f17b31' : '';
    }

    render() {
        if (this.state.sessionInvalid) {
            return (
                <Redirect to={{pathname: `${window.contextPath}/logout`}}/>
            );
        }
        if (this.state.hasViewerPermission) {
            return (
                <div style={{backgroundColor: '#222222'}}>
                    <Header/>
                    <div style={styles.navBar} className="navigation-bar">
                        <Link style={{textDecoration: 'none'}} to={window.contextPath}>
                            <Button style={styles.navBtn}>
                                <HomeButton style={{paddingRight: 8, color: '#BDBDBD'}}/>
                                Overview >
                            </Button>
                        </Link>
                        <Link style={{textDecoration: 'none'}} to={window.contextPath + '/worker/' +
                        this.props.match.params.id}>
                            <Button style={styles.navBtn}>
                                {this.state.workerID} >
                            </Button>
                        </Link>
                        <Link style={{textDecoration: 'none'}} to={window.contextPath + '/worker/' +
                        this.props.match.params.id + '/siddhi-apps/' + this.props.match.params.appName + '/' +
                        this.state.statsEnable}>
                            <Button style={styles.navBtn}>
                                {this.props.match.params.appName} >
                            </Button>
                        </Link>
                        <Typography style={styles.navBtnActive}>Metrics</Typography>
                    </div>
                    <Typography variant="title" style={styles.titleStyle}>
                        {this.state.workerID} : {this.state.appName} Metrics
                    </Typography>
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
                            <RaisedButton label="Last Week" backgroundColor={this.setColor('1wk')}
                                          onClick={() => this.handleChange("1wk")}
                                          style={styles.button}/>
                        </ToolbarGroup>
                    </Toolbar>
                    {this.renderCharts()}
                </div>
            );
        } else {
            return <Error403/>;
        }
    }
}

