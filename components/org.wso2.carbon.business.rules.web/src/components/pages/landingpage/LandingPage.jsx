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
import { Link } from 'react-router-dom';
// Material UI Components
import Typography from 'material-ui/Typography';
import Table, {
  TableBody,
  TableCell,
  TableHead,
  TableRow,
} from 'material-ui/Table';
import Button from 'material-ui/Button';
import AddIcon from 'material-ui-icons/Add';
import Dialog, {
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from 'material-ui/Dialog';
import Paper from 'material-ui/Paper';
import Snackbar from 'material-ui/Snackbar';
import Slide from 'material-ui/transitions/Slide';
// Localization
import { FormattedMessage } from 'react-intl';
// App Components
import BusinessRule from './elements/BusinessRule';
import DeploymentInfo from './elements/DeploymentInfo';
import Header from '../../common/Header';
import ProgressDisplay from '../../common/ProgressDisplay';
import ErrorDisplay from '../../common/error/ErrorDisplay';
// App Utils
import BusinessRulesUtilityFunctions from '../../../utils/BusinessRulesUtilityFunctions';
// App APIs
import BusinessRulesAPI from '../../../api/BusinessRulesAPI';
// App Constants
import BusinessRulesConstants from '../../../constants/BusinessRulesConstants';
// Styles
import Styles from '../../../style/Styles';
import '../../../index.css';

/**
 * Styles related to this component
 */
const styles = {
  container: {
    maxWidth: 1020,
  },
  floatingButton: {
    position: 'fixed',
    right: '20%',
    zIndex: 9,
    top: '10%',
  },
};

/**
 * App context.
 */
const appContext = window.contextPath;

/**
 * Represents the landing page that displays the list of existing business rules (if any),
 * and allows to view, edit, delete and re-deploy a business rule among the list,
 * or to create a new business rule
 */
export default class LandingPage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            permissions: BusinessRulesConstants.USER_PERMISSIONS.UNSET,
            businessRules: [], // Available Business Rules
            displaySnackbar: false,
            snackbarMessage: '',

            // Dialog for deleting a business rule
            deleteDialog: {
                businessRule: {},
                businessRuleIndex: -1,
                isVisible: false,
            },

            // Deployment Info of a business rule
            deploymentInfoDialog: {
                isVisible: false,
                info: {},
                businessRule: {},
            },

            // Loaded state of page and related Error code
            hasLoaded: false,
            errorCode: BusinessRulesConstants.ERROR_CODES.UNKNOWN,
        };
    }

    componentDidMount() {
        this.loadAvailableBusinessRules();
    }

    /**
     * Loads available business rules from the database
     */
    loadAvailableBusinessRules() {
        new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
            .getBusinessRules()
            .then((response) => {
                this.setState({
                    permissions: response.data[3],
                    businessRules: response.data[2],
                    hasLoaded: true,
                    errorCode: BusinessRulesConstants.ERROR_CODES.NONE,
                });
            })
            .catch((error) => {
                this.setState({
                    hasLoaded: true,
                    errorCode: BusinessRulesUtilityFunctions.getErrorDisplayCode(error),
                });
            });
    }

    /**
     * Checks whether the given error has been occurred due to authorization,
     * and updates the error code necessarily in the state, if so
     * @param {Object} error     Error object
     */
    checkAuthorizationOnError(error) {
        if (error.response.status === 401) {
            this.setState({
                errorCode: BusinessRulesUtilityFunctions.getErrorDisplayCode(error),
            });
        }
    }

    /**
     * Updates the status of the business rule - which is denoted with the given index in the array from the state,
     * with the given status
     * @param {number} businessRuleIndex     Index of the business rule in the array, in the state
     * @param {number} status                Status of the business rule
     */
    updateBusinessRuleStatus(businessRuleIndex, status) {
        let businessRules = this.state.businessRules;
        businessRules[businessRuleIndex][1] = status;
        this.setState({ businessRules });
    }

    /**
     * Gets the status code of a business rule, based on the given response data
     * @param {Array} responseData      Response data array which is either from a Response or an Error
     * @returns {number}                Status code for the business rule
     */
    getBusinessRuleStatusCode(responseData) {
        if (Array.isArray(responseData[2])) {
            return BusinessRulesConstants.BUSINESS_RULE_STATUSES[4];
        }
        if (responseData[2] === BusinessRulesConstants.SCRIPT_EXECUTION_ERROR) {
            return BusinessRulesConstants.BUSINESS_RULE_STATUSES[5];
        }
        return responseData[2];
    }

    /**
     * Re-deploys the business rule, that has the given UUID
     * @param {String} businessRuleUUID     UUID of the business rule
     * @param businessRuleIndex             Index of the business rule in the array, in the state
     */
    redeployBusinessRule(businessRuleUUID, businessRuleIndex) {
        new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
            .redeployBusinessRule(businessRuleUUID)
            .then((redeployResponse) => {
                this.toggleSnackbar(redeployResponse.data[1]);
                this.updateBusinessRuleStatus(businessRuleIndex, this.getBusinessRuleStatusCode(redeployResponse.data));
            })
            .catch((error) => {
                this.checkAuthorizationOnError(error);
                this.toggleSnackbar(
                    <FormattedMessage
                        id="landing.failedToDeploy"
                        defaultMessage="Failed to deploy the business rule {businessRuleUUID}"
                        values={{ businessRuleUUID }}
                    />,
                );
                if (error.response.data) {
                    this.updateBusinessRuleStatus(
                        businessRuleIndex, this.getBusinessRuleStatusCode(error.response.data));
                } else {
                    this.updateBusinessRuleStatus(businessRuleIndex, BusinessRulesConstants.BUSINESS_RULE_STATUSES[5]);
                }
            });
    }

    /**
     * Un-deploys the business rule, that has the given UUID
     * @param {String} businessRuleUUID     UUID of the business rule
     * @param {number} businessRuleIndex    Index of the business rule in the array, in the state
     */
    undeployBusinessRule(businessRuleUUID, businessRuleIndex) {
        new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
            .undeployBusinessRule(businessRuleUUID)
            .then((undeployResponse) => {
                this.toggleSnackbar(undeployResponse.data[1]);
                this.updateBusinessRuleStatus(businessRuleIndex, this.getBusinessRuleStatusCode(undeployResponse.data));
            })
            .catch((error) => {
                this.checkAuthorizationOnError(error);
                this.toggleSnackbar(`Failed to un-deploy the business rule '${businessRuleUUID}'`);
                if (error.response.data) {
                    this.updateBusinessRuleStatus(
                        businessRuleIndex, this.getBusinessRuleStatusCode(error.response.data));
                } else {
                    this.updateBusinessRuleStatus(businessRuleIndex, BusinessRulesConstants.BUSINESS_RULE_STATUSES[5]);
                }
            });
    }
    
    /**
     * Deletes the business rule, that has the given UUID
     * @param {String} businessRuleUUID     UUID of the business rule
     * @param {number} businessRuleIndex    Index of the business rule in the array, in the state
     */
    deleteBusinessRule(businessRuleUUID, businessRuleIndex) {
        this.toggleDeleteDialog();
        new BusinessRulesAPI(BusinessRulesConstants.BASE_URL).deleteBusinessRule(businessRuleUUID, false)
            .then((deleteResponse) => {
                this.toggleSnackbar(deleteResponse.data[1]);
                this.updateBusinessRuleStatus(businessRuleIndex, this.getBusinessRuleStatusCode(deleteResponse.data));
                this.loadAvailableBusinessRules();
            })
            .catch((error) => {
                this.checkAuthorizationOnError(error);
                this.toggleSnackbar(
                    <FormattedMessage
                        id="landing.failedToDeleteRule"
                        defaultMessage="Failed to delete the business rule "
                        values={{
                            businessRuleUUID,
                        }}
                    />,
                );
                if (error.response.data) {
                    this.updateBusinessRuleStatus(
                        businessRuleIndex, this.getBusinessRuleStatusCode(error.response.data));
                } else {
                    this.updateBusinessRuleStatus(businessRuleIndex, BusinessRulesConstants.BUSINESS_RULE_STATUSES[5]);
                }
            });
    }

    /**
     * Displays the deployment information of the given business rule
     * @param {Object} businessRule     Business Rule object
     */
    showDeploymentInfo(businessRule) {
        new BusinessRulesAPI(BusinessRulesConstants.BASE_URL).getDeploymentInfo(businessRule[0].uuid)
            .then((response) => {
                const state = this.state;
                state.deploymentInfoDialog.info = response.data[2];
                state.deploymentInfoDialog.businessRule = businessRule;
                this.setState(state);
                this.toggleDeploymentInfoView();
            })
            .catch(() => {
                this.toggleSnackbar(
                    <FormattedMessage
                        id="landing.unableToRetreve"
                        defaultMessage="Unable to retrieve deployment info"
                    />,
                );
            });
    }

    /**
     * Toggles the visibility of deployment info
     */
    toggleDeploymentInfoView() {
        const state = this.state;
        state.deploymentInfoDialog.isVisible = !state.deploymentInfoDialog.isVisible;
        this.setState(state);
    }

    /**
     * Displays the snackbar with the given message (if any), otherwise hides it
     * @param {String} message      Snackbar message
     */
    toggleSnackbar(message) {
        if (message) {
            this.setState({
                displaySnackbar: true,
                snackbarMessage: message,
            });
        } else {
            this.setState({
                displaySnackbar: false,
            });
        }
    }

    /**
     * Displays the delete dialog for the business rule with the given UUID (if any),
     * otherwise hides it
     * @param {Object} businessRule         Business rule object
     * @param {number} businessRuleIndex    Index of the business rule in the array, in the state
     */
    toggleDeleteDialog(businessRule, businessRuleIndex) {
        const state = this.state;
        if (businessRule) {
            state.deleteDialog.businessRule = businessRule;
            state.deleteDialog.businessRuleIndex = businessRuleIndex;
            state.deleteDialog.isVisible = true;
        } else {
            state.deleteDialog.businessRule = {};
            state.deleteDialog.isVisible = false;
        }
        this.setState(state);
    }

    /**
     * Returns message for getting started
     * @returns {Component}     Component with the message for getting started
     */
    displayGetStarted() {
        if (
            this.state.permissions === BusinessRulesConstants.USER_PERMISSIONS.MANAGER
        ) {
            return (
                <Paper style={Styles.messageContainer}>
                    <Typography type="title">
                        <FormattedMessage
                            id="landing.title"
                            defaultMessage="No business rules found"
                        />
                    </Typography>
                    <Typography type="subheading">
                        <FormattedMessage
                            id="landing.subheading"
                            defaultMessage=" Get started by creating one"
                        />
                    </Typography>
                    <br />
                    <Link
                        to={`${appContext}/businessRuleCreator`}
                        style={{ textDecoration: 'none' }}
                    >
                        <Button raised color="primary">
                            <FormattedMessage id="landing.create" defaultMessage="Create" />
                        </Button>
                    </Link>
                </Paper>
            );
        }
        return (
            <Paper style={Styles.messageContainer}>
                <Typography type="title">
                    <FormattedMessage
                        id="landing.title"
                        defaultMessage="No business rules found"
                    />
                </Typography>
                <Typography type="subheading">
                    <FormattedMessage
                        id="landing.permission.subheading"
                        defaultMessage="Login with suitable permissions to create one"
                    />
                </Typography>
                <br />
                <Link to={`${appContext}/logout`} style={{ textDecoration: 'none' }}>
                    <Button color="primary">
                        <FormattedMessage id="login.title" defaultMessage="Login" />
                    </Button>
                </Link>
            </Paper>
        );
    }

    /**
     * Displays a list of business rules when at least one is available,
     * otherwise, a message for creation
     * @returns {HTMLElement}       Div containing available business rules, or creation message
     */
    displayAvailableBusinessRules() {
        if (this.state.businessRules && this.state.businessRules.length > 0) {
            return (
                <div style={styles.container}>
                    {(this.state.permissions === BusinessRulesConstants.USER_PERMISSIONS.MANAGER) ?
                        (this.displayCreateFloatButton()) : (null)}
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>
                                    <FormattedMessage
                                        id="landing.table.businessrule"
                                        defaultMessage="Business Rule"
                                    />
                                </TableCell>
                                <TableCell>
                                    <FormattedMessage
                                        id="landing.table.status"
                                        defaultMessage="Status"
                                    />
                                </TableCell>
                                <TableCell>
                                    <FormattedMessage
                                        id="landing.table.actions"
                                        defaultMessage="Actions"
                                    />
                                </TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {this.state.businessRules.map((businessRuleAndStatus, index) =>
                                (<BusinessRule
                                    key={businessRuleAndStatus[0].uuid}
                                    name={businessRuleAndStatus[0].name}
                                    uuid={businessRuleAndStatus[0].uuid}
                                    type={businessRuleAndStatus[0].type}
                                    status={businessRuleAndStatus[1]}
                                    permissions={this.state.permissions}
                                    onRedeployRequest={() =>
                                        this.redeployBusinessRule(businessRuleAndStatus[0].uuid, index)}
                                    onDeleteRequest={() => this.toggleDeleteDialog(businessRuleAndStatus[0], index)}
                                    onUndeployRequest={() =>
                                        this.undeployBusinessRule(businessRuleAndStatus[0].uuid, index)}
                                    onDeploymentInfoRequest={() => this.showDeploymentInfo(businessRuleAndStatus)}
                                  />
                                ))}
                        </TableBody>
                    </Table>
                </div>
            );
        }
        return this.displayGetStarted();
    }

    /**
     * Displays the create button, for creating a business rule
     * @returns {Component}       Create Button
     */
    displayCreateFloatButton() {
        return (
            <div style={styles.floatingButton}>
                <Link to={`${appContext}/businessRuleCreator`} style={{ textDecoration: 'none' }}>
                    <Button fab color="primary" aria-label="Add">
                        <AddIcon />
                    </Button>
                </Link>
            </div>
        );
    }

    /**
     * Returns Snackbar component
     * @returns {Component}         Snackbar Component
     */
    displaySnackbar() {
        return (
            <Snackbar
                autoHideDuration={3500}
                open={this.state.displaySnackbar}
                onRequestClose={() => this.toggleSnackbar()}
                transition={<Slide direction={Styles.snackbar.direction} />}
                SnackbarContentProps={{
                    'aria-describedby': 'snackbarMessage',
                }}
                message={
                    <span id="snackbarMessage">
                        {this.state.snackbarMessage}
                    </span>
                }
            />
        );
    }

    /**
     * Returns Delete Confirmation dialog
     * @returns {Component}         Dialog Component
     */
    displayDeleteConfirmationDialog() {
        if (!BusinessRulesUtilityFunctions.isEmpty(this.state.deleteDialog.businessRule)) {
            return (
                <Dialog open={this.state.deleteDialog.isVisible} onRequestClose={() => this.toggleDeleteDialog()}>
                    <DialogTitle>
                        <FormattedMessage
                            id="landing.confrimDelete"
                            defaultMessage="Confirm Delete"
                        />
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText>
                            <FormattedMessage
                                id="landing.confirmDelete"
                                defaultMessage="Do you really want to delete the business rule {ruleName} "
                                values={{
                                    ruleName: this.state.deleteDialog.businessRule.name,
                                }}
                            />
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Button
                            onClick={() =>
                                this.deleteBusinessRule(
                                    this.state.deleteDialog.businessRule.uuid,
                                    this.state.deleteDialog.businessRuleIndex)}
                        >
                            <FormattedMessage id="landing.delete" defaultMessage="Delete" />
                        </Button>
                    </DialogActions>
                </Dialog>
            );
        }
        return null;
    }

    /**
     * Returns Deployment Info dialog
     * @returns {Component}     DeploymentInfo component
     */
    displayDeploymentInfoDialog() {
        return (
            <DeploymentInfo
                businessRule={this.state.deploymentInfoDialog.businessRule}
                info={this.state.deploymentInfoDialog.info}
                open={this.state.deploymentInfoDialog.isVisible}
                onRequestClose={() => this.toggleDeploymentInfoView()}
            />
        );
    }

  /**
   * Displays content of the page
   * @returns {HTMLElement}       Content of the page
   */
  displayContent() {
    if (this.state.hasLoaded) {
      if (this.state.errorCode === BusinessRulesConstants.ERROR_CODES.NONE) {
        return (
          <div>
            {this.displaySnackbar()}
            {this.displayDeleteConfirmationDialog()}
            {this.displayDeploymentInfoDialog()}
            <center>
              <div>
                {this.state.businessRules.length > 0 ? (
                  <Typography type="headline">
                    <FormattedMessage
                      id="landing.businessRules.heading"
                      defaultMessage="Business Rules"
                    />
                  </Typography>
                ) : (
                    <div />
                  )}
              </div>
              <br />
              {this.displayAvailableBusinessRules()}
            </center>
          </div>
        );
      }
      return <ErrorDisplay errorCode={this.state.errorCode} />;

    }
      return <ProgressDisplay />;

  }

    render() {
        return (
            <div>
                <Header hideHomeButton />
                <br />
                <br />
                {this.displayContent()}
            </div>
        );
    }
}
