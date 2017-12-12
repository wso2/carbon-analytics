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

const metadata = {
    names: ['Time', 'Total Classes Loaded', 'Current Classes Loaded', 'Total Classes Unloaded'],
    types: ['time', 'linear', 'linear', 'linear']
};

/**
 * JVM Loading chart component.
 */
export default class JVMLoading extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            jvmClassLoadingLoadedTotal: this.props.data[0],
            jvmClassLoadingLoadedCurrent: this.props.data[1],
            jvmClassLoadingUnloadedTotal: this.props.data[2],
            tickCount: 20
        }
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            jvmClassLoadingLoadedTotal: nextprops.data[0],
            jvmClassLoadingLoadedCurrent: nextprops.data[1],
            jvmClassLoadingUnloadedTotal: nextprops.data[2],
            tickCount: nextprops.data[0].length>20 ? 20 : nextprops.data[0].length
        });
    }

    render() {
        const chartConfig = {
            x: 'Time',
            charts: [{type: 'area', y: 'Total Classes Loaded', fill: '#058DC7', style: {markRadius: 2}},
                {type: 'area', y: 'Current Classes Loaded', fill: '#50B432', style: {markRadius: 2}},
                {type: 'area', y: 'Total Classes Unloaded', fill: '#f17b31', style: {markRadius: 2}}],
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
        if(this.state.jvmClassLoadingLoadedTotal.length === 0 && this.state.jvmClassLoadingLoadedCurrent.length === 0
            && this.state.jvmClassLoadingUnloadedTotal.length === 0){
            return(
                <div style={{paddingLeft: 10}}>
                    <Card>
                        <CardHeader
                            title="Class Loading"
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
        let data = DashboardUtils.getCombinedChartList(
            DashboardUtils.getCombinedChartList(this.state.jvmClassLoadingLoadedTotal,
                this.state.jvmClassLoadingLoadedCurrent), this.state.jvmClassLoadingUnloadedTotal);
        let intY= DashboardUtils.initCombinedYDomain(this.state.jvmClassLoadingLoadedTotal,
            this.state.jvmClassLoadingLoadedCurrent);
        let y2 = DashboardUtils.getCombinedYDomain(this.state.jvmClassLoadingUnloadedTotal,intY);
        return (
            <div style={{paddingLeft: 10}}>
                <ChartCard data={data} metadata={metadata} config={chartConfig} yDomain={y2}
                           title="Class Loading"/>
            </div>

        );


    }
}