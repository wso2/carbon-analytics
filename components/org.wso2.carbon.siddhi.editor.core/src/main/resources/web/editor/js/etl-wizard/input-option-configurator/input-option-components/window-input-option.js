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

define(['require', 'jquery', 'lodash', 'log', 'alerts', 'app/source-editor/completion-engine'],

    function (require, $, _, log, Alerts, CompletionEngine) {
        var WindowInputOptionComponent = function (container, config) {
            var self = this;
            this.__container = container;
            this.__config = config;
            this.__extensionData = {};

            CompletionEngine.getRawMetadata().inBuilt.windowProcessors.forEach(function (window) {
                self.__extensionData[window.name] = window;
            });
        }

        WindowInputOptionComponent.prototype.constructor = WindowInputOptionComponent;

        WindowInputOptionComponent.prototype.render = function () {
            var self = this;
            var config = self.__config;
            var container = self.__container;
            var extensionData = self.__extensionData;

            container.empty();
            container.append(`
                <h3 style="margin-top: 0; color: #373737">Window configuration</h3>
                <div style="color: #373737">
                    <label for="window-type">Window type&nbsp;:&nbsp;</label>
                    <select name="window-type" id="window-type">
                        <option disabled selected value> -- select an option -- </option>
                    </select>
                </div>
                <div style="padding: 0 5px;color: #373737; margin-top: 15px" class="window-option-section">
                </div>
            `);

            Object.keys(extensionData).forEach(function (key) {
                container.find('#window-type').append(`
                    <option>${key}</option>
                `);
            });

            container.find('#window-type').on('change', function (evt) {
                var windowExtensionType = $(evt.currentTarget).val();

                config.query.window['type'] = windowExtensionType;
                config.query.window['parameters'] = {};

                extensionData[windowExtensionType].parameters
                    .filter(paramData => !paramData.optional)
                    .forEach(function (paramData) {
                        config.query.window.parameters[paramData.name] = {
                            value: paramData.defaultValue,
                            type: paramData.type
                    }
                });
                self.render();
            });

            if (config.query.window['type']) {
                container.find('#window-type').val(config.query.window.type)

                if(Object.keys(config.query.window.parameters).length > 0) {
                    container.find('.window-option-section').append(`
                        <span><b>Window Options</b></span>
                        ${
                            self.__extensionData[config.query.window.type].parameters.length !== 
                                config.query.window.parameters.length ?
                                    `
                                        <button style="background-color: #ee6719" 
                                            class="btn btn-default btn-circle" id="btn-add-window-property" 
                                            type="button" data-toggle="dropdown"
                                        >
                                            <i class="fw fw-add"></i>
                                        </button>
                                    ` : ''                                    
                        }
                        <div id="window-options-dropdown" class="dropdown-menu-style hidden" 
                            aria-labelledby="">
                        </div> 
                    `);
                }

                self.__extensionData[config.query.window.type].parameters
                    .filter((paramData) => Object.keys(config.query.window.parameters).indexOf(paramData.name) === -1)
                    .forEach(paramData => {
                        container.find('#window-options-dropdown').append(`
                            <a title="" class="dropdown-item" href="#">
                                <div>
                                    <div class="option-title">${paramData.name}</div><br/>
                                    <small style="opacity: 0.8">
                                        ${
                                            paramData.description.replaceAll('<', '&lt;')
                                                .replaceAll('>', '&gt;').replaceAll('`', '')
                                        }
                                    </small><br/>
                                    <small style="opacity: 0.8">
                                        <b>Default Value</b>: ${paramData.defaultValue.replaceAll(/\`/g, '')}
                                    </small>
                                </div>
                            </a>
                        `)
                    })

                container.find('#btn-add-window-property')
                    .on('mouseover', function (evt) {
                        var leftOffset = evt.currentTarget.offsetLeft;
                        var elementObj = container.find('#window-options-dropdown');
                        elementObj.css({"left": `${leftOffset}px`})
                        elementObj
                            .removeClass('hidden')
                            .on('mouseleave', function () {
                                elementObj.addClass('hidden');
                            });
                    })
                    .on('mouseleave', function () {
                        setTimeout(function () {
                            var elementObj = container.find('#window-options-dropdown');
                            if (!(container.find('#window-options-dropdown:hover').length > 0)) {
                                elementObj.addClass('hidden');
                            }
                        }, 300);
                    });

                container.find('#window-options-dropdown>a').on('click', function (evt) {
                    var optionName = $(evt.currentTarget).find('.option-title').text();
                    var selectedOption = extensionData[config.query.window.type].parameters
                        .filter(paramData => paramData.name === optionName)[0];

                    config.query.window.parameters[optionName] = {
                        value: selectedOption.defaultValue,
                        type: selectedOption.type
                    };

                    self.render();
                });

                Object.keys(config.query.window.parameters).forEach(function (key) {
                    paramData = extensionData[config.query.window.type]
                        .parameters.find(function(param) { return key === param.name; })

                    container.find('.window-option-section').append(`
                        <div style="width: 100%; padding-bottom: 10px; display: flex" class="input-section">
                            <div style="flex:1">
                                <label style="margin-bottom: 0; color: #373737" 
                                    class="${config.query.window.parameters[key].value.length > 0 ? '' : 'not-visible'}" 
                                    id="label-window-op-${key.replaceAll(/\./g,'-')}"
                                     for="window-op-${key.replaceAll(/\./g,'-')}">
                                        ${key}
                                </label>
                                <input 
                                    id="window-op-${key.replaceAll(/\./g, '-')}" 
                                    style="width: 100%; border: none; background-color: transparent; 
                                    border-bottom: 1px solid #373737" placeholder="${key}" 
                                    type="text" value="${config.query.window.parameters[key].value}">
                            </div>
                            <div style="padding-top:20px; cursor: pointer">
                                <i title="${paramData.description}" class="fw fw-info"></i>
                            </div>
                            ${
                                paramData.optional ?
                                    `<div style="margin-left: 5px; padding-top:20px; cursor: pointer">
                                        <i id="window-op-del-${paramData.name.replaceAll(/\./g, '-')}" class="fw fw-delete"></i>
                                    </div>` : ''
                            }
                        </div>
                    `);
                });

                container.find('.window-option-section .fw-delete')
                    .on('click', (evt) => {
                       let propertyName = evt.currentTarget.id
                           .match('window-op-del-([a-zA-Z\-0-9]+)')[1].replaceAll(/-/g, '.');

                       delete config.query.window.parameters[propertyName];
                       self.render();
                    });

                container.find('.window-option-section .input-section input')
                    .on('focus', function (evt) {
                        var inputId = evt.currentTarget.id.match('window-op-([a-zA-Z\-]+)')[1];
                        container.find(`#label-window-op-${inputId}`).removeClass('not-visible');
                        $(evt.currentTarget).attr('placeholder', 'Type here to input the value');
                    })
                    .on('focusout', function (evt) {
                        var inputId = evt.currentTarget.id.match('window-op-([a-zA-Z\-]+)')[1];
                        if($(evt.currentTarget).val().length === 0) {
                            container.find(`#label-window-op-${inputId}`).addClass('not-visible');
                            $(evt.currentTarget).attr('placeholder', inputId.replaceAll(/-/g,'.'));
                        }
                    })
                    .on('keyup', _.debounce(function (evt) {
                        var inputId = evt.currentTarget.id.match('window-op-([a-zA-Z\-]+)')[1];
                        config.query.window.parameters[inputId.replaceAll(/-/g, '.')].value = $(evt.currentTarget).val();
                    }, 100, {}))
            }
        }

        return WindowInputOptionComponent;
    });
