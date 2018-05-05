/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

define(
    function () {

        /**
         * @class Edge
         * @constructor
         * @class Edge  Creates an Edge
         * @param {Object} options Rendering options for the view
         */
        var Edge = function (options) {
            /*
             Data storing structure as follows
                id: '',  ex: 'parentId_childId'
                parentId: '',
                parentType: '',
                childId: '',
                childType: '',
            */
            if (options !== undefined) {
                this.id = options.id;
                this.parentId = options.parentId;
                this.parentType = options.parentType;
                this.childId = options.childId;
                this.childType = options.childType;
            }
        };

        Edge.prototype.getId = function () {
            return this.id;
        };

        Edge.prototype.getParentId = function () {
            return this.parentId;
        };

        Edge.prototype.getParentType = function () {
            return this.parentType;
        };

        Edge.prototype.getChildId = function () {
            return this.childId;
        };

        Edge.prototype.getChildType = function () {
            return this.childType;
        };

        Edge.prototype.setId = function (id) {
            this.id = id;
        };

        Edge.prototype.setParentId = function (parentId) {
            this.parentId = parentId;
        };

        Edge.prototype.setParentType = function (parentType) {
            this.parentType = parentType;
        };

        Edge.prototype.setChildId = function (childId) {
            this.childId = childId;
        };

        Edge.prototype.setchildType = function (childType) {
            this.childType = childType;
        };

        return Edge;

    });
