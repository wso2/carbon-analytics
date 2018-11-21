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
import {Link, Redirect} from 'react-router-dom';
import PropTypes from 'prop-types';
//Material UI
import {GridList, Toggle} from 'material-ui';
import Info from 'material-ui/svg-icons/action/info';
import HomeButton from 'material-ui/svg-icons/action/home';
import Sync from 'material-ui/svg-icons/notification/sync';
import SyncDisabled from 'material-ui/svg-icons/notification/sync-disabled';
import {Button, Card, Divider, IconButton, Tab, Typography} from 'material-ui-next';
import ContentAdd from 'material-ui/svg-icons/content/add';
//App Components
import WorkerThumbnail from './WorkerThumbnail';
import StatusDashboardAPIS from '../utils/apis/StatusDashboardAPIs';
import Header from '../common/Header';
import AuthenticationAPI from '../utils/apis/AuthenticationAPI';
import AuthManager from '../auth/utils/AuthManager';
import StatusDashboardOverViewAPI from '../utils/apis/StatusDashboardOverViewAPI';
import FormPanel from '../common/FormPanel';
import Error500 from '../error-pages/Error500';
import {HttpStatus} from '../utils/Constants';
import ManagerThumbnail from './ManagerThumbnail';
import '../../public/css/dashboard.css';
//Localization
import { FormattedMessage } from 'react-intl';

const styles = {
    root: {display: 'flex', flexWrap: 'wrap', justifyContent: 'space-around', backgroundColor: '#222222'},
    gridList: {width: '100%', padding: '40px 24px', margin: 0},
    h3Title: {color: '#dedede', marginLeft: '24px', fontWeight: '400'},
    titleStyle: {fontSize: '1.6rem', margin: '20px 0 0 24px', color: '#dedede'},
    headerStyle: {height: 30, backgroundColor: '#242424'},
    paper: {height: 50, width: 500, textAlign: 'center'},
    divider: {backgroundColor: '#9E9E9E', width: 'calc(100% - 48px)', margin: '-16px 24px 0 24px'},
    navBar: {padding: '0 15px'},
    navBtn: {color: '#BDBDBD', padding: '0 10px', verticalAlign: 'middle', textTransform: 'capitalize'},
    alignCenter: {display: 'flex', alignItems: 'center'},
    addBtn: {
        backgroundColor: '#f17b31',
        padding: 20,
        color: '#fff'
    }
};
const errorTitleStyles = {
    color: "#c7cad1",
    fontSize: 45
};

const errorMessageStyles = {
    color: "#abaeb4",
    fontSize: 22
};
const errorContainerStyles = {
    textAlign: "center",
    marginTop: 30
};
const buttonStyle = {marginLeft: 50, width: '35%', fontSize: '12px', backgroundColor: '#f17b31'};
/**
 * class which manages overview page.
 */
export default class WorkerOverview extends React.Component {
    constructor() {
        super();
        this.state = {
            sessionInvalid: false,
            clustersList: {},
            managerClusterList: {},
            pInterval: window.localStorage.getItem("pInterval") != null ?
                parseInt(window.localStorage.getItem("pInterval")) : 5,
            currentTime: '',
            interval: '',
            enableAutoSync: window.localStorage.getItem("enableAutoSync") != null ?
                ((window.localStorage.getItem("enableAutoSync")) === 'true') : false,
            isApiCalled: false,
            isManagerApiCalled: false,
            counter: 0,
            hasManagerPermission: false,
            hasViewPermission: true,
            statusMessage: (
                <FormattedMessage
                    id="workerOverview.noNodes"
                    defaultMessage="Currently there are no nodes to display"
                />),
            isError: false,
            btnType: <SyncDisabled color='#BDBDBD'/>

        };
        this.autoSync = this.autoSync.bind(this);
        this.initAutoSync = this.initAutoSync.bind(this);
        this.renderWorkers = this.renderWorkers.bind(this);
    }

