import React from 'react';
// import './index.css';
// Material-UI
import Typography from 'material-ui/Typography';
import {FormControl, FormHelperText} from 'material-ui/Form';
import Input, {InputLabel} from 'material-ui/Input';
import {MenuItem} from 'material-ui/Menu';
import Select from 'material-ui/Select';
import BusinessRulesFunctions from "../utils/BusinessRulesFunctions";
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
import Header from "./Header";
import BusinessRuleFromTemplateForm from "./BusinessRuleFromTemplateForm";
import BusinessRulesAPIs from "../utils/BusinessRulesAPIs";

/**
 * Allows to select a Rule Template, of a given type ('template', 'input' or 'output')
 * which will then display the form with Rule Template's properties
 */
class RuleTemplateSelector extends React.Component {
    /**
     * Updates selected Rule Template in the state,
     * when Rule Template is selected from the list
     * @param name
     */
    handleRuleTemplateSelected(templateGroupUUID,event){
        let state = this.state
        let self = this
        // Get selected rule template & update in the state
        let selectedRuleTemplatePromise = new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL)
            .getRuleTemplate(templateGroupUUID, event.target.value);
        selectedRuleTemplatePromise.then(function(selectedRuleTemplateResponse){
            state['selectedRuleTemplate'] = selectedRuleTemplateResponse.data
            self.setState(state)
        })
        // state['selectedRuleTemplate'] = BusinessRulesFunctions.getRuleTemplate(templateGroupUUID, event.target.value)
    }

    constructor(props) {
        super(props);
        this.state = {
            selectedTemplateGroup: props.selectedTemplateGroup,
            ruleTemplates: props.ruleTemplates,
            selectedRuleTemplate: {name: '', uuid: ''}, // Initialize with no Rule Template selected
        }
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
                <Header
                    title="Business Rule Manager"
                />
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
                        onChange={(e)=>this.handleRuleTemplateSelected(this.state.selectedTemplateGroup.uuid,e)}
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
