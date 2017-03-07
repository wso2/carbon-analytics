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
<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.event.publisher.ui.EventPublisherUIUtils" %>

<fmt:bundle basename="org.wso2.carbon.event.receiver.ui.i18n.Resources">
    <link type="text/css" href="../eventreceiver/css/eventReceiver.css" rel="stylesheet"/>
    <script type="text/javascript" src="../eventreceiver/js/event_receiver.js"></script>

    <%
        String streamId = request.getParameter("streamNameWithVersion");
        EventStreamAdminServiceStub eventStreamAdminServiceStub = EventPublisherUIUtils.getEventStreamAdminService(config, session, request);
        EventStreamDefinitionDto streamDefinitionDto = eventStreamAdminServiceStub.getStreamDefinitionDto(streamId);
        List<String> attributeList = EventPublisherUIUtils.getAttributeListWithPrefix(streamDefinitionDto);

    %>

    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputMapMapping">
            <td colspan="2" class="middle-header">
                <fmt:message key="map.mapping"/>
            </td>
        </tr>
        <tr name="outputMapMapping">
            <td colspan="2">

                <table class="styledLeft noBorders spacer-bot" id="outputMapPropertiesTable"
                       style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th><fmt:message key="actions"/></th>
                    </thead>
                </table>
                <div class="noDataDiv-plain" id="noOutputMapProperties">
                    <fmt:message key="no.map.properties.defined"/>
                </div>
                <table id="addOutputMapProperties" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message key="property.name"/> :</td>
                        <td>
                            <input type="text" id="outputMapPropName"/>
                        </td>
                        <td class="col-small"><fmt:message key="property.value.of"/> :</td>
                        <td>
                            <select id="outputMapPropValueOf">
                                <% for (String attributeData : attributeList) {
                                    String[] attributeValues = attributeData.split(" ");
                                %>
                                <option value="<%=attributeValues[0]%>"><%=attributeValues[0]%>
                                </option>
                                <% }%>
                            </select>
                        </td>
                        <td><input type="button" class="button" value="<fmt:message key="add"/>"
                                   onclick="addOutputMapProperty()"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>

        </tbody>
    </table>
</fmt:bundle>