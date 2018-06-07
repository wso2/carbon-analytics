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

import React, { Component } from 'react';
import PropTypes from 'prop-types';
// Material UI Components
import TextField from 'material-ui/TextField';
import Typography from 'material-ui/Typography';
import Collapse from 'material-ui/transitions/Collapse';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import ExpandMoreIcon from 'material-ui-icons/ExpandMore';
import AddIcon from 'material-ui-icons/Add';
import { IconButton } from 'material-ui';
import Paper from 'material-ui/Paper';
// App Components
import FilterRule from './filtercomponent/FilterRule';
// App Utils
import BusinessRulesUtilityFunctions from '../../../../../utils/BusinessRulesUtilityFunctions';
// App Constants
import BusinessRulesConstants from '../../../../../constants/BusinessRulesConstants';
import BusinessRulesMessages from '../../../../../constants/BusinessRulesMessages';
// Custom Theme
import { PrimaryColor } from '../../../../../theme/PrimaryColor';
// Styles
import Styles from '../../../../../style/Styles';
import '../../../../../index.css';

/**
 * Styles related to this component
 */
const styles = {
    addFilterRuleButton: {
        backgroundColor: PrimaryColor[500],
        color: 'white',
    },
};

/**
 * Represents the Filter Component of the business rule from scratch form
 */
export default class FilterComponent extends Component {
    /**
     * Gets numbers of filter rules, that are mentioned in the given rule logic
     * @param {String} ruleLogic        Rule Logic
     * @returns {Array}                 Filter rule numbers contained by the rule logic
     */
    getMentionedFilterRuleNumbers(ruleLogic) {
        const existingFilterRuleNumbers = [];
        const regExp = /(\d+)/gm;
        let matches;
        while ((matches = regExp.exec(ruleLogic)) !== null) {
            if (matches.index === regExp.lastIndex) {
                regExp.lastIndex++;
            }
            existingFilterRuleNumbers.push(matches[1]);
        }
        return existingFilterRuleNumbers;
    }

    /**
     * Returns auto generated rule logic, with the given list of filter rules, and the existing rule logic
     * @param {Array} filterRules       Available filter rules
     * @param {String} ruleLogic        Existing rule logic
     * @returns {String}                Auto generated rule logic
     */
    autoGenerateRuleLogic(filterRules, ruleLogic) {
        if (filterRules.length === 0) {
            return '';
        }
        if (ruleLogic !== '') {
            // To avoid cases like '1 AND 2 AND 2', where 2 was deleted and inserted again
            if (!this.getMentionedFilterRuleNumbers(ruleLogic).includes(filterRules.length.toString())) {
                return ruleLogic + ' AND ' + filterRules.length;
            }
            return ruleLogic;
        }
        // No rule logic is present
        // Concatenate each filter rule numbers with AND and return
        const numbers = [];
        for (let i = 0; i < filterRules.length; i++) {
            numbers.push(i + 1);
        }
        return numbers.join(' AND ');
    }

    /**
     * Adds a new filter rule
     */
    addFilterRule() {
        const filterRules = this.props.ruleComponents.filterRules;
        filterRules.push(['', '', '']);
        const ruleLogic = this.autoGenerateRuleLogic(filterRules, this.props.ruleComponents.ruleLogic);
        this.updateRuleComponents(filterRules, ruleLogic);
    }

    /**
     * Updates the filter rule that has the given index, with the given filterRule object
     * @param {number} index        Index of the filter rule to be updated
     * @param {Array} filterRule    An array, whose members are Attribute, Operator and AttributeOrValue
     *                              of a filter rule
     */
    updateFilterRule(index, filterRule) {
        const filterRules = this.props.ruleComponents.filterRules;
        filterRules[index] = filterRule;
        this.updateRuleComponents(filterRules, this.props.ruleComponents.ruleLogic);
    }

    /**
     * Deletes the filter rule that has the given index
     * @param {number} index        Index of the filter rule to be deleted
     */
    deleteFilterRule(index) {
        const filterRules = this.props.ruleComponents.filterRules;
        filterRules.splice(index, 1);
        if (filterRules.length > 0) {
            this.updateRuleComponents(filterRules, this.props.ruleComponents.ruleLogic);
        } else {
            // No filter rules. Reset ruleComponents
            this.updateRuleComponents(filterRules, '');
        }
    }

    /**
     * Updates the rule logic with the given value
     * @param {String} ruleLogic    Value of rule logic
     */
    updateRuleLogic(ruleLogic) {
        this.updateRuleComponents(this.props.ruleComponents.filterRules, ruleLogic);
    }

    /**
     * Updates the 'ruleComponents' object
     * @param {Array} filterRules       Array of filter rules, to be contained by 'ruleComponents'
     * @param {String} ruleLogic        Rule logic, to be contained by 'ruleComponents'
     */
    updateRuleComponents(filterRules, ruleLogic) { // Updates Filter component
        const ruleComponents = this.props.ruleComponents;
        ruleComponents.filterRules = filterRules;
        ruleComponents.ruleLogic = ruleLogic;
        this.props.onUpdate(ruleComponents);
    }

