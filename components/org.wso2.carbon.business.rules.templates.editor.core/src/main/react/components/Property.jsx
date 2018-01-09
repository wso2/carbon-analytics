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
import Button from 'material-ui/Button';
import {FormControl, FormHelperText} from 'material-ui/Form';
import Card, {CardActions, CardContent} from 'material-ui/Card';
import TextField from 'material-ui/TextField';
import Table, {TableBody, TableCell, TableHead, TableRow} from 'material-ui/Table';
import Input, {InputLabel} from 'material-ui/Input';
import {MenuItem} from 'material-ui/Menu';
import Select from 'material-ui/Select';
import ExpandMoreIcon from 'material-ui-icons/ExpandMore';
import Collapse from 'material-ui/transitions/Collapse';
import {IconButton} from 'material-ui';
import AddIcon from 'material-ui-icons/Add';
import ClearIcon from 'material-ui-icons/Clear';

/**
 * Styles related to this component
 */
const styles = {
    formPaper: {
        padding: 30,
    },
    formPaperContainer: {
        padding: 10,
    },
    header: {
        padding: 5,
        paddingLeft: 30,
    },
    cardContent: {
        padding: 30,
        paddingTop: 10,
    },
    flexGrow: {
        flex: '1 1 auto',
    },
};

/**
 * Represents a property, which is the derivative of a templated element
 */
class Property extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            isExpanded: false,
        };
        this.toggleExpansion = this.toggleExpansion.bind(this);
    }

    /**
     * Toggle expansion of this component
     */
    toggleExpansion() {
        const state = this.state;
        state.isExpanded = !state.isExpanded;
        this.setState(state);
    }

    render() {
        let defaultValue;
        if ((this.props.configuration.propertyObject.options) && (this.props.configuration.propertyObject.options.length > 0)) {
            const options = this.props.configuration.propertyObject.options.map(option =>
                (<MenuItem key={option} name={option} value={option}>{option}</MenuItem>));
            defaultValue =
                (<FormControl
                    fullWidth
                    margin='normal'
                    required
                >
                    <InputLabel htmlFor='defaultValue'>Default Value</InputLabel>
                    <Select
                        value={this.props.configuration.propertyObject.defaultValue}
                        onChange={e => this.props.handlePropertyValueChange('defaultValue', e.target.value)}
                        input={<Input id='defaultvalue' />}
                    >
                        {options}
                    </Select>
                    <FormHelperText>Select one of the provided options</FormHelperText>
                </FormControl>);
        } else {
            defaultValue =
                (<TextField
                    id='defaultValue'
                    name='defaultValue'
                    label='Default Value'
                    placeholder='Enter the value to show as the default value'
                    value={this.props.configuration.propertyObject.defaultValue}
                    onChange={e => this.props.handlePropertyValueChange('defaultValue', e.target.value)}
                    fullWidth
                    margin='normal'
                />);
        }

        return (
            <div style={styles.formPaperContainer}>
                <Card>
                    <CardActions disableActionSpacing style={styles.header}>
                        <Typography type='subheading'>{this.props.configuration.propertyName}</Typography>
                        <div style={styles.flexGrow} />
                        <IconButton
                            onClick={this.toggleExpansion}
                        >
                            <ExpandMoreIcon />
                        </IconButton>
                        <IconButton
                            color='primary'
                            aria-label='Remove'
                            onClick={this.props.removeProperty}
                        >
                            <ClearIcon />
                        </IconButton>
                    </CardActions>
                    <Collapse in={this.state.isExpanded} transitionDuration='auto' unmountOnExit>
                        <CardContent style={styles.cardContent}>
                            <TextField
                                id='fieldName'
                                name='fieldName'
                                label='Field Name'
                                placeholder='Enter the value to show as the prompt'
                                value={this.props.configuration.propertyObject.fieldName}
                                onChange={e => this.props.handlePropertyValueChange('fieldName', e.target.value)}
                                required
                                fullWidth
                                margin='normal'
                            />
                            <TextField
                                id='description'
                                name='description'
                                label='Field Description'
                                placeholder='Enter the value to show as the helper description'
                                value={this.props.configuration.propertyObject.description}
                                onChange={e => this.props.handlePropertyValueChange('description', e.target.value)}
                                required
                                fullWidth
                                margin='normal'
                            />
                            {defaultValue}
                            <br />
                            <br />
                            {(this.props.configuration.propertyObject.options) ?
                                (<div>
                                    <br />
                                    <Table>
                                        <TableHead>Options</TableHead>
                                        <TableBody>
                                            {this.props.configuration.propertyObject.options.map((option, index) =>
                                                (<TableRow key={index}>
                                                    <TableCell>
                                                        <TextField
                                                            id={option}
                                                            name={option}
                                                            value={option}
                                                            fullWidth
                                                            margin='normal'
                                                            onChange={e =>
                                                                this.props.handlePropertyOptionChange(
                                                                    index, e.target.value)
                                                            }
                                                        />
                                                    </TableCell>
                                                    <TableCell>
                                                        <IconButton
                                                            aria-label='Remove'
                                                            onClick={() => this.props.removePropertyOption(index)}
                                                        >
                                                            <ClearIcon />
                                                        </IconButton>
                                                    </TableCell>
                                                </TableRow>))}
                                        </TableBody>
                                    </Table>
                                </div>) : (null)
                            }
                            <br />
                            {(this.props.configuration.propertyObject.options) ?
                                (<IconButton
                                    color='primary'
                                    aria-label='Add'
                                    onClick={() => this.props.addPropertyOption()}
                                >
                                    <AddIcon />
                                </IconButton>) :
                                (<Button color='primary' onClick={() => this.props.addPropertyOption()}>
                                    <AddIcon />
                                        &nbsp; &nbsp;
                                        Options
                                </Button>)
                            }
                        </CardContent>
                    </Collapse>
                </Card>
            </div>
        );
    }
}

export default Property;
