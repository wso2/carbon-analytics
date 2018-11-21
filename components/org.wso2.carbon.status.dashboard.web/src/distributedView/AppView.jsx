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
import SyntaxHighlighter from 'react-syntax-highlighter';
import PropTypes from 'prop-types';

//App Components
import StatusDashboardAPIS from "../utils/apis/StatusDashboardAPIs";
import Header from "../common/Header";
import ParentAppTable from "./ParentAppTable";
//Material UI
import HomeButton from 'material-ui/svg-icons/action/home';
import {Card, CardHeader, CardText, Dialog, Divider, FlatButton, Snackbar, RaisedButton} from 'material-ui';
import {Button, Typography} from 'material-ui-next';

import AuthenticationAPI from '../utils/apis/AuthenticationAPI';
import AuthManager from '../auth/utils/AuthManager';
import {Redirect} from 'react-router-dom';
import Error403 from '../error-pages/Error403';
import {HttpStatus} from '../utils/Constants';
import KafkaFlow from './KafkaFlow';
//Localization
import { FormattedMessage } from 'react-intl';

const styles = {
    navBar: {padding: '0 15px'},
    navBtn: {color: '#BDBDBD', padding: '0 10px', verticalAlign: 'middle', textTransform: 'capitalize'},
    navBtnActive: {color: '#f17b31', display: 'inline-block', verticalAlign: 'middle', textTransform: 'capitalize',
        padding: '0 10px'},
    titleStyle: {fontSize: '1.6rem', margin: '20px 0 0 24px', color: '#dedede'},
    button: {margin: 0, fontSize: 10, borderLeft: '1px solid #4c4c4c', borderRadius: 0}
};
const messageBoxStyle = {textAlign: "center", color: "white"};
const errorMessageStyle = {backgroundColor: "#FF5722", color: "white"};

const successMessageStyle = {backgroundColor: "#4CAF50", color: "white"};
const codeViewStyle = {
    "hljs": {
        "display": "block",
        "overflowX": "auto",
        "padding": "0.5em",
        "background": "#131313",
        "color": "#dad9d9"
    },
    "hljs-comment": {"color": "#777"},
    "hljs-quote": {"color": "#777"},
    "hljs-variable": {"color": "#ab875d"},
    "hljs-template-variable": {"color": "#ab875d"},
    "hljs-tag": {"color": "#ab875d"},
    "hljs-regexp": {"color": "#ab875d"},
    "hljs-meta": {"color": "#ab875d"},
    "hljs-number": {"color": "#ab875d"},
    "hljs-built_in": {"color": "#ab875d"},
    "hljs-builtin-name": {"color": "#ab875d"},
    "hljs-literal": {"color": "#ab875d"},
    "hljs-params": {"color": "#ab875d"},
    "hljs-symbol": {"color": "#ab875d"},
    "hljs-bullet": {"color": "#ab875d"},
    "hljs-link": {"color": "#ab875d"},
    "hljs-deletion": {"color": "#ab875d"},
    "hljs-section": {"color": "#9b869b"},
    "hljs-title": {"color": "#9b869b"},
    "hljs-name": {"color": "#9b869b"},
    "hljs-selector-id": {"color": "#9b869b"},
    "hljs-selector-class": {"color": "#9b869b"},
    "hljs-type": {"color": "#9b869b"},
    "hljs-attribute": {"color": "#9b869b"},
    "hljs-string": {"color": "#f17b31"},
    "hljs-keyword": {"color": "#f17b31"},
    "hljs-selector-tag": {"color": "#f17b31"},
    "hljs-addition": {"color": "#f17b31"},
    "hljs-emphasis": {"fontStyle": "italic"},
    "hljs-strong": {"fontWeight": "bold"}
};

/**
 * Class which manages Siddhi App specific details.
 */
