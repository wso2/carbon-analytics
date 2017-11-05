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
//App Components
import StatusDashboardAPIS from "../utils/apis/StatusDashboardAPIs";
//Material UI
import {Card, CardText, CardTitle, Divider} from "material-ui";
import {Table, TableBody, TableRow, TableRowColumn} from "material-ui/Table/index";

const styles = {borderBottomColor: "rgba(215,215,215,0.05)", rowColor: "rgba(255,255,255,0.87)", length: {width: 200}};

/**
 * class which is used to get worker general details.
 */
export default class WorkerGeneralCard extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            generalDetails: [],
            workerID: this.props.id
        }
    }

    componentWillMount() {
        let that = this;
        StatusDashboardAPIS.getWorkerGeneralByID(this.state.workerID)
            .then(function (response) {
                that.setState({
                    generalDetails: response.data
                });
            });
    }

    render() {
        return (
            <div style={{paddingLeft: 20, width: '30%', float: 'left', boxSizing: 'border-box'}}>
                <Card style={{height: 660}}>
                    <CardTitle title="Server General Details"/>
                    <Divider/>
                    <CardText style={{textAlign: 'left'}}>
                        <Table
                            height={560}
                            fixedHeader={false}
                            fixedFooter={false}
                            selectable={false}
                            multiSelectable={false}
                        >
                            <TableBody
                                displayRowCheckbox={false}
                                style={{backgroundColor: '#131313'}}
                            >
                                <TableRow style={styles.borderBottomColor}>
                                    <TableRowColumn style={styles.length}>
                                        Started
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor} title={this.state.generalDetails.serverStartTime}>
                                        {this.state.generalDetails.serverStartTime}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottomColor}>
                                    <TableRowColumn style={styles.length}>
                                        Repository Location
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor} title={this.state.generalDetails.repoLocation}>
                                        {this.state.generalDetails.repoLocation}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottomColor}>
                                    <TableRowColumn style={styles.length}>
                                        Operating System
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor} title={this.state.generalDetails.osName}>
                                        {this.state.generalDetails.osName}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottomColor}>
                                    <TableRowColumn style={styles.length}>
                                        OS version
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor} title={this.state.generalDetails.osVersion}>
                                        {this.state.generalDetails.osVersion}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottomColor}>
                                    <TableRowColumn style={styles.length}>
                                        User Home
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor} title={this.state.generalDetails.userHome}>
                                        {this.state.generalDetails.userHome}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottomColor}>
                                    <TableRowColumn style={styles.length}>
                                        Country
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor} title={this.state.generalDetails.userCountry}>
                                        {this.state.generalDetails.userCountry}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottomColor}>
                                    <TableRowColumn style={styles.length}>
                                        Time Zone
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor} title={this.state.generalDetails.userTimezone}>
                                        {this.state.generalDetails.userTimezone}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottomColor}>
                                    <TableRowColumn style={styles.length}>
                                        Java Version
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor} title={this.state.generalDetails.javaVersion}>
                                        {this.state.generalDetails.javaVersion}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottomColor}>
                                    <TableRowColumn style={styles.length}>
                                        Java Home
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor} title={this.state.generalDetails.javaHome}>
                                        {this.state.generalDetails.javaHome}
                                    </TableRowColumn>
                                </TableRow>
                                <TableRow style={styles.borderBottomColor}>
                                    <TableRowColumn style={styles.length}>
                                        Java Runtime Name
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.rowColor} title={this.state.generalDetails.javaRuntimeName}>
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
