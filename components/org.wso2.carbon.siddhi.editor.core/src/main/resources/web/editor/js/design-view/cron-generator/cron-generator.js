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
            CRON_EXPRESSION_FOR_EVERY_MINUTE: '0 * * * * ?',
            MAX_MINUTE_AND_SECOND_VALUE: 59,
            MAX_HOUR_VALUE: 23,
            REGEX_FOR_TIME: /[^\d-,*\/]/ ,
            REGEX_FOR_DAYOFMONTH: /[^\d-,?*\/]/ ,
            REGEX_FOR_MONTH: /[^\w-,*\/]/,
            REGEX_FOR_DAYOFWEEK: /[^\w-,*?\/#]/,
            REGEX_FOR_SPACE: /\s/g,
            REGEX_FOR_ALPHA_NUMERIC: /[^\dA-Z]/,
            REGEX_FOR_NUMBERS: /[^\d]/
        };

        var validateTimeValue = function(time, maxValue) {
            var error;
            if (time.search(constants.REGEX_FOR_TIME) !== -1) {
                return {
                    status: false,
                    error: "Error : Invalid characters used. See tooltip for allowed characters."
                };
            }
            var list = time.split(",");
            var status = list.every(function (time){
                if(time === '*' && list.length > 1){
                    error = "Error : The '*' character should not be used with other values";
                    return false;
                }
                if(time.includes('/')) {
                    if(time.endsWith('/')){
                        error = "Error : Expression should not end with \" / \" ";
                        return false;
                    }
                    var startingTimeOptionArr = time.split('/');
                    if (startingTimeOptionArr.length > 2) {
                        error = "Error : Unsupported value";
                        return false;
                    } else {
                          error = "Error : Expression values must lie between 0 and " + maxValue;
                          return validateTimeRange(startingTimeOptionArr, maxValue) ||
                          (startingTimeOptionArr[0] === '*' && validateTimeRange([startingTimeOptionArr[1]], maxValue));
                    }
                } else if(time.includes('-')) {
                    if(time.endsWith('-')){
                        error = "Error : Expression should not end with \" - \" ";
                        return false;
                    }
                    var timeRangeArr = time.split('-');
                    if(timeRangeArr.length > 2){
                        error =  "Error : Unsupported value";
                        return false;
                    } else {
                        return validateTimeRange(timeRangeArr, maxValue) &&
                                (parseInt(timeRangeArr[0]) < parseInt(timeRangeArr[1]));
                    }
                } else {
                     error = "Error : Expression values must lie between 0 and " + maxValue;
                     return validateTimeRange([time],maxValue) || time === '*';
                }
                return true;
            });
            if(status){
                return {status:true};
            }
            return {status:false,error:error};
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
            var error;
            if(dayOfWeek === '?' && dayOfMonth.includes('L')) {
                if(dayOfMonth.includes('-')) {
                    var dayOfMonthRangeArr = dayOfMonth.split('-');
                    if(dayOfMonthRangeArr[1].search(constants.REGEX_FOR_NUMBERS) !== -1 ){
                        return {
                            status : false,
                            error : "Error : Invalid Cron Expression, Expression can contain numeric values."
                        };
                    } else {
                        return {
                           status : dayOfMonthRangeArr[0] === 'L' && validateRangeOfDay([dayOfMonthRangeArr[1]], 1, 30),
                           error :  "Error : Day of month values must be between 1 and 31"
                        };
                    }
                } else {
                    return {
                        status : dayOfMonth === 'L' || dayOfMonth === 'LW',
                        error : "Error : Invalid Cron Expression, 'L' option is not valid here."
                    };
                }
            } else if(dayOfMonth.includes('W') && dayOfWeek === '?'){
                if(dayOfMonth.search(constants.REGEX_FOR_ALPHA_NUMERIC) !== -1 ){
                    return {
                        status : false,
                        error : "Error : Invalid Cron Expression, Expression can contain alpha numeric values."
                    };
                }
                if(dayOfMonth.length === 1){
                    return {
                        status : false,
                        error : "Error : Invalid Cron Expression"
                    };
                } else {
                    return {
                        status : validateRangeOfDay([dayOfMonth], 1, 31),
                        error : "Error : Day of month values must be between 1 and 31"
                    };
                }
            }
            if (dayOfMonth.search(constants.REGEX_FOR_DAYOFMONTH) !== -1) {
                return {
                    status : false,
                    error : "Error : Invalid characters used. See tooltip for allowed characters."
                };
            }
            var list = dayOfMonth.split(",");
            var status = list.every(function (dayOfMonth){
                if(((dayOfMonth === '*' && dayOfWeek !== '*') || (dayOfMonth === '?' && dayOfWeek !== '?')) && list.length > 1){
                    error = "Error : The '*' character should not be used with other values";
                    return false;
                }
                if(dayOfMonth.includes('/') && dayOfWeek === '?') {
                    if(dayOfMonth.endsWith('/')){
                        error = "Error : Day of month values should not end with \" / \"";
                        return false;
                    }
                    var startingDayOfMonthOptionArr = dayOfMonth.split('/');
                    if(startingDayOfMonthOptionArr.length > 2 ){
                        error = "Error : Unsupported values";
                        return false;
                    } else {
                        var isValidElements = validateRangeOfDay(startingDayOfMonthOptionArr, 1, 31) ;
                        var isValidFirstElem = (startingDayOfMonthOptionArr[0] === '*' &&
                                                validateRangeOfDay([startingDayOfMonthOptionArr[1]], 1, 31));
                        error = "Error : Day of month values must be between 1 and 31";
                        return isValidElements || isValidFirstElem;
                    }

                } else if(dayOfMonth.includes('-') && dayOfWeek === '?') {
                    if(dayOfMonth.endsWith('-')){
                        error = "Error : Day of month values should not end with \" - \" ";
                        return false;
                    }
                    var dayOfMonthRangeArr = dayOfMonth.split('-');
                    if(dayOfMonthRangeArr.length > 2){
                        error = "Error : Unsupported value ";
                        return false;
                    }
                    else {
                        error = "Error : Day of month values must be between 1 and 31";
                        return validateRangeOfDay(dayOfMonthRangeArr, 1, 31) &&
                                            (parseInt(dayOfMonthRangeArr[0]) < parseInt(dayOfMonthRangeArr[1]));
                    }
                } else if(dayOfWeek === '?'){
                    error = "Error : Day of month values must be between 1 and 31";
                    return validateRangeOfDay([dayOfMonth], 1, 31) ||
                            (dayOfMonth === '*' && dayOfWeek !== '*') || (dayOfMonth === '?' && dayOfWeek !== '?');
                } else {
                    error = "Error : Invalid Cron Expression, Expression cannot contain any values except \" ? \" ";
                    return dayOfMonth === '?';
                }
                return true;
            });
            if(status){
                return {status:true};
            }
            return {status:false,error:error};
        };

        var monthArray = ['jan', 'feb', 'mar', 'apr', 'may', 'jun', 'jul', 'aug', 'sep', 'oct', 'nov', 'dec'];

        // this method is used to validate the month values [jan - dec] and week values [sun - sat]
        var validateAbbreviationValue = function(monthArr, dataArr) {
            return monthArr.every(month => {
                return dataArr.includes(month.toLowerCase());
            })
        };

        var validateMonthValue = function(month) {
            var error;
            if(month.includes('_')){
                return {
                    status : false,
                    error : "Error : Invalid characters used. See tooltip for allowed characters."
                };
            } else if (month.search(constants.REGEX_FOR_MONTH) !== -1) {
                return {
                    status : false,
                    error : "Error : Invalid characters used. See tooltip for allowed characters."
                };
            }
            var list = month.split(",");
            var status = list.every(function (month){
                if(month === '*' && list.length > 1){
                    error = "Error : The '*' character should not be used with other values";
                    return false;
                }
                if(month.includes('/')) {
                    if(month.endsWith('/')){
                        error = "Error : Month values should not end with \" / \" ";
                        return false;
                    }
                    var startingMonthOptionArr = month.split('/');
                    if(startingMonthOptionArr.length  > 2){
                        error = "Error : Unsupported values ";
                        return false;
                    }
                    else {
                        var isValidElements = validateRangeOfDay(startingMonthOptionArr, 1, 12);
                        var isValidFirstElem = (startingMonthOptionArr[0] === '*' &&
                                                        validateRangeOfDay([startingMonthOptionArr[1]], 1, 12));
                        error = "Error : Month values must be between 1 and 12.";
                        return isValidElements || isValidFirstElem;
                    }
                } else if(month.includes('-')) {
                    if(month.endsWith('-')){
                        error = "Error : Month values should not end with \" - \" ";
                        return false;
                    }
                    var monthRangeArr = month.split('-');
                    if(monthRangeArr.length > 2){
                        error = "Error : Unsupported values ";
                        return false;
                    } else {
                        var validMonthRange = parseInt(monthRangeArr[0]) < parseInt(monthRangeArr[1]);
                        var validMonthStrRange = monthArray.indexOf(monthRangeArr[0].toLowerCase()) <
                                                                    monthArray.indexOf(monthRangeArr[1].toLowerCase());
                        error = "Error : Month values must be between 1 and 12 or Jan - Dec";
                        return !isNaN(parseInt(monthRangeArr[0])) && !isNaN(parseInt(monthRangeArr[1])) ?
                            validateRangeOfDay(monthRangeArr, 1, 12) && validMonthRange :
                            validateAbbreviationValue(monthRangeArr, monthArray) && validMonthStrRange;
                    }
                } else {
                    var firstIndexValue = month.charAt(0);
                    var secondIndexValue = month.charAt(1);
                    if(month.length === 1){
                        error = "Error : Month values must be between 1 and 12 ";
                        return validateRangeOfDay([month], 1, 12) || month === "*";
                    } else {
                         error = "Error : Month values must be between 1 and 12 or Jan - Dec ";
                        return (!isNaN(parseInt(firstIndexValue)) && !isNaN(parseInt(secondIndexValue))) ?
                                validateRangeOfDay([month], 1, 12) : validateAbbreviationValue([month], monthArray);
                    }
                }
                return true;
            });
            if(status){
                return {status:true};
            }
            return {status:false,error:error};
        };

        var weekArray = ['sun','mon', 'tue', 'wed', 'thu', 'fri', 'sat'];

        var validateDayOfWeekValue = function(dayOfWeek, dayOfMonth) {
            var error;
            if(dayOfMonth === '?' && dayOfWeek.includes('L')) {
                if(dayOfWeek.includes(',')){
                    return {
                        status : false,
                        error : "Error : Support for specifying 'L' with other days of the month is not implemented."
                    };
                } else if(dayOfWeek.includes('-')){
                    return {
                        status : false,
                        error : "Error : Day-of-week values should not end with \" - \" "
                    };
                } else {
                    return {
                        status : validateRangeOfDay([dayOfWeek], 1, 7) || dayOfWeek === 'L',
                        error : "Error : Day-of-Week values must be between 1 and 7"
                    };
                }
            }else if(dayOfWeek.includes('_')){
                return {
                    status : false,
                    error : "Error : Invalid characters used. See tooltip for allowed characters."
                };
            } else if (dayOfWeek.search(constants.REGEX_FOR_DAYOFWEEK) !== -1) {
                return {
                    status : false,
                    error : "Error : Invalid characters used. See tooltip for allowed characters."
                };
            }
            var list = dayOfWeek.split(",");
            var status = list.every(function (dayOfWeek){
                if(((dayOfWeek === '*' && dayOfMonth !== '*') || (dayOfWeek === '?' && dayOfMonth !== '?')) && list.length > 1){
                    error = "Error : The '*' character should not be used with other values";
                    return false;
                }
                if(dayOfWeek.includes('/') && dayOfMonth === '?') {
                    if(dayOfWeek.endsWith('/')){
                        error = "Error : Day-of-week values should not end with \" / \" ";
                        return false;
                    }
                    var startingDayOfWeekOptionArr = dayOfWeek.split('/');
                    if(startingDayOfWeekOptionArr.length > 2){
                        error = "Error : Unsupported value ";
                        return false;
                    } else {
                        var isValidElements = validateRangeOfDay(startingDayOfWeekOptionArr, 1, 7);
                        var isValidFirstElem = (startingDayOfWeekOptionArr[0] === '*' &&
                                        validateRangeOfDay([startingDayOfWeekOptionArr[1]], 1, 7));
                        error = "Error : Day-of-Week values must be between 1 and 7";
                        return isValidElements || isValidFirstElem;
                    }
                } else if(dayOfWeek.includes('-') && dayOfMonth === '?') {
                    if(dayOfWeek.endsWith('-')){
                        error = "Error : Day-of-week values should not end with \" - \" ";
                        return false;
                    }
                    var dayOfWeekRangeArr = dayOfWeek.split('-');
                    if(dayOfWeekRangeArr.length > 2){
                        error = "Error : Unsupported value ";
                        return false;
                    } else {
                        var validWeekRange = parseInt(dayOfWeekRangeArr[0]) < parseInt(dayOfWeekRangeArr[1]);
                        var validWeekStrRange = weekArray.indexOf(dayOfWeekRangeArr[0].toLowerCase()) <
                                                                weekArray.indexOf(dayOfWeekRangeArr[1].toLowerCase());
                        error = "Error : Day-of-Week values must be between 1 and 7";
                        return !isNaN(parseInt(dayOfWeekRangeArr[0])) && !isNaN(parseInt(dayOfWeekRangeArr[1])) ?
                                validateRangeOfDay(dayOfWeekRangeArr, 1, 7) && validWeekRange :
                                validateAbbreviationValue(dayOfWeekRangeArr, weekArray) && validWeekStrRange;
                    }
                } else if(dayOfWeek.includes('#') && dayOfMonth === '?') {
                    if(dayOfWeek.endsWith('#')){
                        error = "Error : Day-of-week values should not end with \" # \" ";
                        return false;
                    }
                    var weekdayOfMonthArr = dayOfWeek.split('#');
                    if(weekdayOfMonthArr.length > 2){
                        error = "Error : Unsupported value";
                        return false;
                    } else {
                        error = "Error : Day-of-Week values must be between 1 and 7";
                        return (validateAbbreviationValue([weekdayOfMonthArr[0]], weekArray) &&
                                    validateRangeOfDay([weekdayOfMonthArr[1]], 1, 5)) ||
                                     (validateRangeOfDay([weekdayOfMonthArr[0]], 1, 7) &&
                                     validateRangeOfDay([weekdayOfMonthArr[1]], 1, 5));
                    }
                } else if(dayOfMonth === '?'){
                    var firstIndexValue = dayOfWeek.charAt(0);
                    var secondIndexValue = dayOfWeek.charAt(1);
                    if(dayOfWeek.length === 1){
                        error = "Error : Day-of-Week values must be between 1 and 7";
                        return validateRangeOfDay([dayOfWeek], 1, 7) ||
                            (dayOfWeek === '*' && dayOfMonth !== '*') || (dayOfWeek === '?' && dayOfMonth !== '?');
                    } else {
                        error = "Error : Day-of-Week values must be between 1 and 7";
                        return (!isNaN(parseInt(firstIndexValue)) && !isNaN(parseInt(secondIndexValue))) ?
                                validateRangeOfDay([dayOfWeek], 1, 7) :
                                validateAbbreviationValue([dayOfWeek], weekArray);
                    }
                } else {
                    error = "Error : Invalid Cron Expression, Expression cannot contain any values except \" ? \" ";
                    return dayOfWeek === '?';
                }
                return true;
            });
            if(status){
                return {status:true};
            }
            return {status:false,error:error};
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

            var statusSecond = validateTimeValue(seconds, constants.MAX_MINUTE_AND_SECOND_VALUE);
            var statusMinute = validateTimeValue(minutes, constants.MAX_MINUTE_AND_SECOND_VALUE);
            var statusHour = validateTimeValue(hours, constants.MAX_HOUR_VALUE);
            var statusDayOfMonth = validateDayOfMonthValue(dayOfMonth, dayOfWeek);
            var statusMonth = validateMonthValue(month);
            var statusDayOfWeek = validateDayOfWeekValue(dayOfWeek, dayOfMonth);

            return {
                status: statusSecond.status && statusMinute.status && statusHour.status &&
                               statusDayOfMonth.status && statusMonth.status && statusDayOfWeek.status,
                error: {
                    errorForSecond: statusSecond.error,
                    errorForMinute: statusMinute.error,
                    errorForHour: statusHour.error,
                    errorForDayOfMonth : statusDayOfMonth.error,
                    errorForMonth: statusMonth.error,
                    errorForDayOfWeek: statusDayOfWeek.error
                }
            };
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
            self._cronGenerator.find("#cron-basic").addClass('active');
            self._cronGenerator.find("#cron-regular").removeClass('active');
            self._cronGenerator.find("#cron-predefined").removeClass('active');
            self._cronGenerator.find("#cron-advanced").removeClass('active');
            self._cronGenerator.find('a[href="#cron-basic"]').click(function () {
                  self._cronGenerator.find("#cron-basic").addClass('active');
                  self._cronGenerator.find("#cron-regular").removeClass('active');
                  self._cronGenerator.find("#cron-predefined").removeClass('active');
                  self._cronGenerator.find("#cron-advanced").removeClass('active');
                  self._cronGenerator.find("#basic-content option[value='"+constants.CRON_EXPRESSION_FOR_EVERY_MINUTE+
                                                                                          "']").attr('selected',true);
                  self._cronGenerator.find('#saveButton').removeAttr("disabled");
            });
        };

        CronGenerator.prototype.renderRegularCronExpression = function(){
            var self = this;
            self._cronGenerator.find('a[href="#cron-regular"]').click(function () {
                   self._cronGenerator.find("#cron-regular").addClass('active');
                   self._cronGenerator.find("#cron-basic").removeClass('active');
                   self._cronGenerator.find("#cron-predefined").removeClass('active');
                   self._cronGenerator.find("#cron-advanced").removeClass('active');
                   self._cronGenerator.find("#everySecond").prop('checked', true);
                   self.selectListOfValues();
                   self._cronGenerator.find("#TimeForRegularDay").empty();
                   self._cronGenerator.find("#dayTime").remove();
                   self._cronGenerator.find('#saveButton').removeAttr("disabled");
            });
            self._cronGenerator.find('#cron-regular').on('change', '#everySecond', function () {
               if ($(this).is(':checked')) {
                   self.selectListOfValues();
                   self._cronGenerator.find("#TimeForRegularDay").empty();
                   self._cronGenerator.find("#timeForDayOption").remove();
               }
            });
            self._cronGenerator.find('#cron-regular').on('change', '#everyMinute', function () {
               if ($(this).is(':checked')) {
                  self.selectListOfValues();
                  self._cronGenerator.find("#TimeForRegularDay").empty();
                  self._cronGenerator.find("#timeForDayOption").remove();
               }
            });
            self._cronGenerator.find('#cron-regular').on('change', '#everyHour', function () {
               if ($(this).is(':checked')) {
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
            self._cronGenerator.find('a[href="#cron-predefined"]').click(function () {
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
                  '<label> Time </label>'+
                  '<i class="fw fw-info" title=" Time in 24hour format "></i>'+
                  '<input type="text" id="predefinedtime" value="12:30" class="form-control" style="margin:0px 15px;'+
                  'width:15%;" readonly/>'+
                  '<input id="predefineEveryDay" type="checkbox" name="predefineEveryDay" value="">&nbsp;&nbsp;'+
                  '<label for="predefineEveryDay"> Everyday </label>'+
                  '</div>');
            self._cronGenerator.find("#defineTime").remove();
            self._cronGenerator.find("#timeForPredefineTab").html(defineTime);
            self._cronGenerator.find('input[name="predefineEveryDay"]').prop('checked',true);
            self._cronGenerator.find('input[name="weekDays"]').prop('checked',false);
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
            self._cronGenerator.find('a[href="#cron-advanced"]').click(function () {
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
            self._cronGenerator.find("#second").on("input", debounce(function(){
                var secValue = self._cronGenerator.find('#second').val();
                var statusSecond = validateTimeValue(secValue, constants.MAX_MINUTE_AND_SECOND_VALUE);
                if(statusSecond.status){
                    self._cronGenerator.find('#second').removeClass("error-field");
                    self.buildCronExpression();
                } else {
                    self._cronGenerator.find('#second').addClass("error-field");
                }
                self.listOfErrorMessage();
                self.isErrorOccurred();
            },250));
            self._cronGenerator.find("#minute").on("input", debounce(function(){
                var minValue = self._cronGenerator.find('#minute').val();
                var statusMinute = validateTimeValue(minValue, constants.MAX_MINUTE_AND_SECOND_VALUE);
                if(statusMinute.status){
                   self._cronGenerator.find('#minute').removeClass("error-field");
                   self.buildCronExpression();
                } else {
                   self._cronGenerator.find('#minute').addClass("error-field");
                }
                self.listOfErrorMessage();
                self.isErrorOccurred();
            },250));
            self._cronGenerator.find("#hour").on("input", debounce(function(){
                var hourValue = self._cronGenerator.find('#hour').val();
                var statusHour = validateTimeValue(hourValue, constants.MAX_HOUR_VALUE);
                if(statusHour.status){
                    self._cronGenerator.find('#hour').removeClass("error-field");
                    self.buildCronExpression();
                } else {
                    self._cronGenerator.find('#hour').addClass("error-field");
                }
                self.listOfErrorMessage();
                self.isErrorOccurred();
            },250));
            self._cronGenerator.find("#dayMonth").on("input", debounce(function(){
                var dayMonValue = self._cronGenerator.find('#dayMonth').val();
                var dayWeekValue = self._cronGenerator.find('#dayWeek').val();
                var statusDayOfMonth = validateDayOfMonthValue(dayMonValue, dayWeekValue);
                if(statusDayOfMonth.status){
                     self._cronGenerator.find('#dayMonth').removeClass("error-field");
                     self.buildCronExpression();
                } else {
                     self._cronGenerator.find('#dayMonth').addClass("error-field");;
                }
                self.listOfErrorMessage();
                self.isErrorOccurred();
            },250));
            self._cronGenerator.find("#month").on("input", debounce(function(){
                var monValue = self._cronGenerator.find('#month').val();
                var statusMonth = validateMonthValue(monValue);
                if(statusMonth.status){
                     self._cronGenerator.find('#month').removeClass("error-field");
                     self.buildCronExpression();
                } else {
                     self._cronGenerator.find('#month').addClass("error-field");
                }
                self.listOfErrorMessage();
                self.isErrorOccurred();
            },250));
            self._cronGenerator.find("#dayWeek").on("input", debounce(function(){
                 var weekValue = self._cronGenerator.find('#dayWeek').val();
                 var dayMonValue = self._cronGenerator.find('#dayMonth').val();
                 var statusDayOfWeek = validateDayOfWeekValue(weekValue, dayMonValue);
                 if(statusDayOfWeek.status){
                      self._cronGenerator.find('#dayWeek').removeClass("error-field");
                      self.buildCronExpression();
                 } else {
                      self._cronGenerator.find('#dayWeek').addClass("error-field");;
                 }
                 self.listOfErrorMessage();
                 self.isErrorOccurred();
            },250));
            self._cronGenerator.find("#expression").on("input",debounce(function(){
                 var expression = self._cronGenerator.find("#expression").val();
                 var description = Cronstrue.toString(expression, { throwExceptionOnParseError:false,
                                                                        dayOfWeekStartIndexZero: false });
                 var splitValues = expression.split(" ");
                 var second = splitValues[0];
                 var minute = splitValues[1];
                 var hour = splitValues[2];
                 var dayMonth = splitValues[3];
                 var month = splitValues[4];
                 var dayWeek = splitValues[5];
                 var statusExpression = validateCronExpression(expression);
                 if(statusExpression.status){
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
                 if(validateTimeValue(second, constants.MAX_MINUTE_AND_SECOND_VALUE).status){
                    self._cronGenerator.find('#second').removeClass("error-field");
                    self._cronGenerator.find("#second").val(splitValues[0]);
                 } else {
                    self._cronGenerator.find('#second').addClass("error-field");
                 }
                 if(validateTimeValue(minute, constants.MAX_MINUTE_AND_SECOND_VALUE).status){
                    self._cronGenerator.find('#minute').removeClass("error-field");
                    self._cronGenerator.find("#minute").val(splitValues[1]);
                 } else {
                    self._cronGenerator.find('#minute').addClass("error-field");
                 }
                 if(validateTimeValue(hour, constants.MAX_HOUR_VALUE).status){
                     self._cronGenerator.find('#hour').removeClass("error-field");
                     self._cronGenerator.find("#hour").val(splitValues[2]);
                 } else {
                     self._cronGenerator.find('#hour').addClass("error-field");
                 }
                 if(validateDayOfMonthValue(dayMonth, dayWeek).status){
                     self._cronGenerator.find('#dayMonth').removeClass("error-field");
                     self._cronGenerator.find("#dayMonth").val(splitValues[3]);
                 } else {
                     self._cronGenerator.find('#dayMonth').addClass("error-field");
                 }
                 if(validateMonthValue(month).status){
                     self._cronGenerator.find('#month').removeClass("error-field");
                     self._cronGenerator.find("#month").val(splitValues[4]);
                 } else {
                     self._cronGenerator.find('#month').addClass("error-field");
                 }
                 if(validateDayOfWeekValue(dayWeek, dayMonth).status){
                     self._cronGenerator.find('#dayWeek').removeClass("error-field");
                     self._cronGenerator.find("#dayWeek").val(splitValues[5]);
                 } else {
                     self._cronGenerator.find('#dayWeek').addClass("error-field");
                 }
            },250));
        };

        CronGenerator.prototype.buildCronExpression = function(){
            var self = this;
            self._cronGenerator.find("#expression").val(self._cronGenerator.find("#second").val() + " " +
                    self._cronGenerator.find("#minute").val()+ " " +self._cronGenerator.find("#hour").val() + " " +
                    self._cronGenerator.find("#dayMonth").val()+ " " +self._cronGenerator.find("#month").val() + " "
                    + self._cronGenerator.find("#dayWeek").val());
            var expression = self._cronGenerator.find("#expression").val();
            var description = Cronstrue.toString(expression, { throwExceptionOnParseError: false,
                                                                    dayOfWeekStartIndexZero: false });
            self._cronGenerator.find("#output").text(description);
        };

        CronGenerator.prototype.isErrorOccurred = function(){
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
        CronGenerator.prototype.listOfErrorMessage = function(){
            var self = this;
            self._cronGenerator.find("#expression").val(self._cronGenerator.find("#second").val() + " " +
                    self._cronGenerator.find("#minute").val()+ " " +self._cronGenerator.find("#hour").val() + " " +
                    self._cronGenerator.find("#dayMonth").val()+ " " +self._cronGenerator.find("#month").val() + " "
                    + self._cronGenerator.find("#dayWeek").val());
            var expression = self._cronGenerator.find("#expression").val();
            var list = self._cronGenerator.find('#errorMsg');
            self._cronGenerator.find('#errorMsg').empty();
            var statusExpression = validateCronExpression(expression);
            var error = "";
            if(!statusExpression.status){
                var errorMsg = statusExpression.error;
                for(var x in errorMsg){
                    if(errorMsg[x] !== undefined){
                        errorList = '<li>' + errorMsg[x] + '</li>';
                        list.append(errorList);
                    }
                }
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

        function debounce(func, wait, immediate) {
        	var timeout;
        	return function() {
        		var context = this, args = arguments;
        		var later = function() {
        			timeout = null;
        			if (!immediate) func.apply(context, args);
        		};
        		var callNow = immediate && !timeout;
        		clearTimeout(timeout);
        		timeout = setTimeout(later, wait);
        		if (callNow) func.apply(context, args);
        	};
        };

        return CronGenerator;
    });