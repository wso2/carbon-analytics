// todo: bind handleClicks in the new way
// todo: refactor TemplateGroup as BusinessDomain
// todo: No cards for Rule Templates. Replace with Selects. Show description and preview form below to give an idea
import React from 'react';
import ReactDOM from 'react-dom';
// import './index.css';
// Material-UI
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import TextField from 'material-ui/TextField';
import {FormControl, FormHelperText} from 'material-ui/Form';
import Input, {InputLabel} from 'material-ui/Input';
import {MenuItem} from 'material-ui/Menu';
import Select from 'material-ui/Select';
import {SnackbarContent} from 'material-ui/Snackbar';

/**
 * Used to create Business Rule from template / from scratch
 */
class BusinessRulesCreator extends React.Component {
    render() {
        return (
            <div>
                <Typography type="headline" component="h2">
                    Let's create a business rule
                </Typography>
                <br/>
                <div>
                    <Button raised color="primary"
                            onClick={(e) => runBusinessRuleCreatorFromTemplate()}>
                        From Template
                    </Button>
                    &nbsp;&nbsp;
                    {/*todo: Remove hard code*/}
                    <Button raised color="default"
                        //onClick={(e) => runBusinessRuleModifier()}
                            onClick={(e) => console.log("Not implemented yet")} //todo: implement this
                    >
                        From the scratch
                    </Button>
                </div>
            </div>
        );
    }
}

/**
 * List TemplateGroups and Rule Templates in order to create Business Rule from Template
 */
class BusinessRulesCreatorFromTemplate extends React.Component {
    /**
     * Updates selected Templated Group in the state,
     * and loads Rule Templates that belong to the selected Template Group,
     * when Template Group is selected from the list
     */
    handleTemplateGroupSelected = name => event => {
        // Update Selected Template Group
        let state = this.state
        state[name] = getTemplateGroup(event.target.value)
        // Load Rule Templates under the selected Template Group
        state['ruleTemplates'] = getRuleTemplates(this.state.selectedTemplateGroup.name)
        this.setState(state)
    };
    /**
     * Updates selected Rule Template in the state,
     * when Rule Template is selected from the list
     */
    handleRuleTemplateSelected = name => event => {
        let state = this.state
        state[name] = getRuleTemplate(state.selectedTemplateGroup.name, event.target.value)
        state['ruleTemplates'] = getRuleTemplates(this.state.selectedTemplateGroup.name)
        // Update selected Template Group & Rule Template names for the Business Rule
        state['businessRuleEnteredValues']['templateGroupName'] = this.state.selectedTemplateGroup.name
        state['businessRuleEnteredValues']['ruleTemplateName'] = this.state.selectedRuleTemplate.name
        this.setState(state)
    };
    /**
     * Updates the Business Rule name in the maintained states, on change of the text field which allows to enter Business Rule Name
     */
    handleBusinessRuleNameChange = event => {
        let state = this.state
        state.businessRuleEnteredValues['businessRuleName'] = event.target.value
        this.setState(
            {state}
        )
        businessRuleEnteredProperties = this.state.businessRuleEnteredValues
    }

    constructor(props) {
        super(props);
        this.state = {
            // To store loaded object arrays
            templateGroups: getTemplateGroups(), // Get from API and store
            ruleTemplates: null,
            // To store selected Strings
            // Name & description entered to prevent null display value of Select box
            selectedTemplateGroup: {'name': '', 'description': 'Please select a Business Domain'},
            selectedRuleTemplate: {'name': '', 'description': 'Please select a Rule Template'},
            // To store entered properties entered for the Business Rule
            businessRuleEnteredValues: {
                'businessRuleName': null,
            }
        }
    }

    /**
     * Validates the Business Rule properties (with the internal JS specified in the template todo: implement & (Q) is the JS thing correct?
     */
    validateBusinessRule() {
        console.log("[TODO] Validate. (Q: How?)")
        // Check external validation (Internal validation is with the JS) todo: (Q) is this correct?
        if (validateBusinessRule()) {
            let dismissButton =
                <Button color="primary" onClick={e => clearSnackBar()} dense>
                    Dismiss
                </Button>;
            ReactDOM.render(<SnackbarContent
                message="Valid Business Rule [TODO]"
                action={dismissButton}
            />, document.getElementById("snackbar"))
        } else {
            let dismissButton =
                <Button color="accent" onClick={e => clearSnackBar()} dense>
                    Dismiss
                </Button>;
            console.log("Fill in all the fields (Only done this validation for now)")
            ReactDOM.render(<SnackbarContent
                message="Invalid Business Rule"
                action={dismissButton}
            />, document.getElementById("snackbar"))
        }
    }

