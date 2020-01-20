/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

        /**
         * @class StreamHandler
         * @constructor
         * @class StreamHandler Creates a Stream Handler object for a query which is stored in streamHandler list
         *          in query input
         * @param {Object} options Rendering options for the view
         */
        var StreamHandler = function (options) {
            /*
             Data storing structure as follows.
             type*: 'FILTER | FUNCTION | WINDOW',
             value*: ''
             */
            if (options !== undefined) {
                this.type
                    = (options.type !== undefined) ? (options.type).toUpperCase() : undefined;
                this.value = options.value;
            }
        };

        StreamHandler.prototype.getType = function () {
            return this.type;
        };

        StreamHandler.prototype.getValue = function () {
            return this.value;
        };

        StreamHandler.prototype.setType = function (type) {
            this.type = type.toUpperCase();
        };

        StreamHandler.prototype.setValue = function (value) {
            this.value = value;
        };

        return StreamHandler;

    });
