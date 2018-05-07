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

import React from "react";
import { Link } from "react-router-dom";
//Material UI
import { CardActions, IconButton, Snackbar, Tooltip, Typography } from "material-ui-next";
import {GridList, GridTile} from "material-ui";

import CircleBorder from "material-ui/svg-icons/av/fiber-manual-record";

//App Components
import AuthenticationAPI from "../utils/apis/AuthenticationAPI";
import AuthManager from "../auth/utils/AuthManager";
import Clock from "./Clock";

const styles = {
    gridList: {width: '100%', height: 150, margin: 0},
    smallIcon: {width: 20, height: 20, zIndex: 1, padding: 5},
    overviewLegend: {fontSize: 10, color: '#fff'},
    legendContainer: {width: '100%', textAlign: 'center', position: 'absolute', bottom: 5},
     };
const messageBoxStyle = { textAlign: "center", color: "white" };
const errorMessageStyle = { backgroundColor: "#FF5722", color: "white" };
const successMessageStyle = { backgroundColor: "#4CAF50", color: "white" };

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
        let gridTiles, lastUpdated, color, haStatus;

            gridTiles = <div>
                <Link style={{ textDecoration: 'none' }} to={window.contextPath + '/add-worker'}>
                    <GridList cols={1} cellHeight={98} style={styles.gridList}>
                        <GridTile>
                            <h4 style={{
                                textAlign: 'center',
                                color: 'white',
                                padding: 20
                            }}>Please add the node manually.</h4>
                        </GridTile>

                    </GridList>
                </Link>
            </div>;
            lastUpdated = "#";
            color='red';

        return [gridTiles, lastUpdated, color, haStatus];
    }

    render() {
        let items = this.renderGridTile();
        let titleBg = items[2] === 'red' ? '#570404' : '#424242';

        return (
            <div>
                <GridTile style={{background: 'black'}}>
                    <div style={{
                        display: 'flex', alignItems: 'center', background: titleBg,
                        position: 'absolute', bottom: 0, width: '100%'
                    }}>
                        <IconButton><CircleBorder
                            color={items[2]}/></IconButton>
                        <div>
                            <Typography className={'node-title'}>
                                {this.props.worker.nodeId}</Typography>
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
