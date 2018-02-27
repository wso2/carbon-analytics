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
         * @class Left Stream
         * @constructor
         * @class Left stream  Creates a Left Stream in join query
         * @param {Object} options Rendering options for the view
         */
        var LeftStream = function (options) {
            /*
             Data storing structure as follows.
                "from":'',
                "filter":'',
                "window":'',
                "post-window-query":'',
                 "as":''
            */
            this.from = options.from;
            this.filter = options.filter;
            this.window = options.window;
            this.postWindowQuery = options.postWindowQuery;
            this.as = options.as;
        };

        LeftStream.prototype.getFrom = function () {
            return this.from;
        };

        LeftStream.prototype.getFilter = function () {
            return this.filter;
        };

        LeftStream.prototype.getWindow = function () {
            return this.window;
        };

        LeftStream.prototype.getPostWindowQuery = function () {
            return this.postWindowQuery;
        };

        LeftStream.prototype.getAs = function () {
            return this.as;
        };

        LeftStream.prototype.setFrom = function (from) {
            this.from = from;
        };

        LeftStream.prototype.setFilter = function (filter) {
            this.filter = filter;
        };

        LeftStream.prototype.setWindow = function (window) {
            this.window = window;
        };

        LeftStream.prototype.setPostWindowQuery = function (postWindowQuery) {
            this.postWindowQuery = postWindowQuery;
        };

        LeftStream.prototype.setAs = function (as) {
            this.as = as;
        };

        return LeftStream;

    });
