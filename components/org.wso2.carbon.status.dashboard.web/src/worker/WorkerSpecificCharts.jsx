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

import React from 'react';
import {Link} from 'react-router-dom';
//App Components
import StatusDashboardAPIS from '../utils/apis/StatusDashboardAPIs';
import DashboardUtils from '../utils/DashboardUtils';
import VizG from 'react-vizgrammar';
//Material UI
import {CardMedia, CardTitle, Checkbox, GridList, GridTile, IconButton} from 'material-ui';
//Localization
import { FormattedMessage } from 'react-intl';

const loadMetadata = {names: ['Time', 'Load Average'], types: ['time', 'linear']};
const widgetHeight = 280;
const loadLineChartConfig = {
    x: 'Time',
    charts: [{type: 'line', y: 'Load Average', style: {markRadius: 2}}],
    gridColor: '#f2f2f2',
    tipTimeFormat: "%M:%S %Z",
    style: {
        axisLabelColor: '#9c9898',
        legendTitleColor: '#9c9898',
        legendTextColor: '#9c9898',
        tickLabelColor: '#f2f2f2',
    }
};
const tpMetadata = {names: ['Time', 'Throughput(events/second)'], types: ['time', 'linear']};
const tpLineChartConfig = {
    x: 'Time', charts: [{type: 'line', y: 'Throughput(events/second)', style: {markRadius: 2}}],
    gridColor: '#f2f2f2',
    tipTimeFormat: "%M:%S %Z",
    style: {
        tickLabelColor: '#f2f2f2',
        legendTextColor: '#9c9898',
        legendTitleColor: '#9c9898',
        axisLabelColor: '#9c9898'
    }
};
const cpuMetadata = {names: ['Time', 'System CPU', 'Process CPU'], types: ['time', 'linear', 'linear']};
const cpuLineChartConfig = {
    x: 'Time',
    charts: [{type: 'line', y: 'System CPU', fill: '#f17b31', style: {markRadius: 2}}, {
        type: 'line',
        fill: '#3366cc',
        y: 'Process CPU',
        style: {markRadius: 2}
    }],
    width: 100, height: 50,
    gridColor: '#f2f2f2',
    tipTimeFormat: "%M:%S %Z",
    style: {
        tickLabelColor: '#f2f2f2',
        legendTextColor: '#9c9898',
        legendTitleColor: '#9c9898',
        axisLabelColor: '#9c9898'
    }
};
const memoryMetadata = {names: ['Time', 'Used Memory', 'Total Memory'], types: ['time', 'linear', 'linear']};
const memoryLineChartConfig = {
    x: 'Time',
    charts: [{type: 'line', y: 'Used Memory', fill: '#f17b31', style: {markRadius: 2}}, {
        type: 'line', y: 'Total' +
        ' Memory', fill: '#3366cc', style: {markRadius: 2}
    }],
    width: 800,
    height: 330,
    gridColor: '#f2f2f2',
    tipTimeFormat: "%M:%S %Z",
    style: {
        tickLabelColor: '#f2f2f2',
        legendTextColor: '#9c9898',
        legendTitleColor: '#9c9898',
        axisLabelColor: '#9c9898'
    }
};
const loadHASendingMetadata = {names: ['Time', 'throughput'], types: ['time', 'linear']};
const loadHASendingChartConfig = {
    x: 'Time',
    charts: [{type: 'line', y: 'throughput', style: {markRadius: 2}}],
    gridColor: '#f2f2f2',
    tipTimeFormat: "%M:%S %Z",
    style: {
        axisLabelColor: '#9c9898',
        legendTitleColor: '#9c9898',
        legendTextColor: '#9c9898',
        tickLabelColor: '#f2f2f2',
    }
};
const loadHAReceivingMetadata = {names: ['Time', 'throughput'], types: ['time', 'linear']};
const loadHAReceivingChartConfig = {
    x: 'Time',
    charts: [{type: 'line', y: 'throughput', style: {markRadius: 2}}],
    gridColor: '#f2f2f2',
    tipTimeFormat: "%M:%S %Z",
    style: {
        axisLabelColor: '#9c9898',
        legendTitleColor: '#9c9898',
        legendTextColor: '#9c9898',
        tickLabelColor: '#f2f2f2',
    }
};
const styles = {
    root: {display: 'flex', flexWrap: 'wrap', justifyContent: 'space-around'},
    gridList: {height: '50%', overflowY: 'auto', padding: '0 10px', margin: '-10px 0'}
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
            receivingThroughput: [],
            sendingThroughput: [],
            workerId: this.props.id,
            sysCpuChecked: true,
            processCpuChecked: true,
            totalMemoryChecked: true,
            usedMemoryChecked: true,
            isHADeployment: false,
            haStatus: true
        }
    }

    componentWillMount() {
        let queryParams = {
            params: {
                period: '5min'
            }
        };
        let that = this;

        StatusDashboardAPIS.getHAWorkerDetailsByID(this.state.workerId, queryParams)
            .then((response) => {
                if (response.data.haStatus) {
                    that.setState({
                        isHADeployment: true,
                        haStatus: response.data.haStatus
                    });
                }
            });

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

        StatusDashboardAPIS.getHAWorkerHistoryByID(this.state.workerId, queryParams)
            .then((response) => {
                that.setState({
                    receivingThroughput: response.data.receivingThroughput.data,
                    sendingThroughput: response.data.sendingThroughput.data
                });
            });
    }

    renderCpuChart() {
        let data, config, metadata, yLimit;
        if (this.state.sysCpuChecked && this.state.processCpuChecked) {
            data = DashboardUtils.getCombinedChartList(this.state.systemCpu, this.state.processCpu);
            config = cpuLineChartConfig;
            metadata = cpuMetadata;
        } else if (this.state.sysCpuChecked) {
            data = this.state.systemCpu;
            config = {
                x: 'Time',
                charts: [{type: 'line', fill: '#f17b31', y: 'System CPU', style: {markRadius: 2}}],
                gridColor: '#f2f2f2',
                tipTimeFormat: "%M:%S %Z",
                style: {
                    tickLabelColor: '#f2f2f2',
                    legendTextColor: '#9c9898',
                    legendTitleColor: '#9c9898',
                    axisLabelColor: '#9c9898'
                }
            };
            metadata = {names: ['Time', 'System CPU'], types: ['time', 'linear']};
        } else if (this.state.processCpuChecked) {
            data = this.state.processCpu;
            config = {
                x: 'Time', charts: [{type: 'line', fill: '#3366cc', y: 'Process CPU', style: {markRadius: 2}}],
                gridColor: '#f2f2f2',
                tipTimeFormat: "%M:%S %Z",
                style: {
                    tickLabelColor: '#f2f2f2',
                    legendTextColor: '#9c9898',
                    legendTitleColor: '#9c9898',
                    axisLabelColor: '#9c9898'
                }
            };
            metadata = {names: ['Time', 'Process CPU'], types: ['time', 'linear']};
        } else {
            data = [];
            config = {
                x: 'Time', charts: [{type: 'line', y: 'value', style: {markRadius: 2}}],
                gridColor: '#f2f2f2',
                tipTimeFormat: "%M:%S %Z",
                style: {
                    tickLabelColor: '#f2f2f2',
                    legendTextColor: '#9c9898',
                    legendTitleColor: '#9c9898',
                    axisLabelColor: '#9c9898'
                }
            };
            metadata = {names: ['Time', 'value'], types: ['time', 'linear']};
        }


        if (this.state.systemCpu.length === 0 && this.state.processCpu.length === 0) {
            return (
                <GridTile className="container" title={<FormattedMessage id='workerSpecific.cpuUsage' defaultMessage='CPU Usage' />} titlePosition="top" titleBackground='#303030' >
                    <div style={{
                        marginTop: 50,
                        backgroundColor: '#131313',
                        padding: 30,
                        textAlign: 'center',
                        height: widgetHeight,
                        color: '#303030'
                    }}><h2><FormattedMessage id='noData' defaultMessage='No Data Available' /></h2></div>
                </GridTile >
            );
        }
        else {
            yLimit = DashboardUtils.initCombinedYDomain(this.state.systemCpu, this.state.processCpu);
        }
        return (
            <GridTile className="container" title={<FormattedMessage id='workerSpecific.cpuUsage' defaultMessage='CPU Usage' />} titlePosition="top" titleBackground='#303030'>
                <div className="overlay" style={{ color: '#303030', paddingTop: 40, textAlign: 'right' }}>
                    <h3><FormattedMessage id='clickForMore' defaultMessage='Click for more details' /></h3>
                </div>
                <div style={{
                    display: 'flex',
                    flexDirection: 'row',
                    paddingTop: 57,
                    backgroundColor: '#131313',
                    paddingLeft: 30
                }}>
                    <div>
                        <Checkbox
                            label="System CPU"
                            onCheck={(e, checked) => this.setState({sysCpuChecked: checked})}
                            checked={this.state.sysCpuChecked}
                            iconStyle={{fill: '#f17b31'}}
                            style={{width: 150, fontSize: 12, zIndex: 1}}
                        />
                    </div>
                    <div>
                        <Checkbox
                            label="Process CPU"
                            onCheck={(e, checked) => this.setState({processCpuChecked: checked})}
                            checked={this.state.processCpuChecked}
                            iconStyle={{fill: '#3366cc'}}
                            style={{width: 150, fontSize: 12, zIndex: 1}}
                        />
                    </div>
                </div>
                <Link key="cpu" to={window.contextPath + '/worker/history/' + this.state.workerId}>
                    <div style={{backgroundColor: '#131313', height: widgetHeight}}>
                       <VizG
                            data={data}
                            metadata={metadata} config={config}
                            yDomain={[yLimit[0], yLimit[1]]}
                            width={560}
                            height={240}
                        />
                    </div>
                </Link>
            </GridTile>
        );
    }

    renderMemoryChart() {
        let data, config, metadata, yLimit;
        if (this.state.usedMemoryChecked && this.state.totalMemoryChecked) {
            data = DashboardUtils.getCombinedChartList(this.state.usedMem, this.state.totalMem);
            config = memoryLineChartConfig;
            metadata = memoryMetadata;
        } else if (this.state.totalMemoryChecked) {
            data = this.state.totalMem;
            config = {
                x: 'Time', charts: [{type: 'line', fill: '#3366cc', y: 'Total Memory', style: {markRadius: 2}}],
                gridColor: '#f2f2f2',
                tipTimeFormat: "%M:%S %Z",
                style: {
                    tickLabelColor: '#f2f2f2',
                    legendTextColor: '#9c9898',
                    legendTitleColor: '#9c9898',
                    axisLabelColor: '#9c9898'
                }


            };
            metadata = {names: ['Time', 'Total Memory'], types: ['time', 'linear']};
        } else if (this.state.usedMemoryChecked) {
            data = this.state.usedMem;
            config = {
                x: 'Time', charts: [{type: 'line', fill: '#f17b31', y: 'Used Memory', style: {markRadius: 2}}],
                gridColor: '#f2f2f2',
                tipTimeFormat: "%M:%S %Z",
                style: {
                    tickLabelColor: '#f2f2f2',
                    legendTextColor: '#9c9898',
                    legendTitleColor: '#9c9898',
                    axisLabelColor: '#9c9898'
                }

            };
            metadata = {names: ['Time', 'Used Memory'], types: ['time', 'linear']};
        } else {
            data = [];
            config = {
                x: 'Time', charts: [{type: 'line', y: 'value', style: {markRadius: 2}}],
                gridColor: '#f2f2f2',
                tipTimeFormat: "%M:%S %Z",
                style: {
                    tickLabelColor: '#f2f2f2',
                    legendTextColor: '#9c9898',
                    legendTitleColor: '#9c9898',
                    axisLabelColor: '#9c9898'
                }

            };
            metadata = {names: ['Time', 'value'], types: ['time', 'linear']};
        }

        if (this.state.usedMem.length === 0 && this.state.totalMem.length === 0) {
            return (
                <GridTile className="container" title={<FormattedMessage id='workerSpecific.memoryUsage' defaultMessage='Memory Usage' />} titlePosition="top" titleBackground='#303030' >
                    <div style={{
                        marginTop: 50,
                        color: '#303030',
                        backgroundColor: '#131313',
                        padding: 30,
                        textAlign: 'center',
                        height: widgetHeight
                    }}><h2><FormattedMessage id='noData' defaultMessage='No Data Available' /></h2></div>
                </GridTile >
            );
        }
        else {
            yLimit = DashboardUtils.initCombinedYDomain(this.state.usedMem, this.state.totalMem);
        }

        return (
            <GridTile className="container" title={<FormattedMessage id='workerSpecific.memoryUsed' defaultMessage='Memory Used(bytes)' />} titlePosition="top" titleBackground='#303030'>
                <div className="overlay" style={{ color: '#303030', paddingTop: 40, textAlign: 'right' }}>
                    <h3><FormattedMessage id='clickForMore' defaultMessage='Click for more details' /></h3>
                </div>
                <div style={{
                    display: 'flex',
                    flexDirection: 'row',
                    paddingTop: 57,
                    backgroundColor: '#131313',
                    paddingLeft: 30
                }}>
                    <div>
                        <Checkbox
                            label={<FormattedMessage id='workerSpecific.usedMemory' defaultMessage='Used Memory' />}
                            onCheck={(e, checked) => this.setState({usedMemoryChecked: checked})}
                            checked={this.state.usedMemoryChecked}
                            iconStyle={{fill: '#f17b31'}}
                            style={{width: 150, fontSize: 12, zIndex: 1}}
                        />
                    </div>
                    <div>
                        <Checkbox
                            label={<FormattedMessage id='workerSpecific.totalMemory' defaultMessage='Total Memory' />}
                            onCheck={(e, checked) => this.setState({totalMemoryChecked: checked})}
                            checked={this.state.totalMemoryChecked}
                            iconStyle={{fill: '#3366cc'}}
                            style={{width: 150, fontSize: 12, zIndex: 1}}
                        />
                    </div>
                </div>
                <Link key="memory" to={window.contextPath + '/worker/history/' + this.state.workerId}>
                    <div style={{ backgroundColor: '#131313', height: widgetHeight }}>
                        <VizG
                            data={data}
                            metadata={metadata} config={config}
                            yDomain={[yLimit[0], yLimit[1]]}
                            width={560}
                            height={240}
                        />
                    </div>
                </Link>
            </GridTile>
        );
    }

    renderLoadAverageChart() {
        let yLimit;
        if (this.state.loadAvg.length === 0) {
            return (
                <GridTile title={<FormattedMessage id='workerSpecific.systemLoad' defaultMessage='System Load Average' />} titlePosition="top" titleBackground='#303030'>
                    <div style={{
                        marginTop: 50,
                        color: '#303030',
                        backgroundColor: '#131313',
                        padding: 30,
                        textAlign: 'center',
                        height: widgetHeight
                    }}><h2><FormattedMessage id='noData' defaultMessage='No Data Available' /></h2></div>
                </GridTile>
            );
        }
        else {
            yLimit = DashboardUtils.getYDomain(this.state.loadAvg);
        }
        return (
            <GridTile className="container" title={<FormattedMessage id='workerSpecific.systemLoad' defaultMessage='System Load Average' />} titlePosition="top" titleBackground='#303030'>
                <div className="overlay" style={{ color: '#303030', paddingTop: 20, textAlign: 'right' }}>
                    <h3><FormattedMessage id='clickForMore' defaultMessage='Click for more details' /></h3>
                </div>
                <Link key="loadAverage"
                      to={window.contextPath + '/worker/history/' + this.state.workerId}>
                    <div style={{backgroundColor: '#131313', paddingTop: 70, width: '100%', height: widgetHeight}}>
                        <VizG data={this.state.loadAvg}
                              metadata={loadMetadata}
                              config={loadLineChartConfig}
                              yDomain={[yLimit[0], yLimit[1]]}
                              width={560}
                              height={240}
                        />
                    </div>
                </Link>
            </GridTile>
        );
    }

    renderThroughputChart() {
        let yLimit;
        if (this.state.throughputAll.length === 0) {
            return (
                <GridTile className="container" title={<FormattedMessage id='workerSpecific.overallThorughput' defaultMessage='Overall Throughput(events/second)' />} titlePosition="top"
                    titleBackground='#303030'>
                    <div style={{
                        marginTop: 50,
                        color: '#303030',
                        backgroundColor: '#131313',
                        paddingTop: 30,
                        textAlign: 'center',
                        height: widgetHeight
                    }}><h2><FormattedMessage id='noData' defaultMessage='No Data Available' /></h2></div>
                </GridTile>
            );
        }
        else {
            yLimit = DashboardUtils.getYDomain(this.state.throughputAll);
        }
        return (

            <GridTile className="container" title={<FormattedMessage id='workerSpecific.overallThorughput' defaultMessage='Overall Throughput(events/second)' />} titlePosition="top"
                titleBackground='#303030'>
                <div className="overlay" style={{ color: '#303030', paddingTop: 20, textAlign: 'right' }}>
                    <h3><FormattedMessage id='clickForMore' defaultMessage='Click for more details9' /></h3>
                </div>
                <Link key="throughput" to={window.contextPath + '/worker/history/' + this.state.workerId}>
                    <div style={{backgroundColor: '#131313', paddingTop: 70, height: widgetHeight}}>
                            <VizG data={this.state.throughputAll}
                                  metadata={tpMetadata}
                                  config={tpLineChartConfig}
                                  yDomain={[yLimit[0], yLimit[1]]}
                                  width={560}
                                  height={240}
                            />
                        </div>
                </Link>
            </GridTile>
        );
    }

    renderHASyncingReceivingThroughputChart() {
        if (this.state.isHADeployment) {
            let yLimit;
            if (this.state.receivingThroughput.length === 0) {
                return (
                    <GridTile title={<FormattedMessage id='workerSpecific.haSyncingReceivingThroughput' defaultMessage='HA Event Syncing Receiving TPS' />} titlePosition="top" titleBackground='#303030'>
                        <div style={{
                            marginTop: 50,
                            color: '#303030',
                            backgroundColor: '#131313',
                            padding: 30,
                            textAlign: 'center',
                            height: widgetHeight
                        }}><h2><FormattedMessage id='noData' defaultMessage='No Data Available' /></h2></div>
                    </GridTile>
                );
            }
            else {
                yLimit = DashboardUtils.getYDomain(this.state.receivingThroughput);
            }
            return (
                <GridTile className="container" title={<FormattedMessage id='workerSpecific.haSyncingReceivingThroughput' defaultMessage='HA Event Syncing Receiving TPS' />} titlePosition="top" titleBackground='#303030'>
                    <div style={{backgroundColor: '#131313', paddingTop: 10, height: widgetHeight}}>
                        <div style={{backgroundColor: '#131313', paddingTop: 60, width: '100%'}}>
                            <VizG data={this.state.receivingThroughput}
                                  metadata={loadHAReceivingMetadata}
                                  config={loadHAReceivingChartConfig}
                                  yDomain={[yLimit[0], yLimit[1]]}
                                  width={550}
                                  height={255}
                            />
                        </div>
                    </div>
                </GridTile>
            );
        }
    }

    renderHASyncingSendingThroughputChart() {
        if (this.state.isHADeployment) {
            let yLimit;
            if (this.state.sendingThroughput.length === 0) {
                return (
                    <GridTile title={<FormattedMessage id='workerSpecific.haSyncingSendingThroughput' defaultMessage='HA Event Syncing Publishing TPS' />} titlePosition="top" titleBackground='#303030'>
                        <div style={{
                            marginTop: 50,
                            color: '#303030',
                            backgroundColor: '#131313',
                            padding: 30,
                            textAlign: 'center',
                            height: 370
                        }}><h2><FormattedMessage id='noData' defaultMessage='No Data Available' /></h2></div>
                    </GridTile>
                );
            }
            else {
                yLimit = DashboardUtils.getYDomain(this.state.sendingThroughput);
            }
            return (
                <GridTile className="container" title={<FormattedMessage id='workerSpecific.haSyncingSendingThroughput' defaultMessage='HA Event Syncing Publishing TPS' />} titlePosition="top" titleBackground='#303030'>
                    <div className="overlay" style={{ color: '#303030', paddingTop: 20, textAlign: 'right' }}>
                        <h3><FormattedMessage id='clickForMore' defaultMessage='Click for more details' /></h3>
                    </div>
                    <div style={{backgroundColor: '#131313', paddingTop: 10, widgetHeight}}>
                        <div style={{backgroundColor: '#131313', paddingTop: 60, width: '100%'}}>
                            <VizG data={this.state.sendingThroughput}
                                  metadata={loadHASendingMetadata}
                                  config={loadHASendingChartConfig}
                                  yDomain={[yLimit[0], yLimit[1]]}
                                  width={550}
                                  height={255}
                            />
                        </div>
                    </div>
                </GridTile>
            );
        }
    }

    render() {
        if (!this.state.isHADeployment) {
            return (
                <div style={{width: '70%', float: 'right', boxSizing: 'border-box'}}>
                    <GridList cols={2} padding={20} cellHeight={350} style={styles.gridList} deployment={this.state.isHADeployment}>
                        {this.renderCpuChart()}
                        {this.renderMemoryChart()}
                        {this.renderLoadAverageChart()}
                        {this.renderThroughputChart()}
                    </GridList>
                </div>
            );
        } else if (this.state.haStatus === 'Active'){
            return (
                <div style={{width: '70%', float: 'right', boxSizing: 'border-box'}}>
                    <GridList cols={2} padding={20} cellHeight={350} style={styles.gridList} deployment={this.state.isHADeployment}>
                        {this.renderCpuChart()}
                        {this.renderMemoryChart()}
                        {this.renderLoadAverageChart()}
                        {this.renderThroughputChart()}
                        {this.renderHASyncingSendingThroughputChart()}
                    </GridList>
                </div>
            );
        } else {
            return (
                <div style={{width: '70%', float: 'right', boxSizing: 'border-box'}}>
                    <GridList cols={2} padding={20} cellHeight={350} style={styles.gridList} deployment={this.state.isHADeployment}>
                        {this.renderCpuChart()}
                        {this.renderMemoryChart()}
                        {this.renderLoadAverageChart()}
                        {this.renderThroughputChart()}
                        {this.renderHASyncingReceivingThroughputChart()}
                    </GridList>
                </div>
            );
        }
    }
}


