<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.stub.types.StreamDefinitionDto" %>
<%@ page import="org.wso2.carbon.event.processor.ui.EventProcessorUIUtils" %>
<%@ page import="java.util.Arrays" %>

<%

    EventProcessorAdminServiceStub eventProcessorAdminServiceStub = EventProcessorUIUtils.getEventProcessorAdminService(config, session, request);
    String strInputStreamDefinitions = request.getParameter("siddhiStreamDefinitions");
    String strQueryExpressions = request.getParameter("queries");
    String strTargetStream = request.getParameter("targetStream");

    String resultString = "";
    if (strInputStreamDefinitions != null) {
        String[] definitions = strInputStreamDefinitions.split(";");
        try {
            StreamDefinitionDto[] siddhiStreams = eventProcessorAdminServiceStub.getSiddhiStreams(definitions, strQueryExpressions);
            StreamDefinitionDto targetDto = null;
            for (StreamDefinitionDto dto : siddhiStreams) {
                if (strTargetStream.equals(dto.getName())) {
                    targetDto = dto;
                    break;
                }
            }

            resultString = EventProcessorUIUtils.getJsonStreamDefinition(targetDto);

        } catch (Exception e) {
            e.printStackTrace();
        }


%>
<%=resultString%>
<%
    }

%>
