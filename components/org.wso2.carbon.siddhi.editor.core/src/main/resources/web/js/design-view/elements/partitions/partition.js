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

define(['require', 'elementUtils', 'lodash'],
    function (require, ElementUtils, _) {

        /**
         * @class Partition
         * @constructor
         * @class Partition  Creates a Partition
         * @param {Object} options Rendering options for the view
         */
        var Partition = function (options) {
            /*
             Data storing structure as follows.
                id*: ‘’,
                streamList = [];
                queryLists: [
                    {
                        '<queryType>': [{Query JSON},...]
                    },
                    ...
                ],
                partitionWith*: [
                    {
                        streamName*: '',
                        expression*: ''
                    },
                    ...
                ],
                annotationList: {Annotation JSON Array}
            */
            if (options !== undefined) {
                this.id = options.id;
            }
            this.streamList = [];
            this.queryLists = {
                WINDOW_FILTER_PROJECTION: [],
                PATTERN: [],
                SEQUENCE: [],
                JOIN: []
            };
            this.partitionWith = [];
            this.annotationList = [];
        };

        Partition.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Partition.prototype.addStream = function (stream) {
            this.streamList.push(stream);
        };

        Partition.prototype.addPartitionWith = function (partitionWith) {
            this.partitionWith.push(partitionWith);
        };

        Partition.prototype.addWindowFilterProjectionQuery = function (windowFilterProjectionQuery) {
            this.queryLists.WINDOW_FILTER_PROJECTION.push(windowFilterProjectionQuery);
        };

        Partition.prototype.addPatternQuery = function (patternQuery) {
            this.queryLists.PATTERN.push(patternQuery);
        };

        Partition.prototype.addSequenceQuery = function (sequenceQuery) {
            this.queryLists.SEQUENCE.push(sequenceQuery);
        };

        Partition.prototype.addJoinQuery = function (joinQuery) {
            this.queryLists.JOIN.push(joinQuery);
        };

        Partition.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Partition.prototype.clearPartitionWith = function () {
            ElementUtils.prototype.removeAllElements(this.partitionWith);
        };

        Partition.prototype.removePartitionWith = function (partitionWithStreamName) {
            var self = this;
            var index = undefined;
            _.forEach(self.partitionWith, function (partitionWith) {
                if (partitionWith.getStreamName() === partitionWithStreamName) {
                    index = self.partitionWith.indexOf(partitionWith);
                }
            });
            if (index > -1) {
                this.partitionWith.splice(index, 1);
            }
        };

        Partition.prototype.removeStream = function (streamId) {
            ElementUtils.prototype.removeElement(this.streamList, streamId);
        };

        Partition.prototype.removeWindowFilterProjectionQuery = function (windowFilterProjectionQueryId) {
            ElementUtils.prototype
                .removeElement(this.queryLists.WINDOW_FILTER_PROJECTION, windowFilterProjectionQueryId);
        };

        Partition.prototype.removePatternQuery = function (patternQueryId) {
            ElementUtils.prototype.removeElement(this.queryLists.PATTERN, patternQueryId);
        };

        Partition.prototype.removeSequenceQuery = function (sequenceQueryId) {
            ElementUtils.prototype.removeElement(this.queryLists.SEQUENCE, sequenceQueryId);
        };

        Partition.prototype.removeJoinQuery = function (joinQueryId) {
            ElementUtils.prototype.removeElement(this.queryLists.JOIN, joinQueryId);
        };

        Partition.prototype.getId = function () {
            return this.id;
        };

        Partition.prototype.getStream = function (streamId) {
            return ElementUtils.prototype.getElement(this.streamList, streamId);
        };

        Partition.prototype.getStreamByName = function (streamName) {
            var self = this;
            var requestedElement;
            _.forEach(self.streamList, function (stream) {
                if (stream.getName() === streamName) {
                    requestedElement = stream;
                }
            });
            return requestedElement;
        };

        Partition.prototype.getWindowFilterProjectionQuery = function (windowFilterProjectionQueryId) {
            return ElementUtils.prototype
                .getElement(this.queryLists.WINDOW_FILTER_PROJECTION, windowFilterProjectionQueryId);
        };

        Partition.prototype.getPatternQuery = function (patternQueryId) {
            return ElementUtils.prototype.getElement(this.queryLists.PATTERN, patternQueryId);
        };

        Partition.prototype.getSequenceQuery = function (sequenceQueryId) {
            return ElementUtils.prototype.getElement(this.queryLists.SEQUENCE, sequenceQueryId);
        };

        Partition.prototype.getJoinQuery = function (joinQueryId) {
            return ElementUtils.prototype.getElement(this.queryLists.JOIN, joinQueryId);
        };

        Partition.prototype.getStreamList = function () {
            return this.streamList;
        };

        Partition.prototype.getWindowFilterProjectionQueryList = function () {
            return this.queryLists.WINDOW_FILTER_PROJECTION;
        };

        Partition.prototype.getPatternQueryList = function () {
            return this.queryLists.PATTERN;
        };

        Partition.prototype.getSequenceQueryList = function () {
            return this.queryLists.SEQUENCE;
        };

        Partition.prototype.getJoinQueryList = function () {
            return this.queryLists.JOIN;
        };

        Partition.prototype.getPartitionWith = function () {
            return this.partitionWith;
        };

        Partition.prototype.getAnnotationList = function () {
            return this.annotationList;
        };

        Partition.prototype.setId = function (id) {
            this.id = id;
        };

        Partition.prototype.setPartitionWith = function (partitionWith) {
            this.partitionWith = partitionWith;
        };

        Partition.prototype.setAnnotationList = function (annotationList) {
            this.annotationList = annotationList;
        };

        Partition.prototype.checkOuterStreamIsAlreadyConnected = function (streamName) {
            var self = this;
            var isStreamConnected = false;
            _.forEach(self.partitionWith, function (partitionWith) {
                if (partitionWith.getStreamName() === streamName) {
                    isStreamConnected = true;
                }
            });
            return isStreamConnected;
        };

        Partition.prototype.getNoOfElementsInPartition = function () {
            var self = this;
            return self.streamList.length + self.queryLists.WINDOW_FILTER_PROJECTION.length +
                self.queryLists.JOIN.length + self.queryLists.PATTERN.length + self.queryLists.SEQUENCE.length;
        };

        Partition.prototype.isElementInsidePartition = function (elementId) {
            var self = this;
            var isElementInsidePartition = false;

            if (self.getWindowFilterProjectionQuery(elementId) !== undefined
                || self.getJoinQuery(elementId) !== undefined
                || self.getPatternQuery(elementId) !== undefined
                || self.getSequenceQuery(elementId) !== undefined
                || self.getStream(elementId) !== undefined) {
                isElementInsidePartition = true;
            }
            return isElementInsidePartition;
        };

        return Partition;

    });
