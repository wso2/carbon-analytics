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
         * @class Attribute
         * @constructor
         * @class Attribute  Creates an object to hold an attribute
         * @param {Object} options Rendering options for the view
         */
        var Attribute = function (options) {
            /*
             Data storing structure as follows
                {
                    name: ‘’,
                    type: ‘’
                }
            */
            this.name = options.name;
            this.type = options.type;
        };

        Attribute.prototype.getName = function () {
            return this.name;
        };

        Attribute.prototype.getType = function () {
            return this.type;
        };

        Attribute.prototype.setName = function (name) {
            this.define = name;
        };

        Attribute.prototype.setType = function (type) {
            this.type = type;
        };

        return Attribute;

    });
