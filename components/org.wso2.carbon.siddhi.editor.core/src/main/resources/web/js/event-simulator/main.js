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


define(['jquery', 'log', './simulator-rest-client', /* void libs */'bootstrap', 'theme_wso2', 'jquery_ui',
    'jquery_validate', 'jquery_timepicker', './templates'], function ($, log, Simulator) {

    "use strict";   // JS strict mode

    var self = {};

    self.singleEventConfigCount = 2;
    self.executionPlanDetailsMap = {};

    self.init = function () {

        // add methods to validate int/long and double/float
        $.validator.addMethod("validateIntOrLong", function (value, element) {
            return this.optional(element) || /^[-+]?[0-9]+$/.test(value);
        }, "Please provide a valid numerical value.");

        $.validator.addMethod("validateFloatOrDouble", function (value, element) {
            return this.optional(element) || /^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$/.test(value);
        }, "Please provide a valid numerical value.");


        //load execution plan names, a validator and a dateTImePicker to the default single event form 'S1'
        self.loadExecutionPlanNames('single_executionPlanName_1');
        self.addSingleEventFormValidator('1');
        self.addDateTimePicker('single_timestamp_1');

        // add a single event form
        $('#addSingleEventForm').on('click', function (e) {
            self.createSingleEventConfigForm(e, this);
            self.loadExecutionPlanNames('single_executionPlanName_' + self.singleEventConfigCount);
            self.addSingleEventFormValidator(self.singleEventConfigCount);
            self.singleEventConfigCount++;
        });

        $("#singleEventConfigs").on('focusin', 'select[id^="single_executionPlanName_"]', function () {
            var dynamicId = $(this).closest('form.singleEventForm').data('id');
            self.loadExecutionPlanNames('single_executionPlanName_' + dynamicId);
            console.log("Execution Plans loaded")
        });

        // change stream names on change function of execution plan name
        $("#singleEventConfigs").on('change', 'select[id^="single_executionPlanName_"]', function () {
            var elementId = this.id;
            var dynamicId = $(this).closest('form.singleEventForm').data('id');
            var streamId = 'single_streamName_' + dynamicId;
            var attributesId = 'single_attributes_' + dynamicId;
            var executionPlanName = $(this).val();
            $('#' + elementId + '_mode').html('mode : ' + self.executionPlanDetailsMap[executionPlanName]);
            self.removeSingleEventAttributeRules(dynamicId);
            $('#' + attributesId).empty();
            $('#single_runDebugButtons_' + dynamicId).empty();
            if (self.executionPlanDetailsMap[executionPlanName] === 'FAULTY') {
                $('#' + streamId).prop('disabled', true);
                $('#single_timestamp_' + dynamicId).prop('disabled', true);
                $('#single_sendEvent_' + dynamicId).prop('disabled', true);
            } else {
                $('#' + streamId).prop('disabled', false);
                $('#single_timestamp_' + dynamicId).prop('disabled', false);
                $('#single_sendEvent_' + dynamicId).prop('disabled', false);
                Simulator.retrieveStreamNames(
                    $('#' + elementId).val(),
                    function (data) {
                        self.refreshStreamList(streamId, data);
                        $('#' + streamId).prop("selectedIndex", -1);
                    },
                    function (data) {
                        console.log(data);
                    });
                if (self.executionPlanDetailsMap[executionPlanName] === 'STOP') {
                    $('#single_runDebugButtons_' + dynamicId).html(self.createRunDebugButtons(dynamicId));
                    $('#single_executionPlanStartMsg_' + dynamicId).html('Start execution plan \'' +
                        executionPlanName + '\' in either \'run\' or \'debug\' mode.');
                    $('#single_sendEvent_' + dynamicId).prop('disabled', true);
                }
            }
        });

        // change stream names on change function of stream name
        $("#singleEventConfigs").on('change', 'select[id^="single_streamName_"]', function () {
            var elementId = this.id;
            var dynamicId = elementId.substring(18, elementId.length);
            self.removeSingleEventAttributeRules(dynamicId);
            Simulator.retrieveStreamAttributes(
                $('#single_executionPlanName_' + dynamicId).val(),
                $('#single_streamName_' + dynamicId).val(),
                function (data) {
                    self.refreshAttributesList(dynamicId, data);
                    self.addRulesForAttributes(dynamicId);
                },
                function (data) {
                    console.log(data);
                });
        });

        // start inactive execution plans in run or debug mode
        $("#singleEventConfigs").on('click', 'button[id^="single_start_"]', function () {
            var dynamicId = $(this).closest('form.singleEventForm').data('id');
            var buttonName = 'single_runDebug_' + dynamicId;
            var executionPlanName = $('#single_executionPlanName_' + dynamicId).val();
            var mode = $('input[name=' + buttonName + ']:checked').val();
            if (mode === 'run') {
                $.ajax({
                    async: true,
                    url: "http://localhost:9090/editor/" + executionPlanName + "/start",
                    type: "GET",
                    success: function (data) {
                        console.log(data)
                    },
                    error: function (msg) {
                        console.error(msg)
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
                            console.log(data)
                    },
                    error: function (msg) {
                        if (typeof error === 'function')
                            console.error(msg)
                    }
                });
                self.executionPlanDetailsMap[executionPlanName] = 'DEBUG';
            }
            self.refreshRunDebugButtons(executionPlanName);
        });

        // remove a single event config tab and make the tab before it active
        $('#singleEventConfigTab').on('click', 'button[id^="delete_singleEventConfig_"]', function () {
            self.removeSingleEventForm(this);
            self.renameSingleEventConfigTabs();
        })

        // is isNull checkbox is checked disable txt input, else enable text input
        $('#singleEventConfigs').on('click', 'input[id^="single_attributes_"][id$="_null"]', function () {
            var elementId = this.id;
            var inputId = elementId.substring(0, (elementId.length - 5));
            if ($(this).is(':checked')) {
                if ($('#' + inputId).is(':text')) {
                    $('#' + inputId).val('');
                    $('#' + inputId).prop('disabled', true);
                } else {
                    $('#' + inputId).prop('selectedIndex', -1);
                    $('#' + inputId).prop('disabled', true);
                }
                self.removeRuleOfAttribute($('#' + inputId));
            } else {
                if ($('#' + inputId).is(':text')) {
                    $('#' + inputId).prop('disabled', false);
                } else {
                    $('#' + inputId).prop('disabled', false);
                }
                self.addRuleForAttribute($('#' + inputId));
            }

        });


        // submit function of single event
        $(document).on('submit', 'form.singleEventForm', function (e) {
            e.preventDefault();
            // serialize all the forms elements and their values
            var form = $(this);
            var formValues = form.serializeArray();
            var formDataMap = {};
            var attributes = [];
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
                console.error("Execution plan name is required for single event simulation.");
            }
            if (!('streamName' in formDataMap) || formDataMap['streamName'].length === 0) {
                console.error("Stream name is required for single event simulation.");
            }
            if (('timestamp' in formDataMap) && parseInt(formDataMap['timestamp']) < 0) {
                console.error("Timestamp value must be a positive value for single event simulation.");
            }
            if (attributes.length === 0) {
                console.error("Attribute values are required for single event simulation.");
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
                        console.log(data);
                        setTimeout(function () {
                            form.loading('hide');
                        }, 250)
                    },
                    function (data) {
                        console.error(data);
                        setTimeout(function () {
                            form.loading('hide');
                        }, 250)
                    })
            }
        });
    };

