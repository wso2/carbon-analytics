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
import { MenuItem , Select, Button } from 'material-ui';
import DateTimePicker from "./DateTimePicker";

export default class CustomTimeRangeSelector extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            inputType: 'millisecond',
        };

        this.startTime= new Date();
        this.endTime= new Date();
        this.handleStartTimeChange = this.handleStartTimeChange.bind(this);
        this.handleEndTimeChange = this.handleEndTimeChange.bind(this);
    }

    handleStartTimeChange(date) {
        this.startTime = date;
    }

    handleEndTimeChange(date) {
        this.endTime = date;
    }

    render() {
        let {inputType} = this.state;
        let { publishMethod } = this.props;

        return (
            <div style={{marginTop: 10}}>
                <div
                    style={{
                        width: '100%',
                        marginBottom: 10
                    }}
                >
                    Per<br />
                    <Select
                        value={inputType}
                        style={{
                            width: '50%',
                            minWidth: 200,
                            maxWidth: 400,
                        }}
                        onChange={(evt)=>{
                            this.setState({ inputType: evt.target.value })
                        }}
                    >
                        <MenuItem value={'year'}>Year</MenuItem>
                        <MenuItem value={'month'}>Month</MenuItem>
                        <MenuItem value={'day'}>Day</MenuItem>
                        <MenuItem value={'hour'}>Hour</MenuItem>
                        <MenuItem value={'minute'}>Minute</MenuItem>
                        <MenuItem value={'second'}>Second</MenuItem>
                        <MenuItem value={'millisecond'}>Millisecond</MenuItem>
                    </Select>
                </div>
                <div
                    style={{ minWidth:420 }}
                >
                    <div
                        style={{
                            width: '50%',
                            float: 'left',
                        }}
                    >
                        From
                        <br/>
                        <DateTimePicker
                            onChange={this.handleStartTimeChange}
                            inputType={inputType}
                        />
                    </div>
                    <div
                        style={{
                            width: '50%',
                            float: 'right',
                        }}
                    >
                        To
                        <br/>
                        <DateTimePicker
                            onChange={this.handleEndTimeChange}
                            inputType={inputType}
                        />
                    </div>
                </div>
                <Button
                    variant="raised"
                    color="primary"
                    style={{ marginTop: 10, float: 'right' }}
                    onClick={()=>{
                        publishMethod({ granularity:inputType, from: this.startTime.getTime(), to: this.endTime.getTime() });
                    }}
                >
                    Apply
                </Button>
            </div>
        )
    }
}


