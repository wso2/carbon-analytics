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
         * @class StoreAnnotation
         * @constructor
         * @class StoreAnnotation  Creates a Store Annotation
         * @param {Object} options Rendering options for the view
         */
        var StoreAnnotation = function (options) {
            /*
             Data storing structure as follows
                type*: ‘’,
                options*: {Key-Value Pair JSON}
            */
            if (options !== undefined) {
                this.type = options.type;
                this.options = options.options;
            }
        };

        StoreAnnotation.prototype.getType = function () {
            return this.type;
        };

        StoreAnnotation.prototype.getOptions = function () {
            return this.options;
        };

        StoreAnnotation.prototype.setType = function (type) {
            this.type = type;
        };

        StoreAnnotation.prototype.setOptions = function (options) {
            this.options = options;
        };

        return StoreAnnotation;

    });
