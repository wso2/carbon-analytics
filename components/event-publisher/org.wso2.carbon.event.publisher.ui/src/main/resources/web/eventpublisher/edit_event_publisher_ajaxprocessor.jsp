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
<%@ page
        import="org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.publisher.ui.EventPublisherUIUtils" %>
<%
    // get required parameters to add a event publisher to back end.
    EventPublisherAdminServiceStub stub = EventPublisherUIUtils.getEventPublisherAdminService(config, session, request);
    String eventPublisherName = request.getParameter("eventPublisherName");
    String eventPublisherPath = request.getParameter("eventPublisherPath");
    String eventPublisherConfiguration = request.getParameter("eventPublisherConfiguration");
    String msg = null;
    if (eventPublisherName != null) {
        try {
            // add event publisher via admin service
            stub.editActiveEventPublisherConfiguration(eventPublisherConfiguration, eventPublisherName);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();

        }
    } else if (eventPublisherPath != null) {
        try {
            // add event publisher via admin service
            stub.editInactiveEventPublisherConfiguration(eventPublisherConfiguration, eventPublisherPath);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();

        }
    }
    // Since JSP faithfully replicates all spaces, new lines encountered to HTML,
    // and since msg is output as a response flag, please take care in editing
    // the snippet surrounding print of msg.
%><%=msg%><%%>
