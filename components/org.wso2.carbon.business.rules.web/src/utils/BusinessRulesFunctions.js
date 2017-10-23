import React from 'react';
import ReactDOM from 'react-dom';
// import './index.css';
// Material-UI
import TemplateGroupSelector from "../components/TemplateGroupSelector";
import BusinessRulesConstants from "./BusinessRulesConstants";
import RuleTemplateSelector from "../components/RuleTemplateSelector";
import BusinessRuleEditor from "../components/BusinessRuleEditor";
import BusinessRuleFromScratchCreator from "../components/BusinessRuleFromScratchCreator";
import {Typography} from "material-ui";
import { CircularProgress } from 'material-ui/Progress';
import BusinessRuleCreator from "../components/BusinessRuleCreator";
import BusinessRulesAPIs from "./BusinessRulesAPIs";
import BusinessRuleFromTemplateForm from "../components/BusinessRuleFromTemplateForm";
import BusinessRuleFromScratchForm from "../components/BusinessRuleFromScratchForm";
import BusinessRulesMessages from "./BusinessRulesMessages";
import BusinessRulesManager from "../components/BusinessRulesManager";
import ProgressDisplay from "../components/ProgressDisplay";


class BusinessRulesFunctions { //todo: RenderingUtils rename
    // todo: try to directly call API involved methods

