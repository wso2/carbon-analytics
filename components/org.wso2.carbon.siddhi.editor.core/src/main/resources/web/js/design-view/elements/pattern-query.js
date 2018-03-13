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

define(['require'],
    function (require) {

        /**
         * @class Pattern Query
         * @constructor
         * @class Pattern Query  Creates a Pattern Query
         * @param {Object} options Rendering options for the view
         */
        var PatternQuery = function (options) {
            /*
             Data storing structure as follows.

                "id": '',
                "name": '',
                "states": [],
                "logic": '',
                "projection": [],
                "filter": '',
                "post-window-filter": '',
                "window": '',
                "having": '',
                "group-by": '',
                "output-type": '',
                "insert-into": '',
                additional attribute for form generation
                "from": []
            */
            this.id = options.id;
            this.name = options.name;
            this.states = options.states;
            this.logic = options.logic;
            this.projection = options.projection;
            this.filter = options.filter;
            this.postWindowFilter = options.postWindowFilter;
            this.window = options.window;
            this.having = options.having;
            this.groupBy = options.groupBy;
            this.outputType = options.outputType;
            this.insertInto = options.insertInto;
            this.from = options.from;
        };

        PatternQuery.prototype.getId = function () {
            return this.id;
        };

        PatternQuery.prototype.getName = function () {
            return this.name;
        };

        PatternQuery.prototype.getStates = function () {
            return this.states;
        };

        PatternQuery.prototype.getLogic = function () {
            return this.logic;
        };

        PatternQuery.prototype.getProjection = function () {
            return this.projection;
        };

        PatternQuery.prototype.getFilter = function () {
            return this.filter;
        };

        PatternQuery.prototype.getPostWindowFilter = function () {
            return this.postWindowFilter;
        };

        PatternQuery.prototype.getWindow = function () {
            return this.window;
        };

        PatternQuery.prototype.getHaving = function () {
            return this.having;
        };

        PatternQuery.prototype.getGroupBy = function () {
            return this.groupBy;
        };

        PatternQuery.prototype.getOutputType = function () {
            return this.outputType;
        };

        PatternQuery.prototype.getInsertInto = function () {
            return this.insertInto;
        };

        PatternQuery.prototype.getFrom = function () {
            return this.from;
        };

        PatternQuery.prototype.setId = function (id) {
            this.id = id;
        };

        PatternQuery.prototype.setName = function (name) {
            this.name = name;
        };

        PatternQuery.prototype.setStates = function (states) {
            this.states = states;
        };

        PatternQuery.prototype.setLogic = function (logic) {
            this.logic = logic;
        };

        PatternQuery.prototype.setProjection = function (projection) {
            this.projection = projection;
        };

        PatternQuery.prototype.setFilter = function (filter) {
            this.filter = filter;
        };

        PatternQuery.prototype.setPostWindowFilter = function (postWindowFilter) {
            this.postWindowFilter = postWindowFilter;
        };

        PatternQuery.prototype.setWindow = function (window) {
            this.window = window;
        };

        PatternQuery.prototype.setHaving = function (having) {
            this.having = having;
        };

        PatternQuery.prototype.setGroupBy = function (groupBy) {
            this.groupBy = groupBy;
        };

        PatternQuery.prototype.setOutputType = function (outputType) {
            this.outputType = outputType;
        };

        PatternQuery.prototype.setInsertInto = function (insertInto) {
            this.insertInto = insertInto;
        };

        PatternQuery.prototype.setFrom = function (from) {
            this.from = from;
        };

        return PatternQuery;

    });
