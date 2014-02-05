<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationFileDto" %>
<%@ page import="org.wso2.carbon.event.processor.ui.EventProcessorUIUtils" %>

<fmt:bundle basename="org.wso2.carbon.event.processor.ui.i18n.Resources">

<carbon:breadcrumb
        label="execution.plans.breadcrumb"
        resourceBundle="org.wso2.carbon.event.processor.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../eventprocessor/js/execution_plans.js"></script>


<%
    String eventStreamWithVersion = request.getParameter("eventStreamWithVersion");
    String loadingCondition = request.getParameter("loadingCondition");

%>

<%
    EventProcessorAdminServiceStub stub = EventProcessorUIUtils.getEventProcessorAdminService(config, session, request);
    String executionPlanName = request.getParameter("executionPlan");
    int totalActiveExecutionPlanConfigurations = 0;

    ExecutionPlanConfigurationFileDto[] inactiveExecutionPlanConigurations = stub.getAllInactiveExecutionPlanConigurations();
    int totalInactiveExecutionPlans = 0;
    if (inactiveExecutionPlanConigurations != null) {
        totalInactiveExecutionPlans = inactiveExecutionPlanConigurations.length;
    }

    if (executionPlanName != null) {
        stub.undeployActiveExecutionPlanConfiguration(executionPlanName);
%>
<script type="text/javascript">CARBON.showInfoDialog('Execution Plan successfully deleted.');</script>
<%
    }

    ExecutionPlanConfigurationDto[] executionPlanConfigurationDtos = null;
    if(loadingCondition == null) {
        executionPlanConfigurationDtos = stub.getAllActiveExecutionPlanConfigurations();

    }else if(loadingCondition.equals("exportedStreams")){
        executionPlanConfigurationDtos = stub.getAllExportedStreamSpecificActiveExecutionPlanConfiguration(eventStreamWithVersion);
    }else if(loadingCondition.equals("importedStreams")){
        executionPlanConfigurationDtos = stub.getAllImportedStreamSpecificActiveExecutionPlanConfiguration(eventStreamWithVersion);
    }

    if (executionPlanConfigurationDtos != null) {
        totalActiveExecutionPlanConfigurations = executionPlanConfigurationDtos.length;
    }

%>

<div>

<br/> <br/>

<div id="workArea">
<%=totalActiveExecutionPlanConfigurations%> Active Execution Plans.
<% if (totalInactiveExecutionPlans > 0) { %><a
        href="../eventprocessor/inactive_execution_plan_files_details.jsp?ordinal=1"><%=totalInactiveExecutionPlans%>
    <fmt:message
            key="inactive.execution.plans"/></a><% } else {%><%="0"%>
<fmt:message key="inactive.execution.plans"/> <% } %>
<br/><br/>
<table class="styledLeft">
    <%

        if (executionPlanConfigurationDtos != null) {
    %>
    <thead>
    <tr>
        <th><fmt:message key="event.processor.execution.plan.name"/></th>
        <th><fmt:message key="event.processor.description"/></th>
        <th width="420px"><fmt:message key="event.processor.actions"/></th>
    </tr>
    </thead>
    <tbody>
    <%
        for (ExecutionPlanConfigurationDto executionPlanConfigurationDto : executionPlanConfigurationDtos) {
    %>
    <tr>
        <td>
            <a href="../eventprocessor/execution_plan_details.jsp?ordinal=1&execPlan=<%=executionPlanConfigurationDto.getName()%>">
                <%=executionPlanConfigurationDto.getName()%>
            </a>
        </td>
        <td><%=executionPlanConfigurationDto.getDescription()%>
        </td>
        <td>
            <% if (executionPlanConfigurationDto.getStatisticsEnabled()) {%>
            <div class="inlineDiv">
                <div id="disableStat<%= executionPlanConfigurationDto.getName()%>">
                    <a href="#"
                       onclick="disableStat('<%= executionPlanConfigurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                            key="stat.disable.link"/></a>
                </div>
                <div id="enableStat<%= executionPlanConfigurationDto.getName()%>"
                     style="display:none;">
                    <a href="#"
                       onclick="enableStat('<%= executionPlanConfigurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                            key="stat.enable.link"/></a>
                </div>
            </div>
            <% } else { %>
            <div class="inlineDiv">
                <div id="enableStat<%= executionPlanConfigurationDto.getName()%>">
                    <a href="#"
                       onclick="enableStat('<%= executionPlanConfigurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                            key="stat.enable.link"/></a>
                </div>
                <div id="disableStat<%= executionPlanConfigurationDto.getName()%>"
                     style="display:none">
                    <a href="#"
                       onclick="disableStat('<%= executionPlanConfigurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                            key="stat.disable.link"/></a>
                </div>
            </div>
            <% }
                if (executionPlanConfigurationDto.getTracingEnabled()) {%>
            <div class="inlineDiv">
                <div id="disableTracing<%= executionPlanConfigurationDto.getName()%>">
                    <a href="#"
                       onclick="disableTracing('<%= executionPlanConfigurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                            key="trace.disable.link"/></a>
                </div>
                <div id="enableTracing<%= executionPlanConfigurationDto.getName()%>"
                     style="display:none;">
                    <a href="#"
                       onclick="enableTracing('<%= executionPlanConfigurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                            key="trace.enable.link"/></a>
                </div>
            </div>
            <% } else { %>
            <div class="inlineDiv">
                <div id="enableTracing<%= executionPlanConfigurationDto.getName() %>">
                    <a href="#"
                       onclick="enableTracing('<%= executionPlanConfigurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                            key="trace.enable.link"/></a>
                </div>
                <div id="disableTracing<%= executionPlanConfigurationDto.getName() %>"
                     style="display:none">
                    <a href="#"
                       onclick="disableTracing('<%= executionPlanConfigurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                            key="trace.disable.link"/></a>
                </div>
            </div>

            <% } %>


            <a style="background-image: url(../admin/images/delete.gif);"
               class="icon-link"
               onclick="doDelete('<%=executionPlanConfigurationDto.getName()%>')"><font
                    color="#4682b4">Delete</font></a>
            <a style="background-image: url(../admin/images/edit.gif);"
               class="icon-link"
               href="../eventprocessor/edit_execution_plan.jsp?ordinal=1&execPlanName=<%=executionPlanConfigurationDto.getName()%>"><font
                    color="#4682b4">Edit</font></a>

        </td>
    </tr>
    </tbody>
    <%
        }

    } else {
    %>
    <tbody>
    <tr>
        <td class="formRaw">
            <table id="noExecutionPlanInputTable" class="normal-nopadding"
                   style="width:100%">
                <tbody>

                <tr>
                    <td class="leftCol-med" colspan="2"><fmt:message
                            key="execution.plan.noeb.msg"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
    </tbody>

    <% } %>

</table>

<div>
    <br/>

    <form id="deleteForm" name="input" action="" method="get"><input type="HIDDEN"
                                                                     name="executionPlan"
                                                                     value=""/></form>
</div>
</div>
</div>

<script type="text/javascript">
    alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
</script>

</fmt:bundle>
