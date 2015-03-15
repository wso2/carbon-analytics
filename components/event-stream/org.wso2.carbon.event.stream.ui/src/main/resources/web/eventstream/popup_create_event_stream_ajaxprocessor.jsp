<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto" %>
<%@ page import="org.wso2.carbon.event.stream.ui.EventStreamUIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.stream.ui.i18n.Resources">
<script type="text/javascript" src="../eventstream/js/event_stream.js"></script>
<script type="text/javascript" src="../eventstream/js/registry-browser.js"></script>

<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript"
        src="../eventstream/js/create_eventStream_helper.js"></script>

<%
    String streamDefStrJson = request.getParameter("streamDef");
    String callback = request.getParameter("callback");
    EventStreamDefinitionDto eventStreamDefinitionDto = null;
    if (streamDefStrJson != null && !streamDefStrJson.isEmpty()) {
        eventStreamDefinitionDto = EventStreamUIUtils.getEventStreamInfoDtoFrom(streamDefStrJson);
    }


%>
<div id="middle">
<h2><fmt:message key="title.event.stream.create"/></h2>

<div id="workArea">

<form name="inputForm" action="#" method="post" id="addEventStream">
<table style="width:100%" id="eventStreamAdd" class="styledLeft">
<thead>
<tr>
    <th><fmt:message key="title.event.stream.details"/></th>
</tr>
</thead>
<tbody>
<tr>
<td class="formRaw">

<table id="eventStreamInputTable" class="normal-nopadding"
       style="width:100%">
<tbody>

<tr>
    <td class="leftCol-med"><fmt:message key="event.stream.name"/><span
            class="required">*</span>
    </td>
    <td>
        <%
            if (eventStreamDefinitionDto != null) {
        %>
        <input type="text" name="eventStreamName" id="eventStreamNameId"
               class="initE"
               value="<%=eventStreamDefinitionDto.getName()%>"
               style="width:75%"/>
        <%
        } else {
        %>
        <input type="text" name="eventStreamName" id="eventStreamNameId"
               class="initE" value=""
               style="width:75%"/>
        <%
            }
        %>
        <div class="sectionHelp">
            <fmt:message key="event.stream.name.help"/>
        </div>
    </td>
</tr>

<tr>
    <td class="leftCol-med"><fmt:message key="event.stream.version"/><span class="required">*</span>
    </td>
    <td>
        <%
            if (eventStreamDefinitionDto != null) {
        %>
        <input type="text" name="eventStreamVersion" id="eventStreamVersionId"
               class="initE"
               value="<%= eventStreamDefinitionDto.getVersion() %>"
               style="width:75%"/>
        <%
        } else {
        %>
        <input type="text" name="eventStreamVersion" id="eventStreamVersionId"
               class="initE" value=""
               style="width:75%"/>
        <%
            }
        %>
        <div class="sectionHelp">
            <fmt:message key="event.stream.version.help"/>
        </div>
    </td>
</tr>

<tr>
    <td class="leftCol-med"><fmt:message key="event.stream.description"/>
    </td>
    <td><input type="text" name="eventStreamDescription" id="eventStreamDescription"
               class="initE"

               value=""
               style="width:75%"/>

        <div class="sectionHelp">
            <fmt:message key="event.stream.description.help"/>
        </div>
    </td>
</tr>

<tr>
    <td class="leftCol-med"><fmt:message key="event.stream.nickname"/>
    </td>
    <td><input type="text" name="eventStreamNickName" id="eventStreamNickName"
               class="initE"

               value=""
               style="width:75%"/>

        <div class="sectionHelp">
            <fmt:message key="event.stream.nickname.help"/>
        </div>
    </td>
</tr>

<tr>
<td colspan="2">
<div id="innerDiv4">
<table class="styledLeft noBorders spacer-bot"
       style="width:100%">
<tbody>
<tr name="streamAttributes">
    <td colspan="2" class="middle-header">
        <fmt:message key="stream.attributes"/>
    </td>
</tr>

