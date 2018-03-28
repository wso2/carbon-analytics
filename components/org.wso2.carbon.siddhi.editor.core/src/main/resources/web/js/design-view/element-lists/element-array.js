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
         * @class ElementArray
         * @constructor
         * @class ElementArray  Creates an array for an element type
         */
        var ElementArray = function () {
        };

        ElementArray.prototype = Array.prototype;

        /**
         * Returns the searched element if found. Otherwise null will be returned.
         * @param {number} elementId id of the element
         * @returns {element}
         */
        ElementArray.prototype.getElement = function (elementId) {
            var foundElement = undefined;
            this.forEach(function(element){
                if (typeof element.getId === 'function' && element.getId() === elementId) {
                    foundElement = element;
                }
            });
            return foundElement;
        };

        /**
         * Removes an element from the array.
         * @param {number} elementId id of the element to be removed.
         */
        ElementArray.prototype.removeElement = function (elementId) {
            var self = this;
            self.every(function(element){
                if (element.id === elementId) {
                    var index = self.indexOf(element);
                    if (index > -1) {
                        self.splice(index, 1);
                    }
                }
            });
        };

        /**
         * Removes all elements from the array.
         */
        ElementArray.prototype.removeAllElements = function () {
            this.length = 0;
        };

        return ElementArray;

    });
