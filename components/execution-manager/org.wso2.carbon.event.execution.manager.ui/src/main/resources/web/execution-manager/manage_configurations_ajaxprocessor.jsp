<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ page import="org.wso2.carbon.event.execution.manager.stub.ExecutionManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.execution.manager.ui.ExecutionManagerUIUtils" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.ScenarioConfigurationDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.ConfigurationParameterDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.StreamMappingDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.AttributeMappingDTO" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>
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

<fmt:bundle basename="org.wso2.carbon.event.execution.manager.ui.i18n.Resources">
<%

    if (!"post".equalsIgnoreCase(request.getMethod())) {
        response.sendError(405);
        return;
    }

    String domainName = request.getParameter("domainName");
    String configuration = request.getParameter("configurationName");
    String saveType = request.getParameter("saveType");
    String description = request.getParameter("description");
    String parametersJson = request.getParameter("parameters");
    String templateType = request.getParameter("templateType");
    String valueSeparator = "::";

    ConfigurationParameterDTO[] parameters;

    ExecutionManagerAdminServiceStub proxy = ExecutionManagerUIUtils.getExecutionManagerAdminService(config, session);
    try {
        if (saveType.equals("delete")) {
            proxy.deleteConfiguration(domainName, configuration);
        } else {

            ScenarioConfigurationDTO scenarioConfigurationDTO = new ScenarioConfigurationDTO();

            scenarioConfigurationDTO.setName(configuration);
            scenarioConfigurationDTO.setDomain(domainName);
            scenarioConfigurationDTO.setDescription(description);
            scenarioConfigurationDTO.setType(templateType);

            if (parametersJson.length() < 1) {
               parameters = new ConfigurationParameterDTO[0];

            } else {
                String[] parameterStrings = parametersJson.split(",\n");
                parameters = new ConfigurationParameterDTO[parameterStrings.length];
                int index = 0;

                for (String parameterString : parameterStrings) {
                    String[] parameterEntities = parameterString.split(valueSeparator);
                    ConfigurationParameterDTO parameterDTO = new ConfigurationParameterDTO();
                    parameterDTO.setName(parameterEntities[0]);
                    parameterDTO.setValue(parameterEntities[1]);
                    parameters[index] = parameterDTO;
                    index++;
                }
            }

            scenarioConfigurationDTO.setConfigurationParameterDTOs(parameters);

            //toStreamIDArray.length defines the number of stream mappings per configuration
            String[] toStreamIDArray = proxy.saveConfiguration(scenarioConfigurationDTO);

            //when stream mapping is disabled, stub returns a string array with a null element hence need to check for toStreamIDArray[0]
            if (toStreamIDArray[0] != null) {
                String toStreamNameID="";
                String fromStreamNameID="";

                EventStreamAdminServiceStub eventStreamAdminServiceStub = ExecutionManagerUIUtils.getEventStreamAdminService(config,
                        session, request);
                String[] fromStreamIds = eventStreamAdminServiceStub.getStreamNames();

                for (int i = 0; i < toStreamIDArray.length; i++) {
                    toStreamNameID = toStreamIDArray[i];
%>
    <div class="container col-md-12 marg-top-20" id="streamMappingConfigurationID_<%=i%>">

        <h4><fmt:message key='template.stream.header.text'/></h4>

        <label class="input-label col-md-5"><fmt:message key='template.label.from.stream.name'/></label>

        <div class="input-control input-full-width col-md-7 text">
            <select id="fromStreamID_<%=i%>" onchange="loadMappingFromStreamAttributes(<%=i%>)">
                <option selected disabled>Select an input stream to map</option>
                <%
                    if (fromStreamIds != null) {
                        Arrays.sort(fromStreamIds);
                        for (String aStreamId : fromStreamIds) {
                            fromStreamNameID = aStreamId;
                %>
                <option id="fromStreamOptionID"><%=fromStreamNameID%>
                </option>
                <%
                        }
                    }
                %>
            </select>
        </div>

        <label class="input-label col-md-5"><fmt:message key='template.label.to.stream.name'/></label>

        <div class="input-control input-full-width col-md-7 text">
            <input type="text" id="toStreamID_<%=i%>"
                   value="<%=toStreamNameID%>" readonly="true"/>
        </div>

        <div id="outerDiv_<%=i%>">
        </div>

    </div>
    <br class="c-both"/>
    <hr class="wr-separate"/>
    <%
        }
    %>

    <div class="action-container">
        <button type="button"
                class="btn btn-default btn-add col-md-2 col-xs-12 pull-right marg-right-15"
                onclick="saveStreamConfiguration('<%=toStreamIDArray.length%>','<%=domainName%>','<%=configuration%>')">
            <fmt:message key='template.add.stream.button.text'/>
        </button>
    </div>
    <%
    } else {
    %>
    return
    <%
                }
            }
        } catch (AxisFault e) {
            response.sendError(500);
        }
    %>
</fmt:bundle>