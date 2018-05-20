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
import IconButton from 'material-ui/IconButton';
import ClearIcon from 'material-ui-icons/Clear';
import { Input, Typography } from 'material-ui';
import { MenuItem } from 'material-ui/Menu';
import { FormControl } from 'material-ui/Form';
import Select from 'material-ui/Select';
// App Components
import AutoCompleteProperty from './AutoCompleteProperty';
// App Constants
import BusinessRulesConstants from '../../../../../../constants/BusinessRulesConstants';
// CSS
import '../../../../../../index.css';

/**
 * Styles related to this component
 */
const styles = {
    deployButton: {
        color: '#EF6C00',
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
    },
};

/**
 * Represents a Filter Rule, which is specified in a Business Rule from scratch, that has exactly 4 elements :
 * FilterRuleNumber, Attribute, Operator and AttributeOrvalue
 */
export default class FilterRule extends Component {
    constructor() {
        super();
        this.state = {
            value: '',
            suggestions: [],
        };
    }

    /**
     * Updates the Attribute of the filter rule
     * @param {String} value        Attribute of the filter rule
     */
    updateFilterRuleAttribute(value) {
        const filterRule = this.props.filterRule;
        filterRule[0] = value.newValue;
        this.updateFilterRule(filterRule);
    }

    /**
     * Updates the Operator of the filter rule
     * @param {String} value        Operator of the filter rule
     */
    updateFilterRuleOperator(value) {
        const filterRule = this.props.filterRule;
        filterRule[1] = value;
        this.updateFilterRule(filterRule);
    }


    /**
     * Updates the AttributeOrValue of the filter rule
     * @param {String} value        AttributeOrValue of the filter rule
     */
    updateFilterRuleAttributeOrValue(value) {
        const filterRule = this.props.filterRule;
        filterRule[2] = value.newValue;
        this.updateFilterRule(filterRule);
    }

    /**
     * Updates the filter rule
     * @param {Array} value        The array which contains Attribute, Operator and AttributeOrValue of the filter rule
     */
    updateFilterRule(value) {
        this.props.onUpdate(value);
    }

    /**
     * Returns the input field for Attribute of the filter rule
     * @returns {Component}         Auto complete input
     */
    displayFilterRuleAttribute() {
        return (
            <AutoCompleteProperty
                elements={this.props.elements}
                onChange={v => this.updateFilterRuleAttribute(v)}
                value={this.props.filterRule[0]}
                disabled={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                // TODO do the disabled check only in the parent, and pass props
                error={false} // TODO implement
            />
        );
    }

    /**
     * Returns the input field for Operator of the filter rule
     * @returns {Component}         Drop down input
     */
    displayFilterRuleOperator() {
        return (
            <FormControl
                disabled={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
            >
                <Select
                    value={this.props.filterRule[1]}
                    onChange={e => this.updateFilterRuleOperator(e.target.value)}
                    input={<Input id={'operator'} />}
                >
                    {BusinessRulesConstants.BUSINESS_RULE_FILTER_RULE_OPERATORS.map(operator =>
                        (<MenuItem key={operator} name={operator} value={operator}>{operator}</MenuItem>))}
                </Select>
            </FormControl>);
    }

    /**
     * Returns the input field for AttributeOrValue of the filter rule
     * @returns {Component}         Auto complete input
     */
    displayFilterRuleAttributeOrValue() {
        return (
            <AutoCompleteProperty
                elements={this.props.elements}
                onChange={v => this.updateFilterRuleAttributeOrValue(v)}
                value={this.props.filterRule[2]}
                disabled={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                // TODO do the disabled check only in the parent, and pass props
                error={false} // TODO implement
            />
        );
    }

    /**
     * Returns the Delete button
     * @returns {Component}         Delete button
     */
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
        if (this.props.exposedInputStreamFields && (this.props.exposedInputStreamFields != null)) {
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
        return null;
    }
}

FilterRule.propTypes = {
    elements: PropTypes.arrayOf(PropTypes.string).isRequired,
    filterRule: PropTypes.arrayOf(PropTypes.string).isRequired,
    onUpdate: PropTypes.func.isRequired,
    getFieldNames: PropTypes.func.isRequired,
    exposedStreamDefinition: PropTypes.string.isRequired,
    mode: PropTypes.oneOf([
        BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE,
        BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT,
        BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW,
    ]).isRequired,
    onRemove: PropTypes.func.isRequired,
    exposedInputStreamFields: PropTypes.object.isRequired,
    filterRuleIndex: PropTypes.number.isRequired,
};
