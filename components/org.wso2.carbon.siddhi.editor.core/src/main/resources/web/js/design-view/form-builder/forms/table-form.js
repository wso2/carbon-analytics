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

define(['log', 'jquery', 'lodash', 'attribute', 'table', 'storeAnnotation', 'designViewUtils', 'handlebar',
    'js_tree', 'annotationObject', 'annotationElement'],
    function (log, $, _, Attribute, Table, StoreAnnotation, DesignViewUtils, Handlebars, jstree,
        AnnotationObject, AnnotationElement) {

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

        const alphabeticValidatorRegex = /^([a-zA-Z])$/;
        const defaultStoreType = "in-memory";
        const rdbmsStoreType = "rdbms";


        /** Function to manage the attribute navigations */
        var changeAtrributeNavigation = function () {
            $('.attr-nav').empty();
            var attrLength = $('#attribute-div li').length;
            if (attrLength == 1) {
                $('.attribute:eq(0)').find('.attr-nav').empty();
            }
            if (attrLength == 2) {
                $('.attribute:eq(0)').find('.attr-nav').append('<a class = "reorder-down"><i class="fw fw-sort-down">' +
                    '</i></a><a class = "btn-del-attr"><i class="fw fw-delete"></i></a>');
                $('.attribute:eq(1)').find('.attr-nav').append('<a class="reorder-up"> <i class="fw fw-sort-up "></i>' +
                    '</a><a class = "btn-del-attr"><i class="fw fw-delete"></i></a>');
            }
            if (attrLength > 2) {
                var lastIndex = attrLength - 1;
                for (var i = 0; i < attrLength; i++) {
                    $('.attribute:eq(' + i + ')').find('.attr-nav').append('<a class="reorder-up"> ' +
                        '<i class="fw fw-sort-up"></i></a>' +
                        '<a class = "reorder-down"><i class="fw fw-sort-down"> </i></a>' +
                        '<a class = "btn-del-attr"><i class="fw fw-delete"></i></a>');
                }
                $('.attribute:eq(0)').find('.attr-nav a:eq(0)').remove();
                $('.attribute:eq(' + lastIndex + ')').find('.attr-nav a:eq(1)').remove();
            }
        };

		/**
		 * Function to obtain a particular option from predefined option
		 * @param {String} optionName option which needs to be found
		 * @param {Object} predefinedOptions set of predefined option
		 * @return {Object} option
		 */
        var getOption = function (optionName, predefinedOptions) {
            var option = null;
            for (var predefinedOption of predefinedOptions) {
                if (predefinedOption.name.toLowerCase() == optionName.toLowerCase()) {
                    option = predefinedOption;
                    break;
                }
            }
            return option;
        };

		/**
		 * Function to validate the predefined options
		 * @param {Object} selectedOptions array to add the options which needs to be saved
		 * @param {Object} predefinedOptions
		 * @return {boolean} isError
		 */
        var validateOptions = function (optionsMap, predefinedOptions) {
            var isError = false;
            $('#define-store-options .option').each(function () {
                var optionName = $(this).find('.option-name').text().trim();
                var optionValue = $(this).find('.option-value').val().trim();
                var predefinedOptionObject = getOption(optionName, predefinedOptions);
                if ($(this).find('.option-name').hasClass('mandatory-option')) {
                    if (optionValue == "") {
                        $(this).find('.error-message').text('Option Value is required.');
                        $(this)[0].scrollIntoView();
                        $(this).find('.option-value').addClass('required-input-field');
                        isError = true;
                        return false;
                    } else {
                        var dataType = predefinedOptionObject.type[0];
                        if (validateDataType(dataType, optionValue)) {
                            $(this).find('.error-message').text('Invalid data-type. ' + dataType + ' required.');
                            $(this)[0].scrollIntoView();
                            $(this).find('.option-value').addClass('required-input-field');
                            isError = true;
                            return false;
                        }
                    }
                    optionsMap[optionName] = optionValue;
                } else {
                    if ($(this).find('.option-checkbox').is(":checked")) {
                        if (optionValue == "") {
                            $(this).find('.error-message').text('Option Value is required.');
                            $(this)[0].scrollIntoView();
                            $(this).find('.option-value').addClass('required-input-field');
                            isError = true;
                            return false;
                        } else {
                            var dataType = predefinedOptionObject.type[0];
                            if (validateDataType(dataType, optionValue)) {
                                $(this).find('.error-message').text('Invalid data-type. ' + dataType + ' required.');
                                $(this)[0].scrollIntoView();
                                $(this).find('.option-value').addClass('required-input-field');
                                isError = true;
                                return false;
                            }
                        }
                        optionsMap[optionName] = optionValue;
                    }
                }
            });
            return isError;
        };

        /**
        * Function to validate the customized options
        * @param {Object} selectedOptions options which needs to be saved
        * @return {boolean} isError
        */
        var validateCustomizedOptions = function (optionsMap) {
            var isError = false;
            if ($('#customized-store-options ul').has('li').length != 0) {
                $('#customized-store-options .option').each(function () {
                    var custOptName = $(this).find('.cust-option-key').val().trim();
                    var custOptValue = $(this).find('.cust-option-value').val().trim();
                    if ((custOptName != "") || (custOptValue != "")) {
                        if (custOptName == "") {
                            $(this).find('.error-message').text('Option key is required.');
                            $(this)[0].scrollIntoView();
                            $(this).find('.cust-option-key').addClass('required-input-field');
                            isError = true;
                            return false;
                        } else if (custOptValue == "") {
                            $(this).find('.error-message').text('Option value is required.');
                            $(this)[0].scrollIntoView();
                            $(this).find('.cust-option-value').addClass('required-input-field');
                            isError = true;
                            return false;
                        } else {
                            optionsMap[custOptName] = custOptValue;
                        }
                    }
                });
            }
            return isError;
        };

		/**
        * Function to validate the data type of the options
        * @param {String} dataType data-type of the option
        * @param {String} optionValue value of the option
        * @return {boolean} invalidDataType
		*/
        var validateDataType = function (dataType, optionValue) {
            var invalidDataType = false;
            intLongRegexMatch = /^[-+]?\d+$/;
            doubleFloatRegexMatch = /^[+-]?([0-9]*[.])?[0-9]+$/;

            if (dataType === "INT" || dataType === "LONG") {
                if (!optionValue.match(intLongRegexMatch)) {
                    invalidDataType = true;
                }
            } else if (dataType === "DOUBLE" || dataType === "FLOAT") {
                if (!optionValue.match(doubleFloatRegexMatch)) {
                    invalidDataType = true;
                }
            } else if (dataType === "BOOL") {
                if (!(optionValue.toLowerCase() === "false" || optionValue.toLowerCase() === "true")) {
                    invalidDataType = true;
                }
            }
            return invalidDataType;
        };

		/**
		 * Function to create option object with an additional empty value attribute
		 * @param {Object} optionArray Predefined options without the attribute 'value'
		 * @return {Object} options
		 */
        var createOptionObjectWithValues = function (optionArray) {
            var options = [];
            _.forEach(optionArray, function (option) {
                options.push({
                    key: option.name, value: "", description: option.description, optional: option.optional,
                    defaultValue: option.defaultValue
                });
            });
            return options;
        };

        /**
		 * Function to map the saved option values to the option object
		 * @param {Object} predefinedOptions Predefined options of a particular source/map annotation type
		 * @param {Object} savedOptions Saved options
		 * @return {Object} options
		*/
        var mapUserOptionValues = function (predefinedOptions, savedOptions) {
            var options = [];
            _.forEach(predefinedOptions, function (predefinedOption) {
                var foundPredefinedOption = false;
                for (var savedOption of savedOptions) {
                    if (savedOption.key.trim().toLowerCase() == predefinedOption.name.toLowerCase()) {
                        foundPredefinedOption = true;
                        options.push({
                            key: predefinedOption.name, value: savedOption.value, description: predefinedOption
                                .description, optional: predefinedOption.optional,
                            defaultValue: predefinedOption.defaultValue
                        });
                        break;
                    }
                }
                if (!foundPredefinedOption) {
                    options.push({
                        key: predefinedOption.name, value: "", description: predefinedOption
                            .description, optional: predefinedOption.optional, defaultValue: predefinedOption.defaultValue
                    });
                }
            });
            return options;
        };

        /**
		 * Function to obtain the customized option entered by the user in the source view
		 * @param {Object} predefinedOptions Predefined options of a particular store annotation type
		 * @param {Object} savedOptions saved store options
		 * @return {Object} customizedOptions
		 */
        var getCustomizedOptions = function (predefinedOptions, savedOptions) {
            var customizedOptions = [];
            _.forEach(savedOptions, function (savedOption) {
                var foundSavedOption = false;
                for (var predefinedOption of predefinedOptions) {
                    if (predefinedOption.name.toLowerCase() == savedOption.key.toLowerCase().trim()) {
                        foundSavedOption = true;
                        break;
                    }
                }
                if (!foundSavedOption) {
                    customizedOptions.push({ key: savedOption.key, value: savedOption.value });
                }
            });
            return customizedOptions;
        };

		/**
		 * Function to render the options for the selected store type using handlebars
		 * @param {Object} optionsArray Saved options
		 * @param {Object} customizedMapperOptions Options typed by the user which aren't one of the predefined option
		 * @param {String} id Id for the div to embed the options
		 */
        var renderOptions = function (optionsArray, customizedOptions, id) {
            optionsArray.sort(function (val1, val2) {
                if (val1.optional && !val2.optional) return 1;
                else if (!val1.optional && val2.optional) return -1;
                else return 0;
            });
            var storeOptionsTemplate = Handlebars.compile($('#source-sink-store-options-template').html());
            var wrappedHtml = storeOptionsTemplate({
                id: id,
                options: optionsArray,
                customizedOptions: customizedOptions
            });
            $('#define-store-options').html(wrappedHtml);
            changeCustOptDiv();
        };

		/**
		 * Function to get the options of the selected store type
		 * @param {String} selectedType Selected store type
		 * @param {object} types Predefined store types
		 * @return {object} options
		 */
        var getSelectedTypeOptions = function (selectedType, types) {
            var options = [];
            for (type of types) {
                if (type.name.toLowerCase() == selectedType.toLowerCase()) {
                    options = type.parameters;
                    break;
                }
            }
            return options;
        };

        /** Function to change the heading and the button text of the customized options div */
        var changeCustOptDiv = function () {
            var storeCustOptionList = $('.table-form-container #customized-store-options').
                find('.cust-options li');
            var storeDivParent = $('.table-form-container #customized-store-options');
            if (storeCustOptionList.length > 0) {
                storeDivParent.find('h3').show();
                storeDivParent.find('.btn-add-options').html('Add more');
            } else {
                storeDivParent.find('h3').hide();
                storeDivParent.find('.btn-add-options').
                    html('Add customized option');
            }
        };

        /**
         * Function to add a default store type to the predefined stores
         * @param {Object} predefinedStores predefined store types
         */
        var addDefaultStoreType = function (predefinedStores) {
            //first check if in-memory is already present in the predefined stores array
            var found = false;
            for (var store of predefinedStores) {
                if (store.name === defaultStoreType) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                var inMemoryType = {
                    name: defaultStoreType,
                    parameters: []
                };
                predefinedStores.push(inMemoryType);
            }
        };

        /**
         * Function to validate the attribute names
         * @param {Object} attributeNameList to add the valid attribute names
         * @return {boolean} isErrorOccurred
         */
        var validateAttributeNames = function (attributeNameList) {
            var isErrorOccurred = false;
            $('.attr-name').each(function () {
                var attributeName = $(this).val().trim();
                if (attributeName != "") {
                    if (attributeName.indexOf(' ') >= 0) {
                        $(this).parents(".attribute").find(".error-message").text("Name can not have white space");
                        $(this)[0].scrollIntoView();
                        $(this).addClass('required-input-field');
                        isErrorOccurred = true;
                        return;
                    }
                    if (!alphabeticValidatorRegex.test(attributeName.charAt(0))) {
                        $(this).parents(".attribute").find(".error-message").text("Name must start with an" +
                            " alphabetical character");
                        $(this)[0].scrollIntoView();
                        $(this).addClass('required-input-field');
                        isErrorOccurred = true;
                        return;
                    }
                    attributeNameList.push(attributeName);
                }
            });
            return isErrorOccurred;
        };

        /**
         * Function to map the values of saved annotation to predefined annotatio object
         * @param {Object} predefined_annotations
         * @param {Object} savedAnnotations
         */
        var mapAnnotationValues = function (predefined_annotations, predefinedSavedAnnotations) {
            for (var savedAnnotation of predefinedSavedAnnotations) {
                for (var predefined_annotation of predefined_annotations) {
                    if (savedAnnotation.name.toLowerCase() === predefined_annotation.name.toLowerCase()) {
                        predefined_annotation.isChecked = true;
                        predefined_annotation.values = [];
                        for (element of savedAnnotation.elements) {
                            predefined_annotation.values.push({ value: element.value });
                        }
                        break;
                    }
                }
            }
        };

        /** Function to remove the delete button of the first attribute-value */
        var changeAnnotValueDelButtons = function () {
            $('#define-predefined-annotations .table-annotation').each(function () {
                $(this).find('.btn-del-annot-value:eq(0)').remove();
            });
        };

        /**
         * Function to validate the table-annotations
         * @return {boolean} isErrorOccurred
         */
        var validateAnnotations = function () {
            var isErrorOccurred = false;
            $('#define-predefined-annotations .table-annotation').each(function () {
                var annotationValues = [];
                if ($(this).find('.annotation-checkbox').is(':checked')) {
                    $(this).find('.annotation-value').each(function () {
                        if ($(this).val().trim() != "") {
                            annotationValues.push($(this).val());
                        }
                    });
                    if (annotationValues.length == 0) {
                        $(this).find('.annotation-value:eq(0)').addClass('required-input-field');
                        $(this).find('.error-message:eq(0)').text("Minimum one value is required");
                        isErrorOccurred = true;
                        return false;
                    }
                }
            });
            return isErrorOccurred;
        };

        /**
         * Function to build the table annotation as a string
         * @param {Object} annotationList array to add the built string annotations
         */
        var buildAnnotations = function (annotationList, annotationObjectList) {
            $('#define-predefined-annotations .table-annotation').each(function () {
                var annotationObject = new AnnotationObject();
                if ($(this).find('.annotation-checkbox').is(':checked')) {
                    var annotName = $(this).find('.annotation-name').text().trim();
                    annotationObject.setName(annotName.substring(1))
                    var annotation = annotName + "(";
                    $(this).find('.annotation-value').each(function () {
                        var annotValue = $(this).val().trim();
                        if (annotValue != "") {
                            var element = new AnnotationElement();
                            element.setValue(annotValue)
                            annotationObject.addElement(element);
                            annotation += "'" + annotValue + "' ,";
                        }
                    });
                    annotation = annotation.substring(0, annotation.length - 1);
                    annotation += ")";
                    annotationObjectList.push(annotationObject);
                    annotationList.push(annotation);
                }
            });
        };

        /**
         * Function to render the html to display the radio options for selecting the rdbms type
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
         * Function to select the options according to the selected rdbms type
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
         * Function to check the radio button of the selected rdbms type
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
		* Function to obtain the user defined annotations from the saved annotations
		* @param {Object} savedAnnotationObjects saved annotation objects
		* @param {Object} tableAnnotations predefined table annotations
		* @return {Object} userAnnotations
		*/
        var getUserAnnotations = function (savedAnnotationObjects, tableAnnotations) {
            var userAnnotations = [];
            _.forEach(savedAnnotationObjects, function (savedAnnotation) {
                var isPredefined = false;
                _.forEach(tableAnnotations, function (tableAnnotation) {
                    if (savedAnnotation.name.toLowerCase() === tableAnnotation.name.toLowerCase()) {
                        isPredefined = true;
                        return false;
                    }
                });
                if (!isPredefined) {
                    userAnnotations.push(savedAnnotation);
                }
            });
            return userAnnotations;
        };

        /**
		 * Function to initialize the jstree
		 * Function to add the event listeners for the jstree -div
		 */
        var loadAnnotation = function () {
            //initialise jstree
            $("#annotation-div").jstree({
                "core": {
                    "check_callback": true
                },
                "themes": {
                    "theme": "default",
                    "url": "editor/commons/lib/js-tree-v3.3.2/themes/default/style.css"
                },
                "checkbox": {
                    "three_state": false,
                    "whole_node": false,
                    "tie_selection": false
                },
                "plugins": ["themes", "checkbox"]
            });

            var tree = $('#annotation-div').jstree(true);

            //to add key-value for annotation node
            $("#btn-add-key-val").on("click", function () {
                var selectedNode = $("#annotation-div").jstree("get_selected");
                tree.create_node(selectedNode,
                    {
                        text: "property", class: "annotation-key", state: { "opened": true },
                        "a_attr": { "class": "annotation-key" }, icon: "/editor/commons/images/properties.png",
                        children: [{
                            text: "value", class: "annotation-value", "a_attr": { "class": "annotation-value" },
                            icon: "/editor/commons/images/value.png"
                        }]
                    }
                );
                tree.open_node(selectedNode);
                tree.deselect_all();
            });

            //to add annotation node
            $("#btn-add-annotation").on("click", function () {
                var selectedNode = $("#annotation-div").jstree("get_selected");
                if (selectedNode == "") {
                    selectedNode = "#"
                }
                tree.create_node(selectedNode, {
                    text: "Annotation", class: "annotation", state: { "opened": true },
                    "a_attr": { "class": "annotation" }, icon: "/editor/commons/images/annotation.png",
                    children: [{
                        text: "property", class: "annotation-key", icon: "/editor/commons/images/properties.png",
                        "a_attr": { "class": "annotation-key" },
                        children: [{
                            text: "value", class: "annotation-value", "a_attr": { "class": "annotation-value" },
                            icon: "/editor/commons/images/value.png"
                        }]
                    }]

                });
                tree.open_node(selectedNode);
                tree.deselect_all();
            });

            //to delete an annotation or a key-value node
            $("#btn-del-annotation").on("click", function () {
                var selectedNode = $("#annotation-div").jstree("get_selected");
                tree.delete_node([selectedNode]);
                tree.deselect_all();
            })

            //to edit the selected node
            //to hide/show the buttons corresponding to the node selected
            $('#annotation-div').on("select_node.jstree", function (e, data) {
                var node_info = $('#annotation-div').jstree("get_node", data.node)
                if ((node_info.original != undefined && (node_info.original.class == "annotation")) ||
                    (node_info.li_attr != undefined && (node_info.li_attr.class == "annotation"))) {
                    tree.edit(data.node)
                    $("#btn-del-annotation").show();
                    $("#btn-add-annotation").show();
                    $("#btn-add-key-val").show();

                } else if ((node_info.original != undefined && (node_info.original.class == "annotation-key")) ||
                    (node_info.li_attr != undefined && (node_info.li_attr.class == "annotation-key"))) {
                    tree.edit(data.node);
                    $("#btn-del-annotation").show();
                    $("#btn-add-annotation").hide();
                    $("#btn-add-key-val").hide();

                } else if ((node_info.original != undefined && (node_info.original.class == "annotation-value")) ||
                    (node_info.li_attr != undefined && (node_info.li_attr.class == "annotation-value"))) {
                    $("#btn-del-annotation").hide();
                    $("#btn-add-annotation").hide();
                    $("#btn-add-key-val").hide();
                    tree.edit(data.node);
                }
            });

            //to unselect the nodes when user clicks other than the nodes in jstree
            $(document).on('click', function (e) {
                if ($(e.target).closest('.jstree').length) {
                    $("#btn-del-annotation").hide();
                    $("#btn-add-annotation").show();
                    $("#btn-add-key-val").hide();
                    tree.deselect_all();
                }
            });
        };

        /**
         * Function to build the annotations as a string
         * Function to create the annotation objects
         * @param {Object} annotationStringList array to add the built annotation strings
         * @param {Object} annotationObjectList array to add the created annotation objects
         */
        var annotation = "";
        var buildUserAnnotations = function (annotationStringList, annotationObjectList) {
            var jsTreeNodes = $('#annotation-div').jstree(true)._model.data['#'].children;
            _.forEach(jsTreeNodes, function (node) {
                var node_info = $('#annotation-div').jstree("get_node", node);
                var childArray = node_info.children
                if (childArray.length != 0) {
                    annotation += "@" + node_info.text.trim() + "( "
                    //create annotation object
                    var annotationObject = new AnnotationObject();
                    annotationObject.setName(node_info.text.trim())
                    traverseChildAnnotations(childArray, annotationObject)
                    annotation = annotation.substring(0, annotation.length - 1);
                    annotation += ")"
                    annotationObjectList.push(annotationObject)
                    annotationStringList.push(annotation);
                    annotation = "";
                }
            });
        };

        /**
         * Function to traverse the children of the parent annotaions
         * @param {Object} children the children of a parent annotation node
         * @param {Object} annotationObject the parent's annotation object
         */
        var traverseChildAnnotations = function (children, annotationObject) {
            children.forEach(function (node) {
                node_info = $('#annotation-div').jstree("get_node", node);
                //if the child is a sub annotation
                if ((node_info.original != undefined && node_info.original.class == "annotation") ||
                    (node_info.li_attr != undefined && (node_info.li_attr.class == "annotation" ||
                        node_info.li_attr.class == "optional-annotation" || node_info.li_attr.class ==
                        "mandatory-annotation"))) {
                    if (node_info.children.length != 0) {
                        annotation += "@" + node_info.text.trim() + "( "
                        var childAnnotation = new AnnotationObject();
                        childAnnotation.setName(node_info.text.trim())
                        traverseChildAnnotations(node_info.children, childAnnotation)
                        annotationObject.addAnnotation(childAnnotation)
                        annotation = annotation.substring(0, annotation.length - 1);
                        annotation += "),"
                    }
                } else {
                    //if the child is a property
                    if (node_info.li_attr.class != undefined && (node_info.li_attr.class == "optional-key")
                        && node_info.state.checked == false) {
                        //not to add the child property if it hasn't been checked(predefined optional-key only)
                    } else {
                        annotation += node_info.text.trim() + "="
                        var node_value = $('#annotation-div').jstree("get_node", node_info.children[0]).text.trim();
                        annotation += "'" + node_value + "' ,";
                        var element = new AnnotationElement(node_info.text.trim(), node_value)
                        annotationObject.addElement(element);
                    }
                }
            });
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
            $('#' + id).addClass('selected-element');
            $(".overlayed-container").fadeTo(200, 1);
            var propertyDiv = $('<div class = "table-form-container table-div"> <div id="property-header"> <h3> Table' +
                ' Configuration </h3> </div> <h4> Name: </h4> <input type="text" id="tableName" class = "clearfix">' +
                '<label class="error-message" id="tableNameErrorMessage"> </label> <div id = "define-attribute"> </div>' +
                '<button id = "btn-submit" type = "button" class = "btn toggle-view-button"> Submit </button> </div> ' +
                '<div class = "table-form-container store-div"> <div id = "define-store"> </div>  ' +
                '<div id="define-rdbms-type"> </div> <div id="define-store-options"> </div> </div> ' +
                '<div class = "table-form-container define-table-annotation">' +
                '<div id = "define-predefined-annotations"> </div> <div id = "define-user-annotations"> </div> </div>');
            formContainer.append(propertyDiv);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var predefinedStores = _.orderBy(this.configurationData.rawExtensions["store"], ['name'], ['asc']);
            addDefaultStoreType(predefinedStores);
            var customizedStoreOptions = [];
            var storeOptions = [];
            var storeOptionsWithValues = [];

            /** Event listeners */

            //To add attribute
            $("#define-attribute").on('click', '#btn-add-attribute', function () {
                $("#attribute-div").append('<li class="attribute clearfix"><div class="clearfix"> ' +
                    '<div class="attr-content">' +
                    '<input type="text" value="" class="attr-name"/> ' +
                    '<select class="attr-type">' +
                    '<option value="string">string</option>' +
                    '<option value="int">int</option>' +
                    '<option value="long">long</option>' +
                    '<option value="float">float</option>' +
                    '<option value="double">double</option>' +
                    '<option value="bool">bool</option>' +
                    '<option value="object">object</option>' +
                    '</select>' +
                    '</div> <div class="attr-nav"> </div></div>' +
                    '<label class="error-message"></label></li>');
                changeAtrributeNavigation();
            });

            //To delete attribute
            $("#define-attribute").on('click', '#attribute-div .btn-del-attr', function () {
                $(this).closest('li').remove();
                changeAtrributeNavigation();
            });

            //To reorder up the attribute
            $("#define-attribute").on('click', ' #attribute-div .reorder-up', function () {
                var $current = $(this).closest('li');
                var $previous = $current.prev('li');
                if ($previous.length !== 0) {
                    $current.insertBefore($previous);
                }
                changeAtrributeNavigation();

            });

            //To reorder down the attribute
            $("#define-attribute").on('click', ' #attribute-div .reorder-down', function () {
                var $current = $(this).closest('li');
                var $next = $current.next('li');
                if ($next.length !== 0) {
                    $current.insertAfter($next);
                }
                changeAtrributeNavigation();
            });

            //To show option description
            $('#define-store-options').on('mouseover', '.option-desc', function () {
                $(this).find('.option-desc-content').show();
            });

            //To hide option description
            $('#define-store-options').on('mouseout', '.option-desc', function () {
                $(this).find('.option-desc-content').hide();
            });

            //To hide and show the option content of the optional options
            $('#define-store-options').on('change', '.option-checkbox', function () {
                if ($(this).is(':checked')) {
                    $(this).parents(".option").find(".option-value").show();
                } else {
                    $(this).parents(".option").find(".option-value").hide();
                    if ($(this).parents(".option").find(".option-value").hasClass("required-input-field")) {
                        $(this).parents(".option").find(".option-value").removeClass("required-input-field");
                    }
                    $(this).parents(".option").find(".error-message").text("");
                }
            });

            //To add customized option
            $('#define-store-options').on('click', '#btn-add-store-options', function () {
                var custOptDiv = '<li class="option">' +
                    '<div class = "clearfix"> <label>option.key</label> <input type="text" class="cust-option-key"' +
                    'value=""> </div> <div class="clearfix"> <label>option.value</label> ' +
                    '<input type="text" class="cust-option-value" value="">' +
                    '<a class = "btn-del btn-del-option"><i class="fw fw-delete"></i></a></div>' +
                    '<label class = "error-message"></label></li>';
                $('#customized-store-options .cust-options').append(custOptDiv);
                changeCustOptDiv();
            });

            //To delete customized option
            $('#define-store-options').on('click', '.btn-del-option', function () {
                $(this).closest('li').remove();
                changeCustOptDiv();
            });

            //To add annotation value
            $('#define-predefined-annotations').on('click', '.btn-add-annot-value', function () {
                $(this).parents(".table-annotation").find("ul").append
                    ('<li class = "clearfix table-annotation-value"> <div class="clearfix"> ' +
                    '<input type = "text" value = "" class = "annotation-value"/> ' +
                    '<a class = "btn-del-annot-value"> <i class = "fw fw-delete"> </i> </a> </div> ' +
                    '<label class="error-message"></label> </li>');
            });

            //To delete annotation value
            $('#define-predefined-annotations').on('click', '.btn-del-annot-value', function () {
                $(this).closest('li').remove();
            });

            // To show the values of the primaryKey and index annotations on change of the checkbox
            $('#define-predefined-annotations').on('change', '.annotation-checkbox', function () {
                if ($(this).is(':checked')) {
                    $(this).parents(".table-annotation").find('.annotation-content').show();
                } else {
                    $(this).parents(".table-annotation").find('.annotation-content').hide();
                    $(this).parents(".table-annotation").find('.error-message').text("");
                    if ($(this).parents(".table-annotation").find('.annotation-value').hasClass('required-input-field')) {
                        $(this).parents(".table-annotation").find('.annotation-value').removeClass('required-input-field')
                    }
                }
            });

            var name = clickedElement.getName();
            if (!name) {
                var attributeFormTemplate = Handlebars.compile($('#attribute-form-template').html());
                var wrappedHtml = attributeFormTemplate([{ name: "", type: "string" }]);
                $('#define-attribute').html(wrappedHtml);
            } else {
                $('#tableName').val(name);
                var savedAttributes = clickedElement.getAttributeList();

                //render the attribute form template
                var attributeFormTemplate = Handlebars.compile($('#attribute-form-template').html());
                var wrappedHtml = attributeFormTemplate(savedAttributes);
                $('#define-attribute').html(wrappedHtml);
                changeAtrributeNavigation();
                //to select the attribute-type of the saved attributes
                var i = 0;
                $('.attribute .attr-content').each(function () {
                    $(this).find('.attr-type option').filter(function () {
                        return ($(this).val() == (savedAttributes[i].getType()).toLowerCase());
                    }).prop('selected', true);
                    i++;
                });
            }

            var savedAnnotations = clickedElement.getAnnotationList();
            var savedAnnotationObjects = clickedElement.getAnnotationListObjects();
            var userAnnotations = [];
            var tableAnnotations = self.configurationData.application.config.table_predefined_annotations;
            if (savedAnnotations || savedAnnotations.length != 0) {
                userAnnotations = getUserAnnotations(savedAnnotationObjects, tableAnnotations);
                mapAnnotationValues(tableAnnotations, savedAnnotationObjects)
            }
            //render the predefined table annotation form template
            var annotationFormTemplate = Handlebars.compile($('#table-store-annotation-template').html());
            var wrappedHtml = annotationFormTemplate(tableAnnotations);
            $('#define-predefined-annotations').html(wrappedHtml);
            changeAnnotValueDelButtons();

            //render the user defined annotations form template
            var raw_partial = document.getElementById('recursiveAnnotationPartial').innerHTML;
            Handlebars.registerPartial('recursiveAnnotation', raw_partial);
            var annotationFormTemplate = Handlebars.compile($('#annotation-form-template').html());
            var wrappedHtml = annotationFormTemplate(userAnnotations);
            $('#define-user-annotations').html(wrappedHtml);
            loadAnnotation();
            $('#define-user-annotations').find('h4').html('Customized Annotations');

            //render the template to  generate the store types
            var storeFormTemplate = Handlebars.compile($('#type-selection-form-template').html());
            var wrappedHtml = storeFormTemplate({ id: "store", types: predefinedStores });
            $('#define-store').html(wrappedHtml);

            $('#define-rdbms-type').on('change', '[name=radioOpt]', function () {
                var dataStoreOptions = getRdbmsOptions(storeOptionsWithValues);
                renderOptions(dataStoreOptions, customizedStoreOptions, "store")
            });

            //if store is defined
            if (clickedElement.getStore()) {
                var savedStoreAnnotation = clickedElement.getStore();
                var savedStoreType = savedStoreAnnotation.getType().toLowerCase();
                storeOptions = getSelectedTypeOptions(savedStoreType, predefinedStores);
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
                customizedStoreOptions = getCustomizedOptions(storeOptions, savedStoreOptions);
                storeOptionsWithValues = mapUserOptionValues(storeOptions, savedStoreOptions);
                if (savedStoreType == rdbmsStoreType) {
                    renderRdbmsTypes();
                    checkRdbmsType(storeOptionsWithValues);
                    var dataStoreOptions = getRdbmsOptions(storeOptionsWithValues);
                    renderOptions(dataStoreOptions, customizedStoreOptions, "store");
                } else {
                    renderOptions(storeOptionsWithValues, customizedStoreOptions, "store");
                }
                $('#define-predefined-annotations').show();
            } else {
                $('#define-store #store-type').val(defaultStoreType);
                $('#define-predefined-annotations').hide();
            }

            //onchange of the store type select box
            $('#define-store').on('change', '#store-type', function () {
                if (this.value === defaultStoreType) {
                    $('#define-store-options').empty();
                    $('#define-predefined-annotations').hide();
                    $('#define-rdbms-type').hide();
                } else if (clickedElement.getStore() && savedStoreType === this.value) {
                    storeOptions = getSelectedTypeOptions(this.value, predefinedStores);
                    customizedStoreOptions = getCustomizedOptions(storeOptions, savedStoreOptions);
                    storeOptionsWithValues = mapUserOptionValues(storeOptions, savedStoreOptions);
                    if (this.value == rdbmsStoreType) {
                        $('#define-rdbms-type').show();
                        renderRdbmsTypes();
                        var dataStoreOptions = getRdbmsOptions(storeOptionsWithValues);
                        checkRdbmsType(dataStoreOptions);
                        renderOptions(dataStoreOptions, customizedStoreOptions, "store");
                    } else {
                        $('#define-rdbms-type').hide();
                        renderOptions(storeOptionsWithValues, customizedStoreOptions, "store");
                    }
                    $('#define-predefined-annotations').show();
                } else {
                    storeOptions = getSelectedTypeOptions(this.value, predefinedStores);
                    storeOptionsWithValues = createOptionObjectWithValues(storeOptions);
                    customizedStoreOptions = [];
                    if (this.value == rdbmsStoreType) {
                        renderRdbmsTypes();
                        //as default select the data-store type
                        $("#define-rdbms-type input[name=radioOpt][value='inline-config']").prop("checked", true);
                        var dataStoreOptions = getRdbmsOptions(storeOptionsWithValues);
                        renderOptions(dataStoreOptions, customizedStoreOptions, "store")
                        $('#define-rdbms-type').show();
                    } else {
                        $('#define-rdbms-type').hide();
                        renderOptions(storeOptionsWithValues, customizedStoreOptions, "store");
                    }
                    $('#define-predefined-annotations').show();
                }
            });

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#btn-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                //clear the error classes
                $('.error-message').text("");
                $('#tableNameErrorMessage').text("");
                $('.required-input-field').removeClass('required-input-field');

                var tableName = $('#tableName').val().trim();

                // to check if stream name is empty
                if (tableName == "") {
                    $('#tableName').addClass('required-input-field');
                    $('#tableName')[0].scrollIntoView();
                    $('#tableNameErrorMessage').text("Table name is required");
                    return;
                }
                var previouslySavedName = clickedElement.getName();
                if (!previouslySavedName) {
                    previouslySavedName = "";
                }

                if (previouslySavedName !== tableName) {
                    var isTableNameUsed = self.formUtils.isDefinitionElementNameUsed(tableName);
                    if (isTableNameUsed) {
                        $('#tableName').addClass('required-input-field');
                        $('#tableName')[0].scrollIntoView();
                        $('#tableNameErrorMessage').text("Table name is already used.");
                        return;
                    }
                    //to check if stream name contains white spaces
                    if (tableName.indexOf(' ') >= 0) {
                        $('#tableName').addClass('required-input-field');
                        $('#tableName')[0].scrollIntoView();
                        $('#tableNameErrorMessage').text("Table name cannot have white space.");
                        return;
                    }
                    //to check if stream name starts with an alphabetic character
                    if (!(alphabeticValidatorRegex).test(tableName.charAt(0))) {
                        $('#tableName').addClass('required-input-field');
                        $('#tableName')[0].scrollIntoView();
                        $('#tableNameErrorMessage').text("Table name must start with an alphabetic character.");
                        return;
                    }
                    // update selected table model
                    clickedElement.setName(tableName);
                    // update connection related to the element if the name is changed
                    self.formUtils.updateConnectionsAfterDefinitionElementNameChange(id);
                }

                //add the store annotation
                var selectedStoreType = $('#define-store #store-type').val();
                if (selectedStoreType !== defaultStoreType) {
                    var optionsMap = {};
                    if (validateOptions(optionsMap, storeOptions)) {
                        return;
                    }
                    if (validateCustomizedOptions(optionsMap)) {
                        return;
                    }
                    var storeAnnotationOptions = {};
                    _.set(storeAnnotationOptions, 'type', selectedStoreType);
                    _.set(storeAnnotationOptions, 'options', optionsMap);
                    var storeAnnotation = new StoreAnnotation(storeAnnotationOptions);
                    clickedElement.setStore(storeAnnotation);
                } else {
                    clickedElement.setStore(undefined);
                }

                var attributeNameList = [];
                if (validateAttributeNames(attributeNameList)) { return; }

                if (attributeNameList.length == 0) {
                    $('.attribute:eq(0)').find('.attr-name').addClass('required-input-field');
                    $('.attribute:eq(0)').find('.attr-name')[0].scrollIntoView();
                    $('.attribute:eq(0)').find('.error-message').text("Minimum one attribute is required");
                    return;
                } else {
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
                }

                var annotationList = [];
                var annotationObjectList = [];
                if (validateAnnotations()) {
                    return;
                } else {
                    //clear the annotationlist
                    clickedElement.clearAnnotationList();
                    clickedElement.clearAnnotationListObjects();
                    buildAnnotations(annotationList, annotationObjectList);
                    buildUserAnnotations(annotationList, annotationObjectList)
                    //add the annotations to the clicked element
                    _.forEach(annotationList, function (annotation) {
                        clickedElement.addAnnotation(annotation);
                    });
                    _.forEach(annotationObjectList, function (annotation) {
                        clickedElement.addAnnotationObject(annotation);
                    });
                }

                // set the isDesignViewContentChanged to true
                self.configurationData.setIsDesignViewContentChanged(true);

                var textNode = $('#' + id).find('.tableNameNode');
                textNode.html(tableName);
                if ($('#' + id).hasClass('incomplete-element')) {
                    $('#' + id).removeClass('incomplete-element');
                }
                $('#' + id).prop('title', '');
                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);

                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');
            });
        };

        return TableForm;
    });
