<%@ page import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.formatter.stub.types.EventFormatterPropertyDto" %>
<%@ page import="org.wso2.carbon.event.formatter.stub.types.OutputEventAdaptorInfoDto" %>
<%@ page import="org.wso2.carbon.event.formatter.ui.EventFormatterUIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.formatter.ui.i18n.Resources">
<script type="text/javascript" src="../eventformatter/js/event_formatter.js"></script>
<script type="text/javascript" src="../eventformatter/js/registry-browser.js"></script>

<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript"
        src="../eventformatter/js/create_eventFormatter_helper.js"></script>

<script type="text/javascript">
jQuery(document).ready(function () {
    showMappingContext();
});

function addEventFormatterViaPopup(form) {

    var isFieldEmpty = false;
    var payloadString = "";
    var correlationString = "";
    var metaString = "";
    var inline = "inline";
    var registry = "registry";
    var dataFrom = "";

    var eventFormatterName = document.getElementById("eventFormatterId").value.trim();
    var streamNameWithVersion = document.getElementById("streamNameFilter")[document.getElementById("streamNameFilter").selectedIndex].text;
    var eventAdaptorInfo = document.getElementById("eventAdaptorNameFilter")[document.getElementById("eventAdaptorNameFilter").selectedIndex].value;


    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(eventFormatterName)) {
        CARBON.showErrorDialog("White spaces are not allowed in event formatter name.");
        return;
    }
    if (isFieldEmpty || (eventFormatterName == "")) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    }


    var propertyCount = 0;
    var outputPropertyParameterString = "";

    // all properties, not required and required are checked
    while (document.getElementById("property_Required_" + propertyCount) != null ||
            document.getElementById("property_" + propertyCount) != null) {
        // if required fields are empty
        if (document.getElementById("property_Required_" + propertyCount) != null) {
            if (document.getElementById("property_Required_" + propertyCount).value.trim() == "") {
                // values are empty in fields
                isFieldEmpty = true;
                outputPropertyParameterString = "";
                break;
            }
            else {
                // values are stored in parameter string to send to backend
                var propertyValue = document.getElementById("property_Required_" + propertyCount).value.trim();
                var propertyName = document.getElementById("property_Required_" + propertyCount).name;
                outputPropertyParameterString = outputPropertyParameterString + propertyName + "$=" + propertyValue + "|=";

            }
        } else if (document.getElementById("property_" + propertyCount) != null) {
            var notRequriedPropertyValue = document.getElementById("property_" + propertyCount).value.trim();
            var notRequiredPropertyName = document.getElementById("property_" + propertyCount).name;
            if (notRequriedPropertyValue == "") {
                notRequriedPropertyValue = "  ";
            }
            outputPropertyParameterString = outputPropertyParameterString + notRequiredPropertyName + "$=" + notRequriedPropertyValue + "|=";


        }
        propertyCount++;
    }

    if (isFieldEmpty) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    }

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'wso2event') {

        var metaData = "";
        var correlationData = "";
        var payloadData = "";

        var metaDataTable = document.getElementById("outputMetaDataTable");
        if (metaDataTable.rows.length > 1) {
            metaData = getWSO2EventDataValues(metaDataTable);
        }
        var correlationDataTable = document.getElementById("outputCorrelationDataTable");
        if (correlationDataTable.rows.length > 1) {
            correlationData = getWSO2EventDataValues(correlationDataTable);
        }
        var payloadDataTable = document.getElementById("outputPayloadDataTable");
        if (payloadDataTable.rows.length > 1) {
            payloadData = getWSO2EventDataValues(payloadDataTable);
        }

        if (metaData == "" && correlationData == "" && payloadData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventformatter/add_event_formatter_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventFormatter: eventFormatterName, streamNameWithVersion: streamNameWithVersion,
                    eventAdaptorInfo: eventAdaptorInfo, mappingType: "wso2event", outputParameters: outputPropertyParameterString,
                    metaData: metaData, correlationData: correlationData, payloadData: payloadData, customMappingValue: "enable"},
                onSuccess: function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showInfoDialog("Event formatter added successfully!!");
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event formatter, Exception: " + response.responseText.trim());
                    }
                }
            })

        }
    }

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'text') {

        var textData = "";
        if ((document.getElementById("inline_text")).checked) {
            textData = document.getElementById("textSourceText").value;
            dataFrom = inline;
        }
        else if ((document.getElementById("registry_text")).checked) {
            textData = document.getElementById("textSourceRegistry").value;
            dataFrom = registry;
        }

        if (textData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventformatter/add_event_formatter_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventFormatter: eventFormatterName, streamNameWithVersion: streamNameWithVersion,
                    eventAdaptorInfo: eventAdaptorInfo, mappingType: "text", outputParameters: outputPropertyParameterString,
                    textData: textData, dataFrom: dataFrom, customMappingValue: "enable"},
                onSuccess: function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showInfoDialog("Event formatter added successfully!!");
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event formatter, Exception: " + response.responseText.trim());
                    }
                }
            })

        }

    }

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'xml') {

        var textData = "";
        if ((document.getElementById("inline_xml")).checked) {
            textData = document.getElementById("xmlSourceText").value;
            dataFrom = inline;
        }
        else if ((document.getElementById("registry_xml")).checked) {
            textData = document.getElementById("xmlSourceRegistry").value;
            dataFrom = registry;
        }

        if (textData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventformatter/add_event_formatter_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventFormatter: eventFormatterName, streamNameWithVersion: streamNameWithVersion,
                    eventAdaptorInfo: eventAdaptorInfo, mappingType: "xml", outputParameters: outputPropertyParameterString,
                    textData: textData, dataFrom: dataFrom, customMappingValue: "enable"},
                onSuccess: function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showInfoDialog("Event formatter added successfully!!");
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event formatter, Exception: " + response.responseText.trim());
                    }
                }
            })

        }

    }

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'map') {

        var mapData = "";

        var mapDataTable = document.getElementById("outputMapPropertiesTable");
        if (mapDataTable.rows.length > 1) {
            mapData = getMapDataValues(mapDataTable);
            new Ajax.Request('../eventformatter/add_event_formatter_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventFormatter: eventFormatterName, streamNameWithVersion: streamNameWithVersion,
                    eventAdaptorInfo: eventAdaptorInfo, mappingType: "map", outputParameters: outputPropertyParameterString,
                    mapData: mapData, customMappingValue: "enable"},
                onSuccess: function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showInfoDialog("Event formatter added successfully!!");
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event formatter, Exception: " + response.responseText.trim());
                    }
                }
            })


        } else {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        }
    }

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'json') {

        var jsonData = ""
        if ((document.getElementById("inline_json")).checked) {
            jsonData = document.getElementById("jsonSourceText").value;
            dataFrom = inline;
        }
        else if ((document.getElementById("registry_json")).checked) {
            jsonData = document.getElementById("jsonSourceRegistry").value;
            parameters = parameters + "&jsonData=" + jsonData;
            dataFrom = registry;
        }

        if (jsonData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventformatter/add_event_formatter_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventFormatter: eventFormatterName, streamNameWithVersion: streamNameWithVersion,
                    eventAdaptorInfo: eventAdaptorInfo, mappingType: "json", outputParameters: outputPropertyParameterString,
                    dataFrom: dataFrom, jsonData: jsonData, customMappingValue: "enable"},
                onSuccess: function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showInfoDialog("Event formatter added successfully!!");
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event formatter, Exception: " + response.responseText.trim());
                    }
                }
            })
        }
    }
}

