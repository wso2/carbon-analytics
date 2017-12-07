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
import {
    VictoryTooltip,
    VictoryScatter,
} from 'victory';
import PropTypes from 'prop-types';
import { scaleLinear } from 'd3';
import { getDefaultColorScale } from './helper';
import VizGError from './VizGError';
import ChartSkeleton from './ChartSkeleton.jsx';
import { getLegendComponent } from './ComponentGenerator.jsx';

const LEGEND_DISABLED_COLOR = '#d3d3d3';

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
            scatterPlotRange: [],
            ignoreArray: [],
        };

        this._handleAndSortData = this._handleAndSortData.bind(this);
        this._handleMouseEvent = this._handleMouseEvent.bind(this);
        this._legendInteraction = this._legendInteraction.bind(this);
    }

    componentDidMount() {
        this._handleAndSortData(this.props);
    }

    componentWillReceiveProps(nextProps) {
        if (!this.props.append) {
            this.state.dataSets = {};
            this.state.chartArray = [];
            this.state.initialized = false;
        }
        this._handleAndSortData(nextProps);
    }

    componentWillUnmount() {
        this.setState({});
    }

    _handleMouseEvent(evt) {
        const { onClick } = this.props;
        return onClick && onClick(evt);
    }

    /**
     * Handles the sorting of data and populating the dataset
     * @param props
     */
    _handleAndSortData(props) {
        const { config, metadata, data } = props;
        let { dataSets, chartArray, initialized, xScale, orientation, legend, scatterPlotRange } = this.state;

        config.charts.map((chart, chartIndex) => {
            if (!chart.x) throw new VizGError('ScatterChart', "Field 'x' is not defined in the Scatter Plot config");
            if (!chart.y) throw new VizGError('ScatterChart', "Field 'y' is not defined in the Scatter Plot config");
            const xIndex = metadata.names.indexOf(chart.x);
            const yIndex = metadata.names.indexOf(chart.y);
            let colorIndex = metadata.names.indexOf(chart.color);
            const sizeIndex = metadata.names.indexOf(chart.size);
            xScale = metadata.types[xIndex] === 'time' ? 'time' : xScale;

            if (xIndex === -1) {
                throw new VizGError('ScatterChart', "Unknown 'x' field defined in the Scatter Plot config.");
            }

            if (yIndex === -1) {
                throw new VizGError('ScatterChart', "Unknown 'y' field defined in the Scatter Plot config.");
            }

            if (!initialized) {
                chartArray.push({
                    type: chart.type,
                    dataSetNames: {},
                    colorType: metadata.types[colorIndex],
                    colorScale: Array.isArray(chart.colorScale) ? chart.colorScale : getDefaultColorScale(),
                    colorIndex: 0,
                });
            }

            if (metadata.types[colorIndex] === 'linear') {
                legend = false;
                data.forEach((datum) => {
                    dataSets['scatterChart' + chartIndex] = dataSets['scatterChart' + chartIndex] || [];
                    dataSets['scatterChart' + chartIndex].push({
                        x: datum[xIndex],
                        y: datum[yIndex],
                        color: datum[colorIndex],
                        amount: datum[sizeIndex],
                    });

                    if (dataSets['scatterChart' + chartIndex].length > chart.maxLength) {
                        dataSets['scatterChart' + chartIndex].shift();
                    }

                    if (scatterPlotRange.length === 0) {
                        scatterPlotRange = [datum[colorIndex], datum[colorIndex]];
                    } else {
                        scatterPlotRange[0] = scatterPlotRange[0] > datum[colorIndex] ?
                            datum[colorIndex] : scatterPlotRange[0];
                        scatterPlotRange[1] = scatterPlotRange[1] < datum[colorIndex] ?
                            datum[colorIndex] : scatterPlotRange[1];
                    }
                    chartArray[chartIndex].dataSetNames['scatterChart' + chartIndex] =
                        chartArray[chartIndex].dataSetNames['scatterChart' + chartIndex] || null;
                });
            } else {
                data.map((datum) => {
                    let dataSetName = 'scatterChart' + chartIndex;
                    if (chart.color) {
                        colorIndex = metadata.names.indexOf(chart.color);
                        dataSetName = colorIndex > -1 ? datum[colorIndex] : dataSetName;
                    }

                    dataSets[dataSetName] = dataSets[dataSetName] || [];
                    dataSets[dataSetName].push({ x: datum[xIndex], y: datum[yIndex], amount: datum[sizeIndex] });
                    if (dataSets[dataSetName].length > config.maxLength) {
                        dataSets[dataSetName].shift();
                    }

                    if (!chartArray[chartIndex].dataSetNames.hasOwnProperty(dataSetName)) {
                        if (chartArray[chartIndex].colorIndex >= chartArray[chartIndex].colorScale.length) {
                            chartArray[chartIndex].colorIndex = 0;
                        }

                        if (chart.colorDomain) {
                            const colorIn = chart.colorDomain.indexOf(dataSetName);
                            chartArray[chartIndex].dataSetNames[dataSetName] = colorIn >= 0 ?
                                (colorIn < chartArray[chartIndex].colorScale.length ?
                                    chartArray[chartIndex].colorScale[colorIn] :
                                    chartArray[chartIndex].colorScale[chartArray[chartIndex].colorIndex++]) :
                                chartArray[chartIndex].colorScale[chartArray[chartIndex].colorIndex++];
                        } else {
                            chartArray[chartIndex]
                                .dataSetNames[dataSetName] = chartArray[chartIndex]
                                .colorScale[chartArray[chartIndex].colorIndex++];
                        }
                    }
                });
            }
        });
        initialized = true;
        this.setState({ dataSets, chartArray, initialized, xScale, orientation, legend, scatterPlotRange });
    }

    /**
     * Function used to disable a chart component when clicked on it's name in the legend.
     * @param {Object} props parameters recieved from the legend component
     */
    _legendInteraction(props) {
        const { ignoreArray } = this.state;
        const ignoreIndex = ignoreArray
            .map(d => d.name)
            .indexOf(props.datum.name);
        if (ignoreIndex > -1) {
            ignoreArray.splice(ignoreIndex, 1);
        } else {
            ignoreArray.push({ name: props.datum.name });
        }
        this.setState({
            ignoreArray,
        });
        const fill = props.style ? props.style.fill : null;
        return fill === LEGEND_DISABLED_COLOR ?
            null :
            { style: { fill: LEGEND_DISABLED_COLOR } };
    }

    render() {
        const { config } = this.props;
        const { height, width, chartArray, dataSets, xScale, legend, ignoreArray } = this.state;
        const chartComponents = [];
        const legendItems = [];

        chartArray.map((chart, chartIndex) => {
            if (chart.colorType === 'linear') {
                Object.keys(chart.dataSetNames).forEach((dataSetName) => {
                    chartComponents.push((
                        <VictoryScatter
                            bubbleProperty='amount'
                            maxBubbleSize={15}
                            minBubbleSize={5}
                            style={{
                                data: {
                                    fill: (d) => {
                                        return scaleLinear()
                                            .range([chart.colorScale[0], chart.colorScale[1]])
                                            .domain(this.state.scatterPlotRange)(d.color);
                                    },
                                },
                            }}
                            data={dataSets[dataSetName]}
                            labels={d => `${config.charts[chartIndex].x} : ${d.x}\n
                                                   ${config.charts[chartIndex].y} : ${d.y}\n
                                                   ${config.charts[chartIndex].size} : ${d.amount}
                                                   ${config.charts[chartIndex].color} : ${d.color}`}
                            labelComponent={
                                <VictoryTooltip
                                    orientation='top'
                                    pointerLength={4}
                                    cornerRadius={2}
                                    flyoutStyle={{ fill: '#000', fillOpacity: '0.8', strokeWidth: 0 }}
                                    style={{ fill: '#b0b0b0', textAlign: 'left' }}
                                />
                            }
                            events={[{
                                target: 'data',
                                eventHandlers: {
                                    onClick: () => {
                                        return [
                                            {
                                                target: 'data',
                                                mutation: this._handleMouseEvent,
                                            },
                                        ];
                                    },
                                },
                            }]}
                            animate={
                                config.animate ?
                                {
                                    onEnter: {
                                        duration: 100,
                                    },
                                } : null
                            }
                        />
                    ));
                });
            } else {
                Object.keys(chart.dataSetNames).forEach((dataSetName) => {
                    chartComponents.push((
                        <VictoryScatter
                            bubbleProperty='amount'
                            maxBubbleSize={20}
                            minBubbleSize={5}
                            style={{ data: { fill: chart.dataSetNames[dataSetName] } }}
                            data={dataSets[dataSetName]}
                            labels={
                                d => `${config.charts[chartIndex].x}:${Number(d.x).toFixed(2)}\n
                                ${config.charts[chartIndex].y}:${Number(d.y).toFixed(2)}\n
                                ${config.charts[chartIndex].size}:${Number(d.amount).toFixed}\n
                                ${config.charts[chartIndex].color}:${d.color}`}
                            labelComponent={
                                <VictoryTooltip
                                    orientation='top'
                                    pointerLength={4}
                                    cornerRadius={2}
                                    flyoutStyle={{ fill: '#000', fillOpacity: '0.8', strokeWidth: 0 }}
                                    style={{ fill: '#b0b0b0' }}
                                />
                            }
                            events={[{
                                target: 'data',
                                eventHandlers: {
                                    onClick: () => {
                                        return [
                                            {
                                                target: 'data',
                                                mutation: this._handleMouseEvent,
                                            },
                                        ];
                                    },
                                },
                            }]}
                        />
                    ));
                });
            }
        });

        return (
            <div style={{ overflow: 'hidden' }}>
                <div
                    style={
                        config.legend && legend ?
                        {
                            width: !config.legendOrientation ? '80%' :
                                    (() => {
                                        if (config.legendOrientation === 'left' ||
                                            config.legendOrientation === 'right') {
                                            return '80%';
                                        } else return '100%';
                                    })(),
                            display: !config.legendOrientation ? 'inline' :
                                    (() => {
                                        if (config.legendOrientation === 'left' ||
                                            config.legendOrientation === 'right') {
                                            return 'inline';
                                        } else return null;
                                    })(),
                            float: !config.legendOrientation ? 'right' : (() => {
                                if (config.legendOrientation === 'left') return 'right';
                                else if (config.legendOrientation === 'right') return 'left';
                                else return null;
                            })(),
                        } : null
                    }
                >
                    {
                        config.legend && legend && (config.legendOrientation && config.legendOrientation === 'top') ?
                            getLegendComponent(config, legendItems, ignoreArray, this._legendInteraction, height, width)
                            : null
                    }
                    <ChartSkeleton width={width} height={height} xScale={xScale} config={config}>
                        {chartComponents}
                    </ChartSkeleton>
                    {
                        config.legend && legend && (!config.legendOrientation || config.legendOrientation !== 'top') ?
                            getLegendComponent(config, legendItems, ignoreArray, this._legendInteraction, height, width)
                            : null
                    }
                </div>
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
    onClick: PropTypes.func,
};
