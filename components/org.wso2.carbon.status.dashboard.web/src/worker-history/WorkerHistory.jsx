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
/**
 * class which manages worker specific details.
 */
import React from "react";
import {Link} from "react-router-dom";
//App Components
import StatusDashboardAPIS from "../utils/apis/StatusDashboardAPIs";
import ChartCard from "../common/ChartCard";
import DashboardUtils from "../utils/DashboardUtils";
//Material UI
import RaisedButton from "material-ui/RaisedButton";
import Checkbox from "material-ui/Checkbox";
import {Toolbar, ToolbarGroup} from "material-ui/Toolbar";
import HomeButton from "material-ui/svg-icons/action/home";
import {Card, CardHeader, CardMedia, Divider, FlatButton} from "material-ui";

const styles = {button: {margin: 12, backgroundColor: '#f17b31'}};
const cpuMetadata = {names: ['timestamp', 'system cpu', 'process cpu'], types: ['time', 'linear', 'linear']};
const cpuLineChartConfig = {
    x: 'timestamp',
    charts: [{type: 'line', y: 'system cpu', fill: '#f17b31'}, {type: 'line', y: 'process cpu'}],
    width: 700,
    height: 200,
    tickLabelColor: '#9c9898',
    axisLabelColor: '#9c9898'
};
const memoryMetadata = {names: ['timestamp', 'used memory', 'total memory'], types: ['time', 'linear', 'linear']};
const memoryLineChartConfig = {
    x: 'timestamp',
    charts: [{type: 'line', y: 'used memory', fill: '#f17b31'}, {type: 'line', y: 'total memory'}],
    width: 800,
    height: 250,
    tickLabelColor: '#9c9898',
    axisLabelColor: '#9c9898'
};

/**
 * class which manages worker history details.
 */
