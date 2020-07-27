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

define(['require', 'jquery', 'lodash', 'log', 'dataMapperUtil', 'scopeModel'],

    function (require, $, _, log, DataMapperUtil, ScopeModel) {
        var FunctionModel = function(node_data) {
            this.type = 'function';
            this.displayName = node_data.displayName;
            this.returnTypes = node_data.dataTypes;
            this.genericReturnTypes = node_data.dataTypes.map(function(data_type) {
                return DataMapperUtil.getGenericDataType(data_type);
            });
            this.selectedSyntax = node_data.selectedSyntax;
            this.allowRepetitiveParameters = false;
            this.repetitiveParameterTypes = [];
            this.parameters = this.generateParameters(node_data.selectedSyntax);
        }

        FunctionModel.prototype.constructor = FunctionModel;

        FunctionModel.prototype.generateParameters = function(syntax) {
            var parameters = [];
            var functionParameterRegExp = /\(([^)]+)\)/;
            var allowRepetitive = false;
            var repetitiveDataTypes = [];

            functionParameterRegExp.exec(syntax.syntax) ?
                functionParameterRegExp.exec(syntax.syntax)[1].split(',').forEach(function(param) {
                    var temp = param.trim().split(' ');

                    var dataTypes = temp[0].match(/<(.*?)>/)[1].split('|').map(function(type) {
                        return type.toLowerCase();
                    });

                    var placeHolder = syntax.parameterData[temp[1]];

                    if (!(temp[1].indexOf('...') > -1)) {
                        var paramNode = new ScopeModel(dataTypes);
                        paramNode.placeholder = placeHolder;

                        parameters.push(paramNode);
                    } else {
                        allowRepetitive = true;
                        repetitiveDataTypes = dataTypes;
                    }
                }) : null;

            this.allowRepetitiveParameters = allowRepetitive;
            this.repetitiveParameterTypes = repetitiveDataTypes;
            return parameters;
        }

        return FunctionModel;
    });
