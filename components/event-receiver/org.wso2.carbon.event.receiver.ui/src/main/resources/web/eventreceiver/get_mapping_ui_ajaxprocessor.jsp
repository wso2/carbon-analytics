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

<%
    String mappingType = request.getParameter("mappingType");
    String streamNameWithVersion = request.getParameter("streamNameWithVersion");
    if (mappingType.equals("wso2event")) {
%>
<jsp:include page="wso2event_mapping_ui.jsp" flush="true">
    <jsp:param name="streamNameWithVersion" value="<%=streamNameWithVersion%>"/>
</jsp:include>
<%
} else if (mappingType.equals("xml")) {
%>
<jsp:include page="xml_mapping_ui.jsp" flush="true">
    <jsp:param name="streamNameWithVersion" value="<%=streamNameWithVersion%>"/>
</jsp:include>
<%
} else if (mappingType.equals("map")) {
%>
<jsp:include page="map_mapping_ui.jsp" flush="true">
    <jsp:param name="streamNameWithVersion" value="<%=streamNameWithVersion%>"/>
</jsp:include>
<%
} else if (mappingType.equals("text")) {
%>
<jsp:include page="text_mapping_ui.jsp" flush="true">
    <jsp:param name="streamNameWithVersion" value="<%=streamNameWithVersion%>"/>
</jsp:include>
<%
} else if (mappingType.equals("json")) {
%>
<jsp:include page="json_mapping_ui.jsp" flush="true">
    <jsp:param name="streamNameWithVersion" value="<%=streamNameWithVersion%>"/>
</jsp:include>
<%
    }
%>