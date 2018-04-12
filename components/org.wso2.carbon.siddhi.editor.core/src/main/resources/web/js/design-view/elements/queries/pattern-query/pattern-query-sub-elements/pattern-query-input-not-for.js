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
         * @class PatternQueryInputNotFor
         * @constructor
         * @class PatternQueryInputNotFor Creates  not for input section for Pattern Query
         * @param {Object} options Rendering options for the view
         */
        var PatternQueryInputNotFor = function (options) {
            /*
             Data storing structure as follows.
                type*: 'notfor',
                forEvery*: 'true|false',
                streamName*: '',
                filter: '',
                forDuration*: ''
            */
            this.type = 'notfor';
            this.forEvery = options.forEvery;
            this.streamName = options.streamName;
            this.filter = options.filter;
            this.forDuration = options.forDuration;
        };

        PatternQueryInputNotFor.prototype.getType = function () {
            return this.type;
        };

        PatternQueryInputNotFor.prototype.getForEvery = function () {
            return this.forEvery;
        };

        PatternQueryInputNotFor.prototype.getStreamName = function () {
            return this.streamName;
        };

        PatternQueryInputNotFor.prototype.getFilter = function () {
            return this.filter;
        };

        PatternQueryInputNotFor.prototype.getForDuration = function () {
            return this.forDuration;
        };

        PatternQueryInputNotFor.prototype.setForEvery = function (forEvery) {
            this.forEvery = forEvery;
        };

        PatternQueryInputNotFor.prototype.setStreamName = function (streamName) {
            this.streamName = streamName;
        };

        PatternQueryInputNotFor.prototype.setFilter = function (filter) {
            this.filter = filter;
        };

        PatternQueryInputNotFor.prototype.setForDuration = function (forDuration) {
            this.forDuration = forDuration;
        };

        return PatternQueryInputNotFor;

    });
