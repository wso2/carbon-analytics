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
// Localization
import { FormattedMessage } from 'react-intl';

const metadata = {
    names: ['Time', 'Heap Init', 'Heap Used', 'Heap Committed', 'Heap Max', 'Heap Usage'],
    types: ['time', 'linear', 'linear', 'linear', 'linear']
};

/**
 * JVM Heap Memory chart component.
 */
export default class HeapMemory extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            jvmMemoryHeapInit: this.props.data[0],
            jvmMemoryHeapUsed: this.props.data[1],
            jvmMemoryHeapCommitted: this.props.data[2],
            jvmMemoryHeapMax: this.props.data[3],
            jvmMemoryHeapUsage: this.props.data[4],
            tickCount: 10
        };
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            jvmMemoryHeapInit: nextprops.data[0],
            jvmMemoryHeapUsed: nextprops.data[1],
            jvmMemoryHeapCommitted: nextprops.data[2],
            jvmMemoryHeapMax: nextprops.data[3],
            jvmMemoryHeapUsage: nextprops.data[4],
            tickCount: nextprops.data[0].length > 10 ? 10 : nextprops.data[0].length
        });
    }

    render() {
        const chartConfig = {
            x: 'Time',
            charts: [{type: 'area', y: 'Heap Init', fill: '#058DC7', style: {markRadius: 2}},
                {type: 'area', y: 'Heap Used', fill: '#50B432', style: {markRadius: 2}},
                {type: 'area', y: 'Heap Committed', fill: '#f17b31', style: {markRadius: 2}},
                {type: 'area', y: 'Heap Max', fill: '#8c51a5', style: {markRadius: 2}},
                {type: 'area', y: 'Heap Usage', fill: '#5a09a5', style: {markRadius: 2}}
            ],

            width: 700,
            height: 200,
            style: {
                tickLabelColor: '#f2f2f2',
                legendTextColor: '#9c9898',
                legendTitleColor: '#9c9898',
                axisLabelColor: '#9c9898',
                legendTextSize: 10,
                legendTitleSize: 12
            },
            tipTimeFormat: "%Y-%m-%d %H:%M:%S %Z",
            legend: true,
            interactiveLegend: true,
            gridColor: '#f2f2f2',
            xAxisTickCount: this.state.tickCount
        };
        if (this.state.jvmMemoryHeapInit.length === 0 && this.state.jvmMemoryHeapUsed.length === 0
            && this.state.jvmMemoryHeapCommitted.length === 0 && this.state.jvmMemoryHeapMax.length === 0 &&
            this.state.jvmMemoryHeapUsage.length === 0) {
            return (
                <div>
                    <Card>
                        <CardHeader
                            title="JVM Heap Memory (bytes)"
                        />
                        <Divider/>
                        <CardMedia>
                            <div style={{
                                backgroundColor: '#131313', textAlign: 'center', lineHeight: '60px',
                                color: '#9c9898'
                            }}>
                                <FormattedMessage id='noData' defaultMessage='No Data Available' />
                            </div>
                        </CardMedia>
                    </Card>
                </div>
            );
        }
        let data1 = DashboardUtils.getCombinedChartList(this.state.jvmMemoryHeapInit, this.state.jvmMemoryHeapUsed);
        let intY = DashboardUtils.initCombinedYDomain(this.state.jvmMemoryHeapInit, this.state.jvmMemoryHeapUsed);
        let data2 = DashboardUtils.getCombinedChartList(data1, this.state.jvmMemoryHeapCommitted);
        let y2 = DashboardUtils.getCombinedYDomain(this.state.jvmMemoryHeapCommitted, intY);
        let data3 = DashboardUtils.getCombinedChartList(data2, this.state.jvmMemoryHeapMax);
        let y3 = DashboardUtils.getCombinedYDomain(this.state.jvmMemoryHeapMax, y2);
        let data = DashboardUtils.getCombinedChartList(data3, this.state.jvmMemoryHeapUsage);
        let y = DashboardUtils.getCombinedYDomain(this.state.jvmMemoryHeapUsage, y3);
        return (
            <div>
                <ChartCard data={data} yDomain={y}
                           metadata={metadata} config={chartConfig} title="JVM Heap Memory"/>
            </div>
        );
    }
}