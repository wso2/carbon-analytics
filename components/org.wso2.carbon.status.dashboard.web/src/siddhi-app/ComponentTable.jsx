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
import {Link} from "react-router-dom";
//App Components
import StatusDashboardAPIS from "../utils/apis/StatusDashboardAPIs";
import DashboardUtils from "../utils/DashboardUtils";
import VizG from 'react-vizgrammar';
//Material UI
import {Table, TableBody, TableFooter, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from "material-ui";

const styles = {
    header: {color: '#dedede'},
    tableRow: {
        borderLeft: '1px solid rgb(120, 120, 120)'
    },
    rowColumn: {
        borderBottom: '1px solid rgb(120, 120, 120)',
        borderLeft: '1px solid rgb(120, 120, 120)'
    }
};
const metadata = {names: ['Timestamp', 'value'], types: ['time', 'linear']};
const sparkLineConfig = {
    x: 'Timestamp',
    charts: [{type: 'spark-area', y: 'value', fill: '#f17b31',fillOpacity:'0.1'}],
    strokeWidth:1,
    fillOpacity:0.1
};

/**
 * class which displays Siddhi App component metrics.
 */
export default class ComponentTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            componentData: [],
            workerID: this.props.id,
            appName: this.props.appName,
            statsEnable: this.props.statsEnabled
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
        if (this.state.componentData.length === 0) {
            return (
                <Table>
                    <TableHeader displaySelectAll={false}
                                 adjustForCheckbox={false}>
                        <TableRow >
                            <TableHeaderColumn style={styles.header}>Type</TableHeaderColumn>
                            <TableHeaderColumn style={styles.header}>Name</TableHeaderColumn>
                            <TableHeaderColumn style={styles.header}>Metric Type</TableHeaderColumn>
                            <TableHeaderColumn style={styles.header}>Value</TableHeaderColumn>
                        </TableRow>
                    </TableHeader>
                    <TableBody displayRowCheckbox={false} style={{backgroundColor: '#131313'}}>
                        <TableRow>
                            <TableRowColumn style={{fontSize: 16, textAlign: 'center'}}>
                                No Data Available
                            </TableRowColumn>
                        </TableRow>
                    </TableBody>
                </Table>
            );
        }
        return (
            <Table selectable={false}>
                <TableHeader displaySelectAll={false}
                             adjustForCheckbox={false}>
                    <TableRow >
                        <TableHeaderColumn style={styles.header}>Type</TableHeaderColumn>
                        <TableHeaderColumn style={styles.header}>Name</TableHeaderColumn>
                        <TableHeaderColumn style={styles.header}>Metric Type</TableHeaderColumn>
                        <TableHeaderColumn style={styles.header}>Value</TableHeaderColumn>
                    </TableRow>
                </TableHeader>
                <TableBody displayRowCheckbox={false} style={{backgroundColor: '#131313'}}>
                    {this.state.componentData.map((component) => {
                        return (
                            <TableRow style={{
                                borderBottom: '2px solid rgb(200, 200, 200)',
                            }}>
                                <TableRowColumn
                                    style={{fontSize: '20px', width: '25%'}}>{component.type}</TableRowColumn>
                                <TableRowColumn style={{paddingLeft: 0, paddingRight: 0, width: '75%'}}>
                                    {component.data.map((components, index) => {
                                        if (index + 1 === component.data.length) {
                                            return (
                                                <Table selectable>
                                                    <TableBody showRowHover onRowSelection={() =>
                                                        window.location.href = window.contextPath + '/worker/' +
                                                            this.state.workerID + "/siddhi-apps/" + this.state.appName
                                                            + "/components/" + component.type + '/'
                                                            + components.name + '/history/' + this.state.statsEnable}
                                                               displayRowCheckbox={false}
                                                               style={{backgroundColor: '#131313', cursor: 'pointer'}}>
                                                        <TableRow style={styles.tableRow}>
                                                            <TableRowColumn
                                                                style={{width: '33.33%'}}>{components.name}</TableRowColumn>
                                                            <TableRowColumn style={{paddingLeft: 0, paddingRight: 0}}>
                                                                {components.metrics.map((metric, index) => {
                                                                    if (index + 1 === components.metrics.length) {
                                                                        return (
                                                                            <Table selectable={false}>
                                                                                <TableBody displayRowCheckbox={false}
                                                                                           style={
                                                                                               {backgroundColor: '#131313'}}>
                                                                                    <TableRow style={styles.tableRow}>
                                                                                        <TableRowColumn>
                                                                                            {metric.type}
                                                                                        </TableRowColumn>
                                                                                        <TableRowColumn
                                                                                            style={{
                                                                                                paddingLeft: 0,
                                                                                                paddingRight: 0
                                                                                            }}>
                                                                                            {<Table selectable={false}>
                                                                                                <TableBody
                                                                                                    displayRowCheckbox={false}
                                                                                                    style={
                                                                                                        {backgroundColor: '#131313'}
                                                                                                    }>
                                                                                                    <TableRow
                                                                                                        style={styles.tableRow}>
                                                                                                        <TableRowColumn>
                                                                                                            <div>
                                                                                                                <div
                                                                                                                    style={{
                                                                                                                        width: '50%',
                                                                                                                        float: 'left',
                                                                                                                        lineHeight: 4
                                                                                                                    }}>
                                                                                                                    {metric.attribute.value}
                                                                                                                </div>
                                                                                                                <Link
                                                                                                                    style={{textDecoration: 'none'}}
                                                                                                                    to={window.contextPath + '/worker/'
                                                                                                                    + this.state.workerID + "/siddhi-apps/"
                                                                                                                    + this.state.appName
                                                                                                                    + "/components/" + component.type + '/'
                                                                                                                    + components.name + '/history/'
                                                                                                                    + this.state.statsEnable}>
                                                                                                                    <div
                                                                                                                        style={{
                                                                                                                            width: '50%',
                                                                                                                            float: 'right'
                                                                                                                        }}>
                                                                                                                        <div style={{width: '100px', height: '48px'}}>
                                                                                                                        <VizG
                                                                                                                            data={metric.attribute.recentValues}
                                                                                                                            metadata={metadata}
                                                                                                                            config={sparkLineConfig}
                                                                                                                            yDomain={DashboardUtils.getYDomain(metric.attribute.recentValues)}
                                                                                                                            width={100}
                                                                                                                            height={50}
                                                                                                                        />
                                                                                                                        </div>
                                                                                                                    </div>
                                                                                                                </Link>
                                                                                                            </div>

                                                                                                        </TableRowColumn>
                                                                                                    </TableRow></TableBody></Table>}
                                                                                        </TableRowColumn>
                                                                                    </TableRow></TableBody></Table>
                                                                        )
                                                                    }
                                                                    return (
                                                                        <Table selectable={false}><TableBody
                                                                            displayRowCheckbox={false}
                                                                            style={{backgroundColor: '#131313'}}>
                                                                            <TableRow
                                                                                style={styles.rowColumn}>
                                                                                <TableRowColumn>
                                                                                    {metric.type}
                                                                                </TableRowColumn>
                                                                                <TableRowColumn
                                                                                    style={{
                                                                                        paddingLeft: 0,
                                                                                        paddingRight: 0
                                                                                    }}>
                                                                                    {<Table
                                                                                        selectable={false}><TableBody
                                                                                        displayRowCheckbox={false}
                                                                                        style={{backgroundColor: '#131313'}}>
                                                                                        <TableRow
                                                                                            style={styles.tableRow}>
                                                                                            <TableRowColumn
                                                                                                style={styles.tableRow}>
                                                                                                <div>
                                                                                                    <div style={{
                                                                                                        width: '50%',
                                                                                                        float: 'left',
                                                                                                        lineHeight: 4
                                                                                                    }}>
                                                                                                        {metric.attribute.value}
                                                                                                    </div>
                                                                                                    <Link
                                                                                                        style={{textDecoration: 'none'}}
                                                                                                        to={window.contextPath + '/worker/'
                                                                                                        + this.state.workerID + "/siddhi-apps/" + this.state.appName
                                                                                                        + "/components/" + component.type + '/'
                                                                                                        + components.name + '/history/' + this.state.statsEnable}>
                                                                                                        <div style={{
                                                                                                            width: '50%',
                                                                                                            float: 'right'
                                                                                                        }}>
                                                                                                            <div style={{width: '100px', height: '48px'}}>
                                                                                                            <VizG
                                                                                                                data={metric.attribute.recentValues}
                                                                                                                metadata={metadata}
                                                                                                                config={sparkLineConfig}
                                                                                                                yDomain={DashboardUtils.getYDomain(metric.attribute.recentValues)}
                                                                                                                width={100}
                                                                                                                height={50}
                                                                                                            />
                                                                                                            </div>
                                                                                                        </div>
                                                                                                    </Link>
                                                                                                </div>

                                                                                            </TableRowColumn>
                                                                                        </TableRow></TableBody></Table>}
                                                                                </TableRowColumn>
                                                                            </TableRow></TableBody></Table>
                                                                    )
                                                                })}
                                                            </TableRowColumn>
                                                        </TableRow></TableBody></Table>
                                            )
                                        }
                                        return (
                                            <Table selectable>
                                                <TableBody showRowHover onRowSelection={() =>
                                                    window.location.href = window.contextPath + '/worker/'
                                                        + this.state.workerID + "/siddhi-apps/" + this.state.appName
                                                        + "/components/" + component.type + '/'
                                                        + components.name + '/history/' + this.state.statsEnable}
                                                           displayRowCheckbox={false}
                                                           style={{backgroundColor: '#131313', cursor: 'pointer'}}>
                                                    <TableRow style={styles.rowColumn}>
                                                        <TableRowColumn
                                                            style={{width: '33.33%'}}>{components.name}</TableRowColumn>
                                                        <TableRowColumn style={{paddingLeft: 0, paddingRight: 0}}>
                                                            {components.metrics.map((metric, index) => {
                                                                if (index + 1 === components.metrics.length) {
                                                                    return (
                                                                        <Table selectable={false}><TableBody
                                                                            displayRowCheckbox={false}
                                                                            style={{backgroundColor: '#131313'}}>
                                                                            <TableRow style={styles.tableRow}>
                                                                                <TableRowColumn>
                                                                                    {metric.type}
                                                                                </TableRowColumn>
                                                                                <TableRowColumn
                                                                                    style={{
                                                                                        paddingLeft: 0,
                                                                                        paddingRight: 0
                                                                                    }}>
                                                                                    {<Table
                                                                                        selectable={false}><TableBody
                                                                                        displayRowCheckbox={false}
                                                                                        style={{backgroundColor: '#131313'}}>
                                                                                        <TableRow
                                                                                            style={styles.tableRow}>
                                                                                            <TableRowColumn
                                                                                                style={styles.tableRow}>
                                                                                                <div>
                                                                                                    <div style={{
                                                                                                        width: '50%',
                                                                                                        float: 'left',
                                                                                                        lineHeight: 4
                                                                                                    }}>
                                                                                                        {metric.attribute.value}
                                                                                                    </div>
                                                                                                    <Link
                                                                                                        style={{textDecoration: 'none'}}
                                                                                                        to={window.contextPath + '/worker/'
                                                                                                        + this.state.workerID + "/siddhi-apps/" + this.state.appName
                                                                                                        + "/components/" + component.type + '/'
                                                                                                        + components.name + '/history/' + this.state.statsEnable}>
                                                                                                        <div style={{
                                                                                                            width: '50%',
                                                                                                            float: 'right'
                                                                                                        }}>
                                                                                                            <div style={{width: '100px', height: '48px'}}>
                                                                                                            <VizG
                                                                                                                data={metric.attribute.recentValues}
                                                                                                                metadata={metadata}
                                                                                                                config={sparkLineConfig}
                                                                                                                yDomain={DashboardUtils.getYDomain(metric.attribute.recentValues)}
                                                                                                                width={100}
                                                                                                                height={50}
                                                                                                            />
                                                                                                            </div>
                                                                                                        </div>
                                                                                                    </Link>
                                                                                                </div>

                                                                                            </TableRowColumn>
                                                                                        </TableRow></TableBody></Table>}
                                                                                </TableRowColumn>
                                                                            </TableRow></TableBody></Table>
                                                                    )
                                                                }
                                                                return (
                                                                    <Table selectable={false}><TableBody
                                                                        displayRowCheckbox={false}
                                                                        style={{backgroundColor: '#131313'}}>
                                                                        <TableRow
                                                                            style={styles.rowColumn}>
                                                                            <TableRowColumn>{metric.type}</TableRowColumn>
                                                                            <TableRowColumn
                                                                                style={{
                                                                                    paddingLeft: 0,
                                                                                    paddingRight: 0
                                                                                }}>
                                                                                {<Table selectable={false}><TableBody
                                                                                    displayRowCheckbox={false}
                                                                                    style={{backgroundColor: '#131313'}}><TableRow
                                                                                    style={styles.tableRow}>
                                                                                    <TableRowColumn
                                                                                        style={styles.tableRow}>
                                                                                        <div>
                                                                                            <div style={{
                                                                                                width: '50%',
                                                                                                float: 'left',
                                                                                                lineHeight: 4
                                                                                            }}>
                                                                                                {metric.attribute.value}
                                                                                            </div>
                                                                                            <Link
                                                                                                style={{textDecoration: 'none'}}
                                                                                                to={window.contextPath + '/worker/'
                                                                                                + this.state.workerID + "/siddhi-apps/"
                                                                                                + this.state.appName
                                                                                                + "/components/" + component.type + '/'
                                                                                                + components.name + '/history/'
                                                                                                + this.state.statsEnable}>
                                                                                                <div style={{
                                                                                                    width: '50%',
                                                                                                    float: 'right'
                                                                                                }}>
                                                                                                    <div style={{width: '100px', height: '48px'}}>
                                                                                                    <VizG
                                                                                                        data={metric.attribute.recentValues}
                                                                                                        metadata={metadata}
                                                                                                        config={sparkLineConfig}
                                                                                                        yDomain={DashboardUtils.getYDomain(metric.attribute.recentValues)}
                                                                                                        width={100}
                                                                                                        height={50}
                                                                                                    />
                                                                                                    </div>
                                                                                                </div>
                                                                                            </Link>
                                                                                        </div>

                                                                                    </TableRowColumn>
                                                                                </TableRow></TableBody></Table>}
                                                                            </TableRowColumn>
                                                                        </TableRow>
                                                                    </TableBody></Table>
                                                                )
                                                            })}
                                                        </TableRowColumn>
                                                    </TableRow>
                                                </TableBody>
                                            </Table>
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