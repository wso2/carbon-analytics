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

define(['require', 'log', 'jquery', 'lodash', 'formUtils', 'streamForm', 'tableForm', 'windowForm', 'aggregationForm',
        'triggerForm', 'windowFilterProjectionQueryForm', 'patternQueryForm', 'joinQueryForm', 'partitionForm',
        'sequenceQueryForm'],
    function (require, log, $, _, FormUtils, StreamForm, TableForm, WindowForm, AggregationForm, TriggerForm,
              WindowFilterProjectionQueryForm, PatternQueryForm, JoinQueryForm, PartitionForm, SequenceQueryForm) {

        // common properties for the JSON editor
        JSONEditor.defaults.options.theme = 'bootstrap3';
        //JSONEditor.defaults.options.iconlib = 'bootstrap3';
        JSONEditor.defaults.options.disable_edit_json = true;
        JSONEditor.plugins.sceditor.emoticonsEnabled = true;
        JSONEditor.defaults.options.disable_collapse = true;
        JSONEditor.plugins.selectize.enable = true;

        var constants = {
            STREAM : 'streamDrop',
            TABLE : 'tableDrop',
            WINDOW :'windowDrop',
            TRIGGER :'triggerDrop',
            AGGREGATION : 'aggregationDrop',
            PROJECTION : 'projectionQueryDrop',
            FILTER : 'filterQueryDrop',
            JOIN : 'joinQueryDrop',
            WINDOW_QUERY : 'windowQueryDrop',
            PATTERN : 'patternQueryDrop',
            SEQUENCE : 'sequenceQueryDrop',
            PARTITION :'partitionDrop'
        };

        /**
         * @class FormBuilder Creates forms to collect data when a element is dropped on the canvas
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var FormBuilder = function (options) {
            this.configurationData = options.configurationData;
            this.application = options.application;
            this.consoleListManager = options.application.outputController;
            this.formUtils = new FormUtils(this.configurationData);
            this.jsPlumbInstance = options.jsPlumbInstance;
            var currentTabId = this.application.tabController.activeTab.cid;
            this.designViewContainer = $('#design-container-' + currentTabId);
            this.toggleViewButton = $('#toggle-view-button-' + currentTabId);
        };

        /**
         * @function generate a tab in the output console to view the form
         * @param elementId id of the element which form is created for
         * @param elementType type of the element
         * @returns newly created formConsole
         */
        FormBuilder.prototype.createTabForForm = function (elementId, elementType) {
            var self = this;
            var activeTab = this.application.tabController.getActiveTab();
            var siddhiAppName = "";

            if(activeTab.getTitle().lastIndexOf(".siddhi") !== -1){
                siddhiAppName = activeTab.getTitle().substring(0, activeTab.getTitle().lastIndexOf(".siddhi"));
            } else{
                siddhiAppName = activeTab.getTitle();
            }

            var uniqueTabId = 'form-' + activeTab.cid;
            var consoleOptions = {};
            var options = {};
            _.set(options, '_type', "FORM");
            _.set(options, 'title', "Form");
            _.set(options, 'uniqueTabId', uniqueTabId);
            _.set(options, 'appName', siddhiAppName);

            var console = this.consoleListManager.getGlobalConsole();
            if(console === undefined){
                var globalConsoleOptions = {};
                var opts = {};
                _.set(opts, '_type', "CONSOLE");
                _.set(opts, 'title', "Console");
                _.set(opts, 'currentFocusedFile', siddhiAppName);
                _.set(opts, 'statusForCurrentFocusedFile', "SUCCESS");
                _.set(opts, 'message', "");
                _.set(globalConsoleOptions, 'consoleOptions', opts);
                console = this.consoleListManager.newConsole(globalConsoleOptions);
            }

            _.set(options, 'consoleObj', console);
            _.set(consoleOptions, 'consoleOptions', options);
            var formConsole = this.consoleListManager.newFormConsole(consoleOptions);
            $(formConsole).on( "close-button-in-form-clicked", function() {
                if(elementType === constants.STREAM) {
                    if(self.configurationData.getSiddhiAppConfig().getStream(elementId) === undefined) {
                        $("#"+elementId).remove();
                    }
                }
                if(elementType === constants.TABLE) {
                    if(self.configurationData.getSiddhiAppConfig().getTable(elementId) === undefined) {
                        $("#"+elementId).remove();
                    }
                }
                if(elementType === constants.WINDOW) {
                    if(self.configurationData.getSiddhiAppConfig().getWindow(elementId) === undefined) {
                        $("#"+elementId).remove();
                    }
                }
                if(elementType === constants.TRIGGER) {
                    if(self.configurationData.getSiddhiAppConfig().getTrigger(elementId) === undefined) {
                        $("#"+elementId).remove();
                    }
                }
                if(elementType === constants.AGGREGATION) {
                    if(self.configurationData.getSiddhiAppConfig().getAggregation(elementId) === undefined) {
                        $("#"+elementId).remove();
                    }
                }
                // close the form window
                self.consoleListManager.removeConsole(formConsole);
                self.consoleListManager.hideAllConsoles();
                // design view container and toggle view button are enabled
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');
            });
            return formConsole;
        };

        /**
         * @function generate the form to define the stream once it is dropped on the canvas
         * @param i id for the element
         * @returns user given stream name
         */
        FormBuilder.prototype.DefineStream = function (i) {
            var self = this;
            var formConsole = this.createTabForForm(i, constants.STREAM);
            var formContainer = formConsole.getContentContainer();

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            var streamForm = new StreamForm(formOptions);
            return streamForm.generateDefineForm(i, formConsole, formContainer);
        };

        /**
         * @function generate the property window for an existing stream
         * @param element selected element(stream)
         */
        FormBuilder.prototype.GeneratePropertiesFormForStreams = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            var streamForm = new StreamForm(formOptions);
            streamForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate the form to define the table once it is dropped on the canvas
         * @param i id for the element
         * @returns user given table name
         */
        FormBuilder.prototype.DefineTable = function (i) {
            var self = this;
            var formConsole = this.createTabForForm(i, constants.TABLE);
            var formContainer = formConsole.getContentContainer();

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            var tableForm = new TableForm(formOptions);
            return tableForm.generateDefineForm(i, formConsole, formContainer);
        };

        /**
         * @function generate the property window for an existing table
         * @param element selected element(table)
         */
        FormBuilder.prototype.GeneratePropertiesFormForTables = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            var tableForm = new TableForm(formOptions);
            tableForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate the form to define the window once it is dropped on the canvas
         * @param i id for the element
         * @returns user given window name
         */
        FormBuilder.prototype.DefineWindow = function (i) {
            var self = this;
            var formConsole = this.createTabForForm(i, constants.WINDOW);
            var formContainer = formConsole.getContentContainer();

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            var windowForm = new WindowForm(formOptions);
            return windowForm.generateDefineForm(i, formConsole, formContainer);
        };

        /**
         * @function generate the form for an existing window
         * @param element selected element(window)
         */
        FormBuilder.prototype.GeneratePropertiesFormForWindows = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            var windowForm = new WindowForm(formOptions);
            windowForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate the form to define the trigger once it is dropped on the canvas
         * @param i id for the element
         * @returns user given trigger name
         */
        FormBuilder.prototype.DefineTrigger = function (i) {
            var self = this;
            var formConsole = this.createTabForForm(i, constants.TRIGGER);
            var formContainer = formConsole.getContentContainer();

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            var triggerForm = new TriggerForm(formOptions);
            return triggerForm.generateDefineForm(i, formConsole, formContainer);
        };

        /**
         * @function generate the form window for an existing trigger
         * @param element selected element(trigger)
         */
        FormBuilder.prototype.GeneratePropertiesFormForTriggers = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            var triggerForm = new TriggerForm(formOptions);
            triggerForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate the form to define the aggregation once it is dropped on the canvas
         * @param i id for the element
         * @returns user given aggregation name
         */
        FormBuilder.prototype.DefineAggregation = function (i) {
            var self = this;
            var formConsole = this.createTabForForm(i, constants.AGGREGATION);
            var formContainer = formConsole.getContentContainer();

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            var aggregationForm = new AggregationForm(formOptions);
            return aggregationForm.generateDefineForm(i, formConsole, formContainer);
        };

        /**
         * @function generate the form for an existing aggregation
         * @param element selected element(aggregation)
         */
        FormBuilder.prototype.GeneratePropertiesFormForAggregations = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            var aggregationForm = new AggregationForm(formOptions);
            aggregationForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate the form for the simple queries (projection, filter and window)
         * @param element selected element(query)
         */
        FormBuilder.prototype.GeneratePropertiesFormForWindowFilterProjectionQueries = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            var windowFilterProjectionQueryForm = new WindowFilterProjectionQueryForm(formOptions);
            windowFilterProjectionQueryForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate form for state machine
         * @param element selected element(query)
         */
        FormBuilder.prototype.GeneratePropertiesFormForPatternQueries = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            var patternQueryForm = new PatternQueryForm(formOptions);
            patternQueryForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate form for state machine
         * @param element selected element(query)
         */
        FormBuilder.prototype.GeneratePropertiesFormForSequenceQueries = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            var sequenceQueryForm = new SequenceQueryForm(formOptions);
            sequenceQueryForm.generatePropertiesForm(element, formConsole, formContainer);
        };
        
        /**
         * @function generate form for Join Query
         * @param element selected element(query)
         */
        FormBuilder.prototype.GeneratePropertiesFormForJoinQuery = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            var joinQueryForm = new JoinQueryForm(formOptions);
            joinQueryForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate form for Partition
         * @param element selected element(query)
         */
        FormBuilder.prototype.GeneratePartitionKeyForm = function (element) {
            var self = this;
            var formConsole = this.createTabForForm();
            var formContainer = formConsole.getContentContainer();

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'jsPlumbInstance', self.jsPlumbInstance);
            var partitionForm = new PartitionForm(formOptions);
            partitionForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        return FormBuilder;
    });
