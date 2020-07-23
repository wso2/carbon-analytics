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

define(['require', 'jquery', 'lodash', 'log', 'dataMapperUtil', 'scopeNode'],

    function (require, $, _, log, DataMapperUtil, ScopeNode) {
        var FunctionNode = function(node_data) {
            this.displayName = node_data.displayName;
            this.dataTypes = node_data.dataTypes;
            this.supportedGenericDataTypes = node_data.dataTypes.map(function(data_type) {
                return DataMapperUtil.getGenericDataType(data_type);
            });
            this.nodeType = 'function';
            this.selectedSyntax = node_data.selectedSyntax;
            this.allowRepetitiveParameters = false;
            this.repetitiveParameterTypes = [];
            this.generateParameters = this.generateParameters.bind(this);
            this.parameters = this.generateParameters(node_data.selectedSyntax);
        }

        FunctionNode.prototype.constructor = FunctionNode;

        FunctionNode.prototype.generateParameters = function(syntax) {
            var parameters = [];
            var functionParameterRegexp = /\(([^)]+)\)/;
            var allowRepetitive = false;
            var repetitiveDataTypes = [];

            functionParameterRegexp.exec(syntax.syntax) ? functionParameterRegexp.exec(syntax.syntax)[1]
                .split(',').forEach(function(param) {
                var temp = param.trim().split(' ');

                var dataTypes = temp[0].match(/<(.*?)>/)[1].split('|').map(function(type) {
                    return type.toLowerCase();
                });

                var placeHolder = syntax.parameterData[temp[1]];

                if (!(temp[1].indexOf('...') > -1)) {
                    var paramNode = new ScopeNode(dataTypes);
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

        return FunctionNode;
    });
