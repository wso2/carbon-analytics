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
import Button from 'material-ui/Button';
import TextField from 'material-ui/TextField';
import { Typography } from 'material-ui';
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';
import Snackbar from 'material-ui/Snackbar';
import Slide from 'material-ui/transitions/Slide';
// App Components
import Property from './elements/Property';
import InputComponent from './elements/fromscratch/InputComponent';
import OutputComponent from './elements/fromscratch/OutputComponent';
import FilterComponent from './elements/fromscratch/FilterComponent';
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
 * Represents the form, which allows to fill and create a Business Rules from scratch
 */
export default class BusinessRuleFromScratchForm extends Component {
    constructor(props) {
        super(props);
        this.state = {
            formMode: this.props.match.params.formMode,
            inputRuleTemplates: [],
            outputRuleTemplates: [],

            businessRuleName: '',
            businessRuleUUID: '',
            selectedTemplateGroup: {},
            selectedInputRuleTemplate: {},
            selectedOutputRuleTemplate: {},
            businessRuleProperties: {
                inputData: {},
                ruleComponents: {
                    filterRules: [],
                    ruleLogic: '',
                },
                outputData: {},
                outputMappings: {},
            },

            // Expanded states of components
            isInputComponentExpanded: false,
            isFilterComponentExpanded: false,
            isOutputComponentExpanded: false,

            // Snackbar
            displaySnackbar: false,
            snackbarMessage: '',

            // For form validation purpose
            showSubmitButtons: true,
            isFieldErrorStatesDirty: false,
            fieldErrorStates: {},

            // To show Progress or Unauthorized message
            hasLoaded: false,
            errorCode: BusinessRulesConstants.ERROR_CODES.UNKNOWN,
        };
    }

    componentDidMount() {
        if (this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE) {
            // 'Create' mode
            const templateGroupUUID = this.props.match.params.templateGroupUUID;
            this.loadNewForm(templateGroupUUID);
        } else {
            // 'Edit' or 'View' mode
            const businessRuleUUID = this.props.match.params.businessRuleUUID;
            this.loadExistingForm(businessRuleUUID);
        }
    }

    /**
     * Returns Property Components, for properties of the Business Rule
     * @param {String} propertyType     Type of the Property
     * @param {String} formMode         Mode of the Business Rule form
     * @returns {Component[]}           Property Components
     */
    getPropertyComponents(propertyType, formMode) {
        let unArrangedPropertiesFromTemplate = []; // To store values that are going to be used

        // Get properties from the rule templates
        if (propertyType === BusinessRulesConstants.INPUT_DATA_KEY) {
            if (!BusinessRulesUtilityFunctions.isEmpty(this.state.selectedInputRuleTemplate)) {
                unArrangedPropertiesFromTemplate = this.state.selectedInputRuleTemplate.properties;
            }
        } else if (propertyType === BusinessRulesConstants.OUTPUT_DATA_KEY) {
            if (!BusinessRulesUtilityFunctions.isEmpty(this.state.selectedOutputRuleTemplate)) {
                unArrangedPropertiesFromTemplate = this.state.selectedOutputRuleTemplate.properties;
            }
        }

        return Object.keys(unArrangedPropertiesFromTemplate).map(property =>
            (<Property
                key={property}
                name={property}
                fieldName={unArrangedPropertiesFromTemplate[property].fieldName}
                description={unArrangedPropertiesFromTemplate[property].description ?
                    unArrangedPropertiesFromTemplate[property].description : ''}
                value={(this.state.businessRuleProperties[propertyType][property]) ?
                    (this.state.businessRuleProperties[propertyType][property]) : ('')}
                errorState={
                    !BusinessRulesUtilityFunctions.isEmpty(this.state.fieldErrorStates.properties[propertyType]) ?
                        this.state.fieldErrorStates.properties[propertyType][property] : false}
                disabledState={formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                options={unArrangedPropertiesFromTemplate[property].options}
                onValueChange={e => this.updatePropertyValue(property, propertyType, e)}
                fullWidth
            />));
    }

    /**
     * Gets names of the fields from the given Stream Definition, as an array
     * @param {String} streamDefinition     Stream Definition
     * @returns {Array}                     Field names
     */
    getFieldNames(streamDefinition) {
        const fieldNames = [];
        for (const field in this.getFieldNamesAndTypes(streamDefinition)) {
            if (Object.prototype.hasOwnProperty.call(this.getFieldNamesAndTypes(streamDefinition), field)) {
                fieldNames.push(field.toString());
            }
        }
        return fieldNames;
    }

    /**
     * Gets the given Stream Definition's field names as keys, and types as values in an object
     * @param {String} streamDefinition     Stream Definition
     * @returns {Object}                    Field names and types
     */
    getFieldNamesAndTypes(streamDefinition) {
        const regExp = /\(([^)]+)\)/;
        const matches = regExp.exec(streamDefinition);
        if (matches === null) {
            return {};
        }
        const fields = {};
        // Keep the field name and type, as each element in an array
        for (const field of matches[1].split(',')) {
            // Key: name, Value: type
            const fieldName = field.trim().split(' ')[0];
            fields[fieldName] = field.trim().split(' ')[1];
        }
        return fields;
    }

