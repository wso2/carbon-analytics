/*
 ~   Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 ~
 ~   Licensed under the Apache License, Version 2.0 (the "License");
 ~   you may not use this file except in compliance with the License.
 ~   You may obtain a copy of the License at
 ~
 ~        http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~   Unless required by applicable law or agreed to in writing, software
 ~   distributed under the License is distributed on an "AS IS" BASIS,
 ~   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~   See the License for the specific language governing permissions and
 ~   limitations under the License.
 */

define(['jquery', 'log', './simulator-rest-client', 'lodash', /* void libs */'bootstrap', 'theme_wso2', 'jquery_ui',
    'jquery_validate', 'jquery_timepicker', './templates'], function ($, log, Simulator, _) {

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

        self.siddhiAppDetailsMap = {};
        self.eventFeedForm = $('#event-feed-form').find('form').clone();
        self.$eventFeedConfigTab = $("#event-feed-config-tab");
        self.$eventFeedConfigTabContent = $(".simulation-list");

        self.FAULTY = 'FAULTY';
        self.STOP = 'STOP';
        self.RUN = 'RUN';
        self.DEBUG = 'DEBUG';
        self.app = _.get(config, 'application');

        self.activeSimulationList = {};
        self.inactiveSimulationList = {};

        self.propertyBasedGenerationOptions = ['TIME_12H', 'TIME_24H',
            'SECOND', 'MINUTE', 'MONTH',
            'MONTH_NUM', 'YEAR', 'DAY',
            'DAY_OF_WEEK', 'DATE', 'FULL_NAME',
            'FIRST_NAME', 'LAST_NAME', 'WORDS',
            'BSN', 'ADDRESS', 'EMAIL',
            'PHONE_NUM', 'POST_CODE', 'STATE',
            'CITY', 'COMPANY', 'COUNTRY',
            'STREET_NAME', 'HOUSE_NO', 'HEIGHT_CM',
            'HEIGHT_M', 'WEIGHT', 'OCCUPATION',
            'IBAN', 'BIC', 'VISA_CARD', 'PIN_CODE',
            'URL',
            'IP',
            'IP_V6',
            'MAC_ADDRESS',
            'UUID',
            'USERNAME',
            'COLOUR',
            'ALTITUDE',
            'DEPTH',
            'COORDINATES',
            'LATITUDE',
            'LONGITUDE',
            'GEO_HASH',
            'SENTENCE',
            'PARAGRAPH'
        ];

        self.pollingSimulation();
        
        var $form = $('form.feedSimulationConfig');
        $form.validate({
            ignore: false,
            invalidHandler: function (e, validator) {
                for (var i = 0; i < validator.errorList.length; i++) {
                    $(validator.errorList[i].element).closest('div.panel-collapse.collapse[id^="source_"]').collapse('show');
                }
            }
        });
        $form.find('input[name="simulation-name"]').rules('add', {
            required: true,
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

        $("#event-feed-configs").on('click', 'button.sidebar', function () {
            if ("false" == $(this).attr("aria-expanded")) {
                self.addDateTimePickers();
            }
        });

        $("#event-feed-form").on('submit', 'form.feedSimulationConfig', function () {
            var simulation = {};
            var properties = {};
            properties.simulationName = $form.find('input[name="simulation-name"]').val();
            properties.startTimestamp = $form.find('input[name="start-timestamp"]').val();
            properties.endTimestamp = $form.find('input[name="end-timestamp"]').val();
            properties.noOfEvents = $form.find('input[name="no-of-events"]').val();
            properties.description = $form.find('input[name="feed-description"]').val();
            properties.timeInterval = $form.find('input[name="time-interval"]').val();
            simulation.properties = properties;
            var sources = [];
            $('div.sourceConfigs div.source').each(function () {
                var $sourceConfigForm = $(this).find('.sourceConfigForm');
                var sourceType = $sourceConfigForm.attr('data-type');
                var source = {};
                source.siddhiAppName = $sourceConfigForm.find('select[name="siddhi-app-name"]').val();
                source.streamName = $sourceConfigForm.find('select[name="stream-name"]').val();
                source.timeInterval = $sourceConfigForm.find('input[name="timestamp-interval"]').val();
                var indices;
                var $attributes;
                if ('csv' == sourceType) {
                    source.simulationType = "CSV_SIMULATION";
                    source.fileName = $sourceConfigForm.find('select[name="file-name"]').val();
                    source.delimiter = $sourceConfigForm.find('input[name="delimiter"]').val();
                    if ($sourceConfigForm.find('input[name="timestamp-attribute"]').is(':disabled')) {
                        source.isOrdered = true;
                        source.timeInterval = $sourceConfigForm.find('input[name="timestamp-interval"]').val();
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
                        source.timestampInterval = $sourceConfigForm.find('select[name="timestamp-interval"]').val();
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
                    source.timestampInterval = $sourceConfigForm.find('select[name="timestamp-interval"]').val();
                    source.attributeConfiguration = [];
                    var $attributesDivs = $sourceConfigForm.find('div.attributes-section label[for^="attributes_"]').closest('div');
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
                                attributeConfig.precision = $attributesDiv.find('input[name$="_primitive_precision"]').val();
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
                $('#event-feed-form').removeAttr( "mode" );
                Simulator.updateSimulation(
                    simulation.properties.simulationName,
                    JSON.stringify(simulation),
                    function (data) {
                        self.addActiveSimulationToUi(simulation);
                        self.clearEventFeedForm();
                        log.info(data);
                    },
                    function (data) {
                        log.error(data);
                    }
                );
            } else {
                Simulator.uploadSimulation(
                    JSON.stringify(simulation),
                    function (data) {
                        // self.addActiveSimulationToUi(simulation);
                        self.clearEventFeedForm();
                        log.info(data);
                    },
                    function (data) {
                        log.error(data);
                    }
                );
            }
            return false;
        });

        $("#event-feed-form").on('click', '.feedSimulationConfig button.addNewSource', function () {
            $('.collapse').collapse();
            var $sourceConfigs = $(this).closest('form.feedSimulationConfig').find('div.sourceConfigs');
            var sourceType = $(this).closest('div.form-inline').find('select.sources').val();
            var sourcePanel = self.createConfigPanel(self.currentTotalSourceNum, self.dataCollapseNum, sourceType);
            $sourceConfigs.append(sourcePanel);
            var sourceForm = self.createSourceForm(sourceType, self.currentTotalSourceNum);
            var $sourceConfigBody = $sourceConfigs.find('div.source[data-uuid=' + self.currentTotalSourceNum + '] div.panel-body');
            $sourceConfigBody.append(sourceForm);
            self.loadSiddhiAppNames(self.totalSourceNum);
            self.loadCSVFileNames(self.totalSourceNum);
            self.addSourceConfigValidation(sourceType, self.currentTotalSourceNum);
            self.currentTotalSourceNum++;
            self.dataCollapseNum++;
            self.totalSourceNum++;
            return false;
        });

        self.$eventFeedConfigTabContent.on('click', 'a i.fw-start', function () {
            var $panel = $(this).closest('.input-group');
            var simulationName = $panel.attr('data-name');
            Simulator.simulationAction(
                simulationName,
                "run",
                function (data) {
                    log.info(data.message);
                    self.activeSimulationList[simulationName].status = "RUN";
                    setTimeout(function () {
                        self.checkSimulationStatus($panel, simulationName)
                    }, 3000);
                },
                function (msg) {
                    log.error(msg);
                }
            );
            $panel.find('i.fw-start').closest('a').addClass("hidden");
            $panel.find('i.fw-assign').closest('a').removeClass("hidden");
            $panel.find('i.fw-resume').closest('a').removeClass("hidden");
            $panel.find('i.fw-stop').closest('a').removeClass("hidden");
        });
        self.$eventFeedConfigTabContent.on('click', 'a i.fw-assign', function () {
            var $panel = $(this).closest('.input-group');
            var simulationName = $panel.attr('data-name');
            Simulator.simulationAction(
                simulationName,
                "pause",
                function (data) {
                    log.info(data.message);
                },
                function (msg) {
                    log.error(msg);
                }
            );
            self.activeSimulationList[simulationName].status = "PAUSE";
            $panel.find('i.fw-start').closest('a').addClass("hidden");
            $panel.find('i.fw-assign').closest('a').addClass("hidden");
            $panel.find('i.fw-resume').closest('a').removeClass("hidden");
            $panel.find('i.fw-stop').closest('a').removeClass("hidden");
        });
        self.$eventFeedConfigTabContent.on('click', 'a i.fw-resume', function () {
            var $panel = $(this).closest('.input-group');
            var simulationName = $panel.attr('data-name');
            self.activeSimulationList[simulationName].status = "RESUME";
            Simulator.simulationAction(
                simulationName,
                "resume",
                function (data) {
                    log.info(data.message);
                },
                function (msg) {
                    log.error(msg);
                }
            );
            $panel.find('i.fw-start').closest('a').addClass("hidden");
            $panel.find('i.fw-assign').closest('a').removeClass("hidden");
            $panel.find('i.fw-resume').closest('a').addClass("hidden");
            $panel.find('i.fw-stop').closest('a').removeClass("hidden");
        });
        self.$eventFeedConfigTabContent.on('click', 'a i.fw-stop', function () {
            var $panel = $(this).closest('.input-group');
            var simulationName = $panel.attr('data-name');
            self.activeSimulationList[simulationName].status = "STOP";
            Simulator.simulationAction(
                simulationName,
                "stop",
                function (data) {
                    log.info(data.message);
                },
                function (msg) {
                    log.error(msg);
                }
            );
            $panel.find('i.fw-start').closest('a').removeClass("hidden");
            $panel.find('i.fw-assign').closest('a').addClass("hidden");
            $panel.find('i.fw-resume').closest('a').addClass("hidden");
            $panel.find('i.fw-stop').closest('a').addClass("hidden");
        });

        self.$eventFeedConfigTabContent.on('click', 'a[name="delete-source"]', function () {
            var $panel = $(this).closest('.input-group');
            var simulationName = $panel.attr('data-name');
            Simulator.deleteSimulation(
                simulationName,
                function (data) {
                    log.info(data);
                    self.$eventFeedConfigTabContent.find('div[data-name="' + simulationName + '"]').remove();
                },
                function (data) {
                    log.error(data);
                }
            );
        });

        $("#event-feed-configs").on('click', 'button[name="create-new-config"]', function () {
            self.clearEventFeedForm();
            $('#event-feed-form').removeAttr("mode");
        });
        
        self.$eventFeedConfigTabContent.on('click', 'a[name="edit-source"]', function () {
            self.clearEventFeedForm();
            var $panel = $(this).closest('.input-group');
            var simulationName = $panel.attr('data-name');
            var simulationConfig = self.activeSimulationList[simulationName];
            var $eventFeedForm = $('#event-feed-form');
            self.clearEventFeedForm();
            $eventFeedForm.attr("mode", "edit");
            $eventFeedForm.find('input[name="simulation-name"]').val(self.getValue(simulationConfig.properties.simulationName));
            $eventFeedForm.find('input[name="start-timestamp"]').val(self.getValue(simulationConfig.properties.startTimestamp));
            $eventFeedForm.find('input[name="feed-description"]').val(self.getValue(simulationConfig.properties.description));
            $eventFeedForm.find('input[name="end-timestamp"]').val(self.getValue(simulationConfig.properties.endTimestamp));
            $eventFeedForm.find('input[name="no-of-events"]').val(self.getValue(simulationConfig.properties.noOfEvents));
            $eventFeedForm.find('input[name="time-interval"]').val(self.getValue(simulationConfig.properties.timeInterval));
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
                var sourcePanel = self.createConfigPanel(self.currentTotalSourceNum, self.dataCollapseNum, sourceSimulationType);
                $sourceConfigs.append(sourcePanel);
                var sourceForm = self.createSourceForm(sourceSimulationType, self.currentTotalSourceNum);
                var $sourceConfigBody = $sourceConfigs.find('div.source[data-uuid=' + self.currentTotalSourceNum + '] div.panel-body');
                $sourceConfigBody.append(sourceForm);
                var $sourceForm = $sourceConfigBody.find('form.sourceConfigForm[data-uuid=' + self.currentTotalSourceNum + ']');
                self.loadSiddhiAppNamesAndSelectOption(self.totalSourceNum, source);
                if ("CSV_SIMULATION" == source.simulationType) {
                    self.loadCSVFileNamesAndSelectOption(self.totalSourceNum, source.fileName);
                    var $timestampIndex = $sourceForm.find('input[value="attribute"]');
                    var $timestampInteval = $sourceForm.find('input[value="interval"]');
                    var $ordered = $sourceForm.find('input[value="ordered"]');
                    var $notordered = $sourceForm.find('input[value="not-ordered"]');
                    var $timestampAttribute = $sourceForm.find('input[name="timestamp-attribute"]');
                    var $timeInterval = $sourceForm.find('input[name="timestamp-interval"]');
                    if (source.timeInterval && 0 != source.timeInterval.length) {
                        $timeInterval.prop('disabled', false);
                        $timeInterval.val(source.timeInterval);
                        $timestampAttribute.prop('disabled', true).val('');
                        $ordered.prop('disabled', true);
                        $notordered.prop('disabled', true);
                        $timestampIndex.prop("checked", false);
                        $timestampInteval.prop("checked", true);
                    } else {
                        log.info("timestamp thing is selected");
                        // $sourceForm.find('select[name="timestamp-attribute"] > option').eq($sourceForm.find('select[name="timestamp-attribute"] > option[value="' + source.timestampAttribute + '"]')).prop('selected', true);
                        $timestampAttribute.prop('disabled', false).val(source.timestampAttribute);
                        $timeInterval.prop('disabled', true).val('');
                        $ordered.prop('disabled', false);
                        $notordered.prop('disabled', false);
                        $timestampIndex.prop("checked", true);
                        $timestampInteval.prop("checked", false);
                        if (source.isOrdered) {
                            $ordered.prop("checked", true);
                        } else {
                            log.info("timestamp thing is selected");
                            // $sourceForm.find('select[name="timestamp-attribute"] > option').eq($sourceForm.find('select[name="timestamp-attribute"] > option[value="' + source.timestampAttribute + '"]')).prop('selected', true);
                            $timestampAttribute.prop('disabled', false).val(source.timestampAttribute);
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

                    $sourceForm.find('input[name="delimiter"]').val(source.delimiter);
                    self.addSourceConfigValidation(source.simulationType, self.currentTotalSourceNum);
                    self.currentTotalSourceNum++;
                    self.dataCollapseNum++;
                    self.totalSourceNum++;
                }
            }
        });

        $("#event-feed-form").on('click', 'button.delete-source', function () {
            var $sourceDiv = $(this).closest('div.source');
            var removingUuid = $sourceDiv.attr("data-uuid");
            $sourceDiv.remove();
            self.refreshSourcePanelHeadings(removingUuid);
            self.currentTotalSourceNum--;
        });

        $("#event-feed-form").on('change', '.sourceConfigs div select[name="siddhi-app-name"]', function () {
            var $element = $(this);
            var $div = $element.closest('.sourceConfigForm');
            var uuid = $div.attr("data-uuid");
            var $streamNameSelect = $div.find('select[name="stream-name"]');
            $streamNameSelect.empty();
            var siddhiAppName = $element.val();
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
                        $streamNameSelect.prop("selectedIndex", -1);
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
            Simulator.retrieveStreamAttributes(
                $sourceConfigForm
                    .find('select[name="siddhi-app-name"]')
                    .val(),
                $sourceConfigForm
                    .find('select[name="stream-name"]')
                    .val(),
                function (data) {
                    self.refreshAttributesList(sourceUuid, data);
                    var $attributes = $sourceConfigForm.find('input[id^="attributes"]');
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
            var $sourceConfigForm = $element.closest('.sourceConfigForm');
            var connectionDetails = self.validateAndGetDbConfiguration($sourceConfigForm);
            Simulator.retrieveColumnNames(
                JSON.stringify(connectionDetails),
                $element.val(),
                function (data) {
                    self.loadColumnNamesList(data, $sourceConfigForm);
                },
                function (msg) {
                    log.error(msg['responseText']);
                }
            );
        });

        //allow only one of timestamp options for csv source config
        $("#event-feed-form").on('click', 'input[name="timestamp-option"]', function () {
            var elementId = this.value;
            var form = $(this).closest('div.form-inline');
            var dataType = $(this).closest('form.sourceConfigForm').attr('data-type');
            var $timestampAttribute;
            if ('csv' == dataType) {
                $timestampAttribute = form.find('input[name="timestamp-attribute"]');
            } else {
                $timestampAttribute = form.find('select[name="timestamp-attribute"]');
            }
            var $timeInterval = form.find('input[name="timestamp-interval"]');

            var $sourceConfigForm = $(this).closest('form.sourceConfigForm');
            var $ordered = $sourceConfigForm.find('input[value="ordered"]');
            var $notordered = $sourceConfigForm.find('input[value="not-ordered"]');
            if (elementId == 'attribute') {
                $timeInterval.prop('disabled', true).val('');
                $timestampAttribute.prop('disabled', false);
                $ordered.prop('disabled', false);
                $notordered.prop('disabled', false);
            } else if (elementId == 'interval') {
                $timeInterval.prop('disabled', false).val('1000');
                $timestampAttribute.prop('disabled', true).val('');
                $ordered.prop('disabled', true);
                $notordered.prop('disabled', true);
            }
        });

        $("#event-feed-form").on('change', 'div select[name="file-name"]', function () {
            var $element = $(this);
            var value = $element.find(":selected").attr("name");
            if (value == "upload-csv-file") {
                var $div = $element.closest('.sourceConfigForm');
                self.selectedSourceNum = $div.attr("data-uuid");
                $('#csv_upload_modal').modal('show');
            }
        });

        $("form#csv_upload_modal_form").submit(function (e) {
            var $element = $(this);
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
            var $sourceConfigForm = $(this).closest('form.sourceConfigForm');
            self.selectedSourceNum = $sourceConfigForm.attr("data-uuid");
            $sourceConfigForm.find('.connectionSuccessMsg').html(self.generateConnectionMessage('connecting'));
            var connectionDetails = self.validateAndGetDbConfiguration($sourceConfigForm);
            if (null != connectionDetails) {
                var $tableNames = $sourceConfigForm.find('select[name="table-name"]');
                $(this).prop('disabled', true);
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
            var dynamicId = $(this).closest('div.sourceConfigForm').attr('data-uuid');
            var attributeType = $(this).attr('data-type');
            var attributeName = $(this).attr('name').replaceAll('attributes_', '');
            var id = this.id;
            $('.attributes_' + attributeName + '_config').html(self.generateRandomAttributeConfiguration(randomType, attributeType, dynamicId, id));
            // set the selected option of property based attribute configuration type (if any) to -1
            $('[class^="feed-attribute-random-' + dynamicId + '-property"]').each(function () {
                $(this).prop('selectedIndex', -1);
            });
            // addRandomConfigTypeValidation(id);
        });

    };

    // create a map containing siddhi app name
    self.createSiddhiAppMap = function (data) {
        self.siddhiAppDetailsMap = {};
        for (var i = 0; i < data.length; i++) {
            self.siddhiAppDetailsMap[data[i]['siddhiAppame']] = data[i]['mode'];
        }
    };
    // create the siddhi app name drop down
    self.refreshSiddhiAppList = function ($siddhiAppSelect, siddhiAppNames) {
        var newSiddhiApps = self.generateOptions(siddhiAppNames);
        $siddhiAppSelect.html(newSiddhiApps);
    };
    // select an option from the siddhi app name drop down
    self.selectSourceOptions = function ($siddhiAppSelect, siddhiAppName) {
        /*
         * if an siddhi app has been already selected when the siddhi app name list was refreshed,
         * check whether the siddhi app still exists in the workspace, if yes, make that siddhi app name the
         * selected value.
         * If the siddhi app no longer exists in the work space, set the selected option to -1 and refresh the form
         * */
        if (siddhiAppName in self.siddhiAppDetailsMap) {
            $siddhiAppSelect.val(siddhiAppName);
        } else {
            $siddhiAppSelect.prop('selectedIndex', -1);
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
    self.generateOptions = function (dataArray) {
        var dataOption =
            '<option value = "{{dataName}}">' +
            '   {{dataName}}' +
            '</option>';
        var result = '';
        for (var i = 0; i < dataArray.length; i++) {
            result += dataOption.replaceAll('{{dataName}}', dataArray[i]);
        }
        return result;
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

    // rename the event feed config tabs once a tab is deleted
    self.renameEventFeedConfigTabs = function () {
        var nextNum = 1;
        $('ul#event-feed-config-tab li').each(function () {
            var $element = $(this);
            var uuid = $element.data('uuid');
            if (uuid !== undefined) {
                $element
                    .find('a')
                    .html(self.createSingleListItemText(nextNum, uuid));
                nextNum++;
            }
        })
    };

    // create text element of the single event tab list element
    self.createSingleListItemText = function (nextNum) {
        var listItemText =
            'S {{nextNum}}' +
            '<button type="button" class="close" name="delete" data-form-type="feed"' +
            '       aria-label="Close">' +
            '   <span aria-hidden="true">×</span>' +
            '</button>';
        return listItemText.replaceAll('{{nextNum}}', nextNum);
    };

    /*
     * feed simulation functions
     * */

    self.createConfigPanel = function (totalSourceNum, dataCollapseNum, sourceType) {
        var panel =
            '<div class="panel panel-default source" data-uuid="{{totalSourceNum}}">' +
            '<div class="panel-heading feed-config" role="tab" data-toggle="collapse" ' +
            'data-target="#source_{{dataCollapseNum}}"> ' +
            '<h4 class="source-title panel-title" data-type="{{sourceType}}">' +
            'Source {{totalSourceNum}} - {{sourceType}}' +
            '</h4>' +
            '<button type = "button" class = "btn btn-primary delete-source">Delete</button>' +
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
                return csvTemplate;
            case 'Database':
                var dbTemplate = $("#dbSourceConfig_dynamicId").clone();
                dbTemplate.attr("id", "dbSourceConfig_" + totalSourceNum);
                dbTemplate.attr("data-uuid", totalSourceNum);
                dbTemplate.css("display", "block");
                dbTemplate.html(dbTemplate.html().replaceAll('{{dynamicId}}', totalSourceNum));
                return dbTemplate;
            case 'Random':
                var randomTemplate = $("#randomSourceConfig_dynamicId").clone();
                randomTemplate.attr("id", "randomSourceConfig_" + totalSourceNum);
                randomTemplate.attr("data-uuid", totalSourceNum);
                randomTemplate.css("display", "block");
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
                $source.find('h4').text('Source ' + uuid + ' - ' + match[2]);
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
        var $siddhiAppSelect = $('div[data-uuid="' + elementId + '"] select[name="siddhi-app-name"]');
        var siddhiAppName = $siddhiAppSelect.val();
        Simulator.retrieveSiddhiAppNames(
            function (data) {
                self.createSiddhiAppMap(data);
                self.refreshSiddhiAppList($siddhiAppSelect, Object.keys(self.siddhiAppDetailsMap));
                self.selectSourceOptions($siddhiAppSelect, siddhiAppName);
            },
            function (data) {
                log.info(data);
            }
        );
    };

    // load execution plan names to form
    self.loadSiddhiAppNamesAndSelectOption = function (elementId, source) {
        var $siddhiAppSelect = $('div[data-uuid="' + elementId + '"] select[name="siddhi-app-name"]');
        var $siddhiAppMode = $('div[data-uuid="' + elementId + '"] div[data-name="siddhi-app-name-mode"]');
        var $streamNameSelect = $('div[data-uuid="' + elementId + '"] select[name="stream-name"]');
        var siddhiAppName = $siddhiAppSelect.val();
        Simulator.retrieveSiddhiAppNames(
            function (data) {
                self.createSiddhiAppMap(data);
                self.refreshSiddhiAppList($siddhiAppSelect, Object.keys(self.siddhiAppDetailsMap));
                self.selectSourceOptions($siddhiAppSelect, siddhiAppName);
                $siddhiAppSelect.find('option').eq($siddhiAppSelect.find('option[value="' + source.siddhiAppName + '"]').index()).prop('selected', true);
                $siddhiAppMode.html('mode : ' + self.siddhiAppDetailsMap[source.siddhiAppName]);
                if (self.siddhiAppDetailsMap[source.siddhiAppName] === self.FAULTY) {
                    $streamNameSelect.prop('disabled', true);
                } else {
                    $streamNameSelect.prop('disabled', false);
                    Simulator.retrieveStreamNames(
                        source.siddhiAppName,
                        function (data) {
                            self.refreshStreamList($streamNameSelect, data);
                            $streamNameSelect.find('option').eq($streamNameSelect.find('option[value="' + source.streamName + '"]').index()).prop('selected', true);
                            Simulator.retrieveStreamAttributes(
                                source.siddhiAppName,
                                source.streamName,
                                function (data) {
                                    self.refreshAttributesList(elementId, data);
                                    var $sourceConfigForm = $('form.sourceConfigForm[data-uuid="' + elementId + '"]');
                                    if ("CSV_SIMULATION" == source.simulationType) {
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
                                    } else if ("DATABASE_SIMULATION" == source.simulationType) {
                                        $sourceConfigForm.find('input[name="data-source-location"]').val(source.dataSourceLocation);
                                        $sourceConfigForm.find('input[name="driver-class"]').val(source.driver);
                                        $sourceConfigForm.find('input[name="username"]').val(source.username);
                                        $sourceConfigForm.find('input[name="password"]').val(source.password);
                                        var connectionDetails = self.validateAndGetDbConfiguration($sourceConfigForm);
                                        if (null != connectionDetails) {
                                            var $tableNames = $sourceConfigForm.find('select[name="table-name"]');
                                            $(this).prop('disabled', true);
                                            Simulator.testDatabaseConnectivity(
                                                JSON.stringify(connectionDetails),
                                                function (data) {
                                                    self.refreshTableNamesFromDataSource(connectionDetails, $tableNames);
                                                    $tableNames.find('option').eq($tableNames.find('option[value="' + source.tableName + '"]').index()).prop('selected', true);
                                                    $sourceConfigForm.find('.connectionSuccessMsg').html(self.generateConnectionMessage('success'));
                                                    Simulator.retrieveColumnNames(
                                                        JSON.stringify(connectionDetails),
                                                        source.tableName,
                                                        function (data) {
                                                            self.loadColumnNamesListAndSelect(data, $sourceConfigForm, source.columnNamesList.split(","));
                                                            $tableNames.find('option').eq($tableNames.find('option[value="' + source.tableName + '"]').index()).prop('selected', true);
                                                            var $timestampIndex = $sourceConfigForm.find('input[value="attribute"]');
                                                            var $timestampInteval = $sourceConfigForm.find('input[value="interval"]');
                                                            var $timestampAttribute = $sourceConfigForm.find('input[name="timestamp-attribute"]');
                                                            var $timeInterval = $sourceConfigForm.find('input[name="timestamp-interval"]')
                                                            if (source.timeInterval && 0 != source.timeInterval.length) {
                                                                $timeInterval.prop('disabled', false);
                                                                $timeInterval.val(source.timeInterval);
                                                                $timestampAttribute.prop('disabled', true).val('');
                                                                $timestampIndex.prop("checked", false);
                                                                $timestampInteval.prop("checked", true);
                                                            } else {
                                                                var $timestampAtt = $sourceConfigForm.find('select[name="timestamp-attribute"]');
                                                                $timestampAtt.find('option').eq($timestampAtt.find('option[value="' + source.timestampAttribute + '"]').index()).prop('selected', true);
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
                                                    log.error(msg['responseText']);
                                                    $sourceConfigForm.find('.connectionSuccessMsg').html(self.generateConnectionMessage('failure'));
                                                }
                                            );
                                        }
                                    } else if ("RANDOM_DATA_SIMULATION" == source.simulationType) {
                                        var attributeConfiguration = source.attributeConfiguration;
                                        var $attributesDivs = $sourceConfigForm.find('div.attributes-section label[for^="attributes_"]').closest('div');
                                        var i=0;
                                        $attributesDivs.each(function () {
                                            var attributeConfig = attributeConfiguration[i];
                                            var $attributesDiv = $(this);
                                            var $attributeSelect = $attributesDiv.find('select[name^="attributes"]');
                                            var randomType = $attributeSelect.val();
                                            var attributeType = $attributeSelect.attr('data-type');
                                            var attributeName = $attributeSelect.attr('name').replaceAll('attributes_', '');
                                            var id = this.id;
                                            var $selectType = $attributesDiv.find('select[id^="attributes_"]');
                                            if ("CUSTOM_DATA_BASED" == attributeConfig.type) {
                                                $selectType.find('option').eq($selectType.find('option[value="custom"]').index()).prop('selected', true);
                                                $('.attributes_' + attributeName + '_config').html(self.generateRandomAttributeConfiguration("custom", attributeType, elementId, id));
                                                $attributesDiv.find('input[data-type="custom"]').val(attributeConfig.list);
                                            } else if ("PRIMITIVE_BASED" == attributeConfig.type) {
                                                $selectType.find('option').eq($selectType.find('option[value="primitive"]').index()).prop('selected', true);
                                                var attDataType = attributeConfig.primitiveType;
                                                $('.attributes_' + attributeName + '_config').html(self.generateRandomAttributeConfiguration("primitive", attributeType, elementId, id));
                                                if ("BOOL" == attDataType) {

                                                } else if ("STRING" == attDataType) {
                                                    $attributesDiv.find('input[name$="_primitive_length"]').val(attributeConfig.length);
                                                } else if ("INT" == attDataType || "LONG" == attDataType) {
                                                    $attributesDiv.find('input[name$="_primitive_min"]').val(attributeConfig.min);
                                                    $attributesDiv.find('input[name$="_primitive_max"]').val(attributeConfig.max);
                                                } else if ("FLOAT" == attDataType || "DOUBLE" == attDataType) {
                                                    $attributesDiv.find('input[name$="_primitive_min"]').val(attributeConfig.min);
                                                    $attributesDiv.find('input[name$="_primitive_max"]').val(attributeConfig.max);
                                                    $attributesDiv.find('input[name$="_primitive_precision"]').val(attributeConfig.precision);
                                                }
                                            } else if ("PROPERTY_BASED" == attributeConfig.type) {
                                                $selectType.find('option').eq($selectType.find('option[value="property"]').index()).prop('selected', true);
                                                $('.attributes_' + attributeName + '_config').html(self.generateRandomAttributeConfiguration("property", attributeType, elementId, id));
                                                $attributesDiv.find('select[name$="_property"]').val(attributeConfig.property);
                                            } else if ("REGEX_BASED" == attributeConfig.type) {
                                                $selectType.find('option').eq($selectType.find('option[value="regex"]').index()).prop('selected', true);
                                                $('.attributes_' + attributeName + '_config').html(self.generateRandomAttributeConfiguration("regex", attributeType, elementId, id));
                                                $attributesDiv.find('input[name$="_regex"]').val(attributeConfig.pattern);
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
            },
            function (data) {
                log.info(data);
            }
        );
    };

    self.loadCSVFileNames = function (dynamicId) {
        var $csvFileSelect = $('div[data-uuid="' + dynamicId + '"] select[name="file-name"]');
        Simulator.retrieveCSVFileNames(
            function (data) {
                self.refreshCSVFileList($csvFileSelect, data);
                $csvFileSelect.prop("selectedIndex", -1);
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
                $csvFileSelect.find('option').eq($csvFileSelect.find('option[value="' + selectedFileName + '"]').index()).prop('selected', true);
            },
            function (data) {
                log.error(data);
            });
    };

    self.refreshCSVFileList = function ($csvFileSelect, csvFileNames) {
        var fileNames = self.generateOptions(csvFileNames);
        fileNames +=
            '<option value = "" name="upload-csv-file">' +
            'Upload CSV file' +
            '</option>';
        $csvFileSelect.html(fileNames);
    };

    self.refreshStreamList = function ($streamNameSelect, streamNames) {
        var newStreamOptions = self.generateOptions(streamNames);
        $streamNameSelect
            .html(newStreamOptions);
    };

    self.generateConnectionMessage = function (status) {
        var connectingMsg =
            '<div id="connectionSuccessMsg" class="color-grey">' +
            '<label>Attempting to connect to datasource...</label>' +
            '</div>';

        var successMsg =
            '<div id="connectionSuccessMsg" class="color-green">' +
            '<label>Successfully connected</label>' +
            '</div>';

        var failureMsg =
            '<div id="connectionSuccessMsg" class="color-red">' +
            '<label>Connection failed</label>' +
            '</div>';
        switch (status) {
            case 'connecting':
                return connectingMsg;
            case 'success':
                return successMsg;
            case 'failure':
                return failureMsg;
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
        var $sourceConfigForm = $('form.sourceConfigForm[data-uuid="' + dynamicId + '"]');
        $sourceConfigForm.validate();
        $sourceConfigForm.find('select[name="siddhi-app-name"]').rules('add', {
            required: true,
            messages: {
                required: "Please select an execution plan name."
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

    self.refreshAttributesList = function (uuid, streamAttributes) {
        var $attributesDiv = $('form.sourceConfigForm[data-uuid="' + uuid + '"] div.attributes-section');
        var dataType = $('form.sourceConfigForm[data-uuid=' + uuid + ']').attr('data-type');
        $attributesDiv.html(self.generateAttributesDivForSource(dataType));
        var attributes = self.generateAttributesListForSource(dataType, streamAttributes);
        $attributesDiv.html(attributes);
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

    self.generateAttributesListForSource = function (dataType, attributes) {
        var csvAttribute =
            '<div>' +
            '   <label for ="attributes_{{attributeName}}">' +
            '        {{attributeName}}({{attributeType}})' +
            '       <input type="text" class="feed-attribute-csv form-control"' +
            '       name="attributes_{{attributeName}}" ' +
            '       id="attributes_{{attributeName}}"' +
            '       data-type ="{{attributeType}}">' +
            '   </label>' +
            '</div>';
        var dbAttribute =
            '<div>' +
            '   <label for ="attributes_{{attributeName}}" class="labelSize300Px">' +
            '       {{attributeName}}({{attributeType}})' +
            '       <select id="attributes_{{attributeName}}"' +
            '       name="attributes_{{attributeName}}" ' +
            '       class="feed-attribute-db form-control" ' +
            '       data-type="{{attributeType}}"> ' +
            '       </select>' +
            '   </label>' +
            '</div>';
        var randomAttribute =
            '<div>' +
            '   <label for ="attributes_{{attributeName}}" class="labelSize300Px">' +
            '       {{attributeName}}({{attributeType}})' +
            '       <label class="labelSize300Px">' +
            '           Configuration Type' +
            '           <select id="attributes_{{attributeName}}"' +
            '           name="attributes_{{attributeName}}" ' +
            '           class="feed-attribute-random form-control"' +
            '           data-type ="{{attributeType}}"> ' +
            '              <option disabled selected value> -- select an option -- </option>' +
            '              <option value="custom">Custom data based</option>' +
            '              <option value="primitive">Primitive based</option>' +
            '              <option value="property">Property based </option>' +
            '              <option value="regex">Regex based</option>' +
            '           </select>' +
            '       </label>' +
            '   </label>' +
            '   <div class ="attributes_{{attributeName}}_config">' +
            '   </div> ' +
            '</div>';

        var result = "";

        for (var i = 0; i < attributes.length; i++) {
            var temp;
            switch (dataType) {
                case 'csv':
                    temp = csvAttribute.replaceAll('{{attributeName}}', attributes[i]['name']);
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
        $sourceConfigForm.find('select[name="timestamp-attribute"]').html(columnsList);
        $sourceConfigForm.find('select[name="timestamp-attribute"]').prop("selectedIndex", -1);
    };

    //generate input fields for attributes
    self.loadColumnNamesListAndSelect = function (columnNames, $sourceConfigForm, selectedValueList) {
        var columnsList = self.generateOptions(columnNames);
        var i = 0;
        $sourceConfigForm.find('.feed-attribute-db').each(function () {
            $(this).html(columnsList);
            $(this).find('option').eq($(this).find('option[value="' + selectedValueList[i] + '"]').index()).prop('selected', true);
            i++;
        });
        $sourceConfigForm.find('select[name="timestamp-attribute"]').html(columnsList);
        $sourceConfigForm.find('select[name="timestamp-attribute"]').prop("selectedIndex", -1);
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
                return self.generateCustomBasedAttributeConfiguration(parentId);
            case 'primitive':
                return self.generatePrimitiveBasedAttributeConfiguration(attributeType, parentId);
            case 'property':
                return self.generatePropertyBasedAttributeConfiguration(attributeType, parentId);
            case 'regex' :
                return self.generateRegexBasedAttributeConfiguration(parentId);
        }
    };

    // generate input fields to provide configuration for 'custom based' random generation type
    self.generateCustomBasedAttributeConfiguration = function (parentId) {
        var custom =
            '<div>' +
            '<label class="labelSize300Px">' +
            'Data' +
            '<input type="text" class="form-control" name="' + parentId + '_custom"' +
            'data-type ="custom">' +
            '</label>' +
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
            '<div>' +
                '<label class="labelSize300Px">' +
                    'Length' +
                    '<input type="text" class="form-control" name="{{parentId}}_primitive_length" ' +
                            'data-type="numeric">' +
                '</label>' +
            '</div>';

        var min =
            '<div>' +
                '<label class="labelSize300Px">' +
                    'Min' +
                    '<input type="text" class="form-control" name="{{parentId}}_primitive_min" ' +
                            'data-type="{{attributeType}}">' +
                '</label>' +
            '</div>';

        var max =
            '<div>' +
                '<label class="labelSize300Px">' +
                    'Max' +
                    '<input type="text" class="form-control" name="{{parentId}}_primitive_max" ' +
                            'data-type="{{attributeType}}">' +
                '</label>' +
            '</div>';

        var precision =
            '<div>' +
            '<label class="labelSize300Px">' +
            'Precision' +
            '<input type="text" class="form-control" name="{{parentId}}_primitive_precision" ' +
            'data-type="numeric">' +
            '</label>' +
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
            '<div>' +
            '<label class="labelSize300Px">' +
            'Type' +
            '<select name="{{parentId}}_property" class="feed-attribute-random-property form-control" ' +
            'data-type="property"> ';

        var propertyEndingTag =
            '</select>' +
            '</label>' +
            '</div>';

        var temp = propertyStartingTag;
        temp += this.refreshPropertyBasedOptionsList(attrType);
        temp += propertyEndingTag;
        return temp.replaceAll('{{parentId}}', parentId);
    };

    //refresh the list of property based random generation options
    self.refreshPropertyBasedOptionsList = function (attrType) {
        return self.generateOptions(self.propertyBasedGenerationOptions);
    };

    // generate input fields to provide configuration for 'regex based' random generation type
    self.generateRegexBasedAttributeConfiguration = function (parentId) {
        var temp =
            '<div>' +
            '<label class="labelSize300Px">' +
            'Pattern' +
            '<input type="text" class="form-control" name="{{parentId}}_regex" data-type="regex">' +
            '</label>' +
            '</div>';
        return temp.replaceAll('{{parentId}}', parentId);
    };

    self.addAvailableFeedSimulations = function () {
        Simulator.getFeedSimulations(
            function (data) {
                var simulations = JSON.parse(data.message);
                log.info(simulations);
                var activeSimulations = simulations.activeSimulations;
                for (var i = 0; i < activeSimulations.length; i++) {
                    self.addActiveSimulationToUi(activeSimulations[i]);
                }
                var inActiveSimulations = simulations.inActiveSimulations;
                for (var i = 0; i < inActiveSimulations.length; i++) {
                    self.addInActiveSimulationToUi(inActiveSimulations[i]);
                }
            },
            function (msg) {
                log.error(msg['responseText']);
            }
        )
    };

    self.addActiveSimulationToUi = function (simulation) {
        var simulationName = simulation.properties.simulationName;
        self.activeSimulationList[simulationName] = simulation;
        self.activeSimulationList[simulationName].status = "STOP";
        self.$eventFeedConfigTabContent.find('div[data-name="' + simulation.properties.simulationName + '"]').remove();
        var simulationDiv =
            '<div class="input-group" data-name="' + simulation.properties.simulationName + '">' +
            '<span class="form-control">' +
            '<span class="simulation-name">' + simulation.properties.simulationName + '</span>' +
            '<span class="simulator-tools pull-right">' +
            '<a><i class="fw fw-start"></i></a>' +
            '<a class="hidden"><i class="fw fw-resume"></i></a>' +
            '<a class="hidden"><i class="fw fw-assign fw-rotate-90"></i></a>' +
            '<a class="hidden"><i class="fw fw-stop"></i></a>' +
            '</span>' +
            '</span>' +
            '<div class="input-group-btn">' +
            '<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown"' +
            ' aria-haspopup="true" aria-expanded="false">' +
            '<i class="fw fw-ellipsis fw-rotate-90"></i>' +
            '<span class="sr-only">Toggle Dropdown Menu</span>' +
            '</button>' +
            '<ul class="dropdown-menu dropdown-menu-right">' +
            '<li><a name="edit-source" data-toggle="sidebar" data-target="#left-sidebar-sub" aria-expanded="false">' +
            'Edit</a>' +
            '</li>' +
            '<li><a name="delete-source">Delete</a></li>' +
            '</ul>' +
            '</div>' +
            '</div>';
        self.$eventFeedConfigTabContent.append(simulationDiv);
    };

    self.addInActiveSimulationToUi = function (simulation) {
        var simulationName = simulation.properties.simulationName;
        self.activeSimulationList[simulationName] = simulation;
        self.activeSimulationList[simulationName].status = "STOP";
        self.$eventFeedConfigTabContent.find('div[data-name="' + simulation.properties.simulationName + '"]').remove();
        var simulationDiv =
            '<div class="input-group" data-name="' + simulation.properties.simulationName + '">' +
                '<span class="form-control">' +
                '<span class="simulation-name">' + simulation.properties.simulationName + '</span>' +
                '</span>' +
                '<div class="input-group-btn">' +
                    '<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown"' +
                            ' aria-haspopup="true" aria-expanded="false">' +
                        '<i class="fw fw-ellipsis fw-rotate-90"></i>' +
                        '<span class="sr-only">Toggle Dropdown Menu</span>' +
                    '</button>' +
                    '<ul class="dropdown-menu dropdown-menu-right">' +
                        '<li><a name="edit-source" data-toggle="sidebar" data-target="#left-sidebar-sub" aria-expanded="false">' +
                        'Edit</a>' +
                        '</li>' +
                        '<li><a name="delete-source">Delete</a></li>' +
                    '</ul>' +
                '</div>' +
            '</div>';
        self.$eventFeedConfigTabContent.append(simulationDiv);
    };

    self.checkSimulationStatus = function ($panel, simulationName) {
        Simulator.getFeedSimulationStatus(
            simulationName,
            function (data) {
                var status = data.message;
                if ("STOP" == status && "RUN" == self.activeSimulationList[simulationName].status) {
                    $panel.find('i.fw-start').closest('a').removeClass("hidden");
                    $panel.find('i.fw-assign').closest('a').addClass("hidden");
                    $panel.find('i.fw-resume').closest('a').addClass("hidden");
                    $panel.find('i.fw-stop').closest('a').addClass("hidden");
                } else if (!("STOP" == status && "STOP" == self.activeSimulationList[simulationName].status)) {
                    setTimeout(function () {
                        self.checkSimulationStatus($panel, simulationName)
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
        $eventFeedForm.find('input[name="feed-description"]').val('');
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
            $element
                .val(Date.parse($element.val()));
        } else {
            $element
                .val('');
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
                $element
                    .val(Date.parse($element.val()));
            }
        } else {
            $element
                .val('');
        }
    };

    self.pollingSimulation = function () {
        self.addAvailableFeedSimulations();
        setTimeout(self.pollingSimulation, 5000);
    };

    return self;
});
