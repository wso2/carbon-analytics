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

define(['require', 'log', 'jquery', 'lodash', 'attribute', 'designViewUtils', 'handlebar', 'annotationObject',
    'annotationElement'],
    function (require, log, $, _, Attribute, DesignViewUtils, Handlebars, AnnotationObject, AnnotationElement) {

        /**
         * @class WindowForm Creates a forms to collect data from a window
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var WindowForm = function (options) {
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
        const sort = "sort";
        const frequent = "frequent";
        const lossyFrequent = "lossyFrequent";


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
                        $(this).parents(".attribute").find(".error-message").text("Name can not have white space")
                        $(this)[0].scrollIntoView();
                        $(this).addClass('required-input-field')
                        isErrorOccurred = true;
                        return;
                    }
                    if (!alphabeticValidatorRegex.test(attributeName.charAt(0))) {
                        $(this).parents(".attribute").find(".error-message").text("Name must start with an" +
                            " alphabetical character");
                        $(this)[0].scrollIntoView();
                        $(this).addClass('required-input-field')
                        isErrorOccurred = true;
                        return;
                    }
                    attributeNameList.push(attributeName)
                }
            });
            return isErrorOccurred;
        };

        /**
		 * Function to obtain a particular parameter from predefined parameters
		 * @param {String} parameterName parameter which needs to be found
		 * @param {Object} predefinedParameters set of predefined parameters
		 * @return {Object} parameter
		 */
        var getParameter = function (parameterName, predefinedParameters) {
            var parameter = null;
            for (var predefinedParameter of predefinedParameters) {
                if (predefinedParameter.name.toLowerCase() == parameterName.toLowerCase()) {
                    parameter = predefinedParameter;
                    break;
                }
            }
            return parameter;
        };

		/**
		 * Function to render the parameter for the selected window function using handlebars
		 * @param {Object} parameterArray Saved parameters
		 * @param {Object} windowFunctionName selected window processor type
		 * @param {String} id Id for the div to embed the parameters
		 */
        var renderParameters = function (parameterArray, windowFunctionName, id) {
            parameterArray.sort(function (val1, val2) {
                if (val1.optional && !val2.optional) return 1;
                else if (!val1.optional && val2.optional) return -1;
                else return 0;
            });
            var parameterTemplate = Handlebars.compile($('#window-function-parameters-template').html());
            var wrappedHtml = parameterTemplate({
                id: id,
                windowFunctionName: windowFunctionName,
                parameters: parameterArray
            });
            $('#defineFunctionParameters').html(wrappedHtml);
        };

		/**
		 * Function to get the parameters of the selected window function
		 * @param {String} selectedType Selected window function type
		 * @param {object} types Predefined window types
		 * @return {object} parameters
		 */
        var getSelectedTypeParameters = function (selectedType, types) {
            var parameters = [];
            for (type of types) {
                if (type.name.toLowerCase() == selectedType.toLowerCase()) {
                    if (type.parameters) {
                        parameters = type.parameters;
                    }
                    break;
                }
            }
            return parameters;
        };

		/**
		 * Function to create parameter object with an additional empty value attribute
		 * @param {Object} parameterArray Predefined parameters without the attribute 'value'
		 * @return {Object} parameters
		 */
        var createParameterWithValues = function (parameterArray) {
            var parameters = [];
            _.forEach(parameterArray, function (parameter) {
                parameters.push({
                    name: parameter.name, value: "", description: parameter.description, optional: parameter.optional,
                    defaultValue: parameter.defaultValue
                });
            });
            return parameters;
        };

        /**
        * Function to map the saved parameter values to the parameter object
        * @param {Object} predefinedParameters Predefined parameters of a particular window type
        * @param {Object} savedParameterValues Saved parameter values
        * @return {Object} parameters
        */
        var mapUserParameterValues = function (predefinedParameters, savedParameterValues) {
            var parameters = [];
            for (var i = 0; i < predefinedParameters.length; i++) {
                var timeStamp = "";
                if (i < savedParameterValues.length) {
                    var parameterValue = savedParameterValues[i];
                    if (predefinedParameters[i].type.includes("STRING")) {
                        parameterValue = parameterValue.slice(1, parameterValue.length - 1)
                    }
                    parameters.push({
                        name: predefinedParameters[i].name, value: parameterValue, description:
                            predefinedParameters[i].description, optional: predefinedParameters[i].optional,
                        defaultValue: predefinedParameters[i].defaultValue, timeStamp: timeStamp
                    });
                } else {
                    parameters.push({
                        name: predefinedParameters[i].name, value: "", description: predefinedParameters[i]
                            .description, optional: predefinedParameters[i].optional,
                        defaultValue: predefinedParameters[i].defaultValue, timeStamp: timeStamp
                    });
                }
            }
            return parameters;
        };

        /** Function to render the output event types */
        var renderOutputEventTypes = function () {
            var outputEventDiv = '<div class = "clearfix"> <label>Event Type </label></div>' +
                '<div class = "clearfix" ><select id="event-type">' +
                '<option value = "all_events"> all events </option>' +
                '<option value = "current_events"> current events </option>' +
                '<option value = "expired_events"> expired events </option>' +
                '</select> </div>'
            $('#defineOutputEvents').html(outputEventDiv);
        };

        /**
        * Function to validate the data type of the parameters
        * @param {String} dataType data-type of the parameter
        * @param {String} parameterValue value of the parameter value
        * @return {boolean} invalidDataType
        */
        var validateDataType = function (dataTypes, parameterValue) {
            var invalidDataType;
            const intLongRegexMatch = /^[-+]?\d+$/;
            const doubleFloatRegexMatch = /^[+-]?([0-9]*[.])?[0-9]+$/;
            const timeRegexMatch = /^[_A-z0-9]*((-|\s)*[_A-z0-9])*$/g;

            for (var dataType of dataTypes) {
                if (dataType === "INT" || dataType === "LONG") {
                    if (!parameterValue.match(intLongRegexMatch)) {
                        invalidDataType = true;
                    } else {
                        invalidDataType = false;
                        break;
                    }
                } else if (dataType === "DOUBLE" || dataType === "FLOAT") {
                    if (!parameterValue.match(doubleFloatRegexMatch)) {
                        invalidDataType = true;
                    } else {
                        invalidDataType = false;
                        break;
                    }
                } else if (dataType === "BOOL") {
                    if (!(parameterValue.toLowerCase() === "false" || parameterValue.toLowerCase() === "true")) {
                        invalidDataType = true;
                    } else {
                        invalidDataType = false;
                        break;
                    }
                } else if (dataType === "TIME") {
                    if (!parameterValue.match(timeRegexMatch)) {
                        invalidDataType = true;
                    } else {
                        invalidDataType = false;
                        break;
                    }
                }
            }
            return invalidDataType;
        };

        /**
         * Function to validate the parameter values
         * @param {Object} parent the parameter div
         * @param {Object} predefinedParameters predefined parameters
         * @return {boolean} valid or invalid parameter value
         */
        var validateParameterValues = function (parent, predefinedParameter) {
            var parameterName = $(parent).find('.parameter-name').text().trim();
            var parameterValue = $(parent).find('.parameter-value').val().trim();
            if (!predefinedParameter.optional) {
                if (parameterValue == "") {
                    $(parent).find('.error-message').text('Parameter Value is required.');
                    $(parent)[0].scrollIntoView();
                    $(parent).find('.parameter-value').addClass('required-input-field');
                    return false;
                } else {
                    var dataType = predefinedParameter.type;
                    if (validateDataType(dataType, parameterValue)) {
                        $(parent).find('.error-message').text('Invalid data-type. ');
                        $(parent)[0].scrollIntoView();
                        $(parent).find('.parameter-value').addClass('required-input-field');
                        return false;
                    }
                }
            } else {
                if ($(parent).find('.parameter-checkbox').is(":checked")) {
                    if (parameterValue == "") {
                        $(parent).find('.error-message').text('Parameter Value is required.');
                        $(parent)[0].scrollIntoView();
                        $(parent).find('.parameter-value').addClass('required-input-field');
                        return false;
                    } else {
                        var dataType = predefinedParameter.type;
                        if (validateDataType(dataType, parameterValue)) {
                            $(parent).find('.error-message').text('Invalid data-type');
                            $(parent)[0].scrollIntoView();
                            $(parent).find('.parameter-value').addClass('required-input-field');
                            return false;
                        }
                    }
                }
            }
            return true;
        };

        /**
         * Function to build the parameter values
         * @param {Object} parameterValues array to add the parameters
         * @param {Object} predefinedParameters predefined parameters
         * @return {boolean} isError
         */
        var buildParameterValues = function (parameterValues, predefinedParameters) {
            var isError = false;
            $('.parameter').each(function () {
                if ($(this).find('.parameter-name').hasClass('mandatory-parameter') || ($(this).find('.parameter-name')
                    .hasClass('optional-parameter') && $(this).find('.parameter-checkbox').is(":checked"))) {
                    var parameterValue = $(this).find('.parameter-value').val().trim();
                    var parameterName = $(this).find('.parameter-name').text().trim();;
                    var predefinedParameter = getParameter(parameterName, predefinedParameters);
                    if (validateParameterValues(this, predefinedParameter)) {
                        if (predefinedParameter.type.includes("STRING")) {
                            parameterValue = "'" + parameterValue + "'";
                        }
                        parameterValues.push(parameterValue)
                    } else {
                        isError = true;
                        return false;
                    }
                }
            });
            return isError;
        };

        /**
         * Function to build parameters for frequent and lossyFrequent type
         * @param {Object} parameterValues array to add the parameters
         * @param {Object} predefinedParameters predefined parameters
         * @return {boolean} isError
         */
        var buildParameterValuesFrequentOrLossyFrequent = function (parameterValues, predefinedParameters) {
            var isError = false;
            $('.parameter').each(function () {
                if ($(this).find('.parameter-name').hasClass('mandatory-parameter') || ($(this).find('.parameter-name')
                    .hasClass('optional-parameter') && $(this).find('.parameter-checkbox').is(":checked"))) {
                    var parameterValue = $(this).find('.parameter-value').val().trim();
                    var parameterName = $(this).find('.parameter-name').text().trim();
                    var predefinedParameter = getParameter(parameterName, predefinedParameters);
                    if (validateParameterValues(this, predefinedParameter)) {
                        if (parameterName === "attribute") {
                            var attributeArray = parameterValue.split(',');
                            _.forEach(attributeArray, function (attribute) {
                                parameterValues.push(attribute.trim())
                            });
                        } else {
                            if (predefinedParameter.type.includes("STRING")) {
                                parameterValue = "'" + parameterValue + "'";
                            }
                            parameterValues.push(parameterValue)
                        }
                    } else {
                        isError = true;
                        return false;
                    }
                }
            });
            return isError;
        };

        /**
         * Function to build parameters for sort type
         * @param {Object} parameterValues array to add the parameters
         * @param {Object} predefinedParameters predefined parameters
         * @return {boolean} isError
         */
        var buildParameterValuesSort = function (parameterValues, predefinedParameters) {
            var isError = false;
            $('.parameter').each(function () {
                var parameterValue = $(this).find('.parameter-value').val().trim();
                var parameterName = $(this).find('.parameter-name').text().trim();;
                var predefinedParameter = getParameter(parameterName, predefinedParameters);
                if (parameterName === "window.length") {
                    if (validateParameterValues(this, predefinedParameter)) {
                        parameterValues.push(parameterValue)
                    } else {
                        isError = true;
                        return false;
                    }
                } else if (parameterName === "attribute") {
                    if ($('#attribute-parameter').find('.parameter-checkbox').is(":checked")) {
                        if (validateParameterValues(this, predefinedParameter)) {
                            var attributeArray = parameterValue.split(',');
                            _.forEach(attributeArray, function (attribute) {
                                parameterValues.push(attribute.trim())
                            });
                        } else {
                            isError = true;
                            return false;
                        }
                    }
                } else {
                    if (($('#attribute-parameter').find('.parameter-checkbox').is(":checked")) && ($
                        ('#order-parameter').find('.parameter-checkbox').is(":checked"))) {
                        if (validateParameterValues(this, predefinedParameter)) {
                            if (parameterValue.toLowerCase() === "asc" ||
                                parameterValue.toLowerCase() === "desc") {
                                parameterValue = "'" + parameterValue + "'";
                                parameterValues.push(parameterValue)
                            } else {
                                $(this).find('.error-message').text("asc or desc is required.");
                                $(this)[0].scrollIntoView();
                                $(this).find('.parameter-value').addClass('required-input-field');
                                isError = true;
                                return false;
                            }
                        } else {
                            isError = true;
                            return false;
                        }
                    }
                }
            });
            return isError;
        };

        /**
         * Function to map the user saved parameters of lossyFrequent
         * @param {Object} predefinedParameters predefined parameters
         * @param {Object} savedParameterValues user saved parameters
         * @return {Object} parameters
         */
        var mapParameterValuesLossyFrequent = function (predefinedParameters, savedParameterValues) {
            var parameters = [];
            var attributes = "";
            //add the two mandatory params of the saved values to the predefined param objects
            for (var i = 0; i <= 1; i++) {
                parameters.push({
                    name: predefinedParameters[i].name, value: savedParameterValues[i], description:
                        predefinedParameters[i].description, optional: predefinedParameters[i].optional,
                    defaultValue: predefinedParameters[i].defaultValue
                });
            }
            // add the attributes
            for (var i = 2; i < savedParameterValues.length; i++) {
                attributes += savedParameterValues[i] + ", "
            }
            //cutting off the last white space and the comma
            attributes = attributes.substring(0, attributes.length - 2);
            //add the attributes to the third obj of the predefined parameter
            parameters.push({
                name: predefinedParameters[2].name, value: attributes, description:
                    predefinedParameters[2].description, optional: predefinedParameters[2].optional,
                defaultValue: predefinedParameters[2].defaultValue
            });
            return parameters;
        };

        /**
         * Function to map the user saved parameters of frequent
         * @param {Object} predefinedParameters predefined parameters
         * @param {Object} savedParameterValues user saved parameters
         * @return {Object} parameters
         */
        var mapParameterValuesFrequent = function (predefinedParameters, savedParameterValues) {
            var parameters = [];
            var attributes = "";
            //add the first saved param to predefined param's first index (event.count)
            parameters.push({
                name: predefinedParameters[0].name, value: savedParameterValues[0], description:
                    predefinedParameters[0].description, optional: predefinedParameters[0].optional,
                defaultValue: predefinedParameters[0].defaultValue
            });
            // add the attributes
            for (var i = 1; i < savedParameterValues.length; i++) {
                attributes += savedParameterValues[i] + ", "
            }
            //cutting off the last white space and the comma
            attributes = attributes.substring(0, attributes.length - 2);
            //add the attributes to second obj of the predefined parameter
            parameters.push({
                name: predefinedParameters[1].name, value: attributes, description:
                    predefinedParameters[1].description, optional: predefinedParameters[1].optional,
                defaultValue: predefinedParameters[1].defaultValue
            });
            return parameters;
        };

        /**
         * Function to map the user saved parameters of sort
         * @param {Object} predefinedParameters predefined parameters
         * @param {Object} savedParameterValues user saved parameters
         * @return {Object} parameters
         */
        var mapParameterValuesSort = function (predefinedParameters, savedParameterValues) {
            var parameters = [];
            var attributes = "";
            var order = "";
            var length = "";
            if (savedParameterValues.length != 0) {
                length = savedParameterValues[0];
            }
            //add the first saved param to predefined param's first index (window.length)
            parameters.push({
                name: predefinedParameters[0].name, value: length, description:
                    predefinedParameters[0].description, optional: predefinedParameters[0].optional,
                defaultValue: predefinedParameters[0].defaultValue
            });
            // to determine the attributes and order
            if (savedParameterValues.length > 1) {
                for (var i = 1; i < savedParameterValues.length; i++) {
                    if (savedParameterValues[i].indexOf("'") >= 0 || savedParameterValues[i].indexOf('"') >= 0) {
                        order = savedParameterValues[i];
                        order = order.slice(1, order.length - 1)
                    } else {
                        //attributes
                        attributes += savedParameterValues[i] + ", ";

                    }
                }
                //cutting off the last white space and the comma
                attributes = attributes.substring(0, attributes.length - 2);
            }
            //add the attributes to second obj of the predefined parameter
            parameters.push({
                name: predefinedParameters[1].name, value: attributes, description:
                    predefinedParameters[1].description, optional: predefinedParameters[1].optional,
                defaultValue: predefinedParameters[1].defaultValue
            });
            //add the order to the third obj of the predefined parameter
            parameters.push({
                name: predefinedParameters[2].name, value: order, description:
                    predefinedParameters[2].description, optional: predefinedParameters[2].optional,
                defaultValue: predefinedParameters[2].defaultValue
            });
            return parameters;
        };

        /** Function to show and hide the order parameter of sort type */
        var showHideOrderForSort = function () {
            if ($('#window-parameters #attribute-parameter').find('.parameter-checkbox').is(":checked")) {
                $('#window-parameters #order-parameter').show();
            } else {
                $('#window-parameters #order-parameter').hide();
            }
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
        var buildAnnotation = function (annotationStringList, annotationObjectList) {
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
         * @function generate properties form for a window
         * @param element selected element(window)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        WindowForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');

            // retrieve the window information from the collection
            var clickedElement = self.configurationData.getSiddhiAppConfig().getWindow(id);
            var propertyDiv = $('<div class = "window-form-container"><div id="property-header"><h3>Window' +
                ' Configuration</h3></div> <h4>Name: </h4> <input type="text" id="windowName" class="clearfix">' +
                '<label class="error-message" id="windowNameErrorMessage"></label> <div id="define-attribute"></div>' +
                '<button id="btn-submit" type="button" class="btn toggle-view-button">' +
                'Submit </button> <button id="btn-cancel" type="button" class="btn btn-default"> Cancel </button> ' +
                '</div> <div class= "window-form-container"> ' +
                '<div id = "defineFunctionName"> </div> <div id="defineFunctionParameters"> </div>' +
                '</div> <div class = "window-form-container"> <div id="defineOutputEvents"> </div> </div>' +
                '<div class = "window-form-container"> <div id="define-annotation"> </div> </div>');
            formContainer.append(propertyDiv);

            //to pop-up the clicked element
            $('#' + id).addClass('selected-element');
            $(".overlayed-container").fadeTo(200, 1);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            //declaration and initialization of variables
            var predefinedWindowFunctionNames = _.orderBy(this.configurationData.rawExtensions["windowFunctionNames"],
                ['name'], ['asc']);
            var functionParameters = [];
            var functionParametersWithValues = [];
            var selectedWindowType;
            var annotations = [];

            //event listener to show parameter description
            $('#defineFunctionParameters').on('mouseover', '.parameter-desc', function () {
                $(this).find('.parameter-desc-content').show();
            });

            //event listener to hide parameter description
            $('#defineFunctionParameters').on('mouseout', '.parameter-desc', function () {
                $(this).find('.parameter-desc-content').hide();
            });

            //event listener when the parameter checkbox is changed
            $('#defineFunctionParameters').on('change', '.parameter-checkbox', function () {
                var parameterParent = $(this).parents(".parameter");
                if ($(this).is(':checked')) {
                    parameterParent.find(".optional-param-content").show();
                } else {
                    parameterParent.find(".optional-param-content").hide();
                    parameterParent.find(".parameter-value").removeClass("required-input-field");
                    parameterParent.find(".error-message").text("");
                }
                //check for sort type's parameter (order & attribute params)
                if (selectedType === sort) {
                    showHideOrderForSort();
                }
            });

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

            var name = clickedElement.getName();
            var windowFunctionNameTemplate = Handlebars.compile($('#type-selection-form-template').html());
            var wrappedHtml = windowFunctionNameTemplate({ id: "window", types: predefinedWindowFunctionNames });
            $('#defineFunctionName').html(wrappedHtml);
            renderOutputEventTypes();

            if (!name) {
                var attributeFormTemplate = Handlebars.compile($('#attribute-form-template').html());
                var wrappedHtml = attributeFormTemplate([{ name: "" }]);
                $('#define-attribute').html(wrappedHtml);
                selectedWindowType = $('#defineFunctionName #window-type').val();
                functionParameters = getSelectedTypeParameters(selectedWindowType, predefinedWindowFunctionNames);
                functionParametersWithValues = createParameterWithValues(functionParameters);
                renderParameters(functionParametersWithValues, selectedWindowType, "window")
            } else {
                var functionName = clickedElement.getFunction().toLowerCase();
                var savedParameterValues = clickedElement.getParameters();

                $('#windowName').val(name.trim());
                selectedType = functionName;
                $('#defineFunctionName').find('#window-type option').filter(function () {
                    return ($(this).val().toLowerCase() == (functionName));
                }).prop('selected', true);
                functionParameters = getSelectedTypeParameters(functionName, predefinedWindowFunctionNames);
                if (functionName === sort.toLowerCase()) {
                    functionParametersWithValues = mapParameterValuesSort(functionParameters, savedParameterValues);
                    renderParameters(functionParametersWithValues, selectedType, "window");
                    showHideOrderForSort();
                } else if (functionName === frequent.toLowerCase()) {
                    functionParametersWithValues = mapParameterValuesFrequent(functionParameters,
                        savedParameterValues);
                    renderParameters(functionParametersWithValues, selectedType, "window");
                } else if (functionName === lossyFrequent.toLowerCase()) {
                    functionParametersWithValues = mapParameterValuesLossyFrequent(functionParameters,
                        savedParameterValues);
                    renderParameters(functionParametersWithValues, selectedType, "window");
                } else {
                    functionParametersWithValues = mapUserParameterValues(functionParameters, savedParameterValues);
                    renderParameters(functionParametersWithValues, selectedType, "window");
                }

                var savedAttributes = clickedElement.getAttributeList();
                var attributeFormTemplate = Handlebars.compile($('#attribute-form-template').html());
                var wrappedHtml = attributeFormTemplate(savedAttributes);
                $('#define-attribute').html(wrappedHtml);
                changeAtrributeNavigation();

                //to select the type of the saved attributes
                var i = 0;
                $('.attribute .attr-content').each(function () {
                    $(this).find('.attr-type option').filter(function () {
                        return ($(this).val() == (savedAttributes[i].getType()).toLowerCase());
                    }).prop('selected', true);
                    i++;
                });

                var savedOutputEventType = clickedElement.getOutputEventType().toLowerCase();
                $('#defineOutputEvents').find('#event-type option').filter(function () {
                    return ($(this).val().toLowerCase() == (savedOutputEventType));
                }).prop('selected', true);

                var savedAnnotationObjects = clickedElement.getAnnotationListObjects();
                annotations = savedAnnotationObjects;
            }

            //render the user defined annotations form template
            var raw_partial = document.getElementById('recursiveAnnotationPartial').innerHTML;
            Handlebars.registerPartial('recursiveAnnotation', raw_partial);
            var annotationFormTemplate = Handlebars.compile($('#annotation-form-template').html());
            var wrappedHtml = annotationFormTemplate(annotations);
            $('#define-annotation').html(wrappedHtml);
            loadAnnotation();

            $('#window-type').change(function () {
                functionParameters = getSelectedTypeParameters(this.value, predefinedWindowFunctionNames);
                selectedType = this.value.toLowerCase();
                if (savedParameterValues && selectedType == functionName.toLowerCase()) {
                    if (selectedType === sort.toLowerCase()) {
                        functionParametersWithValues = mapParameterValuesSort(functionParameters, savedParameterValues);
                        renderParameters(functionParametersWithValues, selectedType, "window")
                    } else if (selectedType === frequent.toLowerCase()) {
                        functionParametersWithValues = mapParameterValuesFrequent(functionParameters,
                            savedParameterValues);
                        renderParameters(functionParametersWithValues, selectedType, "window")
                    } else if (selectedType === lossyFrequent.toLowerCase()) {
                        functionParametersWithValues = mapParameterValuesLossyFrequent(functionParameters,
                            savedParameterValues);
                        renderParameters(functionParametersWithValues, selectedType, "window")
                    } else {
                        functionParametersWithValues = mapUserParameterValues(functionParameters, savedParameterValues);
                        renderParameters(functionParametersWithValues, selectedType, "window");
                        selectTimeStamp(functionParametersWithValues)
                    }
                } else {
                    functionParametersWithValues = createParameterWithValues(functionParameters);
                    renderParameters(functionParametersWithValues, selectedType, "window");
                }
                if (selectedType === sort) {
                    showHideOrderForSort();
                }
            });

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#btn-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                //clear the error messages
                $('.error-message').text("")
                $('.required-input-field').removeClass('required-input-field');
                var isErrorOccurred = false;

                var windowName = $('#windowName').val().trim();
                //check if window name is empty
                if (windowName == "") {
                    $('#windowName').addClass('required-input-field');
                    $('#windowName')[0].scrollIntoView();
                    $('#windowNameErrorMessage').text("Window name is required.")
                    isErrorOccurred = true;
                    return;
                }

                var previouslySavedName = clickedElement.getName();
                if (previouslySavedName === undefined) {
                    previouslySavedName = "";
                }
                var isWindowNameUsed = self.formUtils.isDefinitionElementNameUsed(windowName, id);
                if (isWindowNameUsed) {
                    $('#windowName').addClass('required-input-field');
                    $('#windowName')[0].scrollIntoView();
                    $('#windowNameErrorMessage').text("Window name is already used.")
                    isErrorOccurred = true;
                    return;
                }

                if (previouslySavedName !== windowName) {
                    //validate window name
                    if ((windowName.indexOf(' ') >= 0)) {
                        $('#windowName').addClass('required-input-field');
                        $('#windowName')[0].scrollIntoView();
                        $('#windowNameErrorMessage').text("Window name cannot have white space.")
                        isErrorOccurred = true;
                        return;
                    }
                    if (!alphabeticValidatorRegex.test(windowName.charAt(0))) {
                        $('#windowName').addClass('required-input-field');
                        $('#windowName')[0].scrollIntoView();
                        $('#windowNameErrorMessage').text("Window name must start with an alphabetic character.")
                        isErrorOccurred = true;
                        return;
                    }
                }

                var attributeNameList = [];
                if (validateAttributeNames(attributeNameList)) {
                    isErrorOccurred = true;
                    return;
                }

                if (attributeNameList.length == 0) {
                    $('.attribute:eq(0)').find('.attr-name').addClass('required-input-field');
                    $('.attribute:eq(0)').find('.attr-name')[0].scrollIntoView();
                    $('.attribute:eq(0)').find('.error-message').text("Minimum one attribute is required.");
                    isErrorOccurred = true;
                    return;
                }

                var functionName = $('#defineFunctionName #window-type').val();
                var parameters = [];
                var isError = false;
                if (functionName.toLowerCase() === sort) {
                    isError = buildParameterValuesSort(parameters, functionParameters)
                } else if (functionName.toLowerCase() === frequent ||
                    functionName === lossyFrequent) {
                    isError = buildParameterValuesFrequentOrLossyFrequent(parameters, functionParameters);
                } else {
                    isError = buildParameterValues(parameters, functionParameters)
                }

                if (isError) {
                    isErrorOccurred = true;
                    return;
                }

                if (!isErrorOccurred) {
                    if (previouslySavedName !== windowName) {
                        // update connection related to the element if the name is changed
                        clickedElement.setName(windowName);
                        self.formUtils.updateConnectionsAfterDefinitionElementNameChange(id);

                        var textNode = $(element).parent().find('.windowNameNode');
                        textNode.html(windowName);
                    }

                    clickedElement.setFunction(functionName);
                    clickedElement.setParameters(parameters);

                    //clear the previously saved attribute list
                    clickedElement.clearAttributeList();
                    //add the attributes to the attribute list
                    $('.attribute .attr-content').each(function () {
                        var nameValue = $(this).find('.attr-name').val().trim();
                        var typeValue = $(this).find('.attr-type').val();
                        if (nameValue != "") {
                            var attributeObject = new Attribute({ name: nameValue, type: typeValue });
                            clickedElement.addAttribute(attributeObject)
                        }
                    });

                    var outputEventType = $('#defineOutputEvents #event-type').val().toUpperCase();
                    clickedElement.setOutputEventType(outputEventType);

                    clickedElement.clearAnnotationList();
                    clickedElement.clearAnnotationListObjects();
                    var annotationStringList = [];
                    var annotationObjectList = [];

                    buildAnnotation(annotationStringList, annotationObjectList);
                    _.forEach(annotationStringList, function (annotation) {
                        clickedElement.addAnnotation(annotation);
                    });
                    _.forEach(annotationObjectList, function (annotation) {
                        clickedElement.addAnnotationObject(annotation);
                    });

                    $('#' + id).removeClass('incomplete-element');
                    $('#' + id).prop('title', '');
                    self.designViewContainer.removeClass('disableContainer');
                    self.toggleViewButton.removeClass('disableContainer');

                    // set the isDesignViewContentChanged to true
                    self.configurationData.setIsDesignViewContentChanged(true);

                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);

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

        return WindowForm;
    });

