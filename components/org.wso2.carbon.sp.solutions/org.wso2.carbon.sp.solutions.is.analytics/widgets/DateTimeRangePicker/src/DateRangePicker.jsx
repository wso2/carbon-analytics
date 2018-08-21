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
import Widget from '@wso2-dashboards/widget';
import { MuiThemeProvider, createMuiTheme, MenuItem, Select} from 'material-ui';
import GranularityModeSelector from "./GranularityModeSelector";
import CustomTimeRangeSelector from "./CustomTimeRangeSelector";
import Moment from 'moment';
import { Scrollbars } from 'react-custom-scrollbars';

const darkTheme = createMuiTheme({
    palette: {
        type: 'dark',
    },
});

const lightTheme = createMuiTheme({
    palette: {
        type: 'light',
    },
});

export default class DateRangePicker extends Widget {

    constructor(props) {
        super(props);
        this.state = {
            id: props.widgetID,
            width: props.glContainer.width,
            height: props.glContainer.height,
            granularityMode: null,
            granularityValue: '',
            options: this.props.configs.options,
        };

        this.handleResize = this.handleResize.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
        this.handleGranularityChange = this.handleGranularityChange.bind(this);
        this.publishTimeRange = this.publishTimeRange.bind(this);
        this.getTimeIntervalDescriptor = this.getTimeIntervalDescriptor.bind(this);
        this.generateGranularitySelector = this.generateGranularitySelector.bind(this);

    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    publishTimeRange(message) {
       super.publish(message);
    }

    handleGranularityChange(mode) {

        let granularity = '';
        let startTime = null;

        if(mode !== 'custom') {

            switch (mode) {
                case '1 Min':
                    startTime = Moment().subtract(1, 'minutes').toDate();
                    granularity = 'minute';
                    break;
                case '15 Min':
                    startTime = Moment().subtract(15, 'minutes').toDate();
                    granularity = 'minute';
                    break;
                case '1 Hour' :
                    startTime = Moment().subtract(1, 'hours').toDate();
                    granularity = 'minute';
                    break;
                case '1 Day':
                    startTime = Moment().subtract(1, 'days').toDate();
                    granularity = 'hour';
                    break;
                case '7 Days':
                    startTime = Moment().subtract(7, 'days').toDate();
                    granularity = 'day';
                    break;
                case '1 Month':
                    startTime = Moment().subtract(1, 'months').toDate();
                    granularity = 'day';
                    break;
                case '3 Months':
                    startTime = Moment().subtract(3, 'months').toDate();
                    granularity = 'month';
                    break;
                case '6 Months':
                    startTime = Moment().subtract(6, 'months').toDate();
                    granularity = 'month';
                    break;
                case '1 Year':
                    startTime = Moment().subtract(1, 'years').toDate();
                    granularity = 'month';
                    break;
            }

            this.publishTimeRange({
                granularity: granularity,
                from: startTime.getTime(),
                to: new Date().getTime()
            });
        }

        this.setState({ granularityMode: mode, granularityValue: granularity, startTime: startTime, endTime: new Date() });
    }

    componentDidMount() {
        this.handleGranularityChange(this.state.options.defaultValue)
    }

    render() {
        let { granularityMode, width, height } = this.state;
        return (
            <MuiThemeProvider theme={this.props.muiTheme.name === 'dark' ? darkTheme : lightTheme}>
                <Scrollbars style={{ width, height }} >
                    <div style={{ margin: '2%', maxWidth: 840 }}>
                        <GranularityModeSelector onChange={this.handleGranularityChange} />
                        {
                            granularityMode === 'custom' ?
                                <CustomTimeRangeSelector publishMethod={this.publishTimeRange} /> :
                                this.getTimeIntervalDescriptor(granularityMode)
                        }
                    </div>
                </Scrollbars>
            </MuiThemeProvider>
        );
    }

    getTimeIntervalDescriptor(granularityMode) {
        let startTime = null;
        let endTime = null;
        let granularity = null;

        switch (granularityMode) {
            case '1 Min':
                startTime = Moment().subtract(1, 'minutes').format("DD-MMM-YYYY hh:mm A");
                endTime = Moment().format("DD-MMM-YYYY hh:mm A");
                granularity = 'minute';
                break;
            case '15 Min':
                startTime = Moment().subtract(15, 'minutes').format("DD-MMM-YYYY hh:mm A");
                endTime = Moment().format("DD-MMM-YYYY hh:mm A");
                granularity = 'minute';
                break;
            case '1 Hour' :
                startTime = Moment().subtract(1, 'hours').format("DD-MMM-YYYY hh:mm A");
                endTime = Moment().format("DD-MMM-YYYY hh:mm A");
                granularity = 'minute';
                break;
            case '1 Day':
                startTime = Moment().subtract(1, 'days').format("DD-MMM-YYYY");
                endTime = Moment().format("DD-MMM-YYYY");
                granularity = 'day';
                break;
            case '7 Days':
                startTime = Moment().subtract(7, 'days').format("DD-MMM-YYYY");
                endTime = Moment().format("DD-MMM-YYYY");
                granularity = 'day';
                break;
            case '1 Month':
                startTime = Moment().subtract(1, 'months').format("MMM-YYYY");
                endTime = Moment().format('MMM-YYYY');
                granularity = 'month';
                break;
            case '3 Months':
                startTime = Moment().subtract(3, 'months').format('MMM-YYYY');
                endTime = Moment().format('MMM-YYYY');
                granularity = 'month';
                break;
            case '6 Months':
                startTime = Moment().subtract(6, 'months').format('MMM-YYYY');
                endTime = Moment().format('MMM-YYYY');
                granularity = 'month';
                break;
            case '1 Year':
                startTime = Moment().subtract(1, 'years').format('YYYY');
                endTime = Moment().format('YYYY');
                granularity = 'month';
                break;
        }

        if (granularityMode) {
            return (
                <div
                    style={{
                        marginTop: 5
                    }}
                >
                    {`${startTime}  to  ${endTime}  per  `}{this.generateGranularitySelector()}
                </div>
            )
        } else {
            return null;
        }
    }

    generateGranularitySelector() {
        return(
            <Select
                className={'perUnderline'}
                value={this.state.granularityValue}
                onChange={(evt)=> {
                    super.publish({
                        granularity: evt.target.value,
                        from: this.state.startTime.getTime(),
                        to: this.state.endTime.getTime(),
                    });
                    this.setState({ granularityValue: evt.target.value });
                }}
            >
                <MenuItem value={'millisecond'}>Millisecond</MenuItem>
                <MenuItem value={'second'}>Second</MenuItem>
                <MenuItem value={'minute'}>Minute</MenuItem>
                <MenuItem value={'hour'}>Hour</MenuItem>
                <MenuItem value={'day'}>Day</MenuItem>
                <MenuItem value={'month'}>Month</MenuItem>
                <MenuItem value={'year'}>Year</MenuItem>
            </Select>
        )
    }
}

global.dashboard.registerWidget("DateRangePicker", DateRangePicker);
