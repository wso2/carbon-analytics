<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.event.execution.plan.wizard.ui.i18n.Resources">

    <link type="text/css" href="../eventexecutionplanwizard/css/cep.css" rel="stylesheet"/>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
    <script type="text/javascript" src="../eventprocessor/js/execution_plans.js"></script>
    <script type="text/javascript"
            src="../eventprocessor/js/create_execution_plan_helper.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>


    <link type="text/css" href="../resources/css/registry.css" rel="stylesheet"/>

    <script type="text/javascript">
        function addCEPExecutionPlan(form, next) {
            var isFieldEmpty = false;
            var execPlanName = document.getElementById("executionPlanId").value.trim();
            var description = document.getElementById("executionPlanDescId").value.trim();
            var snapshotInterval = document.getElementById("siddhiSnapshotTime").value.trim();
            var distributedProcessing = document.getElementById("distributedProcessing").value.trim();

            // query expressions can be empty for pass thru buckets...
            var queryExpressions = window.queryEditor.getValue();
            var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");

            if (!reWhiteSpace.test(execPlanName)) {
                CARBON.showErrorDialog("White spaces are not allowed in execution plan name.");
                return;
            }
            if ((execPlanName == "")) {
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
            } else {

                var mapData = "";
                var importedStreams = "";

                var importedStreamTable = document.getElementById("streamDefinitionsTable");
                if (importedStreamTable.rows.length > 0) {
                    importedStreams = getImportedStreamDataValues(importedStreamTable);
                }
                else {
                    CARBON.showErrorDialog("Imported streams cannot be empty.");
                    return;
                }

                var exportedStreams = "";
                var exportedStreamsTable = document.getElementById("exportedStreamsTable");
                if (exportedStreamsTable.rows.length > 0) {
                    exportedStreams = getExportedStreamDataValues(exportedStreamsTable);
                }

            }

            new Ajax.Request('../eventprocessor/add_execution_plan_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {execPlanName: execPlanName, description: description,
                    snapshotInterval: snapshotInterval, distributedProcessing: distributedProcessing, queryExpressions: queryExpressions,
                    importedStreams: importedStreams, exportedStreams: exportedStreams},
                onSuccess: function (response) {
                    if ("true" == response.responseText.trim().trim()) {
                        if (next) {
                            loadUIElements('formatter');
                        } else {
                            form.submit();
                        }

                    } else {
                        CARBON.showErrorDialog("Failed to add Execution Plan, Exception: " + response.responseText.trim());
                    }

                }
            })


        }
    </script>

    <div id="middle">
        <h2><fmt:message key="execution.plan.create"/></h2>

            <%--<h6><fmt:message key="title.execution.plan.create"/></h6>--%>

        <div id="workArea">
            <form name="inputForm" method="get" id="addExecutionPlanForm">
                <table style="width:100%" id="eventProcessorAdd"
                       class="styledLeft noBorders spacer-bot">
                    <thead>
                    <tr>
                        <th>Enter Event Processor Details</th>
                    </tr>
                    </thead>
                    <tbody>
                    <%@include file="../eventprocessor/inner_executionPlan_ui.jsp" %>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="<fmt:message key="save"/>"
                                   onclick="addCEPExecutionPlan(document.getElementById('addExecutionPlanForm'),false)"/>
                            <input type="button" value="<fmt:message key="add.execution.plan.and.proceed.to.event.formatter"/>"
                                   onclick="addCEPExecutionPlan(document.getElementById('addExecutionPlanForm'),true)"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
        <div>

</fmt:bundle>