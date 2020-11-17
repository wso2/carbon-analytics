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

define(['require', 'jquery', 'lodash', 'log', 'alerts', 'groupByConfigurator', 'orderByConfigurator', 'advancedOutputConfiguration'],

    function (require, $, _, log, Alerts, GroupByComponent, OrderByComponent, AdvancedOutputOptionsComponent) {

        var OutputConfigurator = function (container, config) {
            this.__container = container;
            this.__config = config;
        }

        OutputConfigurator.prototype.constructor = OutputConfigurator;

        OutputConfigurator.prototype.render = function () {
            var self = this;
            var container = self.__container;
            var config = self.__config;

            container.append(`
                <div style="max-height: ${container[0].offsetHeight}; flex-direction: column; overflow-y:auto" 
                    class="content-section">
                    <!-- Group by section -->
                </div>
                <div style="max-height: ${container[0].offsetHeight}; flex-direction: column" class="content-section">
                    <!-- Order by section -->
                </div>
                <div style="max-height: ${container[0].offsetHeight}; flex-direction: column" class="content-section">
                    Advanced Output Options
                </div>
            `);



            new GroupByComponent($(container.find('.content-section')[0]), config, container).render();
            new OrderByComponent($(container.find('.content-section')[1]), config).render();
            new AdvancedOutputOptionsComponent($(container.find('.content-section')[2]), config).render();
        }

        return OutputConfigurator;
    });
