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
         * @class Stream
         * @constructor
         * @class Stream  Creates a Stream
         * @param {Object} options Rendering options for the view
         */
        var Stream = function (options) {
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
                annotationList: [annotation1, annotation2, ...]
            */
            if (options !== undefined) {
                this.id = options.id;
                this.name = options.name;
            }
            this.attributeList = [];
            this.annotationList = [];
        };

        Stream.prototype.addAttribute = function (attribute) {
            this.attributeList.push(attribute);
        };

        Stream.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Stream.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Stream.prototype.clearAttributeList = function () {
            ElementUtils.prototype.removeAllElements(this.attributeList);
        };

        Stream.prototype.getId = function () {
            return this.id;
        };

        Stream.prototype.getName = function () {
            return this.name;
        };

        Stream.prototype.getAttributeList = function () {
            return this.attributeList;
        };

        Stream.prototype.getAnnotationList = function () {
            return this.annotationList;
        };

        Stream.prototype.setId = function (id) {
            this.id = id;
        };

        Stream.prototype.setName = function (name) {
            this.name = name;
        };

        Stream.prototype.setAttributeList = function (attributeList) {
            this.attributeList = attributeList;
        };

        Stream.prototype.setAnnotationList = function (annotationList) {
            this.annotationList = annotationList;
        };

        return Stream;

    });
