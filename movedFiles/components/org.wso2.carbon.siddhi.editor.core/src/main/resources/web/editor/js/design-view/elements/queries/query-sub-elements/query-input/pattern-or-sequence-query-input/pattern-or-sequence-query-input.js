/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'elementUtils'],
    function (require, ElementUtils) {

        /**
         * @class PatternOrSequenceQueryInput
         * @constructor
         * @class PatternOrSequenceQueryInput Creates an input section for Pattern/Sequence Query
         * @param {Object} options Rendering options for the view
         */
        var PatternOrSequenceQueryInput = function (options) {
            /*
             Data storing structure as follows.
                type*: 'PATTERN|SEQUENCE',
                connectedElementList: [],
                conditionList*: [
                    {
                        conditionId*: '',
                        streamName*: '',
                        filter: ''
                    },
                    ...
                ],
                logic*: ''
            */
            if (options !== undefined) {
                this.type = (options.type !== undefined) ? (options.type).toUpperCase() : undefined;
                this.logic = options.logic;
            }
            // This will hold all the connected streams to the pattern/sequence query(front end use only).
            // This attribute will be deleted from the json when sending to backend.
            this.connectedElementNameList = [];
            this.conditionList = [];

        };

        PatternOrSequenceQueryInput.prototype.addConnectedElementName = function (connectedElementName) {
            this.connectedElementNameList.push(connectedElementName);
        };

        PatternOrSequenceQueryInput.prototype.addCondition = function (condition) {
            this.conditionList.push(condition);
        };

        PatternOrSequenceQueryInput.prototype.removeConnectedElementName = function (connectedElementName) {
            var index = this.connectedElementNameList.indexOf(connectedElementName);
            if (index > -1) {
                this.connectedElementNameList.splice(index, 1);
            }
        };

        PatternOrSequenceQueryInput.prototype.removeConditionsWhereStreamNameIsUsed = function (elementName) {
            var self = this;
            var i;
            for (i = self.conditionList.length - 1; i >= 0; --i) {
                if (self.conditionList[i].getStreamName() === elementName) {
                    self.conditionList.splice(i, 1);
                }
            }
        };

        PatternOrSequenceQueryInput.prototype.clearConditionList = function () {
            ElementUtils.prototype.removeAllElements(this.conditionList);
        };

        PatternOrSequenceQueryInput.prototype.getType = function () {
            return this.type;
        };

        PatternOrSequenceQueryInput.prototype.getConnectedElementNameList = function () {
            return this.connectedElementNameList;
        };

        PatternOrSequenceQueryInput.prototype.getConditionList = function () {
            return this.conditionList;
        };

        PatternOrSequenceQueryInput.prototype.getLogic = function () {
            return this.logic;
        };

        PatternOrSequenceQueryInput.prototype.setType = function (type) {
            this.type = type.toUpperCase();
        };

        PatternOrSequenceQueryInput.prototype.setConnectedElementNameList = function (connectedElementNameList) {
            this.connectedElementNameList = connectedElementNameList;
        };

        PatternOrSequenceQueryInput.prototype.setConditionList = function (conditionList) {
            this.conditionList = conditionList;
        };

        PatternOrSequenceQueryInput.prototype.setLogic = function (logic) {
            this.logic = logic;
        };

        PatternOrSequenceQueryInput.prototype.resetModel = function (queryInput, disconnectedElementName) {
            var logic = queryInput.getLogic();
            queryInput.removeConnectedElementName(disconnectedElementName);
            queryInput.removeConditionsWhereStreamNameIsUsed(disconnectedElementName);
            if (logic && logic != "") {
                queryInput.setLogic(" ");
            }
        };

        return PatternOrSequenceQueryInput;

    });
