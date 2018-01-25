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

import React from 'react';
import downloadFile from 'react-file-download';
import _ from 'lodash';
// Material UI Components
import Button from 'material-ui/Button';
import Select from 'material-ui/Select';
import Collapse from 'material-ui/transitions/Collapse';
import { MenuItem } from 'material-ui/Menu';
import Input from 'material-ui/Input';
import { FormControl, FormControlLabel, FormGroup } from 'material-ui/Form';
import Checkbox from 'material-ui/Checkbox';
import ErrorIcon from 'material-ui-icons/Error';
import Tooltip from 'material-ui/Tooltip';
import Typography from 'material-ui/Typography';
import CachedIcon from 'material-ui-icons/Cached';
import { IconButton } from 'material-ui';
import Toolbar from 'material-ui/Toolbar';
import Snackbar from 'material-ui/Snackbar';
import Slide from 'material-ui/transitions/Slide';
import Dialog, { DialogActions, DialogContent, DialogTitle } from 'material-ui/Dialog';
// App Components
import Header from './Header';
import DesignView from './DesignView';
import CodeView from './CodeView';
// App Constants
import TemplateEditorConstants from '../constants/TemplateEditorConstants';
// App Utils
import TemplateEditorUtilityFunctions from '../utils/TemplateEditorUtilityFunctions';
// Custom theme
import { createMuiTheme, MuiThemeProvider } from 'material-ui/styles';
import { Orange } from '../theme/TemplateEditorColors';

const theme = createMuiTheme({
    palette: {
        primary: Orange,
    },
});

/**
 * Styles related to this component
 */
