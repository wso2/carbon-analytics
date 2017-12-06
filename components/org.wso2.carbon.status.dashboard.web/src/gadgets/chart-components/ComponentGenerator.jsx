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
    VictoryLine,
    VictoryScatter,
    VictoryTooltip,
    VictoryArea,
    VictoryBar,
    VictoryPortal,
    VictoryLegend,
    VictoryContainer,
} from 'victory';
import { Range } from 'rc-slider';
import 'rc-slider/assets/index.css';

const DEFAULT_MARK_RADIUS = 4;
const DEFAULT_AREA_FILL_OPACITY = 0.1;

export function getLineOrAreaComponent(config, chartIndex, onClick) {
    const chartElement = config.charts[chartIndex].type === 'line' ?
        (<VictoryLine
            style={{
                data: {
                    strokeWidth: config.charts[chartIndex].style ?
                        config.charts[chartIndex].style.strokeWidth || null : null,
                },
            }}
            animate={
                config.animate ?
                {
                    onEnter: {
                        duration: 100,
                    },
                } : null
            }
        />) :
        (<VictoryArea
            style={{
                data: {
                    fillOpacity: config.charts[chartIndex].style ?
                        config.charts[chartIndex].style.fillOpacity || DEFAULT_AREA_FILL_OPACITY :
                        DEFAULT_AREA_FILL_OPACITY,
                },
            }}
            animate={
                config.animate ?
                {
                    onEnter: {
                        duration: 100,
                    },
                } : null
            }
        />);

    return ([
        chartElement,
        (<VictoryPortal>
            <VictoryScatter
                labels={
                    d => `${config.x}:${Number(d.x).toFixed(2)}\n
                ${config.charts[chartIndex].y}:${Number(d.y).toFixed(2)}`
                }
                labelComponent={
                    <VictoryTooltip
                        orientation='right'
                        pointerLength={4}
                        cornerRadius={2}
                        flyoutStyle={{ fill: '#000', fillOpacity: '0.8', strokeWidth: 0 }}
                        style={{ fill: '#b0b0b0' }}
                    />
                }
                padding={{ left: 100, top: 30, bottom: 50, right: 30 }}
                size={(
                    config.charts[chartIndex].style ?
                        config.charts[chartIndex].style.markRadius || DEFAULT_MARK_RADIUS : DEFAULT_MARK_RADIUS
                )}
                events={[{
                    target: 'data',
                    eventHandlers: {
                        onClick: () => {
                            return [
                                {
                                    target: 'data',
                                    mutation: onClick,
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
        </VictoryPortal>),
    ]);
}

export function getBarComponent(config, chartIndex, data, color, onClick) {
    return (

        <VictoryBar
            labels={d => `${config.x}:${d.x}\n${config.charts[chartIndex].y}:${d.y}`}
            labelComponent={
                <VictoryTooltip
                    orientation='top'
                    pointerLength={4}
                    cornerRadius={2}
                    flyoutStyle={{ fill: '#000', fillOpacity: '0.8', strokeWidth: 0 }}
                    style={{ fill: '#b0b0b0' }}
                />
            }
            data={data}
            color={color}
            events={[{
                target: 'data',
                eventHandlers: {
                    onClick: () => {
                        return [
                            {
                                target: 'data',
                                mutation: onClick,
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
    );
}

/**
 * Returns a Victory legend component for the given config.
 * @param {Object} config Chart configuration
 * @param {Array} legendItems Items to be included in the legend
 * @param {Array} ignoreArray Items to be ignored in the legend
 * @param {Function} interaction Function to handle interactions
 * @param {Number} height height of the chart
 * @param {Number} width width of the chart
 * @returns Victory Legend component to be included in the graph.
 */
export function getLegendComponent(config, legendItems, ignoreArray, interaction, height, width) {
    return (
        <div
            style={{
                width: !config.legendOrientation ? '15%' :
                    (() => {
                        if (config.legendOrientation === 'left' || config.legendOrientation === 'right') {
                            return '20%';
                        } else return '100%';
                    })(),
                display: !config.legendOrientation ? 'inline' :
                    (() => {
                        if (config.legendOrientation === 'left' || config.legendOrientation === 'right') {
                            return 'inline';
                        } else return null;
                    })(),
                float: !config.legendOrientation ? 'right' : (() => {
                    if (config.legendOrientation === 'left') return 'left';
                    else if (config.legendOrientation === 'right') return 'right';
                    else return null;
                })(),
            }}
        >
            <VictoryLegend
                containerComponent={<VictoryContainer responsive />}
                centerTitle
                height={(() => {
                    if (!config.legendOrientation) return height;
                    else if (config.legendOrientation === 'left' || config.legendOrientation === 'right') {
                        return height;
                    } else return 100;
                })()}
                width={(() => {
                    if (!config.legendOrientation) return 200;
                    else if (config.legendOrientation === 'left' || config.legendOrientation === 'right') return 200;
                    else return width;
                })()}
                orientation={
                    !config.legendOrientation ?
                        'vertical' :
                        (() => {
                            if (config.legendOrientation === 'left' || config.legendOrientation === 'right') {
                                return 'vertical';
                            } else {
                                return 'horizontal';
                            }
                        })()
                }
                title="Legend"
                style={{
                    title: { fontSize: 25, fill: config.style ? config.style.legendTitleColor : null },
                    labels: { fontSize: 20, fill: config.style ? config.style.legendTextColor : null },
                }}
                data={legendItems.length > 0 ? legendItems : [{
                    name: 'undefined',
                    symbol: { fill: '#333' },
                }]}
                itemsPerRow={config.legendOrientation === 'top' || config.legendOrientation === 'bottom' ? 5 : 4}
                events={[
                    {
                        target: 'data',
                        eventHandlers: {
                            onClick: config.interactiveLegend ? () => { // TODO: update doc with the attribute
                                return [
                                    {
                                        target: 'data',
                                        mutation: interaction,
                                    },
                                ];
                            } : null,
                        },
                    },
                ]}
            />
        </div>
    );
}

export function getBrushComponent(xScale, xRange, xDomain, reset, onChange) {
    return (
        <div style={{ width: '80%', height: 40, display: 'inline', float: 'left', right: 10 }}>
            <div style={{ width: '10%', display: 'inline', float: 'left', left: 20 }}>
                <button
                    onClick={() => {
                        reset(xRange);
                    }}
                >
                    Reset
                </button>
            </div>
            <div style={{ width: '90%', display: 'inline', float: 'right' }}>
                <Range
                    max={xScale === 'time' ? xRange[1].getDate() : xRange[1]}
                    min={xScale === 'time' ? xRange[0].getDate() : xRange[0]}
                    defaultValue={xScale === 'time' ?
                        [xRange[0].getDate(), xRange[1].getDate()] :
                        [xRange[0], xRange[1]]
                    }
                    value={xScale === 'time' ?
                        [xDomain[0].getDate(), xDomain[1].getDate()] :
                        xDomain}
                    onChange={(d) => {
                        onChange(d);
                    }}
                />
            </div>
        </div>
    );
}
