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
import {MuiThemeProvider, createMuiTheme} from '@material-ui/core/styles';
import _ from 'lodash';
import Button from '@material-ui/core/Button';

const widgetTexts = {
    Overall: {
        bodyText: 'Analyze overall login attempts made via WSO2 Identity Server. This includes information about overall flows of authentication took place through Identity Server. A collection of authentication steps is considered as an overall attempt',
        heading: 'Overall Login Attempts',
        seeMoreLink: window.location.href + '/../overall',
    },
    Local: {
        bodyText: 'Analyze local login attempts made via WSO2 Identity Server. Local login attempts include all login attempts which are done through resident IDP. These statistics will give an idea on the involvement of resident IDP in an authentication flow.',
        heading: 'Local Login Attempts',
        seeMoreLink: window.location.href + '/../local',
    },
    Federated: {
        bodyText: 'Analyze federated login attempts made via WSO2 Identity Server. This will give an idea about the authentication steps took place via federated identity providers.',
        heading: 'Federated Login Attempts',
        seeMoreLink: window.location.href + '/../federated',
    },
};

const colorGreen = '#6ED460';
const colorRed = '#EC5D40';

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
    names: ['totalLoginAttempts'],
    types: ['linear'],
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
            'type': 'number'
        },
    ],
    showDifference: false,
    showPercentage: false,
    showDecimal: false,
};

class IsAnalyticsSummary extends Widget {
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
            isProviderConfigFault: false,
            options: this.props.configs.options,
            widgetTexts: widgetTexts[this.props.configs.options.widgetType],
            totalAttempts: 0,
        };

        this.handleReceivedData = this.handleReceivedData.bind(this);
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
                    isProviderConfigFault: true,
                });
            });
    }

    componentWillUnmount() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
    }

    handleReceivedData(message) {
        const totalAttempts = parseInt(message.data[0][0]) + parseInt(message.data[0][1]);
        const successPercentage = parseFloat(parseInt(message.data[0][1]) * 100 / totalAttempts)
            .toFixed(2);
        const failurePercentage = parseFloat(parseInt(message.data[0][1]) * 100 / totalAttempts)
            .toFixed(2);

        this.setState({
            successPercentage,
            failurePercentage,
            totalAttempts,
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

        });
    }

    onReceivingMessage(message) {
        this.setState({
            per: message.granularity,
            fromDate: message.from,
            toDate: message.to,
            pieChartData: [],
            numChartData,
            totalAttempts: 0,
            successPercentage: 0,
            failurePercentage: 0,
        }, this.assembleQuery);
    }

    assembleQuery() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
        const dataProviderConfigs = _.cloneDeep(this.state.dataProviderConf);
        let query = dataProviderConfigs.configs.config.queryData.query;

        if (this.state.options.widgetType === 'Local') {
            query = dataProviderConfigs.configs.config.queryData.queryLocal;
        } else if (this.state.options.widgetType === 'Federated') {
            query = dataProviderConfigs.configs.config.queryData.queryFederated;
        }
        query = query
            .replace('{{per}}', this.state.per)
            .replace('{{from}}', this.state.fromDate)
            .replace('{{to}}', this.state.toDate);

        dataProviderConfigs.configs.config.queryData.query = query;
        super.getWidgetChannelManager().subscribeWidget(this.props.id, this.handleReceivedData, dataProviderConfigs);
    }

    render() {
        const height = this.state.height;
        const width = this.state.width;
        const padding = this.state.width * 0.05;
        let theme = darkTheme;

        if (this.props.muiTheme.appBar.color === "#eeeeee") {
            theme = lightTheme;
        }

        if (this.state.isProviderConfigFault) {
            return (
                <MuiThemeProvider theme={theme}>
                    <div style={{padding: padding, height, width}}>
                        <h3>{this.state.widgetTexts.heading}</h3>
                        <h5>[ERROR]: Cannot connect to the data provider</h5>
                    </div>
                </MuiThemeProvider>
            );
        }
        return (
            <MuiThemeProvider theme={theme}>
                <div style={{paddingLeft: padding, paddingRight: padding, height, width}}>

                    <div style={{height: this.state.height * 0.05}}>
                        <h2>{this.state.widgetTexts.heading}</h2>
                    </div>
                    <div style={{height: height * 0.10, width: width * 0.9}}>
                        <p>{this.state.widgetTexts.bodyText}</p>
                    </div>
                    <div style={{height: height * 0.25, width: width * 0.9}}>
                        <VizG
                            config={numChartConfig}
                            metadata={this.state.numChartMetadata}
                            data={this.state.numChartData}
                            theme={this.props.muiTheme.name}
                        />
                    </div>
                    <div style={{height: height * 0.25, width: width * 0.9}}>
                        {
                            this.state.totalAttempts > 0
                            &&
                            <div>
                                <div style={{height: height * 0.05, width: width * 0.9, 'text-align': 'center'}}>
                                    <h5 style={successStyle}>
                                        Success:{this.state.successPercentage}
                                    </h5>
                                    <h5 style={failureStyle}>
                                        Failure:{this.state.failurePercentage}
                                    </h5>
                                </div>
                                <div style={{height: height * 0.2, width: width * 0.9}}>
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
                    <div style={{height: height * 0.1, width: width * 0.9}}>
                        <a href={this.state.widgetTexts.seeMoreLink}>
                            <Button color="primary" variant="contained" component="span">
                                See More >>
                            </Button>
                        </a>
                    </div>
                </div>
            </MuiThemeProvider>
        );
    }
}

global.dashboard.registerWidget('IsAnalyticsSummary', IsAnalyticsSummary);