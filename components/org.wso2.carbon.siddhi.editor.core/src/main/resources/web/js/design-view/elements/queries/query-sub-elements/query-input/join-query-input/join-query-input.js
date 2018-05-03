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
                type*: 'join',
                joinWith*: 'stream|table|window|aggregation|trigger',
                left*: {Join Element JSON},
                joinType*: 'join|left_outer|right_outer|full_outer',
                right*: {Join Element JSON},
                on: '',
                within: '', // If joinWith == aggregation
                per: '' // If joinWith == aggregation
            */
            this.type = 'join';
            //TODO: remove this connectedElementNameList when sending the json to backend
            this.connectedElementNameList = []; // This will hold all the connected streams to the join query(front end use only)
            this.joinWith = options.joinWith;
            this.left = options.left;
            this.joinType = options.joinType;
            this.right = options.right;
            this.on = options.on;
            this.within = options.within;
            this.per = options.per;
        };

        JoinQueryInput.prototype.addConnectedElementName = function (connectedElementName) {
            this.connectedElementNameList.push(connectedElementName);
        };

        JoinQueryInput.prototype.removeConnectedElementName = function (connectedElementName) {
            var index = this.connectedElementNameList.indexOf(connectedElementName);
            if (index > -1) {
                this.connectedElementNameList.splice(index, 1);
            }
        };

        JoinQueryInput.prototype.getConnectedElementNameList = function () {
            return this.connectedElementNameList;
        };

        JoinQueryInput.prototype.getType = function () {
            return this.type;
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

        JoinQueryInput.prototype.setConnectedElementNameList = function (connectedElementNameList) {
            this.connectedElementNameList = connectedElementNameList;
        };

        JoinQueryInput.prototype.setJoinWith = function (joinWith) {
            this.joinWith = joinWith;
        };

        JoinQueryInput.prototype.setLeft = function (left) {
            this.left = left;
        };

        JoinQueryInput.prototype.setJoinType = function (joinType) {
            this.joinType = joinType;
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
