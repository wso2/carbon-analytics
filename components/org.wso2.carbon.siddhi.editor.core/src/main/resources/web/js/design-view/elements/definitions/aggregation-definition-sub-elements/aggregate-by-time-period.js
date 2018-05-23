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
         * @class AggregateByTimePeriod
         * @constructor
         * @class AggregateByTimePeriod  Creates a AggregateByTimePeriod in aggregation definition select section
         * @param {Object} options Rendering options for the view
         */
        var AggregateByTimePeriod = function (options) {
            /*
             Data storing structure as follows
                minValue*: '', // At least one value should be added, and that will be marked as the minValue
                maxValue: '' // Max value is added if the user wants to define a range of timestamps
            */
            if (options !== undefined) {
                this.minValue = (options.minValue).toUpperCase();
                this.maxValue = (options.maxValue).toUpperCase();
            }
        };

        AggregateByTimePeriod.prototype.getMinValue = function () {
            return this.minValue;
        };

        AggregateByTimePeriod.prototype.getMaxValue = function () {
            return this.maxValue;
        };

        AggregateByTimePeriod.prototype.setMinValue = function (minValue) {
            this.minValue = minValue;
        };

        AggregateByTimePeriod.prototype.setMaxValue = function (maxValue) {
            this.maxValue = maxValue;
        };

        return AggregateByTimePeriod;

    });
