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
import Collapse from 'material-ui/transitions/Collapse';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import ExpandMoreIcon from 'material-ui-icons/ExpandMore';
import { FormControl, FormHelperText } from 'material-ui/Form';
import Input, { InputLabel } from 'material-ui/Input';
import Select from 'material-ui/Select';
import { MenuItem } from 'material-ui/Menu';
import { IconButton } from 'material-ui';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';
// App Components
import AutoCompleteProperty from './filtercomponent/AutoCompleteProperty';
// App Utils
import BusinessRulesUtilityFunctions from '../../../../../utils/BusinessRulesUtilityFunctions';
// App Constants
import BusinessRulesConstants from '../../../../../constants/BusinessRulesConstants';
import BusinessRulesMessages from '../../../../../constants/BusinessRulesMessages';
// CSS
import '../../../../../index.css';

/**
 * Styles related to this component
 */
const styles = {
    errorText: {
        color: '#ff1744',
    },
};

/**
 * Represents the Output Component of the business rule from scratch form
 */
export default class OutputComponent extends Component {
    constructor() {
        super();
        this.state = {
            value: '',
            suggestions: [],
        };
    }

    /**
     * Displays Rule Template selection
     * @returns {Component}     Select Component
     */
    displayRuleTemplateSelection() {
        return (
            <FormControl
                disabled={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
            >
                <InputLabel htmlFor="inputRuleTemplate">Rule Template</InputLabel>
                <Select
                    value={(!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedOutputRuleTemplate)) ?
                        this.props.selectedOutputRuleTemplate.uuid : ''
                    }
                    onChange={e => this.props.handleOutputRuleTemplateSelected(e)}
                    input={<Input id="inputRuleTemplate" />}
                >
                    {this.props.outputRuleTemplates.map(outputRuleTemplate =>
                        (<MenuItem key={outputRuleTemplate.uuid} value={outputRuleTemplate.uuid}>
                            {outputRuleTemplate.name}
                        </MenuItem>))}
                </Select>
                <FormHelperText>
                    {(!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedOutputRuleTemplate)) ?
                        this.props.selectedOutputRuleTemplate.description :
                        (BusinessRulesMessages.SELECT_RULE_TEMPLATE)
                    }
                </FormHelperText>
            </FormControl>
        );
    }

    /**
     * Returns output property configurations
     * @returns {Element}       Property Components
     */
    displayProperties() {
        if (!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedOutputRuleTemplate)) {
            return (
                <div>
                    <Typography type="subheading">
                        Configurations
                    </Typography>
                    {this.props.getPropertyComponents(BusinessRulesConstants.OUTPUT_DATA_KEY, this.props.mode)}
                </div>
            );
        }
        return null;
    }

    /**
     * Returns output mapping configurations
     * @returns {Element}       Components for Output mapping
     */
    displayOutputMappings() {
        if (!BusinessRulesUtilityFunctions.isEmpty(this.props.selectedOutputRuleTemplate) &&
            !BusinessRulesUtilityFunctions.isEmpty(this.props.selectedInputRuleTemplate)) {
            const exposedOutputStreamFieldNames =
                this.props.getFieldNames(
                    this.props.selectedOutputRuleTemplate.templates[0].exposedStreamDefinition);

            return (
                <div>
                    <Typography type="subheading">
                        Mappings
                    </Typography>
                    <br />
                    <div style={{ width: '100%' }}>
                        <div style={{ float: 'left', width: '40%', height: 30 }}>
                            <Typography type="caption">Input</Typography>
                        </div>
                        <div style={{ float: 'left', width: '20%', height: 30 }}>
                            <Typography />
                        </div>
                        <div style={{ float: 'left', width: '40%', height: 30 }}>
                            <Typography type="caption">Output</Typography>
                        </div>
                        {exposedOutputStreamFieldNames.map(fieldName =>
                            (<div key={fieldName} style={{ width: '100%' }}>
                                <AutoCompleteProperty
                                    elements={this.props.inputStreamFields}
                                    disabled={this.props.mode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                                    error={
                                        !BusinessRulesUtilityFunctions.isEmpty(this.props.mappingErrorStates) ?
                                            this.props.mappingErrorStates[fieldName] : false
                                    }
                                    value={this.props.businessRuleProperties.outputMappings[fieldName] || ''}
                                    onChange={v => this.props.handleOutputMappingChange(v, fieldName)}
                                />
                                <div style={{ float: 'left', width: '20%', height: 70 }}>
                                    <center>
                                        <Typography type="subheading">As</Typography>
                                    </center>
                                </div>
                                <div style={{ float: 'left', width: '40%', height: 70 }}>
                                    <Typography type="subheading">{fieldName}</Typography>
                                </div>
                            </div>))}
                    </div>
                </div>
            );
        }
        return null;
    }

    render() {
        return (
            <div>
                <AppBar position="static" color="default">
                    <Toolbar>
                        <Typography type="subheading" style={this.props.isErroneous ? styles.errorText : {}}>
                            Output
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
                        <div style={this.props.style.paperContainer}>
                            <br />
                            <center>
                                {this.displayRuleTemplateSelection()}
                            </center>
                            <br />
                            <br />
                            <br />
                            {this.displayProperties()}
                            <br />
                            <br />
                            {this.displayOutputMappings()}
                            <br />
                        </div>
                    </Collapse>
                </Paper>
            </div>
        );
    }
}

OutputComponent.propTypes = {
    getFieldNames: PropTypes.func.isRequired,
    selectedInputRuleTemplate: PropTypes.object.isRequired,
    mode: PropTypes.oneOf([
        BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE,
        BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT,
        BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW,
    ]).isRequired,
    selectedOutputRuleTemplate: PropTypes.object.isRequired,
    handleOutputRuleTemplateSelected: PropTypes.func.isRequired,
    outputRuleTemplates: PropTypes.arrayOf(PropTypes.object).isRequired,
    getPropertyComponents: PropTypes.func.isRequired,
    inputStreamFields: PropTypes.arrayOf(PropTypes.string).isRequired,
    mappingErrorStates: PropTypes.object.isRequired,
    businessRuleProperties: PropTypes.object.isRequired,
    handleOutputMappingChange: PropTypes.func.isRequired,
    toggleExpansion: PropTypes.func.isRequired,
    isExpanded: PropTypes.bool.isRequired,
    isErroneous: PropTypes.bool.isRequired,
    style: PropTypes.object.isRequired,
};
