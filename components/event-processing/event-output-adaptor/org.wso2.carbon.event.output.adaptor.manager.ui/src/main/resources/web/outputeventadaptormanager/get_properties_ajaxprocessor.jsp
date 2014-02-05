
<%@ page
        import="org.wso2.carbon.event.output.adaptor.manager.ui.OutputEventAdaptorUIUtils" %>
<%@ page
        import="org.wso2.carbon.event.output.adaptor.manager.stub.OutputEventAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.output.adaptor.manager.stub.types.OutputEventAdaptorPropertiesDto" %>
<%@ page import="com.google.gson.Gson" %>

<%
    // get Event Adaptor properties
    OutputEventAdaptorManagerAdminServiceStub stub = OutputEventAdaptorUIUtils.getOutputEventManagerAdminService(config, session, request);
    String eventType = request.getParameter("eventType");

%>

<%

    if (eventType != null) {

        OutputEventAdaptorPropertiesDto eventAdaptorPropertiesDto = stub.getOutputEventAdaptorProperties(eventType);
        String propertiesString = "";
        propertiesString = new Gson().toJson(eventAdaptorPropertiesDto);


%>


<%=propertiesString%>
<%
    }

%>
