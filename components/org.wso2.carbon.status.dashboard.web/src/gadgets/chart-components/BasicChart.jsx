/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
import React from 'react';
import PropTypes from 'prop-types';
import { VictoryGroup, VictoryStack } from 'victory';
import VizGError from './VizGError';
import { getDefaultColorScale } from './helper';
import ChartSkeleton from './ChartSkeleton.jsx';
import {
    getBarComponent,
    getBrushComponent,
    getLegendComponent,
    getLineOrAreaComponent,
} from './ComponentGenerator.jsx';

const LEGEND_DISABLED_COLOR = '#d3d3d3';

/**
 * Generate Line, Area or Bar Chart
 */
export default class BasicChart extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            dataSets: {},
            chartArray: [],
            height: props.config.height || props.height,
            width: props.config.width || props.width,
            initialized: false,
            xScale: 'linear',
            xDomain: [null, null],
            ignoreArray: [],
            seriesMaxXVal: null,
            seriesMinXVal: null,
            incrementor: 0,
        };
        this.xRange = [];
        this.chartConfig = null;

        this.visualizeData = this.visualizeData.bind(this);
        this.sortData = this.sortData.bind(this);
        this.generateChartArray = this.generateChartArray.bind(this);
        this.getXDomain = this.getXDomain.bind(this);
        this.getXRange = this.getXRange.bind(this);
        this.getDataSetDomain = this.getDataSetDomain.bind(this);
        this.maintainArrayLength = this.maintainArrayLength.bind(this);
        this._legendInteraction = this._legendInteraction.bind(this);
        this._brushOnChange = this._brushOnChange.bind(this);
        this._brushReset = this._brushReset.bind(this);
        this._handleMouseEvent = this._handleMouseEvent.bind(this);
    }

    componentDidMount() {
        this.chartConfig = this.props.config;
        this.visualizeData(this.props);
    }

    componentWillUnmount() {
        this.setState({});
    }

    /**
     * Define xDomain to be used when brushing data
     * @param xDomain current xDomain of the chart
     * @param domain array containing maximum and minimum of the current dataset
     */
    getXDomain(xDomain, domain) {
        if (xDomain[0] !== null) {
            if (domain[0] > xDomain[0]) {
                xDomain[0] = domain[0];
            }

            if (domain[1] > xDomain[1]) {
                xDomain[1] = domain[1];
            }
        } else {
            xDomain = [domain[0], domain[1]];
        }
        return xDomain;
    }

    /**
     * Returns an array containing maximum and minimum of the dataset
     * @param {Array} dataSet the dataSet array
     */
    getDataSetDomain(dataSet) {
        const max = Math.max.apply(null, dataSet.map(d => d.x));
        const min = Math.min.apply(null, dataSet.map(d => d.x));
        return [min, max];
    }

    /**
     * Sets the range of x values to be shown in the chart.
     * @param domain array containing the range of x values
     * @param seriesMaxXVal current maximum value of the xRange
     * @param seriesMinXVal current minimum value of the xRange
     */
    getXRange(domain, seriesMaxXVal, seriesMinXVal) {
        if (seriesMaxXVal === null) {
            seriesMaxXVal = domain[1];
            seriesMinXVal = domain[0];
        } else {
            if (seriesMaxXVal < domain[1]) {
                seriesMaxXVal = domain[1];
            }
            if (seriesMinXVal < domain[0]) {
                seriesMinXVal = domain[0];
            }
        }

        return { seriesMinXVal, seriesMaxXVal };
    }

    /**
     * Will trigger the set state that will visualize the data in chart.
     * @param {Object} props Props received by the element.
     */
    visualizeData(props) {
        const { config, metadata, data } = props;
        let { initialized, xScale, chartArray, dataSets, xDomain, seriesMaxXVal, seriesMinXVal } = this.state;

        if (!config.x) {
            throw new VizGError('BasicChart', "Independent axis 'x' is not defined in the Configuration JSON.");
        }
        const xIndex = metadata.names.indexOf(config.x);
        if (xIndex === -1) {
            throw new VizGError('BasicChart',
                `Defined independent axis ${config.x} is not found in the provided metadata`);
        }

        if (['linear', 'time', 'ordinal'].indexOf(metadata.types[xIndex].toLowerCase()) === -1) {
            throw new VizGError('BasicChart', 'Unknown metadata type is defined for x axis in the chart configuration');
        }
        if (!initialized) {
            chartArray = this.generateChartArray(config.charts);
            initialized = true;
        }

        const plotData = this.sortData(
            config.charts,
            metadata,
            data, xIndex, config.maxLength, chartArray, dataSets, xScale, xDomain, seriesMinXVal, seriesMaxXVal);

        plotData.initialized = initialized;
        plotData.xScale = metadata.types[xIndex].toLowerCase();

        this.setState(plotData);
    }

    /**
     * Sort data and update the state.
     * @param {Array} charts chart configurations provided
     * @param {Object} metadata metadata object provided by the user
     * @param {Array} data Dataset provided by the user.
     * @param {Number} xIndex index of the x data field in the dataset.
     * @param {maxLength} maxLength maximum length of a dataset should be.
     * @param chartArray
     * @param dataSets
     * @param xScale
     * @param xDomain
     * @param seriesMinXVal
     * @param seriesMaxXVal
     * @private
     */
    sortData(charts, metadata, data, xIndex, maxLength, chartArray, dataSets, xScale, xDomain, seriesMinXVal, seriesMaxXVal) {
        charts.forEach((chart, index) => {
            if (!chart.y) {
                throw new VizGError('BasicChart', "Dependent axis 'y' is not defined in the chart configuration");
            }
            const yIndex = metadata.names.indexOf(chart.y);
            if (yIndex === -1) {
                throw new VizGError('BasicChart', `Dependent axis '${chart.y}' is not found in the metadata provided`);
            }

            data.forEach((datum) => {
                let dataSetName = metadata.names[yIndex];

                if (chart.color) {
                    const colorIndex = metadata.names.indexOf(chart.color);
                    if (colorIndex > -1) {
                        dataSetName = datum[colorIndex];
                    } else {
                        throw new VizGError('BasicChart',
                            `Color category field '${chart.color}' is not found in the provided metadata`);
                    }
                }

                dataSets[dataSetName] = dataSets[dataSetName] || [];
                dataSets[dataSetName].push({ x: datum[xIndex], y: datum[yIndex] });
                if (maxLength) dataSets[dataSetName] = this.maintainArrayLength(dataSets[dataSetName], maxLength);
                if (xScale !== 'ordinal') {
                    this.xRange = xDomain = this.getXDomain(xDomain, this.getDataSetDomain(dataSets[dataSetName]));
                    const xRange =
                        this.getXRange(this.getDataSetDomain(dataSets[dataSetName]), seriesMaxXVal, seriesMinXVal);
                    seriesMinXVal = xRange.seriesMinXVal;
                    seriesMaxXVal = xRange.seriesMaxXVal;
                }

                if (!Object.prototype.hasOwnProperty.call(chartArray[index].dataSetNames, dataSetName)) {
                    if (chartArray[index].colorIndex >= chartArray[index].colorScale.length) {
                        chartArray[index].colorIndex = 0;
                    }

                    if (chart.colorDomain) {
                        const colorIn = chart.colorDomain.indexOf(dataSetName);

                        if (colorIn >= 0) {
                            if (colorIn < chartArray[index].colorScale.length) {
                                chartArray[index]
                                    .dataSetNames[dataSetName] = chartArray[index].colorScale[colorIn];
                            } else {
                                chartArray[index]
                                    .dataSetNames[dataSetName] = chartArray[index]
                                    .colorScale[chartArray[index].colorIndex++];
                            }
                        } else {
                            chartArray[index]
                                .dataSetNames[dataSetName] = chartArray[index]
                                .colorScale[chartArray[index].colorIndex++];
                        }
                    } else {
                        chartArray[index].dataSetNames[dataSetName] =
                            chartArray[index].colorScale[chartArray[index].colorIndex++];
                    }

                    chartArray[index]
                        .dataSetNames[dataSetName] = chart.fill || chartArray[index].dataSetNames[dataSetName];
                }
            });
        });

        return { chartArray, dataSets, xDomain, seriesMaxXVal, seriesMinXVal };
    }

    /**
     * Reduce the array length to the the given maximum array length
     * @param {Array} dataSet the dataSet that needs to be maintained by the length
     * @param {Number} maxLength maximum length the dataset shoul be
     */
    maintainArrayLength(dataSet, maxLength) {
        while (dataSet.length > maxLength) {
            dataSet.shift();
        }
        return dataSet;
    }

    /**
     * Generate the chart array that contains the information on visualization of the charts in config
     * @param charts chart configuration provided
     * @private
     */
    generateChartArray(charts) {
        return charts.map((chart, chartIndex) => {
            return {
                type: chart.type,
                dataSetNames: {},
                mode: chart.mode,
                orientation: chart.orientation,
                colorScale: Array.isArray(chart.colorScale) ? chart.colorScale : getDefaultColorScale(),
                colorIndex: 0,
                id: chartIndex,
            };
        });
    }

    /**
     * function to reset domain of the chart when zoomed in.
     * @param {Array} xDomain domain range of the x Axis.
     */
    _brushReset(xRange) {
        this.setState({ xDomain: xRange });
    }

    /**
     * Function to handle onChange in brush slider
     * @param {Array} xDomain New Domain of the x-axis
     */
    _brushOnChange(xDomain) {
        this.setState({ xDomain });
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

    _handleMouseEvent(evt) {
        const { onClick } = this.props;
        return onClick && onClick(evt);
    }

    render() {
        const { config } = this.props;
        const { height, width, chartArray, dataSets, xScale, ignoreArray } = this.state;
        let chartComponents = [];
        const legendItems = [];
        let horizontal = false;
        const lineCharts = [];
        let areaCharts = [];
        let barcharts = [];

        chartArray.map((chart, chartIndex) => {
            let addChart = false;
            switch (chart.type) {
                case 'line':
                    Object.keys(chart.dataSetNames).map((dataSetName) => {
                        legendItems.push({
                            name: dataSetName,
                            symbol: { fill: chart.dataSetNames[dataSetName] },
                            chartIndex,
                        });

                        addChart = ignoreArray
                            .filter(d => (d.name === dataSetName)).length > 0;
                        if (!addChart) {
                            lineCharts.push((
                                <VictoryGroup
                                    key={`chart-${chart.id}-${chart.type}-${dataSetName}`}
                                    data={dataSets[dataSetName]}
                                    color={chart.dataSetNames[dataSetName]}
                                >
                                    {getLineOrAreaComponent(config, chartIndex, this._handleMouseEvent)}
                                </VictoryGroup>
                            ));
                        }

                        return null;
                    });
                    break;
                case 'area': {
                    const areaLocal = [];

                    Object.keys(chart.dataSetNames).map((dataSetName) => {
                        legendItems.push({
                            name: dataSetName,
                            symbol: { fill: chart.dataSetNames[dataSetName] },
                            chartIndex,
                        });

                        addChart = ignoreArray
                            .filter(d => (d.name === dataSetName)).length > 0;

                        if (!addChart) {
                            areaLocal.push((
                                <VictoryGroup
                                    key={`chart-${chart.id}-${chart.type}-${dataSetName}`}
                                    data={dataSets[dataSetName]}
                                    color={chart.dataSetNames[dataSetName]}

                                >
                                    {getLineOrAreaComponent(config, chartIndex, this._handleMouseEvent)}
                                </VictoryGroup>
                            ));
                        }
                        return null;
                    });

                    if (chart.mode === 'stacked') {
                        areaCharts.push((
                            <VictoryStack>
                                {areaLocal}
                            </VictoryStack>
                        ));
                    } else {
                        areaCharts = areaCharts.concat(areaLocal);
                    }

                    break;
                }
                case 'bar': {
                    const localBar = [];

                    horizontal = horizontal || chart.orientation === 'left';

                    Object.keys(chart.dataSetNames).map((dataSetName) => {
                        legendItems.push({
                            name: dataSetName,
                            symbol: { fill: chart.dataSetNames[dataSetName] },
                            chartIndex,
                        });
                        addChart = ignoreArray
                            .filter(d => (d.name === dataSetName)).length > 0;
                        if (!addChart) {
                            localBar.push((
                                getBarComponent(config, chartIndex,
                                    dataSets[dataSetName], chart.dataSetNames[dataSetName])
                            ));
                        }

                        return null;
                    });

                    if (chart.mode === 'stacked') {
                        barcharts.push((
                            <VictoryStack>
                                {localBar}
                            </VictoryStack>
                        ));
                    } else {
                        barcharts = barcharts.concat(localBar);
                    }
                    break;
                }
                default:
                    throw new VizGError('BasicChart', 'Error in rendering unknown chart type');
            }

            return null;
        });

        if (areaCharts.length > 0) chartComponents = chartComponents.concat(areaCharts);
        if (lineCharts.length > 0) chartComponents = chartComponents.concat(lineCharts);
        if (barcharts.length > 0) {
            const barWidth =
                ((horizontal ?
                    height : width) / (config.maxLength * (barcharts.length > 1 ? barcharts.length : 2))) - 3;

            chartComponents.push((
                <VictoryGroup
                    horizontal={horizontal}
                    offset={barWidth}
                    style={{ data: { width: barWidth } }}
                >
                    {barcharts}
                </VictoryGroup>
            ));
        }

        return (
            <div style={{ overflow: 'hidden', zIndex: 99999 }}>
                <div
                    style={
                        config.legend ? {
                            width: !config.legendOrientation ? '80%' :
                                (() => {
                                    if (config.legendOrientation === 'left' || config.legendOrientation === 'right') {
                                        return '80%';
                                    } else return '100%';
                                })(),
                            display: !config.legendOrientation ? 'inline' :
                                (() => {
                                    if (config.legendOrientation === 'left' || config.legendOrientation === 'right') {
                                        return 'inline';
                                    } else return null;
                                })(),
                            float: !config.legendOrientation ? 'left' : (() => {
                                if (config.legendOrientation === 'left') return 'right';
                                else if (config.legendOrientation === 'right') return 'left';
                                else return null;
                            })(),
                        } : { width: '100%' }
                    }
                >
                    {
                        config.legend && config.legendOrientation && config.legendOrientation === 'top' ?
                            getLegendComponent(config, legendItems, ignoreArray, this._legendInteraction, height, width)
                            : null
                    }
                    <ChartSkeleton
                        width={width}
                        height={height}
                        config={config}
                        xScale={xScale}
                        yDomain={this.props.yDomain}
                        xDomain={this.state.xDomain}
                        xRange={this.xRange}
                        dataSets={dataSets}
                    >
                        {chartComponents}
                    </ChartSkeleton>
                </div>
                {
                    config.legend && (!config.legendOrientation || ['bottom', 'left', 'right'].indexOf(config.legendOrientation) > -1) ?
                        getLegendComponent(config, legendItems, ignoreArray, this._legendInteraction, height, width) :
                        null
                }
                {
                    config.brush ?
                        getBrushComponent(xScale, this.xRange, this.state.xDomain, this._brushReset, this._brushOnChange) :
                        null
                }
            </div>

        );
    }
}

BasicChart.defaultProps = {
    width: 800,
    height: 450,
    onClick: null,
    yDomain: null,
    append: false,
};

BasicChart.propTypes = {
    width: PropTypes.number,
    height: PropTypes.number,
    onClick: PropTypes.func,
    config: PropTypes.shape({
        x: PropTypes.string,
        charts: PropTypes.arrayOf(PropTypes.shape({
            type: PropTypes.string.isRequired,
            y: PropTypes.string.isRequired,
            fill: PropTypes.string,
            color: PropTypes.string,
            colorScale: PropTypes.arrayOf(PropTypes.string),
            colorDomain: PropTypes.arrayOf(PropTypes.string),
            mode: PropTypes.string,
        })),
        tickLabelColor: PropTypes.string,
        legendTitleColor: PropTypes.string,
        legendTextColor: PropTypes.string,
        axisColor: PropTypes.string,
        height: PropTypes.number,
        width: PropTypes.number,
        maxLength: PropTypes.number,
    }).isRequired,
    yDomain: PropTypes.arrayOf(PropTypes.number),
    append: PropTypes.bool,
};
