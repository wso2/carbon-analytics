/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'log', 'jquery', 'lodash', 'designViewUtils', 'constants'],
    function (require, log, $, _, DesignViewUtils, Constants) {

        /**
         * @class JSONValidator
         * @constructor
         * @class JSONValidator  Validates a given JSON structure by looking for minimum required fields for particular
         * element
         */
        var JSONValidator = function () {
        };

        /**
         * @function Validates a given JSON
         * @param JSON provided JSON
         * @param jsPlumbInstance jsPlumb instance for the current tab
         */
        JSONValidator.prototype.validate = function (JSON, jsPlumbInstance) {
            var self = this;
            var isValid = true;
            var commonErrorMessage = 'Siddhi app design contains errors';
            _.forEach(JSON.sourceList, function (source) {
                isValid = self.validateSourceOrSinkAnnotation(source, 'Source', true);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            // exit from the validate method
            if (!isValid) {
                DesignViewUtils.prototype.errorAlert(commonErrorMessage);
                return isValid;
            }

            _.forEach(JSON.sinkList, function (sink) {
                isValid = self.validateSourceOrSinkAnnotation(sink, 'Sink', true);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            // exit from the validate method
            if (!isValid) {
                DesignViewUtils.prototype.errorAlert(commonErrorMessage);
                return isValid;
            }

            _.forEach(JSON.aggregationList, function (aggregation) {
                isValid = self.validateAggregation(aggregation, true);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            // exit from the validate method
            if (!isValid) {
                DesignViewUtils.prototype.errorAlert(commonErrorMessage);
                return isValid;
            }

            _.forEach(JSON.queryLists.WINDOW_FILTER_PROJECTION, function (query) {
                isValid = self.validateWindowFilterProjectionQuery(query, true);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            // exit from the validate method
            if (!isValid) {
                DesignViewUtils.prototype.errorAlert(commonErrorMessage);
                return isValid;
            }

            _.forEach(JSON.queryLists.JOIN, function (query) {
                isValid = self.validateJoinQuery(query, true);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            // exit from the validate method
            if (!isValid) {
                DesignViewUtils.prototype.errorAlert(commonErrorMessage);
                return isValid;
            }

            _.forEach(JSON.queryLists.PATTERN, function (query) {
                isValid = self.validatePatternOrSequenceQuery(query, 'Pattern Query', true);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            // exit from the validate method
            if (!isValid) {
                DesignViewUtils.prototype.errorAlert(commonErrorMessage);
                return isValid;
            }

            _.forEach(JSON.queryLists.SEQUENCE, function (query) {
                isValid = self.validatePatternOrSequenceQuery(query, 'Sequence Query', true);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            // exit from the validate method
            if (!isValid) {
                DesignViewUtils.prototype.errorAlert(commonErrorMessage);
                return isValid;
            }

            _.forEach(JSON.partitionList, function (partition) {
                isValid = self.validatePartition(partition, jsPlumbInstance, true);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            if (!isValid) {
                DesignViewUtils.prototype.errorAlert(commonErrorMessage);
            }

            return isValid;
        };

        /**
         * @function Validates a given stream by checking if it is an inner stream inside a partition, then checks
         * whether it has a connection in
         * @param innerStream inner stream json
         * @param jsPlumbInstance jsPlumb instance for the current tab
         * @param doNotShowErrorMessages If true error messages will not be shown as alerts. Only the validity will be
         * returned
         * @returns {boolean} validity of the json
         */
        JSONValidator.prototype.validateInnerStream = function (innerStream, jsPlumbInstance, doNotShowErrorMessages) {
            var errorMessage;
            removeTooltipErrorMessage(innerStream.id);
            // check whether it has a connection in because there cannot be a inner stream without a 'connection-in'
            var inConnections = jsPlumbInstance.getConnections({ target: innerStream.id + '-in' });
            if (inConnections.length === 0) {
                errorMessage = 'Inner stream does not contain a connection input from an inner query';
                highlightErrorElement(innerStream.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;
            } else {
                removeErrorHighlighter(innerStream.id);
                return true;
            }
        };

        /**
         * @function Validates a given source or a sink annotation json
         * @param annotation annotation json
         * @param type source or sink
         * @param doNotShowErrorMessages If true error messages will not be shown as alerts. Only the validity will be
         * returned
         * @returns {boolean} validity of the json
         */
        JSONValidator.prototype.validateSourceOrSinkAnnotation = function (annotation, type, doNotShowErrorMessages) {
            var errorMessage;
            removeTooltipErrorMessage(annotation.id);
            if (!annotation.connectedElementName) {
                errorMessage = type + ' annotation does not contain a connected stream';
                if (annotation.type) {
                    highlightErrorElement(annotation.id, errorMessage);
                } else {
                    highlightIncompleteElement(annotation.id, errorMessage)
                }
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;
            } else {
                if (!annotation.type) {
                    errorMessage = type + ' annotation form is incomplete'
                    highlightIncompleteElement(annotation.id, errorMessage);
                }
                removeErrorHighlighter(annotation.id);
                return true;
            }
        };

        /**
         * @function Validates a given element
         * @param element element json
         * @param type stream or table or window or trigger or function
         * @param doNotShowErrorMessages If true error messages will not be shown as alerts. Only the validity will be
         * returned
         * @returns {boolean} validity of the json
         */
        JSONValidator.prototype.validateForElementName = function (element, type, doNotShowErrorMessages) {
            var errorMessage;
            if (!element.name) {
                errorMessage = type + ' form is incomplete'
                highlightIncompleteElement(element.id, errorMessage);
                return false;
            } else {
                removeTooltipErrorMessage(element.id);
                return true;
            }
        };

        /**
         * @function Validates a given aggregation annotation json
         * @param aggregation aggregation json
         * @param doNotShowErrorMessages If true error messages will not be shown as alerts. Only the validity will be
         * returned
         * @returns {boolean} validity of the json
         */
        JSONValidator.prototype.validateAggregation = function (aggregation, doNotShowErrorMessages) {
            var self = this;
            var errorMessage;
            var isValid = true;
            removeTooltipErrorMessage(aggregation.id);
            if (!aggregation.from) {
                errorMessage = 'Aggregation element does not contain a connected input';
                isValid = false;
            } else if (!aggregation.name) {
                errorMessage = 'Aggregation form is incomplete';
                isValid = false;
            } else if (!self.validateQuerySelectSection(aggregation.select)) {
                isValid = false;
                errorMessage = 'Aggregation form is incomplete';
            }
            if (!isValid && !doNotShowErrorMessages) {
                DesignViewUtils.prototype.errorAlert(errorMessage);
            }
            if (isValid) {
                removeErrorHighlighter(aggregation.id);
                removeIncompleteElement(aggregation.id);
                removeTooltipErrorMessage(aggregation.id);
            } else {
                if(aggregation.name) {
                    highlightErrorElement(aggregation.id, errorMessage);
                } else {
                    highlightIncompleteElement(aggregation.id, errorMessage);
                }
            }

            return isValid;
        };

        /**
         * @function Validates a given window/filter/projection/function query json
         * @param query query json
         * @param doNotShowErrorMessages If true error messages will not be shown as alerts. Only the validity will be
         * returned
         * @returns {boolean} validity of the json
         */
        JSONValidator.prototype.validateWindowFilterProjectionQuery = function (query, doNotShowErrorMessages) {
            var self = this;
            var isValid = true;
            var errorMessage;
            if ((!query.queryInput) || (query.queryInput && !query.queryInput.from)) {
                errorMessage = 'Query does not contain a connected input element';
                isValid = false;
            } else if (!self.validateQueryOutputSection(query)) {
                errorMessage = "Query does not contain a connected output element";
                isValid = false;
            } else if (!self.validateQuerySelectSection(query.select)) {
                isValid = false;
                errorMessage = 'Query form is incomplete';
            }
            if (!isValid && !doNotShowErrorMessages) {
                DesignViewUtils.prototype.errorAlert(errorMessage);
            }
            if (isValid) {
                removeIncompleteElement(query.id);
                removeErrorHighlighter(query.id);
                removeTooltipErrorMessage(query.id);
            } else {
                if (self.isQueryFreshlyDropped(query.queryOutput)) {
                    highlightIncompleteElement(query.id, errorMessage);
                } else {
                    highlightErrorElement(query.id, errorMessage);
                }
            }

            return isValid;
        };

        /**
         * @function Validates a given join query json
         * @param query query json
         * @param doNotShowErrorMessages If true error messages will not be shown as alerts. Only the validity will be
         * returned
         * @returns {boolean} validity of the json
         */
        JSONValidator.prototype.validateJoinQuery = function (query, doNotShowErrorMessages) {
            var self = this;
            var isValid = true;
            var errorMessage;
            if (!query.queryInput || (query.queryInput && !query.queryInput.firstConnectedElement &&
                    !query.queryInput.secondConnectedElement)) {
                errorMessage = 'Join query does not contain two connected inputs';
                isValid = false
            } else if (!query.queryInput.firstConnectedElement || !query.queryInput.secondConnectedElement) {
                errorMessage = 'Only one element is connected to Join query';
                isValid = false;
            } else if (!self.validateQueryOutputSection(query)) {
                errorMessage = "Join query does not contain a connected output element";
                isValid = false;
            } else if (!self.validateQuerySelectSection(query.select)) {
                isValid = false;
                errorMessage = 'Join query form is incomplete';
            }
            if (!isValid && !doNotShowErrorMessages) {
                DesignViewUtils.prototype.errorAlert(errorMessage);
            }
            if (isValid) {
                removeErrorHighlighter(query.id);
                removeIncompleteElement(query.id);
                removeTooltipErrorMessage(query.id);
            } else {
                if (self.isQueryFreshlyDropped(query.queryOutput)) {
                    highlightIncompleteElement(query.id, errorMessage);
                } else {
                    highlightErrorElement(query.id, errorMessage);
                }
            }

            return isValid;
        };

        /**
         * @function Validates a given pattern or sequence query json
         * @param query query json
         * @param type pattern query or sequence query
         * @param doNotShowErrorMessages If true error messages will not be shown as alerts. Only the validity will be
         * returned
         * @returns {boolean} validity of the json
         */
        JSONValidator.prototype.validatePatternOrSequenceQuery = function (query, type, doNotShowErrorMessages) {
            var self = this;
            var isValid = true;
            var errorMessage;
            if (!query.queryInput || (query.queryInput && query.queryInput.connectedElementNameList &&
                    query.queryInput.connectedElementNameList.length === 0)) {
                errorMessage = type + ' does not contain a connected input element';
                isValid = false;
            } else if (!self.validateQueryOutputSection(query)) {
                errorMessage = type + ' query does not contain a connected output element';
                isValid = false;
            } else if (!self.validateQuerySelectSection(query.select)) {
                errorMessage = type + ' form is incomplete';
                isValid = false;
            }
            if (!isValid && !doNotShowErrorMessages) {
                DesignViewUtils.prototype.errorAlert(errorMessage);
            }
            if (isValid) {
                removeIncompleteElement(query.id);
                removeErrorHighlighter(query.id);
                removeTooltipErrorMessage(query.id);
            } else {
                if (self.isQueryFreshlyDropped(query.queryOutput)) {
                    highlightIncompleteElement(query.id, errorMessage);
                } else {
                    highlightErrorElement(query.id, errorMessage);
                }
            }

            return isValid;
        };

        /**
         * @function Validates a select section of a given query json
         * @param select select section json
         * @returns {boolean} validity of the json
         */
        JSONValidator.prototype.validateQuerySelectSection = function (select) {
            var isValid = true;
            if (!select) {
                isValid = false;
            } else if (select.type.toLowerCase() === Constants.TYPE_USER_DEFINED) {
                _.forEach(select.value, function (value) {
                    if (!value.expression || value.expression === '') {
                        isValid = false;
                        return false;
                    }
                });
            }
            return isValid;
        };

        /**
         * @function Validates a output section of a given query json
         * @param query object of the query
         * @param type query type
         * @param doNotShowErrorMessages If true error messages will not be shown as alerts. Only the validity will be
         * returned
         * @returns {boolean} validity of the json
         */
        JSONValidator.prototype.validateQueryOutputSection = function (query) {
            var isValid = true;
            if ((!query.queryOutput) || (query.queryOutput && !query.queryOutput.target)) {
                isValid = false;
            }
            return isValid;
        };

        /**
         * @function to identify if it is a freshly dropped query
         * @param queryOutput output section of the query
         * @returns {boolean}
         */
        JSONValidator.prototype.isQueryFreshlyDropped = function (queryOutput) {
            var isFresh = true;
            if (queryOutput && queryOutput.type) {
                isFresh = false;
            }
            return isFresh;
        };

        /**
         * @function Validates a given partition json
         * @param partition partition json
         * @param jsPlumbInstance jsPlumb instance for the current tab
         * @param doNotShowErrorMessages If true error messages will not be shown as alerts. Only the validity will be
         * returned
         * @returns {boolean} validity of the json
         */
        JSONValidator.prototype.validatePartition = function (partition, jsPlumbInstance, doNotShowErrorMessages) {
            var self = this;
            var isValid = true;
            var errorMessage;
            removeTooltipErrorMessage(partition.id);
            if (partition.partitionWith.length === 0) {
                errorMessage = 'Partition does not contain a connected outer stream';
                highlightIncompleteElement(partition.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;
            } else {
                _.forEach(partition.partitionWith, function (partitionWithAttribute) {
                    if (!partitionWithAttribute.expression || partitionWithAttribute.expression === ''
                        || !partitionWithAttribute.streamName) {
                        errorMessage = 'Partition by section of partition form is not filled';
                        highlightIncompleteElement(partition.id, errorMessage);
                        if (!doNotShowErrorMessages) {
                            DesignViewUtils.prototype.errorAlert(errorMessage);
                        }
                        isValid = false;
                        // break the for each loop
                        return false;
                    }
                });
            }

            if (!isValid) {
                return false;
            } else {
                // At this moment fields related to partition form is valid. So if the partition is highlighted in
                // red(error element) then we remove it
                removeErrorHighlighter(partition.id);
            }

            _.forEach(partition.streamList, function (stream) {
                isValid = self.validateInnerStream(stream, jsPlumbInstance, doNotShowErrorMessages);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            if (!isValid) {
                return false;
            }

            _.forEach(partition.queryLists.WINDOW_FILTER_PROJECTION, function (query) {
                isValid = self.validateWindowFilterProjectionQuery(query, doNotShowErrorMessages);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            if (!isValid) {
                return false;
            }

            _.forEach(partition.queryLists.JOIN, function (query) {
                isValid = self.validateJoinQuery(query, doNotShowErrorMessages);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            if (!isValid) {
                return false;
            }

            _.forEach(partition.queryLists.PATTERN, function (query) {
                isValid = self.validatePatternOrSequenceQuery(query, 'Pattern Query', doNotShowErrorMessages);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            if (!isValid) {
                return false;
            }

            _.forEach(partition.queryLists.SEQUENCE, function (query) {
                isValid = self.validatePatternOrSequenceQuery(query, 'Sequence Query', doNotShowErrorMessages);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            return isValid;
        };

        function highlightErrorElement(errorElementId, errorMessage) {
            var element = $('#' + errorElementId);
            element.addClass('error-element');
            // set error message as the tooltip message
            element.prop('title', errorMessage);
        }

        function highlightIncompleteElement(errorElementId, errorMessage) {
            removeErrorHighlighter(errorElementId);
            var element = $('#' + errorElementId);
            element.addClass('incomplete-element');
            // set error message as the tooltip message
            element.prop('title', errorMessage);
        }

        function removeIncompleteElement(incompleteElementId) {
            var element = $('#' + incompleteElementId);
            if (element.hasClass('incomplete-element')) {
                element.removeClass('incomplete-element');
            }
        }

        function removeErrorHighlighter(errorElementId) {
            var element = $('#' + errorElementId);
            if (element.hasClass('error-element')) {
                element.removeClass('error-element');
            }
        }

        function addToolTipErrorMessage(errorElementId, errorMessage) {
            var element = $('#' + errorElementId);
            element.prop('title', errorMessage);
        }

        function removeTooltipErrorMessage(errorElementId) {
            var element = $('#' + errorElementId);
            element.prop('title', '');
        }

        return JSONValidator;
    });
