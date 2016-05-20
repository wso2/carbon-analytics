<%--
  ~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto" %>
<%@ page import="org.wso2.carbon.event.execution.manager.ui.ExecutionManagerUIUtils" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="org.wso2.carbon.event.execution.manager.ui.ExecutionManagerUIConstants" %>
<%@ page import="org.apache.commons.lang.ArrayUtils" %>
<%@ page import="java.util.ArrayList" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.execution.manager.ui.i18n.Resources">

    <%
        String fromStreamId = request.getParameter("fromStreamNameWithVersion");
        String toStreamId = request.getParameter("toStreamNameWithVersion");
        String index = request.getParameter("index");
        EventStreamAdminServiceStub eventStreamAdminServiceStub = ExecutionManagerUIUtils.getEventStreamAdminService(config, session, request);
        EventStreamDefinitionDto toStreamDefinitionDto = eventStreamAdminServiceStub.getStreamDefinitionDto(toStreamId);
        EventStreamDefinitionDto fromStreamDefinitionDto = eventStreamAdminServiceStub.getStreamDefinitionDto(fromStreamId);

        ArrayList<EventStreamAttributeDto> fromStreamAttributeArray = new ArrayList<EventStreamAttributeDto>();
        //get meta data
        if (fromStreamDefinitionDto.getMetaData() != null) {
            for (EventStreamAttributeDto fromStreamMetaAttribute : fromStreamDefinitionDto.getMetaData()) {
                fromStreamAttributeArray.add(fromStreamMetaAttribute);
            }
        }
        //get correlation data
        if (fromStreamDefinitionDto.getCorrelationData() != null) {
            for (EventStreamAttributeDto fromStreamCorrelationAttribute : fromStreamDefinitionDto.getCorrelationData()) {
                fromStreamAttributeArray.add(fromStreamCorrelationAttribute);
            }
        }
        //get payload data
        if (fromStreamDefinitionDto.getPayloadData() != null) {
            for (EventStreamAttributeDto fromStreamPayloadAttribute : fromStreamDefinitionDto.getPayloadData()) {
                fromStreamAttributeArray.add(fromStreamPayloadAttribute);
            }
        }
    %>

    <h4><fmt:message
            key='template.stream.attribute.mapping.header.text'/></h4>
    <table>
        <tbody>

            <%--Map Meta Data--%>
        <tr id="metaMappingRow">
            <td colspan="2">
                <h6><fmt:message key="meta.attribute.mapping"/></h6>
                <table id="addMetaEventDataTable_<%=index%>">
                    <tbody>
                    <%
                        if (toStreamDefinitionDto.getMetaData() != null) {
                            int counter = 0;
                            for (EventStreamAttributeDto metaAttribute : toStreamDefinitionDto.getMetaData()) {
                    %>

                    <tr>
                        <td>Mapped From :
                        </td>
                        <td>
                            <select id="metaEventMappingValue_<%=index%><%=counter%>">
                                <%
                                    boolean isMatchingAttributeType = false;
                                    for (EventStreamAttributeDto fromStreamAttribute : fromStreamAttributeArray) {
                                        if (fromStreamAttribute.getAttributeType().equals(metaAttribute.getAttributeType())) {
                                            isMatchingAttributeType = true;
                                %>
                                <option><%=fromStreamAttribute.getAttributeName()%>
                                </option>
                                <%
                                        }
                                    }
                                    if (isMatchingAttributeType == false) {
                                %>
                                <option>No matching attribute type to map</option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                        <td>Mapped To :
                        </td>
                        <td>
                            <input type="text" id="metaEventMappedValue_<%=index%><%=counter%>"
                                   value="<%=ExecutionManagerUIConstants.PROPERTY_META_PREFIX + metaAttribute.getAttributeName()%>"
                                   readonly="true"/>
                        </td>
                        <td>Attribute Type :
                        </td>
                        <td>
                            <input type="text" id="metaEventType_<%=index%><%=counter%>"
                                   value="<%=metaAttribute.getAttributeType()%>" readonly="true"/>
                        </td>
                    </tr>
                    <%
                            counter++;
                        }
                    } else {
                    %>
                    <div id="noInputMetaEventData">
                        No Meta Attributes to define
                    </div>
                    <%
                        }
                    %>
                    </tbody>
                </table>
            </td>
        </tr>

            <%--Map Correlation Data--%>
        <tr id="correlationMappingRow">
            <td colspan="2">
                <h6><fmt:message key="correlation.attribute.mapping"/></h6>
                <table id="addCorrelationEventDataTable_<%=index%>">
                    <tbody>
                    <%
                        if (toStreamDefinitionDto.getCorrelationData() != null) {
                            int counter = 0;
                            for (EventStreamAttributeDto correlationAttribute : toStreamDefinitionDto.getCorrelationData()) {
                    %>

                    <tr>
                        <td>Mapped From :
                        </td>
                        <td>
                            <select id="correlationEventMappingValue_<%=index%><%=counter%>">
                                <%
                                    boolean isMatchingAttributeType = false;
                                    for (EventStreamAttributeDto fromStreamAttribute : fromStreamAttributeArray) {
                                        if (fromStreamAttribute.getAttributeType().equals(correlationAttribute.getAttributeType())) {
                                            isMatchingAttributeType = true;
                                %>
                                <option><%=fromStreamAttribute.getAttributeName()%>
                                </option>
                                <%
                                        }
                                    }
                                    if (isMatchingAttributeType == false) {
                                %>
                                <option>No matching attribute type to map</option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                        <td>Mapped To :
                        </td>
                        <td>
                            <input type="text" id="correlationEventMappedValue_<%=index%><%=counter%>"
                                   value="<%=ExecutionManagerUIConstants.PROPERTY_CORRELATION_PREFIX + correlationAttribute.getAttributeName()%>"
                                   readonly="true"/>
                        </td>
                        <td>Attribute Type :
                        </td>
                        <td>
                            <input type="text" id="correlationEventType_<%=index%><%=counter%>"
                                   value="<%=correlationAttribute.getAttributeType()%>" readonly="true"/>
                        </td>
                    </tr>
                    <%
                            counter++;
                        }
                    } else {
                    %>
                    <div id="noInputPayloadEventData">
                        No Correlation Attributes to define
                    </div>
                    <%
                        }
                    %>
                    </tbody>
                </table>
            </td>
        </tr>

            <%--Map Payload Data--%>
        <tr id="PayloadMappingRow">
            <td colspan="2">
                <h6><fmt:message key="payload.attribute.mapping"/></h6>
                <table id="addPayloadEventDataTable_<%=index%>">
                    <tbody>
                    <%
                        if (toStreamDefinitionDto.getPayloadData() != null) {
                            int counter = 0;
                            for (EventStreamAttributeDto payloadAttribute : toStreamDefinitionDto.getPayloadData()) {
                    %>

                    <tr>
                        <td>Mapped From :
                        </td>
                        <td>
                            <select id="payloadEventMappingValue_<%=index%><%=counter%>">
                                <%
                                    boolean isMatchingAttributeType = false;
                                    for (EventStreamAttributeDto fromStreamAttribute : fromStreamAttributeArray) {
                                        if (fromStreamAttribute.getAttributeType().equals(payloadAttribute.getAttributeType())) {
                                            isMatchingAttributeType = true;
                                %>
                                <option><%=fromStreamAttribute.getAttributeName()%>
                                </option>
                                <%
                                        }
                                    }
                                    if (isMatchingAttributeType == false) {
                                %>
                                <option>No matching attribute type to map</option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                        <td>Mapped To :
                        </td>
                        <td>
                            <input type="text" id="payloadEventMappedValue_<%=index%><%=counter%>"
                                   value="<%=payloadAttribute.getAttributeName()%>" readonly="true"/>
                        </td>
                        <td>Attribute Type :
                        </td>
                        <td>
                            <input type="text" id="payloadEventType_<%=index%><%=counter%>"
                                   value="<%=payloadAttribute.getAttributeType()%>" readonly="true"/>
                        </td>
                    </tr>
                    <%
                            counter++;
                        }
                    } else {
                    %>
                    <div id="noInputPayloadEventData">
                        No Payload Attributes to define
                    </div>
                    <%
                        }
                    %>
                    </tbody>
                </table>
            </td>
        </tr>
        </tbody>
    </table>
</fmt:bundle>