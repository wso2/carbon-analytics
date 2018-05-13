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
import IconButton from 'material-ui/IconButton';
import ClearIcon from 'material-ui-icons/Clear';
import { Input, Typography } from 'material-ui';
import Paper from 'material-ui/Paper';
import { MenuItem } from 'material-ui/Menu';
import TextField from 'material-ui/TextField';
import { FormControl } from 'material-ui/Form';
import Select from 'material-ui/Select';
// App Components
import Property from '../Property';
// App Constants
import BusinessRulesConstants from '../../../../../constants/BusinessRulesConstants';
// CSS
import '../../../../../index.css';

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
            suggestions: [],
        };
    }

    // TODO under construction ------------------------------------
    // TODO I STOPPED HERE - REVISED

    updateFilterRuleAttribute(value) {
        const filterRule = this.props.filterRule;
        filterRule[0] = value.newValue;
        this.updateFilterRule(filterRule);
    }

    updateFilterRuleOperator(value) {
        const filterRule = this.props.filterRule;
        filterRule[1] = value;
        this.updateFilterRule(filterRule);
    }

    updateFilterRuleAttributeOrValue(value) {
        const filterRule = this.props.filterRule;
        filterRule[2] = value.newValue;
        this.updateFilterRule(filterRule);
    }

    updateFilterRule(value) {
        this.props.onUpdate(value);
    }

    // TODO -----------------------------------------

    /* AutoSuggest related functions [START] */

    returnSuggestionsAsLabels() {
        return this.props.getFieldNames(this.props.exposedStreamDefinition).map(fieldName => ({ label: fieldName }));
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
            />);
    }

    renderSuggestion(suggestion, { query, isHighlighted }) {
        const matches = match(suggestion.label, query);
        const parts = parse(suggestion.label, matches);
        return (
            <MenuItem selected={isHighlighted} component="div">
                <div>
                    {parts.map((part, index) => {
                        return part.highlight ?
                            (<span key={String(index)} style={{ fontWeight: 300 }}>{part.text}</span>) :
                            (<strong key={String(index)} style={{ fontWeight: 500 }}>
                                {part.text}
                            </strong>);
                    })}
                </div>
            </MenuItem>);
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
            </Paper>);
    }

    getSuggestionValue(suggestion) {
        return suggestion.label;
    }

    getSuggestions(value) {
        const inputValue = value.trim().toLowerCase();
        const inputLength = inputValue.length;
        let count = 0;
        let suggestions = this.returnSuggestionsAsLabels();
        return (
            inputLength === 0 ? [] : suggestions.filter((suggestion) => {
                const keep =
                    count < 5 && suggestion.label.toLowerCase().slice(0, inputLength) === inputValue;
                if (keep) {
                    count += 1;
                }
                return keep;
            }));
    }

    handleSuggestionsFetchRequested({ value }) {
        this.setState({
            suggestions: this.getSuggestions(value),
        });
    }

    /* AutoSuggest related functions [END] */

    displayFilterRuleAttribute() {
        if (this.props.exposedInputStreamFields && (this.props.exposedInputStreamFields != null)) {
            return (
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
                        value: this.props.filterRule[0],
                        onChange: (e, v) => this.updateFilterRuleAttribute(v),
                    }}
                />);
        } else {
            return (
                <Property
                    name="filterRuleAttribute"
                    fieldName=""
                    description=""
                    value={this.props.filterRule[0]}
                    onValueChange={(modifiedValue) => this.updateFilterRuleAttribute(modifiedValue)}
                    disabledState={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                />);
        }
    }

    displayFilterRuleOperator() {
        return (
            <FormControl
                disabled={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
            >
                <Select
                    value={this.props.filterRule[1]}
                    onChange={(e) => this.updateFilterRuleOperator(e.target.value)}
                    input={<Input id={"operator"}/>}
                >
                    {BusinessRulesConstants.BUSINESS_RULE_FILTER_RULE_OPERATORS.map(operator =>
                        (<MenuItem key={operator} name={operator} value={operator}>{operator}</MenuItem>))}
                </Select>
            </FormControl>);
    }

    displayFilterRuleAttributeOrValue() {
        return (
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
                    value: this.props.filterRule[2],
                    onChange: (e, v) => this.updateFilterRuleAttributeOrValue(v),
                }}
            />);
    }

    displayDeleteButton() {
        if (this.props.mode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW) {
            return (
                <IconButton
                    color="primary"
                    style={styles.deployButton}
                    aria-label="Remove"
                    onClick={() => this.props.onRemove()}
                >
                    <ClearIcon />
                </IconButton>);
        }
        return null;
    }

    render() {
        return (
            <div style={{ width: '100%' }}>
                <div style={{ float: 'left', width: '10%', height: 50 }}>
                    <Typography type="subheading">
                        {this.props.filterRuleIndex + 1}
                    </Typography>
                </div>
                <div style={{ float: 'left', width: '30%', height: 50 }}>
                    {this.displayFilterRuleAttribute()}
                </div>
                <div style={{ float: 'left', width: '20%', height: 50 }}>
                    <center>
                        {this.displayFilterRuleOperator()}
                    </center>
                </div>
                <div style={{ float: 'left', width: '30%', height: 50 }}>
                    {this.displayFilterRuleAttributeOrValue()}
                </div>
                <div style={{ float: 'left', width: '10%', height: 50 }}>
                    {this.displayDeleteButton()}
                </div>
            </div>
        );
    }
}

export default FilterRule;
