/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

define(['log', 'jquery', 'lodash', 'sourceOrSinkAnnotation', 'mapAnnotation', 'payloadOrAttribute',
    'jsonValidator', 'handlebar', 'designViewUtils'],
    function (log, $, _, SourceOrSinkAnnotation, MapAnnotation, PayloadOrAttribute, JSONValidator, Handlebars,
        DesignViewUtils) {

        /**
         * @class SinkForm Creates a forms to collect data from a sink
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var SinkForm = function (options) {
            if (options !== undefined) {
                this.configurationData = options.configurationData;
                this.application = options.application;
                this.formUtils = options.formUtils;
                this.consoleListManager = options.application.outputController;
                var currentTabId = this.application.tabController.activeTab.cid;
                this.designViewContainer = $('#design-container-' + currentTabId);
                this.toggleViewButton = $('#toggle-view-button-' + currentTabId);
            }
        };

        const constAttribute = "attributes";
        const constPayload = "payload";
        const constMap = "map";
        const constList = "list";

        /**
         * Function to get the options of the selected sink/map type
         * @param {String} selectedType Selected sink/map type
         * @param {object} types Predefined sink/map types
         * @return {object} options
         */
        var getSelectedTypeOptions = function (selectedType, types) {
            var options = [];
            for (type of types) {
                if (type.name.toLowerCase() == selectedType.toLowerCase()) {
                    options = type.parameters;
                    break;
                }
            }
            return options;
        };

        /**
         * Function to render the options for the selected map/sink type using handlebars
         * @param {Object} optionsArray Saved options
         * @param {Object} customizedMapperOptions Options typed by the user which aren't one of the predefined option
         * @param {String} id Id for the div to embed the options
         */
        var renderOptions = function (optionsArray, customizedOptions, id) {
            optionsArray.sort(function (val1, val2) {
                if (val1.optional && !val2.optional) return 1;
                else if (!val1.optional && val2.optional) return -1;
                else return 0;
            });
            var optionsTemplate = Handlebars.compile($('#source-sink-store-options-template').html());
            var wrappedHtml = optionsTemplate({
                id: id,
                options: optionsArray,
                customizedOptions: customizedOptions
            });
            $('#' + id + '-options-div').html(wrappedHtml);
            changeCustOptDiv();
        };

        /**
         * Function to render the select options for the map type using handlebars
         * @param {Object} predefinedSinkMaps Predefined map annotations
         */
        var renderMap = function (predefinedSinkMaps) {
            if (!$.trim($('#define-map').html()).length) {
                var mapFormTemplate = Handlebars.compile($('#type-selection-form-template').html());
                var wrappedHtml = mapFormTemplate({ id: "map", types: predefinedSinkMaps });
                $('#define-map').html(wrappedHtml);
                $('#define-map #map-type').val('passThrough');
                $('#define-map #map-type option:contains("passThrough")').text('passThrough (default)');
            }
        };

        /**
         * Function to map the saved option values to the option object
         * @param {Object} predefinedOptions Predefined options of a particular sink/map annotation type
         * @param {Object} savedOptions Saved options
         * @return {Object} options
        */
        var mapUserOptionValues = function (predefinedOptions, savedOptions) {
            var options = [];
            _.forEach(predefinedOptions, function (predefinedOption) {
                var foundPredefinedOption = false;
                for (var savedOption of savedOptions) {
                    var optionKey = savedOption.split('=')[0].trim();
                    var optionValue = savedOption.split('=')[1].trim();
                    optionValue = optionValue.substring(1, optionValue.length - 1);
                    if (optionKey.toLowerCase() == predefinedOption.name.toLowerCase()) {
                        foundPredefinedOption = true;
                        options.push({
                            key: predefinedOption.name, value: optionValue, description: predefinedOption
                                .description, optional: predefinedOption.optional,
                            defaultValue: predefinedOption.defaultValue
                        });
                        break;
                    }
                }
                if (!foundPredefinedOption) {
                    options.push({
                        key: predefinedOption.name, value: "", description: predefinedOption
                            .description, optional: predefinedOption.optional, defaultValue: predefinedOption.defaultValue
                    });
                }
            });
            return options;
        };

        /**
         * Function to render the html to display the select options for attribute mapping
         */
        var renderAttributeMapping = function () {
            if (!$.trim($('#define-attribute').html()).length) {
                var attributeDiv = $('<div class="clearfix"> <label id="attribute-map-label">' +
                    '<input type="checkbox" id="attributeMap-checkBox"> Payload or Attribute Mapping' +
                    '</label> </div> <div class = "clearfix"> <select id = "attributeMap-type" disabled>' +
                    '<option value = "attributeMap"> Enter attributes as key/value pairs </option>' +
                    '<option value = "payloadMap"> Enter payload as key/value pairs </option>' +
                    '<option value = "payloadList"> Enter a single payload attribute </option>' +
                    '</select></div>');
                $('#define-attribute').html(attributeDiv);
            }
        };

        /**
         * Function to obtain the customized option entered by the user in the source view
         * @param {Object} predefinedOptions Predefined options of a particular sink/map annotation type
         * @param {Object} savedOptions Options defined by the user in the source view
         * @return {Object} customizedOptions
         */
        var getCustomizedOptions = function (predefinedOptions, savedOptions) {
            var customizedOptions = [];
            _.forEach(savedOptions, function (savedOption) {
                var foundSavedOption = false;
                for (var predefinedOption of predefinedOptions) {
                    var optionKey = savedOption.split('=')[0];
                    var optionValue = savedOption.split('=')[1].trim();
                    optionValue = optionValue.substring(1, optionValue.length - 1);
                    if (predefinedOption.name.toLowerCase() == optionKey.toLowerCase().trim()) {
                        foundSavedOption = true;
                        break;
                    }
                }
                if (!foundSavedOption) {
                    customizedOptions.push({ key: optionKey, value: optionValue });
                }
            });
            return customizedOptions;
        };

        /**
         * Function to create option object with an additional empty value attribute
         * @param {Object} optionArray Predefined options without the attribute 'value'
         * @return {Object} options
         */
        var createOptionObjectWithValues = function (optionArray) {
            var options = [];
            _.forEach(optionArray, function (option) {
                options.push({
                    key: option.name, value: "", description: option.description, optional: option.optional,
                    defaultValue: option.defaultValue
                });
            });
            return options;
        };

        /**
         * Function to create attribute-map object with the saved attribute-map
         * @param {Object} savedMapperAttributes Saved attribute-map
         * @param {Object} streamAttributes Attributes of the connected stream
         * @return {Object} attributes
         */
        var createAttributeObjectList = function (savedMapperAttributes, streamAttributes) {
            var attributeType = savedMapperAttributes.getType().toLowerCase();
            var annotationType = savedMapperAttributes.getAnnotationType().toLowerCase();
            var attributeValues = savedMapperAttributes.getValue();
            var attributes = [];
            if ((annotationType === constAttribute && attributeType === constMap) || (annotationType === constPayload &&
                attributeType === constMap)) {
                if (annotationType === constAttribute) {
                    $('#define-attribute #attributeMap-type').val('attributeMap');
                } else {
                    $('#define-attribute #attributeMap-type').val('payloadMap');
                }
                for (streamAttribute of streamAttributes) {
                    attributes.push({ key: streamAttribute.key, value: "" });
                }
                for (var mappedAttribute of attributes) {
                    for (var attribute in attributeValues) {
                        if (mappedAttribute.key === attribute) {
                            mappedAttribute.value = attributeValues[attribute]
                            break;
                        }
                    }
                }
            } else if (annotationType === constAttribute && attributeType === constList) {
                $('#define-attribute #attributeMap-type').val('attributeMap');
                for (streamAttribute of streamAttributes) {
                    attributes.push({ key: streamAttribute.key, value: "" });
                }

                var i = 0;
                for (var attribute in attributeValues) {
                    if (i < streamAttributes.length) {
                        attributes[i].value = attributeValues[attribute];
                        i++;
                    }
                }
            } else if (annotationType === constPayload && attributeType === constList) {
                $('#define-attribute #attributeMap-type').val('payloadList');
                attributes.push({ value: attributeValues[0] });
            }
            return attributes;
        };

        /**
         * Function to create attribute-map objects with empty values
         * @param {Object} streamAttributes Attributes if the connected stream
         * @return {Object} attributes
         */
        var initialiseAttributeContent = function (streamAttributes) {
            var selectedAttributeType = $('#define-attribute #attributeMap-type').val();
            var attributes = [];
            if (selectedAttributeType === 'attributeMap' || selectedAttributeType === 'payloadMap') {
                for (var streamAttribute of streamAttributes) {
                    attributes.push({ key: streamAttribute.key, value: "" });
                }
            } else {
                attributes.push({ value: "" });
            }
            return attributes;
        };

        /**
         * Function to render the attribute-map div using handlebars
         * @param {Object} attributes which needs to be mapped on to the template
         */
        var renderAttributeMappingContent = function (attributes) {
            var attributeMapFormTemplate = Handlebars.compile($('#source-sink-map-attribute-template').html());
            var wrappedHtml = attributeMapFormTemplate(attributes);
            $('#attribute-map-content').html(wrappedHtml);
        }

        /**
         * Function to obtain the connected stream's attributes
         * @param {Object} streamList List of all stream objects
         * @param {String} connectedElement source's connected element's name
         * @return {Object} streamAttributes
         */
        var getConnectStreamAttributes = function (streamList, connectedElement) {
            var streamAttributes = [];
            for (var stream of streamList) {
                if (stream.name == connectedElement) {
                    var attributeList = stream.getAttributeList();
                    _.forEach(attributeList, function (attribute) {
                        streamAttributes.push({ key: attribute.getName(), value: "" });
                    })
                    break;
                }
            }
            return streamAttributes;
        };

        /**
         * Function to validate the customized options
         * @param {Object} selectedOptions options which needs to be saved
         * @param {String} id to identify the div in the html to traverse
         * @return {boolean} isError
         */
        var validateCustomizedOptions = function (selectedOptions, id) {
            var isError = false;
            var option = "";
            if ($('#customized-' + id + ' ul').has('li').length != 0) {
                $('#customized-' + id + ' .option').each(function () {
                    var custOptName = $(this).find('.cust-option-key').val().trim();
                    var custOptValue = $(this).find('.cust-option-value').val().trim();
                    if ((custOptName != "") || (custOptValue != "")) {
                        if (custOptName == "") {
                            $(this).find('.error-message').text('Option key is required.');
                            $(this)[0].scrollIntoView();
                            $(this).find('.cust-option-key').addClass('required-input-field');
                            isError = true;
                            return false;
                        } else if (custOptValue == "") {
                            $(this).find('.error-message').text('Option value is required.');
                            $(this)[0].scrollIntoView();
                            $(this).find('.cust-option-value').addClass('required-input-field');
                            isError = true;
                            return false;
                        } else {
                            option = custOptName + " = \"" + custOptValue + "\"";
                            selectedOptions.push(option);
                        }
                    }
                });
            }
            return isError;
        };

        /**
         * Function to obtain a particular option from predefined option
         * @param {String} optionName option which needs to be found
         * @param {Object} predefinedOptions set of predefined option
         * @return {Object} option
         */
        var getOption = function (optionName, predefinedOptions) {
            var option = null;
            for (var predefinedOption of predefinedOptions) {
                if (predefinedOption.name.toLowerCase() == optionName.toLowerCase()) {
                    option = predefinedOption;
                    break;
                }
            }
            return option;
        };

        /**
        * Function to validate the data type of the options
        * @param {String} dataType data-type of the option
        * @param {String} optionValue value of the option
        * @return {boolean} invalidDataType
        */
        var validateDataType = function (dataType, optionValue) {
            var invalidDataType = false;
            intLongRegexMatch = /^[-+]?\d+$/;
            doubleFloatRegexMatch = /^[+-]?([0-9]*[.])?[0-9]+$/;

            if (dataType === "INT" || dataType === "LONG") {
                if (!optionValue.match(intLongRegexMatch)) {
                    invalidDataType = true;
                }
            } else if (dataType === "DOUBLE" || dataType === "FLOAT") {
                if (!optionValue.match(doubleFloatRegexMatch)) {
                    invalidDataType = true;
                }
            } else if (dataType === "BOOL") {
                if (!(optionValue.toLowerCase() === "false" || optionValue.toLowerCase() === "true")) {
                    invalidDataType = true;
                }
            }
            return invalidDataType;
        };

        /** Function to change the heading and the button text of the customized options div */
        var changeCustOptDiv = function () {
            var sourceCustOptionList = $('.source-sink-map-options #customized-sink-options').
                find('.cust-options li');
            var sourceDivParent = $('.source-sink-map-options #customized-sink-options');
            if (sourceCustOptionList.length > 0) {
                sourceDivParent.find('h3').show();
                sourceDivParent.find('.btn-add-options').html('Add more');
            } else {
                sourceDivParent.find('h3').hide();
                sourceDivParent.find('.btn-add-options').html('Add customized option');
            }
            var mapperCustOptionList = $('.source-sink-map-options #customized-mapper-options').
                find('.cust-options li');
            var mapperDivParent = $('.source-sink-map-options #customized-mapper-options');
            if (mapperCustOptionList.length > 0) {
                mapperDivParent.find('h3').show();
                mapperDivParent.find('.btn-add-options').html('Add more');
            } else {
                mapperDivParent.find('h3').hide();
                mapperDivParent.find('.btn-add-options').html('Add customized option');
            }
        };

        /**
         * Function to validate the predefined options
         * @param {Object} selectedOptions array to add the options which needs to be saved
         * @param {Object} predefinedOptions
         * @param {String} id to identify the div in the html to traverse
         * @return {boolean} isError
         */
        var validateOptions = function (selectedOptions, predefinedOptions, id) {
            var isError = false;
            var option = "";
            $('.source-sink-map-options #' + id + ' .option').each(function () {
                var optionName = $(this).find('.option-name').text().trim();
                var optionValue = $(this).find('.option-value').val().trim();
                var predefinedOptionObject = getOption(optionName, predefinedOptions);
                if (!predefinedOptionObject.optional) {
                    if (optionValue == "") {
                        $(this).find('.error-message').text('Option value is required.');
                        $(this)[0].scrollIntoView();
                        $(this).find('.option-value').addClass('required-input-field');
                        isError = true;
                        return false;
                    } else {
                        var dataType = predefinedOptionObject.type[0];
                        if (validateDataType(dataType, optionValue)) {
                            $(this).find('.error-message').text('Invalid data-type. ' + dataType + ' required.');
                            $(this)[0].scrollIntoView();
                            $(this).find('.option-value').addClass('required-input-field');
                            isError = true;
                            return false;
                        }
                    }
                    option = optionName + " = \"" + optionValue + "\"";
                    selectedOptions.push(option);
                } else {
                    if ($(this).find('.option-checkbox').is(":checked")) {
                        if (optionValue == "") {
                            $(this).find('.error-message').text('Option value is required.');
                            $(this)[0].scrollIntoView();
                            $(this).find('.option-value').addClass('required-input-field');
                            isError = true;
                            return false;
                        } else {
                            var dataType = predefinedOptionObject.type[0];
                            if (validateDataType(dataType, optionValue)) {
                                $(this).find('.error-message').text('Invalid data-type. ' + dataType + ' required.');
                                $(this)[0].scrollIntoView();
                                $(this).find('.option-value').addClass('required-input-field');
                                isError = true;
                                return false;
                            }
                        }
                        option = optionName + " = \"" + optionValue + "\"";
                        selectedOptions.push(option);
                    }
                }
            });
            return isError;
        };

        /**
         * @function generate properties form for a sink
         * @param element selected element(sink)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        SinkForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');
            var clickedElement = self.configurationData.getSiddhiAppConfig().getSink(id);

            var isSinkConnected = true;
            if ($('#' + id).hasClass('error-element')) {
                isSinkConnected = false;
                DesignViewUtils.prototype.errorAlert("Please connect to a stream");
            } else if (!JSONValidator.prototype.validateSourceOrSinkAnnotation(clickedElement, 'Sink', true)) {
                // perform JSON validation to check if sink contains a connectedElement.
                isSinkConnected = false;
            }
            if (!isSinkConnected) {
                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');
            } else {
                $('#' + id).addClass('selected-element');
                $(".overlayed-container").fadeTo(200, 1);
                var streamList = self.configurationData.getSiddhiAppConfig().getStreamList();
                var connectedElement = clickedElement.connectedElementName;
                var predefinedSinks = _.orderBy(this.configurationData.rawExtensions["sink"], ['name'], ['asc']);
                var predefinedSinkMaps = _.orderBy(this.configurationData.rawExtensions["sinkMaps"], ['name'], ['asc']);
                var streamAttributes = getConnectStreamAttributes(streamList, connectedElement);
                var propertyDiv = $('<div class="source-sink-form-container sink-div"><div id="define-sink"></div>' +
                    '<div class = "source-sink-map-options" id="sink-options-div"></div>' +
                    '<button type="submit" id ="btn-submit" class="btn toggle-view-button"> Submit </button>' +
                    '<button id="btn-cancel" type="button" class="btn btn-default"> Cancel </button> </div>' +
                    '<div class="source-sink-form-container mapper-div"> <div id="define-map"> </div>' +
                    '<div class="source-sink-map-options" id="mapper-options-div"></div>' +
                    '</div> <div class= "source-sink-form-container attribute-map-div"><div id="define-attribute">' +
                    '</div> <div id="attribute-map-content"></div> </div>');
                formContainer.append(propertyDiv);
                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');

                //declaration of variables
                var sinkOptions = [];
                var sinkOptionsWithValues = [];
                var customizedSinkOptions = [];
                var mapperOptions = [];
                var mapperOptionsWithValues = [];
                var customizedMapperOptions = [];
                var attributes = [];

                //event listener to show option description
                $('.source-sink-map-options').on('mouseover', '.option-desc', function () {
                    $(this).find('.option-desc-content').show();
                });

                //event listener to hide option description
                $('.source-sink-map-options').on('mouseout', '.option-desc', function () {
                    $(this).find('.option-desc-content').hide();
                });

                //event listener when the option checkbox is changed
                $('.source-sink-map-options').on('change', '.option-checkbox', function () {
                    var optionParent = $(this).parents(".option");
                    if ($(this).is(':checked')) {
                        optionParent.find(".option-value").show();
                    } else {
                        optionParent.find(".option-value").hide();
                        optionParent.find(".option-value").removeClass("required-input-field");
                        optionParent.find(".error-message").text("");
                    }
                });

                var customizedOptionDiv = '<li class="option">' +
                    '<div class = "clearfix"> <label>option.key</label> <input type="text" class="cust-option-key"' +
                    'value=""> </div> <div class="clearfix"> <label>option.value</label> ' +
                    '<input type="text" class="cust-option-value" value="">' +
                    '<a class = "btn-del btn-del-option"><i class="fw fw-delete"></i></a></div>' +
                    '<label class = "error-message"></label></li>';

                //onclick to add customized sink option
                $('#sink-options-div').on('click', '#btn-add-sink-options', function () {
                    $('#customized-sink-options .cust-options').append(customizedOptionDiv);
                    changeCustOptDiv();
                });

                //onclick to add customized mapper option
                $('#mapper-options-div').on('click', '#btn-add-mapper-options', function () {
                    $('#customized-mapper-options .cust-options').append(customizedOptionDiv);
                    changeCustOptDiv();
                });

                //onclick to delete customized option
                $('.source-sink-form-container').on('click', '.btn-del-option', function () {
                    $(this).closest('li').remove();
                    changeCustOptDiv();
                });

                //event listener for attribute-map checkbox
                $('#define-attribute').on('change', '#attributeMap-checkBox', function () {
                    if ($(this).is(':checked')) {
                        var attributes = [];
                        if (map && map.getPayloadOrAttribute()) {
                            attributes = createAttributeObjectList(savedMapperAttributes, streamAttributes);
                        } else {
                            attributes = initialiseAttributeContent(streamAttributes);
                        }
                        $('#attribute-map-content').show();
                        renderAttributeMappingContent(attributes)
                        $('#define-attribute #attributeMap-type').prop('disabled', false);
                    } else {
                        $('#attribute-map-content').hide();
                        $("#define-attribute #attributeMap-type").prop('disabled', 'disabled');
                    }
                });

                //get the clicked element's information
                var type = clickedElement.getType();
                var savedSinkOptions = clickedElement.getOptions();
                var map = clickedElement.getMap();

                //render the template to select the sink type
                var sinkFormTemplate = Handlebars.compile($('#type-selection-form-template').html());
                var wrappedHtml = sinkFormTemplate({ id: "sink", types: predefinedSinks });
                $('#define-sink').html(wrappedHtml);

                //onchange of the sink-type selection
                $('#sink-type').change(function () {
                    sinkOptions = getSelectedTypeOptions(this.value, predefinedSinks);
                    if (type && (type.toLowerCase() == this.value.toLowerCase()) && savedSinkOptions) {
                        //if the selected type is same as the saved sink-type
                        sinkOptionsWithValues = mapUserOptionValues(sinkOptions, savedSinkOptions);
                        customizedSinkOptions = getCustomizedOptions(sinkOptions, savedSinkOptions);
                    } else {
                        sinkOptionsWithValues = createOptionObjectWithValues(sinkOptions);
                        customizedSinkOptions = [];
                    }
                    renderOptions(sinkOptionsWithValues, customizedSinkOptions, "sink");
                    if (!map) {
                        renderMap(predefinedSinkMaps);
                        customizedMapperOptions = [];
                        mapperOptions = getSelectedTypeOptions("passThrough", predefinedSinkMaps);
                        mapperOptionsWithValues = createOptionObjectWithValues(mapperOptions);
                        renderOptions(mapperOptionsWithValues, customizedMapperOptions, "mapper")
                        renderAttributeMapping();
                    }
                });

                // if sink type is defined
                if (type) {
                    $('#define-sink').find('#sink-type option').filter(function () {
                        return ($(this).val().toLowerCase() == (type.toLowerCase()));
                    }).prop('selected', true);
                    sinkOptions = getSelectedTypeOptions(type, predefinedSinks);
                    if (savedSinkOptions) {
                        //get the savedSourceoptions values and map it
                        sinkOptionsWithValues = mapUserOptionValues(sinkOptions, savedSinkOptions);
                        customizedSinkOptions = getCustomizedOptions(sinkOptions, savedSinkOptions);
                    } else {
                        //create option object with empty values
                        sinkOptionsWithValues = createOptionObjectWithValues(sinkOptions);
                        customizedSinkOptions = [];
                    }
                    renderOptions(sinkOptionsWithValues, customizedSinkOptions, "sink");
                    if (!map) {
                        renderMap(predefinedSinkMaps);
                        customizedMapperOptions = [];
                        mapperOptions = getSelectedTypeOptions("passThrough", predefinedSinkMaps);
                        mapperOptionsWithValues = createOptionObjectWithValues(mapperOptions);
                        renderOptions(mapperOptionsWithValues, customizedMapperOptions, "mapper")
                        renderAttributeMapping();
                    }
                }

                //if map is defined
                if (map) {
                    renderMap(predefinedSinkMaps);
                    renderAttributeMapping();
                    var mapperType = map.getType();
                    var savedMapperOptions = map.getOptions();
                    var savedMapperAttributes = map.getPayloadOrAttribute();

                    if (mapperType) {
                        $('#define-map').find('#map-type option').filter(function () {
                            return ($(this).val().toLowerCase() == (mapperType.toLowerCase()));
                        }).prop('selected', true);
                        mapperOptions = getSelectedTypeOptions(mapperType, predefinedSinkMaps);
                        if (savedMapperOptions) {
                            //get the savedMapoptions values and map it
                            mapperOptionsWithValues = mapUserOptionValues(mapperOptions, savedMapperOptions);
                            customizedMapperOptions = getCustomizedOptions(mapperOptions, savedMapperOptions);
                        } else {
                            //create option object with empty values
                            mapperOptionsWithValues = createOptionObjectWithValues(mapperOptions);
                            customizedMapperOptions = [];
                        }
                        renderOptions(mapperOptionsWithValues, customizedMapperOptions, "mapper");
                    }
                    if (savedMapperAttributes) {
                        $('#define-attribute #attributeMap-checkBox').prop('checked', true);
                        $('#define-attribute #attributeMap-type').prop('disabled', false);
                        attributes = createAttributeObjectList(savedMapperAttributes, streamAttributes);
                        renderAttributeMappingContent(attributes);
                    }
                }

                //onchange of map type selection
                $('#define-map').on('change', '#map-type', function () {

                    mapperOptions = getSelectedTypeOptions(this.value, predefinedSinkMaps);
                    if (map && mapperType && (mapperType.toLowerCase() == this.value.toLowerCase()) && savedMapperOptions) {
                        //if the selected type is same as the saved map type
                        mapperOptionsWithValues = mapUserOptionValues(mapperOptions, savedMapperOptions);
                        customizedMapperOptions = getCustomizedOptions(mapperOptions, savedMapperOptions);
                    } else {
                        mapperOptionsWithValues = createOptionObjectWithValues(mapperOptions);
                        customizedMapperOptions = [];
                    }
                    renderOptions(mapperOptionsWithValues, customizedMapperOptions, "mapper");
                    if (!map || (map && !savedMapperAttributes)) {
                        renderAttributeMapping();
                        attributes = initialiseAttributeContent(streamAttributes)
                    } else if (map && savedMapperAttributes) {
                        renderAttributeMapping();
                        $('#define-attribute #attributeMap-checkBox').prop('checked', true);
                        attributes = createAttributeObjectList(savedMapperAttributes, streamAttributes);
                    }
                    renderAttributeMappingContent(attributes);
                });

                //onchange of attribute type selection
                $('#define-attribute').on('change', '#attributeMap-type', function () {
                    var attributes = [];
                    if (map && savedMapperAttributes) {
                        var attributeType = savedMapperAttributes.getType().toLowerCase();
                        var annotationType = savedMapperAttributes.getAnnotationType().toLowerCase();
                        var selAttributeType = "";
                        if ((annotationType === constAttribute && attributeType === constMap) ||
                            (annotationType === constAttribute && attributeType === constList)) {
                            selAttributeType = "attributeMap"
                        } else if ((annotationType === constPayload && attributeType === constMap)) {
                            selAttributeType = "payloadMap"
                        } else {
                            selAttributeType = "payloadList"
                        }
                    }
                    if (map && savedMapperAttributes && selAttributeType === this.value) {
                        attributes = createAttributeObjectList(savedMapperAttributes, streamAttributes);
                    } else {
                        attributes = initialiseAttributeContent(streamAttributes);
                    }
                    renderAttributeMappingContent(attributes)
                });

                //onclick of submit
                var submitButtonElement = $(formContainer).find('#btn-submit')[0];
                submitButtonElement.addEventListener('click', function () {

                    //clear the error classes
                    $('.error-message').text("")
                    $('.required-input-field').removeClass('required-input-field');
                    var isErrorOccurred = false;

                    var selectedSinkType = $('#define-sink #sink-type').val();
                    if (selectedSinkType === null) {
                        DesignViewUtils.prototype.errorAlert("Select a sink type to submit");
                        isErrorOccurred = true;
                        return;
                    } else {
                        var annotationOptions = [];
                        if (validateOptions(annotationOptions, sinkOptions, "sink-options")) {
                            isErrorOccurred = true;
                            return;
                        }
                        if (validateCustomizedOptions(annotationOptions, "sink-options")) {
                            isErrorOccurred = true;
                            return;
                        }

                        var selectedMapType = $('#define-map #map-type').val();
                        var mapperAnnotationOptions = [];

                        if (validateOptions(mapperAnnotationOptions, mapperOptions, "mapper-options")) {
                            isErrorOccurred = true;
                            return;
                        }
                        if (validateCustomizedOptions(mapperAnnotationOptions, "mapper-options")) {
                            isErrorOccurred = true;
                            return;
                        }

                        if ($('#define-attribute #attributeMap-checkBox').is(":checked")) {
                            //if attribute section is checked
                            var mapperAttributeValuesArray = {};
                            var attributeType;
                            var annotationType;
                            var selAttributeType = $('#define-attribute #attributeMap-type').val();
                            // to identify the selected attribute type and annotation type for attribute-mapper annotation
                            if (selAttributeType === "attributeMap" || selAttributeType === "payloadMap") {
                                attributeType = "MAP";
                                if (selAttributeType === "attributeMap") {
                                    annotationType = "ATTRIBUTES"
                                } else {
                                    annotationType = "PAYLOAD"
                                }
                                //validate attribute value if it is not filled
                                $('#mapper-attributes .attribute').each(function () {
                                    var key = $(this).find('.attr-key').val().trim();
                                    var value = $(this).find('.attr-value').val().trim();
                                    if (value == "") {
                                        $(this).find('.error-message').text('Attribute Value is ' +
                                            'required')
                                        $(this)[0].scrollIntoView();
                                        $(this).find('.attr-value').addClass('required-input-field');
                                        isErrorOccurred = true;
                                        return false;
                                    } else {
                                        mapperAttributeValuesArray[key] = value;
                                    }
                                });
                            } else {
                                var mapperAttributeValuesArray = [];
                                attributeType = "LIST"
                                annotationType = "PAYLOAD"
                                var value = $('#mapper-attributes .attribute .attr-value:first').val().trim();
                                //validate the single payload attribute value if it is empty
                                if (value == "") {
                                    $('#mapper-attributes .attribute .error-message:first').text('Payload Value is ' +
                                        'required')
                                    $('#mapper-attributes .attribute .attr-value:first')[0].scrollIntoView();
                                    $('#mapper-attributes .attribute .attr-value:first').addClass('required-input-field');
                                    isErrorOccurred = true;
                                } else {
                                    mapperAttributeValuesArray.push(value);
                                }
                            }
                        }
                    }

                    if (!isErrorOccurred) {
                        clickedElement.setType(selectedSinkType);
                        if (annotationOptions.length == 0) {
                            clickedElement.setOptions(undefined);
                        } else {
                            clickedElement.setOptions(annotationOptions);
                        }

                        var mapper = {};
                        _.set(mapper, 'type', selectedMapType);
                        if (mapperAnnotationOptions.length == 0) {
                            _.set(mapper, 'options', undefined);
                        } else {
                            _.set(mapper, 'options', mapperAnnotationOptions);
                        }

                        if ($('#define-attribute #attributeMap-checkBox').is(":checked")) {
                            payloadOrAttributeOptions = {};
                            _.set(payloadOrAttributeOptions, 'annotationType', annotationType);
                            _.set(payloadOrAttributeOptions, 'type', attributeType);
                            _.set(payloadOrAttributeOptions, 'value', mapperAttributeValuesArray);
                            var payloadOrAttributeObject = new PayloadOrAttribute(payloadOrAttributeOptions);
                            _.set(mapper, 'payloadOrAttribute', payloadOrAttributeObject);
                        } else {
                            _.set(mapper, 'payloadOrAttribute', undefined);
                        }
                        var mapperObject = new MapAnnotation(mapper);
                        clickedElement.setMap(mapperObject);

                        var textNode = $('#' + id).find('.sinkNameNode');
                        textNode.html(selectedSinkType);

                        $('#' + id).removeClass('incomplete-element');
                        $('#' + id).prop('title', '');

                        // set the isDesignViewContentChanged to true
                        self.configurationData.setIsDesignViewContentChanged(true);

                        self.designViewContainer.removeClass('disableContainer');
                        self.toggleViewButton.removeClass('disableContainer');

                        // close the form window
                        self.consoleListManager.removeFormConsole(formConsole);
                    }
                });

                // 'Cancel' button action
                var cancelButtonElement = $(formContainer).find('#btn-cancel')[0];
                cancelButtonElement.addEventListener('click', function () {
                    self.designViewContainer.removeClass('disableContainer');
                    self.toggleViewButton.removeClass('disableContainer');
                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                });
            }
        };
        return SinkForm;
    });

