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

import org.wso2.carbon.event.execution.manager.admin.dto.domain.DomainInfoDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.ParameterDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.ScenarioInfoDTO;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Domain;
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
     * Maps given Domain object to DomainInfoDTO object
     *
     * @param domain Domain object needs to be mapped
     * @return Mapped DomainInfoDTO object
     */
    public static DomainInfoDTO mapExecutionManagerTemplate(
            Domain domain) {
        DomainInfoDTO domainInfoDTO = null;

        if (domain != null) {
            domainInfoDTO = new DomainInfoDTO();
            domainInfoDTO.setDomain(domain.getName());
            domainInfoDTO.setDescription(domain.getDescription());
            domainInfoDTO.setScenarioInfoDTOs(mapScenarios(domain.getScenarios().getScenario()));
        }

        return domainInfoDTO;
    }


    /**
     * Maps given list of Domain objects to array of DomainInfoDTO objects
     *
     * @param domains List of Domain objects needs to be mapped
     * @return Mapped array of DomainInfoDTO objects
     */
    public static DomainInfoDTO[] mapExecutionManagerTemplates(
            List<Domain> domains) {
        DomainInfoDTO[] domainInfoDTO = null;

        if (domains != null) {
            domainInfoDTO = new DomainInfoDTO[domains.size()];

            for (int i = 0; i < domainInfoDTO.length; i++) {
                domainInfoDTO[i] = mapExecutionManagerTemplate(domains.get(i));
            }
        }
        return domainInfoDTO;
    }


    private static ScenarioInfoDTO[] mapScenarios(List<Scenario> scenarios) {
        ScenarioInfoDTO[] scenarioInfoDTOs = new ScenarioInfoDTO[scenarios.size()];
        int i = 0;
        for (Scenario scenario : scenarios) {
            ScenarioInfoDTO scenarioInfoDTO = new ScenarioInfoDTO();
            scenarioInfoDTO.setType(scenario.getType());
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
