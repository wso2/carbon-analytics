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
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto" %>

<fmt:bundle basename="org.wso2.carbon.event.receiver.ui.i18n.Resources">
    <link type="text/css" href="../eventreceiver/css/eventReceiver.css" rel="stylesheet"/>
    <script type="text/javascript" src="../eventreceiver/js/event_receiver.js"></script>
    <%
        String streamId = request.getParameter("streamNameWithVersion");
        EventStreamAdminServiceStub eventStreamAdminServiceStub = EventReceiverUIUtils.getEventStreamAdminService(config, session, request);
        EventStreamDefinitionDto streamDefinitionDto = eventStreamAdminServiceStub.getStreamDefinitionDto(streamId);
        List<String> attributeList = EventReceiverUIUtils.getAttributeListWithPrefix(streamDefinitionDto);
       String types ="[";
        boolean initial=true;
        if (streamDefinitionDto.getMetaData() != null) {
            for (EventStreamAttributeDto attributeDto : streamDefinitionDto.getMetaData()) {
                if(initial){
                    types +="\""+attributeDto.getAttributeType()+"\"";
                    initial=false;
                }  else {
                    types +=",\""+attributeDto.getAttributeType()+"\"";
                }
            }
        }
        if (streamDefinitionDto.getCorrelationData() != null) {
            for (EventStreamAttributeDto attributeDto : streamDefinitionDto.getCorrelationData()) {
                if(initial){
                    types +="\""+attributeDto.getAttributeType()+"\"";
                    initial=false;
                }  else {
                    types +=",\""+attributeDto.getAttributeType()+"\"";
                }
            }
        }
        if (streamDefinitionDto.getPayloadData() != null) {
            for (EventStreamAttributeDto attributeDto : streamDefinitionDto.getPayloadData()) {
                if(initial){
                    types +="\""+attributeDto.getAttributeType()+"\"";
                    initial=false;
                }  else {
                    types +=",\""+attributeDto.getAttributeType()+"\"";
                }
            }
        }
        types+="]";


    %>

    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr fromElementKey="inputTextMapping">
            <td colspan="2" class="middle-header">
                <fmt:message key="event.receiver.mapping.text"/>
            </td>
        </tr>
        <tr fromElementKey="inputTextMapping">
            <td colspan="2">
                <h6><fmt:message key="regex.definition.header"/></h6>
                <table class="styledLeft noBorders spacer-bot" id="inputRegexDefTable"
                       style="display:none">
                    <thead>
                    <th><fmt:message
                            key="event.receiver.regex.expr"/></th>
                    <th><fmt:message key="event.receiver.mapping.actions"/></th>
                    </thead>
                    <tbody id="inputRegexDefTBody"></tbody>
                </table>
                <div class="noDataDiv-plain" id="noInputRegex">
                    No Regular Expressions Defined
                </div>
                <table id="addRegexDefinition" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message key="event.receiver.regex.expr"/> : <span
                                class="required">*</span>
                        </td>
                        <td>
                            <input type="text" id="inputRegexDef"/>
                        </td>
                        <td><input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addInputRegexDef()"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>

        <tr fromElementKey="inputTextMapping">
            <td colspan="2">

                <h6><fmt:message key="text.mapping.header"/></h6>
                <table class="styledLeft noBorders spacer-bot"
                       id="inputTextMappingTable" style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message
                            key="event.receiver.property.regex"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.receiver.property.valueof"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.receiver.property.type"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.receiver.property.default"/></th>
                    <th><fmt:message key="event.receiver.mapping.actions"/></th>
                    </thead>
                    <tbody id="inputTextMappingTBody"></tbody>
                </table>
                <div class="noDataDiv-plain" id="noInputProperties">
                    No Text Mappings Available
                </div>
                <div id="streamMapping" mapping='<%=types%>' style="display:none">
                </div>
                <table id="addTextMappingTable" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message
                                key="event.receiver.property.regex"/> :
                            <select id="inputPropertyValue">
                                <option value="">No regular expression defined</option>
                            </select>
                        </td>
                        <td class="col-small"><fmt:message key="event.receiver.property.valueof"/> :
                        </td>
                        <td>
                            <select id="inputPropertyName" onchange="updateAttributeType()">
                                <% for (String attributeData : attributeList) {
                                    String[] attributeValues = attributeData.split(" ");
                                %>
                                <option value="<%=attributeValues[0]%>"><%=attributeValues[0]%>
                                </option>
                                <% }%>
                            </select>
                        </td>
                        <td><fmt:message key="event.receiver.property.type"/>:
                            <select id="inputPropertyType" disabled="disabled">
                                <option value="int">int</option>
                                <option value="long">long</option>
                                <option value="double">double</option>
                                <option value="float">float</option>
                                <option value="string">string</option>
                                <option value="boolean">boolean</option>
                            </select>
                        </td>
                        <td class="col-small"><fmt:message
                                key="event.receiver.property.default"/></td>
                        <td><input type="text" id="inputPropertyDefault"/></td>
                        <td><input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addInputTextProperty()"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        </tbody>
    </table>
</fmt:bundle>