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

define(['require', 'lodash', 'appData', 'log', 'constants', 'handlebar', 'annotationObject', 'annotationElement',
    'designViewUtils', 'queryWindowOrFunction', 'streamHandler', 'patternOrSequenceQueryCondition'],
    function (require, _, AppData, log, Constants, Handlebars, AnnotationObject, AnnotationElement, DesignViewUtils,
        QueryWindowOrFunction, StreamHandler, PatternOrSequenceQueryCondition) {

        /**
         * @class FormUtils Contains utility methods for forms
         * @constructor
         * @param {Object} configurationData Siddhi app data
         * @param {Object} jsPlumbInstance JsPlumb instance of the current tab
         */
        var FormUtils = function (configurationData, jsPlumbInstance) {
            this.configurationData = configurationData;
            this.jsPlumbInstance = jsPlumbInstance;
        };

        /**
         * @function check whether given name to the definition element is used(This will only consider definitions
         * which creates internal streams in Siddhi for each of them. Function definitions are not considered.)
         * @param elementName given name to the definition element
         * @param skipElementID this element name will be ignored when checking the unique name. This is used when
         *          saving the same name after editing a particular element
         * @return {boolean}
         */
        FormUtils.prototype.isDefinitionElementNameUsed = function (elementName, skipElementID) {
            var self = this;
            var isNameUsed = false;
            var streamList = self.configurationData.getSiddhiAppConfig().getStreamList();
            var tableList = self.configurationData.getSiddhiAppConfig().getTableList();
            var windowList = self.configurationData.getSiddhiAppConfig().getWindowList();
            var aggregationList = self.configurationData.getSiddhiAppConfig().getAggregationList();
            var triggerList = self.configurationData.getSiddhiAppConfig().getTriggerList();
            var listNames = [streamList, tableList, windowList, aggregationList, triggerList];
            _.forEach(listNames, function (list) {
                _.forEach(list, function (element) {
                    if (element.getName() === elementName) {
                        if (!(skipElementID !== undefined && skipElementID === element.getId())) {
                            isNameUsed = true;
                        }
                    }
                });
            });

            return isNameUsed;
        };

        /**
         * @function check whether given name to the function definition element is used.
         * @param elementName given name to the definition element
         * @param skipElementID this element name will be ignored when checking the unique name. This is used when
         *          saving the same name after editing a particular element
         * @return {boolean}
         */
        FormUtils.prototype.isFunctionDefinitionElementNameUsed = function (elementName, skipElementID) {
            var self = this;
            var isNameUsed = false;
            var functionList = self.configurationData.getSiddhiAppConfig().getFunctionList();
            _.forEach(functionList, function (element) {
                if (element.getName() === elementName) {
                    if (!(skipElementID !== undefined && skipElementID === element.getId())) {
                        isNameUsed = true;
                    }
                }
            });

            return isNameUsed;
        };

        /**
         * @function check whether given name to the inner stream definition is used in the given partition.
         * @param partitionId id of the partition element
         * @param elementName given name to the definition element
         * @param skipElementID this element name will be ignored when checking the unique name. This is used when
         *          saving the same name after editing a particular element
         * @return {boolean}
         */
        FormUtils.prototype.isStreamDefinitionNameUsedInPartition = function (partitionId, elementName,
            skipElementID) {
            var self = this;
            var isNameUsed = false;
            var partition = self.configurationData.getSiddhiAppConfig().getPartition(partitionId);
            var streamList = partition.getStreamList();
            _.forEach(streamList, function (element) {
                if (element.getName() === elementName) {
                    if (!(skipElementID !== undefined && skipElementID === element.getId())) {
                        isNameUsed = true;
                    }
                }
            });

            return isNameUsed;
        };

        /**
         * @function check whether given query name is used in the query list
         * @param elementName given name to the definition element
         * @param skipElementID this element name will be ignored when checking the unique name. This is used when
         *          saving the same name after editing a particular element
         * @return {boolean}
         */
        FormUtils.prototype.isQueryDefinitionNameUsed = function (elementName, skipElementID) {
            var self = this;
            var isNameUsed = false;
            var joinQueryList = self.configurationData.getSiddhiAppConfig().getJoinQueryList();
            var sequenceQueryList = self.configurationData.getSiddhiAppConfig().getSequenceQueryList();
            var patternQueryList = self.configurationData.getSiddhiAppConfig().getPatternQueryList();
            var WindowFilterProjectionQueryList = self.configurationData.getSiddhiAppConfig()
                .getWindowFilterProjectionQueryList();
            var listNames = [joinQueryList, sequenceQueryList, patternQueryList, WindowFilterProjectionQueryList];
            _.forEach(listNames, function (list) {
                _.forEach(list, function (element) {
                    if (element.getQueryName() === elementName) {
                        if (!(skipElementID !== undefined && skipElementID === element.getId())) {
                            isNameUsed = true;
                        }
                    }
                });
            });

            return isNameUsed;
        };

        /**
         * @function This method removes undefined, null, empty arrays, empty object property fields from a JSON object
         * @param objectElement object which is needed to be cleaned
         * @return cleaned element
         */
        FormUtils.prototype.cleanJSONObject = function (objectElement) {
            var self = this;
            for (var propertyName in objectElement) {
                if (objectElement.hasOwnProperty(propertyName)
                    && (objectElement[propertyName] === null
                        || (!_.isNumber(objectElement[propertyName]) && !_.isBoolean(objectElement[propertyName])
                            && _.isEmpty(objectElement[propertyName]))
                        || !objectElement[propertyName])) {
                    delete objectElement[propertyName];
                } else if (objectElement.hasOwnProperty(propertyName)
                    && objectElement[propertyName] instanceof Object) {
                    self.cleanJSONObject(objectElement[propertyName]);
                    if (objectElement.hasOwnProperty(propertyName) && !_.isBoolean(objectElement[propertyName])
                        && _.isEmpty(objectElement[propertyName])) {
                        delete objectElement[propertyName];
                    }
                }
            }
            return objectElement;
        };

        /**
         * @function to delete the connections
         */
        FormUtils.prototype.deleteConnectionsAfterDefinitionElementNameChange = function (outConnections, inConnections) {
            var self = this;

            _.forEach(outConnections, function (connection) {
                self.jsPlumbInstance.deleteConnection(connection);
            });
            _.forEach(inConnections, function (connection) {
                self.jsPlumbInstance.deleteConnection(connection);
            });
        };

        /**
         * @function to establish the connections
         */
        FormUtils.prototype.establishConnectionsAfterDefinitionElementNameChange = function (outConnections, inConnections) {
            var self = this;
            _.forEach(inConnections, function (inConnection) {
                self.jsPlumbInstance.connect({
                    source: inConnection.sourceId,
                    target: inConnection.targetId
                });
            });

            _.forEach(outConnections, function (outConnection) {
                self.jsPlumbInstance.connect({
                    source: outConnection.sourceId,
                    target: outConnection.targetId
                });
            });
        };

        /**
         * @function Builds HTML for form buttons.
         * @returns {string} HTML string
         */
        FormUtils.prototype.buildFormButtons = function () {
            var html = '<div class="query-form-actions">' +
                '<button type="button" id="btn-submit" class="btn btn-primary">Submit</button>' +
                '<button type="button" id="btn-cancel" class="btn btn-default">Cancel</button> </div>';
            return html;
        };

        /**
         * @function Builds HTML for atribute
         * @return {String} attributeHtml
         */
        FormUtils.prototype.addAttribute = function () {
            var attributeHtml = '<li class="attribute clearfix"><div class="clearfix"> ' +
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
                '<label class="error-message"></label></li>';
            return attributeHtml;
        };

        /**
		 * @function to add a default type to the predefined array
		 */
        FormUtils.prototype.addCustomizedType = function (predefinedFunctions, typeToBeAdded) {
            var customizedType = {
                name: typeToBeAdded,
                parameters: []
            };
            predefinedFunctions.push(customizedType);
        };

        /**
         * @function to append an attribute for the order-by section
         * @param {Object} possibleAttributes to be rendered down in the dropdown
         */
        FormUtils.prototype.appendOrderBy = function (possibleAttributes) {
            var self = this;
            var orderByDiv = '<li> <div class="clearfix"> ' +
                '<div class="order-by-attribute-content"> <div class="define-attribute-drop-down">' +
                '</div> <div class="define-order-del"> <select class="order-selection">' +
                '<option value="asc"> asc </option> <option value="desc"> desc </option>' +
                '</select> <a class = "btn-del-option"> <i class = "fw fw-delete"> </i></a>' +
                '</div> </div> </div> <label class="error-message"> </label> </li>';
            $('.order-by-attributes').append(orderByDiv)
            self.renderDropDown('.order-by-content .define-attribute-drop-down:last', possibleAttributes, Constants
                .ORDER_BY);
        };

        /**
         * @function to append an attribute to the group-by section
         * @param {Object} possibleAttributes to be rendered down in the dropdown
         */
        FormUtils.prototype.appendGroupBy = function (possibleAttributes) {
            var self = this;
            var groupByDiv = '<li>  <div class="clearfix"> <div class="group-by-attribute-content"> ' +
                '<div class="define-attribute-drop-down"> </div> ' +
                '<a class = "btn-del-option"> <i class = "fw fw-delete"> </i> </a> </div> </div> ' +
                '<label class="error-message"> </label> </li>';
            $('.group-by-attributes').append(groupByDiv)
            self.renderDropDown('.group-by-content .define-attribute-drop-down:last', possibleAttributes, Constants
                .GROUP_BY);
        };

        /**
         * @function to append an attribute for the projection section
         */
        FormUtils.prototype.appendUserSelectAttribute = function () {
            var userSelectDiv = '<li class="attribute"> <div class="clearfix"> <div class="clearfix"> ' +
                '<input type="text" class = "attribute-expression-as name" value=""><a class = "btn-del-option"> ' +
                '<i class = "fw fw-delete"> </i></a> </div> <label class = "error-message"></label> ' +
                '</div> </li>';
            $('.user-defined-attributes').append(userSelectDiv);
        };

        /**
         * @function to render the html to display the radio options for selecting the rdbms type
         */
        FormUtils.prototype.renderRdbmsTypes = function () {
            var rdbmsTypeDiv = '<div class="clearfix"> <label class = "rdbms-type">' +
                '<input type= "radio" name ="radioOpt" value="inline-config"> Inline-config' +
                '</label> <label class = "rdbms-type">  ' +
                '<input type = "radio" name = "radioOpt" value = "datasource"> Datasource </label>' +
                '<label class = "rdbms-type"> <input type = "radio" name = "radioOpt" value="jndi"> Jndi-resource ' +
                '</label></div> ';
            $('#define-rdbms-type').html(rdbmsTypeDiv);
        };

        /**
         * @function to render the html for query output
         */
        FormUtils.prototype.renderQueryOutput = function (outputElementName) {
            var queryOutputTemplate = Handlebars.compile($('#query-output-template').html())
                ({ into: outputElementName, operation: Constants.INSERT });
            $('.define-query-output').html(queryOutputTemplate);
        };

        /**
         * @function render the select box template
         * @param {String} id div id to embed the select box
         * @param {Object} predefinedTypes
         */
        FormUtils.prototype.renderSourceSinkStoreTypeDropDown = function (id, predefinedTypes) {
            var selectionFormTemplate = Handlebars.compile($('#type-selection-form-template').html())
                ({ id: id, types: predefinedTypes });
            $('#define-' + id).html(selectionFormTemplate);
        };

        /**
         * @function render the attribute form template
         * @param {Object} attributes the saved attributes
         */
        FormUtils.prototype.renderAttributeTemplate = function (attributes) {
            var self = this;
            var attributeFormTemplate = Handlebars.compile($('#attribute-form-template').html())(attributes);
            $('#define-attribute').html(attributeFormTemplate);
            self.changeAttributeNavigation('#attribute-div');
            self.addEventListenersForAttributeDiv();
        };

        /**
         * @function render the user defined select atributes
         * @param {Object} attributes user saved attribute expression to be rendered
         * @param {templateId} the id of the template div to render
         */
        FormUtils.prototype.renderUserDefinedAttributeSelection = function (attributes, templateId) {
            var userAttributeSelectionTemplate = Handlebars.compile($('#' + templateId + '-template').html())(attributes);
            $('.define-select').html(userAttributeSelectionTemplate);
        };

        /**
         * @function to register the drop-down partial(handlebars)
         */
        FormUtils.prototype.registerDropDownPartial = function () {
            var raw_partial = document.getElementById('drop-down-template').innerHTML;
            Handlebars.registerPartial('renderDropDown', raw_partial);
        };

        /**
         * @function renders the annotation form template
         * @param {String} className div to embed the template
         * @param {Object} annotations saved annotations
         */
        FormUtils.prototype.renderAnnotationTemplate = function (className, annotations) {
            var self = this;
            var raw_partial = document.getElementById('recursiveAnnotationPartial').innerHTML;
            Handlebars.registerPartial('recursiveAnnotation', raw_partial);
            var annotationFormTemplate = Handlebars.compile($('#annotation-form-template').html())(annotations);
            $('.' + className).html(annotationFormTemplate);
            self.loadAnnotation();
        };

        /**
         * @function render the select box for predefined maps
         * @param {Object} predefinedMaps Predefined map annotations
         */
        FormUtils.prototype.renderMap = function (predefinedMaps) {
            if (!$.trim($('#define-map').html()).length) {
                var mapFormTemplate = Handlebars.compile($('#type-selection-form-template').html())
                    ({ id: "map", types: predefinedMaps });
                $('#define-map').html(mapFormTemplate);
                $('#define-map #map-type').val('passThrough');
                $('#define-map #map-type option:contains("' + Constants.DEFAULT_MAPPER_TYPE + '")').
                    text('passThrough (default)');
            }
        };

        /**
         * @function to render the options for the selected type
         * @param {Object} optionsArray Saved options
         * @param {Object} customizedOptions Options typed by the user which aren't one of the predefined option
         * @param {String} id identify the div to embed the options
         */
        FormUtils.prototype.renderOptions = function (optionsArray, customizedOptions, id) {
            var self = this;
            optionsArray.sort(function (val1, val2) {
                if (val1.optional && !val2.optional) return 1;
                else if (!val1.optional && val2.optional) return -1;
                else return 0;
            });
            var optionsTemplate = Handlebars.compile($('#source-sink-store-options-template').html())({
                id: id,
                options: optionsArray,
                customizedOptions: customizedOptions
            });
            $('#' + id + '-options-div').html(optionsTemplate);
            self.changeCustomizedOptDiv(id);;
        };

        /**
         * @function to render the conditions of pattern or sequence query
         * @param {Object} conditionList List of conditions
         * @param {Object} inputStreamNames streams connected to the query
         */
        FormUtils.prototype.renderConditions = function (conditionList, inputStreamNames) {
            var self = this;
            var streamNames = {
                id: "condition-stream-name",
                options: inputStreamNames
            }
            self.registerDropDownPartial();
            var conditionNavTemplate = Handlebars.compile($('#pattern-sequence-condition-navigation-form-template')
                .html());
            var conditionContentTemplate = Handlebars.compile($('#pattern-sequence-condition-content-form-template')
                .html());
            self.addDeleteButtonForConditionNav();
            _.forEach(conditionList, function (condition) {
                var wrappedHtml = conditionNavTemplate(condition);
                $('.define-conditions .nav-tabs li:last-child').before(wrappedHtml);
            });

            _.forEach(conditionList, function (condition) {
                var wrappedHtml = conditionContentTemplate({
                    condition: condition,
                    inputStreamNames: streamNames
                });
                $('.define-conditions .tab-content').append(wrappedHtml);
            });
            //removes the first delete button
            $('.define-conditions').find('.nav-tabs li:eq(0) .btn-del-condition').remove();
        };

        /**
         * @function to add the button to add the condition in the nav bar of the condition-ul
         */
        FormUtils.prototype.addDeleteButtonForConditionNav = function () {
            var buttonHTML = '<li> <a class="btn-add-condition">+</a> </li> ';
            if ($(".define-conditions").find(".btn-add-condition").length == 0) {
                $('.define-conditions .nav-tabs').append(buttonHTML);
            }
        };

        /**
         * @function to render the function of window/stream-function types
         * @param {Object} predefinedFunction possible options of a particular type
         * @param {String} className div to embed the template
         * @param {String} id window or stream-function
         */
        FormUtils.prototype.renderFunctions = function (predefinedFunctions, className, id) {
            var windowFunctionNameTemplate = Handlebars.compile($('#type-selection-form-template').html())
                ({ id: id, types: predefinedFunctions });
            $(className).find('.defineFunctionName').html(windowFunctionNameTemplate);
        };

        /**
         * @function to render the parameter for the selected function using handlebars
         * @param {Object} parameterArray Saved parameters
         * @param {String} id window or stream-function
         * @param {String} parameterDiv div to embed the parameters
         */
        FormUtils.prototype.renderParameters = function (parameterArray, id, parameterDiv) {
            parameterArray.sort(function (val1, val2) {
                if (val1.optional && !val2.optional) return 1;
                else if (!val1.optional && val2.optional) return -1;
                else return 0;
            });
            var parameterTemplate = Handlebars.compile($('#window-function-parameters-template').html())({
                id: id,
                parameters: parameterArray
            });
            $(parameterDiv).find('.defineFunctionParameters').html(parameterTemplate);
        };

        /**
         * @function to render the left and right source for join-query
         * @param {String} type left or right
         */
        FormUtils.prototype.renderLeftRightSource = function (type) {
            var sourceTemplate = Handlebars.compile($('#query-source-form-template').html())({ type: type });
            $('.define-' + type + '-source').html(sourceTemplate);
        };

        /**
         * @function to render the drop-down template
         */
        FormUtils.prototype.renderDropDown = function (className, possibleOptions, id) {
            var possibleValues = {
                options: possibleOptions,
                id: id
            }
            var dropDownTemplate = Handlebars.compile($('#drop-down-template').html())(possibleValues);
            $(className).append(dropDownTemplate);
        };

        /**
         * @function to render options for predefined annotations
         */
        FormUtils.prototype.renderOptionsForPredefinedAnnotations = function (predefinedAnnotations) {
            var self = this;
            _.forEach(predefinedAnnotations, function (annotation) {
                if (annotation.parameters) {
                    self.renderOptions(annotation.parameters, [], annotation.name)
                }
                if (annotation.annotations) {
                    self.renderOptionsForPredefinedAnnotations(annotation.annotations);
                }
                self.addEventListenersForGenericOptionsDiv(annotation.name);
            });
            self.hideCustomizedOptionsDiv();
        };

        /**
         * @function to render the stream handler template
         */
        FormUtils.prototype.renderStreamHandler = function (className, savedData, streamHandlerTypes) {
            var self = this;
            var streamHandlerList = []
            var types = {
                id: Constants.STREAM_HANDLER,
                options: streamHandlerTypes
            }
            if (savedData && savedData.streamHandlerList != 0) {
                streamHandlerList = savedData.streamHandlerList;
            }
            var streamHandlers = {
                streamHandlerList: streamHandlerList,
                types: types,
                className: className
            }
            self.registerDropDownPartial();
            var streamHandlerTemplate = Handlebars.compile($('#stream-handler-form-template').html())(streamHandlers);
            $('.define-' + className + '-source .define-stream-handler-section').html(streamHandlerTemplate);
        };

        /**
         * @function to map the stream-handler content depending on the type
         */
        FormUtils.prototype.renderStreamHandlerContentDiv = function (type, div) {
            if (type === Constants.FILTER) {
                var contentDiv = '<input type="text" class = "filter-condition-content symbol-syntax-required-value ' +
                    'name clearfix"> <label class = "error-message"> </label>';
            } else if (type === Constants.WINDOW) {
                var contentDiv = '<div class= "defineFunctionName"> </div> <div class = "defineFunctionParameters"> ' +
                    '</div>';
            } else if (type === Constants.FUNCTION) {
                var contentDiv = '<div class= "defineFunctionName"> </div> <div class = "defineFunctionParameters"> ' +
                    '</div>'
            }
            div.html(contentDiv)
        };

        /**
         * @function to render the template of primary and index annotations
         */
        FormUtils.prototype.renderPrimaryIndexAnnotations = function (primaryIndexAnnotations, className) {
            var self = this;
            var annotationFormTemplate = Handlebars.compile($('#primary-index-annotation-template').html())
                (primaryIndexAnnotations);
            $('.' + className).html(annotationFormTemplate);
            self.removeDeleteButtonOfPrimaryIndexAnnotationValue();
            self.addEventListenerForPrimaryIndexAnnotationDiv();
        };

        /**
         * @function to render the template for predefined annotations
         */
        FormUtils.prototype.renderPredefinedAnnotations = function (predefinedAnnotations, className) {
            var raw_partial = document.getElementById('recursive-predefined-annotation-partial').innerHTML;
            Handlebars.registerPartial('recursive-predefined-annotation', raw_partial);
            var annotationFormTemplate = Handlebars.compile($('#predefined-annotation-form-template').html())
                (predefinedAnnotations);
            $('.' + className).html(annotationFormTemplate);
        };

        /**
         * @function to render the group-by template
         * @param {Object} possibleGroupByAttributes attributes to be shown in the drop down
         * @param {Object} groupBy user defined group by
         */
        FormUtils.prototype.renderGroupBy = function (possibleGroupByAttributes, groupBy) {
            var self = this;
            var possibleGroupByAttributes = {
                options: possibleGroupByAttributes,
                id: Constants.GROUP_BY
            }
            var groupByAttributes = {
                groupBy: groupBy,
                possibleGroupByAttributes: possibleGroupByAttributes
            }
            self.registerDropDownPartial();
            var groupByTemplate = Handlebars.compile($('#group-by-template').html())(groupByAttributes);
            $('.define-group-by-attributes').html(groupByTemplate);
            self.checkForAttributeLength(possibleGroupByAttributes.length, Constants.GROUP_BY);
        };

        /**
         * @function to render the order-by template
         * @param {Object} possibleOrderByAttributes attributes to be shown in the drop down
         * @param {Object} orderBy user defined order by
         * @param {String} className of the division
         */
        FormUtils.prototype.renderOrderBy = function (possibleOrderByAttributes, orderBy, className) {
            var self = this;
            var possibleOrderByAttributes = {
                options: possibleOrderByAttributes,
                id: Constants.ORDER_BY
            }
            var orderByAttributes = {
                orderBy: orderBy,
                possibleOrderByAttributes: possibleOrderByAttributes
            }
            self.registerDropDownPartial();
            var orderByTemplate = Handlebars.compile($('#order-by-template').html())(orderByAttributes);
            $('.define-order-by-attributes').html(orderByTemplate);
            self.checkForAttributeLength(possibleOrderByAttributes.length, Constants.ORDER_BY);
        };

        /**
         * @function to render output event types
         */
        FormUtils.prototype.renderOutputEventTypes = function () {
            var outputEventDiv = '<div class = "clearfix"> <label> Event Type </label> </div>' +
                '<div class = "clearfix"> <select id="event-type">' +
                '<option value = "current_events"> current events </option>' +
                '<option value = "all_events"> all events </option>' +
                '<option value = "expired_events"> expired events </option>' +
                '</select> </div>'
            $('.define-output-events').html(outputEventDiv);
        };

        /**
         * @function to select the attribute type from the select box
         * @param {Object} savedAttributes
         */
        FormUtils.prototype.selectTypesOfSavedAttributes = function (savedAttributes) {
            //to select the options(type) of the saved attributes
            var i = 0;
            $('.attribute .attr-content').each(function () {
                $(this).find('.attr-type option').filter(function () {
                    return ($(this).val() == (savedAttributes[i].getType()).toLowerCase());
                }).prop('selected', true);
                i++;
            });
        };

        /**
         * @function to select the projection for aggregation
         */
        FormUtils.prototype.selectAggregateProjection = function (select) {
            var self = this;
            var attributes;
            var selectedType = (select) ? (select.getType().toLowerCase()) : Constants.TYPE_ALL;
            if (selectedType === Constants.TYPE_ALL) {
                attributes = [""]
            } else {
                attributes = self.getAttributeExpressions(select.getValue())
            }
            self.renderUserDefinedAttributeSelection(attributes, "aggregate-projection");
            //removes the first delete button
            $('.define-select').find('.user-defined-attributes li:eq(0) .btn-del-option').remove();
            self.selectAttributeSelection(selectedType);
        };

        /**
         * @function to select the attribute selection [all or user-defined]
         */
        FormUtils.prototype.selectAttributeSelection = function (selectedType) {
            $('.define-select').find('.attribute-selection-type option').filter(function () {
                return ($(this).val() === selectedType);
            }).prop('selected', true);
            if (selectedType === Constants.TYPE_ALL) {
                $('.define-user-defined-attributes').hide();
            } else {
                $('.define-user-defined-attributes').show();
            }
        };

        /**
         * @function to select the projection for queries
         */
        FormUtils.prototype.selectQueryProjection = function (select, outputElementName) {
            var self = this;
            var attributes;
            var selectedType = (select) ? (select.getType().toLowerCase()) : Constants.TYPE_ALL;
            if (selectedType === Constants.TYPE_ALL) {
                attributes = self.createEmptyAttributesForQueryProjection(outputElementName);
            } else {
                attributes = self.createAttributesForQueryProjection(select.getValue(), outputElementName);
            }
            self.renderUserDefinedAttributeSelection(attributes, "query-projection");
            self.selectAttributeSelection(selectedType);
        };

        /**
         * @function to create empty attribute expression for projection depending on
         * the attributes defined in the connected output element
         */
        FormUtils.prototype.createEmptyAttributesForQueryProjection = function (outputElementName) {
            var self = this;
            var attributes = [];
            var connectedElement = self.configurationData.getSiddhiAppConfig().getDefinitionElementByName(outputElementName);
            _.forEach(connectedElement.element.getAttributeList(), function (attribute) {
                attributes.push({
                    expression: "",
                    as: attribute.getName()
                });
            });
            return attributes;
        };

        /**
         * @function to create the saved user defined attribute expression objects by having the as values as
         * the attributes of the output connected element
         */
        FormUtils.prototype.createAttributesForQueryProjection = function (projectionValues, outputElementName) {
            var self = this;
            var attributes = [];
            var i = 0;
            var connectedElement = self.configurationData.getSiddhiAppConfig().getDefinitionElementByName(outputElementName);
            _.forEach(connectedElement.element.getAttributeList(), function (attribute) {
                var expression = "";
                if (projectionValues[i]) {
                    expression = projectionValues[i].expression;
                }
                attributes.push({
                    expression: expression,
                    as: attribute.getName()
                });
                i++;
            });
            return attributes;
        };

        /**
         * @function to merge the attribute expression and as value
         */
        FormUtils.prototype.getAttributeExpressions = function (projectionValues) {
            var attributes = [];
            _.forEach(projectionValues, function (projection) {
                var attribute = projection.expression;
                if (projection.as != "") {
                    attribute += " " + Constants.AS + " " + projection.as
                }
                attributes.push(attribute);
            });
            return attributes;
        };

        /**
         * @function to get the parameters of a selected type
         * @param {String} selectedType
         * @param {object} predefinedTypes Predefined types
         * @return {object} parameters
         */
        FormUtils.prototype.getSelectedTypeParameters = function (selectedType, predefinedTypes) {
            var parameters = [];
            _.forEach(predefinedTypes, function (type) {
                if (type.name.toLowerCase() == selectedType.toLowerCase()) {
                    if (type.parameters) {
                        parameters = type.parameters;
                    }
                    return false;
                }
            });
            return parameters;
        };

        /**
         * @function to obtain a particular option from predefined options
         * @param {String} optionName option which needs to be found
         * @param {Object} predefinedOptions set of predefined option
         * @return {Object} option
         */
        FormUtils.prototype.getObject = function (optionName, predefinedOptions) {
            var option;
            _.forEach(predefinedOptions, function (predefinedOption) {
                if (predefinedOption.name.toLowerCase() == optionName.toLowerCase()) {
                    option = predefinedOption;
                    return false;
                }
            });
            return option;
        };

        /**
         * @function to identify if atleast one connected input stream has been filled
         */
        FormUtils.prototype.isOneElementFilled = function (inputStreamNames) {
            var isComplete = false;
            _.forEach(inputStreamNames, function (streamName) {
                if (streamName) {
                    isComplete = true;
                    return false;
                }
            });
            return isComplete;
        };

        /**
         * @function to obtain only the stream function names
         */
        FormUtils.prototype.getStreamFunctionNames = function () {
            var self = this;
            var streamFunctions = self.configurationData.rawExtensions["streamFunctions"];
            var streamFunctionNames = [];
            _.forEach(streamFunctions, function (streamFunction) {
                streamFunctionNames.push(streamFunction.name);
            });
            return streamFunctionNames;
        };

        /**
         * @function to obtain customized options from the saved options
         * @param {Object} predefinedOptions Predefined options of a particular annotation type
         * @param {Object} savedOptions saved options
         * @return {Object} customizedOptions
         */
        FormUtils.prototype.getCustomizedOptions = function (predefinedOptions, savedOptions) {
            var customizedOptions = [];
            _.forEach(savedOptions, function (savedOption) {
                var foundSavedOption = false;
                var optionName = savedOption.split('=')[0];
                var optionValue = savedOption.split('=')[1].trim();
                optionValue = optionValue.substring(1, optionValue.length - 1);
                _.forEach(predefinedOptions, function (predefinedOption) {
                    if (predefinedOption.name.toLowerCase() == optionName.toLowerCase().trim()) {
                        foundSavedOption = true;
                        return false;
                    }
                })
                if (!foundSavedOption) {
                    customizedOptions.push({ name: optionName, value: optionValue });
                }
            });
            return customizedOptions;
        };

        /**
         * @function to obtain the user defined annotations from the saved annotations
         * @param {Object} savedAnnotationObjects saved annotation objects
         * @param {Object} predefinedAnnotations predefined annotations
         * @return {Object} userAnnotations
         */
        FormUtils.prototype.getUserAnnotations = function (savedAnnotationObjects, predefinedAnnotations) {
            var userAnnotations = [];
            _.forEach(savedAnnotationObjects, function (savedAnnotation) {
                var isPredefined = false;
                _.forEach(predefinedAnnotations, function (annotation) {
                    if (savedAnnotation.name.toLowerCase() === annotation.name.toLowerCase()) {
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
         * @function to create stream attribute object with empty values
         * @param {Object} streamAttributes
         * @return {Object} streamAttributesObject
         */
        FormUtils.prototype.createStreamAttributesObject = function (streamAttributes) {
            var streamAttributesObject = [];

            _.forEach(streamAttributes, function (attribute) {
                streamAttributesObject.push({ key: attribute.getName(), value: "" });
            })
            return streamAttributesObject;
        };

        /**
         * @function to select the options according to the selected rdbms type
         * @param {Object} savedOptions all the options of rdbms with the user given values
         * @return {Object} rdbmsOptions
         */
        FormUtils.prototype.getRdbmsOptions = function (savedOptions) {
            var self = this;
            var rdbmsOptions = [];
            var selectedRdbmsType = $('input[name=radioOpt]:checked', '#define-rdbms-type').val();
            var predefinedRdbmsOptions = _.cloneDeep(self.configurationData.application.config.
                rdbms_types);
            if (selectedRdbmsType == Constants.DATASOURCE) {
                var datasourceOptions = (_.filter(predefinedRdbmsOptions, function (rdbmsOption) {
                    return rdbmsOption.name == Constants.DATASOURCE
                }))[0].parameters;
                _.forEach(datasourceOptions, function (datasourceOption) {
                    var savedOption = (_.filter(savedOptions, function (rdbmsOption) {
                        return rdbmsOption.name == datasourceOption.name
                    }))[0];
                    rdbmsOptions.push({
                        name: datasourceOption.name, value: savedOption.value, description: datasourceOption
                            .description, optional: datasourceOption.optional, defaultValue: datasourceOption.defaultValue
                    });
                });
            } else if (selectedRdbmsType == Constants.INLINE_CONFIG) {
                var inlineConfigOptions = (_.filter(predefinedRdbmsOptions, function (rdbmsOption) {
                    return rdbmsOption.name == Constants.INLINE_CONFIG
                }))[0].parameters;
                _.forEach(inlineConfigOptions, function (inlineConfigOption) {
                    var savedOption = (_.filter(savedOptions, function (rdbmsOption) {
                        return rdbmsOption.name == inlineConfigOption.name
                    }))[0];
                    rdbmsOptions.push({
                        name: inlineConfigOption.name, value: savedOption.value, description: inlineConfigOption
                            .description, optional: inlineConfigOption.optional, defaultValue: inlineConfigOption
                                .defaultValue
                    });
                });
            } else {
                var jndiResourceOptions = (_.filter(predefinedRdbmsOptions, function (rdbmsOption) {
                    return rdbmsOption.name == Constants.JNDI_RESOURCE
                }))[0].parameters;
                _.forEach(jndiResourceOptions, function (jndiResourceOption) {
                    var savedOption = (_.filter(savedOptions, function (rdbmsOption) {
                        return rdbmsOption.name == jndiResourceOption.name
                    }))[0];
                    rdbmsOptions.push({
                        name: jndiResourceOption.name, value: savedOption.value, description: jndiResourceOption
                            .description, optional: jndiResourceOption.optional, defaultValue: jndiResourceOption.defaultValue
                    });
                });

            }
            return rdbmsOptions;
        };

        /**
         * @function to validate the predefined options
         * @param {Object} predefinedOptions
         * @param {String} id to identify the div in the html to traverse
         * @return {boolean} isError
         */
        FormUtils.prototype.validateOptions = function (predefinedOptions, id) {
            var self = this;
            var isError = false;
            $('#' + id + '-options .option').each(function () {
                var optionName = $(this).find('.option-name').text().trim();
                var optionValue = $(this).find('.option-value').val().trim();
                var predefinedOptionObject = self.getObject(optionName, predefinedOptions);
                if ($(this).find('.option-name').hasClass('mandatory-option')) {
                    if (!self.checkOptionValue(optionValue, predefinedOptionObject, this)) {
                        isError = true;
                        return false;
                    }
                } else {
                    if ($(this).find('.option-checkbox').is(":checked")) {
                        if (!self.checkOptionValue(optionValue, predefinedOptionObject, this)) {
                            isError = true;
                            return false;
                        }
                    }
                }
            });
            return isError;
        };

        /**
         * @function to validate conditions of pattern and sequence query
         */
        FormUtils.prototype.validateConditions = function () {
            var self = this;
            var isErrorOccurred = false;
            $('.define-conditions .condition-content').each(function () {
                var conditionId = $(this).find('.condition-id');
                if (conditionId.val().trim() == "") {
                    $(conditionId).next('.error-message').text('Condition ID is required');
                    self.addErrorClass(conditionId);
                    isErrorOccurred = true;
                }
                if (!isErrorOccurred) {
                    var isValidStreamHandler = self.validateStreamHandlers($(this));
                    if (isValidStreamHandler) {
                        isErrorOccurred = true;
                    }
                }
                if (isErrorOccurred) {
                    $(this).addClass('active');
                    var conditionIndex = $(this).index();
                    $('.define-conditions .nav-tabs .active').removeClass('active');
                    $('.define-conditions .condition-navigation:eq(' + conditionIndex + ')').addClass('active');
                    return false;
                }
            });
            return isErrorOccurred;
        };

        /**
         *
         * @param className the divisions which needs to be validated
         * @returns {boolean}
         */
        FormUtils.prototype.validateRequiredFields = function (className) {
            var self = this;
            var isErrorOccurred = false;
            $(className).each(function () {
                var checkBox = $(this).find('input:checkbox');
                if (checkBox.length == 0 || (checkBox.length > 0 && checkBox.is(":checked"))) {
                    var inputValue = $(this).find('input:text');
                    if (inputValue.val().trim() == "") {
                        self.addErrorClass(inputValue);
                        $(className).find('.error-message').text('Value is required');
                        isErrorOccurred = true;
                    }
                }
            });
            return isErrorOccurred;
        };

        /**
         * @function to expand the collapsed div if any error occurs inside the collapsed div
         */
        FormUtils.prototype.expandCollapsedDiv = function (divToBeExpanded) {
            var collapseHeader = $(divToBeExpanded).find('.collapsed:first');
            var collapseBody = $(divToBeExpanded).find('.collapse:first');
            collapseHeader.attr("aria-expanded", "true");
            collapseHeader.removeClass("collapsed");
            collapseBody.attr("aria-expanded", "true");
            collapseBody.addClass("collapse in");
            collapseBody.css("height", "auto");
            $('.collapse .error-input-field')[0].scrollIntoView();
        };

        /**
         * @function to validate stream handlers
         * @param {String} div identify the div of stream-handler
         */
        FormUtils.prototype.validateStreamHandlers = function (div) {
            var self = this;
            var isErrorOccurred = false;
            var streamHandlerDiv = $(div).find('.define-stream-handler');
            if ($(streamHandlerDiv).find('.stream-handler-checkbox').is(':checked')) {
                $(streamHandlerDiv).find('.stream-handler-list li').each(function () {
                    var streamHandlerContent = $(this).find('.define-stream-handler-type-content');
                    if (streamHandlerContent.hasClass('define-filter-stream-handler')) {
                        var filterCondition = streamHandlerContent.find('.filter-condition-content')
                        if (filterCondition.val().trim() == "") {
                            streamHandlerContent.find('.error-message').text('Filter value is required');
                            self.addErrorClass(filterCondition);
                            self.expandCollapsedDiv($(this));
                            isErrorOccurred = true;
                            return false;
                        }
                    } else if (streamHandlerContent.hasClass('define-window-stream-handler') ||
                        streamHandlerContent.hasClass('define-function-stream-handler')) {
                        var predefinedParameters;
                        if (streamHandlerContent.hasClass('define-window-stream-handler')) {
                            predefinedParameters = self.getPredefinedParameters(streamHandlerContent,
                                self.configurationData.rawExtensions["windowFunctionNames"])
                        } else {
                            predefinedParameters = self.getPredefinedParameters(streamHandlerContent,
                                self.configurationData.rawExtensions["streamFunctions"])
                        }
                        if (self.validateParameters(streamHandlerContent, predefinedParameters)) {
                            self.expandCollapsedDiv($(this));
                            isErrorOccurred = true;
                            return false;
                        }
                    }
                });
            }
            return isErrorOccurred
        };

        /**
         * @function to obtain the predefined parameters of particular window or stream-function type
         */
        FormUtils.prototype.getPredefinedParameters = function (div, predefinedFunctions) {
            var predefinedParameters = [];
            var functionName = $(div).find('.defineFunctionName select').val().toLowerCase()
            _.forEach(predefinedFunctions, function (predefinedFunction) {
                if (functionName == predefinedFunction.name.toLowerCase()) {
                    predefinedParameters = predefinedFunction.parameters;
                    return false;
                }
            });
            return predefinedParameters;
        };

        /**
         * @function for generic validation of parameter values
         * @param {Object} predefinedParameters predefined parameters of the selected window type
         * @return {boolean} isError
         */
        FormUtils.prototype.validateParameters = function (parameterDiv, predefinedParameters) {
            var self = this;
            var isError = false;
            $(parameterDiv).find('.parameter').each(function () {
                var parameterValue = $(this).find('.parameter-value').val().trim();
                var parameterName = $(this).find('.parameter-name').text().trim();;
                var predefinedParameter = self.getObject(parameterName, predefinedParameters);
                if (!predefinedParameter.optional) {
                    if (!self.checkParameterValue(parameterValue, predefinedParameter, this)) {
                        isError = true;
                        return false;
                    }
                } else {
                    if ($(this).find('.parameter-checkbox').is(":checked")) {
                        if (!self.checkParameterValue(parameterValue, predefinedParameter, this)) {
                            isError = true;
                            return false;
                        }
                    }
                }
            });
            return isError;
        };

        /**
         * @function to show autocomplete drop down
         */
        FormUtils.prototype.showDropDown = function () {
            $.widget("custom.combobox", {
                _create: function () {
                    this.wrapper = $("<span>")
                        .addClass("custom-combobox")
                        .insertAfter(this.element);

                    this.element.hide();
                    this._createAutocomplete();
                    this._createShowAllButton();
                },

                _createAutocomplete: function () {
                    var selected = this.element.children(":selected"),
                        value = selected.val() ? selected.text() : "";

                    this.input = $("<input>")
                        .appendTo(this.wrapper)
                        .val(value)
                        .attr("title", "")
                        .addClass("custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-left")
                        .autocomplete({
                            delay: 0,
                            minLength: 0,
                            source: $.proxy(this, "_source")
                        })
                        .tooltip({
                            classes: {
                                "ui-tooltip": "ui-state-highlight"
                            }
                        });

                    this._on(this.input, {
                        autocompleteselect: function (event, ui) {
                            ui.item.option.selected = true;
                            this._trigger("select", event, {
                                item: ui.item.option
                            });
                        },

                        autocompletechange: "_removeIfInvalid"
                    });
                },

                _createShowAllButton: function () {
                    var input = this.input,
                        wasOpen = false;

                    $("<a>")
                        .attr("tabIndex", -1)
                        .attr("title", "Show All Items")
                        .tooltip()
                        .appendTo(this.wrapper)
                        .button({
                            icons: {
                                primary: "ui-icon-triangle-1-s"
                            },
                            text: false
                        })
                        .removeClass("ui-corner-all")
                        .addClass("custom-combobox-toggle ui-corner-right")
                        .on("mousedown", function () {
                            wasOpen = input.autocomplete("widget").is(":visible");
                        })
                        .on("click", function () {
                            input.trigger("focus");

                            // Close if already visible
                            if (wasOpen) {
                                return;
                            }

                            // Pass empty string as value to search for, displaying all results
                            input.autocomplete("search", "");
                        });
                },

                _source: function (request, response) {
                    var matcher = new RegExp($.ui.autocomplete.escapeRegex(request.term), "i");
                    response(this.element.children("option").map(function () {
                        var text = $(this).text();
                        if (this.value && (!request.term || matcher.test(text)))
                            return {
                                label: text,
                                value: text,
                                option: this
                            };
                    }));
                },

                _removeIfInvalid: function (event, ui) {

                    // Selected an item, nothing to do
                    if (ui.item) {
                        return;
                    }

                    // Search for a match (case-insensitive)
                    var value = this.input.val(),
                        valueLowerCase = value.toLowerCase(),
                        valid = false;
                    this.element.children("option").each(function () {
                        if ($(this).text().toLowerCase() === valueLowerCase) {
                            this.selected = valid = true;
                            return false;
                        }
                    });

                    // Found a match, nothing to do
                    if (valid) {
                        return;
                    }

                    // Remove invalid value
                    this.input
                        .val("")
                        .attr("title", value + " didn't match any item")
                        .tooltip("open");
                    this.element.val("");
                    this._delay(function () {
                        this.input.tooltip("close").attr("title", "");
                    }, 2500);
                    this.input.autocomplete("instance").term = "";
                },

                _destroy: function () {
                    this.wrapper.remove();
                    this.element.show();
                }
            });

            $("#stream-function-type").combobox();
            $("#toggle").on("click", function () {
                $("#combobox").toggle();
            });
        };

        /**
         * @function to check the given parameter value
         * @param {String} parameterValue value which needs to be validated
         * @param {Object} predefinedParameter predefined parameter object
         * @param {Object} parent div of the html to locate the parameter
         * @return {boolean}
         */
        FormUtils.prototype.checkParameterValue = function (parameterValue, predefinedParameter, parent) {
            var self = this;
            if (parameterValue === "") {
                $(parent).find('.error-message').text('Parameter Value is required.');
                self.addErrorClass($(parent).find('.parameter-value'));
                return false;
            } else {
                var dataType = predefinedParameter.type;
                if (self.validateDataType(dataType, parameterValue)) {
                    var errorMessage = "Invalid data-type. ";
                    _.forEach(dataType, function (type) {
                        errorMessage += type + " or ";
                    });
                    errorMessage = errorMessage.substring(0, errorMessage.length - 4);
                    errorMessage += " is required";
                    $(parent).find('.error-message').text(errorMessage);
                    self.addErrorClass($(parent).find('.parameter-value'));
                    return false;
                }
            }
            return true;
        };

        /**
         * @function to validate the group-by attributes
         */
        FormUtils.prototype.validateGroupOrderBy = function (className) {
            var self = this;
            var selectedAttributes = [];
            var isErrorOccurred = false;
            $('.' + className + '-attributes li').each(function () {
                var selectedValue = $(this).find('.' + className + '-selection').val();
                if (selectedValue) {
                    selectedAttributes.push(selectedValue);
                }
            });
            if (selectedAttributes.length == 0) {
                $('.' + className + '-attributes').find('.error-message:eq(0)').text('Minimum one attribute is required');
                self.addErrorClass($('.' + className + '-attributes').find('.define-attribute-drop-down:eq(0)'))
                isErrorOccurred = true;
            }
            return isErrorOccurred;
        };

        /**
         * @function to validate the predefined annotations
         */
        FormUtils.prototype.validatePredefinedAnnotations = function (predefinedAnnotations) {
            var self = this;
            var isErrorOccurred = false;
            var isCheckOptions = false;
            _.forEach(predefinedAnnotations, function (annotation) {
                if (annotation.parameters) {
                    if (annotation.optional) {
                        annotationCheckbox = $('#' + annotation.name + '-annotation').find('.annotation-checkbox').
                            first();
                        if (annotationCheckbox.is(':checked') && !annotationCheckbox.is(':disabled')) {
                            isCheckOptions = true;
                        }
                    } else {
                        isCheckOptions = true;
                    }
                    if (isCheckOptions) {
                        if (self.validateOptions(annotation.parameters, annotation.name)) {
                            isErrorOccurred = true;
                            return false;
                        }
                    }
                }
                if (annotation.annotations) {
                    self.validatePredefinedAnnotations(annotation.annotations)
                }
            });
            return isErrorOccurred;
        };

        /**
         * @function to validate the data type of a given value
         * @param {Objects} dataType possible data-types
         * @param {String} optionValue value which needs to be checked for
         * @return {boolean} invalidDataType
         */
        FormUtils.prototype.validateDataType = function (dataTypes, value) {
            var invalidDataType = false;
            _.forEach(dataTypes, function (dataType) {
                dataType = dataType.toLowerCase();
                if (dataType === Constants.INT || dataType === Constants.LONG) {
                    if (!value.match(Constants.INT_LONG_VALIDATOR_REGEX)) {
                        invalidDataType = true;
                    } else {
                        invalidDataType = false;
                        return false;
                    }
                } else if (dataType === Constants.DOUBLE || dataType === Constants.FLOAT) {
                    if (!value.match(Constants.DOUBLE_FLOAT_VALIDATOR_REGEX)) {
                        invalidDataType = true;
                    } else {
                        invalidDataType = false;
                        return false;
                    }
                } else if (dataType === Constants.BOOL) {
                    if (!(value.toLowerCase() === "false" || value.toLowerCase() === "true")) {
                        invalidDataType = true;
                    } else {
                        invalidDataType = false;
                        return false;
                    }
                } else if (dataType === Constants.TIME) {
                    if (!value.match(Constants.TIME_VALIDATOR_REGEX)) {
                        invalidDataType = true;
                    } else {
                        invalidDataType = false;
                        return false;
                    }
                }
            });
            return invalidDataType;
        };

        /**
         * @function to validate the customized options
         * @param {String} id to identify the div in the html to traverse
         * @return {boolean} isError
         */
        FormUtils.prototype.validateCustomizedOptions = function (id) {
            var self = this;
            var isError = false;
            if ($('#customized-' + id + '-options ul').has('li').length != 0) {
                $('#customized-' + id + '-options .option').each(function () {
                    var custOptName = $(this).find('.cust-option-key');
                    var custOptValue = $(this).find('.cust-option-value');
                    if ((custOptName.val().trim() != "") || (custOptValue.val().trim() != "")) {
                        if (custOptName.val().trim() == "") {
                            self.addErrorClass(custOptName);
                            custOptName.parent().next('.error-message').text('Option key is required.');
                            isError = true;
                            return false;
                        } else if (custOptValue.val().trim() == "") {
                            custOptValue.parent().next('.error-message').text('Option value is required.');
                            self.addErrorClass(custOptValue);
                            isError = true;
                            return false;
                        }
                    }
                });
            }
            return isError;
        };

        /**
         * @function to check if a particular value is valid
         * @param {String} optionValue value which needs to be validated
         * @param {Object} predefinedOptionObject predefined object of the option
         * @param {Object} parent div of the particular option
         */
        FormUtils.prototype.checkOptionValue = function (optionValue, predefinedOptionObject, parent) {
            var self = this;
            if (optionValue == "") {
                $(parent).find('.error-message').text('Option value is required.');
                self.addErrorClass($(parent).find('.option-value'));
                return false;
            } else {
                if (self.validateDataType(predefinedOptionObject.type, optionValue)) {
                    $(parent).find('.error-message').text('Invalid data-type. ' + predefinedOptionObject.type[0] +
                        ' required.');
                    self.addErrorClass($(parent).find('.option-value'));
                    return false;
                }
            }
            return true;
        };

        /**
         * @function to validate the user defined attribute selection (projection)
         */
        FormUtils.prototype.validateAggregateProjection = function (possibleAttributes) {
            var self = this;
            var isErrorOccurred = false;
            var errorMessage = ""
            var attributes = 0;
            if ($('.define-select .attribute-selection-type').val() == Constants.TYPE_USER_DEFINED) {
                $('.define-select .user-defined-attributes li').each(function () {
                    var expressionAs = $(this).find('.attribute-expression-as');
                    var expressionAsValue = expressionAs.val().trim();
                    var separateExpression = expressionAsValue.split(/as/i);
                    if (expressionAsValue != "") {
                        attributes++;
                        if (separateExpression.length == 1 && !possibleAttributes.includes(expressionAsValue)) {
                            isErrorOccurred = true;
                            errorMessage = "As value is required."
                        } else if (separateExpression.length == 2 && separateExpression[1].trim() == "") {
                            isErrorOccurred = true;
                            errorMessage = "As value is required."
                        } else if (separateExpression.length > 2) {
                            isErrorOccurred = true;
                            errorMessage = "Only one as value is required."
                        }
                        if (isErrorOccurred) {
                            $(this).find('.error-message').text(errorMessage);
                            self.addErrorClass(expressionAs);
                            return false;
                        }
                    }
                });

                if (attributes == 0) {
                    isErrorOccurred = true;
                    var firstAttributeList = '.user-defined-attributes li:first';
                    $(firstAttributeList).find('.error-message').text("Minimum one attribute is required.")
                    self.addErrorClass($(firstAttributeList).find('.attribute-expression-as'));
                }
            }
            return isErrorOccurred;
        };

        /**
         * @function to validate the user defined attribute selection (queries)
         */
        FormUtils.prototype.validateQueryProjection = function () {
            var self = this;
            var isErrorOccurred = false;
            if ($('.define-select .attribute-selection-type').val() == Constants.TYPE_USER_DEFINED) {
                $('.define-select .user-defined-attributes li').each(function () {
                    var expression = $(this).find('.attribute-expression').val().trim();
                    if (expression == "") {
                        isErrorOccurred = true;
                        $(this).find('.error-message').text("Expression Value is required");
                        self.addErrorClass($(this).find('.attribute-expression'));
                        return false;
                    }
                });
            }
            return isErrorOccurred;
        };

        /**
         * @function validates the names
         * @param {Object} id to find the error-message label
         * @param {String} type Attribue or any element
         * @param {String} name the name to be validated
         * @return {boolean}
         */
        FormUtils.prototype.validateAttributeOrElementName = function (id, type, name) {
            var self = this;
            var errorMessageLabel;
            if (type === Constants.ATTRIBUTE) {
                errorMessageLabel = $(id).parents(".attribute").find(".error-message");
            } else {
                errorMessageLabel = $(id + 'ErrorMessage');
            }

            if (name.indexOf(' ') >= 0) {
                errorMessageLabel.text(self.capitalizeFirstLetter(type) + " name can not have white space.")
                self.addErrorClass(id);
                return true;
            }
            if (!Constants.ALPHABETIC_VALIDATOR_REGEX.test(name.charAt(0))) {
                errorMessageLabel.text
                    (self.capitalizeFirstLetter(type) + " name must start with an alphabetical character.");
                self.addErrorClass(id);
                return true;
            }
            return false;
        };

        /**
         * @function validate the attributes
         * @param {Object} attributeNameList to add the valid attributes
         * @return {boolean} isErrorOccurred
         */
        FormUtils.prototype.validateAttributes = function (attributeNameList) {
            var self = this;
            var isErrorOccurred = false;
            $('.attr-name').each(function () {
                var attributeName = $(this).val().trim();
                if (attributeName != "") {
                    var isError = self.validateAttributeOrElementName(this, Constants.ATTRIBUTE, attributeName);
                    if (!isError) {
                        attributeNameList.push(attributeName)
                    } else {
                        isErrorOccurred = true;
                    }
                }
            });
            return isErrorOccurred;
        };

        /**
         * @function to generate the group-by div
         */
        FormUtils.prototype.generateGroupByDiv = function (savedGroupBy, possibleAttributes) {
            var self = this;
            var groupByAttributes = [""];
            if ((savedGroupBy && savedGroupBy.length != 0)) {
                groupByAttributes = savedGroupBy.slice();
            }
            self.renderGroupBy(possibleAttributes, groupByAttributes);
            self.addEventListenersForGroupByDiv(possibleAttributes);
            //removes the first delete button
            $('.define-group-by-attributes').find('.group-by-attributes li:eq(0) .btn-del-option').remove();
            self.checkForAttributeLength(possibleAttributes.length, Constants.GROUP_BY);

            if (savedGroupBy && savedGroupBy.length != 0) {
                self.mapUserGroupBy(savedGroupBy);
                self.preventMultipleSelection(Constants.GROUP_BY);
                $(".group-by-checkbox").prop("checked", true);
            } else {
                $('.group-by-content').hide();
            }
        };

        /**
         * @function to generate the order-by div
         */
        FormUtils.prototype.generateOrderByDiv = function (savedOrderBy, possibleAttributes) {
            var self = this;
            var orderByAttributes = [""];
            if ((savedOrderBy && savedOrderBy.length != 0)) {
                orderByAttributes = savedOrderBy.slice();
            }
            self.renderOrderBy(possibleAttributes, orderByAttributes);
            self.addEventListenersForOrderByDiv(possibleAttributes);
            //removes delete button of the first order by
            $('.define-order-by-attributes').find('.order-by-attributes li:eq(0) .btn-del-option').remove();

            if (savedOrderBy && savedOrderBy.length != 0) {
                self.mapUserOrderBy(orderByAttributes);
                self.preventMultipleSelection(Constants.ORDER_BY);
                $(".order-by-checkbox").prop("checked", true);
            } else {
                $('.order-by-content').hide();
            }
        };

        /**
         * @function to build the select object
         */
        FormUtils.prototype.buildAttributeSelection = function (elementType) {
            var self = this;
            var selectAttributeOptions = {}
            var selectionType = $('.attribute-selection-type').val();
            if (selectionType == Constants.TYPE_ALL) {
                _.set(selectAttributeOptions, 'type', Constants.TYPE_ALL.toUpperCase());
                _.set(selectAttributeOptions, 'value', Constants.VALUE_ALL);
            } else {
                _.set(selectAttributeOptions, 'type', Constants.TYPE_USER_DEFINED.toUpperCase());
                if (elementType == Constants.AGGREGATION) {
                    var attributeExpressions = self.buildAggregateExpressions();
                } else {
                    var attributeExpressions = self.buildQueryExpressions();
                }
                _.set(selectAttributeOptions, 'value', attributeExpressions);
            }
            return selectAttributeOptions;
        };

        /**
         * @function to build the user defined attributes for queries
         */
        FormUtils.prototype.buildQueryExpressions = function () {
            var attributes = [];
            $('.define-select .user-defined-attributes li').each(function () {
                var expressionValue = $(this).find('.attribute-expression').val().trim();
                var asValue = $(this).find('.attribute-as').val().trim();
                var expressionObject = {
                    expression: expressionValue,
                    as: asValue
                }
                attributes.push(expressionObject);
            });
            return attributes;
        };

        /**
         * @function to build the user defined attributes for aggregation
         */
        FormUtils.prototype.buildAggregateExpressions = function () {
            var attributes = [];
            $('.define-select .user-defined-attributes li').each(function () {
                var expressionAsValue = $(this).find('.attribute-expression-as').val().trim();
                if (expressionAsValue != "") {
                    var separateExpression = expressionAsValue.split(/as/i);
                    if (separateExpression.length == 1) {
                        var expressionAs = {
                            expression: separateExpression[0].trim(),
                            as: ""
                        }
                    } else if (separateExpression.length = 2) {
                        var asValue = "";
                        if (separateExpression[0].trim() != separateExpression[1].trim()) {
                            asValue = separateExpression[1].trim();
                        }
                        var expressionAs = {
                            expression: separateExpression[0].trim(),
                            as: asValue
                        }
                    }
                    attributes.push(expressionAs)
                }
            });
            return attributes;
        };

        /**
         * @function to build the options
         * @param {Object} selectedOptions array to add the built options
         * @param {String} id to identify the div in the html to traverse
         */
        FormUtils.prototype.buildOptions = function (selectedOptions, id) {
            var option;
            $('#' + id + '-options .option').each(function () {
                var optionName = $(this).find('.option-name').text().trim();
                var optionValue = $(this).find('.option-value').val().trim();
                if ($(this).find('.option-name').hasClass('mandatory-option')) {
                    option = optionName + " = \"" + optionValue + "\"";
                    selectedOptions.push(option);
                } else {
                    if ($(this).find('.option-checkbox').is(":checked")) {
                        option = optionName + " = \"" + optionValue + "\"";
                        selectedOptions.push(option);
                    }
                }
            });
        };

        /**
         * @function to build pattern or sequence conditions
         */
        FormUtils.prototype.buildConditions = function () {
            var self = this;
            var conditions = [];
            $('.define-conditions .condition-content').each(function () {
                var conditionId = $(this).find('.condition-id').val().trim();
                var streamName = $(this).find('.condition-stream-name-selection').val();
                var conditionObjectOptions = {};
                _.set(conditionObjectOptions, 'conditionId', conditionId);
                _.set(conditionObjectOptions, 'streamName', streamName);
                var conditionObject = new PatternOrSequenceQueryCondition(conditionObjectOptions);
                conditionObject.setStreamHandlerList(self.buildStreamHandlers($(this).find('.define-stream-handler')));
                conditions.push(conditionObject);
            });
            return conditions
        };

        /**
         * @function to build the stream-handlers
         * @param {Object} sourceDiv where the stream handler is embedded
         */
        FormUtils.prototype.buildStreamHandlers = function (sourceDiv) {
            var self = this;
            var streamHandlers = [];
            if (sourceDiv.find('.stream-handler-checkbox').is(':checked')) {
                sourceDiv.find('.stream-handler-list li').each(function () {
                    var streamHandlerOptions = {};
                    var streamHandlerContent = $(this).find('.define-stream-handler-type-content');
                    if (streamHandlerContent.hasClass('define-filter-stream-handler')) {
                        var filterCondition = streamHandlerContent.find('.filter-condition-content').val().trim()
                        _.set(streamHandlerOptions, 'type', Constants.FILTER.toUpperCase());
                        _.set(streamHandlerOptions, 'value', filterCondition);
                    } else if (streamHandlerContent.hasClass('define-window-stream-handler') ||
                        streamHandlerContent.hasClass('define-function-stream-handler')) {
                        var functionName = streamHandlerContent.find('.defineFunctionName select').val();
                        var parameters;
                        var predefinedFunctions;
                        var handlerType;
                        var predefinedParameters;
                        if (streamHandlerContent.hasClass('define-window-stream-handler')) {
                            predefinedFunctions = self.configurationData.rawExtensions["windowFunctionNames"]
                            parameters = self.buildWindowParameters(streamHandlerContent, functionName, predefinedFunctions)
                            handlerType = Constants.WINDOW.toUpperCase()
                        } else {
                            predefinedFunctions = self.configurationData.rawExtensions["streamFunctions"]
                            predefinedParameters = self.getPredefinedParameters(streamHandlerContent, predefinedFunctions);
                            parameters = self.buildParameterValues(streamHandlerContent, predefinedParameters)
                            handlerType = Constants.FUNCTION.toUpperCase()
                        }
                        var windowFunctionOptions = {};
                        _.set(windowFunctionOptions, 'function', functionName);
                        _.set(windowFunctionOptions, 'parameters', parameters);
                        var queryWindowFunction = new QueryWindowOrFunction(windowFunctionOptions);
                        _.set(streamHandlerOptions, 'type', handlerType);
                        _.set(streamHandlerOptions, 'value', queryWindowFunction);
                    }
                    var streamHandlerObject = new StreamHandler(streamHandlerOptions);
                    streamHandlers.push(streamHandlerObject);
                });
            }
            return streamHandlers;
        };

        /**
         * @function to build the parameters of window
         */
        FormUtils.prototype.buildWindowParameters = function (div, selectedType, predefinedFunctions) {
            var self = this;
            var parameters = [];
            var predefinedParameters = self.getPredefinedParameters(div, predefinedFunctions)
            if (selectedType.toLowerCase() === Constants.SORT) {
                parameters = self.buildParameterValuesSort(div)
            } else if (selectedType.toLowerCase() === Constants.FREQUENT ||
                selectedType.toLowerCase() === Constants.LOSSY_FREQUENT) {
                parameters = self.buildParameterValuesFrequentOrLossyFrequent(div,
                    predefinedParameters)
            } else {
                parameters = self.buildParameterValues(div, predefinedParameters);
            }
            return parameters;
        };

        /**
         * @function to build the parameter values
         * @param {Object} div division where the parameters are in html
         * @param {Object} predefinedParameters predefined parameters
         */
        FormUtils.prototype.buildParameterValues = function (div, predefinedParameters) {
            var self = this;
            var parameterValues = [];
            $(div).find('.parameter').each(function () {
                if ($(this).find('.parameter-name').hasClass('mandatory-parameter') || ($(this).find('.parameter-name')
                    .hasClass('optional-parameter') && $(this).find('.parameter-checkbox').is(":checked"))) {
                    var parameterValue = $(this).find('.parameter-value').val().trim();
                    var parameterName = $(this).find('.parameter-name').text().trim();;
                    var predefinedParameter = self.getObject(parameterName, predefinedParameters);
                    if (predefinedParameter.type.includes("STRING")) {
                        parameterValue = "'" + parameterValue + "'";
                    }
                    parameterValues.push(parameterValue);
                }
            });
            return parameterValues;
        };

        /**
         * @function to build parameters for sort type
         * @param {Object} div where the parameters are in html
         * @returns {Object} parameterValues
         */
        FormUtils.prototype.buildParameterValuesSort = function (div) {
            var self = this;
            var parameterValues = [];
            $(div).find('.parameter').each(function () {
                var parameterValue = $(this).find('.parameter-value').val().trim();
                var parameterName = $(this).find('.parameter-name').text().trim();;
                if (parameterName === "window.length") {
                    parameterValues.push(parameterValue)
                } else if (parameterName === "attribute") {
                    if ($('#attribute-parameter').find('.parameter-checkbox').is(":checked")) {
                        self.buildAttributes(parameterValue, parameterValues);
                    }
                } else {
                    if (($('#attribute-parameter').find('.parameter-checkbox').is(":checked")) && ($
                        ('#order-parameter').find('.parameter-checkbox').is(":checked"))) {
                        parameterValue = "'" + parameterValue + "'";
                        parameterValues.push(parameterValue)
                    }
                }
            });
            return parameterValues;
        };

        /**
         * @function to build parameters for frequent and lossyFrequent type
         * @param {Object} div where the parameters are in html
         * @param {Object} predefinedParameters predefined parameters
         * @returns {Object} parameterValues
         */
        FormUtils.prototype.buildParameterValuesFrequentOrLossyFrequent = function (div, predefinedParameters) {
            var self = this;
            var parameterValues = [];
            $(div).find('.parameter').each(function () {
                if ($(this).find('.parameter-name').hasClass('mandatory-parameter') || ($(this).find('.parameter-name')
                    .hasClass('optional-parameter') && $(this).find('.parameter-checkbox').is(":checked"))) {
                    var parameterValue = $(this).find('.parameter-value').val().trim();
                    var parameterName = $(this).find('.parameter-name').text().trim();
                    var predefinedParameter = self.getObject(parameterName, predefinedParameters);
                    if (parameterName === "attribute") {
                        self.buildAttributes(parameterValue, parameterValues);
                    } else {
                        if (predefinedParameter.type.includes("STRING")) {
                            parameterValue = "'" + parameterValue + "'";
                        }
                        parameterValues.push(parameterValue)
                    }
                }
            });
            return parameterValues;
        };

        /**
         * @function to construct parameter 'attributes'
         * @param {String} parameterValue the attribute value
         * @param {Object} parameterValues array to add the parameters
         */
        FormUtils.prototype.buildAttributes = function (parameterValue, parameterValues) {
            var attributeArray = parameterValue.split(',');
            _.forEach(attributeArray, function (attribute) {
                attribute = attribute.trim();
                if (attribute !== "") {
                    parameterValues.push(attribute);
                }
            });
        };

        /**
         * @function to build the elements of predefined annotations
         * @param {String} id to refer to the html where the elements are embedded of a particular annotation
         */
        FormUtils.prototype.buildAnnotationElements = function (id) {
            var elements = []
            $('#' + id + '-options .option').each(function () {
                var option = $(this).find('.option-name');
                var optionName = option.text().trim();
                var optionValue = $(this).find('.option-value').val().trim();
                if (option.hasClass('mandatory-option')) {
                    elements.push({ key: optionName, value: optionValue });
                } else {
                    if ($(this).find('.option-checkbox').is(":checked")) {
                        elements.push({ key: optionName, value: optionValue });
                    }
                }
            });
            return elements;
        };

        /**
         * @function to build the customized options
         * @param {Object} selectedOptions array to add the built option
         * @param {String} id to identify the div in the html to traverse
         */
        FormUtils.prototype.buildCustomizedOption = function (selectedOptions, id) {
            var option = "";
            if ($('#customized-' + id + '-options ul').has('li').length != 0) {
                $('#customized-' + id + '-options .option').each(function () {
                    var custOptName = $(this).find('.cust-option-key').val().trim();
                    var custOptValue = $(this).find('.cust-option-value').val().trim();
                    if ((custOptName != "") && (custOptValue != "")) {
                        option = custOptName + " = \"" + custOptValue + "\"";
                        selectedOptions.push(option);
                    }
                });
            }
        };

        /**
         * @function to build the primary index
         * @param {Object} annotationList array to add the built string annotations
         * @param {Object} annotationObjectList array to add the annotation objects
         */
        FormUtils.prototype.buildPrimaryIndexAnnotations = function (annotationList, annotationObjectList) {
            $('#primary-index-annotations .annotation').each(function () {
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
         * @function to determine if the annotation is to be built
         */
        FormUtils.prototype.isBuildAnnotation = function (annotation) {
            var isBuildAnnotation = false;
            if (annotation.optional) {
                annotationCheckbox = $('#' + annotation.name + '-annotation').find('.annotation-checkbox').
                    first();
                if (annotationCheckbox.is(':checked') && !annotationCheckbox.is(':disabled')) {
                    isBuildAnnotation = true;
                }
            } else {
                isBuildAnnotation = true;
            }
            return isBuildAnnotation;
        };

        /**
         * @function to traverse through all parent predefined annotation
         */
        var predefinedAnnotationString = "";
        FormUtils.prototype.buildPredefinedAnnotations = function (predefinedAnnotations, annotationStringList,
            annotationObjectList) {
            var self = this;
            _.forEach(predefinedAnnotations, function (annotation) {
                if (self.isBuildAnnotation(annotation)) {
                    var annotationObject = new AnnotationObject();
                    annotationObject.setName(annotation.name);
                    predefinedAnnotationString += "@" + annotation.name + "(";
                    self.buildPredefinedAnnotation(annotation, annotationObject);

                    predefinedAnnotationString += ")"
                    annotationObjectList.push(annotationObject)
                    annotationStringList.push(predefinedAnnotationString);
                    predefinedAnnotationString = "";
                }
            });
        };

        /**
         * @function to traverse through the sub annotations
         */
        FormUtils.prototype.buildPredefinedAnnotation = function (annotation, annotationObject) {
            var self = this;
            if (self.isBuildAnnotation(annotation)) {
                if (annotation.annotations) {
                    _.forEach(annotation.annotations, function (subAnnotation) {
                        if (self.isBuildAnnotation(subAnnotation)) {
                            predefinedAnnotationString += "@" + subAnnotation.name + "( "
                            var childAnnotation = new AnnotationObject();
                            childAnnotation.setName(subAnnotation.name)
                            self.buildPredefinedAnnotation(subAnnotation, childAnnotation)
                            annotationObject.addAnnotation(childAnnotation)
                            predefinedAnnotationString = predefinedAnnotationString.substring(0,
                                predefinedAnnotationString.length - 1);
                            predefinedAnnotationString += "),"
                        }
                    });
                }
                if (annotation.parameters) {
                    self.buildPredefinedAnnotationElements(annotation, annotationObject);
                }
            }
        };

        /**
         * @function to build the options of an annotation
         */
        FormUtils.prototype.buildPredefinedAnnotationElements = function (annotation, annotationObject) {
            var self = this;
            var elements = self.buildAnnotationElements(annotation.name);
            _.forEach(elements, function (element) {
                predefinedAnnotationString += element.key + "="
                predefinedAnnotationString += "'" + element.value + "' ,";
                var newElement = new AnnotationElement(element.key, element.value);
                annotationObject.addElement(newElement);
            })

            if (elements.length != 0) {
                predefinedAnnotationString = predefinedAnnotationString.substring(0, predefinedAnnotationString.length - 1);
            }
        };

        /**
         * @function to create the map section of the source/sink annotation
         * @param {Object} predefinedMaps predefined mappers
         */
        FormUtils.prototype.buildMapSection = function (predefinedMaps, mapperOptions) {
            var self = this;
            var customizedMapperOptions = [];
            self.renderMap(predefinedMaps);
            mapperOptions = self.getSelectedTypeParameters(Constants.DEFAULT_MAPPER_TYPE, predefinedMaps);
            var mapperOptionsWithValues = self.createObjectWithValues(mapperOptions);
            self.renderOptions(mapperOptionsWithValues, customizedMapperOptions, Constants.MAPPER)
        };

        /**
         * @function to create option object with an additional empty value
         * @param {Object} objectArray Predefined objects without the 'value' attribute
         * @return {Object} objects
         */
        FormUtils.prototype.createObjectWithValues = function (objectArray) {
            var objects = [];
            _.forEach(objectArray, function (object) {
                objects.push({
                    name: object.name, value: "", description: object.description, optional: object.optional,
                    defaultValue: object.defaultValue, type: object.type
                });
            });
            return objects;
        };

        /**
         * @function to map the user saved conditions
         */
        FormUtils.prototype.mapConditions = function (conditionList) {
            var self = this;
            var streamHandlerTypes = self.configurationData.application.config.stream_handler_types_without_window;
            if (conditionList && conditionList.length != 0) {
                _.forEach(conditionList, function (condition) {
                    //select the stream name
                    $('.define-' + condition.conditionId + '-source').find('.define-stream select option').filter
                        (function () {
                            return ($(this).val() == condition.streamName);
                        }).prop('selected', true);

                    //render and map stream handler
                    self.renderStreamHandler(condition.conditionId, condition, streamHandlerTypes)
                    self.mapStreamHandler(condition, condition.conditionId)
                });
            }
        };

        /**
         * @function to map the stream-handlers
         * @param {Object} savedData the saved object which holds the streamhandler list
         * @param {String} className the identification of the div where the stream-handlers are embedded
         */
        FormUtils.prototype.mapStreamHandler = function (savedData, className) {
            var self = this;
            var sourceDiv = '.define-' + className + '-source .define-stream-handler';
            if (savedData && savedData.streamHandlerList && savedData.streamHandlerList.length != 0) {
                $(sourceDiv).find('.stream-handler-checkbox').prop('checked', true);
                var streamHandlerList = savedData.streamHandlerList;
                var i = 0;
                $(sourceDiv).find('.define-stream-handler-content').each(function () {
                    var streamHandlerType = streamHandlerList[i].getType().toLowerCase();
                    var streamHandlerContent = $(this).find('.define-stream-handler-type-content');
                    streamHandlerContent.addClass('define-' + streamHandlerType + '-stream-handler')
                    self.renderStreamHandlerContentDiv(streamHandlerType, streamHandlerContent);
                    self.mapStreamHandlerContent(streamHandlerContent, streamHandlerList[i])
                    self.selectHandlerSelection($(this).find('.stream-handler-selection'), streamHandlerType)
                    i++;
                });
            } else {
                $(sourceDiv).find('.define-stream-handler-section').hide();
                $(sourceDiv).find('.define-stream-handler-buttons').hide();
            }
            self.changeAttributeNavigation($(sourceDiv).find('.stream-handler-list'));
            self.removeNavigationForWindow(sourceDiv)
            self.showHideStreamHandlerWindowButton(sourceDiv)
            self.preventMultipleSelectionOfWindowStreamHandler(sourceDiv)
            self.addEventListenersForParameterDiv();
            self.showDropDown();
        };

        /**
         * @function to select the first condition as default
         */
        FormUtils.prototype.selectFirstConditionByDefault = function () {
            $('.define-conditions .nav-tabs').find('li:first-child').addClass('active');
            $('.define-conditions .tab-content').find('.tab-pane:first-child').addClass('active');
        };

        /**
         * @function to map the stream handler content depending on the type
         */
        FormUtils.prototype.mapStreamHandlerContent = function (div, streamHandler) {
            var self = this;
            var type = streamHandler.type.toLowerCase();
            if (type === Constants.FILTER) {
                $(div).find('.filter-condition-content').val(streamHandler.value)
            } else if (type === Constants.WINDOW) {
                var predefinedWindowFunctions = _.orderBy(JSON.parse(JSON.stringify
                    (self.configurationData.rawExtensions["windowFunctionNames"]), ['name'], ['asc']));
                self.renderFunctions(predefinedWindowFunctions, div, Constants.WINDOW);
                self.mapStreamHandlerWindow(div, predefinedWindowFunctions, streamHandler);
            } else if (type === Constants.FUNCTION) {
                var predefinedStreamFunctions = _.orderBy(JSON.parse(JSON.stringify
                    (self.configurationData.rawExtensions["streamFunctions"]), ['name'], ['asc']));
                self.renderFunctions(predefinedStreamFunctions, div, Constants.STREAM_FUNCTION);
                self.mapStreamHandlerStreamFunction(div, predefinedStreamFunctions, streamHandler)
            }
        };

        /**
         * @function to map the window stream handler
         */
        FormUtils.prototype.mapStreamHandlerWindow = function (div, predefinedFunctions, streamHandler) {
            var self = this;
            var windowType = streamHandler.value.function.toLowerCase();
            $(div).find('#window-type option').filter(function () {
                return ($(this).val().toLowerCase() == (windowType));
            }).prop('selected', true);
            var savedParameterValues = streamHandler.value.parameters;
            var functionParameters = self.getSelectedTypeParameters(windowType, predefinedFunctions);
            self.callToMapParameters(windowType, functionParameters, savedParameterValues, div)
            if (windowType === Constants.SORT) {
                self.showHideOrderForSort();
                self.addEventListenerForSortWindow(selectedType)
            }
        };

        /**
         * @function to map the stream function stream handler
         */
        FormUtils.prototype.mapStreamHandlerStreamFunction = function (div, predefinedFunctions, streamHandler) {
            var self = this;
            var functionType = streamHandler.value.function.toLowerCase();
            $(div).find('#stream-function-type option').filter(function () {
                return ($(this).val().toLowerCase() == (functionType));
            }).prop('selected', true);
            var savedParameterValues = streamHandler.value.parameters;
            var functionParameters = self.getSelectedTypeParameters(functionType, predefinedFunctions);
            var functionParametersWithValues = self.mapUserParameterValues(functionParameters, savedParameterValues);
            self.renderParameters(functionParametersWithValues, Constants.STREAM_FUNCTION, div);
        };

        /**
         * @function to map the values of saved annotation to predefined annotation object
         * @param {Object} predefinedAnnotations
         * @param {Object} savedAnnotations
         */
        FormUtils.prototype.mapPrimaryIndexAnnotationValues = function (predefinedAnnotations, savedAnnotations) {
            _.forEach(savedAnnotations, function (savedAnnotation) {
                _.forEach(predefinedAnnotations, function (predefinedAnnotation) {
                    if (savedAnnotation.name.toLowerCase() === predefinedAnnotation.name.toLowerCase()) {
                        predefinedAnnotation.isChecked = true;
                        predefinedAnnotation.values = [];
                        _.forEach(savedAnnotation.elements, function (element) {
                            predefinedAnnotation.values.push({ value: element.value });
                        })
                        return false;
                    }
                })
            });
        };

        /**
        * @function to map the saved parameter values to the parameter object
        * @param {Object} predefinedParameters Predefined parameters of a particular window type
        * @param {Object} savedParameterValues Saved parameter values
        * @return {Object} parameters
        */
        FormUtils.prototype.mapUserParameterValues = function (predefinedParameters, savedParameterValues) {
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

        /**
         * @function to map the user saved parameters of lossyFrequent
         * @param {Object} predefinedParameters predefined parameters
         * @param {Object} savedParameterValues user saved parameters
         * @return {Object} parameters
         *
         */
        FormUtils.prototype.mapParameterValuesLossyFrequent = function (predefinedParameters, savedParameterValues) {
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
         * @function to map the user saved parameters of frequent
         * @param {Object} predefinedParameters predefined parameters
         * @param {Object} savedParameterValues user saved parameters
         * @return {Object} parameters
         */
        FormUtils.prototype.mapParameterValuesFrequent = function (predefinedParameters, savedParameterValues) {
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
         * @function to map the user saved parameters of sort
         * @param {Object} predefinedParameters predefined parameters
         * @param {Object} savedParameterValues user saved parameters
         * @return {Object} parameters
         */
        FormUtils.prototype.mapParameterValuesSort = function (predefinedParameters, savedParameterValues) {
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

        /**
         * Function to select the parameter mapping method
         * @param {String} selectedType selected window type
         * @param {Object} functionParameters parameters of the selected window type
         * @param {Object} savedParameterValues saved parameter values
         * @param {Object} functionParametersWithValues array to hold the parameter of the mapped value
         */
        FormUtils.prototype.callToMapParameters = function (selectedType, functionParameters, savedParameterValues,
            parameterDiv) {
            var self = this;
            var functionParametersWithValues;
            if (selectedType === Constants.SORT) {
                functionParametersWithValues = self.mapParameterValuesSort(functionParameters, savedParameterValues);
            } else if (selectedType === Constants.FREQUENT) {
                functionParametersWithValues = self.mapParameterValuesFrequent(functionParameters,
                    savedParameterValues);
            } else if (selectedType === Constants.LOSSY_FREQUENT) {
                functionParametersWithValues = self.mapParameterValuesLossyFrequent(functionParameters,
                    savedParameterValues);
            } else {
                functionParametersWithValues = self.mapUserParameterValues(functionParameters, savedParameterValues);
            }
            self.renderParameters(functionParametersWithValues, Constants.WINDOW, parameterDiv);
        };

        /**
         * @function to map the user given values for group-by
         * @param {Object} attributes user saved group-by-attributes
         */
        FormUtils.prototype.mapUserGroupBy = function (attributes) {
            var i = 0;
            $('.group-by-attributes li').each(function () {
                $(this).find('.group-by-selection option').filter(function () {
                    return ($(this).val() == (attributes[i]));
                }).prop('selected', true);
                i++;
            });
        };

        /**
         * @function to map the user saved values of order-by attributes
         * @param {String} replacedAttributes attribute names which were replaced (only for join-query as because
         *  there are two source the attribute names were changed to <sourceName>.<attributeName>).
         *  It would be the same for the other forms
         * @param {orderByAttributes} orderByAttributes saved order by attributes
         */
        FormUtils.prototype.mapUserOrderBy = function (orderByAttributes) {
            var i = 0;
            $('.order-by-attributes li').each(function () {
                $(this).find('.order-by-selection option').filter(function () {
                    return ($(this).val() == (orderByAttributes[i].getValue()));
                }).prop('selected', true);
                $(this).find('.order-selection option').filter(function () {
                    return ($(this).val() == (orderByAttributes[i].getOrder().toLowerCase()));
                }).prop('selected', true);
                i++;
            });
        };

        /**
         * @function to map the saved option values to the option object
         * @param {Object} predefinedOptions Predefined options of a particular annotation type
         * @param {Object} savedOptions Saved options
         * @return {Object} options
         */
        FormUtils.prototype.mapUserOptionValues = function (predefinedOptions, savedOptions) {
            var options = [];
            _.forEach(predefinedOptions, function (predefinedOption) {
                var foundPredefinedOption = false;
                _.forEach(savedOptions, function (savedOption) {
                    var optionName = savedOption.split('=')[0].trim();
                    var optionValue = savedOption.split('=')[1].trim();
                    optionValue = optionValue.substring(1, optionValue.length - 1);
                    if (optionName.toLowerCase() == predefinedOption.name.toLowerCase()) {
                        foundPredefinedOption = true;
                        options.push({
                            name: predefinedOption.name, value: optionValue, description: predefinedOption
                                .description, optional: predefinedOption.optional,
                            defaultValue: predefinedOption.defaultValue
                        });
                        return false;
                    }
                });
                if (!foundPredefinedOption) {
                    options.push({
                        name: predefinedOption.name, value: "", description: predefinedOption
                            .description, optional: predefinedOption.optional, defaultValue: predefinedOption.defaultValue
                    });
                }
            });
            return options;
        };

        /**
         * @function to map the values of the predefined annotations
         */
        FormUtils.prototype.mapPredefinedAnnotations = function (savedAnnotations, predefinedAnnotations) {
            var self = this;
            _.forEach(savedAnnotations, function (savedAnnotation) {
                _.forEach(predefinedAnnotations, function (predefinedAnnotation) {
                    if (savedAnnotation.name.toLowerCase() === predefinedAnnotation.name.toLowerCase()) {
                        predefinedAnnotation.isChecked = true;
                        if (savedAnnotation.elements) {
                            self.mapPredefinedAnnotationElements(savedAnnotation, predefinedAnnotation);
                        }
                        if (savedAnnotation.annotations) {
                            self.mapPredefinedAnnotations(savedAnnotation.annotations, predefinedAnnotation.annotations);
                        }
                        return false;
                    }
                })
            });
        };

        /**
         * @function to map the user given values for the predefined options
         */
        FormUtils.prototype.mapPredefinedAnnotationElements = function (savedAnnotation, predefinedAnnotation) {
            var self = this;
            if (predefinedAnnotation.parameters) {
                if (predefinedAnnotation.name.toLowerCase() === Constants.RETENTION_PERIOD) {
                    self.mapTimeBasedAnnotationElements(savedAnnotation, predefinedAnnotation);
                } else {
                    _.forEach(savedAnnotation.elements, function (savedElement) {
                        _.forEach(predefinedAnnotation.parameters, function (predefinedElement) {
                            if (savedElement.key.toLowerCase() === predefinedElement.name.toLowerCase()) {
                                predefinedElement.value = savedElement.value;
                            }
                        });
                    });
                }
            }
        };

        /**
         * @function to map time based annotation elements. A seperate method is used as an array
         * is required to determine the option name as there are more than one option name referring
         * to the same option. ex: sec,second,seconds
         */
        FormUtils.prototype.mapTimeBasedAnnotationElements = function (savedAnnotation, predefinedAnnotation) {
            _.forEach(savedAnnotation.elements, function (savedElement) {
                _.forEach(predefinedAnnotation.parameters, function (predefinedElement) {
                    if (predefinedElement.possibleNames.includes(savedElement.key.toLowerCase())) {
                        predefinedElement.value = savedElement.value;
                    }
                });
            });
        };

        /**
         * @function to create the annotation object with additional attributes[ isChecked and values[] ]
         * @param predefinedAnnotations primary/index annotations
         * @returns {Array}
         */
        FormUtils.prototype.createObjectsForAnnotationsWithoutKeys = function (predefinedAnnotations) {
            var annotationsWithoutKeys = [];
            _.forEach(predefinedAnnotations, function (predefinedAnnotation) {
                if (predefinedAnnotation.name.toLowerCase() == Constants.PRIMARY_KEY ||
                    predefinedAnnotation.name.toLowerCase() == Constants.INDEX) {
                    var annotationObject = {
                        name: predefinedAnnotation.name, values: [{ value: "" }], isChecked: false
                    }
                    annotationsWithoutKeys.push(annotationObject);
                }
            });
            return annotationsWithoutKeys;
        };

        /**
         * @function to create the annotation object with additional attributes[ isChecked and values[] ]
         * @param predefinedAnnotations
         * @returns {Array}
         */
        FormUtils.prototype.createObjectsForAnnotationsWithKeys = function (predefinedAnnotations) {
            var self = this;
            var annotationsWithKeys = [];
            var subAnnotations;
            _.forEach(predefinedAnnotations, function (predefinedAnnotation) {
                if (predefinedAnnotation.name.toLowerCase() != Constants.PRIMARY_KEY &&
                    predefinedAnnotation.name.toLowerCase() != Constants.INDEX) {
                    var parameters;
                    if (predefinedAnnotation.annotations) {
                        subAnnotations = self.createObjectsForAnnotationsWithKeys(predefinedAnnotation.annotations);
                    }
                    if (predefinedAnnotation.name.toLowerCase() === Constants.RETENTION_PERIOD) {
                        parameters = self.createParametersWithPossibleNames(predefinedAnnotation.parameters);
                    } else {
                        parameters = self.createObjectWithValues(predefinedAnnotation.parameters);
                    }
                    var annotationObject = {
                        name: predefinedAnnotation.name, parameters: parameters,
                        annotations: subAnnotations,
                        optional: predefinedAnnotation.optional, isChecked: false
                    }
                    annotationsWithKeys.push(annotationObject);
                }
            });
            return annotationsWithKeys;
        };

        /**
         * @function to creat an additional attribute[possibleNames] for parameters of retentionPeriod
         * @param parameters parameters of retentionPeriod
         * @returns {Array}
         */
        FormUtils.prototype.createParametersWithPossibleNames = function (parameters) {
            var parametersWithPossibleNames = [];
            var possibleNames;
            _.forEach(parameters, function (parameter) {
                if (parameter.name == "sec") {
                    possibleNames = ["sec", "second", "seconds"];
                } else if (parameter.name == "min") {
                    possibleNames = ["min", "minute", "minutes"];
                } else if (parameter.name == "hours") {
                    possibleNames = ["hour", "hours"];
                } else if (parameter.name == "days") {
                    possibleNames = ["day", "days"];
                } else if (parameter.name == "months") {
                    possibleNames = ["month", "months"];
                } else if (parameter.name == "years") {
                    possibleNames = ["year", "years"];
                }
                parametersWithPossibleNames.push({
                    name: parameter.name, value: "", description: parameter.description, optional: parameter.optional,
                    defaultValue: parameter.defaultValue, possibleNames: possibleNames, type: parameter.type
                });
            });
            return parametersWithPossibleNames;
        };

        /**
         * @function checks if an annotation is predefined using the annotation name
         * @param {Object} predefinedAnnotationList list of predefined annotations
         * @param {String} annotationName the name which needs to be checked
         * @return {Object} predefinedObject
         */
        FormUtils.prototype.isPredefinedAnnotation = function (predefinedAnnotationList, annotationName) {
            var predefinedObject = undefined;
            _.forEach(predefinedAnnotationList, function (predefinedAnnotation) {
                if (predefinedAnnotation.name.toLowerCase() == annotationName.toLowerCase()) {
                    predefinedObject = predefinedAnnotation;
                    return false;
                }
            });
            return predefinedObject;
        };

        /**
         * @function validate the annotations
         * @param {Object} predefinedAnnotationList List of predefined annotations
         * @param {Object} annotationNodes array to add the nodes which needs to be built
         * @return {boolean} isErrorOccurred
         */
        FormUtils.prototype.validateAnnotations = function (predefinedAnnotationList, annotationNodes) {
            var self = this;
            //gets all the parent nodes
            var jsTreeAnnotationList = $('#annotation-div').jstree(true)._model.data['#'].children;
            var isErrorOccurred = false;
            _.forEach(jsTreeAnnotationList, function (jsTreeAnnotation) {
                var node_info = $('#annotation-div').jstree("get_node", jsTreeAnnotation);
                var predefinedObject = self.isPredefinedAnnotation(predefinedAnnotationList, node_info.text.trim())
                if (predefinedObject) {
                    if ((predefinedObject.isMandatory) || (!predefinedObject.isMandatory && node_info.state.checked)) {
                        if (self.validatePredefinedAnnotation(node_info, predefinedObject)) {
                            isErrorOccurred = true;
                            return false;
                        } else {
                            annotationNodes.push(jsTreeAnnotation)
                        }
                    }
                } else {
                    annotationNodes.push(jsTreeAnnotation)
                }
            });
            return isErrorOccurred;
        };

        /**
         * @function to validate the primary and index annotations
         * @return {boolean} isErrorOccurred
         */
        FormUtils.prototype.validatePrimaryIndexAnnotations = function () {
            var isErrorOccurred = false;
            $('#primary-index-annotations .annotation').each(function () {
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
         * @function validates elements of jstree predefined Annotations
         * @param {Object} node_info jstree node info
         * @param {Object} predefinedAnnotationObject predefined annotation
         * @return {boolean} isErrorOccurred
         */
        FormUtils.prototype.validatePredefinedAnnotation = function (node_info, predefinedAnnotationObject) {
            var self = this;
            var isErrorOccurred = false;
            var childrenOFPredefinedAnnotationNode = node_info.children;
            _.forEach(childrenOFPredefinedAnnotationNode, function (jsTreePredefinedAnnotationElement) {
                var annotation_key_info = $('#annotation-div').jstree("get_node",
                    jsTreePredefinedAnnotationElement);
                var annotation_value_info = $('#annotation-div').jstree("get_node", annotation_key_info
                    .children[0])
                //validate for checked(optional)properties which has empty values
                if (annotation_key_info.state.checked && annotation_value_info.text.trim() == "") {
                    DesignViewUtils.prototype.errorAlert("Property '" + annotation_key_info.text.trim() +
                        "' is empty");
                    isErrorOccurred = true;
                    return false;
                }
                if (self.validateMandatoryElementsOfPredefinedObjects(annotation_key_info,
                    annotation_value_info, predefinedAnnotationObject)) {
                    isErrorOccurred = true;
                    return false;
                }
            });
            return isErrorOccurred;
        };

        /**
         * @function validate the elements of the annotation
         * @param {Object} annotationKey jstree annotation key
         * @param {Object} annotationValue jstree annotation key
         * @param {Object} predefinedAnnotationObject predefined annotation object
         * @return {Boolean} isErrorOccurred
         */
        FormUtils.prototype.validateMandatoryElementsOfPredefinedObjects = function (annotationKey, annotationValue,
            predefinedAnnotationObject) {
            var isErrorOccurred = false;
            _.forEach(predefinedAnnotationObject.elements, function (predefinedObjectElement) {
                if (annotationKey.text.trim().toLowerCase() == predefinedObjectElement.name
                    .toLowerCase()) {
                    if (predefinedObjectElement.isMandatory) {
                        if (annotationValue.text.trim() == "") {
                            DesignViewUtils.prototype.errorAlert("Property '" + predefinedObjectElement
                                .name + "' is mandatory");
                            isErrorOccurred = true;
                            return false;
                        }
                    }
                }
            });
            return isErrorOccurred
        };

        /**
         * @function makes the predefined annotation's checkbox checked
         * @param {Object} checkedBoxes array of saved annotation names
         */
        FormUtils.prototype.checkPredefinedAnnotations = function (checkedBoxes) {
            var jsTreeNodes = $('#annotation-div').jstree(true).get_json('#', { 'flat': true });
            _.forEach(checkedBoxes, function (checkedBoxName) {
                _.forEach(jsTreeNodes, function (node) {
                    if (node.text.trim().toLowerCase() == checkedBoxName.toLowerCase()) {
                        $("#annotation-div").jstree(true).check_node(node.id)
                        return false;
                    }
                })
            });
        };

        /**
         * @function to check the radio button of the selected rdbms type
         * @param {Object} rdbmsOptions all the options of rdbms with the user given values
         */
        FormUtils.prototype.checkRdbmsType = function (rdbmsOptions) {
            var isFound = false;
            _.forEach(rdbmsOptions, function (option) {
                if (option.name.toLowerCase() == "datasource" && option.value != "") {
                    $("#define-rdbms-type input[name=radioOpt][value='datasource']").prop("checked", true);
                    isFound = true;
                    return false;
                } else if (option.name.toLowerCase() == "jndi.resource" && option.value != "") {
                    $("#define-rdbms-type input[name=radioOpt][value='jndi']").prop("checked", true);
                    isFound = true;
                    return false;
                }
            })
            if (!isFound) {
                $("#define-rdbms-type input[name=radioOpt][value='inline-config']").prop("checked", true);
            }
        };

        /**
         * @function to check if the selected store type is rdbms
         * @param {String} selectedRdbmsType
         * @param {Object} storeOptions
         * @param {Object} customizedStoreOptions
         */
        FormUtils.prototype.populateStoreOptions = function (selectedRdbmsType, storeOptions, customizedStoreOptions) {
            var self = this;
            self.renderRdbmsTypes();
            if (selectedRdbmsType == Constants.RDBMS_STORE_TYPE) {
                $('#define-rdbms-type').show();
                self.checkRdbmsType(storeOptions);
                var dataStoreOptions = self.getRdbmsOptions(storeOptions);
                self.renderOptions(dataStoreOptions, customizedStoreOptions, Constants.STORE);
            } else {
                $('#define-rdbms-type').hide();
                self.renderOptions(storeOptions, customizedStoreOptions, Constants.STORE);
            }
        };

        /**
         * @function to build the annotations
         * @param {Object} annotationNodes array of nodes which needs to be constructed
         * @param {Object} annotationStringList array to add the built annotation strings
         * @param {Object} annotationObjectList array to add the created annotation objects
         */
        var annotation = "";
        FormUtils.prototype.buildAnnotation = function (annotationNodes, annotationStringList, annotationObjectList) {
            var self = this;
            _.forEach(annotationNodes, function (node) {
                var node_info = $('#annotation-div').jstree("get_node", node);
                var childArray = node_info.children
                if (childArray.length != 0) {
                    annotation += "@" + node_info.text.trim() + "( "
                    //create annotation object
                    var annotationObject = new AnnotationObject();
                    annotationObject.setName(node_info.text.trim())
                    self.traverseChildAnnotations(childArray, annotationObject)
                    annotation = annotation.substring(0, annotation.length - 1);
                    annotation += ")"
                    annotationObjectList.push(annotationObject)
                    annotationStringList.push(annotation);
                    annotation = "";
                }
            });
        };

        /**
         * @function to traverse the children of the parent annotaions
         * @param {Object} children the children of a parent annotation node
         * @param {Object} annotationObject the parent's annotation object
         */
        FormUtils.prototype.traverseChildAnnotations = function (children, annotationObject) {
            var self = this;
            children.forEach(function (node) {
                var node_info = $('#annotation-div').jstree("get_node", node);
                //if the child is a sub annotation
                if (self.isChildSubAnnotation(node_info)) {
                    if (node_info.children.length != 0) {
                        annotation += "@" + node_info.text.trim() + "( "
                        var childAnnotation = new AnnotationObject();
                        childAnnotation.setName(node_info.text.trim())
                        self.traverseChildAnnotations(node_info.children, childAnnotation)
                        annotationObject.addAnnotation(childAnnotation)
                        annotation = annotation.substring(0, annotation.length - 1);
                        annotation += "),"
                    }
                } else {
                    self.addAnnotationElement(node_info, annotationObject);
                }
            });
        };

        /**
         * @function to add the annotation's element
         * @param {Object} node_info jstree node information
         * @param {AnnotationObject} annotationObject
         */
        FormUtils.prototype.addAnnotationElement = function (node_info, annotationObject) {
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
        };

        /**
         * @function to determine if annotation's child is a sub-annotation
         * @param {Object} node_info jstree node information
         * @return {Boolean}
         */
        FormUtils.prototype.isChildSubAnnotation = function (node_info) {
            if ((node_info.original != undefined && node_info.original.class == "annotation") ||
                (node_info.li_attr != undefined && (node_info.li_attr.class == "annotation" ||
                    node_info.li_attr.class == "optional-annotation" || node_info.li_attr.class ==
                    "mandatory-annotation"))) {
                return true;
            } else {
                return false;
            }
        };

        /**
         * @function to build the group-by attributes
         */
        FormUtils.prototype.buildGroupBy = function () {
            var attributes = [];
            $('.group-by-attributes li').each(function () {
                var selectedValue = $(this).find('select').val();
                if (selectedValue) {
                    attributes.push(selectedValue);
                }
            });
            return attributes;
        };

        /**
		 * @function to build the join-query order by section
		 */
        FormUtils.prototype.buildOrderBy = function () {
            var orderByAttributes = [];
            $('.order-by-attributes li').each(function () {
                var orderValue = $(this).find('.order-by-selection').val();
                if (orderValue) {
                    var orderByValueObjectOptions = {};
                    _.set(orderByValueObjectOptions, 'value', orderValue);
                    _.set(orderByValueObjectOptions, 'order', $(this).find('.order-selection').val());
                    orderByAttributes.push(orderByValueObjectOptions);
                }
            });
            return orderByAttributes;
        };

        /**
         * @function to initialize the jstree for annotations
         */
        FormUtils.prototype.loadAnnotation = function () {
            var self = this;
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
            self.addEventListenersForJstree(tree);
        };

        /**
         * @function to add event listeners for jstree annotations
         */
        FormUtils.prototype.addEventListenersForJstree = function (tree) {
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
         * @function to add event listeners for the group by div
         */
        FormUtils.prototype.addEventListenersForGroupByDiv = function (possibleAttributes) {
            var self = this;
            $('.define-group-by-attributes').on('change', '.group-by-checkbox', function () {
                if ($(this).is(':checked')) {
                    $('.group-by-content').show();
                } else {
                    $('.group-by-content').hide();
                }
            });

            $('.btn-add-group-by-attribute').on('click', function () {
                self.appendGroupBy(possibleAttributes);
                self.preventMultipleSelection(Constants.GROUP_BY);
                self.checkForAttributeLength(possibleAttributes.length, Constants.GROUP_BY);
            });

            $('.define-group-by-attributes').on('change', '.group-by-selection', function () {
                self.preventMultipleSelection(Constants.GROUP_BY);
                self.removeErrorClass();
            });

            $('.define-group-by-attributes').on('click', '.btn-del-option', function () {
                $(this).closest('li').remove();
                self.preventMultipleSelection(Constants.GROUP_BY);
                self.checkForAttributeLength(possibleAttributes.length, Constants.GROUP_BY);
            });
        };

        /**
         * @function to add event listeners for parameter division
         */
        FormUtils.prototype.addEventListenersForParameterDiv = function () {
            //event listener to show parameter description
            $('.defineFunctionParameters').on('mouseover', '.parameter-desc', function () {
                $(this).find('.parameter-desc-content').show();
            });

            //event listener to hide parameter description
            $('.defineFunctionParameters').on('mouseout', '.parameter-desc', function () {
                $(this).find('.parameter-desc-content').hide();
            });

            //event listener when the parameter checkbox is changed
            $('.defineFunctionParameters').on('change', '.parameter-checkbox', function () {
                var parameterParent = $(this).parents(".parameter");
                if ($(this).is(':checked')) {
                    parameterParent.find(".optional-param-content").show();
                } else {
                    parameterParent.find(".optional-param-content").hide();
                    parameterParent.find(".parameter-value").removeClass("required-input-field");
                    parameterParent.find(".error-message").text("");
                }
            });
        };

        /**
         * @function to add event listeners for sort window type parameters
         */
        FormUtils.prototype.addEventListenerForSortWindow = function (selectedType) {
            var self = this;
            $('.defineFunctionParameters').on('change', '.parameter-checkbox', function () {
                //check for sort type's parameter (order & attribute params)
                if (selectedType === Constants.SORT) {
                    self.showHideOrderForSort();
                }
            });
        };

        /**
         * @function to remove the error class once the user fills in the required input
         */
        FormUtils.prototype.addEventListenerToRemoveRequiredClass = function () {
            $('.design-view-form-content').on('input', '.error-input-field', function () {
                var input = $(this).val().trim();
                if (input != "") {
                    $(this).removeClass('required-input-field');
                    $(this).closest('.clearfix').next('label.error-message').hide();
                } else {
                    $(this).addClass('required-input-field');
                    $(this).closest('.clearfix').next('label.error-message').show();
                }
            });
        };

        /**
         * @function to add event listeners for the attribute selection(projection) division
         */
        FormUtils.prototype.addEventListenersForSelectionDiv = function () {
            $('.define-select').on('change', '.attribute-selection-type', function () {
                if ($(this).val() === Constants.TYPE_ALL) {
                    $('.define-user-defined-attributes').hide();
                } else {
                    $('.define-user-defined-attributes').show();
                }
            });

            $('.define-select').on('click', '.btn-del-option', function () {
                $(this).closest('li').remove();
            });
        };

        /**
         * @function to add event listeners for order by division
         * @param {Object} possibleAttributes possible order by attributes
         */
        FormUtils.prototype.addEventListenersForOrderByDiv = function (possibleAttributes) {
            var self = this;
            $('.define-order-by-attributes').on('change', '.order-by-checkbox', function () {
                if ($(this).is(':checked')) {
                    $('.order-by-content').show();
                } else {
                    $('.order-by-content').hide();
                }
            });

            $('.define-order-by-attributes').on('change', '.order-by-selection', function () {
                self.preventMultipleSelection(Constants.ORDER_BY);
                self.removeErrorClass();
            });

            $('.define-order-by-attributes').on('click', '.btn-del-option', function () {
                $(this).closest('li').remove();
                self.preventMultipleSelection(Constants.ORDER_BY);
                self.checkForAttributeLength(possibleAttributes.length, Constants.ORDER_BY);
            });

            $('.btn-add-order-by-attribute').on('click', function () {
                self.appendOrderBy(possibleAttributes);
                self.preventMultipleSelection(Constants.ORDER_BY);
                self.checkForAttributeLength(possibleAttributes.length, Constants.ORDER_BY);
            });
        };

        /**
         * @function to add event listeners for stream handler section
         * @param {Object} streamHandlerList list of stream handlers
         */
        FormUtils.prototype.addEventListenersForStreamHandlersDiv = function (streamHandlerList) {
            var self = this;

            //on change of stream handler checkbox
            $('.define-stream-handler').on('change', '.stream-handler-checkbox', function () {
                var sourceDiv = self.getSourceDiv($(this));
                if ($(this).is(':checked')) {
                    $(sourceDiv).find('.define-stream-handler-section').show();
                    $(sourceDiv).find('.define-stream-handler-buttons').show();
                } else {
                    $(sourceDiv).find('.define-stream-handler-section').hide();
                    $(sourceDiv).find('.define-stream-handler-buttons').hide();
                }
            });

            //on change of window type
            var predefinedWindowFunctions = _.orderBy(JSON.parse(JSON.stringify
                (self.configurationData.rawExtensions["windowFunctionNames"]), ['name'], ['asc']));
            $('.define-stream-handler').on('change', '#window-type', function () {
                var sourceDiv = self.getSourceDiv($(this))
                if ($(sourceDiv).hasClass('define-left-source')) {
                    var source = Constants.LEFT
                } else {
                    var source = Constants.RIGHT
                }
                var windowType;
                var savedParameterValues;
                var selectedType = this.value.toLowerCase();
                var streamHandlerWindow = self.getStreamHandler(streamHandlerList, Constants.WINDOW, selectedType);
                if (streamHandlerWindow) {
                    windowType = streamHandlerWindow.value.function.toLowerCase();
                    savedParameterValues = streamHandlerWindow.value.parameters;
                }
                var functionParameters = self.getSelectedTypeParameters(selectedType, predefinedWindowFunctions);
                var parameterDiv = $(this).closest('.defineFunctionName').parents('.define-stream-handler-type-content');
                if (savedParameterValues && selectedType == windowType) {
                    self.callToMapParameters(selectedType, functionParameters, savedParameterValues, parameterDiv)
                } else {
                    var functionParametersWithValues = self.createObjectWithValues(functionParameters);
                    self.renderParameters(functionParametersWithValues, Constants.WINDOW, parameterDiv);
                }
                if (selectedType === Constants.SORT) {
                    self.showHideOrderForSort();
                    self.addEventListenerForSortWindow(selectedType)
                }
            });

            //on change of stream-function type
            var predefinedStreamFunctions = _.orderBy(JSON.parse(JSON.stringify
                (self.configurationData.rawExtensions["streamFunctions"]), ['name'], ['asc']));
            $(".define-stream-handler").on("autocompleteselect", '.custom-combobox-input', function (event, ui) {
                var selectedType = ui.item.value.toLowerCase();
                var functionType;
                var savedParameterValues;
                var streamHandlerFunction = self.getStreamHandler(streamHandlerList, Constants.FUNCTION, selectedType);
                if (streamHandlerFunction) {
                    functionType = streamHandlerFunction.value.function.toLowerCase();
                    savedParameterValues = streamHandlerFunction.value.parameters;
                }
                var functionParameters = self.getSelectedTypeParameters(selectedType, predefinedStreamFunctions);
                var parameterDiv = $(this).closest('.defineFunctionName').parents('.define-stream-handler-type-content');
                if (savedParameterValues && selectedType == functionType) {
                    self.callToMapParameters(selectedType, functionParameters, savedParameterValues, parameterDiv)
                } else {
                    functionParametersWithValues = self.createObjectWithValues(functionParameters);
                }
                self.renderParameters(functionParametersWithValues, Constants.STREAM_FUNCTION, parameterDiv);
            });

            //to add window
            $('.define-stream-handler').on('click', ".btn-add-window", function () {
                var sourceDiv = self.getSourceDiv($(this));
                self.addNewStreamHandler(sourceDiv, Constants.WINDOW);
            });

            //to add stream-function
            $('.define-stream-handler').on('click', ".btn-add-function", function () {
                var sourceDiv = self.getSourceDiv($(this));
                self.addNewStreamHandler(sourceDiv, Constants.FUNCTION);
                self.showDropDown();
            });

            //on change of stream-handler type
            var previousValue;
            var previousContent;
            $('.define-stream-handler').on('focus', '.stream-handler-selection', function () {
                previousValue = this.value;
                previousContent = $(this).closest('.define-stream-handler-type').next().
                    find('.define-stream-handler-type-content').contents();

            }).on('change', '.stream-handler-selection', function () {
                var currentValue = this.value; // New Value
                var currentContentDiv = $(this).closest('.define-stream-handler-type').next().
                    find('.define-stream-handler-type-content');
                if (currentValue == previousValue) {
                    currentContentDiv.html(previousContent)
                } else {
                    var sourceDiv = self.getSourceDiv($(this));
                    var streamHandlerContent = $(this).closest('.define-stream-handler-type').next().
                        find('.define-stream-handler-type-content');
                    self.renderStreamHandlerContentDiv(currentValue, streamHandlerContent)
                    self.mapStreamHandlerContent(streamHandlerContent, self.createEmptyStreamHandler(currentValue));
                }
                self.preventMultipleSelectionOfWindowStreamHandler(sourceDiv);
            });

            //To delete stream-handler
            $(".define-stream-handler").on('click', '.btn-del-attr', function () {
                var sourceDiv = self.getSourceDiv($(this));
                $(this).closest('li').remove();
                self.preventMultipleSelectionOfWindowStreamHandler(sourceDiv);
                self.changeAttributeNavigation($(sourceDiv).find('.stream-handler-list'));
                self.removeNavigationForWindow(sourceDiv)
                self.showHideStreamHandlerWindowButton(sourceDiv)
            });

            //To reorder up the stream-handler
            $(".define-stream-handler").on('click', '.reorder-up', function () {
                var sourceDiv = self.getSourceDiv($(this));
                var $current = $(this).closest('li');
                var $previous = $current.prev('li');
                if ($previous.length !== 0) {
                    $current.insertBefore($previous);
                }
                self.changeAttributeNavigation($(sourceDiv).find('.stream-handler-list'));
                self.removeNavigationForWindow(sourceDiv)
            });

            //To reorder down the stream-handler
            $(".define-stream-handler").on('click', '.reorder-down', function () {
                var sourceDiv = self.getSourceDiv($(this));
                var $current = $(this).closest('li');
                var $next = $current.next('li');
                if ($next.length !== 0) {
                    $current.insertAfter($next);
                }
                self.changeAttributeNavigation($(sourceDiv).find('.stream-handler-list'));
                self.removeNavigationForWindow(sourceDiv)
            });
        };

        /**
         * @function to return the div of where the stream handler is embedded
         * of join query form.
         */
        FormUtils.prototype.getSourceDiv = function (clickedSection) {
            var sourceDiv = clickedSection.closest('.define-source');
            return sourceDiv;
        };

        /**
         * @function to get id of the div so that each stream handler content will be added an id for the div to
         * collapse in and out. This is required because in join query there are two stream handler sections [left &
         * right source]
         */
        FormUtils.prototype.getIdOfDiv = function (div) {
            if (div.hasClass('define-left-source')) {
                return Constants.LEFT;
            } else if (div.hasClass('define-right-source')) {
                return Constants.RIGHT;
            } else {
                return Constants.QUERY;
            }
        };

        /**
         * @function to add a new stream handler
         * @param {String} div where the stream handler section is present
         * @param {String} type type of the stream handler
         */
        FormUtils.prototype.addNewStreamHandler = function (sourceDiv, type) {
            var self = this;
            var streamHandlerTypes = self.configurationData.application.config.stream_handler_types;
            var id = self.getIdOfDiv(sourceDiv);
            var streamHandlerList = $(sourceDiv).find('.stream-handler-list');
            var streamHandlerListLength = $(streamHandlerList).find('li').length
            var appendedIndex;
            var handlerList = '<li class="define-stream-handler-content"> <div> ' +
                '<div class="collapse-div" href="#' + streamHandlerListLength + '-' + id + '-stream-handler-content" ' +
                'data-toggle="collapse" aria-expanded="true"> <label class="clearfix"> ' +
                '<span class="mandatory-symbol"> * </span> Type <a class="collapse-icon"> </a> </label> ' +
                '<div class = "define-stream-handler-type"> </div> </div> <div id="' +
                streamHandlerListLength + '-' + id + '-stream-handler-content" class="collapse in"> <div class="clearfix">' +
                '<div class = "define-stream-handler-type-content"> </div> <div class = "attr-nav"> </div> ' +
                '</div> <label class = "error-message"> </label> </div> </div> </li>';
            if (type === Constants.WINDOW || streamHandlerListLength == 0 ||
                $(sourceDiv).find('.define-window-stream-handler').length == 0) {
                $(streamHandlerList).append(handlerList);
                appendedIndex = streamHandlerListLength;
            } else {
                var lastIndex = streamHandlerListLength - 1;
                $(streamHandlerList).find('li:eq(' + lastIndex + ')').before(handlerList);
                appendedIndex = lastIndex;
            }
            var streamHandlerType = $(sourceDiv).find('.define-stream-handler-type:eq(' + appendedIndex + ')');
            var streamHandlerContent = $(sourceDiv).find('.define-stream-handler-type-content:eq(' + appendedIndex + ')');
            streamHandlerContent.addClass('define-' + type + '-stream-handler')
            self.renderDropDown(streamHandlerType, streamHandlerTypes, Constants.STREAM_HANDLER);
            self.renderStreamHandlerContentDiv(type, streamHandlerContent)
            self.selectHandlerSelection($(sourceDiv).find('.stream-handler-selection:eq(' + appendedIndex + ')'), type)
            self.changeAttributeNavigation($(sourceDiv).find('.stream-handler-list'));
            self.mapStreamHandlerContent(streamHandlerContent, self.createEmptyStreamHandler(type))
            self.preventMultipleSelectionOfWindowStreamHandler(sourceDiv);
            self.mapStreamHandlerContent(streamHandlerContent, self.createEmptyStreamHandler(type))
            self.removeNavigationForWindow(sourceDiv)
            self.showHideStreamHandlerWindowButton(sourceDiv)
            streamHandlerType[0].scrollIntoView();
        };

        /**
         * @function to add event listeners for condition div
         */
        FormUtils.prototype.addEventListenersForConditionDiv = function (inputStreamNames) {
            var self = this;

            $('.define-conditions').on('input', '.condition-id', function () {
                var conditionIndex = $(this).closest('.condition-content').index();
                $('.define-conditions .condition-navigation:eq(' + conditionIndex + ') a:eq(0)').html($(this).val());
            });
        };

        /**
         * @function to remove the up and down navigation for swindow stream handler
         */
        FormUtils.prototype.removeNavigationForWindow = function (sourceDiv) {
            var streamHandlerListLength = $(sourceDiv).find('.stream-handler-list li').length
            var lastIndex = streamHandlerListLength - 1;
            var lastList = $(sourceDiv).find(' .stream-handler-list li:eq(' + lastIndex + ')');
            if (lastList.find('.define-stream-handler-type-content').hasClass('define-window-stream-handler')) {
                lastList.find('.attr-nav a:eq(0)').remove();
                if (streamHandlerListLength == 2) {
                    lastList.prev('li').find('.attr-nav a:eq(0)').remove();
                } else {
                    lastList.prev('li').find('.attr-nav a:eq(1)').remove();
                }
            }
        };

        /**
         * @function to get the saved stream handler[window/stream function] so that the saved parameters
         * can be pre-populated if the user changes to a different function type and changes it back to
         * the saved function type - assuming that the user has saved different function types in each case
         */
        FormUtils.prototype.getStreamHandler = function (streamHandlerList, streamHandlerType, selectedType) {
            var streamHandler = undefined;
            if (streamHandlerList && streamHandlerList.length != 0) {
                _.forEach(streamHandlerList, function (handler) {
                    if (handler.type.toLowerCase() === streamHandlerType &&
                        handler.value.function.toLowerCase() === selectedType) {
                        streamHandler = handler;
                        return false;
                    }
                });
            }
            return streamHandler;
        };

        /**
         * @function to select the stream handler type
         */
        FormUtils.prototype.selectHandlerSelection = function (classDiv, streamHandlerType) {
            classDiv.find('option').filter(function () {
                return ($(this).val() == streamHandlerType);
            }).prop('selected', true);
        };

        /**
         * @function to create an empty stream handler object
         */
        FormUtils.prototype.createEmptyStreamHandler = function (type) {
            var streamHandlerObject;
            if (type == Constants.FILTER) {
                streamHandlerObject = {
                    type: Constants.FILTER,
                    value: ""
                }
            } else if (type == Constants.WINDOW) {
                streamHandlerObject = {
                    type: Constants.WINDOW,
                    value: {
                        function: "delay", //as default
                        parameters: []
                    }
                }
            } else if (type == Constants.FUNCTION) {
                streamHandlerObject = {
                    type: Constants.FUNCTION,
                    value: {
                        function: "approximate:count", //as default
                        parameters: []
                    }
                }
            }
            return streamHandlerObject;
        };

        /**
         * @function to add event listeners for attribute section
         */
        FormUtils.prototype.addEventListenersForAttributeDiv = function () {
            var self = this;
            //To add attribute
            $("#define-attribute").on('click', '#btn-add-attribute', function () {
                $("#attribute-div").append(self.addAttribute());
                self.changeAttributeNavigation('#attribute-div');
            });

            //To delete attribute
            $("#define-attribute").on('click', '#attribute-div .btn-del-attr', function () {
                $(this).closest('li').remove();
                self.changeAttributeNavigation('#attribute-div');
            });

            //To reorder up the attribute
            $("#define-attribute").on('click', ' #attribute-div .reorder-up', function () {
                var $current = $(this).closest('li');
                var $previous = $current.prev('li');
                if ($previous.length !== 0) {
                    $current.insertBefore($previous);
                }
                self.changeAttributeNavigation('#attribute-div');

            });

            //To reorder down the attribute
            $("#define-attribute").on('click', ' #attribute-div .reorder-down', function () {
                var $current = $(this).closest('li');
                var $next = $current.next('li');
                if ($next.length !== 0) {
                    $current.insertAfter($next);
                }
                self.changeAttributeNavigation('#attribute-div');
            });
        };

        /**
         * @function to add event listeners of the annotation options
         */
        FormUtils.prototype.addEventListenersForGenericOptionsDiv = function (id) {
            var self = this;
            //To show option description
            $('#' + id + '-options-div').on('mouseover', '.option-desc', function () {
                $(this).find('.option-desc-content').show();
            });

            //To hide option description
            $('#' + id + '-options-div').on('mouseout', '.option-desc', function () {
                $(this).find('.option-desc-content').hide();
            });

            //To hide and show the option content of the optional options
            $('#' + id + '-options-div').on('change', '.option-checkbox', function () {
                var optionParent = $(this).parents(".option");
                if ($(this).is(':checked')) {
                    optionParent.find(".option-value").show();
                } else {
                    optionParent.find(".option-value").hide();
                    optionParent.find(".option-value").removeClass("required-input-field");
                    optionParent.find(".error-message").text("");
                }
            });

            //To add customized option
            $('#' + id + '-options-div').on('click', '#btn-add-' + id + '-options', function () {
                var custOptDiv = '<li class="option">' +
                    '<div class = "clearfix"> <label>option.key</label> <input type="text" class="cust-option-key"' +
                    'value=""> </div> <label class = "error-message"></label> ' +
                    '<div class="clearfix"> <label>option.value</label> ' +
                    '<input type="text" class="cust-option-value" value="">' +
                    '<a class = "btn-del btn-del-option"><i class="fw fw-delete"></i></a></div>' +
                    '<label class = "error-message"></label></li>';
                $('#customized-' + id + '-options .cust-options').append(custOptDiv);
                self.changeCustomizedOptDiv(id);
            });

            //To delete customized option
            $('#' + id + '-options-div').on('click', '.btn-del-option', function () {
                $(this).closest('li').remove();
                self.changeCustomizedOptDiv(id);
            });
        };

        /**
         * @function to add event listeners for the primary index annotation div
         */
        FormUtils.prototype.addEventListenerForPrimaryIndexAnnotationDiv = function () {

            //To add annotation value
            $('#primary-index-annotations').on('click', '.btn-add-annot-value', function () {
                $(this).parents(".annotation").find("ul").append
                    ('<li class = "clearfix primary-index-annotation-value"> <div class="clearfix"> ' +
                        '<input type = "text" value = "" class = "annotation-value"/> ' +
                        '<a class = "btn-del-annot-value"> <i class = "fw fw-delete"> </i> </a> </div> ' +
                        '<label class="error-message"></label> </li>');
            });

            //To delete annotation value
            $('#primary-index-annotations').on('click', '.btn-del-annot-value', function () {
                $(this).closest('li').remove();
            });

            // To show the values of the primaryKey and index annotations on change of the checkbox
            $('#primary-index-annotations').on('change', '.annotation-checkbox', function () {
                var parent = $(this).parents(".annotation");
                if ($(this).is(':checked')) {
                    parent.find('.annotation-content').show();
                } else {
                    parent.find('.annotation-content').hide();
                    parent.find('.error-message').text("");
                    parent.find('.annotation-value').removeClass('required-input-field')
                }
            });
        };

        /**
         * @function to add event listeners for the predefined annotations
         */
        FormUtils.prototype.addEventListenersForPredefinedAnnotations = function () {
            $('.define-predefined-annotations').on('change', '.annotation-checkbox', function () {
                var parent = $(this).closest(".predefined-annotation");
                if ($(this).is(':checked')) {
                    parent.find('.annotation-content').first().show();
                } else {
                    parent.find('.annotation-content').first().hide();
                    parent.find('.error-message').text("");
                    parent.find('.option-value').removeClass('required-input-field')
                }
            });
        };

        /**
         * @function to change the heading and the button text of the customized options div
         */
        FormUtils.prototype.changeCustomizedOptDiv = function (id) {
            var customizedOptionList = $('#customized-' + id + '-options').
                find('.cust-options li');
            var parent = $('#customized-' + id + '-options');
            if (customizedOptionList.length > 0) {
                parent.find('h4').show();
                parent.find('.btn-add-options').html('+ More');
            } else {
                parent.find('h4').hide();
                parent.find('.btn-add-options').html('+ Customized Option');
            }
        };

        /**
         * @function manages the attribute navigations
         * @param {Object} ulDiv div where the navigations needs to be altered
         */
        FormUtils.prototype.changeAttributeNavigation = function (ulDiv) {
            $(ulDiv).find('.attr-nav').empty();
            var attrLength = $(ulDiv).find('li').length;

            if (attrLength == 1) {
                $(ulDiv).find('li:eq(0)').find('.attr-nav').empty();
            }
            if (attrLength == 2) {
                $(ulDiv).find('li:eq(0)').find('.attr-nav').append('<a class = "reorder-down"><i class="fw ' +
                    'fw-sort-down"> </i></a><a class = "btn-del-attr"><i class="fw fw-delete"></i></a>');
                $(ulDiv).find('li:eq(1)').find('.attr-nav').append('<a class="reorder-up"> <i class="fw fw-sort-up">' +
                    '</i> </a><a class = "btn-del-attr"><i class="fw fw-delete"></i></a>');
            }
            if (attrLength > 2) {
                var lastIndex = attrLength - 1;
                for (var i = 0; i < attrLength; i++) {
                    $(ulDiv).find('li:eq(' + i + ')').find('.attr-nav').append('<a class="reorder-up"> ' +
                        '<i class="fw fw-sort-up"></i></a>' +
                        '<a class = "reorder-down"><i class="fw fw-sort-down"> </i></a>' +
                        '<a class = "btn-del-attr"><i class="fw fw-delete"></i></a>');
                }
                $(ulDiv).find('li:eq(0)').find('.attr-nav a:eq(0)').remove();
                $(ulDiv).find('li:eq(' + lastIndex + ')').find('.attr-nav a:eq(1)').remove();
            }
        };

        //to hide the customized option section
        FormUtils.prototype.hideCustomizedOptionsDiv = function () {
            $('.customized-options').remove();
        };

        //to prevent multi-selection of dropdown
        FormUtils.prototype.preventMultipleSelection = function (className) {
            var dropDown = $('.' + className + '-selection');
            dropDown.children().prop('disabled', false);
            dropDown.each(function () {
                var val = this.value;
                dropDown.not(this).children('[value="' + val + '"]').prop('disabled', true);
            });
        };

        /**
         * @function to prevent multiple selection. Prevents from selecting window multiple times
         */
        FormUtils.prototype.preventMultipleSelectionOfWindowStreamHandler = function (className) {
            var dropDown = $(className).find(' .' + Constants.STREAM_HANDLER + '-selection')
            dropDown.children().prop('disabled', false);
            dropDown.each(function () {
                var val = this.value;
                if (val === Constants.WINDOW)
                    dropDown.not(this).children('[value="' + Constants.WINDOW + '"]').prop('disabled', true);
            });
        };

        /**
         * @function to show and hide the order parameter of sort type
         */
        FormUtils.prototype.showHideOrderForSort = function () {
            if ($('#window-parameters #attribute-parameter').find('.parameter-checkbox').is(":checked")) {
                $('#window-parameters #order-parameter').show();
            } else {
                $('#window-parameters #order-parameter').hide();
            }
        };

        /**
         * @function to show and hide the window button for stream handlers
         */
        FormUtils.prototype.showHideStreamHandlerWindowButton = function (sourceDiv) {
            var found = false;
            $(sourceDiv).find('.define-stream-handler-type-content').each(function () {
                if ($(this).hasClass('define-window-stream-handler')) {
                    found = true;
                }
            });
            if (found) {
                $(sourceDiv).find('.btn-add-window').hide()
            } else {
                $(sourceDiv).find('.btn-add-window').show()
            }
        };

        /**
         * @function to show the + attribute button based on the max group-by attribute a user can select
         * @param {Int} maxLength
         */
        FormUtils.prototype.checkForAttributeLength = function (maxLength, className) {
            if ($('.' + className + '-attributes li').length >= maxLength) {
                $('.btn-add-' + className + '-attribute').hide();
            } else {
                $('.btn-add-' + className + '-attribute').show();
            }
        };

        /**
         * @function to remove the first delete button of primary/index annotation value
         */
        FormUtils.prototype.removeDeleteButtonOfPrimaryIndexAnnotationValue = function () {
            $('#primary-index-annotations .annotation').each(function () {
                $(this).find('.btn-del-annot-value:eq(0)').remove();
            });
        };

        /**
         * @function to pop up the element which is being currently edited
         */
        FormUtils.prototype.popUpSelectedElement = function (id) {
            $('#' + id).addClass('selected-element');
            $(".overlayed-container").fadeTo(200, 1);
        };

        /**
         * @function to add the error class
         * @param {Object} id object where the error needs to be added
         */
        FormUtils.prototype.addErrorClass = function (id) {
            $(id)[0].scrollIntoView();
            $(id).addClass('required-input-field');
            $(id).addClass('error-input-field');
            $(id).closest('.clearfix').next('label.error-message').show();
        };

        /**
         * @function to remove the error class
         */
        FormUtils.prototype.removeErrorClass = function () {
            $('.required-input-field').removeClass('required-input-field');
            $('.error-input-field').removeClass('error-input-field');
            $('.error-message').hide();
        };

        /**
         * @function to capitalize the first letter
         */
        FormUtils.prototype.capitalizeFirstLetter = function (text) {
            return text[0].toUpperCase() + text.slice(1);
        };

        /**
         * @function to find matches and drops down as user types in
         */
        FormUtils.prototype.substringMatcher = function (text) {
            return function findMatches(q, cb) {
                var matches, substringRegex;

                // an array that will be populated with substring matches
                matches = [];

                // regex used to determine if a string contains the substring `q`
                substringRegex = new RegExp(q, 'i');

                // iterate through the pool of strings and for any string that
                // contains the substring `q`, add it to the `matches` array
                $.each(text, function (i, str) {
                    if (substringRegex.test(str)) {
                        matches.push(str);
                    }
                });

                cb(matches);
            };
        };

        //split the given val for space
        FormUtils.prototype.splitForAutocomplete = function (val) {
            return val.split(/\s/g);
        };

        //obtain the last entered letter
        FormUtils.prototype.extractLast = function (term) {
            var self = this;
            return self.splitForAutocomplete(term).pop();
        };

        //create autocomplete for forms
        FormUtils.prototype.createAutocomplete = function (element, possibleOptions) {
            var self = this;
            $(element)
                // don't navigate away from the field on tab when selecting an item
                .on("keydown", function (event) {
                    if (event.keyCode === $.ui.keyCode.TAB &&
                        $(this).autocomplete("instance").menu.active) {
                        event.preventDefault();
                    }
                })
                .autocomplete({
                    minLength: 0,
                    source: function (request, response) {
                        // delegate back to autocomplete, but extract the last term
                        response($.ui.autocomplete.filter(
                            possibleOptions, self.extractLast(request.term)));
                    },
                    focus: function () {
                        // prevent value inserted on focus
                        return false;
                    },
                    select: function (event, ui) {
                        var terms = self.splitForAutocomplete(this.value);
                        // remove the current input
                        terms.pop();
                        // add the selected item
                        terms.push(ui.item.value);
                        // add placeholder to get the comma-and-space at the end
                        terms.push("");
                        this.value = terms.join(" ");
                        return false;
                    }

                });
        };

        /**
        * @function generate tooltip for siddhi app elements
        * @param element JSON object of the element
        * @param type type of the element
        * @returns {string} tooltip
        */
        FormUtils.prototype.getTooltip = function (element, type) {
            var appData = new AppData();

            switch (type) {
                case Constants.AGGREGATION:
                    appData.addAggregation(element);
                    break;

                case Constants.FUNCTION:
                    appData.addFunction(element);
                    break;

                case Constants.JOIN_QUERY:
                    appData.addJoinQuery(element);
                    break;

                case Constants.PARTITION:
                    appData.addPartition(element);
                    break;

                case Constants.PATTERN_QUERY:
                    appData.addPatternQuery(element);
                    break;

                case Constants.SEQUENCE_QUERY:
                    appData.addSequenceQuery(element);
                    break;

                case Constants.SINK:
                    appData.addSink(element);
                    break;

                case Constants.SOURCE:
                    appData.addSource(element);
                    break;

                case Constants.STREAM:
                    appData.addStream(element);
                    break;

                case Constants.TABLE:
                    appData.addTable(element);
                    break;

                case Constants.TRIGGER:
                    appData.addTrigger(element);
                    break;

                case Constants.WINDOW:
                    appData.addWindow(element);
                    break;

                case Constants.WINDOW_FILTER_PROJECTION_QUERY:
                    appData.addWindowFilterProjectionQuery(element);
                    break;
            };

            var self = this;
            var result = '';
            self.tooltipsURL = window.location.protocol + "//" + window.location.host + "/editor/tooltips";
            $.ajax({
                type: "POST",
                url: self.tooltipsURL,
                data: window.btoa(JSON.stringify(appData)),
                async: false,
                success: function (response) {
                    var toolTipObject = _.find(response, function (toolTip) {
                        return toolTip.id === element.getId();
                    });
                    if (toolTipObject !== undefined) {
                        result = toolTipObject.text;
                    }
                },
                error: function (error) {
                    if (error.responseText) {
                        log.error(error.responseText);
                    } else {
                        log.error("Error occurred while processing the request");
                    }
                }
            });
            return result;
        };

        /** Register classes for Handlebars */

        /** Generates the current index of the option being rendered */
        Handlebars.registerHelper('sum', function () {
            return Array.prototype.slice.call(arguments, 0, -1).reduce((acc, num) => acc += num);
        });

        /** Handlebar helper to check if the index is equivalent to half the length of the option's array */
        Handlebars.registerHelper('isDivisor', function (index, options) {
            var divLength = Math.ceil(options.length / 2);
            return index === divLength;
        });

        /** Handlebar helper to render heading for the form */
        Handlebars.registerHelper('addTitle', function (id) {
            return id.charAt(0).toUpperCase() + id.slice(1);
        });

        /** Handlebar helper to compare if the id is "source" or "sink" */
        Handlebars.registerHelper('ifSourceOrSink', function (id, div) {
            if (id === Constants.SOURCE || id === Constants.SINK) {
                return div.fn(this);
            }
            return div.inverse(this);
        });

        /** Handlebar helper to compare if the id is "group-by" or "order-by" */
        Handlebars.registerHelper('ifGroupOrOrderBy', function (id, div) {
            if (id === Constants.GROUP_BY || id === Constants.ORDER_BY) {
                return div.fn(this);
            }
            return div.inverse(this);
        });

        /** Handlebar helper to compare if the id is "source" or "sink" or "store" */
        Handlebars.registerHelper('ifSourceOrSinkOrStore', function (id, div) {
            if (id === Constants.SOURCE || id === Constants.SINK || id === Constants.STORE) {
                return div.fn(this);
            }
            return div.inverse(this);
        });

        /** Handlebar helper to check id is equivalent to a given string */
        Handlebars.registerHelper('ifId', function (id, name, div) {
            if (id === name) {
                return div.fn(this);
            }
            return div.inverse(this);
        });
        /** End of register classes */

        return FormUtils;
    });
