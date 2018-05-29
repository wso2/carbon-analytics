/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Has values for all the constants related to the Template Editor
 */
const TemplateEditorConstants = {
    TEMPLATED_ELEMENT_REGEX: /\${([^\$\s]+)}/g,
    STREAM_DEFINITION_REGEX: /define stream.*?\((.*?)\);/g,
    VALID_UUID_REGEX: /[a-zA-Z][a-zA-Z0-9_-]*/g,

    // Rule Template types
    RULE_TEMPLATE_TYPE_TEMPLATE: 'template',
    RULE_TEMPLATE_TYPE_INPUT: 'input',
    RULE_TEMPLATE_TYPE_OUTPUT: 'output',

    // Rule Template instance counts
    RULE_TEMPLATE_INSTANCE_COUNT_ONE: 'one',
    RULE_TEMPLATE_INSTANCE_COUNT_MANY: 'many',

    // Skeletons
    TEMPLATE_GROUP_SKELETON: {
        uuid: '',
        name: '',
        description: '',
        ruleTemplates: [
            {
                uuid: '',
                name: '',
                description: '',
                type: '',
                instanceCount: '',
                script: '',
                templates: [
                    {
                        'type': 'siddhiApp',
                        'content': ''
                    }
                ],
                properties: {}
            }
        ]
    },
    PROPERTY_SKELETON: {
        fieldName: '',
        description: '',
        defaultValue: ''
    },

    // Schemas
    TEMPLATE_GROUP_SCHEMA: {
        "id": "/templateGroupSchema",
        "type": "object",
        "properties": {
            "uuid": {"type": "string", "required": true},
            "name": {"type": "string", "required": true},
            "description": {"type": "string"},
            "ruleTemplates": {
                "type": "array",
                "required": true,
                "minItems": 1,
                "uniqueItems": true,
                "items": {
                    "$ref": "/ruleTemplateSchema"
                }
            },
        }
    },
    RULE_TEMPLATE_SCHEMA: {
        "id": "/ruleTemplateSchema",
        "type": "object",
        "properties": {
            "uuid": {"type": "string", "required": true},
            "name": {"type": "string", "required": true},
            "description": {"type": "string"},
            "type": {
                "enum": ["template", "input", "output"],
                "type": "string",
                "required": true
            },
            "instanceCount": {
                "enum": ["one", "many"],
                "type": "string",
                "required": true
            },
            "script": {"type": "string"},
            "templates": {
                "type": "array",
                "required": true,
                "minItems": 1,
                "items": {
                    "$ref": "/templateSchema",
                    "type": "object"
                }
            },
            "properties": {
                "type": "object",
                "required": true,
                "minProperties": 1,
                "patternProperties": {
                    "[a-zA-Z0-9_]+": {
                        "$ref": "/propertySchema"
                    }
                }
            }
        }
    },
    TEMPLATE_SCHEMA: {
        "id": "/templateSchema",
        "type": "object",
        "properties": {
            "type": {"enum": ["siddhiApp"], "type": "string", "required": true},
            "content": {"type": "string", "required": true}
        }
    },
    PROPERTY_SCHEMA: {
        "id": "/propertySchema",
        "type": "object",
        "properties": {
            "fieldName": {"type": "string", "required": true},
            "description":  {"type": "string", "required": true},
            "options": {
                "type": "array",
                "minItems": 2,
                "uniqueItems": true,
            },
            "defaultValue": {"type": "string", "required": true}
        }
    }
};

export default TemplateEditorConstants;
