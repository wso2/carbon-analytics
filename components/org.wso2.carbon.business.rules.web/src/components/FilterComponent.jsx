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
// Material UI Components
import Typography from 'material-ui/Typography';
import Collapse from 'material-ui/transitions/Collapse';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import ExpandMoreIcon from 'material-ui-icons/ExpandMore';
import Table, {TableBody, TableCell, TableHead, TableRow} from 'material-ui/Table';
import AddIcon from "material-ui-icons/Add"
import {IconButton} from "material-ui";
import Paper from 'material-ui/Paper';
// App Components
import Property from './Property';
import FilterRule from "./FilterRule";
// App Utilities
import BusinessRulesUtilityFunctions from "../utils/BusinessRulesUtilityFunctions";
// App Constants
import BusinessRulesConstants from "../constants/BusinessRulesConstants";
import BusinessRulesMessages from "../constants/BusinessRulesMessages";
// CSS
import '../index.css';

const animations = theme => ({
    expand: {
        transform: 'rotate(0deg)',
        transition: theme.transitions.create('transform', {
            duration: theme.transitions.duration.shortest,
        }),
    },
    expandOpen: {
        transform: 'rotate(180deg)',
    }
})

/**
 * Represents the filter component of business rules from scratch form, which contains filter rules, rule logic and
 * a button for adding filter rule
 */
class FilterComponent extends React.Component {
    render() {
        let filterRulesToDisplay
        let filterRulesTableToDisplay
        let ruleLogicToDisplay
        let propertyOptions = null
        let isRuleLogicDisabled = false // To disable the field when no filter rule is present

        let exposedInputStreamFields = null // To display selectable field options to each filter rule

        // If an input rule template has been selected
        if (!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedInputRuleTemplate)) {
            exposedInputStreamFields = this.props.getFields(
                this.props.selectedInputRuleTemplate['templates'][0]['exposedStreamDefinition'])
        }

        filterRulesToDisplay =
            this.props.businessRuleProperties
                [BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_RULE_COMPONENTS]
                [BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_RULE_COMPONENT_PROPERTY_TYPE_FILTER_RULES]
                .map((filterRule, index) =>
                    <FilterRule
                        key={index}
                        mode={this.props.mode}
                        filterRuleIndex={index}
                        filterRule={filterRule}
                        exposedInputStreamFields={exposedInputStreamFields}
                        onAttributeChange={(filterRuleIndex, value) =>
                            this.props.handleAttributeChange(filterRuleIndex, value)}
                        onOperatorChange={(filterRuleIndex, value) =>
                            this.props.handleOperatorChange(filterRuleIndex, value)}
                        onAttributeOrValueChange={(filterRuleIndex, value) =>
                            this.props.handleAttributeOrValueChange(filterRuleIndex, value)}
                        handleRemoveFilterRule={(e) => this.props.handleRemoveFilterRule(index)}
                    />)

        // Display rule logic, when at least one filter rule is present
        if (this.props.businessRuleProperties['ruleComponents']['filterRules'].length > 0) {
            filterRulesTableToDisplay =
                (<div style={{ overflowX: 'auto' }}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell/>
                                <TableCell>Attribute</TableCell>
                                <TableCell>Operator</TableCell>
                                <TableCell>Value/Attribute</TableCell>
                                <TableCell/>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {filterRulesToDisplay}
                        </TableBody>
                    </Table>
                </div>);

            ruleLogicToDisplay =
                <Property
                    name="ruleLogic"
                    fieldName="Rule Logic"
                    description={
                        (!this.props.ruleLogicWarn) ?
                            BusinessRulesMessages.RULE_LOGIC_HELPER_TEXT :
                            (BusinessRulesMessages.RULE_LOGIC_WARNING +
                                '. ' + BusinessRulesMessages.RULE_LOGIC_HELPER_TEXT)
                    }
                    value={this.props.businessRuleProperties['ruleComponents']['ruleLogic'][0]}
                    onValueChange={(e) => this.props.handleRuleLogicChange(e)}
                    errorState={this.props.ruleLogicWarn}
                    disabledState={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                    fullWidth
                />
        }

        // View add filter button only in 'create' and 'edit' modes
        let addFilterButton
        if (this.props.mode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW) {
            addFilterButton =
                <IconButton color="primary" style={this.props.style.addFilterRuleButton} aria-label="Remove"
                            onClick={(e) => this.props.addFilterRule()}>
                    <AddIcon/>
                </IconButton>
        }

        return (
            <div>
                <AppBar position="static" color="default">
                    <Toolbar>
                        <Typography type="subheading">Filters</Typography>
                        {(!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedInputRuleTemplate)) ?
                            (<IconButton
                                onClick={(e) => this.props.toggleExpansion()}
                            >
                                <ExpandMoreIcon/>
                            </IconButton>) : ('')}

                    </Toolbar>
                </AppBar>
                <Paper style={this.props.style.paper}>
                    <Collapse in={this.props.isExpanded} transitionDuration="auto" unmountOnExit>
                        {filterRulesTableToDisplay}
                        <br/>
                        {addFilterButton}
                        <br/>
                        <br/>
                        {ruleLogicToDisplay}
                        <br/>
                    </Collapse>
                </Paper>
            </div>
        )

    }
}

export default FilterComponent;
