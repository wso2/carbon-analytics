/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.business.rules.core.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.bean.BusinessRule;
import org.wso2.carbon.business.rules.core.bean.RuleTemplate;
import org.wso2.carbon.business.rules.core.bean.RuleTemplateProperty;
import org.wso2.carbon.business.rules.core.bean.Template;
import org.wso2.carbon.business.rules.core.bean.TemplateGroup;
import org.wso2.carbon.business.rules.core.bean.businessRulesFromScratch.BusinessRuleFromScratch;
import org.wso2.carbon.business.rules.core.bean.businessRulesFromTemplate.BusinessRuleFromTemplate;
import org.wso2.carbon.business.rules.core.exceptions.TemplateManagerException;
import org.wso2.carbon.business.rules.core.services.businessRulesFromTemplate.BusinessRulesFromTemplate;
import org.wso2.carbon.business.rules.core.util.TemplateManagerConstants;
import org.wso2.carbon.business.rules.core.util.TemplateManagerHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The exposed Template Manager service, which contains methods related to
 * Business Rules from template, and Business Rules from scratch
 */
public class TemplateManagerService implements BusinessRulesService {
    private static final Logger log = LoggerFactory.getLogger(TemplateManagerService.class);
    // Available Template Groups from the directory
    private Map<String, TemplateGroup> availableTemplateGroups;
    private Map<String, BusinessRule> availableBusinessRules;

    public TemplateManagerService() {
        // Load & store available Template Groups & Business Rules at the time of instantiation
        this.availableTemplateGroups = loadTemplateGroups();
        this.availableBusinessRules = loadBusinessRules();
    }

    public void createBusinessRuleFromTemplate(BusinessRuleFromTemplate businessRuleFromTemplate) {
        // todo: CHECK THIS METHOD
        // TODO: 9/20/17  Deploy the BR before save so that we can add the
        // Derive templates from the given business rule from template
        Map<String, Template> derivedTemplates = null;
        boolean isDeployed = false;
        try {
            derivedTemplates = deriveTemplates(businessRuleFromTemplate);
        } catch (TemplateManagerException e) {
            log.error("Error in deriving templates", e);
        }
        String businessRuleUUID = businessRuleFromTemplate.getUuid();
        try {
            saveBusinessRuleDefinition(businessRuleUUID, businessRuleFromTemplate,isDeployed); // todo : Implement method
            // Deploy all derived templates, only if saving Business Rule definition is successful
            for (String templateUUID : derivedTemplates.keySet()) {
                try {
                    deployTemplate(templateUUID, derivedTemplates.get(templateUUID));
                } catch (TemplateManagerException e) {
                    log.error(e.getMessage(), e);
                }
            }
            isDeployed =true;
            saveBusinessRuleDefinition(businessRuleUUID,businessRuleFromTemplate,isDeployed);
        } catch (TemplateManagerException e) {
            // Saving definition is unsuccessful
            log.error(e.getMessage(), e); // Exception is thrown from the saveBusinessRuleDefinition method itself
        }
    }

    public void editBusinessRuleFromTemplate(String uuid, BusinessRuleFromTemplate businessRuleFromTemplate) { // todo: verify next lower level
        Map<String, Template> derivedTemplates = null;
        try {
            derivedTemplates = deriveTemplates(businessRuleFromTemplate);
        } catch (TemplateManagerException e) {
            log.error(e.getMessage(), e);
        }

        try {
            overwriteBusinessRuleDefinition(uuid, businessRuleFromTemplate);
            // Load all available Business Rules again
            this.availableBusinessRules = loadBusinessRules();
            // Update Deploy templates, only if overwriting Business Rule Definition is successful
            // todo: (Q) is this ok?
            for (String templateUUID : derivedTemplates.keySet()) {
                updateDeployTemplate(templateUUID, derivedTemplates.get(templateUUID));
            }
        } catch (TemplateManagerException e) {
            // Overwriting definition / Update Deploy unsuccessful
            log.error(e.getMessage(), e);
        }
    }

