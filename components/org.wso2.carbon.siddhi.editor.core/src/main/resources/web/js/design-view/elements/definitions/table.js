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
         * @class Table
         * @constructor
         * @class Table  Creates a Table
         * @param {Object} options Rendering options for the view
         */
        var Table = function (options) {
            /*
             Data storing structure as follows
                id: '',
                previousCommentSegment:'',
                name: '',
                attributeList: [
                    {
                        name: ‘’,
                        type: ‘’
                    }
                ],
                store: {},
                annotationList: [annotation1, annotation2, ...]
            */
            if (options !== undefined) {
                this.id = options.id;
                this.previousCommentSegment = options.previousCommentSegment;
                this.name = options.name;
                this.store = options.store;
            }
            this.attributeList = [];
            this.annotationList = [];
        };

        Table.prototype.addAttribute = function (attribute) {
            this.attributeList.push(attribute);
        };

        Table.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Table.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Table.prototype.clearAttributeList = function () {
            ElementUtils.prototype.removeAllElements(this.attributeList);
        };

        Table.prototype.getId = function () {
            return this.id;
        };

        Table.prototype.getName = function () {
            return this.name;
        };

        Table.prototype.getStore = function () {
            return this.store;
        };

        Table.prototype.getAttributeList = function () {
            return this.attributeList;
        };

        Table.prototype.getAnnotationList = function () {
            return this.annotationList;
        };

        Table.prototype.setId = function (id) {
            this.id = id;
        };

        Table.prototype.setName = function (name) {
            this.name = name;
        };

        Table.prototype.setStore = function (store) {
            this.store = store;
        };

        Table.prototype.setAttributeList = function (attributeList) {
            this.attributeList = attributeList;
        };

        Table.prototype.setAnnotationList = function (annotationList) {
            this.annotationList = annotationList;
        };

        return Table;

    });