    render() {
        var templateGroupSelection // To display Template Group selection box
        var loadedRuleTemplates // To store Rule Templates of the selected Template Group
        var ruleTemplateSelection // To display Rule Template selection box
        var businessRuleForm // To display Business Rules form

        // To display each item in TemplateGroup selection todo: BusinessDomain refactor if needed
        var loadedTemplateGroups = this.state.templateGroups.map((loadedTemplateGroup) =>
            <MenuItem key={loadedTemplateGroup.name}
                      value={loadedTemplateGroup.name}>{loadedTemplateGroup.name}</MenuItem>
        )

        // To display TemplateGroup selection
        templateGroupSelection = <FormControl>
            <InputLabel htmlFor="templateGroup">Business Domain</InputLabel>
            <Select
                value={this.state.selectedTemplateGroup.name}
                onChange={this.handleTemplateGroupSelected('selectedTemplateGroup')}
                input={<Input id="templateGroup"/>}
            >
                {loadedTemplateGroups}
            </Select>
            <FormHelperText>{this.state.selectedTemplateGroup.description}</FormHelperText>
        </FormControl>

        // To display each item in Rule Template selection
        if (this.state.ruleTemplates !== null) {
            loadedRuleTemplates = this.state.ruleTemplates.map((loadedRuleTemplate) =>
                <MenuItem key={loadedRuleTemplate.name} value={loadedRuleTemplate.name}>
                    {loadedRuleTemplate.name}
                </MenuItem>
            )

            // To display Rule Template selection
            ruleTemplateSelection = <FormControl>
                <InputLabel htmlFor="ruleTemplate">Rule Template</InputLabel>
                <Select
                    value={this.state.selectedRuleTemplate.name}
                    onChange={this.handleRuleTemplateSelected('selectedRuleTemplate')}
                    input={<Input id="ruleTemplate"/>}
                >
                    {loadedRuleTemplates}
                </Select>
                <FormHelperText>{this.state.selectedRuleTemplate.description}</FormHelperText>
            </FormControl>

            // To display Business Rules form

            // Update selected form values in state
            let state = this.state
            state['formProperties'] = this.state.selectedRuleTemplate.properties
            this.state = state

            // Form can be displayed only if a Rule Template name exists
            if (this.state.selectedRuleTemplate.name !== '') {
                // To store property objects in a re-arranged format
                var propertiesArray = []
                for (var propertyKey in this.state.selectedRuleTemplate.properties) {
                    // Push as an object,
                    // which has the original object's Key & Value
                    // denoted by new Keys : 'propertyName' & 'propertyObject'
                    propertiesArray.push(
                        {
                            propertyName: propertyKey,
                            propertyObject: this.state.selectedRuleTemplate.properties[propertyKey.toString()]
                        }
                    )
                }

                // Map each property as input field
                const properties = propertiesArray.map((property) =>
                    <Property
                        key={property.propertyName}
                        name={property.propertyName}
                        description={property.propertyObject.description}
                        defaultValue={property.propertyObject.defaultValue}
                        type={property.propertyObject.type}
                        options={property.propertyObject.options}
                        enteredValues={this.state.businessRuleEnteredValues}
                    />
                );

                // To display the form
                businessRuleForm =
                    <div>
                        <TextField
                            id="businessRuleName"
                            name="businessRuleName"
                            label="Business Rule name"
                            placeholder="Please enter"
                            value={this.state.businessRuleName}
                            required={true}
                            onChange={this.handleBusinessRuleNameChange}
                        />
                        <br/>
                        {properties}
                        <br/>
                        <Button raised color="default"
                                onClick={(e) => this.validateBusinessRule(e)} //todo: what about a public method
                        >Validate</Button>
                        &nbsp;&nbsp;
                        <Button raised color="primary"
                                onClick={(e) => prepareBusinessRule(e)} //todo: what about a public method
                        >Create</Button>
                    </div>
            }
        }

        return (
            <div>
                <Typography type="headline" component="h2">
                    Let's create a business rule from template
                </Typography>
                <br/>
                {templateGroupSelection}
                <br/>
                <br/>
                {ruleTemplateSelection}
                <br/>
                <br/>
                {businessRuleForm}

            </div>
        );
    }
}

