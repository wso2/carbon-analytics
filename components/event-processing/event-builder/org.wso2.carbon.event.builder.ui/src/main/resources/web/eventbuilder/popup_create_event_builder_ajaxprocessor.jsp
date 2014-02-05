<%@ page import="org.wso2.carbon.event.builder.stub.EventBuilderAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.builder.stub.types.EventBuilderPropertyDto" %>
<%@ page import="org.wso2.carbon.event.builder.stub.types.InputEventAdaptorInfoDto" %>
<%@ page import="org.wso2.carbon.event.builder.ui.EventBuilderUIUtils" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

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

<fmt:bundle basename="org.wso2.carbon.event.builder.ui.i18n.Resources">

<link type="text/css" href="../eventbuilder/css/cep.css" rel="stylesheet"/>
<script type="text/javascript" src="../eventbuilder/js/event_builders.js"></script>
<script type="text/javascript" src="../eventbuilder/js/create_event_builder_helper.js"></script>

<div id="middle">
<h2><fmt:message key="create.event.builder"/></h2>

<div id="workArea">
<%
    EventBuilderAdminServiceStub stub = EventBuilderUIUtils.getEventBuilderAdminService(config, session, request);
    InputEventAdaptorInfoDto[] inputEventAdaptorInfoDtos = stub.getInputEventAdaptorInfo();
    EventStreamAdminServiceStub eventStreamAdminStub = EventBuilderUIUtils.getEventStreamAdminService(config,session,request);
    String streamNameWithVersion = request.getParameter("streamNameWithVersion");
    String[] streamNamesWitheVersionArray = eventStreamAdminStub.getStreamNames();
    if(streamNameWithVersion != null) {
        streamNamesWitheVersionArray = new String[] {streamNameWithVersion};
    }
    if (inputEventAdaptorInfoDtos != null) {
%>
<form name="inputForm" action="index.jsp?ordinal=1" method="get" id="addEventBuilder">
<table style="width:100%" id="ebAdd" class="styledLeft">
<thead>
<tr>
    <th><fmt:message key="event.builder.create.header"/></th>
</tr>
</thead>
<tbody>
<tr>
    <td class="formRaw">

        <table id="eventBuilderInputTable" class="normal-nopadding smallTextInput"
               style="width:100%">
            <tbody>

            <tr>
                <td class="leftCol-med">Event Builder Name<span
                        class="required">*</span>
                </td>
                <td><input type="text" name="configName" id="eventBuilderNameId"
                           class="initE"
                           onclick="clearTextIn(this)" onblur="fillTextIn(this)"
                           value=""/>

                    <div class="sectionHelp">
                        <fmt:message key="event.builder.name.tooltip"/>
                    </div>

                </td>
            </tr>
            <tr>
                <td colspan="2"><b><fmt:message key="event.builder.from.tooltip"/></b></td>
            </tr>
            <tr id="eventAdaptorSelectTr">
                <td>Input Event Adaptor<span class="required">*</span></td>
                <!-- The element positioning of the select is important since showMessageConfigProperties uses the first
                     ancestral 'tr' to determine where to insert the message configuration properties on loading ajax -->
                <td class="custom-noPadding" style="padding-left: 0px !important;">
                    <table>
                        <tr>
                            <td>
                                <select name="eventAdaptorNameSelect"
                                        id="eventAdaptorNameSelect"
                                        onchange="showMessageConfigProperties()">
                                    <%
                                        String firstEventName = inputEventAdaptorInfoDtos[0].getInputEventAdaptorName();
                                        for (InputEventAdaptorInfoDto InputEventAdaptorInfoDto : inputEventAdaptorInfoDtos) {
                                    %>
                                    <option value="<%=InputEventAdaptorInfoDto.getInputEventAdaptorName() + "$=" + InputEventAdaptorInfoDto.getInputEventAdaptorType()%>"><%=InputEventAdaptorInfoDto.getInputEventAdaptorName()%>
                                    </option>
                                    <%
                                        }
                                    %>
                                </select>

                                <div class="sectionHelp">
                                    <fmt:message key="input.adaptor.select.tooltip"/>
                                </div>
                            </td>
                            <td id="addEventAdaptorTD" class="custom-noPadding"></td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>

                <% //Input fields for message configuration properties
                    if (firstEventName != null && !firstEventName.isEmpty()) {
                        EventBuilderPropertyDto[] messageConfigurationProperties = stub.getMessageConfigurationProperties(firstEventName);

                        //Need to add other types of properties also here
                        if (messageConfigurationProperties != null) {
                            for (int index = 0; index < messageConfigurationProperties.length; index++) {
                %>

                <td class="leftCol-med">
                    <%=messageConfigurationProperties[index].getDisplayName()%>
                    <%
                        String propertyId = "msgConfigProperty_";
                        if (messageConfigurationProperties[index].getRequired()) {
                            propertyId = "msgConfigProperty_Required_";

                    %>
                    <span class="required">*</span>
                    <%
                        }
                    %>

                </td>
                <%
                    String type = "text";
                    if (messageConfigurationProperties[index].getSecured()) {
                        type = "password";
                    }
                %>
                <td><input type="<%=type%>"
                           name="<%=messageConfigurationProperties[index].getKey()%>"
                           id="<%=propertyId%><%=index%>" class="initE"
                           value="<%= (messageConfigurationProperties[index].getDefaultValue()) != null ? messageConfigurationProperties[index].getDefaultValue() : "" %>"/>
                    <%
                        if (messageConfigurationProperties[index].getHint() != null) {
                    %>
                    <div class="sectionHelp">
                        <%=messageConfigurationProperties[index].getHint()%>
                    </div>
                    <%
                        }
                    %>
                </td>

            </tr>
            <%
                        }
                    }
                }
            %>
            <tr>
                <td colspan="2"><b><fmt:message key="event.builder.mapping.tooltip"/></b>
                </td>
            </tr>
            <tr>
                <td>Input Mapping Type<span class="required">*</span></td>
                <td><select name="inputMappingTypeSelect" id="inputMappingTypeSelect"
                            onchange="loadMappingUiElements()">
                    <%
                        String[] mappingTypeNames = stub.getSupportedInputMappingTypes(firstEventName);
                        String firstMappingTypeName = null;
                        if (mappingTypeNames != null) {
                            firstMappingTypeName = mappingTypeNames[0];
                            for (String mappingTypeName : mappingTypeNames) {
                    %>
                    <option><%=mappingTypeName%>
                    </option>
                    <%
                            }
                        }
                    %>
                </select>

                    <div class="sectionHelp">
                        <fmt:message key="input.mapping.type.tooltip"/>
                    </div>
                </td>
            </tr>
            <tr>
                <td id="mappingUiTd" colspan="2">
                    <%
                        if (firstMappingTypeName != null) {
                            if (firstMappingTypeName.equals("wso2event")) {
                    %>
                    <%@include file="wso2event_mapping_ui.jsp" %>
                    <%
                    } else if (firstMappingTypeName.equals("xml")) {
                    %>
                    <%@include file="xml_mapping_ui.jsp" %>
                    <%
                    } else if (firstMappingTypeName.equals("map")) {
                    %>
                    <%@include file="map_mapping_ui.jsp" %>
                    <%
                    } else if (firstMappingTypeName.equals("text")) {
                    %>
                    <%@include file="text_mapping_ui.jsp" %>
                    <%
                    } else if (firstMappingTypeName.equals("json")) {
                    %>
                    <%@include file="json_mapping_ui.jsp" %>
                    <%
                            }
                        }
                    %>
                </td>
            </tr>
            <tr>
                <td colspan="2"><b><fmt:message key="event.builder.to.tooltip"/></b></td>
            </tr>
            <tr>
                <td>To Stream Name<span class="required">*</span></td>
                <td><select name="streamNameFilter" id="streamNameFilter">
                    <%
                        for (String streamName : streamNamesWitheVersionArray) {
                    %>
                    <option><%=streamName%>
                    </option>
                    <%
                        }
                    %>

                </select>
                    <div class="sectionHelp">
                        <fmt:message key="to.stream.id.tooltip"/>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
<tr>
    <td colspan="2" class="buttonRow">
        <input type="button" value="Add Event Builder"
               onclick="addEventBuilderViaPopup(document.getElementById('addEventBuilder'))"/>
    </td>
</tr>
</tbody>

</table>
</form>
<% } %>
</div>
</div>
</fmt:bundle>