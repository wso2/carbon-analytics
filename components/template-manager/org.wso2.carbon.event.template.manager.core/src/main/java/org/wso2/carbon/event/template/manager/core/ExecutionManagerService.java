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
package org.wso2.carbon.event.template.manager.core;

import org.wso2.carbon.event.template.manager.core.exception.ExecutionManagerException;
import org.wso2.carbon.event.template.manager.core.structure.configuration.ScenarioConfiguration;
import org.wso2.carbon.event.template.manager.core.structure.configuration.StreamMapping;
import org.wso2.carbon.event.template.manager.core.structure.domain.Domain;

import java.util.Collection;
import java.util.List;

/**
 * Interface consist of service methods which will be exposed by the ExecutionManagerService
 */
public interface ExecutionManagerService {

    /**
     * save template configuration xml files in wso2 carbon registry
     *
     * @param configuration configuration object which needs to be saved
     */
    public List<String> saveConfiguration(ScenarioConfiguration configuration) throws ExecutionManagerException;


    /**
     * save streamMapping object into the registry and deploy the corresponding execution plan.
     *
     * @param streamMappingList StreamMapping list
     * @param scenarioConfigName name field ScenarioConfiguration object
     * @param domainName domain name of the Domain corresponding to this scenarioConfig
     */
    public void saveStreamMapping(List<StreamMapping> streamMappingList, String scenarioConfigName,
                                  String domainName)
            throws ExecutionManagerException;


    /**
     * provide all the loaded domains
     *
     * @return Domain list
     */
    public Collection<Domain> getAllDomains();

    /**
     * provide configurations of specified domain
     *
     * @param domainName domain template name
     * @return Domain list
     */
    public Collection<ScenarioConfiguration> getConfigurations(String domainName)
            throws ExecutionManagerException;

    /**
     * get information of a specific domain
     *
     * @param domainName domain name
     * @return Domain object
     */
    public Domain getDomain(String domainName);


    /**
     * get information of a specific configuration
     *
     * @param domainName domain name
     * @param configName configuration name
     * @return TemplateConfig object
     */
    public ScenarioConfiguration getConfiguration(String domainName, String configName)  throws ExecutionManagerException;

    /**
     * delete specified scenario configuration when its name is given
     *
     * @param domainName domain name
     * @param configName template configuration name
     */
    public void deleteConfiguration(String domainName, String configName) throws ExecutionManagerException;

}