export default class WorkerHistory extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            workerID: this.props.match.params.id.split("_")[0] + ":" + this.props.match.params.id.split("_")[1],
            systemCpu: [],
            processCpu: [],
            usedMem: [],
            totalMem: [],
            loadAvg: [],
            throughputAll: [],
            period: '5min',
            sysCpuChecked: true,
            processCpuChecked: true,
            totalMemoryChecked: true,
            usedMemoryChecked: true,
            isApiWaiting: true

        };
        this.handleChange = this.handleChange.bind(this);
        this.handleApi = this.handleApi.bind(this);
        this.renderCpuChart = this.renderCpuChart.bind(this);
        this.renderMemoryChart = this.renderMemoryChart.bind(this);
    }


    handleChange(value) {
        this.setState({
            period: value,
            systemCpu: [],
            processCpu: [],
            throughputAll: [],
            usedMem: [],
            totalMem: [],
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
        StatusDashboardAPIS.getWorkerHistoryByID(this.props.match.params.id, queryParams)
            .then(function (response) {
                that.setState({
                    systemCpu: response.data.systemCPU.data,
                    processCpu: response.data.processCPU.data,
                    usedMem: response.data.usedMemory.data,
                    totalMem: response.data.totalMemory.data,
                    loadAvg: response.data.loadAverage.data,
                    throughputAll: response.data.throughput.data,
                    isApiWaiting: false
                });
            })
    }

    componentWillMount() {
        this.handleApi(this.state.period);
    }

    setColor(period) {
        return (this.state.period === period) ? '#f17b31' : '';
    }

    renderCpuChart() {
        let data, config, metadata;
        if (this.state.sysCpuChecked && this.state.processCpuChecked) {
            data = DashboardUtils.getCombinedChartList(this.state.systemCpu, this.state.processCpu);
            config = cpuLineChartConfig;
            metadata = cpuMetadata;
        } else if (this.state.sysCpuChecked) {
            data = this.state.systemCpu;
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'system cpu'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'system cpu'], types: ['time', 'linear']};
        } else if (this.state.processCpuChecked) {
            data = this.state.processCpu;
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'process cpu'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'process cpu'], types: ['time', 'linear']};
        } else {
            data = [];
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'value'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'value'], types: ['time', 'linear']};
        }

        return (
            <div>
                <div style={{display: 'flex', flexDirection: 'row', paddingTop: 50}}>
                    <div>
                        <Checkbox
                            label="System CPU"
                            onCheck={(e, checked) => this.setState({sysCpuChecked: checked})}
                            checked={this.state.sysCpuChecked}
                            iconStyle={{fill: '#f17b31'}}
                        />
                    </div>
                    <div>
                        <Checkbox
                            label="Process CPU"
                            onCheck={(e, checked) => this.setState({processCpuChecked: checked})}
                            checked={this.state.processCpuChecked}
                            iconStyle={{fill: '#f17b31'}}
                        />
                    </div>
                </div>
                <div>
                    <ChartCard data={data} metadata={metadata} config={config} title="CPU Usage"/>
                </div>
            </div>
        );
    }

    renderMemoryChart() {
        let data, config, metadata;
        if (this.state.usedMemoryChecked && this.state.totalMemoryChecked) {
            data = DashboardUtils.getCombinedChartList(this.state.usedMem, this.state.totalMem);
            config = memoryLineChartConfig;
            metadata = memoryMetadata;
        } else if (this.state.totalMemoryChecked) {
            data = this.state.totalMem;
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'total memory'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'total memory'], types: ['time', 'linear']};
        } else if (this.state.usedMemoryChecked) {
            data = this.state.usedMem;
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'used memory'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'used memory'], types: ['time', 'linear']};
        } else {
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'value'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'value'], types: ['time', 'linear']};
            return <div><ChartCard metadata={metadata} config={config} title="Memory Usage"/></div>
        }

        return (
            <div>
                <div style={{display: 'flex', flexDirection: 'row', paddingTop: 50}}>
                    <div>
                        <Checkbox
                            label="Used Memory"
                            onCheck={(e, checked) => this.setState({usedMemoryChecked: checked})}
                            checked={this.state.usedMemoryChecked}
                            iconStyle={{fill: '#f17b31'}}
                        />
                    </div>
                    <div>
                        <Checkbox
                            label="Total Memory"
                            onCheck={(e, checked) => this.setState({totalMemoryChecked: checked})}
                            checked={this.state.totalMemoryChecked}
                            iconStyle={{fill: '#f17b31'}}
                        />
                    </div>
                </div>
                <div>
                    <ChartCard data={data} metadata={metadata} config={config} title="Memory Usage"/>
                </div>
            </div>
        );
    }

    renderCharts() {
        if (this.state.isApiWaiting) {
            return (
                <div style={{width: '90%', marginLeft: '5%'}}>
                    <Card >
                        <CardHeader title="CPU Usage"/>
                        <Divider/>
                        <CardMedia>
                            <div style={{
                                marginTop: 50,
                                backgroundColor: '#131313',
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
                                backgroundColor: '#131313',
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
                                backgroundColor: '#131313',
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
                                backgroundColor: '#131313',
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
                        {this.renderCpuChart()}
                    </div>

                    <div style={{padding: 30}}>
                        {this.renderMemoryChart()}
                    </div>

                    <div style={{padding: 30}}>
                        <ChartCard data={this.state.loadAvg} metadata={{
                            names: ['timestamp', 'load average'],
                            types: ['time', 'linear']
                        }} config={{
                            x: 'timestamp', charts: [{type: 'line', y: 'load average'}], width: 800, height: 250,
                            tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
                        }}
                                   title="Load Average"/>
                    </div>

                    <div style={{padding: 30}}>
                        <ChartCard data={this.state.throughputAll} metadata={{
                            names: ['timestamp', 'throughput'],
                            types: ['time', 'linear']
                        }} config={{
                            x: 'timestamp', charts: [{type: 'line', y: 'throughput'}], width: 800, height: 250,
                            tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
                        }}
                                   title="Throughput"/>
                    </div>

                    <div style={{marginLeft: '89%'}}>
                        <Link to={"/sp-status-dashboard/worker/history/" + this.props.match.params.id + "/more"}>
                            <RaisedButton label="More Details" style={styles.button}
                                          backgroundColor='#f17b31'/>
                        </Link>
                    </div>
                </div>
            );
        }
    }

    render() {
        return (
            <div style={{backgroundColor: '#222222'}}>
                <div className="navigation-bar">
                    <Link to="/sp-status-dashboard/overview"><FlatButton label="Overview >"
                                                                         icon={<HomeButton color="black"/>}/>
                    </Link>
                    <Link to={"/sp-status-dashboard/worker/" + this.props.match.params.id }>
                        <FlatButton label={this.state.workerID + " >"}/></Link>
                    <FlatButton label="Metrics"/>
                </div>
                <div className="worker-h1">
                    <h2 style={{marginLeft: 20}}> {this.state.workerID} Metrics </h2>
                </div>
                <Toolbar style={{width: '50%', marginLeft: '50%', padding: 20, backgroundColor: '#424242'}}>
                    <ToolbarGroup firstChild={true}>
                        <RaisedButton label="Last 5 Minutes" backgroundColor={this.setColor('5min')}
                                      onClick={() => this.handleChange('5min')}
                                      style={styles.button}/>
                        <RaisedButton label="Last 1 Hour" backgroundColor={this.setColor('1hr')}
                                      onClick={() => this.handleChange('1hr')}
                                      style={styles.button}/>
                        <RaisedButton label="Last 6 Hours" backgroundColor={this.setColor('6hr')}
                                      onClick={() => this.handleChange('6hr')}
                                      style={styles.button}/>
                        <RaisedButton label="Last day" backgroundColor={this.setColor('24hr')}
                                      onClick={() => this.handleChange('24hr')} style={styles.button}/>
                    </ToolbarGroup>
                </Toolbar>
                {this.renderCharts()}
            </div>
        );
    }
}