    /**
     * Gets default error states of the given business rule properties
     * @param {Object} businessRuleProperties       Business Rule Properties object
     * @returns {Object}                            Object that contains error states of business rule fields
     */
    getDefaultErrorStates(businessRuleProperties) {
        const errorStates = {
            inputData: {},
            ruleComponents: {
                filterRules: [],
                ruleLogic: false,
            },
            outputData: {},
            outputMappings: {},
            // For highlighting the sections
            inputComponent: false,
            filterComponent: false,
            outputComponent: false,
        };

        // Input Data
        for (const propertyKey in businessRuleProperties.inputData) {
            if (Object.prototype.hasOwnProperty.call(businessRuleProperties.inputData, propertyKey)) {
                errorStates.inputData[propertyKey] = false;
            }
        }
        // Output Data
        for (const propertyKey in businessRuleProperties.outputData) {
            if (Object.prototype.hasOwnProperty.call(businessRuleProperties.outputData, propertyKey)) {
                errorStates.outputData[propertyKey] = false;
            }
        }
        // Output Mappings
        for (const field in businessRuleProperties.outputMappings) {
            if (Object.prototype.hasOwnProperty.call(businessRuleProperties.outputMappings, field)) {
                errorStates.outputMappings[field] = false;
            }
        }
        // Rule Logic
        errorStates.ruleComponents.ruleLogic = this.getRuleLogicErrorState();
        // Filter Rules
        const filterRules = [];
        for (let i = 0; i < businessRuleProperties.ruleComponents.filterRules.length; i++) {
            filterRules.push([false, false, false]);
        }
        errorStates.ruleComponents.filterRules = filterRules;

        return {
            businessRuleName: false,
            properties: errorStates,
        };
    }

