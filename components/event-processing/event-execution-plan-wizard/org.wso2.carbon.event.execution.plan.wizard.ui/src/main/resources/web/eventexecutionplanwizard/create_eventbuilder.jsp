<%@ page import="org.wso2.carbon.event.builder.stub.EventBuilderAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.builder.stub.types.InputEventAdaptorInfoDto" %>
<%--
~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~  WSO2 Inc. licenses this file to you under the Apache License,
~  Version 2.0 (the "License"); you may not use this file except
~  in compliance with the License.
~  You may obtain a copy of the License at
~
~     http://www.apache.org/licenses/LICENSE-2.0
~
~  Unless required by applicable law or agreed to in writing,
~  software distributed under the License is distributed on an
~  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~  KIND, either express or implied.  See the License for the
~  specific language governing permissions and limitations
~  under the License.
--%>

<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.execution.plan.wizard.ui.i18n.Resources">


<link type="text/css" href="../eventexecutionplanwizard/css/cep.css" rel="stylesheet"/>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../eventbuilder/js/event_builders.js"></script>
<script type="text/javascript" src="../eventbuilder/js/create_eventBuilder_helper.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>


<script type="text/javascript">
function addCEPEventBuilder(form) {

    var isFieldEmpty = false;

    var eventBuilderName = document.getElementById("eventBuilderNameId").value.trim();
    var toStreamName = document.getElementById("toStreamName").value.trim();
    var toStreamVersion = document.getElementById("toStreamVersion").value.trim();
    var eventAdaptorInfo = document.getElementById("eventAdaptorNameSelect")[document.getElementById("eventAdaptorNameSelect").selectedIndex].value;


    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(eventBuilderName)) {
        CARBON.showErrorDialog("White spaces are not allowed in event builder name.");
        return;
    }
    if (isFieldEmpty || (eventBuilderName == "")) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    }

    var propertyCount = 0;
    var msgConfigPropertyString = "";

    // all properties, not required and required are checked
    while (document.getElementById("msgConfigProperty_Required_" + propertyCount) != null ||
           document.getElementById("msgConfigProperty_" + propertyCount) != null) {
        // if required fields are empty
        if (document.getElementById("msgConfigProperty_Required_" + propertyCount) != null) {
            if (document.getElementById("msgConfigProperty_Required_" + propertyCount).value.trim() == "") {
                // values are empty in fields
                isFieldEmpty = true;
                msgConfigPropertyString = "";
                break;
            }
            else {
                // values are stored in parameter string to send to backend
                var propertyValue = document.getElementById("msgConfigProperty_Required_" + propertyCount).value.trim();
                var propertyName = document.getElementById("msgConfigProperty_Required_" + propertyCount).name;
                msgConfigPropertyString = msgConfigPropertyString + propertyName + "$=" + propertyValue + "|=";

            }
        } else if (document.getElementById("msgConfigProperty_" + propertyCount) != null) {
            var notRequiredPropertyName = document.getElementById("msgConfigProperty_" + propertyCount).name;
            var notRequiredPropertyValue = document.getElementById("msgConfigProperty_" + propertyCount).value.trim();
            if (notRequiredPropertyValue == "") {
                notRequiredPropertyValue = "  ";
            }
            msgConfigPropertyString = msgConfigPropertyString + notRequiredPropertyName + "$=" + notRequiredPropertyValue + "|=";
        }
        propertyCount++;
    }

    if (isFieldEmpty) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    } else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'wso2event') {

        var mappingType = "wso2event";
        var metaData = "";
        var correlationData = "";
        var payloadData = "";
        var customMappingValue = "disable";
        var checkedRB = document.querySelectorAll('input[type="radio"][name="customMapping"]:checked');
        if(checkedRB.length != 0) {
            customMappingValue = checkedRB[0].value;
        }

        if(customMappingValue == "enable") {
            var wso2EventDataTable = document.getElementById("inputWso2EventDataTable");
            if (wso2EventDataTable.rows.length > 1) {
                metaData = getWso2EventDataValues(wso2EventDataTable, 'meta');
                correlationData = getWso2EventDataValues(wso2EventDataTable, 'correlation');
                payloadData = getWso2EventDataValues(wso2EventDataTable, 'payload');
            }
        }
        if (metaData == "" && correlationData == "" && payloadData == "" && customMappingValue == "enable") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventBuilderName: eventBuilderName, toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion, eventAdaptorInfo: eventAdaptorInfo, mappingType: mappingType,
                    msgConfigPropertySet: msgConfigPropertyString, customMappingValue: customMappingValue,
                    metaData: metaData, correlationData: correlationData, payloadData: payloadData},
                onSuccess: function (response) {
                    if ("true" == response.responseText.trim()) {
                        form.submit();
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + response.responseText.trim());
                    }
                }
            })
        }
    }

    else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'text') {

        var mappingType = "text";
        var textData = "";

        var textDataTable = document.getElementById("inputTextMappingTable");
        if (textDataTable.rows.length > 1) {
            textData = getTextDataValues(textDataTable);
        }

        if (textData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventBuilderName: eventBuilderName, toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion, eventAdaptorInfo: eventAdaptorInfo, mappingType: mappingType, msgConfigPropertySet: msgConfigPropertyString,
                    textData: textData},
                onSuccess: function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showConfirmationDialog(
                                "Event Builder " + eventBuilderName + " successfully added, Do you want to add another Event Builder?", function () {
                                    loadUIElements('builder')
                                }, function () {
                                    loadUIElements('processor')
                                });

                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + response.responseText.trim());

                    }
                }
            })
        }
    }

    else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'xml') {
        var parentSelectorXpath = document.getElementById("parentSelectorXpath").checked;

        var mappingType = "xml";
        var prefixData = "";
        var xpathData = "";

        var xpathPrefixTable = document.getElementById("inputXpathPrefixTable");
        if (xpathPrefixTable.rows.length > 1) {
            prefixData = getXpathPrefixValues(xpathPrefixTable);
        }

        var xpathExprTable = document.getElementById("inputXpathExprTable");
        if (xpathExprTable.rows.length > 1) {
            xpathData = getXpathDataValues(xpathExprTable);
        }

        if (prefixData == "" && xpathData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventBuilderName: eventBuilderName, toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion, eventAdaptorInfo: eventAdaptorInfo, mappingType: mappingType, msgConfigPropertySet: msgConfigPropertyString,
                    parentSelectorXpath: parentSelectorXpath, prefixData: prefixData, xpathData: xpathData},
                onSuccess: function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showConfirmationDialog(
                                "Event Builder " + eventBuilderName + " successfully added, Do you want to add another Event Builder?", function () {
                                    loadUIElements('builder')
                                }, function () {
                                    loadUIElements('processor')
                                });

                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + response.responseText.trim());

                    }
                }
            });
        }
    }
    else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'map') {

        var mappingType = "map";
        var mapData = "";

        var mapDataTable = document.getElementById("inputMapPropertiesTable");
        if (mapDataTable.rows.length > 1) {
            mapData = getMapDataValues(mapDataTable);
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventBuilderName: eventBuilderName, toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion, eventAdaptorInfo: eventAdaptorInfo, mappingType: mappingType, msgConfigPropertySet: msgConfigPropertyString,
                    mapData: mapData},
                onSuccess: function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showConfirmationDialog(
                                "Event Builder " + eventBuilderName + " successfully added, Do you want to add another Event Builder?", function () {
                                    loadUIElements('builder')
                                }, function () {
                                    loadUIElements('processor')
                                });

                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + response.responseText.trim());

                    }
                }
            });
        } else {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        }
    }
    else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'json') {

        var mappingType = "json";
        var jsonData = document.getElementById("jsonSourceText").value;

        if (jsonData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventBuilderName: eventBuilderName, toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion, eventAdaptorInfo: eventAdaptorInfo, mappingType: mappingType, msgConfigPropertySet: msgConfigPropertyString,
                    jsonData: jsonData},
                onSuccess: function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showConfirmationDialog(
                                "Event Builder " + eventBuilderName + " successfully added, Do you want to add another Event Builder?", function () {
                                    loadUIElements('builder')
                                }, function () {
                                    loadUIElements('processor')
                                });

                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + response.responseText.trim());

                    }
                }
            });
        }
    }


}
</script>

