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
import {Link} from "react-router-dom";
// App Components
import StatusDashboardAPIS from "../utils/apis/StatusDashboardAPIs";
// Material UI
import HomeButton from "material-ui/svg-icons/action/home";
import {Dialog, FlatButton, RaisedButton, Snackbar, TextField} from "material-ui";
// CSS
import "../../public/css/dashboard.css";

const messageBoxStyle = {textAlign: "center", color: "white"};
const errorMessageStyle = {backgroundColor: "#FF5722", color: "white"};
const successMessageStyle = {backgroundColor: "#4CAF50", color: "white"};
const buttonStyle = {marginLeft: 60, width: '30%'};
const textField = {width: 450};

/**
 * class which manages add worker functionality.
 */
export default class AddWorker extends React.Component {

    constructor() {
        super();
        this.state = {
            messageStyle: '',
            showMsg: false,
            message: '',
            open: false
        };
        this._handleSubmit = this._handleSubmit.bind(this);
        this._showMessage = this._showMessage.bind(this);
        this._showError = this._showError.bind(this);
        this._testConnection = this._testConnection.bind(this);
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
                if (response.status === 200) {
                    that._showMessage("Worker '" + workerID + "' is added successfully !");
                    setTimeout(function () {
                        window.location.href = window.contextPath;
                    }, 1000)
                }
                else {
                    that._showError("Error while adding worker '" + workerID + "' . Try Again !");
                }
            }).catch((error) => {
            that._showError("Error in adding worker !!");
        });
    }

    /**
     * method to handle test connection of a worker
     */
    _testConnection() {
        this.setState({open: true});
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
                    This feature is currently not available
                </Dialog>

                <div className="navigation-bar">
                    <Link to={window.contextPath}><FlatButton label="Overview >" icon={<HomeButton color="black"/>}/></Link>
                    <RaisedButton label= "Add New" disabled disabledLabelColor='white'
                                  disabledBackgroundColor='#f17b31'/>
                </div>

                <h1 style={{textAlign: 'center', marginTop: 50, color: '#9c9898'}}>Let's add a new worker</h1>
                <div className="form">
                    <div className="form-panel">
                        <form onSubmit={this._handleSubmit}>
                            <TextField floatingLabelFocusStyle={{color: '#f17b31'}}
                                       underlineFocusStyle={{borderColor: '#f17b31'}}
                                       style={textField} className="form-group" ref="host" hintText="Eg. 100.10.5.41"
                                       floatingLabelText="Host" type="text"/><br />
                            <TextField floatingLabelFocusStyle={{color: '#f17b31'}}
                                       underlineFocusStyle={{borderColor: '#f17b31'}}
                                       style={textField} className="form-group" ref="port" hintText="Eg. 9080"
                                       floatingLabelText="Port" type="text"/><br />
                            <RaisedButton backgroundColor='#f17b31' style={buttonStyle} label="Add Worker"
                                          type="submit"/>
                            {/*TODO: next version*/}
                            {/*<RaisedButton style={buttonStyle} label="Test Connection" onClick={this._testConnection}/>*/}
                            <Link to={window.contextPath}><RaisedButton style={buttonStyle} label="Cancel"/></Link>
                        </form>
                    </div>
                </div>
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
    }

}

