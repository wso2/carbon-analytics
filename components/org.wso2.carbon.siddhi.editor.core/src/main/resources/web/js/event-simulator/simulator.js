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

    self.init = function () {

        self.singleEventConfigCount = 1;
        self.executionPlanDetailsMap = {};
        self.singleEventForm = $('#single-event-form').find('form').clone();
        self.singleEventConfigTabContent = $("#single-event-config-tab-content");
        self.singleEventConfigs = $("#single-event-configs");
        self.addSingleEventForm = $('#add-single-event-form');

        // add methods to validate int/long and double/float
        $.validator.addMethod("validateIntOrLong", function (value, element) {
            return this.optional(element) || /^[-+]?[0-9]+$/.test(value);
        }, "Please provide a valid numerical value.");

        $.validator.addMethod("validateFloatOrDouble", function (value, element) {
            return this.optional(element) || /^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$/.test(value);
        }, "Please provide a valid numerical value.");

        // add the first single event form and disable the delete button
        self.addSingleEventConfigForm(null, self.addSingleEventForm);
        $('li[data-uuid="1"] button[name="delete"]')
            .prop('disabled', true)
            .hide();

        // add a single event form
        self.addSingleEventForm.on('click', function (e) {
            self.addSingleEventConfigForm(e, this);
        });

        self.singleEventConfigTabContent.on('focusin', 'select[name="execution-plan-name"]', function () {
            var uuid = $(this).closest('form[data-form-type="single"]').data('uuid');
            self.loadExecutionPlanNames(uuid);
            log.info("Execution Plans loaded");
        });

        // change stream names on change function of execution plan name
        self.singleEventConfigTabContent.on('change', 'select[name="execution-plan-name"]', function () {
            var form = $(this).closest('form[data-form-type="single"]');
            var uuid = form.data('uuid');
            var executionPlanName = $(this).val();
            var executionPlanMode = form.find('div[data-name="execution-plan-name-mode"]');
            var streamNameSelect = form.find('select[name="stream-name"]');
            var timestamp = form.find('input[name="timestamp"]');
            var attributes = form.find('div[data-name="attributes"]');
            var runDebugButtons = form.find('div[data-name="run-debug-buttons"]');
            var send = form.find('button[type="submit"][name="send"]');

            streamNameSelect.prop('selectedIndex', -1);
            timestamp.val('');
            executionPlanMode.html('mode : ' + self.executionPlanDetailsMap[executionPlanName]);
            self.removeSingleEventAttributeRules(uuid);
            attributes.empty();
            runDebugButtons.empty();
            /*todo make 'faulty' a constant*/
            if (self.executionPlanDetailsMap[executionPlanName] === 'FAULTY') {
                streamNameSelect.prop('disabled', true);
                timestamp.prop('disabled', true);
                send.prop('disabled', true);
            } else {
                streamNameSelect.prop('disabled', false);
                timestamp.prop('disabled', false);
                send.prop('disabled', false);
                Simulator.retrieveStreamNames(
                    executionPlanName,
                    function (data) {
                        self.refreshStreamList(streamNameSelect, data);
                        streamNameSelect.prop("selectedIndex", -1);
                    },
                    function (data) {
                        log.info(data);
                    });
                if (self.executionPlanDetailsMap[executionPlanName] === 'STOP') {
                    runDebugButtons.html(self.createRunDebugButtons());
                    form.find('label[data-name="execution-plan-start-msg"]').html('Start execution plan \'' +
                        executionPlanName + '\' in either \'run\' or \'debug\' mode.');
                    send.prop('disabled', true);
                }
            }
        });

        // change stream names on change function of stream name
        self.singleEventConfigTabContent.on('change', 'select[name="stream-name"]', function () {
            var form = $(this).closest('form[data-form-type="single"]');
            var uuid = form.data('uuid');
            self.removeSingleEventAttributeRules(uuid);
            Simulator.retrieveStreamAttributes(
                form.find('select[name="execution-plan-name"]').val(),
                form.find('select[name="stream-name"]').val(),
                function (data) {
                    self.refreshAttributesList(uuid, data);
                    self.addRulesForAttributes(uuid);
                },
                function (data) {
                    log.info(data);
                });
        });

        // start inactive execution plans in run or debug mode
        self.singleEventConfigTabContent.on('click', 'button[name="start"]', function () {
            var form = $(this).closest('form[data-form-type="single"]');
            var uuid = form.data('uuid');
            var executionPlanName = form.find('select[name="execution-plan-name"]').val();
            var mode = form.find('input[name="run-debug"]:checked').val();

            if (mode === 'run') {
                $.ajax({
                    async: true,
                    url: "http://localhost:9090/editor/" + executionPlanName + "/start",
                    type: "GET",
                    success: function (data) {
                        log.info(data)
                    },
                    error: function (msg) {
                        log.error(msg)
                    }
                });
                self.executionPlanDetailsMap[executionPlanName] = 'RUN';
            } else if (mode === 'debug') {
                $.ajax({
                    async: true,
                    url: "http://localhost:9090/editor/" + executionPlanName + "/debug",
                    type: "GET",
                    success: function (data) {
                        if (typeof callback === 'function')
                            log.info(data)
                    },
                    error: function (msg) {
                        if (typeof error === 'function')
                            log.error(msg)
                    }
                });
                self.executionPlanDetailsMap[executionPlanName] = 'DEBUG';
            }
            self.refreshRunDebugButtons(executionPlanName);
        });

        // remove a single event config tab and make the tab before it active
        self.singleEventConfigs.on('click', 'button[name="delete"][data-form-type="single"]',
            function () {
                self.removeSingleEventForm(this);
                self.renameSingleEventConfigTabs();
            });

        // is isNull checkbox is checked disable txt input, else enable text input
        self.singleEventConfigTabContent.on('click', 'input[data-input="null"]', function () {
            var form = $(this).closest('form[data-form-type="single"]');
            var attributeName = $(this).attr('name');
            var inputField = form.find('[data-element-type="attribute"][name="' + attributeName + '"]');
            if ($(this).is(':checked')) {
                if (inputField.is(':text')) {
                    inputField
                        .val('')
                        .prop('disabled', true);
                } else {
                    inputField
                        .prop('selectedIndex', -1)
                        .prop('disabled', true);
                }
                self.removeRuleOfAttribute(inputField);
            } else {
                inputField.prop('disabled', false);
                self.addRuleForAttribute(inputField);
            }

        });


        // submit function of single event
        self.singleEventConfigTabContent.on('submit', 'form[data-form-type="single"]', function (e) {
            e.preventDefault();
            // serialize all the forms elements and their values
            var form = $(this);
            var formValues = form.serializeArray();
            var formDataMap = {};
            var attributes = [];

            if (_.has(formValues, 'execution-plan-name') && _.get(formValues, 'execution-plan-name').length === 0) {
                _.set(formValues, 'execution-plan-name', formDataMap);
            } else {
                log.error("Execution plan name is required for single event simulation.");
            }

            if (_.has(formValues, 'stream-name') && _.get(formValues, 'stream-name').length === 0) {
                _.set(formValues, 'stream-name', formDataMap);
            } else {
                log.error("Stream name is required for single event simulation.");
            }

            if (_.has(formValues, 'timestamp')) {
                if (parseInt(_.get(formValues, 'timestamp')) >= 0) {
                    _.set(formValues, 'stream-name', formDataMap);
                } else {
                    log.error("Timestamp value must be a positive integer for single event simulation.");
                }
            }


            var j = 0;
            for (var i = 0; i < formValues.length; i++) {
                if (formValues[i]['name'].startsWith('single_attributes_')) {
                    //create attribute data array
                    if (formValues[i]['name'].endsWith('_null')) {
                        attributes[j++] = null;
                    } else {
                        attributes[j++] = formValues[i]['value']
                    }
                } else if (formValues[i]['name'].startsWith('single_executionPlanName_')) {
                    formDataMap['executionPlanName'] = formValues[i]['value']
                } else if (formValues[i]['name'].startsWith('single_streamName_')) {
                    formDataMap['streamName'] = formValues[i]['value']
                } else if (formValues[i]['name'].startsWith('single_timestamp_')) {
                    formDataMap['timestamp'] = formValues[i]['value']
                }
            }
            if (!('executionPlanName' in formDataMap) || formDataMap['executionPlanName'].length === 0) {
                log.error("Execution plan name is required for single event simulation.");
            }
            if (!('streamName' in formDataMap) || formDataMap['streamName'].length === 0) {
                log.error("Stream name is required for single event simulation.");
            }
            if (('timestamp' in formDataMap) && parseInt(formDataMap['timestamp']) < 0) {
                log.error("Timestamp value must be a positive value for single event simulation.");
            }
            if (attributes.length === 0) {
                log.error("Attribute values are required for single event simulation.");
            }

            if ('executionPlanName' in formDataMap && formDataMap['executionPlanName'].length > 0
                && 'streamName' in formDataMap && formDataMap['streamName'].length > 0
                && !(('timestamp' in formDataMap) && formDataMap['timestamp'] < 0)
                && attributes.length > 0) {
                formDataMap['data'] = attributes;

                form.loading('show');
                Simulator.singleEvent(
                    JSON.stringify(formDataMap),
                    function (data) {
                        /*todo remove timeout, use a different message*/
                        log.info(data);
                        setTimeout(function () {
                            form.loading('hide');
                        }, 250)
                    },
                    function (data) {
                        log.error(data);
                        setTimeout(function () {
                            form.loading('hide');
                        }, 250)
                    })
            }
        });
    };

