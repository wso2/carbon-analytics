/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import Widget from "@wso2-dashboards/widget";
import VizG from 'react-vizgrammar';
import { MuiThemeProvider, darkBaseTheme, getMuiTheme } from 'material-ui/styles';
import moment from 'moment';
let TENANT_ID = '-1234';
let MESSAGE_PAGE = "message";
let PARAM_ID = "id";

class MessageTable extends Widget {
    constructor(props) {
        super(props);

        this.props.glContainer.setTitle(
            "Messages"
        );

        this.chartConfig = {
            "charts": [
                {
                    "type": "table",
                    "columns": [
                        {
                            "name": "messageFlowId",
                            "title": "Message ID"
                        },
                        {
                            "name": "host",
                            "title": "Host"
                        },
                        {
                            "name": "startTime",
                            "title": "Start Time"
                        },
                        {
                            "name": "faultCount",
                            "title": "Status"
                        }
                    ]
                }
            ],
            "pagination": true,
            "filterable": true,
            "append": false
        };

        this.metadata = {
            "names": [
                "messageFlowId",
                "host",
                "startTime",
                "faultCount"
            ],
            "types": [
                "ordinal",
                "ordinal",
                "time",
                "ordinal"
            ]
        };

        this.state = {
            data: [],
            metadata: this.metadata,
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            btnGroupHeight: 100
        };
        this.isDataLoaded = false;
        this.handleResize = this.handleResize.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
        this.handleStats = this.handleStats.bind(this);
        this.handleGraphUpdate = this.handleGraphUpdate.bind(this);
        this.handlePublisherParameters = this.handlePublisherParameters.bind(this);
        this.getCurrentPage = this.getCurrentPage.bind(this);
        this.getUrlParameter = this.getUrlParameter.bind(this);
        this.handleRowSelect = this.handleRowSelect.bind(this);


    }

    handleRowSelect(event) {
        //get the messageId from the selected row 
        let messageId = event.messageFlowId;
        //substring and modify the URL, so on click, the page is directed to relevant message page with query parameter 'id'
        let currPageStr = parent.window.location.href;
        let index = currPageStr.lastIndexOf("/");
        let tempMsgPageURL = currPageStr.substring(0, (index + 1)) + MESSAGE_PAGE;
        let msgPageURL = new URL(tempMsgPageURL);
        msgPageURL.searchParams.append(PARAM_ID, messageId);
        window.location.href = msgPageURL;
    }
    handleResize() {
        this.setState({ width: this.props.glContainer.width, height: this.props.glContainer.height });
    }

    componentWillMount() {
        super.subscribe(this.handlePublisherParameters);
    }

    handlePublisherParameters(message) {
        if ('granularity' in message) {
            // Update time parameters and clear existing table
            this.setState({
                timeFromParameter: message.from,
                timeToParameter: message.to,
                timeUnitParameter: message.granularity,
                data: []
            }, this.handleGraphUpdate);
        }
    }

    handleGraphUpdate() {
        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {


                super.getWidgetChannelManager().unsubscribeWidget(this.props.id);

                // Get data provider sub json string from the widget configuration
                let dataProviderConf = MessageTable.getProviderConf(message.data);
                let query = dataProviderConf.configs.config.queryData.query;
                let pageName = this.getCurrentPage();
                let componentName;
                let componentType;
                let componentIdentifier = "componentName";
                let urlParams = new URLSearchParams(window.location.search);

                if (urlParams.has('id')) {
                    componentName = this.getUrlParameter('id');
                }

                if (pageName == "api") {
                    componentType = "api";
                } else if (pageName == "proxy") {
                    componentType = "proxy service"
                } else {
                    if (urlParams.has('entryPoint')) {
                        entryPoint = this.getUrlParameter('entryPoint')
                    }
                    if (pageName == "mediator") {
                        componentType = "mediator";
                        componentIdentifier = "componentId";
                    } else if (pageName == "endpoint") {
                        componentType = "endpoint";
                    } else if (pageName == "sequence") {
                        componentType = "sequence";
                    } else if (pageName == "inbound") {
                        componentType = "inbound endpoint";
                    }
                }
                // Insert required parameters to the query string
                let formattedQuery = query
                    .replace("{{timeFrom}}", this.state.timeFromParameter)
                    .replace("{{timeTo}}", this.state.timeToParameter)
                    .replace("{{metaTenantId}}", TENANT_ID)
                    .replace("{{componentType}}", componentType)
                    .replace("{{componentIdentifier}}", componentIdentifier)
                    .replace("{{componentName}}", componentName);
                dataProviderConf.configs.config.queryData.query = formattedQuery;
                // Request datastore with the modified query
                super.getWidgetChannelManager()
                    .subscribeWidget(
                        this.props.id, this.handleStats, dataProviderConf
                    );


            })
            .catch((error) => {

            });
    }

    static getProviderConf(widgetConfiguration) {
        return widgetConfiguration.configs.providerConfig;
    }


    handleStats(stats) {
        let dataArray = stats.data;
        dataArray.forEach(element => {
            element[2] = moment(element[2]).format("YYYY-MM-DD HH:mm:ss");
        });
        this.setState({
            metadata: stats.metadata,
            data: dataArray
        });
    }

    componentWillUnmount() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
    }


    getCurrentPage() {
        let pageName;
        let href = parent.window.location.href;
        let lastSegment = href.substr(href.lastIndexOf('/') + 1);
        if (lastSegment.indexOf('?') == -1) {
            pageName = lastSegment;

        } else {
            pageName = lastSegment.substr(0, lastSegment.indexOf('?'));
        }
        return pageName;
    }

    getUrlParameter(name) {
        name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
        var regex = new RegExp('[\\?&]' + name + '=([^&#]*)');
        var results = regex.exec(location.search);
        return results === null ? '' : decodeURIComponent(results[1].replace(/\+/g, ' '));
    };

    render() {
        return (
            <MuiThemeProvider muiTheme={getMuiTheme(darkBaseTheme)}>
                <section style={{ paddingTop: 50 }}>
                    <VizG
                        config={this.chartConfig}
                        metadata={this.state.metadata}
                        data={this.state.data}
                        height={this.state.height - this.state.btnGroupHeight}
                        width={this.state.width}
                        theme={this.props.muiTheme.name}
                        onClick={this.handleRowSelect}
                    />
                </section>
            </MuiThemeProvider>
        );
    }
}

global.dashboard.registerWidget("MessageTable", MessageTable);