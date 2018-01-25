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
import Autosuggest from 'react-autosuggest';
import match from 'autosuggest-highlight/match';
import parse from 'autosuggest-highlight/parse';
// Material UI Components
import Collapse from 'material-ui/transitions/Collapse';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import TextField from 'material-ui/TextField';
import ExpandMoreIcon from 'material-ui-icons/ExpandMore';
import { FormControl, FormHelperText } from 'material-ui/Form';
import Input, { InputLabel } from 'material-ui/Input';
import Select from 'material-ui/Select';
import { MenuItem } from 'material-ui/Menu';
import { IconButton } from 'material-ui';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';
// App Utilities
import BusinessRulesUtilityFunctions from '../utils/BusinessRulesUtilityFunctions';
// App Constants
import BusinessRulesConstants from '../constants/BusinessRulesConstants';
import BusinessRulesMessages from '../constants/BusinessRulesMessages';
// CSS
import '../index.css';

/**
 * Styles related to autosuggest fields
 */
const autoSuggestStyles = {
    container: {
        flexGrow: 1,
        position: 'relative',
        width: '40%',
        height: 70,
        float: 'left',
    },
    suggestionsContainerOpen: {
        position: 'absolute',
        marginTop: 1,
        marginBottom: 3,
        left: 0,
        right: 0,
    },
    suggestion: {
        display: 'block',
    },
    suggestionsList: {
        margin: 0,
        padding: 0,
        listStyleType: 'none',
    },
    textField: {
        width: '100%',
    },
};


/**
 * Represents the output component of the business rule from scratch form,
 * which will contain output rule template selection, output configurations and input-as-output mappings
 */
class OutputComponent extends React.Component {
    constructor() {
        super();
        this.state = {
            value: '',
            suggestions: [],
        };
    }

    returnSuggestionsAsLabels() {
        return this.props.getFieldNames(this.props.selectedInputRuleTemplate.templates[0].exposedStreamDefinition)
            .map(fieldName => ({label: fieldName}));
    }

    renderInput(inputProps, mode) {
        const { autoFocus, value, ref } = inputProps;

        return (
            <TextField
                autoFocus={autoFocus}
                style={autoSuggestStyles.textField}
                value={value}
                inputRef={ref}
                InputProps={{ inputProps }}
                disabled={mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
            />
        );
    }

    renderSuggestion(suggestion, { query, isHighlighted }) {
        const matches = match(suggestion.label, query);
        const parts = parse(suggestion.label, matches);

        return (
            <MenuItem selected={isHighlighted} component="div">
                <div>
                    {parts.map((part, index) => {
                        return part.highlight ? (
                            <span key={String(index)} style={{ fontWeight: 300 }}>
                {part.text}
              </span>
                        ) : (
                            <strong key={String(index)} style={{ fontWeight: 500 }}>
                                {part.text}
                            </strong>
                        );
                    })}
                </div>
            </MenuItem>
        );
    }

    renderSuggestionsContainer(options) {
        const { containerProps, children } = options;

        return (
            <Paper
                key={containerProps.key}
                id={containerProps.id}
                style={containerProps.style}
                ref={containerProps.ref}
                elevation={5}
                square
            >
                {children}
            </Paper>
        );
    }

    getSuggestionValue(suggestion) {
        return suggestion.label;
    }

    getSuggestions(value) {
        const inputValue = value.trim().toLowerCase();
        const inputLength = inputValue.length;
        let count = 0;

        let suggestions = this.returnSuggestionsAsLabels();

        return inputLength === 0
            ? []
            : suggestions.filter(suggestion => {
                const keep =
                    count < 5 && suggestion.label.toLowerCase().slice(0, inputLength) === inputValue;

                if (keep) {
                    count += 1;
                }

                return keep;
            });
    }

    handleSuggestionsFetchRequested({ value }) {
        this.setState({
            suggestions: this.getSuggestions(value),
        });
    };

    render() {
        let outputRuleTemplatesToDisplay;
        let outputDataPropertiesToDisplay;
        let outputMappingsToDisplay;

        // To display rule templates selection drop down
        let outputRuleTemplateElements = this.props.outputRuleTemplates.map((outputRuleTemplate) =>
            <MenuItem key={outputRuleTemplate.uuid} value={outputRuleTemplate.uuid}>
                {outputRuleTemplate.name}
            </MenuItem>
        );
        outputRuleTemplatesToDisplay =
            (<FormControl
                disabled={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}>
                <InputLabel htmlFor="inputRuleTemplate">Rule Template</InputLabel>
                <Select
                    value={(!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedOutputRuleTemplate)) ?
                        this.props.selectedOutputRuleTemplate.uuid : ''
                    }
                    onChange={(e) => this.props.handleOutputRuleTemplateSelected(e)}
                    input={<Input id="inputRuleTemplate"/>}
                >
                    {outputRuleTemplateElements}
                </Select>
                <FormHelperText>
                    {(!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedOutputRuleTemplate)) ?
                        this.props.selectedOutputRuleTemplate.description :
                        (BusinessRulesMessages.SELECT_RULE_TEMPLATE)
                    }
                </FormHelperText>
            </FormControl>);

