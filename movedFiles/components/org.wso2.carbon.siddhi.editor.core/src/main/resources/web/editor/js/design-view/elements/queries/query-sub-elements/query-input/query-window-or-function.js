/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

        /**
         * @class QueryWindowOrFunction
         * @constructor
         * @class QueryWindowOrFunction Creates a window or a function for a query
         * @param {Object} options Rendering options for the view
         */
        var QueryWindowOrFunction = function (options) {
            /*
             Data storing structure as follows.
             function*: '',
             parameters: ['value1',...]'
             */
            if (options !== undefined) {
                this.function = options.function;
                this.parameters = options.parameters;
            }
        };

        QueryWindowOrFunction.prototype.getFunction = function () {
            return this.function;
        };

        QueryWindowOrFunction.prototype.getParameters = function () {
            return this.parameters;
        };

        QueryWindowOrFunction.prototype.setFunction = function (functionName) {
            this.function = functionName;
        };

        QueryWindowOrFunction.prototype.setParameters = function (parameters) {
            this.parameters = parameters;
        };

        return QueryWindowOrFunction;

    });
