<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.analytics.hive.ui.client.HiveScriptStoreClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.DateFormatSymbols" %>
<%@ page import="java.util.Calendar" %>
<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.analytics.hive.ui.i18n.Resources">
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<%--<script type="text/javascript">--%>
    <%--window.onload = function() {--%>
        <%--customCronEnable();--%>
    <%--};--%>
<%--</script>--%>

<%
    String scriptName = request.getParameter("scriptName");
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    HiveScriptStoreClient client = new HiveScriptStoreClient(cookie, serverURL, configContext);
    String cron = client.getCronExpression(scriptName);
%>
<script type="text/javascript">
    var cronExpSelected = '';
    var scriptName = '<%=scriptName%>';

    function saveCron() {
        if (document.getElementById('selectUI').checked) {
            var optionCron = 'selectUI';
            var year = document.getElementById('year').value;
            var month = document.getElementById('month').value;
            var day_month = document.getElementById('dayMonth').value;
            var day_week = document.getElementById('dayWeek').value;
            var selectDay = '';
            if (document.getElementById('selectDayMonth').checked) {
                selectDay = 'selectDayMonth';
            } else {
                selectDay = 'selectDayWeek';
            }
            var hour = document.getElementById('hours').value;
            var minutes = document.getElementById('minutes').value;

            new Ajax.Request('../hive-explorer/SaveCronExpression', {
                        method: 'post',
                        parameters: {yearSelected:year, monthSelected:month,
                            dayMonthSelected:day_month, dayWeekSelected:day_week,
                            selectDay:selectDay,hoursSelected: hour, minutesSelected:minutes,
                            optionCron:optionCron},
                        onSuccess: function(transport) {
                            var result = transport.responseText;
                            var array = result.split('#');
                            var message = array[0];
                            if (message.indexOf("Success") != -1) {
                                cronExpSelected = array[2];
                                sendRequestToSaveScript(cronExpSelected);
                            } else {
                                CARBON.showErrorDialog(message);
                            }
                        },
                        onFailure: function(transport) {
                            CARBON.showErrorDialog(transport.responseText);
                        }
                    });
        } else if (document.getElementById('cronExpSelect').checked) {
            optionCron = "customCron";
            var customCronVal = document.getElementById('cronExpression').value;
            if (customCronVal == '') {
                CARBON.showErrorDialog('Please enter a cron expression in the text field');
            } else {
                new Ajax.Request('../hive-explorer/SaveCronExpression', {
                            method: 'post',
                            parameters: {optionCron:optionCron,
                                customCron:customCronVal},
                            onSuccess: function(transport) {
                                var message = transport.responseText;
                                var array = message.split('#');
                                cronExpSelected = array[2];
                                sendRequestToSaveScript(cronExpSelected);

                                if (message.contains("Success")) {


                                } else {
                                    CARBON.showErrorDialog(message);
                                }
                            },
                            onFailure: function(transport) {
                                CARBON.showErrorDialog(transport.responseText);
                            }
                        });
            }
        } else if (document.getElementById('noSchedule').checked) {
            sendRequestToSaveScript(cronExpSelected);
        } else {
            //when interval -count option is selected..
        }
        return true;
    }


    function cancelConnect() {
        location.href = "../hive-explorer/listscripts.jsp";
    }

    function customCronEnable() {
        disableCustomCron(false);
        disableUI(true);
//        disableIntervalSelection(true);
    }

    function simpleUI() {
        disableUI(false);
        disableCustomCron(true);
//        disableIntervalSelection(true);
    }

    function disableUI(value) {
        document.getElementById('year').disabled = value;
        document.getElementById('month').disabled = value;
        document.getElementById('hours').disabled = value;
        document.getElementById('minutes').disabled = value;
        document.getElementById('selectDayMonth').disabled = value;
        document.getElementById('selectDayWeek').disabled = value;
        disableDaySelection(value);
    }

    function disableDaySelection(value) {
        if (!value) {//if enable
            var monthChecked = document.getElementById('selectDayMonth').checked;
            var weekChecked = document.getElementById('selectDayWeek').checked;
            if (monthChecked) {
                dayMonthSelection();
            } else if (weekChecked) {
                dayWeekSelection();
            } else {
                document.getElementById('selectDayMonth').checked = true;
                dayMonthSelection();
            }
        }
        else {
            document.getElementById('dayMonth').disabled = value;
            document.getElementById('dayWeek').disabled = value;
        }
    }

    function disableCustomCron(value) {
        document.getElementById('cronExpression').disabled = value;
    }

    function dayWeekSelection() {
        disableSelectDayWeek(false);
        disableSelectDayMonth(true);
    }

    function disableSelectDayWeek(value) {
        document.getElementById('dayWeek').disabled = value;
    }

    function disableSelectDayMonth(value) {
        document.getElementById('dayMonth').disabled = value;
    }


    function dayMonthSelection() {
        disableSelectDayMonth(false);
        disableSelectDayWeek(true);
    }

    function intervalSelection() {
        disableCustomCron(true);
        disableUI(true);
//        disableIntervalSelection(false);
    }

    function unscheduleSelection() {
        disableCustomCron(true);
        disableUI(true);
    }


    function cancelCron() {
        history.go(-1);
    }

    function sendRequestToSaveScript(cron) {
        new Ajax.Request('../hive-explorer/SaveScriptProcessor', {
                    method: 'post',
                    parameters: {scriptName:scriptName,
                        cronExp:cron},
                    onSuccess: function(transport) {
                        var result = transport.responseText;
                        if (result.indexOf('Success') != -1) {
                            CARBON.showInfoDialog(result, function() {
                                location.href = "../hive-explorer/listscripts.jsp";
                            }, function() {
                                location.href = "../hive-explorer/listscripts.jsp";
                            });

                        } else {
                            CARBON.showErrorDialog(result);
                        }
                    },
                    onFailure: function(transport) {
                        CARBON.showErrorDialog(result);
                    }
                });
    }

