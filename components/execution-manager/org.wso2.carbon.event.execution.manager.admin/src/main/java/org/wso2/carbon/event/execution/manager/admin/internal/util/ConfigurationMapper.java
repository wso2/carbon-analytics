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

import org.wso2.carbon.event.execution.manager.admin.dto.configuration.AttributeMappingDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.ParameterDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.ScenarioConfigurationDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.StreamMappingDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.ScenarioConfigurationInfoDTO;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.AttributeMapping;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.AttributeMappings;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.ScenarioConfiguration;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.StreamMapping;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.StreamMappings;

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
     * Maps given list of ScenarioConfiguration objects to array of ScenarioConfigurationInfoDTO objects
     *
     * @param scenarioConfigurations List of ScenarioConfiguration objects which needs to be mapped
     * @return Mapped array of ScenarioConfigurationInfoDTO
     */
    public static ScenarioConfigurationInfoDTO[] mapConfigurationsInfo(
            List<ScenarioConfiguration> scenarioConfigurations) {
        ScenarioConfigurationInfoDTO[] scenarioConfigurationInfoDTO = null;

        if (scenarioConfigurations != null) {
            scenarioConfigurationInfoDTO = new ScenarioConfigurationInfoDTO[scenarioConfigurations.size()];

            for (int i = 0; i < scenarioConfigurationInfoDTO.length; i++) {
                scenarioConfigurationInfoDTO[i] = mapConfigurationInfo(scenarioConfigurations.get(i));
            }
        }
        return scenarioConfigurationInfoDTO;
    }

    /**
     * Maps given ScenarioConfiguration object to ScenarioConfigurationInfoDTO object
     *
     * @param scenarioConfig ScenarioConfiguration object needs to be mapped
     * @return Mapped ScenarioConfigurationInfoDTO object
     */
    public static ScenarioConfigurationInfoDTO mapConfigurationInfo(ScenarioConfiguration scenarioConfig) {
        ScenarioConfigurationInfoDTO scenarioConfigurationInfoDTO = null;

        if (scenarioConfig != null) {
            scenarioConfigurationInfoDTO = new ScenarioConfigurationInfoDTO();
            scenarioConfigurationInfoDTO.setName(scenarioConfig.getName());
            scenarioConfigurationInfoDTO.setType(scenarioConfig.getScenario());
            scenarioConfigurationInfoDTO.setDescription(scenarioConfig.getDescription());
            scenarioConfigurationInfoDTO.setDomain(scenarioConfig.getDomain());
        }
        return scenarioConfigurationInfoDTO;
    }

    /**
     * Maps given list of ScenarioConfiguration objects to array of ScenarioConfigurationDTO objects
     *
     * @param scenarioConfigurations List of ScenarioConfiguration objects which needs to be mapped
     * @return Mapped ScenarioConfigurationDTO object
     */
    public static ScenarioConfigurationDTO[] mapConfigurations(
            List<ScenarioConfiguration> scenarioConfigurations) {
        ScenarioConfigurationDTO[] scenarioConfigurationDTOs = null;

        if (scenarioConfigurations != null) {
            scenarioConfigurationDTOs = new ScenarioConfigurationDTO[scenarioConfigurations.size()];

            for (int i = 0; i < scenarioConfigurationDTOs.length; i++) {
                scenarioConfigurationDTOs[i] = mapConfiguration(scenarioConfigurations.get(i));
            }
        }
        return scenarioConfigurationDTOs;
    }

    /**
     * Maps given ScenarioConfiguration object to ScenarioConfigurationDTO object
     *
     * @param scenarioConfig ScenarioConfiguration object needs to be mapped
     * @return Mapped ScenarioConfigurationDTO object
     */
    public static ScenarioConfigurationDTO mapConfiguration(ScenarioConfiguration scenarioConfig) {
        ScenarioConfigurationDTO scenarioConfigurationDTO = null;

        if (scenarioConfig != null) {
            scenarioConfigurationDTO = new ScenarioConfigurationDTO();
            scenarioConfigurationDTO.setName(scenarioConfig.getName());
            scenarioConfigurationDTO.setType(scenarioConfig.getScenario());
            scenarioConfigurationDTO.setDescription(scenarioConfig.getDescription());
            scenarioConfigurationDTO.setDomain(scenarioConfig.getDomain());
            scenarioConfigurationDTO.setParameterDTOs(mapParameters(scenarioConfig.getParameterMap()));
            scenarioConfigurationDTO.setStreamMappingDTOs(mapStreamMappings(scenarioConfig.getStreamMappings()));
        }
        return scenarioConfigurationDTO;
    }

    private static StreamMappingDTO[] mapStreamMappings(StreamMappings streamMappings) {
        if (streamMappings == null) {
            return null;
        }
        List<StreamMapping> streamMappingList = streamMappings.getStreamMapping();
        StreamMappingDTO[] mappingDTOArray = new StreamMappingDTO[streamMappingList.size()];
        int i = 0;
        for (StreamMapping streamMapping: streamMappingList) {
            StreamMappingDTO mappingDTO = new StreamMappingDTO();
            mappingDTO.setFromStream(streamMapping.getFrom());
            mappingDTO.setToStream(streamMapping.getTo());
            AttributeMappingDTO[] attributeDTOArray = new AttributeMappingDTO[streamMapping.getAttributeMappings().getAttributeMapping().size()];
            int j = 0;
            for (AttributeMapping attributeMapping: streamMapping.getAttributeMappings().getAttributeMapping()) {
                AttributeMappingDTO attributeDTO = new AttributeMappingDTO();
                attributeDTO.setFromAttribute(attributeMapping.getFrom());
                attributeDTO.setToAttribute(attributeMapping.getTo());
                attributeDTO.setAttributeType(attributeMapping.getType());
                attributeDTOArray[j] = attributeDTO;
                j++;
            }
            mappingDTO.setAttributeMappingDTOs(attributeDTOArray);
            mappingDTOArray[i] = mappingDTO;
            i++;
        }
        return mappingDTOArray;
    }

    /**
     * Maps given ScenarioConfigurationDTO object to ScenarioConfiguration object
     *
     * @param configDTO ScenarioConfigurationDTO object which needs to be mapped
     * @return Mapped ScenarioConfiguration object
     */
    public static ScenarioConfiguration mapConfiguration(ScenarioConfigurationDTO configDTO) {
        ScenarioConfiguration scenarioConfig = null;

        if (configDTO != null) {
            scenarioConfig = new ScenarioConfiguration();
            scenarioConfig.setName(configDTO.getName());
            scenarioConfig.setScenario(configDTO.getType());
            scenarioConfig.setDescription(configDTO.getDescription());
            scenarioConfig.setDomain(configDTO.getDomain());
            scenarioConfig.setParameterMap(mapParameters(configDTO.getParameterDTOs()));
        }
        return scenarioConfig;
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
                i++;
            }
        }
        return parameterDTOs;
    }

    public static List<StreamMapping> mapStreamMapping(StreamMappingDTO[] streamMappingDTO) {
        List<StreamMapping> streamMappings = new ArrayList<>();
        for (int i = 0; i < streamMappingDTO.length; i++) {
            StreamMapping streamMapping = new StreamMapping();
            streamMapping.setFrom(streamMappingDTO[i].getFromStream());
            streamMapping.setTo(streamMappingDTO[i].getToStream());
            List<AttributeMapping> attributeMappingList = new ArrayList<>();
            for (int j = 0; j < streamMappingDTO[i].getAttributeMappingDTOs().length; j++) {
                AttributeMapping attributeMapping = new AttributeMapping();
                attributeMapping.setFrom(streamMappingDTO[i].getAttributeMappingDTOs()[j].getFromAttribute());
                attributeMapping.setTo(streamMappingDTO[i].getAttributeMappingDTOs()[j].getToAttribute());
                attributeMapping.setType(streamMappingDTO[i].getAttributeMappingDTOs()[j].getAttributeType());
                attributeMappingList.add(attributeMapping);
            }
            AttributeMappings attributeMappings = new AttributeMappings();
            attributeMappings.setAttributeMapping(attributeMappingList);
            streamMapping.setAttributeMappings(attributeMappings);
            streamMappings.add(streamMapping);
        }
        return streamMappings;
    }
}
