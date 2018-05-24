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
//Material UI
import {FlatButton, GridList, GridTile, RaisedButton} from 'material-ui';
import HomeButton from 'material-ui/svg-icons/action/home';
//App Components
import StatusDashboardAPIS from '../utils/apis/StatusDashboardAPIs';
import JVMLoading from './JVMClassLoading';
import JVMOsPhysicalMemory from './JVMOsPhysicalMemory';
import JVMThread from './JVMThread';
import HeapMemory from './HeapMemory';
import NonHeapMemory from './NonHeapMemory';
import FileDescriptor from './FileDescriptor';
import Header from '../common/Header';
import JVMSwap from './JVMSwap';
import JVMGarbageCOllector from './JVMGarbageCOllector';
import JVMOsLoad from './JVMOsLoad';
import JVMLoad from './JVMLoad';
import JVMOTotalMemory from './JVMTotalMemory';

const cardStyle = {padding: 30, width: '90%'};
/**
 * class to manage worker history details.
 */
export default class WorkerHistoryMore extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            workerID: this.props.match.params.id.split("_")[0] + ":" + this.props.match.params.id.split("_")[1],
            jvmClassLoadingLoadedCurrent: [],
            jvmClassLoadingLoadedTotal: [],
            jvmClassLoadingUnloadedTotal: [],
            jvmGcPsMarksweepCount: [],
            jvmGcPsMarksweepTime: [],
            jvmGcPsScavengeCount: [],
            jvmGcPsScavengeTime: [],
            jvmMemoryHeapInit: [],
            jvmMemoryHeapUsed: [],
            jvmMemoryHeapCommitted: [],
            jvmMemoryHeapMax: [],
            jvmMemoryHeapUsage: [],
            jvmMemoryNonHeapInit: [],
            jvmMemoryNonHeapUsed: [],
            jvmMemoryNonHeapCommitted: [],
            jvmMemoryNonHeapMax: [],
            jvmMemoryNonHeapUsage: [],
            jvmMemoryTotalCommitted: [],
            jvmMemoryTotalInit: [],
            jvmMemoryTotalMax: [],
            jvmMemoryTotalUsed: [],
            jvmMemoryPoolsSize: [],
            jvmOsCpuLoadProcess: [],
            jvmOsCpuLoadSystem: [],
            jvmOsSystemLoadAverage: [],
            jvmOsPhysicalMemoryFreeSize: [],
            jvmOsPhysicalMemoryTotalSize: [],
            jvmOsVirtualMemoryCommittedSize: [],
            jvmOsSwapSpaceFreeSize: [],
            jvmOsSwapSpaceTotalSize: [],
            jvmThreadsCount: [],
            jvmThreadsDaemonCount: [],
            jvmThreadsBlockedCount: [],
            jvmThreadsDeadlockCount: [],
            jvmThreadsNewCount: [],
            jvmThreadsRunnableCount: [],
            jvmThreadsTerminatedCount: [],
            jvmThreadsTimedWaitingCount: [],
            jvmThreadsWaitingCount: [],
            jvmOsFileDescriptorOpenCount: [],
            jvmOsFileDescriptorMaxCount: [],
            sessionInvalid: false,
            isApiWaiting: true,
        };
    }

    componentWillMount() {
        let queryParams = {
            params: {
                more: true
            }
        };
        let that = this;
        StatusDashboardAPIS.getWorkerHistoryByID(this.props.match.params.id, queryParams)
            .then(function (response) {
                that.setState({
                    jvmClassLoadingLoadedCurrent: response.data.jvmClassLoadingLoadedCurrent.data,
                    jvmClassLoadingLoadedTotal: response.data.jvmClassLoadingLoadedTotal.data,
                    jvmClassLoadingUnloadedTotal: response.data.jvmClassLoadingUnloadedTotal.data,
                    jvmOsCpuLoadProcess: response.data.jvmOsCpuLoadProcess.data,
                    jvmOsCpuLoadSystem: response.data.jvmOsCpuLoadSystem.data,
                    jvmOsPhysicalMemoryFreeSize: response.data.jvmOsPhysicalMemoryFreeSize.data,
                    jvmOsPhysicalMemoryTotalSize: response.data.jvmOsPhysicalMemoryTotalSize.data,
                    jvmOsVirtualMemoryCommittedSize: response.data.jvmOsVirtualMemoryCommittedSize.data,
                    jvmOsSwapSpaceFreeSize: response.data.jvmOsSwapSpaceFreeSize.data,
                    jvmOsSwapSpaceTotalSize: response.data.jvmOsSwapSpaceTotalSize.data,
                    jvmThreadsCount: response.data.jvmThreadsCount.data,
                    jvmThreadsDaemonCount: response.data.jvmThreadsDaemonCount.data,
                    jvmMemoryHeapInit: response.data.jvmMemoryHeapInit.data,
                    jvmMemoryHeapUsed: response.data.jvmMemoryHeapUsed.data,
                    jvmMemoryHeapCommitted: response.data.jvmMemoryHeapCommitted.data,
                    jvmMemoryHeapMax: response.data.jvmMemoryHeapMax.data,
                    jvmMemoryNonHeapInit: response.data.jvmMemoryNonHeapInit.data,
                    jvmMemoryNonHeapUsed: response.data.jvmMemoryNonHeapUsed.data,
                    jvmMemoryNonHeapCommitted: response.data.jvmMemoryNonHeapCommitted.data,
                    jvmMemoryNonHeapMax: response.data.jvmMemoryNonHeapMax.data,
                    jvmOsFileDescriptorOpenCount: response.data.jvmOsFileDescriptorOpenCount.data,
                    jvmOsFileDescriptorMaxCount: response.data.jvmOsFileDescriptorMaxCount.data,
                    jvmThreadsBlockedCount: response.data.jvmThreadsBlockedCount.data,
                    jvmThreadsDeadlockCount: response.data.jvmThreadsDeadlockCount.data,
                    jvmThreadsNewCount: response.data.jvmThreadsNewCount.data,
                    jvmThreadsRunnableCount: response.data.jvmThreadsRunnableCount.data,
                    jvmThreadsTerminatedCount: response.data.jvmThreadsTerminatedCount.data,
                    jvmThreadsTimedWaitingCount: response.data.jvmThreadsTimedWaitingCount.data,
                    jvmThreadsWaitingCount: response.data.jvmThreadsWaitingCount.data,
                    jvmGcPsMarksweepCount: response.data.jvmGcPsMarksweepCount.data,
                    jvmGcPsMarksweepTime: response.data.jvmGcPsMarksweepTime.data,
                    jvmGcPsScavengeCount: response.data.jvmGcPsScavengeCount.data,
                    jvmGcPsScavengeTime: response.data.jvmGcPsScavengeTime.data,
                    jvmOsSystemLoadAverage: response.data.jvmOsSystemLoadAverage.data,
                    jvmMemoryHeapUsage: response.data.jvmMemoryHeapUsage.data,
                    jvmMemoryNonHeapUsage: response.data.jvmMemoryNonHeapUsage.data,
                    jvmMemoryTotalCommitted: response.data.jvmMemoryTotalCommitted.data,
                    jvmMemoryTotalInit: response.data.jvmMemoryTotalInit.data,
                    jvmMemoryTotalMax: response.data.jvmMemoryTotalMax.data,
                    jvmMemoryTotalUsed: response.data.jvmMemoryTotalUsed.data,
                    jvmMemoryPoolsSize: response.data.jvmMemoryPoolsSize.data,
                    isApiWaiting: false
                });
            }).catch((error) => {
            let re = /The session with id '((?:\\.|[^'])*)'|"((?:\\.|[^"])*)" is not valid./;
            let found = error.response.data.match(re);
            if (found != null) {
                this.setState({
                    sessionInvalid: true
                })
            }
        });
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
                <div>
                    <div style={cardStyle}>
                        <JVMLoading
                            data={[this.state.jvmClassLoadingLoadedTotal,
                                this.state.jvmClassLoadingLoadedCurrent,
                                this.state.jvmClassLoadingUnloadedTotal]}/>
                    </div>
                    <div style={cardStyle}>
                        <JVMOsLoad data={[
                            this.state.jvmOsCpuLoadProcess,
                            this.state.jvmOsCpuLoadSystem
                        ]}/>
                    </div>
                    <div style={cardStyle}>
                        <JVMLoad data={[
                            this.state.jvmOsSystemLoadAverage
                        ]}/>
                    </div>
                    <div style={cardStyle}>
                        <JVMOsPhysicalMemory
                            data={[
                                this.state.jvmOsPhysicalMemoryFreeSize,
                                this.state.jvmOsPhysicalMemoryTotalSize,
                                this.state.jvmOsVirtualMemoryCommittedSize]}/>
                    </div>
                    <div style={cardStyle}>
                        <JVMOTotalMemory
                            data={[
                                this.state.jvmMemoryTotalCommitted,
                                this.state.jvmMemoryTotalInit,
                                this.state.jvmMemoryTotalMax,
                                this.state.jvmMemoryTotalUsed]}/>
                    </div>
                    <div style={cardStyle}>
                        <JVMSwap
                            data={[
                                this.state.jvmOsSwapSpaceFreeSize,
                                this.state.jvmOsSwapSpaceTotalSize]}/>
                    </div>
                    <div style={cardStyle}>
                        <JVMThread data={[
                            this.state.jvmThreadsCount,
                            this.state.jvmThreadsDaemonCount,
                            this.state.jvmThreadsBlockedCount,
                            this.state.jvmThreadsDeadlockCount,
                            this.state.jvmThreadsNewCount,
                            this.state.jvmThreadsRunnableCount,
                            this.state.jvmThreadsTerminatedCount,
                            this.state.jvmThreadsTimedWaitingCount,
                            this.state.jvmThreadsWaitingCount,
                        ]}
                        />
                    </div>
                    <div style={cardStyle}>
                        <HeapMemory data={[
                            this.state.jvmMemoryHeapInit,
                            this.state.jvmMemoryHeapUsed,
                            this.state.jvmMemoryHeapCommitted,
                            this.state.jvmMemoryHeapMax,
                            this.state.jvmMemoryHeapUsage
                        ]}/>
                    </div>
                    <div style={cardStyle}>
                        <NonHeapMemory data={[
                            this.state.jvmMemoryNonHeapInit,
                            this.state.jvmMemoryNonHeapUsed,
                            this.state.jvmMemoryNonHeapCommitted,
                            this.state.jvmMemoryNonHeapMax,
                            this.state.jvmMemoryNonHeapUsage

                        ]}/>
                    </div>
                    <div style={cardStyle}>
                        <JVMGarbageCOllector data={[
                            this.state.jvmGcPsMarksweepCount,
                            this.state.jvmGcPsMarksweepTime,
                            this.state.jvmGcPsScavengeCount,
                            this.state.jvmGcPsScavengeTime
                        ]}/>
                    </div>
                    <div style={cardStyle}>
                        <FileDescriptor data={[
                            this.state.jvmOsFileDescriptorOpenCount,
                            this.state.jvmOsFileDescriptorMaxCount]}
                        />
                    </div>
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
        return (
            <div>
                <div className="navigation-bar">
                    <Header/>
                    <Link to={window.contextPath}><FlatButton label="Overview >"
                                                              icon={<HomeButton color="black"/>}/></Link>
                    <Link to={window.contextPath + '/worker/' + this.props.match.params.id}>
                        <FlatButton label={this.state.workerID + " >"}/></Link>
                    <Link to={window.contextPath + '/worker/history/' + this.props.match.params.id}><FlatButton
                        label="Metrics >"/></Link>
                    <RaisedButton label="More" disabled disabledLabelColor='white'
                                  disabledBackgroundColor='#f17b31'/>
                </div>
                <div className="worker-h1">
                    <h2 style={{marginLeft: 40}}> {this.state.workerID} Metrics </h2>
                </div>

                {this.renderCharts()}
            </div>
        );
    }
}

