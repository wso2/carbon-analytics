<%@ page import="org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub" %>
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
            label="event.builder.add"
            resourceBundle="org.wso2.carbon.event.builder.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <link type="text/css" href="../eventbuilder/css/cep.css" rel="stylesheet"/>
    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="../eventbuilder/js/subscriptions.js"></script>
    <script type="text/javascript" src="../eventbuilder/js/eventing_utils.js"></script>
    <script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
    <script type="text/javascript" src="../eventbuilder/js/event_builders.js"></script>
    <script type="text/javascript" src="../eventbuilder/js/create_event_builder_helper.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>

    <div id="custom_dcontainer" style="display:none"></div>
    <div id="middle">
        <h2><fmt:message key="create.event.builder"/></h2>

        <div id="workArea">
            <%
                EventBuilderAdminServiceStub eventBuilderStub = EventBuilderUIUtils.getEventBuilderAdminService(config, session, request);
                EventStreamAdminServiceStub eventStreamAdminServiceStub = EventBuilderUIUtils.getEventStreamAdminService(config,session,request);
                InputEventAdaptorInfoDto[] InputEventAdaptorInfo = eventBuilderStub.getInputEventAdaptorInfo();
                String[] streamNamesWitheVersion = eventStreamAdminServiceStub.getStreamNames();

                if (InputEventAdaptorInfo != null && streamNamesWitheVersion != null) {
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
                            <%@include file="inner_eventbuilder_ui.jsp" %>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="buttonRow">
                            <input type="button" value="Add Event Builder"
                                   onclick="addEventBuilder(document.getElementById('addEventBuilder'))"/>
                        </td>
                    </tr>
                    </tbody>

                </table>
            </form>
            <%
            } else {
            %>

            <table style="width:100%" id="ebNoAdd" class="styledLeft">

                <tbody>
                <tr>
                    <td class="formRaw">
                        <table id="noEventBuilderInputTable" class="normal-nopadding"
                               style="width:100%">
                            <tbody>

                            <tr>
                                <td class="leftCol-med" colspan="2">Event Streams and/or Input Event Adaptors are not available
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
            %>
        </div>
    </div>
</fmt:bundle>
