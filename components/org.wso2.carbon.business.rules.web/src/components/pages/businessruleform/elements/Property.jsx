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
import { FormControl, FormHelperText } from 'material-ui/Form';
import Input, { InputLabel } from 'material-ui/Input';
import { MenuItem } from 'material-ui/Menu';
import Select from 'material-ui/Select';
// CSS
import '../../../../index.css';

/**
 * Represents a property of a rule template from which, inputs are gained
 */
export default class Property extends Component {
    /**
     * Handles the onChange action of the input field
     * @param {object} event        OnChange event
     */
    handleOnChange(event) {
        this.props.onValueChange(event.target.value);
    }

    /**
     * Returns a drop down
     * @returns {Component}     Drop down Component
     */
    displayDropDown() {
        return (
            <FormControl
                fullWidth={(this.props.fullWidth) ? (this.props.fullWidth) : false}
                error={(this.props.errorState) ? (this.props.errorState) : false}
                disabled={(this.props.disabledState) ? (this.props.disabledState) : false}
                margin="normal"
            >
                <InputLabel htmlFor={this.props.name}>{this.props.fieldName}</InputLabel>
                <Select
                    value={this.props.value}
                    onChange={e => this.handleOnChange(e)}
                    input={<Input id={this.props.name} />}
                >
                    {this.props.options.map(option => (
                        <MenuItem key={option} name={option} value={option}>{option}</MenuItem>))}
                </Select>
                <FormHelperText>{this.props.description ? this.props.description : ''}</FormHelperText>
            </FormControl>
        );
    }

    /**
     * Returns a text field
     * @returns {Component}     Text Field Component
     */
    displayTextField() {
        return (
            <TextField
                fullWidth={this.props.fullWidth || true}
                required
                error={this.props.errorState || false}
                disabled={this.props.disabledState || false}
                id={this.props.name}
                name={this.props.name}
                label={this.props.fieldName}
                value={this.props.value}
                helperText={this.props.description || ''}
                margin="normal"
                onChange={e => this.handleOnChange(e)}
            />
        );
    }

    render() {
        return (
            <div>
                {this.props.options ? this.displayDropDown() : this.displayTextField()}
                <br />
            </div>
        );
    }
}

Property.propTypes = {
    onValueChange: PropTypes.func.isRequired,
    options: PropTypes.arrayOf(PropTypes.string),
    fullWidth: PropTypes.bool,
    errorState: PropTypes.bool,
    disabledState: PropTypes.bool,
    name: PropTypes.string.isRequired,
    fieldName: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    description: PropTypes.string,
};

Property.defaultProps = {
    description: '',
    options: undefined,
    fullWidth: true,
    errorState: false,
    disabledState: false,
};
