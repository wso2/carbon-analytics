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

    /**
     * return domain for a given template domain name
     *
     * @param domainName template domain name
     * @return template domain full details
     * @throws AxisFault
     */
    public TemplateDomainDTO getDomain(String domainName) throws AxisFault {
        try {
            return DomainMapper.mapDomain(ExecutionManagerAdminServiceValueHolder.getCarbonExecutorManagerService()
                    .getDomain(domainName));
        } catch (Exception e) {
            log.error("Error occurred when getting domain " + domainName, e);
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * return domain for a given template domain name
     *
     * @param domainName template domain name
     * @return template domain full details
     * @throws AxisFault
     */
    public TemplateDomainInfoDTO getDomainInfo(String domainName) throws AxisFault {
        try {
            return DomainMapper.mapDomainInfo(ExecutionManagerAdminServiceValueHolder.getCarbonExecutorManagerService()
                    .getDomain(domainName));
        } catch (Exception e) {
            log.error("Error occurred when getting domain " + domainName, e);
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * return all available template domain information
     *
     * @return all template domain information
     * @throws org.apache.axis2.AxisFault
     */
    public TemplateDomainInfoDTO[] getAllDomainsInfo() throws AxisFault {
        try {
            return DomainMapper.mapDomainsInfo(new ArrayList<TemplateDomain>(ExecutionManagerAdminServiceValueHolder
                    .getCarbonExecutorManagerService().getAllDomains()));
        } catch (Exception e) {
            log.error("Error occurred when getting all domains ", e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    /**
     * return all available template domains
     *
     * @return all template domain information
     * @throws org.apache.axis2.AxisFault
     */
    public TemplateDomainDTO[] getAllDomains() throws AxisFault {
        try {
            return DomainMapper.mapDomains(new ArrayList<TemplateDomain>(ExecutionManagerAdminServiceValueHolder
                    .getCarbonExecutorManagerService().getAllDomains()));
        } catch (Exception e) {
            log.error("Error occurred when getting all domains ", e);
            throw new AxisFault(e.getMessage(), e);
        }
    }


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
                    ExecutionManagerAdminServiceValueHolder.getCarbonExecutorManagerService()
                            .getConfigurations(domainName)));
        } catch (Exception e) {
            log.error("Error occurred when getting configurations for domain " + domainName, e);
            throw new AxisFault(e.getMessage());
        }
    }

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
                    .getCarbonExecutorManagerService().getConfiguration(domainName, configName));
        } catch (Exception e) {
            log.error("Error occurred when getting template configuration " + configName, e);
            throw new AxisFault(e.getMessage());
        }
    }

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
                    ExecutionManagerAdminServiceValueHolder.getCarbonExecutorManagerService()
                            .getConfigurations(domainName)));
        } catch (Exception e) {
            log.error("Error occurred when getting configurations for domain " + domainName, e);
            throw new AxisFault(e.getMessage());
        }
    }


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
                    .getCarbonExecutorManagerService().getConfiguration(domainName, configName));
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
            ExecutionManagerAdminServiceValueHolder.getCarbonExecutorManagerService()
                    .deleteConfiguration(domainName, configName);
            return true;
        } catch (ExecutionManagerException e) {
            log.error("Error occurred when deleting configuration " + configName, e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    /**
     * Create or update specified configuration
     *
     * @param configuration configuration data transfer object
     */
    public boolean saveConfiguration(TemplateConfigurationDTO configuration) throws AxisFault {
        try {
            ExecutionManagerAdminServiceValueHolder.getCarbonExecutorManagerService()
                    .saveConfiguration(ConfigurationMapper.mapConfiguration(configuration));
            return true;
        } catch (ExecutionManagerException e) {
            log.error("Error occurred when saving configuration " + configuration.getName(), e);
            throw new AxisFault(e.getMessage(), e);
        }
    }
}
