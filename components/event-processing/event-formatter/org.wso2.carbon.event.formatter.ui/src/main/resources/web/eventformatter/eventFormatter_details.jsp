<%@ page
        import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>

<%@ page
        import="org.wso2.carbon.event.formatter.stub.types.EventFormatterConfigurationDto" %>
<%@ page
        import="org.wso2.carbon.event.formatter.stub.types.EventFormatterPropertyDto" %>
<%@ page import="org.wso2.carbon.event.formatter.stub.types.EventOutputPropertyDto" %>
<%@ page import="org.wso2.carbon.event.formatter.stub.types.ToPropertyConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.formatter.ui.EventFormatterUIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.formatter.ui.i18n.Resources">

<carbon:breadcrumb
        label="event.formatter.details.breabcrumb"
        resourceBundle="org.wso2.carbon.event.formatter.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<link type="text/css" href="css/eventFormatter.css" rel="stylesheet"/>
<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../eventformatter/js/event_formatter.js"></script>
<script type="text/javascript" src="../eventformatter/js/registry-browser.js"></script>

<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>


<%
    EventFormatterAdminServiceStub stub = EventFormatterUIUtils.getEventFormatterAdminService(config, session, request);
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
<h2><fmt:message key="event.formatter.details"/></h2>

<div id="workArea">

<form name="inputForm" action="index.jsp?ordinal=1" method="get">
<table style="width:100%" id="eventFormatterAdd" class="styledLeft">
<thead>
<tr>
    <th><fmt:message key="title.event.formatter.details"/></th>
