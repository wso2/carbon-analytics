<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page
        import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.formatter.stub.types.EventFormatterConfigurationFileDto" %>
<%@ page import="org.wso2.carbon.event.formatter.stub.types.EventFormatterConfigurationInfoDto" %>
<%@ page import="org.wso2.carbon.event.stream.manager.ui.EventStreamUIUtils" %>

<fmt:bundle basename="org.wso2.carbon.event.formatter.ui.i18n.Resources">

<carbon:breadcrumb
        label="eventformatter.list"
        resourceBundle="org.wso2.carbon.event.formatter.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../eventformatter/js/event_formatter.js"></script>

<%
    String eventStreamWithVersion = request.getParameter("eventStreamWithVersion");
%>

<%
    EventFormatterAdminServiceStub stub = EventStreamUIUtils.getEventFormatterAdminService(config, session, request);
    String eventFormatterName = request.getParameter("eventFormatter");
    int totalEventFormatters = 0;
    int totalNotDeployedEventFormatters = 0;
    if (eventFormatterName != null) {
        stub.undeployActiveEventFormatterConfiguration(eventFormatterName);
%>
<script type="text/javascript">CARBON.showInfoDialog('Event Formatter successfully deleted.');</script>
<%
    }

    EventFormatterConfigurationInfoDto[] eventFormatterDetailsArray = stub.getAllStreamSpecificActiveEventFormatterConfiguration(eventStreamWithVersion);
    if (eventFormatterDetailsArray != null) {
        totalEventFormatters = eventFormatterDetailsArray.length;
    }

    EventFormatterConfigurationFileDto[] notDeployedEventFormatterConfigurationFiles = stub.getAllInactiveEventFormatterConfiguration();
    if (notDeployedEventFormatterConfigurationFiles != null) {
        totalNotDeployedEventFormatters = notDeployedEventFormatterConfigurationFiles.length;
    }

%>

<div>
    <br/>
    <a href="../eventformatter/create_eventFormatter.jsp?ordinal=1"
       style="background-image:url(images/add.gif);"
       class="icon-link">
        Add Event Formatter
    </a>
    <br/> <br/>

    <div id="workArea">

        <%=totalEventFormatters%> <fmt:message
            key="active.event.formatters"/> <% if (totalNotDeployedEventFormatters > 0) { %><a
            href="../eventformatter/notdeployed_event_formatter_files_details.jsp?ordinal=1"><%=totalNotDeployedEventFormatters%>
        <fmt:message
                key="inactive.event.formatters"/></a><% } else {%><%=totalNotDeployedEventFormatters%>
        <fmt:message key="inactive.event.formatters"/> <% } %>
        <br/><br/>
        <table class="styledLeft">
            <%

                if (eventFormatterDetailsArray != null) {
            %>
            <thead>
            <tr>
                <th><fmt:message key="event.formatter.name"/></th>
                <th><fmt:message key="mapping.type"/></th>
                <th><fmt:message key="event.adaptor.name"/></th>
                <th width="420px"><fmt:message key="actions"/></th>
            </tr>
            </thead>
            <tbody>
            <%
                for (EventFormatterConfigurationInfoDto eventFormatterDetails : eventFormatterDetailsArray) {
            %>
            <tr>
                <td>
                    <a href="../eventformatter/eventFormatter_details.jsp?ordinal=1&eventFormatterName=<%=eventFormatterDetails.getEventFormatterName()%>"><%=eventFormatterDetails.getEventFormatterName()%>
                    </a>

                </td>
                <td><%=eventFormatterDetails.getMappingType()%>
                </td>
                <td><%=eventFormatterDetails.getOutEventAdaptorName()%>
                </td>
                <td>
                    <% if (eventFormatterDetails.getEnableStats()) {%>
                    <div class="inlineDiv">
                        <div id="disableStat<%= eventFormatterDetails.getEventFormatterName()%>">
                            <a href="#"
                               onclick="disableStat('<%= eventFormatterDetails.getEventFormatterName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                    key="stat.disable.link"/></a>
                        </div>
                        <div id="enableStat<%= eventFormatterDetails.getEventFormatterName()%>"
                             style="display:none;">
                            <a href="#"
                               onclick="enableStat('<%= eventFormatterDetails.getEventFormatterName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                    key="stat.enable.link"/></a>
                        </div>
                    </div>
                    <% } else { %>
                    <div class="inlineDiv">
                        <div id="enableStat<%= eventFormatterDetails.getEventFormatterName()%>">
                            <a href="#"
                               onclick="enableStat('<%= eventFormatterDetails.getEventFormatterName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                    key="stat.enable.link"/></a>
                        </div>
                        <div id="disableStat<%= eventFormatterDetails.getEventFormatterName()%>"
                             style="display:none">
                            <a href="#"
                               onclick="disableStat('<%= eventFormatterDetails.getEventFormatterName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                    key="stat.disable.link"/></a>
                        </div>
                    </div>
                    <% }
                        if (eventFormatterDetails.getEnableTracing()) {%>
                    <div class="inlineDiv">
                        <div id="disableTracing<%= eventFormatterDetails.getEventFormatterName()%>">
                            <a href="#"
                               onclick="disableTracing('<%= eventFormatterDetails.getEventFormatterName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                    key="trace.disable.link"/></a>
                        </div>
                        <div id="enableTracing<%= eventFormatterDetails.getEventFormatterName()%>"
                             style="display:none;">
                            <a href="#"
                               onclick="enableTracing('<%= eventFormatterDetails.getEventFormatterName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                    key="trace.enable.link"/></a>
                        </div>
                    </div>
                    <% } else { %>
                    <div class="inlineDiv">
                        <div id="enableTracing<%= eventFormatterDetails.getEventFormatterName()%>">
                            <a href="#"
                               onclick="enableTracing('<%= eventFormatterDetails.getEventFormatterName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                    key="trace.enable.link"/></a>
                        </div>
                        <div id="disableTracing<%= eventFormatterDetails.getEventFormatterName()%>"
                             style="display:none">
                            <a href="#"
                               onclick="disableTracing('<%= eventFormatterDetails.getEventFormatterName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                    key="trace.disable.link"/></a>
                        </div>
                    </div>

                    <% } %>


                    <a style="background-image: url(../admin/images/delete.gif);"
                       class="icon-link"
                       onclick="doDelete(document.getElementById('deleteForm1'),'<%=eventFormatterDetails.getEventFormatterName()%>')"><font
                            color="#4682b4">Delete</font></a>
                    <a style="background-image: url(../admin/images/edit.gif);"
                       class="icon-link"
                       href="../eventformatter/edit_event_formatter_details.jsp?ordinal=1&eventFormatterName=<%=eventFormatterDetails.getEventFormatterName()%>"><font
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
                    <table id="noEventFormatterInputTable" class="normal-nopadding"
                           style="width:100%">
                        <tbody>

                        <tr>
                            <td class="leftCol-med" colspan="2"><fmt:message
                                    key="empty.event.formatter.msg"/>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
            </tbody>
            <%
                }
            %>

        </table>

        <div>
            <br/>

            <form id="deleteForm1" name="input" action="" method="get"><input type="HIDDEN"
                                                                             name="eventFormatter"
                                                                             value=""/></form>
        </div>
    </div>
</div>

<script type="text/javascript">
    alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
</script>

</fmt:bundle>
