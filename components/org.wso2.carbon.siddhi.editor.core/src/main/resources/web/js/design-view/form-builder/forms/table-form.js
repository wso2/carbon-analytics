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
                this.consoleListManager = options.application.outputController;
                var currentTabId = this.application.tabController.activeTab.cid;
                this.designViewContainer = $('#design-container-' + currentTabId);
                this.toggleViewButton = $('#toggle-view-button-' + currentTabId);
            }
        };

        /**
         * @function to add a default store type to the predefined stores
         * @param {Object} predefinedStores predefined store types
         */
        var addDefaultStoreType = function (predefinedStores) {
            //first check if in-memory is already present in the predefined stores array
            var found = false;
            for (var store of predefinedStores) {
                if (store.name === Constants.DEFAULT_STORE_TYPE) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                var inMemoryType = {
                    name: Constants.DEFAULT_STORE_TYPE,
                    parameters: []
                };
                predefinedStores.push(inMemoryType);
            }
        };

        /**
         * @function to render the html to display the radio options for selecting the rdbms type
         */
        var renderRdbmsTypes = function () {
            var rdbmsTypeDiv = '<div class="clearfix"> <label class = "rdbms-type">' +
                '<input type= "radio" name ="radioOpt" value="inline-config"> Inline-config' +
                '</label> <label class = "rdbms-type">  ' +
                '<input type = "radio" name = "radioOpt" value = "datasource"> Datasource </label>' +
                '<label class = "rdbms-type"> <input type = "radio" name = "radioOpt" value="jndi"> Jndi-resource ' +
                '</label></div> ';
            $('#define-rdbms-type').html(rdbmsTypeDiv);
        };

        /**
         * @function to select the options according to the selected rdbms type
         * @param {Object} predefined_options all the options of rdbms with the user given values
         * @return {Object} rdbms_options
         */
        var getRdbmsOptions = function (predefined_options) {
            var rdbms_options = [];
            var selectedRdbmsType = $('input[name=radioOpt]:checked', '#define-rdbms-type').val();
            if (selectedRdbmsType == "datasource") {
                _.forEach(predefined_options, function (predefinedOption) {
                    if (predefinedOption.key.toLowerCase() === "datasource") {
                        rdbms_options.push({
                            key: predefinedOption.key, value: predefinedOption.value, description: predefinedOption
                                .description, optional: false, defaultValue: predefinedOption.defaultValue
                        })
                    } else if (predefinedOption.key.toLowerCase() === "pool.properties" ||
                        predefinedOption.key.toLowerCase() === "table.name" ||
                        predefinedOption.key.toLowerCase() === "field.length") {
                        rdbms_options.push({
                            key: predefinedOption.key, value: predefinedOption.value, description: predefinedOption
                                .description, optional: predefinedOption.optional, defaultValue: predefinedOption
                                    .defaultValue
                        })
                    }
                });
            } else if (selectedRdbmsType == "inline-config") {
                _.forEach(predefined_options, function (predefinedOption) {
                    if (predefinedOption.key.toLowerCase() === "username" ||
                        predefinedOption.key.toLowerCase() === "password" ||
                        predefinedOption.key.toLowerCase() === "jdbc.url" ||
                        predefinedOption.key.toLowerCase() === "jdbc.driver.name" ||
                        predefinedOption.key.toLowerCase() === "pool.properties" ||
                        predefinedOption.key.toLowerCase() === "table.name" ||
                        predefinedOption.key.toLowerCase() === "field.length") {
                        rdbms_options.push({
                            key: predefinedOption.key, value: predefinedOption.value, description: predefinedOption
                                .description, optional: predefinedOption.optional, defaultValue: predefinedOption
                                    .defaultValue
                        })
                    }
                });
            } else {
                _.forEach(predefined_options, function (predefinedOption) {
                    if (predefinedOption.key.toLowerCase() === "jndi.resource") {
                        rdbms_options.push({
                            key: predefinedOption.key, value: predefinedOption.value, description: predefinedOption
                                .description, optional: false, defaultValue: predefinedOption.defaultValue
                        })
                    } else if (predefinedOption.key.toLowerCase() === "table.name" ||
                        predefinedOption.key.toLowerCase() === "field.length") {
                        rdbms_options.push({
                            key: predefinedOption.key, value: predefinedOption.value, description: predefinedOption
                                .description, optional: predefinedOption.optional, defaultValue: predefinedOption
                                    .defaultValue
                        })
                    }

                });

            }
            return rdbms_options;
        };

        /**
         * @function to check the radio button of the selected rdbms type
         * @param {Object} rdbmsOptions all the options of rdbms with the user given values
         */
        var checkRdbmsType = function (rdbmsOptions) {
            var isFound = false;
            for (var option of rdbmsOptions) {
                if (option.key.toLowerCase() == "datasource" && option.value != "") {
                    $("#define-rdbms-type input[name=radioOpt][value='datasource']").prop("checked", true);
                    isFound = true;
                    break;
                } else if (option.key.toLowerCase() == "jndi.resource" && option.value != "") {
                    $("#define-rdbms-type input[name=radioOpt][value='jndi']").prop("checked", true);
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                $("#define-rdbms-type input[name=radioOpt][value='inline-config']").prop("checked", true);
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
            var clickedElement = self.configurationData.getSiddhiAppConfig().getTable(id);

            var propertyDiv = $('<div class = "table-form-container table-div"> <div id="property-header"> <h3> Table' +
                ' Configuration </h3> </div> <h4> Name: </h4> <input type="text" id="tableName" ' +
                'class = "clearfix name"> <label class="error-message" id="tableNameErrorMessage"> </label>' +
                '<div id = "define-attribute"> </div>' + self.formUtils.buildFormButtons() + '</div> ' +
                '<div class = "table-form-container store-div"> <div id = "define-store"> </div>  ' +
                '<div id="define-rdbms-type"> </div> <div id="store-options-div"> </div> </div> ' +
                '<div class = "table-form-container define-table-annotation">' +
                '<div id = "define-predefined-annotations"> </div> <div id = "define-user-annotations"> </div> </div>');

            formContainer.append(propertyDiv);
            self.formUtils.popUpSelectedElement(id);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var predefinedStores = _.orderBy(this.configurationData.rawExtensions["store"], ['name'], ['asc']);
            addDefaultStoreType(predefinedStores);
            var predefinedTableAnnotations = self.configurationData.application.config.primary_index_annotations;
            var customizedStoreOptions = [];
            var storeOptions = [];
            var storeOptionsWithValues = [];

            self.formUtils.addEventListenersForOptionsDiv(Constants.STORE);

            var name = clickedElement.getName();
            if (!name) {
                var attributes = [{ name: "" }];
                self.formUtils.renderAttributeTemplate(attributes)

            } else {
                $('#tableName').val(name);
                var savedAttributes = clickedElement.getAttributeList();
                self.formUtils.renderAttributeTemplate(savedAttributes)
                self.formUtils.selectTypesOfSavedAttributes(savedAttributes);
            }
            var savedAnnotations = clickedElement.getAnnotationList();
            var savedAnnotationObjects = clickedElement.getAnnotationListObjects();
            var userAnnotations = [];
            var tableAnnotations = JSON.parse(JSON.stringify(predefinedTableAnnotations));
            if (savedAnnotations && savedAnnotations.length != 0) {
                userAnnotations = self.formUtils.getUserAnnotations(savedAnnotationObjects, tableAnnotations);
                self.formUtils.mapPrimaryIndexAnnotationValues(tableAnnotations, savedAnnotationObjects)
            }

            //render the predefined table annotation form template
            self.formUtils.renderPrimaryIndexAnnotations(tableAnnotations, 'define-predefined-annotations');

            self.formUtils.renderAnnotationTemplate("define-user-annotations", userAnnotations);
            $('#define-user-annotations').find('h4').html('Customized Annotations');

            //render the template to  generate the store types
            self.formUtils.renderTypeSelectionTemplate(Constants.STORE, predefinedStores)

            $('#define-rdbms-type').on('change', '[name=radioOpt]', function () {
                var dataStoreOptions = getRdbmsOptions(storeOptionsWithValues);
                self.formUtils.renderOptions(dataStoreOptions, customizedStoreOptions, Constants.STORE)
            });

            //onchange of the store type select box
            $('#define-store').on('change', '#store-type', function () {
                if (this.value === Constants.DEFAULT_STORE_TYPE) {
                    $('#store-options-div').empty();
                    $('#define-rdbms-type').hide();
                } else if (clickedElement.getStore() && savedStoreType === this.value) {
                    storeOptions = self.formUtils.getSelectedTypeParameters(this.value, predefinedStores);
                    customizedStoreOptions = self.formUtils.getCustomizedStoreOptions(storeOptions, savedStoreOptions);
                    storeOptionsWithValues = self.formUtils.mapUserStoreOptionValues(storeOptions, savedStoreOptions);
                    if (this.value == Constants.RDBMS_STORE_TYPE) {
                        $('#define-rdbms-type').show();
                        var dataStoreOptions = getRdbmsOptions(storeOptionsWithValues);
                        self.formUtils.renderOptions(dataStoreOptions, customizedStoreOptions, Constants.STORE);
                        checkRdbmsType(storeOptionsWithValues);
                    } else {
                        $('#define-rdbms-type').hide();
                        self.formUtils.renderOptions(storeOptionsWithValues, customizedStoreOptions, Constants.STORE);
                    }
                } else {
                    storeOptions = self.formUtils.getSelectedTypeParameters(this.value, predefinedStores);
                    storeOptionsWithValues = self.formUtils.createObjectWithValues(storeOptions);
                    customizedStoreOptions = [];
                    if (this.value == Constants.RDBMS_STORE_TYPE) {
                        renderRdbmsTypes();
                        //as default select the data-store type
                        $("#define-rdbms-type input[name=radioOpt][value='inline-config']").prop("checked", true);
                        var dataStoreOptions = getRdbmsOptions(storeOptionsWithValues);
                        self.formUtils.renderOptions(dataStoreOptions, customizedStoreOptions, Constants.STORE)
                        $('#define-rdbms-type').show();
                    } else {
                        $('#define-rdbms-type').hide();
                        self.formUtils.renderOptions(storeOptionsWithValues, customizedStoreOptions, Constants.STORE);
                    }
                }
            });

            if (clickedElement.getStore()) {
                //if table object is already edited
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
                if (savedStoreType == Constants.RDBMS_STORE_TYPE) {
                    renderRdbmsTypes();
                    checkRdbmsType(storeOptionsWithValues);
                    var dataStoreOptions = getRdbmsOptions(storeOptionsWithValues);
                    self.formUtils.renderOptions(dataStoreOptions, customizedStoreOptions, Constants.STORE);
                } else {
                    self.formUtils.renderOptions(storeOptionsWithValues, customizedStoreOptions, Constants.STORE);
                }
            } else {
                //if table form is freshly opened [ new table object]
                $('#define-store #store-type').val(Constants.DEFAULT_STORE_TYPE);
            }

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#btn-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                //clear the error classes
                $('.error-message').text("");
                $('#tableNameErrorMessage').text("");
                $('.required-input-field').removeClass('required-input-field');
                var isErrorOccurred = false;

                var tableName = $('#tableName').val().trim();
                // to check if table name is empty
                if (tableName == "") {
                    self.formUtils.addErrorClass("#tableName");
                    $('#tableNameErrorMessage').text("Table name is required.");
                    isErrorOccurred = true;
                    return;
                }
                var previouslySavedName = clickedElement.getName();
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
                    if (self.formUtils.validateOptions(storeOptions, Constants.STORE)) {
                        isErrorOccurred = true;
                        return;
                    }
                    if (self.formUtils.validateCustomizedOptions(Constants.STORE)) {
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

                if (self.formUtils.validatePrimaryIndexAnnotations()) {
                    isErrorOccurred = true;
                    return;
                }

                if (!isErrorOccurred) {
                    if (previouslySavedName !== tableName) {
                        // update selected table model
                        clickedElement.setName(tableName);
                        // update connection related to the element if the name is changed
                        self.formUtils.updateConnectionsAfterDefinitionElementNameChange(id);

                        var textNode = $('#' + id).find('.tableNameNode');
                        textNode.html(tableName);
                    }

                    if (selectedStoreType !== Constants.DEFAULT_STORE_TYPE) {
                        var optionsMap = {};
                        self.formUtils.buildStoreOptions(optionsMap);
                        self.formUtils.buildCustomizedStoreOption(optionsMap);
                        var storeAnnotationOptions = {};
                        _.set(storeAnnotationOptions, 'type', selectedStoreType);
                        _.set(storeAnnotationOptions, 'options', optionsMap);
                        var storeAnnotation = new StoreAnnotation(storeAnnotationOptions);
                        clickedElement.setStore(storeAnnotation);
                    } else {
                        clickedElement.setStore(undefined);
                    }

                    //clear the saved attributes
                    clickedElement.clearAttributeList()
                    //add the attributes
                    $('.attribute .attr-content').each(function () {
                        var nameValue = $(this).find('.attr-name').val().trim();
                        var typeValue = $(this).find('.attr-type').val();
                        if (nameValue != "") {
                            var attributeObject = new Attribute({ name: nameValue, type: typeValue });
                            clickedElement.addAttribute(attributeObject);
                        }
                    });

                    var annotationList = [];
                    var annotationObjectList = [];
                    //clear the annotationlist
                    clickedElement.clearAnnotationList();
                    clickedElement.clearAnnotationListObjects();
                    self.formUtils.buildPrimaryIndexAnnotations(annotationList, annotationObjectList);
                    var annotationNodes = $('#annotation-div').jstree(true)._model.data['#'].children;
                    self.formUtils.buildAnnotation(annotationNodes, annotationList, annotationObjectList)
                    //add the annotations to the clicked element
                    _.forEach(annotationList, function (annotation) {
                        clickedElement.addAnnotation(annotation);
                    });
                    _.forEach(annotationObjectList, function (annotation) {
                        clickedElement.addAnnotationObject(annotation);
                    });

                    $('#' + id).removeClass('incomplete-element');
                    //Send table element to the backend and generate tooltip
                    var tableToolTip = self.formUtils.getTooltip(clickedElement, Constants.TABLE);
                    $('#' + id).prop('title', tableToolTip);

                    // set the isDesignViewContentChanged to true
                    self.configurationData.setIsDesignViewContentChanged(true);
                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                    self.designViewContainer.removeClass('disableContainer');
                    self.toggleViewButton.removeClass('disableContainer');
                }
            });

            // 'Cancel' button action
            var cancelButtonElement = $(formContainer).find('#btn-cancel')[0];
            cancelButtonElement.addEventListener('click', function () {
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');
                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            });
        };

        return TableForm;
    });

