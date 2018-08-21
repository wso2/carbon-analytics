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

import Widget from '@wso2-dashboards/widget';
import VizG from 'react-vizgrammar';
import {MuiThemeProvider} from '@material-ui/core/styles';
import _ from 'lodash';

const colorGreen = '#6ED460';
const colorRed = '#EC5D40';

const metadata = {
    names: ['timestamp', 'SuccessCount', 'FailureCount'],
    types: ['time', 'linear', 'linear'],
};

const chartConfig = {
    x: 'timestamp',
    charts: [
        {
            type: 'area',
            y: 'SuccessCount',
            fill: colorGreen,
        },
        {
            type: 'area',
            y: 'FailureCount',
            fill: colorRed,
        },
    ],
    yAxisLabel: 'Authentication Attempts',
    xAxisLabel: 'Time',
    tipTimeFormat: '%d/%m/%Y %I:%M:%S',
    maxLength: 6,
    legend: false,
    append: false,
};

class IsAnalyticsAttemptsOverTime extends Widget {
    constructor(props) {
        super(props);

        this.state = {
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,

            chartConfig,
            data: [],
            metadata,
            isProviderConfigFault: false,
            options: this.props.configs.options,
        };

        this.handleReceivedData = this.handleReceivedData.bind(this);
        this.onReceivingMessage = this.onReceivingMessage.bind(this);
        this.assembleQuery = this.assembleQuery.bind(this);

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
                    isProviderConfigFault: true,
                });
            });
    }

    componentWillUnmount() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
    }

    onReceivingMessage(message) {
        if (message.header === 'additionalFilterConditions') {
            if (message.body === '') {
                this.setState({
                    additionalFilterConditions: undefined,
                    data: [],
                }, this.assembleQuery);
            } else {
                this.setState({
                    additionalFilterConditions: message.body,
                    data: [],
                }, this.assembleQuery);
            }
        } else {
            this.setState({
                per: message.granularity,
                fromDate: message.from,
                toDate: message.to,
                data: [],
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
                        filterCondition = filterCondition
                            + " and str:regexp(rolesCommaSeparated, \"" + additionalFilterConditionsClone[key] + ',")';
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
        } else if (this.state.options.widgetType === "Federated") {
            query = dataProviderConfigs.configs.config.queryData.queryFederated;
        } else {
            // Overall attempts - No idpType filter
            filterCondition = filterCondition.replace(" and ", " on ");
        }

        query = query
            .replace("{{per}}", this.state.per)
            .replace("{{from}}", this.state.fromDate)
            .replace("{{to}}", this.state.toDate);

        if (doAdditionalFilter) {
            query = query.replace('{{filterCondition}}', filterCondition);
        } else {
            query = query.replace('{{filterCondition}}', '');
        }

        dataProviderConfigs.configs.config.queryData.query = query;
        super.getWidgetChannelManager().subscribeWidget(this.props.id, this.handleReceivedData, dataProviderConfigs);
    }

    handleReceivedData(message) {
        this.setState({
            data: message.data,
        });
    }

    render() {
        if (this.state.isProviderConfigFault) {
            return (
                <MuiThemeProvider theme={this.props.muiTheme}>
                    <div style={{height: this.state.height}}>
                        <h3> Login Attempts Over Time </h3>
                        <h5>No data found</h5>
                    </div>
                </MuiThemeProvider>
            );
        } 
        return (
            <MuiThemeProvider theme={this.props.muiTheme}>
                <div style={{height: this.state.height}}>
                    <div style={{height: this.state.height * 0.1}}>
                        <h3> Login Attempts Over Time </h3>
                    </div>
                    <div style={{height: this.state.height * 0.9}}>
                        <VizG
                            config={this.state.chartConfig}
                            metadata={this.state.metadata}
                            data={this.state.data}
                        />
                    </div>
                </div>
            </MuiThemeProvider>
        );
    }
}

global.dashboard.registerWidget('IsAnalyticsAttemptsOverTime', IsAnalyticsAttemptsOverTime);
