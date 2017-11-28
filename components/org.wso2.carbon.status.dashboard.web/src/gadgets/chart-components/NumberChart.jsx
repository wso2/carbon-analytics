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
import { VictoryLabel } from 'victory';

export default class NumberCharts extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            height:props.config.height || 450,
            width:props.config.width || 800,
            value:null,
            prevValue:null
        };
    }

    componentDidMount() {
        this._handleData(this.props);
    }

    componentWillReceiveProps(nextProps) {
        this._handleData(nextProps);
    }

    /**
     * handles data received by the props
     * @param props
     * @private
     */
    _handleData(props) {
        let { config,data,metadata } = props;
        let { prevValue,value } = this.state;
        let xIndex = metadata.names.indexOf(config.x);

        if (data.length > 0) {
            prevValue = value;
            value = data[data.length - 1][xIndex];
        }

        this.setState({ value,prevValue });
    }

    render() {
        let { config } = this.props;
        let { width,height,prevValue,value } = this.state;

        return (
            <svg height={'100%'} width={'100%'} viewBox={`0 0 ${width} ${height}`}>
                <VictoryLabel
                    textAnchor="middle"
                    verticalAnchor="middle"
                    x={width / 2}
                    y={height / 5}
                    text={config.title}
                    style={{ fontSize: 30 }}
                />
                <VictoryLabel
                    textAnchor="middle"
                    verticalAnchor="middle"
                    x={width / 2}
                    y={height / 2}
                    text={(value === null ? value : value.toFixed(3))}
                    style={{ fontSize: 45 }}
                />
                <VictoryLabel
                    textAnchor="middle"
                    verticalAnchor="middle"
                    x={width / 2}
                    y={(height / 2) + 50}
                    text={(Math.abs(eval(prevValue - value))).toFixed(3)}
                    style={{ fontSize: 15 }}
                />
                <VictoryLabel
                    textAnchor="middle"
                    verticalAnchor="middle"
                    x={width / 2}
                    y={(height / 2) + 70}
                    text={(Math.abs((100 * ((value - prevValue) / prevValue))).toFixed(3)) + '%'}
                    style={{ fontSize: 15 }}
                />
                <VictoryLabel
                    textAnchor="middle"
                    verticalAnchor="middle"
                    x={(width / 2) + 50}
                    y={((height / 2) + 55)}
                    text={prevValue < value ? '↑' : prevValue === value ? '' : '↓'}
                    style={{ fontSize: 45 }}
                />
            </svg>
        );
    }
}

NumberCharts.propTypes = {
    config:PropTypes.object.isRequired,
    metadata:PropTypes.object.isRequired,
    data:PropTypes.array
};
