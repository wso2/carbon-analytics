import React, {Component} from "react";
import Widget from "@wso2-dashboards/widget";
import VizG from 'react-vizgrammar';
import {MuiThemeProvider, darkBaseTheme, createMuiTheme} from '@material-ui/core/styles';
import {MuiThemeProvider as V0MuiThemeProvider} from 'material-ui';
import _ from 'lodash';
import Pagination from 'material-ui-pagination';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';


let widgetPseudoId = "BarChardWidget_1_1_1";

const colorGreen = "#6ED460";
const colorRed = "#EC5D40";

const theme = createMuiTheme(darkBaseTheme);

const dataPerPage = 3;

const borderRadius = {
    small: 2,
    medium: 4,
    large: 6,
};

const paginationButtonStyle = {
    color: 'blue',
    fontWeight: '700',
    display: 'inline-block',
    verticalAlign: 'text-bottom',
    padding: '0 10px',
    cursor: 'pointer',
    borderRadius: 5,
};

let successMetadata = {
    names: ['username', 'authStepSuccessCount'],
    types: ['ordinal', 'linear']
};

let failureMetadata = {
    names: ['username', 'authFailureCount'],
    types: ['ordinal', 'linear']
};

let chartConfigSuccess = {
    x: "username",
    charts: [
        {
            type: "bar",
            orientation: "left",
            y: "authStepSuccessCount",
            fill: colorGreen,
        },
    ],
    yAxisLabel: "Successful Attempts",
    xAxisLabel: "Username",
    yAxisTickCount: 6,
    linearSeriesStep: 1,
    append: false,
    height: 200,
    width: 350,
};

let chartConfigFailure = {
    x: "username",
    charts: [
        {
            type: "bar",
            orientation: "left",
            y: "authFailureCount",
            fill: colorRed,
        }
    ],
    yAxisLabel: "Failure Attempts",
    xAxisLabel: "Username",
    yAxisTickCount: 6,
    linearSeriesStep: 1,
    append: false,
    height: 200,
    width: 350,
};

class IsAnalyticsHorizontalBarChart extends Widget {
    constructor(props) {
        super(props);

        this.state = {
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,

            chartConfigSuccess: chartConfigSuccess,
            chartConfigFailure: chartConfigFailure,
            successData: [],
            failureData: [],
            currentSuccessDataSet: [],
            currentFailureDataSet: [],
            successMetadata: successMetadata,
            failureMetadata: failureMetadata,
            options: this.props.configs.options,
            currentSuccessPageNumber: 1,
            currentFailurePageNumber: 1,
            faultyProviderConf: false,
            widgetPseudoId: widgetPseudoId,
        };

        this._handleSuccessDataReceived = this._handleSuccessDataReceived.bind(this);
        this._handleFailureDataReceived = this._handleFailureDataReceived.bind(this);
        this.assembleQuery = this.assembleQuery.bind(this);
        this.setReceivedMsg = this.setReceivedMsg.bind(this);
        this.updateTable = this.updateTable.bind(this);
        this.processFirstLogins = this.processFirstLogins.bind(this);

        this.props.glContainer.on('resize', () =>
            this.setState({
                width: this.props.glContainer.width,
                height: this.props.glContainer.height,
            })
        );
    }

