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
<%@ page import="org.wso2.carbon.event.receiver.stub.EventReceiverAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.receiver.ui.EventReceiverUIUtils" %>
<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.receiver.stub.types.DetailInputAdapterPropertyDto" %>
<%@ page import="org.wso2.carbon.event.receiver.stub.types.InputAdapterConfigurationDto" %>
<%@ page import="java.util.Arrays" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.receiver.ui.i18n.Resources">
<script type="text/javascript" src="../eventreceiver/js/event_receiver.js"></script>
<script type="text/javascript" src="../eventreceiver/js/create_eventReceiver_helper.js"></script>
<script type="text/javascript" src="../eventreceiver/js/registry-browser.js"></script>

<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<link rel="stylesheet" href="../eventreceiver/css/eventReceiver.css"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript"
        src="../eventreceiver/js/create_eventReceiver_helper.js"></script>


<div id="custom_dcontainer" style="display:none"></div>
<div id="middle">
<h2><fmt:message key="title.event.receiver.create"/></h2>

<div id="workArea">

<form name="inputForm" action="#" method="post" id="addEventReceiver">
<table style="width:100%" id="eventReceiverAdd" class="styledLeft">
<%
    EventReceiverAdminServiceStub eventReceiverAdminServiceStub = EventReceiverUIUtils.getEventReceiverAdminService(config, session, request);
    String[] inputAdapterTypes = eventReceiverAdminServiceStub.getAllInputAdapterTypes();

    String firstEventAdapterType = null;
    if (inputAdapterTypes != null && inputAdapterTypes.length > 0) {
        firstEventAdapterType = inputAdapterTypes[0];
    }

    String streamId = request.getParameter("streamId");
    String redirectPage = request.getParameter("redirectPage");

    EventStreamAdminServiceStub eventStreamAdminServiceStub = EventReceiverUIUtils.getEventStreamAdminService(config, session, request);
    String[] streamIds = eventStreamAdminServiceStub.getStreamNames();
    if (streamId == null && streamIds != null && streamIds.length > 0) {
        streamId = streamIds[0];
    }


    if (streamId != null && firstEventAdapterType != null) {
%>
<br/>
<thead>
<tr>
    <th><fmt:message key="title.event.receiver.details"/></th>
</tr>
</thead>
<tbody>
<tr>
<td class="formRaw">
<table id="eventReceiverInputTable" class="normal-nopadding"
       style="width:100%">
<tbody>

<tr>
    <td class="leftCol-med"><fmt:message key="event.receiver.name"/><span class="required">*</span>
    </td>
    <td><input type="text" name="eventReceiverName" id="eventReceiverId"
               class="initE"

               value=""
               style="width:75%"/>

        <div class="sectionHelp">
            <fmt:message key="event.receiver.name.help"/>
        </div>
    </td>
</tr>

<tr>
    <td colspan="2">
        <b><fmt:message key="from.heading"/></b>
    </td>
</tr>
<tr>
    <td><fmt:message key="event.adapter.type"/><span class="required">*</span></td>
    <td>
        <table>
            <td class="custom-noPadding" width="60%"><select name="eventAdapterTypeFilter"
                                                             onchange="loadEventAdapterRelatedProperties('<fmt:message
                                                                     key="to.heading"/>','<fmt:message key="properties.heading"/>')"
                                                             id="eventAdapterTypeFilter">
                <%
                    for (String inputAdapterType : inputAdapterTypes) {
                %>
                <option value="<%=inputAdapterType%>"><%=inputAdapterType%>
                </option>
                <%
                    }

                %>

            </select>

                <div class="sectionHelp">
                    <fmt:message key="event.adapter.type.help"/>
                </div>
            </td>
            <td width="40%" id="addInputEventAdapterTD" class="custom-noPadding"></td>
        </table>
    </td>
</tr>

<tr id="eventReceiverUsageTipsRowId"><td hidden><fmt:message key="event.adapter.usage.tips"/></td>
    <td hidden></td>
</tr>

<%
    InputAdapterConfigurationDto inputAdapterConfigurationDto = eventReceiverAdminServiceStub.getInputAdapterConfigurationSchema(firstEventAdapterType);
    if (inputAdapterConfigurationDto != null) {
%>
<%
    DetailInputAdapterPropertyDto[] eventAdapterProperties = inputAdapterConfigurationDto.getInputEventAdapterProperties();
    if (eventAdapterProperties != null && eventAdapterProperties.length > 0) {
%>

<tr>
    <td>
        <b><i><span style="color: #666666; "><fmt:message key="properties.heading"/></span></i></b>
    </td>
</tr>
<%
    for (int index = 0; index < eventAdapterProperties.length; index++) {
%>
<tr>
    <td class="leftCol-med"><%=eventAdapterProperties[index].getDisplayName()%>
        <%
            String propertyId = "property_";
            if (eventAdapterProperties[index].getRequired()) {
                propertyId = "property_Required_";
        %>
        <span class="required">*</span>
        <%
            }
        %>
    </td>
    <%
        String type = "text";
        if (eventAdapterProperties[index].getSecured()) {
            type = "password";
        }
    %>
    <td>
        <div class=inputFields>
            <%
                if (eventAdapterProperties[index].getOptions()[0] != null) {
            %>
            <select name="<%=eventAdapterProperties[index].getKey()%>"
                    id="<%=propertyId%><%=index%>">
                <%
                    for (String property : eventAdapterProperties[index].getOptions()) {
                        if (property.equals(eventAdapterProperties[index].getDefaultValue())) {
                %>
                <option selected="selected"><%=property%>
                </option>
                <% } else { %>
                <option><%=property%>
                </option>
                <% }
                } %>
            </select>

            <% } else { %>
            <input type="<%=type%>"
                   name="<%=eventAdapterProperties[index].getKey()%>"
                   id="<%=propertyId%><%=index%>" class="initE"
                   style="width:75%"
                   value="<%= (eventAdapterProperties[index].getDefaultValue()) != null ? eventAdapterProperties[index].getDefaultValue() : "" %>"
                    />

            <% }

                if (eventAdapterProperties[index].getHint() != null) { %>
            <div class="sectionHelp">
                <%=eventAdapterProperties[index].getHint()%>
            </div>
            <% } %>
        </div>
    </td>
</tr>
<%
        }
    }
%>
<tr>
    <td>
        <b><fmt:message key="to.heading"/></b>
    </td>
</tr>
<tr>
    <td><fmt:message key="event.stream.name"/><span class="required">*</span></td>
    <td><select name="streamIdFilter"
                onchange="loadMappingUiElements()" id="streamIdFilter">
        <%
            if (streamIds != null) {
                Arrays.sort(streamIds);
                for (String aStreamId : streamIds) {
        %>
        <option><%=aStreamId%>
        </option>
        <%
                }
            }
        %>

    </select>

        <div class="sectionHelp">
            <fmt:message key="event.stream.name.help"/>
        </div>
    </td>

</tr>

<tr>
    <td colspan="2">
        <b><fmt:message key="mapping.heading"/></b>
    </td>
</tr>

<tr>
    <td><fmt:message key="message.format"/><span class="required">*</span></td>
    <td><select name="mappingTypeFilter"
                onchange="loadMappingUiElements()" id="mappingTypeFilter">
        <%
            String[] messageFormats = inputAdapterConfigurationDto.getSupportedMessageFormats();

            String firstMappingTypeName = null;
            if (messageFormats != null) {
                firstMappingTypeName = messageFormats[0];
                for (String mappingType : messageFormats) {
        %>
        <option><%=mappingType%>
        </option>
        <%
                }

        %>

    </select>

        <div class="sectionHelp">
            <fmt:message key="message.format.help"/>
        </div>
    </td>

</tr>


<tr>
    <td><a href="#"
           style="background-image:url(images/add.gif);"
           class="icon-link" onclick="handleAdvancedMapping()">
        Advanced
    </a></td>
</tr>
<tr>

    <td id="mappingUiTd" colspan="2">
        <div id="outerDiv" style="display:none">

            <%
                if (firstMappingTypeName != null) {
                    if (firstMappingTypeName.equals("wso2event")) {



            %>
            <jsp:include page="wso2event_mapping_ui.jsp" flush="true">
                <jsp:param name="streamNameWithVersion"
                           value="<%=streamId%>"/>
            </jsp:include>
            <%
            } else if (firstMappingTypeName.equals("xml")) {
            %>
            <jsp:include page="xml_mapping_ui.jsp" flush="true">
                <jsp:param name="streamNameWithVersion"
                           value="<%=streamId%>"/>
            </jsp:include>
            <%
            } else if (firstMappingTypeName.equals("map")) {
            %>
            <jsp:include page="map_mapping_ui.jsp" flush="true">
                <jsp:param name="streamNameWithVersion"
                           value="<%=streamId%>"/>
            </jsp:include>
            <%
            } else if (firstMappingTypeName.equals("text")) {
            %>
            <jsp:include page="text_mapping_ui.jsp" flush="true">
                <jsp:param name="streamNameWithVersion"
                           value="<%=streamId%>"/>
            </jsp:include>
            <%
            } else if (firstMappingTypeName.equals("json")) {
            %>
            <jsp:include page="json_mapping_ui.jsp" flush="true">
                <jsp:param name="streamNameWithVersion"
                           value="<%=streamId%>"/>
            </jsp:include>
            <%
                            }
                        }
                    }
                }
            %>
        </div>
    </td>

</tr>

</tbody>
</table>
</td>
</tr>
<tr>
    <td class="buttonRow">
        <input type="button" value="<fmt:message key="add.event.receiver"/>"
               onclick="addEventReceiverViaPopup(document.getElementById('addEventReceiver') ,document.getElementById('streamIdFilter')[document.getElementById('streamIdFilter').selectedIndex].value)"/>
    </td>
</tr>
</tbody>
<% } else { %>
<tbody>
<tr>
    <td class="formRaw">
        <table id="noEventReceiverInputTable" class="normal-nopadding"
               style="width:100%">
            <tbody>
                <tr>
                <%if(streamId == null){%>

                    <td class="leftCol-med" colspan="2">
                        <span style="float: left; position: relative; margin-top: 5px;">
                            <fmt:message key="event.receiver.error.no.stream"/>
                        </span>
                        <a onclick="createImportedStreamDefinition()",
                           style="background-image:url(images/add.gif);"
                           class="icon-link">
                            Add Event Stream
                        </a>
                    </td>


                <%}else{%>
                    <td class="leftCol-med"  colspan="2">
                        <fmt:message key="event.receiver.error.no.input.adapter"/>
                    </td>

                <%}%>
                </tr>
            </tbody>
        </table>
    </td>
</tr>
</tbody>
<% } %>
</table>
</form>
</div>
</div>
</fmt:bundle>