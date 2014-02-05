<%@ page import="org.wso2.carbon.event.input.adaptor.manager.ui.InputEventAdaptorUIUtils" %>
<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.stub.InputEventAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorPropertyDto" %>


<%
    // get required parameters to add a event adaptor to back end.
    InputEventAdaptorManagerAdminServiceStub stub = InputEventAdaptorUIUtils.getInputEventManagerAdminService(config, session, request);
    String eventName = request.getParameter("eventName");
    String msg = null;

    String eventType = request.getParameter("eventType");

    String inputPropertySet = request.getParameter("inputPropertySet");
    InputEventAdaptorPropertyDto[] inputEventProperties = null;

    if (inputPropertySet != null && (!inputPropertySet.isEmpty())) {
        String[] properties = inputPropertySet.split("\\|=");
        if (properties != null) {
            // construct event adaptor property array for each event adaptor property
            inputEventProperties = new InputEventAdaptorPropertyDto[properties.length];
            int index = 0;
            for (String property : properties) {
                String[] propertyNameAndValue = property.split("\\$=");
                if (propertyNameAndValue != null) {
                    inputEventProperties[index] = new InputEventAdaptorPropertyDto();
                    inputEventProperties[index].setKey(propertyNameAndValue[0].trim());
                    inputEventProperties[index].setValue(propertyNameAndValue[1].trim());
                    index++;
                }
            }

        }
    }

    try {
        // add event adaptor via admin service
        stub.deployInputEventAdaptorConfiguration(eventName, eventType, inputEventProperties);
        msg = "true";
    } catch (Exception e) {
        msg = e.getMessage();

%>

<%

    }

%>  <%=msg%>   <%

%>
