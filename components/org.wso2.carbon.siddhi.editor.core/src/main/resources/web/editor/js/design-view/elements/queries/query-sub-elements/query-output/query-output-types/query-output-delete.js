/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

        /**
         * @class QueryOutputDelete
         * @constructor
         * @class QueryOutputDelete  Creates a delete type in query output
         * @param {Object} options Rendering options for the view
         */
        var QueryOutputDelete = function (options) {
            /*
             Data storing structure as follows
             eventType: 'CURRENT_EVENTS|EXPIRED_EVENTS|ALL_EVENTS',
             on*: ''
             */
            if (options !== undefined) {
                this.eventType = (options.eventType !== undefined) ? (options.eventType).toUpperCase() : undefined;
                this.on = options.on;
            }
        };

        QueryOutputDelete.prototype.getEventType = function () {
            return this.eventType;
        };

        QueryOutputDelete.prototype.getOn = function () {
            return this.on;
        };

        QueryOutputDelete.prototype.setEventType = function (eventType) {
            this.eventType = eventType.toUpperCase();
        };

        QueryOutputDelete.prototype.setOn = function (on) {
            this.on = on;
        };

        return QueryOutputDelete;

    });
