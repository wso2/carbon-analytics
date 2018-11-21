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

const memoryMetadata = {
    names: ['Time', 'Free Physical Memory', 'Total Physical Memory', 'Total Committed', 'Total Init',
        'Total Max', 'Total Used', 'Pool Size', 'Committed Virtual Memory'],
    types: ['time', 'linear', 'linear']
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
            virtualMemory: this.props.data[2],
            tickCount: 10
        };
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            freePhysicalMemory: nextprops.data[0],
            totalPhysicalMemory: nextprops.data[1],
            virtualMemory: nextprops.data[2],
            tickCount: nextprops.data[0].length > 10 ? 10 : nextprops.data[0].length
        });
    }

    render() {
        const memoryLineChartConfig = {
            x: 'Time',
            charts: [{type: 'area', y: 'Free Physical Memory', fill: '#058DC7', style: {markRadius: 2}},
                {type: 'area', y: 'Total Physical Memory', fill: '#50B432', style: {markRadius: 2}},
                {type: 'area', y: 'Committed Virtual Memory', fill: '#7119ff', style: {markRadius: 2}}
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
            legend: true,
            interactiveLegend: true,
            tipTimeFormat: "%Y-%m-%d %H:%M:%S %Z",
            gridColor: '#f2f2f2',
            xAxisTickCount: this.state.tickCount
        };
        if (this.state.freePhysicalMemory.length === 0 && this.state.totalPhysicalMemory.length === 0
            && this.state.virtualMemory.length === 0) {
            return (
                <div>
                    <Card>
                        <CardHeader
                            title="JVM Physical Memory (bytes)"
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
        let data1 = DashboardUtils.getCombinedChartList(this.state.freePhysicalMemory, this.state.totalPhysicalMemory);
        let intY = DashboardUtils.initCombinedYDomain(this.state.freePhysicalMemory, this.state.totalPhysicalMemory);
        let data = DashboardUtils.getCombinedChartList(data1, this.state.virtualMemory);
        let y = DashboardUtils.getCombinedYDomain(this.state.virtualMemory, intY);
        return (
            <div>
                <ChartCard data={data} metadata={memoryMetadata} config={memoryLineChartConfig} yDomain={y}
                           title="JVM Physical Memory (bytes)"/>
            </div>
        );
    }
}