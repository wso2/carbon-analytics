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

const cpuMetadata = {names: ['Time', 'System Load Avg'], types: ['time', 'linear', 'linear']};

/**
 * JVM CPU Load chart component.
 */
export default class JVMLoad extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            jvmOsSystemLoadAverage: this.props.data[0],
            tickCount: 10
        };
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            jvmOsSystemLoadAverage: nextprops.data[0],
            tickCount: nextprops.data[0].length > 10 ? 10 : nextprops.data[0].length
        });
    }

    render() {
        const cpuLineChartConfig = {
            x: 'Time',
            charts: [
                {type: 'area', y: 'System Load Avg', style: {markRadius: 2}}
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
            tipTimeFormat: "%Y-%m-%d %H:%M:%S %Z",
            gridColor: '#f2f2f2',
            xAxisTickCount: this.state.tickCount
        };
        if (this.state.jvmOsSystemLoadAverage.length === 0) {
            return (
                <div style={{paddingLeft: 10}}>
                    <Card>
                        <CardHeader
                            title="JVM CPU Load"
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

        let y = DashboardUtils.getYDomain(this.state.jvmOsSystemLoadAverage);
        return (
            <div style={{paddingLeft: 10}}>
                <ChartCard data={this.state.jvmOsSystemLoadAverage} yDomain={y}
                           metadata={cpuMetadata} config={cpuLineChartConfig} title="System Load Average"/>
            </div>
        );
    }
}