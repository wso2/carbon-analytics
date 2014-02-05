<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.wso2.carbon.event.input.adaptor.manager.ui.InputEventAdaptorUIUtils" %>
<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorPropertiesDto" %>
<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.stub.InputEventAdaptorManagerAdminServiceStub" %>

<%
    // get Event Adaptor properties
    InputEventAdaptorManagerAdminServiceStub stub = InputEventAdaptorUIUtils.getInputEventManagerAdminService(config, session, request);
    String eventType = request.getParameter("eventType");

%>

<%

    if (eventType != null) {

        InputEventAdaptorPropertiesDto eventAdaptorPropertiesDto = stub.getInputEventAdaptorProperties(eventType);
        String propertiesString = "";
        propertiesString = new Gson().toJson(eventAdaptorPropertiesDto);


%>


<%=propertiesString%>
<%
    }

%>