// add a datetimepicker to an element
    self.addDateTimePicker = function (elementId) {
        $('#' + elementId).datetimepicker({
            dateFormat: 'yy-mm-dd',
            timeFormat: 'HH:mm:ss:l',
            showOn: 'button',
            buttonText: '<span class="fw-stack"><i class="fw fw-square-outline fw-stack-2x"></i><i class="fw fw-calendar fw-stack-1x"></i><span class="fw-stack fw-move-right fw-move-bottom"><i class="fw fw-circle fw-stack-2x fw-stroke"></i><i class="fw fw-clock fw-stack-2x fw-inverse"></i></span></span>',
            todayBtn: false,
            onSelect: self.convertDateToUnix,
            onClose: self.closeTimestampPicker

        });
    };

// convert the date string in to unix timestamp onSelect
    self.convertDateToUnix = function () {
        $(this).val(Date.parse($(this).val()));
    };


// check whether the timestamp value is a unix timestamp onClose, if not convert date string into unix timestamp
    self.closeTimestampPicker = function () {
        if ($(this).val().includes('-')) {
            $(this).val(Date.parse($(this).val()));
        }
    };

// create a single event config form
    self.createSingleEventConfigForm = function (event, ctx) {
        var nextTab = $('ul#singleEventConfigTab li').size();

        $(ctx).siblings().removeClass("active");

        // create the tab
        $(self.createListItem(nextTab, self.singleEventConfigCount)).insertBefore($(ctx));

        $("#singleEventConfigTabContent").find(".tab-pane").removeClass("active");

        var singleEvent = singleTemplate.replaceAll('{{dynamicId}}', self.singleEventConfigCount);

        // create the tab content
        $(self.createDivForSingleEventTabContent(self.singleEventConfigCount)).appendTo('#singleEventConfigTabContent');
        $('#singleEventContent_' + self.singleEventConfigCount).html(singleEvent);

        self.addDateTimePicker('single_timestamp_' + self.singleEventConfigCount);
    };

    // create a list item for the single event form tabs
    self.createListItem = function (nextTab, singleEventConfigCount) {
        var listItem =
            '<li class="active" role="presentation" id = "single_ListItem_{{dynamicId}}" data-id="{{dynamicId}}">' +
            '   <a href="#singleEventContent_parent_{{dynamicId}}" data-toggle="tab" ' +
            '   id = "singleEventConfig_{{dynamicId}}" aria-controls="singleEventConfigs" ' +
            '   role = "tab">' +
            '       S {{nextTab}}' +
            '       <button type="button" class="close" id="delete_singleEventConfig_{{dynamicId}}" ' +
            '       aria-label="Close">' +
            '           <span aria-hidden="true">×</span>' +
            '       </button>' +
            '   </a>' +
            '</li>'
        var temp = listItem.replaceAll('{{dynamicId}}', singleEventConfigCount);
        return temp.replaceAll('{{nextTab}}', nextTab);
    };

    // create a div for the tab content of single
    self.createDivForSingleEventTabContent = function (singleEventConfigCount) {
        var div =
            '<div role="tabpanel" class="tab-pane active" id="singleEventContent_parent_{{dynamicId}}">' +
            '   <div class = "content" id="singleEventContent_{{dynamicId}}">' +
            '   </div>' +
            '</div>';
        return div.replaceAll('{{dynamicId}}', singleEventConfigCount);
    };