    public BusinessRule findBusinessRuleFromTemplate(String businessRuleUUID) throws TemplateManagerException {
        for (String availableBusinessRuleUUID : availableBusinessRules.keySet()) {
            if (availableBusinessRuleUUID.equals(businessRuleUUID)) {
                return availableBusinessRules.get(availableBusinessRuleUUID);
            }
        }

        throw new TemplateManagerException("No Business Rule found with the UUID : " + businessRuleUUID);
    }

    public void deleteBusinessRule(String uuid) throws TemplateManagerException { // todo: verify next lower level
        BusinessRule foundBusinessRule;
        try {
            foundBusinessRule = findBusinessRuleFromTemplate(uuid);
        } catch (TemplateManagerException e) {
            // No Business Rule Found
            log.error(e.getMessage(), e);
            // No point of further execution
            return;
        }

        // If found Business Rule is from Template
        if (foundBusinessRule instanceof BusinessRulesFromTemplate) {
            BusinessRuleFromTemplate foundBusinessRuleFromTemplate = (BusinessRuleFromTemplate) foundBusinessRule;
            Collection<String[]> templateTypesAndUUIDs = getTemplateTypesAndUUIDs(foundBusinessRuleFromTemplate);

            // Business Rule completely un-deployed status
            boolean isCompletelyUndeployed = true; // todo: think about having noOfDeployedTemplates field in the db

            for (String[] templateTypeAndUUID : templateTypesAndUUIDs) {
                try {
                    undeployTemplate(templateTypeAndUUID[0], templateTypeAndUUID[1]);
                } catch (TemplateManagerException e) {
                    isCompletelyUndeployed = false;
                    // todo: (Q) what about previously undeployed partially? now the undeployed ones will cause this to be false [noOfDeployedTemplates] might be a solution
                    log.error("Failed to un-deploy " + templateTypeAndUUID[0] + " : " + templateTypeAndUUID[1], e);
                }
            }
            // If all Templates are undeployed
            if (isCompletelyUndeployed) {
                try {
                    removeBusinessRuleDefinition(uuid);
                } catch (TemplateManagerException e) {
                    log.error("Failed to delete Business Rule definition of : " + uuid, e);
                }
            } else {
                log.error("Failed to un-deploy all the templates. Unable to delete the Business Rule definition of : " + uuid); // todo: (Q) is this ok?
            }
        }
        // todo: else: If found Business Rule is from scratch
    }

    public boolean deployBusinessRule(){
        boolean successfullyDeployed = false;


        return successfullyDeployed;
    }

    public void deployTemplates(BusinessRuleFromTemplate businessRuleFromTemplate) throws TemplateManagerException {
        Map<String, Template> derivedTemplates = deriveTemplates(businessRuleFromTemplate);
        for (String templateUUID : derivedTemplates.keySet()) {
            try {
                deployTemplate(templateUUID, derivedTemplates.get(templateUUID));
            } catch (TemplateManagerException e) {
                log.error("Failed to deploy " + derivedTemplates.get(templateUUID).getType() + " : " + templateUUID, e);
            }
        }
    }

