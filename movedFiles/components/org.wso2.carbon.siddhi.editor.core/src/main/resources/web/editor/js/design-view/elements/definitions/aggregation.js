/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
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
                previousCommentSegment:'',
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
                this.previousCommentSegment = options.previousCommentSegment;
                this.name = options.name;
                this.from = options.from;
                this.select = options.select;
                this.groupBy = options.groupBy;
                this.aggregateByAttribute = options.aggregateByAttribute;
                this.aggregateByTimePeriod = options.aggregateByTimePeriod;
                this.store = options.store;
            }
            this.annotationList = [];
            this.annotationListObjects = [];
        };

        Aggregation.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Aggregation.prototype.addAnnotationObject = function (annotation) {
            this.annotationListObjects.push(annotation);
        };

        Aggregation.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Aggregation.prototype.clearAnnotationListObjects = function () {
            ElementUtils.prototype.removeAllElements(this.annotationListObjects);
        };

        Aggregation.prototype.getId = function () {
            return this.id;
        };

        Aggregation.prototype.getName = function () {
            return this.name;
        };

        Aggregation.prototype.getConnectedSource = function () {
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

        Aggregation.prototype.getAnnotationListObjects = function () {
            return this.annotationListObjects;
        };

        Aggregation.prototype.setId = function (id) {
            this.id = id;
        };

        Aggregation.prototype.setName = function (name) {
            this.name = name;
        };

        Aggregation.prototype.setConnectedSource = function (from) {
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

        Aggregation.prototype.setAnnotationListObjects = function (annotationListObjects) {
            this.annotationListObjects = annotationListObjects;
        };

        Aggregation.prototype.resetModel = function (model) {
            model.setSelect(undefined);
            var groupBy = model.getGroupBy();
            var aggregateByAttribute = model.getAggregateByAttribute();
            if(groupBy && groupBy.length > 0) {
                model.setGroupBy([" "]);
            }
            if(aggregateByAttribute && aggregateByAttribute != "") {
                model.setAggregateByAttribute(" ");
            }
        };

        return Aggregation;

    });
