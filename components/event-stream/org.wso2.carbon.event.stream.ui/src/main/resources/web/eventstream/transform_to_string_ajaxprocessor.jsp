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
<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto" %>
<%@ page import="org.wso2.carbon.event.stream.ui.EventStreamUIUtils" %>

<%
	
    String eventDefinitionString = null;
    try {
        EventStreamAdminServiceStub stub = EventStreamUIUtils.getEventStreamAdminService(config, session, request);

        EventStreamDefinitionDto eventStreamDefinitionDto = new EventStreamDefinitionDto();
        eventStreamDefinitionDto.setName(request.getParameter("eventStreamName"));
        eventStreamDefinitionDto.setVersion(request.getParameter("eventStreamVersion"));
        eventStreamDefinitionDto.setDescription(request.getParameter("eventStreamDescription"));
        eventStreamDefinitionDto.setNickName(request.getParameter("eventStreamNickName"));
       
       
        String metaDataSet = request.getParameter("metaData");
        EventStreamAttributeDto[] metaAttributes = null;

        if (metaDataSet != null && !metaDataSet.equals("")) {
            String[] properties = metaDataSet.split("\\$=");
            if (properties != null) {
                // construct property array for each property
                metaAttributes = new EventStreamAttributeDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyConfiguration = property.split("\\^=");
                    if (propertyConfiguration != null) {
                    	metaAttributes[index] = new EventStreamAttributeDto();
                    	metaAttributes[index].setAttributeName(propertyConfiguration[0].trim());
                    	metaAttributes[index].setAttributeType(propertyConfiguration[1].trim());
                        index++;
                    }
                }

            }
        }
        eventStreamDefinitionDto.setMetaData(metaAttributes);

        String correlationDataSet = request.getParameter("correlationData");
        EventStreamAttributeDto[] correlationAttributes = null;

        if (correlationDataSet != null && !correlationDataSet.equals("")) {
            String[] properties = correlationDataSet.split("\\$=");
            if (properties != null) {
                // construct property array for each property
                correlationAttributes = new EventStreamAttributeDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyConfiguration = property.split("\\^=");
                    if (propertyConfiguration != null) {
                    	correlationAttributes[index] = new EventStreamAttributeDto();
                    	correlationAttributes[index].setAttributeName(propertyConfiguration[0].trim());
                    	correlationAttributes[index].setAttributeType(propertyConfiguration[1].trim());
                        index++;
                    }
                }

            }
        }
        eventStreamDefinitionDto.setCorrelationData(correlationAttributes);

        String payloadDataSet = request.getParameter("payloadData");
        EventStreamAttributeDto[] payloadAttributes = null;

        if (payloadDataSet != null && !payloadDataSet.equals("")) {
            String[] properties = payloadDataSet.split("\\$=");
            if (properties != null) {
                // construct property array for each property
                payloadAttributes = new EventStreamAttributeDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyConfiguration = property.split("\\^=");
                    if (propertyConfiguration != null) {
                    	payloadAttributes[index] = new EventStreamAttributeDto();
                    	payloadAttributes[index].setAttributeName(propertyConfiguration[0].trim());
                    	payloadAttributes[index].setAttributeType(propertyConfiguration[1].trim());
                        index++;
                    }
                }

            }
        }
        eventStreamDefinitionDto.setPayloadData(payloadAttributes);

        eventDefinitionString = stub.convertEventStreamDefinitionDtoToString(eventStreamDefinitionDto);
        char         c = 0;
        int          i;
        int          len = eventDefinitionString.length();
        StringBuilder escapeString = new StringBuilder(len + 4);
        String       t;

        escapeString.append('"');
        for (i = 0; i < len; i += 1) {
            c = eventDefinitionString.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    escapeString.append('\\');
                    escapeString.append(c);
                    break;
                case '/':
                    //                if (b == '<') {
                    escapeString.append('\\');
                    //                }
                    escapeString.append(c);
                    break;
                case '\b':
                    escapeString.append("\\b");
                    break;
                case '\t':
                    escapeString.append("\\t");
                    break;
                case '\n':
                    escapeString.append("\\n");
                    break;
                case '\f':
                    escapeString.append("\\f");
                    break;
                case '\r':
                    escapeString.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        escapeString.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        escapeString.append(c);
                    }
            }
        }
        escapeString.append('"');
        eventDefinitionString= "{\"success\":\"success\",\"message\":" +escapeString.toString()+ "}";

    } catch (Exception e) {
        eventDefinitionString = "{\"success\":\"fail\",\"message\":\"" +e.getMessage()+ "\"}";

    }
%>

<%=eventDefinitionString%>
