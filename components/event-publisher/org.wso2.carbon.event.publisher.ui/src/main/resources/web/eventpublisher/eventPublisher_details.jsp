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
<%@ page
        import="org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub" %>

<%@ page
        import="org.wso2.carbon.event.publisher.stub.types.EventPublisherConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.publisher.ui.EventPublisherUIUtils" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.OutputAdapterConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.DetailOutputAdapterPropertyDto" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.publisher.ui.i18n.Resources">

<carbon:breadcrumb
        label="event.publisher.details.breabcrumb"
        resourceBundle="org.wso2.carbon.event.publisher.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<link type="text/css" href="css/eventPublisher.css" rel="stylesheet"/>
<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../eventpublisher/js/event_publisher.js"></script>
<script type="text/javascript" src="../eventpublisher/js/registry-browser.js"></script>

<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>


<%
    EventPublisherAdminServiceStub stub = EventPublisherUIUtils.getEventPublisherAdminService(config, session, request);
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


<div id="middle">
<h2><fmt:message key="event.publisher.details"/></h2>

<div id="workArea">

<form name="inputForm" action="index.jsp?ordinal=1" method="post">
<table style="width:100%" id="eventPublisherAdd" class="styledLeft">
<thead>
<tr>
    <th><fmt:message key="title.event.publisher.details"/></th>
