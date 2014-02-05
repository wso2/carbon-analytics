
<%@ page import="org.wso2.carbon.event.input.adaptor.manager.ui.InputEventAdaptorUIUtils" %>
<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.stub.InputEventAdaptorManagerAdminServiceStub" %>


<%
    // get required parameters to add a event adaptor to back end.
    InputEventAdaptorManagerAdminServiceStub stub = InputEventAdaptorUIUtils.getInputEventManagerAdminService(config, session, request);
    String eventName = request.getParameter("eventName");
    String eventPath = request.getParameter("eventPath");
    String eventAdaptorConfiguration = request.getParameter("eventConfiguration");
    String msg = null;
    if (eventName != null) {
        try {
            // add event adaptor via admin service
            stub.editActiveInputEventAdaptorConfiguration(eventAdaptorConfiguration, eventName);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();

        }
    } else if (eventPath != null) {
        try {
            // add event adaptor via admin service
            stub.editInactiveInputEventAdaptorConfiguration(eventAdaptorConfiguration, eventPath);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();

        }
    }

%>  <%=msg%>   <%

%>
