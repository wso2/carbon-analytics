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
        'queryOutputUpdate', 'queryOutputUpdateOrInsertInto', 'queryOrderByValue',
        'patternOrSequenceQueryCondition', 'streamHandler', 'queryWindowOrFunction', 'designViewUtils'],
    function (require, log, $, _, QuerySelect, QueryOutputInsert, QueryOutputDelete, QueryOutputUpdate,
              QueryOutputUpdateOrInsertInto, QueryOrderByValue, PatternOrSequenceQueryCondition, StreamHandler,
              QueryWindowOrFunction, DesignViewUtils) {

        /**
         * @class PatternQueryForm Creates a forms to collect data from a pattern query
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var PatternQueryForm = function (options) {
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
         * @function generate the form for the pattern query
         * @param element selected element(query)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        PatternQueryForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var propertyDiv = $('<div id="property-header"><h3>Define Pattern Query </h3></div>' +
                '<div class="define-pattern-query"></div>');
            formContainer.append(propertyDiv);
            // design view container and toggle view button are disabled
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var id = $(element).parent().attr('id');
            var clickedElement = self.configurationData.getSiddhiAppConfig().getPatternQuery(id);
            if (!clickedElement.getQueryInput()
                || clickedElement.getQueryInput().getConnectedElementNameList().length === 0) {
                DesignViewUtils.prototype.warnAlert('Connect input streams');
                // design view container and toggle view button are enabled
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            } else if (!clickedElement.getQueryOutput() || !clickedElement.getQueryOutput().getTarget()) {
                DesignViewUtils.prototype.warnAlert('Connect an output element');
                // design view container and toggle view button are enabled
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            } else {

                var savedAnnotations = clickedElement.getAnnotationList();
                var annotations = [];
                _.forEach(savedAnnotations, function (savedAnnotation) {
                    annotations.push({annotation: savedAnnotation});
                });

                var inputStreamNames = clickedElement.getQueryInput().getConnectedElementNameList();
                var savedConditionList = clickedElement.getQueryInput().getConditionList();
                var logic = clickedElement.getQueryInput().getLogic();
                var savedGroupByAttributes = clickedElement.getGroupBy();
                var having = clickedElement.getHaving();
                var savedOrderByAttributes = clickedElement.getOrderBy();
                var limit = clickedElement.getLimit();
                var outputRateLimit = clickedElement.getOutputRateLimit();
                var outputElementName = clickedElement.getQueryOutput().getTarget();

                var conditionList = [];
                _.forEach(savedConditionList, function (savedCondition) {
                    var streamHandlerList = [];
                    _.forEach(savedCondition.getStreamHandlerList(), function (streamHandler) {
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
                        }
                        streamHandlerList.push(streamHandlerObject);
                    });

                    var conditionObject = {
                        conditionId: savedCondition.getConditionId(),
                        streamName: savedCondition.getStreamName(),
                        streamHandlerList: streamHandlerList
                    };
                    conditionList.push(conditionObject);
                });

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
                var outputElementType = undefined;
                var outputElementAttributesList = [];

                var partitionId;
                var partitionElementWhereQueryIsSaved
                    = self.configurationData.getSiddhiAppConfig().getPartitionWhereQueryIsSaved(id);
                if (partitionElementWhereQueryIsSaved !== undefined) {
                    partitionId = partitionElementWhereQueryIsSaved.getId();
                }

                _.forEach(inputStreamNames, function (inputStreamName) {
                    var inputElement =
                        self.configurationData.getSiddhiAppConfig()
                            .getDefinitionElementByName(inputStreamName, partitionId);
                    if (inputElement !== undefined) {
                        if (inputElement.type === 'TRIGGER') {
                            possibleGroupByAttributes.push(inputStreamName + '.triggered_time');
                        } else {
                            _.forEach(inputElement.element.getAttributeList(), function (attribute) {
                                possibleGroupByAttributes.push(inputStreamName + "." + attribute.getName());
                            });
                        }
                    }
                });

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
                } else if(!clickedElement.getSelect().getValue()) {
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

                var fillQueryAnnotation = {
                    annotations: annotations
                };
                fillQueryAnnotation = self.formUtils.cleanJSONObject(fillQueryAnnotation);
                var fillQueryInputWith = {
                    conditions: conditionList,
                    logic: {
                        statement: logic
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

                formContainer.append('<div class="col-md-12 section-seperator frm-qry"><div class="col-md-4">' +
                    '<div class="row"><div id="form-query-annotation" class="col-md-12 section-seperator"></div></div>' +
                    '<div class="row"><div id="form-query-input" class="col-md-12"></div></div></div>' +
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
                    schema: {
                        type: 'object',
                        title: 'Query Input',
                        properties: {
                            conditions: {
                                type: 'array',
                                title: 'Conditions',
                                format: 'tabs',
                                uniqueItems: true,
                                required: true,
                                minItems: 1,
                                propertyOrder: 1,
                                items: {
                                    type: 'object',
                                    options: {
                                        disable_properties: false
                                    },
                                    title: 'condition',
                                    headerTemplate: "Condition" + "{{i1}}",
                                    properties: {
                                        conditionId: {
                                            type: 'string',
                                            title: 'Condition ID',
                                            required: true,
                                            minLength: 1,
                                            propertyOrder: 1
                                        },
                                        streamName: {
                                            type: 'string',
                                            title: 'Stream',
                                            enum: inputStreamNames,
                                            required: true,
                                            propertyOrder: 2
                                        },
                                        streamHandlerList: {
                                            propertyOrder: 3,
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
                                                        title: 'Stream Handler',
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
                                    }
                                }
                            },
                            logic: {
                                type: 'object',
                                title: 'Logic',
                                required: true,
                                propertyOrder: 2,
                                properties: {
                                    statement: {
                                        type: 'string',
                                        title: 'Statement',
                                        minLength:1,
                                        required: true
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
                    },
                    startval: fillQueryInputWith,
                    show_errors: "always",
                    disable_properties: true,
                    display_required_only: true,
                    no_additional_properties: true,
                    disable_array_delete_all_rows: true,
                    disable_array_delete_last_row: true,
                    disable_array_reorder: true
                });

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

                formContainer.append(self.formUtils.buildFormButtons(true));

                // 'Submit' button action
                var submitButtonElement = $(formContainer).find('#btn-submit')[0];
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

                    clickedElement.clearAnnotationList();
                    _.forEach(annotationConfig.annotations, function (annotation) {
                        clickedElement.addAnnotation(annotation.annotation);
                    });

                    var queryInput = clickedElement.getQueryInput();

                    queryInput.clearConditionList();
                    _.forEach(inputConfig.conditions, function (condition) {
                        var conditionObjectOptions = {};
                        _.set(conditionObjectOptions, 'conditionId', condition.conditionId);
                        _.set(conditionObjectOptions, 'streamName', condition.streamName);

                        var streamHandlers = [];

                        _.forEach(condition.streamHandlerList, function (streamHandler) {
                            streamHandler = streamHandler.streamHandler;
                            var streamHandlerOptions = {};
                            if (streamHandler.functionName !== undefined) {
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
                                _.set(streamHandlerOptions, 'type', 'FILTER');
                                _.set(streamHandlerOptions, 'value', streamHandler.filter);
                            } else {
                                console.log("Unknown stream handler received!");
                            }
                            var streamHandlerObject = new StreamHandler(streamHandlerOptions);
                            streamHandlers.push(streamHandlerObject);
                        });
                        var conditionObject = new PatternOrSequenceQueryCondition(conditionObjectOptions);
                        conditionObject.setStreamHandlerList(streamHandlers);
                        queryInput.addCondition(conditionObject);
                    });

                    if (inputConfig.logic !== undefined && inputConfig.logic.statement !== undefined) {
                        queryInput.setLogic(inputConfig.logic.statement);
                    } else {
                        queryInput.setLogic(undefined);
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

                        if (!outputConfig.output.eventType) {
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
                var cancelButtonElement = $(formContainer).find('#btn-cancel')[0];
                cancelButtonElement.addEventListener('click', function () {
                    self.designViewContainer.removeClass('disableContainer');
                    self.toggleViewButton.removeClass('disableContainer');
                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                });
            }
        };

        return PatternQueryForm;
    });
