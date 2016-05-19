/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.execution.manager.admin.internal.util;

import org.wso2.carbon.event.execution.manager.admin.dto.domain.ExecutionManagerTemplateInfoDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.ParameterDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.ScenarioInfoDTO;
import org.wso2.carbon.event.execution.manager.core.structure.domain.ExecutionManagerTemplate;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Parameter;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Scenario;

import java.util.List;

/**
 * Consist of require mapping methods of Domains
 */
public class DomainMapper {

    /**
     * To avoid instantiating
     */
    private DomainMapper() {
    }


    /**
     * Maps given ExecutionManagerTemplate object to ExecutionManagerTemplateInfoDTO object
     *
     * @param executionManagerTemplate ExecutionManagerTemplate object needs to be mapped
     * @return Mapped ExecutionManagerTemplateInfoDTO object
     */
    public static ExecutionManagerTemplateInfoDTO mapExecutionManagerTemplate(
            ExecutionManagerTemplate executionManagerTemplate) {
        ExecutionManagerTemplateInfoDTO executionManagerTemplateInfoDTO = null;

        if (executionManagerTemplate != null) {
            executionManagerTemplateInfoDTO = new ExecutionManagerTemplateInfoDTO();
            executionManagerTemplateInfoDTO.setDomain(executionManagerTemplate.getDomain());
            executionManagerTemplateInfoDTO.setDescription(executionManagerTemplate.getDescription());
            executionManagerTemplateInfoDTO.setScenarioInfoDTOs(mapScenarios(executionManagerTemplate.getScenarios().getScenario()));
        }

        return executionManagerTemplateInfoDTO;
    }


    /**
     * Maps given list of ExecutionManagerTemplate objects to array of ExecutionManagerTemplateInfoDTO objects
     *
     * @param executionManagerTemplates List of ExecutionManagerTemplate objects needs to be mapped
     * @return Mapped array of ExecutionManagerTemplateInfoDTO objects
     */
    public static ExecutionManagerTemplateInfoDTO[] mapExecutionManagerTemplates(
            List<ExecutionManagerTemplate> executionManagerTemplates) {
        ExecutionManagerTemplateInfoDTO[] executionManagerTemplateInfoDTO = null;

        if (executionManagerTemplates != null) {
            executionManagerTemplateInfoDTO = new ExecutionManagerTemplateInfoDTO[executionManagerTemplates.size()];

            for (int i = 0; i < executionManagerTemplateInfoDTO.length; i++) {
                executionManagerTemplateInfoDTO[i] = mapExecutionManagerTemplate(executionManagerTemplates.get(i));
            }
        }
        return executionManagerTemplateInfoDTO;
    }


    private static ScenarioInfoDTO[] mapScenarios(List<Scenario> scenarios) {
        ScenarioInfoDTO[] scenarioInfoDTOs = new ScenarioInfoDTO[scenarios.size()];
        int i = 0;
        for (Scenario scenario : scenarios) {
            ScenarioInfoDTO scenarioInfoDTO = new ScenarioInfoDTO();
            scenarioInfoDTO.setName(scenario.getName());
            scenarioInfoDTO.setDescription(scenario.getDescription());
            scenarioInfoDTO.setParameterDTOs(mapParameters(scenario.getParameters().getParameter()));
            scenarioInfoDTOs[i] = scenarioInfoDTO;
            i++;
        }
        return scenarioInfoDTOs;
    }


    private static ParameterDTO[] mapParameters(List<Parameter> parameters) {
        ParameterDTO[] parameterDTOs = new ParameterDTO[parameters.size()];
        int i = 0;
        for (Parameter parameter: parameters) {
            ParameterDTO parameterDTO = new ParameterDTO();
            parameterDTO.setName(parameter.getName());
            parameterDTO.setType(parameter.getType());
            parameterDTO.setDefaultValue(parameter.getDefaultValue());
            parameterDTO.setDescription(parameter.getDescription());
            parameterDTO.setDisplayName(parameter.getDisplayName());
            parameterDTO.setOptions(parameter.getOptions());
            parameterDTOs[i] = parameterDTO;
            i++;
        }
        return parameterDTOs;
    }
}
