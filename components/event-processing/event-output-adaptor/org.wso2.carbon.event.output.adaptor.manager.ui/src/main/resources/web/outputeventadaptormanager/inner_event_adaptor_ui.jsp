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

<fmt:bundle basename="org.wso2.carbon.event.output.adaptor.manager.ui.i18n.Resources">

    <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
    <%@ page
            import="org.wso2.carbon.event.output.adaptor.manager.ui.OutputEventAdaptorUIUtils" %>
    <%@ page
            import="org.wso2.carbon.event.output.adaptor.manager.stub.OutputEventAdaptorManagerAdminServiceStub" %>
    <%@ page
            import="org.wso2.carbon.event.output.adaptor.manager.stub.types.OutputEventAdaptorPropertiesDto" %>
    <%@ page
            import="org.wso2.carbon.event.output.adaptor.manager.stub.types.OutputEventAdaptorPropertyDto" %>


    <script type="text/javascript"
            src="../outputeventadaptormanager/js/create_event_adaptor_helper.js"></script>

    <table id="eventInputTable" class="normal-nopadding"
           style="width:100%">
        <tbody>

        <tr>
            <td class="leftCol-med"><fmt:message
                    key="event.adaptor.name"/><span
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
            <td><fmt:message key="event.adaptor.type"/><span
                    class="required">*</span></td>
            <td><select name="eventTypeFilter"
                        onchange="showEventProperties('<fmt:message key="output.event.all.properties"/>')"
                        id="eventTypeFilter">
                <%
                    OutputEventAdaptorManagerAdminServiceStub stub = OutputEventAdaptorUIUtils.getOutputEventManagerAdminService(config, session, request);
                    String[] eventNames = stub.getOutputEventAdaptorTypeNames();
                    OutputEventAdaptorPropertiesDto eventAdaptorPropertiesDto = null;
                    String firstEventName = null;
                    String supportedEventAdaptorType = null;
                    if (eventNames != null) {
                        firstEventName = eventNames[0];
                        eventAdaptorPropertiesDto = stub.getOutputEventAdaptorProperties(firstEventName);
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
            if ((eventAdaptorPropertiesDto.getOutputEventAdaptorPropertyDtos()) != null) {

        %>
        <tr>
            <td colspan="2"><b>
                <fmt:message key="output.event.all.properties"/> </b>
            </td>
        </tr>

        <%
            }

            if (firstEventName != null) {

        %>


        <%
            //input fields for output event adaptor properties
            if ((eventAdaptorPropertiesDto.getOutputEventAdaptorPropertyDtos()) != null & firstEventName != null) {

                OutputEventAdaptorPropertyDto[] outputEventProperties = eventAdaptorPropertiesDto.getOutputEventAdaptorPropertyDtos();

                if (outputEventProperties != null) {
                    for (int index = 0; index < outputEventProperties.length; index++) {
        %>
        <tr>

            <td class="leftCol-med"><%=outputEventProperties[index].getDisplayName()%>
                <%
                    String propertyId = "outputProperty_";
                    if (outputEventProperties[index].getRequired()) {
                        propertyId = "outputProperty_Required_";

                %>
                <span class="required">*</span>
                <%
                    }
                %>
            </td>
            <%
                String type = "text";
                if (outputEventProperties[index].getSecured()) {
                    type = "password";
                }
            %>

            <td>
                <div class=outputFields>
                    <%

                        if (outputEventProperties[index].getOptions()[0] != null) {

                    %>

                    <select name="<%=outputEventProperties[index].getKey()%>"
                            id="<%=propertyId%><%=index%>">

                        <%
                            for (String property : outputEventProperties[index].getOptions()) {
                                if (property.equals(outputEventProperties[index].getDefaultValue())) {
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
                           name="<%=outputEventProperties[index].getKey()%>"
                           id="<%=propertyId%><%=index%>" class="initE"
                           style="width:75%"
                           value="<%= (outputEventProperties[index].getDefaultValue()) != null ? outputEventProperties[index].getDefaultValue() : "" %>"/>

                    <% }

                        if (outputEventProperties[index].getHint() != null) { %>
                    <div class="sectionHelp">
                        <%=outputEventProperties[index].getHint()%>
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
