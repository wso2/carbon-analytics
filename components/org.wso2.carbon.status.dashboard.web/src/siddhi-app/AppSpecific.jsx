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
import SyntaxHighlighter from 'react-syntax-highlighter';
import PropTypes from 'prop-types';
//App Components
import StatusDashboardAPIS from '../utils/apis/StatusDashboardAPIs';
import {HttpStatus} from '../utils/Constants';
import ComponentTable from './ComponentTable';
import VizG from 'react-vizgrammar';
import Header from '../common/Header';
//Material UI
import {GridList, GridTile} from 'material-ui/GridList';
import HomeButton from 'material-ui/svg-icons/action/home';
import {Card, CardHeader, CardText, Dialog, Divider, FlatButton, Snackbar} from 'material-ui';
import {Button, Typography, Radio, RadioGroup, FormControlLabel, FormControl, FormLabel} from 'material-ui-next';
import DashboardUtils from '../utils/DashboardUtils';
import AuthenticationAPI from '../utils/apis/AuthenticationAPI';
import AuthManager from '../auth/utils/AuthManager';
import Error403 from '../error-pages/Error403';
import StatusDashboardOverViewAPI from '../utils/apis/StatusDashboardOverViewAPI';
import AppEventFlow from "./AppEventFlow";
import '../../public/css/dashboard.css';
// Localization
import { FormattedMessage } from 'react-intl';