/**
 * Listing Business Rules in order to Edit and Delete, todo: Implement properly
 * will happen within this component
 */
class BusinessRulesModifier extends React.Component { //todo: just hard coded. remove them
    constructor(props) {
        super(props);
        this.state = {
            businessRules: props.businessRules
        }
    }

    render() {
        return (
            <div>
                <Typography type="headline" component="h2">
                    Business Rules
                </Typography>
                <br/>
                <div>
                    <FormControl>
                        <InputLabel htmlFor="age-helper">Age</InputLabel>
                        <Select
                            value={this.state.age}
                            onChange={this.handleChange('age')}
                            input={<Input id="age-helper"/>}
                        >
                            <MenuItem value="">
                                <em>None</em>
                            </MenuItem>
                            <MenuItem value={10}>Ten</MenuItem>
                            <MenuItem value={20}>Twenty</MenuItem>
                            <MenuItem value={30}>Thirty</MenuItem>
                        </Select>
                        <FormHelperText>Some important helper text</FormHelperText>
                    </FormControl>
                </div>
            </div>);
    }
}

/**
 * Represents Property, which is going to be shown as an input element
 */
class Property extends React.Component {
    // Handles onChange of Radio button
    handleSelectChange = name => event => {
        this.setState(
            {value: event.target.value}
        )
        // Update / add entered value for this property to the state
        let state = this.state
        state['enteredValues'][this.state.name] = event.target.value
        this.setState({state})
        businessRuleEnteredProperties = this.state.enteredValues
    }

    constructor(props) {
        super(props);
        this.state = {
            name: props.name,
            description: props.description,
            defaultValue: props.defaultValue,
            value: props.defaultValue,
            type: props.type,
            options: props.options,
            enteredValues: props.enteredValues
        }
        // Update default values as selected values in the state
        // todo: Mutating state directly. Otherwise the following error is thrown :

        // Can only update a mounted or mounting component.
        // This usually means you called setState() on an unmounted component.
        // This is a no-op. Please check the code for the undefined component.
        // fix => https://www.npmjs.com/package/react-safe-promise
        this.state.enteredValues[this.state.name] = this.state.defaultValue
        businessRuleEnteredProperties = this.state.enteredValues

        this.handleSelectChange = this.handleSelectChange.bind(this);
        this.handleTextChange = this.handleTextChange.bind(this);
    }

    // Handles onChange of Text Fields
    handleTextChange(event, value) {
        this.setState({
            value: value
        })
        // Update / add entered value for this property
        let state = this.state
        state['enteredValues'][this.state.name] = event.target.value
        this.setState(state)
        businessRuleEnteredProperties = this.state.enteredValues
    }

    // Renders each Property either as a TextField or Radio Group, with default values and elements as specified
    render() {
        if (this.state.type === "options") {
            const options = this.state.options.map((option) => (
                <MenuItem key={option} name={option} value={option}>{option}</MenuItem>))
            return (
                <div>
                    <br/>
                    <FormControl>
                        <InputLabel htmlFor={this.state.name}>{this.state.name}</InputLabel>
                        <Select
                            value={this.state.value}
                            onChange={this.handleSelectChange(this.state.name)}
                            input={<Input id={this.state.name}/>}
                        >
                            {options}
                        </Select>
                        <FormHelperText>{this.state.description}</FormHelperText>
                    </FormControl>
                    <br/>
                </div>
            );
        } else {
            return (
                <div>
                    <TextField
                        required
                        id={this.state.name}
                        name={this.state.name}
                        label={this.state.name}
                        defaultValue={this.state.defaultValue}
                        helperText={this.state.description}
                        margin="normal"
                        onChange={this.handleTextChange}
                    />
                    <br/>
                </div>
            );
        }
    }
}

/* Start of Methods related to API calls **************************************/

/** [1]
 * Gets available Template Groups
 * todo: from API
 *
 * @returns {Array}
 */
