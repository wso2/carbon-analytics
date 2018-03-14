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
         * @class Join Query
         * @constructor
         * @class Join Query  Creates a Join Query
         * @param {Object} options Rendering options for the view
         */
        var JoinQuery = function (options) {
            /*
             Data storing structure as follows.
                "id":'',
                "join":{
                    "type":'',
                    "left-stream":{
                         "from":'',
                          "filter":'',
                          "window":'',
                          "post-window-query":'',
                          "as":''
                    },
                    "right-stream":{
                         "from":'',
                         "filter":'',
                         "window":'',
                         "post-window-query":'',
                         "as":''
                    },
                    "on":''
                },
                "projection":[],
                "output-type": '',
                "insert-into":'',
                additional attribute for form generation
                "from" : []
            */
            this.id = options.id;
            this.join = options.join;
            this.projection = options.projection;
            this.outputType = options.outputType;
            this.insertInto = options.insertInto;
            this.from = options.from;
        };

        JoinQuery.prototype.getId = function () {
            return this.id;
        };

        JoinQuery.prototype.getJoin = function () {
            return this.join;
        };

        JoinQuery.prototype.getProjection = function () {
            return this.projection;
        };

        JoinQuery.prototype.getOutputType = function () {
            return this.outputType;
        };

        JoinQuery.prototype.getInsertInto = function () {
            return this.insertInto;
        };

        JoinQuery.prototype.getFrom = function () {
            return this.from;
        };

        JoinQuery.prototype.setId = function (id) {
            this.id = id;
        };

        JoinQuery.prototype.setJoin = function (join) {
            this.join = join;
        };

        JoinQuery.prototype.setProjection = function (projection) {
            this.projection = projection;
        };

        JoinQuery.prototype.setOutputType = function (outputType) {
            this.outputType = outputType;
        };

        JoinQuery.prototype.setInsertInto = function (insertInto) {
            this.insertInto = insertInto;
        };

        JoinQuery.prototype.setFrom = function (from) {
            this.from = from;
        };

        return JoinQuery;

    });