const styles = {
    root: {display: 'flex', flexWrap: 'wrap', justifyContent: 'space-around'},
    gridList: {overflowY: 'auto', padding: 24},
    navBar: {padding: '0 15px'},
    navBtn: {color: '#BDBDBD', padding: '0 10px', verticalAlign: 'middle', textTransform: 'capitalize'},
    navBtnActive: {color: '#f17b31', display: 'inline-block', verticalAlign: 'middle', textTransform: 'capitalize',
        padding: '0 10px'},
    titleStyle: {fontSize: '1.6rem', margin: '20px 0 0 24px', color: '#dedede'},
    button: {margin: 0, fontSize: 10, borderLeft: '1px solid #4c4c4c', borderRadius: 0}
};
const memoryMetadata = {names: ['Time', 'Memory'], types: ['time', 'linear']};
const memoryLineChartConfig = {
    x: 'Time',
    charts: [{type: 'line', y: 'Memory', fill: '#f17b31'}],
    gridColor: '#f2f2f2',
    tipTimeFormat: "%M:%S %Z",
    style: {
        tickLabelColor: '#f2f2f2',
        legendTextColor: '#9c9898',
        legendTitleColor: '#9c9898',
        axisLabelColor: '#9c9898'
    }
};
const latencyMetadata = {names: ['Time', 'Latency'], types: ['time', 'linear']};
const latencyLineChartConfig = {
    x: 'Time',
    charts: [{type: 'line', y: 'Latency', fill: '#f17b31'}],
    gridColor: '#f2f2f2',
    tipTimeFormat: "%M:%S %Z",
    style: {
        tickLabelColor: '#f2f2f2',
        legendTextColor: '#9c9898',
        legendTitleColor: '#9c9898',
        axisLabelColor: '#9c9898'
    }
};
const tpMetadata = {names: ['Time', 'Throughput'], types: ['time', 'linear']};
const tpLineChartConfig = {
    x: 'Time',
    charts: [{type: 'line', y: 'Throughput', fill: '#f17b31'}],
    gridColor: '#f2f2f2',
    tipTimeFormat: "%M:%S %Z",
    style: {
        tickLabelColor: '#f2f2f2',
        legendTextColor: '#9c9898',
        legendTitleColor: '#9c9898',
        axisLabelColor: '#9c9898'
    }
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
 * class which manages Siddhi App specific details.
 */
export default class WorkerSpecific extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            latency: [],
            throughputAll: [],
            totalMem: [],
            workerID: this.props.match.params.id.split("_")[0] + ":" + this.props.match.params.id.split("_")[1],
            appName: this.props.match.params.appName,
            id: this.props.match.params.id,
            enabledStatLevel: this.props.match.params.isStatsEnabled,
            appText: '',
            open: false,
            messageStyle: '',
            showMsg: false,
            message: '',
            confirmMessage: '',
            hasManagerPermission: false,
            hasViewerPermission: true,
            sessionInvalid: false,
            radioValue: window.location.href.substr(window.location.href.lastIndexOf('/') + 1)
        };
        this.showMessage = this.showMessage.bind(this);
        this.showError = this.showError.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.getDetailedTable = this.getDetailedTable.bind(this);
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
                    message = this.context.intl.formatMessage({
                        id: 'authenticationFail', defaultMessage: 'Authentication fail. Please login again.'
                    });                    
                        this.setState({
                            sessionInvalid: true
                    })
                } else if (error.response.status === 403) {
                    message = this.context.intl.formatMessage({ id: 'noManagerPermission', defaultMessage: 'User Have No Manager Permission to view this page.' });
                    this.setState({
                        hasManagerPermission: false
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
                    message = this.context.intl.formatMessage({
                        id: 'authenticationFail', defaultMessage: 'Authentication fail. Please login again.'
                    });
                    this.setState({
                        sessionInvalid: true
                    })
                } else if (error.response.status === 403) {
                    message = this.context.intl.formatMessage({ id: 'noViewerPermission', defaultMessage: 'User Have No Viewer Permission to view this page.' });
                    this.setState({
                        hasViewerPermission: false
                    })
                } else {
                        message = message = this.context.intl.formatMessage({ id: 'unknownError', defaultMessage: 'Unknown error occurred! : {data}', values: { data: error.response.data } });
                }
                this.setState({
                    message: message
                })
            }
        });
        StatusDashboardAPIS.getSiddhiAppByName(this.props.match.params.id, this.props.match.params.appName)
            .then((response) => {
                that.setState({
                    appText: response.data.content
                });
                StatusDashboardAPIS.getSiddhiAppHistoryByID(this.props.match.params.id,
                    this.props.match.params.appName, '')
                    .then((response) => {
                        that.setState({
                            latency: response.data[0].latency.data,
                            throughputAll: response.data[0].throughput.data,
                            totalMem: response.data[0].memory.data
                        });
                    });
            }).catch((error) => {
            let message;
            if (error.response !== null) {
                if (error.response.status === 500) {
                    message = this.context.intl.formatMessage({
                        id: 'authenticationFail', defaultMessage: 'Authentication fail. Please login again.'
                    });
                    this.setState({
                        appText: this.context.intl.formatMessage({ id: 'appSpeicific.unableToFetchSiddhiApp', defaultMessage: 'Unable to fetch Siddhi App!' }),
                        latency: [],
                        throughputAll: [],
                        totalMem: []
                    })
                } else {
                    message = this.context.intl.formatMessage({ id: 'appSpeicific.unableToFetchSiddhiAppWithData', defaultMessage: 'Unable to fetch Siddhi App! : {data}', values: { data: error.response.data } });
                }
                this.setState({
                    message: message
                })
            }
        });
    }

    handleChange(event) {
        this.setState({ radioValue: event.target.value });
        let statEnable = JSON.stringify({
            enabledStatLevel: event.target.value,
            statsEnable: true
        });
        let that = this;
        StatusDashboardOverViewAPI.enableSiddhiAppStats(this.state.id, this.state.appName, statEnable)
            .then((response) => {
                if (response.status === HttpStatus.OK) {
                    that.showMessage(this.context.intl.formatMessage({ id: 'appSpecific.statisticsStateChanged', defaultMessage: 'Successfully Changed statistics state of Sidhhi App!' }));
                    var enabledStatLevel = this.state.radioValue;

                    if (enabledStatLevel !== this.state.enabledStatLevel) {
                        that.setState({ enabledStatLevel: enabledStatLevel });
                    }
                    that.setState({ open: false });
                    setTimeout(function () {
                        window.location.href = window.contextPath + '/worker/' + that.state.id
                            + "/siddhi-apps/" + that.state.appName + "/" + that.state.enabledStatLevel;
                    }, 1000);
                }
            }).catch((error) => {
            that.setState({open: false});
            that.showError(this.context.intl.formatMessage({ id: 'appSpecific.error.changingStatistics', defaultMessage: 'Error while changing statistics configuration!!' }));
        });
    };

    renderLatencyChart() {
        if (this.state.latency.length === 0) {
            return (
                <GridTile title={<FormattedMessage id='appSpecific.latencyMiliseconds' defaultMessage='Latency (milliseconds)' />} titlePosition="top" titleBackground='#303030'>
                    <div style={{
                        color: 'rgba(255, 255, 255, 0.2)',
                        marginTop: 50,
                        backgroundColor: '#131313',
                        padding: 30,
                        textAlign: 'center',
                        height: 300
                    }}><h2><FormattedMessage id='noData' defaultMessage='No Data Available' /></h2></div>
                </GridTile>
            );
        }
        return (
            <GridTile className="container" title={<FormattedMessage id='appSpecific.latencyMiliseconds' defaultMessage='Latency (milliseconds)' />} titlePosition="top" titleBackground='#303030'>
                <div className="overlay"
                     style={{color: 'rgba(255, 255, 255, 0.2)', paddingTop: 20, textAlign: 'right'}}>
                    <h4><FormattedMessage id='appSpecific.clickForMore' defaultMessage='Click for more details' /></h4>
                </div>
                <div style={{marginTop: 30, backgroundColor: '#131313', padding: 20}}>
                    <Link
                        to={window.contextPath + '/worker/' + this.props.match.params.id + '/siddhi-apps/' +
                        this.props.match.params.appName + '/app/history/' + this.state.enabledStatLevel}>
                        <VizG data={this.state.latency} metadata={latencyMetadata}
                              config={latencyLineChartConfig}
                              yDomain={DashboardUtils.getYDomain(this.state.latency)}
                              width={700}
                              height={300}
                        />
                    </Link>
                </div>
            </GridTile>
        );
    }

    renderThroughputChart() {
        if (this.state.throughputAll.length === 0) {
            return (
                <GridTile title={<FormattedMessage id='appSpecific.overallThroughput' defaultMessage='Overall Throughput (events/second)' />} titlePosition="top" titleBackground='#303030'>
                    <div style={{
                        color: 'rgba(255, 255, 255, 0.2)',
                        marginTop: 50,
                        backgroundColor: '#131313',
                        padding: 30,
                        textAlign: 'center',
                        height: 300
                    }}><h2><FormattedMessage id='noData' defaultMessage='No Data Available' /></h2></div>
                </GridTile>
            );
        }
        return (
            <GridTile className="container" title="Overall Throughput(events/second)" titlePosition="top"
                      titleBackground='#303030'>
                <div className="overlay"
                     style={{color: 'rgba(255, 255, 255, 0.2)', paddingTop: 20, textAlign: 'right'}}>
                    <h4><FormattedMessage id='appSpecific.clickForMore' defaultMessage='Click for more details' /></h4>
                </div>
                <div style={{marginTop: 30, backgroundColor: '#131313', padding: 20}}>
                    <Link
                        to={window.contextPath + '/worker/' + this.props.match.params.id + '/siddhi-apps/' +
                        this.props.match.params.appName + '/app/history/' + this.state.enabledStatLevel}>
                        <VizG data={this.state.throughputAll} metadata={tpMetadata}
                              config={tpLineChartConfig}
                              yDomain={DashboardUtils.getYDomain(this.state.throughputAll)}
                              width={700}
                              height={300}
                        />
                    </Link>
                </div>
            </GridTile>
        );
    }

    renderMemoryChart() {
        if (this.state.totalMem.length === 0) {
            return (
                <GridTile title={<FormattedMessage id='appSpecific.memoryUsed' defaultMessage='Memory Used (bytes)' />} titlePosition="top" titleBackground='#303030'>
                    <div style={{
                        marginTop: 50,
                        backgroundColor: '#131313',
                        padding: 30,
                        textAlign: 'center',
                        height: 300,
                        color: 'rgba(255, 255, 255, 0.2)'
                    }}><h2><FormattedMessage id='noData' defaultMessage='No Data Available' /></h2></div>
                </GridTile>
            );
        }
        return (
            <GridTile className="container" title={<FormattedMessage id='appSpecific.memoryUsed' defaultMessage='Memory Used (bytes)' />} titlePosition="top"
                titleBackground='#303030'>
                <div className="overlay"
                     style={{color: 'rgba(255, 255, 255, 0.2)', paddingTop: 20, textAlign: 'right'}}>
                    <h4><FormattedMessage id='appSpecific.clickForMore' defaultMessage='Click for more details' /></h4>
                </div>
                <div style={{marginTop: 30, backgroundColor: '#131313', padding: 20}}>
                    <Link
                        to={window.contextPath + '/worker/' + this.props.match.params.id + '/siddhi-apps/' +
                        this.props.match.params.appName + '/app/history/' + this.state.enabledStatLevel}>
                        <VizG data={this.state.totalMem} metadata={memoryMetadata}
                              config={memoryLineChartConfig}
                              yDomain={DashboardUtils.getYDomain(this.state.totalMem)}
                              width={700}
                              height={300}
                        />

                    </Link>
                </div>
            </GridTile>
        );
    }

    /**
     * Method which render metrics enable toggle button if permission is granted
     * @param workersList
     * @returns {XML}
     */
    renderToggle() {
        const enableMessage = this.context.intl.formatMessage(
            {
                id: 'appSpecific.enableAppMetrics',
                defaultMessage: 'Do you want to enable Siddhi App metrics?'
            });
        const disableMessage = this.context.intl.formatMessage({
            id: 'appSpecific.disableMetrics',
            defaultMessage: 'Disabling metrics of a SiddhiApp will cause a data loss.{br} Are you sure you want to disable metrics?',
            values: { br: (<br />) }
        })
        if (this.state.hasManagerPermission) {
            return (
                <div style={{position: 'absolute', right: 15, top: 110}}>
                    <FormControl component="fieldset">
                        <FormLabel style={{color: '#dedede'}} component="legend">Metrics:</FormLabel>
                        <RadioGroup
                            aria-label="metrics"
                            name="metrics"
                            value={this.state.radioValue}
                            onChange={this.handleChange}
                            style={{flexDirection: 'row', marginTop: '-30px', paddingLeft: 70}}
                            className={'metricsRadio'}
                        >
                            <FormControlLabel value="OFF" control={<Radio />} label="Off" />
                            <FormControlLabel value="BASIC" control={<Radio />} label="Basic" />
                            <FormControlLabel value="DETAIL" control={<Radio />} label="Detail" />
                        </RadioGroup>
                    </FormControl>
                </div>
            )
        }
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
        let warningMessage;
        if (this.state.enabledStatLevel === "OFF") {
            warningMessage = <div>
                <FormattedMessage id='appSpecific.metricsDisabled' defaultMessage='Metrics are disabled!' />
            </div>
        } else {
            warningMessage = <div/>
        }
        return (
            <div>
                <div>
                    <Header/>
                    <div style={styles.navBar} className="navigation-bar">
                        <Link style={{textDecoration: 'none'}} to={window.contextPath}>
                            <Button style={styles.navBtn}>
                                <HomeButton style={{paddingRight: 8, color: '#BDBDBD'}}/>
                                <FormattedMessage id='overview' defaultMessage='Overview >' />
                            </Button>
                        </Link>
                        <Link style={{textDecoration: 'none'}} to={window.contextPath + '/worker/' +
                            this.props.match.params.id}>
                            <Button style={styles.navBtn}>
                                {this.state.workerID} >
                            </Button>
                        </Link>
                        <Typography style={styles.navBtnActive}>{this.props.match.params.appName}</Typography>
                    </div>
                    <Typography variant="title" style={styles.titleStyle}>
                        {this.state.workerID} : {this.state.appName}
                    </Typography>

                    <div style={{display: 'inline-block', color: '#8c060a', marginLeft: '45%', fontSize: '20px'}}>
                        {warningMessage}
                    </div>
                    {this.renderToggle()}
                    <GridList cols={3} cellHeight={250} style={styles.gridList}>
                        {this.renderLatencyChart()}
                        {this.renderThroughputChart()}
                        {this.renderMemoryChart()}
                    </GridList>
                </div>

                <div style={{padding: 24, height: '50%', backgroundColor: "#222222"}}>
                    <Card style={{backgroundColor: "#282828", height: '50%'}}>
                        <CardHeader title={<FormattedMessage id='appSpecific.codeView' defaultMessage='Code View' />}
                            titleStyle={{ fontSize: 24, backgroundColor: "#282828" }}
                        />
                        <Divider/>

                        <CardText>
                            <SyntaxHighlighter language='sql'
                                               style={codeViewStyle}>{this.state.appText}</SyntaxHighlighter>
                        </CardText>
                    </Card>
                </div>

                <div style={{padding: 24, height: '50%', backgroundColor: "#222222"}}>
                    <Card style={{backgroundColor: "#282828", height: '50%'}}>
                        <CardHeader title={<FormattedMessage id='appSpecific.designView' defaultMessage='Design View' />}
                            titleStyle={{ fontSize: 24, backgroundColor: "#282828" }}
                        />
                        <Divider/>

                        <CardText style={{padding: '10px'}}>
                            <ul class="legend">
                                <li class="legend-key ">
                                    <span class="legend-colour source-image">

                                    </span>
                                    <span class="legend-text"><FormattedMessage id='appSpecific.source' defaultMessage='Source' /></span>
                                </li>
                                <li class="legend-key ">
                                    <span class="legend-colour sink-image">

                                    </span>
                                    <span class="legend-text"><FormattedMessage id='appSpecific.sink' defaultMessage='Sink' /></span>
                                </li>
                                <li class="legend-key ">
                                    <span class="legend-colour stream-image">
                                    </span>
                                    <span class="legend-text"><FormattedMessage id='appSpecific.stream' defaultMessage='Stream' /></span>
                                </li>
                                <li class="legend-key ">
                                    <span class="legend-colour table-image"></span>
                                    <span class="legend-text"><FormattedMessage id='appSpecific.table' defaultMessage='Table' /></span>
                                </li>
                                <li class="legend-key ">
                                    <span class="legend-colour window-image"></span>
                                    <span class="legend-text"><FormattedMessage id='appSpecific.window' defaultMessage='Window' /></span>
                                </li>
                                <li class="legend-key ">
                                    <span class="legend-colour trigger-image"></span>
                                    <span class="legend-text"><FormattedMessage id='appSpecific.trigger' defaultMessage='Trigger' /></span>
                                </li>
                                <li class="legend-key ">
                                    <span class="legend-colour aggregation-image"></span>
                                    <span class="legend-text"><FormattedMessage id='appSpecific.aggregation' defaultMessage='Aggregation' /></span>
                                </li>
                                <li class="legend-key ">
                                    <span class="legend-colour function-image"></span>
                                    <span class="legend-text"><FormattedMessage id='appSpecific.function' defaultMessage='Function' /></span>
                                </li>
                                <li class="legend-key ">
                                    <span class="legend-colour query-image"></span>
                                    <span class="legend-text"><FormattedMessage id='appSpecific.query' defaultMessage='Query' /></span>
                                </li>
                                <li class="legend-key ">
                                    <span class="legend-colour partition-image"></span>
                                    <span class="legend-text"><FormattedMessage id='appSpecific.partition' defaultMessage='Partition' /></span>
                                </li>
                            </ul>

                            <AppEventFlow id={this.props.match.params.id} appName={this.props.match.params.appName}/>
                        </CardText>
                    </Card>
                </div>

                <div style={{padding: 24}}>
                    <h3 style={{ color: 'white' }}>
                        <FormattedMessage id='appSpecific.siddhiAppComponent' defaultMessage='Siddhi App Component Statistics' />
                    </h3>
                    <h4 style={{ color: 'white' }}>
                        <FormattedMessage id='appSpecific.siddhiAppComponent.basic' defaultMessage='Basic Statistics' />
                    </h4>
                    <ComponentTable id={this.props.match.params.id}
                                    appName={this.props.match.params.appName}
                                    enabledStatLevel={'BASIC'}/>
                    {this.getDetailedTable()}
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

    getDetailedTable() {
        if (this.state.enabledStatLevel === 'DETAIL') {
            return (
                <div style={{ display: this.state.enabledStatLevel === 'DETAIL' ? 'block' : 'none' }}>
                    <h4 style={{ color: 'white' }}>
                        <FormattedMessage id='appSpecific.siddhiAppComponent.detail' defaultMessage='Detail Statistics' />
                    </h4>
                    <ComponentTable id={this.props.match.params.id}
                                    appName={this.props.match.params.appName}
                                    enabledStatLevel={'DETAIL'}/>
                </div>
            );
        }
    }
}


WorkerSpecific.contextTypes = {
    intl: PropTypes.object.isRequired
}
