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
import BusinessRule from './elements/landingpage/BusinessRule';
import DeploymentInfo from './elements/landingpage/DeploymentInfo';
import Header from '../common/Header';
import ProgressDisplay from '../common/ProgressDisplay';
import ErrorDisplay from './elements/error/ErrorDisplay';
// App Constants
import BusinessRulesMessages from '../../constants/BusinessRulesMessages';
import BusinessRulesConstants from '../../constants/BusinessRulesConstants';
// App APIs
import BusinessRulesAPI from '../../api/BusinessRulesAPI';
// CSS
import '../../index.css';
import BusinessRulesUtilityFunctions from '../../utils/BusinessRulesUtilityFunctions';

/**
 * Styles related to this component
 */
const styles = {
    container: {
        maxWidth: 1020,
    },
    paper: {
        maxWidth: 400,
        paddingTop: 30,
        paddingBottom: 30,
    },
    card: {
        width: 345,
        height: 200,
        margin: 15,
    },
    chip: {
        margin: 5,
    },
    spacing: '0',
    snackbar: {
        direction: 'up',
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

            // To show dialog when deleting a business rule
            displayDeleteDialog: false,
            businessRuleUUIDToBeDeleted: '',

            // Business rule Deployment Info
            displayDeploymentInfo: false,
            deploymentInfo: {},
            deploymentInfoRequestedBusinessRule: {}, // TODO move inside deploymentInfo{} if possible

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
                this.setState({
                    deploymentInfo: response.data[2],
                    deploymentInfoRequestedBusinessRule: businessRule, // TODO bring that inside
                });
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
        this.setState({ displayDeploymentInfo: !this.state.displayDeploymentInfo });
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
     * @param {String} businessRuleUUID     UUID of the business rule
     */
    toggleDeleteDialog(businessRuleUUID) {
        if (businessRuleUUID) {
            this.setState({
                businessRuleUUIDToBeDeleted: businessRuleUUID,
                displayDeleteDialog: true,
            });
        } else {
            this.setState({
                displayDeleteDialog: false,
            });
        }
    }

    /**
     * Returns message for getting started
     * @returns {Component}     Component with the message for getting started
     */
    displayGetStarted() {
        if (this.state.permissions === BusinessRulesConstants.USER_PERMISSIONS.MANAGER) {
            return (
                <Paper style={styles.paper}>
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
                <Paper style={styles.paper}>
                    <Typography type="title">
                        No business rules found
                    </Typography>
                    <Typography type="subheading">
                        Login with suitable permissions to create one
                    </Typography>
                </Paper>
            );
        }
    }

    /**
     * Displays a list of business rules when at least one is available,
     * otherwise, a message for creation
     * @returns {Element}       Div containing available business rules, or creation message
     */
    displayAvailableBusinessRules() {
        const isNoneAvailable = !(this.state.businessRules && this.state.businessRules.length > 0);

        if (!isNoneAvailable) {
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
                            {this.state.businessRules.map(businessRule =>
                                (<BusinessRule
                                    key={businessRule[0].uuid}
                                    name={businessRule[0].name}
                                    uuid={businessRule[0].uuid}
                                    type={businessRule[0].type}
                                    status={businessRule[1]}
                                    permissions={this.state.permissions}
                                    redeploy={uuid => this.redeployBusinessRule(uuid)}
                                    showDeleteDialog={uuid => this.toggleDeleteDialog(uuid)}
                                    showDeploymentInfo={() => this.showDeploymentInfo(businessRule)}
                                />))}
                        </TableBody>
                    </Table>
                </div>
            );
        } else {
            return this.displayGetStarted();
        }
    }

    /**
     * Displays the create button, for creating a business rule
     * @returns {Component}       Create Button
     */
    displayCreateFloatButton() {
        return (
            <Link to={`${appContext}/businessRuleCreator`} style={{ textDecoration: 'none' }}>
                <Button fab color="primary" style={{ float: 'right' }} aria-label="Add">
                    <AddIcon />
                </Button>
            </Link>
        );
    }

    /**
     * Displays content of the page
     * @returns {Element}       Content of the page
     */
    displayContent() {
        if (this.state.hasLoaded) {
            if (this.state.errorCode === BusinessRulesConstants.ERROR_CODES.NONE) {
                // Show snackbar with response message, when this page is rendered after a form submission
                const snackbar =
                    (<Snackbar
                        autoHideDuration={3500}
                        open={this.state.displaySnackbar}
                        onRequestClose={() => this.toggleSnackbar()}
                        transition={<Slide direction={styles.snackbar.direction} />}
                        SnackbarContentProps={{
                            'aria-describedby': 'snackbarMessage',
                        }}
                        message={
                            <span id="snackbarMessage">
                                {this.state.snackbarMessage}
                            </span>
                        }
                    />);

                const deleteConfirmationDialog =
                    (<Dialog open={this.state.displayDeleteDialog} onRequestClose={() => this.toggleDeleteDialog()}>
                        <DialogTitle>
                            {BusinessRulesMessages.BUSINESS_RULE_DELETION_CONFIRMATION_TITLE}
                        </DialogTitle>
                        <DialogContent>
                            <DialogContentText>
                                {BusinessRulesMessages.BUSINESS_RULE_DELETION_CONFIRMATION_CONTENT}
                            </DialogContentText>
                        </DialogContent>
                        <DialogActions>
                            <Button onClick={() => this.deleteBusinessRule(this.state.businessRuleUUIDToBeDeleted)}>
                                Delete
                            </Button>
                        </DialogActions>
                    </Dialog>);

                return (
                    <div>
                        {snackbar}
                        {deleteConfirmationDialog}
                        <DeploymentInfo
                            businessRule={this.state.deploymentInfoRequestedBusinessRule}
                            info={this.state.deploymentInfo}
                            open={this.state.displayDeploymentInfo}
                            onRequestClose={() => this.toggleDeploymentInfoView()}
                        />
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