</tr>
</thead>
<% String eventFormatterName = request.getParameter("eventFormatterName");
    if (eventFormatterName != null) {
        EventFormatterConfigurationDto eventFormatterConfigurationDto = stub.getActiveEventFormatterConfiguration(eventFormatterName);
%>
<tbody>
<tr>
<td class="formRaw">
<table id="eventFormatterInputTable" class="normal-nopadding"
       style="width:100%">
<tbody>

<tr>
    <td class="leftCol-med"><fmt:message key="event.formatter.name"/><span class="required">*</span>
    </td>
    <td><input type="text" name="eventFormatterName" id="eventFormatterId"
               class="initE"

               value="<%= eventFormatterConfigurationDto.getEventFormatterName()%>"
               style="width:75%" disabled="disabled"/>
    </td>
</tr>


<tr>
    <td><fmt:message key="event.stream.name"/><span class="required">*</span></td>
    <td><select name="streamNameFilter" id="streamNameFilter" disabled="disabled">
        <%

        %>
        <option><%=eventFormatterConfigurationDto.getFromStreamNameWithVersion()%>
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
                  disabled="disabled"><%=eventFormatterConfigurationDto.getStreamDefinition()%>
        </textArea>
    </td>

</tr>

<tr>
    <td><fmt:message key="event.adaptor.name"/><span class="required">*</span></td>
    <td><select name="eventAdaptorNameFilter"
                id="eventAdaptorNameFilter" disabled="disabled">
        <option><%=eventFormatterConfigurationDto.getToPropertyConfigurationDto().getEventAdaptorName()%>
        </option>
    </select>
    </td>
</tr>

<tr>
    <td><fmt:message key="mapping.type"/><span class="required">*</span></td>
    <td><select name="mappingTypeFilter"
                id="mappingTypeFilter" disabled="disabled">
        <option><%=eventFormatterConfigurationDto.getMappingType()%>
        </option>
    </select>
    </td>

</tr>

<tr>
<td class="formRaw" colspan="2">
<div id="outerDiv">
<%
    if (eventFormatterConfigurationDto.getMappingType().equalsIgnoreCase("wso2event")) {
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
                <% if (eventFormatterConfigurationDto.getWso2EventOutputMappingDto().getMetaWSO2EventOutputPropertyConfigurationDto() != null && eventFormatterConfigurationDto.getWso2EventOutputMappingDto().getMetaWSO2EventOutputPropertyConfigurationDto()[0] != null) { %>
                <table class="styledLeft noBorders spacer-bot" id="outputMetaDataTable">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th class="leftCol-med"><fmt:message key="property.type"/></th>
                    </thead>
                    <% EventOutputPropertyDto[] eventOutputPropertyDtos = eventFormatterConfigurationDto.getWso2EventOutputMappingDto().getMetaWSO2EventOutputPropertyConfigurationDto();
                        for (EventOutputPropertyDto eventOutputPropertyDto : eventOutputPropertyDtos) { %>
                    <tr>
                        <td class="property-names"><%=eventOutputPropertyDto.getName()%>
                        </td>
                        <td class="property-names"><%=eventOutputPropertyDto.getValueOf()%>
                        </td>
                        <td class="property-names"><%=eventOutputPropertyDto.getType()%>
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
                <% if (eventFormatterConfigurationDto.getWso2EventOutputMappingDto().getCorrelationWSO2EventOutputPropertyConfigurationDto() != null && eventFormatterConfigurationDto.getWso2EventOutputMappingDto().getCorrelationWSO2EventOutputPropertyConfigurationDto()[0] != null) { %>
                <table class="styledLeft noBorders spacer-bot"
                       id="outputCorrelationDataTable">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th class="leftCol-med"><fmt:message key="property.type"/></th>
                    </thead>
                    <% EventOutputPropertyDto[] eventOutputPropertyDtos = eventFormatterConfigurationDto.getWso2EventOutputMappingDto().getCorrelationWSO2EventOutputPropertyConfigurationDto();
                        for (EventOutputPropertyDto eventOutputPropertyDto : eventOutputPropertyDtos) { %>
                    <tr>
                        <td class="property-names"><%=eventOutputPropertyDto.getName()%>
                        </td>
                        <td class="property-names"><%=eventOutputPropertyDto.getValueOf()%>
                        </td>
                        <td class="property-names"><%=eventOutputPropertyDto.getType()%>
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
                <% if (eventFormatterConfigurationDto.getWso2EventOutputMappingDto().getPayloadWSO2EventOutputPropertyConfigurationDto() != null && eventFormatterConfigurationDto.getWso2EventOutputMappingDto().getPayloadWSO2EventOutputPropertyConfigurationDto()[0] != null) { %>
                <table class="styledLeft noBorders spacer-bot"
                       id="outputPayloadDataTable">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th class="leftCol-med"><fmt:message key="property.type"/></th>
                    </thead>
                    <% EventOutputPropertyDto[] eventOutputPropertyDtos = eventFormatterConfigurationDto.getWso2EventOutputMappingDto().getPayloadWSO2EventOutputPropertyConfigurationDto();
                        for (EventOutputPropertyDto eventOutputPropertyDto : eventOutputPropertyDtos) { %>
                    <tr>
                        <td class="property-names"><%=eventOutputPropertyDto.getName()%>
                        </td>
                        <td class="property-names"><%=eventOutputPropertyDto.getValueOf()%>
                        </td>
                        <td class="property-names"><%=eventOutputPropertyDto.getType()%>
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
} else if (eventFormatterConfigurationDto.getMappingType().equalsIgnoreCase("text")) {
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
        <% if (!(eventFormatterConfigurationDto.getTextOutputMappingDto().getRegistryResource())) {%>
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
                              disabled="disabled"><%= eventFormatterConfigurationDto.getTextOutputMappingDto().getMappingText() %>
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
                       value="<%=eventFormatterConfigurationDto.getTextOutputMappingDto().getMappingText()%>"
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
} else if (eventFormatterConfigurationDto.getMappingType().equalsIgnoreCase("xml")) {
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
        <% if (!eventFormatterConfigurationDto.getXmlOutputMappingDto().getRegistryResource()) { %>
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
                              disabled="disabled"><%=eventFormatterConfigurationDto.getXmlOutputMappingDto().getMappingXMLText()%>
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
} else if (eventFormatterConfigurationDto.getMappingType().equalsIgnoreCase("map")) {
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
        <% if (eventFormatterConfigurationDto.getMapOutputMappingDto().getOutputPropertyConfiguration() != null && eventFormatterConfigurationDto.getMapOutputMappingDto().getOutputPropertyConfiguration()[0] != null) { %>
        <tr name="outputMapMapping">
            <td colspan="2">
                <table class="styledLeft noBorders spacer-bot" id="outputMapPropertiesTable">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    </thead>
                    <% EventOutputPropertyDto[] eventOutputPropertyDtos = eventFormatterConfigurationDto.getMapOutputMappingDto().getOutputPropertyConfiguration();
                        for (EventOutputPropertyDto eventOutputPropertyDto : eventOutputPropertyDtos) { %>
                    <tr>
                        <td class="property-names"><%=eventOutputPropertyDto.getName()%>
                        </td>
                        <td class="property-names"><%=eventOutputPropertyDto.getValueOf()%>
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
} else if (eventFormatterConfigurationDto.getMappingType().equalsIgnoreCase("json")) {
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
        <% if (!eventFormatterConfigurationDto.getJsonOutputMappingDto().getRegistryResource()) { %>
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
                              disabled="disabled"><%=eventFormatterConfigurationDto.getJsonOutputMappingDto().getMappingText()%>
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
                       value="<%=eventFormatterConfigurationDto.getJsonOutputMappingDto().getMappingText()%>"
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

<%
    ToPropertyConfigurationDto toPropertyConfigurationDto = eventFormatterConfigurationDto.getToPropertyConfigurationDto();
    if (toPropertyConfigurationDto != null && toPropertyConfigurationDto.getOutputEventAdaptorMessageConfiguration()[0] != null) {

        for (int index = 0; index < toPropertyConfigurationDto.getOutputEventAdaptorMessageConfiguration().length; index++) {
            EventFormatterPropertyDto[] eventFormatterPropertyDto = toPropertyConfigurationDto.getOutputEventAdaptorMessageConfiguration();
%>
<tr>


    <td class="leftCol-med"><%=eventFormatterPropertyDto[index].getDisplayName()%>
        <%
            String propertyId = "property_";
            if (eventFormatterPropertyDto[index].getRequired()) {
                propertyId = "property_Required_";

        %>
        <span class="required">*</span>
        <%
            }
        %>
    </td>
    <%
        String type = "text";
        if (eventFormatterPropertyDto[index].getSecured()) {
            type = "password";
        }
    %>

    <td>
        <div class=outputFields>
            <%
                if (eventFormatterPropertyDto[index].getOptions()[0] != null) {
            %>

            <select name="<%=eventFormatterPropertyDto[index].getKey()%>"
                    id="<%=propertyId%><%=index%>" disabled="disabled">

                <%
                    for (String property : eventFormatterPropertyDto[index].getOptions()) {
                        if (property.equals(eventFormatterPropertyDto[index].getDefaultValue())) {
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
                   name="<%=eventFormatterPropertyDto[index].getKey()%>"
                   id="<%=propertyId%><%=index%>" class="initE"
                   style="width:75%"
                   value="<%=eventFormatterPropertyDto[index].getValue() %>" disabled="disabled"/>

            <% } %>


        </div>
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
%>
</tbody>
</table>


</form>
</div>
</div>
</fmt:bundle>
