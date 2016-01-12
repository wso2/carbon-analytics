<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
<%@ page import="org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.publisher.ui.EventPublisherUIUtils" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.BasicOutputAdapterPropertyDto" %>

<%
    String msg = null;
    try {
        EventPublisherAdminServiceStub stub = EventPublisherUIUtils
                .getEventPublisherAdminService(config, session, request);

        String eventPublisherName = request.getParameter("eventPublisher");

        String eventAdapterType = request.getParameter("eventAdapterInfo");
        String messageFormat = request.getParameter("messageFormat");

        String outputParameterSet = request.getParameter("outputParameters");

        BasicOutputAdapterPropertyDto[] eventPublisherProperties = null;

        if (outputParameterSet != null && !outputParameterSet.equals("")) {
            String[] properties = outputParameterSet.split("\\|=");
            if (properties != null) {
                // construct property array for each property
                eventPublisherProperties = new BasicOutputAdapterPropertyDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyNameAndValue = property.split("\\$=");
                    if (propertyNameAndValue != null) {
                        eventPublisherProperties[index] = new BasicOutputAdapterPropertyDto();
                        eventPublisherProperties[index].setKey(propertyNameAndValue[0].trim());
                        eventPublisherProperties[index].setValue(propertyNameAndValue[1].trim());
                        eventPublisherProperties[index].set_static(true);
                        index++;
                    }
                }

            }
        }

        stub.testPublisherConnection(eventPublisherName, eventAdapterType, eventPublisherProperties, messageFormat);
        msg = "true";
    } catch (Exception e) {
        msg=e.getMessage();
    }
%>
<%=msg%>