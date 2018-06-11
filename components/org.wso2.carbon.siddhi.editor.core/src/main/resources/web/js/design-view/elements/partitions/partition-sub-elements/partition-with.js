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
         * @class PartitionWith
         * @constructor
         * @class PartitionWith  Creates PartitionWith
         * @param {Object} options Rendering options for the view
         */
        var PartitionWith = function (options) {
            /*
             Data storing structure as follows
                streamName*: '',
                expression*: ''
            */
            if (options !== undefined) {
                this.streamName = options.streamName;
                this.expression = options.expression;
            }
        };

        PartitionWith.prototype.getStreamName = function () {
            return this.streamName;
        };

        PartitionWith.prototype.getExpression = function () {
            return this.expression;
        };

        PartitionWith.prototype.setStreamName = function (streamName) {
            this.streamName = streamName;
        };

        PartitionWith.prototype.setExpression = function (expression) {
            this.expression = expression;
        };

        return PartitionWith;

    });
