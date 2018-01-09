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

/**
 * Has values for all the constants related to Templates Editor
 */
const TemplatesEditorConstants = {
    TEMPLATED_ELEMENT_REGEX: /\$\{(\S+)\}/g,
    STREAM_DEFINITION_REGEX: /define stream.*?\((.*?)\);/g,

    // Skeletons
    TEMPLATE_GROUP_SKELETON: {
        uuid: '',
        name: '',
        description: '',
        ruleTemplates: [
            {
                name: '',
                uuid: '',
                type: '',
                instanceCount: '',
                description: '',
                script: '',
                templates: [
                    {
                        'type': 'siddhiApp',
                        'content': '',
                    },
                ],
                properties: {

                },
            },
        ],
    },
    RULE_TEMPLATE_SKELETON: {
        uuid: '',
        name: '',
        description: '',
        type: '',
        instanceCount: '',
        script: '',
        templates: [
            {
                'type': 'siddhiApp',
                'content': '',
            },
        ],
        properties: {},
    },
    PROPERTY_SKELETON: {
        fieldName: '',
        description: '',
        defaultValue: '',
    }
};

export default TemplatesEditorConstants;