    /**
     * Returns the table which contains filter rules
     * @returns {HTMLElement}       Div containing the table that has filter rules
     */
    displayFilterRulesTable() {
        if (this.props.ruleComponents.filterRules.length > 0) {
            let exposedInputStreamFields = null; // To display selectable field options to each filter rule

            // If an input rule template has been selected
            if (!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedInputRuleTemplate)) {
                exposedInputStreamFields = this.props.getFieldNamesAndTypes(
                    this.props.selectedInputRuleTemplate.templates[0].exposedStreamDefinition);
            }

            return (
                <div style={{ width: '100%', overflowX: 'auto' }}>
                    <div style={{ width: '100%', minWidth: 560 }}>
                        <div style={{ float: 'left', width: '10%', height: 30 }}>
                            <Typography />
                        </div>
                        <div style={{ float: 'left', width: '30%', height: 30 }}>
                            <Typography type="caption">
                                Attribute
                            </Typography>
                        </div>
                        <div style={{ float: 'left', width: '20%', height: 30 }}>
                            <center>
                                <Typography type="caption">
                                    Operator
                                </Typography>
                            </center>
                        </div>
                        <div style={{ float: 'left', width: '30%', height: 30 }}>
                            <Typography type="caption">
                                Value/Attribute
                            </Typography>
                        </div>
                        <div style={{ float: 'left', width: '10%', height: 30 }}>
                            <Typography />
                        </div>
                        {this.props.ruleComponents.filterRules.map((filterRule, index) =>
                            (<FilterRule
                                key={index}
                                disabled={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                                filterRuleIndex={index}
                                filterRule={filterRule}
                                exposedStreamDefinition={
                                    this.props.selectedInputRuleTemplate.templates[0].exposedStreamDefinition}
                                getFieldNames={streamDefinition => this.props.getFieldNames(streamDefinition)}
                                elements={
                                    this.props.getFieldNames(
                                        this.props.selectedInputRuleTemplate.templates[0].exposedStreamDefinition)}
                                exposedInputStreamFields={exposedInputStreamFields}
                                onUpdate={value => this.updateFilterRule(index, value)}
                                onRemove={() => this.deleteFilterRule(index)}
                                error={
                                    !BusinessRulesUtilityFunctions.isEmpty(this.props.errorStates) ?
                                        this.props.errorStates.filterRules[index] : []
                                }
                            />))}
                    </div>
                </div>);
        }
        return null;
    }

    /**
     * Returns the Add Filter button
     * @returns {Component}     Add Filter button
     */
    displayAddFilterButton() {
        if (this.props.mode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW) {
            return (
                <IconButton
                    color="primary"
                    style={styles.addFilterRuleButton}
                    aria-label="Remove"
                    onClick={() => this.addFilterRule()}
                >
                    <AddIcon />
                </IconButton>);
        }
        return null;
    }

    /**
     * Returns the input field, which contains rule logic
     * @returns {Component}     TextField that contains the rule logic
     */
    displayRuleLogic() {
        if (this.props.ruleComponents.filterRules.length > 0) {
            return (
                <TextField
                    id="ruleLogic"
                    name="ruleLogic"
                    label="Rule Logic"
                    helperText={BusinessRulesMessages.RULE_LOGIC_HELPER_TEXT}
                    value={this.props.ruleComponents.ruleLogic}
                    onChange={e => this.updateRuleLogic(e.target.value)}
                    error={
                        !BusinessRulesUtilityFunctions.isEmpty(this.props.errorStates) ?
                            this.props.errorStates.ruleLogic : false}
                    disabled={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                    fullWidth
                />);
        }
        return null;
    }

    /**
     * Returns the expand button
     * @returns {Component}     Expand button
     */
    displayExpandButton() {
        if (!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedInputRuleTemplate)) {
            return (
                <IconButton onClick={() => this.props.toggleExpansion()}>
                    <ExpandMoreIcon />
                </IconButton>);
        }
        return null;
    }

    displayContent() {
        if (this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW &&
            this.props.ruleComponents.filterRules.length === 0 && this.props.ruleComponents.ruleLogic === '') {
            return (
                <div>
                    <br />
                    <center>
                        <Typography type="subheading">
                            No filters given
                        </Typography>
                    </center>
                    <br />
                </div>
            );
        }
        return (
            <div>
                <br />
                {this.displayFilterRulesTable()}
                <br />
                {this.displayAddFilterButton()}
                <br />
                <br />
                {this.displayRuleLogic()}
                <br />
            </div>
        );
    }

    render() {
        return (
            <div>
                <AppBar position="static" color="default">
                    <Toolbar>
                        <Typography
                            type="subheading"
                            style={this.props.isErroneous ?
                                Styles.businessRuleForm.fromScratch.component.erroneousTitle : {}}
                        >
                            Filters
                        </Typography>
                        {this.displayExpandButton()}
                    </Toolbar>
                </AppBar>
                <Paper>
                    <Collapse in={this.props.isExpanded} transitionDuration="auto" unmountOnExit>
                        <div style={Styles.businessRuleForm.fromScratch.component.contentContainer}>
                            {this.displayContent()}
                        </div>
                    </Collapse>
                </Paper>
            </div>);
    }
}

FilterComponent.propTypes = {
    ruleComponents: PropTypes.shape({
        filterRules: PropTypes.arrayOf(PropTypes.arrayOf(PropTypes.string)),
        ruleLogic: PropTypes.string,
    }).isRequired,
    onUpdate: PropTypes.func.isRequired,
    getFieldNamesAndTypes: PropTypes.func.isRequired,
    selectedInputRuleTemplate: PropTypes.object.isRequired,
    mode: PropTypes.oneOf([
        BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE,
        BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT,
        BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW,
    ]).isRequired,
    getFieldNames: PropTypes.func.isRequired,
    errorStates: PropTypes.object.isRequired,
    toggleExpansion: PropTypes.func.isRequired,
    isErroneous: PropTypes.bool.isRequired,
    isExpanded: PropTypes.bool.isRequired,
};
