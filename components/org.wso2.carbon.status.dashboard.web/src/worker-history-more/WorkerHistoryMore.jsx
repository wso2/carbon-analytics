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
 * class which manages worker specific more history details.
 */
import React from "react";
import {Link} from "react-router-dom";
//Material UI
import {FlatButton, GridList, GridTile} from "material-ui";
import HomeButton from "material-ui/svg-icons/action/home";
//App Components
import StatusDashboardAPIS from "../utils/apis/StatusDashboardAPIs";
import JVMLoading from "./JVMLoading";
import JVMOsCpu from "./JVMOsCpu";
import JVMOsPhysicalMemory from "./JVMOsPhysicalMemory";
import JVMThread from "./JVMThread";
import JVMSwap from "./JVMSwap";

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
            jvmOsCpuLoadProcess: [],
            jvmOsCpuLoadSystem: [],
            jvmOsPhysicalMemoryFreeSize: [],
            jvmOsPhysicalMemoryTotalSize: [],
            jvmThreadsCount: [],
            jvmThreadsDaemonCount: [],
            jvmOsSwapSpaceFreeSize: [],
            jvmOsSwapSpaceTotalSize: []

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
                    jvmThreadsCount: response.data.jvmThreadsCount.data,
                    jvmThreadsDaemonCount: response.data.jvmThreadsDaemonCount.data,
                    jvmOsSwapSpaceFreeSize: response.data.jvmOsSwapSpaceFreeSize.data,
                    jvmOsSwapSpaceTotalSize: response.data.jvmOsSwapSpaceTotalSize.data
                });
            })
    }

    render() {
        return (
            <div>
                <div className="navigation-bar">
                    <Link to="/sp-status-dashboard/overview"><FlatButton label="Overview >"
                                                                         icon={<HomeButton color="black"/>}/></Link>
                    <Link to={"/sp-status-dashboard/worker/" + this.props.match.params.id }>
                        <FlatButton label={this.state.workerID + " >"}/></Link>
                    <Link to={"/sp-status-dashboard/worker/history/" + this.props.match.params.id }><FlatButton
                        label="Metrics >"/></Link>
                    <FlatButton label="More"/>
                </div>
                <div className="worker-h1">
                    <h1 style={{marginLeft: 20}}> {this.state.workerID} Metrics </h1>
                </div>


                <div style={{padding: 30}}>
                    <JVMLoading
                        data={[this.state.jvmClassLoadingLoadedTotal, this.state.jvmClassLoadingLoadedCurrent, this.state.jvmClassLoadingUnloadedTotal]}/>
                </div>
                <div style={{padding: 30}}>
                    <JVMOsCpu data={[this.state.jvmOsCpuLoadProcess, this.state.jvmOsCpuLoadSystem]}/>
                </div>
                <div style={{padding: 30}}>
                    <JVMOsPhysicalMemory
                        data={[this.state.jvmOsPhysicalMemoryFreeSize, this.state.jvmOsPhysicalMemoryTotalSize]}/>
                </div>
                <div style={{padding: 30}}>
                    <JVMThread data={[this.state.jvmThreadsCount, this.state.jvmThreadsDaemonCount]}/>
                </div>
                <div style={{padding: 30}}>
                    <JVMSwap data={[this.state.jvmOsSwapSpaceFreeSize, this.state.jvmOsSwapSpaceTotalSize]}/>
                </div>

            </div>
        );
    }
}

