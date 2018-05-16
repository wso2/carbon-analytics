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

//Material UI
import {Table, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from 'material-ui/Table';
import Pagination from 'material-ui-pagination';
import Circle from 'material-ui/svg-icons/av/fiber-manual-record';
import {TableFooter} from 'material-ui/Table/index';
import StatusDashboardAPIS from '../utils/apis/StatusDashboardAPIs';

const dataConstants = {PAGE_LENGTH: 5};

/**
 * Class which manages Parent siddhi application details.
 */
export default class ParentAppTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            data: [],
            workerId: this.props.id,
            appName: this.props.appName,
            appsList: [],
            totalSize: [],
        };
        this.currentPage = 1;
        this.loadData = this.loadData.bind(this);
    }

    componentWillMount() {
        let that = this;
        StatusDashboardAPIS.getChildAppDetails(this.state.workerId, this.state.appName)
            .then((response) => {
                that.loadData(this.currentPage, response.data)
            });
    }

    //todo fix pagination from API level
    loadData(pageNumber, response) {
        let sortedData = [];
        let pages = Math.floor(response.length / dataConstants.PAGE_LENGTH) + 1;
        if (pageNumber === pages) {
            let loadedData = (dataConstants.PAGE_LENGTH * (pageNumber - 1));
            for (let i = loadedData; i < response.length; i++) {
                sortedData.push(response[i]);
            }
            this.setState({
                data: sortedData
            });

        } else {
            for (let i = (dataConstants.PAGE_LENGTH * pageNumber - dataConstants.PAGE_LENGTH); i <
            (dataConstants.PAGE_LENGTH * pageNumber); i++) {
                sortedData.push(response[i]);
            }
            this.setState({
                data: sortedData
            });

        }
        this.setState({
            appsList: response,
            totalSize: response.length
        });
    }

    renderRow(row) {
        let isInactive = (row.appStatus === "waiting");
        return (

            <TableRow>
                <TableRowColumn style={{width: '400px'}}>
                    {isInactive ? (
                        <div style={{height: 24, color: 'white'}}>
                            <Circle color={isInactive ? 'red' : 'green'} style={{float: 'left', marginRight: 5}}/>
                            {row.appName}
                        </div>
                    ) : (

                        <div style={{height: 24, color: 'white'}}>
                            <Circle color={isInactive ? 'red' : 'green'} style={{float: 'left', marginRight: 5}}/>
                            {row.appName}
                        </div>

                    )}
                </TableRowColumn>
                <TableRowColumn>
                    <div style={{height: 24, color: 'white'}}>
                        {row.groupName}
                    </div>
                </TableRowColumn>
                <TableRowColumn>
                    <div style={{height: 24, color: 'white'}}>
                        {row.appStatus}
                    </div>
                </TableRowColumn>
                <TableRowColumn>
                    {isInactive ? (
                        <div style={{height: 24, color: 'white'}}>
                            {"-"}
                        </div>

                    ) : (
                        <div style={{height: 24, color: 'white'}}>
                            {row.host + ":" + row.port}
                        </div>

                    )}
                </TableRowColumn>
            </TableRow>
        );
    }

    render() {
        return (
            <div style={{backgroundColor: '#222222'}}>
                <Table>
                    <TableHeader displaySelectAll={false}
                                 adjustForCheckbox={false}>
                        <TableRow>
                            <TableHeaderColumn style={{color: '#f6f6f6', width: '100px'}}><h3>Child Apps</h3>
                            </TableHeaderColumn>
                            <TableHeaderColumn style={{color: '#f6f6f6', width: '100px'}}><h3>Group Name</h3>
                            </TableHeaderColumn>
                            <TableHeaderColumn style={{color: '#f6f6f6', width: '100px'}}><h3>Child App Status</h3>
                            </TableHeaderColumn>
                            <TableHeaderColumn style={{color: '#f6f6f6', width: '100px'}}><h3>Worker Node</h3>
                            </TableHeaderColumn>
                        </TableRow>
                    </TableHeader>

                    <TableBody displayRowCheckbox={false} style={{backgroundColor: '#131313'}}>
                        {this.state.data.map((row) => (
                            this.renderRow(row)
                        ))}
                    </TableBody>
                    <TableFooter adjustForCheckbox={false} style={{height: 10}}>
                        <TableRow style={{height: 10}}>
                            <TableRowColumn colSpan="1" style={{textAlign: 'center', height: 10}}>
                            </TableRowColumn>
                        </TableRow>
                    </TableFooter>
                </Table>

                <div style={{float: 'right'}}>
                    <Pagination
                        total={Math.floor(this.state.totalSize / dataConstants.PAGE_LENGTH) + 1}
                        current={this.currentPage}
                        display={dataConstants.PAGE_LENGTH}
                        onChange={number => {
                            this.currentPage = number;
                            this.loadData(number, this.state.appsList);
                        }}
                    />
                </div>
            </div>
        );
    }
}
