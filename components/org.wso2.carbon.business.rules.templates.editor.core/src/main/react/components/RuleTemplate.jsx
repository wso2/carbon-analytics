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
// Ace Editor Components
import AceEditor from 'react-ace';
import 'brace/mode/javascript';
// Material UI Components
import Typography from 'material-ui/Typography';
import Snackbar from 'material-ui/Snackbar';
import Slide from 'material-ui/transitions/Slide';
import TextField from 'material-ui/TextField';
import Button from 'material-ui/Button';
import Collapse from 'material-ui/transitions/Collapse';
import AppBar from 'material-ui/AppBar';
import Select from 'material-ui/Select';
import {MenuItem} from 'material-ui/Menu';
import Input from 'material-ui/Input';
import Toolbar from 'material-ui/Toolbar';
import ExpandMoreIcon from 'material-ui-icons/ExpandMore';
import Radio, {RadioGroup} from 'material-ui/Radio';
import {FormControl, FormControlLabel, FormGroup, FormHelperText, FormLabel} from 'material-ui/Form';
import Checkbox from 'material-ui/Checkbox';
import {IconButton} from 'material-ui';
import AddIcon from 'material-ui-icons/Add';
import Dialog, {DialogActions, DialogContent, DialogTitle,} from 'material-ui/Dialog';
import ClearIcon from 'material-ui-icons/Clear';
import Paper from 'material-ui/Paper';
import Card, {CardActions, CardContent} from 'material-ui/Card';
// App Components
import Property from './Property';
import Template from './Template';
// App Utilities
import BusinessRulesConstants from '../constants/BusinessRulesConstants';
import TemplatesEditorConstants from '../constants/TemplatesEditorConstants';
import TemplatesEditorUtilityFunctions from '../utils/TemplatesEditorUtilityFunctions';

// Styles related to this component
const styles = {
    formPaper: {
        margin: 50,
        marginTop: 15,
    },
    header: {
        padding: 5,
        paddingLeft: 30,
    },
    flexGrow: {
        flex: '1 1 auto',
    },
};

/**
 * Represents a rule template
 */
