/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['log', 'jquery', 'lodash', 'attribute', 'storeAnnotation', 'handlebar', 'annotationObject', 'annotationElement',
        'constants'],
    function (log, $, _, Attribute, StoreAnnotation, Handlebars, AnnotationObject, AnnotationElement, Constants) {

        /**
         * @class TableForm Creates a forms to collect data from a table
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var TableForm = function (options) {
            if (options !== undefined) {
                this.configurationData = options.configurationData;
                this.application = options.application;
                this.formUtils = options.formUtils;
                this.jsPlumbInstance = options.jsPlumbInstance;
                this.consoleListManager = options.application.outputController;
                var currentTabId = this.application.tabController.activeTab.cid;
                this.designViewContainer = $('#design-container-' + currentTabId);
                this.toggleViewButton = $('#toggle-view-button-' + currentTabId);
            }
        };

        /**
         * @function generate properties form for a table
         * @param element selected element(table)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        TableForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');
            var tableObject = self.configurationData.getSiddhiAppConfig().getTable(id);
            var previousTableObject = _.cloneDeep(tableObject);

            var propertyDiv = $('<div class="clearfix form-min-width"> <div class = "table-form-container table-div"> ' +
                '<label> <span class="mandatory-symbol"> *</span>Name </label> <input type="text" id="tableName" ' +
                'class = "clearfix name"> <label class="error-message" id="tableNameErrorMessage"> </label>' +
                '<div id = "define-attribute"> </div></div> <div class = "table-form-container store-div"> ' +
                '<div id = "define-store"> </div> <div id="define-rdbms-type"> </div> <div id="store-options-div"> ' +
                '</div> </div> <div class = "table-form-container define-table-annotation">' +
                '<div class = "define-predefined-annotations"> </div> <div class="define-user-defined-annotations"> ' +
                '</div> </div> </div>');

            formContainer.html(propertyDiv);
            self.formUtils.buildFormButtons(formConsole.cid);
            self.formUtils.popUpSelectedElement(id);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            self.formUtils.addEventListenerToRemoveRequiredClass();
            self.formUtils.addEventListenerToShowAndHideInfo();
            self.formUtils.addEventListenerToShowInputContentOnHover();

            var predefinedStores = _.orderBy(_.cloneDeep(this.configurationData.rawExtensions["store"]),
                ['name'], ['asc']);
            self.formUtils.addCustomizedType(predefinedStores, Constants.DEFAULT_STORE_TYPE);
            var predefinedTableAnnotations = _.cloneDeep(self.configurationData.application.config.
                type_table_predefined_annotations);
            var customizedStoreOptions = [];
            var currentStoreOptions = [];
            var storeOptionsWithValues = [];

            self.formUtils.addEventListenersForGenericOptionsDiv(Constants.STORE);

            var name = tableObject.getName();
            if (!name) {
                var attributes = [{ name: "" }];
                self.formUtils.renderAttributeTemplate(attributes)

            } else {
                $('#tableName').val(name);
                var attributeList = tableObject.getAttributeList();
                self.formUtils.renderAttributeTemplate(attributeList)
                self.formUtils.selectTypesOfSavedAttributes(attributeList);
            }

            var annotationListObjects = tableObject.getAnnotationListObjects();
            var userAnnotations = [];
            var tableAnnotations = self.formUtils.createObjectsForAnnotationsWithoutKeys(predefinedTableAnnotations);
            if (annotationListObjects && annotationListObjects.length != 0) {
                userAnnotations = self.formUtils.getUserAnnotations(annotationListObjects, tableAnnotations);
                self.formUtils.mapPrimaryIndexAnnotationValues(tableAnnotations, annotationListObjects)
            }

            //render the predefined table annotation form template
            self.formUtils.renderPrimaryIndexAnnotations(tableAnnotations, 'define-predefined-annotations');

            self.formUtils.renderAnnotationTemplate("define-user-defined-annotations", userAnnotations);
            $('.define-user-defined-annotations').find('label:first-child').html('Customized Annotations');

            //render the template to  generate the store types
            self.formUtils.renderSourceSinkStoreTypeDropDown(Constants.STORE, predefinedStores)

            $('#define-rdbms-type').on('change', '[name=radioOpt]', function () {
                var dataStoreOptions = self.formUtils.getRdbmsOptions(storeOptionsWithValues);
                self.formUtils.renderOptions(dataStoreOptions, customizedStoreOptions, Constants.STORE)
            });

            //onchange of the store type select box
            $('#define-store').on('change', '#store-type', function () {
                if (this.value === Constants.DEFAULT_STORE_TYPE) {
                    $('.define-predefined-annotations').hide();
                    $('#store-options-div').empty();
                    $('#define-rdbms-type').hide();
                } else {
                    $('.define-predefined-annotations').show();
                    currentStoreOptions = self.formUtils.getSelectedTypeParameters(this.value, predefinedStores);
                    if (tableObject.getStore() && storeType === this.value) {
                        customizedStoreOptions = self.formUtils.getCustomizedOptions(currentStoreOptions, storeOptions);
                        storeOptionsWithValues = self.formUtils.mapUserOptionValues(currentStoreOptions, storeOptions);
                        self.formUtils.populateStoreOptions(this.value, storeOptionsWithValues, customizedStoreOptions);
                    } else {
                        storeOptionsWithValues = self.formUtils.createObjectWithValues(currentStoreOptions);
                        customizedStoreOptions = [];
                        self.formUtils.populateStoreOptions(this.value, storeOptionsWithValues, customizedStoreOptions);
                    }
                }
            });

            if (tableObject.getStore()) {
                //if table object is already edited
                var storeAnnotation = tableObject.getStore();
                var storeType = storeAnnotation.getType().toLowerCase();
                currentStoreOptions = self.formUtils.getSelectedTypeParameters(storeType, predefinedStores);
                var storeOptions = storeAnnotation.getOptions();
                $('#define-store #store-type').val(storeType);
                customizedStoreOptions = self.formUtils.getCustomizedOptions(currentStoreOptions, storeOptions);
                storeOptionsWithValues = self.formUtils.mapUserOptionValues(currentStoreOptions, storeOptions);
                self.formUtils.populateStoreOptions(storeType, storeOptionsWithValues, customizedStoreOptions);
            } else {
                //if table form is freshly opened [ new table object]
                $('#define-store #store-type').val(Constants.DEFAULT_STORE_TYPE);
                $('.define-predefined-annotations').hide();
            }

            self.formUtils.initPerfectScroller(formConsole.cid);

            // 'Submit' button action
            $('#' + formConsole.cid).on('click', '#btn-submit', function () {

                self.formUtils.removeErrorClass();
                var isErrorOccurred = false;

                var tableName = $('#tableName').val().trim();
                // to check if table name is empty
                if (tableName == "") {
                    self.formUtils.addErrorClass("#tableName");
                    $('#tableNameErrorMessage').text("Table name is required.");
                    isErrorOccurred = true;
                    return;
                }
                var previouslySavedName = tableObject.getName();
                if (!previouslySavedName) {
                    previouslySavedName = "";
                }
                if (previouslySavedName !== tableName) {
                    var isTableNameUsed = self.formUtils.isDefinitionElementNameUsed(tableName);
                    if (isTableNameUsed) {
                        self.formUtils.addErrorClass("#tableName");
                        $('#tableNameErrorMessage').text("Table name is already used.");
                        isErrorOccurred = true;
                        return;
                    }
                    if (self.formUtils.validateAttributeOrElementName("#tableName", Constants.TABLE, tableName)) {
                        isErrorOccurred = true;
                        return;
                    }
                }

                //store annotation
                var selectedStoreType = $('#define-store #store-type').val();
                if (selectedStoreType !== Constants.DEFAULT_STORE_TYPE) {
                    if (self.formUtils.validateOptions(currentStoreOptions, Constants.STORE)) {
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

                var attributeNameList = [];
                if (self.formUtils.validateAttributes(attributeNameList)) {
                    isErrorOccurred = true;
                    return;
                }
                if (attributeNameList.length == 0) {
                    self.formUtils.addErrorClass($('.attribute:eq(0)').find('.attr-name'));
                    $('.attribute:eq(0)').find('.error-message').text("Minimum one attribute is required.");
                    isErrorOccurred = true;
                    return;
                }

                if (!isErrorOccurred) {
                    var annotationList = [];
                    var annotationObjectList = [];
                    //clear the annotation list
                    tableObject.clearAnnotationList();
                    tableObject.clearAnnotationListObjects();

                    tableObject.setName(tableName);
                    var textNode = $('#' + id).find('.tableNameNode');
                    textNode.html(tableName);

                    if (selectedStoreType !== Constants.DEFAULT_STORE_TYPE) {
                        var storeOptions = [];
                        self.formUtils.buildOptions(storeOptions, Constants.STORE);
                        self.formUtils.buildCustomizedOption(storeOptions, Constants.STORE);
                        var storeAnnotationOptions = {};
                        _.set(storeAnnotationOptions, 'type', selectedStoreType);
                        _.set(storeAnnotationOptions, 'options', storeOptions);
                        var storeAnnotation = new StoreAnnotation(storeAnnotationOptions);
                        tableObject.setStore(storeAnnotation);

                        self.formUtils.buildPrimaryIndexAnnotations(annotationList, annotationObjectList);
                    } else {
                        tableObject.setStore(undefined);
                    }

                    var annotationNodes = $('#annotation-div').jstree(true)._model.data['#'].children;
                    self.formUtils.buildAnnotation(annotationNodes, annotationList, annotationObjectList)
                    //add the annotations to the clicked element
                    _.forEach(annotationList, function (annotation) {
                        tableObject.addAnnotation(annotation);
                    });
                    _.forEach(annotationObjectList, function (annotation) {
                        tableObject.addAnnotationObject(annotation);
                    });

                    //clear the saved attributes
                    tableObject.clearAttributeList()
                    //add the attributes
                    $('.attribute .attr-content').each(function () {
                        var nameValue = $(this).find('.attr-name').val().trim();
                        var typeValue = $(this).find('.attr-type').val();
                        if (nameValue != "") {
                            var attributeObject = new Attribute({ name: nameValue, type: typeValue });
                            tableObject.addAttribute(attributeObject);
                        }
                    });

                    if (self.formUtils.isUpdatingOtherElementsRequired(previousTableObject, tableObject,
                        Constants.TABLE)) {
                        var outConnections = self.jsPlumbInstance.getConnections({source: id + '-out'});
                        var inConnections = self.jsPlumbInstance.getConnections({target: id + '-in'});

                        //to delete the connection, it requires the previous object name
                        tableObject.setName(previousTableObject.getName())
                        // delete connections related to the element if the name is changed
                        self.formUtils.deleteConnectionsAfterDefinitionElementNameChange(outConnections, inConnections);
                        //reset the name to new name
                        tableObject.setName(tableName);

                        // establish connections related to the element if the name is changed
                        self.formUtils.establishConnectionsAfterDefinitionElementNameChange(outConnections, inConnections);
                    }

                    $('#' + id).removeClass('incomplete-element');
                    //Send table element to the backend and generate tooltip
                    var tableToolTip = self.formUtils.getTooltip(tableObject, Constants.TABLE);
                    $('#' + id).prop('title', tableToolTip);

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
        };

        return TableForm;
    });

