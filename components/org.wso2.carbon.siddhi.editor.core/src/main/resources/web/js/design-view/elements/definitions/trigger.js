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
         * @class Trigger
         * @constructor
         * @class Trigger  Creates a Trigger
         * @param {Object} options Rendering options for the view
         */
        var Trigger = function (options) {
            /*
             Data storing structure as follows
                id*: '',
                name*: '',
                at*: ‘’
                annotationList: [annotation1, annotation2, ...]
            */
            if (options !== undefined) {
                this.id = options.id;
                this.name = options.name;
                this.at = options.at;
            }
            this.annotationList =  [];
        };

        Trigger.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Trigger.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Trigger.prototype.getId = function () {
            return this.id;
        };

        Trigger.prototype.getName = function () {
            return this.name;
        };

        Trigger.prototype.getAt = function () {
            return this.at;
        };

        Trigger.prototype.getAnnotationList = function () {
            return this.annotationList;
        };

        Trigger.prototype.setId = function (id) {
            this.id = id;
        };

        Trigger.prototype.setName = function (name) {
            this.name = name;
        };

        Trigger.prototype.setAt = function (at) {
            this.at = at;
        };

        Trigger.prototype.setAnnotationList = function (annotationList) {
            this.annotationList = annotationList;
        };

        return Trigger;

    });