// create jquery validators for single event forms
    self.addSingleEventFormValidator = function (formId) {
        $('#singleEventForm_' + formId).validate();
        $('#single_executionPlanName_' + formId).rules('add', {
            required: true,
            messages: {
                required: "Please select an execution plan name."
            }
        });
        $('#single_streamName_' + formId).rules('add', {
            required: true,
            messages: {
                required: "Please select a stream name."
            }
        });
        $('#single_timestamp_' + formId).rules('add', {
            digits: true,
            messages: {
                digits: "Timestamp value must be a positive integer."
            }
        });
    };

// if the execution plan is not on run r debug mode, append buttons to start execution plan in either of the modes
    self.createRunDebugButtons = function (dynamicId) {
        var runDebugButtons =
            '<div class="col-md-8 btn-group " data-toggle="buttons">' +
            '   <label class="btn btn-primary active"> ' +
            '       <input type="radio" id="single_run_{{dynamicId}}"' +
            '       name="single_runDebug_{{dynamicId}}"' +
            '       value="run" autocomplete="off" checked> Run ' +
            '   </label> ' +
            '   <label class="btn btn-primary"> ' +
            '       <input type="radio" id="single_debug_{{dynamicId}}"' +
            '        name="single_runDebug_{{dynamicId}}"' +
            '       value="debug" autocomplete="off"> Debug ' +
            '   </label> ' +
            '</div>' +
            '<div class="col-md-2">' +
            '   <button type="button" class="btn btn-default pull-right" id="single_start_{{dynamicId}}"' +
            '    name="single_start_{{dynamicId}}">Start</button>' +
            '</div>' +
            '<div class="col-md-12">' +
            '<label id="single_executionPlanStartMsg_{{dynamicId}}">' +
            '</label>' +
            '</div>' ;
        return runDebugButtons.replaceAll('{{dynamicId}}', dynamicId);
    };

