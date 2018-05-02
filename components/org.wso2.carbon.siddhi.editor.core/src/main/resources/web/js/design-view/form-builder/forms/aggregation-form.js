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

define(['require', 'log', 'jquery', 'lodash', 'attribute', 'aggregation', 'aggregateByTimePeriod', 'querySelect'],
    function (require, log, $, _, Attribute, Aggregation, AggregateByTimePeriod, QuerySelect) {

        /**
         * @class AggregationForm Creates a forms to collect data from a aggregation
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var AggregationForm = function (options) {
            this.configurationData = options.configurationData;
            this.application = options.application;
            this.formUtils = options.formUtils;
            this.consoleListManager = options.application.outputController;
            this.gridContainer = $("#grid-container");
            this.toolPaletteContainer = $("#tool-palette-container");
        };

        /**
         * @function generate form when defining a form
         * @param i id for the element
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        AggregationForm.prototype.generateDefineForm = function (i, formConsole, formContainer) {
            var self = this;
            var propertyDiv = $('<div id="property-header"><h3>Define Aggregation </h3></div>' +
                '<div id="define-aggregation" class="define-aggregation"></div>');
            formContainer.append(propertyDiv);
            var aggregationElement = $("#define-aggregation")[0];

            // generate the form to define a aggregation
            var editor = new JSONEditor(aggregationElement, {
                schema: {
                    type: "object",
                    title: "Aggregation",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 1
                        },
                        from: {
                            type: "string",
                            title: "From",
                            minLength: 1,
                            required: true,
                            propertyOrder: 2
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
                                            $ref: "#/definitions/userDefined",
                                            title: "User-Defined"
                                        },
                                        {
                                            $ref: "#/definitions/all",
                                            title: "All"
                                        }
                                    ]
                                }
                            }
                        },
                        groupBy: {
                            required: true,
                            propertyOrder: 5,
                            type: "array",
                            format: "table",
                            title: "Group By Attributes",
                            uniqueItems: true,
                            items: {
                                type: "object",
                                title : 'Attribute',
                                properties: {
                                    attribute: {
                                        type: "string"
                                    }
                                }
                            }
                        },
                        aggregateByAttribute: {
                            required: true,
                            type: "string",
                            title: "Aggregate by Attribute",
                            minLength: 1,
                            propertyOrder: 6
                        },
                        aggregateByTimePeriod: {
                            type: "object",
                            title: "Aggregate by Time Period",
                            required: true,
                            propertyOrder: 7,
                            properties: {
                                minValue: {
                                    type: "string",
                                    title: "Starting Time Value",
                                    required: true,
                                    enum: [
                                        "seconds",
                                        "minutes",
                                        "hours",
                                        "days",
                                        "weeks",
                                        "months",
                                        "years"
                                    ],
                                    default: "seconds"
                                },
                                maxValue: {
                                    type: "string",
                                    title: "Ending Time Value",
                                    required: true,
                                    enum: [
                                        "",
                                        "seconds",
                                        "minutes",
                                        "hours",
                                        "days",
                                        "weeks",
                                        "months",
                                        "years"
                                    ],
                                    default: ""
                                }
                            }

                        }
                    },
                    definitions: {
                        userDefined: {
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
                        all: {
                            type: "string",
                            title: "All",
                            template: '*'
                        }
                    }
                },
                show_errors: "always",
                disable_properties: false,
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                display_required_only: true,
                no_additional_properties: true
            });

            formContainer.append('<div id="submit"><button type="button" class="btn btn-default">Submit</button></div>');

            // 'Submit' button action
            var submitButtonElement = $('#submit')[0];
            submitButtonElement.addEventListener('click', function () {
                var isAggregationNameUsed = self.formUtils.IsDefinitionElementNameUnique(editor.getValue().name);
                if(isAggregationNameUsed) {
                    alert("Aggregation name \"" + editor.getValue().name + "\" is already used.");
                    return;
                }
                // add the new aggregation to the aggregation array
                var aggregationOptions = {};
                _.set(aggregationOptions, 'id', i);
                _.set(aggregationOptions, 'name', editor.getValue().name);
                _.set(aggregationOptions, 'from', editor.getValue().from);
                _.set(aggregationOptions, 'aggregateByAttribute', editor.getValue().aggregateByAttribute);

                var selectAttributeOptions = {};
                if (editor.getValue().querySelect.select instanceof Array) {
                    _.set(selectAttributeOptions, 'type', 'user_defined');
                    _.set(selectAttributeOptions, 'value', editor.getValue().querySelect.select);
                } else if (editor.getValue().querySelect.select === "*") {
                    _.set(selectAttributeOptions, 'type', 'all');
                    _.set(selectAttributeOptions, 'value', editor.getValue().querySelect.select);
                } else {
                    console.log("Value other than \"user_defined\" and \"all\" received!");
                }
                var selectObject = new QuerySelect(selectAttributeOptions);
                _.set(aggregationOptions, 'select', selectObject);

                if (editor.getValue().groupBy !== undefined) {
                    var groupByAttributes = [];
                    _.forEach(editor.getValue().groupBy, function (groupByAttribute) {
                        groupByAttributes.push(groupByAttribute.attribute);
                    });
                    _.set(aggregationOptions, 'groupBy', groupByAttributes);
                } else {
                    _.set(aggregationOptions, 'groupBy', '');
                }

                var aggregateByTimePeriod = new AggregateByTimePeriod(editor.getValue().aggregateByTimePeriod);
                if (editor.getValue().aggregateByTimePeriod === undefined) {
                    aggregateByTimePeriod.setMaxValue('');
                }
                _.set(aggregationOptions, 'aggregateByTimePeriod', aggregateByTimePeriod);

                var aggregation = new Aggregation(aggregationOptions);
                self.configurationData.getSiddhiAppConfig().addAggregation(aggregation);

                var textNode = $('#'+i).find('.aggregationNameNode');
                textNode.html(editor.getValue().name);

                // close the form aggregation
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();

                self.gridContainer.removeClass("disabledbutton");
                self.toolPaletteContainer.removeClass("disabledbutton");

            });
            return editor.getValue().name;
        };

        /**
         * @function generate properties form for a aggregation
         * @param element selected element(aggregation)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        AggregationForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            // The container and the tool palette are disabled to prevent the user from dropping any elements
            self.gridContainer.addClass("disabledbutton");
            self.toolPaletteContainer.addClass("disabledbutton");

            var id = $(element).parent().attr('id');
            // retrieve the aggregation information from the collection
            var clickedElement = self.configurationData.getSiddhiAppConfig().getAggregation(id);
            if(clickedElement === undefined) {
                var errorMessage = 'unable to find clicked element';
                log.error(errorMessage);
            }
            var name = clickedElement.getName();
            var from = clickedElement.getFrom();
            var select = clickedElement.getSelect().getValue();
            var savedGroupByAttributes = clickedElement.getGroupBy();
            var aggregateByAttribute = clickedElement.getAggregateByAttribute();
            var aggregateByTimePeriod = clickedElement.getAggregateByTimePeriod();

            var groupBy = [];
            _.forEach(savedGroupByAttributes, function (savedGroupByAttribute) {
                var groupByAttributeObject = {
                    attribute: savedGroupByAttribute
                };
                groupBy.push(groupByAttributeObject);
            });

            var fillWith = {
                name : name,
                from : from,
                querySelect : {
                    select: select
                },
                groupBy : groupBy,
                aggregateByAttribute : aggregateByAttribute,
                aggregateByTimePeriod : aggregateByTimePeriod
            };
            // generate the form to define a aggregation
            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Aggregation",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 1
                        },
                        from: {
                            type: "string",
                            title: "From",
                            minLength: 1,
                            required: true,
                            propertyOrder: 2
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
                                            $ref: "#/definitions/userDefined",
                                            title: "User-Defined"
                                        },
                                        {
                                            $ref: "#/definitions/all",
                                            title: "All"
                                        }
                                    ]
                                }
                            }
                        },
                        groupBy: {
                            required: true,
                            propertyOrder: 5,
                            type: "array",
                            format: "table",
                            title: "Group By Attributes",
                            uniqueItems: true,
                            items: {
                                type: "object",
                                title : 'Attribute',
                                properties: {
                                    attribute: {
                                        type: "string"
                                    }
                                }
                            }
                        },
                        aggregateByAttribute: {
                            required: true,
                            type: "string",
                            title: "Aggregate by Attribute",
                            minLength: 1,
                            propertyOrder: 6
                        },
                        aggregateByTimePeriod: {
                            type: "object",
                            title: "Aggregate by Time Period",
                            required: true,
                            propertyOrder: 7,
                            properties: {
                                minValue: {
                                    type: "string",
                                    title: "Starting Time Value",
                                    required: true,
                                    enum: [
                                        "seconds",
                                        "minutes",
                                        "hours",
                                        "days",
                                        "weeks",
                                        "months",
                                        "years"
                                    ],
                                    default: "seconds"
                                },
                                maxValue: {
                                    type: "string",
                                    title: "Ending Time Value",
                                    required: true,
                                    enum: [
                                        "",
                                        "seconds",
                                        "minutes",
                                        "hours",
                                        "days",
                                        "weeks",
                                        "months",
                                        "years"
                                    ],
                                    default: ""
                                }
                            }

                        }
                    },
                    definitions: {
                        userDefined: {
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
                        all: {
                            type: "string",
                            title: "All",
                            template: '*'
                        }
                    }
                },
                show_errors: "always",
                startval: fillWith,
                disable_properties: false,
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                display_required_only: true,
                no_additional_properties: true
            });
            $(formContainer).append('<div id="form-submit"><button type="button" ' +
                'class="btn btn-default">Submit</button></div>' +
                '<div id="form-cancel"><button type="button" class="btn btn-default">Cancel</button></div>');

            // 'Submit' button action
            var submitButtonElement = $('#form-submit')[0];
            submitButtonElement.addEventListener('click', function () {
                var isAggregationNameUsed = self.formUtils.IsDefinitionElementNameUnique(editor.getValue().name,
                    clickedElement.getId());
                if(isAggregationNameUsed) {
                    alert("Aggregation name \"" + editor.getValue().name + "\" is already used.");
                    return;
                }
                // The container and the palette are disabled to prevent the user from dropping any elements
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                var config = editor.getValue();

                // update selected aggregation model
                clickedElement.setName(config.name);
                clickedElement.setFrom(config.from);
                clickedElement.setAggregateByAttribute(config.aggregateByAttribute);

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

                var aggregateByTimePeriod = new AggregateByTimePeriod(config.aggregateByTimePeriod);
                if (config.aggregateByTimePeriod === undefined) {
                    aggregateByTimePeriod.setMaxValue('');
                }
                clickedElement.setAggregateByTimePeriod(aggregateByTimePeriod);

                var textNode = $(element).parent().find('.aggregationNameNode');
                textNode.html(config.name);

                // close the form aggregation
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            });

            // 'Cancel' button action
            var cancelButtonElement = $('#form-cancel')[0];
            cancelButtonElement.addEventListener('click', function () {
                self.gridContainer.removeClass('disabledbutton');
                self.toolPaletteContainer.removeClass('disabledbutton');

                // close the form aggregation
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            });
        };

        return AggregationForm;
    });
