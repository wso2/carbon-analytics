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
import Button from 'material-ui/Button';
import TextField from 'material-ui/TextField';
import Dialog, { DialogActions, DialogContent, DialogContentText, DialogTitle } from 'material-ui/Dialog';
import Typography from 'material-ui/Typography';
import { FormControl, FormHelperText } from 'material-ui/Form';
import Select from 'material-ui/Select';
import Input, { InputLabel } from 'material-ui/Input';
import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import { MenuItem } from 'material-ui/Menu';
import Snackbar from 'material-ui/Snackbar';
import Slide from 'material-ui/transitions/Slide';
// App Components
import Property from './Property';
import Header from './common/Header';
// App Utilities
import BusinessRulesUtilityFunctions from '../utils/BusinessRulesUtilityFunctions';
// App Constants
import BusinessRulesConstants from '../constants/BusinessRulesConstants';
import BusinessRulesMessages from '../constants/BusinessRulesMessages';
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
        align: 'center',
        maxWidth: 800
    },
    formRoot: {
        flexGrow: 1,
        marginTop: 30,
    },
    formPaper: {
        padding: 50
    },
    snackbar: {
        direction: 'up'
    }
};

/**
 * App context.
 */
const appContext = window.contextPath;

/**
 * Represents a form, shown to create Business Rules from template
 */
