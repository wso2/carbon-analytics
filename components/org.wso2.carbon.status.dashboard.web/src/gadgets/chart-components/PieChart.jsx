/**
 * Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


import React from 'react';
import {
    VictoryTooltip,
    VictoryContainer,
    VictoryLegend,
    VictoryPie,
    VictoryLabel
} from 'victory';
import PropTypes from 'prop-types';
import { getDefaultColorScale } from './helper';


export default class PieCharts extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            height: props.height || props.config.height || 450,
            width: props.width || props.config.width || 800,
            dataSets: {},
            chartArray: [],
            initialized: false,
            xScale: 'linear',
            orientation: 'bottom',
            legend: true,
            scatterPlotRange: [],
            randomUpdater: 0
        };

        this._handleAndSortData = this._handleAndSortData.bind(this);
        this._handleMouseEvent=this._handleMouseEvent.bind(this);
    }

    componentDidMount() {
        this._handleAndSortData(this.props);
    }

    componentWillReceiveProps(nextProps) {
        this._handleAndSortData(nextProps);
    }

    componentWillUnmount() {
        this.setState({});
    }

    /* *************************[Start] Event Handlers****************************/

    _handleMouseEvent(evt) {
        const { onClick } = this.props;

        return onClick && onClick(evt);
    }

    /* *************************[END] Event Handlers****************************/

    /**
     * Handles the sorting of data and populating the dataset
     * @param props
     * @private
     */
    _handleAndSortData(props) {
        let { config, metadata, data } = props;
        let { dataSets, chartArray, initialized, xScale, orientation, legend, scatterPlotRange, randomUpdater } = this.state;


        config.charts.map(() => {
            let arcConfig = config.charts[0];
            let xIndex = metadata.names.indexOf(arcConfig.x);
            let colorIndex = metadata.names.indexOf(arcConfig.color);
            if (!config.percentage) {

                if (!initialized) {
                    chartArray.push({
                        type: arcConfig.type,
                        dataSetNames: {},
                        mode: arcConfig.mode,
                        colorScale: Array.isArray(arcConfig.colorScale) ? arcConfig.colorScale : getDefaultColorScale(),
                        colorIndex: 0,

                    });
                }

                data.map((datum) => {
                    randomUpdater++;
                    let dataSetName = datum[colorIndex];

                    if (dataSets[dataSetName]) {
                        dataSets[dataSetName].y += datum[xIndex];

                    } else {
                        chartArray[0].colorIndex = chartArray[0].colorIndex >= chartArray[0].colorScale.length ? 0 : chartArray[0].colorIndex;
                        if (arcConfig.colorDomain) {
                            let colorDomIndex = arcConfig.colorDomain.indexOf(dataSetName);

                            if (colorDomIndex > -1 && colorDomIndex < chartArray[0].colorScale.length) {
                                dataSets[dataSetName] = {
                                    x: dataSetName,
                                    y: datum[xIndex],
                                    fill: chartArray[0].colorScale[colorDomIndex]
                                };
                                chartArray[0].dataSetNames[dataSetName] = chartArray[0].colorIndex++;
                            } else {
                                dataSets[dataSetName] = {
                                    x: dataSetName,
                                    y: datum[xIndex],
                                    fill: chartArray[0].colorScale[chartArray[0].colorIndex]
                                };
                            }

                        } else {
                            dataSets[dataSetName] = {
                                x: dataSetName,
                                y: datum[xIndex],
                                fill: chartArray[0].colorScale[chartArray[0].colorIndex]
                            };
                        }

                        if (!chartArray[0].dataSetNames[dataSetName]) {
                            chartArray[0].dataSetNames[dataSetName] = chartArray[0].colorScale[chartArray[0].colorIndex++];
                        }

                    }
                });
            } else {

                legend = false;
                if (!initialized) {
                    chartArray.push({
                        type: arcConfig.type,
                        colorScale: Array.isArray(arcConfig.colorScale) ? arcConfig.colorScale : getDefaultColorScale(),
                    });

                }

                data.map((datum) => {
                    dataSets = this._getPercentDataForPieChart(datum[xIndex]);
                });
            }

        });

        initialized = true;

        this.setState({
            dataSets,
            chartArray,
            initialized,
            xScale,
            orientation,
            legend,
            scatterPlotRange,
            randomUpdater
        });

    }


    /**
     * generates a data set for the given value
     * @param value for data set should be generated
     * @private
     */
    _getPercentDataForPieChart(value) {
        return [{ x: 'primary', y: value }, { x: 'secondary', y: 100 - value }];
    }


    render() {

        let { config } = this.props;
        let { height, width, chartArray, dataSets, xScale, legend, randomUpdater } = this.state;
        let chartComponents = [];
        let legendItems = [];


        chartArray.map((chart) => {
            let pieChartData = [];
            let total = 0;
            if (!config.percentage) {
                Object.keys(chart.dataSetNames).map((dataSetName) => {
                    // console.info(chart.dataSetNames[dataSetName]);
                    legendItems.push({ name: dataSetName, symbol: { fill: chart.dataSetNames[dataSetName] } });
                    total += dataSets[dataSetName].y;
                    pieChartData.push(dataSets[dataSetName]);
                });
            }

            chartComponents.push(
                <svg width='100%' height={'100%'} viewBox={`0 0 ${height > width ? width : height} ${height > width ? width : height}`}>
                    <VictoryPie
                        height={height > width ? width : height}
                        width={height > width ? width : height}
                        colorScale={chart.colorScale}
                        data={config.percentage ? dataSets : pieChartData}
                        labelComponent={config.percentage ? <VictoryLabel text={''} /> :
                            <VictoryTooltip width={50} height={25} />}
                        labels={config.percentage === 'percentage' ? '' : (d) => `${d.x} : ${((d.y / total) * 100).toFixed(2)}%`}
                        style={{ labels: { fontSize: 6 } }}
                        labelRadius={height / 4}
                        innerRadius={chart.mode === 'donut' || config.percentage ? (height > width ? width : height / 4) + (config.innerRadius || 0) : 0}
                        events={[{
                            target: 'data',
                            eventHandlers: {
                                onClick: () => {
                                    return [
                                        {
                                            target: 'data',
                                            mutation: this._handleMouseEvent
                                        }
                                    ];
                                }
                            }
                        }]}
                        randomUpdater={randomUpdater}
                    />
                    {
                        config.percentage ?
                            <VictoryLabel
                                textAnchor="middle" verticalAnchor="middle"
                                x={height / 2} y={width / 2}
                                text={`${Math.round(dataSets[0].y)}%`}
                                style={{ fontSize: 45, fill: config.labelColor || 'black' }}
                            /> : null
                    }
                </svg>
            );
        });

        console.info(legend);
        return (
            <div style={{ overflow: 'hidden' }}>
                <div style={{ float: 'left', width: legend ? '80%' : '100%', display: 'inline' }}>
                    {chartComponents}
                </div>
                {
                    legend ?
                        <div style={{ width: '20%', display: 'inline', float: 'right' }}>
                            <VictoryLegend
                                containerComponent={<VictoryContainer responsive={true} />}
                                height={this.state.height}
                                width={300}
                                title="Legend"
                                style={{ title: { fontSize: 25, fill: config.axisLabelColor }, labels: { fontSize: 20, fill: config.axisLabelColor } }}
                                data={legendItems.length > 0 ? legendItems : [{
                                    name: 'undefined',
                                    symbol: { fill: '#333' }
                                }]}
                            />
                        </div> :
                        null
                }

            </div>
        );
    }
}

PieCharts.propTypes = {
    data: PropTypes.array,
    config: PropTypes.object.isRequired,
    metadata: PropTypes.object.isRequired,
    width: PropTypes.number,
    height: PropTypes.number,
    onClick: PropTypes.func
};