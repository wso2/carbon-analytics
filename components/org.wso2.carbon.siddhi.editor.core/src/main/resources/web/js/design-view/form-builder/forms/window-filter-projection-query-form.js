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
        'streamHandler', 'designViewUtils', 'jsonValidator'],
    function (require, log, $, _, QuerySelect, QueryOutputInsert, QueryOutputDelete, QueryOutputUpdate,
              QueryOutputUpdateOrInsertInto, QueryWindowOrFunction, QueryOrderByValue, StreamHandler, DesignViewUtils,
              JSONValidator) {

        var constants = {
            PROJECTION: 'projectionQueryDrop',
            FILTER: 'filterQueryDrop',
            WINDOW_QUERY: 'windowQueryDrop',
            FUNCTION_QUERY: 'functionQueryDrop'
        };

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
         * @function generate the form for the simple queries (projection, filter and window)
         * @param element selected element(query)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        WindowFilterProjectionQueryForm.prototype.generatePropertiesForm = function (element, formConsole,
                                                                                     formContainer) {
            var self = this;
            var propertyDiv = $('<div id="property-header"><h3>Query Configuration </h3></div>' +
                '<div class="define-windowFilterProjection-query"></div>');
            formContainer.append(propertyDiv);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var id = $(element).parent().attr('id');
            var clickedElement = self.configurationData.getSiddhiAppConfig().getWindowFilterProjectionQuery(id);
            if (!clickedElement.getQueryInput() || !clickedElement.getQueryInput().getFrom()) {
                DesignViewUtils.prototype.warnAlert('Connect an input element');
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            } else if (!clickedElement.getQueryOutput() || !clickedElement.getQueryOutput().getTarget()) {
                DesignViewUtils.prototype.warnAlert('Connect an output stream');
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            } else {

                var savedStreamHandlerList = clickedElement.getQueryInput().getStreamHandlerList();
                var streamHandlerList = [];
                var noOfSavedFilters = 0;
                var noOfSavedWindows = 0;
                var noOfSavedFunctions = 0;
                _.forEach(savedStreamHandlerList, function (streamHandler) {
                    var streamHandlerObject;
                    var parameters = [];
                    if (streamHandler.getType() === "FILTER") {
                        noOfSavedFilters++;
                        streamHandlerObject = {
                            streamHandler: {
                                filter: streamHandler.getValue()
                            }
                        };
                    } else if (streamHandler.getType() === "FUNCTION") {
                        noOfSavedFunctions++;
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
                    } else if (streamHandler.getType() === "WINDOW") {
                        noOfSavedWindows++;
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

                var savedAnnotations = clickedElement.getAnnotationList();
                var annotations = [];
                _.forEach(savedAnnotations, function (savedAnnotation) {
                    annotations.push({annotation: savedAnnotation});
                });

                var queryName = clickedElement.getQueryName();
                var inputElementName = clickedElement.getQueryInput().getFrom();
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

                var possibleGroupByAttributes = [];
                var inputElementType = undefined;
                var outputElementType = undefined;
                var outputElementAttributesList = [];

                var partitionId;
                var partitionElementWhereQueryIsSaved
                    = self.configurationData.getSiddhiAppConfig().getPartitionWhereQueryIsSaved(id);
                if (partitionElementWhereQueryIsSaved !== undefined) {
                    partitionId = partitionElementWhereQueryIsSaved.getId();
                }

                var inputElement =
                    self.configurationData.getSiddhiAppConfig()
                        .getDefinitionElementByName(inputElementName, partitionId);
                if (inputElement !== undefined) {
                    if (inputElement.type !== undefined
                        && (inputElement.type === 'STREAM' || inputElement.type === 'WINDOW')) {
                        inputElementType = inputElement.type;
                        if (inputElement.element !== undefined) {
                            _.forEach(inputElement.element.getAttributeList(), function (attribute) {
                                possibleGroupByAttributes.push(attribute.getName());
                            });
                        }
                    } else if (inputElement.type !== undefined && (inputElement.type === 'TRIGGER')) {
                        inputElementType = inputElement.type;
                        possibleGroupByAttributes.push('triggered_time');
                    }
                }

                var outputElement =
                    self.configurationData.getSiddhiAppConfig()
                        .getDefinitionElementByName(outputElementName, partitionId);
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
                if (!clickedElement.getSelect()) {
                    for (var i = 0; i < outputElementAttributesList.length; i++) {
                        var attr = {
                            expression: undefined,
                            as: outputElementAttributesList[i].getName()
                        };
                        select.push(attr);
                    }
                } else if (!clickedElement.getSelect().getValue()) {
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
                        if (!output.getEventType()) {
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


                /*
                * Test whether filter, function and window queries has their unique elements. For an example if a filter
                * is added the filter field should be activated. If a window is added window fields should be
                * activated. If a function query is added function related fields should be activated.
                * NOTE: this check is only essential when a form is opened for a query for the first time. After that
                * query type is changed according to the user input. So the required fields are already activated and
                * filled.
                * */
                if ($(element).parent().hasClass(constants.FILTER) && noOfSavedFilters === 0) {
                    var streamHandlerFilterObject = {
                        streamHandler: {
                            filter: ' '
                        }
                    };
                    streamHandlerList.push(streamHandlerFilterObject);

                } else if ($(element).parent().hasClass(constants.WINDOW_QUERY) && noOfSavedWindows === 0) {
                    var streamHandlerWindowObject = {
                        streamHandler: {
                            windowName: ' ',
                            parameters: [{parameter: ' '}]
                        }
                    };
                    streamHandlerList.push(streamHandlerWindowObject);
                } else if ($(element).parent().hasClass(constants.FUNCTION_QUERY) && noOfSavedFunctions === 0) {
                    var streamHandlerFunctionObject = {
                        streamHandler: {
                            functionName: ' ',
                            parameters: [{parameter: ' '}]
                        }
                    };
                    streamHandlerList.push(streamHandlerFunctionObject);

                }

                var savedQueryInput = {
                    input: {
                        from: clickedElement.getQueryInput().getFrom()
                    },
                    streamHandlerList: streamHandlerList
                };

                var inputElementAttributeList;
                var descriptionForFromElement = 'Attributes { ';
                if (inputElementType === 'STREAM' || inputElementType === 'WINDOW') {
                    inputElementAttributeList = (inputElement.element).getAttributeList();
                    _.forEach(inputElementAttributeList, function (attribute) {
                        descriptionForFromElement
                            = descriptionForFromElement + attribute.getName() + ' : ' + attribute.getType() + ', ';
                    });
                    descriptionForFromElement
                        = descriptionForFromElement.substring(0, descriptionForFromElement.length - 2);
                    descriptionForFromElement = descriptionForFromElement + ' }';
                } else if (inputElementType === 'TRIGGER') {
                    descriptionForFromElement = descriptionForFromElement + 'triggered_time : long }';
                }

                var fillQueryInputWith = self.formUtils.cleanJSONObject(savedQueryInput);

                var fillQueryAnnotation = {
                    annotations: annotations
                };
                fillQueryAnnotation = self.formUtils.cleanJSONObject(fillQueryAnnotation);
                var fillQuerySelectWith = {
                    select: select,
                    groupBy: groupBy,
                    postFilter: {
                        having: having
                    }
                };
                fillQuerySelectWith = self.formUtils.cleanJSONObject(fillQuerySelectWith);
                var fillQueryOutputWith = {
                    orderBy: orderBy,
                    limit: {
                        limit: limit
                    },
                    outputRateLimit: {
                        outputRateLimit: outputRateLimit
                    },
                    output: queryOutput
                };
                fillQueryOutputWith = self.formUtils.cleanJSONObject(fillQueryOutputWith);

                var inputSchema;
                if (inputElementType === 'WINDOW') {
                    inputSchema = {
                        type: "object",
                        title: "Input",
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
                                        title: "Window",
                                        type: "string",
                                        template: inputElementName,
                                        minLength: 1,
                                        description: descriptionForFromElement
                                    }
                                }
                            },
                            streamHandlerList: {
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
                                            required: true,
                                            title: 'Stream Handler1',
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
                            functionDef: {
                                title: "Function",
                                type: "object",
                                required: true,
                                options: {
                                    disable_properties: false
                                },
                                properties: {
                                    functionName: {
                                        required: true,
                                        title: "Function Name",
                                        type: "string",
                                        minLength: 1
                                    },
                                    parameters: {
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
                    }
                } else {
                    inputSchema = {
                        type: "object",
                        title: "Input",
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
                                        title: "Stream/Trigger",
                                        type: "string",
                                        template: inputElementName,
                                        minLength: 1,
                                        description: descriptionForFromElement
                                    }
                                }
                            },
                            streamHandlerList: {
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
                                options: {
                                    disable_properties: false
                                },
                                properties: {
                                    windowName: {
                                        required: true,
                                        title: "Window Name",
                                        type: "string",
                                        minLength: 1
                                    },
                                    parameters: {
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
                                options: {
                                    disable_properties: false
                                },
                                properties: {
                                    functionName: {
                                        required: true,
                                        title: "Function Name",
                                        type: "string",
                                        minLength: 1
                                    },
                                    parameters: {
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
                                default: 'current events'
                            }
                        }
                    };
                }

                formContainer.find('.define-windowFilterProjection-query')
                    .append('<div class="col-md-12 section-seperator frm-qry"><div class="col-md-4">' +
                    '<div class="row"><div id="form-query-name"></div>'+
                    '<div id="form-query-annotation" class="col-md-12 section-seperator"></div></div>' +
                    '<div class="row"><div id="form-query-input" class="col-md-12"></div></div></div>' +
                    '<div id="form-query-select" class="col-md-4"></div>' +
                    '<div id="form-query-output" class="col-md-4"></div></div>');

                var editorAnnotation = new JSONEditor($(formContainer).find('#form-query-annotation')[0], {
                    schema: {
                        type: "object",
                        title: "Annotations",
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

                var editorQueryName = new JSONEditor($(formContainer).find('#form-query-name')[0], {
                schema: {
                       required: true,
                       title: "Name",
                       type: "string",
                       default: "query"
                },
                startval: queryName,
                show_errors: "always"
                });

                var editorInput = new JSONEditor($(formContainer).find('#form-query-input')[0], {
                    schema: inputSchema,
                    startval: fillQueryInputWith,
                    show_errors: "always",
                    disable_properties: true,
                    display_required_only: true,
                    no_additional_properties: true,
                    disable_array_delete_all_rows: true,
                    disable_array_delete_last_row: true,
                    disable_array_reorder: false
                });
                var selectScheme = {
                    schema: {
                        required: true,
                        options: {
                            disable_properties: false
                        },
                        type: "object",
                        title: "Select",
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
                        title: "Output",
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

                formContainer.append(self.formUtils.buildFormButtons(true));

                // 'Submit' button action
                var submitButtonElement = $(formContainer).find('#btn-submit')[0];
                submitButtonElement.addEventListener('click', function () {

                    var annotationErrors = editorAnnotation.validate();
                    var inputErrors = editorInput.validate();
                    var selectErrors = editorSelect.validate();
                    var outputErrors = editorOutput.validate();
                    if (annotationErrors.length || inputErrors.length || selectErrors.length || outputErrors.length) {
                        return;
                    }

                    // set the isDesignViewContentChanged to true
                    self.configurationData.setIsDesignViewContentChanged(true);

                    var annotationConfig = editorAnnotation.getValue();
                    var queryNameConfig = editorQueryName.getValue();
                    var inputConfig = editorInput.getValue();
                    var selectConfig = editorSelect.getValue();
                    var outputConfig = editorOutput.getValue();

                    var numberOfWindows = 0;
                    var numberOfFilters = 0;
                    var numberOfFunctions = 0;

                    var isQueryNameUsed
                        = self.formUtils.isQueryDefinitionNameUsed(queryNameConfig, clickedElement.getId());
                    if (isQueryNameUsed) {
                           DesignViewUtils.prototype.errorAlert("Query name \"" + queryNameConfig + "\" is already"
                                                                                                +" defined.");
                        return;
                    }

                    clickedElement.getQueryInput().clearStreamHandlerList();
                    clickedElement.addQueryName(queryNameConfig);

                    _.forEach(inputConfig.streamHandlerList, function (streamHandler) {
                        streamHandler = streamHandler.streamHandler;
                        var streamHandlerOptions = {};
                        if (streamHandler.windowName !== undefined) {
                            numberOfWindows++;
                            var windowOptions = {};
                            _.set(windowOptions, 'function', streamHandler.windowName);
                            var parameters = [];
                            _.forEach(streamHandler.parameters, function (parameter) {
                                parameters.push(parameter.parameter);
                            });
                            _.set(windowOptions, 'parameters', parameters);
                            var queryWindow = new QueryWindowOrFunction(windowOptions);
                            _.set(streamHandlerOptions, 'type', 'WINDOW');
                            _.set(streamHandlerOptions, 'value', queryWindow);
                        } else if (streamHandler.functionName !== undefined) {
                            numberOfFunctions++;
                            var functionOptions = {};
                            _.set(functionOptions, 'function', streamHandler.functionName);
                            var parameters = [];
                            _.forEach(streamHandler.parameters, function (parameter) {
                                parameters.push(parameter.parameter);
                            });
                            _.set(functionOptions, 'parameters', parameters);
                            var queryFunction = new QueryWindowOrFunction(functionOptions);
                            _.set(streamHandlerOptions, 'type', 'FUNCTION');
                            _.set(streamHandlerOptions, 'value', queryFunction);
                        } else if (streamHandler.filter !== undefined) {
                            numberOfFilters++;
                            _.set(streamHandlerOptions, 'type', 'FILTER');
                            _.set(streamHandlerOptions, 'value', streamHandler.filter);
                        } else {
                            console.log("Unknown stream handler received!");
                        }
                        var streamHandlerObject = new StreamHandler(streamHandlerOptions);
                        clickedElement.getQueryInput().addStreamHandler(streamHandlerObject);
                    });

                    if (numberOfWindows > 1) {
                        DesignViewUtils.prototype.errorAlert('Only one window can be defined!');
                        return;
                    }
                    clickedElement.clearAnnotationList();
                    _.forEach(annotationConfig.annotations, function (annotation) {
                        clickedElement.addAnnotation(annotation.annotation);
                    });

                    var type;
                    // change the query icon depending on the fields filled
                    if (numberOfFunctions > 0) {
                        type = "FUNCTION";
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.FUNCTION_QUERY + ' jtk-draggable');
                    } else if (numberOfWindows === 1) {
                        type = "WINDOW";
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.WINDOW_QUERY + ' jtk-draggable');
                    } else if (numberOfFilters > 0) {
                        type = "FILTER";
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.FILTER + ' jtk-draggable');
                    } else {
                        type = "PROJECTION";
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.PROJECTION + ' jtk-draggable');
                    }

                    var queryInput = clickedElement.getQueryInput();
                    queryInput.setType(type);

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

                    // update name of the query related to the element if the name is changed
                    if (queryName !== queryNameConfig) {
                        // update selected query
                        clickedElement.addQueryName(queryNameConfig);
                        if (queryNameConfig == "") {
                             queryNameConfig = "Query";
                        }
                        var textNode = $('#' + clickedElement.getId()).find('.queryNameNode');
                        textNode.html(queryNameConfig);
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

                        if (!outputConfig.output.eventType) {
                            outputObject.setEventType(undefined);
                        } else if (outputConfig.output.eventType === "all events") {
                            outputObject.setEventType('ALL_EVENTS');
                        } else if (outputConfig.output.eventType === "current events") {
                            outputObject.setEventType('CURRENT_EVENTS');
                        } else if (outputConfig.output.eventType === "expired events") {
                            outputObject.setEventType('EXPIRED_EVENTS');
                        }
                        queryOutput.setTarget(outputTarget);
                        queryOutput.setOutput(outputObject);
                        queryOutput.setType(outputType);
                    }

                    // perform JSON validation
                    JSONValidator.prototype.validateWindowFilterProjectionQuery(clickedElement);

                    self.designViewContainer.removeClass('disableContainer');
                    self.toggleViewButton.removeClass('disableContainer');

                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                });

                // 'Cancel' button action
                var cancelButtonElement = $(formContainer).find('#btn-cancel')[0];
                cancelButtonElement.addEventListener('click', function () {
                    self.designViewContainer.removeClass('disableContainer');
                    self.toggleViewButton.removeClass('disableContainer');
                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                });
            }
        };

        return WindowFilterProjectionQueryForm;
    });
