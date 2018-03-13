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

define(['require'],
    function (require) {

    //TODO: remove unwanted requrirejs imports
        /**
         * @class Partition
         * @constructor
         * @class Partition  Creates a Partition
         * @param {Object} options Rendering options for the view
         */
        var Partition = function (options) {
            /*
             Data storing structure as follows.

                "partition": {
                   "with" :[] // this will contain json objects { stream : '', property :''}
                },
                "queries" :[]
            */
            this.id = options.id;
            this.partition = options.partition;
            this.queries = options.queries;
        };

        Partition.prototype.getId = function () {
            return this.id;
        };

        Partition.prototype.getPartition = function () {
            return this.partition;
        };

        Partition.prototype.getQueries = function () {
            return this.queries;
        };

        Partition.prototype.setId = function (id) {
            this.id = id;
        };

        Partition.prototype.setPartition = function (partition) {
            this.partition = partition;
        };

        Partition.prototype.setQueries = function (queries) {
            this.queries = queries;
        };

        return Partition;

    });
