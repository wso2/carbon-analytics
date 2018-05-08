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
import Dialog, { DialogActions, DialogContent, DialogContentText, DialogTitle } from 'material-ui/Dialog';
import { Typography } from 'material-ui';
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';
import Snackbar from 'material-ui/Snackbar';
import Slide from 'material-ui/transitions/Slide';
// App Components
import Property from './elements/businessruleform/Property';
import InputComponent from './elements/businessruleform/fromscratch/InputComponent';
import OutputComponent from './elements/businessruleform/fromscratch/OutputComponent';
import FilterComponent from './elements/businessruleform/fromscratch/FilterComponent';
import Header from '../common/Header';
import ProgressDisplay from '../common/ProgressDisplay';
import ErrorDisplay from './elements/error/ErrorDisplay';
// App Utilities
import BusinessRulesUtilityFunctions from '../../utils/BusinessRulesUtilityFunctions';
// App Constants
import BusinessRulesConstants from '../../constants/BusinessRulesConstants';
import BusinessRulesMessages from '../../constants/BusinessRulesMessages';
// App APIs
import BusinessRulesAPI from '../../api/BusinessRulesAPI';
// CSS
import '../../index.css';
import FormSubmissionError from "../../utils/FormSubmissionError";

/**
 * Styles related to this component
 */
const styles = {
    addFilterRuleButton: {
        backgroundColor: '#EF6C00',
        color: 'white',
    },
    button: {
        backgroundColor: '#EF6C00',
        color: 'white',
        marginRight: 10,
    },
    secondaryButton: {
        marginRight: 10,
    },
    paper: {
        padding: 40,
        paddingTop: 15,
        paddingBottom: 15,
    },
    paperContainer: {
        margin: 40,
        marginTop: 15,
        marginBottom: 15,
    },
    root: {
        width: '100%',
        maxWidth: 360,
        position: 'relative',
        overflow: 'auto',
        maxHeight: 300,
    },
    rootGrid: {
        flexGrow: 1,
        paddingTop: 20,
    },
    propertyComponentPadding: {
        paddingLeft: 40,
        paddingRight: 40,
    },
    listSection: {
        background: 'inherit',
    },
    formRoot: {
        flexGrow: 1,
        marginTop: 30,
    },
    formPaper: {
        padding: 50,
    },
    snackbar: {
        direction: 'up',
    },
};

