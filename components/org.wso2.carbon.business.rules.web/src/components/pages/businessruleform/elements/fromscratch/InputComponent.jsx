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
import Typography from 'material-ui/Typography';
import Collapse from 'material-ui/transitions/Collapse';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import ExpandMoreIcon from 'material-ui-icons/ExpandMore';
import { FormControl, FormHelperText } from 'material-ui/Form';
import Input, { InputLabel } from 'material-ui/Input';
import Select from 'material-ui/Select';
import { MenuItem } from 'material-ui/Menu';
import Grid from 'material-ui/Grid';
import { IconButton } from 'material-ui';
import Paper from 'material-ui/Paper';
import List, { ListItem, ListItemText } from 'material-ui/List';
// App Utils
import BusinessRulesUtilityFunctions from '../../../../../utils/BusinessRulesUtilityFunctions';
// App Constants
import BusinessRulesConstants from '../../../../../constants/BusinessRulesConstants';
import BusinessRulesMessages from '../../../../../constants/BusinessRulesMessages';
// Styles
import Styles from '../../../../../style/Styles';
import '../../../../../index.css';

/**
 * Styles related to this component
 */
const styles = {
    root: {
        width: '100%',
        maxWidth: 360,
        position: 'relative',
        overflow: 'auto',
        maxHeight: 300,
    },
    listSection: {
        background: 'inherit',
    },
};

/**
 * Represents the Input Component of the business rule from scratch form
 */
export default class InputComponent extends Component {
    /**
     * Returns Input Rule Template selection
     * @returns {Component}     Selection field
     */
    displayRuleTemplateSelection() {
        return (
            <FormControl disabled={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}>
                <InputLabel htmlFor="inputRuleTemplate">Rule Template</InputLabel>
                <Select
                    value={(!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedRuleTemplate)) ?
                        (this.props.selectedRuleTemplate.uuid) : ('')}
                    onChange={e => this.props.handleInputRuleTemplateSelected(e)}
                    input={<Input id="inputRuleTemplate" />}
                >
                    {this.props.inputRuleTemplates.map(inputRuleTemplate =>
                        (<MenuItem key={inputRuleTemplate.uuid} value={inputRuleTemplate.uuid}>
                            {inputRuleTemplate.name}
                        </MenuItem>))}
                </Select>
                <FormHelperText>
                    {(!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedRuleTemplate)) ?
                        this.props.selectedRuleTemplate.description :
                        (BusinessRulesMessages.SELECT_RULE_TEMPLATE)
                    }
                </FormHelperText>
            </FormControl>
        );
    }

    /**
     * Returns Properties
     * @returns {HTMLElement}       Property Components
     */
    displayProperties() {
        if (!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedRuleTemplate)) {
            return (
                <div>
                    <Typography type="subheading">
                        Configurations
                    </Typography>
                    {this.props.getPropertyComponents(BusinessRulesConstants.INPUT_DATA_KEY, this.props.mode)}
                </div>
            );
        }
        return null;
    }

    /**
     * Displays exposed stream fields
     * @returns {Component}     Grid with the names and types of exposed stream fields
     */
    displayExposedStreamFields() {
        if (!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedRuleTemplate)) {
            const inputStreamFields =
                this.props.getFieldNamesAndTypes(this.props.selectedRuleTemplate.templates[0].exposedStreamDefinition);

            return (
                <Grid item>
                    <Paper style={{ padding: 10 }}>
                        <Typography type="subheading">
                            Exposed stream fields
                        </Typography>
                        <br />
                        {<List subheader style={styles.root}>
                            {Object.keys(inputStreamFields).map(field => (
                                <div key={field} style={styles.listSection}>
                                    <ListItem button key={field}>
                                        <ListItemText primary={field} secondary={inputStreamFields[field]} />
                                    </ListItem>
                                </div>
                            ))}
                        </List>}
                    </Paper>
                </Grid>
            );
        }
        return null;
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
                            Input
                        </Typography>
                        <IconButton
                            onClick={() => this.props.toggleExpansion()}
                        >
                            <ExpandMoreIcon />
                        </IconButton>
                    </Toolbar>
                </AppBar>
                <Paper>
                    <Collapse in={this.props.isExpanded} transitionDuration="auto" unmountOnExit>
                        <div style={Styles.businessRuleForm.fromScratch.component.contentContainer}>
                            <br />
                            <center>
                                {this.displayRuleTemplateSelection()}
                            </center>
                            <br />
                            <br />
                            <br />
                            <div>
                                <Grid container spacing={40} styles={{ flexGrow: 1 }}>
                                    <Grid item xs={12} sm={8}>
                                        <div>
                                            {this.displayProperties()}
                                        </div>
                                    </Grid>
                                    <Grid item xs={12} sm={4}>
                                        {this.displayExposedStreamFields()}
                                    </Grid>
                                </Grid>
                            </div>
                            <br />
                        </div>
                    </Collapse>
                </Paper>
            </div>
        );
    }
}

InputComponent.propTypes = {
    mode: PropTypes.oneOf([
        BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE,
        BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT,
        BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW,
    ]).isRequired,
    selectedRuleTemplate: PropTypes.object.isRequired,
    handleInputRuleTemplateSelected: PropTypes.func.isRequired,
    inputRuleTemplates: PropTypes.array.isRequired,
    getPropertyComponents: PropTypes.func.isRequired,
    getFieldNamesAndTypes: PropTypes.func.isRequired,
    style: PropTypes.object.isRequired,
    isErroneous: PropTypes.bool.isRequired,
    toggleExpansion: PropTypes.func.isRequired,
    isExpanded: PropTypes.bool.isRequired,
};
