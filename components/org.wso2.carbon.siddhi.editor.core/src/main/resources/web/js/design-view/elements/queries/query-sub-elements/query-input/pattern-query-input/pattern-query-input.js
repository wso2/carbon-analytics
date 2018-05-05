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
         * @class PatternQueryInput
         * @constructor
         * @class PatternQueryInput Creates an input section for Pattern Query
         */
        var PatternQueryInput = function () {
            /*
             Data storing structure as follows.
                type*: 'pattern',
                connectedElementList: [],
                eventList*: [
                    {COUNTING JSON | ANDOR JSON | NOTFOR JSON | NOTAND JSON},
                    ...
                ]
            */
            this.type = 'pattern';
            //TODO: remove this connectedElementNameList when sending the json to backend
            this.connectedElementNameList = []; // This will hold all the connected streams to the pattern query(front end use only)
            this.eventList = [];
        };

        PatternQueryInput.prototype.addConnectedElementName = function (connectedElementName) {
            this.connectedElementNameList.push(connectedElementName);
        };

        PatternQueryInput.prototype.addEvent = function (event) {
            this.eventList.push(event);
        };

        PatternQueryInput.prototype.removeConnectedElementName = function (connectedElementName) {
            var index = this.connectedElementNameList.indexOf(connectedElementName);
             if (index > -1) {
                 this.connectedElementNameList.splice(index, 1);
             }
        };

        PatternQueryInput.prototype.clearEventList = function () {
            ElementUtils.prototype.removeAllElements(this.eventList);
        };

        PatternQueryInput.prototype.getConnectedElementNameList = function () {
            return this.connectedElementNameList;
        };

        PatternQueryInput.prototype.getType = function () {
            return this.type;
        };

        PatternQueryInput.prototype.getEventList = function () {
            return this.eventList;
        };

        PatternQueryInput.prototype.setConnectedElementNameList = function (connectedElementNameList) {
            this.connectedElementNameList = connectedElementNameList;
        };

        PatternQueryInput.prototype.setEventList = function (eventList) {
            this.eventList = eventList;
        };

        return PatternQueryInput;

    });
