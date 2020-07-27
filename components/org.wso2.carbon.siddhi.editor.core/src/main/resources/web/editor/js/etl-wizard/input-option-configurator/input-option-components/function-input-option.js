define(['require', 'jquery', 'lodash', 'log', 'alerts', 'app/source-editor/completion-engine', 'functionModel'],

    function (require, $, _, log, Alerts, CompletionEngine, FunctionModel) {
        var FunctionInputOptionComponent = function (container, config) {
            var self = this;
            this.__container = container;
            this.__config = config;
            this.__functionData = {};
            this.__allowRepetitiveParameters = false;
            this.__repetitiveParameterTypes = [];
            this.__repetitiveParamName = '';
            this.__repetitiveParameterDescription = '';

            var extensionData = CompletionEngine.getRawMetadata().extensions;

            Object.keys(extensionData)
                .filter(function (key) {
                    return extensionData[key].streamProcessors.length > 0;
                })
                .forEach(function (key) {
                    extensionData[key].streamProcessors.forEach(function (funcData) {
                        self.__functionData[`${funcData.namespace}:${funcData.name}`] = funcData;
                    });
                });

        }

        FunctionInputOptionComponent.prototype.constructor = FunctionInputOptionComponent;

        FunctionInputOptionComponent.prototype.render = function () {
            var self = this;
            var container = this.__container;
            var config = this.__config;

            container.empty();
            container.append(`
                <h3 style="margin-top: 0;color: #373737">Function configuration</h3>
                <div style="color: #373737">
                    <label for="function-name">Function type&nbsp;:&nbsp;</label>
                    <select name="function-name" id="function-name">
                        <option disabled selected value="0"> -- select an option -- </option>
                    </select>
                </div>
                <div style="padding: 0 5px" class="function-parameter-section">
                </div>
            `);

            Object.keys(this.__functionData).forEach(function (key) {
                container.find('#function-name').append(`
                    <option>${key}</option>
                `);
            });

            if (config.query.function.text) {
                var functionName = config.query.function.text.match('([a-zA-Z:_0-9]+)\(.*\)')[1];
                container.find('.function-parameter-section')
                    .append(`
                        <div style="display:flex;margin-top: 15px; text-align: center; font-size: 1.8rem;">
                            <div style="flex: 1">
                                <span>${config.query.function.text}</span>
                            </div>
                            <div>
                                <a style="color: #323232">
                                    <i title="clear stream processor function" class="fw fw-clear"></i>    
                                </a>    
                            </div>
                        </div>
                    `);

                container.find('#function-name').val(functionName);
                container.find('.fw-clear').on('click', function(evt) {
                    config.query.function = {enable: true}
                    self.render();
                });
            } else {
                if (config.query.function.name) {
                    container.find('#function-name').val(config.query.function.name);
                }

                if (config.query.function['parameters']
                    && Object.keys(config.query.function['parameters']).length > 0) {
                    var functionDataContainer = container.find('.function-parameter-section');
                    var functionData = self.__functionData[config.query.function.name];

                    functionDataContainer.empty();
                    functionDataContainer.append('<h6 style="color: #373737">Function Parameters:</h6>');

                    Object.keys(config.query.function.parameters)
                        .forEach(function(key, i) {
                            var paramData = config.query.function.parameters[key];
                            var paramDescription = functionData.parameters
                                .filter(datum => datum.name === key);
                            functionDataContainer.append(`
                                <div style="display: flex">
                                    <div style="flex:1; padding-bottom: 10px" class="input-section">
                                        <label 
                                            style="margin-bottom: 0" 
                                            class="${paramData.value.length > 0 ? '' : 'not-visible'}" 
                                            id="label-function-param-${key.replaceAll(/\./g, '-')}" 
                                            for="function-param-${key.replaceAll(/\./g, '-')}"
                                        >
                                            ${key}
                                        </label>
                                        <input id="function-param-${key.replaceAll(/\./g, '-')}" 
                                            style="width: 100%; border: none; background-color: transparent; 
                                            border-bottom: 1px solid #373737" placeholder="${key}" 
                                            type="text" value="${paramData.value}">
                                    </div>
                                    <div style="padding: 20px 0">
                                        <a style="color: #323232">
                                            <i title="${paramDescription[0] ? 
                                                paramDescription[0].description 
                                                : self.__repetitiveParameterDescription}" class="fw fw-info"></i>
                                        </a>
                                    </div>
                                </div>
                            `);
                        });
                    
                    if(self.__allowRepetitiveParameters) {
                        functionDataContainer.append(`
                            <button id="btn-add-repetitive-param" class="btn btn-default">
                                Add repetitive parameter
                            </button>
                        `);

                        functionDataContainer.find('#btn-add-repetitive-param')
                            .on('click', function(evt) {
                                var number = 0;
                                Object.keys(config.query.function.parameters)
                                    .forEach(function(key, i) {
                                        if(key.startsWith(self.__repetitiveParamName)) {
                                            number++;
                                        }
                                    });
                                config.query.function.parameters[`${self.__repetitiveParamName}.${number}`] = {
                                    value: '',
                                    type: self.__repetitiveParameterTypes
                                };

                                self.render();
                            })
                    }

                    container.find('.function-parameter-section .input-section input')
                        .on('focus', function (evt) {
                            var inputId = evt.currentTarget.id.match('function-param-([a-zA-Z\-\.0-9]+)')[1];
                            container.find(`#label-function-param-${inputId}`).removeClass('not-visible');
                            $(evt.currentTarget).attr('placeholder', 'Type here to input the value');
                        })
                        .on('focusout', function (evt) {
                            var inputId = evt.currentTarget.id.match('function-param-([a-zA-Z\-\.0-9]+)')[1];
                            if ($(evt.currentTarget).val().length === 0) {
                                container.find(`#label-function-param-${inputId}`).addClass('not-visible');
                                $(evt.currentTarget)
                                    .attr('placeholder', container.find(`#label-function-param-${inputId}`).html());
                            }
                        })
                        .on('keyup', _.debounce(function (evt) {
                            var inputId = evt.currentTarget.id
                                .match('function-param-([a-zA-Z\-\.0-9]+)')[1].replaceAll(/-/g, '.');
                            config.query.function.parameters[inputId].value = $(evt.currentTarget).val();
                        }, 100, {}))
                }
            }

            container.find('#function-name')
                .on('change', function (evt) {
                    var functionID = $(evt.currentTarget).val();
                    var functionData = self.__functionData[functionID];
                    self.__allowRepetitiveParameters = false;
                    var syntax = null;
                    config.query.function = {enable: true, name: functionID}

                    if (functionData.syntax.length > 1) {
                        var functionDataContainer = container.find('.function-parameter-section');
                        functionDataContainer.empty();
                        functionDataContainer.append('<h6 style="color: #373737">' +
                            'Select function syntax to proceed</h6>');
                        var functionList = $('<ul></ul>');

                        functionData.syntax.forEach(function (syntax, i) {
                            functionList.append(`
                                    <li class="" id="syntax-id-${i}">
                                        <a style="color:#333">
                                            <div style="padding: 10px 15px;border-bottom: 1px solid #373737" >
                                                <b>${syntax.syntax.replaceAll(/</g, '&lt;')
                                                            .replaceAll(/>/g, '&gt;')}</b>
                                            </div>
                                        </a>    
                                    </li>
                                `)
                        });

                        functionDataContainer.append(functionList);
                        functionList.find('li').on('click', function (evt) {
                            var syntaxIndex = evt.currentTarget.id.match('syntax-id-([0-9]+)')[1];
                            self.renderParametersFromSyntax(functionData, functionData.syntax[Number(syntaxIndex)]);
                        });
                    } else {
                        self.renderParametersFromSyntax(functionData, functionData.syntax[0])
                    }
                });
        }

        FunctionInputOptionComponent.prototype.renderParametersFromSyntax = function (functionData, syntax) {
            var self = this;
            var config = this.__config;            
            var functionParameterRegexp = /\(([^)]+)\)/;
            var parameters = functionParameterRegexp.exec(syntax.syntax)[1].split(',');
            config.query.function['parameters'] = {}
            parameters.forEach(function (param) {
                var parameterName = param.trim().split(' ')[1];
                if(parameterName !== '...') {
                    var paramData = functionData.parameters.find(function (paramDat) {
                        return paramDat.name === parameterName;
                    })
                    config.query.function.parameters[parameterName] = {
                        value: paramData.defaultValue,
                        type: paramData.type
                    }
                } else{
                    var paramDataIndex = functionData.parameters.map(function (param) {
                        return param.name;
                    }).length-1;

                    if(paramDataIndex > -1) {
                        self.__allowRepetitiveParameters = true;
                        self.__repetitiveParameterTypes = functionData.parameters[paramDataIndex].type;
                        self.__repetitiveParamName = functionData.parameters[paramDataIndex].name;
                        self.__repetitiveParameterDescription = functionData.parameters[paramDataIndex].description;
                    }
                }
            });

            self.render();
        }

        FunctionInputOptionComponent.prototype.generateParameters = function (syntax) {
            var parameters = [];
            var functionParameterRegexp = /\(([^)]+)\)/;
            var allowRepetitive = false;
            var repetitiveDataTypes = [];

            functionParameterRegexp.exec(syntax.syntax) ?
                functionParameterRegexp.exec(syntax.syntax)[1].split(',').forEach(function (param) {
                var temp = param.trim().split(' ');

                var dataTypes = temp[0].match(/<(.*?)>/)[1].split('|').map(function (type) {
                    return type.toLowerCase();
                });

                var placeHolder = syntax.parameterData[temp[1]];

                if (!(temp[1].indexOf('...') > -1)) {
                    var paramNode = {};
                    paramNode.name = temp[1];
                    paramNode.dataTypes = dataTypes;
                    paramNode.placeholder = placeHolder;
                    paramNode.value = '';

                    parameters.push(paramNode);
                } else {
                    allowRepetitive = true;
                    repetitiveDataTypes = dataTypes;
                }
            }) : null;

            this.__allowRepetitiveParameters = allowRepetitive;
            this.__repetitiveParameterTypes = repetitiveDataTypes;
            return parameters;
        }

        return FunctionInputOptionComponent;
    });