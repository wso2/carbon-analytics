<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
<%@ page import="org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.publisher.ui.EventPublisherUIConstants" %>
<%@ page import="org.wso2.carbon.event.publisher.ui.EventPublisherUIUtils" %>
<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.DetailOutputAdapterPropertyDto" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.OutputAdapterConfigurationDto" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.publisher.ui.i18n.Resources">
<script type="text/javascript" src="../eventpublisher/js/event_publisher.js"></script>
<script type="text/javascript" src="../eventpublisher/js/create_eventPublisher_helper.js"></script>
<script type="text/javascript" src="../eventpublisher/js/registry-browser.js"></script>
<link rel="stylesheet" href="../eventpublisher/css/eventPublisher.css"/>

<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript"
        src="../eventpublisher/js/create_eventPublisher_helper.js"></script>

<script type="text/javascript">
    jQuery(document).ready(function () {
        showEventStreamDefinition();
    });

</script>

<div id="custom_dcontainer" style="display:none"></div>
<div id="middle">
<h2><fmt:message key="title.event.publisher.create"/></h2>

<div id="workArea">

<form name="inputForm" action="#" method="post" id="addEventPublisher">
<table style="width:100%" id="eventPublisherAdd" class="styledLeft">
<%
    EventPublisherAdminServiceStub eventPublisherAdminServiceStub = EventPublisherUIUtils.getEventPublisherAdminService(config, session, request);
    String[] outputAdapterTypes = eventPublisherAdminServiceStub.getAllOutputAdapterTypes();

    String firstEventAdapterType = null;
    if (outputAdapterTypes != null && outputAdapterTypes.length > 0) {
        firstEventAdapterType = outputAdapterTypes[0];
    }
    String streamId = request.getParameter("streamId");
    String redirectPage = request.getParameter("redirectPage");

    EventStreamAdminServiceStub eventStreamAdminServiceStub = EventPublisherUIUtils.getEventStreamAdminService(config, session, request);
    String[] streamIds = eventStreamAdminServiceStub.getStreamNames();
    if (streamId == null && streamIds != null && streamIds.length > 0) {
        streamId = streamIds[0];
    }
    if (streamId != null && firstEventAdapterType != null) {
        EventStreamDefinitionDto streamDefinitionDto = eventStreamAdminServiceStub.getStreamDefinitionDto(streamId);
        EventStreamAttributeDto[] metaAttributeList = streamDefinitionDto.getMetaData();
        EventStreamAttributeDto[] correlationAttributeList = streamDefinitionDto.getCorrelationData();
        EventStreamAttributeDto[] payloadAttributeList = streamDefinitionDto.getPayloadData();

        String attributes = "";

        if (metaAttributeList != null && metaAttributeList.length > 0) {
            for (EventStreamAttributeDto attribute : metaAttributeList) {
                attributes += EventPublisherUIConstants.PROPERTY_META_PREFIX + attribute.getAttributeName() + " " + attribute.getAttributeType() + ", \n";
            }
        }
        if (correlationAttributeList != null) {
            for (EventStreamAttributeDto attribute : correlationAttributeList) {
                attributes += EventPublisherUIConstants.PROPERTY_CORRELATION_PREFIX + attribute.getAttributeName() + " " + attribute.getAttributeType() + ", \n";
            }
        }
        if (payloadAttributeList != null) {
            for (EventStreamAttributeDto attribute : payloadAttributeList) {
                attributes += attribute.getAttributeName() + " " + attribute.getAttributeType() + ", \n";
            }
        }

        if (!attributes.equals("")) {
            attributes = attributes.substring(0, attributes.lastIndexOf(","));
        }

        String streamDefinition = attributes;
        String streamName = streamDefinitionDto.getName();
        String streamVersion = streamDefinitionDto.getVersion();

        List<String> attributeList = EventPublisherUIUtils.getAttributeListWithPrefix(streamDefinitionDto);
%>
<br/>
<thead>
<tr>
    <th><fmt:message key="title.event.publisher.details"/></th>
</tr>
</thead>
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

               value=""
               style="width:75%"/>

        <div class="sectionHelp">
            <fmt:message key="event.publisher.name.help"/>
        </div>
    </td>
</tr>

<tr>
    <td colspan="2">
        <b><fmt:message key="from.heading"/></b>
    </td>
</tr>

<tr>
    <td><fmt:message key="event.source.name"/><span class="required">*</span></td>
    <td><select name="streamIdFilter"
                onchange="showEventStreamDefinition()" id="streamIdFilter">
        <%

            if (streamIds != null) {
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
            <fmt:message key="event.source.name.help"/>
        </div>
    </td>

</tr>

<tr>
    <td>
        <fmt:message key="stream.attributes"/>
    </td>
    <td>
        <textArea class="expandedTextarea" id="streamDefinitionText" name="streamDefinitionText"
                  readonly="true"
                  cols="60"><%=streamDefinition%>
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
    <td>
        <table>
            <td class="custom-noPadding" width="60%"><select name="eventAdapterTypeFilter"
                                                             onchange="loadEventAdapterRelatedProperties('<fmt:message
                                                                     key="to.heading"/>')"
                                                             id="eventAdapterTypeFilter">
                <%
                    for (String outputAdapterType : outputAdapterTypes) {
                %>
                <option value="<%=outputAdapterType%>"><%=outputAdapterType%>
                </option>
                <%
                    }
                %>

            </select>

                <div class="sectionHelp">
                    <fmt:message key="event.adapter.type.help"/>
                </div>
            </td>
            <td width="40%" id="addOutputEventAdapterTD" class="custom-noPadding"></td>
        </table>
    </td>
</tr>
<%
    OutputAdapterConfigurationDto outputAdapterConfigurationDto = eventPublisherAdminServiceStub.getOutputAdapterConfigurationSchema(firstEventAdapterType);
    if (outputAdapterConfigurationDto != null) {
%>
<%
    DetailOutputAdapterPropertyDto[] eventAdapterProperties = outputAdapterConfigurationDto.getOutputEventAdapterStaticProperties();
    int initialDynamicIndex = 0;
    if (eventAdapterProperties != null && eventAdapterProperties.length > 0) {
        initialDynamicIndex=eventAdapterProperties.length;
%>
<tr>
    <td>
        <b><i><span style="color: #666666; "><fmt:message key="static.properties.heading"/></span></i></b>
    </td>
</tr>
<%
    for (int index=0; index < eventAdapterProperties.length; index++) {
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
        <div class=outputFields>
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
    eventAdapterProperties = outputAdapterConfigurationDto.getOutputEventAdapterDynamicProperties();
    if (eventAdapterProperties != null && eventAdapterProperties.length > 0) {
%>
<tr>
    <td>
        <b><i><span style="color: #666666; "><fmt:message key="dynamic.properties.heading"/></span></i></b>
    </td>
</tr>
<%

    for ( int index = 0; index < eventAdapterProperties.length; index++) {
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
        <div class=outputFields>
            <%
                if (eventAdapterProperties[index].getOptions()[0] != null) {
            %>

            <select name="<%=eventAdapterProperties[index].getKey()%>"
                    id="<%=propertyId%><%=index+initialDynamicIndex%>">

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
                   id="<%=propertyId%><%=index+initialDynamicIndex%>" class="initE"
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
    <td colspan="2">
        <b><fmt:message key="mapping.heading"/></b>
    </td>
</tr>

<tr>
    <td><fmt:message key="message.format"/><span class="required">*</span></td>
    <td><select name="mappingTypeFilter"
                onchange="showEventStreamDefinition()" id="mappingTypeFilter">
        <%
            String[] messageFormats = outputAdapterConfigurationDto.getSupportedMessageFormats();
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
<td class="formRaw" colspan="2">
<div id="outerDiv" style="display:none">
                <%if (firstMappingTypeName != null) {
                    if (firstMappingTypeName.equals("wso2event")) {%>
                        <jsp:include page="wso2event_mapping_ui.jsp" flush="true">
                            <jsp:param name="streamNameWithVersion"
                                       value="<%=streamId%>"/>
                        </jsp:include>
                <%}else if (firstMappingTypeName.equals("xml")) {%>
                    <jsp:include page="xml_mapping_ui.jsp" flush="true"/>
                <%} else if (firstMappingTypeName.equals("map")) {%>
                    <jsp:include page="map_mapping_ui.jsp" flush="true">
                        <jsp:param name="streamNameWithVersion"
                                   value="<%=streamId%>"/>
                    </jsp:include>
                <%} else if (firstMappingTypeName.equals("text")) {%>
                    <jsp:include page="text_mapping_ui.jsp" flush="true"/>
                <%} else if (firstMappingTypeName.equals("json")) {%>
                    <jsp:include page="json_mapping_ui.jsp" flush="true"/>
                <%}
            }
        }
    }%>
</div>


</div>
</td>
</tr>

</tbody>
</table>
</td>
</tr>
<tr>
    <td class="buttonRow">
        <input type="button" value="<fmt:message key="add.event.publisher"/>"
               onclick="addEventPublisher(document.getElementById('addEventPublisher') ,document.getElementById('streamIdFilter')[document.getElementById('streamIdFilter').selectedIndex].value)"/>
    </td>
</tr>
<tr style="display: none">
    <td id="dynamicHeader" name="<fmt:message key="dynamic.properties.heading"/>"></td>
    <td id="staticHeader" name="<fmt:message key="static.properties.heading"/>"></td>
</tr>
</tbody>
<% } else { %>
<tbody>
<tr>
    <td class="formRaw">
        <table id="noEventBuilderInputTable" class="normal-nopadding"
               style="width:100%">
            <tbody>

            <tr>
                <%if(streamId == null){%>

                <td class="leftCol-med" colspan="2">
                        <span style="float: left; position: relative; margin-top: 5px;">
                            <fmt:message key="event.receiver.error.no.stream"/>
                        </span>
                    <a onclick="createImportedStreamDefinition()"
                       style="background-image:url(images/add.gif);"
                       class="icon-link">
                        Add Event Stream
                    </a>
                </td>


                <%}else{%>
                <td class="leftCol-med"  colspan="2">
                    <fmt:message key="event.receiver.error.no.output.adapter"/>
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