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
//Material UI
import HomeButton from "material-ui/svg-icons/action/home";
import {Card, CardHeader, CardMedia, CardText, CardTitle, Divider, FlatButton, Toggle} from "material-ui";
import {Toolbar, ToolbarGroup} from "material-ui/Toolbar";
import RaisedButton from "material-ui/RaisedButton";

const styles = {
    button: {margin: 12, backgroundColor: '#f17b31'}
};
const memoryMetadata = {names: ['Time', 'Memory'], types: ['time', 'linear']};

const latencyMetadata = {names: ['Time', 'Latency'], types: ['time', 'linear']};

const tpMetadata = {names: ['Time', 'Throughput'], types: ['time', 'linear']};

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
            tickCount: 20
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

    renderLatencyChart(){
        this.setState({
            tickCount: this.state.latency.length>20 ? 20 : this.state.latency.length
        });
        const latencyLineChartConfig = {
            x: 'Time',
            charts: [{type: 'area', y: 'Latency', fill: '#f17b31', markRadius: 2}],
            width: 800,
            height: 250,
            legend:true,
            interactiveLegend: true,
            gridColor: 'white',
            xAxisTickCount:this.state.tickCount,
            style: {
                tickLabelColor:'white',
                legendTextColor: '#9c9898',
                legendTitleColor: '#9c9898',
                axisLabelColor: '#9c9898',
            }
        };
        if(this.state.latency.length === 0) {
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
        return(
            <ChartCard data={this.state.latency} metadata={latencyMetadata} config={latencyLineChartConfig}
                       title="Latency"/>
        );
    }
    renderMemoryChart(){
        this.setState({
            tickCount: this.state.memory.length>20 ? 20 : this.state.memory.length
        });
        const memoryLineChartConfig = {
            x: 'Time',
            charts: [{type: 'area', y: 'Memory', fill: '#f17b31', markRadius: 2}],
            width: 800,
            height: 250,
            legend:true,
            interactiveLegend: true,
            gridColor: 'white',
            xAxisTickCount:this.state.tickCount,
            style: {
                tickLabelColor:'white',
                legendTextColor: '#9c9898',
                legendTitleColor: '#9c9898',
                axisLabelColor: '#9c9898',
            }
        };
        if(this.state.memory.length === 0) {
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
        return(
            <ChartCard data={this.state.memory} metadata={memoryMetadata} config={memoryLineChartConfig}
                       title="Memory Usage"/>
        );
    }
    renderThroughputChart(){
        this.setState({
            tickCount: this.state.throughputAll.length>20 ? 20 : this.state.throughputAll.length
        });
        const tpLineChartConfig = {
            x: 'Time',
            charts: [{type: 'area', y: 'Throughput', fill: '#f17b31', markRadius: 2}],
            width: 800,
            height: 250,
            legend:true,
            interactiveLegend: true,
            gridColor: 'white',
            xAxisTickCount:this.state.tickCount,
            style: {
                tickLabelColor:'white',
                legendTextColor: '#9c9898',
                legendTitleColor: '#9c9898',
                axisLabelColor: '#9c9898',
            }
        };
        if(this.state.throughputAll.length === 0) {
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
        return(
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
                <div style={{width: '90%', marginLeft: '10px'}}>
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

    setColor(period) {
        return (this.state.period === period) ? '#f17b31' : '';
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
                        this.props.match.params.appName + '/' + this.state.statsEnable}>
                        <FlatButton label={this.props.match.params.appName + " >"}/></Link>
                    <RaisedButton label= "Metrics" disabled disabledLabelColor='white'
                                  disabledBackgroundColor='#f17b31'/>
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

