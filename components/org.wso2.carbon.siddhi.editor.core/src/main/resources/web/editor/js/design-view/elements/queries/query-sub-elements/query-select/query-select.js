/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

        /**
         * @class QuerySelect
         * @constructor
         * @class QuerySelect  Creates a QuerySelect part in a query
         * @param {Object} options Rendering options for the view
         */
        var QuerySelect = function (options) {
            /*
             Data storing structure as follows
             type*: 'USER_DEFINED',
             value*: [
             {
             expression*: '',
             as: ''
             },
             ...
             ]
             << or >>
             type*: 'ALL',
             value*: '*'
             */
            if (options !== undefined) {
                this.type = (options.type !== undefined) ? (options.type).toUpperCase() : undefined;
                this.value = options.value;
            }
        };

        QuerySelect.prototype.getType = function () {
            return this.type;
        };

        QuerySelect.prototype.getValue = function () {
            return this.value;
        };

        QuerySelect.prototype.setType = function (type) {
            this.type = type.toUpperCase();
        };

        QuerySelect.prototype.setValue = function (value) {
            this.value = value;
        };

        return QuerySelect;

    });
