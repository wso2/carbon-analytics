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
import {Redirect} from 'react-router';
//App Components
import AppTable from './AppTable';
import WorkerSpecificCharts from './WorkerSpecificCharts';
import WorkerGeneralCard from './WorkerGeneralCard';
import StatusDashboardAPIS from '../utils/apis/StatusDashboardAPIs';
import Header from '../common/Header';
//Material UI
import HomeButton from 'material-ui/svg-icons/action/home';
import {Card, Dialog, FlatButton, Popover, Snackbar} from 'material-ui';
import {List, ListItem} from 'material-ui/List';
import {Button, Typography} from 'material-ui-next';
import Delete from 'material-ui/svg-icons/action/delete';
import Settings from 'material-ui/svg-icons/action/settings';
import AuthenticationAPI from '../utils/apis/AuthenticationAPI';
import AuthManager from '../auth/utils/AuthManager';
import Error403 from '../error-pages/Error403';
// Localization
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';

const messageBoxStyle = {textAlign: "center", color: "white"};
const errorMessageStyle = {backgroundColor: "#FF5722", color: "white"};
const successMessageStyle = {backgroundColor: "#4CAF50", color: "white"};

const styles = {
    navBar: {padding: '0 15px'},
    navBtn: {color: '#BDBDBD', padding: '0 10px', verticalAlign: 'middle', textTransform: 'capitalize'},
    navBtnActive: {color: '#f17b31', display: 'inline-block', verticalAlign: 'middle', textTransform: 'capitalize',
        padding: '0 10px'},
    titleStyle: {fontSize: '1.6rem', margin: '20px 0 0 24px', color: '#dedede'},
};

