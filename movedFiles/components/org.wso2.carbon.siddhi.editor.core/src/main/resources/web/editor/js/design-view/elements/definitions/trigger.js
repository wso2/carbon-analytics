/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'elementUtils'],
    function (require, ElementUtils) {

        /**
         * @class Trigger
         * @constructor
         * @class Trigger  Creates a Trigger
         * @param {Object} options Rendering options for the view
         */
        var Trigger = function (options) {
            /*
             Data storing structure as follows
             id*: '',
             previousCommentSegment:'',
             name*: '',
             at*: ‘’,
             atEvery*: '',
             annotationList: [annotation1, annotation2, ...]
             */
            if (options !== undefined) {
                this.id = options.id;
                this.previousCommentSegment = options.previousCommentSegment;
                this.name = options.name;
                this.criteria = options.criteria;
                this.criteriaType = options.criteriaType;
            }
            this.annotationList = [];
        };

        Trigger.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Trigger.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Trigger.prototype.getId = function () {
            return this.id;
        };

        Trigger.prototype.getName = function () {
            return this.name;
        };

        Trigger.prototype.getCriteria = function () {
            return this.criteria;
        };

        Trigger.prototype.getCriteriaType = function () {
            return this.criteriaType;
        };

        Trigger.prototype.getAnnotationList = function () {
            return this.annotationList;
        };

        Trigger.prototype.setId = function (id) {
            this.id = id;
        };

        Trigger.prototype.setName = function (name) {
            this.name = name;
        };

        Trigger.prototype.setCriteria = function (criteria) {
            this.criteria = criteria;
        };

        Trigger.prototype.setCriteriaType = function (criteriaType) {
            this.criteriaType = criteriaType;
        };

        Trigger.prototype.setAnnotationList = function (annotationList) {
            this.annotationList = annotationList;
        };

        return Trigger;

    });
