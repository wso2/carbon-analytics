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
         * @class Function
         * @constructor
         * @class Function  Creates a Function Definition
         * @param {Object} options Rendering options for the view
         */
        var Function = function (options) {
            /*
             Data storing structure as follows
                id*: '',
                name*: '',
                scriptType*: 'JAVASCRIPT | R | SCALA',
                returnType*: 'INT | LONG | DOUBLE | FLOAT | STRING | BOOL | OBJECT',
                body*: ''
            */
            if (options !== undefined) {
                this.id = options.id;
                this.name = options.name;
                this.scriptType = (options.scriptType !== undefined)? (options.scriptType).toUpperCase() : undefined;
                this.returnType = (options.returnType !== undefined)? (options.returnType).toUpperCase() : undefined;
                this.body = options.body;
            }
        };

        Function.prototype.getId = function () {
            return this.id;
        };

        Function.prototype.getName = function () {
            return this.name;
        };

        Function.prototype.getScriptType = function () {
            return this.scriptType;
        };

        Function.prototype.getReturnType = function () {
            return this.returnType;
        };

        Function.prototype.getBody = function () {
            return this.body;
        };

        Function.prototype.setId = function (id) {
            this.id = id;
        };

        Function.prototype.setName = function (name) {
            this.name = name;
        };

        Function.prototype.setScriptType = function (scriptType) {
            this.scriptType = scriptType;
        };

        Function.prototype.setReturnType = function (returnType) {
            this.returnType = returnType;
        };

        Function.prototype.setBody = function (body) {
            this.body = body;
        };

        return Function;

    });
