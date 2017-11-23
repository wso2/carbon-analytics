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
import {Link} from 'react-router-dom';
// Material UI Components
import Typography from 'material-ui/Typography';
import Table, {TableBody, TableCell, TableHead, TableRow,} from 'material-ui/Table';
import Button from "material-ui/Button";
import AddIcon from "material-ui-icons/Add";
import Dialog, {DialogActions, DialogContent, DialogContentText, DialogTitle,} from 'material-ui/Dialog';
import Paper from 'material-ui/Paper';
import Snackbar from 'material-ui/Snackbar';
import Slide from 'material-ui/transitions/Slide';
import MaterialSwitch from 'material-ui/Switch';
import {FormControlLabel} from 'material-ui/Form';
// App Components
import BusinessRule from "./BusinessRule";
// App Utilities
import BusinessRulesUtilityFunctions from "../utils/BusinessRulesUtilityFunctions";
// App Constants
import BusinessRulesMessages from "../constants/BusinessRulesMessages";
import BusinessRulesConstants from "../constants/BusinessRulesConstants";
// App APIs
import BusinessRulesAPICaller from "../api/BusinessRulesAPICaller";
// CSS
import '../index.css';
// Custom Theme
import {createMuiTheme, MuiThemeProvider} from 'material-ui/styles';
import {Orange} from '../theme/BusinessRulesManagerColors';

const theme = createMuiTheme({
    palette: {
        primary: Orange,
    },
});

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
}

/**
 * Allows to select a Business Rule among Business Rules displayed as table rows
 * and view, edit, delete or re-deploy (when not deployed already) each;
 * Or to create a new business rule
 */
class BusinessRulesManager extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            businessRules: [], // Available Business Rules

            // To show the snackbar, after deployment / save
            displaySnackbar: this.props.displaySnackbar,
            snackbarMessage: this.props.snackbarMessage,

            // To show dialog when deleting a business rule
            displayDeleteDialog: false,
            businessRuleUUIDToBeDeleted: '',
            forceDeleteBusinessRule: false
        }
    }

    componentDidMount() {
        this.loadAvailableBusinessRules();
    }

    /**
     * Loads available business rules from the database, to the state
     */
    loadAvailableBusinessRules() {
        let that = this;
        let businessRulesPromise = BusinessRulesUtilityFunctions.getBusinessRules()
        businessRulesPromise.then(function (response) {
            that.setState({
                businessRules: response.data[2]
            })
        })
    }

    /**
     * Re-deploys the business rule with the given uuid
     *
     * @param uuid
     */
    redeployBusinessRule(uuid) {
        let that = this;
        let apis = new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL);
        let redeployPromise = apis.redeployBusinessRule(uuid).then(
            function (redeployResponse) {
                that.displaySnackBar(redeployResponse.data[1]);
                that.loadAvailableBusinessRules();
            }
        ).catch(function (error) {
            that.displaySnackBar("Failed to deploy business rule '" + uuid + "'");
            that.loadAvailableBusinessRules();
        });
    }

    /**
     * Sends request to the API, to delete a specific business rule
     *
     * @param businessRuleUUID
     * @param forceDeleteStatus
     */
    deleteBusinessRule(businessRuleUUID, forceDeleteStatus) {
        let that = this;
        this.dismissDeleteDialog();
        let apis = new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL)
        let deletePromise = apis.deleteBusinessRule(businessRuleUUID, forceDeleteStatus);
        deletePromise.then(function (deleteResponse) {
            that.displaySnackBar(deleteResponse.data[1]);
            that.loadAvailableBusinessRules();
        }).catch(function (error) {
            that.displaySnackBar("Failed to delete the business rule '" + businessRuleUUID + "'");
            that.loadAvailableBusinessRules();
        })
        this.setState({forceDeleteBusinessRule: false})
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
        let state = this.state
        state['businessRuleUUIDToBeDeleted'] = businessRuleUUID;
        state['displayDeleteDialog'] = true;
        this.setState(state)
    }

    /**
     * Closes the dialog
     */
    dismissDeleteDialog() {
        this.setState({displayDeleteDialog: false})
    }

    /**
     * Displays list of Business Rules when available, or message for creation when not
     */
    displayAvailableBusinessRules() {
        // Check whether business rules are available
        let isNoneAvailable
        if (this.state.businessRules) {
            // If at least one business rule is available
            if (this.state.businessRules.length > 0) {
                isNoneAvailable = false
            } else {
                // No business rules are available
                isNoneAvailable = true
            }
        } else {
            isNoneAvailable = true
        }

        if (!isNoneAvailable) {
            // Show available business rules
            let businessRules = this.state.businessRules.map((businessRule) =>
                <BusinessRule
                    key={businessRule[0].uuid}
                    name={businessRule[0].name}
                    uuid={businessRule[0].uuid}
                    type={businessRule[0].type}
                    status={businessRule[1]}
                    redeploy={(uuid) => this.redeployBusinessRule(uuid)}
                    showDeleteDialog={(uuid) => this.displayDeleteDialog(uuid)}
                />
            )

            return (
                <div style={styles.container}>
                    <Link to={`${window.contextPath}/businessRuleCreator`} style={{textDecoration: 'none'}}>
                        <Button fab color="primary" style={{float: 'right'}} aria-label="Add">
                            <AddIcon/>
                        </Button>
                    </Link>
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
            // Show message for creation
            return (
                <div>
                    <Paper style={styles.paper}>
                        <Typography type="title">
                            No business rule found
                        </Typography>
                        <Typography type="subheading">
                            Get started by creating one
                        </Typography>
                        <br/>
                        <Link to={`${window.contextPath}/businessRuleCreator`} style={{textDecoration: 'none'}}>
                            <Button raised color="primary">
                                Create
                            </Button>
                        </Link>
                    </Paper>
                </div>
            )
        }
    }

    render() {
        // Show snackbar with response message, when this page is rendered after a form submission
        let snackbar =
            <Snackbar
                autoHideDuration={3500}
                open={this.state.displaySnackbar}
                onRequestClose={(e) => this.dismissSnackbar()}
                transition={<Slide direction={styles.snackbar.direction}/>}
                SnackbarContentProps={{
                    'aria-describedby': 'snackbarMessage',
                }}
                message={
                    <span id="snackbarMessage">
                        {this.state.snackbarMessage}
                    </span>
                }
            />

        let deleteConfirmationDialog =
            <Dialog open={this.state.displayDeleteDialog} onRequestClose={(e) => this.dismissDeleteDialog()}>
                <DialogTitle>
                    {BusinessRulesMessages.BUSINESS_RULE_DELETION_CONFIRMATION_TITLE}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        {BusinessRulesMessages.BUSINESS_RUL_DELETION_CONFIRMATION_CONTENT}
                    </DialogContentText>
                    <br/>
                    <FormControlLabel
                        control={
                            <MaterialSwitch
                                checked={this.state.forceDeleteBusinessRule}
                                onChange={(event, checked) => this.setState({forceDeleteBusinessRule: checked})}
                            />
                        }
                        label="Clear all the information on deletion"
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={(e) => this.deleteBusinessRule(
                        this.state.businessRuleUUIDToBeDeleted,
                        this.state.forceDeleteBusinessRule)}>
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>


        return (
            <MuiThemeProvider theme={theme}>
                <div>
                    {snackbar}
                    {deleteConfirmationDialog}
                    <center>
                        <br/>
                        <div>
                            {(this.state.businessRules.length > 0) ?
                                (<Typography type="headline">
                                    Business Rules
                                </Typography>) :
                                (<div></div>)
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
