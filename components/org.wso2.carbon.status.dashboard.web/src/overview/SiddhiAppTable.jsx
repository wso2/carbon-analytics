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
import Info from 'material-ui/svg-icons/action/info';
import Header from '../common/Header';
import {TableFooter} from 'material-ui/Table/index';
import HomeButton from 'material-ui/svg-icons/action/home';
import StatusDashboardOverViewAPI from "../utils/apis/StatusDashboardOverViewAPI";
import {Button, Divider, Typography} from "material-ui-next";
import {Link} from "react-router-dom";

const dataConstants = {PAGE_LENGTH: 7};

const styles = {
    h3: {color: '#dedede', marginLeft: '2%', backgroundColor: '#222222'},
    h3Title: {color: '#C0C0C0', marginLeft: '2%', backgroundColor: '#222222', marginTop: '4%'},
    navBtn: {color: '#fff', padding: '0 16px 0 12px', verticalAlign: 'middle'},
    divider: {backgroundColor: '#9E9E9E', width: '95%', marginLeft:'2%'},
    alignCenter: {display: 'flex', alignItems: 'center'},
};

/**
 * Class which manages Parent siddhi application details.
 */
export default class SiddhiAppTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            data: [],
            haAppData: [],
            managerData: [],
            appsList: [],
            managerAppList: [],
            haAppList: [],
            totalSize: [],
            managerTotalSize: [],
            haTotalSize: []
        };
        this.currentPage = 1;
        this.managerCurrentPage = 1;
        this.haCurrentPage = 1;
        this.loadData = this.loadData.bind(this);
        this.loadManagerData = this.loadManagerData.bind(this);
        this.loadHAAppData = this.loadHAAppData.bind(this);
    }

    componentWillMount() {
        StatusDashboardOverViewAPI.getSingleNodeDeploymentSiddhiAppSummary()
            .then((response) => {
                this.loadData(this.currentPage, response.data)
            });
        StatusDashboardOverViewAPI.getManagerSiddhiAppSummary()
            .then((response) => {
                this.loadManagerData(this.managerCurrentPage, response.data)


            });
        StatusDashboardOverViewAPI.getHASiddhiAppSummary()
            .then((response) => {
                this.loadHAAppData(this.haCurrentPage, response.data)
            })
    }

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

    loadHAAppData(pageNumber, response) {
        let sortedData = [];
        let pages = Math.floor(response.length / dataConstants.PAGE_LENGTH) + 1;
        if (pageNumber === pages) {
            let loadedData = (dataConstants.PAGE_LENGTH * (pageNumber - 1));
            for (let i = loadedData; i < response.length; i++) {
                sortedData.push(response[i]);
            }
            this.setState({
                haAppData: sortedData
            });

        } else {
            for (let i = (dataConstants.PAGE_LENGTH * pageNumber - dataConstants.PAGE_LENGTH); i <
            (dataConstants.PAGE_LENGTH * pageNumber); i++) {
                sortedData.push(response[i]);
            }
            this.setState({
                haAppData: sortedData
            });
        }
        this.setState({
            haAppList: response,
            haTotalSize: response.length
        });
    }

    loadManagerData(pageNumber, response) {
        let sortedData = [];
        let pages = Math.floor(response.length / dataConstants.PAGE_LENGTH) + 1;
        if (pageNumber === pages) {
            let loadedData = (dataConstants.PAGE_LENGTH * (pageNumber - 1));
            for (let i = loadedData; i < response.length; i++) {
                sortedData.push(response[i]);
            }
            this.setState({
                managerData: sortedData
            });

        } else {
            for (let i = (dataConstants.PAGE_LENGTH * pageNumber - dataConstants.PAGE_LENGTH); i <
            (dataConstants.PAGE_LENGTH * pageNumber); i++) {
                sortedData.push(response[i]);
            }
            this.setState({
                managerData: sortedData
            });

        }
        this.setState({
            managerAppList: response,
            managerTotalSize: response.length
        });
    }

    static renderRow(app) {
        let isInactive = (app.status === "inactive" || app.status === 'passive');
        let isWorker = (app.deployedNodeType === "Worker")
        return (
            <TableRow>
                {isWorker ? (
                    <TableRowColumn style={{width: '400px'}}>
                        {isInactive ? (
                            <Link style={{textDecoration: 'none'}}
                                  to={window.contextPath + '/worker/' + app.deployedNodeHost + '_'
                                  + app.deployedNodePort + '/siddhi-apps/' + app.appName + '/' + app.isStatEnabled}>
                                <div style={{height: 24, color: 'white'}}>
                                    <Circle color={isInactive ? 'red' : 'green'}
                                            style={{float: 'left', marginRight: 5}}/>

                                    {app.appName}
                                </div>
                            </Link>
                        ) : (
                            <Link style={{textDecoration: 'none'}}
                                  to={window.contextPath + '/worker/' + app.deployedNodeHost + '_'
                                  + app.deployedNodePort + '/siddhi-apps/' + app.appName + '/' + app.isStatEnabled}>

                                <div style={{height: 24, color: 'white'}}>
                                    <Circle color={isInactive ? 'red' : 'green'}
                                            style={{float: 'left', marginRight: 5}}/>
                                    {app.appName}
                                </div>
                            </Link>

                        )}
                    </TableRowColumn>
                ) : (
                    <TableRowColumn style={{width: '400px'}}>
                        {isInactive ? (
                            <Link style={{textDecoration: 'none'}}
                                  to={window.contextPath + '/' + app.deployedNodeHost + '_'
                                  + app.deployedNodePort + '/siddhi-apps/' + app.appName}>
                                <div style={{height: 24, color: 'white'}}>
                                    <Circle color={isInactive ? 'red' : 'green'}
                                            style={{float: 'left', marginRight: 5}}/>

                                    {app.appName}
                                </div>
                            </Link>
                        ) : (
                            <Link style={{textDecoration: 'none'}}
                                  to={window.contextPath + '/' + app.deployedNodeHost + '_'
                                  + app.deployedNodePort + '/siddhi-apps/' + app.appName}>

                                <div style={{height: 24, color: 'white'}}>
                                    <Circle color={isInactive ? 'red' : 'green'}
                                            style={{float: 'left', marginRight: 5}}/>
                                    {app.appName}
                                </div>
                            </Link>

                        )}
                    </TableRowColumn>
                )}

                <TableRowColumn>
                    <div style={{height: 24, color: 'white'}}>
                        {app.status}
                    </div>
                </TableRowColumn>
                <TableRowColumn>
                    <div style={{height: 24, color: 'white'}}>
                        {app.lastUpdate}
                    </div>
                </TableRowColumn>

                {isWorker ? (
                    <TableRowColumn>
                        <Link style={{textDecoration: 'none'}}
                              to={window.contextPath + '/worker/' + app.deployedNodeHost + '_' + app.deployedNodePort}>

                            <div style={{height: 24, color: 'white'}}>
                                {app.deployedNodeHost + ":" + app.deployedNodePort}
                            </div>
                        </Link>
                    </TableRowColumn>
                ) : (
                    <TableRowColumn>
                        <Link style={{textDecoration: 'none'}}
                              to={window.contextPath + '/' + app.deployedNodeHost + '_' + app.deployedNodePort + '/siddhi-apps'}>

                            <div style={{height: 24, color: 'white'}}>
                                {app.deployedNodeHost + ":" + app.deployedNodePort}
                            </div>
                        </Link>
                    </TableRowColumn>
                )}
            </TableRow>
        );
    }

    static renderTableHeader() {
        return (
            <TableHeader displaySelectAll={false}
                         adjustForCheckbox={false}>
                <TableRow>
                    <TableHeaderColumn style={{color: '#f6f6f6', width: '100px'}}><h3>Siddhi
                        Application</h3>
                    </TableHeaderColumn>
                    <TableHeaderColumn style={{color: '#f6f6f6', width: '100px'}}><h3>Status</h3>
                    </TableHeaderColumn>
                    <TableHeaderColumn style={{color: '#f6f6f6', width: '100px'}}><h3>Deployed Time</h3>
                    </TableHeaderColumn>

                    <TableHeaderColumn style={{color: '#f6f6f6', width: '100px'}}><h3>Deployed
                        Node </h3>
                    </TableHeaderColumn>
                </TableRow>
            </TableHeader>
        )
    }

    static renderPageHeader() {
        return (
            <div>
                <Header/>
                <div className="navigation-bar">
                    <Button style={styles.navBtn}>
                        <HomeButton style={{paddingRight: 8, color: '#000'}}/>Overview</Button>
                </div>

                <Typography variant="display2" className={'node-title'}
                            style={{marginTop: '2%', color: '#dedede', marginLeft: '2%', backgroundColor: '#222222'}}>
                    App Overview</Typography>

                <div style={{marginTop: '-2%', marginRight: '5%'}}>
                    <Link style={{textDecoration: 'none', color: '#dedede', float: 'right'}}
                          to={window.contextPath}> |&nbsp;&nbsp; NODE VIEW</Link>

                    <Link style={{textDecoration: 'none', color: '#f17b31', float: 'right'}}
                          to={window.contextPath + "/siddhi-apps"}>
                        APP VIEW &nbsp;&nbsp; </Link>
                </div>
            </div>
        )
    }

    render() {
        if (SiddhiAppTable.hasNodes(this.state.appsList) && SiddhiAppTable.hasNodes(this.state.managerAppList)
            && SiddhiAppTable.hasNodes(this.state.haAppList)) {
            return (
                <div style={{backgroundColor: '#222222'}}>
                    {SiddhiAppTable.renderPageHeader()}

                    <Typography variant="headline" className={'app-title'} style={styles.h3Title}>Single Node
                        Deployment</Typography>
                    <Divider inset={true} style={styles.divider}/>
                    <div style={{padding: '40px'}}>

                        <Table>
                            {SiddhiAppTable.renderTableHeader()}
                            <TableBody displayRowCheckbox={false} style={{backgroundColor: '#131313'}}>
                                {this.state.data.map((row) => (
                                    SiddhiAppTable.renderRow(row)
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

                    <Typography variant="headline" className={'app-title'} style={styles.h3Title}>HA
                        Deployment</Typography>
                    <Divider inset={true} style={styles.divider}/>
                    <div style={{padding: '40px'}}>

                        <Table>
                            {SiddhiAppTable.renderTableHeader()}
                            <TableBody displayRowCheckbox={false} style={{backgroundColor: '#131313'}}>
                                {this.state.haAppData.map((row) => (
                                    SiddhiAppTable.renderRow(row)
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
                                total={Math.floor(this.state.haTotalSize / dataConstants.PAGE_LENGTH) + 1}
                                current={this.haCurrentPage}
                                display={dataConstants.PAGE_LENGTH}
                                onChange={number => {
                                    this.haCurrentPage = number;
                                    this.loadHAAppData(number, this.state.haAppList);
                                }}
                            />
                        </div>
                    </div>
                    <Typography variant="headline" className={'app-title'} style={styles.h3Title}>Distributed
                        Deployment</Typography>
                    <Divider inset={true} style={styles.divider}/>
                    <div style={{padding: '40px'}}>
                        <Table>
                            {SiddhiAppTable.renderTableHeader()}
                            <TableBody displayRowCheckbox={false} style={{backgroundColor: '#131313'}}>
                                {this.state.managerData.map((row) => (
                                    SiddhiAppTable.renderRow(row)
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
                                total={Math.floor(this.state.managerTotalSize / dataConstants.PAGE_LENGTH) + 1}
                                current={this.managerCurrentPage}
                                display={dataConstants.PAGE_LENGTH}
                                onChange={number => {
                                    this.managerCurrentPage = number;
                                    this.loadManagerData(number, this.state.managerAppList);
                                }}
                            />
                        </div>
                    </div>
                </div>
            );
        } else if (SiddhiAppTable.hasNodes(this.state.appsList) && !SiddhiAppTable.hasNodes(this.state.haAppList)
            && !SiddhiAppTable.hasNodes(this.state.managerAppList)) {
            return (
                <div style={{backgroundColor: '#222222'}}>
                    {SiddhiAppTable.renderPageHeader()}
                    <Typography variant="headline" className={'app-title'} style={styles.h3Title}>Single Node
                        Deployment</Typography>
                    <Divider inset={true} style={styles.divider}/>
                    <div style={{padding: '40px'}}>

                        <Table>
                            {SiddhiAppTable.renderTableHeader()}
                            <TableBody displayRowCheckbox={false} style={{backgroundColor: '#131313'}}>
                                {this.state.data.map((row) => (
                                    SiddhiAppTable.renderRow(row)
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
                </div>
            )

        } else if (!SiddhiAppTable.hasNodes(this.state.appsList) && !SiddhiAppTable.hasNodes(this.state.haAppList)
            && SiddhiAppTable.hasNodes(this.state.managerAppList)) {
            return (
                <div style={{backgroundColor: '#222222'}}>
                    {SiddhiAppTable.renderPageHeader()}
                    <Typography variant="headline" className={'app-title'} style={styles.h3Title}>Distributed
                        Deployment</Typography>
                    <Divider inset={true} style={styles.divider}/>
                    <div style={{padding: '40px'}}>

                        <Table>
                            {SiddhiAppTable.renderTableHeader()}

                            <TableBody displayRowCheckbox={false} style={{backgroundColor: '#131313'}}>
                                {this.state.managerData.map((row) => (
                                    SiddhiAppTable.renderRow(row)
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
                                total={Math.floor(this.state.managerTotalSize / dataConstants.PAGE_LENGTH) + 1}
                                current={this.managerCurrentPage}
                                display={dataConstants.PAGE_LENGTH}
                                onChange={number => {
                                    this.managerCurrentPage = number;
                                    this.loadManagerData(number, this.state.managerAppList);
                                }}
                            />
                        </div>
                    </div>
                </div>
            )

        } else if (!SiddhiAppTable.hasNodes(this.state.appsList) && !SiddhiAppTable.hasNodes(this.state.managerAppList)
            && SiddhiAppTable.hasNodes(this.state.haAppList)) {
            return (
                <div style={{backgroundColor: '#222222'}}>
                    {SiddhiAppTable.renderPageHeader()}
                    <Typography variant="headline" className={'app-title'} style={styles.h3Title}>HA
                        Deployment</Typography>
                    <Divider inset={true} style={styles.divider}/>
                    <div style={{padding: '40px'}}>

                        <Table>
                            {SiddhiAppTable.renderTableHeader()}
                            <TableBody displayRowCheckbox={false} style={{backgroundColor: '#131313'}}>
                                {this.state.haAppData.map((row) => (
                                    SiddhiAppTable.renderRow(row)
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
                                total={Math.floor(this.state.haTotalSize / dataConstants.PAGE_LENGTH) + 1}
                                current={this.haCurrentPage}
                                display={dataConstants.PAGE_LENGTH}
                                onChange={number => {
                                    this.haCurrentPage = number;
                                    this.loadHAAppData(number, this.state.haAppList);
                                }}
                            />
                        </div>
                    </div>
                </div>
            )

        } else if (SiddhiAppTable.hasNodes(this.state.appsList) && SiddhiAppTable.hasNodes(this.state.managerAppList)
            && !SiddhiAppTable.hasNodes(this.state.haAppList)) {
            return (
                <div style={{backgroundColor: '#222222'}}>
                    {SiddhiAppTable.renderPageHeader()}

                    <Typography variant="headline" className={'app-title'} style={styles.h3Title}>Single Node
                        Deployment</Typography>
                    <Divider inset={true} style={styles.divider}/>
                    <div style={{padding: '40px'}}>

                        <Table>
                            {SiddhiAppTable.renderTableHeader()}
                            <TableBody displayRowCheckbox={false} style={{backgroundColor: '#131313'}}>
                                {this.state.data.map((row) => (
                                    SiddhiAppTable.renderRow(row)
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

                    <Typography variant="headline" className={'app-title'} style={styles.h3Title}>Distributed
                        Deployment</Typography>
                    <Divider inset={true} style={styles.divider}/>
                    <div style={{padding: '40px'}}>

                        <Table>
                            {SiddhiAppTable.renderTableHeader()}
                            <TableBody displayRowCheckbox={false} style={{backgroundColor: '#131313'}}>
                                {this.state.managerData.map((row) => (
                                    SiddhiAppTable.renderRow(row)
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
                                total={Math.floor(this.state.managerTotalSize / dataConstants.PAGE_LENGTH) + 1}
                                current={this.managerCurrentPage}
                                display={dataConstants.PAGE_LENGTH}
                                onChange={number => {
                                    this.managerCurrentPage = number;
                                    this.loadManagerData(number, this.state.managerAppList);
                                }}
                            />
                        </div>
                    </div>
                </div>
            );
        } else if (SiddhiAppTable.hasNodes(this.state.appsList) && !SiddhiAppTable.hasNodes(this.state.managerAppList)
            && SiddhiAppTable.hasNodes(this.state.haAppList)) {
            return (
                <div style={{backgroundColor: '#222222'}}>
                    {SiddhiAppTable.renderPageHeader()}

                    <Typography variant="headline" className={'app-title'} style={styles.h3Title}>Single Node
                        Deployment</Typography>
                    <Divider inset={true} style={styles.divider}/>
                    <div style={{padding: '40px'}}>

                        <Table>
                            {SiddhiAppTable.renderTableHeader()}
                            <TableBody displayRowCheckbox={false} style={{backgroundColor: '#131313'}}>
                                {this.state.data.map((row) => (
                                    SiddhiAppTable.renderRow(row)
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

                    <Typography variant="headline" className={'app-title'} style={styles.h3Title}>HA
                        Deployment</Typography>
                    <Divider inset={true} style={styles.divider}/>
                    <div style={{width: '90%', padding: '40px'}}>

                        <Table>
                            {SiddhiAppTable.renderTableHeader()}
                            <TableBody displayRowCheckbox={false} style={{backgroundColor: '#131313'}}>
                                {this.state.haAppData.map((row) => (
                                    SiddhiAppTable.renderRow(row)
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
                                total={Math.floor(this.state.haTotalSize / dataConstants.PAGE_LENGTH) + 1}
                                current={this.haCurrentPage}
                                display={dataConstants.PAGE_LENGTH}
                                onChange={number => {
                                    this.haCurrentPage = number;
                                    this.loadHAAppData(number, this.state.haAppList);
                                }}
                            />
                        </div>
                    </div>
                </div>
            );

        } else if (!SiddhiAppTable.hasNodes(this.state.appsList) && SiddhiAppTable.hasNodes(this.state.managerAppList)
            && SiddhiAppTable.hasNodes(this.state.haAppList)) {
            return (
                <div style={{backgroundColor: '#222222'}}>
                    {SiddhiAppTable.renderPageHeader()}

                    <Typography variant="headline" className={'app-title'} style={styles.h3Title}>HA
                        Deployment</Typography>
                    <Divider inset={true} style={styles.divider}/>
                    <div style={{padding: '40px'}}>

                        <Table>
                            {SiddhiAppTable.renderTableHeader()}
                            <TableBody displayRowCheckbox={false} style={{backgroundColor: '#131313'}}>
                                {this.state.haAppData.map((row) => (
                                    SiddhiAppTable.renderRow(row)
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
                                total={Math.floor(this.state.haTotalSize / dataConstants.PAGE_LENGTH) + 1}
                                current={this.haCurrentPage}
                                display={dataConstants.PAGE_LENGTH}
                                onChange={number => {
                                    this.haCurrentPage = number;
                                    this.loadHAAppData(number, this.state.haAppList);
                                }}
                            />
                        </div>
                    </div>
                    <Typography variant="headline" className={'app-title'} style={styles.h3Title}>Distributed
                        Deployment</Typography>
                    <Divider inset={true} style={styles.divider}/>
                    <div style={{padding: '40px'}}>

                        <Table>
                            {SiddhiAppTable.renderTableHeader()}
                            <TableBody displayRowCheckbox={false} style={{backgroundColor: '#131313'}}>
                                {this.state.managerData.map((row) => (
                                    SiddhiAppTable.renderRow(row)
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
                                total={Math.floor(this.state.managerTotalSize / dataConstants.PAGE_LENGTH) + 1}
                                current={this.managerCurrentPage}
                                display={dataConstants.PAGE_LENGTH}
                                onChange={number => {
                                    this.managerCurrentPage = number;
                                    this.loadManagerData(number, this.state.managerAppList);
                                }}
                            />
                        </div>
                    </div>
                </div>
            );
        } else {
            return (
                <div style={{backgroundColor: '#222222'}}>
                    {SiddhiAppTable.renderPageHeader()}
                    <div>
                        <div className="center-wrapper" style={{margin: '100px 0'}}>
                            <div className="info-card">
                                <Typography style={styles.alignCenter}>
                                    <Info/>
                                    There is no siddhi apps deployed
                                </Typography>
                            </div>
                        </div>
                    </div>

                </div>
            )
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
}
