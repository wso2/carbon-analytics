/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'elementUtils'],
    function (require, ElementUtils) {

        /**
         * @class WindowFilterProjectionQueryInput
         * @constructor
         * @class WindowFilterProjectionQueryInput Creates an input section for a Window/Filter/Projection Query
         * @param {Object} options Rendering options for the view
         */
        var WindowFilterProjectionQueryInput = function (options) {
            /*
             Data storing structure as follows.
                type*: 'FUNCTION|WINDOW|FILTER|PROJECTION',
                from*: '',
                streamHandlerList: [
                    {
                        type*: 'FILTER',
                        value*: ''
                    },
                    << and|or >>
                    {
                        type*: 'FUNCTION|WINDOW',
                        value*: {
                            function*: '',
                            parameters*: ['value1',...],
                        }
                    },
                    ...
                ]
            */
            if (options !== undefined) {
                this.type = (options.type !== undefined) ? (options.type).toUpperCase() : undefined;
                this.from = options.from;
            }
            this.streamHandlerList = [];
        };

        WindowFilterProjectionQueryInput.prototype.addStreamHandler = function (streamHandler) {
            this.streamHandlerList.push(streamHandler);
        };

        WindowFilterProjectionQueryInput.prototype.clearStreamHandlerList = function () {
            ElementUtils.prototype.removeAllElements(this.streamHandlerList);
        };

        WindowFilterProjectionQueryInput.prototype.getType = function () {
            return this.type;
        };

        WindowFilterProjectionQueryInput.prototype.getConnectedSource = function () {
            return this.from;
        };

        WindowFilterProjectionQueryInput.prototype.getStreamHandlerList = function () {
            return this.streamHandlerList;
        };

        WindowFilterProjectionQueryInput.prototype.setType = function (type) {
            this.type = type.toUpperCase();
        };

        WindowFilterProjectionQueryInput.prototype.setConnectedSource = function (from) {
            this.from = from;
        };

        WindowFilterProjectionQueryInput.prototype.setStreamHandlerList = function (streamHandlerList) {
            this.streamHandlerList = streamHandlerList;
        };

        WindowFilterProjectionQueryInput.prototype.resetModel = function (queryInput) {
            queryInput.setConnectedSource(undefined);
        };

        return WindowFilterProjectionQueryInput;

    });
