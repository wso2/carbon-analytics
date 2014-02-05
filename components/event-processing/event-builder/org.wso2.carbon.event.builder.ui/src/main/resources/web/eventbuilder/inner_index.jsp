<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="org.wso2.carbon.event.builder.stub.EventBuilderAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.builder.stub.types.EventBuilderConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.builder.ui.EventBuilderUIUtils" %>

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

<fmt:bundle basename="org.wso2.carbon.event.builder.ui.i18n.Resources">

<carbon:breadcrumb
        label="event.builder.list.breadcrumb"
        resourceBundle="org.wso2.carbon.event.builder.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../eventbuilder/js/event_builders.js"></script>

<%
    String eventBuilderName = request.getParameter("eventBuilder");
    int activeEventBuilders = 0;
    int totalNotDeployedEventBuilders = 0;
    EventBuilderAdminServiceStub stub = EventBuilderUIUtils.getEventBuilderAdminService(config, session, request);
    if (stub != null) {
        if (eventBuilderName != null) {
            stub.undeployActiveConfiguration(eventBuilderName);
%>
<script type="text/javascript">CARBON.showInfoDialog('Event builder successfully deleted.');</script>
<%
        }

        EventBuilderConfigurationDto[] deployedEventBuilderConfigFiles = stub.getAllActiveEventBuilderConfigurations();
        if (deployedEventBuilderConfigFiles != null) {
            activeEventBuilders = deployedEventBuilderConfigFiles.length;
        }

        String[] inactiveEventBuilderConfigurations = stub.getAllInactiveEventBuilderConfigurations();
        if (inactiveEventBuilderConfigurations != null) {
            totalNotDeployedEventBuilders = inactiveEventBuilderConfigurations.length;
        }
    }

%>

<div>
<a href="../eventbuilder/create_eventbuilder.jsp?ordinal=1"
   style="background-image:url(images/add.gif);"
   class="icon-link">
    Add Event Builder
</a>
<br/> <br/>

