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
 */

import React from 'react';
import PropTypes from 'prop-types';
import { ComposableMap, Geographies, Geography } from 'react-simple-maps';
import { VictoryLegend, VictoryContainer } from 'victory';
import ReactToolTip from 'react-tooltip';
import * as d3 from 'd3';
import feature from 'topojson-client/src/feature';
import { getDefaultColorScale } from './helper';
import { CountryInfo, EuropeMap, WorldMap, USAMap } from './resources/MapData';

export default class MapGenerator extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            height: props.height || 450,
            width: props.width || 800,
            mapData: [],
            markerData: [],
            config: props.config,
            projectionConfig: {},
            mapType: props.config.charts[0].mapType || 'world',
            mapDataRange: [],
            colorType: 'linear',
            ordinalColorMap: {},
            colorIndex: 0,
            colorScale: [],
        };
        this._handleMouseEvent = this._handleMouseEvent.bind(this);
    }

    componentDidMount() {
        this._handleDataReceived(this.props);
    }

    componentWillReceiveProps(nextProps) {
        this._handleDataReceived(nextProps);
    }

    componentWillUnmount() {
        this.setState({});
    }

    _handleMouseEvent(evt) {
        const { onClick } = this.props;
        return onClick && onClick(evt);
    }

    /**
     * This function converts the country name into
     * Alpha - 3 code in case a whole country name is given
     *
     * @param countryName
     * @private
     */
    _convertCountryNamesToCode(countryName) {
        if (countryName.length === 3) {
            return countryName;
        } else {
            const countryName1 = CountryInfo.filter(x => x.name === countryName);
            if (countryName1.length > 0) {
                return countryName1[0]['alpha-3'];
            } else {
                return countryName;
            }
        }
    }


    _getLinearColor(value) {
        return d3.scaleLinear()
            .range([this.state.colorScale[0], this.state.colorScale[1]]).domain(this.state.mapDataRange)(value);
    }

    /**
     * handles the data received by the component to render the map
     * @param props - current props of the component
     * @private
     */
    _handleDataReceived(props) {
        const { metadata, data, config } = props;
        let {
            projectionConfig,
            mapType,
            mapDataRange,
            mapData,
            colorType,
            ordinalColorMap,
            colorIndex,
            colorScale } = this.state;
        const mapConfig = config.charts[0];
        const xIndex = metadata.names.indexOf(config.x);
        const yIndex = metadata.names.indexOf(mapConfig.y);
        colorScale = Array.isArray(mapConfig.colorScale) ? mapConfig.colorScale : getDefaultColorScale();
        mapType = mapConfig.mapType;
        switch (mapConfig.mapType) {
            case 'world':
                projectionConfig.scale = 120;
                break;
            case 'usa':
                projectionConfig.scale = 600;
                projectionConfig.yOffset = this.state.height / 1.2;
                projectionConfig.xOffset = this.state.width / 0.75;
                break;
            case 'europe':
                projectionConfig.scale = 400;
                projectionConfig.yOffset = this.state.height;
                break;
            default:
                console.error('unrecognized map type.');
        }
        colorType = metadata.types[yIndex];
        if (metadata.types[yIndex] === 'linear') {
            data.map((datum) => {
                if (mapDataRange.length === 0) {
                    mapDataRange = [datum[yIndex], datum[yIndex]];
                }

                if (mapDataRange[0] > datum[yIndex]) {
                    mapDataRange[0] = datum[yIndex];
                }

                if (mapDataRange[1] < datum[yIndex]) {
                    mapDataRange[1] = datum[yIndex];
                }

                const dataIndex = mapData.findIndex(obj => obj.x === this._convertCountryNamesToCode(datum[xIndex]));
                if (dataIndex >= 0) {
                    mapData[dataIndex].y = datum[yIndex];
                } else {
                    mapData.push({
                        givenName: datum[xIndex],
                        x: mapType === 'usa' ? datum[xIndex] : this._convertCountryNamesToCode(datum[xIndex]),
                        y: datum[yIndex],
                    });
                }
            });
        } else {
            data.map((datum) => {
                if (!ordinalColorMap.hasOwnProperty(datum[yIndex])) {
                    if (colorIndex >= colorScale.length) {
                        colorIndex = 0;
                    }
                    ordinalColorMap[datum[yIndex]] = colorScale[colorIndex++];
                }

                mapData.push({
                    givenName: datum[xIndex],
                    x: mapType === 'usa' ? datum[xIndex] : this._convertCountryNamesToCode(datum[xIndex]),
                    y: datum[yIndex],
                });
            });
        }

        this.setState({
            projectionConfig,
            mapType,
            mapData,
            mapDataRange,
            colorType,
            ordinalColorMap,
            colorIndex,
            colorScale,
        });
    }

    render() {
        const { config } = this.props;
        const { mapType, mapData, mapDataRange, colorType, ordinalColorMap } = this.state;
        let mapFeatureData = null;
        switch (mapType) {
            case 'world':
                mapFeatureData = WorldMap;
                break;
            case 'usa':
                mapFeatureData = USAMap;
                break;
            case 'europe':
                mapFeatureData = EuropeMap;
                break;
        }

        return (
            <div style={{ overflow: 'hidden', zIndex: 9999 }}>
                <div
                    style={{
                        float: 'left',
                        width: '85%',
                        display: 'inline',
                    }}
                >
                    <ComposableMap
                        projection={'mercator'}
                        projectionConfig={this.state.projectionConfig}
                        width={this.state.width}
                        heght={this.state.height}
                        style={{
                            width: '100%',
                            height: '100%',
                        }}
                    >
                        <Geographies
                            geographyPaths={feature(mapFeatureData, mapFeatureData.objects[Object.keys(mapFeatureData.objects)[0]]).features}
                            disableOptimization
                        >
                            {
                                (geographies, projection) => {
                                    return geographies.map((geography, i) => {
                                        let dataTip = '';
                                        let toolTip = null;

                                        if (mapType === 'usa') {
                                            dataTip = mapData.filter(x => x.x === geography.properties.name);
                                        } else {
                                            dataTip = mapData.filter(x => x.x === geography.id);
                                        }

                                        if (dataTip.length > 0) {
                                            toolTip = '' + config.x + ' : ' + dataTip[0].givenName + ', ' + config.charts[0].y + ' : ' + dataTip[0].y;
                                        }

                                        return (
                                            <Geography
                                                key={i}
                                                data-tip={toolTip ? toolTip.toString() : ''}
                                                geography={geography}
                                                projection={projection}
                                                style={{
                                                    default: {
                                                        fill: dataTip.length > 0 ?
                                                            (colorType === 'linear' ?
                                                                this._getLinearColor(dataTip[0].y) :
                                                                ordinalColorMap[dataTip[0].y]) : '#ddd',
                                                        stroke: '#fff',
                                                        strokeWidth: 0.5,
                                                        outline: 'none',
                                                    },
                                                    hover: {
                                                        fill: dataTip.length > 0 ?
                                                            (colorType === 'linear' ?
                                                                this._getLinearColor(dataTip[0].y) :
                                                                ordinalColorMap[dataTip[0].y]) : '#ddd',
                                                        stroke: '#fff',
                                                        opacity: 0.8,
                                                        strokeWidth: 0.5,
                                                        outline: 'none',

                                                    },
                                                    pressed: {
                                                        fill: '#3a79ff',
                                                        outline: 'none',
                                                    },
                                                }}
                                                onClick={this._handleMouseEvent}

                                            />);
                                    });
                                }

                            }
                        </Geographies>
                    </ComposableMap>
                    <ReactToolTip />
                </div>

                <div style={{ width: '15%', height: 'auto', display: 'inline', float: 'right' }}>
                    {
                        colorType === 'linear' ?
                            <svg width={'100%'} height={'100%'}>
                                <defs>
                                    <linearGradient id="grad1" x1="0%" y1="100%" x2="0%" y2="0%">
                                        <stop offset={'0%'} stopColor={this.state.colorScale[0]} stopOpacity={1} />

                                        <stop offset={'100%'} stopColor={this.state.colorScale[1]} stopOpacity={1} />
                                    </linearGradient>
                                </defs>
                                <g className='legend'>

                                    <text x={20} y={20}>{config.charts[0].y}</text>
                                    <text x={37} y={37}>{this.state.mapDataRange[1]}</text>
                                    <text x={37} y={132}>{this.state.mapDataRange[0]}</text>
                                    <rect x={20} y={30} fill='url(#grad1)' height={100} width={15} />
                                </g>
                            </svg>
                            : <VictoryLegend
                                containerComponent={<VictoryContainer responsive />}
                                height={this.state.height}
                                width={300}
                                title="Legend"
                                style={{ title: { fontSize: 25, fill: config.axisLabelColor }, labels: { fontSize: 20, fill: config.axisLabelColor } }}
                                data={Object.keys(ordinalColorMap).map((name) => {
                                    return { name, symbol: { fill: ordinalColorMap[name] } };
                                })}
                            />
                    }
                </div>
            </div>
        );
    }
}

MapGenerator.propTypes = {
    height: PropTypes.number,
    width: PropTypes.number,
    config: PropTypes.object.isRequired,
    mapData: PropTypes.array,
    metadata: PropTypes.object,
    colorRange: PropTypes.array,
    colorScale: PropTypes.array,
    colorType: PropTypes.string,
    onClick: PropTypes.func,
};
