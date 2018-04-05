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
 *  Unless requi#f2f2f2 by applicable law or agreed to in writing,
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
import DashboardUtils from "../utils/DashboardUtils";
import VizG from 'react-vizgrammar';
//Material UI
import {CardMedia, CardTitle, Checkbox, GridList, GridTile, IconButton} from "material-ui";

const loadMetadata = {names: ['Time', 'Load Average'], types: ['time', 'linear']};
const loadLineChartConfig = {
    x: 'Time',
    charts: [{type: 'line', y: 'Load Average',style: {markRadius: 2}}],
    gridColor: '#f2f2f2',
    tipTimeFormat:"%M:%S %Z",
    style: {
        axisLabelColor: '#9c9898',
        legendTitleColor: '#9c9898',
        legendTextColor: '#9c9898',
        tickLabelColor:'#f2f2f2',
    }
};
const tpMetadata = {names: ['Time', 'Throughput(events/second)'], types: ['time', 'linear']};
const tpLineChartConfig = {
    x: 'Time', charts: [{type: 'line', y: 'Throughput(events/second)',style: {markRadius: 2}}],
    gridColor: '#f2f2f2',
    tipTimeFormat:"%M:%S %Z",
    style: {
        tickLabelColor:'#f2f2f2',
        legendTextColor: '#9c9898',
        legendTitleColor: '#9c9898',
        axisLabelColor: '#9c9898'
    }
};
const cpuMetadata = {names: ['Time', 'System CPU', 'Process CPU'], types: ['time', 'linear', 'linear']};
const cpuLineChartConfig = {
    x: 'Time',
    charts: [{type: 'line', y: 'System CPU', fill: '#f17b31',style: {markRadius: 2}}, {type: 'line', fill: '#3366cc', y: 'Process CPU',style: {markRadius: 2}}],
    width: 100, height: 50,
    gridColor: '#f2f2f2',
    tipTimeFormat:"%M:%S %Z",
    style: {
        tickLabelColor:'#f2f2f2',
        legendTextColor: '#9c9898',
        legendTitleColor: '#9c9898',
        axisLabelColor: '#9c9898'
    }
};
const memoryMetadata = {names: ['Time', 'Used Memory', 'Total Memory'], types: ['time', 'linear', 'linear']};
const memoryLineChartConfig = {
    x: 'Time',
    charts: [{type: 'line', y: 'Used Memory', fill: '#f17b31',style: {markRadius: 2}}, {type: 'line', y: 'Total' +
    ' Memory',fill: '#3366cc',style: {markRadius: 2}}],
    width: 800,
    height: 330,
    gridColor: '#f2f2f2',
    tipTimeFormat:"%M:%S %Z",
    style: {
        tickLabelColor:'#f2f2f2',
        legendTextColor: '#9c9898',
        legendTitleColor: '#9c9898',
        axisLabelColor: '#9c9898'
    }
};
const styles = {
    root: {display: 'flex', flexWrap: 'wrap', justifyContent: 'space-around'},
    gridList: {width: '95%', height: '50%', overflowY: 'auto', paddingLeft: 30}
};

/**
 * Worker specific chart component.
 */
