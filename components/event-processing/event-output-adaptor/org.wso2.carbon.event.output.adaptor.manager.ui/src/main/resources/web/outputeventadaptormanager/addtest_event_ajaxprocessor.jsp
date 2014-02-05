
<%@ page
        import="org.wso2.carbon.event.output.adaptor.manager.ui.OutputEventAdaptorUIUtils" %>
<%@ page
        import="org.wso2.carbon.event.output.adaptor.manager.stub.OutputEventAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.output.adaptor.manager.stub.types.OutputEventAdaptorPropertyDto" %>

<%
    // get required parameters to add a event adaptor to back end.
    OutputEventAdaptorManagerAdminServiceStub stub = OutputEventAdaptorUIUtils.getOutputEventManagerAdminService(config, session, request);
    String eventName = request.getParameter("eventName");
    String testConnection = request.getParameter("testConnection");
    String msg = null;

    String eventType = request.getParameter("eventType");

    String outputPropertySet = request.getParameter("outputPropertySet");
    OutputEventAdaptorPropertyDto[] inputEventProperties = null;

    if (outputPropertySet != null && (!outputPropertySet.isEmpty())) {
        String[] properties = outputPropertySet.split("\\|=");
        if (properties != null) {
            // construct event adaptor property array for each event adaptor property
            inputEventProperties = new OutputEventAdaptorPropertyDto[properties.length];
            int index = 0;
            for (String property : properties) {
                String[] propertyNameAndValue = property.split("\\$=");
                if (propertyNameAndValue != null) {
                    inputEventProperties[index] = new OutputEventAdaptorPropertyDto();
                    inputEventProperties[index].setKey(propertyNameAndValue[0].trim());
                    inputEventProperties[index].setValue(propertyNameAndValue[1].trim());
                    index++;
                }
            }

        }
    }


    try {
        if (testConnection != null) {
            stub.testConnection(eventName, eventType, inputEventProperties);
        } else {
            // add event adaptor via admin service
            stub.deployOutputEventAdaptorConfiguration(eventName, eventType, inputEventProperties);
        }
        msg = "true";
    } catch (Exception e) {
        msg = e.getMessage();

    }

%><%=msg%><%

%>
