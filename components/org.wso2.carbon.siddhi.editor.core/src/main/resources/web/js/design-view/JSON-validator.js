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
         */
        JSONValidator.prototype.validate = function (JSON) {
            var self = this;
            var isValid = true;

            _.forEach(JSON.sourceList, function (source) {
                isValid = self.validateSourceOrSinkAnnotation(source, 'Source');
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            // exit from the validate method
            if(!isValid) {
                return isValid;
            }

            _.forEach(JSON.sinkList, function (sink) {
                isValid = self.validateSourceOrSinkAnnotation(sink, 'Sink');
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            // exit from the validate method
            if(!isValid) {
                return isValid;
            }

            _.forEach(JSON.aggregationList, function (aggregation) {
                isValid = self.validateAggregation(aggregation);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            // exit from the validate method
            if(!isValid) {
                return isValid;
            }

            _.forEach(JSON.queryLists.WINDOW_FILTER_PROJECTION, function (query) {
                isValid = self.validateWindowFilterProjectionQuery(query);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            // exit from the validate method
            if(!isValid) {
                return isValid;
            }

            _.forEach(JSON.queryLists.JOIN, function (query) {
                isValid = self.validateJoinQuery(query);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            // exit from the validate method
            if(!isValid) {
                return isValid;
            }

            _.forEach(JSON.queryLists.PATTERN, function (query) {
                isValid = self.validatePatternOrSequenceQuery(query, 'Pattern Query');
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            // exit from the validate method
            if(!isValid) {
                return isValid;
            }

            _.forEach(JSON.queryLists.SEQUENCE, function (query) {
                isValid = self.validatePatternOrSequenceQuery(query, 'Sequence Query');
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            // exit from the validate method
            if(!isValid) {
                return isValid;
            }

            _.forEach(JSON.partitionList, function (partition) {
                isValid = self.validatePartition(partition);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            return isValid;
        };

        JSONValidator.prototype.validateSourceOrSinkAnnotation = function (annotation, type) {
            var errorMessage;
            if (!annotation.connectedElementName) {
                errorMessage = 'A ' + type + ' annotation does not contain a connected stream';
                highlightErrorElement(annotation.id, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
                return false;
            } else {
                return true;
            }
        };

        JSONValidator.prototype.validateAggregation = function (aggregation) {
            var self = this;
            var errorMessage;
            var isValid;
            if (!aggregation.from) {
                errorMessage = 'An Aggregation element does not contain a connected input';
                highlightErrorElement(aggregation.id, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
                return false;

            } else if (!aggregation.name) {
                errorMessage = 'Name field of an Aggregation form cannot be blank';
                highlightErrorElement(aggregation.id, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
                return false;
            }

            isValid = self.validateQuerySelectSection(aggregation.select, 'Aggregation', aggregation.id);
            if (!isValid) {
                return isValid;
            }

            return isValid;
        };

        JSONValidator.prototype.validateWindowFilterProjectionQuery = function (query) {
            var self = this;
            var isValid;
            var errorMessage;
            if (!query.queryInput) {
                errorMessage = 'A Function/Window/Filter/Projection query does not contain a connected input';
                highlightErrorElement(query.id, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
                return false;

            } else if (!query.queryInput.from) {
                errorMessage = 'A Function/Window/Filter/Projection query does not contain a connected input';
                highlightErrorElement(query.id, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
                return false;
            }

            isValid = self.validateQueryOutputSection(query.queryOutput, 'Function/Window/Filter/Projection query',
                query.id);
            if (!isValid) {
                return isValid;
            }

            isValid = self.validateQuerySelectSection(query.select, 'Function/Window/Filter/Projection query',
                query.id);
            if (!isValid) {
                return isValid;
            }

            return isValid;
        };

        JSONValidator.prototype.validateJoinQuery = function (query) {
            var self = this;
            var isValid;
            var errorMessage;
            if (!query.queryInput) {
                errorMessage = 'A Join query does not contain two connected inputs';
                highlightErrorElement(query.id, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
                return false;
            } else if (!query.queryInput.firstConnectedElement && !query.queryInput.secondConnectedElement) {
                errorMessage = 'A Join query does not contain two connected inputs';
                highlightErrorElement(query.id, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
                return false;
            } else if (!query.queryInput.firstConnectedElement || !query.queryInput.secondConnectedElement) {
                errorMessage = 'Only one element is connected to a Join query';
                highlightErrorElement(query.id, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
                return false;
            }

            isValid = self.validateQueryOutputSection(query.queryOutput, 'Join query', query.id);
            if (!isValid) {
                return isValid;
            }

            if (!query.queryInput.left) {
                errorMessage = 'Left source of a Join query is not defined';
                highlightErrorElement(query.id, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
                return false;
            } else if (!query.queryInput.right) {
                errorMessage = 'Right source of a Join query is not defined';
                highlightErrorElement(query.i, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
                return false;
            } else if (!query.queryInput.joinWith || !query.queryInput.joinType) {
                errorMessage = 'A Join query form is not filled';
                highlightErrorElement(query.id, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
                return false;
            }

            isValid = self.validateQuerySelectSection(query.select, 'Join query', query.id);
            if (!isValid) {
                return isValid;
            }

            return isValid;
        };

        JSONValidator.prototype.validatePatternOrSequenceQuery = function (query, type) {
            var self = this;
            var isValid;
            var errorMessage;
            if (!query.queryInput) {
                errorMessage = 'A ' + type + ' does not contain a input';
                highlightErrorElement(query.id, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
                return false;

            } else if (query.queryInput.connectedElementNameList !== undefined
                && query.queryInput.connectedElementNameList.length === 0) {
                errorMessage = 'A ' + type + ' does not contain a input';
                highlightErrorElement(query.id, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
                return false;

            }

            isValid = self.validateQueryOutputSection(query.queryOutput, type, query.id);
            if (!isValid) {
                return isValid;
            }

            if (!query.queryInput.logic) {
                errorMessage = 'Logic section in query input of a ' + type + ' form cannot be blank';
                highlightErrorElement(query.id, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
                return false;
            } else if (query.queryInput.conditionList.length === 0){
                errorMessage = 'Condition list in query input of a ' + type + ' form cannot be blank';
                highlightErrorElement(query.id, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
                return false;
            }

            isValid = self.validateQuerySelectSection(query.select, type, query.id);
            if (!isValid) {
                return isValid;
            }

            return isValid;
        };

        JSONValidator.prototype.validateQuerySelectSection = function (select, type, elementId) {
            var isValid = true;
            var errorMessage;
            if (!select) {
                isValid = false;
                errorMessage = 'Select section of a ' + type + ' form cannot be blank';
                highlightErrorElement(elementId, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
            } else if(select.type === 'USER_DEFINED') {
                _.forEach(select.value, function (value) {
                    if (!value.expression || value.expression === '') {
                        isValid = false;
                        errorMessage = 'Select section of a ' + type + ' form cannot be blank';
                        highlightErrorElement(elementId, errorMessage);
                        DesignViewUtils.prototype.errorAlert(errorMessage);
                        // break the for each loop
                        return false;
                    }
                });
            }
            return isValid;
        };

        JSONValidator.prototype.validateQueryOutputSection = function (output, type, elementId) {
            var isValid = true;
            var errorMessage;
            if (!output) {
                isValid = false;
                errorMessage = 'A ' + type + ' does not contain a connected output element';
                highlightErrorElement(elementId, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
            } else if (!output.target) {
                isValid = false;
                errorMessage = 'A ' + type + ' does not contain a connected output element';
                highlightErrorElement(elementId, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
            } else if(!output.type || !output.output) {
                isValid = false;
                errorMessage = 'Output section of a ' + type + ' form is not filled';
                highlightErrorElement(elementId, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
            }
            return isValid;
        };

        JSONValidator.prototype.validatePartition = function (partition) {
            var self = this;
            var isValid = true;
            var errorMessage;
            if (partition.partitionWith.length === 0) {
                errorMessage = 'A partition does not contain a connected outer stream';
                highlightErrorElement(partition.id, errorMessage);
                DesignViewUtils.prototype.errorAlert(errorMessage);
                return false;
            } else {
                _.forEach(partition.partitionWith, function (partitionWithAttribute) {
                    if (!partitionWithAttribute.expression || partitionWithAttribute.expression === ''
                        || !partitionWithAttribute.streamName) {
                        errorMessage = 'Partition by section of a partition form is not filled';
                        highlightErrorElement(partition.id, errorMessage);
                        DesignViewUtils.prototype.errorAlert(errorMessage);
                        isValid = false;
                        // break the for each loop
                        return false;
                    }
                });
            }

            if (!isValid) {
                return false;
            }

            _.forEach(partition.queryLists.WINDOW_FILTER_PROJECTION, function (query) {
                isValid = self.validateWindowFilterProjectionQuery(query);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            if (!isValid) {
                return false;
            }

            _.forEach(partition.queryLists.JOIN, function (query) {
                isValid = self.validateJoinQuery(query);
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            if (!isValid) {
                return false;
            }

            _.forEach(partition.queryLists.PATTERN, function (query) {
                isValid = self.validatePatternOrSequenceQuery(query, 'Pattern Query');
                if (!isValid) {
                    // break the for each loop
                    return false;
                }
            });

            if (!isValid) {
                return false;
            }

            _.forEach(partition.queryLists.SEQUENCE, function (query) {
                isValid = self.validatePatternOrSequenceQuery(query, 'Sequence Query');
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
            element.prop('title', errorMessage);
        }

        return JSONValidator;
    });
