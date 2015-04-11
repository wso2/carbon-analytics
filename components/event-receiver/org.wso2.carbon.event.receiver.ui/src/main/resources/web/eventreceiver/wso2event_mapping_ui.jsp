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
<%@ page import="org.wso2.carbon.event.receiver.ui.EventReceiverUIConstants" %>
<%@ page import="org.wso2.carbon.event.receiver.ui.EventReceiverUIUtils" %>
<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.receiver.ui.i18n.Resources">
    <link type="text/css" href="../eventreceiver/css/eventReceiver.css" rel="stylesheet"/>
    <script type="text/javascript" src="../eventreceiver/js/event_receiver.js"></script>

    <%
        String streamId = request.getParameter("streamNameWithVersion");
        EventStreamAdminServiceStub eventStreamAdminServiceStub = EventReceiverUIUtils.getEventStreamAdminService(config, session, request);
        EventStreamDefinitionDto streamDefinitionDto = eventStreamAdminServiceStub.getStreamDefinitionDto(streamId);
    %>

    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr fromElementKey="inputWso2EventMapping">
            <td colspan="2" class="middle-header">
                <fmt:message key="event.receiver.mapping.wso2event"/>
            </td>
        </tr>


        <tr id="metaMappingRow">
            <td colspan="2">
                <h6><fmt:message key="meta.attribute.mapping"/></h6>

                <table id="addMetaEventDataTable" class="normal">
                    <tbody>
                    <%
                        if (streamDefinitionDto.getMetaData() != null) {
                            int counter = 0;
                            for (EventStreamAttributeDto metaAttribute : streamDefinitionDto.getMetaData()) {
                    %>

                    <tr>

                        <td class="col-small">Input Attribute Name :
                        </td>
                        <td>
                            <input type="text" id="metaEventPropertyName_<%=counter%>"
                                    />
                        </td>
                        <td class="col-small">Mapped To :
                        </td>
                        <td>
                            <input type="text" id="metaEventMappedValue_<%=counter%>"
                                   value="<%=EventReceiverUIConstants.PROPERTY_META_PREFIX + metaAttribute.getAttributeName()%>"
                                   readonly="true"/>
                        </td>
                        <td class="col-small">Attribute Type :
                        </td>
                        <td>
                            <input type="text" id="metaEventType_<%=counter%>"
                                   value="<%=metaAttribute.getAttributeType()%>" readonly="true"/>
                        </td>
                    </tr>
                    <%
                            counter++;
                        }
                    } else {
                    %>
                    <div class="noDataDiv-plain" id="noInputMetaEventData">
                        No Meta Attributes to define
                    </div>
                    <%
                        }
                    %>
                    </tbody>
                </table>
            </td>
        </tr>

        <tr id="correlationMappingRow">
            <td colspan="2">
                <h6><fmt:message key="correlation.attribute.mapping"/></h6>

                <table id="addCorrelationEventDataTable" class="normal">
                    <tbody>
                    <%
                        if (streamDefinitionDto.getCorrelationData() != null) {
                            int counter = 0;
                            for (EventStreamAttributeDto correlationAttribute : streamDefinitionDto.getCorrelationData()) {
                    %>

                    <tr>

                        <td class="col-small">Input Attribute Name :
                        </td>
                        <td>
                            <input type="text" id="correlationEventPropertyName_<%=counter%>"
                                    />
                        </td>
                        <td class="col-small">Mapped To :
                        </td>
                        <td>
                            <input type="text" id="correlationEventMappedValue_<%=counter%>"
                                   value="<%=EventReceiverUIConstants.PROPERTY_CORRELATION_PREFIX + correlationAttribute.getAttributeName()%>"
                                   readonly="true"/>
                        </td>
                        <td class="col-small">Attribute Type :
                        </td>
                        <td>
                            <input type="text" id="correlationEventType_<%=counter%>"
                                   value="<%=correlationAttribute.getAttributeType()%>" readonly="true"/>
                        </td>
                    </tr>
                    <%
                            counter++;
                        }
                    } else {
                    %>
                    <div class="noDataDiv-plain" id="noInputCorrelationEventData">
                        No Correlation Attributes to define
                    </div>
                    <%
                        }
                    %>
                    </tbody>
                </table>
            </td>
        </tr>


        <tr id="PayloadMappingRow">
            <td colspan="2">
                <h6><fmt:message key="payload.attribute.mapping"/></h6>

                <table id="addPayloadEventDataTable" class="normal">
                    <tbody>
                    <%
                        if (streamDefinitionDto.getPayloadData() != null) {
                            int counter = 0;
                            for (EventStreamAttributeDto payloadAttribute : streamDefinitionDto.getPayloadData()) {
                    %>

                    <tr>

                        <td class="col-small">Input Attribute Name :
                        </td>
                        <td>
                            <input type="text" id="payloadEventPropertyName_<%=counter%>"
                                    />
                        </td>
                        <td class="col-small">Mapped To :
                        </td>
                        <td>
                            <input type="text" id="payloadEventMappedValue_<%=counter%>"
                                   value="<%=payloadAttribute.getAttributeName()%>" readonly="true"/>
                        </td>
                        <td class="col-small">Attribute Type :
                        </td>
                        <td>
                            <input type="text" id="payloadEventType_<%=counter%>"
                                   value="<%=payloadAttribute.getAttributeType()%>" readonly="true"/>
                        </td>
                    </tr>
                    <%
                            counter++;
                        }
                    } else {
                    %>
                    <div class="noDataDiv-plain" id="noInputPayloadEventData">
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