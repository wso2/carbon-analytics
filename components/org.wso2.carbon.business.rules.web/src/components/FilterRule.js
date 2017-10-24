import React from 'react';
// import './index.css';
// Material-UI
import {TableCell, TableRow} from 'material-ui/Table';
import IconButton from 'material-ui/IconButton';
import ClearIcon from 'material-ui-icons/Clear';
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
import Property from "./Property";
import {Typography} from "material-ui";
import TextField from 'material-ui/TextField';
import { MenuItem } from 'material-ui/Menu';
import Paper from 'material-ui/Paper';

/**
 * Represents a Filter Rule, which is specified in a Business Rule from scratch, that has exactly 4 elements :
 * FilterRuleNumber, Attribute, Operator and AttributeOrvalue
 */

// Styles related to this component
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
}

class FilterRule extends React.Component {

    /**
     * Gives an array, which has Attribute, Logic, AttributeOrValue elements seperated from the given filter rule
     *
     * @param filterRule
     * @returns {*}
     */
    deriveElementsFromFilterRule(filterRule){
        let initialSplitArray = filterRule.split(" ")
        let newSplitArray = []

        // If more than 3 members available after splitting
        if (initialSplitArray.length > 3) {
            // Push first two members as they are
            for (let i = 0; i < 2; i++) {
                newSplitArray.push(initialSplitArray[i])
            }
            // Push rest of the members concatenated with space
            newSplitArray.push(initialSplitArray.slice(2, initialSplitArray.length).join(" "))

            return newSplitArray
        }

        return initialSplitArray
    }

    // To store Attribute, Operator and AttributeOrValue elements of the filter, when a change occurs
    onAttributeChange(value) {
        this.props.onAttributeChange(this.props.filterRuleIndex, value)
    }
    onOperatorChange(value) {
        this.props.onOperatorChange(this.props.filterRuleIndex, value)
    }
    onAttributeOrValueChange(value) {
        this.props.onAttributeOrValueChange(this.props.filterRuleIndex, value)
    }

    /**
     * Handles onClick of remove button of the filter rule
     *
     * @param index : Index of the rule template, comes from prop
     */
    handleRemoveFilterRuleButtonClick(index){
        this.props.handleRemoveFilterRule(index)
    }

    render() {
        // To display Attribute drop down
        var filterRuleAttributeToDisplay

        // If exposed input stream fields are passed through props
        if (this.props.exposedInputStreamFields && (this.props.exposedInputStreamFields != null)) {
            // To store options to display
            let fieldNameOptions = []
            for(let fieldName in this.props.exposedInputStreamFields){
                fieldNameOptions.push(fieldName.toString())
            }
            filterRuleAttributeToDisplay =
                <Property
                    name="filterRuleAttribute"
                    fieldName=""
                    description=""
                    value={this.deriveElementsFromFilterRule(this.props.filterRule)[0]}
                    options={fieldNameOptions}
                    onValueChange={(modifiedValue) => this.onAttributeChange(modifiedValue)}
                    disabledState={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
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

        let deleteButton
        // Display only in 'create' or 'edit' modes
        if(this.props.mode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW){
            deleteButton =
                <TableCell>
                <IconButton color="primary" style={styles.deployButton} aria-label="Remove"
                            onClick={(e)=>this.handleRemoveFilterRuleButtonClick()}>
                    <ClearIcon/>
                </IconButton>
            </TableCell>
        }

        return (
            <TableRow>
                <TableCell>
                    <Typography>
                        {this.props.filterRuleIndex + 1}
                    </Typography>
                </TableCell>
                <TableCell>
                    {filterRuleAttributeToDisplay}
                </TableCell>
                <TableCell>
                    <Property
                        name="operator"
                        fieldName=""
                        description=""
                        value={this.deriveElementsFromFilterRule(this.props.filterRule)[1]}
                        options={BusinessRulesConstants.BUSINESS_RULE_FILTER_RULE_OPERATORS}
                        onValueChange={(modifiedValue) => this.onOperatorChange(modifiedValue)}
                        disabledState={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                    />
                </TableCell>
                <TableCell>
                    <Property
                        name="attributeOrValue"
                        fieldName=""
                        description=""
                        value={this.deriveElementsFromFilterRule(this.props.filterRule)[2]}
                        onValueChange={(modifiedValue) => this.onAttributeOrValueChange(modifiedValue)}
                        disabledState={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                    />
                </TableCell>
                {deleteButton}
            </TableRow>
        )
    }
}

export default FilterRule;
