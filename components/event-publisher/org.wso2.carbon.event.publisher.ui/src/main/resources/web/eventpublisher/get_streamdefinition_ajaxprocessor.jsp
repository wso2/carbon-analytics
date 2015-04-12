<%--
  ~ Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.event.publisher.ui.EventPublisherUIConstants" %>
<%@ page
        import="org.wso2.carbon.event.publisher.ui.EventPublisherUIUtils" %>
<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto" %>

<%
    // get Event Stream Definition
    EventStreamAdminServiceStub stub = EventPublisherUIUtils.getEventStreamAdminService(config, session, request);
    String streamName = request.getParameter("streamName");

%>

<%

    if (streamName != null) {
        EventStreamDefinitionDto streamDefinitionDto = stub.getStreamDefinitionDto(streamName);
        EventStreamAttributeDto[] metaAttributeList = streamDefinitionDto.getMetaData();
        EventStreamAttributeDto[] correlationAttributeList = streamDefinitionDto.getCorrelationData();
        EventStreamAttributeDto[] payloadAttributeList = streamDefinitionDto.getPayloadData();

        String attributes = "";

        if (metaAttributeList != null && metaAttributeList.length > 0) {
            for (EventStreamAttributeDto attribute : metaAttributeList) {
                attributes += EventPublisherUIConstants.PROPERTY_META_PREFIX + attribute.getAttributeName() + " " + attribute.getAttributeType() + ", \n";
            }
        }
        if (correlationAttributeList != null) {
            for (EventStreamAttributeDto attribute : correlationAttributeList) {
                attributes += EventPublisherUIConstants.PROPERTY_CORRELATION_PREFIX + attribute.getAttributeName() + " " + attribute.getAttributeType() + ", \n";
            }
        }
        if (payloadAttributeList != null) {
            for (EventStreamAttributeDto attribute : payloadAttributeList) {
                attributes += attribute.getAttributeName() + " " + attribute.getAttributeType() + ", \n";
            }
        }

        if (!attributes.equals("")) {
            attributes = attributes.substring(0, attributes.lastIndexOf(","));
        }

        String streamDefinition = attributes;

%>
<%=streamDefinition%>
<%
    }

%>
