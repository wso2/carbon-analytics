<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.processor.stub.types.SiddhiConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.processor.stub.types.StreamConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.processor.ui.EventProcessorUIUtils" %>
<%@ page import="org.wso2.carbon.event.processor.ui.UIConstants" %>
<%@ page import="java.util.Arrays" %>
<%
    String msg;
    EventProcessorAdminServiceStub stub = EventProcessorUIUtils.getEventProcessorAdminService(config, session, request);

    String executionPlanName = request.getParameter("execPlanName");
    String description = request.getParameter("description");
    String snapshotInterval = request.getParameter("snapshotInterval");
    String distributedProcessing = request.getParameter("distributedProcessing");
    String queryExpressions = request.getParameter("queryExpressions");
    String importedStreams = request.getParameter("importedStreams");
    String exportedStreams = request.getParameter("exportedStreams");

    ExecutionPlanConfigurationDto configDto = new ExecutionPlanConfigurationDto();
    configDto.setName(executionPlanName);
    configDto.setDescription(description);

    SiddhiConfigurationDto[] siddhiConfigurationDtos = new SiddhiConfigurationDto[2];
    siddhiConfigurationDtos[0] = new SiddhiConfigurationDto();
    siddhiConfigurationDtos[0].setKey(UIConstants.SIDDHI_SNAPSHOT_INTERVAL);
    siddhiConfigurationDtos[0].setValue(snapshotInterval);

    siddhiConfigurationDtos[1] = new SiddhiConfigurationDto();
    siddhiConfigurationDtos[1].setKey(UIConstants.SIDDHI_DISTRIBUTED_PROCESSING);
    siddhiConfigurationDtos[1].setValue(distributedProcessing);

    configDto.setSiddhiConfigurations(siddhiConfigurationDtos);
    configDto.setQueryExpressions(queryExpressions);

    String[] importedStreamsArray = importedStreams.split("\\$=");
    StreamConfigurationDto[] importedStreamConfigurationDtoArray = new StreamConfigurationDto[importedStreamsArray.length];
    int i = 0;
    for (String streamMapping : importedStreamsArray) {
        String[] mappings = streamMapping.trim().split("\\^=");
        if (mappings.length >= 2) {
            StreamConfigurationDto streamDto = new StreamConfigurationDto();
            streamDto.setStreamId(mappings[0].trim());
            streamDto.setSiddhiStreamName(mappings[1].trim());
            importedStreamConfigurationDtoArray[i] = streamDto;
            i++;
        }
    }
    if (importedStreamConfigurationDtoArray.length > i + 1) {
        importedStreamConfigurationDtoArray = Arrays.copyOf(importedStreamConfigurationDtoArray, i + 1);
    }

    String[] exportedStreamsArray = exportedStreams.split("\\$=");
    StreamConfigurationDto[] exportedStreamConfigurationDtoArray = new StreamConfigurationDto[exportedStreamsArray.length];
    i = 0;
    for (String streamMapping : exportedStreamsArray) {
        String[] mappings = streamMapping.trim().split("\\^=");
        if (mappings.length >= 2) {
            StreamConfigurationDto streamDto = new StreamConfigurationDto();
            streamDto.setSiddhiStreamName(mappings[0].trim());
            streamDto.setStreamId(mappings[1].trim());
            if(mappings.length >= 3 && UIConstants.TRUE_LITERAL.equalsIgnoreCase(mappings[2])) {
                streamDto.setPassThroughFlowSupported(true);
            }
            exportedStreamConfigurationDtoArray[i] = streamDto;
            i++;
        }
    }
    if (exportedStreamConfigurationDtoArray.length > i + 1) {
        exportedStreamConfigurationDtoArray = Arrays.copyOf(exportedStreamConfigurationDtoArray, i + 1);
    }

    configDto.setImportedStreams(importedStreamConfigurationDtoArray);
    configDto.setExportedStreams(exportedStreamConfigurationDtoArray);

    try {
        stub.deployExecutionPlanConfiguration(configDto);
        msg = "true";
    } catch (Exception ex) {
        msg = ex.getMessage();
    }
%><%=msg%>