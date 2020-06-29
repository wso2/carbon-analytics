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

        var XMLMapper = function (type, container, mapConfig) {
            this.__mapperContainer = container;
            this.__mapperType = type;
            this.__extensionConfig = mapConfig;
            this.__hoveredEl = '';
        }

        XMLMapper.prototype.constructor = XMLMapper;

        XMLMapper.prototype.render = function () {
            var self = this;
            var container = this.__mapperContainer;
            var config = this.__extensionConfig.mapping;

            if(config.payload) {
                console.log(config.payload);
            }

            container.empty();
            container.append(`
                <div id="source-mapper-configurator">
                    <div style="padding-top: 10px">
                        <div style="padding-top: 15px" class="attribute-list">
                            <div>
                              Source Mapper configuration
                              ${
                Object.keys(config.possibleProperties).length !== Object.keys(config.properties).length ?
                    `<button style="background-color: #ee6719" class="btn btn-default btn-circle" id="btn-add-source-mapper-property" type="button" data-toggle="dropdown">
                                            <i class="fw fw-add"></i>
                                        </button>`: ''
            }
                              <div id="source-mapper-option-dropdown" style="left: 150px" class="dropdown-menu-style hidden" aria-labelledby="">
                              </div>
                            </div>
                            <div style="" class="source-mapper-options">
                            </div>
                            <div class="custom-mapping-section">
                                <div style="display: flex; padding-top:15px">
                                    <div style="width: 100%;padding-top: 5px">
                                        Enable custom attribute mapping                                        
                                    </div>
                                    <div>
                                        <button style="background-color: #ee6719" class="btn btn-default btn-circle" id="btn-enable-custom-map" type="button" data-toggle="dropdown">
                                            <i class="fw ${config.customEnabled ? 'fw-check' : 'fw-minus'}"></i>
                                        </button> 
                                    </div>
                                </div>
                                <div class="attrib-section">
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `);

            Object.keys(config.possibleProperties).forEach(function (key) {
                if (!config.properties[key]) {
                    container.find('#source-mapper-option-dropdown').append(`
                        <a title="" class="dropdown-item" href="#">
                            <div class="mapper-option">${key}</div><br/>
                            <small style="opacity: 0.8">${config.possibleProperties[key].description.replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('`', '')}</small><br/>
                            <small style="opacity: 0.8"><b>Default value</b>: ${config.possibleProperties[key].defaultValue}</small>
                        </a>
                    `);
                }
            });

            Object.keys(config.properties).forEach(function (key) {
                var optionData = config.properties[key];
                var name = key.replaceAll(/\./g, '-');
                container.find('.source-mapper-options').append(`
                    <div style="display: flex; margin-bottom: 15px" class="mapper-option">
                            <div style="width: 100%" class="input-section">
                                <label style="margin-bottom: 0" class="${optionData.value.length > 0 ? '' : 'not-visible'}" id="label-mapper-op-${name}" for="mapper-op-${name}">${key}</label>
                                <input id="mapper-op-${name}" style="width: 100%; border: none; background-color: transparent; border-bottom: 1px solid #333" placeholder="${key}" type="text" value="${optionData.value}">
                            </div>
                            <div style="display: flex;padding-top: 20px; padding-left: 5px;" class="delete-section">
                                <a style="margin-right: 5px; color: #333" title="${optionData.description.replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('`', '')}">
                                    <i class="fw fw-info"></i>    
                                </a>  
                                ${
                    optionData.optional ?
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
                config.properties[optionName] = config.possibleProperties[optionName];
                config.properties[optionName].value = config.properties[optionName].defaultValue;
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
                    var propertyName = evt.currentTarget.id.match('mapper-op-([a-zA-Z-]+)')[1].replaceAll(/-/g, '.');
                    config.properties[propertyName].value = $(evt.currentTarget).val();
                }, 100, {}));

            container.find('.mapper-option>.delete-section>a>.fw-delete').on('click', function (evt) {
                var attribName = evt.currentTarget.id.match('mapper-op-del-([a-zA-Z-]+)')[1].replaceAll(/-/g, '.');
                delete config.properties[attribName];
                self.render();
            });

            container.find('#btn-enable-custom-map').on('click', function (evt) {
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



        XMLMapper.prototype.updateConfigPayload = function () {
            var self = this;
            var config = this.__extensionConfig;

            var parsedXML = new DOMParser().parseFromString(config.mapping.samplePayload, 'text/xml');
            config.mapping.payload = self.generateSinkPayload(parsedXML, '');
            console.log(config.mapping.payload)
        }

        XMLMapper.prototype.renderCustomMapper = function () {
            var self = this;
            var container = this.__mapperContainer;
            var config = this.__extensionConfig;
            var hoveredEl = this.__hoveredEl;

            container.append(`
                <div style="display: flex; padding-top: 10px" class="sample-payload-submit-section">
                    <input id="sample-payload-submit-input" style="width: 100%; border: none; background-color: transparent; border-bottom: 1px solid #333" placeholder="Enter the sample payload here" type="text" value="">
                    <button style="background-color: #ee6719" class="btn btn-default btn-add-sample-payload">Submit</button>
                </div>
            `);

            if (config.mapping.samplePayload.length > 0) {
                var parsedXml = new DOMParser().parseFromString(config.mapping.samplePayload, 'text/xml');

                container.append(`
                    <div style="border: 1px solid #333; padding: 5px; margin-top: 5px;" class="parsed-representation-container">
                        ${self.generateHTMLFromXML(parsedXml, '', '')}
                    </div>
                    <small>Hover over the attributes to assign attributes</small>
                    <div id="source-mapper-attribute-dropdown" style="left: 150px" class="dropdown-menu-style hidden" aria-labelledby="">
                    </div>
                `);

            }

            container.find('.btn-add-sample-payload').on('click', function (evt) {
                config.mapping.samplePayload = container.find('#sample-payload-submit-input').val();
                self.render();
            });

            config.stream.attributes.forEach(function (el) {
                var definedAttributes = Object.values(config.mapping.attributes).map(function (value) {
                    return value.attributeName;
                });

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
                    var attribName = evt.currentTarget.id.match('custom-source-attrib-([a-zA-Z_]+)')[1];
                    var pathArray = hoveredEl.split('\/');
                    var name = ''

                    if(config.mapping.properties['enclosing.element']) {
                        var index = pathArray.indexOf(config.mapping.properties['enclosing.element'].value.split('\/\/')[1]);
                        if(index > -1) {
                            pathArray.splice(index, 1);
                        }
                        pathArray.forEach(function (path, i) {
                            if(i !== 0) {
                                name += '/';
                            }
                            name += path;
                        });
                    }

                    config.mapping.attributes[hoveredEl] = {attributeName: attribName, value: name};
                    if(self.__mapperType === 'sink') {
                        self.updateConfigPayload();
                    }
                    self.render();
                });

            container.find('.parsed-representation-container>span.no-clear')
                .on('mouseover', function (evt) {
                    hoveredEl = evt.currentTarget.id.match('custom-map-val-([a-zA-Z-]+)')[1].replaceAll(/-/g, '\/').substr(1);
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
                template: '<div style="width: fit-content; background-color: #3a3a3a" class="popover" role="tooltip"><div class="arrow"></div><div class="popover-content"></div></div>',
                placement: 'top',
            });

            container.find('.parsed-representation-container>span.ok-to-clear')
                .on('mouseover', function (evt) {
                    $(evt.currentTarget).popover('show');
                    hoveredEl = evt.currentTarget.id.match('custom-map-val-([a-zA-Z-]+)')[1].replaceAll(/-/g, '\/').substr(1);
                    $(container).find(`#${$(evt.currentTarget).attr('aria-describedby')}`).on('click', function(e) {
                        e.stopPropagation();
                        delete config.mapping.attributes[hoveredEl];
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



        XMLMapper.prototype.generateHTMLFromXML = function(xmlObject, indent, id) {
            var content = '';
            var config = this.__extensionConfig;

            for (var i = 0; i < xmlObject.childElementCount; i++) {
                content += `${indent}&lt;${xmlObject.children[i].tagName}`;
                Object.keys(xmlObject.children[i].attributes).forEach(function (key) {
                    content += ` ${xmlObject.children[i].attributes[key].name}="${xmlObject.children[i].attributes[key].nodeValue}" `
                });
                content += '&gt;';
                if(xmlObject.children[i].childElementCount > 0) {
                    content += `<br/>${this.generateHTMLFromXML(xmlObject.children[i], indent+'&nbsp;&nbsp;&nbsp;&nbsp;', `${id}-${xmlObject.children[i].tagName}`)}`;
                } else {
                    var key = `${id}-${xmlObject.children[i].tagName}`;
                    var attrib_key = key.replaceAll(/-/g, '\/').substr(1);

                    content += `<span id="custom-map-val-${key}" class="custom-mapper-val ${config.mapping.attributes[attrib_key] ? 'ok-to-clear': 'no-clear'}" id="attrib-val-${i}">${config.mapping.attributes[attrib_key] ? `{{  ${config.mapping.attributes[attrib_key].attributeName}  }}` : this.__mapperType === 'source' ? '{{value}}' : xmlObject.children[i].textContent }</span>`;
                }
                content += `${xmlObject.children[i].childElementCount > 0 ? indent : ''}&lt;/${xmlObject.children[i].tagName}&gt;<br/>`;
            }

            return content;
        }

        XMLMapper.prototype.generateSinkPayload = function (xmlObject, id) {
            var content = '';
            var config = this.__extensionConfig;

            for (var i = 0; i < xmlObject.childElementCount; i++) {
                content += `<${xmlObject.children[i].tagName}`;
                Object.keys(xmlObject.children[i].attributes).forEach(function (key) {
                    content += ` ${xmlObject.children[i].attributes[key].name}="${xmlObject.children[i].attributes[key].nodeValue}" `
                });
                content += '>';
                if(xmlObject.children[i].childElementCount > 0) {
                    content += `${this.generateSinkPayload(xmlObject.children[i], `${id}-${xmlObject.children[i].tagName}`)}`;
                } else {
                    var key = `${id}-${xmlObject.children[i].tagName}`;
                    var attrib_key = key.replaceAll(/-/g, '\/').substr(1);

                    content += `${config.mapping.attributes[attrib_key] ? `{{${config.mapping.attributes[attrib_key].attributeName}}}` : xmlObject.children[i].textContent }`;
                }
                content += `</${xmlObject.children[i].tagName}>`;
            }

            return content;
        }

        return XMLMapper;
    });
