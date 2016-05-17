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

import org.wso2.carbon.event.execution.manager.admin.dto.configuration.ParameterDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.StreamMappingDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.TemplateConfigurationDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.TemplateConfigurationInfoDTO;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.Parameter;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.StreamMapping;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.TemplateConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consist of the necessary mapping methods of Configurations
 */
public class ConfigurationMapper {

    /**
     * To avoid instantiating
     */
    private ConfigurationMapper() {
    }


    /**
     * Maps given list of TemplateConfiguration objects to array of TemplateConfigurationInfoDTO objects
     *
     * @param templateConfigurations List of TemplateConfiguration objects which needs to be mapped
     * @return Mapped array of TemplateConfigurationInfoDTO
     */
    public static TemplateConfigurationInfoDTO[] mapConfigurationsInfo(
            List<TemplateConfiguration> templateConfigurations) {
        TemplateConfigurationInfoDTO[] templateConfigurationInfoDTO = null;

        if (templateConfigurations != null) {
            templateConfigurationInfoDTO = new TemplateConfigurationInfoDTO[templateConfigurations.size()];

            for (int i = 0; i < templateConfigurationInfoDTO.length; i++) {
                templateConfigurationInfoDTO[i] = mapConfigurationInfo(templateConfigurations.get(i));
            }
        }
        return templateConfigurationInfoDTO;
    }

    /**
     * Maps given TemplateConfiguration object to TemplateConfigurationInfoDTO object
     *
     * @param templateConfig TemplateConfiguration object needs to be mapped
     * @return Mapped TemplateConfigurationInfoDTO object
     */
    public static TemplateConfigurationInfoDTO mapConfigurationInfo(TemplateConfiguration templateConfig) {
        TemplateConfigurationInfoDTO templateConfigurationInfoDTO = null;

        if (templateConfig != null) {
            templateConfigurationInfoDTO = new TemplateConfigurationInfoDTO();
            templateConfigurationInfoDTO.setName(templateConfig.getName());
            templateConfigurationInfoDTO.setType(templateConfig.getType());
            templateConfigurationInfoDTO.setDescription(templateConfig.getDescription());
            templateConfigurationInfoDTO.setFrom(templateConfig.getFrom());
        }
        return templateConfigurationInfoDTO;
    }

    /**
     * Maps given list of TemplateConfiguration objects to array of TemplateConfigurationDTO objects
     *
     * @param templateConfigurations List of TemplateConfiguration objects which needs to be mapped
     * @return Mapped TemplateConfigurationDTO object
     */
    public static TemplateConfigurationDTO[] mapConfigurations(
            List<TemplateConfiguration> templateConfigurations) {
        TemplateConfigurationDTO[] templateConfigurationDTOs = null;

        if (templateConfigurations != null) {
            templateConfigurationDTOs = new TemplateConfigurationDTO[templateConfigurations.size()];

            for (int i = 0; i < templateConfigurationDTOs.length; i++) {
                templateConfigurationDTOs[i] = mapConfiguration(templateConfigurations.get(i));
            }
        }
        return templateConfigurationDTOs;
    }

    /**
     * Maps given TemplateConfiguration object to TemplateConfigurationDTO object
     *
     * @param templateConfig TemplateConfiguration object needs to be mapped
     * @return Mapped TemplateConfigurationDTO object
     */
    public static TemplateConfigurationDTO mapConfiguration(TemplateConfiguration templateConfig) {
        TemplateConfigurationDTO templateConfigurationDTO = null;

        if (templateConfig != null) {
            templateConfigurationDTO = new TemplateConfigurationDTO();
            templateConfigurationDTO.setName(templateConfig.getName());
            templateConfigurationDTO.setType(templateConfig.getType());
            templateConfigurationDTO.setDescription(templateConfig.getDescription());
            templateConfigurationDTO.setFrom(templateConfig.getFrom());
            templateConfigurationDTO.setParameterDTOs(mapParameters(templateConfig.getParameterMap()));
        }
        return templateConfigurationDTO;
    }