    componentDidMount() {
        StatusDashboardAPIS.getDashboardConfig()
            .then((response) => {
                this.setState({
                    pInterval: response.data.pollingInterval,
                    counter: this.state.counter
                });
            }).catch((error) => {
            if (error.response != null) {
                if (error.response.status === 401) {
                    this.setState({
                        sessionInvalid: true,
                            statusMessage: this.context.intl.formatMessage({ id: 'authenticationFail', defaultMessage: 'Authentication fail. Please login again.' }),
                        isApiCalled: true
                    })
                } else if (error.response.status === 403) {
                    this.setState({
                        hasViewPermission: false,
                            statusMessage: this.context.intl.formatMessage({ id: 'noViewPermission', defaultMessage: 'User Have No Permission to view this page.' }),
                        isApiCalled: true
                    })
                } else {
                    this.setState({
                        isError: true,
                            statusMessage: this.context.intl.formatMessage({ id: 'unknownError', defaultMessage: 'Unknown error occurred! : {data}', values: { data: JSON.stringify(error.response.data) } }),
                        isApiCalled: true
                    })
                }
            }
        });

        this.setState({currentTime: new Date().getTime()});
        StatusDashboardOverViewAPI.getWorkersList()
            .then((response) => {
                this.setState({
                    clustersList: response.data,
                    isApiCalled: true,
                    statusMessage: !WorkerOverview.hasNodes(this.state.clustersList) ? this.context.intl.formatMessage({ id: 'workerOverview.noNodes', defaultMessage: 'Currently there are no nodes to display' }) : ''
                });
            }).catch((error) => {
            if (error.response != null) {
                if (error.response.status === 401) {
                    this.setState({
                        isApiCalled: true,
                        sessionInvalid: true,
                            statusMessage: this.context.intl.formatMessage({ id: 'authenticationFail', defaultMessage: 'Authentication fail. Please login again.' })
                    })
                } else if (error.response.status === 403) {
                    this.setState({
                        isApiCalled: true,
                            statusMessage: this.context.intl.formatMessage({ id: 'noViewPermission', defaultMessage: 'User Have No Permission to view this page.' })
                    });
                } else {
                    this.setState({
                        isError: true,
                        isApiCalled: true,
                            statusMessage: this.context.intl.formatMessage({ id: 'unknownError', defaultMessage: 'Unknown error occurred! : {data}', values: { data: JSON.stringify(error.response.data) } })
                    });
                }
            }
        });

        StatusDashboardOverViewAPI.getManagerList()
            .then((response) => {
                if (response.status === HttpStatus.OK) {
                    this.setState({
                        managerClusterList: response.data,
                        isManagerApiCalled: true,
                        statusMessage: !WorkerOverview.hasNodes(this.state.managerClusterList) ?
                            this.context.intl.formatMessage({ id: 'workerOverview.noNodes', defaultMessage: 'Currently there are no nodes to display' }) : ''
                    });
                } else {
                    console.log("manager connection failed")
                }

            }).catch((error) => {
            if (error.response != null) {
                if (error.response.status === 401) {
                    this.setState({
                        isManagerApiCalled: true,
                        sessionInvalid: true,
                            statusMessage: this.context.intl.formatMessage({ id: 'authenticationFail', defaultMessage: 'Authentication fail. Please login again.' })
                    })
                } else if (error.response.status === 403) {
                    this.setState({
                        isManagerApiCalled: true,
                        statusMessage: "User Have No Permission to view this page."
                    })
                } else {
                    this.state({
                        isError: true,
                        isManagerApiCalled: true,
                            statusMessage: this.context.intl.formatMessage({ id: 'unknownError', defaultMessage: 'Unknown error occurred! : {data}', values: { data: JSON.stringify(error.response.data) } })
                    });
                }
            }
        });
    }

    componentWillUnmount() {
        clearInterval(this.state.interval);
    }

    componentWillMount() {
        let that = this;
        this.initAutoSync();
        AuthenticationAPI.isUserAuthorized('manager', AuthManager.getUser().SDID)
            .then((response) => {
                that.setState({
                    hasManagerPermission: response.data
                });
            })
            .catch((error) => {
                if (error.response.status === 401) {
                    this.setState({
                        isApiCalled: true,
                        sessionInvalid: true,
                        statusMessage: this.context.intl.formatMessage({ id: 'authenticationFail', defaultMessage: 'Authentication fail. Please login again.' })
                    })
                } else {
                    this.setState({
                        isError: true,
                        isApiCalled: true,
                        statusMessage: this.context.intl.formatMessage({ id: 'unknownError', defaultMessage: 'Unknown error occurred! : {data}', values: { data: JSON.stringify(error.response.data) } })
                    });
                }
            });
    }

