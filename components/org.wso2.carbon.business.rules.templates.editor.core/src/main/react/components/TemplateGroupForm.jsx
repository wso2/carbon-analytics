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
import downloadFile from 'react-file-download';
// Ace Editor Components
import AceEditor from 'react-ace';
import 'brace/mode/json';
import 'brace/theme/ambiance';
import 'brace/theme/chaos';
import 'brace/theme/chrome';
import 'brace/theme/clouds';
import 'brace/theme/clouds_midnight';
import 'brace/theme/cobalt';
import 'brace/theme/crimson_editor';
import 'brace/theme/dawn';
import 'brace/theme/dracula';
import 'brace/theme/dreamweaver';
import 'brace/theme/eclipse';
import 'brace/theme/github';
import 'brace/theme/gob';
import 'brace/theme/gruvbox';
import 'brace/theme/idle_fingers';
import 'brace/theme/iplastic';
import 'brace/theme/katzenmilch';
import 'brace/theme/kr_theme';
import 'brace/theme/kuroir';
import 'brace/theme/merbivore';
import 'brace/theme/merbivore_soft';
import 'brace/theme/mono_industrial';
import 'brace/theme/monokai';
import 'brace/theme/pastel_on_dark';
import 'brace/theme/solarized_dark';
import 'brace/theme/solarized_light';
import 'brace/theme/sqlserver';
import 'brace/theme/terminal';
import 'brace/theme/textmate';
import 'brace/theme/tomorrow';
import 'brace/theme/tomorrow_night';
import 'brace/theme/tomorrow_night_blue';
import 'brace/theme/tomorrow_night_bright';
import 'brace/theme/tomorrow_night_eighties';
import 'brace/theme/twilight';
import 'brace/theme/vibrant_ink';
import 'brace/theme/xcode';
// Material UI Components
import Button from 'material-ui/Button';
import Select from 'material-ui/Select';
import {MenuItem} from 'material-ui/Menu';
import Input from 'material-ui/Input';
import {FormControl, FormControlLabel, FormGroup} from 'material-ui/Form';
import Checkbox from 'material-ui/Checkbox';
import ErrorIcon from 'material-ui-icons/Error';
import Tooltip from 'material-ui/Tooltip';
import Typography from 'material-ui/Typography';
import TextField from 'material-ui/TextField';
import Paper from 'material-ui/Paper';
import AddIcon from 'material-ui-icons/Add';
import CachedIcon from 'material-ui-icons/Cached';
import {IconButton} from 'material-ui';
import Toolbar from 'material-ui/Toolbar';
import Snackbar from 'material-ui/Snackbar';
import Slide from 'material-ui/transitions/Slide';
import Dialog, {DialogActions, DialogContent, DialogTitle,} from 'material-ui/Dialog';
// App Components
import RuleTemplate from './RuleTemplate';
// App Constants
import BusinessRulesConstants from '../constants/BusinessRulesConstants';
import Header from './Header';
// Custom theme
import {createMuiTheme, MuiThemeProvider} from 'material-ui/styles';
import {Orange} from '../theme/BusinessRulesManagerColors';
import TemplatesEditorConstants from '../constants/TemplatesEditorConstants';
import TemplatesEditorUtilityFunctions from '../utils/TemplatesEditorUtilityFunctions';

const theme = createMuiTheme({
    palette: {
        primary: Orange,
    },
});

// Styles related to this component
const styles = {
    gridRoot: {
        overflow: 'hidden',
    },
    formPaper: {
        margin: 50,
    },
    formRoot: {
        flexGrow: 1,
        marginTop: 30,
    },
    codeViewer: {
        padding: 50,
        paddingLeft: 0,
        paddingBottom: 0,
        margin: 0,
    },
    codeViewErrorDisplay: {
        padding: 20,
        paddingTop: 0,
        backgroundColor: '#C62828',
        color: '#ffffff',
    },
    incompleteTemplateErrorDisplay: {
        color: '#C62828',
    }
};

const editorThemes = [
    'ambiance',
    'chaos',
    'chrome',
    'clouds',
    'clouds_midnight',
    'cobalt',
    'crimson_editor',
    'dawn',
    'dracula',
    'dreamweaver',
    'eclipse',
    'github',
    'gob',
    'gruvbox',
    'idle_fingers',
    'iplastic',
    'katzenmilch',
    'kr_theme',
    'kuroir',
    'merbivore',
    'merbivore_soft',
    'mono_industrial',
    'monokai',
    'pastel_on_dark',
    'solarized_dark',
    'solarized_light',
    'sqlserver',
    'terminal',
    'textmate',
    'tomorrow',
    'tomorrow_night',
    'tomorrow_night_blue',
    'tomorrow_night_bright',
    'tomorrow_night_eighties',
    'twilight',
    'vibrant_ink',
    'xcode'
];
const editorFontSizes = [11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24];

/**
 * Represents the template group definition form
 */
