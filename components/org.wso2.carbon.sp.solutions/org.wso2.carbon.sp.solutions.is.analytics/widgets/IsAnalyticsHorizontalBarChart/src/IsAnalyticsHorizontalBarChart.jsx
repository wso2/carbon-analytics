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

import Widget from "@wso2-dashboards/widget";
import VizG from 'react-vizgrammar';
import {MuiThemeProvider} from '@material-ui/core/styles';
import {MuiThemeProvider as V0MuiThemeProvider} from 'material-ui';
import _ from 'lodash';
import Pagination from 'material-ui-pagination';

let widgetPseudoId = "BarChardWidget_1_1_1";

const colorGreen = "#6ED460";
const colorRed = "#EC5D40";
const messageHeading = "barChartFilter";
const dataPerPage = 10;
const noOfPagesInPaginationNavigation = 5;

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

        this.handleReceivedSuccessData = this.handleReceivedSuccessData.bind(this);
        this.handleReceivedFailureData = this.handleReceivedFailureData.bind(this);
        this.assembleQuery = this.assembleQuery.bind(this);
        this.onReceivingMessage = this.onReceivingMessage.bind(this);
        this.updateTable = this.updateTable.bind(this);

        this.props.glContainer.on('resize', () =>
            this.setState({
                width: this.props.glContainer.width,
                height: this.props.glContainer.height,
            })
        );
    }

    componentDidMount() {
        super.subscribe(this.onReceivingMessage);
        let successMetadataClone = _.cloneDeep(successMetadata);
        let failureMetadataClone = _.cloneDeep(failureMetadata);
        let chartConfigSuccessClone = _.cloneDeep(chartConfigSuccess);
        let chartConfigFailureClone = _.cloneDeep(chartConfigFailure);

        let widgetPseudoId;

        let xAxisLabel = "";
        let xAxisValue = "";
        let header = "By ";

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
                });
            })
            .catch(() => {
                this.setState({
                    faultyProviderConf: true
                });
            });
    }

    handleReceivedSuccessData(message) {
        this.updateTable(message.data, this.state.currentSuccessPageNumber, true);
    }

    handleReceivedFailureData(message) {
        this.updateTable(message.data, this.state.currentFailurePageNumber, false);
    }

    /*
     * Data is also passed into update table function to reduce the number of this.setState() calls.
     * Otherwise the resulting chart will get more cycles to update, which will left user in ambiguity.
     */
    updateTable(data, pageNumber, isSuccess) {
        let internalPageNumber = pageNumber - 1; // Internally pages are counted from 0.

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

    onReceivingMessage(message) {
        if (message.header === "additionalFilterConditions") {
            if (message.body === "") {
                this.setState({
                    additionalFilterConditions: undefined,
                    successData: [],
                    failureData: [],
                    currentSuccessDataSet: [],
                    currentFailureDataSet: [],
                }, this.assembleQuery)
            } else {
                this.setState({
                    additionalFilterConditions: message.body,
                    successData: [],
                    failureData: [],
                    currentSuccessDataSet: [],
                    currentFailureDataSet: [],
                }, this.assembleQuery);
            }
        }
        else {
            this.setState({
                per: message.granularity,
                fromDate: message.from,
                toDate: message.to,
                successData: [],
                failureData: [],
                currentSuccessDataSet: [],
                currentFailureDataSet: [],
            }, () => {
                this.assembleQuery()
            });
        }
    }

    assembleQuery() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
        let dataProviderConfigsSuccess = _.cloneDeep(this.state.dataProviderConf);
        let query = dataProviderConfigsSuccess.configs.config.queryData.query;
        let countType = "authStepSuccessCount";
        let filterCondition = " on ";
        let idpFilter = " identityProviderType=='{{idpType}}' ";
        let additionalFilters = "";
        let doIdpFilter = false;
        let xAxisValue = "";
        let aggregationName = "AuthenticationStatAggregation";
        let doAdditionalFilter = false;

        if (this.state.additionalFilterConditions !== undefined) {
            let additionalFilterConditionsClone = _.cloneDeep(this.state.additionalFilterConditions);

            for (var key in additionalFilterConditionsClone) {
                if (additionalFilterConditionsClone[key] !== "") {
                    if (key === "role") {
                        console.log("Role Found: ", key, "\nValue: ", additionalFilterConditionsClone[key]);
                    } else if (key === "isFirstLogin") {
                        additionalFilters = additionalFilters +
                            " and " + key + "==" + additionalFilterConditionsClone[key] + " ";
                    } else {
                        additionalFilters = additionalFilters +
                            " and " + key + "==\'" + additionalFilterConditionsClone[key] + "\' ";
                    }
                }
            }
            doAdditionalFilter = true;
        }

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

        if (this.state.options.widgetType === "Local") {
            countType = "authSuccessCount";
        } else {
            countType = "authStepSuccessCount";
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

        if (doAdditionalFilter) {
            filterCondition = filterCondition + additionalFilters;
        }

        if (doIdpFilter && doAdditionalFilter) {
            filterCondition = filterCondition + additionalFilters;
            query = query.replace("{{filterCondition}}", filterCondition);
        } else if (doIdpFilter) {
            query = query.replace("{{filterCondition}}", filterCondition);
        } else if (doAdditionalFilter) {
            filterCondition = additionalFilters.replace(" and ", " on ");
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
        dataProviderConfigsSuccess.configs.config.queryData.query = querySuccess;

        super.getWidgetChannelManager().subscribeWidget(this.props.id,
            this.handleReceivedSuccessData, dataProviderConfigsSuccess);

        super.getWidgetChannelManager().unsubscribeWidget(widgetPseudoId);

        let dataProviderConfigsFailure = _.cloneDeep(this.state.dataProviderConf);
        let queryFailure = query
            .replace()
            .replace(/{{yAxisValue}}/g, "authFailureCount");
        dataProviderConfigsFailure.configs.config.queryData.query = queryFailure;

        super.getWidgetChannelManager().subscribeWidget(this.state.widgetPseudoId,
            this.handleReceivedFailureData, dataProviderConfigsFailure);
    }

    onChartClick(data) {
        let message = {
            header: messageHeading,
            title: this.state.chartConfigFailure.x,
            value: data[this.state.chartConfigFailure.x],
        };
        super.publish(message);
    }

    render() {
        let width = this.state.width;
        let height = this.state.height;

        if (this.state.faultyProviderConf) {
            return (
                <MuiThemeProvider theme={this.props.muiTheme}>
                    <div style={{height: this.state.height, width: this.state.width}}>
                        <h3>{this.state.header}</h3>
                        Unable to fetch data, please check the data provider configurations.
                    </div>
                </MuiThemeProvider>
            );
        }
        else if (this.state.currentSuccessDataSet.length === 0 && this.state.currentFailureDataSet.length > 0) {
            return (
                <MuiThemeProvider theme={this.props.muiTheme}>
                    <div
                        style={{
                            height: height,
                            width: width,
                            paddingLeft: width * 0.05,
                            paddingRight: width * 0.05,
                            paddingTop: height * 0.05,
                            paddingBottom: height * 0.05,
                        }}>
                        <div style={{height: height * 0.1, width: width * 0.9}}>
                            <h3>{this.state.header}</h3>
                        </div>
                        <div style={{height: height * 0.8, width: width * 0.9}}>
                            <div style={{height: height * 0.7, width: width * 0.9}}>
                                <VizG config={this.state.chartConfigFailure}
                                      metadata={this.state.failureMetadata}
                                      data={this.state.currentFailureDataSet}
                                      theme={this.props.muiTheme.name}
                                      onClick={(data) => this.onChartClick(data)}
                                />
                            </div>
                            <div style={{
                                height: height * 0.1,
                                width: width * 0.9,
                                justifyContent: 'center',
                                alignItems: 'center'
                            }}
                            >
                                <V0MuiThemeProvider>
                                    {
                                        this.state.failureData.length > dataPerPage &&
                                        <Pagination
                                            total={this.state.failurePageCount}
                                            current={this.state.currentFailurePageNumber}
                                            display={noOfPagesInPaginationNavigation}
                                            onChange={number =>
                                                this.updateTable(this.state.failureData, number, false)}
                                        />
                                    }
                                </V0MuiThemeProvider>
                            </div>
                        </div>
                    </div>
                </MuiThemeProvider>
            );
        }
        else if (this.state.currentFailureDataSet.length === 0 && this.state.currentSuccessDataSet.length > 0) {
            return (
                <MuiThemeProvider theme={this.props.muiTheme}>
                    <div
                        style={{
                            height: height,
                            width: width,
                            paddingLeft: width * 0.05,
                            paddingRight: width * 0.05,
                            paddingTop: height * 0.05,
                            paddingBottom: height * 0.05,
                        }}>
                        <div style={{height: height * 0.1, width: width * 0.9}}>
                            <h3>{this.state.header}</h3>
                        </div>
                        <div style={{height: height * 0.8, width: width * 0.9}}>
                            <div style={{height: height * 0.7, width: width * 0.9}}>
                                <VizG config={this.state.chartConfigSuccess}
                                      metadata={this.state.successMetadata}
                                      data={this.state.currentSuccessDataSet}
                                      theme={this.props.muiTheme.name}
                                      onClick={(data) => this.onChartClick(data)}
                                />
                            </div>
                            <div style={{height: height * 0.1, width: width * 0.9}}>
                                <V0MuiThemeProvider>
                                    {
                                        this.state.successData.length > dataPerPage &&
                                        <Pagination
                                            total={this.state.successPageCount}
                                            current={this.state.currentSuccessPageNumber}
                                            display={noOfPagesInPaginationNavigation}
                                            onChange={number =>
                                                this.updateTable(this.state.successData, number, true)}
                                        />
                                    }
                                </V0MuiThemeProvider>
                            </div>
                        </div>
                    </div>
                </MuiThemeProvider>
            );
        }
        else {
            return (
                <MuiThemeProvider theme={this.props.muiTheme}>
                    <div
                        style={{
                            height: height,
                            width: width,
                            paddingLeft: width * 0.05,
                            paddingRight: width * 0.05,
                            paddingTop: height * 0.05,
                            paddingBottom: height * 0.05,
                        }}>
                        <div style={{height: height * 0.1, width: width * 0.9}}>
                            <h3>{this.state.header}</h3>
                        </div>
                        <div style={{height: height * 0.4, width: width * 0.9}}>
                            <div style={{height: height * 0.3, width: width * 0.9}}>
                                <VizG config={this.state.chartConfigSuccess}
                                      metadata={this.state.successMetadata}
                                      data={this.state.currentSuccessDataSet}
                                      theme={this.props.muiTheme.name}
                                      onClick={(data) => this.onChartClick(data)}
                                />
                            </div>
                            <div style={{
                                height: height * 0.1,
                                width: width * 0.9,
                                justifyContent: 'center',
                                alignItems: 'center'
                            }}
                            >
                                <V0MuiThemeProvider>
                                    {
                                        this.state.successData.length > dataPerPage &&
                                        <Pagination
                                            total={this.state.successPageCount}
                                            current={this.state.currentSuccessPageNumber}
                                            display={noOfPagesInPaginationNavigation}
                                            onChange={number =>
                                                this.updateTable(this.state.successData, number, true)}
                                        />
                                    }
                                </V0MuiThemeProvider>
                            </div>
                        </div>
                        <div style={{height: height * 0.4, width: width * 0.9}}>
                            <div style={{height: height * 0.3, width: width * 0.9}}>
                                <VizG config={this.state.chartConfigFailure}
                                      metadata={this.state.failureMetadata}
                                      data={this.state.currentFailureDataSet}
                                      theme={this.props.muiTheme.name}
                                      onClick={(data) => this.onChartClick(data)}
                                />
                            </div>
                            <div style={{height: height * 0.1, width: width * 0.9}}>
                                <V0MuiThemeProvider>
                                    {
                                        this.state.failureData.length > dataPerPage &&
                                        <Pagination
                                            total={this.state.failurePageCount}
                                            current={this.state.currentFailurePageNumber}
                                            display={noOfPagesInPaginationNavigation}
                                            onChange={number =>
                                                this.updateTable(this.state.failureData, number, false)}
                                        />
                                    }
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