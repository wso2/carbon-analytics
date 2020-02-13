/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

        /**
         * @class PartitionWith
         * @constructor
         * @class PartitionWith  Creates PartitionWith
         * @param {Object} options Rendering options for the view
         */
        var PartitionWith = function (options) {
            /*
             Data storing structure as follows
             streamName*: '',
             expression*: ''
             */
            if (options !== undefined) {
                this.streamName = options.streamName;
                this.expression = options.expression;
            }
        };

        PartitionWith.prototype.getStreamName = function () {
            return this.streamName;
        };

        PartitionWith.prototype.getExpression = function () {
            return this.expression;
        };

        PartitionWith.prototype.setStreamName = function (streamName) {
            this.streamName = streamName;
        };

        PartitionWith.prototype.setExpression = function (expression) {
            this.expression = expression;
        };

        return PartitionWith;

    });
