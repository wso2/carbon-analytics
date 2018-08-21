/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { Button, IconButton } from 'material-ui';
import HighGranularityMode from '@material-ui/icons/KeyboardArrowRight';
import LowGranularityMode from '@material-ui/icons/KeyboardArrowLeft';

export default class GranularityModeSelector extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            granularityMode: 'high',
            granularityModeValue: 'none',
        };


        this.highGranularityOptions = [
            {
                label: '1 Day',
                value: '1 Day',
            }, {
                label: '7 Days',
                value: '7 Days',
            }, {
                label: '1 Month',
                value: '1 Month',
            }, {
                label: '3 Months',
                value: '3 Months',
            }, {
                label: '6 Months',
                value: '6 Months',
            }, {
                label: '1 Year',
                value: '1 Year',
            },
        ];

        this.lowGranularityOptions = [
            {
                label: '1 Min',
                value: '1 Min',
            }, {
                label: '15 Min',
                value: '15 Min',
            }, {
                label: '1 Hour',
                value: '1 Hour',
            }, {
                label: '1 Day',
                value: '1 Day',
            }
        ];

        this.generateTabs = this.generateTabs.bind(this);
        this.switchGranularity = this.switchGranularity.bind(this);
        this.onGranularityModeChange = this.onGranularityModeChange.bind(this);
    }

    render() {
        let { granularityMode } = this.state;
        return (
            <div>
                <div>
                    Last :
                    {this.generateTabs(granularityMode)}
                    <IconButton aria-label="Delete" style={{ marginRight: 5 }} onClick={this.switchGranularity}>
                        {
                            granularityMode === 'low' ?
                                <HighGranularityMode />:
                                <LowGranularityMode />
                        }
                    </IconButton>
                    <Button onClick={() => this.onGranularityModeChange('custom')}>Custom</Button>
                </div>
            </div>
        );
    }

    switchGranularity() {
        this.setState({ granularityMode : this.state.granularityMode==='low' ? 'high' : 'low' });
    }

    generateTabs(granularityMode) {
        let options = granularityMode==='high' ? this.highGranularityOptions : this.lowGranularityOptions;
        return  options.map((option) =>
            <Button onClick={() => this.onGranularityModeChange(option.value)}>
                {option.label}</Button>
        );
    }

    onGranularityModeChange(value) {
        let { onChange } = this.props;
        this.setState({ granularityModeValue: value });
        return onChange && onChange(value);
    }
}
