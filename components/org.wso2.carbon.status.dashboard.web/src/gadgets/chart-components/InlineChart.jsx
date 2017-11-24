/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from 'react';
import BasicChart from './BasicChart.jsx';
import { VictoryLine, VictoryArea, VictoryGroup, VictoryBar, VictoryTooltip, VictoryStack } from 'victory';

export default class InlineChart extends BasicChart {

    constructor(props) {
        super(props);
        this.handleAndSortData = this.handleAndSortData.bind(this);
    }

    componentDidMount() {
        this.handleAndSortData(this.props);
        console.info(this.state);
    }

    componentWillReceiveProps(nextProps) {
        this.handleAndSortData(nextProps);
    }

    render() {
        const { config } = this.props;
        const { height, width, chartArray, dataSets } = this.state;
        let chartComponents = [];
        const legendItems = [];
        let horizontal = false;
        const lineCharts = [];
        let areaCharts = [];
        let barCharts = [];

        chartArray.map((chart, chartIndex) => {
            switch (chart.type) {
                case 'spark-line':
                    Object.keys(chart.dataSetNames).map((dataSetName) => {
                        legendItems.push({ name: dataSetName, symbol: { fill: chart.dataSetNames[dataSetName] } });
                        lineCharts.push((
                            <VictoryGroup
                                key={`chart-${chartIndex}-${chart.type}-${dataSetName}`}
                                data={dataSets[dataSetName]}
                                color={chart.dataSetNames[dataSetName]}
                                height={height}
                                width={width}
                                padding={0}
                                style={{ data: { strokeWidth: 0.5 } }}

                            >
                                <VictoryLine
                                    domain={{ y: this.props.yDomain || null }}
                                />

                            </VictoryGroup>
                        ));
                        return null;
                    });
                    break;
                case 'spark-area': {
                    const areaLocal = [];
                    Object.keys(chart.dataSetNames).map((dataSetName) => {
                        legendItems.push({ name: dataSetName, symbol: { fill: chart.dataSetNames[dataSetName] } });
                        areaLocal.push((
                            <VictoryGroup
                                key={`chart-${chartIndex}-${chart.type}-${dataSetName}`}
                                data={dataSets[dataSetName]}
                                color={chart.dataSetNames[dataSetName]}
                                style={{ data: { fillOpacity: config.charts[chartIndex].fillOpacity || 0.5, strokeWidth: 0.5 } }}
                                height={height}
                                width={width}
                                padding={0}
                            >
                                <VictoryArea
                                    domain={{ y: this.props.yDomain || null }}
                                />
                            </VictoryGroup>
                        ));
                        return null;
                    });

                    if (chart.mode === 'stacked') {
                        areaCharts.push((
                            <VictoryStack
                                height={height}
                                width={width}
                                padding={0}
                            >
                                {areaLocal}
                            </VictoryStack>
                        ));
                    } else {
                        areaCharts = areaCharts.concat(areaLocal);
                    }
                    break;
                }
                case 'spark-bar': {
                    const localBar = [];

                    horizontal = horizontal || chart.orientation === 'left';

                    Object.keys(chart.dataSetNames).map((dataSetName) => {
                        legendItems.push({ name: dataSetName, symbol: { fill: chart.dataSetNames[dataSetName] } });
                        localBar.push((
                            <VictoryBar
                                labels={d => `${config.x}:${d.x}\n${config.charts[chartIndex].y}:${d.y}`}
                                labelComponent={
                                    <VictoryTooltip
                                        orientation='bottom'
                                    />
                                }
                                data={dataSets[dataSetName]}
                                color={chart.dataSetNames[dataSetName]}
                                height={height}
                                width={width}
                                padding={0}
                            />
                        ));
                        return null;
                    });

                    if (chart.mode === 'stacked') {
                        barCharts.push((
                            <VictoryStack
                                height={height}
                                width={width}
                                padding={0}
                            >
                                {localBar}
                            </VictoryStack>
                        ));
                    } else {
                        barCharts = barCharts.concat(localBar);
                    }
                    break;
                }
                default:
                    console.error('unsupported chart type');
            }
            return null;
        });

        if (areaCharts.length > 0) chartComponents = chartComponents.concat(areaCharts);
        if (lineCharts.length > 0) chartComponents = chartComponents.concat(lineCharts);
        if (barCharts.length > 0) {
            const barWidth =
                ((horizontal ? height : width) / (config.maxLength * (barCharts.length > 1 ? barCharts.length : 2))) - 3;
            chartComponents.push((
                <VictoryGroup
                    horizontal={horizontal}
                    offset={barWidth}
                    style={{ data: { width: barWidth } }}
                    height={height}
                    width={width}
                    padding={5}
                >
                    {barCharts}
                </VictoryGroup>
            ));
        }

        return (
            <div>{chartComponents}</div>
        );
    }

}
