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

import { Validator } from 'jsonschema';
import TemplateEditorConstants from '../constants/TemplateEditorConstants';

class TemplateEditorUtilityFunctions {
    /**
     * Gets matching elements for the provided regex in the given text, as an array
     * If regex pattern is for ${templatedElement}, captures group 1. Otherwise, full match
     * @param text
     * @param regex
     * @returns {Array}
     */
    static getRegexMatches(text, regex) {
        const matches = [];
        let matcher;
        do {
            matcher = regex.exec(text);
            if (matcher) {
                if (regex === TemplateEditorConstants.TEMPLATED_ELEMENT_REGEX) {
                    matches.push(matcher[1]);
                } else {
                    matches.push(matcher[0]);
                }
            }

        } while (matcher);
        return matches;
    }

    /**
     * Checks whether a given object is empty or not
     * @param object
     * @returns {boolean}
     */
    static isEmpty(object) {
        for (let key in object) {
            if (object.hasOwnProperty(key))
                return false;
        }
        return true;
    }

    /**
     * Calculates reading time of a message in milliseconds, for efficiently producing error display timeouts
     * @param text
     * @returns {number}
     */
    static calculateReadingTime(text) {
        const wordsPerMinute = 140;
        const noOfWords = text.split(/\s/g).length;
        const millis = (noOfWords / wordsPerMinute) * 60000;
        return Math.ceil(millis);
    }

    /**
     * Detects distinct templated elements from SiddhiApp templates.
     * Only the first SiddhiApp template is considered when the rule template is of type 'input' or 'output'
     * @param ruleTemplateConfig
     * @param scriptAdditionsBin
     * @returns {Array}
     */
    static detectTemplatedElementsFromTemplates(ruleTemplateConfig, scriptAdditionsBin) {
        const properties = [];
        for (let i = 0; i < ruleTemplateConfig.templates.length; i++) {
            const templatedElements = TemplateEditorUtilityFunctions.getRegexMatches(
                ruleTemplateConfig.templates[i].content,
                TemplateEditorConstants.TEMPLATED_ELEMENT_REGEX);
            for (const templatedElement of templatedElements) {
                // To avoid re-generating same elements
                if (properties.indexOf(templatedElement) === -1) {
                    // To avoid re-generation for elements that are already available in properties,
                    // or added to script.
                    // However, deleting variables from a script doesn't remove items from the bin
                    if (!Object.prototype.hasOwnProperty.call(ruleTemplateConfig.properties, templatedElement) &&
                        (scriptAdditionsBin.indexOf(templatedElement) === -1)) {
                        properties.push(templatedElement);
                    }
                }
            }
            if ((ruleTemplateConfig.type !== TemplateEditorConstants.RULE_TEMPLATE_TYPE_TEMPLATE) && (i === 0)) {
                break;
            }
        }
        return properties;
    }

    /**
     * Detects and returns distinct templated elements, from SiddhiApp template(s) and the script
     * @param ruleTemplateConfig
     * @param scriptAdditionsBin
     * @returns {Array}
     */
    static detectTemplatedElements(ruleTemplateConfig, scriptAdditionsBin) {
        const properties = [];
        const elementsFromTemplates =
            TemplateEditorUtilityFunctions.detectTemplatedElementsFromTemplates(ruleTemplateConfig, scriptAdditionsBin);
        for (const element of elementsFromTemplates) {
            properties.push(element);
        }
        // Add templated elements from the script
        if (ruleTemplateConfig.script) {
            const elementsFromScript = TemplateEditorUtilityFunctions.getRegexMatches(ruleTemplateConfig.script,
                TemplateEditorConstants.TEMPLATED_ELEMENT_REGEX);
            for (const templatedElement of elementsFromScript) {
                // To avoid re-generating same elements
                if (properties.indexOf(templatedElement) === -1) {
                    properties.push(templatedElement);
                }
            }
        }
        return properties;
    }

    /**
     * Validates the given templateGroup definition's structure.
     * Throws respective error if the structure is invalid, otherwise returns true
     * @param templateGroupDefinition
     * @returns {boolean}
     */
    static isSkeletonValid(templateGroupDefinition) {
        let validator = new Validator();
        validator.addSchema(TemplateEditorConstants.RULE_TEMPLATE_SCHEMA, '/ruleTemplateSchema');
        validator.addSchema(TemplateEditorConstants.TEMPLATE_SCHEMA, '/templateSchema');
        validator.addSchema(TemplateEditorConstants.PROPERTY_SCHEMA, '/propertySchema');
        let validatorResult =
            validator.validate(templateGroupDefinition, TemplateEditorConstants.TEMPLATE_GROUP_SCHEMA);
        if (validatorResult.errors.length > 0) {
            throw validatorResult.errors[0].stack;
        }
        return true;
    }

