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
import {Link, Redirect} from 'react-router-dom';
// App Components
import StatusDashboardAPIS from '../utils/apis/StatusDashboardAPIs';
import {HttpStatus} from '../utils/Constants';
import Header from '../common/Header';
import '../../public/css/dashboard.css';
// Material UI
import HomeButton from 'material-ui/svg-icons/action/home';
import {Dialog, FlatButton, RaisedButton, Snackbar, TextField} from 'material-ui';
import {Button, Typography} from 'material-ui-next';
//Localization
import PropTypes from 'prop-types';
// CSS
import '../../public/css/dashboard.css';
import AuthenticationAPI from '../utils/apis/AuthenticationAPI';
import AuthManager from '../auth/utils/AuthManager';
import FormPanel from '../common/FormPanel';
import {darkBaseTheme, getMuiTheme, MuiThemeProvider} from 'material-ui/styles';
import Error403 from '../error-pages/Error403';
import StatusDashboardOverViewAPI from '../utils/apis/StatusDashboardOverViewAPI';
import { FormattedMessage } from 'react-intl';

const muiTheme = getMuiTheme(darkBaseTheme);
const messageBoxStyle = {textAlign: "center", color: "white"};
const errorMessageStyle = {backgroundColor: "#FF5722", color: "white"};
const successMessageStyle = {backgroundColor: "#4CAF50", color: "white"};
const buttonStyle = {marginLeft: 10, width: '30%', fontSize: '8px'};
const popupButtonStyle = {marginLeft: 25, width: '30%', fontSize: '8px'};
const textField = {width: 650};

const styles = {
    root: {display: 'flex', flexWrap: 'wrap', justifyContent: 'space-around', backgroundColor: '#222222'},
    gridList: {width: '100%', padding: '40px 24px', margin: 0},
    h3Title: {color: '#dedede', marginLeft: '24px', fontWeight: '400'},
    titleStyle: {fontSize: 18, lineHeight: 1.5, color: '#FF3D00'},
    headerStyle: {height: 30, backgroundColor: '#242424'},
    paper: {height: 50, width: 500, textAlign: 'center'},
    divider: {backgroundColor: '#9E9E9E', width: 'calc(100% - 48px)', margin: '-22px 24px 0 24px'},
    navBtn: {color: '#BDBDBD', padding: '0 10px', verticalAlign: 'middle', textTransform: 'capitalize'},
    navBtnActive: {color: '#f17b31', display: 'inline-block', verticalAlign: 'middle', textTransform: 'capitalize',
        padding: '0 10px'},
    navBar: {padding: '0 15px'},
};

/**
 * class which manages add worker functionality.
 */
class AddWorker extends React.Component {

    constructor() {
        super();
        this.state = {
            sessionInvalid: false,
            host: '',
            port: '',
            messageStyle: '',
            showMsg: false,
            message: '',
            open: false,
            hasPermission: false,
            isApiCalled: false,
            isError: false,
        };
        this._handleSubmit = this._handleSubmit.bind(this);
        this._showMessage = this._showMessage.bind(this);
        this._showError = this._showError.bind(this);
        this._testConnection = this._testConnection.bind(this);
    }

    componentWillMount() {
        let that = this;
        AuthenticationAPI.isUserAuthorized('manager', AuthManager.getUser().SDID)
            .then((response) => {
                that.setState({
                    hasPermission: response.data,
                    isApiCalled: true
                });
            }).catch((error) => {
            if (error.response != null) {
                if (error.response.status === 401) {
                    this.setState({
                        isApiCalled: true,
                        sessionInvalid: true,
                            statusMessage: this.context.intl.formatMessage({ id: 'authenticationFail', defaultMessage: "Authentication fail. Please login again." })
                    })
                } else if (error.response.status === 403) {
                    this.setState({
                        isApiCalled: true,
                        hasPermission: false,
                            statusMessage: this.context.intl.formatMessage({ id: 'noviewPermission', defaultMessage: 'User Have No Permission to view this page.' })
                    })
                } else {
                    this.setState({
                        isError: true,
                        hasPermission: false,
                            statusMessage: this.context.intl.formatMessage({ id: 'addWorker.mayExists', defaultMessage: 'Error while adding worker!. Worker may exists or unexpected error has occurred.' })
                    })
                }
                that._showError(that.state.statusMessage);
            }
        });
    }

