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
         * @class QueryOutput
         * @constructor
         * @class QueryOutput  Creates a query output section
         * @param {Object} options Rendering options for the view
         */
        var QueryOutput = function (options) {
            /*
             Data storing structure as follows
                type*: 'INSERT|DELETE|UPDATE|UPDATE_OR_INSERT_INTO',
                output*: {INSERT JSON|DELETE JSON|UPDATE JSON|UPDATE-OR-INSERT JSON},
                target*: ''
            */
            if (options !== undefined) {
                this.type = (options.type !== undefined) ? (options.type).toUpperCase() : undefined;
                this.output = options.output;
                this.target = options.target;
            }
        };

        QueryOutput.prototype.getType = function () {
            return this.type;
        };

        QueryOutput.prototype.getOutput = function () {
            return this.output;
        };


        QueryOutput.prototype.getTarget = function () {
            return this.target;
        };

        QueryOutput.prototype.setType = function (type) {
            this.type = type.toUpperCase();
        };

        QueryOutput.prototype.setOutput = function (output) {
            this.output = output;
        };

        QueryOutput.prototype.setTarget = function (target) {
            this.target = target;
        };

        return QueryOutput;

    });
