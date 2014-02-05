<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.ui.EventProcessorUIUtils" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>


<%
    String executionPlanName = request.getParameter("execPlanName");
    String action = request.getParameter("action");

    if (executionPlanName != null && action != null) {
        EventProcessorAdminServiceStub stub = EventProcessorUIUtils.getEventProcessorAdminService(config, session, request);
        if ("enableStat".equals(action)) {
            stub.setStatisticsEnabled(executionPlanName, true);
        } else if ("disableStat".equals(action)) {
            stub.setStatisticsEnabled(executionPlanName, false);
        } else if ("enableTracing".equals(action)) {
            stub.setTracingEnabled(executionPlanName, true);
        } else if ("disableTracing".equals(action)) {
            stub.setTracingEnabled(executionPlanName, false);
        }
    }

%>