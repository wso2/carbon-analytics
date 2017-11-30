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

const metadata = {names: ['Time', 'Non-Heap Init', 'Non-Heap Used','Non-Heap Committed','Non-Heap Max'],
    types: ['time', 'linear', 'linear', 'linear', 'linear']};
const chartConfig = {
    x: 'Time',
    charts: [{type: 'area', y: 'Non-Heap Init',fill: '#058DC7', markRadius: 2},
        {type: 'area', y: 'Non-Heap Used', fill: '#50B432', markRadius: 2},
        {type: 'area', y: 'Non-Heap Committed', fill: '#f17b31', markRadius: 2},
        {type: 'area', y: 'Non-Heap Max', fill: '#8c51a5', markRadius: 2}],
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
 * JVM Non-Heap Memory chart component.
 */
export default class NonHeapMemory extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            jvmMemoryNonHeapInit: this.props.data[0],
            jvmMemoryNonHeapUsed: this.props.data[1],
            jvmMemoryNonHeapCommitted: this.props.data[2],
            jvmMemoryNonHeapMax: this.props.data[3]
        };
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            jvmMemoryNonHeapInit: nextprops.data[0],
            jvmMemoryNonHeapUsed: nextprops.data[1],
            jvmMemoryNonHeapCommitted: nextprops.data[2],
            jvmMemoryNonHeapMax: nextprops.data[3]
        });
    }

    render() {
        if(this.state.jvmMemoryNonHeapInit.length === 0 && this.state.jvmMemoryNonHeapUsed.length === 0
            && this.state.jvmMemoryNonHeapCommitted.length === 0 && this.state.jvmMemoryNonHeapMax.length === 0){
            return(
                <div style={{paddingLeft: 10}}>
                    <Card>
                        <CardHeader
                            title="JVM Non-Heap Memory (MB)"
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
        let data1 = DashboardUtils.getCombinedChartList(this.state.jvmMemoryNonHeapInit,
            this.state.jvmMemoryNonHeapUsed);
        let data2 = DashboardUtils.getCombinedChartList(data1, this.state.jvmMemoryNonHeapCommitted);
        let data = DashboardUtils.getCombinedChartList(data2 , this.state.jvmMemoryNonHeapMax);
        return (
            <div style={{paddingLeft: 10}}>
                <ChartCard data={data}
                           metadata={metadata} config={chartConfig} title="JVM Non-Heap Memory (MB)"/>
            </div>
        );
    }
}