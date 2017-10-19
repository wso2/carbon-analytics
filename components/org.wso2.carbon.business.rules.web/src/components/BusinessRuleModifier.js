import React from 'react';
// import './index.css';
// Material-UI
import Typography from 'material-ui/Typography';
import Header from "./Header";
import BusinessRulesFunctions from "../utils/BusinessRulesFunctions";
import BusinessRule from "./BusinessRule";
import Table, {TableBody, TableCell, TableHead, TableRow,} from 'material-ui/Table';
import Button from "material-ui/Button";
import AddIcon from "material-ui-icons/Add";
import Dialog, {
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
} from 'material-ui/Dialog';
import Paper from 'material-ui/Paper';
import Snackbar from 'material-ui/Snackbar';
import Slide from 'material-ui/transitions/Slide';
import BusinessRulesMessageStringConstants from "../utils/BusinessRulesMessageStringConstants";
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
import BusinessRulesAPIs from "../utils/BusinessRulesAPIs";
import Switch from 'material-ui/Switch';
import { FormControlLabel, FormGroup } from 'material-ui/Form';

/**
 * Allows to select a Business Rule among Business Rules displayed as table rows
 * and view, edit, delete or re-deploy (when not deployed already) each;
 * Or to create a new business rule
 */
const styles = {
    floatButton: {
        backgroundColor: '#EF6C00',
        color: 'white',
        float: 'right'
    },
    raisedButton: {
        backgroundColor: '#EF6C00',
        color: 'white'
    },
    container: {
        maxWidth: 1020,
    },
    paper: {
        maxWidth: 400,
        paddingTop: 30,
        paddingBottom: 30
    },
    secondaryButton: {
        marginRight: 10
    },
    snackbar: {
        direction: 'up'
    },
    check: {
        color: '#EF6C00',
        '& + $bar': {
            backgroundColor: '#EF6C00',
        },
    }
}

