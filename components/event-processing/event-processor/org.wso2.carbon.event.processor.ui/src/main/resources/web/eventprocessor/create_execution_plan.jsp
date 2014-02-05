<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.event.processor.ui.i18n.Resources">

    <carbon:breadcrumb
            label="add"
            resourceBundle="org.wso2.carbon.event.processor.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
    <script type="text/javascript" src="../eventprocessor/js/execution_plans.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>

    <script type="text/javascript"
            src="js/create_execution_plan_helper.js"></script>

    <link type="text/css" href="../resources/css/registry.css" rel="stylesheet"/>
        <div id="custom_dcontainer" style="display:none"></div>

    <div id="middle">
        <h2><fmt:message key="title.execution.plan.create"/></h2>
        <div id="workArea">

            <form name="inputForm" action="index.jsp?ordinal=1" method="get" id="addExecutionPlanForm">

                <table style="width:100%" id="eventProcessorAdd"
                       class="styledLeft noBorders spacer-bot">

                        <%--error page--%>
                    <thead>
                    <tr>
                        <th>Enter Event Processor Details</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr id="uiElement">
                    <%@include file="inner_execution_plan_ui.jsp" %>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="<fmt:message key="add.execution.plan"/>"
                                   onclick="addExecutionPlan(document.getElementById('addExecutionPlanForm'))"/>
                        </td>
                    </tr>
                    </tbody>

                </table>

            </form>
        </div>
    </div>

</fmt:bundle>