    /**
     * Maps given TemplateConfigurationDTO object to TemplateConfiguration object
     *
     * @param configDTO TemplateConfigurationDTO object which needs to be mapped
     * @return Mapped TemplateConfiguration object
     */
    public static TemplateConfiguration mapConfiguration(TemplateConfigurationDTO configDTO) {
        TemplateConfiguration templateConfig = null;

        if (configDTO != null) {
            templateConfig = new TemplateConfiguration();
            templateConfig.setName(configDTO.getName());
            templateConfig.setType(configDTO.getType());
            templateConfig.setDescription(configDTO.getDescription());
            templateConfig.setFrom(configDTO.getFrom());
            templateConfig.setParameterMap(mapParameters(configDTO.getParameterDTOs()));
        }
        return templateConfig;
    }

    /**
     * Maps given List of ParameterDTO objects to a Map in which parameter name is mapped to value.
     *
     * @param parameterDTOs ParameterDTO object which needs to mapped
     * @return Parameter map
     */
    private static Map<String,String> mapParameters(ParameterDTO[] parameterDTOs) {
        Map<String,String> parameterMap = new HashMap<>();
        if (parameterDTOs != null) {
            for (int i = 0; i < parameterDTOs.length; i++) {
                parameterMap.put(parameterDTOs[i].getName(),parameterDTOs[i].getValue());
            }
        }
        return parameterMap;
    }

    /**
     * Maps given List of Parameter objects to array of ParameterDTO
     *
     * @param parameterMap Map, containing the parameter names-value pairs, which needs to be mapped
     * @return Mapped array of ParameterDTO objects
     */
    private static ParameterDTO[] mapParameters(Map<String,String> parameterMap) {
        ParameterDTO[] parameterDTOs = null;

        if (parameterMap != null) {
            parameterDTOs = new ParameterDTO[parameterMap.size()];
            int i=0;
            for (Map.Entry<String,String> entry : parameterMap.entrySet()) {
                ParameterDTO dto = new ParameterDTO();
                dto.setName(entry.getKey());
                dto.setValue(entry.getValue());
                parameterDTOs[i] = dto;
            }
            i++;
        }
        return parameterDTOs;
    }

    /**
     * Maps given ParameterDTO object to ParameterDTO object
     *
     * @param parameterDTO ParameterDTO needs to be mapped
     * @return Converyed ParameterDTO object
     */
    private static Parameter mapParameter(ParameterDTO parameterDTO) {       //todo: remove if no use
        Parameter parameter = null;

        if (parameterDTO != null) {
            parameter = new Parameter();
            parameter.setName(parameterDTO.getName());
            parameter.setValue(parameterDTO.getValue());
        }
        return parameter;
    }


    /**
     * Maps given Parameter object to ParameterDTO object
     *
     * @param parameter Parameter needs to be mapped
     * @return Mapped ParameterDTO object
     */
    private static ParameterDTO mapParameter(Parameter parameter) {         //todo: remove if no use
        ParameterDTO parameterDTO = null;
        if (parameter != null) {
            parameterDTO = new ParameterDTO();
            parameterDTO.setName(parameter.getName());
            parameterDTO.setValue(parameter.getValue());
        }
        return parameterDTO;
    }

    public static String[] mapStreamIds(List<String> streamIdList) {
        return (String[]) streamIdList.toArray();
    }

    public static StreamMapping mapStreamMapping(StreamMappingDTO streamMappingDTO) {
        StreamMapping streamMapping = new StreamMapping();
        streamMapping.setFromStream(streamMappingDTO.getFromStream());
        streamMapping.setToStream(streamMappingDTO.getToStream());
        List<String[]> attributePairs = new ArrayList<>();
        for (int i = 0; i < streamMappingDTO.getAttributePairs().length; i++) {
            attributePairs.add(new String[]{streamMappingDTO.getAttributePairs()[i][0]
                    , streamMappingDTO.getAttributePairs()[i][1]});
        }
        streamMapping.setAttributePairs(attributePairs);
        return streamMapping;
    }
}
