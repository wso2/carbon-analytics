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
    VictoryArea,
    VictoryAxis,
    VictoryBar,
    VictoryChart,
    VictoryContainer,
    VictoryGroup,
    VictoryLabel,
    VictoryLegend,
    VictoryLine,
    VictoryPortal,
    VictoryScatter,
    VictoryStack,
    VictoryTheme,
    VictoryTooltip,
    VictoryVoronoiContainer,
} from 'victory';
import PropTypes from 'prop-types';
import { formatPrefix, timeFormat } from 'd3';
import { Range } from 'rc-slider';
import 'rc-slider/assets/index.css';
import { getDefaultColorScale } from './helper';

/**
 * React component required to render Bar, Line and Area Charts.
 */
export default class BasicCharts extends React.Component {

    constructor(props) {
        super(props);
        this.state = {

            dataBuffer: [],
            height: props.config.height || props.height || 450,
            width: props.config.width || props.width || 800,
            dataSets: {},
            chartArray: [],
            initialized: false,
            xScale: 'linear',
            orientation: 'bottom',
            xDomain: [null, null],
            ignoreArray: [],
            seriesXMaxVal: null,
            seriesXMinVal: null,
        };

        this.handleAndSortData = this.handleAndSortData.bind(this);
        this._handleMouseEvent = this._handleMouseEvent.bind(this);

        this.xRange = [];
        this.chartConfig = null;
    }

    componentDidMount() {
        this.chartConfig = this.props.config;
        this.handleAndSortData(this.props);
    }

    componentWillReceiveProps(nextProps) {
        if (JSON.stringify(this.chartConfig) !== JSON.stringify(nextProps.config)) {
            console.info('not similar');
            this.chartConfig = nextProps.config;
            this.state.chartArray = [];
            this.state.dataSets = [];
            this.state.initialized = false;
            console.info(this.state.chartArray);
        }

        this.handleAndSortData(nextProps);
    }

    componentWillUnmount() {
        this.setState({});
    }

    _handleMouseEvent(evt) {
        const { onClick } = this.props;

        return onClick && onClick(evt);
    }

