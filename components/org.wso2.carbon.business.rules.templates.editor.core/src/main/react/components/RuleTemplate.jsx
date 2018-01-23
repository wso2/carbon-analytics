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
import PropTypes from 'prop-types';
import _ from 'lodash';
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
import { MenuItem } from 'material-ui/Menu';
import Input from 'material-ui/Input';
import Toolbar from 'material-ui/Toolbar';
import ExpandMoreIcon from 'material-ui-icons/ExpandMore';
import Radio, { RadioGroup } from 'material-ui/Radio';
import { FormControl, FormControlLabel, FormGroup, FormHelperText, FormLabel } from 'material-ui/Form';
import Checkbox from 'material-ui/Checkbox';
import { IconButton } from 'material-ui';
import AddIcon from 'material-ui-icons/Add';
import Dialog, { DialogActions, DialogContent, DialogTitle } from 'material-ui/Dialog';
import ClearIcon from 'material-ui-icons/Clear';
import Paper from 'material-ui/Paper';
import Card, { CardActions, CardContent } from 'material-ui/Card';
// App Components
import Property from './Property';
import Template from './Template';
// App Utilities
import TemplateEditorConstants from '../constants/TemplateEditorConstants';
import TemplateEditorUtilityFunctions from '../utils/TemplateEditorUtilityFunctions';

