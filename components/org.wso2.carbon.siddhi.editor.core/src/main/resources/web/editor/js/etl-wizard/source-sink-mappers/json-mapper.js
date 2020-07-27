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

        var JSONMapper = function (type, container, mapConfig, mapperData) {
            this.__mapperContainer = container;
            this.__mapperType = type;
            this.__extensionConfig = mapConfig;
            this.__hoveredEl = '';
            this.__mapperData = mapperData;
        }

        JSONMapper.prototype.constructor = JSONMapper;

        JSONMapper.prototype.render = function () {
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
                                    `<button style="background-color: #ee6719" 
                                        class="btn btn-default btn-circle" id="btn-add-source-mapper-property" 
                                        type="button" data-toggle="dropdown">
                                            <i class="fw fw-add"></i>
                                    </button>`
                                    : ''}
                                <div id="source-mapper-option-dropdown" 
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

            mapperData.parameters
                .filter(function(param) {
                    return !config.properties[param.name];
                }).forEach(function(param) {
                    container.find('#source-mapper-option-dropdown').append(`
                        <a title="" class="dropdown-item" href="#">
                            <div class="mapper-option">${param.name}</div><br/>
                            <small style="opacity: 0.8">${param.description
                        .replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('`', '')}</small><br/>
                            <small style="opacity: 0.8"><b>Default value</b>: ${param.defaultValue}</small>
                        </a>
                    `);
                })

            Object.keys(config.properties).forEach(function (key) {
                var optionData = config.properties[key];
                var name = key.replaceAll(/\./g, '-');
                var selectedOption = mapperData.parameters.find(function(param) {return param.name === key})
                container.find('.source-mapper-options').append(`
                    <div style="display: flex; margin-bottom: 15px" class="mapper-option">
                            <div style="width: 100%" class="input-section">
                                <label 
                                    style="margin-bottom: 0" 
                                    class="${optionData.value.length > 0 ? '' : 'not-visible'}" 
                                    id="label-mapper-op-${name}" for="mapper-op-${name}">${key}</label>
                                <input 
                                    id="mapper-op-${name}" 
                                    style="width: 100%; border: none; background-color: transparent;
                                     border-bottom: 1px solid #333" placeholder="${key}" 
                                     type="text" value="${optionData.value}"
                                >
                            </div>
                            <div style="display: flex;padding-top: 20px; padding-left: 5px;" class="delete-section">
                                <a 
                                    style="margin-right: 5px; color: #333" 
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
                var selectedOption = mapperData.parameters.find(function(param) {return param.name === optionName})

                config.properties[optionName] = {
                    value: selectedOption.defaultValue,
                    type: selectedOption.type
                };
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
                }
                self.render();
            });

            if (config.customEnabled) {
                this.renderCustomMapper();
            }
        }

        JSONMapper.prototype.updateConfigPayload = function () {
            var self = this;
            var config = this.__extensionConfig;

            var parsedJSON = JSON.parse(config.mapping.samplePayload);
            config.mapping.payload = self.generateSinkPayload(parsedJSON, '');
        }

        JSONMapper.prototype.renderCustomMapper = function () {
            var self = this;
            var container = this.__mapperContainer;
            var config = this.__extensionConfig;
            var hoveredEl = this.__hoveredEl;

            if(self.__mapperType === 'source') {
                if(config.mapping.samplePayload.length === 0 && Object.keys(config.mapping.attributes).length === 0) {
                    container.append(`
                        <div style="display: flex; padding-top: 10px" class="sample-payload-submit-section">
                            <textarea 
                                id="sample-payload-submit-input" style="height: 100px; width: 100%; border: none; 
                                    background-color: transparent; border: 1px solid #333" 
                                placeholder="Enter the sample payload here" type="text" value=""></textarea>
                            <div style="margin-left: 5px;">
                                <button style="background-color: #ee6719" 
                                    class="btn btn-default btn-add-sample-payload">Submit</button>
                            </div>
                        </div>
                    `);
                } else {
                    container.append(`
                        <div style="display: flex; padding-top: 10px" class="sample-payload-submit-section">
                            <div>
                                <button style="background-color: #ee6719" class="btn btn-default btn-clear-mapping">
                                    Clear
                                </button>
                            </div>
                        </div>
                    `);
                }
            } else {
                if(config.mapping.samplePayload.length === 0 && config.mapping.payload.length === 0) {
                    container.append(`
                        <div style="display: flex; padding-top: 10px" class="sample-payload-submit-section">
                            <textarea 
                                id="sample-payload-submit-input" style="height: 100px; 
                                width: 100%; border: none; background-color: transparent; border: 1px solid #333" 
                                placeholder="Enter the sample payload here" type="text" value=""></textarea>
                            <div style="margin-left: 5px;">
                                <button 
                                    style="background-color: #ee6719" 
                                    class="btn btn-default btn-add-sample-payload">Submit</button>
                            </div>
                        </div>
                    `);
                } else {
                    container.append(`
                        <div style="display: flex; padding-top: 10px" class="sample-payload-submit-section">
                            <div>
                                <button style="background-color: #ee6719" class="btn btn-default btn-clear-mapping">
                                    Clear
                                </button>
                            </div>
                        </div>
                    `);
                }
            }

            container.find('.btn-clear-mapping')
                .on('click', function(evt) {
                    config.mapping.samplePayload = '';
                    config.mapping.attributes = {};
                    if(self.__mapperType === 'sink') {
                        config.mapping.payload = '';
                    }
                    self.render();
                });

            if (config.mapping.samplePayload.length > 0 || config.mapping.payload.length > 0) {
                var jsonObject = config.mapping.samplePayload.length > 0 ?
                                    JSON.parse(config.mapping.samplePayload)
                                    : config.mapping.payload;

                container.append(`
                    <div style="border: 1px solid #333; padding: 5px; margin-top: 5px;" class="parsed-representation-container">
                        ${
                            typeof jsonObject !== 'string' ?  
                                self.generateHTMLFromJSON(jsonObject, '', '') : jsonObject}
                    </div>
                    ${
                        typeof jsonObject !== 'string' ?
                            `<small>Hover over the attributes to assign attributes</small>` : ''
                    }
                    <div id="source-mapper-attribute-dropdown" 
                        style="left: 150px" class="dropdown-menu-style hidden" aria-labelledby="">
                    </div>
                `);
            }

            if ( self.__mapperData.namespace === 'sourceMapper' && Object.keys(config.mapping.attributes).length > 0) {
                container.append('<div style="margin: 15px 0 10px 0">Mapped attributes</div>' +
                    '<div style="display: flex; flex-direction: column" id="mapped-attrib-list"></div>')
                Object.keys(config.mapping.attributes).forEach(function (key) {
                    container.find('#mapped-attrib-list')
                        .append(`
                            <div style="width: 100%; padding-bottom: 5px" class="attribute-map">
                                <label style="margin-bottom: 0" class="" id="" 
                                    for="index-${key.replaceAll(/\//g, '-')}">${key}</label>
                                <input 
                                    disabled id="index-${key.replaceAll(/\//g, '-')}" 
                                    style="width: 100%; border: none; background-color: transparent; 
                                        border-bottom: 1px solid #333" placeholder="" type="text" 
                                    value="${config.mapping.attributes[key]}">
                            </div>
                        `)
                });
            }

            container.find('.btn-add-sample-payload').on('click', function (evt) {
                config.mapping.samplePayload = container.find('#sample-payload-submit-input').val();
                config.mapping.attributes = {};
                self.render();
            });

            config.stream.attributes.forEach(function (el) {
                var definedAttributes = Object.keys(config.mapping.attributes);

                if (!(definedAttributes.indexOf(el.name) > -1)) {
                    container.find('#source-mapper-attribute-dropdown').append(`
                        <a id="custom-source-attrib-${el.name}" title="" class="dropdown-item" href="#">
                            <div class="mapper-option">${el.name}</div><br/>
                            <small style="opacity: 0.8">${el.type}</small>
                        </a>
                    `);
                }
            });

            container.find('#source-mapper-attribute-dropdown>.dropdown-item')
                .on('click', function (evt) {
                    evt.stopPropagation();
                    var attribName = evt.currentTarget.id.match('custom-source-attrib-([a-zA-Z0-9_]+)')[1];
                    var pathArray = hoveredEl.split('-');
                    var name = '';
                    var jsonPathArray = [];

                    if (config.mapping.properties['enclosing.element']
                        && config.mapping.properties['enclosing.element'].value.startsWith('\$')) {
                        jsonPathArray = config.mapping.properties['enclosing.element'].value.split('.');
                        for (let i = 1; i < jsonPathArray.length; i++) {
                            var index = pathArray.indexOf(jsonPathArray[i]);
                            if (index === 0) {
                                pathArray.splice(0, 1);
                            }
                        }
                    }

                    pathArray.forEach(function (path, i) {
                        if(i !== 0) {
                            name += '.';
                        }
                        name += path;
                    });

                    // console.log[hoveredEl];
                    config.mapping.attributes[attribName] = hoveredEl.replaceAll(/-/g, '.');
                    if(self.__mapperType === 'sink') {
                        self.updateConfigPayload();
                    }
                    self.render();
                });

            container.find('.parsed-representation-container>span.no-clear')
                .on('mouseover', function (evt) {
                    hoveredEl = evt.currentTarget.id.match('custom-map-val-([a-zA-Z0-9\-]+)')[1];
                    var elementObj = container.find('#source-mapper-attribute-dropdown');
                    var leftOffset =  evt.currentTarget.offsetLeft;
                    var topOffset = evt.currentTarget.offsetTop + 20;

                    elementObj.css({"left": `${leftOffset}px`, 'top': `${topOffset}px`});
                    elementObj.removeClass('hidden');

                    elementObj.on('mouseleave', function (evt) {
                        elementObj.addClass('hidden');
                    })
                })
                .on('mouseleave', function (evt) {
                    var elementObj = container.find('#source-mapper-attribute-dropdown');

                    setTimeout(function () {
                        if (!container.find('#source-mapper-attribute-dropdown:hover').length > 0) {
                            elementObj.addClass('hidden');
                        }
                    }, 300);
                });

            container.find('.parsed-representation-container>span.ok-to-clear').popover({
                html: true,
                content: function () {
                    return `
                        <div class="popover-content">
                            <a href="#">
                                <i class="fw fw-clear"></i> Clear mapping
                            </a>
                        </div>
                    `
                },
                template: '<div style="width: fit-content; background-color: #3a3a3a" class="popover" role="tooltip">' +
                    '<div class="arrow"></div><div class="popover-content"></div></div>',
                placement: 'top',
            });

            container.find('.parsed-representation-container>span.ok-to-clear')
                .on('mouseover', function (evt) {
                    $(evt.currentTarget).popover('show');
                    hoveredEl = evt.currentTarget.id.match('custom-map-val-([a-zA-Z0-9\-]+)')[1];
                    $(container).find(`#${$(evt.currentTarget).attr('aria-describedby')}`).on('click', function(e) {
                        e.stopPropagation();
                        var attributeIndex = Object.values(config.mapping.attributes)
                            .indexOf(hoveredEl.replaceAll(/-/g, '.'));
                        delete config.mapping.attributes[Object.keys(config.mapping.attributes)[attributeIndex]];
                        if(self.__mapperType === 'sink') {
                            self.updateConfigPayload();
                        }
                        self.render();
                    })
                    $(container).find('.popover').on('mouseleave', function() {
                        $(evt.currentTarget).popover('hide');
                    });
                })
                .on('mouseleave', function (evt) {
                    setTimeout(function () {
                        if (!($(container).find('.popover:hover').length > 0)) {
                            $(evt.currentTarget).popover('hide');
                        }
                    }, 300);
                });
        }

        JSONMapper.prototype.generateHTMLFromJSON = function(jsonObject, indent, id) {
            var self = this;
            var content = '';
            var config = this.__extensionConfig;
            var value = id.substr(1).replaceAll(/-/g,'.');
            var attribIndex = Object.values(config.mapping.attributes).indexOf(value);

            switch (typeof jsonObject) {
                case 'object':
                    if ( Array.isArray(jsonObject)) {
                        content += '[<br/>';

                        Object.keys(jsonObject).forEach(function (key) {
                            content += `${indent+'&nbsp;&nbsp;&nbsp;&nbsp;'}`;
                            content+= self.generateHTMLFromJSON(
                                jsonObject[key],
                                indent+'&nbsp;&nbsp;&nbsp;&nbsp;',
                                `${id}`);
                        });
                        content += `${indent}]<br/>`;
                    } else {
                        content += `{<br/>`;
                        Object.keys(jsonObject).forEach(function (key) {
                            content += `${indent+'&nbsp;&nbsp;&nbsp;&nbsp;'}${key} : `
                            content+= self.generateHTMLFromJSON(
                                jsonObject[key],
                                indent+'&nbsp;&nbsp;&nbsp;&nbsp;',
                                `${id}-${key}`);
                        });
                        content += `${indent}}<br/>`;
                    }
                    break;
                case "number":
                    content += `<span 
                                    id="custom-map-val${id}" 
                                    class="custom-mapper-val ${attribIndex > -1 ? 'ok-to-clear': 'no-clear'}" 
                                >
                                    ${
                                        attribIndex > -1 ? 
                                            `{{ ${Object.keys(config.mapping.attributes)[attribIndex]} }}` 
                                            : jsonObject
                                    }</span>,<br/>`
                    break;
                case "string":
                    content += `<span 
                                    id="custom-map-val${id}" 
                                    class="custom-mapper-val ${attribIndex > -1 ? 'ok-to-clear': 'no-clear'}" 
                                >
                                    ${
                                        attribIndex > -1 ? 
                                            `{{ ${Object.keys(config.mapping.attributes)[attribIndex]} }}` 
                                            : `"${jsonObject}"`
                                    }
                                </span>,<br/>`
                    break;
            }
            return content;
        }

        JSONMapper.prototype.generateSinkPayload = function (jsonObject, id) {
            var self = this;
            var content = '';
            var config = this.__extensionConfig;
            var value = id.substr(1).replaceAll(/-/g,'.');
            var attribIndex = Object.values(config.mapping.attributes).indexOf(value);

            if(id.length === 0) {
                content += '"""';
            }

            switch (typeof jsonObject) {
                case 'object':
                    if ( Array.isArray(jsonObject)) {
                        content += '[';

                        Object.keys(jsonObject).forEach(function (key, index) {
                            content+= self.generateSinkPayload(jsonObject[key], `${id}`);

                            if(Object.keys(jsonObject).length > (index+1)) {
                                content+=',';
                            }
                        });
                        content += `]`;
                    } else {
                        content += `{`;
                        Object.keys(jsonObject).forEach(function (key, index) {
                            content += `"${key}":`
                            content += self.generateSinkPayload(jsonObject[key], `${id}-${key}`);

                            if(Object.keys(jsonObject).length > (index+1)) {
                                content+=',';
                            }
                        });
                        content += `}`;
                    }
                    break;
                case "number":
                    content += `${attribIndex > -1 ? 
                        Object.keys(config.mapping.attributes)[attribIndex] : jsonObject }`
                    break;
                case "string":
                    content += `"${attribIndex > -1 ? 
                        `{{${Object.keys(config.mapping.attributes)[attribIndex]}}` : jsonObject }"`
                    break;
            }

            if(id.length === 0) {
                content += '"""';
            }

            return content;
        }

        return JSONMapper;
    });
