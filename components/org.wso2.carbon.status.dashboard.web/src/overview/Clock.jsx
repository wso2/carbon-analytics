/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

import React, {Component} from "react";

/**
 * class to get charts to the worker overview page.
 */
export default class Clock extends Component {
    constructor(props) {
        super(props);
        this.state = {
            lastUpdatedTime: (new Date().getTime() - this.props.lastUpdate)
        }
    }

    millisecondsToStr(milliseconds) {
        function numberEnding(number) {
            return (number > 1) ? '' : '';
        }

        let temp = Math.floor(milliseconds / 1000);
        let years = Math.floor(temp / 31536000);
        if (years) {
            return years + ' y' + numberEnding(years);
        }
        let days = Math.floor((temp %= 31536000) / 86400);
        if (days) {
            return days + ' d' + numberEnding(days);
        }
        let hours = Math.floor((temp %= 86400) / 3600);
        if (hours) {
            return hours + ' h' + numberEnding(hours);
        }
        let minutes = Math.floor((temp %= 3600) / 60);
        if (minutes) {
            return minutes + ' m' + numberEnding(minutes);
        }
        let seconds = temp % 60;
        if (seconds) {
            return seconds + ' s' + numberEnding(seconds);
        }
        return 'just now'; //'just now'
    }

    componentWillMount() {
        let interval = setInterval(() => {
            this.setState({lastUpdatedTime: ((new Date().getTime() - this.props.lastUpdate))});
        }, 1000);
    }

    render() {
        return (
            <text>{this.millisecondsToStr(this.state.lastUpdatedTime) + " ago"}</text>
        );
    }
}

