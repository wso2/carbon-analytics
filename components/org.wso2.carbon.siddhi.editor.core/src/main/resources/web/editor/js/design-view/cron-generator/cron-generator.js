/**
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['require', 'lodash', 'jquery', 'log', 'constants', 'cronstrue', 'jquery_timepicker'],
    function (require, _ , $, log, Constants, Cronstrue, Timepicker) {

        var CronGenerator = function(){
            var self = this;
            this._cronGenerator = $('#cronGenerator');
        };
        var errorMsg;
        var constants = {
            EVERY_MINUTE: '0 * * * * ?',
            EVERY_HOUR: '0 0 * * * ?',
            EVERYDAY_AT_NOON: '0 0 12 * * ?',
            EVERYWEEK_ON_SUNDAY_AT_NOON: '0 0 12 ? * SUN',
            ON_THE_FIRST_OF_EVERYMONTH_AT_NOON: '0 0 12 1 * ?',
            ON_THE_FIRST_OF_JANUARY_OF_EVERYYEAR_AT_NOON: '0 0 12 1 1 ?',
            MAX_MINUTE_AND_SECOND_VALUE: 59,
            MAX_HOUR_VALUE: 23,
            REGEX_FOR_TIME: /[^\d-,*\/]/ ,
            REGEX_FOR_DAYOFMONTH: /[^\w-,?*\/]/ ,
            REGEX_FOR_MONTH: /[^\w-,*\/]/,
            REGEX_FOR_DAYOFWEEK: /[^\w-,*?\/#]/,
            REGEX_FOR_SPACE: /\s/g,
            REGEX_FOR_ALPHA_NUMERIC: /[^\da-zA-Z]/
        };

        var validateTimeValue = function(time, maxValue) {
            if (time.search(constants.REGEX_FOR_TIME) !== -1) {
                errorMsg = " contains the unallowed characters. "
                return false;
            }
            var list = time.split(",");
            return list.every(function (time){
                if(time.includes('/')) {
                    var startingTimeOptionArr = time.split('/');
                    if(time.endsWith('/')){
                        errorMsg = " should not end with / "
                    } else {
                        errorMsg = " values must lies between 0 and " + maxValue;
                    }
                    return validateTimeRange(startingTimeOptionArr, maxValue) ||
                            (startingTimeOptionArr[0] === '*' && validateTimeRange([startingTimeOptionArr[1]], maxValue));
                } else if(time.includes('-')) {
                    var timeRangeArr = time.split('-');
                    if(time.endsWith('-')){
                        errorMsg = " should not end with - "
                    } else {
                        errorMsg = " values must lies between 0 and " + maxValue;
                    }
                    return validateTimeRange(timeRangeArr, maxValue) &&
                                        (parseInt(timeRangeArr[0])<parseInt(timeRangeArr[1]));
                } else {
                    if(time === ""){
                        errorMsg = " should not end with , "
                    } else {
                        errorMsg = " values must lies between 0 and " + maxValue ;
                    }
                    return validateTimeRange([time],maxValue) || time === '*';
                }
            });
        };

        var validateTimeRange = function(dataArray, value) {
            return dataArray.every(element => {
                return parseInt(element) >= 0  && parseInt(element) <= value;
            });
        };

        //this method is used to check the range of dayOfMonth(1-31) ,month(1-12) ,dayOfWeek(1-7)
        var validateRangeOfDay = function(dataArray, val, endVal) {
            return dataArray.every(element => {
                return parseInt(element) >= val && parseInt(element) <= endVal;
            });
        };

        var validateDayOfMonthValue = function(dayOfMonth, dayOfWeek) {
            if(dayOfWeek === '?' && dayOfMonth.includes('L')) {
                if(dayOfMonth.includes(',') && dayOfWeek === '?'){
                    return false;
                } else if(dayOfMonth.includes('-') && dayOfWeek === '?') {
                    var dayOfMonthRangeArr = dayOfMonth.split('-');

                    return dayOfMonthRangeArr[0] === 'L' && validateRangeOfDay([dayOfMonthRangeArr[1]], 1, 30);
                }
            } else if(dayOfMonth.includes('W') && dayOfWeek === '?'){
                if(dayOfMonth.search(constants.REGEX_FOR_ALPHA_NUMERIC) !== -1 ){
                    errorMsg = "Invalid Cron Expression";
                    return false;
                }
                if(dayOfMonth.length === 1){
                    errorMsg = "Invalid Cron Expression";
                    return false;
                } else {
                    errorMsg = "Day of month values must be between 1 and 31";
                    return validateRangeOfDay([dayOfMonth], 1, 31);
                }
            }
            if (dayOfMonth.search(constants.REGEX_FOR_DAYOFMONTH) !== -1) {
                errorMsg = "Invalid Cron Expression";
                return false;
            }
            var list = dayOfMonth.split(",");
            return list.every(function (dayOfMonth){
                if(dayOfMonth.includes('/') && dayOfWeek === '?') {
                    if(dayOfMonth.endsWith('/')){
                        errorMsg = "Day of month values should not end with /";
                    } else {
                        errorMsg = "Day of month values must be between 1 and 31";
                    }
                    var startingDayOfMonthOptionArr = dayOfMonth.split('/');
                    var isValidElements = (validateRangeOfDay([startingDayOfMonthOptionArr[0]], 1, 31) &&
                                                        validateRangeOfDay([startingDayOfMonthOptionArr[1]], 1, 31));
                    var isValidFirstElem = (startingDayOfMonthOptionArr[0] === '*' &&
                                                        validateRangeOfDay([startingDayOfMonthOptionArr[1]], 1, 31));
                    return isValidElements || isValidFirstElem;
                } else if(dayOfMonth.includes('-') && dayOfWeek === '?') {
                    var dayOfMonthRangeArr = dayOfMonth.split('-');
                    if(dayOfMonth.endsWith('-')){
                        errorMsg = "Day of month values should not end with - ";
                    } else {
                        errorMsg = "Day of month values must be between 1 and 31";
                    }
                    return validateRangeOfDay(dayOfMonthRangeArr, 1, 31) &&
                                    (parseInt(dayOfMonthRangeArr[0]) < parseInt(dayOfMonthRangeArr[1]));
                } else {
                    if(dayOfMonth === ""){
                        errorMsg = "Day of month values should not end with , ";
                    } else {
                        errorMsg = "Day of month values must be between 1 and 31";
                    }
                    return validateRangeOfDay([dayOfMonth], 1, 31) ||
                        (dayOfMonth === '*' && dayOfWeek !== '*') || (dayOfMonth === '?' && dayOfWeek !== '?') ||
                        (dayOfWeek === '?' && (dayOfMonth === 'LW' || dayOfMonth === 'L'));
                }
            });
        };

        var monthArray = ['jan', 'feb', 'mar', 'apr', 'may', 'jun', 'jul', 'aug', 'sep', 'oct', 'nov', 'dec'];

        var validateStringValue = function(monthArr, dataArr) {
            return monthArr.every(month => {
                return dataArr.includes(month.toLowerCase());
            })
        };

        var validateMonthValue = function(month) {
            if(month.includes('_')){
                errorMsg = "Invalid cron expression";
                return false;
            }
            if (month.search(constants.REGEX_FOR_MONTH) !== -1) {
                errorMsg = "Invalid cron expression";
                return false;
            }
            var list = month.split(",");
            return list.every(function (month){
                if(month.includes('/')) {
                    if(month.endsWith('/')){
                        errorMsg = "Month values should not end with / ";
                    } else {
                        errorMsg = "Month values must be between 1 and 12";
                    }
                    var startingDayOfMonthOptionArr = month.split('/');
                    var isValidElements = (validateRangeOfDay([startingDayOfMonthOptionArr[0]], 1, 12) &&
                                                        validateRangeOfDay([startingDayOfMonthOptionArr[1]], 1, 12));
                    var isValidFirstElem = (startingDayOfMonthOptionArr[0] === '*' &&
                                                        validateRangeOfDay([startingDayOfMonthOptionArr[1]], 1, 12));
                    return isValidElements || isValidFirstElem;
                } else if(month.includes('-')) {
                    if(month.endsWith('-')){
                        errorMsg = "Month values should not end with - ";
                    } else {
                        errorMsg = "Month values must be between 1 and 12 or Jan - Dec";
                    }
                    var monthRangeArr = month.split('-');
                    var validMonthRange = parseInt(monthRangeArr[0]) < parseInt(monthRangeArr[1]);
                    var validMonthStrRange = monthArray.indexOf(monthRangeArr[0]) < monthArray.indexOf(monthRangeArr[1]);

                    return !isNaN(parseInt(monthRangeArr[0])) && !isNaN(parseInt(monthRangeArr[1])) ?
                        validateRangeOfDay(monthRangeArr, 1, 12) && validMonthRange :
                        validateStringValue(monthRangeArr, monthArray) && validMonthStrRange;
                } else {
                    if(month === ""){
                        errorMsg = "Month values should not end with , ";
                    } else {
                        errorMsg = "Month values must be between 1 and 12 or Jan - Dec";
                    }
                    var firstIndexValue = month.charAt(0);
                    var secondIndexValue = month.charAt(1);
                    if(month.length === 1){
                        return validateRangeOfDay([month], 1, 12) || month === '*';
                    } else {
                        return (!isNaN(parseInt(firstIndexValue)) && !isNaN(parseInt(secondIndexValue))) ?
                                validateRangeOfDay([month], 1, 12) : validateStringValue([month], monthArray);
                    }
                }
            });
        };

        var weekArray = ['mon', 'tue', 'wed', 'thu', 'fri', 'sat','sun'];

        var validateDayOfWeekValue = function(dayOfWeek, dayOfMonth) {
            if(dayOfMonth === '?' && dayOfWeek.includes('L')) {
                if(dayOfWeek.includes(',') && dayOfMonth === '?'){
                    errorMsg = "Invalid cron expression";
                    return false;
                } else if(dayOfWeek.includes('-') && dayOfMonth === '?'){
                    errorMsg = "Invalid cron expression";
                    return false;
                } else if (!isNaN(parseInt(dayOfWeek))){
                    errorMsg = "Day-of-Week values must be between 1 and 7";
                    return validateRangeOfDay([dayOfWeek], 1, 7);
                }
            }
            if(dayOfWeek.includes('_')){
                errorMsg = "Invalid cron expression";
                return false;
            }
            if (dayOfWeek.search(constants.REGEX_FOR_DAYOFWEEK) !== -1) {
                errorMsg = "Invalid cron expression";
                return false;
            }
            var list = dayOfWeek.split(",");
            return list.every(function (dayOfWeek){
                if(dayOfWeek.includes('/') && dayOfMonth === '?') {
                    var startingDayOfWeekOptionArr = dayOfWeek.split('/');
                    if(dayOfWeek.endsWith('/')){
                        errorMsg = "Day-of-week values should not end with / ";
                    } else {
                        errorMsg = "Day-of-Week values must be between 1 and 7";
                    }
                    var isValidElements = (validateRangeOfDay([startingDayOfWeekOptionArr[0]], 1, 7) &&
                                                        validateRangeOfDay([startingDayOfWeekOptionArr[1]], 1, 7));
                    var isValidFirstElem = (startingDayOfWeekOptionArr[0] === '*' &&
                                                        validateRangeOfDay([startingDayOfWeekOptionArr[1]], 1, 7));
                    return isValidElements || isValidFirstElem;
                } else if(dayOfWeek.includes('-') && dayOfMonth === '?') {
                    var dayOfWeekRangeArr = dayOfWeek.split('-');
                    if(dayOfWeek.endsWith('-')){
                        errorMsg = "Day-of-week values should not end with - ";
                    } else {
                        errorMsg = "Day-of-Week values must be between 1 and 7";
                    }
                    var validWeekRange = parseInt(dayOfWeekRangeArr[0]) < parseInt(dayOfWeekRangeArr[1]);
                    var validWeekStrRange = weekArray.indexOf(dayOfWeekRangeArr[0]) < weekArray.indexOf(dayOfWeekRangeArr[1]);
                    return !isNaN(parseInt(dayOfWeekRangeArr[0])) && !isNaN(parseInt(dayOfWeekRangeArr[1])) ?
                                        validateRangeOfDay(dayOfWeekRangeArr, 1, 7) && validWeekRange :
                                        validateStringValue(dayOfWeekRangeArr, weekArray) && validWeekStrRange;
                } else if(dayOfWeek.includes('#') && dayOfMonth === '?') {
                    var weekdayOfMonthArr = dayOfWeek.split('#');
                    if(dayOfWeek.endsWith('#')){
                        errorMsg = "Day-of-week values should not end with # "
                    } else {
                        errorMsg = "Day-of-Week values must be between 1 and 7";
                    }
                    return (validateStringValue([weekdayOfMonthArr[0]], weekArray) &&
                                validateRangeOfDay([weekdayOfMonthArr[1]], 1, 5)) ||
                                 (validateRangeOfDay([weekdayOfMonthArr[0]], 1, 7) &&
                                 validateRangeOfDay([weekdayOfMonthArr[1]], 1, 5));
                } else {
                    if(dayOfWeek === ""){
                        errorMsg = "Day-of-week values should not end with , "
                    } else {
                        errorMsg = "Day-of-Week values must be between 1 and 7";
                    }
                    var firstIndexValue = dayOfWeek.charAt(0);
                    var secondIndexValue = dayOfWeek.charAt(1);
                    if(dayOfWeek.length === 1){
                        return validateRangeOfDay([dayOfWeek], 1, 7) || dayOfWeek === 'L' ||
                            (dayOfWeek === '*' && dayOfMonth !== '*') || (dayOfWeek === '?' && dayOfMonth !== '?');
                    } else {
                        return (!isNaN(parseInt(firstIndexValue)) && !isNaN(parseInt(secondIndexValue))) ?
                                validateRangeOfDay([dayOfWeek], 1, 7) : validateStringValue([dayOfWeek], weekArray);
                    }
                }
            });
        };
        var validateCronExpression = function(cronExpression) {
            if(!constants.REGEX_FOR_SPACE.test(cronExpression)) {
                return false;
            }
            var cronArray = cronExpression.split(" ");
            if(parseInt(cronArray.length) !== 6) {
                return false;
            }
            var seconds = cronArray[0];
            var minutes = cronArray[1];
            var hours = cronArray[2];
            var dayOfMonth = cronArray[3];
            var month = cronArray[4];
            var dayOfWeek = cronArray[5];

            var isValidSeconds = validateTimeValue(seconds, constants.MAX_MINUTE_AND_SECOND_VALUE);
            var isValidMinutes = validateTimeValue(minutes, constants.MAX_MINUTE_AND_SECOND_VALUE);
            var isValidHour = validateTimeValue(hours, constants.MAX_HOUR_VALUE);
            var isValidDayOfMonth = validateDayOfMonthValue(dayOfMonth, dayOfWeek);
            var isValidMonth = validateMonthValue(month);
            var isValidDayOfWeek = validateDayOfWeekValue(dayOfWeek, dayOfMonth);

            var isValidCron = isValidSeconds && isValidMinutes && isValidHour && isValidDayOfMonth &&
                                                                    isValidMonth && isValidDayOfWeek;

            return isValidCron;
        };

        CronGenerator.prototype.init = function(optionParent){
            var self = this;
            var generate = $('<div id = "generate" class = "col-xs-2" style="margin:10px 5px;">'+
                    '<button type = "button" class = "btn btn-primary" '+
                    'id = "generate-btn"> Schedule </button></div>');
            $(optionParent).find('.option-content').append(generate);
            $(optionParent).find('.option-value').attr('readonly', true);
            $(optionParent).find('.option-value').after(' <i'+
                ' class="fw fw-info"><span id="crondescription" style = "display:none"></span></i>');
            var expression = $(optionParent).find('.option-value').val();
            var description = Cronstrue.toString(expression, { throwExceptionOnParseError: false });
            $('#source-options #crondescription').text(description);
            $('#source-options #generate-btn').on('click',function(){
                 self.render(optionParent);
            });
        };

        CronGenerator.prototype.render = function(optionParent){
            var self = this;
            this._cronGenerator = $('#cronGenerator').clone();
            self._cronGenerator.modal('show');
            self.renderBasicCronExpression();
            self.renderRegularCronExpression();
            self.renderPredefinedCronExpression();
            self.renderAdvancedCronExpression();
            self._cronGenerator.find("#saveButton").on('click', function(){
                var inputValue;
                var selectedValue = self._cronGenerator.find('#basic-cron-list :selected').val();
               if(self._cronGenerator.find('#basicTab').hasClass('active')){
                   $(optionParent).find('.option-value').val(selectedValue);
               } else if(self._cronGenerator.find('#regularTab').hasClass('active')){
                    selectedValue = self._cronGenerator.find('#selectListValues :selected').val();
                    if(self._cronGenerator.find('#everySecond').is(':checked')){
                        inputValue = "*/" + selectedValue + " * * * * ?";
                    } else if(self._cronGenerator.find('#everyMinute').is(':checked')){
                        inputValue = "0 */" + selectedValue + " * * * ?";
                    } else if(self._cronGenerator.find('#everyHour').is(':checked')){
                        inputValue = "0 0 */" + selectedValue + " * * ?";
                    } else if(self._cronGenerator.find('#everyDay').is(':checked')){
                        var timeValue = self._cronGenerator.find("#timeForEveryDay").val();
                        var time = timeValue.split(":");
                        var hour = time[0];
                        var minute = time[1];
                        inputValue = "0 " + minute + " " + hour + " */" + selectedValue + " * ?";
                    }
                    $(optionParent).find('.option-value').val(inputValue);
               } else if(self._cronGenerator.find('#predefineTab').hasClass('active')){
                    var selectedDays = [];
                    selectedValue = self._cronGenerator.find("#predefinedtime").val();
                    var time = selectedValue.split(":");
                    var hour = time[0];
                    var minute = time[1];
                    if(self._cronGenerator.find("input[name='weekDays']").is(':checked')){
                         self._cronGenerator.find("input[name='weekDays']:checked").each(function() {
                             selectedDays.push($(this).val());
                         });
                         inputValue = "0 " + minute + " " + hour + " ? * " + selectedDays.join(",");
                    }
                    else {
                         inputValue = "0 " + minute + " " + hour + " * * ?";
                    }
                    $(optionParent).find('.option-value').val(inputValue);
               } else if(self._cronGenerator.find('#advancedTab').hasClass('active')){
                    selectedValue = self._cronGenerator.find('#expression').val();
                    $(optionParent).find('.option-value').val(selectedValue);
               }
               var expression = $(optionParent).find('.option-value').val();
               var description = Cronstrue.toString(expression, { throwExceptionOnParseError: false });
               $('#source-options #crondescription').text(description);
               self._cronGenerator.modal('hide');
            });
        };

        CronGenerator.prototype.renderBasicCronExpression = function(){
            var self = this;
            var cronBasic = $('<select id="basic-cron-list" class="form-control" style="width:50%;">'+
                     '<option value="'+constants.EVERY_MINUTE+'">Every minute</option>'+
                     '<option value="'+constants.EVERY_HOUR+'">Every hour</option>'+
                     '<option value="'+constants.EVERYDAY_AT_NOON+'">Every day at noon</option>'+
                     '<option value="'+constants.EVERYWEEK_ON_SUNDAY_AT_NOON+'">Every week on Sunday at noon</option>'+
                     '<option value="'+constants.ON_THE_FIRST_OF_EVERYMONTH_AT_NOON+'">On the First of every month'+
                     ' at noon</option><option value="'+constants.ON_THE_FIRST_OF_JANUARY_OF_EVERYYEAR_AT_NOON+'">'+
                     'On the First of January of every year at noon</option></select>');
            self._cronGenerator.find("#basic-content").html(cronBasic);
            self._cronGenerator.find("#cron-basic").addClass('active');

            self._cronGenerator.find('a[href="#cron-basic"]').click(function () {
                  self._cronGenerator.find("#cron-basic").addClass('active');
                  self._cronGenerator.find("#cron-regular").removeClass('active');
                  self._cronGenerator.find("#cron-predefined").removeClass('active');
                  self._cronGenerator.find("#cron-advanced").removeClass('active');
                  self._cronGenerator.find("#basic-content option[value='"+constants.EVERY_MINUTE+"']").attr('selected',true);
                  self._cronGenerator.find('#saveButton').removeAttr("disabled");
            });
        };

        CronGenerator.prototype.renderRegularCronExpression = function(){
            var self = this;
            var cronRegular = $('<div class = "col-xs-12"><div id = "cronEverySecond" class="col-xs-2">'+
                    '<input id = "everySecond" type = "radio" name = "cronRegularButton">&nbsp;&nbsp;'+
                    '<label for = "everySecond"> Second </label></div><div id = "cronEveryMinute" class="col-xs-2">'+
                    '<input id = "everyMinute" type = "radio" name = "cronRegularButton">&nbsp;&nbsp;'+
                    '<label for = "everyMinute"> Minute </label></div><div id = "cronEveryHour" class="col-xs-2">'+
                    '<input id = "everyHour" type = "radio" name = "cronRegularButton">&nbsp;&nbsp;'+
                    '<label for = "everyHour"> Hour </label></div><div id = "cronEveryDay" class="col-xs-2">'+
                    '<input id = "everyDay" type = "radio" name = "cronRegularButton">&nbsp;&nbsp;'+
                    '<label for = "everyDay"> Day </label></div></div>'+
                    '<div class = "col-xs-12"><div id="everySelectValues" class="col-xs-2"></div>'+
                    '<div id="TimeForRegularDay" class="col-xs-4">'+
                    '</div></div>');
            var selectListValues = $('<select id="selectListValues" class="form-control" onfocus="this.size=4;" '+
                    'onblur="this.size=1;" onchange="this.size=1; this.blur();" style="margin: 2px 0px;"></select>');
            self._cronGenerator.find('a[href="#cron-regular"]').click(function () {
                   self._cronGenerator.find("#cronRegular").html(cronRegular);
                   self._cronGenerator.find("#cron-regular").addClass('active');
                   self._cronGenerator.find("#cron-basic").removeClass('active');
                   self._cronGenerator.find("#cron-predefined").removeClass('active');
                   self._cronGenerator.find("#cron-advanced").removeClass('active');
                   self._cronGenerator.find("#everySecond").prop('checked', true);
                   self._cronGenerator.find("#everySelectValues").html(selectListValues);
                   self.selectListOfValues();
                   self._cronGenerator.find("#TimeForRegularDay").empty();
                   self._cronGenerator.find("#dayTime").remove();
                   self._cronGenerator.find('#saveButton').removeAttr("disabled");
            });
            self._cronGenerator.find('#cron-regular').on('change', '#everySecond', function () {
               if ($(this).is(':checked')) {
                   self._cronGenerator.find("#everySelectValues").html(selectListValues);
                   self.selectListOfValues();
                   self._cronGenerator.find("#TimeForRegularDay").empty();
                   self._cronGenerator.find("#timeForDayOption").remove();
               }
            });

            self._cronGenerator.find('#cron-regular').on('change', '#everyMinute', function () {
               if ($(this).is(':checked')) {
                  self._cronGenerator.find("#everySelectValues").html(selectListValues);
                  self.selectListOfValues();
                  self._cronGenerator.find("#TimeForRegularDay").empty();
                  self._cronGenerator.find("#timeForDayOption").remove();
               }
            });
            self._cronGenerator.find('#cron-regular').on('change', '#everyHour', function () {
               if ($(this).is(':checked')) {
                   self._cronGenerator.find("#everySelectValues").html(selectListValues);
                   self.selectListOfValues();
                   self._cronGenerator.find("#timeForDayOption").remove();
               }
            });
            self._cronGenerator.find('#cron-regular').on('change', '#everyDay', function () {
                var dayTime = $('<div id="timeForDayOption"><label>Time </label><i class="fw fw-info" '+
                                'title=" Time in 24hour format "></i><input type="text" id="timeForEveryDay" value="12:00"'+
                                ' class="form-control" style="margin:0px 15px;width:40%;" readonly/></div>');
               if ($(this).is(':checked')) {
                   self._cronGenerator.find("#everySelectValues").html(selectListValues);
                   self._cronGenerator.find("#timeForDayOption").remove();
                   self._cronGenerator.find("#TimeForRegularDay").html(dayTime);
                   self._cronGenerator.find("#timeForEveryDay").timepicker();
                   self.selectListOfValues();
               }
            });
        };

        CronGenerator.prototype.renderPredefinedCronExpression = function(){
            var self = this;
            var predefinedCron = $('<div id="timeForPredefineTab" class="col-xs-10"><div id="defineTime">'+
                   '<label style="margin:15px 5px 15px 0px;">Time</label><i class="fw fw-info" title=" Time in 24hour format ">'+
                   '</i><input type="text" id="predefinedtime" value="12:30" '+
                   'class="form-control" style="margin:15px 15px;width:15%" readonly/>'+
                   '<input id="predefineEveryDay" type="checkbox" name="predefineEveryDay" style="margin:15px 5px;">'+
                   '&nbsp;&nbsp;<label for="predefineEveryDay" style="margin:15px 15px;"> Everyday </label></div></div>'+
                   '<div id="valuesOfDays" class="col-xs-12"></div>');
            self._cronGenerator.find('a[href="#cron-predefined"]').click(function () {
                   self._cronGenerator.find("#cronPredefined").html(predefinedCron);
                   self._cronGenerator.find("#cron-predefined").addClass('active');
                   self._cronGenerator.find("#cron-regular").removeClass('active');
                   self._cronGenerator.find("#cron-basic").removeClass('active');
                   self._cronGenerator.find("#cron-advanced").removeClass('active');
                   self._cronGenerator.find('#saveButton').removeAttr("disabled");
                   self.onChangePredefinedTime();
            });
        };

        CronGenerator.prototype.onChangePredefinedTime = function(){
            var self = this;
            var defineTime = $('<div id="defineTime">'+
                  '<label style="margin:15px 5px 15px 0px;"> Time </label>'+
                  '<i class="fw fw-info" title = " Time in 24hour format "></i>'+
                  '<input type="text" id="predefinedtime" value="12:30" class="form-control" style="margin:15px 15px;'+
                  'width:15%;" readonly/>'+
                  '<input id="predefineEveryDay" type="checkbox" name="predefineEveryDay" value="">&nbsp;&nbsp;'+
                  '<label for="predefineEveryDay"> Everyday </label>'+
                  '</div>');
            var Days = $('<div class="col-xs-3" style="width:13%;">'+
                 '<input id = "sunday" type="checkbox" name="weekDays" value = "SUN">'+
                 '<label for="sunday"> Sunday </label></div><div class="col-xs-3" style="width:14%;">'+
                 '<input id = "monday" type="checkbox" name="weekDays" value = "MON">'+
                 '<label for="monday"> Monday </label></div><div class="col-xs-3" style="width:14%;">'+
                 '<input id = "tuesday" type="checkbox" name="weekDays" value = "TUE">'+
                 '<label for="tuesday"> Tuesday </label></div><div class="col-xs-3" style="width:15.9%;">'+
                 '<input id = "wednesday" type="checkbox" name="weekDays" value = "WED">'+
                 '<label for="wednesday"> Wednesday </label></div><div class="col-xs-3" style="width:14.5%;">'+
                 '<input id = "thursday" type="checkbox" name="weekDays" value = "THU">'+
                 '<label for="thursday"> Thursday </label></div><div class="col-xs-3" style="width:13%;">'+
                 '<input id = "friday" type="checkbox" name="weekDays" value = "FRI">'+
                 '<label for="friday"> Friday </label></div><div class="col-xs-3" style="width:14%;">'+
                 '<input id = "saturday" type="checkbox" name="weekDays" value = "SAT">'+
                 '<label for="saturday"> Saturday </label></div>');
            self._cronGenerator.find("#defineTime").remove();
            self._cronGenerator.find("#timeForPredefineTab").html(defineTime);
            self._cronGenerator.find("#valuesOfDays").html(Days);
            self._cronGenerator.find('input[name="predefineEveryDay"]').prop('checked',true);
            self._cronGenerator.find("#predefinedtime").timepicker();

            self._cronGenerator.find('input[name="weekDays"]').click(function () {
                if ($(this).is(':checked')) {
                   self._cronGenerator.find('input[name="predefineEveryDay"]').prop('checked',false);
                   if(self._cronGenerator.find('input[name="weekDays"]:checked').length === 7) {
                       self._cronGenerator.find('input[name="predefineEveryDay"]').prop('checked',true);
                   }
                } else {
                    if(self._cronGenerator.find('input[name="weekDays"]:checked').length === 0) {
                        self._cronGenerator.find('input[name="predefineEveryDay"]').prop('checked',true);
                    } else if(self._cronGenerator.find('input[name="weekDays"]:checked').length > 0 &&
                                    self._cronGenerator.find('input[name="weekDays"]:checked').length < 7) {
                        self._cronGenerator.find('input[name="predefineEveryDay"]').prop('checked',false);
                    }
                }
            });
            self._cronGenerator.find('input[name="predefineEveryDay"]').click(function () {
                if ($(this).is(':checked')) {
                   self._cronGenerator.find('input[name="weekDays"]').prop('checked',false);
                } else {
                    self._cronGenerator.find('input[name="weekDays"]').prop('checked',false);
                    self._cronGenerator.find('#sunday').prop('checked',true);
                }
            });
        };

        CronGenerator.prototype.renderAdvancedCronExpression = function(){
            var self = this;
            var cronAdvanced = $('<div class="col-xs-12" style = "margin:5px"><div class="col-xs-2">'+
                '<input id="second" class="form-control" type="text" value = "*">'+
                '<label for="second"> Second </label><i class="fw fw-info" title = "Allowed characters : 0-59,'+
                ' special characters : ,/*- "></i></div><div class="col-xs-2">'+
                '<input  id="minute" class="form-control" type="text" value = "*" >'+
                '<label for="minute"> Minute </label><i class="fw fw-info" title = "Allowed characters : 0-59,'+
                ' special characters : ,/*- "></i></div><div class="col-xs-2">'+
                '<input id="hour" class="form-control" type="text" value = "*">'+
                '<label for="hour"> Hour </label><i class="fw fw-info" title = "Allowed characters : 0-23,'+
                ' special characters : ,/*- "></i></div><div class="col-xs-2">'+
                '<input id="dayMonth" class="form-control" type="text" value = "*">'+
                '<label for="dayMonth">Day(month)</label><i class="fw fw-info" title="Allowed characters : 1-31,L,LW,'+
                ' special characters : ,/*?- "></i></div><div class="col-xs-2">'+
                '<input id="month" class="form-control" type="text" value = "*">'+
                '<label for="month"> Month </label><i class="fw fw-info" title=" Allowed characters : 1-12'+
                ' or JAN-DEC, special characters : ,/*- "></i></div><div class="col-xs-2">'+
                '<input id="dayWeek" class="form-control" type="text" value = "?">'+
                '<label for="dayWeek">Day(week)</label><i class="fw fw-info" title = " Allowed characters'+
                ' : 1-7 or SUN-SAT, special characters : ,?/*- "></i></div></div>'+
                '<div class = "col-xs-10"><label class="error-message" id="errorMsg" style="margin:2px 0px;"></label>'+
                '</div><div class = col-xs-12"><div class="col-xs-4">'+
                '<label for="expression"> Expression in Quartz </label></div><div class="col-xs-6">'+
                '<input id="expression" value="* * * * * ?" class="form-control" style = "margin:10px 0px;" />'+
                '</div><div class="col-xs-4"><label for="output"> Expression in English </label></div>'+
                '<div class="col-xs-6"><label id="output"> Every second </label></div></div>');
            self._cronGenerator.find('a[href="#cron-advanced"]').click(function () {
                  self._cronGenerator.find('#advancedCron').html(cronAdvanced);
                  self._cronGenerator.find("#cron-advanced").addClass('active');
                  self._cronGenerator.find("#cron-regular").removeClass('active');
                  self._cronGenerator.find("#cron-basic").removeClass('active');
                  self._cronGenerator.find("#cron-predefined").removeClass('active');
                  self.onChangeInput();
                  if(self._cronGenerator.find("#expression").hasClass('error-field')){
                       self._cronGenerator.find('#saveButton').attr("disabled","disabled");
                  }
            });
        };

        CronGenerator.prototype.onChangeInput = function(){
            var self = this;
            self._cronGenerator.find("#second").on("input", function(){
                var secValue = self._cronGenerator.find('#second').val();
                self._cronGenerator.find("#expression").val(self._cronGenerator.find("#second").val() + " " +
                        self._cronGenerator.find("#minute").val()+ " " +self._cronGenerator.find("#hour").val() + " " +
                        self._cronGenerator.find("#dayMonth").val()+ " " +self._cronGenerator.find("#month").val() + " "
                        + self._cronGenerator.find("#dayWeek").val());
                var expression = self._cronGenerator.find("#expression").val();
                var description = Cronstrue.toString(expression, {throwExceptionOnParseError:false});
                if(validateTimeValue(secValue, constants.MAX_MINUTE_AND_SECOND_VALUE)){
                    self._cronGenerator.find('#second').removeClass("error-field");
                    self._cronGenerator.find("#output").text(description);
                    self._cronGenerator.find('#errorMsg').hide();
                } else {
                    self._cronGenerator.find('#second').addClass("error-field");
                    self._cronGenerator.find('#errorMsg').text("Second" + errorMsg);
                    self._cronGenerator.find('#errorMsg').show();
                }
                self.isErrorOccured();
            });
            self._cronGenerator.find("#minute").on("input", function(){
                var minValue = self._cronGenerator.find('#minute').val();
                self._cronGenerator.find("#expression").val(self._cronGenerator.find("#second").val() + " " +
                    self._cronGenerator.find("#minute").val()+ " " + self._cronGenerator.find("#hour").val() + " " +
                    self._cronGenerator.find("#dayMonth").val() + " " + self._cronGenerator.find("#month").val() + " " +
                    self._cronGenerator.find("#dayWeek").val());
                var expression = self._cronGenerator.find("#expression").val();
                var description = Cronstrue.toString(expression, {throwExceptionOnParseError:false});
                if(validateTimeValue(minValue, constants.MAX_MINUTE_AND_SECOND_VALUE)){
                   self._cronGenerator.find('#minute').removeClass("error-field");
                   self._cronGenerator.find("#output").text(description);
                   self._cronGenerator.find('#errorMsg').hide();
                } else {
                   self._cronGenerator.find('#minute').addClass("error-field");
                   self._cronGenerator.find('#errorMsg').text("Minute" + errorMsg);
                   self._cronGenerator.find('#errorMsg').show();
                }
                self.isErrorOccured();
            });
            self._cronGenerator.find("#hour").on("input", function(){
                self._cronGenerator.find("#expression").val(self._cronGenerator.find("#second").val() + " " +
                    self._cronGenerator.find("#minute").val()+ " " + self._cronGenerator.find("#hour").val() + " " +
                    self._cronGenerator.find("#dayMonth").val() + " " + self._cronGenerator.find("#month").val() + " " +
                    self._cronGenerator.find("#dayWeek").val());
                var expression = self._cronGenerator.find("#expression").val();
                var description = Cronstrue.toString(expression, {throwExceptionOnParseError:false});
                var hourValue = self._cronGenerator.find('#hour').val();
                if(validateTimeValue(hourValue, constants.MAX_HOUR_VALUE)){
                    self._cronGenerator.find('#hour').removeClass("error-field");
                    self._cronGenerator.find("#output").text(description);
                    self._cronGenerator.find('#errorMsg').hide();
                } else {
                    self._cronGenerator.find('#hour').addClass("error-field");
                    self._cronGenerator.find('#errorMsg').text("Hour" + errorMsg);
                    self._cronGenerator.find('#errorMsg').show();
                }
                self.isErrorOccured();
            });
            self._cronGenerator.find("#dayMonth").on("input", function(){
                self._cronGenerator.find("#expression").val(self._cronGenerator.find("#second").val() + " " +
                    self._cronGenerator.find("#minute").val()+ " " + self._cronGenerator.find("#hour").val() + " " +
                    self._cronGenerator.find("#dayMonth").val() + " " + self._cronGenerator.find("#month").val() + " " +
                    self._cronGenerator.find("#dayWeek").val());
                var expression = self._cronGenerator.find("#expression").val();
                var description = Cronstrue.toString(expression, { throwExceptionOnParseError: false });
                var dayMonValue = self._cronGenerator.find('#dayMonth').val();
                var dayWeekValue = self._cronGenerator.find('#dayWeek').val();
                if(validateDayOfMonthValue(dayMonValue, dayWeekValue)){
                     self._cronGenerator.find('#dayMonth').removeClass("error-field");
                     self._cronGenerator.find("#output").text(description);
                     self._cronGenerator.find('#errorMsg').hide();
                } else {
                     self._cronGenerator.find('#dayMonth').addClass("error-field");
                     self._cronGenerator.find('#errorMsg').text(errorMsg);
                     self._cronGenerator.find('#errorMsg').show();
                }
                self.isErrorOccured();
            });
            self._cronGenerator.find("#month").on("input", function(){
                self._cronGenerator.find("#expression").val(self._cronGenerator.find("#second").val() + " " +
                    self._cronGenerator.find("#minute").val()+ " " + self._cronGenerator.find("#hour").val() + " " +
                    self._cronGenerator.find("#dayMonth").val() + " " + self._cronGenerator.find("#month").val() + " " +
                    self._cronGenerator.find("#dayWeek").val());
                var expression = self._cronGenerator.find("#expression").val();
                var description = Cronstrue.toString(expression,{throwExceptionOnParseError:false});
                var monValue = self._cronGenerator.find('#month').val();
                if(validateMonthValue(monValue)){
                     self._cronGenerator.find('#month').removeClass("error-field");
                     self._cronGenerator.find("#output").text(description);
                     self._cronGenerator.find('#errorMsg').hide();
                } else {
                     self._cronGenerator.find('#month').addClass("error-field");
                     self._cronGenerator.find('#errorMsg').text(errorMsg);
                     self._cronGenerator.find('#errorMsg').show();
                }
                self.isErrorOccured();
            });
            self._cronGenerator.find("#dayWeek").on("input", function(){
                 self._cronGenerator.find("#expression").val(self._cronGenerator.find("#second").val() + " " +
                     self._cronGenerator.find("#minute").val()+ " " + self._cronGenerator.find("#hour").val() + " " +
                     self._cronGenerator.find("#dayMonth").val() + " " +self._cronGenerator.find("#month").val() + " " +
                     self._cronGenerator.find("#dayWeek").val());
                 var expression = self._cronGenerator.find("#expression").val();
                 var description = Cronstrue.toString(expression, { throwExceptionOnParseError:false, dayOfWeekStartIndexZero: false });

                 var weekValue = self._cronGenerator.find('#dayWeek').val();
                 var dayMonValue = self._cronGenerator.find('#dayMonth').val();
                 if(validateDayOfWeekValue(weekValue, dayMonValue)){
                      self._cronGenerator.find('#dayWeek').removeClass("error-field");
                      self._cronGenerator.find("#output").text(description);
                      self._cronGenerator.find('#errorMsg').hide();
                 } else {
                      self._cronGenerator.find('#dayWeek').addClass("error-field");
                      self._cronGenerator.find('#errorMsg').text(errorMsg);
                      self._cronGenerator.find('#errorMsg').show();
                 }
                 self.isErrorOccured();
            });
            self._cronGenerator.find("#expression").on("input",function(){
                 var expression = self._cronGenerator.find("#expression").val();
                 var description = Cronstrue.toString(expression, { throwExceptionOnParseError:false, dayOfWeekStartIndexZero: false });
                 var splitValues = expression.split(" ");
                 var second = splitValues[0];
                 var minute = splitValues[1];
                 var hour = splitValues[2];
                 var dayMonth = splitValues[3];
                 var month = splitValues[4];
                 var dayWeek = splitValues[5];
                 if(validateCronExpression(expression)){
                     self._cronGenerator.find('#expression').removeClass("error-field");
                     self._cronGenerator.find("#output").text(description);
                     self._cronGenerator.find('#output').removeClass("error-message");
                     self._cronGenerator.find('#saveButton').removeAttr("disabled");
                     self._cronGenerator.find("#second").val(splitValues[0]);
                     self._cronGenerator.find("#minute").val(splitValues[1]);
                     self._cronGenerator.find("#hour").val(splitValues[2]);
                     self._cronGenerator.find("#dayMonth").val(splitValues[3]);
                     self._cronGenerator.find("#month").val(splitValues[4]);
                     self._cronGenerator.find("#dayWeek").val(splitValues[5]);
                 } else{
                    self._cronGenerator.find('#expression').addClass("error-field");
                    self._cronGenerator.find('#output').html("Invalid Cron Expression");
                    self._cronGenerator.find('#output').addClass("error-message");
                    self._cronGenerator.find('#saveButton').attr("disabled","disabled");
                 }
                 if(validateTimeValue(second, constants.MAX_MINUTE_AND_SECOND_VALUE)){
                    self._cronGenerator.find('#second').removeClass("error-field");
                    self._cronGenerator.find("#second").val(splitValues[0]);
                    self._cronGenerator.find('#errorMsg').hide();
                 } else {
                    self._cronGenerator.find('#second').addClass("error-field");
                 }
                 if(validateTimeValue(minute, constants.MAX_MINUTE_AND_SECOND_VALUE)){
                    self._cronGenerator.find('#minute').removeClass("error-field");
                    self._cronGenerator.find("#minute").val(splitValues[1]);
                    self._cronGenerator.find('#errorMsg').hide();
                 } else {
                    self._cronGenerator.find('#minute').addClass("error-field");
                 }
                 if(validateTimeValue(hour, constants.MAX_HOUR_VALUE)){
                     self._cronGenerator.find('#hour').removeClass("error-field");
                     self._cronGenerator.find("#hour").val(splitValues[2]);
                     self._cronGenerator.find('#errorMsg').hide();
                 } else {
                     self._cronGenerator.find('#hour').addClass("error-field");
                 }
                 if(validateDayOfMonthValue(dayMonth, dayWeek)){
                     self._cronGenerator.find('#dayMonth').removeClass("error-field");
                     self._cronGenerator.find("#dayMonth").val(splitValues[3]);
                     self._cronGenerator.find('#errorMsg').hide();
                 } else {
                     self._cronGenerator.find('#dayMonth').addClass("error-field");
                 }
                 if(validateMonthValue(month)){
                     self._cronGenerator.find('#month').removeClass("error-field");
                     self._cronGenerator.find("#month").val(splitValues[4]);
                     self._cronGenerator.find('#errorMsg').hide();
                 } else {
                     self._cronGenerator.find('#month').addClass("error-field");
                 }
                 if(validateDayOfWeekValue(dayWeek, dayMonth)){
                     self._cronGenerator.find('#dayWeek').removeClass("error-field");
                     self._cronGenerator.find("#dayWeek").val(splitValues[5]);
                     self._cronGenerator.find('#errorMsg').hide();
                 } else {
                     self._cronGenerator.find('#dayWeek').addClass("error-field");
                 }
            });
        };

        CronGenerator.prototype.isErrorOccured = function(){
            var self = this;
            var value = self._cronGenerator.find('#second').hasClass("error-field") ||
                        self._cronGenerator.find('#minute').hasClass("error-field") ||
                        self._cronGenerator.find('#hour').hasClass("error-field") ||
                        self._cronGenerator.find('#dayMonth').hasClass("error-field") ||
                        self._cronGenerator.find('#month').hasClass("error-field") ||
                        self._cronGenerator.find('#dayWeek').hasClass("error-field");
            if(value){
                self._cronGenerator.find('#expression').addClass("error-field");
                self._cronGenerator.find('#output').html("Invalid Cron Expression");
                self._cronGenerator.find('#output').addClass("error-message");
                self._cronGenerator.find('#saveButton').attr("disabled","disabled");
            } else {
                self._cronGenerator.find('#expression').removeClass("error-field");
                self._cronGenerator.find('#output').removeClass("error-message");
                self._cronGenerator.find('#saveButton').removeAttr("disabled");
            }
        };

        CronGenerator.prototype.selectListOfValues = function(){
            var self = this;
            var i; var length;
            var selectBoxObject = self._cronGenerator.find('#selectListValues');
            if(self._cronGenerator.find('#everySecond').is(':checked')){
                i = 0; length = 59;
                self._cronGenerator.find('#selectListValues').empty();
            } else if (self._cronGenerator.find('#everyMinute').is(':checked')){
                i = 0; length = 59;
                self._cronGenerator.find('#selectListValues').empty();
            } else if (self._cronGenerator.find('#everyHour').is(':checked')){
                i = 0; length = 23;
                self._cronGenerator.find('#selectListValues').empty();
            } else if(self._cronGenerator.find('#everyDay').is(':checked')){
                i = 1; length = 31;
                self._cronGenerator.find('#selectListValues').empty();
            }
            for(i; i<=length; i++){
                optionValues = '<option value = ' + i + '>' + i + '</option>';
                selectBoxObject.append(optionValues);
            }
        };
        return CronGenerator;
    });