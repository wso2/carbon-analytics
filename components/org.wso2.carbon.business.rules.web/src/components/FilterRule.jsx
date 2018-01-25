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
import Autosuggest from 'react-autosuggest';
import match from 'autosuggest-highlight/match';
import parse from 'autosuggest-highlight/parse';
// Material UI Components
import IconButton from 'material-ui/IconButton';
import ClearIcon from 'material-ui-icons/Clear';
import { Input, Typography } from 'material-ui';
import Paper from 'material-ui/Paper';
import { MenuItem } from 'material-ui/Menu';
import TextField from 'material-ui/TextField';
import { FormControl } from 'material-ui/Form';
import Select from 'material-ui/Select';
// App Components
import Property from './Property';
// App Constants
import BusinessRulesConstants from '../constants/BusinessRulesConstants';
// CSS
import '../index.css';

/**
 * Styles related to this component
 */
const styles = {
    deployButton: {
        color: '#EF6C00'
    },
    container: {
        flexGrow: 1,
        position: 'relative',
        height: 200,
    },
    suggestionsContainerOpen: {
        position: 'absolute',
        marginTop: 2,
        marginBottom: 2,
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
    }
};

/**
 * Styles related to autosuggest fields
 */
const autoSuggestStyles = {
    container: {
        flexGrow: 1,
        position: 'relative',
        width: '100%',
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
 * Represents a Filter Rule, which is specified in a Business Rule from scratch, that has exactly 4 elements :
 * FilterRuleNumber, Attribute, Operator and AttributeOrvalue
 */
class FilterRule extends React.Component {
    constructor() {
        super();
        this.state = {
            value: '',
            suggestions: []
        };
    }

    /**
     * Gives an array, which has Attribute, Logic, AttributeOrValue elements seperated from the given filter rule
     *
     * @param filterRule
     * @returns {*}
     */
    deriveElementsFromFilterRule(filterRule) {
        let initialSplitArray = filterRule.split(' ');
        const newSplitArray = [];

        // If more than 3 members available after splitting
        if (initialSplitArray.length > 3) {
            // Push first two members as they are
            for (let i = 0; i < 2; i++) {
                newSplitArray.push(initialSplitArray[i]);
            }
            // Push rest of the members concatenated with space
            newSplitArray.push(initialSplitArray.slice(2, initialSplitArray.length).join(' '));
            return newSplitArray;
        }
        return initialSplitArray;
    }

    // To store Attribute, Operator and AttributeOrValue elements of the filter, when a change occurs
    onAttributeChange(value) {
        this.props.onAttributeChange(this.props.filterRuleIndex, value.newValue);
    }

    onOperatorChange(value) {
        this.props.onOperatorChange(this.props.filterRuleIndex, value);
    }

    onAttributeOrValueChange(value) {
        this.props.onAttributeOrValueChange(this.props.filterRuleIndex, value.newValue);
    }

    /**
     * Handles onClick of remove button of the filter rule
     *
     * @param index : index of the rule template, comes from prop
     */
    handleRemoveFilterRuleButtonClick(index) {
        this.props.handleRemoveFilterRule(index);
    }

    /* AutoSuggest related functions [START] */

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
                            <span key={ String(index) } style={{ fontWeight: 300 }}>
                {part.text}
              </span>
                        ) : (
                            <strong key={ String(index) } style={{ fontWeight: 500 }}>
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

    /* AutoSuggest related functions [END] */

    render() {

        // To display Attribute drop down
        let filterRuleAttributeToDisplay;

        // If exposed input stream fields are passed through props
        if (this.props.exposedInputStreamFields && (this.props.exposedInputStreamFields != null)) {
            // To store options to display
            const fieldNameOptions = [];
            for (let fieldName in this.props.exposedInputStreamFields) {
                if (Object.prototype.hasOwnProperty.call(this.props.exposedInputStreamFields, fieldName)) {
                    fieldNameOptions.push(fieldName.toString());
                }
            }

            filterRuleAttributeToDisplay =
                <Autosuggest
                    theme={autoSuggestStyles}
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
                        value: this.deriveElementsFromFilterRule(this.props.filterRule)[0],
                        onChange: (e,v) => this.onAttributeChange(v),
                    }}
                />
        } else {
            filterRuleAttributeToDisplay =
                <Property
                    name="filterRuleAttribute"
                    fieldName=""
                    description=""
                    value={this.deriveElementsFromFilterRule(this.props.filterRule)[0]}
                    onValueChange={(modifiedValue) => this.onAttributeChange(modifiedValue)}
                    disabledState={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                />
        }

        let deleteButton;
        // Display only in 'create' or 'edit' modes
        if (this.props.mode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW) {
            deleteButton =
                <IconButton
                    color="primary"
                    style={styles.deployButton}
                    aria-label="Remove"
                    onClick={() => this.handleRemoveFilterRuleButtonClick(this.props.filterRuleIndex)}
                >
                    <ClearIcon/>
                </IconButton>
        }

        return (
            <div style={{ width: '100%' }}>
                <div style={{ float: 'left', width: '10%', height: 50 }}>
                    <Typography type="subheading">
                        {this.props.filterRuleIndex + 1}
                    </Typography>
                </div>
                <div style={{ float: 'left', width: '30%', height: 50 }}>
                    {filterRuleAttributeToDisplay}
                </div>
                <div style={{ float: 'left', width: '20%', height: 50 }}>
                    <center>
                        <FormControl
                            disabled={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                        >
                            <Select
                                value={this.deriveElementsFromFilterRule(this.props.filterRule)[1]}
                                onChange={(e) => this.onOperatorChange(e.target.value)}
                                input={<Input id={"operator"}/>}
                            >
                                {BusinessRulesConstants.BUSINESS_RULE_FILTER_RULE_OPERATORS.map(operator =>
                                    (<MenuItem key={operator} name={operator} value={operator}>{operator}</MenuItem>))}
                            </Select>
                        </FormControl>
                    </center>
                </div>
                <div style={{ float: 'left', width: '30%', height: 50 }}>
                    <Autosuggest
                        theme={autoSuggestStyles}
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
                            value: this.deriveElementsFromFilterRule(this.props.filterRule)[2],
                            onChange: (e,v) => this.onAttributeOrValueChange(v),
                        }}
                    />
                </div>
                <div style={{ float: 'left', width: '10%', height: 50 }}>
                    {deleteButton}
                </div>
            </div>
        )
    }
}

export default FilterRule;
