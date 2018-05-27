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
import PropTypes from 'prop-types';
// Material UI Components
import TextField from 'material-ui/TextField';
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
import Property from './elements/Property';
import SubmitButtonGroup from './elements/SubmitButtonGroup';
import Header from '../../common/Header';
import ProgressDisplay from '../../common/ProgressDisplay';
import ErrorDisplay from '../../common/error/ErrorDisplay';
// App Errors
import FormSubmissionError from '../../../error/FormSubmissionError';
// App Utils
import BusinessRulesUtilityFunctions from '../../../utils/BusinessRulesUtilityFunctions';
// App APIs
import BusinessRulesAPI from '../../../api/BusinessRulesAPI';
// App Constants
import BusinessRulesConstants from '../../../constants/BusinessRulesConstants';
import BusinessRulesMessages from '../../../constants/BusinessRulesMessages';
// Styles
import Styles from '../../../style/Styles';
import '../../../index.css';

/**
 * App context
 */
const appContext = window.contextPath;

/**
 * Represents the form, which allows to fill and create a Business Rules from template
 */
export default class BusinessRuleFromTemplateForm extends Component {
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
            showSubmitButtons: true,

            // To display the Snackbar
            displaySnackbar: false,
            snackbarMessage: '',

            // To capture and display errors in form fields
            isFieldErrorStatesDirty: false,
            fieldErrorStates: {},

