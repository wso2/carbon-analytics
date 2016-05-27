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
        String attributeMappingDTOArray = request.getParameter("attributeMappingDTOArray");
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
    <table style="width:100%">
        <tbody>

            <%--Map Meta Data--%>
        <tr>
            <td colspan="6">
                <h6><fmt:message key="meta.attribute.mapping"/></h6>
            </td>
        </tr>
        <%
            int metaCounter = 0;
            if (toStreamDefinitionDto.getMetaData() != null) {
                for (EventStreamAttributeDto metaAttribute : toStreamDefinitionDto.getMetaData()) {
        %>

        <tr id="metaMappingRow_<%=metaCounter%>">
            <td>Mapped From :
            </td>
            <td>
                <select id="metaEventMappingValue_<%=index%><%=metaCounter%>">
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
                <input type="text" id="metaEventMappedValue_<%=index%><%=metaCounter%>"
                       value="<%=ExecutionManagerUIConstants.PROPERTY_META_PREFIX + metaAttribute.getAttributeName()%>"
                       readonly="true"/>
            </td>
            <td>Attribute Type :
            </td>
            <td>
                <input type="text" id="metaEventType_<%=index%><%=metaCounter%>"
                       value="<%=metaAttribute.getAttributeType()%>" readonly="true"/>
            </td>
        </tr>
        <%
                metaCounter++;
            }

        } else {
        %>
            <tr>
                <td colspan="6">
                    <div id="noInputMetaEventData">
                        No Meta Attributes to define
                    </div>
                </td>
            </tr>
        <%
            }
        %>

            <%--Map Correlation Data--%>
            <tr>
                <td colspan="6">
                    <h6><fmt:message key="correlation.attribute.mapping"/></h6>
                </td>
            </tr>
        <%
            int correlationCounter = 0;
            if (toStreamDefinitionDto.getCorrelationData() != null) {
                for (EventStreamAttributeDto correlationAttribute : toStreamDefinitionDto.getCorrelationData()) {
        %>

        <tr id="correlationMappingRow_<%=correlationCounter%>">
            <td>Mapped From :
            </td>
            <td>
                <select id="correlationEventMappingValue_<%=index%><%=correlationCounter%>">
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
                <input type="text" id="correlationEventMappedValue_<%=index%><%=correlationCounter%>"
                       value="<%=ExecutionManagerUIConstants.PROPERTY_CORRELATION_PREFIX + correlationAttribute.getAttributeName()%>"
                       readonly="true"/>
            </td>
            <td>Attribute Type :
            </td>
            <td>
                <input type="text" id="correlationEventType_<%=index%><%=correlationCounter%>"
                       value="<%=correlationAttribute.getAttributeType()%>" readonly="true"/>
            </td>
        </tr>
        <%
                correlationCounter++;
            }
        } else {
        %>
            <tr>
                <td colspan="6">
                    <div id="noInputCorrelationEventData">
                        No Correlation Attributes to define
                    </div>
                </td>
            </tr>
        <%
            }
        %>

            <%--Map Payload Data--%>
            <tr>
                <td colspan="6">
                    <h6><fmt:message key="payload.attribute.mapping"/></h6>
                </td>
            </tr>
        <%
            int payloadCounter = 0;
            if (toStreamDefinitionDto.getPayloadData() != null) {
                for (EventStreamAttributeDto payloadAttribute : toStreamDefinitionDto.getPayloadData()) {
        %>

        <tr id="payloadMappingRow_<%=payloadCounter%>">
            <td>Mapped From :
            </td>
            <td>
                <select id="payloadEventMappingValue_<%=index%><%=payloadCounter%>">
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
                <input type="text" id="payloadEventMappedValue_<%=index%><%=payloadCounter%>"
                       value="<%=payloadAttribute.getAttributeName()%>" readonly="true"/>
            </td>
            <td>Attribute Type :
            </td>
            <td>
                <input type="text" id="payloadEventType_<%=index%><%=payloadCounter%>"
                       value="<%=payloadAttribute.getAttributeType()%>" readonly="true"/>
            </td>
        </tr>
        <%
                payloadCounter++;
            }
        } else {
        %>
            <tr>
                <td colspan="6">
                    <div id="noInputPayloadEventData">
                        No Payload Attributes to define
                    </div>
                </td>
            </tr>
        <%
            }
        %>
        <div style="display: none">
            <input type="text" id="metaRows"
                   value="<%=metaCounter%>"/>
            <input type="text" id="correlationRows"
                   value="<%=correlationCounter%>"/>
            <input type="text" id="payloadRows"
                   value="<%=payloadCounter%>"/>
        </div>

        </tbody>
    </table>
</fmt:bundle>