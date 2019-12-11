/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

        /**
         * @class Annotation
         * @constructor
         * @class Annotation  Creates an object to hold annotations for a particular element
         * @param {Object} options Rendering options for the view
         */
        var Annotation = function (options) {
            /*
             Data storing structure as follows
             {
             name: ‘’,
             type: ‘VALUE’,
             value: [‘value1’,’value2’]
             },
             and|or
             {
             name: ‘’
             type: ‘KEY_VALUE’,
             value: {‘option’:’value’}
             }
             */
            if (options !== undefined) {
                this.name = options.name;
                this.type = options.type;
                this.value = options.value;
            }
        };

        Annotation.prototype.getName = function () {
            return this.name;
        };

        Annotation.prototype.getType = function () {
            return this.type;
        };

        Annotation.prototype.getValue = function () {
            return this.value;
        };

        Annotation.prototype.setName = function (name) {
            this.define = name;
        };

        Annotation.prototype.setType = function (type) {
            this.type = type;
        };

        Annotation.prototype.setValue = function (value) {
            this.value = value;
        };

        return Annotation;

    });
