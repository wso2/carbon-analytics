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
<%@ page import="com.google.gson.Gson" %>
<%@ page
        import="org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.publisher.ui.EventPublisherUIUtils" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.OutputAdapterConfigurationDto" %>

<%
    // get Event Adapter properties
    EventPublisherAdminServiceStub stub = EventPublisherUIUtils.getEventPublisherAdminService(config, session, request);
    String eventAdapterType = request.getParameter("eventAdapterType");

    if (eventAdapterType != null) {
        OutputAdapterConfigurationDto eventPublisherPropertiesDto = stub.getOutputAdapterConfigurationSchema(eventAdapterType);
        String propertiesString = "";
        propertiesString = new Gson().toJson(eventPublisherPropertiesDto);


%>
<%=propertiesString%>
<%
    }

%>
