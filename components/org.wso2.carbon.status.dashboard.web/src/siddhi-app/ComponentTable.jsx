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

import React from "react";
//App Components
import StatusDashboardAPIS from "../utils/apis/StatusDashboardAPIs";
//Material UI
import {Table, TableBody, TableFooter, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from "material-ui";

/**
 * class which displays Siddhi App component metrics.
 */
export default class ComponentTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            componentData: [],
            workerID: this.props.id,
            appName: this.props.appName
        }
    }

    componentWillMount() {
        StatusDashboardAPIS.getComponents(this.state.workerID, this.state.appName)
            .then((response) => {
                this.setState({
                    componentData: response.data
                })
            });
    }

    render() {
        return (
            <Table>
                <TableHeader displaySelectAll={false}
                             adjustForCheckbox={false}>
                    <TableRow >
                        <TableHeaderColumn style={{color: 'white'}}>Type</TableHeaderColumn>
                        <TableHeaderColumn style={{color: 'white'}}>Name</TableHeaderColumn>
                        <TableHeaderColumn style={{color: 'white'}}>Metric type</TableHeaderColumn>
                        <TableHeaderColumn style={{color: 'white'}}>Attribute</TableHeaderColumn>
                        <TableHeaderColumn style={{color: 'white'}}>Value</TableHeaderColumn>
                    </TableRow>
                </TableHeader>
                <TableBody displayRowCheckbox={false} style={{backgroundColor: '#131313'}}>
                    {this.state.componentData.map((component) => {
                        return (
                            <TableRow>
                                <TableRowColumn>{component.type}</TableRowColumn>
                                <TableRowColumn colSpan={4} style={{paddingLeft: 0, paddingRight: 0}}>
                                    {component.data.map((components, index) => {
                                        if (index + 1 === component.data.length) {
                                            return (
                                                <TableRow style={{
                                                    borderBottom: '', borderLeft: '1px solid rgb(224, 224, 224)'
                                                }}>
                                                    <TableRowColumn
                                                        style={{width: 322}}>{components.name}</TableRowColumn>
                                                    <TableRowColumn style={{paddingLeft: 0, paddingRight: 0}}>
                                                        {components.metrics.map((metric, index) => {
                                                            if (index + 1 === components.metrics.length) {
                                                                return (
                                                                    <TableRow style={{
                                                                        borderBottom: '',
                                                                        borderLeft: '1px solid rgb(224, 224, 224)'
                                                                    }}>
                                                                        <TableRowColumn
                                                                            style={{width: 322}}>{metric.type}</TableRowColumn>
                                                                        <TableRowColumn
                                                                            style={{paddingLeft: 0, paddingRight: 0}}>
                                                                            {metric.attributes.map((attribute, index) => {
                                                                                if (index + 1 === metric.attributes.length) {
                                                                                    return (
                                                                                        <TableRow style={{
                                                                                            borderBottom: '',
                                                                                            borderLeft: '1px solid rgb(224, 224, 224)'
                                                                                        }}>
                                                                                            <TableRowColumn>{attribute.name}</TableRowColumn>
                                                                                            <TableRowColumn style={{
                                                                                                borderLeft: '1px solid rgb(224, 224, 224)',
                                                                                                width: 371
                                                                                            }}>
                                                                                                {attribute.value}</TableRowColumn>
                                                                                        </TableRow>
                                                                                    )
                                                                                }
                                                                                return (
                                                                                    <TableRow
                                                                                        style={{borderLeft: '1px solid rgb(224, 224, 224)'}}>
                                                                                        <TableRowColumn
                                                                                            style={{width: 360}}>{attribute.name}</TableRowColumn>
                                                                                        <TableRowColumn style={{
                                                                                            borderLeft: '1px solid rgb(224, 224, 224)',
                                                                                            width: 371
                                                                                        }}>
                                                                                            {attribute.value}</TableRowColumn>
                                                                                    </TableRow>
                                                                                )
                                                                            })}
                                                                        </TableRowColumn>
                                                                    </TableRow>
                                                                )
                                                            }
                                                            return (
                                                                <TableRow
                                                                    style={{borderLeft: '1px solid rgb(224, 224, 224)'}}>
                                                                    <TableRowColumn
                                                                        style={{width: 322}}>{metric.type}</TableRowColumn>
                                                                    <TableRowColumn
                                                                        style={{paddingLeft: 0, paddingRight: 0}}>
                                                                        {metric.attributes.map((attribute, index) => {
                                                                            if (index + 1 === metric.attributes.length) {
                                                                                return (
                                                                                    <TableRow style={{
                                                                                        borderBottom: '',
                                                                                        borderLeft: '1px solid rgb(224, 224, 224)'
                                                                                    }}>
                                                                                        <TableRowColumn>{attribute.name}</TableRowColumn>
                                                                                        <TableRowColumn style={{
                                                                                            borderLeft: '1px solid rgb(224, 224, 224)',
                                                                                            width: 371
                                                                                        }}>
                                                                                            {attribute.value}</TableRowColumn>
                                                                                    </TableRow>
                                                                                )
                                                                            }
                                                                            return (
                                                                                <TableRow
                                                                                    style={{borderLeft: '1px solid rgb(224, 224, 224)'}}>
                                                                                    <TableRowColumn
                                                                                        style={{width: 360}}>{attribute.name}</TableRowColumn>
                                                                                    <TableRowColumn style={{
                                                                                        borderLeft: '1px solid rgb(224, 224, 224)',
                                                                                        width: 371
                                                                                    }}>
                                                                                        {attribute.value}</TableRowColumn>
                                                                                </TableRow>
                                                                            )
                                                                        })}
                                                                    </TableRowColumn>
                                                                </TableRow>
                                                            )
                                                        })}
                                                    </TableRowColumn>
                                                </TableRow>
                                            )
                                        }
                                        return (
                                            <TableRow style={{
                                                borderLeft: '1px solid rgb(224, 224, 224)'
                                            }}>
                                                <TableRowColumn style={{width: 322}}>{components.name}</TableRowColumn>
                                                <TableRowColumn style={{paddingLeft: 0, paddingRight: 0}}>
                                                    {components.metrics.map((metric, index) => {
                                                        if (index + 1 === components.metrics.length) {
                                                            return (
                                                                <TableRow style={{
                                                                    borderBottom: '',
                                                                    borderLeft: '1px solid rgb(224, 224, 224)'
                                                                }}>
                                                                    <TableRowColumn
                                                                        style={{width: 322}}>{metric.type}</TableRowColumn>
                                                                    <TableRowColumn
                                                                        style={{paddingLeft: 0, paddingRight: 0}}>
                                                                        {metric.attributes.map((attribute, index) => {
                                                                            if (index + 1 === metric.attributes.length) {
                                                                                return (
                                                                                    <TableRow style={{
                                                                                        borderBottom: '',
                                                                                        borderLeft: '1px solid rgb(224, 224, 224)'
                                                                                    }}>
                                                                                        <TableRowColumn>{attribute.name}</TableRowColumn>
                                                                                        <TableRowColumn style={{
                                                                                            borderLeft: '1px solid rgb(224, 224, 224)',
                                                                                            width: 371
                                                                                        }}>
                                                                                            {attribute.value}</TableRowColumn>
                                                                                    </TableRow>
                                                                                )
                                                                            }
                                                                            return (
                                                                                <TableRow
                                                                                    style={{borderLeft: '1px solid rgb(224, 224, 224)'}}>
                                                                                    <TableRowColumn
                                                                                        style={{width: 360}}>{attribute.name}</TableRowColumn>
                                                                                    <TableRowColumn style={{
                                                                                        borderLeft: '1px solid rgb(224, 224, 224)',
                                                                                        width: 371
                                                                                    }}>
                                                                                        {attribute.value}</TableRowColumn>
                                                                                </TableRow>
                                                                            )
                                                                        })}
                                                                    </TableRowColumn>
                                                                </TableRow>
                                                            )
                                                        }
                                                        return (
                                                            <TableRow
                                                                style={{borderLeft: '1px solid rgb(224, 224, 224)'}}>
                                                                <TableRowColumn
                                                                    style={{width: 322}}>{metric.type}</TableRowColumn>
                                                                <TableRowColumn
                                                                    style={{paddingLeft: 0, paddingRight: 0}}>
                                                                    {metric.attributes.map((attribute, index) => {
                                                                        if (index + 1 === metric.attributes.length) {
                                                                            return (
                                                                                <TableRow style={{
                                                                                    borderBottom: '',
                                                                                    borderLeft: '1px solid rgb(224, 224, 224)'
                                                                                }}>
                                                                                    <TableRowColumn>{attribute.name}</TableRowColumn>
                                                                                    <TableRowColumn style={{
                                                                                        borderLeft: '1px solid rgb(224, 224, 224)',
                                                                                        width: 371
                                                                                    }}>
                                                                                        {attribute.value}</TableRowColumn>
                                                                                </TableRow>
                                                                            )
                                                                        }
                                                                        return (
                                                                            <TableRow
                                                                                style={{borderLeft: '1px solid rgb(224, 224, 224)'}}>
                                                                                <TableRowColumn
                                                                                    style={{width: 360}}>{attribute.name}</TableRowColumn>
                                                                                <TableRowColumn style={{
                                                                                    borderLeft: '1px solid rgb(224, 224, 224)',
                                                                                    width: 371
                                                                                }}>
                                                                                    {attribute.value}</TableRowColumn>
                                                                            </TableRow>
                                                                        )
                                                                    })}
                                                                </TableRowColumn>
                                                            </TableRow>
                                                        )
                                                    })}
                                                </TableRowColumn>
                                            </TableRow>
                                        )
                                    })}
                                </TableRowColumn>
                            </TableRow>
                        )
                    })}
                </TableBody>
                <TableFooter adjustForCheckbox={false}>
                    <TableRow>
                        <TableRowColumn colSpan="1" style={{textAlign: 'center'}}>
                        </TableRowColumn>
                    </TableRow>
                </TableFooter>
            </Table>
        );
    }
}