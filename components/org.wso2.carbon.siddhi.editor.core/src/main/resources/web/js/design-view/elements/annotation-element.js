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
define(
    function () {

        /**
         * @class AnnotationElement
         * @constructor
         * @class AnnotationElement Creates an object to hold an annotation element
         * @param {key} to store the property of the annotation element
         * @param {value} to store the property's value of the annotation element
         */

        var AnnotationElement = function (name, value) {
            this.name = name;
            this.value = value;
        };

        AnnotationElement.prototype.getName = function () {
            return this.key;
        };

        AnnotationElement.prototype.setName= function (name) {
            this.name = name;
        };

        AnnotationElement.prototype.getValue = function () {
            return this.value;
        };

        AnnotationElement.prototype.setValue = function (value) {
            this.value = value;
        };

        return AnnotationElement;

    });

