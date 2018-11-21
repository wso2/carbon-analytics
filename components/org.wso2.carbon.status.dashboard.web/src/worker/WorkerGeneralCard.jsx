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
//App Components
//Material UI
import { Card, CardText, CardTitle, Divider, FontIcon } from 'material-ui';
import {Table, TableBody, TableRow, TableRowColumn} from 'material-ui/Table/index';
import CircleBorder from 'material-ui/svg-icons/av/fiber-manual-record';
import StatusDashboardOverViewAPI from '../utils/apis/StatusDashboardOverViewAPI';
//Localization
import { FormattedMessage } from 'react-intl';

const styles = {
    borderBottom: {borderBottomColor: 'rgba(215,215,215,0.05)'},
    rowColor: {color: 'rgba(255,255,255,0.87)'}, length: {width: 150}
};

/**
 * class which is used to get worker general details.
 */
export default class WorkerGeneralCard extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            generalDetails: [],
            haDetails: [],
            workerID: this.props.id,
            isApiCalled: false
        };
        this.getSnapshotTime = this.getSnapshotTime.bind(this);
        this.getSyncTime = this.getSyncTime.bind(this);
        this.getClusterId = this.getClusterId.bind(this);
    }

    componentWillMount() {
        let that = this;
        StatusDashboardOverViewAPI.postWorkerGeneralByID(this.state.workerID)
            .then(function (response) {
                that.setState({
                    generalDetails: response.data
                });
            });

        StatusDashboardOverViewAPI.getWorkerHaDetailsByID(this.state.workerID)
            .then(function (response) {
                that.setState({
                    haDetails: response.data,
                    isApiCalled: true
                });
            });
    }

    getSnapshotTime() {
        if (this.state.haDetails.clusterID === "Non Clusters" ||
            (this.state.haDetails.clusterID !== "Non Clusters" && this.state.haDetails.haStatus === "Active")) {
            return (
                <TableRow style={styles.borderBottom}>
                    <TableRowColumn style={styles.length}>
                        <FormattedMessage id='workerGeneral.lastSnapshotTime' defaultMessage='Last Snapshot Time' />
                    </TableRowColumn>
                    <TableRowColumn style={styles.rowColor} title={this.state.haDetails.lastSnapshotTime}>
                        {this.state.haDetails.lastSnapshotTime}
                    </TableRowColumn>
                </TableRow>
            );
        }
        return (
            <div/>
        );
    }

    getSyncTime() {
        if (this.state.haDetails.clusterID !== "Non Clusters" && this.state.haDetails.haStatus === "Passive") {
            return (
                <TableRow style={styles.borderBottom}>
                    <TableRowColumn style={styles.length}>
                        <FormattedMessage id='workerGeneral.lastSyncTime' defaultMessage='Last Sync Time' />
                    </TableRowColumn>
                    <TableRowColumn style={styles.rowColor} title={this.state.haDetails.lastSyncTime}>
                        {this.state.haDetails.lastSyncTime}
                    </TableRowColumn>
                </TableRow>
            );
        }
        return (
            <div/>
        );
    }

    getClusterId() {
        if (this.state.haDetails.clusterID !== "Non Clusters") {
            return (
                <TableRow style={styles.borderBottom}>
                    <TableRowColumn style={styles.length}>
                        <FormattedMessage id='workerGeneral.clusterID' defaultMessage='Cluster ID' />
                    </TableRowColumn>
                    <TableRowColumn style={styles.rowColor} title={this.state.haDetails.clusterID}>
                        {this.state.haDetails.clusterID}
                    </TableRowColumn>
                </TableRow>
            );
        }
        return (
            <div/>
        );
    }

    render() {
        if (!this.state.isApiCalled) {
            return (
                <div style={{paddingLeft: 24, width: '30%', float: 'left', boxSizing: 'border-box'}}>
                    <Card style={{height: 660}}>
                        <CardTitle title={<FormattedMessage id='workerGeneral.serverDetails' defaultMessage='Server General Details' />} />
                        <Divider/>
                        <CardText style={{textAlign: 'left'}}>
                            <div style={{
                                textAlign: 'center',
                                paddingTop: '50%'
                            }}>
                                <i className="fw fw-loader5 fw-spin fw-inverse fw-3x"></i>
                            </div>
                        </CardText>
                    </Card>
                </div>
            );
        }
        return (
            <div style={{paddingLeft: 24, width: '30%', float: 'left', boxSizing: 'border-box'}}>
                <Card style={{height: 660}}>
                    <CardTitle titleStyle={{ fontSize: 16, lineHeight: '48px', color: '#fff' }}
                        style={{ padding: '0 16px' }} title={<FormattedMessage id='workerGeneral.serverDetails' defaultMessage='Server General Details' />} />
                    <CardText style={{textAlign: 'left', padding: 0}}>
                        <Table
                            height={612}
                            fixedHeader={false}
                            fixedFooter={false}
                            selectable={false}
                            multiSelectable={false}
                        >
                            <TableBody
                                displayRowCheckbox={false}
                                style={{backgroundColor: '#131313'}}
                            >
                                <TableRow style={styles.borderBottom}>
                                    <TableRowColumn style={styles.length}>
                                        <FormattedMessage id='workerGeneral.started' defaultMessage='Started' />
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor}
                                                    title={this.state.generalDetails.serverStartTime}>
                                        {this.state.generalDetails.serverStartTime}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottom}>
                                    <TableRowColumn style={styles.length}>
                                        <FormattedMessage id='workerGeneral.repoLocation' defaultMessage='Repository Location' />
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor}
                                        title={this.state.generalDetails.repoLocation}>
                                        {this.state.generalDetails.repoLocation}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottom}>
                                    <TableRowColumn style={styles.length}>
                                        <FormattedMessage id='workerGeneral.type' defaultMessage='Type' />
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor}>
                                        {this.state.haDetails.clusterID
                                            !== "Non Clusters" ? "HA Cluster" : "Single Node"}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottom}>
                                    <TableRowColumn style={styles.length}>
                                        <FormattedMessage id='workerGeneral.status' defaultMessage='Status' />
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor}>
                                        {this.state.haDetails.runningStatus === "Reachable" ?
                                            <FontIcon style={{fontSize: '13px'}}>
                                                <CircleBorder style={{height: '13px'}} color='green'/>
                                                <FormattedMessage id='workerGeneral.running' defaultMessage='Running' />
                                            </FontIcon> :
                                            <FontIcon style={{fontSize: '13px'}}>
                                                <CircleBorder style={{height: '13px'}} color='red'/> 
                                                <FormattedMessage id='workerGeneral.notReachable' defaultMessage='Not Reachable' />
                                            </FontIcon>}
                                    </TableRowColumn>
                                </TableRow>
                                {this.getClusterId()}
                                {this.getSyncTime()}
                                {this.getSnapshotTime()}
                                <TableRow style={styles.borderBottom}>
                                    <TableRowColumn style={styles.length}>
                                        <FormattedMessage id='workerGeneral.operatingSystem' defaultMessage='Operating System' />
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor} title={this.state.generalDetails.osName}>
                                        {this.state.generalDetails.osName}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottom}>
                                    <TableRowColumn style={styles.length}>
                                        <FormattedMessage id='workerGeneral.OSVersion' defaultMessage='OS version' />
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor} title={this.state.generalDetails.osVersion}>
                                        {this.state.generalDetails.osVersion}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottom}>
                                    <TableRowColumn style={styles.length}>
                                        <FormattedMessage id='workerGeneral.userHome' defaultMessage='User Home' />
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor} title={this.state.generalDetails.userHome}>
                                        {this.state.generalDetails.userHome}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottom}>
                                    <TableRowColumn style={styles.length}>
                                        <FormattedMessage id='workerGeneral.country' defaultMessage='Country' />
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor}
                                                    title={this.state.generalDetails.userCountry}>
                                        {this.state.generalDetails.userCountry}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottom}>
                                    <TableRowColumn style={styles.length}>
                                        <FormattedMessage id='workerGeneral.timeZone' defaultMessage='Time Zone' />
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor}
                                                    title={this.state.generalDetails.userTimezone}>
                                        {this.state.generalDetails.userTimezone}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottom}>
                                    <TableRowColumn style={styles.length}>
                                        <FormattedMessage id='workerGeneral.javaVersion' defaultMessage='Java Version' />
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor}
                                                    title={this.state.generalDetails.javaVersion}>
                                        {this.state.generalDetails.javaVersion}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottom}>
                                    <TableRowColumn style={styles.length}>
                                        <FormattedMessage id='workerGeneral.javaHome' defaultMessage='Java Home' />
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor} title={this.state.generalDetails.javaHome}>
                                        {this.state.generalDetails.javaHome}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottom}>
                                    <TableRowColumn style={styles.length}>
                                        <FormattedMessage id='workerGeneral.JRName' defaultMessage='Java Runtime Name' />
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor}
                                                    title={this.state.generalDetails.javaRuntimeName}>
                                        {this.state.generalDetails.javaRuntimeName}
                                    </TableRowColumn>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </CardText>
                </Card>
            </div>
        );
    }
}
