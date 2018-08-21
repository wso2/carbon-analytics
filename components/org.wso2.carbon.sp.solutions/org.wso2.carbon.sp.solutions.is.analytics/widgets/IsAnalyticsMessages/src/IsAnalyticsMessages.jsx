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
import {Scrollbars} from 'react-custom-scrollbars';
import {MuiThemeProvider} from '@material-ui/core/styles';
import _ from 'lodash';

const colorGreen = "#6ED460";
const colorRed = "#EC5D40";
const boolColorScale = [colorRed, colorGreen];

let metadataOverall = {
    names: ['contextId', 'username', 'serviceProvider', 'authenticationStep', 'rolesCommaSeparated','tenantDomain', 'remoteIp', 'region', 'authSuccess', 'utcTime'],
    types: ['ordinal', 'ordinal', 'ordinal', 'ordinal', 'ordinal', 'ordinal', 'ordinal', 'ordinal', 'ordinal', 'ordinal']
};

let metadataLocal = {
    names: ['contextId', 'username', 'serviceProvider', 'userStoreDomain', 'tenantDomain', 'rolesCommaSeparated', 'remoteIp', 'region', 'authSuccess', 'utcTime'],
    types: ['ordinal', 'ordinal', 'ordinal', 'ordinal', 'ordinal', 'ordinal', 'ordinal', 'ordinal', 'ordinal', 'ordinal']
};

let metadataFederated = {
    names: ['contextId', 'username', 'serviceProvider', 'identityProvider', 'remoteIp', 'region', 'authSuccess', 'utcTime'],
    types: ['ordinal', 'ordinal', 'ordinal', 'ordinal', 'ordinal', 'ordinal', 'ordinal', 'ordinal']
};

let columnsOverall = [
    {
        name: "contextId",
        title: "Context ID"
    },
    {
        name: "username",
        title: "User Name"
    },
    {
        name: "serviceProvider",
        title: "Service Provider"
    },
    {
        name: "authenticationStep",
        title: "Subject Step"
    },
    {
        name: "rolesCommaSeparated",
        title: "Roles"
    },
    {
        name: "tenantDomain",
        title: "Tenant Domain"
    },
    {
        name: "remoteIp",
        title: "IP"
    },
    {
        name: "region",
        title: "Region"
    },
    {
        name: "authSuccess",
        title: "Overall Authentication",
        colorBasedStyle: true,
        colorScale: boolColorScale,
    },
    {
        name: "utcTime",
        title: "Timestamp"
    }
];

let columnsLocal = [
    {
        name: "contextId",
        title: "Context ID"
    },
    {
        name: "username",
        title: "User Name"
    },
    {
        name: "serviceProvider",
        title: "Service Provider"
    },
    {
        name: "userStoreDomain",
        title: "User Store"
    },
    {
        name: "tenantDomain",
        title: "Tenant Domain"
    },
    {
        name: "rolesCommaSeparated",
        title: "Roles"
    },
    {
        name: "remoteIp",
        title: "IP"
    },
    {
        name: "region",
        title: "Region"
    },
    {
        name: "authSuccess",
        title: "Local Authentication",
        colorBasedStyle: true,
        colorScale: boolColorScale,
    },
    {
        name: "utcTime",
        title: "Timestamp"
    }
];

let columnsFederated = [
    {
        name: "contextId",
        title: "Context ID"
    },
    {
        name: "username",
        title: "User Name"
    },
    {
        name: "serviceProvider",
        title: "Service Provider"
    },
    {
        name: "identityProvider",
        title: "identityProvider"
    },
    {
        name: "remoteIp",
        title: "IP"
    },
    {
        name: "region",
        title: "Region"
    },
    {
        name: "authSuccess",
        title: "Authentication Step Success",
        colorBasedStyle:true,
        colorScale: boolColorScale,
    },
    {
        name: "utcTime",
        title: "Timestamp"
    }
];

let tableConfig = {
    charts: [
        {
            type: "table",
        }
    ],
    pagination: true,
    filterable: true,
    append: false
};

