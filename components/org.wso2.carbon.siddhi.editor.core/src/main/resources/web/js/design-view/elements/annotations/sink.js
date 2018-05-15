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
         * @class Sink
         * @constructor
         * @class Sink  Creates a Sink
         * @param {Object} options Rendering options for the view
         */
        var Sink = function (options) {
            /*
             Data storing structure as follows
                id*: ‘’,
                type*: ‘’,
                options: {Key-Value Pair JSON},
                map: {
                    type*: ‘’,
                    options: {Key-Value Pair JSON},
                    payload: {
                        type*: ‘map’,
                        value*: {Key-Value Pair JSON}
                    }
                    << or  >>
                    payload: {
                        type*: 'single',
                        value*: ''
                    }
                }
            */
            if (options !== undefined) {
                this.id = options.id;
                this.type = options.type;
                this.options = options.options;
                this.map = options.map;
            }
        };

        Sink.prototype.getId = function () {
            return this.id;
        };

        Sink.prototype.getType = function () {
            return this.type;
        };

        Sink.prototype.getOptions = function () {
            return this.options;
        };

        Sink.prototype.getMap = function () {
            return this.map;
        };

        Sink.prototype.setId = function (id) {
            this.id = id;
        };

        Sink.prototype.setType = function (type) {
            this.type = type;
        };

        Sink.prototype.setOptions = function (options) {
            this.options = options;
        };

        Sink.prototype.setMap = function (map) {
            this.map = map;
        };

        return Sink;

    });
