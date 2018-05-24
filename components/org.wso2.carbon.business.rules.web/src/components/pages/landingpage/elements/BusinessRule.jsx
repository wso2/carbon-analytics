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

import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
// Material UI Components
import IconButton from 'material-ui/IconButton';
import RefreshIcon from 'material-ui-icons/Refresh';
import EditIcon from 'material-ui-icons/Edit';
import DeleteIcon from 'material-ui-icons/Delete';
import { TableCell, TableRow } from 'material-ui/Table';
import Tooltip from 'material-ui/Tooltip';
import VisibilityIcon from 'material-ui-icons/Visibility';
import DnsIcon from 'material-ui-icons/Dns';
// App Constants
import BusinessRulesConstants from '../../../../constants/BusinessRulesConstants';
// CSS
import '../../../../index.css';

/**
 * App context
 */
const appContext = window.contextPath;

/**
 * Represents an available business rule with relevant actions, that is shown in the landing page
 */
export default class BusinessRule extends Component {
    /**
     * Re-deploys the business rule
     */
    handleReDeployButtonClick() {
        this.props.redeploy(this.props.uuid);
    }

    /**
     * Deletes the business rule
     */
    handleDeleteButtonClick() {
        this.props.showDeleteDialog(this.props.uuid);
    }

    /**
     * Returns the Retry Deploy button
     * @returns {Element}       Retry Deploy button
     */
    displayRetryDeployButton() {
        switch (this.props.status) {
            case 1:
                return (
                    <Tooltip id="tooltip-right" title="Deploy" placement="right-end">
                        <IconButton
                            color="primary"
                            aria-label="Deploy"
                            onClick={() => this.handleReDeployButtonClick()}
                        >
                            <RefreshIcon />
                        </IconButton>
                    </Tooltip>);
            case 2:
            case 4:
                return (
                    <Tooltip id="tooltip-right" title="Re-Deploy" placement="right-end">
                        <IconButton
                            color="primary"
                            aria-label="ReDeploy"
                            onClick={() => this.handleReDeployButtonClick()}
                        >
                            <RefreshIcon />
                        </IconButton>
                    </Tooltip>);
            case 3:
                return (
                    <Tooltip id="tooltip-right" title="Retry Un-deploy" placement="right-end">
                        <IconButton
                            color="primary"
                            aria-label="RetryUndeploy"
                            onClick={() => this.handleReDeployButtonClick()}
                        >
                            <RefreshIcon />
                        </IconButton>
                    </Tooltip>);
            default:
                return null;
        }
    }

    /**
     * Returns Action Buttons of the business rule
     * @returns {Component}     Action Buttons of the business rule
     */
    displayActionButtons() {
        if (this.props.permissions === BusinessRulesConstants.USER_PERMISSIONS.MANAGER) {
            // Manager permissions
            return (
                <TableCell>
                    <Tooltip id="tooltip-right" title="Deployment Info" placement="right-end">
                        <IconButton aria-label="View" onClick={() => this.props.showDeploymentInfo(this.props.uuid)}>
                            <DnsIcon />
                        </IconButton>
                    </Tooltip>
                    &nbsp;
                    <Tooltip id="tooltip-right" title="View" placement="right-end">
                        <Link
                            to={appContext + '/businessRuleFrom' + (this.props.type.charAt(0).toUpperCase() +
                                this.props.type.substr(1).toLowerCase()) + 'Form/' +
                            BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW + '/templateGroup/businessRule/' +
                            this.props.uuid}
                            style={{ textDecoration: 'none' }}
                        >
                            <IconButton aria-label="View">
                                <VisibilityIcon />
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
                            style={{ textDecoration: 'none' }}
                        >
                            <IconButton aria-label="Edit">
                                <EditIcon />
                            </IconButton>
                        </Link>
                    </Tooltip>
                    &nbsp;
                    <Tooltip id="tooltip-right" title="Delete" placement="right-end">
                        <IconButton aria-label="Delete" onClick={() => this.handleDeleteButtonClick()}>
                            <DeleteIcon />
                        </IconButton>
                    </Tooltip>
                    &nbsp;
                    {this.displayRetryDeployButton()}
                </TableCell>
            );
        } else {
            // Viewer permissions
            return (
                <TableCell>
                    <Tooltip id="tooltip-right" title="Deployment Info" placement="right-end">
                        <IconButton aria-label="View" onClick={() => this.props.showDeploymentInfo(this.props.uuid)}>
                            <DnsIcon />
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
                            style={{ textDecoration: 'none' }}
                        >
                            <IconButton aria-label="View">
                                <VisibilityIcon />
                            </IconButton>
                        </Link>
                    </Tooltip>
                </TableCell>
            );
        }
    }

    render() {
        const deploymentStatus = BusinessRulesConstants.BUSINESS_RULE_STATUSES[Number(this.props.status)];

        return (
            <TableRow>
                <TableCell>
                    {this.props.name}
                </TableCell>
                <TableCell>
                    {deploymentStatus}
                </TableCell>
                {this.displayActionButtons()}
            </TableRow>
        );
    }
}

BusinessRule.propTypes = {
    uuid: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    type: PropTypes.oneOf([
        BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE,
        BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH,
    ]).isRequired,
    status: PropTypes.number.isRequired,
    permissions: PropTypes.oneOf([
        BusinessRulesConstants.USER_PERMISSIONS.MANAGER,
        BusinessRulesConstants.USER_PERMISSIONS.VIEWER,
        BusinessRulesConstants.USER_PERMISSIONS.NONE,
        BusinessRulesConstants.USER_PERMISSIONS.UNSET,
    ]).isRequired,
    redeploy: PropTypes.func.isRequired,
    showDeleteDialog: PropTypes.func.isRequired,
    showDeploymentInfo: PropTypes.func.isRequired,
};
