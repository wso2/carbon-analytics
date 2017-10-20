import React from 'react';
import ReactDOM from 'react-dom';
// import './index.css';
// Material-UI
import Property from './Property';
import Button from 'material-ui/Button';
import TextField from 'material-ui/TextField';
// import Autosuggest from 'react-autosuggest';
// import match from 'autosuggest-highlight/match';
// import parse from 'autosuggest-highlight/parse';
import Header from "./Header";
import Dialog, {DialogActions, DialogContent, DialogContentText, DialogTitle,} from 'material-ui/Dialog';
import BusinessRulesFunctions from "../utils/BusinessRulesFunctions";
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
import {Typography} from "material-ui";
import BusinessRulesAPIs from "../utils/BusinessRulesAPIs";
import InputComponent from "./InputComponent";
import OutputComponent from "./OutputComponent";
import FilterComponent from "./FilterComponent";
import BusinessRulesMessageStringConstants from "../utils/BusinessRulesMessageStringConstants";
import ShowProgressComponent from "./ShowProgressComponent";
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';

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
        maxWidth: 1000,
    }
}

class BusinessRuleFromScratchForm extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            formMode: props.formMode,
            templateGroups: props.templateGroups,
            selectedTemplateGroup: props.selectedTemplateGroup,
            inputRuleTemplates: props.inputRuleTemplates,
            outputRuleTemplates: props.outputRuleTemplates,

            // Present only in 'edit' mode
            businessRuleName: props.businessRuleName,
            businessRuleUUID: props.businessRuleUUID,
            // Rule Templates, whose properties will be used to generate form
            selectedInputRuleTemplate: props.selectedInputRuleTemplate,
            selectedOutputRuleTemplate: props.selectedOutputRuleTemplate,

            // To store values given for properties displayed in the form
            businessRuleProperties: props.businessRuleProperties,

            // Expanded states of components
            isInputComponentExpanded: false,
            isFilterComponentExpanded: false,
            isOutputComponentExpanded: false,

            // For displaying messages / errors
            displayDialog: false,
            dialogTitle: '',
            dialogContentText: '',
            dialogPrimaryButtonText: ''
        }

        // Assign default values of properties as entered values in create mode
        if (this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE) {
            // Create mode
            let state = this.state
            // Set an empty business rule values in the state
            state['selectedInputRuleTemplate'] = {}
            state['selectedOutputRuleTemplate'] = {}
            state['businessRuleName'] = ''
            state['businessRuleUUID'] = ''
            state['businessRuleProperties'] = {
                'inputData': {},
                'ruleComponents': {
                    'filterRules': [],
                    'ruleLogic': ['']
                },
                'outputData': {},
                'outputMappings': {}
            }
            this.state = state
        } else {
            let state = this.state
            // Set received business rule values in the state
            state['selectedInputRuleTemplate'] = this.props.selectedInputRuleTemplate
            state['selectedOutputRuleTemplate'] = this.props.selectedOutputRuleTemplate
            state['businessRuleProperties'] = this.props.businessRuleProperties
            this.state = state
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
        state['businessRuleUUID'] = BusinessRulesFunctions.generateBusinessRuleUUID(event.target.value)
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
     * Re-arranges the structure of property objects of the given type, and returns them as input fields
     * @param propertiesType
     * @returns {Array}
     */
    reArrangePropertiesForDisplay(propertiesType, formMode) {
        let unArrangedPropertiesFromTemplate // To store values that are going to be used
        let reArrangedProperties = [] // To store after arranging properties for displaying
        let propertiesToDisplay // To store mapped properties as input fields

        // Get properties from the rule templates, specific for given property type
        if (propertiesType === BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_INPUT) {
            if (!BusinessRulesFunctions.isEmpty(this.state.selectedInputRuleTemplate.uuid)) {
                unArrangedPropertiesFromTemplate = this.state.selectedInputRuleTemplate.properties
            }
        } else if (propertiesType === BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_OUTPUT) {
            if (!BusinessRulesFunctions.isEmpty(this.state.selectedOutputRuleTemplate.uuid)) {
                unArrangedPropertiesFromTemplate = this.state.selectedOutputRuleTemplate.properties
            }
        }

        // Push each property in the rule template, as an object in a re-arranged format,
        // which has the original object's Key & Value
        // denoted by new Keys : 'propertyName' & 'propertyObject'
        for (let propertyKey in unArrangedPropertiesFromTemplate) {
            let property = unArrangedPropertiesFromTemplate[propertyKey.toString()]
            reArrangedProperties.push({
                propertyName: propertyKey,
                propertyObject: property
            })
        }

        // Map re-arranged properties for rendering
        propertiesToDisplay = reArrangedProperties.map((property) =>
            <Property
                key={property.propertyName}
                name={property.propertyName}
                fieldName={property.propertyObject.fieldName}
                description={property.propertyObject.description}
                value={(this.state['businessRuleProperties'][propertiesType][property.propertyName]) ?
                    (this.state['businessRuleProperties'][propertiesType][property.propertyName]) : ('')}
                errorState={this.state['businessRuleProperties'][propertiesType][property.propertyName] === ''}
                disabledState={formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                options={property.propertyObject.options}
                onValueChange={(e) => this.handleValueChange(property.propertyName, propertiesType, e)}
            />
        )

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
        console.log("THIS STATE INPUT")
        console.log(this.state)
        let state = this.state
        var that = this
        let selectedInputRuleTemplatePromise =
            BusinessRulesFunctions.getRuleTemplate(this.state.selectedTemplateGroup.uuid, event.target.value)
        let selectedInputRuleTemplate = selectedInputRuleTemplatePromise.then(function (response) {
            state['selectedInputRuleTemplate'] = response.data
            // Set default values as inputData values in state
            for (let propertyKey in state['selectedInputRuleTemplate']['properties']) {
                state['businessRuleProperties'][BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_INPUT][propertyKey.toString()] =
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
            state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex].split(" ")[1] + " " +
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
        var ruleComponentFilterRuleType = BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_RULE_COMPONENT_PROPERTY_TYPE_FILTER_RULES

        let state = this.state
        state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex] =
            state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex].split(" ")[0] + " " +
            value + " " +
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
            state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex].split(" ")[0] + " " +
            state.businessRuleProperties[ruleComponentType][ruleComponentFilterRuleType][filterRuleIndex].split(" ")[1] + " " +
            value
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
        if (existingRuleLogic == "") {
            // No rule logic is present
            // Concatenate each filter rule numbers with AND and return
            let numbers = []
            for (let i = 0; i < this.state['businessRuleProperties']['ruleComponents']['filterRules'].length; i++) {
                numbers.push(i + 1)
            }

            state['businessRuleProperties']['ruleComponents']['ruleLogic'][0] = numbers.join(" AND ")
        } else {
            state['businessRuleProperties']['ruleComponents']['ruleLogic'][0] =
                existingRuleLogic + ' AND ' + this.state['businessRuleProperties']['ruleComponents']['filterRules'].length
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

        // Check the rule logic, and update auto generated logic, if it's empty
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
            BusinessRulesFunctions.getRuleTemplate(this.state.selectedTemplateGroup.uuid, event.target.value)
        let selectedOutputRuleTemplate = selectedOutputRuleTemplatePromise.then(function (response) {
            state['selectedOutputRuleTemplate'] = response.data
            // Set default values as outputData values in state
            for (let propertyKey in state['selectedOutputRuleTemplate']['properties']) {
                state['businessRuleProperties'][BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_OUTPUT][propertyKey.toString()] =
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
        console.log("HANDLE OUTPUT MAP")
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
        // Validate whether all required fields are filled or not
        if(this.isBusinessRuleValid()){
            // Prepare the business rule object
            let state = this.state
            var businessRuleObject = {}
            businessRuleObject['uuid'] = state.businessRuleUUID
            businessRuleObject['name'] = state.businessRuleName
            businessRuleObject['templateGroupUUID'] = state.selectedTemplateGroup.uuid
            businessRuleObject['inputRuleTemplateUUID'] = state.selectedInputRuleTemplate.uuid
            businessRuleObject['outputRuleTemplateUUID'] = state.selectedOutputRuleTemplate.uuid
            businessRuleObject['type'] = BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH
            businessRuleObject['properties'] = state.businessRuleProperties

            // Send prepared business rule object to API
            let apis = new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL)
            apis.createBusinessRule(JSON.stringify(businessRuleObject), deployStatus).then(function (response) {
                BusinessRulesFunctions.loadBusinessRuleModifier(true, response.status);
            }).catch(function (error){
                ReactDOM.render(
                    <ShowProgressComponent
                        error={BusinessRulesMessageStringConstants.API_FAILURE}/>,document.getElementById('root'))
            })
            // Show 'please wait'
            ReactDOM.render(<ShowProgressComponent/>,document.getElementById('root'))
        }else{
            // Display error todo: highlight the fields when errors occur (background color)
            this.setDialog(BusinessRulesMessageStringConstants.ALL_FIELDS_REQUIRED_ERROR_TITLE,
                BusinessRulesMessageStringConstants.ALL_FIELDS_REQUIRED_ERROR_CONTENT,
                BusinessRulesMessageStringConstants.ALL_FIELDS_REQUIRED_ERROR_PRIMARY_BUTTON)
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
        if(this.isBusinessRuleValid()){
            // Prepare the business rule object
            let state = this.state
            var businessRuleObject = {}
            businessRuleObject['uuid'] = state.businessRuleUUID
            businessRuleObject['name'] = state.businessRuleName
            businessRuleObject['templateGroupUUID'] = state.selectedTemplateGroup.uuid
            businessRuleObject['inputRuleTemplateUUID'] = state.selectedInputRuleTemplate.uuid
            businessRuleObject['outputRuleTemplateUUID'] = state.selectedOutputRuleTemplate.uuid
            businessRuleObject['type'] = BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH
            businessRuleObject['properties'] = state.businessRuleProperties

            // Send prepared business rule object to API
            let apis = new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL)
            // Deployment true or false
            apis.updateBusinessRule(businessRuleObject['uuid'], JSON.stringify(businessRuleObject), deployStatus).then(function (response) {
                BusinessRulesFunctions.loadBusinessRuleModifier(true, response.status.toString());
            })
        }else{
            // Display error
            this.setDialog(BusinessRulesMessageStringConstants.ALL_FIELDS_REQUIRED_ERROR_TITLE,
                BusinessRulesMessageStringConstants.ALL_FIELDS_REQUIRED_ERROR_CONTENT,
                BusinessRulesMessageStringConstants.ALL_FIELDS_REQUIRED_ERROR_PRIMARY_BUTTON)
        }
    }

    /**
     * Checks whether the business rule object in the state is a valid one or not
     */
    isBusinessRuleValid() {
        if(this.state.businessRuleName === '' || BusinessRulesFunctions.isEmpty(this.state.businessRuleName)){
            return false
        }
        if(this.state.businessRuleUUID === '' || BusinessRulesFunctions.isEmpty(this.state.businessRuleUUID)){
            return false
        }
        if(this.state.selectedTemplateGroup.uuid === '' ||
            BusinessRulesFunctions.isEmpty(this.state.selectedTemplateGroup.uuid)){
            return false
        }
        if(this.state.selectedInputRuleTemplate.uuid === '' ||
            BusinessRulesFunctions.isEmpty(this.state.selectedInputRuleTemplate)){
            return false
        }
        if(this.state.selectedOutputRuleTemplate.uuid === '' ||
            BusinessRulesFunctions.isEmpty(this.state.selectedOutputRuleTemplate)){
            return false
        }
        // Validate property type components
        for (let propertyKey in this.state.businessRuleProperties){
            // 'ruleComponent' property type components can be empty.
            // Validation happens only for 'input' & 'output' types
            if(propertyKey !== BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_RULE_COMPONENTS){
                // If any 'input' or 'output' property type component is completely empty
                if(BusinessRulesFunctions.isEmpty(this.state.businessRuleProperties[propertyKey.toString()])){
                    return false
                }else{
                    // If any of the component member is
                    // - undefined (to prevent the error)
                    // - empty
                    // - or not entered
                    for(let propertyComponentKey in this.state.businessRuleProperties[propertyKey.toString()]){
                        if(
                            (!this.state.businessRuleProperties[propertyKey.toString()][propertyComponentKey.toString()]) ||
                            (BusinessRulesFunctions.isEmpty(
                                this.state.businessRuleProperties[propertyKey.toString()][propertyComponentKey.toString()])) ||
                            (this.state.businessRuleProperties[propertyKey.toString()][propertyComponentKey.toString()]==='')){
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
    setDialog(title, contentText, primaryButtonText){
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
    dismissDialog(){
        this.setState({displayDialog: false})
    }

    /**
     * Shows the dialog, with displaying the contents available from the state
     *
     * @returns {XML}
     */
    showDialog(){
        return (
            <Dialog open={this.state.displayDialog}
                    onRequestClose={(e)=>this.dismissDialog()}
            >
                <DialogTitle>{this.state.dialogTitle}</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        {this.state.dialogContentText}
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button style={styles.secondaryButton}
                            onClick={(e)=>this.dismissDialog()}
                            color="default">
                        {this.state.dialogPrimaryButtonText}
                    </Button>
                </DialogActions>
            </Dialog>
        )
    }

    render() {
        // Business Rule Name
        var businessRuleNameToDisplay =
            <TextField
                id="businessRuleName"
                name="businessRuleName"
                label={BusinessRulesMessageStringConstants.BUSINESS_RULE_NAME_FIELD_NAME}
                placeholder={BusinessRulesMessageStringConstants.BUSINESS_RULE_NAME_FIELD_DESCRIPTION}
                value={this.state.businessRuleName}
                required={true}
                onChange={(e) => this.handleBusinessRuleNameChange(e)}
                disabled={(this.state.formMode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE)}
            />

        // Save, and Save & Deploy buttons
        let submitButtons

        // If form should be displayed for Creating a business rule
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
                <Header
                    title="Business Rule Manager"
                />
                <br/>
                <div style={styles.formRoot}>
                    <center>
                        <Grid container spacing={24} justify='center'>
                            <Grid item xs>
                                <Paper style={styles.formPaper}>
                                    <Typography type="headline">{this.state.selectedTemplateGroup.name}</Typography>
                                    <Typography type="subheading">
                                        {this.state.selectedTemplateGroup.description}
                                    </Typography>
                                    <br/>
                                    {businessRuleNameToDisplay}
                                    <br/>
                                    <br/>
                                    <InputComponent
                                        mode={this.state.formMode}
                                        inputRuleTemplates={this.state.inputRuleTemplates}
                                        getFields={(streamDefinition) => this.getFields(streamDefinition)}
                                        selectedInputRuleTemplate={this.state.selectedInputRuleTemplate}
                                        handleInputRuleTemplateSelected={(e) => this.handleInputRuleTemplateSelected(e)}
                                        reArrangePropertiesForDisplay={(propertiesType, formMode) => this.reArrangePropertiesForDisplay(propertiesType, formMode)}
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
                                        handleAttributeChange={(filterRuleIndex, value) => this.handleAttributeChange(filterRuleIndex, value)}
                                        handleOperatorChange={(filterRuleIndex, value) => this.handleOperatorChange(filterRuleIndex, value)}
                                        handleAttributeOrValueChange={(filterRuleIndex, value) => this.handleAttributeOrValueChange(filterRuleIndex, value)}
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
                                        handleOutputMappingChange={(e, fieldName) => this.handleOutputMappingChange(e, fieldName)}
                                        reArrangePropertiesForDisplay={(propertiesType, formMode) => this.reArrangePropertiesForDisplay(propertiesType, formMode)}
                                        businessRuleProperties={this.state['businessRuleProperties']}
                                        isExpanded={this.state.isOutputComponentExpanded}
                                        toggleExpansion={(e) => this.toggleOutputComponentExpansion()}
                                        style={styles}
                                    />
                                    <br/>
                                    {submitButtons}
                                </Paper>
                            </Grid>
                        </Grid>
                    </center>
                </div>
            </div>

        )
    }
}

export default BusinessRuleFromScratchForm;