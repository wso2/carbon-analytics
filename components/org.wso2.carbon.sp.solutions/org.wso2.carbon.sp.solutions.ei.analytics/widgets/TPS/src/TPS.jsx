/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import Widget from '@wso2-dashboards/widget';
import VizG from 'react-vizgrammar';
import moment from 'moment';

var PUBLISHER_DATE_TIME_PICKER = 'granularity';
var TENANT_ID = '-1234';

class TPS extends Widget {
    constructor(props) {
        super(props);

        // Set title to 'OverallTPS'
        this.props.glContainer.setTitle(
            "OVERALL TPS"
        );

        var config = {
            "x": 'Time',
            "charts": [{ type: "line", y: "TPS" }],
            "maxLength": 10,
            "width": 400,
            "height": 200,
            "legend": false,
            "append": false,
            "disableVerticalGrid": true,
            "disableHorizontalGrid": true,
            "animate": true
        };

        let metadata = {
            "names": [
                "Time",
                "TPS"
            ],
            "types": [
                "time",
                "linear"
            ]
        }

        let data = [];

        this.state = {
            graphConfig: config,
            graphMetadata: metadata,
            graphData: data,
            graphWidth: props.width,
            graphHeight: props.height,
            clearGraph: true,
            timeFromParameter: null,
            timeToParameter: null,
            timeUnitParameter: null
        };

        this.props.glContainer.on('resize', this.handleResize.bind(this));

        this.handlePublisherParameters = this.handlePublisherParameters.bind(this);
        this.handleGraphUpdate = this.handleGraphUpdate.bind(this);
        this.handleStats = this.handleStats.bind(this);
    }

    handleResize() {
        this.setState({ width: this.props.glContainer.width, height: this.props.glContainer.height });
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
            // Update time parameters and clear existing graph
            this.setState({
                timeFromParameter: moment(message.from).format("YYYY-MM-DD HH:mm:ss"),
                timeToParameter: moment(message.to).format("YYYY-MM-DD HH:mm:ss"),
                timeUnitParameter: message.granularity,
                clearGraph: true
            }, this.handleGraphUpdate);
        }
    }

    /**
     * Update graph parameters according to the updated publisher widget parameters
     */
    handleGraphUpdate() {
        //console.log('A');
        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {

                // Get data provider sub json string from the widget configuration
                let dataProviderConf = this.getProviderConf(message.data);
                var query = dataProviderConf.configs.config.queryData.query;

                let timeUnit = this.state.timeUnitParameter.concat("s");

                // Insert required parameters to the query string
                let formattedQuery = query
                    .replace("{{tenantId}}", TENANT_ID)
                    .replace("{{timeFrom}}", "\'" + this.state.timeFromParameter + "\'")
                    .replace("{{timeTo}}", "\'" + this.state.timeToParameter + "\'")
                    .replace("{{timeunit}}", "\'" + timeUnit + "\'")

                dataProviderConf.configs.config.queryData.query = formattedQuery;

                // Request datastore with the modified query
                super.getWidgetChannelManager()
                    .subscribeWidget(
                        this.props.id, this.handleStats, dataProviderConf
                    );
            })
            .catch((error) => {
                // console.log(error);
            });
    }

    getProviderConf(widgetConfiguration) {
        return widgetConfiguration.configs.providerConfig;
    }

    

    /**
     * Draw the graph with the data retrieved from the data store
     */
    handleStats(stats) {
        // For each data point(Ex: For each API), an array of [total invocations, component name of that data point]
        let dataPointArray = stats.data;
        let divider = 1;

        // index and label mapping of each element in a data point
        let labelMapper = {};
        stats.metadata.names.forEach((value, index) => {
            labelMapper[value] = index;
        })

        dataPointArray.forEach((e) => {
            switch (this.state.timeUnitParameter) {
                case "month":
                    divider = 3600 * 24 * 30;
                    let timeStamp=new Date(e[labelMapper.AGG_TIMESTAMP]);
                    divider=new Date(timeStamp.getFullYear(),(timeStamp.getMonth()+1),0).getDate()*3600*24;            
                    break;
                case "day":
                    divider = 3600 * 24;
                    break;
                case "hour":
                    divider = 3600;
                    break;
                case "minute":
                    divider = 60;
                    break;
            }
            e[labelMapper.noOfInvocation]=(e[labelMapper.noOfInvocation])/divider;
        });


        // Build data for the graph
        let data = [];
        dataPointArray.forEach((dataPoint) => {
            data.push(
                [dataPoint[labelMapper.AGG_TIMESTAMP], dataPoint[labelMapper.noOfInvocation]]
            );
        });


        console.log(data);
        // Draw the graph with received stats only if data is present after filtering
        if (data.length > 0) {
            this.setState({
                graphData: data,
                clearGraph: false
            });
        }
    }

    /**
     * Return notification message when required parameters to draw the graph are not available
     *
     * @returns {*} <div> element containing the notification message
     */
    getEmptyRecordsText() {
        return (
            <div class="status-message" style={{ color: 'white', marginLeft: 'auto', marginRight: 'auto' }}>
                <div class="message message-info">
                    <h4><i class="icon fw fw-info"></i> No records found</h4>
                    <p>Please select a valid date range to view stats.</p>
                </div>
            </div>
        );
    };



    /**
     * Draw the graph with parameters from the widget state
     *
     * @returns {*} A VizG graph component with the required graph
     */
    drawGraph() {
        //console.log("Graph Config:", this.state.graphConfig);
        return <VizG
            theme={this.props.muiTheme.name}
            config={this.state.graphConfig}
            data={this.state.graphData}
            metadata={this.state.graphMetadata}
            height={this.props.glContainer.height}
            width={this.props.glContainer.width}
        />;
    }

    render() {
        return (
            <div>
                {this.state.clearGraph ? this.getEmptyRecordsText() : this.drawGraph()}
            </div>
        );
    }
}

global.dashboard.registerWidget('TPS', TPS);