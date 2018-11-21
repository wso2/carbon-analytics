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
import {Link} from 'react-router-dom';
//App Components
import DashboardUtils from '../utils/DashboardUtils';
import VizG from 'react-vizgrammar';
//Material UI
import {Table, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from 'material-ui/Table';
import Pagination from 'material-ui-pagination';
import Circle from 'material-ui/svg-icons/av/fiber-manual-record';
import {TableFooter} from 'material-ui/Table/index';
import StatusDashboardOverViewAPI from '../utils/apis/StatusDashboardOverViewAPI';
//Localization
import { FormattedMessage } from 'react-intl';

const dataConstants = {PAGE_LENGTH: 5};
const metadata = {names: ['Time', 'value'], types: ['linear', 'linear']};
const sparkLineConfig = {
    x: 'Time',
    charts: [{type: 'spark-area', y: 'value', fill: '#f17b31', fillOpacity: 0.1}],
    width: 100,
    height: 40,
    strokeWidth: 1,
    fillOpacity: 0.1,
    append: false,
};

let currentPage = 1;

/**
 * class which manages Siddhi App list of a worker.
 */
export default class AppTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            data: [],
            workerId: this.props.id,
            appsList: [],
            totalSize: []
        };
        this.loadData = this.loadData.bind(this);
    }

    componentWillMount() {
        let that = this;
        StatusDashboardOverViewAPI.getSiddhiApps(this.state.workerId)
            .then(function (response) {
                that.loadData(currentPage, response.data.siddhiAppMetricsHistoryList)
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
        let isInactive = (row.status === "inactive");
        let throughputLimit, latencyLimit, memoryLimit;
        if (row.appMetricsHistory.throughput.data == null) {
            throughputLimit = [0, 10]
        } else {
            throughputLimit = DashboardUtils.getYDomain(row.appMetricsHistory.throughput.data);
        }
        if (row.appMetricsHistory.latency.data == null) {
            latencyLimit = [0, 10]
        } else {
            latencyLimit = DashboardUtils.getYDomain(row.appMetricsHistory.latency.data);
        }
        if (row.appMetricsHistory.memory.data == null) {
            memoryLimit = [0, 10]
        } else {
            memoryLimit = DashboardUtils.getYDomain(row.appMetricsHistory.memory.data);
        }

        return (
            <TableRow>
                <TableRowColumn style={{width: '400px'}}>
                    {isInactive ? (
                        <div style={{height: 24, color: 'white', display: 'flex', alignItems: 'center'}}>
                            <Circle color={isInactive ? 'red' : 'green'} style={{float: 'left', marginRight: 5}}/>
                            {row.appName}
                        </div>
                    ) : (
                        <Link style={{textDecoration: 'none'}}
                              to={window.contextPath + '/worker/' + this.state.workerId + "/siddhi-apps/" + row.appName
                              + "/" + row.isStatEnabled}
                        >
                            <div style={{height: 24, color: 'white', display: 'flex', alignItems: 'center'}}>
                                <Circle color={isInactive ? 'red' : 'green'} style={{float: 'left', marginRight: 5}}/>
                                {row.appName}
                            </div>
                        </Link>
                    )}
                </TableRowColumn>
                <TableRowColumn style={{width: '100px'}}>{row.status}</TableRowColumn>
                <TableRowColumn style={{width: '100px'}}>{row.agetime}</TableRowColumn>
                {row.isStatEnabled ?
                    row.appMetricsHistory.latency.data.length !== 0 ?
                        (<TableRowColumn>
                            <div>
                                <div style={{float: 'left', lineHeight: 4}}>
                                    {row.appMetricsHistory.latencyRecent}
                                </div>
                                <Link style={{textDecoration: 'none'}}
                                      to={window.contextPath + '/worker/' + this.state.workerId + "/siddhi-apps/"
                                      + row.appName
                                      + "/" + row.isStatEnabled}>
                                    <div style={{width: '100px', float: 'right', height: '40px'}}>
                                        <VizG data={row.appMetricsHistory.latency.data} metadata={metadata}
                                              config={sparkLineConfig}
                                              yDomain={[latencyLimit[0], latencyLimit[1]]}
                                              append={false}
                                              width={100}
                                              height={40}
                                        />
                                    </div>
                                </Link>
                            </div>
                        </TableRowColumn>)
                        : (<TableRowColumn><FormattedMessage id='noData' defaultMessage='No data available' /></TableRowColumn>)
                    : (<TableRowColumn>-</TableRowColumn>)
                }

                {row.isStatEnabled ?
                    row.appMetricsHistory.throughput.data.length !== 0 ?
                        (<TableRowColumn>
                            <div style={{float: 'left', lineHeight: 4}}>
                                {row.appMetricsHistory.throughputRecent}
                            </div>
                            <Link style={{textDecoration: 'none'}}
                                  to={window.contextPath + '/worker/' + this.state.workerId + "/siddhi-apps/"
                                  + row.appName
                                  + "/" + row.isStatEnabled}>
                                <div style={{width: '100px', float: 'right', height: '40px'}}>
                                    <VizG data={row.appMetricsHistory.throughput.data} metadata={metadata}
                                          config={sparkLineConfig}
                                          yDomain={[throughputLimit[0], throughputLimit[1]]}
                                          width={100}
                                          height={40}
                                    />
                                </div>
                            </Link>
                        </TableRowColumn>)
                        : (<TableRowColumn><FormattedMessage id='noData' defaultMessage='No data available' /></TableRowColumn>)
                    : (<TableRowColumn>-</TableRowColumn>)
                }

                {row.isStatEnabled ?
                    row.appMetricsHistory.memory.data.length !== 0 ?
                        (<TableRowColumn>
                            <div style={{height: '40px', float: 'left', lineHeight: 4}}>
                                {row.appMetricsHistory.memoryRecent}
                            </div>
                            <Link style={{textDecoration: 'none'}}
                                  to={window.contextPath + '/worker/' + this.state.workerId + "/siddhi-apps/"
                                  + row.appName
                                  + "/" + row.isStatEnabled}>

                                <div style={{width: '100px', float: 'right', height: '40px'}}>
                                    <VizG data={row.appMetricsHistory.memory.data} metadata={metadata}
                                          config={sparkLineConfig}
                                          yDomain={[memoryLimit[0], memoryLimit[1]]}
                                          width={100}
                                          height={40}
                                    />
                                </div>
                            </Link>
                        </TableRowColumn>)
                        : (<TableRowColumn><FormattedMessage id='noData' defaultMessage='No data available' /></TableRowColumn>)
                    : (<TableRowColumn>-</TableRowColumn>)
                }
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
                            <TableHeaderColumn style={{ color: '#f6f6f6', width: '400px' }}>
                                <h3><FormattedMessage id='appTable.name' defaultMessage='App Name' /></h3>
                            </TableHeaderColumn>
                            <TableHeaderColumn style={{ color: '#f6f6f6', width: '100px' }}>
                                <h3><FormattedMessage id='appTable.status' defaultMessage='Status' /></h3>
                            </TableHeaderColumn>
                            <TableHeaderColumn style={{ color: '#f6f6f6', width: '100px' }}>
                                <h3><FormattedMessage id='appTable.age' defaultMessage='Age' /></h3>
                            </TableHeaderColumn>
                            <TableHeaderColumn style={{ color: '#f6f6f6' }}>
                                <h3><FormattedMessage id='appTable.latencyInMillis' defaultMessage='Latency (milliseconds)' /></h3>
                            </TableHeaderColumn>
                            <TableHeaderColumn style={{ color: '#f6f6f6' }}
                            ><h3><FormattedMessage id='appTable.throughput' defaultMessage='Throughput (events/second)' /></h3>
                            </TableHeaderColumn>
                            <TableHeaderColumn style={{ color: '#f6f6f6' }}>
                                <h3><FormattedMessage id='appTable.memory' defaultMessage='Memory' /></h3></TableHeaderColumn>
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
                        current={currentPage}
                        display={dataConstants.PAGE_LENGTH}
                        onChange={number => {
                            currentPage = number;
                            this.loadData(number, this.state.appsList);
                        }}
                    />
                </div>
            </div>
        );
    }
}
