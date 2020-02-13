/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

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

        return StoreAnnotation;

    });
