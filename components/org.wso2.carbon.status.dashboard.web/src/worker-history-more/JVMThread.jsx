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

const threadMetadata = {names: ['Time', 'Live Threads', 'Daemon Threads', 'Blocked Threads', 'Deadlock Threads', 'New' +
' Threads', 'Runnable Threads', 'Terminated Threads', 'Timed Waiting Threads', 'Waiting Threads'], types: ['time', 'linear', 'linear']};


/**
 * JVM Threads chart component.
 */
export default class JVMThread extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            count: this.props.data[0],
            daemonCount: this.props.data[1],
            jvmThreadsBlockedCount:this.props.data[2],
            jvmThreadsDeadlockCount:this.props.data[3],
            jvmThreadsNewCount:this.props.data[4],
            jvmThreadsRunnableCount:this.props.data[5],
            jvmThreadsTerminatedCount:this.props.data[6],
            jvmThreadsTimedWaitingCount:this.props.data[7],
            jvmThreadsWaitingCount:this.props.data[8],
            tickCount: 20
        };
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            count: nextprops.data[0],
            daemonCount: nextprops.data[1],
            jvmThreadsBlockedCount:nextprops.data[2],
            jvmThreadsDeadlockCount:nextprops.data[3],
            jvmThreadsNewCount:nextprops.data[4],
            jvmThreadsRunnableCount:nextprops.data[5],
            jvmThreadsTerminatedCount:nextprops.data[6],
            jvmThreadsTimedWaitingCount:nextprops.data[7],
            jvmThreadsWaitingCount:nextprops.data[8],
            tickCount: nextprops.data[0].length>20 ? 20 : nextprops.data[0].length
        });
    }

    render() {
        const threadLineChartConfig = {
            x: 'Time',
            charts: [
                {type: 'area', y: 'Live Threads', fill: '#5a09a5', style: {markRadius: 2}},
                {type: 'area', y: 'Daemon Threads', fill: '#1af12c',style: {markRadius: 2}},
                {type: 'area', y:  'Blocked Threads', fill: '#a5390e', style: {markRadius: 2}},
                {type: 'area', y: 'Deadlock Threads', fill: '#0cf1bf',style: {markRadius: 2}},
                {type: 'area', y: 'New Threads', fill: '#f1b32d', style: {markRadius: 2}},
                {type: 'area', y: 'Runnable Threads', fill: '#7b82f1',style: {markRadius: 2}},
                {type: 'area', y: 'Terminated Threads', fill: '#f10c8e', style: {markRadius: 2}},
                {type: 'area', y: 'Timed Waiting Threads',style: {markRadius: 2}},
                {type: 'area', y: 'Waiting Threads', fill: '#f17b31', style: {markRadius: 2}}
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
        if(this.state.count.length === 0 && this.state.daemonCount.length === 0 && this.state.jvmThreadsBlockedCount.length === 0  ){
            return(
                <div style={{paddingLeft: 10}}>
                    <Card>
                        <CardHeader
                            title="Threads"
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
        let data1=DashboardUtils.getCombinedChartList(this.state.count, this.state.daemonCount);
        let intY= DashboardUtils.initCombinedYDomain(this.state.count, this.state.daemonCount);
        let data2=DashboardUtils.getCombinedChartList(data1, this.state.jvmThreadsBlockedCount);
        let y2 = DashboardUtils.getCombinedYDomain(this.state.jvmThreadsBlockedCount,intY);
        let data3=DashboardUtils.getCombinedChartList(data2, this.state.jvmThreadsDeadlockCount);
        let y3 = DashboardUtils.getCombinedYDomain(this.state.jvmThreadsDeadlockCount,y2);
        let data4=DashboardUtils.getCombinedChartList(data3, this.state.jvmThreadsNewCount);
        let y4 = DashboardUtils.getCombinedYDomain(this.state.jvmThreadsNewCount,y3);
        let data5=DashboardUtils.getCombinedChartList(data4, this.state.jvmThreadsRunnableCount);
        let y5 = DashboardUtils.getCombinedYDomain(this.state.jvmThreadsRunnableCount,y4);
        let data6=DashboardUtils.getCombinedChartList(data5, this.state.jvmThreadsTerminatedCount);
        let y6 = DashboardUtils.getCombinedYDomain(this.state.jvmThreadsTerminatedCount,y5);
        let data7=DashboardUtils.getCombinedChartList(data6, this.state.jvmThreadsTimedWaitingCount);
        let y7 = DashboardUtils.getCombinedYDomain(this.state.jvmThreadsTimedWaitingCount,y6);
        let data=DashboardUtils.getCombinedChartList(data7, this.state.jvmThreadsWaitingCount);
        let y = DashboardUtils.getCombinedYDomain(this.state.jvmThreadsWaitingCount,y7);
        return (
            <div style={{paddingLeft: 10}}>
                <ChartCard data={data} yDomain={y}
                           metadata={threadMetadata} config={threadLineChartConfig} title="Threads"/>
            </div>
        );
    }
}