</script>

<style type="text/css">
    table.day {
        border-width: 1px;
        border-style: solid;
        border-color: white;
        background-color: white;
        width: 100%;
    }

</style>


<div id="middle">
<h2>Schedule Script Execution</h2>

<div id="workArea">

<form id="cronForm" name="cronForm" action="" method="POST">
<table class="styledLeft">
<thead>
<tr>
    <th>
        <input TYPE=RADIO NAME="cronExpSelect" id="cronExpSelect" VALUE="cronExpSelect"
               checked="true" onclick="customCronEnable();" class="selectedOption"><label>Schedule
        By Cron Expression: </label>
    </th>
</tr>
</thead>
<tbody>
<tr>
    <td>
        <table class="normal-nopadding">
            <tbody>
            <tr>
                <td width="250px"><fmt:message key="cron.expression"/>
                    <span class="required">*</span></td>
                <td><input name="cronExpression"
                           id="cronExpression"
                        <%
                            if (cron != null && !cron.equals("")) {
                        %>
                           value='<%=cron%>'
                        <%
                        } else {
                        %>
                           value="<fmt:message key="default.cron.expression"/>"
                        <%
                            }
                        %>
                           size="60"/>
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
</tbody>
<thead>
<tr>
    <th>
        <input TYPE=RADIO NAME="cronExpSelect" id="selectUI" VALUE="selectUI"
               onclick="simpleUI()" class="selectedOption"><label>Simple
        Scheduling:</label>
    </th>
