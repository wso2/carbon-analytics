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

define(['require', 'jquery', 'lodash', 'log', 'alerts', 'scopeModel', 'attributeModel', 'functionModel', 'operatorModel', 'customValueModel', 'dataMapperUtil'],

    function (require, $, _, log, Alerts, ScopeModel, AttributeModel, FunctionModel, OperatorModel, CustomValueModel, DataMapperUtil) {
        var inputOptionCallback;

        var FilterInputOptionComponent = function (container, config, callback) {
            this.__container = container;
            this.__config = config;
            inputOptionCallback = callback;

            if (!config.query.filter['expression']) {
                config.query.filter['expression'] = new ScopeModel(['bool']);
                config.query.filter['reverseFilter'] = false;
            }

            this.__expression = config.query.filter.expression;

            this.__indexArray = [];
            this.__focusNodes = [];
        }

        FilterInputOptionComponent.prototype.constructor = FilterInputOptionComponent;

        FilterInputOptionComponent.prototype.render = function () {
            var self = this;
            var container = this.__container;
            var config = this.__config;
            var expression = this.__config.query.filter.expression;
            var indexArray = this.__indexArray;
            var focusNodes = this.__focusNodes;

            container.empty();
            container.append(`
                <div style="display: flex; margin-bottom: 5px">
                    <div style="flex: 1">
                        <h3 style="margin-top: 0; color: #373737">Filter condition</h3>
                    </div>
                    <button id="btn-reverse-filter" class="btn btn-default">
                        <i class="fw ${config.query.filter.reverseFilter ? 'fw-check' : 'fw-error'}"></i>&nbsp;
                        Exclude events
                    </button>    
                </div>
                <div style="color: #373737" class="expression-section">
                </div>
                ${
                typeof expression !== 'string' ?
                    `<div class="operand-section" style="display: flex; flex: 1; margin-top: 15px">
                            <div class="operand-category-select" style="width: 20%">
                                <ul>
                                </ul>
                            </div>
                            <div class="operand-select-section" style="width: 80%; background: #d3d3d3; overflow: auto">
                                <ul>
                                </ul>
                            </div>
                        </div>` : ''
                }
            `);

            container.find('.expression-section')
                .append(`
                    <div style="display: flex; padding: ${focusNodes.length === 0 ? '15px' : '5px'} 0;" class="expression ${focusNodes.length === 0 ? 'focus' : ''}">
                        <div style="width: 95%" class="expression-content">
                            ${
                    typeof expression !== 'string' ?
                        DataMapperUtil.generateExpressionHTML(expression, '')
                        : expression
                    }
                        </div>    
                        ${
                    focusNodes.length === 0 ?
                        `<div style="width: 5%;padding: 5px;" class="icon-section">
                            <a style="color: #373737"><i class="fw fw-clear"></i></a>
                        </div>`
                        : ''
                    }
                    </div>
                `);

            if (focusNodes.length > 0) {
                focusNodes.forEach(function (node, i) {
                    container.find('.expression-section')
                        .append(`
                            <div style="display: flex; padding: ${focusNodes.length - 1 === i ? '15px' : '5px'} 0;" 
                                class="expression ${focusNodes.length - 1 === i ? 'focus' : ''}">
                                <div style="width: 95%" class="expression-content">
                                    ${DataMapperUtil.generateExpressionHTML(node, '')}
                                </div>    
                                ${
                            focusNodes.length - 1 === i ?
                                '<div style="width: 5%;padding: 5px;" class="icon-section">' +
                                '   <a><i class="fw fw-up"></i></a>' +
                                '</div>'
                                : ''
                            }
                            </div>
                        `);
                });
            }

            var allowedAttributes = {};
            var allowedOperators = {};

            var tempExpression = focusNodes.length > 0 ? focusNodes[focusNodes.length - 1] : expression;

            if (typeof tempExpression !== 'string') {
                if (tempExpression.rootNode) {
                    switch (tempExpression.rootNode.type) {
                        case 'function':
                        case 'attribute':
                        case 'customValue':
                        case 'scope':

                            if (tempExpression.genericReturnTypes.indexOf('bool') > -1) {
                                Object.keys(DataMapperUtil.OperatorMap)
                                    .filter(function (key) {
                                        return DataMapperUtil.OperatorMap[key].hasLeft
                                            && _.intersection(DataMapperUtil.OperatorMap[key].leftTypes,
                                                tempExpression.rootNode.genericReturnTypes).length > 0
                                    })
                                    .forEach(function (key) {
                                        allowedOperators[key] = DataMapperUtil.OperatorMap[key]
                                    });
                            } else {
                                Object.keys(DataMapperUtil.OperatorMap)
                                    .filter(function (key) {
                                        return DataMapperUtil.OperatorMap[key].hasLeft
                                            && _.intersection(DataMapperUtil.OperatorMap[key].leftTypes,
                                                tempExpression.rootNode.genericReturnTypes).length > 0
                                            && _.intersection(DataMapperUtil.OperatorMap[key].returnTypes,
                                                tempExpression.genericReturnTypes).length > 0
                                    })
                                    .forEach(function (key) {
                                        allowedOperators[key] = DataMapperUtil.OperatorMap[key]
                                    });
                            }
                            break;
                        case 'operator':
                            if (tempExpression.rootNode.hasRight && !tempExpression.rootNode.rightNode) {
                                config.input.stream.attributes
                                    .filter(function (attr) {
                                        return tempExpression.rootNode.rightTypes
                                            .indexOf(DataMapperUtil.getGenericDataType(attr.type)) > -1;
                                    })
                                    .forEach(function (attr) {
                                        allowedAttributes[attr.name] = attr;
                                    });

                                allowedAttributes['$custom_val_properties'] = {
                                    genericDataTypes: tempExpression.rootNode.rightTypes,
                                };

                                if (tempExpression.rootNode.type !== 'scope') {
                                    allowedOperators = {
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
                                            tempExpression.rootNode.genericReturnTypes).length > 0
                                    })
                                    .forEach(function (key) {
                                        allowedOperators[key] = DataMapperUtil.OperatorMap[key]
                                    });
                            }

                            break;
                    }
                } else {

                    var customDataTypes = []
                    if (tempExpression.returnTypes.indexOf('bool') > -1) {
                        config.input.stream.attributes
                            .forEach(function (attr) {
                                allowedAttributes[attr.name] = attr;
                            });
                        customDataTypes = ['text', 'number', 'bool'];

                    } else {
                        config.input.stream.attributes
                            .filter(function (attr) {
                                return tempExpression.returnTypes.indexOf(attr.type) > -1;
                            }).forEach(function (attr) {
                                allowedAttributes[attr.name] = attr;
                            });

                        tempExpression.returnTypes.forEach(function (type) {
                            customDataTypes.push(DataMapperUtil.getGenericDataType(type));
                        })
                    }

                    allowedAttributes['$custom_val_properties'] = {
                        genericDataTypes: customDataTypes
                    };

                    allowedOperators['bracket'] = {
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
                                    tempExpression.returnTypes).length > 0;
                        })
                        .forEach(function (key) {
                            allowedOperators[key] = DataMapperUtil.OperatorMap[key];
                        });
                }
            }

            if (Object.keys(allowedAttributes).length > 0) {
                container.find('.operand-category-select>ul')
                    .append(`
                        <li id="operand-type-attribute" >
                            <a style="color: #373737">
                                <div style="padding: 15px; border-bottom: 1px solid rgba(0,0,0,.075);">
                                    Attribute
                                </div>
                            </a>    
                        </li>
                    `);
            }

            if (Object.keys(allowedOperators).length > 0) {
                container.find('.operand-category-select>ul')
                    .append(`
                        <li id="operand-type-operator" >
                            <a style="color: #373737">
                                <div style="padding: 15px; border-bottom: 1px solid rgba(0,0,0,.075);">
                                    Operator
                                </div>
                            </a>    
                        </li>
                    `);
            }

            container.find('.operand-category-select>ul>li').on('click', function (evt) {
                var operandType = evt.currentTarget.id.match('operand-type-([a-z]+)')[1];
                var operandList = container.find('.operand-select-section>ul');

                container.find('.operand-category-select>ul>li>a>div').removeClass('focus');
                $(evt.currentTarget).find('a>div').addClass('focus');

                operandList.empty();

                switch (operandType) {
                    case 'attribute':
                        Object.keys(allowedAttributes).forEach(function (key) {
                            if (key === '$custom_val_properties') {
                                container.find('.operand-select-section>ul').append(`
                                    <li class="custom" id="attribute-${key}">
                                        <a style="color: #373737">
                                            <div class="attribute" 
                                                style="display: flex; flex-wrap: wrap; padding: 5px; 
                                                border-bottom: 1px solid rgba(0,0,0,.075);">
                                                <div class="description" style="width: 100%;">
                                                    Add a custom value to the expression 
                                                </div>
                                                <div style="width: 100%;">
                                                    Custom Value
                                                    <select name="" id="custom-val-type">
                                                    </select>
                                                    <input style="display: none; width: 50%" 
                                                        id="custom_value_input_txt" type="text">
                                                    <select style="display: none; width: 50%" 
                                                        id="custom_value_input_bool" >
                                                        <option value="true">True</option>
                                                        <option value="false">false</option>
                                                    </select>
                                                    <button style="background-color: #f47b20; padding: 0 6px 0 6px;"
                                                        class="btn btn-primary btn-custom-val-submit">Add</button>
                                                </div>
                                            </div>
                                        </a>    
                                    </li>
                                `);

                                container.find('.operand-select-section #custom-val-type')
                                    .on('change', function (evt) {
                                        var customValueType = $(evt.currentTarget).val();

                                        switch (customValueType.toLowerCase()) {
                                            case 'text':
                                                var textFieldElement =
                                                    container.find('.operand-select-section #custom_value_input_txt');
                                                textFieldElement.show();
                                                break;
                                            case 'number':
                                                var textFieldElement =
                                                    container.find('.operand-select-section #custom_value_input_txt');
                                                textFieldElement.show();
                                                textFieldElement.attr('type', 'number');
                                                break;
                                            case 'bool':
                                                var booleanFieldElement =
                                                    container.find('.operand-select-section #custom_value_input_bool');
                                                booleanFieldElement.show();
                                                break;
                                        }
                                    });

                                allowedAttributes[key].genericDataTypes.forEach(function (type) {
                                    switch (type) {
                                        case 'text':
                                            container.find('.operand-select-section #custom-val-type')
                                                .append(
                                                    `<option>string</option>`
                                                );
                                            break;
                                        case 'number':
                                            container.find('.operand-select-section #custom-val-type')
                                                .append(`
                                                    <option>int</option>
                                                    <option>long</option>
                                                    <option>float</option>
                                                    <option>double</option>
                                                `);
                                            break;
                                        case 'bool':
                                            container.find('.operand-select-section #custom-val-type')
                                                .append(`
                                                    <option>bool</option>
                                                `);
                                            break;
                                    }
                                });

                                switch (DataMapperUtil.getGenericDataType(
                                    container.find('.operand-select-section #custom-val-type').val())) {
                                    case 'text':
                                    case 'number':
                                        var textFieldElement =
                                            container.find('.operand-select-section #custom_value_input_txt');
                                        textFieldElement.show();
                                        textFieldElement.attr('type',
                                            container.find('.operand-select-section #custom-val-type')
                                                .val().toLowerCase());
                                        break;
                                    case 'bool':
                                        var booleanFieldElement =
                                            container.find('.operand-select-section #custom_value_input_bool');
                                        booleanFieldElement.show();
                                        break;
                                }

                                container.find('.operand-select-section .btn-custom-val-submit')
                                    .on('click', function (evt) {
                                        var dataType =
                                            container.find('.operand-select-section #custom-val-type').val();
                                        var value = dataType === 'bool' ?
                                            container.find('.operand-select-section #custom_value_input_bool').val()
                                            : container.find('.operand-select-section #custom_value_input_txt').val();

                                        var nodeData = {
                                            dataType,
                                            value: DataMapperUtil.getGenericDataType(dataType) === 'number' ?
                                                Number(value) : value
                                        };

                                        tempExpression.addNode(new CustomValueModel(nodeData));
                                        self.render();
                                    });
                            } else {
                                container.find('.operand-select-section>ul').append(`
                                    <li class="not-custom" id="attribute-${key}">
                                        <a>
                                            <div style="padding: 10px 15px;color: #373737; 
                                                border-bottom: 1px solid rgba(0,0,0,.075);" >
                                                <b>${key}</b>
                                                <br/><small>${allowedAttributes[key].type}</small>
                                            </div>
                                        </a>    
                                    </li>
                                `);
                            }
                        });
                        break;
                    case 'operator':
                        Object.keys(allowedOperators).forEach(function (key) {
                            container.find('.operand-select-section>ul').append(`
                                <li class="not-custom" id="operator-${key}">
                                    <a>
                                        <div style="color: #373737; padding: 10px 15px; 
                                            border-bottom: 1px solid rgba(0,0,0,.075);" >
                                            <b>${allowedOperators[key].symbol}</b>
                                            &nbsp;-&nbsp;${allowedOperators[key].description}
                                        </div>
                                    </a>    
                                </li>
                            `);
                        });
                        break;
                }

                container.find('.operand-select-section>ul>li.not-custom').on('click', function (evt) {
                    var operandDataArray = evt.currentTarget.id.split('-');

                    switch (operandDataArray[0]) {
                        case 'attribute':
                            var nodeData = nodeData = {
                                name: operandDataArray[1],
                                dataType: allowedAttributes[operandDataArray[1]].type
                            };
                            tempExpression.addNode(new AttributeModel(nodeData));
                            break;
                        case 'operator':
                            if (operandDataArray[1] !== 'bracket') {
                                tempExpression.addNode(new OperatorModel(allowedOperators[operandDataArray[1]]));
                            } else {
                                tempExpression.addNode(new ScopeModel(['int', 'long', 'double', 'float', 'bool']))
                            }
                            break;
                    }
                    self.render();
                });

            });

            $(container.find('.operand-category-select>ul>li')[0]).click();

            container.find('.expression.focus .expression-content>span').on('click', function (evt) {
                var pathIndex = evt.currentTarget.id.match('item-([a-z-]+)')[1];
                self.__indexArray.push(pathIndex);

                var path = pathIndex.split('-');

                var addToFocus = function (exp, pathArray) {
                    if (pathArray.length === 1) {
                        if (pathArray[0] === 'n') {
                            self.__focusNodes.push(_.cloneDeep(exp.rootNode));
                        } else if (pathArray[0] === 'l') {
                            self.__focusNodes.push(_.cloneDeep(exp.leftNode));
                        } else {
                            self.__focusNodes.push(_.cloneDeep(exp.rightNode));
                        }
                    } else {
                        var next = pathArray.splice(0, 1);

                        if (next[0] === 'n') {
                            addToFocus(exp.rootNode, pathArray);
                        } else if (next[0] === 'l') {
                            addToFocus(exp.leftNode, pathArray);
                        } else {
                            addToFocus(exp.rightNode, pathArray);
                        }
                    }
                }

                addToFocus(tempExpression, path);

                console.log(self.__indexArray, self.__focusNodes);
                self.render();
            });

            container.find('#btn-reverse-filter').on('click', function (evt) {
                config.query.filter.reverseFilter = !config.query.filter.reverseFilter;
                self.render();
            })

            container.find('.expression.focus .fw-clear').on('click', function (evt) {
                self.__config.query.filter.expression = new ScopeModel(['bool']);
                self.render();
            });

            container.find('.expression.focus .fw-up').on('click', function (evt) {
                var path = self.__indexArray[self.__indexArray.length - 1].split('-');

                var replaceInExpression = function (exp, pathArray) {
                    if (pathArray.length === 1) {
                        if (pathArray[0] === 'n') {
                            exp.rootNode = focusNodes[focusNodes.length - 1];
                        } else if (pathArray[0] === 'l') {
                            exp.leftNode = focusNodes[focusNodes.length - 1];
                        } else {
                            exp.rightNode = focusNodes[focusNodes.length - 1];
                        }
                    } else {
                        var next = pathArray.splice(0, 1);

                        if (next[0] === 'n') {
                            replaceInExpression(exp.rootNode, pathArray);
                        } else if (next[0] === 'l') {
                            replaceInExpression(exp.leftNode, pathArray);
                        } else {
                            replaceInExpression(exp.rightNode, pathArray);
                        }
                    }
                }

                replaceInExpression(focusNodes.length > 1 ? focusNodes[focusNodes.length - 2] : expression, path);
                focusNodes.pop();
                self.__indexArray.pop();
                self.render();
            });

        }

        return FilterInputOptionComponent;
    });