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
         * @class WindowFilterProjectionQueryInput
         * @constructor
         * @class WindowFilterProjectionQueryInput Creates an input section for a Window/Filter/Projection Query
         * @param {Object} options Rendering options for the view
         */
        var WindowFilterProjectionQueryInput = function (options) {
            /*
             Data storing structure as follows.
                type*: 'window_filter_projection',
                subType*: 'window|filter|projection',
                from*: '',
                filter: '',
                window: {
                    function*: '',
                    parameters*: ['value1',...],
                    filter: ''
                }
            */
            this.type = 'window_filter_projection';
            this.subType = options.subType;
            this.from = options.from;
            this.filter = options.filter;
            this.window = options.window;
        };

        WindowFilterProjectionQueryInput.prototype.getType = function () {
            return this.type;
        };

        WindowFilterProjectionQueryInput.prototype.getSubType = function () {
            return this.subType;
        };

        WindowFilterProjectionQueryInput.prototype.getFrom = function () {
            return this.from;
        };

        WindowFilterProjectionQueryInput.prototype.getFilter = function () {
            return this.filter;
        };

        WindowFilterProjectionQueryInput.prototype.getWindow = function () {
            return this.window;
        };

        WindowFilterProjectionQueryInput.prototype.setSubType = function (subType) {
            this.subType = subType;
        };

        WindowFilterProjectionQueryInput.prototype.setFrom = function (from) {
            this.from = from;
        };

        WindowFilterProjectionQueryInput.prototype.setFilter = function (filter) {
            this.filter = filter;
        };

        WindowFilterProjectionQueryInput.prototype.setWindow = function (window) {
            this.window = window;
        };

        return WindowFilterProjectionQueryInput;

    });
