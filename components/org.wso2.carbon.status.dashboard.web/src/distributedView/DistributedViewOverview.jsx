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
import PropTypes from 'prop-types';
//Material UI
import {Typography} from 'material-ui-next';
import {GridList} from 'material-ui/GridList';
import Info from 'material-ui/svg-icons/action/info';
import HomeButton from 'material-ui/svg-icons/action/home';
import {Card, CardText, Divider, FlatButton, RaisedButton} from 'material-ui';
//App Components
import DistributedViewAppThumbnail from './DistributedViewAppThumbnail';
import StatusDashboardAPIS from '../utils/apis/StatusDashboardAPIs';
import Header from '../common/Header';
import AuthenticationAPI from '../utils/apis/AuthenticationAPI';
import AuthManager from '../auth/utils/AuthManager';
import {Redirect} from 'react-router-dom';
import FormPanel from '../common/FormPanel';
import Error500 from '../error-pages/Error500';
import {HttpStatus} from '../utils/Constants';
// Localization
import { FormattedMessage } from 'react-intl';

const styles = {
    root: {display: 'flex', flexWrap: 'wrap', justifyContent: 'space-around', backgroundColor: '#222222'},
    gridList: {width: '100%', padding: '40px 24px', margin: 0},
    h3: {color: '#dedede', marginLeft: '24px', fontWeight: '400'},
    titleStyle: {fontSize: 18, lineHeight: 1.5, color: '#FF3D00'},
    headerStyle: {height: 30, backgroundColor: '#242424'},
    paper: {height: 50, width: 500, textAlign: 'center'},
    background: {backgroundColor: '#222222'},
    divider: {
        backgroundColor: '#9E9E9E',
        width: 'calc(100% - 48px)',
        margin: '-10px 24px 0 24px',
        marginLeft: '25px',
        marginTop: '10px'
    },
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
export default class DistributedOverview extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            sessionInvalid: false,
            id: this.props.match.params.id,
            clustersList: {},
            pInterval: window.localStorage.getItem("pInterval") != null ? parseInt(window.localStorage.getItem("pInterval")) : 5,
            currentTime: '',
            interval: '',
            isApiCalled: false,
            counter: 0,
            hasManagerPermission: false,
            hasViewPermission: true,
            statusMessage: this.context.intl.formatMessage({ id: 'distributedOverview', defaultMessage: 'Currently there are no siddhi application to display' }),
            isError: false

        };
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
        StatusDashboardAPIS.getManagerSiddhiApps(this.props.match.params.id)
            .then((response) => {
                if (response.status = HttpStatus.OK) {
                    this.setState({
                        clustersList: response.data,
                        isApiCalled: true,
                        statusMessage: response.data === null ? this.context.intl.formatMessage({ id: 'distributedOverview.currentyNoApps', defaultMessage: 'Currently there are no apps to display' }) : ''
                    });
                }
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
    }

    componentWillUnmount() {
        clearInterval(this.state.interval);
    }

    componentWillMount() {
        let that = this;
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
     * Method which render workers
     * @param workersList
     * @returns {XML}
     */
    renderWorkers(appList) {
        if (this.state.isApiCalled && !DistributedOverview.hasApps(this.state.clustersList)) {
            if (this.state.hasViewPermission) {
                return (
                    <div style={styles.background}>
                        <div className="info-card" style={{backgroundColor: '#f17b31'}}>
                            <FlatButton
                                label={<FormattedMessage id='distributedOverview.noDeployedSiddhiApps' defaultMessage='Currently there are no siddhi apps deployed in the node' />}
                                icon={<Info/>}
                                style={{marginTop: 10, backgroundColor: '#f17b31'}}
                            />
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
                            <CardText style={{borderBottom: '1px solid #AE5923', borderTop: '1px solid #AE5923'}}>
                                <FormPanel title={""} width={650}>
                                    <div style={errorContainerStyles}>
                                        <i class="fw fw-security fw-inverse fw-5x"></i>
                                        <h1 style={errorTitleStyles}><FormattedMessage id='distributedOverview.pageForbidden' defaultMessage='Page Forbidden!' /></h1>
                                        <text style={errorMessageStyles}><FormattedMessage id='distributedOverview.noPermission' defaultMessage='You have no permission to access this page.' />
                                        </text>
                                        <br/> <br/>
                                        <Link to={`${window.contextPath}/logout`}>
                                            <RaisedButton backgroundColor='#f17b31' style={buttonStyle} label={<FormattedMessage id='distributedOverview.login' defaultMessage='Login' />} />
                                        </Link>
                                    </div>
                                </FormPanel>
                            </CardText>
                        </Card>
                    </div>
                );
            }
        } else if (this.state.isApiCalled && DistributedOverview.hasApps(this.state.clustersList)) {
            return <div style={styles.background}>
                <div style={{padding: '24px'}}>
                    <Typography variant="display2" className={'node-title'} style={{
                        marginTop: '20px',
                        color: '#dedede',
                        marginLeft: '24px',
                        backgroundColor: '#222222',
                        fontSize: '1.6rem'
                    }}><FormattedMessage id='distributedOverview.parentSiddhiApp' defaultMessage='Parent Siddhi Application' /></Typography>
                    <Divider inset={true} style={styles.divider}/>
                    <div style={styles.root}>
                        <GridList className={'node-wrapper'} cols={3} cellHeight='100%'
                                  style={styles.gridList}>
                            {Object.keys(appList).map(key => {
                                return (

                                    <DistributedViewAppThumbnail distributedApp={appList[key]}
                                                                 currentTime={new Date().getTime()}/>
                                )
                            })}
                        </GridList>
                    </div>
                </div>


            </div>;
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
                <div style={styles.background}>
                    <Header/>
                    <div className="navigation-bar">
                        <Link to={window.contextPath}> <FlatButton label={<FormattedMessage id='overview' defaultMessage='Overview >' />} icon={<HomeButton color="black" />} /></Link>
                        {/*<Link to={window.contextPath + '/worker/' + this.props.match.params.id }>*/}

                        <RaisedButton label={this.props.match.params.id} disabled disabledLabelColor='white'
                                      disabledBackgroundColor='#f17b31'/>

                    </div>
                    {this.renderWorkers(this.state.clustersList)}
                </div>
            );
        } else {
            return (
                <Redirect to={{pathname: `${window.contextPath}/logout`}}/>
            );
        }
    }

    static hasApps(clusters) {
        for (let prop in clusters) {

            if (clusters.hasOwnProperty(prop)) {
                return true;
            }
        }
        return false;
    }
}

DistributedOverview.contextTypes = {
    intl: PropTypes.object.isRequired

}
