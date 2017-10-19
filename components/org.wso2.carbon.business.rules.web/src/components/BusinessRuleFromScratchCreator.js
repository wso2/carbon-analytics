import React from 'react';
// import './index.css';
// Material-UI
import Typography from 'material-ui/Typography';
import {FormControl, FormHelperText} from 'material-ui/Form';
import Input, {InputLabel} from 'material-ui/Input';
import Select from 'material-ui/Select';
import BusinessRulesFunctions from "../utils/BusinessRulesFunctions";
import Header from "./Header";
import BusinessRuleFromScratchForm from "./BusinessRuleFromScratchForm";
import BusinessRulesConstants from "../utils/BusinessRulesConstants";

/**
 * Allows to select a Rule Template, of a given type ('template', 'input' or 'output')
 * which will then display the form with Rule Template's properties
 */
class BusinessRuleFromScratchCreator extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            templateGroups: props.templateGroups, //todo: remove
            templateGroup: props.templateGroup,
            inputRuleTemplates: props.inputRuleTemplates,
            outputRuleTemplates: props.outputRuleTemplates
        }
    }

    /**
     * Renders a drop down which displays available Rule Templates of filtered types,
     * And displays a form on selecting a Rule Template
     */
    render() {
        // todo: get rule templates related to selected tempGroup and pass
        // todo: as 'input rule templates' and 'output rule templates'
        // To display the business Rule form
        var businessRuleForm =
            <BusinessRuleFromScratchForm
                formMode={BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE}
                selectedTemplateGroup={this.state.templateGroup}
                inputRuleTemplates={this.state.inputRuleTemplates}
                outputRuleTemplates={this.state.outputRuleTemplates}
            />


        return (
            <div>
                <Header
                    title="Business Rule Manager"
                />
                <br/>
                <Typography type="headline">Create a business rule from scratch</Typography>
                <Typography type="subheading">
                    Select a Template Group for choosing rule templates for input and output from
                </Typography>
                <br/>
                {businessRuleForm}
            </div>
        )
    }
}

export default BusinessRuleFromScratchCreator;