// add a datetimepicker to an element
    self.addDateTimePicker = function (uuid) {
        var timestamp = $('form[data-form-type="single"][data-uuid="' + uuid + '"] input[name="timestamp"]');
        timestamp.datetimepicker({
            dateFormat: 'yy-mm-dd',
            timeFormat: 'HH:mm:ss:l',
            showOn: 'button',
            buttonText: '<span class="fw-stack"><i class="fw fw-square-outline fw-stack-2x"></i>' +
            '<i class="fw fw-calendar fw-stack-1x"></i><span class="fw-stack fw-move-right fw-move-bottom">' +
            '<i class="fw fw-circle fw-stack-2x fw-stroke"></i><i class="fw fw-clock fw-stack-2x fw-inverse"></i>' +
            '</span></span>',
            todayBtn: false,
            onSelect: self.convertDateToUnix,
            onClose: self.closeTimestampPicker

        });
    };

// convert the date string in to unix timestamp onSelect
    self.convertDateToUnix = function () {
        var form = $(this).closest('form[data-form-type="single"]');
        if (self.executionPlanDetailsMap[form.find('select[name="execution-plan-name"]').val()] !== 'FAULTY') {
            $(this).val(Date.parse($(this).val()));
        } else {
            $(this).val('');
        }
    };


