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
         * @class Query
         * @constructor
         * @class Query  Creates a Query
         * @param {Object} options Rendering options for the view
         */
        var Query = function (options) {
            /*
             Data storing structure as follows.

                id*: '',
                queryInput*: {Query Input JSON},
                select*: {Query Select JSON},
                groupBy: ['value1',...],
                orderBy: [
                    {
                        value*: '',
                        order: 'ASC|DESC'
                    },
                    ...
                ],
                limit: <long>,
                having: '',
                outputRateLimit: ''
                queryOutput*: {Query Output JSON},
                annotationList: [annotation1, annotation2, ...]
            */
            if (options !== undefined) {
                this.id = options.id;
                this.queryInput = options.queryInput;
                this.select = options.select;
                this.groupBy = options.groupBy;
                this.limit = options.limit;
                this.having = options.having;
                this.outputRateLimit = options.outputRateLimit;
                this.queryOutput = options.queryOutput;
            }
            this.orderBy = [];
            this.annotationList = [];
        };

        Query.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Query.prototype.addOrderByValue = function (orderByValue) {
            this.orderBy.push(orderByValue);
        };

        Query.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Query.prototype.clearOrderByValueList = function () {
            ElementUtils.prototype.removeAllElements(this.orderBy);
        };

        Query.prototype.getId = function () {
            return this.id;
        };

        Query.prototype.getQueryInput = function () {
            return this.queryInput;
        };

        Query.prototype.getSelect = function () {
            return this.select;
        };

        Query.prototype.getGroupBy = function () {
            return this.groupBy;
        };

        Query.prototype.getOrderBy = function () {
            return this.orderBy;
        };

        Query.prototype.getLimit = function () {
            return this.limit;
        };

        Query.prototype.getHaving = function () {
            return this.having;
        };

        Query.prototype.getOutputRateLimit = function () {
            return this.outputRateLimit;
        };

        Query.prototype.getQueryOutput = function () {
            return this.queryOutput;
        };

        Query.prototype.getAnnotationList = function () {
            return this.annotationList;
        };

        Query.prototype.setId = function (id) {
            this.id = id;
        };

        Query.prototype.setQueryInput = function (queryInput) {
            this.queryInput = queryInput;
        };

        Query.prototype.setSelect = function (select) {
            this.select = select;
        };

        Query.prototype.setGroupBy = function (groupBy) {
            this.groupBy = groupBy;
        };

        Query.prototype.setOrderBy = function (orderBy) {
            this.orderBy = orderBy;
        };

        Query.prototype.setLimit = function (limit) {
            this.limit = limit;
        };

        Query.prototype.setHaving = function (having) {
            this.having = having;
        };

        Query.prototype.setOutputRateLimit = function (outputRateLimit) {
            this.outputRateLimit = outputRateLimit;
        };

        Query.prototype.setQueryOutput = function (queryOutput) {
            this.queryOutput = queryOutput;
        };

        Query.prototype.setAnnotationList = function (annotationList) {
            this.annotationList = annotationList;
        };

        return Query;

    });
