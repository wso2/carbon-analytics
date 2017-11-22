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
    VictoryChart,
    VictoryTheme,
    VictoryTooltip,
    VictoryContainer,
    VictoryVoronoiContainer,
    VictoryLegend,
    VictoryScatter,
    VictoryAxis,
    VictoryLabel
} from 'victory';
import PropTypes from 'prop-types';
import { formatPrefix, scaleLinear, timeFormat } from 'd3';
import { getDefaultColorScale } from './helper';


export default class ScatterCharts extends React.Component {
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
            legend: false,
            scatterPlotRange: []
        };

        this._handleAndSortData = this._handleAndSortData.bind(this);
        this._handleMouseEvent = this._handleMouseEvent.bind(this);
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
        let { dataSets, chartArray, initialized, xScale, orientation, legend, scatterPlotRange } = this.state;


        config.charts.map((chart, chartIndex) => {
            let xIndex = metadata.names.indexOf(chart.x);
            let yIndex = metadata.names.indexOf(chart.y);
            let colorIndex = metadata.names.indexOf(chart.color);
            let sizeIndex = metadata.names.indexOf(chart.size);
            xScale = metadata.types[xIndex] === 'time' ? 'time' : xScale;
            if (!initialized) {
                chartArray.push({
                    type: chart.type,
                    dataSetNames: {},
                    colorType: metadata.types[colorIndex],
                    colorScale: Array.isArray(chart.colorScale) ? chart.colorScale : getDefaultColorScale(),
                    colorIndex: 0
                });
            }


            if (metadata.types[colorIndex] === 'linear') {
                legend = false;
                data.map((datum) => {

                    dataSets['scatterChart' + chartIndex] = dataSets['scatterChart' + chartIndex] || [];
                    dataSets['scatterChart' + chartIndex].push({
                        x: datum[xIndex],
                        y: datum[yIndex],
                        color: datum[colorIndex],
                        amount: datum[sizeIndex]
                    });

                    if (dataSets['scatterChart' + chartIndex].length > chart.maxLength) {
                        // console.info('check');
                        dataSets['scatterChart' + chartIndex].shift();
                    }

                    // console.info(datum[sizeIndex]);

                    if (scatterPlotRange.length === 0) {
                        scatterPlotRange = [datum[colorIndex], datum[colorIndex]];
                    } else {
                        scatterPlotRange[0] = scatterPlotRange[0] > datum[colorIndex] ? datum[colorIndex] : scatterPlotRange[0];
                        scatterPlotRange[1] = scatterPlotRange[1] < datum[colorIndex] ? datum[colorIndex] : scatterPlotRange[1];
                    }


                    chartArray[chartIndex].dataSetNames['scatterChart' + chartIndex] = chartArray[chartIndex].dataSetNames['scatterChart' + chartIndex] || null;
                });
            } else {
                data.map((datum) => {
                    let dataSetName = 'scatterChart' + chartIndex;
                    if (chart.color) {
                        let colorIndex = metadata.names.indexOf(chart.color);
                        dataSetName = colorIndex > -1 ? datum[colorIndex] : dataSetName;
                    }

                    dataSets[dataSetName] = dataSets[dataSetName] || [];
                    // console.info(yIndex);
                    dataSets[dataSetName].push({ x: datum[xIndex], y: datum[yIndex], amount: datum[sizeIndex] });

                    // console.info(chart.maxLength);
                    if (dataSets[dataSetName].length > config.maxLength) {
                        // console.info('check');
                        dataSets[dataSetName].shift();
                    }

                    // console.info(chartArray[chartIndex].dataSetNames);
                    if (!chartArray[chartIndex].dataSetNames.hasOwnProperty(dataSetName)) {
                        if (chartArray[chartIndex].colorIndex >= chartArray[chartIndex].colorScale.length) {
                            chartArray[chartIndex].colorIndex = 0;
                        }

                        if (chart.colorDomain) {
                            let colorIn = chart.colorDomain.indexOf(dataSetName);
                            chartArray[chartIndex].dataSetNames[dataSetName] = colorIn >= 0 ? (colorIn < chartArray[chartIndex].colorScale.length ? chartArray[chartIndex].colorScale[colorIn] : chartArray[chartIndex].colorScale[chartArray[chartIndex].colorIndex++]) : chartArray[chartIndex].colorScale[chartArray[chartIndex].colorIndex++];
                        } else {
                            chartArray[chartIndex].dataSetNames[dataSetName] = chartArray[chartIndex].colorScale[chartArray[chartIndex].colorIndex++];
                        }

                        // chartArray[chartIndex].dataSetNames[dataSetName]=chart.fill || chartArray[chartIndex].dataSetNames[dataSetName];


                    }

                });
            }

        });

        initialized = true;

        this.setState({ dataSets, chartArray, initialized, xScale, orientation, legend, scatterPlotRange });

    }


    render() {

        let { config } = this.props;
        let { height, width, chartArray, dataSets, xScale, legend } = this.state;
        let chartComponents = [];
        let legendItems = [];


        chartArray.map((chart, chartIndex) => {
            if (chart.colorType === 'linear') {
                Object.keys(chart.dataSetNames).map((dataSetName) => {
                    chartComponents.push(
                        <VictoryScatter
                            bubbleProperty='amount'
                            maxBubbleSize={15}
                            minBubbleSize={5}
                            style={{
                                data: {
                                    fill: (d) => {
                                        // console.info(this.state.scatterPlotRange);
                                        // console.info(d3.scaleLinear().range(['#FFFFDD', '#3E9583', '#1F2D86']).domain(this.state.scatterPlotRange)(d.color));
                                        return scaleLinear().range([chart.colorScale[0], chart.colorScale[1]]).domain(this.state.scatterPlotRange)(d.color);
                                    }
                                }
                            }}
                            data={dataSets[dataSetName]}
                            labels={(d) => `${config.charts[chartIndex].x}:${d.x}\n
                                                   ${config.charts[chartIndex].y}:${d.y}\n
                                                   ${config.charts[chartIndex].size}:${d.amount}
                                                   ${config.charts[chartIndex].color}:${d.color}`}
                            labelComponent={
                                <VictoryTooltip
                                    orientation='bottom'
                                />
                            }
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

                        />
                    );
                });
            } else {
                Object.keys(chart.dataSetNames).map((dataSetName) => {
                    // legendItems.push({name: dataSetName, symbol: {fill: chart.dataSetNames[dataSetName]}});
                    chartComponents.push(
                        <VictoryScatter
                            bubbleProperty='amount'
                            maxBubbleSize={20}
                            minBubbleSize={5}
                            style={{ data: { fill: chart.dataSetNames[dataSetName] } }}
                            data={dataSets[dataSetName]}
                            labels={(d) => `${config.charts[chartIndex].x}:${Number(d.x).toFixed(2)}\n${config.charts[chartIndex].y}:${Number(d.y).toFixed(2)}\n${config.charts[chartIndex].size}:${Number(d.amount).toFixed}\n${config.charts[chartIndex].color}:${d.color}`}
                            labelComponent={
                                <VictoryTooltip
                                    orientation='bottom'
                                />
                            }
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

                        />
                    );
                });
            }
        });


        return (
            <div style={{ overflow: 'hidden' }}>
                <div style={{ float: 'left', width: legend ? '80%' : '100%', display: 'inline' }}>

                    <VictoryChart
                        width={width}
                        height={height}
                        theme={VictoryTheme.material}
                        container={<VictoryVoronoiContainer />}
                    >
                        <VictoryAxis crossAxis
                            style={{ axisLabel: { padding: 35 }, fill: config.axisLabelColor || '#455A64' }}
                            label={config.charts[0].x}
                            tickFormat={xScale === 'linear' ?
                                (text) => {
                                    if (text.toString().match(/[a-z]/i)) {
                                        if (text.length > 5) {
                                            return text.subString(0, 4) + '...';
                                        } else {
                                            return text;
                                        }
                                    } else {
                                        return formatPrefix(',.0', Number(text));
                                    }
                                } :
                                config.timeFormat ?
                                    (date) => {
                                        return timeFormat(config.timeFormat)(new Date(date));
                                    } : null}
                            standalone={false}
                            tickLabelComponent={
                                <VictoryLabel
                                    angle={config.xAxisTickAngle || 0}
                                    style={{ fill: config.tickLabelColor || 'black' }}
                                />
                            }


                        />
                        <VictoryAxis dependentAxis crossAxis
                            style={{ axisLabel: { padding: 35 }, fill: config.axisLabelColor || '#455A64' }}
                            label={config.charts.length > 1 ? '' : config.charts[0].y}
                            standalone={false}
                            tickFormat={text => formatPrefix(',.0', Number(text))}
                            tickLabelComponent={
                                <VictoryLabel
                                    angle={config.yAxisTickAngle || 0}
                                    style={{ fill: config.tickLabelColor || 'black' }}
                                />
                            }
                        />

                        {chartComponents}

                    </VictoryChart>


                </div>
                {
                    legend ?
                        <div style={{ width: '20%', display: 'inline', float: 'right' }}>
                            <VictoryLegend
                                containerComponent={<VictoryContainer responsive={true} />}
                                height={this.state.height}
                                width={300}
                                title="Legend"
                                style={{ title: { fontSize: 25, fill: config.legendTitleColor }, labels: { fontSize: 20, fill: config.legendTextColor } }}
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

ScatterCharts.propTypes = {
    data: PropTypes.array,
    config: PropTypes.object.isRequired,
    metadata: PropTypes.object.isRequired,
    width: PropTypes.number,
    height: PropTypes.number,
    onClick: PropTypes.func
};