    /**
     * Validates whether the given template group definition is complete,
     * as a usable template in the Business Rules Manager.
     * Throws respective error when not complete, otherwise returns true
     * @param definition
     * @param scriptAdditionBins
     * @returns {boolean}
     */
    static isComplete(definition, scriptAdditionBins) {
        if (definition.uuid === '') {
            throw `'uuid' cannot be empty for the template group`;
        }
        // Validate for accepted characters
        let uuidMatches = TemplateEditorUtilityFunctions
            .getRegexMatches(definition.uuid, TemplateEditorConstants.VALID_UUID_REGEX);
        if ((uuidMatches.length !== 1) || (uuidMatches[0] !== definition.uuid)) {
            throw `'uuid' doesn't match regex ${TemplateEditorConstants.VALID_UUID_REGEX}`;
        }
        if (definition.name === '') {
            throw `'name' cannot be empty for the template group`;
        }
        if (definition.ruleTemplates.length === 0) {
            throw 'There should be at least one rule template';
        }

        // Validate rule templates
        for (let i = 0; i < definition.ruleTemplates.length; i++) {
            const ruleTemplate = definition.ruleTemplates[i];
            if (ruleTemplate.uuid === '') {
                throw `'uuid' cannot be empty for ruleTemplates[${i}]`;
            }
            // Validate for accepted characters
            uuidMatches = TemplateEditorUtilityFunctions
                .getRegexMatches(ruleTemplate.uuid, TemplateEditorConstants.VALID_UUID_REGEX);
            if ((uuidMatches.length !== 1) || (uuidMatches[0] !== ruleTemplate.uuid)) {
                throw `'uuid' doesn't match regex ${TemplateEditorConstants.VALID_UUID_REGEX}`;
            }
            if (ruleTemplate.name === '') {
                throw `'name' cannot be empty for rule template '${ruleTemplate.uuid}'`;
            }
            if (ruleTemplate.type === '') {
                throw `'type' cannot be empty for rule template '${ruleTemplate.uuid}'`;
            }
            if (!((ruleTemplate.type === TemplateEditorConstants.RULE_TEMPLATE_TYPE_TEMPLATE) ||
                    (ruleTemplate.type === TemplateEditorConstants.RULE_TEMPLATE_TYPE_INPUT) ||
                    (ruleTemplate.type === TemplateEditorConstants.RULE_TEMPLATE_TYPE_OUTPUT))) {
                throw `'type' should be 'template' / 'input' / 'output' for rule template '${ruleTemplate.uuid}'`;
            }
            if (ruleTemplate.instanceCount === '') {
                throw `'instanceCount' cannot be blank for rule template '${ruleTemplate.uuid}'`;
            }
            if (!((ruleTemplate.instanceCount === TemplateEditorConstants.RULE_TEMPLATE_INSTANCE_COUNT_ONE) ||
                    (ruleTemplate.instanceCount === TemplateEditorConstants.RULE_TEMPLATE_INSTANCE_COUNT_MANY))) {
                throw `'instanceCount' should be one or many for rule template '${ruleTemplate.uuid}'`;
            }

            // Validate templates
            if (ruleTemplate.templates.length === 0) {
                throw `There should be at least one template under rule template '${ruleTemplate.uuid}'`;
            }

            for (let j = 0; j < ruleTemplate.templates.length; j++) {
                if (ruleTemplate.templates[j].content === '') {
                    throw `Cannot find valid content for templates[${j}] in rule template '${ruleTemplate.uuid}'`;
                }
                // Validate templates for input / output rule templates
                if ((ruleTemplate.type === TemplateEditorConstants.RULE_TEMPLATE_TYPE_INPUT) ||
                    (ruleTemplate.type === TemplateEditorConstants.RULE_TEMPLATE_TYPE_OUTPUT)) {
                    if (!Object.prototype.hasOwnProperty
                            .call(ruleTemplate.templates[j], 'exposedStreamDefinition')) {
                        throw `Cannot find 'exposedStreamDefinition' for templates[${j}]` +
                        `rule template '${ruleTemplate.uuid}'`;
                    }
                    if (ruleTemplate.templates[j].exposedStreamDefinition === '') {
                        throw `'exposedStreamDefinition' cannot be empty for rule template '${ruleTemplate.uuid}'`;
                    }
                    if (TemplateEditorUtilityFunctions.getRegexMatches(
                            ruleTemplate.templates[j].exposedStreamDefinition,
                            TemplateEditorConstants.STREAM_DEFINITION_REGEX).length !== 1) {
                        throw `Cannot find a valid stream definition in 'exposedStreamDefinition' of rule template` +
                        ` rule template '${ruleTemplate.uuid}'`;
                    }
                } else {
                    if (Object.prototype.hasOwnProperty
                            .call(ruleTemplate.templates[j], 'exposedStreamDefinition')) {
                        throw `Unnecessary 'exposedStreamDefinition' will be removed from  templates[${j}]` +
                        `of rule template '${ruleTemplate.uuid}'`;
                    }
                }
                if ((ruleTemplate.type !== TemplateEditorConstants.RULE_TEMPLATE_TYPE_TEMPLATE) && (j === 0)) {
                    break;
                }
            }

            // Validate properties
            if (TemplateEditorUtilityFunctions.isEmpty(ruleTemplate.properties)) {
                throw 'Cannot find properties';
            }
            for (const property in ruleTemplate.properties) {
                if (ruleTemplate.properties[property].fieldName === '') {
                    throw `Cannot find 'fieldName' for property '${property}' of ` +
                    `rule template '${ruleTemplate.uuid}'`;
                }
                if (ruleTemplate.properties[property].defaultValue === '') {
                    throw `Cannot find 'defaultValue' for property '${property}' of ` +
                    `rule template '${ruleTemplate.uuid}'`;
                }
                if (Object.prototype.hasOwnProperty.call(ruleTemplate.properties[property], 'options')) {
                    // Options are present
                    if (ruleTemplate.properties[property].options.length < 2) {
                        throw `There should be at least two options for property '${property}' ` +
                        `of rule template '${ruleTemplate.uuid}'`;
                    }
                    if (ruleTemplate.properties[property].options.
                        indexOf(ruleTemplate.properties[property].defaultValue) === -1) {
                        throw `'defaultValue' for property '${property}' of ` +
                        `rule template '${ruleTemplate.uuid}' should be one of its options`;
                    }
                }
            }
            // Validate templated elements from script against properties
            let scriptTemplatedElements = TemplateEditorUtilityFunctions.getRegexMatches(
                ruleTemplate.script, TemplateEditorConstants.TEMPLATED_ELEMENT_REGEX);
            for (let t = 0; t < scriptTemplatedElements.length; t++) {
                if (!Object.prototype.hasOwnProperty.call(ruleTemplate.properties, scriptTemplatedElements[t])) {
                    throw `Cannot find property for '${scriptTemplatedElements[t]}' from script, ` +
                    `of rule template [${i}]`;
                }
            }
            // Validate whether templated elements present in both the script & properties
            for (let t = 0; t < ruleTemplate.templates.length; t++ ) {
                const template = ruleTemplate.templates[t];
                let templatedElements = TemplateEditorUtilityFunctions.getRegexMatches(template.content,
                    TemplateEditorConstants.TEMPLATED_ELEMENT_REGEX);
                for (let t = 0; t < templatedElements.length; t++) {
                    if ((scriptAdditionBins.indexOf(templatedElements[t]) > -1)) {
                        if (Object.prototype.hasOwnProperty.call(ruleTemplate.properties, templatedElements[t])) {
                            // Templated element exists in both script & properties
                            throw `'${templatedElements[t]}' has been already referred in script.` +
                            ` It should be removed from properties of rule template '${ruleTemplate.uuid}'`;
                        }
                    }
                }
                if ((ruleTemplate.type !== TemplateEditorConstants.RULE_TEMPLATE_TYPE_TEMPLATE) && (t === 0)) {
                    if (ruleTemplate.templates.length > 1) {
                        throw `More than one template found in rule template '${ruleTemplate.uuid}'`;
                    }
                    break;
                }
            }
        }
        return true;
    }

    /**
     * Removes optional & left out elements, and returns the definition to be saved
     * @param definition
     */
    static prepareTemplateGroupDefinition(definition) {
        let configuration = JSON.parse(JSON.stringify(definition));
        if (!Object.prototype.hasOwnProperty.call(configuration, 'description') || configuration.description === '') {
            delete configuration.description;
        }
        // Rule Templates
        for (let i = 0; i < configuration.ruleTemplates.length; i++) {
            if (!Object.prototype.hasOwnProperty.call(configuration.ruleTemplates[i], 'description') ||
                configuration.ruleTemplates[i].description === '') {
                delete configuration.ruleTemplates[i].description;
            }
            // Remove exposed stream definition when rule template's type is 'template'
            if (configuration.ruleTemplates[i].type === TemplateEditorConstants.RULE_TEMPLATE_TYPE_TEMPLATE &&
                Object.prototype.hasOwnProperty.call(configuration.ruleTemplates[i].templates[0],
                    'exposedStreamDefinition')) {
                delete configuration.ruleTemplates[i].templates[0].exposedStreamDefinition;
            }
        }
        return configuration;
    }
}

export default TemplateEditorUtilityFunctions;
