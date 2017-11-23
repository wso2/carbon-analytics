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
import Dialog, {DialogActions, DialogContent, DialogContentText, DialogTitle,} from 'material-ui/Dialog';
import {Typography} from "material-ui";
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';
import Snackbar from 'material-ui/Snackbar';
import Slide from 'material-ui/transitions/Slide';
// App Components
import Property from './Property';
import InputComponent from "./InputComponent";
import OutputComponent from "./OutputComponent";
import FilterComponent from "./FilterComponent";
// App Utilities
import BusinessRulesUtilityFunctions from "../utils/BusinessRulesUtilityFunctions";
// App Constants
import BusinessRulesConstants from "../constants/BusinessRulesConstants";
import BusinessRulesMessages from "../constants/BusinessRulesMessages";
// App APIs
import BusinessRulesAPICaller from "../api/BusinessRulesAPICaller";
// CSS
import '../index.css';

/**
 * Represents a form, shown to for Business Rules from scratch
 */

// Button Style
const styles = {
    addFilterRuleButton: {
        backgroundColor: '#EF6C00',
        color: 'white'
    },
    button: {
        backgroundColor: '#EF6C00',
        color: 'white',
        marginRight: 10
    },
    secondaryButton: {
        marginRight: 10
    },
    paper: {
        padding: 40,
        paddingTop: 15,
        paddingBottom: 15,
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
        paddingTop: 20
    },
    propertyComponentPadding: {
        paddingLeft: 40,
        paddingRight: 40
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
        direction: 'up'
    }
}

/**
 * App context.
 */
const appContext = window.contextPath;

class BusinessRuleFromScratchForm extends React.Component {

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
            businessRuleProperties: {},

