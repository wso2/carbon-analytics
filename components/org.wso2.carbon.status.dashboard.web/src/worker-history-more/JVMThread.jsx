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

const threadMetadata = {names: ['Time', 'Live Threads', 'Daemon Threads'], types: ['time', 'linear', 'linear']};


/**
 * JVM Threads chart component.
 */
export default class JVMThread extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            count: this.props.data[0],
            daemonCount: this.props.data[1],
            tickCount: 20
        };
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            count: nextprops.data[0],
            daemonCount: nextprops.data[1],
            tickCount: nextprops.data[0].length>20 ? 20 : nextprops.data[0].length
        });
    }

    render() {
        const threadLineChartConfig = {
            x: 'Time',
            charts: [{type: 'area', y: 'Live Threads', fill: '#f17b31', style: {markRadius: 2}},
                {type: 'area', y: 'Daemon Threads',style: {markRadius: 2}}],
            width: 700,
            height: 200,
            style: {
                tickLabelColor:'#f2f2f2',
                legendTextColor: '#9c9898',
                legendTitleColor: '#9c9898',
                axisLabelColor: '#9c9898'
            },
            legend:true,
            interactiveLegend: true,
            gridColor: '#f2f2f2',
            xAxisTickCount:this.state.tickCount
        };
        if(this.state.count.length === 0 && this.state.daemonCount.length === 0){
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
        return (
            <div style={{paddingLeft: 10}}>
                <ChartCard data={DashboardUtils.getCombinedChartList(this.state.count, this.state.daemonCount)}
                           metadata={threadMetadata} config={threadLineChartConfig} title="Threads"/>
            </div>
        );
    }
}