/**x
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
            anchorEl: '',
            hasManagerPermission: false,
            hasViewerPermission: true,
            sessionInvalid: false
        };
        this._handleDelete = this._handleDelete.bind(this);
        this._showMessage = this._showMessage.bind(this);
        this._showError = this._showError.bind(this);
    }

    componentWillMount() {
        let that = this;
        AuthenticationAPI.isUserAuthorized('manager', AuthManager.getUser().SDID)
            .then((response) => {
                that.setState({
                    hasManagerPermission: response.data
                });
            }).catch((error) => {
            let message;
            if (error.response != null) {
                if (error.response.status === 401) {
                    message = this.context.intl.formatMessage({ id: 'authenticationFail', defaultMessage: 'Authentication fail. Please login again.' });
                    this.setState({
                        sessionInvalid: true
                    })
                } else if (error.response.status === 403) {
                    message = this.context.intl.formatMessage({ id: 'noViewPermission', defaultMessage: 'User Have No Permission to view this page.' });
                    this.setState({
                        hasViewerPermission: false
                    })
                } else {
                    message = this.context.intl.formatMessage({ id: 'unknownError', defaultMessage: 'Unknown error occurred! : {data}', values: { data: error.response.data } });
                }
                this.setState({
                    message: message
                })
            }
        });
        AuthenticationAPI.isUserAuthorized('viewer', AuthManager.getUser().SDID)
            .then((response) => {
                that.setState({
                    hasViewerPermission: response.data
                });
            }).catch((error) => {
            let message;
            if (error.response != null) {
                if (error.response.status === 401) {
                    message = this.context.intl.formatMessage({ id: 'authenticationFail', defaultMessage: 'Authentication fail. Please login again.' });
                    this.setState({
                        sessionInvalid: true
                    })
                } else if (error.response.status === 403) {
                    message = this.context.intl.formatMessage({ id: 'noViewerPermission', defaultMessage: 'User Have No Viewer Permission to view this page.' });
                    this.setState({
                        hasViewerPermission: false
                    })
                } else {
                    message = this.context.intl.formatMessage({ id: 'unknownError', defaultMessage: 'Unknown error occurred! : {data}', values: { data: error.response.data } });
                }
                this.setState({
                    message: message
                });
            }
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

    _handleDelete() {
        let that = this;
        let workerIDD = this.props.match.params.id;
        StatusDashboardAPIS.deleteWorkerByID(this.props.match.params.id)
            .then((response) => {
                if (response.status === 200) {
                    that._showMessage(this.context.intl.formatMessage({
                        id: 'workerSpecific.workerDeletedSuccessfull',
                        defaultMessage: 'Worker   {workerIDD} is deleted successfully !!',
                        values: { workerIDD: workerIDD }
                    }));
                    that.setState({
                        redirectDelete: true
                    });
                }
                else {
                    that._showError(this.context.intl.formatMessage({
                        id: 'workerSpecific.workerDeleteError',
                        defaultMessage: 'Worker {workerIDD}  is not deleted successfully. Try again',
                        values: { workerIDD: workerIDD }
                    }));
                }
            })
    }

    /**
     * Method which render delete worker button if permission is granted
     * @param workersList
     * @returns {XML}
     */
    renderSettings() {
        if (this.state.hasManagerPermission) {
            return (
                <div style={{right: 0, marginRight: 12, position: 'absolute', top: '85px'}}>
                    <ListItem
                        style={{color: 'white'}}
                        primaryText={<FormattedMessage id='workerSpecific.settings' defaultMessage='Settings' />}
                        leftIcon={<Settings style={{paddingLeft: '22px'}}/>}
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
                                primaryText={<FormattedMessage id='workerSpecific.deleteWorker' defaultMessage='Delete Worker' />}
                                leftIcon={<Delete style={{paddingLeft: '22px'}}/>}
                                onClick={() => {
                                    this.setState({open: true})
                                }}
                            />
                        </List>
                    </Popover>
                </div>
            )
        } else {
            return (
                <div style={{float: 'right', marginRight: 50, backgroundColor: '#222222'}}>
                    <ListItem
                        style={{color: 'white', display: 'none'}}
                        primaryText="Settings"
                        leftIcon={<Settings/>}
                        onClick={(event) => {
                            this.setState({popOver: true, anchorEl: event.currentTarget})
                        }}
                    />
                </div>
            )
        }

    }

    render() {
        if (this.state.sessionInvalid) {
            return (
                <Redirect to={{pathname: `${window.contextPath}/logout`}}/>
            );
        }
        if (this.state.hasViewerPermission) {
            let actionsButtons = [
                <FlatButton
                    label={<FormattedMessage id='yes' defaultMessage='Yes' />}
                    backgroundColor='#f17b31'
                    onClick={this._handleDelete}
                />,
                <FlatButton
                    label={<FormattedMessage id='no' defaultMessage='No' />}
                    onClick={() => {
                        this.setState({open: false})
                    }}
                />,
            ];
            if (this.state.redirectDelete) {
                return <Redirect to={window.contextPath}/>;
            }
            return (
                <div style={{backgroundColor: '#222222'}}>
                    <Dialog
                        title={<FormattedMessage id='workerspecific.confirmation' defaultMessage='Confirmation' />}
                        actions={actionsButtons}
                        modal={true}
                        open={this.state.open}
                        onRequestClose={() => {
                            this.setState({open: false})
                        }}>
                        {<FormattedMessage id='workerSpecific.deleteWorkerConfirmation' defaultMessage='Do you want to delete worker {workerID} ?' values={{ workerID: this.state.workerID }} />}
                    </Dialog>

                    <Header/>
                    <div style={styles.navBar} className="navigation-bar">
                        <Link style={{textDecoration: 'none'}} to={window.contextPath}>
                            <Button style={styles.navBtn}>
                                <HomeButton style={{paddingRight: 8, color: '#BDBDBD'}}/>
                                <FormattedMessage id='overview' defaultMessage='Overview >' />
                            </Button>
                        </Link>
                        <Typography style={styles.navBtnActive}>{this.state.workerID}</Typography>
                    </div>
                    <Typography variant="title" style={styles.titleStyle}> {this.state.workerID} </Typography>
                    {this.renderSettings()}
                    <div style={{margin: '10px 0'}}>
                        <div><WorkerGeneralCard id={this.props.match.params.id}/></div>
                        <div><WorkerSpecificCharts id={this.props.match.params.id}/></div>
                    </div>

                    <div style={{color: '#dedede', marginLeft: '24px', display: 'inline-block'}}>
                        <h3> Siddhi Applications </h3>
                    </div>
                    <div style={{padding: 20, paddingTop: 10, float: 'left', boxSizing: 'border-box'}}>
                        <Card style={{height: 400}}>
                            <AppTable id={this.props.match.params.id}/>
                        </Card>
                    </div>
                    <Snackbar contentStyle={messageBoxStyle} bodyStyle={this.state.messageStyle}
                              open={this.state.showMsg}
                              message={this.state.message} autoHideDuration={4000}
                              onRequestClose={() => {
                                  this.setState({showMsg: false, message: ""})
                              }}
                    />
                </div>
            );
        } else {
            return <Error403/>;
        }
    }
}

WorkerSpecific.contextTypes = {
    intl: PropTypes.object.isRequired
}

