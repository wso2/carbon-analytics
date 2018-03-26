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
         * @class AppData  Holds the data for given Siddhi app
         */
        var AppData = function () {
            // initiates the collections
            this.streamList = new ElementArray();
            this.filterList = new ElementArray();
            this.passThroughList = new ElementArray();
            this.windowQueryList = new ElementArray();
            this.queryList = new ElementArray();
            this.patternList = new ElementArray();
            this.joinQueryList = new ElementArray();
            this.partitionList = new ElementArray();
            this.edgeList = new ElementArray();
            // finalElementCount --> Number of elements that exist on the canvas at the time of saving the model
            this.finalElementCount = 0;

        };

        AppData.prototype.addStream = function (stream) {
            this.streamList.push(stream);
        };

        AppData.prototype.addFilterQuery = function (filterQuery) {
            this.filterList.push(filterQuery);
        };

        AppData.prototype.addPassThroughQuery = function (passThroughQuery) {
            this.passThroughList.push(passThroughQuery);
        };

        AppData.prototype.addWindowQuery = function (windowQuery) {
            this.windowQueryList.push(windowQuery);
        };

        AppData.prototype.addQuery = function (query) {
            this.queryList.push(query);
        };

        AppData.prototype.addPatternQuery = function (patternQuery) {
            this.patternList.push(patternQuery);
        };

        AppData.prototype.addJoinQuery = function (joinQuery) {
            this.joinQueryList.push(joinQuery);
        };

        AppData.prototype.addPartition = function (partition) {
            this.partitionList.push(partition);
        };

        AppData.prototype.addEdge = function (edge) {
            this.edgeList.push(edge);
        };

        AppData.prototype.removeStream = function (streamId) {
            this.streamList.removeElement(streamId);
        };

        AppData.prototype.removeFilterQuery = function (filterQueryId) {
            this.filterList.removeElement(filterQueryId);
        };

        AppData.prototype.removePassThroughQuery = function (passThroughQueryId) {
            this.passThroughList.removeElement(passThroughQueryId);
        };

        AppData.prototype.removeWindowQuery = function (windowQueryId) {
            this.windowQueryList.removeElement(windowQueryId);
        };

        AppData.prototype.removeQuery = function (queryId) {
            this.queryList.removeElement(queryId);
        };

        AppData.prototype.removePatternQuery = function (patternQueryId) {
            this.patternList.removeElement(patternQueryId);
        };

        AppData.prototype.removeJoinQuery = function (joinQueryId) {
            this.joinQueryList.removeElement(joinQueryId);
        };

        AppData.prototype.removePartition = function (partitionId) {
            this.partitionList.removeElement(partitionId);
        };

        AppData.prototype.removeEdge = function (edgeId) {
            this.edgeList.removeElement(edgeId);
        };

        AppData.prototype.setFinalElementCount = function (finalElementCount) {
            this.finalElementCount = finalElementCount;
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

        AppData.prototype.getEdge = function (edgeId) {
            return this.edgeList.getElement(edgeId);
        };

        AppData.prototype.getFinalElementCount = function () {
            return this.finalElementCount;
        };

        return AppData;
    });
