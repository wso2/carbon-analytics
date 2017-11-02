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
//Material UI
import {CardActions, Dialog, FlatButton, GridList, GridTile, IconButton, Snackbar} from "material-ui";
import CircleBorder from "material-ui/svg-icons/av/fiber-manual-record";
import Delete from "material-ui/svg-icons/action/delete";
import TrendDown from "material-ui/svg-icons/hardware/keyboard-arrow-down";
import TrendUp from "material-ui/svg-icons/hardware/keyboard-arrow-up";
//App Components
import StatusDashboardAPIS from "../utils/apis/StatusDashboardAPIs";
import OverviewChart from "./OverviewChart";

const styles = {gridList: {width: '100%', height: 250}, smallIcon: {width: 20, height: 20,}};
const messageBoxStyle = {textAlign: "center", color: "white"};
const errorMessageStyle = {backgroundColor: "#FF5722", color: "white"};
const successMessageStyle = {backgroundColor: "#4CAF50", color: "white"};
const constants = {memory: "memory", cpu: "cpu", load: "load", down: "down", up: "up", na: "n/a"};

export default class WorkerThumbnail extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            warning: '',
            showMsg: '',
            message: '',
            messageStyle: '',
            open: false,
            workerID: this.props.worker.workerId.split("_")[0] + ":" + this.props.worker.workerId.split("_")[1],
            worker: this.props.worker
        };
        this.deleteWorker = this.deleteWorker.bind(this);
        this.showError = this.showError.bind(this);
        this.showMessage = this.showMessage.bind(this);
    }

    showError(message) {
        this.setState({
            messageStyle: errorMessageStyle,
            showMsg: true,
            message: message
        });
    }

    showMessage(message) {
        this.setState({
            messageStyle: successMessageStyle,
            showMsg: true,
            message: message
        });
    }

    componentWillReceiveProps(nextProps) {
        this.setState({worker: nextProps.worker})
    }

    deleteWorker() {
        let that = this;
        StatusDashboardAPIS.deleteWorkerByID(this.props.worker.workerId)
            .then((response) => {
                if (response.status === 200) {
                    that.setState({open: false});
                    that.showMessage("Worker '" + this.props.worker.workerId + "' is deleted successfully !!");
                    setTimeout(function () {
                        window.location.href = "/sp-status-dashboard/overview";
                    }, 1000)
                }
                else {
                    that.setState({open: false});
                    that.showError("Worker '" + this.props.worker.workerId + "' is not deleted successfully !!");
                }
            });

    }

    renderGridTile() {
        let gridTiles, lastUpdated, color;
        //never reached workers
        if (this.props.worker.serverDetails.clusterID == null) {
            gridTiles = <div>
                <GridList cols={1} cellHeight={180} style={styles.gridList}>
                    <h2 style={{textAlign: 'center', color: 'white', padding: 50}}>Worker is not reachable!</h2>
                </GridList>
            </div>;
            lastUpdated = "N/A";
            color = 'red';
            //statistics disabled workers
        } else if (!this.props.worker.serverDetails.isStatsEnabled) {
            gridTiles = <div>
                <Link style={{textDecoration: 'none'}}
                      to={"/sp-status-dashboard/worker/" + this.props.worker.workerId}>
                    <GridList cols={2} cellHeight={180} style={styles.gridList}>
                        <GridTile>
                            <h4 style={{
                                textAlign: 'center',
                                color: 'white',
                                padding: 50
                            }}>{this.props.worker.statusMessage}</h4>
                        </GridTile>
                        <GridTile title="Siddhi Apps" titlePosition="bottom"
                                  titleStyle={{fontSize: 10, textAlign: 'center'}}>
                            <div className="grid-tile-h1" style={{marginTop: 50}}><h1
                                className="active-apps">{this.props.worker.serverDetails.siddhiApps.active}</h1>
                                <h1 style={{display: 'inline'}}> |</h1>
                                <h1 className="inactive-apps"> {this.props.worker.serverDetails.siddhiApps.inactive}</h1>
                            </div>
                        </GridTile>
                    </GridList></Link>
            </div>;
            lastUpdated = Math.abs((this.props.worker.lastUpdate - this.props.currentTime)) / 1000 + "s ago";

            if (this.props.worker.serverDetails.clusterID === "Non Clusters") {
                if (this.props.worker.serverDetails.runningStatus === "Reachable") {
                    color = 'green'
                } else {
                    color = 'red'
                }
            } else {
                if (this.props.worker.serverDetails.runningStatus === "Reachable") {
                    if (this.props.worker.serverDetails.haStatus === "Active") {
                        color = 'green'
                    } else if (this.props.worker.serverDetails.haStatus === "Passive") {
                        color = 'grey'
                    }
                } else {
                    color = 'red'
                }
            }
        } else {
            //handling trend for cpu, memory and load average
            let cpuTrend, memoryTrend, loadTrend;

            if (JSON.parse(localStorage.getItem(constants.cpu)) === null) {
                cpuTrend = constants.na
            }
            else if (JSON.parse(localStorage.getItem(constants.cpu)) > this.props.worker.serverDetails.workerMetrics.systemCPU * 100) {
                cpuTrend = constants.down
            } else {
                cpuTrend = constants.up
            }

            if (JSON.parse(localStorage.getItem(constants.memory)) === null) {
                memoryTrend = constants.na
            }
            if (JSON.parse(localStorage.getItem(constants.memory)) > this.props.worker.serverDetails.workerMetrics.memoryUsage * 100) {
                memoryTrend = constants.down
            } else {
                memoryTrend = constants.up
            }

            if (JSON.parse(localStorage.getItem(constants.load)) === null) {
                loadTrend = constants.na
            }
            if (JSON.parse(localStorage.getItem(constants.load)) > this.props.worker.serverDetails.workerMetrics.loadAverage) {
                loadTrend = constants.down
            } else {
                loadTrend = constants.up
            }

            localStorage.setItem(constants.memory, JSON.stringify(this.props.worker.serverDetails.workerMetrics.memoryUsage * 100));
            localStorage.setItem(constants.cpu, JSON.stringify(this.props.worker.serverDetails.workerMetrics.systemCPU * 100));
            localStorage.setItem(constants.load, this.props.worker.serverDetails.workerMetrics.loadAverage);

            gridTiles =
                <div>
                    <Link style={{textDecoration: 'none'}}
                          to={"/sp-status-dashboard/worker/" + this.props.worker.workerId}>
                        <GridList cols={4} cellHeight={180} style={styles.gridList}>
                            <GridTile title="CPU Usage" titlePosition="bottom" titleStyle={{fontSize: 10}}>
                                <div><OverviewChart
                                    chartValue={this.props.worker.serverDetails.workerMetrics.systemCPU * 100}
                                    color="#19cdd7"/></div>
                                <div style={{display: 'inline', float: 'right', marginTop: '15%', marginRight: 0}}>
                                    {cpuTrend === constants.up ? <TrendUp style={{color: 'red'}}/> :
                                        <TrendDown style={{color: 'green'}}/>}</div>
                            </GridTile>

                            <GridTile title="Memory Usage" titlePosition="bottom" titleStyle={{fontSize: 10}}>
                                <div><OverviewChart
                                    chartValue={this.state.worker.serverDetails.workerMetrics.memoryUsage * 100}
                                    color="#f17b31"/></div>
                                <div style={{display: 'inline', float: 'right', marginTop: '15%', marginRight: 0}}>
                                    {memoryTrend === constants.up ? <TrendUp style={{color: 'red'}}/> :
                                        <TrendDown style={{color: 'green'}}/>}</div>
                            </GridTile>

                            <GridTile title="Load Average" titlePosition="bottom" titleStyle={{fontSize: 10}}>
                                <div className="grid-tile-h1" style={{marginTop: 50}}>
                                    <h1>{this.props.worker.serverDetails.workerMetrics.loadAverage} </h1></div>
                                <div style={{display: 'inline', float: 'right', marginTop: '28%', marginRight: 0}}>
                                    {loadTrend === constants.up ? <TrendUp style={{color: 'red'}}/> :
                                        <TrendDown style={{color: 'green'}}/>}</div>
                            </GridTile>

                            <GridTile title="Siddhi Apps" titlePosition="bottom" titleStyle={{fontSize: 10}}>
                                <div className="grid-tile-h1" style={{marginTop: 50}}><h1
                                    className="active-apps">{this.props.worker.serverDetails.siddhiApps.active}</h1>
                                    <h1 style={{display: 'inline'}}> |</h1>
                                    <h1 className="inactive-apps"> {this.props.worker.serverDetails.siddhiApps.inactive}</h1>
                                </div>
                            </GridTile>
                        </GridList>
                    </Link>
                </div>;
            lastUpdated = Math.abs((this.props.worker.lastUpdate - this.props.currentTime)) / 1000 + "s ago";
            if (this.props.worker.serverDetails.clusterID === "Non Clusters") {
                if (this.props.worker.serverDetails.runningStatus === "Reachable") {
                    color = 'green'
                } else {
                    color = 'red'
                }
            } else {
                if (this.props.worker.serverDetails.runningStatus === "Reachable") {
                    if (this.props.worker.serverDetails.haStatus === "Active") {
                        color = 'green'
                    } else if (this.props.worker.serverDetails.haStatus === "Passive") {
                        color = 'grey'
                    }
                } else {
                    color = 'red'
                }
            }
        }
        return [gridTiles, lastUpdated, color];
    }

    render() {
        let items = this.renderGridTile();
        let actionsButtons = [
            <FlatButton
                label="Yes"
                backgroundColor='#f17b31'
                onClick={this.deleteWorker}
            />,
            <FlatButton
                label="No"
                onClick={() => {
                    this.setState({open: false})
                }}
            />,
        ];
        return (
            <div>
                <Dialog
                    title="Confirmation"
                    actions={actionsButtons}
                    modal={true}
                    open={this.state.open}
                    onRequestClose={() => {
                        this.setState({open: false})
                    }}>
                    {"Do you want to delete worker '" + this.state.workerID + "' ?"}
                </Dialog>

                <GridTile
                    title={this.state.workerID}
                    subtitle={<span>Last Updated: {items[1]}</span>}
                    actionIcon={<IconButton><CircleBorder
                        color={items[2]}/></IconButton>}
                    actionPosition="left"
                    style={{background: 'black'}}
                    titleBackground={items[2] === 'red' ? '#570404' : '#424242'}
                >
                    <CardActions style={{boxSizing: 'border-box', float: 'right', display: 'inline', height: 20}}>
                        <IconButton iconStyle={styles.smallIcon} tooltip="Delete Worker"
                                    tooltipPosition="bottom-center" onClick={() => {
                            this.setState({open: true})
                        }}><Delete color="grey"/></IconButton>
                    </CardActions>
                    {items[0]}
                </GridTile>
                <Snackbar contentStyle={messageBoxStyle} bodyStyle={this.state.messageStyle} open={this.state.showMsg}
                          message={this.state.message} autoHideDuration={4000}
                          onRequestClose={() => {
                              this.setState({showMsg: false, message: ""})
                          }}/>
            </div>
        );
    }

}