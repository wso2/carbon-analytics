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

define(['require', 'jquery', 'lodash', 'log', 'alerts'],

    function (require, $, _, log, Alerts) {
        var QueryOrderByComponent = function (container, config) {
            this.__config = config;
            this.__container = container;
        }

        QueryOrderByComponent.prototype.constructor = QueryOrderByComponent;

        QueryOrderByComponent.prototype.render = function () {
            var container = this.__container;
            var config = this.__config;

            container.append(`
                Order Output by fields
                <button style="background-color: #ee6719" class="btn btn-default btn-circle" id="btn-add-orderby-property" type="button" data-toggle="dropdown">
                    <i class="fw fw-add"></i>
                </button>
                <div id="orderby-options-dropdown" class="dropdown-menu-style hidden" aria-labelledby="">
                </div>
            `);
        }

        return QueryOrderByComponent;
    });
