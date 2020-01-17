/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

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

        return AnnotationObject;

    });
