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
         * @class QueryWindow
         * @constructor
         * @class QueryWindow Creates a window for a query
         * @param {Object} options Rendering options for the view
         */
        var QueryWindow = function (options) {
            /*
             Data storing structure as follows.
                function*: '',
                parameters*: ['value1',...],
                filter: ''
            */
            this.function = options.function;
            this.parameters = options.parameters;
            this.filter = options.filter;
        };

        QueryWindow.prototype.getFunction = function () {
            return this.function;
        };

        QueryWindow.prototype.getParameters = function () {
            return this.parameters;
        };

        QueryWindow.prototype.getFilter = function () {
            return this.filter;
        };

        QueryWindow.prototype.setFunction = function (functionName) {
            this.function = functionName;
        };

        QueryWindow.prototype.setParameters = function (parameters) {
            this.parameters = parameters;
        };

        QueryWindow.prototype.setFilter = function (filter) {
            this.filter = filter;
        };

        return QueryWindow;

    });