function getTemplateGroups() {
    // todo: remove hardcode *****************************
    var receivedTemplateGroups = [
        {
            "name": "SensorDataAnalysis1",
            "description": "Collection for sensor data analysis(1)",
            "ruleTemplates": [
                {
                    "name": "SensorAnalytics1",
                    "type": "app",
                    "instanceCount": "many",
                    "script": "<script> (optional)",
                    "description": "Configure a sensor analytics scenario to display statistics for a given stream of your choice (1)",
                    "templates": [
                        {
                            "type": "siddhiApp",
                            "content": "<from ${inStream1} select ${property1} insert into ${outStream1}>"
                        },
                        {
                            "type": "siddhiApp",
                            "content": "<from ${inStream1} select ${property2} insert into ${outStream2}>"
                        }
                    ],
                    "properties": {
                        "inStream1": {
                            "description": "Input Stream",
                            "defaultValue": "myInputStream1",
                            "type": "options",
                            "options": ["myInputStream1", "myInputStream2"]
                        },
                        "property1": {
                            "description": "Unique Identifier for the sensor",
                            "defaultValue": "sensorName",
                            "type": "options",
                            "options": ["sensorID", "sensorName"]
                        },
                        "property2": {
                            "description": "Type of value, the sensor measures",
                            "defaultValue": "sensorValue",
                            "type": "String"
                        },
                        "outStream1": {
                            "description": "Output Stream 1",
                            "defaultValue": "myOutputStream1",
                            "type": "options",
                            "options": ["myOutputStream1", "myOutputStream2"]
                        },
                        "outStream2": {
                            "description": "Output Stream 2",
                            "defaultValue": "myOutputStream2",
                            "type": "options",
                            "options": ["myOutputStream1", "myOutputStream2"]
                        }
                    }
                },
                {
                    "name": "SensorLoggings1",
                    "type": "<app>",
                    "instanceCount": "many",
                    "script": "<script> (optional)",
                    "description": "Configure a sensor analytics scenario to display statistics for a given stream of your choice (1)",
                    "templates": [
                        {
                            "type": "siddhiApp",
                            "content": "<from ${inStream1} select ${property1} insert into ${outStream1}>"
                        }
                    ],
                    "properties": {
                        "inStream1": {
                            "description": "Input Stream",
                            "defaultValue": "myInputStream1",
                            "type": "options",
                            "options": ["myInputStream1", "myInputStream2"]
                        },
                        "property1": {
                            "description": "Unique Identifier for the sensor",
                            "defaultValue": "sensorName",
                            "type": "options",
                            "options": ["sensorID", "sensorName"]
                        },
                        "outStream1": {
                            "description": "Output Stream 1",
                            "defaultValue": "myOutputStream1",
                            "type": "options",
                            "options": ["myOutputStream1", "myOutputStream2"]
                        }
                    }
                }
            ]
        },
        {
            "name": "SensorDataAnalysis2",
            "description": "Collection for sensor data analysis(2)",
            "ruleTemplates": [
                {
                    "name": "SensorAnalytics2",
                    "type": "app",
                    "instanceCount": "many",
                    "script": "<script> (optional)",
                    "description": "Configure a sensor analytics scenario to display statistics for a given stream of your choice (2)",
                    "templates": [
                        {
                            "type": "siddhiApp",
                            "content": "<from ${inStream1} select ${property1} insert into ${outStream1}>"
                        },
                        {
                            "type": "siddhiApp",
                            "content": "<from ${inStream1} select ${property2} insert into ${outStream2}>"
                        }
                    ],
                    "properties": {
                        "inStream1": {
                            "description": "Input Stream",
                            "defaultValue": "myInputStream1",
                            "type": "options",
                            "options": ["myInputStream1", "myInputStream2"]
                        },
                        "property1": {
                            "description": "Unique Identifier for the sensor",
                            "defaultValue": "sensorName",
                            "type": "options",
                            "options": ["sensorID", "sensorName"]
                        },
                        "property2": {
                            "description": "Type of value, the sensor measures",
                            "defaultValue": "sensorValue",
                            "type": "String"
                        },
                        "outStream1": {
                            "description": "Output Stream 1",
                            "defaultValue": "myOutputStream1",
                            "type": "options",
                            "options": ["myOutputStream1", "myOutputStream2"]
                        },
                        "outStream2": {
                            "description": "Output Stream 2",
                            "defaultValue": "myOutputStream2",
                            "type": "options",
                            "options": ["myOutputStream1", "myOutputStream2"]
                        }
                    }
                },
                {
                    "name": "SensorLoggings2",
                    "type": "<app>",
                    "instanceCount": "many",
                    "script": "<script> (optional)",
                    "description": "Configure a sensor analytics scenario to display statistics for a given stream of your choice (2)",
                    "templates": [
                        {
                            "type": "siddhiApp",
                            "content": "<from ${inStream1} select ${property1} insert into ${outStream1}>"
                        }
                    ],
                    "properties": {
                        "inStream1": {
                            "description": "Input Stream",
                            "defaultValue": "myInputStream1",
                            "type": "options",
                            "options": ["myInputStream1", "myInputStream2"]
                        },
                        "property1": {
                            "description": "Unique Identifier for the sensor",
                            "defaultValue": "sensorName",
                            "type": "options",
                            "options": ["sensorID", "sensorName"]
                        },
                        "outStream1": {
                            "description": "Output Stream 1",
                            "defaultValue": "myOutputStream1",
                            "type": "options",
                            "options": ["myOutputStream1", "myOutputStream2"]
                        }
                    }
                }
            ]
        }
    ]

    return receivedTemplateGroups
    // todo: *********************************************
    // todo: Get Template Groups from API
}