    /**
     * Loads the form, that represents an existing form for viewing. Editable mode as specified
     * @param editable
     * @param businessRuleUUID
     */
    static viewBusinessRuleForm(editable,businessRuleUUID){
        let businessRulePromise = new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL)
            .getBusinessRule(businessRuleUUID);
        businessRulePromise.then(function(businessRuleResponse){
            let gotBusinessRule = businessRuleResponse.data

            // Get the template group
            let templateGroupPromise = new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL)
                .getTemplateGroup(gotBusinessRule.templateGroupUUID);
            templateGroupPromise.then(function (response) {
                let templateGroup = response.data

                // Get rule templates
                let ruleTemplatesPromise = BusinessRulesFunctions.getRuleTemplates(templateGroup.uuid)
                ruleTemplatesPromise.then(function(ruleTemplatesResponse){
                    // Filter rule template types
                    let templateRuleTemplates = []
                    let inputRuleTemplates = []
                    let outputRuleTemplates = []
                    for(let ruleTemplate of ruleTemplatesResponse.data){
                        if(ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE){
                            templateRuleTemplates.push(ruleTemplate)
                        }else if(ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_INPUT){
                            inputRuleTemplates.push(ruleTemplate)
                        } else {
                            outputRuleTemplates.push(ruleTemplate)
                        }
                    }

                    // If Business Rule has been created from a template
                    if (gotBusinessRule.type === BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE) {
                        // Get 'template' type rule template, from which the Business Rule has been created
                        let ruleTemplatePromise = BusinessRulesFunctions.getRuleTemplate(
                            gotBusinessRule.templateGroupUUID,
                            gotBusinessRule.ruleTemplateUUID
                        )

                        ruleTemplatePromise.then(function(ruleTemplateResponse){
                            // Render the form
                            ReactDOM.render (
                                <BusinessRuleFromTemplateForm
                                    businessRuleType={BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE}
                                    formMode={
                                        (editable)?(BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT):
                                            (BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW)
                                    }
                                    businessRuleName={gotBusinessRule.name}
                                    businessRuleUUID={gotBusinessRule.uuid}
                                    selectedTemplateGroup={templateGroup}
                                    selectedRuleTemplate={ruleTemplateResponse.data}
                                    templateRuleTemplates={templateRuleTemplates}
                                    businessRuleProperties={gotBusinessRule.properties}
                                />,
                                document.getElementById('root')
                            )
                        })
                    } else {
                        // If business rule has been created from scratch

                        // Get input rule template, from which the business rule has been created
                        let inputRuleTemplatePromise = BusinessRulesFunctions.getRuleTemplate(
                            gotBusinessRule.templateGroupUUID,
                            gotBusinessRule.inputRuleTemplateUUID
                        )
                        inputRuleTemplatePromise.then(function(inputRuleTemplateResponse){
                            let inputRuleTemplate = inputRuleTemplateResponse.data

                            // Get output rule template, from which the business rule has been created
                            let outputRuleTemplatePromise = new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL)
                                .getRuleTemplate(
                                    gotBusinessRule.templateGroupUUID,
                                    gotBusinessRule.outputRuleTemplateUUID).getRuleTemplate()
                            outputRuleTemplatePromise.then(function(outputRuleTemplateResponse){
                                let outputRuleTemplate = outputRuleTemplateResponse.data

                                // Render the form
                                ReactDOM.render (
                                    <BusinessRuleFromScratchForm
                                        businessRuleType={BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH}
                                        formMode={
                                            (editable)?(BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT):
                                                (BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW)
                                        }
                                        businessRuleName={gotBusinessRule.name}
                                        businessRuleUUID={gotBusinessRule.uuid}
                                        selectedTemplateGroup={templateGroup}
                                        selectedInputRuleTemplate={inputRuleTemplate}
                                        selectedOutputRuleTemplate={outputRuleTemplate}
                                        inputRuleTemplates={inputRuleTemplates}
                                        outputRuleTemplates={outputRuleTemplates}
                                        businessRuleProperties={gotBusinessRule.properties}
                                    />,
                                    document.getElementById('root')
                                )
                            })
                        })
                    }
                })
            })
        })
    }

    /**
     * Loads business rule creator, which shows options to create a business rule from template, or scratch
     */
    static loadBusinessRuleCreator() {
        ReactDOM.render(
            <BusinessRuleCreator/>,
            document.getElementById('root')
        );
    }

    /**
     * Shows available Template Groups as thumbnails,
     * to select one for creating a Business Rule in the given mode
     *
     * @param mode 'scratch' or 'template'
     */
    static loadTemplateGroupSelector(mode) {
        new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL).getTemplateGroups()
            .then(function (templateGroupsResponse) {
            ReactDOM.render(
                <TemplateGroupSelector
                    templateGroups={templateGroupsResponse.data}
                    mode={mode}
                />, document.getElementById('root'))
        }).catch(function(error){
            console.error('Failed to load template groups.',error)
            ReactDOM.render(<ProgressDisplay error={BusinessRulesMessages.API_FAILURE}/>,
                document.getElementById('root'))
        })
        ReactDOM.render(<ProgressDisplay/>,document.getElementById('root'))
    }

    /**
     * Shows form to create a BusinessRule from scratch,
     * with available input & output rule templates from the template group, identified by the given UUID
     *
     * @param templateGroupUUID
     */
    static loadBusinessRuleFromScratchCreator(templateGroupUUID) {
        let that = this
        let templateGroupPromise = new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL)
            .getTemplateGroup(templateGroupUUID);
        templateGroupPromise.then(function(templateGroupResponse){
            // Load template group
            let templateGroup = templateGroupResponse.data
            let ruleTemplatesPromise = that.getRuleTemplates(templateGroupUUID)
            ruleTemplatesPromise.then(function(ruleTemplatesResponse){
                let inputRuleTemplates = []
                let outputRuleTemplates = []

                // Get input & output templates into different arrays
                for (let ruleTemplate of ruleTemplatesResponse.data) {
                    if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_INPUT) {
                        inputRuleTemplates.push(ruleTemplate)
                    } else if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_OUTPUT) {
                        outputRuleTemplates.push(ruleTemplate)
                    }
                }
                ReactDOM.render(
                    <BusinessRuleFromScratchForm
                        formMode={BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE}
                        selectedTemplateGroup={templateGroup}
                        inputRuleTemplates={inputRuleTemplates}
                        outputRuleTemplates={outputRuleTemplates}
                    />, document.getElementById('root'))
            })
        })
    }

    /**
     * Loads the landing page. Snackbar optional when a message is passed as a parameter
     *
     * @param snackbarMessage
     */
    static loadBusinessRulesManager(snackbarMessage){
        new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL).getBusinessRules().then(function(response){
            ReactDOM.render(
                <BusinessRulesManager
                    businessRules={response.data}
                    displaySnackBar={!!(snackbarMessage)}
                    snackbarMessageStatus={(snackbarMessage)?(snackbarMessage):('')}
                />, document.getElementById("root"))
        }).catch(function(error){
            console.error('Failed to load business rules manager.',error) // todo: rename as BRManager
            ReactDOM.render(<ProgressDisplay error={BusinessRulesMessages.CONNECTION_FAILURE}/>,
                document.getElementById("root"))
        })
        ReactDOM.render(<ProgressDisplay/>, document.getElementById("root"))
    }

    /**
     * Loads the form for creating a business rule from template by selecting a rule template,
     * that belongs to the template group which is identified by the given UUID
     */
    static loadBusinessRulesFromTemplateCreator(templateGroupUUID) {
        // Get the template group
        let templateGroupPromise = new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL)
            .getTemplateGroup(templateGroupUUID);
        templateGroupPromise.then(function(templateGroupResponse){
            let ruleTemplatesPromise = BusinessRulesFunctions.getRuleTemplates(templateGroupUUID)
            ruleTemplatesPromise.then(function(ruleTemplatesResponse){
                // Filter and get the rule templates, only of type 'template'
                let templateRuleTemplates = []
                for(let ruleTemplate of ruleTemplatesResponse.data){
                    if(ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE){
                        templateRuleTemplates.push(ruleTemplate)
                    }
                }

                ReactDOM.render(
                    <BusinessRuleFromTemplateForm
                        formMode={BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE}
                        selectedTemplateGroup={templateGroupResponse.data}
                        templateRuleTemplates={templateRuleTemplates}
                    />,
                    document.getElementById('root')
                )
            }).catch(function(error){
                console.error('Failed to load rule templates of template group : ' + templateGroupUUID + '.',error)
                ReactDOM.render(<ProgressDisplay error={BusinessRulesMessages.API_FAILURE}/>,
                    document.getElementById('root'))
            })
        }).catch(function(error){
            console.error('Failed to load template group : ' + templateGroupUUID + '.',error)
            ReactDOM.render(<ProgressDisplay error={BusinessRulesMessages.API_FAILURE}/>,
                document.getElementById('root'))
        })
    }

    /**
     * Generates UUID for a given Business Rule name
     *
     * @param businessRuleName
     * @returns {string}
     */
    static generateBusinessRuleUUID(businessRuleName) {
        return businessRuleName.toLowerCase().split(' ').join('-')
    }

    /**
     * Checks whether a given object is empty or not
     *
     * @param object
     * @returns {boolean}
     */
    static isEmpty(object) {
        for(var key in object) {
            if(object.hasOwnProperty(key))
                return false;
        }
        return true;
    }

    /*
    * Functions that have API calls within them
    */

    /** [2]
     * Returns promise for available Rule Templates, belong to the given Template Group
     *
     * @param templateGroupName
     */
    static getRuleTemplates(templateGroupUUID) { // todo: a lot of usages. remove
        return new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL).getRuleTemplates(templateGroupUUID)
    }

    /**
     * Returns promise of the Rule Template with the given name, that belongs to the given Template Group name
     *
     * @param templateGroupName
     * @param ruleTemplateName
     * @returns {*}
     */
    static getRuleTemplate(templateGroupUUID, ruleTemplateUUID) {
        // todo: call this method directly from API. stopped in the middle of refactoring
        return new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL).getRuleTemplate(templateGroupUUID, ruleTemplateUUID);
    }
}

export default BusinessRulesFunctions;
