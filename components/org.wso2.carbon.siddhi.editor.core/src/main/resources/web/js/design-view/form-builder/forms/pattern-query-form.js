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

define(['require', 'log', 'jquery', 'lodash', 'querySelect', 'patternQueryInputCounting', 'patternQueryInputAndOr',
        'patternQueryInputNotFor', 'patternQueryInputNotAnd', 'queryOutputInsert', 'queryOutputDelete',
        'queryOutputUpdate', 'queryOutputUpdateOrInsertInto', 'queryWindow', 'queryOrderByValue'],
    function (require, log, $, _, Attribute, Stream, Table, Window, Trigger, Aggregation, AggregateByTimePeriod,
              QuerySelect, PatternQueryInputCounting, PatternQueryInputAndOr, PatternQueryInputNotFor,
              PatternQueryInputNotAnd, QueryOutputInsert, QueryOutputDelete, QueryOutputUpdate,
              QueryOutputUpdateOrInsertInto, QueryWindow, QueryOrderByValue) {

        /**
         * @class PatternQueryForm Creates a forms to collect data from a pattern query
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var PatternQueryForm = function (options) {
            this.configurationData = options.configurationData;
            this.application = options.application;
            this.consoleListManager = options.application.outputController;
            this.gridContainer = $("#grid-container");
            this.toolPaletteContainer = $("#tool-palette-container");
        };

        /**
         * @function generate the form for the pattern query
         * @param element selected element(query)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        PatternQueryForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            // The container and the tool palette are disabled to prevent the user from dropping any elements
            self.gridContainer.addClass('disabledbutton');
            self.toolPaletteContainer.addClass('disabledbutton');

            // The container and the tool palette are disabled to prevent the user from dropping any elements
            self.gridContainer.addClass('disabledbutton');
            self.toolPaletteContainer.addClass('disabledbutton');

            var id = $(element).parent().attr('id');
            var clickedElement = self.configurationData.getSiddhiAppConfig().getPatternQuery(id);
            if (clickedElement.getQueryInput() === ''
                || clickedElement.getQueryInput().getConnectedElementNameList().length === 0) {
                alert('Connect input streams');
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            } else if (clickedElement.getQueryOutput() === '' || clickedElement.getQueryOutput().getTarget() === '') {
                alert('Connect an output stream');
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            } else {

                var savedQueryInputEventList = clickedElement.getQueryInput().getEventList();
                var inputStreams = clickedElement.getQueryInput().getConnectedElementNameList();
                var queryInput1 = [];
                var queryInput2 = {};
                var inputEvent;
                _.forEach(savedQueryInputEventList, function (queryInputEvent) {
                    var inputEventType = queryInputEvent.getType();
                    if (inputEventType === "counting") {
                        inputEvent = {
                            inputType:"Counting Pattern Event",
                            forEvery: queryInputEvent.getForEvery(),
                            eventReference: queryInputEvent.getEventReference(),
                            streamName: queryInputEvent.getStreamName(),
                            filter: queryInputEvent.getFilter(),
                            minCount: queryInputEvent.getMinCount(),
                            maxCount: queryInputEvent.getMaxCount(),
                            within: queryInputEvent.getWithin()
                        };
                        queryInput1.push(inputEvent);
                    } else if (inputEventType === "andor") {
                        inputEvent = {
                            inputType:"And Or Event",
                            forEvery: queryInputEvent.getForEvery(),
                            leftEventReference:queryInputEvent.getLeftEventReference(),
                            leftStreamName: queryInputEvent.getLeftStreamName(),
                            leftFilter: queryInputEvent.getLeftFilter(),
                            connectedWith: (queryInputEvent.getConnectedWith() === '')?
                                'and' : queryInputEvent.getConnectedWith(),
                            rightEventReference: queryInputEvent.getRightEventReference(),
                            rightStreamName: queryInputEvent.getRightStreamName(),
                            rightFilter: queryInputEvent.getRightFilter(),
                            within: queryInputEvent.getWithin()
                        };
                        queryInput1.push(inputEvent);
                    } else if (inputEventType === "notfor") {
                        inputEvent = {
                            inputType:"Not For Event",
                            forEvery: queryInputEvent.getForEvery(),
                            streamName: queryInputEvent.getStreamName(),
                            filter: queryInputEvent.getFilter(),
                            forDuration: queryInputEvent.getForDuration()
                        };
                        queryInput2 = inputEvent;
                    } else if (inputEventType === "notand") {
                        inputEvent = {
                            inputType:"Not And Event",
                            forEvery: queryInputEvent.getForEvery(),
                            leftStreamName: queryInputEvent.getLeftStreamName(),
                            leftFilter: queryInputEvent.getLeftFilter(),
                            rightEventReference: queryInputEvent.getRightEventReference(),
                            rightStreamName: queryInputEvent.getRightStreamName(),
                            rightFilter: queryInputEvent.getRightFilter(),
                            within: queryInputEvent.getWithin()
                        };
                        queryInput2 = inputEvent;
                    }
                });

                var select = clickedElement.getSelect().getValue();
                var savedGroupByAttributes = clickedElement.getGroupBy();
                var having = clickedElement.getHaving();
                var savedOrderByAttributes = clickedElement.getOrderBy();
                var limit = clickedElement.getLimit();
                var outputRateLimit = clickedElement.getOutputRateLimit();

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
                        order: savedOrderByValue.getOrder()
                    };
                    orderBy.push(orderByValueObject);
                });

                var possibleGroupByAttributes = [];
                _.forEach(inputStreams, function (inputStreamName) {
                    _.forEach(self.configurationData.getSiddhiAppConfig().streamList, function (stream) {
                        if (stream.getName() === inputStreamName) {
                            _.forEach(stream.getAttributeList(), function (attribute) {
                                possibleGroupByAttributes.push(attribute.getName());
                            });
                        }
                    });
                });

                var savedQueryOutput = clickedElement.getQueryOutput();
                var savedQueryOutputTarget = savedQueryOutput.getTarget();
                var savedQueryOutputType = savedQueryOutput.getType();
                var output = savedQueryOutput.getOutput();
                var queryOutput;
                if (savedQueryOutputTarget !== undefined && savedQueryOutputType!== undefined && output!== undefined) {
                    if (savedQueryOutputType === "insert") {
                        queryOutput = {
                            output : {
                                outputType: "Insert",
                                insertTarget: savedQueryOutputTarget,
                                eventType: (output.getEventType() === '')?'all':output.getEventType()
                            }
                        };
                    } else if (savedQueryOutputType === "delete") {
                        queryOutput = {
                            output : {
                                outputType : "Delete",
                                deleteTarget: savedQueryOutputTarget,
                                forEventType : (output.getForEventType() === '')?'all':output.getForEventType(),
                                on : output.getOn()
                            }
                        };

                    } else if (savedQueryOutputType === "update") {
                        queryOutput = {
                            output : {
                                outputType : "Update",
                                updateTarget: savedQueryOutputTarget,
                                forEventType : (output.getForEventType() === '')?'all':output.getForEventType(),
                                set: output.getSet(),
                                on : output.getOn()
                            }
                        };
                    } else if (savedQueryOutputType === "update_or_insert_into") {
                        queryOutput = {
                            output: {
                                outputType: "Update or Insert Into",
                                updateOrInsertIntoTarget: savedQueryOutputTarget,
                                forEventType: (output.getForEventType() === '')?'all':output.getForEventType(),
                                set: output.getSet(),
                                on: output.getOn()
                            }
                        };
                    }
                }

                var fillWith = {
                    queryInput : {
                        queryInput1: queryInput1,
                        queryInput2: queryInput2
                    },
                    querySelect : {
                        select : select
                    },
                    groupBy : groupBy,
                    having : having,
                    orderBy : orderBy,
                    limit : limit,
                    outputRateLimit : outputRateLimit,
                    queryOutput: queryOutput
                };

                //TODO: check whether the left and right stream both are same in some occasions
                var editor = new JSONEditor(formContainer[0], {
                    schema: {
                        type: "object",
                        title: "Pattern Query",
                        properties: {
                            queryInput: {
                                propertyOrder: 1,
                                type: "object",
                                title: "Query Input",
                                required: true,
                                properties: {
                                    queryInput1: {
                                        title: "Query Input Type - Normal Types",
                                        type: "array",
                                        format: "array",
                                        items: {
                                            title: "Normal Query Input Type",
                                            oneOf: [
                                                {
                                                    $ref: "#/definitions/queryInputCountingType",
                                                    title: "Counting Pattern Type"
                                                },
                                                {
                                                    $ref: "#/definitions/queryInputAndOrType",
                                                    title: "Logical Pattern(And Or) Type"
                                                }
                                            ]
                                        }
                                    },
                                    queryInput2: {
                                        type: "object",
                                        title: "Query Input Type - Negation Events",
                                        oneOf: [
                                            {
                                                $ref: "#/definitions/queryInputNotAndType",
                                                title: "Logical Pattern(Not And) Type"
                                            },
                                            {
                                                $ref: "#/definitions/queryInputNotForType",
                                                title: "Logical Pattern(Not For) Type"
                                            }
                                        ]
                                    }
                                }
                            },
                            querySelect: {
                                propertyOrder: 3,
                                required: true,
                                type: "object",
                                title: "Query Select",
                                properties: {
                                    select: {
                                        title: "Query Select Type",
                                        required: true,
                                        oneOf: [
                                            {
                                                $ref: "#/definitions/querySelectUserDefined",
                                                title: "User-Defined"
                                            },
                                            {
                                                $ref: "#/definitions/querySelectAll",
                                                title: "All"
                                            }
                                        ]
                                    }
                                }
                            },
                            groupBy: {
                                propertyOrder: 4,
                                type: "array",
                                format: "table",
                                title: "Group By Attributes",
                                uniqueItems: true,
                                items: {
                                    type: "object",
                                    title : 'Attribute',
                                    properties: {
                                        attribute: {
                                            type: 'string',
                                            title: 'Attribute Name',
                                            enum: possibleGroupByAttributes,
                                            default: ""
                                        }
                                    }
                                }
                            },
                            having: {
                                propertyOrder: 5,
                                title: "Having",
                                type: "string",
                                minLength: 1
                            },
                            orderBy: {
                                propertyOrder: 6,
                                type: "array",
                                format: "table",
                                title: "Order By Attributes",
                                uniqueItems: true,
                                items: {
                                    type: "object",
                                    title : 'Attribute',
                                    properties: {
                                        attribute: {
                                            required: true,
                                            type: 'string',
                                            title: 'Attribute Name',
                                            enum: possibleGroupByAttributes,
                                            default: ""
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
                                propertyOrder: 7,
                                title: "Limit",
                                type: "number",
                                minLength: 1
                            },
                            outputRateLimit: {
                                propertyOrder: 8,
                                title: "Output Rate Limit",
                                type: "string",
                                minLength: 1
                            },
                            queryOutput: {
                                propertyOrder: 9,
                                required: true,
                                type: "object",
                                title: "Query Output Type",
                                properties: {
                                    output: {
                                        title: "Query Output Type",
                                        required: true,
                                        oneOf: [
                                            {
                                                $ref: "#/definitions/queryOutputInsertType",
                                                title: "Insert Type"
                                            },
                                            {
                                                $ref: "#/definitions/queryOutputDeleteType",
                                                title: "Delete Type"
                                            },
                                            {
                                                $ref: "#/definitions/queryOutputUpdateType",
                                                title: "Update Type"
                                            },
                                            {
                                                $ref: "#/definitions/queryOutputUpdateOrInsertIntoType",
                                                title: "Update Or Insert Into Type"
                                            }
                                        ]
                                    }
                                }
                            }
                        },
                        definitions: {
                            queryInputCountingType: {
                                title: "Counting Pattern Event",
                                type: "object",
                                properties: {
                                    inputType: {
                                        required: true,
                                        title: "Input Type",
                                        type: "string",
                                        template: "Counting Pattern Event"
                                    },
                                    forEvery: {
                                        required: true,
                                        title: "For Every",
                                        type: "boolean",
                                        enum: [true, false],
                                        default: false
                                    },
                                    eventReference: {
                                        title: "Event Reference",
                                        type: "string",
                                        minLength: 1
                                    },
                                    streamName: {
                                        type: 'string',
                                        title: 'Stream Name',
                                        enum: inputStreams,
                                        required: true
                                    },
                                    filter: {
                                        title: "Filter",
                                        type: "string",
                                        minLength: 1
                                    },
                                    minCount: {
                                        title: "Min Count",
                                        type: "string",
                                        minLength: 1
                                    },
                                    maxCount: {
                                        title: "Max Count",
                                        type: "string",
                                        minLength: 1
                                    },
                                    within: {
                                        title: "Within",
                                        type: "string",
                                        minLength: 1
                                    }
                                }

                            },
                            queryInputAndOrType: {
                                title: "And Or Event",
                                type: "object",
                                properties: {
                                    inputType: {
                                        required: true,
                                        title: "Input Type",
                                        type: "string",
                                        template: "Logical And Or Event"
                                    },
                                    forEvery: {
                                        required: true,
                                        title: "For Every",
                                        type: "boolean",
                                        enum: [true, false],
                                        default: false
                                    },
                                    leftEventReference: {
                                        title: "Left Stream Event Reference",
                                        type: "string",
                                        minLength: 1
                                    },
                                    leftStreamName: {
                                        type: 'string',
                                        title: 'Left Stream Name',
                                        enum: inputStreams,
                                        required: true
                                    },
                                    leftFilter: {
                                        title: "Left Stream Filter",
                                        type: "string",
                                        minLength: 1
                                    },
                                    connectedWith: {
                                        title: "Connected With",
                                        type: "string",
                                        enum: ['and', 'or'],
                                        default: 'and'
                                    },
                                    rightEventReference: {
                                        title: "Right Stream Event Reference",
                                        type: "string",
                                        minLength: 1
                                    },
                                    rightStreamName: {
                                        type: 'string',
                                        title: 'Right Stream Name',
                                        enum: inputStreams,
                                        required: true
                                    },
                                    rightFilter: {
                                        title: "Right Stream Filter",
                                        type: "string",
                                        minLength: 1
                                    },
                                    within: {
                                        title: "Within",
                                        type: "string",
                                        minLength: 1
                                    }
                                }
                            },
                            queryInputNotForType: {
                                title: "Not For Event",
                                type: "object",
                                properties: {
                                    inputType: {
                                        required: true,
                                        title: "Input Type",
                                        type: "string",
                                        template: "Logical Not For Event"
                                    },
                                    forEvery: {
                                        required: true,
                                        title: "For Every",
                                        type: "boolean",
                                        enum: [true, false],
                                        default: false
                                    },
                                    streamName: {
                                        type: 'string',
                                        title: 'Stream Name',
                                        enum: inputStreams,
                                        required: true
                                    },
                                    filter: {
                                        title: "Filter",
                                        type: "string",
                                        minLength: 1
                                    },
                                    forDuration: {
                                        required: true,
                                        title: "Duration",
                                        type: "string",
                                        minLength: 1
                                    }
                                }
                            },
                            queryInputNotAndType: {
                                title: "Not And Event",
                                type: "object",
                                properties: {
                                    inputType: {
                                        required: true,
                                        title: "Input Type",
                                        type: "string",
                                        template: "Logical Not And Event"
                                    },
                                    forEvery: {
                                        required: true,
                                        title: "For Every",
                                        type: "boolean",
                                        enum: [true, false],
                                        default: false
                                    },
                                    leftStreamName: {
                                        type: 'string',
                                        title: 'Left Stream Name',
                                        enum: inputStreams,
                                        required: true
                                    },
                                    leftFilter: {
                                        title: "Left Stream Filter",
                                        type: "string",
                                        minLength: 1
                                    },
                                    rightEventReference: {
                                        title: "Right Stream Event Reference",
                                        type: "string",
                                        minLength: 1
                                    },
                                    rightStreamName: {
                                        type: 'string',
                                        title: 'Right Stream Name',
                                        enum: inputStreams,
                                        required: true
                                    },
                                    rightFilter: {
                                        title: "Right Stream Filter",
                                        type: "string",
                                        minLength: 1
                                    },
                                    within: {
                                        title: "Within",
                                        type: "string",
                                        minLength: 1
                                    }
                                }
                            },
                            querySelectUserDefined: {
                                required: true,
                                type: "array",
                                format: "table",
                                title: "User Defined",
                                uniqueItems: true,
                                minItems: 1,
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
                                title: "All",
                                template: '*'
                            },
                            queryOutputInsertType: {
                                required: true,
                                title: "Insert",
                                type: "object",
                                properties: {
                                    outputType: {
                                        required: true,
                                        title: "Output Type",
                                        type: "string",
                                        template: "Insert"
                                    },
                                    eventType: {
                                        required: true,
                                        title: "For Every",
                                        type: "string",
                                        enum: ['current', 'expired', 'all'],
                                        default: 'all'
                                    },
                                    insertTarget: {
                                        type: 'string',
                                        title: 'Insert Into',
                                        template: savedQueryOutputTarget,
                                        required: true
                                    }
                                }
                            },
                            queryOutputDeleteType: {
                                required: true,
                                title: "Delete",
                                type: "object",
                                properties: {
                                    outputType: {
                                        required: true,
                                        title: "Output Type",
                                        type: "string",
                                        template: "Delete"
                                    },
                                    deleteTarget: {
                                        type: 'string',
                                        title: 'Delete From',
                                        template: savedQueryOutputTarget,
                                        required: true
                                    },
                                    forEventType: {
                                        title: "For Every",
                                        type: "string",
                                        enum: ['current', 'expired', 'all'],
                                        default: 'all'
                                    },
                                    on: {
                                        type: 'string',
                                        title: 'On',
                                        minLength: 1,
                                        required: true
                                    }
                                }
                            },
                            queryOutputUpdateType: {
                                required: true,
                                title: "Update",
                                type: "object",
                                properties: {
                                    outputType: {
                                        required: true,
                                        title: "Output Type",
                                        type: "string",
                                        template: "Update"
                                    },
                                    updateTarget: {
                                        type: 'string',
                                        title: 'Update From',
                                        template: savedQueryOutputTarget,
                                        required: true
                                    },
                                    forEventType: {
                                        title: "For Every",
                                        type: "string",
                                        enum: ['current', 'expired', 'all'],
                                        default: 'all'
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
                                        title: 'On',
                                        minLength: 1,
                                        required: true
                                    }
                                }
                            },
                            queryOutputUpdateOrInsertIntoType: {
                                required: true,
                                title: "Update or Insert Into",
                                type: "object",
                                properties: {
                                    outputType: {
                                        required: true,
                                        title: "Output Type",
                                        type: "string",
                                        template: "Update or Insert Into"
                                    },
                                    updateOrInsertIntoTarget: {
                                        type: 'string',
                                        title: 'Update or Insert Into',
                                        template: savedQueryOutputTarget,
                                        required: true
                                    },
                                    forEventType: {
                                        title: "For Every",
                                        type: "string",
                                        enum: ['current', 'expired', 'all'],
                                        default: 'all'
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
                                        title: 'On',
                                        minLength: 1,
                                        required: true
                                    }
                                }

                            }

                        }
                    },
                    show_errors: "always",
                    startval: fillWith,
                    disable_properties: false,
                    display_required_only: true,
                    no_additional_properties: true,
                    disable_array_delete_all_rows: true,
                    disable_array_delete_last_row: true,
                    disable_array_reorder: true
                });
                $(formContainer).append('<div id="form-submit"><button type="button" ' +
                    'class="btn btn-default">Submit</button></div>' +
                    '<div id="form-cancel"><button type="button" class="btn btn-default">Cancel</button></div>');

                // 'Submit' button action
                var submitButtonElement = $('#form-submit')[0];
                submitButtonElement.addEventListener('click', function () {
                    self.gridContainer.removeClass('disabledbutton');
                    self.toolPaletteContainer.removeClass('disabledbutton');

                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);
                    self.consoleListManager.hideAllConsoles();

                    var config = editor.getValue();
                    var patternQueryInput = clickedElement.getQueryInput();
                    var newEventObject;
                    patternQueryInput.getEventList().removeAllElements();
                    if (config.queryInput.queryInput1 !== undefined) {
                        _.forEach(config.queryInput.queryInput1, function (queryInputEvent) {
                            if (queryInputEvent.inputType === "Counting Pattern Event") {
                                newEventObject = new PatternQueryInputCounting(queryInputEvent);
                                if (queryInputEvent.eventReference === undefined) {
                                    newEventObject.setEventReference('');
                                }
                                if (queryInputEvent.filter === undefined) {
                                    newEventObject.setFilter('');
                                }
                                if (queryInputEvent.minCount === undefined) {
                                    newEventObject.setMinCount('');
                                }
                                if (queryInputEvent.maxCount === undefined) {
                                    newEventObject.setMaxCount('');
                                }
                                if (queryInputEvent.within === undefined) {
                                    newEventObject.setWithin('');
                                }
                            } else if (queryInputEvent.inputType === "Logical And Or Event") {
                                newEventObject = new PatternQueryInputAndOr(queryInputEvent);
                                if (queryInputEvent.leftEventReference === undefined) {
                                    newEventObject.setLeftEventReference('');
                                }
                                if (queryInputEvent.leftFilter === undefined) {
                                    newEventObject.setLeftFilter('');
                                }
                                if (queryInputEvent.rightEventReference === undefined) {
                                    newEventObject.setRightEventReference('');
                                }
                                if (queryInputEvent.rightFilter === undefined) {
                                    newEventObject.setRightFilter('');
                                }
                                if (queryInputEvent.within === undefined) {
                                    newEventObject.setWithin('');
                                }
                            } else {
                                console.log("Invalid input event type for pattern query received!")
                            }
                            patternQueryInput.addEvent(newEventObject);
                        });
                    }

                    if (config.queryInput.queryInput2 !== undefined
                        && config.queryInput.queryInput2.inputType !== undefined) {
                        if (config.queryInput.queryInput2.inputType === "Logical Not For Event") {
                            newEventObject = new PatternQueryInputNotFor(config.queryInput.queryInput2);
                            if (config.queryInput.queryInput2.filter === undefined) {
                                newEventObject.setFilter('');
                            }
                        } else if (config.queryInput.queryInput2.inputType === "Logical Not And Event") {
                            newEventObject = new PatternQueryInputNotAnd(config.queryInput.queryInput2);
                            if (config.queryInput.queryInput2.leftFilter === undefined) {
                                newEventObject.setLeftFilter('');
                            }
                            if (config.queryInput.queryInput2.rightEventReference === undefined) {
                                newEventObject.setRightEventReference('');
                            }
                            if (config.queryInput.queryInput2.rightFilter === undefined) {
                                newEventObject.setRightFilter('');
                            }
                            if (config.queryInput.queryInput2.within === undefined) {
                                newEventObject.setWithin('');
                            }
                        } else {
                            console.log("Invalid input event type for pattern query received!")
                        }
                        patternQueryInput.addEvent(newEventObject);
                    }

                    var selectAttributeOptions = {};
                    if (config.querySelect.select instanceof Array) {
                        _.set(selectAttributeOptions, 'type', 'user_defined');
                        _.set(selectAttributeOptions, 'value', editor.getValue().querySelect.select);
                    } else if (config.querySelect.select === "*") {
                        _.set(selectAttributeOptions, 'type', 'all');
                        _.set(selectAttributeOptions, 'value', editor.getValue().querySelect.select);
                    } else {
                        console.log("Value other than \"user_defined\" and \"all\" received!");
                    }
                    var selectObject = new QuerySelect(selectAttributeOptions);
                    clickedElement.setSelect(selectObject);

                    if (config.groupBy !== undefined) {
                        var groupByAttributes = [];
                        _.forEach(config.groupBy, function (groupByAttribute) {
                            groupByAttributes.push(groupByAttribute.attribute);
                        });
                        clickedElement.setGroupBy(groupByAttributes);
                    } else {
                        clickedElement.setGroupBy('');
                    }

                    if (config.having !== undefined) {
                        clickedElement.setHaving(config.having);
                    } else {
                        clickedElement.setHaving('');
                    }

                    clickedElement.getOrderBy().removeAllElements();
                    if (config.orderBy !== undefined) {
                        _.forEach(config.orderBy, function (orderByValue) {
                            var orderByValueObjectOptions = {};
                            _.set(orderByValueObjectOptions, 'value', orderByValue.attribute);
                            _.set(orderByValueObjectOptions, 'order', orderByValue.order);
                            var orderByValueObject = new QueryOrderByValue(orderByValueObjectOptions);
                            clickedElement.addOrderByValue(orderByValueObject);
                        });
                    }

                    if (config.limit !== undefined) {
                        clickedElement.setLimit(config.limit);
                    } else {
                        clickedElement.setLimit('');
                    }

                    if (config.outputRateLimit !== undefined) {
                        clickedElement.setOutputRateLimit(config.outputRateLimit);
                    } else {
                        clickedElement.setOutputRateLimit('');
                    }

                    var patternQueryOutput = clickedElement.getQueryOutput();
                    var outputObject;
                    var outputType;
                    if (config.queryOutput.output !== undefined) {
                        if (config.queryOutput.output.outputType === "Insert") {
                            outputType = "insert";
                            outputObject = new QueryOutputInsert(config.queryOutput.output);
                            if (config.queryOutput.output.eventType === undefined) {
                                outputObject.setEventType('');
                            }
                        } else if (config.queryOutput.output.outputType === "Delete") {
                            outputType = "delete";
                            outputObject = new QueryOutputDelete(config.queryOutput.output);
                            if (config.queryOutput.output.forEventType === undefined) {
                                outputObject.setForEventType('');
                            }
                        } else if (config.queryOutput.output.outputType === "Update") {
                            outputType = "update";
                            outputObject = new QueryOutputUpdate(config.queryOutput.output);
                            if (config.queryOutput.output.forEventType === undefined) {
                                outputObject.setForEventType('');
                            }
                        } else if (config.queryOutput.output.outputType === "Update or Insert Into") {
                            outputType = "update_or_insert_into";
                            outputObject = new QueryOutputUpdateOrInsertInto(config.queryOutput.output);
                            if (config.queryOutput.output.forEventType === undefined) {
                                outputObject.setForEventType('');
                            }
                        } else {
                            console.log("Invalid output type for pattern query received!")
                        }
                        patternQueryOutput.setOutput(outputObject);
                        patternQueryOutput.setType(outputType);
                    }
                });

                // 'Cancel' button action
                var cancelButtonElement = $('#form-cancel')[0];
                cancelButtonElement.addEventListener('click', function () {
                    self.gridContainer.removeClass('disabledbutton');
                    self.toolPaletteContainer.removeClass('disabledbutton');
                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);
                    self.consoleListManager.hideAllConsoles();
                });
            }
        };

        return PatternQueryForm;
    });