/** [2]
 * Get available Rule Templates, belong to the given Template Group
 * todo: from API
 *
 * @param templateGroupName
 */
function getRuleTemplates(templateGroupName) {
    // todo: remove hardcode ******************************
    var templateGroups = availableTemplateGroups
    for (let templateGroup of templateGroups) {
        if (templateGroup.name === templateGroupName) {
            return templateGroup.ruleTemplates
        }
    }
    // todo: **********************************************
    // todo: Return Rule Templates from API
}

/** [3]
 * Get available Properties, belong to the given Template Group and Rule Template
 * todo: from API
 *
 * @param templateGroupName
 * @param ruleTemplateName
 * @returns {*|Array}
 */
function getRuleTemplateProperties(templateGroupName, ruleTemplateName) {
    // todo: remove hardcode ******************************
    var ruleTemplates
    for (let templateGroup of availableTemplateGroups) {
        if (templateGroup.name === templateGroupName) {
            ruleTemplates = templateGroup.ruleTemplates
            break
        }
    }
    for (let ruleTemplate of ruleTemplates) {
        if (ruleTemplate.name === ruleTemplateName) {
            return ruleTemplate.properties
        }
    }
    // todo: **********************************************
    // todo: Return Properties from API
}

// API [4] is the POST for CreateBusinessRule

/** [5]
 * Gets available BusinessRulesCreator
 * todo: from API
 *
 * @returns {[null,null]}
 */
function getBusinessRules() {
    // todo: remove hardcode *****************************
    var receivedBusinessRules = [
        {
            "uuid": "aaabbbcccddd",
            "name": "TemperatureLoggings",
            "templateGroupName": "SensorDataAnalysis1",
            "ruleTemplateName": "SensorLoggings1",
            "type": "template",
            "properties": {
                "inStream1": "myInputStream1",
                "property1": "sensorName",
                "outStream1": "myOutputStream1"
            }
        },
        {
            "uuid": "eeefffggghhh",
            "name": "HumidityAnalytics",
            "templateGroupName": "SensorDataAnalysis1",
            "ruleTemplateName": "SensorAnalytics1",
            "type": "template",
            "properties": {
                "inStream1": "myInputStream2",
                "property1": "sensorID",
                "property2": "humidity",
                "outStream1": "myOutputStream2",
                "outStream2": "myOutputStream1"
            }
        }
    ]

    return receivedBusinessRules
    // todo: *********************************************
    // todo: Get BusinessRulesCreator from API ******************
}

/** [6]
 * Gets the BusinessRule with the given UUID
 * todo: from API
 *
 * @param businessRuleUUID
 * @returns {null|null}
 */
function getBusinessRule(businessRuleUUID) {
    // todo: remove hardcode ******************************
    for (let businessRule of availableBusinessRules) {
        if (businessRuleUUID === businessRule.uuid) {
            return businessRule
        }
    }
    // todo: *********************************************
    // todo: Get BusinessRule from API *******************

}

// Functions that have API calls unnecessarily /////////////////////////////////
/**
 * Gets the Template Group with the given name
 * todo: from API (We have available templateGroups in front end itself)
 *
 * @param templateGroupName
 * @returns {*}
 */
function getTemplateGroup(templateGroupName) {
    // todo: remove hardcode ******************************
    for (let templateGroup of availableTemplateGroups) {
        if (templateGroup.name === templateGroupName) {
            return templateGroup
        }
    }
    // todo: **********************************************
    // todo: Return Template Group from API
}

