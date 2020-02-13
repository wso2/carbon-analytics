/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

        /**
         * @class Attribute
         * @constructor
         * @class Attribute  Creates an object to hold an attribute
         * @param {Object} options Rendering options for the view
         */
        var Attribute = function (options) {
            /*
             Data storing structure as follows
             {
             name: ‘’,
             type: ‘’
             }
             */
            if (options !== undefined) {
                this.name = options.name;
                this.type = (options.type !== undefined) ? (options.type).toUpperCase() : undefined;
            }
        };

        Attribute.prototype.getName = function () {
            return this.name;
        };

        Attribute.prototype.getType = function () {
            return this.type;
        };

        Attribute.prototype.setName = function (name) {
            this.define = name;
        };

        Attribute.prototype.setType = function (type) {
            this.type = type.toUpperCase();
        };

        return Attribute;

    });
