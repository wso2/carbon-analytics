/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'log', 'jquery', 'lodash', 'formUtils', 'streamForm', 'tableForm', 'windowForm', 'aggregationForm',
        'triggerForm', 'windowFilterProjectionQueryForm', 'patternQueryForm', 'joinQueryForm', 'partitionForm',
        'sequenceQueryForm', 'sourceForm', 'sinkForm', 'functionForm', 'appAnnotationForm', 'constants'],
    function (require, log, $, _, FormUtils, StreamForm, TableForm, WindowForm, AggregationForm, TriggerForm,
              WindowFilterProjectionQueryForm, PatternQueryForm, JoinQueryForm, PartitionForm, SequenceQueryForm,
              SourceForm, SinkForm, FunctionForm, AppAnnotationForm, Constants) {

        // common properties for the JSON editor
        JSONEditor.defaults.options.theme = 'bootstrap3';
        JSONEditor.defaults.options.iconlib = 'bootstrap3';
        JSONEditor.defaults.options.disable_edit_json = true;
        JSONEditor.plugins.sceditor.emoticonsEnabled = true;
        JSONEditor.defaults.options.disable_collapse = true;
        JSONEditor.plugins.selectize.enable = true;

        var constants = {
            SOURCE: 'sourceDrop',
            SINK: 'sinkDrop',
            STREAM: 'streamDrop',
            TABLE: 'tableDrop',
            WINDOW: 'windowDrop',
            TRIGGER: 'triggerDrop',
            AGGREGATION: 'aggregationDrop',
            FUNCTION: 'functionDrop',
            PROJECTION: 'projectionQueryDrop',
            FILTER: 'filterQueryDrop',
            JOIN: 'joinQueryDrop',
            WINDOW_QUERY: 'windowQueryDrop',
            FUNCTION_QUERY: 'functionQueryDrop',
            PATTERN: 'patternQueryDrop',
            SEQUENCE: 'sequenceQueryDrop',
            PARTITION: 'partitionDrop'
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
            this.jsPlumbInstance = options.jsPlumbInstance;
            this.dropElementInstance = options.dropElementInstance;
            this.designGrid = options.designGrid;
            this.formUtils = new FormUtils(this.application, this.configurationData, this.jsPlumbInstance);
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
        FormBuilder.prototype.createTabForForm = function (formType) {
            var self = this;
            var activeTab = this.application.tabController.getActiveTab();
            var siddhiAppName = "";

            if (activeTab.getTitle().lastIndexOf(".siddhi") !== -1) {
                siddhiAppName = activeTab.getTitle().substring(0, activeTab.getTitle().lastIndexOf(".siddhi"));
            } else {
                siddhiAppName = activeTab.getTitle();
            }

            var uniqueTabId = 'form-' + activeTab.cid;
            var formTitle = formType + ' - ' + siddhiAppName;
            var consoleOptions = {};
            var options = {};
            _.set(options, '_type', "FORM");
            _.set(options, 'title', "Form");
            _.set(options, 'uniqueTabId', uniqueTabId);
            _.set(options, 'appName', formTitle);

            var console = this.consoleListManager.getGlobalConsole();
            if (!console) {
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
            $(formConsole).on("close-button-in-form-clicked", function () {
                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
                self.consoleListManager.removeAllConsoles();
            });
            return formConsole;
        };

        /**
         * @function generate the form to add app annotations
         * @param element the element
         */
        FormBuilder.prototype.DefineFormForAppAnnotations = function (element) {
            var self = this;
            var formConsole = this.createTabForForm(Constants.APP_ANNOTATION_TITLE);
            var formContainer = formConsole.getContentContainer().find('.design-view-form-content');

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            var appAnnotationForm = new AppAnnotationForm(formOptions);
            appAnnotationForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate the property window for an existing source
         * @param element selected element(source)
         */
        FormBuilder.prototype.GeneratePropertiesFormForSources = function (element) {
            var self = this;
            var formConsole = this.createTabForForm(Constants.SOURCE_TITLE);
            var formContainer = formConsole.getContentContainer().find('.design-view-form-content');;

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            _.set(formOptions, 'dropElementInstance', self.dropElementInstance);
            _.set(formOptions, 'jsPlumbInstance', self.jsPlumbInstance);
            var sourceForm = new SourceForm(formOptions);
            sourceForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate the property window for an existing sink
         * @param element selected element(sink)
         */
        FormBuilder.prototype.GeneratePropertiesFormForSinks = function (element) {
            var self = this;
            var formConsole = this.createTabForForm(Constants.SINK_TITLE);
            var formContainer = formConsole.getContentContainer().find('.design-view-form-content');;

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            _.set(formOptions, 'dropElementInstance', self.dropElementInstance);
            _.set(formOptions, 'jsPlumbInstance', self.jsPlumbInstance);
            var sinkForm = new SinkForm(formOptions);
            sinkForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate the property window for an existing stream
         * @param element selected element(stream)
         */
        FormBuilder.prototype.GeneratePropertiesFormForStreams = function (element) {
            var self = this;
            var formConsole = this.createTabForForm(Constants.STREAM_TITLE);
            var formContainer = formConsole.getContentContainer().find('.design-view-form-content');;

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            _.set(formOptions, 'dropElementInstance', self.dropElementInstance);
            _.set(formOptions, 'jsPlumbInstance', self.jsPlumbInstance);
            _.set(formOptions, 'designGrid', self.designGrid);
            var streamForm = new StreamForm(formOptions);
            streamForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate the property window for an existing table
         * @param element selected element(table)
         */
        FormBuilder.prototype.GeneratePropertiesFormForTables = function (element) {
            var self = this;
            var formConsole = this.createTabForForm(Constants.TABLE_TITLE);
            var formContainer = formConsole.getContentContainer().find('.design-view-form-content');;

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            _.set(formOptions, 'jsPlumbInstance', self.jsPlumbInstance);
            var tableForm = new TableForm(formOptions);
            tableForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate the form for an existing window
         * @param element selected element(window)
         */
        FormBuilder.prototype.GeneratePropertiesFormForWindows = function (element) {
            var self = this;
            var formConsole = this.createTabForForm(Constants.WINDOW_TITLE);
            var formContainer = formConsole.getContentContainer().find('.design-view-form-content');;

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            _.set(formOptions, 'jsPlumbInstance', self.jsPlumbInstance);
            var windowForm = new WindowForm(formOptions);
            windowForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate the form window for an existing trigger
         * @param element selected element(trigger)
         */
        FormBuilder.prototype.GeneratePropertiesFormForTriggers = function (element) {
            var self = this;
            var formConsole = this.createTabForForm(Constants.TRIGGER_TITLE);
            var formContainer = formConsole.getContentContainer().find('.design-view-form-content');;

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            _.set(formOptions, 'jsPlumbInstance', self.jsPlumbInstance);
            var triggerForm = new TriggerForm(formOptions);
            triggerForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate the form for an existing aggregation
         * @param element selected element(aggregation)
         */
        FormBuilder.prototype.GeneratePropertiesFormForAggregations = function (element) {
            var self = this;
            var formConsole = this.createTabForForm(Constants.AGGREGATION_TITLE);
            var formContainer = formConsole.getContentContainer().find('.design-view-form-content');;

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            _.set(formOptions, 'jsPlumbInstance', self.jsPlumbInstance);
            var aggregationForm = new AggregationForm(formOptions);
            aggregationForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate the form for an existing function
         * @param element selected element(function)
         */
        FormBuilder.prototype.GeneratePropertiesFormForFunctions = function (element) {
            var self = this;
            var formConsole = this.createTabForForm(Constants.FUNCTION_TITLE);
            var formContainer = formConsole.getContentContainer().find('.design-view-form-content');;

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            _.set(formOptions, 'jsPlumbInstance', self.jsPlumbInstance);
            var functionForm = new FunctionForm(formOptions);
            functionForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        /**
         * @function generate the form for the simple queries (projection, filter, window and function)
         * @param element selected element(query)
         */
        FormBuilder.prototype.GeneratePropertiesFormForWindowFilterProjectionQueries = function (element) {
            var self = this;
            var formConsole = this.createTabForForm(Constants.QUERY_TITLE);
            var formContainer = formConsole.getContentContainer().find('.design-view-form-content');;

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
            var formConsole = this.createTabForForm(Constants.PATTERN_QUERY_TITLE);
            var formContainer = formConsole.getContentContainer().find('.design-view-form-content');;

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
            var formConsole = this.createTabForForm(Constants.SEQUENCE_QUERY_TITLE);
            var formContainer = formConsole.getContentContainer().find('.design-view-form-content');;

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
            var formConsole = this.createTabForForm(Constants.JOIN_QUERY_TITLE);
            var formContainer = formConsole.getContentContainer().find('.design-view-form-content');;

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
            var formConsole = this.createTabForForm(Constants.PARTITION_TITLE);
            var formContainer = formConsole.getContentContainer().find('.design-view-form-content');;

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            _.set(formOptions, 'jsPlumbInstance', self.jsPlumbInstance);
            var partitionForm = new PartitionForm(formOptions);
            partitionForm.generatePropertiesForm(element, formConsole, formContainer);
        };

        return FormBuilder;
    });

