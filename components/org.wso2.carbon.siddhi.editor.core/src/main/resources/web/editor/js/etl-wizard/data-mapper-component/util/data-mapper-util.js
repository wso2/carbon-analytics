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

define(['require', 'jquery', 'lodash', 'log'],

    function (require, $, _, log) {

        var getGenericDataType = function (data_type) {
            switch (data_type) {
                case 'string':
                    return 'text';
                case 'bool':
                    return 'bool';
                case 'int':
                case 'long':
                case 'float':
                case 'double':
                    return 'number';
            }
        }

        var OperatorMap = {
            is_null: {
                returnTypes: ['bool'],
                beforeTypes: ['text'],
                afterTypes: ['bool'],
                symbol: 'IS NULL',
                description: 'Null Check',
                isFirst: false,
                isEnd: true
            },
            not: {
                returnTypes: ['bool'],
                beforeTypes: ['bool'],
                afterTypes: ['bool', 'text', 'number'],
                symbol: 'NOT',
                description: 'Logical Not',
                isFirst: true
            },
            multiply: {
                returnTypes: ['number'],
                beforeTypes: ['number'],
                afterTypes: ['number'],
                symbol: '*',
                description: 'Multiplication',
                isFirst: false
            },
            divide: {
                returnTypes: ['number'],
                beforeTypes: ['number'],
                afterTypes: ['number'],
                symbol: '/',
                description: 'Division',
                isFirst: false
            },
            modulo: {
                returnTypes: ['number'],
                beforeTypes: ['number'],
                afterTypes: ['number'],
                symbol: '%',
                description: 'Modulus',
                isFirst: false
            },
            addition: {
                returnTypes: ['number'],
                beforeTypes: ['number'],
                afterTypes: ['number'],
                symbol: '+',
                description: 'Addition',
                isFirst: false
            },
            subtraction: {
                returnTypes: ['number'],
                beforeTypes: ['number'],
                afterTypes: ['number'],
                symbol: '-',
                description: 'Subtraction',
                isFirst: false
            },
            less_than: {
                returnTypes: ['bool'],
                beforeTypes: ['number'],
                afterTypes: ['number'],
                symbol: '<',
                description: 'Less than',
                isFirst: false
            },
            less_than_equal: {
                returnTypes: ['bool'],
                beforeTypes: ['number'],
                afterTypes: ['number'],
                symbol: '<=',
                description: 'Less than or equal',
                isFirst: false
            },
            greater_than: {
                returnTypes: ['bool'],
                beforeTypes: ['number'],
                afterTypes: ['number'],
                symbol: '>',
                description: 'Greater than',
                isFirst: false
            },
            greater_than_equal: {
                returnTypes: ['bool'],
                beforeTypes: ['number'],
                afterTypes: ['number'],
                symbol: '>=',
                description: 'Greater than or equal',
                isFirst: false
            },
            equal: {
                returnTypes: ['bool'],
                beforeTypes: ['text', 'number'],
                afterTypes: ['text', 'number'],
                symbol: '==',
                description: 'Equal comparison',
                isFirst: false
            },
            not_equal: {
                returnTypes: ['bool'],
                beforeTypes: ['text', 'number'],
                afterTypes: ['text', 'number'],
                symbol: '!=',
                description: 'Not equal comparison',
                isFirst: false
            },
            and: {
                returnTypes: ['bool'],
                beforeTypes: ['text', 'number', 'bool'],
                afterTypes: ['text', 'number', 'bool'],
                symbol: 'AND',
                description: 'Logical AND',
                isFirst: false
            },
            or: {
                returnTypes: ['bool'],
                beforeTypes: ['text', 'number', 'bool'],
                afterTypes: ['text', 'number', 'bool'],
                symbol: 'OR',
                description: 'Logical OR',
                isFirst: false
            }
        };

        var OperatorMap2 = {
            is_null: {
                returnTypes: ['bool'],
                leftTypes: ['text'],
                rightTypes: ['bool'],
                hasLeft: true,
                hasRight: false,
                symbol: 'IS NULL',
                description: 'Null Check',
                isFirst: false,
                isEnd: true
            },
            not: {
                returnTypes: ['bool'],
                leftTypes: ['bool'],
                rightTypes: ['bool', 'text', 'number'],
                hasLeft: false,
                hasRight: true,
                symbol: 'NOT',
                description: 'Logical Not',
                isFirst: true
            },
            multiply: {
                returnTypes: ['number'],
                leftTypes: ['number'],
                rightTypes: ['number'],
                symbol: '*',
                hasLeft: true,
                hasRight: true,
                description: 'Multiplication',
                isFirst: false
            },
            divide: {
                returnTypes: ['number'],
                leftTypes: ['number'],
                rightTypes: ['number'],
                symbol: '/',
                hasLeft: true,
                hasRight: true,
                description: 'Division',
                isFirst: false
            },
            modulo: {
                returnTypes: ['number'],
                leftTypes: ['number'],
                rightTypes: ['number'],
                symbol: '%',
                hasLeft: true,
                hasRight: true,
                description: 'Modulus',
                isFirst: false
            },
            addition: {
                returnTypes: ['number'],
                leftTypes: ['number'],
                rightTypes: ['number'],
                symbol: '+',
                hasLeft: true,
                hasRight: true,
                description: 'Addition',
                isFirst: false
            },
            subtraction: {
                returnTypes: ['number'],
                leftTypes: ['number'],
                rightTypes: ['number'],
                symbol: '-',
                hasLeft: true,
                hasRight: true,
                description: 'Subtraction',
                isFirst: false
            },
            less_than: {
                returnTypes: ['bool'],
                leftTypes: ['number'],
                rightTypes: ['number'],
                symbol: '<',
                hasLeft: true,
                hasRight: true,
                description: 'Less than',
                isFirst: false
            },
            less_than_equal: {
                returnTypes: ['bool'],
                leftTypes: ['number'],
                rightTypes: ['number'],
                symbol: '<=',
                hasLeft: true,
                hasRight: true,
                description: 'Less than or equal',
                isFirst: false
            },
            greater_than: {
                returnTypes: ['bool'],
                leftTypes: ['number'],
                rightTypes: ['number'],
                symbol: '>',
                hasLeft: true,
                hasRight: true,
                description: 'Greater than',
                isFirst: false
            },
            greater_than_equal: {
                returnTypes: ['bool'],
                leftTypes: ['number'],
                rightTypes: ['number'],
                symbol: '>=',
                hasLeft: true,
                hasRight: true,
                description: 'Greater than or equal',
                isFirst: false
            },
            equal: {
                returnTypes: ['bool'],
                leftTypes: ['text', 'number'],
                rightTypes: ['text', 'number'],
                symbol: '==',
                hasLeft: true,
                hasRight: true,
                description: 'Equal comparison',
                isFirst: false
            },
            not_equal: {
                returnTypes: ['bool'],
                leftTypes: ['text', 'number'],
                rightTypes: ['text', 'number'],
                symbol: '!=',
                hasLeft: true,
                hasRight: true,
                description: 'Not equal comparison',
                isFirst: false
            },
            and: {
                returnTypes: ['bool'],
                leftTypes: ['text', 'number', 'bool'],
                rightTypes: ['text', 'number', 'bool'],
                symbol: 'AND',
                hasLeft: true,
                hasRight: true,
                description: 'Logical AND',
                isFirst: false
            },
            or: {
                returnTypes: ['bool'],
                leftTypes: ['text', 'number', 'bool'],
                rightTypes: ['text', 'number', 'bool'],
                symbol: 'OR',
                hasLeft: true,
                hasRight: true,
                description: 'Logical OR',
                isFirst: false
            }
        };

        var generateExpressionHTML = function (highlightIndex, node) {
            var htmlContent = '';

            var i = 0;
            if (node.children) {
                node.children.forEach(function (childNode) {
                    switch (childNode.nodeType) {
                        case 'attribute':
                            htmlContent += childNode.name;
                            break;
                        case 'customValue':
                            if (childNode.genericDataType === 'text') {
                                htmlContent += `\'${childNode.value}\'`
                            } else {
                                htmlContent += childNode.value;
                            }
                            break;
                        case 'operator':
                            htmlContent += ` ${childNode.symbol} `;
                            break;
                        case 'function':
                            htmlContent += `<span class="item-${i} ${highlightIndex != null ? (highlightIndex === i ? 'selected' : '') : ''}">`;
                            htmlContent += generateExpressionHTML(highlightIndex, childNode);
                            htmlContent += '</span>';
                            break;
                        case 'scope':
                            htmlContent += `<span class="item-${i} ${highlightIndex != null ? (highlightIndex === i ? 'selected' : '') : ''} ${childNode.children.length > 0 ? 'ok-clear' : ''}">(${generateExpressionHTML(null, childNode)})</span>`;
                            break;
                    }
                    i++;
                });
            } else {
                if (node.nodeType === 'function') {
                    htmlContent += `${node.displayName.slice(0, -1)}`
                    var isFirst = true;
                    node.parameters.forEach(function (parameterNode) {
                        if (!isFirst) {
                            htmlContent += ', '
                        }

                        if (parameterNode.nodeType === 'scope') {
                            // title="${parameterNode.placeholder}"
                            htmlContent += `<span title="${parameterNode.placeholder}" class="param-${i} ${highlightIndex != null ? (highlightIndex === i ? 'selected' : '') : ''} ${parameterNode.children.length > 0 ? 'ok-clear' : ''}">${generateExpressionHTML(null, parameterNode)}</span>`;
                        }
                        isFirst = false;
                        i++;
                    });
                    if (node.allowRepetitiveParameters) {
                        htmlContent += `<span title="Add parameter" style="display: none;" class="add-param"><i style="font-size: 1.3rem; padding-left: 1rem;" class="fw fw-import"></i></span>`
                    }
                    htmlContent += `)`;

                }
            }


            return htmlContent.length === 0 ? '...' : htmlContent;
        }

        var generateExpressionHTML2 = function (node, id) {
            var htmlContent = '';

            switch (node.type) {
                case 'attribute':
                    htmlContent += node.name;
                    break;
                case 'customValue':
                    node.genericReturnTypes.indexOf('text') > -1 ?
                        htmlContent += `"${node.value}"`
                        : htmlContent += node.value
                    break;
                case 'operator':
                    if (node.hasLeft) {
                        node.leftNode ? htmlContent += generateExpressionHTML2(node.leftNode, `${id}-l`) : '...'
                    }
                    htmlContent += ` ${node.symbol} `
                    if (node.hasRight) {
                        node.rightNode ? htmlContent += generateExpressionHTML2(node.rightNode, `${id}-r`) : '...'
                    }
                    break;
                case 'function':
                    htmlContent += `<span id="fn${id}">${node.displayName.slice(0, -1)}`;
                    var isFirst = true;
                    node.parameters.forEach(function (param, i) {
                        if (!isFirst) {
                            htmlContent += ', '
                        }

                        htmlContent += `<span id="fn-param${id}-${i}" >${generateExpressionHTML2(param, `${id}-${i}`)}</span>`

                        isFirst = false;
                    })

                    htmlContent += `)</span>`;
                    break;
                case 'scope':
                    if (id.length === 0) {
                        htmlContent += `${node.rootNode ? generateExpressionHTML2(node.rootNode, '-n') : '...'}`
                    } else {
                        htmlContent += `
                            <span id="scope${id}" >
                                (&nbsp;${node.rootNode? generateExpressionHTML2(node.rootNode, `${id}-n`) : '...'}&nbsp;)
                            </span>`;
                    }
                    break;
            }

            return htmlContent.length === 0 ? '...' : htmlContent;
        }

        var validateExpressionTree = function (expression) {
            var errorsFound = 0;
            validateLevel(expression);

            function validateLevel(expression) {
                if (expression.nodeType === 'scope') {

                    if (expression.children.length === 0) {
                        errorsFound++;
                    }

                    if (expression.children.length > 0 &&
                        expression.children[expression.children.length - 1].nodeType === 'operator' &&
                        !(expression.children[expression.children.length - 1].isEnd)) {

                        errorsFound++;
                    }

                    expression.children.forEach(function (childNode) {
                        validateLevel(childNode);
                    });

                } else if (expression.nodeType === 'function') {
                    expression.parameters.forEach(function (parameter) {
                        validateLevel(parameter);
                    })
                }
            }

            return errorsFound === 0;
        }

        return { getGenericDataType, OperatorMap, OperatorMap2, generateExpressionHTML, generateExpressionHTML2, validateExpressionTree };
    });
