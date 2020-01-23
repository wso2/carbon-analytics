/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['jquery', 'log', './simulator-rest-client', 'lodash', './open-siddhi-apps', 'workspace',
        /* void libs*/ 'bootstrap', 'theme_wso2', 'jquery_ui', 'jquery_validate', 'jquery_timepicker', './templates'],
    function ($, log, Simulator, _, OpenSiddhiApps) {

        "use strict";   // JS strict mode

        var self = {};

        self.init = function (config) {
            self.feedConfigs = [];
            self.currentTotalSourceNum = 1;
            self.dataCollapseNum = 1;
            self.totalSourceNum = 1;

            self.selectedFeed = -1;
            self.selectedSource = -1;
            self.eventFeedConfigCount = 1;
            self.loggedTimestamp = null;

            self.siddhiAppDetailsMap = {};
            self.eventFeedForm = $('#event-feed-form').find('form').clone();
            self.$eventFeedConfigTab = $("#event-feed-config-tab");
            self.$eventFeedConfigTabContent = $(".simulation-list");
            self.$eventFeedForm = $('#event-feed-form');
            self.$eventFeedTab = $('#event-simulator ul.nav-tabs').find('li a[aria-controls="event-feed-configs"]');
            self.isDirty = false;

            self.FAULTY = 'FAULTY';
            self.STOP = 'STOP';
            self.RUN = 'RUN';
            self.app = _.get(config, 'application');

            self.activeSimulationList = {};
            self.inactiveSimulationList = {};

            self.OpenSiddhiApps = OpenSiddhiApps;
            self.workspace = self.app.workspaceManager;
            self.OpenSiddhiApps.init(config);
            self.consoleTab = $('#console-container li.console-header');
            self.notification_container = $("#notification-container");
            self.simulationDeleteModal = null;

            self.propertyBasedGenerationOptionsForString = ['FIRST_NAME', 'TIME_12H', 'TIME_24H',
                'SECOND', 'MINUTE', 'MONTH',
                'MONTH_NUM', 'YEAR', 'DAY',
                'DAY_OF_WEEK', 'DATE', 'FULL_NAME',
                'LAST_NAME', 'WORDS',
                'BSN', 'ADDRESS', 'EMAIL',
                'PHONE_NUM', 'POST_CODE', 'STATE',
                'CITY', 'COMPANY', 'COUNTRY',
                'STREET_NAME', 'HOUSE_NO', 'HEIGHT_CM',
                'HEIGHT_M', 'WEIGHT', 'OCCUPATION',
                'IBAN', 'BIC', 'VISA_CARD', 'PIN_CODE',
                'URL', 'IP', 'IP_V6', 'MAC_ADDRESS', 'UUID',
                'USERNAME', 'COLOUR', 'ALTITUDE',
                'DEPTH', 'COORDINATES', 'LATITUDE',
                'LONGITUDE', 'GEO_HASH', 'SENTENCE', 'PARAGRAPH'
            ];

            self.propertyBasedGenerationOptionsForInt = [
                'YEAR', 'SECOND', 'MINUTE', 'MONTH',
                'MONTH_NUM', 'DAY', 'DAY_OF_WEEK',
                'PHONE_NUM', 'POST_CODE', 'LATITUDE',
                'LONGITUDE', 'GEO_HASH'
            ];

            self.propertyBasedGenerationOptionsForLong = [
                'LATITUDE', 'SECOND', 'MINUTE', 'MONTH',
                'MONTH_NUM', 'YEAR', 'DAY', 'DAY_OF_WEEK',
                'PHONE_NUM', 'POST_CODE', 'HOUSE_NO',
                'PIN_CODE', 'LONGITUDE', 'GEO_HASH'
            ];

            self.propertyBasedGenerationOptionsForDouble = [
                'HEIGHT_CM', 'SECOND', 'MINUTE', 'MONTH',
                'MONTH_NUM', 'YEAR', 'DAY', 'DAY_OF_WEEK',
                'PHONE_NUM', 'POST_CODE', 'HOUSE_NO',
                'HEIGHT_M', 'WEIGHT', 'PIN_CODE',
                'LATITUDE', 'LONGITUDE', 'GEO_HASH'
            ];

            self.propertyBasedGenerationOptionsForFloat = [
                'HEIGHT_M', 'SECOND', 'MINUTE', 'MONTH',
                'MONTH_NUM', 'YEAR', 'DAY',
                'DAY_OF_WEEK', 'PHONE_NUM', 'POST_CODE',
                'HOUSE_NO', 'HEIGHT_CM', 'WEIGHT', 'PIN_CODE',
                'LATITUDE', 'LONGITUDE', 'GEO_HASH'
            ];

            self.pollingSimulation();

            self.addAvailableFeedSimulations();

            var $form = $('form.feedSimulationConfig');
            self.form = $form;

            $form.validate({
                ignore: false,
                invalidHandler: function (e, validator) {
                    for (var i = 0; i < validator.errorList.length; i++) {
                        $(validator.errorList[i].element)
                            .closest('div.panel-collapse.collapse[id^="source_"]').collapse('show');
                    }
                }
            });

            $form.find('input[name="simulation-name"]').rules('add', {
                required: function () {
                    if ($form.find('input[name="simulation-name"]').val() == "") {
                        return true;
                    }
                },
                messages: {
                    required: "Please enter an simulation name."
                }
            });

            $form.find('input[name="start-timestamp"]').rules('add', {
                digits: true,
                messages: {
                    digits: "Start timestamp attribute must be a positive integer."
                }
            });

            $form.find('input[name="end-timestamp"]').rules('add', {
                digits: true,
                messages: {
                    digits: "End timestamp attribute must be a positive integer."
                }
            });

            $form.find('input[name="no-of-events"]').rules('add', {
                digits: true,
                messages: {
                    digits: "No of events should be a positive integer."
                }
            });

            $form.find('a[id="feedAdvanceConfigToggle"]').on('click', function () {
                if ($(this).hasClass("active")) {
                    $(this).removeClass('active');
                } else {
                    $(this).addClass("active");
                }
                $("#feedAdvanceContent").toggle();
            });

            $form.find(":input").change(function () {
                self.isDirty = true;
            });

            $("#event-feed-form").on('submit', 'form.feedSimulationConfig', function () {
                if (self.currentTotalSourceNum == 1) {
                    $("#addNewSourceError").removeClass("hidden");
                    return false;
                } else {
                    var simulation = {};
                    var properties = {};

                    if ($form.find('input[name="simulation-name"]').val() == "") {
                        properties.simulationName = $form.find('input[name="simulation-name"]').attr('placeholder');
                    } else {
                        properties.simulationName = $form.find('input[name="simulation-name"]').val();
                    }

                    properties.startTimestamp = $form.find('input[name="start-timestamp"]').val();
                    properties.endTimestamp = $form.find('input[name="end-timestamp"]').val();
                    properties.noOfEvents = $form.find('input[name="no-of-events"]').val();
                    properties.description = $form.find('textarea[name="feed-description"]').val();
                    properties.timeInterval = $form.find('input[name="time-interval"]').val();
                    if (properties.timeInterval == "") {
                        properties.timeInterval = "1000";
                    }
                    simulation.properties = properties;
                    var sources = [];
                    $('div.sourceConfigs div.source').each(function () {
                        var $sourceConfigForm = $(this).find('.sourceConfigForm');
                        var sourceType = $sourceConfigForm.attr('data-type');
                        var uniqueId = $sourceConfigForm.attr('data-uuid');
                        var source = {};
                        source.siddhiAppName = $sourceConfigForm.find('select[id="siddhi-app-name_' + uniqueId + '"]').val();
                        source.streamName = $sourceConfigForm.find('select[name="stream-name"]').val();
                        source.timestampInterval = $sourceConfigForm.find('input[name="timestamp-interval"]').val();
                        var indices;
                        var $attributes;
                        if ('csv' == sourceType) {
                            source.simulationType = "CSV_SIMULATION";
                            source.fileName = $sourceConfigForm.find('select[name="file-name"]').val();
                            source.delimiter = $sourceConfigForm.find('input[name="delimiter"]').val();
                            if ($sourceConfigForm.find('input[name="timestamp-attribute"]').is(':disabled')) {
                                source.isOrdered = true;
                                source.timestampInterval = $sourceConfigForm.find('input[name="timestamp-interval"]').val();
                            } else {
                                source.timestampAttribute = $sourceConfigForm.find('input[name="timestamp-attribute"]').val();
                                if ($sourceConfigForm.find('input[value="ordered"]').is(':checked')) {
                                    source.isOrdered = true;
                                }
                                if ($sourceConfigForm.find('input[value="not-ordered"]').is(':checked')) {
                                    source.isOrdered = false;
                                }
                            }
                            indices = "";
                            $attributes = $sourceConfigForm.find('input[id^="attributes"]');
                            $attributes.each(function () {
                                if ("" != $(this).val()) {
                                    if (indices == "") {
                                        indices += $(this).val();
                                    } else {
                                        indices += "," + $(this).val();
                                    }
                                }
                            });
                            if ("" != indices) {
                                source.indices = indices;
                            }
                        } else if ('db' == sourceType) {
                            source.simulationType = "DATABASE_SIMULATION";
                            source.dataSourceLocation = $form.find('input[name="data-source-location"]').val();
                            source.driver = $form.find('input[name="driver-class"]').val();
                            source.username = $form.find('input[name="username"]').val();
                            source.password = $form.find('input[name="password"]').val();
                            source.tableName = $form.find('select[name="table-name"]').val();
                            if ($sourceConfigForm.find('select[name="timestamp-attribute"]').is(':disabled')) {
                                source.timestampInterval = $sourceConfigForm.find('input[name="timestamp-interval"]').val();
                            } else {
                                source.timestampAttribute = $sourceConfigForm.find('select[name="timestamp-attribute"]').val();
                            }
                            var columnNamesList = "";
                            $attributes = $sourceConfigForm.find('select[id^="attributes"]');
                            $attributes.each(function () {
                                if ("" != $(this).val()) {
                                    if (columnNamesList == "") {
                                        columnNamesList += $(this).val();
                                    } else {
                                        columnNamesList += "," + $(this).val();
                                    }
                                }
                            });
                            if ("" != columnNamesList) {
                                source.columnNamesList = columnNamesList;
                            }
                        } else if ('random' == sourceType) {
                            source.simulationType = "RANDOM_DATA_SIMULATION";
                            source.timestampInterval = $sourceConfigForm.find('input[name="timestamp-interval"]').val();
                            source.attributeConfiguration = [];
                            var $attributesDivs = $sourceConfigForm
                                .find('div.attributes-section label[for^="attributes_"]').closest('div.form-group');
                            $attributesDivs.each(function () {
                                var attributeConfig = {};
                                var $attributesDiv = $(this);
                                if ("custom" == $attributesDiv.find('select[id^="attributes_"]').val()) {
                                    attributeConfig.type = "CUSTOM_DATA_BASED";
                                    var valueList = $attributesDiv.find('input[data-type="custom"]').val();
                                    attributeConfig.list = valueList.split(",");
                                } else if ("primitive" == $attributesDiv.find('select[id^="attributes_"]').val()) {
                                    attributeConfig.type = "PRIMITIVE_BASED";
                                    var attDataType = $attributesDiv.find('select[id^="attributes_"]').attr("data-type");
                                    if ("BOOL" == attDataType) {
                                        attributeConfig.primitiveType = "BOOL";
                                    } else if ("STRING" == attDataType) {
                                        attributeConfig.primitiveType = "STRING";
                                        attributeConfig.length = $attributesDiv.find('input[name$="_primitive_length"]').val();
                                    } else if ("INT" == attDataType || "LONG" == attDataType) {
                                        attributeConfig.primitiveType = "INT";
                                        attributeConfig.min = $attributesDiv.find('input[name$="_primitive_min"]').val();
                                        attributeConfig.max = $attributesDiv.find('input[name$="_primitive_max"]').val();
                                    } else if ("FLOAT" == attDataType || "DOUBLE" == attDataType) {
                                        attributeConfig.primitiveType = "FLOAT";
                                        attributeConfig.min = $attributesDiv.find('input[name$="_primitive_min"]').val();
                                        attributeConfig.max = $attributesDiv.find('input[name$="_primitive_max"]').val();
                                        attributeConfig.precision =
                                            $attributesDiv.find('input[name$="_primitive_precision"]').val();
                                    }
                                } else if ("property" == $attributesDiv.find('select[id^="attributes_"]').val()) {
                                    attributeConfig.type = "PROPERTY_BASED";
                                    attributeConfig.property = $attributesDiv.find('select[name$="_property"]').val();
                                } else if ("regex" == $attributesDiv.find('select[id^="attributes_"]').val()) {
                                    attributeConfig.type = "REGEX_BASED";
                                    attributeConfig.pattern = $attributesDiv.find('input[name$="_regex"]').val();
                                }
                                source.attributeConfiguration.push(attributeConfig);
                            });
                        }
                        sources.push(source);
                        simulation.sources = sources;
                    });

                    if ("edit" == $("#event-feed-form").attr("mode")) {
                        $('#event-feed-form').removeAttr("mode");
                        Simulator.updateSimulation(
                            simulation.properties.simulationName,
                            JSON.stringify(simulation),
                            function (data) {
                                var simulationName = simulation.properties.simulationName;
                                self.activeSimulationList[simulationName] = simulation;
                                self.clearEventFeedForm();
                                $.sidebar_toggle('hide', '#left-sidebar-sub', '.simulation-list');
                                self.alertSuccess(
                                    "'" + simulation.properties.simulationName + "' feed updated. Please wait until feed" +
                                    " list updated.");
                                $("#create-simulation-modal-backdrop").remove();
                                log.info(data);
                            },
                            function (data) {
                                self.addInActiveSimulationToUi(simulation);
                                self.alertError(JSON.parse(data.responseText).message);
                                log.error(data);
                            }
                        );
                    } else {
                        Simulator.uploadSimulation(
                            JSON.stringify(simulation),
                            function (data) {
                                self.clearEventFeedForm();
                                $.sidebar_toggle('hide', '#left-sidebar-sub', '.simulation-list');
                                self.alertSuccess(
                                    "'" + simulation.properties.simulationName + "' feed saved. Please wait until feed" +
                                    " list updated.");
                                $("#create-simulation-modal-backdrop").remove();
                                log.info(data);
                            },
                            function (data) {
                                self.alertError(JSON.parse(data.responseText).message);
                                log.error(data);
                            }
                        );
                    }
                    self.enableEditButtons();
                    self.enableCreateButtons(false);
                    $("#event-feed-form").removeAttr("mode");
                    self.isDirty = false;
                    //if needed we can add this self.addLoadingButton(self.$eventFeedConfigTabContent);
                    return false;
                }

            });

            $("#event-feed-form").on('click', '.feedSimulationConfig button.addNewSource', function () {
                $('.collapse').collapse();
                $("#addNewSourceError").addClass("hidden");
                var $sourceConfigs = $(this).closest('form.feedSimulationConfig').find('div.sourceConfigs');
                var sourceType = $(this).closest('div.form-inline').find('select.sources').val();
                var sourcePanel = self.createConfigPanel(self.currentTotalSourceNum, self.dataCollapseNum, sourceType);
                $sourceConfigs.append(sourcePanel);
                var sourceForm = self.createSourceForm(sourceType, self.currentTotalSourceNum);
                var $sourceConfigBody =
                    $sourceConfigs.find('div.source[data-uuid=' + self.currentTotalSourceNum + '] div.panel-body');
                $sourceConfigBody.append(sourceForm);
                self.loadSiddhiAppNames(self.totalSourceNum);
                self.loadCSVFileNames(self.totalSourceNum, true);
                self.addSourceConfigValidation(sourceType, self.currentTotalSourceNum);

                sourceForm.find(":input").change(function () {
                    self.isDirty = true;
                });
                self.bindDynamicContent(sourceForm, sourceType, self.currentTotalSourceNum);
                self.currentTotalSourceNum++;
                self.dataCollapseNum++;
                self.totalSourceNum++;
                return false;
            });

            $("#run_app_modal").on('click', 'button[name="confirm"]', function () {
                var simulationName = $("#run_app_modal").attr("data-uuid");
                var $panel = $("#active-simulation-list").find('div.input-group[data-name="' + simulationName + '"]');
                var tabController = self.app.tabController;
                var simulationConfigs = self.activeSimulationList[simulationName].sources;
                var prevActiveTab = tabController.getActiveTab();
                var activeTab = '';
                var $siddhiAppStartList = $(this).closest("div.modal-content").find("div.siddhi-app-list");
                var simulatingApps = {};

                $siddhiAppStartList.find("div.siddhi_app_mode_config").each(function () {
                    var $appMode = $(this);
                    var siddhiAppName = $appMode.find("label.siddhi_app_name").text();
                    simulatingApps[siddhiAppName] = "run";
                });
                for (var i = 0; i < simulationConfigs.length; i++) {
                    var siddhiAppName = simulationConfigs[i].siddhiAppName;
                    activeTab = tabController.getTabFromTitle(siddhiAppName);
                    if (!activeTab) {
                        self.OpenSiddhiApps.openFile(siddhiAppName);
                        activeTab = tabController.getTabFromTitle(siddhiAppName);
                    }
                    tabController.setActiveTab(activeTab);
                    if (siddhiAppName in simulatingApps) {
                        var launcher;
                        launcher = self.app.tabController.getActiveTab().getSiddhiFileEditor().getLauncher();
                        launcher.runApplication(self.workspace, false);
                    }
                }
                tabController.setActiveTab(activeTab);
                var file = activeTab.getFile();
                if (file !== undefined && !file.isDirty()) {
                    self.simulateFeed(simulationName, $panel);
                }
            });

            self.$eventFeedConfigTabContent.on('click', 'a i.fw-start', function () {
                var $panel = $(this).closest('.input-group');
                var simulationName = $panel.attr('data-name');
                var $runAppModal = $("#run_app_modal");
                $runAppModal.attr("data-uuid", simulationName);
                var stoppedAppAvailable = false;
                var isValidApp = false;
                var appName = "";
                var dynamicRunContent = "";
                var $siddhiAppList = $runAppModal.find("div.siddhi-app-list");
                $siddhiAppList.empty();

                Simulator.retrieveSiddhiAppNames(
                    function (data) {
                        var simulationConfigs = self.activeSimulationList[simulationName].sources;
                        for (var j = 0; j < data.length; j++) {
                            for (var i = 0; i < simulationConfigs.length; i++) {
                                if (data[j]['siddhiAppName'] == simulationConfigs[i].siddhiAppName && "STOP" ==
                                    data[j]['mode']) {
                                    appName = data[j]['siddhiAppName'];
                                    stoppedAppAvailable = true;
                                    isValidApp = true;
                                    dynamicRunContent += self.createSiddhiAppList(data[j]['siddhiAppName']);
                                    break;
                                } else if (data[j]['siddhiAppName'] == simulationConfigs[i].siddhiAppName &&
                                    "RUN" == data[j]['mode']) {
                                    //todo handle properly
//                                 if(stoppedAppAvailable){
//                                    $siddhiAppList.append(self.createRunDebugButtons(data[j]['siddhiAppName']));
//                                 }
                                    stoppedAppAvailable = false;
                                    appName = data[j]['siddhiAppName'];
                                    isValidApp = true;
                                    break;
                                } else if (data[j]['siddhiAppName'] == simulationConfigs[i].siddhiAppName && "FAULTY" ==
                                    data[j]['mode']) {
                                    appName = data[j]['siddhiAppName'];
                                    isValidApp = false;
                                    break;
                                }
                            }
                        }
                        if (!isValidApp) {
                            var message = {
                                "type": "ERROR",
                                "message": "Cannot Simulate Siddhi App \"" + appName + "\" as its in Faulty state."
                            };
                            var consoleListManager = self.app.outputController;
                            var console = consoleListManager.getGlobalConsole();
                            if (self.console == undefined && console == undefined) {
                                var consoleOptions = {};
                                var options = {};
                                _.set(options, '_type', "CONSOLE");
                                _.set(options, 'title', "Console");
                                _.set(options, 'statusForCurrentFocusedFile', "simulation");
                                _.set(options, 'message', message);
                                _.set(consoleOptions, 'consoleOptions', options);
                                console = consoleListManager.newConsole(consoleOptions);
                            }
                            self.console = console;
                            self.console.println(message);
                        } else if (stoppedAppAvailable) {
                            $siddhiAppList.append(dynamicRunContent);
                            $runAppModal.modal('show');
                        } else {
                            self.simulateFeed(simulationName, $panel);
                        }
                    },
                    function (data) {
                        log.info(data);
                    }
                );
            });

            self.$eventFeedConfigTabContent.on('click', 'a i.fw-assign', function () {
                var $panel = $(this).closest('.input-group');
                var simulationName = $panel.attr('data-name');
                Simulator.simulationAction(
                    simulationName,
                    "pause",
                    function (data) {
                        var message = {
                            "type": "INFO",
                            "message": data.message
                        };
                        self.console.println(message);
                        self.activeSimulationList[simulationName].status = "PAUSE";
                        $panel.find('i.fw-start').closest('a').addClass("hidden");
                        $panel.find('i.fw-assign').closest('a').addClass("hidden");
                        $panel.find('i.fw-resume').closest('a').removeClass("hidden");
                        $panel.find('i.fw-stop').closest('a').removeClass("hidden");
                    },
                    function (msg) {
                        var message = {
                            "type": "ERROR",
                            "message": msg
                        };
                        self.console.println(message);
                    }
                );
            });

            self.$eventFeedConfigTabContent.on('click', 'a i.fw-resume', function () {
                var $panel = $(this).closest('.input-group');
                var simulationName = $panel.attr('data-name');
                Simulator.simulationAction(
                    simulationName,
                    "resume",
                    function (data) {
                        var message = {
                            "type": "INFO",
                            "message": data.message
                        };
                        self.activeSimulationList[simulationName].status = "RUN";
                        $panel.find('i.fw-start').closest('a').addClass("hidden");
                        $panel.find('i.fw-assign').closest('a').removeClass("hidden");
                        $panel.find('i.fw-resume').closest('a').addClass("hidden");
                        $panel.find('i.fw-stop').closest('a').removeClass("hidden");
                        self.console.println(message);
                    },
                    function (msg) {
                        var message = {
                            "type": "ERROR",
                            "message": msg
                        };
                        self.console.println(msg);
                    }
                );
            });

            self.$eventFeedConfigTabContent.on('click', 'a i.fw-stop', function () {
                var $panel = $(this).closest('.input-group');
                var simulationName = $panel.attr('data-name');
                self.activeSimulationList[simulationName].status = "STOP";
                Simulator.simulationAction(
                    simulationName,
                    "stop",
                    function (data) {
                        var message = {
                            "type": "INFO",
                            "message": data.message
                        };
                        self.console.println(message);
                    },
                    function (msg) {
                        var responseText = JSON.parse(msg.responseText);
                        var message = {
                            "type": "ERROR",
                            "message": responseText.status
                        };
                        self.console.println(message);
                    }
                );
                $panel.find('i.fw-start').closest('a').removeClass("hidden");
                $panel.find('i.fw-assign').closest('a').addClass("hidden");
                $panel.find('i.fw-resume').closest('a').addClass("hidden");
                $panel.find('i.fw-stop').closest('a').addClass("hidden");
                $panel.find('div.input-group-btn button.dropdown-toggle').prop('disabled', false);
            });

            self.$eventFeedConfigTabContent.on('click', 'a[name="delete-source"]', function () {
                var $panel = $(this).closest('.input-group');
                var simulationName = $panel.attr('data-name');
                self.simulationDeleteModal = getSimulationDeleteConfirmation(simulationName);
                self.simulationDeleteModal.modal('show').on('shown.bs.modal');
                self.simulationDeleteModal.on('hidden.bs.modal');
                var deleteWizardError = self.simulationDeleteModal.find("#deleteWizardError");
                deleteWizardError.hide();
                self.simulationDeleteModal.find("button").filter("#deleteButton").click(function () {
                    Simulator.deleteSimulation(
                        simulationName,
                        function (data) {
                            self.simulationDeleteModal.modal('hide');
                            self.alertSuccess("Simulation '" + simulationName + "' deleted. Please wait until feed list" +
                                " updated");
                        },
                        function (data) {
                            deleteWizardError.text("Simulation '" + simulationName + "' deletion unsuccessful. "
                                + data.message);
                            deleteWizardError.show();
                        }
                    );
                });
            });

            self.$eventFeedForm.on('click', 'button[name="cancel"]', function () {
                if ("create" == self.$eventFeedForm.attr("mode")) {
                    if (self.isDirty == true) {
                        $('#clear_confirmation_modal_for_create').modal('show');
                    } else {
                        self.clearEventFeedForm();
                        self.$eventFeedForm.removeAttr("mode");
                        self.enableEditButtons();
                        self.enableCreateButtons(false);
                        $.sidebar_toggle('hide', '#left-sidebar-sub', '.simulation-list');
                        $("#create-simulation-modal-backdrop").remove();
                    }
                } else {
                    $('#clear_confirmation_modal').modal('show');
                }
            });
            $("#left-sidebar-sub").on('click', 'button.close-handle', function () {
                if (self.isDirty == false) {
                    self.clearEventFeedForm();
                    self.$eventFeedForm.removeAttr("mode");
                    self.enableEditButtons();
                    self.enableCreateButtons(false);
                    $.sidebar_toggle('hide', '#left-sidebar-sub', '.simulation-list');
                    $("#create-simulation-modal-backdrop").remove();
                } else if ("create" == self.$eventFeedForm.attr("mode")) {
                    self.isDirty = false;
                    $('#clear_confirmation_modal_for_create').modal('show');
                } else {
                    self.isDirty = false;
                    $('#clear_confirmation_modal').modal('show');
                }
            });

            $("#event-feed-configs").on('click', 'button[name="create-new-config"]', function () {
                self.clearEventFeedForm();
                $("#addNewSourceError").addClass("hidden");
                if ("create" == self.$eventFeedForm.attr("mode")) {
                    $('#clear_confirmation_modal_for_create').modal('show');
                } else {
                    $.sidebar_toggle('show', '#left-sidebar-sub', '.simulation-list');
                    self.addDateTimePickers();
                    self.$eventFeedForm.attr("mode", "create");
                    self.disableEditButtons();
                    self.disableCreateButtons(false);
                    self.addDynamicDefaultValues();
                    $("#event-feed-form").find('select[name="sources"]').val("Random");
                    $('<div class="modal-backdrop fade in" id="create-simulation-modal-backdrop"></div>').appendTo('body');
                }
            });

            $("#clear_confirmation_modal_for_create").on('click', 'button[name="confirm"]', function () {
                self.clearEventFeedForm();
                self.$eventFeedForm.removeAttr("mode");
                self.enableEditButtons();
                self.enableCreateButtons(false);
                $.sidebar_toggle('hide', '#left-sidebar-sub', '.simulation-list');
                $("#create-simulation-modal-backdrop").remove();
                self.isDirty = false;
            });

            $("#clear_confirmation_modal").on('click', 'button[name="confirm"]', function () {
                self.clearEventFeedForm();
                self.$eventFeedForm.removeAttr("mode");
                self.enableCreateButtons(false);
                self.enableEditButtons();
                $.sidebar_toggle('hide', '#left-sidebar-sub', '.simulation-list');
                $("#create-simulation-modal-backdrop").remove();
            });

            self.$eventFeedConfigTabContent.on('click', 'a[name="edit-source"]', function () {
                self.clearEventFeedForm();
                var $panel = $(this).closest('.input-group');
                var simulationName = $panel.attr('data-name');
                var editingActiveSimulation = true;
                var simulationConfig = self.activeSimulationList[simulationName];
                if (!simulationConfig) {
                    simulationConfig = self.inactiveSimulationList[simulationName];
                    editingActiveSimulation = false;
                }
                var $eventFeedForm = self.$eventFeedForm;
                if ("edit" == $eventFeedForm.attr("mode")) {
                    $('#clear_confirmation_modal').modal('show');
                    return;
                } else {
                    $.sidebar_toggle('show', '#left-sidebar-sub', '.simulation-list');
                    self.disableCreateButtons(false);
                    self.disableEditButtons();
                    if (editingActiveSimulation) {
                        self.activeSimulationList[self.getValue(simulationConfig.properties.simulationName)].editMode = true;
                    } else {
                        self.inactiveSimulationList[self.getValue(simulationConfig.properties.simulationName)].editMode = true;
                    }
                }

                $eventFeedForm.attr("mode", "edit");
                $eventFeedForm.find('input[name="simulation-name"]')
                    .val(self.getValue(simulationConfig.properties.simulationName));
                $eventFeedForm.find('input[name="start-timestamp"]')
                    .val(self.getValue(simulationConfig.properties.startTimestamp));
                $eventFeedForm.find('textarea[name="feed-description"]')
                    .val(self.getValue(simulationConfig.properties.description));
                $eventFeedForm.find('input[name="end-timestamp"]')
                    .val(self.getValue(simulationConfig.properties.endTimestamp));
                $eventFeedForm.find('input[name="no-of-events"]')
                    .val(self.getValue(simulationConfig.properties.noOfEvents));
                $eventFeedForm.find('input[name="time-interval"]')
                    .val(self.getValue(simulationConfig.properties.timeInterval));
                self.addDateTimePickers();
                var $sourceConfigs = $eventFeedForm.find('div.sourceConfigs');
                var sources = simulationConfig.sources;
                for (var i = 0; i < sources.length; i++) {
                    var source = sources[i];
                    var sourceSimulationType;
                    switch (source.simulationType) {
                        case 'CSV_SIMULATION':
                            sourceSimulationType = "CSV file";
                            break;
                        case 'DATABASE_SIMULATION':
                            sourceSimulationType = "Database";
                            break;
                        case 'RANDOM_DATA_SIMULATION':
                            sourceSimulationType = "Random";
                            break;
                    }
                    var sourcePanel = self.createConfigPanel(self.currentTotalSourceNum, self.dataCollapseNum,
                        sourceSimulationType);
                    $sourceConfigs.append(sourcePanel);
                    var sourceForm = self.createSourceForm(sourceSimulationType, self.currentTotalSourceNum);
                    var $sourceConfigBody = $sourceConfigs.find('div.source[data-uuid=' + self.currentTotalSourceNum + ']'
                        + ' div.panel-body');
                    $sourceConfigBody.append(sourceForm);
                    self.bindDynamicContent(sourceForm, sourceSimulationType, self.currentTotalSourceNum);
                    self.loadSiddhiAppNamesAndSelectOption(self.currentTotalSourceNum, source);
                }
            });

            $("#event-feed-form").on('click', 'button.delete-source', function () {
                var $sourceDiv = $(this).closest('div.source');
                var removingUuid = $sourceDiv.attr("data-uuid");
                $sourceDiv.remove();
                self.refreshSourcePanelHeadings(removingUuid);
                self.currentTotalSourceNum--;
                self.dataCollapseNum--;
                self.totalSourceNum--;
            });

            $("#event-feed-form").on('change', '.sourceConfigs div select[name="siddhi-app-name"]', function () {
                var $element = $(this);
                var $div = $element.closest('.sourceConfigForm');
                var uuid = $div.attr("data-uuid");
                var $streamNameSelect = $div.find('select[name="stream-name"]');
                $streamNameSelect.empty();
                var siddhiAppName = $element.val();
                var $panel = $element.closest('.panel-default');
                var $panelHeader = $panel.find('a[name="panel-header"]');
                var aLinkId = $panelHeader[0].id;
                var panelHeaderValue = "";
                panelHeaderValue = $('#' + aLinkId).find('span').text();
                var splitValues = panelHeaderValue.split(':');
                var newPanelHeader = "";
                newPanelHeader = splitValues[0].trim() + " : " + siddhiAppName;
                var truncatedValue = _.truncate(newPanelHeader, {'length': 55});
                $('#' + aLinkId).find('span').text(truncatedValue);

                $panel.find('h4').hover(function () {
                    $('#' + aLinkId).find('span').text(newPanelHeader);
                }, function () {
                    $('#' + aLinkId).find('span').text(truncatedValue);
                });

                var $siddhiAppMode = $div.find('div[data-name="siddhi-app-name-mode"]');
                $siddhiAppMode.html('mode : ' + self.siddhiAppDetailsMap[siddhiAppName]);
                if (self.siddhiAppDetailsMap[siddhiAppName] === self.FAULTY) {
                    $streamNameSelect.prop('disabled', true);
                } else {
                    $streamNameSelect.prop('disabled', false);
                    Simulator.retrieveStreamNames(
                        siddhiAppName,
                        function (data) {
                            self.refreshStreamList($streamNameSelect, data);
                            $div.find('div[class="dynamicToggleFormContent"]').show();
                        },
                        function (data) {
                            log.info(data);
                        });
                }
            });

            $("#event-feed-form").on('change', '.sourceConfigs div select[name="stream-name"]', function () {
                var $element = $(this);
                var $sourceConfigForm = $element.closest('.sourceConfigForm');
                var sourceUuid = $sourceConfigForm.attr('data-uuid');
                var streamName = $sourceConfigForm.find('select[name="stream-name"]').val();
                var $panel = $element.closest('.panel-default');
                var $panelHeader = $panel.find('a[name="panel-header"]');
                var aLinkId = $panelHeader[0].id;
                var panelHeaderValue = "";
                panelHeaderValue = $('#' + aLinkId).find('span').text();
                var splitValues = panelHeaderValue.split(':');
                var newPanelHeader = "";
                newPanelHeader = splitValues[0].trim() + " : " + splitValues[1].trim() + " : " + streamName;
                var truncatedValue = _.truncate(newPanelHeader, {'length': 55});
                $('#' + aLinkId).find('span').text(truncatedValue);

                $panel.find('h4').hover(function () {
                    $('#' + aLinkId).find('span').text(newPanelHeader);
                }, function () {
                    $('#' + aLinkId).find('span').text(truncatedValue);
                });

                Simulator.retrieveStreamAttributes(
                    $sourceConfigForm
                        .find('select[id="siddhi-app-name_' + sourceUuid + '"]')
                        .val(),
                    $sourceConfigForm
                        .find('select[name="stream-name"]')
                        .val(),
                    function (data) {
                        self.refreshAttributesList(sourceUuid, data);
                        var $attributes = $sourceConfigForm.find('input[id^="attributes"]');
                        $("#event-feed-form").find((':submit')).prop('disabled', false);
                        $attributes.each(function () {
                            $(this).on("change", function () {
                                self.addRulesForAttributes($sourceConfigForm);
                            });
                        });
                    },
                    function (data) {
                        log.info(data);
                    });
            });

            $("#event-feed-form").on('change', '.sourceConfigs div select[name="table-name"]', function () {
                var $element = $(this);
                self.loadColumnNames($element.closest('.sourceConfigForm'), $element.val());
            });

            //allow only one of timestamp options for csv source config
            $("#event-feed-form").on('click', 'input[name="timestamp-option"]', function () {
                var elementId = this.value;
                var form = $(this).closest('div.form-inline');
                var dataType = $(this).closest('div.sourceConfigForm').attr('data-type');
                var $timestampAttribute;
                if ('csv' == dataType) {
                    $timestampAttribute = form.find('input[name="timestamp-attribute"]');
                    $timestampAttribute.val(0);
                } else {
                    $timestampAttribute = form.find('select[name="timestamp-attribute"]');
                }
                var $timeInterval = form.find('input[name="timestamp-interval"]');

                var $sourceConfigForm = $(this).closest('div.sourceConfigForm');
                var $ordered = $sourceConfigForm.find('input[value="ordered"]');
                var $notordered = $sourceConfigForm.find('input[value="not-ordered"]');
                if (elementId == 'attribute') {
                    $timeInterval.prop('disabled', true).val('');
                    $timestampAttribute.prop('disabled', false);
                    $ordered.prop('disabled', false);
                    $ordered.prop("checked", true);
                    $notordered.prop("checked", false);
                    $notordered.prop('disabled', false);
                } else if (elementId == 'interval') {
                    $timeInterval.prop('disabled', false).val('1000');
                    $timestampAttribute.prop('disabled', true).val('');
                    $ordered.prop('disabled', true);
                    $notordered.prop('disabled', true);
                }
            });

            $("form#csv_upload_modal_form").on("click", "button.upload-csv", function (e) {
                var $element = $("form#csv_upload_modal_form");
                var formData = new FormData();
                formData.append('file', $element.find('input[type=file]')[0].files[0]);
                e.preventDefault();
                Simulator.uploadCSVFile(formData, function (data) {
                        log.info(data);
                        self.loadCSVFileNames(self.selectedSourceNum);
                        $('#csv_upload_modal').modal('hide');
                    },
                    function (data) {
                        log.error(data);
                    });
                return false;
            });

            $("#event-feed-form").on('click', 'button[name="loadDbConnection"]', function () {
                var $sourceConfigForm = $(this).closest('div.sourceConfigForm');
                self.selectedSourceNum = $sourceConfigForm.attr("data-uuid");
                $sourceConfigForm.find('.connectionSuccessMsg').html(self.generateConnectionMessage('connecting'));
                var connectionDetails = self.validateAndGetDbConfiguration($sourceConfigForm);
                if (null != connectionDetails) {
                    var $tableNames = $sourceConfigForm.find('select[name="table-name"]');
                    Simulator.testDatabaseConnectivity(
                        JSON.stringify(connectionDetails),
                        function (data) {
                            self.refreshTableNamesFromDataSource(connectionDetails, $tableNames);
                            $sourceConfigForm.find('.connectionSuccessMsg').html(self.generateConnectionMessage('success'));
                        },
                        function (msg) {
                            log.error(msg['responseText']);
                            $sourceConfigForm.find('.connectionSuccessMsg').html(self.generateConnectionMessage('failure'));
                        }
                    );
                }
            });

            // configure attribute configurations of random source
            $("#event-feed-form").on('change', 'select.feed-attribute-random', function () {
                var randomType = $(this).val();
                var $sourceConfigForm = $(this).closest('.sourceConfigForm');
                var dynamicId = $sourceConfigForm.attr('data-uuid');
                var attributeType = $(this).attr('data-type');
                var id = this.id;
                $sourceConfigForm.find('.' + id + '_config')
                    .html(self.generateRandomAttributeConfiguration(randomType, attributeType, dynamicId, id));
                // set the selected option of property based attribute configuration type (if any) to -1
                $sourceConfigForm.find('[class^="feed-attribute-random-' + dynamicId + '-property"]').each(function () {
                    $(this).prop('selectedIndex', -1);
                });
                // Get all input elements of new attribute and add validation rule
                var inputs = this.closest('div.form-group').getElementsByTagName('input');
                self.addSourceValuesValidation(inputs);
                // addRandomConfigTypeValidation(id);
            });
        };

        self.loadColumnNames = function ($sourceConfigForm, tableName) {
            var connectionDetails = self.validateAndGetDbConfiguration($sourceConfigForm);
            Simulator.retrieveColumnNames(
                JSON.stringify(connectionDetails),
                tableName,
                function (data) {
                    self.loadColumnNamesList(data, $sourceConfigForm);
                },
                function (msg) {
                    log.error(msg['responseText']);
                }
            );
        }

        self.addLoadingButton = function (selector) {
            selector.append('<div class="loader"></div>');
        };

        self.removeLoadingButton = function (selector) {
            selector.find('div[class="loader"]').remove();
        };

        // create a map containing siddhi app name
        self.createSiddhiAppMap = function (data) {
            self.siddhiAppDetailsMap = {};
            for (var i = 0; i < data.length; i++) {
                self.siddhiAppDetailsMap[data[i]['siddhiAppName']] = data[i]['mode'];
            }
        };
        // create the siddhi app name drop down
        self.refreshSiddhiAppList = function ($siddhiAppSelect, siddhiAppNames) {
            var initialOptionValue = '<option value = "-1" disabled>-- Please Select a Siddhi App --</option>';
            var newSiddhiApps = self.generateOptions(siddhiAppNames, initialOptionValue);
            $siddhiAppSelect.html(newSiddhiApps);
            $siddhiAppSelect.find('option[value="-1"]').attr("selected", true);
        };
        // select an option from the siddhi app name drop down
        self.selectSourceOptions = function ($siddhiAppSelect, siddhiAppName, initialLoading) {
            /*
             * if an siddhi app has been already selected when the siddhi app name list was refreshed,
             * check whether the siddhi app still exists in the workspace, if yes, make that siddhi app name the
             * selected value.
             * If the siddhi app no longer exists in the work space, set the selected option to -1 and refresh the form
             * */
            if (siddhiAppName in self.siddhiAppDetailsMap) {
                $siddhiAppSelect.val(siddhiAppName);
            } else {
                if (initialLoading !== undefined && !initialLoading) {
                    $siddhiAppSelect.prop('selectedIndex', -1);
                }
                if (siddhiAppName !== null) {
                    var $form = $siddhiAppSelect.closest('form[data-form-type="feed"]');
                    $form
                        .find('div[data-name="siddhi-app-name-mode"]')
                        .empty();
                    $form
                        .find('select[name="stream-name"]')
                        .empty()
                        .prop('disabled', true);
                    $form
                        .find('select[name="file-name"]')
                        .empty()
                        .prop('disabled', true);
                    $form
                        .find('input[name="timestamp-option"][value="attribute"]')
                        .prop('checked', false);
                    $form
                        .find('input[name="timestamp-attribute"]')
                        .empty();
                    $form
                        .find('input[name="timestamp-option"][value="interval"]')
                        .prop('checked', true);
                    $form
                        .find('input[name="timestamp-interval"]')
                        .val('1000');
                    $form
                        .find('input[name="ordered"][value="true"]')
                        .prop('checked', true);
                    $form
                        .find('input[name="ordered"][value="false"]')
                        .prop('checked', false);
                }
            }
        };

        // create jquery validators for single event forms
        self.addEventFeedFormValidator = function (uuid) {
            var $form = $('form[data-form-type="feed"][data-uuid="' + uuid + '"]');
            $form.validate();
            $form.find('[name="simulation-name"]').rules('add', {
                required: true,
                messages: {
                    required: "Please enter a simulation name."
                }
            });
        };

        // used to create options for available siddhi apps and streams
        self.generateOptions = function (dataArray, initialOptionValue) {
            dataArray.sort();
            var dataOption =
                '<option value = "{{dataName}}">' +
                '{{dataName}}' +
                '</option>';

            var result = '';
            if (initialOptionValue !== undefined) {
                result += initialOptionValue;
            }
            for (var i = 0; i < dataArray.length; i++) {
                result += dataOption.replaceAll('{{dataName}}', dataArray[i]);
            }
            return result;
        };

        self.bindDynamicContent = function (sourceForm, type, uniqueId) {

            if (type == "Random") {
                sourceForm.find("a[id='randomAdvanceConfigToggle_" + uniqueId + "']").on('click',
                    function () {
                        if ($(this).hasClass("active")) {
                            $(this).removeClass('active');
                        } else {
                            $(this).addClass("active");
                        }
                        var id = this.id;
                        var dynamicId = id.split("_")[1];
                        $("#randomAdvanceContent_" + dynamicId).toggle();

                    });
            } else if (type == "CSV file") {
                sourceForm.find("button[id='upload-csv-file_" + uniqueId + "']").on('click', function () {
                    var $element = $(this);
                    var $div = $element.closest('.sourceConfigForm');
                    self.selectedSourceNum = $div.attr("data-uuid");
                    $('#csv_upload_modal').modal('show');
                });

                sourceForm.find("a[id='csvAdvanceConfigToggle_" + uniqueId + "']").on('click',
                    function () {
                        if ($(this).hasClass("active")) {
                            $(this).removeClass('active');
                        } else {
                            $(this).addClass("active");
                        }
                        var id = this.id;
                        var dynamicId = id.split("_")[1];
                        $("#csvAdvanceContent_" + dynamicId).toggle();
                    });
            }
        };

        // remove the tab from the single event tabs list and remove its tab content
        self.removeEventFeedForm = function (ctx) {
            var x = $(ctx).parents("a").attr("href");
            var $current = $('#event-feed-config-tab-content ' + x);
            $(ctx)
                .parents('li')
                .prev()
                .addClass('active');
            $current
                .prev()
                .addClass('active');
            $current
                .remove();
            $(ctx)
                .parents("li")
                .remove();
        };

        /*
         * feed simulation functions
         * */

        self.createConfigPanel = function (totalSourceNum, dataCollapseNum, sourceType) {
            var panel =
                '<div class="panel panel-default source" data-uuid="{{totalSourceNum}}">' +
                '<div class="panel-heading feed-config" role="tab"> ' +
                '<h4 class="source-title panel-title" data-type="{{sourceType}}">' +
                '<a role="button" name="panel-header" data-toggle="collapse" data-parent="#source-accordion" ' +
                'href="#source_{{dataCollapseNum}}" id="simulationSource_{{dataCollapseNum}}" aria-expanded="true" ' +
                'aria-controls="source_{{dataCollapseNum}}">' +
                '<i class="fw fw-down pull-right"></i> <i class="fw fw-up pull-right"></i>' +
                '<button type = "button" class = "close pull-right delete-source"><i class="fw fw-delete"></i></button>' +
                '<span class="simulationHeader">{{sourceType}} Source {{totalSourceNum}}</span>' +
                '</a>' +
                '</h4>' +
                '</div>' +
                '<div class="panel-collapse collapse in" role="tabpanel" id="source_{{dataCollapseNum}}">' +
                '<div class="panel-body"></div> ' +
                '</div>' +
                '</div>';
            var temp = panel.replaceAll('{{totalSourceNum}}', totalSourceNum);
            var temp2 = temp.replaceAll('{{dataCollapseNum}}', dataCollapseNum);
            return temp2.replaceAll('{{sourceType}}', sourceType);

        };

        self.createSourceForm = function (sourceType, totalSourceNum) {
            switch (sourceType) {
                case 'CSV file':
                    var csvTemplate = $("#csvSourceConfig_dynamicId").clone();
                    csvTemplate.attr("id", "csvSourceConfig_" + totalSourceNum);
                    csvTemplate.attr("data-uuid", totalSourceNum);
                    csvTemplate.css("display", "block");
                    csvTemplate.html(csvTemplate.html().replaceAll('{{dynamicId}}', totalSourceNum));
                    csvTemplate.find(":input").change(function () {
                        self.isDirty = true;
                    });
                    return csvTemplate;
                case 'Database':
                    var dbTemplate = $("#dbSourceConfig_dynamicId").clone();
                    dbTemplate.attr("id", "dbSourceConfig_" + totalSourceNum);
                    dbTemplate.attr("data-uuid", totalSourceNum);
                    dbTemplate.css("display", "block");
                    dbTemplate.html(dbTemplate.html().replaceAll('{{dynamicId}}', totalSourceNum));
                    dbTemplate.find(":input").change(function () {
                        self.isDirty = true;
                    });
                    return dbTemplate;
                case 'Random':
                    var randomTemplate = $("#randomSourceConfig_dynamicId").clone();
                    randomTemplate.attr("id", "randomSourceConfig_" + totalSourceNum);
                    randomTemplate.attr("data-uuid", totalSourceNum);
                    randomTemplate.css("display", "block");
                    randomTemplate.find('a').attr("id", "randomAdvanceConfigToggle_" + totalSourceNum);
                    randomTemplate.find('div[id="randomAdvanceContent_dynamicId"]').attr("id",
                        "randomAdvanceContent_" + totalSourceNum);
                    randomTemplate.html(randomTemplate.html().replaceAll('{{dynamicId}}', totalSourceNum));
                    return randomTemplate;
            }
        };

        // refresh the remaining source config panel headings once a source is deleted
        self.refreshSourcePanelHeadings = function (removedUUID) {
            $('.sourceConfigs div.source').each(function (i) {
                var $source = $(this);
                var uuid = $source.attr("data-uuid");
                if (uuid > removedUUID) {
                    uuid--;
                    $source.attr("data-uuid", uuid);
                    var sourceTitle = $source.find('h4').text();
                    var regexp = /(.*)-(.*)/g;
                    var match = regexp.exec(sourceTitle);
                    $source.find('h4 a').contents().last().replaceWith('Source ' + uuid + ' - ' + match[2]);
                }
            });
        };

        // remove jquery validators for deleted feed config form
        self.removeSourceConfigValidation = function (sourceType, dynamicId) {
            switch (sourceType) {
                case 'CSV file':
                    removeCSVSourceConfigValidation(dynamicId);
                    break;
                case 'Database':
                    removeDBSourceConfigValidation(dynamicId);
                    break;
                case 'Random':
                    removeRandomConfigTypeValidation(dynamicId);
                    break;
            }
        };

        // remove jquery validators for deleted csv source config
        self.removeCSVSourceConfigValidation = function (dynamicId) {
            $('#executionPlanName_' + dynamicId).rules('remove');
            $('#streamName_' + dynamicId).rules('remove');
            $('#fileName_' + dynamicId).rules('remove');
            $('#timestampAttribute_' + dynamicId).rules('remove');
            $('#timestampInterval_' + dynamicId).rules('remove');
            $('#delimiter_' + dynamicId).rules('remove');
            self.removeCSVSourceAttributeConfigValidation(dynamicId);
        };

        // remove jquery validators for deleted db source config
        self.removeDBSourceConfigValidation = function (dynamicId) {
            $('#dataSourceLocation_' + dynamicId).rules('remove');
            $('#driver_' + dynamicId).rules('remove');
            $('#username_' + dynamicId).rules('remove');
            $('#password_' + dynamicId).rules('remove');
            $('#tableName_' + dynamicId).rules('remove');
        };

        // remove jquery validators for deleted random source config
        self.removeRandomSourceConfigValidation = function (dynamicId) {
            $('#timestampInterval_' + dynamicId).rules('remove');
            self.removeRandomConfigTypeValidation(dynamicId);
        };

        // remove jquery validators of random source attribute config
        self.removeRandomConfigTypeValidation = function (dynamicId) {
            $('.feed-attribute-random-' + dynamicId).each(function () {
                var id = this.id;
                self.removeRulesOfAttribute(this);
                $('input[id^="' + id + '_"], select[id^="' + id + '_"]').each(function () {
                    self.removeRulesOfAttribute(this);
                });
            })
        };

        // remove validation rule of an attribute
        self.removeRulesOfAttribute = function (ctx) {
            $(ctx).rules('remove');
        };

        // remove jquery validators of csv source indices
        self.removeCSVSourceAttributeConfigValidation = function (dynamicId) {
            $('.feed-attribute-csv-' + dynamicId).each(function () {
                self.removeRulesOfAttribute(this);
            })
        };

        // load execution plan names to form
        self.loadSiddhiAppNames = function (elementId) {
            var $siddhiAppSelect = $('div[data-uuid="' + elementId + '"] select[id="siddhi-app-name_' + elementId + '"]');
            var siddhiAppName = $siddhiAppSelect.val();
            Simulator.retrieveSiddhiAppNames(
                function (data) {
                    self.createSiddhiAppMap(data);
                    self.refreshSiddhiAppList($siddhiAppSelect, Object.keys(self.siddhiAppDetailsMap));
                    self.selectSourceOptions($siddhiAppSelect, siddhiAppName, true);
                },
                function (data) {
                    log.info(data);
                }
            );
        };

        // load execution plan names to form
        self.loadSiddhiAppNamesAndSelectOption = function (elementId, source) {
            var $siddhiAppSelect = $('div[data-uuid="' + elementId + '"] select[id="siddhi-app-name_' + elementId + '"]');
            var $siddhiAppMode = $('div[data-uuid="' + elementId + '"] div[data-name="siddhi-app-name-mode"]');
            var $streamNameSelect = $('div[data-uuid="' + elementId + '"] select[name="stream-name"]');
            var siddhiAppName = $siddhiAppSelect.val();
            Simulator.retrieveSiddhiAppNames(
                function (data) {
                    self.createSiddhiAppMap(data);
                    self.refreshSiddhiAppList($siddhiAppSelect, Object.keys(self.siddhiAppDetailsMap));
                    self.selectSourceOptions($siddhiAppSelect, siddhiAppName);
                    if (source.siddhiAppName in self.siddhiAppDetailsMap) {
                        $siddhiAppSelect.val(source.siddhiAppName).change();
                        $siddhiAppMode.html('mode : ' + self.siddhiAppDetailsMap[source.siddhiAppName]);
                        if (self.siddhiAppDetailsMap[source.siddhiAppName] === self.FAULTY) {
                            $streamNameSelect.prop('disabled', true);
                        } else {
                            $streamNameSelect.prop('disabled', false);
                            Simulator.retrieveStreamNames(
                                source.siddhiAppName,
                                function (data) {
                                    self.refreshStreamList($streamNameSelect, data);
                                    $streamNameSelect.val(source.streamName).change();
                                    Simulator.retrieveStreamAttributes(
                                        source.siddhiAppName,
                                        source.streamName,
                                        function (data) {
                                            self.refreshAttributesList(elementId, data);
                                            var $sourceConfigForm = $('div.sourceConfigForm[data-uuid="' + elementId + '"]');
                                            if ("CSV_SIMULATION" == source.simulationType) {
                                                if (source.indices !== undefined) {
                                                    var indices = source.indices.split(",");
                                                    var i = 0;
                                                    var $attributes = $sourceConfigForm.find('input[id^="attributes"]');
                                                    $attributes.each(function () {
                                                        $(this).val(indices[i]);
                                                        i++;
                                                        $(this).on("change", function () {
                                                            self.addRulesForAttributes($sourceConfigForm);
                                                        });
                                                    });
                                                }
                                                self.loadCSVFileNamesAndSelectOption(self.totalSourceNum, source.fileName);
                                                var $timestampIndex = $sourceConfigForm.find('input[value="attribute"]');
                                                var $timestampInteval = $sourceConfigForm.find('input[value="interval"]');
                                                var $ordered = $sourceConfigForm.find('input[value="ordered"]');
                                                var $notordered = $sourceConfigForm.find('input[value="not-ordered"]');
                                                var $timestampAttribute = $sourceConfigForm
                                                    .find('input[name="timestamp-attribute"]');
                                                var $timeInterval = $sourceConfigForm
                                                    .find('input[name="timestamp-interval"]');
                                                if (source.timestampInterval && 0 != source.timestampInterval.length) {
                                                    $timeInterval.prop('disabled', false);
                                                    $timeInterval.val(source.timestampInterval);
                                                    $timestampAttribute.prop('disabled', true).val('');
                                                    $ordered.prop('disabled', true);
                                                    $notordered.prop('disabled', true);
                                                    $timestampIndex.prop("checked", false);
                                                    $timestampInteval.prop("checked", true);
                                                } else {
                                                    $timestampAttribute.prop('disabled', false)
                                                        .val(source.timestampAttribute);
                                                    $timeInterval.prop('disabled', true).val('');
                                                    $ordered.prop('disabled', false);
                                                    $notordered.prop('disabled', false);
                                                    $timestampIndex.prop("checked", true);
                                                    $timestampInteval.prop("checked", false);
                                                    if (source.isOrdered) {
                                                        $ordered.prop("checked", true);
                                                    } else {
                                                        $timestampAttribute.prop('disabled', false)
                                                            .val(source.timestampAttribute);
                                                        $timeInterval.prop('disabled', true).val('');
                                                        $ordered.prop('disabled', false);
                                                        $notordered.prop('disabled', false);
                                                        $timestampIndex.prop("checked", true);
                                                        $timestampInteval.prop("checked", false);
                                                        if (source.isOrdered) {
                                                            $ordered.prop("checked", true);
                                                        } else {
                                                            $notordered.prop("checked", true);
                                                        }
                                                    }
                                                }
                                                $sourceConfigForm.find('input[name="delimiter"]').val(source.delimiter);
                                                self.addSourceConfigValidation(source.simulationType,
                                                    self.currentTotalSourceNum);
                                            } else if ("DATABASE_SIMULATION" == source.simulationType) {
                                                $sourceConfigForm.find('input[name="data-source-location"]')
                                                    .val(source.dataSourceLocation);
                                                $sourceConfigForm.find('input[name="driver-class"]').val(source.driver);
                                                $sourceConfigForm.find('input[name="username"]').val(source.username);
                                                $sourceConfigForm.find('input[name="password"]').val(source.password);
                                                var $timestampIndex = $sourceConfigForm.find('input[value="attribute"]');
                                                var $timestampInterval = $sourceConfigForm.find('input[value="interval"]');
                                                var $timestampAttribute = $sourceConfigForm
                                                    .find('input[name="timestamp-attribute"]');
                                                var $timeInterval = $sourceConfigForm
                                                    .find('input[name="timestamp-interval"]')
                                                var connectionDetails = self
                                                    .validateAndGetDbConfiguration($sourceConfigForm);
                                                var connectionStatus = "success";
                                                if (null != connectionDetails) {
                                                    var $tableNames = $sourceConfigForm.find('select[name="table-name"]');
                                                    $(this).prop('disabled', true);
                                                    Simulator.testDatabaseConnectivity(
                                                        JSON.stringify(connectionDetails),
                                                        function (data) {
                                                            // self.refreshTableNamesFromDataSource(connectionDetails, $tableNames);
                                                            Simulator.retrieveTableNames(
                                                                JSON.stringify(connectionDetails),
                                                                function (data) {
                                                                    $tableNames.html(self.generateOptions(data));
                                                                    $tableNames.prop("selectedIndex", -1);
                                                                    $tableNames.find('option').eq($tableNames
                                                                        .find('option[value="' + source.tableName + '"]').index())
                                                                        .prop('selected', true);
                                                                    $tableNames.val(source.tableName).change();
                                                                },
                                                                function (msg) {
                                                                    log.error(msg['responseText']);
                                                                }
                                                            );
                                                            $sourceConfigForm.find('.connectionSuccessMsg')
                                                                .html(self.generateConnectionMessage('success'));
                                                            Simulator.retrieveColumnNames(
                                                                JSON.stringify(connectionDetails),
                                                                source.tableName,
                                                                function (data) {
                                                                    self.loadColumnNamesListAndSelect(data, $sourceConfigForm,
                                                                        source.columnNamesList.split(","));
                                                                    var $timestampIndex = $sourceConfigForm.find('input[value="attribute"]');
                                                                    var $timestampInteval = $sourceConfigForm.find('input[value="interval"]');
                                                                    var $timestampAttribute = $sourceConfigForm.find('input[name="timestamp-attribute"]');
                                                                    var $timeInterval = $sourceConfigForm.find('input[name="timestamp-interval"]');
                                                                    if (source.timestampInterval && 0 != source.timestampInterval.length) {
                                                                        $timeInterval.prop('disabled', false);
                                                                        $timeInterval.val(source.timestampInterval);
                                                                        $timestampAttribute.prop('disabled', true).val('');
                                                                        $timestampIndex.prop("checked", false);
                                                                        $timestampInteval.prop("checked", true);
                                                                    } else {
                                                                        var $timestampAtt = $sourceConfigForm
                                                                            .find('select[name="timestamp-attribute"]');
                                                                        $timestampAtt.find('option').eq($timestampAtt
                                                                            .find('option[value="'
                                                                                + source.timestampAttribute + '"]').index())
                                                                            .prop('selected', true);
                                                                        $timestampAttribute.prop('disabled', false);
                                                                        $timeInterval.prop('disabled', true).val('');
                                                                        $timestampIndex.prop("checked", true);
                                                                        $timestampInteval.prop("checked", false);
                                                                    }
                                                                },
                                                                function (msg) {
                                                                    log.error(msg['responseText']);
                                                                }
                                                            );
                                                        },
                                                        function (msg) {
                                                            log.error(msg);
                                                            connectionStatus = "error";
                                                            $sourceConfigForm.find('.connectionSuccessMsg')
                                                                .html(self.generateConnectionMessage('editFailure'));
                                                            var tableOption =
                                                                '<option value = "' + source.tableName + '">'
                                                                + source.tableName + '</option>';
                                                            $tableNames.html(tableOption);
                                                            $tableNames.attr('disabled', true);
                                                            var i = 0;
                                                            var selectedValueList = source.columnNamesList.split(",");
                                                            $sourceConfigForm.find('.feed-attribute-db').each(function () {
                                                                var columnOption =
                                                                    '<option value = "' + selectedValueList[i] + '">'
                                                                    + selectedValueList[i] + '</option>';
                                                                $(this).html(columnOption);
                                                                $(this).attr('disabled', true);
                                                                i++;
                                                            });
                                                            if (source.timeInterval && 0 != source.timeInterval.length) {
                                                                $timeInterval.attr('disabled', true);
                                                                $timeInterval.val(source.timeInterval);
                                                                $timestampAttribute.prop('disabled', true).val('');
                                                                $timestampIndex.prop("checked", false);
                                                                $timestampInterval.prop("checked", true);
                                                            } else {
                                                                var $timestampAtt = $sourceConfigForm
                                                                    .find('select[name="timestamp-attribute"]');
                                                                if (connectionStatus == "success") {
                                                                    $timestampAtt.find('option')
                                                                        .eq($timestampAtt.find('option[value="'
                                                                            + source.timestampAttribute + '"]').index())
                                                                        .prop('selected', true);
                                                                } else {
                                                                    var attributeOption =
                                                                        '<option value = "'
                                                                        + source.timestampAttribute + '">'
                                                                        + source.timestampAttribute + '</option>';
                                                                    $timestampAtt.html(attributeOption);
                                                                }
                                                                $timestampAttribute.attr('disabled', true);
                                                                $timeInterval.prop('disabled', true).val('');
                                                                $timestampIndex.prop("checked", true);
                                                                $timestampInterval.prop("checked", false);
                                                            }
                                                            $timestampIndex.attr('disabled', true);
                                                            $timestampInterval.attr('disabled', true);
                                                        }
                                                    );
                                                }

                                            } else if ("RANDOM_DATA_SIMULATION" == source.simulationType) {
                                                var attributeConfiguration = source.attributeConfiguration;
                                                var $attributesDivs = $sourceConfigForm
                                                    .find('div.attributes-section label[for^="attributes_"]')
                                                    .closest('div');
                                                var i = 0;
                                                $attributesDivs.each(function () {
                                                    var attributeConfig = attributeConfiguration[i];
                                                    var $attributesDiv = $(this);
                                                    var $attributeSelect = $attributesDiv
                                                        .find('select[name^="attributes"]');
                                                    var attributeType = $attributeSelect.attr('data-type');
                                                    var attributeName = $attributeSelect.attr('name')
                                                        .replaceAll('attributes_', '');
                                                    var id = this.id;
                                                    var $selectType = $attributesDiv.find('select[id^="attributes_"]');
                                                    if ("CUSTOM_DATA_BASED" == attributeConfig.type) {
                                                        $selectType.find('option').eq($selectType
                                                            .find('option[value="custom"]').index()).prop('selected', true);
                                                        $sourceConfigForm
                                                            .find('.attributes_' + attributeName + '_config')
                                                            .html(self.generateRandomAttributeConfiguration(
                                                                "custom", attributeType, elementId, id));
                                                        $attributesDiv.find('input[data-type="custom"]')
                                                            .val(attributeConfig.list);
                                                    } else if ("PRIMITIVE_BASED" == attributeConfig.type) {
                                                        $selectType.find('option').eq($selectType
                                                            .find('option[value="primitive"]').index())
                                                            .prop('selected', true);
                                                        var attDataType = attributeConfig.primitiveType;
                                                        $sourceConfigForm.find('.attributes_' + attributeName + '_config')
                                                            .html(self.generateRandomAttributeConfiguration("primitive",
                                                                attributeType, elementId, id));
                                                        if ("BOOL" == attDataType) {

                                                        } else if ("STRING" == attDataType) {
                                                            $attributesDiv.find('input[name$="_primitive_length"]')
                                                                .val(attributeConfig.length);
                                                        } else if ("INT" == attDataType || "LONG" == attDataType) {
                                                            $attributesDiv.find('input[name$="_primitive_min"]')
                                                                .val(attributeConfig.min);
                                                            $attributesDiv.find('input[name$="_primitive_max"]')
                                                                .val(attributeConfig.max);
                                                        } else if ("FLOAT" == attDataType || "DOUBLE" == attDataType) {
                                                            $attributesDiv.find('input[name$="_primitive_min"]')
                                                                .val(attributeConfig.min);
                                                            $attributesDiv.find('input[name$="_primitive_max"]')
                                                                .val(attributeConfig.max);
                                                            $attributesDiv.find('input[name$="_primitive_precision"]')
                                                                .val(attributeConfig.precision);
                                                        }
                                                    } else if ("PROPERTY_BASED" == attributeConfig.type) {
                                                        $selectType.find('option')
                                                            .eq($selectType.find('option[value="property"]').index())
                                                            .prop('selected', true);
                                                        $sourceConfigForm.find('.attributes_' + attributeName + '_config')
                                                            .html(self.generateRandomAttributeConfiguration("property",
                                                                attributeType, elementId, id));
                                                        $attributesDiv.find('select[name$="_property"]')
                                                            .val(attributeConfig.property);
                                                    } else if ("REGEX_BASED" == attributeConfig.type) {
                                                        $selectType.find('option').eq($selectType
                                                            .find('option[value="regex"]').index()).prop('selected', true);
                                                        $sourceConfigForm.find('.attributes_' + attributeName + '_config')
                                                            .html(self.generateRandomAttributeConfiguration("regex",
                                                                attributeType, elementId, id));
                                                        $attributesDiv.find('input[name$="_regex"]')
                                                            .val(attributeConfig.pattern);
                                                    }
                                                    i++;
                                                });
                                            }
                                        },
                                        function (data) {
                                            log.info(data);
                                        });
                                },
                                function (data) {
                                    log.info(data);
                                });
                        }
                    }

                },
                function (data) {
                    log.info(data);
                }
            );
            self.currentTotalSourceNum++;
            self.dataCollapseNum++;
            self.totalSourceNum++;
            self.addAllSourceValuesValidation();
        };

        self.loadCSVFileNames = function (dynamicId, initialLoading) {
            var $csvFileSelect = $('div[data-uuid="' + dynamicId + '"] select[name="file-name"]');
            Simulator.retrieveCSVFileNames(
                function (data) {
                    self.refreshCSVFileList($csvFileSelect, data);
                    if (initialLoading !== undefined && !initialLoading) {
                        $csvFileSelect.prop("selectedIndex", -1);
                    }
                },
                function (data) {
                    log.error(data);
                });
        };

        self.loadCSVFileNamesAndSelectOption = function (dynamicId, selectedFileName) {
            var $csvFileSelect = $('div[data-uuid="' + dynamicId + '"] select[name="file-name"]');
            Simulator.retrieveCSVFileNames(
                function (data) {
                    self.refreshCSVFileList($csvFileSelect, data);
                    $csvFileSelect.find('option').eq($csvFileSelect.find('option[value="' + selectedFileName + '"]')
                        .index()).prop('selected', true);
                },
                function (data) {
                    log.error(data);
                });
        };

        self.refreshCSVFileList = function ($csvFileSelect, csvFileNames) {
            var options = self.generateOptions(csvFileNames);
            var isNotUploaded = false;
            if (csvFileNames.length == 0) {
                isNotUploaded = true;
                options += '<option value = "-1" disabled>No Uploaded CSV file available</option>';
            }
            $csvFileSelect.html(options);
            if (isNotUploaded) {
                $csvFileSelect.find('option[value="-1"]').attr("selected", true);
            } else {
                $csvFileSelect.find('option:eq(0)').prop('selected', true);
            }
        };

        self.refreshStreamList = function ($streamNameSelect, streamNames) {
            var initialOptionValue = '<option value = "-1" disabled>-- Please Select a Stream --</option>';
            var newStreamOptions = self.generateOptions(streamNames, initialOptionValue);
            $streamNameSelect.html(newStreamOptions);
            $streamNameSelect.find('option[value="-1"]').attr("selected", true);
        };

        self.generateConnectionMessage = function (status) {
            var connectingMsg =
                '<div id="connectionSuccessMsg" class="text-muted">' +
                '<label>Attempting to connect to datasource...</label>' +
                '</div>';
            var successMsg =
                '<div id="connectionSuccessMsg" class="text-success">' +
                '<label>Successfully connected</label>' +
                '</div>';

            var failureMsg =
                '<div id="connectionSuccessMsg" class="text-danger">' +
                '<label>Connection failed. Edit latter part of config after successful connection</label>' +
                '</div>';

            var editFailureMsg =
                '<div id="connectionSuccessMsg" class="text-danger">' +
                '<label>Connection failed. Edit latter part of config after successful connection</label>' +
                '</div>';

            switch (status) {
                case 'connecting':
                    return connectingMsg;
                case 'success':
                    return successMsg;
                case 'failure':
                    return failureMsg;
                case 'editFailure':
                    return editFailureMsg;
            }
        };

        //generate input fields for attributes
        self.refreshTableNamesFromDataSource = function (connectionDetails, $tableNames) {
            Simulator.retrieveTableNames(
                JSON.stringify(connectionDetails),
                function (data) {
                    $tableNames.html(self.generateOptions(data));
                    $tableNames.prop("selectedIndex", -1);
                },
                function (msg) {
                    log.error(msg['responseText']);
                }
            )
        };

        self.addSourceConfigValidation = function (sourceType, dynamicId) {
            var $sourceConfigForm = $('form.feedSimulationConfig');
            $sourceConfigForm.validate();
            $sourceConfigForm.find('select[id="siddhi-app-name_' + dynamicId + '"]').rules('add', {
                required: true,
                messages: {
                    required: "Please select a Siddhi App name."
                }
            });
            $sourceConfigForm.find('select[name="stream-name"]').rules('add', {
                required: true,
                messages: {
                    required: "Please select a stream name."
                }
            });
            switch (sourceType) {
                case 'CSV file':
                    self.addCSVSourceConfigValidation($sourceConfigForm);
                    break;
                case 'Database':
                    self.addDBSourceConfigValidation($sourceConfigForm);
                    break;
                case 'Random':
                    // no specific validations required
                    break;
            }
        };

        self.addCSVSourceConfigValidation = function ($sourceConfigForm) {
            $sourceConfigForm.find('select[name="file-name"]').rules('add', {
                required: true,
                messages: {
                    required: "Please select a CSV file."
                }
            });
            $sourceConfigForm.find('input[name="timestamp-attribute"]').rules('add', {
                required: true,
                digits: true,
                messages: {
                    digits: "Timestamp index must be a positive integer."
                }
            });
            $sourceConfigForm.find('input[name="timestamp-interval"]').rules('add', {
                required: true,
                digits: true,
                messages: {
                    digits: "Timestamp index must be a positive integer."
                }
            });
            $sourceConfigForm.find('input[name="delimiter"]').rules('add', {
                required: true,
                messages: {
                    required: "Please specify a delimiter."
                }
            });
        };

        // create jquery validators for db source config
        self.addDBSourceConfigValidation = function ($sourceConfigForm) {
            $sourceConfigForm.find('input[name="data-source-location"]').rules('add', {
                required: true,
                messages: {
                    required: "Please specify a datasource location."
                }
            });
            $sourceConfigForm.find('input[name="driver-class"]').rules('add', {
                required: true,
                messages: {
                    required: "Please specify a driver class. eg: com.mysql.jdbc.Driver"
                }
            });
            $sourceConfigForm.find('input[name="username"]').rules('add', {
                required: true,
                messages: {
                    required: "Please specify a username."
                }
            });
            $sourceConfigForm.find('input[name="password"]').rules('add', {
                required: true,
                messages: {
                    required: "Please specify a password."
                }
            });
            $sourceConfigForm.find('select[name="table-name"]').rules('add', {
                required: true,
                messages: {
                    required: "Please select a table name."
                }
            });
        };

        self.addAllSourceValuesValidation = function () {
            $("#source-accordion div.attributes-section input[type=text]").each(function () {
                $(this).rules('add', {
                    required: true,
                    messages: {
                        required: "This field can not be empty"
                    }
                });
            });
        }

        self.addSourceValuesValidation = function (inputs) {
            $(inputs).each(function () {
                $(this).rules('add', {
                    required: true,
                    messages: {
                        required: "This field can not be empty"
                    }
                });
            });
        }

        self.refreshAttributesList = function (uuid, streamAttributes) {
            var $attributesDiv = $('div.sourceConfigForm[data-uuid="' + uuid + '"] div.attributes-section');
            var dataType = $('div.sourceConfigForm[data-uuid=' + uuid + ']').attr('data-type');
            $attributesDiv.html(self.generateAttributesDivForSource(dataType));
            var attributes = self.generateAttributesListForSource(dataType, streamAttributes, uuid);
            $attributesDiv.html(attributes);
            var $sourceConfig = $('div.sourceConfigForm[data-uuid="' + uuid + '"]');
            //this will trigger default primitive selection
            if (dataType == "random") {
                for (var i = 0; i < streamAttributes.length; i++) {
                    var dynamicSelectBoxId = "attributes_" + streamAttributes[i]['name'];
                    $attributesDiv.find('select[id="' + dynamicSelectBoxId + '"]').val('primitive').change();
                }
            } else if (dataType == "db") {
                var $timestampIndex = $sourceConfig.find('input[value="attribute"]');
                var $timestampInteval = $sourceConfig.find('input[value="interval"]');
                $timestampIndex.prop("checked", false);
                $timestampInteval.prop("checked", true);
                var $timeInterval = $sourceConfig.find('input[name="timestamp-interval"]');
                $timeInterval.val(1000);
                self.loadColumnNames($sourceConfig, $sourceConfig.find('select[name="table-name"]').val());
            } else {
                var $timestampIndex = $sourceConfig.find('input[value="attribute"]');
                var $timestampInteval = $sourceConfig.find('input[value="interval"]');
                $timestampIndex.prop("checked", false);
                $timestampInteval.prop("checked", true);
                var $ordered = $sourceConfig.find('input[value="ordered"]');
                var $notordered = $sourceConfig.find('input[value="not-ordered"]');
                $ordered.prop('disabled', true);
                $notordered.prop('disabled', true);
                var $timeInterval = $sourceConfig.find('input[name="timestamp-interval"]');
                $timeInterval.val(1000);
                var $timeIndex = $sourceConfig.find('input[name="timestamp-attribute"]');
                $timeIndex.prop('disabled', true);
            }
        };

        // add rules for attribute
        self.addRulesForAttributes = function ($sourceConfigForm) {
            var $attributes = $sourceConfigForm.find('input[id^="attributes"]');
            var sourceType = $sourceConfigForm.attr('data-type');

            var attributesFilled = false;
            $attributes.each(function () {
                if ("" != $(this).val()) {
                    attributesFilled = true;
                }
            });

            if ('csv' == sourceType) {
                $attributes.each(
                    function () {
                        $(this).rules('add', {
                            required: attributesFilled,
                            digits: true,
                            messages: {
                                required: "Please enter an index number to match the attribute."
                            }
                        });
                    }
                );
            } else {
                $attributes.each(
                    function () {
                        $(this).rules('add', {
                            required: attributesFilled,
                            messages: {
                                required: "Please enter table column name to match the attribute."
                            }
                        });
                    }
                );
            }


        };

        self.generateAttributesDivForSource = function (dataType) {
            var csv =
                '<div class="form-group">' +
                '   <label>Indices</label>' +
                '   <div id="attributes">' +
                '   </div> ' +
                '</div>';
            var db =
                '<div class="form-group">' +
                '   <label>Columns List</label>' +
                '   <div id="attributes">' +
                '   </div> ' +
                '</div>';
            var random =
                '<div class="form-group">' +
                '   <label>Attribute Configuration</label>' +
                '   <div id="attributes">' +
                '   </div>' +
                '</div>';

            switch (dataType) {
                case 'csv':
                    return csv;
                case 'db':
                    return db;
                case 'random':
                    return random;
            }

        };

        self.generateAttributesListForSource = function (dataType, attributes, uuid) {
            var csvAttribute =
                '<div class="form-group">' +
                '   <label for ="attributes_{{attributeName}}_{{id}}">' +
                '        {{attributeName}}({{attributeType}})' +
                '   </label>' +
                '       <input type="text" class="feed-attribute-csv form-control"' +
                '       name="attributes_{{attributeName}}_{{id}}" value="{{defaultVal}}" ' +
                '       id="attributes_{{attributeName}}_{{id}}"' +
                '       data-type ="{{attributeType}}">' +
                '</div>';
            var dbAttribute =
                '<div class="form-group">' +
                '   <label for ="attributes_{{attributeName}}_{{id}}">' +
                '       {{attributeName}}({{attributeType}})' +
                '   </label>' +
                '       <select id="attributes_{{attributeName}}_{{id}}"' +
                '       name="attributes_{{attributeName}}_{{id}}" ' +
                '       class="feed-attribute-db form-control" ' +
                '       data-type="{{attributeType}}"> ' +
                '       </select>' +
                '</div>';
            var randomAttribute =
                '<div class="form-group">' +
                '   <label for ="attributes_{{attributeName}}_{{id}}">' +
                '       {{attributeName}}({{attributeType}})' +
                '   </label>' +
                '           <select id="attributes_{{attributeName}}_{{id}}"' +
                '           name="attributes_{{attributeName}}_{{id}}" ' +
                '           class="feed-attribute-random form-control"' +
                '           data-type ="{{attributeType}}"> ' +
                '              <option disabled selected value> -- select an configuration type -- </option>' +
                '              <option value="custom">Static value</option>' +
                '              <option value="primitive">Primitive based</option>' +
                '              <option value="property">Property based </option>' +
                '              <option value="regex">Regex based</option>' +
                '           </select>' +
                '   <div class ="attributes_{{attributeName}}_{{id}}_config">' +
                '   </div> ' +
                '</div>';

            var result = "";
            if (dataType == "csv") {
                result = '<span class="helper">Column Index mapping</span>';
            }

            for (var i = 0; i < attributes.length; i++) {
                var temp;
                switch (dataType) {
                    case 'csv':
                        temp = csvAttribute.replaceAll('{{attributeName}}', attributes[i]['name']);
                        temp = temp.replaceAll('{{defaultVal}}', i);
                        result += temp.replaceAll('{{attributeType}}', attributes[i]['type']);
                        break;
                    case 'db':
                        temp = dbAttribute.replaceAll('{{attributeName}}', attributes[i]['name']);
                        result += temp.replaceAll('{{attributeType}}', attributes[i]['type']);
                        break;
                    case 'random':
                        temp = randomAttribute.replaceAll('{{attributeName}}', attributes[i]['name']);
                        result += temp.replaceAll('{{attributeType}}', attributes[i]['type']);
                        break;
                }
            }
            result = result.replaceAll("{{id}}", uuid);
            return result;
        };

        self.validateAndGetDbConfiguration = function ($sourceConfigForm) {
            var dataSourceLocation = $sourceConfigForm.find('input[name="data-source-location"]').val();
            if (dataSourceLocation === null || dataSourceLocation.length === 0) {
                log.error("Datasource location is required to test database connection")
            }
            var driverName = $sourceConfigForm.find('input[name="driver-class"]').val();
            if (driverName === null || driverName.length === 0) {
                log.error("Driver is required to test database connection")
            }
            var username = $sourceConfigForm.find('input[name="username"]').val();
            if (username === null || username.length === 0) {
                log.error("Driver is required to test database connection")
            }
            var password = $sourceConfigForm.find('input[name="password"]').val();
            if (password === null || password.length === 0) {
                log.error("Password is required to test database connection")
            }

            if (dataSourceLocation !== null && dataSourceLocation.length > 0
                && driverName !== null && driverName.length > 0
                && username !== null && username.length > 0
                && password !== null && password.length > 0) {
                var connectionDetails = {};
                connectionDetails['driver'] = driverName;
                connectionDetails['dataSourceLocation'] = dataSourceLocation;
                connectionDetails['username'] = username;
                connectionDetails['password'] = password;
                return connectionDetails;
            }
            return null;
        };

        //generate input fields for attributes
        self.loadColumnNamesList = function (columnNames, $sourceConfigForm) {
            var columnsList = self.generateOptions(columnNames);
            $sourceConfigForm.find('.feed-attribute-db').each(function () {
                $(this).html(columnsList);
                $(this).prop("selectedIndex", -1);
            });
        };

        //generate input fields for attributes
        self.loadColumnNamesListAndSelect = function (columnNames, $sourceConfigForm, selectedValueList) {
            var columnsList = self.generateOptions(columnNames);
            var i = 0;
            $sourceConfigForm.find('.feed-attribute-db').each(function () {
                $(this).html(columnsList);
                $(this).find('option').eq($(this).find('option[value="' + selectedValueList[i] + '"]').index())
                    .prop('selected', true);
                i++;
            });
        };

        self.getCSVSimulationCongig = function ($sourceCOnfig) {
            var source = {};
            source.simulationType = "CSV_SIMULATION";
            source.executionPlanName = $sourceCOnfig.find('select[name="siddhi-app-name"]').val();
            source.streamName = $sourceCOnfig.find('select[name="stream-name"]').val();
            source.fileName = $sourceCOnfig.find('select[name="file-name"]').val();
        };

        // generate input fields to provide configuration for random generation type (factory method)
        self.generateRandomAttributeConfiguration = function (randomType, attributeType, dynamicId, parentId) {

            switch (randomType) {
                case 'custom' :
                    return self.generateCustomBasedAttributeConfiguration(attributeType, parentId);
                case 'primitive':
                    return self.generatePrimitiveBasedAttributeConfiguration(attributeType, parentId);
                case 'property':
                    return self.generatePropertyBasedAttributeConfiguration(attributeType, parentId);
                case 'regex' :
                    return self.generateRegexBasedAttributeConfiguration(attributeType, parentId);
            }
        };

        // generate input fields to provide configuration for 'custom based' random generation type
        self.generateCustomBasedAttributeConfiguration = function (attrType, parentId) {

            var staticValue = "";

            switch (attrType) {
                case 'BOOL':
                    staticValue = "FALSE";
                    break;
                case 'STRING':
                    staticValue = "Data" + Math.floor(Math.random() * (100 - 1 + 1)) + 1;
                    break;
                case 'INT':
                    staticValue = Math.floor(Math.random() * (100 - 1 + 1)) + 1;
                    break;
                case 'LONG':
                    staticValue = Math.floor(Math.random() * (100 - 1 + 1)) + 1;
                    break;
                case 'FLOAT':
                case 'DOUBLE':
                    staticValue = Math.random() * (100 - 0) + 0;
                    break;
            }
            var custom =
                '<div class="add-margin-top-1x">' +
                '<label>' +
                'Data' +
                '</label>' +
                '<input type="text" class="form-control" value="' + staticValue + '" name="' + parentId + '_custom"' +
                'data-type ="custom">' +
                '</div>';
            return custom;
        };


        // generate input fields to provide configuration for 'primitive based' random generation type
        self.generatePrimitiveBasedAttributeConfiguration = function (attrType, parentId) {

            var bool =
                '<div>' +
                '<span class="helper color-grey" id="{{parentId}}_primitive_bool">' +
                'No primitive based configuration required for attribute type \'BOOL\'.' +
                '</span>' +
                '</div>';

            var length =
                '<div class="add-margin-top-1x">' +
                '<div class="row">' +
                '<div class="col-md-6">' +
                '<label>' +
                'String Length' +
                '</label>' +
                '<input type="text" class="form-control" value="5" name="{{parentId}}_primitive_length" ' +
                'data-type="numeric">' +
                '</div>' +
                '</div>' +
                '</div>';

            var min =
                '<div class="add-margin-top-1x">' +
                '<div class="row">' +
                '<div class="col-md-6">' +
                '<label>' +
                'From' +
                '</label>' +
                '<input type="text" class="form-control" value="0" name="{{parentId}}_primitive_min" ' +
                'data-type="{{attributeType}}">' +
                '</div>';

            var max =
                '<div class="col-md-6">' +
                '<label>' +
                'Less than' +
                '</label>' +
                '<input type="text" class="form-control" value="999" name="{{parentId}}_primitive_max" ' +
                'data-type="{{attributeType}}">' +
                '</div>' +
                '</div>' +
                '</div>';

            var precision =
                '<div class="add-margin-top-1x">' +
                '<div class="row">' +
                '<div class="col-md-6">' +
                '<label>' +
                'Number of Decimals' +
                '</label>' +
                '<input type="text" class="form-control" value ="2" name="{{parentId}}_primitive_precision" ' +
                'data-type="numeric">' +
                '</div>' +
                '</div>' +
                '</div>';

            var temp = '';

            switch (attrType) {
                case 'BOOL':
                    temp = bool;
                    break;
                case 'STRING':
                    temp = length;
                    break;
                case 'INT':
                case 'LONG':
                    temp = min;
                    temp += max;
                    break;
                case 'FLOAT':
                case 'DOUBLE':
                    temp = min;
                    temp += max;
                    temp += precision;
                    break;
            }
            var temp1 = temp.replaceAll('{{attributeType}}', attrType.toLowerCase());
            return temp1.replaceAll('{{parentId}}', parentId);
        };

        // generate input fields to provide configuration for 'property based' random generation type
        self.generatePropertyBasedAttributeConfiguration = function (attrType, parentId) {
            var propertyStartingTag =
                '<div class="add-margin-top-1x">' +
                '<label>' +
                'Type' +
                '</label>' +
                '<select name="{{parentId}}_property" class="feed-attribute-random-property form-control" ' +
                'data-type="property"> ';

            var propertyEndingTag =
                '</select>' +
                '</div>';

            var temp = propertyStartingTag;
            temp += this.refreshPropertyBasedOptionsList(attrType);
            temp += propertyEndingTag;
            return temp.replaceAll('{{parentId}}', parentId);
        };

        //refresh the list of property based random generation options
        self.refreshPropertyBasedOptionsList = function (attrType) {
            var properties = "";

            switch (attrType) {
                case 'BOOL':
                    properties = self.propertyBasedGenerationOptionsForString;
                    break;
                case 'STRING':
                    properties = self.propertyBasedGenerationOptionsForString;
                    break;
                case 'INT':
                    properties = self.propertyBasedGenerationOptionsForInt;
                    break;
                case 'LONG':
                    properties = self.propertyBasedGenerationOptionsForLong;
                    break;
                case 'FLOAT':
                    properties = self.propertyBasedGenerationOptionsForFloat;
                    break;
                case 'DOUBLE':
                    properties = self.propertyBasedGenerationOptionsForDouble;
                    break;
            }
            return self.generateOptions(properties);
        };

        // generate input fields to provide configuration for 'regex based' random generation type
        self.generateRegexBasedAttributeConfiguration = function (attrType, parentId) {

            var defaultValue = "";
            var boolRegex = "(?i)(true|false)";
            var stringRegex = "[A-Z]([a-z]){4}";
            var intRegex = "[0-9]{3}";
            var longRegex = "-?[0-9]{1,19}";
            var floatRegex = "[+-]?([0-9]*[.])?[0-9]+";
            var doubleRegex = "[0-9]{1,13}(\\.[0-9]*)?";

            switch (attrType) {
                case 'BOOL':
                    defaultValue = boolRegex;
                    break;
                case 'STRING':
                    defaultValue = stringRegex;
                    break;
                case 'INT':
                    defaultValue = intRegex;
                    break;
                case 'LONG':
                    defaultValue = longRegex;
                    break;
                case 'FLOAT':
                    defaultValue = floatRegex;
                    break;
                case 'DOUBLE':
                    defaultValue = doubleRegex;
                    break;
            }

            var temp =
                '<div class="add-margin-top-1x">' +
                '<label>' +
                'Pattern' +
                '</label>' +
                '<input type="text" class="form-control" value="' + defaultValue + '" name="{{parentId}}_regex"' +
                'data-type="regex">' +
                '</div>';
            return temp.replaceAll('{{parentId}}', parentId);
        };

        self.addAvailableFeedSimulations = function () {
            Simulator.getFeedSimulations(
                function (data) {
                    var simulations = JSON.parse(data.message);
                    var activeSimulations = simulations.activeSimulations;
                    if (0 == activeSimulations.length) {
                        $("#active-simulation-list").hide();
                    } else {
                        $("#active-simulation-list").show();
                    }
                    for (var i = 0; i < activeSimulations.length; i++) {
                        self.addActiveSimulationToUi(activeSimulations[i]);
                    }
                    var inActiveSimulations = simulations.inActiveSimulations;
                    if (0 == inActiveSimulations.length) {
                        $("#inactive-simulation-list").hide();
                    } else {
                        $("#inactive-simulation-list").show();
                    }
                    for (var i = 0; i < inActiveSimulations.length; i++) {
                        self.addInActiveSimulationToUi(inActiveSimulations[i]);
                    }
                    self.removeUnavailableSimulationsFromUi(simulations);
                },
                function (msg) {
                    log.error(msg['responseText']);
                }
            );
        };

        self.removeUnavailableSimulationsFromUi = function (simulations) {
            var activeSimulations = simulations.activeSimulations;
            var simulationName, i;
            for (var simulationName in self.activeSimulationList) {
                if (self.activeSimulationList.hasOwnProperty(simulationName)) {
                    var foundSimulaion = false;
                    for (i = 0; i < activeSimulations.length; i++) {
                        if (activeSimulations[i].properties.simulationName == simulationName) {
                            foundSimulaion = true;
                        }
                    }
                    if (!foundSimulaion) {
                        self.$eventFeedConfigTabContent.find('div[data-name="' + simulationName + '"]').remove();
                        delete self.activeSimulationList[simulationName];
                    }
                }
            }

            var inActiveSimulations = simulations.inActiveSimulations;

            for (var simulationName in self.inactiveSimulationList) {
                if (self.inactiveSimulationList.hasOwnProperty(simulationName)) {
                    var foundSimulaion = false;
                    for (i = 0; i < inActiveSimulations.length; i++) {
                        if (inActiveSimulations[i].properties.simulationName == simulationName) {
                            foundSimulaion = true;
                        }
                    }
                    if (!foundSimulaion) {
                        self.$eventFeedConfigTabContent.find('div[data-name="' + simulationName + '"]').remove();
                        delete self.inactiveSimulationList[simulationName];
                    }
                }
            }
        };

        self.addActiveSimulationToUi = function (simulation) {
            var simulationName = simulation.properties.simulationName;
            if (simulationName in self.inactiveSimulationList) {
                self.$eventFeedConfigTabContent.find('div[data-name="' + simulation.properties.simulationName + '"]')
                    .remove();
                delete self.inactiveSimulationList[simulationName];
            }
            if (0 ===
                self.$eventFeedConfigTabContent.find('div[data-name="' + simulation.properties.simulationName + '"]')
                    .length) {
                self.$eventFeedConfigTabContent.find('div[data-name="' + simulation.properties.simulationName + '"]')
                    .remove();
                var simulationDiv =
                    '<div class="input-group" data-name="' + simulation.properties.simulationName + '">' +
                    '<span class="form-control">' +
                    '<span class="simulation-name">' + simulation.properties.simulationName + '</span>' +
                    '<span class="simulator-tools pull-right">' +
                    '<a title="Start"><i class="fw fw-start"></i></a>' +
                    '<a class="hidden" title="Resume"><i class="fw fw-resume"></i></a>' +
                    '<a class="hidden" title="Pause"><i class="fw fw-assign fw-rotate-90"></i></a>' +
                    '<a class="hidden" title="Stop"><i class="fw fw-stop"></i></a>' +
                    '</span>' +
                    '</span>' +
                    '<div class="input-group-btn">' +
                    '<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown"' +
                    ' aria-haspopup="true" aria-expanded="false">' +
                    '<i class="fw fw-ellipsis fw-rotate-90"></i>' +
                    '<span class="sr-only">Toggle Dropdown Menu</span>' +
                    '</button>' +
                    '<ul class="dropdown-menu dropdown-menu-right">' +
                    '<li><a name="edit-source">' +
                    'Edit</a>' +
                    '</li>' +
                    '<li><a name="delete-source">Delete</a></li>' +
                    '</ul>' +
                    '</div>' +
                    '</div>';
                self.$eventFeedConfigTabContent.find("#active-simulation-list").append(simulationDiv);

                if (_.isEmpty(self.activeSimulationList)) {
                    $("#active-simulation-list").show();
                }
                self.activeSimulationList[simulationName] = simulation;
                self.activeSimulationList[simulationName].status = "STOP";
            }
        };

        self.addInActiveSimulationToUi = function (simulation) {
            var simulationName = simulation.properties.simulationName;
            if (simulationName in self.activeSimulationList) {
                self.$eventFeedConfigTabContent.find('div[data-name="' + simulation.properties.simulationName + '"]')
                    .remove();
                delete self.activeSimulationList[simulationName];
            }
            if (0 ===
                self.$eventFeedConfigTabContent.find('div[data-name="' + simulation.properties.simulationName + '"]')
                    .length) {
                self.inactiveSimulationList[simulationName] = simulation;
                self.inactiveSimulationList[simulationName].status = "STOP";
                self.$eventFeedConfigTabContent.find('div[data-name="' + simulation.properties.simulationName + '"]')
                    .remove();
                var simulationDiv =
                    '<div class="input-group" data-name="' + simulation.properties.simulationName + '">' +
                    '<span class="form-control" data-toggle="tooltip" title="' + simulation.exceptionReason + '">' +
                    '<span class="simulation-name">' + simulation.properties.simulationName + '</span>' +
                    '</span>' +
                    '<div class="input-group-btn">' +
                    '<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown"' +
                    ' aria-haspopup="true" aria-expanded="false">' +
                    '<i class="fw fw-ellipsis fw-rotate-90"></i>' +
                    '<span class="sr-only">Toggle Dropdown Menu</span>' +
                    '</button>' +
                    '<ul class="dropdown-menu dropdown-menu-right">' +
                    '<li><a name="edit-source" data-toggle="sidebar" data-target="#left-sidebar-sub" ' +
                    'aria-expanded="false">' +
                    'Edit</a>' +
                    '</li>' +
                    '<li><a name="delete-source">Delete</a></li>' +
                    '</ul>' +
                    '</div>' +
                    '</div>';
                $('[data-toggle="tooltip"]').tooltip();
                self.$eventFeedConfigTabContent.find("#inactive-simulation-list").append(simulationDiv);
            }
        };

        self.checkSimulationStatus = function ($panel, simulationName, isInitialStart) {
            Simulator.getFeedSimulationStatus(
                simulationName,
                function (data) {
                    var status = data.message;
                    if ((!isInitialStart && "STOP" === status) && "RUN" === self.activeSimulationList[simulationName].status
                        || "RESUME" === self.activeSimulationList[simulationName].status) {
                        $panel.find('i.fw-start').closest('a').removeClass("hidden");
                        $panel.find('i.fw-assign').closest('a').addClass("hidden");
                        $panel.find('i.fw-resume').closest('a').addClass("hidden");
                        $panel.find('i.fw-stop').closest('a').addClass("hidden");
                        $panel.find('div.input-group-btn button.dropdown-toggle').prop('disabled', false);
                        self.activeSimulationList[simulationName].status = "STOP";
                        var message = {
                            "type": "INFO",
                            "message": "Event Simulation finished for \"" + simulationName + "\"."
                        };
                        self.console.println(message);
                    } else if (!("STOP" === status && "STOP" === self.activeSimulationList[simulationName].status)) {
                        setTimeout(function () {
                            self.checkSimulationStatus($panel, simulationName, false)
                        }, 3000);
                    }
                },
                function (data) {
                    log.error(data);
                }
            );
        };

        self.getValue = function (value) {
            if (value == null) {
                return "";
            }
            return value;
        };

        self.clearEventFeedForm = function () {
            var $eventFeedForm = $('#event-feed-form');
            $eventFeedForm.find('input[name="simulation-name"]').val('');
            $eventFeedForm.find('input[name="start-timestamp"]').val('');
            $eventFeedForm.find('textarea[name="feed-description"]').val('');
            $eventFeedForm.find('input[name="end-timestamp"]').val('');
            $eventFeedForm.find('input[name="no-of-events"]').val('');
            $eventFeedForm.find('input[name="time-interval"]').val('');
            $eventFeedForm.find('div.sourceConfigs').empty();
            self.currentTotalSourceNum = 1;
            self.dataCollapseNum = 1;
            self.totalSourceNum = 1;
        };

        self.addDateTimePickers = function () {
            var $startTimestamp = $('#event-feed-form input[name="start-timestamp"]');
            $startTimestamp.datetimepicker({
                controlType: myControl,
                showSecond: true,
                showMillisec: true,
                dateFormat: 'yy-mm-dd',
                timeFormat: 'HH:mm:ss:l',
                showOn: 'button',
                buttonText: '<span class="fw-stack"><i class="fw fw-square-outline fw-stack-2x"></i>' +
                '<i class="fw fw-calendar fw-stack-1x"></i><span class="fw-stack fw-move-right fw-move-bottom">' +
                '<i class="fw fw-circle fw-stack-2x fw-stroke"></i><i class="fw fw-clock fw-stack-2x fw-inverse"></i>' +
                '</span></span>',
                onSelect: self.convertDateToUnix,
                onClose: self.closeTimestampPicker
            });
            var $endTimestamp = $('#event-feed-form input[name="end-timestamp"]');
            $endTimestamp.datetimepicker({
                controlType: myControl,
                showSecond: true,
                showMillisec: true,
                dateFormat: 'yy-mm-dd',
                timeFormat: 'HH:mm:ss:l',
                showOn: 'button',
                buttonText: '<span class="fw-stack"><i class="fw fw-square-outline fw-stack-2x"></i>' +
                '<i class="fw fw-calendar fw-stack-1x"></i><span class="fw-stack fw-move-right fw-move-bottom">' +
                '<i class="fw fw-circle fw-stack-2x fw-stroke"></i><i class="fw fw-clock fw-stack-2x fw-inverse"></i>' +
                '</span></span>',
                onSelect: self.convertDateToUnix,
                onClose: self.closeTimestampPicker
            });
        };

        // add a datetimepicker to an element
        var myControl = {
            create: function (tp_inst, obj, unit, val, min, max, step) {
                $('<input class="ui-timepicker-input" value="' + val + '" style="width:50%">')
                    .appendTo(obj)
                    .spinner({
                        min: min,
                        max: max,
                        step: step,
                        change: function (e, ui) {
                            if (e.originalEvent !== undefined)
                                tp_inst._onTimeChange();
                            tp_inst._onSelectHandler();
                        },
                        spin: function (e, ui) { // spin events
                            tp_inst.control.value(tp_inst, obj, unit, ui.value);
                            tp_inst._onTimeChange();
                            tp_inst._onSelectHandler();
                        }
                    });
                return obj;
            },
            options: function (tp_inst, obj, unit, opts, val) {
                if (typeof(opts) == 'string' && val !== undefined)
                    return obj.find('.ui-timepicker-input').spinner(opts, val);
                return obj.find('.ui-timepicker-input').spinner(opts);
            },
            value: function (tp_inst, obj, unit, val) {
                if (val !== undefined)
                    return obj.find('.ui-timepicker-input').spinner('value', val);
                return obj.find('.ui-timepicker-input').spinner('value');
            }
        };

        // convert the date string in to unix timestamp onSelect
        self.convertDateToUnix = function () {
            var $element = $(this);
            var $form = $element.closest('form[data-form-type="single"]');
            if (self.siddhiAppDetailsMap[$form.find('select[name="siddhi-app-name"]').val()] !== self.FAULTY) {
                var date = $element.val();
                var patt = new RegExp("^((\\d)+||NaN)$");
                if (patt.test(date)) {
                    return;
                }
                var dateParts = date.split(/[^0-9]/);
                var time = new Date(dateParts[0], dateParts[1] - 1, dateParts[2], dateParts[3], dateParts[4], dateParts[5]).getTime()
                    + parseInt(dateParts[6]);
                $element.val(time);
            } else {
                $element.val('');
            }
        };

        // check whether the timestamp value is a unix timestamp onClose, if not convert date string into unix timestamp
        self.closeTimestampPicker = function () {
            var $element = $(this);
            var $form = $element.closest('form[data-form-type="single"]');
            if (self.siddhiAppDetailsMap[$form.find('select[name="siddhi-app-name"]').val()] !== self.FAULTY) {
                if ($element
                        .val()
                        .includes('-')) {
                    var date = $element.val();
                    var patt = new RegExp("^((\\d)+||NaN)$");
                    if (patt.test(date)) {
                        return;
                    }
                    var dateParts = date.split(/[^0-9]/);
                    var time = new Date(dateParts[0], dateParts[1] - 1, dateParts[2], dateParts[3],
                            dateParts[4], dateParts[5]).getTime()
                        + parseInt(dateParts[6]);
                    $element.val(time);
                }
            } else {
                $element
                    .val('');
            }
        };

        self.pollingSimulation = function () {
            if (self.$eventFeedTab.attr("aria-expanded") && self.$eventFeedTab.closest('li').hasClass('active')) {
                self.addAvailableFeedSimulations();
                setTimeout(self.pollingSimulation, 5000);
            } else {
                setTimeout(self.pollingSimulation, 5000);
            }
        };

        self.disableEditButtons = function () {
            $('div.simulation-list button.dropdown-toggle').each(function () {
                $(this).prop('disabled', true);
            });
        };
        self.disableCreateButtons = function (onlyCreateButton) {
            var createButton = $("#event-feed-configs button.sidebar");
            createButton.prop('disabled', true);
            if (!onlyCreateButton) {
                $(".event-simulator-activate-btn").addClass('disabled')
            }
        };

        self.enableEditButtons = function () {
            $('div.simulation-list button.dropdown-toggle').each(function () {
                $(this).prop('disabled', false);
            });
        };
        self.enableCreateButtons = function (onlyCreateButton) {
            var createButton = $("#event-feed-configs button.sidebar");
            createButton.prop('disabled', false);
            if (!onlyCreateButton) {
                $(".event-simulator-activate-btn").removeClass('disabled');
            }
        };

        self.addDynamicDefaultValues = function () {
            Simulator.getFeedSimulations(
                function (data) {
                    var simulations = JSON.parse(data.message);
                    var activeSimulations = simulations.activeSimulations.length;
                    var inactiveSimulations = simulations.inActiveSimulations.length;
                    self.numOfFeedSimulations = activeSimulations + inactiveSimulations;
                    self.form.find('input[name="simulation-name"]').val("Feed Simulation " + ++self.numOfFeedSimulations);
                    self.form.find('input[name="time-interval"]').val(1000);
                },
                function (data) {
                    log.info("Error retrieving data from backend " + data);
                }
            );
        };

        self.createSiddhiAppList = function (siddhiAppName) {
            var siddhiAppName =
                '<div class="siddhi_app_mode_config">' +
                '<div class="clearfix app-list">' +
                '<label class="siddhi_app_name col-md-6">' + siddhiAppName + '</label>' +
                '</div></div>';
            return siddhiAppName;
        };

        self.simulateFeed = function (simulationName, $panel) {
            Simulator.simulationAction(
                simulationName,
                "run",
                function (data) {
                    $panel.find('i.fw-start').closest('a').addClass("hidden");
                    $panel.find('i.fw-assign').closest('a').removeClass("hidden");
                    $panel.find('i.fw-stop').closest('a').removeClass("hidden");
                    $panel.find('div.input-group-btn button.dropdown-toggle').prop('disabled', true);

                    self.activeSimulationList[simulationName].status = "RUN";
                    var consoleListManager = self.app.outputController;
                    var console = consoleListManager.getGlobalConsole();
                    var message = {
                        "type": "INFO",
                        "message": "" + simulationName + " simulation started Successfully!"
                    };
                    if (self.console == undefined && console == undefined) {
                        var consoleOptions = {};
                        var options = {};
                        _.set(options, '_type', "CONSOLE");
                        _.set(options, 'title', "Console");
                        _.set(options, 'statusForCurrentFocusedFile', "simulation");
                        _.set(options, 'message', message);
                        _.set(consoleOptions, 'consoleOptions', options);
                        console = consoleListManager.newConsole(consoleOptions);
                    } else {
                        console.println(message);
                    }
                    self.console = console;
                    self.alertSuccess(simulationName + " simulation started Successfully!");
                    self.checkSimulationStatus($panel, simulationName, true);
                },
                function (data) {
                    var responseObj = JSON.parse(data.responseText);
                    var message = {
                        "type": "ERROR",
                        "message": responseObj.message
                    };
                    var consoleListManager = self.app.outputController;
                    var console = consoleListManager.getGlobalConsole();
                    if (self.console == undefined && console == undefined) {
                        var consoleOptions = {};
                        var options = {};
                        _.set(options, '_type', "CONSOLE");
                        _.set(options, 'title', "Console");
                        _.set(options, 'statusForCurrentFocusedFile', "simulation");
                        _.set(options, 'message', message);
                        _.set(consoleOptions, 'consoleOptions', options);
                        console = consoleListManager.newConsole(consoleOptions);
                    } else {
                        console.println(message);
                    }
                    self.console = console;
                }
            );
        };


        self.alertSuccess = function (successMessage) {
            var successNotification = getSuccessNotification(successMessage);
            self.notification_container.append(successNotification);
            successNotification.fadeTo(2000, 200).slideUp(1000, function () {
                successNotification.slideUp(1000);
            });
        };
        self.alertError = function (errorMessage) {
            var errorNotification = getErrorNotification(errorMessage);
            self.notification_container.append(errorNotification);
            errorNotification.fadeTo(2000, 200).slideUp(1000, function () {
                errorNotification.slideUp(1000);
            });
        };
        self.alertWarning = function (warningMessage) {
            var warningNotification = getWarningNotification(warningMessage);
            self.notification_container.append(warningNotification);
            warningNotification.fadeTo(2000, 200).slideUp(1000, function () {
                warningNotification.slideUp(1000);
            });
        };

        self.stopRunningSimulationOnStartup = function () {
            Simulator.getFeedSimulations(
                function (data) {
                    var simulations = JSON.parse(data.message);
                    var activeSimulations = simulations.activeSimulations;
                    for (var i = 0; i < activeSimulations.length; i++) {
                        var simulationName = activeSimulations[i].properties.simulationName;
                        Simulator.getFeedSimulationStatus(
                            simulationName,
                            function (data) {
                                var status = data.message;
                                if ("RUN" == status) {
                                    Simulator.simulationAction(
                                        simulationName,
                                        "stop",
                                        function (data) {
                                            console.log(data);
                                        },
                                        function (data) {
                                            console.log(data);
                                        },
                                        false
                                    );
                                } else {

                                }
                            },
                            function (data) {
                                console.log(data);
                            },
                            false
                        );
                    }
                },
                function (data) {
                    console.log(data);
                }
            );
        };

        self.stopRunningSimulationOnSiddhiAppStop = function (siddhiApp) {
            for (var simulationName in self.activeSimulationList) {
                if (self.activeSimulationList.hasOwnProperty(simulationName)) {
                    var simulationConfigs = self.activeSimulationList[simulationName].sources;
                    var simulatorStatus = self.activeSimulationList[simulationName].status;
                    for (var i = 0; i < simulationConfigs.length; i++) {
                        var siddhiAppName = simulationConfigs[i].siddhiAppName;
                        if (siddhiAppName == siddhiApp && ( simulatorStatus == "RUN" || simulatorStatus == "PAUSE" )) {
                            Simulator.simulationAction(
                                simulationName,
                                "stop",
                                function (data) {
                                    self.alertWarning("'" + simulationName + "' simulation stopped due to termination of" +
                                        " Siddhi app: " + siddhiApp);
                                },
                                function (data) {
                                    self.alertError("'" + simulationName + "' simulation could not stopped when stopping" +
                                        " Siddhi app: '" + siddhiApp + "'. Reason: " + data);
                                },
                                false
                            );
                        }
                    }
                }
            }
        };

        self.updateFeedCreationButtonAndNotification = function () {
            Simulator.retrieveSiddhiAppNames(
                function (data) {
                    var numOfSiddhiApps = data.length;
                    if (numOfSiddhiApps == 0) {
                        $('#createFeedSimulationNotification').show();
                        self.disableCreateButtons(true);
                    } else {
                        $('#createFeedSimulationNotification').hide();
                        self.enableCreateButtons(true);
                    }
                },
                function (data) {
                    log.error("Error in retrieving back end data " + data);
                }
            );

        };

        function getWarningNotification(warningMessage) {
            return $(
                "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-warning' id='error-alert'>" +
                "<span class='notification'>" +
                warningMessage +
                "</span>" +
                "</div>");
        };

        function getErrorNotification(errorMessage) {
            return $(
                "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-danger' id='error-alert'>" +
                "<span class='notification'>" +
                errorMessage +
                "</span>" +
                "</div>");
        };

        function getSuccessNotification(successMessage) {
            return $(
                "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-success' id='success-alert'>" +
                "<span class='notification'>" +
                successMessage +
                "</span>" +
                "</div>");
        };

        function getSimulationDeleteConfirmation(simulationName) {
            return $(
                "<div class='modal fade' id='deleteSimulationModal' tabindex='-1' role='dialog' aria-tydden='true'>" +
                "<div class='modal-dialog file-dialog' role='document'>" +
                "<div class='modal-content'>" +
                "<div class='modal-header'>" +
                "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                "<span aria-hidden='true'>&times;</span>" +
                "</button>" +
                "<h4 class='modal-title file-dialog-title' id='newConfigModalLabel'>Delete simulation config<" +
                "/h4>" +
                "<hr class='style1'>" +
                "</div>" +
                "<div class='modal-body'>" +
                "<div class='container-fluid'>" +
                "<form class='form-horizontal' onsubmit='return false'>" +
                "<div class='form-group'>" +
                "<label for='configName' class='col-sm-9 file-dialog-label'>" +
                "Are you sure to delete Simulation Configuration: " + simulationName + "" +
                "</label>" +
                "</div>" +
                "<div class='form-group'>" +
                "<div class='file-dialog-form-btn'>" +
                "<button id='deleteButton' type='button' class='btn btn-primary'>delete" +
                "</button>" +
                "<div class='divider'/>" +
                "<button type='cancelButton' class='btn btn-default' data-dismiss='modal'>cancel</button>" +
                "</div>" +
                "</form>" +
                "<div id='deleteWizardError' class='alert alert-danger'>" +
                "<strong>Error!</strong> Something went wrong." +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>"
            );
        }

        return self;
    });