// check whether the timestamp value is a unix timestamp onClose, if not convert date string into unix timestamp
    self.closeTimestampPicker = function () {
        var form = $(this).closest('form[data-form-type="single"]');
        if (self.executionPlanDetailsMap[form.find('select[name="execution-plan-name"]').val()] !== 'FAULTY') {
            if ($(this).val().includes('-')) {
                $(this).val(Date.parse($(this).val()));
            }
        } else {
            $(this).val('');
        }
    };

// create a new single event simulation config form
    self.addSingleEventConfigForm = function (e, ctx) {
        self.createSingleEventConfigForm(e, ctx);
        self.loadExecutionPlanNames(self.singleEventConfigCount);
        self.addSingleEventFormValidator(self.singleEventConfigCount);
        $('form[data-form-type="single"][data-uuid="' + self.singleEventConfigCount + '"] select[name="stream-name"]')
            .prop('disabled', true);
        self.singleEventConfigCount++;
    };

// create a single event config form
    self.createSingleEventConfigForm = function (event, ctx) {
        // can't assign the ul to a variable since we need to get the count and count changes dynamically
        var nextTab = $('ul#single-event-config-tab li').size() - 1;
        $(ctx).siblings().removeClass("active");
        // create the tab
        $(self.createListItem(nextTab, self.singleEventConfigCount)).insertBefore($(ctx));
        $("#single-event-config-tab-content").find(".tab-pane").removeClass("active");
        var singleEvent = self.singleEventForm.clone();
        singleEvent.attr('data-uuid', self.singleEventConfigCount + '');
        // create the tab content
        $(self.createDivForSingleEventTabContent(self.singleEventConfigCount))
            .appendTo('#single-event-config-tab-content');
        // $(singleEvent).appendTo('#event-content-' + self.singleEventConfigCount);
        $('#event-content-' + self.singleEventConfigCount).append(singleEvent);
        self.addDateTimePicker(self.singleEventConfigCount);
    };

    // create a list item for the single event form tabs
    self.createListItem = function (nextTab, singleEventConfigCount) {
        var listItem =
            '<li class="active" role="presentation" data-uuid="{{dynamicId}}">' +
            '   <a href="#event-content-parent-{{dynamicId}}" data-toggle="tab"' +
            '   aria-controls="event-configs" role = "tab">' +
            '       S {{nextTab}}' +
            '       <button type="button" class="close" name="delete" data-form-type="single"' +
            '       aria-label="Close">' +
            '           <span aria-hidden="true">×</span>' +
            '       </button>' +
            '   </a>' +
            '</li>';
        var temp = listItem.replaceAll('{{dynamicId}}', singleEventConfigCount);
        return temp.replaceAll('{{nextTab}}', nextTab);
    };

    // create a div for the tab content of single
    self.createDivForSingleEventTabContent = function (singleEventConfigCount) {
        var div =
            '<div role="tabpanel" class="tab-pane active" id="event-content-parent-{{dynamicId}}">' +
            '   <div class = "content" id="event-content-{{dynamicId}}">' +
            '   </div>' +
            '</div>';
        return div.replaceAll('{{dynamicId}}', singleEventConfigCount);
    };

