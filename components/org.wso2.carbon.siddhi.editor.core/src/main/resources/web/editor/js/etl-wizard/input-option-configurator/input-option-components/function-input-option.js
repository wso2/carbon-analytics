define(['require', 'jquery', 'lodash', 'log', 'alerts', 'app/source-editor/completion-engine', 'functionModel'],

    function (require, $, _, log, Alerts, CompletionEngine, FunctionModel) {
        var FunctionInputOptionComponent = function (container, config) {
            var self = this;
            this.__container = container;
            this.__config = config;
            this.__functionData = {};
            this.__allowRepetitiveParameters = false;
            this.__repetitiveParameterTypes = [];

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

            console.log(config);

            container.empty();
            container.append(`
                <h3 style="margin-top: 0">Function configuration</h3>
                <div>
                    <label style="color: white" for="function-name">Window type&nbsp;:&nbsp;</label>
                    <select name="function-name" id="function-name">
                        <option disabled selected value> -- select an option -- </option>
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

            container.find('#function-name')
                .on('change', function (evt) {
                    var functionID = $(evt.currentTarget).val();
                    var functionData = self.__functionData[functionID];

                    if (functionData.syntax.length > 1) {
                        var functionDataContainer = container.find('.function-parameter-section');
                        functionDataContainer.empty();
                        functionDataContainer.append('<h6 style="color: white">Select function syntax to proceed</h6>');
                        var functionList = $('<ul></ul>');

                        functionData.syntax.forEach(function (syntax, i) {
                            functionList.append(`
                                <li class="" id="syntax-id-${i}">
                                    <a>
                                        <div style="padding: 10px 15px;border-bottom: 1px solid rgba(255, 255, 255, 0.2);" >
                                            <b>${syntax.syntax.replaceAll(/</g, '&lt;').replaceAll(/>/g, '&gt;')}</b>
                                        </div>
                                    </a>    
                                </li>
                            `)
                        });

                        functionDataContainer.append(functionList);
                        functionList.find('li').on('click', function (evt) {
                            var syntaxIndex = evt.currentTarget.id.match('syntax-id-([0-9]+)')[1];
                            config.query.function['name'] = functionID;
                            config.query.function['syntax'] = functionData.syntax[Number(syntaxIndex)];
                            config.query.function['syntax'].parameterData = _.reduce(functionData.parameters, function (obj, param) {
                                obj[param.name] = param.description
                                return obj
                            }, {});
                            config.query.function['parameters'] = self.generateParameters(config.query.function['syntax']);
                            self.render();
                        });
                    } else {
                        config.query.function['name'] = functionID;
                        config.query.function['syntax'] = functionData.syntax[0];
                        config.query.function['syntax'].parameterData = _.reduce(functionData.parameters, function (obj, param) {
                            obj[param.name] = param.description
                            return obj
                        }, {})
                        config.query.function['parameters'] = self.generateParameters(config.query.function['syntax']);
                        self.render();
                    }
                });

            if (Object.keys(config.query.function).length > 0) {

                var functionDataContainer = container.find('.function-parameter-section');
                functionDataContainer.empty();
                functionDataContainer.append('<h6 style="color: white">Select function syntax to proceed</h6>');

                config.query.function.parameters.forEach(function (param, i) {
                    functionDataContainer.append(`
                        <div style="width: 100%; padding-bottom: 10px" class="input-section">
                            <label style="margin-bottom: 0" class="${param.value.length > 0 ? '' : 'not-visible'}" id="label-function-param-${i}" for="function-param-${i}">${param.name}</label>
                            <input id="function-param-${i}" style="width: 100%; border: none; background-color: transparent; border-bottom: 1px solid rgba(255,255,255,0.8)" placeholder="${param.name}" type="text" value="${param.value}">
                        </div>
                    `);
                });

                container.find('.function-parameter-section .input-section input')
                    .on('focus', function (evt) {
                        var inputId = evt.currentTarget.id.match('function-param-([0-9]+)')[1];
                        container.find(`#label-function-param-${inputId}`).removeClass('not-visible');
                        $(evt.currentTarget).attr('placeholder', 'Type here to input the value');
                    })
                    .on('focusout', function (evt) {
                        var inputId = evt.currentTarget.id.match('function-param-([0-9]+)')[1];
                        if($(evt.currentTarget).val().length === 0) {
                            container.find(`#label-function-param-${inputId}`).addClass('not-visible');
                            $(evt.currentTarget).attr('placeholder', inputId.replaceAll(/-/g,'.'));
                        }
                    })
                    .on('keyup', _.debounce(function (evt) {
                        var inputId = evt.currentTarget.id.match('function-param-([0-9]+)')[1];
                        config.query.function.parameters[inputId].value = $(evt.currentTarget).val();
                        console.log(config.query.function);
                    }, 100, {}))
            }
        }

        FunctionInputOptionComponent.prototype.generateParameters = function (syntax) {
            var parameters = [];
            var regExp = /\(([^)]+)\)/;
            var allowRepetitive = false;
            var repetitiveDataTypes = [];

            regExp.exec(syntax.syntax) ? regExp.exec(syntax.syntax)[1].split(',').forEach(function (param) {
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