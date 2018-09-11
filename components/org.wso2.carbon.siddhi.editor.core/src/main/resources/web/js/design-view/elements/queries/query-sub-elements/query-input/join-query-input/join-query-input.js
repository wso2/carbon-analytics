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
         * @class JoinQueryInput
         * @constructor
         * @class JoinQueryInput Creates an input section for a Join Query
         * @param {Object} options Rendering options for the view
         */
        var JoinQueryInput = function (options) {
            /*
             Data storing structure as follows.
                type*: 'JOIN',
                joinWith*: 'STREAM|TABLE|WINDOW|AGGREGATION|TRIGGER',
                left*: {Join Element JSON},
                joinType*: 'JOIN|LEFT_OUTER|RIGHT_OUTER|FULL_OUTER',
                right*: {Join Element JSON},
                on: '',
                within: '', // If joinWith == aggregation
                per: '' // If joinWith == aggregation
            */
            /*
            *  firstConnectedElement and secondConnectedElement stores data as follows.
            *  These will hold a connected element to the join query(front end use only).
            *  {
            *       name: '',
            *       type: ''
            *  }
            *
            *  This attributes will be deleted from the json when sending to backend.
            * */
            this.type = 'JOIN';
            if (options !== undefined) {
                this.firstConnectedElement = options.firstConnectedElement;
                this.secondConnectedElement = options.secondConnectedElement;
                this.joinWith = (options.joinWith !== undefined) ? (options.joinWith).toUpperCase() : undefined;
                this.left = options.left;
                this.joinType = (options.joinType !== undefined) ? (options.joinType).toUpperCase() : undefined;
                this.right = options.right;
                this.on = options.on;
                this.within = options.within;
                this.per = options.per;
            }
        };

        JoinQueryInput.prototype.getType = function () {
            return this.type;
        };

        JoinQueryInput.prototype.getFirstConnectedElement = function () {
            return this.firstConnectedElement;
        };

        JoinQueryInput.prototype.getSecondConnectedElement = function () {
            return this.secondConnectedElement;
        };

        JoinQueryInput.prototype.getJoinWith = function () {
            return this.joinWith;
        };

        JoinQueryInput.prototype.getLeft = function () {
            return this.left;
        };

        JoinQueryInput.prototype.getJoinType = function () {
            return this.joinType;
        };

        JoinQueryInput.prototype.getRight = function () {
            return this.right;
        };

        JoinQueryInput.prototype.getOn = function () {
            return this.on;
        };

        JoinQueryInput.prototype.getWithin = function () {
            return this.within;
        };

        JoinQueryInput.prototype.getPer = function () {
            return this.per;
        };

        JoinQueryInput.prototype.setFirstConnectedElement = function (firstConnectedElement) {
            this.firstConnectedElement = firstConnectedElement;
        };

        JoinQueryInput.prototype.setSecondConnectedElement = function (secondConnectedElement) {
            this.secondConnectedElement = secondConnectedElement;
        };

        JoinQueryInput.prototype.setJoinWith = function (joinWith) {
            this.joinWith = joinWith.toUpperCase();
        };

        JoinQueryInput.prototype.setLeft = function (left) {
            this.left = left;
        };

        JoinQueryInput.prototype.setJoinType = function (joinType) {
            this.joinType = joinType.toUpperCase();
        };

        JoinQueryInput.prototype.setRight = function (right) {
            this.right = right;
        };

        JoinQueryInput.prototype.setOn = function (on) {
            this.on = on;
        };

        JoinQueryInput.prototype.setWithin = function (within) {
            this.within = within;
        };

        JoinQueryInput.prototype.setPer = function (per) {
            this.per = per;
        };

        return JoinQueryInput;

    });
