/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
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
                previousCommentSegment:'',
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
                this.queryName = options.queryName;
                this.id = options.id;
                this.previousCommentSegment = options.previousCommentSegment;
                this.queryInput = options.queryInput;
                this.select = options.select;
                this.groupBy = options.groupBy;
                this.limit = options.limit;
                this.offset = options.offset;
                this.having = options.having;
                this.outputRateLimit = options.outputRateLimit;
                this.queryOutput = options.queryOutput;
            }
            this.orderBy = [];
            this.annotationList = [];
            this.annotationListObjects = [];
        };

        Query.prototype.addQueryName = function (queryName) {
            this.queryName = queryName;
        };

        Query.prototype.getQueryName = function () {
            return this.queryName;
        };

        Query.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Query.prototype.addAnnotationObject = function (annotationObject) {
            this.annotationListObjects.push(annotationObject);
        };

        Query.prototype.addOrderByValue = function (orderByValue) {
            this.orderBy.push(orderByValue);
        };

        Query.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Query.prototype.clearAnnotationListObjects = function () {
            ElementUtils.prototype.removeAllElements(this.annotationListObjects);
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

        Query.prototype.getOffset = function () {
            return this.offset;
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

        Query.prototype.getAnnotationListObjects = function () {
            return this.annotationListObjects;
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

        Query.prototype.setOffset = function (offset) {
            this.offset = offset;
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

        Query.prototype.resetInputModel = function (model, disconnectedElementName) {
            model.setSelect(undefined);
            var groupBy = model.getGroupBy();
            var orderBy = model.getOrderBy();
            var having = model.getHaving();
            if (groupBy && groupBy.length > 0) {
                model.setGroupBy([" "]);
            }
            if (orderBy && orderBy.length > 0) {
                model.setOrderBy([{ value: "", order: "" }]);
            }
            if (having && having != "") {
                model.setHaving(" ");
            }
            model.getQueryInput().resetModel(model.queryInput, disconnectedElementName);
        };

        Query.prototype.resetOutputModel = function (model) {
            var queryOutput = model.getQueryOutput();
            model.setSelect(undefined);
            queryOutput.setTarget(undefined);
            if (queryOutput.output) {
                if (queryOutput.output.on) {
                    queryOutput.getOutput().setOn("");
                }
                if (queryOutput.output.set && queryOutput.output.set.length != 0) {
                    queryOutput.getOutput().setSet([{attribute: "", value: ""}]);
                }
            }
        };

        return Query;

    });
