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

import org.wso2.carbon.event.execution.manager.admin.dto.domain.ParameterDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.TemplateDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.TemplateDomainDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.TemplateDomainInfoDTO;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Parameter;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Template;
import org.wso2.carbon.event.execution.manager.core.structure.domain.TemplateDomain;

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
     * Maps given list of TemplateDomain objects to array of TemplateDomainInfoDTO objects
     *
     * @param templateDomains List of TemplateDomain objects needs to be mapped
     * @return Mapped array of TemplateDomainInfoDTO objects
     */
    public static TemplateDomainInfoDTO[] mapDomainsInfo(List<TemplateDomain> templateDomains) {
        TemplateDomainInfoDTO[] templateDomainInfoDTO = null;

        if (templateDomains != null) {
            templateDomainInfoDTO = new TemplateDomainInfoDTO[templateDomains.size()];

            for (int i = 0; i < templateDomainInfoDTO.length; i++) {
                templateDomainInfoDTO[i] = mapDomainInfo(templateDomains.get(i));
            }
        }
        return templateDomainInfoDTO;
    }

    /**
     * Maps given TemplateDomain object to TemplateDomainInfoDTO object
     *
     * @param templateDomain TemplateDomain object needs to be mapped
     * @return Mapped TemplateDomainInfoDTO object
     */
    public static TemplateDomainInfoDTO mapDomainInfo(TemplateDomain templateDomain) {
        TemplateDomainInfoDTO templateDomainInfoDTO = null;

        if (templateDomain != null) {
            templateDomainInfoDTO = new TemplateDomainInfoDTO();
            templateDomainInfoDTO.setName(templateDomain.getName());
            templateDomainInfoDTO.setDescription(templateDomain.getDescription());
        }

        return templateDomainInfoDTO;
    }

    /**
     * Maps given list of TemplateDomain objects to array of TemplateDomainDTO objects
     *
     * @param templateDomains List of TemplateDomain objects needs to be mapped
     * @return Mapped array of TemplateDomainDTO objects
     */
    public static TemplateDomainDTO[] mapDomains(List<TemplateDomain> templateDomains) {
        TemplateDomainDTO[] templateDomainDTOs = null;

        if (templateDomains != null) {
            templateDomainDTOs = new TemplateDomainDTO[templateDomains.size()];

            for (int i = 0; i < templateDomainDTOs.length; i++) {
                templateDomainDTOs[i] = mapDomain(templateDomains.get(i));
            }
        }
        return templateDomainDTOs;
    }

    /**
     * Maps given TemplateDomain object to TemplateDomainDTO object
     *
     * @param templateDomain TemplateDomain object needs to be mapped
     * @return Mapped TemplateDomainDTO object
     */
    public static TemplateDomainDTO mapDomain(TemplateDomain templateDomain) {
        TemplateDomainDTO templateDomainDTO = null;

        if (templateDomain != null) {
            templateDomainDTO = new TemplateDomainDTO();
            templateDomainDTO.setName(templateDomain.getName());
            templateDomainDTO.setDescription(templateDomain.getName());
            templateDomainDTO.setStreams(templateDomain.getStreams());
            templateDomainDTO.setTemplateDTOs(mapTemplates(templateDomain.getTemplates()));
        }

        return templateDomainDTO;
    }

    /**
     * Maps given array of Template objects to array of TemplateDTO objects
     *
     * @param templates Template objects array needs to mapped
     * @return Mapped array of TemplateDTO objects
     */
    private static TemplateDTO[] mapTemplates(Template[] templates) {
        TemplateDTO[] templateDTOs = null;

        if (templates != null) {
            templateDTOs = new TemplateDTO[templates.length];
            for (int i = 0; i < templateDTOs.length; i++) {
                templateDTOs[i] = mapTemplate(templates[i]);
            }
        }
        return templateDTOs;
    }

    /**
     * Maps given Template object to TemplateDTO object
     *
     * @param template Template object needs to be mapped
     * @return Mapped TemplateDTO object
     */
    private static TemplateDTO mapTemplate(Template template) {
        TemplateDTO templateDTO = null;

        if (template != null) {
            templateDTO = new TemplateDTO();
            templateDTO.setName(template.getName());
            templateDTO.setDescription(template.getDescription());
            templateDTO.setExecutionScript(template.getScript());
            templateDTO.setParameterDTOs(mapParameters(template.getParameters()));
            templateDTO.setExecutionType(template.getExecutionType());
        }
        return templateDTO;
    }

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
