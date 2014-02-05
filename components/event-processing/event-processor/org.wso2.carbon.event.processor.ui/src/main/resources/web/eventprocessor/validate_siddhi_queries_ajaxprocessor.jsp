<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.ui.EventProcessorUIUtils" %>

<%

    EventProcessorAdminServiceStub eventProcessorAdminServiceStub = EventProcessorUIUtils.getEventProcessorAdminService(config, session, request);
    String strInputStreamDefinitions = request.getParameter("siddhiStreamDefinitions");
    String strQueryExpressions = request.getParameter("queries");

      String resultString = "";
    if (strInputStreamDefinitions != null) {
        String[] definitions = strInputStreamDefinitions.split(";");

        try {
            boolean result = eventProcessorAdminServiceStub.validateSiddhiQueries(definitions, strQueryExpressions);
            if (result) {
                resultString = "success";
            } else {
                // should be handled by the exception.
            }
        } catch (Exception e) {
            resultString = e.getMessage();
        }


%>
<%=resultString%>
<%
    }

%>
