/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React from "react";
//App Components
import DashboardUtils from "../utils/DashboardUtils";
import ChartCard from "../common/ChartCard";
// Material UI
import {Card, CardHeader, CardMedia, Divider} from "material-ui";

const memoryMetadata = {
    names: ['Time', 'Free Physical Memory', 'Total Physical Memory', 'Free Swap Space', 'Total Swap Space',
        'Committed Virtual Memory'],
    types: ['time', 'linear', 'linear']
};
const memoryLineChartConfig = {
    x: 'Time',
    charts: [{type: 'area', y: 'Free Physical Memory',fill: '#058DC7', markRadius: 2},
        {type: 'area', y: 'Total Physical Memory', fill: '#50B432', markRadius: 2},
        {type: 'area', y: 'Free Swap Space', fill: '#f17b31', markRadius: 2},
        {type: 'area', y: 'Total Swap Space', fill: '#8c51a5', markRadius: 2},
        {type: 'area', y: 'Committed Virtual Memory',fill: '#FFEB3B', markRadius: 2}
    ],
    width: 700,
    height: 200,
    tickLabelColor: '#9c9898',
    axisLabelColor: '#9c9898',
    legendTitleColor: '#9c9898',
    legendTextColor: '#9c9898',
    interactiveLegend: true,
    disableVerticalGrid: true,
    disableHorizontalGrid: true
};

/**
 * JVM Physical memory chart component.
 */
export default class JVMOsPhysicalMemory extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            freePhysicalMemory: this.props.data[0],
            totalPhysicalMemory: this.props.data[1],
            freeSwapSpace: this.props.data[2],
            totalSwapSpace: this.props.data[3],
            virtualMemory: this.props.data[4]
        };
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            freePhysicalMemory: nextprops.data[0],
            totalPhysicalMemory: nextprops.data[1],
            freeSwapSpace: nextprops.data[2],
            totalSwapSpace: nextprops.data[3],
            virtualMemory: nextprops.data[4]
        });
    }

    render() {
        if(this.state.freePhysicalMemory.length === 0 && this.state.totalPhysicalMemory.length === 0
            && this.state.freeSwapSpace.length === 0 && this.state.totalSwapSpace.length === 0
            && this.state.virtualMemory.length === 0){
            return(
                <div style={{paddingLeft: 10}}>
                    <Card>
                        <CardHeader
                            title="JVM Physical Memory (MB)"
                        />
                        <Divider/>
                        <CardMedia>
                            <div style={{backgroundColor: '#131313'}}>
                                <h4 style={{marginTop: 0}}>No Data Available</h4>
                            </div>
                        </CardMedia>
                    </Card>
                </div>
            );
        }
        let data1 = DashboardUtils.getCombinedChartList(this.state.freePhysicalMemory, this.state.totalPhysicalMemory);
        let data2 = DashboardUtils.getCombinedChartList(data1, this.state.freeSwapSpace);
        let data3 = DashboardUtils.getCombinedChartList(data2 , this.state.totalSwapSpace);
        let data = DashboardUtils.getCombinedChartList(data3, this.state.virtualMemory);
        return (
            <div style={{paddingLeft: 10}}>
                <ChartCard data={data} metadata={memoryMetadata} config={memoryLineChartConfig} title="JVM Physical Memory (MB)"/>
            </div>
        );
    }
}