<tr name="streamAttributes">
    <td colspan="2">

        <h6><fmt:message key="attribute.data.type.meta"/></h6>
        <% if (eventStreamDefinitionDto != null && eventStreamDefinitionDto.getMetaData() != null) { %>
        <table class="styledLeft noBorders spacer-bot" id="outputMetaDataTable">
            <thead>
            <th class="leftCol-med"><fmt:message key="attribute.name"/></th>
            <th class="leftCol-med"><fmt:message key="attribute.type"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
            <tbody>
            <%
                EventStreamAttributeDto[] metaAttributes = eventStreamDefinitionDto.getMetaData();
                for (EventStreamAttributeDto metaAttrib : metaAttributes) {
            %>
            <tr>
                <td><%=metaAttrib.getAttributeName()%>
                </td>
                <td><%=metaAttrib.getAttributeType()%>
                </td>
                <td><a class="icon-link" style="background-image:url(../admin/images/delete.gif)"
                       onclick="removeStreamAttribute(this,'meta')">Delete</a></td>
            </tr>
            <%
                }
            %>
            </tbody>
        </table>
        <% } else { %>
        <table class="styledLeft noBorders spacer-bot" id="outputMetaDataTable"
               style="display:none">
            <thead>
            <th class="leftCol-med"><fmt:message key="attribute.name"/></th>
            <th class="leftCol-med"><fmt:message key="attribute.type"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
        </table>
        <div class="noDataDiv-plain" id="noOutputMetaData">
            <fmt:message key="no.meta.attributes.defined"/>
        </div>
        <%
            }
        %>
        <table id="addMetaData" class="normal">
            <tbody>
            <tr>
                <td class="col-small"><fmt:message key="attribute.name"/> :</td>
                <td>
                    <input type="text" id="outputMetaDataPropName"/>
                </td>
                <td class="col-small"><fmt:message key="attribute.type"/> :
                </td>
                <td>
                    <select id="outputMetaDataPropType">
                        <option value="int">int</option>
                        <option value="long">long</option>
                        <option value="double">double</option>
                        <option value="float">float</option>
                        <option value="string">string</option>
                        <option value="boolean">boolean</option>
                    </select>
                </td>
                <td><input type="button" class="button"
                           value="<fmt:message key="add"/>"
                           onclick="addStreamAttribute('Meta')"/>
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
<tr name="streamAttributes">
    <td colspan="2">

        <h6><fmt:message key="attribute.data.type.correlation"/></h6>
        <% if (eventStreamDefinitionDto != null && eventStreamDefinitionDto.getCorrelationData() != null) { %>
        <table class="styledLeft noBorders spacer-bot"
               id="outputCorrelationDataTable">
            <thead>
            <th class="leftCol-med"><fmt:message key="attribute.name"/></th>
            <th class="leftCol-med"><fmt:message key="attribute.type"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
            <tbody>
            <%
                EventStreamAttributeDto[] correlationAttributes = eventStreamDefinitionDto.getCorrelationData();
                for (EventStreamAttributeDto correlationAttrib : correlationAttributes) {
            %>
            <tr>
                <td><%=correlationAttrib.getAttributeName()%>
                </td>
                <td><%=correlationAttrib.getAttributeType()%>
                </td>
                <td><a class="icon-link" style="background-image:url(../admin/images/delete.gif)"
                       onclick="removeStreamAttribute(this,'meta')">Delete</a></td>
            </tr>
            <%
                }
            %>
            </tbody>
        </table>
        <% } else { %>
        <table class="styledLeft noBorders spacer-bot"
               id="outputCorrelationDataTable" style="display:none">
            <thead>
            <th class="leftCol-med"><fmt:message key="attribute.name"/></th>
            <th class="leftCol-med"><fmt:message key="attribute.type"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
        </table>
        <% } %>
        <div class="noDataDiv-plain" id="noOutputCorrelationData">
            <fmt:message key="no.correlation.attributes.defined"/>
        </div>
        <table id="addCorrelationData" class="normal">
            <tbody>
            <tr>
                <td class="col-small"><fmt:message key="attribute.name"/> :</td>
                <td>
                    <input type="text" id="outputCorrelationDataPropName"/>
                </td>
                <td class="col-small"><fmt:message key="attribute.type"/> :
                </td>
                <td>
                    <select id="outputCorrelationDataPropType">
                        <option value="int">int</option>
                        <option value="long">long</option>
                        <option value="double">double</option>
                        <option value="float">float</option>
                        <option value="string">string</option>
                        <option value="boolean">boolean</option>
                    </select>
                </td>
                <td><input type="button" class="button"
                           value="<fmt:message key="add"/>"
                           onclick="addStreamAttribute('Correlation')"/>
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
<tr name="streamAttributes">
    <td colspan="2">

        <h6><fmt:message key="attribute.data.type.payload"/></h6>
        <%
            if (eventStreamDefinitionDto != null && eventStreamDefinitionDto.getPayloadData() != null) {
        %>
        <table class="styledLeft noBorders spacer-bot"
               id="outputPayloadDataTable">
            <thead>
            <th class="leftCol-med"><fmt:message key="attribute.name"/></th>
            <th class="leftCol-med"><fmt:message key="attribute.type"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
            <tbody>
            <%
                EventStreamAttributeDto[] payloadAttributes = eventStreamDefinitionDto.getPayloadData();
                for (EventStreamAttributeDto payloadAttrib : payloadAttributes) {
            %>
            <tr>
                <td><%=payloadAttrib.getAttributeName()%>
                </td>
                <td><%=payloadAttrib.getAttributeType()%>
                </td>
                <td><a class="icon-link" style="background-image:url(../admin/images/delete.gif)"
                       onclick="removeStreamAttribute(this,'meta')">Delete</a></td>
            </tr>
            <%
                }
            %>
            </tbody>
        </table>
        <%
        } else {
        %>
        <table class="styledLeft noBorders spacer-bot"
               id="outputPayloadDataTable" style="display:none">
            <thead>
            <th class="leftCol-med"><fmt:message key="attribute.name"/></th>
            <th class="leftCol-med"><fmt:message key="attribute.type"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
        </table>
        <div class="noDataDiv-plain" id="noOutputPayloadData">
            <fmt:message key="no.payload.attributes.defined"/>
        </div>
        <%
            }
        %>
        <table id="addPayloadData" class="normal">
            <tbody>
            <tr>
                <td class="col-small"><fmt:message key="attribute.name"/> :</td>
                <td>
                    <input type="text" id="outputPayloadDataPropName"/>
                </td>
                <td class="col-small"><fmt:message key="attribute.type"/> :
                </td>
                <td>
                    <select id="outputPayloadDataPropType">
                        <option value="int">int</option>
                        <option value="long">long</option>
                        <option value="double">double</option>
                        <option value="float">float</option>
                        <option value="string">string</option>
                        <option value="boolean">boolean</option>
                    </select>
                </td>
                <td><input type="button" class="button"
                           value="<fmt:message key="add"/>"
                           onclick="addStreamAttribute('Payload')"/>
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
</tbody>
</table>
</div>
</td>
</tr>
</tbody>
</table>
</td>
</tr>
<tr>
    <td class="buttonRow">
        <input type="button" value="<fmt:message key="add.event.stream"/>"
               onclick="addEventStreamViaPopup(document.getElementById('addEventStream'),'<%=callback%>')"/>
    </td>
</tr>
</tbody>
</table>
</form>
</div>
</div>
</fmt:bundle>