    /**
     * Loads and returns available Template Groups from the directory
     *
     * @return
     */
    public Map<String, TemplateGroup> loadTemplateGroups() {
        File directory = new File(TemplateManagerConstants.TEMPLATES_DIRECTORY);
        // To store UUID and Template Group object
        Map<String, TemplateGroup> templateGroups = new HashMap();

        // Files from the directory
        File[] files = directory.listFiles();

        if (files != null) {
            for (final File fileEntry : files) {
                // If file is a valid json file
                if (fileEntry.isFile() && fileEntry.getName().endsWith("json")) {
                    // convert and store
                    TemplateGroup templateGroup = TemplateManagerHelper.jsonToTemplateGroup(TemplateManagerHelper
                            .fileToJson
                                    (fileEntry));
                    if (templateGroup != null) {
                        try {
                            TemplateManagerHelper.validateTemplateGroup(templateGroup);
                        } catch (TemplateManagerException e) {
                            // Invalid Template Configuration file is found
                            // Abort loading the current file and continue with the remaining
                            log.error("Invalid Template Group configuration file found: " + fileEntry.getName(), e);
                        }
                        // Put to map, as denotable by UUID
                        templateGroups.put(templateGroup.getName(), templateGroup);
                    } else {
                        log.error("Invalid Template Group configuration file found: " + fileEntry.getName());
                    }

                }
            }
        }

        return templateGroups;
    }

    /**
     * Loads and returns available Business Rules from the database
     *
     * @return
     */
    public Map<String, BusinessRule> loadBusinessRules() {
        return null; //todo: implement
    }

    /**
     * Returns available Template Group objects, denoted by UUIDs
     *
     * @return
     */
    public Map<String, TemplateGroup> getTemplateGroups() {
        return this.availableTemplateGroups;
    }

    /**
     * Gets the Template Group, that has the given UUID
     *
     * @param templateGroupUUID
     * @return
     */
    public TemplateGroup getTemplateGroup(String templateGroupUUID) throws TemplateManagerException {
        for (String availableTemplateGroupUUID : availableTemplateGroups.keySet()) {
            if (availableTemplateGroupUUID.equals(templateGroupUUID)) {
                return availableTemplateGroups.get(availableTemplateGroupUUID);
            }
        }
        throw new TemplateManagerException("No template group found with the UUID - " + templateGroupUUID);
    }

    /**
     * Returns RuleTemplate objects belonging to the given Template Group, denoted by UUIDs
     *
     * @param templateGroupUUID
     * @return
     */
    public Map<String, RuleTemplate> getRuleTemplates(String templateGroupUUID) throws TemplateManagerException {
        HashMap<String, RuleTemplate> ruleTemplates = new HashMap<String, RuleTemplate>();
        for (String availableTemplateGroupUUID : availableTemplateGroups.keySet()) {
            // If matching UUID found
            if (availableTemplateGroupUUID.equals(templateGroupUUID)) {
                TemplateGroup foundTemplateGroup = availableTemplateGroups.get(availableTemplateGroupUUID);
                Collection<RuleTemplate> foundRuleTemplates = foundTemplateGroup.getRuleTemplates();

                // Put all the found Rule Templates denoted by their UUIDs, for returning
                for (RuleTemplate foundRuleTemplate : foundRuleTemplates) {
                    ruleTemplates.put(foundRuleTemplate.getName(), foundRuleTemplate);
                }

                return ruleTemplates;
            }
        }

        throw new TemplateManagerException("No template group found with the UUID - " + templateGroupUUID);
    }

    /**
     * Gets Rule Template, which belongs to the given Template Group and has the given Rule Template UUID
     *
     * @param templateGroupUUID
     * @param ruleTemplateUUID
     * @return
     */
    public RuleTemplate getRuleTemplate(String templateGroupUUID, String ruleTemplateUUID) throws TemplateManagerException {
        TemplateGroup foundTemplateGroup = getTemplateGroup(templateGroupUUID);
        for (RuleTemplate ruleTemplate : foundTemplateGroup.getRuleTemplates()) {
            if (ruleTemplate.getUuid().equals(ruleTemplateUUID)) {
                return ruleTemplate;
            }
        }

        throw new TemplateManagerException("No rule template found with the UUID - " + ruleTemplateUUID);
    }

    /**
     * Returns available Business Rule objects, denoted by UUIDs
     *
     * @return
     */
    public Map<String, BusinessRule> getBusinessRules() {
        return this.availableBusinessRules;
    }

