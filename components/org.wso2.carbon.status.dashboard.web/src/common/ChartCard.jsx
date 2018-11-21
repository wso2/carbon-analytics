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
//Material UI
import {Card, CardHeader, CardMedia} from "material-ui/Card";
import {Divider} from "material-ui";
//App Components
import DashboardUtils from "../utils/DashboardUtils";
import VizG from 'react-vizgrammar';

/**
 * class used to display charts of worker specific page.
 */
export default class ChartCard extends Component {
    constructor(props) {
        super(props);
        this.state = {
            expanded: true
        };
    }

    render() {
        let yLimit;
        if (this.props.yDomain == null) {
            yLimit = DashboardUtils.getYDomain(this.props.data)[0] < DashboardUtils.getYDomain(this.props.data)[1] ?
                DashboardUtils.getYDomain(this.props.data) : ((DashboardUtils.getYDomain(this.props.data)[0] <
                    DashboardUtils.getYDomain(this.props.data)[1]) && (this.props.data[0][1] === 0) ? [0, 10] :
                    [0, this.props.data[0][1]]);
        } else {
            yLimit = this.props.yDomain;
        }
        return (
            <Card expanded={this.state.expanded} onExpandChange={(expanded) => {
                this.setState({expanded: expanded})
            }}>
                <CardHeader
                    title={this.props.title}
                    actAsExpander={true}
                    showExpandableButton={true}
                />
                <Divider/>
                <CardMedia
                    expandable={true}
                >
                    <div style={{backgroundColor: '#131313'}}>
                        <VizG data={this.props.data} metadata={this.props.metadata} config={this.props.config}
                              yDomain={yLimit} width={800} height={250}/>
                    </div>
                </CardMedia>
            </Card>
        );
    }
}