class BusinessRuleModifier extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            businessRules: props.businessRules, // Available Business Rules

            // To show the snackbar, after deployment / save
            displaySnackBar: this.props.displaySnackBar,
            snackbarMessageStatus: this.props.snackbarMessageStatus,

            // To show dialog when deleting a business rule
            displayDeleteDialog: false,
            businessRuleUUIDToBeDeleted: '',
            forceDeleteBusinessRule: false
        }
    }

    /**
     * Displays list of Business Rules when available, or message for creation when not
     */
    loadAvailableBusinessRules() {
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
                    showDeleteDialog={(uuid) => this.handleDeleteDialogOpen(uuid)}
                />
            )

            return (
                <div style={styles.container}>
                    <Button fab color="primary" style={styles.floatButton} aria-label="Remove"
                            onClick={(e) => BusinessRulesFunctions.loadBusinessRuleCreator()}>
                        <AddIcon/>
                    </Button>
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
                            No business rule found!
                        </Typography>
                        <Typography type="subheading">
                            Get started by creating one
                        </Typography>
                        <br/>
                        <Button color="primary" style={styles.raisedButton} aria-label="Remove"
                                onClick={(e) => BusinessRulesFunctions.loadBusinessRuleCreator()}>
                            Create
                        </Button>
                    </Paper>
                </div>
            )
        }
    }

    /**
     * Sends request to the API, to delete the business rule with the given UUID, and status
     *
     * @param businessRuleUUID
     * @param forceDeleteStatus
     */
    deleteBusinessRule(businessRuleUUID, forceDeleteStatus){
        this.setState({displayDialog: true})
        let apis = new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL)
        let deletePromise = apis.deleteBusinessRule(this.state.uuid, 'true').then(function(deleteResponse){
            BusinessRulesFunctions.loadBusinessRuleModifier(true,202) // todo: precise responses. Use 200
        }).catch(function(error){
            BusinessRulesFunctions.loadBusinessRuleModifier(true,502) // todo: no toggle button. use checkbox
        })
    }

    /**
     * Closes the snackbar
     */
    handleRequestClose(){
        this.setState({ displaySnackBar: false });
    };

    /**
     * Opens the delete confirmation dialog, after updating the state with business rule's UUID, that is to be deleted
     */
    handleDeleteDialogOpen(businessRuleUUID){
        this.setState({
            displayDeleteDialog: true,
            businessRuleUUIDToBeDeleted: businessRuleUUID})
    }

    /**
     * Returns delete confirmation dialog
     *
     * @returns {XML}
     */
    showDeleteConfirmationDialog() {
        return (
            <Dialog open={this.state.displayDeleteDialog}
                    onRequestClose={(e) => this.dismissDialog()}
            >
                <DialogTitle>
                    {BusinessRulesMessageStringConstants.BUSINESS_RULE_DELETION_CONFIRMATION_TITLE}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        {BusinessRulesMessageStringConstants.BUSINESS_RUL_DELETION_CONFIRMATION_CONTENT}
                    </DialogContentText>
                    <FormControlLabel
                        control={
                            <Switch
                                checked={this.state.forceDeleteBusinessRule}
                                onChange={(event, checked) => this.setState({forceDeleteBusinessRule:checked})}
                                style={styles.check}
                            />
                        }
                        label="Clear all the information on deletion"
                    />
                </DialogContent>
                <DialogActions>
                    <Button style={styles.secondaryButton}
                            onClick={(e) => this.deleteBusinessRule(
                                this.state.businessRuleUUIDToBeDeleted,
                                this.state.forceDeleteBusinessRule)}
                            color="default">
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>
        )
    }

    /**
     * Closes the dialog
     */
    dismissDialog(){
        this.setState({displayDeleteDialog: false})
    }

    render() {
        // Show snackbar with response message, when this page is rendered after a form submission
        let snackBar =
            <Snackbar
                open={this.state.displaySnackBar}
                onRequestClose={(e)=>this.handleRequestClose()}
                transition={<Slide direction={styles.snackbar.direction} />}
                SnackbarContentProps={{
                    'aria-describedby': 'snackbarMessage',
                }}
                message={
                    <span id="snackbarMessage">
                        {(this.state.snackbarMessageStatus ===
                            BusinessRulesMessageStringConstants.BUSINESS_RULE_SAVE_SUCCESSFUL) ?
                            (BusinessRulesMessageStringConstants.BUSINESS_RULE_SAVE_SUCCESSFUL_MESSAGE) :
                            (this.state.snackbarMessageStatus ===
                                BusinessRulesMessageStringConstants
                                    .BUSINESS_RULE_SAVE_AND_DEPLOYMENT_SUCCESS) ?
                                (BusinessRulesMessageStringConstants
                                    .BUSINESS_RULE_SAVE_AND_DEPLOYMENT_SUCCESS_MESSAGE) :
                                (this.state.snackbarMessageStatus ===
                                    BusinessRulesMessageStringConstants
                                        .BUSINESS_RULE_SAVE_SUCCESSFUL_DEPLOYMENT_FAILURE) ?
                                    (BusinessRulesMessageStringConstants
                                        .BUSINESS_RULE_SAVE_SUCCESSFUL_DEPLOYMENT_FAILURE_MESSAGE) :
                                    (this.state.snackbarMessageStatus ===
                                        BusinessRulesMessageStringConstants
                                            .BUSINESS_RULE_SAVE_AND_DEPLOYMENT_FAILURE) ?
                                        (BusinessRulesMessageStringConstants
                                            .BUSINESS_RULE_SAVE_AND_DEPLOYMENT_FAILURE_MESSAGE) :
                                        (this.state.snackbarMessageStatus ===
                                            BusinessRulesMessageStringConstants
                                                .BUSINESS_RULE_DELETION_SUCCESSFUL)?
                                            (BusinessRulesMessageStringConstants
                                                .BUSINESS_RULE_DELETION_SUCCESSFUL_MESSAGE):
                                            (this.state.snackbarMessageStatus ===
                                                BusinessRulesMessageStringConstants
                                                    .BUSINESS_RULE_DELETION_FAILURE)?
                                                (BusinessRulesMessageStringConstants
                                                    .BUSINESS_RULE_DELETION_FAILURE_MESSAGE):
                                                ('')}
                    </span>
                }
            />


        return (
            <div>
                {this.showDeleteConfirmationDialog()}
                {snackBar}
                <center>
                    <Header
                        title="Business Rule Manager"
                    />
                    <br/>
                    <br/>
                    <div>
                        {
                            (this.state.businessRules.length > 0)?
                                (<Typography type="headline">
                                    Business Rules
                                </Typography>):
                                (<div></div>)
                        }
                    </div>
                    <br/>
                    {this.loadAvailableBusinessRules()}
                </center>
            </div>
        )
    }
}

export default BusinessRuleModifier;