</tr>
</thead>
<tbody>
<tr>
    <td>
        <table class="normal-nopadding" id='allSimpleConf'>
            <tbody>
            <tr>
                <td width="250px"><fmt:message key="year"/></td>
                <td>
                    <select name="year" id="year">
                        <%
                            int year = Calendar.getInstance().get(Calendar.YEAR);
                        %>
                        <option value="All">Every Year</option>
                        <%
                            for (int i = year; i <= 2099; i++) {
                        %>
                        <option value="<%=i%>"><%=i%>
                        </option>
                        <%
                            }
                        %>
                    </select>

                </td>
            </tr>
            <tr>
                <td width="250px"><fmt:message key="month"/></td>
                <td>
                    <select name="month" id="month">
                        <%
                            String[] months = new DateFormatSymbols().getMonths();
                        %>
                        <option value="All">Every Month</option>
                        <%
                            for (int i = 0; i < 12; i++) {
                        %>
                        <option value="<%=i+1%>"><%=months[i]%>
                        </option>
                        <%
                            }
                        %>
                    </select>

                </td>
            </tr>
            <tr>
                <td>
                    <input TYPE=RADIO NAME="selectDay" id="selectDayMonth"
                           VALUE="selectDayMonth"
                           checked="true" onclick="dayMonthSelection()"><label>Use
                    day of month for scheduling </label><br>
                </td>

                <td>
                    <select name="dayMonth" id="dayMonth">
                        <option value="All">Every Days of a Month</option>

                        <%
                            for (int i = 1; i <= 31; i++) {
                        %>
                        <option value="<%=i%>">Day - <%=i%> of Selected Month
                        </option>
                        <%
                            }
                        %>

                    </select>

                </td>
            </tr>
            <tr>
                <td>
                    <input TYPE=RADIO NAME="selectDay" id="selectDayWeek"
                           VALUE="selectDayWeek"
                           onclick="dayWeekSelection();"><label>Use
                    day of week for scheduling</label><br>

                </td>
                <td>
                    <select name="dayWeek" id="dayWeek" onclick="">
                        <%
                            String[] weekdays = new DateFormatSymbols().getWeekdays();
                        %>                 scriptName != null && !scriptName.equals("")
                        <option value="All">Every Days of a week</option>
                        <%
                            for (int i = 1; i <= 7; i++) {
                        %>

                        <option value="<%=i%>"><%=weekdays[i]%>
                        </option>
                        <%
                            }
                        %>
                    </select>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="hours"></fmt:message></td>
                <td>
                    <select name="hours" id="hours">
                        <option value="All">Every Hours</option>
                        <%
                            for (int i = 0; i <= 23; i++) {
                        %>
                        <option value="<%=i%>"><%=i%>
                        </option>
                        <%
                            }
                        %>
                    </select>

                </td>
            </tr>
            <tr>
                <td width="250px"><fmt:message key="minutes"/></td>
                <td>
                    <select name="minutes" id="minutes">
                        <option value="All">Every Minutes</option>
                        <%
                            for (int i = 0; i <= 59; i++) {
                        %>
                        <option value="<%=i%>"><%=i%>
                        </option>
                        <%
                            }
                        %>
                    </select>

                </td>
            </tr>

                <%--<tr>--%>
                <%--<td width="250px"><fmt:message key="seconds"/></td>--%>
                <%--<td>--%>
                <%--<select name="seconds" id="seconds">--%>
                <%--<%--%>
                <%--for (int i = 0; i <= 59; i++) {--%>
                <%--%>--%>
                <%--<option value="<%=i%>"><%=i%>--%>
                <%--</option>--%>
                <%--<%--%>
                <%--}--%>
                <%--%>--%>
                <%--</select>--%>

                <%--</td>--%>
                <%--</tr>--%>
            </tbody>
        </table>
    </td>
</tr>
</tbody>
    <%--<thead>--%>
    <%--<tr>--%>
    <%--<th>--%>
    <%--<input TYPE=RADIO NAME="cronExpSelect" id="intervalSelect" VALUE="selectInterval"--%>
    <%--onclick="intervalSelection()"><label>Schedule by Interval:</label>--%>
    <%--</th>--%>
    <%--</tr>--%>
    <%--</thead>--%>
    <%--<tbody>--%>
    <%--<tr>--%>
    <%--<td>--%>
    <%--<table class="normal-nopadding" id='intervalSchedule'>--%>
    <%--<tbody>--%>
    <%--<tr>--%>
    <%--<td width="250px"><fmt:message key="interval"/></td>--%>
    <%--<td>--%>
    <%--<input name="interval"--%>
    <%--id="interval"--%>
    <%--size="60"/>--%>

    <%--</td>--%>
    <%--</tr>--%>
    <%--<tr>--%>
    <%--<td width="250px"><fmt:message key="count"/></td>--%>
    <%--<td>--%>
    <%--<input name="count"--%>
    <%--id="count"--%>
    <%--size="60"/>--%>

    <%--</td>--%>
    <%--</tr>--%>


    <%--</tbody>--%>

    <%--</table>--%>
    <%--</td>--%>
    <%--</tr>--%>
    <%--</tbody>--%>


<thead>
<tr>
    <th>
        <input TYPE=RADIO NAME="cronExpSelect" id="noSchedule" VALUE="noSchedule" onclick="unscheduleSelection();"><label>Unschedule</label>
    </th>
</tr>
</thead>

<tbody>

<tr>

    <td>
        <table class="normal-nopadding">
            <tbody>
            <tr>
                <td width="250px"></td>
                <td>

                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>


<tr>
    <td>
        <input class="button" type="button" value="Save" onclick="saveCron()"/>
        <input class="button" type="button" value="Cancel" onclick="cancelCron()"/>
    </td>
</tr>
<input type="hidden" name="scriptContent" id="scriptContent"/>
<inut type="hidden" name="cron" id="cron"/>
<input type="hidden" name="scriptName" id="scriptName"/>
<input type="hidden" name="mode" id="mode"/>
</tbody>
</table>

</form>
</div>
</div>

<script type="text/javascript">
    <%
    if(null != cron && !cron.isEmpty()){
    %>
    customCronEnable();
    <%
    }else {
    %>
    document.getElementById('noSchedule').checked = 'true';
    unscheduleSelection();
    <%
    }
    %>

</script>



</fmt:bundle>
