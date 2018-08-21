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

import React, { Component } from 'react';
import Widget from '@wso2-dashboards/widget';
import VizG from 'react-vizgrammar';
import { MuiThemeProvider } from '@material-ui/core/styles';
import _ from 'lodash';

const colorGreen = '#6ED460';
const colorRed = '#EC5D40';

const successStyle = {
    color: colorGreen,
    margin: "0px auto",
};

const failureStyle = {
    color: colorRed,
    margin: "0px auto",
};

const pieChartMetadata = {
    names: ['attemptType', 'attemptCount'],
    types: ['ordinal', 'linear'],
};

const numChartMetadata = {
    names: [
        'totalLoginAttempts'
    ],
    types: [
        'linear'
    ],
};

const numChartData = [
    [0],
    [0],
];

const pieChartConfig = {
    charts: [
        {
            type: 'arc',
            x: 'attemptCount',
            color: 'attemptType',
            mode: 'donut',
            colorScale: [colorRed, colorGreen],
        },
    ],
};

const numChartConfig = {
    x: 'totalLoginAttempts',
    title: 'Total Login Attempts',
    charts: [
        {
            type: 'number'
        },
    ],
    showDifference: false,
    showPercentage: false,
    showDecimal: false,

};

class IsAnalyticsCompactSummary extends Widget {
    constructor(props) {
        super(props);

        this.state = {
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,

            pieChartConfig,
            pieChartData: [],
            pieChartMetadata,
            numChartConfig,
            numChartData,
            numChartMetadata,
            faultyProviderConf: false,
            options: this.props.configs.options,
            totalAttempts: 0,
            successPercentage: 0,
            failurePercentage: 0,
        };

        this.handleDataReceived = this.handleDataReceived.bind(this);
        this.assembleQuery = this.assembleQuery.bind(this);
        this.onReceivingMessage = this.onReceivingMessage.bind(this);

        this.props.glContainer.on('resize', () => this.setState({
                width: this.props.glContainer.width,
                height: this.props.glContainer.height,
            }),
        );
    }

    componentDidMount() {
        super.subscribe(this.onReceivingMessage);
        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {
                this.setState({
                    dataProviderConf: message.data.configs.providerConfig,
                });
            })
            .catch(() => {
                this.setState({
                    faultyProviderConf: true,
                });
            });
    }

    handleDataReceived(message) {
        const totalAttempts = message.data[0][0] + message.data[0][1];

        const successPercentage = parseFloat(parseInt(message.data[0][1]) * 100 / totalAttempts)
            .toFixed(2);
        const failurePercentage = parseFloat(parseInt(message.data[0][0]) * 100 / totalAttempts)
            .toFixed(2);

        this.setState({
            pieChartMetadata,
            numChartMetadata,
            pieChartData: [
                [
                    'Failure',
                    message.data[0][0],
                ],
                [
                    'Success',
                    message.data[0][1],
                ],
            ],
            numChartData: [
                [
                    message.data[0][1],
                ],
                [
                    message.data[0][0] + message.data[0][1],
                ],
            ],
            totalAttempts,
            successPercentage,
            failurePercentage,
        });
    }

    onReceivingMessage(message) {
        if (message.header === 'additionalFilterConditions') {
            if (message.body === '') {
                this.setState({
                    additionalFilterConditions: undefined,
                    pieChartData: [],
                    numChartData,
                    successPercentage: 0,
                    failurePercentage: 0,
                    totalAttempts: 0,
                }, this.assembleQuery);
            } else {
                this.setState({
                    additionalFilterConditions: message.body,
                    pieChartData: [],
                    numChartData,
                    successPercentage: 0,
                    failurePercentage: 0,
                    totalAttempts: 0,
                }, this.assembleQuery);
            }
        } else {
            this.setState({
                per: message.granularity,
                fromDate: message.from,
                toDate: message.to,
                pieChartData: [],
                numChartData,
                successPercentage: 0,
                failurePercentage: 0,
                totalAttempts: 0,
            }, this.assembleQuery);
        }
    }

    assembleQuery() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
        const dataProviderConfigs = _.cloneDeep(this.state.dataProviderConf);
        let query = dataProviderConfigs.configs.config.queryData.query;
        let filterCondition = ' ';
        let doAdditionalFilter = false;

        if (this.state.additionalFilterConditions !== undefined) {
            const additionalFilterConditionsClone = _.cloneDeep(this.state.additionalFilterConditions);
            for (let key in additionalFilterConditionsClone) {
                if (additionalFilterConditionsClone[key] !== '') {
                    if (key === 'role') {
                        console.log('Role Found: ', key, '\nValue: ', additionalFilterConditionsClone[key]);
                    } else if (key === 'isFirstLogin') {
                        filterCondition = filterCondition
                            + " and " + key + '==' + additionalFilterConditionsClone[key] + ' ';
                    } else {
                        filterCondition = filterCondition
                            + " and " + key + "==\'" + additionalFilterConditionsClone[key] + "\' ";
                    }
                }
            }
            doAdditionalFilter = true;
        }

        if (this.state.options.widgetType === 'Local') {
            query = dataProviderConfigs.configs.config.queryData.queryLocal;
        } else if (this.state.options.widgetType === 'Federated') {
            query = dataProviderConfigs.configs.config.queryData.queryFederated;
        } else {
            filterCondition = filterCondition.replace(' and ', ' on ');
        }

        let updatedQuery = query
            .replace('{{per}}', this.state.per)
            .replace('{{from}}', this.state.fromDate)
            .replace('{{to}}', this.state.toDate);

        if (doAdditionalFilter) {
            updatedQuery = updatedQuery.replace('{{filterCondition}}', filterCondition);
        } else {
            updatedQuery = updatedQuery.replace('{{filterCondition}}', '');
        }

        dataProviderConfigs.configs.config.queryData.query = updatedQuery;
        super.getWidgetChannelManager().subscribeWidget(this.props.id, this.handleDataReceived, dataProviderConfigs);
    }

    render() {
        const height = this.state.height;
        const width = this.state.width;

        if (this.state.faultyProviderConf) {
            return (
                <MuiThemeProvider theme={this.props.muiTheme}>
                    <div style={{ padding: 24 }}>
                        <h5>Data Provider Connection Error - Please check the provider configs</h5>
                    </div>
                </MuiThemeProvider>
            );
        }
        return (
            <MuiThemeProvider theme={this.props.muiTheme}>
                <div style={{ height: height, width}}>
                    <div style={{ height: height * 0.45 }}>
                        <VizG
                            config={numChartConfig}
                            metadata={this.state.numChartMetadata}
                            data={this.state.numChartData}
                            theme={this.props.muiTheme.name}
                        />
                    </div>
                    {
                        (this.state.totalAttempts !== 0)
                        && <div style={{ padding: 24, height: height * 0.55 }}>
                            <div style={{ height: height * 0.05, width: width * 0.9, 'text-align':'center'}}>
                                <h5 style={successStyle}>
                                    Success: {this.state.successPercentage}
                                </h5>
                                <h5 style={failureStyle}>
                                    Failure: {this.state.failurePercentage}
                                </h5>
                            </div>
                            <div style={{ height: height * 0.5, width: width * 0.9 }}>
                                <VizG
                                    config={this.state.pieChartConfig}
                                    metadata={this.state.pieChartMetadata}
                                    data={this.state.pieChartData}
                                    theme={this.props.muiTheme.name}
                                />
                            </div>
                        </div>
                    }
                </div>
            </MuiThemeProvider>
        );
    }
}

global.dashboard.registerWidget('IsAnalyticsCompactSummary', IsAnalyticsCompactSummary);
