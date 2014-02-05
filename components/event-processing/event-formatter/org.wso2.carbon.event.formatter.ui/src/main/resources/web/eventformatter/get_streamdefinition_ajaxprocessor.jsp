<%@ page import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.formatter.ui.EventFormatterUIUtils" %>

<%
    // get Event Stream Definition
    EventFormatterAdminServiceStub stub = EventFormatterUIUtils.getEventFormatterAdminService(config, session, request);
    String streamName = request.getParameter("streamName");

%>

<%

    if (streamName != null) {

        String streamDefinition = stub.getStreamDefinition(streamName);

%>


<%=streamDefinition%>
<%
    }

%>
