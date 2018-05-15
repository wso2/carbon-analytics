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
         * @class PatternOrSequenceQueryInput
         * @constructor
         * @class PatternOrSequenceQueryInput Creates an input section for Pattern/Sequence Query
         * @param {Object} options Rendering options for the view
         */
        var PatternOrSequenceQueryInput = function (options) {
            /*
             Data storing structure as follows.
                type*: 'pattern|sequence',
                connectedElementList: [],
                conditionList*: [
                    {
                        conditionId*: '',
                        streamName*: '',
                        filter: ''
                    },
                    ...
                ],
                logic*: ''
            */
            if (options !== undefined) {
                this.type = options.type;
                this.logic = options.logic;
            }
            // This will hold all the connected streams to the pattern/sequence query(front end use only).
            // This attribute will be deleted from the json when sending to backend.
            this.connectedElementNameList = [];
            this.conditionList = [];

        };

        PatternOrSequenceQueryInput.prototype.addConnectedElementName = function (connectedElementName) {
            this.connectedElementNameList.push(connectedElementName);
        };

        PatternOrSequenceQueryInput.prototype.addCondition = function (condition) {
            this.conditionList.push(condition);
        };

        PatternOrSequenceQueryInput.prototype.removeConnectedElementName = function (connectedElementName) {
            var index = this.connectedElementNameList.indexOf(connectedElementName);
            if (index > -1) {
                this.connectedElementNameList.splice(index, 1);
            }
        };

        PatternOrSequenceQueryInput.prototype.clearConditionList = function () {
            ElementUtils.prototype.removeAllElements(this.conditionList);
        };

        PatternOrSequenceQueryInput.prototype.getType = function () {
            return this.type;
        };

        PatternOrSequenceQueryInput.prototype.getConnectedElementNameList = function () {
            return this.connectedElementNameList;
        };

        PatternOrSequenceQueryInput.prototype.getConditionList = function () {
            return this.conditionList;
        };

        PatternOrSequenceQueryInput.prototype.getLogic = function () {
            return this.logic;
        };

        PatternOrSequenceQueryInput.prototype.setType = function (type) {
            this.type = type;
        };

        PatternOrSequenceQueryInput.prototype.setConnectedElementNameList = function (connectedElementNameList) {
            this.connectedElementNameList = connectedElementNameList;
        };

        PatternOrSequenceQueryInput.prototype.setConditionList = function (conditionList) {
            this.conditionList = conditionList;
        };

        PatternOrSequenceQueryInput.prototype.setLogic = function (logic) {
            this.logic = logic;
        };

        return PatternOrSequenceQueryInput;

    });
