/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

        /**
         * @class QueryOrderBy
         * @constructor
         * @class QueryOrderBy  Creates a QueryOrderBy part in a query
         * @param {Object} options Rendering options for the view
         */
        var QueryOrderBy = function (options) {
            /*
             Data storing structure as follows
             value*: '',
             order: 'ASC|DESC'
             */
            if (options !== undefined) {
                this.value = options.value;
                this.order = (options.order !== undefined) ? (options.order).toUpperCase() : undefined;
            }
        };

        QueryOrderBy.prototype.getValue = function () {
            return this.value;
        };

        QueryOrderBy.prototype.getOrder = function () {
            return this.order;
        };

        QueryOrderBy.prototype.setValue = function (value) {
            this.value = value;
        };

        QueryOrderBy.prototype.setOrder = function (order) {
            this.order = order.toUpperCase();
        };

        return QueryOrderBy;

    });
