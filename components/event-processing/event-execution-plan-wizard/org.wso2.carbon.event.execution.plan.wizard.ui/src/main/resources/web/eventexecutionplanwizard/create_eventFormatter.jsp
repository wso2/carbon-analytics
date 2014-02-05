<%@ page import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.formatter.stub.types.OutputEventAdaptorInfoDto" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.execution.plan.wizard.ui.i18n.Resources">

<link type="text/css" href="../eventexecutionplanwizard/css/cep.css" rel="stylesheet"/>
<link type="text/css" href="../eventformatter/css/eventFormatter.css" rel="stylesheet"/>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../eventformatter/js/event_formatter.js"></script>
<script type="text/javascript"
        src="../eventformatter/js/create_eventFormatter_helper.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>


<script type="text/javascript">
jQuery(document).ready(function () {
    showMappingContext();
});

function addCEPEventFormatter(form) {

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
                    metaData: metaData, correlationData: correlationData, payloadData: payloadData},
                onSuccess: function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showConfirmationDialog(
                                "Event Formatter " + eventFormatterName + " successfully added, Do you want to add another Event Formatter?", function(){
                                    loadUIElements('formatter');
                                }, function () {
                                    CARBON.showInfoDialog("Event process flow is successfully created !");
                                    window.location.href = "../inputeventadaptormanager/index.jsp?ordinal=1";

                                });
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
                    textData: textData, dataFrom: dataFrom},
                onSuccess: function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showConfirmationDialog(
                                "Event Formatter " + eventFormatterName + " successfully added, Do you want to add another Event Formatter?", function(){
                                    loadUIElements('formatter');
                                }, function () {
                                    CARBON.showInfoDialog("Event process flow is successfully created !");
                                    window.location.href = "../inputeventadaptormanager/index.jsp?ordinal=1";
                                });
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
                    textData: textData, dataFrom: dataFrom},
                onSuccess: function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showConfirmationDialog(
                                "Event Formatter " + eventFormatterName + " successfully added, Do you want to add another Event Formatter?", function(){
                                    loadUIElements('formatter');
                                }, function () {
                                    CARBON.showInfoDialog("Event process flow is successfully created !");
                                    window.location.href = "../inputeventadaptormanager/index.jsp?ordinal=1";
                                });
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
                    mapData: mapData},
                onSuccess: function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showConfirmationDialog(
                                "Event Formatter " + eventFormatterName + " successfully added, Do you want to add another Event Formatter?", function(){
                                    loadUIElements('formatter');
                                }, function () {
                                    CARBON.showInfoDialog("Event process flow is successfully created !");
                                    window.location.href = "../inputeventadaptormanager/index.jsp?ordinal=1";
                                });
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
                    dataFrom: dataFrom, jsonData: jsonData},
                onSuccess: function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showConfirmationDialog(
                                "Event Formatter " + eventFormatterName + " successfully added, Do you want to add another Event Formatter?", function(){
                                    loadUIElements('formatter');
                                }, function () {
                                    CARBON.showInfoDialog("Event process flow is successfully created !");
                                    window.location.href = "../inputeventadaptormanager/index.jsp?ordinal=1";
                                });
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
    <h2><fmt:message key="event.formatter.create"/></h2>
    <%--<h6><fmt:message key="title.event.formatter.create"/></h6>--%>

    <div id="workArea">
        <form name="inputForm" method="get" id="addEventFormatter">

            <table style="width:100%" id="eventFormatterAdd" class="styledLeft">
                <% EventFormatterAdminServiceStub eventFormatterstub = EventExecutionPlanWizardUiUtils.getEventFormatterAdminService(config, session, request);
                    String[] outputStreamNames = eventFormatterstub.getAllEventStreamNames();
                    OutputEventAdaptorInfoDto[] outputEventAdaptorInfoDtos = eventFormatterstub.getOutputEventAdaptorInfo();
                    if (outputStreamNames != null && outputEventAdaptorInfoDtos != null && outputEventAdaptorInfoDtos.length > 0) {
                %>
                <thead>
                <tr>
                    <th><fmt:message key="title.event.formatter.details"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRaw">
                        <%@include file="../eventformatter/inner_eventFormatter_ui.jsp" %>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input type="button" value="<fmt:message key="save"/>"
                               onclick="addCEPEventFormatter(document.getElementById('addEventFormatter'))"/>
                    </td>
                </tr>
                </tbody>
                <%
                } else { %>
                <thead>
                <tr>
                    <th><fmt:message key="event.formatter.nostreams.header"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRaw">
                        <table id="noEventBuilderInputTable" class="normal-nopadding"
                               style="width:100%">
                            <tbody>

                            <tr>
                                <td width="80%">Event Streams or output
                                                Event Adaptors are
                                                not available
                                </td>
                                </td>
                                <td width="20%"><a onclick="loadUIElements('outputAdaptor')"
                                                   style="background-image:url(images/add.gif);"
                                                   class="icon-link" href="#">
                                    Add New Output Event Adaptor
                                </a>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                </tbody>


                <% }

                %>
            </table>
        </form>
    </div>
</div>

</fmt:bundle>
