/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

import React from 'react';
import {Link} from 'react-router-dom';
// Material UI Components
import IconButton from 'material-ui/IconButton';
import RefreshIcon from 'material-ui-icons/Refresh';
import EditIcon from 'material-ui-icons/Edit';
import DeleteIcon from 'material-ui-icons/Delete';
import {TableCell, TableRow} from 'material-ui/Table';
import Tooltip from 'material-ui/Tooltip';
import VisibilityIcon from 'material-ui-icons/Visibility';
import DnsIcon from 'material-ui-icons/Dns';
// App Constants
import BusinessRulesConstants from '../constants/BusinessRulesConstants';
// CSS
import '../index.css';

/**
 * App context sans starting forward slash.
 */
const appContext = window.contextPath;

/**
 * Represents each Business Rule, that is shown as a row, to view, edit, delete / re-deploy Business Rules
 */
class BusinessRule extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            name: props.name,
            uuid: props.uuid,
            type: props.type,
            status: props.status
        };
    }

    /**
     * Handles onClick action of the 'Re-deploy' button
     */
    handleReDeployButtonClick() {
        this.props.redeploy(this.props.uuid);
    }

    /**
     * Sends the API call for deleting this business rule
     */
    handleDeleteButtonClick() {
        this.props.showDeleteDialog(this.props.uuid);
    }

    render() {
        let deploymentStatus = BusinessRulesConstants.BUSINESS_RULE_STATUSES[Number(this.props.status)];
        let retryDeployButton;
        switch (this.props.status) {
            case (1) : {
                retryDeployButton =
                    (<Tooltip id="tooltip-right" title="Deploy" placement="right-end">
                        <IconButton color="primary" aria-label="Deploy"
                                    onClick={() => this.handleReDeployButtonClick()}>
                            <RefreshIcon/>
                        </IconButton>
                    </Tooltip>);
                break;
            }
            case (2) : {
                retryDeployButton =
                    (<Tooltip id="tooltip-right" title="Re-Deploy" placement="right-end">
                        <IconButton color="primary" aria-label="ReDeploy"
                                    onClick={() => this.handleReDeployButtonClick()}>
                            <RefreshIcon/>
                        </IconButton>
                    </Tooltip>);
                break;
            }
            case (3) : {
                retryDeployButton =
                    (<Tooltip id="tooltip-right" title="Retry Un-deploy" placement="right-end">
                        <IconButton color="primary" aria-label="RetryUndeploy"
                                    onClick={() => this.handleReDeployButtonClick()}>
                            <RefreshIcon/>
                        </IconButton>
                    </Tooltip>);
                break;
            }
            case (4) : {
                retryDeployButton =
                    (<Tooltip id="tooltip-right" title="Re-Deploy" placement="right-end">
                        <IconButton color="primary" aria-label="ReDeploy"
                                    onClick={() => this.handleReDeployButtonClick()}>
                            <RefreshIcon/>
                        </IconButton>
                    </Tooltip>);
                break;
            }
        }

        // Display action buttons according to permissions
        let actionButtonsCell;

        if (this.props.permissions === 0) {
            // Manager permissions
            actionButtonsCell =
                <TableCell>
                    <Tooltip id="tooltip-right" title="Deployment Info" placement="right-end">
                        <IconButton aria-label="View" onClick={() => this.props.showDeploymentInfo(this.props.uuid)}>
                            <DnsIcon/>
                        </IconButton>
                    </Tooltip>
                    &nbsp;
                    <Tooltip id="tooltip-right" title="View" placement="right-end">
                        <Link
                            to={appContext + '/businessRuleFrom' + (this.props.type.charAt(0).toUpperCase() +
                                this.props.type.substr(1).toLowerCase()) + 'Form/' +
                            BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW + '/templateGroup/businessRule/' +
                            this.props.uuid}
                            style={{textDecoration: 'none'}}>
                            <IconButton aria-label="View">
                                <VisibilityIcon/>
                            </IconButton>
                        </Link>
                    </Tooltip>
                    &nbsp;
                    <Tooltip id="tooltip-right" title="Edit" placement="right-end">
                        <Link
                            to={appContext + '/businessRuleFrom' +
                            (this.props.type.charAt(0).toUpperCase() + this.props.type.substr(1).toLowerCase())
                            + 'Form/' +
                            BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT + '/templateGroup/businessRule/' +
                            this.props.uuid}
                            style={{textDecoration: 'none'}}>
                            <IconButton aria-label="Edit">
                                <EditIcon/>
                            </IconButton>
                        </Link>
                    </Tooltip>
                    &nbsp;
                    <Tooltip id="tooltip-right" title="Delete" placement="right-end">
                        <IconButton aria-label="Delete" onClick={() => this.handleDeleteButtonClick()}>
                            <DeleteIcon/>
                        </IconButton>
                    </Tooltip>
                    &nbsp;
                    {retryDeployButton}
                </TableCell>
        } else {
            // Viewer permissions only
            actionButtonsCell =
                <TableCell>
                    <Tooltip id="tooltip-right" title="Deployment Info" placement="right-end">
                        <IconButton aria-label="View" onClick={() => this.props.showDeploymentInfo(this.props.uuid)}>
                            <DnsIcon/>
                        </IconButton>
                    </Tooltip>
                    &nbsp;
                    <Tooltip id="tooltip-right" title="View" placement="right-end">
                        <Link
                            to={appContext + '/businessRuleFrom' +
                            (this.props.type.charAt(0).toUpperCase() + this.props.type.substr(1).toLowerCase()) +
                            'Form/' +
                            BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW + '/templateGroup/businessRule/' +
                            this.props.uuid}
                            style={{textDecoration: 'none'}}>
                            <IconButton aria-label="View">
                                <VisibilityIcon/>
                            </IconButton>
                        </Link>
                    </Tooltip>
                </TableCell>
        }

        return (
            <TableRow>
                <TableCell>
                    {this.props.name}
                </TableCell>
                <TableCell>{deploymentStatus}</TableCell>
                {actionButtonsCell}
            </TableRow>
        )
    }
}

export default BusinessRule;
