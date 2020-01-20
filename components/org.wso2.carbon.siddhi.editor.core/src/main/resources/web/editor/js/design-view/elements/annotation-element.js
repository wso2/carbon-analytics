/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

        /**
         * @class AnnotationElement
         * @constructor
         * @class AnnotationElement Creates an object to hold an annotation element
         * @param {key} to store the property of the annotation element
         * @param {value} to store the property's value of the annotation element
         */

        var AnnotationElement = function (key, value) {
            this.key = key;
            this.value = value;
        };

        AnnotationElement.prototype.getKey = function () {
            return this.key;
        };

        AnnotationElement.prototype.setKey= function (key) {
            this.key = key;
        };

        AnnotationElement.prototype.getValue = function () {
            return this.value;
        };

        AnnotationElement.prototype.setValue = function (value) {
            this.value = value;
        };

        return AnnotationElement;

    });

