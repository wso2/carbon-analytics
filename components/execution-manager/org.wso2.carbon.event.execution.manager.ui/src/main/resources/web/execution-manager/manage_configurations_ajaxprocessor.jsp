<%@ page import="org.wso2.carbon.event.execution.manager.stub.ExecutionManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.execution.manager.ui.ExecutionManagerUIUtils" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.TemplateConfigurationDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.ParameterDTOE" %>
<%@ page import="org.apache.axis2.AxisFault" %>
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

<%

    String domainName = request.getParameter("domainName");
    String configuration = request.getParameter("configurationName");
    String saveType = request.getParameter("saveType");
    String description = request.getParameter("description");
    String parametersJson = request.getParameter("parameters");
    String templateType = request.getParameter("templateType");
    String cronExpression = request.getParameter("executionParameters");
    String valueSeparator = "::";

    ParameterDTOE[] parameters;

    ExecutionManagerAdminServiceStub proxy = ExecutionManagerUIUtils.getExecutionManagerAdminService(config, session);
    try {
        if (saveType.equals("delete")) {
            proxy.deleteConfiguration(domainName, configuration);
        } else {

            TemplateConfigurationDTO templateConfigurationDTO = new TemplateConfigurationDTO();

            templateConfigurationDTO.setName(configuration);
            templateConfigurationDTO.setFrom(domainName);
            templateConfigurationDTO.setDescription(description);
            templateConfigurationDTO.setType(templateType);

            if (parametersJson.length() < 1) {
               parameters = new ParameterDTOE[0];

            } else {
                String[] parameterStrings = parametersJson.split(",");
                parameters = new ParameterDTOE[parameterStrings.length];
                int index = 0;

                for (String parameterString : parameterStrings) {
                    ParameterDTOE parameterDTO = new ParameterDTOE();
                    parameterDTO.setName(parameterString.split(valueSeparator)[0]);
                    parameterDTO.setValue(parameterString.split(valueSeparator)[1]);
                    parameters[index] = parameterDTO;
                    index++;
                }
            }

            templateConfigurationDTO.setParameterDTOs(parameters);

            if (cronExpression != null && cronExpression.length() > 0) {
                        templateConfigurationDTO.setExecutionParameters(cronExpression);
            }

            proxy.saveConfiguration(templateConfigurationDTO);
        }

    } catch (AxisFault e) {
        response.sendError(500);
    }


%>