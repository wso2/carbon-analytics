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
         * @class ElementUtils
         * @constructor
         * @class ElementUtils  Contains utility methods regard to elements
         */
        var ElementUtils = function () {

        };

        /**
         * Returns the searched element if found from a given array. Otherwise undefined will be returned.
         * @param array Array which the elementId is needed to be found.
         * @param elementId Id of the element.
         * @returns {element}
         */
        ElementUtils.prototype.getElement = function (array, elementId) {
            var foundElement = undefined;
            array.forEach(function(element){
                if (typeof element.getId === 'function' && element.getId() === elementId) {
                    foundElement = element;
                }
            });
            return foundElement;
        };

        /**
         * Removes an element from the array.
         * @param array Array which the element is needed to be removed.
         * @param elementId Id of the element to be removed.
         * @returns boolean returns whether element is removed or not
         */
        ElementUtils.prototype.removeElement = function (array, elementId) {
            var isElementRemoved = false;
            array.forEach(function(element){
                if (element.id === elementId) {
                    var index = array.indexOf(element);
                    if (index > -1) {
                        array.splice(index, 1);
                        isElementRemoved = true;
                    }
                }
            });
            return isElementRemoved;
        };

        /**
         * Removes all elements from the array.
         * @param array Given array.
         */
        ElementUtils.prototype.removeAllElements = function (array) {
            array.length = 0;
        };

        return ElementUtils;

    });
