/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *fString
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

define(['require', 'log', 'lodash', 'jquery', 'appData', 'initialiseData', 'jsonValidator', 'app/source-editor/completion-engine'],
    function (require, log, _, $, AppData, InitialiseData, JSONValidator, CompletionEngine) {
        var operatorMap = {
            is_null: {
                return_types: ['bool'],
                beforeTypes: ['text'],
                afterTypes: ['bool'],
                symbol: 'IS NULL',
                description: 'Null Check',
                isFirst: false,
                isEnd: true
            },
            not: {
                return_types: ['bool'],
                beforeTypes: ['bool'],
                afterTypes: ['bool'],
                symbol: 'NOT',
                description: 'Logical Not',
                isFirst: true
            },
            multiply: {
                return_types: ['number'],
                before_types: ['number'],
                afterTypes: ['number'],
                symbol: '*',
                description: 'Multiplication',
                isFirst: false
            },
            divide: {
                return_types: ['number'],
                before_types: ['number'],
                afterTypes: ['number'],
                symbol: '/',
                description: 'Division',
                isFirst: false
            },
            modulo: {
                return_types: ['number'],
                before_types: ['number'],
                afterTypes: ['number'],
                symbol: '%',
                description: 'Modulus',
                isFirst: false
            },
            addition: {
                return_types: ['number'],
                before_types: ['number'],
                afterTypes: ['number'],
                symbol: '+',
                description: 'Addition',
                isFirst: false
            },
            subtraction: {
                return_types: ['number'],
                before_types: ['number'],
                afterTypes: ['number'],
                symbol: '-',
                description: 'Subtraction',
                isFirst: false
            },
            less_than: {
                return_types: ['bool'],
                before_types: ['number'],
                afterTypes: ['number'],
                symbol: '<',
                description: 'Less than',
                isFirst: false
            },
            less_than_equal: {
                return_types: ['bool'],
                before_types: ['number'],
                afterTypes: ['number'],
                symbol: '<=',
                description: 'Less than or equal',
                isFirst: false
            },
            greater_than: {
                return_types: ['bool'],
                before_types: ['number'],
                afterTypes: ['number'],
                symbol: '>',
                description: 'Greater than',
                isFirst: false
            },
            greater_than_equal: {
                return_types: ['bool'],
                before_types: ['number'],
                afterTypes: ['number'],
                symbol: '>=',
                description: 'Greater than or equal',
                isFirst: false
            },
            equal: {
                return_types: ['bool'],
                before_types: ['text', 'number'],
                afterTypes: ['text', 'number'],
                symbol: '==',
                description: 'Equal comparison',
                isFirst: false
            },
            not_equal: {
                return_types: ['bool'],
                before_types: ['text', 'number'],
                afterTypes: ['text', 'number'],
                symbol: '!=',
                description: 'Not equal comparison',
                isFirst: false
            },
            and: {
                return_types: ['bool'],
                before_types: ['bool'],
                afterTypes: ['bool'],
                symbol: 'AND',
                description: 'Logical AND',
                isFirst: false
            },
            or: {
                return_types: ['bool'],
                before_types: ['bool'],
                afterTypes: ['bool'],
                symbol: 'OR',
                description: 'Logical OR',
                isFirst: false
            }
        };

        var ETLTaskView = function (options, container, callback, appObject) {
            console.log(this);
            // var self = this;
            this.inputAttributes = [
                {
                    name: 'id',
                    type: 'string',
                },
                {
                    name: 'name',
                    type: 'string',
                },
                {
                    name: 'amount',
                    type: 'int'
                }
            ];

            this.outputAttributes = [
                {
                    name: 'name',
                    type: 'string'
                },
                {
                    name: 'isTheCorrect',
                    type: 'bool'
                },
                {
                    name: 'count',
                    type: 'int'
                }
            ];

            var color = 'gray';

            this.jsPlumbInstance = window.j = jsPlumb.getInstance({
                Connector: ["Straight", {curviness: 50}],
                DragOptions: {cursor: "pointer", zIndex: 2000},
                PaintStyle: {stroke: color, strokeWidth: 2},
                EndpointStyle: {radius: 3, fill: 'rgba(0, 0, 0, 0)'},
                endpointHoverStyle: {fill: 'rgba(0, 0, 0, 0)'},
                HoverPaintStyle: {stroke: "#ec9f2e"},
                EndpointHoverStyle: {fill: "#ec9f2e"},
                Container: $(container).find('.etl-task-wizard-canvas')
            })
            this.container = container;
            this.inputAttributeEndpoints = {};
            this.outputAttributeEndpoints = {};
            this.connectionMapRef = {};
            this.expressionMap = {};
            this.coordinate = [];
            this.expressionGenerationDialog = $(container).find('.popup-backdrop').clone();
            $(container).prepend(this.expressionGenerationDialog);

            this.inputListContainer = $(container).find('.etl-task-wizard-canvas').find('.inputs').find('.attributeList');
            this.outputListContainer = $(container).find('.etl-task-wizard-canvas').find('.outputs').find('.attributeList')

            //function binding
            this.showExpressionDialog = this.showExpressionDialog.bind(this);
            this.renderAttributes = this.renderAttributes.bind(this);
            this.renderFunctionAttributeSelector = this.renderFunctionAttributeSelector.bind(this);
            this.renderExpressionGeneratorComponents = this.renderExpressionGeneratorComponents.bind(this);
            this.hideExpressionGenerationDialog = this.hideExpressionGenerationDialog.bind(this);
            this.addNodeToExpression = this.addNodeToExpression.bind(this);
            this.displayExpression = this.displayExpression.bind(this);

            this.renderAttributes(this.inputAttributes, this.outputAttributes);
            this.functionDataMap = this.generateExpressionMap(this.inputAttributes, CompletionEngine.getRawMetadata());

            console.log(this.functionDataMap);
            // Regex for stuff inside bracket: \((.*?)\)
            // Regex for breaking parameters inside bracket: [a-zA-Z .<>]+
            var regExp = /\(([^)]+)\)/;
            // regexp.exec(syntax.syntax)[1].split(',')[0].match('[a-zA-Z0-9.]+$')

        }

        ETLTaskView.prototype.renderAttributes = function (inputAttributes, outputAttributes) {
            var inputListContainer = this.inputListContainer;
            var outputListContainer = this.outputListContainer;
            var jsPlumbInstance = this.jsPlumbInstance;
            var inputEndpointMap = {};
            var outputEndpointMap = {};
            var showExpressionDialog = this.showExpressionDialog;

            inputAttributes.forEach(function (element) {
                var inputAttribElement = inputListContainer.append(`
                    <li>
                        <div class="attribute" style="">
                            ${element.name}
                            <div class="attrib-type" style="">
                                ${element.type}
                            </div>
                        </div>
                    </li>
                `);

                inputEndpointMap[element.name] = jsPlumbInstance.addEndpoint($(inputAttribElement).children().last(), {anchor: 'Right'}, {isSource: true});
            });

            outputAttributes.forEach(function (element) {
                var outputAttribElement = outputListContainer.append(`
                    <li>
                        <div class="attribute" style="">
                            ${element.name}
                            <div class="clear-icon">
                                <a href="#" title="Clear mapping" href="#" class="icon clear" style="">
                                    <i class="fw fw-clear"></i>
                                </a>
                            </div>
                            <div class="attrib-type" style="">
                                ${element.type}
                            </div>
                            <div class="mapped-expression" style="">
                            </div>
                        </div>
                    </li>
                `);

                outputAttribElement.children().last().on('click', function (evt) {
                    evt.stopPropagation()
                    showExpressionDialog(element);
                });
                outputEndpointMap[element.name] = jsPlumbInstance.addEndpoint($(outputAttribElement).children().last(), {anchor: 'Left'}, {isTarget: true});

            });

            this.inputAttributeEndpoints = inputEndpointMap;
            this.outputAttributeEndpoints = outputEndpointMap;
        }

        ETLTaskView.prototype.showExpressionDialog = function (output_attribute) {
            this.expressionMap[output_attribute.name] = new ScopeNode([output_attribute.type]);
            var expressionGeneratorContainer = this.expressionGenerationDialog.show();
            var container = this.container;
            this.renderExpressionGeneratorComponents(output_attribute.name, this.expressionMap[output_attribute.name]);
            var hideExpressionGenerationDialog = this.hideExpressionGenerationDialog;
            this.displayExpression(output_attribute.name);

            $(expressionGeneratorContainer).find('.btn-default').on('click', function () {
                hideExpressionGenerationDialog(container, expressionGeneratorContainer);
            });


        }

        ETLTaskView.prototype.hideExpressionGenerationDialog = function (container, expressionGeneratorContainer) {
            expressionGeneratorContainer.remove();
            expressionGeneratorContainer = $(container).find('.popup-backdrop').clone();
            $(container).prepend(expressionGeneratorContainer);
            this.expressionGenerationDialog = expressionGeneratorContainer;
        }

        ETLTaskView.prototype.renderExpressionGeneratorComponents = function (outputAttributeName, expression) {
            var coordinates = this.coordinate;
            var attributes = this.inputAttributes;
            var functions = this.functionDataMap[expression.dataType];
            var operators = _.filter(operatorMap, function (operator) {
                return operator.return_types.indexOf(expression.genericDataType) > -1
            });
            var renderAttributeSelector = this.renderFunctionAttributeSelector;
            var nodeCategoryContainer = this.expressionGenerationDialog.find('.node-category');


            expression.dataTypes.forEach(function (dataType) {
                var test= _.filter(attributes, function(att) {
                    return att.type.toLowerCase() === dataType;
                });
            });


            operators.unshift({
                return_types: ['bool', 'text', 'number'],
                beforeTypes: ['bool', 'text', 'number'],
                afterTypes: ['bool', 'text', 'number'],
                symbol: '()',
                description: 'Bracket',
                isFirst: true,
                scope: true
            });

            $(nodeCategoryContainer).append(`
                                <li>
                                    <a>
                                        <div style="">
                                            Attribute
                                        </div>
                                    </a>
                                </li>
                            `)

            $(nodeCategoryContainer).children().last().on('click', function (evt) {
                $(nodeCategoryContainer).find('li>a>div').removeClass('selected');
                $(evt.target).addClass('selected');
                renderAttributeSelector('attribute', attributes, outputAttributeName);
            })

            $(nodeCategoryContainer).append(`
                <li>
                    <a>
                        <div class="" style="">
                            Function
                        </div>
                    </a>
                </li>
            `);

            $(nodeCategoryContainer).children().last().on('click', function (evt) {
                $(nodeCategoryContainer).find('li>a>div').removeClass('selected');
                $(evt.target).addClass('selected');
                renderAttributeSelector('function', functions['function'], outputAttributeName);
            })


            $(nodeCategoryContainer).append(`
                <li>
                    <a>
                        <div class="" style="">
                            Operator
                        </div>
                    </a>
                </li>
            `);

            $(nodeCategoryContainer).children().last().on('click', function (evt) {
                $(nodeCategoryContainer).find('li>a>div').removeClass('selected');
                $(evt.target).addClass('selected');
                renderAttributeSelector('operator', operators, outputAttributeName);
            });

            $(nodeCategoryContainer).children().first().click();
        }

        ETLTaskView.prototype.renderFunctionAttributeSelector = function (type, attributeFunctionArray, outputAttributeName) {
            var nodeCategoryContainer = this.expressionGenerationDialog.find('.att-fun-op-container');
            var addNodeToExpression = this.addNodeToExpression;
            var attributeContainer = $(nodeCategoryContainer).find('.select-function-operator-attrib');
            var syntaxSelectorContainer = $(nodeCategoryContainer).find('.select-function-format-container');

            $(attributeContainer).find('.attrib-selector-containers').children().remove();

            console.log(attributeFunctionArray);
            Object.values(attributeFunctionArray).forEach(function (element) {
                var displayName = '';
                var description = '';
                var elementData = '';

                switch (type) {
                    case 'attribute':
                        displayName = element.name;
                        description = element.type;
                        break;
                    case 'function':
                        displayName = element.displayName;
                        description = element.description;
                        break;
                    case 'operator':
                        displayName = `${element.symbol} - ${element.description}`;
                }

                $(attributeContainer).find('.attrib-selector-containers').append(`
                    <a>
                        <div class="attribute" style="">
                            <div>
                                ${displayName}
                            </div>
                            <div class="description" style="">
                                ${description}
                            </div>
                        </div>
                    </a>
                `);

                $(attributeContainer).find('.attrib-selector-containers').children().last().on('click', function () {
                    if (type !== 'function') {
                        if (!element.scope) {
                            addNodeToExpression(type, element, outputAttributeName);
                        } else {
                            addNodeToExpression('scope', element, outputAttributeName);
                        }
                    } else {
                        console.log(element);

                        element.syntax.forEach(function (syntax_obj) {
                            $(syntaxSelectorContainer).find('ul').append(`
                                <li>
                                    <a style="">
                                        <div class="function-syntax" style="">
                                            <div class="syntax-expression">
                                                ${syntax_obj.syntax}
                                            </div>
<!--                                            <div class="description" style="">-->
<!--                                                Test_Attrib_Description-->
<!--                                            </div>-->
                                        </div>
                                    </a>
                                </li>
                            `);

                            $(syntaxSelectorContainer).find('ul').children().last().on('click', function () {
                                element['syntax_selected'] = syntax_obj;
                                addNodeToExpression(type, element, outputAttributeName);
                            });

                            $(syntaxSelectorContainer).show();
                            $(attributeContainer).hide();
                        });
                    }
                })
            });

            $(nodeCategoryContainer).find('.select-function-operator-attrib').show();
        }

        ETLTaskView.prototype.addNodeToExpression = function (type, node_data, outputAttributeName) {
            var coordinates = this.coordinate;
            var node = null;
            var data = null;

            switch (type) {
                case 'attribute':
                    data = {
                        name: node_data.name,
                        dataType: node_data.type,
                    };
                    node = new AttributeNode(data);
                    break;
                case 'operator':
                    data = {
                        symbol: node_data.symbol,
                        dataType: this.expressionMap[outputAttributeName].dataType,
                        isEnd: node_data.isFirst | false,
                    };
                    node = new OperatorNode(data);
                    break;
                case 'scope':
                    node = new ScopeNode([this.expressionMap[outputAttributeName].dataType]);
                    break;
                case 'function':
                    data = {
                        displayName: node_data.displayName,
                        dataType: this.expressionMap[outputAttributeName].dataType,
                        selectedSyntax: node_data['syntax_selected']
                    }
                    node = new FunctionNode(data);
            }

            if (coordinates.length === 0) {
                this.expressionMap[outputAttributeName].addNodeToMainExpression(node);
            } else {
                this.expressionMap[outputAttributeName].addNodeToChildExpression(coordinates, node);
            }
            this.displayExpression(outputAttributeName);
        }

        ETLTaskView.prototype.displayExpression = function (outputAttrName) {
            var htmlContent = generateExpressionHTML(this.expressionMap[outputAttrName]);
            $(this.container).find('.main-exp').empty()
            $(this.container).find('.main-exp').append(htmlContent);
        }

        ETLTaskView.prototype.generateExpressionMap = function (inputAttributes, expressionFunctions) {
            var supportedExtensionTypes = ['time', 'env', 'geo', 'math', 'str'];
            var expressionMap = {
                string: {}, int: {}, long: {}, double: {}, float: {}, bool: {}, object: {}
            }

            inputAttributes.forEach(function (attrib) {
                if (!expressionMap[attrib.type.toLowerCase()]['attribute']) {
                    expressionMap[attrib.type.toLowerCase()]['attribute'] = {}
                }
                expressionMap[attrib.type.toLowerCase()]['attribute'][attrib.name] = attrib;
            });

            supportedExtensionTypes.forEach(function (extensionType) {
                expressionFunctions['extensions'][extensionType].functions.forEach(function (func) {
                    if (func.returnAttributes) {
                        func.returnAttributes[0].type.forEach(function (type) {
                            if (!expressionMap[type.toLowerCase()]['function']) {
                                expressionMap[type.toLowerCase()]['function'] = {}
                            }
                            func['displayName'] = func.namespace + ':' + func.name + '()';
                            expressionMap[type.toLowerCase()]['function'][func.name] = func;
                        });
                    }

                });
            })

            expressionFunctions['inBuilt'].functions.forEach(function (func) {
                if (func.returnAttributes) {
                    func.returnAttributes[0].type.forEach(function (type) {
                        if (!expressionMap[type.toLowerCase()]['function']) {
                            expressionMap[type.toLowerCase()]['function'] = {}
                        }
                        func['displayName'] = func.name + '()';
                        expressionMap[type.toLowerCase()]['function'][func.name] = func;
                    })
                }

            });

            return expressionMap;
        }

        var getGenericDataType = function (data_type) {
            switch (data_type) {
                case 'string':
                    return 'text';
                case 'bool':
                    return 'bool';
                case 'int':
                case 'long':
                case 'float':
                case 'doble':
                    return 'number';
            }
        }

// Function node for Expression structure

        var AttributeNode = function (node_data) {
            this.name = node_data.name;
            this.dataType = node_data.dataType;
            this.nodeType = 'attribute';
            this.genericDataType = getGenericDataType(node_data.dataType);
        }

        var OperatorNode = function (node_data) {
            this.symbol = node_data.symbol;
            this.dataType = node_data.dataType;
            this.nodeType = 'operator';
            this.genericDataType = getGenericDataType(node_data.dataType);
            this.isEnd = node_data.isEnd;
        }

        var CustomValueNode = function (node_data) {
            this.value = node_data.value;
            this.dataType = node_data.dataType;
            this.nodeType = 'customValue';
            this.genericDataType = getGenericDataType(node_data.dataType);
        }

        var FunctionNode = function (node_data) {
            this.displayName = node_data.displayName;
            this.dataType = node_data.dataType;
            this.nodeType = 'function';
            this.genericDataType = getGenericDataType(node_data.dataType)
            this.parameters = this.generateParameters(node_data.selectedSyntax);
        }

        FunctionNode.prototype.generateParameters = function (syntax) {
            var parameters = [];

            // TODO : write parameter generation from selected syntax
            console.log(syntax);

            syntax.syntax.match('\((.*?)\)').forEach(function (match) {
                console.log(match);
            })



            return parameters;
        }

        var ScopeNode = function (data_types) {
            this.dataTypes = data_types;
            this.supportedGenericDataTypes = data_types.map(function (data_type) {
                return getGenericDataType(data_type);
            });
            this.canBeLast = true;
            this.children = [];
            this.nodeType = 'scope';
        }

        ScopeNode.prototype.addNodeToMainExpression = function (node) {
            this.children.push(node);
        }

        ScopeNode.prototype.addNodeToChildExpression = function (coordinates, node) {
            var array = this.children;
            addValueToArray(coordinates.length, coordinates, node, array);
            this.children = array;
        }

        function addValueToArray(level, coordinates, val, array) {
            if (level === 1 && coordinates.length === 1) {
                return array[coordinates[0]].push(val);
            }

            if (level === 1) {
                return array.push(val)
            }

            return addValueToArray(level - 1, coordinates, val, array[coordinates[coordinates.length - level]]);
        }

        function deleteValueFromArray(level, coordinates, array) {
            if (level === 1 && coordinates.length === 1) {
                return array.splice(coordinates[0], 1);
            }

            if (level === 1) {
                return array.splice(coordinates[coordinates.length - 1], 1);
            }

            return deleteValueFromArray(level - 1, coordinates, array[coordinates[coordinates.length - level]]);
        }

        var generateExpressionHTML = function (node) {
            var htmlContent = '';

            var i = 0;
            if (node.children) {
                node.children.forEach(function (node) {
                    switch (node.nodeType) {
                        case 'attribute':
                            htmlContent += node.name;
                            break;
                        case 'customValue':
                            htmlContent += node.value;
                            break;
                        case 'operator':
                            htmlContent += node.symbol;
                            break;
                        case 'function':
                            htmlContent += `<span class="item-${i}">`;
                            var isFirst = true;
                            node.parameters.forEach(function (parameterNode) {
                                if (!isFirst) {
                                    htmlContent += ', '
                                }

                                if (parameterNode.nodeType === 'scope') {
                                    htmlContent += generateExpressionHTML(node);
                                }

                                isFirst = false;
                            })
                            htmlContent += '</span>';
                            break;
                        case 'scope':
                            htmlContent += `<span class="item-${i}">(${generateExpressionHTML(node)})</span>`;
                            break;
                    }
                    i++;
                });
            } else {
                if (node.nodeType === 'function') {
                    htmlContent += `${node.displayName.slice(0, -1)}`

                    node.parameters.forEach(function (parameterNode) {
                        if (!isFirst) {
                            htmlContent += ', '
                        }

                        if (parameterNode.nodeType === 'scope') {
                            htmlContent += `<span class="fun-param-${i}">${generateExpressionHTML(node)}<span class="item-${i}">`;
                        }

                        isFirst = false;
                    })
                    htmlContent += `)`;
                    i++;
                }
            }

            if (htmlContent.length === 0) {
                return '...';
            } else {
                return htmlContent;
            }
        }

        return ETLTaskView;
    })
;
