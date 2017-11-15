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
const chartConfig = {
    x: 'Time',
    charts: [{type: 'area', y: 'Total Classes Loaded', fill: '#058DC7', markRadius: 2},
        {type: 'area', y: 'Current Classes Loaded', fill: '#50B432', markRadius: 2},
        {type: 'area', y: 'Total Classes Unloaded', fill: '#f17b31', markRadius: 2}],
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
 * JVM Loading chart component.
 */
export default class JVMLoading extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            jvmClassLoadingLoadedTotal: this.props.data[0],
            jvmClassLoadingLoadedCurrent: this.props.data[1],
            jvmClassLoadingUnloadedTotal: this.props.data[2]
        }
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            jvmClassLoadingLoadedTotal: nextprops.data[0],
            jvmClassLoadingLoadedCurrent: nextprops.data[1],
            jvmClassLoadingUnloadedTotal: nextprops.data[2]
        });
    }

    render() {
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
        let data = DashboardUtils.getCombinedChartList(DashboardUtils.getCombinedChartList(this.state.jvmClassLoadingLoadedTotal,
                this.state.jvmClassLoadingLoadedCurrent), this.state.jvmClassLoadingUnloadedTotal);
        return (
            <div style={{paddingLeft: 10}}>
                <ChartCard data={data} metadata={metadata} config={chartConfig}
                           title="Class Loading"/>
            </div>

        );


    }
}