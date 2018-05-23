/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['require', 'elementUtils'],
    function (require, ElementUtils) {

        /**
         * @class Window
         * @constructor
         * @class Window  Creates a Window
         * @param {Object} options Rendering options for the view
         */
        var Window = function (options) {
            /*
             Data storing structure as follows
                id: '',
                name: '',
                attributeList: [
                    {
                        name: ‘’,
                        type: ‘’
                    }
                ],
                function*: ‘time|length|timeBatch|lengthBatch...’,
                parameters*: ['value1',...],
                outputEventType: ‘CURRENT_EVENTS|EXPIRED_EVENTS|ALL_EVENTS’,
                annotationList: [
                    {
                        name: ‘’,
                        type: ‘VALUE’,
                        value: [‘value1’,’value2’]
                    },
                    and|or
                    {
                        name: ‘’
                        type: ‘KEY_VALUE’,
                        value: {‘option’:’value’}
                    }
                ]
            */
            if (options !== undefined) {
                this.id = options.id;
                this.name = options.name;
                this.function = options.function;
                this.parameters = options.parameters;
                this.outputEventType
                    = (options.outputEventType !== undefined)? (options.outputEventType).toUpperCase() : undefined;
            }
            this.attributeList = [];
            this.annotationList =  [];
        };

        Window.prototype.addAttribute = function (attribute) {
            this.attributeList.push(attribute);
        };

        Window.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Window.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Window.prototype.clearAttributeList = function () {
            ElementUtils.prototype.removeAllElements(this.attributeList);
        };

        Window.prototype.getId = function () {
            return this.id;
        };

        Window.prototype.getName = function () {
            return this.name;
        };

        Window.prototype.getAttributeList = function () {
            return this.attributeList;
        };

        Window.prototype.getFunction = function () {
            return this.function;
        };

        Window.prototype.getParameters = function () {
            return this.parameters;
        };

        Window.prototype.getOutputEventType = function () {
            return this.outputEventType;
        };

        Window.prototype.getAnnotationList = function () {
            return this.annotationList;
        };

        Window.prototype.setId = function (id) {
            this.id = id;
        };

        Window.prototype.setName = function (name) {
            this.name = name;
        };

        Window.prototype.setAttributeList = function (attributeList) {
            this.attributeList = attributeList;
        };

        Window.prototype.setFunction = function (functionName) {
            this.function = functionName;
        };

        Window.prototype.setParameters = function (parameters) {
            this.parameters = parameters;
        };

        Window.prototype.setOutputEventType = function (outputEventType) {
            this.outputEventType = outputEventType;
        };

        Window.prototype.setAnnotationList = function (annotationList) {
            this.annotationList = annotationList;
        };

        return Window;

    });
