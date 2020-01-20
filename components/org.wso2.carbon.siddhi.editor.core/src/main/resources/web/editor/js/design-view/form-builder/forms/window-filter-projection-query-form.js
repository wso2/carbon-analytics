/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'log', 'jquery', 'lodash', 'querySelect', 'queryWindowOrFunction', 'queryOrderByValue',
        'streamHandler', 'designViewUtils', 'jsonValidator', 'constants', 'handlebar'],
    function (require, log, $, _, QuerySelect, QueryWindowOrFunction, QueryOrderByValue, StreamHandler, DesignViewUtils,
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
         * @function generate the form for the simple queries (projection, filter and window)
         * @param element selected element(query)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        WindowFilterProjectionQueryForm.prototype.generatePropertiesForm = function (element, formConsole,
                                                                                     formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');
            var queryObject = self.configurationData.getSiddhiAppConfig().getWindowFilterProjectionQuery(id);

            if (!queryObject.getQueryInput() || !queryObject.getQueryInput().getConnectedSource()) {
                DesignViewUtils.prototype.warnAlert('Connect an input element');
                self.consoleListManager.removeFormConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            } else if (!queryObject.getQueryOutput() || !queryObject.getQueryOutput().getTarget()) {
                DesignViewUtils.prototype.warnAlert('Connect an output stream');
                self.consoleListManager.removeFormConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            } else {
                var propertyDiv = $('<div id="define-windowFilterProjection-query" class="clearfix form-min-width"></div>');
                formContainer.html(propertyDiv);
                self.formUtils.buildFormButtons(formConsole.cid);

                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');
                self.formUtils.popUpSelectedElement(id);

                var queryName = queryObject.getQueryName();
                var queryInput = queryObject.getQueryInput();
                var inputElementName = queryInput.getConnectedSource();
                var groupBy = queryObject.getGroupBy();
                var having = queryObject.getHaving();
                var orderBy = queryObject.getOrderBy();
                var limit = queryObject.getLimit();
                var offset = queryObject.getOffset();
                var select = queryObject.getSelect();
                var outputRateLimit = queryObject.getOutputRateLimit();
                var queryOutput = queryObject.getQueryOutput();
                var outputElementName = queryOutput.getTarget();
                var streamHandlerList = queryInput.getStreamHandlerList();
                var annotationListObjects = queryObject.getAnnotationListObjects();

                var partitionId;
                var partitionElementWhereQueryIsSaved
                    = self.configurationData.getSiddhiAppConfig().getPartitionWhereQueryIsSaved(id);
                if (partitionElementWhereQueryIsSaved !== undefined) {
                    partitionId = partitionElementWhereQueryIsSaved.getId();
                }
                var outputElement = self.configurationData.getSiddhiAppConfig()
                    .getDefinitionElementByName(outputElementName, partitionId);

                var predefinedAnnotations = _.cloneDeep(self.configurationData.application.config.type_query_predefined_annotations);
                var streamHandlerTypes = self.configurationData.application.config.stream_handler_types;

                //render the query form template
                var queryFormTemplate = Handlebars.compile($('#window-filter-projection-query-form-template').html())
                ({name: queryName, from: inputElementName});
                $('#define-windowFilterProjection-query').html(queryFormTemplate);
                self.formUtils.renderQueryOutput(outputElement, queryOutput);
                self.formUtils.renderOutputEventTypes();
                self.formUtils.addEventListenerForQueryOutputDiv();
                self.formUtils.addEventListenerToRemoveRequiredClass();
                self.formUtils.addEventListenerToShowAndHideInfo();
                self.formUtils.addEventListenerToShowInputContentOnHover();

                $('.query-form-container').on('change', '.query-checkbox', function () {
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

                var possibleAttributes = [];
                _.forEach(self.formUtils.getInputAttributes([inputElementName]), function (attribute) {
                    possibleAttributes.push(attribute.name);
                });

                var outputAttributes = [];
                if (outputElement.type.toLowerCase() === Constants.STREAM ||
                    outputElement.type.toLowerCase() === Constants.TABLE) {
                    var streamAttributes = outputElement.element.getAttributeList();
                    _.forEach(streamAttributes, function (attribute) {
                        outputAttributes.push(attribute.getName());
                    });
                }

                self.formUtils.generateGroupByDiv(groupBy, possibleAttributes);
                self.formUtils.generateOrderByDiv(orderBy, outputAttributes);

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

                if (outputRateLimit) {
                    $('.rate-limiting-value').val(outputRateLimit);
                    $(".rate-limiting-checkbox").prop("checked", true);
                } else {
                    $('.rate-limiting-content').hide();
                }

                if (offset) {
                    $('.offset-value').val(offset);
                    $(".offset-checkbox").prop("checked", true);
                } else {
                    $('.offset-content').hide();
                }


                /**
                 * if stream-handlers are empty, add a stream handler according to the selected query
                 * [window/filter/projection] else use the saved stream handler
                 */
                if (streamHandlerList && streamHandlerList.length == 0) {
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
                self.formUtils.addEventListenersForStreamHandlersDiv(streamHandlerList, possibleAttributes);

                /**
                 * to show user the lost saved data when the connection is deleted/ when the connected stream is modified
                 * only if the form is an already edited form
                 */
                if (queryOutput && queryOutput.type) {
                    validateSectionsOnLoadOfForm(self);
                }

                //autocompletion
                var outputAttributesWithElementName = self.formUtils.constructOutputAttributes(outputAttributes);
                self.formUtils.addAutoCompleteForSelectExpressions(possibleAttributes);
                self.formUtils.addAutoCompleteForFilterConditions(possibleAttributes);
                self.formUtils.addAutoCompleteForStreamWindowFunctionAttributes(possibleAttributes);
                self.formUtils.addAutoCompleteForOutputOperation(outputAttributesWithElementName, possibleAttributes);
                self.formUtils.addAutoCompleteForHavingCondition(possibleAttributes.concat(outputAttributes));
                self.formUtils.addAutoCompleteForOnCondition(possibleAttributes.concat(outputAttributes),
                    [inputElementName]);
                self.formUtils.addAutoCompleteForRateLimits();

                //to add query operation set
                var setDiv = '<li class="setAttributeValue">' +
                    '<div class="clearfix">' +
                    '<input type="text" class="setAttribute"> <input type="text" class="setValue"> ' +
                    '<a class = "btn-del-option"> <i class = "fw fw-delete"> </i> </a>' +
                    '</div> <label class="error-message"> </label> </li>'
                $('.define-operation-set-condition').on('click', '.btn-add-set', function () {
                    $('.define-operation-set-condition .set-condition').append(setDiv);
                    self.formUtils.addAutoCompleteForOutputOperation(outputAttributesWithElementName, possibleAttributes);
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

                    if (validateSectionsOnLoadOfForm(self)) {
                        isErrorOccurred = true;
                        return;
                    }

                    if (self.formUtils.validatePredefinedAnnotations(predefinedAnnotations)) {
                        isErrorOccurred = true;
                        return;
                    }

                    if (self.formUtils.validateStreamHandlers($('.define-query-source'))) {
                        isErrorOccurred = true;
                    }

                    if (!isErrorOccurred) {
                        if (queryName != "") {
                            queryObject.addQueryName(queryName);
                        } else {
                            queryName = "Query";
                            queryObject.addQueryName('query');
                        }

                        if ($('.group-by-checkbox').is(':checked')) {
                            var groupByAttributes = self.formUtils.buildGroupBy();
                            queryObject.setGroupBy(groupByAttributes);
                        } else {
                            queryObject.setGroupBy(undefined);
                        }

                        queryObject.clearOrderByValueList()
                        if ($('.order-by-checkbox').is(':checked')) {
                            var orderByAttributes = self.formUtils.buildOrderBy();
                            _.forEach(orderByAttributes, function (attribute) {
                                var orderByValueObject = new QueryOrderByValue(attribute);
                                queryObject.addOrderByValue(orderByValueObject);
                            });
                        }

                        if ($('.having-checkbox').is(':checked')) {
                            queryObject.setHaving($('.having-value').val().trim());
                        } else {
                            queryObject.setHaving(undefined)
                        }

                        if ($('.limit-checkbox').is(':checked')) {
                            queryObject.setLimit($('.limit-value').val().trim())
                        } else {
                            queryObject.setLimit(undefined)
                        }

                        if ($('.offset-checkbox').is(':checked')) {
                            queryObject.setOffset($('.offset-value').val().trim())
                        } else {
                            queryObject.setOffset(undefined)
                        }

                        if ($('.rate-limiting-checkbox').is(':checked')) {
                            queryObject.setOutputRateLimit($('.rate-limiting-value').val().trim())
                        } else {
                            queryObject.setOutputRateLimit(undefined)
                        }

                        var selectObject = new QuerySelect(self.formUtils.buildAttributeSelection(Constants.JOIN_QUERY));
                        queryObject.setSelect(selectObject);

                        var annotationObjectList = [];
                        var annotationStringList = [];
                        var annotationNodes = $('#annotation-div').jstree(true)._model.data['#'].children;
                        self.formUtils.buildAnnotation(annotationNodes, annotationStringList, annotationObjectList);
                        self.formUtils.buildPredefinedAnnotations(predefinedAnnotations, annotationStringList,
                            annotationObjectList);
                        queryObject.clearAnnotationList();
                        queryObject.clearAnnotationListObjects();
                        //add the annotations to the clicked element
                        _.forEach(annotationStringList, function (annotation) {
                            queryObject.addAnnotation(annotation);
                        });
                        _.forEach(annotationObjectList, function (annotation) {
                            queryObject.addAnnotationObject(annotation);
                        });

                        queryObject.getQueryInput().clearStreamHandlerList();
                        var streamHandlers = self.formUtils.buildStreamHandlers($('.define-stream-handler'));
                        _.forEach(streamHandlers, function (streamHandlerOption) {
                            queryInput.addStreamHandler(streamHandlerOption);
                        });

                        self.formUtils.buildQueryOutput(outputElement, queryOutput);

                        /**
                         * This is to change the icon of the query depending on the selected stream handlers
                         */
                        var noOfSavedFilters = 0;
                        var noOfSavedWindows = 0;
                        var noOfSavedFunctions = 0;
                        var newStreamHandlers = queryObject.getQueryInput().getStreamHandlerList();
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

                        JSONValidator.prototype.validateWindowFilterProjectionQuery(queryObject);
                        self.configurationData.setIsDesignViewContentChanged(true);

                        //Send query element to the backend and generate tooltip
                        var queryToolTip = self.formUtils.getTooltip(queryObject,
                            Constants.WINDOW_FILTER_PROJECTION_QUERY);
                        $('#' + id).prop('title', queryToolTip);
                        var textNode = $('#' + queryObject.getId()).find('.queryNameNode');
                        textNode.html(queryName);

                        // close the form window
                        self.consoleListManager.removeFormConsole(formConsole);
                        if (self.application.browserStorage.get("isWidgetFromTourGuide")) {
                            self.consoleListManager.removeAllConsoles();
                        }
                    }
                });

                // 'Cancel' button action
                $('#' + formConsole.cid).on('click', '#btn-cancel', function () {
                    // close the form
                    self.consoleListManager.removeFormConsole(formConsole);
                });
            }
        };

        return WindowFilterProjectionQueryForm;
    });
