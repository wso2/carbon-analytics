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
import VizG from "../gadgets/VizG";
//Material UI
import {Table, TableBody, TableFooter, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from "material-ui";

const styles = {
    header : {color: '#dedede'},
    tableRow : {
        borderBottom: '',
        borderLeft: '1px solid rgb(224, 224, 224)'
    },
    rowColumn: {
        borderLeft: '1px solid rgb(224, 224, 224)',
        width: 371
    },
    bottomLine: {borderBottom: '', borderLeft: '1px solid rgb(224, 224, 224)'},
    rowWidth: {width: 322}
};
const metadata = {names: ['timestamp', 'value'], types: ['time', 'linear']};
const sparkLineConfig = {x: 'time', charts: [{type: 'spark-area', y: 'value', fill: '#f17b31'}], width: 100, height: 80};

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
                            <TableRowColumn colSpan={4} style={{fontSize: 16, textAlign: 'center'}}>
                                No Data Available
                            </TableRowColumn>
                        </TableRow>
                    </TableBody>
                </Table>
            );
        }
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
                    {this.state.componentData.map((component) => {
                        return (
                            <TableRow>
                                <TableRowColumn style={{fontSize:'20px'}}>{component.type}</TableRowColumn>
                                <TableRowColumn colSpan={3} style={{paddingLeft: 0, paddingRight: 0}}>
                                    {component.data.map((components, index) => {
                                        if (index + 1 === component.data.length) {
                                            return (
                                                <TableRow style={styles.bottomLine}>
                                                    <TableRowColumn
                                                        style={styles.rowWidth}>{components.name}</TableRowColumn>
                                                    <TableRowColumn style={{paddingLeft: 0, paddingRight: 0}}>
                                                        {components.metrics.map((metric, index) => {
                                                            if (index + 1 === components.metrics.length) {
                                                                return (
                                                                    <TableRow style={styles.tableRow}>
                                                                        <TableRowColumn
                                                                            style={styles.rowWidth}>{metric.type}</TableRowColumn>
                                                                        <TableRowColumn
                                                                            style={{
                                                                                paddingLeft: 0,
                                                                                paddingRight: 0
                                                                            }}>
                                                                            {<TableRow style={styles.tableRow}>
                                                                                <TableRowColumn style={{
                                                                                    borderLeft: '1px solid rgb(224, 224, 224)',
                                                                                }}>
                                                                                    <div>
                                                                                        <div style={{width: '50%', float: 'left', lineHeight: 4}}>
                                                                                            {metric.attribute.value}
                                                                                        </div>
                                                                                        <Link style={{textDecoration:'none'}}
                                                                                              to={window.contextPath + '/worker/' + this.state.workerID + "/siddhi-apps/" + this.state.appName
                                                                                              + "/components/" + component.type + '/' + components.name + '/history/' + this.state.statsEnable}>
                                                                                            <div style={{width: '50%', float: 'right'}}>
                                                                                                <VizG data={metric.attribute.recentValues}
                                                                                                      metadata={metadata}
                                                                                                      config={sparkLineConfig}
                                                                                                      yDomain={DashboardUtils.getYDomain(metric.attribute.recentValues)}
                                                                                                />
                                                                                            </div>
                                                                                        </Link>
                                                                                    </div>

                                                                                </TableRowColumn>
                                                                            </TableRow>}
                                                                        </TableRowColumn>
                                                                    </TableRow>
                                                                )
                                                            }
                                                            return (
                                                                <TableRow
                                                                    style={{borderLeft: '1px solid rgb(224, 224, 224)'}}>
                                                                    <TableRowColumn
                                                                        style={styles.rowWidth}>{metric.type}</TableRowColumn>
                                                                    <TableRowColumn
                                                                        style={{paddingLeft: 0, paddingRight: 0}}>
                                                                        {<TableRow style={styles.tableRow}>
                                                                            <TableRowColumn style={{
                                                                                borderLeft: '1px solid rgb(224, 224, 224)',
                                                                            }}>
                                                                                <div>
                                                                                    <div style={{width: '50%', float: 'left', lineHeight: 4}}>
                                                                                        {metric.attribute.value}
                                                                                    </div>
                                                                                    <Link style={{textDecoration:'none'}}
                                                                                          to={window.contextPath + '/worker/' + this.state.workerID + "/siddhi-apps/" + this.state.appName
                                                                                          + "/components/" + component.type + '/' + components.name + '/history/' + this.state.statsEnable}>
                                                                                        <div style={{width: '50%', float: 'right'}}>
                                                                                            <VizG data={metric.attribute.recentValues}
                                                                                                  metadata={metadata}
                                                                                                  config={sparkLineConfig}
                                                                                                  yDomain={DashboardUtils.getYDomain(metric.attribute.recentValues)}
                                                                                            />
                                                                                        </div>
                                                                                    </Link>
                                                                                </div>

                                                                            </TableRowColumn>
                                                                        </TableRow>}
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
                                                <TableRowColumn
                                                    style={styles.rowWidth}>{components.name}</TableRowColumn>
                                                <TableRowColumn style={{paddingLeft: 0, paddingRight: 0}}>
                                                    {components.metrics.map((metric, index) => {
                                                        if (index + 1 === components.metrics.length) {
                                                            return (
                                                                <TableRow style={styles.tableRow}>
                                                                    <TableRowColumn
                                                                        style={styles.rowWidth}>{metric.type}</TableRowColumn>
                                                                    <TableRowColumn
                                                                        style={{paddingLeft: 0, paddingRight: 0}}>
                                                                        {<TableRow style={styles.tableRow}>
                                                                            <TableRowColumn style={{
                                                                                borderLeft: '1px solid rgb(224, 224, 224)',
                                                                            }}>
                                                                                <div>
                                                                                    <div style={{width: '50%', float: 'left', lineHeight: 4}}>
                                                                                        {metric.attribute.value}
                                                                                    </div>
                                                                                    <Link style={{textDecoration:'none'}}
                                                                                          to={window.contextPath + '/worker/' + this.state.workerID + "/siddhi-apps/" + this.state.appName
                                                                                          + "/components/" + component.type + '/' + components.name + '/history/' + this.state.statsEnable}>
                                                                                        <div style={{width: '50%', float: 'right'}}>
                                                                                            <VizG data={metric.attribute.recentValues}
                                                                                                  metadata={metadata}
                                                                                                  config={sparkLineConfig}
                                                                                                  yDomain={DashboardUtils.getYDomain(metric.attribute.recentValues)}
                                                                                            />
                                                                                        </div>
                                                                                    </Link>
                                                                                </div>

                                                                            </TableRowColumn>
                                                                        </TableRow>}
                                                                    </TableRowColumn>
                                                                </TableRow>
                                                            )
                                                        }
                                                        return (
                                                            <TableRow
                                                                style={{borderLeft: '1px solid rgb(224, 224, 224)'}}>
                                                                <TableRowColumn
                                                                    style={styles.rowWidth}>{metric.type}</TableRowColumn>
                                                                <TableRowColumn
                                                                    style={{paddingLeft: 0, paddingRight: 0}}>
                                                                    {<TableRow style={styles.tableRow}>
                                                                        <TableRowColumn style={{
                                                                            borderLeft: '1px solid rgb(224, 224, 224)',
                                                                        }}>
                                                                            <div>
                                                                                <div style={{width: '50%', float: 'left', lineHeight: 4}}>
                                                                                    {metric.attribute.value}
                                                                                </div>
                                                                                <Link style={{textDecoration:'none'}}
                                                                                      to={window.contextPath + '/worker/' + this.state.workerID + "/siddhi-apps/" + this.state.appName
                                                                                      + "/components/" + component.type + '/' + components.name + '/history/' + this.state.statsEnable}>
                                                                                    <div style={{width: '50%', float: 'right'}}>
                                                                                        <VizG data={metric.attribute.recentValues}
                                                                                              metadata={metadata}
                                                                                              config={sparkLineConfig}
                                                                                              yDomain={DashboardUtils.getYDomain(metric.attribute.recentValues)}
                                                                                        />
                                                                                    </div>
                                                                                </Link>
                                                                            </div>

                                                                        </TableRowColumn>
                                                                    </TableRow>}
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