    /**
     * Returns the Promise for submitting a business rule for creating/updating
     * @param {Object} businessRuleObject       Business rule object to be submitted
     * @param {boolean} deployStatus            Whether to deploy the business rule or not
     * @param {boolean} isUpdate                Whether to update or create the business rule
     * @returns {AxiosPromise}                  Promise to perform create/update with a business rule object
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
     * Gets error state of the Rule Logic
     * @returns {boolean}       Error state of the Rule Logic
     */
    getRuleLogicErrorState() {
        // If rule logic exists
        if (!BusinessRulesUtilityFunctions.isEmpty(this.state.businessRuleProperties.ruleComponents) &&
            this.state.businessRuleProperties.ruleComponents.ruleLogic !== '') {
            const ruleLogic = this.state.businessRuleProperties.ruleComponents.ruleLogic;

            // Get all the numbers, mentioned in the rule logic
            const numberPattern = /\d+/g;
            for (const number of ruleLogic.match(numberPattern)) {
                // If a number exceeds the latest filter rule's number, a corresponding filter rule is not available
                if (number > this.state.businessRuleProperties.ruleComponents.filterRules.length) {
                    return true;
                }
            }
        }
        return false;
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
                        // Filter rule templates
                        const inputRuleTemplates = [];
                        const outputRuleTemplates = [];
                        for (const ruleTemplate of ruleTemplatesResponse.data[2]) {
                            if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_INPUT) {
                                inputRuleTemplates.push(ruleTemplate);
                            } else if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_OUTPUT) {
                                outputRuleTemplates.push(ruleTemplate);
                            }
                        }
                        this.setState({
                            selectedTemplateGroup: templateGroup,
                            inputRuleTemplates,
                            outputRuleTemplates,
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
                    .then((templateGroupResponse) => {
                        const templateGroup = templateGroupResponse.data[2];
                        new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
                            .getRuleTemplates(templateGroup.uuid)
                            .then((ruleTemplatesResponse) => {
                                // Filter rule templates
                                const inputRuleTemplates = [];
                                const outputRuleTemplates = [];
                                for (const ruleTemplate of ruleTemplatesResponse.data[2]) {
                                    if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_OUTPUT) {
                                        outputRuleTemplates.push(ruleTemplate);
                                    } else if (ruleTemplate.type ===
                                        BusinessRulesConstants.RULE_TEMPLATE_TYPE_INPUT) {
                                        inputRuleTemplates.push(ruleTemplate);
                                    }
                                    new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
                                        .getRuleTemplate(businessRule.templateGroupUUID,
                                            businessRule.inputRuleTemplateUUID)
                                        .then((selectedInputRuleTemplateResponse) => {
                                            const selectedInputRuleTemplate =
                                                selectedInputRuleTemplateResponse.data[2];
                                            new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
                                                .getRuleTemplate(businessRule.templateGroupUUID,
                                                    businessRule.outputRuleTemplateUUID)
                                                .then((selectedOutputRuleTemplateResponse) => {
                                                    const selectedOutputRuleTemplate
                                                        = selectedOutputRuleTemplateResponse.data[2];
                                                    this.setState({
                                                        businessRuleType:
                                                        BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH,
                                                        businessRuleName: businessRule.name,
                                                        businessRuleUUID: businessRule.uuid,
                                                        selectedTemplateGroup: templateGroup,
                                                        inputRuleTemplates,
                                                        outputRuleTemplates,
                                                        selectedInputRuleTemplate,
                                                        selectedOutputRuleTemplate,
                                                        businessRuleProperties:
                                                            this.unwrapBusinessRuleProperties(
                                                                businessRule.properties),
                                                        fieldErrorStates:
                                                            this.getDefaultErrorStates(businessRule.properties),
                                                        hasLoaded: true,
                                                        errorCode: BusinessRulesConstants.ERROR_CODES.NONE,
                                                    });
                                                })
                                                .catch((error) => {
                                                    // Error in Loading Selected Output Rule Template
                                                    this.setState({
                                                        hasLoaded: true,
                                                        errorCode: BusinessRulesUtilityFunctions
                                                            .getErrorDisplayCode(error),
                                                    });
                                                });
                                        })
                                        .catch((error) => {
                                            // Error in Loading Selected Input Template
                                            this.setState({
                                                hasLoaded: true,
                                                errorCode: BusinessRulesUtilityFunctions.getErrorDisplayCode(error),
                                            });
                                        });
                                }
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
                        // Error in loading the Template Group
                        this.setState({
                            hasLoaded: true,
                            errorCode: BusinessRulesUtilityFunctions.getErrorDisplayCode(error),
                        });
                    });
            })
            .catch((error) => {
                // Error in loading the Business Rule
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
        if (BusinessRulesUtilityFunctions.isEmpty(this.state.selectedInputRuleTemplate) ||
            this.state.selectedInputRuleTemplate.uuid === '') {
            throw new FormSubmissionError(fieldErrorStates, 'Please select a valid Input Rule Template');
        }
        if (BusinessRulesUtilityFunctions.isEmpty(this.state.selectedOutputRuleTemplate) ||
            this.state.selectedOutputRuleTemplate.uuid === '') {
            throw new FormSubmissionError(fieldErrorStates, 'Please select a valid Output Rule Template');
        }
        if ((this.state.businessRuleName.match(BusinessRulesConstants.BUSINESS_RULE_NAME_REGEX) === null) ||
            (this.state.businessRuleName.match(BusinessRulesConstants.BUSINESS_RULE_NAME_REGEX)[0] !==
                this.state.businessRuleName)) {
            fieldErrorStates.businessRuleName = true;
            throw new FormSubmissionError(fieldErrorStates, 'Please enter a valid name for the Business Rule');
        }

        // Input Data
        if (BusinessRulesUtilityFunctions.isEmpty(this.state.businessRuleProperties.inputData)) {
            fieldErrorStates.properties.inputComponent = true;
            throw new FormSubmissionError(fieldErrorStates, 'Invalid Input data found');
        }
        let isAnyPropertyEmpty = false;
        for (const propertyKey in this.state.businessRuleProperties.inputData) {
            if (Object.prototype.hasOwnProperty.call(this.state.businessRuleProperties.inputData, propertyKey)) {
                if (this.state.businessRuleProperties.inputData[propertyKey] === '') {
                    fieldErrorStates.properties.inputData[propertyKey] = true;
                    isAnyPropertyEmpty = true;
                }
            }
        }
        if (isAnyPropertyEmpty) {
            fieldErrorStates.properties.inputComponent = true;
            throw new FormSubmissionError(fieldErrorStates, 'Please fill in values for all Input data properties');
        }

        // Output Data
        if (BusinessRulesUtilityFunctions.isEmpty(this.state.businessRuleProperties.outputData)) {
            fieldErrorStates.properties.outputComponent = true;
            throw new FormSubmissionError(fieldErrorStates, 'Invalid Output data found');
        }
        for (const propertyKey in this.state.businessRuleProperties.outputData) {
            if (Object.prototype.hasOwnProperty.call(this.state.businessRuleProperties.outputData, propertyKey)) {
                if (this.state.businessRuleProperties.outputData[propertyKey] === '') {
                    fieldErrorStates.properties.outputData[propertyKey] = true;
                    isAnyPropertyEmpty = true;
                }
            }
        }
        if (isAnyPropertyEmpty) {
            fieldErrorStates.properties.outputComponent = true;
            throw new FormSubmissionError(fieldErrorStates, 'Please fill in values for all Output data properties');
        }

        // Output Mappings
        if (BusinessRulesUtilityFunctions.isEmpty(this.state.businessRuleProperties.outputMappings)) {
            fieldErrorStates.properties.outputComponent = true;
            throw new FormSubmissionError(fieldErrorStates, 'Invalid Output Mappings found');
        }
        for (const outputFieldName in this.state.businessRuleProperties.outputMappings) {
            if (Object.prototype.hasOwnProperty.call(
                this.state.businessRuleProperties.outputMappings, outputFieldName)) {
                if (this.state.businessRuleProperties.outputMappings[outputFieldName] === '') {
                    fieldErrorStates.properties.outputMappings[outputFieldName] = true;
                    isAnyPropertyEmpty = true;
                }
            }
        }
        if (isAnyPropertyEmpty) {
            fieldErrorStates.properties.outputComponent = true;
            throw new FormSubmissionError(fieldErrorStates, 'Please fill in values for all the mappings');
        }

        // Rule Components
        if (BusinessRulesUtilityFunctions.isEmpty(this.state.businessRuleProperties.ruleComponents)) {
            fieldErrorStates.properties.filterComponent = true;
            throw new FormSubmissionError(fieldErrorStates, 'Invalid Rule Component data found');
        }
        // Rule Logic
        if (this.state.businessRuleProperties.ruleComponents.filterRules.length > 0 &&
            this.state.businessRuleProperties.ruleComponents.ruleLogic === '') {
            fieldErrorStates.properties.filterComponent = true;
            fieldErrorStates.properties.ruleComponents.ruleLogic = true;
            throw new FormSubmissionError(
                fieldErrorStates, 'Rule Logic can not be empty, when one or more Filter Rules are there');
        }
        if (this.getRuleLogicErrorState()) {
            fieldErrorStates.properties.filterComponent = true;
            fieldErrorStates.properties.ruleComponents.ruleLogic = true;
            throw new FormSubmissionError(fieldErrorStates, 'Can not find Filter Rule(s) referred in Rule Logic');
        }
        // Filter Rules
        const filterRules = [];
        let isFilterRulesErroneous = false;
        for (let i = 0; i < this.state.businessRuleProperties.ruleComponents.filterRules.length; i++) {
            const filterRule = [];
            for (let j = 0; j < 3; j++) {
                if (this.state.businessRuleProperties.ruleComponents.filterRules[i][j] === '') {
                    isFilterRulesErroneous = true;
                }
                filterRule.push(this.state.businessRuleProperties.ruleComponents.filterRules[i][j] === '');
            }
            filterRules.push(filterRule);
        }
        fieldErrorStates.properties.ruleComponents.filterRules = filterRules;
        if (isFilterRulesErroneous) {
            fieldErrorStates.properties.filterComponent = true;
            throw new FormSubmissionError(fieldErrorStates, 'Invalid Filter Rule(s) found');
        }

        return true;
    }

    /**
     * Submits the entered properties of the business rule for saving
     * @param {boolean} shouldDeploy        Whether to deploy the business rule or not
     * @param {boolean} isUpdate            Whether to update or create the business rule
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
                    type: BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH,
                    templateGroupUUID: this.state.selectedTemplateGroup.uuid,
                    inputRuleTemplateUUID: this.state.selectedInputRuleTemplate.uuid,
                    outputRuleTemplateUUID: this.state.selectedOutputRuleTemplate.uuid,
                    properties: this.wrapBusinessRuleProperties(this.state.businessRuleProperties),
                };

                this.getSubmitPromise(businessRuleObject, shouldDeploy, isUpdate)
                    .then((response) => {
                        this.toggleSnackbar(response.data[1]);
                        setTimeout(() => {
                            window.location.href = appContext + '/businessRulesManager';
                        }, 3000);
                    })
                    .catch((error) => {
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
     * Prepares the business rule properties object for submission
     * @param {Object} businessRuleProperties       Business rule properties
     * @returns {Object}                            Modified business rule properties, for submission
     */
    wrapBusinessRuleProperties(businessRuleProperties) {
        const properties = JSON.parse(JSON.stringify(businessRuleProperties));
        const filterRules = [];
        for (const filterRule of properties.ruleComponents.filterRules) {
            filterRules.push(filterRule.join(' '));
        }
        properties.ruleComponents = {
            filterRules,
            ruleLogic: [properties.ruleComponents.ruleLogic],
        };
        return properties;
    }

    /**
     * Prepares the business rule properties object, to be able to be handled in the form
     * @param {Object} businessRuleProperties       Business rule properties
     * @returns {Object}                            Modified business rule properties, for handling in the form
     */
    unwrapBusinessRuleProperties(businessRuleProperties) {
        // To avoid mutating the param object, as it is not referenced from the state
        const properties = JSON.parse(JSON.stringify(businessRuleProperties));
        // TODO handle saved with old implementation data
        const filterRules = [];
        for (const filterRule of properties.ruleComponents.filterRules) {
            filterRules.push(filterRule.split(' '));
        }
        properties.ruleComponents = {
            filterRules,
            ruleLogic: properties.ruleComponents.ruleLogic[0],
        };
        return properties;
    }

    /**
     * Handles onChange action of the business rule name text field
     * @param {Object} event        OnChange event of the business rule name text field
     */
    handleBusinessRuleNameChange(event) {
        const state = this.state;
        state.businessRuleName = event.target.value;
        state.businessRuleUUID = BusinessRulesUtilityFunctions.generateBusinessRuleUUID(event.target.value);
        this.setState(state);
        this.resetErrorStates();
    }

    /**
     * Updates the Input Rule Template selection
     * @param {Object} event        Selection event of the Input Rule Template
     */
    handleInputRuleTemplateSelected(event) {
        const state = this.state;
        new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
            .getRuleTemplate(this.state.selectedTemplateGroup.uuid, event.target.value)
            .then((response) => {
                state.selectedInputRuleTemplate = response.data[2];
                // Set default values as inputData values in state
                for (const propertyKey in state.selectedInputRuleTemplate.properties) {
                    if (Object.prototype.hasOwnProperty.call(state.selectedInputRuleTemplate.properties,
                        propertyKey)) {
                        state.businessRuleProperties.inputData[propertyKey] =
                            state.selectedInputRuleTemplate.properties[propertyKey].defaultValue;
                    }
                }
                state.fieldErrorStates = this.getDefaultErrorStates(state.businessRuleProperties);
                state.hasLoaded = true;
                state.errorCode = BusinessRulesConstants.ERROR_CODES.NONE;
                this.setState(state);
            })
            .catch((error) => {
                // Error in Loading Input Rule Template Properties
                this.setState({
                    hasLoaded: true,
                    errorCode: BusinessRulesUtilityFunctions.getErrorDisplayCode(error),
                });
            });
    }

    /**
     * Updates the Output Rule Template selection
     * @param {Object} event        Selection event of the Output Rule Template
     */
    handleOutputRuleTemplateSelected(event) {
        const state = this.state;
        new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
            .getRuleTemplate(this.state.selectedTemplateGroup.uuid, event.target.value)
            .then((response) => {
                state.selectedOutputRuleTemplate = response.data[2];
                // Set default values as outputData values in state
                for (const propertyKey in state.selectedOutputRuleTemplate.properties) {
                    if (Object.prototype.hasOwnProperty.call(state.selectedOutputRuleTemplate.properties,
                        propertyKey)) {
                        state.businessRuleProperties.outputData[propertyKey] =
                            state.selectedOutputRuleTemplate.properties[propertyKey.toString()].defaultValue;
                    }
                }

                // Generate Output Mappings object
                const outputMappings = {};
                for (const fieldName of this.getFieldNames(
                    state.selectedOutputRuleTemplate.templates[0].exposedStreamDefinition)) {
                    outputMappings[fieldName] = '';
                }

                state.businessRuleProperties.outputMappings = outputMappings;
                state.fieldErrorStates = this.getDefaultErrorStates(state.businessRuleProperties);
                state.hasLoaded = true;
                state.errorCode = BusinessRulesConstants.ERROR_CODES.NONE;
                this.setState(state);
            })
            .catch((error) => {
                // Error in Loading Output Rule Template Properties
                this.setState({
                    hasLoaded: true,
                    errorCode: BusinessRulesUtilityFunctions.getErrorDisplayCode(error),
                });
            });
    }

    /**
     * Updates RuleComponents
     * @param {Object} ruleComponents       Modified 'ruleComponents' part
     */
    updateRuleComponents(ruleComponents) {
        const state = this.state;
        state.businessRuleProperties.ruleComponents = ruleComponents;
        this.setState(state);
        this.resetErrorStates();
    }

    /**
     * Updates Output Mapping (value As outputFieldName)
     * @param {String} value                Value that is mapped
     * @param {String} outputFieldName      Name of the Output Field
     */
    updateOutputMapping(value, outputFieldName) {
        const state = this.state;
        state.businessRuleProperties.outputMappings[outputFieldName] = value;
        this.setState(state);
        this.resetErrorStates();
    }

    /**
     * Updates the given property which is of the given type, with the given value
     * @param {String} property         Name of the Property
     * @param {String} propertyType     Type (sub-section) of the Property
     * @param {String} value            Value of the Property
     */
    updatePropertyValue(property, propertyType, value) {
        const state = this.state;
        state.businessRuleProperties[propertyType][property] = value;
        this.setState(state);
        this.resetErrorStates();
    }

    /**
     * Resets error states of all the properties of the business rule, if any of the field is currently having an error
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
     * Returns Input Component
     * @returns {Component}     Input Component
     */
    displayInputComponent() {
        return (
            <InputComponent
                mode={this.state.formMode}
                isExpanded={this.state.isInputComponentExpanded}
                isErroneous={
                    this.state.fieldErrorStates.properties ?
                        this.state.fieldErrorStates.properties.inputComponent : false}
                inputRuleTemplates={this.state.inputRuleTemplates}
                selectedRuleTemplate={this.state.selectedInputRuleTemplate}
                getFieldNamesAndTypes={streamDefinition =>
                    this.getFieldNamesAndTypes(streamDefinition)}
                getFieldNames={streamDefinition => this.getFieldNames(streamDefinition)}
                handleInputRuleTemplateSelected={e => this.handleInputRuleTemplateSelected(e)}
                getPropertyComponents={(propertiesType, formMode) =>
                    this.getPropertyComponents(propertiesType, formMode)}
                toggleExpansion={() => this.toggleInputComponentExpansion()}
            />
        );
    }

    /**
     * Returns Filter Component
     * @returns {Component}     Filter Component
     */
    displayFilterComponent() {
        return (
            <FilterComponent
                isExpanded={this.state.isFilterComponentExpanded}
                isErroneous={
                    this.state.fieldErrorStates.properties ?
                        this.state.fieldErrorStates.properties.filterComponent : false}
                errorStates={
                    this.state.fieldErrorStates.properties ?
                        this.state.fieldErrorStates.properties.ruleComponents : {}}
                toggleExpansion={() => this.toggleFilterComponentExpansion()}
                onUpdate={ruleComponents => this.updateRuleComponents(ruleComponents)}
                formMode={this.state.formMode}
                selectedInputRuleTemplate={this.state.selectedInputRuleTemplate}
                getFieldNamesAndTypes={streamDefinition =>
                    this.getFieldNamesAndTypes(streamDefinition)}
                getFieldNames={streamDefinition => this.getFieldNames(streamDefinition)}
                ruleComponents={this.state.businessRuleProperties.ruleComponents}
            />
        );
    }

    /**
     * Returns Output Component
     * @returns {Component}     Output Component
     */
    displayOutputComponent() {
        return (
            <OutputComponent
                mode={this.state.formMode}
                isErroneous={
                    this.state.fieldErrorStates.properties ?
                        this.state.fieldErrorStates.properties.outputComponent : false}
                mappingErrorStates={
                    !BusinessRulesUtilityFunctions.isEmpty(this.state.fieldErrorStates) ?
                        this.state.fieldErrorStates.properties.outputMappings : {}
                }
                outputRuleTemplates={this.state.outputRuleTemplates}
                getFieldNames={streamDefinition => this.getFieldNames(streamDefinition)}
                selectedOutputRuleTemplate={this.state.selectedOutputRuleTemplate}
                selectedInputRuleTemplate={this.state.selectedInputRuleTemplate}
                inputStreamFields={
                    !BusinessRulesUtilityFunctions.isEmpty(
                        this.state.selectedInputRuleTemplate) ?
                        this.getFieldNames(
                            this.state.selectedInputRuleTemplate.templates[0]
                                .exposedStreamDefinition) : []
                }
                handleOutputRuleTemplateSelected={e =>
                    this.handleOutputRuleTemplateSelected(e)}
                handleOutputMappingChange={(value, fieldName) =>
                    this.updateOutputMapping(value.newValue, fieldName)}
                getPropertyComponents={(propertiesType, formMode) =>
                    this.getPropertyComponents(propertiesType, formMode)}
                businessRuleProperties={this.state.businessRuleProperties}
                isExpanded={this.state.isOutputComponentExpanded}
                toggleExpansion={() => this.toggleOutputComponentExpansion()}
            />
        );
    }

    /**
     * Displays content of the page
     */
    displayContent() {
        if (this.state.hasLoaded) {
            if (this.state.errorCode === BusinessRulesConstants.ERROR_CODES.NONE) {
                return (
                    <div>
                        {this.displaySnackbar()}
                        <Grid container spacing={24} style={Styles.businessRuleForm.root} justify="center">
                            <Grid item xs={12} sm={7}>
                                <Paper style={Styles.businessRuleForm.paper}>
                                    <center>
                                        <Typography type="headline">
                                            {this.state.selectedTemplateGroup.name}
                                        </Typography>
                                        <Typography type="subheading">
                                            {this.state.selectedTemplateGroup.description ?
                                                this.state.selectedTemplateGroup.description : ''}
                                        </Typography>
                                        <br />
                                        {/* Business Rule Name */}
                                        <TextField
                                            id="businessRuleName"
                                            name="businessRuleName"
                                            label={BusinessRulesMessages.BUSINESS_RULE_NAME_FIELD_NAME}
                                            placeholder={BusinessRulesMessages.BUSINESS_RULE_NAME_FIELD_DESCRIPTION}
                                            value={this.state.businessRuleName}
                                            onChange={e => this.handleBusinessRuleNameChange(e)}
                                            disabled={(this.state.formMode !==
                                                BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE)}
                                            error={this.state.fieldErrorStates.businessRuleName}
                                            required
                                            fullWidth
                                            margin="normal"
                                        />
                                    </center>
                                    <br />
                                    <br />
                                    {this.displayInputComponent()}
                                    <br />
                                    {this.displayFilterComponent()}
                                    <br />
                                    {this.displayOutputComponent()}
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

    /**
     * Returns the Snackbar
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
     * Returns Submit Buttons
     * @returns {HTMLElement}       Submit Buttons
     */
    displaySubmitButtons() {
        if (this.state.showSubmitButtons) {
            if (this.state.formMode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW &&
                !BusinessRulesUtilityFunctions.isEmpty(this.state.selectedInputRuleTemplate) &&
                !BusinessRulesUtilityFunctions.isEmpty(this.state.selectedOutputRuleTemplate)) {
                return (
                    <div>
                        <Button
                            raised
                            color="default"
                            style={{ marginRight: 10 }}
                            onClick={() => this.submitBusinessRule(
                                false, this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT)}
                        >
                            Save
                        </Button>
                        <Button
                            raised
                            color="primary"
                            style={{ marginRight: 10 }}
                            onClick={() => this.submitBusinessRule(
                                true, this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT)}
                        >
                            Save & Deploy
                        </Button>
                        <Button
                            color="default"
                            style={{ marginRight: 10 }}
                            onClick={() => {
                                window.location.href = appContext + '/businessRulesManager';
                            }}
                        >
                            Cancel
                        </Button>
                    </div>);
            }
        }
        return null;
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
            });
        }
    }

    /**
     * Toggles expansion of the Input Component
     */
    toggleInputComponentExpansion() {
        this.setState({ isInputComponentExpanded: !this.state.isInputComponentExpanded });
    }

    /**
     * Toggles expansion of the Filter Component
     */
    toggleFilterComponentExpansion() {
        this.setState({ isFilterComponentExpanded: !this.state.isFilterComponentExpanded });
    }

    /**
     * Toggles expansion of the Output Component
     */
    toggleOutputComponentExpansion() {
        this.setState({ isOutputComponentExpanded: !this.state.isOutputComponentExpanded });
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

BusinessRuleFromScratchForm.propTypes = {
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
