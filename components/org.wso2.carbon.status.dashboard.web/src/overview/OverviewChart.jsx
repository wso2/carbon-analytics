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

import React, {Component} from 'react';
//App Components
import VizG from '../gadgets/VizG';

/**
 * class to get charts to the worker overview page.
 */
export default class OverviewChart extends Component {
    constructor(props) {
        super(props);
        this.state = {
            data: this.props.chartValue,
            color: this.props.color,
            type: this.props.type
        }
    }

    componentWillReceiveProps(nextProps) {
        this.setState({data: nextProps.chartValue})
    }

    render() {
        let configT = {
            charts: [{type: 'arc', x: this.state.type, colorScale: [this.state.color, '#333333']}],
            tooltip: {'enabled': false},
            legend: false, percentage: true,
            width: 300,
            height: 300,
            labelColor: 'white',
            innerRadius: 15
        };

        let metadata = {
            names: [this.state.type],
            types: ['linear']
        };

        return (
            <div>
                <VizG config={configT} metadata={metadata} data={[[this.state.data]]}/>
            </div>

        );
    }
}

