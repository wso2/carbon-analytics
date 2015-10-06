<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
<%@ page
        import="org.wso2.carbon.event.receiver.stub.EventReceiverAdminServiceStub" %>

<%@ page
        import="org.wso2.carbon.event.receiver.stub.types.EventReceiverConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.receiver.ui.EventReceiverUIUtils" %>
<%@ page import="org.wso2.carbon.event.receiver.stub.types.EventMappingPropertyDto" %>
<%@ page import="org.wso2.carbon.event.receiver.stub.types.InputAdapterConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.receiver.stub.types.DetailInputAdapterPropertyDto" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.receiver.ui.i18n.Resources">

<carbon:breadcrumb
        label="event.receiver.details.breabcrumb"
        resourceBundle="org.wso2.carbon.event.receiver.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<link type="text/css" href="css/eventReceiver.css" rel="stylesheet"/>
<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../eventreceiver/js/event_receiver.js"></script>
<script type="text/javascript" src="../eventreceiver/js/registry-browser.js"></script>

<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>


    <%
        EventReceiverAdminServiceStub stub = EventReceiverUIUtils.getEventReceiverAdminService(config, session, request);
        String eventReceiverName = request.getParameter("eventReceiverName");
        EventReceiverConfigurationDto eventReceiverConfigurationDto = null;
        if (eventReceiverName != null) {
            eventReceiverConfigurationDto = stub.getActiveEventReceiverConfiguration(eventReceiverName);
    %>
    <script language="javascript">

    function clearTextIn(obj) {
        if (YAHOO.util.Dom.hasClass(obj, 'initE')) {
            YAHOO.util.Dom.removeClass(obj, 'initE');
            YAHOO.util.Dom.addClass(obj, 'normalE');
            textValue = obj.value;
            obj.value = "";
        }
    }
    function fillTextIn(obj) {
        if (obj.value == "") {
            obj.value = textValue;
            if (YAHOO.util.Dom.hasClass(obj, 'normalE')) {
                YAHOO.util.Dom.removeClass(obj, 'normalE');
                YAHOO.util.Dom.addClass(obj, 'initE');
            }
        }
    }
</script>

<script type="text/javascript">
    function doDelete(eventReceiverName) {

        CARBON.showConfirmationDialog("Are you sure want to delete event receiver:" + eventReceiverName,
                function () {
                    new Ajax.Request('../eventreceiver/delete_event_receiver_ajaxprocessor.jsp', {
                        method: 'POST',
                        asynchronous: false,
                        parameters: {
                            eventReceiverName: eventReceiverName
                        }, onSuccess: function (msg) {
                            if ("success" == msg.responseText.trim()) {
                                CARBON.showInfoDialog("Event receiver successfully deleted.", function () {
                                    window.location.href = "../eventreceiver/index.jsp?region=region1&item=eventreceiver_menu.jsp";
                                });
                            } else {
                                CARBON.showErrorDialog("Failed to delete event receiver, Exception: " + msg.responseText.trim());
                            }
                        }
                    })
                }, null, null);
    }

</script>


<div id="middle">
<h2 style="padding-bottom: 7px">
    <fmt:message key="event.receiver.details"/>
    <span style="float: right; font-size:75%">
        <% if (stub.isReceiverEditable(eventReceiverName)) { %>
            <% if (stub.isReceiverStatisticsEnabled(eventReceiverName)) {%>
            <div style="display: inline-block">
                <div id="disableStat<%= eventReceiverName%>">
                    <a href="#"
                       onclick="disableReceiverStat('<%= eventReceiverName %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                            key="stat.disable.link"/></a>
                </div>
                <div id="enableStat<%= eventReceiverName%>"
                     style="display:none;">
                    <a href="#"
                       onclick="enableReceiverStat('<%= eventReceiverName %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                            key="stat.enable.link"/></a>
                </div>
            </div>
            <% } else { %>
            <div style="display: inline-block">
                <div id="enableStat<%= eventReceiverName%>">
                    <a href="#"
                       onclick="enableReceiverStat('<%= eventReceiverName %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                            key="stat.enable.link"/></a>
                </div>
                <div id="disableStat<%= eventReceiverName%>"
                     style="display:none">
                    <a href="#"
                       onclick="disableReceiverStat('<%= eventReceiverName %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                            key="stat.disable.link"/></a>
                </div>
            </div>
            <% }
                if (stub.isReceiverTraceEnabled(eventReceiverName)) {%>
            <div style="display: inline-block">
                <div id="disableTracing<%= eventReceiverName%>">
                    <a href="#"
                       onclick="disableReceiverTracing('<%= eventReceiverName %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                            key="trace.disable.link"/></a>
                </div>
                <div id="enableTracing<%= eventReceiverName%>"
                     style="display:none;">
                    <a href="#"
                       onclick="enableReceiverTracing('<%= eventReceiverName %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                            key="trace.enable.link"/></a>
                </div>
            </div>
            <% } else { %>
            <div style="display: inline-block">
                <div id="enableTracing<%= eventReceiverName%>">
                    <a href="#"
                       onclick="enableReceiverTracing('<%= eventReceiverName %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                            key="trace.enable.link"/></a>
                </div>
                <div id="disableTracing<%= eventReceiverName%>"
                     style="display:none">
                    <a href="#"
                       onclick="disableReceiverTracing('<%= eventReceiverName %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                            key="trace.disable.link"/></a>
                </div>
            </div>

            <% } %>

            <div style="display: inline-block">
                <a style="background-image: url(../admin/images/delete.gif);"
                   class="icon-link"
                   onclick="doDelete('<%=eventReceiverName%>')"><font
                        color="#4682b4">Delete</font></a>
            </div>
            <div style="display: inline-block">
                <a style="background-image: url(../admin/images/edit.gif);"
                   class="icon-link"
                   href="edit_event_receiver_details.jsp?ordinal=1&eventReceiverName=<%=eventReceiverName%>"><font
                        color="#4682b4">Edit</font></a>
            </div>

            <% } else { %>
                <div style="display: inline-block">
                    <div id="cappArtifact<%= eventReceiverName%>">
                        <div style="background-image: url(images/capp.gif);" class="icon-nolink-nofloat">
                            <fmt:message key="capp.artifact.message"/></div>
                    </div>
                </div>
            <% } %>
    </span>
</h2>

<div id="workArea">

<form name="inputForm" action="index.jsp?ordinal=1" method="post">
<table style="width:100%" id="eventReceiverAdd" class="styledLeft">
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

               value="<%= eventReceiverConfigurationDto.getEventReceiverName()%>"
               style="width:75%" disabled="disabled"/>
    </td>
</tr>

<tr>
    <td colspan="2">
        <b><fmt:message key="from.heading"/></b>
    </td>
</tr>

<tr>
    <td><fmt:message key="event.adapter.type"/><span class="required">*</span></td>
    <td><select name="eventAdapterTypeFilter"
                id="eventAdapterTypeFilter" disabled="disabled">
        <option><%=eventReceiverConfigurationDto.getFromAdapterConfigurationDto().getEventAdapterType()%>
        </option>
    </select>
    </td>
</tr>


<%
    InputAdapterConfigurationDto fromPropertyConfigurationDto = eventReceiverConfigurationDto.getFromAdapterConfigurationDto();
    if (fromPropertyConfigurationDto.getUsageTips() != null) {
%>

<tr>
    <td><fmt:message key="event.adapter.usage.tips"/></td>
    <td><%=fromPropertyConfigurationDto.getUsageTips()%></td>
</tr>

<%    }
    if (fromPropertyConfigurationDto != null) {
    if (fromPropertyConfigurationDto.getInputEventAdapterProperties()!=null && fromPropertyConfigurationDto.getInputEventAdapterProperties().length > 0) {
%>

<tr>
    <td>
        <b><i><span style="color: #666666; "><fmt:message key="properties.heading"/></span></i></b>
    </td>
</tr>
<%
    DetailInputAdapterPropertyDto[] eventReceiverPropertyDto = fromPropertyConfigurationDto.getInputEventAdapterProperties();
    for (int index = 0; index < eventReceiverPropertyDto.length; index++) {
%>
<tr>


    <td class="leftCol-med"><%=eventReceiverPropertyDto[index].getDisplayName()%>
        <%
            String propertyId = "property_";
            if (eventReceiverPropertyDto[index].getRequired()) {
                propertyId = "property_Required_";

        %>
        <span class="required">*</span>
        <%
            }
        %>
    </td>
    <%
        String type = "text";
        if (eventReceiverPropertyDto[index].getSecured()) {
            type = "password";
        }
    %>

    <td>
        <div class=outputFields>
            <%
                if (eventReceiverPropertyDto[index].getOptions()[0] != null) {
            %>

            <select name="<%=eventReceiverPropertyDto[index].getKey()%>"
                    id="<%=propertyId%><%=index%>" disabled="disabled">

                <%
                    for (String property : eventReceiverPropertyDto[index].getOptions()) {
                        if (property.equals(eventReceiverPropertyDto[index].getValue())) {
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
                   name="<%=eventReceiverPropertyDto[index].getKey()%>"
                   id="<%=propertyId%><%=index%>" class="initE"
                   style="width:75%"
                   value="<%= eventReceiverPropertyDto[index].getValue() != null ? eventReceiverPropertyDto[index].getValue() : "" %>" disabled="disabled"/>

            <% } %>

            <% if(eventReceiverPropertyDto[index].getHint()!=null) { %>
            <div class="sectionHelp">
                <%=eventReceiverPropertyDto[index].getHint()%>
            </div>
            <% } %>

        </div>
    </td>

</tr>
<%
        }
    }
%>
<%
    }
%>

<tr>
    <td>
        <b><fmt:message key="to.heading"/></b>
    </td>
</tr>

<tr>
    <td><fmt:message key="event.stream.name"/><span class="required">*</span></td>
    <td><select name="streamIdFilter" id="streamIdFilter" disabled="disabled">
        <%

        %>
        <option><%=eventReceiverConfigurationDto.getToStreamNameWithVersion()%>
        </option>


    </select>

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
                id="mappingTypeFilter" disabled="disabled">
        <option><%=eventReceiverConfigurationDto.getMessageFormat()%>
        </option>
    </select>
    </td>

</tr>

<%
    boolean customMappingEnabled = eventReceiverConfigurationDto.getCustomMappingEnabled();
    String mappingType= eventReceiverConfigurationDto.getMessageFormat();
%>
<tr>
    <td id="mappingUiTd" colspan="2">
        <table class="styledLeft noBorders spacer-bot"
               style="width:100%">
            <tbody>
            <%
                if (customMappingEnabled) {
            %>

            <%
                if (mappingType != null) {
                    if (mappingType.equals("wso2event")) {
            %>
            <tr fromElementKey="inputWso2EventMapping">
                <td colspan="2" class="middle-header">
                    <fmt:message key="event.receiver.mapping.wso2event"/>
                </td>
            </tr>

            <%
                //get from stream properties for WSO2Event custom mapping
                String fromStreamProperties[] = eventReceiverConfigurationDto.getFromStreamNameWithVersion().split(":");
                String fromEventStreamName = fromStreamProperties[0];
                String fromEventStreamVersion = fromStreamProperties[1];
            %>


            <tr name="outputWSO2EventMapping">
                <td class="leftCol-med">
                    <fmt:message key="from.event.stream"/>
                </td>
                <td>
                    <input type="text" name="outputStreamName" id="outputStreamNameId"
                           class="initE"
                           value="<%=fromEventStreamName%>"
                           style="width:75%" disabled="disabled"/>
                </td>
            </tr>
            <tr name="outputWSO2EventMapping">
                <td class="leftCol-med">
                    <fmt:message key="from.event.version"/>
                </td>
                <td>
                    <input type="text" name="outputStreamVersion" id="outputStreamVersionId"
                           class="initE"
                           value="<%=fromEventStreamVersion%>"
                           style="width:75%" disabled="disabled"/>
                </td>
            </tr>

            <tr fromElementKey="inputWso2EventMapping">
                <td colspan="2">

                    <h6><fmt:message key="wso2event.mapping.header"/></h6>
                    <table class="styledLeft noBorders spacer-bot"
                           id="inputWso2EventDataTable">
                        <thead>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.name"/></th>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.inputtype"/></th>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.valueof"/></th>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.type"/></th>
                        </thead>
                        <tbody>
                        <%
                            EventMappingPropertyDto[] metaMappingDto = eventReceiverConfigurationDto.getMetaMappingPropertyDtos();
                            if (metaMappingDto != null && metaMappingDto.length > 0) {
                                for (EventMappingPropertyDto metaEbProperties : metaMappingDto) {

                        %>
                        <tr id="mappingRow">
                            <td class="property-names"><%=metaEbProperties.getName()%>
                            </td>
                            <td class="property-names">
                                <%="meta"%>
                            </td>
                            <td class="property-names"><%=metaEbProperties.getValueOf()%>
                            </td>
                            <td class="property-names"><%=metaEbProperties.getType()%>
                            </td>
                        </tr>
                        <%
                                }
                            }
                        %>
                        <%
                            EventMappingPropertyDto[] correlationMappingDto = eventReceiverConfigurationDto.getCorrelationMappingPropertyDtos();
                            if (correlationMappingDto != null && correlationMappingDto.length > 0) {
                                for (EventMappingPropertyDto correlationEbProperties : correlationMappingDto) {
                        %>
                        <tr id="mappingRow">
                            <td class="property-names"><%=correlationEbProperties.getName()%>
                            </td>
                            <td class="property-names">
                                <%="correlation"%>
                            </td>
                            <td class="property-names"><%=correlationEbProperties.getValueOf()%>
                            </td>
                            <td class="property-names"><%=correlationEbProperties.getType()%>
                            </td>
                        </tr>
                        <%
                                }
                            }
                        %>
                        <%
                            EventMappingPropertyDto[] payLoadMappingDto = eventReceiverConfigurationDto.getMappingPropertyDtos();
                            if (payLoadMappingDto != null && payLoadMappingDto.length > 0) {
                                for (EventMappingPropertyDto payloadEbProperties : payLoadMappingDto) {
                        %>
                        <tr id="mappingRow">
                            <td class="property-names"><%=payloadEbProperties.getName()%>
                            </td>
                            <td class="property-names">
                                <%="payload"%>
                            </td>
                            <td class="property-names"><%=payloadEbProperties.getValueOf()%>
                            </td>
                            <td class="property-names"><%=payloadEbProperties.getType()%>
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
            } else if (mappingType.equals("xml")) {
            %>
            <tr fromElementKey="inputXmlMapping">
                <td colspan="2" class="middle-header">
                    <fmt:message key="event.receiver.mapping.xml"/>
                </td>
            </tr>
            <tr fromElementKey="inputXmlMapping">
                <td colspan="2">

                    <h6><fmt:message key="xpath.prefix.header"/></h6>
                    <table class="styledLeft noBorders spacer-bot" id="inputXpathPrefixTable">
                        <thead>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.xpath.prefix"/></th>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.xpath.ns"/></th>
                        </thead>
                        <tbody id="inputXpathPrefixTBody">
                        <%
                            if (eventReceiverConfigurationDto.getXpathDefinitionMappingPropertyDtos() != null) {
                                for (EventMappingPropertyDto xpathDefinition : eventReceiverConfigurationDto.getXpathDefinitionMappingPropertyDtos()) {
                        %>
                        <tr>
                            <td class="property-names"><%=xpathDefinition.getName()%>
                            </td>
                            <td class="property-names"><%=xpathDefinition.getValueOf()%>
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
                String parentSelectorXpathProperty = eventReceiverConfigurationDto.getParentSelectorXpath();
            %>
            <tr>
                <td colspan="2">
                    <table class="normal">
                        <tbody>
                        <tr>
                            <td><fmt:message key="event.receiver.parentselector.xpath"/></td>
                            <td><input type="text" id="batchProcessingEnabled"
                                       value="<%=parentSelectorXpathProperty%>"
                                       disabled/>
                            </td>
                        </tr>
                        </tbody>
                    </table>
            </tr>

            <tr fromElementKey="inputXmlMapping">
                <td colspan="2">

                    <h6><fmt:message key="xpath.expression.header"/></h6>
                    <table class="styledLeft noBorders spacer-bot"
                           id="inputXpathExprTable">
                        <thead>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.xpath"/></th>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.valueof"/></th>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.type"/></th>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.default"/></th>
                        </thead>
                        <tbody id="inputXpathExprTBody">
                        <%
                            for (EventMappingPropertyDto xpathExpressions : eventReceiverConfigurationDto.getMappingPropertyDtos()) {
                        %>
                        <tr>
                            <td class="property-names"><%=xpathExpressions.getName()%>
                            </td>
                            <td class="property-names"><%=xpathExpressions.getValueOf()%>
                            </td>
                            <td class="property-names"><%=xpathExpressions.getType()%>
                            </td>
                            <td class="property-names"><%=xpathExpressions.getDefaultValue() != null ? xpathExpressions.getDefaultValue() : ""%>
                            </td>
                        </tr>
                        <%
                            }
                        %>
                        </tbody>
                    </table>
                </td>
            </tr>
            <%
            } else if (mappingType.equals("map")) {
            %>
            <tr fromElementKey="inputMapMapping">
                <td colspan="2" class="middle-header">
                    <fmt:message key="event.receiver.mapping.map"/>
                </td>
            </tr>
            <tr fromElementKey="inputMapMapping">
                <td colspan="2">

                    <h6><fmt:message key="map.mapping.header"/></h6>
                    <table class="styledLeft noBorders spacer-bot"
                           id="inputMapDataTable">
                        <thead>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.name"/></th>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.valueof"/></th>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.type"/></th>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.default"/></th>
                        </thead>
                        <tbody>
                        <%
                            for (EventMappingPropertyDto getMappingProperties : eventReceiverConfigurationDto.getMappingPropertyDtos()) {
                        %>
                        <tr>
                            <td class="property-names"><%=getMappingProperties.getName()%>
                            </td>
                            <td class="property-names"><%=getMappingProperties.getValueOf()%>
                            </td>
                            <td class="property-names"><%=getMappingProperties.getType()%>
                            </td>
                            <td class="property-names"><%=getMappingProperties.getDefaultValue() != null ? getMappingProperties.getDefaultValue() : ""%>
                            </td>
                        </tr>
                        <%
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
                    <fmt:message key="event.receiver.mapping.text"/>
                </td>
            </tr>
            <tr fromElementKey="inputTextMapping">
                <td colspan="2">

                    <h6><fmt:message key="text.mapping.header"/></h6>
                    <table class="styledLeft noBorders spacer-bot"
                           id="inputTextMappingTable">
                        <thead>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.regex"/></th>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.valueof"/></th>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.type"/></th>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.default"/></th>
                        </thead>
                        <tbody id="inputTextMappingTBody">
                        <%
                            for (EventMappingPropertyDto eventReceiverMessagePropertyDto : eventReceiverConfigurationDto.getMappingPropertyDtos()) {
                        %>
                        <tr>
                            <td class="property-names"><%=eventReceiverMessagePropertyDto.getName()%>
                            </td>
                            <td class="property-names"><%=eventReceiverMessagePropertyDto.getValueOf()%>
                            </td>
                            <td class="property-names"><%=eventReceiverMessagePropertyDto.getType()%>
                            </td>
                            <td class="property-names"><%=eventReceiverMessagePropertyDto.getDefaultValue() != null ? eventReceiverMessagePropertyDto.getDefaultValue() : ""%>
                            </td>
                        </tr>
                        <%
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
                    <fmt:message key="event.receiver.mapping.json"/>
                </td>
            </tr>
            <tr fromElementKey="inputJsonMapping">
                <td colspan="2">

                    <h6><fmt:message key="jsonpath.expression.header"/></h6>
                    <table class="styledLeft noBorders spacer-bot"
                           id="inputJsonpathExprTable">
                        <thead>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.jsonpath"/></th>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.valueof"/></th>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.type"/></th>
                        <th class="leftCol-med"><fmt:message
                                key="event.receiver.property.default"/></th>
                        </thead>
                        <tbody id="inputJsonpathExprTBody">
                        <%
                            for (EventMappingPropertyDto jsonpathExpressions : eventReceiverConfigurationDto.getMappingPropertyDtos()) {
                        %>
                        <tr>
                            <td class="property-names"><%=jsonpathExpressions.getName()%>
                            </td>
                            <td class="property-names"><%=jsonpathExpressions.getValueOf()%>
                            </td>
                            <td class="property-names"><%=jsonpathExpressions.getType()%>
                            </td>
                            <td class="property-names"><%=jsonpathExpressions.getDefaultValue() != null ? jsonpathExpressions.getDefaultValue() : ""%>
                            </td>
                        </tr>
                        <%
                            }
                        %>
                        </tbody>
                    </table>
                </td>
            </tr>
            <%
                        }
                    }
                }
            %>
            </tbody>
        </table>
    </td>
</tr>
</tbody>
</table>
</td>
</tr>
<%
    }
%>
</tbody>
</table>


</form>
</div>
</div>
</fmt:bundle>
