/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

        /**
         * @class AggregateByTimePeriod
         * @constructor
         * @class AggregateByTimePeriod  Creates a AggregateByTimePeriod in aggregation definition select section
         * @param {Object} options Rendering options for the view
         */
        var AggregateByTimePeriod = function (options) {
            /*
             Data storing structure as follows
             type*: 'RANGE',
             value*: {
             min*: '',
             max*: ''
             }
             << or >>
             type*: 'INTERVAL',
             value*: ['seconds', 'minutes', ...] // At least one value must be available
             */
            if (options !== undefined) {
                this.type = (options.type !== undefined) ? (options.type).toUpperCase() : undefined;
                this.value = options.value;
            }
        };

        AggregateByTimePeriod.prototype.getType = function () {
            return this.type.toUpperCase();
        };

        AggregateByTimePeriod.prototype.getValue = function () {
            return this.value;
        };

        AggregateByTimePeriod.prototype.setType = function (type) {
            this.type = type.toUpperCase();
        };

        AggregateByTimePeriod.prototype.setValue = function (value) {
            this.value = value;
        };

        return AggregateByTimePeriod;

    });