    /**
     * Method which render add worker button if permission is granted
     * @param workersList
     * @returns {XML}
     */
    renderAddWorker() {
        if (this.state.hasManagerPermission) {
            return (
                <div>
                    <Link style={{textDecoration: 'none'}} to={window.contextPath + '/add-worker'}>
                        <Button className={'add-button'} style={styles.addBtn}>
                            <ContentAdd /><FormattedMessage id='workerOveriew.addNewNode' defaultMessage='Add New Node' />
                        </Button>
                    </Link>
                </div>
            )
        } else {
            return (
                <div className="add-button-disabled">
                    <Button
                        icon={<ContentAdd/>}
                        style={{marginTop: 10, display: 'none'}}
                    />
                </div>
            )
        }
    }

    /**
     * Method which render add worker flotting button if permission is granted
     * @param workersList
     * @returns {XML}
     */
    renderAddWorkerFlotting() {
        if (this.state.hasManagerPermission) {
            return (
                <div className="floating-button">
                    <Link to={window.contextPath + '/add-worker'}>
                        <Button variant="fab" style={{backgroundColor: '#f17b31'}}>
                            <ContentAdd/>
                        </Button>
                    </Link>
                </div>
            )
        } else {
            return (
                <div className="floating-button">
                    <Button variant="fab" backgroundColor='#f17b31'
                            style={{marginTop: 10, display: 'none'}}>
                    </Button>
                </div>
            )
        }
    }

    /**
     * Method which handles auto sync button submit
     */
    initAutoSync() {
        let interval = '';
        let that = this;
        if (this.state.enableAutoSync) {
            interval = setInterval(() => {
                // that.setState({currentTime: new Date().getTime()});
                StatusDashboardOverViewAPI.getWorkersList()
                    .then((response) => {
                        that.setState({clustersList: response.data});
                    }).catch((error) => {
                });

                StatusDashboardOverViewAPI.getManagerList()
                    .then((response) => {
                        that.setState({managerClusterList: response.data});
                    }).catch((error) => {

                });
            }, parseInt(this.state.pInterval * 1000));
            this.setState({interval: interval});
        }
    }

    /**
     * Method which handles auto sync button submit
     */
    autoSync() {
        let interval;
        let that = this;
        if (!this.state.enableAutoSync) {
            interval = setInterval(() => {
                // that.setState({currentTime: new Date().getTime()});
                StatusDashboardOverViewAPI.getWorkersList()
                    .then((response) => {
                        that.setState({clustersList: response.data});
                    }).catch((error) => {
                    //TODO Need to use proper notification library to show the error
                });

                StatusDashboardOverViewAPI.getManagerList()
                    .then((response) => {
                        that.setState({managerClusterList: response.data});
                    }).catch((error) => {

                });
            }, parseInt(this.state.pInterval * 1000));
            this.setState({interval: interval, enableAutoSync: true, btnType: <Sync color='#f17b31'/>});
            window.localStorage.setItem("enableAutoSync", true);
            window.localStorage.setItem("pInterval", this.state.pInterval)
        } else {
            clearInterval(this.state.interval);
            this.setState({enableAutoSync: false, btnType: <SyncDisabled color='#BDBDBD'/>});
            window.localStorage.setItem("enableAutoSync", false)
        }
    }

