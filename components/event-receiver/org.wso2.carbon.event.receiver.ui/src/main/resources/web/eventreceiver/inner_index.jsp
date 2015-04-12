<%--
  ~ Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License"); you may not
  ~ use this file except in compliance with the License. You may obtain a copy
  ~ of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software distributed
  ~ under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  ~ CONDITIONS OF ANY KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations under the License.
  --%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page
        import="org.wso2.carbon.event.receiver.stub.EventReceiverAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.receiver.stub.types.EventReceiverConfigurationFileDto" %>
<%@ page import="org.wso2.carbon.event.receiver.stub.types.EventReceiverConfigurationInfoDto" %>
<%@ page import="org.wso2.carbon.event.receiver.ui.EventReceiverUIUtils" %>

<fmt:bundle basename="org.wso2.carbon.event.receiver.ui.i18n.Resources">

<carbon:breadcrumb
        label="eventreceiver.list"
        resourceBundle="org.wso2.carbon.event.receiver.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../eventreceiver/js/event_receiver.js"></script>

<%
    EventReceiverAdminServiceStub stub = EventReceiverUIUtils.getEventReceiverAdminService(config, session, request);
    String eventReceiverName = request.getParameter("eventReceiver");
    int totalEventReceivers = 0;
    int totalNotDeployedEventReceivers = 0;
    if (eventReceiverName != null) {
        stub.undeployActiveEventReceiverConfiguration(eventReceiverName);
%>
<script type="text/javascript">CARBON.showInfoDialog('Event Receiver successfully deleted.');</script>
<%
    }

    EventReceiverConfigurationInfoDto[] eventReceiverDetailsArray = stub.getAllActiveEventReceiverConfigurations();
    if (eventReceiverDetailsArray != null) {
        totalEventReceivers = eventReceiverDetailsArray.length;
    }

    EventReceiverConfigurationFileDto[] notDeployedEventReceiverConfigurationFiles = stub.getAllInactiveEventReceiverConfigurations();
    if (notDeployedEventReceiverConfigurationFiles != null) {
        totalNotDeployedEventReceivers = notDeployedEventReceiverConfigurationFiles.length;
    }

%>

<div id="workArea">

    <%=totalEventReceivers%> <fmt:message
        key="active.event.receivers"/> <% if (totalNotDeployedEventReceivers > 0) { %><a
        href="notdeployed_event_receiver_files_details.jsp?ordinal=1"><%=totalNotDeployedEventReceivers%>
    <fmt:message
            key="inactive.event.receivers"/></a><% } else {%><%=totalNotDeployedEventReceivers%>
    <fmt:message key="inactive.event.receivers"/> <% } %>
    <br/><br/>
    <table class="styledLeft">
        <%

            if (eventReceiverDetailsArray != null) {
        %>
        <thead>
        <tr>
            <th><fmt:message key="event.receiver.name"/></th>
            <th><fmt:message key="message.format"/></th>
            <th><fmt:message key="event.adapter.type"/></th>
            <th><fmt:message key="input.stream.id"/></th>
            <th width="420px"><fmt:message key="actions"/></th>
        </tr>
        </thead>
        <tbody>
        <%
            for (EventReceiverConfigurationInfoDto eventReceiverDetails : eventReceiverDetailsArray) {
        %>
        <tr>
            <td>
                <a href="eventReceiver_details.jsp?ordinal=1&eventReceiverName=<%=eventReceiverDetails.getEventReceiverName()%>"><%=eventReceiverDetails.getEventReceiverName()%>
                </a>

            </td>
            <td><%=eventReceiverDetails.getMessageFormat()%>
            </td>
            <td><%=eventReceiverDetails.getInputAdapterType()%>
            </td>
            <td><%=eventReceiverDetails.getInputStreamId()%>
            </td>
            <td>
                <% if(eventReceiverDetails.getEditable()) { %>
                <% if (eventReceiverDetails.getEnableStats()) {%>
                <div class="inlineDiv">
                    <div id="disableStat<%= eventReceiverDetails.getEventReceiverName()%>">
                        <a href="#"
                           onclick="disableReceiverStat('<%= eventReceiverDetails.getEventReceiverName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                key="stat.disable.link"/></a>
                    </div>
                    <div id="enableStat<%= eventReceiverDetails.getEventReceiverName()%>"
                         style="display:none;">
                        <a href="#"
                           onclick="enableReceiverStat('<%= eventReceiverDetails.getEventReceiverName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                key="stat.enable.link"/></a>
                    </div>
                </div>
                <% } else { %>
                <div class="inlineDiv">
                    <div id="enableStat<%= eventReceiverDetails.getEventReceiverName()%>">
                        <a href="#"
                           onclick="enableReceiverStat('<%= eventReceiverDetails.getEventReceiverName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                key="stat.enable.link"/></a>
                    </div>
                    <div id="disableStat<%= eventReceiverDetails.getEventReceiverName()%>"
                         style="display:none">
                        <a href="#"
                           onclick="disableReceiverStat('<%= eventReceiverDetails.getEventReceiverName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                key="stat.disable.link"/></a>
                    </div>
                </div>
                <% }
                    if (eventReceiverDetails.getEnableTracing()) {%>
                <div class="inlineDiv">
                    <div id="disableTracing<%= eventReceiverDetails.getEventReceiverName()%>">
                        <a href="#"
                           onclick="disableReceiverTracing('<%= eventReceiverDetails.getEventReceiverName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                key="trace.disable.link"/></a>
                    </div>
                    <div id="enableTracing<%= eventReceiverDetails.getEventReceiverName()%>"
                         style="display:none;">
                        <a href="#"
                           onclick="enableReceiverTracing('<%= eventReceiverDetails.getEventReceiverName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                key="trace.enable.link"/></a>
                    </div>
                </div>
                <% } else { %>
                <div class="inlineDiv">
                    <div id="enableTracing<%= eventReceiverDetails.getEventReceiverName()%>">
                        <a href="#"
                           onclick="enableReceiverTracing('<%= eventReceiverDetails.getEventReceiverName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                key="trace.enable.link"/></a>
                    </div>
                    <div id="disableTracing<%= eventReceiverDetails.getEventReceiverName()%>"
                         style="display:none">
                        <a href="#"
                           onclick="disableReceiverTracing('<%= eventReceiverDetails.getEventReceiverName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                key="trace.disable.link"/></a>
                    </div>
                </div>

                <% } %>


                <a style="background-image: url(../admin/images/delete.gif);"
                   class="icon-link"
                   onclick="deleteEventReceiver('<%=eventReceiverDetails.getEventReceiverName()%>')"><font
                        color="#4682b4">Delete</font></a>
                <a style="background-image: url(../admin/images/edit.gif);"
                   class="icon-link"
                   href="edit_event_receiver_details.jsp?ordinal=1&eventReceiverName=<%=eventReceiverDetails.getEventReceiverName()%>"><font
                        color="#4682b4">Edit</font></a>
                <% } %>

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
                <table id="noEventReceiverInputTable" class="normal-nopadding"
                       style="width:100%">
                    <tbody>

                    <tr>
                        <td class="leftCol-med" colspan="2"><fmt:message
                                key="empty.event.receiver.msg"/>
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

        <form id="deleteForm" name="input" action="" method="post"><input type="HIDDEN"
                                                                         name="eventReceiver"
                                                                         value=""/></form>
    </div>
</div>

<script type="text/javascript">
    alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
</script>

</fmt:bundle>