class RuleTemplate extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            configuration: props.configuration,
            exposedStreamDefinitions: [],
            templatedElementsOfTemplates: [],
            templatedElementsOfScript: [],
            scriptAdditionsBin: [], // Stores properties sent to script, to avoid re-adding the same variable
            // Templated elements selected to add to script
            selectableTemplatedElementsForScript: [],
            templatedElementsForScriptSelection: [],
            // UI related
            isExpanded: false,
            isScriptExpanded: false,
            showScriptTemplatedElements: false,
            showSnackbar: false,
            snackbarMessage: '',
            snackbarAutoHideDuration: 3500,
        };
        this.toggleExpansion = this.toggleExpansion.bind(this);
        this.handleValueChange = this.handleValueChange.bind(this);
        this.handleScriptChange = this.handleScriptChange.bind(this);
        this.addTemplate = this.addTemplate.bind(this);
        this.addTemplatedElementsToScript = this.addTemplatedElementsToScript.bind(this);
        this.loadTemplatedElementSelection = this.loadTemplatedElementSelection.bind(this);
        this.generateProperties = this.generateProperties.bind(this);
        this.detectExposedStreamDefinition = this.detectExposedStreamDefinition.bind(this);
        this.handleExposedStreamDefinitionSelection = this.handleExposedStreamDefinitionSelection.bind(this);
        this.toggleSnackbar = this.toggleSnackbar.bind(this);
    }

    componentWillReceiveProps(props) {
        this.setState({ configuration: props.configuration });
    }

    /**
     * Toggles expansion
     */
    toggleExpansion() {
        this.setState({ isExpanded: !this.state.isExpanded });
    }

    /**
     * Updates the change of a rule template configuration value
     * @param event
     */
    handleValueChange(event) {
        const state = this.state;
        state.configuration[event.target.name] = event.target.value;
        this.setState(state);
        this.props.onChange(state.configuration);
    }

    /**
     * Updates the script content
     * @param content
     */
    handleScriptChange(content) {
        const state = this.state;
        state.configuration.script = content;
        this.setState(state);
        this.props.onChange(state.configuration);
    }

    /**
     * Adds an empty template
     */
    addTemplate() {
        const state = this.state;
        state.configuration.templates.push({
            type: 'siddhiApp',
            content: '',
        });
        this.setState(state);
        this.props.onChange(state.configuration);
    }

    /**
     * Updates the value of content of a template, that has the given index
     * @param templateIndex
     * @param value
     */
    handleTemplateValueChange(templateIndex, value) {
        const state = this.state;
        state.configuration.templates[templateIndex].content = value;
        this.setState(state);
        this.props.onChange(state.configuration);
    }

    /**
     * Removes the template that has the given index
     * @param templateIndex
     */
    removeTemplate(templateIndex) {
        const state = this.state;
        state.configuration.templates.splice(templateIndex, 1);
        this.setState(state);
        this.props.onChange(state.configuration);
    }

    /**
     * Detects and returns all the templated elements, from the templates and the script
     * @returns {Array}
     */
    detectTemplatedElements() {
        const state = this.state;
        const properties = [];
        const elementsFromTemplate = this.detectTemplatedElementsFromTemplates();
        for (const element of elementsFromTemplate) {
            properties.push(element);
        }
        // Add templated elements from the script
        if (state.configuration.script) {
            const templatedElements = TemplatesEditorUtilityFunctions.getRegexMatches(state.configuration.script,
                TemplatesEditorConstants.TEMPLATED_ELEMENT_REGEX);
            for (const templatedElement of templatedElements) {
                // To avoid re-generating same elements
                if (properties.indexOf(templatedElement) === -1) {
                    properties.push(templatedElement);
                }
            }
        }
        return properties;
    }

    /**
     * Detects templated elements from SiddhiApp templates.
     * Only the first SiddhiApp template is considered when the rule template is of type 'input' or 'output'
     * @returns {Array}
     */
    detectTemplatedElementsFromTemplates() {
        const state = this.state;
        const properties = [];
        console.log(this.state.configuration.templates.length)
        for (let i = 0; i < state.configuration.templates.length; i++) {
            const templatedElements = TemplatesEditorUtilityFunctions.getRegexMatches(
                state.configuration.templates[i].content,
                TemplatesEditorConstants.TEMPLATED_ELEMENT_REGEX);
            for (const templatedElement of templatedElements) {
                // To avoid re-generating same elements
                if (properties.indexOf(templatedElement) === -1) {
                    // To avoid re-generation for elements that are already available in properties,
                    // or added to script.
                    // However, deleting variables from a script doesn't remove items from the bin
                    if (!Object.prototype.hasOwnProperty.call(state.configuration.properties, templatedElement) &&
                        (state.scriptAdditionsBin.indexOf(templatedElement) === -1)) {
                        properties.push(templatedElement);
                    }
                }
            }
            if ((state.configuration.type !== BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) && (i === 0)) {
                break;
            }
        }
        return properties;
    }

    /* Functions related to adding templated elements to script [START] */

    /**
     * Displays dialog, to select detected templated elements in order to add to script
     */
    loadTemplatedElementSelection() {
        const detectedElements = this.detectTemplatedElementsFromTemplates();
        const selections = [];
        // Initially mark all detected elements as deselected
        for (const element of detectedElements) {
            selections.push(false);
        }
        this.setState({
            selectableTemplatedElementsForScript: detectedElements,
            templatedElementsForScriptSelection: selections,
            showScriptTemplatedElements: true,
        });
    }

    /**
     * Toggles selection state of a detected templated element that has the given index
     * @param index
     */
    toggleTemplatedElementForScriptSelection(index) {
        const state = this.state;
        state.templatedElementsForScriptSelection[index] = !state.templatedElementsForScriptSelection[index];
        this.setState(state);
    }

    /**
     * Adds selected templated elements from templates, as variables to the script
     */
    addTemplatedElementsToScript() {
        const state = this.state;
        for (let i = 0; i < this.state.selectableTemplatedElementsForScript.length; i++) {
            if (this.state.templatedElementsForScriptSelection[i]) {
                let elementName = this.state.selectableTemplatedElementsForScript[i];
                state.scriptAdditionsBin.push(elementName);
                state.configuration.script = state.configuration.script +
                    'var ' + elementName + this.generateSampleFunction(state.scriptAdditionsBin) + '\n';
            }
        }
        state.showScriptTemplatedElements = false;
        state.isScriptExpanded = true;
        this.setState(state);
        this.props.onScriptAddition(state.scriptAdditionsBin);
    }

    /**
     * Returns a string with a sample function.
     * Function name's suffix number will grow according to the members count for the given script additions bin.
     * @param scriptAdditionsBin
     */
    generateSampleFunction(scriptAdditionsBin) {
        return (' = myFunction' + scriptAdditionsBin.length + '(\'${userInputFor' +
        scriptAdditionsBin[scriptAdditionsBin.length -1 ] + '}\');\n\n' +
        '/**\n* Does some magic on given variable\n* @returns Processed input\n* @param input User given value\n*/\n' +
        'function myFunction' + scriptAdditionsBin.length + '(input) {\n' +
        '\treturn input + \' some magic\';\n}\n\n\n\n');
    }

    /* Functions related to adding templated elements to script [END] */

    /**
     * Generates properties for each templated element from templates and the script,
     * only when a detected element is not already in properties / not already added to script
     */
    generateProperties() {
        const templatedElements = this.detectTemplatedElements();
        if (templatedElements.length > 0) {
            const state = this.state;
            // Create new property for each detected templated element
            for (const elementName of this.detectTemplatedElements()) {
                // To avoid re-generation for elements that are already available in properties, or added to script.
                // However, deleting variables from a script doesn't remove items from the bin
                if (!Object.prototype.hasOwnProperty.call(state.configuration.properties, elementName) &&
                    (state.scriptAdditionsBin.indexOf(elementName) === -1)) {
                    state.configuration.properties[elementName] =
                        JSON.parse(JSON.stringify(TemplatesEditorConstants.PROPERTY_SKELETON));
                }
            }
            this.setState(state);
            this.props.onChange(state.configuration);
        } else {
            this.toggleSnackbar('No templated elements found in template(s)');
        }
    }

    /**
     * Updates given properties value
     * @param propertyName
     * @param fieldName
     * @param value
     */
    handlePropertyValueChange(propertyName, fieldName, value) {
        const state = this.state;
        state.configuration.properties[propertyName][fieldName] = value;
        this.setState(state);
        this.props.onChange(state.configuration);
    }

    /**
     * Adds a new option to the property that has the given name
     * @param propertyName
     */
    addPropertyOption(propertyName) {
        const state = this.state;
        // Add as a next option, or the first option
        if (state.configuration.properties[propertyName].options) {
            state.configuration.properties[propertyName].options.push('');
        } else {
            state.configuration.properties[propertyName].options = [''];
        }
        this.setState(state);
        this.props.onChange(state.configuration);
    }

    /**
     * Updates value of the option with the given index, that belongs to the given property
     * @param propertyName
     * @param optionIndex
     * @param value
     */
    handlePropertyOptionChange(propertyName, optionIndex, value) {
        const state = this.state;
        state.configuration.properties[propertyName].options[optionIndex] = value;
        this.setState(state);
        this.props.onChange(state.configuration);
    }

    /**
     * Removes the option with the given index, from the given property name
     * @param propertyName
     * @param optionIndex
     */
    removePropertyOption(propertyName, optionIndex) {
        const state = this.state;
        state.configuration.properties[propertyName].options.splice(optionIndex, 1);
        if (state.configuration.properties[propertyName].options.length === 0) {
            delete state.configuration.properties[propertyName].options;
        }
        this.setState(state);
        this.props.onChange(state.configuration);
    }

    /**
     * Removes all properties from the property with the given name
     * @param propertyName
     */
    removeAllPropertyOptions(propertyName) {
        const state = this.state;
        delete state.configuration.properties[propertyName].options;
        this.setState(state);
        this.props.onChange(state.configuration);
    }

    /**
     * Removes the property that has the given name
     * @param propertyName
     */
    removeProperty(propertyName) {
        const state = this.state;
        state.templatedElements = this.detectTemplatedElements();
        if (state.templatedElements.length > 0) {
            delete state.configuration.properties[propertyName];
        }
        this.setState(state);
        this.props.onChange(state.configuration);
    }

    /**
     * Returns an array of properties, consisting properties - each converted as an array member from an object
     * @returns {Array}
     */
    getPropertiesAsArray() {
        const propertiesArray = [];
        for (const propertyName in this.state.configuration.properties) {
            if (Object.prototype.hasOwnProperty.call(this.state.configuration.properties, propertyName)) {
                propertiesArray.push({
                    propertyName: propertyName.toString(),
                    propertyObject: this.state.configuration.properties[propertyName.toString()],
                });
            }
        }
        return propertiesArray;
    }

    /**
     * Detects exposed stream definitions from the template content, and updates in the state
     */
    detectExposedStreamDefinition() {
        const state = this.state;
        const exposedStreamDefinitions = TemplatesEditorUtilityFunctions.getRegexMatches(
            this.state.configuration.templates[0].content, TemplatesEditorConstants.STREAM_DEFINITION_REGEX);
        // Reset to avoid detected definitions from the last update
        state.exposedStreamDefinitions = [];
        for (const definition of exposedStreamDefinitions) {
            state.exposedStreamDefinitions.push(definition);
        }
        // Add exposedStreamDefinition key under first template
        if (exposedStreamDefinitions.length === 1) {
            state.configuration.templates[0].exposedStreamDefinition = exposedStreamDefinitions[0];
        } else {
            state.configuration.templates[0].exposedStreamDefinition = '';
        }
        this.setState(state);
        this.props.onChange(state.configuration);
    }

    /**
     * Updates the exposed stream definition when one from dropdown is selected
     * @param event
     */
    handleExposedStreamDefinitionSelection(event) {
        const state = this.state;
        state.configuration.templates[0].exposedStreamDefinition = event.target.value;
        this.setState(state);
        this.props.onChange(state.configuration);
    }

    /**
     * Displays available exposed stream definition(s)
     * @returns {XML}
     */
    displayExposedStreamDefinition() {
        const title =
            (<div>
                <Typography type="title">
                    Exposed Stream Definition
                    <Button
                        color='primary'
                        style={{ marginLeft: 10 }}
                        onClick={this.detectExposedStreamDefinition}
                    >
                        Detect
                    </Button>
                </Typography>
            </div>);
        if (this.state.configuration.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_INPUT ||
            this.state.configuration.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_OUTPUT) {
            if (this.state.exposedStreamDefinitions.length > 1) {
                // Allow user to select one exposed stream definition
                return (
                    <div>
                        {title}
                        <FormControl
                            fullWidth
                            margin='normal'
                            required
                        >
                            <Select
                                value={(this.state.configuration.templates[0].exposedStreamDefinition) ?
                                    (this.state.configuration.templates[0].exposedStreamDefinition) : ('')}
                                onChange={this.handleExposedStreamDefinitionSelection}
                                input={<Input id='exposedStreamDefinition' />}
                            >
                                {this.state.exposedStreamDefinitions.map((definition, index) =>
                                    (<MenuItem key={index} name={index} value={definition}>{definition}</MenuItem>))}
                            </Select>
                            <FormHelperText>Select a stream from the template</FormHelperText>
                        </FormControl>
                    </div>);
            } else if (this.state.exposedStreamDefinitions.length === 1) {
                // Display the one and only exposed stream definition from state
                return (
                    <div>
                        {title}
                        <br />
                        <Typography type='subheading'>
                            {this.state.configuration.templates[0].exposedStreamDefinition}
                        </Typography>
                    </div>);
            }
            return (
                <div>
                    {title}
                    <br />
                    <Typography type='subheading'>
                        {(this.state.configuration.templates[0].exposedStreamDefinition) ?
                            (this.state.configuration.templates[0].exposedStreamDefinition) :
                            ('No stream definition found in the template')}
                    </Typography>
                </div>);
        }
        return (null);
    }

    /**
     * Toggles display of the snackbar.
     * Shows snackbar for readable amount of time with the message - if given, hides otherwise
     * @param message
     */
    toggleSnackbar(message) {
        this.setState({
            snackbarAutoHideDuration: (message ? TemplatesEditorUtilityFunctions.calculateReadingTime(message) : 3500),
            showSnackbar: !!message,
            snackbarMessage: (message ? message : '')
        })
    }

    render() {
        return (
            <div>
                <AppBar position="static" color="default">
                    <Toolbar>
                        <Typography type='subheading' style={{ flex: 1 }}>{this.state.configuration.uuid}</Typography>
                        <IconButton
                            onClick={this.toggleExpansion}
                        >
                            <ExpandMoreIcon />
                        </IconButton>
                        <IconButton
                            color='primary'
                            aria-label='Remove'
                            onClick={this.props.removeRuleTemplate}
                        >
                            <ClearIcon />
                        </IconButton>
                    </Toolbar>
                </AppBar>
                <Paper>
                    <Collapse in={this.state.isExpanded} transitionDuration='auto' unmountOnExit>
                        <div style={styles.formPaper}>
                            <TextField
                                fullWidth
                                id='uuid'
                                name='uuid'
                                label='UUID'
                                value={this.state.configuration.uuid}
                                helperText='Used to identify the rule template'
                                margin="normal"
                                onChange={this.handleValueChange}
                            />
                            <TextField
                                fullWidth
                                id='name'
                                name='name'
                                label='Name'
                                value={this.state.configuration.name}
                                helperText='Used for representation'
                                margin="normal"
                                onChange={this.handleValueChange}
                            />
                            <TextField
                                fullWidth
                                id='description'
                                name='description'
                                label='Description'
                                value={this.state.configuration.description ?
                                    this.state.configuration.description : ''}
                                helperText='Short description of what this rule template does'
                                margin="normal"
                                onChange={this.handleValueChange}
                            />
                            <br />
                            <br />
                            <br />
                            <FormControl component="fieldset" required>
                                <FormLabel component="legend">Type</FormLabel>
                                <RadioGroup
                                    aria-label='type'
                                    name='type'
                                    value={this.state.configuration.type}
                                    onChange={this.handleValueChange}
                                >
                                    <FormControlLabel value="template" control={<Radio />} label="Template" />
                                    <FormControlLabel value="input" control={<Radio />} label="Input" />
                                    <FormControlLabel value="output" control={<Radio />} label="Output" />
                                </RadioGroup>
                                <FormHelperText>
                                    Select the rule template type
                                </FormHelperText>
                            </FormControl>
                            <br />
                            <br />
                            <br />
                            <FormControl component="fieldset" required>
                                <FormLabel component="legend">Instance Count</FormLabel>
                                <RadioGroup
                                    aria-label="instanceCount"
                                    name='instanceCount'
                                    value={this.state.configuration.instanceCount}
                                    onChange={this.handleValueChange}
                                >
                                    <FormControlLabel value="one" control={<Radio />} label="One" />
                                    <FormControlLabel value="many" control={<Radio />} label="Many" />
                                </RadioGroup>
                                <FormHelperText>
                                    Select the usage constraint for this rule template
                                </FormHelperText>
                            </FormControl>
                            <br />
                            <br />
                            <br />
                            <Typography type="title">Templates</Typography>
                            <br />
                            {this.state.configuration.templates.map((template, index) =>
                                (<div key={index}>
                                    <Template
                                        type={template.type}
                                        content={template.content}
                                        handleTemplateValueChange=
                                            {value => this.handleTemplateValueChange(index, value)}
                                        invalid={((this.state.configuration.type !==
                                            BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) && (index > 0)) ?
                                            (this.state.configuration.type) : false}
                                        notRemovable={this.state.configuration.templates.length === 1}
                                        removeTemplate={() => this.removeTemplate(index)}
                                        editorSettings={this.props.editorSettings}
                                    />
                                    <br />
                                </div>))}
                            <br />
                            {this.displayExposedStreamDefinition()}
                            {((this.state.configuration.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) ?
                                (<IconButton
                                    color='primary'
                                    style={{ backgroundColor: '#EF6C00', color: 'white' }}
                                    aria-label='Add'
                                    onClick={this.addTemplate}
                                >
                                    <AddIcon />
                                </IconButton>) :
                                (null))}
                            <br />
                            <br />
                            <br />
                            <Typography type="title">Script</Typography>
                            <br />
                            <Card>
                                <CardActions disableActionSpacing style={{ padding: 5 }}>
                                    <Button
                                        raised
                                        color='primary'
                                        style={{ marginLeft: 5 }}
                                        onClick={this.loadTemplatedElementSelection}
                                    >
                                        Add variables
                                    </Button>
                                    <div style={styles.flexGrow} />
                                    <IconButton
                                        onClick={() => this.setState({
                                            isScriptExpanded: !this.state.isScriptExpanded })}
                                    >
                                        <ExpandMoreIcon/>
                                    </IconButton>
                                </CardActions>
                                <Collapse in={this.state.isScriptExpanded} transitionDuration='auto' unmountOnExit>
                                    <CardContent style={{padding: 0, paddingBottom: 0}}>
                                        <AceEditor
                                            mode='javascript'
                                            theme={this.props.editorSettings.theme}
                                            fontSize={this.props.editorSettings.fontSize}
                                            wrapEnabled={this.props.editorSettings.wrapEnabled}
                                            value={this.state.configuration.script}
                                            onChange={this.handleScriptChange}
                                            name='script'
                                            showPrintMargin={false}
                                            tabSize={3}
                                            useSoftTabs='true'
                                            width='100%'
                                            editorProps={{
                                                $blockScrolling: Infinity,
                                                "display_indent_guides": true,
                                                folding: "markbeginandend" }}
                                            setOptions={{
                                                cursorStyle: "smooth",
                                                wrapBehavioursEnabled: true
                                            }}
                                        />
                                    </CardContent>
                                </Collapse>
                            </Card>
                            <br />
                            <br />
                            <br />
                            <br />
                            <Typography type="title">
                                Properties
                                &nbsp;
                                <Button
                                    raised
                                    color='primary'
                                    style={{ marginLeft: 10 }}
                                    onClick={this.generateProperties}
                                >
                                    Generate
                                </Button>
                            </Typography>
                            <br />
                            {this.getPropertiesAsArray().map(property =>
                                (<div key={property.propertyName}>
                                    <Property
                                        key={property.propertyName}
                                        configuration={property}
                                        handlePropertyValueChange={(fieldName, value) =>
                                            this.handlePropertyValueChange(property.propertyName, fieldName, value)}
                                        addPropertyOption={() => this.addPropertyOption(property.propertyName)}
                                        handlePropertyOptionChange={(optionIndex, value) =>
                                            this.handlePropertyOptionChange(property.propertyName, optionIndex, value)}
                                        removePropertyOption={optionIndex =>
                                            this.removePropertyOption(property.propertyName, optionIndex)}
                                        removeAllPropertyOptions={() =>
                                            this.removeAllPropertyOptions(property.propertyName)}
                                        removeProperty={() => this.removeProperty(property.propertyName)}
                                    />
                                    <br />
                                </div>))}
                        </div>
                    </Collapse>
                </Paper>
                <Dialog
                    open={this.state.showScriptTemplatedElements}
                    onClose={() => this.setState({ showScriptTemplatedElements: false })}
                    aria-labelledby="add-to-script-dialog-title"
                    aria-describedby="add-to-script-dialog-description"
                >
                    <DialogTitle id="alert-dialog-title">
                        {(this.state.selectableTemplatedElementsForScript.length > 0) ?
                            ('Select templated elements') : ('No Templated Elements Found')}
                    </DialogTitle>
                    <DialogContent>
                        {(this.state.selectableTemplatedElementsForScript.length > 0) ?
                            (<FormControl component="fieldset">
                                <FormGroup>
                                    {this.state.selectableTemplatedElementsForScript.map((element, index) =>
                                        <FormControlLabel
                                            key={index}
                                            control={
                                                <Checkbox
                                                    checked={this.state.templatedElementsForScriptSelection[index]}
                                                    onChange={() =>
                                                        this.toggleTemplatedElementForScriptSelection(index)}
                                                    value={element}
                                                />
                                            }
                                            label={element}
                                        />)}
                                </FormGroup>
                            </FormControl>) :
                            (<Typography type='subheading'>
                                {'Insert templated elements as: \${templatedElement} in template'}
                                {((this.state.configuration.type ===
                                    BusinessRulesConstants.RULE_TEMPLATE_TYPE_INPUT) ||
                                    (this.state.configuration.type ===
                                    BusinessRulesConstants.RULE_TEMPLATE_TYPE_OUTPUT)) ? (null) : ('(s)')}
                            </Typography>)}
                    </DialogContent>
                    <DialogActions>
                        <Button
                            onClick={() => this.setState({ showScriptTemplatedElements: false })}
                            color='default'
                            autoFocus
                        >
                           Cancel
                        </Button>
                        {(this.state.selectableTemplatedElementsForScript.length > 0) ?
                            (<Button
                                onClick={this.addTemplatedElementsToScript}
                                color='primary'
                                disabled={this.state.templatedElementsForScriptSelection.indexOf(true) === -1}
                                autoFocus
                            >
                                Add to script
                            </Button>) : (null)}
                    </DialogActions>
                </Dialog>
                <Snackbar
                    autoHideDuration={this.state.snackbarAutoHideDuration}
                    open={this.state.showSnackbar}
                    onRequestClose={this.toggleSnackbar}
                    transition={<Slide direction='up'/>}
                    SnackbarContentProps={{
                        'aria-describedby': 'snackbarMessage',
                    }}
                    message={
                        <span id="snackbarMessage">
                        {this.state.snackbarMessage}
                    </span>
                    }
                />
            </div>
        );
    }
}

export default RuleTemplate;
