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
import { MenuItem, Select } from 'material-ui';
import moment from 'moment';

export default class DateTimePicker extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            year: new Date().getFullYear(),
            month: new Date().getMonth(),
            days: new Date().getDate(),
            time: moment().format("HH:mm:ss.SSS")
        };

        this.generateDays = this.generateDays.bind(this);
        this.generateMonths = this.generateMonths.bind(this);
        this.generateYears = this.generateYears.bind(this);

    }

    handleOnChange(property, evt) {

        let { inputType, onChange } = this.props;
        let state = this.state;

        state[property] = evt.target.value;

        let date = moment(`${state.year}:${(state.month+1)}:${state.days} ${state.time}`,'YYYY-MM-DD HH:mm:ss.SSS').toDate();

        switch (inputType) {
            case 'year':
                date.setMonth(0);
            case 'month':
                date.setDate(1);
            case 'day':
                date.setHours(0);
            case 'hour':
                date.setMinutes(0);
            case 'minute':
                date.setSeconds(0);
            case 'second':
                date.setMilliseconds(0);
        }

        this.setState(state);

        return onChange && onChange(date);
    }

    render() {
        let { year, month, days, time } = this.state;
        let { inputType } = this.props;


        switch (inputType) {
            case 'hour':
                time = moment(time, 'HH:mm').format('HH:00:00.000');
                break;
            case 'minute':
                time = moment(time, 'HH:mm').format('HH:mm:00.000');
                break;
            case 'second':
                time = moment(time, 'HH:mm:ss').format('HH:mm:ss.000');
                break;
        }

        return (
            <div>
                <div
                    style={{
                        display: 'inline-block'
                    }}
                >
                    {
                        ['year','millisecond','month','day', 'hour', 'minute', 'second'].indexOf(inputType) > -1 ?
                            <Select
                                value={year}
                                onChange={event => {
                                    this.handleOnChange('year', event);
                                }}
                            >
                                {
                                    this.generateYears()
                                }
                            </Select>:
                            null
                    }
                    {
                        ['month','millisecond','day', 'hour', 'minute', 'second'].indexOf(inputType) > -1 ?
                            <Select
                                value={month}
                                onChange={event => {
                                    this.handleOnChange('month', event);
                                }}

                            >
                                {
                                    this.generateMonths()
                                }
                            </Select>:
                            null
                    }
                    {
                        ['day','millisecond', 'hour', 'minute', 'second'].indexOf(inputType) > -1 ?
                            <Select
                                value={days}
                                onChange={event => {
                                    this.handleOnChange('days', event);
                                }}

                            >
                                {
                                    this.generateDays(year, month)
                                }
                            </Select>:
                            null
                    }
                </div>
                {
                    ['millisecond', 'hour', 'minute', 'second'].indexOf(inputType) > -1 ?
                        <div>
                            <br/>Time<br/>
                            <div className={'MuiFormControl-root-69'}>
                                <div className={'MuiInput-root-52 MuiInput-formControl-53 MuiInput-underline-56 underline'}>
                                    <input className={'MuiInput-input-60'} type="time" step={this.getTimeStep(inputType)} value={time} onChange={(evt) => {
                                        this.handleOnChange('time', evt);

                                    }}/>

                                </div>
                            </div>

                        </div>:
                        null

                }

            </div>
        );
    }

    generateYears() {
        let yearArray = [];

        for (let index = 1970; index <= 2099; index++) {
            yearArray.push(
                <MenuItem key={`year-${index}`} value={index}>{index}</MenuItem>
            )
        }

        return yearArray;
    }

    generateMonths() {
        let monthComponents = [];
        let monthArray = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September',
            'October', 'November', 'December'];

        for (let i = 0; i < monthArray.length; i++) {
            monthComponents.push(
                <MenuItem key={`month-${i}`} value={i}>{monthArray[i]}</MenuItem>
            )
        }

        return monthComponents;
    }

    generateDays(year, month) {
        let dayComponents = [];
        let days = 0;

        if (month === 1) {
            if (this.isLeapYear(year)) days = 29;
            else days = 28;
        } else if ((month < 7 && ((month + 1) % 2 === 1)) || (month > 6 && ((month + 1) % 2 === 0))) {
            days = 31;
        } else {
            days = 30;
        }

        for (let i = 1; i <= days; i++) {
            dayComponents.push(
                <MenuItem key={`$days-${i}`} value={i}>{i}</MenuItem>
            )
        }

        return dayComponents;
    }

    isLeapYear(year) {
        return ((year % 4 === 0) && (year % 100 !== 0)) || (year % 400 === 0);
    }

    getTimeStep(inputType) {
        switch (inputType) {
            case 'hour':
                return 3600;
            case 'minute':
                return 60;
            case 'second':
                return 1;
            case 'millisecond':
                return 0.001;
        }
    }

    getTimeString(inputType) {
        switch (inputType) {
            case 'hour':
                return moment().format('HH:00:00.000');
            case 'minute':
                return moment().format('HH:mm:00.000');
            case 'second':
                return moment().format('HH:mm:ss.000');
            case 'millisecond':
                return moment().format('HH:mm:ss.SSS');
        }
    }

}
