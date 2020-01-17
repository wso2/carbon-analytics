/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

        /**
         * @class SourceOrSinkAnnotation
         * @constructor
         * @class SourceOrSinkAnnotation  Creates a Source Or Sink Annotation
         * @param {Object} options Rendering options for the view
         */
        var SourceOrSinkAnnotation = function (options) {
            /*
             Data storing structure as follows
             id*: ‘’,
             previousCommentSegment:'',
             connectedElementName*: '',
             annotationType*: 'SOURCE | SINK',
             type*: ‘’,
             options: ['option1', 'option2=value2',...],
             map: {
             type*: ‘’,
             options: {Key-Value Pair JSON},
             payloadOrAttribute: {
             annotationType: 'PAYLOAD | ATTRIBUTES',
             type*: ‘MAP’,
             value*: {Key-Value Pair JSON}
             }
             << or >>
             payloadOrAttribute: {
             annotationType: 'PAYLOAD | ATTRIBUTES',
             type*: ‘LIST’,
             value*: ['value1',...]
             }
             }
             */
            if (options !== undefined) {
                this.id = options.id;
                this.previousCommentSegment = options.previousCommentSegment;
                this.connectedElementName = options.connectedElementName;
                this.connectedRightElementName = options.connectedRightElementName;
                this.annotationType
                    = (options.annotationType !== undefined) ? (options.annotationType).toUpperCase() : undefined;
                this.type = options.type;
                this.options = options.options;
                this.map = options.map;
            }
        };

        SourceOrSinkAnnotation.prototype.getId = function () {
            return this.id;
        };

        SourceOrSinkAnnotation.prototype.getConnectedElementName = function () {
            return this.connectedElementName;
        };

        SourceOrSinkAnnotation.prototype.getConnectedRightElementName = function () {
            return this.connectedRightElementName;
        };

        SourceOrSinkAnnotation.prototype.getAnnotationType = function () {
            return this.annotationType;
        };

        SourceOrSinkAnnotation.prototype.getType = function () {
            return this.type;
        };

        SourceOrSinkAnnotation.prototype.getOptions = function () {
            return this.options;
        };

        SourceOrSinkAnnotation.prototype.getMap = function () {
            return this.map;
        };

        SourceOrSinkAnnotation.prototype.setId = function (id) {
            this.id = id;
        };

        SourceOrSinkAnnotation.prototype.setConnectedElementName = function (connectedElementName) {
            this.connectedElementName = connectedElementName;
        };

        SourceOrSinkAnnotation.prototype.setConnectedRightElementName = function (connectedRightElementName) {
            this.connectedRightElementName = connectedRightElementName;
        };

        SourceOrSinkAnnotation.prototype.setAnnotationType = function (annotationType) {
            this.annotationType = annotationType.toUpperCase();
        };

        SourceOrSinkAnnotation.prototype.setType = function (type) {
            this.type = type;
        };

        SourceOrSinkAnnotation.prototype.setOptions = function (options) {
            this.options = options;
        };

        SourceOrSinkAnnotation.prototype.setMap = function (map) {
            this.map = map;
        };

        return SourceOrSinkAnnotation;

    });
