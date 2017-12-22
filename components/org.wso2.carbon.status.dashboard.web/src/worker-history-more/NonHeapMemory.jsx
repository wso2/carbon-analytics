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

const metadata = {names: ['Time', 'Non-Heap Init', 'Non-Heap Used','Non-Heap Committed','Non-Heap Max','Non-Heap' +
' Usage'],
    types: ['time', 'linear', 'linear', 'linear', 'linear']};


/**
 * JVM Non-Heap Memory chart component.
 */
export default class NonHeapMemory extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            jvmMemoryNonHeapInit: this.props.data[0],
            jvmMemoryNonHeapUsed: this.props.data[1],
            jvmMemoryNonHeapCommitted: this.props.data[2],
            jvmMemoryNonHeapMax: this.props.data[3],
            jvmMemoryNonHeapUsage:this.props.data[4],
            tickCount: 10
        };
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            jvmMemoryNonHeapInit: nextprops.data[0],
            jvmMemoryNonHeapUsed: nextprops.data[1],
            jvmMemoryNonHeapCommitted: nextprops.data[2],
            jvmMemoryNonHeapMax: nextprops.data[3],
            jvmMemoryNonHeapUsage: nextprops.data[4],
            tickCount: nextprops.data[0].length>10 ? 10 : nextprops.data[0].length
        });
    }

    render() {
        const chartConfig = {
            x: 'Time',
            charts: [{type: 'area', y: 'Non-Heap Init',fill: '#058DC7', style: {markRadius: 2}},
                {type: 'area', y: 'Non-Heap Used', fill: '#50B432', style: {markRadius: 2}},
                {type: 'area', y: 'Non-Heap Committed', fill: '#f17b31', style: {markRadius: 2}},
                {type: 'area', y: 'Non-Heap Max', fill: '#8c51a5', style: {markRadius: 2}},
                {type: 'area', y: 'Non-Heap Usage', fill: '#540aa5', style: {markRadius: 2}}
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
            tipTimeFormat:"%Y-%m-%d %H:%M:%S %Z",
            interactiveLegend: true,
            gridColor: '#f2f2f2',
            xAxisTickCount:this.state.tickCount
        };
        if(this.state.jvmMemoryNonHeapInit.length === 0 && this.state.jvmMemoryNonHeapUsed.length === 0
            && this.state.jvmMemoryNonHeapCommitted.length === 0 && this.state.jvmMemoryNonHeapMax.length === 0){
            return(
                <div style={{paddingLeft: 10}}>
                    <Card>
                        <CardHeader
                            title="JVM Non-Heap Memory (bytes)"
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
        let data1 = DashboardUtils.getCombinedChartList(this.state.jvmMemoryNonHeapInit, this.state.jvmMemoryNonHeapUsed);
        let intY= DashboardUtils.initCombinedYDomain(this.state.jvmMemoryNonHeapInit, this.state.jvmMemoryNonHeapUsed);
        let data2 = DashboardUtils.getCombinedChartList(data1, this.state.jvmMemoryNonHeapCommitted);
        let y2 = DashboardUtils.getCombinedYDomain(this.state.jvmMemoryNonHeapCommitted,intY);
        let data3 = DashboardUtils.getCombinedChartList(data2, this.state.jvmMemoryNonHeapMax);
        let y3 = DashboardUtils.getCombinedYDomain(this.state.jvmMemoryNonHeapMax,y2);
        let data = DashboardUtils.getCombinedChartList(data3 , this.state.jvmMemoryNonHeapUsage);
        let y = DashboardUtils.getCombinedYDomain(this.state.jvmMemoryNonHeapUsage,y3);
        return (
            <div style={{paddingLeft: 10}}>
                <ChartCard data={data} yDomain={y}
                           metadata={metadata} config={chartConfig} title="JVM Non-Heap Memory (bytes)"/>
            </div>
        );
    }
}