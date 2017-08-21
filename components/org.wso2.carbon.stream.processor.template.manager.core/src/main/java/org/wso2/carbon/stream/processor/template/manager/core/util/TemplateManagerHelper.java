package org.wso2.carbon.stream.processor.template.manager.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Consists of methods for additional features for the exposed root.Template Manager service
 */
public class TemplateManagerHelper {
    //private static final Log log = LogFactory.getLog(TemplateManagerHelper.class);

    /**
     * To avoid instantiation
     */
    private TemplateManagerHelper() {

    }

    // todo : hardcoded test. remove main()
    public static void main(String[] args) throws TemplateManagerException {

        File directory = new File(TemplateManagerConstants.TEMPLATES_DIRECTORY);
        Collection<RuleCollection> availableRuleCollections = new ArrayList();

        // To store files from the directory
        File[] files = directory.listFiles();
        if (files != null) {
            for (final File fileEntry : files) {
                // If file is a valid json file
                if (fileEntry.isFile() && fileEntry.getName().endsWith("json")) {
                    RuleCollection ruleCollection = null;
                    // Convert to RuleCollection object
                    try {
                        ruleCollection = TemplateManagerHelper.jsonToRuleCollection(TemplateManagerHelper.fileToJson(fileEntry));
                    } catch (NullPointerException ne) {
                        System.out.println("Unable to convert RuleCollection file : " + fileEntry.getName() + " " + ne);
                    }

                    // Validate contents of the object
                    if (ruleCollection != null) {
                        try {
                            TemplateManagerHelper.validateRuleCollection(ruleCollection);
                            // Add only valid RuleCollections to the template
                            availableRuleCollections.add(ruleCollection);
                        } catch (TemplateManagerException e) { //todo: implement properly
                            // Files with invalid content are not added.
                            System.out.println("Invalid Rule Collection configuration file found : " + fileEntry.getName() + e);
                        }
                    } else {
                        System.out.println("Invalid Rule Collection configuration file found : " + fileEntry.getName());
                    }

                }
            }
        }

        System.out.println(availableRuleCollections.size()+" Templates Found");

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
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * Converts given JSON object to RuleCollection object
     *
     * @param jsonObject Given JSON object
     * @return RuleCollection object
     */
    public static RuleCollection jsonToRuleCollection(JsonObject jsonObject) {
        String ruleCollectionJsonString = jsonObject.get("ruleCollection").toString();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        RuleCollection ruleCollection = gson.fromJson(ruleCollectionJsonString, RuleCollection.class);

        return ruleCollection;
    }

    /**
     * Converts given String JSON definition to RuleCollection object
     *
     * @param jsonDefinition Given String JSON definition
     * @return RuleCollection object
     */
    public static RuleCollection jsonToRuleCollection(String jsonDefinition) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        RuleCollection ruleCollection = gson.fromJson(jsonDefinition, RuleCollection.class);

        return ruleCollection;
    }

    /**
     * Converts given JSON object to BusinessRule object
     *
     * @param jsonObject Given JSON object
     * @return BusinessRule object
     */
    public static BusinessRule jsonToBusinessRule(JsonObject jsonObject) {
        String businessRuleJsonString = jsonObject.get("businessRule").toString();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        BusinessRule businessRule = gson.fromJson(businessRuleJsonString, BusinessRule.class);

        return businessRule;
    }

    /**
     * Converts given String JSON definition to RuleCollection object
     *
     * @param jsonDefinition Given String JSON definition
     * @return RuleCollection object
     */
    public static BusinessRule jsonToBusinessRule(String jsonDefinition) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        BusinessRule businessRule = gson.fromJson(jsonDefinition, BusinessRule.class);

        return businessRule;
    }

    /**
     * Checks whether a given RuleCollection object has valid content
     * Validation criteria : //todo: confirm
     * - name is available
     * - At least one ruleTemplate is available
     *
     * @param ruleCollection
     * @throws TemplateManagerException
     */
    public static void validateRuleCollection(RuleCollection ruleCollection) throws TemplateManagerException {
        try{ // todo: remove this. This is just temporary
            if (ruleCollection.getName() == null) {
                throw new TemplateManagerException("Invalid RuleCollection configuration file found");
            }
            if (!(ruleCollection.getRuleTemplates().size() > 0)) {
                throw new TemplateManagerException("Invalid RuleCollection configuration file found");
            }
            for (RuleTemplate ruleTemplate : ruleCollection.getRuleTemplates()) {
                validateRuleTemplate(ruleTemplate);
            }
        }catch(TemplateManagerException x){
            System.out.println("RuleCollection Not Valid");
        }

    }

    /**
     * Checks whether a given RuleTemplate object has valid content
     * Validation Criteria : todo: cofirm validation criteria for RuleTemplate
     * - name is available
     * - type is either 'app', 'source' or 'sink'
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
        ArrayList<String> validTemplateTypes = new ArrayList<String>(Arrays.asList("siddhiApp", "gadget", "dashboard")); //todo: more types might come

        if(ruleTemplate == null){
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
            validateProperty(ruleTemplate.getProperties().get(property));
            // If template type is not valid
            if (!validTemplateTypes.contains(ruleTemplate.getProperties().get(property).getType())) {
                // todo: throw exception
            }
        }
        validateTemplatesAndProperties(ruleTemplate.getTemplates(), ruleTemplate.getProperties());
    }

    /**
     * Checks whether a given property object has valid content
     * Validation Criteria :
     * - All properties have defaultValue
     * - Each property of type 'option' should have at least one option
     *
     * @param property
     * @throws TemplateManagerException
     */
    public static void validateProperty(Property property) throws TemplateManagerException { //todo: conversion null pointer exception
        if (property.getDefaultValue() == null) {
            // todo: throw exception
        }
        if (property.getType().equals("option") && (property.getOptions() == null || property.getOptions().size() < 1)) {
            // todo: throw exception
        }
    }

    /**
     * Checks whether all the templated elements of each template, has matching values in properties
     *
     * @param templates  Templates
     * @param properties Property names, denoting Property objects
     * @throws TemplateManagerException
     */
    public static void validateTemplatesAndProperties(Collection<Template> templates, Map<String, Property> properties) throws TemplateManagerException {
        Collection<String> templatedElements = new ArrayList();

        // Add all templated elements to Collection
        for (Template template : templates) {
            String templatedContent = template.getContent();

            // Find all templated elements from the siddhiApp
            Pattern templatedElementPattern = Pattern.compile(TemplateManagerConstants.TEMPLATED_ELEMENT_REGEX_PATTERN);
            Matcher templatedElementMatcher = templatedElementPattern.matcher(templatedContent);

            // When each templated element is found
            while (templatedElementMatcher.find()) {
                // Add templated element (inclusive of template pattern)
                String templatedElement = templatedElementMatcher.group(1);

                // Find Templated Element's Name
                Pattern templatedElementNamePattern = Pattern.compile(TemplateManagerConstants.TEMPLATED_ELEMENT_NAME_REGEX_PATTERN);
                Matcher templatedElementNameMatcher = templatedElementNamePattern.matcher(templatedElement);

                // When the Templated Element's Name is found
                if (templatedElementNameMatcher.find()) {
                    // Templated Element's Name
                    String templatedElementName = templatedElementNameMatcher.group(1);

                    templatedElements.add(templatedElementName);
                }
            }

        }

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
}
