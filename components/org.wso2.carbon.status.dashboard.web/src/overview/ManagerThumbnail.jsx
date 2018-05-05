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
import {CardActions, Dialog, Divider, FlatButton, GridList, GridTile, IconButton, Snackbar, Toggle} from 'material-ui';
import CircleBorder from 'material-ui/svg-icons/av/fiber-manual-record';
import Delete from 'material-ui/svg-icons/action/delete';
import TrendDown from 'material-ui/svg-icons/hardware/keyboard-arrow-down';
import TrendUp from 'material-ui/svg-icons/hardware/keyboard-arrow-up';
//App Components
import StatusDashboardAPIS from '../utils/apis/StatusDashboardAPIs';
import {HttpStatus} from '../utils/Constants';
import OverviewChart from './OverviewChart';
import AuthenticationAPI from '../utils/apis/AuthenticationAPI';
import AuthManager from '../auth/utils/AuthManager';
import Clock from './Clock';
import StatusDashboardOverViewAPI from "../utils/apis/StatusDashboardOverViewAPI";
import WorkerOverview from "./WorkerOverview";
import WorkerThumbnail from "./WorkerThumbnail";
import DistributedViewResourceNodeThumbnail from "./DistributedViewResourceNodeThumbnail";
import DistributedViewAppThumbnail from "../distributedView/DistributedViewAppThumbnail";

const style = {gridList: {width: '100%', height: 250}, smallIcon: {width: 20, height: 20, zIndex: 1}};
const messageBoxStyle = {textAlign: "center", color: "white"};
const errorMessageStyle = {backgroundColor: "#FF5722", color: "white"};
const successMessageStyle = {backgroundColor: "#4CAF50", color: "white"};
const constants = {memory: "memory", cpu: "cpu", load: "load", down: "down", up: "up", na: "n/a"};

const styles = {
    root: {
        display: 'flex',
        flexWrap: 'wrap',
        justifyContent: 'space-around',
        backgroundColor: '#222222',
        width: '100%'
    },
    gridList: {width: '90%', height: '100%', padding: 40},
    h3: {color: 'white', marginLeft: '4%', backgroundColor: '#222222'},
    h3Title: {color: '#C0C0C0', marginLeft: '4%', backgroundColor: '#222222'},
    titleStyle: {fontSize: 18, lineHeight: 1.5, color: '#FF3D00'},
    headerStyle: {height: 30, backgroundColor: '#242424'},
    paper: {height: 50, width: 500, textAlign: 'center'},
    background: {backgroundColor: '#222222'}
};

