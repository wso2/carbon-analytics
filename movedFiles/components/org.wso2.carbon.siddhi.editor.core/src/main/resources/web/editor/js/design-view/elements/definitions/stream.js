/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'elementUtils', 'constants'],
    function (require, ElementUtils, Constants) {

        /**
         * @class Stream
         * @constructor
         * @class Stream  Creates a Stream
         * @param {Object} options Rendering options for the view
         */
        var Stream = function (options) {
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
                annotationList: [annotation1, annotation2, ...]
            */
            if (options !== undefined) {
                this.id = options.id;
                this.previousCommentSegment = options.previousCommentSegment;
                this.name = options.name;
            }

            this.annotationListObjects = [];
            this.attributeList = [];
            this.annotationList = [];
        };

        Stream.prototype.addAnnotationObject = function (annotation) {
            this.annotationListObjects.push(annotation)
        }

        Stream.prototype.addAttribute = function (attribute) {
            this.attributeList.push(attribute);
        };

        Stream.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Stream.prototype.clearAnnotationListObjects = function () {
            ElementUtils.prototype.removeAllElements(this.annotationListObjects);
        };

        Stream.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Stream.prototype.clearAttributeList = function () {
            ElementUtils.prototype.removeAllElements(this.attributeList);
        };

        Stream.prototype.getId = function () {
            return this.id;
        };

        Stream.prototype.getName = function () {
            return this.name;
        };

        Stream.prototype.getAttributeList = function () {
            return this.attributeList;
        };

        Stream.prototype.getAnnotationList = function () {
            return this.annotationList;
        };

        Stream.prototype.getAnnotationListObjects = function () {
            return this.annotationListObjects;
        };

        Stream.prototype.setId = function (id) {
            this.id = id;
        };

        Stream.prototype.setName = function (name) {
            this.name = name;
        };

        Stream.prototype.setAttributeList = function (attributeList) {
            this.attributeList = attributeList;
        };

        Stream.prototype.setAnnotationList = function (annotationList) {
            this.annotationList = annotationList;
        };

        Stream.prototype.hasFaultStream = function() {
            // Check if the OnError(action='STREAM') annotation is set. If so enable the fault connector.
            var faultStream = false;
            this.annotationListObjects.forEach(function(annotation) {
                if (annotation.name.toLowerCase() === 'onerror') {
                    annotation.elements.forEach(function(p) {
                        if (p.key.toLowerCase() === 'action' && p.value.toLowerCase() === 'stream') {
                            faultStream = true;
                        }
                    });
                }
            });
            return faultStream;
        };

        Stream.prototype.isFaultStream = function() {
            return this.name && this.name.startsWith(Constants.FAULT_STREAM_PREFIX);
        };

        return Stream;

    });
