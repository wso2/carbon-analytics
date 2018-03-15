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
         * @class Window Query
         * @constructor
         * @class Window Query  Creates a Window Query
         * @param {Object} options Rendering options for the view
         */
        var WindowQuery = function (options) {
            /*
             Data storing structure as follows.

                id: '',
                name: '',
                inStream: '',
                outStream: '',
                filter1: '',
                filter2: '',
                window: '',
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
            this.filter1 = options.filter1;
            this.filter2 = options.filter2;
            this.window = options.window;
            this.attributes = options.attributes;
        };

        WindowQuery.prototype.getId = function () {
            return this.id;
        };

        WindowQuery.prototype.getName = function () {
            return this.name;
        };

        WindowQuery.prototype.getInStream = function () {
            return this.inStream;
        };

        WindowQuery.prototype.getOutStream = function () {
            return this.outStream;
        };

        WindowQuery.prototype.getFilter1 = function () {
            return this.filter1;
        };

        WindowQuery.prototype.getFilter2 = function () {
            return this.filter2;
        };

        WindowQuery.prototype.getWindow = function () {
            return this.window;
        };

        WindowQuery.prototype.getAttributes = function () {
            return this.attributes;
        };

        WindowQuery.prototype.setId = function (id) {
            this.id = id;
        };

        WindowQuery.prototype.setName = function (name) {
            this.name = name;
        };

        WindowQuery.prototype.setInStream = function (inStream) {
            this.inStream = inStream;
        };

        WindowQuery.prototype.setOutStream = function (outStream) {
            this.outStream = outStream;
        };

        WindowQuery.prototype.setFilter1 = function (filter1) {
            this.filter1 = filter1;
        };

        WindowQuery.prototype.setFilter2 = function (filter2) {
            this.filter2 = filter2;
        };

        WindowQuery.prototype.setWindow = function (window) {
            this.window = window;
        };

        WindowQuery.prototype.setAttributes = function (attributes) {
            this.attributes = attributes;
        };

        return WindowQuery;

    });
