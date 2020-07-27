/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

define(['require', 'jquery', 'lodash', 'log', 'dataMapperUtil', 'app/source-editor/completion-engine'],

    function (require, $, _, log, DataMapperUtil, CompletionEngine) {
        var isSourceSinkConfigValid = function (sourceConfig) {
            if (sourceConfig.type.length === 0) {
                return false;
            }

            var hasMatchingType = true;
            Object.keys(sourceConfig.properties)
                .forEach(function (key) {
                    sourceConfig.properties[key].type.forEach(function (type) {
                        if (hasMatchingType) {
                            switch (DataMapperUtil.getGenericDataType(type.toLowerCase())) {
                                case 'text':
                                    hasMatchingType = sourceConfig.properties[key].value.length > 0
                                        && typeof sourceConfig.properties[key].value === 'string';
                                    break;
                                case 'number':
                                    hasMatchingType = sourceConfig.properties[key].value.length > 0
                                        && /[-.0-9]+/.test(sourceConfig.properties[key].value);
                                    break;
                                case 'bool':
                                    hasMatchingType = sourceConfig.properties[key].value.length > 0
                                        && (sourceConfig.properties[key].value === 'true'
                                            || sourceConfig.properties[key].value === 'false');
                                    break;
                            }
                        }
                    });
                });

            return hasMatchingType;
        }

        var isStreamDefValid = function (streamConfig) {
            var noIncomplete = true;
            var noSimilarNames = true;

            if (!(streamConfig.name.length > 0)) {
                return false;
            }

            var nameArray = [];

            streamConfig.attributes.forEach(function (attr) {
                if (noIncomplete && noSimilarNames) {
                    noIncomplete = attr.name.length > 0;

                    noSimilarNames = nameArray.indexOf(attr.name) === -1;
                    nameArray.push(attr.name);
                }
            });

            if (nameArray.length === 0) {
                return false;
            }

            return noIncomplete && noSimilarNames;
        }

        var isInputMappingValid = function (inputConfig) {

            if (inputConfig.mapping.type.length === 0) {
                return false;
            }

            var hasMatchingType = true;
            Object.keys(inputConfig.mapping.properties)
                .forEach(function (key) {
                    inputConfig.mapping.properties[key].type.forEach(function (type) {
                        if (hasMatchingType) {
                            switch (DataMapperUtil.getGenericDataType(type.toLowerCase())) {
                                case 'text':
                                    hasMatchingType = inputConfig.mapping.properties[key].value.length > 0
                                        && typeof inputConfig.mapping.properties[key].value === 'string';
                                    break;
                                case 'number':
                                    hasMatchingType = inputConfig.mapping.properties[key].value.length > 0
                                        && /[-.0-9]+/.test(inputConfig.mapping.properties[key].value);
                                    break;
                                case 'bool':
                                    hasMatchingType = inputConfig.mapping.properties[key].value.length > 0
                                        && (inputConfig.mapping.properties[key].value === 'true'
                                            || sourceConfig.properties[key].value === 'false');
                                    break;
                            }
                        }
                    });
                });

            if (inputConfig.mapping.customEnabled) {
                return Object.keys(inputConfig.mapping.attributes).length > 0 &&
                    Object.keys(inputConfig.mapping.attributes).length === inputConfig.stream.attributes.length;
            }

            return hasMatchingType;
        }

        var isOutputMappingValid = function (outputConfig) {
            if (outputConfig.mapping.type.length === 0) {
                return false;
            }

            var hasMatchingType = true;
            Object.keys(outputConfig.mapping.properties)
                .forEach(function (key) {
                    outputConfig.mapping.properties[key].type.forEach(function (type) {
                        if (hasMatchingType) {
                            switch (DataMapperUtil.getGenericDataType(type.toLowerCase())) {
                                case 'text':
                                    hasMatchingType = outputConfig.mapping.properties[key].value.length > 0
                                        && typeof outputConfig.mapping.properties[key].value === 'string';
                                    break;
                                case 'number':
                                    hasMatchingType = outputConfig.mapping.properties[key].value.length > 0
                                        && /[-.0-9]+/.test(outputConfig.mapping.properties[key].value);
                                    break;
                                case 'bool':
                                    hasMatchingType = outputConfig.mapping.properties[key].value.length > 0
                                        && (outputConfig.mapping.properties[key].value === 'true'
                                            || sourceConfig.properties[key].value === 'false');
                                    break;
                            }
                        }
                    });
                });

            if (outputConfig.mapping.customEnabled) {
                if (outputConfig.mapping.type === 'csv') {
                    return outputConfig.mapping.attributes.length > 0;
                } else {
                    return outputConfig.mapping.payload.length > 0
                }
            }

            return hasMatchingType;
        }

        var areInputOptionsValid = function (queryConfig) {
            // Filter validation
            var filterValidity = true;
            var windowValidity = true;
            var functionValidity = true;

            if (queryConfig.filter.enable && typeof queryConfig.filter.expression !== 'string') {
                filterValidity = validateExpression(queryConfig.filter.expression);

                if (filterValidity) {
                    filterValidity = queryConfig.filter.expression.rootNode.genericReturnTypes.indexOf('bool') > -1;
                }
            }

            if (queryConfig.window.enable) {
                if (queryConfig.window.type.length === 0) {
                    windowValidity = false;
                }

                Object.keys(queryConfig.window.parameters)
                    .forEach(function (key) {
                        queryConfig.window.parameters[key].type.forEach(function (type) {
                            if (windowValidity) {
                                switch (DataMapperUtil.getGenericDataType(type.toLowerCase())) {
                                    case 'text':
                                        windowValidity = queryConfig.window.parameters[key].value.length > 0
                                            && typeof queryConfig.window.parameters[key].value === 'string';
                                        break;
                                    case 'number':
                                        windowValidity = queryConfig.window.parameters[key].value.length > 0
                                            && /[-.0-9]+/.test(queryConfig.window.parameters[key].value);
                                        break;
                                    case 'bool':
                                        windowValidity = queryConfig.window.parameters[key].value.length > 0
                                            && (queryConfig.window.parameters[key].value === 'true'
                                                || sourceConfig.properties[key].value === 'false');
                                        break;
                                }
                            }
                        });
                    });
            }

            if (queryConfig.function.enable) {
                if (queryConfig.function.name.length === 0) {
                    functionValidity = false;
                }

                Object.keys(queryConfig.function.parameters)
                    .forEach(function (key) {
                        queryConfig.function.parameters[key].type.forEach(function (type) {
                            if (functionValidity) {
                                switch (DataMapperUtil.getGenericDataType(type.toLowerCase())) {
                                    case 'text':
                                        functionValidity = queryConfig.function.parameters[key].value.length > 0
                                            && typeof queryConfig.function.parameters[key].value === 'string';
                                        break;
                                    case 'number':
                                        functionValidity = queryConfig.function.parameters[key].value.length > 0
                                            && /[-.0-9]+/.test(queryConfig.function.parameters[key].value);
                                        break;
                                    case 'bool':
                                        functionValidity = queryConfig.function.parameters[key].value.length > 0
                                            && (queryConfig.function.parameters[key].value === 'true'
                                                || sourceConfig.properties[key].value === 'false');
                                        break;
                                }
                            }
                        });
                    });
            }

            return filterValidity && windowValidity && functionValidity;
        }

        var validateExpression = function (expression) {
            var errorsFound = 0;
            validateLevel(expression);

            function validateLevel(expression) {
                switch (expression.type) {
                    case 'scope':
                        if (expression.rootNode) {
                            validateLevel(expression.rootNode);
                        } else {
                            errorsFound++;
                        }
                        break;
                    case 'operator':
                        if (expression.hasLeft) {
                            if (expression.leftNode) {
                                validateLevel(expression.leftNode);
                            } else {
                                errorsFound++;
                            }
                        }

                        if (expression.hasRight) {
                            if (expression.rightNode) {
                                validateLevel(expression.rightNode);
                            } else {
                                errorsFound++;
                            }
                        }
                        break;
                    case 'function':
                        expression.parameters.forEach(function (paramNode) {
                            validateLevel(paramNode);
                        })
                        break;
                }
            }

            return errorsFound === 0;
        }

        var validateDataMapping = function (config) {
            return config.output.stream.attributes.length === Object.keys(config.query.mapping).length;
        }

        var validateGroupBy = function (groupBy) {
            var isValid = true;
            if (groupBy.attributes.length > 0
                    && groupBy.havingFilter.expression
                                && typeof groupBy.havingFilter.expression !== 'string') {
                isValid = validateExpression(groupBy.havingFilter.expression);

                if (isValid) {
                    isValid = groupBy.havingFilter.expression.rootNode.genericReturnTypes.indexOf('bool') > -1;
                }
            }

            return isValid;
        }

        var validateAdvancedOutputOptions = function (advancedOutputOption) {
            var validOffset = true;
            var validLimit = true;
            var validRateLimit = true;

            if (Object.keys(advancedOutputOption.offset).length > 0) {
                if (!(advancedOutputOption.offset.value > -1)) {
                    validOffset = false;
                }
            }

            if (Object.keys(advancedOutputOption.limit).length > 0) {
                if (!(advancedOutputOption.offset.value > -1)) {
                    validLimit = false;
                }
            }

            if(Object.keys(advancedOutputOption.ratelimit).length > 0) {
                if (!(advancedOutputOption.ratelimit.value > -1)) {
                    validLimit = false;
                }
            }

            return validOffset && validLimit && validRateLimit;
        }

        return {
            isSourceSinkConfigValid,
            isStreamDefValid,
            isInputMappingValid,
            isOutputMappingValid,
            areInputOptionsValid,
            validateDataMapping,
            validateGroupBy,
            validateAdvancedOutputOptions
        };
    });