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
         * @class Stream
         * @constructor
         * @class Stream  Creates a Stream
         * @param {Object} options Rendering options for the view
         */
        var Stream = function (options) {
            /*
             Data storing structure as follows
                id: '',
                define: '',
                type: '',
                attributes: [
                     {
                        attribute:'',
                        type: ''
                     }
                ]
            */
            this.id = options.id;
            this.define = options.define;
            this.type = options.type;
            this.attributes = options.attributes;
        };

        Stream.prototype.getId = function () {
            return this.id;
        };

        Stream.prototype.getDefine = function () {
            return this.define;
        };

        Stream.prototype.getType = function () {
            return this.type;
        };

        Stream.prototype.getAttributes = function () {
            return this.attributes;
        };

        Stream.prototype.setId = function (id) {
            this.id = id;
        };

        Stream.prototype.setDefine = function (define) {
            this.define = define;
        };

        Stream.prototype.setType = function (type) {
            this.type = type;
        };

        Stream.prototype.setAttributes = function (attributes) {
            this.attributes = attributes;
        };

        return Stream;

    });