// refresh the run debug buttons of single event forms which have the same execution plan name selected
    self.refreshRunDebugButtons = function (executionPlanName) {
        $('.singleEventForm').each(function () {
            var dynamicId = $(this).data('id');
            var thisExecutionPlanName = $('#single_executionPlanName_' + dynamicId).val();
            if (thisExecutionPlanName !== null && thisExecutionPlanName === executionPlanName) {
                var mode = self.executionPlanDetailsMap[executionPlanName];
                $('#single_executionPlanName_' + dynamicId + '_mode').html('mode : ' + mode);
                $('#single_executionPlanStartMsg_' + dynamicId).html('Started execution plan \'' +
                    executionPlanName + '\' in \'' + mode + '\' mode.');
                self.disableRunDebugButtonSection(dynamicId);
                $('#single_sendEvent_' + dynamicId).prop('disabled', false);
            }
        });
    };

// disable the run, debug and start buttons
    self.disableRunDebugButtonSection = function (dynamicId) {
        $('#single_run_' + dynamicId).prop('disabled', true);
        $('#single_debug_' + dynamicId).prop('disabled', true);
        $('#single_start_' + dynamicId).prop('disabled', true);
    };

// remove the tab from the single event tabs list and remove its tab content
    self.removeSingleEventForm = function (ctx) {
        var x = $(ctx).parents("a").attr("href");
        $(ctx).parents('li').prev().addClass('active');
        $('#singleEventConfigTabContent ' + x).prev().addClass('active');
        $('#singleEventConfigTabContent ' + x).remove();
        $(ctx).parents("li").remove();
    };

// rename the single event config tabs once a tab is deleted
    self.renameSingleEventConfigTabs = function () {
        var nextNum = 2;
        $('li[id^="single_ListItem_"]').each(function () {
            var elementId = this.id;
            if (elementId !== 'single_ListItem_1') {
                var dynamicId = $(this).data('id');
                $('a[id=singleEventConfig_' + dynamicId + ']').html(self.createSingleListItemText(nextNum, dynamicId));
                nextNum++;
            }
        })
    };

// create text element of the single event tab list element
    self.createSingleListItemText = function (nextNum, dynamicId) {
        var listItemText =
            'S {{nextNum}}' +
            '<button type="button" class="close" id="delete_singleEventConfig_{{dynamicId}}" aria-label="Close">' +
            '   <span aria-hidden="true">×</span>' +
            '</button>';
        var temp = listItemText.replaceAll('{{dynamicId}}', dynamicId);
        return temp.replaceAll('{{nextNum}}', nextNum);
    };

// load execution plan names to form
    self.loadExecutionPlanNames = function (elementId) {
        Simulator.retrieveExecutionPlanNames(
            function (data) {
                self.createExecutionPlanMap(data);
                self.refreshExecutionPlanList(elementId, Object.keys(self.executionPlanDetailsMap));
                $('#' + elementId).prop("selectedIndex", -1);
            },
            function (data) {
                console.log(data);
            }
        );

    };

// create a map containing execution plan name
    self.createExecutionPlanMap = function (data) {
        for (var i = 0; i < data.length; i++) {
            self.executionPlanDetailsMap[data[i]['executionPlaName']] = data[i]['mode'];
        }
    };

// create the execution plan name drop down
    self.refreshExecutionPlanList = function (elementId, executionPlanNames) {
        var newExecutionPlans = self.generateOptions(executionPlanNames);
        $('#' + elementId).html(newExecutionPlans);
    };


