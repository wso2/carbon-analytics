import React from 'react';
// import './index.css';
// Material-UI
import Typography from 'material-ui/Typography';
import Property from './Property';
import Button from 'material-ui/Button';
import TextField from 'material-ui/TextField';
import Collapse from 'material-ui/transitions/Collapse';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import ExpandMoreIcon from 'material-ui-icons/ExpandMore';
// import Autosuggest from 'react-autosuggest';
// import match from 'autosuggest-highlight/match';
// import parse from 'autosuggest-highlight/parse';

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
 * Represents the input component of the business rule from scratch form,
 * which will contain input rule template selection, input configurations and input exposed stream fields
 */
class InputComponent extends React.Component {
    render() {
        let inputRuleTemplatesToDisplay
        let inputDataPropertiesToDisplay
        let exposedInputStreamFieldsToDisplay

        // To display rule templates selection drop down
        let inputRuleTemplateElements = this.props.inputRuleTemplates.map((inputRuleTemplate) =>
            <MenuItem key={inputRuleTemplate.uuid} value={inputRuleTemplate.uuid}>
                {inputRuleTemplate.name}
            </MenuItem>
        )
        inputRuleTemplatesToDisplay =
            <FormControl disabled={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}>
                <InputLabel htmlFor="inputRuleTemplate">Rule Template</InputLabel>
                <Select
                    value={(!BusinessRulesFunctions.isEmpty(this.props.selectedInputRuleTemplate)) ?
                        this.props.selectedInputRuleTemplate.uuid : ''
                    }
                    onChange={(e) => this.props.handleInputRuleTemplateSelected(e)} // todo: recheck
                    input={<Input id="inputRuleTemplate"/>}
                >
                    {inputRuleTemplateElements}
                </Select>
                <FormHelperText>
                    {(!BusinessRulesFunctions.isEmpty(this.props.selectedInputRuleTemplate)) ?
                        this.props.selectedInputRuleTemplate.description :
                        (BusinessRulesMessages.SELECT_RULE_TEMPLATE)
                    }
                </FormHelperText>
            </FormControl>

        // If an input rule template has been selected
        if (!BusinessRulesFunctions.isEmpty(this.props.selectedInputRuleTemplate)) {
            // To display input data properties
            let inputConfigurations = this.props.reArrangePropertiesForDisplay(
                BusinessRulesConstants.BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_INPUT,
                this.props.mode)

            inputDataPropertiesToDisplay =
                <div>
                    <Typography type="subheading">
                        Configurations
                    </Typography>
                    {inputConfigurations}
                </div>

            // Store as a 2 dimensional array of [fieldName, fieldType]s
            let inputStreamFields =
                this.props.getFields(this.props.selectedInputRuleTemplate.templates[0]['exposedStreamDefinition'])
            let inputStreamFieldsToDisplay = []
            for (let field in inputStreamFields) {
                inputStreamFieldsToDisplay.push([field, inputStreamFields[field]])
            }

            // To display exposed input stream fields
            let exposedInputStreamFieldElementsToDisplay =
                <List subheader style={this.props.style.root}>
                    {inputStreamFieldsToDisplay.map(field => (
                        <div key={field} style={this.props.style.listSection}>
                            <ListItem button key={field[0]}>
                                <ListItemText primary={field[0]} secondary={field[1]}/>
                            </ListItem>
                        </div>
                    ))}
                </List>
            exposedInputStreamFieldsToDisplay =
                <Grid item>
                    <Paper style={{padding: 10}}>
                        <Typography type="subheading">
                            Exposed stream fields
                        </Typography>
                        <br/>
                        {exposedInputStreamFieldElementsToDisplay}
                    </Paper>
                </Grid>
        }

        return (
            <div>
                <AppBar position="static" color="default">
                    <Toolbar>
                        <Typography type="subheading">Input</Typography>
                        <IconButton
                            onClick={(e) => this.props.toggleExpansion()}
                        >
                            <ExpandMoreIcon/>
                        </IconButton>
                    </Toolbar>
                </AppBar>
                <Paper style={this.props.style.paper}>
                    <Collapse in={this.props.isExpanded} transitionDuration="auto" unmountOnExit>
                        {inputRuleTemplatesToDisplay}
                        <br/>
                        <br/>
                        <br/>
                        <Grid container style={this.props.style.rootGrid} >
                            <Grid item xs={12}>
                                <Grid container spacing={40}>
                                    <Grid item xs={9} style={{paddingLeft: 100, paddingRight: 100}}>
                                        {inputDataPropertiesToDisplay}
                                    </Grid>
                                    <Grid item xs={3}>
                                        {exposedInputStreamFieldsToDisplay}
                                    </Grid>
                                </Grid>
                            </Grid>
                        </Grid>
                        <br/>
                    </Collapse>
                </Paper>
            </div>
        )

    }
}

export default InputComponent;
