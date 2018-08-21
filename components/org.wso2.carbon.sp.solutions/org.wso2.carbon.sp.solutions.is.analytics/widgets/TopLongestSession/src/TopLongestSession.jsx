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

import VizG from 'react-vizgrammar';
import Widget from '@wso2-dashboards/widget';
import {MuiThemeProvider, darkBaseTheme, getMuiTheme} from 'material-ui/styles';
import Pagination from 'material-ui-pagination';

const dataPerPage = 3;

class TopLongestSession extends Widget{
    constructor(props){
        super(props);

        this.ChartConfig =
        {
            x: "username",
            charts: [
                {
                    type: "bar",
                    y: "duration",
                    color: "sessionId",
                    colorScale: ["#00e600"],
                    orientation: "left"
                }
            ],
            yAxisLabel: ' Duration (s)',
            xAxisLabel: 'Username',
            maxLength: 10,
            legend: false,
            append: false,
        };
        this.metadata = {
               names: ['sessionId','username', 'duration'],
               types: ['ordinal','ordinal', 'linear']

        };

        this.state ={
            data: [],
            metadata: this.metadata,
            ChartConfig: this.ChartConfig,
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            currentDataSet: [],
            currentPageNumber: 1
        };

        this.handleResize = this.handleResize.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
        this.handleDataReceived = this.handleDataReceived.bind(this);
        this.setReceivedMsg = this.setReceivedMsg.bind(this);
        this.assembleQuery = this.assembleQuery.bind(this);
        this.updateTable = this.updateTable.bind(this);

    }

    handleResize(){
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }
    componentDidMount(setData) {
        console.log("Configs: ", super.getWidgetConfiguration(this.props.widgetID));

        super.subscribe(this.setReceivedMsg);
        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {
                this.setState({
                    providerConfig: message.data.configs.providerConfig
                });
            })
    }

    componentWillUnmount() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
    }

    handleDataReceived(message) {
       message.data = message.data.reverse();
        this.updateTable(message.data, this.state.currentPageNumber, true);
    }
    updateTable(data, pageNumber, isAvailable) {
        let internalPageNumber = pageNumber - 1; 
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

        if (isAvailable) {
            let dataLength = data.length;
            if (endPoint > dataLength) {
                endPoint = dataLength;
            }
            let dataSet = data.slice(startPoint, endPoint);

            this.setState({
                data: data,
                currentDataSet: dataSet,
                currentPageNumber: pageNumber,
                pageCount: totalPageCount,
            });
        }
    }
    setReceivedMsg(message)
    {
        this.setState({
            fromDate: message.from,
            toDate: message.to,
            data: [],
            currentDataSet: [],
        }, this.assembleQuery);
    }
    assembleQuery(){
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
        let dataProviderConfigs = _.cloneDeep(this.state.providerConfig);
        let query = dataProviderConfigs.configs.config.queryData.query;
        query = query
            .replace('begin', this.state.fromDate)
            .replace('finish', this.state.toDate)
            .replace('now', new Date().getTime() );
        dataProviderConfigs.configs.config.queryData.query = query;
        super.getWidgetChannelManager()
            .subscribeWidget(this.props.id, this.handleDataReceived, dataProviderConfigs);
    }

    render(){
            return (
                <MuiThemeProvider muiTheme={getMuiTheme(darkBaseTheme)}>
                <section style={{paddingTop: 25}}>
                        <VizG
                            config={this.state.ChartConfig}
                            metadata={this.state.metadata}
                            data={this.state.currentDataSet}
                            height={this.state.height * .8}
                            width={this.state.width*1.2}
                            theme={this.props.muiTheme.name}
                        />
                </section>
                            <Pagination
                              total={this.state.pageCount}
                              current={this.state.currentPageNumber}
                              display={3}
                              onChange={number => this.updateTable(this.state.data, number, true)}
                            />
                </MuiThemeProvider>
            );
        }
}
global.dashboard.registerWidget("TopLongestSession", TopLongestSession);