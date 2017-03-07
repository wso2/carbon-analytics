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
<%@ page import="org.wso2.carbon.event.stream.ui.EventStreamUIUtils" %>

<%

    EventStreamAdminServiceStub stub = EventStreamUIUtils.getEventStreamAdminService(config, session, request);
    String streamId = request.getParameter("streamId");
    String eventType = request.getParameter("eventType");
    String responseText = "";

    if (streamId != null && eventType != null) {
        String sampleEvent = stub.generateSampleEvent(streamId, eventType);
        if (sampleEvent != null) {
            responseText = sampleEvent;
        }
    }
%>
<%=responseText%>
