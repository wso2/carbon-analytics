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
//App Components
import StatusDashboardAPIS from '../utils/apis/StatusDashboardAPIs';
import {HttpStatus} from '../utils/Constants';
import ComponentTable from './ComponentTable';
import VizG from 'react-vizgrammar';
import Header from '../common/Header';
//Material UI
import {GridList, GridTile} from 'material-ui/GridList';
import HomeButton from 'material-ui/svg-icons/action/home';
import {Card, CardHeader, CardText, Dialog, Divider, FlatButton, RaisedButton, Snackbar, Toggle} from 'material-ui';
import DashboardUtils from '../utils/DashboardUtils';
import AuthenticationAPI from '../utils/apis/AuthenticationAPI';
import AuthManager from '../auth/utils/AuthManager';
import Error403 from '../error-pages/Error403';
import StatusDashboardOverViewAPI from '../utils/apis/StatusDashboardOverViewAPI';
import AppEventFlow from "./AppEventFlow";

const styles = {
    root: {display: 'flex', flexWrap: 'wrap', justifyContent: 'space-around'},
    gridList: {width: '90%', height: '50%', overflowY: 'auto', padding: 10, paddingLeft: 60}
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
const enableMessage = "Do you want to enable Siddhi App metrics?";
const disableMessage = "Disabling metrics of a SiddhiApp will cause a data loss. \n " +
    "Are you sure you want to disable metrics?";
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
            statsEnabled: (this.props.match.params.isStatsEnabled === 'true'),
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
        this.handleToggle = this.handleToggle.bind(this);
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
                    message = "Authentication fail. Please login again.";
                    this.setState({
                        sessionInvalid: true
                    })
                } else if (error.response.status === 403) {
                    message = "User Have No Manager Permission to view this page.";
                    this.setState({
                        hasManagerPermission: false
                    })
                } else {
                    message = "Unknown error occurred! : " + error.response.data;
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
                    message = "Authentication fail. Please login again.";
                    this.setState({
                        sessionInvalid: true
                    })
                } else if (error.response.status === 403) {
                    message = "User Have No Viewer Permission to view this page.";
                    this.setState({
                        hasViewerPermission: false
                    })
                } else {
                    message = "Unknown error occurred! : " + error.response.data;
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
            });
    }

    renderLatencyChart() {
        if (this.state.latency.length === 0) {
            return (
                <GridTile title="Latency(milliseconds)" titlePosition="top" titleBackground='#303030'>
                    <div style={{
                        color: 'rgba(255, 255, 255, 0.2)',
                        marginTop: 50,
                        backgroundColor: '#131313',
                        padding: 30,
                        textAlign: 'center',
                        height: 300
                    }}><h2>No Data Available</h2></div>
                </GridTile>
            );
        }
        return (
            <GridTile className="container" title="Latency(milliseconds)" titlePosition="top" titleBackground='#303030'>
                <div className="overlay"
                     style={{color: 'rgba(255, 255, 255, 0.2)', paddingTop: 20, textAlign: 'right'}}>
                    <h4>Click for more details</h4>
                </div>
                <div style={{marginTop: 30, backgroundColor: '#131313', padding: 20}}>
                    <Link
                        to={window.contextPath + '/worker/' + this.props.match.params.id + '/siddhi-apps/' +
                        this.props.match.params.appName + '/app/history/' + this.state.statsEnabled}>
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
                <GridTile title="Overall Throughput(events/second)" titlePosition="top" titleBackground='#303030'>
                    <div style={{
                        color: 'rgba(255, 255, 255, 0.2)',
                        marginTop: 50,
                        backgroundColor: '#131313',
                        padding: 30,
                        textAlign: 'center',
                        height: 300
                    }}><h2>No Data Available</h2></div>
                </GridTile>
            );
        }
        return (
            <GridTile className="container" title="Overall Throughput(events/second)" titlePosition="top"
                      titleBackground='#303030'>
                <div className="overlay"
                     style={{color: 'rgba(255, 255, 255, 0.2)', paddingTop: 20, textAlign: 'right'}}>
                    <h4>Click for more details</h4>
                </div>
                <div style={{marginTop: 30, backgroundColor: '#131313', padding: 20}}>
                    <Link
                        to={window.contextPath + '/worker/' + this.props.match.params.id + '/siddhi-apps/' +
                        this.props.match.params.appName + '/app/history/' + this.state.statsEnabled}>
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
                <GridTile title="Memory Used(bytes)" titlePosition="top" titleBackground='#303030'>
                    <div style={{
                        marginTop: 50,
                        backgroundColor: '#131313',
                        padding: 30,
                        textAlign: 'center',
                        height: 300,
                        color: 'rgba(255, 255, 255, 0.2)'
                    }}><h2>No Data Available</h2></div>
                </GridTile>
            );
        }
        return (
            <GridTile className="container" title="Memory Used(bytes)" titlePosition="top"
                      titleBackground='#303030'>
                <div className="overlay"
                     style={{color: 'rgba(255, 255, 255, 0.2)', paddingTop: 20, textAlign: 'right'}}>
                    <h4>Click for more details</h4>
                </div>
                <div style={{marginTop: 30, backgroundColor: '#131313', padding: 20}}>
                    <Link
                        to={window.contextPath + '/worker/' + this.props.match.params.id + '/siddhi-apps/' +
                        this.props.match.params.appName + '/app/history/' + this.state.statsEnabled}>
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
        if (this.state.hasManagerPermission) {
            return (
                <div style={{float: 'right', padding: 20, paddingRight: 20}}>
                    <Toggle labelPosition="left"
                            label="Metrics"
                            labelStyle={{color: 'white'}}
                            thumbStyle={{backgroundColor: 'grey'}}
                            thumbSwitchedStyle={{backgroundColor: '#f17b31'}}
                            trackSwitchedStyle={{backgroundColor: '#f17b31'}}
                            toggled={this.state.statsEnabled}
                            onToggle={() => {
                                this.setState({
                                    open: true,
                                    confirmMessage: this.state.statsEnabled ? disableMessage : enableMessage
                                })
                            }}
                    >
                    </Toggle>

                </div>
            )
        } else {
            return (
                <div style={{float: 'right', padding: 20, paddingRight: 20, display: 'none'}}>
                    <Toggle labelPosition="left"
                            label="Metrics"
                            labelStyle={{color: 'white'}}
                            thumbStyle={{backgroundColor: 'grey'}}
                            thumbSwitchedStyle={{backgroundColor: '#f17b31'}}
                            trackSwitchedStyle={{backgroundColor: '#f17b31'}}
                            toggled={this.state.statsEnabled}
                            onToggle={() => {
                                this.setState({
                                    open: true,
                                    confirmMessage: this.state.statsEnabled ? disableMessage : enableMessage
                                })
                            }}
                    >
                    </Toggle>
                </div>
            )
        }
    }


    handleToggle() {
        let statEnable = JSON.stringify({
            statsEnable: !this.state.statsEnabled
        });
        let that = this;
        StatusDashboardOverViewAPI.enableSiddhiAppStats(this.state.id, this.state.appName, statEnable)
            .then((response) => {
                if (response.status === HttpStatus.OK) {
                    that.showMessage("Successfully Changed statistics state of Sidhhi App!");
                    that.setState({statsEnabled: !this.state.statsEnabled, open: false});
                    setTimeout(function () {
                        window.location.href = window.contextPath + '/worker/' + that.state.id
                            + "/siddhi-apps/" + that.state.appName + "/" + that.state.statsEnabled;
                    }, 1000);
                }
            }).catch((error) => {
            that.setState({open: false});
            that.showError("Error while changing statistics configuration!!");
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
                label="Yes"
                backgroundColor='#f17b31'
                onClick={this.handleToggle}
                //disabled={!this.state.hasManagerPermission}
            />,
            <FlatButton
                label="No"
                //disabled={!this.state.hasManagerPermission}
                onClick={() => {
                    this.setState({open: false})
                }}
            />,
        ];
        let warningMessage;
        if (!this.state.statsEnabled) {
            warningMessage = <div>
                Metrics are disabled!
            </div>
        } else {
            warningMessage = <div/>
        }
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
                    <div className="navigation-bar">
                        <Link to={window.contextPath}><FlatButton label="Overview >"
                                                                  icon={<HomeButton color="black"/>}/></Link>
                        <Link to={window.contextPath + '/worker/' + this.props.match.params.id}>
                            <FlatButton label={this.state.workerID + " >"}/></Link>
                        <RaisedButton label={this.props.match.params.appName} disabled disabledLabelColor='white'
                                      disabledBackgroundColor='#f17b31'/>
                    </div>
                    <div className="worker-h1">
                        <h2 style={{display: 'inline-block', float: 'left', marginLeft: 40}}> {this.state.workerID}
                            : {this.state.appName} </h2>
                    </div>

                    <div style={{display: 'inline-block', color: '#8c060a', marginLeft: '60%', fontSize: '20px'}}>
                        {warningMessage}
                    </div>
                    {this.renderToggle()}
                    <GridList cols={3} padding={35} cellHeight={250} style={styles.gridList}>
                        {this.renderLatencyChart()}
                        {this.renderThroughputChart()}
                        {this.renderMemoryChart()}
                    </GridList>
                </div>

                <div style={{padding: 10, paddingLeft: 40, width: '90%', height: '50%', backgroundColor: "#222222"}}>
                    <Card style={{backgroundColor: "#282828", height: '50%'}}>
                        <CardHeader title="Code View" subtitle={this.props.match.params.appName}
                                    titleStyle={{fontSize: 24, backgroundColor: "#282828"}}
                        />
                        <Divider/>

                        <CardText>
                            <SyntaxHighlighter language='sql'
                                               style={codeViewStyle}>{this.state.appText}</SyntaxHighlighter>
                        </CardText>
                    </Card>
                </div>

                <div style={{padding: 10, paddingLeft: 40, width: '90%', height: '50%', backgroundColor: "#222222"}}>
                    <Card style={{backgroundColor: "#282828", height: '50%'}}>
                        <CardHeader title="Design View" subtitle={this.props.match.params.appName}
                                    titleStyle={{fontSize: 24, backgroundColor: "#282828"}}
                        />
                        <Divider/>

                        <CardText style={{padding: '80px'}}>
                            <ul class="legend">
                                <li class="legend-key ">
                                    <span class="legend-colour stream-image" >

                                    </span>
                                    <span class="legend-text">Stream</span>
                                </li>
                                <li class="legend-key ">
                                    <span class="legend-colour table-image" ></span>
                                    <span class="legend-text">Table</span>
                                </li>
                                <li class="legend-key ">
                                    <span class="legend-colour window-image"></span>
                                    <span class="legend-text">Window</span>
                                </li>
                                <li class="legend-key ">
                                    <span class="legend-colour trigger-image" ></span>
                                    <span class="legend-text">Trigger</span>
                                </li>
                                <li class="legend-key ">
                                    <span class="legend-colour aggregation-image"></span>
                                    <span class="legend-text">Aggregation</span>
                                </li>
                                <li class="legend-key ">
                                    <span class="legend-colour function-image"></span>
                                    <span class="legend-text">Function</span>
                                </li>
                                <li class="legend-key ">
                                    <span class="legend-colour query-image"></span>
                                    <span class="legend-text">Query</span>
                                </li>
                                <li class="legend-key ">
                                    <span class="legend-colour partition-image"></span>
                                    <span class="legend-text">Partition</span>
                                </li>
                            </ul>

                            <AppEventFlow id={this.props.match.params.id} appName={this.props.match.params.appName}/>
                        </CardText>
                    </Card>
                </div>

                <div style={{width: '90%', marginLeft: 40}}>
                    <h3 style={{color: 'white'}}> Siddhi App Component Statistics</h3>
                    <ComponentTable id={this.props.match.params.id} appName={this.props.match.params.appName}
                                    statsEnabled={this.state.statsEnabled}/>
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


