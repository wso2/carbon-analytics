/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'elementUtils'],
    function (require, ElementUtils) {

        /**
         * @class PatternOrSequenceQueryCondition
         * @constructor
         * @class PatternOrSequenceQueryCondition  Creates a condition in pattern or sequence query
         * @param {Object} options Rendering options for the view
         */
        var PatternOrSequenceQueryCondition = function (options) {
            /*
             Data storing structure as follows.
             conditionId*: '',
             streamName*: '',
             streamHandlerList: [
             {
             type*: 'FILTER',
             value*: ''
             },
             << and|or >>
             {
             type*: 'FUNCTION',
             value*: {
             function*: '',
             parameters*: ['value1',...],
             }
             },
             ...
             ]
             */
            if (options !== undefined) {
                this.conditionId = options.conditionId;
                this.streamName = options.streamName;
            }
            this.streamHandlerList = [];
        };

        PatternOrSequenceQueryCondition.prototype.addStreamHandler = function (streamHandler) {
            this.streamHandlerList.push(streamHandler);
        };

        PatternOrSequenceQueryCondition.prototype.clearStreamHandlerList = function () {
            ElementUtils.prototype.removeAllElements(this.streamHandlerList);
        };

        PatternOrSequenceQueryCondition.prototype.getConditionId = function () {
            return this.conditionId;
        };

        PatternOrSequenceQueryCondition.prototype.getStreamName = function () {
            return this.streamName;
        };

        PatternOrSequenceQueryCondition.prototype.getStreamHandlerList = function () {
            return this.streamHandlerList;
        };

        PatternOrSequenceQueryCondition.prototype.setConditionId = function (conditionId) {
            this.conditionId = conditionId;
        };

        PatternOrSequenceQueryCondition.prototype.setStreamName = function (streamName) {
            this.streamName = streamName;
        };

        PatternOrSequenceQueryCondition.prototype.setStreamHandlerList = function (streamHandlerList) {
            this.streamHandlerList = streamHandlerList;
        };

        return PatternOrSequenceQueryCondition;

    });
