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

define(['require', 'elementArray'],
    function (require, ElementArray) {

        /**
         * @class AppData
         * @constructor
         * @class DesignView  Wraps the Ace editor for design view
         */
        var AppData = function () {
            //initiates the collections
            this.streamList = new ElementArray();
            this.filterList = new ElementArray();
            this.passThroughList = new ElementArray();
            this.windowQueryList = new ElementArray();
            this.queryList = new ElementArray();
            this.patternList = new ElementArray();
            this.joinQueryList = new ElementArray();
            this.partitionList = new ElementArray();

        };

        AppData.prototype.AddStream = function (stream) {
            this.streamList.push(stream);
        };

        AppData.prototype.AddFilterQuery = function (filterQuery) {
            this.filterList.push(filterQuery);
        };

        AppData.prototype.AddPassThroughQuery = function (passThroughQuery) {
            this.passThroughList.push(passThroughQuery);
        };

        AppData.prototype.AddWindowQuery = function (windowQuery) {
            this.windowQueryList.push(windowQuery);
        };

        AppData.prototype.AddQuery = function (query) {
            this.queryList.push(query);
        };

        AppData.prototype.AddPatternQuery = function (patternQuery) {
            this.patternList.push(patternQuery);
        };

        AppData.prototype.AddJoinQuery = function (joinQuery) {
            this.joinQueryList.push(joinQuery);
        };

        AppData.prototype.AddPartition = function (partition) {
            this.partitionList.push(partition);
        };

        AppData.prototype.getStream = function (streamId) {
            return this.streamList.getElement(streamId);
        };

        AppData.prototype.getFilterQuery = function (filterQueryId) {
            return this.filterList.getElement(filterQueryId);
        };

        AppData.prototype.getPassThroughQuery = function (passThroughQueryId) {
            return this.passThroughList.getElement(passThroughQueryId);
        };

        AppData.prototype.getWindowQuery = function (windowQueryId) {
            return this.windowQueryList.getElement(windowQueryId);
        };

        AppData.prototype.getQuery = function (queryId) {
            return this.queryList.getElement(queryId);
        };

        AppData.prototype.getPatternQuery = function (patternQueryId) {
            return this.patternList.getElement(patternQueryId);
        };

        AppData.prototype.getJoinQuery = function (joinQueryId) {
            return this.joinQueryList.getElement(joinQueryId);
        };

        AppData.prototype.getPartition = function (partitionId) {
            return this.partitionList.getElement(partitionId);
        };

        return AppData;
    });
