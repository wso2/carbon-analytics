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

define(['require', 'jquery', 'lodash', 'log', 'alerts', 'filterInputOptionComponent', 'windowInputOptionComponent', 'functionInputOptionComponent'],

    function (require, $, _, log, Alerts, FilterInputOptionsComponent, WindowInputOptionsComponent, FunctionInputOptionComponent) {
        
        var InputOptionConfigurator = function (container, config) {
            this.__container = container;
            this.__config = config;
        }

        InputOptionConfigurator.prototype.constructor = InputOptionConfigurator;

        InputOptionConfigurator.prototype.render = function () {
            var self = this;
            var container = self.__container;
            var config = self.__config;

            container.empty();
            container.append(`
                <div style="max-height: ${self.__container.offsetHeight}; flex-direction: column" class="content-section">
                    <div class="input-option-btn-group" style="display: flex;">
                        <button id="btn-add-filter" style="min-width: 90px; background: #ee6719" class="btn btn-default"><i class="fw ${Object.keys(config.query.filter).length > 0 ? 'fw-check': 'fw-error'}"></i>&nbsp;Filter</button>
                    </div>
                    <div class="input-option-container" style="display: flex; height: calc(100% - 15px)" >
                        
                    </div>
                </div>
                <div  style="max-height: ${self.__container.offsetHeight}; flex-direction: column" class="content-section">
                    <div class="input-option-btn-group" style="display: flex;">
                        <button id="btn-add-function" style="margin-left: auto; min-width: 90px; background: #ee6719" class="btn btn-default">
                            <i class="fw ${Object.keys(config.query.function).length > 0 ? 'fw-check': 'fw-error'}"></i>&nbsp;Function
                        </button>
                        <button id="btn-add-window" style="margin-left: 15px; min-width: 90px; background: #ee6719" class="btn btn-default">
                            <i class="fw ${Object.keys(config.query.window).length > 0 ? 'fw-check': 'fw-error'}"></i>&nbsp;Window
                        </button>
                    </div>
                    <div class="input-option-container" style="display: flex; height: calc(100% - 15px)" >
                        
                    </div>
                </div>
            `);

            var inputOptionSection = $('<div class="input-option-section" style="width: 100%;margin: 15px 0 15px 0;display: flex;flex-direction: column;padding:15px;background-color: rgba(162,162,162,1);"></div>');


            self.__container.find('.input-option-btn-group>button')
                .on('click', function (evt) {
                    // $(evt.currentTarget).attr('disabled', true);
                    var btnType = evt.currentTarget.id.match('btn-add-([a-z]+)')[1];

                    if(config.query[btnType]['enable']) {
                        config.query[btnType] = {};
                    } else {
                        config.query[btnType]['enable'] = true;
                    }

                    switch (btnType) {
                        case 'function':
                            config.query.window = {}
                            break;
                        case 'window':
                            config.query.function = {}
                            break;
                    }

                    self.render();
                });
            var containerSection = '';
            if (Object.keys(self.__config.query.filter).length > 0) {
                containerSection = inputOptionSection.clone();
                $(container.find('.input-option-container')[0]).append(containerSection);
                // container.find('#btn-add-filter').attr('disabled', true);
                var filterComponent = new FilterInputOptionsComponent(containerSection, config);
                filterComponent.render();
            }

            if (Object.keys(self.__config.query.window).length > 0) {
                containerSection = inputOptionSection.clone();
                $(container.find('.input-option-container')[1]).append(containerSection);
                // container.find('#btn-add-window').attr('disabled', true);
                // container.find('#btn-add-function').attr('disabled', true);
                var windowComponent = new WindowInputOptionsComponent(containerSection, config);
                windowComponent.render();
            }

            if (Object.keys(self.__config.query.function).length > 0) {
                containerSection = inputOptionSection.clone();
                $(container.find('.input-option-container')[1]).append(containerSection);
                // container.find('#btn-add-window').attr('disabled', true);
                // container.find('#btn-add-function').attr('disabled', true);
                var functionComponent = new FunctionInputOptionComponent(containerSection, config);
                functionComponent.render();
            }
        }

        return InputOptionConfigurator;
    });
