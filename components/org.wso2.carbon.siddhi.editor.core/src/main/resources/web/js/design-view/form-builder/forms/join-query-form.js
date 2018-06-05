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
        'joinQuerySource', 'streamHandler', 'queryWindowOrFunction'],
    function (require, log, $, _, QuerySelect, QueryOutputInsert, QueryOutputDelete, QueryOutputUpdate,
              QueryOutputUpdateOrInsertInto, QueryWindowOrFunction, QueryOrderByValue, joinQuerySource, StreamHandler) {

        var constants = {
            LEFT_SOURCE : 'Left Source',
            RIGHT_SOURCE : 'Right Source'
        };

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
         * @function generate the form for the join query
         * @param element selected element(query)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        JoinQueryForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var propertyDiv = $('<div id="property-header"><h3>Define Join Query </h3></div>' +
                '<div class="define-join-query"></div>');
            formContainer.append(propertyDiv);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var id = $(element).parent().attr('id');
            var clickedElement = self.configurationData.getSiddhiAppConfig().getJoinQuery(id);
            if (clickedElement.getQueryInput() === undefined
                || clickedElement.getQueryInput().getFirstConnectedElement() === undefined
                || clickedElement.getQueryInput().getSecondConnectedElement() === undefined) {
                alert('Connect two input elements to join query');
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            } else if (clickedElement.getQueryOutput() === undefined ||
                clickedElement.getQueryOutput().getTarget() === undefined) {
                alert('Connect an output element');
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            } else {

                var firstInputElementName = clickedElement.getQueryInput().getFirstConnectedElement().name;
                var secondInputElementName = clickedElement.getQueryInput().getSecondConnectedElement().name;

                // if left and right sources are defined then replace the element names with them. This first and
                // left name order is used to define/display the left and right sources in the form.
                if (clickedElement.getQueryInput().getLeft() !== undefined
                    && clickedElement.getQueryInput().getRight() !== undefined) {
                    firstInputElementName = clickedElement.getQueryInput().getLeft().getFrom();
                    secondInputElementName = clickedElement.getQueryInput().getRight().getFrom();
                }

                var savedAnnotations = clickedElement.getAnnotationList();
                var annotations = [];
                _.forEach(savedAnnotations, function (savedAnnotation) {
                    annotations.push({annotation: savedAnnotation});
                });

                var leftSourceSavedData = clickedElement.getQueryInput().getLeft();
                var rightSourceSavedData = clickedElement.getQueryInput().getRight();
                var joinType = clickedElement.getQueryInput().getJoinType();
                if (joinType !== undefined) {
                    if (joinType === "JOIN") {
                        joinType = "join";
                    } else if (joinType === "LEFT_OUTER") {
                        joinType = "left outer";
                    } else if (joinType === "RIGHT_OUTER") {
                        joinType = "right outer";
                    } else if (joinType === "FULL_OUTER") {
                        joinType = "full outer";
                    }
                }
                var on = clickedElement.getQueryInput().getOn();
                var within = clickedElement.getQueryInput().getWithin();
                var per = clickedElement.getQueryInput().getPer();
                var savedGroupByAttributes = clickedElement.getGroupBy();
                var having = clickedElement.getHaving();
                var savedOrderByAttributes = clickedElement.getOrderBy();
                var limit = clickedElement.getLimit();
                var outputRateLimit = clickedElement.getOutputRateLimit();
                var outputElementName = clickedElement.getQueryOutput().getTarget();

                var groupBy = [];
                _.forEach(savedGroupByAttributes, function (savedGroupByAttribute) {
                    var groupByAttributeObject = {
                        attribute: savedGroupByAttribute
                    };
                    groupBy.push(groupByAttributeObject);
                });

                var orderBy = [];
                _.forEach(savedOrderByAttributes, function (savedOrderByValue) {
                    var orderByValueObject = {
                        attribute: savedOrderByValue.getValue(),
                        order: (savedOrderByValue.getOrder()).toLowerCase()
                    };
                    orderBy.push(orderByValueObject);
                });
                //
                var possibleGroupByAttributes = [];
                var firstInputElementType = undefined;
                var secondInputElementType = undefined;
                var outputElementType = undefined;
                var outputElementAttributesList = [];

                var firstInputElement =
                    self.configurationData.getSiddhiAppConfig().getDefinitionElementByName(firstInputElementName);
                var secondInputElement =
                    self.configurationData.getSiddhiAppConfig().getDefinitionElementByName(secondInputElementName);
                if (firstInputElement !== undefined && secondInputElement !== undefined) {

                    if (firstInputElement.type !== undefined && firstInputElement.type === 'TRIGGER') {
                        firstInputElementType = firstInputElement.type;
                        possibleGroupByAttributes.push(firstInputElementName + '.triggered_time');
                    } else if(firstInputElement.type !== undefined && firstInputElement.type === 'AGGREGATION') {
                        firstInputElementType = firstInputElement.type;
                        if (firstInputElement.element !== undefined) {
                            _.forEach(firstInputElement.element.getSelect().getValue(), function (selectAttribute) {
                                possibleGroupByAttributes.push(firstInputElementName + "." + selectAttribute.as);
                            });
                        }
                    } else if (firstInputElement.type !== undefined) {
                        firstInputElementType = firstInputElement.type;
                        if (firstInputElement.element !== undefined) {
                            _.forEach(firstInputElement.element.getAttributeList(), function (attribute) {
                                possibleGroupByAttributes.push(firstInputElementName + "." + attribute.getName());
                            });
                        }
                    }

                    if (secondInputElement.type !== undefined && secondInputElement.type === 'TRIGGER'){
                        secondInputElementType = secondInputElement.type;
                        possibleGroupByAttributes.push(secondInputElementName + '.triggered_time');
                    } else if(secondInputElement.type !== undefined && secondInputElement.type === 'AGGREGATION') {
                        secondInputElementType = secondInputElement.type;
                        if (secondInputElement.element !== undefined) {
                            _.forEach(secondInputElement.element.getSelect().getValue(), function (selectAttribute) {
                                possibleGroupByAttributes.push(secondInputElement + "." + selectAttribute.as);
                            });
                        }
                    } else if (secondInputElement.type !== undefined) {
                        secondInputElementType = secondInputElement.type;
                        if (secondInputElement.element !== undefined) {
                            _.forEach(secondInputElement.element.getAttributeList(), function (attribute) {
                                possibleGroupByAttributes.push(secondInputElementName + "." + attribute.getName());
                            });
                        }
                    }
                }

                var outputElement =
                    self.configurationData.getSiddhiAppConfig().getDefinitionElementByName(outputElementName);
                if (outputElement !== undefined) {
                    if (outputElement.type !== undefined
                        && (outputElement.type === 'STREAM' || outputElement.type === 'TABLE'
                            || outputElement.type === 'WINDOW')) {
                        outputElementType = outputElement.type;
                        if (outputElement.element !== undefined) {
                            outputElementAttributesList = outputElement.element.getAttributeList();
                        }
                    }
                }

                var select = [];
                var possibleUserDefinedSelectTypeValues = [];
                if (clickedElement.getSelect() === undefined) {
                    for (var i = 0; i < outputElementAttributesList.length; i++) {
                        var attr = {
                            expression: undefined,
                            as: outputElementAttributesList[i].getName()
                        };
                        select.push(attr);
                    }
                } else if(clickedElement.getSelect().getValue() === undefined) {
                    for (var i = 0; i < outputElementAttributesList.length; i++) {
                        var attr = {
                            expression: undefined,
                            as: outputElementAttributesList[i].getName()
                        };
                        select.push(attr);
                    }
                } else if (clickedElement.getSelect().getValue() === '*') {
                    select = '*';
                    for (var i = 0; i < outputElementAttributesList.length; i++) {
                        var attr = {
                            expression: undefined,
                            as: outputElementAttributesList[i].getName()
                        };
                        possibleUserDefinedSelectTypeValues.push(attr);
                    }
                } else if (!(clickedElement.getSelect().getValue() === '*')) {
                    var selectedAttributes = clickedElement.getSelect().getValue();
                    for (var i = 0; i < outputElementAttributesList.length; i++) {
                        var expressionStatement = undefined;
                        if (selectedAttributes[i] !== undefined && selectedAttributes[i].expression !== undefined) {
                            expressionStatement = selectedAttributes[i].expression;
                        }
                        var attr = {
                            expression: expressionStatement,
                            as: outputElementAttributesList[i].getName()
                        };
                        select.push(attr);
                    }
                }

                var savedQueryOutput = clickedElement.getQueryOutput();
                if (savedQueryOutput !== undefined) {
                    var savedQueryOutputTarget = savedQueryOutput.getTarget();
                    var savedQueryOutputType = savedQueryOutput.getType();
                    var output = savedQueryOutput.getOutput();
                    var queryOutput;
                    if ((savedQueryOutputTarget !== undefined)
                        && (savedQueryOutputType !== undefined)
                        && (output !== undefined)) {
                        // getting the event tpe and pre load it
                        var eventType;
                        if (output.getEventType() === undefined) {
                            eventType = 'all events';
                        } else if (output.getEventType() === 'ALL_EVENTS') {
                            eventType = 'all events';
                        } else if (output.getEventType() === 'CURRENT_EVENTS') {
                            eventType = 'current events';
                        } else if (output.getEventType() === 'EXPIRED_EVENTS') {
                            eventType = 'expired events';
                        }
                        if (savedQueryOutputType === "INSERT") {
                            queryOutput = {
                                insertTarget: savedQueryOutputTarget,
                                eventType: eventType
                            };
                        } else if (savedQueryOutputType === "DELETE") {
                            queryOutput = {
                                deleteTarget: savedQueryOutputTarget,
                                eventType: eventType,
                                on: output.getOn()
                            };
                        } else if (savedQueryOutputType === "UPDATE") {
                            queryOutput = {
                                updateTarget: savedQueryOutputTarget,
                                eventType: eventType,
                                set: output.getSet(),
                                on: output.getOn()
                            };
                        } else if (savedQueryOutputType === "UPDATE_OR_INSERT_INTO") {
                            queryOutput = {
                                updateOrInsertIntoTarget: savedQueryOutputTarget,
                                eventType: eventType,
                                set: output.getSet(),
                                on: output.getOn()
                            };
                        }
                    }
                }

                self.leftSourceStartValues = {};
                self.rightSourceStartValues = {};

                var fillQueryAnnotation = {
                    annotations: annotations
                };
                fillQueryAnnotation = self.formUtils.cleanJSONObject(fillQueryAnnotation);

                var fillQueryInputWith = {
                    left: self.leftSourceStartValues,
                    joinType: {
                        type: joinType
                    },
                    right: self.rightSourceStartValues,
                    on : {
                        condition: on
                    },
                    within: {
                        condition: within
                    },
                    per: {
                        condition : per
                    }
                };
                fillQueryInputWith = self.formUtils.cleanJSONObject(fillQueryInputWith);
                var fillQuerySelectWith = {
                    select : select,
                    groupBy : groupBy,
                    postFilter: {
                        having : having
                    }
                };
                fillQuerySelectWith = self.formUtils.cleanJSONObject(fillQuerySelectWith);
                var fillQueryOutputWith = {
                    orderBy : orderBy,
                    limit: {
                        limit : limit
                    },
                    outputRateLimit: {
                        outputRateLimit : outputRateLimit
                    },
                    output: queryOutput
                };
                fillQueryOutputWith = self.formUtils.cleanJSONObject(fillQueryOutputWith);

                var inputSchema = {
                    type: "object",
                    title: "Query Input",
                    required: true,
                    options: {
                        disable_properties: false
                    },
                    properties: {
                        left: {},
                        joinType: {
                            required: true,
                            propertyOrder: 2,
                            type: "object",
                            title: "Join",
                            properties: {
                                type: {
                                    required: true,
                                    title: "Type",
                                    type: "string",
                                    enum: ['join', 'left outer', 'right outer', 'full outer'],
                                    default: 'join'
                                }
                            }
                        },
                        right: {},
                        on: {
                            propertyOrder: 4,
                            type: "object",
                            title: "On",
                            properties: {
                                condition: {
                                    required: true,
                                    title: "Condition",
                                    type: "string",
                                    minLength: 1
                                }
                            }
                        }
                    },
                    definitions: {
                        filter: {
                            type: "object",
                            title: "Filter",
                            required: true,
                            properties: {
                                filter: {
                                    required: true,
                                    title: "Filter Condition",
                                    type: "string",
                                    minLength: 1
                                }
                            }
                        },
                        window: {
                            title: "Window",
                            type: "object",
                            required: true,
                            properties: {
                                windowName: {
                                    required: true,
                                    title: "Window Name",
                                    type: "string",
                                    minLength: 1
                                },
                                parameters: {
                                    required: true,
                                    type: "array",
                                    format: "table",
                                    title: "Parameters",
                                    minItems: 1,
                                    items: {
                                        type: "object",
                                        title: 'Attribute',
                                        properties: {
                                            parameter: {
                                                required: true,
                                                type: 'string',
                                                title: 'Parameter Name',
                                                minLength: 1
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        functionDef: {
                            title: "Function",
                            type: "object",
                            required: true,
                            properties: {
                                functionName: {
                                    required: true,
                                    title: "Function Name",
                                    type: "string",
                                    minLength: 1
                                },
                                parameters: {
                                    required: true,
                                    type: "array",
                                    format: "table",
                                    title: "Parameters",
                                    minItems: 1,
                                    items: {
                                        type: "object",
                                        title: 'Attribute',
                                        properties: {
                                            parameter: {
                                                required: true,
                                                type: 'string',
                                                title: 'Parameter Name',
                                                minLength: 1
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                };

                var leftSchema =
                    self.getJoinSourceSchema(firstInputElementType, firstInputElementName, secondInputElementName,
                        constants.LEFT_SOURCE, leftSourceSavedData);
                var rightSchema =
                    self.getJoinSourceSchema(secondInputElementType, secondInputElementName, firstInputElementName,
                        constants.RIGHT_SOURCE, rightSourceSavedData);
                _.set(inputSchema.properties, 'left', leftSchema);
                _.set(inputSchema.properties, 'right', rightSchema);
                _.set(fillQueryInputWith, 'left', self.leftSourceStartValues);
                _.set(fillQueryInputWith, 'right', self.rightSourceStartValues);

                // add within and per clauses to input schema if one of the input element is an aggregation
                if (firstInputElementType === "AGGREGATION" || secondInputElementType === "AGGREGATION") {
                    var withinSchema = {
                        propertyOrder: 5,
                        type: "object",
                        required: true,
                        title: "Within",
                        properties: {
                            condition: {
                                required: true,
                                title: "Condition",
                                type: "string",
                                minLength: 1
                            }
                        }
                    };
                    var perSchema = {
                        propertyOrder: 6,
                        type: "object",
                        required: true,
                        title: "Per",
                        properties: {
                            condition: {
                                required: true,
                                title: "Condition",
                                type: "string",
                                minLength: 1
                            }
                        }
                    };
                    _.set(inputSchema.properties, 'within', withinSchema);
                    _.set(inputSchema.properties, 'per', perSchema);
                }

                var outputSchema;
                if (outputElementType === 'TABLE') {
                    outputSchema = {
                        title: "Action",
                        propertyOrder: 5,
                        required: true,
                        oneOf: [
                            {
                                $ref: "#/definitions/queryOutputInsertType",
                                title: "Insert"
                            },
                            {
                                $ref: "#/definitions/queryOutputDeleteType",
                                title: "Delete"
                            },
                            {
                                $ref: "#/definitions/queryOutputUpdateType",
                                title: "Update"
                            },
                            {
                                $ref: "#/definitions/queryOutputUpdateOrInsertIntoType",
                                title: "Update Or Insert"
                            }
                        ]
                    };
                } else {
                    outputSchema = {
                        required: true,
                        title: "Action",
                        propertyOrder: 5,
                        type: "object",
                        properties: {
                            insert: {
                                required: true,
                                title: "Operation",
                                type: "string",
                                template: "Insert"
                            },
                            insertTarget: {
                                type: 'string',
                                title: 'Into',
                                template: savedQueryOutputTarget,
                                required: true
                            },
                            eventType: {
                                required: true,
                                title: "For",
                                type: "string",
                                enum: ['current events', 'expired events', 'all events'],
                                default: 'all events'
                            }
                        }
                    };
                }

                formContainer.append('<div class="row"><div id="form-query-annotation" class="col-md-12"></div></div>' +
                    '<div class="row"><div id="form-query-input" class="col-md-4"></div>' +
                    '<div id="form-query-select" class="col-md-4"></div>' +
                    '<div id="form-query-output" class="col-md-4"></div></div>');

                var editorAnnotation = new JSONEditor($(formContainer).find('#form-query-annotation')[0], {
                    schema: {
                        type: "object",
                        title: "Query Annotations",
                        properties: {
                            annotations: {
                                propertyOrder: 1,
                                type: "array",
                                format: "table",
                                title: "Add Annotations",
                                uniqueItems: true,
                                minItems: 1,
                                items: {
                                    type: "object",
                                    title: "Annotation",
                                    options: {
                                        disable_properties: true
                                    },
                                    properties: {
                                        annotation: {
                                            title: "Annotation",
                                            type: "string",
                                            minLength: 1
                                        }
                                    }
                                }
                            }
                        }
                    },
                    startval: fillQueryAnnotation,
                    show_errors: "always",
                    display_required_only: true,
                    no_additional_properties: true,
                    disable_array_delete_all_rows: true,
                    disable_array_delete_last_row: true
                });

                var editorInput = new JSONEditor($(formContainer).find('#form-query-input')[0], {
                    schema: inputSchema,
                    startval: fillQueryInputWith,
                    show_errors: "always",
                    disable_properties: true,
                    display_required_only: true,
                    no_additional_properties: true,
                    disable_array_delete_all_rows: true,
                    disable_array_delete_last_row: true
                });

                /*
                * This function adds watch fields for the input section. Each time a new editorInput is created,
                * this function needs to be called.
                * */
                function addWatchFieldsForInput() {
                    editorInput.watch('root.left.input', function () {
                        var leftFromNode = editorInput.getEditor('root.left.input.from');
                        var oldLeftSourceFromValue = editorInput.getValue().left.input.from;
                        var newLeftSourceFromValue = leftFromNode.getValue();
                        interChangeSourceDataAndSchema(oldLeftSourceFromValue, newLeftSourceFromValue);
                    });

                    editorInput.watch('root.right.input', function () {
                        var rightFromNode = editorInput.getEditor('root.right.input.from');
                        var oldRightSourceFromValue = editorInput.getValue().right.input.from;
                        var newRightSourceFromValue = rightFromNode.getValue();
                        interChangeSourceDataAndSchema(oldRightSourceFromValue, newRightSourceFromValue);
                    });

                    var leftIsUnidirectionalNode = editorInput.getEditor('root.left.isUnidirectional');
                    var rightIsUnidirectionalNode = editorInput.getEditor('root.right.isUnidirectional');

                    editorInput.watch('root.right.isUnidirectional', function () {
                        var newRightIsUnidirectionalValue = rightIsUnidirectionalNode.getValue();
                        if (newRightIsUnidirectionalValue) {
                            leftIsUnidirectionalNode.setValue(false);
                        }
                    });

                    editorInput.watch('root.left.isUnidirectional', function () {
                        var newLeftIsUnidirectionalValue = leftIsUnidirectionalNode.getValue();
                        if (newLeftIsUnidirectionalValue) {
                            rightIsUnidirectionalNode.setValue(false);
                        }
                    });
                }

                /*
                * This function will swap the left and right sources data in the form.
                * Right source values will be saved in the left source and vice versa.
                * Since the input schema is changed, we have to reset the watch functions
                * for the each left and right source changes.
                * */
                function interChangeSourceDataAndSchema(oldFromSourceValue, newFromSourceValue) {
                    if (oldFromSourceValue !== newFromSourceValue) {
                        var newLeftSchema =
                            self.getJoinSourceSchema(secondInputElementType, secondInputElementName,
                                firstInputElementName, constants.LEFT_SOURCE);
                        var newRightSchema =
                            self.getJoinSourceSchema(firstInputElementType, firstInputElementName,
                                secondInputElementName, constants.RIGHT_SOURCE);
                        _.set(inputSchema.properties, 'left', newLeftSchema);
                        _.set(inputSchema.properties, 'right', newRightSchema);

                        function swapValues(value1, value2) {
                            var temporaryValue1= value1;
                            value1 = value2;
                            value2 = temporaryValue1;
                        }

                        swapValues(firstInputElementName, secondInputElementName);
                        swapValues(firstInputElementType, secondInputElementType);

                        var newStartingValues = {
                            left: editorInput.getValue().right,
                            joinType: editorInput.getValue().joinType,
                            right: editorInput.getValue().left,
                            on : editorInput.getValue().on,
                            within: editorInput.getValue().within,
                            per: editorInput.getValue().per
                        };

                        newStartingValues = self.formUtils.cleanJSONObject(newStartingValues);
                        $(formContainer).find('#form-query-input').empty();
                        editorInput = new JSONEditor($(formContainer).find('#form-query-input')[0], {
                            schema: inputSchema,
                            startval: newStartingValues,
                            show_errors: "always",
                            disable_properties: true,
                            display_required_only: true,
                            no_additional_properties: true,
                            disable_array_delete_all_rows: true,
                            disable_array_delete_last_row: true,
                            disable_array_reorder: true
                        });
                        addWatchFieldsForInput();
                    }
                }

                addWatchFieldsForInput();

                var selectScheme = {
                    schema: {
                        required: true,
                        options: {
                            disable_properties: false
                        },
                        type: "object",
                        title: "Query Select",
                        properties: {
                            select: {
                                propertyOrder: 1,
                                title: "Select",
                                required: true,
                                oneOf: [
                                    {
                                        $ref: "#/definitions/querySelectUserDefined",
                                        title: "User Defined Attributes"
                                    },
                                    {
                                        $ref: "#/definitions/querySelectAll",
                                        title: "All Attributes"
                                    }
                                ]
                            },
                            groupBy: {
                                propertyOrder: 2,
                                type: "array",
                                format: "table",
                                title: "Group By Attributes",
                                uniqueItems: true,
                                minItems: 1,
                                items: {
                                    type: "object",
                                    title: 'Attribute',
                                    properties: {
                                        attribute: {
                                            type: 'string',
                                            title: 'Attribute Name',
                                            enum: possibleGroupByAttributes
                                        }
                                    }
                                }
                            },
                            postFilter: {
                                propertyOrder: 3,
                                type: "object",
                                title: "Post Select Filter",
                                properties: {
                                    having: {
                                        required: true,
                                        title: "Condition",
                                        type: "string",
                                        minLength: 1
                                    }
                                }
                            }
                        },
                        definitions: {
                            querySelectUserDefined: {
                                required: true,
                                type: "array",
                                format: "table",
                                title: "Select Attributes",
                                uniqueItems: true,
                                options: {
                                    disable_array_add: true,
                                    disable_array_delete: true
                                },
                                items: {
                                    title: "Value Set",
                                    type: "object",
                                    properties: {
                                        expression: {
                                            title: "Expression",
                                            type: "string",
                                            minLength: 1
                                        },
                                        as: {
                                            title: "As",
                                            type: "string"
                                        }
                                    }
                                }
                            },
                            querySelectAll: {
                                type: "string",
                                title: "Select All Attributes",
                                template: '*'
                            }
                        }
                    },
                    startval: fillQuerySelectWith,
                    show_errors: "always",
                    disable_properties: true,
                    display_required_only: true,
                    no_additional_properties: true,
                    disable_array_delete_all_rows: true,
                    disable_array_delete_last_row: true,
                    disable_array_reorder: true
                };
                var editorSelect = new JSONEditor($(formContainer).find('#form-query-select')[0], selectScheme);
                var selectNode = editorSelect.getEditor('root.select');
                //disable fields that can not be changed
                if (!(selectNode.getValue() === "*")) {
                    for (var i = 0; i < outputElementAttributesList.length; i++) {
                        editorSelect.getEditor('root.select.' + i + '.as').disable();
                    }
                }

                editorSelect.watch('root.select', function () {
                    var oldSelectValue = editorSelect.getValue().select;
                    var newSelectValue = selectNode.getValue();
                    if (oldSelectValue === "*" && newSelectValue !== "*") {
                        if (select === "*") {
                            fillQuerySelectWith = {
                                select: possibleUserDefinedSelectTypeValues,
                                groupBy: editorSelect.getValue().groupBy,
                                postFilter: editorSelect.getValue().postFilter
                            };
                        } else {
                            fillQuerySelectWith = {
                                select: select,
                                groupBy: editorSelect.getValue().groupBy,
                                postFilter: editorSelect.getValue().postFilter
                            };
                        }
                        fillQuerySelectWith = self.formUtils.cleanJSONObject(fillQuerySelectWith);
                        selectScheme.startval = fillQuerySelectWith;
                        $(formContainer).find('#form-query-select').empty();
                        editorSelect = new JSONEditor($(formContainer).find('#form-query-select')[0], selectScheme);
                        //disable fields that can not be changed
                        for (var i = 0; i < outputElementAttributesList.length; i++) {
                            editorSelect.getEditor('root.select.' + i + '.as').disable();
                        }
                    }
                });

                var editorOutput = new JSONEditor($(formContainer).find('#form-query-output')[0], {
                    schema: {
                        required: true,
                        type: "object",
                        title: "Query Output",
                        options: {
                            disable_properties: false
                        },
                        properties: {
                            orderBy: {
                                propertyOrder: 2,
                                type: "array",
                                format: "table",
                                title: "Order By Attributes",
                                uniqueItems: true,
                                minItems: 1,
                                items: {
                                    type: "object",
                                    title: 'Attribute',
                                    properties: {
                                        attribute: {
                                            required: true,
                                            type: 'string',
                                            title: 'Attribute Name',
                                            enum: possibleGroupByAttributes
                                        },
                                        order: {
                                            required: true,
                                            type: "string",
                                            title: "Order",
                                            enum: ['asc', 'desc'],
                                            default: 'asc'
                                        }
                                    }
                                }
                            },
                            limit: {
                                propertyOrder: 3,
                                type: "object",
                                title: "Limit",
                                properties: {
                                    limit: {
                                        required: true,
                                        title: "Number of Events per Output",
                                        type: "number",
                                        minimum: 0
                                    }
                                }
                            },
                            outputRateLimit: {
                                propertyOrder: 4,
                                type: "object",
                                title: "Rate Limiting",
                                properties: {
                                    outputRateLimit: {
                                        required: true,
                                        title: "By Events/Time/Snapshot",
                                        type: "string",
                                        minLength: 1
                                    }
                                }
                            },
                            output: outputSchema
                        },
                        definitions: {
                            queryOutputInsertType: {
                                required: true,
                                title: "Action",
                                type: "object",
                                options: {
                                    disable_properties: true
                                },
                                properties: {
                                    insertTarget: {
                                        type: 'string',
                                        title: 'Into',
                                        template: savedQueryOutputTarget,
                                        required: true
                                    },
                                    eventType: {
                                        required: true,
                                        title: "For",
                                        type: "string",
                                        enum: ['current events', 'expired events', 'all events'],
                                        default: 'all events'
                                    }
                                }
                            },
                            queryOutputDeleteType: {
                                required: true,
                                title: "Action",
                                type: "object",
                                options: {
                                    disable_properties: true
                                },
                                properties: {
                                    deleteTarget: {
                                        type: 'string',
                                        title: 'From',
                                        template: savedQueryOutputTarget,
                                        required: true
                                    },
                                    eventType: {
                                        title: "For",
                                        type: "string",
                                        enum: ['current events', 'expired events', 'all events'],
                                        default: 'all events',
                                        required: true
                                    },
                                    on: {
                                        type: 'string',
                                        title: 'On Condition',
                                        minLength: 1,
                                        required: true
                                    }
                                }
                            },
                            queryOutputUpdateType: {
                                required: true,
                                title: "Action",
                                type: "object",
                                options: {
                                    disable_properties: true
                                },
                                properties: {
                                    updateTarget: {
                                        type: 'string',
                                        title: 'From',
                                        template: savedQueryOutputTarget,
                                        required: true
                                    },
                                    eventType: {
                                        title: "For",
                                        type: "string",
                                        enum: ['current events', 'expired events', 'all events'],
                                        default: 'all events',
                                        required: true
                                    },
                                    set: {
                                        required: true,
                                        type: "array",
                                        format: "table",
                                        title: "Set",
                                        uniqueItems: true,
                                        items: {
                                            type: "object",
                                            title: 'Set Condition',
                                            properties: {
                                                attribute: {
                                                    type: "string",
                                                    title: 'Attribute',
                                                    minLength: 1
                                                },
                                                value: {
                                                    type: "string",
                                                    title: 'Value',
                                                    minLength: 1
                                                }
                                            }
                                        }
                                    },
                                    on: {
                                        type: 'string',
                                        title: 'On Condition',
                                        minLength: 1,
                                        required: true
                                    }
                                }
                            },
                            queryOutputUpdateOrInsertIntoType: {
                                required: true,
                                title: "Action",
                                type: "object",
                                options: {
                                    disable_properties: true
                                },
                                properties: {
                                    updateOrInsertIntoTarget: {
                                        type: 'string',
                                        title: 'From/Into',
                                        template: savedQueryOutputTarget,
                                        required: true
                                    },
                                    eventType: {
                                        title: "For",
                                        type: "string",
                                        enum: ['current events', 'expired events', 'all events'],
                                        default: 'all events',
                                        required: true
                                    },
                                    set: {
                                        required: true,
                                        type: "array",
                                        format: "table",
                                        title: "Set",
                                        uniqueItems: true,
                                        items: {
                                            type: "object",
                                            title: 'Set Condition',
                                            properties: {
                                                attribute: {
                                                    type: "string",
                                                    title: 'Attribute',
                                                    minLength: 1
                                                },
                                                value: {
                                                    type: "string",
                                                    title: 'Value',
                                                    minLength: 1
                                                }
                                            }
                                        }
                                    },
                                    on: {
                                        type: 'string',
                                        title: 'On Condition',
                                        minLength: 1,
                                        required: true
                                    }
                                }

                            }

                        }
                    },
                    startval: fillQueryOutputWith,
                    show_errors: "always",
                    disable_properties: true,
                    display_required_only: true,
                    no_additional_properties: true,
                    disable_array_delete_all_rows: true,
                    disable_array_delete_last_row: true,
                    disable_array_reorder: true
                });

                formContainer.append('<div id="form-submit"><button type="button" ' +
                    'class="btn btn-default">Submit</button></div>' +
                    '<div id="form-cancel"><button type="button" class="btn btn-default">Cancel</button></div>');

                // 'Submit' button action
                var submitButtonElement = $(formContainer).find('#form-submit')[0];
                submitButtonElement.addEventListener('click', function () {

                    var annotationErrors = editorAnnotation.validate();
                    var inputErrors = editorInput.validate();
                    var selectErrors = editorSelect.validate();
                    var outputErrors = editorOutput.validate();
                    if(annotationErrors.length || inputErrors.length || selectErrors.length || outputErrors.length) {
                        return;
                    }

                    var annotationConfig = editorAnnotation.getValue();
                    var inputConfig = editorInput.getValue();
                    var selectConfig = editorSelect.getValue();
                    var outputConfig = editorOutput.getValue();

                    // checks stream handlers related validations
                    function validateSourceStreamHandlers(joinConfiguration, joinSourceSide) {
                        var validity = true;
                        var noOfFiltersInSource = 0;
                        var noOfWindowsInSource = 0;
                        _.forEach(joinConfiguration.streamHandlerList, function (streamHandler) {
                            streamHandler = streamHandler.streamHandler;
                            if (streamHandler.windowName !== undefined) {
                                noOfWindowsInSource++;
                            } else if (streamHandler.filter !== undefined) {
                                noOfFiltersInSource++;
                            }
                        });
                        var elementType;
                        if (joinSourceSide === constants.LEFT_SOURCE) {
                            elementType = firstInputElementType;
                        } else {
                            elementType = secondInputElementType;
                        }
                        if (noOfWindowsInSource > 1) {
                            alert("Only one window can be defined in a join source!");
                            validity = false;
                        } else if (noOfFiltersInSource > 0
                            && noOfWindowsInSource === 0 && elementType !== 'WINDOW') {
                            alert("Since a filter is defined, a window is also needed to be defined in join source!");
                            validity = false;
                        } else if (noOfWindowsInSource === 1){
                            var streamHandlerListLength = joinConfiguration.streamHandlerList.length;
                            var lastStreamHandlerInList = joinConfiguration.streamHandlerList[streamHandlerListLength-1];
                            if (lastStreamHandlerInList.streamHandler.windowName === undefined) {
                                alert("Window should be defined as the last stream handler in a join source!");
                                validity = false;
                            }
                        }
                        return validity;
                    }

                    if (!validateSourceStreamHandlers(inputConfig.left, constants.LEFT_SOURCE)
                        || !validateSourceStreamHandlers(inputConfig.right, constants.RIGHT_SOURCE)) {
                        return;
                    }
                    clickedElement.clearAnnotationList();
                    _.forEach(annotationConfig.annotations, function (annotation) {
                        clickedElement.addAnnotation(annotation.annotation);
                    });

                    var queryInput = clickedElement.getQueryInput();

                    function saveDataForJoinSources(joinConfiguration, joinSourceSide) {
                        var sourceOptions = {};
                        if (joinSourceSide === constants.LEFT_SOURCE) {
                            _.set(sourceOptions, 'type', firstInputElementType);
                        } else {
                            _.set(sourceOptions, 'type', secondInputElementType);
                        }
                        if (joinConfiguration.input.from !== undefined) {
                            _.set(sourceOptions, 'from', joinConfiguration.input.from);
                        } else {
                            _.set(sourceOptions, 'from', undefined);
                        }
                        if (joinConfiguration.as !== undefined && joinConfiguration.as.name !== undefined) {
                            _.set(sourceOptions, 'as', joinConfiguration.as.name);
                        } else {
                            _.set(sourceOptions, 'as', undefined);
                        }
                        if (joinConfiguration.isUnidirectional !== undefined) {
                            _.set(sourceOptions, 'isUnidirectional', joinConfiguration.isUnidirectional);
                        } else {
                            _.set(sourceOptions, 'isUnidirectional', undefined);
                        }
                        var joinSource = new joinQuerySource(sourceOptions);

                        _.forEach(joinConfiguration.streamHandlerList, function (streamHandler) {
                            streamHandler = streamHandler.streamHandler;
                            var streamHandlerOptions = {};
                            var parameters = [];
                            if (streamHandler.windowName !== undefined) {
                                var windowOptions = {};
                                _.set(windowOptions, 'function', streamHandler.windowName);
                                _.forEach(streamHandler.parameters, function (parameter) {
                                    parameters.push(parameter.parameter);
                                });
                                _.set(windowOptions, 'parameters', parameters);
                                var queryWindow = new QueryWindowOrFunction(windowOptions);
                                _.set(streamHandlerOptions, 'type', 'WINDOW');
                                _.set(streamHandlerOptions, 'value', queryWindow);
                            } else if (streamHandler.functionName !== undefined) {
                                var functionOptions = {};
                                _.set(functionOptions, 'function', streamHandler.functionName);
                                _.forEach(streamHandler.parameters, function (parameter) {
                                    parameters.push(parameter.parameter);
                                });
                                _.set(functionOptions, 'parameters', parameters);
                                var queryFunction = new QueryWindowOrFunction(functionOptions);
                                _.set(streamHandlerOptions, 'type', 'FUNCTION');
                                _.set(streamHandlerOptions, 'value', queryFunction);
                            } else if (streamHandler.filter !== undefined) {
                                _.set(streamHandlerOptions, 'type', 'FILTER');
                                _.set(streamHandlerOptions, 'value', streamHandler.filter);
                            } else {
                                console.log("Unknown stream handler received!");
                            }
                            var streamHandlerObject = new StreamHandler(streamHandlerOptions);
                            joinSource.addStreamHandler(streamHandlerObject);
                        });

                        return joinSource;
                    }
                    
                    // saving data related to left and right source
                    var leftSource = saveDataForJoinSources(inputConfig.left, constants.LEFT_SOURCE);
                    queryInput.setLeft(leftSource);

                    var rightSource = saveDataForJoinSources(inputConfig.right, constants.RIGHT_SOURCE);
                    queryInput.setRight(rightSource);
                    
                    var joinWithType = undefined;
                    if (firstInputElementType === "TABLE" || secondInputElementType === "TABLE") {
                        joinWithType = "TABLE";
                    } else if (firstInputElementType === "WINDOW" || secondInputElementType === "WINDOW") {
                        joinWithType = "WINDOW";
                    } else if (firstInputElementType === "AGGREGATION" || secondInputElementType === "AGGREGATION") {
                        joinWithType = "AGGREGATION";
                    } else if (firstInputElementType === "TRIGGER" || secondInputElementType === "TRIGGER") {
                        joinWithType = "TRIGGER";
                    } else if (firstInputElementType === "STREAM" && secondInputElementType === "STREAM") {
                        joinWithType = "STREAM";
                    } else {
                        console.log("Unknown join with type received!")
                    }

                    queryInput.setJoinWith(joinWithType);

                    if (inputConfig.joinType !== undefined && inputConfig.joinType.type !== undefined) {
                        queryInput.setJoinType(inputConfig.joinType.type);
                    } else {
                        queryInput.setJoinType(undefined);
                    }

                    if (inputConfig.on !== undefined && inputConfig.on.condition !== undefined) {
                        queryInput.setOn(inputConfig.on.condition);
                    } else {
                        queryInput.setOn(undefined);
                    }

                    if (inputConfig.within !== undefined && inputConfig.within.condition !== undefined) {
                        queryInput.setWithin(inputConfig.within.condition);
                    } else {
                        queryInput.setWithin(undefined);
                    }

                    if (inputConfig.per !== undefined && inputConfig.per.condition !== undefined) {
                        queryInput.setPer(inputConfig.per.condition);
                    } else {
                        queryInput.setPer(undefined);
                    }

                    var selectAttributeOptions = {};
                    if (selectConfig.select instanceof Array) {
                        _.set(selectAttributeOptions, 'type', 'USER_DEFINED');
                        _.set(selectAttributeOptions, 'value', selectConfig.select);
                    } else if (selectConfig.select === "*") {
                        _.set(selectAttributeOptions, 'type', 'ALL');
                        _.set(selectAttributeOptions, 'value', selectConfig.select);
                    } else {
                        console.log("Value other than \"USER_DEFINED\" and \"ALL\" received!");
                    }
                    var selectObject = new QuerySelect(selectAttributeOptions);
                    clickedElement.setSelect(selectObject);

                    if (selectConfig.groupBy !== undefined) {
                        var groupByAttributes = [];
                        _.forEach(selectConfig.groupBy, function (groupByAttribute) {
                            groupByAttributes.push(groupByAttribute.attribute);
                        });
                        clickedElement.setGroupBy(groupByAttributes);
                    } else {
                        clickedElement.setGroupBy(undefined);
                    }

                    if (selectConfig.postFilter !== undefined && selectConfig.postFilter.having !== undefined) {
                        clickedElement.setHaving(selectConfig.postFilter.having);
                    } else {
                        clickedElement.setHaving(undefined);
                    }

                    clickedElement.clearOrderByValueList();
                    if (outputConfig.orderBy !== undefined) {
                        _.forEach(outputConfig.orderBy, function (orderByValue) {
                            var orderByValueObjectOptions = {};
                            _.set(orderByValueObjectOptions, 'value', orderByValue.attribute);
                            _.set(orderByValueObjectOptions, 'order', (orderByValue.order).toUpperCase());
                            var orderByValueObject = new QueryOrderByValue(orderByValueObjectOptions);
                            clickedElement.addOrderByValue(orderByValueObject);
                        });
                    }

                    if (outputConfig.limit !== undefined && outputConfig.limit.limit !== undefined) {
                        clickedElement.setLimit(outputConfig.limit.limit);
                    } else {
                        clickedElement.setLimit(undefined);
                    }

                    if (outputConfig.outputRateLimit !== undefined
                        && outputConfig.outputRateLimit.outputRateLimit !== undefined) {
                        clickedElement.setOutputRateLimit(outputConfig.outputRateLimit.outputRateLimit);
                    } else {
                        clickedElement.setOutputRateLimit(undefined);
                    }

                    var queryOutput = clickedElement.getQueryOutput();
                    var outputObject;
                    var outputType;
                    var outputTarget;
                    if (outputConfig.output !== undefined) {
                        if (outputConfig.output.insertTarget !== undefined) {
                            outputType = "INSERT";
                            outputTarget = outputConfig.output.insertTarget;
                            outputObject = new QueryOutputInsert(outputConfig.output);
                        } else if (outputConfig.output.deleteTarget !== undefined) {
                            outputType = "DELETE";
                            outputTarget = outputConfig.output.deleteTarget;
                            outputObject = new QueryOutputDelete(outputConfig.output);
                        } else if (outputConfig.output.updateTarget !== undefined) {
                            outputType = "UPDATE";
                            outputTarget = outputConfig.output.updateTarget;
                            outputObject = new QueryOutputUpdate(outputConfig.output);
                        } else if (outputConfig.output.updateOrInsertIntoTarget !== undefined) {
                            outputType = "UPDATE_OR_INSERT_INTO";
                            outputTarget = outputConfig.output.updateOrInsertIntoTarget;
                            outputObject = new QueryOutputUpdateOrInsertInto(outputConfig.output);
                        } else {
                            console.log("Invalid output type for query received!")
                        }

                        if (outputConfig.output.eventType === undefined) {
                            outputObject.setEventType(undefined);
                        } else if(outputConfig.output.eventType === "all events"){
                            outputObject.setEventType('ALL_EVENTS');
                        } else if(outputConfig.output.eventType === "current events"){
                            outputObject.setEventType('CURRENT_EVENTS');
                        } else if(outputConfig.output.eventType === "expired events"){
                            outputObject.setEventType('EXPIRED_EVENTS');
                        }
                        queryOutput.setTarget(outputTarget);
                        queryOutput.setOutput(outputObject);
                        queryOutput.setType(outputType);
                    }

                    self.designViewContainer.removeClass('disableContainer');
                    self.toggleViewButton.removeClass('disableContainer');

                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                });

                // 'Cancel' button action
                var cancelButtonElement = $(formContainer).find('#form-cancel')[0];
                cancelButtonElement.addEventListener('click', function () {
                    self.designViewContainer.removeClass('disableContainer');
                    self.toggleViewButton.removeClass('disableContainer');
                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                });
            }
        };

        /**
         * @function generates the join source schema according to the source type for the join query. If the
         * savedJoinSourceData provided loads them as the starting values.
         * @param sourceType type of the source
         * @param sourceName name of the source
         * @param secondarySourceName other source name
         * @param sourceSide whether it is left or right source (ex: Right Source or Left Source)
         * @param savedJoinSourceData saved data related to join
         * @returns fullJoinSchema join source schema
         */
        JoinQueryForm.prototype.getJoinSourceSchema = function (sourceType, sourceName, secondarySourceName, sourceSide,
                                                                savedJoinSourceData) {
            var self = this;
            // starting values for the join source
            var fillSourceWith = {};
            if (savedJoinSourceData !== undefined) {
                fillSourceWith = {
                    as: {
                        name : savedJoinSourceData.getAs()
                    },
                    isUnidirectional: savedJoinSourceData.getIsUnidirectional()
                }
            }

            var sourcePropertyOrder;
            if (sourceSide === constants.LEFT_SOURCE) {
                sourcePropertyOrder = 1;
            } else if (sourceSide === constants.RIGHT_SOURCE) {
                sourcePropertyOrder = 3;
            } else {
                console.log("Unknown source side received!");
            }

            var commonJoinSourceSchema = {
                type: "object",
                propertyOrder: sourcePropertyOrder,
                title: sourceSide,
                required: true,
                options: {
                    disable_properties: false
                },
                properties: {
                    input: {
                        propertyOrder: 1,
                        type: "object",
                        title: "Input",
                        required: true,
                        properties: {
                            from: {
                                required: true,
                                title: "From",
                                type: "string",
                                enum: [sourceName, secondarySourceName],
                                default: sourceName
                            }
                        }
                    },
                    as: {
                        propertyOrder: 4,
                        type: "object",
                        title: "As",
                        properties: {
                            name: {
                                required: true,
                                title: "Name",
                                type: "string",
                                minLength: 1
                            }
                        }
                    },
                    isUnidirectional: {
                        propertyOrder: 5,
                        required: true,
                        type: "boolean",
                        format: "checkbox",
                        title: "Is Unidirectional"
                    }
                }
            };

            var streamHandlerListSchema;
            if (sourceType === "WINDOW") {
                streamHandlerListSchema = {
                    propertyOrder: 2,
                    type: "array",
                    format: "table",
                    title: "Stream Handlers",
                    minItems: 1,
                    items: {
                        type: "object",
                        title: 'Stream Handler',
                        properties: {
                            streamHandler: {
                                title: 'Stream Handler',
                                required: true,
                                oneOf: [
                                    {
                                        $ref: "#/definitions/filter",
                                        title: "Filter"
                                    },
                                    {
                                        $ref: "#/definitions/functionDef",
                                        title: "Function"
                                    }
                                ]
                            }
                        }
                    }
                };
            } else if (sourceType === "STREAM" || sourceType === "TRIGGER" ||sourceType === "TABLE"
                || sourceType === "AGGREGATION") {
                streamHandlerListSchema = {
                    propertyOrder: 2,
                    type: "array",
                    format: "table",
                    title: "Stream Handlers",
                    minItems: 1,
                    items: {
                        type: "object",
                        title: 'Stream Handler',
                        properties: {
                            streamHandler: {
                                title: 'Stream Handler',
                                required: true,
                                oneOf: [
                                    {
                                        $ref: "#/definitions/filter",
                                        title: "Filter"
                                    },
                                    {
                                        $ref: "#/definitions/functionDef",
                                        title: "Function"
                                    },
                                    {
                                        $ref: "#/definitions/window",
                                        title: "Window"
                                    }
                                ]
                            }
                        }
                    }
                };
            } else {
                console.log("Unknown source type received!");
            }
            var fullJoinSchema = commonJoinSourceSchema;
            _.set(fullJoinSchema.properties, 'streamHandlerList', streamHandlerListSchema);

            if (savedJoinSourceData !== undefined) {
                var savedStreamHandlerList = savedJoinSourceData.getStreamHandlerList();
                var streamHandlerList = [];
                _.forEach(savedStreamHandlerList, function (streamHandler) {
                    var streamHandlerObject;
                    var parameters = [];
                    if (streamHandler.getType() === "FILTER") {
                        streamHandlerObject = {
                            streamHandler: {
                                filter: streamHandler.getValue()
                            }
                        };
                    } else if (streamHandler.getType() === "FUNCTION") {
                        _.forEach(streamHandler.getValue().getParameters(), function (savedParameterValue) {
                            var parameterObject = {
                                parameter: savedParameterValue
                            };
                            parameters.push(parameterObject);
                        });
                        streamHandlerObject = {
                            streamHandler: {
                                functionName: streamHandler.getValue().getFunction(),
                                parameters: parameters
                            }
                        };
                    } else if (streamHandler.getType() === "WINDOW" && sourceType !== "WINDOW") {
                        _.forEach(streamHandler.getValue().getParameters(), function (savedParameterValue) {
                            var parameterObject = {
                                parameter: savedParameterValue
                            };
                            parameters.push(parameterObject);
                        });
                        streamHandlerObject = {
                            streamHandler: {
                                windowName: streamHandler.getValue().getFunction(),
                                parameters: parameters
                            }
                        };
                    }
                    streamHandlerList.push(streamHandlerObject);
                });

                _.set(fillSourceWith, 'streamHandlerList', streamHandlerList);
            }

            if (sourceSide === constants.LEFT_SOURCE) {
                self.leftSourceStartValues = self.formUtils.cleanJSONObject(fillSourceWith);
            } else if (sourceSide === constants.RIGHT_SOURCE) {
                self.rightSourceStartValues = self.formUtils.cleanJSONObject(fillSourceWith);
            }

            return fullJoinSchema;
        };

        return JoinQueryForm;
    });
