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
         * @class Pass Through Query
         * @constructor
         * @class Pass Through Query  Creates a Pass Through Query
         * @param {Object} options Rendering options for the view
         */
        var PassThroughQuery = function (options) {
            /*
             Data storing structure as follows.

                id: '',
                name: '',
                inStream: '',
                outStream: '',
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
            this.attributes = options.attributes;
        };

        PassThroughQuery.prototype.getId = function () {
            return this.id;
        };

        PassThroughQuery.prototype.getName = function () {
            return this.name;
        };

        PassThroughQuery.prototype.getInStream = function () {
            return this.inStream;
        };

        PassThroughQuery.prototype.getOutStream = function () {
            return this.outStream;
        };

        PassThroughQuery.prototype.getAttributes = function () {
            return this.attributes;
        };

        PassThroughQuery.prototype.setId = function (id) {
            this.id = id;
        };

        PassThroughQuery.prototype.setName = function (name) {
            this.name = name;
        };

        PassThroughQuery.prototype.setInStream = function (inStream) {
            this.inStream = inStream;
        };

        PassThroughQuery.prototype.setOutStream = function (outStream) {
            this.outStream = outStream;
        };

        PassThroughQuery.prototype.setAttributes = function (attributes) {
            this.attributes = attributes;
        };

        return PassThroughQuery;

    });
