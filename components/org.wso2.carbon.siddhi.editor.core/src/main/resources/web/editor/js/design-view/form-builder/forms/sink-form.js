/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['log', 'jquery', 'lodash', 'mapAnnotation', 'payloadOrAttribute', 'jsonValidator',
        'handlebar', 'designViewUtils', 'constants'],
    function (log, $, _, MapAnnotation, PayloadOrAttribute, JSONValidator, Handlebars, DesignViewUtils,
              Constants) {

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
                this.dropElementInstance = options.dropElementInstance;
                this.jsPlumbInstance = options.jsPlumbInstance;
                var currentTabId = this.application.tabController.activeTab.cid;
                this.designViewContainer = $('#design-container-' + currentTabId);
                this.toggleViewButton = $('#toggle-view-button-' + currentTabId);
            }
        };

        /**
         * @function to render the html to display the select options for attribute mapping
         */
        var renderAttributeMapping = function () {
            if (!$.trim($('#define-attribute').html()).length) {
                var attributeDiv = $('<div class="clearfix"> <label id="attribute-map-label">' +
                    '<input type="checkbox" id="attributeMap-checkBox"> Payload or Attribute Mapping' +
                    '</label> </div> <div class = "clearfix"> <select id = "attributeMap-type" disabled>' +
                    '<option value = "payloadMap"> Enter payload as key/value pairs </option>' +
                    '<option value = "payloadList"> Enter a single payload attribute </option>' +
                    '</select></div>');
                $('#define-attribute').html(attributeDiv);
            }
        };

        /**
         * @function to create attribute-map object with the saved attribute-map
         * @param {Object} savedMapperAttributes Saved attribute-map
         * @return {Object} attributes
         */
        var createAttributeObjectList = function (savedMapperAttributes) {
            var attributeType = savedMapperAttributes.getType().toLowerCase();
            var attributeValues = savedMapperAttributes.getValue();
            var attributes = [];
            if (attributeType === Constants.LIST) {
                $('#define-attribute #attributeMap-type').val('payloadList');
                attributes.push({ value: attributeValues[0] });
            } else {
                $('#define-attribute #attributeMap-type').val('payloadMap');
                for (var attribute in attributeValues) {
                    attributes.push({ key: attribute, value: attributeValues[attribute] });
                }
            }
            return attributes;
        };

        /**
         * @function to create attribute-map objects with empty values
         * @return {Object} attributes
         */
        var initialiseAttributeContent = function () {
            var selectedAttributeType = $('#define-attribute #attributeMap-type').val();
            var attributes = [];
            if (selectedAttributeType === "payloadList") {
                attributes.push({ value: "" });
            } else {
                attributes.push({ key: " ", value: "" });
            }
            return attributes;
        };

        /**
         * @function to render the attribute-map div using handlebars
         * @param {Object} attributes which needs to be mapped on to the template
         * @param {Object} streamAttributes to display the stream attributes
         */
        var renderAttributeMappingContent = function (attributes, streamAttributes) {
            var attributeMapFormTemplate = Handlebars.compile($('#source-sink-map-attribute-template').html())
            ({ id: Constants.SINK, attributes: attributes });
            $('#attribute-map-content').html(attributeMapFormTemplate);

            var streamAttributeMessage = "Stream Attributes are: ";
            _.forEach(streamAttributes, function (attribute) {
                streamAttributeMessage += attribute.key + ", ";
            });
            streamAttributeMessage = streamAttributeMessage.substring(0, streamAttributeMessage.length - 2);
            $('#stream-attributes').html(streamAttributeMessage);
            if (!attributes[0].key) {
                $('#btn-add-payload-map').hide()
            } else {
                $('#btn-add-payload-map').show()
            }
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
            var sinkObject = self.configurationData.getSiddhiAppConfig().getSink(id);

            var isSinkConnected = true;
            if ($('#' + id).hasClass('error-element')) {
                isSinkConnected = false;
                DesignViewUtils.prototype.errorAlert("Please connect to a stream");
            } else if (!JSONValidator.prototype.validateSourceOrSinkAnnotation(sinkObject, Constants.SINK, true)) {
                // perform JSON validation to check if sink contains a connectedElement.
                isSinkConnected = false;
            }
            if (!isSinkConnected) {
                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
                self.consoleListManager.removeAllConsoles();
            } else {
                var connectedElement = sinkObject.connectedElementName;
                var predefinedSinks = _.orderBy(this.configurationData.rawExtensions["sink"], ['name'], ['asc']);
                var predefinedSinkMaps = _.orderBy(this.configurationData.rawExtensions["sinkMaps"], ['name'], ['asc']);
                var connectedStream = self.configurationData.getSiddhiAppConfig().getDefinitionElementByName
                (connectedElement);
                var streamAttributes = self.formUtils.createStreamAttributesObject
                (connectedStream.element.getAttributeList());

                var propertyDiv = $('<div class="clearfix form-min-width"><div class="source-sink-form-container sink-div">' +
                    '<div id="define-sink"></div>' +
                    '<div class = "source-sink-map-options" id="sink-options-div"></div></div>' +
                    '<div class="source-sink-form-container mapper-div"> <div id="define-map"> </div>' +
                    '<div class="source-sink-map-options" id="mapper-options-div"></div>' +
                    '</div> <div class= "source-sink-form-container attribute-map-div"><div id="define-attribute">' +
                    '</div> <div id="attribute-map-content"></div> </div> </div>');

                formContainer.html(propertyDiv);
                self.formUtils.buildFormButtons(formConsole.cid);
                self.formUtils.popUpSelectedElement(id);
                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');

                self.formUtils.addEventListenerToRemoveRequiredClass();
                self.formUtils.addEventListenerToShowAndHideInfo();
                self.formUtils.addEventListenerToShowInputContentOnHover();

                //declaration of variables
                var currentSinkOptions = [];
                var sinkOptionsWithValues = [];
                var customizedSinkOptions = [];
                var currentMapperOptions = [];
                var mapperOptionsWithValues = [];
                var customizedMapperOptions = [];
                var attributes = [];

                self.formUtils.addEventListenersForGenericOptionsDiv(Constants.SINK);
                self.formUtils.addEventListenersForGenericOptionsDiv(Constants.MAPPER);

                self.formUtils.renderSourceSinkStoreTypeDropDown(Constants.SINK, predefinedSinks);

                //event listener for attribute-map checkbox
                $('#define-attribute').on('change', '#attributeMap-checkBox', function () {
                    if ($(this).is(':checked')) {
                        var attributes = [];
                        if (map && map.getPayloadOrAttribute()) {
                            attributes = createAttributeObjectList(mapperAttributes);
                        } else {
                            attributes = initialiseAttributeContent();
                        }
                        $('#attribute-map-content').show();
                        renderAttributeMappingContent(attributes, streamAttributes)
                        $('#define-attribute #attributeMap-type').prop('disabled', false);
                    } else {
                        $('#attribute-map-content').hide();
                        $("#define-attribute #attributeMap-type").prop('disabled', 'disabled');
                    }
                    self.formUtils.updatePerfectScroller();
                });

                //onchange of the sink-type selection
                $('#sink-type').change(function () {
                    currentSinkOptions = self.formUtils.getSelectedTypeParameters(this.value, predefinedSinks);
                    if (type && (type.toLowerCase() == this.value.toLowerCase()) && sinkOptions) {
                        //if the selected type is same as the saved sink-type
                        sinkOptionsWithValues = self.formUtils.mapUserOptionValues(currentSinkOptions, sinkOptions);
                        customizedSinkOptions = self.formUtils.getCustomizedOptions(currentSinkOptions, sinkOptions);
                    } else {
                        sinkOptionsWithValues = self.formUtils.createObjectWithValues(currentSinkOptions);
                        customizedSinkOptions = [];
                    }
                    self.formUtils.renderOptions(sinkOptionsWithValues, customizedSinkOptions, Constants.SINK);
                    if (!map && !$.trim($('#mapper-options-div').html()).length) {
                        self.formUtils.buildMapSection(predefinedSinkMaps);
                        renderAttributeMapping()
                    }
                });

                //onchange of map type selection
                $('#define-map').on('change', '#map-type', function () {
                    currentMapperOptions = self.formUtils.getSelectedTypeParameters(this.value, predefinedSinkMaps);
                    if (map && mapperType && (mapperType.toLowerCase() == this.value.toLowerCase()) && mapperOptions) {
                        //if the selected type is same as the saved map type
                        mapperOptionsWithValues = self.formUtils.mapUserOptionValues(currentMapperOptions, mapperOptions);
                        customizedMapperOptions = self.formUtils.getCustomizedOptions(currentMapperOptions, mapperOptions);
                    } else {
                        mapperOptionsWithValues = self.formUtils.createObjectWithValues(currentMapperOptions);
                        customizedMapperOptions = [];
                    }
                    self.formUtils.renderOptions(mapperOptionsWithValues, customizedMapperOptions, Constants.MAPPER);
                });

                //onchange of attribute type selection
                $('#define-attribute').on('change', '#attributeMap-type', function () {
                    var attributes = [];
                    if (map && mapperAttributes) {
                        var attributeType = mapperAttributes.getType().toLowerCase();
                        var selAttributeType = "";
                        if (attributeType === Constants.LIST) {
                            selAttributeType = "payloadList"
                        } else {
                            selAttributeType = "payloadMap"
                        }
                    }
                    if (map && mapperAttributes && selAttributeType === this.value) {
                        attributes = createAttributeObjectList(mapperAttributes);
                    } else {
                        attributes = initialiseAttributeContent(streamAttributes);
                    }
                    renderAttributeMappingContent(attributes, streamAttributes)
                });

                //onclick of the payload key value add button
                $('#attribute-map-content').on('click', '#btn-add-payload-map', function () {
                    var payloadMapDiv = '<div class= "attribute"> <div class= "clearfix">' +
                        '<input type = "text" value = "" class = "attr-key"/> ' +
                        ' <input type = "text" value = "" class = "attr-value"/>' +
                        '</div> <label class = "error-message"></label> </div>';
                    $('#attribute-map-content').find('#attributes').append(payloadMapDiv);
                    self.formUtils.updatePerfectScroller();
                });

                //get the clicked element's information
                var type = sinkObject.getType();
                var sinkOptions = sinkObject.getOptions();
                var map = sinkObject.getMap();

                if (type) {
                    //if sink object is already edited
                    $('#define-sink').find('#sink-type option').filter(function () {
                        return ($(this).val().toLowerCase() == (type.toLowerCase()));
                    }).prop('selected', true);
                    currentSinkOptions = self.formUtils.getSelectedTypeParameters(type, predefinedSinks);
                    if (sinkOptions) {
                        //get the saved sink options values and map it
                        sinkOptionsWithValues = self.formUtils.mapUserOptionValues(currentSinkOptions, sinkOptions);
                        customizedSinkOptions = self.formUtils.getCustomizedOptions(currentSinkOptions, sinkOptions);
                    } else {
                        //create option object with empty values
                        sinkOptionsWithValues = self.formUtils.createObjectWithValues(currentSinkOptions);
                        customizedSinkOptions = [];
                    }
                    self.formUtils.renderOptions(sinkOptionsWithValues, customizedSinkOptions, Constants.SINK);
                    if (!map) {
                        self.formUtils.buildMapSection(predefinedSinkMaps);
                        renderAttributeMapping()
                    }
                }

                if (map) {
                    //if map is filled
                    self.formUtils.renderMap(predefinedSinkMaps);
                    renderAttributeMapping();
                    var mapperType = map.getType();
                    var mapperOptions = map.getOptions();
                    var mapperAttributes = map.getPayloadOrAttribute();
                    if (mapperType) {
                        $('#define-map').find('#map-type option').filter(function () {
                            return ($(this).val().toLowerCase() == (mapperType.toLowerCase()));
                        }).prop('selected', true);
                        currentMapperOptions = self.formUtils.getSelectedTypeParameters(mapperType, predefinedSinkMaps);
                        if (mapperOptions) {
                            //get the saved Map options values and map it
                            mapperOptionsWithValues = self.formUtils.mapUserOptionValues(currentMapperOptions, mapperOptions);
                            customizedMapperOptions = self.formUtils.getCustomizedOptions(currentMapperOptions, mapperOptions);
                        } else {
                            //create option object with empty values
                            mapperOptionsWithValues = self.formUtils.createObjectWithValues(currentMapperOptions);
                            customizedMapperOptions = [];
                        }
                        self.formUtils.renderOptions(mapperOptionsWithValues, customizedMapperOptions, Constants.MAPPER);
                    }
                    if (mapperAttributes) {
                        $('#define-attribute #attributeMap-checkBox').prop('checked', true);
                        $('#define-attribute #attributeMap-type').prop('disabled', false);
                        attributes = createAttributeObjectList(mapperAttributes);
                        renderAttributeMappingContent(attributes, streamAttributes);
                    }
                }

                self.formUtils.initPerfectScroller(formConsole.cid);

                //onclick of submit
                $('#' + formConsole.cid).on('click', '#btn-submit', function () {

                    self.formUtils.removeErrorClass();
                    var isErrorOccurred = false;

                    var selectedSinkType = $('#define-sink #sink-type').val();
                    if (selectedSinkType === null) {
                        DesignViewUtils.prototype.errorAlert("Select a sink type to submit");
                        isErrorOccurred = true;
                        return;
                    } else {
                        if (self.formUtils.validateOptions(currentSinkOptions, Constants.SINK)) {
                            isErrorOccurred = true;
                            return;
                        }
                        if (self.formUtils.validateCustomizedOptions(Constants.SINK)) {
                            isErrorOccurred = true;
                            return;
                        }

                        var selectedMapType = $('#define-map #map-type').val();

                        if (self.formUtils.validateOptions(currentMapperOptions, Constants.MAPPER)) {
                            isErrorOccurred = true;
                            return;
                        }
                        if (self.formUtils.validateCustomizedOptions(Constants.MAPPER)) {
                            isErrorOccurred = true;
                            return;
                        }

                        if ($('#define-attribute #attributeMap-checkBox').is(":checked")) {
                            //if attribute section is checked
                            var attributeType;
                            var selAttributeType = $('#define-attribute #attributeMap-type').val();
                            // to identify the selected attribute type and annotation type for attribute-mapper annotation
                            if (selAttributeType === "payloadMap") {
                                var isEmptyList = false
                                var mapperAttributeValuesArray = {};
                                attributeType = Constants.MAP;
                                //validate attribute value if it is not filled
                                $('#mapper-attributes .attribute').each(function () {
                                    var key = $(this).find('.attr-key').val().trim();
                                    var value = $(this).find('.attr-value').val().trim();
                                    if (key != "" || value != "") {
                                        if (key == "") {
                                            $(this).find('.error-message').text('Payload key is required.')
                                            self.formUtils.addErrorClass($(this).find('.attr-key'));
                                            isErrorOccurred = true;
                                            return false;
                                        } else if (value == "") {
                                            $(this).find('.error-message').text('Payload value is required.')
                                            self.formUtils.addErrorClass($(this).find('.attr-value'));
                                            isErrorOccurred = true;
                                            return false;
                                        } else {
                                            mapperAttributeValuesArray[key] = value;
                                        }
                                    }
                                });
                                if (_.isEmpty(mapperAttributeValuesArray)) {
                                    isEmptyList = true;
                                }
                            } else {
                                var mapperAttributeValuesArray = [];
                                attributeType = Constants.LIST
                                var value = $('#mapper-attributes .attribute .attr-value:first').val().trim();
                                //validate the single payload attribute value if it is empty
                                if (value == "") {
                                    self.formUtils.addErrorClass($('#mapper-attributes .attribute .attr-value:first'));
                                    $('#mapper-attributes .attribute .error-message:first').text('Payload Value is ' +
                                        'required')
                                    isErrorOccurred = true;
                                } else {
                                    mapperAttributeValuesArray.push(value);
                                }
                            }
                        }
                    }

                    if (!isErrorOccurred) {
                        sinkObject.setType(selectedSinkType);
                        var annotationOptions = [];
                        self.formUtils.buildOptions(annotationOptions, Constants.SINK);
                        self.formUtils.buildCustomizedOption(annotationOptions, Constants.SINK);
                        if (annotationOptions.length == 0) {
                            sinkObject.setOptions(undefined);
                        } else {
                            sinkObject.setOptions(annotationOptions);
                        }

                        var mapper = {};
                        var mapperAnnotationOptions = [];
                        self.formUtils.buildOptions(mapperAnnotationOptions, Constants.MAPPER);
                        self.formUtils.buildCustomizedOption(mapperAnnotationOptions, Constants.MAPPER);
                        _.set(mapper, 'type', selectedMapType);
                        if (mapperAnnotationOptions.length == 0) {
                            _.set(mapper, 'options', undefined);
                        } else {
                            _.set(mapper, 'options', mapperAnnotationOptions);
                        }

                        if ($('#define-attribute #attributeMap-checkBox').is(":checked") && !isEmptyList) {
                            payloadOrAttributeOptions = {};
                            _.set(payloadOrAttributeOptions, 'annotationType', 'PAYLOAD');
                            _.set(payloadOrAttributeOptions, 'type', attributeType);
                            _.set(payloadOrAttributeOptions, 'value', mapperAttributeValuesArray);
                            var payloadOrAttributeObject = new PayloadOrAttribute(payloadOrAttributeOptions);
                            _.set(mapper, 'payloadOrAttribute', payloadOrAttributeObject);
                        } else {
                            _.set(mapper, 'payloadOrAttribute', undefined);
                        }
                        var mapperObject = new MapAnnotation(mapper);
                        sinkObject.setMap(mapperObject);

                        var textNode = $('#' + id).find('.sinkNameNode');
                        textNode.html(selectedSinkType);

                        $('#' + id).removeClass('incomplete-element');
                        //Send sink element to the backend and generate tooltip
                        var sinkToolTip = self.formUtils.getTooltip(sinkObject, Constants.SINK);
                        $('#' + id).prop('title', sinkToolTip);

                        self.dropElementInstance.generateSpecificSinkConnectionElements(selectedSinkType,
                            self.jsPlumbInstance, id, $('#' + id));

                        // set the isDesignViewContentChanged to true
                        self.configurationData.setIsDesignViewContentChanged(true);
                        // close the form window
                        self.consoleListManager.removeFormConsole(formConsole);
                        if (self.application.browserStorage.get("isWidgetFromTourGuide")) {
                            self.consoleListManager.removeAllConsoles();
                        }
                    }
                });

                $('#' + formConsole.cid).on('click', '#btn-cancel', function () {
                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                });
            }
        };
        return SinkForm;
    });

