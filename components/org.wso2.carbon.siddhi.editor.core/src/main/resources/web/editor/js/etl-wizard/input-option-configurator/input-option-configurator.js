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
                <div style="max-height: ${self.__container.offsetHeight}; 
                    flex-direction: column; margin: 0;margin-right:5px; background: rgb(162 162 162)" 
                    class="content-section">
                    
                    ${
                        Object.keys(self.__config.query.filter).length > 0 ?
                            `                    
                            <div 
                                class="input-option-container-filter" 
                                style="display: flex; height: calc(100%); flex-direction: column" >
                                <div style="display:flex;">
                                    <div style="flex:1;color: transparent">placeholder</div>
                                    <div class="grey-on-hover" style="padding: 5px; text-align: center;">
                                        <a id="btn-add-filter" class="enable-input-option" style="color: #222">
                                            <div><i class="fw fw-cancel"></i></div
                                            <div>Cancel</div>
                                        </a>
                                    </div>
                                </div>
                            </div>`
                            : `
                                <div style="height: 100%; width: 100%;">
                                    <div style="display:table; height: 100%; width: 100%;">
                                        <div style="display:table-cell; vertical-align: middle;">
                                            <div style="display: flex; width: fit-content; 
                                                margin-left: auto; margin-right: auto;">
                                                <a  id="btn-add-filter" 
                                                    class='enable-input-option grey-on-hover' style="color: #222;">
                                                    <div style="padding: 15px;">
                                                        <img src="/editor/images/filter-query.svg" 
                                                            class="tool-image">
                                                        <div style="text-align:center">
                                                            Apply Filter
                                                        </div>
                                                    </div>
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            `
                    }
                </div>
                <div  style="max-height: ${self.__container.offsetHeight}; flex-direction: column; margin: 0;margin-left:5px; background: rgb(162 162 162)" class="content-section">
                ${
                    Object.keys(self.__config.query.window).length > 0 || Object.keys(self.__config.query.function).length > 0 ?
                        `
                            <div class="input-option-container-${Object.keys(config.query.window).length > 0 ? 
                                'window' : 'function'}" 
                                style="display: flex; height: calc(100% - 15px); flex-direction: column" >
                                <div style="display:flex;">
                                    <div style="flex:1;color: transparent">placeholder</div>
                                    <div class="grey-on-hover" style="padding: 5px; text-align: center;">
                                        <a id="btn-add-${Object.keys(config.query.window).length > 0 ? 
                                            'window' : 'function'}" class="enable-input-option" style="color: #222">
                                            <div><i class="fw fw-cancel"></i></div
                                            <div>Cancel</div>
                                        </a>
                                    </div>
                                </div>
                            </div>
                        ` : `
                            <div style="height: 100%; width: 100%; display:flex; flex-direction:column">
                                <div style="display:table; height: 100%; width: 100%;">
                                    <div style="display:table-cell; vertical-align: middle;">
                                        <div style="display: flex; width: fit-content; 
                                            margin-left: auto; margin-right: auto;">
                                            <a  id="btn-add-window" 
                                                class='enable-input-option grey-on-hover' style="color: #222;flex:1">
                                                <div style="padding: 15px;">
                                                    <div style="vertical-align:middle">
                                                        <img src="/editor/images/window-query.svg" class="tool-image">
                                                        <div style="text-align:center">
                                                            Aggregate Input
                                                        </div>
                                                    </div>
                                                </div>
                                            </a>
                                            <a  id="btn-add-function" 
                                                class='enable-input-option grey-on-hover' style="color: #222;flex:1">
                                                <div style="padding: 15px;">
                                                    <div style="vertical-align:middle">
                                                        <img src="/editor/images/function-query.svg" class="tool-image">
                                                        <div style="text-align:center">
                                                            Apply Stream Processor Function
                                                        </div>
                                                    </div>
                                                </div>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        `
                }
                    
                </div>
            `);

            var inputOptionSection = $('<div class="input-option-section" ' +
                'style="width: 100%;display: flex;flex: 1;flex-direction: column;' +
                'padding:15px;background-color: rgba(162,162,162,1);"></div>');

            self.__container.find('.enable-input-option')
                .on('click', function (evt) {
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
                container.find('.input-option-container-filter').append(containerSection);
                var filterComponent = new FilterInputOptionsComponent(containerSection, config, self.toggleInputOption);
                filterComponent.render();
            }

            if (Object.keys(self.__config.query.window).length > 0) {
                containerSection = inputOptionSection.clone();
                container.find('.input-option-container-window').append(containerSection);
                var windowComponent = new WindowInputOptionsComponent(containerSection, config);
                windowComponent.render();
            }

            if (Object.keys(self.__config.query.function).length > 0) {
                containerSection = inputOptionSection.clone();
                container.find('.input-option-container-function').append(containerSection);
                var functionComponent = new FunctionInputOptionComponent(containerSection, config);
                functionComponent.render();
            }
        }

        return InputOptionConfigurator;
    });
