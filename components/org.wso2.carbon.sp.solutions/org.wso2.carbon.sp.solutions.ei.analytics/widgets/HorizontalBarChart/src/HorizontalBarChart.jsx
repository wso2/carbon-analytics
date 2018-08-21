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

import React from 'react';
import Widget from '@wso2-dashboards/widget';
import VizG from 'react-vizgrammar';
import moment from 'moment';

const BAR_GRAPH_TYPE = 'Component Type Selection';
const URL_PARAMETER_ID = 'id';
const DIV_ID_GRAPH = 'graph';
const PUBLISHER_DATE_TIME_PICKER = 'granularity';
const TENANT_ID = '-1234';

/**
 * Dashboard widget class for the HorizontalBarChart widget
 */
class HorizontalBarChart extends Widget {
    /**
     * Initialize graph parameters with the default values
     * @param props Props received from the dashboard
     */
    constructor(props) {
        super(props);
        this.props.glContainer.setTitle(
            'Top ' + props.configs.options[BAR_GRAPH_TYPE] + 's by Request Count'
        );
        const config = {
            'x': 'Name',
            charts: [
                {
                    type: 'bar',
                    y: 'Requests',
                    'fill': 'rgb(21, 101, 192)',
                    orientation: 'left'
                },
            ],
            legend: false,
            append: false,
            'disableVerticalGrid': true,
            'disableHorizontalGrid': true,
            'animate': true,
        };
        const metadata = {
            'names': [
                'Name',
                'Requests'
            ],
            types: [
                'ordinal',
                'linear'
            ],
        };
        this.state = {
            graphConfig: config,
            graphMetadata: metadata,
            graphData: null,
            graphWidth: props.width,
            graphHeight: props.height,
            graphType: props.configs.options[BAR_GRAPH_TYPE],
            isLoading: true,
            timeFromParameter: null,
            timeToParameter: null,
            timeUnitParameter: null,
        };
        this.props.glContainer.on('resize', () => {
                this.setState({
                    width: this.props.glContainer.width,
                    height: this.props.glContainer.height,
                });
            }
        );
        this.isConfLoadError = false;
        this.handlePublisherParameters = this.handlePublisherParameters.bind(this);
        this.handleGraphUpdate = this.handleGraphUpdate.bind(this);
        this.handleStats = this.handleStats.bind(this);
    }

    componentWillMount() {
        super.subscribe(this.handlePublisherParameters);
    }

    /**
     * Handle published messages from the subscribed widgets in the dashboard to extract required parameters
     *
     * @param message JSON object coming from the subscribed widgets
     */
    handlePublisherParameters(message) {
        if (PUBLISHER_DATE_TIME_PICKER in message) {
            this.setState({
                timeFromParameter: moment(message.from).format('YYYY-MM-DD HH:mm:ss'),
                timeToParameter: moment(message.to).format('YYYY-MM-DD HH:mm:ss'),
                timeUnitParameter: message.granularity,
                isLoading: true,
            }, this.handleGraphUpdate);
        }
    }

