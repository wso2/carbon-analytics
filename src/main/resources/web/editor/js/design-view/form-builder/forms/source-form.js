/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['log', 'jquery', 'lodash', 'sourceOrSinkAnnotation', 'mapAnnotation', 'payloadOrAttribute',
        'jsonValidator', 'handlebar', 'designViewUtils', 'constants'],
    function (log, $, _, SourceOrSinkAnnotation, MapAnnotation, PayloadOrAttribute, JSONValidator, Handlebars,
              DesignViewUtils, Constants) {

        /**
         * @class SourceForm Creates a forms to collect data from a source
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var SourceForm = function (options) {
            if (options !== undefined) {
                this.configurationData = options.configurationData;
                this.application = options.application;
                this.formUtils = options.formUtils;
                this.dropElementInstance = options.dropElementInstance;
                this.jsPlumbInstance = options.jsPlumbInstance;
                this.consoleListManager = options.application.outputController;
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
                    '<input type="checkbox" id="attributeMap-checkBox"> Map Attribute As Key/Value Pairs ' +
                    '</label> </div>');
                $('#define-attribute').html(attributeDiv);
            }
        };

        /**
         * @function to render the attribute-map div using handlebars
         * @param {Object} attributes which needs to be mapped on to the template
         */
        var renderAttributeMappingContent = function (id, attributes) {
            var attributeMapFormTemplate = Handlebars.compile($('#source-sink-map-attribute-template').html())
            ({ id: id, attributes: attributes });
            $('#attribute-map-content').html(attributeMapFormTemplate);
        };

        /**
         * @function to create attribute-map object
         * @param {Object} savedMapperAttributes Saved attribute-map
         * @param {Objects} streamAttributes Attributes of the connected stream
         * @return {Object} attributes
         */
        var createAttributeObjectList = function (savedMapperAttributes, streamAttributes) {
            var attributeType;
            var attributes = [];
            if (!savedMapperAttributes) {
                attributeType = "none";
            } else {
                attributeType = savedMapperAttributes.getType().toLowerCase();
                var attributeValues = savedMapperAttributes.getValue();
            }
            if (attributeType === Constants.LIST) {
                _.forEach(streamAttributes, function (streamAttribute) {
                    attributes.push({ key: streamAttribute.key, value: "" });
                })
                var i = 0;
                _.forEach(attributeValues, function (attribute) {
                    if (i < streamAttributes.length) {
                        attributes[i].value = attributeValues[attribute];
                        i++;
                    }
                });
            } else if (attributeType === Constants.MAP) {
                _.forEach(streamAttributes, function (streamAttribute) {
                    attributes.push({ key: streamAttribute.key, value: "" });
                })
                _.forEach(attributes, function (mappedAttribute) {
                    for (var attribute in attributeValues) {
                        if (mappedAttribute.key === attribute) {
                            mappedAttribute.value = attributeValues[attribute]
                            break;
                        }
                    }
                })
            } else {
                _.forEach(streamAttributes, function (streamAttribute) {
                    attributes.push({ key: streamAttribute.key, value: "" });
                });
            }
            return attributes;
        };

        /**
         * @function generate properties form for a source
         * @param element selected element(source)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        SourceForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');
            var sourceObject = self.configurationData.getSiddhiAppConfig().getSource(id);
            var isSourceConnected = true;

            if ($('#' + id).hasClass('error-element')) {
                isSourceConnected = false;
                DesignViewUtils.prototype.errorAlert("Please connect to a stream");
            } else if (!JSONValidator.prototype.validateSourceOrSinkAnnotation(sourceObject, Constants.SOURCE, true)) {
                // perform JSON validation to check if source contains a connectedElement.
                isSourceConnected = false;
            }
            if (!isSourceConnected) {
                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
                self.consoleListManager.removeAllConsoles();
            } else {
                var connectedElement = sourceObject.connectedElementName;
                var predefinedSources = _.orderBy(this.configurationData.rawExtensions["source"], ['name'], ['asc']);
                var predefinedSourceMaps = _.orderBy(this.configurationData.rawExtensions["sourceMaps"], ['name'], ['asc']);
                var connectedStream = self.configurationData.getSiddhiAppConfig().getDefinitionElementByName
                (connectedElement);
                var streamAttributes = self.formUtils.createStreamAttributesObject
                (connectedStream.element.getAttributeList());

                var propertyDiv = $('<div class="clearfix form-min-width"><div class="source-sink-form-container source-div">' +
                    '<div id="define-source"></div> <div class = "source-sink-map-options" id="source-options-div"></div>' +
                    '</div> <div class="source-sink-form-container mapper-div"> <div id="define-map"> </div> ' +
                    '<div class="source-sink-map-options" id="mapper-options-div">' +
                    '</div> </div> <div class= "source-sink-form-container attribute-map-div">' +
                    '<div id="define-attribute"> </div> <div id="attribute-map-content"></div> </div> </div>');

                formContainer.html(propertyDiv);
                self.formUtils.buildFormButtons(formConsole.cid);
                self.formUtils.popUpSelectedElement(id);
                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');

                self.formUtils.addEventListenerToRemoveRequiredClass();
                self.formUtils.addEventListenerToShowAndHideInfo();
                self.formUtils.addEventListenerToShowInputContentOnHover();

                //declaration of variables
                var currentSourceOptions = [];
                var sourceOptionsWithValues = [];
                var customizedSourceOptions = [];
                var currentMapperOptions = [];
                var mapperOptionsWithValues = [];
                var customizedMapperOptions = [];
                var attributes = [];

                self.formUtils.addEventListenersForGenericOptionsDiv(Constants.SOURCE);
                self.formUtils.addEventListenersForGenericOptionsDiv(Constants.MAPPER);

                self.formUtils.renderSourceSinkStoreTypeDropDown(Constants.SOURCE, predefinedSources);

                //event listener for attribute-map checkbox
                $('#define-attribute').on('change', '#attributeMap-checkBox', function () {
                    if ($(this).is(':checked')) {
                        var attributes = createAttributeObjectList(mapperAttributes, streamAttributes);
                        $('#attribute-map-content').show();
                        renderAttributeMappingContent(Constants.SOURCE, attributes)
                    } else {
                        $('#attribute-map-content').hide();
                    }
                    self.formUtils.updatePerfectScroller();
                });

                //get the clicked element's information
                var type = sourceObject.getType();
                var sourceOptions = sourceObject.getOptions();
                var map = sourceObject.getMap();

                //onchange of the source-type selection
                $('#source-type').change(function () {
                    currentSourceOptions = self.formUtils.getSelectedTypeParameters(this.value, predefinedSources);
                    if (type && (type.toLowerCase() == this.value.toLowerCase()) && sourceOptions) {
                        //if the selected type is same as the saved source-type
                        sourceOptionsWithValues = self.formUtils.mapUserOptionValues(currentSourceOptions, sourceOptions);
                        customizedSourceOptions = self.formUtils.getCustomizedOptions(currentSourceOptions, sourceOptions);
                    } else {
                        sourceOptionsWithValues = self.formUtils.createObjectWithValues(currentSourceOptions);
                        customizedSourceOptions = [];
                    }
                    self.formUtils.renderOptions(sourceOptionsWithValues, customizedSourceOptions, Constants.SOURCE);
                    if (!map && !$.trim($('#mapper-options-div').html()).length) {
                        self.formUtils.buildMapSection(predefinedSourceMaps);
                        renderAttributeMapping()
                    }
                });

                //onchange of map type selection
                $('#define-map').on('change', '#map-type', function () {
                    currentMapperOptions = self.formUtils.getSelectedTypeParameters(this.value, predefinedSourceMaps);
                    if ((map) && (mapperType) && (mapperType.toLowerCase() == this
                        .value.toLowerCase()) && mapperOptions) {
                        //if the selected type is same as the saved map type
                        mapperOptionsWithValues = self.formUtils.mapUserOptionValues(currentMapperOptions, mapperOptions);
                        customizedMapperOptions = self.formUtils.getCustomizedOptions(currentMapperOptions, mapperOptions);
                    } else {
                        mapperOptionsWithValues = self.formUtils.createObjectWithValues(currentMapperOptions);
                        customizedMapperOptions = [];
                    }
                    self.formUtils.renderOptions(mapperOptionsWithValues, customizedMapperOptions, Constants.MAPPER)
                });

                if (type) {
                    //if source object is already edited
                    $('#define-source').find('#source-type option').filter(function () {
                        return ($(this).val().toLowerCase() == (type.toLowerCase()));
                    }).prop('selected', true);
                    currentSourceOptions = self.formUtils.getSelectedTypeParameters(type, predefinedSources);
                    if (sourceOptions) {
                        //get the saved Source options values and map it
                        sourceOptionsWithValues = self.formUtils.mapUserOptionValues(currentSourceOptions, sourceOptions);
                        customizedSourceOptions = self.formUtils.getCustomizedOptions(currentSourceOptions, sourceOptions);
                    } else {
                        //create option object with empty values
                        sourceOptionsWithValues = self.formUtils.createObjectWithValues(currentSourceOptions);
                        customizedSourceOptions = [];
                    }
                    self.formUtils.renderOptions(sourceOptionsWithValues, customizedSourceOptions, Constants.SOURCE);
                    if (!map) {
                        self.formUtils.buildMapSection(predefinedSourceMaps);
                        renderAttributeMapping()
                    }
                }

                if (map) {
                    //if map section is filled
                    self.formUtils.renderMap(predefinedSourceMaps);
                    renderAttributeMapping();
                    var mapperType = map.getType();
                    var mapperOptions = map.getOptions();
                    var mapperAttributes = map.getPayloadOrAttribute();
                    if (mapperType) {
                        $('#define-map').find('#map-type option').filter(function () {
                            return ($(this).val().toLowerCase() == (mapperType.toLowerCase()));
                        }).prop('selected', true);
                        currentMapperOptions = self.formUtils.getSelectedTypeParameters(mapperType, predefinedSourceMaps);
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
                        attributes = createAttributeObjectList(mapperAttributes, streamAttributes);
                        renderAttributeMappingContent(Constants.SOURCE, attributes);
                    }
                }

                self.formUtils.initPerfectScroller(formConsole.cid);

                //onclick of submit
                $('#' + formConsole.cid).on('click', '#btn-submit', function () {

                    self.formUtils.removeErrorClass();
                    var isErrorOccurred = false;

                    var selectedSourceType = $('#define-source #source-type').val();
                    if (selectedSourceType === null) {
                        DesignViewUtils.prototype.errorAlert("Select a source type to submit.");
                        isErrorOccurred = true;
                        return;
                    } else {
                        if (self.formUtils.validateOptions(currentSourceOptions, Constants.SOURCE)) {
                            isErrorOccurred = true;
                            return;
                        }
                        if (self.formUtils.validateCustomizedOptions(Constants.SOURCE)) {
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
                            var mapperAttributeValuesArray = {};
                            $('#mapper-attributes .attribute').each(function () {
                                //validate mapper  attributes if value is not filled
                                var key = $(this).find('.attr-key').val().trim();
                                var value = $(this).find('.attr-value').val().trim();
                                if (value == "") {
                                    $(this).find('.error-message').text('Attribute Value is required.');
                                    self.formUtils.addErrorClass($(this).find('.attr-value'));
                                    isErrorOccurred = true;
                                    return false;
                                } else {
                                    mapperAttributeValuesArray[key] = value;
                                }
                            });
                        }
                    }

                    if (!isErrorOccurred) {
                        sourceObject.setType(selectedSourceType);
                        var textNode = $('#' + id).find('.sourceNameNode');
                        textNode.html(selectedSourceType);

                        var annotationOptions = [];
                        self.formUtils.buildOptions(annotationOptions, Constants.SOURCE);
                        self.formUtils.buildCustomizedOption(annotationOptions, Constants.SOURCE);
                        if (annotationOptions.length == 0) {
                            sourceObject.setOptions(undefined);
                        } else {
                            sourceObject.setOptions(annotationOptions);
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

                        if ($('#define-attribute #attributeMap-checkBox').is(":checked")) {
                            payloadOrAttributeOptions = {};
                            _.set(payloadOrAttributeOptions, 'annotationType', 'ATTRIBUTES');
                            _.set(payloadOrAttributeOptions, 'type', Constants.MAP);
                            _.set(payloadOrAttributeOptions, 'value', mapperAttributeValuesArray);
                            var payloadOrAttributeObject = new PayloadOrAttribute(payloadOrAttributeOptions);
                            _.set(mapper, 'payloadOrAttribute', payloadOrAttributeObject);
                        } else {
                            _.set(mapper, 'payloadOrAttribute', undefined);
                        }
                        var mapperObject = new MapAnnotation(mapper);
                        sourceObject.setMap(mapperObject);

                        $('#' + id).removeClass('incomplete-element');
                        //Send source element to the backend and generate tooltip
                        var sourceToolTip = self.formUtils.getTooltip(sourceObject, Constants.SOURCE);
                        $('#' + id).prop('title', sourceToolTip);

                        self.dropElementInstance.generateSpecificSourceConnectionElements(selectedSourceType,
                            self.jsPlumbInstance, id, $('#' + id));

                        // set the isDesignViewContentChanged to true
                        self.configurationData.setIsDesignViewContentChanged(true);
                        // close the form window
                        self.consoleListManager.removeFormConsole(formConsole);
                    }
                });

                // 'Cancel' button action
                $('#' + formConsole.cid).on('click', '#btn-cancel', function () {
                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                });
            }
        };
        return SourceForm;
    });
