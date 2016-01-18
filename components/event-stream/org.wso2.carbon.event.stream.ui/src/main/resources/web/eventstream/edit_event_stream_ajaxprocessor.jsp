<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License"); you may not
  ~ use this file except in compliance with the License. You may obtain a copy
  ~ of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software distributed
  ~ under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  ~ CONDITIONS OF ANY KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations under the License.
  --%>
<%@ page import="org.wso2.carbon.analytics.stream.persistence.stub.EventStreamPersistenceAdminServiceStub" %>
<%@ page import="org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTable" %>
<%@ page import="org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTableRecord" %>
<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto" %>
<%@ page import="org.wso2.carbon.event.stream.ui.EventStreamUIUtils" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<%

    String msg;
    try {
        EventStreamAdminServiceStub stub = EventStreamUIUtils.getEventStreamAdminService(config, session, request);

        EventStreamDefinitionDto eventStreamDefinitionDto = new EventStreamDefinitionDto();
        String streamId = request.getParameter("oldStreamId");
        eventStreamDefinitionDto.setName(request.getParameter("eventStreamName"));
        eventStreamDefinitionDto.setVersion(request.getParameter("eventStreamVersion"));
        eventStreamDefinitionDto.setDescription(request.getParameter("eventStreamDescription"));
        eventStreamDefinitionDto.setNickName(request.getParameter("eventStreamNickName"));
        String metaDataSet = request.getParameter("metaData");
        EventStreamAttributeDto[] metaWSO2EventAttributeDtos = null;

        if (metaDataSet != null && !metaDataSet.equals("")) {
            String[] properties = metaDataSet.split("\\$=");
            if (properties != null) {
                // construct property array for each property
                metaWSO2EventAttributeDtos = new EventStreamAttributeDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyConfiguration = property.split("\\^=");
                    if (propertyConfiguration != null) {
                        metaWSO2EventAttributeDtos[index] = new EventStreamAttributeDto();
                        metaWSO2EventAttributeDtos[index].setAttributeName(propertyConfiguration[0].trim());
                        metaWSO2EventAttributeDtos[index].setAttributeType(propertyConfiguration[1].trim());
                        index++;
                    }
                }

            }
        }
        eventStreamDefinitionDto.setMetaData(metaWSO2EventAttributeDtos);
        String correlationDataSet = request.getParameter("correlationData");
        EventStreamAttributeDto[] correlationWSO2EventAttributeDtos = null;

        if (correlationDataSet != null && !correlationDataSet.equals("")) {
            String[] properties = correlationDataSet.split("\\$=");
            if (properties != null) {
                // construct property array for each property
                correlationWSO2EventAttributeDtos = new EventStreamAttributeDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyConfiguration = property.split("\\^=");
                    if (propertyConfiguration != null) {
                        correlationWSO2EventAttributeDtos[index] = new EventStreamAttributeDto();
                        correlationWSO2EventAttributeDtos[index].setAttributeName(propertyConfiguration[0].trim());
                        correlationWSO2EventAttributeDtos[index].setAttributeType(propertyConfiguration[1].trim());
                        index++;
                    }
                }

            }
        }
        eventStreamDefinitionDto.setCorrelationData(correlationWSO2EventAttributeDtos);

        String payloadDataSet = request.getParameter("payloadData");
        EventStreamAttributeDto[] payloadWSO2EventAttributeDtos = null;

        if (payloadDataSet != null && !payloadDataSet.equals("")) {
            String[] properties = payloadDataSet.split("\\$=");
            if (properties != null) {
                // construct property array for each property
                payloadWSO2EventAttributeDtos = new EventStreamAttributeDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyConfiguration = property.split("\\^=");
                    if (propertyConfiguration != null) {
                        payloadWSO2EventAttributeDtos[index] = new EventStreamAttributeDto();
                        payloadWSO2EventAttributeDtos[index].setAttributeName(propertyConfiguration[0].trim());
                        payloadWSO2EventAttributeDtos[index].setAttributeType(propertyConfiguration[1].trim());
                        index++;
                    }
                }

            }
        }
        eventStreamDefinitionDto.setPayloadData(payloadWSO2EventAttributeDtos);

        stub.editEventStreamDefinitionAsDto(eventStreamDefinitionDto, streamId);

        EventStreamPersistenceAdminServiceStub
                streamPersistenceAdminServiceStub = EventStreamUIUtils.getEventStreamPersistenceAdminService(config, session, request);

        if (EventStreamUIUtils.isEventStreamPersistenceAdminServiceAvailable(streamPersistenceAdminServiceStub)) {
            List<AnalyticsTableRecord> analyticsTableRecords = new ArrayList<AnalyticsTableRecord>();
            String metaIndexString = request.getParameter("metaIndex");
            if (metaIndexString != null && !metaIndexString.isEmpty()) {
                String[] properties = metaIndexString.split("\\$=");
                List<AnalyticsTableRecord> metaColumns = EventStreamUIUtils.getAnalyticsRecordList(properties, "meta_");
                analyticsTableRecords.addAll(metaColumns);
            }
            String correlationIndexString = request.getParameter("correlationIndex");
            if (correlationIndexString != null && !correlationIndexString.isEmpty()) {
                String[] properties = correlationIndexString.split("\\$=");
                List<AnalyticsTableRecord> correlationColumns = EventStreamUIUtils.getAnalyticsRecordList(properties,
                                                                                                          "correlation_");
                analyticsTableRecords.addAll(correlationColumns);
            }
            String payloadIndexString = request.getParameter("payloadIndex");
            if (payloadIndexString != null && !payloadIndexString.isEmpty()) {
                String[] properties = payloadIndexString.split("\\$=");
                List<AnalyticsTableRecord> payloadColumns = EventStreamUIUtils.getAnalyticsRecordList(properties, "");
                analyticsTableRecords.addAll(payloadColumns);
            }
            String arbitraryIndexString = request.getParameter("arbitraryIndex");
            if (arbitraryIndexString != null && !arbitraryIndexString.isEmpty()) {
                String[] properties = arbitraryIndexString.split("\\$=");
                List<AnalyticsTableRecord> arbitraryColumns = EventStreamUIUtils.getArbitraryRecordList(properties);
                analyticsTableRecords.addAll(arbitraryColumns);
            }

            AnalyticsTable analyticsTable = new AnalyticsTable();
            analyticsTable.setTableName(request.getParameter("eventStreamName"));
            analyticsTable.setRecordStoreName(request.getParameter("recordStream"));
            analyticsTable.setStreamVersion(request.getParameter("eventStreamVersion"));
            analyticsTable.setPersist(Boolean.parseBoolean(request.getParameter("eventPersist")));
            analyticsTable.setMergeSchema(Boolean.parseBoolean(request.getParameter("mergeSchema")));
            analyticsTable.setAnalyticsTableRecords(
                    analyticsTableRecords.toArray(new AnalyticsTableRecord[analyticsTableRecords.size()]));
            streamPersistenceAdminServiceStub.addAnalyticsTable(analyticsTable);
        }
        msg = "true";

    } catch (Exception e) {
        msg = e.getMessage();
    }
%>
<%=msg%>