/**
 * App context.
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
                    // filterRules: [], // TODO REMOVE THIS OLD String[]. Now it's String[][]
                    filterRules: [],
                    // ruleLogic: [''], // TODO REMOVE THIS OLD ONE
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
        } else {
            // 'Edit' or 'View' mode
            const businessRuleUUID = this.props.match.params.businessRuleUUID;
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
                                                            businessRuleProperties: businessRule.properties,
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
    }

    /**
     * Returns Property Components, for properties of the Business Rule
     * @param {string} propertyType     Type of the Property
     * @param {string} formMode         Mode of the Business Rule form
     * @returns {Component[]}           Property Components
     */
    getPropertyComponents(propertyType, formMode) {
        let unArrangedPropertiesFromTemplate = []; // To store values that are going to be used

        // Get properties from the rule templates
        if (propertyType === BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_INPUT) {
            if (!BusinessRulesUtilityFunctions.isEmpty(this.state.selectedInputRuleTemplate)) {
                unArrangedPropertiesFromTemplate = this.state.selectedInputRuleTemplate.properties;
            }
        } else if (propertyType === BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_OUTPUT) {
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
                errorState={this.state.fieldErrorStates.properties[propertyType][property]}
                // TODO ERROR HANDLING FOR ABOVE
                disabledState={formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                options={unArrangedPropertiesFromTemplate[property].options}
                onValueChange={e => this.updatePropertyValue(property, propertyType, e)}
                fullWidth
            />));
    }

    /**
     * Gets field names of the given Stream Definition, as an array
     * @param {string} streamDefinition     Stream Definition
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
     * @param {string} streamDefinition     Stream Definition
     * @returns {Object}                    Field names and types
     */
    getFieldNamesAndTypes(streamDefinition) {
        const regExp = /\(([^)]+)\)/;
        const matches = regExp.exec(streamDefinition);
        const fields = {};
        // Keep the field name and type, as each element in an array
        for (const field of matches[1].split(',')) {
            // Key: name, Value: type
            const fieldName = field.trim().split(' ')[0];
            fields[fieldName.toString()] = field.trim().split(' ')[1];
        }
        return fields;
    }

    /**
     * Shows the snackbar with the given message, or hides when no message is given
     * @param {string} message       Snackbar message text
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

    // Filter Rule related functions todo organize
    updateFilterRule(filterRuleIndex, filterRuleArray) {
        const state = this.state;
        state.businessRuleProperties.ruleComponents.filterRules[filterRuleIndex] = filterRuleArray;
        this.setState(state);
    }

    // TODO The following should be removed [start]--------------------------------
    /**
     * Updates the Attribute of the Filter Rule, that has the given index
     * @param {number} filterRuleIndex      Index of the Filter rule, whose Attribute was modified
     * @param {string} value                Value of the Attribute
     */
    updateFilterRuleAttribute(filterRuleIndex, value) {
        // TODO refactor to object[variable]
        const ruleComponentType = BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_RULE_COMPONENTS;
        const ruleComponentFilterRuleType =
            BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_RULE_COMPONENT_PROPERTY_TYPE_FILTER_RULES;

        const state = this.state;
        state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex] =
            value + ' ' +
            state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex].split(' ')[1]
            + ' ' +
            state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex].split(' ')[2];
        this.setState(state);
    }

    /**
     * Updates the Operator of the Filter Rule, that has the given index
     * @param {number} filterRuleIndex      Index of the Filter rule, whose Operator was modified
     * @param {string} value                Value of the Operator
     */
    updateFilterRuleOperator(filterRuleIndex, value) {
        const ruleComponentType = BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_RULE_COMPONENTS;
        const ruleComponentFilterRuleType =
            BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_RULE_COMPONENT_PROPERTY_TYPE_FILTER_RULES;

        const state = this.state;
        state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex] =
            state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex].split(' ')[0]
            + ' ' + value + ' ' +
            state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex].split(' ')[2];
        this.setState(state);
    }

    /**
     * Updates the Attribute Or Value of the Filter Rule, that has the given index
     * @param {number} filterRuleIndex      Index of the Filter rule, whose Attribute Or Value was modified
     * @param {string} value                Value of the Attribute Or Value
     */
    updateFilterRuleAttributeOrValue(filterRuleIndex, value) {
        const ruleComponentType = BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_RULE_COMPONENTS;
        const ruleComponentFilterRuleType =
            BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_RULE_COMPONENT_PROPERTY_TYPE_FILTER_RULES;

        const state = this.state;
        state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex] =
            state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex].split(' ')[0]
            + ' ' +
            state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex].split(' ')[1]
            + ' ' + value;
        this.setState(state);
    }
    // TODO ----------------------------------------------------------------------------------------

    // END OF FILTER RULE FUNCTIONS

    /**
     * Updates the Rule Logic, with the given value
     * @param {string} value        Rule Logic
     */
    updateRuleLogic(value) {
        // TODO under construction
        const state = this.state;
        state.businessRuleProperties.ruleComponents.ruleLogic = value;
        this.setState(state);

        // const state = this.state;
        // state.businessRuleProperties.ruleComponents.ruleLogic[0] = value;
        // this.setState(state);
    }

    generateDefaultRuleLogic(filterRules, ruleLogic) {
        if (this.state.businessRuleProperties.ruleComponents.filterRules.length === 0) {
            return '';
        }
        if (ruleLogic === '') {
            // No rule logic is present
            // Concatenate each filter rule numbers with AND and return
            const numbers = [];
            for (let i = 0; i < filterRules.length; i++) {
                numbers.push(i + 1);
            }
            return numbers.join(' AND ');
        } else {
            return ruleLogic + ' AND ' + filterRules.length;
        }
    }

    /**
     * Updates the Rule Logic, by concatenating the latest Rule Logic number with an 'AND'
     */
    updateDefaultRuleLogic() { // todo under reconstruction
        const state = this.state;
        state.businessRuleProperties.ruleComponents.ruleLogic =
            this.generateDefaultRuleLogic(
                state.businessRuleProperties.ruleComponents.filterRules,
                state.businessRuleProperties.ruleComponents.ruleLogic);
        this.setState(state);

        // const state = this.state;
        // const existingRuleLogic = state.businessRuleProperties.ruleComponents.ruleLogic[0];
        // // If a rule logic is not present
        // if (existingRuleLogic === '') {
        //     // No rule logic is present
        //     // Concatenate each filter rule numbers with AND and return
        //     const numbers = [];
        //     for (let i = 0; i < this.state.businessRuleProperties.ruleComponents.filterRules.length; i++) {
        //         numbers.push(i + 1);
        //     }
        //     state.businessRuleProperties.ruleComponents.ruleLogic[0] = numbers.join(' AND ');
        // } else {
        //     state.businessRuleProperties.ruleComponents.ruleLogic[0] =
        //         existingRuleLogic + ' AND ' +
        //         this.state.businessRuleProperties.ruleComponents.filterRules.length;
        // }
        // if (this.state.businessRuleProperties.ruleComponents.filterRules.length === 0) {
        //     state.businessRuleProperties.ruleComponents.ruleLogic[0] = '';
        // }
        // this.setState(state);
    }

    /**
     * Adds a new Filter Rule
     */
    addFilterRule() {
        // TODO modification in progress
        const state = this.state;
        state.businessRuleProperties.ruleComponents.filterRules.push(['', '', '']);
        this.setState(state);
        this.updateDefaultRuleLogic();

        // const state = this.state;
        // state.businessRuleProperties.ruleComponents.filterRules.push('  ');
        // this.setState(state);
        // this.updateDefaultRuleLogic();
    }

    /**
     * Removes the Filter Rule that has the given index
     * @param {number} index        Index of the Filter Rule
     */
    removeFilterRule(index) {
        const state = this.state;
        state.businessRuleProperties.ruleComponents.filterRules.splice(index, 1);
        this.setState(state);
        this.updateDefaultRuleLogic();
    }

    /**
     * Get the status, whether to give a warning for the Rule Logic, or not
     * @returns {boolean}       Status of the warning for the Rule Logic
     */
    getRuleLogicWarnStatus() {
        // TODO under reconstruction
        // If rule logic exists
        if (this.state.businessRuleProperties.ruleComponents.ruleLogic !== '') {
            const ruleLogic = this.state.businessRuleProperties.ruleComponents.ruleLogic[0];

            // Get all the numbers, mentioned in the rule logic
            const numberPattern = /\d+/g;
            for (const number of ruleLogic.match(numberPattern)) {
                // If a number exceeds the latest filter rule's number, a corresponding filter rule can not be found
                if (number > this.state.businessRuleProperties.ruleComponents.filterRules.length) {
                    return true;
                }
            }
        }
        return false;

        // // If rule logic exists
        // if (this.state.businessRuleProperties.ruleComponents.ruleLogic[0] &&
        //     this.state.businessRuleProperties.ruleComponents.ruleLogic[0] != null &&
        //     this.state.businessRuleProperties.ruleComponents.ruleLogic[0] !== '') {
        //     const ruleLogic = this.state.businessRuleProperties.ruleComponents.ruleLogic[0];
        //
        //     // Get all the numbers, mentioned in the rule logic
        //     const numberPattern = /\d+/g;
        //     for (const number of ruleLogic.match(numberPattern)) {
        //         // If a number exceeds the latest filter rule's number, a corresponding filter rule can not be found
        //         if (number > this.state.businessRuleProperties.ruleComponents.filterRules.length) {
        //             return true;
        //         }
        //     }
        // }
        // return false;
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

    /**
     * Updates the Input Rule Template selection
     * @param {object} event        Selection event of the Input Rule Template
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
                        state.businessRuleProperties[BusinessRulesConstants
                            .BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_INPUT][propertyKey] =
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
     * @param {object} event        Selection event of the Output Rule Template
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
                        state.businessRuleProperties[BusinessRulesConstants
                            .BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_OUTPUT][propertyKey] =
                            state.selectedOutputRuleTemplate.properties[propertyKey.toString()].defaultValue;
                    }
                }
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
     * Updates the Output Mapping (value As outputFieldName)
     * @param {object} value                Value object of the AutoSuggest field's onChange action
     * @param {string} outputFieldName      Name of the Output Field
     */
    updateOutputMapping(value, outputFieldName) {
        const state = this.state;
        state.businessRuleProperties.outputMappings[outputFieldName] = value.newValue;
        this.setState(state);
        this.resetErrorStates();
    }

    /**
     * Updates the given property which is of the given type, with the given value
     * @param {string} property         Name of the Property
     * @param {string} propertyType     Type (sub-section) of the Property
     * @param {string} value            Value of the Property
     */
    updatePropertyValue(property, propertyType, value) {
        const state = this.state;
        state.businessRuleProperties[propertyType][property] = value;
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

    getDefaultErrorStates(businessRuleProperties) {
        const errorStates = {
            inputData: {},
            ruleComponents: {
                filterRules: [],
                ruleLogic: [''],
            },
            outputData: {},
            outputMappings: {},
        };

        for (const propertyType in businessRuleProperties) {
            if (Object.prototype.hasOwnProperty.call(businessRuleProperties, propertyType)) {
                if (propertyType === BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_INPUT ||
                    propertyType === BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_OUTPUT) {
                    for (const propertyKey in businessRuleProperties[propertyType]) {
                        if (Object.prototype.hasOwnProperty.call(businessRuleProperties[propertyType], propertyKey)) {
                            errorStates[propertyType][propertyKey] = false;
                        }
                    }
                }
            }
        }
        return {
            businessRuleName: false,
            properties: errorStates,
        };
    }

    /**
     * Returns the Snackbar
     * @returns {Component}     Snackbar Component
     */
    showSnackbar() {
        return (
            <Snackbar
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
            />
        );
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

        // Validate property type components
        for (const propertyType in this.state.businessRuleProperties) {
            if (Object.prototype.hasOwnProperty.call(this.state.businessRuleProperties, propertyType)) {
                // Validation happens only for properties of type 'input' & 'output', not for 'ruleComponent'
                // TODO the above statement seems no!
                if (propertyType === BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_INPUT ||
                    propertyType === BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_OUTPUT) {
                    // If any 'inputData' or 'outputData' property type component is completely empty
                    if (BusinessRulesUtilityFunctions.isEmpty(this.state.businessRuleProperties[propertyType])) {
                        throw new FormSubmissionError(
                            fieldErrorStates,
                            'Invalid ' +
                            (propertyType ===
                            BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_INPUT ?
                                'input' : 'output') + ' properties found');
                        // TODO maybe improve by highlight 'INPUT COMPONENT' as red!
                    } else {
                        let isAnyPropertyEmpty = false;
                        // If any property of the specific property type in invalid
                        for (const propertyKey in this.state.businessRuleProperties[propertyType]) {
                            if (Object.prototype.hasOwnProperty.call(
                                this.state.businessRuleProperties[propertyType], propertyKey)) {
                                if (propertyType !==
                                    BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_RULE_COMPONENTS) {
                                    if (this.state.businessRuleProperties[propertyType][propertyKey] === '') {
                                        fieldErrorStates.properties[propertyType][propertyKey] = true;
                                        isAnyPropertyEmpty = true;
                                    }
                                }
                            } else {
                                isAnyPropertyEmpty = true;
                            }
                        }
                        if (isAnyPropertyEmpty) {
                            throw new FormSubmissionError(fieldErrorStates,
                                'Please fill in values for all the properties');
                        }
                    }
                } else if (propertyType ===
                    BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_RULE_PROPERTY_TYPE_OUTPUT_MAPPINGS) {
                    if (BusinessRulesUtilityFunctions.isEmpty(this.state.businessRuleProperties[propertyType])) {
                        throw new FormSubmissionError(fieldErrorStates, 'Please provide valid Output Mappings');
                        // TODO more precise should be introduced
                    }
                }
            }
        }
        return true;
    }

    getSubmitPromise(businessRuleObject, deployStatus, isUpdate) {
        if (isUpdate) {
            return new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
                .updateBusinessRule(businessRuleObject.uuid, JSON.stringify(businessRuleObject), deployStatus);
        }
        return new BusinessRulesAPI(BusinessRulesConstants.BASE_URL)
            .createBusinessRule(JSON.stringify(businessRuleObject), deployStatus.toString());
    }

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
            console.log('caught error')
            console.log(error)
            this.setState({
                showSubmitButtons: true,
                isFieldErrorStatesDirty: true,
                fieldErrorStates: error.fieldErrorStates,
            });
            this.toggleSnackbar(error.message);
        }
    }

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

    displayContent() {
        if (this.state.hasLoaded) {
            if (this.state.errorCode === BusinessRulesConstants.ERROR_CODES.NONE) {
                return (
                    <div>
                        {this.showSnackbar()}
                        <Grid container spacing={24} style={styles.formRoot} justify="center">
                            <Grid item xs={12} sm={7}>
                                <Paper style={styles.formPaper}>
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
                                            // TODO IMPLEMENT LOGIC FOR ERROR
                                            required
                                            fullWidth
                                            margin="normal"
                                        />
                                    </center>
                                    <br />
                                    <br />
                                    <InputComponent
                                        mode={this.state.formMode}
                                        inputRuleTemplates={this.state.inputRuleTemplates}
                                        getFields={streamDefinition => this.getFieldNamesAndTypes(streamDefinition)}
                                        getFieldNames={streamDefinition => this.getFieldNames(streamDefinition)}
                                        selectedInputRuleTemplate={this.state.selectedInputRuleTemplate}
                                        handleInputRuleTemplateSelected={e =>
                                            this.handleInputRuleTemplateSelected(e)}
                                        getPropertyComponents={(propertiesType, formMode) =>
                                            this.getPropertyComponents(propertiesType, formMode)}
                                        style={styles}
                                        isExpanded={this.state.isInputComponentExpanded}
                                        toggleExpansion={() => this.toggleInputComponentExpansion()}
                                    />
                                    <br />
                                    <FilterComponent
                                        filterRules={this.state.businessRuleProperties.ruleComponents.filterRules}
                                        onFilterRuleChange={
                                            (index, filterRule) => this.updateFilterRule(index, filterRule)}
                                        onRuleLogicChange={value => this.updateRuleLogic(value)}






                                        mode={this.state.formMode}
                                        selectedInputRuleTemplate={this.state.selectedInputRuleTemplate}
                                        getFields={streamDefinition => this.getFieldNamesAndTypes(streamDefinition)}
                                        getFieldNames={streamDefinition => this.getFieldNames(streamDefinition)}
                                        businessRuleProperties={this.state.businessRuleProperties}
                                        handleAttributeChange={(filterRuleIndex, value) =>
                                            this.updateFilterRuleAttribute(filterRuleIndex, value)}
                                        handleOperatorChange={(filterRuleIndex, value) =>
                                            this.updateFilterRuleOperator(filterRuleIndex, value)}
                                        handleAttributeOrValueChange={(filterRuleIndex, value) =>
                                            this.updateFilterRuleAttributeOrValue(filterRuleIndex, value)}
                                        handleRemoveFilterRule={index => this.removeFilterRule(index)}
                                        handleRuleLogicChange={value => this.updateRuleLogic(value)}
                                        addFilterRule={() => this.addFilterRule()}
                                        onFilterRuleAddition={() => this.updateDefaultRuleLogic()}
                                        ruleLogicWarn={this.getRuleLogicWarnStatus()}
                                        isExpanded={this.state.isFilterComponentExpanded}
                                        toggleExpansion={() => this.toggleFilterComponentExpansion()}
                                        style={styles}
                                    />
                                    {/*<FilterComponent*/}
                                        {/*mode={this.state.formMode}*/}
                                        {/*selectedInputRuleTemplate={this.state.selectedInputRuleTemplate}*/}
                                        {/*getFields={streamDefinition => this.getFieldNamesAndTypes(streamDefinition)}*/}
                                        {/*getFieldNames={streamDefinition => this.getFieldNames(streamDefinition)}*/}
                                        {/*businessRuleProperties={this.state.businessRuleProperties}*/}
                                        {/*handleAttributeChange={(filterRuleIndex, value) =>*/}
                                            {/*this.updateFilterRuleAttribute(filterRuleIndex, value)}*/}
                                        {/*handleOperatorChange={(filterRuleIndex, value) =>*/}
                                            {/*this.updateFilterRuleOperator(filterRuleIndex, value)}*/}
                                        {/*handleAttributeOrValueChange={(filterRuleIndex, value) =>*/}
                                            {/*this.updateFilterRuleAttributeOrValue(filterRuleIndex, value)}*/}
                                        {/*handleRemoveFilterRule={index => this.removeFilterRule(index)}*/}
                                        {/*handleRuleLogicChange={value => this.updateRuleLogic(value)}*/}
                                        {/*addFilterRule={() => this.addFilterRule()}*/}
                                        {/*onFilterRuleAddition={() => this.updateDefaultRuleLogic()}*/}
                                        {/*ruleLogicWarn={this.getRuleLogicWarnStatus()}*/}
                                        {/*isExpanded={this.state.isFilterComponentExpanded}*/}
                                        {/*toggleExpansion={() => this.toggleFilterComponentExpansion()}*/}
                                        {/*style={styles}*/}
                                    {/*/>*/}
                                    <br />
                                    <OutputComponent
                                        mode={this.state.formMode}
                                        outputRuleTemplates={this.state.outputRuleTemplates}
                                        getFields={streamDefinition => this.getFieldNamesAndTypes(streamDefinition)}
                                        getFieldNames={streamDefinition => this.getFieldNames(streamDefinition)}
                                        selectedOutputRuleTemplate={this.state.selectedOutputRuleTemplate}
                                        selectedInputRuleTemplate={this.state.selectedInputRuleTemplate}
                                        handleOutputRuleTemplateSelected={e =>
                                            this.handleOutputRuleTemplateSelected(e)}
                                        handleOutputMappingChange={(value, fieldName) =>
                                            this.updateOutputMapping(value, fieldName)}
                                        getPropertyComponents={(propertiesType, formMode) =>
                                            this.getPropertyComponents(propertiesType, formMode)}
                                        businessRuleProperties={this.state.businessRuleProperties}
                                        isExpanded={this.state.isOutputComponentExpanded}
                                        toggleExpansion={() => this.toggleOutputComponentExpansion()}
                                        style={styles}
                                    />
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
