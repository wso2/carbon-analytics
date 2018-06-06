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
         * @class Annotation
         * @constructor
         * @class Annotation  Creates an object to hold annotations for a particular element
         * @param {Object} options Rendering options for the view
         */
        var Annotation = function (options) {
            /*
             Data storing structure as follows
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
            */
            if (options !== undefined) {
                this.name = options.name;
                this.type = options.type;
                this.value = options.value;
            }
        };

        Annotation.prototype.getName = function () {
            return this.name;
        };

        Annotation.prototype.getType = function () {
            return this.type;
        };

        Annotation.prototype.getValue = function () {
            return this.value;
        };

        Annotation.prototype.setName = function (name) {
            this.define = name;
        };

        Annotation.prototype.setType = function (type) {
            this.type = type;
        };

        Annotation.prototype.setValue = function (value) {
            this.value = value;
        };

        return Annotation;

    });
