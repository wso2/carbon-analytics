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
    names: ['Time','Free Physical Memory', 'Total Physical Memory', 'Total Committed','Total Init','Total Max','Total Used','Pool Size',
        'Committed Virtual Memory'],
    types: ['time', 'linear', 'linear']
};


/**
 * JVM Physical memory chart component.
 */
export default class JVMOsPhysicalMemory extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            jvmMemoryTotalCommitted: this.props.data[0],
            jvmMemoryTotalInit: this.props.data[1],
            jvmMemoryTotalMax: this.props.data[2],
            jvmMemoryTotalUsed: this.props.data[3],
            jvmMemoryPoolsSize: this.props.data[4],
            freePhysicalMemory: this.props.data[5],
            totalPhysicalMemory: this.props.data[6],
            virtualMemory: this.props.data[7],
            tickCount: 10
        };
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            jvmMemoryTotalCommitted: nextprops.data[0],
            jvmMemoryTotalInit: nextprops.data[1],
            jvmMemoryTotalMax: nextprops.data[2],
            jvmMemoryTotalUsed: nextprops.data[3],
            jvmMemoryPoolsSize: nextprops.data[4],
            freePhysicalMemory: nextprops.data[5],
            totalPhysicalMemory: nextprops.data[6],
            virtualMemory: nextprops.data[7],
            tickCount: nextprops.data[0].length>10 ? 10 : nextprops.data[0].length
        });
    }

    render() {
        const memoryLineChartConfig = {
            x: 'Time',
            charts: [{type: 'area', y: 'Free Physical Memory',fill: '#058DC7', style: {markRadius: 2}},
                {type: 'area', y: 'Total Physical Memory', fill: '#50B432', style: {markRadius: 2}},
                {type: 'area', y: 'Total Committed',fill: '#FFEB3B', style: {markRadius: 2}},
                {type: 'area', y: 'Total Init', fill: '#8c51a5', style: {markRadius: 2}},
                {type: 'area', y: 'Total Max', fill: '#f17b31', style: {markRadius: 2}},
                {type: 'area', y: 'Total Used', fill: '#1bf1b9', style: {markRadius: 2}},
                {type: 'area', y: 'Pool Size', fill: '#a5390e', style: {markRadius: 2}},
                {type: 'area', y: 'Committed Virtual Memory',fill: '#7119ff', style: {markRadius: 2}}
            ],
            width: 700,
            height: 200,
            style: {
                tickLabelColor:'#f2f2f2',
                legendTextColor: '#9c9898',
                legendTitleColor: '#9c9898',
                axisLabelColor: '#9c9898',
                legendTextSize:12,
                legendTitleSize:12
            },
            legend:true,
            interactiveLegend: true,
            tipTimeFormat:"%Y-%m-%d %H:%M:%S %Z",
            gridColor: '#f2f2f2',
            xAxisTickCount:this.state.tickCount
        };
        if(this.state.freePhysicalMemory.length === 0 && this.state.totalPhysicalMemory.length === 0
            && this.state.jvmMemoryTotalCommitted.length === 0 && this.state.jvmMemoryTotalInit.length === 0
            && this.state.virtualMemory.length === 0&& this.state.jvmMemoryPoolsSize.length === 0 &&
            this.jvmMemoryTotalUsed.length === 0 && this.state.virtualMemory.length === 0){
            return(
                <div style={{paddingLeft: 10}}>
                    <Card>
                        <CardHeader
                            title="JVM Physical Memory (bytes)"
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
        let intY= DashboardUtils.initCombinedYDomain(this.state.freePhysicalMemory, this.state.totalPhysicalMemory);
        let data2 = DashboardUtils.getCombinedChartList(data1, this.state.jvmMemoryTotalCommitted);
        let y2 = DashboardUtils.getCombinedYDomain( this.state.jvmMemoryTotalCommitted,intY);
        let data3 = DashboardUtils.getCombinedChartList(data2 , this.state.jvmMemoryTotalInit);
        let y3 = DashboardUtils.getCombinedYDomain(this.state.jvmMemoryTotalInit,y2);
        let data4 = DashboardUtils.getCombinedChartList(data3 , this.state.jvmMemoryTotalMax);
        let y4 = DashboardUtils.getCombinedYDomain(this.state.jvmMemoryTotalMax,y3);
        let data5 = DashboardUtils.getCombinedChartList(data4 , this.state.jvmMemoryTotalUsed);
        let y5 = DashboardUtils.getCombinedYDomain(this.state.jvmMemoryTotalUsed,y4);
        let data6 = DashboardUtils.getCombinedChartList(data5 , this.state.jvmMemoryPoolsSize);
        let y6 = DashboardUtils.getCombinedYDomain(this.state.jvmMemoryPoolsSize,y5);
        let data = DashboardUtils.getCombinedChartList(data6, this.state.virtualMemory);
        let y = DashboardUtils.getCombinedYDomain(this.state.virtualMemory,y6);
        return (
            <div style={{paddingLeft: 10}}>
                <ChartCard data={data} metadata={memoryMetadata} config={memoryLineChartConfig}  yDomain={y}
                           title="JVM Physical Memory (bytes)"/>
            </div>
        );
    }
}