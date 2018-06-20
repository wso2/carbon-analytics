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

define(['require', 'log', 'lodash', 'designViewUtils'],
    function (require, log, _, DesignViewUtils) {

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
            var isValid = true;

            _.forEach(JSON.sourceList, function (source) {
                isValid = validateSourceOrSinkAnnotation(source, 'Source');
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
                isValid = validateSourceOrSinkAnnotation(sink, 'Sink');
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
                isValid = validateAggregation(aggregation);
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
                isValid = validateWindowFilterProjectionQuery(query);
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
                isValid = validateJoinQuery(query);
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
                isValid = validatePatternOrSequenceQuery(query, 'Pattern Query');
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
                isValid = validatePatternOrSequenceQuery(query, 'Sequence Query');
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
                _.forEach(partition.queryLists.WINDOW_FILTER_PROJECTION, function (query) {
                    isValid = validateWindowFilterProjectionQuery(query);
                    if (!isValid) {
                        // break the for each loop
                        return false;
                    }
                });

                if (!isValid) {
                    // break the partition for each loop
                    return false;
                }

                _.forEach(partition.queryLists.JOIN, function (query) {
                    isValid = validateJoinQuery(query);
                    if (!isValid) {
                        // break the for each loop
                        return false;
                    }
                });

                if (!isValid) {
                    // break the partition for each loop
                    return false;
                }

                _.forEach(partition.queryLists.PATTERN, function (query) {
                    isValid = validatePatternOrSequenceQuery(query, 'Pattern Query');
                    if (!isValid) {
                        // break the for each loop
                        return false;
                    }
                });

                if (!isValid) {
                    // break the partition for each loop
                    return false;
                }

                _.forEach(partition.queryLists.SEQUENCE, function (query) {
                    isValid = validatePatternOrSequenceQuery(query, 'Sequence Query');
                    if (!isValid) {
                        // break the for each loop
                        return false;
                    }
                });

                if (!isValid) {
                    // break the partition for each loop
                    return false;
                }
            });

            return isValid;
        };

        function validateSourceOrSinkAnnotation(annotation, type) {
            if (!annotation.connectedElementName) {
                DesignViewUtils.prototype.errorAlert('A ' + type + ' annotation does not contain a connected stream');
                return false;
            } else {
                return true;
            }
        }

        function validateAggregation(aggregation) {
            var isValid;
            if (!aggregation.from) {
                DesignViewUtils.prototype.errorAlert('An Aggregation element does not contain a connected input');
                return false;

            } else if (!aggregation.name) {
                DesignViewUtils.prototype.errorAlert('Name field of an Aggregation form cannot be blank');
                return false;
            }

            isValid = validateQuerySelectSection(aggregation.select, 'Aggregation');
            if (!isValid) {
                return isValid;
            }

            return isValid;
        }

        function validateWindowFilterProjectionQuery(query) {
            var isValid;
            if (!query.queryInput) {
                DesignViewUtils.prototype.errorAlert('A Function/Window/Filter/Projection query does not contain a ' +
                    'connected input');
                return false;

            } else if (!query.queryInput.from) {
                DesignViewUtils.prototype.errorAlert('A Function/Window/Filter/Projection query does not contain a ' +
                    'connected input');
                return false;
            }

            isValid = validateQueryOutputSection(query.queryOutput, 'Function/Window/Filter/Projection query');
            if (!isValid) {
                return isValid;
            }

            isValid = validateQuerySelectSection(query.select, 'Function/Window/Filter/Projection query');
            if (!isValid) {
                return isValid;
            }

            return isValid;
        }

        function validateJoinQuery(query) {
            var isValid;
            if (!query.queryInput) {
                DesignViewUtils.prototype.errorAlert('A Join query does not contain two connected inputs');
                return false;
            } else if (!query.queryInput.firstConnectedElement && !query.queryInput.secondConnectedElement) {
                DesignViewUtils.prototype.errorAlert('A Join query does not contain two connected inputs');
                return false;
            } else if (!query.queryInput.firstConnectedElement || !query.queryInput.secondConnectedElement) {
                DesignViewUtils.prototype.errorAlert('Only one element is connected to a Join query');
                return false;
            }

            isValid = validateQueryOutputSection(query.queryOutput, 'Join query');
            if (!isValid) {
                return isValid;
            }

            if (!query.queryInput.left) {
                DesignViewUtils.prototype.errorAlert('Left source of a Join query is not defined');
                return false;
            } else if (!query.queryInput.right) {
                DesignViewUtils.prototype.errorAlert('Right source of a Join query is not defined');
                return false;
            } else if (!query.queryInput.joinWith || !query.queryInput.joinType) {
                DesignViewUtils.prototype.errorAlert('A Join query form is not filled');
                return false;
            }

            isValid = validateQuerySelectSection(query.select, 'Join query');
            if (!isValid) {
                return isValid;
            }

            return isValid;
        }

        function validatePatternOrSequenceQuery(query, type) {
            var isValid;
            if (!query.queryInput) {
                DesignViewUtils.prototype.errorAlert('A ' + type + ' does not contain a input');
                return false;

            } else if (query.queryInput.connectedElementNameList !== undefined
                && query.queryInput.connectedElementNameList.length === 0) {
                DesignViewUtils.prototype.errorAlert('A ' + type + ' does not contain a input');
                return false;

            }

            isValid = validateQueryOutputSection(query.queryOutput, type);
            if (!isValid) {
                return isValid;
            }

            if (!query.queryInput.logic) {
                DesignViewUtils.prototype
                    .errorAlert('Logic section in query input of a ' + type + ' form cannot be blank');
                return false;
            } else if (query.queryInput.conditionList.length === 0){
                DesignViewUtils.prototype
                    .errorAlert('Condition list in query input of a ' + type + ' form cannot be blank');
                return false;
            }

            isValid = validateQuerySelectSection(query.select, type);
            if (!isValid) {
                return isValid;
            }

            return isValid;
        }

        function validateQuerySelectSection(select, type) {
            var isValid = true;
            if (!select) {
                isValid = false;
                DesignViewUtils.prototype.errorAlert('Select section of a ' + type + ' form cannot be blank');
            } else if(select.type === 'USER_DEFINED') {
                _.forEach(select.value, function (value) {
                    if (!value.expression || value.expression === '') {
                        isValid = false;
                        // break the for each loop
                        return false;
                    }
                });
            }
            return isValid;
        }

        function validateQueryOutputSection(output, type) {
            var isValid = true;
            if (!output) {
                isValid = false;
                DesignViewUtils.prototype.errorAlert('A ' + type + ' does not contain a connected output element');
            } else if (!output.target) {
                isValid = false;
                DesignViewUtils.prototype.errorAlert('A ' + type + ' does not contain a connected output element');
            } else if(!output.type || !output.output) {
                isValid = false;
                DesignViewUtils.prototype.errorAlert('Output section of a ' + type + ' form is not filled');
            }
            return isValid;
        }

        return JSONValidator;
    });