export default class AppView extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            workerID: this.props.match.params.id.split("_")[0] + ":" + this.props.match.params.id.split("_")[1],
            appName: this.props.match.params.appName,
            id: this.props.match.params.id,
            appText: '',
            open: false,
            messageStyle: '',
            showMsg: false,
            message: '',
            confirmMessage: '',
            hasManagerPermission: false,
            hasViewerPermission: true,
            sessionInvalid: false
        };

        this.showMessage = this.showMessage.bind(this);
        this.showError = this.showError.bind(this);
    }

    componentWillMount() {
        let that = this;
        AuthenticationAPI.isUserAuthorized('metrics.manager', AuthManager.getUser().SDID)
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
                    message = this.context.intl.formatMessage({ id: 'noManagerPermission', defaultMessage: 'User Have No Manager Permission to view this page.' });
                    this.setState({
                        hasManagerPermission: false
                    })
                } else {
                    message = this.context.intl.formatMessage({ id: 'appview.unknowenError', defaultMessage: 'Unknown error occurred! :  {date}', values: { date: error.response.date } });
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
                    message = this.context.intl.formatMessage({ id: 'appview.unknownError', defaultMessage: 'Unknown error occurred! :  {date}', values: { date: error.response.date } });
                }
                this.setState({
                    message: message
                })
            }
        });

        StatusDashboardAPIS.getSiddhiAppTextView(this.props.match.params.id, this.props.match.params.appName)
            .then((response) => {
                if (response.status == HttpStatus.OK) {
                    that.setState({
                        appText: response.data
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
                        statusMessage: "User Have No Permission to view this page."
                    });
                } else {
                    this.setState({
                        isError: true,
                        isApiCalled: true,
                        statusMessage: this.context.intl.formatMessage({ id: 'appview.unknownError', defaultMessage: 'Unknown error occurred! : {date} ', values: { date: JSON.stringify(error.response.data) } })
                    });
                }
            }
        });
    }

    showError(message) {
        this.setState({
            messageStyle: errorMessageStyle,
            showMsg: true,
            message: message
        });
    }

    showMessage(message) {
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
        if (!this.state.hasViewerPermission) {
            return <Error403/>;
        }
        //when state changes the width changes
        let actionsButtons = [
            <FlatButton
                label={<FormattedMessage id='yes' defaultMessage='Yes' />}
                backgroundColor='#f17b31'
                onClick={this.handleToggle}
            />,
            <FlatButton
                label={<FormattedMessage id='no' defaultMessage='No' />}
                onClick={() => {
                    this.setState({open: false})
                }}
            />,
        ];

        return (
            <div>
                <Dialog
                    actions={actionsButtons}
                    modal
                    open={this.state.open}
                    onRequestClose={() => this.setState({open: false})}>
                    {this.state.confirmMessage}
                </Dialog>

                <div>
                    <Header/>
                    <div style={styles.navBar} className="navigation-bar">
                        <Link style={{textDecoration: 'none'}} to={window.contextPath}>
                            <Button style={styles.navBtn}>
                                <HomeButton style={{paddingRight: 8, color: '#BDBDBD'}}/>
                                <FormattedMessage id='overview' defaultMessage='Overview >' />
                            </Button>
                        </Link>
                        <Link style={{textDecoration: 'none'}} to={window.contextPath + "/" +
                            this.props.match.params.id + "/" + "siddhi-apps"}>
                            <Button style={styles.navBtn}>
                                {this.props.match.params.id} >
                            </Button>
                        </Link>
                        <Typography style={styles.navBtnActive}>{this.props.match.params.appName}</Typography>
                    </div>
                    <Typography variant="title" style={styles.titleStyle}>
                        {this.state.workerID} : {this.state.appName}
                    </Typography>
                </div>

                <div style={{padding: 10, paddingLeft: 40, width: '90%', height: '50%', backgroundColor: "#222222"}}>
                    <Card style={{backgroundColor: "#282828", height: '50%'}}>
                        <CardHeader title={<FormattedMessage id='appview.codeView' defaultMessage='Code View' />} subtitle={this.props.match.params.appName}
                            titleStyle={{ fontSize: 24, backgroundColor: "#282828" }}
                        />
                        <Divider/>

                        <CardText>
                            <SyntaxHighlighter language='sql'
                                               style={codeViewStyle}>{this.state.appText}</SyntaxHighlighter>
                        </CardText>
                    </Card>
                </div>
                <div style={{padding: 10, paddingLeft: 40, width: '90%', height: '100%', backgroundColor: "#222222"}}>
                    <Card style={{backgroundColor: "#282828", height: '100%'}}>
                        <CardHeader title={<FormattedMessage id='appview.distributedSiddhiApp' defaultMessage='Distributed Siddhi App Deployment' />}
                            titleStyle={{ fontSize: 24, backgroundColor: "#282828" }}
                        />

                        <Divider/>

                        <CardText>
                            <ul className='legend' style={{
                                color: '#fff',
                                fontSize: 14,
                                textAlign: 'left',
                                float: 'right',
                                width: '100%',
                                maxWidth: 250,
                                height: 30,
                                padding: 10,
                                background: '#333'
                            }}>
                                <li className='legend-key' style={{
                                    paddingRight: 5,
                                    paddingLeft: 5,
                                    paddingTop: 0,
                                    paddingBottom: 0,
                                    height: 30,
                                    display: 'inline-block'
                                }}>
                                        <span className='legend-color' style={{
                                            width: 15,
                                            height: 15,
                                            float: 'left',
                                            margin: 5,
                                            backgroundColor: 'blue'
                                        }} ChildApps></span>
                                    <span className='legend-text' style={{
                                        lineHeight: 2,
                                        fontSize: 12
                                    }}><FormattedMessage id='appview.childApps' defaultMessage=' Child Apps' /></span>

                                </li>
                                <li className='legend-key' style={{
                                    paddingRight: 5, paddingLeft: 5, paddingTop: 0, paddingBottom: 0,
                                    display: 'inline-block', height: 30
                                }}>
                                         <span className='legend-color' style={{
                                             width: 15,
                                             height: 15,
                                             float: 'left',
                                             margin: 5,
                                             backgroundColor: 'red'
                                         }}></span>
                                    <span className='legend-text' style={{
                                        lineHeight: 2,
                                        fontSize: 12
                                    }}><FormattedMessage id='appview.kafkatopics' defaultMessage='Kafka-Topics' /> </span>
                                </li>
                            </ul>
                            <KafkaFlow id={this.props.match.params.id} appName={this.props.match.params.appName}/>
                        </CardText>
                    </Card>
                </div>
                <div style={{width: '90%', marginLeft: 40}}>
                    <h3 style={{ color: 'white' }}><FormattedMessage id='appview.childAppDetails' defaultMessage='Child App Details' /> </h3>
                    <ParentAppTable id={this.props.match.params.id} appName={this.props.match.params.appName}/>
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

AppView.contextTypes = {
    intl: PropTypes.object.isRequired
}