    componentDidMount() {
        let successMetadataClone = _.cloneDeep(successMetadata);
        let failureMetadataClone = _.cloneDeep(failureMetadata);
        let chartConfigSuccessClone = _.cloneDeep(chartConfigSuccess);
        let chartConfigFailureClone = _.cloneDeep(chartConfigFailure);

        let widgetPseudoId;

        let xAxisLabel = "";
        let xAxisValue = "";
        let header = "By ";

        let doFirstLoginTabNeeded = false;

        // For Overall authentication, service provider widget needs a firstLogin tab
        if (this.state.options.xAxis === "Service Provider" && this.state.options.widgetType === "Overall") {
            doFirstLoginTabNeeded = true;
        }

        switch (this.state.options.xAxis) {
            case "Service Provider":
                xAxisLabel = "Service Provider";
                header = header + "Service Provider";
                xAxisValue = "serviceProvider";
                break;
            case "User Store Domain":
                xAxisLabel = "User Store Domain";
                header = header + "User Store Domain";
                xAxisValue = "userStoreDomain";
                break;
            case "Role":
                xAxisLabel = "Role";
                header = header + "Role";
                xAxisValue = "role";
                break;
            case "Identity Provider":
                xAxisLabel = "Identity Provider";
                header = header + "Identity Provider";
                xAxisValue = "identityProvider";
                break;
            default:
                xAxisLabel = "Username";
                header = header + "Username";
                xAxisValue = "username";
        }

        widgetPseudoId = this.state.options.widgetType + xAxisValue + "_failure";

        chartConfigSuccessClone.x = xAxisValue;
        chartConfigSuccessClone.xAxisLabel = xAxisLabel;
        chartConfigFailureClone.x = xAxisValue;
        chartConfigFailureClone.xAxisLabel = xAxisLabel;

        successMetadataClone.names[0] = xAxisValue;
        failureMetadataClone.names[0] = xAxisValue;

        if (this.state.options.widgetType == "Local") {
            let value = "authSuccessCount";
            successMetadataClone.names[1] = value;
            chartConfigSuccessClone.charts[0].y = value;
        } else {
            let value = "authStepSuccessCount";
            successMetadataClone.names[1] = value;
            chartConfigSuccessClone.charts[0].y = value;
        }

        super.subscribe(this.setReceivedMsg);
        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {
                this.setState({
                    dataProviderConf: message.data.configs.providerConfig,
                    successMetadata: successMetadataClone,
                    failureMetadata: failureMetadataClone,
                    chartConfigSuccess: chartConfigSuccessClone,
                    chartConfigFailure: chartConfigFailureClone,
                    widgetPseudoId: widgetPseudoId,
                    header: header,
                    doFirstLoginTabNeeded: doFirstLoginTabNeeded,
                });
            })
            .catch((error) => {
                console.error("[ERROR]: ", error);
                this.setState({
                    faultyProviderConf: true
                });
            });
    }

    shouldComponentUpdate(nextProps, nextState) {
        let failureResult = (this.state.currentFailureDataSet != nextState.currentFailureDataSet);
        let successResult = (this.state.currentSuccessPageNumber != nextState.currentSuccessPageNumber);

        return (failureResult || successResult);
    }

    _handleSuccessDataReceived(message) {
        this.updateTable(message.data, this.state.currentSuccessPageNumber, true);
        window.dispatchEvent(new Event('resize'));
    }

    _handleFailureDataReceived(message) {
        this.updateTable(message.data, this.state.currentFailurePageNumber, false);
        window.dispatchEvent(new Event('resize'));
    }

    /*
     * Data is also passed into update table function to reduce the number of this.setState() calls.
     * Otherwise the resulting chart will get more cycles to update, which will left user in ambiguity.
     */
    updateTable(data, pageNumber, isSuccess) {
        let internalPageNumber = pageNumber - 1; // Internally pages are counted from 0, to make logic easy.

        let startPoint = internalPageNumber * dataPerPage;
        let endPoint = startPoint + dataPerPage;
        let totalPageCount = Math.ceil(data.length / dataPerPage);

        if (pageNumber < 1) {
            console.error("[ERROR]: Wrong page number", pageNumber,
                "Provided. Page number should be positive integer.");
        } else if (pageNumber > totalPageCount) {
            console.error("[ERROR]: Wrong page number", pageNumber,
                "Provided. Page number exceeds total page count, ", totalPageCount);
        }

        if (isSuccess) {
            let dataLength = data.length;

            if (endPoint > dataLength) {
                endPoint = dataLength;
            }
            let dataSet = data.slice(startPoint, endPoint);

            this.setState({
                successData: data,
                currentSuccessDataSet: dataSet,
                currentSuccessPageNumber: pageNumber,
                successPageCount: totalPageCount,
            });
        } else {
            let dataLength = data.length;

            if (endPoint > dataLength) {
                endPoint = dataLength;
            }
            let dataSet = data.slice(startPoint, endPoint);

            this.setState({
                failureData: data,
                currentFailureDataSet: dataSet,
                currentFailurePageNumber: pageNumber,
                failurePageCount: totalPageCount,
            });
        }
    }

    setReceivedMsg(receivedMsg) {
        this.setState({
            per: receivedMsg.granularity,
            fromDate: receivedMsg.from,
            toDate: receivedMsg.to,
            successData: [],
            failureData: [],
            currentSuccessDataSet: [],
            currentFailureDataSet: [],
        }, () => {
            this.assembleQuery(false)
        });
    }

    assembleQuery(isFirstLoginNeeded) {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
        let dataProviderConfigsSuccess = _.cloneDeep(this.state.dataProviderConf);
        let query = dataProviderConfigsSuccess.configs.config.queryData.query;
        let countType = "authStepSuccessCount";
        let filterCondition = " on ";
        let idpFilter = " identityProviderType=='{{idpType}}' ";
        let firstLoginFilter = " authFirstSuccessCount > 0 ";
        let doIdpFilter = false;
        let xAxisValue = "";
        let aggregationName = "AuthenticationStatAggregation";

        switch (this.state.options.xAxis) {
            case "Service Provider":
                xAxisValue = "serviceProvider";
                break;
            case "User Store Domain":
                xAxisValue = "userStoreDomain";
                break;
            case "Role":
                aggregationName = "RoleAggregation";
                xAxisValue = "role";
                break;
            case "Identity Provider":
                xAxisValue = "identityProvider";
                break;
            default:
                xAxisValue = "username";
        }

        // First Logins are filtered in Overall login attempts only.
        // Hence IDP filter and the First Login filter will not overlap

        if (isFirstLoginNeeded) {
            countType = "authFirstSuccessCount";
            filterCondition = filterCondition + firstLoginFilter;
        } else {
            if (this.state.options.widgetType === "Local") {
                countType = "authSuccessCount";
            } else {
                countType = "authStepSuccessCount";
            }
        }

        if (this.state.options.widgetType === "Local") {
            idpFilter = idpFilter.replace("{{idpType}}", "LOCAL");
            doIdpFilter = true;
        } else if (this.state.options.widgetType === "Federated") {
            idpFilter = idpFilter.replace("{{idpType}}", "FEDERATED");
            doIdpFilter = true;
        }

        if (doIdpFilter) {
            filterCondition = filterCondition + idpFilter;
        }

        if (doIdpFilter || isFirstLoginNeeded) {
            query = query.replace("{{filterCondition}}", filterCondition);
        } else {
            query = query.replace("{{filterCondition}}", "");
        }

        query = query
            .replace("{{per}}", this.state.per)
            .replace("{{from}}", this.state.fromDate)
            .replace("{{to}}", this.state.toDate)
            .replace("{{AggregationName}}", aggregationName)
            .replace(/{{xAxisValue}}/g, xAxisValue);

        let querySuccess = query.replace(/{{yAxisValue}}/g, countType);
        console.log("Success: ", querySuccess);

        dataProviderConfigsSuccess.configs.config.queryData.query = querySuccess;

        super.getWidgetChannelManager().subscribeWidget(this.props.id,
            this._handleSuccessDataReceived, dataProviderConfigsSuccess);

        super.getWidgetChannelManager().unsubscribeWidget(widgetPseudoId);

        let dataProviderConfigsFailure = _.cloneDeep(this.state.dataProviderConf);
        let queryFailure = query
            .replace()
            .replace(/{{yAxisValue}}/g, "authFailureCount");
        console.log("Failure: ", queryFailure);
        dataProviderConfigsFailure.configs.config.queryData.query = queryFailure;

        super.getWidgetChannelManager().subscribeWidget(this.state.widgetPseudoId,
            this._handleFailureDataReceived, dataProviderConfigsFailure);
    }

    // This is only used for create tabs (In service provider vs. succes/failure count in Overall page
    processFirstLogins(event, value) {
        if (value === 0) {
            this.assembleQuery(false);
        } else if (value === 1) {
            this.assembleQuery(true);
        }
    }

    render() {
        const noOfPagesInPaginationNavigation = 3;

        if (this.state.faultyProviderConf) {
            return (
                <MuiThemeProvider theme={theme}>
                    <div style={{padding: 24}}>
                        <h3>{this.state.header}</h3>
                        Unable to fetch data, please check the data provider configurations.
                    </div>
                </MuiThemeProvider>
            );
        }
        if (this.state.currentSuccessDataSet.length === 0 && this.state.currentFailureDataSet.length === 0) {
            return (
                <MuiThemeProvider theme={theme}>
                    <div style={{padding: 24}}>
                        <h3>{this.state.header}</h3>
                        <h5>No Data to Show</h5>
                    </div>
                </MuiThemeProvider>
            );
        }
        else if (this.state.currentSuccessDataSet.length === 0) {
            if (this.state.doFirstLoginTabNeeded) {
                return (
                    <MuiThemeProvider theme={theme}>
                        <div style={{height: this.state.height}}>
                            <div style={{height: this.state.height * 0.1}}>
                                <h3>{this.state.header}</h3>
                            </div>
                            <div style={{height: this.state.height * 0.1}}>
                                <Tabs
                                    onChange={this.processFirstLogins}
                                    value={false}
                                    indicatorColor="secondary"
                                    textColor="primary"
                                >
                                    <Tab label="By All"/>
                                    <Tab label="By First Logins"/>
                                </Tabs>
                            </div>
                            <div style={{height: this.state.height * 0.8}}>
                                <div style={{height: this.state.height * 0.7}}>
                                    <VizG config={this.state.chartConfigFailure}
                                          metadata={this.state.failureMetadata}
                                          data={this.state.currentFailureDataSet}
                                          theme={this.props.muiTheme.name}
                                    />
                                </div>
                                <div style={{height: this.state.height * 0.1}}>
                                    <Pagination
                                        total={this.state.failurePageCount}
                                        current={this.state.currentFailurePageNumber}
                                        display={noOfPagesInPaginationNavigation}
                                        onChange={number => this.updateTable(this.state.failureData, number, false)}
                                        styleButton={{paginationButtonStyle}}
                                    />
                                </div>
                            </div>
                        </div>
                    </MuiThemeProvider>
                );
            }
            return (
                <MuiThemeProvider theme={theme}>
                    <div style={{height: this.state.height}}>
                        <div style={{height: this.state.height * 0.1}}>
                            <h3>{this.state.header}</h3>
                        </div>
                        <div style={{height: this.state.height * 0.1}}/>
                        <div style={{height: this.state.height * 0.8}}>
                            <div style={{height: this.state.height * 0.7}}>
                                <VizG config={this.state.chartConfigFailure}
                                      metadata={this.state.failureMetadata}
                                      data={this.state.currentFailureDataSet}
                                      theme={this.props.muiTheme.name}
                                />
                            </div>
                            <div style={{height: this.state.height * 0.1}}>
                                <Pagination
                                    total={this.state.failurePageCount}
                                    current={this.state.currentFailurePageNumber}
                                    display={noOfPagesInPaginationNavigation}
                                    onChange={number => this.updateTable(this.state.failureData, number, false)}
                                    styleButton={{paginationButtonStyle}}
                                />
                            </div>
                        </div>
                    </div>
                </MuiThemeProvider>
            );
        }
        else if (this.state.currentFailureDataSet.length === 0) {
            if (this.state.doFirstLoginTabNeeded) {
                return (
                    <MuiThemeProvider theme={theme}>
                        <div style={{height: this.state.height}}>
                            <div style={{height: this.state.height * 0.1}}>
                                <h3>{this.state.header}</h3>
                            </div>
                            <div style={{height: this.state.height * 0.1}}>
                                <Tabs
                                    onChange={this.processFirstLogins}
                                    value={false}
                                    indicatorColor="primary"
                                    textColor="primary"
                                >
                                    <Tab label="By All"/>
                                    <Tab label="By First Logins"/>
                                </Tabs>
                            </div>
                            <div style={{height: this.state.height * 0.8}}>
                                <div style={{height: this.state.height * 0.7}}>
                                    <VizG config={this.state.chartConfigSuccess}
                                          metadata={this.state.successMetadata}
                                          data={this.state.currentSuccessDataSet}
                                          theme={this.props.muiTheme.name}
                                    />
                                </div>
                                <div style={{height: this.state.height * 0.1}}>
                                    <Pagination
                                        total={this.state.successPageCount}
                                        current={this.state.currentSuccessPageNumber}
                                        display={noOfPagesInPaginationNavigation}
                                        onChange={number => this.updateTable(this.state.successData, number, true)}
                                        styleButton={{paginationButtonStyle}}
                                    />
                                </div>
                            </div>
                        </div>
                    </MuiThemeProvider>
                );
            }
            return (
                <MuiThemeProvider theme={theme}>
                    <div style={{height: this.state.height}}>
                        <div style={{height: this.state.height * 0.1}}>
                            <h3>{this.state.header}</h3>
                        </div>
                        <div style={{height: this.state.height * 0.1}}/>
                        <div style={{height: this.state.height * 0.8}}>
                            <div style={{height: this.state.height * 0.7}}>
                                <VizG config={this.state.chartConfigSuccess}
                                      metadata={this.state.successMetadata}
                                      data={this.state.currentSuccessDataSet}
                                      theme={this.props.muiTheme.name}
                                />
                            </div>
                            <div style={{height: this.state.height * 0.1}}>
                                <V0MuiThemeProvider>
                                    <Pagination
                                        total={this.state.successPageCount}
                                        current={this.state.currentSuccessPageNumber}
                                        display={noOfPagesInPaginationNavigation}
                                        onChange={number => this.updateTable(this.state.successData, number, true)}
                                        styleButton={{paginationButtonStyle}}
                                    />
                                </V0MuiThemeProvider>
                            </div>
                        </div>
                    </div>
                </MuiThemeProvider>
            );
        }
        else {
            if (this.state.doFirstLoginTabNeeded) {
                return (
                    <MuiThemeProvider theme={theme}>
                        <div style={{height: this.state.height}}>
                            <div style={{height: this.state.height * 0.1}}>
                                <h3>{this.state.header}</h3>
                            </div>
                            <div style={{height: this.state.height * 0.1}}>
                                <Tabs
                                    onChange={this.processFirstLogins}
                                    value={false}
                                    indicatorColor="primary"
                                    textColor="primary"
                                >
                                    <Tab label="By All"/>
                                    <Tab label="By First Logins"/>
                                </Tabs>
                            </div>
                            <div style={{height: this.state.height * 0.4}}>
                                <div style={{height: this.state.height * 0.35}}>
                                    <VizG config={this.state.chartConfigSuccess}
                                          metadata={this.state.successMetadata}
                                          data={this.state.currentSuccessDataSet}
                                          theme={this.props.muiTheme.name}
                                    />
                                </div>
                                <div style={{height: this.state.height * 0.05}}>
                                    <V0MuiThemeProvider>
                                        <Pagination
                                            total={this.state.successPageCount}
                                            current={this.state.currentSuccessPageNumber}
                                            display={noOfPagesInPaginationNavigation}
                                            onChange={number => this.updateTable(this.state.successData, number, true)}
                                            styleButton={{paginationButtonStyle}}
                                        />
                                    </V0MuiThemeProvider>
                                </div>
                            </div>
                            <div style={{height: this.state.height * 0.4}}>
                                <div style={{height: this.state.height * 0.35}}>
                                    <VizG config={this.state.chartConfigFailure}
                                          metadata={this.state.failureMetadata}
                                          data={this.state.currentFailureDataSet}
                                          theme={this.props.muiTheme.name}
                                    />
                                </div>
                                <div style={{height: this.state.height * 0.05}}>
                                    <V0MuiThemeProvider>
                                        <Pagination
                                            total={this.state.failurePageCount}
                                            current={this.state.currentFailurePageNumber}
                                            display={noOfPagesInPaginationNavigation}
                                            onChange={number => this.updateTable(this.state.failureData, number, false)}
                                            styleButton={{paginationButtonStyle}}
                                        />
                                    </V0MuiThemeProvider>
                                </div>
                            </div>
                        </div>
                    </MuiThemeProvider>
                );
            }
            return (
                <MuiThemeProvider theme={theme}>
                    <div style={{height: this.state.height}}>
                        <div style={{height: this.state.height * 0.1}}>
                            <h3>{this.state.header}</h3>
                        </div>
                        <div style={{height: this.state.height * 0.1}}/>
                        <div style={{height: this.state.height * 0.4}}>
                            <div style={{height: this.state.height * 0.35}}>
                                <VizG config={this.state.chartConfigSuccess}
                                      metadata={this.state.successMetadata}
                                      data={this.state.currentSuccessDataSet}
                                      theme={this.props.muiTheme.name}
                                />
                            </div>
                            <div style={{height: this.state.height * 0.05}}>
                                <V0MuiThemeProvider>
                                    <Pagination
                                        total={this.state.successPageCount}
                                        current={this.state.currentSuccessPageNumber}
                                        display={noOfPagesInPaginationNavigation}
                                        onChange={number => this.updateTable(this.state.successData, number, true)}
                                        styleButton={{paginationButtonStyle}}
                                    />
                                </V0MuiThemeProvider>
                            </div>
                        </div>
                        <div style={{height: this.state.height * 0.4}}>
                            <div style={{height: this.state.height * 0.35}}>
                                <VizG config={this.state.chartConfigFailure}
                                      metadata={this.state.failureMetadata}
                                      data={this.state.currentFailureDataSet}
                                      theme={this.props.muiTheme.name}
                                />
                            </div>
                            <div style={{height: this.state.height * 0.05}}>
                                <V0MuiThemeProvider>
                                    <Pagination
                                        total={this.state.failurePageCount}
                                        current={this.state.currentFailurePageNumber}
                                        display={noOfPagesInPaginationNavigation}
                                        onChange={number => this.updateTable(this.state.failureData, number, false)}
                                        styleButton={{paginationButtonStyle}}
                                    />
                                </V0MuiThemeProvider>
                            </div>
                        </div>
                    </div>
                </MuiThemeProvider>
            );
        }
    }
}

global.dashboard.registerWidget('IsAnalyticsHorizontalBarChart', IsAnalyticsHorizontalBarChart);