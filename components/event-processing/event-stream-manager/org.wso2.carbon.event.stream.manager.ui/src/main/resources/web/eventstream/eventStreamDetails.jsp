<%@ page import="org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.manager.ui.EventStreamUIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.stream.manager.ui.i18n.Resources">

    <carbon:breadcrumb
            label="eventstream.detail"
            resourceBundle="org.wso2.carbon.event.stream.manager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>


    <link type="text/css" href="css/eventStream.css" rel="stylesheet"/>
    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
    <script type="text/javascript" src="../eventstream/js/event_stream.js"></script>
    <script type="text/javascript"
            src="../eventstream/js/create_eventStream_helper.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>


    <div id="middle">
        <h2><fmt:message key="event.stream.details"/></h2>

        <div id="workArea">
            <form name="eventStreamInfo" action="index.jsp?ordinal=1" method="get"
                  id="showEventStream">
                <table id="eventStreamInfoTable" class="styledLeft"
                       style="width:100%">

                    <thead>
                    <tr>
                        <th><fmt:message key="event.stream.details"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRaw">
                            <table id="eventStreamInputTable" class="normal-nopadding"
                                   style="width:100%">

                                <tbody>
                                <%
                                    String eventStreamWithVersion = request.getParameter("eventStreamWithVersion");
                                    if (eventStreamWithVersion != null) {

                                        EventStreamAdminServiceStub stub = EventStreamUIUtils.getEventStreamAdminService(config, session, request);


                                        String[] eventAdaptorPropertiesDto = stub.getStreamDetailsForStreamId(eventStreamWithVersion);
                                %>
                                <tr>
                                    <td class="leftCol-small">Event Stream Definition</td>
                                    <td><textArea class="expandedTextarea" id="streamDefinitionText"
                                                  name="streamDefinitionText"
                                                  readonly="true"
                                                  cols="120"
                                                  style="height:350px;"><%=eventAdaptorPropertiesDto[0]%>
                                    </textArea></td>


                                </tr>
                                <tr>
                                    <td>Create Sample Event</td>
                                    <td><select name="sampleEventTypeFilter" id="sampleEventTypeFilter">
                                        <option>xml</option>
                                        <option>json</option>
                                        <option>text</option>
                                    </select>
                                        <input type="button" value="<fmt:message key="generate.event"/>"
                                               onclick="generateEvent('<%=eventStreamWithVersion%>')"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td></td>
                                    <td><textArea class="expandedTextarea" id="sampleEventText"
                                                  name="sampleEventText"
                                                  readonly="true"
                                                  cols="120"><%=eventAdaptorPropertiesDto[1]%>
                                    </textArea>
                                    </td>
                                </tr>
                                <%
                                    }
                                %>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    </tbody>

                </table>

            </form>
        </div>
    </div>
</fmt:bundle>