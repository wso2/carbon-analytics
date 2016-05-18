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
import org.wso2.carbon.event.execution.manager.core.structure.configuration.AttributeMapping;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.AttributeMappings;
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
            templateConfigurationInfoDTO.setScenario(templateConfig.getScenario());
            templateConfigurationInfoDTO.setDescription(templateConfig.getDescription());
            templateConfigurationInfoDTO.setDomain(templateConfig.getDomain());
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
            templateConfigurationDTO.setScenario(templateConfig.getScenario());
            templateConfigurationDTO.setDescription(templateConfig.getDescription());
            templateConfigurationDTO.setDomain(templateConfig.getDomain());
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
            templateConfig.setScenario(configDTO.getScenario());
            templateConfig.setDescription(configDTO.getDescription());
            templateConfig.setDomain(configDTO.getDomain());
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


    public static String[] mapStreamIds(List<String> streamIdList) {
        return (String[]) streamIdList.toArray();
    }

    public static StreamMapping mapStreamMapping(StreamMappingDTO streamMappingDTO) {
        StreamMapping streamMapping = new StreamMapping();
        streamMapping.setFrom(streamMappingDTO.getFromStream());
        streamMapping.setTo(streamMappingDTO.getToStream());
        List<AttributeMapping> attributeMappingList = new ArrayList<>();
        for (int i = 0; i < streamMappingDTO.getAttributePairs().length; i++) {
            AttributeMapping attributeMapping = new AttributeMapping();
            attributeMapping.setFrom(streamMappingDTO.getAttributePairs()[i][0]);
            attributeMapping.setTo(streamMappingDTO.getAttributePairs()[i][1]);
            attributeMappingList.add(attributeMapping);
        }
        AttributeMappings attributeMappings = new AttributeMappings();
        attributeMappings.setAttributeMapping(attributeMappingList);
        streamMapping.setAttributeMappings(attributeMappings);
        return streamMapping;
    }
}