<div id="workArea">
    <%=activeEventBuilders%> <fmt:message
        key="active.event.builders.header"/> , <% if (totalNotDeployedEventBuilders > 0) { %><a
        href="../eventbuilder/inactive_event_builder_files_details.jsp?ordinal=1"><%=totalNotDeployedEventBuilders%>
    <fmt:message
            key="inactive.event.builders.header"/></a><% } else {%><%=totalNotDeployedEventBuilders%>
    <fmt:message key="inactive.event.builders.header"/> <% } %>

    <br/> <br/>
    <table class="styledLeft">
        <%
            if (stub != null) {
                EventBuilderConfigurationDto[] eventBuilderConfigurationDtos = stub.getAllActiveEventBuilderConfigurations();
                if (eventBuilderConfigurationDtos != null && eventBuilderConfigurationDtos.length > 0) {

        %>

        <thead>
        <tr>
            <th><fmt:message key="event.builder.name.header"/></th>
            <th><fmt:message key="event.builder.type.header"/></th>
            <th><fmt:message key="transport.adaptor.name.header"/></th>
            <th><fmt:message key="event.builder.stream.header"/></th>
            <th width="420px"><fmt:message key="actions.header"/></th>
        </tr>
        </thead>
        <tbody>
        <%
            for (EventBuilderConfigurationDto eventBuilderConfigurationDto : eventBuilderConfigurationDtos) {

        %>
        <tr>
            <td>
                <a href="../eventbuilder/eventbuilder_details.jsp?ordinal=1&eventBuilderName=<%=eventBuilderConfigurationDto.getEventBuilderConfigName()%>">
                    <%=eventBuilderConfigurationDto.getEventBuilderConfigName()%>
                </a>
            </td>
            <td><%=eventBuilderConfigurationDto.getInputMappingType()%>
            </td>
            <td><%=eventBuilderConfigurationDto.getInputEventAdaptorName()%>
            </td>
            <td><%=eventBuilderConfigurationDto.getToStreamName() + ":" + eventBuilderConfigurationDto.getToStreamVersion()%>
            </td>
            <td>
                <% if (eventBuilderConfigurationDto.getStatisticsEnabled()) {%>
                <div class="inlineDiv">
                    <div id="disableStat<%= eventBuilderConfigurationDto.getEventBuilderConfigName()%>">
                        <a href="#"
                           onclick="disableStat('<%= eventBuilderConfigurationDto.getEventBuilderConfigName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                key="stat.disable.link"/></a>
                    </div>
                    <div id="enableStat<%= eventBuilderConfigurationDto.getEventBuilderConfigName()%>"
                         style="display:none;">
                        <a href="#"
                           onclick="enableStat('<%= eventBuilderConfigurationDto.getEventBuilderConfigName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                key="stat.enable.link"/></a>
                    </div>
                </div>
                <% } else { %>
                <div class="inlineDiv">
                    <div id="enableStat<%= eventBuilderConfigurationDto.getEventBuilderConfigName()%>">
                        <a href="#"
                           onclick="enableStat('<%= eventBuilderConfigurationDto.getEventBuilderConfigName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                key="stat.enable.link"/></a>
                    </div>
                    <div id="disableStat<%= eventBuilderConfigurationDto.getEventBuilderConfigName()%>"
                         style="display:none">
                        <a href="#"
                           onclick="disableStat('<%= eventBuilderConfigurationDto.getEventBuilderConfigName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                key="stat.disable.link"/></a>
                    </div>
                </div>
                <% }
                    if (eventBuilderConfigurationDto.getTraceEnabled()) {%>
                <div class="inlineDiv">
                    <div id="disableTracing<%= eventBuilderConfigurationDto.getEventBuilderConfigName()%>">
                        <a href="#"
                           onclick="disableTracing('<%= eventBuilderConfigurationDto.getEventBuilderConfigName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                key="trace.disable.link"/></a>
                    </div>
                    <div id="enableTracing<%= eventBuilderConfigurationDto.getEventBuilderConfigName()%>"
                         style="display:none;">
                        <a href="#"
                           onclick="enableTracing('<%= eventBuilderConfigurationDto.getEventBuilderConfigName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                key="trace.enable.link"/></a>
                    </div>
                </div>
                <% } else { %>
                <div class="inlineDiv">
                    <div id="enableTracing<%= eventBuilderConfigurationDto.getEventBuilderConfigName()%>">
                        <a href="#"
                           onclick="enableTracing('<%= eventBuilderConfigurationDto.getEventBuilderConfigName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                key="trace.enable.link"/></a>
                    </div>
                    <div id="disableTracing<%= eventBuilderConfigurationDto.getEventBuilderConfigName()%>"
                         style="display:none">
                        <a href="#"
                           onclick="disableTracing('<%= eventBuilderConfigurationDto.getEventBuilderConfigName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                key="trace.disable.link"/></a>
                    </div>
                </div>

                <% } %>
                <a style="background-image: url(../admin/images/delete.gif);"
                   class="icon-link"
                   onclick="deleteEventBuilder('<%=eventBuilderConfigurationDto.getEventBuilderConfigName()%>')"><font
                        color="#4682b4">Delete</font></a>
                <a style="background-image: url(../admin/images/edit.gif);"
                   class="icon-link"
                   href="../eventbuilder/edit_event_builder_details.jsp?ordinal=1&eventBuilderName=<%=eventBuilderConfigurationDto.getEventBuilderConfigName()%>"><font
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
                <table id="noEventBuilderInputTable" class="normal-nopadding"
                       style="width:100%">
                    <tbody>

                    <tr>
                        <td class="leftCol-med" colspan="2"><fmt:message
                                key="event.builder.noeb.msg"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        </tbody>

        <%
                }
            }
        %>
    </table>

</div>
<div>
    <br/>

    <form id="deleteForm" name="input" action="" method="get"><input type="hidden"
                                                                     name="eventBuilder"
                                                                     value=""/></form>
</div>


<script type="text/javascript">
    alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
</script>

</fmt:bundle>
