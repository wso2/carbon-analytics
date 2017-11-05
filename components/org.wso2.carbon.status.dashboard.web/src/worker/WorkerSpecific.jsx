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
import {Redirect} from "react-router";
//App Components
import AppTable from "./AppTable";
import WorkerSpecificCharts from "./WorkerSpecificCharts";
import WorkerGeneralCard from "./WorkerGeneralCard";
import StatusDashboardAPIS from "../utils/apis/StatusDashboardAPIs";
//Material UI
import HomeButton from "material-ui/svg-icons/action/home";
import {Card, Dialog, FlatButton, Popover, Snackbar} from "material-ui";
import {List, ListItem} from "material-ui/List";
import Delete from "material-ui/svg-icons/action/delete";
import Settings from "material-ui/svg-icons/action/settings";

const messageBoxStyle = {textAlign: "center", color: "white"};
const errorMessageStyle = {backgroundColor: "#FF5722", color: "white"};
const successMessageStyle = {backgroundColor: "#4CAF50", color: "white"};

/**
 * class which manages worker specific details.
 */
export default class WorkerSpecific extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            workerID: this.props.match.params.id.split("_")[0] + ":" + this.props.match.params.id.split("_")[1],
            redirectEdit: false,
            redirectDelete: false,
            messageStyle: '',
            showMsg: false,
            message: '',
            open: false,
            popOver: false,
            anchorEl: ''
        };
        this._handleDelete = this._handleDelete.bind(this);
        this._showMessage = this._showMessage.bind(this);
        this._showError = this._showError.bind(this);
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

    _handleDelete() {
        let that = this;
        let workerIDD = this.props.match.params.id;
        StatusDashboardAPIS.deleteWorkerByID(this.props.match.params.id)
            .then((response) => {
                if (response.status === 200) {
                    that._showMessage("Worker '" + workerIDD + "' is deleted successfully !!");
                    that.setState({
                        redirectDelete: true
                    });
                }
                else {
                    that._showError("Worker '" + workerIDD + "' is not deleted successfully. Try again");
                }
            })
    }

    render() {
        let actionsButtons = [
            <FlatButton
                label="Yes"
                backgroundColor='#f17b31'
                onClick={this._handleDelete}
            />,
            <FlatButton
                label="No"
                onClick={() => {
                    this.setState({open: false})
                }}
            />,
        ];
        if (this.state.redirectDelete) {
            return <Redirect to="/sp-status-dashboard/overview/"/>;
        }
        return (
            <div style={{backgroundColor: '#222222'}}>
                <Dialog
                    title="Confirmation"
                    actions={actionsButtons}
                    modal={true}
                    open={this.state.open}
                    onRequestClose={() => {
                        this.setState({open: false})
                    }}>
                    {"Do you want to delete worker '" + this.state.workerID + "' ?"}
                </Dialog>

                <div className="navigation-bar">
                    <Link to="/sp-status-dashboard/overview"><FlatButton label="Overview >"
                                                                         icon={<HomeButton color="black"/>}/></Link>
                    <FlatButton label={this.state.workerID}/>
                </div>
                <div className="worker-h1">
                    <h2 style={{display: 'inline-block', float: 'left', marginLeft: 20}}> {this.state.workerID} </h2>
                </div>
                <div style={{float: 'right', marginRight: 50, backgroundColor: '#222222'}}>
                    <ListItem
                        style={{color: 'white'}}
                        primaryText="Settings"
                        leftIcon={<Settings />}
                        onClick={(event) => {
                            this.setState({popOver: true, anchorEl: event.currentTarget})
                        }}
                    />

                    <Popover
                        open={this.state.popOver}
                        anchorEl={this.state.anchorEl}
                        onRequestClose={() => this.setState({popOver: false})}
                        anchorOrigin={{horizontal: 'left', vertical: 'bottom'}}
                        targetOrigin={{horizontal: 'left', vertical: 'top'}}>
                        <List>
                            <ListItem
                                style={{color: 'white'}}
                                key={1}
                                primaryText="Delete Worker"
                                leftIcon={<Delete />}
                                onClick={() => {
                                    this.setState({open: true})
                                }}
                            />
                        </List>
                    </Popover>
                </div>

                <div><WorkerGeneralCard id={this.props.match.params.id}/></div>
                <div><WorkerSpecificCharts id={this.props.match.params.id}/></div>

                <div style={{color: '#dedede', marginLeft: 30, paddingTop: 20, display: 'inline-block'}}>
                    <h3> Siddhi Applications </h3>
                </div>
                <div style={{padding: 20, paddingTop: 10, width: '98%', float: 'left', boxSizing: 'border-box'}}>
                    <Card style={{height: 400}}>
                        <AppTable id={this.props.match.params.id}/>
                    </Card>
                </div>
                <Snackbar contentStyle={messageBoxStyle} bodyStyle={this.state.messageStyle} open={this.state.showMsg}
                          message={this.state.message} autoHideDuration={4000}
                          onRequestClose={() => {
                              this.setState({showMsg: false, message: ""})
                          }}
                />
            </div>
        );
    }

}

