/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

        /**
         * @class Function
         * @constructor
         * @class Function  Creates a Function Definition
         * @param {Object} options Rendering options for the view
         */
        var Function = function (options) {
            /*
             Data storing structure as follows
             id*: '',
             previousCommentSegment:'',
             name*: '',
             scriptType*: 'JAVASCRIPT | R | SCALA',
             returnType*: 'INT | LONG | DOUBLE | FLOAT | STRING | BOOL | OBJECT',
             body*: ''
             */
            if (options !== undefined) {
                this.id = options.id;
                this.previousCommentSegment = options.previousCommentSegment;
                this.name = options.name;
                this.scriptType = (options.scriptType !== undefined) ? (options.scriptType).toUpperCase() : undefined;
                this.returnType = (options.returnType !== undefined) ? (options.returnType).toUpperCase() : undefined;
                this.body = options.body;
            }
        };

        Function.prototype.getId = function () {
            return this.id;
        };

        Function.prototype.getName = function () {
            return this.name;
        };

        Function.prototype.getScriptType = function () {
            return this.scriptType;
        };

        Function.prototype.getReturnType = function () {
            return this.returnType;
        };

        Function.prototype.getBody = function () {
            return this.body;
        };

        Function.prototype.setId = function (id) {
            this.id = id;
        };

        Function.prototype.setName = function (name) {
            this.name = name;
        };

        Function.prototype.setScriptType = function (scriptType) {
            this.scriptType = scriptType.toUpperCase();
        };

        Function.prototype.setReturnType = function (returnType) {
            this.returnType = returnType.toUpperCase();
        };

        Function.prototype.setBody = function (body) {
            this.body = body;
        };

        return Function;

    });
