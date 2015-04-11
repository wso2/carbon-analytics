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
        import="org.wso2.carbon.event.receiver.stub.EventReceiverAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.receiver.stub.types.InputAdapterConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.receiver.ui.EventReceiverUIUtils" %>

<%
    EventReceiverAdminServiceStub stub = EventReceiverUIUtils.getEventReceiverAdminService(config, session, request);
    String eventAdapterType = request.getParameter("eventAdapterType");
%>
<%
    if (eventAdapterType != null) {
        String propertiesString = "";
        InputAdapterConfigurationDto configurationDto = stub.getInputAdapterConfigurationSchema(eventAdapterType);
        propertiesString = new Gson().toJson(configurationDto);

%>

<%=propertiesString%>
<%
    }

%>
