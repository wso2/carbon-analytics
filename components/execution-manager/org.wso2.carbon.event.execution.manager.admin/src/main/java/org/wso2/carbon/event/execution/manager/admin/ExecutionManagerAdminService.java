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
package org.wso2.carbon.event.execution.manager.admin;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.TemplateConfigurationDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.TemplateConfigurationInfoDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.TemplateDomainDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.TemplateDomainInfoDTO;
import org.wso2.carbon.event.execution.manager.admin.internal.ds.ExecutionManagerAdminServiceValueHolder;
import org.wso2.carbon.event.execution.manager.admin.internal.util.ConfigurationMapper;
import org.wso2.carbon.event.execution.manager.admin.internal.util.DomainMapper;
import org.wso2.carbon.event.execution.manager.core.exception.ExecutionManagerException;
import org.wso2.carbon.event.execution.manager.core.structure.domain.TemplateDomain;

import java.util.ArrayList;

/**
 * Consist of the methods exposed by ExecutionManagerAdminService
 */
public class ExecutionManagerAdminService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(ExecutionManagerAdminService.class);

    /**
     * Default Constructor
     */
    public ExecutionManagerAdminService() {

    }

    //TODO: DONE FROM ADMIN SERVICE SIDE, CHECK UI USAGE.
    /**
     * return domain for a given template domain name
     *
     * @param domainName template domain name
     * @return template domain full details
     * @throws AxisFault
     */
    public TemplateDomainDTO getDomain(String domainName) throws AxisFault {
        try {
            return DomainMapper.mapDomain(ExecutionManagerAdminServiceValueHolder.getCarbonExecutionManagerService()
                    .getDomain(domainName));
        } catch (Exception e) {
            log.error("Error occurred when getting domain " + domainName, e);
            throw new AxisFault(e.getMessage());
        }
    }

    //TODO: NO CHANGES NEEDED
    /**
     * return domain for a given template domain name
     *
     * @param domainName template domain name
     * @return template domain full details
     * @throws AxisFault
     */
    public TemplateDomainInfoDTO getDomainInfo(String domainName) throws AxisFault {
        try {
            return DomainMapper.mapDomainInfo(ExecutionManagerAdminServiceValueHolder.getCarbonExecutionManagerService()
                    .getDomain(domainName));
        } catch (Exception e) {
            log.error("Error occurred when getting domain " + domainName, e);
            throw new AxisFault(e.getMessage());
        }
    }

    //TODO: NO CHANGES NEEDED
    /**
     * return all available template domain information
     *
     * @return all template domain information
     * @throws org.apache.axis2.AxisFault
     */
    public TemplateDomainInfoDTO[] getAllDomainsInfo() throws AxisFault {
        try {
            return DomainMapper.mapDomainsInfo(new ArrayList<TemplateDomain>(ExecutionManagerAdminServiceValueHolder
                    .getCarbonExecutionManagerService().getAllDomains()));
        } catch (Exception e) {
            log.error("Error occurred when getting all domains ", e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    //TODO: CHECK UI USAGE
    /**
     * return all available template domains
     *
     * @return all template domain information
     * @throws org.apache.axis2.AxisFault
     */
    public TemplateDomainDTO[] getAllDomains() throws AxisFault {
        try {
            return DomainMapper.mapDomains(new ArrayList<TemplateDomain>(ExecutionManagerAdminServiceValueHolder
                    .getCarbonExecutionManagerService().getAllDomains()));
        } catch (Exception e) {
            log.error("Error occurred when getting all domains ", e);
            throw new AxisFault(e.getMessage(), e);
        }
    }


    //TODO: CHECK UI USAGE.
    //Only change done in mapping is removing executionParameters.
    /**
     * return details for a given template domain name
     *
     * @param domainName template domain name
     * @return template domain configuration details
     * @throws AxisFault
     */
    public TemplateConfigurationDTO[] getConfigurations(String domainName) throws AxisFault {
        try {
            return ConfigurationMapper.mapConfigurations(new ArrayList<>(
                    ExecutionManagerAdminServiceValueHolder.getCarbonExecutionManagerService()
                            .getConfigurations(domainName)));
        } catch (Exception e) {
            log.error("Error occurred when getting configurations for domain " + domainName, e);
            throw new AxisFault(e.getMessage());
        }
    }

    //TODO: CHECK UI USAGE
    /**
     * return details for a given template configuration name
     *
     * @param domainName template domain name
     * @param configName template configuration name
     * @return template domain configuration details
     * @throws AxisFault
     */
    public TemplateConfigurationDTO getConfiguration(String domainName, String configName) throws AxisFault {
        try {
            return ConfigurationMapper.mapConfiguration(ExecutionManagerAdminServiceValueHolder
                    .getCarbonExecutionManagerService().getConfiguration(domainName, configName));
        } catch (Exception e) {
            log.error("Error occurred when getting template configuration " + configName, e);
            throw new AxisFault(e.getMessage());
        }
    }

    //TODO: SEEMS LIKE NOTHING TO BE CHANGED
    /**
     * return details for a given template domain name
     *
     * @param domainName template domain name
     * @return template domain configuration details
     * @throws AxisFault
     */
    public TemplateConfigurationInfoDTO[] getConfigurationsInfo(String domainName) throws AxisFault {
        try {
            return ConfigurationMapper.mapConfigurationsInfo(new ArrayList<>(
                    ExecutionManagerAdminServiceValueHolder.getCarbonExecutionManagerService()
                            .getConfigurations(domainName)));
        } catch (Exception e) {
            log.error("Error occurred when getting configurations for domain " + domainName, e);
            throw new AxisFault(e.getMessage());
        }
    }


    //TODO: SEEMS LIKE NOTHING TO BE CHANGED
    /**
     * return details for a given template configuration name
     *
     * @param domainName template domain name
     * @param configName template configuration name
     * @return template domain configuration details
     * @throws AxisFault
     */
    public TemplateConfigurationInfoDTO getConfigurationInfo(String domainName, String configName) throws AxisFault {
        try {
            return ConfigurationMapper.mapConfigurationInfo(ExecutionManagerAdminServiceValueHolder
                    .getCarbonExecutionManagerService().getConfiguration(domainName, configName));
        } catch (Exception e) {
            log.error("Error occurred when getting template configuration " + configName, e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    /**
     * Delete specified configuration
     *
     * @param domainName template domain name
     * @param configName configuration name which needs to be deleted
     */
    public boolean deleteConfiguration(String domainName, String configName) throws AxisFault {
        try {
            ExecutionManagerAdminServiceValueHolder.getCarbonExecutionManagerService()
                    .deleteConfiguration(domainName, configName);
            return true;
        } catch (ExecutionManagerException e) {
            log.error("Error occurred when deleting configuration " + configName, e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    /**
     * Create or update specified configuration.
     *
     * @param configuration configuration data transfer object
     * @return  Stream ID array
     * @throws AxisFault
     */
    public String[] saveConfiguration(TemplateConfigurationDTO configuration) throws AxisFault {
        try {
            return ConfigurationMapper.mapStreamIds(ExecutionManagerAdminServiceValueHolder.getCarbonExecutionManagerService()
                    .saveConfiguration(ConfigurationMapper.mapConfiguration(configuration)));
        } catch (ExecutionManagerException e) {
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
     * @param streamMappingDTO  Maps a user-defined stream to a stream defined in the template.
     * @return true on successful operation completion.
     * @throws AxisFault
     */
    public boolean saveStreamMapping(
            org.wso2.carbon.event.execution.manager.admin.dto.configuration.StreamMappingDTO streamMappingDTO) throws AxisFault {
//        try {
//            ExecutionManagerAdminServiceValueHolder.getCarbonExecutionManagerService()
//                    .saveStreamMapping(ConfigurationMapper.mapStreamMapping(streamMappingDTO));
//            return true;
//        } catch (ExecutionManagerException e) {
//
//        }
        return true;
    }
}
