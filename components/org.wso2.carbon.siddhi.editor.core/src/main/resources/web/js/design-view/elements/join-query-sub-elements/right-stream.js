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
         * @class Right Stream
         * @constructor
         * @class Right stream Creates a Right Stream in join query
         * @param {Object} options Rendering options for the view
         */
        var RightStream = function (options) {
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

        RightStream.prototype.getFrom = function () {
            return this.from;
        };

        RightStream.prototype.getFilter = function () {
            return this.filter;
        };

        RightStream.prototype.getWindow = function () {
            return this.window;
        };

        RightStream.prototype.getPostWindowQuery = function () {
            return this.postWindowQuery;
        };

        RightStream.prototype.getAs = function () {
            return this.as;
        };

        RightStream.prototype.setFrom = function (from) {
            this.from = from;
        };

        RightStream.prototype.setFilter = function (filter) {
            this.filter = filter;
        };

        RightStream.prototype.setWindow = function (window) {
            this.window = window;
        };

        RightStream.prototype.setPostWindowQuery = function (postWindowQuery) {
            this.postWindowQuery = postWindowQuery;
        };

        RightStream.prototype.getAs = function (as) {
            this.as = as;
        };

        return RightStream;

    });
