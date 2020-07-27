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

        var TextMapper = function (type, container, mapConfig, extensionData) {
            this.__mapperContainer = container;
            this.__mapperType = type;
            this.__extensionConfig = mapConfig;
            this.__regexGroupNumber = 0;
            this.__extensionData = extensionData;
        }

        TextMapper.constructor = TextMapper;

        TextMapper.prototype.render = function () {
            var self = this;
            var container = this.__mapperContainer;
            var config = this.__extensionConfig.mapping;
            var extensionData = this.__extensionData;

            container.empty();
            container.append(`
                <div id="source-mapper-configurator">
                    <div style="padding-top: 10px">
                        <div style="padding-top: 15px" class="attribute-list">
                            <div>
                              Source Mapper configuration
                              ${extensionData.parameters.length !== config.properties.length ? 
                                `<button 
                                    style="background-color: #ee6719" 
                                    class="btn btn-default btn-circle" 
                                    id="btn-add-source-mapper-property" 
                                    type="button" data-toggle="dropdown">
                                        <i class="fw fw-add"></i>
                                </button>`
                                : ''}
                              <div 
                                id="source-mapper-option-dropdown" 
                                style="left: 150px" class="dropdown-menu-style hidden" aria-labelledby="">
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
            `);

            delete config.properties['regex.groupid'];

            extensionData.parameters
                .filter(function(property) {
                    return !(property.name.startsWith('regex.'));
                })
                .forEach(function(property) {
                    if (!config.properties[property.name]) {
                        container.find('#source-mapper-option-dropdown').append(`
                            <a title="" class="dropdown-item" href="#">
                                <div class="mapper-option">${property.name}</div><br/>
                                <small style="opacity: 0.8">
                                    ${property.description.replaceAll('<', '&lt;')
                                        .replaceAll('>', '&gt;').replaceAll('`', '')}
                                </small><br/>
                                <small style="opacity: 0.8"><b>Default value</b>: ${property.defaultValue}</small>
                            </a>
                        `);
                    }
                });

            Object.keys(config.properties)
                .filter(function (property) {
                    return !(property.startsWith('regex.'));
                }).forEach(function (key) {
                    var optionData = config.properties[key];
                    var optionFullData = self.__extensionData.parameters.find(function(param) {
                        return param.name === key;
                    })
                    var name = key.replaceAll(/\./g, '-');
                    container.find('.source-mapper-options').append(`
                        <div style="display: flex; margin-bottom: 15px" class="mapper-option">
                                <div style="width: 100%" class="input-section">
                                    <label 
                                        style="margin-bottom: 0" 
                                        class="${optionData.value.length > 0 ? '' : 'not-visible'}" 
                                        id="label-mapper-op-${name}" for="mapper-op-${name}">${key}</label>
                                    <input id="mapper-op-${name}" 
                                        style="width: 100%; border: none; background-color: transparent; 
                                        border-bottom: 1px solid #333" placeholder="${key}" 
                                        type="text" value="${optionData.value}">
                                </div>
                                <div style="display: flex;padding-top: 20px; padding-left: 5px;" class="delete-section">
                                    <a style="margin-right: 5px; color: #333" 
                                    title="${optionFullData.description.replaceAll('<', '&lt;')
                                                .replaceAll('>', '&gt;').replaceAll('`', '')}">
                                        <i class="fw fw-info"></i>    
                                    </a>  
                                    ${
                                        optionFullData.optional ?
                                            `<a style="color: #333">
                                                <i id="mapper-op-del-${name}" class="fw fw-delete"></i>    
                                             </a>` : ''
                                    }                              
                                </div>
                            </div>
                    `);
                });

            container.find('#btn-add-source-mapper-property')
                .on('mouseover', function (evt) {
                    var dropDownMenu = container.find('#source-mapper-option-dropdown');
                    var leftOffset = evt.currentTarget.offsetLeft;
                    dropDownMenu.css({ "left": `${leftOffset}px` })
                    dropDownMenu.removeClass('hidden');
                    dropDownMenu.on('mouseleave', function () {
                        dropDownMenu.addClass('hidden');
                    });
                })
                .on('mouseleave', function (evt) {
                    setTimeout(function () {
                        var dropDownMenu = container.find('#source-mapper-option-dropdown');
                        if (!(container.find('#source-mapper-option-dropdown:hover').length > 0)) {
                            dropDownMenu.addClass('hidden');
                        }
                    }, 300);
                });

            container.find('.dropdown-item').on('click', function (evt) {
                var optionName = $(evt.currentTarget).find('.mapper-option').text();
                var extensionData = self.__extensionData.parameters.find(function(param) {
                    return param.name === optionName;
                })

                config.properties[optionName] = {}
                config.properties[optionName].value = extensionData.defaultValue;
                config.properties[optionName].type = extensionData.type;
                self.render();
            });

            container.find('.mapper-option>.input-section>input')
                .on('focus', function (evt) {
                    var labelId = `label-${evt.currentTarget.id}`;
                    container.find(`#${labelId}`).removeClass('not-visible');
                    $(evt.currentTarget).attr('placeholder', 'Type here to enter value');
                })
                .on('focusout', function (evt) {
                    var labelId = `label-${evt.currentTarget.id}`;
                    if ($(evt.currentTarget).val().length === 0) {
                        container.find(`#${labelId}`).addClass('not-visible');
                        $(evt.currentTarget).attr('placeholder', container.find(`#${labelId}`).text());
                    }
                })
                .on('keyup', _.debounce(function (evt) {
                    var propertyName = evt.currentTarget.id
                        .match('mapper-op-([a-zA-Z0-9\-]+)')[1].replaceAll(/-/g, '.');
                    config.properties[propertyName].value = $(evt.currentTarget).val();
                }, 100, {}));

            container.find('.mapper-option>.delete-section>a>.fw-delete').on('click', function (evt) {
                var attribName = evt.currentTarget.id.match('mapper-op-del-([a-zA-Z0-9\-]+)')[1].replaceAll(/-/g, '.');
                delete config.properties[attribName];
                self.render();
            });

            container.find('#btn-group-enable-custom-map .btn').on('click', function (evt) {
                config.customEnabled = !config.customEnabled;

                if (!config.customEnabled) {
                    config.attributes = {};
                    Object.keys(config.properties)
                        .filter((key) => key.startsWith('regex\.'))
                        .forEach(key => {
                           delete config.properties[key];
                        });
                }

                self.render();
            });

            if (config.customEnabled) {
                container.append(`
                    <div 
                        style="display: flex; padding-top: 10px; flex-direction: column" 
                        class="sample-payload-submit-section">
                    </div>
                `);
                if (self.__mapperType === 'sink') {
                    self.renderSinkMapper();
                } else {
                    self.renderSourceMapper();
                }
            }
        }

        TextMapper.prototype.renderSourceMapper = function () {
            var self  = this;
            var container = this.__mapperContainer;
            var config = this.__extensionConfig;
            var regexIDOption = this.__extensionData.parameters.find(function(param) {
                return param.name === 'regex.groupid';
            });

            container.append(`
                <div style="padding-top: 10px" class="regex-group-section">
                </div>
                <div style="padding-top: 10px" class="custom-attribute-section">
                    Attribute mappings:
                </div>
            `);

            container.find('.sample-payload-submit-section').append(`
                <div style="display: flex; width: 100%" >
                    <span style="width: 100%">Regex Groups
                        <i style="padding: 0 10px;" title="${regexIDOption.description}" class="fw fw-info"></i>
                    </span>
                    ${
                        self.__regexGroupNumber < 26 ? 
                            `<button 
                                style="padding: 2px 12px; background: #607c8b;" 
                                class="btn btn-default" id="btn-add-regex-group" 
                                type="button"
                            >
                                Add Regex Group
                             </button>` : ''    
                     }
                </div>
            `);

            Object.keys(config.mapping.properties)
                .filter(function (property) {
                    return property.startsWith('regex.');
                })
                .forEach(function (key) {
                    container.find('.regex-group-section').append(`
                        <div style="display: flex; padding-bottom: 15px">
                            <div style="width: 30%" class="input-section">
                                <label 
                                    style="margin-bottom: 0; 
                                    font-size: 1.2rem" 
                                    class="" id="" for="regex-id-${key.replaceAll(/\./g,'-')}"
                                >
                                    Regex GroupID
                                </label>
                                <input id="regex-id-${key.replaceAll(/\./g,'-')}" 
                                    style="width: 100%; border: none; background-color: transparent; 
                                        border-bottom: 1px solid #333" 
                                    placeholder="" type="text" value="${key}" disabled>
                            </div>
                            <div style="width: 70%; margin-left: 15px" class="input-section">
                                <label 
                                    style="margin-bottom: 0; font-size: 1.2rem" 
                                    class="" id="" 
                                    for="regex-inp-${key.replaceAll(/\./g,'-')}"
                                >
                                    Regex Expression
                                </label>
                                <input id="regex-inp-${key.replaceAll(/\./g,'-')}" 
                                    class="regex-input-field" 
                                    style="width: 100%; border: none; background-color: transparent; 
                                        border-bottom: 1px solid #333" 
                                    placeholder="Type regex expression here" 
                                    type="text" value="${config.mapping.properties[key].value}">
                            </div>
                            <div style="cursor: pointer;" class="regex-group-del-section">
                                <i id="delete-regex-${key.replaceAll(/\./g,'-')}" 
                                    style="padding: 15px" class="fw fw-delete" ></i>    
                            </div>
                        </div>
                    `);
                });

            container.find('#btn-add-regex-group').on('click', function () {
                if (self.__regexGroupNumber < 26) {
                    var availableCharacters = {};

                    for (let i = 0; i < 26 ; i++) {
                        availableCharacters[i] = String.fromCharCode(65+i);
                    }

                    Object.keys(config.mapping.properties)
                        .filter(function (key) {
                            return key.startsWith('regex\.');
                        }).map(function (key) {
                            return key.match('regex\.([A-Z])')[1].charCodeAt(0);
                        }).forEach(function (key) {
                            delete availableCharacters[key-65];
                        });

                    config.mapping.properties[`regex.${availableCharacters[Object.keys(availableCharacters)[0]]}`] = {
                        value : '',
                        type: regexIDOption.type
                    };

                    self.__regexGroupNumber++;
                    self.render();
                }
            });

            container.find('.regex-input-field').on('keyup', _.debounce(function (evt) {
                var regexGroupID = evt.currentTarget.id.match('regex-inp-([a-zA-Z\-]+)')[1].replaceAll(/-/g, '.');
                config.mapping.properties[regexGroupID].value = $(evt.currentTarget).val();
            }, 200, {}));

            container.find('.regex-group-del-section>i').on('click', function (evt) {
                var regexGroupID = evt.currentTarget.id.match('delete-regex-([a-zA-Z\-]+)')[1].replaceAll(/-/g, '.');
                delete config.mapping.properties[regexGroupID];
                self.__regexGroupNumber--;
                self.render();
            });

            if(self.__regexGroupNumber > 0) {
                config.stream.attributes.forEach(function (attrib) {
                    config.mapping.attributes[attrib.name] = '';
                    container.find('.custom-attribute-section').append(`
                        <div style="width: 100%; padding-bottom: 5px" class="attribute-map">
                            <label style="margin-bottom: 0" class="" id="" 
                                for="index-${attrib.name}">${attrib.name}</label>
                            <input id="index-${attrib.name}" 
                                style="width: 100%; border: none; background-color: transparent; 
                                    border-bottom: 1px solid #333" placeholder="" type="text" value="">
                        </div>
                    `);

                    container.find('.custom-attribute-section>.attribute-map>input').on('keyup', function (evt) {
                        var attributeName = evt.currentTarget.id.match('index-([a-zA-Z_]+)')[1];
                        config.mapping.attributes[attributeName] = $(evt.currentTarget).val();
                    });
                });
            }
        }

        TextMapper.prototype.renderSinkMapper = function () {
            var self  = this;
            var container = this.__mapperContainer;
            var config = this.__extensionConfig;

            container.find('.sample-payload-submit-section')
                .append(`
                    <div style="width: 100%; padding-bottom: 10px">
                        Type the payload in the textarea<br/>
                        hover over the (+) button to add attributes
                    </div>
                    <div class="components" style="display: flex; width: 100%">
                        <div class="add-attribute-section">
                            <button style="background-color: #ee6719" class="btn btn-default btn-circle" 
                                id="btn-add-map-attribute" type="button" data-toggle="dropdown">
                                <i class="fw fw-add"></i>
                            </button>    
                            <div class="dropdown-menu-style hidden"></div>
                        </div>
                        <div style="width: 100%; padding-left: 5px;" class="text-area-section" >
                            <textarea style="width: 100%;" name="" id=""></textarea>
                        </div>
                    </div>
                `);

            config.stream.attributes.forEach(function (attribute) {
                container.find('.sample-payload-submit-section>.components>.add-attribute-section>.dropdown-menu-style')
                    .append(
                        `
                            <a id="attr-map-${attribute.name}" title="" class="dropdown-item" href="#">
                                <div class="mapper-option">${attribute.name}</div><br/>
                                <small style="opacity: 0.8">${attribute.type}</small>
                            </a>
                        `
                    )
            });

            container.find('.sample-payload-submit-section textarea').on('keyup', function () {
                var textAreaElement = container.find('.sample-payload-submit-section textarea');
                config.mapping.payload = textAreaElement.val();
            });

            container.find('.sample-payload-submit-section .dropdown-item')
                .on('click', function (evt) {
                    var attributeName = evt.currentTarget.id.match('attr-map-([a-zA-Z0-9_]+)')[1];
                    var textAreaElement = container.find('.sample-payload-submit-section textarea');

                    var start = textAreaElement[0].selectionStart;
                    var finish = textAreaElement[0].selectionEnd;
                    var allText = textAreaElement[0].value;

                    var newText = `${allText.substring(0, start)}{{ ${attributeName} }}${allText.substring(finish)}`;

                    textAreaElement.val(newText);
                    config.mapping.payload = textAreaElement.val();
                })

            container.find('#btn-add-map-attribute')
                .on('mouseover', function (evt) {
                    var dropdownMenu = container.find('.sample-payload-submit-section .dropdown-menu-style');
                    var leftOffset = evt.currentTarget.offsetLeft;

                    dropdownMenu.css({'left': `${leftOffset}px`});
                    dropdownMenu.removeClass('hidden');

                    dropdownMenu.on('mouseleave', function () {
                        dropdownMenu.addClass('hidden');
                    })

                })
                .on('mouseleave', function (evt) {
                    setTimeout(function () {
                        var dropDownMenu = container.find('.sample-payload-submit-section .dropdown-menu-style');

                        if(!(container.find('.sample-payload-submit-section .dropdown-menu-style:hover')).length > 0) {
                            dropDownMenu.addClass('hidden');
                        }

                    }, 300);
                });
        }

        return TextMapper;
    });