    /**
     * Method which render workers
     * @param workersList
     * @param managerList
     * @returns {XML}
     */
    renderWorkers(workersList, managerList) {

        if (this.state.isApiCalled && this.state.isManagerApiCalled && !WorkerOverview.hasNodes(this.state.clustersList) &&
            !WorkerOverview.hasNodes(this.state.managerClusterList)) {
            if (this.state.hasViewPermission) {
                return (
                    <div>
                        <div className="center-wrapper">
                            <div className="info-card">
                                <Typography style={styles.alignCenter}>
                                    <Info/>
                                    {this.state.statusMessage}
                                </Typography>
                            </div>
                        </div>
                        <div className="center-wrapper">
                            {this.renderAddWorker()}
                        </div>
                    </div>
                );
            } else {
                return (
                    <div style={styles.background}>
                        <Card style={{
                            width: 700, high: '100%', marginTop: '10%', marginLeft: '33%', backgroundColor: '#1a1a1a',
                            borderColor: '#f17b31', borderRadius: 2, borderBottomColor: '#f17b31'
                        }}>
                            <div style={{borderBottom: '1px solid #AE5923', borderTop: '1px solid #AE5923'}}>
                                <FormPanel title={""} width={650}>
                                    <div style={errorContainerStyles}>
                                        <i class="fw fw-security fw-inverse fw-5x"></i>
                                        <h1 style={errorTitleStyles}><FormattedMessage id='workerOverview.pageForbidden' defaultMessage='Page Forbidden!' /></h1>
                                        <text style={errorMessageStyles}>
                                            <FormattedMessage id='workerOverview.noPermisison' defaultMessage='You have no permission to access this page.' />
                                        </text>
                                        <br/>
                                        <br/>
                                        <Link to={`${window.contextPath}/logout`}>
                                            <Button variant="raised" backgroundColor='#f17b31'
                                                style={buttonStyle}><FormattedMessage id='workerOverview.login' defaultMessage='Login' /></Button>
                                        </Link>
                                    </div>
                                </FormPanel>
                            </div>
                        </Card>
                    </div>
                );
            }
        } else if (this.state.isApiCalled && this.state.isManagerApiCalled && ((WorkerOverview.hasNodes(this.state.clustersList)) &&
                (WorkerOverview.hasNodes(this.state.managerClusterList)))) {
            return (

                <div>
                    <div style={{padding: 20}}>
                        {this.renderAddWorkerFlotting()}
                        <div className="toggle">
                            <Button style={styles.navBtn} onClick={() => {
                                this.autoSync();
                            }}>
                                {this.state.btnType}
                                <Typography style={{
                                    color: '#E0E0E0'
                                }}><FormattedMessage id='workerOverview.autoSync' defaultMessage='Auto-Sync' /></Typography>
                            </Button>

                        </div>
                    </div>

                    {Object.keys(workersList).map((id, workerList) => {
                        if (id !== "Single Node Deployments" && id !== "Never Reached" && id !== " ") {
                            return (
                                <div>
                                    <Typography variant="headline" className={'app-title'} style={{
                                        color: '#dedede',
                                        marginLeft: '24px',
                                        fontWeight: '400',
                                        fontSize: '1.25rem'
                                    }}>
                                        <FormattedMessage id='workerOverview.haDeployments' defaultMessage='HA Deployments' />
                                    </Typography>
                                    <h3 style={styles.h3Title}>
                                        <FormattedMessage id='workerOverview.groupId' defaultMessage='Group Id:' /> {id}
                                    </h3>
                                    <Divider inset={true} style={styles.divider} />
                                    <div style={styles.root}>
                                        <GridList className={'node-wrapper'} cols={3} cellHeight='100%'
                                                  style={styles.gridList}>
                                            {workersList[id].map((worker) => {
                                                return (
                                                    <WorkerThumbnail worker={worker}
                                                                     currentTime={new Date().getTime()}/>
                                                )
                                            })}
                                        </GridList>
                                    </div>
                                </div>
                            )

                        } else {
                            return (
                                <div>
                                    <h3 style={styles.h3Title}>{id}</h3>
                                    <Divider inset={true} style={styles.divider}/>
                                    <div style={styles.root}>
                                        <GridList className={'node-wrapper'} cols={3} cellHeight='100%'
                                                  style={styles.gridList}>
                                            {workersList[id].map((worker) => {
                                                return (
                                                    <WorkerThumbnail worker={worker}
                                                                     currentTime={new Date().getTime()}/>
                                                )
                                            })}
                                        </GridList>
                                    </div>
                                </div>
                            )
                        }

                    })}

                    <Typography variant="headline" className={'app-title'}
                                style={{color: '#dedede', marginLeft: '24px', fontWeight: '400', fontSize: '1.25rem'}}>Distributed
                        <FormattedMessage id='workerOverview.distributedDeployments' defaultMessage='Distributed Deployments' />
                    </Typography>

                    {Object.keys(managerList).map((id, workerList) => {
                        if (id !== "Never Reached" && id !== "Not-Reachable") {
                            return (
                                <div>
                                    <h3 style={styles.h3Title}>
                                        <FormattedMessage id='workerOverview.groupId' defaultMessage='Group Id: ' />{id}
                                    </h3>
                                    <Divider inset={true} style={styles.divider}/>
                                    <h4 style={styles.h3Title}>
                                        <FormattedMessage id='workerOverview.managers' defaultMessage='Managers' />
                                    </h4>
                                    <div style={styles.root}>
                                        <GridList className={'node-wrapper'} cols={3} cellHeight='100%'
                                                  style={styles.gridList}>
                                            {managerList[id].map((worker) => {
                                                return (
                                                    <ManagerThumbnail worker={worker}
                                                                      currentTime={new Date().getTime()}/>
                                                )
                                            })}

                                        </GridList>
                                    </div>
                                </div>
                            )
                        } else {
                            return (
                                <div>
                                    <h3 style={styles.h3Title}>
                                        <FormattedMessage id='workerOverview.managers' defaultMessage='Managers' />
                                    </h3>
                                    <Divider inset={true} style={styles.divider} />
                                    <div style={styles.root}>
                                        <GridList className={'node-wrapper'} cols={3} cellHeight='100%'
                                                  style={styles.gridList}>
                                            {managerList[id].map((worker) => {

                                                return (
                                                    <ManagerThumbnail worker={worker}
                                                                      currentTime={new Date().getTime()}/>
                                                )
                                            })}
                                        </GridList>
                                    </div>
                                </div>
                            )
                        }

                    })}
                </div>
            );
        } else if (this.state.isApiCalled && this.state.isManagerApiCalled && ((WorkerOverview.hasNodes(this.state.clustersList))) &&
            (!WorkerOverview.hasNodes(this.state.managerClusterList))) {
            return (
                <div style={styles.background}>
                    <div style={{padding: 20}}>
                        {this.renderAddWorkerFlotting()}
                        <div className="toggle">
                            <Button style={styles.navBtn} onClick={() => {
                                this.autoSync();
                            }}>
                                {this.state.btnType}
                                <Typography style={{
                                    color: '#E0E0E0'
                                }}>
                                    <FormattedMessage id='workerOverview.autoSync' defaultMessage='Auto-Sync' />
                                </Typography>
                            </Button>
                        </div>
                    </div>

                    {Object.keys(workersList).map((id, workerList) => {
                        if (id !== "Single Node Deployments" && id !== "Never Reached" && id !== " ") {
                            return (
                                <div>
                                    <Typography variant="headline" className={'app-title'} style={{
                                        color: '#dedede',
                                        marginLeft: '24px',
                                        fontWeight: '400',
                                        fontSize: '1.25rem'
                                    }}>
                                        <FormattedMessage id='workerOverview.haDeployments' defaultMessage='HA Deployments' />
                                    </Typography>
                                    <h3 style={styles.h3Title}>{id}</h3>
                                    <Divider inset={true} style={styles.divider}/>
                                    <div style={styles.root}>
                                        <GridList className={'node-wrapper'} cols={3} cellHeight='100%'
                                                  style={styles.gridList}>
                                            {workersList[id].map((worker) => {
                                                return (
                                                    <WorkerThumbnail worker={worker}
                                                                     currentTime={new Date().getTime()}/>
                                                )
                                            })}
                                        </GridList>
                                    </div>
                                </div>
                            )

                        } else {
                            return (
                                <div>
                                    <h3 style={styles.h3Title}>{id}</h3>
                                    <Divider inset={true} style={styles.divider}/>
                                    <div style={styles.root}>
                                        <GridList className={'node-wrapper'} cols={3} cellHeight='100%'
                                                  style={styles.gridList}>
                                            {workersList[id].map((worker) => {
                                                return (
                                                    <WorkerThumbnail worker={worker}
                                                                     currentTime={new Date().getTime()}/>
                                                )
                                            })}
                                        </GridList>
                                    </div>
                                </div>
                            )
                        }
                    })}
                </div>
            );
        } else if (this.state.isApiCalled && this.state.isManagerApiCalled && (WorkerOverview.hasNodes(this.state.managerClusterList)) &&
            (!WorkerOverview.hasNodes(this.state.clustersList))) {
            return (
                <div style={styles.background}>
                    <div style={{padding: 20}}>
                        {this.renderAddWorkerFlotting()}
                        <div className="toggle">
                            <Button style={styles.navBtn} onClick={() => {
                                this.autoSync();
                            }}>
                                {this.state.btnType}
                                <Typography style={{
                                    color: '#E0E0E0'
                                }}>
                                    <FormattedMessage id='workerOverview.autoSync' defaultMessage='Auto-Sync' />
                                </Typography>
                            </Button>
                        </div>
                    </div>
                    <Typography variant="headline" className={'app-title'}
                        style={{ color: '#dedede', marginLeft: '24px', fontWeight: '400', fontSize: '1.25rem' }}>
                        <FormattedMessage id='workerOverview.distributedDeployments' defaultMessage='Distributed Deployments' />
                    </Typography>

                    {Object.keys(managerList).map((id, workerList) => {
                        if (id !== "Never Reached" && id !== "Not-Reachable") {
                            return (
                                <div>
                                    <h3 style={styles.h3Title}>
                                        <FormattedMessage id='workerOverview.groupId' defaultMessage='Group Id:' />  {id}
                                    </h3>
                                    <Divider inset={true} style={styles.divider}/>
                                    <h4 style={styles.h3Title}>
                                        <FormattedMessage id='workerOverview.managers' defaultMessage='Managers' />
                                    </h4>
                                    <div style={styles.root}>
                                        <GridList className={'node-wrapper'} cols={3} cellHeight='100%'
                                                  style={styles.gridList}>
                                            {managerList[id].map((worker) => {
                                                return (
                                                    <ManagerThumbnail worker={worker}
                                                                      currentTime={new Date().getTime()}/>
                                                )
                                            })}

                                        </GridList>
                                    </div>

                                </div>
                            )
                        } else {
                            return (
                                <div>
                                    <h3 style={styles.h3Title}>Managers</h3>
                                    <Divider inset={true} style={styles.divider}/>
                                    <div style={styles.root}>
                                        <GridList className={'node-wrapper'} cols={3} cellHeight='100%'
                                                  style={styles.gridList}>
                                            {managerList[id].map((worker) => {
                                                return (
                                                    <ManagerThumbnail worker={worker}
                                                                      currentTime={new Date().getTime()}/>
                                                )
                                            })}
                                        </GridList>
                                    </div>
                                </div>
                            )
                        }
                    })}
                </div>
            );
        } else {
            return (
                <div style={{backgroundColor: '#222222', width: '100%', height: '1000px'}} data-toggle="loading"
                     data-loading-inverse="true">
                    <div style={{
                        textAlign: 'center',
                        paddingTop: '200px'
                    }}>
                        <i className="fw fw-loader5 fw-spin fw-inverse fw-5x"></i>
                    </div>

                </div>
            );
        }
    }

