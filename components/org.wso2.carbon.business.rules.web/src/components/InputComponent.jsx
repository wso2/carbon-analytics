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
import {FormControl, FormHelperText} from 'material-ui/Form';
import Input, {InputLabel} from 'material-ui/Input';
import Select from 'material-ui/Select';
import {MenuItem} from 'material-ui/Menu';
import Grid from 'material-ui/Grid';
import {IconButton} from "material-ui";
import Paper from 'material-ui/Paper';
import List, {ListItem, ListItemText} from 'material-ui/List';
// App Utilities
import BusinessRulesUtilityFunctions from "../utils/BusinessRulesUtilityFunctions";
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
import BusinessRulesMessages from "../utils/BusinessRulesMessages";
// CSS
import '../index.css';

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
                    value={(!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedInputRuleTemplate)) ?
                        this.props.selectedInputRuleTemplate.uuid : ''
                    }
                    onChange={(e) => this.props.handleInputRuleTemplateSelected(e)} // todo: recheck
                    input={<Input id="inputRuleTemplate"/>}
                >
                    {inputRuleTemplateElements}
                </Select>
                <FormHelperText>
                    {(!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedInputRuleTemplate)) ?
                        this.props.selectedInputRuleTemplate.description :
                        (BusinessRulesMessages.SELECT_RULE_TEMPLATE)
                    }
                </FormHelperText>
            </FormControl>

        // If an input rule template has been selected
        if (!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedInputRuleTemplate)) {
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
                        <br/>
                        <center>
                            {inputRuleTemplatesToDisplay}
                        </center>
                        <br/>
                        <br/>
                        <br/>
                        <div>
                            <Grid container spacing={40} styles={{flexGrow: 1}}>
                                <Grid item xs={12} sm={8}>
                                    <div>
                                        {inputDataPropertiesToDisplay}
                                    </div>
                                </Grid>
                                <Grid item xs={12} sm={4}>
                                    {exposedInputStreamFieldsToDisplay}
                                </Grid>
                            </Grid>
                        </div>
                        <br/>
                    </Collapse>
                </Paper>
            </div>
        )

    }
}

export default InputComponent;
