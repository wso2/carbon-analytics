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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.event.receiver.ui.EventReceiverUIUtils" %>
<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto" %>
<%@ page import="java.util.List" %>

<fmt:bundle basename="org.wso2.carbon.event.receiver.ui.i18n.Resources">
    <link type="text/css" href="../eventreceiver/css/eventReceiver.css" rel="stylesheet"/>
    <script type="text/javascript" src="../eventreceiver/js/event_receiver.js"></script>

    <%
        String streamId = request.getParameter("streamNameWithVersion");
        EventStreamAdminServiceStub eventStreamAdminServiceStub = EventReceiverUIUtils.getEventStreamAdminService(config, session, request);
        EventStreamDefinitionDto streamDefinitionDto = eventStreamAdminServiceStub.getStreamDefinitionDto(streamId);

        List<String> attributeList = EventReceiverUIUtils.getAttributeListWithPrefix(streamDefinitionDto);

    %>

    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr fromElementKey="inputJsonMapping">
            <td colspan="2" class="middle-header">
                <fmt:message key="event.receiver.mapping.json"/>
            </td>
        </tr>
        <tr fromElementKey="inputJsonMapping">
            <td colspan="2">

                <h6><fmt:message key="jsonpath.expression.header"/></h6>

                <table id="addJsonpathExprTable" class="normal">
                    <tbody>
                    <%
                        int counter = 0;
                        for (String attributeData : attributeList) {

                            String[] attributeValues = attributeData.split(" ");
                    %>
                    <tr>
                        <td class="col-small"><fmt:message
                                key="event.receiver.property.jsonpath"/> :
                        </td>
                        <td>
                            <input type="text" id="inputPropertyValue_<%=counter%>"/>
                        </td>
                        <td class="col-small"><fmt:message key="event.receiver.property.mappedto"/> :
                        </td>
                        <td>
                            <input type="text" id="inputPropertyName_<%=counter%>"
                                   value="<%=attributeValues[0]%>" readonly="true"/>
                        </td>
                        <td><input type="text" id="inputPropertyType_<%=counter%>"
                                   value="<%=attributeValues[1]%>" readonly="true"/>
                        </td>
                        <td class="col-small"><fmt:message
                                key="event.receiver.property.default"/></td>
                        <td><input type="text" id="inputPropertyDefault_<%=counter%>"/></td>
                    </tr>
                    <%
                            counter++;
                        } %>
                    </tbody>
                </table>
            </td>
        </tr>

        </tbody>
    </table>
</fmt:bundle>