class TemplateGroupForm extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            templateGroup: JSON.parse(JSON.stringify(TemplatesEditorConstants.TEMPLATE_GROUP_SKELETON)),
            codeViewDefinition: '',
            scriptAdditionBins: [],
            isUnsaved: false,
            // UI Related
            isCodeViewEnabled: true,
            showCodeViewError: false,
            codeViewError: '',
            editorSettings: {
                theme: 'tomorrow_night_eighties',
                fontSize: 18,
                wrapEnabled: true,
            },
            showSettingsDialog: false,
            showOpenDialog: false,
            showSaveConfirmation: false,
            saveConfirmationAction: '',
            showIncompleteTemplateDialog: false,
            incompleteTemplateError: '',
            showSnackbar: false,
            snackbarMessage: '',
            snackbarAutoHideDuration: 3500,
        };
        this.addRuleTemplate = this.addRuleTemplate.bind(this);
        this.handleTemplateGroupValueChange = this.handleTemplateGroupValueChange.bind(this);
        this.updateByCode = this.updateByCode.bind(this);
        this.handleNewClick = this.handleNewClick.bind(this);
        this.handleOpenClick = this.handleOpenClick.bind(this);
        this.importFromFile = this.importFromFile.bind(this);
        this.handleSaveClick = this.handleSaveClick.bind(this);
        this.toggleCodeView = this.toggleCodeView.bind(this);
        this.recoverCodeView = this.recoverCodeView.bind(this);
    }

    componentDidMount() {
        this.setState({
            codeViewDefinition: JSON.stringify(this.state.templateGroup, null, 3),
        });
    }

    /**
     * Adds an empty rule template
     */
    addRuleTemplate() {
        const state = this.state;
        state.templateGroup.ruleTemplates.push(
            JSON.parse(JSON.stringify(TemplatesEditorConstants.RULE_TEMPLATE_SKELETON)));
        state.codeViewDefinition = JSON.stringify(state.templateGroup, null, 3);
        this.setState(state);
    }

    /**
     * Updates the rule template with the given index, when a change is made
     * @param index
     * @param ruleTemplate
     */
    onRuleTemplateChange(index, ruleTemplate) {
        const state = this.state;
        state.templateGroup.ruleTemplates[index] = ruleTemplate;
        state.codeViewDefinition = JSON.stringify(state.templateGroup, null, 3);
        this.setState(state);
    }

    /**
     * Updates the script additions bin in the state,
     * when a new variable is added to the script of the rule template - that has the given index
     * @param index
     * @param scriptBin
     */
    onScriptAddition(index, scriptBin) {
        const state = this.state;
        state.scriptAdditionBins[index] = scriptBin;
        this.setState(state);
    }

    /**
     * Removes the rule template that has the given index, from the state
     *
     * @param index
     */
    removeRuleTemplate(index) {
        const state = this.state;
        state.templateGroup.ruleTemplates.splice(index, 1);
        state.codeViewDefinition = JSON.stringify(state.templateGroup, null, 3);
        this.setState(state);
    }

    /**
     * Updates value of a property, that belongs to the current template group
     * @param event
     */
    handleTemplateGroupValueChange(event) {
        const state = this.state;
        state.templateGroup[event.target.name] = event.target.value;
        state.codeViewDefinition = JSON.stringify(state.templateGroup, null, 3);
        this.setState(state);
    }

    /* Code View functions [START] */

    /**
     * Updates the template group definition from the code view, to the code view cache
     * @param content
     */
    updateByCode(content) {
        const state = this.state;
        state.isUnsaved = true;
        state.codeViewDefinition = content;
        try {
            let validJSON = JSON.parse(content);
            try {
                if (this.isSkeletonValid(validJSON)) {
                    this.toggleCodeViewError();
                    state.templateGroup = validJSON;
                }
            } catch (error) {
                this.toggleCodeViewError(error);
            }
        } catch(e) {
            // Absorb and wait until a valid JSON is entered
        }
        this.setState(state);
    }

    /**
     * Validates whether the given templateGroup definition's structure
     * Throws respective error when the structure is invalid
     * @param templateGroupDefinition
     * @returns {boolean}
     */
    isSkeletonValid(templateGroupDefinition) {
        if (!(typeof templateGroupDefinition === 'object' && templateGroupDefinition.constructor === Object)) {
            throw `The template group definition should be wrapped as an object`;
        }
        if (!Object.prototype.hasOwnProperty.call(templateGroupDefinition, 'uuid')) {
            throw `Cannot find 'uuid' for the template group`;
        }
        if (typeof templateGroupDefinition.uuid !== 'string') {
            throw `Expected string for 'uuid', but found: ${typeof templateGroupDefinition.uuid}`;
        }
        if (!Object.prototype.hasOwnProperty.call(templateGroupDefinition, 'name')) {
            throw `Cannot find 'name' for the template group`;
        }
        if (typeof templateGroupDefinition.name !== 'string') {
            throw `Expected string for 'name', but found: ${typeof templateGroupDefinition.name}`;
        }

        // Validate rule templates
        if (!Object.prototype.hasOwnProperty.call(templateGroupDefinition, 'ruleTemplates')) {
            throw `Cannot find 'ruleTemplates' array in the template group`;
        }
        if (!Array.isArray(templateGroupDefinition.ruleTemplates)) {
            throw `Expected array for 'ruleTemplates', but found ${typeof templateGroupDefinition.ruleTemplates}`;
        }
        for (let i = 0; i < templateGroupDefinition.ruleTemplates.length; i++) {
            const ruleTemplateDefinition = templateGroupDefinition.ruleTemplates[i];
            if (!(typeof ruleTemplateDefinition === 'object' && ruleTemplateDefinition.constructor === Object)) {
                throw `ruleTemplates[${i}] should be wrapped as an object`;
            }
            if (!Object.prototype.hasOwnProperty.call(templateGroupDefinition.ruleTemplates[i], 'uuid')) {
                throw `Cannot find 'uuid' for ruleTemplates[${i}]`;
            }
            if (typeof templateGroupDefinition.ruleTemplates[i].uuid !== 'string') {
                throw `Expected string for 'uuid' of ruleTemplates[${i}], ` +
                `but found: ${typeof templateGroupDefinition.ruleTemplates[i].uuid}`;
            }
            if (!Object.prototype.hasOwnProperty.call(templateGroupDefinition.ruleTemplates[i], 'name')) {
                throw `Cannot find 'name' for rule template '${ruleTemplateDefinition.uuid}'`;
            }
            if (typeof templateGroupDefinition.ruleTemplates[i].name !== 'string') {
                throw `Expected string for 'name' of rule template '${ruleTemplateDefinition.uuid}', ` +
                `but found: ${typeof templateGroupDefinition.ruleTemplates[i].name}`;
            }
            if (!Object.prototype.hasOwnProperty.call(templateGroupDefinition.ruleTemplates[i], 'type')) {
                throw `Cannot find 'type' for rule template '${ruleTemplateDefinition.uuid}'`;
            }
            if (typeof templateGroupDefinition.ruleTemplates[i].type !== 'string') {
                throw `Expected string for 'type' of rule template '${ruleTemplateDefinition.uuid}', ` +
                `but found: ${typeof templateGroupDefinition.ruleTemplates[i].type}`;
            }
            if (!((templateGroupDefinition.ruleTemplates[i].type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) ||
                    (templateGroupDefinition.ruleTemplates[i].type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_INPUT) ||
                    (templateGroupDefinition.ruleTemplates[i].type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_OUTPUT))) {
                throw `'type' of rule template '${ruleTemplateDefinition.uuid}' should be ` +
                `${BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE}/` +
                `${BusinessRulesConstants.RULE_TEMPLATE_TYPE_INPUT}/` +
                `${BusinessRulesConstants.RULE_TEMPLATE_TYPE_OUTPUT}`;
            }
            if (!Object.prototype.hasOwnProperty.call(templateGroupDefinition.ruleTemplates[i], 'instanceCount')) {
                throw `Cannot find 'instanceCount' for rule template '${ruleTemplateDefinition.uuid}'`;
            }
            if (typeof templateGroupDefinition.ruleTemplates[i].instanceCount !== 'string') {
                throw `Expected string for 'instanceCount' of rule template '${ruleTemplateDefinition.uuid}', ` +
                `but found: ${typeof templateGroupDefinition.ruleTemplates[i].instanceCount}`;
            }
            if (!((templateGroupDefinition.ruleTemplates[i].instanceCount ===
                    BusinessRulesConstants.RULE_TEMPLATE_INSTANCE_COUNT_ONE) ||
                    (templateGroupDefinition.ruleTemplates[i].instanceCount ===
                        BusinessRulesConstants.RULE_TEMPLATE_INSTANCE_COUNT_MANY))) {
                throw `'instanceCount' of rule template '${ruleTemplateDefinition.uuid}' should be ` +
                `${BusinessRulesConstants.RULE_TEMPLATE_INSTANCE_COUNT_ONE} / ` +
                `${BusinessRulesConstants.RULE_TEMPLATE_INSTANCE_COUNT_MANY}`;
            }

            // Validate templates
            if (!Object.prototype.hasOwnProperty.call(templateGroupDefinition.ruleTemplates[i], 'templates')) {
                throw `Cannot find 'templates' array for rule template '${ruleTemplateDefinition.uuid}'`;
            }
            if (!Array.isArray(templateGroupDefinition.ruleTemplates[i].templates)) {
                throw `Expected array for 'templates' of rule template '${ruleTemplateDefinition.uuid}', ` +
                `but found: ${typeof templateGroupDefinition.ruleTemplates[i].templates}`;
            }
            for(let t = 0; t < templateGroupDefinition.ruleTemplates[i].templates.length; t++)  {
                if (!(typeof templateGroupDefinition.ruleTemplates[i].templates[t] === 'object' &&
                        templateGroupDefinition.ruleTemplates[i].templates[t].constructor === Object)) {
                    throw `templates[${t}] of rule template '${ruleTemplateDefinition.uuid}'` +
                    ` should be wrapped as an object`;
                }
                if (!Object.prototype.hasOwnProperty.call(
                        templateGroupDefinition.ruleTemplates[i].templates[t], 'type')) {
                    throw `templates[${t}] of rule template '${ruleTemplateDefinition.uuid}' ` +
                    `should have 'type' - 'siddhiApp'`;
                }
                if (typeof templateGroupDefinition.ruleTemplates[i].templates[t].type !== 'string') {
                    throw `Expected string for 'type' of templates[${t}] of ` +
                    `rule template '${ruleTemplateDefinition.uuid}', ` +
                    `but found: ${typeof templateGroupDefinition.ruleTemplates[i].templates[t].type}`;
                }
                if (templateGroupDefinition.ruleTemplates[i].templates[t].type !== 'siddhiApp') {
                    throw `templates[${t}] of rule template '${ruleTemplateDefinition.uuid}' ` +
                    `should have type 'siddhiApp'`;
                }
                if (!Object.prototype.hasOwnProperty.call(
                        templateGroupDefinition.ruleTemplates[i].templates[t], 'content')) {
                    throw `Cannot find 'content' for template[${t}] of ` +
                    `rule template '${ruleTemplateDefinition.uuid}'`;
                }
                if (typeof templateGroupDefinition.ruleTemplates[i].templates[t].content !== 'string') {
                    throw `Expected string for 'content' in templates[${t}] of ` +
                    `rule template '${ruleTemplateDefinition.uuid}', ` +
                    `but found: ${typeof templateGroupDefinition.ruleTemplates[i].templates[t].content}`;
                }

                // Validate exposed stream definition for input / output rule template
                if (templateGroupDefinition.ruleTemplates[i].type ===
                    BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) {
                    if (Object.prototype.hasOwnProperty.call(
                            templateGroupDefinition.ruleTemplates[i].templates[t], 'exposedStreamDefinition')) {
                        throw `templates[${t}] of rule template '${ruleTemplateDefinition.uuid}' ` +
                        `cannot have an exposedStreamDefinition`;
                    }
                } else if (templateGroupDefinition.ruleTemplates[i].type !== ''){
                    if (!Object.prototype.hasOwnProperty.call(
                            templateGroupDefinition.ruleTemplates[i].templates[t], 'exposedStreamDefinition')) {
                        throw `templates[${t}] of rule template '${ruleTemplateDefinition.uuid}' ` +
                        `should have an exposedStreamDefinition`;
                    }
                    if (typeof templateGroupDefinition.ruleTemplates[i].templates[t].exposedStreamDefinition
                        !== 'string') {
                        throw `Expected string for 'exposedStreamDefinition' in ` +
                        `templates[${t}] of rule template '${ruleTemplateDefinition.uuid}', but found: ` +
                        `${typeof templateGroupDefinition.ruleTemplates[i].templates[t].exposedStreamDefinition}`;
                    }
                }
            }

            // Validate properties
            if (!Object.prototype.hasOwnProperty.call(templateGroupDefinition.ruleTemplates[i], 'properties')) {
                throw `Cannot find 'properties' in rule template '${ruleTemplateDefinition.uuid}'`;
            }
            if (!(typeof templateGroupDefinition.ruleTemplates[i].properties === 'object' &&
                    templateGroupDefinition.ruleTemplates[i].properties.constructor === Object)) {
                let foundType = typeof templateGroupDefinition.ruleTemplates[i].properties;
                if (Array.isArray(templateGroupDefinition.ruleTemplates[i].properties)) {
                    foundType = 'array';
                }
                throw `Expected object for 'properties' of rule template '${ruleTemplateDefinition.uuid}', ` +
                `but found: ${foundType}`;
            }
            for (const propertyKey in templateGroupDefinition.ruleTemplates[i].properties) {
                if (!(typeof templateGroupDefinition.ruleTemplates[i].properties[propertyKey] === 'object' &&
                        templateGroupDefinition.ruleTemplates[i].properties[propertyKey].constructor === Object)) {
                    let foundType = typeof templateGroupDefinition.ruleTemplates[i].properties[propertyKey];
                    if (Array.isArray(templateGroupDefinition.ruleTemplates[i].properties[propertyKey])) {
                        foundType = 'array';
                    }
                    throw `Expected object for property '${propertyKey}' of ` +
                    `rule template '${ruleTemplateDefinition.uuid}', but found: ${foundType}`;
                }
                if (!Object.prototype.hasOwnProperty.call(templateGroupDefinition.ruleTemplates[i].
                        properties[propertyKey], 'fieldName')) {
                    throw `Cannot find 'fieldName' for property '${propertyKey}' of ` +
                    `rule template '${ruleTemplateDefinition.uuid}'`;
                }
                if (typeof templateGroupDefinition.ruleTemplates[i].properties[propertyKey].fieldName !== 'string') {
                    throw `Expected string for 'fieldName' of property '${propertyKey}' of ` +
                    `rule template '${ruleTemplateDefinition.uuid}', ` +
                    `but found: ` +
                    `${typeof templateGroupDefinition.ruleTemplates[i].properties[propertyKey].fieldName}`;
                }
                if (!Object.prototype.hasOwnProperty.call(templateGroupDefinition.ruleTemplates[i]
                        .properties[propertyKey], 'description')) {
                    throw `Cannot find 'description' for property '${propertyKey}' of ` +
                    `rule template '${ruleTemplateDefinition.uuid}'`;
                }
                if (typeof templateGroupDefinition.ruleTemplates[i].properties[propertyKey].description !== 'string') {
                    throw `Expected string for 'description' of property '${propertyKey}' of ` +
                    `rule template '${ruleTemplateDefinition.uuid}', ` +
                    `but found: ` +
                    `${typeof templateGroupDefinition.ruleTemplates[i].properties[propertyKey].description}`;
                }
                if (!Object.prototype.hasOwnProperty.call(templateGroupDefinition.ruleTemplates[i]
                        .properties[propertyKey], 'defaultValue')) {
                    throw `Cannot find 'defaultValue' for property '${propertyKey}' of ` +
                    `rule template '${ruleTemplateDefinition.uuid}'`;
                }
                if (typeof templateGroupDefinition.ruleTemplates[i].properties[propertyKey].defaultValue !== 'string') {
                    throw `Expected string for 'defaultValue' of property '${propertyKey}' of ` +
                    `rule template '${ruleTemplateDefinition.uuid}', ` +
                    `but found: ` +
                    `${typeof templateGroupDefinition.ruleTemplates[i].properties[propertyKey].defaultValue}`;
                }
                // If options are there, validate them
                if (Object.prototype.hasOwnProperty.call(
                    templateGroupDefinition.ruleTemplates[i].properties[propertyKey], 'options')) {
                    if (!Array.isArray(templateGroupDefinition.ruleTemplates[i].properties[propertyKey].options)) {
                        throw `Expected array for 'options' of property '${propertyKey}' of ` +
                        `rule template '${ruleTemplateDefinition.uuid}', ` +
                        `but found: ` +
                        `${typeof templateGroupDefinition.ruleTemplates[i].properties[propertyKey].options}`;
                    }
                    for (let o = 0;
                         o < templateGroupDefinition.ruleTemplates[i].properties[propertyKey].options.length; o++) {
                        if (typeof templateGroupDefinition.ruleTemplates[i].properties[propertyKey].options[o] !==
                            'string') {
                            throw `Expected string for options[${o}] in property '${propertyKey}' of ` +
                            `rule template '${ruleTemplateDefinition.uuid}', ` +
                            `but found: ` +
                            `${typeof templateGroupDefinition.ruleTemplates[i].properties[propertyKey].options[o]}`;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Removes unneeded elements and returns the definition to be saved
     * @param definition
     */
    prepareTemplateGroupDefinition(definition) {
        let configuration = JSON.parse(JSON.stringify(definition));
        if (!Object.prototype.hasOwnProperty.call(configuration, 'description') || configuration.description === '') {
            delete configuration.description;
        }
        for (let i = 0; i < configuration.ruleTemplates.length; i++) {
            if (!Object.prototype.hasOwnProperty.call(configuration.ruleTemplates[i], 'description') ||
                configuration.ruleTemplates[i].description === '') {
                delete configuration.ruleTemplates[i].description;
            }
            // Remove exposed stream definition when rule template's type is 'template'
            if (configuration.ruleTemplates[i].type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE &&
                Object.prototype.hasOwnProperty.call(configuration.ruleTemplates[i].templates[0],
                    'exposedStreamDefinition')) {
                delete configuration.ruleTemplates[i].templates[0].exposedStreamDefinition;
            }
        }
        return configuration;
    }

    /**
     * Enables code view error when error is given as a parameter, or disables when no error has been given
     * @param error
     */
    toggleCodeViewError(error) {
        let state = this.state;
        state.showCodeViewError = !!error;
        if (error) {
            state.codeViewError = error;
        } else {
            state.codeViewError = '';
        }
        this.setState(state);
    }

    /**
     * Updates code view cache with the content from template group definition
     */
    recoverCodeView() {
        this.toggleCodeViewError();
        this.setState({ codeViewDefinition: JSON.stringify(this.state.templateGroup, null, 3) });
    }

    /**
     * Updates editor appearance settings for the given element, with the given value
     * @param element
     * @param value
     */
    updateEditorSettings(element, value) {
        const state = this.state;
        state.editorSettings[element] = value;
        this.setState(state);
    }

    /* Code View functions [END] */

    /* Header functions [START] */

    /**
     * Handles onClick of the New button
     */
    handleNewClick() {
        if (!this.state.isUnsaved) {
            this.setNewTemplateGroup();
        } else {
            this.setState({
                showSaveConfirmation: true,
                saveConfirmationAction: 'new',
            });
        }
    }

    /**
     * Sets an empty template group skeleton to the state
     */
    setNewTemplateGroup() {
        this.setState({
            templateGroup: JSON.parse(JSON.stringify(TemplatesEditorConstants.TEMPLATE_GROUP_SKELETON)),
            codeViewDefinition: JSON.stringify(
                JSON.parse(JSON.stringify(TemplatesEditorConstants.TEMPLATE_GROUP_SKELETON)), null, 3)
        });
    }

    /**
     * Handles onClick of the Open button
     */
    handleOpenClick() {
        if (!this.state.isUnsaved) {
            this.setState({
                showOpenDialog: true,
            });
        } else {
            this.setState({
                showSaveConfirmation: true,
                saveConfirmationAction: 'open',
            });
        }
    }

    /**
     * Opens a template group definition from file
     */
    importFromFile() {
        let fileToLoad = this.refs.fileLoadInput.files.item(0);
        let fileReader = new FileReader();
        let fileError;
        fileReader.onload = fileLoadedEvent => {
            let textFromFileLoaded = fileLoadedEvent.target.result;
            try {
                this.handleFileImport(textFromFileLoaded);
            } catch (error) {
                fileError = error;
            }
            this.setState({ showOpenDialog: false });
            if (fileError) {
                this.toggleSnackbar(fileError);
            }
        };
        fileReader.readAsText(fileToLoad, "UTF-8")
    }

    /**
     * Sets the content from loaded file to the state
     * @param definition
     */
    handleFileImport(definition) {
        let templateGroup;
        try {
            templateGroup = JSON.parse(definition);
        } catch (error) {
            throw 'Invalid JSON for template group definition';
        }
        if (Object.hasOwnProperty.call(templateGroup, 'templateGroup')) {
            if (this.isSkeletonValid(templateGroup.templateGroup)) {
                this.setState({
                    templateGroup: templateGroup.templateGroup,
                    codeViewDefinition: JSON.stringify(templateGroup.templateGroup, null, 3),
                    isUnsaved: false,
                });
            }
        } else {
            throw `Cannot find wrapper 'templateGroup'`;
        }
    }

    /**
     * Handles onClick of the save button
     */
    handleSaveClick() {
        try {
            if (this.isComplete(this.state.templateGroup)) {
                this.saveTemplate(this.state.templateGroup);
            }
        } catch (error) {
            this.setState({
                incompleteTemplateError: error,
                showIncompleteTemplateDialog: true,
            });
        }
    }

    /**
     * Downloads the template group definition
     * @param templateGroupDefinition
     */
    saveTemplate(templateGroupDefinition) {
        let saveableTemplate = { "templateGroup": this.prepareTemplateGroupDefinition(templateGroupDefinition) };
        downloadFile(JSON.stringify(
            saveableTemplate, null, 3), `${templateGroupDefinition.uuid}.json`);
        this.setState({ isUnsaved: false });
    }

    /**
     * Handles the respective action after save confirmation, depending on the given choice
     * @param choice
     */
    handleAfterSaveConfirmation(choice) {
        switch (choice) {
            case 'yes':
                this.handleSaveClick();
            case 'no':
                // Do specific action (New / Open)
                if (this.state.saveConfirmationAction === 'open') {
                    this.setState({ showOpenDialog: true });
                } else {
                    this.setNewTemplateGroup();
                }
                this.setState({ isUnsaved: false });
            default:
                this.setState({ showSaveConfirmation: false });
        }
    }

    /**
     * Toggles code view
     */
    toggleCodeView() {
        const state = this.state;
        state.isCodeViewEnabled = !state.isCodeViewEnabled;
        this.setState(state);
    }

    /* Header functions [END]*/

    /**
     * Validates whether the given template group definition is complete.
     * Throws respective error when not complete
     * @param definition
     * @returns {boolean}
     */
    isComplete(definition) {
        if (definition.uuid === '') {
            throw `'uuid' cannot be empty for the template group`;
        }
        if (definition.name === '') {
            throw `'name' cannot be empty for the template group`;
        }
        if (definition.ruleTemplates.length === 0) {
            throw 'There should be at least one rule template';
        }

        // Validate rule templates
        for (let i = 0; i < definition.ruleTemplates.length; i++) {
            const ruleTemplate = definition.ruleTemplates[i];
            if (ruleTemplate.uuid === '') {
                throw `'uuid' cannot be empty for ruleTemplates[${i}]`;
            }
            if (ruleTemplate.name === '') {
                throw `'name' cannot be empty for rule template '${ruleTemplate.uuid}'`;
            }
            if (ruleTemplate.type === '') {
                throw `'type' cannot be empty for rule template '${ruleTemplate.uuid}'`;
            }
            if (!((ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) ||
                    (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_INPUT) ||
                    (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_OUTPUT))) {
                throw `'type' should be 'template' / 'input' / 'output' for rule template '${ruleTemplate.uuid}'`;
            }
            if (ruleTemplate.instanceCount === '') {
                throw `'instanceCount' cannot be blank for rule template '${ruleTemplate.uuid}'`;
            }
            if (!((ruleTemplate.instanceCount === BusinessRulesConstants.RULE_TEMPLATE_INSTANCE_COUNT_ONE) ||
                    (ruleTemplate.instanceCount === BusinessRulesConstants.RULE_TEMPLATE_INSTANCE_COUNT_MANY))) {
                throw `'instanceCount' should be one or many for rule template '${ruleTemplate.uuid}'`;
            }

            // Validate templates
            if (ruleTemplate.templates.length === 0) {
                throw `There should be at least one template under rule template '${ruleTemplate.uuid}'`;
            }
            if ((ruleTemplate.templates.type !== BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) &&
                (ruleTemplate.templates.length !== 1)) {
                throw `There can be only one template for ${ruleTemplate.type} ` +
                `rule template '${ruleTemplate.uuid}`;
            }
            for (let j = 0; j < ruleTemplate.templates.length; j++) {
                if (ruleTemplate.templates[j].content === '') {
                    throw `Cannot find valid content for templates[${j}] in rule template '${ruleTemplate.uuid}'`;
                }
                // Validate templates for input / output rule templates
                if (ruleTemplate.type !== BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE){
                    if (!Object.prototype.hasOwnProperty
                            .call(ruleTemplate.templates[j], 'exposedStreamDefinition')) {
                        throw `Cannot find 'exposedStreamDefinition' for templates[${j}]` +
                        `rule template '${ruleTemplate.uuid}'`;
                    }
                    if (ruleTemplate.templates[j].exposedStreamDefinition === '') {
                        throw `'exposedStreamDefinition' cannot be empty for rule template '${ruleTemplate.uuid}'`;
                    }
                    if (TemplatesEditorUtilityFunctions.getRegexMatches(
                            ruleTemplate.templates[j].exposedStreamDefinition,
                            TemplatesEditorConstants.STREAM_DEFINITION_REGEX).length !== 1) {
                        throw `Cannot find a valid stream definition in 'exposedStreamDefinition' of rule template` +
                        ` rule template '${ruleTemplate.uuid}'`;
                    }
                }
                if ((ruleTemplate.type !== BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) && (j === 0)) {
                    break;
                }
            }

            // Validate properties
            if (TemplatesEditorUtilityFunctions.isEmpty(ruleTemplate.properties)) {
                throw 'Cannot find properties';
            }
            for (const property in ruleTemplate.properties) {
                if (ruleTemplate.properties[property].fieldName === '') {
                    throw `Cannot find 'fieldName' for property '${property}' of ` +
                    `rule template '${ruleTemplate.uuid}'`;
                }
                if (ruleTemplate.properties[property].defaultValue === '') {
                    throw `Cannot find 'defaultValue' for property '${property}' of ` +
                    `rule template '${ruleTemplate.uuid}'`;
                }
                if (Object.prototype.hasOwnProperty.call(ruleTemplate.properties[property], 'options')) {
                    // Options are present
                    if (ruleTemplate.properties[property].options.length < 2) {
                        throw `There should be at least two options for property '${property}' ` +
                        `of rule template '${ruleTemplate.uuid}'`;
                    }
                    if (ruleTemplate.properties[property].options.
                        indexOf(ruleTemplate.properties[property].defaultValue) === -1) {
                        throw `'defaultValue' for property '${property}' of ` +
                        `rule template '${ruleTemplate.uuid}' should be one of its options`;
                    }
                }
            }
            // Validate templated elements from script against properties
            let scriptTemplatedElements = TemplatesEditorUtilityFunctions.getRegexMatches(
                ruleTemplate.script, TemplatesEditorConstants.TEMPLATED_ELEMENT_REGEX);
            for (let t = 0; t < scriptTemplatedElements.length; t++) {
                if (!Object.prototype.hasOwnProperty.call(ruleTemplate.properties, scriptTemplatedElements[t])) {
                    throw `Cannot find property for '${scriptTemplatedElements[t]}' from script, ` +
                    `of rule template [${i}]`;
                }
            }
            // Validate whether templated elements present in both the script & properties
            for (let t = 0; t < ruleTemplate.templates.length; t++ ) {
                const template = ruleTemplate.templates[t];
                let templatedElements = TemplatesEditorUtilityFunctions.getRegexMatches(template.content,
                    TemplatesEditorConstants.TEMPLATED_ELEMENT_REGEX);
                console.log(templatedElements);
                for (let t = 0; t < templatedElements.length; t++) {
                    if ((this.state.scriptAdditionBins.indexOf(templatedElements[t]) > -1)) {
                        if (Object.prototype.hasOwnProperty.call(ruleTemplate.properties, templatedElements[t])) {
                            // Templated element exists in both script & properties
                            throw `'${templatedElements[t]}' has been already referred in script.` +
                            ` It should be removed from properties of rule template '${ruleTemplate.uuid}'`;
                        }
                    }
                }
                if ((ruleTemplate.type !== BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) && (t === 0)) {
                    break;
                }
            }
        }
        return true;
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
            <MuiThemeProvider theme={theme}>
                <div>
                    <div style={{position: 'absolute', top: 0, bottom: 0, left: 0, right: 0, overflowX: 'hidden'}}>
                        <Header
                            templateGroupDefinition={this.state.templateGroup}
                            scriptAdditionBins={this.state.scriptAdditionBins}
                            isUnsaved={this.state.isUnsaved}
                            isCodeViewEnabled={this.state.isCodeViewEnabled}
                            onNewClick={this.handleNewClick}
                            onOpenClick={this.handleOpenClick}
                            onSaveClick={this.handleSaveClick}
                            onSettingsClick={() => this.setState({ showSettingsDialog: true })}
                            onCodeViewToggle={this.toggleCodeView}
                            style={{ flex: 1}}
                        />
                        {(this.state.showCodeViewError && this.state.isCodeViewEnabled) ?
                            (<div style={styles.codeViewErrorDisplay}>
                                <Toolbar style={{ padding: 0, margin: 0}}>
                                    <ErrorIcon />
                                    <Typography type="body2" color="inherit" style={{ flex: 1 }}>
                                        &nbsp;&nbsp;&nbsp;Error in code view.
                                        Design view & saving the template rely on the latest valid configuration
                                    </Typography>
                                    <Tooltip title="Recover from design view">
                                        <IconButton
                                            color="contrast"
                                            onClick={this.recoverCodeView}
                                        >
                                            <CachedIcon/>
                                        </IconButton>
                                    </Tooltip>
                                </Toolbar>
                                <Typography
                                    type="subheading"
                                    color="inherit"
                                >
                                    {this.state.codeViewError}
                                </Typography>
                            </div>) :
                            (null)}
                        <div style={{ position: 'relative', height: '100%', width: '100%', margin: 0 }}>
                            <div style={{ position: 'relative', float: 'left', width: (this.state.isCodeViewEnabled) ? '50%' : '100%', height: '100%', overflowY: 'auto' }}>
                                <div>
                                    <div style={styles.formPaper}>
                                        <div>
                                            <Paper>
                                                <div style={styles.formPaper}>
                                                    <TextField
                                                        fullWidth
                                                        id='uuid'
                                                        name='uuid'
                                                        label='UUID'
                                                        value={this.state.templateGroup.uuid}
                                                        helperText='Used to identify the template group'
                                                        margin="normal"
                                                        onChange={this.handleTemplateGroupValueChange}
                                                    />
                                                    <TextField
                                                        fullWidth
                                                        id='name'
                                                        name='name'
                                                        label='Name'
                                                        value={this.state.templateGroup.name}
                                                        helperText='Used for representing the template group'
                                                        margin="normal"
                                                        onChange={this.handleTemplateGroupValueChange}
                                                    />
                                                    <TextField
                                                        fullWidth
                                                        id='description'
                                                        name='description'
                                                        label='Description'
                                                        value={this.state.templateGroup.description ?
                                                            this.state.templateGroup.description : ''}
                                                        helperText='Short description of what this template group does'
                                                        margin='normal'
                                                        onChange={this.handleTemplateGroupValueChange}
                                                    />
                                                    <br/>
                                                    <br/>
                                                    <br/>
                                                    <Typography type="title">
                                                        Rule Templates
                                                    </Typography>
                                                    <br/>
                                                    {this.state.templateGroup.ruleTemplates.map((ruleTemplate, index) =>
                                                        (<div key={index}>
                                                            <RuleTemplate
                                                                configuration={ruleTemplate}
                                                                removeRuleTemplate={() =>
                                                                    this.removeRuleTemplate(index)}
                                                                editorSettings={this.state.editorSettings}
                                                                onChange={config =>
                                                                    this.onRuleTemplateChange(index, config)}
                                                                onScriptAddition={scriptBin =>
                                                                    this.onScriptAddition(index, scriptBin)}
                                                            />
                                                            <br />
                                                        </div>))}
                                                    <br/>
                                                    <div>
                                                        <IconButton
                                                            color='primary'
                                                            style={{ backgroundColor: '#EF6C00', color: 'white' }}
                                                            aria-label='Add'
                                                            onClick={this.addRuleTemplate}
                                                        >
                                                            <AddIcon />
                                                        </IconButton>
                                                    </div>
                                                    <br/>
                                                    <br/>
                                                </div>
                                            </Paper>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            {(this.state.isCodeViewEnabled) ?
                                (<div style={{ position: 'relative', float: 'left', width: '50%', height: '100%' }}>
                                    <AceEditor
                                        mode='json'
                                        name='templateGroup'
                                        theme={this.state.editorSettings.theme}
                                        fontSize={this.state.editorSettings.fontSize}
                                        wrapEnabled={this.state.editorSettings.wrapEnabled}
                                        onChange={this.updateByCode}
                                        value={this.state.codeViewDefinition}
                                        showPrintMargin={false}
                                        width='100%'
                                        height='100%'
                                        tabSize={3}
                                        useSoftTabs='true'
                                        editorProps={{
                                            $blockScrolling: Infinity,
                                            "display_indent_guides": true,
                                            folding: "markbeginandend"
                                        }}
                                        setOptions={{
                                            cursorStyle: "smooth",
                                            wrapBehavioursEnabled: true
                                        }}
                                    />
                                </div>) : (null)}
                        </div>
                    </div>
                    <Dialog
                        open={this.state.showSettingsDialog}
                        onClose={() => this.setState({ showSettingsDialog: false })}
                        aria-labelledby="alert-dialog-title"
                        aria-describedby="alert-dialog-description"
                    >
                        <DialogTitle id="alert-dialog-title">{"Editor Appearance Settings"}</DialogTitle>
                        <DialogContent style={{ width: 450 }}>
                            <br />
                            <Typography type="body2">Theme</Typography>
                            <Select
                                value={this.state.editorSettings.theme}
                                onChange={e => this.updateEditorSettings('theme', e.target.value)}
                                input={<Input id='editorTheme' />}
                                fullWidth
                            >
                                {editorThemes.map((theme, index) =>
                                    (<MenuItem key={index} name={index} value={theme}>{theme}</MenuItem>))}
                            </Select>
                            <br />
                            <br />
                            <Typography type="body2">Font Size</Typography>
                            <Select
                                value={this.state.editorSettings.fontSize}
                                onChange={e => this.updateEditorSettings('fontSize', e.target.value)}
                                input={<Input id='editorTheme' />}
                                fullWidth
                            >
                                {editorFontSizes.map((fontSize, index) =>
                                    (<MenuItem key={index} name={index} value={fontSize}>{fontSize}</MenuItem>))}
                            </Select>
                            <br />
                            <br />
                            <FormControl component="fieldset">
                                <FormGroup>
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={this.state.editorSettings.wrapEnabled}
                                                onChange={(e, c) => this.updateEditorSettings('wrapEnabled', c)}
                                                value="codeWrap"
                                            />
                                        }
                                        label="Code wrap"
                                    />
                                </FormGroup>
                            </FormControl>
                        </DialogContent>
                        <DialogActions>
                            <Button onClick={() => this.setState({ showSettingsDialog: false })} color="default" autoFocus>
                                Close
                            </Button>
                        </DialogActions>
                    </Dialog>
                    <Dialog
                        open={this.state.showOpenDialog}
                        onClose={() => this.setState({ showOpenDialog: false })}
                        aria-labelledby="open-dialog-title"
                        aria-describedby="open-dialog-description"
                    >
                        <DialogContent style={{ paddingBottom: 10, width: 500}}>
                            <div>
                                <Typography type="title">Open a template file</Typography>
                                <br />
                                <input type="file" ref="fileLoadInput"></input>
                            </div>
                            <DialogActions>
                                <Button onClick={() => this.setState({ showOpenDialog: false })} color="default">
                                    Cancel
                                </Button>
                                <Button onClick={this.importFromFile} color="primary" autoFocus>
                                    Load
                                </Button>
                            </DialogActions>
                        </DialogContent>
                    </Dialog>
                    <Dialog
                        open={this.state.showSaveConfirmation}
                        onClose={() => this.setState({ showSaveConfirmation: false })}
                        aria-labelledby="save-confirmation-dialog-title"
                        aria-describedby="save-confirmation-dialog-description"
                    >
                        <DialogContent style={{ paddingBottom: 10, width: 500}}>
                            <DialogTitle id="alert-dialog-title">
                                Some changes are not saved
                            </DialogTitle>
                            <DialogContent>
                                <Typography type='subheading'>
                                    Do you want to save the changes?
                                </Typography>
                            </DialogContent>
                            <DialogActions>
                                <Button onClick={() => this.handleAfterSaveConfirmation('yes')} color="primary" autoFocus>
                                    Yes
                                </Button>
                                <Button onClick={() => this.handleAfterSaveConfirmation('no')} color="default">
                                    No
                                </Button>
                                <Button onClick={() => this.handleAfterSaveConfirmation('cancel')} color="default">
                                    Cancel
                                </Button>
                            </DialogActions>
                        </DialogContent>
                    </Dialog>
                    <Dialog
                        open={this.state.showIncompleteTemplateDialog}
                        onClose={() => this.setState({ showIncompleteTemplateDialog: false })}
                        aria-labelledby="incomplete-template-dialog-title"
                        aria-describedby="incomplete-template-dialog-description"
                    >
                        <DialogContent style={{ paddingBottom: 10, width: 500}}>
                            <DialogTitle id="alert-dialog-title">
                                Incomplete Template
                            </DialogTitle>
                            <DialogContent>
                                <div style={styles.incompleteTemplateErrorDisplay}>
                                    <Typography type='subheading' color='inherit'>
                                        {this.state.incompleteTemplateError}
                                    </Typography>
                                </div>
                                <Typography type='subheading'>
                                    Do you want still want to save it for editing later?
                                </Typography>
                            </DialogContent>
                            <DialogActions>
                                <Button
                                    onClick={() => {
                                        this.saveTemplate(this.state.templateGroup);
                                        this.setState({ showIncompleteTemplateDialog: false })
                                    }}
                                    color="primary"
                                    autoFocus
                                >
                                    Save
                                </Button>
                                <Button
                                    onClick={() => this.setState({ showIncompleteTemplateDialog: false })}
                                    color="default"
                                >
                                    Cancel
                                </Button>
                            </DialogActions>
                        </DialogContent>
                    </Dialog>
                    <Snackbar
                        autoHideDuration={this.state.snackbarAutoHideDuration}
                        open={this.state.showSnackbar}
                        onRequestClose={() => this.toggleSnackbar()}
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
            </MuiThemeProvider>
        );
    }
}

export default TemplateGroupForm;