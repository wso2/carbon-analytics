/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'log', 'jquery', 'lodash', 'attribute', 'jsonValidator', 'constants'],
    function (require, log, $, _, Attribute, JSONValidator, Constants) {

        /**
         * @class StreamForm Creates a forms to collect data from a stream
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var StreamForm = function (options) {
            if (options !== undefined) {
                this.configurationData = options.configurationData;
                this.application = options.application;
                this.formUtils = options.formUtils;
                this.dropElementInstance = options.dropElementInstance;
                this.designGrid = options.designGrid;
                this.jsPlumbInstance = options.jsPlumbInstance;
                this.consoleListManager = options.application.outputController;
                var currentTabId = this.application.tabController.activeTab.cid;
                this.designViewContainer = $('#design-container-' + currentTabId);
                this.toggleViewButton = $('#toggle-view-button-' + currentTabId);
            }
        };

        /**
         * @function generate properties form for a stream
         * @param element selected element(stream)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        StreamForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');
            var streamObject = self.configurationData.getSiddhiAppConfig().getStream(id);
            var previousStreamObject = _.cloneDeep(streamObject);
            var propertyDiv = $('<div class="clearfix form-min-width"><div class = "stream-form-container"> <label> ' +
                '<span class="mandatory-symbol"> *</span> Name </label> <input type="text" id="streamName" ' +
                'class="clearfix name"> <label class="error-message" id="streamNameErrorMessage"> </label>' +
                '<div id="define-attribute"></div> </div> <div class= "stream-form-container"> ' +
                '<div class ="define-annotation"> </div> </div>');

            formContainer.html(propertyDiv);
            self.formUtils.buildFormButtons(formConsole.cid);
            self.formUtils.popUpSelectedElement(id);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            self.formUtils.addEventListenerToRemoveRequiredClass();
            self.formUtils.addEventListenerToShowInputContentOnHover();

            var annotations = [];
            var predefinedAnnotationList = _.cloneDeep(self.configurationData.application.config.stream_predefined_annotations);
            var checkedAnnotations = [];

            var name = streamObject.getName();
            if (!name) {
                //if stream form is freshly opened [new object]
                annotations = predefinedAnnotationList;
                var attributes = [{name: ""}];
                self.formUtils.renderAttributeTemplate(attributes)
            } else {
                //if the stream object is already edited
                $('#streamName').val(name);

                //load the saved attributes
                var attributeList = streamObject.getAttributeList();
                self.formUtils.renderAttributeTemplate(attributeList)
                self.formUtils.selectTypesOfSavedAttributes(attributeList);

                //load the saved annotations
                var annotationListObjects = streamObject.getAnnotationListObjects();
                _.forEach(predefinedAnnotationList, function (predefinedAnnotation) {
                    var foundPredefined = false;
                    _.forEach(annotationListObjects, function (savedAnnotation) {
                        if (savedAnnotation.name.toLowerCase() === predefinedAnnotation.name.toLowerCase()) {
                            //if an optional annotation is found push it to the checkedAnnotations[]
                            if (!predefinedAnnotation.isMandatory) {
                                checkedAnnotations.push(savedAnnotation.name);
                            }
                            foundPredefined = true;
                            _.forEach(predefinedAnnotation.elements, function (predefinedAnnotationElement) {
                                _.forEach(savedAnnotation.elements, function (savedAnnotationElement) {
                                    if (predefinedAnnotationElement.name.toLowerCase() === savedAnnotationElement.key
                                        .toLowerCase()) {
                                        //if an optional property is found push it to the checkedAnnotations[]
                                        if (!predefinedAnnotationElement.isMandatory) {
                                            checkedAnnotations.push(savedAnnotationElement.name);
                                        }
                                        predefinedAnnotationElement.defaultValue = savedAnnotationElement.value;
                                    }
                                })
                            })
                            annotations.push(predefinedAnnotation)
                        } else {
                            annotations.push(savedAnnotation)
                        }
                    });
                    if (!foundPredefined) {
                        annotations.push(predefinedAnnotation)
                    }
                });
            }

            self.formUtils.renderAnnotationTemplate("define-annotation", annotations);
            self.formUtils.checkPredefinedAnnotations(checkedAnnotations);

            self.formUtils.initPerfectScroller(formConsole.cid);

            //submit button action
            $('#' + formConsole.cid).on('click', '#btn-submit', function () {

                self.formUtils.removeErrorClass();
                var previouslySavedStreamName = streamObject.getName();

                var configName = $('#streamName').val().trim();
                var streamName;
                var firstCharacterInStreamName;
                var isStreamNameUsed;
                var isErrorOccurred = false;
                /*
                * check whether the stream is inside a partition and if yes check whether it begins with '#'.
                *  If not add '#' to the beginning of the stream name.
                * */
                var isStreamSavedInsideAPartition
                    = self.configurationData.getSiddhiAppConfig().getStreamSavedInsideAPartition(id);
                if (!isStreamSavedInsideAPartition) {
                    firstCharacterInStreamName = (configName).charAt(0);
                    if (firstCharacterInStreamName === '#') {
                        self.formUtils.addErrorClass("#streamName");
                        $('#streamNameErrorMessage').text("'#' is used to define inner streams only.")
                        isErrorOccurred = true;
                        return;
                    } else {
                        streamName = configName;
                    }
                    isStreamNameUsed
                        = self.formUtils.isDefinitionElementNameUsed(streamName, id);
                    if (isStreamNameUsed) {
                        self.formUtils.addErrorClass("#streamName");
                        $('#streamNameErrorMessage').text("Stream name is already defined.")
                        isErrorOccurred = true;
                        return;
                    }
                } else {
                    firstCharacterInStreamName = (configName).charAt(0);
                    if (firstCharacterInStreamName !== '#') {
                        streamName = '#' + configName;
                    } else {
                        streamName = configName;
                    }
                    var partitionWhereStreamIsSaved
                        = self.configurationData.getSiddhiAppConfig().getPartitionWhereStreamIsSaved(id);
                    var partitionId = partitionWhereStreamIsSaved.getId();
                    isStreamNameUsed
                        = self.formUtils.isStreamDefinitionNameUsedInPartition(partitionId, streamName, id);
                    if (isStreamNameUsed) {
                        self.formUtils.addErrorClass("#streamName");
                        $('#streamNameErrorMessage').text("Stream name is already defined in the partition.")
                        isErrorOccurred = true;
                        return;
                    }
                }
                //check if stream name is empty
                if (streamName == "") {
                    self.formUtils.addErrorClass("#streamName");
                    $('#streamNameErrorMessage').text("Stream name is required.")
                    isErrorOccurred = true;
                    return;
                }
                var previouslySavedName = streamObject.getName();
                if (previouslySavedName === undefined) {
                    previouslySavedName = "";
                }
                if (previouslySavedName !== streamName) {
                    if (self.formUtils.validateAttributeOrElementName("#streamName", Constants.STREAM, streamName)) {
                        isErrorOccurred = true;
                        return;
                    }
                }

                var attributeNameList = [];
                if (self.formUtils.validateAttributes(attributeNameList)) {
                    isErrorOccurred = true;
                    return;
                }
                if (attributeNameList.length == 0) {
                    self.formUtils.addErrorClass($('.attribute:eq(0)').find('.attr-name'));
                    $('.attribute:eq(0)').find('.error-message').text("Minimum one attribute is required.")
                    isErrorOccurred = true;
                    return;
                }

                var annotationNodes = [];
                if (self.formUtils.validateAnnotations(predefinedAnnotationList, annotationNodes)) {
                    isErrorOccurred = true;
                    return;
                }

                // If this is an inner stream perform validation
                var streamSavedInsideAPartition
                    = self.configurationData.getSiddhiAppConfig().getStreamSavedInsideAPartition(id);
                // if streamSavedInsideAPartition is undefined then the stream is not inside a partition
                if (streamSavedInsideAPartition !== undefined) {
                    var isValid = JSONValidator.prototype.validateInnerStream(streamObject, self.jsPlumbInstance,
                        false);
                    if (!isValid) {
                        isErrorOccurred = true;
                        return;
                    }
                }

                if (!isErrorOccurred) {
                    // update selected stream model
                    streamObject.setName(streamName);
                    var textNode = $('#' + id).find('.streamNameNode');
                    textNode.html(streamName);

                    //clear the previously saved attribute list
                    streamObject.clearAttributeList();
                    //add the attributes to the attribute list
                    $('.attribute .attr-content').each(function () {
                        var nameValue = $(this).find('.attr-name').val().trim();
                        var typeValue = $(this).find('.attr-type').val();
                        if (nameValue != "") {
                            var attributeObject = new Attribute({name: nameValue, type: typeValue});
                            streamObject.addAttribute(attributeObject)
                        }
                    });

                    var annotationStringList = [];
                    var annotationObjectList = [];
                    //clear the saved annotations
                    streamObject.clearAnnotationList();
                    streamObject.clearAnnotationListObjects();
                    self.formUtils.buildAnnotation(annotationNodes, annotationStringList, annotationObjectList);
                    _.forEach(annotationStringList, function (annotation) {
                        streamObject.addAnnotation(annotation);
                    });
                    _.forEach(annotationObjectList, function (annotation) {
                        streamObject.addAnnotationObject(annotation);
                    });

                    if (self.formUtils.isUpdatingOtherElementsRequired(previousStreamObject, streamObject,
                        Constants.STREAM)) {
                        var outConnections = self.jsPlumbInstance.getConnections({source: id + '-out'});
                        var inConnections = self.jsPlumbInstance.getConnections({target: id + '-in'});

                        //to delete the connection, it requires the previous object name
                        streamObject.setName(previousStreamObject.getName())
                        // delete connections related to the element if the name is changed
                        self.formUtils.deleteConnectionsAfterDefinitionElementNameChange(outConnections, inConnections);
                        //reset the name to new name
                        streamObject.setName(streamName);

                        // establish connections related to the element if the name is changed
                        self.formUtils.establishConnectionsAfterDefinitionElementNameChange(outConnections, inConnections);
                    }

                    self.dropElementInstance.toggleFaultStreamConnector(streamObject, self.jsPlumbInstance,
                        previouslySavedStreamName);

                    $('#' + id).removeClass('incomplete-element');
                    //Send stream element to the backend and generate tooltip
                    var streamToolTip = self.formUtils.getTooltip(streamObject, Constants.STREAM);
                    $('#' + id).prop('title', streamToolTip);

                    // set the isDesignViewContentChanged to true
                    self.configurationData.setIsDesignViewContentChanged(true);
                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                    if (self.application.browserStorage.get("isWidgetFromTourGuide")) {
                        self.consoleListManager.removeAllConsoles();
                    }
                }
            });

            // 'Cancel' button action
            $('#' + formConsole.cid).on('click', '#btn-cancel', function () {
                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            });

        };
        return StreamForm;
    });

