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
import Table, { TableBody, TableCell, TableHead, TableRow } from 'material-ui/Table';
import Button from 'material-ui/Button';
import AddIcon from 'material-ui-icons/Add';
import Dialog, { DialogActions, DialogContent, DialogContentText, DialogTitle } from 'material-ui/Dialog';
import Paper from 'material-ui/Paper';
import Snackbar from 'material-ui/Snackbar';
import Slide from 'material-ui/transitions/Slide';
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
     * Re-deploys the business rule, that has the given UUID
     * @param {String} businessRuleUUID     UUID of the business rule
     */
    redeployBusinessRule(businessRuleUUID) {
        new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
            .redeployBusinessRule(businessRuleUUID)
            .then((redeployResponse) => {
                this.toggleSnackbar(redeployResponse.data[1]);
                this.loadAvailableBusinessRules();
            })
            .catch(() => {
                this.toggleSnackbar(`Failed to deploy the business rule '${businessRuleUUID}'`);
                this.loadAvailableBusinessRules();
            });
    }

    /**
     * Deletes the business rule, that has the given UUID
     * @param {String} businessRuleUUID     UUID of the business rule
     */
    deleteBusinessRule(businessRuleUUID) {
        this.toggleDeleteDialog();
        new BusinessRulesAPI(BusinessRulesConstants.BASE_URL).deleteBusinessRule(businessRuleUUID, false)
            .then((deleteResponse) => {
                this.toggleSnackbar(deleteResponse.data[1]);
                this.loadAvailableBusinessRules();
            })
            .catch(() => {
                this.toggleSnackbar(`Failed to delete the business rule '${businessRuleUUID}'`);
                this.loadAvailableBusinessRules();
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
                this.toggleSnackbar('Unable to retrieve deployment info');
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
     * @param {Object} businessRule     Business rule object
     */
    toggleDeleteDialog(businessRule) {
        const state = this.state;
        if (businessRule) {
            state.deleteDialog.businessRule = businessRule;
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
        if (this.state.permissions === BusinessRulesConstants.USER_PERMISSIONS.MANAGER) {
            return (
                <Paper style={Styles.messageContainer}>
                    <Typography type="title">
                        No business rules found
                    </Typography>
                    <Typography type="subheading">
                        Get started by creating one
                    </Typography>
                    <br />
                    <Link to={`${appContext}/businessRuleCreator`} style={{ textDecoration: 'none' }}>
                        <Button raised color="primary">
                            Create
                        </Button>
                    </Link>
                </Paper>
            );
        } else {
            return (
                <Paper style={Styles.messageContainer}>
                    <Typography type="title">
                        No business rules found
                    </Typography>
                    <Typography type="subheading">
                        Login with suitable permissions to create one
                    </Typography>
                    <br />
                    <Link to={`${appContext}/logout`} style={{ textDecoration: 'none' }}>
                        <Button color="primary">
                            Login
                        </Button>
                    </Link>
                </Paper>
            );
        }
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
                                <TableCell>Business Rule</TableCell>
                                <TableCell>Status</TableCell>
                                <TableCell>Actions</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {this.state.businessRules.map(businessRuleAndStatus =>
                                (<BusinessRule
                                    key={businessRuleAndStatus[0].uuid}
                                    name={businessRuleAndStatus[0].name}
                                    uuid={businessRuleAndStatus[0].uuid}
                                    type={businessRuleAndStatus[0].type}
                                    status={businessRuleAndStatus[1]}
                                    permissions={this.state.permissions}
                                    onRedeploy={() => this.redeployBusinessRule(businessRuleAndStatus[0].uuid)}
                                    onDeleteRequest={() => this.toggleDeleteDialog(businessRuleAndStatus[0])}
                                    onDeploymentInfoRequest={() => this.showDeploymentInfo(businessRuleAndStatus)}
                                />))}
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
                        Confirm Delete
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText>
                            {'Do you really want to delete the business rule ' +
                            `'${this.state.deleteDialog.businessRule.name}'?`}
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Button
                            onClick={() => this.deleteBusinessRule(this.state.deleteDialog.businessRule.uuid)}
                        >
                            Delete
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
                                {(this.state.businessRules.length > 0) ?
                                    (<Typography type="headline">
                                        Business Rules
                                    </Typography>) :
                                    (<div />)
                                }
                            </div>
                            <br />
                            {this.displayAvailableBusinessRules()}
                        </center>
                    </div>
                );
            } else {
                return <ErrorDisplay errorCode={this.state.errorCode} />;
            }
        } else {
            return <ProgressDisplay />;
        }
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
