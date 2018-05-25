/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import {Link} from 'react-router-dom';
//Material UI
import {Button, CardActions, IconButton, Snackbar, Tooltip, Typography} from 'material-ui-next';
import {Dialog, GridList, GridTile} from 'material-ui';
import CircleBorder from 'material-ui/svg-icons/av/fiber-manual-record';
import Delete from 'material-ui/svg-icons/action/delete';
import TrendDown from 'material-ui/svg-icons/hardware/keyboard-arrow-down';
import TrendUp from 'material-ui/svg-icons/hardware/keyboard-arrow-up';
import '../../public/css/dashboard.css';
//App Components
import StatusDashboardAPIS from '../utils/apis/StatusDashboardAPIs';
import {HttpStatus} from '../utils/Constants';
import OverviewChart from './OverviewChart';
import AuthenticationAPI from '../utils/apis/AuthenticationAPI';
import AuthManager from '../auth/utils/AuthManager';
import Clock from './Clock';

const styles = {
    gridList: {width: '100%', height: 150, margin: 0},
    smallIcon: {width: 20, height: 20, zIndex: 1, padding: 5},
    overviewLegend: {fontSize: 10, color: '#fff'},
    legendContainer: {width: '100%', textAlign: 'center', position: 'absolute', bottom: 5}
};
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
            worker: this.props.worker,
            hasPermission: false
        };
        this.deleteWorker = this.deleteWorker.bind(this);
        this.showError = this.showError.bind(this);
        this.showMessage = this.showMessage.bind(this);
    }

    componentWillMount() {
        let that = this;
        AuthenticationAPI.isUserAuthorized('manager', AuthManager.getUser().SDID)
            .then((response) => {
                that.setState({
                    hasPermission: response.data
                });
            });
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
                if (response.status === HttpStatus.OK) {
                    that.setState({open: false});
                    that.showMessage("Worker '" + this.props.worker.workerId + "' is deleted successfully !!");
                    setTimeout(function () {
                        window.location.href = window.contextPath;
                    }, 1000)
                }
                else {
                    that.setState({open: false});
                    that.showError("Worker '" + this.props.worker.workerId + "' is not deleted successfully !!");
                }
            });

    }

    renderTime(lastUpdate) {
        if (lastUpdate === "#") {
            return (
                <Clock lastUpdate={this.props.worker.lastUpdate}/>
            )
        } else {
            return (
                <text>{lastUpdate}</text>
            )
        }
    }

    /**
     * Method which render delete worker button if permission is granted
     * @param workersList
     * @returns {XML}
     */
    renderDeleteWorker() {
        if (this.state.hasPermission) {
            return (
                <Tooltip id="tooltip-icon" title="Delete Worker">
                    <IconButton className={'btn-delete'} iconStyle={styles.smallIcon}
                                style={{zIndex: 1}} onClick={() => {
                        this.setState({open: true})
                    }}>
                        <Delete color="grey"/>
                    </IconButton>
                </Tooltip>
            )
        }
    }

    renderGridTile() {
        let gridTiles, lastUpdated, color, haStatus;
        //never reached workers
        if (this.props.worker.serverDetails.clusterID == null) {
            if (this.props.worker.statusMessage == null) {
                gridTiles = <div>
                    <GridList cols={1} cellHeight={98} style={styles.gridList}>
                        <h2 style={{textAlign: 'center', color: '#dedede', padding: 0, margin: 12}}>
                            Worker is not reachable!</h2>
                    </GridList>
                </div>;
                lastUpdated = "N/A";
                color = 'red';
            } else {
                gridTiles = <div>
                    <GridList cols={1} cellHeight={98} style={styles.gridList}>
                        <h2 style={{textAlign: 'center', color: '#dedede', padding: 0, margin: 12}}>
                            Worker is not reachable!
                            <br/>
                            <text style={{textAlign: 'center', fontSize: 12, color: '#dedede'}}>
                                {this.props.worker.statusMessage}
                            </text>
                        </h2>
                    </GridList>
                </div>;
                lastUpdated = "N/A";
                color = 'red';
            }
            //statistics disabled workers
        } else if (!this.props.worker.serverDetails.isStatsEnabled) {
            gridTiles = <div>
                <Link style={{textDecoration: 'none'}}
                      to={window.contextPath + '/worker/' + this.props.worker.workerId}>
                    <GridList cols={2} cellHeight={98} style={styles.gridList}>
                        <GridTile>
                            <div style={{height: '100%', display: 'flex', alignItems: 'center'}}>
                                <h4 style={{
                                    textAlign: 'center',
                                    color: '#dedede',
                                    padding: 0,
                                    margin: '0 20px'
                                }}>{this.props.worker.statusMessage}</h4>
                            </div>
                        </GridTile>
                        <GridTile>
                            <div className="grid-tile-h1" style={{marginTop: 30}}>
                                <h1 className="active-apps">
                                    {this.props.worker.serverDetails.siddhiAppStatus.activeAppCount}
                                </h1>
                                <h1 style={{display: 'inline'}}> | </h1>
                                <h1 className="inactive-apps">
                                    {this.props.worker.serverDetails.siddhiAppStatus.inactiveAppCount}
                                </h1>
                            </div>
                            <div style={styles.legendContainer}>
                                <Typography style={styles.overviewLegend} align={'center'}>
                                    Siddhi Apps
                                </Typography>
                            </div>
                        </GridTile>
                    </GridList></Link>
            </div>;
            lastUpdated = "#";
            if (this.props.worker.serverDetails.clusterID === "Non Clusters") {
                if (this.props.worker.serverDetails.runningStatus === "Reachable") {
                    color = 'green'
                } else {
                    color = 'red'
                }
            } else {
                if (this.props.worker.serverDetails.runningStatus === "Reachable") {
                    if (this.props.worker.serverDetails.haStatus === "Active") {
                        color = 'green';
                        haStatus = 'Active'
                    } else if (this.props.worker.serverDetails.haStatus === "Passive") {
                        color = 'grey';
                        haStatus = 'Passive'
                    }
                } else {
                    color = 'red'
                }
            }
        } else {
            //handling trend for cpu, memory and load average
            let cpuTrend, memoryTrend, loadTrend, loadAvg, loadTrendImg;

            if (JSON.parse(localStorage.getItem(constants.cpu)) === null) {
                cpuTrend = constants.na
            }
            else if (JSON.parse(localStorage.getItem(constants.cpu)) >
                this.props.worker.serverDetails.workerMetrics.systemCPU * 100) {
                cpuTrend = constants.down
            } else {
                cpuTrend = constants.up
            }

            if (JSON.parse(localStorage.getItem(constants.memory)) === null) {
                memoryTrend = constants.na
            }
            if (JSON.parse(localStorage.getItem(constants.memory)) >
                this.props.worker.serverDetails.workerMetrics.memoryUsage * 100) {
                memoryTrend = constants.down
            } else {
                memoryTrend = constants.up
            }

            if (JSON.parse(localStorage.getItem(constants.load)) === null) {
                loadTrend = constants.na
            }
            if (JSON.parse(localStorage.getItem(constants.load)) >
                this.props.worker.serverDetails.workerMetrics.loadAverage) {
                loadTrend = constants.down
            } else {
                loadTrend = constants.up
            }

            localStorage.setItem(constants.memory,
                JSON.stringify(this.props.worker.serverDetails.workerMetrics.memoryUsage * 100));
            localStorage.setItem(constants.cpu,
                JSON.stringify(this.props.worker.serverDetails.workerMetrics.systemCPU * 100));
            localStorage.setItem(constants.load, this.props.worker.serverDetails.workerMetrics.loadAverage);

            if (this.props.worker.serverDetails.osName === "windows") {
                loadAvg = <h4 style={{margin: 0}}>N/A in Windows</h4>;
                loadTrendImg = <div/>;
            } else {
                loadAvg = <h1 style={{margin: 0}}>{this.props.worker.serverDetails.workerMetrics.loadAverage}</h1>;
                {loadTrendImg = loadTrend === constants.up ? <span style={{color: 'red'}}>˄</span> :
                    <span style={{color: 'green'}}>˅</span>}
            }
            gridTiles =
                <div>
                    <Link style={{textDecoration: 'none'}}
                          to={window.contextPath + '/worker/' + this.props.worker.workerId}>
                        <GridList className={'node-overview'} cols={4} cellHeight={98} style={styles.gridList}>
                            <GridTile>
                                <OverviewChart
                                    chartValue={this.props.worker.serverDetails.workerMetrics.systemCPU * 100}
                                    color="#19cdd7"/>
                                <div style={styles.legendContainer}>
                                    <Typography style={styles.overviewLegend} align={'center'}>
                                        CPU Usage
                                        {cpuTrend === constants.up ? <span style={{color: 'red'}}>˄</span> :
                                            <span style={{color: 'green'}}>˅</span>}
                                    </Typography>
                                </div>
                            </GridTile>

                            <GridTile>
                                <OverviewChart
                                    chartValue={this.state.worker.serverDetails.workerMetrics.memoryUsage * 100}
                                    color="#f17b31"/>
                                <div style={styles.legendContainer}>
                                    <Typography style={styles.overviewLegend} align={'center'}>
                                        Memory Usage
                                        {memoryTrend === constants.up ? <span style={{color: 'red'}}>˄</span> :
                                            <span style={{color: 'green'}}>˅</span>}
                                    </Typography>
                                </div>
                            </GridTile>

                            <GridTile>
                                <div className="grid-tile-h1" style={{
                                    display: 'flex',
                                    alignItems: 'center', height: '100%',
                                    justifyContent: 'center'
                                }}>
                                    {loadAvg}</div>
                                <div style={styles.legendContainer}>
                                    <Typography style={styles.overviewLegend} align={'center'}>
                                        Load Average
                                        {loadTrendImg}
                                    </Typography>
                                </div>
                            </GridTile>

                            <GridTile>
                                <div className="grid-tile-h1" style={{marginTop: 30}}>
                                    <h1 className="active-apps">
                                        {this.props.worker.serverDetails.siddhiAppStatus.activeAppCount}</h1>
                                    <h1 style={{display: 'inline'}}> | </h1>
                                    <h1 className="inactive-apps">
                                        {this.props.worker.serverDetails.siddhiAppStatus.inactiveAppCount}
                                    </h1>
                                </div>
                                <div style={styles.legendContainer}>
                                    <Typography style={styles.overviewLegend} align={'center'}>
                                        Siddhi Apps
                                    </Typography>
                                </div>
                            </GridTile>
                        </GridList>
                    </Link>
                </div>;
            lastUpdated = "#";
            if (this.props.worker.serverDetails.clusterID === "Non Clusters") {
                if (this.props.worker.serverDetails.runningStatus === "Reachable") {
                    color = 'green'
                } else {
                    color = 'red'
                }
            } else {
                if (this.props.worker.serverDetails.runningStatus === "Reachable") {
                    if (this.props.worker.serverDetails.haStatus === "Active") {
                        color = 'green';
                        haStatus = 'Active'
                    } else if (this.props.worker.serverDetails.haStatus === "Passive") {
                        color = 'grey';
                        haStatus = 'Passive'
                    }
                } else {
                    color = 'red'
                }
            }
        }
        return [gridTiles, lastUpdated, color, haStatus];
    }

    render() {
        let items = this.renderGridTile();
        let actionsButtons = [
            <Button
                variant="raised"
                className="btn-primary"
                onClick={this.deleteWorker}
            >Yes</Button>,
            <Button
                onClick={() => {
                    this.setState({open: false})
                }}
                className="btn-default"
            >NO</Button>,
        ];
        let titleBg = items[2] === 'red' ? '#570404' : '#424242';
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

                <GridTile style={{background: 'black'}}>
                    <div style={{
                        display: 'flex', alignItems: 'center', background: titleBg,
                        position: 'absolute', bottom: 0, width: '100%'
                    }}>
                        <IconButton><CircleBorder
                            color={items[2]}/></IconButton>
                        <div>
                            <Typography className={'node-title'}>
                                {this.state.workerID}</Typography>
                            <Typography className={'node-last-update'}>
                                <span>Last Updated: {this.renderTime(items[1])}
                                    <div style={{float: 'right', display: 'inline'}}><strong>{items[3]}</strong>
                                    </div>
                                </span>
                            </Typography>
                        </div>
                    </div>
                    <CardActions style={{
                        position: 'absolute', right: 0,
                        display: 'inline', height: 20, padding: 5
                    }}>
                        {this.renderDeleteWorker()}
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