    /**
     * Handles the sorting of data and populating the dataset.
     * @param {Object} props - Props object to be processed.
     */
    handleAndSortData(props) {
        const { config, metadata, data } = props;
        const { dataSets, chartArray } = this.state;
        let { initialized, xScale, orientation, xDomain, seriesXMaxVal, seriesXMinVal } = this.state;
        const xIndex = metadata.names.indexOf(config.x);
        let hasMaxLength = false;

        switch (metadata.types[xIndex]) {
            case 'linear':
                xScale = 'linear';
                break;
            case 'time':
                xScale = 'time';
                break;
            case 'ordinal':
                xScale = 'ordinal';
                break;
            default:
                console.error('unsupported data type on xAxis');
        }

        xScale = metadata.types[xIndex] === 'time' ? 'time' : xScale;
        config.charts.map((chart, chartIndex) => {
            orientation = orientation === 'left' ? orientation : (chart.orientation || 'bottom');

            const yIndex = metadata.names.indexOf(chart.y);
            if (!initialized) {
                chartArray.push({
                    type: chart.type,
                    dataSetNames: {},
                    mode: chart.mode,
                    orientation: chart.orientation,
                    colorScale: Array.isArray(chart.colorScale) ? chart.colorScale :
                        getDefaultColorScale(),
                    colorIndex: 0,
                    id: chartArray.length,
                });
            }

            data.map((datum) => {
                let dataSetName = metadata.names[yIndex];
                if (chart.color) {
                    const colorIndex = metadata.names.indexOf(chart.color);
                    dataSetName = colorIndex > -1 ? datum[colorIndex] : dataSetName;
                }

                dataSets[dataSetName] = dataSets[dataSetName] || [];

                dataSets[dataSetName].push({ x: datum[xIndex], y: datum[yIndex] });
                if (dataSets[dataSetName].length > config.maxLength) {
                    hasMaxLength = true;
                    dataSets[dataSetName].shift();
                }

                const max = Math.max.apply(null, dataSets[dataSetName].map(d => d.x));
                const min = Math.min.apply(null, dataSets[dataSetName].map(d => d.x));

                if (xScale === 'linear') {
                    if (xScale === 'linear' && xDomain[0] !== null) {
                        if (min > xDomain[0]) {
                            xDomain[0] = min;
                            this.xRange[0] = min;
                        }

                        if (max > xDomain[1]) {
                            xDomain[1] = max;
                            this.xRange[1] = max;
                        }
                    } else {
                        xDomain = [min, max];
                        this.xRange = [min, max];
                    }
                }

                if (xScale === 'time') {
                    if (xScale === 'time' && xDomain[0] !== null) {
                        if (min > xDomain[0]) {
                            xDomain[0] = new Date(min);
                            this.xRange[0] = new Date(min);
                        }

                        if (max > xDomain[1]) {
                            xDomain[1] = new Date(max);
                            this.xRange[1] = new Date(max);
                        }
                    } else {
                        xDomain = [new Date(min), new Date(max)];
                        this.xRange = [new Date(min), new Date(max)];
                    }
                }

                if (seriesXMaxVal === null) {
                    seriesXMaxVal = max;
                    seriesXMinVal = min;
                } else {
                    if (seriesXMaxVal < max) {
                        seriesXMaxVal = max;
                    }
                    if (seriesXMinVal < min) {
                        seriesXMinVal = min;
                    }
                }

                if (!Object.prototype.hasOwnProperty.call(chartArray[chartIndex].dataSetNames, dataSetName)) {
                    if (chartArray[chartIndex].colorIndex >= chartArray[chartIndex].colorScale.length) {
                        chartArray[chartIndex].colorIndex = 0;
                    }

                    if (chart.colorDomain) {
                        const colorIn = chart.colorDomain.indexOf(dataSetName);

                        if (colorIn >= 0) {
                            if (colorIn < chartArray[chartIndex].colorScale.length) {
                                chartArray[chartIndex]
                                    .dataSetNames[dataSetName] = chartArray[chartIndex].colorScale[colorIn];
                            } else {
                                chartArray[chartIndex]
                                    .dataSetNames[dataSetName] = chartArray[chartIndex]
                                    .colorScale[chartArray[chartIndex].colorIndex++];
                            }
                        } else {
                            chartArray[chartIndex]
                                .dataSetNames[dataSetName] = chartArray[chartIndex]
                                .colorScale[chartArray[chartIndex].colorIndex++];
                        }
                    } else {
                        chartArray[chartIndex].dataSetNames[dataSetName] =
                            chartArray[chartIndex].colorScale[chartArray[chartIndex].colorIndex++];
                    }

                    chartArray[chartIndex]
                        .dataSetNames[dataSetName] = chart.fill || chartArray[chartIndex].dataSetNames[dataSetName];
                }

                return null;
            });

            if (hasMaxLength && xScale === 'linear') {
                Object.keys(dataSets).map((dataSetName) => {
                    dataSets[dataSetName].map((d, k) => {
                        if (d.x < seriesXMinVal) {
                            dataSets[dataSetName].splice(k, 1);
                        }
                        return null;
                    });

                    return null;
                });
            }

            return null;
        });


        initialized = true;

        this.setState({ dataSets, chartArray, initialized, xScale, orientation, xDomain });
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
                                    <VictoryLine
                                        style={{ data: { strokeWidth: config.charts[chartIndex].strokeWidth || null } }}
                                    />
                                    <VictoryPortal>
                                        <VictoryScatter
                                            labels={
                                                d => `${config.x}:${Number(d.x).toFixed(2)}\n
                                                ${config.charts[chartIndex].y}:${Number(d.y).toFixed(2)}`
                                            }
                                            labelComponent={
                                                <VictoryTooltip
                                                    orientation='right'
                                                />
                                            }
                                            size={(d, a) => {
                                                return a ? 20 : (config.charts[chartIndex].markRadius || 4);
                                            }}
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
                                    </VictoryPortal>
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
                                    style={{ data: { fillOpacity: config.charts[chartIndex].fillOpacity || 0.5 } }}
                                >
                                    <VictoryArea />
                                    <VictoryPortal>
                                        <VictoryScatter
                                            labels={d => `${config.x}:${Number(d.x).toFixed(2)}\n
                                                          ${config.charts[chartIndex].y}:${Number(d.y).toFixed(2)}`}
                                            labelComponent={
                                                <VictoryTooltip
                                                    orientation='right'
                                                />
                                            }
                                            size={(d, a) => {
                                                return a ? 20 : config.charts[chartIndex].markRadius || 4;
                                            }}
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
                                    </VictoryPortal>
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
                                <VictoryBar
                                    labels={d => `${config.x}:${d.x}\n${config.charts[chartIndex].y}:${d.y}`}
                                    labelComponent={
                                        <VictoryTooltip
                                            orientation='right'
                                        />
                                    }
                                    data={dataSets[dataSetName]}
                                    color={chart.dataSetNames[dataSetName]}
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
                    console.error('Error in rendering unknown chart type');
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
                <div style={{ float: 'left', width: '80%', display: 'inline' }}>

                    <VictoryChart
                        width={width}
                        height={height}
                        theme={VictoryTheme.material}
                        container={<VictoryVoronoiContainer />}
                        padding={{ left: 100, top: 10, bottom: 50, right: 0 }}
                        scale={{ x: xScale === 'linear' ? 'linear' : 'time', y: 'linear' }}
                        domain={{
                            x: config.brush && this.state.xDomain[0] ? this.state.xDomain : null,
                            y: this.props.yDomain || null,
                        }}
                        style={{ parent: { overflow: 'visible' } }}
                    >
                        {chartComponents}
                        <VictoryAxis
                            crossAxis
                            style={{
                                axis: { stroke: config.axisColor },
                                axisLabel: { padding: 35, fill: config.axisLabelColor },
                                fill: config.axisLabelColor || '#455A64',
                            }}
                            gridComponent={config.disableVerticalGrid ? <g /> : <line />}
                            label={config.xAxisLabel || config.x}
                            tickFormat={(() => {
                                if (xScale === 'linear') {
                                    return (text) => {
                                        if (text.toString().match(/[a-z]/i)) {
                                            if (text.length > 6) {
                                                return text.substring(0, 4) + '...';
                                            } else {
                                                return text;
                                            }
                                        } else {
                                            return formatPrefix(',.2', Number(text));
                                        }
                                    };
                                } else if (config.timeFormat) {
                                    return (date) => {
                                        return timeFormat(config.timeFormat)(new Date(date));
                                    };
                                } else {
                                    return null;
                                }
                            })()}
                            standalone={false}
                            tickLabelComponent={
                                <VictoryLabel
                                    angle={config.xAxisTickAngle || 0}
                                    style={{ fill: config.tickLabelColor || 'black' }}
                                />
                            }
                        />
                        <VictoryAxis
                            dependentAxis
                            crossAxis
                            style={{
                                axisLabel: { padding: 35, fill: config.axisLabelColor },
                                fill: config.axisLabelColor || '#455A64',
                                axis: { stroke: config.axisColor },
                            }}
                            gridComponent={config.disableHorizontalGrid ? <g /> : <line />}
                            label={config.yAxisLabel || config.charts.length > 1 ? '' : config.charts[0].y}
                            standalone={false}
                            tickFormat={(text) => {
                                if (Number(text) < 999) {
                                    return text;
                                } else {
                                    return formatPrefix(',.2', Number(text));
                                }
                            }}
                            tickLabelComponent={
                                <VictoryLabel
                                    angle={config.yAxisTickAngle || 0}
                                    style={{ fill: config.tickLabelColor || 'black' }}
                                />
                            }
                        />
                    </VictoryChart>
                </div>
                <div style={{ width: '20%', display: 'inline', float: 'right' }}>
                    <VictoryLegend
                        containerComponent={<VictoryContainer responsive />}
                        height={this.state.height}
                        width={300}
                        title="Legend"
                        style={{
                            title: { fontSize: 25, fill: config.legendTitleColor },
                            labels: { fontSize: 20, fill: config.legendTextColor },
                        }}
                        data={legendItems.length > 0 ? legendItems : [{
                            name: 'undefined',
                            symbol: { fill: '#333' },
                        }]}
                        events={[
                            {
                                target: 'data',
                                eventHandlers: {
                                    onClick: config.interactiveLegend ? () => { // TODO: update doc with the attribute
                                        return [
                                            {
                                                target: 'data',
                                                mutation: (props) => {
                                                    console.info(props.index);

                                                    const ignoreIndex = ignoreArray
                                                        .map(d => d.name)
                                                        .indexOf(props.datum.name);
                                                    if (ignoreIndex > -1) {
                                                        ignoreArray.splice(ignoreIndex, 1);
                                                    } else {
                                                        ignoreArray.push({ name: props.datum.name });
                                                    }
                                                    console.info(ignoreArray);
                                                    this.setState({
                                                        ignoreArray,
                                                    });
                                                },
                                            }, {
                                                target: 'labels',
                                                mutation: (props) => {
                                                    const fill = props.style && props.style.fill;
                                                    return fill === 'grey' ? null : { style: { fill: 'grey' } };
                                                },
                                            },
                                        ];
                                    } : null,
                                },
                            },
                        ]}
                    />
                </div>
                {config.brush ?
                    <div
                        style={{ width: '80%', height: 40, display: 'inline', float: 'left', right: 10 }}
                    >
                        <div
                            style={{ width: '10%', display: 'inline', float: 'left', left: 20 }}
                        >
                            <button onClick={() => {
                                this.setState({ xDomain: this.xRange });
                            }}
                            >Reset
                            </button>
                        </div>
                        <div
                            style={{ width: '90%', display: 'inline', float: 'right' }}
                        >
                            <Range
                                max={xScale === 'time' ? this.xRange[1].getDate() : this.xRange[1]}
                                min={xScale === 'time' ? this.xRange[0].getDate() : this.xRange[0]}
                                defaultValue={xScale === 'time' ?
                                    [this.xRange[0].getDate(), this.xRange[1].getDate()] :
                                    [this.xRange[0], this.xRange[1]]
                                }
                                value={xScale === 'time' ?
                                    [this.state.xDomain[0].getDate(), this.state.xDomain[1].getDate()] :
                                    this.state.xDomain}
                                onChange={(d) => {
                                    this.setState({
                                        xDomain: d,
                                    });
                                }}
                            />
                        </div>
                    </div> : null
                }
            </div >

        );
    }
}

BasicCharts.defaultProps = {
    width: 800,
    height: 450,
    onClick: null,
};

BasicCharts.propTypes = {
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
};
