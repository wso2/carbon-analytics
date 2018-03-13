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
         * @class FilterQuery
         * @constructor
         * @class FilterQuery  Creates a Filter Query
         * @param {Object} options Rendering options for the view
         */
        var FilterQuery = function (options) {
            /*
             Data storing structure as follows.
                id: '',
                name: '',
                inStream: '',
                outStream: '',
                filter: '',
                attributes: [
                     {
                        attribute:'',
                        type: ''
                     }
                ]
            */
            this.id = options.id;
            this.name = options.name;
            this.inStream = options.inStream;
            this.outStream = options.outStream;
            this.filter = options.filter;
            this.attributes = options.attributes;
        };

        FilterQuery.prototype.getId = function () {
            return this.id;
        };

        FilterQuery.prototype.getName = function () {
            return this.name;
        };

        FilterQuery.prototype.getInStream = function () {
            return this.inStream;
        };

        FilterQuery.prototype.getOutStream = function () {
            return this.outStream;
        };

        FilterQuery.prototype.getFilter = function () {
            return this.filter;
        };

        FilterQuery.prototype.getAttributes = function () {
            return this.attributes;
        };

        FilterQuery.prototype.setId = function (id) {
            this.id = id;
        };

        FilterQuery.prototype.setName = function (name) {
            this.name = name;
        };

        FilterQuery.prototype.setInStream = function (inStream) {
            this.inStream = inStream;
        };

        FilterQuery.prototype.setOutStream = function (outStream) {
            this.outStream = outStream;
        };

        FilterQuery.prototype.setFilter = function (filter) {
            this.filter = filter;
        };

        FilterQuery.prototype.setAttributes = function (attributes) {
            this.attributes = attributes;
        };

        return FilterQuery;

    });
