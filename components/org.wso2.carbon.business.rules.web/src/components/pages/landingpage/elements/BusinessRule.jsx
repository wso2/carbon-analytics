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
     * Gets path for the respective businessRuleType's form
     * @param {String} businessRuleType     Type of the business rule
     * @returns {string}                    Path of the business rule form
     */
    getFormPath(businessRuleType) {
        if (businessRuleType === BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE) {
            return 'businessRuleFromTemplateForm';
        }
        return 'businessRuleFromScratchForm';
    }

    /**
     * Handles Deployment Info request
     */
    handleDeploymentInfoRequest() {
        this.props.onDeploymentInfoRequest();
    }

    /**
     * Handles Re-deploy button click
     */
    handleReDeployButtonClick() {
        this.props.onRedeploy();
    }

    /**
     * Handles Delete button click
     */
    handleDeleteButtonClick() {
        this.props.onDeleteRequest();
    }

    /**
     * Returns the Deploy button, with the given title
     * @param {String} title        Title of the deploy button, denoting its action
     * @returns {Component}         Tooltip Component, containing IconButton
     */
    displayDescribedDeployButton(title) {
        return (
            <Tooltip id="tooltip-right" title={title} placement="right-end">
                <IconButton
                    color="primary"
                    aria-label={title}
                    onClick={() => this.handleReDeployButtonClick()}
                >
                    <RefreshIcon />
                </IconButton>
            </Tooltip>
        );
    }

    /**
     * Returns the Deploy button
     * @returns {Component}     Retry Deploy button
     */
    displayDeployButton() {
        switch (this.props.status) {
            case 1:
                return this.displayDescribedDeployButton('Deploy');
            case 3:
                return this.displayDescribedDeployButton('Retry Un-Deploy');
            case 2:
            case 4:
                return this.displayDescribedDeployButton('Re-Deploy');
            default:
                return null;
        }
    }

    /**
     * Returns the Deployment Info button
     * @returns {Component}     Tooltip Component, containing IconButton
     */
    displayDeploymentInfoButton() {
        return (
            <Tooltip id="tooltip-right" title="Deployment Info" placement="right-end">
                <IconButton
                    aria-label="Deployment Info"
                    onClick={() => this.handleDeploymentInfoRequest()}
                >
                    <DnsIcon />
                </IconButton>
            </Tooltip>
        );
    }

    /**
     * Returns the View button
     * @returns {Component}     Tooltip Component, containing IconButton
     */
    displayViewButton() {
        return (
            <Tooltip id="tooltip-right" title="View" placement="right-end">
                <Link
                    to={`${appContext}/${this.getFormPath(this.props.type)}` +
                    `/${BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}` +
                    `/templateGroup/businessRule/${this.props.uuid}`}
                    style={{ textDecoration: 'none' }}
                >
                    <IconButton aria-label="View">
                        <VisibilityIcon />
                    </IconButton>
                </Link>
            </Tooltip>
        );
    }

    /**
     * Returns the Edit button
     * @returns {Component}     Tooltip Component, containing IconButton
     */
    displayEditButton() {
        return (
            <Tooltip id="tooltip-right" title="Edit" placement="right-end">
                <Link
                    to={`${appContext}/${this.getFormPath(this.props.type)}}` +
                    `/${BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT}` +
                     `/templateGroup/businessRule/${this.props.uuid}`}
                    style={{ textDecoration: 'none' }}
                >
                    <IconButton aria-label="Edit">
                        <EditIcon />
                    </IconButton>
                </Link>
            </Tooltip>
        );
    }

    /**
     * Returns the Delete button
     * @returns {Component}     Tooltip Component, containing IconButton
     */
    displayDeleteButton() {
        return (
            <Tooltip id="tooltip-right" title="Delete" placement="right-end">
                <IconButton aria-label="Delete" onClick={() => this.handleDeleteButtonClick()}>
                    <DeleteIcon />
                </IconButton>
            </Tooltip>
        );
    }

    /**
     * Returns Action Buttons of the business rule
     * @returns {Component}     TableCell, containing action buttons of the business rule
     */
    displayActionButtons() {
        if (this.props.permissions === BusinessRulesConstants.USER_PERMISSIONS.MANAGER) {
            // Manager permissions
            return (
                <TableCell>
                    {this.displayDeploymentInfoButton()}
                    &nbsp;
                    {this.displayViewButton()}
                    &nbsp;
                    {this.displayEditButton()}
                    &nbsp;
                    {this.displayDeleteButton()}
                    &nbsp;
                    {this.displayDeployButton()}
                </TableCell>
            );
        } else {
            // Viewer permissions
            return (
                <TableCell>
                    {this.displayDeploymentInfoButton()}
                    &nbsp;
                    {this.displayViewButton()}
                </TableCell>
            );
        }
    }

    render() {
        return (
            <TableRow>
                <TableCell>
                    {this.props.name}
                </TableCell>
                <TableCell>
                    {BusinessRulesConstants.BUSINESS_RULE_STATUSES[Number(this.props.status)]}
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
    onRedeploy: PropTypes.func.isRequired,
    onDeleteRequest: PropTypes.func.isRequired,
    onDeploymentInfoRequest: PropTypes.func.isRequired,
};
