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
import {MuiThemeProvider, createMuiTheme} from '@material-ui/core';
import Switch from '@material-ui/core/Switch';
import FormControlLabel from "@material-ui/core/FormControlLabel";
import _ from 'lodash';

const colorWhite = "#FFFFFF";
const colorGreen = "#6ED460";
const colorRed = "#EC5D40";

let darkTheme = createMuiTheme({
    palette: {
        type: 'dark',
    }
});

let lightTheme = createMuiTheme({
    palette: {
        type: 'light',
    }
});

let colorScaleSuccess = [
    colorWhite,
    colorGreen,
];

let colorScaleFailure = [
    colorWhite,
    colorRed,
];

let metadata = {
    names: ['region', 'Count'],
    types: ['ordinal', 'linear'],
};

let chartConfig = {
    type: "map",
    x: "region",
    charts: [
        {
            "type": "map",
            "y": "Count",
            "mapType": "world",
            "colorScale": colorScaleSuccess,
        }
    ],
    chloropethRangeLowerBound: 0,
};

class IsAnalyticsAreaChart extends Widget {
    constructor(props) {
        super(props);

        this.state = {
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,

            chartConfig: chartConfig,
            data: [],
            metadata: metadata,
            faultyProviderConf: false,
            options: this.props.configs.options,
            isFailureMap: false,
            switchLabel: "Success",
        };

        this.handleReceivedData = this.handleReceivedData.bind(this);
        this.onReceivingMessage = this.onReceivingMessage.bind(this);
        this.assembleQuery = this.assembleQuery.bind(this);

        this.props.glContainer.on('resize', () =>
            this.setState({
                width: this.props.glContainer.width,
                height: this.props.glContainer.height
            })
        );
    }

    componentDidMount() {
        super.subscribe(this.onReceivingMessage);
        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {

                this.setState({
                    dataProviderConf: message.data.configs.providerConfig
                });
            })
            .catch((error) => {
                console.error("[ERROR]: ", error);
                this.setState({
                    faultyProviderConf: true
                });
            });
    }

    componentWillUnmount() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
    }

    onReceivingMessage(message) {
        if (message.header === "additionalFilterConditions") {
            if (message.body === "") {
                this.setState({
                    additionalFilterConditions: undefined,
                    data: [],
                }, () => this.assembleQuery(null))
            } else {
                this.setState({
                    additionalFilterConditions: message.body,
                    data: [],
                }, () => this.assembleQuery(null));
            }
        }
        else {
            this.setState({
                per: message.granularity,
                fromDate: message.from,
                toDate: message.to,
                data: [],
            }, () => this.assembleQuery(null));
        }
    }

    assembleQuery(event) {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);

        let dataProviderConfig = _.cloneDeep(this.state.dataProviderConf);
        let query = dataProviderConfig.configs.config.queryData.query;
        let filterCondition = " on identityProviderType=='{{idpType}}' ";
        let additionalFilters = "";
        let countType = "";
        let doFilter = false;
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

        if (event !== null) {
            if (event.target.checked) {
                countType = "authFailureCount";
            } else if (this.state.options.widgetType === "Local") {
                countType = "authSuccessCount";
            } else {
                countType = "authStepSuccessCount";
            }
        } else if (this.state.isFailureMap) {
            countType = "authFailureCount";
        }
        else if (this.state.options.widgetType === "Local") {
            countType = "authSuccessCount";
        } else {
            countType = "authStepSuccessCount";
        }

        if (this.state.options.widgetType === "Local") {
            filterCondition = filterCondition.replace("{{idpType}}", "LOCAL");
            doFilter = true;
        } else if (this.state.options.widgetType === "Federated") {
            filterCondition = filterCondition.replace("{{idpType}}", "LOCAL");
            doFilter = true;
        }

        let updatedQuery = query
            .replace("{{per}}", this.state.per)
            .replace("{{from}}", this.state.fromDate)
            .replace("{{to}}", this.state.toDate)
            .replace(/{{countType}}/g, countType);

        if (doFilter && doAdditionalFilter) {
            filterCondition = filterCondition + additionalFilters;
            updatedQuery = updatedQuery.replace("{{filterCondition}}", filterCondition);
        } else if (doFilter) {
            updatedQuery = updatedQuery.replace("{{filterCondition}}", filterCondition);
        } else if (doAdditionalFilter) {
            filterCondition = additionalFilters.replace(" and ", " on ");
            updatedQuery = updatedQuery.replace("{{filterCondition}}", filterCondition);
        } else {
            updatedQuery = updatedQuery.replace("{{filterCondition}}", "");
        }

        dataProviderConfig.configs.config.queryData.query = updatedQuery;
        super.getWidgetChannelManager().subscribeWidget(this.props.id, this.handleReceivedData, dataProviderConfig);

        if (event !== null) {
            let chartConfigClone = _.cloneDeep(chartConfig);
            let doChangeToFailureMap = event.target.checked;
            let switchLabelNew = "";

            if (doChangeToFailureMap) {
                chartConfigClone.charts[0].colorScale = colorScaleFailure;
                switchLabelNew = "Failure";
            } else {
                chartConfigClone.charts[0].colorScale = colorScaleSuccess;
                switchLabelNew = "Success";
            }

            this.setState({
                    chartConfig: chartConfigClone,
                    isFailureMap: doChangeToFailureMap,
                    switchLabel: switchLabelNew,
                    data: [],
                }, super.getWidgetChannelManager()
                    .subscribeWidget(this.props.id, this.handleReceivedData, dataProviderConfig)
            );
        } else {
            super.getWidgetChannelManager()
                .subscribeWidget(this.props.id, this.handleReceivedData, dataProviderConfig);
        }
    }

    handleReceivedData(message) {
        this.setState({
            data: message.data,
        });
    }

    render() {
        let width = this.state.width;
        let height = this.state.height;
        let theme = darkTheme;

        if (this.props.muiTheme.appBar.color === "#eeeeee") {
            theme = lightTheme;
        }

        return (
            <MuiThemeProvider theme={theme}>
                <div
                    style={{
                        paddingLeft: width * 0.05,
                        paddingRight: width * 0.05,
                        paddingTop: height * 0.05,
                        paddingBottom: height * 0.05,
                        height: height,
                        width: width,
                    }}
                >
                    <div style={{height: height * 0.1, width: width * 0.9}}>
                        <h3> Area Chart </h3>
                    </div>
                    <div style={{height: height * 0.6, width: width * 0.9}}>
                        <VizG
                            config={this.state.chartConfig}
                            metadata={this.state.metadata}
                            data={this.state.data}
                        />
                    </div>
                    <div style={{height: height * 0.2, width: width * 0.9}}>
                        <FormControlLabel
                            control={
                                <Switch
                                    checked={this.state.isFailureMap}
                                    onChange={this.assembleQuery}
                                />
                            }
                            label={this.state.switchLabel}
                        />
                    </div>
                </div>
            </MuiThemeProvider>
        );
    }
}

global.dashboard.registerWidget('IsAnalyticsAreaChart', IsAnalyticsAreaChart);