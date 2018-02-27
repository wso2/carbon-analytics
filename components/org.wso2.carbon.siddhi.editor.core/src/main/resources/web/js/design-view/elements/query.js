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
         * @class Query
         * @constructor
         * @class Query  Creates a Query
         * @param {Object} options Rendering options for the view
         */
        var Query = function (options) {
            /*
             Data storing structure as follows.

                "id": '',
                "name": '',
                "from": '',
                "insert-into": '',
                "filter": '',
                "post-window-query": '',
                "window": '',
                "output-type": '',
                "projection": []
            */
            this.id = options.id;
            this.name = options.name;
            this.from = options.from;
            this.insertInto = options.insertInto;
            this.filter = options.filter;
            this.postWindowFilter = options.postWindowFilter;
            this.window = options.window;
            this.outputType = options.outputType;
            this.projection = options.projection;
        };

        Query.prototype.getId = function () {
            return this.id;
        };

        Query.prototype.getName = function () {
            return this.name;
        };

        Query.prototype.getFrom = function () {
            return this.from;
        };

        Query.prototype.getInsertInto = function () {
            return this.insertInto;
        };

        Query.prototype.getFilter = function () {
            return this.filter;
        };

        Query.prototype.getPostWindowFilter = function () {
            return this.postWindowFilter;
        };

        Query.prototype.getWindow = function () {
            return this.window;
        };

        Query.prototype.getOutputType = function () {
            return this.outputType;
        };

        Query.prototype.getProjection = function () {
            return this.projection;
        };

        Query.prototype.setId = function (id) {
            this.id = id;
        };

        Query.prototype.setName = function (name) {
            this.name = name;
        };

        Query.prototype.setFrom = function (from) {
            this.from = from;
        };

        Query.prototype.setInsertInto = function (insertInto) {
            this.insertInto = insertInto;
        };

        Query.prototype.setFilter = function (filter) {
            this.filter = filter;
        };

        Query.prototype.setPostWindowFilter = function (postWindowFilter) {
            this.postWindowFilter = postWindowFilter;
        };

        Query.prototype.setWindow = function (window) {
            this.window = window;
        };

        Query.prototype.setOutputType = function (outputType) {
            this.outputType = outputType;
        };

        Query.prototype.setProjection = function (projection) {
            this.projection = projection;
        };

        return Query;

    });
