/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'log', 'jquery', 'lodash', 'querySelect', 'queryWindowOrFunction', 'queryOrderByValue',
        'joinQuerySource', 'streamHandler', 'designViewUtils', 'jsonValidator', 'constants', 'handlebar'],
    function (require, log, $, _, QuerySelect, QueryWindowOrFunction, QueryOrderByValue, joinQuerySource, StreamHandler,
              DesignViewUtils, JSONValidator, Constants, Handlebars) {

        /**
         * @class JoinQueryForm Creates a forms to collect data from a join query
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var JoinQueryForm = function (options) {
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
         * @function to construct the possible attributes with defined as of the source
         */
        var constructPossibleAttributes = function (attributeList, connectedElementName, possibleAttributes) {
            _.forEach(attributeList, function (attribute) {
                possibleAttributes.push(connectedElementName + "." + attribute.name);
            });
        };

        /**
         * @function to map the source-as value of the saved source data
         * @param {Object} sourceData saved source data
         * @param {String} type left or right
         */
        var mapSourceAs = function (sourceData, type) {
            var sourceDiv = $('.define-' + type + '-source');
            if (sourceData && sourceData.getAs()) {
                sourceDiv.find('.as-content-value').val(sourceData.getAs());
                sourceDiv.find('.source-as-checkbox').prop('checked', true);
            } else {
                sourceDiv.find('.source-as-content').hide()
            }
        };

        /**
         * @function to map the uni directional checkbox
         * @param {Object} sourceData saved source data
         * @param {String} type left or right
         */
        var mapUnidirectionalCheckbox = function (sourceData, type) {
            if (sourceData) {
                $('.define-' + type + '-source').find('.is-unidirectional-checkbox').prop('checked', sourceData
                    .getIsUnidirectional())
            }
        };

        /**
         *
         * @param {String} type left or right
         * @param {String} savedSourceType saved type (element name) of the source
         * @param {boolean} equalityCheck to choose the given value or to choose the opp value
         */
        var mapSourceType = function (type, savedSourceType, equalityCheck) {
            if (equalityCheck) {
                $('.define-' + type + '-source').find('.source-selection option').filter(function () {
                    return ($(this).val() == savedSourceType);
                }).prop('selected', true);
            } else {
                $('.define-' + type + '-source').find('.source-selection option').filter(function () {
                    return ($(this).val() != savedSourceType);
                }).prop('selected', true);
            }
        };

        /**
         * @function to validate the left and right source
         */
        var validateSource = function (type, self) {
            var isErrorOccurred = false;
            var sourceDiv = $('.define-' + type + '-source');
            if (self.formUtils.validateStreamHandlers(sourceDiv)) {
                self.formUtils.expandCollapsedDiv(sourceDiv);
                isErrorOccurred = true;
            }
            return isErrorOccurred;
        };

        /**
         * @function to get the defined source-as in a join query
         * @param {String} type left or right source
         */
        var getSourceAs = function (type) {
            if ($('.define-' + type + '-source').find('.source-as-checkbox').is(':checked')) {
                return $('.define-' + type + '-source').find('.as-content-value').val().trim();
            } else {
                return $('.define-' + type + '-source').find('.source-selection').val().trim();
            }
        };

        /**
         * @function to build the left and right source
         */
        var buildSource = function (type, self) {
            var sourceOptions = {};
            var sourceDiv = $('.define-' + type + '-source');
            var sourceFrom = sourceDiv.find('.source-selection').val();
            var sourceElementType = self.configurationData.getSiddhiAppConfig().getDefinitionElementByName(sourceFrom).type;
            _.set(sourceOptions, 'type', sourceElementType);
            _.set(sourceOptions, 'from', sourceFrom);
            if (sourceDiv.find('.source-as-checkbox').is(':checked')) {
                _.set(sourceOptions, 'as', sourceDiv.find('.as-content-value').val().trim());
            } else {
                _.set(sourceOptions, 'as', undefined)
            }
            if (sourceDiv.find('.is-unidirectional-checkbox').is(':checked')) {
                _.set(sourceOptions, 'isUnidirectional', true);
            } else {
                _.set(sourceOptions, 'isUnidirectional', false);
            }
            var joinSource = new joinQuerySource(sourceOptions);
            var streamHandlers = self.formUtils.buildStreamHandlers(sourceDiv.find('.define-stream-handler'));
            _.forEach(streamHandlers, function (streamHandlerOption) {
                joinSource.addStreamHandler(streamHandlerOption);
            });
            return joinSource;
        };

        /**
         * @function to render per and within input fields if the connected element is aggregation
         */
        var renderPerAndWithin = function () {
            var perDiv = '<div class="define-per-condition define-content">' +
                '<label> <label class="mandatory-symbol"> * </label> Per Condition </label> ' +
                '<div class="per-condition-content query-content">' +
                '<input type="text"  class="clearfix per-condition-value per-within name">' +
                '<label class="error-message"> </label> </div> </div>';
            var withinDiv = '<div class="define-within-condition define-content">' +
                '<label> <label class="mandatory-symbol"> * </label> Within Condition </label> ' +
                '<div class="within-condition-content query-content">' +
                '<input type="text"  class="clearfix within-condition-value per-within name">' +
                '<label class="error-message"> </label> </div> </div>';
            $('.define-aggregate-per-within-condition').append(perDiv);
            $('.define-aggregate-per-within-condition').append(withinDiv);
        };

        /**
         * @function to get the stream handler objects
         */
        var getStreamHandlers = function (savedData, streamHandlerList) {
            if (savedData && savedData.getStreamHandlerList() && savedData.getStreamHandlerList().length != 0) {
                _.forEach(savedData.getStreamHandlerList(), function (streamHandler) {
                    streamHandlerList.push(streamHandler);
                });
            }
        };

        /**
         * @function to obtain the possible attributes with source as value
         * eg <source-as>.attributeName
         */
        var getPossibleAttributesWithSourceAs = function (self) {
            var possibleAttributesWithSourceAs = [];
            var firstElementAttributes = self.formUtils.getInputAttributes
            ([$('.define-left-source').find('.source-selection').val()])
            var secondElementAttributes = self.formUtils.getInputAttributes
            ([$('.define-right-source').find('.source-selection').val()]);
            constructPossibleAttributes(firstElementAttributes,
                getSourceAs(Constants.LEFT), possibleAttributesWithSourceAs)
            constructPossibleAttributes(secondElementAttributes,
                getSourceAs(Constants.RIGHT), possibleAttributesWithSourceAs)
            return possibleAttributesWithSourceAs;
        };

        /**
         * @function add autocomplete for fields with changing attributes
         */
        var autoCompleteFieldsWithChangingAttributes = function (self, outputAttributes) {
            var inputSources = getLeftRightSourceNames();
            var possibleAttributesWithSourceAs = getPossibleAttributesWithSourceAs(self);
            self.formUtils.addAutoCompleteForSelectExpressions(possibleAttributesWithSourceAs);
            self.formUtils.addAutoCompleteForOnCondition(possibleAttributesWithSourceAs.concat(outputAttributes), inputSources);
            self.formUtils.addAutoCompleteForPerWithinConditions(possibleAttributesWithSourceAs.concat(outputAttributes));
            self.formUtils.addAutoCompleteForHavingCondition(possibleAttributesWithSourceAs.concat(outputAttributes));
            self.formUtils.addAutoCompleteForOutputOperation(outputAttributes, possibleAttributesWithSourceAs);
        };

        /**
         * @function get the names of the connected source
         */
        var getLeftRightSourceNames = function () {
            var inputNames = [];
            $('.define-source').each(function () {
                var checkbox = $(this).find('.source-as-checkbox');
                var asValue = $(this).find('.as-content-value').val().trim();
                if (checkbox.is(':checked') && asValue !== "") {
                    inputNames.push(asValue)
                } else {
                    inputNames.push($(this).find('.source-selection').val())
                }
            });
            return inputNames;
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
            if (!$('.join-selection').val()) {
                self.formUtils.addErrorClass('.define-join-type-selection')
                isErrorOccurred = true;
            }
            if (self.formUtils.validateQueryOutputSet()) {
                isErrorOccurred = true;
            }
            return isErrorOccurred;
        };

        /**
         * @function generate the form for the join query
         * @param element selected element(query)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        JoinQueryForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');
            var joinQueryObject = self.configurationData.getSiddhiAppConfig().getJoinQuery(id);

            var inValid = false;
            if (!joinQueryObject.getQueryInput()
                || !joinQueryObject.getQueryInput().getFirstConnectedElement()
                || !joinQueryObject.getQueryInput().getSecondConnectedElement()) {
                DesignViewUtils.prototype.warnAlert('Connect two input elements to join query');
                inValid = true;

            } else if ((joinQueryObject.getQueryInput().getFirstConnectedElement() && !joinQueryObject.getQueryInput()
                    .getFirstConnectedElement().name)
                || (joinQueryObject.getQueryInput().getSecondConnectedElement() && !joinQueryObject.getQueryInput()
                    .getSecondConnectedElement().name)) {
                DesignViewUtils.prototype.warnAlert('Fill the connected element to join query');
                inValid = true;

            } else if (!joinQueryObject.getQueryOutput() || !joinQueryObject.getQueryOutput().getTarget()) {
                DesignViewUtils.prototype.warnAlert('Connect an output element');
                inValid = true;
            }
            if (inValid) {
                self.consoleListManager.removeFormConsole(formConsole);
                self.consoleListManager.removeAllConsoles();
            } else {
                var propertyDiv = $('<div id="define-join-query" class="clearfix form-min-width"></div>');
                formContainer.html(propertyDiv);
                self.formUtils.buildFormButtons(formConsole.cid);

                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');
                self.formUtils.popUpSelectedElement(id);

                var firstConnectedElement = joinQueryObject.getQueryInput().getFirstConnectedElement();
                var secondConnectedElement = joinQueryObject.getQueryInput().getSecondConnectedElement();
                var leftSourceData = joinQueryObject.getQueryInput().getLeft();
                var rightSourceData = joinQueryObject.getQueryInput().getRight();
                var joinType = joinQueryObject.getQueryInput().getJoinType();
                var queryName = joinQueryObject.getQueryName();
                var on = joinQueryObject.getQueryInput().getOn();
                var within = joinQueryObject.getQueryInput().getWithin();
                var per = joinQueryObject.getQueryInput().getPer();
                var groupBy = joinQueryObject.getGroupBy();
                var having = joinQueryObject.getHaving();
                var orderBy = joinQueryObject.getOrderBy();
                var limit = joinQueryObject.getLimit();
                var offset = joinQueryObject.getOffset();
                var outputRateLimit = joinQueryObject.getOutputRateLimit();
                var outputElementName = joinQueryObject.getQueryOutput().getTarget();
                var queryInput = joinQueryObject.getQueryInput();
                var queryOutput = joinQueryObject.getQueryOutput();
                var select = joinQueryObject.getSelect();
                var annotationListObjects = joinQueryObject.getAnnotationListObjects();

                var partitionId;
                var partitionElementWhereQueryIsSaved
                    = self.configurationData.getSiddhiAppConfig().getPartitionWhereQueryIsSaved(id);
                if (partitionElementWhereQueryIsSaved !== undefined) {
                    partitionId = partitionElementWhereQueryIsSaved.getId();
                }
                var outputElement = self.configurationData.getSiddhiAppConfig()
                    .getDefinitionElementByName(outputElementName, partitionId);

                var possibleJoinTypes = self.configurationData.application.config.join_types;
                var predefinedAnnotations = _.cloneDeep(self.configurationData.application.config.type_query_predefined_annotations);
                var streamHandlerTypes = self.configurationData.application.config.stream_handler_types;

                //render the join-query form template
                var joinFormTemplate = Handlebars.compile($('#join-query-form-template').html())({name: queryName});
                $('#define-join-query').html(joinFormTemplate);
                self.formUtils.renderQueryOutput(outputElement, queryOutput);
                self.formUtils.renderOutputEventTypes();

                self.formUtils.addEventListenerForQueryOutputDiv();
                self.formUtils.addEventListenerToRemoveRequiredClass();
                self.formUtils.addEventListenerToShowAndHideInfo();
                self.formUtils.addEventListenerToShowInputContentOnHover();

                //annotations
                predefinedAnnotations = self.formUtils.createObjectsForAnnotationsWithKeys(predefinedAnnotations);
                var userDefinedAnnotations = self.formUtils.getUserAnnotations(annotationListObjects,
                    predefinedAnnotations);
                self.formUtils.renderAnnotationTemplate("define-user-defined-annotations", userDefinedAnnotations);
                $('.define-user-defined-annotations').find('label:first').html('Customized Annotations');
                self.formUtils.mapPredefinedAnnotations(annotationListObjects, predefinedAnnotations);
                self.formUtils.renderPredefinedAnnotations(predefinedAnnotations,
                    'define-predefined-annotations');
                self.formUtils.renderOptionsForPredefinedAnnotations(predefinedAnnotations);
                self.formUtils.addEventListenersForPredefinedAnnotations();

                //event listeners
                $('.join-query-form-container').on('change', '.source-selection', function () {
                    var currentValue = this.value;
                    $(this).find('option').filter(function () {
                        return ($(this).val() != currentValue);
                    }).prop('selected', true);

                    var leftSource = $('.left-source-content').contents();
                    var rightSource = $('.right-source-content').contents();
                    //exchange the div
                    $('.left-source-content').html(rightSource);
                    $('.right-source-content').html(leftSource);
                });

                $('.join-query-form-container').on('change', '.query-checkbox', function () {
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

                $('.join-query-form-container').on('change', '.join-selection', function () {
                    $('.join-type-div').find('.error-message').hide();
                });

                //join-type
                self.formUtils.renderDropDown('.define-join-type-selection', possibleJoinTypes, Constants.JOIN);
                if (joinType) {
                    $('.define-join-type-selection').find('.join-selection option').filter(function () {
                        return ($(this).val() == joinType.toLowerCase());
                    }).prop('selected', true);
                }

                var eventType = Constants.CURRENT_EVENTS;
                if (queryOutput.output && queryOutput.output.eventType) {
                    eventType = queryOutput.output.eventType.toLowerCase();
                }
                $('.define-output-events').find('#event-type option').filter(function () {
                    return ($(this).val() == eventType);
                }).prop('selected', true);

                var possibleSources = [];
                possibleSources.push(firstConnectedElement.name);
                possibleSources.push(secondConnectedElement.name);
                self.formUtils.renderLeftRightSource(Constants.LEFT);
                self.formUtils.renderLeftRightSource(Constants.RIGHT);
                self.formUtils.renderStreamHandler(Constants.LEFT, leftSourceData, streamHandlerTypes);
                self.formUtils.renderStreamHandler(Constants.RIGHT, rightSourceData, streamHandlerTypes);
                self.formUtils.renderDropDown('.input-from-drop-down', possibleSources, Constants.SOURCE);

                var inputAttributes = [];
                _.forEach(self.formUtils.getInputAttributes(possibleSources), function (attribute) {
                    inputAttributes.push(attribute.name);
                });

                if (leftSourceData && !rightSourceData) {
                    mapSourceType(Constants.LEFT, leftSourceData.getConnectedSource(), true);
                    mapSourceType(Constants.RIGHT, leftSourceData.getConnectedSource(), false);
                } else if (!leftSourceData && rightSourceData) {
                    mapSourceType(Constants.RIGHT, rightSourceData.getConnectedSource(), true);
                    mapSourceType(Constants.LEFT, rightSourceData.getConnectedSource(), false);
                } else if (leftSourceData && rightSourceData) {
                    mapSourceType(Constants.LEFT, leftSourceData.getConnectedSource(), true);
                    mapSourceType(Constants.RIGHT, rightSourceData.getConnectedSource(), true);
                } else if (!leftSourceData && !rightSourceData) {
                    mapSourceType(Constants.LEFT, firstConnectedElement.name, true);
                    mapSourceType(Constants.RIGHT, secondConnectedElement.name, true);
                }

                //source-as
                mapSourceAs(leftSourceData, Constants.LEFT);
                mapSourceAs(rightSourceData, Constants.RIGHT);

                //map-streamHandler
                var streamHandlerList = [];
                getStreamHandlers(leftSourceData, streamHandlerList, Constants.LEFT);
                getStreamHandlers(rightSourceData, streamHandlerList, Constants.RIGHT);
                self.formUtils.addEventListenersForStreamHandlersDiv(streamHandlerList, inputAttributes);

                self.formUtils.mapStreamHandler(leftSourceData, Constants.LEFT)
                self.formUtils.mapStreamHandler(rightSourceData, Constants.RIGHT)

                //is unidirectional
                mapUnidirectionalCheckbox(leftSourceData, Constants.LEFT);
                mapUnidirectionalCheckbox(rightSourceData, Constants.RIGHT);

                //projection
                self.formUtils.selectQueryProjection(select, outputElementName);
                self.formUtils.addEventListenersForSelectionDiv();

                if (having) {
                    $('.having-value').val(having);
                    $(".having-checkbox").prop("checked", true);
                } else {
                    $('.having-condition-content').hide();
                }
                if (on) {
                    $('.on-condition-value').val(on);
                    $(".on-condition-checkbox").prop("checked", true);
                } else {
                    $('.on-condition-content').hide();
                }

                var possibleAttributesWithSourceAs = getPossibleAttributesWithSourceAs(self);
                var outputAttributes = [];
                if (outputElement.type.toLowerCase() === Constants.STREAM ||
                    outputElement.type.toLowerCase() === Constants.TABLE) {
                    var streamAttributes = outputElement.element.getAttributeList();
                    _.forEach(streamAttributes, function (attribute) {
                        outputAttributes.push(attribute.getName());
                    });
                }
                self.formUtils.generateGroupByDiv(groupBy, possibleAttributesWithSourceAs);
                self.formUtils.generateOrderByDiv(orderBy, outputAttributes);

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

                //per and within
                if (firstConnectedElement.type.toLowerCase() === Constants.AGGREGATION ||
                    secondConnectedElement.type.toLowerCase() === Constants.AGGREGATION) {
                    renderPerAndWithin();
                    if (per) {
                        $('.per-condition-value').val(per)
                    }
                    if (within) {
                        $('.within-condition-value').val(within)
                    }
                }

                /**
                 * to show user the lost saved data when the connection is deleted/ when the connected stream is modified
                 * only if the form is an already edited form
                 */
                if (queryOutput && queryOutput.type) {
                    validateSectionsOnLoadOfForm(self);
                }

                //autocompletion
                var outputAttributesWithElementName = self.formUtils.constructOutputAttributes(outputAttributes);
                self.formUtils.addAutoCompleteForRateLimits();
                self.formUtils.addAutoCompleteForFilterConditions(inputAttributes);
                self.formUtils.addAutoCompleteForStreamWindowFunctionAttributes(inputAttributes);
                autoCompleteFieldsWithChangingAttributes(self, outputAttributesWithElementName);

                $('.join-query-form-container').on('blur', '.as-content-value', function () {
                    autoCompleteFieldsWithChangingAttributes(self, outputAttributesWithElementName);
                    var possibleAttributesWithSourceAs = getPossibleAttributesWithSourceAs(self);
                    self.formUtils.generateGroupByDiv(groupBy, possibleAttributesWithSourceAs);
                });

                //to add query operation set
                var setDiv = '<li class="setAttributeValue">' +
                    '<div class="clearfix">' +
                    '<input type="text" class="setAttribute"> <input type="text" class="setValue"> ' +
                    '<a class = "btn-del-option"> <i class = "fw fw-delete"> </i> </a>' +
                    '</div> <label class="error-message"> </label> </li>'
                $('.define-operation-set-condition').on('click', '.btn-add-set', function () {
                    $('.define-operation-set-condition .set-condition').append(setDiv);
                    autoCompleteFieldsWithChangingAttributes(self, outputAttributesWithElementName);
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

                    if (validateSource(Constants.LEFT, self)) {
                        isErrorOccurred = true;
                        return;
                    }

                    if (validateSource(Constants.RIGHT, self)) {
                        isErrorOccurred = true;
                        return;
                    }
                    //isunidirection
                    var isLeftUniDirectionChecked = $('.define-left-source').find('.is-unidirectional-checkbox')
                        .is(':checked');
                    var isRightUniDirectionChecked = $('.define-right-source').find('.is-unidirectional-checkbox')
                        .is(':checked');
                    if (isLeftUniDirectionChecked && isRightUniDirectionChecked) {
                        var leftUnidirectionalDiv = $('.define-left-source .define-unidirectional');
                        leftUnidirectionalDiv.find('.error-message')
                            .text('Unidirectional can not be applied to both the sources.')
                        self.formUtils.addErrorClass(leftUnidirectionalDiv)
                        isErrorOccurred = true;
                        return;
                    }

                    if (!isErrorOccurred) {
                        if (queryName != "") {
                            joinQueryObject.addQueryName(queryName);
                        } else {
                            queryName = "Join Query";
                            joinQueryObject.addQueryName('query');
                        }

                        if ($('.group-by-checkbox').is(':checked')) {
                            var groupByAttributes = self.formUtils.buildGroupBy();
                            joinQueryObject.setGroupBy(groupByAttributes);
                        } else {
                            joinQueryObject.setGroupBy(undefined);
                        }

                        joinQueryObject.clearOrderByValueList()
                        if ($('.order-by-checkbox').is(':checked')) {
                            var orderByAttributes = self.formUtils.buildOrderBy();
                            _.forEach(orderByAttributes, function (attribute) {
                                var orderByValueObject = new QueryOrderByValue(attribute);
                                joinQueryObject.addOrderByValue(orderByValueObject);
                            });
                        }

                        if ($('.on-condition-checkbox').is(':checked')) {
                            queryInput.setOn($('.on-condition-value').val().trim())
                        } else {
                            queryInput.setOn(undefined)
                        }

                        if ($('.having-checkbox').is(':checked')) {
                            joinQueryObject.setHaving($('.having-value').val().trim());
                        } else {
                            joinQueryObject.setHaving(undefined)
                        }

                        if ($('.limit-checkbox').is(':checked')) {
                            joinQueryObject.setLimit($('.limit-value').val().trim())
                        } else {
                            joinQueryObject.setLimit(undefined)
                        }

                        if ($('.offset-checkbox').is(':checked')) {
                            joinQueryObject.setOffset($('.offset-value').val().trim())
                        } else {
                            joinQueryObject.setOffset(undefined)
                        }

                        if ($('.rate-limiting-checkbox').is(':checked')) {
                            joinQueryObject.setOutputRateLimit($('.rate-limiting-value').val().trim())
                        } else {
                            joinQueryObject.setOutputRateLimit(undefined)
                        }

                        if (firstConnectedElement.type.toLowerCase() === Constants.AGGREGATION ||
                            secondConnectedElement.type.toLowerCase() === Constants.AGGREGATION) {
                            queryInput.setPer($('.per-condition-value').val().trim())
                            queryInput.setWithin($('.within-condition-value').val().trim())
                        }

                        var selectObject = new QuerySelect(self.formUtils.buildAttributeSelection(Constants.JOIN_QUERY));
                        joinQueryObject.setSelect(selectObject);

                        var annotationObjectList = [];
                        var annotationStringList = [];
                        var annotationNodes = $('#annotation-div').jstree(true)._model.data['#'].children;
                        self.formUtils.buildAnnotation(annotationNodes, annotationStringList, annotationObjectList);
                        self.formUtils.buildPredefinedAnnotations(predefinedAnnotations, annotationStringList,
                            annotationObjectList);
                        joinQueryObject.clearAnnotationList();
                        joinQueryObject.clearAnnotationListObjects();
                        //add the annotations to the clicked element
                        _.forEach(annotationStringList, function (annotation) {
                            joinQueryObject.addAnnotation(annotation);
                        });
                        _.forEach(annotationObjectList, function (annotation) {
                            joinQueryObject.addAnnotationObject(annotation);
                        });

                        queryInput.setLeft(buildSource(Constants.LEFT, self));
                        queryInput.setRight(buildSource(Constants.RIGHT, self));
                        queryInput.setJoinType($('.join-selection').val().toUpperCase());
                        var joinWithType = undefined;
                        var firstConnectedElementType = firstConnectedElement.type.toLowerCase();
                        var secondConnectedElementType = secondConnectedElement.type.toLowerCase();
                        if (firstConnectedElementType === Constants.TABLE ||
                            secondConnectedElementType === Constants.TABLE) {
                            joinWithType = Constants.TABLE.toUpperCase();
                        } else if (firstConnectedElementType === Constants.WINDOW || secondConnectedElementType ===
                            Constants.WINDOW) {
                            joinWithType = Constants.WINDOW.toUpperCase();
                        } else if (firstConnectedElementType === Constants.AGGREGATION ||
                            secondConnectedElementType === Constants.AGGREGATION) {
                            joinWithType = Constants.AGGREGATION.toUpperCase();
                        } else if (firstConnectedElementType === Constants.TRIGGER ||
                            secondConnectedElementType === Constants.TRIGGER) {
                            joinWithType = Constants.TRIGGER.toUpperCase();
                        } else if (firstConnectedElementType === Constants.STREAM &&
                            secondConnectedElementType === Constants.STREAM) {
                            joinWithType = Constants.STREAM.toUpperCase();
                        }
                        queryInput.setJoinWith(joinWithType);

                        self.formUtils.buildQueryOutput(outputElement, queryOutput);

                        JSONValidator.prototype.validateJoinQuery(joinQueryObject);
                        self.configurationData.setIsDesignViewContentChanged(true);

                        //Send join-query element to the backend and generate tooltip
                        var queryToolTip = self.formUtils.getTooltip(joinQueryObject, Constants.JOIN_QUERY);
                        $('#' + id).prop('title', queryToolTip);
                        var textNode = $('#' + joinQueryObject.getId()).find('.joinQueryNameNode');
                        textNode.html(queryName);

                        // close the form window
                        self.consoleListManager.removeFormConsole(formConsole);
                    }
                });

                // 'Cancel' button action
                $('#' + formConsole.cid).on('click', '#btn-cancel', function () {
                    // close the form
                    self.consoleListManager.removeFormConsole(formConsole);
                });
            }
        };
        return JoinQueryForm;
    });