        // If an output rule template has been selected
        if (!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedOutputRuleTemplate)) {
            // To display output data properties
            let outputDataConfigurations = this.props.getPropertyComponents(
                BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_OUTPUT,
                this.props.mode);

            outputDataPropertiesToDisplay =
                (<div>
                    <Typography type="subheading">
                        Configurations
                    </Typography>
                    {outputDataConfigurations}
                </div>);

            // To display Output Mappings

            // If an input rule template has been selected
            if (!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedInputRuleTemplate)) {
                // Each field of the exposed output stream must be mapped with an available field of the exposed
                // input stream
                let exposedOutputStreamFieldNames =
                    this.props.getFieldNames(
                        this.props.selectedOutputRuleTemplate.templates[0].exposedStreamDefinition);

                let outputMappingElementsToDisplay = exposedOutputStreamFieldNames.map((fieldName, index) =>
                    <div key={index} style={{ width: '100%' }}>
                        <Autosuggest
                            theme={autoSuggestStyles}
                            // renderInputComponent={this.renderInput}
                            renderInputComponent={e => this.renderInput(e, this.props.mode)}
                            suggestions={this.state.suggestions}
                            onSuggestionsFetchRequested={e => this.handleSuggestionsFetchRequested(e)}
                            renderSuggestionsContainer={this.renderSuggestionsContainer}
                            getSuggestionValue={this.getSuggestionValue}
                            renderSuggestion={this.renderSuggestion}
                            inputProps={{
                                autoFocus: true,
                                autoSuggestStyles,
                                placeholder: '',
                                value: (this.props.businessRuleProperties.outputMappings[fieldName]) ?
                                    (this.props.businessRuleProperties.outputMappings[fieldName]) : '',
                                onChange: (e,v) => this.props.handleOutputMappingChange(v, fieldName),
                            }}
                        />
                        <div style={{ float: 'left', width: '20%', height: 70 }}>
                            <center>
                                <Typography type="subheading">As</Typography>
                            </center>
                        </div>
                        <div style={{ float: 'left', width: '40%', height: 70 }}>
                            <Typography type="subheading">{fieldName}</Typography>
                        </div>
                    </div>
                );

                outputMappingsToDisplay =
                    <div>
                        <Typography type="subheading">
                            Mappings
                        </Typography>
                        <br />
                        <div style={{ width: '100%' }}>
                            <div style={{ float: 'left', width: '40%', height: 30 }}>
                                <Typography type="caption">Input</Typography>
                            </div>
                            <div style={{ float: 'left', width: '20%', height: 30 }}>
                                <Typography />
                            </div>
                            <div style={{ float: 'left', width: '40%', height: 30 }}>
                                <Typography type="caption">Output</Typography>
                            </div>
                            {outputMappingElementsToDisplay}
                        </div>
                    </div>
            }
        }

        return (
            <div>
                <AppBar position="static" color="default">
                    <Toolbar>
                        <Typography type="subheading">Output</Typography>
                        <IconButton
                            onClick={() => this.props.toggleExpansion()}
                        >
                            <ExpandMoreIcon/>
                        </IconButton>
                    </Toolbar>
                </AppBar>
                <Paper>
                    <Collapse in={this.props.isExpanded} transitionDuration="auto" unmountOnExit>
                        <div style={this.props.style.paperContainer}>
                            <br/>
                            <center>
                                {outputRuleTemplatesToDisplay}
                            </center>
                            <br/>
                            <br/>
                            <br/>
                            {outputDataPropertiesToDisplay}
                            <br/>
                            <br/>
                            {outputMappingsToDisplay}
                            <br/>
                        </div>
                    </Collapse>
                </Paper>
            </div>
        )

    }
}

export default OutputComponent;
