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

import React from 'react';
//App Components
import DashboardUtils from '../utils/DashboardUtils';
import ChartCard from '../common/ChartCard';
// Material UI
import {Card, CardHeader, CardMedia, Divider} from 'material-ui';

const metadata = {
    names: ['Time', 'Mark Sweep count', 'Mark Sweep time', 'Scavenge count', 'Scavenge time'],
    types: ['time', 'linear', 'linear', 'linear', 'linear']
};

/**
 * JVM Heap Memory chart component.
 */
export default class JVMGarbageCOllector extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            jvmGcPsMarksweepCount: this.props.data[0],
            jvmGcPsMarksweepTime: this.props.data[1],
            jvmGcPsScavengeCount: this.props.data[2],
            jvmGcPsScavengeTime: this.props.data[3],
            tickCount: 10
        };
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            jvmGcPsMarksweepCount: nextprops.data[0],
            jvmGcPsMarksweepTime: nextprops.data[1],
            jvmGcPsScavengeCount: nextprops.data[2],
            jvmGcPsScavengeTime: nextprops.data[3],
            tickCount: nextprops.data[0].length > 10 ? 10 : nextprops.data[0].length
        });
    }

    render() {
        const chartConfig = {
            x: 'Time',
            charts: [{type: 'area', y: 'Mark Sweep count', fill: '#0e1cc7', style: {markRadius: 2}},
                {type: 'area', y: 'Mark Sweep time', fill: '#50B432', style: {markRadius: 2}},
                {type: 'area', y: 'Scavenge count', fill: '#f17b31', style: {markRadius: 2}},
                {type: 'area', y: 'Scavenge time', fill: '#6508a5', style: {markRadius: 2}}
            ],

            width: 700,
            height: 200,
            style: {
                tickLabelColor: '#f2f2f2',
                legendTextColor: '#9c9898',
                legendTitleColor: '#9c9898',
                axisLabelColor: '#9c9898',
                legendTextSize: 12,
                legendTitleSize: 12
            },
            legend: true,
            interactiveLegend: true,
            gridColor: '#f2f2f2',
            tipTimeFormat: "%Y-%m-%d %H:%M:%S %Z",
            xAxisTickCount: this.state.tickCount
        };
        if (this.state.jvmGcPsMarksweepCount.length === 0 && this.state.jvmGcPsMarksweepTime.length === 0
            && this.state.jvmGcPsScavengeCount.length === 0 && this.state.jvmGcPsScavengeTime.length === 0) {
            return (
                <div style={{paddingLeft: 10}}>
                    <Card>
                        <CardHeader
                            title="JVM Garbage Collector."
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
        let data1 = DashboardUtils.getCombinedChartList(this.state.jvmGcPsMarksweepCount, this.state.jvmGcPsMarksweepTime);
        let intY = DashboardUtils.initCombinedYDomain(this.state.jvmGcPsMarksweepCount, this.state.jvmGcPsMarksweepTime);
        let data2 = DashboardUtils.getCombinedChartList(data1, this.state.jvmGcPsScavengeCount);
        let y2 = DashboardUtils.getCombinedYDomain(this.state.jvmGcPsScavengeCount, intY);
        let data = DashboardUtils.getCombinedChartList(data2, this.state.jvmGcPsScavengeTime);
        let y3 = DashboardUtils.getCombinedYDomain(this.state.jvmGcPsScavengeTime, y2);
        return (
            <div style={{paddingLeft: 10}}>
                <ChartCard data={data} yDomain={y3}
                           metadata={metadata} config={chartConfig} title="JVM Garbage Collector"/>
            </div>
        );
    }
}