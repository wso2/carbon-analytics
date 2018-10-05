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

         * @class AnnotationObject
         * @constructor
         * @class AnnotationObject creates an object to hold an annotation object
         * @param {Object} options Rendering options for the view
         */

        var AnnotationObject = function (options) {
            if (!arguments.length) {
                this.elements = [];
                this.annotations = [];
            }
            else {
                if (options !== undefined) {
                    this.name = options.name;
                    this.elements = options.elements;
                    this.annotations = options.annotations;
                }
            }
        };

        AnnotationObject.prototype.getName = function () {
            return this.name;
        };

        AnnotationObject.prototype.setName = function (name) {
            this.name = name;
        };

        AnnotationObject.prototype.addElement = function (element) {
            this.elements.push(element)
        };

        AnnotationObject.prototype.addAnnotation = function (annotation) {
            this.annotations.push(annotation)
        };

        return AnnotationObject;

    });

