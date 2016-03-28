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
package org.wso2.carbon.event.execution.manager.core;

import org.wso2.carbon.event.execution.manager.core.exception.ExecutionManagerException;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.TemplateConfiguration;
import org.wso2.carbon.event.execution.manager.core.structure.domain.TemplateDomain;

import java.util.Collection;

/**
 * Interface consist of service methods which will be exposed by the ExecutionManagerService
 */
public interface ExecutionManagerService {

    /**
     * save template configuration xml files in wso2 carbon registry
     *
     * @param configuration configuration object which needs to be saved
     */
    public void saveConfiguration(TemplateConfiguration configuration) throws ExecutionManagerException;

    /**
     * provide all the loaded domains
     *
     * @return Domain list
     */
    public Collection<TemplateDomain> getAllDomains();

    /**
     * provide configurations of specified domain
     *
     * @param domainName domain template name
     * @return Domain list
     */
    public Collection<TemplateConfiguration> getConfigurations(String domainName);

    /**
     * get information of a specific domain
     *
     * @param domainName domain name
     * @return TemplateDomain object
     */
    public TemplateDomain getDomain(String domainName);


    /**
     * get information of a specific configuration
     *
     * @param domainName domain name
     * @param configName configuration name
     * @return TemplateConfig object
     */
    public TemplateConfiguration getConfiguration(String domainName, String configName);

    /**
     * delete template configuration when the name of configuration is given
     *
     * @param domainName domain name
     * @param configName template configuration name
     */
    public void deleteConfiguration(String domainName, String configName) throws ExecutionManagerException;


}
