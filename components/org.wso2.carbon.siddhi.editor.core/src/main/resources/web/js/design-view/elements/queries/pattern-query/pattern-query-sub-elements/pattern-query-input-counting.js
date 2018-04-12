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
         * @class PatternQueryInputCounting
         * @constructor
         * @class PatternQueryInputCounting Creates an default input section for Pattern Query
         * @param {Object} options Rendering options for the view
         */
        var PatternQueryInputCounting = function (options) {
            /*
             Data storing structure as follows.
                type*: 'counting',
                forEvery*: 'true|false',
                eventReference: '',
                streamName*: '',
                filter: '',
                minCount: '',
                maxCount: '',
                within: ''
            */
            this.type = 'counting';
            this.forEvery = options.forEvery;
            this.eventReference = options.eventReference;
            this.streamName = options.streamName;
            this.filter = options.filter;
            this.minCount = options.minCount;
            this.maxCount = options.maxCount;
            this.within = options.within;
        };

        PatternQueryInputCounting.prototype.getType = function () {
            return this.type;
        };

        PatternQueryInputCounting.prototype.getForEvery = function () {
            return this.forEvery;
        };

        PatternQueryInputCounting.prototype.getEventReference = function () {
            return this.eventReference;
        };

        PatternQueryInputCounting.prototype.getStreamName = function () {
            return this.streamName;
        };

        PatternQueryInputCounting.prototype.getFilter = function () {
            return this.filter;
        };

        PatternQueryInputCounting.prototype.getMinCount = function () {
            return this.minCount;
        };

        PatternQueryInputCounting.prototype.getMaxCount = function () {
            return this.maxCount;
        };

        PatternQueryInputCounting.prototype.getWithin = function () {
            return this.within;
        };

        PatternQueryInputCounting.prototype.setForEvery = function (forEvery) {
            this.forEvery = forEvery;
        };

        PatternQueryInputCounting.prototype.setEventReference = function (eventReference) {
            this.eventReference = eventReference;
        };

        PatternQueryInputCounting.prototype.setStreamName = function (streamName) {
            this.streamName = streamName;
        };

        PatternQueryInputCounting.prototype.setFilter = function (filter) {
            this.filter = filter;
        };

        PatternQueryInputCounting.prototype.setMinCount = function (minCount) {
            this.minCount = minCount;
        };

        PatternQueryInputCounting.prototype.setMaxCount = function (maxCount) {
            this.maxCount = maxCount;
        };

        PatternQueryInputCounting.prototype.setWithin = function (within) {
            this.within = within;
        };

        return PatternQueryInputCounting;

    });