            // To show Progress or error message
            hasLoaded: false,
            errorCode: BusinessRulesConstants.ERROR_CODES.UNKNOWN,
        };
    }

    componentDidMount() {
        if (this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE) {
            this.loadNewForm(this.props.match.params.templateGroupUUID);
        } else {
            this.loadExistingForm(this.props.match.params.businessRuleUUID);
        }
    }

    /**
     * Gets default error states, for the fields of the form
     * @param {Object} businessRuleProperties       Object containing values for each property of the business rule
     * @returns {Object}                            Object with the default error states of the business rule name,
     *                                              and properties
     */
    getDefaultErrorStates(businessRuleProperties) {
        const errorStates = {};
        for (const property in businessRuleProperties) {
            if (Object.prototype.hasOwnProperty.call(businessRuleProperties, property)) {
                errorStates[property] = false;
            }
        }
        return {
            businessRuleName: false,
            properties: errorStates,
        };
    }

    /**
     * Returns Property Components, for properties of the Business Rule
     * @param {String} formMode     Mode of the Business Rule form
     * @returns {Component[]}       Property Components
     */
    getPropertyComponents(formMode) {
        const properties = this.state.selectedRuleTemplate.properties;
        return Object.keys(properties).map(property =>
            (<Property
                key={property}
                name={property}
                fieldName={properties[property].fieldName}
                description={properties[property].description ? properties[property].description : ''}
                value={(this.state.businessRuleProperties[property]) ?
                    (this.state.businessRuleProperties[property]) : ('')}
                errorState={this.state.fieldErrorStates.properties[property]}
                disabledState={formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                options={properties[property].options}
                onValueChange={e => this.updatePropertyValue(property, e)}
                fullWidth
            />));
    }

    /**
     * Gets the AxiosPromise for Create/Update, based on the given parameter
     * @param {Object} businessRuleObject       Business Rule Object to submit
     * @param {boolean} deployStatus            Deployment status, when saving the business rule
     * @param {boolean} isUpdate                Whether the submission is an update of a business rule
     * @returns {AxiosPromise}                  AxiosPromise for Create/Update business rule
     */
    getSubmitPromise(businessRuleObject, deployStatus, isUpdate) {
        if (isUpdate) {
            return new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
                .updateBusinessRule(businessRuleObject.uuid, JSON.stringify(businessRuleObject), deployStatus);
        }
        return new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
            .createBusinessRule(JSON.stringify(businessRuleObject), deployStatus.toString());
    }

    /**
     * Loads a new form, with configurations of the template group with the given UUID
     * @param {String} templateGroupUUID        UUID of the template group
     */
    loadNewForm(templateGroupUUID) {
        new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
            .getTemplateGroup(templateGroupUUID)
            .then((templateGroupResponse) => {
                const templateGroup = templateGroupResponse.data[2];
                new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
                    .getRuleTemplates(templateGroupUUID)
                    .then((ruleTemplatesResponse) => {
                        // Filter 'template' type rule templates
                        const templateRuleTemplates = [];
                        for (const ruleTemplate of ruleTemplatesResponse.data[2]) {
                            if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) {
                                templateRuleTemplates.push(ruleTemplate);
                            }
                        }
                        this.setState({
                            selectedTemplateGroup: templateGroup,
                            templateRuleTemplates,
                            hasLoaded: true,
                            errorCode: BusinessRulesConstants.ERROR_CODES.NONE,
                        });
                    })
                    .catch((error) => {
                        // Error in Loading Rule Templates
                        this.setState({
                            hasLoaded: true,
                            errorCode: BusinessRulesUtilityFunctions.getErrorDisplayCode(error),
                        });
                    });
            })
            .catch((error) => {
                // Error in Loading Template Group
                this.setState({
                    hasLoaded: true,
                    errorCode: BusinessRulesUtilityFunctions.getErrorDisplayCode(error),
                });
            });
    }

    /**
     * Loads form for an existing business rule, which has the given UUID
     * @param {String} businessRuleUUID         UUID of the business rule
     */
    loadExistingForm(businessRuleUUID) {
        new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
            .getBusinessRule(businessRuleUUID)
            .then((businessRuleResponse) => {
                const businessRule = businessRuleResponse.data[2];
                new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
                    .getTemplateGroup(businessRule.templateGroupUUID)
                    .then((response) => {
                        const templateGroup = response.data[2];
                        // Filter rule template types
                        new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
                            .getRuleTemplates(templateGroup.uuid)
                            .then((ruleTemplatesResponse) => {
                                const templateRuleTemplates = [];
                                for (const ruleTemplate of ruleTemplatesResponse.data[2]) {
                                    if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) {
                                        templateRuleTemplates.push(ruleTemplate);
                                    }
                                }
                                new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
                                    .getRuleTemplate(businessRule.templateGroupUUID, businessRule.ruleTemplateUUID)
                                    .then((ruleTemplateResponse) => {
                                        this.setState({
                                            businessRuleType: BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE,
                                            businessRuleName: businessRule.name,
                                            businessRuleUUID: businessRule.uuid,
                                            selectedTemplateGroup: templateGroup,
                                            selectedRuleTemplate: ruleTemplateResponse.data[2],
                                            templateRuleTemplates,
                                            businessRuleProperties: businessRule.properties,
                                            fieldErrorStates: this.getDefaultErrorStates(businessRule.properties),
                                            hasLoaded: true,
                                            errorCode: BusinessRulesConstants.ERROR_CODES.NONE,
                                        });
                                    })
                                    .catch((error) => {
                                        // Error in Loading the respective Rule Template
                                        this.setState({
                                            hasLoaded: true,
                                            errorCode: BusinessRulesUtilityFunctions.getErrorDisplayCode(error),
                                        });
                                    });
                            })
                            .catch((error) => {
                                // Error in Loading Rule Templates
                                this.setState({
                                    hasLoaded: true,
                                    errorCode: BusinessRulesUtilityFunctions.getErrorDisplayCode(error),
                                });
                            });
                    })
                    .catch((error) => {
                        // Error in Loading Template Group
                        this.setState({
                            hasLoaded: true,
                            errorCode: BusinessRulesUtilityFunctions.getErrorDisplayCode(error),
                        });
                    });
            })
            .catch((error) => {
                // Error in Loading Business Rule
                this.setState({
                    hasLoaded: true,
                    errorCode: BusinessRulesUtilityFunctions.getErrorDisplayCode(error),
                });
            });
    }

    /**
     * Returns whether the Business Rule is valid or not
     * @returns {boolean}               Validity of the Business Rule
     * @throws {FormSubmissionError}    Error of the form, containing the fieldErrorStates object and the error message
     */
    isBusinessRuleValid() {
        const fieldErrorStates = this.getDefaultErrorStates(this.state.businessRuleProperties);
        if (BusinessRulesUtilityFunctions.isEmpty(this.state.selectedTemplateGroup) ||
            this.state.selectedTemplateGroup.uuid === '') {
            throw new FormSubmissionError(fieldErrorStates, 'Please select a valid Template Group');
        }
        if (BusinessRulesUtilityFunctions.isEmpty(this.state.selectedRuleTemplate) ||
            this.state.selectedRuleTemplate.uuid === '') {
            throw new FormSubmissionError(fieldErrorStates, 'Please select a valid Rule Template');
        }
        if (this.state.businessRuleName === '') {
            fieldErrorStates.businessRuleName = true;
            throw new FormSubmissionError(fieldErrorStates, 'Name of the Business Rule can not be empty');
        }
        if ((this.state.businessRuleName.match(BusinessRulesConstants.BUSINESS_RULE_NAME_REGEX) === null) ||
            (this.state.businessRuleName.match(BusinessRulesConstants.BUSINESS_RULE_NAME_REGEX)[0] !==
                this.state.businessRuleName)) {
            fieldErrorStates.businessRuleName = true;
            throw new FormSubmissionError(fieldErrorStates, 'Please enter a valid name for the Business Rule');
        }
        let isAnyPropertyEmpty = false;
        for (const propertyKey in this.state.businessRuleProperties) {
            if (Object.prototype.hasOwnProperty.call(this.state.businessRuleProperties, propertyKey)) {
                if (this.state.businessRuleProperties[propertyKey] === '') {
                    fieldErrorStates.properties[propertyKey] = true;
                    isAnyPropertyEmpty = true;
                }
            } else {
                isAnyPropertyEmpty = true;
            }
        }
        if (isAnyPropertyEmpty) {
            throw new FormSubmissionError(fieldErrorStates, 'Please fill in values for all the properties');
        }
        return true;
    }

    /**
     * Handles error, occurred when submitting the business rule
     * @param {Object} error        Error response
     */
    handleSubmissionError(error) {
        this.setState({
            isFieldErrorStatesDirty: true,
        });
        // Check for script execution error
        if (error.response) {
            if (error.response.data[2] === BusinessRulesConstants.SCRIPT_EXECUTION_ERROR) {
                this.setState({
                    showSubmitButtons: true,
                });
                this.toggleSnackbar(error.response.data[1]);
            } else {
                this.toggleSnackbar('Failed to create the Business Rule');
                setTimeout(() => {
                    window.location.href = appContext + '/businessRulesManager';
                }, 3000);
            }
        } else {
            this.toggleSnackbar('Failed to create the Business Rule');
            setTimeout(() => {
                window.location.href = appContext + '/businessRulesManager';
            }, 3000);
        }
    }

    /**
     * Submits the business rule object to save and deploy (optionally), on Create/Update mode
     * @param {boolean} shouldDeploy        Whether to deploy or not, in addition to saving the business rule
     * @param {boolean} isUpdate            Whether the submission is an update of a business rule
     */
    submitBusinessRule(shouldDeploy, isUpdate) {
        this.setState({
            showSubmitButtons: false,
            isFieldErrorStatesDirty: false,
            fieldErrorStates: this.getDefaultErrorStates(this.state.businessRuleProperties),
        });

        try {
            if (this.isBusinessRuleValid()) {
                // Prepare the business rule object
                const businessRuleObject = {
                    name: this.state.businessRuleName,
                    uuid: this.state.businessRuleUUID,
                    type: BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE,
                    templateGroupUUID: this.state.selectedTemplateGroup.uuid,
                    ruleTemplateUUID: this.state.selectedRuleTemplate.uuid,
                    properties: this.state.businessRuleProperties,
                };

                this.getSubmitPromise(businessRuleObject, shouldDeploy, isUpdate)
                    .then((response) => {
                        this.toggleSnackbar(response.data[1]);
                        setTimeout(() => {
                            window.location.href = appContext + '/businessRulesManager';
                        }, 3000);
                    })
                    .catch((error) => {
                        this.handleSubmissionError(error);
                    });
            }
        } catch (error) {
            this.setState({
                showSubmitButtons: true,
                isFieldErrorStatesDirty: true,
                fieldErrorStates: error.fieldErrorStates,
            });
            this.toggleSnackbar(error.message);
        }
    }

    /**
     * Loads properties for the selected Rule Template
     * @param {String} templateGroupUUID        UUID of the Template Group to which, the Rule Template belongs to
     * @param {Object} event                    Event of the Rule Template selection
     */
    handleRuleTemplateSelected(templateGroupUUID, event) {
        const state = this.state;
        new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
            .getRuleTemplate(templateGroupUUID, event.target.value)
            .then((selectedRuleTemplateResponse) => {
                // Set default value for properties in state
                state.selectedRuleTemplate = selectedRuleTemplateResponse.data[2];
                for (const propertyKey in state.selectedRuleTemplate.properties) {
                    if (Object.prototype.hasOwnProperty.call(state.selectedRuleTemplate.properties, propertyKey)) {
                        state.businessRuleProperties[propertyKey] =
                            state.selectedRuleTemplate.properties[propertyKey].defaultValue;
                    }
                }
                state.fieldErrorStates = this.getDefaultErrorStates(state.businessRuleProperties);
                state.hasLoaded = true;
                state.errorCode = BusinessRulesConstants.ERROR_CODES.NONE;
                this.setState(state);
            })
            .catch((error) => {
                // Error in Loading Properties for the selected Rule Template
                this.setState({
                    hasLoaded: true,
                    errorCode: BusinessRulesUtilityFunctions.getErrorDisplayCode(error),
                });
            });
    }

    /**
     * Updates the name of the Business Rule
     * @param {Object} event     Event of the Business Rule Name Text Field
     */
    handleBusinessRuleNameChange(event) {
        const state = this.state;
        state.businessRuleName = event.target.value;
        state.businessRuleUUID = BusinessRulesUtilityFunctions.generateBusinessRuleUUID(event.target.value);
        this.setState(state);
        this.resetErrorStates();
    }

    /**
     * Updates the value of the given property of the Business Rule
     * @param {String} property      Property key
     * @param {String} value         Property value
     */
    updatePropertyValue(property, value) {
        const state = this.state;
        state.businessRuleProperties[property] = value;
        state.fieldErrorStates = this.getDefaultErrorStates(this.state.businessRuleProperties);
        this.setState(state);
        this.resetErrorStates();
    }

    /**
     * Resets error states of all the properties of the Business Rule,
     * if any of the field is currently having an error
     */
    resetErrorStates() {
        if (this.state.isFieldErrorStatesDirty) {
            this.setState({
                isFieldErrorStatesDirty: false,
                fieldErrorStates: this.getDefaultErrorStates(this.state.businessRuleProperties),
            });
        }
    }

    /**
     * Shows the snackbar with the given message, or hides when no message is given
     * @param {String} message       Snackbar message text
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
                snackbarMessage: '',
            });
        }
    }

    /**
     * Returns Submit buttons
     * @returns {HTMLElement}     Div containing Save, Save And Deploy, and Cancel buttons
     */
    displaySubmitButtons() {
        if (this.state.showSubmitButtons) {
            if (this.state.formMode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW &&
                !BusinessRulesUtilityFunctions.isEmpty(this.state.selectedRuleTemplate)) {
                return (
                    <SubmitButtonGroup
                        onSubmit={
                            shouldDeploy =>
                                this.submitBusinessRule(
                                    shouldDeploy,
                                    this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT)}
                        onCancel={() => {
                            window.location.href = appContext + '/businessRulesManager';
                        }}
                    />
                );
            }
        }
        return null;
    }

    /**
     * Returns a Snackbar
     * @returns {Component}     Snackbar Component
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
     * Displays the title of the form
     * @returns {HTMLElement}       Title of the form
     */
    displayTitle() {
        return (
            <div>
                <Typography type="headline">
                    {this.state.selectedTemplateGroup.name}
                </Typography>
                <Typography type="subheading">
                    {this.state.selectedTemplateGroup.description || ''}
                </Typography>
            </div>
        );
    }

    /**
     * Returns Dropdown for selecting a Rule Template
     * @returns {Component}     FormControl Component
     */
    displayRuleTemplateSelection() {
        let ruleTemplateDescription;
        if (!BusinessRulesUtilityFunctions.isEmpty(this.state.selectedRuleTemplate)) {
            if (this.state.selectedRuleTemplate.description) {
                ruleTemplateDescription = this.state.selectedRuleTemplate.description;
            } else {
                ruleTemplateDescription = '';
            }
        } else {
            ruleTemplateDescription = BusinessRulesMessages.SELECT_RULE_TEMPLATE;
        }

        return (
            <FormControl
                disabled={this.state.formMode !==
                BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE}
            >
                <InputLabel htmlFor="ruleTemplate">RuleTemplate</InputLabel>
                <Select
                    value={(!BusinessRulesUtilityFunctions
                        .isEmpty(this.state.selectedRuleTemplate)) ? (this.state.selectedRuleTemplate.uuid) : ('')}
                    onChange={e => this.handleRuleTemplateSelected(this.state.selectedTemplateGroup.uuid, e)}
                    input={<Input id="ruleTemplate" />}
                >
                    {this.state.templateRuleTemplates.map(ruleTemplate =>
                        (<MenuItem key={ruleTemplate.uuid} value={ruleTemplate.uuid}>
                            {ruleTemplate.name}
                        </MenuItem>))}
                </Select>
                <FormHelperText>
                    {ruleTemplateDescription}
                </FormHelperText>
            </FormControl>
        );
    }

    /**
     * Returns Input field to enter the business rule's name
     * @returns {Component}     TextField Component
     */
    displayBusinessRuleName() {
        // If a rule template has been selected
        if (!BusinessRulesUtilityFunctions.isEmpty(this.state.selectedRuleTemplate)) {
            return (
                <TextField
                    id="businessRuleName"
                    name="businessRuleName"
                    label={BusinessRulesMessages.BUSINESS_RULE_NAME_FIELD_NAME}
                    placeholder={BusinessRulesMessages.BUSINESS_RULE_NAME_FIELD_DESCRIPTION}
                    value={this.state.businessRuleName}
                    onChange={e => this.handleBusinessRuleNameChange(e)}
                    disabled={this.state.formMode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE}
                    error={this.state.fieldErrorStates.businessRuleName}
                    required
                    fullWidth
                    margin="normal"
                />);
        }
        return null;
    }

    /**
     * Displays content of the page
     * @returns {Component}     Content of the page
     */
    displayContent() {
        if (this.state.hasLoaded) {
            if (this.state.errorCode === BusinessRulesConstants.ERROR_CODES.NONE) {
                return (
                    <div>
                        {this.displaySnackbar()}
                        <Grid container spacing={24} style={Styles.businessRuleForm.root} justify="center">
                            <Grid item xs={12} sm={6}>
                                <Paper style={Styles.businessRuleForm.paper}>
                                    <center>
                                        {this.displayTitle()}
                                        <br />
                                        {this.displayRuleTemplateSelection()}
                                        <br />
                                        <br />
                                        {this.displayBusinessRuleName()}
                                    </center>
                                    {this.getPropertyComponents(this.state.formMode)}
                                    <br />
                                    <br />
                                    <center>
                                        {this.displaySubmitButtons()}
                                    </center>
                                </Paper>
                            </Grid>
                        </Grid>
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
                <Header />
                <br />
                <br />
                <div>
                    {this.displayContent()}
                </div>
            </div>
        );
    }
}

BusinessRuleFromTemplateForm.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.shape({
            formMode: PropTypes.oneOf([
                BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE,
                BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT,
                BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW,
            ]),
            templateGroupUUID: PropTypes.string,
            businessRuleUUID: PropTypes.string,
        }),
    }).isRequired,
};
