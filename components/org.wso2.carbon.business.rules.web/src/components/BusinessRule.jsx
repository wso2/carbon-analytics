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
 */

import React from 'react';
// Material UI Components
import IconButton from 'material-ui/IconButton';
import RefreshIcon from 'material-ui-icons/Refresh';
import EditIcon from 'material-ui-icons/Edit';
import DeleteIcon from 'material-ui-icons/Delete';
import {TableCell, TableRow,} from 'material-ui/Table';
import Tooltip from 'material-ui/Tooltip';
import VisibilityIcon from 'material-ui-icons/Visibility';
// App Utilities
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
import BusinessRulesUtilityFunctions from "../utils/BusinessRulesUtilityFunctions";
import BusinessRulesAPICaller from "../utils/BusinessRulesAPICaller";
// CSS
import '../index.css';


// Styles related to this component
const styles = {
    deployButton: {
        color: '#EF6C00'
    },
    hyperlink: {
        cursor: 'pointer',
        color: 'inherit',
        textDecoration: 'inherit',
        ':hover': {
            textDecoration: 'underline',
        },
    },
}

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
            status: props.status,
        }
    }

    /**
     * Views the business rule form in 'view' mode
     */
    viewBusinessRule() {
        BusinessRulesUtilityFunctions.viewBusinessRuleForm(false, this.state.uuid)
    }

    /**
     * Handles onClick action of the 'Re-deploy' button
     */
    handleReDeployButtonClick() { //todo: implement redeploy properly with status code response
        let apis = new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL)
        let redeployPromise = apis.redeployBusinessRule(this.state.uuid).then(
            function (redeployResponse) {
                // BusinessRulesUtilityFunctions.loadBusinessRulesManager(BusinessRulesMessages
                //     .BUSINESS_RULE_REDEPLOY_SUCCESSFUL)
                BusinessRulesUtilityFunctions.loadBusinessRulesManager(redeployResponse.data[1]) //todo: check
                // deployment
                // and remove hardcode
            }
        ).catch(function (error) {
            BusinessRulesUtilityFunctions
                .loadBusinessRulesManager("Failed to deploy business rule '" + this.state.uuid + "'.")
        })
    }

    /**
     * Opens the business rule form in 'edit' mode
     */
    handleEditButtonClick() {
        BusinessRulesUtilityFunctions.viewBusinessRuleForm(true, this.state.uuid)
    }

    /**
     * Sends the API call for deleting this business rule
     */
    handleDeleteButtonClick() {
        this.props.showDeleteDialog(this.state.uuid)
    }

    render() {
        let deploymentStatus = BusinessRulesConstants[this.state.status]
        let retryDeployButton
        switch (this.state.status) {
            case (1) : {
                retryDeployButton =
                    <Tooltip id="tooltip-right" title="Deploy" placement="right-end">
                        <IconButton color="primary" style={styles.deployButton} aria-label="Deploy"
                                    onClick={(e) => this.handleReDeployButtonClick()}>
                            <RefreshIcon/>
                        </IconButton>
                    </Tooltip>
                break;
            }
            case (2) : {
                retryDeployButton =
                    <Tooltip id="tooltip-right" title="Re-Deploy" placement="right-end">
                        <IconButton color="primary" style={styles.deployButton} aria-label="ReDeploy"
                                    onClick={(e) => this.handleReDeployButtonClick()}>
                            <RefreshIcon/>
                        </IconButton>
                    </Tooltip>
                break;
            }
            case (3) : {
                retryDeployButton =
                    <Tooltip id="tooltip-right" title="Retry Un-deploy" placement="right-end">
                        <IconButton color="primary" style={styles.deployButton} aria-label="RetryUndeploy"
                                    onClick={(e) => this.handleReDeployButtonClick()}>
                            <RefreshIcon/>
                        </IconButton>
                    </Tooltip>
                break;
            }
            case (4) : {
                retryDeployButton =
                    <Tooltip id="tooltip-right" title="Re-Deploy" placement="right-end">
                        <IconButton color="primary" style={styles.deployButton} aria-label="ReDeploy"
                                    onClick={(e) => this.handleReDeployButtonClick()}>
                            <RefreshIcon/>
                        </IconButton>
                    </Tooltip>
                break;
            }
        }

        // To show all the action buttons
        let actionButtonsCell =
            <TableCell>
                <Tooltip id="tooltip-right" title="View" placement="right-end">
                    <IconButton aria-label="View" onClick={(e) => this.viewBusinessRule()}>
                        <VisibilityIcon/>
                    </IconButton>
                </Tooltip>
                &nbsp;
                <Tooltip id="tooltip-right" title="Edit" placement="right-end">
                    <IconButton aria-label="Edit" onClick={(e) => this.handleEditButtonClick()}>
                        <EditIcon/>
                    </IconButton>
                </Tooltip>
                &nbsp;
                <Tooltip id="tooltip-right" title="Delete" placement="right-end">
                    <IconButton aria-label="Delete" onClick={(e) => this.handleDeleteButtonClick()}>
                        <DeleteIcon/>
                    </IconButton>
                </Tooltip>
                &nbsp;
                {retryDeployButton}
            </TableCell>


        return (
            <TableRow>
                <TableCell>
                    {this.state.name}
                </TableCell>
                <TableCell>{deploymentStatus}</TableCell>
                {actionButtonsCell}
            </TableRow>
        )
    }
}

export default BusinessRule;