class IsAnalyticsMessages extends Widget {
    constructor(props) {
        super(props);

        this.state = {
            tableConfig: tableConfig,
            data: [],
            isProviderConfigsFaulty: false,
            options: this.props.configs.options,
            width: this.props.glContainer.width,
            height: this.props.glContainer.height
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
            .catch(() => {
                this.setState({
                    isProviderConfigsFaulty: true
                });
            });

        let tableConfigClone = _.cloneDeep(tableConfig);
        let metadata = [];
        switch (this.state.options.widgetType) {
            case ("Local"):
                tableConfigClone.charts[0].columns = columnsLocal;
                metadata = metadataLocal;
                break;
            case ("Federated"):
                tableConfigClone.charts[0].columns = columnsFederated;
                metadata = metadataFederated;
                break;
            case ("Overall"):
                tableConfigClone.charts[0].columns = columnsOverall;
                metadata = metadataOverall;
                break;
        }
        this.setState({
            tableConfig: tableConfigClone,
            metadata: metadata,
        })
    }

    handleReceivedData(message) {
        this.setState({
            data: message.data
        });
        window.dispatchEvent(new Event('resize'));
    }

    onReceivingMessage(message) {
        if (message.header === "additionalFilterConditions") {
            if (message.body === "") {
                this.setState({
                    additionalFilterConditions: undefined,
                    data: [],
                }, this.assembleQuery)
            } else {
                this.setState({
                    additionalFilterConditions: message.body,
                    data: [],
                }, this.assembleQuery);
            }
        }
        else {
            this.setState({
                fromDate: message.from,
                toDate: message.to,
                data: []
            }, this.assembleQuery);
        }
    }

    assembleQuery() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
        let dataProviderConfigs = _.cloneDeep(this.state.dataProviderConf);
        let updatedQuery = dataProviderConfigs.configs.config.queryData.query;
        let filterCondition = " on _timestamp > {{from}}L and _timestamp < {{to}}L ";
        let additionalFilters = "";
        let doAdditionalFilter = false;

        filterCondition = filterCondition
            .replace("{{per}}", this.state.per)
            .replace("{{from}}", this.state.fromDate)
            .replace("{{to}}", this.state.toDate);

        if (this.state.options.widgetType === "Local") {
            updatedQuery = dataProviderConfigs.configs.config.queryData.queryLocal;
        } else if (this.state.options.widgetType === "Federated") {
            updatedQuery = dataProviderConfigs.configs.config.queryData.queryFederated;
        }

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

        if (doAdditionalFilter) {
            filterCondition = filterCondition + additionalFilters;
        }

        updatedQuery = updatedQuery.replace("{{filterCondition}}", filterCondition);
        dataProviderConfigs.configs.config.queryData.query = updatedQuery;

        super.getWidgetChannelManager().subscribeWidget(this.props.id, this.handleReceivedData, dataProviderConfigs);
    }

    render() {
        if (this.state.isProviderConfigsFaulty) {
            return (
                <MuiThemeProvider theme={this.props.muiTheme}>
                    <Scrollbars style={{height: this.state.height}}>
                        <div
                            style={{
                                width: this.props.glContainer.width,
                                height: this.props.glContainer.height,
                            }}
                            className="list-table-wrapper"
                        >
                            <h1> Messages </h1>
                            <h5>[ERROR]: Cannot connect with the data provider</h5>
                        </div>
                    </Scrollbars>
                </MuiThemeProvider>
            );
        }
        return (
            <MuiThemeProvider theme={this.props.muiTheme}>
                <Scrollbars style={{height: this.state.height}}>
                    <div
                        style={{
                            width: this.props.glContainer.width,
                            height: this.props.glContainer.height,
                        }}
                        className="list-table-wrapper"
                    >
                        <h1> Messages </h1>
                        <VizG
                            config={this.state.tableConfig}
                            metadata={this.state.metadata}
                            data={this.state.data}
                            append={false}
                            height={this.props.glContainer.height}
                            width={this.props.glContainer.width}
                            theme={this.props.muiTheme.name}
                        />
                    </div>
                </Scrollbars>
            </MuiThemeProvider>
        );
    }
}

global.dashboard.registerWidget('IsAnalyticsMessages', IsAnalyticsMessages);
