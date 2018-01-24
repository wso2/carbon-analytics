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
import BusinessRule from './BusinessRule';
import Header from './common/Header';
import ProgressDisplay from './ProgressDisplay';
// App Constants
import BusinessRulesMessages from '../constants/BusinessRulesMessages';
import BusinessRulesConstants from '../constants/BusinessRulesConstants';
// App APIs
import BusinessRulesAPICaller from '../api/BusinessRulesAPICaller';
// CSS
import '../index.css';
// Custom Theme
import { createMuiTheme, MuiThemeProvider } from 'material-ui/styles';
import { Orange } from '../theme/BusinessRulesManagerColors';

const theme = createMuiTheme({
    palette: {
        primary: Orange,
    },
});

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
        paddingBottom: 30
    },
    snackbar: {
        direction: 'up'
    },
};

/**
 * App context.
 */
const appContext = window.contextPath;

/**
 * Allows to select a Business Rule among Business Rules displayed as table rows
 * and view, edit, delete or re-deploy (when not deployed already) each;
 * Or to create a new business rule
 */
class BusinessRulesManager extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            permissions: -1, // 0: manager, 1: viewer, -1: waiting for permission
            businessRules: [], // Available Business Rules

            // To show the snackbar, after deployment / save
            displaySnackbar: this.props.displaySnackbar,
            snackbarMessage: this.props.snackbarMessage,

            // To show dialog when deleting a business rule
            displayDeleteDialog: false,
            businessRuleUUIDToBeDeleted: '',
        };
    }

    componentDidMount() {
        this.loadAvailableBusinessRules();
    }

    /**
     * Loads available business rules from the database, to the state
     */
    loadAvailableBusinessRules() {
        new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL).getBusinessRules().then((response) => {
            this.setState({
                permissions: response.data[3],
                businessRules: response.data[2]
            })
        }).catch((error) => {
            if (error.response.status === 401) {
                this.displaySnackBar('You do not have enough permissions to view business rules');
                this.setState({permissions: 1});
            }
        });
    }

    /**
     * Re-deploys the business rule with the given uuid
     *
     * @param uuid
     */
    redeployBusinessRule(uuid) {
        new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL).redeployBusinessRule(uuid).then(
            (redeployResponse) => {
                this.displaySnackBar(redeployResponse.data[1]);
                this.loadAvailableBusinessRules();
            }
        ).catch(() => {
            this.displaySnackBar("Failed to deploy business rule '" + uuid + "'");
            this.loadAvailableBusinessRules();
        });
    }

    /**
     * Sends request to the API, to delete a specific business rule
     *
     * @param businessRuleUUID
     */
    deleteBusinessRule(businessRuleUUID) {
        this.dismissDeleteDialog();
        new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL).deleteBusinessRule(businessRuleUUID, false)
            .then((deleteResponse) => {
                this.displaySnackBar(deleteResponse.data[1]);
                this.loadAvailableBusinessRules();
            }).catch(() => {
            this.displaySnackBar("Failed to delete the business rule '" + businessRuleUUID + "'");
            this.loadAvailableBusinessRules();
        });
    }

    /**
     * Displays snackbar with the given message
     *
     * @param message
     */
    displaySnackBar(message) {
        this.setState({
            displaySnackbar: true,
            snackbarMessage: message
        });
    }

    /**
     * Closes the snackbar
     */
    dismissSnackbar() {
        this.setState({displaySnackbar: false});
    };

    /**
     * Opens the delete confirmation dialog, for deleting the business rule with the given UUID
     *
     * @param businessRuleUUID
     */
    displayDeleteDialog(businessRuleUUID) {
        let state = this.state;
        state.businessRuleUUIDToBeDeleted = businessRuleUUID;
        state.displayDeleteDialog = true;
        this.setState(state);
    }

    /**
     * Closes the dialog
     */
    dismissDeleteDialog() {
        this.setState({displayDeleteDialog: false});
    }

    /**
     * Displays list of Business Rules when available, or message for creation when not
     */
    displayAvailableBusinessRules() {
        // Check whether business rules are available
        let isNoneAvailable = true;
        if (this.state.businessRules && this.state.businessRules.length > 0) {
            isNoneAvailable = false;
        }

        if (this.state.permissions !== -1) {
            if (!isNoneAvailable) {
                // Business rules are available
                // Show available business rules
                let businessRules = this.state.businessRules.map((businessRule) =>
                    <BusinessRule
                        key={businessRule[0].uuid}
                        name={businessRule[0].name}
                        uuid={businessRule[0].uuid}
                        type={businessRule[0].type}
                        status={businessRule[1]}
                        permissions={this.state.permissions}
                        redeploy={(uuid) => this.redeployBusinessRule(uuid)}
                        showDeleteDialog={(uuid) => this.displayDeleteDialog(uuid)}
                    />
                );

                return (
                    <div style={styles.container}>
                        {(this.state.permissions === 0) ?
                            (<Link to={`${appContext}/businessRuleCreator`} style={{textDecoration: 'none'}}>
                                <Button fab color="primary" style={{float: 'right'}} aria-label="Add">
                                    <AddIcon/>
                                </Button>
                            </Link>) :
                            (null)}
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Business Rule</TableCell>
                                    <TableCell>Status</TableCell>
                                    <TableCell>Actions</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {businessRules}
                            </TableBody>
                        </Table>
                    </div>
                )
            } else {
                if (this.state.permissions === 0) {
                    // Manager
                    // Show message for creation
                    return (
                        <div>
                            <Paper style={styles.paper}>
                                <Typography type="title">
                                    No business rules found
                                </Typography>
                                <Typography type="subheading">
                                    Get started by creating one
                                </Typography>
                                <br/>
                                <Link to={`${appContext}/businessRuleCreator`} style={{textDecoration: 'none'}}>
                                    <Button raised color="primary">
                                        Create
                                    </Button>
                                </Link>
                            </Paper>
                        </div>
                    );
                } else {
                    // Viewer
                    // Deny creation
                    return (
                        <div>
                            <Paper style={styles.paper}>
                                <Typography type="title">
                                    Access Denied
                                </Typography>
                                <Typography type="subheading">
                                    Please login with valid permissions
                                </Typography>
                            </Paper>
                        </div>
                    );
                }
            }
        } else {
            // Show Loading progress
            return (<ProgressDisplay />);
        }
    }

    render() {
        // Show snackbar with response message, when this page is rendered after a form submission
        const snackbar =
            (<Snackbar
                autoHideDuration={3500}
                open={this.state.displaySnackbar}
                onRequestClose={() => this.dismissSnackbar()}
                transition={<Slide direction={styles.snackbar.direction}/>}
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
            (<Dialog open={this.state.displayDeleteDialog} onRequestClose={() => this.dismissDeleteDialog()}>
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
            <MuiThemeProvider theme={theme}>
                <Header hideHomeButton />
                <br />
                <div>
                    {snackbar}
                    {deleteConfirmationDialog}
                    <center>
                        <br />
                        <div>
                            {(this.state.businessRules.length > 0) ?
                                (<Typography type="headline">
                                    Business Rules
                                </Typography>) :
                                (<div />)
                            }
                        </div>
                        <br/>
                        {this.displayAvailableBusinessRules()}
                    </center>
                </div>
            </MuiThemeProvider>
        )
    }
}

export default BusinessRulesManager;
