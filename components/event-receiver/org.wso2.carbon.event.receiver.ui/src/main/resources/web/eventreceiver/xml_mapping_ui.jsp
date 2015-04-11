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
        <tr>
            <td colspan="2" class="middle-header">
                <fmt:message key="event.receiver.mapping.xml"/>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <h6><fmt:message key="xpath.prefix.header"/></h6>
                <table class="styledLeft noBorders spacer-bot" id="inputXpathPrefixTable"
                       style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message
                            key="event.receiver.xpath.prefix"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.receiver.xpath.ns"/></th>
                    <th><fmt:message key="event.receiver.mapping.actions"/></th>
                    </thead>
                    <tbody id="inputXpathPrefixTBody"></tbody>
                </table>
                <div class="noDataDiv-plain" id="noInputPrefixes">
                    <fmt:message key="event.receiver.property.no.xpath.definitions"/>
                </div>
                <table id="addXpathDefinition" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message key="event.receiver.xpath.prefix"/> :
                        </td>
                        <td>
                            <input type="text" id="inputPrefixName"/>
                        </td>
                        <td class="col-small"><fmt:message
                                key="event.receiver.xpath.ns"/> :
                        </td>
                        <td>
                            <input type="text" id="inputXpathNs"/>
                        </td>
                        <td><input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addInputXpathDef()"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>


        <tr>
            <td colspan="2">
                <table class="normal">
                    <tbody>
                    <tr>
                        <td><fmt:message key="event.receiver.parentselector.xpath"/>:</td>
                        <td><input type="text" id="parentSelectorXpath"></td>
                    </tr>
                    </tbody>
                </table>
        </tr>

        <tr>
            <td colspan="2">

                <h6><fmt:message key="xpath.expression.header"/></h6>

                    <%--<div class="noDataDiv-plain" id="noInputProperties">--%>
                    <%--No XPath expressions properties Defined--%>
                    <%--</div>--%>
                <table id="addXpathExprTable" class="normal">
                    <tbody>
                    <%
                        int counter = 0;
                        for (String attributeData : attributeList) {
                            String attributeValues[] = attributeData.split(" ");
                    %>
                    <tr>
                        <td class="col-small"><fmt:message
                                key="event.receiver.property.xpath"/>:
                        </td>
                        <td>
                            <input type="text" id="inputPropertyValue_<%=counter%>"/>
                        </td>
                        <td class="col-small"><fmt:message key="event.receiver.property.mappedto"/>:
                        </td>
                        <td>
                            <input type="text" id="inputPropertyName_<%=counter%>"
                                   value="<%=attributeValues[0]%>" readonly="true"/>
                        </td>
                        <td><fmt:message key="event.receiver.property.type"/>:</td>
                        <td>
                            <input type="text" id="inputPropertyType_<%=counter%>"
                                   value="<%=attributeValues[1]%>" readonly="true"/>
                        </td>
                        <td class="col-small"><fmt:message
                                key="event.receiver.property.default"/>:
                        </td>
                        <td><input type="text" id="inputPropertyDefault_<%=counter%>"/></td>
                    </tr>
                    <% counter++;
                    } %>
                    </tbody>
                </table>
            </td>
        </tr>

        </tbody>
    </table>
</fmt:bundle>