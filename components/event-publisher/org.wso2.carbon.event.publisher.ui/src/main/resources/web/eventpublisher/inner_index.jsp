<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page
        import="org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.publisher.stub.types.EventPublisherConfigurationFileDto" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.EventPublisherConfigurationInfoDto" %>
<%@ page import="org.wso2.carbon.event.publisher.ui.EventPublisherUIUtils" %>

<fmt:bundle basename="org.wso2.carbon.event.publisher.ui.i18n.Resources">

    <carbon:breadcrumb
            label="eventpublisher.list"
            resourceBundle="org.wso2.carbon.event.publisher.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <link type="text/css" href="css/eventPublisher.css" rel="stylesheet"/>
    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="../eventpublisher/js/event_publisher.js"></script>

    <%
        EventPublisherAdminServiceStub stub = EventPublisherUIUtils.getEventPublisherAdminService(config, session, request);
        String eventPublisherName = request.getParameter("eventPublisher");
        int totalEventPublishers = 0;
        int totalNotDeployedEventPublishers = 0;
        if (eventPublisherName != null) {
            stub.undeployActiveEventPublisherConfiguration(eventPublisherName);
    %>
    <script type="text/javascript">CARBON.showInfoDialog('Event Publisher successfully deleted.');</script>
    <%
        }
        EventPublisherConfigurationInfoDto[] eventPublisherDetailsArray = stub.getAllActiveEventPublisherConfigurations();
        if (eventPublisherDetailsArray != null) {
            totalEventPublishers = eventPublisherDetailsArray.length;
        }
        EventPublisherConfigurationFileDto[] notDeployedEventPublisherConfigurationFiles = stub.getAllInactiveEventPublisherConfigurations();
        if (notDeployedEventPublisherConfigurationFiles != null) {
            totalNotDeployedEventPublishers = notDeployedEventPublisherConfigurationFiles.length;
        }
    %>

    <div id="workArea">

        <%=totalEventPublishers%> <fmt:message
            key="active.event.publishers"/> <% if (totalNotDeployedEventPublishers > 0) { %><a
            href="../eventpublisher/notdeployed_event_publisher_files_details.jsp?ordinal=1"><%=totalNotDeployedEventPublishers%>
        <fmt:message
                key="inactive.event.publishers"/></a><% } else {%><%=totalNotDeployedEventPublishers%>
        <fmt:message key="inactive.event.publishers"/> <% } %>
        <br/><br/>
        <table class="styledLeft">
            <%
                if (eventPublisherDetailsArray != null) {
            %>
            <thead>
            <tr>
                <th><fmt:message key="event.publisher.name"/></th>
                <th><fmt:message key="message.format"/></th>
                <th><fmt:message key="event.adapter.type"/></th>
                <th><fmt:message key="input.stream.id"/></th>
                <th width="420px"><fmt:message key="actions"/></th>
            </tr>
            </thead>
            <tbody>
            <%
                for (EventPublisherConfigurationInfoDto eventPublisherDetails : eventPublisherDetailsArray) {
            %>
            <tr>
                <td>
                    <a href="../eventpublisher/eventPublisher_details.jsp?ordinal=1&eventPublisherName=<%=eventPublisherDetails.getEventPublisherName()%>"><%=eventPublisherDetails.getEventPublisherName()%>
                    </a>

                </td>
                <td><%=eventPublisherDetails.getMessageFormat()%>
                </td>
                <td><%=eventPublisherDetails.getOutputAdapterType()%>
                </td>
                <td><%=eventPublisherDetails.getInputStreamId()%>
                </td>
                <td>
                    <% if (eventPublisherDetails.getEditable()) { %>
                    <% if (eventPublisherDetails.getEnableStats()) {%>
                    <div class="inlineDiv">
                        <div id="disableStat<%= eventPublisherDetails.getEventPublisherName()%>">
                            <a href="#"
                               onclick="disablePublisherStat('<%= eventPublisherDetails.getEventPublisherName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                    key="stat.disable.link"/></a>
                        </div>
                        <div id="enableStat<%= eventPublisherDetails.getEventPublisherName()%>"
                             style="display:none;">
                            <a href="#"
                               onclick="enablePublisherStat('<%= eventPublisherDetails.getEventPublisherName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                    key="stat.enable.link"/></a>
                        </div>
                    </div>
                    <% } else { %>
                    <div class="inlineDiv">
                        <div id="enableStat<%= eventPublisherDetails.getEventPublisherName()%>">
                            <a href="#"
                               onclick="enablePublisherStat('<%= eventPublisherDetails.getEventPublisherName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                    key="stat.enable.link"/></a>
                        </div>
                        <div id="disableStat<%= eventPublisherDetails.getEventPublisherName()%>"
                             style="display:none">
                            <a href="#"
                               onclick="disablePublisherStat('<%= eventPublisherDetails.getEventPublisherName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                    key="stat.disable.link"/></a>
                        </div>
                    </div>
                    <% }
                        if (eventPublisherDetails.getEnableTracing()) {%>
                    <div class="inlineDiv">
                        <div id="disableTracing<%= eventPublisherDetails.getEventPublisherName()%>">
                            <a href="#"
                               onclick="disablePublisherTracing('<%= eventPublisherDetails.getEventPublisherName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                    key="trace.disable.link"/></a>
                        </div>
                        <div id="enableTracing<%= eventPublisherDetails.getEventPublisherName()%>"
                             style="display:none;">
                            <a href="#"
                               onclick="enablePublisherTracing('<%= eventPublisherDetails.getEventPublisherName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                    key="trace.enable.link"/></a>
                        </div>
                    </div>
                    <% } else { %>
                    <div class="inlineDiv">
                        <div id="enableTracing<%= eventPublisherDetails.getEventPublisherName()%>">
                            <a href="#"
                               onclick="enablePublisherTracing('<%= eventPublisherDetails.getEventPublisherName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                    key="trace.enable.link"/></a>
                        </div>
                        <div id="disableTracing<%= eventPublisherDetails.getEventPublisherName()%>"
                             style="display:none">
                            <a href="#"
                               onclick="disablePublisherTracing('<%= eventPublisherDetails.getEventPublisherName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                    key="trace.disable.link"/></a>
                        </div>
                    </div>

                    <% }
                    if (eventPublisherDetails.getEnableProcessing()) { %>
                       <div class="inlineDiv">
                           <div id="disableProcessing<%= eventPublisherDetails.getEventPublisherName()%>">
                               <a href="#"
                                     onclick="disablePublisherProcessing('<%= eventPublisherDetails.getEventPublisherName() %>')"
                                     class="icon-link"
                                     style="background-image:url(images/disable_processing.gif);">Disable Processing </a>
                          </div>
                          <div id="enableProcessing<%= eventPublisherDetails.getEventPublisherName()%>"
                               style="display:none;">
                                <a href="#"
                                      onclick="enablePublisherProcessing('<%= eventPublisherDetails.getEventPublisherName() %>')"
                                      class="icon-link"
                                      style="background-image:url(images/enable_processing.gif);">Enable Processing </a>
                          </div>

                       </div>
                    <% } else { %>
                       <div class="inlineDiv">
                            <div id="enableProcessing<%= eventPublisherDetails.getEventPublisherName()%>">
                                  <a href="#"
                                       onclick="enablePublisherProcessing('<%= eventPublisherDetails.getEventPublisherName() %>')"
                                       class="icon-link"
                                       style="background-image:url(images/enable_processing.gif);">Enable Processing </a>
                           </div>
                          <div id="disableProcessing<%= eventPublisherDetails.getEventPublisherName()%>"
                                  style="display:none">
                                  <a href="#"
                                        onclick="disablePublisherProcessing('<%= eventPublisherDetails.getEventPublisherName() %>')"
                                        class="icon-link"
                                        style="background-image:url(images/disable_processing.gif);">Disable Processing </a>
                           </div>

                       </div>
                    <% } %>
                    <a style="background-image: url(../admin/images/delete.gif);"
                       class="icon-link"
                       onclick="deleteEventPublisher('<%=eventPublisherDetails.getEventPublisherName()%>')"><font
                            color="#4682b4">Delete</font></a>
                    <a style="background-image: url(../admin/images/edit.gif);"
                       class="icon-link"
                       href="../eventpublisher/edit_event_publisher_details.jsp?ordinal=1&eventPublisherName=<%=eventPublisherDetails.getEventPublisherName()%>"><font
                            color="#4682b4">Edit</font></a>
                    <% } else { %>
                    <div class="inlineDiv">
                        <div id="cappArtifact<%= eventPublisherDetails.getEventPublisherName()%>">
                            <div style="background-image: url(images/capp.gif);" class="icon-nolink-nofloat">
                                <fmt:message key="capp.artifact.message"/></div>
                        </div>
                    </div>
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
                    <table id="noEventPublisherInputTable" class="normal-nopadding"
                           style="width:100%">
                        <tbody>

                        <tr>
                            <td class="leftCol-med" colspan="2"><fmt:message
                                    key="empty.event.publisher.msg"/>
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
                                                                              name="eventPublisher"
                                                                              value=""/></form>
        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>