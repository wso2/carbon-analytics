/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {
        /**
         * @class Map
         * @constructor
         * @class Map  Creates a Map
         * @param {Object} options Rendering options for the view
         */
        var Map = function (options) {
            /*
             Data storing structure as follows
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
             } NOTE: LIST will contain only one value in sink mapper payload.
             */
            if (options !== undefined) {
                this.type = options.type;
                this.options = options.options;
                this.payloadOrAttribute = options.payloadOrAttribute;
            }
        };

        Map.prototype.getType = function () {
            return this.type;
        };

        Map.prototype.getOptions = function () {
            return this.options;
        };

        Map.prototype.getPayloadOrAttribute = function () {
            return this.payloadOrAttribute;
        };

        Map.prototype.setType = function (type) {
            this.type = type;
        };

        Map.prototype.setOptions = function (options) {
            this.options = options;
        };

        Map.prototype.setPayloadOrAttribute = function (payloadOrAttribute) {
            this.payloadOrAttribute = payloadOrAttribute;
        };

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

        /**
         * @class PayloadOrAttribute
         * @constructor
         * @class PayloadOrAttribute  Creates a Payload Or Attribute object
         * @param {Object} options Rendering options for the view
         */
        var PayloadOrAttribute = function (options) {
            /*
             Data storing structure as follows
             annotationType: 'PAYLOAD' | 'ATTRIBUTES',
             type*: ‘MAP' | 'LIST’,
             value*: {Key-Value Pair JSON} | ['value1',...]

             NOTE: LIST will contain only one value in sink mapper payload.
             */
            if (options !== undefined) {
                this.annotationType
                    = (options.annotationType !== undefined) ? (options.annotationType).toUpperCase() : undefined;
                this.type = (options.type !== undefined) ? (options.type).toUpperCase() : undefined;
                this.value = options.value;
            }
        };

        PayloadOrAttribute.prototype.getAnnotationType = function () {
            return this.annotationType;
        };

        PayloadOrAttribute.prototype.getType = function () {
            return this.type;
        };

        PayloadOrAttribute.prototype.getValue = function () {
            return this.value;
        };

        PayloadOrAttribute.prototype.setAnnotationType = function (annotationType) {
            this.annotationType = annotationType.toUpperCase();
        };

        PayloadOrAttribute.prototype.setType = function (type) {
            this.type = type.toUpperCase();
        };
        PayloadOrAttribute.prototype.setValue = function (value) {
            this.value = value;
        };

        return PayloadOrAttribute;

        /**
         * @class StoreAnnotation
         * @constructor
         * @class StoreAnnotation  Creates a Store Annotation
         * @param {Object} options Rendering options for the view
         */
        var StoreAnnotation = function (options) {
            /*
             Data storing structure as follows
             type*: ‘’,
             options*: {Key-Value Pair JSON}
             */
            if (options !== undefined) {
                this.type = options.type;
                this.options = options.options;
            }
        };

        StoreAnnotation.prototype.getType = function () {
            return this.type;
        };

        StoreAnnotation.prototype.getOptions = function () {
            return this.options;
        };

        StoreAnnotation.prototype.setType = function (type) {
            this.type = type;
        };

        StoreAnnotation.prototype.setOptions = function (options) {
            this.options = options;
        };

        /**
         * @class AnnotationObject
         * @constructor
         * @class AnnotationObject creates an object to hold an annotation object
         * @param {Object} options Rendering options for the view
         */

        var AnnotationObject = function (options) {
            if (options !== undefined) {
                this.name = options.name;
                this.elements = options.elements;
                this.annotations = options.annotations;
            } else {
                this.elements = [];
                this.annotations = [];
            }
        };

        AnnotationObject.prototype.getName = function () {
            return this.name;
        };

        AnnotationObject.prototype.setName = function (name) {
            this.name = name;
        };

        AnnotationObject.prototype.addElement = function (element) {
            this.elements.push(element)
        };

        AnnotationObject.prototype.addAnnotation = function (annotation) {
            this.annotations.push(annotation)
        };

        return {
            "mapAnnotation": Map,
            "sourceOrSinkAnnotation": SourceOrSinkAnnotation,
            "payloadOrAttribute": PayloadOrAttribute,
            "storeAnnotation": StoreAnnotation,
            "annotationObject": AnnotationObject
        };

    });