</script>

<div id="middle">
<h2><fmt:message key="title.event.formatter.create"/></h2>

<div id="workArea">

<form name="inputForm" action="#" method="get" id="addEventFormatter">
<table style="width:100%" id="eventFormatterAdd" class="styledLeft">
<%
    EventFormatterAdminServiceStub stub = EventFormatterUIUtils.getEventFormatterAdminService(config, session, request);
    String[] streamNames = stub.getAllEventStreamNames();
    OutputEventAdaptorInfoDto[] outputEventAdaptorInfoArray = stub.getOutputEventAdaptorInfo();
    String firstEventStreamName = null;
    if (streamNames != null && outputEventAdaptorInfoArray != null) {
        String streamId = request.getParameter("streamId");
%>
<br/>
<thead>
<tr>
    <th><fmt:message key="title.event.formatter.details"/></th>
</tr>
</thead>
<tbody>
<tr>
<td class="formRaw">


<table id="eventFormatterInputTable" class="normal-nopadding"
       style="width:100%">
<tbody>

<tr>
    <td class="leftCol-med"><fmt:message key="event.formatter.name"/><span class="required">*</span>
    </td>
    <td><input type="text" name="eventFormatterName" id="eventFormatterId"
               class="initE"

               value=""
               style="width:75%"/>

        <div class="sectionHelp">
            <fmt:message key="event.formatter.name.help"/>
        </div>
    </td>
</tr>

<tr>
    <td colspan="2">
        <b><fmt:message key="from.heading"/></b>
    </td>
</tr>
<tr>
    <td><fmt:message key="event.stream.name"/><span class="required">*</span></td>
    <td><select name="streamNameFilter"
                onchange="showEventStreamDefinition()" id="streamNameFilter">
        <%
            if (streamId == null || (streamId.equals(""))) {
                firstEventStreamName = streamNames[0];
                for (String streamName : streamNames) {
        %>
        <option><%=streamName%>
        </option>
        <%
            }
        } else {
            firstEventStreamName = streamId;
        %>
        <option><%=streamId%>
        </option>
        <% } %>

    </select>

        <div class="sectionHelp">
            <fmt:message key="event.stream.name.help"/>
        </div>
    </td>

</tr>
<tr>
    <td>
        <fmt:message key="stream.attributes"/>
    </td>
    <td>
        <textArea class="expandedTextarea" id="streamDefinitionText" name="streamDefinitionText"
                  readonly="true"
                  cols="60"><%=stub.getStreamDefinition(firstEventStreamName)%>
        </textArea>

    </td>

</tr>

<tr>
    <td><fmt:message key="event.adaptor.name"/><span class="required">*</span></td>
    <td>
        <table>
            <td class="custom-noPadding" width="60%"><select name="eventAdaptorNameFilter"
                                                             onchange="loadEventAdaptorRelatedProperties('<fmt:message
                                                                     key="to.heading"/>')"
                                                             id="eventAdaptorNameFilter">
                <%
                    String firstEventAdaptorName = null;

                    if (outputEventAdaptorInfoArray != null) {
                        firstEventAdaptorName = outputEventAdaptorInfoArray[0].getEventAdaptorName();
                        for (OutputEventAdaptorInfoDto outputEventAdaptorInfoDto : outputEventAdaptorInfoArray) {
                %>
                <option value="<%=outputEventAdaptorInfoDto.getEventAdaptorName() + "$=" + outputEventAdaptorInfoDto.getEventAdaptorType()%>"><%=outputEventAdaptorInfoDto.getEventAdaptorName()%>
                </option>
                <%
                        }
                    }
                %>

            </select>

                <div class="sectionHelp">
                    <fmt:message key="event.adaptor.name.help"/>
                </div>
            </td>
            <td width="40%" id="addOutputEventAdaptorTD" class="custom-noPadding"></td>
        </table>
    </td>

</tr>

<tr>
    <td colspan="2">
        <b><fmt:message key="mapping.heading"/></b>
    </td>
</tr>

<tr>
    <td><fmt:message key="mapping.type"/><span class="required">*</span></td>
    <td><select name="mappingTypeFilter"
                onchange="showMappingContext()" id="mappingTypeFilter">
        <%
            String[] mappingTypes = stub.getSupportedMappingTypes(firstEventAdaptorName);


            if (mappingTypes != null) {
                for (String mappingType : mappingTypes) {
        %>
        <option><%=mappingType%>
        </option>
        <%
                }
            }
        %>

    </select>

        <div class="sectionHelp">
            <fmt:message key="mapping.type.help"/>
        </div>
    </td>

</tr>

<tr>
<td class="formRaw" colspan="2">
<div id="outerDiv">

<div id="innerDiv1" style="display:none">

    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputWSO2EventMapping">
            <td colspan="2" class="middle-header">
                <fmt:message key="wso2event.mapping"/>
            </td>
        </tr>
        <tr name="outputWSO2EventMapping">
            <td colspan="2">

                <h6><fmt:message key="property.data.type.meta"/></h6>
                <table class="styledLeft noBorders spacer-bot" id="outputMetaDataTable"
                       style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th><fmt:message key="actions"/></th>
                    </thead>
                </table>
                <div class="noDataDiv-plain" id="noOutputMetaData">
                    <fmt:message key="no.meta.defined.message"/>
                </div>
                <table id="addMetaData" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message key="property.name"/> :</td>
                        <td>
                            <input type="text" id="outputMetaDataPropName"/>
                        </td>
                        <td class="col-small"><fmt:message key="property.value.of"/> :
                        </td>
                        <td>
                            <input type="text" id="outputMetaDataPropValueOf"/>
                        </td>
                        <td><input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addOutputWSO2EventProperty('Meta')"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>


        <tr name="outputWSO2EventMapping">
            <td colspan="2">

                <h6><fmt:message key="property.data.type.correlation"/></h6>
                <table class="styledLeft noBorders spacer-bot"
                       id="outputCorrelationDataTable" style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th><fmt:message key="actions"/></th>
                    </thead>
                </table>
                <div class="noDataDiv-plain" id="noOutputCorrelationData">
                    <fmt:message key="no.correlation.defined.message"/>
                </div>
                <table id="addCorrelationData" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message key="property.name"/> :</td>
                        <td>
                            <input type="text" id="outputCorrelationDataPropName"/>
                        </td>
                        <td class="col-small"><fmt:message key="property.value.of"/> :
                        </td>
                        <td>
                            <input type="text" id="outputCorrelationDataPropValueOf"/>
                        </td>
                        <td><input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addOutputWSO2EventProperty('Correlation')"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        <tr name="outputWSO2EventMapping">
            <td colspan="2">

                <h6><fmt:message key="property.data.type.payload"/></h6>
                <table class="styledLeft noBorders spacer-bot"
                       id="outputPayloadDataTable" style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th><fmt:message key="actions"/></th>
                    </thead>
                </table>
                <div class="noDataDiv-plain" id="noOutputPayloadData">
                    <fmt:message key="no.payload.defined.message"/>
                </div>
                <table id="addPayloadData" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message key="property.name"/> :</td>
                        <td>
                            <input type="text" id="outputPayloadDataPropName"/>
                        </td>
                        <td class="col-small"><fmt:message key="property.value.of"/> :
                        </td>
                        <td>
                            <input type="text" id="outputPayloadDataPropValueOf"/>
                        </td>
                        <td><input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addOutputWSO2EventProperty('Payload')"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>

        </tbody>
    </table>
</div>


<div id="innerDiv2" style="display:none">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputTextMapping">
            <td colspan="3" class="middle-header">
                <fmt:message key="text.mapping"/>
            </td>
        </tr>
        <tr>
            <td class="leftCol-med" colspan="1"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_text" type="radio" checked="checked" value="content"
                       name="inline_text" onclick="enable_disable_Registry(this)">
                <label for="inline_text"><fmt:message key="inline.input"/></label>
                <input id="registry_text" type="radio" value="reg" name="registry_text"
                       onclick="enable_disable_Registry(this)">
                <label for="registry_text"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputTextMappingInline" id="outputTextMappingInline">
            <td colspan="3">
                <p>
                    <textarea id="textSourceText" name="textSourceText"
                              style="border:solid 1px rgb(204, 204, 204); width: 99%;
    height: 150px; margin-top: 5px;"
                              name="textSource" rows="30"></textarea>
                </p>
            </td>
        </tr>
        <tr name="outputTextMappingRegistry" style="display:none" id="outputTextMappingRegistry">
            <td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span
                    class="required">*</span></td>
            <td colspan="1"><input type="text" id="textSourceRegistry" disabled="disabled"
                                   class="initE"
                                   value=""
                                   style="width:100%"/></td>

            <td class="nopadding" style="border:none" colspan="1">
                <a href="#registryBrowserLink" class="registry-picker-icon-link"
                   style="padding-left:20px"
                   onclick="showRegistryBrowser('textSourceRegistry','/_system/config');"><fmt:message
                        key="conf.registry"/></a>
                <a href="#registryBrowserLink"
                   class="registry-picker-icon-link"
                   style="padding-left:20px"
                   onclick="showRegistryBrowser('textSourceRegistry', '/_system/governance');"><fmt:message
                        key="gov.registry"/></a>
            </td>
        </tr>
        </tbody>
    </table>
</div>

<div id="innerDiv3" style="display:none">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputXMLMapping">
            <td colspan="3" class="middle-header">
                <fmt:message key="xml.mapping"/>
            </td>
        </tr>
        <tr>
            <td class="leftCol-med" colspan="1"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_xml" type="radio" checked="checked" value="content"
                       name="inline_xml" onclick="enable_disable_Registry(this)">
                <label for="inline_xml"><fmt:message key="inline.input"/></label>
                <input id="registry_xml" type="radio" value="reg" name="registry_xml"
                       onclick="enable_disable_Registry(this)">
                <label for="registry_xml"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputXMLMappingInline" id="outputXMLMappingInline">
            <td colspan="3">
                <p>
                    <textarea id="xmlSourceText"
                              style="border:solid 1px rgb(204, 204, 204); width: 99%;
                                     height: 150px; margin-top: 5px;"
                              name="xmlSource" rows="30"></textarea>
                </p>
            </td>
        </tr>
        <tr name="outputXMLMappingRegistry" style="display:none" id="outputXMLMappingRegistry">
            <td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span
                    class="required">*</span></td>
            <td colspan="1">
                <input type="text" id="xmlSourceRegistry" disabled="disabled" class="initE" value=""
                       style="width:100%"/>
            </td>
            <td class="nopadding" style="border:none" colspan="1">
                <a href="#registryBrowserLink" class="registry-picker-icon-link"
                   style="padding-left:20px"
                   onclick="showRegistryBrowser('xmlSourceRegistry','/_system/config');"><fmt:message
                        key="conf.registry"/></a>
                <a href="#registryBrowserLink"
                   class="registry-picker-icon-link"
                   style="padding-left:20px"
                   onclick="showRegistryBrowser('xmlSourceRegistry', '/_system/governance');"><fmt:message
                        key="gov.registry"/></a>
            </td>
        </tr>
        </tbody>
    </table>
</div>


<div id="innerDiv4" style="display:none">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputMapMapping">
            <td colspan="2" class="middle-header">
                <fmt:message key="map.mapping"/>
            </td>
        </tr>
        <tr name="outputMapMapping">
            <td colspan="2">

                <table class="styledLeft noBorders spacer-bot" id="outputMapPropertiesTable"
                       style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th><fmt:message key="actions"/></th>
                    </thead>
                </table>
                <div class="noDataDiv-plain" id="noOutputMapProperties">
                    <fmt:message key="no.map.properties.defined"/>
                </div>
                <table id="addOutputMapProperties" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message key="property.name"/> :</td>
                        <td>
                            <input type="text" id="outputMapPropName"/>
                        </td>
                        <td class="col-small"><fmt:message key="property.value.of"/> :</td>
                        <td>
                            <input type="text" id="outputMapPropValueOf"/>
                        </td>
                        <td><input type="button" class="button" value="<fmt:message key="add"/>"
                                   onclick="addOutputMapProperty()"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>

        </tbody>
    </table>
</div>

<div id="innerDiv5" style="display:none">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputJSONMapping">
            <td colspan="3" class="middle-header">
                <fmt:message key="json.mapping"/>
            </td>
        </tr>
        <tr>
            <td class="leftCol-med" colspan="1"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_json" type="radio" checked="checked" value="content"
                       name="inline_json" onclick="enable_disable_Registry(this)">
                <label for="inline_json"><fmt:message key="inline.input"/></label>
                <input id="registry_json" type="radio" value="reg" name="registry_json"
                       onclick="enable_disable_Registry(this)">
                <label for="registry_json"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputJSONMappingInline" id="outputJSONMappingInline">
            <td colspan="3">
                <p>
                    <textarea id="jsonSourceText"
                              style="border:solid 1px rgb(204, 204, 204); width: 99%;
                                     height: 150px; margin-top: 5px;"
                              name="jsonSource" rows="30"></textarea>
                </p>
            </td>
        </tr>
        <tr name="outputJSONMappingRegistry" style="display:none" id="outputJSONMappingRegistry">
            <td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span
                    class="required">*</span></td>
            <td colspan="1">
                <input type="text" id="jsonSourceRegistry" disabled="disabled" class="initE"
                       value=""
                       style="width:100%"/>
            </td>
            <td class="nopadding" style="border:none" colspan="1">
                <a href="#registryBrowserLink" class="registry-picker-icon-link"
                   style="padding-left:20px"
                   onclick="showRegistryBrowser('jsonSourceRegistry','/_system/config');"><fmt:message
                        key="conf.registry"/></a>
                <a href="#registryBrowserLink"
                   class="registry-picker-icon-link"
                   style="padding-left:20px"
                   onclick="showRegistryBrowser('jsonSourceRegistry', '/_system/governance');"><fmt:message
                        key="gov.registry"/></a>
            </td>
        </tr>
        </tbody>
    </table>
</div>


</div>
</td>
</tr>

<%
    EventFormatterPropertyDto[] eventFormatterProperties = stub.getEventFormatterProperties(firstEventAdaptorName);
    if (eventFormatterProperties != null) {
%>
<tr>
    <td>
        <b><fmt:message key="to.heading"/></b>
    </td>
</tr>
<%
    for (int index = 0; index < eventFormatterProperties.length; index++) {
%>
<tr>


    <td class="leftCol-med"><%=eventFormatterProperties[index].getDisplayName()%>
        <%
            String propertyId = "property_";
            if (eventFormatterProperties[index].getRequired()) {
                propertyId = "property_Required_";

        %>
        <span class="required">*</span>
        <%
            }
        %>
    </td>
    <%
        String type = "text";
        if (eventFormatterProperties[index].getSecured()) {
            type = "password";
        }
    %>

    <td>
        <div class=outputFields>
            <%
                if (eventFormatterProperties[index].getOptions()[0] != null) {
            %>

            <select name="<%=eventFormatterProperties[index].getKey()%>"
                    id="<%=propertyId%><%=index%>">

                <%
                    for (String property : eventFormatterProperties[index].getOptions()) {
                        if (property.equals(eventFormatterProperties[index].getDefaultValue())) {
                %>
                <option selected="selected"><%=property%>
                </option>
                <% } else { %>
                <option><%=property%>
                </option>
                <% }
                } %>
            </select>

            <% } else { %>
            <input type="<%=type%>"
                   name="<%=eventFormatterProperties[index].getKey()%>"
                   id="<%=propertyId%><%=index%>" class="initE"
                   style="width:75%"
                   value="<%= (eventFormatterProperties[index].getDefaultValue()) != null ? eventFormatterProperties[index].getDefaultValue() : "" %>"
                    />

            <% }

                if (eventFormatterProperties[index].getHint() != null) { %>
            <div class="sectionHelp">
                <%=eventFormatterProperties[index].getHint()%>
            </div>
            <% } %>
        </div>
    </td>

</tr>
<%
        }
    }
%>
</tbody>
</table>
</td>
</tr>
<tr>
    <td class="buttonRow">
        <input type="button" value="<fmt:message key="add.event.formatter"/>"
               onclick="addEventFormatterViaPopup(document.getElementById('addEventFormatter'))"/>
    </td>
</tr>
</tbody>
<% } else { %>
<tbody>
<tr>
    <td class="formRaw">
        <table id="noEventBuilderInputTable" class="normal-nopadding"
               style="width:100%">
            <tbody>

            <tr>
                <td class="leftCol-med" colspan="2">Event Streams or Output
                    Event Adaptors are not available, Please create a Output Event Adaptor to continue...
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
</tbody>
<% } %>
</table>
</form>
</div>
</div>
</fmt:bundle>