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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.internal.bean.BusinessRule;
import org.wso2.carbon.business.rules.core.internal.bean.RuleTemplate;
import org.wso2.carbon.business.rules.core.internal.bean.RuleTemplateProperty;
import org.wso2.carbon.business.rules.core.internal.bean.Template;
import org.wso2.carbon.business.rules.core.internal.bean.TemplateGroup;
import org.wso2.carbon.business.rules.core.internal.bean.businessRulesFromScratch.BusinessRuleFromScratch;
import org.wso2.carbon.business.rules.core.internal.bean.businessRulesFromTemplate.BusinessRuleFromTemplate;
import org.wso2.carbon.business.rules.core.internal.exceptions.TemplateManagerException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

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
            log.error(e.getMessage(), e);
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
                throw new TemplateManagerException("Invalid TemplateGroup configuration file found - TemplateGroup " +
                        "name  is null" +
                        " ");
            }
            if (templateGroup.getUuid() == null) {
                throw new TemplateManagerException("Invalid TemplateGroup configuration file found - UUID is null for" +
                        " templateGroup " + templateGroup.getName());
            }
            if (!(templateGroup.getRuleTemplates().size() > 0)) {
                throw new TemplateManagerException("Invalid TemplateGroup configuration file found - No ruleTemplate" +
                        " configurations found for templateGroup ");
            }
            for (RuleTemplate ruleTemplate : templateGroup.getRuleTemplates()) {
                validateRuleTemplate(ruleTemplate);
            }
        } catch (TemplateManagerException e) {
            // System.out.println("TemplateGroup Not Valid");
            // todo:
            log.error(e.getMessage(), e);

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
        ArrayList<String> validTemplateTypes = new ArrayList<String>(Arrays.asList(TemplateManagerConstants
                .SIDDHI_APP_TEMPLATE_TYPE, TemplateManagerConstants.GADGET, TemplateManagerConstants.DASHBOARD));
        //todo: more
        // types might come
        if (ruleTemplate.getUuid() == null) {
            throw new TemplateManagerException("Invalid rule template - rule template uuid is null ");
        }
        if (ruleTemplate.getName() == null) {
            throw new TemplateManagerException("Invalid rule template - rule template name is null in " +
                    ruleTemplate.getUuid());
        }

        if (ruleTemplate.getType() == null) {
            throw new TemplateManagerException("Invalid rule template - rule template type is null for rule template " +
                    "" + ruleTemplate.getUuid());
        }
        if (!(ruleTemplate.getType().equals(TemplateManagerConstants.INPUT) || ruleTemplate.getType().equals
                (TemplateManagerConstants.OUTPUT) ||
                ruleTemplate.getType()
                        .equals(TemplateManagerConstants.TEMPLATE))) {
            throw new TemplateManagerException("Invalid rule template - invalid rule template type for rule template " +
                    "" + ruleTemplate.getUuid());
        }
        if (ruleTemplate.getType().equals(TemplateManagerConstants.INPUT) || ruleTemplate.getType().equals
                (TemplateManagerConstants.OUTPUT)) {
            if (ruleTemplate.getTemplates().size() != 1) {
                throw new TemplateManagerException("Invalid rule template - there should exactly one template for " +
                        "rule template " + ruleTemplate.getUuid());
            }
        } else {
            if (ruleTemplate.getTemplates().size() == 0) {

                throw new TemplateManagerException("Invalid rule template - No templates found in rule template "
                        + ruleTemplate.getUuid());
            }
        }

        validateTemplatesProperties(ruleTemplate);
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
     * @param ruleTemplate Templates
     * @throws TemplateManagerException
     */
    public static void validateTemplatesProperties(RuleTemplate ruleTemplate) throws TemplateManagerException {
        // TODO: 9/19/17 Pass ruleTemplate and if there is a script, validate with that, else use this.
        Collection<Template> templates = ruleTemplate.getTemplates();
        Map<String, RuleTemplateProperty> properties = ruleTemplate.getProperties();
        Collection<String> templatedElements = new ArrayList();

        // todo: implement

        // All templated elements are not given in properties
        // TODO: 9/19/17 if no script
        if (!properties.keySet().containsAll(templatedElements)) {
            throw new TemplateManagerException("All templated elements are not defined in properties");
            // TODO: 9/19/17 pass the not implemented template field as well
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
    public static String replaceRegex(String stringWithRegex, String regexPatternString, Map<String, String> replacementValues) throws TemplateManagerException{
        StringBuffer replacedString = new StringBuffer();

        Pattern regexPattern = Pattern.compile(regexPatternString);
        Matcher regexMatcher = regexPattern.matcher(stringWithRegex);

        // When an element with regex is is found
        while (regexMatcher.find()) {
            String elementToReplace = regexMatcher.group(1);
            String elementReplacement = replacementValues.get(elementToReplace);
            // No replacement found in the given map
            if(elementReplacement == null){
                throw new TemplateManagerException("No matching replacement found for the value - "+elementToReplace);
            }
            // Replace element with regex, with the found replacement
            regexMatcher.appendReplacement(replacedString, elementReplacement);
        }
        regexMatcher.appendTail(replacedString);

        return replacedString.toString();
    }

    /**
     * Creates a BusinessRule object from a map of entered values, and the recieved RuleTemplate
     *
     * @param ruleTemplate  todo: might not need this method
     * @param enteredValues
     * @return
     */
    public static BusinessRule createBusinessRuleFromTemplateDefinition(RuleTemplate ruleTemplate, Map<String, String> enteredValues) {
        // Values required for replacement. Values processed by the script will be added to this
        Map<String, String> valuesForReplacement = enteredValues;
        // Script with templated elements
        String scriptWithTemplatedElements = ruleTemplate.getScript();


        return null;
    }

    /**
     * Runs the script that is given as a string, and gives all the variables specified in the script
     *
     * @param script
     * @return
     * @throws TemplateManagerException
     */
    public static Map<String, String> getScriptGeneratedVariables(String script) throws TemplateManagerException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        ScriptContext scriptContext = new SimpleScriptContext();
        scriptContext.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
        try {
            // Run script
            engine.eval(script);
            Map<String, Object> returnedScriptContextBindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);

            // Store binding variable values returned as objects, as strings
            Map<String,String> variableValues = new HashMap<String,String>();
            for (String variableName : returnedScriptContextBindings.keySet()) {
                variableValues.put(variableName, returnedScriptContextBindings.get(variableName).toString());
            }

            return variableValues;
        } catch (ScriptException e) {
            throw new TemplateManagerException("Error running the script :\n" + script + '\n', e);
        }
    }
}
