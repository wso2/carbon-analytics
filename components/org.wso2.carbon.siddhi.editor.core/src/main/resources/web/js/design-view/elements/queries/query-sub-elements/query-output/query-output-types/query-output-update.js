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
         * @class QueryOutputUpdate
         * @constructor
         * @class QueryOutputUpdate  Creates a update type in query output
         * @param {Object} options Rendering options for the view
         */
        var QueryOutputUpdate = function (options) {
            /*
             Data storing structure as follows
                forEventType: 'current|expired|all',
                set*: [
                    {
                        attribute*: '',
                        value*: ''
                    },
                    ...
                ],
                on*: ''
            */
            this.forEventType = options.forEventType;
            this.set = options.set;
            this.on = options.on;
        };

        QueryOutputUpdate.prototype.getForEventType = function () {
            return this.forEventType;
        };

        QueryOutputUpdate.prototype.getSet = function () {
            return this.set;
        };

        QueryOutputUpdate.prototype.getOn = function () {
            return this.on;
        };

        QueryOutputUpdate.prototype.setForEventType = function (forEventType) {
            this.forEventType = forEventType;
        };

        QueryOutputUpdate.prototype.setSet = function (set) {
            this.set = set;
        };

        QueryOutputUpdate.prototype.setOn = function (on) {
            this.on = on;
        };

        return QueryOutputUpdate;

    });
