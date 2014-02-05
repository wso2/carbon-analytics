<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.execution.plan.wizard.ui.i18n.Resources">

    <link type="text/css" href="../eventexecutionplanwizard/css/cep.css" rel="stylesheet"/>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>
    <script type="text/javascript">
        function addInputEvent(form) {

            var isFieldEmpty = false;
            var inputParameterString = "";
            var inputPropertyCount = 0;

            // all input properties, not required and required are checked
            while (document.getElementById("inputProperty_Required_" + inputPropertyCount) != null ||
                   document.getElementById("inputProperty_" + inputPropertyCount) != null) {
                // if required fields are empty
                if ((document.getElementById("inputProperty_Required_" + inputPropertyCount) != null)) {
                    if (document.getElementById("inputProperty_Required_" + inputPropertyCount).value.trim() == "") {
                        // values are empty in fields
                        isFieldEmpty = true;
                        inputParameterString = "";
                        break;
                    }
                    else {
                        // values are stored in parameter string to send to backend
                        var propertyValue = document.getElementById("inputProperty_Required_" + inputPropertyCount).value.trim();
                        var propertyName = document.getElementById("inputProperty_Required_" + inputPropertyCount).name;
                        inputParameterString = inputParameterString + propertyName + "$=" + propertyValue + "|=";

                    }
                } else if (document.getElementById("inputProperty_" + inputPropertyCount) != null) {
                    var notRequriedPropertyValue = document.getElementById("inputProperty_" + inputPropertyCount).value.trim();
                    var notRequiredPropertyName = document.getElementById("inputProperty_" + inputPropertyCount).name;
                    if (notRequriedPropertyValue == "") {
                        notRequriedPropertyValue = "  ";
                    }
                    inputParameterString = inputParameterString + notRequiredPropertyName + "$=" + notRequriedPropertyValue + "|=";


                }
                inputPropertyCount++;
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
                // create parameter string
                var selectedIndex = document.getElementById("eventTypeFilter").selectedIndex;
                var selected_text = document.getElementById("eventTypeFilter").options[selectedIndex].text;
                var eventAdaptorName = (document.getElementById("eventNameId").value.trim());
                var parameters = "?eventName=" + eventAdaptorName
                                         + "&eventType=" + selected_text;

                if (inputParameterString != "") {
                    parameters = parameters + "&inputPropertySet=" + inputParameterString;
                }

                new Ajax.Request('../inputeventadaptormanager/add_event_ajaxprocessor.jsp', {
                    method: 'post',
                    asynchronous: false,
                    parameters: {eventName: eventAdaptorName, eventType: selected_text,
                        inputPropertySet: inputParameterString},
                    onSuccess: function (msg) {
                        if ("true"==msg.responseText.trim()) {
                            CARBON.showConfirmationDialog(
                                    "Input Event Adaptor " + eventAdaptorName + " successfully added, Do you want to add another input Event Adaptor?", function () {
                                        loadUIElements('inputAdaptor')
                                    }, function () {
                                        loadUIElements('builder')
                                    });
                        } else {
                            CARBON.showErrorDialog("Failed to add event adaptor, Exception: " + msg.responseText.trim());
                        }
                    }
                })

            }

        }
    </script>

    <div id="middle">
        <h2><fmt:message key="event.input.adaptpr.create"/></h2>
        <%--<h6><fmt:message key="input.event.adaptor.create"/></h6>--%>

        <div id="workArea">
            <form name="inputForm" method="get" id="addEvent">
                <table style="width:100%" id="eventAdd" class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="input.event.adaptor.details"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRaw">
                            <%@include
                                    file="../inputeventadaptormanager/inner_event_adaptor_ui.jsp" %>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="<fmt:message key="save"/>"
                                   onclick="addInputEvent(document.getElementById('addEvent'))"/>
                            <input type="button" value="<fmt:message key="skip"/>"
                                   onclick="loadUIElements('builder')"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>

        </div>
    </div>
</fmt:bundle>