    /**
     * Update graph parameters according to the updated publisher widget parameters
     */
    handleGraphUpdate() {
        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {
                this.isConfLoadError = false;
                /* Get data provider sub json string from the widget configuration. */
                const dataProviderConf = HorizontalBarChart.getProviderConf(message.data);
                let query = dataProviderConf.configs.config.queryData.query;
                const graphType = this.state.graphType;
                const aggregator = (
                    graphType === 'api' || graphType === 'proxy service' || graphType === 'inbound endpoint'
                ) ? 'ESBStatAgg' : 'MediatorStatAgg';
                /* Insert required parameters to the query string. */
                dataProviderConf.configs.config.queryData.query = query
                    .replace('{{aggregator}}', aggregator)
                    .replace('{{componentType}}', graphType)
                    .replace('{{tenantId}}', TENANT_ID)
                    .replace('{{timeFrom}}', this.state.timeFromParameter)
                    .replace('{{timeTo}}', this.state.timeToParameter);
                /* Request data-store with the modified query. */
                super.getWidgetChannelManager().subscribeWidget(
                    this.props.id,
                    this.handleStats,
                    dataProviderConf
                );
            })
            .catch(() => {
                this.isConfLoadError = true;
            });
    }

    static getProviderConf(widgetConfiguration) {
        return widgetConfiguration.configs.providerConfig;
    }

    /**
     * Draw the graph with the data retrieved from the data store
     */
    handleStats(stats) {
        /* For each data point(Ex: For each API), an array of [total invocations, component name of that data point]. */
        const dataPointArray = stats.data;
        /* index and label mapping of each element in a data point. */
        const labelMapper = {};
        stats.metadata.names.forEach((value, index) => {
            labelMapper[value] = index;
        });
        /* Build data for the graph. */
        const data = [];
        dataPointArray.forEach((dataPoint) => {
            /* Filter well known components. */
            let excludeEndpoints;
            switch (this.state.graphType) {
                case 'endpoint':
                    excludeEndpoints = ['AnonymousEndpoint'];
                    break;
                case 'sequence':
                    excludeEndpoints = ['PROXY_INSEQ', 'PROXY_OUTSEQ', 'PROXY_FAULTSEQ', 'API_OUTSEQ', 'API_INSEQ',
                        'API_FAULTSEQ', 'AnonymousSequence', 'fault'];
                    break;
                default:
                    excludeEndpoints = [];
            }
            const componentName = dataPoint[labelMapper.componentName];
            const validity = excludeEndpoints.indexOf(componentName) === -1;
            if (validity) {
                data.push([
                    componentName,
                    dataPoint[labelMapper.totalInvocations],
                ],);
            }
        });
        /* Draw the graph with received stats only if data is present after filtering. */
        if (data.length > 0) {
            this.setState({
                graphData: data,
                isLoading: false,
            });
        }
    }

    /**
     * Return notification message when required parameters to draw the graph are not available
     *
     * @returns {*} <div> element containing the notification message
     */
    renderEmptyRecordsMessage() {
        return (
            <div className="status-message" style={{color: 'white', marginLeft: 'auto', marginRight: 'auto'}}>
                <div className="message message-info">
                    <h4>
                        <i class="icon fw fw-info"/> No records found</h4>
                    <p>
                        {
                            this.isConfLoadError ? "Error loading widget configuration file" :
                                "Please select a valid date range to view stats."
                        }
                    </p>
                </div>
            </div>
        );
    }

    handleGraphOnClick(message) {
        const clickedComponentName = message.Name;
        const urlString = window.location.href;
        const pageNameStartIndex = urlString.lastIndexOf('/');
        const pageNameEndIndex = urlString.indexOf('?');
        let redirectPageName;
        switch (this.state.graphType) {
            case 'API':
                redirectPageName = 'api';
                break;
            case 'Endpoint':
                redirectPageName = 'endpoint';
                break;
            case 'Sequence':
                redirectPageName = 'sequence';
                break;
            case 'Mediator':
                redirectPageName = 'mediator';
                break;
            case 'Proxy Service':
                redirectPageName = 'proxy';
                break;
            case 'Inbound Endpoint':
                redirectPageName = 'inbound';
                break;
            default:
                redirectPageName = '';
        }
        const formattedString = urlString.substring(0, pageNameStartIndex + 1) + redirectPageName
            + urlString.substring(pageNameEndIndex, -1);
        const redirectUrl = new URL(formattedString);
        redirectUrl.searchParams.append(URL_PARAMETER_ID, clickedComponentName);
        window.location.href = redirectUrl.toString();
    }

    /**
     * Draw the graph with parameters from the widget state
     *
     * @returns {*} A VizG graph component with the required graph
     */
    renderGraph() {
        return (
            <VizG
                theme={this.props.muiTheme.name}
                config={this.state.graphConfig}
                data={this.state.graphData}
                metadata={this.state.graphMetadata}
                onClick={this.handleGraphOnClick.bind(this)}
                height={this.props.glContainer.height}
                width={this.props.glContainer.width}
            />
        );
    }

    render() {
        return (
            <div id={DIV_ID_GRAPH}>
                {this.state.isLoading ? this.renderEmptyRecordsMessage() : this.renderGraph()}
            </div>
        );
    }
}

global.dashboard.registerWidget('HorizontalBarChart', HorizontalBarChart);