// create jquery validators for single event forms
    self.addSingleEventFormValidator = function (uuid) {
        var form = $('form[data-form-type="single"][data-uuid="' + uuid + '"]');
        form.validate();
        form.find('[name="execution-plan-name"]').rules('add', {
            required: true,
            messages: {
                required: "Please select an execution plan name."
            }
        });
        form.find('[name="stream-name"]').rules('add', {
            required: true,
            messages: {
                required: "Please select a stream name."
            }
        });
        form.find('[name="timestamp"]').rules('add', {
            digits: true,
            messages: {
                digits: "Timestamp value must be a positive integer."
            }
        });
    };

// if the execution plan is not on run r debug mode, append buttons to start execution plan in either of the modes
    self.createRunDebugButtons = function () {
        var runDebugButtons =
            '<div class="col-md-8 btn-group " data-toggle="buttons">' +
            '   <label class="btn btn-primary active"> ' +
            '       <input type="radio" name="run-debug" value="run" autocomplete="off" checked> Run ' +
            '   </label> ' +
            '   <label class="btn btn-primary"> ' +
            '       <input type="radio" name="run-debug" value="debug" autocomplete="off"> Debug ' +
            '   </label> ' +
            '</div>' +
            '<div class="col-md-2">' +
            '   <button type="button" class="btn btn-default pull-right" name="start">Start</button>' +
            '</div>' +
            '<div class="col-md-12">' +
            '<label data-name="execution-plan-start-msg">' +
            '</label>' +
            '</div>';
        return runDebugButtons;
    };

