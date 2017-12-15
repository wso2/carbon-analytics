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
import {Link, Redirect} from "react-router-dom";
// App Components
import StatusDashboardAPIS from "../utils/apis/StatusDashboardAPIs";
import {HttpStatus} from "../utils/Constants";
import Header from "../common/Header";
// Material UI
import HomeButton from "material-ui/svg-icons/action/home";
import {Dialog, FlatButton, RaisedButton, Snackbar, TextField} from "material-ui";
// CSS
import "../../public/css/dashboard.css";
import AuthenticationAPI from "../utils/apis/AuthenticationAPI";
import AuthManager from "../auth/utils/AuthManager";
import Error401 from "../error-pages/Error401";
import FormPanel from "../common/FormPanel";
import {darkBaseTheme, getMuiTheme, MuiThemeProvider} from "material-ui/styles";
import Error403 from "../error-pages/Error403";
const muiTheme = getMuiTheme(darkBaseTheme);
const messageBoxStyle = {textAlign: "center", color: "white"};
const errorMessageStyle = {backgroundColor: "#FF5722", color: "white"};
const successMessageStyle = {backgroundColor: "#4CAF50", color: "white"};
const buttonStyle = {marginLeft: 60, width: '35%', fontSize: '11px'};
const textField = {width: 450};

/**
 * class which manages add worker functionality.
 */
export default class AddWorker extends React.Component {

    constructor() {
        super();
        this.state = {
            sessionInvalid: false,
            host:'',
            port:'',
            messageStyle: '',
            showMsg: false,
            message: '',
            open: false,
            hasPermission: false,
            isApiCalled: false
        };
        this._handleSubmit = this._handleSubmit.bind(this);
        this._showMessage = this._showMessage.bind(this);
        this._showError = this._showError.bind(this);
        this._testConnection = this._testConnection.bind(this);
    }

    componentWillMount() {
        let that = this;
        AuthenticationAPI.isUserAuthorized('manager', AuthManager.getUser().token)
            .then((response) => {
                that.setState({
                    hasPermission: response.data,
                    isApiCalled: true
                });
            }).catch((error) => {
            let message;
            if(error.response != null){
                if(error.response.status === 401){
                    message = "Authentication fail. Please login again.";
                    this.setState({
                        sessionInvalid: true
                    })
                } else if(error.response.status === 403){
                    message = "User Have No Permission to view this page.";
                    this.setState({
                        hasPermission: false
                    })
                } else {
                    message = "Unknown error occurred! : " + error.response.data;
                }
            }
            this.setState({
                isApiCalled: true,
                statusMessage: message
            });
        });
    }

    /**
     * Method to handle add worker submit button.
     */
    _handleSubmit(e) {
        e.preventDefault();
        let worker = JSON.stringify({
            host: this.refs.host.input.value,
            port: this.refs.port.input.value,
        });
        let that = this;
        let workerID = this.refs.host.input.value + ":" + this.refs.port.input.value;
        StatusDashboardAPIS.createWorker(worker)
            .then((response) => {
                if (response.status === HttpStatus.OK) {
                    that._showMessage("Worker '" + workerID + "' is added successfully !");
                    setTimeout(function () {
                        window.location.href = window.contextPath;
                    }, 1000)
                } else {
                    that._showError("Error while adding worker '" + workerID + "' . Try Again ! \n " + response.data);
                }
            }).catch((error) => {
            let message;
            if(error.response != null){
                if(error.response.status === 401){
                    message = "Authentication fail. Please login again.";
                    this.setState({
                        isApiCalled: true,
                        sessionInvalid: true
                    })
                } else if(error.response.status === 403){
                    message = "User Have No Permission to view this";
                    this.setState({
                        hasPermission: false
                    })
                } else {
                    message = "Unknown error occurred! : " + error.response.data;
                }
            }
            that._showError(message);
            this.setState({
                isApiCalled: true,
                statusMessage: message
            });
        });


    }

    /**
     * method to handle test connection of a worker
     */
    _testConnection() {
        let workerID = this.refs.host.input.value + "_" + this.refs.port.input.value;
        let that = this;
        StatusDashboardAPIS.testConnection(workerID)
            .then((response) => {
                if (response.status === HttpStatus.OK) {
                    that.setState({
                        open: true,
                        message: "Connection Success!"
                    });
                }
                else {
                    that.setState({
                        open: true,
                        message: "Connection Fail!"
                    });
                }
            }).catch((error) => {
            that._showError("Error while testing the connection !!");
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

    render() {
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
                            <FormPanel title={"Let's add a new worker"} onSubmit={this._handleSubmit}>
                                <TextField floatingLabelFocusStyle={{color: '#f17b31'}}
                                           underlineFocusStyle={{borderColor: '#f17b31'}}
                                           style={textField} className="form-group" ref="host"
                                           hintText="Eg. 100.10.5.41"
                                           floatingLabelText="Host"
                                           type="text"
                                           value={this.state.host}
                                           onChange={(e) => {
                                               this.setState({
                                                   host: e.target.value,
                                               });
                                           }}


                                /><br />
                                <TextField floatingLabelFocusStyle={{color: '#f17b31'}}
                                           underlineFocusStyle={{borderColor: '#f17b31'}}
                                           style={textField} className="form-group" ref="port"
                                           hintText="Eg. 9080"
                                           floatingLabelText="Port"
                                           type="text"
                                           value={this.state.port}
                                           onChange={(e) => {
                                               this.setState({
                                                   port: e.target.value,
                                               });
                                           }}

                                /><br />
                                <br />
                                <RaisedButton
                                    disabled={this.state.host === '' || this.state.port === ''}
                                    backgroundColor='#f17b31'
                                    style={buttonStyle}
                                    label="Add Worker"
                                    type="submit"/>
                                {/*TODO: next version*/}
                                {/*<RaisedButton style={buttonStyle} label="Test Connection" onClick={this._testConnection}/>*/}
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

