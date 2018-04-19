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
         * @class PatternQueryInputNotAnd
         * @constructor
         * @class PatternQueryInputNotAnd Creates a not and input section for Pattern Query
         * @param {Object} options Rendering options for the view
         */
        var PatternQueryInputNotAnd = function (options) {
            /*
             Data storing structure as follows.
                type*: 'notand',
                forEvery*: true|false,
                leftStreamName*: '',
                leftFilter: ''
                rightEventReference: '',
                rightStreamName*: '',
                rightFilter: '',
                within: ''
            */
            this.type = 'notand';
            this.forEvery = options.forEvery;
            this.leftStreamName = options.leftStreamName;
            this.leftFilter = options.leftFilter;
            this.rightEventReference = options.rightEventReference;
            this.rightStreamName = options.rightStreamName;
            this.rightFilter = options.rightFilter;
            this.within = options.within;
        };

        PatternQueryInputNotAnd.prototype.getType = function () {
            return this.type;
        };

        PatternQueryInputNotAnd.prototype.getForEvery = function () {
            return this.forEvery;
        };

        PatternQueryInputNotAnd.prototype.getLeftStreamName = function () {
            return this.leftStreamName;
        };

        PatternQueryInputNotAnd.prototype.getLeftFilter = function () {
            return this.leftFilter;
        };

        PatternQueryInputNotAnd.prototype.getRightEventReference = function () {
            return this.rightEventReference;
        };

        PatternQueryInputNotAnd.prototype.getRightStreamName = function () {
            return this.rightStreamName;
        };

        PatternQueryInputNotAnd.prototype.getRightFilter = function () {
            return this.rightFilter;
        };

        PatternQueryInputNotAnd.prototype.getWithin = function () {
            return this.within;
        };

        PatternQueryInputNotAnd.prototype.setForEvery = function (forEvery) {
            this.forEvery = forEvery;
        };

        PatternQueryInputNotAnd.prototype.setLeftStreamName = function (leftStreamName) {
            this.leftStreamName = leftStreamName;
        };

        PatternQueryInputNotAnd.prototype.setLeftFilter = function (leftFilter) {
            this.leftFilter = leftFilter;
        };

        PatternQueryInputNotAnd.prototype.setRightEventReference = function (rightEventReference) {
            this.rightEventReference = rightEventReference;
        };

        PatternQueryInputNotAnd.prototype.setRightStreamName = function (rightStreamName) {
            this.rightStreamName = rightStreamName;
        };

        PatternQueryInputNotAnd.prototype.setRightFilter = function (rightFilter) {
            this.rightFilter = rightFilter;
        };

        PatternQueryInputNotAnd.prototype.setWithin = function (within) {
            this.within = within;
        };

        return PatternQueryInputNotAnd;

    });