// create the stream name drop down
    self.refreshStreamList = function (elementId, streamNames) {
        var newStreamOptions = self.generateOptions(streamNames);
        $('#' + elementId).html(newStreamOptions);
    };

//    used to create options for available execution plans and streams
    self.generateOptions = function (dataArray) {
        var dataOption =
            '<option value = "{{dataName}}">' +
            '   {{dataName}}' +
            '</option>';
        var result = ''; // '<option disabled selected value> -- select an option -- </option>';
        for (var i = 0; i < dataArray.length; i++) {
            result += dataOption.replaceAll('{{dataName}}', dataArray[i]);
        }
        return result;
    };


// create input fields for attributes
    self.refreshAttributesList = function (dynamicId, streamAttributes) {
        var newAttributesOption =
            '<table class="table table-responsive"> ' +
            '   <thead>' +
            '    <tr>' +
            '       <th width="90%">' +
            '           <label for="single_attributes_{{dynamicId}}">' +
            '               Attributes<span class="requiredAstrix"> *</span>' +
            '           </label> ' +
            '       </th>' +
            '       <th width="10%">' +
            '           <label for="single_attributes_{{dynamicId}}">' +
            '            Is Null' +
            '           </label>' +
            '       </th>' +
            '    </tr>' +
            '   </thead>' +
            '   <tbody id="single_attributesTableBody_{{dynamicId}}">' +
            '   </tbody>' +
            '</table>';
        $('#single_attributes_' + dynamicId).html(newAttributesOption.replaceAll('{{dynamicId}}', dynamicId));
        $('#single_attributesTableBody_' + dynamicId).html(self.generateAttributes(dynamicId, streamAttributes));
        // if there are any boolean attributes set the selected option fo the drop down to -1
        $('select[class^="single-event-attribute-"]').each(function () {
            $(this).prop('selectedIndex', -1);
        });
    };

// create input fields for attributes
    self.generateAttributes = function (dynamicId, attributes) {
        var booleanInput =
            '<tr>' +
            '   <td width="85%">' +
            '       <label for="single_attributes_{{dynamicId}}_{{attributeName}}_true">' +
            '           {{attributeName}} ({{attributeType}})' +
            '            <select class="single-event-attribute-{{dynamicId}} form-control"' +
            '            name="single_attributes_{{dynamicId}}_{{attributeName}}"' +
            '            id="single_attributes_{{dynamicId}}_{{attributeName}}" data-id="{{dynamicId}}"' +
            '            data-type ="{{attributeType}}">' +
            '               <option value="true">True</option> ' +
            '               <option value="false">False</option> ' +
            '           </select>' +
            '      </label>' +
            '   </td>' +
            '   <td width="15%" class="align-middle">' +
            '       <input type="checkbox" name="single_attributes_{{dynamicId}}_{{attributeName}}_null"' +
            '       id="single_attributes_{{dynamicId}}_{{attributeName}}_null">' +
            '   </td>' +
            '</tr>';

        var textInput =
            '<tr>' +
            '   <td width="85%">' +
            '       <label for ="single_attributes_{{dynamicId}}_{{attributeName}}">' +
            '           {{attributeName}} ({{attributeType}})' +
            '           <input type="text" class="form-control single-event-attribute-{{dynamicId}}"' +
            '           name="single_attributes_{{dynamicId}}_{{attributeName}}" ' +
            '           id="single_attributes_{{dynamicId}}_{{attributeName}}" data-id="{{dynamicId}}"' +
            '           data-type ="{{attributeType}}">' +
            '       </label>' +
            '   </td>' +
            '   <td width="15%" class="align-middle">' +
            '       <input align="center" type="checkbox" ' +
            '       name="single_attributes_{{dynamicId}}_{{attributeName}}_null"' +
            '       id="single_attributes_{{dynamicId}}_{{attributeName}}_null">' +
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
        return result.replaceAll('{{dynamicId}}', dynamicId);
    };

// add rules for attribute
    self.addRulesForAttributes = function (elementId) {
        $('.single-event-attribute-' + elementId).each(
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
    self.removeSingleEventAttributeRules = function (elementId) {
        $('.single-event-attribute-' + elementId).each(
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