// refresh the run debug buttons of single event forms which have the same execution plan name selected
    self.refreshRunDebugButtons = function (executionPlanName) {
        $('form[data-form-type="single"]').each(function () {
            var form = $(this);
            var uuid = form.data('uuid');
            var thisExecutionPlanName = form.find('select[name="execution-plan-name"]').val();
            if (thisExecutionPlanName !== null && thisExecutionPlanName === executionPlanName) {
                var mode = self.executionPlanDetailsMap[executionPlanName];
                form.find('div[data-name="execution-plan-name-mode"]').html('mode : ' + mode);
                form.find('label[data-name="execution-plan-start-msg"]').html('Started execution plan \'' +
                    executionPlanName + '\' in \'' + mode + '\' mode.');
                self.disableRunDebugButtonSection(form);
                form.find('button[type="submit"][name="send"]').prop('disabled', false);
            }
        });
    };

// disable the run, debug and start buttons
    self.disableRunDebugButtonSection = function (form) {
        $(form).find('input[name="run-debug"]').each(function () {
            $(this).prop('disabled', true)
        });
        $(form).find('button[name="start"]').prop('disabled', true);
    };

// remove the tab from the single event tabs list and remove its tab content
    self.removeSingleEventForm = function (ctx) {
        var x = $(ctx).parents("a").attr("href");
        var current = $('#single-event-config-tab-content ' + x);
        $(ctx).parents('li').prev().addClass('active');
        current.prev().addClass('active');
        current.remove();
        $(ctx).parents("li").remove();
    };

// rename the single event config tabs once a tab is deleted
    self.renameSingleEventConfigTabs = function () {
        var nextNum = 1;
        $('ul#single-event-config-tab li').each(function () {
            var uuid = $(this).data('uuid');
            if (uuid !== undefined) {
                $(this).find('a').html(self.createSingleListItemText(nextNum, uuid));
                nextNum++;
            }
        })
    };

// create text element of the single event tab list element
    self.createSingleListItemText = function (nextNum) {
        var listItemText =
            'S {{nextNum}}' +
            '<button type="button" class="close" name="delete" data-form-type="single"' +
            '       aria-label="Close">' +
            '   <span aria-hidden="true">×</span>' +
            '</button>';
        return listItemText.replaceAll('{{nextNum}}', nextNum);
    };

// load execution plan names to form
    self.loadExecutionPlanNames = function (uuid) {
        var executionPlanSelect = $('form[data-uuid="' + uuid + '"] select[name="execution-plan-name"]');
        Simulator.retrieveExecutionPlanNames(
            function (data) {
                self.createExecutionPlanMap(data);
                self.refreshExecutionPlanList(executionPlanSelect, Object.keys(self.executionPlanDetailsMap));
                $(executionPlanSelect).prop("selectedIndex", -1);
            },
            function (data) {
                log.info(data);
            }
        );

    };

// create a map containing execution plan name
    self.createExecutionPlanMap = function (data) {
        self.executionPlanDetailsMap = {};
        for (var i = 0; i < data.length; i++) {
            self.executionPlanDetailsMap[data[i]['executionPlaName']] = data[i]['mode'];
        }
    };

// create the execution plan name drop down
    self.refreshExecutionPlanList = function (executionPlanSelect, executionPlanNames) {
        var newExecutionPlans = self.generateOptions(executionPlanNames);
        $(executionPlanSelect).html(newExecutionPlans);
    };


// create the stream name drop down
    self.refreshStreamList = function (streamNameSelect, streamNames) {
        var newStreamOptions = self.generateOptions(streamNames);
        $(streamNameSelect).html(newStreamOptions);
    };

//    used to create options for available execution plans and streams
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


// create input fields for attributes
    self.refreshAttributesList = function (uuid, streamAttributes) {
        var newAttributesOption =
            '<table class="table table-responsive"> ' +
            '   <thead>' +
            '    <tr>' +
            '       <th width="90%">' +
            '           <label>' +
            '               Attributes<span class="requiredAstrix"> *</span>' +
            '           </label> ' +
            '       </th>' +
            '       <th width="10%">' +
            '           <label>' +
            '            Is Null' +
            '           </label>' +
            '       </th>' +
            '    </tr>' +
            '   </thead>' +
            '   <tbody data-name="attributes-body">' +
            '   </tbody>' +
            '</table>';

        var attrSection = $(newAttributesOption);
        attrSection.find('[data-name="attributes-body"]').html(self.generateAttributes(streamAttributes));
        var attributesDiv = $('form[data-uuid="' + uuid + '"] div[data-name="attributes"]');
        attributesDiv.html(attrSection);
        // if there are any boolean attributes set the selected option fo the drop down to -1
        $('select[data-input="bool"]').each(function () {
            $(this).prop('selectedIndex', -1);
        });
    };

