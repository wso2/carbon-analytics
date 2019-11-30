/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'log', 'jquery', 'lodash', 'partitionWith', 'jsonValidator', 'handlebar', 'constants'],
    function (require, log, $, _, PartitionWith, JSONValidator, Handlebars, Constants) {

        /**
         * @class PartitionForm Creates a forms to collect data from a partition
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var PartitionForm = function (options) {
            if (options !== undefined) {
                this.configurationData = options.configurationData;
                this.application = options.application;
                this.consoleListManager = options.application.outputController;
                this.formUtils = options.formUtils;
                this.jsPlumbInstance = options.jsPlumbInstance;
                var currentTabId = this.application.tabController.activeTab.cid;
                this.designViewContainer = $('#design-container-' + currentTabId);
                this.toggleViewButton = $('#toggle-view-button-' + currentTabId);
            }
        };

        /**
         * Function to check if the connected streams are filled
         * @param {Object} partitionWithList
         * @return {boolean} isFilled
         */
        var ifStreamsAreFilled = function (partitionWithList) {
            var isFilled = false;
            _.forEach(partitionWithList, function (partitionKey) {
                if (partitionKey.getStreamName()) {
                    isFilled = true;
                    return false;
                }
            });
            return isFilled;
        };

        /**
         * @function to get the attribute names of the connected stream
         */
        var getAttributeNames = function (streamName, self) {
            var connectedElement = self.configurationData.getSiddhiAppConfig().getDefinitionElementByName(streamName);
            var attributes = [];
            _.forEach(connectedElement.element.getAttributeList(), function (attribute) {
                attributes.push(attribute.getName());
            });
            return attributes;
        };

        /**
         * @function generate form for Partition
         * @param element selected element(partition)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        PartitionForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');
            var partitionObject = self.configurationData.getSiddhiAppConfig().getPartition(id);
            var partitionWithList = partitionObject.getPartitionWith();

            if (!partitionWithList || partitionWithList.length === 0) {
                $("#" + id).addClass('incomplete-element');
                $('#' + id).prop('title', 'Connect a stream for partitioning');

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            } else {
                if (!ifStreamsAreFilled(partitionWithList)) {
                    $("#" + id).addClass('incomplete-element');
                    $('#' + id).prop('title', 'To edit partition configuration, fill the connected stream.');

                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                    self.consoleListManager.removeAllConsoles();
                } else {
                    var propertyDiv = $('<div class="clearfix form-min-width"> <div class = "partition-form-container"> ' +
                        '<div id = "define-partition-keys"> </div> </div>' +
                        '<div class = "partition-form-container"> <div class = "define-annotation"> </div> </div> </div>');

                    formContainer.html(propertyDiv);
                    self.formUtils.buildFormButtons(formConsole.cid);
                    self.formUtils.popUpSelectedElement(id);
                    // design view container and toggle view button are enabled
                    self.designViewContainer.addClass('disableContainer');
                    self.toggleViewButton.addClass('disableContainer');

                    var annotationListObjects = partitionObject.getAnnotationListObjects();
                    self.formUtils.renderAnnotationTemplate("define-annotation", annotationListObjects);

                    var partitionKeys = [];
                    for (var i = 0; i < partitionWithList.length; i++) {
                        if (partitionWithList[i].getStreamName()) {
                            var partitionKey = {
                                expression: partitionWithList[i].getExpression(),
                                streamName: partitionWithList[i].getStreamName(),
                                streamAttributes: {
                                    id: "partition-by-expression",
                                    options: getAttributeNames(partitionWithList[i].getStreamName(), self)
                                }
                            };
                            partitionKeys.push(partitionKey);
                        }
                    }
                    self.formUtils.registerDropDownPartial();
                    var partitionFormTemplate = Handlebars.compile($('#partition-by-template').html())(partitionKeys);
                    $('#define-partition-keys').html(partitionFormTemplate);

                    //to map partition expressions
                    var i = 0;
                    $('.partition-key').each(function () {
                        $(this).find('.partition-by-expression-selection option').filter(function () {
                            return ($(this).val() == (partitionKeys[i].expression));
                        }).prop('selected', true);
                        i++;
                    });

                    self.formUtils.addEventListenerToRemoveRequiredClass();
                    self.formUtils.addEventListenerToShowInputContentOnHover();

                    self.formUtils.initPerfectScroller(formConsole.cid);

                    // 'Submit' button action
                    $('#' + formConsole.cid).on('click', '#btn-submit', function () {

                        self.formUtils.removeErrorClass();
                        var isErrorOccurred = false;

                        var partitionKeys = [];
                        $('#partition-by-content .partition-key').each(function () {
                            var expression = $(this).find('.partition-by-expression-selection');
                            if (!expression.val()) {
                                $(this).find('.error-message').text("Expression is required");
                                self.formUtils.addErrorClass(expression);
                                isErrorOccurred = true;
                                return false;
                            } else {
                                var streamName = $(this).find('.partition-by-stream-name').val().trim();
                                var partitionKey = {
                                    expression: expression.val(),
                                    streamName: streamName
                                };
                                partitionKeys.push(partitionKey);
                            }
                        });

                        if (!isErrorOccurred) {
                            partitionObject.clearPartitionWith();
                            _.forEach(partitionKeys, function (partitionKey) {
                                var partitionWithObject = new PartitionWith(partitionKey);
                                partitionObject.addPartitionWith(partitionWithObject);
                            });

                            var isValid = JSONValidator.prototype.validatePartition(partitionObject, self.jsPlumbInstance,
                                false);
                            if (!isValid) {
                                return;
                            }

                            partitionObject.clearAnnotationList();
                            partitionObject.clearAnnotationListObjects();
                            var annotationStringList = [];
                            var annotationObjectList = [];
                            var annotationNodes = $('#annotation-div').jstree(true)._model.data['#'].children;
                            self.formUtils.buildAnnotation(annotationNodes, annotationStringList, annotationObjectList);
                            _.forEach(annotationStringList, function (annotation) {
                                partitionObject.addAnnotation(annotation);
                            });
                            _.forEach(annotationObjectList, function (annotation) {
                                partitionObject.addAnnotationObject(annotation);
                            });


                            $('#' + id).removeClass('incomplete-element');
                            //Send partition element to the backend and generate tooltip
                            var partitionToolTip = self.formUtils.getTooltip(partitionObject, Constants.PARTITION);
                            $('#' + id).prop('title', partitionToolTip);

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
            }
        };

        return PartitionForm;
    });

