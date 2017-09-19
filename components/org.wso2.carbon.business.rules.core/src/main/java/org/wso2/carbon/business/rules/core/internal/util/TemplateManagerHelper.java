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

package org.wso2.carbon.business.rules.core.internal.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.internal.bean.BusinessRule;
import org.wso2.carbon.business.rules.core.internal.bean.businessRulesFromScratch.BusinessRuleFromScratch;
import org.wso2.carbon.business.rules.core.internal.bean.businessRulesFromTemplate.BusinessRuleFromTemplate;
import org.wso2.carbon.business.rules.core.internal.exceptions.TemplateManagerException;
import org.wso2.carbon.business.rules.core.internal.bean.RuleTemplate;
import org.wso2.carbon.business.rules.core.internal.bean.RuleTemplateProperty;
import org.wso2.carbon.business.rules.core.internal.bean.Template;
import org.wso2.carbon.business.rules.core.internal.bean.TemplateGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Consists of methods for additional features for the exposed Template Manager service
 */
//TODO : Verify class names
public class TemplateManagerHelper {
    private static final Logger log = LoggerFactory.getLogger(TemplateManagerHelper.class);

    /**
     * To avoid instantiation
     */
    private TemplateManagerHelper() {

    }

    /**
     * Converts given JSON File to a JSON object
     *
     * @param jsonFile Given JSON File
     * @return JSON object
     */
    public static JsonObject fileToJson(File jsonFile) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        JsonObject jsonObject = null;

        try {
            Reader reader = new FileReader(jsonFile);
            jsonObject = gson.fromJson(reader, JsonObject.class);
        } catch (FileNotFoundException e) {
            //log.error("FileNotFound Exception occurred when converting JSON file to JSON Object", e); //todo: FileNotFound exception occured. error message?
            log.error(e.getMessage(),e);
        }

