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

define(['require', 'jquery', 'lodash', 'log', 'alerts', 'scopeModel', 'operatorModel', 'attributeModel', 'customValueModel', 'dataMapperUtil'],

    function (require, $, _, log, Alerts, ScopeModel, OperatorModel, AttributeModel, CustomValueModel, DataMapperUtil) {
        var QueryGroupByComponent = function (container, config, parentContainer) {
            this.__config = config;
            this.__container = container;
            this.__indexArray = [];
            this.__focusNodes = [];
            this.__expression = config.query.groupby.havingFilter.expression || null;
        }

        QueryGroupByComponent.prototype.constructor = QueryGroupByComponent;

        QueryGroupByComponent.prototype.render = function () {
            var self = this;
            var container = this.__container;
            var config = this.__config;
            var expression = this.__expression;
            var focusNodes = this.__focusNodes;
            var indexArray = this.__indexArray;

            container.empty();

            container.append(`
                <div style="font-size: 1.8rem; margin-bottom:15px">
                    Group output by fields<br/>
                    <small style="font-size: 1.3rem">
                        Group the output records using a set of fields and filter them based on a condition
                    </small>
                </div>
                Group by attributes
                <button style="background-color: #ee6719" class="btn btn-default btn-circle" 
                    id="btn-add-groupby-field" type="button" data-toggle="dropdown">
                    <i class="fw fw-add"></i>
                </button>
                <div id="groupby-options-dropdown" class="dropdown-menu-style hidden" aria-labelledby="">
                </div>
                <div id="group-by-fields-list" style="display: flex; margin-top: 15px">
                </div>
                <div id="having-option-container">
                </div>
            `);

            var existingGroupByAttributes = config.query.groupby.attributes.map(function (attr) {
                return attr.name;
            });

            config.output.stream.attributes
                .filter(function (attr) {
                    return existingGroupByAttributes.indexOf(attr.name) === -1;
                })
                .forEach(function (attr) {
                    container.find('#groupby-options-dropdown')
                        .append(`
                            <a title="" class="dropdown-item" href="#">
                                <div>
                                    <div class="option-title">${attr.name}</div><br/>
                                    <small style="opacity: 0.8">${attr.type}</small><br/>
                                </div>
                            </a>
                        `);
                });

            container.find('#btn-add-groupby-field')
                .on('mouseover', function (evt) {
                    var leftOffset = evt.currentTarget.offsetLeft;
                    var elementObj = container.find('#groupby-options-dropdown');
                    elementObj.css({"left": `${leftOffset}px`})
                    elementObj
                        .removeClass('hidden')
                        .on('mouseleave', function () {
                            elementObj.addClass('hidden');
                        });
                })
                .on('mouseleave', function () {
                    setTimeout(function () {
                        var elementObj = container.find('#groupby-options-dropdown');
                        if (!(container.find('#groupby-options-dropdown:hover').length > 0)) {
                            elementObj.addClass('hidden');
                        }
                    }, 300);
                });

            container.find('#groupby-options-dropdown .dropdown-item')
                .on('click', function (evt) {
                    var attributeName = $(evt.currentTarget).find('.option-title').html();

                    var attrib = config.output.stream.attributes.find(function (attr) {
                        return attributeName === attr.name;
                    });

                    config.query.groupby.attributes.push(attrib);
                    self.render();
                });

            config.query.groupby.attributes.forEach(function (attr) {
                container.find('#group-by-fields-list')
                    .append(`
                        <div style="color: white; background-color: #555;
                            padding: 5px 15px; margin-right: 15px; border-radius: 15px">
                            <b>${attr.name}</b>
                            <span style="margin-left: 5px; opacity: 0.6;cursor: pointer;">
                                <i id="del-attr-${attr.name}" class="fw fw-error"></i>
                            </span>
                        </div>
                    `);
            });

            container.find('#group-by-fields-list .fw-error').on('click', function (evt) {
                var attributeName = evt.currentTarget.id.match('del-attr-([a-zA-Z0-9\_]+)')[1];
                var index = config.query.groupby.attributes.map(function (attr) {
                    return attr.name;
                }).indexOf(attributeName);

                if(index > -1) {
                    config.query.groupby.attributes.splice(index, 1);
                }

                self.render();
            });

            if(config.query.groupby.attributes.length > 0) {
                container.find('#having-option-container')
                    .append(`
                        <div style="margin-top: 15px; display: flex">
                            <div style="">
                                <span>Set having condition : </span>
                            </div>
                            <div>
                               <div style="margin-left: 15px">
                                    <div id="btn-group-enable-groupby-filter" 
                                        class="btn-group btn-group-toggle" data-toggle="buttons">
                                        <label class="btn" 
                                                style="${
                                                    Object.keys(config.query.groupby.havingFilter).length !== 0 ?
                                                            "background-color: rgb(91,203,92); color: white;"
                                                            : "background-color: rgb(100,109,118); color: white;"}" 
                                         >
                                            <input type="radio" name="options" id="enable" autocomplete="off"> 
                                            <i class="fw fw-check"></i>
                                        </label>
                                        <label class="btn" 
                                                style="${
                                                    Object.keys(config.query.groupby.havingFilter).length === 0 ?
                                                            "background-color: red; color: white;"
                                                            : "background-color: rgb(100,109,118); color: white;"}" 
                                        >
                                            <input type="radio" name="options" id="disable" autocomplete="off"> 
                                            <i class="fw fw-cancel"></i>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    `);
                
                container.find('#btn-group-enable-groupby-filter .btn').on('click', function(evt) {
                    if(config.query.groupby.havingFilter['enabled']) {
                        config.query.groupby.havingFilter = {};
                    } else {
                        config.query.groupby.havingFilter['enabled'] = true;
                        config.query.groupby.havingFilter['expression'] = new ScopeModel(['bool']);
                        self.__expression = config.query.groupby.havingFilter['expression'];
                    }

                    self.render();
                });
            }

            if(Object.keys(config.query.groupby.havingFilter).length > 0) {
                container.find('#having-option-container')
                    .append(`
                        <div style="background-color: #3a3a3a">
                            <div style="color: #fff; margin-top: 15px" class="expression-section">
                            </div>
                            ${
                                typeof config.query.groupby.havingFilter !=='string'?
                                    `<div class="operand-section" style="display: flex; flex: 1; flex-direction: column">
                                        <div class="operand-category-select" style="display: flex;background: rgb(162,162,162)">
                                            
                                        </div>
                                        <div class="operand-select-section" style="background: lightgray; overflow: auto">
                                            <ul>
                                            </ul>
                                        </div>
                                    </div>` : ''    
                            }
                            
                        </div>
                    `);

                container.find('.expression-section')
                    .append(`
                        <div 
                            style="display: flex; padding: ${focusNodes.length === 0 ? '15px' : '5px'} 0; 
                                background: ${focusNodes.length === 0 ? 'lightgray' : '#c3c3c3'}" 
                            class="expression ${focusNodes.length === 0 ? 'focus' : ''}">
                            <div style="width: 95%; color: #323232" class="expression-content">
                                ${
                                    typeof expression !== 'string' ?
                                        DataMapperUtil.generateExpressionHTML(expression, '')
                                        : expression
                                }
                            </div>    
                            ${
                                focusNodes.length === 0 ?
                                    `<div style="padding: 5px;" class="icon-section">
                                        <a style="color: #323232"><i class="fw fw-clear"></i></a>
                                    </div>`
                                    : ''
                            }
                        </div>
                    `);

                if (focusNodes.length > 0) {
                    focusNodes.forEach(function (node, i) {
                        container.find('.expression-section')
                            .append(`
                                <div style="display: flex; padding: ${focusNodes.length - 1 === i ? '15px' : '5px'} 0; 
                                color: #323232" class="expression ${focusNodes.length - 1 === i ? 'focus' : ''}">
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

                container.find('.expression-section .fw-clear')
                    .on('click', function () {
                        config.query.groupby.havingFilter['enabled'] = true;
                        config.query.groupby.havingFilter['expression'] = new ScopeModel(['bool']);
                        self.__expression = config.query.groupby.havingFilter['expression'];
                        self.render();
                    })

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
                                                && _.intersection(
                                                        DataMapperUtil.OperatorMap[key].leftTypes,
                                                        tempExpression.rootNode.genericReturnTypes
                                                ).length > 0
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
                                    config.output.stream.attributes
                                        .filter(function (attr) {
                                            return tempExpression.rootNode
                                                .rightTypes.indexOf(DataMapperUtil.getGenericDataType(attr.type)) > -1;
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
                                                && _.intersection(DataMapperUtil.OperatorMap[key].returnTypes,
                                                    tempExpression.genericReturnTypes).length > 0;
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
                            config.output.stream.attributes
                                .forEach(function (attr) {
                                    allowedAttributes[attr.name] = attr;
                                });
                            customDataTypes = ['text', 'number', 'bool'];
                        } else {
                            config.output.stream.attributes
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
                    container.find('.operand-category-select')
                        .append(`
                            <a style="color:#323232" id="operand-type-attribute" >
                                <div style="padding: 15px; border-right: 1px solid rgba(102, 102, 102, 0.6);">
                                    Attribute
                                </div>
                            </a>    
                        `);
                }

                if (Object.keys(allowedOperators).length > 0) {
                    container.find('.operand-category-select')
                        .append(`
                            <a style="color:#323232" id="operand-type-operator">
                                <div style="padding: 15px; border-right: 1px solid rgba(102, 102, 102, 0.6);">
                                    Operator
                                </div>
                            </a>    
                        `);
                }

                container.find('.operand-category-select>a').on('click', function (evt) {
                    var operandType = evt.currentTarget.id.match('operand-type-([a-z]+)')[1];
                    var operandList = container.find('.operand-select-section>ul');

                    container.find('.operand-category-select>a>div').removeClass('focus');
                    $(evt.currentTarget).find('div').addClass('focus');

                    operandList.empty();

                    switch (operandType) {
                        case 'attribute':
                            Object.keys(allowedAttributes).forEach(function (key) {
                                if (key === '$custom_val_properties') {
                                    container.find('.operand-select-section>ul').append(`
                                    <li class="custom" id="attribute-${key}">
                                        <a style="color: #323232">
                                            <div class="attribute" style="display: flex; 
                                                flex-wrap: wrap; padding-bottom: 5px; padding-left: 5px">
                                                <div class="description" style="width: 100%;">
                                                    Add a custom value to the expression 
                                                </div>
                                                <div style="width: 100%;">
                                                    Custom Value
                                                    <select name="" id="custom-val-type">
                                                    </select>
                                                    <input style="display: none; width: 45%" 
                                                        id="custom_value_input_txt" type="text">
                                                    <select style="display: none; width: 45%" 
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
                                                    var textFieldElement = container
                                                        .find('.operand-select-section #custom_value_input_txt');
                                                    textFieldElement.show();
                                                    break;
                                                case 'number':
                                                    var textFieldElement = container
                                                        .find('.operand-select-section #custom_value_input_txt');
                                                    textFieldElement.show();
                                                    textFieldElement.attr('type', 'number');
                                                    break;
                                                case 'bool':
                                                    var booleanFieldElement = container
                                                        .find('.operand-select-section #custom_value_input_bool');
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

                                    switch (DataMapperUtil
                                        .getGenericDataType(container
                                            .find('.operand-select-section #custom-val-type').val())) {
                                        case 'text':
                                        case 'number':
                                            var textFieldElement = container.find('.operand-select-section #custom_value_input_txt');
                                            textFieldElement.show();
                                            textFieldElement.attr('type',
                                                container.find('.operand-select-section #custom-val-type')
                                                    .val().toLowerCase());
                                            break;
                                        case 'bool':
                                            var booleanFieldElement = container
                                                .find('.operand-select-section #custom_value_input_bool');
                                            booleanFieldElement.show();
                                            break;
                                    }

                                    container.find('.operand-select-section .btn-custom-val-submit')
                                        .on('click', function (evt) {
                                            var dataType = container
                                                .find('.operand-select-section #custom-val-type').val();
                                            var value = dataType === 'bool' ?
                                                container
                                                    .find('.operand-select-section #custom_value_input_bool').val()
                                                : container
                                                    .find('.operand-select-section #custom_value_input_txt').val();

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
                                        <a style="color: #323232">
                                            <div style="padding: 10px 15px;
                                            border-bottom: 1px solid rgba(255, 255, 255, 0.2);" >
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
                                    <a style="color: #323232">
                                        <div style="padding: 10px 15px;
                                        border-bottom: 1px solid rgba(255, 255, 255, 0.2);" >
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
                                var nodeData = {
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
    
                    replaceInExpression(
                        focusNodes.length > 1 ? focusNodes[focusNodes.length - 2] : expression, path);
                    focusNodes.pop();
                    self.__indexArray.pop();
                    self.render();
                });

                $(container.find('.operand-category-select>a')[0]).click();
            }
        }

        return QueryGroupByComponent;
    });