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

import React from 'react';
import PropTypes from 'prop-types';
// Material UI Components
import Typography from 'material-ui/Typography';
import TextField from 'material-ui/TextField';
import { IconButton } from 'material-ui';
import AddIcon from 'material-ui-icons/Add';
// App Components
import RuleTemplate from './RuleTemplate';

/**
 * Represents a template group
 */
class TemplateGroup extends React.Component {
    render() {
        return (
            <div>
                <TextField
                    fullWidth
                    id="uuid"
                    name="uuid"
                    label="UUID"
                    value={this.props.configuration.uuid}
                    helperText="Used to identify the template group"
                    margin="normal"
                    onChange={e =>
                        this.props.handleTemplateGroupValueChange(e.target.name, e.target.value)}
                />
                <TextField
                    fullWidth
                    id="name"
                    name="name"
                    label="Name"
                    value={this.props.configuration.name}
                    helperText="Used for representing the template group"
                    margin="normal"
                    onChange={e =>
                        this.props.handleTemplateGroupValueChange(e.target.name, e.target.value)}
                />
                <TextField
                    fullWidth
                    id="description"
                    name="description"
                    label="Description"
                    value={this.props.configuration.description ?
                        this.props.configuration.description : ''}
                    helperText="Short description of what this template group does"
                    margin="normal"
                    onChange={e =>
                        this.props.handleTemplateGroupValueChange(e.target.name, e.target.value)}
                />
                <br />
                <br />
                <br />
                <Typography type="title">
                    Rule Templates
                </Typography>
                <br />
                {this.props.configuration.ruleTemplates.map((ruleTemplate, index) =>
                    (<div key={index}>
                        <RuleTemplate
                            configuration={ruleTemplate}
                            scriptAdditionsBin={this.props.scriptAdditionsBins[index]}
                            removeRuleTemplate={() =>
                                this.props.removeRuleTemplate(index)}
                            editorSettings={this.props.editorSettings}
                            onChange={config =>
                                this.props.handleRuleTemplateChange(index, config)}
                            onScriptAddition={scriptBin =>
                                this.props.addScript(index, scriptBin)}
                        />
                        <br />
                    </div>))}
                <br />
                <div>
                    <IconButton
                        color="primary"
                        style={{ backgroundColor: '#EF6C00', color: 'white' }}
                        aria-label="Add"
                        onClick={this.props.addRuleTemplate}
                    >
                        <AddIcon />
                    </IconButton>
                </div>
                <br />
                <br />
            </div>
        );
    }
}

TemplateGroup.propTypes = {
    configuration: PropTypes.object.isRequired,
    scriptAdditionsBins: PropTypes.array.isRequired,
    editorSettings: PropTypes.object.isRequired,
    handleTemplateGroupValueChange: PropTypes.func.isRequired,
    handleRuleTemplateChange: PropTypes.func.isRequired,
    addScript: PropTypes.func.isRequired,
    addRuleTemplate: PropTypes.func.isRequired,
    removeRuleTemplate: PropTypes.func.isRequired
};

export default TemplateGroup;
