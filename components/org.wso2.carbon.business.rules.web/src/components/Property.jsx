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
import TextField from 'material-ui/TextField';
import {FormControl, FormHelperText} from 'material-ui/Form';
import Input, {InputLabel} from 'material-ui/Input';
import {MenuItem} from 'material-ui/Menu';
import Select from 'material-ui/Select';
// CSS
import '../index.css';

/**
 * Represents a property of a rule template. Rendered either as a text field or a drop down
 */
class Property extends React.Component {
    /**
     * Handles onChange action of a TextField or a Select
     * @param name
     */
    handleOnChange(event) {
        this.props.onValueChange(event.target.value)
    }

    // Renders each Property either as a TextField or Radio Group, with default values and elements as specified
    render() {
        // If there are options specified, it is a dropdown
        if (this.props.options) {
            var options = this.props.options.map((option) => (
                <MenuItem key={option} name={option} value={option}>{option}</MenuItem>))
            return (
                <div>
                    <FormControl
                        fullWidth={(this.props.fullWidth) ? (this.props.fullWidth) : false}
                        error={(this.props.errorState) ? (this.props.errorState) : false}
                        disabled={(this.props.disabledState) ? (this.props.disabledState) : false}
                        margin="normal">
                        <InputLabel htmlFor={this.props.name}>{this.props.fieldName}</InputLabel>
                        <Select
                            value={this.props.value}
                            onChange={(e) => this.handleOnChange(e)}
                            input={<Input id={this.props.name}/>}
                        >
                            {options}
                        </Select>
                        <FormHelperText>{this.props.description}</FormHelperText>
                    </FormControl>
                    <br/>
                </div>
            );
        } else {
            return (
                <div>
                    <TextField
                        fullWidth={(this.props.fullWidth) ? (this.props.fullWidth) : false}
                        required
                        error={(this.props.errorState) ? (this.props.errorState) : false}
                        disabled={(this.props.disabledState) ? (this.props.disabledState) : false}
                        id={this.props.name}
                        name={this.props.name}
                        label={this.props.fieldName}
                        value={this.props.value}
                        helperText={this.props.description}
                        margin="normal"
                        onChange={(e) => this.handleOnChange(e)}/>
                    <br/>
                </div>
            );
        }
    }
}

export default Property;
