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
import { Link } from "react-router-dom";
//Material UI
import { CardActions, Dialog, FlatButton, GridList, GridTile, IconButton, Snackbar } from "material-ui";
import CircleBorder from "material-ui/svg-icons/av/fiber-manual-record";
import Delete from "material-ui/svg-icons/action/delete";
import TrendDown from "material-ui/svg-icons/hardware/keyboard-arrow-down";
import TrendUp from "material-ui/svg-icons/hardware/keyboard-arrow-up";
//App Components
import StatusDashboardAPIS from "../utils/apis/StatusDashboardAPIs";
import { HttpStatus } from '../utils/Constants';
import OverviewChart from "./OverviewChart";
import AuthenticationAPI from "../utils/apis/AuthenticationAPI";
import AuthManager from "../auth/utils/AuthManager";
import Clock from "./Clock";

const styles = { gridList: { width: '100%', height: 200 }, smallIcon: { width: 20, height: 20, zIndex: 1 } };
const messageBoxStyle = { textAlign: "center", color: "white" };
const errorMessageStyle = { backgroundColor: "#FF5722", color: "white" };
const successMessageStyle = { backgroundColor: "#4CAF50", color: "white" };
const constants = { memory: "memory", cpu: "cpu", load: "load", down: "down", up: "up", na: "n/a" };


export default class DistributedViewResourceNodeThumbnail extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            warning: '',
            showMsg: '',
            message: '',
            messageStyle: '',
            open: false,
            worker: this.props.worker,
            hasPermission: false
        };
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
        this.setState({ worker: nextProps.worker })
    }

    renderTime(lastUpdate) {
        if (lastUpdate === "#") {
            return (
                <Clock lastUpdate={this.props.worker.lastUpdate} />
            )
        } else {
            return (
                <text>{lastUpdate}</text>
            )
        }
    }

    renderGridTile() {

        console.log("poppy"+this.state.worker);

        let gridTiles, lastUpdated, color, haStatus;
     //   if (this.props.worker.statusMessage === "Please add the node manually.") {

            gridTiles = <div>
                <Link style={{ textDecoration: 'none' }} to={window.contextPath + '/add-worker'}>
                    <GridList cols={1} cellHeight={180} style={styles.gridList}>
                        <GridTile>
                            <h4 style={{
                                textAlign: 'center',
                                color: 'white',
                                padding: 50
                            }}>Please add the node manually.</h4>
                        </GridTile>
                        {/*<GridTile title="Siddhi Apps" titlePosition="bottom"*/}
                                  {/*titleStyle={{ fontSize: 10, textAlign: 'center' }}>*/}
                            {/*<div className="grid-tile-h1" style={{ marginTop: 50 }}><h1*/}
                                {/*className="active-apps">{this.props.worker.serverDetails.siddhiAppStatus.activeAppCount}</h1>*/}
                                {/*<h1 style={{ display: 'inline' }}> |</h1>*/}
                                {/*<h1 className="inactive-apps">*/}
                                    {/*{this.props.worker.serverDetails.siddhiAppStatus.inactiveAppCount}*/}
                                {/*</h1>*/}
                            {/*</div>*/}
                        {/*</GridTile>*/}
                    </GridList>
                </Link>
            </div>;
            lastUpdated = "#";
            color='red';
            // if (this.props.worker.serverDetails.clusterID === "Non Clusters") {
            //     if (this.props.worker.serverDetails.runningStatus === "Reachable") {
            //         color = 'green'
            //     } else {
            //         color = 'red'
            //     }
            // } else {
            //     if (this.props.worker.serverDetails.runningStatus === "Reachable") {
            //         if (this.props.worker.serverDetails.haStatus === "Active") {
            //             color = 'green';
            //             haStatus = 'Active'
            //         } else if (this.props.worker.serverDetails.haStatus === "Passive") {
            //             color = 'grey';
            //             haStatus = 'Passive'
            //         }
            //     } else {
            //         color = 'red'
            //     }
            // }
       // }
        return [gridTiles, lastUpdated, color, haStatus];
    }

    render() {
        let items = this.renderGridTile();

        {console.log("am here!!!")}


        return (
            <div>
                <GridTile
                    //todo: this should be nodeId
                     title={this.props.worker.nodeId}
                    subtitle=
                        {<span>Last Updated: {this.renderTime(items[1])}
                            <div style={{ float: 'right', display: 'inline' }}><strong>{items[3]}</strong></div>
                    </span>}
                    actionIcon={<IconButton><CircleBorder
                        color={items[2]} /></IconButton>}
                    actionPosition="left"
                    style={{ background: 'black' }}
                    titleBackground={items[2] === 'red' ? '#570404' : '#424242'}
                >
                    <CardActions style={{ boxSizing: 'border-box', float: 'right', display: 'inline', height: 20 }}>

                    </CardActions>
                    {items[0]}
                </GridTile>
                <Snackbar contentStyle={messageBoxStyle} bodyStyle={this.state.messageStyle} open={this.state.showMsg}
                          message={this.state.message} autoHideDuration={4000}
                          onRequestClose={() => {
                              this.setState({ showMsg: false, message: "" })
                          }} />
            </div>
        );
    }
}