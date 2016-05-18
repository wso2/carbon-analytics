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

import org.wso2.carbon.event.execution.manager.admin.dto.domain.CommonArtifactDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.ExecutionManagerTemplateInfoDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.ParameterDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.ScenarioInfoDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.StreamMappingDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.TemplateDTO;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Artifact;
import org.wso2.carbon.event.execution.manager.core.structure.domain.ExecutionManagerTemplate;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Parameter;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Scenario;
import org.wso2.carbon.event.execution.manager.core.structure.domain.StreamMapping;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Template;

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
     * Maps given list of ExecutionManagerTemplate objects to array of ExecutionManagerTemplateInfoDTO objects
     *
     * @param executionManagerTemplates List of ExecutionManagerTemplate objects needs to be mapped
     * @return Mapped array of ExecutionManagerTemplateInfoDTO objects
     */
    public static ExecutionManagerTemplateInfoDTO[] mapDomainsInfo(List<ExecutionManagerTemplate> executionManagerTemplates) {
        ExecutionManagerTemplateInfoDTO[] executionManagerTemplateInfoDTO = null;

        if (executionManagerTemplates != null) {
            executionManagerTemplateInfoDTO = new ExecutionManagerTemplateInfoDTO[executionManagerTemplates.size()];

            for (int i = 0; i < executionManagerTemplateInfoDTO.length; i++) {
                executionManagerTemplateInfoDTO[i] = mapDomainInfo(executionManagerTemplates.get(i));
            }
        }
        return executionManagerTemplateInfoDTO;
    }

    /**
     * Maps given ExecutionManagerTemplate object to ExecutionManagerTemplateInfoDTO object
     *
     * @param executionManagerTemplate ExecutionManagerTemplate object needs to be mapped
     * @return Mapped ExecutionManagerTemplateInfoDTO object
     */
    public static ExecutionManagerTemplateInfoDTO mapDomainInfo(ExecutionManagerTemplate executionManagerTemplate) {
        ExecutionManagerTemplateInfoDTO executionManagerTemplateInfoDTO = null;

        if (executionManagerTemplate != null) {
            executionManagerTemplateInfoDTO = new ExecutionManagerTemplateInfoDTO();
            executionManagerTemplateInfoDTO.setName(executionManagerTemplate.getDomain());
            executionManagerTemplateInfoDTO.setDescription(executionManagerTemplate.getDescription());
            executionManagerTemplateInfoDTO.setScenarioInfoDTOs(mapScenarios(executionManagerTemplate.getScenarios().getScenario()));
        }

        return executionManagerTemplateInfoDTO;
    }


    private static CommonArtifactDTO[] mapCommonArtifactListToDTO(List<Artifact> artifacts) {
        CommonArtifactDTO[] artifactDTOs = new CommonArtifactDTO[artifacts.size()];
        int i = 0;
        for (Artifact artifact: artifacts) {
            CommonArtifactDTO artifactDTO = new CommonArtifactDTO();
            artifactDTO.setType(artifact.getType());
            artifactDTO.setArtifact(artifact.getValue());
            artifactDTOs[i] = artifactDTO;
            i++;
        }
        return artifactDTOs;
    }

    private static ScenarioInfoDTO[] mapScenarios(List<Scenario> scenarios) {
        ScenarioInfoDTO[] scenarioInfoDTOs = new ScenarioInfoDTO[scenarios.size()];
        int i = 0;
        for (Scenario scenario : scenarios) {
            ScenarioInfoDTO scenarioInfoDTO = new ScenarioInfoDTO();
            scenarioInfoDTO.setName(scenario.getName());
            scenarioInfoDTO.setDescription(scenario.getDescription());
            scenarioInfoDTO.setParameterDTOs(mapParameterListToDTOs(scenario.getParameters().getParameter()));
            scenarioInfoDTOs[i] = scenarioInfoDTO;
            i++;
        }
        return scenarioInfoDTOs;
    }

    private static ParameterDTO[] mapParameterListToDTOs(List<Parameter> parameters) {
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

    private static StreamMappingDTO[] mapStreamMappingListToDTOs(List<StreamMapping> streamMappings) {
        StreamMappingDTO[] streamMappingDTOs = new StreamMappingDTO[streamMappings.size()];
        int i = 0;
        for (StreamMapping streamMapping: streamMappings) {
            StreamMappingDTO streamMappingDTO = new StreamMappingDTO();
            streamMappingDTO.setToStream(streamMapping.getTo());
            streamMappingDTOs[i] = streamMappingDTO;
            i++;
        }
        return streamMappingDTOs;
    }

    private static TemplateDTO[] mapTemplateListToDTOs(List<Template> templates) {
        TemplateDTO[] templateDTOs = new TemplateDTO[templates.size()];
        int i = 0;
        for (Template template: templates) {
            TemplateDTO templateDTO = new TemplateDTO();
            templateDTO.setType(template.getType());
            templateDTO.setArtifact(template.getValue());
            templateDTOs[i] = templateDTO;
            i++;
        }
        return templateDTOs;
    }
//
//    /**
//     * Maps given array of Template objects to array of TemplateDTO objects
//     *
//     * @param templates Template objects array needs to mapped
//     * @return Mapped array of TemplateDTO objects
//     */
//    private static TemplateDTO[] mapTemplates(Template[] templates) {
//        TemplateDTO[] templateDTOs = null;
//
//        if (templates != null) {
//            templateDTOs = new TemplateDTO[templates.length];
//            for (int i = 0; i < templateDTOs.length; i++) {
//                templateDTOs[i] = mapTemplate(templates[i]);
//            }
//        }
//        return templateDTOs;
//    }
//
//    /**
//     * Maps given Template object to TemplateDTO object
//     *
//     * @param template Template object needs to be mapped
//     * @return Mapped TemplateDTO object
//     */
//    private static TemplateDTO mapTemplate(Template template) {
//        TemplateDTO templateDTO = null;
//
//        if (template != null) {
//            templateDTO = new TemplateDTO();
//            templateDTO.setName(template.getType());
//        }
//        return templateDTO;
//    }

    /**
     * Maps given array of Parameter objects to array of ParameterDTO objects
     *
     * @param parameters Parameter objects array needs to be mapped
     * @return Mapped array of ParameterDTO objects
     */
    private static ParameterDTO[] mapParameters(Parameter[] parameters) {
        ParameterDTO[] parameterDTOs = null;

        if (parameters != null) {
            parameterDTOs = new ParameterDTO[parameters.length];
            for (int i = 0; i < parameterDTOs.length; i++) {
                parameterDTOs[i] = mapParameter(parameters[i]);
            }
        }
        return parameterDTOs;
    }

    /**
     * Maps given Parameter object to ParameterDTO object
     *
     * @param parameter Parameter object needs to be mapped
     * @return Mapped ParameterDTO object
     */
    private static ParameterDTO mapParameter(Parameter parameter) {
        ParameterDTO parameterDTO = null;

        if (parameter != null) {
            parameterDTO = new ParameterDTO();
            parameterDTO.setName(parameter.getName());
            parameterDTO.setDescription(parameter.getDescription());
            parameterDTO.setDefaultValue(parameter.getDefaultValue());
            parameterDTO.setDisplayName(parameter.getDisplayName());
            parameterDTO.setType(parameter.getType());
            parameterDTO.setOptions(parameter.getOptions());
        }
        return parameterDTO;
    }

}
