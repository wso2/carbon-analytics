/*
 *
 *  * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */
define(['require', 'jquery', 'lodash', 'log', 'smart_wizard', 'app/source-editor/completion-engine', 'alerts'],

    function (require, $, _, log, smartWizard, CompletionEngine, Alerts) {

        /**
         * Constants used by the wizard
         */
        var constants = {
            CLASS_WIZARD_MODAL_HEADER: '.header-content',
            CLASS_WIZARD_MODAL_BODY: '.body-content',
            CLASS_WIZARD_MODAL_FOOTER: '.footer-content',
            ID_ETL_WIZARD_BODY: '#ETLWizardForm',
            SERVER_URL: window.location.protocol + "//" + window.location.host + "/editor/",
            SOURCE_TYPE: 'source',
            SINK_TYPE: 'sink',
        };

        var ETLWizard = function (initOpts) {
            this.__options = initOpts;
            this.__app = initOpts.application;
            this.__$parent_el_container = $(initOpts.container);
            this.__expressionData = undefined;

            //object structure used to store data
            this.__propertyMap = {
                input: {
                    source: {
                        type: '',
                        properties: {},
                        possibleOptions: {},
                    },
                    stream: {
                        name: "",
                        attributes: []
                    },
                    mapping: {}
                },
                output: {
                    sink: {
                        type: '',
                        properties: {},
                        possibleOptions: {},
                    },
                    stream: {
                        name: "",
                        attributes: []
                    },
                    mapping: {}
                },
                query: {
                    window: {},
                    filter: {},
                    function: {},
                    mapping: {},
                }
            };

            this.__stepIndex = 1;
            this.__parentWizardForm = this.constructWizardHTMLElements($('#ETLWizardForm').clone());
        };

        //Constructor for the ETLWizard
        ETLWizard.prototype.constructor = ETLWizard;

        //Construct and return wizard skeleton
        ETLWizard.prototype.constructWizardHTMLElements = function (wizardObj) {
            var wizardFormContainer = wizardObj.find(constants.ID_ETL_WIZARD_BODY);
            var wizardHeaderContent = wizardObj.find(constants.CLASS_WIZARD_MODAL_HEADER);
            var wizardFooterContent = wizardObj.find(constants.CLASS_WIZARD_MODAL_FOOTER);
            var stepIndex = this.__stepIndex;

            // Define header for the wizard
            wizardHeaderContent.append(`
                <h4 class="modal-title form-wizard-title" id="idHeaderTitle"> 
                    ETL Flow Creation Wizard 
                </h4>
            `);

            wizardHeaderContent.append(`
                <div class="header-steps">
                    <div id="step-1" class="step-item">
                        Step 1
                        <br/>
                        <small>Configure Input</small>
                    </div>
                    <div id="step-2" class="step-item">
                        Step 2
                        <br/>
                        <small>Configure Output</small>
                    </div>
                    <div id="step-3" class="step-item">
                        Step 3
                        <br/>
                        <small>Configure Input Options</small>
                    </div>
                    <div id="step-4" class="step-item">
                        Step 4
                        <br/>
                        <small>Configure schema mapping</small>
                    </div>
                    <div id="step-5" class="step-item">
                        Step 5
                        <br/>
                        <small>Configure output options</small>
                    </div>
                </div>
            `);

            wizardFooterContent.append(`
                <div style="position: relative" class="btn-tray">
                    <button style="position: absolute; left: 0" class="btn btn-default back-btn">Back</button>
                    <button style="position: absolute; right: 0" class="btn btn-default next-btn">Next</button>
                </div>
            `);

            wizardObj.find(`#step-${stepIndex}`).addClass('selected');

            wizardObj.find('.next-btn').on('click', function () {
                if (stepIndex < 5) {
                    wizardObj.find(`#step-${stepIndex++}`).removeClass('selected');
                    wizardObj.find(`#step-${stepIndex}`).addClass('selected');
                }
            });

            wizardObj.find('.back-btn').on('click', function () {
                if (stepIndex > 1) {
                    wizardObj.find(`#step-${stepIndex--}`).removeClass('selected');
                    wizardObj.find(`#step-${stepIndex}`).addClass('selected');
                }
            });

            return wizardObj;
        };

        ETLWizard.prototype.render = function () {
            var self = this;
            var etlWizardContainer = this.__$parent_el_container.find(_.get(this.__options, 'etl_wizard.container'));
            var canvasContainer = this.__$parent_el_container.find(_.get(this.__options, 'canvas.container'));
            var sourceContainer = this.__$parent_el_container.find(_.get(this.__options, 'source.container'));
            var designContainer = this.__$parent_el_container.find(_.get(this.__options, 'design_view.container'));
            var previewContainer = this.__$parent_el_container.find(_.get(this.__options, 'preview.container'));
            var toggleControlsContainer = this.__$parent_el_container.find('.toggle-controls-container');
            var wizardBodyContent = this.__parentWizardForm.find(constants.CLASS_WIZARD_MODAL_BODY)

            etlWizardContainer.append(this.__parentWizardForm);

            canvasContainer.removeClass('show-div').addClass('hide-div');
            previewContainer.removeClass('show-div').addClass('hide-div');
            designContainer.removeClass('show-div').addClass('hide-div');
            sourceContainer.removeClass('show-div').addClass('hide-div');
            toggleControlsContainer.addClass('hide');
            etlWizardContainer.addClass('etl-wizard-view-enabled');

            if (!self.__expressionData) {
                self.__expressionData = CompletionEngine.getRawMetadata();
            }

            switch (this.__stepIndex) {
                case 1:
                    //ToDo : Configure input
                    wizardBodyContent.empty();
                    this.renderSourceSinkConfigurator(constants.SOURCE_TYPE);
                    break;
                case 2:
                    // TODO: Configure output
                    break;
                case 3:
                    // TODO: Configure input options
                    break;
                case 4:
                    // TODO: Configure output options
                    break;
                case 5:
            }

        };

        ETLWizard.prototype.renderSourceSinkConfigurator = function (type) {
            var self = this;
            var config = type === constants.SOURCE_TYPE ?
                                    this.__propertyMap.input.source : this.__propertyMap.output.sink;
            var wizardBodyContent = this.__parentWizardForm.find(constants.CLASS_WIZARD_MODAL_BODY);
            var extensionData = constants.SOURCE_TYPE ? 
                                    this.__expressionData.extensions.source.sources :
                                    this.__expressionData.extensions.sink.sinks;

            wizardBodyContent.append(`
                <div style="" class="content-section">
                    <div style="font-size: 1.8rem">
                        Configure Source
                    </div>
                    <div style="padding-top: 5px">
                        <div>
                            <label for="source-type">Source type</label>
                            <select name="source-type" id="source-type">
                                <option disabled selected value> -- select an option -- </option>
                            </select>
                        </div>
                        ${
                            config.type.length > 0 ?
                                `
                                    <div style="padding-top: 15px" class="source-properties">
                                        <div>
                                            Source properties: 
                                          <button style="background-color: #ee6719" class="btn btn-default btn-circle" id="btn-add-source-property" type="button" data-toggle="dropdown">
                                             <i class="fw fw-add"></i>
                                          </button>
                                          <div id="source-options-dropdown" style="left: 150px" class="dropdown-menu-style hidden" aria-labelledby="">
                                          </div>
                                        </div>
                                        <div class="options">
                                        </div>
                                    </div>
                                ` : ''
                        }
                    </div>
                </div>
            `);
            
            extensionData.forEach(function (extension) {
                wizardBodyContent.find('#source-type').append(`
                    <option value="${extension.name}">${extension.name}</option>
                `);
            });

            if(config.type.length > 0) {
                Object.keys(config.possibleOptions).forEach(function (key) {
                    if(!config.properties[key]) {
                        wizardBodyContent.find('#source-options-dropdown').append(`
                            <a title="${config.possibleOptions[key].description.replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('`','')}" class="dropdown-item" href="#">${key}</a>
                        `)
                    }
                })

                wizardBodyContent.find('#btn-add-source-property')
                    .on('mouseover', function (evt) {
                        var leftOffset = evt.currentTarget.offsetLeft;
                        var elementObj = wizardBodyContent.find('.dropdown-menu-style');
                        elementObj.css({"left": `${leftOffset}px`})
                        elementObj
                            .removeClass('hidden')
                            .on('mouseleave', function () {
                                elementObj.addClass('hidden');
                            });
                    })
                    .on('mouseleave',function () {
                        setTimeout(function () {
                            var elementObj = wizardBodyContent.find('.dropdown-menu-style');
                            if(!(wizardBodyContent.find('.dropdown-menu-style:hover').length > 0)) {
                                elementObj.addClass('hidden');
                            }
                        }, 300);
                    });
                wizardBodyContent.find('#source-type').val(config.type);
                wizardBodyContent.find('.source-properties>.options').empty();
                Object.keys(config.properties).forEach(function (key) {
                    var optionData = config.properties[key];
                    var name = key.replaceAll(/\./g,'-');
                    wizardBodyContent.find('.source-properties>.options').append(`
                        <div style="display: flex; margin-bottom: 15px" class="property-option">
                            <div style="width: 100%" class="input-section">
                                <label style="margin-bottom: 0" class="${optionData.value.length > 0 ? '' : 'not-visible'}" id="label-source-op-${name}" for="source-op-${name}">${key}</label>
                                <input id="source-op-${name}" style="width: 100%; border: none; background-color: transparent; border-bottom: 1px solid #333" placeholder="${key}" type="text" value="${optionData.value}">
                            </div>
                            <div style="display: flex;padding-top: 20px; padding-left: 5px;" class="delete-section">
                                <a style="margin-right: 5px; color: #333" title="${optionData.description.replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('`','')}">
                                    <i class="fw fw-info"></i>    
                                </a>  
                                ${
                        optionData.optional ?
                            `<a style="color: #333">
                                            <i class="fw fw-delete"></i>    
                                         </a>`: ''
                    }                              
                            </div>
                        </div>
                    `);
                });

                wizardBodyContent.find('#source-options-dropdown>a').on('click', function (evt) {
                    config.properties[$(evt.currentTarget).text()] = config.possibleOptions[$(evt.currentTarget).text()];
                    config.properties[$(evt.currentTarget).text()].value = config.properties[$(evt.currentTarget).text()].defaultValue.replaceAll('`','');
                    self.render();
                });
            }

            wizardBodyContent.find('#source-type').on('change', function (evt) {
                config.type = $(evt.currentTarget).val();
                var sourceData = extensionData.find(function (el) {
                    return el.name === config.type;
                });
                config.properties = {};
                config.possibleOptions = {};
                sourceData.parameters
                    .filter(function (el) {
                        config.possibleOptions[el.name] = el;
                        return !el.optional;
                    })
                    .forEach(function (param) {
                        param['value'] = param.defaultValue.replaceAll('`','');
                        config.properties[param.name] = param;
                    });

                self.render();
            });

            wizardBodyContent.find('.property-option>.input-section>input')
                .on('focus', function (evt) {
                    var inputId = $(evt.currentTarget).attr('id');
                    wizardBodyContent.find(`#label-${inputId}`).removeClass('not-visible');
                    $(evt.currentTarget).attr('placeholder', 'Type here to enter');
                })
                .on('focusout', function (evt) {
                    if ($(evt.currentTarget).val().length === 0) {
                        var inputId = $(evt.currentTarget).attr('id');
                        wizardBodyContent.find(`#label-${inputId}`).addClass('not-visible');
                        $(evt.currentTarget).attr('placeholder', wizardBodyContent.find(`#label-${inputId}`).text());
                    }
                });
        }
        
        return ETLWizard;
    });