</tr>
</thead>
<% String eventPublisherName = request.getParameter("eventPublisherName");
    if (eventPublisherName != null) {
        EventPublisherConfigurationDto eventPublisherConfigurationDto = stub.getActiveEventPublisherConfiguration(eventPublisherName);
%>
<tbody>
<tr>
<td class="formRaw">
<table id="eventPublisherInputTable" class="normal-nopadding"
       style="width:100%">
<tbody>

<tr>
    <td class="leftCol-med"><fmt:message key="event.publisher.name"/><span class="required">*</span>
    </td>
    <td><input type="text" name="eventPublisherName" id="eventPublisherId"
               class="initE"

               value="<%= eventPublisherConfigurationDto.getEventPublisherName()%>"
               style="width:75%" disabled="disabled"/>
    </td>
</tr>

<tr>
    <td colspan="2">
        <b><fmt:message key="from.heading"/></b>
    </td>
</tr>

<tr>
    <td><fmt:message key="event.stream.name"/><span class="required">*</span></td>
    <td><select name="streamIdFilter" id="streamIdFilter" disabled="disabled">
        <%

        %>
        <option><%=eventPublisherConfigurationDto.getFromStreamNameWithVersion()%>
        </option>


    </select>

    </td>

</tr>
<tr>
    <td>
        <fmt:message key="stream.attributes"/>
    </td>
    <td>
        <textArea class="expandedTextarea" id="streamDefinitionText" name="streamDefinitionText"
                  readonly="true"
                  cols="60"
                  disabled="disabled"><%=eventPublisherConfigurationDto.getStreamDefinition()%>
        </textArea>
    </td>

</tr>

<tr>
    <td>
        <b><fmt:message key="to.heading"/></b>
    </td>
</tr>

<tr>
    <td><fmt:message key="event.adapter.type"/><span class="required">*</span></td>
    <td><select name="eventAdapterTypeFilter"
                id="eventAdapterTypeFilter" disabled="disabled">
        <option><%=eventPublisherConfigurationDto.getToAdapterConfigurationDto().getEventAdapterType()%>
        </option>
    </select>
    </td>
</tr>


<%
    OutputAdapterConfigurationDto toPropertyConfigurationDto = eventPublisherConfigurationDto.getToAdapterConfigurationDto();
    if (toPropertyConfigurationDto != null) {
%>
<%
    if (toPropertyConfigurationDto.getOutputEventAdapterStaticProperties()!=null && toPropertyConfigurationDto.getOutputEventAdapterStaticProperties().length > 0) {
%>
<tr>
    <td>
        <b><i><span style="color: #666666; "><fmt:message key="static.properties.heading"/></span></i></b>
    </td>
</tr>
<%
    DetailOutputAdapterPropertyDto[] eventPublisherPropertyDto = toPropertyConfigurationDto.getOutputEventAdapterStaticProperties();
    for (int index = 0; index < eventPublisherPropertyDto.length; index++) {
%>
<tr>


    <td class="leftCol-med"><%=eventPublisherPropertyDto[index].getDisplayName()%>
        <%
            String propertyId = "property_";
            if (eventPublisherPropertyDto[index].getRequired()) {
                propertyId = "property_Required_";

        %>
        <span class="required">*</span>
        <%
            }
        %>
    </td>
    <%
        String type = "text";
        if (eventPublisherPropertyDto[index].getSecured()) {
            type = "password";
        }
    %>

    <td>
        <div class=outputFields>
            <%
                if (eventPublisherPropertyDto[index].getOptions()[0] != null) {
            %>

            <select name="<%=eventPublisherPropertyDto[index].getKey()%>"
                    id="<%=propertyId%><%=index%>" disabled="disabled">

                <%
                    for (String property : eventPublisherPropertyDto[index].getOptions()) {
                        if (property.equals(eventPublisherPropertyDto[index].getValue())) {
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
                   name="<%=eventPublisherPropertyDto[index].getKey()%>"
                   id="<%=propertyId%><%=index%>" class="initE"
                   style="width:75%"
                   value="<%= eventPublisherPropertyDto[index].getValue() != null ? eventPublisherPropertyDto[index].getValue() : "" %>" disabled="disabled"/>

            <% } %>


        </div>
    </td>

</tr>
<%
        }
    }
%>
<%
    if (toPropertyConfigurationDto.getOutputEventAdapterDynamicProperties()!=null && toPropertyConfigurationDto.getOutputEventAdapterDynamicProperties().length > 0) {
%>
<tr>
    <td>
        <b><i><span style="color: #666666; "><fmt:message key="dynamic.properties.heading"/></span></i></b>
    </td>
</tr>
<%
    DetailOutputAdapterPropertyDto[] eventPublisherPropertyDto = toPropertyConfigurationDto.getOutputEventAdapterDynamicProperties();
    for (int index = 0; index < eventPublisherPropertyDto.length; index++) {
%>
<tr>


    <td class="leftCol-med"><%=eventPublisherPropertyDto[index].getDisplayName()%>
        <%
            String propertyId = "property_";
            if (eventPublisherPropertyDto[index].getRequired()) {
                propertyId = "property_Required_";

        %>
        <span class="required">*</span>
        <%
            }
        %>
    </td>
    <%
        String type = "text";
        if (eventPublisherPropertyDto[index].getSecured()) {
            type = "password";
        }
    %>

    <td>
        <div class=outputFields>
            <%
                if (eventPublisherPropertyDto[index].getOptions()[0] != null) {
            %>

            <select name="<%=eventPublisherPropertyDto[index].getKey()%>"
                    id="<%=propertyId%><%=index%>" disabled="disabled">

                <%
                    for (String property : eventPublisherPropertyDto[index].getOptions()) {
                        if (property.equals(eventPublisherPropertyDto[index].getValue())) {
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
                   name="<%=eventPublisherPropertyDto[index].getKey()%>"
                   id="<%=propertyId%><%=index%>" class="initE"
                   style="width:75%"
                   value="<%= eventPublisherPropertyDto[index].getValue() != null ? eventPublisherPropertyDto[index].getValue() : "" %>" disabled="disabled"/>

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
    <td colspan="2">
        <b><fmt:message key="mapping.heading"/></b>
    </td>
</tr>

<tr>
    <td><fmt:message key="message.format"/><span class="required">*</span></td>
    <td><select name="mappingTypeFilter"
                id="mappingTypeFilter" disabled="disabled">
        <option><%=eventPublisherConfigurationDto.getMessageFormat()%>
        </option>
    </select>
    </td>

</tr>

<tr>
<td class="formRaw" colspan="2">
<div id="outerDiv">
<%

    if (eventPublisherConfigurationDto.getMessageFormat().equalsIgnoreCase("wso2event") && eventPublisherConfigurationDto.getCustomMappingEnabled()) {
%>
<div id="innerDiv1">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputWSO2EventMapping">
            <td colspan="2" class="middle-header">
                <fmt:message key="wso2event.mapping"/>
            </td>
        </tr>
        <tr name="outputWSO2EventMapping">
            <td colspan="2">

                <h6><fmt:message key="property.data.type.meta"/></h6>
                <% if (eventPublisherConfigurationDto.getWso2EventOutputMappingDto().getMetaWSO2EventMappingProperties() != null && eventPublisherConfigurationDto.getWso2EventOutputMappingDto().getMetaWSO2EventMappingProperties()[0] != null) { %>
                <table class="styledLeft noBorders spacer-bot" id="outputMetaDataTable">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th class="leftCol-med"><fmt:message key="property.type"/></th>
                    </thead>
                    <% EventMappingPropertyDto[] eventMappingPropertyDtos = eventPublisherConfigurationDto.getWso2EventOutputMappingDto().getMetaWSO2EventMappingProperties();
                        for (EventMappingPropertyDto eventMappingPropertyDto : eventMappingPropertyDtos) { %>
                    <tr>
                        <td class="property-names"><%=eventMappingPropertyDto.getName()%>
                        </td>
                        <td class="property-names"><%=eventMappingPropertyDto.getValueOf()%>
                        </td>
                        <td class="property-names"><%=eventMappingPropertyDto.getType()%>
                        </td>
                    </tr>
                    <% } %>

                </table>
                <% } else { %>
                <div class="noDataDiv-plain" id="noOutputMetaData">
                    <fmt:message key="no.meta.defined.message"/>
                </div>
                <% } %>
            </td>
        </tr>


        <tr name="outputWSO2EventMapping">
            <td colspan="2">

                <h6><fmt:message key="property.data.type.correlation"/></h6>
                <% if (eventPublisherConfigurationDto.getWso2EventOutputMappingDto().getCorrelationWSO2EventMappingProperties() != null && eventPublisherConfigurationDto.getWso2EventOutputMappingDto().getCorrelationWSO2EventMappingProperties()[0] != null) { %>
                <table class="styledLeft noBorders spacer-bot"
                       id="outputCorrelationDataTable">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th class="leftCol-med"><fmt:message key="property.type"/></th>
                    </thead>
                    <% EventMappingPropertyDto[] eventMappingPropertyDtos = eventPublisherConfigurationDto.getWso2EventOutputMappingDto().getCorrelationWSO2EventMappingProperties();
                        for (EventMappingPropertyDto eventMappingPropertyDto : eventMappingPropertyDtos) { %>
                    <tr>
                        <td class="property-names"><%=eventMappingPropertyDto.getName()%>
                        </td>
                        <td class="property-names"><%=eventMappingPropertyDto.getValueOf()%>
                        </td>
                        <td class="property-names"><%=eventMappingPropertyDto.getType()%>
                        </td>
                    </tr>
                    <% } %>
                </table>
                <% } else {%>
                <div class="noDataDiv-plain" id="noOutputCorrelationData">
                    <fmt:message key="no.correlation.defined.message"/>
                </div>
                <% } %>
            </td>
        </tr>

        <tr name="outputWSO2EventMapping">
            <td colspan="2">
                <h6><fmt:message key="property.data.type.payload"/></h6>
                <% if (eventPublisherConfigurationDto.getWso2EventOutputMappingDto().getPayloadWSO2EventMappingProperties() != null && eventPublisherConfigurationDto.getWso2EventOutputMappingDto().getPayloadWSO2EventMappingProperties()[0] != null) { %>
                <table class="styledLeft noBorders spacer-bot"
                       id="outputPayloadDataTable">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th class="leftCol-med"><fmt:message key="property.type"/></th>
                    </thead>
                    <% EventMappingPropertyDto[] eventMappingPropertyDtos = eventPublisherConfigurationDto.getWso2EventOutputMappingDto().getPayloadWSO2EventMappingProperties();
                        for (EventMappingPropertyDto eventMappingPropertyDto : eventMappingPropertyDtos) { %>
                    <tr>
                        <td class="property-names"><%=eventMappingPropertyDto.getName()%>
                        </td>
                        <td class="property-names"><%=eventMappingPropertyDto.getValueOf()%>
                        </td>
                        <td class="property-names"><%=eventMappingPropertyDto.getType()%>
                        </td>
                    </tr>
                    <% } %>
                </table>
                <% } else { %>
                <div class="noDataDiv-plain" id="noOutputPayloadData">
                    <fmt:message key="no.payload.defined.message"/>
                </div>
                <% } %>
            </td>
        </tr>

        </tbody>
    </table>
</div>

<%
} else if (eventPublisherConfigurationDto.getMessageFormat().equalsIgnoreCase("text") && eventPublisherConfigurationDto.getCustomMappingEnabled()) {
%>

<div id="innerDiv2">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputTextMapping">
            <td colspan="3" class="middle-header">
                <fmt:message key="text.mapping"/>
            </td>
        </tr>
        <% if (!(eventPublisherConfigurationDto.getTextOutputMappingDto().getRegistryResource())) {%>
        <tr>
            <td class="leftCol-med" colspan="1"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_text" type="radio" checked="checked" value="content"
                       name="inline_text" disabled="disabled">
                <label for="inline_text"><fmt:message key="inline.input"/></label>
                <input id="registry_text" type="radio" value="reg" name="registry_text"
                       disabled="disabled">
                <label for="registry_text"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputTextMappingInline" id="outputTextMappingInline">
            <td colspan="3">
                <p>
                    <textarea id="textSourceText" name="textSourceText"
                              style="border:solid 1px rgb(204, 204, 204); width: 99%;
    height: 150px; margin-top: 5px;"
                              name="textSource"
                              rows="30"
                              disabled="disabled"><%= eventPublisherConfigurationDto.getTextOutputMappingDto().getMappingText() %>
                    </textarea>
                </p>
            </td>
        </tr>
        <% } else { %>
        <tr>
            <td colspan="1" class="leftCol-med"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_text_reg" type="radio" value="content"
                       name="inline_text" disabled="disabled">
                <label for="inline_text_reg"><fmt:message key="inline.input"/></label>
                <input id="registry_text_reg" type="radio" value="reg" name="registry_text"
                       disabled="disabled" checked="checked">
                <label for="registry_text_reg"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputTextMappingRegistry" id="outputTextMappingRegistry">
            <td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span
                    class="required">*</span></td>
            <td colspan="1">
                <input type="text" id="textSourceRegistry" disabled="disabled" class="initE"
                       value="<%=eventPublisherConfigurationDto.getTextOutputMappingDto().getMappingText() !=null ? eventPublisherConfigurationDto.getTextOutputMappingDto().getMappingText() : ""%>"
                       style="width:100%"/>
            </td>

            <td class="nopadding" style="border:none" colspan="1">
                <a href="#registryBrowserLink" class="registry-picker-icon-link"
                   style="padding-left:20px"><fmt:message
                        key="conf.registry"/></a>
                <a href="#registryBrowserLink"
                   class="registry-picker-icon-link"
                   style="padding-left:20px"><fmt:message
                        key="gov.registry"/></a>
            </td>
        </tr>
        <% } %>
        </tbody>
    </table>
</div>

<%
} else if (eventPublisherConfigurationDto.getMessageFormat().equalsIgnoreCase("xml") && eventPublisherConfigurationDto.getCustomMappingEnabled()) {
%>

<div id="innerDiv3">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputXMLMapping">
            <td colspan="3" class="middle-header">
                <fmt:message key="xml.mapping"/>
            </td>
        </tr>
        <% if (!eventPublisherConfigurationDto.getXmlOutputMappingDto().getRegistryResource()) { %>
        <tr>
            <td colspan="1" class="leftCol-med"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_xml" type="radio" checked="checked" value="content"
                       name="inline_xml" disabled="disabled">
                <label for="inline_xml"><fmt:message key="inline.input"/></label>
                <input id="registry_xml" type="radio" value="reg" name="registry_xml"
                       disabled="disabled">
                <label for="registry_xml"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputXMLMappingInline" id="outputXMLMappingInline">
            <td colspan="3">
                <p>
                    <textarea id="xmlSourceText"
                              style="border:solid 1px rgb(204, 204, 204); width: 99%;
                                     height: 150px; margin-top: 5px;"
                              name="xmlSource"
                              rows="30"
                              disabled="disabled"><%=eventPublisherConfigurationDto.getXmlOutputMappingDto().getMappingXMLText() != null ? eventPublisherConfigurationDto.getXmlOutputMappingDto().getMappingXMLText() : ""%>
                    </textarea>
                </p>
            </td>
        </tr>
        <% } else { %>
        <tr>
            <td colspan="1" class="leftCol-med"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_xml_reg" type="radio" value="content"
                       name="inline_xml" disabled="disabled">
                <label for="inline_xml_reg"><fmt:message key="inline.input"/></label>
                <input id="registry_xml_reg" type="radio" value="reg" name="registry_xml"
                       disabled="disabled" checked="checked">
                <label for="registry_xml_reg"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputXMLMappingRegistry" id="outputXMLMappingRegistry">
            <td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span
                    class="required">*</span></td>
            <td colspan="1">
                <input type="text" id="xmlSourceRegistry" disabled="disabled" class="initE" value=""
                       style="width:100%"/>
            </td>
            <td class="nopadding" style="border:none" colspan="1">
                <a href="#registryBrowserLink" class="registry-picker-icon-link"
                   style="padding-left:20px"><fmt:message
                        key="conf.registry"/></a>
                <a href="#registryBrowserLink"
                   class="registry-picker-icon-link"
                   style="padding-left:20px"><fmt:message
                        key="gov.registry"/></a>
            </td>
        </tr>
        <% } %>
        </tbody>
    </table>
</div>

<%
} else if (eventPublisherConfigurationDto.getMessageFormat().equalsIgnoreCase("map") && eventPublisherConfigurationDto.getCustomMappingEnabled()) {
%>

<div id="innerDiv4">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputMapMapping">
            <td colspan="2" class="middle-header">
                <fmt:message key="map.mapping"/>
            </td>
        </tr>
        <% if (eventPublisherConfigurationDto.getMapOutputMappingDto().getEventMappingProperties() != null && eventPublisherConfigurationDto.getMapOutputMappingDto().getEventMappingProperties()[0] != null) { %>
        <tr name="outputMapMapping">
            <td colspan="2">
                <table class="styledLeft noBorders spacer-bot" id="outputMapPropertiesTable">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    </thead>
                    <% EventMappingPropertyDto[] eventMappingPropertyDtos = eventPublisherConfigurationDto.getMapOutputMappingDto().getEventMappingProperties();
                        for (EventMappingPropertyDto eventMappingPropertyDto : eventMappingPropertyDtos) { %>
                    <tr>
                        <td class="property-names"><%=eventMappingPropertyDto.getName()%>
                        </td>
                        <td class="property-names"><%=eventMappingPropertyDto.getValueOf()%>
                        </td>
                    </tr>
                    <% } %>
                </table>
                <% } else { %>
                <div class="noDataDiv-plain" id="noOutputMapProperties">
                    <fmt:message key="no.map.properties.defined"/>
                </div>
                <% } %>
            </td>
        </tr>

        </tbody>
    </table>
</div>
<%
} else if (eventPublisherConfigurationDto.getMessageFormat().equalsIgnoreCase("json") && eventPublisherConfigurationDto.getCustomMappingEnabled()) {
%>

    <div id="innerDiv5">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputJSONMapping">
            <td colspan="3" class="middle-header">
                <fmt:message key="json.mapping"/>
            </td>
        </tr>
        <% if (!eventPublisherConfigurationDto.getJsonOutputMappingDto().getRegistryResource()) { %>
        <tr>
            <td colspan="1" class="leftCol-med"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_json" type="radio" checked="checked" value="content"
                       name="inline_json" disabled="disabled">
                <label for="inline_json"><fmt:message key="inline.input"/></label>
                <input id="registry_json" type="radio" value="reg" name="registry_json"
                       disabled="disabled">
                <label for="registry_json"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputJSONMappingInline" id="outputJSONMappingInline">
            <td colspan="3">
                <p>
                    <textarea id="jsonSourceText"
                              style="border:solid 1px rgb(204, 204, 204); width: 99%;
                                     height: 150px; margin-top: 5px;"
                              name="jsonSource"
                              rows="30"
                              disabled="disabled"><%=eventPublisherConfigurationDto.getJsonOutputMappingDto().getMappingText()!=null ? eventPublisherConfigurationDto.getJsonOutputMappingDto().getMappingText() : ""%>
                    </textarea>
                </p>
            </td>
        </tr>
        <% } else { %>
        <tr>
            <td colspan="1" class="leftCol-med"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_json_text" type="radio" value="content"
                       name="inline_json_text" disabled="disabled">
                <label for="inline_json_text"><fmt:message key="inline.input"/></label>
                <input id="registry_json_reg" type="radio" value="reg" name="registry_json"
                       disabled="disabled" checked="checked">
                <label for="registry_json_reg"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputJSONMappingRegistry" id="outputJSONMappingRegistry">
            <td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span
                    class="required">*</span></td>
            <td colspan="1">
                <input type="text" id="jsonSourceRegistry" disabled="disabled" class="initE"
                       value="<%=eventPublisherConfigurationDto.getJsonOutputMappingDto().getMappingText() != null ? eventPublisherConfigurationDto.getJsonOutputMappingDto().getMappingText() : ""%>"
                       style="width:100%"/>
            </td>
            <td colspan="1" class="nopadding" style="border:none">
                <a href="#registryBrowserLink" class="registry-picker-icon-link"
                   style="padding-left:20px"><fmt:message
                        key="conf.registry"/></a>
                <a href="#registryBrowserLink"
                   class="registry-picker-icon-link"
                   style="padding-left:20px"><fmt:message
                        key="gov.registry"/></a>
            </td>
        </tr>
        <% } %>
        </tbody>
    </table>
</div>

<%
    }
%>

</div>
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
