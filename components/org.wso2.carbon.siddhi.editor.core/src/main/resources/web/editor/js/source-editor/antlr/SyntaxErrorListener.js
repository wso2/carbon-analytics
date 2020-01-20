/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */

"use strict";   // JS strict mode

var ErrorListener = require("../antlr4-js-runtime/error/ErrorListener").ErrorListener;

/**
 * The Syntax Error Listener prototype constructor
 * Inherits from ErrorListener in the antlr4 JS runtime
 *
 * Only recognizing syntax errors is done by this listener
 * Errors are added to the ace editor gutter in the main js
 *
 * Semantic errors such as invalid extension names is not recognized by this listener
 * These errors are recognized using a server side ajax call to validate using the Siddhi engine
 *
 * @constructor
 * @param walker The walker for which this listener is listening for errors
 */
function SyntaxErrorListener(walker) {
    ErrorListener.call(this);
    this.walker = walker;
    return this;
}
SyntaxErrorListener.prototype = Object.create(ErrorListener.prototype);
SyntaxErrorListener.prototype.constructor = SyntaxErrorListener;

SyntaxErrorListener.prototype.syntaxError = function (recognizer, offendingSymbol, line, column, msg, e) {
    this.walker.syntaxErrorList.push({
        row: line - 1,
        column: column,
        text: msg,
        type: "error"
    });
};

exports.SyntaxErrorListener = SyntaxErrorListener;