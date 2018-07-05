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
import {ComponentType} from '../utils/Constants';
//Material UI
import {Toolbar, ToolbarGroup} from 'material-ui/Toolbar';
import HomeButton from 'material-ui/svg-icons/action/home';
import {Card, CardHeader, CardMedia, Divider, FlatButton, RaisedButton} from 'material-ui';
import {Button, Typography} from 'material-ui-next';
import AuthenticationAPI from '../utils/apis/AuthenticationAPI';
import AuthManager from '../auth/utils/AuthManager';
import Error403 from '../error-pages/Error403';

const styles = {
    navBar: {padding: '0 15px'},
    navBtn: {color: '#BDBDBD', padding: '0 10px', verticalAlign: 'middle', textTransform: 'capitalize'},
    navBtnActive: {color: '#f17b31', display: 'inline-block', verticalAlign: 'middle', textTransform: 'capitalize',
        padding: '0 10px'},
    titleStyle: {fontSize: '1.6rem', margin: '20px 0 0 24px', color: '#dedede', textTransform: 'capitalize'},
    button: {margin: 0, fontSize: 10, borderLeft: '1px solid #4c4c4c', borderRadius: 0}
};
const toolBar = {position: 'absolute', top: 85, right: 15, padding: 0, backgroundColor: 'transparent'};

const latencyMetadata = {
    names: ['Time', 'Max', 'Mean', 'Min', 'Standard Deviation', '75th Percentile', '95th Percentile',
        '99th Percentile', '99.9th Percentile', 'Mean Rate', '1 Minute Rate', '5 Minutes Rate', '15 Minutes Rate'],
    types: ['time', 'linear', 'linear', 'linear', 'linear', 'linear', 'linear', 'linear', 'linear', 'linear', 'linear',
        'linear', 'linear']
};
const latencyLineChartConfig = {
    x: 'Time',
    charts: [
        {type: 'area', y: 'Max', fill: '#50B432', style: {markRadius: 2}},
        {type: 'area', y: 'Mean', fill: '#f17b31', style: {markRadius: 2}},
        {type: 'area', y: 'Min', fill: '#8c51a5', style: {markRadius: 2}},
        {type: 'area', y: 'Standard Deviation', fill: '#FFEB3B', style: {markRadius: 2}},
        {type: 'area', y: '75th Percentile', fill: '#70dbed', style: {markRadius: 2}},
        {type: 'area', y: '95th Percentile', fill: '#ffb873', style: {markRadius: 2}},
        {type: 'area', y: '99th Percentile', fill: '#95dd87', style: {markRadius: 2}},
        {type: 'area', y: '99.9th Percentile', fill: '#890f02', style: {markRadius: 2}},
        {type: 'area', y: 'Mean Rate', fill: '#ff918f', style: {markRadius: 2}},
        {type: 'area', y: '1 Minute Rate', fill: '#b76969', style: {markRadius: 2}},
        {type: 'area', y: '5 Minutes Rate', fill: '#aea2e0', style: {markRadius: 2}},
        {type: 'area', y: '15 Minutes Rate', fill: '#FFEB3B', style: {markRadius: 2}}
    ],
    width: 800,
    height: 250,
    style: {
        tickLabelColor: '#f2f2f2',
        legendTextColor: '#9c9898',
        legendTitleColor: '#9c9898',
        axisLabelColor: '#9c9898',
        legendTextSize: 8,
        legendTitleSize: 8
    },
    tipTimeFormat: "%Y-%m-%d %H:%M:%S %Z",
    legend: true,
    interactiveLegend: true,
    gridColor: '#f2f2f2',
    xAxisTickCount: 10
};
const memoryMetadata = {names: ['Time', 'Memory'], types: ['time', 'linear']};
const memoryLineChartConfig = {
    x: 'Time',
    charts: [{type: 'area', y: 'Memory', fill: '#f17b31', style: {markRadius: 2}}],
    width: 800,
    height: 250,
    style: {
        tickLabelColor: '#f2f2f2',
        legendTextColor: '#9c9898',
        legendTitleColor: '#9c9898',
        axisLabelColor: '#9c9898',
        legendTextSize: 10,
        legendTitleSize: 12
    },
    tipTimeFormat: "%Y-%m-%d %H:%M:%S %Z",
    legend: true,
    interactiveLegend: true,
    gridColor: '#f2f2f2',
    xAxisTickCount: 10
};
const tpMetadata = {
    names: ['Time', 'Mean', '1 Minute', '5 Minutes', '15 Minutes'],
    types: ['time', 'linear', 'linear', 'linear', 'linear']
};

const tpLineChartConfig = {
    x: 'Time',
    charts: [
        {type: 'area', y: 'Mean', fill: '#50B432', style: {markRadius: 2}},
        {type: 'area', y: '1 Minute', fill: '#f17b31', style: {markRadius: 2}},
        {type: 'area', y: '5 Minutes', fill: '#8c51a5', style: {markRadius: 2}},
        {type: 'area', y: '15 Minutes', fill: '#FFEB3B', style: {markRadius: 2}}
    ],
    width: 800,
    height: 250,
    style: {
        tickLabelColor: '#f2f2f2',
        legendTextColor: '#9c9898',
        legendTitleColor: '#9c9898',
        axisLabelColor: '#9c9898',
        legendTextSize: 10,
        legendTitleSize: 12
    },
    tipTimeFormat: "%Y-%m-%d %H:%M:%S %Z",
    legend: true,
    interactiveLegend: true,
    gridColor: '#f2f2f2',
    xAxisTickCount: 10
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
            throughput: [],
            hasViewerPermission: true,
            sessionInvalid: false,
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
                <Card><CardHeader title="Latency(milliseconds)"/><Divider/>
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
                       title="Latency(milliseconds)"/>
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
                <Card><CardHeader title="Memory(bytes)"/><Divider/>
                    <CardMedia>
                        <div style={{backgroundColor: '#131313'}}>
                            <h4 style={{marginTop: 0}}>No Data Available</h4>
                        </div>
                    </CardMedia>
                </Card>
            );
        }
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
        else if ((this.state.componentType === ComponentType.STREAMS ||
                this.state.componentType === ComponentType.TRIGGER
                || this.state.componentType === ComponentType.TABLES ||
                this.state.componentType === ComponentType.SOURCES
                || this.state.componentType === ComponentType.SINKS) && this.state.throughput.length === 0) {
            return (
                <Card><CardHeader title="Throughput(events/second)"/><Divider/>
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
                <div style={{padding: '30px 24px'}}>
                    {this.renderLatencyChart()}
                    {this.renderMemoryChart()}
                    {this.renderThroughputChart()}
                </div>
            );
        }
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
                        <Typography style={styles.navBtnActive}>{this.props.match.params.componentId}</Typography>
                    </div>
                    <Typography variant="title" style={styles.titleStyle}>
                        {this.props.match.params.componentId} Metrics
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
                            <RaisedButton label="Last Day" backgroundColor={this.setColor('24hr')}
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