/**
 * Gets the Rule Template with the given name, that belongs to the given Template Group name
 * todo: from API (We have available templateGroups in front end itself)
 * todo: make sure to assign the belonging templateGroup for ruleTemplate
 *
 * @param templateGroupName
 * @param ruleTemplateName
 * @returns {*}
 */
function getRuleTemplate(templateGroupName, ruleTemplateName) {
    // todo: remove hardcode ******************************
    var ruleTemplates
    for (let templateGroup of availableTemplateGroups) {
        if (templateGroup.name === templateGroupName) {
            var foundTemplateGroupObject = templateGroup
            ruleTemplates = templateGroup.ruleTemplates
            break
        }
    }
    for (let ruleTemplate of ruleTemplates) {
        if (ruleTemplate.name === ruleTemplateName) {
            var foundRuleTemplateObject = ruleTemplate
            // Assign belonging Template Group
            foundRuleTemplateObject['templateGroup'] = foundTemplateGroupObject
            return ruleTemplate
        }
    }
    // todo: **********************************************
    // todo: Return Rule Template from API
}

// End of Functions that have API calls unnecessarily //////////////////////////

/* End of Methods related to API calls ****************************************/

/**
 * Starts and runs Business Rules Creator
 */
function startBusinessRulesCreator() {
    console.log("[Started Business Rules Creator]")
    ReactDOM.render(<BusinessRulesCreator/>, document.getElementById("root"))
}

/**
 * Starts and runs Business Rules Creator from Template
 */
function runBusinessRuleCreatorFromTemplate() {
    console.log("[Started Create Business Rule from Template]")

    ReactDOM.render(
        <BusinessRulesCreatorFromTemplate
            templateGroups={availableTemplateGroups}
        />, document.getElementById("root"))
}

/**
 * Starts and runs Business Rules Modifier
 */
function runBusinessRuleModifier() {
    console.log("[Started Business Rules Modifier]")

    // Get available Business Rules and display
    displayBusinessRules(availableBusinessRules)
}

/**
 * Displays available Business Rules, as thumbnails
 *
 * @param availableTemplate Groups
 */
function displayBusinessRules() {
    ReactDOM.render(
        <BusinessRulesModifier
            businessRules={availableBusinessRules}
        />, document.getElementById("root"))
}

/**
 * Prepares the Business Rule object to send the form element names & filled values as Key Value pairs, to the API
 *
 * @param filledValues
 */
function prepareBusinessRule() {
    if (validateBusinessRule()) {
        createObjectForBusinessRuleCreation()
    } else {
        var dismissButton =
            <Button color="accent" onClick={e => clearSnackBar()} dense>
                Dismiss
            </Button>;
        console.log("Fill in all the fields (Only done this validation for now)")
        ReactDOM.render(<SnackbarContent
            message="Invalid Business Rule"
            action={dismissButton}
        />, document.getElementById("snackbar"))
    }
}

/**
 * Validates the Business Rule before creating
 * No JS validation to be done here. That'll be in internal method
 */
function validateBusinessRule() {
    var isRequiredIncomplete = false
    for (let property in businessRuleEnteredProperties) {
        if ((!(businessRuleEnteredProperties[property] != null)) || (businessRuleEnteredProperties[property] === "")) {
            isRequiredIncomplete = true
            return false
        }
    }

    return true
}

/* Roughly implemented functions *//////////////////////////////////////////////////////////////////////////////////////

/**
 * Gives the mapped properties, to send to the API to create Business Rule
 */
function createObjectForBusinessRuleCreation() {
    console.log("Business Rule Properties :")
    console.log(businessRuleEnteredProperties)
    var dismissButton =
        <Button color="primary" onClick={e => clearSnackBar()} dense>
            Dismiss
        </Button>;
    ReactDOM.render(<SnackbarContent
        message="Properties are ready for sending to API"
        action={dismissButton}
    />, document.getElementById("snackbar"))
}

/**
 * Clears the Snackbar from 'snackbars' div element
 */
function clearSnackBar() {
    ReactDOM.render(<br/>, document.getElementById("snackbar"))
}

/* End of Roughly implemented functions *///////////////////////////////////////////////////////////////////////////////

// Load from API and store
var availableTemplateGroups = getTemplateGroups()
var availableBusinessRules = getBusinessRules()

// todo: ((!)Q) look into this. Seems not a good practise. If so, solution?
// Properties given in the form, for Creating a Business Rule
var businessRuleEnteredProperties

// Start & Run BusinessRulesCreator();
startBusinessRulesCreator();