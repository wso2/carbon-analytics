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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.ui.InputEventAdaptorUIUtils" %>
<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorPropertiesDto" %>
<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.stub.InputEventAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorPropertyDto" %>
<fmt:bundle basename="org.wso2.carbon.event.input.adaptor.manager.ui.i18n.Resources">


    <script type="text/javascript"
            src="../inputeventadaptormanager/js/create_event_adaptor_helper.js"></script>

    <table id="eventInputTable" class="normal-nopadding"
           style="width:100%">
        <tbody>

        <tr>
            <td class="leftCol-med"><fmt:message key="event.adaptor.name"/><span
                    class="required">*</span>
            </td>
            <td><input type="text" name="eventName" id="eventNameId"
                       class="initE"
                       onclick="clearTextIn(this)" onblur="fillTextIn(this)"
                       value=""
                       style="width:75%"/>

                <div class="sectionHelp">
                    <fmt:message key="event.adaptor.name.help"/>
                </div>

            </td>
        </tr>
        <tr>
            <td><fmt:message key="event.adaptor.type"/><span class="required">*</span></td>
            <td><select name="eventTypeFilter"
                        onchange="showEventProperties('<fmt:message key="input.event.all.properties"/>')"
                        id="eventTypeFilter">
                <%
                    InputEventAdaptorManagerAdminServiceStub stub = InputEventAdaptorUIUtils.getInputEventManagerAdminService(config, session, request);
                    String[] eventNames = stub.getAllInputEventAdaptorTypeNames();
                    InputEventAdaptorPropertiesDto eventAdaptorPropertiesDto = null;
                    String firstEventName = null;
                    String supportedEventAdaptorType = null;
                    if (eventNames != null) {
                        firstEventName = eventNames[0];
                        eventAdaptorPropertiesDto = stub.getInputEventAdaptorProperties(firstEventName);
                        for (String type : eventNames) {
                %>
                <option><%=type%>
                </option>
                <%
                        }
                    }
                %>
            </select>

                <div class="sectionHelp">
                    <fmt:message key="event.adaptor.type.help"/>
                </div>
            </td>

        </tr>

        <%
            if ((eventAdaptorPropertiesDto.getInputEventAdaptorPropertyDtos()) != null) {

        %>
        <tr>
            <td colspan="2"><b>
                <fmt:message key="input.event.all.properties"/> </b>
            </td>
        </tr>

        <%
            }

            if (firstEventName != null) {

        %>


        <%
            //Input fields for input event adaptor properties
            if ((eventAdaptorPropertiesDto.getInputEventAdaptorPropertyDtos()) != null & firstEventName != null) {

                InputEventAdaptorPropertyDto[] inputEventProperties = eventAdaptorPropertiesDto.getInputEventAdaptorPropertyDtos();

                if (inputEventProperties != null) {
                    for (int index = 0; index < inputEventProperties.length; index++) {
        %>
        <tr>

            <td class="leftCol-med"><%=inputEventProperties[index].getDisplayName()%>
                <%
                    String propertyId = "inputProperty_";
                    if (inputEventProperties[index].getRequired()) {
                        propertyId = "inputProperty_Required_";

                %>
                <span class="required">*</span>
                <%
                    }
                %>
            </td>
            <%
                String type = "text";
                if (inputEventProperties[index].getSecured()) {
                    type = "password";
                }
            %>

            <td>
                <div class=inputFields>
                    <%

                        if (inputEventProperties[index].getOptions()[0] != null) {

                    %>

                    <select name="<%=inputEventProperties[index].getKey()%>"
                            id="<%=propertyId%><%=index%>">

                        <%
                            for (String property : inputEventProperties[index].getOptions()) {
                                if (property.equals(inputEventProperties[index].getDefaultValue())) {
                        %>
                        <option selected="selected"><%=property%>
                        </option>
                        <% } else { %>
                        <option><%=property%>
                        </option>
                        <% }
                        }%>
                    </select>

                    <% } else { %>
                    <input type="<%=type%>"
                           name="<%=inputEventProperties[index].getKey()%>"
                           id="<%=propertyId%><%=index%>" class="initE"
                           style="width:75%"
                           value="<%= (inputEventProperties[index].getDefaultValue()) != null ? inputEventProperties[index].getDefaultValue() : "" %>"/>

                    <% }

                        if (inputEventProperties[index].getHint() != null) { %>
                    <div class="sectionHelp">
                        <%=inputEventProperties[index].getHint()%>
                    </div>
                    <% } %>
                </div>
            </td>

        </tr>
        <%
                        }
                    }
                }
            }
        %>

        </tbody>
    </table>
</fmt:bundle>