    /**
     * Derives and returns templates from the given BusinessRuleFromTemplate.
     * As specified in the given Business Rule,
     * RuleTemplate is found and its templated properties are replaced with the properties map
     *
     * @param businessRuleFromTemplate
     * @return Templates with replaced properties in the content, denoted by their UUIDs
     */
    public Map<String, Template> deriveTemplates(BusinessRuleFromTemplate businessRuleFromTemplate) throws TemplateManagerException {
        // To store derived Template types and Templates
        HashMap<String, Template> derivedTemplates = new HashMap<String, Template>();
        // Get available Templates under the Rule Template, which is specified in the Business Rule
        Collection<Template> templatesToBeUsed = getTemplates(businessRuleFromTemplate);
        // Get properties to map, as specified in the Business Rule
        Map<String, String> propertiesToMap = businessRuleFromTemplate.getProperties();

        for (Template template : templatesToBeUsed) {
            // If Template is a SiddhiApp
            if (template.getType().equals(TemplateManagerConstants.SIDDHI_APP_TEMPLATE_TYPE)) {
                // Derive SiddhiApp
                Template derivedSiddhiApp = deriveSiddhiApp(template, propertiesToMap);
                try {
                    // Put SiddhiApp's name and content to derivedTemplates HashMap
                    derivedTemplates.put(TemplateManagerHelper.getSiddhiAppName(derivedSiddhiApp), derivedSiddhiApp);
                } catch (TemplateManagerException e) {
                    log.error("Error in deriving SiddhiApp", e);
                }
            }
            // Other template types are not concerned for now
        }

        return derivedTemplates;
    }

    /**
     * Gives the list of Templates, that should be used by the given BusinessRuleFromTemplate
     *
     * @param businessRuleFromTemplate Given Business Rule
     * @return
     */
    public Collection<Template> getTemplates(BusinessRuleFromTemplate businessRuleFromTemplate) throws TemplateManagerException {
        RuleTemplate foundRuleTemplate = getRuleTemplate(businessRuleFromTemplate);
        // Get Templates from the found Rule Template
        Collection<Template> templates = foundRuleTemplate.getTemplates();

        return templates;
    }

    /**
     * Derives a SiddhiApp by replacing given SiddhiApp template's templated properties with the given values
     *
     * @param siddhiAppTemplate      Templated SiddhiApp
     * @param templatedElementValues Values for replacing templated elements, that are derived by the script from the
     *                               TemplateGroup / directly entered by the user (when no script is present)
     * @return
     */
    public Template deriveSiddhiApp(Template siddhiAppTemplate, Map<String, String> templatedElementValues) {
        // SiddhiApp content, that contains templated elements
        String templatedSiddhiApp = siddhiAppTemplate.getContent();

        // To replace templated elements with given values
        StringBuffer derivedSiddhiAppBuffer = new StringBuffer();
        // Find all templated elements from the SiddhiApp
        Pattern templatedElementPattern = Pattern.compile(TemplateManagerConstants.TEMPLATED_ELEMENT_REGEX_PATTERN);
        Matcher templatedElementMatcher = templatedElementPattern.matcher(templatedSiddhiApp);

        // When a templated element is found
        while (templatedElementMatcher.find()) {
            // Templated Element (inclusive of template pattern)
            String templatedElement = templatedElementMatcher.group(1);
            // Find Templated Element's name
            Pattern templatedElementNamePattern = Pattern.compile(TemplateManagerConstants.TEMPLATED_ELEMENT_NAME_REGEX_PATTERN);
            Matcher templatedElementNameMatcher = templatedElementNamePattern.matcher(templatedElement);


            // When the templated element's name is found
            if (templatedElementNameMatcher.find()) {
                String templatedElementName = templatedElementNameMatcher.group(1);
                String elementReplacement = templatedElementValues.get(templatedElementName);
                // Replace templated element with provided value
                templatedElementMatcher.appendReplacement(derivedSiddhiAppBuffer, elementReplacement);
            }
        }
        templatedElementMatcher.appendTail(derivedSiddhiAppBuffer);
        Template derivedSiddhiApp = new Template(TemplateManagerConstants.SIDDHI_APP_TEMPLATE_TYPE, derivedSiddhiAppBuffer.toString(), null);

        return derivedSiddhiApp;
    }

