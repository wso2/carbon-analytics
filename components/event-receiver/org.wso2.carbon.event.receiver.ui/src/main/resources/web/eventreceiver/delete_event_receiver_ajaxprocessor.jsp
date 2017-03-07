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
<%@ page import="org.wso2.carbon.event.receiver.ui.EventReceiverUIUtils" %>
<%@ page import="org.wso2.carbon.event.receiver.stub.EventReceiverAdminServiceStub" %>

<%
    if (!"post".equalsIgnoreCase(request.getMethod())) {
        response.sendError(405);
        return;
    }

    String msg = "fail";
    EventReceiverAdminServiceStub stub = EventReceiverUIUtils.getEventReceiverAdminService(config, session, request);
    String eventReceiverName = request.getParameter("eventReceiverName");
    if (eventReceiverName != null) {
        try{
            stub.undeployActiveEventReceiverConfiguration(eventReceiverName);
            msg = "success";
        }catch(Exception e){
            msg = e.getMessage();
        }
    }
%>
<%=msg%>