/**
 * Styles related to this component
 */
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
            exposedStreamDefinitions: [], // Stores detected stream definitions from SiddhiApp templates
            // To display templated elements for selecting and adding to script
            selectableTemplatedElementsForScript: [],
            templatedElementsForScriptSelection: [],
            // UI related
            isExpanded: false,
            isScriptExpanded: false,
            showScriptTemplatedElements: false,
            showSnackbar: false,
            snackbarMessage: '',
            snackbarAutoHideDuration: 3500
        };
        this.toggleExpansion = this.toggleExpansion.bind(this);
        this.addTemplate = this.addTemplate.bind(this);
        this.addTemplatedElementsToScript = this.addTemplatedElementsToScript.bind(this);
        this.loadTemplatedElementSelection = this.loadTemplatedElementSelection.bind(this);
        this.generateProperties = this.generateProperties.bind(this);
        this.handleTemplateValueChange = _.debounce(this.handleTemplateValueChange.bind(this), 900);
        this.detectExposedStreamDefinitions = this.detectExposedStreamDefinitions.bind(this);
        this.handleExposedStreamDefinitionSelection = this.handleExposedStreamDefinitionSelection.bind(this);
        this.toggleSnackbar = this.toggleSnackbar.bind(this);
    }

    componentWillReceiveProps(props) {
        this.setState({ configuration: props.configuration });
    }

    /**
     * Toggles expansion of the rule template container
     */
    toggleExpansion() {
        this.setState({ isExpanded: !this.state.isExpanded });
    }

    /**
     * Updates the change of a rule template configuration value
     * @param name
     * @param value
     */
    handleValueChange(name, value) {
        const configuration = this.props.configuration;
        configuration[name] = value;
        this.props.onChange(configuration);
    }

    /**
     * Adds an empty template
     */
    addTemplate() {
        const configuration = this.props.configuration;
        configuration.templates.push({
            type: 'siddhiApp',
            content: ''
        });
        this.props.onChange(configuration);
    }

    /**
     * Updates the content of SiddhiApp template that has the given index, with given value
     * @param templateIndex
     * @param value
     */
    handleTemplateValueChange(templateIndex, value) {
        const configuration = this.props.configuration;
        configuration.templates[templateIndex].content = value;
        if ((this.props.configuration.type === TemplateEditorConstants.RULE_TEMPLATE_TYPE_INPUT) ||
                (this.props.configuration.type === TemplateEditorConstants.RULE_TEMPLATE_TYPE_OUTPUT)) {
            this.detectExposedStreamDefinitions();
        }
        this.props.onChange(configuration);
    }

    /**
     * Removes the SiddhiApp template that has the given index
     * @param templateIndex
     */
    removeTemplate(templateIndex) {
        const configuration = this.props.configuration;
        configuration.templates.splice(templateIndex, 1);
        this.props.onChange(configuration);
    }

    /* Functions related to adding templated elements to script [START] */

    /**
     * Displays dialog with detected templated elements from template(s), to select for adding to script
     */
    loadTemplatedElementSelection() {
        // const detectedElements = this.detectTemplatedElementsFromTemplates();
        const detectedElements = TemplateEditorUtilityFunctions.detectTemplatedElementsFromTemplates(
            this.props.configuration, this.props.scriptAdditionsBin);
        // Reset selections
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
     * Toggles selection state of the detected templated element that has the given index
     * @param index
     */
    toggleTemplatedElementForScriptSelection(index) {
        const state = this.state;
        state.templatedElementsForScriptSelection[index] = !state.templatedElementsForScriptSelection[index];
        this.setState(state);
    }

    /**
     * Adds selected templated elements from templates to script, each as a variable with a sample function
     */
    addTemplatedElementsToScript() {
        const state = this.state;
        const configuration = this.props.configuration;
        const scriptAdditionsBin = this.props.scriptAdditionsBin;
        for (let i = 0; i < state.selectableTemplatedElementsForScript.length; i++) {
            if (state.templatedElementsForScriptSelection[i]) {
                let elementName = state.selectableTemplatedElementsForScript[i];
                scriptAdditionsBin.push(elementName);
                configuration.script = configuration.script +
                    `var ${elementName} = ${RuleTemplate.generateSampleFunction(scriptAdditionsBin)}\n`;
            }
        }
        state.showScriptTemplatedElements = false;
        state.isScriptExpanded = true;
        this.setState(state);
        this.props.onScriptAddition(scriptAdditionsBin);
        this.props.onChange(configuration);
    }

    /**
     * Returns a string with a sample function
     * @param scriptAdditionsBin
     */
    static generateSampleFunction(scriptAdditionsBin) {
        return (`myFunction${scriptAdditionsBin.length}` +
            `(\'\${userInputFor${scriptAdditionsBin[scriptAdditionsBin.length -1 ]}}\');` +
            `\n` +
            `\n/**` +
            `\n* Does some magic on given variable` +
            `\n* @returns Processed input` +
            `\n* @param input User given value` +
            `\n*/` +
            `\nfunction myFunction${scriptAdditionsBin.length}(input) {` +
            `\n\treturn input + \' some magic\';` +
            `\n}` +
            `\n\n\n\n`);
    }

    /* Functions related to adding templated elements to script [END] */

    /**
     * Generates property object for each distinct templated element from SiddhiApp templates and the script,
     * only when the detected element is not already available in properties / added to script
     */
    generateProperties() {
        const templatedElements =
            TemplateEditorUtilityFunctions.detectTemplatedElements(
                this.props.configuration, this.props.scriptAdditionsBin);
        if (templatedElements.length > 0) {
            const configuration = this.props.configuration;
            // Create new property for each detected templated element
            for (const elementName of templatedElements) {
                // To avoid re-generation for elements that are already available in properties, or added to script.
                // However, deleting variables from a script doesn't remove items from the bin
                if (!Object.prototype.hasOwnProperty.call(configuration.properties, elementName) &&
                    (this.props.scriptAdditionsBin.indexOf(elementName) === -1)) {
                    configuration.properties[elementName] =
                        JSON.parse(JSON.stringify(TemplateEditorConstants.PROPERTY_SKELETON));
                }
            }
            this.props.onChange(configuration);
        } else {
            this.toggleSnackbar('No templated elements found in template(s)');
        }
    }

    /**
     * Updates given properties value
     * @param propertyName
     * @param key
     * @param value
     */
    handlePropertyValueChange(propertyName, key, value) {
        const configuration = this.props.configuration;
        configuration.properties[propertyName][key] = value;
        this.props.onChange(configuration);
    }

    /**
     * Adds a new option to the property that has the given name
     * @param propertyName
     */
    addPropertyOption(propertyName) {
        const configuration = this.props.configuration;
        if (configuration.properties[propertyName].options) {
            // Add as next option
            configuration.properties[propertyName].options.push('');
        } else {
            // Add as first option
            configuration.properties[propertyName].options = [''];
        }
        this.props.onChange(configuration);
    }

    /**
     * Updates value of the option with the given index, that belongs to the property with the given name
     * @param propertyName
     * @param optionIndex
     * @param value
     */
    handlePropertyOptionChange(propertyName, optionIndex, value) {
        const configuration = this.props.configuration;
        configuration.properties[propertyName].options[optionIndex] = value;
        this.props.onChange(configuration);
    }

    /**
     * Removes the option with the given index, from the property with the given name
     * @param propertyName
     * @param optionIndex
     */
    removePropertyOption(propertyName, optionIndex) {
        const configuration = this.props.configuration;
        configuration.properties[propertyName].options.splice(optionIndex, 1);
        if (configuration.properties[propertyName].options.length === 0) {
            delete configuration.properties[propertyName].options;
        }
        this.props.onChange(configuration);
    }

    /**
     * Removes the property that has the given name
     * @param propertyName
     */
    removeProperty(propertyName) {
        const configuration = this.props.configuration;
        delete configuration.properties[propertyName];
        this.props.onChange(configuration);
    }

    /**
     * Returns a property array, whose members are property objects converted as array members
     * @returns {Array}
     */
    getPropertiesAsArray() {
        const propertiesArray = [];
        for (const propertyName in this.props.configuration.properties) {
            if (Object.prototype.hasOwnProperty.call(this.props.configuration.properties, propertyName)) {
                propertiesArray.push({
                    propertyName: propertyName.toString(),
                    propertyObject: this.props.configuration.properties[propertyName.toString()],
                });
            }
        }
        return propertiesArray;
    }

    /**
     * Detects exposed stream definitions from the template content,
     * and updates 'exposedStreamDefinitions' in the state
     */
    detectExposedStreamDefinitions() {
        const state = this.state;
        const configuration = this.props.configuration;
        const exposedStreamDefinitions = TemplateEditorUtilityFunctions.getRegexMatches(
            this.props.configuration.templates[0].content, TemplateEditorConstants.STREAM_DEFINITION_REGEX);
        // Store detected stream definitions in state
        state.exposedStreamDefinitions = [];
        for (const definition of exposedStreamDefinitions) {
            state.exposedStreamDefinitions.push(definition);
        }
        if (exposedStreamDefinitions.length === 1) {
            // Single stream definition detected - Direct assign
            configuration.templates[0].exposedStreamDefinition = exposedStreamDefinitions[0];
        } else {
            // Multiple stream definitions detected - Introduce the key
            configuration.templates[0].exposedStreamDefinition = '';
        }
        this.setState(state);
        this.props.onChange(configuration);
    }

    /**
     * Updates the exposed stream definition, when one from dropdown is selected
     * @param event
     */
    handleExposedStreamDefinitionSelection(event) {
        const configuration = this.props.configuration;
        configuration.templates[0].exposedStreamDefinition = event.target.value;
        this.props.onChange(configuration);
    }

    /**
     * Returns detected exposed stream definitions.
     * Dropdown for selection, when more than one streams are detected
     * @returns {XML}
     */
    displayExposedStreamDefinition() {
        const title =
            (<div>
                <Typography type="title">
                    Exposed Stream Definition
                </Typography>
                <Button
                    color='primary'
                    style={{ marginLeft: 10 }}
                    onClick={this.detectExposedStreamDefinitions}
                >
                    Detect
                </Button>
            </div>);
        if (this.props.configuration.type === TemplateEditorConstants.RULE_TEMPLATE_TYPE_INPUT ||
            this.props.configuration.type === TemplateEditorConstants.RULE_TEMPLATE_TYPE_OUTPUT) {
            if (this.state.exposedStreamDefinitions.length > 1) {
                // Dropdown with detected stream definitions
                return (
                    <div>
                        {title}
                        <FormControl
                            fullWidth
                            margin='normal'
                            required
                        >
                            <Select
                                value={(this.props.configuration.templates[0].exposedStreamDefinition) ?
                                    (this.props.configuration.templates[0].exposedStreamDefinition) : ('')}
                                onChange={this.handleExposedStreamDefinitionSelection}
                                input={<Input id='exposedStreamDefinition' />}
                            >
                                {this.state.exposedStreamDefinitions.map((definition, index) =>
                                    (<MenuItem key={index} name={index} value={definition}>{definition}</MenuItem>))}
                            </Select>
                            <FormHelperText>Select a stream definition from the template</FormHelperText>
                        </FormControl>
                    </div>);
            } else if (this.state.exposedStreamDefinitions.length === 1) {
                // The one and only detected stream definition
                return (
                    <div>
                        {title}
                        <br />
                        <Typography type='subheading'>
                            {this.props.configuration.templates[0].exposedStreamDefinition}
                        </Typography>
                    </div>);
            }
            return (
                <div>
                    {title}
                    <br />
                    <Typography type='subheading'>
                        {(this.props.configuration.templates[0].exposedStreamDefinition) ?
                            (this.props.configuration.templates[0].exposedStreamDefinition) :
                            ('No stream definition found in the template')}
                    </Typography>
                </div>);
        }
        return (null);
    }

    /**
     * Toggles display of the snackbar.
     * Shows snackbar for readable amount of time with the message - if given, Hides otherwise
     * @param message
     */
    toggleSnackbar(message) {
        this.setState({
            snackbarAutoHideDuration: (message ? TemplateEditorUtilityFunctions.calculateReadingTime(message) : 3500),
            showSnackbar: !!message,
            snackbarMessage: (message ? message : '')
        })
    }

    render() {
        return (
            <div>
                <AppBar position="static" color="default">
                    <Toolbar>
                        <Typography type='subheading' style={{ flex: 1 }}>{this.props.configuration.uuid}</Typography>
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
                                value={this.props.configuration.uuid}
                                helperText='Used to identify the rule template'
                                margin="normal"
                                onChange={e => this.handleValueChange(e.target.name, e.target.value)}
                            />
                            <TextField
                                fullWidth
                                id='name'
                                name='name'
                                label='Name'
                                value={this.props.configuration.name}
                                helperText='Used for representation'
                                margin="normal"
                                onChange={e => this.handleValueChange(e.target.name, e.target.value)}
                            />
                            <TextField
                                fullWidth
                                id='description'
                                name='description'
                                label='Description'
                                value={this.props.configuration.description ?
                                    this.props.configuration.description : ''}
                                helperText='Short description of what this rule template does'
                                margin="normal"
                                onChange={e => this.handleValueChange(e.target.name, e.target.value)}
                            />
                            <br />
                            <br />
                            <br />
                            <FormControl component="fieldset" required>
                                <FormLabel component="legend">Type</FormLabel>
                                <RadioGroup
                                    aria-label='type'
                                    name='type'
                                    value={this.props.configuration.type}
                                    onChange={e => this.handleValueChange(e.target.name, e.target.value)}
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
                                    value={this.props.configuration.instanceCount}
                                    onChange={e => this.handleValueChange(e.target.name, e.target.value)}
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
                            {this.props.configuration.templates.map((template, index) =>
                                (<div key={index}>
                                    <Template
                                        type={template.type}
                                        content={template.content}
                                        detectExposedStreamDefinitions={this.detectExposedStreamDefinitions}
                                        handleTemplateValueChange=
                                            {value => this.handleTemplateValueChange(index, value)}
                                        invalid={((this.props.configuration.type !==
                                            TemplateEditorConstants.RULE_TEMPLATE_TYPE_TEMPLATE) && (index > 0)) ?
                                            (this.props.configuration.type) : false}
                                        notRemovable={this.props.configuration.templates.length === 1}
                                        removeTemplate={() => this.removeTemplate(index)}
                                        editorSettings={this.props.editorSettings}
                                    />
                                    <br />
                                </div>))}
                            <br />
                            {this.displayExposedStreamDefinition()}
                            {((this.props.configuration.type === TemplateEditorConstants.RULE_TEMPLATE_TYPE_TEMPLATE) ?
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
                                        <ExpandMoreIcon />
                                    </IconButton>
                                </CardActions>
                                <Collapse in={this.state.isScriptExpanded} transitionDuration='auto' unmountOnExit>
                                    <CardContent style={{ padding: 0, paddingBottom: 0 }}>
                                        <AceEditor
                                            mode='javascript'
                                            theme={this.props.editorSettings.theme}
                                            fontSize={this.props.editorSettings.fontSize}
                                            wrapEnabled={this.props.editorSettings.wrapEnabled}
                                            value={this.props.configuration.script}
                                            onChange={v => this.handleValueChange('script', v)}
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
                    aria-labelledby="templatedElementsSelectionDialog"
                    aria-describedby="templatedElementsSelectionDialog"
                >
                    <DialogTitle id="templatedElementsSelectionTitle">
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
                                {((this.props.configuration.type ===
                                    TemplateEditorConstants.RULE_TEMPLATE_TYPE_INPUT) ||
                                    (this.props.configuration.type ===
                                    TemplateEditorConstants.RULE_TEMPLATE_TYPE_OUTPUT)) ? (null) : ('(s)')}
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

RuleTemplate.propTypes = {
    configuration: PropTypes.object.isRequired,
    onChange: PropTypes.func.isRequired,
    onScriptAddition: PropTypes.func.isRequired,
    removeRuleTemplate: PropTypes.func.isRequired,
    editorSettings: PropTypes.object.isRequired
};

export default RuleTemplate;