    /**
     * Gets the Rule Template, that is specified in the given Business Rule
     *
     * @param businessRuleFromTemplate
     * @return
     */
    public RuleTemplate getRuleTemplate(BusinessRuleFromTemplate businessRuleFromTemplate) throws TemplateManagerException {
        String templateGroupUUID = businessRuleFromTemplate.getTemplateGroupUUID();
        String ruleTemplateUUID = businessRuleFromTemplate.getRuleTemplateUUID();

        TemplateGroup foundTemplateGroup = this.availableTemplateGroups.get(templateGroupUUID);
        RuleTemplate foundRuleTemplate = null;

        // A Template Group has been found with the given UUID
        if (foundTemplateGroup != null) {
            for (RuleTemplate ruleTemplate : foundTemplateGroup.getRuleTemplates()) {
                if (ruleTemplate.getUuid().equals(ruleTemplateUUID)) {
                    foundRuleTemplate = ruleTemplate;
                    break;
                }
            }
            // If a Rule Template has been found
            if (foundRuleTemplate != null) {
                return foundRuleTemplate;
            } else {
                throw new TemplateManagerException("No rule template found with the given uuid");
            }
        } else {
            throw new TemplateManagerException("No template group found with the given uuid");
        }

    }

    /**
     * Saves JSON definition of the given Business Rule, to the database
     *
     * @param businessRuleFromTemplate
     * @throws TemplateManagerException
     */
    public void saveBusinessRuleDefinition(String uuid, BusinessRuleFromTemplate businessRuleFromTemplate, boolean
            isDeployed) throws
            TemplateManagerException {
        // System.out.println("[SAVED BUSINESS RULE DEFINITION]__________");
        // System.out.println("UUID : " + uuid);
        // System.out.println("Business Rule Definition : ");
        // System.out.println(businessRuleFromTemplate);
        // todo: implement
    }

    /**
     * Deploys the given Template
     *
     * @param template
     * @throws TemplateManagerException
     */
    public void deployTemplate(String uuid, Template template) throws TemplateManagerException {
        if (template.getType().equals(TemplateManagerConstants.SIDDHI_APP_TEMPLATE_TYPE)) {
            deploySiddhiApp(uuid, template);
        }
        // Other template types are not considered for now todo: exception
    }

    /**
     * Deploys the given Template, of type SiddhiApp
     *
     * @param siddhiAppName
     * @param siddhiApp
     * @throws TemplateManagerException
     */
    public void deploySiddhiApp(String siddhiAppName, Template siddhiApp) throws TemplateManagerException {
        // System.out.println("Successfully Deployed SiddhiApp : " + siddhiAppName);
        // todo: implement
    }

    /**
     * Gets properties that are specified in the BusinessRuleFromTemplate, with entered values as default values
     *
     * @param businessRuleFromTemplate
     * @return
     */
    public Collection<RuleTemplateProperty> getProperties(BusinessRuleFromTemplate businessRuleFromTemplate) {
        return null; //todo: implement
    }

    /**
     * Overwrites JSON definition of the Business Rule that has the given id,
     * with the given Business Rule
     *
     * @param uuid
     * @param businessRuleFromTemplate
     * @throws TemplateManagerException
     */
    public void overwriteBusinessRuleDefinition(String uuid, BusinessRuleFromTemplate businessRuleFromTemplate) throws TemplateManagerException {
        //todo: implement
    }

    /**
     * Updates the deployment of the given Template
     *
     * @param template
     * @throws TemplateManagerException
     */
    public void updateDeployTemplate(String uuid, Template template) throws TemplateManagerException {
        // todo: implement
    }

