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
// Material UI
import HomeButton from 'material-ui/svg-icons/action/home';
import {Dialog, FlatButton, RaisedButton, Snackbar, TextField} from 'material-ui';
// CSS
import '../../public/css/dashboard.css';
import AuthenticationAPI from '../utils/apis/AuthenticationAPI';
import AuthManager from '../auth/utils/AuthManager';
import FormPanel from '../common/FormPanel';
import {darkBaseTheme, getMuiTheme, MuiThemeProvider} from 'material-ui/styles';
import Error403 from '../error-pages/Error403';
import StatusDashboardOverViewAPI from '../utils/apis/StatusDashboardOverViewAPI';

const muiTheme = getMuiTheme(darkBaseTheme);
const messageBoxStyle = {textAlign: "center", color: "white"};
const errorMessageStyle = {backgroundColor: "#FF5722", color: "white"};
const successMessageStyle = {backgroundColor: "#4CAF50", color: "white"};
const buttonStyle = {marginLeft: 10, width: '30%', fontSize: '8px'};
const popupButtonStyle = {marginLeft: 25, width: '30%', fontSize: '8px'};
const textField = {width: 650};

/**
 * class which manages add worker functionality.
 */
export default class AddWorker extends React.Component {

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
                        statusMessage: "Authentication fail. Please login again."
                    })
                } else if (error.response.status === 403) {
                    this.setState({
                        isApiCalled: true,
                        hasPermission: false,
                        statusMessage: "User Have No Permission to view this page."
                    })
                } else {
                    this.setState({
                        isError: true,
                        hasPermission: false,
                        statusMessage: "Error while adding worker!. Worker may exists or unexpected error has occurred."
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

                        that._showError("Error while connecting with the node" + nodeID);
                    }
                }).catch((error) => {
                if (error.response != null) {
                    if (error.response.status === 401) {
                        this.setState({
                            isApiCalled: true,
                            sessionInvalid: true,
                            statusMessage: "Authentication fail. Please login again."
                        })
                    } else if (error.response.status === 403) {
                        this.setState({
                            isApiCalled: true,
                            hasPermission: false,
                            statusMessage: "User Have No Permission to view this"
                        })
                    } else if (error.response.status === 500) {
                        this.setState({
                            isApiCalled: true,
                            hasPermission: false,
                            statusMessage: "Unreachable node. Try again !"
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
            that._showError("In valid port number. Try again ");
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
                                    that._showMessage("Successfully reached the manager : " + workerID);
                                } else {
                                    that._showError(response.data.message)
                                }
                            }).catch((error) => {
                            that._showError("Error while testing the connection!! ");
                        });
                    } else if (response.data === "worker") {
                        StatusDashboardAPIS.testConnection(workerID)
                            .then((response) => {
                                if (response.data.code === 200) {
                                    that._showMessage("Successfully reached the worker : " + workerID);
                                } else {
                                    that._showError(response.data.message)
                                }
                            }).catch((error) => {
                            that._showError("Error while testing the connection!! ");
                        });
                    } else {
                        that._showError("Unreachable Node. Try Again !");
                    }
                } else {
                    that._showError("Something went wrong please check ")
                }
            }).catch((error) => {
            if (error.response != null) {
                if (error.response.status === 401) {
                    this.setState({
                        isApiCalled: true,
                        sessionInvalid: true,
                        statusMessage: "Authentication fail. Please login again."
                    })
                } else if (error.response.status === 403) {
                    this.setState({
                        isApiCalled: true,
                        hasPermission: false,
                        statusMessage: "User Have No Permission to view this"
                    })
                } else if (error.response.status === 500) {
                    this.setState({
                        isApiCalled: true,
                        hasPermission: false,
                        statusMessage: "Unreachable node. Try again !"
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
                    that._showMessage("Worker '" + nodeID + "' is added successfully !");
                    setTimeout(function () {
                        window.location.href = window.contextPath;
                    }, 1000)
                } else {
                    that._showError("Error while adding worker '" + nodeID + "' . Try Again !");
                }
            }).catch((error) => {
            if (error.response != null) {
                if (error.response.status === 401) {
                    this.setState({
                        isApiCalled: true,
                        sessionInvalid: true,
                        statusMessage: "Authentication fail. Please login again."
                    })
                } else if (error.response.status === 403) {
                    this.setState({
                        isApiCalled: true,
                        hasPermission: false,
                        statusMessage: "User Have No Permission to view this"
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
                    this._showMessage("Manager " + nodeID + " is added successfully !");
                    setTimeout(function () {
                        window.location.href = window.contextPath;
                    }, 1000)
                } else {
                    that._showError("Error while adding manager " + nodeID + " .Try Again !");
                }
            }).catch((error) => {
            if (error.response != null) {
                if (error.response.status === 401) {
                    this.setState({
                        isApiCalled: true,
                        sessionInvalid: true,
                        statusMessage: "Authentication fail. Please login again."
                    })
                } else if (error.response.status === 403) {
                    this.setState({
                        isApiCalled: true,
                        hasPermission: false,
                        statusMessage: "User Have No Permission to view this"
                    })
                } else {
                    this.setState({
                        isApiCalled: true,
                        statusMessage: "Unknown error occurred4444!"
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
                    label="OK"
                    backgroundColor='#f17b31'
                    onClick={() => {
                        this.setState({open: false})
                    }}
                />
            ];

            let nodeButtons = [
                <FlatButton
                    label="Worker"
                    backgroundColor='#f17b31'
                    style={popupButtonStyle}
                    onClick={() => {
                        let nodeID = this.refs.host.input.value + "_" + this.refs.port.input.value;
                        this._addWorker(nodeID);
                    }}
                />,
                <FlatButton
                    label="Manager"
                    backgroundColor='#f17b31'
                    style={popupButtonStyle}
                    onClick={() => {
                        let nodeID = this.refs.host.input.value + "_" + this.refs.port.input.value;
                        this._addManager(nodeID);
                    }}
                />,

                <FlatButton
                    label="Cancel"
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
                        title="Unreachable Node"
                        actions={nodeButtons}
                        modal={false}
                        open={this.state.open}
                        onRequestClose={() => {
                            this.setState({open: false, openAdd: false});
                        }}>
                        Node details you entered is currently unreachable. Please choose the run time environment.
                    </Dialog>

                    <Header/>
                    <div className="navigation-bar">
                        <Link to={window.contextPath}><FlatButton label="Overview >"
                                                                  icon={<HomeButton color="black"/>}/>
                        </Link>
                        <RaisedButton label="Add New" disabled disabledLabelColor='white'
                                      disabledBackgroundColor='#f17b31'/>
                    </div>
                    <MuiThemeProvider muiTheme={muiTheme}>
                        <div>
                            <FormPanel title={"Let's add a new node"} onSubmit={this._handleSubmit} width={650}>
                                <TextField floatingLabelFocusStyle={{color: '#f17b31'}}
                                           underlineFocusStyle={{borderColor: '#f17b31'}}
                                           style={textField} className="form-group" ref="host"
                                           hintText="Eg. localhost"
                                           floatingLabelText="HOST"
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
                                           hintText="Eg. 9443"
                                           floatingLabelText="HTTPS PORT"
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
                                    label="Add Node"
                                    type="submit"/>
                                <RaisedButton style={buttonStyle}
                                              disabled={this.state.host === '' || this.state.port === ''}
                                              label="Test Connection"
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
