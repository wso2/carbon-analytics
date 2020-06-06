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

define(['require', 'log', 'lodash', 'jquery', 'appData', 'initialiseData', 'jsonValidator', 'app/source-editor/completion-engine', 'alerts'],
    function (require, log, _, $, AppData, InitialiseData, JSONValidator, CompletionEngine, alerts) {
        var operatorMap = {
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
                afterTypes: ['bool'],
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

        var ETLTaskView = function (options, container, callback, appObject) {
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
            this.focusNode = [];
            this.currenOutputElement = null;
            this.expressionGenerationDialog = $(container).find('.popup-backdrop').clone();
            $(container).prepend(this.expressionGenerationDialog);

            this.inputListContainer = $(container).find('.etl-task-wizard-canvas').find('.inputs').find('.attributeList');
            this.outputListContainer = $(container).find('.etl-task-wizard-canvas').find('.outputs').find('.attributeList')

            //function binding
            this.showExpressionDialog = this.showExpressionDialog.bind(this);
            this.renderAttributes = this.renderAttributes.bind(this);
            this.renderFunctionAttributeSelector = this.renderFunctionAttributeSelector.bind(this);
            this.hideExpressionGenerationDialog = this.hideExpressionGenerationDialog.bind(this);
            this.addNodeToExpression = this.addNodeToExpression.bind(this);
            this.displayExpression = this.displayExpression.bind(this);
            this.renderGenerator = this.renderGenerator.bind(this);
            this.updateExpression = this.updateExpression.bind(this);
            this.addCoordinate = this.addCoordinate.bind(this);
            this.removeCoordinate = this.removeCoordinate.bind(this);
            this.updateConnections = this.updateConnections.bind(this);

            this.renderAttributes(this.inputAttributes, this.outputAttributes);
            this.functionDataMap = this.generateExpressionMap(this.inputAttributes, CompletionEngine.getRawMetadata());
        }

        ETLTaskView.prototype.updateConnections = function (outputAttribute) {
            var jsPlumbInstance = this.jsPlumbInstance;
            var inputAttributeEndpoints = this.inputAttributeEndpoints;
            var outputAttributeEndpoints = this.outputAttributeEndpoints;
            var generatedExpression = this.expressionMap[outputAttribute] ?
                generateExpressionHTML(null, this.expressionMap[outputAttribute]) : '';

            $(outputAttributeEndpoints[outputAttribute].element).find('.mapped-expression').empty();
            $(outputAttributeEndpoints[outputAttribute].element).find('.mapped-expression').append(`
               ${generatedExpression}
            `);

            this.inputAttributes.forEach(function (inputAttribute) {
                if (generatedExpression.indexOf(inputAttribute.name) > -1) {
                    jsPlumbInstance.connect({
                        source: inputAttributeEndpoints[inputAttribute.name],
                        target: outputAttributeEndpoints[outputAttribute]
                    })
                }
            })

        }

        ETLTaskView.prototype.renderAttributes = function (inputAttributes, outputAttributes) {
            var inputListContainer = this.inputListContainer;
            var outputListContainer = this.outputListContainer;
            var jsPlumbInstance = this.jsPlumbInstance;
            var inputEndpointMap = {};
            var outputEndpointMap = {};
            var showExpressionDialog = this.showExpressionDialog;
            var expressionMap = this.expressionMap;
            var updateConnections = this.updateConnections;

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

                inputEndpointMap[element.name] = jsPlumbInstance.addEndpoint($(inputAttribElement).children().last(), {anchor: 'Right'}, {
                    isSource: true,
                    maxConnections: -1
                });
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

                outputAttribElement.children().last().find('.clear-icon').on('click', function (evt) {
                    evt.stopPropagation();
                    delete expressionMap[element.name];
                    jsPlumbInstance.deleteConnectionsForElement(outputEndpointMap[element.name].element);
                    updateConnections(element.name);
                });

                outputEndpointMap[element.name] = jsPlumbInstance.addEndpoint($(outputAttribElement).children().last(), {anchor: 'Left'}, {
                    isTarget: true,
                    maxConnections: -1
                });

            });

            this.inputAttributeEndpoints = inputEndpointMap;
            this.outputAttributeEndpoints = outputEndpointMap;
        }

        ETLTaskView.prototype.showExpressionDialog = function (output_attribute) {
            this.currenOutputElement = output_attribute.name;
            this.expressionMap[output_attribute.name] = this.expressionMap[output_attribute.name] ?
                this.expressionMap[output_attribute.name] : new ScopeNode([output_attribute.type]);
            var container = this.container;
            var hideExpressionGenerationDialog = this.hideExpressionGenerationDialog;
            var expressionGeneratorContainer = this.expressionGenerationDialog.show();
            var coordinates = this.coordinate;
            var expressionMap = this.expressionMap;
            var expression = this.expressionMap[output_attribute.name];
            var initialExpression = _.cloneDeep(this.expressionMap[output_attribute.name])
            var updateConnections = this.updateConnections;

            this.renderGenerator();

            $(expressionGeneratorContainer).find('.btn-default').on('click', function () {
                expressionMap[output_attribute.name] = initialExpression;
                hideExpressionGenerationDialog(container, expressionGeneratorContainer);
            });

            $(expressionGeneratorContainer).find('.btn-primary').on('click', function () {
                if (coordinates.length > 0) {
                    alerts.error('Please complete the expression creation process to submit');
                } else {
                    var isExpressionValid = validateExpressionTree(expression);

                    if (isExpressionValid) {
                        updateConnections(output_attribute.name);
                        hideExpressionGenerationDialog(container, expressionGeneratorContainer);
                    } else {
                        alerts.error('Please complete the expression creation process to submit');
                    }
                }
            });
        }

        ETLTaskView.prototype.addCoordinate = function (index) {
            this.coordinate.push(index);
        }

        ETLTaskView.prototype.removeCoordinate = function () {
            this.coordinate.pop();
        }

        ETLTaskView.prototype.renderGenerator = function () {
            var container = this.container;
            var expressionContainer = $(container).find('.expression-container');
            var expression = this.expressionMap[this.currenOutputElement];
            var coordinates = this.coordinate;
            var focusNodes = this.focusNode;
            var functionDataMap = this.functionDataMap;
            var updateExpression = this.updateExpression;
            var addToCoordinates = this.addCoordinate;
            var renderExpression = this.renderGenerator;
            $(this.container).find('.dialog-heading').text('');
            $(this.container).find('.dialog-heading')
                .append(`Create expression for attribute '<b>${this.currenOutputElement}</b>'`);

            // render the main Expression
            expressionContainer.empty();
            var tempExp = expression;

            if (coordinates.length === 0) {
                // render the expression if none of the expression nodes are selected
                expressionContainer.append(`
                    <div class="expression target" style="display: flex">
                        <div class="exp-content" style="width: 100%;">
                            ${generateExpressionHTML(null, expression)}
                        </div>
                    </div>
                `);
            } else {
                expressionContainer.append(`
                    <div class="expression" style="">
                        ${generateExpressionHTML(coordinates[0], expression)}
                    </div>
                `);
            }

            // render expression when one attribute/function/scope is selected in drill down form
            coordinates.forEach(function (index, i) {
                tempExp = focusNodes[i];

                if (i === (coordinates.length - 1)) {
                    expressionContainer.append(`
                        <div class="expression target" style="display: flex">
                            <div class="exp-content">
                                ${generateExpressionHTML(null, tempExp)}
                            </div>
                            <div class="expression-merge">
                                <a href="#">
                                    <i class="fw fw-up"></i>
                                </a>
                            </div>
                        </div>
                    `);
                } else {
                    expressionContainer.append(`
                        <div class="expression" style="">
                            ${generateExpressionHTML(coordinates[i + 1], tempExp)}
                        </div>
                    `);
                }
            });

            $(expressionContainer).find('.expression.target>.exp-content>span').on('click', function (evt) {
                coordinates.push(Number(evt.currentTarget.classList[0].split('-')[1]));
                focusNodes.push(tempExp.children ? _.cloneDeep(tempExp.children[coordinates[coordinates.length - 1]]) :
                    _.cloneDeep(tempExp.parameters[coordinates[coordinates.length - 1]]));
                renderExpression();
            })

            $(expressionContainer).find('.expression.target>.expression-merge').on('click', function (evt) {
                if (coordinates.length === 1) {
                    expression.children[coordinates[0]] = focusNodes[0];
                } else {
                    var childNode = focusNodes[focusNodes.length - 1];
                    var parentNode = focusNodes[focusNodes.length - 2];
                    var replacingIndex = coordinates[coordinates.length - 1];

                    if (parentNode.children) {
                        parentNode.children[replacingIndex] = childNode;
                    } else {
                        parentNode.parameters[replacingIndex] = childNode;
                    }
                }
                focusNodes.pop();
                coordinates.pop();
                renderExpression();
            });


            // render supported attributes based off expression context

            //generate the supported attributes
            var supportedInputAttributes = {};
            var supportedFunctions = {};
            var supportedOperators = {};

            console.log(functionDataMap)

            if (tempExp.children) {
                if (tempExp.children.length > 0) {
                    switch (tempExp.children[tempExp.children.length - 1].nodeType) {
                        case 'attribute':
                            var attributeGenericDataType = tempExp.children[tempExp.children.length - 1].genericDataType;

                            Object.keys(operatorMap).forEach(function (key) {
                                if (operatorMap[key].beforeTypes.indexOf(attributeGenericDataType) > -1
                                    && _.intersection(operatorMap[key].returnTypes, tempExp.supportedGenericDataTypes).length > 0) {
                                    supportedOperators[key] = operatorMap[key];
                                }
                            })
                            break;
                        case 'function':
                        case 'scope':
                            Object.keys(operatorMap).forEach(function (key) {
                                if (_.intersection(operatorMap[key].beforeTypes, tempExp.children[tempExp.children.length - 1].supportedGenericDataTypes).length > 0 &&
                                    _.intersection(operatorMap[key].returnTypes, tempExp.supportedGenericDataTypes).length > 0) {
                                    supportedOperators[key] = operatorMap[key];
                                }
                            })
                            break;
                        case 'operator':
                            var dataTypesFollowingOperator = tempExp.children[tempExp.children.length - 1].afterTypes;

                            Object.keys(functionDataMap).forEach(function (key) {
                                if (dataTypesFollowingOperator.indexOf(getGenericDataType(key)) > -1) {
                                    supportedInputAttributes = _.merge({}, supportedInputAttributes, functionDataMap[key]['attribute'])
                                    supportedFunctions = _.merge({}, supportedFunctions, functionDataMap[key]['function'])
                                }
                            })

                            supportedOperators = {
                                bracket: {
                                    returnTypes: ['bool', 'text', 'number'],
                                    beforeTypes: ['bool', 'text', 'number'],
                                    afterTypes: ['bool', 'text', 'number'],
                                    symbol: '()',
                                    description: 'Bracket',
                                    isFirst: true,
                                    scope: true
                                }
                            }

                            break;
                    }

                } else {
                    if (tempExp.dataTypes.indexOf('bool') > -1) {
                        this.inputAttributes.forEach(function (att) {
                            supportedInputAttributes[att.name] = att;
                        })
                    }

                    tempExp.dataTypes.forEach(function (dataType) {
                        supportedInputAttributes = functionDataMap[dataType]['attribute'] ?
                            _.merge({}, supportedInputAttributes, functionDataMap[dataType]['attribute']) :
                            supportedInputAttributes;

                        supportedFunctions = functionDataMap[dataType]['function'] ?
                            _.merge({}, supportedFunctions, functionDataMap[dataType]['function']) :
                            supportedFunctions;

                        supportedOperators = {
                            bracket: {
                                returnTypes: ['bool', 'text', 'number'],
                                beforeTypes: ['bool', 'text', 'number'],
                                afterTypes: ['bool', 'text', 'number'],
                                symbol: '()',
                                description: 'Bracket',
                                isFirst: true,
                                scope: true
                            }
                        }

                        Object.keys(operatorMap).forEach(function (key) {
                            if ((!supportedOperators[key]) && operatorMap[key].isFirst && operatorMap[key].returnTypes.indexOf(getGenericDataType(dataType)) > -1) {
                                supportedOperators[key] = operatorMap[key]
                            }
                        })
                    });


                }
            }

            // render attribute selector
            var nodeCategoryContainer = $(container).find('.node-category');
            nodeCategoryContainer.empty();
            var attributeContainer = $(container).find('.att-fun-op-container');
            var nodeData;


            Object.keys(supportedInputAttributes).length > 0 ?
                nodeCategoryContainer.append(`
                    <li>
                        <a>
                             <div class="attribute-category">
                                 Attribute
                             </div>
                         </a>
                     </li>
                `)
                : null;

            Object.keys(supportedFunctions).length > 0 ?
                nodeCategoryContainer.append(`
                    <li>
                        <a>
                             <div class="function-category">
                                 Function
                             </div>
                         </a>
                     </li>
                `)
                : null;

            Object.keys(supportedOperators).length > 0 ?
                nodeCategoryContainer.append(`
                    <li>
                        <a>
                             <div class="operator-category">
                                 Operators
                             </div>
                         </a>
                     </li>
                `)
                : null;

            // setup events for attribute selection
            $(nodeCategoryContainer).find('.attribute-category').on('click', function (evt) {
                nodeCategoryContainer.find('li>a>div').removeClass('selected');
                nodeCategoryContainer.find('.attribute-category').addClass('selected');

                attributeContainer.find('.select-function-operator-attrib').show();
                attributeContainer.find('.attrib-selector-containers').empty();

                Object.keys(supportedInputAttributes).forEach(function (key) {
                    attributeContainer.find('.attrib-selector-containers').append(`
                        <a id="attr-${supportedInputAttributes[key].name}" style="color: #333">
                            <div class="attribute" style="">
                                <div>
                                    ${supportedInputAttributes[key].name}
                                </div>
                                <div class="description" style="">
                                    ${supportedInputAttributes[key].type}
                                </div>
                           </div>
                        </a>
                    `);
                });

                attributeContainer.find('.attrib-selector-containers').children().on('click', function (evt) {
                    nodeData = {
                        name: supportedInputAttributes[evt.currentTarget.id.split('attr-')[1]].name,
                        dataType: supportedInputAttributes[evt.currentTarget.id.split('attr-')[1]].type,
                    }

                    tempExp.addNodeToExpression(new AttributeNode(nodeData));
                    updateExpression(tempExp);
                })
            })

            $(nodeCategoryContainer).find('.function-category').on('click', function (evt) {
                nodeCategoryContainer.find('li>a>div').removeClass('selected');
                nodeCategoryContainer.find('.function-category').addClass('selected');
                attributeContainer.find('.select-function-operator-attrib').show();
                attributeContainer.find('.attrib-selector-containers').empty();

                Object.keys(supportedFunctions).forEach(function (key) {
                    attributeContainer.find('.attrib-selector-containers').append(`
                        <a id="func-${supportedFunctions[key].name}" style="color: #333">
                            <div class="attribute" style="">
                                <div>
                                    ${supportedFunctions[key].displayName}
                                </div>
                                <div class="description" style="">
                                    ${supportedFunctions[key].description}
                                </div>
                           </div>
                        </a>
                    `);
                });

                attributeContainer.find('.attrib-selector-containers').children().on('click', function (evt) {
                    attributeContainer.find('.select-function-operator-attrib').hide();
                    attributeContainer.find('.select-function-format-container').show();

                    supportedFunctions[evt.currentTarget.id.split('func-')[1]].syntax.forEach(function (syntax, i) {
                        attributeContainer.find('.select-function-format-container').find('ul').append(`
                            <li id="syntax-${i}">
                                <a style="">
                                    <div class="function-syntax" style="">
                                        <div class="syntax-expression">
                                            ${syntax.syntax}
                                        </div>
                                    </div>
                                </a>
                            </li>
                        `);
                    });

                    attributeContainer.find('.select-function-format-container').find('ul').children().on('click', function (child_evt) {
                        nodeData = {
                            displayName: supportedFunctions[evt.currentTarget.id.split('func-')[1]].displayName,
                            dataTypes: supportedFunctions[evt.currentTarget.id.split('func-')[1]].returnAttributes[0].type.map(function (dataType) {
                                return dataType.toLowerCase();
                            }),
                            selectedSyntax: supportedFunctions[evt.currentTarget.id.split('func-')[1]].syntax[child_evt.currentTarget.id.split('syntax-')[1]]
                        };

                        tempExp.addNodeToExpression(new FunctionNode(nodeData));
                        updateExpression(tempExp);
                        attributeContainer.find('.select-function-format-container').find('ul').empty();
                        attributeContainer.find('.select-function-format-container').hide();
                    })
                })
            })

            $(nodeCategoryContainer).find('.operator-category').on('click', function (evt) {
                nodeCategoryContainer.find('li>a>div').removeClass('selected');
                nodeCategoryContainer.find('.operator-category').addClass('selected');
                attributeContainer.find('.select-function-operator-attrib').show();
                attributeContainer.find('.attrib-selector-containers').empty();

                Object.keys(supportedOperators).forEach(function (key) {
                    attributeContainer.find('.attrib-selector-containers').append(`
                        <a id="operator-${key}" style="color: #333">
                            <div class="attribute" style="">
                                <div>
                                    ${supportedOperators[key].symbol} - ${supportedOperators[key].description}
                                </div>
                           </div>
                        </a>
                    `);
                });

                attributeContainer.find('.attrib-selector-containers').children().on('click', function (evt) {
                    if (evt.currentTarget.id.split('operator-')[1] === 'bracket') {
                        tempExp.addNodeToExpression(new ScopeNode(tempExp.dataTypes));
                    } else {
                        nodeData = {
                            symbol: supportedOperators[evt.currentTarget.id.split('operator-')[1]].symbol,
                            dataTypes: supportedOperators[evt.currentTarget.id.split('operator-')[1]].returnTypes,
                            isEnd: supportedOperators[evt.currentTarget.id.split('operator-')[1]].isEnd,
                            afterTypes: supportedOperators[evt.currentTarget.id.split('operator-')[1]].afterTypes,
                            beforeTypes: supportedOperators[evt.currentTarget.id.split('operator-')[1]].beforeTypes,
                        }

                        tempExp.addNodeToExpression(new OperatorNode(nodeData));
                    }

                    updateExpression(tempExp);
                })

            })

            $(nodeCategoryContainer).children().first().find('a>div').click();
        }

        ETLTaskView.prototype.updateExpression = function (expression) {
            if (this.coordinate.length === 0) {
                this.expressionMap[this.currenOutputElement] = expression;
            }

            this.renderGenerator();
        }

        ETLTaskView.prototype.hideExpressionGenerationDialog = function (container, expressionGeneratorContainer) {
            expressionGeneratorContainer.remove();
            expressionGeneratorContainer = $(container).find('.popup-backdrop').clone();
            $(container).prepend(expressionGeneratorContainer);
            this.expressionGenerationDialog = expressionGeneratorContainer;
            this.currenOutputElement = null;
            this.coordinate = [];
            this.focusNode = [];
        }

        ETLTaskView.prototype.renderFunctionAttributeSelector = function (type, attributeFunctionArray, outputAttributeName) {
            var nodeCategoryContainer = this.expressionGenerationDialog.find('.att-fun-op-container');
            var addNodeToExpression = this.addNodeToExpression;
            var attributeContainer = $(nodeCategoryContainer).find('.select-function-operator-attrib');
            var syntaxSelectorContainer = $(nodeCategoryContainer).find('.select-function-format-container');

            $(attributeContainer).find('.attrib-selector-containers').children().remove();

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
                        element.syntax.forEach(function (syntax_obj) {
                            $(syntaxSelectorContainer).find('ul').append(`
                                <li>
                                    <a style="">
                                        <div class="function-syntax" style="">
                                            <div class="syntax-expression">
                                                ${syntax_obj.syntax}
                                            </div>
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
            this.genericDataTypes = node_data.dataTypes;
            this.nodeType = 'operator';
            this.afterTypes = node_data.afterTypes;
            this.beforeTypes = node_data.beforeTypes;
            // this.genericDataType = getGenericDataType(node_data.dataType);
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
            this.dataTypes = node_data.dataTypes;
            this.supportedGenericDataTypes = node_data.dataTypes.map(function (data_type) {
                return getGenericDataType(data_type);
            });
            this.nodeType = 'function';
            this.parameters = this.generateParameters(node_data.selectedSyntax);
        }

        FunctionNode.prototype.generateParameters = function (syntax) {
            var parameters = [];
            var regExp = /\(([^)]+)\)/;

            // TODO : write parameter generation from selected syntax
            regExp.exec(syntax.syntax) ? regExp.exec(syntax.syntax)[1].split(',').forEach(function (param) {
                var temp = param.trim().split(' ');

                var dataTypes = temp[0].match(/<(.*?)>/)[1].split('|').map(function (type) {
                    return type.toLowerCase();
                });

                var placeHolder = '<';
                var isFirst = true;
                dataTypes.forEach(function (dataType) {
                    if (!isFirst) {
                        placeHolder += ' | '
                    }
                    placeHolder += dataType;
                })
                placeHolder += '>';
                placeHolder += ' ' + temp[1];

                var paramNode = new ScopeNode(dataTypes);
                paramNode.placeholder = placeHolder;

                parameters.push(paramNode);

            }) : null;

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
            this.placeholder = null;
        }

        ScopeNode.prototype.addNodeToExpression = function (node) {
            this.children.push(node);
        }

        function addValueToArray(level, coordinates, replacementChildNode, node) {
            if ((level === 1 && coordinates.length === 1) || level === 1) {
                if (node.children) {
                    node.children[coordinates[coordinates.length - level]] = replacementChildNode;
                } else {
                    node.parameters[coordinates[coordinates.length] - level] = replacementChildNode;
                }

                return node;
            }

            return addValueToArray(level - 1, coordinates,
                replacementChildNode, node.children ?
                    node.children[coordinates[coordinates.length - level]]
                    : node.parameters[coordinates[coordinates.length - level]]);
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
                            htmlContent += childNode.value;
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
                            htmlContent += `<span class="item-${i} ${highlightIndex != null ? (highlightIndex === i ? 'selected' : '') : ''}">(${generateExpressionHTML(null, childNode)})</span>`;
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
                            htmlContent += `<span title="${parameterNode.placeholder}" class="param-${i} ${highlightIndex != null ? (highlightIndex === i ? 'selected' : '') : ''}">${generateExpressionHTML(null, parameterNode)}</span>`;
                        }
                        isFirst = false;
                        i++;
                    })
                    htmlContent += `)`;

                }
            }


            return htmlContent.length === 0 ? '...' : htmlContent;
        }

        function validateExpressionTree(expression) {
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

        return ETLTaskView;
    })
;