    /**
     * Updates the deployment of the given Template, of type SiddhiApp
     *
     * @param siddhiApp
     * @throws TemplateManagerException
     */
    public void updateDeploySiddhiApp(String uuid, Template siddhiApp) throws TemplateManagerException {
        // todo: implement
    }

    /**
     * Gets types and UUIDs of the Templates, that belong to the given BusinessRuleFromTemplate
     *
     * @param businessRuleFromTemplate
     * @return Collection of String array entries, of which elements are as following : [0]-TemplateType & [1]-TemplateUUID
     */
    public Collection<String[]> getTemplateTypesAndUUIDs(BusinessRuleFromTemplate businessRuleFromTemplate) throws TemplateManagerException {
        // To store found Template UUIDs and types
        // Each entry's [0]-TemplateType [1]-TemplateUUID
        Collection<String[]> templateTypesAndUUIDs = new ArrayList();

        // UUIDs and denoted derived Templates
        Map<String, Template> derivedTemplates = deriveTemplates(businessRuleFromTemplate);
        for (Template derivedTemplate : derivedTemplates.values()) {
            // If Template is a SiddhiApp
            if (derivedTemplate.getType().equals(TemplateManagerConstants.SIDDHI_APP_TEMPLATE_TYPE)) {
                try {
                    String siddhiAppName = TemplateManagerHelper.getSiddhiAppName(derivedTemplate);
                    // Add type and name of template
                    templateTypesAndUUIDs.add(new String[]{TemplateManagerConstants.SIDDHI_APP_TEMPLATE_TYPE, siddhiAppName});
                } catch (TemplateManagerException e) {
                    log.error(e.getMessage(), e);
                }
            }
            // Other template types are not considered for now
        }

        return templateTypesAndUUIDs;
    }

    /**
     * Undeploys the Template with the given UUID, according to the given template type
     *
     * @param templateType
     * @param uuid
     * @throws TemplateManagerException
     */
    public void undeployTemplate(String templateType, String uuid) throws TemplateManagerException {
        // If Template is a SiddhiApp
        if (templateType.equals(TemplateManagerConstants.SIDDHI_APP_TEMPLATE_TYPE)) {
            undeploySiddhiApp(uuid);
        }
        // other Template types are not considered for now
    }

    /**
     * Undeploys the Template of type SiddhiApp, with the given UUID
     *
     * @param uuid
     * @throws TemplateManagerException
     */
    public void undeploySiddhiApp(String uuid) throws TemplateManagerException {
        // todo: implement
    }

    /**
     * Deletes the JSON definition of the Business Rule, that has the given UUID
     *
     * @param uuid
     * @throws TemplateManagerException
     */
    public void removeBusinessRuleDefinition(String uuid) throws TemplateManagerException {
        // todo: implement
    }

    //////////// insert anything on top of this //////////

    /**
     * Finds the Template Group with the given name
     *
     * @param templateGroupName
     * @return
     * @throws TemplateManagerException
     */
    public TemplateGroup findTemplateGroup(String templateGroupName) throws TemplateManagerException {
        for (String availableTemplateGroupName : availableTemplateGroups.keySet()) {
            if (availableTemplateGroupName.equals(templateGroupName)) {
                return availableTemplateGroups.get(availableTemplateGroupName);
            }
        }

        throw new TemplateManagerException("No Template Group found with the name : " + templateGroupName);
    }


    public void createBusinessRuleFromScratch(BusinessRuleFromScratch businessRuleFromScratch) {
        // todo: implement
    }

    public void editBusinessRuleFromScratch(String uuid, BusinessRuleFromScratch businessRuleFromScratch) {
        // todo: implement
    }

    public void deployTemplates(BusinessRuleFromScratch businessRuleFromScratch) {
        // TODO: 9/18/17 implement this
    }
}
