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
         * @class QuerySelect
         * @constructor
         * @class QuerySelect  Creates a QuerySelect part in a query
         * @param {Object} options Rendering options for the view
         */
        var QuerySelect = function (options) {
            /*
             Data storing structure as follows
                type*: 'USER_DEFINED',
                value*: [
                    {
                        expression*: '',
                        as: ''
                    },
                    ...
                ]
                << or >>
                type*: 'ALL',
                value*: '*'
            */
            if (options !== undefined) {
                this.type = (options.type !== undefined) ? (options.type).toUpperCase() : undefined;
                this.value = options.value;
            }
        };

        QuerySelect.prototype.getType = function () {
            return this.type;
        };

        QuerySelect.prototype.getValue = function () {
            return this.value;
        };

        QuerySelect.prototype.setType = function (type) {
            this.type = type.toUpperCase();
        };

        QuerySelect.prototype.setValue = function (value) {
            this.value = value;
        };

        return QuerySelect;

    });