const styles = {
    rootContainer: {
        position: 'absolute',
        top: 0,
        bottom: 0,
        left: 0,
        right: 0,
        overflowX: 'hidden'
    },
    codeViewErrorDisplay: {
        padding: 20,
        paddingTop: 0,
        backgroundColor: '#C62828',
        color: '#ffffff',
    },
    contentContainer: {
        position: 'relative',
        height: '100%',
        width: '100%',
        margin: 0
    },
    codeViewContainer: {
        position: 'relative',
        float: 'left',
        width: '50%',
        height: '100%'
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
 * Represents the template editor
 */
class TemplateEditor extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            templateGroup: JSON.parse(JSON.stringify(TemplateEditorConstants.TEMPLATE_GROUP_SKELETON)),
            codeViewDefinition: '',
            scriptAdditionBins: [[]], // Holds names of variables added to the script of rule templates
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
            saveConfirmationAction: '', // The action to be executed after save confirmation
            showIncompleteTemplateDialog: false,
            incompleteTemplateError: '',
            showSnackbar: false,
            snackbarMessage: '',
            snackbarAutoHideDuration: 3500
        };
        this.handleTemplateGroupValueChange = this.handleTemplateGroupValueChange.bind(this);
        this.onRuleTemplateChange = this.onRuleTemplateChange.bind(this);
        this.onScriptAddition = this.onScriptAddition.bind(this);
        this.addRuleTemplate = this.addRuleTemplate.bind(this);
        this.removeRuleTemplate = this.removeRuleTemplate.bind(this);
        this.updateByCode = _.debounce(this.updateByCode.bind(this), 900);
        this.handleNewClick = this.handleNewClick.bind(this);
        this.handleOpenClick = this.handleOpenClick.bind(this);
        this.importFromFile = this.importFromFile.bind(this);
        this.handleSaveClick = this.handleSaveClick.bind(this);
        this.toggleCodeView = this.toggleCodeView.bind(this);
        this.recoverCodeView = this.recoverCodeView.bind(this);
    }

    componentDidMount() {
        this.setState({
            codeViewDefinition: JSON.stringify(this.state.templateGroup, null, 3)
        });
    }

    /**
     * Updates value of a property, that belongs to the current template group
     * @param name
     * @param value
     */
    handleTemplateGroupValueChange(name, value) {
        let state = this.state;
        state.isUnsaved = true;
        state.templateGroup[name] = value;
        state.codeViewDefinition = JSON.stringify(state.templateGroup, null, 3);
        this.setState(state);
    }

    /**
     * Adds a rule template to the template group
     */
    addRuleTemplate() {
        const state = this.state;
        state.isUnsaved = true;
        state.templateGroup.ruleTemplates.push(
            JSON.parse(JSON.stringify(TemplateEditorConstants.TEMPLATE_GROUP_SKELETON.ruleTemplates[0])));
        state.scriptAdditionBins.push([]);
        state.codeViewDefinition = JSON.stringify(state.templateGroup, null, 3);
        this.setState(state);
    }

    /**
     * Updates the content of the rule template with the given index, when a change is made
     * @param index
     * @param ruleTemplate
     */
    onRuleTemplateChange(index, ruleTemplate) {
        const state = this.state;
        state.isUnsaved = true;
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
        state.isUnsaved = true;
        this.setState(state);
    }

    /**
     * Removes the rule template that has the given index
     * @param index
     */
    removeRuleTemplate(index) {
        const state = this.state;
        state.isUnsaved = true;
        state.templateGroup.ruleTemplates.splice(index, 1);
        state.scriptAdditionBins.splice(index, 1);
        state.codeViewDefinition = JSON.stringify(state.templateGroup, null, 3);
        this.setState(state);
    }

    /* Code View functions [START] */

    /**
     * Updates the template group definition from the code view
     * @param content
     */
    updateByCode(content) {
        const state = this.state;
        state.isUnsaved = true;
        state.codeViewDefinition = content;
        try {
            let validJSON = JSON.parse(content);
            try {
                if (TemplateEditorUtilityFunctions.isSkeletonValid(validJSON)) {
                    // Switch off error, in case of a previous error
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
     * Enables code view error when error is given as a parameter, or disables when none is given
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
     * Reverts code view to the configuration, from which the design view has been rendered
     */
    recoverCodeView() {
        this.toggleCodeViewError();
        this.setState({ codeViewDefinition: JSON.stringify(this.state.templateGroup, null, 3) });
    }

    /**
     * Updates the given element of editor appearance settings, with the given value
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
            this.toggleCodeViewError();
            this.setNewTemplateGroup();
        } else {
            this.setState({
                showSaveConfirmation: true,
                saveConfirmationAction: 'new',
            });
        }
    }

    /**
     * Sets an empty skeleton as the template group definition
     */
    setNewTemplateGroup() {
        this.setState({
            templateGroup: JSON.parse(JSON.stringify(TemplateEditorConstants.TEMPLATE_GROUP_SKELETON)),
            codeViewDefinition: JSON.stringify(
                JSON.parse(JSON.stringify(TemplateEditorConstants.TEMPLATE_GROUP_SKELETON)), null, 3)
        });
    }

    /**
     * Handles onClick of the Open button
     */
    handleOpenClick() {
        if (!this.state.isUnsaved) {
            this.toggleCodeViewError();
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
     * Imports template group definition from a file and assigns it to the state.
     * Shows respective error when the file is invalid
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
     * Validates and sets the content from loaded file, as the configuration.
     * Throws respective error when invalid content is read
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
            if (TemplateEditorUtilityFunctions.isSkeletonValid(templateGroup.templateGroup)) {
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
            if (TemplateEditorUtilityFunctions.isComplete(this.state.templateGroup, this.state.scriptAdditionBins)) {
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
     * Downloads the wrapped template group definition
     * @param templateGroupDefinition
     */
    saveTemplate(templateGroupDefinition) {
        let saveableTemplate = {
            "templateGroup": TemplateEditorUtilityFunctions.prepareTemplateGroupDefinition(templateGroupDefinition)
        };
        downloadFile(JSON.stringify(saveableTemplate, null, 3), `${templateGroupDefinition.uuid}.json`);
        this.setState({ isUnsaved: false });
    }

    /**
     * Handles the respective action after save confirmation, depending on the given choice
     * @param choice
     */
    handleAfterSaveConfirmation(choice) {
        if (choice !== 'cancel') {
            this.toggleCodeViewError();
            if (choice === 'yes') {
                this.handleSaveClick();
            }
            // Do specific action (New / Open)
            if (this.state.saveConfirmationAction === 'open') {
                this.setState({ showOpenDialog: true });
            } else {
                this.setNewTemplateGroup();
            }
            this.setState({ isUnsaved: false });
        }
        this.setState({ showSaveConfirmation: false });
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
     * Toggles display of the snackbar.
     * Shows snackbar for readable amount of time with the message - if given, hides otherwise
     * @param message
     */
    toggleSnackbar(message) {
        this.setState({
            snackbarAutoHideDuration: (message ? TemplateEditorUtilityFunctions.calculateReadingTime(message) : 3500),
            showSnackbar: !!message,
            snackbarMessage: (message ? message : '')
        })
    }

    /* UI Component displaying functions */
    displaySettings() {
        return (
            <Dialog
                open={this.state.showSettingsDialog}
                onClose={() => this.setState({ showSettingsDialog: false })}
                aria-labelledby="editorAppearanceSettings"
                aria-describedby="editorAppearanceSettings"
            >
                <DialogTitle id="editorAppearanceSettings">{"Editor Appearance Settings"}</DialogTitle>
                <DialogContent style={{ width: 450 }}>
                    <br />
                    <Typography type="body2">Theme</Typography>
                    <Select
                        value={this.state.editorSettings.theme}
                        onChange={e => this.updateEditorSettings('theme', e.target.value)}
                        input={<Input id="editorTheme" />}
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
                        input={<Input id="editorFontSize" />}
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
                    <Button
                        onClick={() => this.setState({ showSettingsDialog: false })}
                        color="default"
                        autoFocus
                    >
                        Close
                    </Button>
                </DialogActions>
            </Dialog>
        );
    }

    displayOpen() {
        return (
            <Dialog
                open={this.state.showOpenDialog}
                onClose={() => this.setState({ showOpenDialog: false })}
                aria-labelledby="openDialogTitle"
                aria-describedby="openDialogTitle"
            >
                <DialogContent style={{ paddingBottom: 10, width: 500 }}>
                    <div>
                        <Typography type="title">Open a template file</Typography>
                        <br />
                        <input type="file" ref="fileLoadInput" />
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
        );
    }

    displaySaveConfirmation() {
        return (
            <Dialog
                open={this.state.showSaveConfirmation}
                onClose={() => this.setState({ showSaveConfirmation: false })}
                aria-labelledby="saveConfirmationDialog"
                aria-describedby="saveConfirmationDialog"
            >
                <DialogContent style={{ paddingBottom: 10, width: 500 }}>
                    <DialogTitle id="unsavedChangesTitle">
                        Some changes are not saved
                    </DialogTitle>
                    <DialogContent>
                        <Typography type="subheading">
                            Do you want to save the changes?
                        </Typography>
                    </DialogContent>
                    <DialogActions>
                        <Button
                            onClick={() => this.handleAfterSaveConfirmation('yes')}
                            color="primary"
                            autoFocus
                        >
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
        );
    }

    displayIncompleteTemplate() {
        return (
            <Dialog
                open={this.state.showIncompleteTemplateDialog}
                onClose={() => this.setState({ showIncompleteTemplateDialog: false })}
                aria-labelledby="incompleteTemplateDialog"
                aria-describedby="incompleteTemplateDialog"
            >
                <DialogContent style={{ paddingBottom: 10, width: 500 }}>
                    <DialogTitle id="incompleteTemplateTitle">
                        Incomplete Template
                    </DialogTitle>
                    <DialogContent>
                        <div style={styles.incompleteTemplateErrorDisplay}>
                            <Typography type="subheading" color="inherit">
                                {this.state.incompleteTemplateError}
                            </Typography>
                        </div>
                        <Typography type="subheading">
                            Do you want to save it for editing later?
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
        );
    }

    render() {
        return (
            <MuiThemeProvider theme={theme}>
                <div>
                    <div style={styles.rootContainer}>
                        <Header
                            isUnsaved={this.state.isUnsaved}
                            isCodeViewEnabled={this.state.isCodeViewEnabled}
                            onNewClick={this.handleNewClick}
                            onOpenClick={this.handleOpenClick}
                            onSaveClick={this.handleSaveClick}
                            onSettingsClick={() => this.setState({ showSettingsDialog: true })}
                            onCodeViewToggle={this.toggleCodeView}
                            style={{ flex: 1 }}
                        />
                        {<Collapse
                            in={this.state.showCodeViewError && this.state.isCodeViewEnabled}
                            transitionDuration="auto"
                            unmountOnExit
                        >
                                <div style={styles.codeViewErrorDisplay}>
                                    <Toolbar style={{ padding: 0, margin: 0 }}>
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
                                                <CachedIcon />
                                            </IconButton>
                                        </Tooltip>
                                    </Toolbar>
                                    <Typography
                                        type="subheading"
                                        color="inherit"
                                    >
                                        {this.state.codeViewError}
                                    </Typography>
                                </div>
                            </Collapse>}
                        <div style={styles.contentContainer}>
                            <div
                                style={{
                                    position: 'relative',
                                    float: 'left',
                                    width: (this.state.isCodeViewEnabled) ? '50%' : '100%',
                                    height: '100%',
                                    overflowY: 'auto'
                                }}
                            >
                                <DesignView
                                    templateGroup={this.state.templateGroup}
                                    scriptAdditionsBins={this.state.scriptAdditionBins}
                                    editorSettings={this.state.editorSettings}
                                    handleTemplateGroupValueChange={this.handleTemplateGroupValueChange}
                                    handleRuleTemplateChange={this.onRuleTemplateChange}
                                    addScript={this.onScriptAddition}
                                    addRuleTemplate={this.addRuleTemplate}
                                    removeRuleTemplate={this.removeRuleTemplate}
                                />
                            </div>
                            {(this.state.isCodeViewEnabled) ?
                                (<div style={styles.codeViewContainer}>
                                    <CodeView
                                        content={this.state.codeViewDefinition}
                                        settings={this.state.editorSettings}
                                        onChange={this.updateByCode}
                                    />
                                </div>) : (null)}
                        </div>
                    </div>
                    {this.displaySettings()}
                    {this.displayOpen()}
                    {this.displaySaveConfirmation()}
                    {this.displayIncompleteTemplate()}
                    <Snackbar
                        autoHideDuration={this.state.snackbarAutoHideDuration}
                        open={this.state.showSnackbar}
                        onRequestClose={() => this.toggleSnackbar()}
                        transition={<Slide direction="up"/>}
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

export default TemplateEditor;
