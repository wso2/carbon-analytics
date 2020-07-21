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
define(['require', 'jquery', 'lodash', 'log', 'app/source-editor/completion-engine', 'alerts', 'inputOutputMapper', 'inputOptionConfigurator', 'dataMapper', 'outputConfigurator', 'etlWizardUtil'],

    function (require, $, _, log, CompletionEngine, Alerts, InputOutputMapper, InputOptionConfigurator, DataMapper, OutputConfigurator, etlWizardUtil) {

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
            SUPPORTED_DATA_TYPES: ['INT', 'LONG', 'FLOAT', 'DOUBLE', 'STRING', 'BOOL']
        };

        var ETLWizard = function (initOpts) {
            this.__options = initOpts;
            this.__app = initOpts.application;
            this.__$parent_el_container = $(initOpts.container);
            this.__expressionData = undefined;
            this.__previousSchemaDef = undefined;
            this.__resetSchema = false;

            //object structure used to store data
            this.__propertyMap = {
                appName: '',
                input: {
                    source: {
                        type: '',
                        properties: {},
                        possibleOptions: {},
                    },
                    stream: {
                        name: "",
                        attributes: [],
                        addLog: false
                    },
                    mapping: {
                        type: '',
                        properties: {},
                        possibleProperties: {},
                        attributes: {},
                        customEnabled: false,
                        samplePayload: "",
                    }
                },
                output: {
                    sink: {
                        type: '',
                        properties: {},
                        possibleOptions: {},
                        addOnError: false
                    },
                    stream: {
                        name: "",
                        attributes: [],
                        addLog: false
                    },
                    mapping: {
                        type: '',
                        properties: {},
                        possibleProperties: {},
                        attributes: {},
                        payload: '',
                        customEnabled: false,
                        samplePayload: ''
                    }
                },
                query: {
                    window: {},
                    filter: {
                        // enable: true,
                        // expression: 'haha'
                    },
                    function: {
                        // enable: true,
                        // text: `rdbms:query("INFO", "Sample Event :", true)`
                        "enable": true,
                        "name": "str:tokenize",
                        "parameters": {
                            "input.string": { "value": "name", "type": ["STRING"] },
                            "regex": { "value": "'-'", "type": ["STRING"] }
                        }
                    },
                    mapping: {
                        // id: "id"
                    },
                    groupby: {
                        attributes: [],
                        havingFilter : {}
                    },
                    orderby: {
                        attributes: []
                    },
                    advanced: {
                        offset: {},
                        limit: {},
                        ratelimit: {}
                    }
                }
            };

            this.__stepIndex = 1;
            this.__substep = 0;
            this.__steps = [
                {id: 1, description: 'Data source'},
                {id: 2, description: 'Data Destination'},
                {id: 3, description: 'Process Input Data'},
                {id: 4, description: 'Data Mapping'},
                {id: 5, description: 'Process Output Data'},
            ];
            if (this.__propertyMap.appName.length === 0) {
                this.__propertyMap.appName = 'UntitledETLTaskFlow';
            }
            this.__parentWizardForm = this.constructWizardHTMLElements($('#ETLWizardForm').clone());
        };

        //Constructor for the ETLWizard
        ETLWizard.prototype.constructor = ETLWizard;

        // handle schema changes
        ETLWizard.prototype.handleSchemaChange = function(typeOfSchema, wizardObj, config) {
            var self = this;
            var newSchemaDef = typeOfSchema === constants.SOURCE_TYPE ?
                                self.__propertyMap.input.stream.attributes:
                                self.__propertyMap.output.stream.attributes;

            wizardObj.find('.next-btn').popover({
                html: true,
                content: function () {
                    return '<div>' +
                        'Schema change detected this will reset the subsequent mappings generated using the schema do you wish to proceed with changes?' +
                        '</div>' +
                        '<div>' +
                        '    <button class="popover-confirm-proceed" >Yes</button>' +
                        '    <button class="popover-confirm-cancel" >No</button>' +
                        '    <button class="popover-btn-reset" >Reset</button>' +
                        '</div>';
                },
                template: '<div class="popover" role="tooltip"><div class="arrow"></div><div class="popover-content" style="display: flex; flex-direction: column"></div></div>',
                placement: 'top',
            });
        
            if(self.__previousSchemaDef.length > 0 && !_.isEqual(self.__previousSchemaDef, newSchemaDef)) {
                
                wizardObj.find('.next-btn').popover('show');
                wizardObj.find(`#${wizardObj.find('.next-btn').attr('aria-describedby')} .popover-confirm-proceed`).on('click', function (e) {
                    e.stopPropagation();
                    if(typeOfSchema === constants.SOURCE_TYPE) {
                        self.__propertyMap.input.mapping.attributes = {};
                        self.__propertyMap.input.mapping.samplePayload = "";
                        self.__propertyMap.query.mapping = {};
                        self.__propertyMap.query.filter = {};
                    } else {
                        self.__propertyMap.output.mapping.attributes = {};
                        self.__propertyMap.output.mapping.samplePayload = "";
                        self.__propertyMap.output.mapping.payload = "";
                        self.__propertyMap.query.groupby = {
                            attributes: [],
                            havingFilter: {}
                        };
                        self.__propertyMap.query.orderby.attributes = [];
                    }
                    
                    wizardObj.find('.next-btn').popover('hide');
                    self.__previousSchemaDef = undefined;
                    self.incrementStep(wizardObj);
                });
                wizardObj.find(`#${wizardObj.find('.next-btn').attr('aria-describedby')} .popover-confirm-cancel`).on('click', function (e) {
                    e.stopPropagation();
                    wizardObj.find('.next-btn').popover('hide');
                });
                wizardObj.find(`#${wizardObj.find('.next-btn').attr('aria-describedby')} .popover-btn-reset`).on('click', function (e) {
                    e.stopPropagation();
                    wizardObj.find('.next-btn').popover('hide');
                    self.__resetSchema = true;
                    self.render();
                });
            } else {
                self.__previousSchemaDef = undefined;
                self.incrementStep(wizardObj);
            }
        }

        //Construct and return wizard skeleton
        ETLWizard.prototype.constructWizardHTMLElements = function (wizardObj) {
            var self = this;
            var wizardFormContainer = wizardObj.find(constants.ID_ETL_WIZARD_BODY);
            var wizardHeaderContent = wizardObj.find(constants.CLASS_WIZARD_MODAL_HEADER);
            var wizardFooterContent = wizardObj.find(constants.CLASS_WIZARD_MODAL_FOOTER);
            var stepIndex = this.__stepIndex;
            var substep = this.__substep;
            var config = this.__propertyMap;
            var steps = this.__steps;

            wizardHeaderContent.empty();
            wizardFooterContent.empty();

            // Define header for the wizard
            wizardHeaderContent.append(`
                <input class="etl-flow-name" id="" type="text" value="${config.appName}"/>
                <div class="header-steps"></div>
            `);

            steps.forEach(function (step) {
                wizardHeaderContent.find('.header-steps')
                    .append(`
                        <div id="step-${step.id}" class="step-item">
                            Step ${step.id}
                            <br/>
                            <small>${step.description}</small>
                        </div>
                    `);
            });

            wizardFooterContent.append(`
                <div style="position: relative" class="btn-tray">
                    <button style="position: absolute; left: 0" class="btn btn-default back-btn">Back</button>
                    <button style="position: absolute; right: 0" class="btn btn-default next-btn">Next</button>
                </div>
            `);

            wizardObj.find(`#step-${stepIndex}`).addClass('selected');



            wizardObj.find('.next-btn').on('click', function (evt) {
                switch (self.__stepIndex) {
                    case 1:
                        switch (self.__substep) {
                            case 0:
                                if (etlWizardUtil.isSourceSinkConfigValid(config.input.source)) {
                                    self.incrementStep(wizardObj);
                                } else {
                                    Alerts.error('Invalid source configuration please check the all the values are defined properly');
                                }
                                break;
                            case 1:
                                if(etlWizardUtil.isStreamDefValid(config.input.stream)) {
                                    self.handleSchemaChange(constants.SOURCE_TYPE, wizardObj, config);
                                } else {
                                    Alerts.error('Invalid source configuration please check the all the properties are defined properly');
                                }
                                break;
                            case 2:
                                // wizardObj.find('.next-btn').popover({});
                                if(etlWizardUtil.isInputMappingValid(config.input)) {
                                    self.incrementStep(wizardObj);
                                } else {
                                    Alerts.error('Invalid source mapping configuration please check the mapping configuration');
                                }
                                break;
                        }
                        break;
                    case 2:
                        switch (self.__substep) {
                            case 0:
                                if (etlWizardUtil.isSourceSinkConfigValid(config.output.sink)) {
                                    self.incrementStep(wizardObj);
                                } else {
                                    Alerts.error('Invalid sink configuration please check the all the values are defined properly');
                                }
                                break;
                            case 1:
                                if(etlWizardUtil.isStreamDefValid(config.output.stream)) {
                                    self.handleSchemaChange(constants.SOURCE_TYPE, wizardObj, config);
                                } else {
                                    Alerts.error('Invalid stream definition please check the all the properties are defined properly');
                                }
                                break;
                            case 2:
                                if(etlWizardUtil.isOutputMappingValid(config.output)) {
                                    self.incrementStep(wizardObj);
                                } else {
                                    Alerts.error('Invalid sink mapping configuration please check the mapping configuration');
                                }
                                break;
                        }
                        break;
                    case 3:
                        if(etlWizardUtil.areInputOptionsValid(config.query)) {
                            self.incrementStep(wizardObj);
                        } else {
                            Alerts.error('Invalid input option configuration please check the mapping configuration');
                        }
                        break;
                    case 4:
                        if(etlWizardUtil.validateDataMapping(config)) {
                            self.incrementStep(wizardObj);
                        } else {
                            Alerts.error('Please perform the attribute mapping for all the output attributes');
                        }
                        break;
                    case 5:
                        if(etlWizardUtil.validateGroupBy(config.query.groupby) && etlWizardUtil.validateAdvancedOutputOptions(config.query.advanced)) {
                            self.incrementStep(wizardObj);
                        } else {
                            Alerts.error('Please recheck the output options before submitting');
                        }
                        break;

                }

            });

            wizardObj.find('.back-btn').on('click', function () {
                switch (stepIndex) {
                    case 1:
                        self.decrementStep(wizardObj);
                        break;
                    case 2:
                        self.decrementStep(wizardObj);
                        break;
                    case 3:
                        self.decrementStep(wizardObj);
                        break;
                    case 4:
                        self.decrementStep(wizardObj);
                        break;
                    case 5:
                        self.decrementStep(wizardObj);
                        break;

                }

            });

            wizardHeaderContent.find('.etl-flow-name')
                .on('keyup', _.debounce(function (evt) {
                    config.appName = $(evt.currentTarget).val();
                }, 100, {}))
                .on('focusout', function (evt) {
                    var appName = $(evt.currentTarget).val();
                    if (!appName.length > 0 && config.appName.length > 0) {
                        config.appName = 'UntitledETLTaskFlow';
                        $(evt.currentTarget).val(config.appName);
                    }
                });

            return wizardObj;
        };

        ETLWizard.prototype.incrementStep = function (wizardObj) {
            var self = this;
            if (self.__stepIndex < self.__steps.length) {
                if (self.__stepIndex < 3 && self.__substep < 2) {
                    self.__substep++;
                } else {
                    wizardObj.find(`#step-${self.__stepIndex++}`).removeClass('selected');
                    wizardObj.find(`#step-${self.__stepIndex}`).addClass('selected');
                    self.__substep = 0;
                }

                self.render();
            }
        }

        ETLWizard.prototype.decrementStep = function (wizardObj) {
            var self = this;
            if (self.__stepIndex > 0) {
                if (self.__stepIndex < 3 && self.__substep > 0) {
                    self.__substep--;
                } else {
                    wizardObj.find(`#step-${self.__stepIndex--}`).removeClass('selected');
                    wizardObj.find(`#step-${self.__stepIndex}`).addClass('selected');
                    self.__substep = 0;
                }

                self.render();
            }
        }

        ETLWizard.prototype.render = function () {
            var self = this;
            var etlWizardContainer = this.__$parent_el_container.find(_.get(this.__options, 'etl_wizard.container'));
            var canvasContainer = this.__$parent_el_container.find(_.get(this.__options, 'canvas.container'));
            var sourceContainer = this.__$parent_el_container.find(_.get(this.__options, 'source.container'));
            var designContainer = this.__$parent_el_container.find(_.get(this.__options, 'design_view.container'));
            var previewContainer = this.__$parent_el_container.find(_.get(this.__options, 'preview.container'));
            var toggleControlsContainer = this.__$parent_el_container.find('.toggle-controls-container');
            var wizardBodyContent = this.__parentWizardForm.find(constants.CLASS_WIZARD_MODAL_BODY)
            
            this.__parentWizardForm.find('.next-btn').popover('destroy');
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

            if(self.__resetSchema) {
                if(this.__stepIndex === 1) {
                    this.__propertyMap.input.stream.attributes = _.cloneDeep(self.__previousSchemaDef);
                } else if(this.__stepIndex === 2) {
                    this.__propertyMap.output.stream.attributes = _.cloneDeep(self.__previousSchemaDef);
                }
                self.__resetSchema = false;
            }

            if(!this.__previousSchemaDef && this.__stepIndex === 1) {
                this.__previousSchemaDef = _.cloneDeep(this.__propertyMap.input.stream.attributes);    
            }

            if(!this.__previousSchemaDef && this.__stepIndex === 2) {
                this.__previousSchemaDef = _.cloneDeep(this.__propertyMap.output.stream.attributes);    
            }

            wizardBodyContent.empty();

            switch (this.__stepIndex) {
                case 1:
                    this.renderSourceSinkConfigurator(constants.SOURCE_TYPE);
                    this.renderSchemaConfigurator(constants.SOURCE_TYPE);
                    this.renderInputOutputMapper(constants.SOURCE_TYPE);
                    break;
                case 2:
                    this.renderSourceSinkConfigurator(constants.SINK_TYPE);
                    this.renderSchemaConfigurator(constants.SINK_TYPE);
                    this.renderInputOutputMapper(constants.SINK_TYPE);
                    break;
                case 3:
                    var inputOptionConfigurator = new InputOptionConfigurator(wizardBodyContent, self.__propertyMap);
                    inputOptionConfigurator.render();
                    break;
                case 4:
                    var dataMapperContainer = self.__$parent_el_container.find('.etl-task-wizard-container').clone();
                    wizardBodyContent.append(dataMapperContainer);
                    new DataMapper(dataMapperContainer, self.__propertyMap);

                    break;
                case 5:
                    var outputConfigurator = new OutputConfigurator(wizardBodyContent, self.__propertyMap);
                    outputConfigurator.render();
            }

            if(this.__stepIndex < 3) {
                var containers = wizardBodyContent.find('.content-section');
                for (let i = 0; i < containers.length; i++) {
                    if(i!==this.__substep) {
                        var offsetLeft = wizardBodyContent.find('.content-section')[i].offsetLeft;
                        var offsetTop = wizardBodyContent.find('.content-section')[i].offsetTop;
                        var minWidth = $(wizardBodyContent.find('.content-section')[i]).width();
                        var minHeight = $(wizardBodyContent.find('.content-section')[i]).height();

                        wizardBodyContent.append(`<div style="position: absolute; top: ${offsetTop-15}; left: ${offsetLeft-15}; width: ${minWidth+30}; height: ${minHeight+30}; background-color: rgba(0,0,0,0.5)"></div>`);
                    }
                }
            }
        };

        ETLWizard.prototype.renderSourceSinkConfigurator = function (type) {
            var self = this;
            var config = type === constants.SOURCE_TYPE ?
                this.__propertyMap.input.source : this.__propertyMap.output.sink;
            var wizardBodyContent = this.__parentWizardForm.find(constants.CLASS_WIZARD_MODAL_BODY);
            var extensionData = constants.SOURCE_TYPE === type ?
                this.__expressionData.extensions.source.sources :
                this.__expressionData.extensions.sink.sinks;
            var selectedExtension = null;

            var logIndex = extensionData
                            .map(function(extension) {
                                return extension.name;
                            }).indexOf('log');
            
            if(logIndex > -1) {
                extensionData.splice(logIndex, 1);
            }

            wizardBodyContent.append(`
                <div style="max-height: ${wizardBodyContent[0].offsetHeight}; overflow: auto" class="content-section">
                    <div style="font-size: 1.8rem">
                        Transport Properties<br/>
                        <small style="font-size: 1.3rem">Configure ${type === constants.SOURCE_TYPE ? 'Source' : 'Sink'} extension</small>
                    </div>
                    ${
                        type !== constants.SOURCE_TYPE ?
                            `<div style="display: flex; padding-top:15px">
                                <div style="width: 100%;padding-top: 5px">
                                    Store mapping errors                                      
                                </div>
                                <div>
                                    <button style="background-color: #ee6719" class="btn btn-default btn-circle" id="btn-enable-error-handling" type="button" data-toggle="dropdown">
                                        <i class="fw ${config.addOnError ? 'fw-check' : 'fw-minus'}"></i>
                                    </button> 
                                </div>
                            </div>` : ''
                    }
                    <div style="padding-top: 10px">
                        <div>
                            <label for="extension-type">${type === constants.SOURCE_TYPE ? 'Source' : 'Sink'} type</label>
                            <select name="extension-type" id="extension-type">
                                <option disabled selected value> -- select an option -- </option>
                            </select>
                        </div>

                        ${
                            config.type.length > 0 ?
                                `
                                    <div style="padding-top: 15px" class="extension-properties">
                                        <div>
                                          ${type === constants.SOURCE_TYPE ? 'Source' : 'Sink'} properties: 
                                            <button style="background-color: #ee6719" class="btn btn-default btn-circle" id="btn-add-transport-property" type="button" data-toggle="dropdown">
                                                <i class="fw fw-add"></i>
                                            </button>
                                            <div id="extension-options-dropdown" class="dropdown-menu-style hidden" aria-labelledby="">
                                            </div>
                                        </div>
                                        <div style="" class="options">
                                        </div>
                                    </div>
                                ` : ''
                        }
                    </div>
                </div>
            `);

            wizardBodyContent.find('#btn-enable-error-handling').on('click', function(evt) {
                config.addOnError = !config.addOnError;
                self.render();
            });

            extensionData.forEach(function (extension) {
                wizardBodyContent.find('#extension-type').append(`
                    <option value="${extension.name}">${extension.name}</option>
                `);
            });

            if (config.type.length > 0) {
                selectedExtension = extensionData.find(function (el) {
                    return el.name === config.type;
                });

                selectedExtension.parameters.forEach(function (param) {
                    if (!config.properties[param.name]) {
                        wizardBodyContent.find('#extension-options-dropdown').append(`
                            <a title="" class="dropdown-item" href="#">
                                <div>
                                    <div class="option-title">${param.name}</div><br/>
                                    <small style="opacity: 0.8">${param.description.replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('`', '')}</small><br/>
                                    <small style="opacity: 0.8"><b>Default Value</b>: ${param.defaultValue}</small>
                                </div>
                            </a>
                        `)
                    }
                })

                wizardBodyContent.find('#btn-add-transport-property')
                    .on('mouseover', function (evt) {
                        var leftOffset = evt.currentTarget.offsetLeft;
                        var elementObj = wizardBodyContent.find('#extension-options-dropdown');
                        elementObj.css({"left": `${leftOffset}px`})
                        elementObj
                            .removeClass('hidden')
                            .on('mouseleave', function () {
                                elementObj.addClass('hidden');
                            });
                    })
                    .on('mouseleave', function () {
                        setTimeout(function () {
                            var elementObj = wizardBodyContent.find('#extension-options-dropdown');
                            if (!(wizardBodyContent.find('#extension-options-dropdown:hover').length > 0)) {
                                elementObj.addClass('hidden');
                            }
                        }, 300);
                    });
                wizardBodyContent.find('#extension-type').val(config.type);
                wizardBodyContent.find('.extension-properties>.options').empty();
                Object.keys(config.properties).forEach(function (key) {
                    var optionData = config.properties[key];
                    var name = key.replaceAll(/\./g, '-');
                    var selectedOption = selectedExtension.parameters.find(function(element) {
                        return element.name === key;
                    });
                    wizardBodyContent.find('.extension-properties>.options').append(`
                        <div style="display: flex; margin-bottom: 15px" class="property-option">
                            <div style="width: 100%" class="input-section">
                                <label style="margin-bottom: 0" class="${optionData.value.length > 0 ? '' : 'not-visible'}" id="label-extension-op-${name}" for="extension-op-${name}">${key}</label>
                                <input id="extension-op-${name}" style="width: 100%; border: none; background-color: transparent; border-bottom: 1px solid #333" placeholder="${key}" type="text" value="${optionData.value}">
                            </div>
                            <div style="display: flex;padding-top: 20px; padding-left: 5px;" class="delete-section">
                                <a style="margin-right: 5px; color: #333" title="${selectedOption.description.replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('`', '')}">
                                    <i class="fw fw-info"></i>    
                                </a>  
                                ${
                                    selectedOption.optional ?
                                        `<a style="color: #333">
                                            <i id="extension-op-del-${name}" class="fw fw-delete"></i>    
                                         </a>` : ''
                                }                              
                            </div>
                        </div>
                    `);
                });

                wizardBodyContent.find('#extension-options-dropdown>a').on('click', function (evt) {
                    var optionName = $(evt.currentTarget).find('.option-title').text();
                    var selectedOption = selectedExtension.parameters.find(function(element) {
                        return element.name === optionName;
                    });
                    
                    config.properties[optionName] = {};
                    config.properties[optionName].value = selectedOption.defaultValue;
                    config.properties[optionName].type = selectedOption.type;
                    self.render();
                });
            }

            wizardBodyContent.find('#extension-type').on('change', function (evt) {
                config.type = $(evt.currentTarget).val();
                var sourceData = extensionData.find(function (el) {
                    return el.name === config.type;
                });

                config.properties = {};
                // config.possibleOptions = {};
                sourceData.parameters
                    .filter(function (el) {
                        // config.possibleOptions[el.name] = el;
                        return !el.optional;
                    })
                    .forEach(function (param) {
                        var paramData = {}
                        paramData['value'] = param.defaultValue.replaceAll('`', '');
                        paramData.type = param.type;
                        config.properties[param.name] = paramData;
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
                })
                .on('keyup', _.debounce(function (evt) {
                    var optionName = evt.currentTarget.id.match('extension-op-([a-zA-Z0-9-]+)')[1].replaceAll(/-/g, '.');
                    config.properties[optionName].value = $(evt.currentTarget).val();
                }, 100, {}));

            wizardBodyContent.find('.property-option>.delete-section>a>.fw-delete').on('click', function (evt) {
                var optionName = evt.currentTarget.id.match('extension-op-del-([a-zA-Z0-9-]+)')[1].replaceAll(/-/g, '.');
                delete config.properties[optionName];
                self.render();
            });
        }

        ETLWizard.prototype.renderSchemaConfigurator = function (type) {
            var self = this;
            var config = type === constants.SOURCE_TYPE ?
                self.__propertyMap.input.stream : self.__propertyMap.output.stream;
            var wizardBodyContent = this.__parentWizardForm.find(constants.CLASS_WIZARD_MODAL_BODY);
            config.name = config.name.length > 0 ? config.name : (type === constants.SOURCE_TYPE ? 'input_stream' : 'output_stream');

            wizardBodyContent.append(`
                <div style="max-height: ${wizardBodyContent[0].offsetHeight}; overflow: auto" class="content-section">
                    <div style="font-size: 1.8rem">
                        Configure Schema<br/>
                        <small style="font-size: 1.3rem">Configure ${type === constants.SOURCE_TYPE ? 'input' : 'output'} stream definition</small>
                    </div>
                    <div style="display: flex; padding-top:15px">
                        <div style="width: 100%;padding-top: 5px">
                            Add log sink for testing                                      
                        </div>
                        <div>
                            <button style="background-color: #ee6719" class="btn btn-default btn-circle" id="btn-enable-log-sink" type="button" data-toggle="dropdown">
                                <i class="fw ${config.addLog ? 'fw-check' : 'fw-minus'}"></i>
                            </button> 
                        </div>
                    </div>
                    <div style="padding-top: 10px">
                        <div>
                            <label for="stream-name-txt">Enter ${type === constants.SOURCE_TYPE ? 'input' : 'output'} stream name</label>
                            <input id="stream-name-txt" type="text" style="width: 100%; border: none; background-color: transparent; border-bottom: 1px solid #333" value="${config.name}">
                        </div>
                        <div style="padding-top: 10px">
                            <div style="padding-top: 15px" class="attribute-list">
                                <div>
                                  ${type === constants.SOURCE_TYPE ? 'input' : 'output'} stream attributes: 
                                  <button style="background-color: #ee6719" class="btn btn-default btn-circle" id="btn-add-stream-attrib" type="button" data-toggle="dropdown">
                                    <i class="fw fw-add"></i>
                                  </button> 
                                  <div id="stream-attribute-type-dropdown" style="left: 150px" class="dropdown-menu-style hidden" aria-labelledby="">
                                  </div>
                                </div>
                                <div style="" class="attributes">
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `);

            wizardBodyContent.find('#btn-enable-log-sink').on('click', function(evt) {
                config.addLog = !config.addLog;
                self.render();
            });
            var attributeTypeDiv = wizardBodyContent.find('#stream-attribute-type-dropdown');

            constants.SUPPORTED_DATA_TYPES.forEach(function (dataType) {
                attributeTypeDiv.append(`
                    <a id="attrib-option-${dataType.toLowerCase()}" title="Attribute of type ${dataType}" class="dropdown-item" href="#">${dataType}</a>
                `);
            });

            config.attributes.forEach(function (attribute, i) {
                wizardBodyContent.find('.attribute-list>.attributes').append(`
                    <div style="display: flex;">
                        <div class="stream-attrib-sort-div" style="display: flex;flex-direction: column;padding: 5px;">
                            <a id="move-attrib-up-${i}" title="Move attribute up the schema" style="color: #333">
                                <i class="fw fw-up"></i>
                            </a>
                            <a id="move-attrib-down-${i}" title="Move attribute down the schema" style="color: #333">
                                <i class="fw fw-down"></i>
                            </a>
                        </div>
                        <div style="width: 100%; padding-bottom: 15px" class="attribute-input-section">
                            <label style="margin-bottom: 0; font-size: 1.2rem;" for="attribute-input-${i}">${attribute.type.toUpperCase()}</label>
                            <input id="attribute-name-input-${i}" style="width: 100%; border: none; background-color: transparent; border-bottom: 1px solid #333" placeholder="Type Attribute name here" type="text" value="${attribute.name}">
                        </div>
                        <div style="padding: 20px 5px;">
                            <a title="Delete attribute from schema" style="color: #333">
                                <i id="delete-attribute-${i}" class="fw fw-delete attrib-del"></i>
                            </a>
                        </div>
                    </div>
                `);
            });

            wizardBodyContent.find('#btn-add-stream-attrib').on('mouseover', function (evt) {
                var attributeTypeDiv = wizardBodyContent.find('#stream-attribute-type-dropdown');
                var leftOffset = evt.currentTarget.offsetLeft;
                attributeTypeDiv.css({"left": `${leftOffset}px`})
                attributeTypeDiv.removeClass('hidden');

                attributeTypeDiv.on('mouseleave', function () {
                    attributeTypeDiv.addClass('hidden');
                })
            }).on('mouseleave', function (evt) {
                setTimeout(function () {
                    var attributeTypeDiv = wizardBodyContent.find('#stream-attribute-type-dropdown');
                    if (!(wizardBodyContent.find('#stream-attribute-type-dropdown:hover').length > 0)) {
                        attributeTypeDiv.addClass('hidden');
                    }
                }, 300)
            });

            wizardBodyContent.find("#stream-attribute-type-dropdown>a").on('click', function (evt) {
                var attributeType = evt.currentTarget.id.match('attrib-option-([a-zA-Z0-9-]+)')[1];
                config.attributes.push({name: '', type: attributeType});
                self.render();
            });

            wizardBodyContent.find('.attrib-del').on('click', function (evt) {
                var index = evt.currentTarget.id.match('delete-attribute-([0-9]+)')[1];
                config.attributes.splice(index, 1);
                self.render();
            });

            wizardBodyContent.find('.stream-attrib-sort-div>a').on('click', function (evt) {
                var arrowIndex = evt.currentTarget.id.match('move-attrib-([a-zA-Z0-9-]+)')[1].split('-');
                var index = Number(arrowIndex[1]);
                var temp = _.cloneDeep(config.attributes[index]);

                if (arrowIndex[0] === 'up' && index !== 0) {
                    config.attributes[index] = config.attributes[index - 1];
                    config.attributes[index - 1] = temp;
                } else if (index !== (config.attributes.length - 1)) {
                    config.attributes[index] = config.attributes[index + 1];
                    config.attributes[index + 1] = temp;
                }
                self.render();
            });

            wizardBodyContent.find('.attribute-input-section>input').on('keyup', _.debounce(function (evt) {
                var attributeIndex = Number(evt.currentTarget.id.match('attribute-name-input-([0-9]+)')[1]);

                config.attributes[attributeIndex].name = $(evt.currentTarget).val();
            }, 100, {}));

            wizardBodyContent.find('#stream-name-txt').on('keyup', _.debounce(function (evt) {
                config.name = $(evt.currentTarget).val();
            }, 100, {}));
        }

        ETLWizard.prototype.renderInputOutputMapper = function (type) {
            var self = this;
            var config = type === constants.SOURCE_TYPE ?
                this.__propertyMap.input.mapping : this.__propertyMap.output.mapping;
            var extensionConfig = type === constants.SOURCE_TYPE ?
                this.__propertyMap.input : this.__propertyMap.output;
            var wizardBodyContent = this.__parentWizardForm.find(constants.CLASS_WIZARD_MODAL_BODY);
            var mapperData = constants.SOURCE_TYPE === type ?
                this.__expressionData.extensions.sourceMapper.sourceMaps :
                this.__expressionData.extensions.sinkMapper.sinkMaps;

            wizardBodyContent.append(`
                <div style="max-height: ${wizardBodyContent[0].offsetHeight}; overflow: auto" class="content-section">
                    <div style="font-size: 1.8rem">
                        Configure ${type === constants.SOURCE_TYPE ? 'Input' : 'Output'} Mapping<br/>
                        <small style="font-size: 1.3rem">Configure ${type === constants.SOURCE_TYPE ? 'source' : 'sink'} extension mapping</small>
                    </div>
                    <div style="padding-top: 10px">
                        <div>
                            <label for="mapper-type">${type === constants.SOURCE_TYPE ? 'Source' : 'Sink'} Mapper type</label>
                            <select name="mapper-type" id="mapper-type">
                                <option disabled selected value> -- select an option -- </option>
                            </select>
                        </div>
                    </div>
                    <div id="mapper-container">
                    </div>
                </div>
            `);

            var mapperContainer = wizardBodyContent.find('#mapper-container');
            mapperContainer.empty();

            mapperData.forEach(function (map) {
                wizardBodyContent.find('#mapper-type').append(`
                    <option value="${map.name}">${map.name}</option>
                `);
            });

            wizardBodyContent.find('#mapper-type').on('change', function (evt) {
                var mapper = mapperData.find(function (map) {
                    return map.name === $(evt.currentTarget).val();
                });

                config.type = $(evt.currentTarget).val();
                config.properties = {};
                // config.possibleProperties = {};
                config.attributes = {};
                config.payload = '';
                config.customEnabled = false;
                config.samplePayload = '';

                if(mapper.parameters) {
                    mapper.parameters
                    .filter(function (el) {
                        // config.possibleProperties[el.name] = el;
                        return !el.optional;
                    })
                    .forEach(function (el) {
                        // el['value'] = el.defaultValue;
                        var mapperData = {};
                        mapperData.value = el.defaultValue;
                        mapperData.type = el.type;
                        config.properties[el.name] = mapperData;
                    });
                }

                var inputOutputMapper = new InputOutputMapper(type, mapperContainer, extensionConfig, mapper);
                inputOutputMapper.render();
            });

            if (config.type.length > 0) {
                var mapper = mapperData.find(function (map) {
                    return map.name === config.type;
                });
                wizardBodyContent.find('#mapper-type').val(config.type);
                var inputOutputMapper = new InputOutputMapper(type, mapperContainer, extensionConfig, mapper);
                inputOutputMapper.render();
            }
        }

        return ETLWizard;
    });
