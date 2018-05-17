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

define(['require', 'log', 'jquery', 'lodash', 'attribute', 'table'],
    function (require, log, $, _, Attribute,  Table) {

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
         * @function generate form when defining a form
         * @param i id for the element
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        TableForm.prototype.generateDefineForm = function (i, formConsole, formContainer) {
            var self = this;
            var propertyDiv = $('<div id="property-header"><h3>Define Table </h3></div>' +
                '<div id="define-table" class="define-table"></div>');
            formContainer.append(propertyDiv);

            // generate the form to define a table
            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Table",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 1
                        },
                        attributes: {
                            required: true,
                            propertyOrder: 2,
                            type: "array",
                            format: "table",
                            title: "Attributes",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title : 'Attribute',
                                properties: {
                                    name: {
                                        title: "Name",
                                        type: "string",
                                        minLength: 1
                                    },
                                    type: {
                                        title: "Type",
                                        type: "string",
                                        enum: [
                                            "string",
                                            "int",
                                            "long",
                                            "float",
                                            "double",
                                            "boolean"
                                        ],
                                        default: "string"
                                    }
                                }
                            }
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
            var submitButtonElement = $(formContainer).find('#submit')[0];
            submitButtonElement.addEventListener('click', function () {

                var errors = editor.validate();
                if(errors.length) {
                    return;
                }
                var isTableNameUsed = self.formUtils.isDefinitionElementNameUnique(editor.getValue().name);
                if (isTableNameUsed) {
                    alert("Table name \"" + editor.getValue().name + "\" is already used.");
                    return;
                }
                // add the new out table to the table array
                var tableOptions = {};
                _.set(tableOptions, 'id', i);
                _.set(tableOptions, 'name', editor.getValue().name);
                _.set(tableOptions, 'store', undefined);
                var table = new Table(tableOptions);
                _.forEach(editor.getValue().attributes, function (attribute) {
                    var attributeObject = new Attribute(attribute);
                    table.addAttribute(attributeObject);
                });
                self.configurationData.getSiddhiAppConfig().addTable(table);

                var textNode = $('#'+i).find('.tableNameNode');
                textNode.html(editor.getValue().name);

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();

                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

            });
            return editor.getValue().name;
        };

        /**
         * @function generate properties form for a table
         * @param element selected element(table)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        TableForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var id = $(element).parent().attr('id');
            // retrieve the table information from the collection
            var clickedElement = self.configurationData.getSiddhiAppConfig().getTable(id);
            if(clickedElement === undefined) {
                var errorMessage = 'unable to find clicked element';
                log.error(errorMessage);
            }
            var name = clickedElement.getName();
            var attributes = clickedElement.getAttributeList();
            var fillWith = {
                name : name,
                attributes : attributes
            };
            var editor = new JSONEditor(formContainer[0], {
                schema: {
                    type: "object",
                    title: "Table",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name",
                            minLength: 1,
                            required: true,
                            propertyOrder: 1
                        },
                        attributes: {
                            required: true,
                            propertyOrder: 2,
                            type: "array",
                            format: "table",
                            title: "Attributes",
                            uniqueItems: true,
                            minItems: 1,
                            items: {
                                type: "object",
                                title : 'Attribute',
                                properties: {
                                    name: {
                                        title: "Name",
                                        type: "string",
                                        minLength: 1
                                    },
                                    type: {
                                        title: "Type",
                                        type: "string",
                                        enum: [
                                            "string",
                                            "int",
                                            "long",
                                            "float",
                                            "double",
                                            "boolean"
                                        ],
                                        default: "string"
                                    }
                                }
                            }
                        }
                    }
                },
                show_errors: "always",
                disable_properties: false,
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                display_required_only: true,
                no_additional_properties: true,
                startval: fillWith
            });
            formContainer.append('<div id="form-submit"><button type="button" ' +
                'class="btn btn-default">Submit</button></div>' +
                '<div id="form-cancel"><button type="button" class="btn btn-default">Cancel</button></div>');

            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#form-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                var errors = editor.validate();
                if(errors.length) {
                    return;
                }
                var isTableNameUsed = self.formUtils.isDefinitionElementNameUnique(editor.getValue().name,
                    clickedElement.getId());
                if (isTableNameUsed) {
                    alert("Table name \"" + editor.getValue().name + "\" is already used.");
                    return;
                }
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

                var config = editor.getValue();

                // update selected table model
                clickedElement.setName(config.name);
                // removing all elements from attribute list
                clickedElement.clearAttributeList();
                // adding new attributes to the attribute list
                _.forEach(config.attributes, function (attribute) {
                    var attributeObject = new Attribute(attribute);
                    clickedElement.addAttribute(attributeObject);
                });

                var textNode = $(element).parent().find('.tableNameNode');
                textNode.html(config.name);

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            });

            // 'Cancel' button action
            var cancelButtonElement = $(formContainer).find('#form-cancel')[0];
            cancelButtonElement.addEventListener('click', function () {
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
            });
        };

        return TableForm;
    });
