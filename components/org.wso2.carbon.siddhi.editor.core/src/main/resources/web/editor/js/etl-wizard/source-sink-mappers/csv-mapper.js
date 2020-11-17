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

        var CSVMapper = function (type, container, extensionConfig, mapperData) {
            this.__mapperContainer = container;
            this.__mapperType = type;
            this.__extensionConfig = extensionConfig;
            this.__mapperData = mapperData;
        }

        CSVMapper.prototype.constructor = CSVMapper;

        CSVMapper.prototype.render = function () {
            var self = this;
            var container = this.__mapperContainer;
            var config = this.__extensionConfig.mapping;
            var mapperData = this.__mapperData;

            container.empty();
            container.append(`
                <div id="source-mapper-configurator">
                    <div style="padding-top: 10px">
                        <div style="padding-top: 15px" class="attribute-list">
                            <div>
                                Source Mapper configuration
                                ${mapperData.parameters.length !== config.properties.length ? 
                                    `<button 
                                        style="background-color: #ee6719" 
                                        class="btn btn-default btn-circle" 
                                        id="btn-add-source-mapper-property" 
                                        type="button" 
                                        data-toggle="dropdown">
                                            <i class="fw fw-add"></i>
                                    </button>`
                                    : ''}
                                <div 
                                    id="source-mapper-option-dropdown" 
                                    style="left: 150px" 
                                    class="dropdown-menu-style hidden" aria-labelledby="">
                                </div>
                            </div>
                            <div style="" class="source-mapper-options">
                            </div>
                            <div class="custom-mapping-section">
                                <div style="display: flex; padding-top:15px">
                                    <div style="padding-top: 5px">
                                        Enable custom attribute mapping                                        
                                    </div>
                                    <div style="margin-left: 15px">
                                        <div id="btn-group-enable-custom-map" class="btn-group btn-group-toggle" 
                                            data-toggle="buttons">
                                            <label class="btn" 
                                                    style="${
                                                        config.customEnabled ?
                                                            "background-color: rgb(91,203,92); color: white;"
                                                            : "background-color: rgb(100,109,118); color: white;"}" 
                                             >
                                                <input type="radio" name="options" id="enable" autocomplete="off"> 
                                                <i class="fw fw-check"></i>
                                            </label>
                                            <label class="btn" 
                                                    style="${
                                                        !config.customEnabled ?
                                                            "background-color: red; color: white;"
                                                            : "background-color: rgb(100,109,118); color: white;"}" 
                                            >
                                                <input type="radio" name="options" id="disable" autocomplete="off"> 
                                                <i class="fw fw-cancel"></i>
                                            </label>
                                        </div>
                                    </div>
                                </div>
                                <div class="attrib-section">
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `)

            mapperData.parameters
                .filter(function(param) {
                    return !config.properties[param.name];
                }).forEach(function(param) {
                    container.find('#source-mapper-option-dropdown').append(`
                        <a title="" class="dropdown-item" href="#">
                            <div class="mapper-option">${param.name}</div><br/>
                            <small style="opacity: 0.8">
                                ${param.description.replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('`', '')}
                            </small><br/>
                            <small style="opacity: 0.8">
                                <b>Default value</b>: ${param.defaultValue}
                            </small>
                        </a>
                    `);
                })

            Object.keys(config.properties).forEach(function (key) {
                var optionData = config.properties[key];
                var name = key.replaceAll(/\./g, '-');
                var selectedOption = mapperData.parameters.find(function(param) {return param.name === name})
                container.find('.source-mapper-options').append(`
                    <div style="display: flex; margin-bottom: 15px" class="mapper-option">
                            <div style="width: 100%" class="input-section">
                                <label 
                                    style="margin-bottom: 0" 
                                    class="${optionData.value.length > 0 ? 
                                                '' : 'not-visible'}" 
                                    id="label-mapper-op-${name}" for="mapper-op-${name}">${key}</label>
                                <input id="mapper-op-${name}" 
                                    style="width: 100%; border: none; 
                                        background-color: transparent; border-bottom: 1px solid #333" 
                                    placeholder="${key}" type="text" value="${optionData.value}">
                            </div>
                            <div style="display: flex;padding-top: 20px; padding-left: 5px;" class="delete-section">
                                <a style="margin-right: 5px; color: #333" 
                                title="${selectedOption.description.replaceAll('<', '&lt;')
                                            .replaceAll('>', '&gt;').replaceAll('`', '')}">
                                    <i class="fw fw-info"></i>    
                                </a>  
                                ${
                                    selectedOption.optional ?
                                        `<a style="color: #333">
                                            <i id="mapper-op-del-${name}" class="fw fw-delete"></i>    
                                         </a>` : ''
                                }                              
                            </div>
                        </div>
                `);
            });

            container.find('.mapper-option>.input-section>input')
                .on('focus', function (evt) {
                    var labelId = `label-${evt.currentTarget.id}`;
                    container.find(`#${labelId}`).removeClass('not-visible');
                    $(evt.currentTarget).attr('placeholder', 'Type here to enter value');
                })
                .on('focusout', function (evt) {
                    var labelId = `label-${evt.currentTarget.id}`;
                    if($(evt.currentTarget).val().length === 0) {
                        container.find(`#${labelId}`).addClass('not-visible');
                        $(evt.currentTarget).attr('placeholder', container.find(`#${labelId}`).text());
                    }
                })
                .on('keyup', _.debounce(function (evt) {
                    var propertyName = evt.currentTarget.id.match('mapper-op-([a-zA-Z-]+)')[1].replaceAll(/-/g, '.');
                    config.properties[propertyName].value = $(evt.currentTarget).val();
                }, 100, {}));

            container.find('.mapper-option>.delete-section>a>.fw-delete').on('click', function (evt) {
                var attribName = evt.currentTarget.id.match('mapper-op-del-([a-zA-Z-]+)')[1].replaceAll(/-/g, '.');
                delete config.properties[attribName];
                self.render();
            })

            container.find('#btn-add-source-mapper-property')
                .on('mouseover', function (evt) {
                    var dropDownMenu = container.find('#source-mapper-option-dropdown');
                    var leftOffset = evt.currentTarget.offsetLeft;
                    dropDownMenu.css({"left": `${leftOffset}px`})
                    dropDownMenu.removeClass('hidden');
                    dropDownMenu.on('mouseleave', function () {
                        dropDownMenu.addClass('hidden');
                    });
                })
                .on('mouseleave', function (evt) {
                    setTimeout(function () {
                        var dropDownMenu = container.find('#source-mapper-option-dropdown');
                        if(!(container.find('#source-mapper-option-dropdown:hover').length > 0)) {
                            dropDownMenu.addClass('hidden');
                        }
                    }, 300);
                });

            container.find('.dropdown-item').on('click', function (evt) {
                var optionName = $(evt.currentTarget).find('.mapper-option').text();
                var selectedOption = mapperData.parameters.find(function(param) {return param.name === optionName})

                config.properties[optionName] = {
                    value: selectedOption.defaultValue,
                    type: selectedOption.type
                };
                self.render();
            });

            container.find('#btn-group-enable-custom-map .btn').on('click', function (evt) {
                config.customEnabled = !config.customEnabled;

                if (!config.customEnabled) {
                    config.attributes = {};
                }

                self.render();
            });

            if (config.customEnabled) {
                var extensionConfig = this.__extensionConfig;

                extensionConfig.stream.attributes.forEach(function (attrib, i) {
                    config.attributes[attrib.name] = i;
                    container.find('.custom-mapping-section>.attrib-section').append(`
                        <div style="width: 100%; padding-bottom: 5px" class="attribute-map">
                            <label style="margin-bottom: 0" 
                                class="" id="" for="index-${attrib.name}">${attrib.name}</label>
                            <input 
                                id="index-${attrib.name}" 
                                style="width: 100%; border: none; background-color: transparent; 
                                border-bottom: 1px solid #333" placeholder="" type="text" value="${i}">
                        </div>
                    `);

                    container.find('.custom-mapping-section>.attrib-section>.attribute-map>input')
                        .on('keyup', function (evt) {
                            var attributeName = evt.currentTarget.id.match('index-([a-zA-Z_]+)')[1];
                            config[attributeName] = $(evt.currentTarget).val();
                        });
                });
            }
        }

        return CSVMapper;
    });
