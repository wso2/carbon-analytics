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
import {FormControl, FormHelperText} from 'material-ui/Form';
import Input, {InputLabel} from 'material-ui/Input';
import {MenuItem} from 'material-ui/Menu';
import Select from 'material-ui/Select';
// App Components
import Header from "./Header";
import BusinessRuleFromTemplateForm from "./BusinessRuleFromTemplateForm";
// App Utilities
import BusinessRulesUtilityFunctions from "../utils/BusinessRulesUtilityFunctions";
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
// CSS
import '../index.css';

/**
 * Allows to select a Rule Template, of a given type ('template', 'input' or 'output')
 * which will then display the form with Rule Template's properties
 */
class RuleTemplateSelector extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            selectedTemplateGroup: props.selectedTemplateGroup,
            ruleTemplates: props.ruleTemplates,
            selectedRuleTemplate: {name: '', uuid: ''}, // Initialize with no Rule Template selected
        }
    }

    /**
     * Updates selected Rule Template in the state,
     * when Rule Template is selected from the list
     * @param name
     */
    handleRuleTemplateSelected(templateGroupUUID, event) {
        let state = this.state
        let self = this
        // Get selected rule template & update in the state
        let selectedRuleTemplatePromise = BusinessRulesUtilityFunctions.getRuleTemplate(templateGroupUUID, event.target.value)
        selectedRuleTemplatePromise.then(function (selectedRuleTemplateResponse) {
            state['selectedRuleTemplate'] = selectedRuleTemplateResponse.data[2]
            self.setState(state)
        })
        // state['selectedRuleTemplate'] = BusinessRulesUtilityFunctions.getRuleTemplate(templateGroupUUID, event.target.value)
    }

    /**
     * Renders a drop down which displays available Rule Templates of type 'template',
     * And displays a form on selecting a Rule Template
     */
    render() {
        // Only add Rule Templates of type 'template'
        var filteredRuleTemplates = []
        for (let ruleTemplate of this.state.ruleTemplates) {
            if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) {
                filteredRuleTemplates.push(ruleTemplate)
            }
        }

        var ruleTemplatesToDisplay = filteredRuleTemplates.map((ruleTemplate) =>
            <MenuItem key={ruleTemplate.uuid} value={ruleTemplate.uuid}>
                {ruleTemplate.name}
            </MenuItem>
        )

        // To display helper text, for the drop down to select Rule Template
        var ruleTemplateSelectionHelperText;

        // To store Business Rule form of the selected Rule Template
        var businessRuleForm;

        // If a rule template has been already selected
        if (this.state.selectedRuleTemplate.name != '') {
            ruleTemplateSelectionHelperText = this.state.selectedRuleTemplate.description
            businessRuleForm =
                <BusinessRuleFromTemplateForm
                    businessRuleType={BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE}
                    formMode={BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE}
                    selectedTemplateGroup={this.state.selectedTemplateGroup}
                    ruleTemplate={this.state.selectedRuleTemplate}/>
        } else {
            // Otherwise, show default helper text
            ruleTemplateSelectionHelperText = "Select a rule template of type - " +
                BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE
            businessRuleForm = <div></div>
        }

        return (
            <div>
                <Header/>
                <br/>
                <Typography type="headline">{this.state.selectedTemplateGroup.name}</Typography>
                <Typography type="subheading">
                    Select a template and fill in the form to create a business rule
                </Typography>
                <br/>
                <FormControl>
                    <InputLabel htmlFor="ruleTemplate">RuleTemplate</InputLabel>
                    <Select
                        value={this.state.selectedRuleTemplate.uuid}
                        onChange={(e) => this.handleRuleTemplateSelected(this.state.selectedTemplateGroup.uuid, e)}
                        input={<Input id="ruleTemplate"/>}
                    >
                        {ruleTemplatesToDisplay}
                    </Select>
                    <FormHelperText>{ruleTemplateSelectionHelperText}</FormHelperText>
                </FormControl>
                <br/>
                {businessRuleForm}
            </div>
        )
    }
}

export default RuleTemplateSelector;