export default class ManagerThumbnail extends React.Component {
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
            hasPermission: false,
            isApiCalled: false,
            sessionInvalid: false,
            isError: false,
            //TODO: ONLY FOR TESTING
            resourceClustersList: {}
        };
        this.deleteManager = this.deleteManager.bind(this);
        this.showError = this.showError.bind(this);
        this.showMessage = this.showMessage.bind(this);
        this.renderWorkers = this.renderWorkers.bind(this);
        //this.getResourceClusterList = this.getResourceClusterList.bind(this);
    }


    // //todo: ONLY FOR TESTING
    componentDidMount() {
        let that = this;

        StatusDashboardOverViewAPI.getResourceClusterNodes(this.props.worker.workerId)
            .then((response) => {
                if (response.status === HttpStatus.OK) {
                    this.setState({
                        resourceClustersList: response.data,
                    });
                } else {
                    that.showError("Manager '" + this.props.worker.workerId + "' is not rendered successfully !!");
                }

            }).catch((error) => {
            if (error.response != null) {
                if (error.response.status === 401) {
                    this.setState({
                        isApiCalled: true,
                        sessionInvalid: true,
                        statusMessage: "Authentication fail. Please login again."
                    })
                } else if (error.response.status === 403) {
                    this.setState({
                        isApiCalled: true,
                        statusMessage: "User Have No Permission to view this page."
                    });
                } else {
                    this.setState({
                        isError: true,
                        isApiCalled: true,
                        statusMessage: "Unknown error occurred! : " + JSON.stringify(error.response.data)
                    });
                }
            }
        });


        console.log("ids" + this.state.workerID);
        console.log("ID" + this.state.workerId);


        // console.log("ressy" + this.state.resourceClustersList);

        //todo: check the catch
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

    deleteManager() {
        let that = this;
        StatusDashboardAPIS.deleteManagerById(this.props.worker.workerId)
            .then((response) => {
                if (response.status === HttpStatus.OK) {
                    that.setState({open: false});
                    that.showMessage("Manager '" + this.props.worker.workerId + "' is deleted successfully !!");
                    setTimeout(function () {
                        window.location.href = window.contextPath;
                    }, 1000)
                }
                else {
                    that.setState({open: false});
                    that.showError("Manager '" + this.props.worker.workerId + "' is not deleted successfully !!");
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
    renderDeleteManager() {
        if (this.state.hasPermission) {
            return (
                <IconButton iconStyle={style.smallIcon} tooltip="Delete Manager" style={{zIndex: 1}}
                            tooltipPosition="bottom-center" onClick={() => {
                    this.setState({open: true})
                }}><Delete color="grey"/></IconButton>
            )
        } else {
            return (
                <IconButton iconStyle={{width: 20, height: 20, display: 'none'}} tooltip="Delete Worker"
                            tooltipPosition="bottom-center" onClick={() => {
                }}><Delete color="grey"/></IconButton>
            )
        }
    }


    renderWorkers(workersList) {

        if (ManagerThumbnail.hasNodes(this.state.resourceClustersList)) {
            return (

                Object.keys(workersList).map((id, workerList) => {
                    if (id === 'ResourceCluster') {
                        return (
                            <div style={{marginLeft: '-60px', height: '200%'}}>
                                <h4 style={styles.h3Title}>Workers</h4>
                                <div style={{
                                    display: 'flex',
                                    flexWrap: 'wrap',
                                    width: '290%',
                                    height: '200%',
                                    padding: '40px',
                                    justifyContent: 'space-around',
                                    backgroundColor: '#222222'
                                }}>

                                    <GridList cols={3} padding={50} cellHeight={300} style={style.gridList}>
                                        {workersList[id].map((worker) => {

                                            if (worker.statusMessage === "Please add the node manually.") {
                                                return (
                                                    <div>
                                                        <DistributedViewResourceNodeThumbnail worker={worker}
                                                                                              currentTime={new Date().getTime()}/>
                                                    </div>
                                                )

                                            } else {
                                                return (
                                                    <div>
                                                        <WorkerThumbnail worker={worker}
                                                                         currentTime={new Date().getTime()}/>
                                                    </div>
                                                )
                                            }

                                        })}
                                    </GridList>
                                </div>
                            </div>
                        )

                    }

                })
            );
        } else {
            console.log("am not passing")
        }

    }


    static hasNodes(clusters) {
        for (let prop in clusters) {
            if (clusters.hasOwnProperty(prop)) {
                return true;
            }
        }
        return false;
    }


    renderGridTile() {
        let gridTiles, lastUpdated, color, haStatus;
        //never reached workers
        if (this.props.worker.clusterInfo.groupId === " ") {
            if (this.props.worker.statusMessage == null) {
                gridTiles = <div>
                    <GridList cols={1} cellHeight={180} style={style.gridList}>
                        <h2 style={{textAlign: 'center', color: 'white', padding: 50}}>Manager is not reachable!</h2>
                    </GridList>
                </div>;
                lastUpdated = "N/A";
                color = 'red';
            } else {
                gridTiles = <div>
                    <GridList cols={1} cellHeight={180} style={style.gridList}>
                        <h2 style={{textAlign: 'center', color: 'white', padding: 15}}>Manager is not reachable!
                            <br/>
                            <text style={{textAlign: 'center', fontSize: 12, color: 'white'}}>
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
                <GridList cols={2} cellHeight={180} style={style.gridList}>
                    <GridTile>
                        <h4 style={{
                            textAlign: 'center',
                            color: 'white',
                            padding: 50
                        }}>{this.props.worker.statusMessage}</h4>
                    </GridTile>
                </GridList>
            </div>;
            lastUpdated = "#";
            if (this.props.worker.clusterInfo.groupId === "Non Clusters") {
                if (this.props.worker.serverDetails.runningStatus === "Reachable") {
                    color = 'green'
                } else {
                    color = 'red'
                }
            } else {
                if (this.props.worker.serverDetails.runningStatus === "Reachable") {
                    if (this.props.worker.clusterInfo.haStatus === "Active") {
                        color = 'green';
                        haStatus = 'Active'
                    } else if (this.props.worker.clusterInfo.haStatus === "Pasive") {
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
                loadAvg = <h4>N/A in Windows</h4>;
                loadTrendImg = <div/>;
            } else {
                loadAvg = <h1>{this.props.worker.serverDetails.workerMetrics.loadAverage}</h1>;
                loadTrendImg = loadTrend === constants.up ? <TrendUp style={{color: 'red'}}/> :
                    <TrendDown style={{color: 'green'}}/>
            }
            if (this.props.worker.clusterInfo.haStatus === "Active") {
                gridTiles =
                    <div>
                        <Link style={{textDecoration: 'none'}}
                              to={window.contextPath + "/" + this.props.worker.workerId + "/siddhi-apps"}>
                            <GridList cols={4} cellHeight={180} style={style.gridList}>
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
                                        {loadAvg}</div>
                                    <div style={{display: 'inline', float: 'right', marginTop: '28%', marginRight: 0}}>
                                        {loadTrendImg}</div>
                                </GridTile>
                                {/*Active Nodes display the siddhi detils*/}
                                <GridTile title="Siddhi Apps" titlePosition="bottom" titleStyle={{fontSize: 10}}>
                                    <div className="grid-tile-h1" style={{marginTop: 50}}><h1
                                        className="active-apps">{this.props.worker.serverDetails.siddhiAppStatus.activeAppCount}</h1>
                                        <h1 style={{display: 'inline'}}> |</h1>
                                        <h1 className="inactive-apps">
                                            {this.props.worker.serverDetails.siddhiAppStatus.inactiveAppCount}
                                        </h1>
                                    </div>
                                </GridTile>
                            </GridList>
                        </Link>
                    </div>;
            } else {
                gridTiles =
                    <div style={{cursor: 'not-allowed'}}>
                        <GridList cols={4} cellHeight={180} style={style.gridList}>
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
                                    {loadAvg}</div>
                                <div style={{display: 'inline', float: 'right', marginTop: '28%', marginRight: 0}}>
                                    {loadTrendImg}</div>
                            </GridTile>
                            <GridTile title="Siddhi Apps" titlePosition="bottom" titleStyle={{fontSize: 10}}>
                                <div className="grid-tile-h1" style={{marginTop: 50}}>
                                    <h4 style={{
                                        textAlign: 'center',
                                        color: 'white',
                                        padding: 20
                                    }}>Siddhi Apps are disabled</h4>
                                </div>
                            </GridTile>
                        </GridList>
                    </div>;
            }

            lastUpdated = "#";
            if (this.props.worker.clusterInfo.groupId === "Non Clusters") {
                if (this.props.worker.serverDetails.runningStatus === "Reachable") {
                    color = 'green'
                } else {
                    color = 'red'
                }
            } else {
                if (this.props.worker.serverDetails.runningStatus === "Reachable") {
                    if (this.props.worker.clusterInfo.haStatus === "Active") {
                        color = 'green';
                        haStatus = 'Active'
                    } else if (this.props.worker.clusterInfo.haStatus === "Pasive") {
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
            <FlatButton
                label="Yes"
                backgroundColor='#f17b31'
                onClick={this.deleteManager}
            />,
            <FlatButton
                label="No"
                onClick={() => {
                    this.setState({open: false})
                }}
            />,
        ];
        return (
            <div style={styles.root}>
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
                    subtitle=
                        {<span>Last Updated: {this.renderTime(items[1])}
                            <div style={{float: 'right', display: 'inline'}}><strong>{items[3]}</strong></div>
                        </span>}
                    actionIcon={<IconButton><CircleBorder
                        color={items[2]}/></IconButton>}
                    actionPosition="left"
                    style={{background: 'black'}}
                    titleBackground={items[2] === 'red' ? '#570404' : '#424242'}
                >
                    <CardActions style={{boxSizing: 'border-box', float: 'right', display: 'inline', height: 20}}>
                        {this.renderDeleteManager()}
                    </CardActions>
                    {items[0]}
                </GridTile>


                <Snackbar contentStyle={messageBoxStyle} bodyStyle={this.state.messageStyle} open={this.state.showMsg}
                          message={this.state.message} autoHideDuration={4000}
                          onRequestClose={() => {
                              this.setState({showMsg: false, message: ""})
                          }}/>
                <div style={{
                    width: '200%', height: '200%', display: 'flex',
                    flexWrap: 'wrap'
                }}>


                    {this.renderWorkers(this.state.resourceClustersList)}
                </div>
            </div>

        )
            ;
    }
}