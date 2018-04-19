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
         * @class PatternQuery
         * @constructor
         * @class PatternQuery  Creates a Pattern Query
         * @param {Object} options Rendering options for the view
         */
        var PatternQuery = function (options) {
            /*
             Data storing structure as follows.

                id*: '',
                queryInput*: {
                    type*: 'pattern',
                    events*: [
                        {COUNTING JSON | ANDOR JSON | NOTFOR JSON | NOTAND JSON},
                        ...
                    ]
                },
                select*: {
                    type*: 'user_defined',
                    value*: [
                        {
                            expression*: '',
                            as: ''
                        },
                        ...
                    ]
                    << or >>
                    type*: 'all',
                    value*: '*'
                },
                groupBy: ['value1',...],
                having: '',
                outputRateLimit: ''
                queryOutput*: {
                    {
                        type*: 'insert',
                        insert: 'current|expired|all',
                        into*: ''
                    }
                    << or >>
                    {
                        type*: 'delete',
                        target*: '',
                        forEventType: 'current|expired|all',
                        on*: ''
                    }
                    << or >>
                    {
                        type*: 'update',
                        target*: '',
                        forEventType: 'current|expired|all',
                        set*: [
                            {
                                attribute*: '',
                                value*: ''
                            },
                            ...
                        ],
                        on*: ''
                    }
                    << or >>
                    {
                        type*: 'update_or_insert_into',
                        target*: '',
                        forEventType: 'current|expired|all',
                        set*: [
                            {
                                attribute*: '',
                                value*: ''
                            },
                            ...
                        ],
                        on*: ''
                    }
                },
                annotationList: {Annotation JSON Array}
            */
            this.id = options.id;
            this.queryInput = options.queryInput;
            this.select = options.select;
            this.groupBy = options.groupBy;
            this.having = options.having;
            this.outputRateLimit = options.outputRateLimit;
            this.queryOutput = options.queryOutput;
            this.annotationList =  new ElementArray();
        };

        PatternQuery.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        PatternQuery.prototype.getId = function () {
            return this.id;
        };

        PatternQuery.prototype.getQueryInput = function () {
            return this.queryInput;
        };

        PatternQuery.prototype.getSelect = function () {
            return this.select;
        };

        PatternQuery.prototype.getGroupBy = function () {
            return this.groupBy;
        };

        PatternQuery.prototype.getHaving = function () {
            return this.having;
        };

        PatternQuery.prototype.getOutputRateLimit = function () {
            return this.outputRateLimit;
        };

        PatternQuery.prototype.getQueryOutput = function () {
            return this.queryOutput;
        };

        PatternQuery.prototype.getAnnotationList = function () {
            return this.annotationList;
        };

        PatternQuery.prototype.setId = function (id) {
            this.id = id;
        };

        PatternQuery.prototype.setQueryInput = function (queryInput) {
            this.queryInput = queryInput;
        };

        PatternQuery.prototype.setSelect = function (select) {
            this.select = select;
        };

        PatternQuery.prototype.setGroupBy = function (groupBy) {
            this.groupBy = groupBy;
        };

        PatternQuery.prototype.setHaving = function (having) {
            this.having = having;
        };

        PatternQuery.prototype.setOutputRateLimit = function (outputRateLimit) {
            this.outputRateLimit = outputRateLimit;
        };

        PatternQuery.prototype.setQueryOutput = function (queryOutput) {
            this.queryOutput = queryOutput;
        };

        PatternQuery.prototype.setAnnotationList = function (annotationList) {
            this.annotationList = annotationList;
        };

        return PatternQuery;

    });
