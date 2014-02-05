<%@ page import="org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.manager.ui.EventStreamUIUtils" %>

<%

    EventStreamAdminServiceStub stub = EventStreamUIUtils.getEventStreamAdminService(config, session, request);
    String streamId = request.getParameter("streamId");
    String eventType = request.getParameter("eventType");
    String responseText = "";

    if (streamId != null && eventType != null) {
        String sampleEvent = stub.generateSampleEvent(streamId,eventType);
        if(sampleEvent !=null){
            responseText =sampleEvent;
        }
    }
%>
<%=responseText%>
