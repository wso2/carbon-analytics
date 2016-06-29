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
package org.wso2.carbon.event.template.manager.admin;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.event.template.manager.admin.dto.configuration.ScenarioConfigurationDTO;
import org.wso2.carbon.event.template.manager.admin.dto.configuration.ScenarioConfigurationInfoDTO;
import org.wso2.carbon.event.template.manager.admin.dto.domain.DomainInfoDTO;
import org.wso2.carbon.event.template.manager.admin.internal.ds.TemplateManagerAdminServiceValueHolder;
import org.wso2.carbon.event.template.manager.admin.internal.util.ConfigurationMapper;
import org.wso2.carbon.event.template.manager.admin.internal.util.DomainMapper;
import org.wso2.carbon.event.template.manager.core.exception.TemplateManagerException;
import org.wso2.carbon.event.template.manager.core.structure.domain.Domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Consist of the methods exposed by TemplateManagerAdminService
 */
public class TemplateManagerAdminService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(TemplateManagerAdminService.class);

    /**
     * Default Constructor
     */
    public TemplateManagerAdminService() {

    }


    /**
     * Return "limited information" with regards to a Domain Template, given  its name.
     * "limited information" means, only the information which are required by the UI will be returned.
     *
     * @param domainName name of the Domain.
     * @return DomainInfoDTO object
     * @throws AxisFault
     */
    public DomainInfoDTO getDomainInfo(String domainName) throws AxisFault {
        try {
            return DomainMapper.mapTemplateManagerTemplate(TemplateManagerAdminServiceValueHolder.getCarbonTemplateManagerService()
                                                                    .getDomain(domainName));
        } catch (Throwable e) {
            log.error("Error occurred when getting domain " + domainName, e);
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * Return "limited information" with regards to all available Domain Templates.
     * "limited information" means, only the information which are required by the UI will be returned.
     *
     * @return Domain information for all of the available Domain Templates
     * @throws org.apache.axis2.AxisFault
     */
    public DomainInfoDTO[] getAllDomainInfos() throws AxisFault {
        try {
            return DomainMapper.mapTemplateManagerTemplates(new ArrayList
                    <Domain>(TemplateManagerAdminServiceValueHolder.getCarbonTemplateManagerService().getAllDomains()));
        } catch (Throwable e) {
            log.error("Error occurred when getting all domains ", e);
            throw new AxisFault(e.getMessage(), e);
        }
    }


    /**
     * return details for a given template configuration name
     *
     * @param domainName domain name of the Domain, corresponding to the configuration
     * @param configName scenario configuration name
     * @return scenario configuration details
     * @throws AxisFault
     */
    public ScenarioConfigurationDTO getConfiguration(String domainName, String configName) throws AxisFault {
        try {
            return ConfigurationMapper.mapConfiguration(TemplateManagerAdminServiceValueHolder
                    .getCarbonTemplateManagerService().getConfiguration(domainName, configName));
        } catch (TemplateManagerException e) {
            log.error("Error occurred when getting template configuration " + configName, e);
            throw new AxisFault(e.getMessage());
        }
    }


    /**
     * return all of the available Scenario Configuration Info's for a given Domain domain.
     * This method does not return all the fields in a Scenario Configurations, rather returns only the fields which are required for the UI.
     *
     * @param domainName domain name of the Domain
     * @return scenario configuration information.
     * @throws AxisFault
     */
    public ScenarioConfigurationInfoDTO[] getConfigurationInfos(String domainName) throws AxisFault {
        try {
            return ConfigurationMapper.mapConfigurationsInfo(new ArrayList<>(
                    TemplateManagerAdminServiceValueHolder.getCarbonTemplateManagerService()
                            .getConfigurations(domainName)));
        } catch (TemplateManagerException e) {
            log.error("Error occurred when getting configurations for domain " + domainName, e);
            throw new AxisFault(e.getMessage());
        }
    }


    /**
     * Delete specified scenario configuration
     *
     * @param domainName domain name of the Domain
     * @param configName name of the scenario configuration which needs to be deleted
     */
    public boolean deleteConfiguration(String domainName, String configName) throws AxisFault {
        try {
            TemplateManagerAdminServiceValueHolder.getCarbonTemplateManagerService()
                    .deleteConfiguration(domainName, configName);
            return true;
        } catch (TemplateManagerException e) {
            log.error("Error occurred when deleting configuration " + configName, e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    /**
     * Create or update specified scenario configuration.
     *
     * @param configuration scenario configuration data transfer object which needs to be saved.
     * @return  Stream ID array. In case there are StreamMappings in the Domain (under this particular scenario),
     * then the "toStream" IDs will be returned. If no StreamMappings present, null will be returned.
     * @throws AxisFault
     */
    public String[] saveConfiguration(ScenarioConfigurationDTO configuration) throws AxisFault {
        try {
            List<String> streamIdList = TemplateManagerAdminServiceValueHolder.getCarbonTemplateManagerService()
                    .saveConfiguration(ConfigurationMapper.mapConfiguration(configuration));
            if (streamIdList != null) {
               return streamIdList.toArray(new String[0]);
            } else {
                return null;
            }
        } catch (TemplateManagerException e) {
            log.error("Error occurred when saving configuration " + configuration.getName(), e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    /**
     * When the template refers to a stream (say StreamX) which needs to be populated by a user-defined stream (say StreamY),
     * specify how attributes in StreamY needs to be mapped to StreamX, using the streamMappingDTO,
     * and invoke this service.
     * This service will create a Siddhi execution plan, which will select required attributes (as specified in the mapping)
     * from StreamY and insert into StreamX.
     *
     * @param streamMappingDTOs  Each StreamMappingDTO maps a user-defined stream to a stream defined in the template.
     * @param configName ScenarioConfiguration name
     * @param domainName domain name of the Domain.
     * @return true on successful operation completion.
     * @throws AxisFault
     */
    public boolean saveStreamMapping(
            org.wso2.carbon.event.template.manager.admin.dto.configuration.StreamMappingDTO[]
                    streamMappingDTOs, String configName, String domainName) throws AxisFault {
        try {
            TemplateManagerAdminServiceValueHolder.getCarbonTemplateManagerService()
                    .saveStreamMapping(ConfigurationMapper.mapStreamMapping(streamMappingDTOs)
                            , configName, domainName);
            return true;
        } catch (TemplateManagerException e) {
            log.error("Error occurred when saving configuration " + configName + " in domain " + domainName + " with stream mappings", e);
            throw new AxisFault(e.getMessage(), e);
        }
    }
}