    render() {
        if (this.state.isError) {
            return <Error500 message={this.state.statusMessage}/>;
        }
        if (!this.state.sessionInvalid) {
            return (
                <div>
                    <Header/>
                    <div style={styles.navBar} className="navigation-bar">
                        <Button style={styles.navBtn}>
                            <HomeButton style={{paddingRight: 8, color: '#BDBDBD'}}/>
                            <FormattedMessage id='workerOverview.overview' defaultMessage='Overview' />
                        </Button>
                    </div>
                    <Typography variant="title" style={styles.titleStyle}>
                        <FormattedMessage id='workerOverview.nodeOverview' defaultMessage='Node Overview' />
                    </Typography>
                    <div style={{marginTop: '-26px', marginRight: '24px', fontSize: '0.875rem'}}>
                        <Link style={{textDecoration: 'none', color: '#f17b31', float: 'right', paddingLeft: 10}}
                            to={window.contextPath}>
                            <FormattedMessage id='workerOverview.nodeView' defaultMessage='Node View' />
                        </Link>

                        <Typography style={{float: 'right', color: '#757575'}}>|</Typography>

                        <Link style={{textDecoration: 'none', color: '#dedede', float: 'right', paddingRight: 10}}
                              to={window.contextPath + "/siddhi-apps"}>
                            <FormattedMessage id='workerOverview.appView' defaultMessage='App view' />
                        </Link>
                    </div>
                    {this.renderWorkers(this.state.clustersList, this.state.managerClusterList)}
                </div>
            );
        } else {
            return (
                <Redirect to={{pathname: `${window.contextPath}/logout`}}/>
            );
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

WorkerOverview.contextTypes = {
  intl: PropTypes.object.isRequired
};