    /**
     * Method to handle popup open
     */

    handlePopupOpen() {
        this.setState({open: true})
    };

    /**
     * Method to handle add worker submit button.
     */
    _handleSubmit(e) {
        e.preventDefault();
        let node = JSON.stringify({
            host: this.refs.host.input.value,
            port: this.refs.port.input.value,
        });

        let that = this;
        let nodeID = this.refs.host.input.value + "_" + this.refs.port.input.value;

        if (this.refs.port.input.value > 0 && this.refs.port.input.value <= 65535) {
            StatusDashboardAPIS.getRuntimeEnv(nodeID)
                .then((response) => {
                    if (response.status = HttpStatus.OK) {
                        if (response.data === "manager") {
                            that._addManager(nodeID);
                        } else if (response.data === "worker") {
                            that._addWorker(nodeID);
                        } else {
                            this.handlePopupOpen();
                        }
                    } else {

                        that._showError((this.context.intl.formatMessage({ id: 'addworker.errorConnecting', defaultMessage: 'Error while connecting with the node"' })) + nodeID);
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
                            hasPermission: false,
                                statusMessage: this.context.intl.formatMessage({ id: 'noviewPermission', defaultMessage: 'User Have No Permission to view this' })
                        })
                    } else if (error.response.status === 500) {
                        this.setState({
                            isApiCalled: true,
                            hasPermission: false,
                                statusMessage: this.context.intl.formatMessage({ id: 'addworker.unreachableNode', defaultMessage: 'Unreachable node. Try again !' })
                        })
                    } else {
                        this.setState({
                            isApiCalled: true,
                            statusMessage: error.response.data.message
                        })
                    }
                }
                that._showError(that.state.statusMessage);
            });
        } else {
            that._showError(<FormattedMessage id='addworker.invaliedPort' defaultMessage='In valid port number. Try again ' />);
        }
    }

    /**
     * method to handle test connection of a worker
     */
    _testConnection() {
        let workerID = this.refs.host.input.value + "_" + this.refs.port.input.value;
        let that = this;

        StatusDashboardAPIS.getRuntimeEnv(workerID)
            .then((response) => {
                if (response.status = HttpStatus.OK) {
                    if (response.data === "manager") {
                        StatusDashboardAPIS.testConnection(workerID)
                            .then((response) => {
                                if (response.data.code === 200) {
                                    that._showMessage(this.context.intl.formatMessage({
                                        id: 'addworker.successfulReachManager', defaultMessage: 'Successfully reached the manager : {workerID}', values: { workerID: workerID }
                                    }));
                                } else {
                                    that._showError(response.data.message)
                                }
                            }).catch((error) => {
                                that._showError(<FormattedMessage id='addworker.errorTesting' defaultMessage='Error while testing the connection! ' />);
                        });
                    } else if (response.data === "worker") {
                        StatusDashboardAPIS.testConnection(workerID)
                            .then((response) => {
                                if (response.data.code === 200) {
                                    that._showMessage(this.context.intl.formatMessage({ id: 'addworker.successfulReachWorker', defaultMessage: 'Successfully reached the worker : {workerID}', values: { workerID: workerID } }));
                                } else {
                                    that._showError(response.data.message)
                                }
                            }).catch((error) => {
                                that._showError(<FormattedMessage id='addworker.errorTesting' defaultMessage='Error while testing the connection!! ' />);
                        });
                    } else {
                        that._showError(<FormattedMessage id='addworker.unreachableNode' defaultMessage='Unreachable Node. Try Again !' />);
                    }
                } else {
                    that._showError(<FormattedMessage id='addworker.somethingWrong' defaultMessage='Something went wrong please check ' />);
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
                        hasPermission: false,
                            statusMessage: this.context.intl.formatMessage({ id: 'noviewPermission', defaultMessage: 'User Have No Permission to view this' })
                    })
                } else if (error.response.status === 500) {
                    this.setState({
                        isApiCalled: true,
                        hasPermission: false,
                            statusMessage: this.context.intl.formatMessage({ id: 'addworker.unreachableNode', defaultMessage: 'Unreachable node. Try again !' })
                    })
                } else {
                    this.setState({
                        isApiCalled: true,
                        statusMessage: error.response.data.message
                    })
                }
            }
            that._showError(that.state.statusMessage);
        });
    }

    _showError(message) {
        this.setState({
            messageStyle: errorMessageStyle,
            showMsg: true,
            message: message
        });
    }

    _showMessage(message) {
        this.setState({
            messageStyle: successMessageStyle,
            showMsg: true,
            message: message
        });
    }


    _addWorker(nodeID) {
        let node = JSON.stringify({
            host: this.refs.host.input.value,
            port: this.refs.port.input.value,
        });

        let that = this;
        nodeID = this.refs.host.input.value + "_" + this.refs.port.input.value;
        StatusDashboardOverViewAPI.createWorker(node)
            .then((response) => {
                if (response.status === HttpStatus.OK) {
                    that._showMessage(<FormattedMessage id='addworker.workerAddedSuccessfull' defaultMessage='Worker {nodeID} is added  successfully !' values={{ nodeID: nodeID }} />);
                    setTimeout(function () {
                        window.location.href = window.contextPath;
                    }, 1000)
                } else {
                    that._showError(<FormattedMessage id='addworker.errorAddingWorker' defaultMessage='Error while adding worker {nodeID} . Try Again !' values={{ nodeID: nodeID }} />);
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
                        hasPermission: false,
                            statusMessage: this.context.intl.formatMessage({ id: 'noviewPermission', defaultMessage: 'User Have No Permission to view this' })
                    })
                } else {
                    this.setState({
                        isApiCalled: true,
                        statusMessage: error.response.data.message
                    })
                }
            }
            that._showError(that.state.statusMessage);
        });
    }

    _addManager(nodeID) {
        let node = JSON.stringify({
            host: this.refs.host.input.value,
            port: this.refs.port.input.value,
        });

        let that = this;
        nodeID = this.refs.host.input.value + "_" + this.refs.port.input.value;
        StatusDashboardOverViewAPI.createManager(node)
            .then((response) => {
                if (response.status === HttpStatus.OK) {
                    this._showMessage(<FormattedMessage id='addworker.managerAddedSuccessful' defaultMessage='Manager {nodeID} is added successfully !' values={{ nodeID: nodeID }} />);
                    setTimeout(function () {
                        window.location.href = window.contextPath;
                    }, 1000)
                } else {
                    that._showError(<FormattedMessage id='addworker.errorAddingManager' defaultMessage='Error while adding manager {nodeID} .Try Again !' values={{ nodeID: nodeID }} />);
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
                        hasPermission: false,
                            statusMessage: this.context.intl.formatMessage({ id: 'noviewPermission', defaultMessage: 'User Have No Permission to view this' })
                    })
                } else {
                    this.setState({
                        isApiCalled: true,
                            statusMessage: this.context.intl.formatMessage({ id: 'addworker.unknownError', defaultMessage: 'Unknown error occurred!' })
                    })
                }
            }
            that._showError(that.state.statusMessage);
        });

    }

    render() {
        if (this.state.isError) {
            return <Error500 message={this.state.statusMessage}/>;
        }
        if (this.state.sessionInvalid) {
            return (
                <Redirect to={{pathname: `${window.contextPath}/logout`}}/>
            );
        }
        if (this.state.isApiCalled) {
            if (!this.state.hasPermission) {
                return <Error403/>;
            }
            let actionsButtons = [
                <FlatButton
                    label={<FormattedMessage id='addworker.ok' defaultMessage='OK' />}
                    backgroundColor='#f17b31'
                    onClick={() => {
                        this.setState({open: false})
                    }}
                />
            ];

            let nodeButtons = [
                <FlatButton
                    label={<FormattedMessage id='addworker.worker' defaultMessage='Worker' />}
                    backgroundColor='#f17b31'
                    style={popupButtonStyle}
                    onClick={() => {
                        let nodeID = this.refs.host.input.value + "_" + this.refs.port.input.value;
                        this._addWorker(nodeID);
                    }}
                />,
                < FlatButton
                    label={<FormattedMessage id='addworker.manager' defaultMessage='Manager' />}
                    backgroundColor='#f17b31'
                    style={popupButtonStyle}
                    onClick={() => {
                        let nodeID = this.refs.host.input.value + "_" + this.refs.port.input.value;
                        this._addManager(nodeID);
                    }}
                />,

                < FlatButton
                    label={<FormattedMessage id='addworker.cancel' defaultMessage='Cancel' />}
                    style={popupButtonStyle}
                    onClick={() => {
                        this.setState({open: false, openAdd: false})
                    }}
                />

            ];
            return (
                <div>
                    <Dialog
                        actions={actionsButtons}
                        modal
                        open={this.state.open}
                        onRequestClose={() => {
                            this.setState({open: false, openAdd: false});
                        }}>
                        {this.state.message}
                    </Dialog>

                    <Dialog
                        title={<h3><FormattedMessage id='addworker.unreachableNode' defaultMessage='Unreachable Node' /></h3>}
                        actions={nodeButtons}
                        modal={false}
                        open={this.state.open}
                        onRequestClose={() => {
                            this.setState({open: false, openAdd: false});
                        }}>
                        <FormattedMessage id='addworker.nodedetaialsunreachable' defaultMessage='Node details you entered is currently unreachable. Please choose the run time environment.' />
                    </Dialog>

                    <Header/>
                    <div style={styles.navBar} className="navigation-bar">
                        <Link style={{textDecoration: 'none'}} to={window.contextPath}>
                            <Button style={styles.navBtn}>
                                <HomeButton style={{paddingRight: 8, color: '#BDBDBD'}}/>
                                <FormattedMessage id='overview' defaultMessage='Overview >' />
                            </Button>
                        </Link>
                        <Typography style={styles.navBtnActive}><FormattedMessage id='addworker.addNew' defaultMessage='Add New' />/></Typography>
                    </div>
                    <MuiThemeProvider muiTheme={muiTheme}>
                        <div className="addWorker-container">
                            <FormPanel title={<FormattedMessage id='addworker.letsAdd' defaultMessage="Let' s add a new node" />} onSubmit={this._handleSubmit} width={650}>
                                <TextField floatingLabelFocusStyle={{color: '#f17b31'}}
                                           underlineFocusStyle={{borderColor: '#f17b31'}}
                                           style={textField} className="form-group" ref="host"
                                    hintText={this.context.intl.formatMessage({ id: 'addworker.hintextforhost', defaultMessage: 'Eg. localhost' })}
                                    floatingLabelText={this.context.intl.formatMessage({ id: 'addworker.host', defaultMessage: 'HOST' })}
                                           type="text"
                                           value={this.state.host}
                                           onChange={(e) => {
                                               this.setState({
                                                   host: e.target.value,
                                               });
                                           }}


                                /><br/>
                                <TextField floatingLabelFocusStyle={{color: '#f17b31'}}
                                           underlineFocusStyle={{borderColor: '#f17b31'}}
                                           style={textField} className="form-group" ref="port"
                                    hintText={this.context.intl.formatMessage({ id: 'addworker.hintforport', defaultMessage: 'Eg. 9443' })}
                                    floatingLabelText={this.context.intl.formatMessage({ id: 'addworker.HttpsPort', defaultMessage: 'HTTPS PORT' })}
                                           type="text"
                                           value={this.state.port}
                                           onChange={(e) => {
                                               this.setState({
                                                   port: e.target.value,
                                               });
                                           }}

                                /><br/>
                                <br/>
                                <RaisedButton
                                    disabled={this.state.host === '' || this.state.port === ''}
                                    backgroundColor='#f17b31'
                                    style={buttonStyle}
                                    label={this.context.intl.formatMessage({ id: 'addworker.addnode', defaultMessage: 'Add Node' })}
                                    type="submit"/>
                                <RaisedButton style={buttonStyle}
                                              disabled={this.state.host === '' || this.state.port === ''}
                                    label={this.context.intl.formatMessage({ id: 'addworker.testConnection', defaultMessage: 'Test Connection' })}
                                              onClick={this._testConnection}/>
                                <Link to={window.contextPath}><RaisedButton style={buttonStyle} label="Cancel"/></Link>
                            </FormPanel>
                        </div>
                    </MuiThemeProvider>
                    <Snackbar contentStyle={messageBoxStyle} bodyStyle={this.state.messageStyle}
                              open={this.state.showMsg}
                              message={this.state.message} autoHideDuration={4000}
                              onRequestClose={() => {
                                  this.setState({
                                      showMsg: false,
                                      message: ""
                                  });
                              }}
                    />
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
}

AddWorker.contextTypes = {
    intl: PropTypes.object.isRequired,
}

export default AddWorker;