export default class WorkerSpecificCharts extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            processCpu: [],
            systemCpu: [],
            totalMem: [],
            usedMem: [],
            loadAvg: [],
            throughputAll: [],
            workerId: this.props.id,
            sysCpuChecked: true,
            processCpuChecked: true,
            totalMemoryChecked: true,
            usedMemoryChecked: true
        }
    }

    componentWillMount() {
        let queryParams = {
            params: {
                period: '5min'
            }
        };
        let that = this;
        StatusDashboardAPIS.getWorkerHistoryByID(this.state.workerId, queryParams)
            .then((response) => {
                that.setState({
                    processCpu: response.data.processCPU.data,
                    systemCpu: response.data.systemCPU.data,
                    totalMem: response.data.totalMemory.data,
                    usedMem: response.data.usedMemory.data,
                    loadAvg: response.data.loadAverage.data,
                    throughputAll: response.data.throughput.data
                });
            });
    }

    renderCpuChart() {
        let data, config, metadata,yLimit;
        if (this.state.sysCpuChecked && this.state.processCpuChecked) {
            data = DashboardUtils.getCombinedChartList(this.state.systemCpu, this.state.processCpu);
            config = cpuLineChartConfig;
            metadata = cpuMetadata;
        } else if (this.state.sysCpuChecked) {
            data = this.state.systemCpu;
            config = {
                x: 'Time', charts: [{type: 'line', fill: '#f17b31', y: 'System CPU',style: {markRadius: 2}}],  width: 100, height: 50,
                gridColor: '#f2f2f2',
                tipTimeFormat:"%M:%S %Z",
                style: {
                    tickLabelColor:'#f2f2f2',
                    legendTextColor: '#9c9898',
                    legendTitleColor: '#9c9898',
                    axisLabelColor: '#9c9898'
                }
            };
            metadata = {names: ['Time', 'System CPU'], types: ['time', 'linear']};
        } else if (this.state.processCpuChecked) {
            data = this.state.processCpu;
            config = {
                x: 'Time', charts: [{type: 'line',fill: '#3366cc', y: 'Process CPU',style: {markRadius: 2}}],
                gridColor: '#f2f2f2',
                tipTimeFormat:"%M:%S %Z",
                style: {
                    tickLabelColor:'#f2f2f2',
                    legendTextColor: '#9c9898',
                    legendTitleColor: '#9c9898',
                    axisLabelColor: '#9c9898'
                }
            };
            metadata = {names: ['Time', 'Process CPU'], types: ['time', 'linear']};
        } else {
            data = [];
            config = {
                x: 'Time', charts: [{type: 'line', y: 'value',style: {markRadius: 2}}],
                gridColor: '#f2f2f2',
                tipTimeFormat:"%M:%S %Z",
                style: {
                    tickLabelColor:'#f2f2f2',
                    legendTextColor: '#9c9898',
                    legendTitleColor: '#9c9898',
                    axisLabelColor: '#9c9898'
                }
            };
            metadata = {names: ['Time', 'value'], types: ['time', 'linear']};
        }


        if (this.state.systemCpu.length === 0 && this.state.processCpu.length === 0) {
            return (
                <GridTile className="container" title="CPU Usage" titlePosition="top" titleBackground='#303030'>
                    <div style={{
                        marginTop: 50,
                        backgroundColor: '#131313',
                        padding: 30,
                        textAlign: 'center',
                        height: 370,
                        color: '#303030'
                    }}><h2>No Data Available</h2></div>
                </GridTile>
            );
        } 
        else {
            yLimit = DashboardUtils.initCombinedYDomain(this.state.systemCpu, this.state.processCpu);
            console.log(yLimit)
        }
        return (
            <GridTile className="container" title="CPU Usage" titlePosition="top" titleBackground='#303030'>
                <div className="overlay" style={{color: '#303030', paddingTop: 40, textAlign: 'right'}}>
                    <h3>Click for more details</h3>
                </div>
                <div style={{
                    display: 'flex',
                    flexDirection: 'row',
                    paddingTop: 50,
                    backgroundColor: '#131313',
                    paddingLeft: 30
                }}>
                    <div>
                        <Checkbox
                            label="System CPU"
                            onCheck={(e, checked) => this.setState({sysCpuChecked: checked})}
                            checked={this.state.sysCpuChecked}
                            iconStyle={{fill: '#f17b31'}}
                            style={{width: 150, fontSize: 12, zIndex:1}}
                        />
                    </div>
                    <div>
                        <Checkbox
                            label="Process CPU"
                            onCheck={(e, checked) => this.setState({processCpuChecked: checked})}
                            checked={this.state.processCpuChecked}
                            iconStyle={{fill: '#3366cc'}}
                            style={{width: 150, fontSize: 12, zIndex:1}}
                        />
                    </div>
                </div>
                <Link key="cpu" to={window.contextPath + '/worker/history/' + this.state.workerId }>
                    <div style={{backgroundColor: '#131313', paddingTop: 18, height: 370}}>
                        <div style={{backgroundColor: '#131313', height: 200, width: '100%'}}>
                        <VizG
                            data={data}
                            metadata={metadata} config={config}
                            yDomain={[yLimit[0],yLimit[1]]}
                            width={590}
                            height={230}
                        />
                        </div>
                    </div>
                </Link>
            </GridTile>
        );
    }

    renderMemoryChart() {
        let data, config, metadata,yLimit;
        if (this.state.usedMemoryChecked && this.state.totalMemoryChecked) {
            data = DashboardUtils.getCombinedChartList(this.state.usedMem, this.state.totalMem);
            config = memoryLineChartConfig;
            metadata = memoryMetadata;
        } else if (this.state.totalMemoryChecked) {
            data = this.state.totalMem;
            config = {
                x: 'Time', charts: [{type: 'line',fill: '#3366cc', y: 'Total Memory',style: {markRadius: 2}}],
                gridColor: '#f2f2f2',
                tipTimeFormat:"%M:%S %Z",
                style: {
                    tickLabelColor:'#f2f2f2',
                    legendTextColor: '#9c9898',
                    legendTitleColor: '#9c9898',
                    axisLabelColor: '#9c9898'
                }


            };
            metadata = {names: ['Time', 'Total Memory'], types: ['time', 'linear']};
        } else if (this.state.usedMemoryChecked) {
            data = this.state.usedMem;
            config = {
                x: 'Time', charts: [{type: 'line', fill: '#f17b31', y: 'Used Memory',style: {markRadius: 2}}],
                gridColor: '#f2f2f2',
                tipTimeFormat:"%M:%S %Z",
                style: {
                    tickLabelColor:'#f2f2f2',
                    legendTextColor: '#9c9898',
                    legendTitleColor: '#9c9898',
                    axisLabelColor: '#9c9898'
                }

            };
            metadata = {names: ['Time', 'Used Memory'], types: ['time', 'linear']};
        } else {
            data = [];
            config = {
                x: 'Time', charts: [{type: 'line', y: 'value',style: {markRadius: 2}}],
                gridColor: '#f2f2f2',
                tipTimeFormat:"%M:%S %Z",
                style: {
                    tickLabelColor:'#f2f2f2',
                    legendTextColor: '#9c9898',
                    legendTitleColor: '#9c9898',
                    axisLabelColor: '#9c9898'
                }

            };
            metadata = {names: ['Time', 'value'], types: ['time', 'linear']};
        }

        if (this.state.usedMem.length === 0 && this.state.totalMem.length === 0) {
            return (
                <GridTile className="container" title="Memory Usage" titlePosition="top" titleBackground='#303030'>
                    <div style={{
                        marginTop: 50,
                        color: '#303030',
                        backgroundColor: '#131313',
                        padding: 30,
                        textAlign: 'center',
                        height: 370
                    }}><h2>No Data Available</h2></div>
                </GridTile>
            );
        } 
        else {
            yLimit = DashboardUtils.initCombinedYDomain(this.state.usedMem, this.state.totalMem);
        }

        return (
            <GridTile className="container" title="Memory Used(bytes)" titlePosition="top" titleBackground='#303030'>
                <div className="overlay" style={{color: '#303030', paddingTop: 40, textAlign: 'right'}}>
                    <h3>Click for more details</h3>
                </div>
                <div style={{
                    display: 'flex',
                    flexDirection: 'row',
                    paddingTop: 50,
                    backgroundColor: '#131313',
                    paddingLeft: 30
                }}>
                    <div>
                        <Checkbox
                            label="Used Memory"
                            onCheck={(e, checked) => this.setState({usedMemoryChecked: checked})}
                            checked={this.state.usedMemoryChecked}
                            iconStyle={{fill: '#f17b31'}}
                            style={{width: 150, fontSize: 12, zIndex:1}}
                        />
                    </div>
                    <div>
                        <Checkbox
                            label="Total Memory"
                            onCheck={(e, checked) => this.setState({totalMemoryChecked: checked})}
                            checked={this.state.totalMemoryChecked}
                            iconStyle={{fill: '#3366cc'}}
                            style={{width: 150, fontSize: 12, zIndex:1}}
                        />
                    </div>
                </div>
                <Link key="memory" to={window.contextPath + '/worker/history/' + this.state.workerId }>
                    <div style={{backgroundColor: '#131313', paddingTop: 18, height: '370px'}}>
                        <div style={{backgroundColor: '#131313', height: 200, width: '100%'}}>
                        <VizG
                            data={data}
                            metadata={metadata} config={config}
                            yDomain={[yLimit[0],yLimit[1]]}
                            width={590}
                            height={230}
                        />
                        </div>
                    </div>
                </Link>
            </GridTile>
        );
    }

    renderLoadAverageChart() {
        let yLimit;
        if (this.state.loadAvg.length === 0) {
            return (
                <GridTile title="System Load Average" titlePosition="top" titleBackground='#303030'>
                    <div style={{
                        marginTop: 50,
                        color: '#303030',
                        backgroundColor: '#131313',
                        padding: 30,
                        textAlign: 'center',
                        height: 370
                    }}><h2>No Data Available</h2></div>
                </GridTile>
            );
        } 
        else {
            yLimit = DashboardUtils.getYDomain(this.state.loadAvg);
        }
        return (
            <GridTile className="container" title="System Load Average" titlePosition="top" titleBackground='#303030'>
                <div className="overlay" style={{color: '#303030', paddingTop: 20, textAlign: 'right'}}>
                    <h3>Click for more details</h3>
                </div>
                <Link key="loadAverage"
                      to={window.contextPath +'/worker/history/' + this.state.workerId}>
                    <div style={{backgroundColor: '#131313', paddingTop: 10, height: '370px'}}>
                        <div style={{backgroundColor: '#131313', paddingTop: 60, height: 255, width: '100%'}}>
                        <VizG data={this.state.loadAvg}
                              metadata={loadMetadata}
                              config={loadLineChartConfig}
                              yDomain={[yLimit[0],yLimit[1]]}
                                width={550}
                                height={255}
                        />
                        </div>
                    </div>
                </Link>
            </GridTile>
        );
    }

    renderThroughputChart() {
        let yLimit;
        if (this.state.throughputAll.length === 0) {
            return (
                <GridTile className="container" title="Overall Throughput(events/second)" titlePosition="top"
                          titleBackground='#303030'>
                    <div style={{
                        marginTop: 50,
                        color: '#303030',
                        backgroundColor: '#131313',
                        paddingTop: 30,
                        textAlign: 'center',
                        height: 370
                    }}><h2>No Data Available</h2></div>
                </GridTile>
            );
        } 
        else {
            yLimit = DashboardUtils.getYDomain(this.state.throughputAll);
        }
        return (

            <GridTile className="container" title="Overall Throughput(events/second)" titlePosition="top" titleBackground='#303030'>
                <div className="overlay" style={{color: '#303030', paddingTop: 20, textAlign: 'right'}}>
                    <h3>Click for more details</h3>
                </div>
                <Link key="throughput" to={window.contextPath + '/worker/history/' + this.state.workerId }>
                    <div style={{backgroundColor: '#131313', paddingTop: 10, height: '370px'}}>
                        <div style={{backgroundColor: '#131313', paddingTop: 60, height: 255, width: '100%'}}>
                        <VizG data={this.state.throughputAll}
                              metadata={tpMetadata}
                              config={tpLineChartConfig}
                              yDomain={[yLimit[0],yLimit[1]]}
                              width={550}
                              height={255}
                        />
                        </div>
                    </div>
                </Link>
            </GridTile>
        );
    }

    render() {
        return (
            <div style={{width: '70%', float: 'right', boxSizing: 'border-box'}}>
                <GridList cols={2} padding={20} cellHeight={320} style={styles.gridList}>
                    {this.renderCpuChart()}
                    {this.renderMemoryChart()}
                    {this.renderLoadAverageChart()}
                    {this.renderThroughputChart()}
                </GridList>
            </div>
        );
    }
}


