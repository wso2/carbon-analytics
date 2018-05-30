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

define(['require', 'elementUtils'],
    function (require, ElementUtils) {

        /**
         * @class WindowFilterProjectionQueryInput
         * @constructor
         * @class WindowFilterProjectionQueryInput Creates an input section for a Window/Filter/Projection Query
         * @param {Object} options Rendering options for the view
         */
        var WindowFilterProjectionQueryInput = function (options) {
            /*
             Data storing structure as follows.
                type*: 'WINDOW|FILTER|PROJECTION',
                from*: '',
                streamHandlerList: [
                    {
                        type*: 'FILTER',
                        value*: ''
                    },
                    << and|or >>
                    {
                        type*: 'FUNCTION|WINDOW',
                        value*: {
                            function*: '',
                            parameters*: ['value1',...],
                        }
                    },
                    ...
                ]
            */
            if (options !== undefined) {
                this.type = (options.type !== undefined) ? (options.type).toUpperCase() : undefined;
                this.from = options.from;
            }
            this.streamHandlerList = [];
        };

        WindowFilterProjectionQueryInput.prototype.addStreamHandler = function (streamHandler) {
            this.streamHandlerList.push(streamHandler);
        };

        WindowFilterProjectionQueryInput.prototype.clearStreamHandlerList = function () {
            ElementUtils.prototype.removeAllElements(this.streamHandlerList);
        };

        WindowFilterProjectionQueryInput.prototype.getType = function () {
            return this.type;
        };

        WindowFilterProjectionQueryInput.prototype.getFrom = function () {
            return this.from;
        };

        WindowFilterProjectionQueryInput.prototype.getStreamHandlerList = function () {
            return this.streamHandlerList;
        };

        WindowFilterProjectionQueryInput.prototype.setType = function (type) {
            this.type = type.toUpperCase();
        };

        WindowFilterProjectionQueryInput.prototype.setFrom = function (from) {
            this.from = from;
        };

        WindowFilterProjectionQueryInput.prototype.setStreamHandlerList = function (streamHandlerList) {
            this.streamHandlerList = streamHandlerList;
        };

        return WindowFilterProjectionQueryInput;

    });
