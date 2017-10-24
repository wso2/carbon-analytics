import React from 'react';
import ReactDOM from 'react-dom';
// import './index.css';
// Material-UI
import Typography from 'material-ui/Typography';
import BusinessRulesFunctions from "../utils/BusinessRulesFunctions";
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
import Header from "./Header";
import BusinessRuleFromTemplateForm from "./BusinessRuleFromTemplateForm";
import BusinessRuleFromScratchForm from "./BusinessRuleFromScratchForm";
import BusinessRulesAPIs from "../utils/BusinessRulesAPIs";

/**
 * Allows to edit a Business Rule
 */
class BusinessRuleEditor extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            businessRule: props.businessRule,
            editable: props.editable // Form's editable status
        }
    }

    static test() {
        // // Get the Template Group
        // var templateGroup =
        //     BusinessRulesFunctions.getTemplateGroup(this.state.businessRule.templateGroupUUID)
        let that = this

        let templateGroupPromise = new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL)
            .getTemplateGroup(this.state.businessRule.templateGroupUUID);
        templateGroupPromise.then(function (response) {
            let templateGroup = response.data

            // If Business Rule has been created from a template
            if (that.state.businessRule.type === BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE) {
                // Get rule Template, from which the Business Rule has been created
                let ruleTemplatePromise = BusinessRulesFunctions.getRuleTemplate(
                    that.state.businessRule.templateGroupUUID,
                    that.state.businessRule.ruleTemplateUUID
                )

                ruleTemplatePromise.then(function(ruleTemplateResponse){
                    // Render the form
                    ReactDOM.render (
                        <BusinessRuleFromTemplateForm
                            businessRuleType={BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE}
                            formMode={BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT}
                            businessRuleName={that.state.businessRule.name}
                            businessRuleUUID={that.state.businessRule.uuid}
                            selectedTemplateGroup={templateGroup}
                            selectedRuleTemplate={ruleTemplateResponse.data}
                            businessRuleProperties={that.state.businessRule.properties}
                        />,
                        document.getElementById('root')
                    )
                })
            } else {
                // Get input rule template
                let inputRuleTemplatePromise = BusinessRulesFunctions.getRuleTemplate(
                    that.state.businessRule.templateGroupUUID,
                    that.state.businessRule.inputRuleTemplateUUID
                )

                inputRuleTemplatePromise.then(function(inputRuleTemplateResponse){
                    let inputRuleTemplate = inputRuleTemplateResponse.data

                    // Get output rule template
                    let outputRuleTemplatePromise = BusinessRulesFunctions.getRuleTemplate(
                        this.state.businessRule.templateGroupUUID,
                        this.state.businessRule.outputRuleTemplateUUID
                    )

                    outputRuleTemplatePromise.then(function(outputRuleTemplateResponse){
                        let outputRuleTemplate = outputRuleTemplateResponse.data

                        // Business Rule has been created from scratch
                        ReactDOM.render (
                            <BusinessRuleFromScratchForm
                                businessRuleType={BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH}
                                formMode={BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT}
                                businessRuleName={this.state.businessRule.name}
                                businessRuleUUID={this.state.businessRule.uuid}
                                selectedTemplateGroup={templateGroup}
                                selectedInputRuleTemplate={inputRuleTemplate}
                                selectedOutputRuleTemplate={outputRuleTemplate}
                                businessRuleProperties={this.state.businessRule.properties}
                            />,
                            document.getElementById('root')
                        )
                    })

                })
            }
        })

        // return(
        //     <Typography type="title">TET</Typography>
        // )
    }
}

export default BusinessRuleEditor;
