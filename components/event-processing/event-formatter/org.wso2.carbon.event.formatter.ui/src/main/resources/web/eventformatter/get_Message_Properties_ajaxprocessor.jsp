<%@ page import="com.google.gson.Gson" %>
<%@ page
        import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.formatter.stub.types.EventFormatterPropertyDto" %>
<%@ page import="org.wso2.carbon.event.formatter.ui.EventFormatterUIUtils" %>

<%
    // get Event Adaptor properties
    EventFormatterAdminServiceStub stub = EventFormatterUIUtils.getEventFormatterAdminService(config, session, request);
    String eventAdaptorName = request.getParameter("eventAdaptorName");

    if (eventAdaptorName != null) {
        EventFormatterPropertyDto[] eventFormatterPropertiesDto = stub.getEventFormatterProperties(eventAdaptorName);
        String propertiesString = "";
        propertiesString = new Gson().toJson(eventFormatterPropertiesDto);


%>


<%=propertiesString%>
<%
    }

%>
