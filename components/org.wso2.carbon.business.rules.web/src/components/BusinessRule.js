import React from 'react';
import ReactDOM from 'react-dom';
// import './index.css';
// Material-UI
import IconButton from 'material-ui/IconButton';
import RefreshIcon from 'material-ui-icons/Refresh';
import EditIcon from 'material-ui-icons/Edit';
import DeleteIcon from 'material-ui-icons/Delete';
import {TableCell, TableRow,} from 'material-ui/Table';
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
import BusinessRulesFunctions from "../utils/BusinessRulesFunctions";
import BusinessRulesAPIs from "../utils/BusinessRulesAPIs";
import Tooltip from 'material-ui/Tooltip';
import VisibilityIcon from 'material-ui-icons/Visibility';
import BusinessRuleModifier from "./BusinessRuleModifier";
import BusinessRulesMessageStringConstants from "../utils/BusinessRulesMessageStringConstants";

// Styles todo doc comments
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
        BusinessRulesFunctions.viewBusinessRuleForm(false, this.state.uuid) //todo: thisstateuuid is enough
    }

    /**
     * Handles onClick action of the 'Re-deploy' button
     */
    handleReDeployButtonClick() {
        let apis = new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL) // todo instantiate and use. new()
        let redeployPromise = apis.redeployBusinessRule(this.state.uuid).then( // todo remove promise
            function(redeployResponse){
                // BusinessRulesFunctions.loadBusinessRuleModifier(true, BusinessRulesMessageStringConstants
                //     .BUSINESS_RULE_REDEPLOY_SUCCESSFUL)
                BusinessRulesFunctions.loadBusinessRuleModifier(true, BusinessRulesMessageStringConstants
                    .BUSINESS_RULE_REDEPLOY_FAILURE)
            }
        ).catch(function(error){
            BusinessRulesFunctions.loadBusinessRuleModifier(true, BusinessRulesMessageStringConstants
                .BUSINESS_RULE_REDEPLOY_FAILURE) // todo console.error() for all the catches
        })
    }

    /**
     * Opens the business rule form in 'edit' mode
     */
    handleEditButtonClick() {
        BusinessRulesFunctions.viewBusinessRuleForm(true, this.state.uuid)
    }

    /**
     * Sends the API call for deleting this business rule
     */
    handleDeleteButtonClick() {
        this.props.showDeleteDialog(this.state.uuid)

        let apis = new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL);
    }

    render() {
        // To show deployment status and redeploy button
        let deployedStatus
        let redeployButton
        switch(this.state.status) {
            case BusinessRulesConstants.BUSINESS_RULE_STATUS_DEPLOYMENT_FAILED:
                // Deployment failed
                deployedStatus = BusinessRulesConstants.BUSINESS_RULE_STATUS_DEPLOYMENT_FAILED_STRING
                redeployButton =
                    <Tooltip id="tooltip-right" title="Re-Deploy" placement="right-end">
                        <IconButton color="primary" style={styles.deployButton} aria-label="Refresh"
                                    onClick={(e) => this.handleReDeployButtonClick()}>
                            <RefreshIcon/>
                        </IconButton>
                    </Tooltip>
                break;
            case BusinessRulesConstants.BUSINESS_RULE_STATUS_NOT_DEPLOYED:
                // Not deployed
                deployedStatus = BusinessRulesConstants.BUSINESS_RULE_STATUS_NOT_DEPLOYED_STRING
                redeployButton =
                    <Tooltip id="tooltip-right" title="Re-Deploy" placement="right-end">
                        <IconButton color="primary" style={styles.deployButton} aria-label="Refresh"
                                    onClick={(e) => this.handleReDeployButtonClick()}>
                            <RefreshIcon/>
                        </IconButton>
                    </Tooltip>
                break;
            default:
                // Deployed
                deployedStatus = BusinessRulesConstants.BUSINESS_RULE_STATUS_DEPLOYED_STRING
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
                {redeployButton}
            </TableCell>


        return (
            <TableRow>
                <TableCell>
                        {this.state.name}
                </TableCell>
                <TableCell>{deployedStatus}</TableCell>
                {actionButtonsCell}
            </TableRow>
        )
    }
}

export default BusinessRule;
