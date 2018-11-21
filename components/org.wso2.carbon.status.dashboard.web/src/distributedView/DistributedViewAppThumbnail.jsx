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
import {Typography} from 'material-ui-next';
import {CardActions, Dialog, FlatButton, GridList, GridTile, IconButton, Snackbar} from 'material-ui';
import ToolTip from 'react-tooltip';
import CircleBorder from 'material-ui/svg-icons/av/fiber-manual-record';
import AuthenticationAPI from '../utils/apis/AuthenticationAPI';
import AuthManager from '../auth/utils/AuthManager';
import { FormattedMessage } from 'react-intl';

const styles = {
    gridList: {width: '100%', height: 150, margin: 0},
    smallIcon: {width: 20, height: 20, zIndex: 1},
    overviewLegend: {fontSize: 10, color: '#fff'},
    legendContainer: {width: '100%', textAlign: 'center', position: 'absolute', bottom: 5}
};
const messageBoxStyle = {textAlign: "center", color: "white"};
const errorMessageStyle = {backgroundColor: "#FF5722", color: "white"};
const successMessageStyle = {backgroundColor: "#4CAF50", color: "white"};

export default class DistributedViewAppThumbnail extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            warning: '',
            showMsg: '',
            message: '',
            messageStyle: '',
            open: false,
            distributedApp: this.props.distributedApp,
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
        this.setState({ distributedApp: nextProps.distributedApp });
    }


    renderGridTile() {
        let gridTiles, color, appStatus;
        gridTiles =
            <div>
                <Link style={{textDecoration: 'none'}}
                      to={window.contextPath + "/" + this.props.distributedApp.managerId + '/siddhi-apps/' + this.props.distributedApp.parentAppName}>
                    <GridList cols={3} cellHeight={98} style={styles.gridList}>
                        <GridTile data-tip
                                  data-for='groups'>
                            <div className="grid-tile-h1" style={{marginTop: 30}}>
                                <h1 className="deployed-apps-details">{this.props.distributedApp.numberOfGroups}</h1>
                            </div>
                            <div style={styles.legendContainer}>
                                <Typography style={styles.overviewLegend} align={'center'}>
                                    <FormattedMessage id='distributedviewthumbnail.groups' defaultMessage='Groups' />
                                </Typography>
                            </div>
                            <ToolTip id='groups' aria-haspopup='true' role='example'>
                                <p><FormattedMessage id='distributedviewthumbnail.numberOfSiddhiApps' defaultMessage='Indicates number of groups in the parent siddhi application' /></p>
                            </ToolTip>

                        </GridTile>
                        <GridTile>
                            <div className="grid-tile-h1" style={{marginTop: 30}}><h1
                                className="active-apps" data-tip
                                data-for='deployedChildApps'>{this.props.distributedApp.deployedChildApps}</h1>
                                <h1 style={{display: 'inline'}}> | </h1>
                                <h1 className="inactive-apps" data-tip data-for='notDeployedChildApps'>
                                    {this.props.distributedApp.notDeployedChildApps}
                                </h1>
                            </div>
                            <div style={styles.legendContainer}>
                                <Typography style={styles.overviewLegend} align={'center'}>
                                    <FormattedMessage id='distributedviewthumbnail.childApps' defaultMessage='Child Apps' />
                                </Typography>
                            </div>
                            <ToolTip id='deployedChildApps' aria-haspopup='true' role='example'>
                                <p> <FormattedMessage id='distributedviewthumbnail.noOfDeployedChildApps' defaultMessage='Indicates number of deployed child apps' /></p>
                            </ToolTip>
                            <ToolTip id='notDeployedChildApps' aria-haspopup='true' role='example'>
                                <p> <FormattedMessage id='distributedviewthumbnail.noOfUndeployedChildApps' defaultMessage='Indicates number of un-deployed child apps' /></p>
                            </ToolTip>
                        </GridTile>
                        <GridTile>
                            <div className="grid-tile-h1" style={{marginTop: 30}}><h1
                                className="active-apps" data-tip
                                data-for='usedWorkerNodes'>{this.props.distributedApp.usedWorkerNodes}</h1>
                                <h1 style={{display: 'inline'}}> | </h1>
                                <h1 className="inactive-apps" style={{color: 'orange'}} data-tip
                                    data-for='totalWorkerNodes'>
                                    {this.props.distributedApp.totalWorkerNodes}
                                </h1>
                            </div>
                            <div style={styles.legendContainer}>
                                <Typography style={styles.overviewLegend} align={'center'}>
                                    <FormattedMessage id='distributedviewthumbnail.workerNodes' defaultMessage='Worker Nodes' />
                                </Typography>
                            </div>
                            <ToolTip id='usedWorkerNodes' aria-haspopup='true' role='example'>
                                <p><FormattedMessage id='distributedviewthumbnail.tooltip.numberOfNodesIndicator' defaultMessage='Indicates number of worker nodes used in the {br} particular parent siddhi applications deployment ' values={{ br: (<br />) }} /></p>
                            </ToolTip>
                            <ToolTip id='totalWorkerNodes' aria-haspopup='true' role='example'>
                                <p><FormattedMessage id='distributedviewthumbnail.totalNumberOfNodes' defaultMessage='Indicates total number of worker nodes in the resource cluster' /></p>
                            </ToolTip>
                        </GridTile>
                    </GridList>
                </Link>
            </div>

        if (this.props.distributedApp.usedWorkerNodes !== "0") {
            color = 'green';
            appStatus = 'Deployed'
        } else {
            color = 'red';
            appStatus = 'Not-Deployed'
        }

        return [gridTiles, color, appStatus];
    }

    render() {
        let items = this.renderGridTile();
        return (

            <div>
                <GridTile
                    title={this.props.distributedApp.parentAppName}
                    actionIcon={<IconButton><CircleBorder
                        color={items[1]}/></IconButton>}
                    actionPosition="left"
                    style={{background: 'black', marginBottom: '30px'}}
                    titleBackground={'#424242'}
                >
                    <CardActions style={{boxSizing: 'border-box', float: 'right', display: 'inline', height: 20}}>

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
