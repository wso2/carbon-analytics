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

define(['require', 'log', 'jquery', 'lodash', 'querySelect', 'queryOutputInsert', 'queryOrderByValue', 'designViewUtils',
    'jsonValidator', 'constants', 'handlebar'],
    function (require, log, $, _, QuerySelect, QueryOutputInsert, QueryOrderByValue, DesignViewUtils,
        JSONValidator, Constants, Handlebars) {

        /**
         * @class PatternQueryForm Creates a forms to collect data from a pattern query
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var PatternQueryForm = function (options) {
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
                var inputElement = self.configurationData.getSiddhiAppConfig()
                    .getDefinitionElementByName(connectedStreamName, partitionId);
                if (inputElement.type.toLowerCase() === Constants.TRIGGER) {
                    possibleAttributes.push(conditionId + "." + Constants.TRIGGERED_TIME);
                } else {
                    _.forEach(inputElement.element.getAttributeList(), function (attribute) {
                        possibleAttributes.push(conditionId + "." + attribute.getName());
                    });
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
         * @function to add autocompletion for input fields
         */
        var addAutoCompletion = function (self, partitionId, QUERY_CONDITION_SYNTAX, QUERY_SYNTAX,
            incrementalAggregator, streamFunctions) {
            possibleAttributes = getPossibleAttributes(self, partitionId);
            var selectExpressionMatches = JSON.parse(JSON.stringify(possibleAttributes));
            selectExpressionMatches = selectExpressionMatches.concat(incrementalAggregator);
            selectExpressionMatches = selectExpressionMatches.concat(streamFunctions);
            var filterMatches = JSON.parse(JSON.stringify(possibleAttributes));
            filterMatches = filterMatches.concat(QUERY_CONDITION_SYNTAX);
            var logicMatches = filterMatches.concat(QUERY_SYNTAX);
            logicMatches = logicMatches.concat(Constants.SIDDHI_TIME);
            self.formUtils.createAutocomplete($('.attribute-expression'), selectExpressionMatches);
            self.formUtils.createAutocomplete($('.logic-statement'), logicMatches);
            self.formUtils.createAutocomplete($('.symbol-syntax-required-value'), filterMatches);
        };

        /**
         * @function to generate the group-by and order-by div when the condition id or the the
         * condition's connected stream is changed
         */
        var generateDivRequiringPossibleAttributes = function (self, partitionId, groupBy, orderBy) {
            possibleAttributes = getPossibleAttributes(self, partitionId);
            self.formUtils.generateGroupByDiv(groupBy, possibleAttributes);
            self.formUtils.generateOrderByDiv(orderBy, possibleAttributes);
        };

        /**
         * @function generate the form for the pattern query
         * @param element selected element(query)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        PatternQueryForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');
            var clickedElement = self.configurationData.getSiddhiAppConfig().getPatternQuery(id);

            if (!clickedElement.getQueryInput()
                || clickedElement.getQueryInput().getConnectedElementNameList().length === 0) {
                DesignViewUtils.prototype.warnAlert('Connect input streams');
                self.consoleListManager.removeFormConsole(formConsole);
            } else if (!self.formUtils.isOneElementFilled(clickedElement.getQueryInput().getConnectedElementNameList())) {
                DesignViewUtils.prototype.warnAlert('Fill the incomplete input stream');
                self.consoleListManager.removeFormConsole(formConsole);
            } else if (!clickedElement.getQueryOutput() || !clickedElement.getQueryOutput().getTarget()) {
                DesignViewUtils.prototype.warnAlert('Connect an output element');
                self.consoleListManager.removeFormConsole(formConsole);
            } else {
                var propertyDiv = $('<div id="property-header"><h3>Pattern Query Configuration</h3></div>' +
                    '<div id="define-pattern-query"></div>' + self.formUtils.buildFormButtons());
                formContainer.append(propertyDiv);

                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');
                self.formUtils.popUpSelectedElement(id);

                const QUERY_CONDITION_SYNTAX = self.configurationData.application.config.query_condition_syntax;
                const QUERY_SYNTAX = self.configurationData.application.config.other_query_syntax;

                var incrementalAggregator = self.configurationData.application.config.incremental_aggregator;
                var queryName = clickedElement.getQueryName();
                var conditionList = clickedElement.getQueryInput().getConditionList();
                var logic = clickedElement.getQueryInput().getLogic();
                var groupBy = clickedElement.getGroupBy();
                var having = clickedElement.getHaving();
                var orderBy = clickedElement.getOrderBy();
                var limit = clickedElement.getLimit();
                var outputRateLimit = clickedElement.getOutputRateLimit();
                var outputElementName = clickedElement.getQueryOutput().getTarget();
                var select = clickedElement.getSelect();
                var savedAnnotations = clickedElement.getAnnotationListObjects();
                var queryInput = clickedElement.getQueryInput();
                var queryOutput = clickedElement.getQueryOutput();

                var predefinedAnnotations = JSON.parse(JSON.stringify(self.configurationData.application.config.
                    query_predefined_annotations));
                var streamFunctions = self.formUtils.getStreamFunctionNames();

                //render the pattern-query form template
                var patternFormTemplate = Handlebars.compile($('#pattern-sequence-query-form-template').html());
                var wrappedHtml = patternFormTemplate({ name: queryName });
                $('#define-pattern-query').html(wrappedHtml);
                self.formUtils.renderQueryOutput(outputElementName);
                self.formUtils.renderOutputEventTypes();

                self.formUtils.addEventListenerToRemoveRequiredClass();

                $('.pattern-sequence-query-form-container').on('change', '.query-checkbox', function () {
                    var parent = $(this).parents(".define-content")
                    if ($(this).is(':checked')) {
                        parent.find('.query-content').show();
                        parent.find('.query-content-value').removeClass('required-input-field')
                        parent.find('.error-message').text("");
                    } else {
                        parent.find('.query-content').hide();
                    }
                });

                if (queryOutput.eventType) {
                    $('.define-output-events').find('#event-type option').filter(function () {
                        return ($(this).val() == eventType.toLowerCase());
                    }).prop('selected', true);
                }

                //annotations
                var userDefinedAnnotations = self.formUtils.getUserAnnotations(savedAnnotations,
                    predefinedAnnotations);
                self.formUtils.renderAnnotationTemplate("define-user-defined-annotations", userDefinedAnnotations);
                $('.define-user-defined-annotations').find('h4').html('Customized Annotations');
                self.formUtils.renderPredefinedAnnotations(predefinedAnnotations,
                    'define-predefined-annotations');
                self.formUtils.mapPredefinedAnnotations(savedAnnotations, predefinedAnnotations);
                self.formUtils.renderOptionsForPredefinedAnnotations(predefinedAnnotations);
                self.formUtils.addCheckedForUserSelectedPredefinedAnnotation(savedAnnotations, predefinedAnnotations);
                self.formUtils.addEventListenersForPredefinedAnnotations();

                var connectedStreams = clickedElement.getQueryInput().getConnectedElementNameList();
                var inputStreamNames = [];
                _.forEach(connectedStreams, function (streamName) {
                    if (streamName) {
                        inputStreamNames.push(streamName)
                    }
                });

                //conditions
                self.formUtils.renderConditions(conditionList, inputStreamNames);
                self.formUtils.mapConditions(conditionList);
                var streamHandlerList = getStreamHandlers(conditionList);
                self.formUtils.addEventListenersForStreamHandlersDiv(streamHandlerList);
                self.formUtils.addEventListenersForConditionDiv(inputStreamNames);

                var partitionId;
                var partitionElementWhereQueryIsSaved
                    = self.configurationData.getSiddhiAppConfig().getPartitionWhereQueryIsSaved(id);
                if (partitionElementWhereQueryIsSaved !== undefined) {
                    partitionId = partitionElementWhereQueryIsSaved.getId();
                }

                generateDivRequiringPossibleAttributes(self, partitionId, groupBy, orderBy);

                //projection
                self.formUtils.selectQueryProjection(select, outputElementName);
                self.formUtils.addEventListenersForSelectionDiv();

                if (having) {
                    $('.post-condition-value').val(having);
                    $(".post-filter-checkbox").prop("checked", true);
                } else {
                    $('.post-filter-condition-content').hide();
                }

                if (limit) {
                    $('.limit-value').val(limit);
                    $(".limit-checkbox").prop("checked", true);
                } else {
                    $('.limit-content').hide();
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

                addAutoCompletion(self, partitionId, QUERY_CONDITION_SYNTAX, QUERY_SYNTAX, incrementalAggregator,
                    streamFunctions);

                $('.define-stream-handler').on('click', '.btn-add-filter', function () {
                    var sourceDiv = self.formUtils.getSourceDiv($(this));
                    self.formUtils.addNewStreamHandler(sourceDiv, Constants.FILTER);
                    addAutoCompletion(self, partitionId, QUERY_CONDITION_SYNTAX, QUERY_SYNTAX, incrementalAggregator,
                        streamFunctions);
                });

                $('.define-conditions').on('click', '.btn-del-condition', function () {
                    var conditionIndex = $(this).closest('li').index();
                    $('.define-conditions .tab-pane:eq(' + conditionIndex + ')').remove();
                    $(this).closest('li').remove();
                    generateDivRequiringPossibleAttributes(self, partitionId, groupBy, orderBy);
                    addAutoCompletion(self, partitionId, QUERY_CONDITION_SYNTAX, QUERY_SYNTAX, incrementalAggregator,
                        streamFunctions);
                });

                $('.define-conditions').on('click', '.btn-add-condition', function () {
                    var conditionLength = $('.condition-navigation').length + 1;
                    var conditionName = 'condition' + conditionLength;
                    var conditionList = [{ conditionId: conditionName, streamHandlerList: [], streamName: "" }]
                    self.formUtils.renderConditions(conditionList, inputStreamNames)
                    generateDivRequiringPossibleAttributes(self, partitionId, groupBy, orderBy);
                    addAutoCompletion(self, partitionId, QUERY_CONDITION_SYNTAX, QUERY_SYNTAX, incrementalAggregator,
                        streamFunctions);
                });

                $('.define-conditions').on('blur', '.condition-id', function () {
                    generateDivRequiringPossibleAttributes(self, partitionId, groupBy, orderBy);
                    addAutoCompletion(self, partitionId, QUERY_CONDITION_SYNTAX, QUERY_SYNTAX, incrementalAggregator,
                        streamFunctions);
                });

                $('.define-conditions').on('change', '.condition-stream-name-selection', function () {
                    generateDivRequiringPossibleAttributes(self, partitionId, groupBy, orderBy);
                    addAutoCompletion(self, partitionId, QUERY_CONDITION_SYNTAX, QUERY_SYNTAX, incrementalAggregator,
                        streamFunctions);
                });

                var rateLimitingMatches = QUERY_SYNTAX.concat(Constants.SIDDHI_TIME);
                self.formUtils.createAutocomplete($('.rate-limiting-value'), rateLimitingMatches);

                $(formContainer).on('click', '#btn-submit', function () {

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

                    if ($('.group-by-checkbox').is(':checked')) {
                        if (self.formUtils.validateGroupOrderBy(Constants.GROUP_BY)) {
                            isErrorOccurred = true;
                            return;
                        }
                    }

                    if ($('.order-by-checkbox').is(':checked')) {
                        if (self.formUtils.validateGroupOrderBy(Constants.ORDER_BY)) {
                            isErrorOccurred = true;
                            return;
                        }
                    }

                    if ($('.post-filter-checkbox').is(':checked')) {
                        if (self.formUtils.validateContent('.post-filter-condition-content')) {
                            isErrorOccurred = true;
                            return;
                        }
                    }

                    if ($('.limit-checkbox').is(':checked')) {
                        if (self.formUtils.validateContent('.limit-content')) {
                            isErrorOccurred = true;
                            return;
                        }
                    }

                    if ($('.rate-limiting-checkbox').is(':checked')) {
                        if (self.formUtils.validateContent('.rate-limiting-content')) {
                            isErrorOccurred = true;
                            return;
                        }
                    }

                    if (self.formUtils.validateContent('.define-logic-statement')) {
                        isErrorOccurred = true;
                        return;
                    }

                    if (self.formUtils.validatePredefinedAnnotations(predefinedAnnotations)) {
                        isErrorOccurred = true;
                        return;
                    }

                    if (self.formUtils.validateQueryProjection()) {
                        isErrorOccurred = true;
                        return;
                    }

                    if (self.formUtils.validateConditions()) {
                        isErrorOccurred = true;
                        return;
                    }

                    if (!isErrorOccurred) {
                        if (queryName != "") {
                            clickedElement.addQueryName(queryName);
                        } else {
                            queryName = "Pattern Query";
                            clickedElement.addQueryName('query');
                        }

                        if ($('.group-by-checkbox').is(':checked')) {
                            var groupByAttributes = self.formUtils.buildGroupBy();
                            clickedElement.setGroupBy(groupByAttributes);
                        } else {
                            clickedElement.setGroupBy(undefined);
                        }

                        clickedElement.clearOrderByValueList()
                        if ($('.order-by-checkbox').is(':checked')) {
                            var orderByAttributes = self.formUtils.buildOrderBy();
                            _.forEach(orderByAttributes, function (attribute) {
                                var orderByValueObject = new QueryOrderByValue(attribute);
                                clickedElement.addOrderByValue(orderByValueObject);
                            });
                        }

                        if ($('.post-filter-checkbox').is(':checked')) {
                            clickedElement.setHaving($('.post-condition-value').val().trim());
                        } else {
                            clickedElement.setHaving(undefined)
                        }

                        if ($('.limit-checkbox').is(':checked')) {
                            clickedElement.setLimit($('.limit-value').val().trim())
                        } else {
                            clickedElement.setLimit(undefined)
                        }

                        if ($('.rate-limiting-checkbox').is(':checked')) {
                            clickedElement.setOutputRateLimit($('.rate-limiting-value').val().trim())
                        } else {
                            clickedElement.setOutputRateLimit(undefined)
                        }

                        queryInput.setLogic($('.logic-statement').val().trim());

                        var selectObject = new QuerySelect(self.formUtils.buildAttributeSelection(Constants.PATTERN_QUERY));
                        clickedElement.setSelect(selectObject);

                        queryInput.clearConditionList();
                        var conditions = self.formUtils.buildConditions();
                        _.forEach(conditions, function (condition) {
                            queryInput.addCondition(condition);
                        });

                        var annotationObjectList = [];
                        var annotationStringList = [];
                        var annotationNodes = $('#annotation-div').jstree(true)._model.data['#'].children;
                        self.formUtils.buildAnnotation(annotationNodes, annotationStringList, annotationObjectList);
                        self.formUtils.buildPredefinedAnnotations(predefinedAnnotations, annotationStringList,
                            annotationObjectList);
                        clickedElement.clearAnnotationList();
                        clickedElement.clearAnnotationListObjects();
                        //add the annotations to the clicked element
                        _.forEach(annotationStringList, function (annotation) {
                            clickedElement.addAnnotation(annotation);
                        });
                        _.forEach(annotationObjectList, function (annotation) {
                            clickedElement.addAnnotationObject(annotation);
                        });

                        var outputTarget = $('.query-into').val().trim()
                        var outputConfig = {};
                        _.set(outputConfig, 'eventType', $('#event-type').val());
                        var outputObject = new QueryOutputInsert(outputConfig);
                        queryOutput.setOutput(outputObject);
                        queryOutput.setTarget(outputTarget);
                        queryOutput.setType(Constants.INSERT);

                        var isValid = JSONValidator.prototype.validatePatternOrSequenceQuery(clickedElement,
                            Constants.PATTERN_QUERY, false);
                        if (!isValid) {
                            isErrorOccurred = true;
                            return;
                        }

                        $('#' + id).removeClass('incomplete-element');
                        self.configurationData.setIsDesignViewContentChanged(true);
                        self.designViewContainer.removeClass('disableContainer');
                        self.toggleViewButton.removeClass('disableContainer');

                        //Send pattern-query element to the backend and generate tooltip
                        var queryToolTip = self.formUtils.getTooltip(clickedElement, Constants.PATTERN_QUERY);
                        $('#' + id).prop('title', queryToolTip);
                        var textNode = $('#' + id).find('.patternQueryNameNode');
                        textNode.html(queryName);

                        // close the form window
                        self.consoleListManager.removeFormConsole(formConsole);
                    }
                })

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

        return PatternQueryForm;
    });
