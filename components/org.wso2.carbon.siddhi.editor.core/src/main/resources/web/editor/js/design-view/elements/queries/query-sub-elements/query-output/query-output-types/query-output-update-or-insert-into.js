/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

        /**
         * @class QueryOutputUpdateOrInsertInto
         * @constructor
         * @class QueryOutputUpdateOrInsertInto  Creates a update type in query output
         * @param {Object} options Rendering options for the view
         */
        var QueryOutputUpdateOrInsertInto = function (options) {
            /*
             Data storing structure as follows
             eventType: 'CURRENT_EVENTS|EXPIRED_EVENTS|ALL_EVENTS',
             set*: [
             {
             attribute*: '',
             value*: ''
             },
             ...
             ],
             on*: ''
             */
            if (options !== undefined) {
                this.eventType = (options.eventType !== undefined) ? (options.eventType).toUpperCase() : undefined;
                this.set = options.set;
                this.on = options.on;
            }
        };

        QueryOutputUpdateOrInsertInto.prototype.getEventType = function () {
            return this.eventType;
        };

        QueryOutputUpdateOrInsertInto.prototype.getSet = function () {
            return this.set;
        };

        QueryOutputUpdateOrInsertInto.prototype.getOn = function () {
            return this.on;
        };

        QueryOutputUpdateOrInsertInto.prototype.setEventType = function (eventType) {
            this.eventType = eventType.toUpperCase();
        };

        QueryOutputUpdateOrInsertInto.prototype.setSet = function (set) {
            this.set = set;
        };

        QueryOutputUpdateOrInsertInto.prototype.setOn = function (on) {
            this.on = on;
        };

        return QueryOutputUpdateOrInsertInto;

    });
