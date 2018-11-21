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
         * @class PayloadOrAttribute
         * @constructor
         * @class PayloadOrAttribute  Creates a Payload Or Attribute object
         * @param {Object} options Rendering options for the view
         */
        var PayloadOrAttribute = function (options) {
            /*
             Data storing structure as follows
                annotationType: 'PAYLOAD' | 'ATTRIBUTES',
                type*: ‘MAP' | 'LIST’,
                value*: {Key-Value Pair JSON} | ['value1',...]

                NOTE: LIST will contain only one value in sink mapper payload.
            */
            if (options !== undefined) {
                this.annotationType
                    = (options.annotationType !== undefined) ? (options.annotationType).toUpperCase() : undefined;
                this.type = (options.type !== undefined) ? (options.type).toUpperCase() : undefined;
                this.value = options.value;
            }
        };

        PayloadOrAttribute.prototype.getAnnotationType = function () {
            return this.annotationType;
        };

        PayloadOrAttribute.prototype.getType = function () {
            return this.type;
        };

        PayloadOrAttribute.prototype.getValue = function () {
            return this.value;
        };

        PayloadOrAttribute.prototype.setAnnotationType = function (annotationType) {
            this.annotationType = annotationType.toUpperCase();
        };

        PayloadOrAttribute.prototype.setType = function (type) {
            this.type = type.toUpperCase();
        };
        PayloadOrAttribute.prototype.setValue = function (value) {
            this.value = value;
        };

        return PayloadOrAttribute;

    });