            // Expanded states of components
            isInputComponentExpanded: false,
            isFilterComponentExpanded: false,
            isOutputComponentExpanded: false,

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
        }

        // Assign default values of properties as entered values in create mode
        //if (this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE) {
        this.state.businessRuleProperties = {
            inputData: {},
            ruleComponents: {
                filterRules: [],
                ruleLogic: ['']
            },
            outputData: {},
            outputMappings: {}
        }
        //}
    }

    componentDidMount() {
        let that = this;
        if (this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE) {
            // 'Create' mode
            let templateGroupUUID = that.props.match.params.templateGroupUUID;
            let templateGroupPromise = BusinessRulesUtilityFunctions.getTemplateGroup(templateGroupUUID);
            templateGroupPromise.then(function (templateGroupResponse) {
                let templateGroup = templateGroupResponse.data[2];
                let ruleTemplatesPromise = BusinessRulesUtilityFunctions.getRuleTemplates(templateGroupUUID);
                ruleTemplatesPromise.then(function (ruleTemplatesResponse) {
                    // Filter rule templates
                    let inputRuleTemplates = [];
                    let outputRuleTemplates = [];
                    for (let ruleTemplate of ruleTemplatesResponse.data[2]) {
                        if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_INPUT) {
                            inputRuleTemplates.push(ruleTemplate);
                        } else if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_OUTPUT) {
                            outputRuleTemplates.push(ruleTemplate)
                        }
                    }
                    that.setState({
                        selectedTemplateGroup: templateGroup,
                        inputRuleTemplates: inputRuleTemplates,
                        outputRuleTemplates: outputRuleTemplates
                    })
                })
            })
        } else {
            // 'Edit' or 'View' mode
            let businessRuleUUID = that.props.match.params.businessRuleUUID;
            let businessRulePromise = BusinessRulesUtilityFunctions.getBusinessRule(businessRuleUUID);
            businessRulePromise.then(function (businessRuleResponse) {
                let businessRule = businessRuleResponse.data[2];
                let templateGroupPromise = BusinessRulesUtilityFunctions.getTemplateGroup(businessRule.templateGroupUUID)
                templateGroupPromise.then(function (templateGroupResponse) {
                    let templateGroup = templateGroupResponse.data[2];
                    let ruleTemplatesPromise = BusinessRulesUtilityFunctions.getRuleTemplates(templateGroup.uuid);
                    ruleTemplatesPromise.then(function (ruleTemplatesResponse) {
                        // Filter rule templates
                        let inputRuleTemplates = [];
                        let outputRuleTemplates = [];
                        for (let ruleTemplate of ruleTemplatesResponse.data[2]) {
                            if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_OUTPUT) {
                                outputRuleTemplates.push(ruleTemplate)
                            } else if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_INPUT) {
                                inputRuleTemplates.push(ruleTemplate)
                            }
                            let selectedInputRuleTemplatePromise = BusinessRulesUtilityFunctions.getRuleTemplate(
                                businessRule.templateGroupUUID, businessRule.inputRuleTemplateUUID);
                            selectedInputRuleTemplatePromise.then(function (selectedInputRuleTemplateResponse) {
                                let selectedInputRuleTemplate = selectedInputRuleTemplateResponse.data[2];
                                let selectedOutputRuleTemplatePromise = BusinessRulesUtilityFunctions.getRuleTemplate(
                                    businessRule.templateGroupUUID, businessRule.outputRuleTemplateUUID);
                                selectedOutputRuleTemplatePromise.then(function (selectedOutputRuleTemplateResponse) {
                                    let selectedOutputRuleTemplate = selectedOutputRuleTemplateResponse.data[2];
                                    that.setState({
                                        businessRuleType: BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH,
                                        businessRuleName: businessRule.name,
                                        businessRuleUUID: businessRule.uuid,
                                        selectedTemplateGroup: templateGroup,
                                        inputRuleTemplates: inputRuleTemplates,
                                        outputRuleTemplates: outputRuleTemplates,
                                        selectedInputRuleTemplate: selectedInputRuleTemplate,
                                        selectedOutputRuleTemplate: selectedOutputRuleTemplate,
                                        businessRuleProperties: businessRule.properties
                                    })
                                })
                            })
                        }
                    })
                })
            })
        }
    }

    /**
     * Handles onChange of Business Rule name text field
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
     * Updates value change of any property, of the given type, to the state
     * @param property
     * @param propertyType
     * @param value
     */
    handleValueChange(property, propertyType, value) {
        let state = this.state
        state['businessRuleProperties'][propertyType][property] = value
        this.setState(state)
    }

    /**
     * Returns properties as Property components with the data specified in the state,
     * that belong to the given property type
     *
     * @param propertyType
     * @returns {Array}
     */
    getPropertyComponents(propertyType, formMode) {
        let unArrangedPropertiesFromTemplate // To store values that are going to be used
        let reArrangedProperties = []
        let propertiesToDisplay // To store mapped properties as input fields

        // Get properties from the rule templates
        if (propertyType === BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_INPUT) {
            if (!BusinessRulesUtilityFunctions.isEmpty(this.state.selectedInputRuleTemplate)) {
                unArrangedPropertiesFromTemplate = this.state.selectedInputRuleTemplate.properties
            }
        } else if (propertyType === BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_OUTPUT) {
            if (!BusinessRulesUtilityFunctions.isEmpty(this.state.selectedOutputRuleTemplate)) {
                unArrangedPropertiesFromTemplate = this.state.selectedOutputRuleTemplate.properties
            }
        }

        // Push propertyKey and propertyObject as an array member, in order to use the array.map() function
        for (let propertyKey in unArrangedPropertiesFromTemplate) {
            if (unArrangedPropertiesFromTemplate.hasOwnProperty(propertyKey)) {
                reArrangedProperties.push({
                    propertyName: propertyKey,
                    propertyObject: unArrangedPropertiesFromTemplate[propertyKey.toString()]
                })
            }
        }

        // Map re-arranged properties for rendering
        propertiesToDisplay = reArrangedProperties.map((property) =>
            <Property
                key={property.propertyName}
                name={property.propertyName}
                fieldName={property.propertyObject.fieldName}
                description={property.propertyObject.description}
                value={(this.state['businessRuleProperties'][propertyType][property.propertyName]) ?
                    (this.state['businessRuleProperties'][propertyType][property.propertyName]) : ('')}
                errorState={
                    (this.state.isSubmitPressed) &&
                    (this.state['businessRuleProperties'][propertyType][property.propertyName] === '')
                }
                disabledState={formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                options={property.propertyObject.options}
                onValueChange={(e) => this.handleValueChange(property.propertyName, propertyType, e)}
                fullWidth
            />
        );

        return propertiesToDisplay
    }

    /**
     * Gives field names of the given stream definition, as an array
     * @param exposedStreamDefinition
     */
    getFieldNames(streamDefinition) {
        let fieldNames = []
        for (let field in this.getFields(streamDefinition)) {
            fieldNames.push(field.toString())
        }

        return fieldNames
    }

    /**
     * Gives field names as keys and types as values, of the given stream definition, as an object
     * @param streamDefinition
     * @returns {{x: string}}
     */
    getFields(streamDefinition) {
        let regExp = /\(([^)]+)\)/;
        let matches = regExp.exec(streamDefinition);
        let fields = {}

        // Keep the field name and type, as each element in an array
        for (let field of matches[1].split(",")) {
            // Key: name, Value: type
            let fieldName = field.trim().split(" ")[0]
            let fieldType = field.trim().split(" ")[1]
            fields[fieldName.toString()] = fieldType
        }

        return fields
    }

    /**
     * Toggles expansion of the input component
     */
    toggleInputComponentExpansion() {
        this.setState({isInputComponentExpanded: !this.state.isInputComponentExpanded})
    }

    /**
     * Handles onChange of Input rule template selectio
     * @param event
     */
    handleInputRuleTemplateSelected(event) {
        let state = this.state
        var that = this
        let selectedInputRuleTemplatePromise =
            BusinessRulesUtilityFunctions.getRuleTemplate(this.state.selectedTemplateGroup.uuid, event.target.value)
        let selectedInputRuleTemplate = selectedInputRuleTemplatePromise.then(function (response) {
            state['selectedInputRuleTemplate'] = response.data[2]
            // Set default values as inputData values in state
            for (let propertyKey in state['selectedInputRuleTemplate']['properties']) {
                state['businessRuleProperties'][BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_INPUT]
                    [propertyKey.toString()] =
                    state['selectedInputRuleTemplate']['properties'][propertyKey.toString()]['defaultValue']
            }
            that.setState(state)
        })
    }

    /**
     * Toggles expansion of the filter component
     */
    toggleFilterComponentExpansion() {
        this.setState({isFilterComponentExpanded: !this.state.isFilterComponentExpanded})
    }

    /**
     * Handles onChange of any Attribute, of a filter rule
     * @param filterRuleIndex
     * @param value
     */
    handleAttributeChange(filterRuleIndex, value) {
        var ruleComponentType = BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_RULE_COMPONENTS
        var ruleComponentFilterRuleType = BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_RULE_COMPONENT_PROPERTY_TYPE_FILTER_RULES

        let state = this.state
        state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex] =
            value + " " +
            state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex].split(" ")[1]
            + " " +
            state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex].split(" ")[2]
        this.setState(state)
    }

    /**
     * Handles onChange of any Operator, of a filter rule
     *
     * @param filterRuleIndex
     * @param value
     */
    handleOperatorChange(filterRuleIndex, value) {
        var ruleComponentType = BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_RULE_COMPONENTS
        var ruleComponentFilterRuleType =
            BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_RULE_COMPONENT_PROPERTY_TYPE_FILTER_RULES

        let state = this.state
        state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex] =
            state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex].split(" ")[0]
            + " " + value + " " +
            state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex].split(" ")[2]
        this.setState(state)
    }

    /**
     * Handles onChange of any AttributeOrValue, of a filter
     *
     * @param filterRuleIndex
     * @param value
     */
    handleAttributeOrValueChange(filterRuleIndex, value) {
        var ruleComponentType = BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_RULE_COMPONENTS
        var ruleComponentFilterRuleType = BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_RULE_COMPONENT_PROPERTY_TYPE_FILTER_RULES

        let state = this.state
        state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex] =
            state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex].split(" ")[0]
            + " " +
            state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex].split(" ")[1]
            + " " + value
        this.setState(state)
    }

    /**
     * Handles onChange of the RuleLogic
     * @param value
     */
    handleRuleLogicChange(value) {
        //this.generateRuleLogic(value)
        let state = this.state
        state['businessRuleProperties']['ruleComponents']['ruleLogic'][0] = value
        this.setState(state)
    }

    /**
     * Updates the rule logic by adding the latest rule logic number with an 'AND' in between
     */
    generateRuleLogic() {
        let state = this.state
        let existingRuleLogic = state['businessRuleProperties']['ruleComponents']['ruleLogic'][0]
        // If a rule logic is not present
        if (existingRuleLogic === '') {
            // No rule logic is present
            // Concatenate each filter rule numbers with AND and return
            let numbers = [];
            for (let i = 0; i < this.state['businessRuleProperties']['ruleComponents']['filterRules'].length; i++) {
                numbers.push(i + 1)
            }
            state['businessRuleProperties']['ruleComponents']['ruleLogic'][0] = numbers.join(' AND ');
        } else {
            state['businessRuleProperties']['ruleComponents']['ruleLogic'][0] =
                existingRuleLogic + ' AND ' +
                this.state['businessRuleProperties']['ruleComponents']['filterRules'].length
        }

        if (this.state['businessRuleProperties']['ruleComponents']['filterRules'].length === 0) {
            state['businessRuleProperties']['ruleComponents']['ruleLogic'][0] = '';
        }
        this.setState(state)
    }

    /**
     * Adds a new filter rule
     */
    addFilterRule() {
        let state = this.state
        state.businessRuleProperties['ruleComponents']['filterRules'].push("  ")
        this.setState(state)
        this.generateRuleLogic()
    }

    /**
     * Removes the filter rule given by index
     * @param index
     */
    removeFilterRule(index) {
        let state = this.state
        state.businessRuleProperties['ruleComponents']['filterRules'].splice(index, 1)
        this.setState(state)
        this.generateRuleLogic()
    }

    /**
     * Returns whether the rule logic has a warning, if any of the entered number has exceeded the number of the
     * latest filter rule, or not
     * @returns {boolean}
     */
    warnOnRuleLogic() {
        // If rule logic exists
        if (this.state['businessRuleProperties']['ruleComponents']['ruleLogic'][0] &&
            this.state['businessRuleProperties']['ruleComponents']['ruleLogic'][0] != null &&
            this.state['businessRuleProperties']['ruleComponents']['ruleLogic'][0] !== "") {
            let ruleLogic = this.state['businessRuleProperties']['ruleComponents']['ruleLogic'][0]

            // Get all the numbers, mentioned in the rule logic
            var numberPattern = /\d+/g;
            for (let number of ruleLogic.match(numberPattern)) {
                // If a number exceeds the latest filter rule's number, a corresponding filter rule can not be found
                if (number > this.state['businessRuleProperties']['ruleComponents']['filterRules'].length) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Toggles expansion of the output component
     */
    toggleOutputComponentExpansion() {
        this.setState({isOutputComponentExpanded: !this.state.isOutputComponentExpanded})
    }

    /**
     * Handles onChange of Output rule template selection
     * @param event
     */
    handleOutputRuleTemplateSelected(event) {
        let state = this.state
        let that = this
        let selectedOutputRuleTemplatePromise =
            BusinessRulesUtilityFunctions.getRuleTemplate(this.state.selectedTemplateGroup.uuid, event.target.value)
        let selectedOutputRuleTemplate = selectedOutputRuleTemplatePromise.then(function (response) {
            state['selectedOutputRuleTemplate'] = response.data[2]
            // Set default values as outputData values in state
            for (let propertyKey in state['selectedOutputRuleTemplate']['properties']) {
                state['businessRuleProperties'][BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_OUTPUT]
                    [propertyKey.toString()] =
                    state['selectedOutputRuleTemplate']['properties'][propertyKey.toString()]['defaultValue']
            }
            that.setState(state)
        })
    }

    /**
     * Handles onChange of any value for an input field for output mapping
     * @param outputFieldName
     */
    handleOutputMappingChange(event, outputFieldName) {
        let state = this.state
        state['businessRuleProperties']['outputMappings'][outputFieldName] = event.target.value
        this.setState(state)
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
        let that = this;
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
                    type: BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH,
                    templateGroupUUID: this.state.selectedTemplateGroup.uuid,
                    inputRuleTemplateUUID: this.state.selectedInputRuleTemplate.uuid,
                    outputRuleTemplateUUID: this.state.selectedOutputRuleTemplate.uuid,
                    properties: this.state.businessRuleProperties
                };
                let apis = new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL)
                apis.createBusinessRule(JSON.stringify(businessRuleObject), deployStatus).then(
                    function (response) {
                        that.setSnackbar(response.data[1]);
                        setTimeout(function () {
                            window.location.href = appContext + '/businessRulesManager';
                        }, 3000);
                    }).catch(function (error) {
                    that.setSnackbar('Failed to create the Business Rule');
                    setTimeout(function () {
                        window.location.href = appContext + '/businessRulesManager';
                    }, 3000);
                });
            } else {
                // Display error
                this.setState({
                    isFormFillable: true
                });
                this.setSnackbar(BusinessRulesMessages.ALL_FIELDS_REQUIRED_ERROR_CONTENT)
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
        let that = this;
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
                    type: BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH,
                    templateGroupUUID: this.state.selectedTemplateGroup.uuid,
                    inputRuleTemplateUUID: this.state.selectedInputRuleTemplate.uuid,
                    outputRuleTemplateUUID: this.state.selectedOutputRuleTemplate.uuid,
                    properties: this.state.businessRuleProperties
                };
                let apis = new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL)
                apis.updateBusinessRule(businessRuleObject['uuid'], JSON.stringify(businessRuleObject), deployStatus).then(
                    function (response) {
                        that.setSnackbar(response.data[1]);
                        setTimeout(function () {
                            window.location.href = appContext + '/businessRulesManager';
                        }, 3000);
                    }).catch(function (error) {
                    that.setSnackbar('Failed to create the Business Rule');
                    setTimeout(function () {
                        window.location.href = appContext + '/businessRulesManager';
                    }, 3000);
                });
            } else {
                // Display error
                this.setState({
                    isFormFillable: true
                });
                this.setSnackbar(BusinessRulesMessages.ALL_FIELDS_REQUIRED_ERROR_CONTENT)
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
            this.state.selectedInputRuleTemplate.uuid === '' ||
            BusinessRulesUtilityFunctions.isEmpty(this.state.selectedInputRuleTemplate) ||
            this.state.selectedOutputRuleTemplate.uuid === '' ||
            BusinessRulesUtilityFunctions.isEmpty(this.state.selectedOutputRuleTemplate)) {
            return false
        }
        // Validate property type components
        for (let propertyKey in this.state.businessRuleProperties) {
            // 'ruleComponent' property type components can be empty.
            // Validation happens only for 'input' & 'output' types
            if (propertyKey !== BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_RULE_COMPONENTS) {
                // If any 'input' or 'output' property type component is completely empty
                if (BusinessRulesUtilityFunctions.isEmpty(this.state.businessRuleProperties[propertyKey.toString()])) {
                    return false
                } else {
                    // If any of the component member is
                    // - undefined (to prevent the error)
                    // - empty
                    // - or not entered
                    for (let propertyComponentKey in this.state.businessRuleProperties[propertyKey.toString()]) {
                        if ((!this.state.businessRuleProperties[propertyKey.toString()]
                                [propertyComponentKey.toString()]) ||
                            (BusinessRulesUtilityFunctions.isEmpty(
                                this.state.businessRuleProperties[propertyKey.toString()]
                                    [propertyComponentKey.toString()])) ||
                            (this.state.businessRuleProperties[propertyKey.toString()]
                                [propertyComponentKey.toString()] === '')) {
                            return false
                        }
                    }
                }
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
     * Sets snackbar with the given message
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
     * Closes the snackbar
     */
    handleRequestClose() {
        this.setState({displaySnackbar: false});
    };

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

    /**
     * Shows the snack bar
     */
    showSnackbar() {
        return (
            <Snackbar
                autoHideDuration={3500}
                open={this.state.displaySnackbar}
                onRequestClose={(e) => this.handleRequestClose()}
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

    render() {
        // Business Rule Name
        var businessRuleNameToDisplay =
            <TextField
                id="businessRuleName"
                name="businessRuleName"
                label={BusinessRulesMessages.BUSINESS_RULE_NAME_FIELD_NAME}
                placeholder={BusinessRulesMessages.BUSINESS_RULE_NAME_FIELD_DESCRIPTION}
                value={this.state.businessRuleName}
                onChange={(e) => this.handleBusinessRuleNameChange(e)}
                disabled={(this.state.formMode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE)}
                error={(this.state.isSubmitPressed) && (this.state.businessRuleName === '')}
                required
                fullWidth
                margin="normal"
            />

        let submitButtons;
        if (this.state.isFormFillable) {
            if (this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE) {
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
            } else if (this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT) {
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
        }

        return (
            <div>
                {this.showDialog()}
                {this.showSnackbar()}
                <Grid container spacing={24} style={styles.formRoot} justify='center'>
                    <Grid item xs={12} sm={7}>
                        <Paper style={styles.formPaper}>
                            <center>
                                <Typography type="headline">{this.state.selectedTemplateGroup.name}</Typography>
                                <Typography type="subheading">
                                    {this.state.selectedTemplateGroup.description}
                                </Typography>
                                <br/>
                                {businessRuleNameToDisplay}
                            </center>
                            <br/>
                            <br/>
                            <InputComponent
                                mode={this.state.formMode}
                                inputRuleTemplates={this.state.inputRuleTemplates}
                                getFields={(streamDefinition) => this.getFields(streamDefinition)}
                                selectedInputRuleTemplate={this.state.selectedInputRuleTemplate}
                                handleInputRuleTemplateSelected={(e) => this.handleInputRuleTemplateSelected(e)}
                                getPropertyComponents={(propertiesType, formMode) =>
                                    this.getPropertyComponents(propertiesType, formMode)}
                                style={styles}
                                isExpanded={this.state.isInputComponentExpanded}
                                toggleExpansion={(e) => this.toggleInputComponentExpansion()}
                            />
                            <br/>
                            <FilterComponent
                                mode={this.state.formMode}
                                selectedInputRuleTemplate={this.state.selectedInputRuleTemplate}
                                getFields={(streamDefinition) => this.getFields(streamDefinition)}
                                businessRuleProperties={this.state.businessRuleProperties}
                                handleAttributeChange={(filterRuleIndex, value) =>
                                    this.handleAttributeChange(filterRuleIndex, value)}
                                handleOperatorChange={(filterRuleIndex, value) =>
                                    this.handleOperatorChange(filterRuleIndex, value)}
                                handleAttributeOrValueChange={(filterRuleIndex, value) =>
                                    this.handleAttributeOrValueChange(filterRuleIndex, value)}
                                handleRemoveFilterRule={(index) => this.removeFilterRule(index)}
                                handleRuleLogicChange={(value) => this.handleRuleLogicChange(value)}
                                addFilterRule={(e) => this.addFilterRule()}
                                onFilterRuleAddition={(e) => this.generateRuleLogic()}
                                ruleLogicWarn={this.warnOnRuleLogic()}
                                isExpanded={this.state.isFilterComponentExpanded}
                                toggleExpansion={(e) => this.toggleFilterComponentExpansion()}
                                style={styles}
                            />
                            <br/>
                            <OutputComponent
                                mode={this.state.formMode}
                                outputRuleTemplates={this.state.outputRuleTemplates}
                                getFields={(streamDefinition) => this.getFields(streamDefinition)}
                                getFieldNames={(streamDefinition) => this.getFieldNames(streamDefinition)}
                                selectedOutputRuleTemplate={this.state.selectedOutputRuleTemplate}
                                selectedInputRuleTemplate={this.state.selectedInputRuleTemplate}
                                handleOutputRuleTemplateSelected={(e) => this.handleOutputRuleTemplateSelected(e)}
                                handleOutputMappingChange={(e, fieldName) =>
                                    this.handleOutputMappingChange(e, fieldName)}
                                getPropertyComponents={(propertiesType, formMode) =>
                                    this.getPropertyComponents(propertiesType, formMode)}
                                businessRuleProperties={this.state['businessRuleProperties']}
                                isExpanded={this.state.isOutputComponentExpanded}
                                toggleExpansion={(e) => this.toggleOutputComponentExpansion()}
                                style={styles}
                            />
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

export default BusinessRuleFromScratchForm;