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

define(['require', 'jquery', 'lodash', 'log', 'dataMapperUtil'],

    function (require, $, _, log, DataMapperUtil) {
        var ScopeNode = function (data_types) {
            this.dataTypes = data_types;
            this.supportedGenericDataTypes = data_types.map(function(data_type) {
                return DataMapperUtil.getGenericDataType(data_type);
            });
            this.canBeLast = true;
            this.children = [];
            this.nodeType = 'scope';
            this.placeholder = null;
        }

        ScopeNode.prototype.constructor = ScopeNode;

        ScopeNode.prototype.addNodeToExpression = function(node) {
            this.children.push(node);
        }

        return ScopeNode;
    });
