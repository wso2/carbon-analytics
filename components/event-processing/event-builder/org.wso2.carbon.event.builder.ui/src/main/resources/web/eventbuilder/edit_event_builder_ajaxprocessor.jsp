<%@ page
        import="org.wso2.carbon.event.builder.stub.EventBuilderAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.builder.ui.EventBuilderUIUtils" %>
<%--
  ~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~  in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing,
  ~  software distributed under the License is distributed on an
  ~  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~  KIND, either express or implied.  See the License for the
  ~  specific language governing permissions and limitations
  ~  under the License.
  --%>

<%
    // get required parameters to add a event builder to back end.
    EventBuilderAdminServiceStub stub = EventBuilderUIUtils.getEventBuilderAdminService(config, session, request);
    String eventBuilderName = request.getParameter("eventBuilderName");
    String eventBuilderFilename = request.getParameter("eventBuilderFilename");
    String eventBuilderConfigurationXml = request.getParameter("eventBuilderConfiguration");
    String msg;
    if (eventBuilderName != null) {
        try {
            // add event builder via admin service
            stub.editActiveEventBuilderConfiguration(eventBuilderName, eventBuilderConfigurationXml);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();
        }
    } else if (eventBuilderFilename != null) {
        try {
            // add event builder via admin service
            stub.editInactiveEventBuilderConfiguration(eventBuilderFilename, eventBuilderConfigurationXml);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();
        }
    } else {
        msg = "Did not receive event builder name or event builder file path";
    }
%>  <%=msg%>   <%
%>
