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
         * @class StreamHandler
         * @constructor
         * @class StreamHandler Creates a Stream Handler object for a query which is stored in streamHandler list
         *          in query input
         * @param {Object} options Rendering options for the view
         */
        var StreamHandler = function (options) {
            /*
             Data storing structure as follows.
                type*: 'FILTER | FUNCTION | WINDOW',
                value*: ''
            */
            if (options !== undefined) {
                this.type
                    = (options.type !== undefined)? (options.type).toUpperCase() : undefined;
                this.value = options.value;
            }
        };

        StreamHandler.prototype.getType = function () {
            return this.type;
        };

        StreamHandler.prototype.getValue = function () {
            return this.value;
        };

        StreamHandler.prototype.setType = function (type) {
            this.type = type.toUpperCase();
        };

        StreamHandler.prototype.setValue = function (value) {
            this.value = value;
        };

        return StreamHandler;

    });
