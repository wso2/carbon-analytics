<%@ page import="org.wso2.carbon.event.builder.stub.EventBuilderAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.builder.stub.types.EventBuilderConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.builder.stub.types.EventBuilderPropertyDto" %>
<%@ page import="org.wso2.carbon.event.builder.ui.EventBuilderUIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
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

<carbon:breadcrumb
        label="event.builder.details"
        resourceBundle="org.wso2.carbon.event.builder.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<link type="text/css" href="css/cep.css" rel="stylesheet"/>
<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../eventbuilder/js/subscriptions.js"></script>
<script type="text/javascript" src="../eventbuilder/js/eventing_utils.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../eventbuilder/js/event_builders.js"></script>

<div id="middle">
<h2>Event Builder Details</h2>

<div id="workArea">
<%
    String eventBuilderName = request.getParameter("eventBuilderName");
    EventBuilderAdminServiceStub stub = EventBuilderUIUtils.getEventBuilderAdminService(config, session, request);
    if (eventBuilderName != null) {
        EventBuilderConfigurationDto eventBuilderConfigurationDto = stub.getActiveEventBuilderConfiguration(eventBuilderName);
        if (eventBuilderConfigurationDto != null) {
            String eventAdaptorName = eventBuilderConfigurationDto.getInputEventAdaptorName();
%>
<form name="inputForm" action="index.jsp?ordinal=1" method="get" id="addEventBuilder">
<table style="width:100%" id="ebAdd" class="styledLeft">
<thead>
<tr>
    <th><fmt:message key="event.builder.details.header"/></th>
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
               value="<%=eventBuilderConfigurationDto.getEventBuilderConfigName()%>"
               disabled/>

        <div class="sectionHelp">
            <fmt:message key="event.builder.name.tooltip"/>
        </div>

    </td>
</tr>
<tr>
    <td colspan="2"><b><fmt:message key="event.builder.from.tooltip"/></b></td>
</tr>
<tr>
    <td>Input Event Adaptor<span class="required">*</span></td>
    <td><select name="eventAdaptorNameSelect"
                id="eventAdaptorNameSelect" disabled>
        <option><%=eventAdaptorName%>
        </option>
    </select>

        <div class="sectionHelp">
            <fmt:message key="input.adaptor.select.tooltip"/>
        </div>
    </td>

</tr>
<tr>

    <% //Input fields for message configuration properties
        if (eventAdaptorName != null && !eventAdaptorName.isEmpty()) {
            EventBuilderPropertyDto[] messageConfigurationProperties = stub.getMessageConfigurationPropertiesWithValue(eventBuilderName);

            if (messageConfigurationProperties != null) {
                for (int index = 0; index < messageConfigurationProperties.length; index++) {
                    EventBuilderPropertyDto msgConfigProperty = messageConfigurationProperties[index];
    %>

    <td class="leftCol-med">
        <%=msgConfigProperty.getDisplayName()%>
        <%
            String propertyId = "msgConfigProperty_";
            if (msgConfigProperty.getRequired()) {
                propertyId = "msgConfigProperty_Required_";

        %>
        <span class="required">*</span>
        <%
            }
        %>

    </td>
    <%
        String type = "text";
        if (msgConfigProperty.getSecured()) {
            type = "password";
        }
    %>
    <td><input type="<%=type%>"
               name="<%=msgConfigProperty.getKey()%>"
               id="<%=propertyId%><%=index%>" class="initE"
               value="<%=msgConfigProperty.getValue() %>"
               disabled/>
        <%
            if (msgConfigProperty.getHint() != null) {
        %>
        <div class="sectionHelp">
            <%=msgConfigProperty.getHint()%>
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
                disabled>
        <%
            String mappingType = eventBuilderConfigurationDto.getInputMappingType();
            if (mappingType != null) {
        %>
        <option><%=mappingType%>
        </option>
        <%
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
<table class="styledLeft noBorders spacer-bot"
       style="width:100%">
<tbody>
<%
    if (mappingType != null) {
        if (mappingType.equals("wso2event")) {
%>
<tr fromElementKey="inputWso2EventMapping">
    <td colspan="2" class="middle-header">
        <fmt:message key="event.builder.mapping.wso2event"/>
    </td>
</tr>
<%
    boolean customMappingEnabled = false;
    for (EventBuilderPropertyDto eventBuilderPropertyDto : eventBuilderConfigurationDto.getEventBuilderProperties()) {
        if (eventBuilderPropertyDto.getKey().startsWith("specific_customMappingValue")) {
            customMappingEnabled = eventBuilderPropertyDto.getValue().equalsIgnoreCase("enable");
%>
<tr>
    <td class="col-small">
        <fmt:message key="event.builder.custommapping.enabled"/>
    </td>
    <td>
        <% if(customMappingEnabled) { %>
        <input name="customMapping" type="radio" value="enable"
               onclick="enableMapping(true);" checked disabled>Enable</input>
        <input name="customMapping" type="radio" value="disable" onclick="enableMapping(false);" disabled>Disable</input>
        <% } else{     %>
        <input name="customMapping" type="radio" value="enable"
               onclick="enableMapping(true);" disabled>Enable</input>
        <input name="customMapping" type="radio" value="disable" onclick="enableMapping(false);" checked disabled>Disable</input>

        <% } %>
    </td>
</tr>
<%
        }
    }
    if (customMappingEnabled) {
%>

<tr fromElementKey="inputWso2EventMapping">
    <td colspan="2">

        <h6><fmt:message key="wso2event.mapping.header"/></h6>
        <table class="styledLeft noBorders spacer-bot"
               id="inputWso2EventDataTable">
            <thead>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.name"/></th>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.inputtype"/></th>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.valueof"/></th>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.type"/></th>
            </thead>
            <tbody>
            <%
                for (EventBuilderPropertyDto eventBuilderPropertyDto : eventBuilderConfigurationDto.getEventBuilderProperties()) {
                    if (eventBuilderPropertyDto.getKey().endsWith("_mapping") && !eventBuilderPropertyDto.getKey().startsWith("specific_")) {
            %>
            <tr id="mappingRow">
                <td class="property-names"><%=eventBuilderPropertyDto.getDisplayName()%>
                </td>
                <td class="property-names">
                    <% if (eventBuilderPropertyDto.getKey().startsWith("meta_")) { %>
                    <%="meta"%>
                    <% } else if (eventBuilderPropertyDto.getKey().startsWith("correlation_")) { %>
                    <%="correlation"%>
                    <% } else { %>
                    <%="payload"%>
                    <% } %>
                </td>
                <td class="property-names"><%=eventBuilderPropertyDto.getValue()%></td>
                <td class="property-names"><%=eventBuilderPropertyDto.getPropertyType()%>
                </td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    </td>
</tr>
<%
    }
} else if (mappingType.equals("xml")) {
%>
<tr fromElementKey="inputXmlMapping">
    <td colspan="2" class="middle-header">
        <fmt:message key="event.builder.mapping.xml"/>
    </td>
</tr>
<tr fromElementKey="inputXmlMapping">
    <td colspan="2">

        <h6><fmt:message key="xpath.prefix.header"/></h6>
        <table class="styledLeft noBorders spacer-bot" id="inputXpathPrefixTable">
            <thead>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.xpath.prefix"/></th>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.xpath.ns"/></th>
            </thead>
            <tbody id="inputXpathPrefixTBody">
            <%
                for (EventBuilderPropertyDto eventBuilderPropertyDto : eventBuilderConfigurationDto.getEventBuilderProperties()) {
                    if (eventBuilderPropertyDto.getKey().endsWith("_mapping") && eventBuilderPropertyDto.getKey().startsWith("prefix_")) {
            %>
            <tr>
                <td class="property-names"><%=eventBuilderPropertyDto.getDisplayName()%>
                </td>
                <td class="property-names"><%=eventBuilderPropertyDto.getValue()%>
                </td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    </td>
</tr>

<tr fromElementKey="inputXmlMapping">
    <td colspan="2">

        <h6><fmt:message key="xpath.expression.header"/></h6>
        <table class="styledLeft noBorders spacer-bot"
               id="inputXpathExprTable">
            <thead>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.xpath"/></th>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.valueof"/></th>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.type"/></th>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.default"/></th>
            </thead>
            <tbody id="inputXpathExprTBody">
            <%
                EventBuilderPropertyDto parentSelectorXpathPropertyDto = null;
                for (EventBuilderPropertyDto eventBuilderPropertyDto : eventBuilderConfigurationDto.getEventBuilderProperties()) {
                    if (eventBuilderPropertyDto.getKey().endsWith("_mapping")) {
                        if (!(eventBuilderPropertyDto.getKey().startsWith("prefix_") || eventBuilderPropertyDto.getKey().startsWith("specific_"))) {
            %>
            <tr>
                <td class="property-names"><%=eventBuilderPropertyDto.getValue()%>
                </td>
                <td class="property-names"><%=eventBuilderPropertyDto.getDisplayName()%>
                </td>
                <td class="property-names"><%=eventBuilderPropertyDto.getPropertyType()%>
                </td>
                <td class="property-names"><%=eventBuilderPropertyDto.getDefaultValue() != null ? eventBuilderPropertyDto.getDefaultValue() : ""%>
                </td>
            </tr>
            <%
                        } else if (eventBuilderPropertyDto.getKey().startsWith("specific_")) {
                            if (eventBuilderPropertyDto.getKey().startsWith("specific_parentSelectorXpath")) {
                                parentSelectorXpathPropertyDto = eventBuilderPropertyDto;
                            }
                        }
                    }
                }
            %>
            </tbody>
        </table>
    </td>
</tr>
<tr>
    <td colspan="2">
        <table class="normal">
            <tbody>
            <tr>
                <td><fmt:message key="event.builder.parentselector.xpath"/></td>
                <td><input type="text" id="batchProcessingEnabled"
                           value="<% if(parentSelectorXpathPropertyDto != null) { %><%=parentSelectorXpathPropertyDto.getValue()%><%}%>"
                           disabled/>
                </td>
            </tr>
            </tbody>
        </table>
</tr>
<%
} else if (mappingType.equals("map")) {
%>
<tr fromElementKey="inputMapMapping">
    <td colspan="2" class="middle-header">
        <fmt:message key="event.builder.mapping.map"/>
    </td>
</tr>
<tr fromElementKey="inputMapMapping">
    <td colspan="2">

        <h6><fmt:message key="map.mapping.header"/></h6>
        <table class="styledLeft noBorders spacer-bot"
               id="inputMapDataTable">
            <thead>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.name"/></th>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.valueof"/></th>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.type"/></th>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.default"/></th>
            </thead>
            <tbody>
            <%
                for (EventBuilderPropertyDto eventBuilderPropertyDto : eventBuilderConfigurationDto.getEventBuilderProperties()) {
                    if (eventBuilderPropertyDto.getKey().endsWith("_mapping")) {
            %>
            <tr>
                <td class="property-names"><%=eventBuilderPropertyDto.getValue()%>
                </td>
                <td class="property-names"><%=eventBuilderPropertyDto.getDisplayName()%>
                </td>
                <td class="property-names"><%=eventBuilderPropertyDto.getPropertyType()%>
                </td>
                <td class="property-names"><%=eventBuilderPropertyDto.getDefaultValue() != null ? eventBuilderPropertyDto.getDefaultValue() : ""%>
                </td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    </td>
</tr>
<%

} else if (mappingType.equals("text")) {
%>
<tr fromElementKey="inputTextMapping">
    <td colspan="2" class="middle-header">
        <fmt:message key="event.builder.mapping.text"/>
    </td>
</tr>
<tr fromElementKey="inputTextMapping">
    <td colspan="2">

        <h6><fmt:message key="text.mapping.header"/></h6>
        <table class="styledLeft noBorders spacer-bot"
               id="inputTextMappingTable">
            <thead>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.regex"/></th>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.valueof"/></th>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.type"/></th>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.default"/></th>
            </thead>
            <tbody id="inputTextMappingTBody">
            <%
                for (EventBuilderPropertyDto eventBuilderPropertyDto : eventBuilderConfigurationDto.getEventBuilderProperties()) {
                    if (eventBuilderPropertyDto.getKey().endsWith("_mapping")) {
            %>
            <tr>
                <td class="property-names"><%=eventBuilderPropertyDto.getValue()%>
                </td>
                <td class="property-names"><%=eventBuilderPropertyDto.getDisplayName()%>
                </td>
                <td class="property-names"><%=eventBuilderPropertyDto.getPropertyType()%>
                </td>
                <td class="property-names"><%=eventBuilderPropertyDto.getDefaultValue() != null ? eventBuilderPropertyDto.getDefaultValue() : ""%>
                </td>
            </tr>
            <%
                    }
                }
            %>

            </tbody>
        </table>
    </td>
</tr>
<%
} else if (mappingType.equals("json")) {
%>
<tr fromElementKey="inputJsonMapping">
    <td colspan="2" class="middle-header">
        <fmt:message key="event.builder.mapping.json"/>
    </td>
</tr>
<tr fromElementKey="inputJsonMapping">
    <td colspan="2">

        <h6><fmt:message key="jsonpath.expression.header"/></h6>
        <table class="styledLeft noBorders spacer-bot"
               id="inputJsonpathExprTable">
            <thead>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.jsonpath"/></th>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.valueof"/></th>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.type"/></th>
            <th class="leftCol-med"><fmt:message
                    key="event.builder.property.default"/></th>
            </thead>
            <tbody id="inputJsonpathExprTBody">
            <%
                for (EventBuilderPropertyDto eventBuilderPropertyDto : eventBuilderConfigurationDto.getEventBuilderProperties()) {
                    if (eventBuilderPropertyDto.getKey().endsWith("_mapping")) {
            %>
            <tr>
                <td class="property-names"><%=eventBuilderPropertyDto.getValue()%>
                </td>
                <td class="property-names"><%=eventBuilderPropertyDto.getDisplayName()%>
                </td>
                <td class="property-names"><%=eventBuilderPropertyDto.getPropertyType()%>
                </td>
                <td class="property-names"><%=eventBuilderPropertyDto.getDefaultValue() != null ? eventBuilderPropertyDto.getDefaultValue() : ""%>
                </td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    </td>
</tr>
<%
        }
    }
%>
</tbody>
</table>

</td>
</tr>
<tr>
    <td colspan="2"><b><fmt:message key="event.builder.to.tooltip"/></b></td>
</tr>
<tr>
    <td>To Stream Name<span class="required">*</span></td>
    <td><input type="text" name="toStreamName" id="toStreamName"
               class="initE"
               value="<%=eventBuilderConfigurationDto.getToStreamName()%>"
               disabled/>

        <div class="sectionHelp">
            <fmt:message key="to.stream.name.tooltip"/>
        </div>
    </td>

</tr>
<tr>
    <td>To Stream Version</td>
    <td><input type="text" name="toStreamVersion" id="toStreamVersion"
               class="initE"
               value="<%=eventBuilderConfigurationDto.getToStreamVersion()%>"
               disabled/>

        <div class="sectionHelp">
            <fmt:message key="to.stream.version.tooltip"/>
        </div>
    </td>

</tr>
</tbody>
</table>

</table>
</form>
<%

} else {
%>
<table style="width:100%" id="ebNoAdd" class="styledLeft">
    <thead>
    <tr>
        <th><fmt:message key="event.builder.noeventbuilder.header"/></th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td class="formRaw">
            <table id="noEventBuilderInputTable" class="normal-nopadding"
                   style="width:100%">
                <tbody>

                <tr>
                    <td class="leftCol-med" colspan="2">No Event Builder of name
                        '<%=eventBuilderName%>' available.
                    </td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
    </tbody>
</table>
<%

        }
    }
%>
</div>
</div>
</fmt:bundle>
