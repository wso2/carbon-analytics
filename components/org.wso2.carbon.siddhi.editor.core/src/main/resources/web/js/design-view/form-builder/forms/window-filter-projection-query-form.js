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

define(['require', 'log', 'jquery', 'lodash', 'querySelect', 'queryOutputInsert', 'queryOutputDelete',
    'queryOutputUpdate', 'queryOutputUpdateOrInsertInto', 'queryWindowOrFunction', 'queryOrderByValue',
    'streamHandler', 'designViewUtils', 'jsonValidator', 'constants', 'handlebar'],
    function (require, log, $, _, QuerySelect, QueryOutputInsert, QueryOutputDelete, QueryOutputUpdate,
        QueryOutputUpdateOrInsertInto, QueryWindowOrFunction, QueryOrderByValue, StreamHandler, DesignViewUtils,
        JSONValidator, Constants, Handlebars) {

        /**
         * @class WindowFilterProjectionQueryForm Creates a forms to collect data from a window/filter/projection query
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var WindowFilterProjectionQueryForm = function (options) {
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

		/**
		 * @function to add autocompletion for filter value
		 */
        var addAutoCompletionForFilter = function (self, QUERY_CONDITION_SYNTAX, possibleAttributes) {
            var filterMatches = JSON.parse(JSON.stringify(possibleAttributes));
            filterMatches = filterMatches.concat(QUERY_CONDITION_SYNTAX);
            self.formUtils.createAutocomplete($('.symbol-syntax-required-value'), filterMatches);
        };

        /**
         * @function generate the form for the simple queries (projection, filter and window)
         * @param element selected element(query)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        WindowFilterProjectionQueryForm.prototype.generatePropertiesForm = function (element, formConsole,
            formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');
            var clickedElement = self.configurationData.getSiddhiAppConfig().getWindowFilterProjectionQuery(id);

            if (!clickedElement.getQueryInput() || !clickedElement.getQueryInput().getFrom()) {
                DesignViewUtils.prototype.warnAlert('Connect an input element');
                self.consoleListManager.removeFormConsole(formConsole);
            } else if (!clickedElement.getQueryOutput() || !clickedElement.getQueryOutput().getTarget()) {
                DesignViewUtils.prototype.warnAlert('Connect an output stream');
                self.consoleListManager.removeFormConsole(formConsole);
            } else {
                var propertyDiv = $('<div id="property-header"><h3> Query Configuration </h3></div>' +
                    '<div id="define-windowFilterProjection-query"></div>' + self.formUtils.buildFormButtons());
                formContainer.append(propertyDiv);

                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');
                self.formUtils.popUpSelectedElement(id);

                const QUERY_CONDITION_SYNTAX = self.configurationData.application.config.query_condition_syntax;
                const RATE_LIMITING_SYNTAX = self.configurationData.application.config.other_query_syntax;

                var queryName = clickedElement.getQueryName();
                var queryInput = clickedElement.getQueryInput();
                var inputElementName = queryInput.getFrom();
                var groupBy = clickedElement.getGroupBy();
                var having = clickedElement.getHaving();
                var orderBy = clickedElement.getOrderBy();
                var limit = clickedElement.getLimit();
                var select = clickedElement.getSelect();
                var outputRateLimit = clickedElement.getOutputRateLimit();
                var queryOutput = clickedElement.getQueryOutput();
                var outputElementName = queryOutput.getTarget();
                var savedStreamHandlerList = queryInput.getStreamHandlerList();
                var savedAnnotations = clickedElement.getAnnotationListObjects();

                var predefinedAnnotations = JSON.parse(JSON.stringify(self.configurationData.application.config.
                    query_predefined_annotations));
                var streamHandlerTypes = self.configurationData.application.config.stream_handler_types;
                var incrementalAggregator = self.configurationData.application.config.incremental_aggregator;

                //render the query form template
                var queryFormTemplate = Handlebars.compile($('#window-filter-projection-query-form-template').html());
                var wrappedHtml = queryFormTemplate({ name: queryName, from: inputElementName });
                $('#define-windowFilterProjection-query').html(wrappedHtml);
                self.formUtils.renderQueryOutput(outputElementName);
                self.formUtils.renderOutputEventTypes();

                self.formUtils.addEventListenerToRemoveRequiredClass();

                $('.query-form-container').on('change', '.query-checkbox', function () {
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

                var partitionId;
                var partitionElementWhereQueryIsSaved
                    = self.configurationData.getSiddhiAppConfig().getPartitionWhereQueryIsSaved(id);
                if (partitionElementWhereQueryIsSaved !== undefined) {
                    partitionId = partitionElementWhereQueryIsSaved.getId();
                }

                var possibleAttributes = [];
                var inputElement = self.configurationData.getSiddhiAppConfig()
                    .getDefinitionElementByName(inputElementName, partitionId);
                if (inputElement.type.toLowerCase() === Constants.STREAM) {
                    var streamAttributes = inputElement.element.getAttributeList();
                    _.forEach(streamAttributes, function (attribute) {
                        possibleAttributes.push(attribute.getName());
                    });
                } else if (inputElement.type.toLowerCase() === Constants.TRIGGER) {
                    possibleAttributes.push(Constants.TRIGGERED_TIME);
                }

                self.formUtils.generateGroupByDiv(groupBy, possibleAttributes);
                self.formUtils.generateOrderByDiv(orderBy, possibleAttributes);

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

                /**
                 * if stream-handlers are empty, add a stream handler according to the selected query
                 * [window/filter/projection] else use the saved stream handler
                 */
                if (savedStreamHandlerList && savedStreamHandlerList.length == 0) {
                    var streamHandler;
                    var parent = $(element).parent();
                    if (parent.hasClass(Constants.FUNCTION_QUERY_DROP)) {
                        streamHandler = self.formUtils.createEmptyStreamHandler(Constants.FUNCTION)
                    } else if (parent.hasClass(Constants.WINDOW_QUERY_DROP)) {
                        streamHandler = self.formUtils.createEmptyStreamHandler(Constants.WINDOW)
                    } else if (parent.hasClass(Constants.FILTER_QUERY_DROP)) {
                        streamHandler = self.formUtils.createEmptyStreamHandler(Constants.FILTER)
                    }

                    if (parent.hasClass(Constants.FUNCTION_QUERY_DROP) || parent.hasClass(Constants.WINDOW_QUERY_DROP)
                        || parent.hasClass(Constants.FILTER_QUERY_DROP)) {
                        var streamHandlerObject = new StreamHandler(streamHandler);
                        queryInput.addStreamHandler(streamHandlerObject);
                    }
                }
                self.formUtils.renderStreamHandler("query", queryInput, streamHandlerTypes);
                self.formUtils.mapStreamHandler(queryInput, "query");
                self.formUtils.addEventListenersForStreamHandlersDiv(savedStreamHandlerList);

                //autocompletion
                var streamFunctions = self.formUtils.getStreamFunctionNames();
                var selectExpressionMatches = JSON.parse(JSON.stringify(possibleAttributes));
                selectExpressionMatches = selectExpressionMatches.concat(incrementalAggregator);
                selectExpressionMatches = selectExpressionMatches.concat(streamFunctions);
                self.formUtils.createAutocomplete($('.attribute-expression'), selectExpressionMatches);

                addAutoCompletionForFilter(self, QUERY_CONDITION_SYNTAX, possibleAttributes);

                //to add filter
                $('.define-stream-handler').on('click', '.btn-add-filter', function () {
                    var sourceDiv = self.formUtils.getSourceDiv($(this));
                    self.formUtils.addNewStreamHandler(sourceDiv, Constants.FILTER);
                    addAutoCompletionForFilter(self, QUERY_CONDITION_SYNTAX, possibleAttributes);
                });

                var rateLimitingMatches = RATE_LIMITING_SYNTAX.concat(Constants.SIDDHI_TIME);
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

                    if (self.formUtils.validatePredefinedAnnotations(predefinedAnnotations)) {
                        isErrorOccurred = true;
                        return;
                    }

                    if (self.formUtils.validateQueryProjection()) {
                        isErrorOccurred = true;
                        return;
                    }

                    if (self.formUtils.validateStreamHandlers($('.define-query-source'))) {
                        isErrorOccurred = true;
                    }

                    if (!isErrorOccurred) {
                        if (queryName != "") {
							clickedElement.addQueryName(queryName);
						} else {
							queryName = "Query";
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

                        var selectObject = new QuerySelect(self.formUtils.buildAttributeSelection(Constants.JOIN_QUERY));
                        clickedElement.setSelect(selectObject);

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

                        clickedElement.getQueryInput().clearStreamHandlerList();
                        var streamHandlers = self.formUtils.buildStreamHandlers($('.define-stream-handler'));
                        _.forEach(streamHandlers, function (streamHandlerOption) {
                            queryInput.addStreamHandler(streamHandlerOption);
                        });

                        var outputTarget = $('.query-into').val().trim()
                        var outputConfig = {};
                        _.set(outputConfig, 'eventType', $('#event-type').val());
                        var outputObject = new QueryOutputInsert(outputConfig);
                        queryOutput.setOutput(outputObject);
                        queryOutput.setTarget(outputTarget);
                        queryOutput.setType(Constants.INSERT);

                        var isValid = JSONValidator.prototype.validateWindowFilterProjectionQuery(clickedElement, false);
                        if (!isValid) {
                            isErrorOccurred = true;
                            return;
                        }

                        /**
                         * This is to change the icon of the query depending on the selected stream handlers
                         */
                        var noOfSavedFilters = 0;
                        var noOfSavedWindows = 0;
                        var noOfSavedFunctions = 0;
                        var newStreamHandlers = clickedElement.getQueryInput().getStreamHandlerList();
                        _.forEach(newStreamHandlers, function (streamHandler) {
                            if (streamHandler.getType().toLowerCase() == Constants.FILTER) {
                                noOfSavedFilters++;
                            } else if (streamHandler.getType().toLowerCase() == Constants.WINDOW) {
                                noOfSavedWindows++;
                            } else if (streamHandler.getType().toLowerCase() == Constants.FUNCTION) {
                                noOfSavedFunctions++;
                            }
                        });

                        var queryType;
                        if (noOfSavedFunctions > 0) {
                            queryType = Constants.FUNCTION;
                            $(element).parent().removeClass();
                            $(element).parent().addClass(Constants.FUNCTION_QUERY_DROP + ' jtk-draggable');
                        } else if (noOfSavedWindows === 1) {
                            queryType = Constants.WINDOW;
                            $(element).parent().removeClass();
                            $(element).parent().addClass(Constants.WINDOW_QUERY_DROP + ' jtk-draggable');
                        } else if (noOfSavedFilters > 0) {
                            queryType = Constants.FILTER;
                            $(element).parent().removeClass();
                            $(element).parent().addClass(Constants.FILTER_QUERY_DROP + ' jtk-draggable');
                        } else {
                            queryType = Constants.PROJECTION;
                            $(element).parent().removeClass();
                            $(element).parent().addClass(Constants.PROJECTION_QUERY_DROP + ' jtk-draggable');
                        }
                        queryInput.setType(queryType.toUpperCase());

                        $('#' + id).removeClass('incomplete-element');
                        self.configurationData.setIsDesignViewContentChanged(true);
                        self.designViewContainer.removeClass('disableContainer');
                        self.toggleViewButton.removeClass('disableContainer');

                        //Send query element to the backend and generate tooltip
                        var queryToolTip = self.formUtils.getTooltip(clickedElement,
                            Constants.WINDOW_FILTER_PROJECTION_QUERY);
                        $('#' + id).prop('title', queryToolTip);
                        var textNode = $('#' + clickedElement.getId()).find('.queryNameNode');
                        textNode.html(queryName);

                        // close the form window
                        self.consoleListManager.removeFormConsole(formConsole);
                    }
                });

                // 'Cancel' button action
                var cancelButtonElement = $(formContainer).find('#btn-cancel')[0];
                cancelButtonElement.addEventListener('click', function () {
                    // design view container and toggle view button are enabled
                    self.designViewContainer.removeClass('disableContainer');
                    self.toggleViewButton.removeClass('disableContainer');

                    // close the form
                    self.consoleListManager.removeFormConsole(formConsole);
                });
            }
        };

        return WindowFilterProjectionQueryForm;
    });
