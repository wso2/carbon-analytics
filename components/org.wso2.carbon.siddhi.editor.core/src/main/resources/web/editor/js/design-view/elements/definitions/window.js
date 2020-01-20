/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'elementUtils'],
    function (require, ElementUtils) {

        /**
         * @class Window
         * @constructor
         * @class Window  Creates a Window
         * @param {Object} options Rendering options for the view
         */
        var Window = function (options) {
            /*
             Data storing structure as follows
             id: '',
             previousCommentSegment:'',
             name: '',
             attributeList: [
             {
             name: ‘’,
             type: ‘’
             }
             ],
             function*: ‘time|length|timeBatch|lengthBatch...’,
             parameters*: ['value1',...],
             outputEventType: ‘CURRENT_EVENTS|EXPIRED_EVENTS|ALL_EVENTS’,
             annotationList: [annotation1, annotation2, ...]
             */
            if (options !== undefined) {
                this.id = options.id;
                this.previousCommentSegment = options.previousCommentSegment;
                this.name = options.name;
                this.type = options.type;
                this.parameters = options.parameters;
                this.outputEventType
                    = (options.outputEventType !== undefined) ? (options.outputEventType).toUpperCase() : undefined;
            }
            this.annotationListObjects = [];
            this.attributeList = [];
            this.annotationList = [];
        };

        Window.prototype.addAttribute = function (attribute) {
            this.attributeList.push(attribute);
        };

        Window.prototype.addAnnotationObject = function (annotation) {
            this.annotationListObjects.push(annotation)
        };

        Window.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Window.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Window.prototype.clearAnnotationListObjects = function () {
            ElementUtils.prototype.removeAllElements(this.annotationListObjects);
        };

        Window.prototype.clearAttributeList = function () {
            ElementUtils.prototype.removeAllElements(this.attributeList);
        };

        Window.prototype.getId = function () {
            return this.id;
        };

        Window.prototype.getName = function () {
            return this.name;
        };

        Window.prototype.getAttributeList = function () {
            return this.attributeList;
        };

        Window.prototype.getType = function () {
            return this.type;
        };

        Window.prototype.getParameters = function () {
            return this.parameters;
        };

        Window.prototype.getOutputEventType = function () {
            return this.outputEventType;
        };

        Window.prototype.getAnnotationList = function () {
            return this.annotationList;
        };

        Window.prototype.getAnnotationListObjects = function () {
            return this.annotationListObjects;
        };

        Window.prototype.setId = function (id) {
            this.id = id;
        };

        Window.prototype.setName = function (name) {
            this.name = name;
        };

        Window.prototype.setAttributeList = function (attributeList) {
            this.attributeList = attributeList;
        };

        Window.prototype.setType = function (type) {
            this.type = type;
        };

        Window.prototype.setParameters = function (parameters) {
            this.parameters = parameters;
        };

        Window.prototype.setOutputEventType = function (outputEventType) {
            this.outputEventType = outputEventType.toUpperCase();
        };

        Window.prototype.setAnnotationList = function (annotationList) {
            this.annotationList = annotationList;
        };

        return Window;

    });