class BusinessRuleFromTemplateForm extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            formMode: this.props.match.params.formMode,
            templateRuleTemplates: [],

            businessRuleName: '',
            businessRuleUUID: '',
            selectedTemplateGroup: {},
            selectedRuleTemplate: {},
            businessRuleProperties: {},

            // Dialog
            displayDialog: false,
            dialogTitle: '',
            dialogContentText: '',
            dialogPrimaryButtonText: '',

            // Snackbar
            displaySnackbar: false,
            snackbarMessage: '',

            // For form validation purpose
            isSubmitPressed: false,
            isFormFillable: true
        };
    }

    componentDidMount() {
        if (this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE) {
            // 'Create' mode
            let templateGroupUUID = this.props.match.params.templateGroupUUID;
            new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL)
                .getTemplateGroup(templateGroupUUID)
                .then((templateGroupResponse) => {
                    let templateGroup = templateGroupResponse.data[2];
                    new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL).getRuleTemplates(templateGroupUUID)
                        .then((ruleTemplatesResponse) => {
                            // Filter 'template' type rule templates
                            let templateRuleTemplates = [];
                            for (let ruleTemplate of ruleTemplatesResponse.data[2]) {
                                if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) {
                                    templateRuleTemplates.push(ruleTemplate);
                                }
                            }
                            this.setState({
                                selectedTemplateGroup: templateGroup,
                                templateRuleTemplates: templateRuleTemplates,
                            });
                        });
                });
        } else {
            // 'Edit' or 'View' mode
            let businessRuleUUID = this.props.match.params.businessRuleUUID;
            new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL)
                .getBusinessRule(businessRuleUUID)
                .then((businessRuleResponse) => {
                    let businessRule = businessRuleResponse.data[2];
                    new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL)
                        .getTemplateGroup(businessRule.templateGroupUUID)
                        .then((response) => {
                            let templateGroup = response.data[2];
                            // Filter rule template types
                            new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL)
                                .getRuleTemplates(templateGroup.uuid)
                                .then((ruleTemplatesResponse) => {
                                    const templateRuleTemplates = [];
                                    for (let ruleTemplate of ruleTemplatesResponse.data[2]) {
                                        if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) {
                                            templateRuleTemplates.push(ruleTemplate);
                                        }
                                    }
                                    new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL)
                                        .getRuleTemplate(businessRule.templateGroupUUID, businessRule.ruleTemplateUUID)
                                        .then((ruleTemplateResponse) => {
                                            this.setState({
                                                businessRuleType: BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE,
                                                businessRuleName: businessRule.name,
                                                businessRuleUUID: businessRule.uuid,
                                                selectedTemplateGroup: templateGroup,
                                                selectedRuleTemplate: ruleTemplateResponse.data[2],
                                                templateRuleTemplates: templateRuleTemplates,
                                                businessRuleProperties: businessRule.properties
                                            });
                                        })
                                })
                        })
                })
        }
    }

    /**
     * Updates the selected Rule Template in the state,
     * when Rule Template is selected from the list
     *
     * @param templateGroupUUID
     * @param event
     */
    handleRuleTemplateSelected(templateGroupUUID, event) {
        let state = this.state;
        new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL)
            .getRuleTemplate(templateGroupUUID, event.target.value)
            .then((selectedRuleTemplateResponse) => {
                // Set default value for properties in state
                state.selectedRuleTemplate = selectedRuleTemplateResponse.data[2];
                for (let propertyKey in state.selectedRuleTemplate.properties) {
                    if (Object.prototype.hasOwnProperty.call(state.selectedRuleTemplate.properties, propertyKey)) {
                        state.businessRuleProperties[propertyKey] =
                            state.selectedRuleTemplate.properties[propertyKey].defaultValue;
                    }
                }
                this.setState(state);
            });
    }

    /**
     * Updates the value of the respective property in the state, when a property is changed
     *
     * @param property Property name
     * @param value Entered value for the property
     */
    handleValueChange(property, value) {
        let state = this.state;
        state.businessRuleProperties[property] = value;
        this.setState(state);
    }

    /**
     * Updates the name of business rule when it is changed, in the state
     *
     * @param event
     */
    handleBusinessRuleNameChange(event) {
        let state = this.state;
        state.businessRuleName = event.target.value;
        state.businessRuleUUID = BusinessRulesUtilityFunctions.generateBusinessRuleUUID(event.target.value);
        this.setState(state);
    }

    /**
     * Creates a business rule object from the values entered in the form, and sends to the API,
     * to save only if given deployStatus is false, otherwise, also to deploy
     *
     * @param deployStatus
     */
    createBusinessRule(deployStatus) {
        this.setState({
            isSubmitPressed: true,
            isFormFillable: false
        });
        let isBusinessRuleNameAllowed = true;

        // Validate characters of the business rule name
        if ((this.state.businessRuleName.match(BusinessRulesConstants.BUSINESS_RULE_NAME_REGEX) === null) ||
            (this.state.businessRuleName.match(BusinessRulesConstants.BUSINESS_RULE_NAME_REGEX)[0] !==
                this.state.businessRuleName)) {
            isBusinessRuleNameAllowed = false;
        }

        if (isBusinessRuleNameAllowed) {
            if (this.isBusinessRuleValid()) {
                // Prepare the business rule object
                let businessRuleObject = {
                    name: this.state.businessRuleName,
                    uuid: this.state.businessRuleUUID,
                    type: BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE,
                    templateGroupUUID: this.state.selectedTemplateGroup.uuid,
                    ruleTemplateUUID: this.state.selectedRuleTemplate.uuid,
                    properties: this.state.businessRuleProperties
                };
                new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL)
                    .createBusinessRule(JSON.stringify(businessRuleObject), deployStatus.toString())
                    .then((response) => {
                        this.setSnackbar(response.data[1]);
                        setTimeout(function () {
                            window.location.href = appContext + '/businessRulesManager';
                        }, 3000);
                    })
                    .catch((error) => {
                        // Check for script execution error
                        if (error.response) {
                            if (error.response.data[2] === BusinessRulesConstants.SCRIPT_EXECUTION_ERROR) {
                                this.setState({
                                    isFormFillable: true
                                });
                                this.setSnackbar(error.response.data[1]);
                            } else {
                                this.setSnackbar('Failed to create the Business Rule');
                                setTimeout(function () {
                                    window.location.href = appContext + '/businessRulesManager';
                                }, 3000);
                            }
                        } else {
                            this.setSnackbar('Failed to create the Business Rule');
                            setTimeout(function () {
                                window.location.href = appContext + '/businessRulesManager';
                            }, 3000);
                        }
                    });
            } else {
                // Display error
                this.setState({
                    isFormFillable: true
                });
                this.setSnackbar(BusinessRulesMessages.ALL_FIELDS_REQUIRED_ERROR_CONTENT);
            }
        } else {
            this.setState({
                isFormFillable: true
            });
            this.setSnackbar(BusinessRulesMessages.INVALID_BUSINESS_RULE_NAME);
        }
    }

    /**
     * Re-creates a new business rule object for the business rule with the existing UUID, and sends to the API,
     * to save only if given deployStatus is false, otherwise, also to deploy
     *
     * @param deployStatus
     */
    updateBusinessRule(deployStatus) {
        this.setState({
            isSubmitPressed: true,
            isFormFillable: false
        });
        let isBusinessRuleNameAllowed = true;

        // Validate characters of the business rule name
        if ((this.state.businessRuleName.match(BusinessRulesConstants.BUSINESS_RULE_NAME_REGEX) === null) ||
            (this.state.businessRuleName.match(BusinessRulesConstants.BUSINESS_RULE_NAME_REGEX)[0] !==
                this.state.businessRuleName)) {
            isBusinessRuleNameAllowed = false;
        }

        if (isBusinessRuleNameAllowed) {
            if (this.isBusinessRuleValid()) {
                // Prepare the business rule object
                let businessRuleObject = {
                    name: this.state.businessRuleName,
                    uuid: this.state.businessRuleUUID,
                    type: BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE,
                    templateGroupUUID: this.state.selectedTemplateGroup.uuid,
                    ruleTemplateUUID: this.state.selectedRuleTemplate.uuid,
                    properties: this.state.businessRuleProperties
                };
                new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL)
                    .updateBusinessRule(businessRuleObject.uuid, JSON.stringify(businessRuleObject), deployStatus)
                    .then((response) => {
                        this.setSnackbar(response.data[1]);
                        setTimeout(function () {
                            window.location.href = appContext + '/businessRulesManager';
                        }, 3000);
                    })
                    .catch((error) => {
                        // Check for script execution error
                        if (error.response) {
                            if (error.response.data[2] === BusinessRulesConstants.SCRIPT_EXECUTION_ERROR) {
                                this.setState({
                                    isFormFillable: true
                                });
                                this.setSnackbar(error.response.data[1]);
                            } else {
                                this.setSnackbar('Failed to create the Business Rule');
                                setTimeout(function () {
                                    window.location.href = appContext + '/businessRulesManager';
                                }, 3000);
                            }
                        } else {
                            this.setSnackbar('Failed to create the Business Rule');
                            setTimeout(function () {
                                window.location.href = appContext + '/businessRulesManager';
                            }, 3000);
                        }
                    })
            } else {
                // Display error
                this.setState({
                    isFormFillable: true
                });
                this.setSnackbar(BusinessRulesMessages.ALL_FIELDS_REQUIRED_ERROR_CONTENT);
            }
        } else {
            this.setState({
                isFormFillable: true
            });
            this.setSnackbar(BusinessRulesMessages.INVALID_BUSINESS_RULE_NAME);
        }
    }

    /**
     * Checks whether the business rule object in the state is a valid one or not
     */
    isBusinessRuleValid() {
        if (this.state.businessRuleName === '' || BusinessRulesUtilityFunctions.isEmpty(this.state.businessRuleName) ||
            this.state.businessRuleUUID === '' || BusinessRulesUtilityFunctions.isEmpty(this.state.businessRuleUUID) ||
            this.state.selectedTemplateGroup.uuid === '' ||
            BusinessRulesUtilityFunctions.isEmpty(this.state.selectedTemplateGroup.uuid) ||
            this.state.selectedRuleTemplate.uuid === '' ||
            BusinessRulesUtilityFunctions.isEmpty(this.state.selectedRuleTemplate)) {
            return false;
        }
        for (let propertyKey in this.state.businessRuleProperties) {
            if (Object.prototype.hasOwnProperty.call(this.state.businessRuleProperties, propertyKey)) {
                if (this.state.businessRuleProperties[propertyKey.toString()] === '') {
                    // No need for isEmpty check, since default values are assigned at the beginning, and '' if erased
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Sets the given values to the dialog
     *
     * @param title
     * @param contentText
     * @param primaryButtonText
     */
    setDialog(title, contentText, primaryButtonText) {
        let state = this.state;
        state.displayDialog = true;
        state.dialogTitle = title;
        state.dialogContentText = contentText;
        state.dialogPrimaryButtonText = primaryButtonText;
        this.setState(state);
    }

    /**
     * Sets the given message to the snackbar
     *
     * @param message
     */
    setSnackbar(message) {
        this.setState({
            displaySnackbar: true,
            snackbarMessage: message
        })
    }

    /**
     * Closes the dialog
     */
    dismissDialog() {
        this.setState({displayDialog: false})
    }

    /**
     * Closes the snackbar
     */
    dismissSnackbar() {
        this.setState({displaySnackbar: false});
    };

    /**
     * Shows the dialog, with displaying the contents available from the state
     * @returns {*}
     */
    showDialog() {
        return (
            <Dialog open={this.state.displayDialog}
                    onRequestClose={() => this.dismissDialog()}
            >
                <DialogTitle>{this.state.dialogTitle}</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        {this.state.dialogContentText}
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button style={{marginRight: 10}} onClick={() => this.dismissDialog()} color="default">
                        {this.state.dialogPrimaryButtonText}
                    </Button>
                </DialogActions>
            </Dialog>
        )
    }

    /**
     * Shows the snack bar
     */
    showSnackbar() {
        return (
            <Snackbar
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
            />
        )
    }

    /**
     * Returns properties as Property components with the data specified in the state
     *
     * @param formMode
     * @returns {Array}
     */
    getPropertyComponents(formMode) {
        const reArrangedProperties = [];
        let propertyComponents;

        // Re arrange property objects as an array and map each member as a Property component
        for (let propertyKey in this.state.selectedRuleTemplate.properties) {
            if (this.state.selectedRuleTemplate.properties.hasOwnProperty(propertyKey)) {
                reArrangedProperties.push({
                    propertyName: propertyKey,
                    propertyObject: this.state.selectedRuleTemplate.properties[propertyKey.toString()]
                });
            }
        }
        propertyComponents = reArrangedProperties.map((property) =>
            <Property
                key={property.propertyName}
                name={property.propertyName}
                fieldName={property.propertyObject.fieldName}
                description={property.propertyObject.description ? property.propertyObject.description : ''}
                value={(this.state.businessRuleProperties[property.propertyName]) ?
                    (this.state.businessRuleProperties[property.propertyName]) : ('')}
                errorState={
                    (this.state.isSubmitPressed) &&
                    (this.state.businessRuleProperties[property.propertyName] === '')
                }
                disabledState={formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                options={property.propertyObject.options}
                onValueChange={(e) => this.handleValueChange(property.propertyName, e)}
                fullWidth
            />
        );

        return propertyComponents;
    }

    render() {
        let ruleTemplatesSelectionToDisplay =
            (<FormControl
                disabled={this.state.formMode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE}
            >
                <InputLabel htmlFor="ruleTemplate">RuleTemplate</InputLabel>
                <Select
                    value={(!BusinessRulesUtilityFunctions.isEmpty(this.state.selectedRuleTemplate)) ?
                        (this.state.selectedRuleTemplate.uuid) :
                        ('')
                    }
                    onChange={(e) => this.handleRuleTemplateSelected(this.state.selectedTemplateGroup.uuid, e)}
                    input={<Input id="ruleTemplate"/>}
                >
                    {this.state.templateRuleTemplates.map((ruleTemplate) =>
                        <MenuItem key={ruleTemplate.uuid} value={ruleTemplate.uuid}>
                            {ruleTemplate.name}
                        </MenuItem>
                    )}
                </Select>
                <FormHelperText>
                    {(!BusinessRulesUtilityFunctions.isEmpty(this.state.selectedRuleTemplate)) ?
                        (this.state.selectedRuleTemplate.description ?
                            this.state.selectedRuleTemplate.description : '') :
                        (BusinessRulesMessages.SELECT_RULE_TEMPLATE)
                    }
                </FormHelperText>
            </FormControl>);

        // Business Rule Name text field
        let businessRuleNameTextField;
        // To display properties as input fields
        let propertiesToDisplay;

        // If a rule template has been selected
        if (!BusinessRulesUtilityFunctions.isEmpty(this.state.selectedRuleTemplate)) {
            // To display business rule name field
            businessRuleNameTextField =
                (<TextField
                    id="businessRuleName"
                    name="businessRuleName"
                    label={BusinessRulesMessages.BUSINESS_RULE_NAME_FIELD_NAME}
                    placeholder={BusinessRulesMessages.BUSINESS_RULE_NAME_FIELD_DESCRIPTION}
                    value={this.state.businessRuleName}
                    onChange={(e) => this.handleBusinessRuleNameChange(e)}
                    disabled={this.state.formMode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE}
                    error={(this.state.isSubmitPressed) && (this.state.businessRuleName === '')}
                    required
                    fullWidth
                    margin="normal"
                />);

            // To display each property as an input field
            propertiesToDisplay = this.getPropertyComponents(this.state.formMode);
        }

        let submitButtons;
        if (this.state.isFormFillable) {
            if (this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE) {
                if (!BusinessRulesUtilityFunctions.isEmpty(this.state.selectedRuleTemplate)) {
                    submitButtons =
                        <div>
                            <Button raised color="default" style={{marginRight: 10}}
                                    onClick={() => this.createBusinessRule(false)}>
                                Save
                            </Button>
                            <Button raised color="primary" style={{marginRight: 10}}
                                    onClick={() => this.createBusinessRule(true)}>
                                Save & Deploy
                            </Button>
                            <Button
                                color="default" style={{marginRight: 10}}
                                onClick={() => {
                                    window.location.href = appContext + '/businessRulesManager';
                                }}
                            >
                                Cancel
                            </Button>
                        </div>
                }
            } else if (this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT) {
                submitButtons =
                    <div>
                        <Button raised color="default" style={{marginRight: 10}}
                                onClick={() => this.updateBusinessRule(false)}>
                            Save
                        </Button>
                        <Button raised color="primary" style={{marginRight: 10}}
                                onClick={() => this.updateBusinessRule(true)}>
                            Save & Deploy
                        </Button>
                        <Button
                            color="default" style={{marginRight: 10}}
                            onClick={() => {
                                window.location.href = appContext + '/businessRulesManager';
                            }}
                        >
                            Cancel
                        </Button>
                    </div>
            }
        }

        return (
            <MuiThemeProvider theme={theme}>
                <Header />
                <br />
                <div>
                    {this.showDialog()}
                    {this.showSnackbar()}
                    <Grid container spacing={24} style={styles.formRoot} justify="center">
                        <Grid item xs={12} sm={6}>
                            <Paper style={styles.formPaper}>
                                <center>
                                    <Typography type="headline">
                                        {this.state.selectedTemplateGroup.name}
                                    </Typography>
                                    <Typography type="subheading">
                                        {this.state.selectedTemplateGroup.description ?
                                            this.state.selectedTemplateGroup.description : ''}
                                    </Typography>
                                    <br/>
                                    {ruleTemplatesSelectionToDisplay}
                                    <br/>
                                    <br/>
                                    {businessRuleNameTextField}
                                </center>
                                {propertiesToDisplay}
                                <br/>
                                <br/>
                                <center>
                                    {submitButtons}
                                </center>
                            </Paper>
                        </Grid>
                    </Grid>
                </div>
            </MuiThemeProvider>
        )
    }
}

export default BusinessRuleFromTemplateForm;
