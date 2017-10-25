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
import ReactDOM from 'react-dom';
// import './index.css';
// Material-UI
import TemplateGroupSelector from "../components/TemplateGroupSelector";
import BusinessRulesConstants from "./BusinessRulesConstants";
import BusinessRuleCreator from "../components/BusinessRuleCreator";
import BusinessRulesAPICaller from "./BusinessRulesAPICaller";
import BusinessRuleFromTemplateForm from "../components/BusinessRuleFromTemplateForm";
import BusinessRuleFromScratchForm from "../components/BusinessRuleFromScratchForm";
import BusinessRulesMessages from "./BusinessRulesMessages";
import BusinessRulesManager from "../components/BusinessRulesManager";
import ProgressDisplay from "../components/ProgressDisplay";

class BusinessRulesUtilityFunctions {
    /**
     * Loads the form, that represents an existing form for viewing. Editable mode as specified
     * @param editable
     * @param businessRuleUUID
     */
    static viewBusinessRuleForm(editable, businessRuleUUID) {
        let businessRulePromise = this.getBusinessRule(businessRuleUUID)
        businessRulePromise.then(function (businessRuleResponse) {
            let gotBusinessRule = businessRuleResponse.data[2];

            // Get the template group
            let templateGroupPromise = BusinessRulesUtilityFunctions.getTemplateGroup(gotBusinessRule.templateGroupUUID)
            templateGroupPromise.then(function (response) {
                let templateGroup = response.data[2]

                // Get rule templates
                let ruleTemplatesPromise = BusinessRulesUtilityFunctions.getRuleTemplates(templateGroup.uuid)
                ruleTemplatesPromise.then(function (ruleTemplatesResponse) {
                    // Filter rule template types
                    let templateRuleTemplates = []
                    let inputRuleTemplates = []
                    let outputRuleTemplates = []
                    for (let ruleTemplate of ruleTemplatesResponse.data[2]) {
                        if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) {
                            templateRuleTemplates.push(ruleTemplate)
                        } else if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_INPUT) {
                            inputRuleTemplates.push(ruleTemplate)
                        } else {
                            outputRuleTemplates.push(ruleTemplate)
                        }
                    }

                    // If Business Rule has been created from a template
                    if (gotBusinessRule.type === BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE) {
                        // Get 'template' type rule template, from which the Business Rule has been created
                        let ruleTemplatePromise = BusinessRulesUtilityFunctions.getRuleTemplate(
                            gotBusinessRule.templateGroupUUID,
                            gotBusinessRule.ruleTemplateUUID
                        )

                        ruleTemplatePromise.then(function (ruleTemplateResponse) {
                            // Render the form
                            ReactDOM.render(
                                <BusinessRuleFromTemplateForm
                                    businessRuleType={BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE}
                                    formMode={
                                        (editable) ? (BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT) :
                                            (BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW)
                                    }
                                    businessRuleName={gotBusinessRule.name}
                                    businessRuleUUID={gotBusinessRule.uuid}
                                    selectedTemplateGroup={templateGroup}
                                    selectedRuleTemplate={ruleTemplateResponse.data[2]}
                                    templateRuleTemplates={templateRuleTemplates}
                                    businessRuleProperties={gotBusinessRule.properties}
                                />,
                                document.getElementById('root')
                            )
                        })
                    } else {
                        // If business rule has been created from scratch

                        // Get input rule template, from which the business rule has been created
                        let inputRuleTemplatePromise = BusinessRulesUtilityFunctions.getRuleTemplate(
                            gotBusinessRule.templateGroupUUID,
                            gotBusinessRule.inputRuleTemplateUUID
                        )
                        inputRuleTemplatePromise.then(function (inputRuleTemplateResponse) {
                            let inputRuleTemplate = inputRuleTemplateResponse.data[2]

                            // Get output rule template, from which the business rule has been created
                            let outputRuleTemplatePromise = BusinessRulesUtilityFunctions.getRuleTemplate(
                                gotBusinessRule.templateGroupUUID,
                                gotBusinessRule.outputRuleTemplateUUID
                            )
                            outputRuleTemplatePromise.then(function (outputRuleTemplateResponse) {
                                let outputRuleTemplate = outputRuleTemplateResponse.data[2]

                                // Render the form
                                ReactDOM.render(
                                    <BusinessRuleFromScratchForm
                                        businessRuleType={BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH}
                                        formMode={
                                            (editable) ? (BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT) :
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
        let templateGroupsPromise = this.getTemplateGroups()
        console.error("TemplateGroupsPromise")
        console.error(templateGroupsPromise)
        templateGroupsPromise.then(function (templateGroupsResponse) {
            console.error("templateGroupsResponse")
            console.error(templateGroupsResponse)
            ReactDOM.render(
                <TemplateGroupSelector
                    templateGroups={templateGroupsResponse.data[2]}
                    mode={mode}
                />, document.getElementById('root'))
        }).catch(function (error) {
            ReactDOM.render(<ProgressDisplay
                    error={['Server error', 'Failed to load available template groups']}/>,
                document.getElementById('root'))
        })
        ReactDOM.render(<ProgressDisplay/>, document.getElementById('root'))
    }

    /**
     * Shows form to create a BusinessRule from scratch,
     * with available input & output rule templates from the template group, identified by the given UUID
     *
     * @param templateGroupUUID
     */
    static loadBusinessRuleFromScratchCreator(templateGroupUUID) {
        let that = this
        let templateGroupPromise = this.getTemplateGroup(templateGroupUUID)
        templateGroupPromise.then(function (templateGroupResponse) {
            // Load template group
            let templateGroup = templateGroupResponse.data[2]
            let ruleTemplatesPromise = that.getRuleTemplates(templateGroupUUID)
            ruleTemplatesPromise.then(function (ruleTemplatesResponse) {
                let inputRuleTemplates = []
                let outputRuleTemplates = []

                // Get input & output templates into different arrays
                for (let ruleTemplate of ruleTemplatesResponse.data[2]) {
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
     * Loads the business rule modifier, with snackbar with given message when a message is passed as parameter
     *
     * @param snackbarMessage
     */
    static loadBusinessRulesManager(snackbarMessage) {
        // Load available Business Rules
        let businessRulesPromise = BusinessRulesUtilityFunctions.getBusinessRules()
        console.error("BusinessRulesPromise")
        console.error(businessRulesPromise)
        businessRulesPromise.then(function (response) {
            console.error("Business Rules Promise Response")
            console.error(response)
            ReactDOM.render(
                <BusinessRulesManager
                    businessRules={response.data[2]}
                    displaySnackBar={!!(snackbarMessage)}
                    snackbarMessage={(snackbarMessage) ? (snackbarMessage) : ('')}
                />, document.getElementById("root"))
        }).catch(function (error) {
            ReactDOM.render(<ProgressDisplay
                    error={['Server error', 'Failed to load available business rules']}/>,
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
        let templateGroupPromise = this.getTemplateGroup(templateGroupUUID)
        templateGroupPromise.then(function (templateGroupResponse) {
            let ruleTemplatesPromise = BusinessRulesUtilityFunctions.getRuleTemplates(templateGroupUUID)
            ruleTemplatesPromise.then(function (ruleTemplatesResponse) {
                // Filter and get the rule templates, only of type 'template'
                let templateRuleTemplates = []
                for (let ruleTemplate of ruleTemplatesResponse.data[2]) {
                    if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) {
                        templateRuleTemplates.push(ruleTemplate)
                    }
                }

                ReactDOM.render(
                    <BusinessRuleFromTemplateForm
                        formMode={BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE}
                        selectedTemplateGroup={templateGroupResponse.data[2]}
                        templateRuleTemplates={templateRuleTemplates}
                    />,
                    document.getElementById('root')
                )
            }).catch(function (error) {
                ReactDOM.render(<ProgressDisplay
                        error={['Server error',
                            "Failed to load rule templates from template group '"+ templateGroupUUID + "'"]}/>,
                    document.getElementById('root'))
            })
        }).catch(function (error) {
            ReactDOM.render(<ProgressDisplay
                    error={['Server error', "Failed to load template group '" + templateGroupUUID + "'"]}/>,
                document.getElementById('root'))
        })
    }

    /*
    * Functions that have API calls within them
    */

    /**
     * Returns promise for available template groups
     *
     * @returns {*}
     */
    static getTemplateGroups() {
        let apis = new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL);
        let gotTemplateGroupsPromise = apis.getTemplateGroups();
        return gotTemplateGroupsPromise;
    }

    /**
     * Returns promise for available Rule Templates, belong to the given Template Group
     *
     * @param templateGroupUUID
     * @returns {*}
     */
    static getRuleTemplates(templateGroupUUID) {
        let apis = new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL)
        return apis.getRuleTemplates(templateGroupUUID)
    }

    /**
     * Returns promise for BusinessRulesCreator
     *
     * @returns {*}
     */
    static getBusinessRules() {
        let apis = new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL);
        let gotBusinessRules = apis.getBusinessRules();

        return gotBusinessRules;
    }

    /**
     * Gets the BusinessRule with the given UUID
     *
     * @param businessRuleUUID
     * @returns {*}
     */
    static getBusinessRule(businessRuleUUID) {
        let apis = new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL)
        let gotBusinessRule = apis.getBusinessRule(businessRuleUUID)

        return gotBusinessRule
    }

    /**
     * Returns promise of the found Template Group with the given name
     *
     * @param templateGroupUUID
     * @returns {*}
     */
    static getTemplateGroup(templateGroupUUID) {
        let apis = new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL);
        let gotTemplateGroup = apis.getTemplateGroup(templateGroupUUID);

        return gotTemplateGroup;
    }

    /**
     * Returns promise of the Rule Template with the given name, that belongs to the given Template Group name
     *
     * @param templateGroupUUID
     * @param ruleTemplateUUID
     * @returns {*}
     */
    static getRuleTemplate(templateGroupUUID, ruleTemplateUUID) {
        let apis = new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL);
        let gotRuleTemplate = apis.getRuleTemplate(templateGroupUUID, ruleTemplateUUID);

        return gotRuleTemplate;
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
        for (var key in object) {
            if (object.hasOwnProperty(key))
                return false;
        }
        return true;
    }
}

export default BusinessRulesUtilityFunctions;
