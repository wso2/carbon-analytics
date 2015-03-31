<%--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
--%>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.ui.DashboardAdminClient" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.ui.dto.TableDTO" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.ui.dto.ColumnDTO" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.batch.stub.BatchAnalyticsDashboardAdminServiceStub" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.batch.stub.data.Table" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.batch.stub.data.Column" %>
<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.BasicOutputAdapterPropertyDto" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="com.google.gson.JsonObject" %>

<%

    String responseText = "";
    String action = request.getParameter("action");
    BatchAnalyticsDashboardAdminServiceStub stub =   DashboardAdminClient.getDashboardBatchAnalyticsAdminService(config,session, request);
    EventStreamAdminServiceStub eventStreamAdminServiceStub = DashboardAdminClient.getEventStreamAdminService(config,session,
            request);
    EventPublisherAdminServiceStub eventPublisherAdminServiceStub = DashboardAdminClient.getEventPublisherAdminService(
            config, session,
            request);
    response.setContentType("application/json");
    Gson gson = new Gson();

    if(action == null) {
        String tableName = request.getParameter("table");
        Table table = stub.getRecords(tableName,DashboardAdminClient.timestampFrom("01/09/1985"),DashboardAdminClient.timestampFrom("01/09/2100"),0,10,null);
        TableDTO dto = DashboardAdminClient.toTableDTO(table);
        responseText = gson.toJson(dto);
    } else if(action.equals("getTables")) {

        String[] tables = null;
        String[] streams = null;
        int counter = 0;

        try {
            tables = stub.getTableNames();
        } catch (AxisFault e){
           System.out.println("BAM Specific Admin service does not exist." + e.getMessage() );
        }

        try {
            streams = eventStreamAdminServiceStub.getStreamNames();
        } catch (AxisFault e){
            System.out.println("CEP Specific Admin service does not exist." + e.getMessage() );
        }

        int bamDataLength = 0;
        int cepDataLength = 0;

        if(tables != null){
            bamDataLength = tables.length;
        }
        if(streams != null){
            cepDataLength = streams.length;
        }

        int sourceLength = bamDataLength + cepDataLength;
        ColumnDTO[] dtos = new ColumnDTO[sourceLength];

        //populating BAM source names
        for(int i=0;i<bamDataLength;i++) {
            dtos[counter] = new ColumnDTO(tables[i],"batch");
            counter++;
        }

        //populating CEP source names
        for(int i=0;i<cepDataLength;i++) {
            dtos[counter] = new ColumnDTO(streams[i],"realTime");
            counter++;
        }

        responseText = gson.toJson(dtos);
    } else if(action.equals("getSchema")) {
        String tableName = request.getParameter("table");
        Column[] columns = stub.getTableSchema(tableName);
        if(columns != null && columns.length > 0) {
            ColumnDTO[] dtos = new ColumnDTO[columns.length];
            for(int i=0;i< columns.length;i++) {
              ColumnDTO dto = new ColumnDTO(columns[i].getName(),columns[i].getType());
              dtos[i] = dto;  
            }
            responseText = gson.toJson(dtos);
        }
    } else if(action.equals("deployOutputAdapterUIPublisher")){

        String streamId = request.getParameter("streamId");

        String streamSplittedValues[] = streamId.split(":");
        String streamName = streamSplittedValues[0];
        String streamVersion = streamSplittedValues[1];
        String eventPublisherName = streamName + "_" + streamVersion + "_UIPublisher";
        String streamNameWithVersion = streamId;
        String eventAdapterType = "ui";
        Boolean isSuccess = true;

        BasicOutputAdapterPropertyDto propertyStreamName = new BasicOutputAdapterPropertyDto();
        propertyStreamName.setKey("output.event.stream.name");
        propertyStreamName.setValue(streamName);
        propertyStreamName.set_static(false);

        BasicOutputAdapterPropertyDto propertyStreamVersion = new BasicOutputAdapterPropertyDto();
        propertyStreamVersion.setKey("output.event.stream.version");
        propertyStreamVersion.setValue(streamVersion);
        propertyStreamVersion.set_static(false);

        BasicOutputAdapterPropertyDto[] outputPropertyConfiguration = new
                BasicOutputAdapterPropertyDto[]{propertyStreamName,propertyStreamVersion};

        JsonObject responseObj = new JsonObject();
        try {
            eventPublisherAdminServiceStub
                    .deployWSO2EventPublisherConfiguration(eventPublisherName, streamNameWithVersion,
                            eventAdapterType, null, null, null, outputPropertyConfiguration, false);
        }catch (AxisFault e) {
            isSuccess = false;
            System.out.println(e.getMessage());
            responseObj.addProperty("isDeployedSuccess",false);
            responseObj.addProperty("message",e.getMessage());
        }

        if(isSuccess){
            responseObj.addProperty("isDeployedSuccess",true);
            responseObj.addProperty("message","Event Publisher " + eventPublisherName + "Successfully deployed ");
        }
        responseText = gson.toJson(responseObj);
    } else if(action.equals("getStreamDefinition")){

        String streamId = request.getParameter("streamId");
        int correlationDataLength = 0;
        int metaDataLength = 0;
        int payloadDataLength = 0;
        int counter = 0;

        EventStreamDefinitionDto eventStreamDefinitionDto
                = eventStreamAdminServiceStub.getStreamDefinitionDto(streamId);

        EventStreamAttributeDto[] metaData = eventStreamDefinitionDto.getMetaData();
        EventStreamAttributeDto[] correlationData = eventStreamDefinitionDto.getCorrelationData();
        EventStreamAttributeDto[] payloadData = eventStreamDefinitionDto.getPayloadData();

        if(metaData != null){
            metaDataLength = metaData.length;
        }
        if(correlationData != null){
            correlationDataLength = correlationData.length;
        }
        if(payloadData != null){
            payloadDataLength = payloadData.length;
        }

        int allDataLength = metaDataLength + correlationDataLength + payloadDataLength;
        ColumnDTO[] dtos = new ColumnDTO[allDataLength];

        for(int i=0;i<metaDataLength;i++){

            ColumnDTO dto = new ColumnDTO("Meta_"+metaData[i].getAttributeName(),metaData[i].getAttributeType());
            dtos[counter] = dto;
            counter++;
        }

        for(int i=0;i<correlationDataLength;i++){

            ColumnDTO dto = new
                    ColumnDTO("Correlation_"+correlationData[i].getAttributeName(),correlationData[i].getAttributeType());
            dtos[counter] = dto;
            counter++;
        }

        for(int i=0;i<payloadDataLength;i++){
            ColumnDTO dto = new
                    ColumnDTO("Payload_"+payloadData[i].getAttributeName(),payloadData[i].getAttributeType());
            dtos[counter] = dto;
            counter++;
        }
        responseText = gson.toJson(dtos);

    }
%>
<%=responseText%>

