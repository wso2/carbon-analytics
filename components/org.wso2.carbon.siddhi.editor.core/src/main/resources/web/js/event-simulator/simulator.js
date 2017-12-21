/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['jquery', 'log', './simulator-rest-client', 'lodash', './open-siddhi-apps', 
    /* void libs */'bootstrap', 'theme_wso2', 'jquery_ui', 'jquery_validate', 'jquery_timepicker', './templates'], 
    function ($, log, Simulator, _, OpenSiddhiApps) {

    "use strict";   // JS strict mode

    var self = {};

    self.init = function (config) {

        self.singleEventConfigCount = 1;
        self.siddhiAppDetailsMap = {};
        self.singleEventForm = $('#single-event-form').find('form').clone();
        self.$singleEventConfigTabContent = $("#single-event-config-tab-content");
        self.$singleEventConfigs = $("#single-event-configs");
        self.$addSingleEventForm = $('#add-single-event-form');
        self.FAULTY = 'FAULTY';
        self.STOP = 'STOP';
        self.RUN = 'RUN';
        self.DEBUG = 'DEBUG';
        self.app = _.get(config, 'application');
        self.baseUrl = config.application.config.baseUrl;
        self.workspace = self.app.workspaceManager;
        self.SiddhiAppStatus = "Siddhi App Status : ";
        self.startAndSendLabel = "Start and Send";
        self.sendLabel = "Send";

        // add methods to validate int/long and double/float
        $.validator.addMethod("validateIntOrLong", function (value, element) {
            return this
                    .optional(element) || /^[-+]?[0-9]+$/.test(value);
        }, "Please provide a valid numerical value.");

        $.validator.addMethod("validateFloatOrDouble", function (value, element) {
            return this
                    .optional(element) || /^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$/.test(value);
        }, "Please provide a valid numerical value.");

        // add the first single event form and disable the delete button
        self.addSingleEventConfigForm(null, self.$addSingleEventForm);
        $('li[data-uuid="1"] button[name="delete"]')
            .prop('disabled', true)
            .hide();

        // add a single event form
        self.$addSingleEventForm.on('click', function (e) {
            self.addSingleEventConfigForm(e, this);
        });

        // refresh the siddhi app name drop down when siddhi apps are added to or removed from the workspace
        self.$singleEventConfigTabContent.on('focusin', 'select[name="single-event-siddhi-app-name"]', function () {
            var $element = $(this);
            var $form = $element.closest('form[data-form-type="single"]');
            var uuid = $form.data('uuid');
            self.loadSiddhiAppNames(uuid);
        });

        // When siddhi app name selected changes refresh the form
        self.$singleEventConfigTabContent.on('change', 'select[name="single-event-siddhi-app-name"]', function () {
            var $element = $(this);
            var $form = $element.closest('form[data-form-type="single"]');
            var uuid = $form.data('uuid');
            var siddhiAppName = $element.val();
            var $siddhiAppMode = $form.find('div[data-name="siddhi-app-name-mode"]');
            var $streamNameSelect = $form.find('select[name="stream-name"]');
            var $timestamp = $form.find('input[name="sim-timestamp"]');
            var $attributes = $form.find('div[data-name="attributes"]');
            var $runDebugButtons = $form.find('div[data-name="run-debug-buttons"]');
            var $send = $form.find('button[type="submit"][name="send"]');
            var $singleEventForm = $element.closest(".single-event-form");
            var $nitificationBox = $singleEventForm.find(".alert");

            $streamNameSelect.empty();
            $timestamp.val('');
            $siddhiAppMode.html(self.SiddhiAppStatus + self.siddhiAppDetailsMap[siddhiAppName]);
            self.removeSingleEventAttributeRules(uuid);
            $attributes.empty();
            $runDebugButtons.empty();
            if (self.siddhiAppDetailsMap[siddhiAppName] === self.FAULTY) {
                $streamNameSelect.prop('disabled', true);
                $timestamp.prop('disabled', true);
                $send.prop('disabled', true);
            } else {
                $streamNameSelect.prop('disabled', false);
                $timestamp.prop('disabled', false);
                $send.prop('disabled', false);
                Simulator.retrieveStreamNames(
                    siddhiAppName,
                    function (data) {
                        self.refreshStreamList($streamNameSelect, data);
                        var firstOptionVal = $streamNameSelect.children().first().val();
                        $streamNameSelect.val(firstOptionVal).change();
                    },
                    function (data) {
                        log.info(data);
                    });
                $nitificationBox.removeClass("hidden");
                var $sendButton = $singleEventForm.find('button[name="send"]');
                if (self.siddhiAppDetailsMap[siddhiAppName] === self.STOP) {
                    $nitificationBox.removeClass("alert-success");
                    $nitificationBox.addClass("alert-warning");
                    $runDebugButtons.html(self.createRunDebugButtons());
                    $sendButton.text(self.startAndSendLabel);
                    $form
                        .find('label[data-name="siddhi-app-start-msg"]')
                        .html('starting mode for siddhi app');
                } else {
                    $nitificationBox.addClass("alert-success");
                    $nitificationBox.removeClass("alert-warning");
                    $sendButton.text("Send");
                }
            }
        });

        // refresh attributes list when a stream is selected
        self.$singleEventConfigTabContent.on('change', 'select[name="stream-name"]', function () {
            var $form = $(this).closest('form[data-form-type="single"]');
            var uuid = $form.data('uuid');
            self.removeSingleEventAttributeRules(uuid);
            Simulator.retrieveStreamAttributes(
                $form
                    .find('select[name="single-event-siddhi-app-name"]')
                    .val(),
                $form
                    .find('select[name="stream-name"]')
                    .val(),
                function (data) {
                    self.refreshAttributesList(uuid, data);
                    self.addRulesForAttributes(uuid);
                },
                function (data) {
                    log.info(data);
                });
        });

        // remove a single event config tab and make the tab before it active
        self.$singleEventConfigs.on('click', 'button[name="delete"][data-form-type="single"]',
            function () {
                self.removeSingleEventForm(this);
                self.renameSingleEventConfigTabs();
            });

        // is isNull checkbox is checked disable txt input, else enable text input
        self.$singleEventConfigTabContent.on('click', 'input[data-input="null"]', function () {
            var $element = $(this);
            var $form = $element.closest('form[data-form-type="single"]');
            var attributeName = $element.data('attribute-name');
            var $inputField = $form.find('[data-element-type="attribute"][name="' + attributeName + '"]');
            if ($element.is(':checked')) {
                if ($inputField.is(':text')) {
                    $inputField
                        .val('')
                        .prop('disabled', true);
                } else {
                    $inputField
                        .prop('selectedIndex', -1)
                        .prop('disabled', true);
                }
                self.removeRuleOfAttribute($inputField);
            } else {
                $inputField
                    .prop('disabled', false);
                self.addRuleForAttribute($inputField);
            }

        });


        // submit function of single event
        self.$singleEventConfigTabContent.on('submit', 'form[data-form-type="single"]', function (e) {
            e.preventDefault();
            var $form = $(this);
            var formValues = _.keyBy($form.serializeArray(), 'name');
            var formDataMap = {};
            var attributes = [];
            var i = 0;

            _.forEach(formValues, function (object, key) {
                if (key === 'single-event-siddhi-app-name' && object.value.length > 0) {
                    _.set(formDataMap, 'siddhiAppName', object.value);
                } else if (key === 'stream-name' && object.value.length > 0) {
                    _.set(formDataMap, 'streamName', object.value);
                } else if (key === 'sim-timestamp' && object.value.length > 0) {
                    _.set(formDataMap, 'timestamp', object.value);
                } else if (key.endsWith('-null')) {
                    attributes[i++] = null;
                } else if (key.endsWith('-attr')) {
                    attributes[i++] = object.value;
                }
            });


            if (!_.has(formDataMap, 'siddhiAppName')) {
                log.error("Siddhi app name is required for single event simulation.");
            }
            if (!_.has(formDataMap, 'streamName')) {
                log.error("Stream name is required for single event simulation.");
            }
            if (_.has(formDataMap, 'timestamp') && parseInt(_.get(formDataMap, 'timestamp')) < 0) {
                log.error("Timestamp value must be a positive integer for single event simulation.");
            }
            if (attributes.length === 0) {
                log.error("Attribute values are required for single event simulation.");
            }
            if (self.siddhiAppDetailsMap[_.get(formDataMap, 'siddhiAppName')] == "STOP") {
                self.startInactiveSiddhiApp($form);
                log.info(_.get(formDataMap, 'siddhiAppName') + " is stopped");
            }

            if (_.has(formDataMap, 'siddhiAppName')
                && _.has(formDataMap, 'streamName')
                && attributes.length > 0) {
                _.set(formDataMap, 'data', attributes);
                $form.loading('show');
                Simulator.singleEvent(
                    JSON.stringify(formDataMap),
                    function (data) {
                        log.info(data);
                        setTimeout(function () {
                            $form.loading('hide');
                        }, 250)
                    },
                    function (data) {
                        log.error(data);
                        setTimeout(function () {
                            $form.loading('hide');
                        }, 250)
                    })
            }
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

    self.addDateTimePicker = function (uuid) {
        var $timestamp = $('form[data-form-type="single"][data-uuid="' + uuid + '"] input[name="sim-timestamp"]');
        $timestamp.datetimepicker({
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

// convert the date string in to unix timestamp onSelect
    self.convertDateToUnix = function () {
        var $element = $(this);
        var $form = $element.closest('form[data-form-type="single"]');
        if (self.siddhiAppDetailsMap[$form.find('select[name="single-event-siddhi-app-name"]').val()] !== self.FAULTY) {
            var date = $element.val();
            var patt = new RegExp("^((\\d)+||NaN)$");
            if(patt.test(date)){
                 return;
            }
            var dateParts = date.split(/[^0-9]/);
            var time=new Date(dateParts[0],dateParts[1]-1,dateParts[2],dateParts[3],dateParts[4],dateParts[5]).getTime()
                + parseInt(dateParts[6]) ;
            $element.val(time);
        } else {
            $element.val('');
        }
    };


// check whether the timestamp value is a unix timestamp onClose, if not convert date string into unix timestamp
    self.closeTimestampPicker = function () {
        var $element = $(this);
        var $form = $element.closest('form[data-form-type="single"]');
        if (self.siddhiAppDetailsMap[$form.find('select[name="single-event-siddhi-app-name"]').val()] !== self.FAULTY) {
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

    //create a new single event simulation config form
    self.addSingleEventConfigForm = function (e, ctx) {
        self.createSingleEventConfigForm(e, ctx);
        self.loadSiddhiAppNames(self.singleEventConfigCount);
        self.addSingleEventFormValidator(self.singleEventConfigCount);
        $('form[data-form-type="single"][data-uuid="' + self.singleEventConfigCount + '"] select[name="stream-name"]')
            .prop('disabled', true);
        self.singleEventConfigCount++;
        self.renameSingleEventConfigTabs();
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
            '            <i class="fw fw-cancel"></i>' +
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
        var $form = $('form[data-form-type="single"][data-uuid="' + uuid + '"]');
        $form.validate();
        $form.find('[name="single-event-siddhi-app-name"]').rules('add', {
            required: true,
            messages: {
                required: "Please select an siddhi app name."
            }
        });
        $form.find('[name="stream-name"]').rules('add', {
            required: true,
            messages: {
                required: "Please select a stream name."
            }
        });
        $form.find('[name="sim-timestamp"]').rules('add', {
            digits: true,
            messages: {
                digits: "Timestamp value must be a positive integer."
            }
        });
    };

// if the siddhi app is not on run or debug mode, append buttons to start siddhi app in either of the modes
    self.createRunDebugButtons = function () {
        var runDebugButtons =
            '<div class="col-md-12 row">' +
            '<label data-name="siddhi-app-start-msg">' +
            '</label>' +
            '</div>'+
            '<div class="switch-toggle switch-ios col-md-6">' +
                '<input id="run" name="run-debug" checked type="radio" value="run">' +
                '<label for="run" onclick="">Run</label>' +
                '<input id="debug" name="run-debug" type="radio" value="debug">' +
                '<label for="debug" onclick="">Debug</label>' +
                '<a></a>' +
            '</div>';
        return runDebugButtons;
    };

// refresh the run debug buttons of single event forms which have the same siddhi app name selected
    self.refreshRunDebugButtons = function (siddhiAppName) {
        $('form[data-form-type="single"]').each(function () {
            var $form = $(this);
            var uuid = $form.data('uuid');
            var thisSiddhiAppName = $form.find('select[name="single-event-siddhi-app-name"]').val();
            if (thisSiddhiAppName !== null && thisSiddhiAppName === siddhiAppName) {
                var mode = self.siddhiAppDetailsMap[siddhiAppName];
                $form
                    .find('div[data-name="siddhi-app-name-mode"]')
                    .html(self.SiddhiAppStatus + mode);
                $form
                    .find('label[data-name="siddhi-app-start-msg"]')
                    .html('Started siddhi app \'' +
                        siddhiAppName + '\' in \'' + mode + '\' mode.');
                self.disableRunDebugButtonSection($form);
                $form
                    .find('button[type="submit"][name="send"]')
                    .prop('disabled', false);
            }
        });
    };

// disable the run, debug and start buttons
    self.disableRunDebugButtonSection = function ($form) {
        $form.find('input[name="run-debug"]').each(function () {
            $(this)
                .prop('disabled', true)
        });
        $form
            .find('button[name="start"]')
            .prop('disabled', true);
    };

    //remove the tab from the single event tabs list and remove its tab content
    self.removeSingleEventForm = function (ctx) {
        var simulationName = $(ctx).parents("a").text();
        var x = $(ctx).parents("a").attr("href");
        var $current = $('#single-event-config-tab-content ' + x);
        if ("S 1" == simulationName.trim()) {
            $(ctx)
                .parents('li')
                .next()
                .addClass('active');
            $current
                .next()
                .addClass('active');
        } else {
            $(ctx)
                .parents('li')
                .prev()
                .addClass('active');
            $current
                .prev()
                .addClass('active');
        }
        $current
            .remove();
        $(ctx)
            .parents("li")
            .remove();
    };

    // rename the single event config tabs once a tab is deleted
    self.renameSingleEventConfigTabs = function () {
        var nextNum = 1;
        var $singleEventConfigTabs = $('#event-simulator #single-event-configs ul#single-event-config-tab li');
        var numOfTabs = $singleEventConfigTabs.size() - 1;
        $singleEventConfigTabs.each(function () {
            var $element = $(this);
            var uuid = $element.data('uuid');
            if (uuid !== undefined) {
                $element
                    .find('a')
                    .html(self.createSingleListItemText(nextNum, numOfTabs));
                nextNum++;
            }
        })
    };

    // create text element of the single event tab list element
    self.createSingleListItemText = function (nextNum, numOfTabs) {
        var listItemText;
        if (nextNum == 1 && numOfTabs == 1) {
            listItemText =
                'S {{nextNum}}' +
                '<button type="button" class="close" name="delete" data-form-type="single"' +
                '       aria-label="Close" disabled style="display: none;">' +
                '    <i class="fw fw-cancel"></i>' +
                '</button>';
        } else {
            listItemText =
                'S {{nextNum}}' +
                '<button type="button" class="close" name="delete" data-form-type="single"' +
                '       aria-label="Close">' +
                '    <i class="fw fw-cancel"></i>' +
                '</button>';
        }
        return listItemText.replaceAll('{{nextNum}}', nextNum);
    };

// load siddhi app names to form
    self.loadSiddhiAppNames = function (uuid) {
        var $siddhiAppSelect = $('form[data-uuid="' + uuid + '"] select[name="single-event-siddhi-app-name"]');
        var siddhiAppName = $siddhiAppSelect.val();
        Simulator.retrieveSiddhiAppNames(
            function (data) {
                self.createSiddhiAppMap(data);
                self.refreshSiddhiAppList($siddhiAppSelect, Object.keys(self.siddhiAppDetailsMap));
                self.selectSiddhiAppOptions($siddhiAppSelect, siddhiAppName);
            },
            function (data) {
                log.info(data);
            }
        );

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
        var initialOptionValue = "";
        if(siddhiAppNames.length == 0){
            initialOptionValue += '<option value = "-1" disabled>-- No saved Siddhi Apps available. --</option>';
        } else{
            initialOptionValue = '<option value = "-1" disabled>-- Please Select a Siddhi App --</option>';
        }

        var newSiddhiApps = self.generateOptions(siddhiAppNames,initialOptionValue);
        $siddhiAppSelect.html(newSiddhiApps);
        $siddhiAppSelect.find('option[value="-1"]').attr("selected",true);
    };

// select an option from the siddhi app name drop down
    self.selectSiddhiAppOptions = function ($siddhiAppSelect, siddhiAppName) {
        /*
         * if an siddhi app has been already selected when the siddhi app name list was refreshed,
         * check whether the siddhi app still exists in the workspace, if yes, make that siddhi app name the
         * selected value.
         * If the siddhi app no longer exists in the work space, set the selected option to -1 and refresh the form
         * */
        if (siddhiAppName in self.siddhiAppDetailsMap) {
            $siddhiAppSelect
                .val(siddhiAppName);
        } else {
            if (siddhiAppName !== null) {
                var $form = $siddhiAppSelect.closest('form[data-form-type="single"]');
                $form
                    .find('div[data-name="siddhi-app-name-mode"]')
                    .empty();
                $form
                    .find('select[name="stream-name"]')
                    .empty()
                    .prop('disabled', true);
                $form
                    .find('input[name="sim-timestamp"]')
                    .empty();
                $form
                    .find('div[data-name="attributes"]')
                    .empty();
                $form
                    .find('div[data-name="run-debug-buttons"]')
                    .empty();
            }
        }
    };


// create the stream name drop down
    self.refreshStreamList = function ($streamNameSelect, streamNames) {
        $streamNameSelect.children().first().remove();
        var initialOptionValue = '<option value = "-1" disabled>-- Please Select a Stream Name --</option>';
        var newStreamOptions = self.generateOptions(streamNames,initialOptionValue);
        $streamNameSelect.html(newStreamOptions);
        $streamNameSelect.find('option[value="-1"]').attr("selected",true);
    };

//    used to create options for available siddhi apps and streams
    self.generateOptions = function (dataArray,initialOptionValue) {
        var dataOption =
            '<option value = "{{dataName}}">' +
            '   {{dataName}}' +
            '</option>';
        var result = '';

        if(initialOptionValue !== undefined){
            result += initialOptionValue;
        }

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

        var $attrSection = $(newAttributesOption);
        var $form = $('form[data-uuid="' + uuid + '"]');
        $attrSection
            .find('[data-name="attributes-body"]')
            .html(self.generateAttributes(streamAttributes, $form));
        var $attributesDiv = $('form[data-uuid="' + uuid + '"] div[data-name="attributes"]');
        $attributesDiv.html($attrSection);

        $form.removeData('validate');
        for (var i = 0; i < streamAttributes.length; i++) {
            if (streamAttributes[i]['type'] === 'BOOL') {
                $form.find('select[name="'+streamAttributes[i]['name']+'-attr"]').rules('add', {
                    required: true,
                    messages: {
                        required: "Please select a value."
                    }
                });
            } else {
                $form.find('input[name="'+streamAttributes[i]['name']+'-attr"]').rules('add', {
                    required: true,
                    messages: {
                        digits: "Please enter a value."
                    }
                });
            }
        }
        // if there are any boolean attributes set the selected option fo the drop down to -1
        $('select[data-input="bool"]').each(function () {
            $(this)
                .prop('selectedIndex', -1);
        });
    };

// create input fields for attributes
    self.generateAttributes = function (attributes, $form) {
        var booleanInput =
            '<tr>' +
            '   <td width="85%">' +
            '       <label>' +
            '           {{attributeName}} ({{attributeType}})' +
            '      </label>' +
            '            <select class="form-control" data-element-type="attribute" name="{{attributeName}}-attr"' +
            '            data-type ="{{attributeType}}" data-input="bool">' +
            '               <option value="true">True</option> ' +
            '               <option value="false">False</option> ' +
            '           </select>' +
            '   </td>' +
            '   <td width="15%" class="align-middle">' +
            '       <input type="checkbox" name="{{attributeName}}-null" data-attribute-name="{{attributeName}}-attr"' +
            '       data-input="null">' +
            '   </td>' +
            '</tr>';

        var textInput =
            '<tr>' +
            '   <td width="85%">' +
            '       <label>' +
            '           {{attributeName}} ({{attributeType}})' +
            '       </label>' +
            '           <input type="text" class="form-control" data-element-type="attribute"' +
            '           name="{{attributeName}}-attr" data-type ="{{attributeType}}">' +
            '   </td>' +
            '   <td width="15%" class="align-middle">' +
            '       <input align="center" type="checkbox" name="{{attributeName}}-null"' +
            '       data-attribute-name="{{attributeName}}-attr" data-input="null">' +
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
        var $attributes = $('form[data-form-type="single"][data-uuid="' + uuid + '"] [data-element-type="attribute"]');
        $attributes.each(
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
        var $attributes = $('form[data-form-type="single"][data-uuid="' + uuid + '"] [data-element-type="attribute"]');
        $attributes.each(
            function () {
                self.removeRuleOfAttribute(this);
            }
        );
    };

// remove validation rule of an attribute
    self.removeRuleOfAttribute = function (ctx) {
        $(ctx).rules('remove');
    };

    // start inactive siddhi apps in run or debug mode
    self.startInactiveSiddhiApp = function ($form) {
        var uuid = $form.data('uuid');
        var siddhiAppName = $form.find('select[name="single-event-siddhi-app-name"]').val();
        var mode = $form.find('input[name="run-debug"]:checked').val();
        var tabController = self.app.tabController;
        var activeTab = tabController.getTabFromTitle(siddhiAppName);
        if (!activeTab) {
            OpenSiddhiApps.openFile(siddhiAppName);
            activeTab = tabController.getTabFromTitle(siddhiAppName);
        }
        tabController.setActiveTab(activeTab);
        var tab = self.app.tabController.getTabFromTitle(siddhiAppName);
        var launcher = tab.getSiddhiFileEditor().getLauncher();
        if(tab !== undefined){
            if (mode === 'run') {
                launcher.runApplication(self.workspace, false);
                self.siddhiAppDetailsMap[siddhiAppName] = self.RUN;
            } else if (mode === 'debug') {
                launcher.debugApplication(self.workspace, false);
                self.siddhiAppDetailsMap[siddhiAppName] = self.DEBUG;
            }
        }
    };

    self.changeSiddhiAppStatusInSingleSimulation = function (siddhiAppName, status) {
        var $singleEventConfigList = $("#single-event-configs").find("div[id^='event-content-parent-']");
        $singleEventConfigList.each(function () {
            var $singleEventConfig = $(this);
            var currentSiddhiAppName = $singleEventConfig.find("select[name='single-event-siddhi-app-name']").val();
            if (siddhiAppName == currentSiddhiAppName) {
                var $notificationBox = $singleEventConfig.find(".alert");
                self.siddhiAppDetailsMap[siddhiAppName] = status;
                $singleEventConfig.find('div[data-name="siddhi-app-name-mode"]').html(self.SiddhiAppStatus + status);
                if (status == "RUN" || status == "DEBUG") {
                    $notificationBox.addClass("alert-success");
                    $notificationBox.removeClass("alert-warning");
                    $notificationBox.removeClass("alert-danger");
                    $singleEventConfig.find('div[data-name="run-debug-buttons"]').empty();
                    $singleEventConfig.find('button[type="submit"][name="send"]').text(self.sendLabel);
                    $singleEventConfig.prop('disabled', false);
                } else if (status == "STOP") {
                    $notificationBox.removeClass("alert-success");
                    $notificationBox.removeClass("alert-danger");
                    $notificationBox.addClass("alert-warning");
                    $singleEventConfig.find('div[data-name="run-debug-buttons"]').html(self.createRunDebugButtons());
                    $singleEventConfig.find('button[type="submit"][name="send"]').text(self.startAndSendLabel);
                    $singleEventConfig.prop('disabled', false);
                } else {
                    $notificationBox.addClass("alert-danger");
                    $notificationBox.removeClass("alert-warning");
                    $notificationBox.removeClass("alert-success");
                    $singleEventConfig.find('div[data-name="run-debug-buttons"]').empty();
                    $singleEventConfig.prop('disabled', true);
                }
            }
        });
    };

        self.changeSiddiAppStreamOptionsInSingleSimulation = function (changedSiddhiAppName, noOfIterations) {
            if (0 != noOfIterations) {
                noOfIterations--;
                Simulator.retrieveSiddhiAppNames(
                    function (data) {
                        if (0 < data.length) {
                            $('#createFeedSimulationNotification').hide();
                            self.app.eventSimulator.feedSimulator.enableCreateButtons(true);
                        }
                        for (var i = 0; i < data.length; i++) {
                            if (changedSiddhiAppName.slice(0, -7) == data[i]['siddhiAppName']) {
                                var $singleEventConfigList = $("#single-event-configs")
                                    .find("div[id^='event-content-parent-']");
                                if (1 == $singleEventConfigList.length) {
                                    self.removeUnwantedSiddiStreams($singleEventConfigList, changedSiddhiAppName);
                                } else {
                                    $singleEventConfigList.each(function () {
                                        self.removeUnwantedSiddiStreams($(this), changedSiddhiAppName);
                                    });
                                }
                                if (self.FAULTY == data[i]['mode']) {
                                    setTimeout(function(){ 
                                        self.changeSiddiAppStreamOptionsInSingleSimulation(
                                            changedSiddhiAppName, noOfIterations); 
                                    }, 1000);
                                }
                            }
                        }
                    },
                    function (data) {
                        log.info(data);
                    }
                );
            }
        };

    self.removeUnwantedSiddiStreams = function ($singleEventConfig, changedSiddhiAppName) {
        var siddhiAppName = $singleEventConfig.find("select[name='single-event-siddhi-app-name']").val();
        var siddhiStreams = $singleEventConfig.find("select[name='stream-name']");
        if (changedSiddhiAppName.slice(0, -7) == siddhiAppName) {
            Simulator.retrieveStreamNames(
                siddhiAppName,
                function (data) {
                    siddhiStreams.find("option").each(function() {
                        if (-1 != $(this).val()) {
                            var valueFound = false;
                            var option = $(this);
                            for (var i = 0; i < data.length; i++) {
                                if (data[i] == option.val()) {
                                    valueFound = true;
                                }
                            }
                            if (!valueFound) {
                                if (option.is(':selected')) {
                                    option.parent().find('option[value=-1]').prop('selected', true);
                                    option.closest('.single-event-form').find('div[data-name="attributes"]').html("");
                                    option.remove();
                                } else {
                                    option.remove();
                                }
                            }
                        }
                    });
                    for (var i = 0; i < data.length; i++) {
                        var valueFound = false;
                        siddhiStreams.find("option").each(function() {
                            if (data[i] == $(this).val()) {
                                valueFound = true;
                            }
                        });
                        if (!valueFound) {
                            siddhiStreams.append($("<option></option>").attr("value", data[i]).text(data[i]));
                        }
                    }
                },
                function (data) {
                    log.info(data);
                }
            );
        }
    };

    return self;
});
