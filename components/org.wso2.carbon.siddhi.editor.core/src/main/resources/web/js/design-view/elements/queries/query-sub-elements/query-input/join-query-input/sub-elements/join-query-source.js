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
         * @class Join Source
         * @constructor
         * @class JoinSource  Creates a join source in join query
         * @param {Object} options Rendering options for the view
         */
        var JoinSource = function (options) {
            /*
             Data storing structure as follows.
                type*: 'STREAM|TABLE|WINDOW|AGGREGATION|TRIGGER',
                from*: '',
                filter: '', // If there is a filter, there must be a window for joins (the only exception is when type = window).
                window: {
                    function*: '',
                    parameters*: ['value1',...]
                },
                as: '',
                isUnidirectional: true|false // Only one 'isUnidirectional' value can be true at a time (either left definition|right definition|none)
            */
            this.type = options.type;
            this.from = options.from;
            this.filter = options.filter;
            this.window = options.window;
            this.as = options.as;
            this.isUnidirectional = options.isUnidirectional;
        };

        JoinSource.prototype.getType = function () {
            return this.type;
        };

        JoinSource.prototype.getFrom = function () {
            return this.from;
        };

        JoinSource.prototype.getFilter = function () {
            return this.filter;
        };

        JoinSource.prototype.getWindow = function () {
            return this.window;
        };

        JoinSource.prototype.getAs = function () {
            return this.as;
        };

        JoinSource.prototype.getIsUnidirectional = function () {
            return this.isUnidirectional;
        };

        JoinSource.prototype.setType = function (type) {
            this.type = type;
        };

        JoinSource.prototype.setFrom = function (from) {
            this.from = from;
        };

        JoinSource.prototype.setFilter = function (filter) {
            this.filter = filter;
        };

        JoinSource.prototype.setWindow = function (window) {
            this.window = window;
        };

        JoinSource.prototype.setAs = function (as) {
            this.as = as;
        };

        JoinSource.prototype.setIsUnidirectional = function (isUnidirectional) {
            this.isUnidirectional = isUnidirectional;
        };

        return JoinSource;

    });