// create input fields for attributes
    self.generateAttributes = function (attributes) {
        var booleanInput =
            '<tr>' +
            '   <td width="85%">' +
            '       <label>' +
            '           {{attributeName}} ({{attributeType}})' +
            '            <select data-element-type="attribute" name="{{attributeName}}"' +
            '            data-type ="{{attributeType}}" data-input="bool">' +
            '               <option value="true">True</option> ' +
            '               <option value="false">False</option> ' +
            '           </select>' +
            '      </label>' +
            '   </td>' +
            '   <td width="15%" class="align-middle">' +
            '       <input type="checkbox" name="{{attributeName}}" data-input="null" value="null">' +
            '   </td>' +
            '</tr>';

        var textInput =
            '<tr>' +
            '   <td width="85%">' +
            '       <label>' +
            '           {{attributeName}} ({{attributeType}})' +
            '           <input type="text" class="form-control" data-element-type="attribute"' +
            '           name="{{attributeName}}" data-type ="{{attributeType}}">' +
            '       </label>' +
            '   </td>' +
            '   <td width="15%" class="align-middle">' +
            '       <input align="center" type="checkbox" ' +
            '       name="{{attributeName}}" data-input="null" value="null">' +
            '   </td>' +
            '</tr>';

        var result = "";
        for (var i = 0; i < attributes.length; i++) {
            var temp;
            if (attributes[i]['type'] === 'BOOL') {
                temp = booleanInput.replaceAll('{{attributeName}}', attributes[i]['name']);
                result += temp.replaceAll('{{attributeType}}', attributes[i]['type'])
            } else {
                temp = textInput.replaceAll('{{attributeName}}', attributes[i]['name']);
                result += temp.replaceAll('{{attributeType}}', attributes[i]['type'])
            }
        }
        return result;
    };

// add rules for attribute
    self.addRulesForAttributes = function (uuid) {
        var attributes = $('form[data-form-type="single"][data-uuid="' + uuid + '"] [data-element-type="attribute"]');
        attributes.each(
            function () {
                self.addRuleForAttribute(this);
            }
        );
    };

// add a validation rule for an attribute based on the attribute type
    self.addRuleForAttribute = function (ctx) {
        var type = $(ctx).data("type");
        switch (type) {
            case 'BOOL' :
                $(ctx).rules('add', {
                    required: true,
                    messages: {
                        required: "Please specify an attribute value."
                    }
                });
                break;
            case 'INT' :
            case 'LONG' :
                $(ctx).rules('add', {
                    required: true,
                    validateIntOrLong: true,
                    messages: {
                        required: "Please specify an attribute value.",
                        validateIntOrLong: "Please specify a valid " + type.toLowerCase() + " value."
                    }
                });
                break;
            case 'DOUBLE' :
            case 'FLOAT' :
                $(ctx).rules('add', {
                    required: true,
                    validateFloatOrDouble: true,
                    messages: {
                        required: "Please specify an attribute value.",
                        validateFloatOrDouble: "Please specify a valid " + type.toLowerCase() + " value."
                    }
                });
                break;
        }
    };

// remove rules used for previous attributes
    self.removeSingleEventAttributeRules = function (uuid) {
        var attributes = $('form[data-form-type="single"][data-uuid="' + uuid + '"] [data-element-type="attribute"]');
        attributes.each(
            function () {
                self.removeRuleOfAttribute(this);
            }
        );
    };

// remove validation rule of an attribute
    self.removeRuleOfAttribute = function (ctx) {
        $(ctx).rules('remove');
    };

    return self;
});