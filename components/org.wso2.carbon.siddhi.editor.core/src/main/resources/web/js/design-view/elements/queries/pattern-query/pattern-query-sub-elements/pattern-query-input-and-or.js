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
         * @class PatternQueryInputAndOr
         * @constructor
         * @class PatternQueryInputAndOr Creates an and/or input section for Pattern Query
         * @param {Object} options Rendering options for the view
         */
        var PatternQueryInputAndOr = function (options) {
            /*
             Data storing structure as follows.
                type*: 'andor',
                forEvery*: 'true|false',
                leftEventReference: '',
                leftStreamName*: '',
                leftFilter: ''
                connectedWith*: 'and|or',
                rightEventReference: '',
                rightStreamName*: '',
                rightFilter: '',
                within: ''
            */
            this.type = 'andor';
            this.forEvery = options.forEvery;
            this.leftEventReference = options.leftEventReference;
            this.leftStreamName = options.leftStreamName;
            this.leftFilter = options.leftFilter;
            this.connectedWith = options.connectedWith;
            this.rightEventReference = options.rightEventReference;
            this.rightStreamName = options.rightStreamName;
            this.rightFilter = options.rightFilter;
            this.within = options.within;
        };

        PatternQueryInputAndOr.prototype.getType = function () {
            return this.type;
        };

        PatternQueryInputAndOr.prototype.getForEvery = function () {
            return this.forEvery;
        };

        PatternQueryInputAndOr.prototype.getLeftEventReference = function () {
            return this.leftEventReference;
        };

        PatternQueryInputAndOr.prototype.getLeftStreamName = function () {
            return this.leftStreamName;
        };

        PatternQueryInputAndOr.prototype.getLeftFilter = function () {
            return this.leftFilter;
        };

        PatternQueryInputAndOr.prototype.getConnectedWith = function () {
            return this.connectedWith;
        };

        PatternQueryInputAndOr.prototype.getRightEventReference = function () {
            return this.rightEventReference;
        };

        PatternQueryInputAndOr.prototype.getRightStreamName = function () {
            return this.rightStreamName;
        };

        PatternQueryInputAndOr.prototype.getRightFilter = function () {
            return this.rightFilter;
        };

        PatternQueryInputAndOr.prototype.getWithin = function () {
            return this.within;
        };

        PatternQueryInputAndOr.prototype.setForEvery = function (forEvery) {
            this.forEvery = forEvery;
        };

        PatternQueryInputAndOr.prototype.setLeftEventReference = function (leftEventReference) {
            this.leftEventReference = leftEventReference;
        };

        PatternQueryInputAndOr.prototype.setLeftStreamName = function (leftStreamName) {
            this.leftStreamName = leftStreamName;
        };

        PatternQueryInputAndOr.prototype.setLeftFilter = function (leftFilter) {
            this.leftFilter = leftFilter;
        };

        PatternQueryInputAndOr.prototype.setConnectedWith = function (connectedWith) {
            this.connectedWith = connectedWith;
        };

        PatternQueryInputAndOr.prototype.setRightEventReference = function (rightEventReference) {
            this.rightEventReference = rightEventReference;
        };

        PatternQueryInputAndOr.prototype.setRightStreamName = function (rightStreamName) {
            this.rightStreamName = rightStreamName;
        };

        PatternQueryInputAndOr.prototype.setRightFilter = function (rightFilter) {
            this.rightFilter = rightFilter;
        };

        PatternQueryInputAndOr.prototype.setWithin = function (within) {
            this.within = within;
        };

        return PatternQueryInputAndOr;

    });