<div id="middle">
    <h2><fmt:message key="event.builder.create"/></h2>
    <%--<h6><fmt:message key="title.event.builder.create"/></h6>--%>
    <%
        EventBuilderAdminServiceStub eventBuilderStub = EventExecutionPlanWizardUiUtils.getEventBuilderAdminService(config, session, request);
        InputEventAdaptorInfoDto[] inputEventAdaptorInfoDtos = eventBuilderStub.getInputEventAdaptorInfo();
        if (inputEventAdaptorInfoDtos != null && inputEventAdaptorInfoDtos.length > 0) {
    %>
    <div id="workArea">
        <form name="inputForm" method="get" id="addEventBuilder">

            <table style="width:100%" id="ebAdd" class="styledLeft">

                <thead>
                <tr>
                    <th><fmt:message key="event.builder.create.header"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRaw">
                        <%@include file="../eventbuilder/inner_eventbuilder_ui.jsp" %>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" class="buttonRow">
                        <input type="button" value="<fmt:message key="save"/>"
                               onclick="addCEPEventBuilder(document.getElementById('addEventBuilder'))"/>
                        <input type="button" value="<fmt:message key="skip"/>"
                               onclick="loadUIElements('processor')"/>
                    </td>

                </tr>
                </tbody>
            </table>
        </form>
    </div>

    <%
    } else {
    %>
    <div id="workArea">
        <table style="width:100%" id="ebNoAdd" class="styledLeft">
            <thead>
            <tr>
                <th><fmt:message key="event.builder.notransport.header"/></th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td class="formRaw">
                    <table id="noEventBuilderInputTable" class="normal-nopadding"
                           style="width:100%">
                        <tbody>

                        <tr>
                            <td width="80%">No Input Event Adaptors
                                            available
                            </td>
                            <td width="20%"><a onclick="loadUIElements('inputAdaptor')"
                                               style="background-image:url(images/add.gif);"
                                               class="icon-link" href="#">
                                Add New Input Event Adaptor
                            </a>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
    <%
        }
    %>
</div>

</fmt:bundle>