        return jsonObject;
    }

    /**
     * Converts given JSON object to TemplateGroup object
     *
     * @param jsonObject Given JSON object
     * @return TemplateGroup object
     */
    public static TemplateGroup jsonToTemplateGroup(JsonObject jsonObject) {
        String templateGroupJsonString = jsonObject.get("templateGroup").toString();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        TemplateGroup templateGroup = gson.fromJson(templateGroupJsonString, TemplateGroup.class);

        return templateGroup;
    }

    /**
     * Converts given String JSON definition to TemplateGroup object
     *
     * @param jsonDefinition Given String JSON definition
     * @return TemplateGroup object
     */
    public static TemplateGroup jsonToTemplateGroup(String jsonDefinition) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        TemplateGroup templateGroup = gson.fromJson(jsonDefinition, TemplateGroup.class);

        return templateGroup;
    }

    /**
     * Converts given JSON object to BusinessRuleFromTemplate object
     *
     * @param jsonObject Given JSON object
     * @return BusinessRuleFromTemplate object
     */
    public static BusinessRuleFromTemplate jsonToBusinessRuleFromTemplate(JsonObject jsonObject) {
        String businessRuleJsonString = jsonObject.get("businessRule").toString();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        BusinessRuleFromTemplate businessRuleFromTemplate = gson.fromJson(businessRuleJsonString, BusinessRuleFromTemplate.class);

        return businessRuleFromTemplate;
    }

    /**
     * Converts given JSON object to BusinessRuleFromScratch object
     *
     * @param jsonObject Given JSON object
     * @return BusinessRuleFromTemplate object
     */
    public static BusinessRuleFromScratch jsonToBusinessRuleFromScratch(JsonObject jsonObject) {
        String businessRuleJsonString = jsonObject.get("businessRule").toString();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        BusinessRuleFromScratch businessRuleFromScratch = gson.fromJson(businessRuleJsonString, BusinessRuleFromScratch.class);

        return businessRuleFromScratch;
    }

    /**
     * Converts given String JSON definition to BusinessRuleFromTemplate object
     *
     * @param jsonDefinition Given String JSON definition
     * @return TemplateGroup object
     */
    public static BusinessRuleFromTemplate jsonToBusinessRuleFromTemplate(String jsonDefinition) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        BusinessRuleFromTemplate businessRuleFromTemplate = gson.fromJson(jsonDefinition, BusinessRuleFromTemplate.class);

        return businessRuleFromTemplate;
    }

    /**
     * Converts given String JSON definition to BusinessRuleFromScratch object
     *
     * @param jsonDefinition Given String JSON definition
     * @return TemplateGroup object
     */
    public static BusinessRuleFromScratch jsonToBusinessRuleFromScratch(String jsonDefinition) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        BusinessRuleFromScratch businessRuleFromScratch = gson.fromJson(jsonDefinition, BusinessRuleFromScratch.class);

        return businessRuleFromScratch;
    }

    /**
     * Checks whether a given TemplateGroup object has valid content
     * Validation criteria : //todo: Implement properly
     * - name is available
     * - At least one ruleTemplate is available
     *
     * @param templateGroup
     * @throws TemplateManagerException
     */
    public static void validateTemplateGroup(TemplateGroup templateGroup) throws TemplateManagerException {
        try { // todo: remove this. This is just temporary
            if (templateGroup.getName() == null) {
                throw new TemplateManagerException("Invalid TemplateGroup configuration file found");
            }
            if (!(templateGroup.getRuleTemplates().size() > 0)) {
                throw new TemplateManagerException("Invalid TemplateGroup configuration file found");
            }
            for (RuleTemplate ruleTemplate : templateGroup.getRuleTemplates()) {
                validateRuleTemplate(ruleTemplate);
            }
        } catch (TemplateManagerException x) {
            // System.out.println("TemplateGroup Not Valid");
            // todo: implement
        }

    }

    /**
     * Checks whether a given RuleTemplate object has valid content
     * Validation Criteria : todo: confirm validation criteria for RuleTemplate
     * - name is available
     * - type is either 'app', 'source' or 'sink' todo: template / input / output
     * todo: if input/output => ExposedStreamDefinition is not null
     * - At least one template available
     * - At least one property available
     * - All properties have defaultValue
     * - Each property of type 'option' should have at least one option
     * - Each template type is either 'siddhiApp', 'gadget' or 'dashboard'
     * - Each templated element in each template, should have a matching property
     *
     * @param ruleTemplate
     * @throws TemplateManagerException
     */
    public static void validateRuleTemplate(RuleTemplate ruleTemplate) throws TemplateManagerException {
        ArrayList<String> validTemplateTypes = new ArrayList<String>(Arrays.asList(TemplateManagerConstants.SIDDHI_APP_TEMPLATE_TYPE, "gadget", "dashboard")); //todo: more types might come

        if (ruleTemplate == null) {
            // todo: throw exception
        }
        if (ruleTemplate.getName() == null) {
            // todo: throw exception
        }
        if (!(ruleTemplate.getName().equals("app") || ruleTemplate.getName().equals("source") || ruleTemplate.getName().equals("sink"))) {
            // todo: throw exception
        }
        if (ruleTemplate.getTemplates().size() < 1) {
            // todo: throw exception
        }
        if (ruleTemplate.getProperties().size() < 1) {
            // todo: throw exception
        }
        for (String property : ruleTemplate.getProperties().keySet()) {
            validateRuleTemplateProperty(ruleTemplate.getProperties().get(property));
            // If template type is not valid
//            if (!validTemplateTypes.contains(ruleTemplate.getProperties().get(property).getType())) {
//                // todo: throw exception
//            }
        }
        validateTemplatesAndProperties(ruleTemplate.getTemplates(), ruleTemplate.getProperties());
    }

    /**
     * Checks whether a given ruleTemplateProperty object has valid content
     * Validation Criteria :
     * - All properties have defaultValue
     * - Each ruleTemplateProperty of type 'option' should have at least one option
     *
     * @param ruleTemplateProperty
     * @throws TemplateManagerException
     */
    public static void validateRuleTemplateProperty(RuleTemplateProperty ruleTemplateProperty) throws TemplateManagerException { //todo: conversion null pointer exception
        if (ruleTemplateProperty.getDefaultValue() == null) {
            // todo: throw exception
        }
    }

    /**
     * Checks whether all the templated elements of each template, has matching values in properties
     * todo: no need for this. Since we have the JS to do processing with entered values
     *
     * @param templates  Templates
     * @param properties RuleTemplateProperty names, denoting RuleTemplateProperty objects
     * @throws TemplateManagerException
     */
    public static void validateTemplatesAndProperties(Collection<Template> templates, Map<String, RuleTemplateProperty> properties) throws TemplateManagerException {
        Collection<String> templatedElements = new ArrayList();

        // todo: implement

        // All templated elements are not given in properties
        if (!properties.keySet().containsAll(templatedElements)) {
            // todo: throw exception
        }
    }

    /**
     * Checks whether a given Template file has valid content.
     * Validation criteria : //todo: confirm validation criteria for templates
     * - name
     * - maximumInstances
     * - maxNumberOfNodes
     * - javascript
     * - siddhiApps
     * - properties //todo: validate whether all templated elements are referred as properties?
     *
     * @param template Given Template object
     * @throws TemplateManagerException
     */
    public static void validateTemplate(Template template) throws TemplateManagerException {
        //todo: no need mostly.
    }

    public static void validateBusinessRuleFromTemplate(BusinessRuleFromTemplate businessRuleFromTemplate) throws TemplateManagerException {
        // todo: implement
    }

    public static void validateBusinessRuleFromScratch(BusinessRuleFromScratch businessRuleFromScratch) throws TemplateManagerException {
        // todo: implement
    }

    /**
     * Generates UUID for the given Template todo: figure out the needs
     *
     * @param template
     * @return
     */
    public static String generateUUID(Template template) throws TemplateManagerException {
        // SiddhiApp Template
        if (template.getType().equals(TemplateManagerConstants.SIDDHI_APP_TEMPLATE_TYPE)) {
            return getSiddhiAppName(template);
        }
        // Other template types are not considered for now
        throw new TemplateManagerException("Invalid template type. Unable to generate UUID"); // todo: (Q) is this correct?
    }

    /**
     * Gives the name of the given Template, which is a SiddhiApp
     *
     * @param siddhiAppTemplate
     * @return
     * @throws TemplateManagerException
     */
    public static String getSiddhiAppName(Template siddhiAppTemplate) throws TemplateManagerException {
        // Content of the SiddhiApp
        String siddhiApp = siddhiAppTemplate.getContent();
        // Regex match and find name
        Pattern siddhiAppNamePattern = Pattern.compile(TemplateManagerConstants.SIDDHI_APP_NAME_REGEX_PATTERN);
        Matcher siddhiAppNameMatcher = siddhiAppNamePattern.matcher(siddhiApp);
        if (siddhiAppNameMatcher.find()) {
            return siddhiAppNameMatcher.group(1);
        }

        throw new TemplateManagerException("Invalid SiddhiApp Name Found");
    }

    /**
     * Generates UUID from the given values, entered for the BusinessRuleFromTemplate todo: figure out usages
     * todo: This will be only called after user's form values come from the API (Read below)
     * 1. User enters values (propertyName : givenValue)
     * 2. TemplateGroupName, and RuleTemplateName is already there
     * 3. A Map with above details will be given from the API, to the backend
     * 4. These details are combined and the UUID is got
     * 5. BR object is created with those entered values, + the uuid in the backend
     *
     * @param givenValuesForBusinessRule
     * @return
     */
    public static String generateUUID(Map<String, String> givenValuesForBusinessRule) {
        return UUID.nameUUIDFromBytes(givenValuesForBusinessRule.toString().getBytes()).toString();
    }

    /**
     * Generates UUID, which only contains lowercase and hyphens,
     * from a TemplateGroup name todo: RuleTemplate name
     *
     * @param nameWithSpaces
     * @return
     */
    public static String generateUUID(String nameWithSpaces) {
        return nameWithSpaces.toLowerCase().replace(' ', '-');
    }

    /**
     * Replaces values with the given regex pattern in a given string, with provided replacement values
     *
     * @param stringWithRegex
     * @param regexPatternString
     * @param replacementValues
     * @return
     */
    public static String replaceRegex(String stringWithRegex, String regexPatternString, Map<String, String> replacementValues) {
        StringBuffer replacedString = new StringBuffer();

        Pattern regexPattern = Pattern.compile(regexPatternString);
        Matcher regexMatcher = regexPattern.matcher(stringWithRegex);

        // When an element with regex is is found
        while (regexMatcher.find()) {
            String elementToReplace = regexMatcher.group(1);
            String elementReplacement = replacementValues.get(elementToReplace);
            // Replace element with regex, with the found replacement
            regexMatcher.appendReplacement(replacedString, elementReplacement);
        }
        regexMatcher.appendTail(replacedString);

        return replacedString.toString();
    }

    /**
     * Creates a BusinessRule object from a map of entered values, and the recieved RuleTemplate
     *
     * @param ruleTemplate
     * @param enteredValues
     * @return
     */
    public static BusinessRule createBusinessRuleFromScratch(RuleTemplate ruleTemplate, Map<String, String> enteredValues){
        // Values required for final replacement
        Map<String, String> valuesForReplacement = enteredValues;

        // Replace templated elements in the script
        String script = ruleTemplate.getScript();



        return null;
    }
}
