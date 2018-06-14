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
         * @class Aggregation
         * @constructor
         * @class Aggregation  Creates an Aggregation definition object
         * @param {Object} options Rendering options for the view
         */
        var Aggregation = function (options) {
            /*
             Data storing structure as follows
                id: '',
                name*: '',
                from*: ‘’,
                select*: [
                    {
                        type*: 'USER_DEFINED',
                        value*: [
                            {
                                expression*: '',
                                as: ''
                            },
                            ...
                        ]
                        << or >>
                        type*: 'ALL',
                        value*: '*'
                    }
                ],
                groupBy: ['value1',...],
                aggregateByAttribute*: ‘’,
                aggregateByTimePeriod*: {
                    type*: 'RANGE',
                    value*: {
                        min*: '',
                        max*: ''
                    }
                    << or >>
                    type*: 'INTERVAL',
                    value*: ['seconds', 'minutes', ...] // At least one value must be available
                },
                store: {Store JSON},
                annotationList: [annotation1, annotation2, ...]
            */
            if (options !== undefined) {
                this.id = options.id;
                this.name = options.name;
                this.from = options.from;
                this.select = options.select;
                this.groupBy = options.groupBy;
                this.aggregateByAttribute = options.aggregateByAttribute;
                this.aggregateByTimePeriod = options.aggregateByTimePeriod;
                this.store = options.store;
            }
            this.annotationList = [];
        };

        Aggregation.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Aggregation.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Aggregation.prototype.getId = function () {
            return this.id;
        };

        Aggregation.prototype.getName = function () {
            return this.name;
        };

        Aggregation.prototype.getFrom = function () {
            return this.from;
        };

        Aggregation.prototype.getSelect = function () {
            return this.select;
        };

        Aggregation.prototype.getGroupBy = function () {
            return this.groupBy;
        };

        Aggregation.prototype.getAggregateByAttribute = function () {
            return this.aggregateByAttribute;
        };

        Aggregation.prototype.getAggregateByTimePeriod = function () {
            return this.aggregateByTimePeriod;
        };

        Aggregation.prototype.getStore = function () {
            return this.store;
        };

        Aggregation.prototype.getAnnotationList = function () {
            return this.annotationList;
        };

        Aggregation.prototype.setId = function (id) {
            this.id = id;
        };

        Aggregation.prototype.setName = function (name) {
            this.name = name;
        };

        Aggregation.prototype.setFrom = function (from) {
            this.from = from;
        };

        Aggregation.prototype.setSelect = function (select) {
            this.select = select;
        };

        Aggregation.prototype.setGroupBy = function (groupBy) {
            this.groupBy = groupBy;
        };

        Aggregation.prototype.setAggregateByAttribute = function (aggregateByAttribute) {
            this.aggregateByAttribute = aggregateByAttribute;
        };

        Aggregation.prototype.setAggregateByTimePeriod = function (aggregateByTimePeriod) {
            this.aggregateByTimePeriod = aggregateByTimePeriod;
        };

        Aggregation.prototype.setStore = function (store) {
            this.store = store;
        };

        Aggregation.prototype.setAnnotationList = function (annotationList) {
            this.annotationList = annotationList;
        };

        return Aggregation;

    });
