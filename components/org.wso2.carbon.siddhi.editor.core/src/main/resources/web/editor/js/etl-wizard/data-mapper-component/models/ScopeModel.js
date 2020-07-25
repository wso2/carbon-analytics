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

        var ScopeModel = function (dataTypes) {
            this.type = 'scope';
            this.returnTypes = dataTypes;
            this.genericReturnTypes = dataTypes.map(function (dataType) {
                return DataMapperUtil.getGenericDataType(dataType);
            });

            this.rootNode = null;
        }

        ScopeModel.prototype.addNode = function(node) {
            if(this.rootNode) {
                switch (this.rootNode.type) {
                    case 'attribute':
                    case 'customValue':
                    case 'function':
                    case 'scope':
                        node.leftNode = this.rootNode;
                        this.rootNode = node;
                        break;
                    case 'operator':
                        if (this.rootNode.hasRight && this.rootNode.rightNode === null) {
                            this.rootNode.rightNode = node;
                        } else {
                            node.leftNode = this.rootNode;
                            this.rootNode = node;
                        }
                        break;
                }
            } else {
                this.rootNode = node;
            }
        }

        ScopeModel.prototype.constructor = ScopeModel;

        ScopeModel.prototype.isValid = function () {
            return this.rootNode
                && _.intersection(this.rootNode.genericReturnTypes, this.genericReturnTypes).length > 0;
        }

        return ScopeModel;
    });
