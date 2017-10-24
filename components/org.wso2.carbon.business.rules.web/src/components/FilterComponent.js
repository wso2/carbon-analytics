import React from 'react';
// import './index.css';
// Material-UI
import Typography from 'material-ui/Typography';
import Property from './Property';
import Button from 'material-ui/Button';
import TextField from 'material-ui/TextField';
// import Autosuggest from 'react-autosuggest';
// import match from 'autosuggest-highlight/match';
// import parse from 'autosuggest-highlight/parse';
import Collapse from 'material-ui/transitions/Collapse';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import ExpandMoreIcon from 'material-ui-icons/ExpandMore';
import classnames from 'classnames';
import {FormControl, FormHelperText} from 'material-ui/Form';
import Input, {InputLabel} from 'material-ui/Input';
import Select from 'material-ui/Select';
import {MenuItem} from 'material-ui/Menu';
import Grid from 'material-ui/Grid';
import Table, {TableBody, TableCell, TableHead, TableRow} from 'material-ui/Table';
import BusinessRulesFunctions from "../utils/BusinessRulesFunctions";
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
import AddIcon from "material-ui-icons/Add"
import {IconButton} from "material-ui";
import Paper from 'material-ui/Paper';
import FilterRule from "./FilterRule";
import BusinessRulesAPIs from "../utils/BusinessRulesAPIs";
import List, {ListItem, ListItemText} from 'material-ui/List';
import Header from "./Header";
import BusinessRuleFromScratchForm from "./BusinessRuleFromScratchForm";
import BusinessRulesMessages from "../utils/BusinessRulesMessages";

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
        if (!BusinessRulesFunctions.isEmpty(this.props.selectedInputRuleTemplate)) {
            exposedInputStreamFields = this.props.getFields(this.props.selectedInputRuleTemplate['templates'][0]['exposedStreamDefinition'])
        }

        filterRulesToDisplay =
            this.props.businessRuleProperties[BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_RULE_COMPONENTS]
                [BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_RULE_COMPONENT_PROPERTY_TYPE_FILTER_RULES]
                .map((filterRule, index) =>
                    <FilterRule
                        key={index}
                        mode={this.props.mode}
                        filterRuleIndex={index}
                        filterRule={filterRule}
                        exposedInputStreamFields={exposedInputStreamFields}
                        onAttributeChange={(filterRuleIndex, value) => this.props.handleAttributeChange(filterRuleIndex, value)}
                        onOperatorChange={(filterRuleIndex, value) => this.props.handleOperatorChange(filterRuleIndex, value)}
                        onAttributeOrValueChange={(filterRuleIndex, value) => this.props.handleAttributeOrValueChange(filterRuleIndex, value)}
                        handleRemoveFilterRule={(e) => this.props.handleRemoveFilterRule(index)}
                    />)

        // Display rule logic, when at least one filter rule is present
        if (this.props.businessRuleProperties['ruleComponents']['filterRules'].length > 0) {
            filterRulesTableToDisplay =
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell></TableCell>
                            <TableCell>Attribute</TableCell>
                            <TableCell>Operator</TableCell>
                            <TableCell>Value/Attribute</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {filterRulesToDisplay}
                    </TableBody>
                </Table>

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
                />
        }

        // View add filter button only in 'create' and 'edit' modes
        let addFilterButton
        if(this.props.mode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW){
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
                        <IconButton
                            onClick={(e) => this.props.toggleExpansion()}
                        >
                            <ExpandMoreIcon/>
                        </IconButton>
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
