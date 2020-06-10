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

define(['require', 'lodash', 'jquery', 'log', 'cronstrue', 'jquery_timepicker'],
    function (require, _ , $, log, Cronstrue, Timepicker) {

        var CronGenerator = function(){
            var self = this;
            this._cronGenerator = $('#cronGenerator').clone();
        };
        var errorMsg;

        var isValidTimeValue = function(time, val) {
            if(time === '*') {
                return true;
            }
            if (time.search(/[^\d-,*\/]/) !== -1) {
                return false;
            }
            var list = time.split(",");
            return list.every(function (time){
                if(time.includes('/')) {
                    var startingTimeOptionArr = time.split('/');
                    return isValidateTime(startingTimeOptionArr, val) ||
                            (startingTimeOptionArr[0] === '*' && isValidateTime([startingTimeOptionArr[1]], val));
                } else if(time.includes('-')) {
                    var timeRangeArr = time.split('-');
                    return isValidateTime(timeRangeArr, val) && (parseInt(timeRangeArr[0]) < parseInt(timeRangeArr[1]));
                } else if (parseInt(time) >= 0 && parseInt(time) <= val) {
                    return true;
                } else {
                    return false;
                }
            });
        };

        var isValidateTime = function(dataArray, value) {
            return dataArray.every(element => {
                if(parseInt(element) < 0 || parseInt(element) > value){
                    errorMsg = "Numeric values between " + 0 + " and " + value;
                } else {
                    errorMsg = "Invalid cron Expression";
                }
                return parseInt(element) >= 0  && parseInt(element) <= value;
            });
        };

        var isValidateNumericValue = function(monthArr, val, endVal) {
            return monthArr.every(month => {
                if(parseInt(month) < val || parseInt(month) > endVal){
                    errorMsg = "Numeric values between " + val + " and " + endVal;
                } else {
                    errorMsg = "Invalid Cron Expression"
                }
                return parseInt(month) >= val && parseInt(month) <= endVal;
            });
        };

        var isValidDayOfMonthValue = function(dayOfMonth, dayOfWeek) {
            if((dayOfMonth === '*' && dayOfWeek !== '*') || (dayOfMonth === '?' && dayOfWeek !== '?')) {
                return true;
            } else if(typeof dayOfMonth === 'string' && dayOfWeek === '?' && (dayOfMonth === 'LW' || dayOfMonth === 'L')){
                return true;
            } else if(typeof dayOfMonth === 'string' && dayOfWeek === '?' && dayOfMonth.includes('L')) {
                if(dayOfMonth.includes(',') && dayOfWeek === '?'){
                    return false;
                }
                if(dayOfMonth.includes('-') && dayOfWeek === '?') {
                    var dayOfMonthRangeArr = dayOfMonth.split('-');
                    var isLastDayIncludes = dayOfMonthRangeArr[0] === 'L' &&
                                                        isValidateNumericValue([dayOfMonthRangeArr[1]], 1, 30);
                    return isValidateNumericValue(dayOfMonthRangeArr, 1, 31) || isLastDayIncludes;
                }
            }
            if (dayOfMonth.search(/[^\d-,*\/]/) !== -1) {
                return false;
            }
            var list = dayOfMonth.split(",");
            return list.every(function (dayOfMonth){
                if(dayOfMonth.includes('/') && dayOfWeek === '?') {
                    var startingDayOfMonthOptionArr = dayOfMonth.split('/');
                    var isValidElements = (isValidateNumericValue([startingDayOfMonthOptionArr[0]], 1, 31) &&
                                                        isValidateNumericValue([startingDayOfMonthOptionArr[1]], 1, 31));
                    var isValidFirstElem = (startingDayOfMonthOptionArr[0] === '*' &&
                                                        isValidateNumericValue([startingDayOfMonthOptionArr[1]], 1, 31));
                    return isValidElements || isValidFirstElem;
                } else if(dayOfMonth.includes('-') && dayOfWeek === '?') {
                    var dayOfMonthRangeArr = dayOfMonth.split('-');

                    return isValidateNumericValue(dayOfMonthRangeArr, 1, 31) &&
                                    (parseInt(dayOfMonthRangeArr[0]) < parseInt(dayOfMonthRangeArr[1]));

                } else if(typeof dayOfMonth === 'string' && dayOfWeek === '?') {
                       return isValidateNumericValue([dayOfMonth], 1, 31);
                }
            });
        };

        var MONTH_LIST = ['jan', 'feb', 'mar', 'apr', 'may', 'jun', 'jul', 'aug', 'sep', 'oct', 'nov', 'dec'];

        var isValidateStringValue = function(monthArr, dataArr) {
            errorMsg = "Invalid Cron Expression";
            return monthArr.every(month => {
                return dataArr.includes(month.toLowerCase());
            })
        };

        var isValidMonthValue = function(month) {
            if(month === '*') {
                return true;
            }
            if(month.includes('_')){
                return false;
            }
            if (month.search(/[^\w-,*\/]/) !== -1) {
                return false;
            }
            var list = month.split(",");
            return list.every(function (month){
                if(month.includes('/')) {
                    var startingDayOfMonthOptionArr = month.split('/');
                    var isValidElements = (isValidateNumericValue([startingDayOfMonthOptionArr[0]], 1, 12) &&
                                                        isValidateNumericValue([startingDayOfMonthOptionArr[1]], 1, 12));
                    var isValidFirstElem = (startingDayOfMonthOptionArr[0] === '*' &&
                                                        isValidateNumericValue([startingDayOfMonthOptionArr[1]], 1, 12));
                    return isValidElements || isValidFirstElem;

                } else if(month.includes('-')) {
                    var monthRangeArr = month.split('-');
                    var validMonthRange = parseInt(monthRangeArr[0]) < parseInt(monthRangeArr[1]);
                    var validMonthStrRange = MONTH_LIST.indexOf(monthRangeArr[0]) < MONTH_LIST.indexOf(monthRangeArr[1]);

                    return !isNaN(parseInt(monthRangeArr[0])) && !isNaN(parseInt(monthRangeArr[1])) ?
                        isValidateNumericValue(monthRangeArr, 1, 12) && validMonthRange :
                        isValidateStringValue(monthRangeArr, MONTH_LIST) && validMonthStrRange;
                } else if(typeof month === 'string') {
                    var firstIndexValue = month.charAt(0);
                    var secondIndexValue = month.charAt(1);
                    if(month.length === 1){
                        return isValidateNumericValue([month], 1, 12)
                    } else {
                        return (!isNaN(parseInt(firstIndexValue)) && !isNaN(parseInt(secondIndexValue))) ?
                                isValidateNumericValue([month], 1, 12) : isValidateStringValue([month], MONTH_LIST);
                    }
                } else {
                    return false;
                }
            });
        };

        var WEEK_ARRRAY = ['mon', 'tue', 'wed', 'thu', 'fri', 'sat','sun'];

        var isValidDayOfWeekValue = function(dayOfWeek, dayOfMonth) {
            if((dayOfWeek === '*' && dayOfMonth !== '*') || (dayOfWeek === '?' && dayOfMonth !== '?')) {
                return true;
            }
            if(dayOfWeek === 'L') {
                return true;
            }
            if(typeof dayOfWeek === 'string' && dayOfMonth === '?' && dayOfWeek.includes('L')) {
                if(dayOfWeek.includes(',') && dayOfMonth === '?'){
                    return false;
                }
                if (!isNaN(parseInt(dayOfWeek))){
                    return isValidateNumericValue([dayOfWeek], 1, 7);
                }
            }
            if(dayOfWeek.includes('_')){
                return false;
            }
            if (dayOfWeek.search(/[^\w-,*\/#]/) !== -1) {
                return false;
            }
            var list = dayOfWeek.split(",");
            return list.every(function (dayOfWeek){
                if(dayOfWeek.includes('/') && dayOfMonth === '?') {
                    var startingDayOfWeekOptionArr = dayOfWeek.split('/');

                    var isValidElements = (isValidateNumericValue([startingDayOfWeekOptionArr[0]], 1, 7) &&
                                                        isValidateNumericValue([startingDayOfWeekOptionArr[1]], 1, 7));
                    var isValidFirstElem = (startingDayOfWeekOptionArr[0] === '*' &&
                                                        isValidateNumericValue([startingDayOfWeekOptionArr[1]], 1, 7));
                    return isValidElements || isValidFirstElem;

                } else if(dayOfWeek.includes('-') && dayOfMonth === '?') {
                    var dayOfWeekRangeArr = dayOfWeek.split('-');
                    var validWeekRange = parseInt(dayOfWeekRangeArr[0]) < parseInt(dayOfWeekRangeArr[1]);
                    var validWeekStrRange = WEEK_ARRRAY.indexOf(dayOfWeekRangeArr[0]) < WEEK_ARRRAY.indexOf(dayOfWeekRangeArr[1]);
                    return !isNaN(parseInt(dayOfWeekRangeArr[0])) && !isNaN(parseInt(dayOfWeekRangeArr[1])) ?
                        isValidateNumericValue(dayOfWeekRangeArr, 1, 7) && validWeekRange :
                        isValidateStringValue(dayOfWeekRangeArr, WEEK_ARRRAY) && validWeekStrRange;

                } else if(dayOfWeek.includes('#') && dayOfMonth === '?') {
                    var weekdayOfMonthArr = dayOfWeek.split('#');
                        return (isValidateStringValue([weekdayOfMonthArr[0]], WEEK_ARRRAY) &&
                                isValidateNumericValue([weekdayOfMonthArr[1]], 1, 5)) ||
                                 (isValidateNumericValue([weekdayOfMonthArr[0]], 1, 7) &&
                                 isValidateNumericValue([weekdayOfMonthArr[1]], 1, 5));

                } else if(typeof dayOfWeek === 'string' && dayOfMonth === '?') {
                    var firstIndexValue = dayOfWeek.charAt(0);
                    var secondIndexValue = dayOfWeek.charAt(1);
                    if(dayOfWeek.length === 1){
                        return isValidateNumericValue([dayOfWeek], 1, 7)
                    } else {
                        return (!isNaN(parseInt(firstIndexValue)) && !isNaN(parseInt(secondIndexValue))) ?
                                isValidateNumericValue([dayOfWeek], 1, 7) : isValidateStringValue([dayOfWeek], WEEK_ARRRAY);
                    }
                } else {
                    return false;
                }
            });
        };

        var isValidCronExpression = function(cronExpression) {
            if(!/\s/g.test(cronExpression)) {
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

            var isValidSeconds = isValidTimeValue(seconds, 59);
            var isValidMinutes = isValidTimeValue(minutes, 59);
            var isValidHour = isValidTimeValue(hours, 23);
            var isValidDayOfMonth = isValidDayOfMonthValue(dayOfMonth, dayOfWeek);
            var isValidMonth = isValidMonthValue(month);
            var isValidDayOfWeek = isValidDayOfWeekValue(dayOfWeek, dayOfMonth);

            var isValidCron = isValidSeconds && isValidMinutes && isValidHour && isValidDayOfMonth &&
                                                                            isValidMonth && isValidDayOfWeek;

            if(isValidCron) {
                return isValidCron;
            }
        };

        CronGenerator.prototype.initialization = function(optionParent){
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
                 new CronGenerator().render(optionParent);
            });
        };

        CronGenerator.prototype.render = function(optionParent){
            var self = this;
            self._cronGenerator.modal('show');
            self.renderBasicCronExpression();
            self.renderRegularCronExpression();
            self.renderPredefinedCronExpression();
            self.renderAdvancedCronExpression();
            self._cronGenerator.find("#saveButton").on('click', function(){
                var selectedValue; var inputValue;
               if(self._cronGenerator.find('#basicTab').hasClass('active')){
                   selectedValue = self._cronGenerator.find('#basic-cron-list :selected').val();
                  $(optionParent).find('.option-value').val(selectedValue);
               }else if(self._cronGenerator.find('#regularTab').hasClass('active')){
                    selectedValue = self._cronGenerator.find('#selectListValues :selected').val();
                    if(self._cronGenerator.find('#everySecond').is(':checked')){
                        inputValue = "*/" + selectedValue + " * * * * ?";
                    }else if(self._cronGenerator.find('#everyMinute').is(':checked')){
                        inputValue = "0 */" + selectedValue + " * * * ?";
                    }else if(self._cronGenerator.find('#everyHour').is(':checked')){
                        inputValue = "0 0 */" + selectedValue + " * * ?";
                    }else if(self._cronGenerator.find('#everyDay').is(':checked')){
                        var timeValue = self._cronGenerator.find("#timeForEveryDay").val();
                        var time = timeValue.split(":");
                        var hour = time[0];
                        var minute = time[1];
                        inputValue = "0 " + minute + " " + hour + " */" + selectedValue + " * ?";
                    }
                    $(optionParent).find('.option-value').val(inputValue);
               }else if(self._cronGenerator.find('#predefineTab').hasClass('active')){
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
               }else if(self._cronGenerator.find('#advancedTab').hasClass('active')){
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
                     '<option value = "0 0 * * * ?">Every minute</option>'+
                     '<option value = "0 0 * * * ?">Every hour</option>'+
                     '<option value = "0 0 12 * * ?">Every day at noon</option>'+
                     '<option value = "0 0 12 ? * sun">Every week on Sunday at noon</option>'+
                     '<option value = "0 0 12 1 * ?">On the First of every month at noon</option>'+
                     '<option value = "0 0 12 1 1 ?">On the First of January of every year at noon</option>'+
                     '</select>');
            self._cronGenerator.find("#basic-content").html(cronBasic);
            self._cronGenerator.find("#cron-basic").addClass('active');
            self._cronGenerator.find('a[href="#cron-basic"]').click(function () {
                  self._cronGenerator.find("#cron-basic").addClass('active');
                  self._cronGenerator.find("#cron-regular").removeClass('active');
                  self._cronGenerator.find("#cron-predefined").removeClass('active');
                  self._cronGenerator.find("#cron-advanced").removeClass('active');
                  self._cronGenerator.find("#basic-content option[value='0 * * * * ?']").attr('selected',true);
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
                    'onblur="this.size=1;" onchange="this.size=1; this.blur();"></select>');
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
                var description = Cronstrue.toString(expression, { throwExceptionOnParseError: false});
                if(isValidTimeValue(secValue, 59)){
                    self._cronGenerator.find('#second').removeClass("error-field");
                    self._cronGenerator.find("#output").text(description);
                    self._cronGenerator.find('#errorMsg').hide();
                } else {
                    self._cronGenerator.find('#second').addClass("error-field");
                    self._cronGenerator.find('#errorMsg').text(errorMsg);
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
                var description = Cronstrue.toString(expression, { throwExceptionOnParseError: false });
                if(isValidTimeValue(minValue, 59)){
                   self._cronGenerator.find('#minute').removeClass("error-field");
                   self._cronGenerator.find("#output").text(description);
                   self._cronGenerator.find('#errorMsg').hide();
                } else {
                   self._cronGenerator.find('#minute').addClass("error-field");
                   self._cronGenerator.find('#errorMsg').text(errorMsg);
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
                var description = Cronstrue.toString(expression, { throwExceptionOnParseError: false });
                var hourValue = self._cronGenerator.find('#hour').val();
                if(isValidTimeValue(hourValue, 23)){
                    self._cronGenerator.find('#hour').removeClass("error-field");
                    self._cronGenerator.find("#output").text(description);
                    self._cronGenerator.find('#errorMsg').hide();
                } else {
                    self._cronGenerator.find('#hour').addClass("error-field");
                    self._cronGenerator.find('#errorMsg').text(errorMsg);
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
                if(isValidDayOfMonthValue(dayMonValue, dayWeekValue)){
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
                var description = Cronstrue.toString(expression, { throwExceptionOnParseError: false });
                var monValue = self._cronGenerator.find('#month').val();
                if(isValidMonthValue(monValue)){
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
                 var description = Cronstrue.toString(expression, { throwExceptionOnParseError: false });

                 var weekValue = self._cronGenerator.find('#dayWeek').val();
                 var dayMonValue = self._cronGenerator.find('#dayMonth').val();
                 if(isValidDayOfWeekValue(weekValue, dayMonValue)){
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
                 var description = Cronstrue.toString(expression, { throwExceptionOnParseError: false });
                 var splitValues = expression.split(" ");
                 var second = splitValues[0];
                 var minute = splitValues[1];
                 var hour = splitValues[2];
                 var dayMonth = splitValues[3];
                 var month = splitValues[4];
                 var dayWeek = splitValues[5];
                 if(isValidCronExpression(expression) === true){
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
                 if(isValidTimeValue(second, 59)){
                    self._cronGenerator.find('#second').removeClass("error-field");
                    self._cronGenerator.find("#second").val(splitValues[0]);
                    self._cronGenerator.find('#errorMsg').hide();
                 } else {
                    self._cronGenerator.find('#second').addClass("error-field");
                 }
                 if(isValidTimeValue(minute, 59)){
                    self._cronGenerator.find('#minute').removeClass("error-field");
                    self._cronGenerator.find("#minute").val(splitValues[1]);
                    self._cronGenerator.find('#errorMsg').hide();
                 } else {
                    self._cronGenerator.find('#minute').addClass("error-field");
                 }
                 if(isValidTimeValue(hour, 23)){
                     self._cronGenerator.find('#hour').removeClass("error-field");
                     self._cronGenerator.find("#hour").val(splitValues[2]);
                     self._cronGenerator.find('#errorMsg').hide();
                 } else {
                     self._cronGenerator.find('#hour').addClass("error-field");
                 }
                 if(isValidDayOfMonthValue(dayMonth, dayWeek)){
                     self._cronGenerator.find('#dayMonth').removeClass("error-field");
                     self._cronGenerator.find("#dayMonth").val(splitValues[3]);
                     self._cronGenerator.find('#errorMsg').hide();
                 } else {
                     self._cronGenerator.find('#dayMonth').addClass("error-field");
                 }
                 if(isValidMonthValue(month)){
                     self._cronGenerator.find('#month').removeClass("error-field");
                     self._cronGenerator.find("#month").val(splitValues[4]);
                     self._cronGenerator.find('#errorMsg').hide();
                 } else {
                     self._cronGenerator.find('#month').addClass("error-field");
                 }
                 if(isValidDayOfWeekValue(dayWeek, dayMonth)){
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
            for(i; i <= length; i++){
                optionValues = '<option value = ' + i + '>' + i + '</option>';
                selectBoxObject.append(optionValues);
            }
        };
        return CronGenerator;
    });