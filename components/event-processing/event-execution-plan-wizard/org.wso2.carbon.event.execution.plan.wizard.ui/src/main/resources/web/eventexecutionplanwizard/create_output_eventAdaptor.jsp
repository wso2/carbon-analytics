<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.execution.plan.wizard.ui.i18n.Resources">

    <link type="text/css" href="../eventexecutionplanwizard/css/cep.css" rel="stylesheet"/>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>
    <script type="text/javascript">

        function getConfigurationProperties(form) {

            var isFieldEmpty = false;
            var outputParameterString = "";
            var outputPropertyCount = 0;

            // all output properties, not required and required are checked
            while (document.getElementById("outputProperty_Required_" + outputPropertyCount) != null ||
                   document.getElementById("outputProperty_" + outputPropertyCount) != null) {
                // if required fields are empty
                if ((document.getElementById("outputProperty_Required_" + outputPropertyCount) != null)) {
                    if (document.getElementById("outputProperty_Required_" + outputPropertyCount).value.trim() == "") {
                        // values are empty in fields
                        isFieldEmpty = true;
                        outputParameterString = "";
                        break;
                    }
                    else {
                        // values are stored in parameter string to send to backend
                        var propertyValue = document.getElementById("outputProperty_Required_" + outputPropertyCount).value.trim();
                        var propertyName = document.getElementById("outputProperty_Required_" + outputPropertyCount).name;
                        outputParameterString = outputParameterString + propertyName + "$=" + propertyValue + "|=";

                    }
                } else if (document.getElementById("outputProperty_" + outputPropertyCount) != null) {
                    var notRequriedPropertyValue = document.getElementById("outputProperty_" + outputPropertyCount).value.trim();
                    var notRequiredPropertyName = document.getElementById("outputProperty_" + outputPropertyCount).name;
                    if (notRequriedPropertyValue == "") {
                        notRequriedPropertyValue = "  ";
                    }
                    outputParameterString = outputParameterString + notRequiredPropertyName + "$=" + notRequriedPropertyValue + "|=";


                }
                outputPropertyCount++;
            }

            var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
            // Check for white space
            if (!reWhiteSpace.test(document.getElementById("eventNameId").value)) {
                CARBON.showErrorDialog("White spaces are not allowed in event adaptor name.");
                return;
            }
            if (isFieldEmpty || (document.getElementById("eventNameId").value.trim() == "")) {
                // empty fields are encountered.
                CARBON.showErrorDialog("Empty inputs fields are not allowed.");
                return;
            }
            else {
                 return outputParameterString;
            }

        }

        function addOutputEvent(form) {
            var parameters = getConfigurationProperties(form);

            var selectedIndex = document.getElementById("eventTypeFilter").selectedIndex;
            var selected_text = document.getElementById("eventTypeFilter").options[selectedIndex].text;
            var eventAdaptorName = (document.getElementById("eventNameId").value.trim());

            new Ajax.Request('../outputeventadaptormanager/addtest_event_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventName: eventAdaptorName, eventType: selected_text,
                    outputPropertySet: parameters},
                onSuccess: function (msg) {
                    if ("true" == msg.responseText.trim()) {
                        CARBON.showConfirmationDialog(
                                "Output Event Adaptor successfully added, Do you want to add another Output Event Adaptor?", function () {
                                    loadUIElements('outputAdaptor')
                                }, function () {
                                    loadUIElements('formatter')
                                });
                    } else {
                        CARBON.showErrorDialog("Failed to add event adaptor, Exception: " + msg.responseText.trim());
                    }
                }
            })
        }

    </script>

    <div id="middle">
        <h2><fmt:message key="event.output.adaptpr.create"/></h2>
        <%--<h6><fmt:message key="output.event.adaptor.create"/></h6>--%>

        <div id="workArea">
            <form name="inputForm" method="get" id="addEvent">

                <table style="width:100%" id="eventAdd" class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="output.event.adaptor.details"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRaw">
                            <%@include
                                    file="../outputeventadaptormanager/inner_event_adaptor_ui.jsp" %>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="<fmt:message key="save"/>"
                                   onclick="addOutputEvent(document.getElementById('addEvent'))"/>
                            <input type="button" value="<fmt:message key="test.connection"/>"
                                   onclick="testConnection(document.getElementById('addEvent'))"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>

</fmt:bundle>
