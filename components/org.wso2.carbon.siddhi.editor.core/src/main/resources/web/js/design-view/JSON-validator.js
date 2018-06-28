/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

define(['require', 'log', 'jquery', 'lodash', 'designViewUtils'],
    function (require, log, $, _, DesignViewUtils) {

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
            if(!isValid) {
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
            if(!isValid) {
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
            if(!isValid) {
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
            if(!isValid) {
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
            if(!isValid) {
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
            if(!isValid) {
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
            if(!isValid) {
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

            if(!isValid) {
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
            var inConnections = jsPlumbInstance.getConnections({target: innerStream.id + '-in'});
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
                highlightErrorElement(annotation.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;
            } else {
                removeErrorHighlighter(annotation.id);
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
            var isValid;
            removeTooltipErrorMessage(aggregation.id);
            if (!aggregation.from) {
                errorMessage = 'Aggregation element does not contain a connected input';
                highlightErrorElement(aggregation.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;

            } else if (!aggregation.name) {
                errorMessage = 'Name field of Aggregation element form cannot be blank';
                highlightErrorElement(aggregation.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;
            }

            isValid = self.validateQuerySelectSection(aggregation.select, 'Aggregation', aggregation.id,
                doNotShowErrorMessages);
            if (isValid) {
                removeErrorHighlighter(aggregation.id);
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
            var isValid;
            var errorMessage;
            removeTooltipErrorMessage(query.id);
            if (!query.queryInput) {
                errorMessage = 'Query does not contain a connected input';
                highlightErrorElement(query.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;

            } else if (!query.queryInput.from) {
                errorMessage = 'Query does not contain a connected input';
                highlightErrorElement(query.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;
            }

            isValid = self.validateQueryOutputSection(query.queryOutput, 'Query', query.id, doNotShowErrorMessages);
            if (!isValid) {
                return isValid;
            }

            isValid = self.validateQuerySelectSection(query.select, 'Query', query.id, doNotShowErrorMessages);

            if (isValid) {
                removeErrorHighlighter(query.id);
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
            var isValid;
            var errorMessage;
            removeTooltipErrorMessage(query.id);
            if (!query.queryInput) {
                errorMessage = 'Join query does not contain two connected inputs';
                highlightErrorElement(query.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;
            } else if (!query.queryInput.firstConnectedElement && !query.queryInput.secondConnectedElement) {
                errorMessage = 'Join query does not contain two connected inputs';
                highlightErrorElement(query.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;
            } else if (!query.queryInput.firstConnectedElement || !query.queryInput.secondConnectedElement) {
                errorMessage = 'Only one element is connected to Join query';
                highlightErrorElement(query.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;
            }

            isValid = self.validateQueryOutputSection(query.queryOutput, 'Join query', query.id,
                doNotShowErrorMessages);
            if (!isValid) {
                return isValid;
            }

            if (!query.queryInput.left) {
                errorMessage = 'Left source of Join query is not defined';
                highlightErrorElement(query.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;
            } else if (!query.queryInput.right) {
                errorMessage = 'Right source of Join query is not defined';
                highlightErrorElement(query.i, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;
            } else if (!query.queryInput.joinWith || !query.queryInput.joinType) {
                errorMessage = 'Join query form is not filled';
                highlightErrorElement(query.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;
            }

            isValid = self.validateQuerySelectSection(query.select, 'Join query', query.id, doNotShowErrorMessages);
            if (isValid) {
                removeErrorHighlighter(query.id);
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
            var isValid;
            var errorMessage;
            removeTooltipErrorMessage(query.id);
            if (!query.queryInput) {
                errorMessage = type + ' does not contain a input';
                highlightErrorElement(query.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;

            } else if (query.queryInput.connectedElementNameList !== undefined
                && query.queryInput.connectedElementNameList.length === 0) {
                errorMessage = type + ' does not contain a input';
                highlightErrorElement(query.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;

            }

            isValid = self.validateQueryOutputSection(query.queryOutput, type, query.id, doNotShowErrorMessages);
            if (!isValid) {
                return isValid;
            }

            if (!query.queryInput.logic) {
                errorMessage = 'Logic section in query input of ' + type + ' form cannot be blank';
                highlightErrorElement(query.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;
            } else if (query.queryInput.conditionList.length === 0){
                errorMessage = 'Condition list in query input of ' + type + ' form cannot be blank';
                highlightErrorElement(query.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;
            }

            isValid = self.validateQuerySelectSection(query.select, type, query.id, doNotShowErrorMessages);
            if (isValid) {
                removeErrorHighlighter(query.id);
            }

            return isValid;
        };

        /**
         * @function Validates a select section of a given query json
         * @param select select section json
         * @param type query type
         * @param elementId id of the query
         * @param doNotShowErrorMessages If true error messages will not be shown as alerts. Only the validity will be
         * returned
         * @returns {boolean} validity of the json
         */
        JSONValidator.prototype.validateQuerySelectSection = function (select, type, elementId,
                                                                       doNotShowErrorMessages) {
            var isValid = true;
            var errorMessage;
            if (!select) {
                isValid = false;
                errorMessage = 'Select section of ' + type + ' form cannot be blank';
                highlightErrorElement(elementId, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
            } else if(select.type === 'USER_DEFINED') {
                _.forEach(select.value, function (value) {
                    if (!value.expression || value.expression === '') {
                        isValid = false;
                        errorMessage = 'Select section of ' + type + ' form cannot be blank';
                        highlightErrorElement(elementId, errorMessage);
                        if (!doNotShowErrorMessages) {
                            DesignViewUtils.prototype.errorAlert(errorMessage);
                        }
                        // break the for each loop
                        return false;
                    }
                });
            }
            return isValid;
        };

        /**
         * @function Validates a output section of a given query json
         * @param output output section json
         * @param type query type
         * @param elementId id of the query
         * @param doNotShowErrorMessages If true error messages will not be shown as alerts. Only the validity will be
         * returned
         * @returns {boolean} validity of the json
         */
        JSONValidator.prototype.validateQueryOutputSection = function (output, type, elementId,
                                                                       doNotShowErrorMessages) {
            var isValid = true;
            var errorMessage;
            if (!output) {
                isValid = false;
                errorMessage = type + ' does not contain a connected output element';
                highlightErrorElement(elementId, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
            } else if (!output.target) {
                isValid = false;
                errorMessage = type + ' does not contain a connected output element';
                highlightErrorElement(elementId, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
            } else if(!output.type || !output.output) {
                isValid = false;
                errorMessage = 'Output section of ' + type + ' form is not filled';
                highlightErrorElement(elementId, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
            }
            return isValid;
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
                highlightErrorElement(partition.id, errorMessage);
                if (!doNotShowErrorMessages) {
                    DesignViewUtils.prototype.errorAlert(errorMessage);
                }
                return false;
            } else {
                _.forEach(partition.partitionWith, function (partitionWithAttribute) {
                    if (!partitionWithAttribute.expression || partitionWithAttribute.expression === ''
                        || !partitionWithAttribute.streamName) {
                        errorMessage = 'Partition by section of partition form is not filled';
                        highlightErrorElement(partition.id, errorMessage);
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

        function removeErrorHighlighter(errorElementId) {
            var element = $('#' + errorElementId);
            if (element.hasClass('error-element')) {
                element.removeClass('error-element');
            }
        }

        function removeTooltipErrorMessage(errorElementId) {
            var element = $('#' + errorElementId);
            element.prop('title', '');
        }

        return JSONValidator;
    });
