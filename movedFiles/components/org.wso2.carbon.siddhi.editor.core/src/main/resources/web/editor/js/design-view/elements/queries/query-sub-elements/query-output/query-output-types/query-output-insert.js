/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

        /**
         * @class QueryOutputInsert
         * @constructor
         * @class QueryOutputInsert  Creates an insert type in query output
         * @param {Object} options Rendering options for the view
         */
        var QueryOutputInsert = function (options) {
            /*
             Data storing structure as follows
             eventType: 'CURRENT_EVENTS|EXPIRED_EVENTS|ALL_EVENTS'
             */
            if (options !== undefined) {
                this.eventType = (options.eventType !== undefined) ? (options.eventType).toUpperCase() : undefined;
            }
        };

        QueryOutputInsert.prototype.getEventType = function () {
            return this.eventType;
        };

        QueryOutputInsert.prototype.setEventType = function (eventType) {
            this.eventType = eventType.toUpperCase();
        };

        return QueryOutputInsert;

    });
