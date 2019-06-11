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

define(['require', 'log', 'jquery', 'lodash', 'querySelect', 'queryOrderByValue', 'designViewUtils',
        'jsonValidator', 'constants', 'handlebar'],
    function (require, log, $, _, QuerySelect, QueryOrderByValue, DesignViewUtils, JSONValidator, Constants, Handlebars) {

        /**
         * @class SequenceQueryForm Creates a forms to collect data from a pattern query
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var SequenceQueryForm = function (options) {
            if (options !== undefined) {
                this.configurationData = options.configurationData;
                this.application = options.application;
                this.formUtils = options.formUtils;
                this.consoleListManager = options.application.outputController;
                this.currentTabId = this.application.tabController.activeTab.cid;
                this.designViewContainer = $('#design-container-' + this.currentTabId);
                this.toggleViewButton = $('#toggle-view-button-' + this.currentTabId);
            }
        };

        /**
         * @function to get the possible attributes
         * <conditionId>.<attributeOfTheConnectedStream>
         */
        var getPossibleAttributes = function (self, partitionId) {
            var possibleAttributes = [];
            $('.condition-content').each(function () {
                var conditionId = $(this).find('.condition-id').val().trim();
                var connectedStreamName = $(this).find('.condition-stream-name-selection').val();
                if (connectedStreamName) {
                    var inputElement = self.configurationData.getSiddhiAppConfig()
                        .getDefinitionElementByName(connectedStreamName, partitionId);
                    if (inputElement.type.toLowerCase() === Constants.TRIGGER) {
                        possibleAttributes.push(conditionId + "." + Constants.TRIGGERED_TIME);
                    } else {
                        _.forEach(inputElement.element.getAttributeList(), function (attribute) {
                            possibleAttributes.push(conditionId + "." + attribute.getName());
                        });
                    }
                }
            });
            return possibleAttributes;
        };

        /**
         * @function to get all the defined stream handlers
         */
        var getStreamHandlers = function (conditionList) {
            var streamHandlerList = [];
            _.forEach(conditionList, function (condition) {
                _.forEach(condition.streamHandlerList, function (streamHandler) {
                    streamHandlerList.push(streamHandler);
                });
            });
            return streamHandlerList;
        };

        /**
         * @function add autocomplete for fields with changing attributes
         */
        var autoCompleteFieldsWithChangingAttributes = function (self, partitionId, outputAttributes) {
            var possibleAttributes = getPossibleAttributes(self, partitionId);
            self.formUtils.addAutoCompleteForSelectExpressions(possibleAttributes);
            self.formUtils.addAutoCompleteForFilterConditions(possibleAttributes.concat(outputAttributes));
            self.formUtils.addAutoCompleteForLogicStatements();
            self.formUtils.addAutoCompleteForHavingCondition(possibleAttributes.concat(outputAttributes));
            self.formUtils.addAutoCompleteForOnCondition(possibleAttributes.concat(outputAttributes),
                self.formUtils.getPatternSequenceInputs());
            self.formUtils.addAutoCompleteForOutputOperation(outputAttributes, possibleAttributes);
        };

        /**
         * @function to generate the group-by and order-by div when the condition id or the the
         * condition's connected stream is changed
         */
        var generateDivRequiringPossibleAttributes = function (self, partitionId, groupBy) {
            var possibleAttributes = getPossibleAttributes(self, partitionId);
            self.formUtils.generateGroupByDiv(groupBy, possibleAttributes);
        };

        /**
         * @function to validate on load of the form
         */
        var validateSectionsOnLoadOfForm = function (self) {
            var isErrorOccurred = false;
            if ($('.group-by-checkbox').is(':checked')) {
                if (self.formUtils.validateGroupOrderBy(Constants.GROUP_BY)) {
                    isErrorOccurred = true;
                }
            }
            if ($('.order-by-checkbox').is(':checked')) {
                if (self.formUtils.validateGroupOrderBy(Constants.ORDER_BY)) {
                    isErrorOccurred = true;
                }
            }
            if (self.formUtils.validateQueryProjection()) {
                isErrorOccurred = true;
            }
            if (self.formUtils.validateRequiredFields('.define-content')) {
                isErrorOccurred = true;
            }
            if (self.formUtils.validateQueryOutputSet()) {
                isErrorOccurred = true;
            }
            return isErrorOccurred;
        };

        /**
         * @function to add new filter stream handler
         */
        var addEventListenerToAddNewFilter = function (self, partitionId, outputAttributes) {
            $('.define-stream-handler').on('click', '.btn-add-filter', function () {
                var sourceDiv = self.formUtils.getSourceDiv($(this));
                self.formUtils.addNewStreamHandler(sourceDiv, Constants.FILTER);
                autoCompleteFieldsWithChangingAttributes(self, partitionId, outputAttributes);
            });
        };

        /**
         * @function generate the form for the sequence query
         * @param element selected element(query)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        SequenceQueryForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');
            var sequenceQueryObject = self.configurationData.getSiddhiAppConfig().getSequenceQuery(id);

            if (!sequenceQueryObject.getQueryInput()
                || sequenceQueryObject.getQueryInput().getConnectedElementNameList().length === 0) {
                DesignViewUtils.prototype.warnAlert('Connect input streams');
                self.consoleListManager.removeFormConsole(formConsole);
            } else if (!self.formUtils.isOneElementFilled(sequenceQueryObject.getQueryInput().getConnectedElementNameList())) {
                DesignViewUtils.prototype.warnAlert('Fill the incomplete input stream');
                self.consoleListManager.removeFormConsole(formConsole);
            } else if (!sequenceQueryObject.getQueryOutput() || !sequenceQueryObject.getQueryOutput().getTarget()) {
                DesignViewUtils.prototype.warnAlert('Connect an output element');
                self.consoleListManager.removeFormConsole(formConsole);
            } else {
                var propertyDiv = $('<div id="define-sequence-query" class="clearfix form-min-width"></div>');
                formContainer.append(propertyDiv);
                self.formUtils.buildFormButtons(formConsole.cid);

                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');
                self.formUtils.popUpSelectedElement(id);

                var queryName = sequenceQueryObject.getQueryName();
                var conditionList = sequenceQueryObject.getQueryInput().getConditionList();
                var logic = sequenceQueryObject.getQueryInput().getLogic();
                var groupBy = sequenceQueryObject.getGroupBy();
                var having = sequenceQueryObject.getHaving();
                var orderBy = sequenceQueryObject.getOrderBy();
                var limit = sequenceQueryObject.getLimit();
                var offset = sequenceQueryObject.getOffset();
                var outputRateLimit = sequenceQueryObject.getOutputRateLimit();
                var outputElementName = sequenceQueryObject.getQueryOutput().getTarget();
                var select = sequenceQueryObject.getSelect();
                var annotationListObjects = sequenceQueryObject.getAnnotationListObjects();
                var queryInput = sequenceQueryObject.getQueryInput();
                var queryOutput = sequenceQueryObject.getQueryOutput();

                var partitionId;
                var partitionElementWhereQueryIsSaved
                    = self.configurationData.getSiddhiAppConfig().getPartitionWhereQueryIsSaved(id);
                if (partitionElementWhereQueryIsSaved !== undefined) {
                    partitionId = partitionElementWhereQueryIsSaved.getId();
                }
                var outputElement = self.configurationData.getSiddhiAppConfig()
                    .getDefinitionElementByName(outputElementName, partitionId);

                var predefinedAnnotations = _.cloneDeep(self.configurationData.application.config.type_query_predefined_annotations);

                //render the sequence-query form template
                var sequenceFormTemplate = Handlebars.compile($('#pattern-sequence-query-form-template').html())
                ({name: queryName});
                $('#define-sequence-query').html(sequenceFormTemplate);
                self.formUtils.renderQueryOutput(outputElement, queryOutput);
                self.formUtils.renderOutputEventTypes();

                self.formUtils.addEventListenerForQueryOutputDiv();
                self.formUtils.addEventListenerToRemoveRequiredClass();
                self.formUtils.addEventListenerToShowAndHideInfo();
                self.formUtils.addEventListenerToShowInputContentOnHover();

                $('.pattern-sequence-query-form-container').on('change', '.query-checkbox', function () {
                    var parent = $(this).parents(".define-content")
                    if ($(this).is(':checked')) {
                        parent.find('.query-content').show();
                        parent.find('.query-content-value').removeClass('required-input-field')
                        parent.find('.error-message').text("");
                    } else {
                        parent.find('.query-content').hide();
                    }
                    self.formUtils.updatePerfectScroller();
                });

                var eventType = Constants.CURRENT_EVENTS;
                if (queryOutput.output && queryOutput.output.eventType) {
                    eventType = queryOutput.output.eventType.toLowerCase();
                }
                $('.define-output-events').find('#event-type option').filter(function () {
                    return ($(this).val() == eventType);
                }).prop('selected', true);

                //annotations
                predefinedAnnotations = self.formUtils.createObjectsForAnnotationsWithKeys(predefinedAnnotations);
                var userDefinedAnnotations = self.formUtils.getUserAnnotations(annotationListObjects,
                    predefinedAnnotations);
                self.formUtils.renderAnnotationTemplate("define-user-defined-annotations", userDefinedAnnotations);
                $('.define-user-defined-annotations').find('label:first-child').html('Customized Annotations');
                self.formUtils.mapPredefinedAnnotations(annotationListObjects, predefinedAnnotations);
                self.formUtils.renderPredefinedAnnotations(predefinedAnnotations,
                    'define-predefined-annotations');
                self.formUtils.renderOptionsForPredefinedAnnotations(predefinedAnnotations);
                self.formUtils.addEventListenersForPredefinedAnnotations();

                var connectedStreams = sequenceQueryObject.getQueryInput().getConnectedElementNameList();
                var inputStreamNames = [];
                _.forEach(connectedStreams, function (streamName) {
                    if (streamName) {
                        inputStreamNames.push(streamName)
                    }
                });

                //conditions
                if (!conditionList || (conditionList && conditionList.length == 0)) {
                    conditionList = [{conditionId: "e1", streamHandlerList: [], streamName: ""}];
                    queryInput.setConditionList(conditionList);
                }
                self.formUtils.renderConditions(conditionList, inputStreamNames);
                self.formUtils.mapConditions(conditionList);
                self.formUtils.selectFirstConditionByDefault();
                var streamHandlerList = getStreamHandlers(conditionList);
                self.formUtils.addEventListenersForStreamHandlersDiv(streamHandlerList);
                self.formUtils.addEventListenersForConditionDiv();

                var outputAttributes = [];
                if (outputElement.type.toLowerCase() === Constants.STREAM ||
                    outputElement.type.toLowerCase() === Constants.TABLE) {
                    var streamAttributes = outputElement.element.getAttributeList();
                    _.forEach(streamAttributes, function (attribute) {
                        outputAttributes.push(attribute.getName());
                    });
                }
                self.formUtils.generateOrderByDiv(orderBy, outputAttributes);
                generateDivRequiringPossibleAttributes(self, partitionId, groupBy);

                //projection
                self.formUtils.selectQueryProjection(select, outputElementName);
                self.formUtils.addEventListenersForSelectionDiv();

                if (having) {
                    $('.having-value').val(having);
                    $(".having-checkbox").prop("checked", true);
                } else {
                    $('.having-condition-content').hide();
                }

                if (limit) {
                    $('.limit-value').val(limit);
                    $(".limit-checkbox").prop("checked", true);
                } else {
                    $('.limit-content').hide();
                }

                if (offset) {
                    $('.offset-value').val(offset);
                    $(".offset-checkbox").prop("checked", true);
                } else {
                    $('.offset-content').hide();
                }

                if (outputRateLimit) {
                    $('.rate-limiting-value').val(outputRateLimit);
                    $(".rate-limiting-checkbox").prop("checked", true);
                } else {
                    $('.rate-limiting-content').hide();
                }

                if (logic) {
                    $('.logic-statement').val(logic)
                }

                /**
                 * to show user the lost saved data when the connection is deleted/ when the connected stream is modified
                 * only if the form is an already edited form
                 */
                if (queryOutput && queryOutput.type) {
                    validateSectionsOnLoadOfForm(self);
                }

                var inputAttributes = [];
                _.forEach(self.formUtils.getInputAttributes(inputStreamNames), function (attribute) {
                    inputAttributes.push(attribute.name);
                });
                var outputAttributesWithElementName = self.formUtils.constructOutputAttributes(outputAttributes);
                self.formUtils.addAutoCompleteForRateLimits();
                autoCompleteFieldsWithChangingAttributes(self, partitionId, outputAttributesWithElementName);

                addEventListenerToAddNewFilter(self, partitionId, outputAttributesWithElementName);

                $('.define-conditions').on('click', '.btn-del-condition', function () {
                    var conditionIndex = $(this).closest('.condition-navigation').index();
                    var prevCondition = conditionIndex - 1;
                    $('.define-conditions .nav-tabs .condition-navigation:eq(' + prevCondition + ')').addClass('active');
                    $('.define-conditions .tab-pane:eq(' + prevCondition + ')').addClass('active');
                    $('.define-conditions .tab-pane:eq(' + conditionIndex + ')').remove();
                    $(this).closest('li').remove();
                    generateDivRequiringPossibleAttributes(self, partitionId, groupBy);
                    autoCompleteFieldsWithChangingAttributes(self, partitionId, outputAttributesWithElementName);
                });

                $('.define-conditions').on('click', '.btn-add-condition', function () {
                    self.formUtils.addNewCondition(inputStreamNames);
                    generateDivRequiringPossibleAttributes(self, partitionId, groupBy);
                    autoCompleteFieldsWithChangingAttributes(self, partitionId, outputAttributesWithElementName);
                    addEventListenerToAddNewFilter(self, partitionId, outputAttributesWithElementName);
                });

                $('.define-conditions').on('blur', '.condition-id', function () {
                    generateDivRequiringPossibleAttributes(self, partitionId, groupBy);
                    autoCompleteFieldsWithChangingAttributes(self, partitionId, outputAttributes);
                });

                $('.define-conditions').on('change', '.condition-stream-name-selection', function () {
                    generateDivRequiringPossibleAttributes(self, partitionId, groupBy);
                    autoCompleteFieldsWithChangingAttributes(self, partitionId, outputAttributesWithElementName);
                });

                //to add query operation set
                var setDiv = '<li class="setAttributeValue">' +
                    '<div class="clearfix">' +
                    '<input type="text" class="setAttribute"> <input type="text" class="setValue"> ' +
                    '<a class = "btn-del-option"> <i class = "fw fw-delete"> </i> </a>' +
                    '</div> <label class="error-message"> </label> </li>'
                $('.define-operation-set-condition').on('click', '.btn-add-set', function () {
                    $('.define-operation-set-condition .set-condition').append(setDiv);
                    autoCompleteFieldsWithChangingAttributes(self, partitionId, outputAttributesWithElementName);
                    self.formUtils.updatePerfectScroller();
                });

                self.formUtils.initPerfectScroller(formConsole.cid);

                $('#' + formConsole.cid).on('click', '#btn-submit', function () {

                    self.formUtils.removeErrorClass();
                    var isErrorOccurred = false;

                    var queryName = $('.query-name').val().trim();
                    var isQueryNameUsed
                        = self.formUtils.isQueryDefinitionNameUsed(queryName, id);
                    if (isQueryNameUsed) {
                        self.formUtils.addErrorClass($('.query-name'));
                        $('.query-name-div').find('.error-message').text('Query name is already used.');
                        isErrorOccurred = true;
                        return;
                    }

                    if (self.formUtils.validatePredefinedAnnotations(predefinedAnnotations)) {
                        isErrorOccurred = true;
                        return;
                    }

                    if (validateSectionsOnLoadOfForm(self)) {
                        isErrorOccurred = true;
                        return;
                    }

                    if (self.formUtils.validateConditions()) {
                        isErrorOccurred = true;
                        return;
                    }

                    if (!isErrorOccurred) {
                        if (queryName != "") {
                            sequenceQueryObject.addQueryName(queryName);
                        } else {
                            queryName = "Sequence Query";
                            sequenceQueryObject.addQueryName('query');
                        }

                        if ($('.group-by-checkbox').is(':checked')) {
                            var groupByAttributes = self.formUtils.buildGroupBy();
                            sequenceQueryObject.setGroupBy(groupByAttributes);
                        } else {
                            sequenceQueryObject.setGroupBy(undefined);
                        }

                        sequenceQueryObject.clearOrderByValueList()
                        if ($('.order-by-checkbox').is(':checked')) {
                            var orderByAttributes = self.formUtils.buildOrderBy();
                            _.forEach(orderByAttributes, function (attribute) {
                                var orderByValueObject = new QueryOrderByValue(attribute);
                                sequenceQueryObject.addOrderByValue(orderByValueObject);
                            });
                        }

                        if ($('.having-checkbox').is(':checked')) {
                            sequenceQueryObject.setHaving($('.having-value').val().trim());
                        } else {
                            sequenceQueryObject.setHaving(undefined)
                        }

                        if ($('.limit-checkbox').is(':checked')) {
                            sequenceQueryObject.setLimit($('.limit-value').val().trim())
                        } else {
                            sequenceQueryObject.setLimit(undefined)
                        }

                        if ($('.offset-checkbox').is(':checked')) {
                            sequenceQueryObject.setOffset($('.offset-value').val().trim())
                        } else {
                            sequenceQueryObject.setOffset(undefined)
                        }

                        if ($('.rate-limiting-checkbox').is(':checked')) {
                            sequenceQueryObject.setOutputRateLimit($('.rate-limiting-value').val().trim())
                        } else {
                            sequenceQueryObject.setOutputRateLimit(undefined)
                        }

                        queryInput.setLogic($('.logic-statement').val().trim());

                        var selectObject = new QuerySelect(self.formUtils.buildAttributeSelection(Constants
                            .SEQUENCE_QUERY));
                        sequenceQueryObject.setSelect(selectObject);

                        var conditions = self.formUtils.buildConditions();
                        queryInput.setConditionList(conditions);

                        var annotationObjectList = [];
                        var annotationStringList = [];
                        var annotationNodes = $('#annotation-div').jstree(true)._model.data['#'].children;
                        self.formUtils.buildAnnotation(annotationNodes, annotationStringList, annotationObjectList);
                        self.formUtils.buildPredefinedAnnotations(predefinedAnnotations, annotationStringList,
                            annotationObjectList);
                        sequenceQueryObject.clearAnnotationList();
                        sequenceQueryObject.clearAnnotationListObjects();
                        //add the annotations to the clicked element
                        _.forEach(annotationStringList, function (annotation) {
                            sequenceQueryObject.addAnnotation(annotation);
                        });
                        _.forEach(annotationObjectList, function (annotation) {
                            sequenceQueryObject.addAnnotationObject(annotation);
                        });

                        self.formUtils.buildQueryOutput(outputElement, queryOutput);

                        JSONValidator.prototype.validatePatternOrSequenceQuery(sequenceQueryObject, Constants.SEQUENCE_QUERY);
                        self.configurationData.setIsDesignViewContentChanged(true);

                        //Send sequence-query element to the backend and generate tooltip
                        var queryToolTip = self.formUtils.getTooltip(sequenceQueryObject, Constants.SEQUENCE_QUERY);
                        $('#' + id).prop('title', queryToolTip);
                        var textNode = $('#' + id).find('.sequenceQueryNameNode');
                        textNode.html(queryName);

                        // close the form window
                        self.consoleListManager.removeFormConsole(formConsole);
                    }
                })

                $('#' + formConsole.cid).on('click', '#btn-cancel', function () {
                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                });
            }
        };

        return SequenceQueryForm;
    });
