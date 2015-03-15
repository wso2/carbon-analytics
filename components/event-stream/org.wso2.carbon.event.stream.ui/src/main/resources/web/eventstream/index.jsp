<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamInfoDto" %>
<%@ page import="org.wso2.carbon.event.stream.ui.EventStreamUIUtils" %>

<fmt:bundle basename="org.wso2.carbon.event.stream.ui.i18n.Resources">

    <carbon:breadcrumb
            label="eventstream.list"
            resourceBundle="org.wso2.carbon.event.stream.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>

    <script type="text/javascript">

        var ENABLE = "enable";
        var DISABLE = "disable";
        var STAT = "statistics";
        var TRACE = "Tracing";

        function doDelete(eventStreamName, eventStreamVersion) {
            CARBON.showConfirmationDialog(
                    "If event stream is deleted then other artifacts using this stream will go into inactive state! Are you sure want to delete?", function () {
                        var theform = document.getElementById('deleteForm');
                        theform.eventStream.value = eventStreamName;
                        theform.eventStreamVersion.value = eventStreamVersion;
                        theform.submit();
                    }, null, null);
        }

    </script>
    <%
        EventStreamAdminServiceStub stub = EventStreamUIUtils.getEventStreamAdminService(config, session, request);
        String eventStreamName = request.getParameter("eventStream");
        String eventStreamVersion = request.getParameter("eventStreamVersion");
        int totalEventStreams = 0;
        if (eventStreamName != null && eventStreamVersion != null) {
            stub.removeEventStreamDefinition(eventStreamName, eventStreamVersion);
    %>
    <script type="text/javascript">CARBON.showInfoDialog('Event Stream successfully deleted.');</script>
    <%
        }

        EventStreamInfoDto[] eventStreamDetailsArray = stub.getAllEventStreamDefinitionDto();
        if (eventStreamDetailsArray != null) {
            totalEventStreams = eventStreamDetailsArray.length;
        }

    %>

    <div id="middle">
        <h2><fmt:message key="available.event.streams"/></h2>

        <table border="0">
            <tr>
                <td><a href="create_event_stream.jsp?ordinal=1"
                       style="background-image:url(images/add.gif);"
                       class="icon-link">
                    Add Event Stream
                </a></td>
                <td width="10%">
                    <a href="../eventbuilder/index.jsp?ordinal=1"
                       style="background-image:url(images/eventBuilder.gif);"
                       class="icon-link">
                        All Event Builders
                    </a>

                </td>
                <td width="10%">
                    <a href="../eventformatter/index.jsp?ordinal=1"
                       style="background-image:url(images/event_formatter.gif);"
                       class="icon-link">
                        All Event Formatters
                    </a>
            </tr>
        </table>

        <br/>

        <div id="workArea">

            <%=totalEventStreams%> <fmt:message
                key="total.event.streams"/>
            <br/><br/>
            <table class="styledLeft">
                <%

                    if (eventStreamDetailsArray != null) {
                %>
                <thead>
                <tr>
                    <th><fmt:message key="event.stream.id"/></th>
                    <th><fmt:message key="event.stream.description"/></th>
                    <th width="30%"><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    for (EventStreamInfoDto eventStreamInfoDto : eventStreamDetailsArray) {
                        String eventStreamWithVersion = eventStreamInfoDto.getStreamName() + ":" + eventStreamInfoDto.getStreamVersion();
                %>
                <tr>
                    <td>
                        <a href="eventStreamDetails.jsp?ordinal=1&eventStreamWithVersion=<%=eventStreamWithVersion%>"><%=eventStreamWithVersion%>
                        </a>
                    </td>
                    <td><%= eventStreamInfoDto.getStreamDescription() != null ? eventStreamInfoDto.getStreamDescription() : "" %>
                    </td>
                    <td>
                        <a style="background-image: url(../eventstream/images/inflow.png);"
                           class="icon-link"
                           href="stream_in_flows.jsp?ordinal=1&eventStreamWithVersion=<%=eventStreamWithVersion%>"><font
                                color="#4682b4">In-Flows</font></a>

                        <a style="background-image: url(../eventstream/images/outflow.png);"
                           class="icon-link"
                           href="stream_out_flows.jsp?ordinal=1&eventStreamWithVersion=<%=eventStreamWithVersion%>"><font
                                color="#4682b4">Out-Flows</font></a>
                        <%
                        if(eventStreamInfoDto.getEditable()) {
                        %>

                        <a style="background-image: url(../admin/images/delete.gif);"
                           class="icon-link"
                           onclick="doDelete('<%=eventStreamInfoDto.getStreamName()%>','<%=eventStreamInfoDto.getStreamVersion()%>')"><font
                                color="#4682b4">Delete</font></a>

                        <a style="background-image: url(../admin/images/edit.gif);"
                           class="icon-link"
                           href="edit_event_stream.jsp?ordinal=1&eventStreamWithVersion=<%=eventStreamWithVersion%>"><font
                                color="#4682b4">Edit</font></a>
                        <%
                            }
                        %>
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
                        <table id="noEventStreamInputTable" class="normal-nopadding"
                               style="width:100%">
                            <tbody>

                            <tr>
                                <td class="leftCol-med" colspan="2"><fmt:message
                                        key="empty.event.stream.msg"/>
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
                                                                                 name="eventStream"
                                                                                 value=""/>
                    <input type="HIDDEN"
                           name="eventStreamVersion"
                           value=""/>
                </form>

            </div>
        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>
