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

define(['require', 'log', 'jquery', 'lodash', 'aggregateByTimePeriod', 'querySelect',
    'elementUtils', 'storeAnnotation', 'designViewUtils', 'jsonValidator', 'constants', 'handlebar'],
    function (require, log, $, _, AggregateByTimePeriod, QuerySelect, ElementUtils,
        StoreAnnotation, DesignViewUtils, JSONValidator, Constants, Handlebars) {

        /**
         * @class AggregationForm Creates a forms to collect data from a aggregation
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var AggregationForm = function (options) {
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

        //to get the index annotation
        var getIndexAnnotation = function (primaryIndexAnnotations) {
            var indexAnnotation = [];
            _.forEach(primaryIndexAnnotations, function (annotation) {
                if (annotation.name.toLowerCase() === Constants.INDEX) {
                    indexAnnotation.push(annotation);
                    return false;
                }
            });
            return indexAnnotation;
        };

        //to disable selection of index and partitionId annotation
        var disableIndexAndPartitionById = function () {
            var indexParent = $('#primary-index-annotations')
            var partitionParent = $('#partitionById-annotation');
            indexParent.find('.annotation-checkbox').prop("disabled", true);
            indexParent.find('.annotation-content').hide();

            partitionParent.find('.annotation-checkbox').prop("disabled", true);
            partitionParent.find('.annotation-content').hide();

            $('.store-content').hide();
            $('.store-annotation-checkbox').prop('checked', false);
        };

        //to enable selection of index and partitionId annotation
        var enableIndexAndPartitionById = function () {
            var indexParent = $('#primary-index-annotations')
            var partitionParent = $('#partitionById-annotation');
            indexParent.find('.annotation-checkbox').prop("disabled", false);
            if (indexParent.find('.annotation-checkbox').is(':checked')) {
                indexParent.find('.annotation-content').show();
            }

            partitionParent.find('.annotation-checkbox').prop("disabled", false);
            if (partitionParent.find('.annotation-checkbox').is(':checked')) {
                partitionParent.find('.annotation-content').show();
            }

            $('.store-content').show();
            $('.store-annotation-checkbox').prop('checked', true);
        };

        //to validate the aggregate[interval section]
        var validateAggregateInterval = function (self) {
            var selectedIntervals = [];
            var isErrorOccurred = false;
            $('.interval-option').each(function () {
                if ($(this).find('.interval-checkbox').is(':checked')) {
                    selectedIntervals.push($(this).text().trim())
                }
            });

            if (selectedIntervals.length == 0) {
                $('.interval-content').find('.error-message').text("Minimum one granularity is required.");
                self.formUtils.addErrorClass('.interval-content')
                isErrorOccurred = true;
            }
            return isErrorOccurred;
        };

        //to validate aggregate range
        var validateAggregateRange = function () {
            var isErrorOccurred = false;
            var minRange = $('.min-content').find('.range-selection').val();
            var maxRange = $('.max-content').find('.range-selection').val();
            if (Constants.SIDDHI_TIME.indexOf(minRange) > Constants.SIDDHI_TIME.indexOf(maxRange)) {
                $('.min-content').find('.error-message').text('Start time period must be less than end time' +
                    ' period')
                isErrorOccurred = true;
            }
            return isErrorOccurred;
        };

        //to build aggregate[interval] section
        var buildAggregateInterval = function () {
            var intervals = [];
            $('.interval-option').each(function () {
                if ($(this).find('.interval-checkbox').is(':checked')) {
                    intervals.push($(this).text().trim())
                }
            });
            return intervals;
        };

        //to render interval or range based on user selection
        var renderIntervalOrRange = function (self, selectedValue, aggregateByTimePeriod) {
            if (selectedValue == Constants.INTERVAL) {
                var interval = [];
                if (aggregateByTimePeriod) {
                    interval = aggregateByTimePeriod.getValue();
                } else {
                    interval.push("");
                }
                renderInterval(Constants.SIDDHI_TIME);
            } else {
                renderRange();
                self.formUtils.renderDropDown('.min-content', Constants.SIDDHI_TIME, Constants.RANGE); //min
                self.formUtils.renderDropDown('.max-content', Constants.SIDDHI_TIME, Constants.RANGE); //max
                //select max to have unique time for max and min as for min sec will be selected as default
                $('.max-content').find('.range-selection option').filter(function () {
                    return ($(this).val() == Constants.MINUTES);
                }).prop('selected', true);
            }
            if (aggregateByTimePeriod) {
                mapUserValuesForIntervalOrRange(aggregateByTimePeriod);
            }
        };

        //to render range
        var renderRange = function () {
            var rangeContent = '<div class = "aggregate-by-range"> <div class="min-range"> <label> Starting Time '
                + 'Granularity </label> <div class="min-content"> </div> </div> <div class="max-range"> <label> Ending Time'
                + ' Granularity </label> <div class="max-content"> </div> </div> </div>';
            $('.aggregate-by-time-period-content').html(rangeContent);
        };

        //to render interval
        var renderInterval = function (possibleIntervalAttributes) {
            var intervalTemplate = Handlebars.compile($('#aggregation-by-interval-template').html());
            var wrappedHtml = intervalTemplate(possibleIntervalAttributes);
            $('.aggregate-by-time-period-content').html(wrappedHtml);
        };

        //depending on the user selected aggregate map the values for interval or range
        var mapUserValuesForIntervalOrRange = function (aggregateByTimePeriod) {
            if (aggregateByTimePeriod.getType().toLowerCase() === Constants.INTERVAL) {
                mapIntervalValues(aggregateByTimePeriod.getValue());
            } else {
                mapRangeValues(aggregateByTimePeriod.getValue());
            }
        };

        //to map the user selected range values
        var mapRangeValues = function (rangeValues) {
            //to select min value
            $('.min-content').find('select option').filter(function () {
                return ($(this).val() == rangeValues.min.toLowerCase());
            }).prop('selected', true);

            //to select max value
            $('.max-content').find('select option').filter(function () {
                return ($(this).val() == rangeValues.max.toLowerCase());
            }).prop('selected', true);
        };

        //to map the user selected interval values
        var mapIntervalValues = function (intervalValues) {
            _.forEach(intervalValues, function (interval) {
                $('.interval-content .' + interval.toLowerCase() + '-checkbox').prop('checked', true)
            });
        };

        /**
         * @function generate properties form for a aggregation
         * @param element selected element(aggregation)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        AggregationForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');
            var clickedElement = self.configurationData.getSiddhiAppConfig().getAggregation(id);

            if (!clickedElement.getFrom()) {
                $('#' + id).addClass('incomplete-element');
                DesignViewUtils.prototype.warnAlert('Connect an input stream element');
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            } else {
                var propertyDiv = $('<div id="property-header"> <h3> Aggregation Configuration </h3> </div> ' +
                    '<div id = "define-aggregation"> </div>' + self.formUtils.buildFormButtons());

                formContainer.append(propertyDiv);
                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');
                self.formUtils.popUpSelectedElement(id);

                var customizedStoreOptions = [];
                var storeOptions = [];
                var storeOptionsWithValues = [];

                var name = clickedElement.getName();
                var from = clickedElement.getFrom();
                var savedAnnotations = clickedElement.getAnnotationListObjects();
                var store = clickedElement.getStore();
                var select = clickedElement.getSelect();
                var groupBy = clickedElement.getGroupBy();
                var aggregateByAttribute = clickedElement.getAggregateByAttribute();
                var aggregateByTimePeriod = clickedElement.getAggregateByTimePeriod();

                var incrementalAggregator = self.configurationData.application.config.incremental_aggregator;

                var predefinedStores = _.orderBy(this.configurationData.rawExtensions["store"], ['name'], ['asc']);
                var predefinedPrimaryIndexAnnotations = JSON.parse(JSON.stringify(self.configurationData.application.config.
                    primary_index_annotations));
                var predefinedAggregationAnnotations = JSON.parse(JSON.stringify(self.configurationData.application.config.
                    aggregation_predefined_annotations));
                var savedSource = self.configurationData.getSiddhiAppConfig().getDefinitionElementByName(from);

                //render the aggregation form template
                var aggregationFormTemplate = Handlebars.compile($('#aggregation-form-template').html());
                var wrappedHtml = aggregationFormTemplate({ name: name, from: from });
                $('#define-aggregation').html(wrappedHtml);

                //separate the annotation
                var indexAnnotation = getIndexAnnotation(predefinedPrimaryIndexAnnotations);
                self.formUtils.mapPrimaryIndexAnnotationValues(indexAnnotation, savedAnnotations);
                var annotationsWithoutPrimaryIndex = self.formUtils.getUserAnnotations(savedAnnotations,
                    predefinedPrimaryIndexAnnotations);
                self.formUtils.mapPredefinedAnnotations(annotationsWithoutPrimaryIndex, predefinedAggregationAnnotations);
                var userDefinedAnnotations = self.formUtils.getUserAnnotations(annotationsWithoutPrimaryIndex,
                    predefinedAggregationAnnotations);

                self.formUtils.renderAnnotationTemplate("define-user-defined-annotation", userDefinedAnnotations);
                $('.define-user-defined-annotation').find('h4').html('Customized Annotations');
                self.formUtils.renderPrimaryIndexAnnotations(indexAnnotation, 'define-index-annotation');
                $('.define-index-annotation').find('h4').hide();
                self.formUtils.renderPredefinedAnnotations(predefinedAggregationAnnotations,
                    'define-predefined-aggregation-annotation');
                self.formUtils.renderOptionsForPredefinedAnnotations(predefinedAggregationAnnotations);
                //render the template to  generate the store types
                self.formUtils.renderTypeSelectionTemplate(Constants.STORE, predefinedStores)

                self.formUtils.addCheckedForUserSelectedPredefinedAnnotation(savedAnnotations,
                    predefinedAggregationAnnotations);

                self.formUtils.addEventListenersForOptionsDiv(Constants.STORE);
                self.formUtils.addEventListenersForPredefinedAnnotations();

                $('#define-rdbms-type').on('change', '[name=radioOpt]', function () {
                    var dataStoreOptions = self.formUtils.getRdbmsOptions(storeOptionsWithValues);
                    self.formUtils.renderOptions(dataStoreOptions, customizedStoreOptions, Constants.STORE)
                });

                $('.define-store-annotation').on('change', '.store-annotation-checkbox', function () {
                    if ($(this).is(':checked')) {
                        enableIndexAndPartitionById();
                    } else {
                        disableIndexAndPartitionById();
                    }
                });

                //onchange of the store type select box
                $('#define-store').on('change', '#store-type', function () {
                    $('#define-predefined-annotations').show();
                    storeOptions = self.formUtils.getSelectedTypeParameters(this.value, predefinedStores);
                    if (savedStoreType && savedStoreType === this.value) {
                        customizedStoreOptions = self.formUtils.getCustomizedStoreOptions(storeOptions,
                            savedStoreOptions);
                        storeOptionsWithValues = self.formUtils.mapUserStoreOptionValues(storeOptions,
                            savedStoreOptions);
                        self.formUtils.checkForRdbmsStoreType(this.value, storeOptionsWithValues,
                            customizedStoreOptions);
                    } else {
                        storeOptionsWithValues = self.formUtils.createObjectWithValues(storeOptions);
                        customizedStoreOptions = [];
                        self.formUtils.checkForRdbmsStoreType(this.value, storeOptionsWithValues,
                            customizedStoreOptions);
                    }
                });

                $('#define-aggregate-by').on('change', '.range-selection', function () {
                    self.formUtils.preventMultipleSelection(Constants.RANGE);
                });

                $('#aggregate-by-attribute').on('change', '#aggregate-by-attribute-checkbox', function () {
                    if ($(this).is(':checked')) {
                        $('.aggregate-by-attribute-content').show();
                    } else {
                        $('.aggregate-by-attribute-content').hide();
                    }
                });

                $('#aggregate-by-attribute').on('mouseover', '.attribute-by-desc', function () {
                    $(this).find('.attribute-by-desc-content').show();
                });

                $('#aggregate-by-attribute').on('mouseout', '.attribute-by-desc', function () {
                    $(this).find('.attribute-by-desc-content').hide();
                });

                $('#define-aggregate-by').on('change', '.aggregate-by-time-period-selection', function () {
                    renderIntervalOrRange(self, this.value, aggregateByTimePeriod);
                    self.formUtils.removeDeleteButtonOfFirstValue();
                    self.formUtils.preventMultipleSelection(Constants.RANGE);
                });

                if (store) {
                    var savedStoreAnnotation = clickedElement.getStore();
                    var savedStoreType = savedStoreAnnotation.getType().toLowerCase();
                    storeOptions = self.formUtils.getSelectedTypeParameters(savedStoreType, predefinedStores);
                    var savedStoreAnnotationOptions = savedStoreAnnotation.getOptions();
                    var savedStoreOptions = [];
                    for (var key in savedStoreAnnotationOptions) {
                        if (savedStoreAnnotationOptions.hasOwnProperty(key)) {
                            savedStoreOptions.push({
                                key: key,
                                value: savedStoreAnnotationOptions[key]
                            });
                        }
                    }
                    $('#define-store #store-type').val(savedStoreType);
                    customizedStoreOptions = self.formUtils.getCustomizedStoreOptions(storeOptions, savedStoreOptions);
                    storeOptionsWithValues = self.formUtils.mapUserStoreOptionValues(storeOptions, savedStoreOptions);
                    self.formUtils.checkForRdbmsStoreType(savedStoreType, storeOptionsWithValues, customizedStoreOptions);
                    enableIndexAndPartitionById();
                } else {
                    storeOptions = self.formUtils.getSelectedTypeParameters($('#define-store #store-type').val(),
                        predefinedStores);
                    storeOptionsWithValues = self.formUtils.createObjectWithValues(storeOptions);
                    customizedStoreOptions = [];
                    self.formUtils.renderOptions(storeOptionsWithValues, customizedStoreOptions, Constants.STORE);
                    disableIndexAndPartitionById();
                }

                var possibleAttributes = [];
                if (savedSource.type.toLowerCase() === Constants.STREAM) {
                    var streamAttributes = savedSource.element.getAttributeList();
                    _.forEach(streamAttributes, function (attribute) {
                        possibleAttributes.push(attribute.getName());
                    });
                } else if (savedSource.type.toLowerCase() === Constants.TRIGGER) {
                    possibleAttributes.push(Constants.TRIGGERED_TIME);
                }

                self.formUtils.selectAggregateProjection(select)
                self.formUtils.addEventListenersForSelectionDiv();

				self.formUtils.generateGroupByDiv(groupBy, possibleAttributes);

                self.formUtils.renderDropDown('.aggregate-by-attribute-content', possibleAttributes,
                    Constants.ATTRIBUTE);

                if (!aggregateByAttribute || aggregateByAttribute == "") {
                    $('.aggregate-by-attribute-content').hide();
                } else {
                    $('#aggregate-by-attribute-checkbox').prop("checked", true);

                    $('#aggregate-by-attribute').find('.attribute-selection option').filter(function () {
                        return ($(this).val() == aggregateByAttribute);
                    }).prop('selected', true);
                }

                if (!aggregateByTimePeriod) {
                    var aggregateByTimePeriodType = Constants.INTERVAL;
                } else {
                    var aggregateByTimePeriodType = aggregateByTimePeriod.getType().toLowerCase();
                    $('#aggregate-by-time-period').find('.aggregate-by-time-period-selection option').filter(function () {
                        return ($(this).val() == aggregateByTimePeriodType);
                    }).prop('selected', true);
                }

                renderIntervalOrRange(self, aggregateByTimePeriodType, aggregateByTimePeriod);
                self.formUtils.removeDeleteButtonOfFirstValue();
                self.formUtils.preventMultipleSelection(Constants.RANGE);

                //create autocompletion
                var selectExpressionMatches = JSON.parse(JSON.stringify(possibleAttributes));
                selectExpressionMatches = selectExpressionMatches.concat(incrementalAggregator);
                selectExpressionMatches.push(Constants.AS);
                self.formUtils.createAutocomplete($('.attribute-expression-as'), selectExpressionMatches);
                $('.define-select').on('click', '.btn-add-user-defined-attribute', function () {
                    self.formUtils.appendUserSelectAttribute();
                    self.formUtils.createAutocomplete($('.attribute-expression-as:last'), selectExpressionMatches);
                });

                $(formContainer).on('click', '#btn-submit', function () {

                    $('.error-message').text("")
                    $('.required-input-field').removeClass('required-input-field');
                    var isErrorOccurred = false;

                    var aggregationName = $('#aggregationName').val().trim();
                    //check if aggregation name is empty
                    if (aggregationName == "") {
                        self.formUtils.addErrorClass("#aggregationName");
                        $('#aggregationNameErrorMessage').text("Aggregation name is required.")
                        isErrorOccurred = true;
                        return;
                    }
                    var previouslySavedName = clickedElement.getName();
                    if (previouslySavedName === undefined) {
                        previouslySavedName = "";
                    }
                    if (previouslySavedName !== aggregationName) {
                        var isAggregationNameUsed = self.formUtils.isDefinitionElementNameUsed(aggregationName, id);
                        if (isAggregationNameUsed) {
                            self.formUtils.addErrorClass("#aggregationName");
                            $('#aggregationNameErrorMessage').text("Aggregation name is already used.")
                            isErrorOccurred = true;
                            return;
                        }
                        if (self.formUtils.validateAttributeOrElementName("#aggregationName", Constants.AGGREGATION,
                            aggregationName)) {
                            isErrorOccurred = true;
                            return;
                        }
                    }

                    var isStoreChecked = $('.store-annotation-checkbox').is(':checked');
                    if (isStoreChecked) {
                        if (self.formUtils.validateOptions(storeOptions, Constants.STORE)) {
                            isErrorOccurred = true;
                            return;
                        }
                        if (self.formUtils.validateCustomizedOptions(Constants.STORE)) {
                            isErrorOccurred = true;
                            return;
                        }
                        if (self.formUtils.validatePrimaryIndexAnnotations()) {
                            isErrorOccurred = true;
                            return;
                        }
                    }

                    if (self.formUtils.validatePredefinedAnnotations(predefinedAggregationAnnotations)) {
                        isErrorOccurred = true;
                        return;
                    }

                    if (self.formUtils.validateAggregateProjection(possibleAttributes)) {
                        isErrorOccurred = true;
                        return;
                    }

                    if ($('.group-by-checkbox').is(':checked')) {
                        if (self.formUtils.validateGroupOrderBy(Constants.GROUP_BY)) {
                            isErrorOccurred = true;
                            return;
                        }
                    }

                    if ($('.aggregate-by-time-period-selection').val() === Constants.INTERVAL) {
                        if (validateAggregateInterval(self)) {
                            isErrorOccurred = true;
                            return;
                        }
                    } else {
                        if (validateAggregateRange()) {
                            isErrorOccurred = true;
                            return;
                        }
                    }

                    if (!isErrorOccurred) {

                        clickedElement.setFrom($('#aggregation-from').val().trim());

                        if (previouslySavedName !== aggregationName) {
                            // update selected aggregation model
                            clickedElement.setName(aggregationName);
                            // update connection related to the element if the name is changed
                            self.formUtils.updateConnectionsAfterDefinitionElementNameChange(id);

                            var textNode = $('#' + id).find('.aggregationNameNode');
                            textNode.html(aggregationName);
                        }

                        var annotationStringList = [];
                        var annotationObjectList = [];
                        if (isStoreChecked) {
                            //add store
                            var selectedStoreType = $('#define-store #store-type').val();
                            var optionsMap = {};
                            self.formUtils.buildStoreAndAnnotationOptions(optionsMap, Constants.STORE);
                            self.formUtils.buildCustomizedStoreOption(optionsMap);
                            var storeAnnotationOptions = {};
                            _.set(storeAnnotationOptions, 'type', selectedStoreType);
                            _.set(storeAnnotationOptions, 'options', optionsMap);
                            var storeAnnotation = new StoreAnnotation(storeAnnotationOptions);
                            clickedElement.setStore(storeAnnotation);

                            //buildAnnotations
                            self.formUtils.buildPrimaryIndexAnnotations(annotationStringList, annotationObjectList);
                        } else {
                            clickedElement.setStore(undefined);
                        }

                        self.formUtils.buildPredefinedAnnotations(predefinedAggregationAnnotations, annotationStringList,
                            annotationObjectList);
                        var annotationNodes = $('#annotation-div').jstree(true)._model.data['#'].children;
                        self.formUtils.buildAnnotation(annotationNodes, annotationStringList, annotationObjectList)
                        clickedElement.clearAnnotationList();
                        clickedElement.clearAnnotationListObjects();
                        _.forEach(annotationStringList, function (annotation) {
                            clickedElement.addAnnotation(annotation);
                        });
                        _.forEach(annotationObjectList, function (annotation) {
                            clickedElement.addAnnotationObject(annotation);
                        });

                        var selectObject = new QuerySelect(self.formUtils.buildAttributeSelection(Constants.AGGREGATION));
                        clickedElement.setSelect(selectObject);

                        if ($('.group-by-checkbox').is(':checked')) {
                            var groupByAttributes = self.formUtils.buildGroupBy();
                            clickedElement.setGroupBy(groupByAttributes);
                        } else {
                            clickedElement.setGroupBy(undefined);
                        }

                        if ($('#aggregate-by-attribute-checkbox').is(':checked')) {
                            clickedElement.setAggregateByAttribute(
                                $('#aggregate-by-attribute .attribute-selection').val())
                        } else {
                            clickedElement.setAggregateByAttribute(undefined)
                        }

                        var aggregateByTimePeriodOptions = {};
                        var aggregateByTimePeriodType;
                        if ($('.aggregate-by-time-period-selection').val() === Constants.INTERVAL) {
                            var value = buildAggregateInterval();
                            aggregateByTimePeriodType = Constants.INTERVAL.toUpperCase();
                        } else {
                            aggregateByTimePeriodType = Constants.RANGE.toUpperCase();
                            var value = {
                                min: ($('.min-content').find('.range-selection').val()).toUpperCase(),
                                max: ($('.max-content').find('.range-selection').val()).toUpperCase()
                            };
                        }
                        _.set(aggregateByTimePeriodOptions, 'type', aggregateByTimePeriodType);
                        _.set(aggregateByTimePeriodOptions, 'value', value);
                        var aggregateByTimePeriod = new AggregateByTimePeriod(aggregateByTimePeriodOptions);
                        clickedElement.setAggregateByTimePeriod(aggregateByTimePeriod);

                        $('#' + id).removeClass('incomplete-element');
                        $('#' + id).removeClass('error-element');
                        //Send aggregation element to the backend and generate tooltip
                        var aggregationToolTip = self.formUtils.getTooltip(clickedElement, Constants.AGGREGATION);
                        $('#' + id).prop('title', aggregationToolTip);

                        self.configurationData.setIsDesignViewContentChanged(true);
                        // design view container and toggle view button are enabled
                        self.designViewContainer.removeClass('disableContainer');
                        self.toggleViewButton.removeClass('disableContainer');

                        // close the form aggregation
                        self.consoleListManager.removeFormConsole(formConsole);
                    }
                });

                // 'Cancel' button action
                var cancelButtonElement = $(formContainer).find('#btn-cancel')[0];
                cancelButtonElement.addEventListener('click', function () {
                    // design view container and toggle view button are enabled
                    self.designViewContainer.removeClass('disableContainer');
                    self.toggleViewButton.removeClass('disableContainer');

                    // close the form aggregation
                    self.consoleListManager.removeFormConsole(formConsole);
                });
            }
        };

        return AggregationForm;
    });
