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

define(['require', 'log', 'lodash', 'jquery', 'appData', 'initialiseData', 'jsonValidator',
        'app/source-editor/completion-engine', 'alerts', 'dataMapperUtil', 'scopeModel', 'functionModel',
        'operatorModel', 'attributeModel', 'customValueModel'],
    function(require, log, _, $, AppData, InitialiseData, JSONValidator, CompletionEngine, alerts, DataMapperUtil,
        ScopeModel, FunctionModel, OperatorModel, AttributeModel, CustomValueModel) {

        var DataMapper = function(container, config) {
            container.show();
            container.css({'width': '100%'});
            this.inputAttributes = config.input.stream.attributes;
            this.outputAttributes = config.output.stream.attributes;

            var color = 'gray';

            this.jsPlumbInstance = window.j = jsPlumb.getInstance({
                Connector: ["Straight", { curviness: 50 }],
                DragOptions: { cursor: "pointer", zIndex: 2000 },
                PaintStyle: { stroke: color, strokeWidth: 2 },
                EndpointStyle: { radius: 3, fill: 'rgba(0, 0, 0, 0)' },
                endpointHoverStyle: { fill: 'rgba(0, 0, 0, 0)' },
                HoverPaintStyle: { stroke: "#ec9f2e" },
                EndpointHoverStyle: { fill: "#ec9f2e" },
                Container: $(container).find('.etl-task-wizard-canvas')
            })
            this.container = container;
            this.inputAttributeEndpoints = {};
            this.config = config;
            this.outputAttributeEndpoints = {};
            this.expressionMap = config.query.mapping;
            this.coordinate = [];
            this.focusNode = [];
            this.selectedCategory = null;
            this.selectedCategoryFilter = '';
            this.currenOutputElement = null;
            this.expressionGenerationDialog = $(container).find('.popup-backdrop').clone();
            $(container).prepend(this.expressionGenerationDialog);

            this.isInputOutputIdentical = _.isEqual(this.inputAttributes, this.outputAttributes);

            this.inputListContainer =
                $(container).find('.etl-task-wizard-canvas').find('.inputs').find('.attributeList');
            this.outputListContainer =
                $(container).find('.etl-task-wizard-canvas').find('.outputs').find('.attributeList')

            //function binding
            this.showExpressionDialog = this.showExpressionDialog.bind(this);
            this.renderAttributes = this.renderAttributes.bind(this);
            this.renderFunctionAttributeSelector = this.renderFunctionAttributeSelector.bind(this);
            this.hideExpressionGenerationDialog = this.hideExpressionGenerationDialog.bind(this);
            this.addNodeToExpression = this.addNodeToExpression.bind(this);
            this.displayExpression = this.displayExpression.bind(this);
            this.renderGenerator = this.renderGenerator.bind(this);
            this.updateFilter = this.updateFilter.bind(this);
            this.updateExpression = this.updateExpression.bind(this);
            this.addCoordinate = this.addCoordinate.bind(this);
            this.removeCoordinate = this.removeCoordinate.bind(this);
            this.updateConnections = this.updateConnections.bind(this);
            this.displayCustomValueInput = this.displayCustomValueInput.bind(this);

            this.renderAttributes(this.inputAttributes, this.outputAttributes);
            this.functionDataMap = this.generateExpressionMap(this.inputAttributes, CompletionEngine.getRawMetadata());
        }

        DataMapper.prototype.updateConnections = function(outputAttribute) {
            var jsPlumbInstance = this.jsPlumbInstance;
            var inputAttributeEndpoints = this.inputAttributeEndpoints;
            var outputAttributeEndpoints = this.outputAttributeEndpoints;
            var generatedExpression = this.expressionMap[outputAttribute]
            && typeof this.expressionMap[outputAttribute] !== 'string' ?
                '= ' + DataMapperUtil.generateExpressionHTML(this.expressionMap[outputAttribute], '', null)
                : this.expressionMap[outputAttribute] ? `= ${this.expressionMap[outputAttribute]}` : '';

            $(outputAttributeEndpoints[outputAttribute].element).find('.mapped-expression').empty();
            $(outputAttributeEndpoints[outputAttribute].element).find('.mapped-expression').append(`
                ${generatedExpression}
            `);

            this.inputAttributes.forEach(function(inputAttribute) {
                var divElement = document.createElement('div');
                divElement.innerHTML = generatedExpression;

                if (divElement.innerText.replace('\"', '\'')
                        .match(`(?<=^([^']|'[^']*')*)${inputAttribute.name}`)) {
                    jsPlumbInstance.connect({
                        source: inputAttributeEndpoints[inputAttribute.name],
                        target: outputAttributeEndpoints[outputAttribute]
                    })
                }
            })

        }

        DataMapper.prototype.renderAttributes = function(inputAttributes, outputAttributes) {
            var container = this.container;
            var inputListContainer = this.inputListContainer;
            var outputListContainer = this.outputListContainer;
            var jsPlumbInstance = this.jsPlumbInstance;
            var inputEndpointMap = {};
            var outputEndpointMap = {};
            var showExpressionDialog = this.showExpressionDialog;
            var expressionMap = this.expressionMap;
            var updateConnections = this.updateConnections;

            inputAttributes.forEach(function(element) {
                var inputAttribElement = inputListContainer.append(`
                    <li>
                        <div class="attribute" style="">
                            <div>
                                ${element.name}
                            </div>
                            <div class="attrib-type" style="">
                                (${element.type})
                            </div>
                        </div>
                    </li>
                `);

                inputEndpointMap[element.name] = jsPlumbInstance.addEndpoint(
                    $(inputAttribElement).children().last(),
                    { anchor: 'Right' },
                    { isSource: true, maxConnections: -1 }
                );
            });

            outputAttributes.forEach(function(element) {
                var outputAttribElement = outputListContainer.append(`
                    <li>
                        <div class="attribute" style="">
                            <div style="display: flex; flex-wrap: wrap; width: 90%;">
                                <div style="display: flex; width: 100%;">
                                    <div>
                                        ${element.name}
                                    </div>
                                    <div class="attrib-type" style="opacity: 0.8">
                                        (${element.type})
                                    </div>
                                </div>
                                <div class="mapped-expression" style="">
                                    <!--     Generated expression goes here     -->
                                </div>
                            </div>
                            <div class="clear-icon">
                                <a href="#" title="Clear mapping" href="#" class="icon clear" style="">
                                    <i style="color:#323232" class="fw fw-clear"></i>
                                </a>
                            </div>
                        </div>
                    </li>
                `);

                outputAttribElement.children().last().on('click', function(evt) {
                    evt.stopPropagation()
                    showExpressionDialog(element);
                });

                outputAttribElement.children().last().find('.clear-icon').on('click', function(evt) {
                    evt.stopPropagation();
                    delete expressionMap[element.name];
                    jsPlumbInstance.deleteConnectionsForElement(outputEndpointMap[element.name].element);
                    updateConnections(element.name);
                });

                outputEndpointMap[element.name] = jsPlumbInstance.addEndpoint(
                    $(outputAttribElement).children().last(),
                    { anchor: 'Left' },
                    {
                        isTarget: true,
                        maxConnections: -1
                    }
                );
            });

            if (this.isInputOutputIdentical) {
                var select_all = $(container).find('.btn-select-all');

                select_all.show();
                select_all.on('click', function(evt) {
                    outputAttributes.forEach(function(attr) {
                        expressionMap[attr.name] = new ScopeModel([attr.type]);
                        expressionMap[attr.name].addNode(new AttributeModel({
                            name: attr.name,
                            dataType: attr.type
                        }));
                        updateConnections(attr.name);

                        select_all.hide();
                    });
                });
            }

            this.inputAttributeEndpoints = inputEndpointMap;
            this.outputAttributeEndpoints = outputEndpointMap;
            outputAttributes.forEach(function (element) {
                if (expressionMap[element.name]) {
                    updateConnections(element.name);
                }
            });
        }

        DataMapper.prototype.showExpressionDialog = function(output_attribute) {
            this.currenOutputElement = output_attribute.name;
            this.expressionMap[output_attribute.name] = this.expressionMap[output_attribute.name] ?
                this.expressionMap[output_attribute.name] : new ScopeModel([output_attribute.type]);
            var container = this.container;
            var hideExpressionGenerationDialog = this.hideExpressionGenerationDialog;
            var expressionGeneratorContainer = this.expressionGenerationDialog.show();
            var coordinates = this.coordinate;
            var expressionMap = this.expressionMap;
            var expression = this.expressionMap[output_attribute.name];
            var initialExpression = _.cloneDeep(this.expressionMap[output_attribute.name])
            var updateConnections = this.updateConnections;

            this.renderGenerator();

            $(expressionGeneratorContainer).find('.btn-default').on('click', function() {
                expressionMap[output_attribute.name] = initialExpression;
                hideExpressionGenerationDialog(container, expressionGeneratorContainer);
            });

            $(expressionGeneratorContainer).find('.btn-expression-submit').on('click', function() {
                if (coordinates.length > 0) {
                    alerts.error('Please complete the expression creation process to submit');
                } else {
                    var isExpressionValid = validateExpressionTree(expression);

                    if (isExpressionValid) {
                        $(container).find('.btn-select-all').hide();
                        updateConnections(output_attribute.name);
                        hideExpressionGenerationDialog(container, expressionGeneratorContainer);
                    } else {
                        alerts.error('Please complete the expression creation process to submit');
                    }
                }
            });
        }

        DataMapper.prototype.addCoordinate = function(index) {
            this.coordinate.push(index);
        }

        DataMapper.prototype.removeCoordinate = function() {
            this.coordinate.pop();
        }

        DataMapper.prototype.renderGenerator = function() {
            var config = this.config;
            var container = this.container;
            var expressionContainer = $(container).find('.expression-container');
            var expression = this.expressionMap[this.currenOutputElement];
            var coordinates = this.coordinate;
            var focusNodes = this.focusNode;
            var functionDataMap = this.functionDataMap;
            var updateExpression = this.updateExpression;
            var renderExpression = this.renderGenerator;
            var displayCustomValueInput = this.displayCustomValueInput;
            var selectedCategory = this.selectedCategory;
            var selectedFilter = this.selectedCategoryFilter;
            var updateFilter = this.updateFilter;

            $(this.container).find('.dialog-heading').text('');
            $(this.container).find('.dialog-heading')
                .append(`Create expression for 
                            <b>${this.currenOutputElement}</b> of <b>${config.output.stream.name}</b>`);

            // render the main Expression
            expressionContainer.empty();
            var tempExp = expression;

            if (coordinates.length === 0) {
                // render the expression if none of the expression nodes are selected
                expressionContainer.append(`
                    <div class="expression target" style="display: flex">
                        <div class="exp-content" style="width: 100%;">
                        <i style="color: #808080">${config.output.stream.name}.${this.currenOutputElement} = </i>
                            ${
                                typeof expression !== 'string' ?
                                    DataMapperUtil.generateExpressionHTML(expression, '', null)
                                    : expression
                            }
                        </div>
                    </div>
                `);
            } else {
                expressionContainer.append(`
                    <div class="expression" style="">
                        <i style="color: #808080">expression: </i>
                        ${DataMapperUtil.generateExpressionHTML(expression, '', coordinates[0])}
                    </div>
                `);
            }

            // render expression when one attribute/function/scope is selected in drill down form
            coordinates.forEach(function(index, i) {
                tempExp = focusNodes[i];

                if (i === (coordinates.length - 1)) {
                    expressionContainer.append(`
                      <div class="expression target" style="display: flex; flex-wrap: wrap">
                          <div class="exp-content">
                              ${DataMapperUtil.generateExpressionHTML(tempExp, '', null)}
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
                      <div class="expression">
                          ${DataMapperUtil.generateExpressionHTML(tempExp, '', coordinates[i])}
                      </div>
                    `);
                }
            });



            $(expressionContainer).find('.expression.target>.exp-content>span.ok-clear').popover({
                html: true,
                content: function() {
                    return $(container).find('.popover-content').html();
                },
                template: '<div class="popover" role="tooltip"><div class="arrow"></div>' +
                    '<div class="popover-content"></div></div>',
                placement: 'top',
            });

            $(expressionContainer).find('.expression.target>.exp-content>span.ok-clear')
                .on('mouseenter', function(evt) {
                    $(evt.currentTarget).popover('show');
                    $(container).find(`#${$(evt.currentTarget).attr('aria-describedby')}`).on('click', function(e) {
                        e.stopPropagation();
                        var index = evt.currentTarget.id.split('-');
                        tempExp.parameters[Number(index[index.length - 1])].rootNode = null;
                        updateExpression(tempExp);
                    })
                    $(container).find('.popover').on('mouseleave', function() {
                        $(evt.currentTarget).popover('hide');
                    });
                })
                .on('mouseleave', function(evt) {
                    setTimeout(function() {
                        if (!($(expressionContainer).find('.popover:hover').length > 0)) {
                            $(evt.currentTarget).popover('hide');
                        }
                    }, 300);
                });

            $(expressionContainer).find('.expression.target>.exp-content>span').on('click', function(evt) {
                var pathIndex = evt.currentTarget.id.match('item-([a-zA-Z0-9\-]+)')[1];
                var pathArray = pathIndex.split('-')
                coordinates.push(pathIndex);

                var addToFocusNode = function(exp, pathArray) {
                    if(pathArray.length === 1) {
                        if(pathArray[0] === 'n') {
                            focusNodes.push(_.cloneDeep(exp.rootNode));
                        } else if(pathArray[0] === 'l') {
                            focusNodes.push(_.cloneDeep(exp.leftNode));
                        } else if(pathArray[0] === 'r') {
                            focusNodes.push(_.cloneDeep(exp.rightNode));
                        } else {
                            focusNodes.push(_.cloneDeep(exp.parameters[pathArray[0]]));
                        }
                    } else {
                        var next = pathArray.splice(0, 1);
                        if(next[0] === 'n') {
                            addToFocusNode(exp.rootNode, pathArray);
                        } else if(next[0] === 'l') {
                            addToFocusNode(exp.leftNode, pathArray);
                        } else if(next[0] === 'r') {
                            addToFocusNode(exp.rightNode, pathArray);
                        } else {
                            addToFocusNode(exp.parameters[Number(next)], pathArray);
                        }
                    }
                }

                addToFocusNode(tempExp, pathArray);
                renderExpression();
            });

            if (coordinates.length > 0 && tempExp.type === 'function') {
                $(expressionContainer).find('.target .add-param').show();
                $(expressionContainer).find('.target .add-param').off('click');
                $(expressionContainer).find('.target .add-param').on('click', function(evt) {
                    evt.stopPropagation();
                    tempExp.parameters.push(new ScopeModel(tempExp.repetitiveParameterTypes));
                    renderExpression();
                });
            }

            $(expressionContainer).find('.expression.target>.expression-merge').on('click', function(evt) {
                var path = coordinates[coordinates.length - 1].split('-');

                var replaceInExpression = function (exp, pathArray) {
                    if (pathArray.length === 1) {
                        if (pathArray[0] === 'n') {
                            exp.rootNode = focusNodes[focusNodes.length - 1];
                        } else if (pathArray[0] === 'l') {
                            exp.leftNode = focusNodes[focusNodes.length - 1];
                        } else if(pathArray[0] === 'r') {
                            exp.rightNode = focusNodes[focusNodes.length - 1];
                        } else {
                            exp.parameters[pathArray[0]] = focusNodes[focusNodes.length - 1];
                        }
                    } else {
                        var next = pathArray.splice(0, 1);

                        if (next[0] === 'n') {
                            replaceInExpression(exp.rootNode, pathArray);
                        } else if (next[0] === 'l') {
                            replaceInExpression(exp.leftNode, pathArray);
                        } else if(next[0] === 'r') {
                            replaceInExpression(exp.rightNode, pathArray);
                        } else {
                            replaceInExpression(exp.parameters[Number(next[0])], pathArray);
                        }
                    }
                }

                replaceInExpression(focusNodes.length > 1 ? focusNodes[focusNodes.length - 2] : expression, path);

                focusNodes.pop();
                coordinates.pop();
                renderExpression();
            });


            // render supported attributes based off expression context

            //generate the supported attributes
            var supportedInputAttributes = {};
            var supportedFunctions = {};
            var supportedOperators = {};

            if (typeof tempExp !== 'string') {
                if (tempExp.rootNode) {
                    switch (tempExp.rootNode.type) {
                        case 'function':
                        case 'attribute':
                        case 'customValue':
                        case 'scope':
                            Object.keys(DataMapperUtil.OperatorMap)
                                .filter(function (key) {
                                    return DataMapperUtil.OperatorMap[key].hasLeft
                                        && _.intersection(DataMapperUtil.OperatorMap[key].leftTypes,
                                            tempExp.rootNode.genericReturnTypes).length > 0
                                        && _.intersection(DataMapperUtil.OperatorMap[key].returnTypes,
                                            tempExp.genericReturnTypes).length > 0
                                })
                                .forEach(function (key) {
                                    supportedOperators[key] = DataMapperUtil.OperatorMap[key]
                                });
                            break;
                        case 'operator':
                            if (tempExp.rootNode.hasRight && !tempExp.rootNode.rightNode) {
                                config.input.stream.attributes
                                    .filter(function (attr) {
                                        return tempExp.rootNode.rightTypes
                                            .indexOf(DataMapperUtil.getGenericDataType(attr.type)) > -1;
                                    })
                                    .forEach(function (attr) {
                                        supportedInputAttributes[attr.name] = attr;
                                    });

                                supportedInputAttributes['$custom_val_properties'] = {
                                    genericDataTypes: tempExp.rootNode.rightTypes,
                                };

                                Object.keys(functionDataMap).forEach(function (key) {
                                    var dataTypes = [];

                                    if (tempExp.rootNode.genericReturnTypes
                                        .indexOf(DataMapperUtil.getGenericDataType(key)) > -1) {
                                        dataTypes.push(key);
                                        supportedFunctions = _.merge({}, supportedFunctions,
                                            functionDataMap[key]['function'])
                                    }
                                });

                                if (tempExp.rootNode.type !== 'scope') {
                                    supportedOperators = {
                                        bracket: {
                                            returnTypes: ['bool', 'text', 'number'],
                                            leftTypes: ['bool', 'text', 'number'],
                                            rightTypes: ['bool', 'text', 'number'],
                                            symbol: '()',
                                            description: 'Bracket',
                                            isFirst: true,
                                            scope: true
                                        }
                                    };
                                }
                            } else {
                                Object.keys(DataMapperUtil.OperatorMap)
                                    .filter(function (key) {
                                        return _.intersection(DataMapperUtil.OperatorMap[key].leftTypes,
                                            tempExp.rootNode.genericReturnTypes).length > 0
                                            && _.intersection(DataMapperUtil.OperatorMap[key].returnTypes,
                                                tempExp.genericReturnTypes).length > 0;
                                    })
                                    .forEach(function (key) {
                                        supportedOperators[key] = DataMapperUtil.OperatorMap[key]
                                    });
                            }

                            break;
                    }
                } else {

                    var customDataTypes = []
                    if (tempExp.returnTypes.indexOf('bool') > -1) {
                        config.input.stream.attributes
                            .forEach(function (attr) {
                                supportedInputAttributes[attr.name] = attr;
                            });
                        customDataTypes = ['text', 'number', 'bool'];

                    } else {
                        config.input.stream.attributes
                            .filter(function (attr) {
                                return tempExp.returnTypes.indexOf(attr.type) > -1;
                            }).forEach(function (attr) {
                            supportedInputAttributes[attr.name] = attr;
                        });

                        tempExp.returnTypes.forEach(function (type) {
                            customDataTypes.push(DataMapperUtil.getGenericDataType(type));
                            supportedFunctions = functionDataMap[type.toLowerCase()]['function'] ?
                                _.merge({}, supportedFunctions, functionDataMap[type.toLowerCase()]['function']) :
                                supportedFunctions;
                        })
                    }

                    supportedInputAttributes['$custom_val_properties'] = {
                        genericDataTypes: customDataTypes
                    };

                    supportedOperators['bracket'] = {
                        returnTypes: ['bool', 'text', 'number'],
                        leftTypes: ['bool', 'text', 'number'],
                        rightTypes: ['bool', 'text', 'number'],
                        symbol: '()',
                        description: 'Bracket',
                        isFirst: true,
                        scope: true
                    }

                    Object.keys(DataMapperUtil.OperatorMap)
                        .filter(function (key) {
                            return DataMapperUtil.OperatorMap[key].isFirst &&
                                _.intersection(DataMapperUtil.OperatorMap[key].returnTypes,
                                    tempExp.returnTypes).length > 0;
                        })
                        .forEach(function (key) {
                            supportedOperators[key] = DataMapperUtil.OperatorMap[key];
                        });
                }

                if (tempExp.type === 'function') {
                    supportedOperators = {};
                    supportedFunctions = {};
                    supportedInputAttributes = {};
                }
            }


            if (selectedCategory === 'Attribute') {
                Object.keys(supportedInputAttributes).forEach(function(key) {
                    if (!(supportedInputAttributes[key].name.toLowerCase().indexOf(selectedFilter) > -1)) {
                        delete supportedInputAttributes[key];
                    }
                });
            }

            if (selectedCategory === 'Function') {
                Object.keys(supportedFunctions).forEach(function(key) {
                    if (!(supportedFunctions[key].displayName.toLowerCase().indexOf(selectedFilter) > -1)) {
                        delete supportedFunctions[key];
                    }
                });
            }

            if (selectedCategory === 'Operator') {
                Object.keys(supportedOperators).forEach(function(key) {
                    if (!(supportedOperators[key].description.toLowerCase().indexOf(selectedFilter) > -1)) {
                            delete supportedOperators[key];
                    }
                });
            }

            // render attribute selector
            var nodeCategoryContainer = $(container).find('.node-category');
            nodeCategoryContainer.empty();
            var attributeContainer = $(container).find('.att-fun-op-container');
            attributeContainer.find('.select-function-format-container').hide();
            attributeContainer.hide();
            var nodeData;


            if (Object.keys(supportedInputAttributes).length > 0) {
                attributeContainer.show();
                nodeCategoryContainer.append(`
                    <li>
                        <a>
                             <div class="attribute-category">
                                 Input Attributes
                             </div>
                         </a>
                     </li>
                `);
            }

            if (Object.keys(supportedFunctions).length > 0) {
                attributeContainer.show();
                nodeCategoryContainer.append(`
                    <li>
                        <a>
                             <div class="function-category">
                                 Function
                             </div>
                         </a>
                     </li>
                `);
            }

            if (Object.keys(supportedOperators).length > 0) {
                attributeContainer.show();
                nodeCategoryContainer.append(`
                    <li>
                        <a>
                             <div class="operator-category">
                                 Operators
                             </div>
                         </a>
                     </li>
                `);
            }

            // setup events for attribute selection
            $(nodeCategoryContainer).find('.attribute-category').on('click', function(evt) {
                nodeCategoryContainer.find('li>a>div').removeClass('selected');
                nodeCategoryContainer.find('.attribute-category').addClass('selected');

                attributeContainer.find('.select-function-operator-attrib').show();
                attributeContainer.find('.attrib-selector-containers').empty();

                Object.keys(supportedInputAttributes).forEach(function(key) {
                    if (key !== '$custom_val_properties') {
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
                    } else {
                        attributeContainer.find('.attrib-selector-containers').append(`
                            <a id="custom_val_input_container" style="color: #333">
                                <div class="attribute" style="display: flex; flex-wrap: wrap;">
                                    <div class="description" style="width: 100%;">
                                       Add constant to the expression
                                    </div>
                                    <div style="width: 100%;">
                                        Custom Value
                                        <select name="" id="custom_val_type">
                                        </select>
                                        <input style="display: none; width: 65%" id="custom_value_input_txt" type="text">
                                        <select style="display: none; width: 65%" id="custom_value_input_bool" >
                                            <option value="true">True</option>
                                            <option value="false">false</option>
                                        </select>
                                        <button style="background-color: #007eff; padding: 0 6px 0 6px;" 
                                            class="btn btn-primary btn-custom-val-submit">Add</button>
                                    </div>
                                </div>
                            </a>
                        `);

                        supportedInputAttributes[key].genericDataTypes.forEach(function(type) {
                            var customOptionList = attributeContainer.find('.attrib-selector-containers').find('#custom_val_type')

                            switch (type) {
                                case 'text':
                                    customOptionList.append(
                                        `<option>string</option>`
                                    );
                                    break;
                                case 'number':
                                    customOptionList.append(`
                                        <option>int</option>
                                        <option>long</option>
                                        <option>float</option>
                                        <option>double</option>
                                    `);
                                    break;
                                case 'bool':
                                    customOptionList.append(`
                                        <option>bool</option>
                                    `);
                                    break;
                            }
                        });
                    }

                });

                attributeContainer.find('.attrib-selector-containers').children().on('click', function(evt) {
                    nodeData = {
                        name: supportedInputAttributes[evt.currentTarget.id.split('attr-')[1]].name,
                        dataType: supportedInputAttributes[evt.currentTarget.id.split('attr-')[1]].type,
                    }
                    $(container).find('.att-fun-op-search-box').val('');
                    tempExp.addNode(new AttributeModel(nodeData));
                    updateExpression(tempExp);
                });
                attributeContainer.find('.attrib-selector-containers')
                    .find('#custom_val_input_container').off('click');
                attributeContainer.find('.attrib-selector-containers')
                    .find('#custom_val_input_container').children().off('click');

                displayCustomValueInput($(container).find('#custom_val_type').val());

                attributeContainer.find('.attrib-selector-containers').find('#custom_val_type')
                    .on('change', function(evt) {
                        displayCustomValueInput($(container).find('#custom_val_type').val());
                    });

                $(container).find('.btn-custom-val-submit').on('click', function(evt) {
                    var customNode = null;
                    var nodeData = {}
                    if ($(container).find('#custom_val_type').val() === 'bool') {
                        var value = $(container).find('#custom_value_input_bool').val();
                        nodeData = {
                            dataType: 'bool',
                            value
                        };
                    } else {
                        var value = $(container).find('#custom_value_input_txt').val();
                        nodeData = {
                            dataType: $(container).find('#custom_val_type').val(),
                            value
                        };
                    }
                    tempExp.addNode(new CustomValueModel(nodeData));
                    selectedFilter = '';
                    selectedCategory = null;
                    updateExpression(tempExp);
                });

                $(container).find('.att-fun-op-search-box').off('keyup');
                $(container).find('.att-fun-op-search-box').on('keyup', _.debounce(function(evt) {
                    updateFilter('Attribute', evt.target.value);
                }, 100, {}));
            })

            $(nodeCategoryContainer).find('.function-category').on('click', function(evt) {
                nodeCategoryContainer.find('li>a>div').removeClass('selected');
                nodeCategoryContainer.find('.function-category').addClass('selected');
                attributeContainer.find('.select-function-operator-attrib').show();
                attributeContainer.find('.attrib-selector-containers').empty();

                Object.keys(supportedFunctions).forEach(function(key) {
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

                attributeContainer.find('.attrib-selector-containers').children().on('click', function(evt) {
                    attributeContainer.find('.select-function-operator-attrib').hide();
                    attributeContainer.find('.select-function-format-container').show();
                    $(container).find('.att-fun-op-search-box').val('');

                    supportedFunctions[evt.currentTarget.id.split('func-')[1]].syntax.forEach(function(syntax, i) {
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

                    attributeContainer.find('.select-function-format-container').find('ul').children()
                        .on('click', function(child_evt) {
                            nodeData = {
                                displayName: supportedFunctions[evt.currentTarget.id.split('func-')[1]].displayName,
                                dataTypes: supportedFunctions[evt.currentTarget.id.split('func-')[1]]
                                    .returnAttributes[0].type.map(function(dataType) {
                                    return dataType.toLowerCase();
                                }),
                                selectedSyntax: supportedFunctions[evt.currentTarget.id.split('func-')[1]]
                                    .syntax[child_evt.currentTarget.id.split('syntax-')[1]],
                            };

                            var parameterData = supportedFunctions[evt.currentTarget.id.split('func-')[1]]
                                .parameters || [];

                            nodeData.selectedSyntax['parameterData'] = _.reduce(parameterData, function(obj, param) {
                                obj[param.name] = param.description
                                return obj
                            }, {})

                            tempExp.addNode(new FunctionModel(nodeData));
                            selectedFilter = '';
                            selectedCategory = null;
                            updateExpression(tempExp);
                            attributeContainer.find('.select-function-format-container').find('ul').empty();
                            attributeContainer.find('.select-function-format-container').hide();

                        })
                    });
                $(container).find('.att-fun-op-search-box').off('keyup');
                $(container).find('.att-fun-op-search-box').on('keyup', _.debounce(function(evt) {
                    updateFilter('Function', evt.target.value);
                }, 100, {}));
            })

            $(nodeCategoryContainer).find('.operator-category').on('click', function(evt) {
                nodeCategoryContainer.find('li>a>div').removeClass('selected');
                nodeCategoryContainer.find('.operator-category').addClass('selected');
                attributeContainer.find('.select-function-operator-attrib').show();
                attributeContainer.find('.attrib-selector-containers').empty();

                Object.keys(supportedOperators).forEach(function(key) {
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

                attributeContainer.find('.attrib-selector-containers').children().on('click', function(evt) {
                    if (evt.currentTarget.id.split('operator-')[1] === 'bracket') {
                        tempExp.addNode(new ScopeModel(tempExp.returnTypes));
                    } else {
                        tempExp.addNode(new OperatorModel(
                            supportedOperators[evt.currentTarget.id.split('operator-')[1]]));
                    }

                    $(container).find('.att-fun-op-search-box').val('');
                    selectedFilter = '';
                    selectedCategory = null;
                    updateExpression(tempExp);
                })
                $(container).find('.att-fun-op-search-box').off('keyup');
                $(container).find('.att-fun-op-search-box').on('keyup', _.debounce(function(evt) {
                    updateFilter('Operator', evt.target.value);
                }, 100, {}));
            })

            if (selectedCategory) {
                $(nodeCategoryContainer).children().find(`a>div:contains("${selectedCategory}")`).click();
            } else {
                $(nodeCategoryContainer).children().first().find('a>div').click();
            }
        }

        DataMapper.prototype.updateFilter = function(filterCategory, filterValue) {
            this.selectedCategory = filterCategory;
            this.selectedCategoryFilter = filterValue;
            this.renderGenerator();
        }

        DataMapper.prototype.displayCustomValueInput = function(type) {
            $(this.container).find('#custom_value_input_txt').hide();
            $(this.container).find('#custom_value_input_txt').val('');
            $(this.container).find('#custom_value_input_bool').hide();
            if (type === 'bool') {
                $(this.container).find('#custom_value_input_bool').show();
            } else {
                $(this.container).find('#custom_value_input_txt').show();
                if (DataMapperUtil.getGenericDataType(type) === 'number') {
                    $(this.container).find('#custom_value_input_txt').attr('type', 'number');
                } else {
                    $(this.container).find('#custom_value_input_txt').attr('type', 'text');
                }
            }
        }

        DataMapper.prototype.updateExpression = function(expression) {
            if (this.coordinate.length === 0) {
                this.expressionMap[this.currenOutputElement] = expression;
            }
            this.selectedCategoryFilter = '';
            this.selectedCategory = null;
            this.renderGenerator();
        }

        DataMapper.prototype.hideExpressionGenerationDialog = function(container, expressionGeneratorContainer) {
            expressionGeneratorContainer.remove();
            expressionGeneratorContainer = $(container).find('.popup-backdrop').clone();
            $(container).prepend(expressionGeneratorContainer);
            $(container).find('.att-fun-op-search-box').val('');

            this.expressionGenerationDialog = expressionGeneratorContainer;
            this.currenOutputElement = null;
            this.selectedCategory = null;
            this.selectedCategoryFilter = '';
            this.coordinate = [];
            this.focusNode = [];
        }

        DataMapper.prototype.renderFunctionAttributeSelector = function(type, attributeFunctionArray,
                                                                        outputAttributeName) {
            var nodeCategoryContainer = this.expressionGenerationDialog.find('.att-fun-op-container');
            var addNodeToExpression = this.addNodeToExpression;
            var attributeContainer = $(nodeCategoryContainer).find('.select-function-operator-attrib');
            var syntaxSelectorContainer = $(nodeCategoryContainer).find('.select-function-format-container');

            $(attributeContainer).find('.attrib-selector-containers').children().remove();

            Object.values(attributeFunctionArray).forEach(function(element) {
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

                $(attributeContainer).find('.attrib-selector-containers').children().last().on('click', function() {
                    if (type !== 'function') {
                        if (!element.scope) {
                            addNodeToExpression(type, element, outputAttributeName);
                        } else {
                            addNodeToExpression('scope', element, outputAttributeName);
                        }
                    } else {
                        element.syntax.forEach(function(syntax_obj) {
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

                            $(syntaxSelectorContainer).find('ul').children().last().on('click', function() {
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

        DataMapper.prototype.addNodeToExpression = function(type, node_data, outputAttributeName) {
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
                    node = new OperatorModel(data);
                    break;
                case 'scope':
                    node = new ScopeModel([this.expressionMap[outputAttributeName].dataType]);
                    break;
                case 'function':
                    data = {
                        displayName: node_data.displayName,
                        dataType: this.expressionMap[outputAttributeName].dataType,
                        selectedSyntax: node_data['syntax_selected']
                    }
                    node = new FunctionModel(data);
            }

            if (coordinates.length === 0) {
                this.expressionMap[outputAttributeName].addNodeToMainExpression(node);
            } else {
                this.expressionMap[outputAttributeName].addNodeToChildExpression(coordinates, node);
            }
            this.displayExpression(outputAttributeName);
        }

        DataMapper.prototype.displayExpression = function(outputAttrName) {
            var htmlContent = DataMapperUtil.generateExpressionHTML(
                    this.expressionMap[outputAttrName],
                '',
                null);
            $(this.container).find('.main-exp').empty()
            $(this.container).find('.main-exp').append(htmlContent);
        }

        DataMapper.prototype.generateExpressionMap = function(inputAttributes, expressionFunctions) {
            var supportedExtensionTypes = ['time', 'env', 'geo', 'math', 'str'];
            var expressionMap = {
                string: {},
                int: {},
                long: {},
                double: {},
                float: {},
                bool: {},
                object: {}
            }

            inputAttributes.forEach(function(attrib) {
                if (!expressionMap[attrib.type.toLowerCase()]['attribute']) {
                    expressionMap[attrib.type.toLowerCase()]['attribute'] = {}
                }
                expressionMap[attrib.type.toLowerCase()]['attribute'][attrib.name] = attrib;
            });

            supportedExtensionTypes.forEach(function(extensionType) {
                expressionFunctions['extensions'][extensionType].functions.forEach(function(func) {
                    if (func.returnAttributes) {
                        func.returnAttributes[0].type.forEach(function(type) {
                            if (!expressionMap[type.toLowerCase()]['function']) {
                                expressionMap[type.toLowerCase()]['function'] = {}
                            }
                            func['displayName'] = func.namespace + ':' + func.name + '()';
                            expressionMap[type.toLowerCase()]['function'][func.name] = func;
                        });
                    }

                });
            })

            expressionFunctions['inBuilt'].functions.forEach(function(func) {
                if (func.returnAttributes) {
                    func.returnAttributes[0].type.forEach(function(type) {
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
                    node.children[coordinates[coordinates.length - level]] :
                    node.parameters[coordinates[coordinates.length - level]]);
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

        function validateExpressionTree(expression) {
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

        return DataMapper;

    });
