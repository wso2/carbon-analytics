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
import ReactDOM from 'react-dom';
// Material UI Components
import Button from 'material-ui/Button';
import TextField from 'material-ui/TextField';
import Dialog, {DialogActions, DialogContent, DialogContentText, DialogTitle,} from 'material-ui/Dialog';
import Typography from 'material-ui/Typography';
import {FormControl, FormHelperText} from 'material-ui/Form';
import Select from 'material-ui/Select';
import Input, {InputLabel} from 'material-ui/Input';
import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import {MenuItem} from 'material-ui/Menu';
// App Components
import Property from './Property';
import Header from "./Header";
import ProgressDisplay from "./ProgressDisplay";
// App Utilities
import BusinessRulesUtilityFunctions from "../utils/BusinessRulesUtilityFunctions";
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
import BusinessRulesAPICaller from "../utils/BusinessRulesAPICaller";
import BusinessRulesMessages from "../utils/BusinessRulesMessages";
// CSS
import '../index.css';

/**
 * Represents a form, shown to create Business Rules from template
 */

// Styles related to this component
const styles = {
    button: {
        backgroundColor: '#EF6C00',
        color: 'white',
        marginRight: 10
    },
    secondaryButton: {
        marginRight: 10
    },
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
    }
}

class BusinessRuleFromTemplateForm extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            formMode: props.formMode, // 'create', 'edit' or 'view'
            businessRuleName: props.businessRuleName,
            businessRuleUUID: props.businessRuleUUID,
            selectedTemplateGroup: props.selectedTemplateGroup,
            templateRuleTemplates: props.templateRuleTemplates, // Rule templates of type 'template'
            selectedRuleTemplate: props.selectedRuleTemplate,
            // To store values given for properties displayed in the form
            businessRuleProperties: props.businessRuleProperties,

            // For displaying messages / errors
            displayDialog: false,
            dialogTitle: '',
            dialogContentText: '',
            dialogPrimaryButtonText: ''
        }

        // Assign default values for properties in the state, for 'create' mode
        if (this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE) {
            let state = this.state
            state.businessRuleName = ''
            state.businessRuleUUID = ''
            state.businessRuleProperties = {}
            this.state = state
        } else {
            // Assign entered values for properties in the state, for 'edit' or 'view' modes
            let state = this.state
            state.businessRuleProperties = props.businessRuleProperties
            this.state = state
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
        let state = this.state
        let that = this
        // Get selected rule template & update in the state
        let selectedRuleTemplatePromise = BusinessRulesUtilityFunctions.getRuleTemplate(templateGroupUUID,
            event.target.value)
        selectedRuleTemplatePromise.then(function (selectedRuleTemplateResponse) {
            state['selectedRuleTemplate'] = selectedRuleTemplateResponse.data[2]

            // Set properties in the state as default value
            for (let propertyKey in state.selectedRuleTemplate.properties) {
                if (state.selectedRuleTemplate.properties.hasOwnProperty(propertyKey)) {
                    state['businessRuleProperties'][propertyKey] =
                        that.state.selectedRuleTemplate.properties[propertyKey.toString()]['defaultValue']
                }
            }

            that.setState(state)
        })
    }

    /**
     * Updates the value of the respective property in the state, when a property is changed
     *
     * @param property Property name
     * @param value Entered value for the property
     */
    handleValueChange(property, value) {
        let state = this.state
        state['businessRuleProperties'][property] = value
        this.setState(state)
    }

    /**
     * Updates the name of business rule when it is changed, in the state
     *
     * @param event
     */
    handleBusinessRuleNameChange(event) {
        let state = this.state
        state['businessRuleName'] = event.target.value
        state['businessRuleUUID'] = BusinessRulesUtilityFunctions.generateBusinessRuleUUID(event.target.value)
        this.setState(state)
    }

    /**
     * Creates a business rule object from the values entered in the form, and sends to the API,
     * to save only if given deployStatus is false, otherwise, also to deploy
     *
     * @param deployStatus
     */
    createBusinessRule(deployStatus) {
        // Validate whether all required fields are filled or not
        if (this.isBusinessRuleValid()) {
            // Prepare the business rule object
            let businessRuleObject = {
                name : this.state.businessRuleName,
                uuid : this.state.businessRuleUUID,
                type : BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE,
                templateGroupUUID : this.state.selectedTemplateGroup.uuid,
                ruleTemplateUUID : this.state.selectedRuleTemplate.uuid,
                properties : this.state.businessRuleProperties
            }


            // Send prepared business rule object to API
            let apis = new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL)
            apis.createBusinessRule(JSON.stringify(businessRuleObject), deployStatus.toString()).then(
                function (response) {
                BusinessRulesUtilityFunctions.loadBusinessRulesManager(response.data[1]);
            }).catch(function (error) {
                ReactDOM.render(
                    <ProgressDisplay
                        error={['Unable to process your request','Failed to create the business rule']}/>,
                    document.getElementById('root'))
            })
            // Show 'please wait'
            ReactDOM.render(<ProgressDisplay/>, document.getElementById('root'))

        } else {
            // Display error
            this.setDialog(BusinessRulesMessages.ALL_FIELDS_REQUIRED_ERROR_TITLE,
                BusinessRulesMessages.ALL_FIELDS_REQUIRED_ERROR_CONTENT,
                BusinessRulesMessages.ALL_FIELDS_REQUIRED_ERROR_PRIMARY_BUTTON)
        }
    }


    /**
     * Re-creates a new business rule object for the business rule with the existing UUID, and sends to the API,
     * to save only if given deployStatus is false, otherwise, also to deploy
     *
     * @param deployStatus
     */
    updateBusinessRule(deployStatus) {
        // Validate whether all required fields are filled or not
        if (this.isBusinessRuleValid()) {
            // Prepare the business rule object
            let businessRuleObject = {
                name : this.state.businessRuleName,
                uuid : this.state.businessRuleUUID,
                type : BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE,
                templateGroupUUID : this.state.selectedTemplateGroup.uuid,
                ruleTemplateUUID : this.state.selectedRuleTemplate.uuid,
                properties : this.state.businessRuleProperties
            }

            // Send prepared business rule object to API
            let apis = new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL)
            // Deployment true or false
            apis.updateBusinessRule(businessRuleObject['uuid'], JSON.stringify(businessRuleObject), deployStatus)
                .then(function (response) {
                    BusinessRulesUtilityFunctions.loadBusinessRulesManager(response.data[1]);
                }).catch(function (error) {
                ReactDOM.render(
                    <ProgressDisplay
                        error={['Unable to process your request',
                            'Failed to update the business rule']}/>, document.getElementById('root'))
            })
            // Show 'please wait'
            ReactDOM.render(<ProgressDisplay/>, document.getElementById('root'))
        } else {
            // Display error
            this.setDialog(BusinessRulesMessages.ALL_FIELDS_REQUIRED_ERROR_TITLE,
                BusinessRulesMessages.ALL_FIELDS_REQUIRED_ERROR_CONTENT,
                BusinessRulesMessages.ALL_FIELDS_REQUIRED_ERROR_PRIMARY_BUTTON)
        }
    }

    /**
     * Checks whether the business rule object in the state is a valid one or not
     */
    isBusinessRuleValid() {
        if (this.state.businessRuleName === '' || BusinessRulesUtilityFunctions.isEmpty(this.state.businessRuleName)) {
            return false
        }
        if (this.state.businessRuleUUID === '' || BusinessRulesUtilityFunctions.isEmpty(this.state.businessRuleUUID)) {
            return false
        }
        if (this.state.selectedTemplateGroup.uuid === '' ||
            BusinessRulesUtilityFunctions.isEmpty(this.state.selectedTemplateGroup.uuid)) {
            return false
        }
        if (this.state.selectedRuleTemplate.uuid === '' ||
            BusinessRulesUtilityFunctions.isEmpty(this.state.selectedRuleTemplate)) {
            return false
        }
        for (let propertyKey in this.state.businessRuleProperties) {
            if (this.state.businessRuleProperties[propertyKey.toString()] === '') {
                // No need for isEmpty check, since default values are assigned at the beginning, and '' if erased
                return false
            }
        }
        return true
    }

    /**
     * Sets members of the state with given values, which will be used to show content in the dialog
     *
     * @param title
     * @param contentText
     * @param primaryButtonText
     */
    setDialog(title, contentText, primaryButtonText) {
        let state = this.state
        state['displayDialog'] = true
        state['dialogTitle'] = title
        state['dialogContentText'] = contentText
        state['dialogPrimaryButtonText'] = primaryButtonText
        this.setState(state)
    }

    /**
     * Closes the dialog
     */
    dismissDialog() {
        this.setState({displayDialog: false})
    }

    /**
     * Shows the dialog, with displaying the contents available from the state
     *
     * @returns {XML}
     */
    showDialog() {
        return (
            <Dialog open={this.state.displayDialog}
                    onRequestClose={(e) => this.dismissDialog()}
            >
                <DialogTitle>{this.state.dialogTitle}</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        {this.state.dialogContentText}
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button style={styles.secondaryButton}
                            onClick={(e) => this.dismissDialog()}
                            color="default">
                        {this.state.dialogPrimaryButtonText}
                    </Button>
                </DialogActions>
            </Dialog>
        )
    }

    render() {
        // To display available rule templates of type 'template' as drop down
        let templateRuleTemplatesToDisplay = this.state.templateRuleTemplates.map((ruleTemplate) =>
            <MenuItem key={ruleTemplate.uuid} value={ruleTemplate.uuid}>
                {ruleTemplate.name}
            </MenuItem>
        )
        let ruleTemplatesSelectionToDisplay =
            <FormControl
                disabled={this.state.formMode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE}>
                <InputLabel htmlFor="ruleTemplate">RuleTemplate</InputLabel>
                <Select
                    value={(!BusinessRulesUtilityFunctions.isEmpty(this.state.selectedRuleTemplate)) ?
                        (this.state.selectedRuleTemplate.uuid) :
                        ('')
                    }
                    onChange={(e) => this.handleRuleTemplateSelected(this.state.selectedTemplateGroup.uuid, e)}
                    input={<Input id="ruleTemplate"/>}
                >
                    {templateRuleTemplatesToDisplay}
                </Select>
                <FormHelperText>
                    {(!BusinessRulesUtilityFunctions.isEmpty(this.state.selectedRuleTemplate)) ?
                        (this.state.selectedRuleTemplate.description) :
                        (BusinessRulesMessages.SELECT_RULE_TEMPLATE)
                    }
                </FormHelperText>
            </FormControl>

        // Business Rule Name text field
        let businessRuleNameTextField

        // To store properties in re-arranged format, in order to generate fields
        let properties = []
        // To display properties as input fields
        let propertiesToDisplay

        // If a rule template has been selected
        if (this.state.selectedRuleTemplate) {
            // To display business rule name field
            businessRuleNameTextField =
                <TextField
                    id="businessRuleName"
                    name="businessRuleName"
                    label={BusinessRulesMessages.BUSINESS_RULE_NAME_FIELD_NAME}
                    placeholder={BusinessRulesMessages.BUSINESS_RULE_NAME_FIELD_DESCRIPTION}
                    value={this.state.businessRuleName}
                    required
                    onChange={(e) => this.handleBusinessRuleNameChange(e)}
                    disabled={this.state.formMode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE}
                />

            // Push propertyKey and propertyObject as an array member, in order to use the array.map() function
            for (let propertyKey in this.state.selectedRuleTemplate.properties) {
                if (this.state.selectedRuleTemplate.properties.hasOwnProperty(propertyKey)) {
                    properties.push({
                        propertyName: propertyKey,
                        propertyObject: this.state.selectedRuleTemplate.properties[propertyKey.toString()]
                    })
                }
            }

            // To display each property as an input field
            propertiesToDisplay = properties.map((property) =>
                <Property
                    key={property.propertyName}
                    name={property.propertyName}
                    fieldName={property.propertyObject.fieldName}
                    description={property.propertyObject.description}
                    value={this.state['businessRuleProperties'][property.propertyName]}
                    options={property.propertyObject.options}
                    onValueChange={(e) => this.handleValueChange(property.propertyName, e)}
                    errorState={this.state['businessRuleProperties'][property.propertyName] == ''}
                    disabledState={this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                    fullWidth
                />
            )
        }

        // Save, and Save & Deploy buttons
        let submitButtons

        // If form should be displayed for Creating a business rule
        if (this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE) {
            if (!BusinessRulesUtilityFunctions.isEmpty(this.state.selectedRuleTemplate)) {
                submitButtons =
                    <div>
                        <Button raised color="default" style={styles.secondaryButton}
                                onClick={(e) => this.createBusinessRule(false)}>
                            Save
                        </Button>
                        <Button raised color="primary" style={styles.button}
                                onClick={(e) => this.createBusinessRule(true)}>
                            Save & Deploy
                        </Button>
                    </div>
            }
        } else if (this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT) {
            // If form should be displayed for Editing a business rule
            submitButtons =
                <div>
                    <Button raised color="default" style={styles.secondaryButton}
                            onClick={(e) => this.updateBusinessRule(false)}>
                        Save
                    </Button>
                    <Button raised color="primary" style={styles.button}
                            onClick={(e) => this.updateBusinessRule(true)}>
                        Save & Deploy
                    </Button>
                </div>
        }

        return (
            <div>
                {this.showDialog()}
                <Header/>
                <br/>
                <Grid container spacing={24} style={styles.formRoot} justify="center">
                    <Grid item xs={12} sm={6}>
                        <Paper style={styles.formPaper}>
                            <center>
                                <Typography type="headline">
                                    {this.state.selectedTemplateGroup.name}
                                </Typography>
                                <Typography type="subheading">
                                    {this.state.selectedTemplateGroup.description}
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
        )
    }
}

export default BusinessRuleFromTemplateForm;
